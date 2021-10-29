/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.csvreader.CsvReader;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class UFlaPaleoUtils extends UtilitaryBase {
	 
	/**
	 * @param con
	 */
	public UFlaPaleoUtils(Connection con) {
		super(con);
	}
	
	
	/**
	 * @param range
	 * @return
	 */
	protected List<String> getRangeStartEnd(Object[] range) {
		Integer id = (Integer)range[0];
		String gRank = range[1].toString(), gName = range[2].toString(), pRank = range[3].toString(), pName = range[4].toString();
		String prompt = gName + " (" + gRank + ")  parent = " + pName + " (" + pRank + ");";
		List<Pair<String, String>> vals = new ArrayList<Pair<String,String>>();
		vals.add(new Pair<String,String>("Start Rank", gRank));
		vals.add(new Pair<String,String>("Start Name", gName.substring(0, gName.indexOf("-"))));
		vals.add(new Pair<String,String>("End Rank", gRank));
		vals.add(new Pair<String,String>("End Name", gName.substring(gName.indexOf("-")+1)));
		if (readValues(prompt, vals)) {
			List<String> result = new ArrayList<String>();
			result.add(id.toString());
			result.add(gRank);
			result.add(gName);
			for (Pair<String,String> v : vals) {
				result.add(v.getSecond());
			}
			return result;
		} else {
			return null;
		}
	}
	
	/**
	 * @param lines
	 * @param outFileName
	 * @throws Exception
	 */
	public void exportAsTabDelim(List<List<String>> lines, String outFileName) throws Exception {
		List<String> outLines = new ArrayList<String>(lines.size());
		for (List<String> line : lines) {
			String outLine = "";
			for (String v : line) {
				outLine += ("".equals(outLine) ? "" : "\t") + v;
			}
			outLines.add(outLine);
		}
		FileUtils.writeLines(new File(outFileName),  "utf8", outLines);
	}
	
	/**
	 * @param outFileName
	 * @throws Exception
	 * 
	 * select geologictime records with names that suggest ranges, parses the ranges,
	 * and outputs the results to a tab-delim text file.
	 */
	public void getFlaPaleoRanges(String outFileName) throws Exception {
		String sql = "select g.geologictimeperiodid, gr.name, g.name, pr.rankid, p.name from geologictimeperiod g inner join geologictimeperiod p"
				+ " on p.geologictimeperiodid = g.parentid inner join geologictimeperiodtreedefitem gr " 
				+ "on gr.geologictimeperiodtreedefitemid = g.geologictimeperiodtreedefitemid "
				+ "inner join geologictimeperiodtreedefitem pr on pr.geologictimeperiodtreedefitemid = p.geologictimeperiodtreedefitemid "
				+ "where g.name like '%-%' order by 1,2";
		List<Object[]> ranges = BasicSQLUtils.query(con, sql);
		List<List<String>> map1 = new ArrayList<List<String>>();
		for (Object[] range : ranges) {
			List<String> startEnd = getRangeStartEnd(range);
			if (startEnd != null) {
				map1.add(startEnd);
			} else {
				throw new Exception("Cancelled!");
			}
			//if (map1.size() > 2) break;
		}
		exportAsTabDelim(map1, outFileName);
	}

	/**
	 * @param rank
	 * @param name
	 * @return
	 */
	protected Integer[] getGeoTimeId(String rank, String name) {
		String sql = "select geologictimeperiodid from geologictimeperiod gtp inner join "
				+ "geologictimeperiodtreedefitem di on di.geologictimeperiodtreedefitemid = "
				+ "gtp.geologictimeperiodtreedefitemid where di.name='"
				+ BasicSQLUtils.escapeStringLiterals(rank) + "' and gtp.name='"
				+ BasicSQLUtils.escapeStringLiterals(name) + "'";
		List<Object> ids = BasicSQLUtils.querySingleCol(con, sql);
		Integer[] result = new Integer[ids.size()];
		for (int i = 0; i < ids.size(); i++) {
			result[i] = (Integer)ids.get(i);
		}
		return result;
	}
	
	/**
	 * @param rangeId
	 * @param endPtRank
	 * @param endPtName
	 * @return GeologicTimePeriod record ID for new GeologicTimePeriod record withendPoint rank and name.
	 * Name may contain "|" to indicate parentage. Assuming 1 parent (no grand parents).
     * If parent is not given, then parent of rangeId is used for the new record's parent.
     * Assumes one treedef.
	 * @throws Exception
	 */
	protected Integer createGeoTime(Integer rangeId, String endPtRank, String endPtName) throws Exception {
		String[] n = endPtName.split("\\|");
		String parentSql = (n.length == 1) 
				? "select parentid from geologictimeperiod where geologictimeperiodid=" + rangeId
				: "select geologictimeperiodid from geologictimeperiod gtp where geologictimeperiodtreedefitemid="
					+ "(select pd.geologictimeperiodtreedefitemid from geologictimeperiodtreedefitem pd where pd.RankId="
					+ "(select d.RankID from geologictimeperiod d where d.Name='" + BasicSQLUtils.escapeStringLiterals(endPtRank)
					+ "') + 100) and name='" + BasicSQLUtils.escapeStringLiterals(endPtName) + "'";
		String treeDefSql = "select geologictimeperiodtreedefid from geologictimeperiodtreedefitem where name='"
				+ BasicSQLUtils.escapeStringLiterals(endPtRank) + "'";
		String treeDefItemSql = "select geologictimeperiodtreedefitemid from geologictimeperiodtreedefitem where name='"
				+ BasicSQLUtils.escapeStringLiterals(endPtRank) + "'";
		String rankSql = "select RankID from geologictimeperiodtreedefitem where name='"
				+ BasicSQLUtils.escapeStringLiterals(endPtRank) + "'";
		String createdBySql = "select CreatedByAgentID from geologictimeperiod where geologictimeperiodid=" + rangeId;
		
		Integer createdById = BasicSQLUtils.querySingleObj(con, createdBySql);
		Integer parentId = BasicSQLUtils.querySingleObj(con, parentSql);
		if (parentId == null|| createdById == null) {
			throw new Exception("Unabled to find parent or createdbyagent for " + endPtRank + ", " + endPtName);
		}
		String sql = "insert into geologictimeperiod(Version, TimestampCreated, CreatedByAgentID, GeologicTimePeriodTreeDefID, "
				+ "GeologicTimePeriodTreeDefItemID, IsAccepted, IsBioStrat, RankID, ParentID, Name) values(0, now(),"
				+ createdById + ","
				+ "(" + treeDefSql + "),"
				+ "(" + treeDefItemSql + "), true, false, "
				+ "(" + rankSql + "),"
				+ parentId +  ","
				+ "'" + BasicSQLUtils.escapeStringLiterals(endPtName) + "')";
		stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		ResultSet key = stmt.getGeneratedKeys();
		if (!key.next()) {
			key.close();
			throw new Exception("Insert failed: " + sql);
		}
		Integer result = key.getInt(1);
		logThisSql(sql);
		key.close();	
		return result;
	}
	
	/**
	 * @param rangeId
	 * @param endPtRank
	 * @param endPtName
	 * @return GeologicTimePeriod record ID for endPoint rank and name, creating a record if necessary.
	 * Name may contain "|" to indicate parentage.
	 * Assuming 1 parent (no grand parents)
	 * 
	 * @throws Exception
	 */
	protected Integer findGeoTimeRangeEndPt(Integer rangeId, String endPtRank, String endPtName) throws Exception {
		String[] n = endPtName.split("\\|");
		String name = n.length == 1 ? n[0] : n[1];
		String sql = "select gtp.geologictimeperiodid from geologictimeperiod gtp inner join "
				+ "geologictimeperiodtreedefitem di on di.geologictimeperiodtreedefitemid = "
				+ "gtp.geologictimeperiodtreedefitemid inner join geologictimeperiod p on "
				+ "p.geologictimeperiodid = gtp.parentid where di.name ='"
				+ BasicSQLUtils.escapeStringLiterals(endPtRank) + "' and gtp.name ='"
				+ BasicSQLUtils.escapeStringLiterals(name) + "'";
		if (n.length > 1) {
			sql += " and p.name='" + BasicSQLUtils.escapeStringLiterals(n[0]) + "'";
		}
		int tries = 0;
		Integer result = null;
		while (tries < 2 && result == null) {
			List<Object> match = BasicSQLUtils.querySingleCol(con, sql);
			if (match.size() > 1) {
				if (tries == 0) {
					sql += " order by p.name limit 1";
				} else {
					throw new Exception("More than match for " + endPtRank + ", " + endPtName);
				}
			} else {
				if (match.size() == 0) {
					result = createGeoTime(rangeId, endPtRank, endPtName);
				} else {
					if (tries > 0) {
						logThis("More than match for " + endPtRank + ", " + endPtName + " in gtp range with id " + rangeId);
					}
					result = (Integer)match.get(0);
				}
			}
			tries++;
		}
		return result;
	}
	
	/**
	 * @param id
	 * @param fromRank
	 * @param fromName
	 * @param toRank
	 * @param toName
	 * @return {id, fromId, ToId}
	 * 
	 * looks up matches for ranks and names, if not found, creates records for them.
	 * names may contain "|" to indicate parentage.
	 */
	protected Integer[] getRangeFromToIds(Integer id, String fromRank, String fromName,
			String toRank, String toName) throws Exception {
		return new Integer[]{id, findGeoTimeRangeEndPt(id, fromRank, fromName), 
				findGeoTimeRangeEndPt(id, toRank, toName)};
	}
	/**
	 * @param inFileName
	 * @throws Exception
	 * 
	 * Taking a file produced by getFlaPaleoRanges, this method produces a list of range geologictime 
	 * records with their IDs and the IDs of the starts and ends of the ranges. Start and End records are
	 * created if necessary.
	 */
	public List<Integer[]> getFlaPaleoRangeIds(String inFileName) throws Exception {
        List<Integer[]> result = new ArrayList<Integer[]>();
		CsvReader csv = new CsvReader(new FileInputStream(inFileName), 
        		'\t', Charset.forName("utf8"));
    	int rank=1, name=2, fromRank=3, fromName=4, toRank=5, toName=6;
		buildStatement();
        while (csv.readRecord()) {
    		String[] line = csv.getValues();
    		Integer[] ids = getGeoTimeId(line[rank], line[name]);
    		if (ids != null && ids.length > 0) {
    			for (Integer id : ids) {
    				result.add(getRangeFromToIds(id, line[fromRank], line[fromName], line[toRank],
    						line[toName]));
    			}
    		} else {
    			throw new Exception("Unable to find " + line[rank] + ", " + line[name]);
    		}
        }
        stmt.close();
        return result;
	}

	/**
	 * @param ranges
	 * @param logFileName
	 * @throws Exception
	 * 
	 * updates paleocontext records associated with ranges so that ChronosStratId points to the range start
	 * and ChronosStratEndId points to the range end.
	 */
	public void processPaleoContextsForRanges(List<Integer[]> ranges) throws Exception {
		String pcSel = "select count(*) from paleocontext where chronosstratid=%d";
		String pcUp = "update paleocontext set chronosstratid=%d, chronosstratendid=%d where chronosstratid=%d";
		for (Integer[] range : ranges) {
			Long pcs = BasicSQLUtils.querySingleObj(con, String.format(pcSel, range[0]));
			if (pcs > 0) {
				String sql = String.format(pcUp, range[1], range[2], range[0]);
				int updated = BasicSQLUtils.update(con, sql);
				if (updated != pcs) {
					throw new Exception("unable to update paleocontexts for range " + range[0]);
				}
				logThisSql(sql);
			} else {
				logThis("no paleocontexts found for range " + range[0]);
			}
		}
	}
	
	public void deleteGtpRanges(List<Integer[]> ranges) throws Exception {
		String sql = "select geologictimeperiodid from geologictimeperiod where geologictimeperiodid in(%s) order by RankID desc";
		String idList = "";
		for (Integer[] range : ranges) {
			if (!"".equals(idList)) {
				idList += ",";
			}
			idList += range[0];
		}
		sql = String.format(sql, idList);
		List<Object> idsToDel = BasicSQLUtils.querySingleCol(con, sql);
		String gtpDel = "delete from geologictimeperiod where geologictimeperiodid=%d";
		for (Object id : idsToDel) {
			String desql = String.format(gtpDel, (Integer)id);
			if (BasicSQLUtils.update(con, desql) != 1) {
				throw new Exception("unable to delete geologictimeperiod " + id);
			} else {
				logThisSql(desql);
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/6_20141007c?characterEncoding=UTF-8&autoReconnect=true";
			UFlaPaleoUtils pu = new UFlaPaleoUtils(UFlaUtils.getConnection(connStr));
			//pu.getFlaPaleoRanges("D:/data/florida/ivp/live/paleoranges.txt");
			List<Integer[]> ranges = pu.getFlaPaleoRangeIds("D:/data/florida/ivp/live/paleoranges.txt");
			for (Integer[] range : ranges) {
				System.out.println(range[0] + ":" + range[1] + " - " + range[2]);
			}
			pu.processPaleoContextsForRanges(ranges);
			pu.deleteGtpRanges(ranges);
			pu.writeLog("D:/data/florida/ivp/live/paleorangeLog_REDO.txt");
			pu.writeSqlLog("D:/data/florida/ivp/live/paleorangeSql_REDO.sql");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
