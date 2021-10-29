/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.util.List;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author timo
 *
 */
public class KUUMam extends KUUtilitary {

	/**
	 * @param con
	 */
	public KUUMam(Connection con) {
		super(con);
	}

	
	/**
	 * @param box
	 * @throws Exception
	 */
	private void addVialsToTowerBox(Object[] box) throws Exception {
		String parentId = box[0].toString();
		String sql = "insert into storage(Version,TimestampCreated,CreatedByAgentID,StorageTreeDefID,StorageTreeDefItemID,RankID,ParentID,Name) values(0,now(), 1, 1, 20, 500, "
				+ parentId + ", 'Vial %s')";
		for (int vialno = 1; vialno < 82; vialno++) {
			String vialstr = String.valueOf(vialno);
			if (vialstr.length() == 1) vialstr = "0" + vialstr;
			if (1 != BasicSQLUtils.update(con,String.format(sql, vialstr))) {
				throw new Exception("insert failed for: " + String.format(sql, vialstr));
			}
		}
	}
	
	/**
	 * @throws Exception
	 */
	private void addVialsToTowerBoxes() throws Exception {
		List<Object[]> boxes = BasicSQLUtils.query(con, "select storageid from storage where name like 'Box __' and parentid between 25 and 30");
		for (Object[] box : boxes) {
			addVialsToTowerBox(box);
		}
	}
	
