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

import java.sql.Timestamp;
import java.util.GregorianCalendar;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataModelObjBase;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *
 * Sets Cataloger to createdByAgent (the user performing the upload).
 * Sets CatalogedDate to timestampCreated.
 * 
 * This is necessary because of form view requirements.
 * 
 * It is less than ideal in that if uploaded dataset includes Cataloger or CatalogedDate, then
 * when values in those columns are null, they will be silently replaced by this class. More work would 
 * need to be done in the upload validation process to improve this situation.
 */
public class CollectionObjectRecFinalizer implements UploadedRecFinalizerIFace
{

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadedRecFinalizerIFace#finalizeForWrite(edu.ku.brc.specify.datamodel.DataModelObjBase, int)
     */
    @Override
    public void finalizeForWrite(DataModelObjBase rec, int recNum, final Uploader uploader)
    {
        CollectionObject co = (CollectionObject )rec;
        if (co.getCatalogedDate() == null)
        {
            Timestamp ts = co.getTimestampCreated();
            co.setCatalogedDate(new GregorianCalendar(ts.getYear() + 1900, ts.getMonth(), ts.getDate()));
        }
        if (co.getCataloger() == null)
        {
            co.setCataloger(co.getCreatedByAgent());
        }

    }

}
