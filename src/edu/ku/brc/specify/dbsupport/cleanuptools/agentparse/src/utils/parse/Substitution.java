/**
 * 
 */
package utils.parse;

/**
 * @author tnoble
 *
 */
public class Substitution {
	protected final String replace;
	protected final String with;
	/**
	 * @param replace
	 * @param with
	 */
	public Substitution(String replace, String with) {
		super();
		this.replace = replace;
		this.with = with;
	}
	/**
	 * @return the replace
	 */
	public String getReplace() {
		return replace;
	}
	/**
	 * @return the with
	 */
	public String getWith() {
		return with;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getReplace() + " -> " + getWith();
	}	
	
	
}
