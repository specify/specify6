/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.GeoRefConverter.GeoRefFormat;

/**
 * @author timo
 *
 */
public class KUUBirdy extends KUUtilitary {

	protected UTax utax;
	protected String voucherDbName;
	protected String tissueDbName;
	
	/**
	 * @param con
	 */
	public KUUBirdy(Connection con, String voucherDbName, String tissueDbname) {
		super(con);
		Integer unplacedFamilyID = 39537;			
		utax = new UTax(con, 1, unplacedFamilyID, 1);
		this.voucherDbName = voucherDbName;
		this.tissueDbName = tissueDbname;
	}

	private void connectLocToCe(Integer sp6LocalityId, Integer accessLocalityId) throws Exception {
		String sql = "select distinct collectingeventid from "
				+ voucherDbName + ".master m inner join " + voucherDbName + ".loc l on "
				+ "m.latitude = l.latitude and "
				+ "m.longitude = l.longitude and "
				+ "m.`latlong source` = l.`latlong source` and "
				+ "m.altitude = l.altitude and "
				+ "m.altitudeunits = l.altitudeunits and "
				+ "m.altitudesource = l.altitudesource and "
				+ "m.`specific locality` = l.`specific locality` and "
				+ "m.county = l.county and "
				+ "m.state = l.state and "
				+ "m.country = l.country and "
				+ "m.decimallatitude = l.decimallatitude and "
				+ "m.decimallongitude = l.decimallongitude and "
				+ "m.geodeticdatum = l.geodeticdatum and "
				+ "m.coordinateuncertaintyinmeters = l.coordinateuncertaintyinmeters and "
				+ "m.VerbatimCoordinateSystem = l.VerbatimCoordinateSystem and "
				+ "m.GeoreferenceProtocol = l.GeoreferenceProtocol and "
				+ "m.GeoreferenceRemarks = l.GeoreferenceRemarks and "
				+ "m.GeoreferenceSources = l.GeoreferenceSources and "
				+ "m.GeoreferencedDate = l.GeoreferencedDate and "
				+ "m.GeoreferencedBy = l.GeoreferencedBy and "
				+ "m.GeoreferenceVerificationStatus = l.GeoreferenceVerificationStatus and "
				+ "m.NoGeorefBecause = l.NoGeorefBecause "
				+ "inner join collectionobject co on co.catalognumber = m.number "
				+ "where idloc =  " + accessLocalityId;
		List<Object> ceIDs = BasicSQLUtils.querySingleCol(con, sql);
		for (Object ceIdObj : ceIDs) {
			String upsql = "update collectingevent set localityid = " + sp6LocalityId
					+ " where collectingeventid = " + ceIdObj;
		
			if (BasicSQLUtils.update(con, upsql) != 1) {
				logThis("error updating: " + upsql);
			}
			
			System.out.print(".");
		}
		System.out.println();
	}

	/*
	 * Fix originallatlongunit for geocoords that have already been created from lat1/long1Text fields
	 * but are missing OriginalLatLongUnits
	 */
	protected static void convertLLTextsToDecimal(Connection conn) {
		String sql = "select distinct lat1text, long1text from locality where latitude1 is null and longitude1 is null"
				+ " and Lat1Text is not null and Long1Text is not null";
		List<Object[]> geocs = BasicSQLUtils.query(conn, sql);
		GeoRefConverter g = new GeoRefConverter();
		List<String> errRecs = new ArrayList<String>();
		String upSql = "update locality set OriginalLatLongUnit = %d, SrcLatLongUnit = %d, Latitude1=%s, Longitude1=%s "
				+ "where Lat1Text='%s' and Long1Text='%s' "
				+ "and latitude1 is null and longitude1 is null";
		for (Object[] geoc : geocs) {
			LatLonConverter.FORMAT latF = g.getLatLonFormat((String)geoc[0]);
			LatLonConverter.FORMAT lngF = g.getLatLonFormat((String)geoc[1]);
			int originalLatLongUnit = latF.ordinal();
			try {
				String dLat1 = g.convert(geoc[0].toString(), GeoRefFormat.D_PLUS_MINUS.name());
				String dLng1 = g.convert(geoc[1].toString(), GeoRefFormat.D_PLUS_MINUS.name());
				if (latF == lngF && dLat1 != null && dLng1 != null) {
					String upper = String.format(upSql, originalLatLongUnit, originalLatLongUnit, dLat1, dLng1,
							BasicSQLUtils.escapeStringLiterals((String)geoc[0]), 
							BasicSQLUtils.escapeStringLiterals((String)geoc[1]));
					System.out.println(upper);
					int up = BasicSQLUtils.update(conn, upper);
					if (up < 0) {
						System.out.println("error updating: " + geoc[0] + " / " + geoc[1]);
						errRecs.add(geoc[0] + " / " + geoc[1]);
					}
				} else {
					System.out.println("error converting:" + geoc[0] + ", " + geoc[1]);
					errRecs.add(geoc[0] + " / " + geoc[1]);
				}
			} catch (Exception ex) {
				System.out.println("error converting: " + geoc[0] + ", " + geoc[1] + ": " + ex.getMessage());
				errRecs.add(geoc[0] + " / " + geoc[1]);
			}
		}
		System.out.println(geocs.size() + " geocoords, " + errRecs.size() + " errors.");
		for (String eRec : errRecs) {
			System.out.println(eRec);
		}
	}

