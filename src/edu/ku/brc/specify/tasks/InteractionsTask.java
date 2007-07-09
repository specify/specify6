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
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskCommandDef;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.dbsupport.TableModel2Excel;
import edu.ku.brc.helpers.EMailHelper;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPhysicalObject;
import edu.ku.brc.specify.datamodel.LoanReturnPhysicalObject;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.specify.ui.LoanReturnDlg;
import edu.ku.brc.specify.ui.LoanSelectPrepsDlg;
import edu.ku.brc.specify.ui.LoanReturnDlg.LoanReturnInfo;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.PickListItemIFace;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.dnd.Trash;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.TableViewObj;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.validation.UIValidatable;
import edu.ku.brc.ui.validation.ValFormattedTextField;


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

    public static final String             INTERACTIONS        = "Interactions";
    public static final DataFlavorTableExt INTERACTIONS_FLAVOR = new DataFlavorTableExt(DataEntryTask.class, INTERACTIONS);
    public static final DataFlavorTableExt INFOREQUEST_FLAVOR  = new DataFlavorTableExt(InfoRequest.class, "InfoRequest");

    protected static final String InfoRequestName      = "InfoRequest";
    protected static final String NEW_LOAN             = "New_Loan";
    protected static final String PRINT_LOAN           = "PrintLoan";
    protected static final String INFO_REQ_MESSAGE     = "Specify Info Request";
    protected static final String CREATE_MAILMSG       = "CreateMailMsg";
    
    protected static final int    loanTableId;
    protected static final int    infoRequestTableId;
    protected static final int    colObjTableId;

    // Data Members
    protected NavBox                  infoRequestNavBox;
    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    protected Vector<NavBoxItemIFace> invoiceList      = new Vector<NavBoxItemIFace>();
    
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
        CommandDispatcher.register(APP_CMD_TYPE, this);
        CommandDispatcher.register(DB_CMD_TYPE, this);
        CommandDispatcher.register(DataEntryTask.DATA_ENTRY, this);
        

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            extendedNavBoxes.clear();
            invoiceList.clear();

            // Temporary
            NavBox navBox = new NavBox(getResourceString("Actions"));
            
            // New Loan Action
            // A New loan can accept RecordSets that contain CollectionObjects or InfoRequests
            // or InfoRequests
            CommandAction cmdAction = new CommandAction(INTERACTIONS, NEW_LOAN);
            NavBoxButton roc = (NavBoxButton)makeDnDNavBtn(navBox, getResourceString(NEW_LOAN), name, cmdAction, null, true, false);// true means make it draggable
            roc.addDropDataFlavor(InfoRequestTask.INFOREQUEST_FLAVOR);
            
            DataFlavorTableExt dfx = new DataFlavorTableExt(RecordSetTask.RECORDSET_FLAVOR.getDefaultRepresentationClass(), RecordSetTask.RECORDSET_FLAVOR.getHumanPresentableName(), new int[] {1, 50});
            roc.addDropDataFlavor(dfx);
            
            // Misc Action (does nothing at the moment XXX IMPLEMENT ME!)
            navBox.add(NavBox.createBtn(getResourceString("New_Gifts"), "Loan", IconManager.IconSize.Std16));
            navBox.add(NavBox.createBtn(getResourceString("New_Exchange"), "Loan", IconManager.IconSize.Std16));
            
            // InfoRequest Action
            cmdAction = new CommandAction(INTERACTIONS, InfoRequestName);
            roc = (NavBoxButton)makeDnDNavBtn(navBox, getResourceString(InfoRequestName), InfoRequestName, cmdAction, null, true, false);// true means make it draggable
            dfx = new DataFlavorTableExt(RecordSetTask.RECORDSET_FLAVOR.getDefaultRepresentationClass(), RecordSetTask.RECORDSET_FLAVOR.getHumanPresentableName(), 1);
            roc.addDropDataFlavor(dfx);
            
            // These need to be loaded as Resources
            //navBox = new NavBox(getResourceString(ReportsTask.REPORTS));
            //navBox.add(NavBox.createBtn(getResourceString("All_Overdue_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
            //navBox.add(NavBox.createBtn(getResourceString("All_Open_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
            //navBox.add(NavBox.createBtn(getResourceString("All_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
            //addToNavBoxAndRegisterAsDroppable(navBox, NavBox.createBtn(getResourceString(PRINT_LOAN),  ReportsTask.REPORTS, IconManager.IconSize.Std16, new NavBoxAction(INTERACTIONS, PRINT_LOAN, this)), null);
            //navBoxes.addElement(navBox);
            
            // Then add
            if (commands != null)
            {
                for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType("jrxml/report"))
                {
                    Map<String, String> params = ap.getMetaDataMap();
                    params.put("title", ap.getDescription());
                    params.put("file", ap.getName());
                    //log.info("["+ap.getDescription()+"]["+ap.getName()+"]");
                    
                    commands.add(new TaskCommandDef(ap.getDescription(), name, params));
                }
                
                for (TaskCommandDef tcd : commands)
                {
                    // XXX won't be needed when we start validating the XML
                    String tableIdStr = tcd.getParams().get("tableid");
                    if (tableIdStr != null)
                    {
                        //addToNavBoxAndRegisterAsDroppable(navBox, NavBox.createBtn(tcd.getName(), "Loan", IconManager.IconSize.Std16, new NavBoxAction(tcd, this)), tcd.getParams());
                        
                        cmdAction = new CommandAction(INTERACTIONS, PRINT_LOAN, Loan.getClassTableId());
                        cmdAction.addStringProperties(tcd.getParams());
                        NavBoxItemIFace nbi = makeDnDNavBtn(navBox, tcd.getName(), "Loan", cmdAction, null, true, false);
                        roc = (NavBoxButton)nbi;
                        invoiceList.add(nbi);// true means make it draggable
                        //setUpDraggable(nbi, new DataFlavor[]{Trash.TRASH_FLAVOR, INFOREQUEST_FLAVOR}, new NavBoxAction("", ""));
                        roc.addDragDataFlavor(INFOREQUEST_FLAVOR);
                        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
                        
                    } else
                    {
                        log.error("Interaction Command is missing the table id");
                    }
                }
            }
            navBoxes.addElement(navBox);
            
            // Load InfoRequests into NavBox
            infoRequestNavBox  = new NavBox(getResourceString("InfoRequest"));
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            
            List infoRequests = session.getDataList(InfoRequest.class);
            for (Iterator iter=infoRequests.iterator();iter.hasNext();)
            {
                InfoRequest infoRequest = (InfoRequest)iter.next();
                
                //roc = (NavBoxButton) addNavBoxItem(infoRequestNavBox, infoRequest.getIdentityTitle(), "InfoRequest", new CommandAction(INTERACTIONS, DELETE_CMD_ACT, infoRequest), infoRequest);
                NavBoxItemIFace nbi = makeDnDNavBtn(infoRequestNavBox, infoRequest.getIdentityTitle(), "InfoRequest", new CommandAction(INTERACTIONS, DELETE_CMD_ACT, infoRequest), null, true, true);
                roc = (NavBoxButton)nbi;
                nbi.setData(infoRequest);
                roc.addDragDataFlavor(INFOREQUEST_FLAVOR);
                roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
                
                //setUpDraggable(nbi, new DataFlavor[]{Trash.TRASH_FLAVOR, INFOREQUEST_FLAVOR}, new NavBoxAction("", ""));
                
            }      
            navBoxes.addElement(infoRequestNavBox);
            session.close();
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
                                                                final Map<String, String> params)
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
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);

        extendedNavBoxes.addAll(rsTask.getNavBoxes());

        return extendedNavBoxes;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return starterPane = new SimpleDescPane(title, this, "Please select an Interaction");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        String label = getResourceString(name);
        String iconName = name;
        String hint = getResourceString("interactions_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);

        list.add(new ToolBarItemDesc(btn));

        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        return list;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    /**
     * Creates a new loan from a RecordSet.
     * @param fileNameArg the filename of the report (Invoice) to use (can be null)
     * @param recordSet the recordset to use to create the loan
     */
    protected void printLoan(final String fileNameArg, final Object data)
    {
        if (data instanceof RecordSetIFace)
        {
            RecordSetIFace rs = (RecordSetIFace)data;
            
            String fileName = fileNameArg;
            if (fileName == null)
            {
                if (invoiceList.size() == 0)
                {
                    // XXX Need Error Dialog that there are no Invoices (can this happen?)
                    
                } else if (invoiceList.size() > 1) // only Count the ones that require data
                {
                    fileName = askForInvoiceName();
                    
                } else  
                {
                    NavBoxItemIFace nbi = invoiceList.get(0);
                    Object nbData = nbi.getData();
                    if (nbData instanceof CommandAction)
                    {
                        fileName = ((CommandAction)nbData).getPropertyAsString("file");
                    }
                }
            }

            if (fileName != null)
            {
                // XXX For Demo purposes only we need to be able to look up report and labels
                final CommandAction cmd = new CommandAction(LabelsTask.LABELS, LabelsTask.PRINT_LABEL, rs);
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
     * Creates a new loan from a InfoRequest.
     * @param infoRequest the infoRequest to use to create the loan
     */
    protected void createNewLoan(final InfoRequest infoRequest)
    {   
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(infoRequest);
        RecordSetIFace rs = infoRequest.getRecordSet();
        session.close();   
        createNewLoan(infoRequest, rs);
    }
    
    /**
     * Creates a new loan from a RecordSet.
     * @param recordSet the recordset to use to create the loan
     */
    @SuppressWarnings("unchecked")
    protected void createNewLoan(final InfoRequest infoRequest, final RecordSetIFace recordSet)
    {      
        DBTableIdMgr.getInClause(recordSet);

        DBTableIdMgr.TableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
        
        DataProviderFactory.getInstance().evict(tableInfo.getClassObj()); // XXX Not sure if this is really needed
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        // OK, it COULD be a RecordSet contain one or more InfoRequest, 
        // we will only accept an RS with one InfoRequest
        if (infoRequest == null && recordSet.getDbTableId() == infoRequestTableId)
        {
            try
            {
                if (recordSet.getItems().size() == 1)
                {
                    RecordSetItemIFace item = recordSet.getOnlyItem();
                    if (item != null)
                    {
                        InfoRequest infoReq = session.get(InfoRequest.class, item.getRecordId().longValue());
                        if (infoReq != null)
                        {
                            createNewLoan(infoReq, infoReq.getRecordSet());
                            
                        } else
                        {
                            // error about missing info request
                        }
                    } else
                    {
                        // error about item being null for some unbelievable reason 
                    }
                } else 
                {
                    // error about item having more than one or none
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
            return;
        }
        
        // OK, here we have a recordset of CollectionObjects
        // First we process all the CollectionObjects in the RecordSet
        // and create a list of Preparations that can be loaned
        String sqlStr = DBTableIdMgr.getInstance().getQueryForTable(recordSet);
        if (StringUtils.isNotBlank(sqlStr))
        {
            final LoanSelectPrepsDlg loanSelectPrepsDlg = new LoanSelectPrepsDlg((List<CollectionObject>)session.getDataList(sqlStr));
            loanSelectPrepsDlg.setModal(true);
            
            UIHelper.centerAndShow(loanSelectPrepsDlg);

            final Taskable thisTask = this;
            final Hashtable<Preparation, Integer> prepsHash = loanSelectPrepsDlg.getPreparationCounts();
            if (prepsHash.size() > 0)
            {
                final SwingWorker worker = new SwingWorker()
                {
                    public Object construct()
                    {
                        JStatusBar statusBar = UIRegistry.getStatusBar();
                        statusBar.setIndeterminate(true);
                        statusBar.setText(getResourceString("CreatingLoan"));
                        
                        Loan loan = new Loan();
                        loan.initialize();
                        
                        Calendar dueDate = Calendar.getInstance();
                        dueDate.add(Calendar.MONTH, 6);                 // XXX PREF Due Date
                        loan.setCurrentDueDate(dueDate);
                        
                        Shipment shipment = new Shipment();
                        shipment.initialize();
                        
                        // Get Defaults for Certain fields
                        SpecifyAppContextMgr appContextMgr     = (SpecifyAppContextMgr)AppContextMgr.getInstance();
                        PickListItemIFace    defShipmentMethod = appContextMgr.getDefaultPickListItem("ShipmentMethod", getResourceString("SHIPMENT_METHOD"));
                        shipment.setShipmentMethod(defShipmentMethod.getValue());
                        
                        FormDataObjIFace shippedBy = appContextMgr.getDefaultObject(Agent.class, "ShippedBy", getResourceString("SHIPPED_BY"), true, false);
                        shipment.setShippedBy((Agent)shippedBy);
                        
                        if (infoRequest != null && infoRequest.getAgent() != null)
                        {
                            shipment.setShippedTo(infoRequest.getAgent());
                        }
                        
                        loan.addShipment(shipment);
                        
                        for (Preparation prep : prepsHash.keySet())
                        {
                            Integer count = prepsHash.get(prep);
                            
                            LoanPhysicalObject lpo = new LoanPhysicalObject();
                            lpo.initialize();
                            lpo.setPreparation(prep);
                            lpo.setQuantity(count);
                            lpo.setLoan(loan);
                            loan.getLoanPhysicalObjects().add(lpo);
                        }
                        
                        DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
                        if (dataEntryTask != null)
                        {
                            DBTableIdMgr.TableInfo loanTableInfo = DBTableIdMgr.getInstance().getInfoById(loan.getTableId());
                            dataEntryTask.openView(thisTask, null, loanTableInfo.getDefaultFormName(), "edit", loan, true);
                        }
                        return null;
                    }

                    //Runs on the event-dispatching thread.
                    public void finished()
                    {
                        JStatusBar statusBar = UIRegistry.getStatusBar();
                        statusBar.setIndeterminate(false);
                        statusBar.setText("");
                    }
                };
                worker.start();
            }
            
        } else
        {
            log.error("Query String empty for RecordSet tableId["+recordSet.getDbTableId()+"]");
        }
    }
    
    /**
     * Displays UI that asks the user to select a predefined label.
     * @return the name of the label file or null if cancelled
     */
    protected String askForInvoiceName()
    {
        initialize();

        // XXX Need to pass in or check table type for different types of lables.

        NavBoxItemIFace nbi = null;
        if (invoiceList.size() == 1)
        {
            nbi = invoiceList.get(0);

        } else
        {
            ChooseFromListDlg<NavBoxItemIFace> dlg = new ChooseFromListDlg<NavBoxItemIFace>((Frame)UIRegistry.getTopWindow(),
                                                                                            getResourceString("ChooseInvoice"), 
                                                                                            invoiceList, 
                                                                                            IconManager.getIcon(name, IconManager.IconSize.Std24));
            dlg.setMultiSelect(false);
            dlg.setModal(true);
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                nbi  = dlg.getSelectedObject();
            }
        }
        
        if (nbi != null && nbi.getData() != null)
        {
            Object data = nbi.getData();
            if (data instanceof CommandAction)
            {
                return ((CommandAction)data).getPropertyAsString("file");
            }
        }
        return null;
    }
    
    /**
     * Fixes up the UI as to whether it is a new or existing loan and copies the 
     * LoanNumber to the ShipmentNumber.
     * @param formPane the form containing the loan
     */
    protected void adjustLoanForm(FormPane formPane)
    {
        FormViewObj formViewObj = formPane.getMultiView().getCurrentViewAsFormViewObj();
        if (formViewObj != null && formViewObj.getDataObj() instanceof Loan)
        {
            Loan      loan     = (Loan)formViewObj.getDataObj();
            boolean   isNewObj = MultiView.isOptionOn(formPane.getMultiView().getOptions(), MultiView.IS_NEW_OBJECT);
            boolean   isEdit   = formPane.getMultiView().isEditable();

            Component comp     = formViewObj.getControlByName("generateInvoice");
            if (comp instanceof JCheckBox)
            {
                ((JCheckBox)comp).setVisible(isEdit);
            }
            comp = formViewObj.getControlByName("ReturnLoan");
            if (comp instanceof JButton)
            {
                comp.setVisible(!isNewObj && isEdit);
                comp.setEnabled(!loan.getIsClosed());
            }
            
            if (isNewObj)
            {
                Component shipComp = formViewObj.getControlByName("shipmentNumber");
                comp = formViewObj.getControlByName("loanNumber");
                if (comp instanceof JTextField && shipComp instanceof JTextField)
                {
                    ValFormattedTextField loanTxt = (ValFormattedTextField)comp;
                    ValFormattedTextField shipTxt = (ValFormattedTextField)shipComp;
                    shipTxt.setValue(loanTxt.getText(), loanTxt.getText());
                    shipTxt.setChanged(true);
                }
            }
        }
    }
    
    /**
     * Creates a new InfoRequest from a RecordSet.
     * @param recordSet the recordset to use to create the InfoRequest
     */
    protected void createInfoRequest(final RecordSetIFace recordSet)
    {
        DBTableIdMgr.TableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(InfoRequest.class.getSimpleName());
        
        SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        
        View view = appContextMgr.getView(tableInfo.getDefaultFormName(), CollectionObjDef.getCurrentCollectionObjDef());

        InfoRequest infoRequest = new InfoRequest();
        infoRequest.initialize();
        if (recordSet != null)
        {
            infoRequest.setRecordSet(recordSet);
            
        } else
        {
            infoRequest.setRecordSet(askForRecordSet(CollectionObject.getClassTableId()));
        }
        
        if (infoRequest.getRecordSet() != null &&
            infoRequest.getRecordSet().getItems() != null &&
            infoRequest.getRecordSet().getItems().size() > 0)
        {
            createFormPanel(view.getViewSetName(), view.getName(), "edit", infoRequest, MultiView.IS_NEW_OBJECT, null);
            
        } else
        {
            UIRegistry.displayErrorDlgLocalized("ERROR_MISSING_RS_OR_NOITEMS");  
        }
    }

    
    /**
     * @param cmdAction
     */
    protected void checkToPrintLoan(final CommandAction cmdAction)
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
            Object[] options = {getResourceString("CreateLoanInvoice"), getResourceString("Cancel")};
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
        
        if (printLoan)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                session.attach(loan);
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
    
    /**
     * Creates an Excel SpreadSheet or CVS file and attaches it to an email and send it to an agent.
     */
    public void createAndSendEMail()
    {
        FormViewObj formViewObj = getCurrentFormViewObj();
        if (formViewObj != null) // Should never happen
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
                    
                    dlg.setCloseListener(new PropertyChangeListener()
                    {
                        public void propertyChange(PropertyChangeEvent evt)
                        {
                            String action = evt.getPropertyName();
                            if (action.equals("OK"))
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
                            else if (action.equals("Cancel"))
                            {
                                log.warn("User clicked Cancel");
                            }
                        }
                    });
    
                    dlg.setVisible(true);
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
    protected void deleteInfoRequest(final InfoRequest infoRequest)
    {
        // delete from database
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            // ???? session.attach(infoRequest);
            session.beginTransaction();
            session.delete(infoRequest);
            session.commit();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
        session.close();

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
     * @param agent the agent doing the return
     * @param returns the list of items being returned
     */
    protected void doReturnLoan(final Agent  agent, 
                                final List<LoanReturnInfo> returns)
    {
        final SwingWorker worker = new SwingWorker()
        {
            public Object construct()
            {
                JStatusBar statusBar = UIRegistry.getStatusBar();
                statusBar.setIndeterminate(true);
                statusBar.setText(getResourceString("ReturningLoanItems"));
                
                for (LoanReturnInfo lri : returns)
                {   
                    LoanPhysicalObject       lpo  = lri.getLoanPhysicalObject();
                    LoanReturnPhysicalObject lrpo = new LoanReturnPhysicalObject();
                    lrpo.initialize();
                    //lrpo.setAgent(agent);
                    lrpo.setReceivedBy(agent);
                    lrpo.setLastEditedBy(FormHelper.getCurrentUserEditStr());
                    lrpo.setReturnedDate(Calendar.getInstance());
                    lrpo.setQuantity(lri.getQuantity());
                    lrpo.setRemarks(lri.getRemarks());
                    if (lri.isResolved() != null)
                    {
                        lri.getLoanPhysicalObject().setIsResolved(lri.isResolved());
                    }
                    lrpo.setLoanPhysicalObject(lpo);
                    lpo.getLoanReturnPhysicalObjects().add(lrpo);
                    
                }
                return null;
            }

            //Runs on the event-dispatching thread.
            public void finished()
            {
                JStatusBar statusBar = UIRegistry.getStatusBar();
                statusBar.setIndeterminate(false);
                statusBar.setText("");
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
            dlg.setModal(true);
            dlg.setVisible(true);
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
                    doReturnLoan(dlg.getAgent(), returns);
                }
            }
            
        } else
        {
            // XXX Show some kind of error dialog
        }
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------
    
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
                printLoan(null, srcObj);
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
                    NavBoxItemIFace nbi = addNavBoxItem(infoRequestNavBox, infoRequest.getIdentityTitle(), INTERACTIONS, new CommandAction(INTERACTIONS, DELETE_CMD_ACT, infoRequest), infoRequest);
                    setUpDraggable(nbi, new DataFlavor[]{Trash.TRASH_FLAVOR, INFOREQUEST_FLAVOR}, new NavBoxAction("", ""));
                }
            }
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
                    return;
                }

                printLoan(null, cmdAction.getData());
                
            } if (cmdAction.getData() instanceof CommandAction)
            {
                String tableIDStr = cmdAction.getPropertyAsString("tableid");
                if (StringUtils.isNotEmpty(tableIDStr) && StringUtils.isNumeric(tableIDStr))
                {
                    RecordSetIFace recordSet = askForRecordSet(Integer.parseInt(tableIDStr));
                    if (recordSet != null)
                    {
                        printLoan(cmdAction.getPropertyAsString("file"), recordSet);
                    }
                }
            }

            
        } else if (cmdAction.isAction("CreateInfoRequest") && cmdAction.getData() instanceof RecordSet)
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSet)
            {
                createInfoRequest((RecordSetIFace)data);
            }
            
        } else if (cmdAction.isAction("ReturnLoan"))
        {
            returnLoan();
            
        } else if (cmdAction.isAction(DELETE_CMD_ACT) && cmdAction.getData() instanceof InfoRequest)
        {
            InfoRequest inforRequest = (InfoRequest)cmdAction.getData();
            deleteInfoRequest(inforRequest);
            deleteInfoRequestFromUI(null, inforRequest);
            
        } else 
        {
            Object cmdData = cmdAction.getData();
            if (cmdData == null)
            {
                //LabelsTask.askForRecordSet(tableId)   
            }
            
            // These all assume there needs to be a recordsset
            if (cmdData != null)
            {
                if (cmdData instanceof RecordSetIFace)
                {
                    RecordSetIFace rs = (RecordSetIFace)cmdData;
                    
                    if (rs.getDbTableId() == colObjTableId || rs.getDbTableId() == infoRequestTableId)
                    {
                        if (cmdAction.isAction(NEW_LOAN))
                        {    
                            createNewLoan(null, rs);
                                
                        } else if (cmdAction.isAction(InfoRequestName))
                        {
                            createInfoRequest(rs);      
                        }
                    } else
                    {
                        log.error("Dropped wrong table type.");
                        // Error Msg Dialog XXX
                    }
                } else if (cmdData instanceof InfoRequest)
                {
                    createNewLoan((InfoRequest)cmdData);
                    
                } else if (cmdData instanceof CommandAction)
                {
                    if (cmdAction.isAction(InfoRequestName))
                    {
                        createInfoRequest(null);    
                    }
                }
            }
        }
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @SuppressWarnings("unchecked")
    public void doCommand(final CommandAction cmdAction)
    {
        log.debug("Processing Command ["+cmdAction.getType()+"]["+cmdAction.getAction()+"]");
        
        if (cmdAction.isType(DB_CMD_TYPE))
        {
            processDatabaseCommands(cmdAction);

        } else if (cmdAction.isType(DataEntryTask.DATA_ENTRY))
        {
            if (cmdAction.isAction(DataEntryTask.VIEW_WAS_OPENED))
            {
                adjustLoanForm((FormPane)cmdAction.getData());
                
            } else if (cmdAction.isAction("Save"))
            {
                checkToPrintLoan(cmdAction);
            }
            
        } else if (cmdAction.isType(INTERACTIONS))
        {
            processInteractionsCommands(cmdAction);
            
        } else if (cmdAction.isType(RecordSetTask.RECORD_SET))
        {
            processRecordSetCommands(cmdAction);
        }
            

    }
}