	/**
	 * @throws Exception
	 */
	private void addColumnRowsToEtOHWing() throws Exception {
		Integer etohID = BasicSQLUtils.getCount(con, "select storageid from storage where name = 'EtOH Wing' and rankid = 200");
		String[] columns = {"B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		String sql = "insert into storage(Version,TimestampCreated,CreatedByAgentID,StorageTreeDefID,StorageTreeDefItemID,RankID,ParentID,Name) values(0,now(), 1, 1, 14, 325, "
				+ etohID + ", 'Column %s')";
		for (String column : columns) {
			if (1 != BasicSQLUtils.update(con, String.format(sql, column))) {
				throw new Exception("insert failed for: " + String.format(sql, column));
			}
		}
	}
	
	/**
	 * @throws Exception
	 */
	private void addShelvesToEtOHColumnRows() throws Exception {
		Integer etohID = BasicSQLUtils.getCount(con, "select storageid from storage where name = 'EtOH Wing' and rankid = 200");
		List<Object> columns = BasicSQLUtils.querySingleCol(con, "select storageid from storage where parentid = " + etohID + " and name like 'Column _' and rankid = 325");
		for (Object column : columns) {
			addShelvesToEtOHColumnRow(column);
		}
	}
	
	/**
	 * @param column
	 * @throws Exception
	 */
	private void addShelvesToEtOHColumnRow(Object column) throws Exception {
		String sql = "insert into storage(Version,TimestampCreated,CreatedByAgentID,StorageTreeDefID,StorageTreeDefItemID,RankID,ParentID,Name) values(0,now(), 1, 1, 18, 362, "
				+ column + ", 'Shelf %d')";
		for (int s = 1; s < 10; s++) {
			if (1 != BasicSQLUtils.update(con, String.format(sql, s))) {
				throw new Exception("insert failed for: " + String.format(sql, s));
			}
		}
	}
	
	/**
	 * @throws Exception
	 */
	private void addCasesTo709Wood() throws Exception {
		Integer woodID = BasicSQLUtils.getCount(con, "select s.storageid from storage s inner join storage p on p.storageid = s.parentid "
				+ "where s.name = 'Wood' and p.name = 'Room 709' and s.rankid = 325 and p.rankid = 200");
		String sql = "insert into storage(Version,TimestampCreated,CreatedByAgentID,StorageTreeDefID,StorageTreeDefItemID,RankID,ParentID,Name) values(0,now(), 1, 1, 15, 337, "
				+ woodID + ", 'Case %s')";
		for (int c = 2; c < 68; c++) {
			String str = String.valueOf(c);
			if (str.length() == 1) str = "0" + str;
			if (1 != BasicSQLUtils.update(con, String.format(sql, str))) {
				throw new Exception("insert failed for: " + String.format(sql, str));
			}
		}
	}
	
	/**
	 * @throws Exception
	 */
	private void addWoodTo150() throws Exception {
		Integer l50ID = BasicSQLUtils.getCount(con, "select s.storageid from storage s "
				+ "where s.name = 'Room 150' and s.rankid = 200");
		String sql = "insert into storage(Version,TimestampCreated,CreatedByAgentID,StorageTreeDefID,StorageTreeDefItemID,RankID,ParentID,Name) values(0,now(), 1, 1, 14, 325, "
				+ l50ID + ", 'Wood Floor %s')";
		for (int c = 2; c < 31; c++) {
			String str = String.valueOf(c);
			if (str.length() == 1) str = "0" + str;
			if (1 != BasicSQLUtils.update(con, String.format(sql, str))) {
				throw new Exception("insert failed for: " + String.format(sql, str));
			}
		}
	}
	
	/**
	 * @throws Exception
	 */
	private void addCasesToWoodAndSteelAndDelta() throws Exception {
		Integer l50ID = BasicSQLUtils.getCount(con, "select s.storageid from storage s "
				+ "where s.name = 'Room 150' and s.rankid = 200");
		addCasesToWoodOrSteel(l50ID, "Wood");
		addCasesToWoodOrSteel(l50ID, "Steel");
		addCasesToDelta(l50ID);
	}
	
	/**
	 * @param l50
	 * @param wos
	 * @throws Exception
	 */
	private void addCasesToWoodOrSteel(Integer l50, String wos) throws Exception{
		String[] pros = {"B","C","D","E","F"};
		String[] epis = {"L","U"};
		List<Object> wosIDs = BasicSQLUtils.querySingleCol(con, "select s.storageid from storage s where s.parentid = " + l50 
				+ " and s.name like '" + wos + " Row __' and s.rankid = 325");
		String sql = "insert into storage(Version,TimestampCreated,CreatedByAgentID,StorageTreeDefID,StorageTreeDefItemID,RankID,ParentID,Name) values(0,now(), 1, 1, 15, 337, %s, 'Case %s')";		
		for (Object wosID : wosIDs) {
			for (String epi : epis) {
				for (String pro : pros) {
					if (1 != BasicSQLUtils.update(con, String.format(sql, wosID.toString(), pro+epi))) {
						throw new Exception("insert failed for: " + String.format(sql, wosID.toString(), pro+epi));
					}
				}
			}
		}
	}
	
	/**
	 * @param l50
	 * @throws Exception
	 */
	private void addCasesToDelta(Integer l50) throws Exception{
		String[] pros = {"A","B","C","D","E","F","G","H"};
		String[] epis = {"L","R"};
		List<Object> deltaIDs = BasicSQLUtils.querySingleCol(con, "select s.storageid from storage s where s.parentid = " + l50 
				+ " and s.name like 'Delta Row __' and s.rankid = 325");
		String sql = "insert into storage(Version,TimestampCreated,CreatedByAgentID,StorageTreeDefID,StorageTreeDefItemID,RankID,ParentID,Name) values(0,now(), 1, 1, 15, 337, %s, 'Case %s')";		
		for (Object deltaID : deltaIDs) {
			for (String epi : epis) {
				for (String pro : pros) {
					if (1 != BasicSQLUtils.update(con, String.format(sql, deltaID.toString(), pro+epi))) {
						throw new Exception("insert failed for: " + String.format(sql, deltaID.toString(), pro+epi));
					}
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/kum?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			KUUMam kum = new KUUMam(con);
			kum.addVialsToTowerBoxes();		
			kum.addColumnRowsToEtOHWing();
			kum.addShelvesToEtOHColumnRows();
			kum.addCasesTo709Wood();
			kum.addWoodTo150();
			kum.addCasesToWoodAndSteelAndDelta();
			//Now set all isaccepted to true and fullname to name, and rebuild the tree in sp6.
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
