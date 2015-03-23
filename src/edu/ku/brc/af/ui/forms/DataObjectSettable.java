/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public interface DataObjectSettable
{

    /**
     * Sets data for a field into the parent data objectinto 
     * @param dataObj the parent data object
     * @param fieldName the name of the field
     * @param data the field's data
     */
    public void setFieldValue(Object dataObj, String fieldName, Object data);
    
    /**
     * Returns true if dots (".") should be used to walk an object tree when setting the value, or
     * false when "." in the name are meaningless.
     * @return true if dots (".") should be used to walk an object tree when setting the value, or
     * false when "." in the name are meaningless.
     */
    public boolean usesDotNotation();
}
