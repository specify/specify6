/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class DaterException extends Exception {
	private final String dateText;
	
	/**
	 * @param msg
	 * @param dateText
	 */
	public DaterException(String msg, String dateText) {
		super(msg);
		this.dateText = dateText;
	}

	/**
	 * @return the dateText
	 */
	public String getDateText() {
		return dateText;
	}
	
	
}
