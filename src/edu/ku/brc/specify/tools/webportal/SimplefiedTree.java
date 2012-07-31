/**
 * 
 */
package edu.ku.brc.specify.tools.webportal;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Pair;


/**
 * @author timo
 *
 *various tools for creating tree data and tree-specimen links for use in the web portal
 *
 */
public class SimplefiedTree {

	private static String getConnectionStr(String dbName, String server)
	{
		return "jdbc:mysql://" + server + "/" + dbName + "?characterEncoding=UTF-8&autoReconnect=true"; 
	}

	private static String[] taxRanks = {"class", "ordername", "family", "genus", "species", "subspecies"};
	private static String[] geoRanks = {"continent", "country", "state", "county"};
	private static Integer[] taxRankIds = {60, 100, 140, 180, 220, 230};
	private static Integer[] geoRankIds = {100, 200, 300, 400};
	
	private static String getIdFld(String nameFldName) {
		if ("ordername".equalsIgnoreCase(nameFldName)) {
			return "orderid";
		}
		return nameFldName + "id";
	}


	private static int insertIntoTreeNode(String nodeText, int rank, Integer nodeParent, int nodeTreeID)
	{
		//return the key of the inserted node...
		return -1;
	}
	
	private static void insertIntoTreeNodeSpec(Connection con, String sql, int nodeID) throws SQLException
	{
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		Statement iStmt = con.createStatement();
		try
		{
			while (rs.next())
			{
				iStmt.executeUpdate("insert into wptreenodespec(NodeID, SpecID) values(" + nodeID + "," + rs.getInt(1));
			}
		} finally
		{
			iStmt.close();
			rs.close();
			stmt.close();
		}
	}
	
	private static int writeSimpleTreeToJson3(Connection con,
			String dir,
			Integer wpTreeId, List<Pair<String, String>> parentRanks,  
			Integer parentId,
			List<Pair<String, Integer>> childRanks,
			String tree, boolean isHost, String cacheTblName, Integer parentKey) throws SQLException, IOException	
	{
		String where = isHost ? "IsHost" : "NOT IsHost";
		for (Pair<String, String> p : parentRanks) {
			if (where.length() > 0) {
				where += " AND ";
			}
			where += p.getFirst() + " = '" + p.getSecond().replace("'", "''") + "'";
		}
		
		String flds ;
		String c = childRanks.get(0).getFirst();
		Integer rankId = childRanks.get(0).getSecond();
		if (where.length() > 0) {
			where += " AND ";
		} 
		flds = "`" + c + "`, `" + getIdFld(c) + "`"; 
		String where1 = where + "`" + c + "` IS NOT NULL";
		//String tblName = tree.equals("taxon") ? "wpflattax" : "wpflatgeo";
		String sql = "select  distinct " + flds + " from " + cacheTblName + " where " + where1 + " order by 1";
		//if (tree.equals("taxon") && parentRanks.size() > 2) sql += " limit 2";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		try
		{
			List<String> result = new ArrayList<String>();
			int cnt = 0;
			while (rs.next())
			{
				cnt++;
				if (result.size() != 0) {
					result.set(result.size()-1, result.get(result.size()-1) + ",");
				}
				result.add("{");
				result.add("\"text\": \"" + escape4Json(rs.getString(1)) + "\",");
				result.add("\"iconCls\":\"" + tree + "-" + ("ordername".equals(c) ? "order" : c) + "-icon\",");
				result.add("\"rankid\":\"" + rankId + "\",");
				result.add("\"nodeid\": \"" + rs.getString(2) + "\",");
				
				int nodeKey = insertIntoTreeNode(rs.getString(1), rankId, parentKey, wpTreeId);
				String where2 = where + "`" + c + "` = " + BasicSQLUtils.getEscapedSQLStrExpr(rs.getString(1));
				String sql2 = "select " + cacheTblName + "Id" + " from " + cacheTblName + " where " + where2;
				insertIntoTreeNodeSpec(con, sql2, nodeKey);
				
				List<Pair<String,String>> parents = new ArrayList<Pair<String, String>>();
				parents.addAll(parentRanks);
				parents.add(new Pair<String, String>(childRanks.get(0).getFirst(), rs.getString(1)));
				List<Pair<String,Integer>> children = new ArrayList<Pair<String, Integer>>();
				children.addAll(childRanks);
				children.remove(0);
				int childCnt = children.size() > 0 ? 
						writeSimpleTreeToJson3(con, dir, wpTreeId, parents, 
								rs.getInt(2), children, tree, isHost, cacheTblName, nodeKey) 
						: 0;
				if (childCnt == 0) {
					result.add("\"leaf\": true");
				}
				result.add("}");
			}
			if (cnt > 0) {
				result.add(0, "[");
				result.add("]");
			}
			//result.add(0, "{\"response\":{\"docs\":");
			//result.add("}}");
			String fileName = dir + wpTreeId + "-" + parentId + ".json";
			FileUtils.writeLines(new File(fileName), result);
			return result.size();
		} finally
		{
			rs.close();
			stmt.close();
		}
	}

