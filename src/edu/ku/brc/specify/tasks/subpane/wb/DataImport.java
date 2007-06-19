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

import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
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
    protected Vector<String>    messages;
    protected Status                     status = DataImportIFace.Status.None;
    // maximum length of text to be assigned to a workbench cell

    public static String IMAGE_PATH_HEADING = "cardImagePath";
    public static String GEO_DATA_HEADING = "bioGeomancerResults";
    
    public static boolean isSystemColumn(final String columnName)
    {
        return columnName.equals(IMAGE_PATH_HEADING) || columnName.equals(GEO_DATA_HEADING);
    }

    protected String truncateIfNecessary(final String value,
                                         final int row,
                                         final short col,
                                         final String colHeader)
    {
        if (value.length() <= WorkbenchDataItem.getCellDataLength()) { return value; }
        return trackTrunc(value, row, col, colHeader);
    }

    protected String trackTrunc(final String value,
                                final int row,
                                final short col,
                                final String colHeader)
    {
        truncations.add(new DataImportTruncation(row, col, colHeader, value));
        return value.substring(0, WorkbenchDataItem.getCellDataLength() - 1);
    }

    public Vector<DataImportTruncation> getTruncations()
    {
        return this.truncations;
    }
    
    public Vector<String> getMessages()
    {
        return this.messages;
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
        messages = new Vector<String>(10);
    }
    

}
