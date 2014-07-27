/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

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
    protected static final Logger     log = Logger.getLogger(DarwinCoreSpecimen.class);
	final protected DwcMapper mapper;
	protected Integer collectionObjectId;
	protected CollectionObject collectionObject;
	protected Map<String, Pair<String, Object>> concepts = new HashMap<String, Pair<String, Object>>();
	
	/**
	 * @param mapper
	 * @throws Exception
	 */
	public DarwinCoreSpecimen(DwcMapper mapper) throws Exception
	{
		this.mapper = mapper;
		mapper.setDarwinCoreConcepts(this);
	}
	
	/**
	 * @param collectionObjectId
	 * @throws Exception
	 */
	public void setCollectionObjectId(Integer collectionObjectId) throws Exception
	{
		this.collectionObjectId = collectionObjectId;
		this.collectionObject = null;
		mapper.setDarwinCoreValues(this);
	}
	
	/**
	 * 
	 */
	protected void clearConcepts()
	{
		concepts.clear();
	}
	
	/**
	 * @param term
	 * @param value
	 * @throws Exception
	 */
	protected void add(String term, String value) throws Exception
	{
		if (concepts.containsKey(term))
		{
			throw new Exception(term + " concept is already mapped.");
		}
		
		concepts.put(term, new Pair<String, Object>(term, value));		
	}
	
	
	/**
	 * @param term
	 * @param value
	 * @throws Exception
	 */
	protected void set(String term, Object value) throws Exception
	{
		String fullTerm = term;
		if (!concepts.containsKey(term))
		{
			//throw new Exception(fieldName + " concept is not mapped.");
			Map.Entry<String, Pair<String, Object>> m = getMappingByName(term);
			if (m == null) {
				log.warn(term + " concept is not mapped.");
				return;
			}
			fullTerm = m.getKey();
		} 
		
		concepts.put(fullTerm, new Pair<String, Object>(fullTerm, value));
	}
	
	/**
	 * @param termName
	 * @return
	 */
	public Object get(String termName)
	{
		//System.out.println("DarwinCoreSpecimen.get(" + termName + ")");
		return concepts.get(termName).getSecond();
	}
	
	/**
	 * @param conceptName
	 * @return
	 */
	public Object getByName(String conceptName)
	{
		Map.Entry<String, Pair<String, Object>> mapping = getMappingByName(conceptName);
		if (mapping != null)
		{
			return mapping.getValue().getSecond();
		}
		return null;
	}
	
	/**
	 * @param termName
	 * @return
	 */
	public boolean isMapped(String termName)
	{
		return concepts.containsKey(termName);
	}
	
	/**
	 * @param conceptName
	 * @return
	 */
	public boolean isMappedByName(String conceptName)
	{
		return getMappingByName(conceptName) != null;
	}
	
	/**
	 * @param fldName
	 * @return
	 */
	protected Map.Entry<String, Pair<String, Object>> getMappingByName(String fldName)
	{
		List<Map.Entry<String, Pair<String, Object>>> results = getMappingsByName(fldName);
		if (results.size() == 0) {
			return null;
		} else {
			if (results.size() > 1) {
				log.warn("more than one mapping for term with name: " + fldName);
			}
			return results.get(0);
		}
	}
	
	/**
	 * @param fldName
	 * @return
	 */
	protected List<Map.Entry<String, Pair<String, Object>>> getMappingsByName(String fldName)
	{
		List<Map.Entry<String, Pair<String, Object>>> result = new ArrayList<Map.Entry<String, Pair<String, Object>>>();
		for (Map.Entry<String, Pair<String, Object>> entry : concepts.entrySet())
		{
			String key = entry.getKey();
			if (fldName.equalsIgnoreCase(key.substring(key.lastIndexOf("/")+1)))
			{
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * @return
	 */
	public int getFieldCount()
	{
		return concepts.size();
	}
	
	/**
	 * @return
	 */
	public Vector<Pair<String, Object>> getFieldValues()
	{
		Vector<Pair<String, Object>> result = new Vector<Pair<String, Object>>();
		for (Map.Entry<String, Pair<String, Object>> me : concepts.entrySet())
		{
			result.add(new Pair<String, Object>(me.getValue().getFirst(), me.getValue().getSecond()));
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
	 * @return
	 */
	public String getCollectionObjectGUID() {
		return collectionObject.getGuid();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			String connStr = "jdbc:mysql://localhost/creac?characterEncoding=UTF-8&autoReconnect=true"; 
			DwcMapper.connection = DriverManager.getConnection(connStr, "Master", "Master");

			DwcMapper mapper = new DwcMapper(1, true);
			DarwinCoreSpecimen spec = new DarwinCoreSpecimen(mapper);
			spec.setCollectionObjectId(10911);
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
