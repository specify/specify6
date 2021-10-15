/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author timo
 *
 */
public class UFlaPaleoBotUtils extends UtilitaryBase {

	
	/**
	 * @param con
	 */
	public UFlaPaleoBotUtils(Connection con) {
		super(con);
	}

	/**
	 * @param localityNumber
	 * @param localityID
	 * @param date
	 * @param coll
	 * @throws Exception
	 */
	protected void createCollectingEvent(String localityNumber, Integer localityID, String date, String coll, Boolean isSingleItem) 
		throws Exception {
		String sql = "INSERT INTO collectingevent(TimestampCreated, TimestampModified, Version, CreatedByAgentID,"
				+ "DisciplineID, LocalityID, " + (isSingleItem ? "VerbatimDate" : "VerbatimLocality") + ", Text1, Text2) VALUES(now(), now(), 0, 1, 3, "
				+ localityID + ",'" + (date != null ? date : "N/A").replace("'", "''") + "', '" + (coll != null ? coll : "N/A").replace("'", "''") + "', '"
				+ localityNumber + "');";
		if (BasicSQLUtils.update(con, sql) != 1) {
			throw new Exception("insert failed: " + sql);
		}
	}
	
	/**
	 * creates separate ces for 'Collected Date'/'Collectors' item pairs
	 * 
	 */
	protected void createCollectingEvents(Object[] locRow) throws Exception {
		String localityNumber = String.class.cast(locRow[0]);
		Integer localityID = Integer.class.cast(locRow[1]);
		String dateList = String.class.cast(locRow[2]);
		String collList = String.class.cast(locRow[3]);
		createCollectingEvent(localityNumber, localityID, dateList, collList, false);
		if (dateList != null || collList != null) {
			String[] dates = dateList == null ? new String[0] : dateList.split(";");
			String[] colls = collList == null ? new String[0] : collList.split(";");
			if (dates.length == colls.length) {
				for (int ce = 0; ce < dates.length; ce++) {
					createCollectingEvent(localityNumber, localityID, dates[ce].trim(), colls[ce].trim(), true);
				}
			} else if (dates.length <= 1 && colls.length >= 1) {
				String date = dates.length == 0 ? null : dates[0];
				for (int ce = 0; ce < colls.length; ce++) {
					createCollectingEvent(localityNumber, localityID, date, colls[ce].trim(), true);
				}
			} else if (dates.length >= 1 && colls.length <= 1) {
				String coll = colls.length == 0 ? null : colls[0];
				for (int ce = 0; ce < dates.length; ce++) {
					createCollectingEvent(localityNumber, localityID, dates[ce].trim(), coll, true);
				}
			}
		}
	}
	
	protected void createCollectingEventsForLocs(String sql) throws Exception {
		List<Object[]> rows = BasicSQLUtils.query(con, sql);
		for (Object[] row : rows) {
			createCollectingEvents(row);
		}
	}
	
	/**
	 * creates separate ces for 'Collected Date'/'Collectors' item pairs
	 */
	protected void createCollectingEventsForLocs() throws Exception {
		String sql = "select locality_number, localityid, `Date Collected`, Collectors "
				+ "from flaleobot.tbl_pb_locality pbl inner join locality l on l.shortname = pbl.locality_number ";
		createCollectingEventsForLocs(sql);
		//locs with equal number of items in Date Collected and Collectors
		//only for locs already added to sp6 db.
//		String sql = "select locality_number, localityid, `Date Collected`, Collectors, "
//				+ "length(`Date Collected`) - length(replace(`Date Collected`, ';', '')) + 1 NumDates, "
//				+ "length(`Collectors`) - length(replace(`Collectors`, ';', '')) + 1 NumColls "
//				+ "from flaleobot.tbl_pb_locality pbl inner join locality l on l.shortname = pbl.locality_number "
//				+ "where length(`Date Collected`) - length(replace(`Date Collected`, ';', '')) + 1 = "
//				+ "length(`Collectors`) - length(replace(`Collectors`, ';', '')) + 1";
//		createCollectingEventsForLocs(sql);
		//where date collected and collectors don't 'match'.
//		sql = "select locality_number, localityid, `Date Collected`, Collectors, "
//				+ "length(`Date Collected`) - length(replace(`Date Collected`, ';', '')) + 1 NumDates, "
//				+ "length(`Collectors`) - length(replace(`Collectors`, ';', '')) + 1 NumColls "
//				+ "from flaleobot.tbl_pb_locality pbl inner join locality l on l.shortname = pbl.locality_number "
//				+ "where (length(`Date Collected`) - length(replace(`Date Collected`, ';', '')) + 1 != "
//				+ "length(`Collectors`) - length(replace(`Collectors`, ';', '')) + 1) OR `Date Collected` IS NULL OR Collectors IS NULL";
//		createCollectingEventsForLocs(sql);
	}
	
