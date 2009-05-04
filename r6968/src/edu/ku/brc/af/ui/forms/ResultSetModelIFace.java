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
package edu.ku.brc.af.ui.forms;

 /**
  * Interface that enables access to a sequential list of data objects. Each data object represents a record or row.
  * Individual field data can be accessed also.
  *
 * @code_status Beta
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
