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

package edu.ku.brc.specify.ui.forms;

 /**
  * Interface that enables access to a sequential list of data objects. Each data object represents a record or row.
  * Individual field data can be accessed also.
  * 
  * @author rods
  *
  */
public interface ResultSetModelIFace
{

    /**
     * Returns a Data Item (object/record) from the sequentiual list 
     * @param aIndex the index of the item to be retrieved
     * @return the data item to be returned
     */
    public Object getDataItem(int aIndex);
    
    /**
     * Return the data for a named field
     * @param aDataItem the data item containg the desired field
     * @param aFieldName the name of the field
     * @return the data for the field
     */
    public Object getDataFieldValue(final Object aDataItem, final String aFieldName);
    
    /**
     * 
     * @return returns the number of objects in the sequential list
     */
    public long getSize();
    
}
