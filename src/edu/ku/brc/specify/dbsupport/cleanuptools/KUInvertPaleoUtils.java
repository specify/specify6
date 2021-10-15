/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class KUInvertPaleoUtils extends KUUtilitary {

	protected Map<Integer, Pair<Integer, String>> ceToPcMap = new HashMap<Integer, Pair<Integer, String>>(); //assuming ces are unique in the updates table
	protected Map<Integer, List<String>> pcToStratMap = new HashMap<Integer, List<String>>();
	protected List<Integer> donePcs = new ArrayList<Integer>();
	protected Map<String, Integer> stratToPcMap = new HashMap<String, Integer>();
	
	/**
	 * @param con
	 */
	public KUInvertPaleoUtils(Connection con) {
		super(con);
	}

	/**
	 * @param connStr
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection(String connStr)	throws SQLException {
		return  DriverManager.getConnection(connStr, "Master", "Master");
	}
	

	/**
	 * @throws Exception
	 */
	protected void applyCeFixes() throws Exception {
		buildStatement();
		String sql = "select ce.collectingeventid, ce.paleocontextid, (select geologictimeperiodid from geologictimeperiod gtp where gtp.fullname = ss.chrono limit 1) newChronoID, "
				+ "(select lithostratid from lithostrat litho where litho.fullname = ss.litho limit 1) newLithoID "
				+ "from collectingevent ce inner join kuinvp05.celocnumpal ss on ss.locid = ce.stationfieldnumber "
				+ "left join paleocontext pc on pc.paleocontextid = ce.paleocontextid " 
				+ "left join geologictimeperiod ch on ch.geologictimeperiodid = pc.chronosstratid "
				+ "left join lithostrat ls on ls.lithostratid = pc.lithostratid "
				+ "where " 
				+ "(ifnull(trim(ch.fullname), '') != ifnull(trim(ss.chrono), '') or ifnull(trim(ls.fullname), '') != ifnull(trim(ss.litho), '')) " 
				+ "and !((select geologictimeperiodid from geologictimeperiod gtp where gtp.fullname = ss.chrono limit 1) is null "
				+ "or (select lithostratid from lithostrat litho where litho.fullname = ss.litho limit 1) is null) "
				+ "order by 1 ";  
		try {
			ResultSet rs = stmt.executeQuery(sql);
			try {
				buildMaps(rs);
			} finally {
				rs.close();
			}
			for (Map.Entry<Integer, Pair<Integer, String>> ce : ceToPcMap.entrySet()) {
				processCe(ce.getKey(), ce.getValue().getFirst(), ce.getValue().getSecond());
			}
		} finally {
			stmt.close();
		}
	}
	
	/**
	 * @param ceId
	 * @param pcId
	 * @param chronoId
	 * @param lithoId
	 * @throws Exception
	 */
	protected void processCe(Integer ceId, Integer pcId, String stratStr) throws Exception {
		if (donePcs.indexOf(pcId) == -1) {
			Pair<Integer, Integer> strats = fromStratStr(stratStr);
			Integer chronoId = strats.getFirst();
			Integer lithoId = strats.getSecond();
//			List<String> pcStrats = pcToStratMap.get(pcId);
//			if (pcStrats.size() == 1) {
//				//just update the existing pc
//				String sql = "UPDATE paleocontext set ChronosStratID=" + chronoId + ", LithoStratID=" + lithoId + " WHERE PaleoContextID=" + pcId;
//				if (1 != BasicSQLUtils.update(con, sql)) {
//					throw new Exception("error updating: " + sql);
//				}
//				logThisSql(sql);
//				donePcs.add(pcId);
//			} else {
				Integer newPc = stratToPcMap.get(stratStr);
				if (newPc == null) {
					newPc = createNewPc(chronoId, lithoId);
					//stratToPcMap.put(stratStr, newPc);
				}
				if (newPc != null && newPc != pcId) {
					String sql = "UPDATE collectingevent set PaleoContextID=" + newPc + " WHERE CollectingEVentID=" + ceId;
					if (1 != BasicSQLUtils.update(con, sql)) {
						throw new Exception("error updating: " + sql);
					}
					logThisSql(sql);
				}
			//}
		}
	}
	
	/**
	 * @param ChronoID
	 * @param lithoId
	 * @return
	 */
	protected Integer createNewPc(Integer chronoId, Integer lithoId) throws Exception {
		logThis("createNewPc(" + chronoId + ", " + lithoId + ")");
		String sql = "INSERT INTO paleocontext(TimestampCreated, Version, DisciplineID, CreatedByAgentID, ChronosStratID, LithoStratID) "
				+ "VALUES(now(), 0, 3, 1," + (chronoId == null ? "NULL" : chronoId) + ", " + (lithoId == null ? "NULL" : lithoId)+ ")";
		stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		logThisSql(sql);
		ResultSet key = stmt.getGeneratedKeys();
		try {
			if (!key.next()) {
				throw new Exception("Insert failed: " + sql);
			}
			Integer result = key.getInt(1);
			return result;
		} finally {
			key.close();
		}
	}
	
	/**
	 * @param ceId
	 * @param chronoId
	 * @param lithoId
	 */
	protected void buildMaps(ResultSet rs) throws Exception {
		ceToPcMap.clear();
		pcToStratMap.clear();
		donePcs.clear();
		while (rs.next()) {
			ceToPcMap.put(rs.getInt(1), new Pair<Integer, String>(rs.getInt(2), toStratStr(rs.getInt(3), rs.getInt(4))));
			addToPcStratMap(rs.getInt(2), rs.getInt(3), rs.getInt(4));
		}
	}
	
	/**
	 * @param pcId
	 * @param chronoId
	 * @param lithoId
	 */
	protected void addToPcStratMap(Integer pcId, Integer chronoId, Integer lithoId) {
		String stratStr = toStratStr(chronoId, lithoId);
		List<String> val = pcToStratMap.get(pcId);
		if (val == null) {
			List<String> newVal = new ArrayList<String>();
			newVal.add(stratStr);
			pcToStratMap.put(pcId, newVal);
		} else {
			if (val.indexOf(stratStr) == -1) {
				val.add(stratStr);
			}
		}
	}
	
	/**
	 * @param chronoId
	 * @param lithoId
	 * @return
	 */
	protected String toStratStr(Integer chronoId, Integer lithoId) {
		return chronoId + "_" + lithoId;
	}
	
	/**
	 * @param str
	 * @return
	 */
	protected Pair<Integer, Integer> fromStratStr(String str) {
		String[] strats = str.split("_");
		return new Pair<Integer, Integer>(Integer.valueOf(strats[0]), Integer.valueOf(strats[1]));
	}
	
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/invp05?characterEncoding=UTF-8&autoReconnect=true";
			KUInvertPaleoUtils pu = new KUInvertPaleoUtils(getConnection(connStr));
			pu.applyCeFixes();
			pu.writeLog("D:/data/KU/invp/model/cefix2.txt");
			pu.writeSqlLog("D:/data/KU/invp/model/cefixSql2.sql");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
