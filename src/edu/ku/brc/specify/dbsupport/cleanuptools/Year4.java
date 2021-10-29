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
public class Year4 extends DaterExpression {
	/**
	 * 
	 */
	public Year4() {
		super("[1|2][0-9]{3}");
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.DaterExpression#getDate(java.lang.String)
	 */
	@Override
	public Undateable getDate(String text) throws DaterException {
		List<UnDateComponent> startParts = new ArrayList<UnDateComponent>();
		startParts.add(new UnYear(text, text));
		return new Undateable(text, startParts, this);
	}

}
