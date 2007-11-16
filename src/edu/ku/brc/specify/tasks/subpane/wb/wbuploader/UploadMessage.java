/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

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
