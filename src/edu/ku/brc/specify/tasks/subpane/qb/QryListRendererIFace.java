/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tasks.subpane.qb;

/**
 * An interface for rendering Query items in a list.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public interface QryListRendererIFace
{
    /**
     * @return the icon name
     */
    public String getIconName();
    
    /**
     * @return the textual title
     */
    public String getTitle();
    
    /**
     * @return whether it has children
     */
    public boolean hasChildren();
    
    /**
     * This should only be called if hasChildren is true.
     * 
     * @return false means it has only a single child (OneToOne or ManyToOne),
     * true means there are many children (ManyToMany, OneToMany)
     */
    public boolean hasMultiChildren();
    
    /**
     * @return whether it is in use
     */
    public Boolean getIsInUse();
    
    /**
     * @param isInUse set whether it is in use
     */
    public void setIsInUse(Boolean isInUse);
    
}
