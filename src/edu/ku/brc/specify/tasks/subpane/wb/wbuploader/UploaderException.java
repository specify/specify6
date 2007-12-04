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
public class UploaderException extends Exception
{
	protected int status = ABORT_ROW;
	public static final int ABORT_ROW = 10;
	public static final int ABORT_IMPORT = 20;
    public static final int INVALID_DATA = 30;
	
	public UploaderException(final String msg, int abortStatus)
	{
		super(msg);
		this.status = abortStatus;
	}
	
	public UploaderException(final Exception ex)
	{
		super(ex);
	}
    
    public UploaderException(final Exception ex, int abortStatus)
    {
        super(ex);
        this.status = abortStatus;
    }
    
	/**
	 * @return the status
	 */
	public int getStatus()
	{
		return status;
	}
}
