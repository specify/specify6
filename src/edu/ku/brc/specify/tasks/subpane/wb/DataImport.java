/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.Vector;

import edu.ku.brc.specify.tasks.subpane.wb.DataImportIFace.Status;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 */
public class DataImport
{
    protected Vector<DataImportTruncation> truncations;
    protected Status                     status = DataImportIFace.Status.None;
    // maximum length of text to be assigned to a workbench cell
    public static int                         MAX_FIELD_SIZE = 256;

    protected String truncateIfNecessary(final String value,
                                         final int row,
                                         final short col,
                                         final String colHeader)
    {
        if (value.length() <= MAX_FIELD_SIZE) { return value; }
        return trackTrunc(value, row, col, colHeader);
    }

    protected String trackTrunc(final String value,
                                final int row,
                                final short col,
                                final String colHeader)
    {
        truncations.add(new DataImportTruncation(row, col, colHeader, value));
        return value.substring(0, MAX_FIELD_SIZE - 1);
    }

    public Vector<DataImportTruncation> getTruncations()
    {
        return this.truncations;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataImportIFace#getStatus()
     */
    public Status getStatus()
    {
        return status;
    }

    protected DataImport()
    {
        super();
        truncations = new Vector<DataImportTruncation>(10);
    }
    

}
