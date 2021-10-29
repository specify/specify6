/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.tools.export.Sampler;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class KUUtento extends KUUtilitary {

	/**
	 * @param con
	 */
	public KUUtento(Connection con) {
		super(con);
	}

	private Map<String, Pair<Integer,Integer>> readUserIdMap(String fileName) throws Exception {
		Map<String, Pair<Integer,Integer>> result = new HashMap<String, Pair<Integer,Integer>>();
		File f = new File(fileName);
		List<String> lines = FileUtils.readLines(f);
		int l = 0;
		while (l < lines.size()) {
			String line = lines.get(l++).trim();
			if (line.startsWith("<string>")) {
				String name = line.replace("<string>", "");
				name = name.replace("</string>","");
				line = lines.get(l++).trim();
				String idStr = line.replace("<int>", "");
				idStr = idStr.replace("</int>", "");
				result.put(name, new Pair<Integer,Integer>(Integer.valueOf(idStr),null));
			}
		}
		return result;
	}

	private void fixKuEntoModifiedByAgents(Connection con5) throws Exception {
		Map<String, Pair<Integer,Integer>> userIdMap = readUserIdMap("D:/data/KU/ento/kuento_agent_mappings.xml");
//		for (Map.Entry<String, Integer> e : userIdMap.entrySet()) {
//			System.out.println(e.getKey() + " - " + e.getValue());
//		}
		for (Map.Entry<String, Pair<Integer,Integer>> e : userIdMap.entrySet()) {
			System.out.println(e.getKey() + " - " + e.getValue());
			String sql = "select LastName, FirstName from agent where agentid=" + e.getValue().getFirst();
			Object[] row = BasicSQLUtils.getRow(con5, sql);
			String ln = row[0] == null ? "LastName is null" : "LastName = '" + BasicSQLUtils.escapeStringLiterals((String)row[0], "'") + "'";
			String fn = row[1] == null ? "FirstName is null" : "FirstName = '" + BasicSQLUtils.escapeStringLiterals((String)row[1], "'") + "'";
			sql = "select agentid from agent where " + ln + " AND " + fn + " ORDER BY AgentID desc"; //This makes picking
			                                                                                                        //first Jennifer Thomas OK
			List<Object> id6 = BasicSQLUtils.querySingleCol(con, sql);
			if (id6.size() > 0) {
				if (id6.size() > 1) {
					System.out.println("picking 1st of multiple sp6 agents for '" + sql + "'");
				}
				e.getValue().setSecond((Integer)id6.get(0));
			}
		}
		int modUps = 0;
		int creUps = 0;
		int noSp6s = 0;
		Set<String> agentsWithoutIds = new HashSet<String>();
		Statement stmt5 = con5.createStatement();
		Statement stmt6 = con.createStatement();
		List<Object> cns = BasicSQLUtils.querySingleCol(con5, 
				"select catalognumber from collectionobjectcatalog where subnumber = 0 and catalogseriesid = 0");
		String modByUp = "UPDATE collectionobject SET ModifiedByAgentID=%d WHERE CollectionObjectID=%d";
		String creByUp = "UPDATE collectionobject SET CreatedByAgentID=%d WHERE CollectionObjectID=%d";
		String sql5 =  "SELECT DATEPART(year, coc.TimestampModified), DATEPART(month, coc.TimestampModified), DATEPART(day,coc.TimestampModified),"
				+ "coc.LastEditedBy FROM collectionobjectcatalog coc "
				+ "INNER JOIN collectionobject co ON co.collectionobjectid = coc.collectionobjectcatalogid WHERE coc.SubNumber = 0 AND " +
				"catalogseriesid = 0 AND catalognumber = %d";
		String sql6 = "SELECT year(TimestampModified), month(TimestampModified), day(TimestampModified), "
				+ "CreatedByAgentID, ModifiedByAgentID, CollectionObjectID FROM collectionobject WHERE " +
				"CollectionID = 4 AND catalognumber='%s'";

		try {
			for (Object cnObj : cns) {
				int cn = ((Double)cnObj).intValue();
				//System.out.println(cn);
				ResultSet row5 = stmt5.executeQuery(String.format(sql5, cn));
				if (row5.next()) {
					ResultSet row6 = stmt6.executeQuery(String.format(sql6, StringUtils.leftPad(String.valueOf(cn), 9, "0")));
					if (row6.next()) {
						Pair<Integer, Integer> ids = userIdMap.get(row5.getObject(4));
						if (ids != null) {
							Integer modBy5 = ids.getSecond();
							Integer modBy6 = row6.getInt(5);
							if (0 == UtilitaryBase.compareTimestampParts(row5.getInt(1), row5.getInt(2), row5.getInt(3), 
									row6.getInt(1), row6.getInt(2), row6.getInt(3))) {
								if (!modBy5.equals(modBy6)) {
									//System.out.println("updating it modifiedby");
									logThisSql(String.format(modByUp, ids.getSecond(), row6.getInt(6)));
									modUps++;
								}
							} 
							Integer creBy6 = row6.getInt(4);
							if (!modBy5.equals(creBy6)) {
								//System.out.println("updating createdby");
								creUps++;
								logThisSql(String.format(creByUp, ids.getSecond(), row6.getInt(6)));
							}
						}	else if (row5.getObject(4) != null) {
							if (!agentsWithoutIds.contains(row5.getString(4))) {
								System.out.println("no id mapping for " + row5.getString(4));
								agentsWithoutIds.add(row5.getString(4));
							}
						}
					} else {
						//System.out.println("Hey! Couldn't find sp6 record for " + cn);
						noSp6s++;
					}
					row6.close();
				}
				row5.close();
			}
		} finally {
			stmt5.close();
			stmt6.close();
		}
		System.out.println("ModifiedBys updated: " + modUps + ". CreatedBys updated: " + creUps + ". MissingIds: " + agentsWithoutIds.size() 
				+ ". Missing Sp6 recs: " + noSp6s);
	}

	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/kuento?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");			
			Connection con5 = Sampler.getConnection("sqlserver", "ENTOSPSMALL", "129.237.201.205\\SQLEXPRESS2008", "sa", "hyla606");
			KUUtento utento = new KUUtento(con);
			utento.fixKuEntoModifiedByAgents(con5);
			utento.writeSqlLog("D:/data/KU/ento/modcrebyupdate.sql");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
