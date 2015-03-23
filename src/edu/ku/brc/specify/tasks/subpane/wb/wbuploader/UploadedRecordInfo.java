/* Copyright (C) 2015, University of Kansas Center for Research
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

import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 *Stores informtion about records created during a workbench upload.
 *
 * @code_status Alpha
 *
 */
@SuppressWarnings("serial")
public class UploadedRecordInfo extends Pair<Integer, Integer> implements Comparable<UploadedRecordInfo>
{
    protected final int seq;
    protected final Object autoAssignedVal; //value of auto-assigned field for the record. (Assuming there will never be more than one)
    protected final Boolean isUpdate;
    protected final DataModelObjBase originalData;
    protected final String tblName;
    
    /**
     * @param key
     * @param wbRow
     * @param seq
     */
    public UploadedRecordInfo(final Integer key, final Integer wbRow, final int seq, final Object autoAssignedVal)
    {
        this(key, wbRow, seq, autoAssignedVal, false, null, null);
    }
    
    public UploadedRecordInfo(final Integer key, final Integer wbRow, final int seq, final Object autoAssignedVal, final boolean isUpdate, 
    		final DataModelObjBase originalData, final String tblName)
    {
        super(key, wbRow);
        this.seq = seq;
        this.autoAssignedVal = autoAssignedVal;
        this.isUpdate = isUpdate;
        this.originalData = originalData;
        this.tblName = tblName;
    }

    /**
     * @return the record key.
     */
    public Integer getKey()
    {
        return getFirst();
    }
    
    /**
     * @return the workbench row that produced the record.
     */
    public Integer getWbRow()
    {
        return getSecond();
    }

    
    /**
     * @return the autoAssignedVal
     */
    public Object getAutoAssignedVal()
    {
        return autoAssignedVal;
    }

    /**
     * @return the seq
     */
    public int getSeq()
    {
        return seq;
    }

    
    /**
	 * @return the isUpdate
	 */
	public Boolean getIsUpdate() {
		return isUpdate;
	}

	/* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    //@Override
    public int compareTo(UploadedRecordInfo o)
    {
        //return getKey().compareTo(o.getKey());
    	int result = getIsUpdate().compareTo(o.getIsUpdate());
    	if (result == 0 && getWbRow() != null && o.getWbRow() != null)
    	{
    		result = getWbRow().compareTo(o.getWbRow());
    	}
        if (result == 0 && getKey() != null && o.getKey() != null)
        {
            result = getKey().compareTo(o.getKey());
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        UploadedRecordInfo otherObj = (UploadedRecordInfo )obj;
    	if (getKey() != null && otherObj.getKey() != null)
    	{
    		return getKey().intValue() == otherObj.getKey().intValue();
    	}
    	if (getKey() == null && otherObj.getKey() == null)
    	{
    		return getWbRow().equals(otherObj.getWbRow());
    	}
    	return false;
    }

	/**
	 * @return the tblName
	 */
	public String getTblName()
	{
		return tblName;
	}
    
    
}
