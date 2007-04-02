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
public class DataImportTruncation
{
    protected int    row;
    protected short    col;
    protected String colHeader;
    protected String originalValue;
    protected String truncatedValue;

    /**
     * @return the originalValue
     */
    public String getOriginalValue()
    {
        return originalValue;
    }

    /**
     * @return the row
     */
    public int getRow()
    {
        return row;
    }

    /**
     * @return the truncatedValue
     */
    public String getTruncatedValue()
    {
        return truncatedValue;
    }

    /**
     * @return the col
     */
    public short getCol()
    {
        return col;
    }

    /**
     * @return the colHeader
     */
    public String getColHeader()
    {
        return colHeader;
    }

    public DataImportTruncation(int row, short col, String colHeader, String originalValue,
            String truncatedValue)
    {
        super();
        this.row = row;
        this.col = col;
        this.colHeader = colHeader;
        this.originalValue = originalValue;
        this.truncatedValue = truncatedValue;
    }

}
