/* Copyright (C) 2013, University of Kansas Center for Research
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


/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Used to specify a mapping from a workbench column to a field in the database.
 */
public class UploadMappingDef
{
	/**
	 * Name of the table containing the field to be mapped to.
	 */
	protected String table;
	/**
	 * Name of the field to be mapped to.
	 */
	protected String field;
	/**
	 * column index (for maps that don't involve relatedFields)
	 */
	protected int index = -1; 
    /**
     * caption of the field in the workbench. (null when index == -1) 
     */
    protected String wbFldName = null;
	/**
	 * @param table
	 * @param field
	 */
	public UploadMappingDef(String table, String field)
	{
		super();
		this.table = table;
		this.field = field;
	}
	/**
	 * @return the field
	 */
	public String getField()
	{
		return field;
	}
	/**
	 * @param field the field to set
	 */
	public void setField(String field)
	{
		this.field = field;
	}
	/**
	 * @return the table
	 */
	public String getTable()
	{
		return table;
	}
	/**
	 * @param table the table to set
	 */
	public void setTable(String table)
	{
		this.table = table;
	}
	/**
	 * @param table
	 * @param field
	 */
	public UploadMappingDef(String table, String field, int index, String wbFldName)
	{
		super();
		this.table = table;
		this.field = field;
		this.index = index;
        this.wbFldName = wbFldName;
	}
	
	/**
	 * @return the index
	 */
	public int getIndex()
	{
		return index;
	}
    /**
     * @return the wbFldName
     */
    public String getWbFldName()
    {
        return wbFldName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + ": " + wbFldName + "(" + String.valueOf(index) + ") -> " + table + "." + field;
    }
}
