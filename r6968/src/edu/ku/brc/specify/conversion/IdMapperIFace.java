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
package edu.ku.brc.specify.conversion;

import edu.ku.brc.ui.ProgressFrame;

/**
 * A Database Hashtable that is for mapping one integer to another.
 * This is usually utilitized for ID mappings
 
 * @code_status Complete
 **
 * @author rods
 *
 */
public interface IdMapperIFace
{

    /**
     * Adds a Key/Value to the hash.
     * @param fromID the integer key
     * @param toID the value (most likely a database ID)
     */
    public abstract void put(int fromID, int toID);

    /**
     * Returns an integer (mostly a table ID).
     * @param key the key (or ID) to use to get the value
     * @return the value of the mapping
     */
    public abstract Integer get(Integer key);

    /**
     * Returns the name of the table.
     * @return the name of the table
     */
    public abstract String getName();

    /**
     * Returns the SQL statement used to fill the table (might be null).
     * @return the SQL statement used to fill the table (might be null)
     */
    public abstract String getSql();

    /**
     * The number of entries in the mapping table.
     * @return the number of entries in the mapping table
     */
    public abstract int size();
    
    /**
     * Sets UI frame for progress.
     * @param frame the frame
     */
    public abstract void setFrame(ProgressFrame frame);

    /**
     * Tells it to show log errors.
     * @param showLogErrors true/false
     */
    public abstract void setShowLogErrors(boolean showLogErrors);
    
    /**
     * The base index is '1' unless otherwise set.
     * @param index the initial index to use for mapping.
     */
    public abstract void setInitialIndex(int index);
}
