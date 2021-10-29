package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.util.ArrayList;
import java.util.List;

public class MonthDayRangeYear4 extends MonthDayYear4Base {
	
	/**
	 * 
	 */
	public MonthDayRangeYear4() {
		super("\\p{Alpha}{3,12} [0-9]{1,2}-[0-9]{1,2}[,| ][ ]*{1}[1|2][0-9]{3}");
	}

	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UFlaPaleoBotUtils.ThreePartDate#getEndParts(java.lang.String)
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
	protected String getEndDayText(String text) {
		String result = text.split(" ")[1];
		if (result.endsWith(",")) {
			result = result.substring(0, result.length()-1);
		} else if (result.contains(",")) {
			result = result.split(",")[0];
		}
		result = result.split("-")[1];
		return result;
	}
}
