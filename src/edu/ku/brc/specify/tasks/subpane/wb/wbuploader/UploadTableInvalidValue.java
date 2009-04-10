/* Copyright (C) 2009, University of Kansas Center for Research
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
