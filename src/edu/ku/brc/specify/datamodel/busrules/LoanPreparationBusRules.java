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

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.TableViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
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
    private final String CMDTYPE = "Interactions";
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
                            CommandDispatcher.dispatch(new CommandAction(CMDTYPE, "AddToLoan", loanMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
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
                            //formViewObj.getDataFromUI();
                            CommandDispatcher.dispatch(new CommandAction(CMDTYPE, "AddToLoan", loanMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
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
                        formViewObj.getValidator().setHasChanged(true);
                        formViewObj.setDataIntoUI();
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
