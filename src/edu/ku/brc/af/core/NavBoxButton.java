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
package edu.ku.brc.af.core;

import javax.swing.ImageIcon;

import edu.ku.brc.ui.RolloverCommand;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class NavBoxButton extends RolloverCommand implements NavBoxItemIFace
{

    /**
     * Constructor.
     */
    public NavBoxButton()
    {
        // nothing to do 
    }

    /**
     * Constructor.
     * @param label the text label
     * @param imgIcon the image icon
     */
    public NavBoxButton(String label, ImageIcon imgIcon)
    {
        super(label, imgIcon);
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#toString()
     */
    public String toString()
    {
        return label;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final NavBoxItemIFace obj)
    {
        return getTitle().toLowerCase().compareTo(obj.getTitle().toLowerCase());
    }
}
