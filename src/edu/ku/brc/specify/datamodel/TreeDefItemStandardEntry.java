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
package edu.ku.brc.specify.datamodel;

import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Describes a 'standard' tree level such as Genus, Species, Country...
 */
public class TreeDefItemStandardEntry implements Comparable<TreeDefItemStandardEntry>
{
    protected final String name;
	protected final String title;
    protected final int rank;
    /**
     * @param name
     * @param rank
     */
    public TreeDefItemStandardEntry(String name, int rank)
    {
        super();
        this.name = name;
        this.title = UIRegistry.getResourceString(name);
        this.rank = rank;
    }
    /**
     * @return the name
     */
    public String getTitle()
    {
        return title;
    }
    /**
     * @return the rank
     */
    public int getRank()
    {
        return rank;
    }
    
    
    /**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getTitle();
    }
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TreeDefItemStandardEntry arg0)
	{
		
		return rank <  arg0.getRank() ? -1 : (rank == arg0.getRank() ? 0 : -1); 
	}
    
    
}
