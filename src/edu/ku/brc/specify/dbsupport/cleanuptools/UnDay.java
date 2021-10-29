/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public class UnDay extends UnDateComponent {

	/**
	 * @param containingText
	 * @param text
	 */
	public UnDay(String containingText, String text) {
		super(containingText, text);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UnDateComponent#getName()
	 */
	@Override
	public String getName() {
		return "day";
	}

}
