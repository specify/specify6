/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.config;

import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;

/**
 * Class used to create RecordSetIFace objects.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jul 14, 2008
 *
 */
public class SpecifyRecordSetFactory extends RecordSetFactory
{
    /**
     * 
     */
    public SpecifyRecordSetFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.RecordSetFactory#createRecordSet()
     */
    @Override
    public RecordSetIFace createRecordSet()
    {
        RecordSet rs =  new RecordSet();
        rs.initialize();
        return rs;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.RecordSetFactory#createRecordSet(java.lang.String, int, java.lang.Byte)
     */
    @Override
    public RecordSetIFace createRecordSet(final String name, final int dbTableId, final Byte type)
    {
        RecordSet rs =  new RecordSet();
        rs.initialize();
        rs.set(name, dbTableId, type);
        return rs;
    }

}