	private static int writeSimpleTreeToJson2(Connection con,
			String dir,
			Integer wpTreeId, List<Pair<String, String>> parentRanks,  
			Integer parentId,
			List<Pair<String, Integer>> childRanks,
			String tree, boolean isHost) throws SQLException, IOException	
	{
		String where = isHost ? "IsHost" : "NOT IsHost";
		for (Pair<String, String> p : parentRanks) {
			if (where.length() > 0) {
				where += " AND ";
			}
			where += p.getFirst() + " = '" + p.getSecond().replace("'", "''") + "'";
		}
		
		String flds ;
		String c = childRanks.get(0).getFirst();
		Integer rankId = childRanks.get(0).getSecond();
		if (where.length() > 0) {
			where += " AND ";
		} 
		flds = c + ", " + getIdFld(c); 
		where += c + " IS NOT NULL";
		String tblName = tree.equals("taxon") ? "wpflattax" : "wpflatgeo";
		String sql = "select  distinct " + flds + " from " + tblName + " where " + where + " order by 1";
		//if (tree.equals("taxon") && parentRanks.size() > 2) sql += " limit 2";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		try
		{
			List<String> result = new ArrayList<String>();
			int cnt = 0;
			while (rs.next())
			{
				cnt++;
				if (result.size() != 0) {
					result.set(result.size()-1, result.get(result.size()-1) + ",");
				}
				result.add("{");
				result.add("\"text\": \"" + escape4Json(rs.getString(1)) + "\",");
				result.add("\"iconCls\":\"" + tree + "-" + ("ordername".equals(c) ? "order" : c) + "-icon\",");
				result.add("\"rankid\":\"" + rankId + "\",");
				result.add("\"nodeid\": \"" + rs.getString(2) + "\",");
				
				List<Pair<String,String>> parents = new ArrayList<Pair<String, String>>();
				parents.addAll(parentRanks);
				parents.add(new Pair<String, String>(childRanks.get(0).getFirst(), rs.getString(1)));
				List<Pair<String,Integer>> children = new ArrayList<Pair<String, Integer>>();
				children.addAll(childRanks);
				children.remove(0);
				int childCnt = children.size() > 0 ? 
						writeSimpleTreeToJson2(con, dir, wpTreeId, parents, 
								rs.getInt(2), children, tree, isHost) 
						: 0;
				if (childCnt == 0) {
					result.add("\"leaf\": true");
				}
				result.add("}");
			}
			if (cnt > 0) {
				result.add(0, "[");
				result.add("]");
			}
			//result.add(0, "{\"response\":{\"docs\":");
			//result.add("}}");
			String fileName = dir + wpTreeId + "-" + parentId + ".json";
			FileUtils.writeLines(new File(fileName), result);
			return result.size();
		} finally
		{
			rs.close();
			stmt.close();
		}
	}

