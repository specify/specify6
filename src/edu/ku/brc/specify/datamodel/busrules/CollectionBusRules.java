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
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.init.SpecifyDBSetupWizard;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.dbsupport.HibernateDataProviderSession;
import edu.ku.brc.specify.dbsupport.SpecifyDeleteHelper;
import edu.ku.brc.specify.ui.DisciplineBasedUIFieldFormatterMgr;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Jan 11, 2008
 *
 */
public class CollectionBusRules extends BaseBusRules
{
    private boolean       isOKToCont    = false;
    private HashMap<Integer, DisciplineBasedUIFieldFormatterMgr> fmtHash = new HashMap<Integer, DisciplineBasedUIFieldFormatterMgr>();

    /**
     * Constructor.
     */
    public CollectionBusRules()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        JButton newBtn = null;
            
        if (formViewObj != null)
        {
            ResultSetController rsc = formViewObj.getRsController();
            if (rsc != null)
            {
                if (formViewObj.getMVParent().isTopLevel())
                {
                    if (rsc.getNewRecBtn() != null) rsc.getNewRecBtn().setVisible(false);
                    if (rsc.getDelRecBtn() != null) rsc.getDelRecBtn().setVisible(false);
                } else 
                {
                    newBtn = rsc.getNewRecBtn();
                }
            }
            
            ValCheckBox chkBox = formViewObj.getCompById("6");
            boolean showColEventChkbx = AppPreferences.getLocalPrefs().getBoolean("show.cecheckbox", false);
            chkBox.setVisible(showColEventChkbx);
        }
        
