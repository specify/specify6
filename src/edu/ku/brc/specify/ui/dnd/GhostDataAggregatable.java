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
package edu.ku.brc.specify.ui.dnd;

/**
 * Interface that enables data objects to be an aggregation. 
 * This way consumers can ask for a specific class of a data object to be returned.
 * 
 * @author rods
 *
 */
public interface GhostDataAggregatable
{
    /**
     * Support Aggregation, allow consumers to ask for data of a specific class. 
     * That way Any object implementing this interface can provide data of many different types
     * include "Object"
     * If the object can't vend it it will return null.
     * @param classObj the object of this desired class type
     * @return the object representing that type of object
     */
    public Object getDataForClass(Class classObj);
    

}
