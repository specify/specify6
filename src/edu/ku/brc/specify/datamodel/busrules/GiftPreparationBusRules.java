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
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.specify.datamodel.GiftPreparation;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 29, 2007
 *
 */
public class GiftPreparationBusRules extends BaseBusRules implements CommandListener
{
    private final String CMDTYPE = "Interactions";
    /**
     * 
     */
    public GiftPreparationBusRules()
    {
        super(GiftPreparation.class);
        
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
                            CommandDispatcher.dispatch(new CommandAction(CMDTYPE, "AddToGift", loanMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
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
                            CommandDispatcher.dispatch(new CommandAction(CMDTYPE, "AddToGift", loanMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
                        }
                    }
                });
            }
        }
    }
    
    @Override
    public void afterFillForm(Object dataObj)
    {
        Component comp = formViewObj.getControlByName("quantity");
        if (comp instanceof ValSpinner && dataObj != null)
        {
            GiftPreparation  giftPrep   = (GiftPreparation)dataObj;
            
            //boolean    isNewObj         = giftPrep.getId() == null;
            ValSpinner quantity         = (ValSpinner)comp;
            
            // TODO I think this would be better if the Max Range 
            // was set to the available number of items.
            
            quantity.setRange(0, giftPrep.getQuantity(), giftPrep.getQuantity());
            
            //quantityReturned.setEnabled(!isNewObj);
            //int max = Math.max(loanPrep.getQuantity(), loanPrep.getQuantityReturned());
            //quantityReturned.setRange(0, max, loanPrep.getQuantityReturned());
            //formViewObj.getLabelFor(quantityReturned).setEnabled(!isNewObj);
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
        if (cmdAction.isType(CMDTYPE) && cmdAction.isAction("REFRESH_GIFT_PREPS"))
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
}
