/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.tasks.InteractionsProcessor;
import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.SubViewBtn;
import edu.ku.brc.af.ui.forms.TableViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Triple;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

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
    private static final Logger log = Logger.getLogger(LoanPreparationBusRules.class);
    
    private final String LOAN_QTY_RANGE_ERR = "LOAN_QTY_RANGE_ERR";
    
    private boolean    isFillingForm    = false;
    private SubViewBtn loanRetBtn       = null;
 
    public static final String REFRESH_PREPS = "REFRESH_LOAN_PREPS";

    /**
     * Constructor.
     */
    public LoanPreparationBusRules()
    {
        super(LoanPreparation.class);
        
        CommandDispatcher.register(LoanBusRules.CMDTYPE, this);
        CommandDispatcher.register(LoanBusRules.DE_CMDS, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            formViewObj.setSkippingAttach(true);

            if (formViewObj.getRsController() != null)
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
                            MultiView loanMV = null;
                            if (viewable instanceof FormViewObj)
                            {
                                loanMV = formViewObj.getMVParent().getMultiViewParent();
                                formViewObj.getDataFromUI();
                                
                            } else if (viewable instanceof TableViewObj)
                            {
                                TableViewObj tblViewObj = (TableViewObj)viewable; 
                                loanMV = tblViewObj.getMVParent().getMultiViewParent();
                            }
                            
                            if (loanMV != null)
                            {
                                formViewObj.getDataFromUI();
                                CommandDispatcher.dispatch(new CommandAction(LoanBusRules.CMDTYPE, LoanBusRules.ADD_TO_LOAN, loanMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
                            }
                        }
                    });
                }
            }
            
            Component comp = formViewObj.getControlByName("quantityResolved");
            if (comp instanceof ValSpinner)
            {
                final JTextField quantityResolved = (JTextField)formViewObj.getControlByName("quantityReturned");
                final ValSpinner quantity         = (ValSpinner)formViewObj.getControlByName("quantity");
                final ValSpinner qtyResolved      = (ValSpinner)comp;
    
                final ValCheckBox isResolved = (ValCheckBox)formViewObj.getControlByName("isResolved");
                ChangeListener cl = new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        if (!UIValidator.isIgnoreAllValidation())
                        {
                            if (formViewObj != null)
                            {
                                LoanPreparation loanPrep = (LoanPreparation)formViewObj.getDataObj();
                                Integer qty = quantity.getIntValue();
                                if (qty != null && qty >= quantity.getMinValue() && qty <= quantity.getMaxValue())
                                {
                                    loanPrep.setQuantity(qty);
                                } else
                                {
                                    UIRegistry.showLocalizedError(LOAN_QTY_RANGE_ERR, qty, quantity.getMinValue(), quantity.getMaxValue());
                                }
                            }
                            
                            if (!isFillingForm)
                            {
                                Integer qtyResVal = Integer.parseInt(quantityResolved.getText());
                                if (qtyResVal != null && qtyResVal >= quantity.getMinValue() && qtyResVal <= quantity.getMaxValue())
                                {
                                    quantitiesChanged(quantity, qtyResVal, qtyResolved, isResolved);
                                } else
                                {
                                    UIRegistry.showLocalizedError(LOAN_QTY_RANGE_ERR, qtyResVal, quantity.getMinValue(), quantity.getMaxValue());
                                }
                            }
                        }
                    }
                };
                quantity.addChangeListener(cl);
                qtyResolved.addChangeListener(cl);
            }
            
            comp = formViewObj.getControlById("loanReturnPreparations");
            if (comp instanceof SubViewBtn)
            {
                loanRetBtn = (SubViewBtn)comp;
                loanRetBtn.getBtn().setIcon(null);
                loanRetBtn.getBtn().setText(getResourceString("LOAN_RET_PREP"));
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
                            CommandDispatcher.dispatch(new CommandAction(LoanBusRules.CMDTYPE, LoanBusRules.ADD_TO_LOAN, loanMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
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
            LoanPreparation loanPrep = (LoanPreparation)dataObj;
            Preparation     prep     = loanPrep.getPreparation();
            
            Component comp    = formViewObj.getControlByName("quantityResolved");
            Component qtyComp = formViewObj.getControlByName("quantity");
            if (comp instanceof JTextField && qtyComp instanceof ValSpinner && prep != null &&  prep.getPreparationId() != null)
            {
                final ValSpinner quantity         = (ValSpinner)qtyComp;
                final JTextField qtyResolved      = (JTextField)comp;
                final JTextField qtyReturned      = (JTextField)formViewObj.getControlByName("quantityReturned");
                
                int qMax = 5000;
                if (loanPrep.getPreparation() != null && loanPrep.getPreparation().getId() != null) {
                    boolean[] settings = {true, true, true, true};
                    String sql = InteractionsProcessor.getAdjustedCountForPrepSQL("p.preparationid = " + loanPrep.getPreparation().getId(), settings);
                    Connection conn = InteractionsProcessor.getConnForAvailableCounts();
                    Object[] amt = BasicSQLUtils.queryForRow(conn, sql);
                    try {
                        conn.close();
                    } catch (SQLException x) {
                        log.warn(x);
                    }
                    qMax = amt != null ? Integer.valueOf(amt[1].toString()).intValue() : qMax;
                    if (loanPrep.getId() != null) {
                        qMax += loanPrep.getQuantity() - loanPrep.getQuantityResolved(); //But... If returns are deleted or modified, this limit will not be adjusted till form is closed and reopened.
                    }
                }
                int qMin = loanPrep.getQuantityResolved(); //But... If returns are deleted or modified, this limit will not be adjusted till form is closed and reopened.
                if (qMin <= loanPrep.getQuantity() && loanPrep.getQuantity() <= qMax) {
                    quantity.setRange(qMin, qMax, loanPrep.getQuantity());
                } else {
                    quantity.setRange(loanPrep.getQuantity(), loanPrep.getQuantity(), loanPrep.getQuantity());
                }
                qtyResolved.setText(Integer.toString(loanPrep.getQuantityResolved()));
                qtyReturned.setText(Integer.toString(loanPrep.getQuantityReturned()));
            }
        }
        
        isFillingForm = false;
    }
    
    /**
     * @param quantity
     * @param quantityReturned
     * @param qtyResolved
     * @param resolvedChkBx
     */
    private void quantitiesChanged(final ValSpinner  quantity, 
                                   final int         quantityReturned,
                                   final ValSpinner  qtyResolved,
                                   final ValCheckBox resolvedChkBx)
    {
        int qty    = (Integer)quantity.getValue();
        int qtyRes = (Integer)qtyResolved.getValue();
        
        quantity.setState(UIValidatable.ErrorType.Valid);
        qtyResolved.setState(UIValidatable.ErrorType.Valid);
        
        qtyResolved.setState(qtyRes > qty || qtyRes > qty ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                formViewObj.getValidator().wasValidated(null);
                formViewObj.getMVParent().getMultiViewParent().getCurrentValidator().setHasChanged(true);
                formViewObj.getMVParent().getMultiViewParent().getCurrentValidator().wasValidated(null);
            }
        });
        
        final Boolean allRet = qty == qtyRes;
        
        LoanPreparation loanPrep = (LoanPreparation)formViewObj.getDataObj();
        Loan            loan     = loanPrep.getLoan();
        
        loanPrep.setIsResolved(allRet);
        
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
        
        final boolean allRetP = allPrepsReturned;
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                resolvedChkBx.setSelected(allRet);

                Component comp = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj().getCompById("2");
                if (comp instanceof ValCheckBox)
                {
                    ValCheckBox chk = (ValCheckBox)comp;
                    chk.setSelected(allRetP);
                    chk.repaint();
                }
            }
        });
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


    @Override
    public void aboutToShutdown() {
        super.aboutToShutdown();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        CommandDispatcher.unregister(LoanBusRules.CMDTYPE, this);
        CommandDispatcher.unregister(LoanBusRules.DE_CMDS, this);
    }

    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session) {
        super.beforeSave(dataObj, session);
        if (dataObj instanceof LoanPreparation) {
            LoanPreparation lp = (LoanPreparation) dataObj;
            if (lp.getQuantity() > 0) {
                lp.setIsResolved(lp.getQuantityResolved() == lp.getQuantity());
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(LoanBusRules.CMDTYPE) && cmdAction.isAction(REFRESH_PREPS))
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
                // Make sure the Loan form knows there is a change
                MultiView loanMV = tvo.getMVParent().getMultiViewParent();
                if (loanMV != null && loanMV.getCurrentValidator() != null)
                {
                    loanMV.getCurrentValidator().setHasChanged(true);
                    loanMV.getCurrentValidator().validateRoot();
                } else
                {
                    log.error("The Loan's Multiview should not be null!");
                }
                
                // Refresh list in the grid
                tvo.refreshDataList();
            }
        } else if (cmdAction.isType(LoanBusRules.DE_CMDS) && cmdAction.isAction("CLOSE_SUBVIEW")) {
            Triple<Object, Object, Object> dataTriple = (Triple<Object, Object, Object>)cmdAction.getData();
//            if (dataTriple.first == formViewObj && dataTriple.second instanceof LoanPreparation) {
//                LoanPreparation            loanPrep = (LoanPreparation)dataTriple.second;
//                Set<LoanReturnPreparation> lrps     = (Set<LoanReturnPreparation>)dataTriple.third;
//
//                if (loanPrep != null && lrps != null) {
//                    int quantityResolved = 0;
//                    int quantityReturned = 0;
//                    for (LoanReturnPreparation lrp : lrps) {
//                        quantityResolved += lrp.getQuantityResolved();
//                        quantityReturned += lrp.getQuantityReturned();
//                    }
//                    //loanPrep.setQuantityResolved(quantityResolved);
//                    //loanPrep.setQuantityReturned(quantityReturned);
//
//                    if (formViewObj != null) {
//                        Component comp = formViewObj.getControlByName("quantityResolved");
//                        if (comp instanceof JTextField) {
//                            ((JTextField)comp).setText(Integer.toString(quantityResolved));
//                        }
//                        comp = formViewObj.getControlByName("quantityReturned");
//                        if (comp instanceof JTextField) {
//                            ((JTextField)comp).setText(Integer.toString(quantityReturned));
//                        }
//                        comp = formViewObj.getControlByName("isResolved");
//                        if (comp instanceof ValCheckBox) {
//                            ((ValCheckBox)comp).setSelected(quantityResolved == loanPrep.getQuantity());
//                            if (((ValCheckBox) comp).isReadOnly()) {
//                                loanPrep.setIsResolved(quantityResolved == loanPrep.getQuantity());
//                            }
//                        }
//                    }
//                } else {
//                    log.error("The loanPrep or lrps should not be null!");
//                }
//            }
        }
    }

