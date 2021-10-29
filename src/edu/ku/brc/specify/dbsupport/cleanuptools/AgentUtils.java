/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.csvreader.CsvReader;
import org.hibernate.exception.ConstraintViolationException;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.tools.export.Sampler;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class AgentUtils {
	private class MergeAgent  implements Comparable<MergeAgent> {
		private final Integer id;
		private final Integer newId;
		private final String[] fldVals;
		private final String[] fldHdrs;
		private final Map<String, Object> record = new HashMap<String, Object>();
		/**
		 * @param id
		 * @param newId
		 * @param fldVals
		 */
		public MergeAgent(Integer id, Integer newId, String[] fldValsArg, String[] fldHdrsArg) {
			super();
			this.id = id;
			this.newId = newId;
			this.fldVals = new String[fldValsArg.length - 2];
			this.fldHdrs = new String[fldValsArg.length - 2];
			for (int i = 2; i < fldValsArg.length; i++) {
				fldVals[i-2] = fldValsArg[i];
				fldHdrs[i-2] = fldHdrsArg[i];
			}
			for (int i = 0; i < fldVals.length; i++) {
				record.put(fldHdrs[i].toLowerCase(), fldVals[i]);
			}
		}
		
		/**
		 * @return
		 */
		public Integer getAgentType() throws Exception {
			try {
				return Integer.valueOf(fldVals[fldVals.length-1]);
			} catch (NumberFormatException fex) {
				//String atype = fldVals[fldVals.length-1];
				Object atypeObj = record.get("agenttype");
				if (atypeObj == null) {
					return 1; //person
				} else {
					String atype = atypeObj.toString().toLowerCase();
					if ("organization".equals(atype)) {
						return 0;
					} else if ("person".equals(atype)) {
						return 1;
					} else if ("other".equals(atype)) {
						return 2;
					} else if ("group".equals(atype)) {
						return 3;
					} else {
						throw new Exception("unknown agent type: " + atype);
					}
				}
			}
		}
		/**
		 * @return the id
		 */
		public Integer getId() {
			return id;
		}
		/**
		 * @return the newId
		 */
		public Integer getNewId() {
			return newId;
		}
		/**
		 * @return the fldVals
		 */
		public String[] getFldVals() {
			return fldVals;
		}
		/**
		 * @return the fldHdrs()
		 */
		public String[] getFldHdrs() {
			return fldHdrs;
		}
		/**
		 * @param fld
		 * @return
		 */
		public Object getVal(String fld) {
			return record.get(fld.toLowerCase());
		}
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(MergeAgent o) {
			Integer at1;
			Integer at2;
			try {
				at1 = getAgentType();
			} catch (Exception ex) {
				at1 = 0; 
			}
			try {
				at2 = o.getAgentType();
			} catch (Exception ex) {
				at2 = 0; 
			}
			int result = at1.compareTo(at2);
			if (result == 0) {
				Integer newId = getNewId();
				if (newId == null) {
					newId = Integer.valueOf(-1);
				}
				Integer newId2 = o.getNewId();
				if (newId2 == null) {
					newId2 = Integer.valueOf(-1);
				}
				result = newId.compareTo(newId2);
				if (result == 0) {
					result = getId().compareTo(o.getId());
				}
			}
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			String result = getId() + "->" + getNewId() + " [";
			String[] vals = getFldVals();
			for (int v = 0; v < vals.length; v++) {
				if (v > 0) result += " | ";
				result += vals[v];
			} 
			return result + "]";
		}
		
		
	}

	/**
	 * @param hdr
	 * @return
	 */
	private static boolean checkMergeAgentHdr(String[] hdr) {
		if (!("AgentID".equalsIgnoreCase(hdr[0]) && "NewAgentID".equalsIgnoreCase(hdr[1]))) {
			return false;
		} 
		//could check that other hdrs are all fields in the agent table... 
		return true;
	}

	/**
	 * @param f
	 * @param delimiter
	 * @param encoding
	 * @param qualifier
	 * @param escaper
	 * @return
	 * @throws Exception
	 */
	private static CsvReader getMergeAgentCsvReader(String f, char delimiter, String encoding, char qualifier, char escaper) throws Exception {
        CsvReader csv = new CsvReader(new FileInputStream(f), delimiter, Charset.forName(encoding));
        csv.setTextQualifier(qualifier);
    	if (escaper == '\\') {
    		csv.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
    	} else {
    		csv.setEscapeMode(CsvReader.ESCAPE_MODE_DOUBLED);
    	}
    	if (csv.readHeaders()) {
    		String[] hdr = csv.getHeaders();
    		if (checkMergeAgentHdr(hdr)) {
    			return csv;
    		} else {
    			throw new Exception("invalid headers");
    		}
    	} else {
    		throw new Exception("unable to read headers");
    	}
	}

	/**
	 * @param csv
	 * @return
	 * @throws Exception
	 */
	private static List<MergeAgent> getMergeAgents(CsvReader csv) throws Exception {
		List<MergeAgent> result = new ArrayList<MergeAgent>();
		AgentUtils flau = new AgentUtils();
		while (csv.readRecord()) {
			String[] headers = csv.getHeaders();
			String[] line = csv.getValues();
			Integer newId = null;
			try {
				newId = Integer.valueOf(line[1]);
			} catch (NumberFormatException nex) {
				// leave newId null
			}
			result.add(flau.new MergeAgent(Integer.valueOf(line[0]), newId, line, headers));
		}
		Collections.sort(result);
		return result;
	}

	/**
	 * @param agents
	 * @return
	 */
	private static Map<Integer, Integer> getMergeAgentIdMapping(Connection conn, List<MergeAgent> agents, String[] fldNames) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (MergeAgent agent : agents) {
			Integer id1 = agent.getId();
			addMergeAgentIdMapping(conn, result, id1, fldNames, agent.getFldVals());
			//Integer id2 = agent.getNewId();
			//addMergeAgentIdMapping(conn, result, id2, fldNames, agent.getFldVals());
		}
		return result;
	}

	/**
	 * @param fldName
	 * @param fldVal
	 * @param isText
	 * @return
	 */
	private static String getMergeAgentIdCondition(String fldName, String fldVal, Boolean isText) {
		if (StringUtils.isBlank(fldVal)) {
			return fldName + " IS NULL";
		} else {
			return fldName + " = " + (isText ? "'" + BasicSQLUtils.escapeStringLiterals(fldVal, "'") + "'": fldVal);
		}
	}

	/**
	 * @param conn
	 * @param map
	 * @param idToMap
	 * @param fldNames
	 * @param fldVals
	 */
	private static void addMergeAgentIdMapping(Connection conn, Map<Integer, Integer> map, Integer idToMap, String[] fldNames, String[] fldVals) {
		if (idToMap != null) {
			String where = "";
			for (int f = 0; f < fldVals.length; f++) {
				if (f > 0) {
					where += " AND ";
				}
				where += getMergeAgentIdCondition(fldNames[f+2], fldVals[f], f < fldVals.length-1);
			}
			String sql = "select AgentID from agent where " + where;
			List<Object> newIds = BasicSQLUtils.querySingleCol(conn, sql);
			//Integer newId = newIds.size() == 1 ? (Integer)newIds.get(0) : null;
			Integer newId = newIds.size() >= 1 ? (Integer)newIds.get(0) : null;
			map.put(idToMap, newId);
		}
	}

	/**
	 * @param agents
	 * @return
	 */
	private static Map<Integer, Integer> getIdentityIdMapping(List<MergeAgent> agents) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (MergeAgent agent : agents) {
			result.put(agent.getId(), agent.getId());
			result.put(agent.getNewId(), agent.getNewId());
		}
		return result;
	}

	/**
	 * @param db
	 * @return
	 * @throws Exception
	 */
	private static Pair<List<Pair<String,String>>, List<Pair<String, String>>> getDependentKeysForAgentMerge(String db) throws Exception {
		List<Pair<String,String>> depKeys = Sampler.getDependentKeys(db, "agent", "AgentID");
		
		//Remove [groupPerson, GroupID]
		Pair<String,String> groupKey = null;
		for (int k=0; k < depKeys.size(); k++) {
			if ("groupperson".equals(depKeys.get(k).getFirst()) && "GroupID".equals(depKeys.get(k).getSecond())) {
				groupKey = depKeys.remove(k);
				break;
			}
		}
		
		List<Pair<String,String>> delKeys = new ArrayList<Pair<String,String>>();
		if (groupKey != null) {
			delKeys.add(groupKey);
		}
		
		return new Pair<List<Pair<String,String>>, List<Pair<String, String>>>(depKeys, delKeys);
	}

	/**
	 * @param merge
	 * @param idMap
	 * @param con
	 * @return
	 */
	private static boolean isMergable(MergeAgent merge, Map<Integer, Integer> idMap, Connection con) {
		Integer fromId = idMap.get(merge.getId());
		Integer toId = idMap.get(merge.getNewId());
		boolean result = fromId != null && toId != null && fromId.longValue() != toId.longValue();
		//if (result && merge.getAgentType() == 3) {
		//	result = BasicSQLUtils.getCount(con, "SELECT count(*) from groupperson where GroupID=" + fromId) == 0;
		//}
		if (result) {
			//Can't merge agents associated with users. (Really ought eliminate specifu
			//users before processing rather running this for every agent.)
			result = BasicSQLUtils.getCount(con, "SELECT count(*) FROM agent WHERE AgentID=" 
					+ fromId + " AND SpecifyUserID IS NOT NULL") == 0;
		}
		return result;	
	}

	/**
	 * @param delSql
	 * @param countSql
	 * @param con
	 * @throws Exception
	 */
	private static void deleteAfterMerge(String delSql, String countSql, Connection con) throws Exception {
		int c = BasicSQLUtils.getCountAsInt(con, countSql);	
		if (c != 0) {
			int r =  BasicSQLUtils.update(con, delSql);
			if (r != c) {
				throw new Exception("merge error executing: " + delSql);
			}
		}
	}

	/**
	 * @param con
	 * @param db
	 * @param tbl
	 * @param fromId
	 * @param toId
	 * @param keys
	 * @throws Exception
	 */
	private static void mergeToRecord(Connection con, String db, String tbl, Long fromId, Long toId, 
			Pair<List<Pair<String,String>>, List<Pair<String, String>>> keys, boolean deleteMergedRecord) throws Exception {
		try {
			Sampler.mergeToRecord(con, "mysql", fromId.longValue(), toId.longValue(), db, "agent", keys.getFirst());
			//now do the deletion
		} catch (Exception x) {
			x.printStackTrace();
			throw x;
		}
		List<Pair<String, String>> delKeys = keys.getSecond();
		if (deleteMergedRecord) {
			delKeys.add(new Pair<String, String>("agent", "AgentID"));
		}
		
		for (Pair<String, String> delKey : keys.getSecond()) {
			String delSql = "delete from "  + Sampler.sqlDelimit("mysql", delKey.getFirst()) +  " where " 
					+ Sampler.sqlDelimit("mysql", delKey.getSecond()) + " = " + fromId;
			String countSql = "select count(*) from " + Sampler.sqlDelimit("mysql", delKey.getFirst()) +  " where " 
					+ Sampler.sqlDelimit("mysql", delKey.getSecond()) + " = " + fromId;
			deleteAfterMerge(delSql, countSql, con);
		}
	}

	/**
	 * @param con
	 * @param schemaCon
	 * @param db
	 * @param table
	 * @param inc
	 * @throws Exception
	 */
	private static void incrementIDs(Connection con, Connection schemaCon, String db, String table, long inc) throws Exception {
		Sampler.schemaCon = schemaCon;
		List<Object[]> agents = BasicSQLUtils.query(con, "select " + table + "id from " + table + " order by " + table + "id desc");
		List<Pair<String,String>> depKeys = Sampler.getDependentKeys(db, table, table + "ID");
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("SET foreign_key_checks = 0");
			stmt.close();
			for (Object[] agent : agents) {
				long id = ((Integer)agent[0]).longValue();
				Sampler.changeKey(con, "mysql", db, table, table + "ID", id, id + inc, 
					depKeys, false);
			}
		} finally {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("SET foreign_key_checks = 1");
			stmt.close();
		}
	}
	
	
	/**
	 * @param a
	 * @param fld
	 * @return
	 */
	protected static String getSqlVal(MergeAgent a, String fld) {
		if (a.getVal(fld) == null) {
			return "NULL";
		} else {
			String result = a.getVal(fld).toString();
			if (!"agenttype".equalsIgnoreCase(fld)) {
				result = "'" + result.toString().replaceAll("'", "''") + "'";
			}
			return result;
		}
	}

	/**
	 * @param a
	 * @param id
	 * @throws Exception
	 */
	protected static boolean updateIfNecessary(Connection con, MergeAgent a, Integer id) throws Exception {
		String sql = "SELECT 1 from agent WHERE AgentID=" + id + " AND NOT(";
		String[] flds = a.getFldHdrs();
		boolean result = false;
		for (int f=0; f < flds.length; f++) {
			if (f > 0) {
				sql += " AND ";
			}
			sql += flds[f] + " = " + getSqlVal(a, flds[f]);
		}
		sql += ")";
		if (BasicSQLUtils.query(con, sql).size() == 1) {
			sql = "UPDATE agent SET TimestampModified=now() "; //ModifiedByAgentID????
			for (int f=0; f < flds.length; f++) {
				sql += ", " + flds[f] + " = " + getSqlVal(a, flds[f]);
			}
			sql += " WHERE AgentID=" + id;
			if (BasicSQLUtils.update(con, sql) != 1) {
				throw new Exception("Error updating: " + sql);
			}
			result = true;
		}
		return result;
	}

	/**
	 * @param merges
	 * @throws Exception
	 */
	static protected void saveToDbForMergerVerification(Connection con, List<Pair<Integer,Integer>> merges, String mergedTblName, String mergeId) throws Exception {
		//create table flatgeo (continent varchar(50), country varchar(50), state varchar(50), county varchar(50), idflatgeo int not null auto_increment, primary key(idflatgeo));
		String createSql = "create table mergers(tblname varchar(120), timestampcreated timestamp, fromid int not null, toid int not null, mergeid varchar(128), idmergers int not null auto_increment, primary key(idmergers))";
		Statement stmt = con.createStatement();
		try {
			stmt.executeUpdate(createSql);
		} catch (Exception ex) {
			//continue, assuming ex is due to pre-existing mergers table, if not we will find out soon enough
		}
		for (Pair<Integer, Integer> m : merges) {
			String sql = "insert into mergers(tblname, timestampcreated, fromid, toid, mergeid) values('" + mergedTblName + "', now(), "
					+ m.getFirst() + ", " + m.getSecond() + ",'" + mergeId + "')";
			stmt.executeUpdate(sql);
		}
		stmt.close();
	}

	/**
	 * @param con
	 * @param origDbName
	 * @param mergeName
	 * @param keys
	 * @return
	 * @throws Exception
	 */
	static protected boolean verifyMergers(Connection con, String origDbName, String mergeName, List<Pair<String,String>> keys, String mergeId) throws Exception {
		boolean result = true;
		List<Object[]> merges = BasicSQLUtils.query(con, "select m.fromid, m.toid, m2.toid, m3.toid, m4.toid, m5.toid from mergers m "
				+ " left join mergers m2 on m2.fromid = m.toid "
				+ " left join mergers m3 on m3.fromid = m2.toid "
				+ " left join mergers m4 on m4.fromid = m3.toid "
				+ " left join mergers m5 on m5.fromid = m4.toid "
				+ " where m.tblname='" + mergeName + "' and m.mergeid='" + mergeId + "'");
		for (Object[] merge : merges) {
			Integer fromId = (Integer)merge[0];
			int i = 5;
			while (merge[i] == null) {
				i--;
			}
			Integer toId = (Integer)merge[i];
			if (!verifyMerger(con, fromId, toId, origDbName, keys)) {
				result = false;
				//break;
			}
		}
		return result;
	}
	
	/**
	 * @param con
	 * @param oldValue
	 * @param newValue
	 * @param origDbName
	 * @param keys
	 * @return
	 * @throws Exception
	 */
	static protected boolean verifyMerger(Connection con, Integer oldValue, Integer newValue, 
			String origDbName, List<Pair<String,String>> keys) throws Exception {
		boolean result = true;
		Statement stmt = con.createStatement();
		for (Pair<String, String> key : keys) {
			String tblName = key.getFirst();
			if (!tblName.endsWith("attr")) {
				String keyFld = tblName + "ID"; //specify convention
				if ("dnasequencerunattachment".equals(tblName)) {
					keyFld = "DnaSequencingRunAttachmentId";
				} else if ("sptasksemaphore".equals(tblName)) {
					keyFld = "TaskSemaphoreID";
				}
				String fldName = key.getSecond();
				String sql = "select * from " + origDbName + "." + tblName + " t1 inner join "
					+ tblName + " t2 on t2." + keyFld + "=" + "t1." + keyFld + " where t1." + fldName 
					+ "=" + oldValue + " and t2." + fldName + " != " + newValue;
				ResultSet rs = stmt.executeQuery(sql);
				if (rs.next()) {
					result = false;
				}
				rs.close();
				if (!result) {
					break;
				}
			}
		} 
		stmt.close();
		return result;
	}

	/**
	 * @param con
	 * @param schemaCon
	 * @param db
	 * @param fromId
	 * @param toId
	 */
	private static void mergeAgent(Connection con, Connection schemaCon, String db, Integer fromId, Integer toId) {
		try {
			try {	
				Statement stmt = con.createStatement();
				stmt.executeUpdate("SET foreign_key_checks = 0");
				stmt.close();
				
				Sampler.schemaCon = schemaCon;
				Pair<List<Pair<String,String>>, List<Pair<String, String>>> keys = getDependentKeysForAgentMerge(db);
				mergeToRecord(con, db, "agent", fromId.longValue(), toId.longValue(), keys, true);
			
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Statement stmt = con.createStatement();
				stmt.executeUpdate("SET foreign_key_checks = 1");
				stmt.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static void changeAgentId(Connection con, Connection schemaCon, String db, Long fromId, Long toId) {
		try {
			try {	
				Statement stmt = con.createStatement();
				stmt.executeUpdate("SET foreign_key_checks = 0");
				stmt.close();
				
				Sampler.schemaCon = schemaCon;
				Pair<List<Pair<String,String>>, List<Pair<String, String>>> keys = getDependentKeysForAgentMerge(db);
				Sampler.changeKey(con, "mysql", db, "agent", "AgentID", fromId.longValue(), toId.longValue(), keys.getFirst(), false);

			
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Statement stmt = con.createStatement();
				stmt.executeUpdate("SET foreign_key_checks = 1");
				stmt.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void changeId(Connection con, Connection schemaCon, String db, String tbl, Long fromId, Long toId) {
		try {
			try {	
				Statement stmt = con.createStatement();
				stmt.executeUpdate("SET foreign_key_checks = 0");
				stmt.close();
				
				Sampler.schemaCon = schemaCon;
				List<Pair<String,String>> keys = Sampler.getDependentKeys(db, tbl, tbl + "id");
				Sampler.changeKey(con, "mysql", db, tbl, tbl + "ID", fromId.longValue(), toId.longValue(), keys, false);

			
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Statement stmt = con.createStatement();
				stmt.executeUpdate("SET foreign_key_checks = 1");
				stmt.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void removeNegativeAgentIds(Connection con, Connection schemaCon, String db, String origDbName, Long initialID) {
		try {
			try {	
				//assumes nobody else is accessing the db
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("select agentid from agent where agentid <= 0");
				List<Pair<Integer, Integer>> moves = new ArrayList<Pair<Integer,Integer>>();
				List<Integer> tos = new ArrayList<Integer>();
				while (rs.next()) {
					moves.add(new Pair<Integer,Integer>(rs.getInt(1), initialID.intValue()));
					changeAgentId(con, schemaCon, db, rs.getLong(1), initialID++);
				}
				String mergeId = java.util.UUID.randomUUID().toString();
				saveToDbForMergerVerification(con, moves, "agent", mergeId);
   				if (verifyMergers(con, origDbName, "agent", getDependentKeysForAgentMerge(db).getFirst(), mergeId)) {
   					System.out.println("verified! MergeId: " + mergeId);
   					
   				} else {
   					System.out.println("NOT verified. Abandon hope. MergeId: " + mergeId);
   				}
   				System.out.println("mergers table was added to db and probably should be deleted.");
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Statement stmt = con.createStatement();
				stmt.executeUpdate("SET foreign_key_checks = 1");
				stmt.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void removeNegativeIds(Connection con, Connection schemaCon, String tbl, String db, String origDbName, Long initialID) {
		try {
			try {	
				//assumes nobody else is accessing the db
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("select " + tbl + "id from " + tbl + " where " + tbl + "id <= 0");
				List<Pair<Integer, Integer>> moves = new ArrayList<Pair<Integer,Integer>>();
				List<Integer> tos = new ArrayList<Integer>();
				while (rs.next()) {
					moves.add(new Pair<Integer,Integer>(rs.getInt(1), initialID.intValue()));
					if ("agent".equals(tbl)) {
						changeAgentId(con, schemaCon, db, rs.getLong(1), initialID++);
					} else {
						changeId(con, schemaCon, db, tbl, rs.getLong(1), initialID++);
					}
				}
				String mergeId = java.util.UUID.randomUUID().toString();
				saveToDbForMergerVerification(con, moves, tbl, mergeId);
				List<Pair<String,String>> keys = "agent".equals(tbl) ?  getDependentKeysForAgentMerge(db).getFirst()
								:  Sampler.getDependentKeys(db, tbl, tbl + "id");
   				if (verifyMergers(con, origDbName, tbl, keys, mergeId)) {
   					System.out.println("verified! + MergeID: " + mergeId);
   					
   				} else {
   					System.out.println("NOT verified. Abandon hope. MergeID: " + mergeId);
   				}
   				System.out.println("mergers table was added to db and probably should be deleted.");
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Statement stmt = con.createStatement();
				stmt.executeUpdate("SET foreign_key_checks = 1");
				stmt.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param f
	 * @param delimiter
	 * @param encoding
	 * @param qualifier
	 * @param escaper
	 * @throws Exception
	 */
	private static void mergeAgents(Connection con, Connection schemaCon, String db, String f, char delimiter, String encoding, 
			char qualifier, char escaper, String fOut, boolean doRemap, boolean deleteMergedRecords,
			String origDbName) throws Exception {
        CsvReader csv = getMergeAgentCsvReader(f, delimiter, encoding, qualifier, escaper);
    	if (csv != null) {
    		try {
    			Statement stmt = con.createStatement();
   				stmt.executeUpdate("SET foreign_key_checks = 0");
   				stmt.close();
   				
   				Sampler.schemaCon = schemaCon;
   				List<MergeAgent> mergers = getMergeAgents(csv); //The MergeAgent class is actually unnecessary!
   				Map<Integer, Integer> idMap = doRemap ? getMergeAgentIdMapping(con, mergers, csv.getHeaders()) //Except that the merge file was produced from an old version of the fish db!
   						: getIdentityIdMapping(mergers);
   				csv.close();
   				//Vector<Pair<String, String>> depKeys = Sampler.getDependentKeys(db, "agent", "AgentID");
   				Pair<List<Pair<String,String>>, List<Pair<String, String>>> keys = getDependentKeysForAgentMerge(db);
   				List<String> unMerged = new ArrayList<String>();
   				int merged = 0, updated = 0;
   				List<Pair<Integer,Integer>> merges = new ArrayList<Pair<Integer,Integer>>();

   				for (MergeAgent merge : mergers) {
					Integer fromId = idMap.get(merge.getId());
					Integer toId = merge.getNewId() != null ? idMap.get(merge.getNewId()) : null;
   					if (toId != null && toId != fromId) {
   						if (isMergable(merge, idMap, con)) {
   							//System.out.println("merging " + merge.getId() + "[" + fromId + "]" + " to " + merge.getNewId() + "[" + toId + "]");
   							//Sampler.mergeToRecord(con, "mysql", merge.getId().longValue(), merge.getNewId().longValue(), db, "agent", depKeys);
   							try {
   								//Sampler.mergeToRecord(con, "mysql", fromId.longValue(), toId.longValue(), db, "agent", depKeys);
   								mergeToRecord(con, db, "agent", fromId.longValue(), toId.longValue(),  
   										keys, deleteMergedRecords);
   								merged++;
   			   					merges.add(new Pair<Integer, Integer>(fromId, toId));
   							} catch(ConstraintViolationException ex) {
   								System.out.println(ex.getLocalizedMessage() + " [" +  fromId + "=>" + toId + "] " + merge);
   								unMerged.add(ex.getLocalizedMessage() + " [" +  fromId + "=>" + toId + "] " + merge.toString());
   							}
   						} else {
   							if (fromId == null || toId == null || fromId.longValue() != toId.longValue()) {
   								System.out.println("Ids not found or Group with members:" +  merge);
   								unMerged.add("[" + idMap.get(merge.getId()) + "=>" + idMap.get(merge.getNewId()) + "] " + merge.toString());
   							}
   						}
   					} else {
   						//check if record needs to be updated with new values....
   						if (toId == null || toId == fromId) {
   	   						//System.out.println("Skipping update check for " + merge.getId());
   	   						if (updateIfNecessary(con, merge, fromId)) {
   	   							updated++;
   	   						}
   						} else {
   							//System.out.println("No new id for " + merge.getId());
   						}
	   					//merged++;
   					}
   				}
   				System.out.println("merged: " + merged + ". not merged: " + unMerged.size() + ". updated: " + updated);
   				FileUtils.writeLines(new File(fOut), encoding, unMerged);
				String mergeId = java.util.UUID.randomUUID().toString();
				saveToDbForMergerVerification(con, merges, "agent", mergeId);

   				if (verifyMergers(con, origDbName, "agent", keys.getFirst(), mergeId)) {
   					System.out.println("verified! MergeId: " + mergeId);
   					
   				} else {
   					System.out.println("NOT verified. Abandon hope. MergeId: " + mergeId);
   				}
   				System.out.println("mergers table was added to db and probably should be deleted.");
    		} finally {
    			Statement stmt = con.createStatement();
   				stmt.executeUpdate("SET foreign_key_checks = 1");
   				stmt.close();
    		}
    	}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			boolean doRemap = false;
//			String connStr = "jdbc:mysql://bimysql.nhm.ku.edu/KU_Fish_Tissue?characterEncoding=UTF-8&autoReconnect=true";
//			Connection con = DriverManager.getConnection(connStr, "specifymaster", "2Kick@$$");
//			Connection schemaCon = DriverManager.getConnection("jdbc:mysql://bimysql.nhm.ku.edu/information_schema?characterEncoding=UTF-8&autoReconnect=true", 
//					"specify", "5(9Q[t$.h2K3");
			String connStr = "jdbc:mysql://localhost/uwfcsp?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = DriverManager.getConnection(connStr, "root", "root");
			Connection schemaCon = DriverManager.getConnection("jdbc:mysql://localhost/information_schema?characterEncoding=UTF-8&autoReconnect=true", 
					"root", "root");
			//mergeAgents(con, schemaCon, "KU_Fish_Tissue", "/home/timo/datas/ku/kufish/AgentMerger2.csv",
			//		',', "utf8", '"', '!', "/home/timo/datas/ku/kufish/AgentMerger2Output.txt", doRemap, true, null);
			//mergeAgents(con, schemaCon, "cumvb", "/home/timo/datas/cu/CUMV/birds/agetns to merge_updated.csv",
			//		',', "utf8", '"', '!', "/home/timo/datas/cu/CUMV/birds/AgentMergerOutput.txt", doRemap, true, "cumvb_a");
			mergeAgents(con, schemaCon, "uwfcsp", "/home/timo/datas/washington/agentmerge_edited_final.csv",
					',', "utf8", '"', '!', "/home/timo/datas/washington/agent_merge_out.txt", doRemap, true, "uwfcsppre");
			//mergeAgents(con, schemaCon, "landbirdy", "/home/timo/datas/cleveland/birds/brennan.csv",
			//		',', "utf8", '"', '!', "/home/timo/datas/cleveland/birds/brennan_out.txt", doRemap, true, "landbirdypre");

			//mergeAgent(con, schemaCon, "cumvb", 0, 21301183);
			//changeAgentId(con, schemaCon, "saiab", -2146684337, 2145049107);
			
			//removeNegativeAgentIds(con, schemaCon, "saiab", "saiab_pre", 2145049168L);
			//removeNegativeIds(con, schemaCon, "address", "saiab", "saiab_pre",  2143838163L);
			
			//removeNegativeIds(con, schemaCon, "shipment", "saiab", "saiab_pre",  2125090428L);
			
			//removeNegativeIds(con, schemaCon, "gift", "saiab", "saiab_pre",  2143587738L);
			//removeNegativeIds(con, schemaCon, "giftagent", "saiab", "saiab_pre",  2143361078L);
			//removeNegativeIds(con, schemaCon, "giftpreparation", "saiab", "saiab_pre",  2139717356L);
			
			
			//mergeAgent(con, schemaCon, "sdsom", 156, 179 );
			//mergeAgent(con, schemaCon, "sdsom", 155, 220 );
					
			//incrementIDs(con, schemaCon, "pkp", "agent", 9436);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
