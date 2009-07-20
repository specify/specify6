/**
 * 
 */
package edu.ku.brc.specify.config;

import java.util.List;
import java.util.Vector;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;

/**
 * @author timo
 *
 *duplicateCollectingEvents() ensures that for 'embedded collecting event' collections, the effective relationship between
 * CollectingEvent and CollectionObject is one-to-one.
 * 
 * Any CollectingEvents that have N > 1 related CollectionObjects are cloned N times to create a distinct CollectingEvent 
 * for each related CollectionObject.
 * 
 */
public class DuplicateCollectingEvents
{
	
	/**
	 * @param collectionId
	 * @return
	 */
	static protected Vector<Object[]> getCollectingEventsWithManyCollectionObjects(int collectionId)
	{
		String sql = "select ce.collectingeventid from collectingevent ce where "
			+ " (select count(collectionobjectid) from collectionobject co where co.collectingeventid "
			+ "= ce.collectingeventid and co.collectionmemberid = " + collectionId + ") > 1";
		return BasicSQLUtils.query(sql);
		
	}
	
	/**
	 * @param collectionId id of an 'embedded collecting event' collection.
	 * @throws Exception
	 */
	static protected void duplicateCollectingEvents(int collectionId) throws Exception
	{
		//Assuming caller has checked collection.getIsEmbeddedCollectingEvent()
		
		Vector<Object[]> ces = getCollectingEventsWithManyCollectionObjects(collectionId);
		for (Object[] ce : ces)
		{
			System.out.println("duplicating ce: " + ce[0]);
			duplicateCollectingEvent(ce[0]);
		}
		
	}
	
	/**
	 * @param ceid id for a collecting event with many (> 1) collection objects
	 * @throws Exception
	 */
	static protected void duplicateCollectingEvent(Object ceid) throws Exception
	{
		Vector<Object[]> cos = BasicSQLUtils.query("select collectionobjectid from collectionobject where collectingeventid = " + ceid);
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
        	session.beginTransaction();
        	CollectingEvent ce = session.get(CollectingEvent.class, (Integer )ceid);
        	for (int co = 1; co < cos.size(); co++)
        	{
        		CollectingEvent ceClone = (CollectingEvent )ce.clone();
        		session.saveOrUpdate(ceClone);
        		CollectionObject coObj = session.get(CollectionObject.class, (Integer )cos.get(co)[0]);
        		coObj.setCollectingEvent(ceClone);
        		session.saveOrUpdate(coObj);
        	}
        	session.commit();
        } catch (Exception ex)
        {
        	session.rollback();
        	throw ex;
        } finally
        {
        	session.close();
        }
		
	}
	
	/**
	 * @throws Exception
	 * 
	 * Duplicates collecting events for all 'embedded collecting event' collections in the database. 
	 */
	static public void duplicateCollectingEvents() throws Exception
	{
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        List<Collection> colls = null;
        try
        {
        	colls = session.getDataList(Collection.class);
        }
        finally
        {
        	session.close();
        }
        if (colls != null)
        {
        	for (Collection coll : colls)
        	{
        		if (coll.getIsEmbeddedCollectingEvent())
        		{
        			duplicateCollectingEvents(coll.getId());
        		}
        	}
        }
	}
}
