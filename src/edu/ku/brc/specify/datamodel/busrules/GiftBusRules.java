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

import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.ui.forms.BaseBusRules;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 2, 2008
 *
 */
public class GiftBusRules extends BaseBusRules
{

    /**
     * @param dataClasses
     */
    public GiftBusRules()
    {
        super(GiftBusRules.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof Permit))
        {
            return STATUS.Error;
        }
        
        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("giftNumber", 
                                                                (FormDataObjIFace)dataObj, 
                                                                Permit.class, 
                                                                "giftId");

        return duplicateNumberStatus;
    }

}
