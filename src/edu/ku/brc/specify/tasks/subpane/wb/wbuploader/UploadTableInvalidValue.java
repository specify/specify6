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
import java.util.List;
import java.util.Vector;

import edu.ku.brc.ui.UIRegistry;

public class UploadTableInvalidValue extends BaseUploadMessage implements Comparable<UploadTableInvalidValue>
{
    protected final UploadTable uploadTbl;
    protected final List<UploadField> uploadFlds = new Vector<UploadField>();
    protected final Integer     rowNum;
    protected final Exception   cause;
    protected final boolean		warn;

    /**
     * @param baseMsg
     * @param uploadTbl
     * @param uploadFld
     * @param rowNum
     * @param cause
     */
    public UploadTableInvalidValue(final String baseMsg, final UploadTable uploadTbl, final UploadField uploadFld, int rowNum,
            final Exception cause) 
    {
        this(baseMsg, uploadTbl, uploadFld, null, rowNum, cause, false);
    }

    /**
     * @param baseMsg
     * @param uploadTbl
     * @param uploadFlds
     * @param rowNum
     * @param cause
     */
    public UploadTableInvalidValue(final String baseMsg, final UploadTable uploadTbl, List<UploadField> uploadFlds, int rowNum,
            final Exception cause) 
    {
        this(baseMsg, uploadTbl, null, uploadFlds, rowNum, cause, false);
    }

    /**
     * @param baseMsg
     * @param uploadTbl
     * @param rowNum
     * @param cause
     */
    public UploadTableInvalidValue(final String baseMsg, final UploadTable uploadTbl, int rowNum,
            final Exception cause) 
    {
        this(baseMsg, uploadTbl, null, null, rowNum, cause, false);
    }
    
    /**
     * @param baseMsg
     * @param uploadTbl
     * @param uploadFld
     * @param uploadFlds
     * @param rowNum
     * @param cause
     */
    protected UploadTableInvalidValue(final String baseMsg, final UploadTable uploadTbl, UploadField uploadFld, List<UploadField> uploadFlds, int rowNum,
            final Exception cause, boolean warn)
    {
    	super(baseMsg);
        this.uploadTbl = uploadTbl;
        if (uploadFld != null)
        {
        	this.uploadFlds.add(uploadFld);
        }
        if (uploadFlds != null)
        {
        	this.uploadFlds.addAll(uploadFlds);
        }
        this.rowNum = new Integer(rowNum);
        this.cause = cause;    	
        this.warn = warn;
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
        if (uploadFlds.size() > 0)
        {
        	return uploadFlds.get(0);
        }
        return null;
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
        if (getUploadFld() != null)
        {
            return getUploadFld().getWbFldName() + " (row " + Integer.toString(rowNum + 1) + "): "
                + getDescription();
        }
        if (cause != null)
        {
        	return "(row " + Integer.toString(rowNum + 1) + "): " + getDescription();
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
        if (getUploadFld() != null)
        {
        	return getUploadFld().getIndex();
        }
        return -1;
    }

    /**
     * @return column indexes associated with the invalid condition.
     */
    public List<Integer> getCols()
    {
    	if (uploadFlds.size() == 0) return null;
    	
    	Vector<Integer> result = new Vector<Integer>(uploadFlds.size());
    	for (int f = 0; f < uploadFlds.size(); f++)
    	{
    		result.add(uploadFlds.get(f).getIndex());
    	}
    	return result;
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
        if (getUploadFld() != null && o.getUploadFld() != null)
        {
            if (getUploadFld().getIndex() < o.getUploadFld().getIndex())
                return -1;
            if (getUploadFld().getIndex() > o.getUploadFld().getIndex())
                return 1;
        }
        return 0;
    }
    
    /**
     * @return true if invalid null value.
     */
    public boolean isInvalidNull()
    {
    	return cause != null && cause.getMessage().equals(UIRegistry.getResourceString("WB_UPLOAD_FIELD_MUST_CONTAIN_DATA"));
    }

	/**
	 * @return the warn
	 */
	public boolean isWarn() 
	{
		return warn;
	}
    
    
}
