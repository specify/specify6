package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public class MonthNum2DayYear2 extends MonthNum2DayYear4 {
	
	/**
	 * 
	 */
	public MonthNum2DayYear2() {
		super("[0-9]{1,2}([ /\\.-])[0-9]{1,2}([ /\\.-])[0-9]{2}");
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UFlaPaleoBotUtils.DayMonthNum2Year4#getYearText(java.lang.String)
	 */
	@Override
	protected String getYearText(String text) {
		String result = text.substring(text.length()-2);
		Integer y = Integer.parseInt(result);
		if (y <= 14) {
			result = "20" + result;
		} else {
			result = "19" + result;
		}
		return result;
	}
}
