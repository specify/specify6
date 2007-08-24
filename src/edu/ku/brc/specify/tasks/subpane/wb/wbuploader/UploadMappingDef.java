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
	public UploadMappingDef(String table, String field, int index)
	{
		super();
		this.table = table;
		this.field = field;
		this.index = index;
	}
	
	/**
	 * @return the index
	 */
	public int getIndex()
	{
		return index;
	}
}
