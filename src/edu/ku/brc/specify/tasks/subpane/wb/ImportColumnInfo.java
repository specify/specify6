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

import java.util.Calendar;
import java.util.Date;



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
    public static ColumnType getType(final Class<?> type)
    {
        if (type != null)
        {
            if (type.equals(Integer.class))
            {
                return ImportColumnInfo.ColumnType.Integer;
                
            } else if (type.equals(Float.class))
            {
                return ImportColumnInfo.ColumnType.Float;
                
            } else if (type.equals(Double.class))
            {
                return ImportColumnInfo.ColumnType.Double;
                
            } else if (type.equals(Short.class))
            {
                return ImportColumnInfo.ColumnType.Short;
                
            } else if (type.equals(Calendar.class) || type.equals(Date.class))
            {
                return ImportColumnInfo.ColumnType.Date;
            }
        }
        return ImportColumnInfo.ColumnType.String;
    }
    
}
