package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public class MonthNum2Year4 extends TwoPartDate {
	
	/**
	 * 
	 */
	public MonthNum2Year4() {
		super("[0-9]{1,2}([ /\\.-])[1|2][0-9]{3}");
	}
	
	/**
	 * @param text
	 * @return
	 */
	@Override
	protected String getMonthText(String text) {
		if (text.length() == 6) {
			return text.substring(0, 1);
		} else {
			return text.substring(0, 2);
		}
	}
	
	/**
	 * @param text
	 * @return
	 */
	@Override
	protected String getYearText(String text) {
		return text.substring(text.length()-4);
	}
}
