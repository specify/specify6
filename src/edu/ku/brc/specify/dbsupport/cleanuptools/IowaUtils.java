/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author timo
 *
 */
public class IowaUtils extends UtilitaryBase {

	/**
	 * @param con
	 */
	public IowaUtils(Connection con) {
		super(con);
	}

	/**
	 * @throws Exception
	 */
	private void generateNewCatNums() throws Exception {
		String sql = "select idoriginalcats from originalcats order by originalcotypeid, collectionobjectid";
		buildStatement();
		ResultSet rows = this.stmt.executeQuery(sql);
		int newcat = 1;
		Statement upstmt = this.con.createStatement();
		while (rows.next()) {
			sql = "update originalcats set newcatnum=" + newcat + " where idoriginalcats=" + rows.getInt(1);
			upstmt.execute(sql);
			newcat++;
		}
		sql = "update originalcats_subs ocs inner join collectionobject co on co.collectionobjectid = ocs.collectionobjectid inner join originalcats oc on oc.collectionobjectid = "
				+ "co.derivedfromid set ocs.newcatnum = oc.newcatnum";
		upstmt.execute(sql);
		rows.close();
		stmt.close();
		upstmt.close();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/iowa5?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			IowaUtils wa = new IowaUtils(con);
			wa.generateNewCatNums();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
