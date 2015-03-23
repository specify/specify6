/* Copyright (C) 2015, University of Kansas Center for Research
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

import edu.ku.brc.specify.datamodel.DataModelObjBase;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * An interface for performing table-specific modifications or additions
 * that are necessary before writing an uploaded record to storage.
 * 
 * Classes that implement this class must be contained in the
 * "edu.ku.brc.specify.tasks.subpane.wb.wbuploader" package and
 * must be named according to the following format: "[ClassName]RecFinalizer" where
 * ClassName is the name of a class that extends edu.ku.brc.specify.datamodel.DataModelObjBase.
 */
public interface UploadedRecFinalizerIFace
{
    /**
     * @param rec
     * @param recNum
     * 
     * Performs final field assignment in preparation for persisting the rec.
     */
    public void finalizeForWrite(final DataModelObjBase rec, int recNum, final Uploader uploader) throws UploaderException;
}
