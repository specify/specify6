/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public abstract class DaterExpression {

	final String regex;
	
	public DaterExpression(String regex) {
		super();
		this.regex = regex;
	}
	
	/**
	 * @param text
	 * @return
	 */
	public boolean matches(String text) {
		if (text != null) {
			return text.matches(regex);
		} else {
			return false;
		}
	}
	
	/**
	 * @param text
	 * @return
	 */
	public abstract Undateable getDate(String text) throws DaterException;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
