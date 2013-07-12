/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tools;

import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 7, 2010
 *
 */
public class StrLocPickListFactory extends edu.ku.brc.af.ui.db.PickListDBAdapterFactory
{

    /**
     * 
     */
    public StrLocPickListFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterFactory#create(java.lang.String, boolean)
     */
    @Override
    public PickListDBAdapterIFace create(String name, boolean createWhenNotFound)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterFactory#createPickList()
     */
    @Override
    public PickListIFace createPickList()
    {
        return super.createPickList();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterFactory#createPickListItem()
     */
    @Override
    public PickListItemIFace createPickListItem()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterFactory#getPickList(java.lang.String)
     */
    @Override
    public PickListIFace getPickList(String name)
    {
        return null;
    }
    
}