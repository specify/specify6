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
	 * @param recordSet
	 * @return
	 */
	public static List<Object> loadRecordSet(RecordSetIFace recordSet)
	{
		DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
		Class<?> recordClass = tableInfo.getClassObj();
		DataProviderIFace dataProvider = DataProviderFactory.getInstance();
		DataProviderSessionIFace session = dataProvider.createSession();
		Vector<Object> records = new Vector<Object>();
		for (RecordSetItemIFace rsItem: recordSet.getOrderedItems())
		{
			Integer id = rsItem.getRecordId();
			Object record = session.get(recordClass,id);
			records.add(record);
		}
		return records;
	}
}