//    /* (non-Javadoc)
//     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
//     */
//    @Override
//    public STATUS processBusinessRules(Object dataObj)
//    {
//        reasonList.clear();
//        STATUS result = STATUS.OK;
//        LoanPreparation loanPrep = (LoanPreparation)dataObj;
//        if (loanPrep.getQuantityResolved() > loanPrep.getQuantity() || loanPrep.getQuantityReturned() > loanPrep.getQuantity()) {
//            reasonList.add(getResourceString("LOAN_RET_LWR_QNT"));
//            result = STATUS.Error;
//        }
//        if (loanPrep.getQuantityReturned() > loanPrep.getQuantityResolved()) {
//            reasonList.add(getResourceString("LOAN_RET_RET_GT_RES"));
//            result = STATUS.Error;
//        }
//        int qtyRes = 0, qtyRet = 0;
//        for (LoanReturnPreparation lrp : loanPrep.getLoanReturnPreparations()) {
//            qtyRes += lrp.getQuantityResolved();
//            qtyRet += lrp.getQuantityReturned();
//        }
//        if (qtyRes != loanPrep.getQuantityResolved() || qtyRet != loanPrep.getQuantityReturned()) {
//            reasonList.add(getResourceString("LOAN_RET_LRQS_NE_LPQS"));
//            result = STATUS.Error;
//        }
//        if (result != STATUS.OK) {
//            return result;
//        } else {
//            return super.processBusinessRules(dataObj);
//        }
//    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();
        LoanPreparation loanPrep = (LoanPreparation)dataObj;
        if (loanPrep.getQuantityResolved() > loanPrep.getQuantity())
        {
            reasonList.add(UIRegistry.getResourceString("LOAN_RET_LWR_QNT"));
            return STATUS.Error;
        }
        return super.processBusinessRules(dataObj);
    }

}
