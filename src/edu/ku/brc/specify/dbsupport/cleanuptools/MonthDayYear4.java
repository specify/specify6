package edu.ku.brc.specify.dbsupport.cleanuptools;


/**
 * @author timo
 *
 */
public class MonthDayYear4 extends MonthDayYear4Base {
	/**
	 * 
	 */
	public MonthDayYear4() {
		super("\\p{Alpha}{3,12} [0-9]{1,2}[,| ][ ]*{1}[1|2][0-9]{3}");
	}				
}

