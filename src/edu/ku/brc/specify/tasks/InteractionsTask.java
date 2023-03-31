/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.tasks.InteractionsProcessor.DEFAULT_SRC_TBL_ID;
import static edu.ku.brc.specify.tasks.InteractionsProcessor.forAcc;
import static edu.ku.brc.specify.tasks.InteractionsProcessor.getInteractionItemLookupField;
import static edu.ku.brc.ui.UIRegistry.displayInfoMsgDlg;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import edu.ku.brc.specify.datamodel.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.PermissionSettings;
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
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.db.CommandActionForDB;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.TableViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.dbsupport.TableModel2Excel;
import edu.ku.brc.helpers.EMailHelper;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.busrules.AccessionBusRules;
import edu.ku.brc.specify.datamodel.busrules.LoanBusRules;
import edu.ku.brc.specify.datamodel.busrules.LoanPreparationBusRules;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.specify.ui.LoanReturnDlg;
import edu.ku.brc.specify.ui.LoanReturnInfo;
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
    public static final DataFlavorTableExt DISPOSAL_FLAVOR     = new DataFlavorTableExt(Disposal.class, "Disposal");

    public static final  String   IS_USING_INTERACTIONS_PREFNAME = "Interactions.Using.Interactions.";

    protected static final String InfoRequestName      = "InfoRequest";
    protected static final String NEW_LOAN             = "NEW_LOAN";
    protected static final String NEW_ACCESSION        = "NEW_ACC";
    protected static final String ADD_TO_ACCESSION     = "AddToAccession";
    protected static final String NEW_DISPOSAL      = "NEW_DISPOSAL";
    protected static final String NEW_DEACC = "NEW_DEACC";
    protected static final String ADD_TO_DISPOSAL   = "AddToDisposal";
    public static final String ADD_TO_EXCHANGE   = "AddToExchange";
    protected static final String NEW_PERMIT           = "NEW_PERMIT";
    protected static final String NEW_GIFT             = "NEW_GIFT";
    protected static final String NEW_EXCHANGE_OUT     = "NEW_EXCHANGE_OUT";
    protected static final String PRINT_LOAN           = "PRINT_LOAN";
    protected static final String PRINT_INVOICE        = "PRINT_INVOICE";
    protected static final String INFO_REQ_MESSAGE     = "Specify Info Request";
    protected static final String CREATE_MAILMSG       = "CreateMailMsg";
    protected static final String ADD_TO_LOAN          = "AddToLoan";
    protected static final String ADD_TO_GIFT          = "AddToGift";
    protected static final String RET_LOAN             = "RET_LOAN";
    protected static final String OPEN_NEW_VIEW        = "OpenNewView";
    protected static final String LN_NO_PRP            = "LN_NO_PRP";
    
    protected static final int    loanTableId;
    protected static final int    infoRequestTableId;
    protected static final int    colObjTableId;
    protected static final int preparationTableId;

    // Data Members
    protected NavBox                  infoRequestNavBox;
    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    protected Vector<NavBoxItemIFace> invoiceList      = new Vector<NavBoxItemIFace>();
    protected NavBoxItemIFace         exchgNavBtn      = null; 
    protected NavBoxItemIFace         giftsNavBtn      = null; 
    protected NavBox                  actionsNavBox;
    
    protected boolean                 isUsingInteractions = true;
    protected ToolBarDropDownBtn      toolBarBtn       = null;
    protected int                     indexOfTBB       = -1;
    
    protected Vector<InteractionEntry>  entries = new Vector<InteractionEntry>();
    
    protected Vector<Integer> printableInvoiceTblIds = new Vector<Integer>();
    
    InteractionsProcessor<Gift> giftProcessor = new InteractionsProcessor<Gift>(this, InteractionsProcessor.forGift, Gift.getClassTableId());
    InteractionsProcessor<Loan> loanProcessor = new InteractionsProcessor<Loan>(this, InteractionsProcessor.forLoan,  Loan.getClassTableId());
    InteractionsProcessor<ExchangeOut> exchProcessor = new InteractionsProcessor<ExchangeOut>(this, InteractionsProcessor.forExchange,  ExchangeOut.getClassTableId());
    InteractionsProcessor<Accession> accProcessor = new InteractionsProcessor<Accession>(this, forAcc,  Accession.getClassTableId());
    InteractionsProcessor<Disposal> disposalProcessor = new InteractionsProcessor<>(this, InteractionsProcessor.forDisposal,  Disposal.getClassTableId());
    InteractionsProcessor<Deaccession> legalDeaccProcessor = new InteractionsProcessor<>(this, InteractionsProcessor.forLegalDeacc,  Deaccession.getClassTableId());

    static 
    {
        loanTableId        = DBTableIdMgr.getInstance().getIdByClassName(Loan.class.getName());
        infoRequestTableId = DBTableIdMgr.getInstance().getIdByClassName(InfoRequest.class.getName());
        colObjTableId      = DBTableIdMgr.getInstance().getIdByClassName(CollectionObject.class.getName());
        preparationTableId = DBTableIdMgr.getInstance().getIdByClassName(Preparation.class.getName());
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
        
        readEntries();
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

            actionsNavBox = new NavBox(getResourceString("CreateAndUpdate"));
            
            SpecifyAppContextMgr acm = (SpecifyAppContextMgr)AppContextMgr.getInstance();
            
            boolean showIRBox = true;
            for (InteractionEntry entry : entries)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(entry.getTableName());
                if (!AppContextMgr.isSecurityOn() || tableInfo.getPermissions().canView())
                {
                    String label;
                    if (StringUtils.isNotEmpty(entry.getLabelKey()))
                    {
                        label = getResourceString(entry.getLabelKey());
                    } else
                    {
                        label = tableInfo.getTitle();
                    }
                    
                    String tooltip = "";
                    if (StringUtils.isEmpty(entry.getTooltip()) && StringUtils.isNotEmpty(entry.getViewName()))
                    {
                        ViewIFace view = acm.getView(entry.getViewName());
                        if (view != null)
                        {
                            tooltip = getLocalizedMessage("DET_OPEN_VIEW", view.getObjTitle());
                        }
                    } else if (StringUtils.isNotEmpty(entry.getTooltip()))
                    {
                        tooltip = getLocalizedMessage(entry.getTooltip());
                    }
                    
                    entry.setTitle(label);
                    entry.setI18NTooltip(tooltip);

                    if (entry.isOnLeft())
                    {
                        NavBoxButton navBtn = addCommand(actionsNavBox, tableInfo, entry);
                        navBtn.setToolTip(entry.getI18NTooltip());
                    }
                    
                    if (StringUtils.isNotEmpty(entry.getViewName()) && entry.isSearchService())
                    {
                        CommandAction cmdAction = createCmdActionFromEntry(entry, tableInfo);
                        ContextMgr.registerService(10, entry.getViewName(), tableInfo.getTableId(), cmdAction, this, "Data_Entry", tableInfo.getTitle(), true); // the Name gets Hashed
                    }
                } else if (tableInfo.getTableId() == infoRequestTableId)
                {
                    showIRBox = false;
                }
            }
            navBoxes.add(actionsNavBox);
            
            infoRequestNavBox  = new NavBox(getResourceString("InfoRequest"));
            loadNavBox(infoRequestNavBox, InfoRequest.class, INFOREQUEST_FLAVOR, showIRBox);
        }
        isShowDefault = true;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doProcessAppCommands(edu.ku.brc.ui.CommandAction)
     */
    @Override
    protected void doProcessAppCommands(CommandAction cmdAction)
    {
        // TODO Auto-generated method stub
        super.doProcessAppCommands(cmdAction);
        
        if (cmdAction.isAction(APP_RESTART_ACT) ||
            cmdAction.isAction(APP_START_ACT))
        {
            this.isInitialized = false;
            initialize();
        }
    }

    /**
     * Returns whether a table id if considered to be an Interaction.
     * @param tableId the table ID in question
     * @return true if it is a table that is handled by Interactions
     */
    public static boolean isInteractionTable(final int tableId)
    {
        return tableId == Loan.getClassTableId() ||
               tableId == Gift.getClassTableId() ||
               tableId == Accession.getClassTableId() ||
               tableId == Permit.getClassTableId() ||
               tableId == RepositoryAgreement.getClassTableId() ||
               tableId == ExchangeIn.getClassTableId() ||
               tableId == ExchangeOut.getClassTableId() ||
                tableId == Borrow.getClassTableId() ||
                tableId == Disposal.getClassTableId() ||
                tableId == Deaccession.getClassTableId();
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);
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
        Vector<TaskConfigItemIFace> srvList  = new Vector<TaskConfigItemIFace>();
        
        boolean showIRBox = true;
        for (InteractionEntry entry : entries)
        {
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(entry.getTableName());
            if (!AppContextMgr.isSecurityOn() || tableInfo.getPermissions().canView())
            {
                //System.err.println(entry.getName()+"\t  "+entry.isSearchService()+"   "+entry.isOnLeft()+"   "+entry.isVisible());
                entry.setEnabled(true);
                if (entry.isVisible())
                {
                    Vector<TaskConfigItemIFace> list = entry.isOnLeft() ? stdList : miscList;
                    // Clone for undo (Cancel)
                    try
                    {
                        list.add((TaskConfigItemIFace)entry.clone());
                        
                    } catch (CloneNotSupportedException ex) {/* ignore */}
                } else
                {
                    srvList.add(entry);
                }
            } else
            {
                entry.setEnabled(false);
                
                if (tableInfo.getTableId() == infoRequestTableId)
                {
                    showIRBox = false;
                }
            }
        }
        
        if (showIRBox)
        {
            navBoxes.add(infoRequestNavBox);
        } else
        {
            navBoxes.remove(infoRequestNavBox);
        }
        
        int origNumStd = stdList.size();
        
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
            
            for (TaskConfigItemIFace ie : srvList)
            {
                entries.add((InteractionEntry)ie);
            }
            
            writeEntries();
            
            actionsNavBox.clear();
            
            Collections.sort(entries);
            
            for (InteractionEntry entry : entries)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(entry.getTableName());
                if (entry.isEnabled())
                {
                    if (entry.isOnLeft())
                    {
                        NavBoxButton navBtn = addCommand(actionsNavBox, tableInfo, entry);
                        navBtn.setToolTip(entry.getI18NTooltip());
                        
                    } else if (StringUtils.isNotEmpty(entry.getViewName()) && !entry.isSearchService())
                    {
                        CommandAction cmdAction = createCmdActionFromEntry(entry, tableInfo);
                        ContextMgr.registerService(10, entry.getViewName(), tableInfo.getTableId(), cmdAction, this, "Data_Entry", tableInfo.getTitle(), true); // the Name gets Hashed
                    }
                }
            }
            
            NavBoxMgr.getInstance().validate();
            NavBoxMgr.getInstance().invalidate();
            NavBoxMgr.getInstance().doLayout();
            
            if (stdList.size() == 0)
            {
                String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
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
                String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
                AppPreferences.getRemote().putBoolean(IS_USING_INTERACTIONS_PREFNAME+ds, true);
                prefsChanged(new CommandAction(null, null, AppPreferences.getRemote()));
                
                if (origNumStd == 0)
                {
                    JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
                    int inx = indexOfTBB != -1 ? indexOfTBB : 4;
                    TaskMgr.addToolbarBtn(toolBarBtn, inx);
                    toolBar.validate();
                    toolBar.repaint();  
                }
            }
        }
    }

    /**
     * Retrieves the prefs that we cache.
     */
    protected void setUpCachedPrefs()
    {
        AppPreferences remotePrefs = AppPreferences.getRemote();
        String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
        isUsingInteractions = remotePrefs.getBoolean(IS_USING_INTERACTIONS_PREFNAME+ds, true);
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
     * @param entry
     * @param tableInfo
     * @return
     */
    private CommandAction createCmdActionFromEntry(final InteractionEntry entry, final DBTableInfo tableInfo)
    {
        CommandAction cmdAction = new CommandAction(entry.getCmdType(), entry.getAction(), tableInfo.getTableId());
        if (StringUtils.isNotEmpty(entry.getViewName()))
        {
            cmdAction.setProperty("view", entry.getViewName());
            cmdAction.setProperty(NavBoxAction.ORGINATING_TASK, this);
        }
        return cmdAction;
    }
            
    /**
     * @param navBox
     * @param tableInfo
     * @param text
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
        CommandAction cmdAction = createCmdActionFromEntry(entry, tableInfo);
        if (StringUtils.isNotEmpty(entry.getViewName()) && entry.isSearchService())
        {
            ContextMgr.registerService(10, entry.getViewName(), tableInfo.getTableId(), cmdAction, this, "Data_Entry", tableInfo.getTitle(), true); // the Name gets Hashed
        }
        
        NavBoxButton roc = (NavBoxButton)makeDnDNavBtn(navBox, entry.getTitle(), entry.getIconName(), cmdAction, null, true, false);// true means make it draggable
        
        for (EntryFlavor ef : entry.getDraggableFlavors())
        {
            if (cmdAction.getAction().equals(PRINT_INVOICE))
            {
                //this.printableInvoiceTblIds.add(ef.getDataFlavorClass().
                System.out.println(ef);
            }
            DataFlavorTableExt dfx = new DataFlavorTableExt(ef.getDataFlavorClass(), ef.getHumanReadable(), ef.getTableIdsAsArray());
            roc.addDragDataFlavor(dfx);
        }
        
        for (EntryFlavor ef : entry.getDroppableFlavors())
        {
            if (cmdAction.getAction().equals(PRINT_INVOICE))
            {
                if (ef.getClassName().equals(RecordSetTask.class.getName()))
                {
                    this.printableInvoiceTblIds.addAll(ef.getTableIds());
                }
            }
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
                              final DataFlavor dragFlav,
                              final boolean    addBox)
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(dataClass.getSimpleName());
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            String sql = "FROM " + ti.getClassName() + " WHERE collectionMemberId = COLMEMID";
            List<?> list = session.getDataList(QueryAdjusterForDomain.getInstance().adjustSQL(sql));
            for (Iterator<?> iter=list.iterator();iter.hasNext();)
            {
                FormDataObjIFace   dataObj = (FormDataObjIFace)iter.next();
                createNavBtn(navBox, dragFlav, dataObj, ti);
            }
            if (addBox)
            {
                navBoxes.add(navBox);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    /**
     * @param navBox
     * @param dragFlav
     * @param dataObj
     * @param ti
     * @return
     */
    protected NavBoxItemIFace createNavBtn(final NavBox     navBox, 
                                           final DataFlavor dragFlav,
                                           final FormDataObjIFace dataObj, 
                                           final DBTableInfo ti)
    {
        RecordSet rs = new RecordSet();
        rs.initialize();
        rs.set(dataObj.getIdentityTitle(), ti.getTableId(), RecordSet.HIDDEN);
        rs.addItem(dataObj.getId());
        
        CommandAction      cmd     = new CommandAction("Data_Entry", "Edit", rs);
        cmd.setProperty(NavBoxAction.ORGINATING_TASK, this);
        
        CommandActionForDB delCmd  = new CommandActionForDB(INTERACTIONS, DELETE_CMD_ACT, ti.getTableId(), dataObj.getId());
        NavBoxItemIFace    nbi     = makeDnDNavBtn(navBox, dataObj.getIdentityTitle(), ti.getShortClassName(), cmd, delCmd, true, true);
        RolloverCommand    roc     = (NavBoxButton)nbi;
        nbi.setData(rs);
        roc.addDragDataFlavor(dragFlav);
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        return nbi;
    }

    /**
     * Helper method for registering a NavBoxItem as a GhostMouseDropAdapter
     * @param navBox the parent box for the nbi to be added to
     * @param navBoxItemDropZone the nbi in question
     * @param params
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

        RecordSetTask     rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);
        List<NavBoxIFace> nbs    = rsTask.getNavBoxes();
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
        
        return starterPane = StartUpTask.createFullImageSplashPanel(title, this);
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
            String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
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
    protected void printInvoice(final String fileNameArg, final Object data)
    {
        if (data instanceof RecordSetIFace)
        {
            RecordSetIFace rs = (RecordSetIFace)data;
            
            InfoForTaskReport invoiceInfo = getInvoiceInfo(rs.getDbTableId());
            if (invoiceInfo != null)
            {
                dispatchReport(invoiceInfo, rs, "LoanInvoice");
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
        JScrollPane     scroll = UIHelper.createScrollPane(ta);
        JLabel          lbl    = UIHelper.createLabel(getResourceString("InteractionsTask.MISSING_DET"));
        
        pb.add(lbl,    cc.xy(1, 1));
        pb.add(scroll, cc.xy(1, 3));
        
        ta.setText(sb.toString());
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        
        pb.setDefaultDialogBorder();
        
        CustomDialog dlg = CustomDialog.create(
                getResourceString("InteractionsTask.MISSING_DET_TITLE"), 
                true, 
                CustomDialog.OK_BTN, 
                pb.getPanel());
        //dlg.setOkLabel(getResourceString(key))
        dlg.setVisible(true);
    }
    
    /**
     * @return returns a list of RecordSets of InfoRequests
     */
    protected Vector<RecordSetIFace> getInfoReqRecordSetsFromSideBar()
    {
        Vector<RecordSetIFace> rsList = new Vector<RecordSetIFace>();
        for (NavBoxItemIFace nbi : infoRequestNavBox.getItems())
        {
            rsList.add((RecordSet)nbi.getData());
        }
        return rsList;
    }
    
    /**
     * @return returns a list of RecordSets of InfoRequests
     */
    protected Vector<RecordSetIFace> getLoanRecordSetsFromSideBar()
    {
        Vector<RecordSetIFace> rsList = new Vector<RecordSetIFace>();
        for (NavBoxItemIFace nbi : infoRequestNavBox.getItems())
        {
            rsList.add((RecordSet)nbi.getData());
        }
        return rsList;
    }
    
    /**
     * @param existingLoan
     * @param infoRequest
     * @param prepsHash
     */
    protected void addPrepsToLoan(final OneToManyProviderIFace   existingLoanArg, 
                                  final InfoRequest                 infoRequest,
                                  final Hashtable<Integer, Integer> prepsHash,
                                  final Viewable                    srcViewable)
    {
        Loan existingLoan = (Loan)existingLoanArg;
        Loan loan;
        
        if (existingLoan == null)
        {
            loan = new Loan();
            loan.initialize();
            
            Calendar dueDate = Calendar.getInstance();
            dueDate.add(Calendar.MONTH, 6);                 // XXX PREF Due Date
            loan.setCurrentDueDate(dueDate);
            
            //Shipment shipment = new Shipment();
            //shipment.initialize();
            
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
            
            //if (infoRequest != null && infoRequest.getAgent() != null)
            {
            //    shipment.setShippedTo(infoRequest.getAgent());
            }
            
            //loan.addReference(shipment, "shipments");
        } else
        {
            loan = existingLoan;
        }
        
        Hashtable<Integer, LoanPreparation> prepToLoanPrepHash = null;
        if (existingLoan != null) {
            prepToLoanPrepHash = new Hashtable<Integer, LoanPreparation>();
            for (LoanPreparation lp : existingLoan.getLoanPreparations()) {
                if (lp.getPreparation() !=  null) {
                    if (lp.getPreparation() != null) {
                        prepToLoanPrepHash.put(lp.getPreparation().getId(), lp);
                    }
                }
            }
        }

        DataProviderSessionIFace session = null;
        try {
            session = DataProviderFactory.getInstance().createSession();

            if (prepsHash.isEmpty()) {
                LoanPreparation lpo = new LoanPreparation();
                lpo.initialize();
                lpo.setLoan(loan);
                loan.getLoanPreparations().add(lpo);
            } else for (Integer prepId : prepsHash.keySet()) {
                Preparation prep = session.get(Preparation.class, prepId);
                Integer count = prepsHash.get(prepId);
                if (prepToLoanPrepHash != null) {
                    LoanPreparation lp = prepToLoanPrepHash.get(prep.getId());
                    if (lp != null) {
                        int lpCnt = lp.getQuantity();
                        lpCnt += count;
                        lp.setQuantity(lpCnt);
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
        } catch (Exception ex)
        {
            ex.printStackTrace();
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);

        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        if (existingLoan == null)
        {
            if (srcViewable != null)
            {
                srcViewable.setNewObject(loan);
                
            } else
            {
                DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
                if (dataEntryTask != null)
                {
                    DBTableInfo loanTableInfo = DBTableIdMgr.getInstance().getInfoById(loan.getTableId());
                    dataEntryTask.openView(this, null, loanTableInfo.getDefaultFormName(), "edit", loan, true);
                }
            }
        } else 
        {
            CommandDispatcher.dispatch(new CommandAction(INTERACTIONS, LoanPreparationBusRules.REFRESH_PREPS, loan));
        }
    }


    /**
     * @param existingAccArg
     * @param cos
     */
    protected void addCosToAcc(final OneToManyProviderIFace existingAccArg, final RecordSetIFace cos, final Viewable srcViewable) {
    	Accession existingAcc = (Accession)existingAccArg;
    	Accession acc;

    	if (existingAcc == null) {
    		acc = new Accession();
    		acc.initialize();
    		acc.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
    	} else {
    		acc = existingAcc;
    	}

    	if (cos != null) {
    		DataProviderSessionIFace session = null;
    		try {
    			session = DataProviderFactory.getInstance().createSession();

    			for (RecordSetItemIFace coId : cos.getItems()) {
    				CollectionObject co = session.get(CollectionObject.class, coId.getRecordId());
    				if (co != null) {
    					co.forceLoad();
    					acc.getCollectionObjects().add(co);
    					//Accession coAcc = co.getAccession();
    					//if (coAcc != null) {
    					//	coAcc.getCollectionObjects().remove(co);
    					//}
    					co.setAccession(acc);
    				}
    			}

    		} catch (Exception ex) {
    			ex.printStackTrace();
    			UsageTracker.incrHandledUsageCount();
    			edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);

    		} finally {
    			if (session != null) {
    				session.close();
    			}
    		}
    	}
        if (existingAcc == null) {
            if (srcViewable != null) {
                srcViewable.setNewObject(acc);
            } else {
                DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
                if (dataEntryTask != null) {
                    DBTableInfo accTableInfo = DBTableIdMgr.getInstance().getInfoById(acc.getTableId());
                    dataEntryTask.openView(this, null, accTableInfo.getDefaultFormName(), "edit", acc, true);
                }
            }
        } else {
            CommandDispatcher.dispatch(new CommandAction(INTERACTIONS, AccessionBusRules.REFRESH_COS, acc));
        }

    }

    /**
     * @param existingAccArg
     * @param objs
     */
    protected void addToLegalDeacc(final OneToManyProviderIFace existingDeaccArg, final RecordSetIFace objs, final Viewable srcViewable) {
        Deaccession existingLegaldeacc = (Deaccession)existingDeaccArg;
        Deaccession deacc;

        if (existingLegaldeacc == null) {
            deacc = new Deaccession();
            deacc.initialize();
        } else {
            deacc = existingLegaldeacc;
        }

        if (objs != null) {
            log.error("adding to deaccessions is not implemented");
        }
        if (existingLegaldeacc == null) {
            if (srcViewable != null) {
                srcViewable.setNewObject(deacc);
            } else {
                DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
                if (dataEntryTask != null) {
                    DBTableInfo deaccTableInfo = DBTableIdMgr.getInstance().getInfoById(deacc.getTableId());
                    dataEntryTask.openView(this, null, deaccTableInfo.getDefaultFormName(), "edit", deacc, true);
                }
            }
        } else {
            CommandDispatcher.dispatch(new CommandAction(INTERACTIONS, AccessionBusRules.REFRESH_COS, deacc));
        }

    }

    /**
     * @param existingLoan
     * @param infoRequest
     * @param prepsHash
     */
    protected void addPrepsToGift(final OneToManyProviderIFace existingGiftArg, 
                                  final InfoRequest               infoRequest,
                                  final Hashtable<Integer, Integer> prepsHash,
                                  final Viewable                    srcViewable)
    {
        Gift existingGift = (Gift)existingGiftArg;
        Gift gift;
        
        if (existingGift == null)
        {
            gift = new Gift();
            gift.initialize();
            
            //Calendar dueDate = Calendar.getInstance();
            //dueDate.add(Calendar.MONTH, 6);                 // XXX PREF Due Date
            
            //Shipment shipment = new Shipment();
            //shipment.initialize();
            
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
            
            //if (infoRequest != null && infoRequest.getAgent() != null)
            //{
            //    shipment.setShippedTo(infoRequest.getAgent());
            //}
            
            //gift.addReference(shipment, "shipments");
        } else
        {
            gift = existingGift;
        }
        
        Hashtable<Integer, GiftPreparation> prepToGiftPrepHash = null;
        if (existingGift != null)
        {
            prepToGiftPrepHash = new Hashtable<Integer, GiftPreparation>();
            for (GiftPreparation lp : existingGift.getGiftPreparations())
            {
                if (lp.getPreparation() != null) {
                    prepToGiftPrepHash.put(lp.getPreparation().getId(), lp);
                }
            }
        }
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();

            if (prepsHash.size() == 0) {
                GiftPreparation gpo = new GiftPreparation();
                gpo.initialize();
                gpo.setGift(gift);
                gift.getGiftPreparations().add(gpo);
            }
            for (Integer prepId : prepsHash.keySet())
            {
                Preparation prep  = session.get(Preparation.class, prepId);
                Integer     count = prepsHash.get(prepId);
                if (prepToGiftPrepHash != null)
                {
                    GiftPreparation gp = prepToGiftPrepHash.get(prep.getId());
                    if (gp != null)
                    {
                        int lpCnt = gp.getQuantity();
                        lpCnt += count;
                        gp.setQuantity(lpCnt);
                        //System.err.println("Adding "+count+"  to "+lp.hashCode());
                        continue;
                    }
                }
                
                GiftPreparation gpo = new GiftPreparation();
                gpo.initialize();
                  gpo.setPreparation(prep);
                gpo.setQuantity(count);
                gpo.setGift(gift);
                gift.getGiftPreparations().add(gpo);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);

        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        if (existingGift == null)
        {
            if (srcViewable != null)
            {
                srcViewable.setNewObject(gift);
                
            } else
            {
                DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
                if (dataEntryTask != null)
                {
                    DBTableInfo giftTableInfo = DBTableIdMgr.getInstance().getInfoById(gift.getTableId());
                    dataEntryTask.openView(this, null, giftTableInfo.getDefaultFormName(), "edit", gift, true);
                }
            }
        } else 
        {
            CommandDispatcher.dispatch(new CommandAction(INTERACTIONS, "REFRESH_GIFT_PREPS", gift));
        }
    }

    protected void addPrepsToExchangeOut(final OneToManyProviderIFace existingExchangeOutArg,
                                      final InfoRequest infoRequest,
                                      final Hashtable<Integer, Integer> prepsHash,
                                      final Viewable srcViewable) {
        ExchangeOut existingExchangeOut = (ExchangeOut) existingExchangeOutArg;
        ExchangeOut exchange;

        if (existingExchangeOut == null) {
            exchange = new ExchangeOut();
            exchange.initialize();
            exchange.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

            Calendar dueDate = Calendar.getInstance();
            dueDate.add(Calendar.MONTH, 6);                 // XXX PREF Due Date

            //Shipment shipment = new Shipment();
            //shipment.initialize();

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

            //if (infoRequest != null && infoRequest.getAgent() != null) {
            //    shipment.setShippedTo(infoRequest.getAgent());
            //}

            //exchange.addReference(shipment, "shipments");
        } else {
            exchange = existingExchangeOut;
        }

        Hashtable<Integer, ExchangeOutPrep> prepToExchangeOutPrepHash = null;
        if (existingExchangeOut != null) {
            prepToExchangeOutPrepHash = new Hashtable<Integer, ExchangeOutPrep>();
            for (ExchangeOutPrep lp : existingExchangeOut.getExchangeOutPreps()) {
                if (lp.getPreparation() != null) {
                    prepToExchangeOutPrepHash.put(lp.getPreparation().getId(), lp);
                }
            }
        }

        DataProviderSessionIFace session = null;
        try {
            session = DataProviderFactory.getInstance().createSession();

            if (prepsHash.isEmpty()) {
                ExchangeOutPrep eopo = new ExchangeOutPrep();
                eopo.initialize();
                eopo.setExchangeOut(exchange);
                exchange.getExchangeOutPreps().add(eopo);
            } else for (Integer prepId : prepsHash.keySet()) {
                Preparation prep = session.get(Preparation.class, prepId);
                Integer count = prepsHash.get(prepId);
                if (prepToExchangeOutPrepHash != null) {
                    ExchangeOutPrep gp = prepToExchangeOutPrepHash.get(prep.getId());
                    if (gp != null) {
                        int lpCnt = gp.getQuantity();
                        lpCnt += count;
                        gp.setQuantity(lpCnt);
                        //System.err.println("Adding "+count+"  to "+lp.hashCode());
                        continue;
                    }
                }

                ExchangeOutPrep gpo = new ExchangeOutPrep();
                gpo.initialize();
                gpo.setPreparation(prep);
                gpo.setQuantity(count);
                gpo.setExchangeOut(exchange);
                exchange.getExchangeOutPreps().add(gpo);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);

        } finally {
            if (session != null) {
                session.close();
            }
        }

        if (existingExchangeOut == null) {
            if (srcViewable != null) {
                srcViewable.setNewObject(exchange);

            } else {
                DataEntryTask dataEntryTask = (DataEntryTask) TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
                if (dataEntryTask != null) {
                    DBTableInfo exchangeTableInfo = DBTableIdMgr.getInstance().getInfoById(exchange.getTableId());
                    dataEntryTask.openView(this, null, exchangeTableInfo.getDefaultFormName(), "edit", exchange, true);
                }
            }
        } else {
            CommandDispatcher.dispatch(new CommandAction(INTERACTIONS, "REFRESH_EXCHANGE_PREPS", exchange));
        }
    }

    /**
     *
     * @param existingDisposalArg
     * @param infoRequest
     * @param prepsHash
     * @param srcViewable
     */
    protected void addPrepsToDisposal(final OneToManyProviderIFace existingDisposalArg,
                                  final InfoRequest               infoRequest,
                                  final Hashtable<Integer, Integer> prepsHash,
                                  final Viewable                    srcViewable)
    {
        Disposal existingDisposal = (Disposal)existingDisposalArg;
        Disposal disposal;

        if (existingDisposal == null) {
            disposal = new Disposal();
            disposal.initialize();
        } else {
            disposal = existingDisposal;
        }

        Hashtable<Integer, DisposalPreparation> prepToDisposalPrepHash = null;
        if (existingDisposal != null) {
            prepToDisposalPrepHash = new Hashtable<>();
            for (DisposalPreparation lp : existingDisposal.getDisposalPreparations())
            {
                if (lp.getPreparation() != null) {
                    prepToDisposalPrepHash.put(lp.getPreparation().getId(), lp);
                }
            }
        }

        DataProviderSessionIFace session = null;
        try {
            session = DataProviderFactory.getInstance().createSession();
            if (prepsHash.size() == 0) {
                DisposalPreparation dpo = new DisposalPreparation();
                dpo.initialize();
                dpo.setDisposal(disposal);
                disposal.getDisposalPreparations().add(dpo);
            }
            for (Integer prepId : prepsHash.keySet()) {
                Preparation prep  = session.get(Preparation.class, prepId);
                Integer     count = prepsHash.get(prepId);
                if (prepToDisposalPrepHash != null) {
                    DisposalPreparation dp = prepToDisposalPrepHash.get(prep.getId());
                    if (dp != null) {
                        int dpCnt = dp.getQuantity();
                        dpCnt += count;
                        dp.setQuantity(dpCnt);
                        //System.err.println("Adding "+count+"  to "+lp.hashCode());
                        continue;
                    }
                }
                DisposalPreparation dpo = new DisposalPreparation();
                dpo.initialize();
                dpo.setPreparation(prep);
                dpo.setQuantity(count);
                dpo.setDisposal(disposal);
                disposal.getDisposalPreparations().add(dpo);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);

        } finally {
            if (session != null) {
                session.close();
            }
        }

        if (existingDisposal == null) {
            if (srcViewable != null) {
                srcViewable.setNewObject(disposal);
            } else {
                DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
                if (dataEntryTask != null) {
                    DBTableInfo disposalTableInfo = DBTableIdMgr.getInstance().getInfoById(disposal.getTableId());
                    dataEntryTask.openView(this, null, disposalTableInfo.getDefaultFormName(), "edit", disposal, true);
                }
            }
        } else {
            CommandDispatcher.dispatch(new CommandAction(INTERACTIONS, "REFRESH_DISPOSAL_PREPS", disposal));
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
    protected void createInfoRequest(final RecordSetIFace recordSetArg,
                                     final CommandAction cmdAction)
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(InfoRequest.getClassTableId());
        
        ViewIFace view = AppContextMgr.getInstance().getView(tableInfo.getDefaultFormName());

        InfoRequest infoRequest = new InfoRequest();
        infoRequest.initialize();
        
        RecordSetTask          rsTask       = (RecordSetTask)TaskMgr.getTask(RecordSetTask.RECORD_SET);
        List<RecordSetIFace>   colObjRSList = rsTask.getRecordSets(CollectionObject.getClassTableId());

        RecordSetIFace recordSetFromDB = getRecordSetOfDataObjs(recordSetArg, CollectionObject.class, "catalogNumber", colObjRSList.size());
        
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
                Taskable irTask = TaskMgr.getTask("InfoRequest");
                if (cmdAction != null && cmdAction.getData() instanceof Viewable)
                {
                    ((Viewable)cmdAction.getData()).setNewObject(infoRequest);
                    
                } else
                {
                    createFormPanel(irTask.getTitle(), view.getViewSetName(), view.getName(), "edit", infoRequest, MultiView.IS_NEW_OBJECT, 
                                    irTask.getIcon(16));
                }
                
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
        boolean isGift = cmdAction.getData() instanceof Gift;
        if (cmdAction.getData() instanceof Loan || isGift)
        {
            Loan loan = isGift ? null : (Loan)cmdAction.getData();
            Gift gift = isGift ? (Gift)cmdAction.getData() : null;
            
            Boolean     doPrintInvoice = null;
            FormViewObj formViewObj    = getCurrentFormViewObj();
            if (formViewObj != null)
            {
                Component comp = formViewObj.getControlByName("generateInvoice");
                if (comp instanceof JCheckBox)
                {
                    doPrintInvoice = ((JCheckBox)comp).isSelected();
                }
            }
            
            if (doPrintInvoice == null)
            {
                String    number  = isGift ? gift.getGiftNumber() :  loan.getLoanNumber();
                String    btnLbl  = getResourceString(isGift ? "GIFT" : "LOAN");
                String    msg     = getLocalizedMessage("CreateInvoiceForNum", getResourceString(isGift ? "GIFT" : "LOAN"), number);
                Object[]  options = {btnLbl, getResourceString("CANCEL")};
                int n = JOptionPane.showOptionDialog(UIRegistry.get(UIRegistry.FRAME),
                                                    msg,
                                                    btnLbl,
                                                    JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE,
                                                    null,     //don't use a custom Icon
                                                    options,  //the titles of buttons
                                                    options[0]); //default button title
                doPrintInvoice = n == 0;
            }
            
            // XXX DEBUG
            //printLoan = false;
            if (doPrintInvoice)
            {
                InfoForTaskReport invoice = getReport(isGift);
                
                if (invoice == null)
                {
                    return;
                }
                
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    //session.attach(loan);
                    
                    String hql = isGift ? "FROM Gift WHERE giftId = "+gift.getGiftId() :
                                          "FROM Loan WHERE loanId = "+loan.getLoanId();
                    
                    loan = isGift ? null : (Loan)session.getData(hql);
                    gift = isGift ? (Gift)session.getData(hql) : null;
                    
                    Set<Shipment> shipments = isGift ? gift.getShipments() : loan.getShipments();
                    boolean keepGoing = true;
                    if (invoice.getSpReport() == null) 
                    {
                    	if (shipments != null && shipments.size() == 0)
                    	{
                    		UIRegistry.displayErrorDlg(String.format(getResourceString("NO_SHIPMENTS_ERROR"), invoice.getSpAppResource().getName()));
                    		keepGoing = false;
                    	} else if (shipments != null && shipments.size() > 1)
                    	{
                        // XXX Do we allow them to pick a shipment or print all?
                    		UIRegistry.displayErrorDlg(String.format(getResourceString("MULTI_SHIPMENTS_NOT_SUPPORTED"), invoice.getSpAppResource().getName()));
                    		keepGoing = false;
                    	} //else
                    	//{
                        // XXX At the moment this is just checking to see if there is at least one "good/valid" shipment
                        // but the hard part will be sending the correct info so the report can be printed
                        // using both a Loan Id and a Shipment ID, and at some point distinguishing between using
                        // the shipped by versus the shipper.
//                        Shipment shipment = isGift ? gift.getShipments().iterator().next() : loan.getShipments().iterator().next();
//                        if (shipment.getShippedBy() == null)
//                        {
//                            UIRegistry.displayErrorDlg(getResourceString("SHIPMENT_MISSING_SHIPPEDBY"));
//                            
//                        } else if (shipment.getShippedBy().getAddresses().size() == 0)
//                        {
//                            UIRegistry.displayErrorDlg(getResourceString("SHIPPEDBY_MISSING_ADDR"));
//                        } else if (shipment.getShippedTo() == null)
//                        {
//                            UIRegistry.displayErrorDlg(getResourceString("SHIPMENT_MISSING_SHIPPEDTO"));
//                        } else if (shipment.getShippedTo().getAddresses().size() == 0)
//                        {
//                            UIRegistry.displayErrorDlg(getResourceString("SHIPPEDTO_MISSING_ADDR"));
//                        } else
//                        {
                    }
					if (keepGoing) 
					{
						String identTitle;
						int tableId;
						Integer id;
						if (isGift) 
						{
							identTitle = gift.getIdentityTitle();
							tableId = gift.getTableId();
							id = gift.getId();
						} else 
						{
							identTitle = loan.getIdentityTitle();
							tableId = loan.getTableId();
							id = loan.getId();
						}

						RecordSet rs = new RecordSet();
						rs.initialize();
						rs.setName(identTitle);
						rs.setDbTableId(tableId);
						rs.addItem(id);

						dispatchReport(invoice, rs, "LoanInvoice");
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
    
    public InfoForTaskReport getReport(final boolean isGift)
    {
        return getInvoiceInfo(DBTableIdMgr.getInstance().getIdByShortName(isGift ? "Gift" : "Loan"));
    }
    
    /**
     * @return a loan invoice if one exists.
     * 
     * If more than one report is defined for loan then user must choose.
     * 
     * Fairly goofy code. Eventually may want to add ui to allow labeling resources as "invoice" (see printLoan()).
     */
    public InfoForTaskReport getInvoiceInfo(final int invoiceTblId)
    {
        DataProviderSessionIFace session = null;
        ChooseFromListDlg<InfoForTaskReport> dlg = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            List<AppResourceIFace> reps = AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.LABELS_MIME);
            reps.addAll(AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.REPORTS_MIME));
            Vector<InfoForTaskReport> repInfo = new Vector<InfoForTaskReport>();
            
            for (AppResourceIFace rep : reps)
            {
                Properties params = rep.getMetaDataMap();
                String tableid = params.getProperty("tableid"); 
                SpReport spReport = null;
                boolean includeIt = false;
                try
                {
                    Integer tblId = null;
                    try
                    {
                        tblId = Integer.valueOf(tableid);
                    }
                    catch (NumberFormatException ex)
                    {
                        //continue;
                    }
                    if (tblId == null)
                    {
                        continue;
                    }
                    
                    if (tblId.equals(invoiceTblId))
                    {
                        includeIt = true;
                    }
                    else if (tblId.equals(-1))
                    {
                        QueryIFace q = session.createQuery("from SpReport spr join spr.appResource apr "
                              + " join spr.query spq "
                              + "where apr.id = " + ((SpAppResource )rep).getId() 
                              + " and spq.contextTableId = " + invoiceTblId, false);
                        List<?> spReps = q.list();
                        if (spReps.size() > 0)
                        {
                            includeIt = true;
                            spReport = (SpReport )((Object[] )spReps.get(0))[0];
                            spReport.forceLoad();
                            if (spReps.size() > 1)
                            {
                                //should never happen
                                log.error("More than SpReport exists for " + rep.getName());
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);
                    //skip this res
                }
                if (includeIt)
                {
                    repInfo.add(new InfoForTaskReport((SpAppResource )rep, spReport));
                }
            }
            
            if (repInfo.size() == 0)
            {
                UIRegistry.displayInfoMsgDlgLocalized("InteractionsTask.NoInvoiceFound", 
                            DBTableIdMgr.getInstance().getTitleForId(invoiceTblId));
                return null;
            }
            
            if (repInfo.size() == 1)
            {
                return repInfo.get(0);
            }
            
            dlg = new ChooseFromListDlg<InfoForTaskReport>((Frame) UIRegistry
                    .getTopWindow(), getResourceString("REP_CHOOSE_INVOICE"),
                    repInfo);
            dlg.setVisible(true);
            if (dlg.isCancelled()) 
            { 
                return null; 
            }
            return dlg.getSelectedObject();
            
//            for (AppResourceIFace res : AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.LABELS_MIME))
//            {
//                Properties params = res.getMetaDataMap();
//                String tableid = params.getProperty("tableid"); 
//                boolean includeIt = false;
//                try
//                {
//                    includeIt = Integer.valueOf(tableid).equals(loanTblId);
//                }
//                catch (Exception ex)
//                {
//                    //skip this res
//                }
//                if (includeIt)
//                {
//                    aprs.add((SpAppResource )res);
//                }
//            }
//            for (AppResourceIFace res : AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.REPORTS_MIME))
//            {
//                Properties params = res.getMetaDataMap();
//                String tableid = params.getProperty("tableid"); 
//                boolean includeIt = false;
//                try
//                {
//                    includeIt = Integer.valueOf(tableid).equals(loanTblId);
//                }
//                catch (Exception ex)
//                {
//                    // skip this res
//                }
//                if (includeIt)
//                {
//                    aprs.add((SpAppResource) res);
//                }
//            }
//            
//            if (aprs.size() == 0)
//            {
//                return null;
//            }
//            
//            if (aprs.size() == 1)
//            {
//                return aprs.get(0);
//            }
            
            
//            Vector<String> reportNames = new Vector<String>();
//            for (Integer id : ids)
//            {
//                QueryIFace q = session.createQuery("from SpReport spr join spr.appResource apr "
//                    + "where apr.id = " + id.toString(), false);
//                List<?> reports = q.list();
//                for (Object repObj : reports)
//                {
//                    reportNames.add(((SpReport )repObj).getName());
//                }
//            if (reports.size() == 0)
//            {
//                return null;
//            }
//            if (reports.size() == 1)
//            {
//                SpReport result = (SpReport )((Object[] )reports.get(0))[0];
//                result.forceLoad();
//                return result;
//            }
//
//            Vector<SpReport> reps = new Vector<SpReport>(reports.size());
//            for (Object rep : reports)
//            {
//                reps.add((SpReport )((Object[] )rep)[0]);
//            }
            
//            dlg = new ChooseFromListDlg<SpAppResource>((Frame) UIRegistry
//                    .getTopWindow(), getResourceString("REP_CHOOSE_SP_REPORT"),
//                    aprs);
//            dlg.setVisible(true);
//            if (dlg.isCancelled()) 
//            { 
//                return null; 
//            }
//            return dlg.getSelectedObject();
//            result.forceLoad();
//            return result;
        }
        finally
        {
            session.close();
            if (dlg != null)
            {
                dlg.dispose();
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
                        JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow() != null ? UIRegistry.getMostRecentWindow() : UIRegistry.getTopWindow(),
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
                        UIRegistry.displayLocalizedStatusBarText("SENDING_EMAIL");
                        
                        String password = Encryption.decrypt(emailPrefs.get("password"));
                        if (StringUtils.isEmpty(password))
                        {
                            password = EMailHelper.askForPassword(topFrame);
                        }
                        
                        if (StringUtils.isNotEmpty(password))
                        {
                            final EMailHelper.ErrorType status = EMailHelper.sendMsg(emailPrefs.get("smtp"), 
                                                                                       emailPrefs.get("username"), 
                                                                                       password, 
                                                                                       emailPrefs.get("email"), 
                                                                                       emailPrefs.get("to"), 
                                                                                       emailPrefs.get("subject"), 
                                                                                       text, 
                                                                                       EMailHelper.HTML_TEXT, 
                                                                                       emailPrefs.get("port"), 
                                                                                       emailPrefs.get("security"), 
                                                                                       excelFile);
                            if (status != EMailHelper.ErrorType.Cancel)
                            {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run()
                                    {
                                        UIRegistry.displayLocalizedStatusBarText(status == EMailHelper.ErrorType.Error ? "EMAIL_SENT_ERROR" : "EMAIL_SENT_OK");
                                    }
                                });
                            }
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
     * Delete a InfoRequest.
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);
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
     * @param agent the agent doing the return
     * @param returnedDate the date it was returned
     * @param returns the list of items being returned
     * @param doingSingleItem whether it is a single item
     */
    protected void doReturnLoans(final MultiView            multiView,
                                 final Agent                agent, 
                                 final Calendar             returnedDate, 
                                 final List<LoanReturnInfo> returns,
                                 final boolean              doingSingleItem)
    {
        final JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setIndeterminate(INTERACTIONS, true);
        
        String msg = getResourceString("InteractionsTask.ReturningLoanItems");
        statusBar.setText(msg);
        UIRegistry.writeSimpleGlassPaneMsg(msg, 24);
        
        final SwingWorker worker = new SwingWorker()
        {
            protected int numLPR = 0;
            protected HashMap<Integer, Loan> mergedLoans = new HashMap<Integer, Loan>();
            
            @Override
            public Object construct()
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    session.beginTransaction();
                    
                    for (LoanReturnInfo loanRetInfo : returns)
                    { 
                        LoanPreparation loanPrep = loanRetInfo.getLoanPreparation();
                        Loan            loan     = mergedLoans.get(loanPrep.getLoan().getId()); // get already merged loan
                        
                        if (loan == null)
                        {
                            loan = session.merge(loanPrep.getLoan());
                            mergedLoans.put(loan.getId(), loan);
                        }
                        
                        // Find the LoanPrep in the Merged Loan Object
                        for (LoanPreparation lp : loan.getLoanPreparations())
                        {
                            if (loanPrep.getId().equals(lp.getId()))
                            {
                                loanPrep = lp;
                            }
                        }
                        
                        // The loanRetInfo contains the total number of Resolved and Returned
                        // so we need to go get the number already resolved/returned and subtract it
                        // to get the remaining difference for this last LoanReturnPrep
                        LoanReturnPreparation loanRetPrep = new LoanReturnPreparation();
                        loanRetPrep.initialize();
                        loanRetPrep.setReceivedBy(agent);
                        loanRetPrep.setModifiedByAgent(Agent.getUserAgent());
                        loanRetPrep.setReturnedDate(returnedDate);
                        loanRetPrep.setQuantityResolved(loanRetInfo.getResolvedQty());
                        loanRetPrep.setQuantityReturned(loanRetInfo.getReturnedQty());
                        
                        Integer lpReturned = loanPrep.getQuantityReturned() != null ? loanPrep.getQuantityReturned() : 0;
                        Integer lpResolved = loanPrep.getQuantityResolved() != null ? loanPrep.getQuantityResolved() : 0;
                        loanPrep.setQuantityResolved(lpResolved + loanRetInfo.getResolvedQty());
                        loanPrep.setQuantityReturned(lpReturned + loanRetInfo.getReturnedQty());
                        loanPrep.setIsResolved(loanPrep.getQuantityResolved() == loanPrep.getQuantity());
                        loanRetPrep.setRemarks(loanRetInfo.getRemarks());
                        loanPrep.setTimestampModified(new Timestamp(System.currentTimeMillis()));

                        loanPrep.addReference(loanRetPrep, "loanReturnPreparations");
                        
                        if (doingSingleItem)
                        {
                            boolean isClosed = true;
                            for (LoanPreparation lp : loan.getLoanPreparations())
                            {
                                if (lp.getQuantityResolved().equals(lp.getQuantity()))
                                {
                                    if (!lp.getIsResolved())
                                    {
                                        lp.setIsResolved(true);
                                    }
                                } else
                                {
                                    isClosed = false;
                                }
                            }
                            loan.setIsClosed(isClosed);
                            
                        } else
                        {
                            loan.setIsClosed(true);
                        }
                        loan.setTimestampModified(new Timestamp(System.currentTimeMillis()));
                        session.save(loanRetPrep);
                        session.saveOrUpdate(loanPrep);
                        session.saveOrUpdate(loan);
                    }
                    
                    session.commit();
                    
                    numLPR += returns.size();
                    
                } catch (Exception ex)
                {
                    // Error Dialog
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);
                    
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
            @Override
            public void finished() {
                statusBar.setProgressDone(INTERACTIONS);
                statusBar.setText("");
                if (multiView != null) {
                    multiView.getCurrentViewAsFormViewObj().reloadDataObj(true);
                }
                UIRegistry.clearSimpleGlassPaneMsg();
                UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "InteractionsTask.LN_RET_TITLE", "InteractionsTask.RET_LN_SV", numLPR);
            }
        };
        worker.start();
    }
    /**
     * Asks where the source of the Loan Preps should come from.
     * @return the source enum
     */
    protected ASK_TYPE askSourceOfPrepsForLoanReturn()
    {
        Object[] options = {
                getResourceString("InteractionsTask.LOAN_RET_RS_FILTER"),
                getResourceString("InteractionsTask.LOAN_RET_ID_FILTER"),
                getResourceString("InteractionsTask.LOAN_RET_NO_FILTER")
        };

        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(),
                getResourceString("InteractionsTask.LOAN_RET_FILTER_DLG_MSG"),
                getResourceString("InteractionsTask.LOAN_RET_FILTER_DLG_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (userChoice == 1) {
            return ASK_TYPE.EnterDataObjs;
        } else if (userChoice == 0) {
            return ASK_TYPE.ChooseRS;
        } else if (userChoice == 2) {
            return ASK_TYPE.None;
        }

        return ASK_TYPE.Cancel;
    }

    /**
     * Starts process to return a loan
     * @param doPartial true means show dialog and do partial, false means just return the loan
     */
    protected void returnLoan() {
        Loan loan = null;
        MultiView mv = null;
        SubPaneIFace subPane = SubPaneMgr.getInstance().getCurrentSubPane();
        if (subPane != null) {
            mv = subPane.getMultiView();
            if (mv != null) {
                if (mv.getData() instanceof Loan) {
                    loan = (Loan) mv.getData();
                }
            }
        }
        if (mv != null && loan != null) {
            if (mv.getCurrentViewAsFormViewObj().getSaveComponent().isEnabled()) {
                displayInfoMsgDlg(getResourceString("InteractionsTask.RET_LOAN_NOT_AVAILABLE_UNSAVED_CHANGES"));
                return;
            }
            ASK_TYPE filter = askSourceOfPrepsForLoanReturn();
            if (filter == ASK_TYPE.Cancel) {
                return;
            }
            Integer defSrcTblId = AppPreferences.getRemote().getInt(DEFAULT_SRC_TBL_ID, 0);
            if (defSrcTblId == 0 && (filter == ASK_TYPE.EnterDataObjs || filter == ASK_TYPE.None)) {
                defSrcTblId = InteractionsProcessor.promptForItemIdTableId();
            }
            RecordSetIFace recordSet = null;
            if (defSrcTblId != null && (defSrcTblId != 0 || filter == ASK_TYPE.ChooseRS || filter == ASK_TYPE.None)) {
                if (filter == ASK_TYPE.EnterDataObjs) {
                    recordSet = ((InteractionsTask) subPane.getTask()).askForDataObjRecordSet(defSrcTblId == CollectionObject.getClassTableId() ? CollectionObject.class : Preparation.class,
                            getInteractionItemLookupField(defSrcTblId), false);
                } else if (filter == ASK_TYPE.ChooseRS) {
                    Vector<Integer> tblIds = new Vector<>();
                    if (defSrcTblId == 0) {
                        tblIds.add(CollectionObject.getClassTableId());
                        tblIds.add(Preparation.getClassTableId());
                    } else {
                        tblIds.add(defSrcTblId);
                    }
                    recordSet = RecordSetTask.askForRecordSet(tblIds, null, true);
                }
                LoanReturnDlg dlg = new LoanReturnDlg(loan, recordSet, defSrcTblId);
                if (dlg.createUI()) {
                    dlg.setModal(true);
                    UIHelper.centerAndShow(dlg);
                    dlg.dispose();
                    if (!dlg.isCancelled()) {
                        FormViewObj fvp = mv.getCurrentViewAsFormViewObj();
                        fvp.setHasNewData(true); //setting to false might eliminate need for changes in formViewObj for #897??
                        // 03/04/09 Commented out the two lines below so the form doesn't get enabled for saving.
                        //fvp.getValidator().setHasChanged(true);
                        //fvp.validationWasOK(fvp.getValidator().getState() == UIValidatable.ErrorType.Valid);
                        List<LoanReturnInfo> returns = dlg.getLoanReturnInfo();
                        if (returns.size() > 0) {
                            doReturnLoans(mv, dlg.getAgent(), dlg.getDate(), returns, true);
                        }
                    }
                }
            }

        } else {
            // XXX Show some kind of error dialog
        }
    }
    
    /**
     * 
     */
    protected void createLoanNoPreps(final Viewable srcViewable)
    {
        Loan loan = new Loan();
        loan.initialize();
        
        Calendar dueDate = Calendar.getInstance();
        dueDate.add(Calendar.MONTH, 6);                 // XXX PREF Due Date
        loan.setCurrentDueDate(dueDate);
        
        Shipment shipment = new Shipment();
        shipment.initialize();
        
        loan.addReference(shipment, "shipments");
        
        if (srcViewable != null)
        {
            srcViewable.setNewObject(loan);
            
        } else
        {
            DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
            if (dataEntryTask != null)
            {
                DBTableInfo loanTableInfo = DBTableIdMgr.getInstance().getInfoById(loan.getTableId());
                FormPane formPane = dataEntryTask.openView(this, null, loanTableInfo.getDefaultFormName(), "edit", loan, true);
                if (formPane != null)
                {
                    MultiView mv = formPane.getMultiView();
                    if (mv != null)
                    {
                        FormViewObj fvo = mv.getCurrentViewAsFormViewObj();
                        if (fvo != null && fvo.getBusinessRules() != null && fvo.getBusinessRules() instanceof LoanBusRules)
                        {
                            ((LoanBusRules)fvo.getBusinessRules()).setDoCreateLoanNoPreps(true);
                        }
                    }
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
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
        AppPreferences remotePrefs = (AppPreferences) cmdAction.getData();
        if (remotePrefs == AppPreferences.getRemote())
        {
            String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
            String prefName = IS_USING_INTERACTIONS_PREFNAME + ds;
            reAddToolBarItem(cmdAction, toolBarBtn, prefName);
        }
    }
    
    /**
     * Processes all Commands of type RECORD_SET.
     * @param cmdAction the command to be processed
     */
    protected void processRecordSetCommands(final CommandAction cmdAction)
    {
        Object data = cmdAction.getData();
        UsageTracker.incrUsageCount("IN."+cmdAction.getType()+(data != null ? ("."+data.getClass().getSimpleName()) : ""));

        if (cmdAction.isAction("Clicked"))
        {
            Object srcObj = cmdAction.getSrcObj();
            Object dstObj = cmdAction.getDstObj();
            
            log.debug("********* In Labels doCommand src["+srcObj+"] dst["+dstObj+"] data["+data+"] context["+ContextMgr.getCurrentContext()+"]");
             
            if (ContextMgr.getCurrentContext() == this)
            {
                if (dstObj instanceof RecordSetIFace)
                {
                    RecordSetIFace rs = (RecordSetIFace)dstObj;
                    if (isInteractionTable(rs.getDbTableId()))
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
            // NOTE: An Update message is sent right after an Insert.
            // So we can ignore the Insert
            System.out.println(cmdAction);
            if (cmdAction.isAction(UPDATE_CMD_ACT))
            {
                // Create Specify Application
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        CommandDispatcher.dispatch(new CommandAction(INTERACTIONS, CREATE_MAILMSG, SubPaneMgr.getInstance().getCurrentSubPane()));
                    }
                });
                
            } else if (cmdAction.isAction(INSERT_CMD_ACT))
            {
                InfoRequest infoRequest = (InfoRequest)cmdAction.getData();
                createNavBtn(infoRequestNavBox, INFOREQUEST_FLAVOR, infoRequest, DBTableIdMgr.getInstance().getInfoById(InfoRequest.getClassTableId()));
            }
        }
    }
    
    /**
     * Loads a InfoRequest into a form
     * @param cmdAction the command action containing the InfoRequest
     */
    private void showInfoReqForm(final CommandActionForDB cmdAction, 
                                 final RecordSetIFace irRS)
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
                infoReq = session.get(InfoRequest.class, cmdAction != null ? cmdAction.getId() : irRS.getItems().iterator().next().getRecordId());
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);
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
                String.format(getResourceString("InteractionsTask.CONFIRM_DELETE_IR"), nb.getName()),
                getResourceString("InteractionsTask.CONFIRM_DELETE_TITLE_IR"), 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION); // I18N
        
        if (option == JOptionPane.YES_OPTION)
        {
            InfoRequest infoRequest = deleteInfoRequest(cmdActionDB.getId());
            deleteInfoRequestFromUI(null, infoRequest);
        }
    }
    
    /**
     * @param dataObj
     */
    private void returnLoan(final RecordSetIFace dataObj)
    {
        RecordSetIFace recordSet = null;
        if (dataObj instanceof RecordSetIFace)
        {
            recordSet = dataObj;
            
        } else if (dataObj == null)
        {
            RecordSetTask            rsTask     = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);
            Vector<RecordSetIFace>   loanRSList = new Vector<RecordSetIFace>(rsTask.getRecordSets(Loan.getClassTableId()));
            
            recordSet = getRecordSetOfDataObjs(null, Loan.class, "loanNumber", loanRSList.size());
        }
        
        if (recordSet == null)
        {
            return;
        }
        
        List<LoanReturnInfo> lriList = new ArrayList<LoanReturnInfo>();
        
        if (recordSet.getDbTableId() == Loan.getClassTableId())
        {
            DataProviderSessionIFace session = null;
            try
            {
                HashSet<Integer> loanHashMap = new HashSet<Integer>();
                
                session = DataProviderFactory.getInstance().createSession();
                
                for (RecordSetItemIFace rsi : recordSet.getItems())
                {
                    if (!loanHashMap.contains(rsi.getRecordId()))
                    {
                        Loan loan = session.get(Loan.class, rsi.getRecordId());
                        if (loan != null)
                        {
                            loanHashMap.add(rsi.getRecordId());
                            
                            if (!loan.getIsClosed())
                            {
                                for (LoanPreparation lp : loan.getLoanPreparations())
                                {
                                    if (!lp.getIsResolved())
                                    {
                                        // Returned items are always resolved.
                                        // but resolved items are not always returned.
                                        int qty         = lp.getQuantity();
                                        int qtyResolved = lp.getQuantityResolved();
                                        int qtyReturned = lp.getQuantityReturned();
                                        
                                        int qtyToBeReturned = qty - qtyResolved;
                                        qtyResolved += qtyToBeReturned;
                                        qtyReturned += qtyToBeReturned;
                                        
                                        lriList.add(new LoanReturnInfo(lp, null, qtyToBeReturned, qtyResolved, true));
                                    }
                                }
                            }
                        }
                    }
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
        
        if (lriList.size() > 0)
        {
            Agent currAgent = AppContextMgr.getInstance().getClassObject(Agent.class);
            doReturnLoans(null, currAgent, Calendar.getInstance(), lriList, false);
        }

    }
    
    /**
     * Processes all Commands of type INTERACTIONS.
     * @param cmdAction the command to be processed
     */
    protected void processInteractionsCommands(final CommandAction cmdAction) {
        boolean isBasicNewInteraction = false;
        DBTableInfo tblInfo = null;
        InteractionsProcessor<?> processor = null;
        if (cmdAction.isAction(NEW_ACCESSION)) {
            tblInfo = DBTableIdMgr.getInstance().getInfoById(Accession.getClassTableId());
            isBasicNewInteraction = true;
            processor = accProcessor;
        } else if (cmdAction.isAction(NEW_DEACC)) {
            tblInfo = DBTableIdMgr.getInstance().getInfoById(Deaccession.getClassTableId());
            isBasicNewInteraction = true;
            processor = legalDeaccProcessor;
        } else if (cmdAction.isAction(NEW_DISPOSAL)) {
            tblInfo = DBTableIdMgr.getInstance().getInfoById(Disposal.getClassTableId());
            isBasicNewInteraction = true;
            processor = disposalProcessor;
        } else if (cmdAction.isAction(NEW_LOAN)) {
            tblInfo = DBTableIdMgr.getInstance().getInfoById(Loan.getClassTableId());
            isBasicNewInteraction = true;
            processor = loanProcessor;
        } else if (cmdAction.isAction(LN_NO_PRP)) {
            tblInfo = DBTableIdMgr.getInstance().getInfoById(Loan.getClassTableId());
            processor = loanProcessor;
        } else if (cmdAction.isAction(NEW_GIFT)) {
            tblInfo = DBTableIdMgr.getInstance().getInfoById(Gift.getClassTableId());
            isBasicNewInteraction = true;
            processor = giftProcessor;
        } else if (cmdAction.isAction(INFO_REQ_MESSAGE)) {
            tblInfo = DBTableIdMgr.getInstance().getInfoById(InfoRequest.getClassTableId());
        } else if (cmdAction.isAction(NEW_EXCHANGE_OUT)) {
            tblInfo = DBTableIdMgr.getInstance().getInfoById(ExchangeOut.getClassTableId());
            isBasicNewInteraction = true;
            processor = exchProcessor;
        }
        boolean isOKToAdd;
        if (AppContextMgr.isSecurityOn() && tblInfo != null) {
            PermissionSettings perms = tblInfo.getPermissions();
            isOKToAdd = perms == null || perms.canAdd();
        } else {
            isOKToAdd = tblInfo != null;
        }

        UsageTracker.incrUsageCount("IN." + cmdAction.getType());

        if (cmdAction.isAction(CREATE_MAILMSG)) {
            createAndSendEMail();

        } else if (cmdAction.isAction(PRINT_INVOICE)) {
            if (cmdAction.getData() instanceof RecordSetIFace) {
                printInvoice(null, cmdAction.getData());

            }
            if (cmdAction.getData() instanceof CommandAction) {
                RecordSetIFace recordSet = RecordSetTask.askForRecordSet(this.printableInvoiceTblIds, null, true);
                if (recordSet != null) {
                    printInvoice(cmdAction.getPropertyAsString("file"), recordSet);
                }
            }

        } else if (isBasicNewInteraction) {
            if (cmdAction.getData() == cmdAction) {
                if (isOKToAdd) {
                    processor.createOrAdd();
                }

            } else if (cmdAction.getData() instanceof RecordSetIFace) {
                RecordSetIFace rs = (RecordSetIFace) cmdAction.getData();
                if (rs.getDbTableId() == colObjTableId || rs.getDbTableId() == infoRequestTableId || rs.getDbTableId() == preparationTableId) {
                    if (isOKToAdd) {
                        processor.createOrAdd(rs);
                    }
                } else {
                    log.error("Dropped wrong table type."); // this shouldn't happen
                }

            } else if (cmdAction.getData() instanceof Viewable) {
                if (isBasicNewInteraction) {
                    processor.createOrAdd((Viewable) cmdAction.getData());
                } else if (tblInfo != null && tblInfo.getName().equalsIgnoreCase("loan"))
                    createLoanNoPreps((Viewable) cmdAction.getData());

            }
        } else if (cmdAction.getData() instanceof InfoRequest) {
            if (isOKToAdd) {
                processor.createFromInfoRequest((InfoRequest) cmdAction.getData());
            }
        } else if (cmdAction.getData() instanceof CommandActionForDB) {
            if (isOKToAdd) {
                CommandActionForDB cmdActionDB = (CommandActionForDB) cmdAction.getData();
                RecordSetIFace rs = RecordSetFactory.getInstance().createRecordSet("", cmdActionDB.getTableId(), RecordSet.GLOBAL);
                rs.addItem(cmdActionDB.getId());
                if (isBasicNewInteraction && processor != null) {
                    processor.createOrAdd(rs);
                }
            }
        } else if (cmdAction.isAction(ADD_TO_LOAN)) {
            loanProcessor.createOrAdd((Loan) cmdAction.getData());
        } else if (cmdAction.isAction(ADD_TO_GIFT)) {
            giftProcessor.createOrAdd((Gift) cmdAction.getData());
        } else if (cmdAction.isAction(ADD_TO_ACCESSION)) {
            accProcessor.createOrAdd((Accession) cmdAction.getData());
        } else if (cmdAction.isAction(ADD_TO_DISPOSAL)) {
            disposalProcessor.createOrAdd((Disposal) cmdAction.getData());
        } else if (cmdAction.isAction(ADD_TO_EXCHANGE)) {
            exchProcessor.createOrAdd((ExchangeOut) cmdAction.getData());
        }else if (cmdAction.isAction(INFO_REQ_MESSAGE)) {
            if (cmdAction.getData() == cmdAction || cmdAction.getData() instanceof Viewable) {
                // We get here when a user clicks on a InfoRequest NB action
                createInfoRequest(null, cmdAction);
            } else if (cmdAction.getData() instanceof RecordSetIFace) {
                // We get here when a RecordSet is dropped on an InfoRequest
                Object data = cmdAction.getData();
                if (data instanceof RecordSetIFace) {
                    RecordSetIFace rs = (RecordSetIFace) data;
                    if (rs.getDbTableId() == infoRequestTableId) {
                        showInfoReqForm(null, rs);
                    } else if (rs.getDbTableId() == CollectionObject.getClassTableId()) {
                        createInfoRequest((RecordSetIFace) data, cmdAction);
                    }
                }
            } else if (cmdAction.getData() instanceof CommandActionForDB) {
                // We get here when a InfoRequest is dropped on an InfoRequest NB action
                showInfoReqForm((CommandActionForDB) cmdAction.getData(), null);
            }
        } else if (cmdAction.isAction("ReturnLoan")) {// from the 'Return Loan' button on the form
            returnLoan();
        } else if (cmdAction.isAction("RET_LOAN")) {// from the sidebar
            if (cmdAction.getData() instanceof RecordSetIFace) {
                returnLoan((RecordSetIFace) cmdAction.getData());
            } else if (cmdAction.getData() == cmdAction) {
                returnLoan(null);
            }
        } else if (cmdAction.isAction(DELETE_CMD_ACT)) {
            if (cmdAction instanceof CommandActionForDB) {
                deleteInfoRequest((CommandActionForDB) cmdAction);
            }
        } else if (cmdAction.isAction(OPEN_NEW_VIEW) || cmdAction.isAction(DataEntryTask.EDIT_DATA)) {
            try {
                final CommandAction cachedCmdAction = (CommandAction) cmdAction.clone();

                cmdAction.setType("Data_Entry");
                cmdAction.setProperty(NavBoxAction.ORGINATING_TASK, this);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        CommandDispatcher.dispatch(cmdAction);
                        cmdAction.set(cachedCmdAction);
                    }
                });
            } catch (CloneNotSupportedException ex) {
                //ignore
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
            super.doCommand(cmdAction);
        
        if (cmdAction.isConsumed())
        {
            return;
        }
        
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
    
    
    
    /**
     * @return the entries
     */
    public Vector<InteractionEntry> getEntries()
    {
        return entries;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#canRequestContext()
     */
    @Override
    protected boolean canRequestContext()
    {
        return Uploader.checkUploadLock(this) == Uploader.NO_LOCK;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        BasicPermisionPanel editor = new BasicPermisionPanel();
        editor.setAssociatedTableIds(new int[] {52});
        return editor;
    }

    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, false, false},
                                {true, false, false, false}};
    }

    /**
     *
     * @param countQuantity
     * @param countUnresolved
     * @param interactionID
     * @param tblId
     * @return
     */
    public static String getCountContentsSql(boolean countQuantity, boolean countUnresolved, Integer interactionID, Integer tblId) {
        if (tblId.equals(Loan.getClassTableId())) {
            String select = countQuantity ? " sum(quantity" + (countUnresolved ? "-ifnull(quantityresolved,0)" : "") + ")"
                    : " count(*) ";
            String sql = "select " + select + " from loanpreparation where loanid = " + interactionID;
            if (countUnresolved) {
                sql += " and (not isresolved";
                if (countQuantity) {
                    sql += " or ifnull(quantity,0) - ifnull(quantityresolved,0) > 0";
                }
                sql += ")";
            }
            return sql;
        } else {
            String tbl =  DBTableIdMgr.getInstance().getInfoById(tblId).getName();
            String idCol = DBTableIdMgr.getInstance().getInfoById(tblId).getIdColumnName(); //assuming foreign key naming pattern
            String prepTbl = tbl + (tblId.equals(ExchangeOut.getClassTableId()) ? "prep" : "preparation");
            String select = countQuantity ? " sum(quantity" +  ")" : " count(*) ";
            String sql = "select " + select + " from " + prepTbl +  " where " + idCol + " = " + interactionID;
            return sql;
        }
    }
}
