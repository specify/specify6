/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTextField;
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
            
            Component comp = formViewObj.getControlByName("quantity");
            if (comp instanceof ValSpinner)
            {
                final ValSpinner quantity = (ValSpinner)formViewObj.getControlByName("quantity");
                ChangeListener cl = new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        if (!isFillingForm)
                        {
                            updateLoanPrepQuantities();
                        }
                    }
                };
                quantity.addChangeListener(cl);
            }
        }
    }
    
    /**
     * @param obj data val object (might be null)
     * @return the value or zero for null
     */
    private int getInt(final Object obj)
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
            updateLoanPrepQuantities();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeDelete(Object dataObj, DataProviderSessionIFace session)
    {
        updateLoanPrepQuantities();
        
        super.beforeDelete(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (dataObj != null && formViewObj != null)
        {
            formViewObj.setSkippingAttach(true);
            
            loanPrepFVO = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
            loanFVO     = loanPrepFVO.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
            
            LoanReturnPreparation loanRetPrep = (LoanReturnPreparation)dataObj;
            LoanPreparation       loanPrep    = (LoanPreparation)loanPrepFVO.getDataObj();
            
            prevLoanRetPrep = loanRetPrep;
            
            Component comp = formViewObj.getControlByName("quantity");
            if (comp instanceof ValSpinner && loanPrep != null &&  loanPrep.getLoanPreparationId() != null)
            {
                int qQnt    = getInt(loanPrep.getQuantity());
                int qQntRes = getInt(loanPrep.getQuantityResolved());
                
                // Calculate the total available
                int qtyToBeReturned = Math.max(0, qQnt - qQntRes); // shouldn't be negative
                
                JButton newBtn = formViewObj.getRsController().getNewRecBtn();
                newBtn.setEnabled(qtyToBeReturned > 0);
                
                if (loanRetPrep != null)
                {
                    final ValSpinner quantity = (ValSpinner)comp;
                    quantity.setRange(0, qQnt, getInt(loanRetPrep.getQuantity()));
                }
            }
        }
        
        isFillingForm = false;
    }
    
    /**
     * 
     */
    protected void updateLoanPrepQuantities()
    {
        if (formViewObj != null)
        {
            loanPrepFVO = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
            loanFVO     = loanPrepFVO.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();

            Component comp = formViewObj.getControlByName("quantity");
            if (comp instanceof ValSpinner)
            {
                LoanReturnPreparation loanRetPrep = (LoanReturnPreparation)formViewObj.getDataObj();
                LoanPreparation       loanPrep    = (LoanPreparation)loanPrepFVO.getDataObj();
                Loan                  loan        = (Loan)loanFVO.getDataObj();
                
                if (loanRetPrep != null)
                {
                    ValSpinner lrpVS  = (ValSpinner)comp;
                    int        lrpQty = (Integer)lrpVS.getValue();
                    loanRetPrep.setQuantity(lrpQty);
                }
                
                int qtyRes = 0;
                int qtyRet = 0;
                
                int i = 0;
                for (LoanReturnPreparation lrp : loanRetPrep.getLoanPreparation().getLoanReturnPreparations())
                {
                    qtyRes += getInt(lrp.getQuantity()); 
                    qtyRet += getInt(lrp.getQuantity());
                    i++;
                }
                
                ValCheckBox isResolved = (ValCheckBox)loanPrepFVO.getControlByName("isResolved");
                isResolved.setSelected(qtyRes == loanPrep.getQuantity());
        
                comp = loanPrepFVO.getControlByName("quantityResolved");
                if (comp instanceof ValSpinner)
                {
                    final JTextField qtyReturnedVS = (JTextField)loanPrepFVO.getControlByName("quantityReturned");
                    //final ValSpinner qtyResolvedVS = (ValSpinner)comp;
                    
                    qtyReturnedVS.setText(Integer.toString(qtyRet));
                    //qtyResolvedVS.setValue(qtyRes);
                    loanPrep.setQuantityReturned(qtyRet);
                    
                    // Do not set the Quantity Resolved
                    //loanPrep.setQuantityResolved(qtyRes);
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
        updateLoanPrepQuantities();
        
        super.aboutToShutdown();
    }
}
