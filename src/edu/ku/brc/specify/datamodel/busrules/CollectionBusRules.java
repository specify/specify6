/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Dialog;
import java.awt.Frame;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.init.NumberingSchemeSetupDlg;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.dbsupport.SpecifyDeleteHelper;
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
        
        if (formViewObj != null && formViewObj.getMVParent().isTopLevel())
        {
            ResultSetController rsc = formViewObj.getRsController();
            if (rsc != null)
            {
                if (rsc.getNewRecBtn() != null) rsc.getNewRecBtn().setVisible(false);
                if (rsc.getDelRecBtn() != null) rsc.getDelRecBtn().setVisible(false);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeDelete(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeDelete(dataObj, session);
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
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        Collection   collection = (Collection)dataObj;
        JTextField txt        = (JTextField)formViewObj.getControlById("4");
        if (txt != null && collection != null)
        {
            Set<AutoNumberingScheme> set = collection.getNumberingSchemes();
            if (set != null)
            {
                if (set.size() > 0)
                {
                    AutoNumberingScheme ans = set.iterator().next();
                    txt.setText(ans.getIdentityTitle());
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
        
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            AutoNumberingScheme ns = dlg.getNumScheme();
            ns.setTableNumber(Collection.getClassTableId());
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                if (ns.getId() != null)
                {
                    session.attach(ns);
                } else
                {
                    session.beginTransaction();
                    session.saveOrUpdate(ns);
                    session.commit();
                }
                
                collection.getNumberingSchemes().add(ns);
                ns.getCollections().add(collection);
                
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
                                                      "userGroupScopeId");
        
        STATUS titleStatus = isCheckDuplicateNumberOK("collectionPrefix", 
                                                    (FormDataObjIFace)dataObj, 
                                                    Collection.class, 
                                                    "userGroupScopeId",
                                                    true);
        
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
                Collection currDiscipline = AppContextMgr.getInstance().getClassObject(Collection.class);
                if (currDiscipline.getId().equals(collection.getId()))
                {
                    UIRegistry.showError("You cannot delete the current Collection.");
                    
                } else
                {
                    try
                    {
                        SpecifyDeleteHelper delHelper = new SpecifyDeleteHelper(true);
                        delHelper.delRecordFromTable(Collection.class, collection.getId(), true);
                        delHelper.done();
                        
                        // This is called instead of calling 'okToDelete' because we had the SpecifyDeleteHelper
                        // delete the actual dataObj and now we tell the form to remove the dataObj from
                        // the form's list and them update the controller appropriately
                        
                        formViewObj.updateAfterRemove(true); // true removes item from list and/or set
                        
                    } catch (Exception ex)
                    {
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
}
