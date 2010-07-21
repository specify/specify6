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
/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.SecurityOption;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.af.ui.db.CommandActionForDB;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.SpVisualQuery;
import edu.ku.brc.specify.tasks.subpane.VisualQueryPanel;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.Trash;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 12, 2010
 *
 */
public class VisualQueryTask extends BaseTask
{
    private static final Logger log  = Logger.getLogger(VisualQueryTask.class);
    
    private static final String  VISUALQUERY      = "VisualQueryTask";
    private static final String  VISQRY_MENU      = "VISQRY_MENU";
    private static final String  VISQRY_MNU       = "VISQRY_MNU";
    private static final String  VISQRY_TITLE     = "VISQRY_TITLE";
    private static final String  VISQRY_SECURITY  = "VISQRYEDIT";
    
    // Data Members
    protected NavBox                  actionNavBox       = null;
    protected NavBox                  visQueryNavBox  = null;
    protected NavBoxItemIFace         openNavBtn         = null; 

    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    protected ToolBarDropDownBtn      toolBarBtn       = null;

    
    /**
     * 
     */
    public VisualQueryTask()
    {
        super(VISUALQUERY, UIRegistry.getResourceString(VISQRY_TITLE));
        this.iconName = "VisualQuery";
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    @Override
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            extendedNavBoxes.clear();
            
            navBoxes.add(actionNavBox);
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                visQueryNavBox = new NavBox(title);
                
                List<?> visSearches = session.getDataList(SpVisualQuery.class);
                for (Iterator<?> iter=visSearches.iterator();iter.hasNext();)
                {
                    addVisQuery((SpVisualQuery)iter.next());
                    
                }      
                navBoxes.add(visQueryNavBox);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InfoRequestTask.class, ex);
                log.error(ex);
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
        isShowDefault = true;
    }

    /**
     * Add an SpVisualQuery Item to the box.
     * @param spVisQuery the infoRequest to be added
     */
    protected void addVisQuery(final SpVisualQuery spVisQuery)
    {           
        NavBoxItemIFace nbi = makeDnDNavBtn(visQueryNavBox, 
                                            spVisQuery.getIdentityTitle(), 
                                            this.iconName, 
                                            null, //ToolTip
                                            new CommandActionForDB("Data_Entry", "Edit", SpVisualQuery.getClassTableId(), spVisQuery.getId()),
                                            new CommandAction(VISUALQUERY, DELETE_CMD_ACT, spVisQuery),
                                            true,
                                            true);
        setUpDraggable(nbi, new DataFlavor[]{Trash.TRASH_FLAVOR, InteractionsTask.LOAN_FLAVOR}, new NavBoxAction("Data_Entry", "Edit"));
        visQueryNavBox.validate();
        visQueryNavBox.repaint();
        NavBox.refresh(visQueryNavBox);
        //setUpDraggable(nbi, new DataFlavor[]{InteractionsTask.LOAN_FLAVOR}, null);//new NavBoxAction(InteractionsTask.INTERACTIONS, InteractionsTask.NEW_LOAN));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#preInitialize()
     */
    @Override
    public void preInitialize()
    {
        CommandDispatcher.register(RecordSetTask.RECORD_SET, this);
        CommandDispatcher.register(PreferencesDlg.PREFERENCES, this);
        CommandDispatcher.register(VISUALQUERY, this);

        //RecordSetTask.addDroppableDataFlavor(defaultFlavor);
        
        // Create and add the Actions NavBox first so it is at the top at the top
        actionNavBox = new NavBox(getResourceString("Actions"));
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return starterPane = new VisualQueryPanel(name, this);
    }
    
    /**
     * 
     */
    protected void prefsChanged(final CommandAction cmdAction)
    {
        AppPreferences appPrefs = (AppPreferences)cmdAction.getData();
        
        if (appPrefs == AppPreferences.getRemote())
        {
            // Note: The event send with the name of pref from the form
            // not the name that was saved. So we don't need to append the discipline name on the end
            Object value = cmdAction.getProperties().get("Exporttask.OnTaskbar");
            if (value != null && value instanceof Boolean)
            {
                /*
                 * This doesn't work because it isn't added to the Toolbar correctly
                 * */
                JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
                
                Boolean isChecked = (Boolean)value;
                if (isChecked)
                {
                    TaskMgr.addToolbarBtn(toolBarBtn, toolBar.getComponentCount()-1);
                } else
                {
                    TaskMgr.removeToolbarBtn(toolBarBtn);
                }
                toolBar.validate();
                toolBar.repaint();
                 
            }
        }
    }

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);
        List<NavBoxIFace> nbs = rsTask.getNavBoxes();
        if (nbs != null)
        {
            extendedNavBoxes.addAll(nbs);
        }
        return extendedNavBoxes;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        String label    = getResourceString(VISQRY_MENU);
        String hint     = getResourceString(VISQRY_MENU);
        toolBarBtn      = createToolbarButton(label, iconName, hint);
        
        toolbarItems = new Vector<ToolBarItemDesc>();
        String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
        if (AppPreferences.getRemote().getBoolean("ExportTask.OnTaskbar"+"."+ds, false))
        {
            toolbarItems.add(new ToolBarItemDesc(toolBarBtn));
        }
        return toolbarItems;
    }
    
    //-------------------------------------------------------
    // SecurityOption Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getAdditionalSecurityOptions()
     */
    @Override
    public List<SecurityOptionIFace> getAdditionalSecurityOptions()
    {
        List<SecurityOptionIFace> list = new ArrayList<SecurityOptionIFace>();
        
        list.add(new SecurityOption(VISQRY_SECURITY, 
                getResourceString("VISQRY_TITLE"), 
                securityPrefix,
                new BasicPermisionPanel(VISQRY_TITLE, 
                                        "VISQRY_VIEW", 
                                        "VISQRY_EDIT")));

        return list;
    }
    
    //-------------------------------------------------------
    // BaseTask
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(RecordSetTask.RECORD_SET) && cmdAction.isAction("Clicked"))
        {
            //processRecordSetCommand(cmdAction);
        }
        
        if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
        } 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        final String DATA_MENU = "Specify.DATA_MENU";
        
        SecurityMgr secMgr = SecurityMgr.getInstance();
        
        menuItems = new Vector<MenuItemDesc>();
        
        MenuItemDesc mid;
        JMenuItem mi;
        String    menuDesc = getResourceString(VISQRY_MENU);

        if (!AppContextMgr.isSecurityOn() || 
            (secMgr.getPermission(VISQRY_SECURITY) != null && 
             !secMgr.getPermission(VISQRY_SECURITY).hasNoPerm()))
        {
            mi       = UIHelper.createLocalizedMenuItem(VISQRY_MENU, VISQRY_MNU, VISQRY_TITLE, true, null); 
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            VisualQueryTask.this.requestContext();
                        }
                    });
                }
            });
            mid = new MenuItemDesc(mi, DATA_MENU);
            mid.setPosition(MenuItemDesc.Position.After, menuDesc);
            menuItems.add(mid);
        }
        
        return menuItems;
    }


}
