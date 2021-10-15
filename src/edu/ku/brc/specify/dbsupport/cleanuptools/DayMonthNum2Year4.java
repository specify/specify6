/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public class DayMonthNum2Year4 extends DayMonthNum2Year {

	/**
	 * 
	 */
	public DayMonthNum2Year4() {
		super("[0-9]{1,2}([ /\\.-])[0-9]{1,2}([ /\\.-])[1|2][0-9]{3}");
	}

	protected DayMonthNum2Year4(String regex) {
		super(regex);
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UFlaPaleoBotUtils.ThreePartDay#getYearText(java.lang.String)
	 */
	@Override
	protected String getYearText(String text) {
		return text.substring(text.length()-4);
	}

	
}
