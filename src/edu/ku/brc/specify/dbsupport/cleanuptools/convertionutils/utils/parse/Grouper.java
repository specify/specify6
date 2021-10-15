/**
 * 
 */
package utils.parse;

/**
 * @author tnoble
 *
 */
public class Grouper {
	protected final String opener;
	protected final String closer;
	/**
	 * @return the opener
	 */
	public String getOpener() {
		return opener;
	}
	/**
	 * @return the closer
	 */
	public String getCloser() {
		return closer;
	}
	/**
	 * @param opener
	 * @param closer
	 */
	public Grouper(String opener, String closer) {
		super();
		this.opener = opener;
		this.closer = closer;
	}
	
}
