/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author timo
 *
 */
public abstract class UnDateComponent {
	protected final String containingText;
	protected final String text;
	protected Integer intVal = null;
	//protected UnDateComponent end = null;
	//protected boolean range = false;
	
	
	/**
	 * @param containingText
	 * @param text
	 */
	public UnDateComponent(String containingText, String text) {
		super();
		this.containingText = containingText;
		this.text = text;
		try {
			intVal = Integer.valueOf(text);
		} catch (NumberFormatException ex) {
			//worth a shot
		}
	}
	
	public boolean isValid() {
		//XXX can't really come up with a validator for UnDay w/o knowing the associated. OR need another validator for Undateable.
		return intVal != null;// && (!range || end != null);
	}
	
	public abstract String getName();
	
	/**
	 * @return the containingText
	 */
	public String getContainingText() {
		return containingText;
	}
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * @return the start
	 */
	public Integer getIntVal() {
		return intVal;
	}
	
	/**
	 * @return the end
	 */
//	public UnDateComponent getEnd() {
//		return end;
//	}
	
	/**
	 * @return the range
	 */
//	public boolean isRange() {
//		return range;
//	}
	
	/**
	 * @param year
	 * @return
	 */
	public static boolean isLeapYear(int year) {
		boolean result = false;
		if (year % 4 == 0) {
			result = true;
			if (year % 100 == 0) {
				if (year % 400 != 0) {
					result = false;
				}
			}
		}
		return result;
	}
	/**
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static boolean isValidSpDate(UnYear year, UnMonth month, UnDay day) {
		boolean result = /*!(year.isRange() || month.isRange() || day.isRange())
				&& */!((year.getIntVal() == null && year.getText() != null)
						|| (month.getIntVal() == null && month.getText() != null)
						|| (day.getIntVal() == null && day.getText() != null));
		if (result) {
			
			String tst = (year.getIntVal() == null ? "0" : "1")
					+ (month.getIntVal() == null ? "0" : "1")
					+ (day.getIntVal() == null ? "0" : "1");
			if (!"000".equals(tst)) {
				result = !tst.contains("01");
				if (result && !"100".equals(tst)) {
					int m = month.getIntVal();
					result = 1 <= m && m <= 12;
					if (result && !"110".equals(tst)) {
						int maxD = 30;
						if (m == 1 || m == 3 || m == 5 || m == 7 || m == 8 || m == 10 || m == 12) {
							maxD = 31;
						} else if (m == 2) {
							if (isLeapYear(year.getIntVal())) {
								maxD = 29;
							} else {
								maxD = 28;
							}
						}
						result = 1 <= day.getIntVal() && day.getIntVal() <= maxD;
					}
				}
			}
					}
		return result;
	}
	
	/**
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static Integer getSpDatePrecision(UnYear year, UnMonth month, UnDay day) {
		Integer result = null;
		if (isValidSpDate(year, month, day)) {
			if (day.getIntVal() != null) {
				result = 1;
			} else if (month.getIntVal() != null) {
				result = 2;
			} else if (year.getIntVal() != null) {
				result = 3;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = intVal != null ? intVal.toString() : "";
//		if (range) {
//			result += " - " + end;
//		}
		return getName() + ":" + result;
	}
	
	
}
