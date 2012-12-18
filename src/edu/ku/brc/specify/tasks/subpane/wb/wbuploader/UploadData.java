/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;

/**
 * @author timo
 *
 * A simple pairing of workbench upload mappings with the data in the workbench being uploaded.
 */
public class UploadData
{
    /**
     * Mappings for the columns in the workbench being uploaded.
     */
    Vector<UploadMappingDef> mappings;
    /**
     * The rows of the workbench being uploaded.
     */
    Vector<WorkbenchRow>     wbRows;

    /**
     * @param m
     * @return mapping m
     */
    public UploadMappingDef getMapping(int m)
    {
        return mappings.get(m);
    }

    /**
     * @param r
     * @return WorkbenchRow r
     */
    public WorkbenchRow getWbRow(int r)
    {
        return wbRows.get(r);
    }

    /**
     * @param row
     * @param col
     * @return Workbench data for row and col
     */
    public String get(int row, int col)
    {
        return wbRows.get(row).getData(col);
    }

    /**
     * @param row
     * @return true if all cells in row are blank
     */
    public boolean isEmptyRow(int row)
    {
    	if (row < 0 || row >= getRows())
    	{
    		return true;
    	}
    	
    	WorkbenchRow wbrow = getWbRow(row);
    	Hashtable<Short, WorkbenchDataItem> items = wbrow.getItems();
    	for (WorkbenchDataItem di : items.values())
    	{
    		if (!StringUtils.isBlank(di.getCellData()))
    		{
    			return false;
    		}
    	}
    	return true;
    }
    /**
     * @return the number rows
     */
    public int getRows()
    {
        return wbRows.size();
    }

    /**
     * @return number of columns mapped
     */
    public int getCols()
    {
        return mappings.size();
    }

    /**
     * @param mappings
     * @param wbRows
     */
    public UploadData(Vector<UploadMappingDef> mappings, Vector<WorkbenchRow> wbRows)
    {
        this.mappings = mappings;
        if (wbRows != null)
        {
        	this.wbRows = wbRows;
        } else
        {
        	this.wbRows = new Vector<WorkbenchRow>();
        }
        
    }
    
    /**
     * @param freshRows - possibly a new collection of rows.
     */
    public void refresh(Vector<WorkbenchRow> freshRows)
    {
        this.wbRows = freshRows;
    }
    
    /**
     * @param wbFldName
     * @return index for column named wbFldName
     */
    public int indexOfWbFldName(String wbFldName)
    {
    	if (wbRows.size() > 0)
    	{
    		for (WorkbenchDataItem wbdi : wbRows.get(0).getWorkbenchDataItems())
    		{
    			if (wbdi.getWorkbenchTemplateMappingItem().getCaption().equals(wbFldName))
    			{
    				return wbdi.getColumnNumber();
    			}
    		}
    	}
    	return -1;
    }
    
}
