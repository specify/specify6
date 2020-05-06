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
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.specify.datamodel.DeaccessionPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;


public class DeaccessionPreparationBusRules extends BaseBusRules {

    public DeaccessionPreparationBusRules() {
        super(DeaccessionPreparationBusRules.class);
    }

    @Override
    public void afterFillForm(final Object dataObj) {
        super.afterFillForm(dataObj);
        if (dataObj != null && isOnLoanReturnForm(dataObj)) {
            DeaccessionPreparation dp = (DeaccessionPreparation)dataObj;
            if (dp.getId() == null &&
                    dp.getLoanReturnPreparation() != null &&
                    dp.getLoanReturnPreparation().getLoanPreparation() != null) {
                dp.setPreparation(dp.getLoanReturnPreparation().getLoanPreparation().getPreparation());
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
}