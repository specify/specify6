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
	protected int abortStatus = ABORT_ROW;
	public static final int ABORT_ROW = 10;
	public static final int ABORT_IMPORT = 20;
	
	public UploaderException(final String msg, int abortStatus)
	{
		super(msg);
		this.abortStatus = abortStatus;
	}
	
	public UploaderException(Exception ex)
	{
		super(ex);
	}
    
    public UploaderException(Exception ex, int abortStatus)
    {
        super(ex);
        this.abortStatus = abortStatus;
    }

	/**
	 * @return the abortStatus
	 */
	public int getAbortStatus()
	{
		return abortStatus;
	}
}
