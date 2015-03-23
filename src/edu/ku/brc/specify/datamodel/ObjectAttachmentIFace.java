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
package edu.ku.brc.specify.datamodel;

/**
 * @author jstewart
 * 
 * @code_status Alpha
 */
public interface ObjectAttachmentIFace <T extends DataModelObjBase>
{
    /**
     * @return
     */
    public abstract T getObject();
    
    /**
     * @param object
     */
    public abstract void setObject(T object);
    
    /**
     * @return
     */
    public abstract Attachment getAttachment();
    
    /**
     * @param attachment
     */
    public abstract void setAttachment(Attachment attachment);
    
    /**
     * @return
     */
    public abstract Integer getOrdinal();
    
    /**
     * @param ordinal
     */
    public abstract void setOrdinal(Integer ordinal);
    
    /**
     * @return
     */
    public abstract String getRemarks();
    
    /**
     * @param remarks
     */
    public abstract void setRemarks(String remarks);
    
    /**
     * @return the table id of the 'owner' of the attachment
     */
    public abstract int getTableID();
    
}
