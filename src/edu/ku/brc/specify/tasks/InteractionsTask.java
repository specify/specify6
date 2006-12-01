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

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.datatransfer.DataFlavor;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
import edu.ku.brc.af.core.TaskCommandDef;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPhysicalObject;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.specify.ui.LoanSelectPrepsDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.Trash;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.forms.persist.View;

/**
 * This task manages Loans, Gifts, Exchanges and provide actions and forms to do the interactions
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class InteractionsTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(InteractionsTask.class);

    public static final String     INTERACTIONS        = "Interactions";
    public static final DataFlavor INTERACTIONS_FLAVOR = new DataFlavor(DataEntryTask.class, INTERACTIONS);
    
    protected static final String InfoRequestName = "InfoRequest";
    protected static final String NewLoan         = "New_Loan";
    protected static final String PrintLoan       = "PrintLoan";
    protected static final String AppType         = "App";
    protected static final String DatabaseType    = "Database";

    // Data Members
    protected Vector<NavBoxIFace> extendedNavBoxes = new Vector<NavBoxIFace>();

   /**
     * Default Constructor
     *
     */
    public InteractionsTask()
    {
        super(INTERACTIONS, getResourceString("Interactions"));
        
        CommandDispatcher.register(INTERACTIONS, this);
        CommandDispatcher.register(RecordSetTask.RECORD_SET, this);
        CommandDispatcher.register(AppType, this);
        CommandDispatcher.register(DatabaseType, this);

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
            //labelsList.clear();

            // Temporary
            NavBox navBox = new NavBox(getResourceString("Actions"));
            addToNavBoxAndRegisterAsDroppable(navBox, NavBox.createBtn(getResourceString(NewLoan),  name, IconManager.IconSize.Std16, new NavBoxAction(INTERACTIONS, NewLoan)), null);
            navBox.add(NavBox.createBtn(getResourceString("New_Gifts"), name, IconManager.IconSize.Std16));
            navBox.add(NavBox.createBtn(getResourceString("New_Exchange"), name, IconManager.IconSize.Std16));
            addToNavBoxAndRegisterAsDroppable(navBox, NavBox.createBtn(getResourceString(InfoRequestName),  InfoRequestName, IconManager.IconSize.Std16, new NavBoxAction(INTERACTIONS, InfoRequestName, this)), null);
            navBoxes.addElement(navBox);
    
            // These need to be loaded as Resources
            navBox = new NavBox(getResourceString(ReportsTask.REPORTS));
            navBox.add(NavBox.createBtn(getResourceString("All_Overdue_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
            //navBox.add(NavBox.createBtn(getResourceString("All_Open_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
            //navBox.add(NavBox.createBtn(getResourceString("All_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
            addToNavBoxAndRegisterAsDroppable(navBox, NavBox.createBtn(getResourceString(PrintLoan),  ReportsTask.REPORTS, IconManager.IconSize.Std16, new NavBoxAction(INTERACTIONS, PrintLoan, this)), null);
            navBoxes.addElement(navBox);
            
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
                    if (tableIdStr == null)
                    {
                        log.error("Interaction Command is missing the table id");
                    } else
                    {
                        addToNavBoxAndRegisterAsDroppable(navBox, NavBox.createBtn(tcd.getName(), name, IconManager.IconSize.Std16, new NavBoxAction(tcd, this)), tcd.getParams());
                    }
                }
            }

            navBoxes.addElement(navBox);
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
        return new SimpleDescPane(title, this, "Please select an Interaction");
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
     * @param recordSet the recordset to use to create the loan
     */
    protected void printLoan(final Object data)
    {
        //String loanNumber = null;
        if (data instanceof RecordSetIFace)
        {
            RecordSetIFace rs = (RecordSetIFace)data;
            CommandAction cmd = new CommandAction(LabelsTask.LABELS, LabelsTask.DOLABELS_ACTION, rs);
            cmd.setProperty(NavBoxAction.ORGINATING_TASK, this);
            CommandDispatcher.dispatch(cmd);
            
            /*
            if (rs.getItems().size() > 0)
            {
                Long recordId = rs.getItems().iterator().next().getRecordId();
                if (recordId != null)
                {
                    DBTableIdMgr.TableInfo   tableInfo = DBTableIdMgr.lookupByClassName(Loan.class.getName());
                    String                   sqlStr    = DBTableIdMgr.getQueryForTable(tableInfo.getTableId(), recordId);
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    List<?> loans = session.getDataList(sqlStr);
                    if (loans.size() > 0)
                    {
                        //RecordSet rs = new RecordSet();
                        //rs.setDbTableId(tableId)
                        CommandDispatcher.dispatch(new CommandAction(LabelsTask.LABELS, LabelsTask.DOLABELS_ACTION, ));
                        
                    } else
                    {
                        // Error Dialog
                    }
                    
                } else
                {
                    // ask for Loan Noan Number
                }
                
            } else
            {
                // ask for loan number here
            }*/
        }
    }
    
    /**
     * Creates a new loan from a RecordSet.
     * @param recordSet the recordset to use to create the loan
     */
    @SuppressWarnings("unchecked")
    protected void createNewLoan(final RecordSetIFace recordSet)
    {
       
        DBTableIdMgr.getInClause(recordSet);

        DBTableIdMgr.TableInfo tableInfo = DBTableIdMgr.lookupInfoById(recordSet.getDbTableId());
        
        DataProviderFactory.getInstance().evict(tableInfo.getClassObj());
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        String sqlStr = DBTableIdMgr.getQueryForTable(recordSet);
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
                        JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
                        statusBar.setIndeterminate(true);
                        statusBar.setText("Creating Loan..."); // XXX I18N
                        
                        Loan loan = new Loan();
                        loan.initialize();
                        
                        Calendar dueDate = Calendar.getInstance();
                        dueDate.add(Calendar.MONTH, 6);                 // XXX PREF Due Date
                        loan.setCurrentDueDate(dueDate);
                        
                        Shipment shipment = new Shipment();
                        shipment.initialize();
                        
                        loan.setShipment(shipment);
                        shipment.getLoans().add(loan);
                        
                        for (Preparation prep : prepsHash.keySet())
                        {
                            Integer count = prepsHash.get(prep);
                            
                            LoanPhysicalObject lpo = new LoanPhysicalObject();
                            lpo.initialize();
                            lpo.setPreparation(prep);
                            lpo.setQuantity(count.shortValue());
                            lpo.setLoan(loan);
                            loan.getLoanPhysicalObjects().add(lpo);
                        }
                        
                        DataEntryTask dataEntryTask = (DataEntryTask)TaskMgr.getTask(DataEntryTask.DATA_ENTRY);
                        if (dataEntryTask != null)
                        {
                            DBTableIdMgr.TableInfo loanTableInfo = DBTableIdMgr.lookupInfoById(loan.getTableId());
                            dataEntryTask.openView(thisTask, null, loanTableInfo.getDefaultFormName(), "edit", loan, true);
                        }
                        return null;
                    }

                    //Runs on the event-dispatching thread.
                    public void finished()
                    {
                        JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
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
     * Creates a new InfoRequest from a RecordSet.
     * @param recordSet the recordset to use to create the InfoRequest
     */
    protected void createInfoRequest(final RecordSetIFace recordSet)
    {
        InfoRequestTask.createInfoRequest(recordSet);
    }
    

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    @SuppressWarnings("unchecked")
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getAction().equals("NewInteraction"))
        {
            if (cmdAction.getData() instanceof RecordSetIFace)
            {
                addSubPaneToMgr(DataEntryTask.createFormFor(this, name, (RecordSetIFace)cmdAction.getData()));

            } else if (cmdAction.getData() instanceof Object[])
            {
                Object[] dataList = (Object[])cmdAction.getData();
                if (dataList.length != 3)
                {
                    View   view = (View)dataList[0];
                    String mode = (String)dataList[1];
                    String idStr = (String)dataList[2];
                    DataEntryTask.openView(this, view, mode, idStr);

                } else
                {
                    log.error("The Edit Command was sent with an object Array that was not 3 components!");
                }
            } else
            {
                log.error("The Edit Command was sent that didn't have data that was a RecordSet or an Object Array");
            }
          
        } else if (cmdAction.getAction().equals(PrintLoan))
        {
            printLoan(cmdAction.getData());
            
        } else if (cmdAction.getType().equals(DatabaseType) && cmdAction.getAction().equals("Insert"))
        {
            if (cmdAction.getData() instanceof Loan)
            {
                Loan loan = (Loan)cmdAction.getData();
                RecordSet rs = new RecordSet();
                rs.initialize();
                rs.setName(loan.getIdentityTitle());
                rs.setDbTableId(loan.getTableId());
                rs.addItem(loan.getId());
                printLoan(rs);
            }
            
        } else 
        {
            Object cmdData = cmdAction.getData();
            if (cmdData == null)
            {
                
                //LabelsTask.askForRecordSet(tableId)
                
            }
            
            // These all assume there needs to be a recordsset
            if (cmdData != null && cmdData instanceof RecordSetIFace)
            {
                if (cmdAction.getAction().equals(NewLoan))
                {
                    createNewLoan((RecordSetIFace)cmdData);    
                        
                } else if (cmdAction.getAction().equals(InfoRequestName))
                {
                    createInfoRequest((RecordSetIFace)cmdData);    
                }
            }
        }

    }


    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------
    /*class InteractionAction implements ActionListener
    {
        private String    type;
        private String    action;
        private String    nameStr;
        private String    titleStr;
        private int       tableId;
        private RecordSetIFace recordSet = null;


        public InteractionAction(final TaskCommandDef tcd)
        {
            this.nameStr  = tcd.getParams().get("file");
            this.titleStr = tcd.getParams().get("title");
            this.type     = tcd.getParams().get("type");
            this.action   = tcd.getParams().get("action");
            this.tableId  = Integer.parseInt(tcd.getParams().get("tableid"));
        }

        public InteractionAction(final String action)
        {
            this.type   = INTERACTIONS;
            this.action = action;
        }

        public void actionPerformed(ActionEvent e)
        {
            boolean needsRecordSets = true;
            
            Object data = null;
            if (e instanceof DataActionEvent)
            {
                DataActionEvent dae = (DataActionEvent)e;
                data = dae.getData();
                if (data instanceof RecordSet)
                {
                    RecordSetIFace rs = (RecordSetIFace)data;
                    if (rs.getDbTableId() != tableId)
                    {
                        if (StringUtils.isNotEmpty(action))
                        {
                            if (action.equals(NewLoan))
                            {
                                doCommand(new CommandAction(INTERACTIONS, action, data));
                                //JOptionPane.showMessageDialog(null, getResourceString("ERROR_LABELS_RECORDSET_TABLEID"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            else if (action.equals(InfoRequestName))
                            {
                                InfoRequestTask.createInfoRequest((RecordSetIFace)data);
                                return;
                            } else
                            {
                                CommandAction cmd = new CommandAction(type, action, data);
                                //cmd.addProperties();
                                CommandDispatcher.dispatch(new CommandAction(type, action, data));
                                return;
                            }
                        } else
                        {
                            log.debug("Action was NULL!");
                        }
                    }
                }
            }

            if (!needsRecordSets)
            {
                //doLabels(nameStr, titleStr, null);
                
            } else if (data instanceof RecordSet)
            {
                //doLabels(nameStr, titleStr, (RecordSetIFace)data);

            } else
            {
                log.error("Data is not RecordSet");
            }

        }

        public void setRecordSet(final RecordSetIFace recordSet)
        {
            this.recordSet = recordSet;
        }

        public RecordSetIFace getRecordSet()
        {
            return recordSet;
        }
    }*/


}
