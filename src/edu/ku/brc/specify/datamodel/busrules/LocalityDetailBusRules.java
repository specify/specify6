/* Copyright (C) 2012, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 18, 2013
 *
 */
public class LocalityDetailBusRules extends BaseBusRules
{
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToAddSibling(java.lang.Object)
     */
    @Override
    public boolean isOkToAddSibling(Object parentObj)
    {
        if (parentObj instanceof Locality)
        {
            if (((Locality)parentObj).getLocalityDetails().size() > 0) 
            {
                UIRegistry.showLocalizedError("LocalityDetailBusRules.ONLY_ONE");
                return false;
            }
            return true;
        }
        return false;
    }

}
