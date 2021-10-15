/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public class UnMonth extends UnDateComponent {
	
	/**
	 * @param containingText
	 * @param text
	 */
	public UnMonth(String containingText, String text) {
		super(containingText, text);
		init();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UnDateComponent#isValid()
	 */
	public boolean isValid() {
		return super.isValid() && 1 <= intVal && intVal <= 12
				/*&& (!range || end.isValid())*/;
	}
	
	/**
	 * 
	 */
	protected void init() {
		if (getIntVal() == null) {
			//if (!isRange()) {
				if ("january".equalsIgnoreCase(getText())) {
					intVal = 1;
				} else if ("jan".equalsIgnoreCase(getText())) {
					intVal = 1;
				} else if ("february".equalsIgnoreCase(getText())) {
					intVal = 2;
				} else if ("feb".equalsIgnoreCase(getText())) {
					intVal = 2;
				} else if ("march".equalsIgnoreCase(getText())) {
					intVal = 3;
				} else if ("mar".equalsIgnoreCase(getText())) {
					intVal = 3;
				} else if ("april".equalsIgnoreCase(getText())) {
					intVal = 4;
				} else if ("apr".equalsIgnoreCase(getText())) {
					intVal = 4;
				} else if ("may".equalsIgnoreCase(getText())) {
					intVal = 5;
				} else if ("june".equalsIgnoreCase(getText())) {
					intVal = 6;
				} else if ("jun".equalsIgnoreCase(getText())) {
					intVal = 6;
				} else if ("july".equalsIgnoreCase(getText())) {
					intVal = 7;
				} else if ("jul".equalsIgnoreCase(getText())) {
					intVal = 7;
				} else if ("august".equalsIgnoreCase(getText())) {
					intVal = 8;
				} else if ("aug".equalsIgnoreCase(getText())) {
					intVal = 8;
				} else if ("september".equalsIgnoreCase(getText())) {
					intVal = 9;
				} else if ("sep".equalsIgnoreCase(getText())) {
					intVal = 9;
				} else if ("october".equalsIgnoreCase(getText())) {
					intVal = 10;
				} else if ("oct".equalsIgnoreCase(getText())) {
					intVal = 10;
				} else if ("november".equalsIgnoreCase(getText())) {
					intVal = 11;
				} else if ("nov".equalsIgnoreCase(getText())) {
					intVal = 11;
				} else if ("december".equalsIgnoreCase(getText())) {
					intVal = 12;
				} else if ("dec".equalsIgnoreCase(getText())) {
					intVal = 12;
				} 
			//}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.dbsupport.cleanuptools.UnDateComponent#getName()
	 */
	@Override
	public String getName() {
		return "month";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
