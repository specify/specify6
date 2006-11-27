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

import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
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
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPhysicalObject;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.ui.ChooseRecordSetDlg;
import edu.ku.brc.specify.ui.LoanSelectPrepsDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.Trash;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.dnd.DataActionEvent;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.MultiView;
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


    // Data Members
    protected Vector<NavBoxIFace> extendedNavBoxes = new Vector<NavBoxIFace>();

   /**
     * Default Constructor
     *
     */
    public InteractionsTask()
    {
        super(INTERACTIONS, getResourceString("Interactions"));
        
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false

            // Temporary
            NavBox navBox = new NavBox(getResourceString("Actions"));
            //navBox.add(NavBox.createBtn(getResourceString("Accession"),  "Interactions", IconManager.IconSize.Std16,
            //        new CreateViewAction(this, null, "Accession", "Edit", Accession.class)));
            //navBox.add(NavBox.createBtn(getResourceString("New_Loan"),  name, IconManager.IconSize.Std16));
            addToNavBoxAndRegisterAsDroppable(navBox, NavBox.createBtn(getResourceString("New_Loan"),  name, IconManager.IconSize.Std16, new InteractionAction("","")), null);
            navBox.add(NavBox.createBtn(getResourceString("New_Gifts"), name, IconManager.IconSize.Std16));
            navBox.add(NavBox.createBtn(getResourceString("New_Exchange"), name, IconManager.IconSize.Std16));
            navBoxes.addElement(navBox);
    
            navBox = new NavBox(getResourceString(ReportsTask.REPORTS));
            navBox.add(NavBox.createBtn(getResourceString("All_Overdue_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
            navBox.add(NavBox.createBtn(getResourceString("All_Open_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
            navBox.add(NavBox.createBtn(getResourceString("All_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
            navBoxes.addElement(navBox);
        }
        
        CommandDispatcher.register(INTERACTIONS, this);
        CommandDispatcher.register(RecordSetTask.RECORD_SET, this);
        CommandDispatcher.register("App", this);

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
        } else if (cmdAction.getAction().equals("CreateLoan"))
        {
            if (cmdAction.getData() instanceof RecordSetIFace)
            {
                RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getData();
                
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
                                statusBar.setText("Creating Loan...");
                                Loan loan = new Loan();
                                loan.initialize();
                                
                                for (Preparation prep : prepsHash.keySet())
                                {
                                    Integer count = prepsHash.get(prep);
                                    
                                    LoanPhysicalObject lpo = new LoanPhysicalObject();
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
        }
    }

    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------
    class InteractionAction implements ActionListener
    {
        private String    nameStr;
        private String    titleStr;
        private int       tableId;
        private RecordSetIFace recordSet = null;


        public InteractionAction(final TaskCommandDef tcd)
        {
            this.nameStr  = tcd.getParams().get("file");
            this.titleStr = tcd.getParams().get("title");
            this.tableId  = Integer.parseInt(tcd.getParams().get("tableid"));
        }

        public InteractionAction(final String nameStr, final String titleStr)
        {
            this.nameStr  = nameStr;
            this.titleStr = titleStr;
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
                        doCommand(new CommandAction(INTERACTIONS, "CreateLoan", data));
                        //JOptionPane.showMessageDialog(null, getResourceString("ERROR_LABELS_RECORDSET_TABLEID"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE);
                        return;
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
    }


}
