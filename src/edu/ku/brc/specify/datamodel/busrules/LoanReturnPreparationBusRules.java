/* Copyright (C) 2020, Specify Collections Consortium
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

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.*;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.Triple;
import org.apache.log4j.Logger;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 29, 2007
 *
 */
public class LoanReturnPreparationBusRules extends BaseBusRules implements CommandListener
{
    private static final Logger log = Logger.getLogger(LoanReturnPreparationBusRules.class);

    private boolean     isFillingForm    = false;
    private FormViewObj loanPrepFVO      = null;
    private FormViewObj loanFVO          = null;
    
    private LoanReturnPreparation  prevLoanRetPrep = null;
    private LoanPreparation theLoanPrep = null;
    
    /**
     * 
     */
    public LoanReturnPreparationBusRules()
    {
        super(LoanReturnPreparation.class);
        CommandDispatcher.register(LoanBusRules.DE_CMDS, this);
    }

    private Object getValueFromComp(Component comp) {
        if (comp instanceof ValCheckBox) {
            return ((ValCheckBox)comp).isSelected();
        }
        if (comp instanceof ValSpinner) {
            return ((ValSpinner)comp).getIntValue();
        }
        if (comp instanceof ValFormattedTextFieldSingle) {
            return ((ValFormattedTextFieldSingle)comp).getText();
        }
        log.error("Invalid Control Type: " + comp.getClass());
        return null;
    }

    private LoanPreparation getTheLoanPrep() {
        if (theLoanPrep == null) {
            if (loanPrepFVO == null) {
                if (formViewObj != null) {
                    loanPrepFVO = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
                }
            }
            LoanPreparation lp = (LoanPreparation)loanPrepFVO.getDataObj();
            Component quantityReturned = loanPrepFVO.getControlByName("quantityReturned");
            Component quantityResolved = loanPrepFVO.getControlByName("quantityResolved");
            Component isResolved = loanPrepFVO.getControlByName("isResolved");
//            System.out.println(quantityReturned.getClass());
//            System.out.println(quantityResolved.getClass());
//            System.out.println(isResolved.getClass());

            theLoanPrep = new LoanPreparation();
            theLoanPrep.setLoanPreparationId(lp.getLoanPreparationId());
            theLoanPrep.setQuantity(lp.getQuantity());
            theLoanPrep.setQuantityResolved(getInt(getValueFromComp(quantityResolved)));
            theLoanPrep.setQuantityReturned(getInt(getValueFromComp(quantityReturned)));
            theLoanPrep.setIsResolved(getBool(getValueFromComp(isResolved)));
        }
        return theLoanPrep;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null && formViewObj.getRsController() != null && formViewObj.isEditing())
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
                            quantityChanged(null, e);
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
    public static int getInt(final Object obj) {
        if (obj == null)  {
            return 0;
        }
        if (obj instanceof Integer) {
            return (Integer)obj;
        }
        if (obj instanceof String) {
            try {
                return Integer.valueOf(obj.toString());
            } catch (NumberFormatException x) {
                //
            }
        }
        return 0;
    }

