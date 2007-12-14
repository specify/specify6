/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Interface for messages that appear in the uploader's message box.
 * 
 * getRow and getCol are intended to return the workbench row and col associated with the message.
 * Implementors should return -1 if there not is an associated row or col.
 * 
 * Implementors also will probably need to override Object.toString(). Or extend the class BaseUploadMessage.
 *
 */
public interface UploadMessage
{
    /**
     * @return the col
     */
    public int getCol();
    /**
     * @return the data
     */
    public Object getData();
    /**
     * @return the msg
     */
    public String getMsg();
    /**
     * @return the row
     */
    public int getRow();
}
