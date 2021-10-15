/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public class Piece {
	final String text;
	final boolean delimiter;
	/**
	 * @param text
	 * @param delimiter
	 */
	public Piece(String text, boolean delimiter) {
		super();
		this.text = text;
		this.delimiter = delimiter;
	}
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @return the delimiter
	 */
	public boolean isDelimiter() {
		return delimiter;
	}
	
	
}
