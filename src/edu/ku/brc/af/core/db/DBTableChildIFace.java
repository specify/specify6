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
package edu.ku.brc.af.core.db;


/**
 * Interface for common methods for Fields and Relationships for a DBTableInfo.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 14, 2008
 *
 */
public interface DBTableChildIFace
{
    /**
     * @return the description
     */
    public abstract String getDescription();
    
    /**
     * @param description the description
     */
    public abstract void setDescription(String description);

    /**
     * @return the name
     */
    public abstract String getTitle();
    
    /**
     * @param title the title
     */
    public abstract void setTitle(String title);

    /**
     * @return the name
     */
    public abstract String getName();

    /**
     * @return the isHidden
     */
    public abstract boolean isHidden();
    
    /**
     * @param isHidden whether it is hidden
     */
    public abstract void setHidden(boolean isHidden);
    
    /**
     * @return whether it can be updated or not
     */
    public abstract boolean isUpdatable();
    
    /**
     * @return whether the field is required
     */
    public abstract boolean isRequired();
    
    /**
     * @return the data class of the child.
     */
    public abstract Class<?> getDataClass();
}
