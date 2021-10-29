/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBInfoBase;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Pair;


/**
 * @author timo
 *
 */
public class LSUUtil extends UtilitaryBase {

	/**
	 * @param con
	 */
	public LSUUtil(Connection con) {
		super(con);
	}

	/**
	 * @throws Exception
	 */
	public void moveTRSFromCoaToLoc() throws Exception {
		buildStatement();
		String locSql = "SELECT DISTINCT ce.LocalityID "
				+ "FROM collectionobjectattribute coa INNER JOIN collectionobject co ON "
				+ "co.CollectionObjectAttributeID=coa.CollectionObjectAttributeID INNER JOIN collectingevent ce ON "
				+ "ce.CollectingEventID = co.CollectingEventID WHERE coa.Text3 IS NOT NULL ORDER BY 1";
		ResultSet rs = stmt.executeQuery(locSql);
		try {
			while (rs.next()) {
				processCoaTRSForLoc(rs.getInt(1));
			}
			writeLog("D:/data/lsu/LSUHerb/TRSLog.txt");
			writeSqlLog("D:/data/lsu/LSUHerb/TRSLogSql.sql");
		} finally {
			rs.close();
			stmt.close();
		}
	}
	
	/**
	 * @throws Exception
	 */
	public void moveSp5LatLngAndCntyToSp6() throws Exception {
		buildStatement();
		String sql = 
			"select co.CollectionObjectID, l5.OriginalLatLongUnit, l5.latitude1, l5.longitude1, g5.county " +
			"from lsuherb5.collectionobjectcatalog coc " +
			"inner join lsuherb5.collectionobject co5 on co5.collectionobjectid = coc.collectionobjectcatalogid " +
			"inner join lsuherb5.collectingevent ce5 on ce5.collectingeventid = co5.collectingeventid " +
			"inner join lsuherb5.locality l5 on l5.localityid = ce5.localityid " +
			"inner join collectionobject co on co.number2 = coc.catalognumber " +
			"inner join collectingevent ce on ce.collectingeventid = co.collectingeventid " +
			"inner join locality l on l.localityid = ce.localityid " +
			"inner join (select distinct RecordID from spauditlog where TableNum=2 and TimestampCreated < date('2012/2/11')) editedLocs " +
			"on editedLocs.RecordID = l.localityid " +
			"inner join geography g on g.geographyid = l.geographyid " +
			"inner join lsuherb5.geography g5 on g5.geographyid = l5.geographyid " +
			"where coc.SubNumber=0 " + 
			"and ((abs(ifnull(l5.latitude1,0) - ifnull(l.latitude1,0)) > .001 or abs(ifnull(l5.longitude1,0) - ifnull(l.longitude1,0)) > .001) " +
			"or g5.county != g.name)";
		ResultSet rs = stmt.executeQuery(sql);
		try {
			while (rs.next()) {
				moveSp5LatLngAndCntyToSp6(rs.getInt(1), rs.getInt(2), rs.getBigDecimal(3), rs.getBigDecimal(4), rs.getString(5));
			}
			writeLog("D:/data/lsu/LSUHerb/TRSLog.txt");
			writeSqlLog("D:/data/lsu/LSUHerb/TRSLogSql.sql");
		} finally {
			rs.close();
			stmt.close();
		}
	}
	
	/**
	 * @param coId
	 * @param latLngUnit
	 * @param lat
	 * @param lng
	 * @param county
	 * @throws Exception
	 */
	protected void moveSp5LatLngAndCntyToSp6(Integer coId, int latLngUnit, BigDecimal lat, BigDecimal lng, String county) throws Exception {
		Integer currentLocId = getCurrentLocId(coId);
		boolean locIsShared = locIsShared(currentLocId);
		if (locIsShared) {
			Integer newLocId = cloneLocality(currentLocId);
			updateLocForCo(coId, newLocId);
			updateLocality(newLocId, latLngUnit, lat, lng, county);
		} else {
			updateLocality(currentLocId, latLngUnit, lat, lng, county);
		}
	}
	