	/**
	 * @throws Exception
	 */
	protected void connectLocsAndCes() throws Exception {
		buildStatement();
		String locSql = "select LocalityID, ShortName from locality order by 1";
		ResultSet locs = stmt.executeQuery(locSql);
		try {
			int processed = 0;
			while (locs.next()) {
				connectLocToCe(locs.getInt(1), Integer.valueOf(locs.getString(2)));
				System.out.print(++processed);
			}
			writeLog("D:/data/KU/birds/live/ce2loc.txt");
		} finally {
			locs.close();
			stmt.close();
		}
		
	}
	
	/**
	 * @param parts
	 * @param collectingEventId
	 * @throws Exception
	 */
	protected void updateCollectedDate(List<UnDateComponent> parts, Integer collectingEventId) throws Exception {
		UnYear year = null;
		UnMonth month = null;
		UnDay day = null;
		for (UnDateComponent part : parts) {
			if (part instanceof UnYear) {
				year = UnYear.class.cast(part);
			} else if (part instanceof UnMonth) {
				month = UnMonth.class.cast(part);
			} else if (part instanceof UnDay) {
				day = UnDay.class.cast(part);
			} else {
				throw new Exception("Unsupported date part: " + part.getClass().getSimpleName());
			}
		}
		if (year != null && month != null && day != null) {
			if (UnDateComponent.isValidSpDate(year, month, day)) {
				Integer prec = UnDateComponent.getSpDatePrecision(year, month, day);
				if (prec != null) {
					String dateStr = year.getIntVal().toString() + "-";
					if (prec == 3) {
						dateStr += "1-1";
					} else  {
						dateStr += month.getIntVal().toString() + "-";
						if (prec == 2) {
							dateStr += "1";
						} else {
							dateStr += day.getIntVal();
						}
					}
					String sql = "update collectingevent set StartDate = date('" + dateStr + "'), StartDatePrecision = " + prec 
						+ " where collectingeventid =" + collectingEventId;
					if (BasicSQLUtils.update(con, sql) != 1) {
						logThis("update failed: " + sql);
					}
				}
			}
		} else {
			throw new Exception("missing date part(s)");
		}
	}
	
	/**
	 * @throws Exception
	 */
	protected void dealWithCollectedDates_1() throws Exception {
//		String sql = "select Day, Month, Year, ce.collectingeventid from " + voucherDbName + ".master m inner join collectionobject co on co.catalognumber = m.number " +
//				" inner join collectingevent ce on ce.collectingeventid = co.collectingeventid " +
//				" where  ce.StartDate is null and (`Early date` = `Late date` or (`Early date` is null and `Late date` is null)) order by 1,2,3";
//		buildStatement();
//		ResultSet rs = stmt.executeQuery(sql);
//		KuBirdDater d = new KuBirdDater();
//		try {
//			while (rs.next()) {
//				List<Pair<String,Object>> rowVals = new ArrayList<Pair<String,Object>>();
//				rowVals.add(new Pair<String,Object>("Day", rs.getString(1)));
//				rowVals.add(new Pair<String,Object>("Month", rs.getString(2)));
//				rowVals.add(new Pair<String,Object>("Year", rs.getString(3)));
//				List<UnDateComponent> parts = d.getParts(rowVals);
//				System.out.print(rs.getString(1) + " - " + rs.getString(2) + " - " + rs.getString(3)
//						+ " => ");
//				for (UnDateComponent part : parts) {
//					System.out.print(part.getClass().getSimpleName() + ": " + part.getText() + " - ");
//				}
//				if (UnDateComponent.isValidSpDate((UnYear)parts.get(2), (UnMonth)parts.get(1),
//						(UnDay)parts.get(0))) {
//					System.out.print("Valid " + UnDateComponent.getSpDatePrecision((UnYear)parts.get(2), (UnMonth)parts.get(1),
//						(UnDay)parts.get(0)));
//					updateCollectedDate(parts, rs.getInt(4));
//				} else {
//					System.out.println("BAD");
//				}
//				System.out.println();
//			}
//		} finally {
//			rs.close();
//			stmt.close();
//			this.writeLog("D:/data/KU/birds/live/startdate.txt");
//		}
		throw new Exception("not implemented");
	}
	
	/**
	 * @param rs
	 * @param startNum
	 * @param catNumLen
	 * @throws Exception
	 */
	protected void fillCatNums(ResultSet rs, int startNum, int catNumLen) throws Exception {
		int nextNum = startNum;
		java.sql.Statement upStmt = con.createStatement();
		try {
			while (rs.next()) {
				String sql = "UPDATE collectionobject SET catalognumber=lpad('" + nextNum++ + "', " + catNumLen + ", '0') "
						+ "WHERE CollectionObjectID=" + rs.getInt(1);
				if (upStmt.executeUpdate(sql) != 1) {
					logThis("Unable to update: " + sql);
				}
			}
		} finally {
			upStmt.close();
		}
	}
	
