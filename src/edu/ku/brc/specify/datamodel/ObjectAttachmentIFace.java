/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
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
    
}
