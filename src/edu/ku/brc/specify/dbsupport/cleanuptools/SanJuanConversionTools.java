/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Vector;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Pair;

/**
 * @author Tadmin
 *
 */
public class SanJuanConversionTools 
{

	protected static void dropFld(Connection conn, String db, String tbl, String fld)
	{
		String sql = "ALTER TABLE `" + db + "`.`" + tbl + "` DROP COLUMN `" + fld + "`"; 
		BasicSQLUtils.update(conn, sql);
	}
	
	protected static Vector<String> getFieldsInTable(Connection conn, String db, String tbl, 
			String dataType)
	{
		String sql = "select COLUMN_NAME from information_schema.columns where TABLE_SCHEMA = '"
			+ db + "' and TABLE_NAME = '" + tbl + "'";
		if (dataType != null)
		{
			sql += " and DATA_TYPE = '" + dataType + "'";
		}
		Vector<Object> fldsInTbl = BasicSQLUtils.querySingleCol(conn, sql);
		Vector<String> result = new Vector<String>();
		for (Object fld : fldsInTbl)
		{
				result.add(fld.toString());
		}
		return result;
		
	}
	
	protected static Vector<String> getBlankFieldsInTable(Connection conn, String db, String tbl)
	{
		Vector<String> result = new Vector<String>();
		for (String fld : getFieldsInTable(conn, db, tbl, null))
		{
			if (!fldContainsData(conn, db, tbl, fld))
			{
				result.add(fld);
			}
		}
		return result;
	}
	
	protected static void globalFindAndReplace(Connection conn, String db, String tbl, String find,
			String replace)
	{
		findAndReplace(conn, db, tbl, find, replace, getFieldsInTable(conn, db, tbl, "varchar"));
	}

	protected static void findAndReplace(Connection conn, String db, String tbl, String find,
			String replace, Vector<String> fields)
	{
		for (String fld : fields)
		{
			String sql = "update `" + db + "`.`" + tbl + "` set `" + fld 
				+ "` = replace(`" + fld + "`, '" + find + "', '" + replace + "');";
			BasicSQLUtils.update(conn, sql);
		}
		
	}

	protected static Vector<Pair<Object, String>> globalTextSearch(Connection conn, String db, String tbl,
			String find, String keyFldName)
	{
		Vector<Pair<Object,String>> result = new Vector<Pair<Object, String>>();
		for (String fld : getFieldsInTable(conn, db, tbl, "varchar"))
		{
			System.out.println("Searching `" + fld + "`");
			String sql = "select `" + keyFldName + "` from `" + db + "`.`" + tbl + "` where `" + fld 
				+ "` like '%" + find + "%'";
			Vector<Object> matches = BasicSQLUtils.querySingleCol(conn, sql);
			for (Object match : matches)
			{
				result.add(new Pair<Object, String>(match, fld));
			}
		}
		return result;
	}
	
	protected static int getNumberOfRecsWithNonNullValsForFld(Connection conn, 
			String db, String tbl, String fld)
	{
		String sql = "select count(*) from `" + db + "`.`" + tbl + "` where `" + fld + "` is not null";
		return BasicSQLUtils.getCountAsInt(conn, sql); 
	}
	
	protected static boolean fldContainsData(Connection conn, 
			String db, String tbl, String fld)
	{
		return getNumberOfRecsWithNonNullValsForFld(conn, db, tbl, fld) != 0; 
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String connStr = "jdbc:mysql://localhost/herbarium2?characterEncoding=UTF-8&autoReconnect=true"; 
		String connStr = "jdbc:mysql://localhost/wisflora?characterEncoding=UTF-8&autoReconnect=true"; 
		try
		{
			Connection conn = DriverManager.getConnection(connStr, "root", "root");
			
			/*
			//Vector<String> blanks = getBlankFieldsInTable(conn, "herbarium2", "herbarium2export");
			Vector<String> blanks = getBlankFieldsInTable(conn, "herbarium2", "herbariumxls");
			for (String fldName : blanks)
			{
				//System.out.println("deleting " + fldName);
				dropFld(conn, "herbarium2", "herbariumxls", fldName);
				System.out.println("deleted " + fldName);
			} */
			
			//globalFindAndReplace(conn, "herbarium2", "herbariumxls", "\\\\", "");
			//globalFindAndReplace(conn, "herbarium2", "herbariumxls", "''", "\\\\''");
			//globalFindAndReplace(conn, "herbarium2", "herbariumxls", "\\\\", "");
			
			
//			Vector<Pair<Object, String>> matches = globalTextSearch(conn, "wisflora", "specimen", "\\r", "Accession");
//			System.out.println(matches.size() + " matches found");
//			for (Pair<Object, String> match : matches)
//			{
//				System.out.println(match.getFirst() + ": " + match.getSecond());
//			}
			
//			Vector<String> flds = new Vector<String>();
//			flds.add("ANNDATE");
//			flds.add("CITY");
//			flds.add("LITCIT");
//			flds.add("PLACE");
//			
//			findAndReplace(conn, "wisflora", "specimen", "\\n", " ", flds);

			Vector<String> flds = new Vector<String>();
			flds.add("ANNDATE");
			flds.add("CITY");
			flds.add("LITCIT");
			flds.add("PLACE");
			
			findAndReplace(conn, "wisflora", "specimen", "\\r", " ", flds);
} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
