/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.util.List;

/**
 * @author timo
 *
 */
public class Undateable {

	public static final int INVALID_PRECISION = 0;
	public static final int DAY_PRECISION = 1;
	public static final int MONTH_PRECISION = 2;
	public static final int YEAR_PRECISION = 3;
	
	protected String text = null;
	protected List<UnDateComponent> parts = null;		
	protected final DaterExpression datedBy;
	
	

	/**
	 * @param text
	 * @param parts
	 */
	public Undateable(String text, List<UnDateComponent> parts) {
		this(text, parts, null);
	}

	
	
	/**
	 * @param text
	 * @param parts
	 * @param datedBy
	 */
	public Undateable(String text, List<UnDateComponent> parts, DaterExpression datedBy) {
		super();
		this.text = text;
		this.parts = parts;
		this.datedBy = datedBy;
	}



	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}



	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}



	/**
	 * @return the datedBy
	 */
	public DaterExpression getDatedBy() {
		return datedBy;
	}

	/**
	 * @return
	 */
	protected UnDay getDayPart() {
		for (UnDateComponent part : parts) {
			if (part instanceof UnDay) {
				return (UnDay)part;
			}		
		} 
		return null;
	}
	
	/**
	 * @return
	 */
	protected UnMonth getMonthPart() {
		for (UnDateComponent part : parts) {
			if (part instanceof UnMonth) {
				return (UnMonth)part;
			}		
		} 
		return null;
	}

	/**
	 * @return
	 */
	protected UnYear getYearPart() {
		for (UnDateComponent part : parts) {
			if (part instanceof UnYear) {
				return (UnYear)part;
			}		
		} 
		return null;
	}

	
	/**
	 * Assumes more or less that isValid()
	 * 
	 * @return
	 */
	public int getPrecision() {
		UnMonth m = getMonthPart();
		UnDay d = getDayPart();
		UnYear y = getYearPart();
		if (d != null) {
			return DAY_PRECISION;
		} else if (m != null) {
			return MONTH_PRECISION;
		} else if (y != null) {
			return YEAR_PRECISION;
		}
		return INVALID_PRECISION;
	}
	
	/**
	 * Kind of assumes isValid();
	 * 
	 * @return
	 */
	public String getSQLDateExpression() {
		
//		if (!isValid()) {
//			return "INVALID";
//		}
		
		int prec = getPrecision();
		if (prec == INVALID_PRECISION) {
			return "INVALID";
		}
		
		String year = String.valueOf(getYearPart().getIntVal());
		if (YEAR_PRECISION == prec) {
			return year + "-01-01";
		}
		
		String month = String.valueOf(getMonthPart().getIntVal());
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (MONTH_PRECISION == prec) {
			return year + "-" + month + "-01";
		}
		
		String day = String.valueOf(getDayPart().getIntVal());
		if (day.length() == 1) {
			day = "0" + day;
		}
		return year + "-" + month + "-" + day;
	}
	
	/**
	 * @return
	 */
	public boolean isValid() {
		UnMonth m = getMonthPart();;
		UnDay d = getDayPart();
		UnYear y = getYearPart();
		return (m == null || m.isValid()) 
				&& (d == null || d.isValid())
				&& (y == null || y.isValid())
				&& isValid(y, m, d);
	}
	
	/**
	 * @param y
	 * @param m
	 * @param d
	 * @return
	 */
	protected boolean isValid(UnYear y, UnMonth m, UnDay d) {
		if (y == null && (m != null || d != null)) {
			return false;
		} else if (m == null && d != null) {
			return false;
		}
		if (d != null) {
			return checkDayForMonth(y, m, d);
		} else {
			return true;
		}
	}
	
	/**
	 * @param y
	 * @param m
	 * @param d
	 * @return
	 */
	protected boolean checkDayForMonth(UnYear y, UnMonth m, UnDay d) {
		//No ranges yet!
		//assumes y and m are valid
		
		if (m.getIntVal() == 2) {
			if (isLeapYear(y)) {
				return 1 <= d.getIntVal() && d.getIntVal() <= 29;
			} else {
				return 1 <= d.getIntVal() && d.getIntVal() <= 28;
			}
		} else if (m.getIntVal() == 1 || m.getIntVal() == 3 || m.getIntVal() == 5 || m.getIntVal() == 7 || m.getIntVal() == 8 || m.getIntVal() == 10 || m.getIntVal() == 12) {
			return 1 <= d.getIntVal() && d.getIntVal() <= 31; 
		} else {
			return 1 <= d.getIntVal() && d.getIntVal() <= 30;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "";
		for (UnDateComponent c : parts) {
			if (!"".equals(result)) {
				result += ", ";
			}
			result += c.toString();
		}
		return result;
	}

	/**
	 * @param y
	 * @return
	 */
	protected boolean isLeapYear(UnYear y) {
		 //no ranges yet!
		 return (y.getIntVal() % 4 == 0) && (y.getIntVal() % 100 != 0) || (y.getIntVal() % 400 == 0);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
