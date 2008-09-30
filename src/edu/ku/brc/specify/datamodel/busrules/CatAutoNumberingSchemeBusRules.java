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

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 29, 2008
 *
 */
public class CatAutoNumberingSchemeBusRules extends BaseBusRules
{

    /**
     * @param dataClasses
     */
    public CatAutoNumberingSchemeBusRules()
    {
        super(AutoNumberingScheme.class);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
    }
    
}
