/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.csvreader.CsvReader;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.tools.export.Sampler;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class UtilitaryBase {
	protected Connection con;
	protected Statement stmt;
	protected List<String> log = new ArrayList<String>();
	protected List<String> sqlLog = new ArrayList<String>();
	protected boolean echoSqlLog = false;
	protected boolean echoLog = false;
	
	public UtilitaryBase(Connection con) {
		this.con = con;
	}
	
	/**
	 * @throws SQLException
	 */
	protected void buildStatement() throws SQLException {
		stmt = con.createStatement();
	}
	
	
	/**
	 * @return the echoSqlLog
	 */
	public boolean isEchoSqlLog() {
		return echoSqlLog;
	}

	/**
	 * @param echoSqlLog the echoSqlLog to set
	 */
	public void setEchoSqlLog(boolean echoSqlLog) {
		this.echoSqlLog = echoSqlLog;
	}

	/**
	 * @return the echoLog
	 */
	public Boolean getEchoLog() {
		return echoLog;
	}

	/**
	 * @param echoLog the echoLog to set
	 */
	public void setEchoLog(Boolean echoLog) {
		this.echoLog = echoLog;
	}

	/**
	 * 
	 */
	protected void clearLog() {
		log.clear();
	}

	/**
	 * 
	 */
	protected void clearSqlLog() {
		sqlLog.clear();
	}

	/**
	 * @param fileName
	 */
	protected void writeLog(String fileName) throws Exception {
		FileUtils.writeLines(new File(fileName), "utf8", log);
	}

	/**
	 * @param fileName
	 */
	protected void writeSqlLog(String fileName) throws Exception {
		FileUtils.writeLines(new File(fileName), "utf8", sqlLog);
	}

	
	/**
	 * @param verbiage
	 */
	protected void logThis(String verbiage) {
		log.add(verbiage);
		if (echoLog) {
			System.out.println(verbiage);
		}
	}

	/**
	 * @param verbiage
	 */
	protected void logThisSql(String sql) {
		String str = sql.endsWith(";")?sql:sql+";";
		sqlLog.add(str);
		if (echoSqlLog) {
			System.out.println(str);
		}
	}

	/**
	 * @param prompt
	 * @param toRead
	 * @return
	 */
	protected boolean readValues(String prompt, List<Pair<String, String>> toRead) {
		boolean done = false;
		boolean result = false;
	    Scanner in = new Scanner(System.in);
	    System.out.println();
	    System.out.println(prompt);
		while (!done) {
			for (Pair<String, String> val : toRead) {
				System.out.print(val.getFirst() + "? " + (StringUtils.isEmpty(val.getSecond()) ? "" : " (" + val.getSecond() + ")"));
				String v = in.nextLine();
				if (!" ".equals(v)) {
					val.setSecond(v);
				}
			}
			System.out.println();
			for (Pair<String, String> val : toRead) {
				System.out.print(val.getFirst() + "=" + val.getSecond() + " | ");
			}
			System.out.println();
			System.out.print("OK (y,n or c to cancel)?");
			String response = in.nextLine();
			if ("y".equalsIgnoreCase(response)) {
				done = true; //wtf
				result = true;
			} else if ("c".equalsIgnoreCase(response)) {
				done = true;
			}
		}
		return result;
	}

	/**
	 * @param connStr
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection(String connStr, String user, String pw) throws SQLException {
		return  DriverManager.getConnection(connStr, user, pw);
	}

	/**
	 * @param y1
	 * @param m1
	 * @param d1
	 * @param y2
	 * @param m2
	 * @param d2
	 * @return
	 */
	public static int compareTimestampParts(Integer y1, Integer m1, Integer d1, Integer y2, Integer m2, Integer d2) {
		int result = y1.compareTo(y2);
		if (result == 0) {
			result = m1.compareTo(m2);
			if (result == 0) {
				result = d1.compareTo(d2);
			}
		}
		return result;
	}

	/**
	 * @param list sorted list of bigints.
	 * @return list of gaps in list
	 */
	public List<Pair<BigInteger,BigInteger>> getGaps(List<BigInteger> list) {
		List<Pair<BigInteger, BigInteger>> result = new ArrayList<Pair<BigInteger, BigInteger>>();
		if (list.size() > 0) {
			BigInteger a = list.get(0);
			BigInteger one = new BigInteger("1");
			for (int l=1; l < list.size(); l++) {
				BigInteger b = list.get(l);
				BigInteger c = a.add(one);
				if (!b.equals(c)) {
					result.add(new Pair<BigInteger, BigInteger>(c, b.subtract(one)));
				}
				a = b;
			}
		}
		return result;
	}
	
	/**
	 * @param collectionID for collection with strictly numeric cat nums.
	 * @return
	 */
	public List<Pair<BigInteger,BigInteger>> getNumericCatNumGaps(Integer collectionID) {
		List<Object> catnums = BasicSQLUtils.querySingleCol(con, "select catalognumber from collectionobject where CollectionMemberID="
				+ collectionID + " and catalognumber is not null order by catalognumber");
		List<BigInteger> bigcats = new ArrayList<BigInteger>(); 
		for (Object numObj : catnums) {
			bigcats.add(new BigInteger(numObj.toString()));
		}
		catnums.clear();
		return getGaps(bigcats);
	}
	
	/**
	 * @param collectionID for collection with strictly numeric cat nums.
	 * @return
	 */
	public List<Pair<BigInteger,BigInteger>> getCOIDGaps() {
		List<Object> ids = BasicSQLUtils.querySingleCol(con, "select CollectionObjectID from collectionobject order by CollectionObjectID");
		List<BigInteger> bigids = new ArrayList<BigInteger>(); 
		for (Object numObj : ids) {
			bigids.add(new BigInteger(numObj.toString()));
		}
		ids.clear();
		return getGaps(bigids);
	}

	/**
	 * @param con
	 * @param tableName
	 * @throws Exception
	 */
	protected static void fillInSp6ModifiedInfoForTbl(Connection con, String tableName) throws Exception {
		Statement stmt = con.createStatement();
		String sql1 = "UPDATE `" + tableName + "` SET TimestampModified=TimestampCreated WHERE "
				+ "TimestampModified IS NULL";
		String sql2 = "UPDATE `" + tableName + "` SET ModifiedByAgentID=CreatedByAgentID WHERE "
				+ "ModifiedByAgentID IS NULL";
		try {
			System.out.println(sql1);
			stmt.executeUpdate(sql1);
			System.out.println(sql2);
			stmt.executeUpdate(sql2);
		} finally {
			stmt.close();
		}
	}
	
	/**
	 * @param con
	 * @param dbName
	 * @throws Exception
	 */
	public static void fillInSp6ModifiedInfo(Connection con, String dbName) throws Exception {
		Sampler.schemaCon = DriverManager.getConnection(Sampler.getConnectionStr("information_schema", "localhost"), 
				"root", "root");
		String sql = Sampler.getTablesSQLForDB(dbName);
		Statement stmt = Sampler.schemaCon.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		try {
			while (rs.next()) {
				String tbl = rs.getString(1);
				Statement fldStmt = Sampler.schemaCon.createStatement();
				ResultSet fldRs = fldStmt.executeQuery(Sampler.getColumnSQLForTable(dbName, tbl));
				boolean hasModInfo = false;
				try {
					while (fldRs.next()) {
						if ("TimestampModified".equalsIgnoreCase(fldRs.getString(1))) {
							hasModInfo = true;
							break;
						}
					}
				} finally {
					fldRs.close();
					fldStmt.close();
				}
				if (hasModInfo) {
					fillInSp6ModifiedInfoForTbl(con, tbl);
				}
			}
		} finally {
			rs.close();
			stmt.close();
		}
	}
	
	/**
	 * @param con
	 * @param dbName
	 * @throws Exception
	 */
	public static void fillInDatePrecisionInfo(Connection con, String dbName) throws Exception {
		Sampler.schemaCon = DriverManager.getConnection(Sampler.getConnectionStr("information_schema", "localhost"), 
				"root", "root");
		String sql = Sampler.getTablesSQLForDB(dbName);
		Statement stmt = Sampler.schemaCon.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		try {
			while (rs.next()) {
				String tbl = rs.getString(1);
				Statement fldStmt = Sampler.schemaCon.createStatement();
				ResultSet fldRs = fldStmt.executeQuery(Sampler.getColumnSQLForTable(dbName, tbl));
				List<String> precFlds = new ArrayList<String>();
				try {
					while (fldRs.next()) {
						if (fldRs.getString(1).endsWith("DatePrecision")) {
							precFlds.add(fldRs.getString(1));
						}
					}
				} finally {
					fldRs.close();
					fldStmt.close();
				}
				for (String precFld : precFlds) {
					fillInDatePrecisionInfo(con, tbl, precFld);
				}
			}
		} finally {
			rs.close();
			stmt.close();
		}
		
	}
	
	/**
	 * @param con
	 * @param tbl
	 * @param precFld
	 * @throws Exception
	 */
	protected static void fillInDatePrecisionInfo(Connection con, String tbl, String precFld) throws Exception {
		Statement stmt = con.createStatement();
		String sql = "UPDATE `" + tbl + "` SET `" + precFld + "`=1 WHERE `" + precFld + "` IS NULL";
		try {
			System.out.println(sql);
			stmt.executeUpdate(sql);
		} finally {
			stmt.close();
		}
	}
	
	protected List<String> createTableDefinitionFromCsv(String f, char delimiter, String encoding, 
			char qualifier, char escaper, String tblname, boolean addKey) throws Exception {
        CsvReader csv = new CsvReader(new FileInputStream(f), delimiter, Charset.forName(encoding));
        csv.setTextQualifier(qualifier);
    	if (escaper == '\\') {
    		csv.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
    	} else {
    		csv.setEscapeMode(CsvReader.ESCAPE_MODE_DOUBLED);
    	}
    	int headless = 0;
    	if (csv.readHeaders()) {
    		String[] hdr = csv.getHeaders();
    		List<String> result = new ArrayList<String>();
    		result.add("CREATE TABLE `" + tblname + "` (");
    		for (String fld : hdr) {
    			if ("".equals(fld)) {
    				fld = "headless" + headless++;
    			}
    			result.add("`" + fld + "` VARCHAR(300) NULL ,");
    		}
    		if (addKey) {
    			String key = "id" + tblname;
    			result.add("`" + key + "` INT NOT NULL AUTO_INCREMENT ,");
    			result.add("PRIMARY KEY (`" + key + "`)");
    		}
    		result.add(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
    		for (String l : result) {
    			System.out.println(l);
    		}
    		return result;
    	} else {
    		throw new Exception("unable to read headers");
    	}

	}
	
	public static void trimCharVals(Connection con, String tbl, boolean setEmptyStrToNull, boolean verticalTabToNewLine, boolean setToNullIfWhitespace) throws Exception {
		Statement stmt = con.createStatement();
		ResultSet fldrs = stmt.executeQuery("select fieldname, fieldtype, idinfo from info where tablename='" + tbl + "'");
		
		while (fldrs.next()) {
			String fldname = fldrs.getString(1);
			String fldtype = fldrs.getString(2);
			if (fldtype == null || fldtype.indexOf("char") != 0 || fldtype.indexOf("text") != 0) {
				Statement upStmt = con.createStatement();
				try {
					String sql = "update `" + tbl + "` set `" + fldname + "` = trim(`" + fldname + "`)";
					upStmt.executeUpdate(sql);
					if (setEmptyStrToNull) {
						sql = "update `" + tbl + "` set `" + fldname + "` = NULL where trim(`" + fldname + "`) = ''";
						upStmt.executeUpdate(sql);
					}
					if (verticalTabToNewLine) {
						sql = "update `" + tbl + "` set `" + fldname + "` = replace(`" + fldname + "`, CHAR(11), '\n')";
						upStmt.executeUpdate(sql);
					}
					if (setToNullIfWhitespace) {
						sql = "update `" + tbl + "` set `" + fldname + "` = NULL where trim(replace(replace(replace(replace(`" + fldname + "`,CHAR(11),''), '\n',''),'\r',''),'\t','')) = ''";
						upStmt.executeUpdate(sql);
					}
				} finally {
					upStmt.close();
				}
			}
		}
	}
	
	public static void removeNewLinesAndReturnsAndExcessWhitespace(Connection con, String tbl, String fld, String idFld) throws Exception {
		Statement stmt = con.createStatement();
		String sql = "select `" + idFld + "`, `" + fld + "` from `" + tbl + "` where `" + fld + "` like " + 
				"'%\\\\n%' or `" + fld + "` like '%\\\\r%' or `" + fld + "` like '%\\\\t%'";
		System.out.println(sql);
		ResultSet fldrs = stmt.executeQuery(sql);
		int r = 0;
		while (fldrs.next()) {
			String val = fldrs.getString(2);
			val = val.trim();
			val = val.replaceAll("\n", " ");
			val = val.replaceAll("\r", " ");
			val = val.replaceAll("\t", " ");
			while (val.indexOf("  ") != -1) {
				val = val.replaceAll("  ", " ");
			}
			if (val.equals(fldrs.getString(2))) {
				System.out.println("splutter");
			}
			r++;
		}
		System.out.println(r);
		stmt.close();
	}
	/**
	 * @param con
	 * @param tbl
	 * @throws Exception
	 */
	public static void fillInfo(Connection con, String tbl) throws Exception {
		Statement stmt = con.createStatement();
		ResultSet fldrs = stmt.executeQuery("select fieldname, fieldtype from info where tablename='" + tbl + "'");
		
		while (fldrs.next()) {
			String fldname = fldrs.getString(1);
			String sql = "select max(`" + fldname + "`), min(`" + fldname + "`), count(distinct `" + fldname + "`), + (select count(*) from `" + tbl + "` where `" + fldname + "` is not null)";
			String fldtype = fldrs.getString(2);
			boolean isText = fldtype == null || fldtype.indexOf("char") != 0 || fldtype.indexOf("text") != 0; 
			if (isText){
				sql += ", max(length(`" + fldname + "`))";
			}
			sql += " from `" + tbl + "`";
			Statement fldStmt = con.createStatement();
			Statement upStmt = con.createStatement();
			ResultSet infors = fldStmt.executeQuery(sql);
			String computedFldType = computeFldType(con, tbl, fldname);
			try {
				if (infors.next()) {
					String maxval = infors.getString(1);
					String minval = infors.getString(2);
					Integer distinctvals = infors.getInt(3);
					Integer nonunvals = infors.getInt(4);
					Integer maxlen = isText ? infors.getInt(5) : null;
					sql = "update info set "
						+ (maxval == null ? "" : "maxval='"  +  maxval.replaceAll("'", "''") + "',")
						+ (minval ==  null ? "" : "minval='" + minval.replaceAll("'", "''") + "',")
						+ "distinctvals=" + infors.getInt(3) + ","
						+ "nonnullvals=" + infors.getInt(4) + ","
						+ "computed_type=" + (computedFldType == null ? "null" : "'" + computedFldType + "'");
					if (isText) {
						sql += ", maxlen=" + infors.getInt(5);
					}
					sql += " where tablename='" + tbl + "' and fieldname='" + fldname + "';";
					int updated = upStmt.executeUpdate(sql);
					if (updated != 1) {
						System.out.println("WARNING: " + updated + " updates for: " + sql);
					}
				} else {
					System.out.println("WARNING: no info for: " + sql);
				}
			} finally {
				infors.close();
				fldStmt.close();
				upStmt.close();
			}
		}		
				
	}
	
	/**
	 * @param con
	 * @param tbl
	 * @param fldname
	 * @return
	 * @throws Exception
	 */
	public static String computeFldType(Connection con, String tbl, String fldname) throws Exception {
		String sql = "select distinct `" + fldname + "` from `" + tbl +  "` where `" + fldname + "` is not null";
		Statement stmt = con.createStatement();
		ResultSet values = stmt.executeQuery(sql);
		try {
			if (!values.next()) {
				return null;
			}
			String result = "";
			do {
				String t = computeValType(values);
				if ("char".equals(t)) {
					return t;
				} else if ("".equals(result)) {
					result = t;
				} else if (!t.equals(result)) {
					if (("float".equals(result) || "int".equals(result))
							&& ("float".equals(result) || "int".equals(result))) {
						result = "float";
					} else if (("timestamp".equals(result) || "date".equals(result))
							&& ("timestamp".equals(result) || "date".equals(result))) {
						result = "timestamp";
					} else {
						return "char";
					}
				}
			} while (values.next());
			return result;
		} finally {
			values.close();
			stmt.close();
		}
	}
	
	/**
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public static String computeValType(ResultSet values) throws Exception {
		try {
			Integer i = values.getInt(1);
			Float f = values.getFloat(1);
			if (i.floatValue() == f.floatValue()) {
				return "int";
			} else {
				return "float";
			}
		} catch (Exception e) {
		}
		try {
			Date d = values.getDate(1);
			Timestamp t = values.getTimestamp(1);
			if (d.equals(t)) {
				return "date";
			} else {
				return "timestamp";
			}
		} catch (Exception e) {
		}
		return "char";
	}

	private static void fixTheDamnSBDB() throws Exception {
		Connection con = DriverManager.getConnection(Sampler.getConnectionStr("sbv2", "localhost"),
				"root", "root");
		String sql = "select distinct l.localityid, l.disciplineid, l.geographyid from locality l inner join collectingevent ce on ce.localityid = l.localityid where ce.disciplineid != l.localityid";
		Statement fldStmt = con.createStatement();
		ResultSet locsToFix = fldStmt.executeQuery(sql);
		while (locsToFix.next()) {
			fixSBLocality(locsToFix, con);
		}
		locsToFix.close();
		fldStmt.close();
	}

	private static void fixSBLocality(ResultSet loc, Connection con) throws Exception {
		String sql = "select distinct ce.disciplineid from collectingevent ce where ce.localityid = " + loc.getInt(1);
		Statement stmt = con.createStatement();
		ResultSet disciplines = stmt.executeQuery(sql);
		List<Integer> ceDisciplines = new ArrayList<>();
		Boolean hasMatchingDiscipline = false;
		while (disciplines.next()) {
			if (loc.getObject(1).equals(disciplines.getObject(1))) {
				hasMatchingDiscipline = true;
			} else {
				ceDisciplines.add(disciplines.getInt(1));
			}
		}
		if (ceDisciplines.size() == 1 && !hasMatchingDiscipline) {
			moveSBLocToDiscipline(loc, ceDisciplines.get(0), con);
		} else {
			for (Integer d : ceDisciplines) {
				createAndConnectSBLocForDisc(loc, d, con);
			}
		}
		disciplines.close();
		stmt.close();
	}

	private static void createAndConnectSBLocForDisc(ResultSet loc, Integer disciplineId, Connection con) throws Exception {
		String locFlds1 = "TimestampCreated, TimestampModified, Version, Datum, ElevationAccuracy, ElevationMethod, GML, GUID, Lat1Text,"
				+ "Lat2Text, LatLongAccuracy, LatLongMethod, LatLongType, Latitude1, Latitude2, LocalityName, Long1Text,"
				+ "Long2Text, Longitude1, Longitude2, MaxElevation, MinElevation, NamedPlace, OriginalElevationUnit,"
				+ "OriginalLatLongUnit, RelationToNamedPlace, Remarks, SGRStatus, ShortName, SrcLatLongUnit, Text1, Text2,"
				+ "Text4, Text5, VerbatimElevation, VerbatimLatitude, VerbatimLongitude, Visibility, YesNo1, YesNo2,"
				+ "YesNo3, YesNo4, YesNo5, PaleoContextID,  VisibilitySetByID, CreatedByAgentID,"
				+ "ModifiedByAgentID,Text3, DisciplineID, GeographyID";
		String locFlds2 = "l.TimestampCreated, l.TimestampModified, l.Version, Datum, ElevationAccuracy, ElevationMethod, l.GML, l.GUID, Lat1Text,"
				+ "Lat2Text, LatLongAccuracy, LatLongMethod, LatLongType, Latitude1, Latitude2, LocalityName, Long1Text,"
				+ "Long2Text, Longitude1, Longitude2, MaxElevation, MinElevation, NamedPlace, OriginalElevationUnit,"
				+ "OriginalLatLongUnit, RelationToNamedPlace, l.Remarks, SGRStatus, ShortName, SrcLatLongUnit, l.Text1, l.Text2,"
				+ "Text4, Text5, VerbatimElevation, VerbatimLatitude, VerbatimLongitude, Visibility, YesNo1, YesNo2,"
				+ "YesNo3, YesNo4, YesNo5, PaleoContextID,  VisibilitySetByID, l.CreatedByAgentID,"
				+ "l.ModifiedByAgentID";
		String locDetFlds = "TimestampCreated, TimestampModified, Version, BaseMeridian, Drainage, EndDepth, enddepthunit,"
				+ "enddepthverbatim, gml, huccode, island, islandgroup, mgrszone, nationalParkname,Number1,Number2,"
				+ "number3, number4, number5, paleolat, paleolng, rangedesc,rangedirection,section,sectionpart,startdepth,"
				+ "startdepthunit,startdepthverbatim,text1,text2,text3,text4,text5,township,townshipdirection,utmdatum,"
				+ "utmeasting,utmfalseeasting,UtmFalsenorthing,UtmNorthing,UtmOrigLatitude,UtmOrigLongitude,UtmScale,UtmZone,"
				+ "WaterBody,YesNo1,YesNo2,YesNo3,YesNo4,YesNo5,ModifiedByAgentID,CreatedByAgentID";
		String geocDetFlds = "TimestampCreated, TimestampModified, Version,ErrorPolygon,GeoRefAccuracy,GeoRefAccuracyUnits,"
				+ "GeoRefCompiledDate,GeoRefDetDate,GeoRefDetRef,GeoRefRemarks,GeoRefVerificationStatus,MaxUncertaintyEst,"
				+ "MaxUncertaintyEstUnit,NamedPlaceExtent,NoGeoRefBecause,OriginalCoordSystem,Protocol,Source,Text1,Text2,"
				+ "Text3,UncertaintyPolygon,Validation,CreatedByAgentID,AgentID,CompiledByID,ModifiedByAgentID";
		String locInsertSql = "insert into locality(" + locFlds1 + ") select " + locFlds2 + ","
				+ loc.getInt(1) + ", "
				+ disciplineId + ","
				+ getGeoSelector(disciplineId, loc)
				+ " from locality l left join geography g on g.geographyid = l.geographyid "
				+ "where l.localityid = " + loc.getInt(1);
		Statement stmt = con.createStatement();
		stmt.executeUpdate(locInsertSql, Statement.RETURN_GENERATED_KEYS);
		ResultSet newLocIdRs = stmt.getGeneratedKeys();
		newLocIdRs.next();
		int newLocId = newLocIdRs.getInt(1);
		String locDetInsSql = "insert into localitydetail (" + locDetFlds+ ",LocalityID) select " + locDetFlds + ", "
				+ newLocId + " from localitydetail where localityid = " + loc.getInt(1);
		stmt.executeUpdate(locDetInsSql);
		String geocDetInsSql = "insert into geocoorddetail (" + geocDetFlds + ",LocalityID) select " + geocDetFlds + ", "
				+ newLocId + " from geocoorddetail where localityid = " + loc.getInt(1);
		stmt.executeUpdate(geocDetInsSql);
		String updateCeSql = "update collectingevent set localityid = " + newLocId + " where localityid = " + loc.getInt(1)
				+ " and disciplineid = " + disciplineId;
		try {
			stmt.executeUpdate(updateCeSql);
		} catch (Exception e) {
			System.out.println("exception executing: " + updateCeSql);
			throw e;
		}
		newLocIdRs.close();
		stmt.close();
	}

	private static String getGeoSelector(Integer disciplineId, ResultSet loc) throws Exception {
		return loc.getObject(3) == null ? "NULL"
				: "(select geographyid from geography g2 inner join discipline d on d.geographytreedefid = g2.geographytreedefid "
				+ "where d.disciplineid = " + disciplineId + " and g2.fullname = g.fullname)";
	}

	private static void moveSBLocToDiscipline(ResultSet loc, Integer disciplineId, Connection con) throws Exception {
		String sql = "update locality l left join geography g on g.geographyid = l.geographyid "
			+ " set l.disciplineid = " + disciplineId + ", "
			+ "l.geographyid = " + getGeoSelector(disciplineId, loc)
				+ " where l.localityid = " + loc.getInt(1);
		Statement stmt = con.createStatement();
		int r = stmt.executeUpdate(sql);
		stmt.close();
		if (r != 1) {
			throw new Exception("failed to update: " + sql);
		}
	}

	private static void regExpress() {
		String regExp = "(?<one>[a-zA-z]{3})(?<two>-)(?<three>[0-9]{6})(?<four>[a-zA-z]?)";
		String[] regExpGrps = {"one","two","three","four"};
		String[] ts = {"AAA-000010","BBB-000011A","XYZ-27-XYZ","123456"};
		for (String t : ts) {
			Pattern r = Pattern.compile(regExp);
			Matcher m = r.matcher(t);
			boolean matches = m.matches();
			System.out.println(t + (matches ? " matches " : " does not match ") + regExp);
			if (matches) {
				for (int i = 1; i <= m.groupCount(); i++) {
					System.out.println("   group " +  regExpGrps[i-1] + " " + m.group(i));
				}
			}
		}
	}

	private List<String> getFieldConcatSql(String tblName, String baseName, String connector, int count) {
		List<String> result = new ArrayList<>();
		String baseName1 = baseName + "1";
		for (int c = 2; c <= count; c++) {
			String baseNameC = baseName + c;
			result.add("update " + tblName + " set " + baseName1 + " = concat(ifnull(trim(" + baseName1 + "), ''), " +
					"case when " + baseNameC + " is null then '' else concat('" + connector + "', trim(" + baseNameC + ")) end);");
		}
		return result;
	}

	private String getFieldConcatClearer(String tblName, String baseName) {
		return "update " + tblName + " set " + baseName + "1 = null where trim(" + baseName + "1) = '';";
	}

	private List<String> getFieldConcatDropSql(String tblName, String baseName, int count) {
		List<String> result = new ArrayList<>();
		for (int c = 2; c < count+1; c++) {
			result.add("alter table " + tblName + " drop column " + baseName + c + ";");
		}
		return result;
	}

//	private List<String> getConcatWestAUSplitFldsSQL(List<String[]> flds) {
//		List<String> result = new ArrayList<>();
//		for (String[] fld : flds) {
//			String tblName = fld[0];
//			String fldName = fld[1];
//			Integer count = Integer.valueOf(fld[2]);
//			result.addAll(getFieldConcatSql(tblName, fldName, " ", count));
//			result.addAll(getFieldConcatDropSql(tblName, fldName, count));
//			result.add(getFieldConcatClearer(tblName, fldName));
//		}
//		return result;
//	}
//
//	private void String[][]oncatWestAUSplitDateFlds() = {
//		{"sample1","creatdat_"},
//		{"sample1","autodat_"},
//		{"sample1","namechan_"},
//		{"sample1","date_"},
//		{"sample1","coldate1_"},
//		{"sample1","coldate2_"}
//
//		{"sample1_2","loanout_"}
//		{"sample1_2","loanin_"}
//		{"sample1_2","date6_"}
//		{"sample1_2","date1_"}
//		{"sample1_2","date2_"}
//		{"sample1_2","date3_"}
//		{"sample1_2","lastupdateddate_"}
//	}
//	private void concatWestAUSplitFlds() {
//		String[][] flds = {
//				{"sample1", "namecomm_","12"},
//				{"sample1", "plantdes_","30"},
//				{"sample1", "sitedesc_","6"},
//				{"sample1", "vegetati_","50"},
//				{"sample1", "fre_","4"},
//				{"sample1", "othernot_","4"},
//				{"sample1", "locality_","4"},
//
//				{"sample1_2", "voucher_","5"},
//				{"sample1_2", "buds_","5"},
//				{"sample1_2", "flowers_","4"},
//				{"sample1_2", "fruit_","4"},
//				{"sample1_2", "phengen_","2"},
//				{"sample1_2", "comments_","12"},
//				{"sample1_2", "tempstore_","24"},
//				{"sample1_2", "namecomm1_","12"},
//				{"sample1_2", "namecomm2_","12"},
//				{"sample1_2", "namecomm3_","12"}
//		};
//		List<String[]> fldList = new ArrayList<>();
//		for (String[] fld : flds) {
//			fldList.add(fld);
//		}
//		List<String> sql = getConcatWestAUSplitFldsSQL(fldList);
//		try {
//			FileUtils.writeLines(new File("/home/timo/datas/westernAU/fldConcat_1.sql"), sql);
//		} catch (Exception x) {
//			x.printStackTrace();
//		}
//	}


	public static void main(String[] args) {
		try {
			Connection con = DriverManager.getConnection(Sampler.getConnectionStr("borrorsp", "localhost"),
				"root", "root");
			
			//fillInSp6ModifiedInfo(con, "flaip6");
			//fillInDatePrecisionInfo(con, "flaip6");
			
			
			//UtilitaryBase.removeNewLinesAndReturnsAndExcessWhitespace(con, "repat", "latlongremarks", "idrepat");
			UtilitaryBase bu = new UtilitaryBase(con);
//			List<Pair<BigInteger, BigInteger>> gaps = bu.getCOIDGaps();
//			BigInteger sum = new BigInteger("0");
//			for (Pair<BigInteger, BigInteger> gap : gaps) {
//				BigInteger start = gap.getFirst();
//				BigInteger finish = gap.getSecond();
//				bu.logThis(start + (start.equals(finish) ? "" : " - " + finish));
//				BigInteger diff = finish.subtract(start).add(new BigInteger("1"));
//				sum = sum.add(diff);
//			}
//			bu.logThis("Total missing: " + sum);
//			bu.writeLog("D:/data/KU/ento/20141216gaps.txt");

			//fixTheDamnSBDB();


			String[] csvs = {
"accession.csv",
"agents.csv",
"attachment.csv",
"collector.csv",
"collectingevent.csv",
"collectionobject.csv",
"collectionobjectattribute.csv",
"conservdescription.csv",
"deaccession.csv",
"determination.csv",
"dimensions.csv",
"geography.csv",
"loan.csv",
"locality.csv",
"otheridentifiers.csv",
"preparation.csv",
"referencework.csv",
"taxon.csv",
"taxonbibliografia.csv",
"taxonnomcomг.csv",
"ubicacions.csv",
"variantagent.csv"
//					"GenbankIDs.csv"
//					"Reference_Work.csv"
//					"agent_merge_1_kpm1.csv"
//					"ADDRESSES_DATA_TABLE.csv",
//					"AGENT_DATA_TABLE.csv",
//					"BACKGROUND_DATA_TABLE.csv",
//					"COLLECTING_EVENT_DATA_TABLE.csv",
//					"COLLECTING_METHOD_DATA_TABLE.csv",
//					"COLLECTING_UNIT_DATA_TABLE.csv",
//					"DETERMINATION_DATA_TABLE.csv",
//					"DIGITAL_CONVERTER_DATA_TABLE.csv",
//					"EMPLOYEE_DATA_TABLE.csv",
//					"MASTER_FORMAT_DATA_TABLE.csv",
//					"MASTER_RECORDING_DATA_TABLE.csv",
//					"MEDIA_DATA_TABLE.csv",
//					"MICROPHONE_DATA_TABLE.csv",
//					"ORGANIZATION_DATA_TABLE.csv",
//					"PARABOLA_DATA_TABLE.csv",
//					"PERSON_DATA_TABLE.csv",
//					"PERSON_INFO_DATA_TABLE.csv",
//					"PREPARATION_CONTENTS_DATA_TABLE.csv",
//					"PREPARATION_DATA_TABLE.csv",
//					"PREPARATION_TYPES_DATA_TABLE.csv",
//					"RECORDER_DATA_TABLE.csv",
//					"RECORDING_BACKGROUND_DATA_TABLE.csv",
//					"RECORDING_BURNED_DATA_TABLE.csv",
//					"RECORDING_EXPORT_LOG_DATA_TABLE.csv",
//					"RECORDING_FILE_DATA_TABLE.csv",
//					"RECORDING_FILTER_DATA_TABLE.csv",
//					"RECORDING_INFO_DATA_TABLE.csv",
//					"RECORDING_SPEED_DATA_TABLE.csv",
//					"RECORDING_VOCALIZATION_DATA_TABLE.csv",
//					"SPECIMEN_DATA_TABLE.csv",
//					"SPECIMEN_PREPARATION_DATA_TABLE.csv",
//					"SPECIMEN_TYPE_DATA_TABLE.csv",
//					"TAXON_NAME_DATA_TABLE.csv",
//					"UNVOUCHERED_RECORD_DATA_TABLE.csv",
//					"VOCALIZATION_DATA_TABLE.csv"
			};
			File defsFile = new File("/home/timo/deadzone/Barcelona/Round2/tbldefs.sql");
			//File defsFile = new File("/home/timo/datas/washington/defsdummy.sql");
			//File defsFile = new File("/home/timo/datas/Barcelona/antCmpHab.sql");
			//File defsFile = new File("/home/timo/datas/westernAU/sample1def.sql");

			for (String csv: csvs) {
				String tbl = csv.replace(".csv", "");
				//String tbl = "genbankids";//"agentmergerows";
				String path = "/home/timo/deadzone/Barcelona/Round2/" + csv;//"/home/timo/datas/washington/" + csv;
				List<String> def = bu.createTableDefinitionFromCsv(path,
						',', "utf8", '"', ' ', tbl, true);
				String loader = "LOAD DATA LOCAL INFILE '" + path + "' INTO TABLE `" + tbl + "` CHARACTER SET utf8 FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\\n' IGNORE 1 LINES;";
				def.add(";");
				def.add(loader);
				FileUtils.writeLines(defsFile, def, true);

			}

			//bu.concatWestAUSplitFlds();

			//UtilitaryBase.trimCharVals(con, "specimens", true, true, true);
			//UtilitaryBase.fillInfo(con, "specimens");
			//UtilitaryBase.trimCharVals(con, "loc", true, true, true);
			//UtilitaryBase.fillInfo(con, "loc");
		//regExpress();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
