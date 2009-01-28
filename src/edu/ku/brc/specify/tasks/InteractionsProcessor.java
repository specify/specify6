/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.tasks.BaseTask.ASK_TYPE;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.PreparationsProviderIFace;
import edu.ku.brc.specify.ui.ColObjInfo;
import edu.ku.brc.specify.ui.PrepInfo;
import edu.ku.brc.specify.ui.SelectPrepsDlg;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 9, 2008
 *
 */
public class InteractionsProcessor<T extends PreparationsProviderIFace>
{
    private static final Logger log = Logger.getLogger(InteractionsProcessor.class);
    
    protected InteractionsTask task;
    protected boolean          isLoan;
    protected int              tableId;
    
    /**
     * 
     */
    public InteractionsProcessor(final InteractionsTask task, 
                                 final boolean          isLoan,
                                 final int              tableId)
    {
        this.task    = task;
        this.isLoan  = isLoan;
        this.tableId = tableId;
    }
    
    
    /**
     * Asks where the source of the Loan Preps should come from.
     * @return the source enum
     */
    protected ASK_TYPE askSourceOfPreps(final boolean hasInfoReqs, final boolean hasColObjRS)
    {
        String label;
        if (hasInfoReqs && hasColObjRS)
        {
            label = getResourceString("NEW_INTER_USE_RS_IR");
            
        } else if (hasInfoReqs)
        {
            label = getResourceString("NEW_INTER_USE_IR");
        } else
        {
            label = getResourceString("NEW_INTER_USE_RS");
        }
        
        Object[] options = { 
                label, 
                getResourceString("NEW_INTER_ENTER_CATNUM") 
              };
        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                     getResourceString("NEW_INTER_CHOOSE_RSOPT"), 
                                                     getResourceString("NEW_INTER_CHOOSE_RSOPT_TITLE"), 
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
     * Creates a new loan/gift.
     */
    public void createOrAdd()
    {
        createOrAdd(null, null, null);
    }
    
    /**
     * @param recordSetArg
     */
    public void createOrAdd(final RecordSetIFace recordSetArg)
    {
        createOrAdd(null, null, recordSetArg);
    }
    
    /**
     * @param currPrepProvider
     */
    public void createOrAdd(final T currPrepProvider)
    {
        createOrAdd(currPrepProvider, null, null);
    }
    
    /**
     * Creates a new loan from a RecordSet.
     * @param currPrepProvider an existing loan that needs additional Preps
     * @param infoRequest a info request
     * @param recordSetArg the recordset to use to create the loan
     */
    public void createOrAdd(final T              currPrepProvider, 
                            final InfoRequest    infoRequest, 
                            final RecordSetIFace recordSetArg)
    {
        RecordSetIFace recordSet = recordSetArg;
        if (infoRequest == null && recordSet == null)
        {
            // Get a List of InfoRequest RecordSets
            Vector<RecordSetIFace> rsList       = task.getInfoReqRecordSetsFromSideBar();
            RecordSetTask          rsTask       = (RecordSetTask)TaskMgr.getTask(RecordSetTask.RECORD_SET);
            List<RecordSetIFace>   colObjRSList = rsTask.getRecordSets(CollectionObject.getClassTableId());
            
            // If the List is empty then
            if (rsList.size() == 0 && colObjRSList.size() == 0)
            {
                recordSet = task.askForCatNumbersRecordSet();
                
            } else 
            {
                ASK_TYPE rv = askSourceOfPreps(rsList.size() > 0, colObjRSList.size() > 0);
                if (rv == ASK_TYPE.ChooseRS)
                {
                    recordSet = RecordSetTask.askForRecordSet(CollectionObject.getClassTableId(), rsList);
                    
                } else if (rv == ASK_TYPE.EnterCats)
                {
                    recordSet = task.askForCatNumbersRecordSet();
                    
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
            if (infoRequest == null && recordSet.getDbTableId() == InfoRequest.getClassTableId())
            {
                if (recordSet.getNumItems() == 1)
                {
                    RecordSetItemIFace item = recordSet.getOnlyItem();
                    if (item != null)
                    {
                        InfoRequest infoReq = session.get(InfoRequest.class, item.getRecordId().intValue());
                        if (infoReq != null)
                        {
                            createOrAdd(null, infoReq, infoReq.getRecordSets().iterator().next());
                            
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
                
                UIRegistry.writeSimpleGlassPaneMsg(getResourceString("NEW_INTER_LOADING_PREP"), 24);
                
                PrepLoaderSQL prepLoaderSQL = new PrepLoaderSQL(currPrepProvider, recordSet, infoRequest, isLoan);
                prepLoaderSQL.addPropertyChangeListener(
                        new PropertyChangeListener() {
                            public  void propertyChange(PropertyChangeEvent evt) {
                                log.debug(evt.getNewValue());
                                if ("progress".equals(evt.getPropertyName())) 
                                {
                                    statusBar.setValue("LoanLoader", (Integer)evt.getNewValue());
                                }
                            }
                        });
                prepLoaderSQL.execute();
                
            } else
            {
                log.error("Query String empty for RecordSet tableId["+recordSet.getDbTableId()+"]");
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsProcessor.class, ex);
        }
    }

    /**
     * @param coToPrepHash
     * @param prepTypeHash
     * @param prepProvider
     * @param infoRequest
     * @param session
     */
    protected void prepsLoaded(final Hashtable<Integer, ColObjInfo> coToPrepHash,
                               final Hashtable<Integer, String>     prepTypeHash,
                               final T                              prepProvider,
                               final InfoRequest                    infoRequest)
    {
        if (coToPrepHash.size() == 0)
        {
            UIRegistry.showLocalizedMsg("NEW_INTER_NO_PREPS_TITLE", "NEW_INTER_NO_PREPS");
            return;
        }
        
        final DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);

        final SelectPrepsDlg loanSelectPrepsDlg = new SelectPrepsDlg(coToPrepHash, prepTypeHash, ti.getTitle());
        loanSelectPrepsDlg.createUI();
        loanSelectPrepsDlg.setModal(true);
        
        UIHelper.centerAndShow(loanSelectPrepsDlg);
        
        if (loanSelectPrepsDlg.isCancelled())
        {
            return;
        }

        final Hashtable<Integer, Integer> prepsHash = loanSelectPrepsDlg.getPreparationCounts();
        if (prepsHash.size() > 0)
        {
            final SwingWorker worker = new SwingWorker()
            {
                @Override
                public Object construct()
                {
                    JStatusBar statusBar = UIRegistry.getStatusBar();
                    statusBar.setIndeterminate("INTERACTIONS", true);
                    statusBar.setText(getLocalizedMessage("CREATING_INTERACTION", ti.getTitle()));
                    
                    if (isLoan)
                    {
                        task.addPrepsToLoan(prepProvider, infoRequest, prepsHash);
                    } else
                    {
                        task.addPrepsToGift(prepProvider, infoRequest, prepsHash);
                    }
                    
                    return null;
                }

                //Runs on the event-dispatching thread.
                @Override
                public void finished()
                {
                    JStatusBar statusBar = UIRegistry.getStatusBar();
                    statusBar.setProgressDone("INTERACTIONS");
                    statusBar.setText("");
                }
            };
            worker.start();
        }
    }
    
    /**
     * Creates a new loan from a InfoRequest.
     * @param infoRequest the infoRequest to use to create the loan
     */
    public void createFromInfoRequest(final InfoRequest infoRequest)
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsProcessor.class, ex);
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
            createOrAdd(null, infoRequest, rs);
        }
    }
    
    //--------------------------------------------------------------
    // Background loader class for loading a large number of loan preparations
    //--------------------------------------------------------------
    class PrepLoaderSQL extends javax.swing.SwingWorker<Integer, Integer>
    {
        private final String PROGRESS = "progress";
        
        private RecordSetIFace recordSet;
        private T              prepsProvider;
        private InfoRequest    infoRequest;
        private boolean        isForLoan;

        private Hashtable<Integer, String>     prepTypeHash = new Hashtable<Integer, String>();
        private Hashtable<Integer, ColObjInfo> coToPrepHash = new Hashtable<Integer, ColObjInfo>();
        
        /**
         * @param prepsProvider
         * @param recordSet
         * @param infoRequest
         */
        public PrepLoaderSQL(final T              prepsProvider,
                             final RecordSetIFace recordSet,
                             final InfoRequest    infoRequest,
                             final boolean        isForLoan)
        {
            this.recordSet     = recordSet;
            this.prepsProvider = prepsProvider;
            this.infoRequest   = infoRequest;
            this.isForLoan     = isForLoan;
        }

        /**
         * @param val
         * @return
         */
        private Integer getInt(final Object val)
        {
            return val == null ? 0 : (Integer)val;
        }
        
        /**
         * @return a List of rows that have the CollectionObject info from the rcordset
         */
        protected Vector<Object[]> getColObjsFromRecordSet()
        {
            String sql = "SELECT co.CollectionObjectID, co.CatalogNumber, tx.FullName FROM determination as dt INNER JOIN collectionobject as co ON dt.CollectionObjectID = co.CollectionObjectID " +
                         "INNER JOIN taxon as tx ON dt.TaxonID = tx.TaxonID WHERE isCurrent <> 0 AND dt.CollectionMemberID = COLMEMID " + 
                         "AND co.CollectionObjectID " + DBTableIdMgr.getInstance().getInClause(recordSet);
            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            log.debug(sql);
            
            return BasicSQLUtils.query(sql);
        }
        
        /**
         * @return
         */
        protected int collectForLoan()
        {
            int total = 0;
            int count = 0;
            try
            {
                Vector<Object[]> coIdRows = getColObjsFromRecordSet();
                total = coIdRows.size() * 2;
                if (coIdRows.size() != 0)
                {
                    UIRegistry.getStatusBar().setProgressRange("LoanLoader", 0, Math.min(count, total));
    
                    // Get Preps with Gifts
                    StringBuilder sb = new StringBuilder();
                    sb.append("SELECT co.CollectionObjectID, p.PreparationID, gp.Quantity " +
                             "FROM preparation AS p INNER JOIN collectionobject AS co ON p.CollectionObjectID = co.CollectionObjectID " +
                             "INNER JOIN giftpreparation AS gp ON p.PreparationID = gp.GiftPreparationID " +
                             "WHERE co.CollectionMemberID = COLMEMID AND co.CollectionObjectID in (");
                   for (Object[] row : coIdRows)
                   {
                       count++;
                       if ((count % 10) == 0) firePropertyChange(PROGRESS, 0, count);
                       
                       Integer coId = (Integer)row[0];
                       sb.append(coId);
                       sb.append(',');
                       
                       if (row[1] != null && row[2] != null)
                       {
                           coToPrepHash.put(coId, new ColObjInfo(coId, row[1].toString(), row[2].toString()));
                       }
                   }
                   sb.setLength(sb.length()-1); // chomp last comma
                   sb.append(')');
                   
                   // Get a hash contain a mapping from PrepId to Gift Quantity
                   Hashtable<Integer, Integer> prepIdToGiftQnt = new Hashtable<Integer, Integer>();
                   
                   String sql = QueryAdjusterForDomain.getInstance().adjustSQL(sb.toString());
                   log.debug(sql);
                   
                   Vector<Object[]> rows = BasicSQLUtils.query(sql);
                   if (rows.size() > 0)
                   {
                       for (Object[] row : rows)
                       {
                           prepIdToGiftQnt.put((Integer)row[0], (Integer)row[1]);
                       }
                   }
                   
                   // Now get the Preps With Loans
                   sb = new StringBuilder();
                   sb.append("SELECT p.PreparationID, p.Count, lp.Quantity, lp.QuantityResolved, " +
                             "co.CollectionObjectID, pt.PrepTypeID, pt.Name " +
                             "FROM preparation AS p INNER JOIN collectionobject AS co ON p.CollectionObjectID = co.CollectionObjectID " +
                             "INNER JOIN preptype AS pt ON p.PrepTypeID = pt.PrepTypeID " +
                             "LEFT OUTER JOIN loanpreparation AS lp ON p.PreparationID = lp.PreparationID " +
                             "WHERE pt.IsLoanable <> 0 AND co.CollectionObjectID in (");
                   for (Object[] row : coIdRows)
                   {
                       sb.append(row[0]);
                       sb.append(',');
                   }
                   sb.setLength(sb.length()-1); // chomp last comma
                   sb.append(") ORDER BY co.CatalogNumber ASC");
                   
                   // Get the Preps and Qty
                   sql = QueryAdjusterForDomain.getInstance().adjustSQL(sb.toString());
                   log.debug(sql);
                   
                   rows = BasicSQLUtils.query(sql);
                   if (rows.size() > 0)
                   {
                       for (Object[] row : rows)
                       {
                           count++;
                           if ((count % 10) == 0) firePropertyChange(PROGRESS, 0, Math.min(count, total));
                           
                           int prepId = getInt(row[0]);
                           int pQty   = getInt(row[1]);
                           int qty    = getInt(row[2]);
                           int qtyRes = getInt(row[3]);
                           int coId   = getInt(row[4]);
                           
                           prepTypeHash.put((Integer)row[5], row[6].toString());
                           
                           pQty -= getInt(prepIdToGiftQnt.get(prepId));
                           
                           ColObjInfo colObjInfo = coToPrepHash.get(coId);
                           if (colObjInfo == null)
                           {
                               // error
                           }
                           
                           PrepInfo prepInfo = colObjInfo.get(prepId);
                           if (prepInfo != null)
                           {
                               prepInfo.add(qty, qtyRes);
                           } else
                           {
                               colObjInfo.add(new PrepInfo(prepId, (Integer)row[5], pQty, qty, qtyRes));    
                           }
                       }
                   }                   
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsProcessor.class, ex);
    
            }
            firePropertyChange(PROGRESS, 0, total);
            UIRegistry.getStatusBar().setIndeterminate("LoanLoader", true);
            return 0;
        }
        
        /**
         * @return
         */
        protected int collectForGift()
        {
            int total = 0;
            int count = 0;
            try
            {
                Vector<Object[]> coIdRows = getColObjsFromRecordSet();
                if (coIdRows.size() != 0)
                {
                    UIRegistry.getStatusBar().setProgressRange("LoanLoader", 0, total);
                    
                    // Get Preps with Loans
                    StringBuilder sb = new StringBuilder();
                    sb.append("SELECT p.PreparationID, lp.Quantity, lp.QuantityResolved " +
                             "FROM preparation AS p INNER JOIN collectionobject AS co ON p.CollectionObjectID = co.CollectionObjectID " +
                             "INNER JOIN loanpreparation AS lp ON p.PreparationID = lp.LoanPreparationID " +
                             "WHERE co.CollectionMemberID = COLMEMID AND co.CollectionObjectID in (");
                   for (Object[] row : coIdRows)
                   {
                       count++;
                       if ((count % 10) == 0) firePropertyChange(PROGRESS, 0, Math.min(count, total));
                       
                       Integer coId = (Integer)row[0];
                       sb.append(coId);
                       sb.append(',');
                       
                       if (row[1] != null && row[2] != null)
                       {
                           coToPrepHash.put(coId, new ColObjInfo(coId, row[1].toString(), row[2].toString()));
                       }
                   }
                   sb.setLength(sb.length()-1); // chomp last comma
                   sb.append(')');
                   
                   // Get a hash contain a mapping from PrepId to Gift Quantity
                   Hashtable<Integer, Integer> prepIdToLoanQnt = new Hashtable<Integer, Integer>();
                   
                   String sql = QueryAdjusterForDomain.getInstance().adjustSQL(sb.toString());
                   log.debug(sql);
                   
                   Vector<Object[]> rows = BasicSQLUtils.query(sql);
                   if (rows.size() > 0)
                   {
                       for (Object[] row : rows)
                       {
                           int qty    = getInt((Integer)row[1]);
                           int qtyRes = getInt((Integer)row[2]);
                           prepIdToLoanQnt.put((Integer)row[0], qty-qtyRes);
                       }
                   }
                   
                   // Now get the Preps With Gift
                   sb = new StringBuilder();
                   sb.append("SELECT p.PreparationID, p.Count, gp.Quantity, " +
                             "co.CollectionObjectID, pt.PrepTypeID, pt.Name " +
                             "FROM preparation AS p INNER JOIN collectionobject AS co ON p.CollectionObjectID = co.CollectionObjectID " +
                             "INNER JOIN preptype AS pt ON p.PrepTypeID = pt.PrepTypeID " +
                             "LEFT OUTER JOIN giftpreparation AS gp ON p.PreparationID = gp.PreparationID " +
                             "WHERE pt.IsLoanable <> 0 AND co.CollectionObjectID in (");
                   for (Object[] row : coIdRows)
                   {
                       sb.append(row[0]);
                       sb.append(',');
                   }
                   sb.setLength(sb.length()-1); // chomp last comma
                   sb.append(") ORDER BY co.CatalogNumber ASC");
                   
                   // Get the Preps and Qty
                   sql = QueryAdjusterForDomain.getInstance().adjustSQL(sb.toString());
                   log.debug(sql);
                   
                   rows = BasicSQLUtils.query(sql);
                   if (rows.size() > 0)
                   {
                       for (Object[] row : rows)
                       {
                           int prepId = getInt(row[0]);
                           
                           count++;
                           if ((count % 10) == 0) firePropertyChange(PROGRESS, 0, Math.min(count, total));
                           
                           int pQty   = getInt(row[1]);
                           int qty    = getInt(row[2]);
                           int coId   = getInt(row[3]);
                           
                           prepTypeHash.put((Integer)row[4], row[5].toString());
                           
                           pQty -= getInt(prepIdToLoanQnt.get(prepId));
                           
                           ColObjInfo colObjInfo = coToPrepHash.get(coId);
                           if (colObjInfo == null)
                           {
                               // error
                           }
                           
                           PrepInfo prepInfo = colObjInfo.get(prepId);
                           if (prepInfo != null)
                           {
                               prepInfo.add(qty, qty);
                           } else
                           {
                               colObjInfo.add(new PrepInfo(prepId, (Integer)row[4], pQty, qty, 0));    
                           }
                       }
                   }                   
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsProcessor.class, ex);

            }
            firePropertyChange(PROGRESS, 0, total);
            UIRegistry.getStatusBar().setIndeterminate("LoanLoader", true);
            return 0;
        }

        
        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#doInBackground()
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Integer doInBackground() throws Exception
        {
            coToPrepHash = new Hashtable<Integer, ColObjInfo>();
            
            return isForLoan ? collectForLoan() : collectForGift();
        }

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#done()
         */
        @Override
        protected void done()
        {
            super.done();
            UIRegistry.getStatusBar().setProgressDone("LoanLoader");
            UIRegistry.clearSimpleGlassPaneMsg();
            
            prepsLoaded(coToPrepHash, prepTypeHash, prepsProvider, infoRequest);
        }
        
    }
    

}
