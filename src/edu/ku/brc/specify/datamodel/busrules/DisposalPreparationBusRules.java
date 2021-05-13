/*This library is free software;you can redistribute it and/or
 *modify it under the terms of the GNU Lesser General Public
 *License as published by the Free Software Foundation;either
 *version2.1of the License,or(at your option)any later version.
 *
 *This library is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY;without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the GNU
 *Lesser General Public License for more details.
 *
 *You should have received a copy of the GNU Lesser General Public
 *License along with this library;if not,write to the Free Software
 *Foundation,Inc.,59Temple Place,Suite 330,Boston,MA 02111-1307USA
 */
/**
 *
 */
package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.af.ui.forms.*;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.specify.datamodel.*;
//import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Triple;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

public class DisposalPreparationBusRules extends BaseBusRules  implements CommandListener {
    public static final String CMDTYPE     = "Interactions";
    public static final String ADD_TO_DISPOSAL = "AddToDisposal";

    public DisposalPreparationBusRules() {
        super(DisposalPreparationBusRules.class);
        CommandDispatcher.register(CMDTYPE, this);
    }
    private static final Logger log = Logger.getLogger(DisposalPreparationBusRules.class);

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg) {
        super.initialize(viewableArg);

        if (formViewObj != null) {
            formViewObj.setSkippingAttach(true);

            if (formViewObj.getRsController() != null) {
                JButton newBtn = formViewObj.getRsController().getNewRecBtn();
                if (newBtn != null) {
                    // Remove all ActionListeners, there should only be one
                    for (ActionListener al : newBtn.getActionListeners()) {
                        newBtn.removeActionListener(al);
                    }

                    newBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            MultiView disposalMV = null;
                            if (viewable instanceof FormViewObj) {
                                disposalMV = formViewObj.getMVParent().getMultiViewParent();
                                formViewObj.getDataFromUI();

                            } else if (viewable instanceof TableViewObj) {
                                TableViewObj tblViewObj = (TableViewObj) viewable;
                                disposalMV = tblViewObj.getMVParent().getMultiViewParent();
                            }

                            if (disposalMV != null) {
                                formViewObj.getDataFromUI();
                                CommandDispatcher.dispatch(new CommandAction(CMDTYPE, ADD_TO_DISPOSAL, disposalMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
                            }
                        }
                    });
                }
            }
        } else if (viewableArg instanceof TableViewObj) {
            final TableViewObj tvo = (TableViewObj) viewableArg;
            JButton newBtn = tvo.getNewButton();
            if (newBtn != null) {
                // Remove all ActionListeners, there should only be one
                for (ActionListener al : newBtn.getActionListeners()) {
                    newBtn.removeActionListener(al);
                }

                newBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        MultiView disposalMV = tvo.getMVParent().getMultiViewParent();
                        if (disposalMV != null) {
                            CommandDispatcher.dispatch(new CommandAction(CMDTYPE, ADD_TO_DISPOSAL, disposalMV.getCurrentViewAsFormViewObj().getCurrentDataObj()));
                        }
                    }
                });
            }
        }
    }

    @Override
    public void afterFillForm(final Object dataObj) {
        super.afterFillForm(dataObj);
        if (dataObj != null) {
            DisposalPreparation dp = (DisposalPreparation) dataObj;
            if (dp.getId() == null) {
                if (isOnLoanReturnForm(dataObj)) {
                    if (dp.getLoanReturnPreparation() != null &&
                            dp.getLoanReturnPreparation().getLoanPreparation() != null) {
                        dp.setPreparation(dp.getLoanReturnPreparation().getLoanPreparation().getPreparation());
                    }
                }
            }
        }
    }

    @Override
    public void afterCreateNewObj(Object newDataObj) {
        super.afterCreateNewObj(newDataObj);
//        if (isOnDisposalForm(newDataObj)) {
//            Preparation p = ((DisposalPreparation)newDataObj).getPreparation();
//            if (p != null && p.getCollectionObject() != null && p.getCollectionObject().getAccession() != null) {
//                BusinessRulesIFace busRules = formViewObj.getMVParent().getCurrentViewAsFormViewObj().getBusinessRules();
//                if (busRules != null) {
//                    ((DisposalBusRules)busRules).checkPrepAccession(p.getCollectionObject().getAccession());
//                }
//            }
//        }
    }

    private Class<?> getContext(final Object dataObj) {
        Class<?> result = null;
        if (formViewObj != null && formViewObj.getParentDataObj() != null){
            result = formViewObj.getParentDataObj().getClass();
        }
        return result;
    }

    private boolean isOnLoanReturnForm(final Object dataObj) {
        return LoanReturnPreparation.class.equals(getContext(dataObj));
    }

    private boolean isOnDisposalForm(final Object dataObj) {
        return Disposal.class.equals(dataObj);
    }

//    private boolean isOnPreparationForm(final Object dataObj) {
//        return Preparation.class.equals(getContext(dataObj));
//    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToAddSibling(java.lang.Object)
     */
    @Override
    public boolean isOkToAddSibling(Object parentObj) {
        if (parentObj instanceof LoanReturnPreparation) {
            if (((LoanReturnPreparation)parentObj).getDisposalPreparations().size() > 0) {
                UIRegistry.showLocalizedError("DisposalPreparationBusRules.ONLY_ONE");
                return false;
            }
            return true;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doCommand(CommandAction cmdAction) {
        if (cmdAction.isType(CMDTYPE) && cmdAction.isAction("REFRESH_DISPOSAL_PREPS")) {
            if (formViewObj != null) {
                MultiView disposalMV = formViewObj.getMVParent().getMultiViewParent();
                if (disposalMV != null) {
                    if (formViewObj.getValidator() != null) {
                        // Reset in the data sp it shows up
                        Disposal disposal = (Disposal) disposalMV.getData();
                        formViewObj.setDataObj(disposal.getDisposalPreparations());
                        formViewObj.getValidator().setHasChanged(true);
                        formViewObj.getValidator().validateRoot();
                    }
                }

            } else if (viewable instanceof TableViewObj) {
                TableViewObj tvo = (TableViewObj) viewable;
                // Make sure the Disposal form knows there is a change
                MultiView disposalMV = tvo.getMVParent().getMultiViewParent();
                if (disposalMV != null && disposalMV.getCurrentValidator() != null) {
                    disposalMV.getCurrentValidator().setHasChanged(true);
                    disposalMV.getCurrentValidator().validateRoot();
                } else {
                    log.error("The Disposal's Multiview should not be null!");
                }

                // Refresh list in the grid
                tvo.refreshDataList();
            }
        }
    }

}