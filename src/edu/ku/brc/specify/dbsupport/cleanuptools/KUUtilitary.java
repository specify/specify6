/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;

/**
 * @author timo
 *
 */
public abstract class KUUtilitary extends UtilitaryBase {

	/**
	 * @param con
	 */
	public KUUtilitary(Connection con) {
		super(con);
	}
	
}
