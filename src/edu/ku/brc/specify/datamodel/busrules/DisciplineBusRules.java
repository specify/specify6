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
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.specify.config.init.DataBuilder.buildDarwinCoreSchema;
import static edu.ku.brc.specify.config.init.DataBuilder.createGeographyTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createGeologicTimePeriodTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createLithoStratTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createLithoStratTreeDefItem;
import static edu.ku.brc.specify.config.init.DataBuilder.createTaxonTreeDef;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.SwingWorker;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.TextFieldFromPickListTable;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.init.SpecifyDBSetupWizard;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.dbsupport.HibernateDataProviderSession;
import edu.ku.brc.specify.dbsupport.SpecifyDeleteHelper;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Feb 5, 2008
 *
 */
public class DisciplineBusRules extends BaseBusRules implements CommandListener
{
    //private final Logger         log      = Logger.getLogger(DisciplineBusRules.class);
    private static final String CMD_TYPE = "DisciplineBusRules"; 
    private static final String PROGRESS = "progress"; 
    
    private boolean       isOKToCont    = false;
    private ProgressFrame progressFrame = null;
    private int           totalSteps    = 8;
    
    /**
     * 
     */
    public DisciplineBusRules()
    {
        super(Discipline.class);
        CommandDispatcher.register(CMD_TYPE, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            /*if (formViewObj.getMVParent().isTopLevel())
            {
                ResultSetController rsc = formViewObj.getRsController();
                if (rsc != null)
                {
                    if (rsc.getNewRecBtn() != null) rsc.getNewRecBtn().setVisible(false);
                    if (rsc.getDelRecBtn() != null) rsc.getDelRecBtn().setVisible(false);
                }
            }*/
            if (formViewObj.getRsController() != null)
            {
                JButton newBtn = formViewObj.getRsController().getNewRecBtn();
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
                            addNewDiscipline();
                        }
                    });
                }
            }
            
            Component comp = formViewObj.getControlByName("type");
            if (comp instanceof ValComboBox)
            {
                final ValComboBox dspCbx = (ValComboBox)comp;
                for (DisciplineType dt : DisciplineType.getDisciplineList())
                {
                    dspCbx.getComboBox().addItem(dt);
                }
                dspCbx.getComboBox().setSelectedIndex(-1);
                
                dspCbx.getComboBox().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        DisciplineType dt = (DisciplineType)dspCbx.getComboBox().getSelectedItem();
                        if (dt != null)
                        {
                            ((Discipline)formViewObj.getDataObj()).setType(dt.getName());
                        }
                    }
                });
            }
        }
    }
    
    /**
     * 
     */
    private void addNewDiscipline()
    {
        isOKToCont = true;
        
        final SpecifyDBSetupWizard wizardPanel = new SpecifyDBSetupWizard(SpecifyDBSetupWizard.WizardType.Brief, null);
        
        final CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), "", true, CustomDialog.NONE_BTN, wizardPanel);
        dlg.setCustomTitleBar(UIRegistry.getResourceString("CREATEDISP"));
        wizardPanel.setListener(new SpecifyDBSetupWizard.WizardListener() {
            @Override
            public void cancelled()
            {
                isOKToCont = false;
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
        });
        UIHelper.centerAndShow(dlg);
        
        if (!isOKToCont)
        {
            return;
        }
        
        progressFrame = new ProgressFrame("Creating Discipline ...", "SpecifyLargeIcon");
        progressFrame.getCloseBtn().setVisible(false);
        progressFrame.pack();
        Dimension size = progressFrame.getSize();
        size.width = Math.max(size.width, 500);
        progressFrame.setSize(size);
        progressFrame.setProcessPercent(true);
        progressFrame.turnOffOverAll();
        
        progressFrame.setProcess(0, totalSteps);

        UIHelper.centerAndShow(progressFrame);
        
        SwingWorker<Integer, Integer> bldWorker = new SwingWorker<Integer, Integer>()
        {
            private int steps = 0;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                firePropertyChange(PROGRESS, 0, ++steps);
                
                Properties props = wizardPanel.getProps();
                
                DisciplineType            disciplineType = wizardPanel.getDisciplineType();
                
                TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
                GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
                GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
                LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");

                Discipline discipline = new Discipline();
                discipline.initialize();
                
                discipline.setType(disciplineType.getName());
                discipline.setName(props.getProperty("dispName"));
                
                FormViewObj divFVO = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
                Division    div    = (Division)divFVO.getDataObj();

                discipline.setDivision(div);
                
                DataType dataType = AppContextMgr.getInstance().getClassObject(DataType.class);
                discipline.setDataType(dataType);

                taxonTreeDef.setDiscipline(discipline);
                discipline.setTaxonTreeDef(taxonTreeDef);
                
                geoTreeDef.getDisciplines().add(discipline);
                discipline.setGeographyTreeDef(geoTreeDef);

                gtpTreeDef.getDisciplines().add(discipline);
                discipline.setGeologicTimePeriodTreeDef(gtpTreeDef);

                lithoStratTreeDef.getDisciplines().add(discipline);
                discipline.setLithoStratTreeDef(lithoStratTreeDef);
                
                boolean isPaleo = disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.paleobotany ||
                disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.vertpaleo ||
                disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.invertpaleo;

                if (isPaleo)
                {
                    LithoStratTreeDefItem earth     = createLithoStratTreeDefItem(lithoStratTreeDef, "Earth", 0, false);
                    LithoStratTreeDefItem superGrp  = createLithoStratTreeDefItem(earth,     "Super Group", 100, false);
                    LithoStratTreeDefItem lithoGrp  = createLithoStratTreeDefItem(superGrp,  "Litho Group", 200, false);
                    LithoStratTreeDefItem formation = createLithoStratTreeDefItem(lithoGrp,  "Formation",   300, false);
                    LithoStratTreeDefItem member    = createLithoStratTreeDefItem(formation, "Member",      400, false);
                    @SuppressWarnings("unused")
                    LithoStratTreeDefItem bed       = createLithoStratTreeDefItem(member,    "Bed",         500, true);
                    //persist(earth);
                }
                
                List<Object> geos        = new Vector<Object>();
                List<Object> gtps        = BuildSampleDatabase.createSimpleGeologicTimePeriod(gtpTreeDef, false);
                List<Object> lithoStrats = isPaleo ? null : BuildSampleDatabase.createSimpleLithoStrat(lithoStratTreeDef, false);
                
                // Create Tree Definition
                Vector<Object> taxa = new Vector<Object>();
                taxonTreeDef.setDiscipline(discipline);
                taxa.add(taxonTreeDef);
                
                BuildSampleDatabase.createTaxonDefFromXML(taxa, null, taxonTreeDef, props.getProperty("TaxonTreeDef.treedefs"));
                
                BuildSampleDatabase.createGeographyDefFromXML(geos, geoTreeDef, props.getProperty("GeographyTreeDef.treedefs"));
                
                HibernateDataProviderSession session     = null;
                try
                {
                    session = (HibernateDataProviderSession)DataProviderFactory.getInstance().createSession();
                    session.beginTransaction();
                    
                    session.saveOrUpdate(discipline);
                    
                    persist(taxa, session);
                    persist(geos, session);
                    persist(gtps, session);
                    persist(lithoStrats, session);
                    
                    firePropertyChange(PROGRESS, 0, ++steps);
                    
                    BuildSampleDatabase bsd = new BuildSampleDatabase();
                    bsd.setSession(session.getSession());
                    bsd.convertGeographyFromXLS(geoTreeDef);
                    
                    firePropertyChange(PROGRESS, 0, ++steps);
                    
                    session.commit();
                    
                    firePropertyChange(PROGRESS, 0, ++steps);
                    
                    bsd.localizeDisciplineSchema(discipline, props);
                    
                    firePropertyChange(PROGRESS, 0, ++steps);
                    
                    session.close();
                    
                    session = (HibernateDataProviderSession)DataProviderFactory.getInstance().createSession();
                    
                    session.beginTransaction();
                    
                    firePropertyChange(PROGRESS, 0, ++steps);
                    
                    List<?> list = session.createQuery("FROM Discipline WHERE id = "+discipline.getId(), false).list();
                    
                    discipline = (Discipline)list.get(0);
                    
                    firePropertyChange(PROGRESS, 0, ++steps);
                    
                    buildDarwinCoreSchema(discipline);
                    
                    firePropertyChange(PROGRESS, 0, ++steps);
                    
                    session.commit();
                    
                } catch (Exception ex)
                {
                    session.rollback();
                    
                    System.err.println(ex);
                    ex.printStackTrace();
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }

                progressFrame.setProcess(totalSteps);
                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                progressFrame.setVisible(false);
                progressFrame.dispose();
                progressFrame = null;
                
                List<?> dataItems = null;
                
                FormViewObj divFVO = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
                Division div = (Division)divFVO.getDataObj();
                
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    
                    div = (Division)session.getData("FROM Division WHERE id = "+div.getId());
                    
                    dataItems = session.getDataList("FROM Division");
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
                    if (session != null)
                    {
                        session.close();
                    }
                }
                
                int curInx = divFVO.getRsController().getCurrentIndex();
                divFVO.setDataObj(dataItems);
                divFVO.getRsController().setIndex(curInx);

            }
        };
        
        bldWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (PROGRESS.equals(evt.getPropertyName())) 
                        {
                            progressFrame.setProcess((Integer)evt.getNewValue());
                        }
                    }
                });
        bldWorker.execute();
    }
    
    /**
     * @param oList
     * @param session
     * @throws Exception
     */
    private void persist(final List<Object> oList, final DataProviderSessionIFace session) throws Exception
    {
        if (oList != null)
        {
            for (Object o: oList)
            {
                session.saveOrUpdate(o);
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
        
        if (formViewObj != null && dataObj != null)
        {
            Discipline discipline = (Discipline)dataObj;
            Component  comp       = formViewObj.getControlByName("type");
            if (comp instanceof TextFieldFromPickListTable)
            {
                TextFieldFromPickListTable tf = (TextFieldFromPickListTable)comp;
                tf.setValue(DisciplineType.getByName(discipline.getType()), "");
                
            } else if (comp instanceof ValTextField && discipline.getType() != null)
            {
                ValTextField tf = (ValTextField)comp;
                tf.setValue(DisciplineType.getByName(discipline.getType()), "");
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        CommandDispatcher.unregister(CMD_TYPE, this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object dataObj)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof Discipline))
        {
            reasonList.add("Object is of wrong Class.");
            return STATUS.Error;
        }
        
        STATUS nameStatus = isCheckDuplicateNumberOK("name",
                                                    (FormDataObjIFace)dataObj, 
                                                    Discipline.class, 
                                                    "userGroupScopeId");
        
        return nameStatus != STATUS.OK ? STATUS.Error : STATUS.OK;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object dataObj,
                           final DataProviderSessionIFace session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        if (deletable != null)
        {
            Discipline discipline = (Discipline)dataObj;
            
            Integer id = discipline.getId();
            if (id != null)
            {
                Discipline currDiscipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
                if (currDiscipline.getId().equals(discipline.getId()))
                {
                    UIRegistry.showError("You cannot delete the current Discipline."); // I18N
                    
                } else
                {
                    try
                    {
                        SpecifyDeleteHelper delHelper = new SpecifyDeleteHelper(true);
                        delHelper.delRecordFromTable(Discipline.class, discipline.getId(), true);
                        delHelper.done();
                        
                        // This is called instead of calling 'okToDelete' because we had the SpecifyDeleteHelper
                        // delete the actual dataObj and now we tell the form to remove the dataObj from
                        // the form's list and them update the controller appropriately
                        formViewObj.updateAfterRemove(true); // true removes item from list and/or set
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DisciplineBusRules.class, ex);
                        ex.printStackTrace();
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
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        Discipline discipline = (Discipline)dataObj;
        if (formViewObj != null && dataObj != null)
        {
            Component comp = formViewObj.getControlByName("type");
            if (comp instanceof ValComboBox)
            {
                ValComboBox dspCbx = (ValComboBox)comp;
                DisciplineType dt = (DisciplineType)dspCbx.getComboBox().getSelectedItem();
                if (dt != null)
                {
                    discipline.setType(dt.getName());
                }
            }
            
            for (Collection col : discipline.getCollections())
            {
                if (col.getId() == null)
                {
                    try
                    {
                        for (AutoNumberingScheme ns : col.getNumberingSchemes())
                        {
                            session.saveOrUpdate(ns);
                        }
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj,session);
        
        Discipline discipline = (Discipline)dataObj;
        if (discipline.getTaxonTreeDef() == null)
        {
            TaxonTreeDef taxonTreeDef = createTaxonTreeDef("Sample Taxon Tree Def");
            discipline.setTaxonTreeDef(taxonTreeDef);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSaveCommit(java.lang.Object)
     */
    @Override
    public boolean afterSaveCommit(final Object dataObj, final DataProviderSessionIFace session)
    {
        Discipline discipline = (Discipline)dataObj;
        for (Collection collection : discipline.getCollections())
        {
            int count = BasicSQLUtils.getCount("SELECT count(*) FROM picklist WHERE CollectionID = " + collection.getId());
            if (count == 0)
            {
                try
                {
                    if (session instanceof HibernateDataProviderSession)
                    {
                        HibernateDataProviderSession hibSession = (HibernateDataProviderSession)session;
                        BuildSampleDatabase.createPickLists(hibSession.getSession(), null, false, collection); // do 'common' first
                        BuildSampleDatabase.createPickLists(hibSession.getSession(), collection.getDiscipline(), false, collection);
                    }
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DisciplineBusRules.class, ex);
                    ex.printStackTrace();
                    UsageTracker.incrNetworkUsageCount();
                }
            }
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Discipline)
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(Discipline.getClassTableId());
            return getLocalizedMessage("DISCIPLINE_DELETED", ti.getTitle());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        SpecifyDBSetupWizard wizardPanel = new SpecifyDBSetupWizard(SpecifyDBSetupWizard.WizardType.Brief, null);
        
        final CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), "", true, CustomDialog.NONE_BTN, wizardPanel);
        dlg.setCustomTitleBar(UIRegistry.getResourceString("CREATEDISP"));
        wizardPanel.setListener(new SpecifyDBSetupWizard.WizardListener() {
            @Override
            public void cancelled()
            {
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
        });
        UIHelper.centerAndShow(dlg);
        
        //Properties prop = wizardPanel.getProps();
        
        DisciplineType            disciplineType = wizardPanel.getDisciplineType();
        
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");

        Discipline discipline = (Discipline)newDataObj;
        
        discipline.setType(disciplineType.getName());
        
        DataType dataType = AppContextMgr.getInstance().getClassObject(DataType.class);
        discipline.setDataType(dataType);

        taxonTreeDef.setDiscipline(discipline);
        discipline.setTaxonTreeDef(taxonTreeDef);
        
        geoTreeDef.getDisciplines().add(discipline);
        discipline.setGeographyTreeDef(geoTreeDef);

        gtpTreeDef.getDisciplines().add(discipline);
        discipline.setGeologicTimePeriodTreeDef(gtpTreeDef);

        lithoStratTreeDef.getDisciplines().add(discipline);
        discipline.setLithoStratTreeDef(lithoStratTreeDef);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeDelete(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeDelete(dataObj, session);
        
        /*Discipline discipline = (Discipline)dataObj;
        Integer dspId = discipline.getId();
        
        Statement stmt = null;
        try
        {
            stmt = DBConnection.getInstance().getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

            List<Integer> ids = new Vector<Integer>();
            ResultSet rs = stmt.executeQuery("SELECT SpAppResourceDirID FROM spappresourcedir WHERE DisciplineID = "+dspId);
            while (rs.next())
            {
                ids.add(rs.getInt(1));
            }         
            rs.close();
            
            if (ids.size() > 0)
            {
                for (Integer id : ids)
                {
                    SpAppResourceDir obj = session.get(SpAppResourceDir.class, id);
                    if (obj != null)
                    {
                        System.err.println(obj.getIdentityTitle());
                        session.delete(obj);
                    }
                }
            }
            
            ids = new Vector<Integer>();
            rs = stmt.executeQuery("SELECT CollectingTripID FROM collectingtrip WHERE DisciplineID = "+dspId);
            while (rs.next())
            {
                ids.add(rs.getInt(1));
            }         
            rs.close();
            
            if (ids.size() > 0)
            {
                for (Integer id : ids)
                {
                    CollectingTrip obj = session.get(CollectingTrip.class, id);
                    if (obj != null)
                    {
                        System.err.println(obj.getIdentityTitle());
                        session.delete(obj);
                    }
                }
            }
            
            ids = new Vector<Integer>();
            rs = stmt.executeQuery("SELECT CollectionID FROM collection WHERE DisciplineID = "+dspId);
            while (rs.next())
            {
                ids.add(rs.getInt(1));
            }         
            rs.close();
            
            for (Integer id : ids)
            {
                rs  = stmt.executeQuery("select CollectionObjectID from collectionobject where CollectionMemberID = "+id);
                while (rs.next())
                {
                    CollectionObject obj = session.get(CollectionObject.class, rs.getInt(1));
                    if (obj != null)
                    {
                        System.err.println(obj.getIdentityTitle());
                        session.delete(obj);
                    }

                }
                rs.close();
            }
            stmt.close();
    
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DisciplineBusRules.class, ex);
            ex.printStackTrace();
            //log.error(ex);
            throw new RuntimeException(ex);
            
        } finally
        {
            try
            {
                stmt.close();
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DisciplineBusRules.class, ex);
                ex.printStackTrace();
                //log.error(ex);
                throw new RuntimeException(ex);
            }
        }

*/
    }
    
    /*public void disciplinehasBeenAdded(Division division, final Discipline discipline)
    {
        
    }*/

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isAction("DivisionSaved"))
        {
            Division divsion = (Division)cmdAction.getData();
            formViewObj.getMVParent().getMultiViewParent().setData(divsion);
            
        } else if (cmdAction.isAction("DivisionError"))
        {
        }
        
    }
    
}
