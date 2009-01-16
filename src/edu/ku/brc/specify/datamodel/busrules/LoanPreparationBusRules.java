/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.TableViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 29, 2007
 *
 */
public class LoanPreparationBusRules extends BaseBusRules implements CommandListener
{
    private final String CMDTYPE     = "Interactions";
    private final String ADD_TO_LOAN = "AddToLoan";
    
    /**
     * 
     */
    public LoanPreparationBusRules()
    {
        super(LoanPreparation.class);
        
        CommandDispatcher.register(CMDTYPE, this);
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
            JButton newBtn = formViewObj.getRsController().getNewRecBtn();
            if (newBtn != null)
            {
                // Remove all ActionListeners, there should only be one
                for (ActionListener al : newBtn.getActionListeners())
                {
                    newBtn.removeActionListener(al);
                }
                
                newBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        MultiView loanMV = formViewObj.getMVParent().getMultiViewParent();
                        if (loanMV != null)
                        {
                            formViewObj.getDataFromUI();
                            CommandDispatcher.dispatch(new CommandAction(CMDTYPE, ADD_TO_LOAN, loanMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
                        }
                    }
                });
            }
        } else if (viewableArg instanceof TableViewObj)
        {
            final TableViewObj tvo = (TableViewObj)viewableArg;
            JButton newBtn = tvo.getNewButton();
            if (newBtn != null)
            {
                // Remove all ActionListeners, there should only be one
                for (ActionListener al : newBtn.getActionListeners())
                {
                    newBtn.removeActionListener(al);
                }
                
                newBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        MultiView loanMV = tvo.getMVParent().getMultiViewParent();
                        if (loanMV != null)
                        {
                            CommandDispatcher.dispatch(new CommandAction(CMDTYPE, ADD_TO_LOAN, loanMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
                        }
                    }
                });
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
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (dataObj != null)
        {
            LoanPreparation loanPrep = (LoanPreparation)dataObj;
            Preparation     prep     = loanPrep.getPreparation();
            
            Component comp = formViewObj.getControlByName("quantityReturned");
            if (comp instanceof ValSpinner)
            {
                final boolean    isNewObj         = loanPrep.getId() == null;
                final ValSpinner quantityReturned = (ValSpinner)comp;
                final ValSpinner quantity         = (ValSpinner)formViewObj.getControlByName("quantity");
                final ValSpinner qtyResolved      = (ValSpinner)formViewObj.getControlByName("quantityResolved");
                
                String sql = "SELECT lp.Quantity, lp.QuantityResolved, lrp.Quantity, p.Count, p.PreparationID " +
                             "FROM  loanpreparation AS lp " +
                             "INNER JOIN loanreturnpreparation AS lrp ON lrp.LoanPreparationID = lp.LoanPreparationID " +
                             "INNER JOIN preparation AS p ON lp.PreparationID = p.PreparationID " +
                             "WHERE p.PreparationID = " + prep.getPreparationId();
                int qQnt     = 0;
                int qQntRes  = 0;
                int qPrepCnt = 0;
                Vector<Object[]> rows = BasicSQLUtils.query(sql);
                for (Object[] cols : rows)
                {
                    qPrepCnt += getInt(cols[3]);
                    
                    if (getInt(cols[4]) != prep.getPreparationId())
                    {
                        qQnt     += getInt(cols[0]);
                        qQntRes  += getInt(cols[1]);
                    }
                }
                int availableQnt = qPrepCnt - (qQnt - qQntRes);
                System.err.println("availableQnt "+availableQnt);
                quantity.setRange(0, availableQnt, loanPrep.getQuantity());
                
                quantityReturned.setEnabled(!isNewObj);
                //int max = Math.max(loanPrep.getQuantity(), loanPrep.getQuantityResolved());
                
                quantityReturned.setRange(0, availableQnt, loanPrep.getQuantityReturned());
                qtyResolved.setRange(0,      availableQnt, loanPrep.getQuantityResolved());
                
                formViewObj.getLabelFor(quantityReturned).setEnabled(!isNewObj);
                
                final ValCheckBox isResolved = (ValCheckBox)formViewObj.getControlByName("isResolved");
                isResolved.setEnabled(!isNewObj);
                
                ChangeListener cl = new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        quantitiesChanged(quantity, quantityReturned, qtyResolved, isResolved);
                    }
                };
                quantity.addChangeListener(cl);
                quantityReturned.addChangeListener(cl);
                qtyResolved.addChangeListener(cl);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object, java.lang.Object, boolean)
     */
    @Override
    public STATUS processBusinessRules(Object parentDataObj,
                                       Object dataObj,
                                       boolean isExistingObject)
    {
        reasonList.clear();

        return super.processBusinessRules(parentDataObj, dataObj, isExistingObject);
    }

    /**
     * @param quantity
     * @param quantityReturned
     * @param qtyResolved
     */
    private void quantitiesChanged(final ValSpinner quantity, 
                                   final ValSpinner quantityReturned, 
                                   final ValSpinner qtyResolved,
                                   final ValCheckBox resolvedChkBx)
    {
        int qty    = (Integer)quantity.getValue();
        int qtyRet = (Integer)quantityReturned.getValue();
        int qtyRes = (Integer)qtyResolved.getValue();
        
        String errKey = null;
        
        //quantity.setState(qty > qtyRet || qty > qtyRes ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid);
        
        System.err.println(qty+"  "+qtyRet+"  "+qtyRes+"  ");
        quantity.setState(UIValidatable.ErrorType.Valid);
        quantityReturned.setState(UIValidatable.ErrorType.Valid);
        qtyResolved.setState(UIValidatable.ErrorType.Valid);
        
        quantityReturned.setState(qtyRet > qty ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid);
        
        qtyResolved.setState(qtyRes > qty ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid);
        
        resolvedChkBx.setSelected(qty == qtyRes);
        
        
        //quantityReturned.setState(qtyRet > qty ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid);
            
        
        if (errKey != null)
        {
            final String errorKey = errKey;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    //UIRegistry.displayErrorDlgLocalized(errorKey);
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        CommandDispatcher.unregister(CMDTYPE, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(CMDTYPE) && cmdAction.isAction("REFRESH_LOAN_PREPS"))
        {
            if (formViewObj != null)
            {
                MultiView loanMV = formViewObj.getMVParent().getMultiViewParent();
                if (loanMV != null)
                {
                    if (formViewObj.getValidator() != null)
                    {
                        // Reset in the data sp it shows up
                        Loan loan = (Loan)loanMV.getData();
                        formViewObj.setDataObj(loan.getLoanPreparations());
                        formViewObj.getValidator().setHasChanged(true);
                        formViewObj.getValidator().validateRoot();
                    }
                }

            } else if (viewable instanceof TableViewObj)
            {
                TableViewObj tvo = (TableViewObj)viewable;
                tvo.refreshDataList();
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();
        LoanPreparation loanPrep = (LoanPreparation)dataObj;
        if (loanPrep.getQuantityReturned() > loanPrep.getQuantity())
        {
            reasonList.add(UIRegistry.getResourceString("LOAN_RET_LWR_QNT"));
            return STATUS.Error;
        }
        return super.processBusinessRules(dataObj);
    }
    
    
}
