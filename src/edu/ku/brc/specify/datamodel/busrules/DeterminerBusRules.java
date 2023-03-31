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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.Hashtable;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Determiner;
import edu.ku.brc.specify.datamodel.Division;

public class DeterminerBusRules extends BaseBusRules
{

    public DeterminerBusRules()
    {
        super(Determiner.class);
    }

    @Override
    public STATUS processBusinessRules(final Object parentDataObj, final Object dataObj, final boolean isExistingObject)
    {
        reasonList.clear();

        // isEdit is false when the data object is new, true when editing an existing object.
        if (isExistingObject &&
            parentDataObj instanceof Determination &&
            dataObj instanceof Determiner)
        {
            Determination det = (Determination)parentDataObj;
            Determiner detr = (Determiner)dataObj;

            Hashtable<Integer, Boolean> hash = new Hashtable<Integer, Boolean>();
            for (Determiner determiner : det.getDeterminers())
            {
                Integer id    = determiner.getAgent().getAgentId();
                boolean isBad = false;
                if (hash.get(id) == null)
                {
                    if (determiner.getId() != null && id.equals(detr.getAgent().getAgentId()))
                    {
                        isBad = true;
                    }
                    hash.put(id, true);
                } else
                {
                    isBad = true;
                }

                if (isBad)
                {
                    reasonList.add(String.format(getResourceString("DT_DUPLICATE_DETERMINERS"), detr.getIdentityTitle()));
                    return STATUS.Error;
                }
            }
        }

        return super.processBusinessRules(parentDataObj, dataObj, isExistingObject);
    }
}
