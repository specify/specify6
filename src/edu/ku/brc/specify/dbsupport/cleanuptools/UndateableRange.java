/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.util.List;

/**
 * @author timo
 *
 */
public class UndateableRange extends Undateable {
	protected final Undateable end;
	
	/**
	 * @param text
	 * @param startParts
	 * @param endParts
	 */
	public UndateableRange(String text, List<UnDateComponent> startParts, List<UnDateComponent> endParts) {
		this(text, startParts, endParts, null);
	}
	
	public UndateableRange(String text, List<UnDateComponent> startParts, List<UnDateComponent> endParts, DaterExpression datedBy) {
		super(text, startParts, datedBy);
		this.end = new Undateable(text, endParts, datedBy);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " - " + end.toString();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.Undateable#isValid()
	 */
	@Override
	public boolean isValid() {
		return super.isValid() && end.isValid() && isRangeValid();
	}

	/**
	 * @return
	 */
	protected boolean isRangeValid() {
		//assumes super.isValid() and end.isValid().
		return getSQLDateExpression().compareTo(end.getSQLDateExpression()) < 0; 
		
	}

	/**
	 * @return the end
	 */
	public Undateable getEnd() {
		return end;
	}

}
