/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.specify.datamodel.LoanPreparation;

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
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (dataObj != null)
        {
            LoanPreparation loanPrep = (LoanPreparation)dataObj;
            
            Component comp = formViewObj.getControlByName("quantityReturned");
            if (comp instanceof ValSpinner)
            {
                boolean    isNewObj         = loanPrep.getId() == null;
                ValSpinner quantityReturned = (ValSpinner)comp;
                ValSpinner quantity         = (ValSpinner)formViewObj.getControlByName("quantity");
                
                quantity.setRange(0, loanPrep.getQuantity(), loanPrep.getQuantity());
                
                quantityReturned.setEnabled(!isNewObj);
                int max = Math.max(loanPrep.getQuantity(), loanPrep.getQuantityReturned());
                quantityReturned.setRange(0, max, loanPrep.getQuantityReturned());
                formViewObj.getLabelFor(quantityReturned).setEnabled(!isNewObj);
                
                ValCheckBox isResolved = (ValCheckBox)formViewObj.getControlByName("isResolved");
                isResolved.setEnabled(!isNewObj);
            }
        }
    }
}
