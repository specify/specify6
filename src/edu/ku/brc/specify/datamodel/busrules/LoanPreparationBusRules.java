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
import java.util.Calendar;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.SubViewBtn;
import edu.ku.brc.af.ui.forms.TableViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;
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
    
    private boolean    isFillingForm    = false;
    private SubViewBtn loanRetBtn       = null;
    
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
            
            Component comp = formViewObj.getControlByName("quantityReturned");
            if (comp instanceof ValSpinner)
            {
                final ValSpinner quantityReturned = (ValSpinner)comp;
                final ValSpinner quantity         = (ValSpinner)formViewObj.getControlByName("quantity");
                final ValSpinner qtyResolved      = (ValSpinner)formViewObj.getControlByName("quantityResolved");
    
                final ValCheckBox isResolved = (ValCheckBox)formViewObj.getControlByName("isResolved");
                ChangeListener cl = new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        boolean isNewObj = false;
                        if (formViewObj != null)
                        {
                            LoanPreparation loanPrep = (LoanPreparation)formViewObj.getDataObj();
                            isNewObj = loanPrep.getId() == null;
                        }
                        
                        if (!isFillingForm && isNewObj)
                        {
                            quantitiesChanged(quantity, quantityReturned, qtyResolved, isResolved);
                        }
                    }
                };
                
                quantity.addChangeListener(cl);
                quantityReturned.addChangeListener(cl);
                qtyResolved.addChangeListener(cl);
            }
            
            comp = formViewObj.getControlById("loanReturnPreparations");
            if (comp instanceof SubViewBtn)
            {
                loanRetBtn = (SubViewBtn)comp;
                loanRetBtn.getBtn().setIcon(null);
                loanRetBtn.getBtn().setText(UIRegistry.getResourceString("LOAN_RET_PREP"));
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
            
            LoanPreparation loanPrep = (LoanPreparation)dataObj;
            Preparation     prep     = loanPrep.getPreparation();
            
            if (loanRetBtn != null)
            {
                loanRetBtn.setEnabled(loanPrep.getId() != null);
            }
            
            Component comp = formViewObj.getControlByName("quantityReturned");
            if (comp instanceof ValSpinner && prep != null &&  prep.getPreparationId() != null)
            {
                final boolean    isNewObj         = loanPrep.getId() == null;
                final ValSpinner quantityReturned = (ValSpinner)comp;
                final ValSpinner quantity         = (ValSpinner)formViewObj.getControlByName("quantity");
                final ValSpinner qtyResolved      = (ValSpinner)formViewObj.getControlByName("quantityResolved");
                
                // Calculate how many have been Gift'ed
                String sql = "SELECT gf.Quantity FROM  giftpreparation AS gf " +
                             "INNER JOIN giftpreparation AS gfp ON gfp.GiftPreparationID = gf.GiftPreparationID " +
                             "INNER JOIN preparation AS p ON gf.PreparationID = p.PreparationID " +
                             "WHERE p.PreparationID = " + prep.getPreparationId();
                
                System.out.println(sql);
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
                    // Get all the LoanReturn Quantities so we can figure out
                    // how many are still available
                    sql = "SELECT lp.Quantity, lp.QuantityResolved, lrp.Quantity, p.Count, p.PreparationID " +
                                 "FROM  loanpreparation AS lp " +
                                 "LEFT JOIN loanreturnpreparation AS lrp ON lrp.LoanPreparationID = lp.LoanPreparationID " +
                                 "INNER JOIN preparation AS p ON lp.PreparationID = p.PreparationID " +
                                 "WHERE p.PreparationID = " + prep.getPreparationId();
                    System.out.println(sql);
                    System.out.println(" prep.getPreparationId() "+prep.getPreparationId());
                    
                    rows = BasicSQLUtils.query(sql);
                    for (Object[] cols : rows)
                    {
                        //for (int i=0;i<cols.length;i++) System.out.print(" "+cols[i]);
                        //System.out.println();
                        
                        qQnt     += getInt(cols[0]); // Qty loaned out
                        qQntRes  += getInt(cols[1]); // Qty Resolved (came back)
                        
                        qPrepCnt = getInt(cols[3]); // Prep Qty available (don't sum)
                    }
                } else
                {
                    qPrepCnt = loanPrep.getQuantity();
                }
                
                // Calculate the total available
                int availableQnt = Math.max(0, qPrepCnt - (qQnt - qQntRes) - qGiftQnt); // shouldn't be negative
                
                quantity.setRange(0, availableQnt, loanPrep.getQuantity());
                
                //quantityReturned.setEnabled(!isNewObj);
                
                quantityReturned.setRange(0, availableQnt, loanPrep.getQuantityReturned());
                qtyResolved.setRange(0,      availableQnt, loanPrep.getQuantityResolved());
                
                quantityReturned.setEnabled(isNewObj);
                qtyResolved.setEnabled(isNewObj);
                
                
                //formViewObj.getLabelFor(quantityReturned).setEnabled(!isNewObj);
                
                //ValCheckBox isResolved = (ValCheckBox)formViewObj.getControlByName("isResolved");
                //isResolved.setEnabled(!isNewObj);
            }
        }
        
        isFillingForm = false;
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
        
        //System.err.println(qty+"  "+qtyRet+"  "+qtyRes+"  ");
        quantity.setState(UIValidatable.ErrorType.Valid);
        quantityReturned.setState(UIValidatable.ErrorType.Valid);
        qtyResolved.setState(UIValidatable.ErrorType.Valid);
        
        quantityReturned.setState(qtyRet > qty ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid);
        qtyResolved.setState(qtyRes > qty ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                //formViewObj.getValidator().wasValidated(null);
            }
        });
        
        Boolean allRet = qty == qtyRes;
        resolvedChkBx.setSelected(allRet);
        
        LoanPreparation loanPrep = (LoanPreparation)formViewObj.getDataObj();
        Loan            loan     = loanPrep.getLoan();
        
        loanPrep.setIsResolved(allRet);
        
        LoanReturnPreparation loanRetPrep = getNewLRP(loanPrep);
        if (allRet)
        {
            if (loanRetPrep == null)
            {
                loanRetPrep = new LoanReturnPreparation();
                loanRetPrep.initialize();
                loanRetPrep.setReceivedBy(null);
                loanRetPrep.setModifiedByAgent(Agent.getUserAgent());
                loanRetPrep.setReturnedDate(Calendar.getInstance());
                loanPrep.addReference(loanRetPrep, "loanReturnPreparations");
            } else
            {
                loanRetPrep.setQuantity(qtyRes);
            }
            
            loanPrep.setQuantity(qty);
            loanPrep.setQuantityResolved(qtyRes);
            loanPrep.setQuantityReturned(qtyRet);
            
        } else if (loanRetPrep != null)
        {
            if (loanRetPrep.getId() == null)
            {
                loanPrep.removeReference(loanRetPrep, "loanReturnPreparations");
            } else
            {
            }
        }
        
        boolean allPrepsReturned = true;
        if (allRet)
        {
            for (LoanPreparation lp : loan.getLoanPreparations())
            {
                if (!lp.getIsResolved())
                {
                    allPrepsReturned = false;
                    break;
                }
            }
        } else
        {
            allPrepsReturned = false;
        }
        Component comp = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj().getCompById("2");
        if (comp instanceof ValCheckBox)
        {
            ValCheckBox chk = (ValCheckBox)comp;
            chk.setSelected(allPrepsReturned);
        }
    }
    
    /**
     * @param lp
     * @return
     */
    protected LoanReturnPreparation getNewLRP(final LoanPreparation lp)
    {
        for (LoanReturnPreparation lrp : lp.getLoanReturnPreparations())
        {
            if (lrp.getId() == null)
            {
                return lrp;
            }
        }

        return null;
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
