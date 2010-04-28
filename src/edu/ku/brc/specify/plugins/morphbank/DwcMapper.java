/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.tools.export.ConceptMapUtils;
import edu.ku.brc.specify.tools.export.MappedFieldInfo;
import edu.ku.brc.ui.UIRegistry;

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
	
	/**
	 * @param mappingId
	 */
	public DwcMapper(Integer mappingId)
	{
		this.mappingId = mappingId;
		Vector<Object[]> rec = BasicSQLUtils.query(connection, getMappingQuery(mappingId));
		mappingName = (String )rec.get(0)[0];
		schemaName = (String )rec.get(0)[1];
		mappingContextTableId = (Integer )rec.get(0)[2];
		schemaURL = (String )rec.get(0)[3];
		fillConcepts();
		Collections.sort(concepts);
		for (MappingInfo mi : concepts)
		{
			System.out.println(mi.getMapping() + "  " + mi.getName());
		}
	}
	
	/**
	 * 
	 */
	public DwcMapper()
	{
		mappingId = null;
		mappingName = UIRegistry.getResourceString("DwcMapper.Default");
		schemaName = null;
		mappingContextTableId = CollectionObject.getClassTableId();
		schemaURL = null;
		fillDefaultConcepts();
		Collections.sort(concepts);
	}
	
	/**
	 * @param mappingId
	 * @return query to extract mapping from 'sp' tables for given mapping key
	 */
	protected String getMappingQuery(Integer mappingId)
	{
		return "select esm.MappingName, es.SchemaName, q.ContextTableId, es.Description from spexportschemamapping esm inner join "
			+ "spexportschemaitemmapping esim on esim.SpExportSchemaMappingID = esm.SpExportSchemaMappingID "
			+ "inner join spexportschemaitem esi on esi.SpExportSchemaItemID = esim.ExportSchemaItemID inner join " 
			+ "spexportschema es on es.SpExportSchemaID = esi.SpExportSchemaID inner join spqueryfield qf on "
			+ "qf.SpQueryFieldID = esim.SpQueryFieldID inner join spquery q on q.SpQueryID = qf.SpQueryID where "
			+ "esm.SpExportSchemaMappingID = " + mappingId;
	}
	
	/**
	 * 
	 */
	protected void fillConcepts()
	{
		Vector<Object[]> cpts = BasicSQLUtils.query(connection, getConceptQuery());
		concepts.clear();
		for (Object[] concept : cpts)
		{
			concepts.add(new MappingInfo((String )concept[0], (String )concept[1], (String )concept[2], 
					mappingContextTableId, (Boolean )concept[3]));
		}	
	}
	
	/**
	 * 
	 */
	protected void fillDefaultConcepts()
	{
		concepts.clear();
		for (Map.Entry<String, MappedFieldInfo> me : ConceptMapUtils.getDefaultDarwinCoreMappings().entrySet())
		{
			concepts.add(new MappingInfo(me.getKey(), me.getValue()));
		}
	}
	
	/**
	 * @param mappingId
	 * @return
	 */
	protected String getMappingName(Integer mappingId)
	{
		return BasicSQLUtils.querySingleObj(connection, 
				"select MappingName from spexportschemamapping where "
				+ " SpExportSchemaMappingID = " + mappingId);
	}
	
	/**
	 * @return
	 */
	protected String getConceptQuery()
	{
		return "select esi.FieldName, esi.DataType, qf.StringId, qf.IsRelFld from spexportschemaitemmapping esim inner join spexportschemaitem esi on "
			+ "esi.SpExportSchemaItemID = esim.ExportSchemaItemID inner join spqueryfield qf on qf.SpQueryFieldID = esim.SpQueryFieldID where esim.SpExportSchemaMappingID = "
			+ mappingId;
	}
	
	/**
	 * @param spec
	 * @throws Exception
	 */
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
		if (/*true*/spec.hasDataModelObject())
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
		//throw new Exception("No code is present to do this thing.");
		
		//Using hibernate objects and reflection ...
		for (MappingInfo mi : concepts)
		{
			spec.set(mi.getName(), getMappedValue(mi, spec.getCollectionObject()));
		}
		
		//But maybe it is easier to construct a query or to create and save a query for this purpose into SpQuery.SqlStr (even though it will have to be hql for now)
		//hibernate strategy is beginning to suck. And what about lazy-loading and session attachment, and the chance for messing things up in the form system's session...???
	}
	
	/**
	 * @param mi
	 * @param obj
	 * @return
	 */
	protected Object getMappedValue(MappingInfo mi, DataModelObjBase obj) throws Exception
	{
		String[] mapSegments = mi.getMapping().split(",");
		DataModelObjBase currentObject = obj;
		DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
		try
		{
			session.attach(currentObject);
			
			if (mapSegments.length == 1)
			{
				return getValueFromObject(currentObject, mapSegments[0], mi
						.isFormatted(), mi.isTreeRank());
			}
			
			for (int s = 1; s < mapSegments.length; s++)
			{
				System.out.println(mapSegments[s]);

				if (currentObject != null
						&& (s < mapSegments.length - 1 || (!mi.isFormatted() && !mi
								.isTreeRank())))
				{
					currentObject = getRelatedObject(currentObject,
							mapSegments[s]);
				}
				if (currentObject == null)
				{
					return null;
				}
				
				session.attach(currentObject); //shouldn't have to do this explicitly???
				
				System.out.println("   "
						+ currentObject.getClass().getSimpleName());

				if (s == mapSegments.length - 1)
				{
					return getValueFromObject(currentObject, mapSegments[s], mi
							.isFormatted(), mi.isTreeRank());
				}
			}
		} finally
		{
			session.close();
		}
		return null;
	}
	
	/**
	 * @param object
	 * @param mapping
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected DataModelObjBase getRelatedObject(DataModelObjBase object, String mapping) throws Exception
	{
		String[] mapInfo = mapping.split("-");
		int tableId = Integer.parseInt(mapInfo[0].split("\\.")[0]);
		String relationshipName = mapInfo.length > 1 ? mapInfo[1].split("\\.")[0] : null;
		Class<? extends DataModelObjBase> relatedClass = (Class<? extends DataModelObjBase> )DBTableIdMgr.getInstance().getInfoById(tableId).getClassObj();
		if (relationshipName != null) //this means we have a 1-many (?)
		{
			String methName =  "get" + relationshipName.substring(0, 1).toUpperCase().concat(relationshipName.substring(1)); 
			Method meth = object.getClass().getMethod(methName);
			return selectRelatedObject(object, meth, relatedClass);
		}
		String methName = "get" + relatedClass.getSimpleName();
		Method meth = object.getClass().getMethod(methName);
		return (DataModelObjBase )meth.invoke(object);
	}
	
	/**
	 * @param parent
	 * @param getter
	 * @return appropriate related object
	 * 
	 * This is used for one-to-many relationships where getter returns a set of related objects.
	 * In most cases it should be safe to assume a criterion for picking based on the related class type.
	 * 
	 * Currently criteria are Determination - isCurrent, Collector - isPrimary, others just the first...
	 * 
	 * Hopefully, in the vast majority of cases, one-to-many's will be handled by aggregations or formatters and this method will not be needed.
	 */
	@SuppressWarnings("unchecked")
	protected DataModelObjBase selectRelatedObject(DataModelObjBase parent, Method getter, Class<? extends DataModelObjBase> relatedClass) throws Exception
	{
		Set<? extends DataModelObjBase> objects = (Set<? extends DataModelObjBase> )getter.invoke(parent);
		if (objects.size() > 0)
		{
			Iterator<? extends DataModelObjBase> iter = objects.iterator();
			while(iter.hasNext())
			{
				DataModelObjBase obj = iter.next();
				if (isObjectToSelect(parent, obj))
				{
					return obj;
				}
			}
		}
		return null;
	}
	
	protected boolean isObjectToSelect(DataModelObjBase parent, DataModelObjBase obj) throws Exception
	{
		if (parent.getClass().equals(CollectionObject.class))
		{
			if (obj.getClass().equals(Determination.class))
			{
				return ((Determination )obj).getIsCurrent();
			}
			//just 
		}
		throw new Exception("Unsupported parent class " + parent.getClass().getName());
	}
	
	protected Object getValueFromObject(DataModelObjBase object, String mapping, boolean isFormatted, boolean isTreeRank) throws Exception
	{
		if (!isFormatted && !isTreeRank)
		{
			String fieldName = mapping.split("\\.")[2];
			String methodName = "get" + fieldName.substring(0, 1).toUpperCase().concat(fieldName.substring(1));
			Method method = object.getClass().getMethod(methodName);
			System.out.println("Getting a value: " + object + ", " + mapping + " = " + method.invoke(object));
			return method.invoke(object);
		}
		return null;
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
