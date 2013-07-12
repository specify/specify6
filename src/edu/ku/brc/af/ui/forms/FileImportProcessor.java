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
package edu.ku.brc.af.ui.forms;

import java.io.File;

import edu.ku.brc.helpers.ImageMetaDataHelper;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class FileImportProcessor
{
    protected static FileImportProcessor instance;
    
    /**
     * 
     */
    protected FileImportProcessor()
    {
        // nothing
    }
    
    /**
     * @return
     */
    public synchronized static FileImportProcessor getInstance()
    {
        if (instance==null)
        {
            instance = new FileImportProcessor();
        }
        return instance;
    }
    
    /**
     * @param record
     * @param tableId
     * @param file
     * @return
     */
    public boolean importFileIntoRecord(final FormDataObjIFace record, final  int tableId, final File file)
    {
        if (record instanceof Attachment)
        {
            Attachment a = (Attachment)record;
            a.setOrigFilename(file.getAbsolutePath());
            a.setTitle(file.getName());
            a.setTableId(tableId);
            
            a.setFileCreatedDate(ImageMetaDataHelper.getEmbeddedDateOrFileDate(file)); // Checks MimeType
            
            return true;
        }
        else if (record instanceof ObjectAttachmentIFace<?>)
        {
            ObjectAttachmentIFace<?> oa = (ObjectAttachmentIFace<?>)record;
            Attachment a = oa.getAttachment();
            if (a == null)
            {
                return false;
            }
            a.setOrigFilename(file.getAbsolutePath());
            a.setTitle(file.getName());
            a.setTableId(oa.getTableID());
            return true;
        }
        return false;
    }
}
