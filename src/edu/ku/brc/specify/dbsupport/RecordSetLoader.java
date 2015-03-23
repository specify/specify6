/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
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
