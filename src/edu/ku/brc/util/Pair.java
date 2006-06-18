package edu.ku.brc.util;

import java.io.Serializable;

/**
 * A utility class that makes it easy for a method to return a pair of objects
 * rather than just one.  NOTE: the data members of this class are public for
 * ease of access.  This is not a code encapsulation problem.
 * 
 * @author jstewart
 *
 * @param <F> the first item in the pair
 * @param <S> the second item in the pair
 */
@SuppressWarnings("serial")
public class Pair<F,S> implements Serializable
{
	public F first = null;
	public S second = null;
	
	public Pair()
	{
		super();
	}
	
	public Pair(F first, S second)
	{
		this.first = first;
		this.second = second;
	}
	
	/**
	 * @return Returns the first item in the Pair.
	 */
	public F getFirst()
	{
		return first;
	}

	/**
	 * @param first The first item to set.
	 */
	public void setFirst(F first)
	{
		this.first = first;
	}

	/**
	 * @return Returns the second item in the Pair.
	 */
	public S getSecond()
	{
		return second;
	}

	/**
	 * @param second The second to set.
	 */
	public void setSecond(S second)
	{
		this.second = second;
	}

	public String toString()
	{
		return "{" + first.toString() + "," + second.toString() + "}";
	}
}
