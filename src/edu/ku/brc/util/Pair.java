package edu.ku.brc.util;

import java.io.Serializable;

/**
 * A utility class that makes it to group a pair of objects.  This can
 * be very useful when a method would benefit from returning two objects.
 * NOTE: the data members of this class are public for ease of access.
 * This is not a code encapsulation problem.
 * 
 * @see Triple
 * @author jstewart
 * @version %I% %G%
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
	public String toString()
	{
		return "{" + first.toString() + "," + second.toString() + "}";
	}
}
