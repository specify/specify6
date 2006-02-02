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
import edu.ku.brc.specify.tasks.subpane.DroppableFormObject;
import edu.ku.brc.specify.tasks.subpane.FormPane;
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
    protected boolean             taskCentricPanesOnly = true;
    
    // Data Memebers needed for support "recent form pane" management
    protected FormPane  recentFormPane = null;    


    
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
                                            final Object data,
                                            final int    position)
    {
        NavBoxItemIFace nb = NavBox.createBtn(labelText, name, IconManager.IconSize.Std16);
        RolloverCommand rb = (RolloverCommand)nb;
        
        // This is part of the "DndDeletable" Interface, 
        // the object is responsible for knowing how to delete itself.
        CommandAction delRSCmd = new CommandAction(cmdGroup, cmdStr, data);
        rb.setCommandAction(delRSCmd);
        
        if (cmdStr != null)
        {       
            JPopupMenu popupMenu = rb.getPopupMenu();
            
            JMenuItem delMenuItem = new JMenuItem(getResourceString("Delete"));
            delMenuItem.addActionListener(new RSAction(delRSCmd));
            popupMenu.add(delMenuItem);
        }
            
        if (position == -1)
        {
            navBox.add(nb);
            
        } else
        {
            navBox.insert(nb, false, position);
        }
        
        if (nb instanceof GhostActionable)
        {
            GhostActionable ga = (GhostActionable)nb;
            ga.createMouseInputAdapter(); // this makes it draggable
            ga.setData(data);
        }
        return nb;
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
        return addNavBoxItem(navBox, labelText,  cmdGroup, cmdStr, data, -1);
    }
    
   
    /**
     * Adds a SubPane to the Mgr and caches a pointer to it
     * @param subPane the subpane in question
     */
    protected void addSubPaneToMgr(final SubPaneIFace subPane)
    {
        //if (isSuperClassOf(subPane, subPaneClassFilter))
        //{
        //    UICacheManager.getSubPaneMgr().addPane(subPane);
        //}
        
        UICacheManager.getSubPaneMgr().addPane(subPane);

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
    // Recent Pane Management
    //-------------------------------------------------------
 
    /**
     * Looks up a SubPane by the viewset name and form id and data
     * @param viewSetName the view set name
     * @param formId the form id
     * @return the subpane that matches
     */
    protected FormPane getFormPane(final String viewSetName, final int formId, final Object data)
    {
        for (SubPaneIFace sp : subPanes)
        {
            if (sp instanceof FormPane) // should always a FormPane
            {
                FormPane fp = (FormPane)sp;
                
                if (viewSetName.equals(fp.getViewSetName()) && 
                    formId == fp.getFormId() && 
                    data == fp.getData())
                {
                    return fp;
                }
            }
        }
        return null;
    }
    
    /**
     * Looks to see if a form already exists for this request and shows it
     * otherwise it creates a form and add it to the SubPaneMgr
     */
    protected FormPane createFormPanel(RolloverCommand roc)
    {
        DroppableFormObject dfo = (DroppableFormObject)roc.getData();
        return createFormPanel(dfo.getViewSetName(), dfo.getFormId(), dfo.getData());
    }
    
    /**
     * Looks to see if a form already exists for this request and shows it
     * otherwise it creates a form and add it to the SubPaneMgr
     */
    protected FormPane createFormPanel(final String viewsetName, final int formId, final Object data)
    {
        FormPane fp = null;
        
        if (recentFormPane != null && recentFormPane.getComponentCount() == 0)
        {
            recentFormPane.createForm(viewsetName, formId, data);
            fp = recentFormPane;
            
        } else
        {
            fp = getFormPane(viewsetName, formId, data);
            if (fp != null)
            {
                UICacheManager.getSubPaneMgr().showPane(fp.getName());
                
            } else
            {
                recentFormPane = new FormPane(name, this, viewsetName, formId, data);            
                addSubPaneToMgr(recentFormPane);
                fp = recentFormPane; 
            }
        }
        return fp;
    }
    
    /**
     * Checks to see if it is the the only panel of its kind and
     * if it is it clears the panel instead of removing it, if there are more panels of that kind
     * then it removes it. The idea is that it doesn't want to remove all the panels of a certain kind. 
     * @param viewName the view name
     * @param viewId the form's id
     * @param data the data in the form
     */
    protected void removePanelForData(final String viewName, final int viewId, Object data)
    {
        FormPane currPane   = null;
        FormPane fp         = null;
        int      cnt        = 0;
        for (SubPaneIFace subPane : subPanes)
        {
            fp = (FormPane)subPane;
            if (viewName.equals(fp.getViewSetName()) &&  viewId == fp.getFormId())
            {
                if (fp.getData() == data)
                {
                    currPane = fp;
                }
                cnt++;
            }               
        }
        if (cnt == 1)
        {
            currPane.clearForm();
            
        } else if (cnt > 0)  // wierd we should always find something
        {
            UICacheManager.getSubPaneMgr().removePane(fp);
        } 
    }
    


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
        if (!taskCentricPanesOnly || subPane.getTask() == this)
        {
            subPanes.add(subPane);
        }
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
