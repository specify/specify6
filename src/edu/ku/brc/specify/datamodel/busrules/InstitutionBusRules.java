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

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Institution;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 18, 2008
 *
 */
public class InstitutionBusRules extends BaseBusRules
{

    /**
     * @param dataClasses
     */
    public InstitutionBusRules()
    {
        super(Institution.class);
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
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean afterSaveCommit(Object dataObj, DataProviderSessionIFace session)
    {
        AppContextMgr.getInstance().setClassObject(Institution.class, dataObj);
        
        return super.afterSaveCommit(dataObj, session);
    }
    
    
}
