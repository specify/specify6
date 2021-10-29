/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.util.ArrayList;
import java.util.List;

/**
 * @author timo
 *
 */
public class MonthNum2DayRangeYear4 extends MonthNum2DayYear4 {
	
	/**
	 * 
	 */
	public MonthNum2DayRangeYear4() {
		super("[0-9]{1,2}([ /\\.])[0-9]{1,2}[-|,][0-9]{1,2}([ /\\.])[1|2][0-9]{3}");
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.ThreePartDate#getEndParts(java.lang.String)
	 */
	@Override
	protected List<UnDateComponent> getEndParts(String text) {
		String month = getMonthText(text);
		String year = getYearText(text);
		String day = getEndDayText(text);
		List<UnDateComponent> endparts = new ArrayList<UnDateComponent>();
		if (month != null) {
			endparts.add(new UnMonth(text, month));
		}
		if (year != null) {
			endparts.add(new UnYear(text, year));
		}
		if (day != null) {
			endparts.add(new UnDay(text, day));
		}
		return endparts;
	}
	
	
	/**
	 * @param text
	 * @return
	 */
	protected String[] splitDate(String text) {
		String splitter = text.indexOf("/") == -1 ? "." : "/";
		return text.split(splitter);
	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.DayMonthNum2Year#getD(java.lang.String)
	 */
	@Override
	protected String getD(String text) {
		return splitDate(text)[1];
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.MonthNum2DayYear4#getMonthText(java.lang.String)
	 */
	@Override
	protected String getMonthText(String text) {
		return splitDate(text)[0];
	}

	/**
	 * @param text
	 * @return
	 */
	protected String[] splitDays(String text) {
		String result = getD(text);
		String splitter = result.indexOf(",") == -1 ? "-" : ",";
		return result.split(splitter);
		
	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.MonthNum2DayYear4#getDayText(java.lang.String)
	 */
	@Override
	protected String getDayText(String text) {
		return splitDays(text)[0];
	}

	/**
	 * @param text
	 * @return
	 */
	protected String getEndDayText(String text) {
		return splitDays(text)[1];
	}
}
