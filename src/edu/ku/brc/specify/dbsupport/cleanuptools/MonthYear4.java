package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public class MonthYear4 extends TwoPartDate {
	public MonthYear4() {
		super("\\p{Alpha}{3,12}[,| ][ ]*{1}[1|2][0-9]{3}");
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
		String result = text.split(" ")[0];
		if (result.endsWith(",")) {
			result = result.substring(0, result.length()-1);
		}
		return result;
	}
}
