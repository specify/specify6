/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.datamodel;

/**
 * @author jstewart
 * @code_status Alpha
 */
public interface ObjectAttachmentIFace <T extends DataModelObjBase>
{
    public T getObject();
    public void setObject(T object);
    public Attachment getAttachment();
    public void setAttachment(Attachment attachment);
    public Integer getOrdinal();
    public void setOrdinal(Integer ordinal);
    public String getRemarks();
    public void setRemarks(String remarks);
}
