/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author timo
 *
 */
public class KUUPlant extends KUUtilitary {

	/**
	 * @param con
	 */
	public KUUPlant(Connection con) {
		super(con);
		
	}

	/**
	 * @param genus
	 * @return
	 */
	protected Integer getTaxonIDForName(String name, int rankID) {
		Integer result = null;
		String sql = "SELECT TaxonID FROM taxon WHERE Name='" + name + "' AND RankID=" + rankID;
		List<Object> ids = BasicSQLUtils.querySingleCol(con, sql);
		if (ids.size() == 1) {
			result = (Integer)ids.get(0);
		} else {
			logThis(ids.size() + " matches for " + name + ", " + rankID);
		}
		return result;
	}
	
	/**
	 * @param family
	 * @param genus
	 */
	protected void moveGenus(String family, String genus) throws Exception {
		Integer genusID = getTaxonIDForName(genus, 180);
		Integer familyID = getTaxonIDForName(family, 140);
		if (genusID != null && familyID != null) {
			String sql = "UPDATE taxon SET parentid=" + familyID + " WHERE TaxonID=" + genusID;
			logThisSql(sql);
			//if (BasicSQLUtils.update(con, sql) != 1) {
			//	logThis("sql error moving " + genus + " to " + family);
			//}
		}	
	}
	
	/**
	 * @param moveFileName
	 */
	protected void moveGenera(String moveFileName) throws Exception {
		List<String> lines = FileUtils.readLines(new File(moveFileName), "utf8");
		for (String line : lines) {
			String[] genFam = line.split(",");
			if (genFam.length == 2) {
				moveGenus(genFam[0].trim(), genFam[1].trim());
			} else {
				logThis("skipped invalid line: " + line);
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/kuplant6?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			KUUPlant uplant = new KUUPlant(con);
			uplant.moveGenera("D:/data/KU/VascularPlant/LostGeneraForSynsInUseWithFamilies.csv");
			uplant.writeLog("D:/data/KU/VascularPlant/genmovelog.txt");
			uplant.writeSqlLog("D:/data/KU/VascularPlant/genmove.sql");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