	protected void connectCoToCe(Integer coID, Integer ceID) throws Exception {
		String sql = "UPDATE collectionobject SET CollectingEventID=" + ceID + " WHERE CollectionObjectID=" + coID;
		if (BasicSQLUtils.update(con, sql) != 1) {
			throw new Exception("update failed: " + sql);
		}
	}
	
	protected boolean connectCoToCe(Integer coId, String ceSql) throws Exception {
		List<Object[]> ces = BasicSQLUtils.query(con, ceSql);
		if (ces.size() == 1) {
			connectCoToCe(coId, Integer.class.cast(ces.get(0)[0]));
			return true;
		}
		return false;
	}
	
	protected boolean connectCoToCe(Object[] row) throws Exception {
		boolean result = false;
		Integer coId = Integer.class.cast(row[0]);
		if (row[2] != null) {
			String ceSql = "select CollectingEventID from collectingevent where LocalityID=" + row[1].toString()
				+ " and Text1 = '" + row[2].toString().replace("'", "''") + "'";
			result = connectCoToCe(coId, ceSql);
		} 
		if (!result) {
			String ceSql = "select CollectingEventID from collectingevent where LocalityID=" + row[1].toString() + " AND VerbatimLocality IS NOT NULL";
			result = connectCoToCe(coId, ceSql);
		}
		return result;
	}
	
	protected void connectCosToCes() throws Exception {
		String sql = "select CollectionObjectID, LocalityID, Collector "
				+ "from collectionobject co inner join flaleobot.tbl_pb_catalog pbc "
				+ "on pbc.catalog_number = co.CatalogNumber inner join locality l " 
				+ "on l.ShortName=pbc.locality_number";
		List<Object[]> rows = BasicSQLUtils.query(con, sql);
		for (Object[] row : rows) {
			if (!connectCoToCe(row)) {
				logThis("Unable to find ce for " + row[0].toString());
			}
		}
		this.writeLog("D:/data/florida/paleobotany/co2ce.log");
	}
	
	/**
	 * Where ce.Text1 != tbl_pb_catalog.collector, copy collector to ce, creating new ce if necessary.
	 */
	protected void copySpecCollectorsToCEs() throws Exception {
		buildStatement();
		String sql = "select ce.collectingeventid from collectingevent ce "
				+ "inner join collectionobject co on co.CollectingEventID = ce.CollectingEventID "
				+ "inner join flaleob.tbl_pb_catalog m on m.catalog_number = co.fieldnumber where " 
				+ "ifnull(m.collector,'') != ifnull(ce.text1,'')";
		List<Object[]> ces = BasicSQLUtils.query(con, sql);
		for (Object[] ce : ces) {
			Integer ceId = Integer.class.cast(ce[0]);
			copySpecCollectorsToCe(ceId);
		}
		stmt.close();
		this.writeLog("D:/data/florida/paleobotany/SpecCollector2CeCollector.log");
	}
	
	protected void copySpecCollectorsToCe(Integer ceId) throws Exception {
		String sql = "SELECT DISTINCT pbc.Collector FROM collectionobject co INNER JOIN flaleob.tbl_pb_catalog pbc "
				+ "ON pbc.catalog_number = co.FieldNumber WHERE co.CollectingEventID=" + ceId;
		List<Object[]> colls = BasicSQLUtils.query(con, sql);
		for (Object[] coll : colls) {
			String collector = String.class.cast(coll[0]);
			copySpecCollectorToCes(ceId, collector);
		}
	}
	
	protected void copySpecCollectorToCes(Integer ceId, String collector) throws Exception {
		Integer newCeId = copyCe(ceId, collector);
		String sql = "UPDATE collectionobject co INNER JOIN flaleob.tbl_pb_catalog pbc "
				+ "ON pbc.catalog_number = co.FieldNumber "
				+ "SET co.CollectingEventID=" + newCeId
				+ " WHERE co.CollectingEventID=" + ceId; 
		if (collector != null) {
			sql += " AND pbc.Collector='" + collector.replaceAll("'", "''") + "'";
		} else {
			sql += " AND pbc.Collector IS NULL";
		}
		BasicSQLUtils.update(con, sql);
	}
	
