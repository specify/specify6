/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.tasks.subpane.qb.ERTICaptionInfoQB;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSource;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 */
/**
 * @author timo
 *
 */
public class ExportToMySQLDB
{
	
	private final static int bulkBlockSize = 1000;
	private final static int bulkQueueSize = 8000;
	
	public final static int defaultFldLenForFormattedFld = 500; 
	//public final static int maxWidthForTextFld = 1250; //assumes MySql 5.0.3 or greater.
													//since this class generates data 
													// intended for web searches
													//it seems acceptable to impose a limit
	
	/**
	 * @param fld
	 * @return mysql field type declaration.
	 */
	protected static String getFieldTypeDef(ERTICaptionInfo column)
	{
		DBFieldInfo fld = column.getFieldInfo();
		Class<?> dataType = column.getColClass() != null ? column.getColClass() : (fld != null ? fld.getDataClass() : null);
		if (dataType == null && fld == null)
		{
			//assume it's format or aggregatated or otherwise special
			return "varchar(" + defaultFldLenForFormattedFld + ")";
		}
		
		if (dataType.equals(String.class))
		{
			if (fld != null && ("text".equalsIgnoreCase(fld.getType()) || fld.getLength() > defaultFldLenForFormattedFld))
			{
				return "text";
				
			} else
			{
				String length = String.valueOf(defaultFldLenForFormattedFld);
				if (fld != null && fld.getLength() != -1 && fld.getLength() <= defaultFldLenForFormattedFld)
				{
					length = String.valueOf(fld.getLength());
				}
				return "varchar(" + length + ")";
			}
		}
		if (dataType.equals(Integer.class) || dataType.equals(Byte.class) || dataType.equals(Short.class) || dataType.equals(Long.class))
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
		if (dataType.equals(Double.class) || dataType.equals(Float.class))
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
		return "`" + getFieldName(column) +  "` " + getFieldTypeDef(column);
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
	 * @param tblName
	 * @throws Exception
	 */
	protected static void dropTable(Connection toConnection, String tblName) throws Exception
	{
		Statement stmt = toConnection.createStatement();
		try
		{
			stmt.execute("drop table " + tblName);
		}
		finally
		{
			stmt.close();
		}
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
		try
		{
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
			sql.append(" CHARSET=utf8");
			stmt.execute(sql.toString());
			
			//should do this in the create statement
			String keySql = "ALTER TABLE " + tblName + " CHANGE COLUMN " + getIdFieldName(tblName) + " " + getIdFieldName(tblName) + " INT(11) NOT NULL, "
				+ "ADD PRIMARY KEY (" + getIdFieldName(tblName) + ")";
			stmt.execute(keySql);
			
		} finally
		{
			stmt.close();
		}
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
	 * @param originalTblName
	 * @param listeners
	 * @param idColumnPresent
	 * @param overwrite
	 * @param update
	 * @param baseTableId
	 * @param firstPass
	 * @param bulkFilePath
	 * @return
	 * @throws Exception
	 * 
	 * Exports rows to a table named tblName in toConnection's db.
	 * NOTE: toConnection is closed by this method.
	 */
	public static long exportToTable(Connection toConnection, List<ERTICaptionInfoQB> columns,
			QBDataSource rows, String originalTblName, List<QBDataSourceListenerIFace> listeners,
			boolean idColumnPresent, boolean overwrite, boolean update, int baseTableId, boolean firstPass,
			String bulkFilePath) throws Exception
	{
	    
		try 
		{
			boolean newTable = false;
			String tblName = fixTblNameForMySQL(originalTblName);
			if (firstPass && (overwrite || !tableExists(toConnection, tblName)))
			{
				if (tableExists(toConnection, tblName))
				{
					dropTable(toConnection, tblName);
				}
				createTable(toConnection, columns, tblName, idColumnPresent);
				newTable = true;
			}
	    
			long deletedRows = 0;
			if (firstPass && update)
			{
				DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(baseTableId);
	    	
				deletedRows = deleteDeletedRecs(toConnection, tblName, tblName + "Id", tbl.getName(), tbl.getIdColumnName(), AppContextMgr.getInstance().getClassObject(Collection.class).getId());
			}
	    
			//System.out.println("deleted deleted recs");
	    
			if (rows.hasResultSize() && rows.size() > 0)
			{
				for (QBDataSourceListenerIFace listener : listeners)
				{
					listener.loaded();
					listener.rowCount(rows.size());
				}
				//System.out.println("listeners notified: loaded()");
			}

			Statement stmt = toConnection.createStatement();
			long rowNum = 0;
			//long startTime = System.nanoTime();
			try
			{
				for (QBDataSourceListenerIFace listener : listeners)
				{
					listener.filling();
				}
			
				rowNum = deletedRows + processRows(listeners, rows, rowNum, update, newTable, firstPass, stmt, tblName, bulkFilePath);
				if (deletedRows != 0)
				{
					deletedRows = 0;
				}
				//System.out.println("returning " + rowNum + ". Time elapsed: " + (System.nanoTime() - startTime)/1000000000L + " seconds.");
				return rowNum;
			}
			finally
			{
				stmt.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
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
	 * @param baseTableId
	 * @param firstPass
	 * @param bulkFilePath
	 * @return
	 * @throws Exception
	 * 
	 * Exports rows to a table named tblName in the default database.
	 */
	public static long exportToTable(List<ERTICaptionInfoQB> columns, QBDataSource rows, 
			String tblName, List<QBDataSourceListenerIFace> listeners,
			boolean idColumnPresent, boolean overwrite, boolean update, int baseTableId, boolean firstPass, String bulkFilePath) throws Exception
	{
		return exportToTable(DBConnection.getInstance().createConnection(), columns, rows, tblName, listeners, idColumnPresent, overwrite, update, baseTableId, firstPass, bulkFilePath);
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
	protected static long deleteDeletedRecs(Connection connection, String tblName, String keyFld, String spTblName, String spKeyFld, int collMemId) throws Exception
	{
		String sql = "delete from " + tblName + " where " + keyFld + " not in(select " + spKeyFld + " from " + spTblName;
		if (collMemId != -1)
		{
			sql += " where CollectionMemberId = " + collMemId;
		}
		sql += ")";
		Statement statement = connection.createStatement();
		statement.execute(sql);
		long result = statement.getUpdateCount();
		statement.close();
		return result;
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
			Object fldVal = row.getFieldValue(r);
			String val;
			if (fldVal == null)
			{
				val = "null";
			}
			else 
			{
				//Not necessary to trim lengths unless sql_mode has been customized...
//				if (fldVal instanceof String)
//				{
//					String fldStr = (String )fldVal;
//					fldVal = fldStr.substring(0, Math.min(fldStr.length(), maxWidthForTextFld));
//				}
				val = BasicSQLUtils.getStrValue(fldVal);
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
	 * @param row
	 * @param tblName
	 * @return
	 */
	protected static String getInsertSql(List<Future<?>> row, String tblName) throws Exception
	{
		StringBuilder result = new StringBuilder();
		result.append("insert into " + tblName + " values(");
		for (int r=0; r < row.size(); r++)
		{
			//System.out.println("geting insert value for: " + row.getFieldValue(r));
			if (r > 0)
			{
				result.append(", ");
			}
			Object fldVal = row.get(r).get();
			String val;
			if (fldVal == null)
			{
				val = "null";
			}
			else 
			{
// 			Not necessary to trim lengths unless sql_mode has been customized...
//				if (fldVal instanceof String)
//				{
//					String fldStr = (String )fldVal;
//					fldVal = fldStr.substring(0, Math.min(fldStr.length(), 256));
//				}
				val = BasicSQLUtils.getStrValue(fldVal);
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
	 * @param row
	 * @return
	 * @throws Exception
	 */
	protected static String getBulkLine(List<Future<?>> row) throws Exception
	{
		//Thread.sleep(500); //to wait for formatters to see if get() is waiting...
		StringBuilder result = new StringBuilder();
		for (int r=0; r < row.size(); r++)
		{
			//System.out.println("geting insert value for: " + row.getFieldValue(r));
			if (r > 0)
			{
				result.append("\t");
			}
			Object fldVal = row.get(r).get();
			String val;
			if (fldVal == null)
			{
				val = "NULL";
			}
			else 
			{
//Not necessary to trim lengths unless sql_mode has been customized...
//				if (fldVal instanceof String)
//				{
//					String fldStr = (String )fldVal;
//					fldVal = fldStr.substring(0, Math.min(fldStr.length(), 256));
//				}
				val = BasicSQLUtils.getStrValue(fldVal);
				if (StringUtils.isBlank(val))
				{
					val = "NULL";
				}
			}
			result.append(val);			
		}
		//result.append(")");
		//System.out.println(result.toString());
		return result.toString();
		
	}
	
	
	/**
	 * @param listeners
	 * @param rows
	 * @param rowNum
	 * @param update
	 * @param newTable
	 * @param firstPass
	 * @param stmt
	 * @param tblName
	 * @param fullFilePathName
	 * @return
	 * @throws Exception
	 */
	protected static long processRows(List<QBDataSourceListenerIFace> listeners,
			QBDataSource rows, long rowNum, boolean update, boolean newTable,
			boolean firstPass, Statement stmt, String tblName, String fullFilePathName) throws Exception 
	{
		boolean doBulk = fullFilePathName != null;
		List<String> bulk = doBulk ? new ArrayList<String>(bulkBlockSize) : null;
		long currentRow = rowNum;
		BlockingRowQueue q = new BlockingRowQueue(bulkQueueSize);
		RowFiller f = new RowFiller(q, rows);
		Thread fThread = new Thread(f);
		try 
		{
			fThread.setPriority(fThread.getPriority() - 3);
			fThread.start();
			while (!rows.hasResultSize()); //need to wait here else the q.take() call in the loop will wait forever for empty data sources.
			while ((!q.isFinished() || !q.isEmpty() || currentRow < rows.size()) && !(rows.hasResultSize() && rows.size() == 0)) 
			{
				//System.out.print("  taking ... ...");
				List<Future<?>> row = q.take();
				//System.out.println(" took");
				// System.out.println("exporting " + currentRow);
				for (QBDataSourceListenerIFace listener : listeners) 
				{
					listener.currentRow(currentRow);
				}
				currentRow++;
				if (update && !newTable) 
				{
					stmt.execute("delete from " + tblName + " where "
							+ getIdFieldName(tblName) + " = " + row.get(0).get());
				}
				if (doBulk)
				{
					bulk.add(getBulkLine(row));
					if (bulk.size() == bulkBlockSize) 
					{
						FileUtils.writeLines(new File(fullFilePathName),bulk, currentRow / bulkBlockSize > 0 || !firstPass);
						bulk.clear();
					}
				} else
				{
					stmt.execute(getInsertSql(row, tblName));
				}
			}
			//System.out.println("returning " + currentRow);
        	for (QBDataSourceListenerIFace listener : listeners)
            {
            	listener.done(currentRow);
            }
			if (doBulk && bulk.size() > 0) 
			{
				FileUtils.writeLines(new File(fullFilePathName),bulk, currentRow / bulkBlockSize > 0 || !firstPass);
				bulk.clear();
			}
			return currentRow;
		} finally 
		{
			//in case of exception, does the RowFiller need to be explicitly shut-down???
			
			stmt.close();
			
			if (doBulk && bulk.size() > 0) 
			{
				FileUtils.writeLines(new File(fullFilePathName),bulk, currentRow / bulkBlockSize > 0 || !firstPass);
				bulk.clear();
			}
		}

	}
	
	/**
	 * @param row
	 * @return
	 */
	protected static String getBulkLine(QBDataSource row) 
	{
		StringBuilder result = new StringBuilder();
		for (int r=0; r < row.getFieldCount(); r++)
		{
			//System.out.println("geting insert value for: " + row.getFieldValue(r));
			if (r > 0)
			{
				result.append("\t");
			}
			Object fldVal = row.getFieldValue(r);
			String val;
			if (fldVal == null)
			{
				val = "NULL";
			}
			else 
			{
				//Not necessary to trim lengths unless sql_mode has been customized...
//				if (fldVal instanceof String)
//				{
//					String fldStr = (String )fldVal;
//					fldVal = fldStr.substring(0, Math.min(fldStr.length(), 256));
//				}
				val = BasicSQLUtils.getStrValue(fldVal);
				if (StringUtils.isBlank(val))
				{
					val = "NULL";
				}
			}
			result.append(val);			
		}
		//result.append(")");
		//.out.println(result.toString());
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
				// If link to original specify db model field was available
				// would only
				// need to do this for 'remarks' fields.
				if (type == java.sql.Types.LONGNVARCHAR
						|| type == java.sql.Types.LONGVARCHAR
						|| type == java.sql.Types.VARCHAR
						|| type == java.sql.Types.CHAR
						|| type == java.sql.Types.NCHAR)// hopefully don't need
														// to worry about blobs.
				{
					val = val.replace("\t", " ");
					val = val.replace("\n", " ");
					val = val.replace("\r", " ");
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
