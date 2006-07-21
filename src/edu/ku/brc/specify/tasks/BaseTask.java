/* This library is free software; you can redistribute it and/or
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

import org.apache.log4j.Logger;

import edu.ku.brc.specify.core.ContextMgr;
import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.core.NavBoxIFace;
import edu.ku.brc.specify.core.NavBoxItemIFace;
import edu.ku.brc.specify.core.TaskCommandDef;
import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.dbsupport.DBTableIdMgr;
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
    private static final Logger log = Logger.getLogger(BaseTask.class);

    // Data Members
    protected final String        name;
    protected final String        title;

    protected Vector<NavBoxIFace> navBoxes      = new Vector<NavBoxIFace>();
    protected ImageIcon           icon          = null;
    protected boolean             isInitialized = false;

    // Members needed for initialization
    protected List<TaskCommandDef> commands     = null;

    // SubPane List Management
    protected List<SubPaneIFace>  subPanes          = new ArrayList<SubPaneIFace>();
    protected boolean             taskCentricPanesOnly = true;

    // Data Members needed for support "recent form pane" management
    protected FormPane  recentFormPane = null;



    /**
     * Constructor.
     * @param name the name of the task
     * @param title the title of the task (already localized)
     */
    public BaseTask(final String name, final String title)
    {
        this.name  = name;
        this.title = title;

        ContextMgr.register(this);

        UICacheManager.getSubPaneMgr().addListener(this);

    }

    /**
     * Remove self from ContextMgr.
     */
    public void finalize()
    {
        ContextMgr.unregister(this);
        UICacheManager.getSubPaneMgr().removeListener(this);


    }

     /**
     * Helper.
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
     * Helper.
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
     * Helper method to add an item to the navbox.
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
     * Helper method to add an item to the navbox.
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
     * Adds a SubPane to the Mgr and caches a pointer to it.
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
     * Removes a SubPane from the Mgr.
     * @param subPane the subpane in question
     */
    protected void removeSubPaneFromMgr(final SubPaneIFace subPane)
    {
        UICacheManager.getSubPaneMgr().removePane(subPane);
    }

    /**
     * Returns the initial pane for this task, may be a blank (empty) pane, but shouldn't null.
     * @return Returns the initial pane for this task, may be a blank (empty) pane, but shouldn't null
     */
    public abstract SubPaneIFace getStarterPane();


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getTaskClass()
     */
    public abstract Class getTaskClass();

    //-------------------------------------------------------
    // Recent Pane Management
    //-------------------------------------------------------

    /**
     * Looks up a SubPane by the viewset name and form id and data.
     * @param viewSetName the view set name
     * @param viewName the form id
     * @return the subpane that matches
     */
    protected FormPane getFormPane(final String viewSetName, final String viewName, final Object data)
    {
        for (SubPaneIFace sp : subPanes)
        {
            if (sp instanceof FormPane) // should always a FormPane
            {
                FormPane fp = (FormPane)sp;

                if (viewSetName.equals(fp.getViewSetName()) &&
                    viewName == fp.getViewName() &&
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
     * otherwise it creates a form and add it to the SubPaneMgr.
     */
    protected FormPane createFormPanel(RolloverCommand roc)
    {
        DroppableFormObject dfo = (DroppableFormObject)roc.getData();
        return createFormPanel(dfo.getViewSetName(), DBTableIdMgr.lookupDefaultFormNameById(dfo.getFormId()), null, dfo.getData());
    }

    /**
     * Looks to see if a form already exists for this request and shows it
     * otherwise it creates a form and add it to the SubPaneMgr
     */
    protected FormPane createFormPanel(final String viewsetName, final String viewName, final String mode, final Object data)
    {
        FormPane fp = null;

        if (recentFormPane != null && recentFormPane.getComponentCount() == 0)
        {
            recentFormPane.createForm(viewsetName, viewName, null, data, false);
            fp = recentFormPane;

        } else
        {
            fp = getFormPane(viewsetName, viewName, data);
            if (fp != null)
            {
                UICacheManager.getSubPaneMgr().showPane(fp.getName());

            } else
            {
                recentFormPane = new FormPane(name, this, viewsetName, viewName, mode, data, false);
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
     * @param viewSetName the viewset name
     * @param viewName the form's name
     * @param data the data in the form
     */
    protected void removePanelForData(final String viewSetName, final String viewName, Object data)
    {
        FormPane currPane   = null;
        FormPane fp         = null;
        int      cnt        = 0;
        for (SubPaneIFace subPane : subPanes)
        {
            fp = (FormPane)subPane;
            if (viewSetName.equals(fp.getViewSetName()) &&  viewName == fp.getViewName())
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

    /**
     * Return a NavBoxItem by title (label).
     * @param navBox to get the items from
     * @param boxName the title of the NavBoxItem
     * @return Return a NavBoxItem by title
     */
    public static NavBoxItemIFace getBoxByTitle(final NavBox navBox, final String boxName)
    {
        for (NavBoxItemIFace nbi : navBox.getItems())
        {
            if (nbi.getTitle().equals(boxName))
            {
                return nbi;
            }
        }
        return null;
    }

    /**
     * Return a NavBoxItem by title (label) from any of the NavBoxes.
     * @param boxName the title of the NavBoxItem
     * @return Return a NavBoxItem by title
     */
    public NavBoxItemIFace getBoxByTitle(final String boxName)
    {
        for (NavBoxIFace navBox : navBoxes)
        {
            for (NavBoxItemIFace nbi : navBox.getItems())
            {
                if (nbi.getTitle().equals(boxName))
                {
                    return nbi;
                }
            }
        }
        return null;
    }

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /**
     * Sets initialization to true.
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#initialize(java.util.List)
     */
    public void initialize(List<TaskCommandDef> cmds)
    {
        this.commands = cmds;
        initialize(); // initializes the Taskable
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        log.error("Command sent to task ["+name+"] and was not processed.");
    }

    //--------------------------------------------------------------
    // SubPaneMgrListener Interface
    //--------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.SubPaneMgrListener#subPaneAdded(edu.ku.brc.specify.ui.SubPaneIFace)
     */
    public void subPaneAdded(SubPaneIFace subPane)
    {
        if (!taskCentricPanesOnly || subPane.getTask() == this)
        {
            subPanes.add(subPane);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.SubPaneMgrListener#subPaneRemoved(edu.ku.brc.specify.ui.SubPaneIFace)
     */
    public void subPaneRemoved(SubPaneIFace subPane)
    {
        subPanes.remove(subPane);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.SubPaneMgrListener#subPaneShown(edu.ku.brc.specify.ui.SubPaneIFace)
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