	protected Integer copyCe(Integer ceId, String newCollector) throws Exception {
		String sql = "INSERT INTO collectingevent(Version, TimestampCreated, TimestampModified, CreatedByAgentID, DisciplineID,  LocalityID, Text1) VALUES(" 
				+ "0, now(), now(), 1, 3, (select LocalityID from collectingevent ce where ce.CollectingEventID=" + ceId + "),";
		if (newCollector == null) {
			sql += "NULL)";
		} else {
			sql += "'" + newCollector.replaceAll("'", "''") + "')";
		}
		stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
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
	 * @author timo
	 *
	 */
	public class UFlaPaleoBotDaterPreprocessor extends DaterPreProcessor {

		/* (non-Javadoc)
		 * @see edu.ku.brc.specify.dbsupport.cleanuptools.DaterExpressionPreProcessor#preprocess(java.lang.String)
		 */
		@Override
		public String preprocess(String text) {
			String result = text;
			if (result.endsWith(".")) {
				result = result.substring(0, result.length() - 1);
			}
			if (result.startsWith("ca. ")) {
				result = result.replace("ca. ", "");
			}
			if (result.startsWith("ca.")) {
				result = result.replace("ca.", "");
			}
			if (result.startsWith("Donated in Feb. ")) {
				result = result.replace("Donated in Feb. ", "");
			}
			if (result.endsWith(" (donation)")) {
				result = result.replace(" (donation)", "");
			}
			if (result.endsWith(" (donation to FLMNH)")) {
				result = result.replace(" (donation to FLMNH)", "");
			}
			return result;
		}
		
	}
	
	protected String getCEDateUpdateSql(String text, Undateable ud) {
		String result = "";
		if (ud != null && ud.isValid()) {
			result = "UPDATE collectingevent SET StartDate='" + ud.getSQLDateExpression() + "', "
					+ "StartDatePrecision=" + ud.getPrecision();
			if (ud instanceof UndateableRange) {
				Undateable endDate = ((UndateableRange)ud).getEnd();
				result += ", EndDate='" + endDate.getSQLDateExpression() + "', EndDatePrecision="
						+ endDate.getPrecision();
			}
			result += " WHERE VerbatimDate='" + text.replaceAll("'", "''") + "'";
		}
		return result;
	}
	
	/**
	 * @return
	 */
	protected Dater createDater(Dater.MultipleMatchType matchType) {
		List<DaterExpression> es = new ArrayList<DaterExpression>();
		es.add(new MonthNum2Year4());
		//es.add(new DayMonthNum2Year4());
		es.add(new MonthNum2DayYear4());
		es.add(new MonthDayYear4());
		es.add(new MonthYear4());
		es.add(new Year4Range());
		es.add(new Year4());
		es.add(new MonthNum2DayYear2());
		es.add(new MonthDayRangeYear4());
		es.add(new Decade());
		es.add(new MonthNum2DayRangeYear4());
		return new Dater(es, matchType, new UFlaPaleoBotDaterPreprocessor());		
	}
	
	/**
	 * 
	 */
	protected void dateCheck() {
		Dater dater = createDater(Dater.MultipleMatchType.MULTIPLE);
		String sql = "select distinct verbatimdate from collectingevent where startdate is null and verbatimdate is not null"
				+ " and verbatimdate not in('?', '(?)', 'N/A', 'unknown') order by 1";
		List<Object[]> verbs = BasicSQLUtils.query(con, sql);
		int matched = 0;
		int unmatched = 0;
		int invalidated = 0;
		int multiMatched = 0;
		for (Object[] verb : verbs) {
			try {
				List<Undateable> uds = dater.getDate(verb[0].toString());
				//System.out.print(verb[0].toString() + " -- ");
				matched++;
				if (uds.size() > 1) {
					multiMatched++;
				}
				for (Undateable ud : uds) {
					if (!ud.isValid()) {
						invalidated++;
						System.out.println("INVALID - " + verb[0] + " - " + ud);
					}
					if (ud.getDatedBy() instanceof Decade /*||ud.getDatedBy() instanceof MonthNum2DayRangeYear4*/) {
						System.out.println(verb[0].toString() + " -- " + ud);
					}
					System.out.println(getCEDateUpdateSql(verb[0].toString(), ud));
				}
			} catch (DaterException dex) {
				unmatched++;
				System.out.println(dex.getMessage());
			}
		}
		System.out.println("Matched: " + matched + ", Unmatched: " + unmatched + ", Invalidated: " + invalidated + ", Multiply Matched: " + multiMatched);
	}
	
	protected void logCollectedDatesUpdateStmts() throws Exception {
		Dater dater = createDater(Dater.MultipleMatchType.NONE);
		String sql = "select distinct verbatimdate from collectingevent where startdate is null and verbatimdate is not null"
				+ " and verbatimdate not in('?', '(?)', 'N/A', 'unknown') order by 1";
		List<Object[]> verbs = BasicSQLUtils.query(con, sql);
		for (Object[] verb : verbs) {
			try {
				List<Undateable> uds = dater.getDate(verb[0].toString());
				for (Undateable ud : uds) {
					if (!ud.isValid()) {
						System.out.println("INVALID - " + verb[0] + " - " + ud);
						logThis("INVALID - " + verb[0] + " - " + ud);
					}
					logThisSql(getCEDateUpdateSql(verb[0].toString(), ud) + ";");
				}
			} catch (DaterException dex) {
				logThis(dex.getMessage());
			}
		}
		writeLog("D:/data/florida/paleobotany/ceDateLog.txt");
		writeSqlLog("D:/data/florida/paleobotany/ceDates.sql");
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/flaleob6?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			UFlaPaleoBotUtils flaleo = new UFlaPaleoBotUtils(con);
			//flaleo.createCollectingEventsForLocs();
			//flaleo.connectCosToCes();
			//flaleo.copySpecCollectorsToCEs();
			flaleo.dateCheck();
			flaleo.logCollectedDatesUpdateStmts();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
