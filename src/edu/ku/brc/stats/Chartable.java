/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.stats;


/**
 * Interface that enables chart speciic informations to be set
 
 * @code_status Unknown (auto-generated)
 **
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
