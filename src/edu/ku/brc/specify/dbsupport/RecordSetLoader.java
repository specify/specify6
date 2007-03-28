package edu.ku.brc.specify.dbsupport;

import java.util.List;
import java.util.Vector;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.dbsupport.DBTableIdMgr.TableInfo;

public class RecordSetLoader
{
	public static List<Object> loadRecordSet(RecordSetIFace recordSet)
	{
		TableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
		Class<?> recordClass = tableInfo.getClassObj();
		DataProviderIFace dataProvider = DataProviderFactory.getInstance();
		DataProviderSessionIFace session = dataProvider.createSession();
		Vector<Object> records = new Vector<Object>();
		for (RecordSetItemIFace rsItem: recordSet.getItems())
		{
			Long id = rsItem.getRecordId();
			Object record = session.get(recordClass,id);
			records.add(record);
		}
		return records;
	}
}
