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
package edu.ku.brc.ui;

/**
 * This interface enables any object to "play nice" within the form system. This way the machinery can always get and set values.
 * (NOTE: this fires a Property change event "setValue")
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public interface GetSetValueIFace
{

    /**
     * Sets a value into the component
     * @param value the new value (NOTE: this fires a Property change event "setValue")
     * @param defaultValue a default value defined as a string, ok if  null
     */
    public void setValue(Object value, String defaultValue);
    
    /**
     * Returns a value for the component
     * @return Returns a value for the component
     */
    public Object getValue();
    
}