    /**
     * @param obj data val object (might be null)
     * @return the value or false for null
     */
    public static Boolean getBool(final Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean)obj;
        }
        return false;
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
            quantityChanged(prevLoanRetPrep, null);
            prevLoanRetPrep = null;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public Object beforeDelete(final Object dataObj, final DataProviderSessionIFace session)
    {
        quantityChanged(null, null);
        
        return super.beforeDelete(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (dataObj != null && formViewObj != null && formViewObj.isEditing() && formViewObj.getMVParent() != null && formViewObj.getMVParent().getMultiViewParent() != null)
        {

            LoanReturnPreparation loanRetPrep = (LoanReturnPreparation)dataObj;

            prevLoanRetPrep = loanRetPrep;
            
            Component comp = formViewObj.getControlByName("quantityReturned");
            if (comp instanceof ValSpinner && getTheLoanPrep() != null &&  getTheLoanPrep().getLoanPreparationId() != null)
            {
                int qQnt    = getInt(getTheLoanPrep().getQuantity());
                
                int qQntRes = getInt(getTheLoanPrep().getQuantityResolved());
                int qQntRet = getInt(getTheLoanPrep().getQuantityReturned());
                
                // Calculate the total available

                int qtyRetLPR = getInt(loanRetPrep.getQuantityReturned());
                int qtyResLPR = getInt(loanRetPrep.getQuantityResolved());

                int qtyToBeReturned = Math.max(0, qQnt - qQntRet + qtyResLPR); // shouldn't be negative
                int qtyToBeResolved = Math.max(0, qQnt - qQntRes + qtyResLPR); // shouldn't be negative

                JButton newBtn = formViewObj.getRsController().getNewRecBtn();
                newBtn.setEnabled(qtyToBeReturned > 0);
                
                ValSpinner quantityReturned = (ValSpinner)comp;
                ValSpinner quantityResolved = (ValSpinner)formViewObj.getControlByName("quantityResolved");

                quantityReturned.setRange(0, qtyToBeResolved, qtyRetLPR);
                quantityResolved.setRange(0, qtyToBeResolved, qtyResLPR);
            }
        }
        
        isFillingForm = false;
    }

    /**
     *
     * @param loanRetPrep
     * @param lrpResolvedVS
     * @param lrpReturnedVS
     * @param e
     */
    protected void adjustQuantitySpinners(final LoanReturnPreparation loanRetPrep, final ValSpinner lrpResolvedVS, final ValSpinner lrpReturnedVS, final ChangeEvent e) {
        int lrpResolvedQty = (Integer) lrpResolvedVS.getValue();
        int lrpReturnedQty = (Integer) lrpReturnedVS.getValue();
        if (e != null) {
            if (e.getSource() == lrpResolvedVS) {
                if (lrpResolvedQty < lrpReturnedQty) {
                    final int qty = lrpResolvedQty;
                    //but what about max limit?
                    SwingUtilities.invokeLater(() -> lrpReturnedVS.setValue(qty));
                }
            } else if (e.getSource() == lrpReturnedVS) {
                if (lrpReturnedQty > lrpResolvedQty) {
                    final int qty = lrpReturnedQty;
                    //but what about max limit?
                    SwingUtilities.invokeLater(() -> lrpResolvedVS.setValue(qty));
                }
            }
            formViewObj.getValidator().setHasChanged(true);
        }
        //loanRetPrep.setQuantityResolved(lrpResolvedQty);
        //loanRetPrep.setQuantityReturned(lrpReturnedQty);
    }

    /**
     *
     * @param loanRetPrep
     * @return
     */
    protected Pair<Integer, Integer> getResRetTotals(LoanReturnPreparation loanRetPrep) {
        Pair<Integer, Integer> result = new Pair<>(0, 0);
        int qtyRes = 0, qtyRet = 0, i = 0;
        if (loanRetPrep != null) {
            LoanPreparation lrpLoanPrep = loanRetPrep.getLoanPreparation();
            if (lrpLoanPrep != null && lrpLoanPrep.getLoanReturnPreparations().size() > 0) {
                for (LoanReturnPreparation lrp : loanRetPrep.getLoanPreparation().getLoanReturnPreparations()) {
                    qtyRes += getInt(lrp.getQuantityResolved());
                    qtyRet += getInt(lrp.getQuantityReturned());
                    i++;
                }
            }
            result.setFirst(qtyRes);
            result.setSecond(qtyRet);
        }
        return result;
    }

    /**
     *
     * @param qtyRes
     */
    protected void setPrepIsResolved(int qtyRes) {
        LoanPreparation lp = getTheLoanPrep();
        if (qtyRes == lp.getQuantity() != lp.getIsResolved()) {
            lp.setIsResolved(!lp.getIsResolved());
        }
    }

    protected void setLoanIsClosedChkBox() {
        Loan loan = (Loan) loanFVO.getDataObj();
        int qQnt = 0;
        int qQntRes = 0;
        for (LoanPreparation lp : loan.getLoanPreparations()) {
            qQnt += getInt(lp.getQuantity());
            qQntRes += getInt(lp.getQuantityResolved());
        }

//                    //isResolved = (ValCheckBox)loanPrepFVO.getControlByName("isResolved");
//                    //isResolved.setSelected(qQnt == qQntRes);
//
//                    qQnt   = 0;
//                    qtyRes = 0;
//
//                    for (LoanPreparation lp : loan.getLoanPreparations())
//                    {
//                        qQnt   += getInt(lp.getQuantity());
//                        qtyRes += getInt(lp.getQuantityResolved());
//                    }

        //isResolved = (ValCheckBox)loanFVO.getControlByName("isClosed");
        //isResolved.setSelected(qQnt == qQntRes);
        loan.setIsClosed(qQnt == qQntRes);
    }
    protected void setLoanPrepResRet(Pair<Integer, Integer> resRetTotals) {
        Component comp = loanPrepFVO.getControlByName("quantityResolved");
        if (comp instanceof JTextField) {
            getTheLoanPrep().setQuantityReturned(resRetTotals.getSecond());
            getTheLoanPrep().setQuantityResolved(resRetTotals.getFirst());
        }
    }
    /**
     * 
     */
    protected void quantityChanged(final LoanReturnPreparation loanRetPrepArg, final ChangeEvent e) {
        if (formViewObj != null) {
            Component comp = formViewObj.getControlByName("quantityResolved");
            if (comp instanceof ValSpinner) {
                LoanReturnPreparation loanRetPrep = loanRetPrepArg != null ? loanRetPrepArg : (LoanReturnPreparation) formViewObj.getDataObj();
                if (loanRetPrep != null) {
                    final ValSpinner lrpResolvedVS = (ValSpinner) comp;
                    final ValSpinner lrpReturnedVS = (ValSpinner) formViewObj.getControlByName("quantityReturned");
                    adjustQuantitySpinners(loanRetPrep, lrpResolvedVS, lrpReturnedVS, e);
                }
                Pair<Integer, Integer> resRetTotals = getResRetTotals(loanRetPrep);
                setPrepIsResolved(resRetTotals.getFirst());
                setLoanPrepResRet(resRetTotals);
                //setLoanIsClosedChkBox();
            }
            updateTheLoanPrepFVO();
        }
    }

    private void updateTheLoanPrepFVO() {
        //quantityChanged(null, null);
        if (loanPrepFVO != null) {
            LoanPreparation loanPrep = getTheLoanPrep();
            if (loanPrep != null) {
                Component comp = loanPrepFVO.getControlByName("quantityResolved");
                if (comp instanceof ValFormattedTextFieldSingle) {
                    final ValFormattedTextFieldSingle c =  (ValFormattedTextFieldSingle) comp;
                    final String qres = Integer.toString(loanPrep.getQuantityResolved());
                    SwingUtilities.invokeLater(() -> c.setText(qres/*, true*/));
                }
                comp = loanPrepFVO.getControlByName("quantityReturned");
                if (comp instanceof ValFormattedTextFieldSingle) {
                    final ValFormattedTextFieldSingle c =  (ValFormattedTextFieldSingle) comp;
                    final String qret = Integer.toString(loanPrep.getQuantityReturned());
                    SwingUtilities.invokeLater(() -> c.setText(qret/*, true*/));
                }
                comp = loanPrepFVO.getControlByName("isResolved");
                if (comp instanceof ValCheckBox) {
                    final ValCheckBox c =  (ValCheckBox) comp;
                    final Boolean qres = loanPrep.getIsResolved();
                    SwingUtilities.invokeLater(() -> c.setSelected(qres/*, true*/));
                    if (((ValCheckBox)comp).isReadOnly()) {
                        ((LoanPreparation)loanPrepFVO.getDataObj()).setIsResolved(qres);
                    }
                }
            }
            if (loanPrepFVO.getValidator() != null) {
                loanPrepFVO.getValidator().setHasChanged(true);
            }
        }
    }

    @Override
    public void formShutdown() {
        super.formShutdown();
        CommandDispatcher.unregister(LoanBusRules.DE_CMDS, this);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doCommand(CommandAction cmdAction) {
        if (cmdAction.isType(LoanBusRules.DE_CMDS) && cmdAction.isAction("CLOSE_SUBVIEW")) {
            Triple<Object, Object, Object> dataTriple = (Triple<Object, Object, Object>)cmdAction.getData();
            if (dataTriple.second == loanPrepFVO.getDataObj()) {
                updateTheLoanPrepFVO();
                cmdAction.setConsumed(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#aboutToShutdown()
     */
    @Override
    public void aboutToShutdown() {
        if (loanPrepFVO != null) {
            updateTheLoanPrepFVO();
//            quantityChanged(null, null);
////            LoanPreparation lp = (LoanPreparation) loanPrepFVO.getDataObj();
////            lp.setQuantity(getTheLoanPrep().getQuantity());
////            lp.setQuantityResolved(getTheLoanPrep().getQuantityResolved());
////            lp.setQuantityReturned(getTheLoanPrep().getQuantityReturned());
////            lp.setIsResolved(getTheLoanPrep().getIsResolved());
//            LoanPreparation loanPrep = getTheLoanPrep();
//            if (loanPrep != null) {
//                Component comp = loanPrepFVO.getControlByName("quantityResolved");
//                if (comp instanceof JTextField) {
//                    ((JTextField) comp).setText(Integer.toString(loanPrep.getQuantityResolved()));
//                }
//                comp = loanPrepFVO.getControlByName("quantityReturned");
//                if (comp instanceof JTextField) {
//                    ((JTextField) comp).setText(Integer.toString(loanPrep.getQuantityReturned()));
//                }
//                comp = loanPrepFVO.getControlByName("isResolved");
//                if (comp instanceof ValCheckBox) {
////                    if (((ValCheckBox)comp).isReadOnly()) {
////                        //set loanpreparation.isResolved but beware of Cancels
////                    } else {
////                        //set the comp
////                    }
//                }
//            }
//            loanPrepFVO.getValidator().setHasChanged(true);
        }
        super.aboutToShutdown();
    }
}