	/**
	 * @param coId
	 * @param taxon
	 * @throws Exception
	 */
	protected void addDetermination(Integer coId, String taxon, java.sql.Statement upStmt) throws Exception {
		Integer taxonId = utax.getTaxonID(taxon);
		if (taxonId == null) {
			logThis("Null taxonID for " + taxon + " for CO " + coId);
		}
		String sql = "INSERT INTO determination (TimestampCreated, Version, CreatedByAgentID, CollectionMemberID, IsCurrent, CollectionObjectID, TaxonID, PreferredTaxonID, Text1) "
				+ "VALUES(now(), 0, 1, 4, true," + coId + "," + taxonId + "," + taxonId + ",'" + taxon.replace("'", "''") + "')";
		if (upStmt.executeUpdate(sql) != 1) {
			logThis("Unable to update: " + sql);
		}
		
	}
	
	/**
	 * @throws Exception
	 */
	protected void addDeterminationsForUnvoucheredCOs() throws Exception {
		String sql = "SELECT CollectionObjectID, uv.taxon from collectionobject co inner join " + tissueDbName + ".unvoucheredtissue uv"
				+ " on uv.TissueNumber = co.Number1 WHERE co.ObjectCondition='no' AND uv.taxon IS NOT NULL";
		ResultSet rs = stmt.executeQuery(sql);
		java.sql.Statement upStmt = con.createStatement();
		try {
			while (rs.next()) {
				addDetermination(rs.getInt(1), rs.getString(2), upStmt);
			}
		} finally {
			upStmt.close();
		}
	}
	
	/**
	 * @throws Exception
	 */
	protected void assignCatNumsAndAddDeterminationsForUnvoucheredSpecs() throws Exception {
		buildStatement();
		try {
			addDeterminationsForUnvoucheredCOs();
			fillCatNums(stmt.executeQuery("SELECT CollectionObjectID FROM collectionobject WHERE ObjectCondition='no' ORDER BY AltCatalogNumber"), 123604, 9);
		} finally {
			stmt.close();
		}
		writeLog("D:/data/KU/birds/live/CatNumAndDetLog.txt");
	}
	
	/**
	 * @throws Exception
	 */
	protected void updateGeographyAndLocalityForUnvoucheredSpecs() throws Exception {
		String sql = "select distinct ce.verbatimlocality, ce.localityid from collectingevent ce "
				+ "inner join collectionobject co on co.collectingeventid = ce.collectingeventid "
				+ "where co.ObjectCondition='no' and verbatimLocality is not null "
				+ "order by 1";
		List<Object[]> locs = BasicSQLUtils.query(con, sql);
		
		List<Pair<String,String>> replacements = new ArrayList<Pair<String,String>>();
		//replacements.add(new Pair<String,String>(";", ":"));
		List<String> splitters = new ArrayList<String>();
		splitters.add(":");
		splitters.add(";");
		splitters.add(",");
		LocalityStringParser lp = new LocalityStringParser(con, 1, replacements, splitters);
		
		buildStatement();
		try {
			for (Object[] loc : locs) {
				updateGeographyAndLocality(loc[0].toString(), (Integer)loc[1], lp);
			}
			writeLog("D:/data/KU/birds/live/unvoucheredLocGeoLog.txt");
		} finally {
			stmt.close();
		}
	}
	
	/**
	 * @param verbatimLocality
	 * @param localityID
	 * @param lp
	 * @throws Exception
	 */
	protected void updateGeographyAndLocality(String verbatimLocality, Integer localityID,
			LocalityStringParser lp) throws Exception {
		List<List<Pair<Pair<Integer, String>, String>>> geos = lp.parseLocalityName(verbatimLocality);
		Integer geoId = lp.getBestGeographyID(geos);
		String locName = lp.getLocNameForBestGeo(geos);
		String sql = "UPDATE locality SET GeographyID=" + geoId + " WHERE LocalityID=" + localityID;
		if (stmt.executeUpdate(sql) != 1) {
			logThis("unable to update: " + sql);
		}
		sql = "UPDATE locality SET LocalityName='" + (StringUtils.isNotBlank(locName) ? locName.replace("'", "''") : "N/A") + "' WHERE LocalityID=" + localityID;
		if (stmt.executeUpdate(sql) != 1) {
			logThis("unable to update: " + sql);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/kubirds?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			//String connStr = "jdbc:mysql://bimysql.nhm.ku.edu/KUBirds?characterEncoding=UTF-8&autoReconnect=true";
			//Connection con = getConnection(connStr, "specifymaster", "2Kick@$$");
			//KUUBirdy ubirdy = new KUUBirdy(con, "thebirds", "thetissues");
			//ubirdy.connectLocsAndCes();
			//ubirdy.dealWithCollectedDates_1();
			//ubirdy.assignCatNumsAndAddDeterminationsForUnvoucheredSpecs();
			//ubirdy.updateGeographyAndLocalityForUnvoucheredSpecs();
			convertLLTextsToDecimal(con);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
