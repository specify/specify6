/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.ui.forms.BaseBusRules;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 29, 2007
 *
 */
public class LoanPreparationBusRules extends BaseBusRules
{
    /**
     * 
     */
    public LoanPreparationBusRules()
    {
        super(LoanPreparation.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return false;
    }

}
