/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 * @code_status Alpha
 *
 * Apr 7, 2010
 */
public class DarwinCoreSpecimen
{
	final protected DwcMapper mapper;
	protected Integer collectionObjectId;
	protected CollectionObject collectionObject;
	protected Map<String, Object> concepts = new HashMap<String, Object>();
	
	public DarwinCoreSpecimen(DwcMapper mapper) throws Exception
	{
		this.mapper = mapper;
		mapper.setDarwinCoreConcepts(this);
	}
	
	public void setCollectionObjectId(Integer collectionObjectId) throws Exception
	{
		this.collectionObjectId = collectionObjectId;
		this.collectionObject = null;
		mapper.setDarwinCoreValues(this);
	}
	
	protected void clearConcepts()
	{
		concepts.clear();
	}
	
	protected void add(String fieldName, String value) throws Exception
	{
		if (concepts.containsKey(fieldName))
		{
			throw new Exception(fieldName + " concept is already mapped.");
		}
		
		concepts.put(fieldName, value);		
	}
	
	protected void set(String fieldName, Object value) throws Exception
	{
		if (!concepts.containsKey(fieldName))
		{
			throw new Exception(fieldName + " concept is not mapped.");
		}
		
		concepts.put(fieldName, value);
	}
	
	public Object get(String fieldName)
	{
		return concepts.get(fieldName);
	}
	
	public boolean isMapped(String conceptName)
	{
		return concepts.containsKey(conceptName);
	}
	
	public int getFieldCount()
	{
		return concepts.size();
	}
	
	public Vector<Pair<String, Object>> getFieldValues()
	{
		Vector<Pair<String, Object>> result = new Vector<Pair<String, Object>>();
		for (Map.Entry<String, Object> me : concepts.entrySet())
		{
			result.add(new Pair<String, Object>(me.getKey(), me.getValue()));
		}
		return result;
	}
	
	/**
	 * @return the collectionObjectId
	 */
	public Integer getCollectionObjectId() 
	{
		return collectionObjectId;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			String connStr = "jdbc:mysql://localhost/lsusmollusca?characterEncoding=UTF-8&autoReconnect=true"; 
			DwcMapper.connection = DriverManager.getConnection(connStr, "Master", "Master");

			DwcMapper mapper = new DwcMapper(1);
			DarwinCoreSpecimen spec = new DarwinCoreSpecimen(mapper);
			spec.setCollectionObjectId(1);
			for (Pair<String, Object> fld : spec.getFieldValues())
			{
				System.out.println(fld.getFirst() + " = " + fld.getSecond());
			}
			System.exit(0);
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * @return the collectionobject
	 */
	public CollectionObject getCollectionObject()
	{
		return collectionObject;
	}

	/**
	 * @param collectionObject
	 * @throws Exception
	 */
	public void setCollectionObject(CollectionObject collectionObject) throws Exception
	{
		this.collectionObject = collectionObject;
		this.collectionObjectId = collectionObject != null ? collectionObject.getId() : null;
		mapper.setDarwinCoreValues(this);
	}
	
	/**
	 * @return true if a hibernate object has been associated with the DarwinCoreSpecimen
	 */
	public boolean hasDataModelObject()
	{
		return collectionObject != null;
	}
}
