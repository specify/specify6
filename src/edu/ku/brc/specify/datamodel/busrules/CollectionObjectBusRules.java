/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.showLocalizedMsg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttribute;
import edu.ku.brc.specify.datamodel.DeaccessionPreparation;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.Project;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.plugins.SeriesProcCatNumPlugin;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 24, 2007
 *
 */
public class CollectionObjectBusRules extends AttachmentOwnerBaseBusRules
{
    private static final String CATNUMNAME = "catalogNumber";
    
    public static final int MAXSERIESSIZE = 500;
    
    //private static final Logger  log = Logger.getLogger(CollectionObjectBusRules.class);
    
    private CollectingEvent  cachedColEve     = null;
    private JButton          generateLabelBtn = null;
    private JCheckBox        generateLabelChk = null;
    
    private AtomicBoolean    processingSeries = new AtomicBoolean(false);
    /**
     * Constructor.
     */
    public CollectionObjectBusRules()
    {
        super(CollectionObject.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(final Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null && formViewObj.isEditing())
        {
            Component comp = formViewObj.getControlByName("generateLabelBtn");
            if (comp instanceof JButton)
            {
                generateLabelBtn = (JButton)comp;
                //generateLabelBtn.setVisible(false);
                generateLabelBtn.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                CommandAction cmdAction = new CommandAction("Data_Entry", "PrintColObjLabel", formViewObj.getDataObj());
                                CommandDispatcher.dispatch(cmdAction);
                            }
                        });
                    }
                });
            }
            
            comp = formViewObj.getControlByName("generateLabelChk");
            if (comp instanceof JCheckBox)
            {
                generateLabelChk = (JCheckBox)comp;
                //generateLabelChk.setVisible(false);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (formViewObj != null && formViewObj.getDataObj() instanceof CollectionObject)
        {
            CollectionObject colObj = (CollectionObject)dataObj;
            
            MultiView mvParent = formViewObj.getMVParent();
            boolean   isNewObj = colObj.getId() == null;
            boolean   isEdit   = mvParent.isEditable();

            if (generateLabelChk != null)
            {
                generateLabelChk.setVisible(isEdit);
            }
            
            if (generateLabelBtn != null)
            {
                generateLabelBtn.setVisible(isEdit);
                generateLabelBtn.setEnabled(!isNewObj);
            }
        }
    }

    /**
     * @param disciplineType
     * @return
     */
    protected PrepType getDefaultPrepType()
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(PrepType.getClassTableId());
        if (tableInfo != null)
        {
            String sqlStr = QueryAdjusterForDomain.getInstance().adjustSQL("FROM PrepType WHERE collectionId = COLLID");
            log.debug(sqlStr);
            if (StringUtils.isNotEmpty(sqlStr))
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    List<?> dataList = session.getDataList(sqlStr);
                    if (dataList != null && !dataList.isEmpty())
                    {
                        // XXX for now we just get the First one
                        return (PrepType)dataList.iterator().next();
                    }
                    // No Data Error
        
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectionObjectBusRules.class, ex);
                    log.error(ex);
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            } else
            {
                log.error("Query String is empty for tableId["+tableInfo.getTableId()+"]");
            }
        } else
        {
            throw new RuntimeException("Error looking up PickLIst's Table Name PrepType");
        }
        return null;
    }
    
    /**
     * @return the default preparer
     */
    protected Agent getDefaultPreparedByAgent()
    {
        return Agent.getUserAgent();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        CollectionObject colObj = (CollectionObject)newDataObj;
        if (AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
        {
            // Carry Forward may have already added 
            // some values so we need to check to make sure
            // before adding new ones automatically.
            if (colObj.getCollectingEvent() == null)
            {
                CollectingEvent ce = new CollectingEvent();
                ce.initialize();
                colObj.addReference(ce, "collectingEvent");
            }
        }
    }
    
    /*
     
        if (status == STATUS.OK)
        {
            String sqlPart = String.format("SELECT COUNT(*) FROM collectionobject co INNER JOIN determination d ON co.CollectionObjectID = d.CollectionObjectID WHERE co.CollectionObjectID = %d", colObj.getId());
            // check that a current determination exists
            int detCnt = BasicSQLUtils.getCountAsInt(sqlPart);
            if (detCnt > 0)
            {
                String sql = String.format("%s AND IsCurrent <> 0", sqlPart);
                int currents = BasicSQLUtils.getCountAsInt(sql);
                
                if (currents != 1)
                {
                    status = STATUS.Error;
                }
                if (currents == 0)
                {
                    reasonList.add(getResourceString("CollectionObjectBusRules.CURRENT_DET_REQUIRED"));
                }
                else
                {
                    reasonList.add(getResourceString("CollectionObjectBusRules.ONLY_ONE_CURRENT_DET"));
                }
            }
        }

     
     */

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object dataObj)
    {
        reasonList.clear();
        STATUS status =  super.processBusinessRules(dataObj);
        
        CollectionObject colObj = (CollectionObject)dataObj;
        if (status == STATUS.OK && colObj.getId() == null)
        {
            DBTableInfo tblInfo   = DBTableIdMgr.getInstance().getInfoById(1); // don't need to check for null
            DBFieldInfo fieldInfo = tblInfo.getFieldByName(CATNUMNAME);
            UIFieldFormatterIFace fmt = fieldInfo.getFormatter();
            if ((fmt != null && fmt.getAutoNumber() == null) || !formViewObj.isAutoNumberOn())
            {
                status = processBusinessRules(null, dataObj, true);
            }

        }
        if (status == STATUS.OK)
        {
            // check that a current determination exists
            if (((CollectionObject) dataObj).getDeterminations().size() > 0)
            {
                int currents = 0;
                for (Determination det : ((CollectionObject) dataObj).getDeterminations())
                {
                    if (det.isCurrentDet())
                    {
                        currents++;
                    }
                }
                if (currents != 1)
                {
                    status = STATUS.Error;
                }
                if (currents == 0)
                {
                    reasonList.add(getResourceString("CollectionObjectBusRules.CURRENT_DET_REQUIRED"));
                }
                else
                {
                    reasonList.add(getResourceString("CollectionObjectBusRules.ONLY_ONE_CURRENT_DET"));
                }
            }
        }
        return status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeMerge(java.lang.Object,
     *      edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(final Object dataObj, 
                            final DataProviderSessionIFace session)
    {
        CollectionObject colObj = (CollectionObject)dataObj;
        
        super.beforeMerge(dataObj, session);
        
        if (AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
        {
            cachedColEve = colObj.getCollectingEvent();
            if (colObj != null && cachedColEve != null)
            {
                colObj.setCollectingEvent(null);
                try
                {
                    cachedColEve.getCollectionObjects().clear();
                } catch (org.hibernate.LazyInitializationException ex)
                {
                    //ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * @param attOwner
     */
    @Override
    protected void addExtraObjectForProcessing(final Object dObjAtt)
    {
        super.addExtraObjectForProcessing(dObjAtt);
        
        CollectionObject colObj = (CollectionObject)dObjAtt;
        
        if (AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent() && 
                (colObj.getCollectingEvent() != null || cachedColEve != null))
        {
            super.addExtraObjectForProcessing(cachedColEve != null ? cachedColEve : colObj.getCollectingEvent());
        }
        
        for (Preparation prep : colObj.getPreparations())
        {
            super.addExtraObjectForProcessing(prep);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        CollectionObject colObj = (CollectionObject)dataObj;
        
        super.beforeSave(dataObj, session);
        
        if (AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
        {
            if (cachedColEve != null)
            {
                try
                {
                    if (cachedColEve != null && cachedColEve.getId() != null)
                    {
                        cachedColEve = session.merge(cachedColEve);
                    } else
                    {
                        session.save(cachedColEve);
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectionObjectBusRules.class, ex);
                }
            }
            
            // Hook back up
            if (cachedColEve != null && colObj != null)
            {
                colObj.setCollectingEvent(cachedColEve);
                cachedColEve.getCollectionObjects().add(colObj);
                cachedColEve = null;
            } else
            {
                log.error("The CE "+cachedColEve+" was null or the CO "+colObj+" was null");
            }
                    
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeDeleteCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        boolean ok = super.beforeDeleteCommit(dataObj, session);
        
        if (ok && dataObj instanceof CollectionObject)
        {
            CollectionObject colObj     = (CollectionObject)dataObj;
            Collection       collection = AppContextMgr.getInstance().getClassObject(Collection.class);
            if (collection != null && collection.getIsEmbeddedCollectingEvent())
            {
                CollectingEvent ce = colObj.getCollectingEvent();
                if (ce != null)
                {
                    try
                    {
                        session.delete(ce);
                        return true;
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectionObjectBusRules.class, ex);
                        ex.printStackTrace();
                        return false;
                    }
                }
            }
        }
        return ok;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean afterSaveCommit(final Object dataObj, final DataProviderSessionIFace session)
    {
        setLSID((FormDataObjIFace)dataObj);

        if (formViewObj != null && formViewObj.isEditing())
        {
            Component comp = formViewObj.getControlByName("generateLabelChk");
            if (comp instanceof JCheckBox && ((JCheckBox)comp).isSelected())
            {
                CommandAction cmdAction = new CommandAction("Data_Entry", "PrintColObjLabel", formViewObj.getDataObj());
                CommandDispatcher.dispatch(cmdAction);
            }
        }
        
        doSeriesProcessing();

        return super.afterSaveCommit(dataObj, session);
    }
    
    /**
     * 
     */
    private void doSeriesProcessing()
    {
		if (!processingSeries.get())
		{
			if (formViewObj != null)
			{
				Component catNumComp = formViewObj.getControlByName(CATNUMNAME);
				if (catNumComp instanceof SeriesProcCatNumPlugin)
				{
					SeriesProcCatNumPlugin spCatNumPlugin = (SeriesProcCatNumPlugin) catNumComp;
					if (spCatNumPlugin.isExpanded())
					{
						DBTableInfo tblInfo = DBTableIdMgr
								.getInstance()
								.getInfoById(CollectionObject.getClassTableId()); // don't
																					// need
																					// to
																					// check
																					// for
																					// null
						DBFieldInfo fieldInfo = tblInfo
								.getFieldByName(CATNUMNAME);
						UIFieldFormatterIFace fmt = fieldInfo.getFormatter();
						if (fmt != null && fmt.getAutoNumber() != null
								&& !formViewObj.isAutoNumberOn())
						{

							doCreateBatchOfColObj(spCatNumPlugin
									.getStartAndEndCatNumbers());
							spCatNumPlugin.clearEndTextField();
						}
					}
				}
			}
		}
    }
    
    
    /**
     * 
     */
    public void doCreateBatchOfColObj(final Pair<String, String> catNumPair)
    {
        if (catNumPair.getFirst().equals(catNumPair.getSecond()))
        {
        	return;
        }
        
    	final String GLASSKEY = "DOBATCHCREATE";
        final String NonIncrementingCatNum = "NonIncrementingCatNum"; 
        final String BatchSaveSuccess = "CollectionObjectBusRules.BatchSaveSuccess";
        final String BatchSaveErrors = "CollectionObjectBusRules.BatchSaveErrors";
        final String BatchSaveErrorsTitle = "CollectionObjectBusRules.BatchSaveErrorsTitle";
        final String BatchRSBaseName = "CollectionObjectBusRules.BatchRSBaseName";
        final String InvalidEntryMsg = "CollectionObjectBusRules.InvalidEntryMsg";
        
        DBFieldInfo CatNumFld = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId()).getFieldByColumnName("CatalogNumber"); 
        final UIFieldFormatterIFace formatter = CatNumFld.getFormatter();
        if (!formatter.isIncrementer())
        {
        	//XXX this will have been checked earlier, right?
        	UIRegistry.showLocalizedError(NonIncrementingCatNum);
        	return;
        }

        final Vector<String> nums = new Vector<String>();

        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            private Vector<Pair<Integer, String>> objectsAdded = new Vector<Pair<Integer, String>>();
            private Vector<String> objectsNotAdded = new Vector<String>();
        	private RecordSet batchRS;
        	private boolean invalidEntry = false;
        	
            @Override
            protected Integer doInBackground() throws Exception
            {
 
                String catNum = catNumPair.getFirst();
                Integer collId = AppContextMgr.getInstance().getClassObject(Collection.class).getId(); 
                String coIdSql = "select CollectionObjectID from collectionobject where CollectionMemberID = " + collId
        			+ " and CatalogNumber = '";
                objectsAdded.add(new Pair<Integer, String>(
                		(Integer )BasicSQLUtils.querySingleObj(coIdSql + catNum + "'"), catNum));
                //XXX potential for infinite loop
                //XXX check for second > first, etc...
                //XXX comparing catnums ...
                while (!catNum.equals(catNumPair.getSecond()) && nums.size() <= MAXSERIESSIZE)
                {
                	//getNextNumber currently gets the number after highest existing number, regardless of the arg.
                	catNum = formatter.getNextNumber(catNum, true); 
                	//catNum = (String )formatter.formatFromUI(String.valueOf(Integer.valueOf(catNum).intValue() + 1));
                    nums.add(catNum);
                }

                if (nums.size() > MAXSERIESSIZE)
                {
                	invalidEntry = true;
                	return 0;
                }
                
            	int cnt = 0;
                CollectionObject co = null;
                CollectionObject carryForwardCo = (CollectionObject )formViewObj.getDataObj();
                
                Thread.sleep(300); //Perhaps this is unnecessary, but it seems
                //to prevent sporadic "illegal access to loading collection" hibernate errors.
                try
                {
                    for (String currentCat : nums)
                    {
                        try
                        {
                            co = new CollectionObject();
                            co.initialize();
                            
                            //Collection doesn't get set in co.initialize(), or carryForward, but it needs to be set.
                            co.setCollection(AppContextMgr.getInstance().getClassObject(Collection.class));
                            //ditto, but doesn't so much need to be set
                            co.setModifiedByAgent(carryForwardCo.getModifiedByAgent()); 
                            
                            formViewObj.getCarryFwdInfo().carryForward(formViewObj.getBusinessRules(), carryForwardCo, co);
                            co.setCatalogNumber(currentCat);
                            formViewObj.setDataObj(co);
                            formViewObj.saveObject();
                            objectsAdded.add(new Pair<Integer, String>(
                            		(Integer )BasicSQLUtils.querySingleObj(coIdSql + co.getCatalogNumber() + "'"), co.getCatalogNumber()));
                        } catch (Exception ex)
                        {
                            log.error(ex);
                            objectsNotAdded.add(co.getCatalogNumber());
                        }
                        cnt++;
                        firePropertyChange(GLASSKEY, 0, cnt);
                    }
                    firePropertyChange(GLASSKEY, 0, nums.size());
                    
                } catch (Exception ex)
                {
					edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
					edu.ku.brc.exceptions.ExceptionTracker.getInstance()
							.capture(Uploader.class, ex);
                }
                formViewObj.setDataObj(carryForwardCo);
                //formViewObj.getRsController().setIndex(formViewObj.getRsController().getLength()-1);
                saveBatchObjectsToRS();
                return objectsAdded.size();
            }

        	protected void saveBatchObjectsToRS()
        	{
        		batchRS = new RecordSet();
        		batchRS.initialize();
        		batchRS.setDbTableId(CollectionObject.getClassTableId());
        		batchRS.setName(getResourceString(BatchRSBaseName) + " " 
        			+ formatter.formatToUI(catNumPair.getFirst()) + "-" 
        			+ formatter.formatToUI(objectsAdded.get(objectsAdded.size()-1).getSecond()));
        		for (Pair<Integer, String> obj : objectsAdded)
        		{
        			batchRS.addItem(obj.getFirst());
        		}
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                boolean transOpen = false;
                try
                {
    				BusinessRulesIFace busRule = DBTableIdMgr.getInstance().getBusinessRule(RecordSet.class);
    				if (busRule != null)
    				{
    					busRule.beforeSave(batchRS, session);
    				}
    				batchRS.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
    				batchRS.setOwner(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
    				session.beginTransaction();
    				transOpen = true;
    				session.save(batchRS);
    				if (busRule != null)
    				{
    					if (!busRule.beforeSaveCommit(batchRS, session))
    					{
    						session.rollback();
    						throw new Exception(
									"Business rules processing failed");
    					}
    				}
    				session.commit();
    				transOpen = false;
    				if (busRule != null)
    				{
    					busRule.afterSaveCommit(batchRS, session);
    				}
                } catch (Exception ex)
                {
                	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                	edu.ku.brc.exceptions.ExceptionTracker.getInstance()
						.capture(Uploader.class, ex);
                	if (transOpen)
                	{
                		session.rollback();
                	}
                }
        	}
        	
        	protected void addBatchRSToUI()
        	{
        		SwingUtilities.invokeLater(new Runnable() {

					/* (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run() {
						CommandAction cmd = new CommandAction(RecordSetTask.RECORD_SET, RecordSetTask.ADD_TO_NAV_BOX);
						cmd.setData(batchRS);
						CommandDispatcher.dispatch(cmd);
					}
        			
        		});
        	}
        	
            @Override
            protected void done()
            {
                super.done();
                processingSeries.set(false);
                addBatchRSToUI();
                UIRegistry.clearSimpleGlassPaneMsg();
                if (invalidEntry)
                {
                	UIRegistry.displayErrorDlgLocalized(InvalidEntryMsg, MAXSERIESSIZE);
                }
                else if (objectsNotAdded.size() == 0)
                {
                	UIRegistry.displayLocalizedStatusBarText(BatchSaveSuccess, formatter.formatToUI(catNumPair.getFirst()), formatter.formatToUI(catNumPair.getSecond()));
                } else
                {
                	JPanel pane = new JPanel(new BorderLayout());
                    JLabel lbl = createLabel(getResourceString(BatchSaveErrors));
                    lbl.setBorder(new EmptyBorder(3, 1, 2, 0));
                    pane.add(lbl, BorderLayout.NORTH);
                    JPanel lstPane = new JPanel(new BorderLayout());
                    JList lst = UIHelper.createList(objectsNotAdded);
                    lst.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
                    lstPane.setBorder(new EmptyBorder(1, 1, 10, 1));
                    lstPane.add(lst, BorderLayout.CENTER);
                    JScrollPane sp = new JScrollPane(lstPane);
                    //pane.add(lstPane, BorderLayout.CENTER);
                    pane.add(sp, BorderLayout.CENTER);
                    pane.setPreferredSize(new Dimension((int )lbl.getPreferredSize().getWidth() + 5, (int )lst.getPreferredScrollableViewportSize().getHeight() + 5));
                    CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(),
                    		UIRegistry.getResourceString(BatchSaveErrorsTitle),
                            true,
                            CustomDialog.OKHELP,
                            pane);
                    UIHelper.centerAndShow(dlg);
                    dlg.dispose();
                }
            }
        };
        
        final SimpleGlassPane gp = UIRegistry.writeSimpleGlassPaneMsg(UIRegistry.getResourceString("CollectionObjectBusRules.SAVING_BATCH"), 24);
        gp.setProgress(0);
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (GLASSKEY.equals(evt.getPropertyName())) 
                        {
                            double value   = (double)((Integer)evt.getNewValue()).intValue();
                            int    percent = (int)(value / ((double)nums.size()) * 100.0);
                            gp.setProgress(percent);
                            
                        }
                    }
                });
        processingSeries.set(true);
        worker.execute();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object dataObjArg,
                           final DataProviderSessionIFace sessionArg,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        boolean isOK = true;
        if (deletable != null && dataObjArg instanceof FormDataObjIFace && ((FormDataObjIFace)dataObjArg).getId() != null)
        {
            Integer count = BasicSQLUtils.getCount("SELECT COUNT(*) FROM collectionobject WHERE CollectionObjectID = " + ((FormDataObjIFace)dataObjArg).getId());
            if (count != null && count == 0)
            {
                showLocalizedMsg("NO_RECORD_FOUND_TITLE", "NO_RECORD_FOUND");
                return;
            }
            
            Object dataObj = dataObjArg;
            DataProviderSessionIFace session = sessionArg != null ? sessionArg : DataProviderFactory.getInstance().createSession();
            try
            {
                dataObj = session.merge(dataObj);
    
                CollectionObject colObj = (CollectionObject)dataObj;
                
                /*if (colObj.getAccession() != null)
                {
                    isOK = false;
                    addDeleteReason(Accession.getClassTableId());
                }*/
                
                if (isOK)
                {
                    // 03/25/09 rods - Added these extra null checks because of Bug 6848
                    // I can't reproduce it and these should never be null.
                    for (Preparation prep : colObj.getPreparations())
                    {
                        if (prep != null)
                        {
                            if (prep.getLoanPreparations() != null && !prep.getLoanPreparations().isEmpty())
                            {
                                isOK = false;
                                addDeleteReason(LoanPreparation.getClassTableId());
                            }
                            
                            if (prep.getDeaccessionPreparations() != null && !prep.getDeaccessionPreparations().isEmpty())
                            {
                                isOK = false;
                                addDeleteReason(DeaccessionPreparation.getClassTableId());
                            }
                            
                            if (!isOK)
                            {
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectionObjectBusRules.class, ex);
                ex.printStackTrace();
                // Error Dialog
                
            } finally
            {
                if (sessionArg == null && session != null)
                {
                    session.close();
                    session = null;
                }
            }
            
            deletable.doDeleteDataObj(dataObj, session, isOK);
            
        } else
        {
            super.okToDelete(dataObjArg, sessionArg, deletable);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#shouldCloneField(java.lang.String)
     */
    @Override
    public boolean shouldCloneField(String fieldName)
    {
        // Depending on the Type of Discipline the Collecting Events can be shared 
        // a ManyToOne from CollectionObject to Collecting (Fish).
        //
        // Or it acts like a OneToOne where each CE acts as if it is "embedded" or is
        // a part of the CO.
        //System.err.println(fieldName);
        if (fieldName.equals("collectingEvent"))
        {
            // So we need to clone it make a full copy when it is embedded.
            return AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent();
            
        }
        
        if (fieldName.equals("collectionObjectAttribute"))
        {
            return true;
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object, java.lang.Object, boolean)
     */
    @Override
    public STATUS processBusinessRules(final Object parentDataObj, final Object dataObj, final boolean isEdit)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof CollectionObject))
        {
            return STATUS.Error;
        }
        
        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK(CATNUMNAME, 
                                                                (FormDataObjIFace)dataObj, 
                                                                CollectionObject.class, 
                                                                "collectionObjectId");
        
        return duplicateNumberStatus;
    }

    
//    /* (non-Javadoc)
//     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
//     */
//    @Override
//    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session)
//            throws Exception
//    {
//        if (!super.beforeSaveCommit(dataObj, session))
//        {
//            return false;
//        }
//        
//        for (Determination det : ((CollectionObject )dataObj).getDeterminations())
//        {
//            if (det.isCurrent())
//            {
//                return true;
//            }
//        }
//        return false;
//    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToAssociateSearchObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean isOkToAssociateSearchObject(Object parentdataObj, Object dataObjectFromSearch)
    {
        if (parentdataObj instanceof Accession)
        {
            reasonList.clear();
            
            Accession        acc    = (Accession)parentdataObj;
            CollectionObject colObj = (CollectionObject)dataObjectFromSearch;
            
            for (CollectionObject co : acc.getCollectionObjects())
            {
                if (co.getId().equals(colObj.getId()))
                {
                    reasonList.add(getLocalizedMessage("CO_DUP", colObj.getIdentityTitle()));
                    return false;
                }
            }
        } else if (parentdataObj instanceof Project)
        {
            reasonList.clear();
            
            Project          prj    = (Project)parentdataObj;
            CollectionObject colObj = (CollectionObject)dataObjectFromSearch;
            
            for (CollectionObject co : prj.getCollectionObjects())
            {
                if (co.getId().equals(colObj.getId()))
                {
                    reasonList.add(getLocalizedMessage("CO_DUP", colObj.getIdentityTitle()));
                    return false;
                }
            }
        }
        return true;
    }
    
    public static void fixDupColObjAttrs()
    {
        String sql = "SELECT * FROM (SELECT CollectionObjectAttributeID, count(*) as cnt FROM collectionobject c WHERE CollectionObjectAttributeID IS NOT NULL group by CollectionObjectAttributeID) T1 WHERE cnt > 1";
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        if (rows != null)
        {
            for (Object[] row : rows)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    CollectionObjectAttribute colObjAttr = session.get(CollectionObjectAttribute.class, (Integer)row[1]);
                    
                    int cnt = 0;
                    for (CollectionObject co : colObjAttr.getCollectionObjects())
                    {
                        if (cnt > 0)
                        {
                            CollectionObjectAttribute colObjAttribute = (CollectionObjectAttribute)colObjAttr.clone();
                            co.setCollectionObjectAttribute(colObjAttribute);
                            colObjAttribute.getCollectionObjects().add(co);
                            
                            session.beginTransaction();
                            session.saveOrUpdate(colObjAttribute);
                            session.saveOrUpdate(co);
                            session.commit();
                        }
                        cnt++;
                    }
                    
                } catch (Exception ex)
                {
                   session.rollback();
                   ex.printStackTrace();
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
}
