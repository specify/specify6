/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.ui.forms;

/**
 * Interface that defines a standard way to get data from an object. There are at least three ways:<br>
 * <ul>
 * <li>The data object may be a java object and each field is a data member.</li>
 * <li>The data object may be a resultset (list of objects) and each row may be a field.</li>
 * <li>The data object may be a XML document and the field name may be a XPath to the data.</li></ul>
 * <br>
 
 * @code_status Unknown (auto-generated)
 **
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
    public Object getFieldValue(Object dataObj, String fieldName);

}
