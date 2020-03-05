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

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.specify.datamodel.PcrPerson;
import edu.ku.brc.specify.datamodel.DNASequence;

import java.util.Hashtable;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

public class PcrPersonBusRules extends BaseBusRules {

    /**
     * @param dataClasses
     */
    public PcrPersonBusRules() {
        super(PcrPerson.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object, java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object parentDataObj, final Object dataObj, final boolean isExistingObject) {
        reasonList.clear();

        // isEdit is false when the data object is new, true when editing an existing object.
        if (isExistingObject &&
                parentDataObj instanceof DNASequence &&
                dataObj instanceof PcrPerson) {
            DNASequence ds = (DNASequence) parentDataObj;
            PcrPerson col = (PcrPerson) dataObj;

            Hashtable<Integer, Boolean> hash = new Hashtable<Integer, Boolean>();
            for (PcrPerson pcrPerson : ds.getPcrPersons()) {
                Integer id = pcrPerson.getAgent().getAgentId();
                boolean isBad = false;
                if (hash.get(id) == null) {
                    if (pcrPerson.getId() != null && id.equals(col.getAgent().getAgentId())) {
                        isBad = true;
                    }
                    hash.put(id, true);
                } else {
                    isBad = true;
                }

                if (isBad) {
                    reasonList.add(String.format(getResourceString("DNASEQ_DUPLICATE_SEQUENCERS"), col.getIdentityTitle()));
                    return STATUS.Error;
                }
            }
        }

        return super.processBusinessRules(parentDataObj, dataObj, isExistingObject);
    }

}
