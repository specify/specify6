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

/**
 * Interface that defines a standard way to get data from an object. There are at least three ways:<br>
 * <ul>
 * <li>The data object may be a java object and each field is a data member.</li>
 * <li>The data object may be a resultset (list of objects) and each row may be a field.</li>
 * <li>The data object may be a XML document and the field name may be a XPath to the data.</li></ul>
 * <br>
 * 
 * @code_status Beta
 *
 * @author rods
 *
 */
public interface DataObjectGettable
{
    /**
     * Returns a field's value
     * @param dataObj the data object that contains the field
     * @param fieldName the fields name
     * @return the value of the field
     */
    public abstract Object getFieldValue(Object dataObj, String fieldName);
    
    /**
     * Returns true if dots (".") should be used to walk an object tree when getting the value, or
     * false when "." in the name are meaningless.
     * @return true if dots (".") should be used to walk an object tree when getting the value, or
     * false when "." in the name are meaningless.
     */
    public abstract boolean usesDotNotation();

}
