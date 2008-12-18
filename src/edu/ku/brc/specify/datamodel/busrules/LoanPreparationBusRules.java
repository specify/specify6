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

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.TableViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPreparation;
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
                final boolean    isNewObj         = loanPrep.getId() == null;
                final ValSpinner quantityReturned = (ValSpinner)comp;
                final ValSpinner quantity         = (ValSpinner)formViewObj.getControlByName("quantity");
                final ValSpinner qtyResolved      = (ValSpinner)formViewObj.getControlByName("quantityResolved");
                
                quantity.setRange(0, loanPrep.getQuantity(), loanPrep.getQuantity());
                
                quantityReturned.setEnabled(!isNewObj);
                int max = Math.max(loanPrep.getQuantity(), loanPrep.getQuantityResolved());
                
                quantityReturned.setRange(0, max, loanPrep.getQuantityReturned());
                qtyResolved.setRange(0, max, loanPrep.getQuantityReturned());
                
                formViewObj.getLabelFor(quantityReturned).setEnabled(!isNewObj);
                
                ValCheckBox isResolved = (ValCheckBox)formViewObj.getControlByName("isResolved");
                isResolved.setEnabled(!isNewObj);
                
                ChangeListener cl = new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        quantitiesChanged(quantity, quantityReturned, qtyResolved);
                    }
                };
                quantity.addChangeListener(cl);
                quantityReturned.addChangeListener(cl);
                qtyResolved.addChangeListener(cl);
            }
        }
    }
    
    /**
     * @param quantity
     * @param quantityReturned
     * @param qtyResolved
     */
    private void quantitiesChanged(final ValSpinner quantity, 
                                   final ValSpinner quantityReturned, 
                                   final ValSpinner qtyResolved)
    {
        int qty    = (Integer)quantity.getValue();
        int qtyRet = (Integer)quantityReturned.getValue();
        int qtyRes = (Integer)qtyResolved.getValue();
        
        String errKey = null;
        if (qty > qtyRes)
        {
            errKey = "LOAN_RET_QTY_GT_RES";
        } else if (qtyRet > qtyRes)
        {
            errKey = "LOAN_RET_RET_GT_RES";
        }
        final String errorKey = errKey;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                UIRegistry.displayErrorDlgLocalized(errorKey);
            }
        });
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
