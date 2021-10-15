package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public abstract class MonthDayYear4Base extends ThreePartDate {
	/**
	 * @param regex
	 */
	protected MonthDayYear4Base(String regex) {
		super(regex);
	}
	
	/**
	 * @param text
	 * @return
	 */
	@Override
	protected String getYearText(String text) {
		return text.substring(text.length()-4);
	}
	
	/**
	 * @param text
	 * @return
	 */
	@Override
	protected String getMonthText(String text) {
		return text.split(" ")[0];
	}	
	
	/**
	 * @param text
	 * @return
	 */
	@Override
	protected String getDayText(String text) {
		String result = text.split(" ")[1];
		if (result.endsWith(",")) {
			result = result.substring(0, result.length()-1);
		} else if (result.contains(",")) {
			result = result.split(",")[0];
		}
		if (result.contains("-")) {
			result = result.split("-")[0];
		}
		return result;
	}
}
