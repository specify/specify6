/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hibernate.tool.hbm2x.StringUtils;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 */
public class ExportToMySQLDB
{
	
	/**
	 * @param fld
	 * @return mysql field type declaration.
	 */
	protected static String getFieldTypeDef(DBFieldInfo fld)
	{
		if (fld == null)
		{
			//assume the column is formatted and/or aggregated
			return "varchar(300)"; //a really long string
		}
		
		Class<?> dataType = fld.getDataClass();
		if (dataType.equals(String.class))
		{
			return "varchar(" + fld.getLength() + ")";
		}
		if (dataType.equals(Integer.class) || dataType.equals(Byte.class))
		{
			return "int";
		}
		if (dataType.equals(Calendar.class))
		{
			return "date";
		}
		if (dataType.equals(BigDecimal.class))
		{
			//XXX too much for everything but longitude, but is it really a problem?
			return "decimal(13,10)"; 
		}
		if (dataType.equals(Boolean.class))
		{
			return "bit(1)";
		}
		if (dataType.equals(Double.class))
		{
			return "double";
		}
		if (dataType.equals(Timestamp.class))
		{
			return "datetime";
		}
		return null;
	}
	
	/**
	 * @param column
	 * @return a 'safe' mysql field name for column.
	 */
	protected static String getFieldName(ERTICaptionInfo column)
	{
		//XXX TESTING!!!!!!!
		return /*"_" + */column.getColLabel();
	}
	
	/**
	 * @param name
	 * @return name with invalid characters removed or subbstituted
	 */
	public static String fixNameForMySQL(String name)
	{
        //XXX probably lots of other possibilities to fix
		return name.trim().replaceAll(" ", "_");
	}
	/**
	 * @param column
	 * @return a mysql field declaration for column.
	 */
	protected static String getFieldDef(ERTICaptionInfo column)
	{
		return "`" + getFieldName(column) +  "` " + getFieldTypeDef(column.getFieldInfo());
	}
	
	/**
	 * @param tblName
	 * @return a name for the 'id' column for tblName
	 */
	protected static String getIdFieldName(String tblName)
	{
		return tblName + "Id";
	}
	
	/**
	 * @param toConnection
	 * @param columns
	 * @param tblName
	 * @param idColumn
	 * @throws Exception
	 * 
	 * Creates a table with the supplied columns. Adds id field if idColumn is true.
	 */
	public static void createTable(Connection toConnection, List<ERTICaptionInfoQB> columns, String tblName, boolean idColumn) throws Exception
	{
        Statement stmt = toConnection.createStatement();
		StringBuilder sql = new StringBuilder();
		sql.append("create table " + tblName + "(");
		if (idColumn)
		{
			sql.append(getIdFieldName(tblName) + " int");
		}
		boolean commafy = idColumn;
		for (ERTICaptionInfo col : columns)
		{
			if (commafy)
			{
				sql.append(", ");
			}
			commafy = true;
			sql.append(getFieldDef(col));
		}
		sql.append(")");
		stmt.execute(sql.toString());
		stmt.close();
	}
	
