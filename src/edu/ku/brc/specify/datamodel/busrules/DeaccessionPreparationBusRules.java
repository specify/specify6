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

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.specify.datamodel.Deaccession;
import edu.ku.brc.specify.datamodel.DeaccessionPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;
//import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.UIRegistry;

public class DeaccessionPreparationBusRules extends BaseBusRules {

    public DeaccessionPreparationBusRules() {
        super(DeaccessionPreparationBusRules.class);
    }

    @Override
    public void afterFillForm(final Object dataObj) {
        super.afterFillForm(dataObj);
        if (dataObj != null) {
            DeaccessionPreparation dp = (DeaccessionPreparation) dataObj;
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
        if (isOnDeaccessionForm(newDataObj)) {
            Preparation p = ((DeaccessionPreparation)newDataObj).getPreparation();
            if (p != null && p.getCollectionObject() != null && p.getCollectionObject().getAccession() != null) {
                BusinessRulesIFace busRules = formViewObj.getMVParent().getCurrentViewAsFormViewObj().getBusinessRules();
                if (busRules != null) {
                    ((DeaccessionBusRules)busRules).checkPrepAccession(p.getCollectionObject().getAccession());
                }
            }
        }
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

    private boolean isOnDeaccessionForm(final Object dataObj) {
        return Deaccession.class.equals(dataObj);
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
            if (((LoanReturnPreparation)parentObj).getDeaccessionPreparations().size() > 0) {
                UIRegistry.showLocalizedError("DeaccessionPreparationBusRules.ONLY_ONE");
                return false;
            }
            return true;
        }
        return true;
    }

}