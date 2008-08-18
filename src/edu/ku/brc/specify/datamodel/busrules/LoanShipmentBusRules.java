/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;

import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextField;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.Shipment;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 26, 2007
 *
 */
public class LoanShipmentBusRules extends BaseBusRules
{

    private static final Logger log = Logger.getLogger(LoanShipmentBusRules.class);

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        if (viewable instanceof FormViewObj)
        {
            if (formViewObj.getDataObj() instanceof Shipment)
            {
                MultiView loanMV = formViewObj.getMVParent().getMultiViewParent();
                if (loanMV != null)
                {
                    FormViewObj loanFVO  = loanMV.getCurrentViewAsFormViewObj();
                    
                    Shipment  shipment   = (Shipment)formViewObj.getDataObj();
                    Loan      loan       = shipment.getLoan();
                    
                    //boolean   isNewObj = MultiView.isOptionOn(mvParent.getOptions(), MultiView.IS_NEW_OBJECT);
                    //boolean   isEdit   = mvParent.isEditable();
                    
                    if (StringUtils.isEmpty(shipment.getShipmentNumber()))
                    {
                        shipment.setShipmentNumber(loan.getLoanNumber());
                    }
                    //System.err.println(loan.getLoanNumber());
                    
                    Component comp = loanFVO.getControlByName("loanNumber");
                    if (comp != null)
                    {
                        String loanNum = comp instanceof ValFormattedTextField ? ((ValFormattedTextField)comp).getText() : ((JTextField)comp).getText();
                        
                        comp = formViewObj.getControlByName("shipmentNumber");
                        if (comp instanceof JTextField)
                        {
                            JTextField tf = (JTextField)comp;
                            if (StringUtils.isEmpty(tf.getText()))
                            {
                                tf.setText(loanNum);
                            }
                        } else
                        {
                            log.error("Couldn't find UI control 'shipmentNumber' on the Loan Form");
                        }
                        
                    } else
                    {
                        log.error("Couldn't find UI control 'loanNumber' on the Loan Form");
                    }
                }
            }
        }
    }
}
