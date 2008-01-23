/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.lang.reflect.InvocationTargetException;

public class UploadTableInvalidValue extends BaseUploadMessage implements Comparable<UploadTableInvalidValue>
{
    protected final UploadTable uploadTbl;
    protected final UploadField uploadFld;
    protected final Integer     rowNum;
    protected final Exception   cause;

    /**
     * @param uploadTbl
     * @param uploadFld
     * @param rowNum
     * @param issueName
     * @param description
     */
    public UploadTableInvalidValue(final String baseMsg, final UploadTable uploadTbl, final UploadField uploadFld, int rowNum,
            final Exception cause) 
    {
        super(baseMsg);
        this.uploadTbl = uploadTbl;
        this.uploadFld = uploadFld;
        this.rowNum = new Integer(rowNum);
        this.cause = cause;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        if (cause == null) 
        { 
            return null; 
        }
        if (cause instanceof UploaderException)
        {
            if (cause.getCause() != null && cause.getCause().getMessage() != null)
            {
                return cause.getCause().getMessage();
            }
        }
        if (cause instanceof InvocationTargetException)
        {
            InvocationTargetException theCause = (InvocationTargetException)cause;
            if (theCause.getTargetException() != null)
            {
                if (theCause.getTargetException() instanceof NumberFormatException)
                {
                    return getResourceString("WB_UPLOAD_INVALID_NUMBER");// + " " + theCause.getTargetException().getMessage();
                }
            }
        }
        if (cause.getMessage() != null)
        {
            return cause.getMessage();
        }
        return null;
    }

    /**
     * @return the issueName
     */
    public String getIssueName()
    {
        if (cause != null)
            return cause.getClass().getSimpleName();
        return null;
    }

    /**
     * @return the rowNum
     */
    @Override
    public int getRow()
    {
        return rowNum;
    }

    /**
     * @return the uploadFld
     */
    public UploadField getUploadFld()
    {
        return uploadFld;
    }

    /**
     * @return the uploadTbl
     */
    public UploadTable getUploadTbl()
    {
        return uploadTbl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.BaseUploadMessage#getMsg()
     */
    @Override
    public String getMsg()
    {
        if (uploadFld != null)
        {
            return uploadFld.getWbFldName() + " (row " + Integer.toString(rowNum + 1) + "): "
                + getDescription();
        }
        return super.getMsg();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.BaseUploadMessage#getCol()
     */
    @Override
    public int getCol()
    {
        return getUploadFld().getIndex();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.BaseUploadMessage#getData()
     */
    @Override
    public Object getData()
    {
        return cause;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    //@Override
    public int compareTo(UploadTableInvalidValue o)
    {
        if (rowNum != null)
        {
            int result = rowNum.compareTo(o.rowNum);
            if (result != 0)
                return result;
        }
        if (uploadFld != null && o.uploadFld != null)
        {
            if (uploadFld.getIndex() < o.uploadFld.getIndex())
                return -1;
            if (uploadFld.getIndex() > o.uploadFld.getIndex())
                return 1;
        }
        return 0;
    }
    
}