        if (newBtn != null)
        {
            // Remove all ActionListeners, there should only be one
            for (ActionListener al : newBtn.getActionListeners())
            {
                newBtn.removeActionListener(al);
            }
            
            newBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    addNewCollection();
                }
            });
        }
    }
    
    /**
     * @param disciplineName
     * @return
     */
    private int getNameCount(final String name)
    {
        return BasicSQLUtils.getCountAsInt(String.format("SELECT COUNT(*) FROM collection WHERE CollectionName = '%s'", name));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean isOkToSave(final Object dataObj, final DataProviderSessionIFace session)
    {
        reasonList.clear();
        
        if (formViewObj != null)
        {
            Component comp = formViewObj.getControlByName("collectionName");
            if (comp instanceof ValTextField)
            {
                Collection collection = (Collection)formViewObj.getDataObj();
                Integer    colId      = collection.getId();
                
                String name = ((ValTextField)comp).getText();
                int    cnt  = getNameCount(name);
                if (cnt == 0 || (cnt == 1 && colId != null))
                {
                    return true;
                }
               reasonList.add(UIRegistry.getLocalizedMessage("COLLNAME_DUP", name));
            }
        }
        return false;
    }

    /**
     * 
     */
    private void addNewCollection()
    {
        if (!DivisionBusRules.checkForParentSave(formViewObj, Discipline.getClassTableId()))
        {
            return;
        }
        
        UIRegistry.loadAndPushResourceBundle("specifydbsetupwiz");
        
        UIRegistry.writeSimpleGlassPaneMsg("Building Collection...", 20); // I18N
        isOKToCont = true;
        final AppContextMgr acm = AppContextMgr.getInstance();
        
        final SpecifyDBSetupWizard wizardPanel = new SpecifyDBSetupWizard(SpecifyDBSetupWizard.WizardType.Collection, null);
        
        String msg = UIRegistry.getResourceString("CREATECOLL");
        final CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), "", true, CustomDialog.NONE_BTN, wizardPanel);
        dlg.setCustomTitleBar(msg);
        wizardPanel.setListener(new SpecifyDBSetupWizard.WizardListener() {
            @Override
            public void cancelled()
            {
                isOKToCont = false;
                dlg.setVisible(false);
            }
            @Override
            public void finished()
            {
                dlg.setVisible(false);
            }
            @Override
            public void hide()
            {
                dlg.setVisible(false);
            }
            @Override
            public void panelChanged(String title)
            {
                dlg.setTitle(title);
            }
            @Override
            public void helpContextChanged(String helpTarget)
            {
                
            }
        });
        dlg.createUI();
        dlg.setVisible(true);
        
        UIRegistry.popResourceBundle();
        
        if (!isOKToCont)
        {
            UIRegistry.clearSimpleGlassPaneMsg();
            return;
        }
        
        wizardPanel.processDataForNonBuild();
        
        final BuildSampleDatabase bldSampleDB   = new BuildSampleDatabase();
        final ProgressFrame       progressFrame = bldSampleDB.createProgressFrame(msg); // I18N
        progressFrame.turnOffOverAll();
        
        progressFrame.setProcess(0, 4);
        progressFrame.setProcessPercent(true);
        progressFrame.getCloseBtn().setVisible(false);
        progressFrame.setAlwaysOnTop(true);
        progressFrame.adjustProgressFrame();
        
        UIHelper.centerAndShow(progressFrame);
        
        SwingWorker<Integer, Integer> bldWorker = new SwingWorker<Integer, Integer>()
        {
            Collection newCollection = null;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                Session session = null;
                try
                {
                    session = HibernateUtil.getNewSession();
                    DataProviderSessionIFace hSession = new HibernateDataProviderSession(session);
                    
                    Discipline     discipline       = (Discipline)formViewObj.getMVParent().getMultiViewParent().getData();
                    //Institution    institution      = acm.getClassObject(Institution.class);
                    SpecifyUser    specifyAdminUser = acm.getClassObject(SpecifyUser.class);
                    Agent          userAgent        = (Agent)hSession.getData("FROM Agent WHERE id = "+Agent.getUserAgent().getId());
                    Properties     props            = wizardPanel.getProps();
                    DisciplineType disciplineType   = DisciplineType.getByName(discipline.getType());
                    
                    discipline       = (Discipline)session.merge(discipline);
                    specifyAdminUser = (SpecifyUser)hSession.getData("FROM SpecifyUser WHERE id = "+specifyAdminUser.getId());
                    
                    bldSampleDB.setSession(session);
                    
                    AutoNumberingScheme catNumScheme = bldSampleDB.createAutoNumScheme(props, "catnumfmt", "Catalog Numbering Scheme", CollectionObject.getClassTableId());
                    /*AutoNumberingScheme accNumScheme = null;

                    if (institution.getIsAccessionsGlobal())
                    {
                        List<?> list = hSession.getDataList("FROM AutoNumberingScheme WHERE tableNumber = "+Accession.getClassTableId());
                        if (list != null && list.size() == 1)
                        {
                            accNumScheme = (AutoNumberingScheme)list.get(0);
                        }
                    } else
                    {
                        accNumScheme = bldSampleDB.createAutoNumScheme(props, "accnumfmt", "Accession Numbering Scheme", Accession.getClassTableId()); // I18N
                    }*/
                    
                    newCollection = bldSampleDB.createEmptyCollection(discipline, 
                                                                      props.getProperty("collPrefix").toString(), 
                                                                      props.getProperty("collName").toString(),
                                                                      userAgent,
                                                                      specifyAdminUser,
                                                                      catNumScheme,
                                                                      disciplineType.isEmbeddedCollecingEvent());
                            
                    acm.setClassObject(SpecifyUser.class, specifyAdminUser);
                    Agent.setUserAgent(userAgent);
                    
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
                
                bldSampleDB.setDataType(null);
                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                progressFrame.setVisible(false);
                progressFrame.dispose();
                
               if (newCollection != null)
               {
                   List<?> dataItems = null;
                   
                   FormViewObj   dispFVO    = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
                   Discipline    discipline = (Discipline)dispFVO.getDataObj();
                   DataProviderSessionIFace pSession = null;
                   try
                   {
                       pSession = DataProviderFactory.getInstance().createSession();
                       
                       discipline = (Discipline)pSession.getData("FROM Discipline WHERE id = "+discipline.getId());
                       //formViewObj.getMVParent().getMultiViewParent().setData(division);
                       acm.setClassObject(Discipline.class, discipline);
                       
                       dataItems = pSession.getDataList("FROM Discipline");
                       if (dataItems.get(0) instanceof Object[])
                       {
                           Vector<Object>dataList = new Vector<Object>();
                           for (Object row : dataItems)
                           {
                               Object[] cols = (Object[])row;
                               dataList.add(cols[0]);
                           }
                           dataItems = dataList;
                       }
                       
                   } catch (Exception ex)
                   {
                       System.err.println(ex);
                       ex.printStackTrace();
                       
                   } finally
                   {
                       if (pSession != null)
                       {
                           pSession.close();
                       }
                   }
                   
                   int curInx = dispFVO.getRsController().getCurrentIndex();
                   dispFVO.setDataObj(dataItems);
                   dispFVO.getRsController().setIndex(curInx);
                   
                   //UIRegistry.clearSimpleGlassPaneMsg();
                   
                   UIRegistry.showLocalizedMsg("Specify.ABT_EXIT");
                   CommandDispatcher.dispatch(new CommandAction(BaseTask.APP_CMD_TYPE, BaseTask.APP_REQ_EXIT));
         
               } else
               {
                   // error creating
               }
               UIRegistry.clearSimpleGlassPaneMsg();
            }
        };
        
        bldWorker.execute();

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public Object beforeDelete(Object dataObj, DataProviderSessionIFace session)
    {
        return super.beforeDelete(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        Collection collection = (Collection)dataObj;
        
        for (AutoNumberingScheme ans : collection.getNumberingSchemes())
        {
            try
            {
                session.saveOrUpdate(ans);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectionBusRules.class, ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        beforeMerge(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        Collection  collection         = (Collection)dataObj;
        Discipline  appCntxtDiscipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
        
        JTextField txt = (JTextField)formViewObj.getControlById("4");
        if (txt != null && collection != null && collection.getId() != null)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                Collection coll = session.get(Collection.class, collection.getId());
                if (coll != null)
                {
                    Discipline  discipline = coll.getDiscipline();
                    Set<AutoNumberingScheme> set = coll.getNumberingSchemes();
                    if (set != null)
                    {
                        if (set.size() > 0)
                        {
                            UIFieldFormatterMgr ffMgr = null;
                            if (discipline.getId().equals(appCntxtDiscipline.getId()))
                            {
                                ffMgr = UIFieldFormatterMgr.getInstance();
                            } else
                            {
                                Integer dispId = coll.getDiscipline().getId();
                                
                                DisciplineBasedUIFieldFormatterMgr tempFFMgr = fmtHash.get(dispId);
                                if (tempFFMgr == null)
                                {
                                    tempFFMgr = new DisciplineBasedUIFieldFormatterMgr(dispId);
                                    tempFFMgr.load();
                                    fmtHash.put(dispId, tempFFMgr);
                                }
                                ffMgr = tempFFMgr;
                            }
                            AutoNumberingScheme ans = set.iterator().next();
                            if (ans != null)
                            {
                                UIFieldFormatterIFace fmt = ffMgr.getFormatter(ans.getFormatName());
                                txt.setText(ans.getIdentityTitle()+ (fmt != null ? (" ("+fmt.toPattern()+")") : ""));
                            }
                        }
                    }

                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectionBusRules.class, ex);
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        /*
        Collection collection = (Collection)newDataObj;
        
        MultiView  disciplineMV = formViewObj.getMVParent().getMultiViewParent();
        Discipline discipline   = (Discipline)disciplineMV.getData();
        
        NumberingSchemeSetupDlg dlg;
        if (UIRegistry.getMostRecentWindow() instanceof Dialog)
        {
            dlg = new NumberingSchemeSetupDlg((Dialog)UIRegistry.getMostRecentWindow(), 
                    discipline.getDivision(), 
                    discipline, 
                    (Collection)newDataObj);
        } else
        {
            dlg = new NumberingSchemeSetupDlg((Frame)UIRegistry.getMostRecentWindow(), 
                    discipline.getDivision(), 
                    discipline, 
                    (Collection)newDataObj);
        }
        dlg.setCustomTitleBar(UIRegistry.getResourceString("SEL_NUM_SCHEME"));
        
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            AutoNumberingScheme autoNumScheme = dlg.getNumScheme();
            autoNumScheme.setTableNumber(CollectionObject.getClassTableId());
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                if (autoNumScheme.getId() != null)
                {
                    session.attach(autoNumScheme);
                } else
                {
                    session.beginTransaction();
                    session.saveOrUpdate(autoNumScheme);
                    session.commit();
                }
                
                collection.setCatalogNumFormatName(autoNumScheme.getFormatName());
                collection.getNumberingSchemes().add(autoNumScheme);
                autoNumScheme.getCollections().add(collection);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectionBusRules.class, ex);
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }*/
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof Collection))
        {
            return STATUS.Error;
        }
        
        STATUS nameStatus = isCheckDuplicateNumberOK("collectionName", 
                                                      (FormDataObjIFace)dataObj, 
                                                      Collection.class, 
                                                      "userGroupScopeId",
                                                      false); // Use Special (check Discipline ID)
        
        STATUS titleStatus = isCheckDuplicateNumberOK("collectionPrefix", 
                                                    (FormDataObjIFace)dataObj, 
                                                    Collection.class, 
                                                    "userGroupScopeId",
                                                    true,   // isEmptyOK
                                                    false); // Use Special (check Discipline ID)
        
        return nameStatus != STATUS.OK || titleStatus != STATUS.OK ? STATUS.Error : STATUS.OK;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(final Object dataObj)
    {
        if (dataObj != null)
        {
            Collection col     = AppContextMgr.getInstance().getClassObject(Collection.class);
            Collection dataCol = (Collection)dataObj;
            if (col.getId() != null && dataCol.getId() != null && col.getId().equals(dataCol.getId()))
            {
                return false;
            }
    
            reasonList.clear();
            
            boolean isOK =  okToDelete("collectionobject", "CollectionID", ((FormDataObjIFace)dataObj).getId());
            if (!isOK)
            {
                return false;
            }
            
            Collection collection = (Collection)dataObj;
            
            String colMemName = "CollectionMemberID";
            
            Vector<String> tableList = new Vector<String>();
            for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
            {
                for (DBFieldInfo fi : ti.getFields())
                {
                    String colName = fi.getColumn();
                    if (StringUtils.isNotEmpty(colName) && colName.equals(colMemName))
                    {
                        tableList.add(ti.getName());
                        break;
                    }
                }
            }
            
            int inx = 0;
            String[] tableFieldNamePairs = new String[tableList.size() * 2];
            for (String tableName : tableList)
            {
                tableFieldNamePairs[inx++] = tableName;
                tableFieldNamePairs[inx++] = colMemName;
            }
            isOK = okToDelete(tableFieldNamePairs, collection.getId());
            
            return isOK;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object                     dataObj,
                           final DataProviderSessionIFace   session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        if (deletable != null)
        {
            Collection collection = (Collection)dataObj;
            
            Integer id = collection.getId();
            if (id != null)
            {
                Collection currCollection = AppContextMgr.getInstance().getClassObject(Collection.class);
                if (currCollection.getId().equals(collection.getId())) 
                {
                    UIRegistry.showLocalizedError("CollectionBusRule.CURR_COL_ERR");
                    
                } else
                {
                    DataProviderSessionIFace pSession = null;
                    try
                    {
                        pSession = session != null ? session : DataProviderFactory.getInstance().createSession();
                        
                        pSession.attach(collection);
                        
                        if (collection.getLeftSideRelTypes().size() > 0 ||
                            collection.getRightSideRelTypes().size() > 0)
                        {
                            
                            if (pSession != null && session == null)
                            {
                                pSession.close();
                            }
                            UIRegistry.showLocalizedError("CollectionBusRule.RELS_ERR");
                            return;
                        }
                        
                        pSession.beginTransaction();
                        
                        
                        Set<AutoNumberingScheme> colANSSet = collection.getNumberingSchemes();
                        for (AutoNumberingScheme ans : new Vector<AutoNumberingScheme>(colANSSet))
                        {
                            pSession.attach(ans);
                            //System.out.println("Removing: "+ans.getSchemeName()+", "+ans.getFormatName()+" "+ans.getTableNumber()+" disp: "+ans.getDisciplines().size()+" div: "+ans.getDivisions().size());
                        }
                        //System.out.println("----------------------");
                        
                        for (AutoNumberingScheme ans : new Vector<AutoNumberingScheme>(colANSSet))
                        {
                            //System.out.println("Removing: "+ans.getSchemeName()+", "+ans.getFormatName()+" "+ans.getTableNumber()+" "+ans.getDisciplines().size()+" "+ans.getDivisions().size());
                            
                            pSession.attach(ans);
                            
                            colANSSet.remove(ans);
                            ans.getCollections().remove(collection);
                            
                            if (ans.getCollections().size() == 0)
                            {
                                pSession.delete(ans);
                            }
                        }
                        pSession.saveOrUpdate(collection);
                        pSession.commit();
                        
                        final Integer             collId    = collection.getId();
                        final SpecifyDeleteHelper delHelper = new SpecifyDeleteHelper();
                        
                        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
                        {
                            /* (non-Javadoc)
                             * @see javax.swing.SwingWorker#doInBackground()
                             */
                            @Override
                            protected Integer doInBackground() throws Exception
                            {
                                try
                                {
                                    delHelper.delRecordFromTable(Collection.class, collId, true);
                                    delHelper.done(false);
                                    
                                } catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DivisionBusRules.class, ex);
                                }
                                
                                return null;
                            }
    
                            /* (non-Javadoc)
                             * @see javax.swing.SwingWorker#done()
                             */
                            @Override
                            protected void done()
                            {
                                super.done();
                                
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                     // This is called instead of calling 'okToDelete' because we had the SpecifyDeleteHelper
                                        // delete the actual dataObj and now we tell the form to remove the dataObj from
                                        // the form's list and them update the controller appropriately
                                        if (formViewObj != null)
                                        {
                                            formViewObj.updateAfterRemove(true); // true removes item from list and/or set
                                        }
                                        
                                        UIRegistry.showLocalizedMsg("Specify.ABT_EXIT");
                                        CommandDispatcher.dispatch(new CommandAction(BaseTask.APP_CMD_TYPE, BaseTask.APP_REQ_EXIT));
                                    }
                                });
                            }
                        };
                        
                        String title = String.format("%s %s %s", getResourceString("DELETING"), 
                                DBTableIdMgr.getInstance().getTitleForId(Collection.getClassTableId()), collection.getCollectionName());
                        
                        JDialog dlg = delHelper.initProgress(worker, title);
    
                        worker.execute();
                        
                        UIHelper.centerAndShow(dlg);
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DisciplineBusRules.class, ex);
                    }
                }
            } else
            {
                super.okToDelete(dataObj, session, deletable);
            }
        } else
        {
            super.okToDelete(dataObj, session, deletable);
        }
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean afterSaveCommit(Object dataObj, DataProviderSessionIFace session)
    {
        AppContextMgr.getInstance().setClassObject(Collection.class, dataObj);
        
        return super.afterSaveCommit(dataObj, session);
    }
    
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#aboutToShutdown()
     */
    @Override
    public void aboutToShutdown()
    {
        super.aboutToShutdown();
        
        for (DisciplineBasedUIFieldFormatterMgr mgr : fmtHash.values())
        {
            mgr.shutdown();
        }
    }
}
