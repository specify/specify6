/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public class UnYear extends UnDateComponent {

	/**
	 * @param containingText
	 * @param text
	 */
	public UnYear(String containingText, String text) {
		super(containingText, text);
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UnDateComponent#getName()
	 */
	@Override
	public String getName() {
		return "year";
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UnDateComponent#isValid()
	 */
	public boolean isValid() {
		return super.isValid() 
				/*&& (!range || (end.isValid() && intVal < end.getIntVal()))*/;
	}

}
