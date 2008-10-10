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
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.specify.datamodel.Gift;

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
        
        if (!(dataObj instanceof Gift))
        {
            return STATUS.Error;
        }
        
        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("giftNumber", 
                                                                (FormDataObjIFace)dataObj, 
                                                                Gift.class, 
                                                                "giftId");

        return duplicateNumberStatus;
    }

}
