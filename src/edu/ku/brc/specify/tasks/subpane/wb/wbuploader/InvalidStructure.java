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
public class InvalidStructure extends BaseUploadMessage
{
    protected Object data;

    /**
     * @param msg
     */
    public InvalidStructure(String msg, final Object data)
    {
        super(msg);
        this.data = data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getData()
     */
    @Override
    public Object getData()
    {
        return data;
    }
}
