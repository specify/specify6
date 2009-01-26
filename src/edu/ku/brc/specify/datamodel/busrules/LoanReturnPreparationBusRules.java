/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;
import edu.ku.brc.specify.datamodel.Preparation;

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
    private FormViewObj parentFVO        = null;
    
    
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
            
            parentFVO = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
            
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
                            quantitiesChanged(quantity);
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
            
            LoanReturnPreparation loanRetPrep = (LoanReturnPreparation)dataObj;
            LoanPreparation       loanPrep    = (LoanPreparation)loanRetPrep.getLoanPreparation();
            Preparation           prep        = loanPrep.getPreparation();
            
            Component comp = formViewObj.getControlByName("quantity");
            if (comp instanceof ValSpinner && prep != null &&  prep.getPreparationId() != null)
            {
                // Calculate how many have been Gift'ed
                String sql = "SELECT gf.Quantity FROM  giftpreparation AS gf " +
                            "INNER JOIN giftpreparation AS gfp ON gfp.GiftPreparationID = gf.GiftPreparationID " +
                            "INNER JOIN preparation AS p ON gf.PreparationID = p.PreparationID " +
                            "WHERE p.PreparationID = " + prep.getPreparationId();
                
                int qGiftQnt = 0;

                Vector<Object[]> rows = BasicSQLUtils.query(sql);
                for (Object[] cols : rows)
                {
                    qGiftQnt  += getInt(cols[1]);
                }
                
                int qQnt     = 0;
                int qQntRes  = 0;
                int qPrepCnt = 0;
                
               if (loanPrep.getId() != null)
                {
                    // Now Calculate how have been loaned and already returned.
                    sql = "SELECT lp.Quantity, lp.QuantityResolved, lrp.Quantity, p.Count, p.PreparationID " +
                                 "FROM  loanpreparation AS lp " +
                                 "LEFT JOIN loanreturnpreparation AS lrp ON lrp.LoanPreparationID = lp.LoanPreparationID " +
                                 "INNER JOIN preparation AS p ON lp.PreparationID = p.PreparationID " +
                                 "WHERE p.PreparationID = " + prep.getPreparationId();
                    
                    System.out.println(sql);
                     
                    rows = BasicSQLUtils.query(sql);
                    for (Object[] cols : rows)
                    {
                        qPrepCnt += getInt(cols[3]);
                        
                        if (getInt(cols[4]) != prep.getPreparationId())
                        {
                            qQnt     += getInt(cols[0]);
                            qQntRes  += getInt(cols[1]);
                        }
                    }
                } else
                {
                    qPrepCnt = loanPrep.getQuantity();
                }
                
                // Calculate the total available
                int availableQnt = Math.max(0, qPrepCnt - (qQnt - qQntRes) - qGiftQnt); // shouldn't be negative
                
                final ValSpinner quantity = (ValSpinner)parentFVO.getControlByName("quantity");
                quantity.setRange(0, availableQnt, loanPrep.getQuantity());
                
                final ValSpinner quantityReturned = (ValSpinner)comp;
                final ValSpinner qtyResolved      = (ValSpinner)parentFVO.getControlByName("quantityResolved");
                
                quantityReturned.setRange(0, availableQnt, loanPrep.getQuantityReturned());
                qtyResolved.setRange(0,      availableQnt, loanPrep.getQuantityResolved());
            }
        }
        
        isFillingForm = false;
    }
    
    /**
     * @param quantity
     * @param quantityReturned
     * @param qtyResolved
     */
    private void quantitiesChanged(final ValSpinner quantity)
    {
        int qty    = (Integer)quantity.getValue();
        quantity.setState(UIValidatable.ErrorType.Valid);
        
        Component comp = parentFVO.getControlByName("quantityReturned");
        if (comp instanceof ValSpinner)
        {
            final ValSpinner quantityReturned = (ValSpinner)comp;
            final ValSpinner qtyResolved      = (ValSpinner)parentFVO.getControlByName("quantityResolved");
            final ValSpinner pQty             = (ValSpinner)parentFVO.getControlByName("quantity");
            
            int pQtyVal = (Integer)pQty.getValue();
            
            quantityReturned.setValue(qty);
            qtyResolved.setValue(qty);
            
            LoanReturnPreparation loanRetPrep = (LoanReturnPreparation)formViewObj.getDataObj();
            LoanPreparation       loanPrep    = (LoanPreparation)loanRetPrep.getLoanPreparation();
            loanPrep.setQuantityResolved(qty);
            loanPrep.setQuantityReturned(qty);
            
            quantityReturned.setState(qty > pQtyVal ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid);
            qtyResolved.setState(qty > pQtyVal ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid);

        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
    }
}