	private static List<String> writeSimpleTreeToJson(Connection con, List<Pair<String, String>> parentRanks,  List<Pair<String, Integer>> childRanks,
			String tree, boolean isHost) throws SQLException	
	{
		String where = isHost ? "IsHost" : "NOT IsHost";
		for (Pair<String, String> p : parentRanks) {
			if (where.length() > 0) {
				where += " AND ";
			}
			where += p.getFirst() + " = '" + p.getSecond().replace("'", "''") + "'";
		}
		
		String flds ;
		String c = childRanks.get(0).getFirst();
		Integer rankId = childRanks.get(0).getSecond();
		if (where.length() > 0) {
			where += " AND ";
		} 
		flds = c + ", " + getIdFld(c); 
		where += c + " IS NOT NULL";
		String tblName = tree.equals("taxon") ? "wpflattax" : "wpflatgeo";
		String sql = "select  distinct " + flds + " from " + tblName + " where " + where + " order by 1";
		//if (tree.equals("taxon") && parentRanks.size() > 2) sql += " limit 2";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		try
		{
			List<String> result = new ArrayList<String>();
			int cnt = 0;
			while (rs.next())
			{
				cnt++;
				if (result.size() != 0) {
					result.set(result.size()-1, result.get(result.size()-1) + ",");
				}
				result.add("{");
				result.add("\"text\": \"" + escape4Json(rs.getString(1)) + "\",");
				result.add("\"iconCls\":\"" + tree + "-" + ("ordername".equals(c) ? "order" : c) + "-icon\",");
				result.add("\"rankid\":\"" + rankId + "\",");
				result.add("\"nodeid\": \"" + rs.getString(2) + "\",");
				
				List<Pair<String,String>> parents = new ArrayList<Pair<String, String>>();
				parents.addAll(parentRanks);
				parents.add(new Pair<String, String>(childRanks.get(0).getFirst(), rs.getString(1)));
				List<Pair<String,Integer>> children = new ArrayList<Pair<String, Integer>>();
				children.addAll(childRanks);
				children.remove(0);
				List<String> childResult = children.size() > 0 ? writeSimpleTreeToJson(con, parents, children, tree, isHost) : null;
				if (childResult != null && childResult.size() > 0) {
					result.add("\"children\": ");
					result.addAll(childResult);
				} else {
					result.add("\"leaf\": true");
				}
				result.add("}");
			}
			if (cnt > 0) {
				result.add(0, "[");
				result.add("]");
			}
			return result;
		} finally
		{
			rs.close();
			stmt.close();
		}
	}

	private static boolean quoteIt(String dataType) {
		return "text".equalsIgnoreCase(dataType);
	}
	
	private static String escape4Json(String str) {
		//escapes quotes,\, /, \r, \n, \b, \f, \t
		String[] badJson = {"\\", "\"", "\r", "\n", "\b", "\f", "\t"};
		String result = str;
		for (String bad : badJson) {
			result = result.replace(bad, "\\" + bad);
		}
		return result;
	}
	
	private static List<String> getTreeNodeSpecsJson(Connection con, String tbl, String rank, String nodeName, List<Pair<String, String>> fldsToRetrieve) throws SQLException {
		List<String> result = new ArrayList<String>();
		Statement stmt = con.createStatement();
		String sql = "select ";
		for (int f = 0; f < fldsToRetrieve.size(); f++) {
			if (f > 0) {
				sql += ", ";
			}
			sql += fldsToRetrieve.get(f).getFirst();
		}
		sql += " from " + tbl + " where " + getIdFld(rank) + " = '" + nodeName.replace("'", "''") + "'" + " order by 1";
		ResultSet rs = stmt.executeQuery(sql);
		try {
			while (rs.next()) {
				String line = "{";
				for (int f = 0; f < fldsToRetrieve.size(); f++) {
					if (rs.getObject(f+1) != null) {
						if (f > 0) {
							line += ", ";
						}
						String delim = quoteIt(fldsToRetrieve.get(f).getSecond()) ? "\"" : "";
						line += "\"" + fldsToRetrieve.get(f).getFirst() + "\": " + delim + escape4Json(rs.getString(f+1)) + delim;
					}
				}
				line += "},";
				result.add(line);
			}
			if (result.size() > 0) {
				String lastLine = result.get(result.size()-1);
				result.set(result.size()-1, lastLine.substring(0, lastLine.length()-1));
			}
//			if (result.size() > 1) {
//				result.add(0, "[");
//				result.add("]");
//			}
		} finally {
			rs.close();
			stmt.close();
		}
		return result;
	}
	
