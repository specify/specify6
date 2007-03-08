/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;


/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ImportColumnInfo  implements Comparable<ImportColumnInfo>
{
    protected Integer colInx;
    protected int     colType;
    protected String  colName;
    protected String  data;
    
    public ImportColumnInfo(int colInx, int colType, String colName, String data)
    {
        super();
        this.colInx  = colInx;
        this.colType = colType;
        this.colName = colName;
        this.data    = data;
    }

    public String getColName()
    {
        return colName;
    }

    public int getColType()
    {
        return colType;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public void setColName(String colName)
    {
        this.colName = colName;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ImportColumnInfo obj)
    {
        return colInx.compareTo(obj.colInx);
    }
    
}
