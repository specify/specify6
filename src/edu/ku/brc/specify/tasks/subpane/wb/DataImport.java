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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.Vector;

import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
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

//    protected String truncateIfNecessary(final String value,
//                                         final int row,
//                                         final short col,
//                                         final String colHeader)
//    {
//        if (value.length() <= WorkbenchDataItem.getCellDataLength()) { return value; }
//        return trackTrunc(value, row, col, colHeader);
//    }

    protected String truncateIfNecessary(final String value,
                                         final int row,
                                         final WorkbenchTemplateMappingItem wbtmi)
    {
        int maxLen = WorkbenchDataItem.getMaxWBCellLength();
        if (/*wbtmi.getFieldInfo().getLength()*/wbtmi.getDataFieldLength() != null && wbtmi.getDataFieldLength() != -1 && /*wbtmi.getFieldInfo().getLength()*/ wbtmi.getDataFieldLength() < maxLen)
        {
            maxLen = wbtmi.getDataFieldLength()/*wbtmi.getFieldInfo().getLength()*/;
        }
        if (value == null || value.length() <= maxLen ) 
        { 
        	return value; 
        }
        return trackTrunc(value, row, wbtmi.getViewOrder(), wbtmi.getCaption(), maxLen);
    }

//    protected String trackTrunc(final String value,
//                                final int row,
//                                final short col,
//                                final String colHeader)
//    {
//        truncations.add(new DataImportTruncation(row, col, WorkbenchDataItem.getCellDataLength(), colHeader, value));
//        return value.substring(0, WorkbenchDataItem.getCellDataLength() - 1);
//    }

    protected String trackTrunc(final String value,
                                final int row,
                                final short col,
                                final String colHeader,
                                final int len)
    {
        truncations.add(new DataImportTruncation(row, col, len, colHeader, value));
        return value.substring(0, len);
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
