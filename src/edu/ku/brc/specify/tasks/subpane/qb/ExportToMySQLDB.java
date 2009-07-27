/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
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
		return name.trim().replaceAll(" ", "_").replaceAll("\\.", "_");
	}
	
	/**
	 * @param name
	 * @return lower-cased name with invalid characters removed or substituted
	 */
	public static String fixTblNameForMySQL(String name)
	{
		return fixNameForMySQL(name).toLowerCase();
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
	public static long exportToTable(Connection toConnection, List<ERTICaptionInfoQB> columns,
			QBDataSource rows, String originalTblName, List<QBDataSourceListenerIFace> listeners,
			boolean idColumnPresent, boolean overwrite, boolean update, int baseTableId) throws Exception
	{
		if (rows.hasResultSize())
		{
			for (QBDataSourceListenerIFace listener : listeners)
			{
				listener.loaded();
				listener.rowCount(rows.size());
			}
		}
	    boolean newTable = false;
	    String tblName = fixTblNameForMySQL(originalTblName);
	    if (overwrite || !tableExists(toConnection, tblName))
	    {
	    	createTable(toConnection, columns, tblName, idColumnPresent);
	    	newTable = true;
	    }
	    
	    if (update)
	    {
	    	DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(baseTableId);
	    	
	    	deleteDeletedRecs(toConnection, tblName, tblName + "Id", tbl.getName(), tbl.getIdColumnName(), AppContextMgr.getInstance().getClassObject(Collection.class).getId());
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
				System.out.println("exporting " + rowNum);
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
		    return rowNum;
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
	public static long exportToTable(List<ERTICaptionInfoQB> columns, QBDataSource rows, 
			String tblName, List<QBDataSourceListenerIFace> listeners,
			boolean idColumnPresent, boolean overwrite, boolean update, int baseTableId) throws Exception
	{
		return exportToTable(DBConnection.getInstance().createConnection(), columns, rows, tblName, listeners, idColumnPresent, overwrite, update, baseTableId);
	}
	
	/**
	 * @param connection
	 * @param tblName
	 * @param keyFld
	 * @param spTblName
	 * @param spKeyFld
	 * @param collMemId
	 * @throws Exception
	 */
	protected static void deleteDeletedRecs(Connection connection, String tblName, String keyFld, String spTblName, String spKeyFld, int collMemId) throws Exception
	{
		Statement statement = connection.createStatement();
		statement.execute("delete from " + tblName + " where " + keyFld + " not in(select " + spKeyFld + " from " + spTblName + ")");
		statement.close();
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
			//System.out.println("geting insert value for: " + row.getFieldValue(r));
			if (r > 0)
			{
				result.append(", ");
			}
			Object valObj = row.getFieldValue(r);
			String val;
			if (valObj == null)
			{
				ERTICaptionInfoQB col = row.getColumnInfo(r);
				if (col instanceof ERTICaptionInfoTreeLevel)
				{
					//need to do this for IPT
					val = "'" + UIRegistry.getResourceString("ExportToMySQLDB.EmptyTreeLevelText") + "'";
				}
				else
				{
					val = "null";
				}
			}
			else 
			{
				val = BasicSQLUtils.getStrValue(row.getFieldValue(r));
				if (StringUtils.isBlank(val))
				{
					val = "null";
				}
			}
			result.append(val);			
		}
		result.append(")");
		//System.out.println(result.toString());
		return result.toString();
	}
	
	/**
	 * @param tblName
	 * @return
	 */
	public static String getSelectForIPTDBSrc(String tblName)
	{
		try
		{
			Connection c = null;
			Statement s = null;
			try
			{
				c = DBConnection.getInstance().createConnection();
				s = c.createStatement();
				ResultSet rs = s.executeQuery("select * from " + tblName + " limit 1");
				String result = "select ";
				for (int col = 1; col <= rs.getMetaData().getColumnCount(); col++)
				{
					if (col > 1)
					{
						result += ", ";
					}
					//XXX This only works because currently fieldNames = conceptNames
					result += tblName.toLowerCase() + "." + rs.getMetaData().getColumnName(col) + " as \"" + rs.getMetaData().getColumnName(col) + "\"";
				}
				return result + " from " + tblName.toLowerCase();
			} finally
			{
				if (s != null)
				{
					s.close();
				}
				if (c != null)
				{
					c.close();
				}
			} 
		} catch (Exception ex)
		{
			UIRegistry.displayErrorDlg(ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
			return null;
		}
	}
	
	/**
	 * @param file
	 * @param headers
	 * @param tableName
	 * @return
	 */
	protected static boolean exportRowsToTabDelimitedText(File file,
			List<String> headers, String tableName)
	{
		try
		{
			Connection conn = DBConnection.getInstance().createConnection();
			Statement stmt = null;
			FileWriter fw = new FileWriter(file);
			try
			{
				//XXX testing - probably no good for large tables.
				//List<String> lines = new LinkedList<String>();
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
					//lines.add(headerLine);
					fw.write(headerLine + "\n");
				}
				int lines = 1;
				while (rows.next())
				{
					//System.out.println(getTabDelimLine(rows));
					//FileUtils.writeStringToFile(file, getTabDelimLine(rows));
					String line = getTabDelimLine(rows);
					//lines.add(getTabDelimLine(rows));
					fw.write(line + "\n");
					if (lines++ == 10000)
					{
						//FileUtils.writeLines(file, lines);
						//lines.clear();
						fw.flush();
						lines = 0;
					}
				}
				fw.flush();
				return true;
			} finally
			{
				fw.close();
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
	
	/**
	 * @param rows
	 * @return
	 * @throws SQLException
	 */
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
	
	/**
	 * @param file
	 * @param headers
	 * @param tableName
	 * @return
	 */
	protected static javax.swing.SwingWorker<Object, Object> exportRowsToTabDelimitedText(final File file,
			final List<String> headers, final String tableName, final List<QBDataSourceListenerIFace> listeners)
	{
		javax.swing.SwingWorker<Object, Object> result = new javax.swing.SwingWorker<Object, Object>() {
			boolean success = false;
			long rowCount = -1;
			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected Object doInBackground() throws Exception
			{
				try
				{
					Connection conn = DBConnection.getInstance().createConnection();
					Statement stmt = null;
					FileWriter fw = new FileWriter(file);
					try
					{
						//XXX testing - probably no good for large tables.
						for (QBDataSourceListenerIFace listener : listeners)
						{
							listener.loading();
						}
						stmt = conn.createStatement();
						ResultSet rows = stmt.executeQuery("select * from " + tableName);
						//no simple way to get record count from ResultSet??
						rowCount = BasicSQLUtils.getCount(conn, "select count(*) from " + tableName);
						for (QBDataSourceListenerIFace listener : listeners)
						{
							listener.loaded();
							listener.rowCount(rowCount);
						}
						
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
							fw.write(headerLine + "\n");
						}
						
						for (QBDataSourceListenerIFace listener : listeners)
						{
							listener.loaded();
							listener.rowCount(rowCount);
						}
						
						long lines = 0;
						while (rows.next())
						{
							fw.write(getTabDelimLine(rows) + "\n");
							if (lines++ % 1000 == 0)
							{
								fw.flush();
								for (QBDataSourceListenerIFace listener : listeners)
								{
									listener.loaded();
									listener.currentRow(lines);
								}
							}
						}
						fw.flush();
						success = true;
						return null;
					} finally
					{
						fw.close();
						if (stmt != null)
						{
							stmt.close();
						}
						conn.close();
					}
				} catch (Exception ex)
				{
					UIRegistry.displayErrorDlg(ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
					success = false;
					return null;
				}
			}

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#done()
			 */
			@Override
			protected void done()
			{
				super.done();
				for (QBDataSourceListenerIFace listener : listeners)
				{
					listener.done(success ? rowCount : -1);
				}
			}
		
		};
		return result;
	}
}