	private static void writeTreeNodeSpecsJson(String dir, String treeType, Connection con, String tbl, String rank, String nodeName, List<Pair<String, String>> fldsToRetrieve, int pageSize) throws Exception {
		List<String> specs = getTreeNodeSpecsJson(con, tbl, rank, nodeName, fldsToRetrieve);
		if (specs.size() > 0) {
			int page = 1;
			List<String> pageSpecs = new ArrayList<String>(); 
			for (String spec : specs) {
				if (pageSpecs.size() == pageSize) {
					String lastLine = pageSpecs.get(pageSpecs.size()-1);
					if (lastLine.endsWith(",")) {
						pageSpecs.set(pageSpecs.size()-1, lastLine.substring(0, lastLine.length()-1));
					}
					pageSpecs.add(0, "{\"response\":{\"numFound\":" + specs.size() + ",\"start\":" + page*pageSize + ",\"docs\":[");
					pageSpecs.add("]}}");
					//String fileName = dir + File.separatorChar + treeType + "." + rank + "." + nodeName.replace(" ", "_") + ".json" + "." + page;
					String fileName = dir + File.separatorChar + treeType + "." + nodeName.replace(" ", "_") + ".json" + "." + page;
					FileUtils.writeLines(new File(fileName), pageSpecs);
					page++;
					pageSpecs.clear();
				} 
				pageSpecs.add(spec);
			}
			if (pageSpecs.size() > 0) {
				String lastLine = pageSpecs.get(pageSpecs.size()-1);
				if (lastLine.endsWith(",")) {
					pageSpecs.set(pageSpecs.size()-1, lastLine.substring(0, lastLine.length()-1));
				}
				pageSpecs.add(0, "{\"response\":{\"numFound\":" + specs.size() + ",\"start\":" + page*pageSize + ",\"docs\":[");
				pageSpecs.add("]}}");
				//String fileName = dir + File.separatorChar + treeType + "." + rank + "." + nodeName.replace(" ", "_") + ".json" + "." + page;
				String fileName = dir + File.separatorChar + treeType + "." +nodeName.replace(" ", "_") + ".json" + "." + page;
				FileUtils.writeLines(new File(fileName), pageSpecs);
			}
//			specs.add(0, "[");
//			specs.add("]");
//			String fileName = dir + File.separatorChar + treeType + "." + rank + "." + nodeName.replace(" ", "_") + ".json";
//			FileUtils.writeLines(new File(fileName), specs);
		}
	}
	
	private static void writeSimpleTreeSpecsForRank(String dir, String treeType, Connection con, String tbl, String rank, List<Pair<String, String>> fldsToRetrieve, int pageSize) throws Exception {
		Statement stmt = con.createStatement();
		String sql = "select distinct " + getIdFld(rank) + " from " + tbl + " where " + getIdFld(rank) + " is not null order by " + rank;
		ResultSet rs = stmt.executeQuery(sql);
		try {
			while (rs.next()) {
				writeTreeNodeSpecsJson(dir, treeType, con, tbl, rank, rs.getString(1), fldsToRetrieve, pageSize);
			}
		} finally {
			rs.close();
			stmt.close();
		}
	}
	
	private static void writeSimpleTreeSpecs(String dir, String treeType, Connection con, String tbl, List<String> ranks, List<Pair<String, String>> fldsToRetrieve, int pageSize) throws Exception {
		for (String rank : ranks) {
			writeSimpleTreeSpecsForRank(dir, treeType, con, tbl, rank, fldsToRetrieve, pageSize);
		}
	}
	
