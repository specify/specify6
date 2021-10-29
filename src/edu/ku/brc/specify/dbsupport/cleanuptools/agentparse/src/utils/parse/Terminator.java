/**
 * 
 */
package utils.parse;

/**
 * @author tnoble
 *
 */
public class Terminator {
	protected final String text;
	protected final boolean include; //if true then terminator is appended to token it terminated
	/**
	 * @param text
	 * @param include
	 */
	public Terminator(String text, boolean include) {
		super();
		this.text = text;
		this.include = include;
	}
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @return the include
	 */
	public boolean isInclude() {
		return include;
	}
	
	
}
