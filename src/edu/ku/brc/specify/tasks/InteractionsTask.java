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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.af.ui.db.CommandActionForDB;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.TableViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.dbsupport.TableModel2Excel;
import edu.ku.brc.helpers.EMailHelper;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.ExchangeIn;
import edu.ku.brc.specify.datamodel.ExchangeOut;
import edu.ku.brc.specify.datamodel.Gift;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.specify.ui.LoanReturnDlg;
import edu.ku.brc.specify.ui.LoanSelectPrepsDlg;
import edu.ku.brc.specify.ui.LoanReturnDlg.LoanReturnInfo;
import edu.ku.brc.specify.ui.db.AskForNumbersDlg;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.Trash;


/**
 * This task manages Loans, Gifts, Exchanges and provide actions and forms to do the interactions.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class InteractionsTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(InteractionsTask.class);

    protected static final String resourceName = "InteractionsTaskInit";
    
    public static final String             INTERACTIONS        = "Interactions";
    public static final DataFlavorTableExt INTERACTIONS_FLAVOR = new DataFlavorTableExt(DataEntryTask.class, INTERACTIONS);
    public static final DataFlavorTableExt INFOREQUEST_FLAVOR  = new DataFlavorTableExt(InfoRequest.class, "InfoRequest", new int[] {50});
    public static final DataFlavorTableExt LOAN_FLAVOR         = new DataFlavorTableExt(Loan.class, "Loan", new int[] {52});
    public static final DataFlavorTableExt GIFT_FLAVOR         = new DataFlavorTableExt(Loan.class, "Gift");
    public static final DataFlavorTableExt EXCHGIN_FLAVOR      = new DataFlavorTableExt(ExchangeIn.class, "ExchangeIn");
    public static final DataFlavorTableExt EXCHGOUT_FLAVOR     = new DataFlavorTableExt(ExchangeOut.class, "ExchangeOut");
    
    public static final  String   IS_USING_INTERACTIONS_PREFNAME = "Interactions.Using.Interactions.";

    enum ASK_TYPE { Cancel, EnterCats, ChooseRS}
    
    protected static final String InfoRequestName      = "InfoRequest";
    protected static final String NEW_LOAN             = "NEW_LOAN";
    protected static final String NEW_ACCESSION        = "NEW_ACCESSION";
    protected static final String NEW_PERMIT           = "NEW_PERMIT";
    protected static final String NEW_GIFT             = "NEW_GIFT";
    protected static final String NEW_EXCHANGE_IN      = "NEW_EXCHANGE_IN";
    protected static final String NEW_EXCHANGE_OUT     = "NEW_EXCHANGE_OUT";
    protected static final String PRINT_LOAN           = "PRINT_LOAN";
    protected static final String INFO_REQ_MESSAGE     = "Specify Info Request";
    protected static final String CREATE_MAILMSG       = "CreateMailMsg";
    protected static final String ADD_TO_LOAN          = "AddToLoan";
    
    
    protected static final int    loanTableId;
    protected static final int    infoRequestTableId;
    protected static final int    colObjTableId;

    // Data Members
    protected NavBox                  infoRequestNavBox;
    //protected NavBox                  loansNavBox;
    //protected NavBox                  giftsNavBox;
    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    protected Vector<NavBoxItemIFace> invoiceList      = new Vector<NavBoxItemIFace>();
    protected NavBoxItemIFace         exchgNavBtn      = null; 
    protected NavBoxItemIFace         giftsNavBtn      = null; 
    protected NavBox                  actionsNavBox;
    
    protected boolean                 isUsingInteractions = true;
    protected ToolBarDropDownBtn      toolBarBtn       = null;
    protected int                     indexOfTBB       = -1;
    
    protected Vector<InteractionEntry>  entries = new Vector<InteractionEntry>();
    
    static 
    {
        loanTableId        = DBTableIdMgr.getInstance().getIdByClassName(Loan.class.getName());
        infoRequestTableId = DBTableIdMgr.getInstance().getIdByClassName(InfoRequest.class.getName());
        colObjTableId      = DBTableIdMgr.getInstance().getIdByClassName(CollectionObject.class.getName());
        
        INFOREQUEST_FLAVOR.addTableId(50);
        
    }

   /**
     * Default Constructor
     *
     */
    public InteractionsTask()
    {
        super(INTERACTIONS, getResourceString("Interactions"));
        
        CommandDispatcher.register(INTERACTIONS, this);
        CommandDispatcher.register(RecordSetTask.RECORD_SET, this);
        CommandDispatcher.register(DB_CMD_TYPE, this);
        CommandDispatcher.register(DataEntryTask.DATA_ENTRY, this);
        CommandDispatcher.register(PreferencesDlg.PREFERENCES, this);
        
        if (false)
        {
            // temporary
            String I = INTERACTIONS;
            String F = OPEN_FORM_CMD_ACT;
            String D = DataEntryTask.DATA_ENTRY;
            String OPEN_NEW_VIEW = DataEntryTask.OPEN_NEW_VIEW;
            
            String RECORD_SET = "Record_Set";
            
            InteractionEntry entry;
            //                                 name          table       lblKey    View         Type    Action      Icon         isOn
            entry = new InteractionEntry("accession",   "accession",   null,  "Accession",   D,   OPEN_NEW_VIEW,    "Accession");
            entry.addDroppable(RecordSetTask.class, RECORD_SET, new int[] {Accession.getClassTableId()});
            entries.add(entry);
            
            entry = new InteractionEntry("permit",      "permit",      null,  "Permit",      D,   OPEN_NEW_VIEW,    "Permit");
            entry.addDroppable(RecordSetTask.class, RECORD_SET, new int[] {Permit.getClassTableId()});
            entries.add(entry);
            
            entry = new InteractionEntry("loan",        "loan",        null,  "Loan",        I,   NEW_LOAN,         "Loan");
            entry.addDroppable(RecordSetTask.class, RECORD_SET, new int[] {Loan.getClassTableId(), CollectionObject.getClassTableId(), InfoRequest.getClassTableId()});
            entries.add(entry);
            
            entry = new InteractionEntry("gift",        "gift",        null,  "Gift",        F,   NEW_GIFT,         "Gift");
            entry.addDroppable(RecordSetTask.class, RECORD_SET, new int[] {Gift.getClassTableId()});
            entries.add(entry);
            
            entry = new InteractionEntry("exchangein",  "exchangein",  null,  "ExchangeIn",  F,   NEW_EXCHANGE_IN,  "ExchangeIn");
            entry.addDroppable(RecordSetTask.class, RECORD_SET, new int[] {ExchangeIn.getClassTableId()});
            entries.add(entry);
            
            entry = new InteractionEntry("exchangeout", "exchangeout", null,  "ExchangeOut", F,   NEW_EXCHANGE_OUT, "ExchangeOut");
            entry.addDroppable(RecordSetTask.class, RECORD_SET, new int[] {ExchangeOut.getClassTableId()});
            entries.add(entry);
            
            entry = new InteractionEntry("inforequest", "inforequest", null,  null,          I,   INFO_REQ_MESSAGE, "InfoRequest");
            entry.addDroppable(RecordSetTask.class, RECORD_SET, new int[] {ExchangeIn.getClassTableId(), ExchangeOut.getClassTableId(), CollectionObject.getClassTableId(), InfoRequest.getClassTableId()});
            entry.addDroppable(InfoRequest.class, "InfoRequest", new int[] {InfoRequest.getClassTableId()});
            entries.add(entry);
            
            entry = new InteractionEntry("printloan",   "loan",        "PRINT_LOANINVOICE",  null, I,   PRINT_LOAN, "Reports");
            entry.addDroppable(RecordSetTask.class, RECORD_SET, new int[] {Loan.getClassTableId()});
            entries.add(entry);
            
            try
            {
                XStream xstream = new XStream();
                InteractionEntry.config(xstream);
                EntryFlavor.config(xstream);
                
                FileUtils.writeStringToFile(new File("interactions.xml"), xstream.toXML(entries));
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        } else
        {
            readEntries();
        }
    }
    
    /**
     * Reads the entries set up information from the database.
     */
    @SuppressWarnings("unchecked")
    private void readEntries()
    {
        try
        {
            XStream xstream = new XStream();
            
            InteractionEntry.config(xstream);
            EntryFlavor.config(xstream);
            
            String           xmlStr = null;
            AppResourceIFace appRes = AppContextMgr.getInstance().getResourceFromDir(SpecifyAppContextMgr.PERSONALDIR, resourceName);
            if (appRes != null)
            {
                xmlStr = appRes.getDataAsString();
                
            } else
            {
                // Get the default resource by name and copy it to a new User Area Resource
                AppResourceIFace newAppRes = AppContextMgr.getInstance().copyToDirAppRes(SpecifyAppContextMgr.PERSONALDIR, resourceName);
                if (newAppRes != null)
                {
                    // Save it in the User Area
                    //AppContextMgr.getInstance().saveResource(newAppRes);
                    xmlStr = newAppRes.getDataAsString();
                } else
                {
                    return;
                }
            }
            
            //log.debug(xmlStr);
            entries = (Vector<InteractionEntry>)xstream.fromXML(xmlStr); // Describes the definitions of the full text search);
            
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * Writes the entries set up information to the database.
     */
    private void writeEntries()
    {
        XStream xstream = new XStream();
        InteractionEntry.config(xstream);
        EntryFlavor.config(xstream);
        
        AppResourceIFace appRes = AppContextMgr.getInstance().getResourceFromDir(SpecifyAppContextMgr.PERSONALDIR, resourceName);
        if (appRes != null)
        {
            appRes.setDataAsString(xstream.toXML(entries));
            AppContextMgr.getInstance().saveResource(appRes);
            
        } else
        {
            AppContextMgr.getInstance().putResourceAsXML(resourceName, xstream.toXML(entries));     
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#isConfigurable()
     */
    @Override
    public boolean isConfigurable()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doConfigure()
     */
    @Override
    public void doConfigure()
    {
        
        boolean isEmpty = infoRequestNavBox.getItems().size() == 0;
        
        Vector<TaskConfigItemIFace> stdList  = new Vector<TaskConfigItemIFace>();
        Vector<TaskConfigItemIFace> miscList = new Vector<TaskConfigItemIFace>();
        
        for (InteractionEntry entry : entries)
        {
            Vector<TaskConfigItemIFace> list = entry.isOnLeft() ? stdList : miscList;
            // Clone for undo (Cancel)
            try
            {
                list.add((TaskConfigItemIFace)entry.clone());
            } catch (CloneNotSupportedException ex) {}
        }
        
        TaskConfigureDlg dlg = new TaskConfigureDlg(stdList, miscList, false,
                "InteractionsConfig",
                "IAT_TITLE",
                "IAT_AVAIL_ITEMS",
                "IAT_HIDDEN_ITEMS",
                "IAT_MAKE_AVAIL",
                "IAT_MAKE_HIDDEN");
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            entries.clear();
            for (TaskConfigItemIFace ie : stdList)
            {
                ((InteractionEntry)ie).setOnLeft(true);
                entries.add((InteractionEntry)ie);
            }
            
            for (TaskConfigItemIFace ie : miscList)
            {
                ((InteractionEntry)ie).setOnLeft(false);
                entries.add((InteractionEntry)ie);
            }
            
            writeEntries();
            
            actionsNavBox.clear();
            
            Collections.sort(entries);
            
            for (InteractionEntry entry : entries)
            {
                if (entry.isOnLeft())
                {
                    DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(entry.getTableName());
                    addCommand(actionsNavBox, tableInfo, entry);
                }
            }
            
            NavBoxMgr.getInstance().validate();
            NavBoxMgr.getInstance().invalidate();
            NavBoxMgr.getInstance().doLayout();
            
            if (stdList.size() == 0)
            {
                String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getName();
                AppPreferences.getRemote().putBoolean(IS_USING_INTERACTIONS_PREFNAME+ds, false);
                JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
                indexOfTBB = toolBar.getComponentIndex(toolBarBtn);
                if (indexOfTBB > -1)
                {
                    TaskMgr.removeToolbarBtn(toolBarBtn);
                    toolBar.validate();
                    toolBar.repaint();
                }
                
            } else if (isEmpty && stdList.size() > 0)
            {
                String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getName();
                AppPreferences.getRemote().putBoolean(IS_USING_INTERACTIONS_PREFNAME+ds, true);
                prefsChanged(new CommandAction(null, null, AppPreferences.getRemote()));
            }
        }
    }

    /**
     * Retrieves the prefs that we cache.
     */
    protected void setUpCachedPrefs()
    {
        AppPreferences remotePrefs = AppPreferences.getRemote();
        String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getName();
        isUsingInteractions = remotePrefs.getBoolean(IS_USING_INTERACTIONS_PREFNAME+ds, true);
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
            
            setUpCachedPrefs();
            
            extendedNavBoxes.clear();
            invoiceList.clear();

            actionsNavBox = new NavBox(getResourceString("Actions"));
            
            for (InteractionEntry entry : entries)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(entry.getTableName());
                
                String label;
                if (StringUtils.isNotEmpty(entry.getLabelKey()))
                {
                    label = getResourceString(entry.getLabelKey());
                } else
                {
                    label = tableInfo.getTitle();
                }
                
                entry.setTitle(label);
                
                if (entry.isOnLeft())
                {
                    addCommand(actionsNavBox, tableInfo, entry);
                }
            }
            navBoxes.add(actionsNavBox);
            
            infoRequestNavBox  = new NavBox(getResourceString("InfoRequest"));
            loadNavBox(infoRequestNavBox, InfoRequest.class, INFOREQUEST_FLAVOR);
        }
        isShowDefault = true;
    }
    
    /**
     * @return
     */
    protected List<AppResourceIFace> getInvoiceAppResources()
    {
        Vector<AppResourceIFace> ars = new Vector<AppResourceIFace>();
        for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType("jrxml/report"))
        {
            Properties params = ap.getMetaDataMap();
            if (params.getProperty("reporttype", "").equals("Invoice"))
            {
                ars.add(ap);
            }
        }
        return ars;
    }
    
    /**
     * @param navBox
     * @param action
     * @param cmdName
     * @param iconNameStr
     * @param tableIds
     * @return
     */
    protected NavBoxButton addCommand(final NavBox navBox, 
                                      final String action, 
                                      final String cmdName, 
                                      final String iconNameStr,
                                      final int[]  tableIds)
    {
        CommandAction cmdAction = new CommandAction(INTERACTIONS, action);
        NavBoxButton roc = (NavBoxButton)makeDnDNavBtn(navBox, getResourceString(cmdName), iconNameStr, cmdAction, null, true, false);// true means make it draggable
        DataFlavorTableExt dfx = new DataFlavorTableExt(RecordSetTask.RECORDSET_FLAVOR.getDefaultRepresentationClass(), 
                                                        RecordSetTask.RECORDSET_FLAVOR.getHumanPresentableName(), tableIds);
        roc.addDropDataFlavor(dfx);
        return roc;
    }
            
    /**
     * @param navBox
     * @param tableInfo
     * @param label
     * @param cmdType
     * @param action
     * @param viewName
     * @param iconName
     * @param tableIds
     * @return
     */
    protected NavBoxButton addCommand(final NavBox navBox, 
                                      final DBTableInfo tableInfo,
                                      final InteractionEntry entry)
    {
        CommandAction cmdAction = new CommandAction(entry.getCmdType(), entry.getAction(), tableInfo.getTableId());
        if (StringUtils.isNotEmpty(entry.getViewName()))
        {
            cmdAction.setProperty("view", entry.getViewName());
            cmdAction.setProperty(NavBoxAction.ORGINATING_TASK, this);
        }
        
        NavBoxButton roc = (NavBoxButton)makeDnDNavBtn(navBox, entry.getTitle(), entry.getIconName(), cmdAction, null, true, false);// true means make it draggable
        
        for (EntryFlavor ef : entry.getDraggableFlavors())
        {
            DataFlavorTableExt dfx = new DataFlavorTableExt(ef.getDataFlavorClass(), ef.getHumanReadable(), ef.getTableIdsAsArray());
            roc.addDragDataFlavor(dfx);
        }
        
        for (EntryFlavor ef : entry.getDroppableFlavors())
        {
            DataFlavorTableExt dfx = new DataFlavorTableExt(ef.getDataFlavorClass(), ef.getHumanReadable(), ef.getTableIdsAsArray());
            roc.addDropDataFlavor(dfx);
        }
        return roc;
    }
            
    /**
     * Helper function to load all the items for a class of data objects (i.e. Loans, Gifts)
     * @param navBox the parent
     * @param dataClass the class to be loaded
     * @param dragFlav the drag flavor of the item
     */
    protected void loadNavBox(final NavBox     navBox, 
                              final Class<?>   dataClass,
                              final DataFlavor dragFlav)
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(dataClass.getName());
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            String sql = "FROM " + ti.getClassName() + " WHERE collectionMemberId = COLMEMID";
            List<?> list = session.getDataList(QueryAdjusterForDomain.getInstance().adjustSQL(sql));
            for (Iterator<?> iter=list.iterator();iter.hasNext();)
            {
                FormDataObjIFace dataObj = (FormDataObjIFace)iter.next();
                
                CommandActionForDB delCmd = new CommandActionForDB(INTERACTIONS, DELETE_CMD_ACT, ti.getTableId(), dataObj.getId());
                NavBoxItemIFace    nbi    = makeDnDNavBtn(navBox, dataObj.getIdentityTitle(), ti.getShortClassName(), null, delCmd, true, true);
                RolloverCommand    roc = (NavBoxButton)nbi;
                nbi.setData(new CommandActionForDB(INTERACTIONS, "InfoRequest", ti.getTableId(), dataObj.getId()));
                roc.addDragDataFlavor(dragFlav);
                roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
            }      
            navBoxes.add(navBox);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    /**
     * Helper method for registering a NavBoxItem as a GhostMouseDropAdapter
     * @param navBox the parent box for the nbi to be added to
     * @param navBoxItemDropZone the nbi in question
     * @return returns the new NavBoxItem
     */
    protected NavBoxItemIFace addToNavBoxAndRegisterAsDroppable(final NavBox              navBox,
                                                                final NavBoxItemIFace     nbi,
                                                                final Properties params)
    {
        NavBoxButton roc = (NavBoxButton)nbi;
        roc.setData(params);

        // When Being Dragged
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        roc.addDragDataFlavor(INTERACTIONS_FLAVOR);

        // When something is dropped on it
        roc.addDropDataFlavor(RecordSetTask.RECORDSET_FLAVOR);

        navBox.add(nbi);
        //labelsList.add(nbi);
        return nbi;
    }
    
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        if (starterPane != null)
        {
            return starterPane;
        }
        
        //DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
        //DroppableFormRecordSetAccepter spDroppable = dataEntryTask.createDroppableFormRecordSetAccepter(title, this, getResourceString("INTERACTIONS_OVERVIEW"));
        //spDroppable.addDropFlavor(INFOREQUEST_FLAVOR);
        //return starterPane = spDroppable;
        return starterPane = new SimpleDescPane(title, this, getResourceString("INTERACTIONS_OVERVIEW"));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        toolbarItems       = new Vector<ToolBarItemDesc>();
        String label       = getResourceString(name);
        String iconNameStr = name;
        String hint = getResourceString("interactions_hint");
        toolBarBtn = createToolbarButton(label, iconNameStr, hint);

        AppPreferences remotePrefs = AppPreferences.getRemote();
        if (remotePrefs == AppPreferences.getRemote())
        {
            String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getName();
            isUsingInteractions = remotePrefs.getBoolean(IS_USING_INTERACTIONS_PREFNAME+ds, true);
        }

        if (isUsingInteractions)
        {
            toolbarItems.add(new ToolBarItemDesc(toolBarBtn));
        }

        return toolbarItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    /**
     * Creates a new loan from a RecordSet.
     * @param fileNameArg the filename of the report (Invoice) to use (can be null)
     * @param recordSets the recordset to use to create the loan
     */
    protected void printLoan(final String fileNameArg, final Object data)
    {
        if (data instanceof RecordSetIFace)
        {
            RecordSetIFace rs = (RecordSetIFace)data;
            
            if (rs.getDbTableId() != Loan.getClassTableId())
            {
                return;
            }
            
            String fileName = fileNameArg;
            if (fileName == null)
            {
                
                List<AppResourceIFace> invoiceReports = getInvoiceAppResources();
                if (invoiceReports.size() == 0)
                {
                    // XXX Need Error Dialog that there are no Invoices (can this happen?)
                    
                } else if (invoiceList.size() > 1) // only Count the ones that require data
                {
                    fileName = askForInvoiceName();
                    
                } else  
                {
                    AppResourceIFace invoiceAppRes = invoiceReports.get(0);
                    fileName = invoiceAppRes.getName();
                }
            }

            if (fileName != null)
            {
                // XXX For Demo purposes only we need to be able to look up report and labels
                final CommandAction cmd = new CommandAction(ReportsBaseTask.REPORTS, ReportsBaseTask.PRINT_REPORT, rs);
                cmd.setProperty("file", "LoanInvoice.jrxml");
                cmd.setProperty("title", "Loan Invoice");
                cmd.setProperty(NavBoxAction.ORGINATING_TASK, this);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        CommandDispatcher.dispatch(cmd);
                    }
                });
            } else
            {
                // XXX need error message about not having an invoice
            }
        }
    }
    
    /**
     * @param list
     */
    protected void showMissingDetsDlg(final List<CollectionObject> noCurrDetList)
    {
        StringBuilder sb = new StringBuilder();
        for (CollectionObject co : noCurrDetList)
        {
            if (sb.length() > 0) sb.append(", ");
            sb.append(co.getIdentityTitle());
        }
        
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("p:g", "p,2px,p"));
        CellConstraints cc     = new CellConstraints();
        JTextArea       ta     = UIHelper.createTextArea(5, 40);
        JScrollPane     scroll = new JScrollPane(ta, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JLabel          lbl    = UIHelper.createLabel(getResourceString("InteractionsTask.MISSING_DET"));
        
        pb.add(lbl,    cc.xy(1, 1));
        pb.add(scroll, cc.xy(1, 3));
        
        ta.setText(sb.toString());
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        
        pb.setDefaultDialogBorder();
        
        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), 
                getResourceString("InteractionsTask.MISSING_DET_TITLE"), 
                true, 
                CustomDialog.OK_BTN, 
                pb.getPanel());
        //dlg.setOkLabel(getResourceString(key))
        dlg.setVisible(true);
    }
    
    /**
     * Creates a new loan from a InfoRequest.
     * @param infoRequest the infoRequest to use to create the loan
     */
    protected void createLoan(final InfoRequest infoRequest)
    {   
        RecordSetIFace rs = null;
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.attach(infoRequest);
            rs = infoRequest.getRecordSets().iterator().next();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            // Error Dialog
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        if (rs != null)
        {
            createOrAddToLoan(null, infoRequest, rs);
        }
    }
    
    /**
     * @return
     */
    protected RecordSetIFace askForCatNumbersRecordSet()
    {
        AskForNumbersDlg dlg = new AskForNumbersDlg("AFN_COLOBJ_TITLE", "AFN_LABEL", CollectionObject.class, "catalogNumber");
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            return dlg.getRecordSet();
        }
        return null;
    }
    
    /**
     * Asks where the source of the Loan Preps should come from.
     * @return the source enum
     */
    protected ASK_TYPE askSourceOfLoanPreps(final boolean hasInfoReqs, final boolean hasColObjRS)
    {
        String label;
        if (hasInfoReqs && hasColObjRS)
        {
            label = getResourceString("NEW_LOAN_USE_RS_IR");
            
        } else if (hasInfoReqs)
        {
            label = getResourceString("NEW_LOAN_USE_IR");
        } else
        {
            label = getResourceString("NEW_LOAN_USE_RS");
        }
        
        Object[] options = { 
                label, 
                getResourceString("NEW_LOAN_ENTER_CATNUM") 
              };
        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                     getResourceString("NEW_LOAN_CHOOSE_RSOPT"), 
                                                     getResourceString("NEW_LOAN_CHOOSE_RSOPT_TITLE"), 
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
     * Asks where the source of the COs should come from.
     * @return the source enum
     */
    protected ASK_TYPE askSourceOfInfoReqColObj()
    {
        Object[] options = { 
                getResourceString("NEW_IR_USE_RS"), 
                getResourceString("NEW_IR_ENTER_CATNUM") 
              };
        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                     getResourceString("NEW_IR_CHOOSE_RSOPT"), 
                                                     getResourceString("NEW_IR_CHOOSE_RSOPT_TITLE"), 
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
     * @return returns a list of RecordSets of InfoRequests
     */
    protected Vector<RecordSetIFace> getInfoReqRecordSetsFromSideBar()
    {
        Vector<RecordSetIFace> rsList = new Vector<RecordSetIFace>();
        for (NavBoxItemIFace nbi : infoRequestNavBox.getItems())
        {
            Object obj = nbi.getData();
            if (obj instanceof CommandActionForDB)
            {
                CommandActionForDB cmdAction = (CommandActionForDB)obj;
                RecordSetIFace     rs       = RecordSetFactory.getInstance().createRecordSet(nbi.getTitle(), cmdAction.getTableId(), RecordSet.GLOBAL);
                rs.addItem(cmdAction.getId());
                rsList.add(rs);
            }
        }
        return rsList;
    }
    
    /**
     * Creates a new loan from a RecordSet.
     * @param currentLoan an existing loan that needs additional Preps
     * @param infoRequest a info request
     * @param recordSetArg the recordset to use to create the loan
     */
    @SuppressWarnings("unchecked")
    protected void createOrAddToLoan(final Loan           currentLoan, 
                                     final InfoRequest    infoRequest, 
                                     final RecordSetIFace recordSetArg)
    {
        RecordSetIFace recordSet = recordSetArg;
        if (infoRequest == null && recordSet == null)
        {
            // Get a List of InfoRequest RecordSets
            Vector<RecordSetIFace> rsList       = getInfoReqRecordSetsFromSideBar();
            RecordSetTask          rsTask       = (RecordSetTask)TaskMgr.getTask(RecordSetTask.RECORD_SET);
            List<RecordSetIFace>   colObjRSList = rsTask.getRecordSets(CollectionObject.getClassTableId());
            
            // If the List is empty then
            if (rsList.size() == 0 && colObjRSList.size() == 0)
            {
                recordSet = askForCatNumbersRecordSet();
                
            } else 
            {
                ASK_TYPE rv = askSourceOfLoanPreps(rsList.size() > 0, colObjRSList.size() > 0);
                if (rv == ASK_TYPE.ChooseRS)
                {
                    recordSet = RecordSetTask.askForRecordSet(CollectionObject.getClassTableId(), rsList);
                    
                } else if (rv == ASK_TYPE.EnterCats)
                {
                    recordSet = askForCatNumbersRecordSet();
                    
                } else if (rv == ASK_TYPE.Cancel)
                {
                    return;
                }
            }
        }
        
        if (recordSet == null)
        {
            return;
        }
        
        DBTableIdMgr.getInstance().getInClause(recordSet);

        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
        
        DataProviderFactory.getInstance().evict(tableInfo.getClassObj()); // XXX Not sure if this is really needed
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            // OK, it COULD be a RecordSet contain one or more InfoRequest, 
            // we will only accept an RS with one InfoRequest
            if (infoRequest == null && recordSet.getDbTableId() == infoRequestTableId)
            {
                if (recordSet.getNumItems() == 1)
                {
                    RecordSetItemIFace item = recordSet.getOnlyItem();
                    if (item != null)
                    {
                        InfoRequest infoReq = session.get(InfoRequest.class, item.getRecordId().intValue());
                        if (infoReq != null)
                        {
                            createOrAddToLoan(null, infoReq, infoReq.getRecordSets().iterator().next());
                            
                        } else
                        {
                            // error about missing info request
                            // Error Dialog
                        }
                    } else
                    {
                        // error about item being null for some unbelievable reason 
                     // Error Dialog
                    }
                } else 
                {
                    // error about item having more than one or none
                    // Error Dialog
                }
                return;
            }
            
            // OK, here we have a recordset of CollectionObjects
            // First we process all the CollectionObjects in the RecordSet
            // and create a list of Preparations that can be loaned
            String sqlStr = DBTableIdMgr.getInstance().getQueryForTable(recordSet);
            if (StringUtils.isNotBlank(sqlStr))
            {
                final JStatusBar statusBar = UIRegistry.getStatusBar();
                statusBar.setIndeterminate("LoanLoader", true);
                
                UIRegistry.writeGlassPaneMsg(getResourceString("InteractionsTask.LOAD_LOAN_PREP"), 24);
                LoanPrepLoader loader = new LoanPrepLoader(this, session, sqlStr, currentLoan, infoRequest);
                loader.addPropertyChangeListener(
                        new PropertyChangeListener() {
                            public  void propertyChange(PropertyChangeEvent evt) {
                                if ("progress".equals(evt.getPropertyName())) 
                                {
                                    statusBar.setValue("LoanLoader", (Integer)evt.getNewValue());
                                }
                            }
                        });
                loader.execute();
                
            } else
            {
                log.error("Query String empty for RecordSet tableId["+recordSet.getDbTableId()+"]");
            }
            
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
            
        }
    }
   
    /**
     * @param availColObjList
     * @param loan
     * @param infoRequest
     * @param session
     */
    protected void loanPrepsLoaded(final ArrayList<CollectionObject> availColObjList,
                                   final Loan                        loan,
                                   final InfoRequest                 infoRequest,
                                   final DataProviderSessionIFace    session)
    {
        try
        {
            final LoanSelectPrepsDlg loanSelectPrepsDlg = new LoanSelectPrepsDlg(availColObjList, loan);
            loanSelectPrepsDlg.createUI();
            
            if (!loanSelectPrepsDlg.hasAvaliblePrepsToLoan())
            {
                UIRegistry.showLocalizedMsg("InteractionsTask.NO_PREPS_TITLE", "InteractionsTask.NO_PREPS");
                return;
            }
            loanSelectPrepsDlg.setModal(true);
            
            UIHelper.centerAndShow(loanSelectPrepsDlg);
            
            if (loanSelectPrepsDlg.isCancelled())
            {
                return;
            }

            final Hashtable<Preparation, Integer> prepsHash = loanSelectPrepsDlg.getPreparationCounts();
            if (prepsHash.size() > 0)
            {
                final SwingWorker worker = new SwingWorker()
                {
                    @Override
                    public Object construct()
                    {
                        JStatusBar statusBar = UIRegistry.getStatusBar();
                        statusBar.setIndeterminate(INTERACTIONS, true);
                        
                        statusBar.setText(getResourceString("CreatingLoan"));
                        
                        InteractionsTask.this.addPrepsToLoan(loan, infoRequest, prepsHash);
                        
                        return null;
                    }

                    //Runs on the event-dispatching thread.
                    @Override
                    public void finished()
                    {
                        JStatusBar statusBar = UIRegistry.getStatusBar();
                        statusBar.setProgressDone(INTERACTIONS);
                        statusBar.setText("");
                    }
                };
                worker.start();
            }
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    /**
     * @param existingLoan
     * @param infoRequest
     * @param prepsHash
     */
    protected void addPrepsToLoan(final Loan        existingLoan, 
                                  final InfoRequest infoRequest,
                                  final Hashtable<Preparation, Integer> prepsHash)
    {
        Loan loan;
        
        if (existingLoan == null)
        {
            loan = new Loan();
            loan.initialize();
            
            Calendar dueDate = Calendar.getInstance();
            dueDate.add(Calendar.MONTH, 6);                 // XXX PREF Due Date
            loan.setCurrentDueDate(dueDate);
            
            Shipment shipment = new Shipment();
            shipment.initialize();
            
            // Get Defaults for Certain fields
            //SpecifyAppContextMgr appContextMgr     = (SpecifyAppContextMgr)AppContextMgr.getInstance();
            
            // Comment out defaults for now until we can manage them
            //PickListItemIFace    defShipmentMethod = appContextMgr.getDefaultPickListItem("ShipmentMethod", getResourceString("SHIPMENT_METHOD"));
            //if (defShipmentMethod != null)
            //{
            //     shipment.setShipmentMethod(defShipmentMethod.getValue());
            //}
            
            //FormDataObjIFace shippedBy = appContextMgr.getDefaultObject(Agent.class, "ShippedBy", getResourceString("SHIPPED_BY"), true, false);
            //if (shippedBy != null)
            //{
            //    shipment.setShippedBy((Agent)shippedBy);
            //}
            
            if (infoRequest != null && infoRequest.getAgent() != null)
            {
                shipment.setShippedTo(infoRequest.getAgent());
            }
            
            loan.addReference(shipment, "shipments");
        } else
        {
            loan = existingLoan;
        }
        
        Hashtable<Integer, LoanPreparation> prepToLoanPrepHash = null;
        if (existingLoan != null)
        {
            prepToLoanPrepHash = new Hashtable<Integer, LoanPreparation>();
            for (LoanPreparation lp : existingLoan.getLoanPreparations())
            {
                prepToLoanPrepHash.put(lp.getPreparation().getId(), lp);
            }
        }
        
        for (Preparation prep : prepsHash.keySet())
        {
            Integer count = prepsHash.get(prep);
            if (prepToLoanPrepHash != null)
            {
                LoanPreparation lp = prepToLoanPrepHash.get(prep.getId());
                if (lp != null)
                {
                    int lpCnt = lp.getQuantity();
                    lpCnt += count;
                    lp.setQuantity(lpCnt);
                    //System.err.println("Adding "+count+"  to "+lp.hashCode());
                    continue;
                }
            }
            
            LoanPreparation lpo = new LoanPreparation();
            lpo.initialize();
            lpo.setPreparation(prep);
            lpo.setQuantity(count);
            lpo.setLoan(loan);
            loan.getLoanPreparations().add(lpo);
        }
        
        if (existingLoan == null)
        {
            DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
            if (dataEntryTask != null)
            {
                DBTableInfo loanTableInfo = DBTableIdMgr.getInstance().getInfoById(loan.getTableId());
                dataEntryTask.openView(this, null, loanTableInfo.getDefaultFormName(), "edit", loan, true);
            }
        } else 
        {
            CommandDispatcher.dispatch(new CommandAction(INTERACTIONS, "REFRESH_LOAN_PREPS", null));
        }
    }
    
    /**
     * Displays UI that asks the user to select a predefined label.
     * @return the name of the label file or null if canceled
     */
    protected String askForInvoiceName()
    {
        List<AppResourceIFace> invoiceReports = getInvoiceAppResources();
        
        if (invoiceReports.size() == 1)
        {
            return (String)invoiceReports.get(0).getMetaDataMap().get("file");

        }
        
        ChooseFromListDlg<AppResourceIFace> dlg = new ChooseFromListDlg<AppResourceIFace>((Frame)UIRegistry.getTopWindow(),
                                                                                        getResourceString("ChooseInvoice"), 
                                                                                        invoiceReports, 
                                                                                        IconManager.getIcon(name, IconManager.IconSize.Std24));
        dlg.setMultiSelect(false);
        dlg.setModal(true);
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            return  (String)dlg.getSelectedObject().getMetaDataMap().get("file");
        }
        return null;
    }
    
    /**
     * Creates a new InfoRequest from a RecordSet.
     * @param recordSet the recordSet to use to create the InfoRequest
     */
    protected void createInfoRequest(final RecordSetIFace recordSetArg)
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(InfoRequest.class.getSimpleName());
        
        ViewIFace view = AppContextMgr.getInstance().getView(tableInfo.getDefaultFormName());

        InfoRequest infoRequest = new InfoRequest();
        infoRequest.initialize();
        
        RecordSetTask          rsTask       = (RecordSetTask)TaskMgr.getTask(RecordSetTask.RECORD_SET);
        List<RecordSetIFace>   colObjRSList = rsTask.getRecordSets(CollectionObject.getClassTableId());

        RecordSetIFace recordSetFromDB = recordSetArg;
        if (recordSetFromDB == null)
        {
            if (colObjRSList.size() > 0)
            {
                ASK_TYPE rv = askSourceOfInfoReqColObj();
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
        
        if (recordSetFromDB != null)
        {
            RecordSet recordSet = RecordSetTask.copyRecordSet(recordSetFromDB);
            if (recordSet == null)
            {
                UIRegistry.showLocalizedError("ERROR_COPYING_RS");
                return;
            }
            recordSet.setName(recordSet.getName() + "_IT");
            
            infoRequest.addReference(recordSet, "recordSets");
            
            if (recordSet.getOrderedItems() != null && recordSet.getNumItems() > 0)
            {
                createFormPanel(name, view.getViewSetName(), view.getName(), "edit", infoRequest, MultiView.IS_NEW_OBJECT, null);
                
            } else
            {
                UIRegistry.displayErrorDlgLocalized("ERROR_MISSING_RS_OR_NOITEMS");  
            }
        }
    }

    
    /**
     * @param cmdAction
     */
    protected void checkToPrintLoan(final CommandAction cmdAction)
    {
        if (cmdAction.getData() instanceof Loan)
        {
            Loan loan = (Loan)cmdAction.getData();
            
            Boolean     printLoan   = null;
            FormViewObj formViewObj = getCurrentFormViewObj();
            if (formViewObj != null)
            {
                Component comp = formViewObj.getControlByName("generateInvoice");
                if (comp instanceof JCheckBox)
                {
                    printLoan = ((JCheckBox)comp).isSelected();
                }
            }
            
            if (printLoan == null)
            {
                Object[] options = {getResourceString("CreateLoanInvoice"), getResourceString("CANCEL")};
                int n = JOptionPane.showOptionDialog(UIRegistry.get(UIRegistry.FRAME),
                                                    String.format(getResourceString("CreateLoanInvoiceForNum"), new Object[] {(loan.getLoanNumber())}),
                                                    getResourceString("CreateLoanInvoice"),
                                                    JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE,
                                                    null,     //don't use a custom Icon
                                                    options,  //the titles of buttons
                                                    options[0]); //default button title
                printLoan = n == 0;
            }
            
            // XXX DEBUG
            //printLoan = false;
            if (printLoan)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    //session.attach(loan);
                    loan = (Loan)session.getData("From Loan where loanId = "+loan.getLoanId());
                    
                    if (loan.getShipments().size() == 0)
                    {
                        UIRegistry.displayErrorDlg(getResourceString("NO_SHIPMENTS_ERROR"));
                        
                    } else if (loan.getShipments().size() > 1)
                    {
                        // XXX Do we allow them to pick a shipment or print all?
                        UIRegistry.displayErrorDlg(getResourceString("MULTI_SHIPMENTS_NOT_SUPPORTED"));
                        
                    } else
                    {
                        // XXX At the moment this is just checking to see if there is at least one "good/valid" shipment
                        // but the hard part will be sending the correct info so the report can be printed
                        // using bouth a Loan Id and a Shipment ID, and at some point distinguishing between using
                        // the shipped by versus the shipper.
                        Shipment shipment = loan.getShipments().iterator().next();
                        if (shipment.getShippedBy() == null)
                        {
                            UIRegistry.displayErrorDlg(getResourceString("SHIPMENT_MISSING_SHIPPEDBY"));
                        } else if (shipment.getShippedBy().getAddresses().size() == 0)
                        {
                            UIRegistry.displayErrorDlg(getResourceString("SHIPPEDBY_MISSING_ADDR"));
                        } else if (shipment.getShippedTo() == null)
                        {
                            UIRegistry.displayErrorDlg(getResourceString("SHIPMENT_MISSING_SHIPPEDTO"));
                        } else if (shipment.getShippedTo().getAddresses().size() == 0)
                        {
                            UIRegistry.displayErrorDlg(getResourceString("SHIPPEDTO_MISSING_ADDR"));
                        } else
                        {
                            //session.close();
                            //session = null;
                            
                            RecordSet rs = new RecordSet();
                            rs.initialize();
                            rs.setName(loan.getIdentityTitle());
                            rs.setDbTableId(loan.getTableId());
                            rs.addItem(loan.getId());
                            printLoan(null, rs);
                        }
                    }
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            }
        }
    }
    
    /**
     * Creates an Excel SpreadSheet or CVS file and attaches it to an email and send it to an agent.
     */
    public void createAndSendEMail()
    {
        FormViewObj formViewObj = getCurrentFormViewObj();
        if (formViewObj != null && formViewObj.getDataObj() instanceof InfoRequest) 
        {
            InfoRequest infoRequest = (InfoRequest)formViewObj.getDataObj();
            Agent       toAgent     = infoRequest.getAgent();
            
            boolean   sendEMail = true; // default to true
            Component comp      = formViewObj.getControlByName("sendEMail");
            if (comp instanceof JCheckBox)
            {
                sendEMail = ((JCheckBox)comp).isSelected();
            }
            
            MultiView mv = formViewObj.getSubView("InfoRequestColObj");
            if (mv != null && sendEMail)
            {
                final Viewable viewable = mv.getCurrentView();
                if (viewable instanceof TableViewObj)
                {
                    final Hashtable<String, String> emailPrefs = new Hashtable<String, String>();
                    if (!EMailHelper.isEMailPrefsOK(emailPrefs))
                    {
                        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                                getResourceString("NO_EMAIL_PREF_INFO"), 
                                getResourceString("NO_EMAIL_PREF_INFO_TITLE"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    final File tempExcelFileName = TableModel2Excel.getTempExcelName();
                    
                    emailPrefs.put("to", toAgent.getEmail() != null ? toAgent.getEmail() : "");
                    emailPrefs.put("from", emailPrefs.get("email"));
                    emailPrefs.put("subject", String.format(getResourceString("INFO_REQUEST_SUBJECT"), new Object[] {infoRequest.getIdentityTitle()}));
                    emailPrefs.put("bodytext", "");
                    emailPrefs.put("attachedFileName", tempExcelFileName.getName());
                    
                    final Frame topFrame = (Frame)UIRegistry.getTopWindow();
                    final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog(topFrame,
                                                  "SystemSetup",
                                                  "SendMail",
                                                  null,
                                                  getResourceString("SEND_MAIL_TITLE"),
                                                  getResourceString("SEND_BTN"),
                                                  null, // className,
                                                  null, // idFieldName,
                                                  true, // isEdit,
                                                  0);
                    dlg.setData(emailPrefs);
                    dlg.setModal(true);
                    dlg.setVisible(true);
                    
                    if (dlg.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
                    {
                        dlg.getMultiView().getDataFromUI();
                        
                        //System.out.println("["+emailPrefs.get("bodytext")+"]");
                        
                        TableViewObj  tblViewObj = (TableViewObj)viewable;
                        File          excelFile  = TableModel2Excel.convertToExcel(tempExcelFileName, 
                                                                                   getResourceString("CollectionObject"), 
                                                                                   tblViewObj.getTable().getModel());
                        StringBuilder sb         = TableModel2Excel.convertToHTML(getResourceString("CollectionObject"), 
                                                                                  tblViewObj.getTable().getModel());
                        
                        //EMailHelper.setDebugging(true);
                        String text = emailPrefs.get("bodytext").replace("\n", "<br>") + "<BR><BR>" + sb.toString();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run()
                            {
                                UIRegistry.displayLocalizedStatusBarText("SENDING_EMAIL");
                            }
                        });
                        
                        String password = Encryption.decrypt(emailPrefs.get("password"));
                        if (StringUtils.isEmpty(password))
                        {
                            password = EMailHelper.askForPassword(topFrame);
                        }
                        
                        if (StringUtils.isNotEmpty(password))
                        {
                            final boolean status = EMailHelper.sendMsg(emailPrefs.get("servername"), 
                                                                       emailPrefs.get("username"), 
                                                                       password, 
                                                                       emailPrefs.get("email"), 
                                                                       emailPrefs.get("to"), 
                                                                       emailPrefs.get("subject"), text, EMailHelper.HTML_TEXT, excelFile);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run()
                                {
                                    UIRegistry.displayLocalizedStatusBarText(status ? "EMAIL_SENT_ERROR" : "EMAIL_SENT_OK");
                                }
                            });
                        }
                    }
                }
            }
        } else
        {
            log.error("Why doesn't the current SubPane have a main FormViewObj?");
        }
    }
    
    /**
     * Delete a InfoRequest..
     * @param infoRequest the infoRequest to be deleted
     */
    protected InfoRequest deleteInfoRequest(final int infoReqId)
    {
        InfoRequest infoRequest = null;
        // delete from database
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            infoRequest = session.get(InfoRequest.class, infoReqId);
            if (infoRequest != null)
            {
                session.beginTransaction();
                session.delete(infoRequest);
                session.commit();
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return infoRequest;
    }
    
    /**
     * Delete the InfoRequest from the UI, which really means remove the NavBoxItemIFace. 
     * This method first checks to see if the boxItem is not null and uses that, if
     * it is null then it looks the box up by name ans used that
     * @param boxItem the box item to be deleted
     * @param infoRequest the infoRequest that is "owned" by some UI object that needs to be deleted (used for secodary lookup
     */
    protected void deleteInfoRequestFromUI(final NavBoxItemIFace boxItem, final InfoRequest infoRequest)
    {
        deleteDnDBtn(infoRequestNavBox, boxItem != null ? boxItem : getBoxByTitle(infoRequest.getIdentityTitle()));
    }
    
    /**
     * Starts process to return a loan.
     * @param multiView the current form doing the return
     * @param loan the loan being returned
     * @param agent the agent doing the return
     * @param returns the list of items being returned
     */
    protected void doReturnLoan(final MultiView            multiView,
                                final Loan                 loan,
                                final Agent                agent, 
                                final List<LoanReturnInfo> returns)
    {
        final SwingWorker worker = new SwingWorker()
        {
            public Object construct()
            {
                JStatusBar statusBar = UIRegistry.getStatusBar();
                statusBar.setIndeterminate(INTERACTIONS, true);
                statusBar.setText(getResourceString("ReturningLoanItems"));
                
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    
                    session.beginTransaction();
                    
                    for (LoanReturnInfo loanRetInfo : returns)
                    {   
                        LoanPreparation loanPrep  = loanRetInfo.getLoanPreparation();
                        session.attach(loanPrep);
                        
                        LoanReturnPreparation loanRetPrep = new LoanReturnPreparation();
                        loanRetPrep.initialize();
                        loanRetPrep.setReceivedBy(agent);
                        loanRetPrep.setModifiedByAgent(Agent.getUserAgent());
                        loanRetPrep.setReturnedDate(Calendar.getInstance());
                        loanRetPrep.setQuantity(loanRetInfo.getQuantity());
                        loanRetPrep.setRemarks(loanRetInfo.getRemarks());
                        
                        int quantity         = loanPrep.getQuantity();
                        int quantityReturned = loanPrep.getQuantityReturned();
                        int quantityResolved = loanPrep.getQuantityResolved();
                        
                        if (loanRetInfo.isResolved() != null)
                        {
                            loanPrep.setIsResolved(loanRetInfo.isResolved());
                            loanPrep.setQuantityReturned(quantityReturned + loanRetInfo.getQuantity());
                        }
                        loanPrep.addReference(loanRetPrep, "loanReturnPreparations");
                        
                        quantityReturned += loanRetInfo.getQuantity();
                        
                        if ((quantityResolved + quantityReturned) == quantity)
                        {
                            loanPrep.setIsResolved(true);
                        }
                        
                        session.save(loanRetPrep);
                        session.saveOrUpdate(loanPrep);
                        session.saveOrUpdate(loan);
                    }
                    
                    session.commit();
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    // Error Dialog
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
                return null;
            }

            //Runs on the event-dispatching thread.
            public void finished()
            {
                JStatusBar statusBar = UIRegistry.getStatusBar();
                statusBar.setProgressDone(INTERACTIONS);
                statusBar.setText("");
                multiView.setData(loan);
            }
        };
        worker.start();
    }
    
    /**
     * Starts process to return a loan
     * @param doPartial true means show dialog and do partial, false means just return the loan
     */
    protected void returnLoan()
    {
        Loan         loan    = null;
        MultiView    mv      = null;
        SubPaneIFace subPane = SubPaneMgr.getInstance().getCurrentSubPane();
        if (subPane != null)
        {
            mv = subPane.getMultiView();
            if (mv != null)
            {
                if (mv.getData() instanceof Loan)
                {
                    loan = (Loan)mv.getData();
                }
            }
        }
        
        if (mv != null && loan != null)
        {
            LoanReturnDlg dlg = new LoanReturnDlg(loan);
            if (dlg.createUI())
            {
                dlg.setModal(true);
                UIHelper.centerAndShow(dlg);
                dlg.dispose();
                
                if (!dlg.isCancelled())
                {
                    FormViewObj fvp = mv.getCurrentViewAsFormViewObj();
                    fvp.setHasNewData(true);
                    //fvp.getValidator().validateForm();
                    fvp.getValidator().setHasChanged(true);
                    fvp.validationWasOK(fvp.getValidator().getState() == UIValidatable.ErrorType.Valid);
                   
                    List<LoanReturnInfo> returns = dlg.getLoanReturnInfo();
                    if (returns.size() > 0)
                    {
                        doReturnLoan(mv, loan, dlg.getAgent(), returns);
                    }
                }
            }
            
        } else
        {
            // XXX Show some kind of error dialog
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        super.subPaneRemoved(subPane);
        
        if (subPane == starterPane)
        {
            starterPane = null;
        }
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------
    /**
     * 
     */
    protected void prefsChanged(final CommandAction cmdAction)
    {
        AppPreferences remotePrefs = (AppPreferences)cmdAction.getData();
        
        if (remotePrefs == AppPreferences.getRemote())
        {
            String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getName();
            isUsingInteractions = remotePrefs.getBoolean(IS_USING_INTERACTIONS_PREFNAME+ds, true);
            
            JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
            if (!isUsingInteractions)
            {
                indexOfTBB = toolBar.getComponentIndex(toolBarBtn);
                TaskMgr.removeToolbarBtn(toolBarBtn);
                toolBar.validate();
                toolBar.repaint();
                
            } else
            {
                int curInx = toolBar.getComponentIndex(toolBarBtn);
                if (curInx == -1)
                {
                    int inx = indexOfTBB != -1 ? indexOfTBB : 4;
                    TaskMgr.addToolbarBtn(toolBarBtn, inx);
                    toolBar.validate();
                    toolBar.repaint();
                }
            }
        }
    }
    
    /**
     * Processes all Commands of type RECORD_SET.
     * @param cmdAction the command to be processed
     */
    protected void processRecordSetCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction("Clicked"))
        {
            Object srcObj = cmdAction.getSrcObj();
            Object dstObj = cmdAction.getDstObj();
            Object data   = cmdAction.getData();
            
            log.debug("********* In Labels doCommand src["+srcObj+"] dst["+dstObj+"] data["+data+"] context["+ContextMgr.getCurrentContext()+"]");
             
            if (ContextMgr.getCurrentContext() == this)
            {
                if (dstObj instanceof RecordSetIFace)
                {
                    RecordSetIFace rs = (RecordSetIFace)dstObj;
                    if (rs.getDbTableId() == Loan.getClassTableId())
                    {
                        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(rs.getDbTableId());
                        
                        super.createFormPanel(ti.getTitle(), "view", rs, IconManager.getIcon(ti.getShortClassName(), IconManager.IconSize.Std16));
                    }
                }
            }
        }
    }
    
    /**
     * Processes all Commands of type DB_CMD_TYPE.
     * @param cmdAction the command to be processed
     */
    protected void processDatabaseCommands(final CommandAction cmdAction)
    {
        if (cmdAction.getData() instanceof InfoRequest)
        {
            if (cmdAction.isAction(INSERT_CMD_ACT) || cmdAction.isAction(UPDATE_CMD_ACT))
            {
                // Create Specify Application
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        CommandDispatcher.dispatch(new CommandAction(INTERACTIONS, CREATE_MAILMSG, SubPaneMgr.getInstance().getCurrentSubPane()));
                    }
                });
                
                if (cmdAction.isAction(INSERT_CMD_ACT))
                {
                    InfoRequest infoRequest = (InfoRequest)cmdAction.getData();
                    NavBoxItemIFace nbi = addNavBoxItem(infoRequestNavBox, infoRequest.getIdentityTitle(), "InfoRequest", new CommandAction(INTERACTIONS, DELETE_CMD_ACT, infoRequest), infoRequest);
                    setUpDraggable(nbi, new DataFlavor[] {Trash.TRASH_FLAVOR, INFOREQUEST_FLAVOR}, new NavBoxAction(INTERACTIONS, INFO_REQ_MESSAGE));
                }
            }
        }
    }
    
    /**
     * Loads a InfoRequest into a form
     * @param cmdAction the command action containing the InfoRequest
     */
    private void showInfoReqForm(final CommandActionForDB cmdAction)
    {
        // Launch Info Req Form
        DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
        if (dataEntryTask != null)
        {
            InfoRequest              infoReq   = null;
            DBTableInfo              tableInfo = DBTableIdMgr.getInstance().getInfoById(InfoRequest.getClassTableId());
            DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
            try
            {
                infoReq = session.get(InfoRequest.class, cmdAction.getId());
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                log.error(ex);
            } finally
            {
                session.close();
            }
            
            if (infoReq != null)
            {
                dataEntryTask.openView(this, null, tableInfo.getDefaultFormName(), "edit", infoReq, true);
            }
        }
    }
    
    /**
     * Asks if they want to delete and then deletes it
     * @param cmdActionDB the delete command
     */
    private void deleteInfoRequest(final CommandActionForDB cmdActionDB)
    {
        NavBoxButton nb = (NavBoxButton)cmdActionDB.getSrcObj();
        int option = JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), 
                String.format(UIRegistry.getResourceString("InteractionsTask.CONFIRM_DELETE_IR"), nb.getName()),
                UIRegistry.getResourceString("InteractionsTask.CONFIRM_DELETE_TITLE_IR"), 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION); // I18N
        
        if (option == JOptionPane.YES_OPTION)
        {
            InfoRequest infoRequest = deleteInfoRequest(cmdActionDB.getId());
            deleteInfoRequestFromUI(null, infoRequest);
        }
    }
    
    /**
     * Processes all Commands of type INTERACTIONS.
     * @param cmdAction the command to be processed
     */
    protected void processInteractionsCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction(CREATE_MAILMSG))
        {
            createAndSendEMail();
            
        } else if (cmdAction.isAction(PRINT_LOAN))
        {
            if (cmdAction.getData() instanceof RecordSetIFace)
            {
                if (((RecordSetIFace)cmdAction.getData()).getDbTableId() != cmdAction.getTableId())
                {
                    JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                                                  getResourceString("ERROR_RECORDSET_TABLEID"), 
                                                  getResourceString("Error"), 
                                                  JOptionPane.ERROR_MESSAGE);
                }

                printLoan(null, cmdAction.getData());
                
            } if (cmdAction.getData() instanceof CommandAction)
            {
                String tableIDStr = cmdAction.getPropertyAsString("tableid");
                if (StringUtils.isNotEmpty(tableIDStr) && StringUtils.isNumeric(tableIDStr))
                {
                    RecordSetIFace recordSet = RecordSetTask.askForRecordSet(Integer.parseInt(tableIDStr));
                    if (recordSet != null)
                    {
                        printLoan(cmdAction.getPropertyAsString("file"), recordSet);
                    }
                }
            }
            
        } else if (cmdAction.isAction(NEW_LOAN))
        {
            if (cmdAction.getData() == cmdAction)
            {
                // We get here when a user clicks on a Loan NB action 
                createOrAddToLoan(null, null, null);
                
            } else if (cmdAction.getData() instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)cmdAction.getData();
                if (rs.getDbTableId() == colObjTableId || rs.getDbTableId() == infoRequestTableId)
                {
                    if (cmdAction.isAction(NEW_LOAN))
                    {    
                        createOrAddToLoan(null, null, rs);
                    }
                } else
                {
                    log.error("Dropped wrong table type."); // this shouldn't happen
                }
                
            } else if (cmdAction.getData() instanceof InfoRequest)
            {
                createLoan((InfoRequest)cmdAction.getData());
                
            }  else if (cmdAction.getData() instanceof CommandActionForDB)
            {
                CommandActionForDB cmdActionDB = (CommandActionForDB)cmdAction.getData();
                RecordSetIFace     rs          = RecordSetFactory.getInstance().createRecordSet("", cmdActionDB.getTableId(), RecordSet.GLOBAL);
                rs.addItem(cmdActionDB.getId());
                createOrAddToLoan(null, null, rs);
            }
            
        } else if (cmdAction.isAction(ADD_TO_LOAN))
        {
            createOrAddToLoan((Loan)cmdAction.getData(), null, null);
            
        } else if (cmdAction.isAction(INFO_REQ_MESSAGE))
        {
            if (cmdAction.getData() == cmdAction)
            {
                // We get here when a user clicks on a InfoRequest NB action 
                createInfoRequest(null);
                
            } else if (cmdAction.getData() instanceof RecordSetIFace)
            {
                // We get here when a RecordSet is dropped on an InfoRequest
                Object data = cmdAction.getData();
                if (data instanceof RecordSetIFace)
                {
                    createInfoRequest((RecordSetIFace)data);
                }
            } else if (cmdAction.getData() instanceof CommandActionForDB)
            {
                // We get here when a InfoRequest is dropped on an InfoRequest NB action
                showInfoReqForm((CommandActionForDB)cmdAction.getData());
            }
            
        } else if (cmdAction.isAction("ReturnLoan"))
        {
            returnLoan();
            
        } else if (cmdAction.isAction(DELETE_CMD_ACT))
        {
            if (cmdAction instanceof CommandActionForDB)
            {
                deleteInfoRequest((CommandActionForDB)cmdAction);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void doCommand(final CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        if (cmdAction.isConsumed())
        {
            return;
        }
        
        //log.debug(cmdAction);
        
        if (cmdAction.isType(DB_CMD_TYPE))
        {
            processDatabaseCommands(cmdAction);

        } else if (cmdAction.isType(DataEntryTask.DATA_ENTRY))
        {
            if (cmdAction.isAction("SaveBeforeSetData"))
            {
                checkToPrintLoan(cmdAction);
            }
            
        } else if (cmdAction.isType(INTERACTIONS))
        {
            processInteractionsCommands(cmdAction);
            
        } else if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
            
        } else if (cmdAction.isType(RecordSetTask.RECORD_SET))
        {
            processRecordSetCommands(cmdAction);
        }
    }
    
    //--------------------------------------------------------------
    // Background loader class for loading a large number of loan preparations
    //--------------------------------------------------------------
    class LoanPrepLoader extends javax.swing.SwingWorker<Integer, Integer>
    {
        private InteractionsTask         task;
        private DataProviderSessionIFace session;
        private String                   sqlStr;
        private InfoRequest              infoRequest;
        private Loan                     loan;
               
        private ArrayList<CollectionObject> availColObjList = new ArrayList<CollectionObject>();
        private ArrayList<CollectionObject> noCurrDetList   = new ArrayList<CollectionObject>();
        private List<CollectionObject>      colObjList      = null;

        public LoanPrepLoader(final InteractionsTask         task, 
                              final DataProviderSessionIFace session,
                              final String                   sqlStr,
                              final Loan                     loan,
                              final InfoRequest              infoRequest)
        {
            this.task        = task;
            this.session     = session;
            this.sqlStr      = sqlStr;
            this.loan        = loan;
            this.infoRequest = infoRequest;
        }

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#doInBackground()
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Integer doInBackground() throws Exception
        {
            try
            {
                colObjList = (List<CollectionObject>)session.getDataList(sqlStr);
                UIRegistry.getStatusBar().setProgressRange("LoanLoader", 0, 100);
                
                int count = 0;
                for (CollectionObject co : colObjList)
                {
                    if (co.getDeterminations().size() > 0)
                    {
                        boolean hasCurrDet = false;
                        for (Determination det : co.getDeterminations())
                        {
                            if (det.getStatus() != null && 
                                det.getStatus().getType() != null &&
                                det.getStatus().getType().equals(DeterminationStatus.CURRENT))
                            {
                                hasCurrDet = true;
                                availColObjList.add(co);
                                break;
                            }
                        }
                        
                        if (!hasCurrDet)
                        {
                            noCurrDetList.add(co);
                        }
                    } else
                    {
                        availColObjList.add(co);
                    }
                    count++;
                    setProgress((int)(100.0 * count / colObjList.size()));
                }
            } catch (Exception ex)
            {
                log.error(ex);
                ex.printStackTrace();
            }
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#done()
         */
        @Override
        protected void done()
        {
            super.done();
            UIRegistry.getStatusBar().setProgressDone("LoanLoader");
            UIRegistry.clearGlassPaneMsg();
            task.loanPrepsLoaded(availColObjList, loan, infoRequest, session);
        }
        
    }
}