	/**
	 * @param locId
	 * @param latLngUnit
	 * @param lat
	 * @param lng
	 * @param county
	 * @throws Exception
	 */
	protected void updateLocality(Integer locId, Integer latLngUnit, BigDecimal lat, BigDecimal lng, String county) throws Exception {
		Pair<Integer, Pair<Integer, String>> currentGeo = getCurrentGeo(locId);
		int currentRank = currentGeo.getSecond().getFirst();
		String currentName = currentGeo.getSecond().getSecond();
		if (county != null) {
			if (currentRank != 400 || !currentName.equals(county)) {
				Integer newGeoId = getCountyGeoId(county);
				updateGeoForLoc(locId, newGeoId);
			}
		} else {
			//we know everything's OK for the LSU herb dbs.
		}
		if (!latLngMatch(locId, latLngUnit, lat, lng)) {
			updateLatLngForLoc(locId, latLngUnit, lat, lng);
		}
	}
	
	/**
	 * @param locId
	 * @param latLngUnit
	 * @param lat
	 * @param lng
	 * @throws Exception
	 */
	protected void updateLatLngForLoc(Integer locId, Integer latLngUnit, BigDecimal lat, BigDecimal lng) throws Exception {
		LatLonConverter.FORMAT f = LatLonConverter.FORMAT.values()[latLngUnit];
		int decimalLen = LatLonConverter.DECIMAL_SIZES[f.ordinal()];
		String lat1Text = lat == null ? null 
				: LatLonConverter.format(lat, LatLonConverter.LATLON.Latitude, f, LatLonConverter.DEGREES_FORMAT.None, decimalLen);
		String lng1Text = lng == null ? null 
				: LatLonConverter.format(lng, LatLonConverter.LATLON.Longitude, f, LatLonConverter.DEGREES_FORMAT.None, decimalLen);
		String sql = "UPDATE locality SET SrcLatLongUnit=" + latLngUnit + ", "
				+ "Latitude1=" + (lat == null ? "NULL" : String.format("%12.10f", lat)) + ", "
				+ "Longitude1=" + (lng == null ? "NULL" : String.format("%13.10f", lng)) + ", "
				+ "Lat1Text=" + (lat1Text == null ? "NULL" : "'" + lat1Text.replace("'", "''") + "'") + ", "
				+ "Long1Text=" + (lng1Text == null ? "NULL" : "'" + lng1Text.replace("'", "''") + "'") + " "
				+ "WHERE LocalityID=" + locId;
		if (1 != BasicSQLUtils.update(con, sql)) {
			throw new Exception("unable to update: " + sql);
		}
	}
	
	/**
	 * @param countyName
	 * @return
	 * @throws Exception
	 */
	protected Integer getCountyGeoId(String countyName) throws Exception {
		//assuming one geography tree with standard county level
		String sql = "SELECT GeographyID FROM geography WHERE RankID=400 AND Name='" + countyName.replace("'", "''") + "'";
		return BasicSQLUtils.querySingleObj(con, sql);
	}
	
	/**
	 * @param locId
	 * @param geoId
	 * @throws Exception
	 */
	protected void updateGeoForLoc(Integer locId, Integer geoId) throws Exception {
		String sql = "UPDATE locality SET GeographyID=" + geoId + " WHERE LocalityID=" + locId;
		if (1 != BasicSQLUtils.update(con, sql)) {
			throw new Exception("error updating: " + sql);
		}
	}
	
