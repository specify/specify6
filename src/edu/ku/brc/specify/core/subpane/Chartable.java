/* Filename:    $RCSfile: Chartable.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
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
package edu.ku.brc.specify.core.subpane;


/**
 * Interface that enables chart speciic informations to be set
 * 
 * @author rods
 * 
 */
public interface Chartable
{

    /**
     * Sets the title of the chart
     * @param title the title
     */
    public void setTitle(final String title);
    
    /**
     * Sets the X Axis label if applicable
     * @param title the title
     */
    public void setXAxis(final String title);
    
    /**
     * Sets the Y Axis label if applicable
     * @param title the title
     */
    public void setYAxis(final String title);
    
    /** 
     * Set whether the chart is vertical or horizontal (vertical is the default)
     * @param isVertical true if vertical 
     */
    public void setVertical(boolean isVertical);
    



}
