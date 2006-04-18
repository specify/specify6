package edu.ku.brc.util;

public class Triple<F,S,T> extends Pair<F,S>
{
	public T third;
	
	public Triple()
	{
		super();
	}
	
	public Triple(F first,S second,T third)
	{
		super(first,second);
		this.third = third;
	}

	/**
	 * @return Returns the third.
	 */
	public T getThird()
	{
		return third;
	}

	/**
	 * @param third The third to set.
	 */
	public void setThird(T third)
	{
		this.third = third;
	}
	
	public String toString()
	{
		return "{" + first.toString() + "," + second.toString() + "," + third.toString() + "}";
	}
}
