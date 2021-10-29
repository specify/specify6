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
public abstract class ThreePartDate extends TwoPartDate {
	/**
	 * @param regex
	 */
	public ThreePartDate(String regex) {
		super(regex);
	}

	/**
	 * @param text
	 * @return
	 */
	protected abstract String getDayText(String text);
	
	/**
	 * @param text
	 * @return
	 */
	protected List<UnDateComponent> getStartParts(String text) {
		String month = getMonthText(text);
		String year = getYearText(text);
		String day = getDayText(text);
		//System.out.println(month + " - " + year);
		List<UnDateComponent> parts = new ArrayList<UnDateComponent>();
		if (month != null) {
			parts.add(new UnMonth(text, month));
		}
		if (year != null) {
			parts.add(new UnYear(text, year));
		}
		if (day != null) {
			parts.add(new UnDay(text, day));
		}
		return parts;
	}
	
	/**
	 * @param text
	 * @return
	 */
	protected List<UnDateComponent> getEndParts(String text) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.DaterExpression#getDate(java.lang.String)
	 */
	public Undateable getDate(String text) throws DaterException {
		List<UnDateComponent> startparts = getStartParts(text);
		List<UnDateComponent> endparts = getEndParts(text);
		if (startparts.size() == 3) {
			if (endparts == null) {
				return new Undateable(text, startparts, this);
			} else if (endparts.size() == 3) {
				return new UndateableRange(text, startparts, endparts, this);
			}
		} 
		throw new DaterException("error get date for: " + text, text);
	}
	
	
}