	/**
	 * @param locId
	 * @param latLngUnit
	 * @param lat
	 * @param lng
	 * @return
	 */
	protected boolean latLngMatch(Integer locId, Integer latLngUnit, BigDecimal lat, BigDecimal lng) throws Exception {
		String sql = "SELECT SrcLatLongUnit, Latitude1, Longitude1 FROM locality WHERE LocalityID=" + locId;
		Object[] row = BasicSQLUtils.queryForRow(con, sql);
		if (row == null) {
			throw new Exception("unable to locate geo data: " + sql);
		} else {
			Integer dbUnits = Integer.class.cast(row[0]);
			BigDecimal dbLat = BigDecimal.class.cast(row[1]);
			BigDecimal dbLng = BigDecimal.class.cast(row[2]);
			return isEqualWithNullTest(latLngUnit, dbUnits) && isEqualWithNullTest(lat, dbLat)
					&& isEqualWithNullTest(lng, dbLng);
		}	
	}
	
	/**
	 * @param o1
	 * @param o2
	 * @return
	 */
	protected boolean isEqualWithNullTest(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 != null) {
			return o1.equals(o2);
		} else {
			return false;
		}
	}
	/**
	 * @param locId
	 * @return
	 * @throws Exception
	 */
	protected Pair<Integer, Pair<Integer, String>> getCurrentGeo(Integer locId) throws Exception {
		String sql = "SELECT g.GeographyID, RankID, Name FROM geography g INNER JOIN locality l on l.GeographyID=g.GeographyID WHERE l.LocalityID=" + locId;
		Object[] row = BasicSQLUtils.queryForRow(con, sql);
		return new Pair<Integer, Pair<Integer, String>>(Integer.class.cast(row[0]), new Pair<Integer, String>(Integer.class.cast(row[1]), String.class.cast(row[2])));
	}
	
	/**
	 * @param collectionObjectId
	 * @return
	 * @throws Exception
	 */
	protected Integer getCurrentLocId(Integer collectionObjectId) throws Exception {
		String sql = "SELECT LocalityID FROM collectionobject co INNER JOIN collectingevent ce "
				+ "on ce.CollectingEventID=co.CollectingEventID WHERE co.CollectionObjectID=" + collectionObjectId;
		return BasicSQLUtils.querySingleObj(con, sql);
	}
	
	/**
	 * @param localityID
	 * @param collectionObjectId
	 * @return
	 * @throws Exception
	 */
	protected boolean locIsShared(Integer localityID) throws Exception {
		//works for embedded ce
		String sql = "SELECT count(*) FROM collectingevent WHERE LocalityID=" + localityID;
		return 1 < BasicSQLUtils.getCountAsInt(con, sql);
	}
	
	/**
	 * @param originalLocalityID
	 * @return
	 * @throws Exception
	 */
	protected Integer cloneLocality(Integer originalLocalityID) throws Exception {
		Object[] origRow = LocalityDuplicateRemover.getAllFlds(con, "locality", originalLocalityID);
		List<Pair<Pair<DBTableInfo, Object>, Integer>> flds = LocalityDuplicateRemover.getFldsForTblAndOwnedTbls("locality"); 
		return createNewLocality(flds, origRow);
	}
	
	/**
	 * @param coId
	 * @param locId
	 * @throws Exception
	 */
	protected void updateLocForCo(Integer coId, Integer locId) throws Exception {
		//assuming embedded ce
		String sql = "UPDATE collectionobject co INNER JOIN collectingevent ce ON ce.CollectingEventID=co.CollectingEventID "
				+ "SET ce.LocalityID=" + locId + " WHERE CollectionObjectID=" + coId;
		if (1 != BasicSQLUtils.update(con, sql)) {
			throw new Exception("error updating: " + sql);
		}
	}
	
	/**
	 * @param localityID
	 * @throws Exception
	 */
	protected void processCoaTRSForLoc(Integer localityID) throws Exception {
		java.sql.Statement locStmt = con.createStatement();
		//There's only case of a TRS with no locality --- handle it by hand
//		if (localityID == 0) {
//			localityID = null;
//		}
		String locSql = "SELECT ce.CollectingEventID, co.CollectionObjectID, coa.Text3 "
				+ "FROM collectionobjectattribute coa INNER JOIN collectionobject co ON "
				+ "co.CollectionObjectAttributeID=coa.CollectionObjectAttributeID INNER JOIN collectingevent ce ON "
				+ "ce.CollectingEventID = co.CollectingEventID WHERE ce.LocalityID" 
				+ (localityID == null ? " IS NULL" : "=" + localityID)
				+ " AND coa.Text3 IS NOT NULL ORDER BY 1, 2, 3";
		ResultSet rs = locStmt.executeQuery(locSql);
		try {
			while (rs.next()) {
				processCoaTRSForTRS(localityID, rs.getInt(1), rs.getInt(2), rs.getString(3));
			}
		} finally {
			rs.close();
			locStmt.close();
		}
	}
	
	/**
	 * @param ceID
	 * @param coID
	 * @param trs
	 * @throws Exception
	 */
	protected void processCoaTRSForTRS(Integer origLocalityID, Integer ceID, Integer coID, String trs) throws Exception {
		Integer newLocalityID = matchLocWithTRS(origLocalityID, trs);
		if (newLocalityID != origLocalityID) {
			//assuming embedded collectingevents
			String upSql = "UPDATE collectingevent SET LocalityID=" + newLocalityID 
					+ " WHERE CollectingEventID=" + ceID;
			if (BasicSQLUtils.update(con, upSql) == 1) {
				logThisSql(upSql);
			} else {
				logThis("error updating: " + upSql);
			}
		}
	}
	
	/**
	 * @param origLocalityID
	 * @param trs
	 * @return
	 */
	protected Integer matchLocWithTRS(Integer origLocalityID, String trs) throws Exception {
		Object[] origRow = LocalityDuplicateRemover.getAllFlds(con, "locality", origLocalityID);
		List<Pair<Pair<DBTableInfo, Object>, Integer>> flds = LocalityDuplicateRemover.getFldsForTblAndOwnedTbls("locality"); 
		int f;
		for (f=0; f<flds.size(); f++) {
			Pair<DBTableInfo, Object> fld = flds.get(f).getFirst();
			if (fld.getFirst().getTableId() == Locality.getClassTableId()) {
				if (fld.getSecond() instanceof DBFieldInfo) {
					DBFieldInfo fldInfo = DBFieldInfo.class.cast(fld.getSecond());
					if (fldInfo.getColumn().equals("Text3")) {
						break;
					}
				}
			}
		}
		if (f < flds.size()) {
			if ((origRow[f] == null && 1 == BasicSQLUtils.getCount(con, "SELECT count(*) FROM collectingevent WHERE LocalityID="+origLocalityID)) 
					|| trs.equals(origRow[f])) {
				if (origRow[f] == null ) {
					Statement upSt = con.createStatement();
					try {
						String upSql = "UPDATE locality SET Text3='" + trs.replace("'", "''") + "' WHERE LocalityID=" + origLocalityID;
						if (1 != upSt.executeUpdate(upSql)) {
							throw new Exception("unable to update: " + upSql);
						} else {
							logThisSql(upSql);
						}
					} finally {
						upSt.close();
					}
				}
				return origLocalityID;
			} else {
				origRow[f] = trs;
				List<Object> matches = LocalityDuplicateRemover.getDuplicates(con, "locality", origRow);
				if (matches.size() > 0) {
					if (matches.size() > 1) {
						logThis("duplicate localities present. run the duplicate remover.");
					}
					return Integer.class.cast(matches.get(0));
				} else {
					return createNewLocality(flds, origRow);
				}
			}
		} else {
			throw new Exception("unable to find TRS field?!?");
		}
	}
	
	/**
	 * @param flds
	 * @param vals
	 * @return
	 * @throws Exception
	 */
	protected Integer createNewLocality(List<Pair<Pair<DBTableInfo, Object>, Integer>> flds, Object[] vals) throws Exception {
		Set<DBTableInfo> childTbls = new HashSet<DBTableInfo>();
		DBTableInfo locInfo = null;
		for (Pair<Pair<DBTableInfo, Object>, Integer> fld : flds) {
			DBTableInfo info = fld.getFirst().getFirst();
			if (locInfo == null && info.getTableId() == Locality.getClassTableId()) {
				locInfo = info;
			} else if (info.getTableId() != Locality.getClassTableId()) {
				childTbls.add(info);
			}
		}
		List<Pair<String, Object>> defs = new ArrayList<Pair<String, Object>>();
		Integer locID = insertRecord(flds, vals, locInfo, defs);
		defs.add(new Pair<String, Object>("LocalityID", locID));
		for (DBTableInfo tbl : childTbls) {
			insertRecord(flds, vals, tbl, defs);
		}
		return locID;
	}
	
	/**
	 * @param flds
	 * @param vals
	 * @param tbl
	 * @return
	 * @throws Exception
	 */
	protected Integer insertRecord(List<Pair<Pair<DBTableInfo, Object>, Integer>> flds, Object[] vals, DBTableInfo tbl, 
			List<Pair<String, Object>> defs) throws Exception {
		//Assuming one insert -- i.e. the Integer in the outer pair is always 0
		
		if (dataIsPresent(flds, tbl, vals)) {
			List<Pair<DBInfoBase, Object>> sysFlds = getSystemFldsWithDefaults(tbl, defs);
			String insSql = "INSERT INTO " + tbl.getName() + "(" + getNamesCommaSeparated(sysFlds) + ", " 
					+ getNamesCommaSeparated(flds, tbl) + ")"
					+ " VALUES(" + getValsCommaSeparated(sysFlds) + ", " + getValsCommaSeparated(flds, tbl, vals) + ")";
			Statement insStmt = con.createStatement();
			try {
				insStmt.executeUpdate(insSql, Statement.RETURN_GENERATED_KEYS);
				ResultSet key = insStmt.getGeneratedKeys();
				try {
					if (!key.next()) {
						key.close();
						throw new Exception("Insert failed: " + insSql);
					}
					logThisSql(insSql);
					return key.getInt(1);
				} finally {
					key.close();
				}
			} finally {
				insStmt.close();
			}
		} else {
			return null;
		}
	}
	
	/**
	 * @param flds
	 * @param fromTbl
	 * @return
	 */
	protected String getValsCommaSeparated(List<Pair<Pair<DBTableInfo, Object>, Integer>> flds, DBTableInfo fromTbl, Object[] vals) {
		String result = "";
		for (int f = 0; f < flds.size(); f++) {
			Pair<Pair<DBTableInfo, Object>, Integer> fld = flds.get(f);
			if (fld.getFirst().getFirst().getTableId() == fromTbl.getTableId()) {
				if (!"".equals(result)) {
					result += ", ";
				}
				result += getSqlValStr(DBInfoBase.class.cast(fld.getFirst().getSecond()), vals[f]);
			}
		}
		return result;
	}
	
	
	/**
	 * @param flds
	 * @param fromTbl
	 * @param vals
	 * @return
	 */
	protected boolean dataIsPresent(List<Pair<Pair<DBTableInfo, Object>, Integer>> flds, DBTableInfo fromTbl, Object[] vals) {
		for (int f = 0; f < flds.size(); f++) {
			Pair<Pair<DBTableInfo, Object>, Integer> fld = flds.get(f);
			if (fld.getFirst().getFirst().getTableId() == fromTbl.getTableId()) {
				if (vals[f] != null) return true;
			}
		}
		return false;
	}
	/**
	 * @param fldList
	 * @return
	 */
	protected String getValsCommaSeparated(List<Pair<DBInfoBase, Object>> fldList) {
		String result = "";
		for (Pair<DBInfoBase, Object> fld : fldList) {
			if (!"".equals(result)) {
				result += ", ";
			}
			result += getSqlValStr(fld.getFirst(), fld.getSecond());
		}
		return result;
	}
	
	/**
	 * @param fld
	 * @param val
	 * @return
	 */
	protected String getSqlValStr(DBInfoBase fld, Object val) {
		if (val instanceof String) {
			String valStr = String.class.cast(val);
			if (valStr.startsWith("#") && valStr.endsWith("#")) {
				return valStr.replace("#", "");
			} else {
				return "'" + valStr.replace("'", "''") + "'";
			}
		} else if (val != null) {
			return val.toString();
		} else {
			return "NULL";
		}
	}
	
	/**
	 * @param flds
	 * @param fromTbl
	 * @return
	 */
	protected String getNamesCommaSeparated(List<Pair<Pair<DBTableInfo, Object>, Integer>> flds, DBTableInfo fromTbl) {
		String result = "";
		for (Pair<Pair<DBTableInfo, Object>, Integer> fld : flds) {
			if (fld.getFirst().getFirst().getTableId() == fromTbl.getTableId()) {
				if (!"".equals(result)) {
					result += ", ";
				}
				result += "`" + getInfoObjColumn(DBInfoBase.class.cast(fld.getFirst().getSecond())) + "`";
			}
		}
		return result;
	}
	
	/**
	 * @param fldList
	 * @return
	 */
	protected String getNamesCommaSeparated(List<Pair<DBInfoBase, Object>> fldList) {
		String result = "";
		for (Pair<DBInfoBase, Object> fld : fldList) {
			if (!"".equals(result)) {
				result += ", ";
			}
			result += "`" + getInfoObjColumn(fld.getFirst()) + "`";
		}
		return result;
	}
	
	/**
	 * @param locInfo
	 * @return
	 */
	protected List<Pair<DBInfoBase, Object>> getSystemFldsWithDefaults(DBTableInfo info, List<Pair<String, Object>> defs) {
		List<Pair<DBInfoBase, Object>> result = new ArrayList<Pair<DBInfoBase, Object>>();
		List<Pair<Pair<DBTableInfo, Object>, Integer>>  preResult = LocalityDuplicateRemover.getFldsForTbl(new Pair<DBTableInfo, DBRelationshipInfo>(info, null), 
				LocalityDuplicateRemover.GET_SYS_FLDS);
		//XXX Assuming no one-to-manies --- outer pair's integer is always 0.
		for (Pair<Pair<DBTableInfo, Object>, Integer> fld : preResult) {
			DBInfoBase infoObj = DBInfoBase.class.cast(fld.getFirst().getSecond());
			result.add(new Pair<DBInfoBase, Object>(infoObj, getDefaultValForSysFld(infoObj, info, defs)));
		}
		return result;
	}
	
	/**
	 * @param infoObj
	 * @return
	 */
	protected String getInfoObjColumn(DBInfoBase infoObj) {
		if (infoObj instanceof DBFieldInfo) {
			return DBFieldInfo.class.cast(infoObj).getColumn();
		} else {
			return DBRelationshipInfo.class.cast(infoObj).getColName();
		}
	}
	
	/**
	 * @param fld
	 * @param tbl
	 * @param defs
	 * @return
	 */
	protected Object getDefaultValForSysFld(DBInfoBase fld, DBTableInfo tbl, List<Pair<String, Object>> defs) {
		String fldName = getInfoObjColumn(fld);
		for (Pair<String,Object> def: defs) {
			if (fldName.equalsIgnoreCase(def.getFirst())) {
				return def.getSecond();
			}
		}
		if ("TimestampModified".equals(fldName) || "TimestampCreated".equals(fldName)) {
			return "#now()#";
		}
		if ("DisciplineID".equals(fldName)) {
			return new Integer(3);
		}
		if ("Version".equals(fldName)) {
			return new Integer(0);
		}
		if ("GUID".equals(fldName)) {
			return "#uuid()#";
		}
		return null;
	}
	
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/lsuherb0627a?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
//			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");			
//			Connection con5 = Sampler.getConnection("sqlserver", "ENTOSPSMALL", "129.237.201.205\\SQLEXPRESS2008", "sa", "hyla606");
			LSUUtil ut = new LSUUtil(con);
			//ut.moveTRSFromCoaToLoc();
			ut.moveSp5LatLngAndCntyToSp6();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
