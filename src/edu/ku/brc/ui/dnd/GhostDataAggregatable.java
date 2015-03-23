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
package edu.ku.brc.ui.dnd;

/**
 * Interface that enables data objects to be an aggregation. 
 * This way consumers can ask for a specific class of a data object to be returned.
 *
 * @code_status Beta
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
    public Object getDataForClass(Class<?> classObj);
    

}
