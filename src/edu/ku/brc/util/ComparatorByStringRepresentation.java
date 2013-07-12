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
package edu.ku.brc.util;

import java.util.Comparator;

public class ComparatorByStringRepresentation<T> implements Comparator<T>
{
	protected boolean ignoreCase;
	
	public ComparatorByStringRepresentation()
	{
		this(false);
	}
	
	public ComparatorByStringRepresentation(boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
	}
	
    public int compare(T o1, T o2)
    {
    	int result;
    	if (ignoreCase)
    	{
            result = o1.toString().compareToIgnoreCase(o2.toString());
            return result;
    	}
    	
    	result = o1.toString().compareTo(o2.toString());
        return result;
    }
}
