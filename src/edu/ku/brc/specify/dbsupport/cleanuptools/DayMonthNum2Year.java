/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public abstract class DayMonthNum2Year extends ThreePartDate {
	
	public DayMonthNum2Year(String regex) {
		super(regex);
	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UFlaPaleoBotUtils.ThreePartDay#getDayText(java.lang.String)
	 */
	@Override
	protected String getDayText(String text) {
		return getD(text);
	}

	/**
	 * @param text
	 * @return
	 */
	protected String getD(String text) {
		String result = text.substring(0,2);
		try {
			Integer.parseInt(result);
		} catch(NumberFormatException nex) {
			result = text.substring(0, 1);
		}
		return result;
	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UFlaPaleoBotUtils.ThreePartDay#getMonthText(java.lang.String)
	 */
	@Override
	protected String getMonthText(String text) {
		int startIdx = getD(text).length() + 1;
		String result = text.substring(startIdx, startIdx + 2);
		try {
			Integer.parseInt(result);
		} catch(NumberFormatException nex) {
			result = text.substring(startIdx, startIdx + 1);
		}
		return result;
	}
}
