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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.SubPaneMgrListener;
import edu.ku.brc.af.core.TaskCommandDef;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.db.CommandActionForDB;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.specify.ui.db.AskForNumbersDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandActionWrapper;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MemoryDropDownButton;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.Trash;


/**
 * Abstract class to provide a base level of functionality for implementing a task.
 * It fully implements Taskable.
 
 * @code_status Code Freeze
 **
 * @author rods
 *
 */
public abstract class BaseTask implements Taskable, CommandListener, SubPaneMgrListener, SecurityOptionIFace
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(BaseTask.class);
    
    protected static final String  securityPrefix    = "Task"; //$NON-NLS-1$
    
    public enum ASK_TYPE { Cancel, EnterCats, ChooseRS}

    
    public static final String APP_CMD_TYPE      = "App"; //$NON-NLS-1$
    public static final String APP_START_ACT     = "StartUp"; //$NON-NLS-1$
    public static final String APP_RESTART_ACT   = "AppRestart"; //$NON-NLS-1$
    public static final String APP_SHUTDOWN_ACT  = "Shutdown"; //$NON-NLS-1$
    public static final String DB_CMD_TYPE       = "Database"; //$NON-NLS-1$
    public static final String APP_REQ_RESTART   = "AppReqRestart"; //$NON-NLS-1$
    public static final String APP_REQ_EXIT      = "AppReqExit"; //$NON-NLS-1$

    
    public static final String SAVE_CMD_ACT      = "Save"; //$NON-NLS-1$
    public static final String INSERT_CMD_ACT    = "Insert"; //$NON-NLS-1$
    public static final String DELETE_CMD_ACT    = "Delete"; //$NON-NLS-1$
    public static final String UPDATE_CMD_ACT    = "Update"; //$NON-NLS-1$
    public static final String OPEN_FORM_CMD_ACT = "OPEN_FORM"; //$NON-NLS-1$

    protected static Font         toolbarBtnFont = null;

    // Data Members
    protected String              name;
    protected String              title;
    protected String              shortDesc;
    protected String              iconName;

    protected List<NavBoxIFace>   navBoxes      = new Vector<NavBoxIFace>();
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
    protected FormPane            recentFormPane       = null;

    protected boolean             isShowDefault        = false;
    protected boolean             isEnabled            = true;
    
    // These are needed to support enabling and disabling of the task
    protected Vector<ToolBarItemDesc> toolbarItems     = null;
    protected Vector<MenuItemDesc>    menuItems        = null;
    
    // Security
    protected PermissionIFace     permissions          = null;
    
    /**
     * Default Constructor 
     * (when this is used the extending class MUST manually set the Name, Title and Icon).
     */
    public BaseTask()
    {
        ContextMgr.register(this);
        SubPaneMgr.getInstance().addListener(this);
        CommandDispatcher.register(APP_CMD_TYPE, this);
    }

    /**
     * Constructor.
     * @param name the name of the task
     * @param title the title of the task (already localized)
     */
    public BaseTask(final String name, final String title)
    {
        this();
        
        this.name     = name;
        this.title    = title;
        this.iconName = name;
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
     * @return the toolbarBtnFont
     */
    public static Font getToolbarBtnFont()
    {
        return toolbarBtnFont;
    }

    /**
     * @param toolbarBtnFont the toolbarBtnFont to set
     */
    public static void setToolbarBtnFont(Font toolbarBtnFont)
    {
        BaseTask.toolbarBtnFont = toolbarBtnFont;
    }

    /**
     * Sets the icon from the IconCacheManager with the appropriate size.
     * @param iconName the name of the icon to use
     */
    public void setIconName(final String iconName)
    {
        this.iconName = iconName;
    }

    /**
     * Helper.
     * @param label the (localized) label string
     * @param iconNameArg the name of the icon (as registered with IconManager)
     * @param hint a (localized) hint string
     * @return the drop down button
     */
    protected ToolBarDropDownBtn createToolbarButton(final String label,
                                                     final String iconNameArg,
                                                     final String hint)
    {

        return createToolbarButton(label, iconNameArg, hint, null);

    }

    /**
     * Helper.
     * @param label the (localized) label string
     * @param iconNameArg the name of the icon (as registered with IconManager)
     * @param hint a (localized) hint string
     * @param menus a List of JComponents to be added in a drop down box
     * @param actionListener the listener to notify when the button is clicked
     * @return the drop down button
     */
    protected ToolBarDropDownBtn createToolbarButton(final String label,
                                                     final String iconNameArg,
                                                     final String hint,
                                                     final List<JComponent> menus,
                                                     final ActionListener actionListener )
    {
        ImageIcon buttonIcon = IconManager.getIcon(iconNameArg, IconManager.IconSize.Std24);

        ToolBarDropDownBtn btn = new ToolBarDropDownBtn(label, buttonIcon, SwingConstants.BOTTOM, menus);
        if (toolbarBtnFont != null)
        {
            btn.setFont(toolbarBtnFont);
        }
        btn.setStatusBarHintText(hint);

        btn.addActionListener(actionListener);
        return btn;
    }
    
    /**
     * @param label
     * @param iconNameArg
     * @param hint
     * @param menus
     * @return
     */
    protected MemoryDropDownButton createMemoryToolbarButton(final String label,
                                                             final String iconNameArg,
                                                             final String hint,
                                                             final List<JComponent> menus)
    {
        ImageIcon buttonIcon = IconManager.getIcon(iconNameArg, IconManager.IconSize.Std24);

        MemoryDropDownButton btn = new MemoryDropDownButton(label, buttonIcon, SwingConstants.BOTTOM, menus);
        btn.setStatusBarHintText(hint);
        return btn;
    }
    
    /**
     * Helper.
     * @param summaryLabel the (localized) label string
     * @param iconNameArg the name of the icon (as registered with IconManager)
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
                                            final String        iconNameArg,
                                            final CommandAction delCmdAction,
                                            final Object        data,
                                            final int           position)
    {
        NavBoxItemIFace nbi = makeDnDNavBtn(navBox, labelText, iconNameArg, null, null, delCmdAction, true, position, false);
        if (nbi instanceof GhostActionable)
        {
            ((GhostActionable)nbi).setData(data != null ? data : this);
        }
        return nbi;
    }
    
    /**
     * @param navBox
     * @param labelText
     * @param iconNameArg
     * @param cmdAction
     * @param delCmdAction
     * @param flavorClass
     * @param dragFlavor
     * @param dropFlavor
     * @return
     */
    protected NavBoxItemIFace makeDnDNavBtn(final NavBox        navBox,
                                            final String        labelText,
                                            final String        iconNameArg,
                                            final CommandAction cmdAction,
                                            final CommandAction delCmdAction,
                                            final Class<?>      flavorClass,
                                            final String        dragFlavor,
                                            final String        dropFlavor)
    {
        RolloverCommand roc = (RolloverCommand)makeDnDNavBtn(navBox, labelText, null, iconNameArg, cmdAction, delCmdAction, true, -1, false);
        roc.addDragDataFlavor(new DataFlavor(Workbench.class, dragFlavor));
        roc.addDropDataFlavor(new DataFlavor(flavorClass, dropFlavor));
        return (NavBoxItemIFace)roc;
    }
    
    /**
     * @param navBox
     * @param labelText
     * @param iconNameArg
     * @param cmdAction
     * @param delCmdAction
     * @param makeDraggable
     * @param addSorted
     * @return
     */
    protected NavBoxItemIFace makeDnDNavBtn(final NavBox        navBox,
                                            final String        labelText,
                                            final String        iconNameArg,
                                            final CommandAction cmdAction,
                                            final CommandAction delCmdAction,
                                            final boolean       makeDraggable,
                                            final boolean       addSorted)
    {
        return makeDnDNavBtn(navBox, labelText, iconNameArg, null, cmdAction, delCmdAction, makeDraggable, -1, addSorted);
    }
 
    /**
     * @param navBox
     * @param labelText
     * @param iconNameArg
     * @param toolTip
     * @param cmdAction
     * @param delCmdAction
     * @param makeDraggable
     * @param addSorted
     * @return
     */
    protected NavBoxItemIFace makeDnDNavBtn(final NavBox        navBox,
                                            final String        labelText,
                                            final String        iconNameArg,
                                            final String        toolTip,
                                            final CommandAction cmdAction,
                                            final CommandAction delCmdAction,
                                            final boolean       makeDraggable,
                                            final boolean       addSorted)
    {
        return makeDnDNavBtn(navBox, labelText, iconNameArg, toolTip, cmdAction, delCmdAction, makeDraggable, -1, addSorted);
    }
 
    /**
     * @param navBox
     * @param labelText
     * @param iconNameArg
     * @param toolTip
     * @param cmdAction
     * @param delCmdAction
     * @param makeDraggable
     * @param position
     * @param addSorted
     * @return
     */
    protected NavBoxItemIFace makeDnDNavBtn(final NavBox        navBox,
                                            final String        labelText,
                                            final String        iconNameArg,
                                            final String        toolTip,
                                            final CommandAction cmdAction,
                                            final CommandAction delCmdAction,
                                            final boolean       makeDraggable,
                                            final int           position,
                                            final boolean       addSorted)
    {
        NavBoxItemIFace nb = NavBox.createBtn(labelText, iconNameArg, IconManager.STD_ICON_SIZE);
        if (StringUtils.isNotEmpty(toolTip))
        {
            ((RolloverCommand)nb).setToolTip(toolTip);
        }
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
        
        if (addSorted)
        {
            navBox.insertSorted(nb);
            
        } else if (position == -1)
        {
            navBox.add(nb);

        } else
        {
            navBox.insert(nb, false, false, position);
        }

        // Make the Btn Draggable
        if (nb instanceof GhostActionable)
        {
            GhostActionable ga = (GhostActionable)nb;
            if (makeDraggable)
            {
                ga.createMouseInputAdapter(); // this makes it draggable
                ((RolloverCommand)nb).setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else
            {
                //UIRegistry.getGlassPane().add(ga);
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
            UIRegistry.getGlassPane().remove((GhostActionable)roc);
            
            box.remove(nbi);
            
            // XXX this is pathetic and needs to be made generic
            NavBoxMgr.getInstance().validate();
            NavBoxMgr.getInstance().invalidate();
            NavBoxMgr.getInstance().doLayout();
            NavBoxMgr.getInstance().repaint();
            UIRegistry.forceTopFrameRepaint();
        }
    }
    
    /**
     * Removes NavBoxItemIFace from any NavBox.
     * @param btnTitle
     * @return true if a Btn was removed.
     */
    protected boolean deleteDnDBtn(final String btnTitle)
    {
        edu.ku.brc.util.Pair<NavBoxIFace, NavBoxItemIFace> btn = findDnDBtn(btnTitle);
        if (btn != null)
        {
            deleteDnDBtn(btn.getFirst(), btn.getSecond());
            return true;
        }
        return false;
    }
    
    protected edu.ku.brc.util.Pair<NavBoxIFace, NavBoxItemIFace> findDnDBtn(final String btnTitle)
    {
        for (NavBoxIFace navBox : navBoxes)
        {
            for (NavBoxItemIFace nbi : navBox.getItems())
            {
                if (nbi.getTitle().equals(btnTitle))
                {
                    return new edu.ku.brc.util.Pair<NavBoxIFace, NavBoxItemIFace>(navBox, nbi);
                }
            }
        }
        return null;
    }
    /**
     * Helper method to add an item to the navbox.
     * @param navBox navBox
     * @param labelText navBox
     * @param iconNameArg icon name
     * @param cmdAction cmdAction
     * @param delCmdAction delCmdAction
     * @param data data
     * @return btn
     */
    protected NavBoxItemIFace addNavBoxItem(final NavBox navBox,
                                            final String labelText,
                                            final String iconNameArg,
                                            final CommandAction delCmdAction,
                                            final Object data)
    {
        return addNavBoxItem(navBox, labelText,  iconNameArg, delCmdAction, data, -1);
    }
    
    /**
     * Sets up NavBoxItemIFace item as a draggable and adds the action
     * @param nbi the item
     * @param flavors the draggable flavors
     */
    protected void setUpDraggable(final NavBoxItemIFace nbi, final DataFlavor[] flavors)
    {
        setUpDraggable(nbi, flavors, null);
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
        
        if (navBoxAction != null)
        {
            roc.addActionListener(navBoxAction);
        }
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

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getStarterPane()
     */
    public abstract SubPaneIFace getStarterPane();


    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#setStarterPane(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void setStarterPane(SubPaneIFace pane)
    {
        starterPane = pane;
    }

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

                if ((viewSetName == null || viewSetName.equals(fp.getViewSetName())) &&
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
     * @param tabName
     * @param mode
     * @param formData
     * @param paneIcon
     * @return
     */
    protected FormPane createFormPanel(final String           tabName,
                                       final String           mode, 
                                       final FormDataObjIFace formData, 
                                       final ImageIcon        paneIcon)
    {
        return createFormPanel(tabName, mode, formData, formData.getDataClass(), paneIcon);
    }
    
    /**
     * @param tabName
     * @param mode
     * @param formData
     * @param dataClass
     * @param paneIcon
     * @return
     */
    protected FormPane createFormPanel(final String           tabName,
                                       final String           mode, 
                                       final FormDataObjIFace formData, 
                                       final Class<?>         dataClass,
                                       final ImageIcon        paneIcon)
    {
        if (formData != null)
        {
            return createFormPanel(tabName, null, dataClass.getSimpleName(), mode, formData, MultiView.VIEW_SWITCHER, paneIcon);
        }
        return null;
    }
    
    /**
     * @param tabName
     * @param mode
     * @param recordSet
     * @param paneIcon
     * @return
     */
    protected FormPane createFormPanel(final String           tabName,
                                       final String           mode, 
                                       final RecordSetIFace   recordSet, 
                                       final ImageIcon        paneIcon)
    {
        if (recordSet != null)
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
            if (ti != null)
            {
                FormPane fp = createFormPanel(tabName, null, ti.getShortClassName(), mode, null, MultiView.VIEW_SWITCHER, paneIcon);
                fp.setRecordSet(recordSet);
                return fp;
            }
        }
        return null;
    }



    /**
     * Looks to see if a form already exists for this request and shows it
     * otherwise it creates a form and add it to the SubPaneMgr.  This call is for existing data objects.
     * For new objects use the call with "options".
     */
    protected FormPane createFormPanel(final String tabName,
                                       final String viewsetName, 
                                       final String viewName, 
                                       final String mode, 
                                       final Object data, 
                                       final ImageIcon paneIcon)
    {
        return createFormPanel(tabName, viewsetName, viewName, mode, data, MultiView.VIEW_SWITCHER, paneIcon);
    }

    /**
     * Looks to see if a form already exists for this request and shows it
     * otherwise it creates a form and add it to the SubPaneMgr.
     */
    protected FormPane createFormPanel(final String tabName,
                                       final String viewsetName, 
                                       final String viewName, 
                                       final String mode, 
                                       final Object data, 
                                       final int options,
                                       final ImageIcon paneIcon)
    {
        return createFormPanel(tabName, viewsetName, viewName, mode, data, options, paneIcon, null);
    }

    /**
     * Looks to see if a form already exists for this request and shows it
     * otherwise it creates a form and add it to the SubPaneMgr.
     */
    protected FormPane createFormPanel(final String tabName,
                                       final String viewsetName, 
                                       final String viewName, 
                                       final String mode, 
                                       final Object data, 
                                       final int options,
                                       final ImageIcon paneIcon,
                                       final FormPane.FormPaneAdjusterIFace adjuster)
    {
        ImageIcon iconImg = paneIcon == null ? getIcon(StdIcon16) : paneIcon;
        FormPane fp = null;

        if (recentFormPane != null && recentFormPane.getComponentCount() == 0)
        {
            recentFormPane.createForm(viewsetName, viewName, null, null, options, adjuster); // not new data object
            recentFormPane.getMultiView().setData(data);
            
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
                recentFormPane = new FormPane(tabName, this, viewsetName, viewName, mode, null, options, adjuster); // not new data object
                recentFormPane.setPaneName(tabName);
                recentFormPane.setIcon(iconImg);
                
                if (data instanceof RecordSetIFace)
                {
                    recentFormPane.setRecordSet((RecordSetIFace)data);
                } else
                {
                    recentFormPane.getMultiView().setData(data);
                }
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
     * @see edu.ku.brc.af.core.Taskable#preInitialize()
     */
    @Override
    public void preInitialize()
    {
        // this is a the stub so all tasks do not have to implement it
        // or call it.
    }

    /**
     * 
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
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();
        
        return navBoxes;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getIcon(int)
     */
    @Override
    public ImageIcon getIcon(int size)
    {
        switch (size)
        {
            case 32 : return IconManager.getIcon(iconName, IconManager.IconSize.Std32);
            case 24 : return IconManager.getIcon(iconName, IconManager.IconSize.Std24);
            case 20 : return IconManager.getIcon(iconName, IconManager.IconSize.Std20);
            case 16 : return IconManager.getIcon(iconName, IconManager.IconSize.Std16);
        }
        return IconManager.getIcon(iconName);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#requestContext()
     */
    @Override
    public void requestContext()
    {
        if (canRequestContext())
        {
            ContextMgr.requestContext(this);

            SubPaneIFace newPane = getStarterPane();
        
            SubPaneMgr.getInstance().addPane(newPane);
        }
    }

    /**
     * @return true if it is OK to get a context for this task.
     * 
     * Should be part of Taskable interface??
     */
    protected boolean canRequestContext()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#initialize(java.util.List)
     */
    @Override
    public void initialize(List<TaskCommandDef> cmds, final boolean isVisibleArg)
    {
        this.commands  = cmds;
        this.isVisible = isVisibleArg;
        initialize(); // initializes the Taskable
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isVisible()
     */
    @Override
    public boolean isVisible()
    {
        return isVisible;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isShowDefault()
     */
    @Override
    public boolean isShowDefault()
    {
        return AppPreferences.getRemote().getBoolean("task.isShowDefault."+name, isShowDefault); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isStarterPane()
     */
    @Override
    public boolean isStarterPane()
    {
        return starterPane != null;
    }
    
    /**
     * Used for processing the Application Commands. 
     * This method also unregisters all services for the task.
     * @param cmdAction the command to be processed
     */
    protected void doProcessAppCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction(APP_RESTART_ACT))
        {
            ContextMgr.removeServicesByTask(this);
        }
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.CommandListener#doCommand(edu.ku.brc.af.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdActionArg)
    {
        CommandAction cmdAction = cmdActionArg;
        if (cmdAction.getData() instanceof CommandActionForDB)
        {
            cmdAction = (CommandAction)cmdAction.getData();
        }
        
        // Why is this here n the BaseTask?
        if (cmdAction.isAction(OPEN_FORM_CMD_ACT))
        {
            FormDataObjIFace formData = null;
            if (cmdAction instanceof CommandActionForDB)
            {
                formData = (FormDataObjIFace)((CommandActionForDB)cmdAction).getDataObj();
                
            } else if (cmdAction.getData() instanceof FormDataObjIFace)
            {
                formData = (FormDataObjIFace)cmdAction.getData();
            }
            
            if (formData != null)
            {
                if (formData instanceof RecordSetIFace)
                {
                    RecordSetIFace rs = (RecordSetIFace)formData;
                    createFormPanel(formData.getIdentityTitle(), "edit", formData, rs.getDataClassFormItems(), getIcon(StdIcon16)); //$NON-NLS-1$
                } else
                {
                    createFormPanel(formData.getIdentityTitle(), "edit", formData, getIcon(StdIcon16)); //$NON-NLS-1$
                }
                cmdActionArg.setConsumed(true);
                
            } else
            {
                // Error Dialog - The Data Object was not found.
            }
        } else if (cmdAction.isType(APP_CMD_TYPE))
        {
            doProcessAppCommands(cmdActionArg);
        }
    }

    //--------------------------------------------------------------
    // SubPaneMgrListener Interface
    //--------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneMgrListener#subPaneAdded(edu.ku.brc.af.ui.SubPaneIFace)
     */
    @Override
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
    @Override
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
    @Override
    public void subPaneShown(SubPaneIFace subPane)
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isSingletonPane()
     */
    @Override
    public boolean isSingletonPane()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isConfigurable()
     */
    @Override
    public boolean isConfigurable()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#doConfigure()
     */
    @Override
    public void doConfigure()
    {
        //Documented Empty Block
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getPopupMenu()
     */
    @Override
    public JPopupMenu getPopupMenu()
    {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mi = new JMenuItem(getResourceString("BaseTask.CONFIGURE")); //$NON-NLS-1$
        popupMenu.add(mi);
        
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doConfigure();
            }
        });
        
        return popupMenu;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        if (toolbarItems != null)
        {
            for (ToolBarItemDesc tbi : toolbarItems)
            {
               tbi.getComp().setEnabled(enabled); 
            }
        }
        
        if (menuItems != null)
        {
            for (MenuItemDesc mi : menuItems)
            {
                if (mi.getMenuItem() instanceof JMenuItem)
                {
                    ((JMenuItem)mi.getMenuItem()).setEnabled(enabled);
                }
            }
        }
    }
    
    //--------------------------------------------------------------
    // NavBoxButton Helpers
    //--------------------------------------------------------------


    /**
     * Gets Command Items for a mime type in the AppResources XML and adds them to commands list.
     * @param mimeType the MimeType to use
     * @param classTableId the Table Id
     */
    protected void addAppResourcesToCommandsByMimeType(final String mimeType, 
                                                       final String reportType,
                                                       final String defaultIcon,
                                                       final Integer classTableId)
    {
            List<TaskCommandDef> cmds = getAppResourceCommandsByMimeType(mimeType, reportType, defaultIcon, classTableId);
            if (cmds.size() > 0)
            {
                if (commands == null)
                {
                    throw new RuntimeException("this should never have happened."); //$NON-NLS-1$
                }
                commands.addAll(cmds);
            }
         
//        for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType(mimeType))
//        {
//            Properties params = ap.getMetaDataMap();
//            
//            String tableid = params.getProperty("tableid");
//            String rptType = params.getProperty("reporttype");
//            
//            if (StringUtils.isNotEmpty(tableid) && 
//               (classTableId == null || (Integer.parseInt(tableid) == classTableId.intValue())) &&
//               StringUtils.isEmpty(reportType) || (StringUtils.isNotEmpty(rptType) && reportType.equals(rptType)))
//            {
//                params.put("name", ap.getName());
//                params.put("title", ap.getDescription());
//                params.put("file", ap.getName());
//                params.put("mimetype", mimeType);
//                
//                //log.debug("["+ap.getDescription()+"]["+ap.getName()+"]");
//                
//                String iconName = params.getProperty("icon");
//                if (StringUtils.isEmpty(iconName))
//                {
//                    iconName = name;
//                }                        
//                commands.add(new TaskCommandDef(ap.getDescription(), iconName, params));
//            }
//        }
    }

    /**
     * Creates Command Items for a mime type in the AppResources XML.
     * @param mimeType the MimeType to use
     * @param classTableId the Table Id
     */
    public static List<TaskCommandDef> getAppResourceCommandsByMimeType(final String mimeType, 
                                                                        final String reportType,
                                                                        final String defaultIcon,
                                                                        final Integer classTableId)
    {
        List<TaskCommandDef> result = new LinkedList<TaskCommandDef>();
        for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType(mimeType))
        {
            Properties params = ap.getMetaDataMap();
            
            String tableid = params.getProperty("tableid"); //$NON-NLS-1$
            String rptType = params.getProperty("reporttype"); //$NON-NLS-1$
            
            if (StringUtils.isNotEmpty(tableid) && 
               (classTableId == null || (Integer.parseInt(tableid) == classTableId.intValue())) &&
               StringUtils.isEmpty(reportType) || (StringUtils.isNotEmpty(rptType) && reportType.equals(rptType)))
            {
                params.put("name", ap.getName()); //$NON-NLS-1$
                params.put("title", ap.getDescription()); //$NON-NLS-1$
                params.put("file", ap.getName()); //$NON-NLS-1$
                params.put("mimetype", mimeType); //$NON-NLS-1$
                
                //log.debug("["+ap.getDescription()+"]["+ap.getName()+"]");
                
                String iconNameStr = params.getProperty("icon"); //$NON-NLS-1$
                if (StringUtils.isEmpty(iconNameStr))
                {
                    iconNameStr = defaultIcon;
                }                        
                result.add(new TaskCommandDef(ap.getDescription(), iconNameStr, params));
            }
        }
        return result;
    }

    /**
      * Helper method for registering a NavBoxItem as a GhostMouseDropAdapter
      * @param navBox the parent box for the nbi to be added to
      * @param navBoxItemDropZone the nbi in question
      * @return returns the new NavBoxItem
      */
     protected NavBoxItemIFace addToNavBoxAndRegisterAsDroppable(final DataFlavor          dataFlavor,
                                                                 final NavBox              navBox,
                                                                 final NavBoxItemIFace     nbi,
                                                                 final Properties          params)
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
    
     /**
      * Asks where the source of the COs should come from.
      * @return the source enum
      */
     public ASK_TYPE askSourceOfColObj()
     {
         Object[] options = { 
                 getResourceString("NEW_BT_USE_RS"), 
                 getResourceString("NEW_BT_ENTER_CATNUM") 
               };
         int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                      getResourceString("NEW_BT_CHOOSE_RSOPT"), 
                                                      getResourceString("NEW_BT_CHOOSE_RSOPT_TITLE"), 
                                                      JOptionPane.YES_NO_CANCEL_OPTION,
                                                      JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
         if (userChoice == JOptionPane.NO_OPTION)
         {
             return ASK_TYPE.EnterCats;
             
         } else if (userChoice == JOptionPane.YES_OPTION)
         {
             return ASK_TYPE.ChooseRS;
         }
         return ASK_TYPE.Cancel;
     }

     
     /**
      * @return a RecordSet of newly entered Catalog Numbers
      */
     public RecordSetIFace askForCatNumbersRecordSet()
     {
         AskForNumbersDlg dlg = new AskForNumbersDlg("BT_COLOBJ_TITLE", "BT_LABEL", CollectionObject.class, "catalogNumber");
         dlg.setVisible(true);
         if (!dlg.isCancelled())
         {
             return dlg.getRecordSet();
         }
         return null;
     }
     
     /**
     * @param recordSetArg
     * @param numColObjRS
     * @return
     */
    public RecordSetIFace getRecordSetOfColObj(final RecordSetIFace recordSetArg,
                                                   final int numColObjRS)
     {
         RecordSetIFace recordSetFromDB = recordSetArg;
         if (recordSetFromDB == null)
         {
             if (numColObjRS > 0)
             {
                 ASK_TYPE rv = askSourceOfColObj();
                 if (rv == ASK_TYPE.ChooseRS)
                 {
                     recordSetFromDB = recordSetArg == null ? RecordSetTask.askForRecordSet(CollectionObject.getClassTableId()) : recordSetArg;
                 
                 } else if (rv == ASK_TYPE.EnterCats)
                 {
                     recordSetFromDB = askForCatNumbersRecordSet();
                 }
             } else
             {
                 recordSetFromDB = askForCatNumbersRecordSet();
             }
         }
         return recordSetFromDB;
     }
    
    //--------------------------------------------------------------
    // SecurityOptionIFace
    //--------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getPermissions()
     */
    public PermissionIFace getPermissions()
    {
        if (permissions == null)
        {
            permissions = SecurityMgr.getInstance().getPermission(securityPrefix + "." + getPermissionName());
        }
        return permissions;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getShortDesc()
     */
    @Override
    public String getShortDesc()
    {
        return this.shortDesc == null ? title : this.shortDesc;
    }

    /**
     * @param shortDesc the shortDesc to set
     */
    public void setShortDesc(String shortDesc)
    {
        this.shortDesc = shortDesc;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getPermissionName()
     */
    public String getPermissionName()
    {
        return name;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getPermissionTitle()
     */
    @Override
    public String getPermissionTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#setPermissions(edu.ku.brc.af.core.PermissionIFace)
     */
    public void setPermissions(final PermissionIFace permissions)
    {
        this.permissions = permissions;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getAdditionalSecurityOptions()
     */
    @Override
    public List<SecurityOptionIFace> getAdditionalSecurityOptions()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getPermEditorPanel()
     */
    public PermissionEditorIFace getPermEditorPanel()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getDefaultPermissions(java.lang.String)
     */
    @Override
    public PermissionIFace getDefaultPermissions(String userType)
    {
        return null;
    }
    
}
