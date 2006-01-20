/* Filename:    $RCSfile: GetSetValueIFace,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/20 01:01:01 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui;

/**
 * This interface enables any object to "play nice" within the form system. This way the machinery can always get and set values.
 * (NOTE: this fires a Property change event "setValue")
 * 
 * @author rods
 *
 */
public interface GetSetValueIFace
{

    /**
     * Sets a value into the component
     * @param value the new value (NOTE: this fires a Property change event "setValue")
     */
    public void setValue(Object value);
    
    /**
     * Returns a value for the component
     * @return Returns a value for the component
     */
    public Object getValue();
    
}
