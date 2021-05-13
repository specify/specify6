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
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Disposal;
import edu.ku.brc.specify.datamodel.Gift;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;


public class DisposalBusRules extends BaseBusRules {
    public DisposalBusRules()
    {
        super(DisposalBusRules.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();

        if (!(dataObj instanceof Disposal))
        {
            return STATUS.Error;
        }

        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("disposalNumber",
                (FormDataObjIFace)dataObj,
                Disposal.class,
                "disposalId");

        return duplicateNumberStatus;
    }

}