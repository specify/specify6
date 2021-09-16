/* Copyright (C) 2021, Specify Collections Consortium
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

import edu.ku.brc.specify.datamodel.FieldNotebookPage;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSet;

public class FieldNotebookPageSetBusRules extends AttachmentOwnerBaseBusRules
{
    public FieldNotebookPageSetBusRules()
    {
        super(FieldNotebookPageSet.class);
    }

    /**
     * @param attOwner
     */
    @Override
    protected void addExtraObjectForProcessing(final Object dObjAtt)
    {
        super.addExtraObjectForProcessing(dObjAtt);
        
        for (FieldNotebookPage page : ((FieldNotebookPageSet)dObjAtt).getPages())
        {
            super.addExtraObjectForProcessing(page);
        }
    }

}
