/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author timo
 *
 */
public class PaleoUtils extends UtilitaryBase {
    private static final Logger log  = Logger.getLogger(PaleoUtils.class);
    
	protected String db = null;
	protected String masterUser = null;
	protected String masterPw = null;
	protected Integer userAgentId = null;
	protected Statement insStmt;
	protected boolean useLocGuids = true;
	protected boolean useCeGuids = true;
	
	protected Map<String, Integer> recMap = new HashMap<String, Integer>();
	
	/**
	 * @param con
	 */
	public PaleoUtils(Connection con) {
		super(con);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param con
	 * @param db
	 * @param masterUser
	 * @param masterPw
	 * @param userAgentId
	 */
	public PaleoUtils(Connection con, String db, String masterUser, String masterPw,
			Integer userAgentId) {
		super(con);
		this.db = db;
		this.masterUser = masterUser;
		this.masterPw = masterPw;
		this.userAgentId = userAgentId;
	}
	
	/**
	 * @throws Exception
	 */
	protected void removePaleoContextDuplicates(Integer disciplineID) throws Exception {
		LocalityDuplicateRemover.removeDuplicatePaleoContexts(db, masterUser, masterPw, disciplineID);
	}
	
	/**
	 * @param coID
	 * @return
	 * @throws Exception
	 */
	protected Integer getDisciplineIDForCo(Integer coID) throws Exception {
		Integer result = null;
		if (coID != null) {
			String sql = "SELECT DisciplineID FROM collection c INNER JOIN collectionobject co "
					+ " ON co.CollectionMemberID = c.CollectionID WHERE co.CollectionObjectID=" + coID;
			result = BasicSQLUtils.getCount(con, sql);
		} else {
			log.warn("returning null disciplineid for null collectionobject id");
		}
		return result;
	}
	
	/**
	 * @param collectionID
	 * @return
	 */
	protected boolean isEmbeddedCE(Integer collectionID) {
		boolean result = false;
		if (collectionID != null) {
			String sql = "SELECT count(*) FROM " + db + ".collection WHERE CollectionID=" + collectionID
					+ " AND IsEmbeddedCollectingEvent";
			result = BasicSQLUtils.getCountAsInt(con, sql) == 1;
		} else {
			log.warn("returning false for null collection id");
		}
		return result;
	}
	
	/**
	 * copies paleocontextids from collectionobject to locality.
	 * creates new localities, and updates/creates related ces where necessary.
	 * 
	 * if pcs are to be shared, duplicate removal should already have been done.
	 * 
	 * @param collectionID
	 * @throws Exception
	 */
	protected void copyPaleoContextIdsFromCoToLoc(Integer collectionID) throws Exception {
		if (isEmbeddedCE(collectionID)) {
			copyPCIdsFromCoToLocEmbeddedCE(collectionID);
		} else {
			copyPCIdsFromCoToLocSharedCE(collectionID);
		}
	}
	
	/**
	 * @param disciplineID
	 * @throws Exception
	 */
	protected void unSharePaleoContextsForTbl(Integer disciplineID, String tbl) throws Exception {
		boolean closeStmt = false;
		if (stmt == null) {
			closeStmt = true;
			buildStatement();
		}
		try {
			String sql = "SELECT DISTINCT PaleoContextID FROM " + tbl + " WHERE PaleoContextID IS NOT NULL "
				+ "AND DisciplineID=" + disciplineID;
			List<Object> pc = BasicSQLUtils.querySingleCol(con, sql);
			for (Object pcObj : pc) {
				Integer pcId = Integer.class.cast(pcObj);
				sql = "SELECT " + tbl + "ID FROM " + tbl + " WHERE PaleoContextID=" + pcId;
				List<Object> ce = BasicSQLUtils.querySingleCol(con, sql);
				for (int i = 1; i < ce.size(); i++) {
					Integer newPcId = LocalityDuplicateRemover.duplicate(con, pcId, "PaleoContext");
					sql = "UPDATE " + tbl + " SET PaleoContextID=" + newPcId + " WHERE "
							+ tbl +"ID=" + Integer.class.cast(ce.get(i));
					if (1 != stmt.executeUpdate(sql)) {
						throw new Exception("error updating: " + sql);
					}
				}
			}
		} finally {
			if (closeStmt) {
				stmt.close();
				stmt = null;
			}
		}
		
	}
	
	/**
	 * @param disciplineID
	 */
	protected void unshareCEs(Integer disciplineID) throws Exception {
		boolean closeStmt = false;
		if (stmt == null) {
			closeStmt = true;
			buildStatement();
		}
		try {
			List<Object[]> ces = BasicSQLUtils.query(con, "select collectingeventid, count(collectionobjectid) from collectionobject where collectingeventid is not null group by 1");
			for (Object[] ce : ces) {
				Integer ceId = (Integer)ce[0];
				Long cnt = (Long)ce[1];
				if (cnt > 1) {
					List<Object> cos = BasicSQLUtils.querySingleCol(con, "select collectionobjectid from collectionobject where collectingeventid=" + ceId);
					boolean first = true;
					for (Object coId : cos) {
						if (!first) {
							Integer newCeId = LocalityDuplicateRemover.duplicate(con, ceId, "CollectingEvent");
							String sql = "UPDATE collectionobject SET CollectingEventID=" + newCeId + " WHERE CollectionObjectID=" + coId;
							if (1 != stmt.executeUpdate(sql)) {
								throw new Exception("error updating: " + sql);
							}
						} else {
							first = false;
						}
					}
				}
			}
		} finally {
			if (closeStmt) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	/**
	 * @param collectionID
	 * @throws Exception
	 */
	protected void copyPaleoContextIdsFromCoToCe(Integer collectionID) throws Exception {
		if (isEmbeddedCE(collectionID)) {
			copyPCIdsFromCoToCeEmbeddedCE();
		} else {
			copyPCIdsFromCoToCeSharedCE();
		}
	}
	
	/**
	 * copies paleocontextids from collectionobject to collectingevent.
	 * creates new ces where necessary.
	 * 
	 * if pcs are to be shared, duplicate removal should already have been done.
	 * 
	 * @param collectionID
	 * @throws Exception
	 */
	protected void copyPCIdsFromCoToCeEmbeddedCE() throws Exception {
		throw new Exception("copyPCIdsFromCoToCeEmbeddedCE is not implemented");
	}
	
	/**
	 * @param collectionID
	 * @throws Exception
	 */
	protected void copyPCIdsFromCoToCeSharedCE() throws Exception {
		recMap.clear();
		String sql = "select CollectionObjectID, PaleoContextID from collectionobject co "
				+ "where PaleoContextID is not null order by CollectionObjectID";
		buildStatement();
		insStmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		try {
			while (rs.next()) {
				movePcToCe(false, rs.getInt(1), rs.getInt(2));
				int cnt = 0; /*BasicSQLUtils.getCountAsInt(con, "select count(*)	from " 
						+ "collectionobject co left join collectingevent ce on co.collectingeventid = ce.CollectingEventID "
						+ "left join paleocontext pc on ce.PaleoContextID = pc.PaleoContextID "
						+ "inner join utapin.collectionobject coin on coin.CollectionObjectID = co.CollectionObjectID "
						+ "left join utapin.paleocontext pcin on pcin.PaleoContextID = coin.PaleoContextID "
						+ "where (trim(ifnull(pc.remarks,'')) !=  trim(ifnull(pcin.remarks,'')) or "
						+ "trim(ifnull(pc.text1,'')) !=  trim(ifnull(pcin.text1,'')) or "
						+ "trim(ifnull(pc.text2,'')) !=  trim(ifnull(pcin.text2,'')) or "
						+ "trim(ifnull(pc.LithoStratID,'')) !=  trim(ifnull(pcin.LithoStratID,'')) or "
						+ "trim(ifnull(pc.BioStratID,'')) !=  trim(ifnull(pcin.BioStratID,'')) or "
						+ "trim(ifnull(pc.ChronosStratID,'')) !=  trim(ifnull(pcin.ChronosStratID,'')) or "
						+ "trim(ifnull(pc.ChronosStratEndID,'')) !=  trim(ifnull(pcin.ChronosStratEndID,''))) and co.CollectionObjectID <= " + rs.getInt(1));*/
				if (cnt > 0) {
					System.out.println("Broken! coid=" + rs.getInt(1) + ", pcid=" + rs.getInt(2));
				}
			}
		} finally {
			rs.close();
			stmt.close();
			stmt = null;
			insStmt.close();
		}
	}
	
	/**
	 * @param collectionID
	 * @throws Exception
	 */
	protected void copyPCIdsFromCoToLocEmbeddedCE(Integer collectionID) throws Exception {
		recMap.clear();
		String sql = "select CollectionObjectID, PaleoContextID from collectionobject co "
				+ "where PaleoContextID is not null";
		buildStatement();
		insStmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		try {
			while (rs.next()) {
				movePcToLoc(true, rs.getInt(1), rs.getInt(2));
			}
		} finally {
			rs.close();
			stmt.close();
			insStmt.close();
		}
	}

	/**
	 * @param locId
	 * @param pcId
	 * @param isEmbeddedCE
	 * @param currentPaleoChildTable
	 * @return
	 * @throws Exception
	 */
	protected boolean canAssignPcToLoc(Integer locId, Integer pcId, boolean isEmbeddedCE, String currentPaleoChildTable) throws Exception {
		boolean result = true;
		if (isEmbeddedCE) {
			if ("collectionobject".equalsIgnoreCase(currentPaleoChildTable)) {
				String sql = "select count(*) from collectionobject co inner join collectingevent ce on ce.collectingeventid = co.collectingeventid"
						+ " where ce.LocalityID=" + locId + " and ";
				if (pcId == null) {
					sql += "co.PaleoContextID is not null";
				} else {
					sql += "(co.PaleoContextID != " + pcId + " or co.PaleoContextID is null)";
				}
				result = 0 == BasicSQLUtils.getCountAsInt(con, sql);
			} else {
				throw new Exception("canAssignPcToLoc: Unsupported Child Table: " + currentPaleoChildTable);
			}
		} else {
			throw new Exception("canAssignPcToLoc: shared ce support not implemented.");
		}
		return result;
	}

	/**
	 * @param ceId
	 * @param pcId
	 * @param isEmbeddedCE
	 * @param currentPaleoChildTable
	 * @return
	 * @throws Exception
	 */
	protected boolean canAssignPcToCe(Integer ceId, Integer pcId, boolean isEmbeddedCE, String currentPaleoChildTable) throws Exception {
		boolean result = true;
		if (!isEmbeddedCE) {
			if ("collectionobject".equalsIgnoreCase(currentPaleoChildTable)) {
				String sql = "select count(*) from collectionobject co"
						+ " where co.CollectingEventID=" + ceId + " and ";
				if (pcId == null) {
					sql += "co.PaleoContextID is not null";
				} else {
					sql += "(co.PaleoContextID != " + pcId + " or co.PaleoContextID is null)";
				}
				result = 0 == BasicSQLUtils.getCountAsInt(con, sql);
			} else {
				throw new Exception("canAssignPcToCe: Unsupported Child Table: " + currentPaleoChildTable);
			}
		} else {
			throw new Exception("canAssignPcToCe: embedded ce support not implemented.");
		}
		return result;
	}

	/**
	 * @param recId
	 * @param pcId
	 * @param tablename
	 * @throws Exception
	 */
	protected void assignPc(Integer recId, Integer pcId, String tablename) throws Exception {
		String sql = "update " + tablename + " set PaleoContextID = " + (pcId == null ? "NULL" : pcId) 
				+ " where " + tablename + "ID=" + recId;
		logThisSql(sql);
		insStmt.executeUpdate(sql);
	}
	/**
	 * @param locId
	 * @param pcId
	 * @throws Exception
	 */
	protected void assignPcToLoc(Integer locId, Integer pcId) throws Exception {
		assignPc(locId, pcId, "locality");
	}

	/**
	 * @param ceId
	 * @param pcId
	 * @throws Exception
	 */
	protected void assignPcToCe(Integer ceId, Integer pcId) throws Exception {
		assignPc(ceId, pcId, "collectingevent");
	}

	
	
	/**
	 * @param isEmbeddedCE
	 * @param coID
	 * @param pcID
	 * @throws exception
	 */
	protected void movePcToLoc(boolean isEmbeddedCE, Integer coId, Integer pcId) throws Exception {
		logThis("movePcToLoc(" + coId + ", " + pcId + ")");
		Integer locId = getLocIDForCO(coId);
		Integer newLocId = null;
		if (locId == null) {
			if (pcId != null) {
				newLocId = getBlankLocID(getDisciplineIDForCo(coId), pcId);
			}
		} else {
			newLocId = lookupInRecMap(locId, pcId);
			if (newLocId == null) {
				if (canAssignPcToLoc(locId, pcId, isEmbeddedCE, "collectionobject")) {
					assignPcToLoc(locId, pcId);
					newLocId = locId;
				} else {
					newLocId = createNewLocForPc(locId, pcId);
				}
				putInRecMap(locId, pcId, newLocId);
			}
		}
		
		if ((locId != null && newLocId == null)
				|| (locId == null && newLocId != null)
				|| (locId != null && newLocId != null && !locId.equals(newLocId))) {
			if (isEmbeddedCE) {
				String sql = "update collectionobject co inner join collectingevent ce on ce.CollectingEventID = "
						+ "co.CollectingEventID set ce.LocalityID=" + newLocId + " where co.CollectionObjectID="
						+ coId;
				logThisSql(sql);
				if (1 != insStmt.executeUpdate(sql)) {
					throw new Exception("error updating: " + sql);
				}
			} else {
				throw new Exception("movePcToLoc: shared ce support not implemented!");
			}
		}
	}
	
	/**
	 * @param isEmbeddedCE
	 * @param coId
	 * @param pcId
	 * @throws Exception
	 */
	protected void movePcToCe(boolean isEmbeddedCE, Integer coId, Integer pcId) throws Exception {
		//logThis("movePcToCe(" + coId + ", " + pcId + ")");
		Integer ceId = getCeIDForCO(coId);
		Integer newCeId = null;
		if (ceId == null) {
			if (pcId != null) {
				newCeId = getBlankCeID(getDisciplineIDForCo(coId), pcId);
			}
		} else {
			newCeId = lookupInRecMap(ceId, pcId);
			if (newCeId == null) {
				if (canAssignPcToCe(ceId, pcId, isEmbeddedCE, "collectionobject")) {
					assignPcToCe(ceId, pcId);
					newCeId = ceId;
				} else {
					newCeId = createNewCeForPc(ceId, pcId);
				}
				putInRecMap(ceId, pcId, newCeId);
			}
		}
		
		if ((ceId != null && newCeId == null)
				|| (ceId == null && newCeId != null)
				|| (ceId != null && newCeId != null && !ceId.equals(newCeId))) {
			if (!isEmbeddedCE) {
				String sql = "update collectionobject co set CollectingEventID=" + newCeId + " where co.CollectionObjectID="
						+ coId;
				logThisSql(sql);
				if (1 != insStmt.executeUpdate(sql)) {
					throw new Exception("error updating: " + sql);
				}
			} else {
				throw new Exception("movePcToCe: embedded ce support not implemented!");
			}
		}
	}
	
	/**
	 * @param origId
	 * @param pcId
	 * @param tblShortClassName
	 * @return
	 * @throws Exception
	 */
	protected Integer createNewRecForPc(Integer origId, Integer pcId, String tblShortClassName, boolean createGuid) throws Exception {
		logThis("createNewRecForPc(" + origId + ", " + pcId + ", " + tblShortClassName + ", " + createGuid + ")");
		Integer result = LocalityDuplicateRemover.duplicate(con, origId, tblShortClassName);
		String sql = "UPDATE " + tblShortClassName.toLowerCase() + " SET PaleoContextID=" 
				+ (pcId != null ? pcId : "NULL");
		if (createGuid) {
			sql += ", guid=uuid()";
		}
		sql += " WHERE " + tblShortClassName + "ID=" + result;
		logThisSql(sql);
		if (1 != BasicSQLUtils.update(con, sql)) {
			throw new Exception("update error: "  + sql);
		}
		return result;
	}
	
	/**
	 * @param origLocId
	 * @param pcId
	 * @return
	 * @throws Exception
	 */
	protected Integer createNewLocForPc(Integer origLocId, Integer pcId) throws Exception {
		return createNewRecForPc(origLocId, pcId, "Locality", useLocGuids);
	}
	
	/**
	 * @param origCeId
	 * @param pcId
	 * @return
	 * @throws Exception
	 */
	protected Integer createNewCeForPc(Integer origCeId, Integer pcId) throws Exception {
		return createNewRecForPc(origCeId, pcId, "CollectingEvent", useCeGuids);
	}
	
	/**
	 * @param locId
	 * @param pcId
	 * @return
	 */
	protected String getLookupKey(Integer locId, Integer pcId) {
		return (locId == null ? "NULL" : locId) + "_" + (pcId == null ? "NULL" : pcId);
	}
	
	/**
	 * @param locId
	 * @param pcId
	 * @return
	 */
	protected Integer lookupInRecMap(Integer recId, Integer pcId) {
		return recMap.get(getLookupKey(recId, pcId));
	}
	
	/**
	 * @param locId
	 * @param pcId
	 * @param newLocId
	 * @return
	 */
	protected Integer putInRecMap(Integer recId, Integer pcId, Integer newRecId) {
		return recMap.put(getLookupKey(recId, pcId), newRecId);
	}
	
	/**
	 * @param discId
	 * @param paleoContextID
	 * @return
	 * @throws Exception
	 */
	protected Integer retrieveBlankLocID(Integer discId, Integer pcId) throws Exception {
		String sql = "SELECT l.LocalityID FROM locality l LEFT JOIN localitydetail ld ON ld.LocalityID = l.LocalityID "
				+ "LEFT JOIN geocoorddetail gd on gd.LocalityID = l.LocalityID LEFT JOIN localityattachment la "
				+ "ON la.LocalityID = l.LocalityID WHERE ld.LocalityDetailID IS NULL "
				+ "AND gd.GeoCoordDetailID IS NULL AND la.LocalityAttachmentID IS NULL "
				+ "AND l.GeographyID IS NULL AND LocalityName='N/A' AND "
				+ (pcId == null ? "PaleoContextID IS NULL" : "PaleoContextID=" + pcId);
		return BasicSQLUtils.getCount(con, sql);
	}

	/**
	 * @param discId
	 * @param paleoContextID
	 * @return
	 * @throws Exception
	 */
	protected Integer retrieveBlankCeID(Integer discId, Integer pcId) throws Exception {
		String sql = "SELECT ce.CollectingEventID FROM collectingevent ce LEFT JOIN collector c ON c.CollectingEventID = ce.CollectingEventID "
				+ "LEFT JOIN collectingeventattachment cea on cea.CollectingEventID = ce.CollectingEventID WHERE cea.CollectingEventAttachmentID IS NULL "
				+ "AND c.CollectorID IS NULL AND ce.CollectingEventAttributeID IS NULL AND StationFieldNumber IS NULL AND "
				+ (pcId == null ? "PaleoContextID IS NULL" : "PaleoContextID=" + pcId);
		return BasicSQLUtils.getCount(con, sql);
	}

	/**
	 * @param discId
	 * @param pcId
	 * @return
	 * @throws Exception
	 */
	protected Integer getBlankLocID(Integer discId, Integer pcId) throws Exception {
		Integer result = retrieveBlankLocID(discId, pcId);
		if (result == null) {
			result = createBlankLoc(discId, pcId);
		}
		return result;
	}
	
	/**
	 * @param discId
	 * @param pcId
	 * @return
	 * @throws Exception
	 */
	protected Integer getBlankCeID(Integer discId, Integer pcId) throws Exception {
		Integer result = retrieveBlankCeID(discId, pcId);
		if (result == null) {
			result = createBlankCe(discId, pcId);
		}
		return result;
	}

	/**
	 * @param discId
	 * @param pcId
	 * @return
	 */
	protected Integer createBlankLoc(Integer discId, Integer pcId) throws Exception {
		logThis("createBlankLoc(" + discId + ", " + pcId + ")");
		String sql = "INSERT INTO locality(TimestampCreated, TimestampModified, Version, SrcLatLongUnit, "
				+ "CreatedByAgentID, DisciplineID, PaleoContextID, LocalityName) VALUES(now(), now(), 0, 3, " 
				+ userAgentId + ", " + discId + ", " + pcId + ", 'N/A')";
		insStmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		logThisSql(sql);
		ResultSet key = insStmt.getGeneratedKeys();
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
	 * @param discId
	 * @param pcId
	 * @return
	 */
	protected Integer createBlankCe(Integer discId, Integer pcId) throws Exception {
		logThis("createBlankCe(" + discId + ", " + pcId + ")");
		String sql = "INSERT INTO collectingevent(TimestampCreated, TimestampModified, Version, StartDatePrecision, EndDatePrecision, "
				+ "CreatedByAgentID, DisciplineID, PaleoContextID) VALUES(now(), now(), 0, 1, 1," 
				+ userAgentId + ", " + discId + ", " + pcId + ")";
		insStmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		logThisSql(sql);
		ResultSet key = insStmt.getGeneratedKeys();
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
	 * @param coID
	 * @return
	 */
	protected Integer getLocIDForCO(Integer coID) {
		Integer result = null;
		if (coID != null) {
			String sql = "SELECT LocalityID FROM collectingevent ce LEFT JOIN collectionobject co "
					+ " ON co.CollectingEventID=ce.CollectingEventID WHERE co.CollectionObjectID=" + coID;
			result = BasicSQLUtils.getCount(con, sql);
		}
		return result;
	}

	/**
	 * @param coID
	 * @return
	 */
	protected Integer getCeIDForCO(Integer coID) {
		Integer result = null;
		if (coID != null) {
			String sql = "SELECT CollectingEventID FROM collectionobject co WHERE co.CollectionObjectID=" + coID;
			result = BasicSQLUtils.getCount(con, sql);
		}
		return result;
	}

	/**
	 * @param collectionID
	 * @throws Exception
	 */
	protected void copyPCIdsFromCoToLocSharedCE(Integer collectionID) throws Exception {
		throw new Exception("copyPCIdsFromCoToLocSharedCE is not implemented");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			//String connStr = "jdbc:mysql://localhost/flaip05?characterEncoding=UTF-8&autoReconnect=true";
			String connStr = "jdbc:mysql://localhost/samnoble5_6?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = DriverManager.getConnection(connStr, "Master", "Master");
			PaleoUtils pu = new PaleoUtils(con, "samnoble5_6", "Master", "Master", 1);
			try {
				//pu.unshareCEs(3);
				//pu.copyPaleoContextIdsFromCoToLoc(4);
				//pu.removePaleoContextDuplicates(5);
				//pu.removePaleoContextDuplicates(6);
				//pu.removePaleoContextDuplicates(3);
				//pu.copyPaleoContextIdsFromCoToLoc(5);
				//pu.copyPaleoContextIdsFromCoToLoc(6);
				//pu.copyPaleoContextIdsFromCoToCe(7);
				//pu.copyPaleoContextIdsFromCoToLoc(4);
				pu.unSharePaleoContextsForTbl(3, "collectingevent");
			} finally {
				//pu.writeLog("D:/data/NDGS/unsharece.txt");
				//pu.writeSqlLog("D:/data/NDGS/unsharece_sql.txt");
				//pu.writeLog("D:/data/Oregon/pctoloc_2.txt");
				//pu.writeSqlLog("D:/data/Oregon/pctoloc_sql_2.txt");
				//pu.writeLog("D:/data/UTA/pctoce.txt");
				//pu.writeSqlLog("D:/data/UTA/pctoce_sql.txt");
				pu.writeLog("/home/timo/datas/samnoble/unsharepc.txt");
				pu.writeSqlLog("/home/timo/datas/samnoble/unsharepcsql.txt");
				//pu.writeLog("/home/timo/datas/ku/VP/pccunshare_sql.txt");
				//pu.writeSqlLog("D:/data/Oregon/pcunshare_sql_2.txt");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}
}
