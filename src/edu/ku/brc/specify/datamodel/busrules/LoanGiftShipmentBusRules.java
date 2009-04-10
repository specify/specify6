/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import java.awt.Component;

import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextField;
import edu.ku.brc.specify.datamodel.Shipment;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 26, 2007
 *
 */
public class LoanGiftShipmentBusRules extends BaseBusRules
{

    private static final Logger log = Logger.getLogger(LoanGiftShipmentBusRules.class);

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
                MultiView multiView = formViewObj.getMVParent().getMultiViewParent();
                if (multiView != null)
                {
                    FormViewObj fvo         = multiView.getCurrentViewAsFormViewObj();
                    //Shipment    shipment    = (Shipment)formViewObj.getDataObj();
                    //Loan        loan        = shipment.getLoan();
                    //Gift        gift        = shipment.getGift();
                    //ExchangeOut exchangeOut = shipment.getExchangeOut();
                    //Borrow      borrow      = shipment.getBorrow();
                    
                    String controlName = null;
                    /*if (loan != null)
                    {
                        if (StringUtils.isEmpty(shipment.getShipmentNumber()))
                        {
                            shipment.setShipmentNumber(loan.getLoanNumber());
                        }
                        controlName = "loanNumber";
                        
                    } else if (gift != null)
                    {
                        if (StringUtils.isEmpty(shipment.getShipmentNumber()))
                        {
                            shipment.setShipmentNumber(gift.getGiftNumber());
                        }
                        controlName = "giftNumber";
                        
                    } else if (exchangeOut != null)
                    {
                        controlName = null;
                        
                    } else if (borrow != null)
                    {
                        if (StringUtils.isEmpty(shipment.getShipmentNumber()))
                        {
                            shipment.setShipmentNumber(borrow.getInvoiceNumber());
                        }
                        controlName = "invoiceNumber";
                    }*/
                    
                    if (controlName != null)
                    {
                        Component comp = fvo.getControlByName(controlName);
                        if (comp != null)
                        {
                            String numberStr = comp instanceof ValFormattedTextField ? ((ValFormattedTextField)comp).getText() : ((JTextField)comp).getText();
                            
                            comp = formViewObj.getControlByName("shipmentNumber");
                            if (comp instanceof JTextField)
                            {
                                JTextField tf = (JTextField)comp;
                                if (StringUtils.isEmpty(tf.getText()))
                                {
                                    tf.setText(numberStr);
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
}
