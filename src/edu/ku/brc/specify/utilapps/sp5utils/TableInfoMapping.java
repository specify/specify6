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
package edu.ku.brc.specify.utilapps.sp5utils;

import java.util.HashMap;

/**
 * Data Object for Mapping Old Table Info to New Table Info.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 8, 2009
 *
 */
public class TableInfoMapping
{
    private String                  newTableName;
    private String                  oldTableName;
    private HashMap<String, String> fieldMappings = new HashMap<String, String>();
    
    /**
     * @param newTableName
     * @param oldTableName
     * @param fieldMappings
     */
    public TableInfoMapping(final String newTableName, 
                            final String oldTableName,
                            final HashMap<String, String> fieldMappings)
    {
        super();
        this.newTableName = newTableName;
        this.oldTableName = oldTableName;
        this.fieldMappings = fieldMappings;
    }
    /**
     * @return the newTableName
     */
    public String getNewTableName()
    {
        return newTableName;
    }
    /**
     * @return the oldTableName
     */
    public String getOldTableName()
    {
        return oldTableName;
    }
    /**
     * @return the fieldMappings
     */
    public HashMap<String, String> getFieldMappings()
    {
        return fieldMappings;
    }
}
