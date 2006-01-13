/* Filename:    $RCSfile: BaseTask.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.core.ContextMgr;
import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.core.NavBoxIFace;
import edu.ku.brc.specify.core.NavBoxItemIFace;
import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.TaskPluginable;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.CommandDispatcher;
import edu.ku.brc.specify.ui.CommandListener;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.RolloverCommand;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.SubPaneMgrListener;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.dnd.GhostActionable;

/**
 * Abstract class to provide a base level of functionality for implementing a task.
 * It fully implements Taskable and partially implements TaskPluginable.
 * 
 * @author rods
 *
 */
public abstract class BaseTask implements Taskable, TaskPluginable, CommandListener, SubPaneMgrListener
{
    // Static Data Members
    private static Log log = LogFactory.getLog(BaseTask.class);
    
    // Data Members
    protected final String        name;
    protected final String        title;
    
    protected Vector<NavBoxIFace> navBoxes      = new Vector<NavBoxIFace>(); 
    protected ImageIcon           icon          = null;
    protected boolean             isInitialized = false;
    
    // SubPane List Management
    protected List<SubPaneIFace>  subPanes          = new ArrayList<SubPaneIFace>();
    protected Class               subPaneClassFilter = null;
    
    //protected boolean             ignoreNofications = false;
    
    /**
     * Constructor
     * @param name the name of the task (already localized)
     */
    public BaseTask(final String name, final String title)
    {
        this.name = name;
        this.title = title;
        
        ContextMgr.register(this);
        
        UICacheManager.getSubPaneMgr().addListener(this);
        
    }
    
    /**
     * Remove self from ContextMgr
     */
    public void finalize()
    {
        ContextMgr.unregister(this);
        UICacheManager.getSubPaneMgr().removeListener(this);
        

    }
    
     /**
     * Helper
     * @param catName catName
     * @param imageName imageName
     * @param hint v
     * @return return
     */
    protected ToolBarDropDownBtn createToolbarButton(final String catName,
                                                     final String imageName,
                                                     final String hint)
    {

        return createToolbarButton(catName, imageName, hint, null);
        
    }
    
    /**
     * Helper
     * @param catName catName
     * @param imageName imageName
     * @param hint hint
     * @return drop down btn
     */
    protected ToolBarDropDownBtn createToolbarButton(final String catName,
                                                     final String imageName,
                                                     final String hint,
                                                     final List<JComponent> menus)
    {
        String name = getResourceString(catName);
        
        icon = IconManager.getIcon(catName, IconManager.IconSize.Std16);
        
        ToolBarDropDownBtn btn = new ToolBarDropDownBtn(name, IconManager.getIcon(catName, IconManager.IconSize.Std24), JButton.BOTTOM, menus);
        btn.setStatusBarHintText(getResourceString(hint));
        
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                requestContext();
            }
        });
        return btn;
    }

    /**
     * Helper method to add an item to the navbox
     * @param navBox navBox
     * @param labelText navBox
     * @param cmdGroup navBox
     * @param cmdStr cmdStr
     * @param data data
     * @return btn
     */
    protected NavBoxItemIFace addNavBoxItem(final NavBox navBox, 
                                            final String labelText, 
                                            final String cmdGroup,
                                            final String cmdStr, 
                                            final Object data)
    {
        NavBoxItemIFace nb = NavBox.createBtn(labelText, name, IconManager.IconSize.Std16);
        RolloverCommand rb = (RolloverCommand)nb;
        
        // This is part of the "DndDeletable" Interface, 
        // the object is responsible for knowing how to delete itself.
        CommandAction delRSCmd = new CommandAction(cmdGroup, cmdStr, data);
        rb.setCommandAction(delRSCmd);
        
        JPopupMenu popupMenu = rb.getPopupMenu();
        
        JMenuItem delMenuItem = new JMenuItem(getResourceString("Delete"));
        delMenuItem.addActionListener(new RSAction(delRSCmd));
        popupMenu.add(delMenuItem);
        
        navBox.add(nb);
        
        if (nb instanceof GhostActionable)
        {
            GhostActionable ga = (GhostActionable)nb;
            ga.createMouseInputAdapter(); // this makes it draggable
            ga.setData(data);
        }
        return nb;
    }
    
    
    /**
     * Adds a SubPane to the Mgr and caches a pointer to it
     * @param subPane the subpane in question
     */
    protected void addSubPaneToMgr(final SubPaneIFace subPane)
    {
        boolean okToAdd = true;
        if (subPaneClassFilter != null)
        {
            okToAdd = false;
            // Now Check super classes 
            Class superclass = subPane.getClass();
            while (superclass != null) 
            {
                if (superclass == subPaneClassFilter)
                {
                    okToAdd = true;
                    break;
                }
                superclass = superclass.getSuperclass();
            }
        }
        
        if (okToAdd)
        {
            UICacheManager.getSubPaneMgr().addPane(subPane);
        }
    }
    
    /**
     * Removes a SubPane from the Mgr
     * @param subPane the subpane in question
     */
    protected void removeSubPaneFromMgr(final SubPaneIFace subPane)
    {
        UICacheManager.getSubPaneMgr().removePane(subPane);
    }

    /**
     * Returns the initial pane for this task, may be a blank (empty) pane, but shouldn't null
     * @return Returns the initial pane for this task, may be a blank (empty) pane, but shouldn't null
     */
    public abstract SubPaneIFace getStarterPane();

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------
    
    /**
     * 
     */
    public void initialize()
    {
        isInitialized = true;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();
        return navBoxes;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getIcon()
     */
    public Icon getIcon()
    {
        return icon;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#requestContext()
     */
    public void requestContext()
    {
        ContextMgr.requestContext(this);
        
        UICacheManager.getSubPaneMgr().addPane(getStarterPane());
    }
    
    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public String getName()
    {
        return name;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getTitle()
     */
    public String getTitle()
    {
        return title;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public abstract List<ToolBarItemDesc> getToolBarItems();
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public abstract List<MenuItemDesc> getMenuItems();

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#installPrefs()
     */
    public void installPrefs()
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#removePrefs()
     */
    public void removePrefs()
    {
        
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    public void doCommand(CommandAction cmdAction)
    {
        log.error("Command sent to task ["+name+"] and was not processed.");
    }
    
    //--------------------------------------------------------------
    // SubPaneMgrListener Interface
    //--------------------------------------------------------------
    
    /**
     * Notication that a SubPane was added to the manager
     * @param subPane the subpane that was added
     */
    public void subPaneAdded(SubPaneIFace subPane)
    {
        subPanes.add(subPane);
    }
    
    /**
     * Notication that a SubPane was removed from the manager
     * @param subPane the subpane that was removed
     */
    public void subPaneRemoved(SubPaneIFace subPane)
    {
        subPanes.remove(subPane);
    }
    
    
    /**
     * Notication that a SubPane was removed from the manager
     * @param subPane the subpane that was removed
     */
    public void subPaneShown(SubPaneIFace subPane)
    {
        
    }

    
    
    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------
 
    /**
     * XXX This is now generic and should be moved out of here 
     * @author rods
     *
     */
    class RSAction implements ActionListener 
    {
        protected CommandAction cmdAction;
        
        public RSAction(final CommandAction cmdAction)
        {
            this.cmdAction = cmdAction;
         }
        
        public void actionPerformed(ActionEvent e) 
        {
            CommandDispatcher.dispatch(cmdAction);
        }
    }
 
   

}
