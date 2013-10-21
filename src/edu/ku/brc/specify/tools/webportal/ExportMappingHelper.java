/**
 * 
 */
package edu.ku.brc.specify.tools.webportal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.ku.brc.specify.tools.export.ExportToMySQLDB;

/**
 * @author timo
 *
 */
public class ExportMappingHelper 
{
	final Integer mappingID;
	final List<ExportMappingInfo> fields;
	final String cacheTblName;
	final Timestamp timeLastUpdated;
	final String mappingName;
	final List<String> conceptSchemaNames;
	
	/**
	 * @param conn
	 * @param mappingID
	 * @throws SQLException
	 */
	public ExportMappingHelper(Connection conn, Integer mappingID) throws SQLException
	{
		super();
		this.mappingID = mappingID;
		Statement stmt = conn.createStatement();
		try
		{
			ResultSet rs = stmt.executeQuery(getMapSql());
			rs.next();
			mappingName = rs.getString(1);
			timeLastUpdated = rs.getTimestamp(2);
			cacheTblName = ExportToMySQLDB.fixTblNameForMySQL(mappingName);
			rs.close();
			
			rs = stmt.executeQuery(getSchemasSql());
			conceptSchemaNames = new ArrayList<String>();
			while (rs.next())
			{
				conceptSchemaNames.add(rs.getString(1));
			}
			rs.close();
			
			this.fields = new ArrayList<ExportMappingInfo>();
			fillFields(stmt);
			
		} finally
		{
			stmt.close();
		}
		
	}
	
	/**
	 * @return sql to get basic info about the mapping
	 */
	protected String getMapSql()
	{
		return "select MappingName, TimestampExported from spexportschemamapping where SpExportSchemaMappingID = " + mappingID;
	}
	
	/**
	 * @return sql to get schemas mapped to
	 */
	protected String getSchemasSql()
	{
		return "select distinct s.Description, s.SchemaName "
			+ "from spexportschemamapping m inner join spexportschemaitemmapping mi on mi.SpExportSchemaMappingID = m.SpExportSchemaMappingID "
			+ "inner join spexportschemaitem si on si.SpExportSchemaItemID = mi.ExportSchemaItemID "
			+ "inner join spexportschema s on s.SpExportSchemaID = si.SpExportSchemaID where m.SpExportSchemaMappingID = " + mappingID;
		
	}
	
	/**
	 * @return mappings
	 */
	public List<ExportMappingInfo> getMappings()
	{
		return Collections.unmodifiableList(fields);
	}
	
	public ExportMappingInfo getMappingByColIdx(int colIdx)
	{
		for (ExportMappingInfo f : fields)
		{
			if (f.getColIdx().equals(colIdx)) return f;
		}
		return null;
	}
	
	/**
	 * @return the name of the mapping's cache table in the db
	 */
	public String getCacheTblName()
	{
		return cacheTblName;
	}
	
	/**
	 * @return the time last updated
	 */
	public Timestamp getTimeLastUpdated()
	{
		return timeLastUpdated;
	}
	
	/**
	 * @return the mapping name
	 */
	public String getMappingName()
	{
		return mappingName;
	}
	
	/**
	 * @return list of schemas mapped to
	 * 
	 * what's returned, and what's stored in spexportschema need work. A url for each schema is needed.
	 */
	public List<String> getConceptSchemas()
	{
		return Collections.unmodifiableList(conceptSchemaNames);
	}
	
	/**
	 * @return sql to get info for each field in the mapping
	 */
	protected String getFldsSql()
	{
		return "select mi.SpExportSchemaItemMappingID, si.FieldName as Concept, s.Description, " 
			+ "qf.ContextTableIdent as SpTblID, qf.FieldName as SpFldName, qf.StringId as FieldID "
			+ "from spexportschemamapping m inner join spexportschemaitemmapping mi "
			+ "on mi.SpExportSchemaMappingID = m.SpExportSchemaMappingID "
			+ "left join spexportschemaitem si on si.SpExportSchemaItemID = mi.ExportSchemaItemID "
			+ "left join spexportschema s on s.SpExportSchemaID = si.SpExportSchemaID "
			+ "inner join spqueryfield qf on qf.SpQueryFieldID = mi.SpQueryFieldID where qf.IsDisplay and m.SpExportSchemaMappingID = " + mappingID + " order by qf.Position";
	}
	
	/**
	 * @param stmt
	 * @throws SQLException
	 */
	protected void fillFields(Statement stmt) throws SQLException 
	{
		try 
		{
			ResultSet rs = stmt.executeQuery(getFldsSql());
			int pos = 0;
			while (rs.next()) 
			{
				fields.add(new ExportMappingInfo(rs.getInt("SpExportSchemaItemMappingID"), rs.getString("Concept"), rs.getString("Description"),
						rs.getInt("SpTblID"), rs.getString("SpFldName"), rs.getString("FieldID"), pos++));
			}
		} finally {
			stmt.close();
		}
	}
	
	/**
	 * @return
	 */
	public static int getDefaultFldLenForFormattedFld()
	{
		return ExportToMySQLDB.defaultFldLenForFormattedFld;
	}
	
}
