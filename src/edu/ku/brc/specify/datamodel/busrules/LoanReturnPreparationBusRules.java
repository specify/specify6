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

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 29, 2007
 *
 */
public class LoanReturnPreparationBusRules extends BaseBusRules
{
    private boolean     isFillingForm    = false;
    private FormViewObj loanPrepFVO      = null;
    private FormViewObj loanFVO          = null;
    
    private LoanReturnPreparation  prevLoanRetPrep = null;
    
    /**
     * 
     */
    public LoanReturnPreparationBusRules()
    {
        super(LoanReturnPreparation.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null && formViewObj.getRsController() != null)
        {
            formViewObj.setSkippingAttach(true);
            loanPrepFVO = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
            loanFVO     = loanPrepFVO.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
            
            Component comp = formViewObj.getControlByName("quantityReturned");
            if (comp instanceof ValSpinner)
            {
                final ValSpinner quantityReturned = (ValSpinner)comp;
                final ValSpinner quantityResolved = (ValSpinner)formViewObj.getControlByName("quantityResolved");
                ChangeListener cl = new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        if (!isFillingForm)
                        {
                            updateLoanPrepQuantities(null, e);
                        }
                    }
                };
                quantityReturned.addChangeListener(cl);
                quantityResolved.addChangeListener(cl);
            }
        }
    }
    
    /**
     * @param obj data val object (might be null)
     * @return the value or zero for null
     */
    public static int getInt(final Object obj)
    {
        if (obj instanceof Integer)
        {
            return (Integer)obj;
        }
        return 0;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeFormFill()
     */
    @Override
    public void beforeFormFill()
    {
        isFillingForm = true;
        
        if (prevLoanRetPrep != null)
        {
            updateLoanPrepQuantities(prevLoanRetPrep, null);
            prevLoanRetPrep = null;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public Object beforeDelete(final Object dataObj, final DataProviderSessionIFace session)
    {
        updateLoanPrepQuantities(null, null);
        
        return super.beforeDelete(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (dataObj != null && formViewObj != null && formViewObj.getMVParent() != null && formViewObj.getMVParent().getMultiViewParent() != null)
        {
            loanPrepFVO = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
            loanFVO     = loanPrepFVO.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
            
            LoanReturnPreparation loanRetPrep = (LoanReturnPreparation)dataObj;
            LoanPreparation       loanPrep    = (LoanPreparation)loanPrepFVO.getDataObj();
            
            prevLoanRetPrep = loanRetPrep;
            
            Component comp = formViewObj.getControlByName("quantityReturned");
            if (comp instanceof ValSpinner && loanPrep != null &&  loanPrep.getLoanPreparationId() != null)
            {
                int qQnt    = getInt(loanPrep.getQuantity());
                
                int qQntRes = getInt(loanPrep.getQuantityResolved());
                int qQntRet = getInt(loanPrep.getQuantityReturned());
                
                // Calculate the total available
                int qtyToBeReturned = Math.max(0, qQnt - qQntRet); // shouldn't be negative
                int qtyToBeResolved = Math.max(0, qQnt - qQntRes); // shouldn't be negative
                
                int qtyRetLPR = getInt(loanRetPrep.getQuantityReturned());
                int qtyResLPR = getInt(loanRetPrep.getQuantityResolved());
                
                JButton newBtn = formViewObj.getRsController().getNewRecBtn();
                newBtn.setEnabled(qtyToBeReturned > 0);
                
                if (loanRetPrep != null)
                {
                    ValSpinner quantityReturned = (ValSpinner)comp;
                    quantityReturned.setRange(0, qtyRetLPR+qtyToBeReturned, qtyRetLPR);
                    
                    ValSpinner quantityResolved = (ValSpinner)formViewObj.getControlByName("quantityResolved");
                    quantityResolved.setRange(0, qtyResLPR+qtyToBeResolved, qtyResLPR);
                }
            }
        }
        
        isFillingForm = false;
    }
    
    /**
     * 
     */
    protected void updateLoanPrepQuantities(final LoanReturnPreparation loanRetPrepArg, final ChangeEvent e)
    {
        if (formViewObj != null && formViewObj.getValidator().hasChanged())
        {
            loanPrepFVO = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
            loanFVO     = loanPrepFVO.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();

            Component comp = formViewObj.getControlByName("quantityResolved");
            if (comp instanceof ValSpinner)
            {
                LoanReturnPreparation loanRetPrep = loanRetPrepArg != null ? loanRetPrepArg : (LoanReturnPreparation)formViewObj.getDataObj();
                LoanPreparation       loanPrep    = (LoanPreparation)loanPrepFVO.getDataObj();
                Loan                  loan        = (Loan)loanFVO.getDataObj();
                
                if (loanRetPrep != null)
                {
                    final ValSpinner lrpResolvedVS  = (ValSpinner)comp;
                    int              lrpResolvedQty = (Integer)lrpResolvedVS.getValue();
                    
                    final ValSpinner lrpReturnedVS  = (ValSpinner)formViewObj.getControlByName("quantityReturned");
                    int              lrpReturnedQty = (Integer)lrpReturnedVS.getValue();
                    
                    if (e != null)
                    {
                        if (e.getSource() == lrpResolvedVS)
                        {
                            if (lrpResolvedQty < lrpReturnedQty)
                            {
                                lrpReturnedQty = lrpResolvedQty;
                                final int qty = lrpReturnedQty;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        lrpReturnedVS.setValue(qty);
                                    }
                                });
                            }
                        } else if (e.getSource() == lrpReturnedVS)
                        {
                            if (lrpReturnedQty > lrpResolvedQty)
                            {
                                lrpResolvedQty = lrpReturnedQty;
                                final int qty = lrpReturnedQty;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        lrpResolvedVS.setValue(qty);
                                    }
                                });
                            }
                        }
                    }
                    loanRetPrep.setQuantityResolved(lrpResolvedQty);
                    loanRetPrep.setQuantityReturned(lrpReturnedQty);
                }
                
                int qtyRes = 0;
                int qtyRet = 0;
                
                int i = 0;
                if (loanRetPrep != null)
                {
                    LoanPreparation lrpLoanPrep = loanRetPrep.getLoanPreparation();
                    if (lrpLoanPrep != null && lrpLoanPrep.getLoanReturnPreparations().size() > 0)
                    {
                            for (LoanReturnPreparation lrp : loanRetPrep.getLoanPreparation().getLoanReturnPreparations())
                            {
                                qtyRes += getInt(lrp.getQuantityResolved()); 
                                qtyRet += getInt(lrp.getQuantityReturned());
                                i++;
                            }
                    }
                }
                
                // We need to do a 'doClick' so the form thinks the user clicked it.
                ValCheckBox isResolved = (ValCheckBox)loanPrepFVO.getControlByName("isResolved");
                if (qtyRes == loanPrep.getQuantity() != isResolved.isSelected())
                {
                    isResolved.doClick();
                }
        
                comp = loanPrepFVO.getControlByName("quantityResolved");
                if (comp instanceof JTextField)
                {
                    final JTextField qtyReturnedVS = (JTextField)loanPrepFVO.getControlByName("quantityReturned");
                    final JTextField qtyResolvedVS = (JTextField)comp;
                    
                    qtyReturnedVS.setText(Integer.toString(qtyRet));
                    qtyResolvedVS.setText(Integer.toString(qtyRes));
                                       
                    loanPrep.setQuantityReturned(qtyRet);
                    loanPrep.setQuantityResolved(qtyRes);
                }
                
                int qQnt    = 0;
                int qQntRes = 0;
                for (LoanPreparation lp : loan.getLoanPreparations())
                {
                    qQnt    += getInt(lp.getQuantity());
                    qQntRes += getInt(lp.getQuantityResolved());
                }
                
                isResolved = (ValCheckBox)loanPrepFVO.getControlByName("isResolved");
                isResolved.setSelected(qQnt == qQntRes);
                
                qQnt   = 0;
                qtyRes = 0;
                
                for (LoanPreparation lp : loan.getLoanPreparations())
                {
                    qQnt   += getInt(lp.getQuantity());
                    qtyRes += getInt(lp.getQuantityResolved());
                }
                
                isResolved = (ValCheckBox)loanFVO.getControlByName("isClosed");
                isResolved.setSelected(qQnt == qQntRes);
                loan.setIsClosed(qQnt == qQntRes);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#aboutToShutdown()
     */
    @Override
    public void aboutToShutdown()
    {
        updateLoanPrepQuantities(null, null);
        
        super.aboutToShutdown();
    }
}
