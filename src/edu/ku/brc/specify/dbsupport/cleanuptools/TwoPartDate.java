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
public abstract class TwoPartDate extends DaterExpression {
	/**
	 * @param regex
	 */
	public TwoPartDate(String regex) {
		super(regex);
	}
	
	/**
	 * @param text
	 * @return
	 */
	protected abstract String getMonthText(String text);
	/**
	 * @param text
	 * @return
	 */
	protected abstract String getYearText(String text);

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.DaterExpression#getDate(java.lang.String)
	 */
	@Override
	public Undateable getDate(String text) throws DaterException {
		String month = getMonthText(text);
		String year = getYearText(text);
		//System.out.println(month + " - " + year);
		List<UnDateComponent> parts = new ArrayList<UnDateComponent>();
		if (month != null) {
			parts.add(new UnMonth(text, month));
		}
		if (year != null) {
			parts.add(new UnYear(text, year));
		}
		if (parts.size() == 2) {
			return new Undateable(text, parts, this);
		} 
		throw new DaterException("error get date for: " + text, text);
	}
	
}
