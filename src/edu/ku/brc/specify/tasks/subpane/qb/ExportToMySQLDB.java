/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author timbo
 *
 */
public class ExportToMySQLDB
{
	
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
		return null;
	}
	
	protected static String getFieldDef(ERTICaptionInfo column)
	{
		//XXX TESTING!!!!!!!
		return "_" + column.getColLabel() + " " + getFieldTypeDef(column.getFieldInfo());
	}
	
	public static void createTable(Connection toConnection, List<ERTICaptionInfoQB> columns, String tblName) throws Exception
	{
        Statement stmt = toConnection.createStatement();
		StringBuilder sql = new StringBuilder();
		sql.append("create table " + tblName + "(");
		boolean commafy = false;
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
	
	public static void exportToTable(Connection toConnection, List<ERTICaptionInfoQB> columns,
			Vector<Vector<Object>> rows, String tblName, List<QBDataSourceListenerIFace> listeners) throws Exception
	{
	    for (QBDataSourceListenerIFace listener : listeners)
	    {
	    	listener.loading();
	    	listener.rowCount(rows.size());
	    }
		createTable(toConnection, columns, tblName);
		Statement stmt = toConnection.createStatement();
	    for (QBDataSourceListenerIFace listener : listeners)
	    {
	    	listener.filling();
	    }
	    int rowNum = 0;
	    for (Vector<Object> row : rows)
		{
			for (QBDataSourceListenerIFace listener : listeners)
			{
				listener.currentRow(rowNum++);
			}
	    	stmt.execute(getInsertSql(row, tblName));
		}
	    for (QBDataSourceListenerIFace listener : listeners)
	    {
	    	listener.done(rowNum);
	    }
		stmt.close();
	}
	
	public static void exportToTable(List<ERTICaptionInfoQB> columns, Vector<Vector<Object>> rows, 
			String tblName, List<QBDataSourceListenerIFace> listeners) throws Exception
	{
		exportToTable(DBConnection.getInstance().getConnection(), columns, rows, tblName, listeners);
	}
	
	protected static String getInsertSql(Vector<Object> row, String tblName)
	{
		StringBuilder result = new StringBuilder();
		result.append("insert into " + tblName + " values(");
		for (int r=0; r < row.size(); r++)
		{
			if (r > 0)
			{
				result.append(", ");
			}
			result.append(BasicSQLUtils.getStrValue(row.get(r)));			
		}
		result.append(")");
		return result.toString();
	}
	
}