	/**
	 * @param connection
	 * @param tblName
	 * @return true if a table named tblName exists.
	 * @throws Exception
	 * 	 
	 */
	public static boolean tableExists(Connection connection, String tblName) throws Exception
	{
		Statement stmt = connection.createStatement();
		try
		{
			stmt.execute("select * from " + tblName);
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
		finally
		{
			stmt.close();
		}
	}
	
	/**
	 * @param toConnection
	 * @param columns
	 * @param rows
	 * @param tblName
	 * @param listeners
	 * @param idColumnPresent
	 * @param overwrite - create a new table for the data.
	 * @param update - rows represent only updates since last export
	 * @throws Exception
	 * 
	 * Exports rows to a table named tblName in toConnection's db.
	 * NOTE: toConnection is closed by this method.
	 */
	public static void exportToTable(Connection toConnection, List<ERTICaptionInfoQB> columns,
			QBDataSource rows, String originalTblName, List<QBDataSourceListenerIFace> listeners,
			boolean idColumnPresent, boolean overwrite, boolean update) throws Exception
	{
		for (QBDataSourceListenerIFace listener : listeners)
	    {
	    	listener.loading();
	    	listener.rowCount(rows.size());
	    }
	    boolean newTable = false;
	    String tblName = fixNameForMySQL(originalTblName);
	    if (overwrite || !tableExists(toConnection, tblName))
	    {
	    	createTable(toConnection, columns, tblName, idColumnPresent);
	    	newTable = true;
	    }
	    
		Statement stmt = toConnection.createStatement();
	    try
		{
			for (QBDataSourceListenerIFace listener : listeners)
			{
				listener.filling();
			}
			int rowNum = 0;
			while (rows.getNext())
			{
				for (QBDataSourceListenerIFace listener : listeners)
				{
					listener.currentRow(rowNum++);
				}

				if (update && !newTable)
				{
					// XXX Not totally sure this is safe.
					// If duplicate ids are allowed/possible, can export query
					// return one of several objects with same id?
					// If co 123 was repeated three times due to preps or
					// something in original export, can a lastExport -sensitive
					// re-export return only 1 of the preps (which would result in
					// the loss of the other two after the delete below)
					// or will the query always get all of the dups???
					// Maybe safer to prevent duplicates.

					// XXX and WHAT ABOUT DELETES anyway? Need lots of extra
					// work to detect when records have been deleted and
					// remove them from the exported cache...
					stmt.execute("delete from " + tblName + " where "
							+ getIdFieldName(tblName) + " = "
							+ rows.getFieldValue(0));
				}
				stmt.execute(getInsertSql(rows, tblName));
			}
			for (QBDataSourceListenerIFace listener : listeners)
			{
				listener.done(rowNum);
			}
		}
	    finally
	    {
	    	stmt.close();
	    	toConnection.close(); 
	    }
	}
	
	/**
	 * @param columns
	 * @param rows
	 * @param tblName
	 * @param listeners
	 * @param idColumnPresent
	 * @param overwrite
	 * @param update
	 * @throws Exception
	 * 
	 * Exports rows to a table named tblName in the default database.
	 */
	public static void exportToTable(List<ERTICaptionInfoQB> columns, QBDataSource rows, 
			String tblName, List<QBDataSourceListenerIFace> listeners,
			boolean idColumnPresent, boolean overwrite, boolean update) throws Exception
	{
		exportToTable(DBConnection.getInstance().createConnection(), columns, rows, tblName, listeners, idColumnPresent, overwrite, update);
	}
	
	/**
	 * @param row
	 * @param tblName
	 * @return a sql insert statement for row.
	 */
	protected static String getInsertSql(QBDataSource row, String tblName)
	{
		StringBuilder result = new StringBuilder();
		result.append("insert into " + tblName + " values(");
		for (int r=0; r < row.getFieldCount(); r++)
		{
			System.out.println("geting insert value for: " + row.getFieldValue(r));
			if (r > 0)
			{
				result.append(", ");
			}
			String val = BasicSQLUtils.getStrValue(row.getFieldValue(r));
			result.append(val);			
		}
		result.append(")");
		System.out.println(result.toString());
		return result.toString();
	}
	
	protected static boolean exportRowsToTabDelimitedText(File file,
			List<String> headers, String tableName)
	{
		try
		{
			Connection conn = DBConnection.getInstance().createConnection();
			Statement stmt = null;
			try
			{
				//XXX testing - probably no good for large tables.
				List<String> lines = new LinkedList<String>();
				stmt = conn.createStatement();
				ResultSet rows = stmt.executeQuery("select * from " + tableName);
				String headerLine = "";
				if (headers != null)
				{
					for (String header : headers)
					{
						if (headerLine.length() > 0)
						{
							headerLine += "\t";
						}
						headerLine += header;
					}
				}
				else
				{
					for (int c = 1; c <= rows.getMetaData().getColumnCount(); c++)
					{
						if (headerLine.length() > 0)
						{
							headerLine += "\t";
						}
						headerLine += rows.getMetaData().getColumnName(c);
					}
				}
				if (headerLine.length() > 0)
				{
					//System.out.println(headerLine);				
					//FileUtils.writeStringToFile(file, headerLine);
					lines.add(headerLine);
				}
				while (rows.next())
				{
					//System.out.println(getTabDelimLine(rows));
					//FileUtils.writeStringToFile(file, getTabDelimLine(rows));
					lines.add(getTabDelimLine(rows));
				}
				FileUtils.writeLines(file, lines);
				return true;
			} finally
			{
				if (stmt != null)
				{
					stmt.close();
				}
				conn.close();
			}
		} catch (Exception ex)
		{
			UIRegistry.displayErrorDlg(ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
			return false;
		}
	}
	
	protected static String getTabDelimLine(ResultSet rows) throws SQLException
	{
		StringBuilder result = new StringBuilder();
		for (int c = 1; c <= rows.getMetaData().getColumnCount(); c++)
		{
			if (c > 1)
			{
				result.append("\t");
			}
			String val = rows.getString(c);
			if (val != null)
			{
				int type = rows.getMetaData().getColumnType(c);
				// remove tabs and line returns
				// If link to origianl specify db model field was available
				// would only
				// need to do this for 'remarks' fields.
				if (type == java.sql.Types.LONGNVARCHAR
						|| type == java.sql.Types.LONGVARCHAR
						|| type == java.sql.Types.VARCHAR
						|| type == java.sql.Types.CHAR
						|| type == java.sql.Types.NCHAR)// hopefully don't need
														// to worry about blobs.
				{
					val.replace("\t", " ");
					val.replace("\n", " ");
				}
				result.append(val);
			}
		}
		return result.toString();
	}
}
