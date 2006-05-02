/*
 * Filename:    $RCSfile: IdMapper.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.0 $
 * Date:        $Date: 2005/10/20 12:53:02 $
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
package edu.ku.brc.specify.conversion;

/**
 * A Database Hashtable that is for mapping one integer to another.
 * This is usually utilitized for ID mappings
 *
 * @author rods
 *
 */
public interface IdMapper
{

    /**
     * Adds a Key/Value to the hash
     * @param key the integer key
     * @param value the value (most likely a database ID)
     */
    public void put(int key, int value);

    /**
     * Returns an integer (mostly a table ID)
     * @param key the key (or ID) to use to get the value
     * @return the value of the mapping
     */
    public Integer get(Integer key);

    /**
     * Returns the name of the table
     * @return the name of the table
     */
    public String getName();

    /**
     * Returns the SQL statement used to fill the table (might be null)
     * @return the SQL statement used to fill the table (might be null)
     */
    public String getSql();

    /**
     * The number of entries in the mapping table
     * @return the number of entries in the mapping table
     */
    public int size();


}
