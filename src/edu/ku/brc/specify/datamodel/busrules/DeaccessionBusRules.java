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
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.specify.datamodel.Accession;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;


public class DeaccessionBusRules extends BaseBusRules {

    private Set<Accession> accessions = new HashSet<>();
    private Component accessionsComp;

    public DeaccessionBusRules()
    {
        super(DeaccessionBusRules.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#initialize(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg) {
        super.initialize(viewableArg);
        accessionsComp = formViewObj.getControlByName("accessions");
    }

    public void checkPrepAccession(final Accession accession) {
        if (accessionsComp != null && !accessions.contains(accession)) {
            accessions.add(accession);
            refreshAccessionsDisplay();
        }
    }

    private void refreshAccessionsDisplay() {
        if (formViewObj != null) {
            formViewObj.setDataIntoUIComp(accessionsComp, accessions, "");
        }
    }
}