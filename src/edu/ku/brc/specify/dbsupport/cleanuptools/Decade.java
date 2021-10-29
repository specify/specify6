/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public class Decade extends Year4Range {

	/**
	 * 
	 */
	public Decade() {
		super("[1|2][0-9]{2}0'?s");
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.Year4Range#getYears(java.lang.String)
	 */
	@Override
	protected String[] getYears(String text) {
		String[] result = new String[2];
		result[0] = text.substring(0, 4);
		result[1] = String.valueOf(Integer.valueOf(result[0]) + 9);
		return result;
	}
	
	
}
