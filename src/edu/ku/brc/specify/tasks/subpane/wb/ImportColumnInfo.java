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
    public enum ColumnType {Integer, Float, Double, Short, Boolean, Date, String}
    
    protected Short      colInx;
    protected ColumnType colType;
    protected String     colName;
    protected String     data;
    
    public ImportColumnInfo(short colInx, ColumnType colType, String colName, String data)
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

    public ColumnType getColType()
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

    public Short getColInx()
    {
        return colInx;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ImportColumnInfo obj)
    {
        return colInx.compareTo(obj.colInx);
    }
    
    
    /**
     * Converts data type string into an enum.
     * @param type the string with the database type
     * @return the enum
     */
    public static ColumnType getType(final String type)
    {
        if (type.equals("java.lang.Integer"))
        {
            return ImportColumnInfo.ColumnType.Integer;
            
        } else if (type.equals("java.lang.Float"))
        {
            return ImportColumnInfo.ColumnType.Float;
            
        } else if (type.equals("java.lang.Double"))
        {
            return ImportColumnInfo.ColumnType.Double;
            
        } else if (type.equals("java.lang.Short") || type.equals("short"))
        {
            return ImportColumnInfo.ColumnType.Short;
            
        } else if (type.equals("calendar_date"))
        {
            return ImportColumnInfo.ColumnType.Date;
        }
        return ImportColumnInfo.ColumnType.String;
    }
    
}