	private static void writeSimpleTaxTreeSpecs() throws Exception {
		Connection c = DriverManager.getConnection(getConnectionStr("kufish", "localhost"),
				"root", "root");
		List<String> ranks = new ArrayList<String>();
		for (String r : taxRanks) {
			ranks.add(r);
		}
		List<Pair<String, String>> fldsToRetrieve = new ArrayList<Pair<String, String>>();
		fldsToRetrieve.add(new Pair<String,String>("catalognumber", "text"));
		fldsToRetrieve.add(new Pair<String,String>("family", "text"));
		fldsToRetrieve.add(new Pair<String,String>("year", "text"));
		fldsToRetrieve.add(new Pair<String,String>("country", "text"));
		fldsToRetrieve.add(new Pair<String,String>("StartDateCollected", "text"));
		fldsToRetrieve.add(new Pair<String,String>("StationFieldNumber", "text"));
		fldsToRetrieve.add(new Pair<String,String>("taxon", "text"));
		fldsToRetrieve.add(new Pair<String,String>("Latitude1", "text"));
		fldsToRetrieve.add(new Pair<String,String>("Longitude1", "text"));
		fldsToRetrieve.add(new Pair<String,String>("LocalityName", "text"));
		fldsToRetrieve.add(new Pair<String,String>("geography", "text"));
		fldsToRetrieve.add(new Pair<String,String>("PrimaryCollector", "text"));
		fldsToRetrieve.add(new Pair<String,String>("image", "text"));
		writeSimpleTreeSpecs("/media/Terror/ConversionsAndFixes/ku/kufish/json/", "taxon", c, "flattaxspecs", ranks, fldsToRetrieve, 50);
	}

	private static void writeSimpleGeoTreeSpecs() throws Exception {
		Connection c = DriverManager.getConnection(getConnectionStr("kufish", "localhost"),
				"root", "root");
		List<String> ranks = new ArrayList<String>();
		for (String r : geoRanks) {
			ranks.add(r);
		}
		List<Pair<String, String>> fldsToRetrieve = new ArrayList<Pair<String, String>>();
		fldsToRetrieve.add(new Pair<String,String>("catalognumber", "text"));
		fldsToRetrieve.add(new Pair<String,String>("family", "text"));
		fldsToRetrieve.add(new Pair<String,String>("year", "text"));
		fldsToRetrieve.add(new Pair<String,String>("country", "text"));
		fldsToRetrieve.add(new Pair<String,String>("StartDateCollected", "text"));
		fldsToRetrieve.add(new Pair<String,String>("StationFieldNumber", "text"));
		fldsToRetrieve.add(new Pair<String,String>("taxon", "text"));
		fldsToRetrieve.add(new Pair<String,String>("Latitude1", "text"));
		fldsToRetrieve.add(new Pair<String,String>("Longitude1", "text"));
		fldsToRetrieve.add(new Pair<String,String>("LocalityName", "text"));
		fldsToRetrieve.add(new Pair<String,String>("geography", "text"));
		fldsToRetrieve.add(new Pair<String,String>("PrimaryCollector", "text"));
		fldsToRetrieve.add(new Pair<String,String>("image", "text"));
		writeSimpleTreeSpecs("/media/Terror/ConversionsAndFixes/ku/kufish/json/", "geo", c, "flattaxspecs", ranks, fldsToRetrieve, 50);
	}

	private static void writeSimpleTreeToJsonFile(Connection c,
			String treeType, Integer wpTreeId, String saveInDir,
			List<Pair<String, Integer>> ranks, boolean isHost) throws Exception {
		List<Pair<String, String>> parentRanks = new ArrayList<Pair<String, String>>();
		List<String> json = writeSimpleTreeToJson(c, parentRanks, ranks,
				treeType, isHost);
		String fileName = saveInDir + treeType + "-" + wpTreeId + ".json";
		FileUtils.writeLines(new File(fileName), json);

	}

