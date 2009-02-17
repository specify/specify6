package edu.ku.brc.util;

/**
 * Provides a simple way to group three objects.  This can be very
 * useful when a method would benefit from returning three objects.
 * NOTE: the data members of this class are public for ease of access.
 * This is not a code encapsulation problem.
 *
 * @see Pair
 * @code_status Complete
 * @author jstewart
 * 
 * @param <F> the first type in the <code>Triple</code>
 * @param <S> the second type in the <code>Triple</code>
 * @param <T> the third type in the <code>Triple</code>
 */
@SuppressWarnings("serial")
public class Triple<F,S,T> extends Pair<F,S>
{
	/** The third object in the triple. */
	public T third;
	
	/**
	 * Constructs a <code>Triple</code> having the third member equaling
	 * <code>null</code> and the others equal to the values produced by
	 * {@link Pair#Pair()}.
	 */
	public Triple()
	{
		super();
	}
	
	/**
	 * Constructs a triple having the given member values.
	 * 
	 * @param first the value of <code>first</code>
	 * @param second the value of <code>second</code>
	 * @param third the value of <code>third</code>
	 */
	public Triple(F first,S second,T third)
	{
		super(first,second);
		this.third = third;
	}

	/**
	 * Returns the value of <code>third</code>.
	 * 
	 * @see #setThird(Object)
	 * @return the value of <code>third</code>
	 */
	public T getThird()
	{
		return third;
	}

	/**
	 * Sets the value of <code>third</code>.
	 * 
	 * @see #getThird()
	 * @param third the value of <code>third</code>
	 */
	public void setThird(T third)
	{
		this.third = third;
	}
	
	/**
	 * Returns a string representation of the <code>Triple</code>.
	 * Overrides {@link Pair#toString()}.
	 * 
	 * @see edu.ku.brc.util.Pair#toString()
	 * @return the string representation
	 */
	@Override
	public String toString()
	{
		return "{" + first.toString() + "," + second.toString() + "," + third.toString() + "}";
	}
}
