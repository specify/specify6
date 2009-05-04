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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.Method;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *A "DefaultFieldEntry" for the Collector.isPrimary field.
 */
public class DefaultIsPrimaryEntry extends DefaultFieldEntry
{

    public DefaultIsPrimaryEntry(final UploadTable uploadTbl, Class<?> fldClass, Method setter,
            String fldName, final UploadField uploadFld)
    {
        super(uploadTbl, fldClass, setter, fldName, uploadFld);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.DefaultFieldEntry#getDefaultValue(java.lang.Object[])
     */
    @Override
    protected Object getDefaultValue(Object... params)
    {
        return params[0].equals(0);
    }

}
