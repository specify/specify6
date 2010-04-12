/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Collections;
import java.util.Vector;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author timo
 *
 * @code_status Alpha
 *
 * Apr 7, 2010
 */
public class DwcMapper
{
	final Integer mappingId; //SpExportSchemaMappingID - key for spexportschemamappingid.
	final String mappingName;
	final String schemaName;
	final Integer mappingContextTableId;
	final String schemaURL;
	
	final Vector<MappingInfo> concepts = new Vector<MappingInfo>();
	
	public static Connection connection; //for testing
	
	public DwcMapper(Integer mappingId)
	{
		this.mappingId = mappingId;
		Vector<Object[]> rec = BasicSQLUtils.query(connection, getMappingQuery(mappingId));
		mappingName = (String )rec.get(0)[0];
		schemaName = (String )rec.get(0)[1];
		mappingContextTableId = (Integer )rec.get(0)[2];
		schemaURL = (String )rec.get(0)[3];
		fillConcepts();
	}
	
	protected String getMappingQuery(Integer mappingId)
	{
		return "select esm.MappingName, es.SchemaName, q.ContextTableId, es.Description from spexportschemamapping esm inner join "
			+ "spexportschemaitemmapping esim on esim.SpExportSchemaMappingID = esm.SpExportSchemaMappingID "
			+ "inner join spexportschemaitem esi on esi.SpExportSchemaItemID = esim.ExportSchemaItemID inner join " 
			+ "spexportschema es on es.SpExportSchemaID = esi.SpExportSchemaID inner join spqueryfield qf on "
			+ "qf.SpQueryFieldID = esim.SpQueryFieldID inner join spquery q on q.SpQueryID = qf.SpQueryID where "
			+ "esm.SpExportSchemaMappingID = " + mappingId;
	}
	
	protected void fillConcepts()
	{
		Vector<Object[]> cpts = BasicSQLUtils.query(connection, getConceptQuery());
		concepts.clear();
		for (Object[] concept : cpts)
		{
			concepts.add(new MappingInfo((String )concept[0], (String )concept[1], (String )concept[2], 
					mappingContextTableId));
		}		
	}
	
	protected String getMappingName(Integer mappingId)
	{
		return BasicSQLUtils.querySingleObj(connection, 
				"select MappingName from spexportschemamapping where "
				+ " SpExportSchemaMappingID = " + mappingId);
	}
	
	protected String getConceptQuery()
	{
		return "select esi.FieldName, esi.DataType, qf.StringId from spexportschemaitemmapping esim inner join spexportschemaitem esi on "
			+ "esi.SpExportSchemaItemID = esim.ExportSchemaItemID inner join spqueryfield qf on qf.SpQueryFieldID = esim.SpQueryFieldID where esim.SpExportSchemaMappingID = "
			+ mappingId;
	}
	
	public void setDarwinCoreConcepts(DarwinCoreSpecimen spec) throws Exception
	{
		spec.clearConcepts();
		for (MappingInfo mi : concepts)
		{
			spec.add(mi.getName(), null);
		}
	}
	
	/**
	 * @param collectionObjectId
	 * @return query to retrieve darwin core record from the cache.
	 */
	protected String getValuesQuery(Integer collectionObjectId)
	{
		return "select * from " + mappingName.toLowerCase() + " where " + mappingName + "id = " + collectionObjectId; 
	}
	
	
	
	/**
	 * @param spec
	 * @throws Exception
	 * 
	 * Get the darwin core values for the specimen.
	 * 
	 * Currently this just gets the values from the cache created by the export tool. Will need
	 * to get live values if the cache is not built or the cache record is out of date or the cache does not 
	 * contain the specimen yet. May turn out to be unnecessary to use the cache.
	 * Current idea for use when cache won't work is to get the ExportMapper to fill in the SpQuery.sql field
	 * with something that can be run without having to setup a querybuilder -- though perhaps something
	 * like what is done with the qb when reports run will work...
	 * 
	 * Also, given a CollectionObject object it should be possible to use MappingInfo.mapping to follow relationships from the CO to the mapped fields.
	 * 
	 */
	
	public void setDarwinCoreValues(DarwinCoreSpecimen spec) throws Exception
	{
		if (spec.hasDataModelObject())
		{
			setDarwinCoreValuesForObj(spec);
		}
		else
		{
			setDarwinCoreValuesForId(spec);
		}
	}
	
	/**
	 * @param spec
	 * @throws Exception
	 */
	protected void setDarwinCoreValuesForId(DarwinCoreSpecimen spec) throws Exception
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery(getValuesQuery(spec.getCollectionObjectId()));
			if (!rs.next())
			{
				throw new Exception("no record for " + spec.getCollectionObjectId() + " in " + mappingName);
			}
			ResultSetMetaData metaData = rs.getMetaData();
			for (int c = 2; c < metaData.getColumnCount(); c++)
			{
				String colName = metaData.getColumnLabel(c);
				spec.set(colName, rs.getObject(colName));
			}
		}
		finally
		{
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
		}
	}
	
	/**
	 * @param spec
	 * @throws Exception
	 */
	protected void setDarwinCoreValuesForObj(DarwinCoreSpecimen spec) throws Exception
	{
		throw new Exception("No code is present to do this thing.");
	}
	
	/**
	 * @return number of concepts
	 */
	public int getConceptCount()
	{
		return concepts.size();
	}
	
	/**
	 * @param c
	 * @return concept c
	 */
	public MappingInfo getConcept(int c)
	{
		return concepts.get(c);
	}
		
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

	}

}
