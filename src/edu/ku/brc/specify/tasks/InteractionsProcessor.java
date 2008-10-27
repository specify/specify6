/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.tasks.BaseTask.ASK_TYPE;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationsProviderIFace;
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
                                 final boolean isLoan,
                                 final int     tableId)
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
                PrepLoader loader = new PrepLoader(session, sqlStr, currPrepProvider, infoRequest);
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
     * @param prepProvider
     * @param infoRequest
     * @param session
     */
    protected void prepsLoaded(final ArrayList<CollectionObject> availColObjList,
                               final T                           prepProvider,
                               final InfoRequest                 infoRequest,
                               final DataProviderSessionIFace    session)
    {
        try
        {
            final DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);

            final SelectPrepsDlg loanSelectPrepsDlg = new SelectPrepsDlg(availColObjList, prepProvider, ti.getTitle());
            loanSelectPrepsDlg.createUI();
            
            if (!loanSelectPrepsDlg.hasAvaliblePrepsToLoan())
            {
                UIRegistry.showLocalizedMsg("NEW_INTER_NO_PREPS_TITLE", "NEW_INTER_NO_PREPS");
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
    class PrepLoader extends javax.swing.SwingWorker<Integer, Integer>
    {
        private DataProviderSessionIFace session;
        private String                   sqlStr;
        private InfoRequest              infoRequest;
        private T                        prepsProvider;
               
        private ArrayList<CollectionObject> availColObjList = new ArrayList<CollectionObject>();
        private ArrayList<CollectionObject> noCurrDetList   = new ArrayList<CollectionObject>();
        private List<CollectionObject>      colObjList      = null;

        public PrepLoader(final DataProviderSessionIFace session,
                          final String                   sqlStr,
                          final T                        prepsProvider,
                          final InfoRequest              infoRequest)
        {
            this.session       = session;
            this.sqlStr        = sqlStr;
            this.prepsProvider = prepsProvider;
            this.infoRequest   = infoRequest;
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
                                DeterminationStatus.isCurrentType(det.getStatus().getType()))
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
            UIRegistry.clearSimpleGlassPaneMsg();
            
            prepsLoaded(availColObjList, prepsProvider, infoRequest, session);
        }
        
    }
    
}