	private static void writeSimpleTreeToJsonFiles(Connection c,
			String treeType, Integer wpTreeId, String saveInDir,
			List<Pair<String, Integer>> ranks, boolean isHost) throws Exception {
		List<Pair<String, String>> parentRanks = new ArrayList<Pair<String, String>>();
		writeSimpleTreeToJson2(c, saveInDir, wpTreeId, parentRanks, null, ranks,
				treeType, isHost);
	}


	private static void writeSimpleTaxTreeToJsonWrapper() throws Exception {
		Connection c = DriverManager.getConnection(
				getConnectionStr("kufish", "localhost"), "root", "root");
		List<Pair<String, Integer>> ranks = new ArrayList<Pair<String, Integer>>();
		for (int r = 0; r < taxRanks.length; r++) {
			ranks.add(new Pair<String, Integer>(taxRanks[r], taxRankIds[r]));
		}
		Integer wpTreeId = 1;
		boolean isHost = false;
		writeSimpleTreeToJsonFile(c, "taxon", wpTreeId, "/home/timo/", ranks,
				isHost);
	}

	private static void writeSimpleTaxTreeToJsonWrapper2() throws Exception {
		Connection c = DriverManager.getConnection(
				getConnectionStr("bigento", "localhost"), "root", "root");
		List<Pair<String, Integer>> ranks = new ArrayList<Pair<String, Integer>>();
		for (int r = 0; r < taxRanks.length; r++) {
			ranks.add(new Pair<String, Integer>(taxRanks[r], taxRankIds[r]));
		}
		Integer wpTreeId = 4;
		boolean isHost = true;
		writeSimpleTreeToJsonFiles(c, "taxon", wpTreeId, "/home/timo/devweb/portalinprogress/resources/data/json/", ranks,
				isHost);
	}

	private static void writeSimpleGeoTreeToJsonWrapper2() throws Exception {
		Connection c = DriverManager.getConnection(
				getConnectionStr("bigento", "localhost"), "root", "root");
		List<Pair<String, Integer>> ranks = new ArrayList<Pair<String, Integer>>();
		for (int r = 0; r < geoRanks.length; r++) {
			ranks.add(new Pair<String, Integer>(geoRanks[r], geoRankIds[r]));
		}
		Integer wpTreeId = 5;
		boolean isHost = false;
		writeSimpleTreeToJsonFiles(c, "geography", wpTreeId, "/home/timo/devweb/portalinprogress/resources/data/json/",
				ranks, isHost);
	}

	// private static void writeSimpleGeoTreeToJson(Connection c, Integer
	// wpTreeId, String saveInDir,
	// List<Pair<String, Integer>> ranks) throws Exception {
	// List<Pair<String, String>> parentRanks = new
	// ArrayList<Pair<String,String>>();
	// List<String> json = writeSimpleTreeToJson(c, parentRanks, ranks,
	// "geography");
	// String fileName = saveInDir + "geography-" + wpTreeId + ".json";
	// FileUtils.writeLines(new File(fileName), json);
	//
	// }

	private static void writeSimpleGeoTreeToJsonWrapper() throws Exception {
		Connection c = DriverManager.getConnection(
				getConnectionStr("kufish", "localhost"), "root", "root");
		List<Pair<String, Integer>> ranks = new ArrayList<Pair<String, Integer>>();
		for (int r = 0; r < geoRanks.length; r++) {
			ranks.add(new Pair<String, Integer>(geoRanks[r], geoRankIds[r]));
		}
		Integer wpTreeId = 2;
		boolean isHost = false;
		writeSimpleTreeToJsonFile(c, "geography", wpTreeId, "/home/timo/",
				ranks, isHost);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try
		{
			writeSimpleTaxTreeToJsonWrapper2();
			//writeSimpleGeoTreeToJsonWrapper2();
			
			//writeSimpleTaxTreeSpecs();
			//writeSimpleGeoTreeSpecs();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	

	}

}
