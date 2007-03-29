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
package edu.ku.brc.af.tasks;

import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.SubPaneMgrListener;
import edu.ku.brc.af.core.TaskCommandDef;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.subpane.DroppableFormObject;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.specify.ui.ChooseRecordSetDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandActionWrapper;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MemoryDropDownButton;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.Trash;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.Viewable;

/**
 * Abstract class to provide a base level of functionality for implementing a task.
 * It fully implements Taskable.
 
 * @code_status Code Freeze
 **
 * @author rods
 *
 */
public abstract class BaseTask implements Taskable, CommandListener, SubPaneMgrListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(BaseTask.class);
    
    protected static final String APP_CMD_TYPE    = "App";
    protected static final String APP_RESTART_ACT = "Restart";
    protected static final String DB_CMD_TYPE     = "Database";

    
    protected static final String SAVE_CMD_ACT   = "Save";
    protected static final String INSERT_CMD_ACT = "Insert";
    protected static final String DELETE_CMD_ACT = "Delete";
    protected static final String UPDATE_CMD_ACT = "Update";

    // Data Members
    protected final String        name;
    protected final String        title;

    protected Vector<NavBoxIFace> navBoxes      = new Vector<NavBoxIFace>();
    protected ImageIcon           icon          = null;
    protected boolean             isInitialized = false;

    // Members needed for initialization
    protected List<TaskCommandDef> commands     = null;

    // SubPane List Management
    protected List<SubPaneIFace>  subPanes             = new ArrayList<SubPaneIFace>();
    protected boolean             taskCentricPanesOnly = true;
    protected boolean             closeOnLastPane      = false;
    protected SubPaneIFace        starterPane          = null;
    protected boolean             isVisible            = true;

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

        SubPaneMgr.getInstance().addListener(this);
        
        this.icon = IconManager.getIcon(name, IconManager.IconSize.Std16);
    }

    /**
     * Remove self from ContextMgr.
     */
    @Override
    public void finalize()
    {
        ContextMgr.unregister(this);
        SubPaneMgr.getInstance().removeListener(this);
    }

    /**
     * Helper.
     * @param label the (localized) label string
     * @param iconName the name of the icon (as registered with IconManager)
     * @param hint a (localized) hint string
     * @return the drop down button
     */
    protected ToolBarDropDownBtn createToolbarButton(final String label,
                                                     final String iconName,
                                                     final String hint)
    {

        return createToolbarButton(label, iconName, hint, null);

    }

    /**
     * Helper.
     * @param label the (localized) label string
     * @param iconName the name of the icon (as registered with IconManager)
     * @param hint a (localized) hint string
     * @param menus a List of JComponents to be added in a drop down box
     * @param actionListener the listener to notify when the button is clicked
     * @return the drop down button
     */
    protected ToolBarDropDownBtn createToolbarButton(final String label,
                                                     final String iconName,
                                                     final String hint,
                                                     final List<JComponent> menus,
                                                     final ActionListener actionListener )
    {
        ImageIcon buttonIcon = IconManager.getIcon(iconName, IconManager.IconSize.Std24);

        ToolBarDropDownBtn btn = new ToolBarDropDownBtn(label, buttonIcon, SwingConstants.BOTTOM, menus);
        btn.setStatusBarHintText(hint);

        btn.addActionListener(actionListener);
        return btn;
    }
    
    protected MemoryDropDownButton createMemoryToolbarButton(final String label,
                                                             final String iconName,
                                                             final String hint,
                                                             final List<JComponent> menus)
    {
        ImageIcon buttonIcon = IconManager.getIcon(iconName, IconManager.IconSize.Std24);

        MemoryDropDownButton btn = new MemoryDropDownButton(label, buttonIcon, SwingConstants.BOTTOM, menus);
        btn.setStatusBarHintText(hint);
        return btn;
    }
    
    /**
     * Helper.
     * @param summaryLabel the (localized) label string
     * @param iconName the name of the icon (as registered with IconManager)
     * @param hint a (localized) hint string
     * @param menus a List of JComponents to be added in a drop down box
     * @return the drop down button
     */
    protected ToolBarDropDownBtn createToolbarButton(final String catName,
                                                     final String imageName,
                                                     final String hint,
                                                     final List<JComponent> menus)
    {
    	ActionListener al = new ActionListener()
    	{
            public void actionPerformed(ActionEvent e)
            {
                requestContext();
            }
    	};
    	return createToolbarButton(catName,imageName,hint,menus,al);
    }

    /**
     * Helper method to add an item to the navbox.
     * @param navBox navBox
     * @param labelText labelText
     * @param icoonName icon name
     * @param delCmdAction delCmdAction
     * @param data data
     * @return btn
     */
    protected NavBoxItemIFace addNavBoxItem(final NavBox        navBox,
                                            final String        labelText,
                                            final String        iconName,
                                            final CommandAction delCmdAction,
                                            final Object        data,
                                            final int           position)
    {
        NavBoxItemIFace nbi = makeDnDNavBtn(navBox, labelText, iconName, null, delCmdAction, true, position);
        if (nbi instanceof GhostActionable)
        {
            ((GhostActionable)nbi).setData(data != null ? data : this);
        }
        return nbi;
    }
    
    protected NavBoxItemIFace makeDnDNavBtn(final NavBox        navBox,
                                            final String        labelText,
                                            final String        iconName,
                                            final CommandAction cmdAction,
                                            final CommandAction delCmdAction,
                                            final Class         flavorClass,
                                            final String        dragFlavor,
                                            final String        dropFlavor)
{
        RolloverCommand roc = (RolloverCommand)makeDnDNavBtn(navBox, labelText, iconName, cmdAction, delCmdAction, true, -1);
        roc.addDragDataFlavor(new DataFlavor(Workbench.class, dragFlavor));
        roc.addDropDataFlavor(new DataFlavor(flavorClass, dropFlavor));
        return (NavBoxItemIFace)roc;
    }
    
    protected NavBoxItemIFace makeDnDNavBtn(final NavBox        navBox,
                                            final String        labelText,
                                            final String        iconName,
                                            final CommandAction cmdAction,
                                            final CommandAction delCmdAction,
                                            final boolean       makeDraggable)
    {
        return makeDnDNavBtn(navBox, labelText, iconName, cmdAction, delCmdAction, makeDraggable, -1);
    }
 
    protected NavBoxItemIFace makeDnDNavBtn(final NavBox        navBox,
                                            final String        labelText,
                                            final String        iconName,
                                            final CommandAction cmdAction,
                                            final CommandAction delCmdAction,
                                            final boolean       makeDraggable,
                                            final int           position)
    {
        NavBoxItemIFace nb = NavBox.createBtn(labelText, iconName, IconManager.IconSize.Std16);
        if (cmdAction != null)
        {
            NavBoxButton nbb = (NavBoxButton)nb;
            nbb.addActionListener(new CommandActionWrapper(cmdAction));
            nbb.setData(cmdAction);
        }
        
        if (delCmdAction != null)
        {
            ((NavBoxButton)nb).setDeleteCommandAction(delCmdAction);
        }
        
        if (position == -1)
        {
            navBox.add(nb);

        } else
        {
            navBox.insert(nb, false, position);
        }

        // Make the Btn Draggable
        if (nb instanceof GhostActionable)
        {
            GhostActionable ga = (GhostActionable)nb;
            if (makeDraggable)
            {
                ga.createMouseInputAdapter(); // this makes it draggable
                ga.getMouseInputAdapter().registerWithGlassPane();
                
            } else
            {
                UICacheManager.getGlassPane().add(ga);
            }
        }
        return nb;
    }

    /**
     * Removes a NavBoxItemIFace from a NavBoxIFace.
     * @param box the containing box
     * @param nbi the item to be removed
     */
    protected void deleteDnDBtn(final NavBoxIFace box, final NavBoxItemIFace nbi)
    {
        RolloverCommand roc = (RolloverCommand)nbi;
        if (nbi instanceof GhostActionable)
        {
            UICacheManager.getGlassPane().remove((GhostActionable)roc);
            
            box.remove(nbi);
            
            // XXX this is pathetic and needs to be made generic
            NavBoxMgr.getInstance().invalidate();
            NavBoxMgr.getInstance().doLayout();
            NavBoxMgr.getInstance().repaint();
            UICacheManager.forceTopFrameRepaint();
        }
    }
    
    /**
     * Helper method to add an item to the navbox.
     * @param navBox navBox
     * @param labelText navBox
     * @param iconName icon name
     * @param cmdAction cmdAction
     * @param delCmdAction delCmdAction
     * @param data data
     * @return btn
     */
    protected NavBoxItemIFace addNavBoxItem(final NavBox navBox,
                                            final String labelText,
                                            final String iconName,
                                            final CommandAction delCmdAction,
                                            final Object data)
    {
        return addNavBoxItem(navBox, labelText,  iconName, delCmdAction, data, -1);
    }
    
    /**
     * Sets up NavBoxItemIFace item as a draggable and adds the action
     * @param nbi the item
     * @param flavors the draggable flavors
     * @param navBoxAction the action to be performed
     */
    protected void setUpDraggable(final NavBoxItemIFace nbi, final DataFlavor[] flavors, final NavBoxAction navBoxAction)
    {
        NavBoxButton roc = (NavBoxButton)nbi;
        for (DataFlavor df : flavors)
        {
            roc.addDragDataFlavor(df);
        }
        roc.addActionListener(navBoxAction);
    }

    /**
     * Adds a SubPane to the Mgr and caches a pointer to it and clear the starterPane data member.
     * @param subPane the subpane in question
     */
    protected SubPaneIFace addSubPaneToMgr(final SubPaneIFace subPane)
    {
        if (starterPane != null)
        {
            SubPaneMgr.getInstance().replacePane(starterPane, subPane);
            starterPane = null;
            
        } else
        {
            SubPaneMgr.getInstance().addPane(subPane);
        }
        return subPane;
    }

    /**
     * Removes a SubPane from the Mgr.
     * @param subPane the subpane in question
     */
    protected void removeSubPaneFromMgr(final SubPaneIFace subPane)
    {
        SubPaneMgr.getInstance().removePane(subPane);
    }

    /**
     * Returns the initial pane for this task, may be a blank (empty) pane, but shouldn't null.
     * @return Returns the initial pane for this task, may be a blank (empty) pane, but shouldn't null
     */
    public abstract SubPaneIFace getStarterPane();


    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
    	return this.getClass();
    }

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
    protected FormPane createFormPanel(NavBoxButton nbb)
    {
        DroppableFormObject dfo = (DroppableFormObject)nbb.getData();
        return createFormPanel(dfo.getViewSetName(), DBTableIdMgr.getInstance().getDefaultFormNameById(dfo.getFormId()), null, dfo.getData(), null);
    }

    /**
     * Looks to see if a form already exists for this request and shows it
     * otherwise it creates a form and add it to the SubPaneMgr.  This call is for existing data objects.
     * For new objects use the call with "options".
     */
    protected FormPane createFormPanel(final String viewsetName, 
                                       final String viewName, 
                                       final String mode, 
                                       final Object data, 
                                       final ImageIcon paneIcon)
    {
        return createFormPanel(viewsetName, viewName, mode, data, MultiView.VIEW_SWITCHER, paneIcon);
    }

    /**
     * Looks to see if a form already exists for this request and shows it
     * otherwise it creates a form and add it to the SubPaneMgr.
     */
    protected FormPane createFormPanel(final String viewsetName, 
                                       final String viewName, 
                                       final String mode, 
                                       final Object data, 
                                       final int options,
                                       final ImageIcon paneIcon)
    {
        ImageIcon iconImg = paneIcon == null ? icon : paneIcon;
        FormPane fp = null;

        if (recentFormPane != null && recentFormPane.getComponentCount() == 0)
        {
            recentFormPane.createForm(viewsetName, viewName, null, data, options); // not new data object
            fp = recentFormPane;
            fp.setIcon(iconImg);
            
        } else
        {
            fp = getFormPane(viewsetName, viewName, data);
            if (fp != null)
            {
                fp.setIcon(iconImg);
                SubPaneMgr.getInstance().showPane(fp.getName());

            } else
            {
                recentFormPane = new FormPane(name, this, viewsetName, viewName, mode, data, options); // not new data object
                recentFormPane.setIcon(iconImg);
                //addSubPaneToMgr(recentFormPane);
                SubPaneMgr.replaceSimplePaneForTask(recentFormPane);
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
            if (currPane != null)
            {
                currPane.clearForm();
            }

        } else if (cnt > 0)  // wierd we should always find something
        {
            SubPaneMgr.getInstance().removePane(fp);
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
    
    /**
     * Returns the top FormViewObj for the current SubPaneIFace, or null.
     * @return the top FormViewObj for the current SubPaneIFace, or null.
     */
    protected FormViewObj getCurrentFormViewObj()
    {
        FormViewObj  formViewObj = null;
        SubPaneIFace subPane     = SubPaneMgr.getInstance().getCurrentSubPane();
        if (subPane != null)
        {
            MultiView mv = subPane.getMultiView();
            if (mv != null)
            {
                Viewable currentViewable = mv.getCurrentView();
                if (currentViewable != null && currentViewable instanceof FormViewObj)
                {
                    formViewObj = (FormViewObj)currentViewable;
                }
            }
        }
        return formViewObj;
    }
    
    

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            isInitialized = true;
            
            // IMportant for reinitializing
            navBoxes.clear();
            if (commands != null)
            {
                commands.clear();
            }
        }
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getNavBoxes()
     */
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();
        
        return navBoxes;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getIcon()
     */
    public ImageIcon getImageIcon()
    {
        return icon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#requestContext()
     */
    public void requestContext()
    {
        ContextMgr.requestContext(this);

        SubPaneMgr.getInstance().addPane(getStarterPane());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getTitle()
     */
    public String getTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getToolBarItems()
     */
    public abstract List<ToolBarItemDesc> getToolBarItems();

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getMenuItems()
     */
    public abstract List<MenuItemDesc> getMenuItems();

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#initialize(java.util.List)
     */
    public void initialize(List<TaskCommandDef> cmds, final boolean isVisibleArg)
    {
        this.commands  = cmds;
        this.isVisible = isVisibleArg;
        initialize(); // initializes the Taskable
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isVisible()
     */
    public boolean isVisible()
    {
        return isVisible;
    }

    /**
     * Displays UI that asks the user to select a predefined label.
     * @param tableId the table id
     * @return returns the selected RecordSet or null
     */
    public static RecordSetIFace askForRecordSet(final int tableId)
    {
        ChooseRecordSetDlg dlg = new ChooseRecordSetDlg((Frame)UICacheManager.get(UICacheManager.TOPFRAME), tableId);
        if (dlg.hasRecordSets())
        {
            if (dlg.getRecordSets().size() == 1)
            {
                return dlg.getRecordSets().get(0);
                
            }
            // else
            dlg.setVisible(true); // modal (waits for answer here)
            return dlg.getSelectedRecordSet();
        }
        
        // else
        return null;
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.CommandListener#doCommand(edu.ku.brc.af.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        log.error("Command sent to task ["+name+"] and was not processed.");
    }

    //--------------------------------------------------------------
    // SubPaneMgrListener Interface
    //--------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneMgrListener#subPaneAdded(edu.ku.brc.af.ui.SubPaneIFace)
     */
    public void subPaneAdded(SubPaneIFace subPane)
    {
        if (!taskCentricPanesOnly || subPane.getTask() == this)
        {
            subPanes.add(subPane);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneMgrListener#subPaneRemoved(edu.ku.brc.af.ui.SubPaneIFace)
     */
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        //if (starterPane == null && subPane.getTask() == this && !closeOnLastPane && subPanes.size() == 1)
        //{
        //    SubPaneMgr.getInstance().replacePane(subPane, starterPane = getStarterPane());
        //    return;
        //}
        // else
        subPanes.remove(subPane);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneMgrListener#subPaneShown(edu.ku.brc.af.ui.SubPaneIFace)
     */
    public void subPaneShown(SubPaneIFace subPane)
    {
        // do nothing
    }

    //--------------------------------------------------------------
    // NavBoxButton Helpers
    //--------------------------------------------------------------

    /**
      * Helper method for registering a NavBoxItem as a GhostMouseDropAdapter
      * @param navBox the parent box for the nbi to be added to
      * @param navBoxItemDropZone the nbi in question
      * @return returns the new NavBoxItem
      */
     protected NavBoxItemIFace addToNavBoxAndRegisterAsDroppable(final DataFlavor          dataFlavor,
                                                                 final NavBox              navBox,
                                                                 final NavBoxItemIFace     nbi,
                                                                 final Map<String, String> params)
     {
         NavBoxButton roc = (NavBoxButton)nbi;
         roc.setData(params);

         // When Being Dragged
         roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
         roc.addDragDataFlavor(dataFlavor);

         // When something is dropped on it
         roc.addDropDataFlavor(RecordSetTask.RECORDSET_FLAVOR);

         navBox.add(nbi);

         return nbi;
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
