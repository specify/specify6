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
public class Year4Range extends DaterExpression {
	
	/**
	 * 
	 */
	public Year4Range() {
		super("[1|2][0-9]{3}-[1|2][0-9]{3}");
	}

	/**
	 * @param regex
	 */
	protected Year4Range(String regex) {
		super(regex);
	}
	
	/**
	 * @param text
	 * @return
	 */
	protected String[] getYears(String text) {
		return text.split("-");
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.DaterExpression#getDate(java.lang.String)
	 */
	@Override
	public Undateable getDate(String text) throws DaterException {
		String[] years = getYears(text);
		List<UnDateComponent> startParts = new ArrayList<UnDateComponent>();
		List<UnDateComponent> endParts = new ArrayList<UnDateComponent>();
		startParts.add(new UnYear(years[0], years[0]));
		endParts.add(new UnYear(years[1], years[1]));
		return new UndateableRange(text, startParts, endParts, this);
	}
	
	
}
