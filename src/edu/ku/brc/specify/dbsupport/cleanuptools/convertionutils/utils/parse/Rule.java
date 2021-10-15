/**
 * 
 */
package utils.parse;

import java.util.List;

/**
 * @author timo
 *
 */
public class Rule implements Comparable<Rule> 
{
	protected final Integer priority; //lower (negative) for high priority. 0 by default.
	protected final String name;
	protected final Symbol left;
	protected final List<Symbol> right;
	
	/**
	 * @param name
	 * @param left
	 * @param right
	 */
	public Rule(String name, Symbol left, List<Symbol> right, int priority) 
	{
		super();
		this.name = name;
		this.left = left;
		this.right = right;
		this.priority = priority;
	}


	/**
	 * @param left
	 * @param right
	 */
	public Rule(Symbol left, List<Symbol> right) {
		this("Anynonymous", left, right, 0);
	}

	/**
	 * @return the right
	 */
	public Symbol getLeft()
	{
		return left;
	}

	/**
	 * @return the left
	 */
	public List<Symbol> getRight()
	{
		return right;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		String result = left.toString() + "->";
		for (Symbol r : right)
		{
			result += r.toString();
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Rule arg0) {
		// TODO Auto-generated method stub
		return priority.compareTo(arg0.priority);
	}
	
	
}
