package edu.ku.brc.specify.dbsupport;

import java.util.List;
import java.util.Vector;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Locality;

/**
 * Original author was JDS.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 27, 2007
 *
 */
public class RecordSetLoader
{
	/**
	 * Loads the objects with the ids into a list of objects.
	 * @param recordSet the recordset of IDs to be loaded into memory
	 * @return List of database objects
	 */
	public static List<Object> loadRecordSet(final RecordSetIFace recordSet)
	{
        Vector<Object>           records      = new Vector<Object>();
		DBTableInfo              tableInfo    = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
		Class<?>                 recordClass  = tableInfo.getClassObj();
		DataProviderIFace        dataProvider = DataProviderFactory.getInstance();
		DataProviderSessionIFace session      = null;
		try
		{
		    session = dataProvider.createSession();
	        for (RecordSetItemIFace rsItem: recordSet.getOrderedItems())
	        {
	            Object record = session.get(recordClass, rsItem.getRecordId());
	            records.add(record);
	        }
	        
		} catch (Exception ex)
		{
		    ex.printStackTrace();
		} finally
		{
		    if (session != null)
		    {
		        session.close();
		    }
		}
		return records;
	}
	
	/**
     * Loads the CollecitonObjects with the ids into a list of objects.
     * @param recordSet the recordset of IDs to be loaded into memory
     * @return List of database objects
     */
    public static List<Object> loadCollectionObjectsRecordSet(final RecordSetIFace recordSet)
    {
        Vector<Object> colObjList = new Vector<Object>();
        if (recordSet.getDbTableId().equals(CollectionObject.getClassTableId()))
        {
            DBTableInfo              tableInfo    = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
            Class<?>                 recordClass  = tableInfo.getClassObj();
            DataProviderIFace        dataProvider = DataProviderFactory.getInstance();
            DataProviderSessionIFace session      = null;
            try
            {
                session = dataProvider.createSession();
                for (RecordSetItemIFace rsItem: recordSet.getOrderedItems())
                {
                    CollectionObject colObj = (CollectionObject)session.get(recordClass, rsItem.getRecordId());
                    for (Determination det : colObj.getDeterminations())
                    {
                        det.getTaxon().getId();
                    }
                    CollectingEvent ce = colObj.getCollectingEvent();
                    if (ce != null)
                    {
                        Locality loc = ce.getLocality();
                        if (loc != null)
                        {
                            loc.getGeography();
                        }
                    }
                    colObjList.add(colObj);
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
        return colObjList;
    }

}
