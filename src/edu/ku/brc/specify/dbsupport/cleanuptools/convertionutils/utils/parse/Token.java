/**
 * 
 */
package utils.parse;

/**
 * @author timo
 *
 */
public class Token 
{
	protected final String name;
	protected final String value;
	protected final String terminatedBy;
	
	/**
	 * @param name
	 * @param value
	 */
	public Token(String name, String value, String terminatedBy) 
	{
		super();
		this.name = name;
		this.value = value;
		this.terminatedBy = terminatedBy;
	}

	/**
	 * @return the name
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * @return the value
	 */
	public String getValue() 
	{
		return value;
	}

	
	/**
	 * @return the terminatedBy
	 */
	public String getTerminatedBy() {
		return terminatedBy;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		return name + " = " + value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Token) {
			Token arg = (Token) obj;
			return getName().equals(arg.getName()) && getValue().equals(arg.getValue());
		}
		return false;
	}
	
	
}
