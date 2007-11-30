/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class InvalidStructure implements UploadMessage
{
    protected String      msg;
    protected Object data;

    /**
     * @param msg
     */
    public InvalidStructure(String msg, final Object data)
    {
        super();
        this.msg = msg;
        this.data = data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getCol()
     */
    public int getCol()
    {
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getData()
     */
    public Object getData()
    {
        return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getMsg()
     */
    public String getMsg()
    {
        return msg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getRow()
     */
    public int getRow()
    {
        return -1;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getMsg();
    }
}
