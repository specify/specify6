package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public class MonthNum2DayYear4 extends DayMonthNum2Year4 {

	public MonthNum2DayYear4() {
		super();
	}
	
	/**
	 * @param regex
	 */
	protected MonthNum2DayYear4(String regex) {
		super(regex);
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UFlaPaleoBotUtils.DayNum2MonthNum2YearNum4#getDayText(java.lang.String)
	 */
	@Override
	protected String getDayText(String text) {
		return super.getMonthText(text);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UFlaPaleoBotUtils.DayNum2MonthNum2YearNum4#getMonthText(java.lang.String)
	 */
	@Override
	protected String getMonthText(String text) {
		return super.getDayText(text);
	}
	
}
