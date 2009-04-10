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
package edu.ku.brc.util;

import java.io.Serializable;

/**
 * A utility class that makes it to group a pair of objects.  This can
 * be very useful when a method would benefit from returning two objects.
 * NOTE: the data members of this class are public for ease of access.
 * This is not a code encapsulation problem.
 * 
 * @see Triple
 * @code_status Complete
 * @author jstewart
 *
 * @param <F> the first item in the pair
 * @param <S> the second item in the pair
 */
@SuppressWarnings("serial")
public class Pair<F,S> implements Serializable
{
	/** The first value in the <code>Pair</code>. */
	public F first = null;
	
	/** The second value in the <code>Pair</code>. */
	public S second = null;
	
	/**
	 * Construct a new <code>Pair</code> with <code>null</code> values.
	 */
	public Pair()
	{
		super();
	}
	
	/**
	 * Construct a new <code>Pair</code> with the given values.
	 * 
	 * @param first the value of <code>first</code>
	 * @param second the value of <code>second</code>
	 */
	public Pair(F first, S second)
	{
		this.first = first;
		this.second = second;
	}
	
	/**
	 * Returns the value of <code>first</code>.
	 * 
	 * @see #setFirst(Object)
	 * @return the value of <code>first</code>
	 */
	public F getFirst()
	{
		return first;
	}

	/**
	 * Sets the value of <code>first</code>.
	 * 
	 * @see #getFirst()
	 * @param first the value of <code>first</code>
	 */
	public void setFirst(F first)
	{
		this.first = first;
	}

	/**
	 * Returns the value of <code>second</code>.
	 * 
	 * @see #setSecond(Object)
	 * @return the value of <code>second</code>
	 */
	public S getSecond()
	{
		return second;
	}

	/**
	 * Sets the value of <code>second</code>.
	 * 
	 * @see #getSecond()
	 * @param second the value of <code>second</code>
	 */
	public void setSecond(S second)
	{
		this.second = second;
	}

	/**
	 * Returns a string representation of the object.
	 * 
	 * @see java.lang.Object#toString()
	 * @return the string representation
	 */
	@Override
	public String toString()
	{
		return "{" + first + "," + second + "}";
	}
}
