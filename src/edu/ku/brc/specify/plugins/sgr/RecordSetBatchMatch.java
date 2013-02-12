/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.plugins.sgr;

import java.util.Iterator;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.sgr.Matchable;
import edu.ku.brc.sgr.MatchableIndexedId;
import edu.ku.brc.sgr.datamodel.BatchMatchResultSet;
import edu.ku.brc.sgr.datamodel.MatchConfiguration;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.RecordSet;

public class RecordSetBatchMatch
{
    public static SGRBatchScenario newScenario(final RecordSetIFace recordSet, 
                                               MatchConfiguration matchConfig)
    {
        if (!recordSet.getDataClassFormItems().equals(CollectionObject.class))
            throw new IllegalArgumentException("record set must contain collection objects");

        return new SGRBatchScenario(recordSet, matchConfig, 
                recordGenerator(recordSet), recordSet.getName(), 
                Long.valueOf(recordSet.getRecordSetId()));
    }
    
    public static SGRBatchScenario resumeScenario(BatchMatchResultSet resultSet)
    {
        Long recordSetId = resultSet.getRecordSetId();
        if (recordSetId == null)
            throw new IllegalArgumentException("result set has no associated record set");
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        RecordSet recordSet = session.getData(RecordSet.class, "recordSetId", recordSetId, 
                DataProviderSessionIFace.CompareType.Equals);

        return new SGRBatchScenario(resultSet, recordGenerator(recordSet));
    }
    
    private static SGRBatchScenario.RecordGenerator recordGenerator(final RecordSetIFace recordSet)
    {
        return new SGRBatchScenario.RecordGenerator()
        {
            Iterator<RecordSetItemIFace> items = recordSet.getItems().iterator();
            
            @Override
            public Matchable next()
            {
                return item2Matchable(items.next());
            }
            
            @Override
            public boolean hasNext() { return items.hasNext(); }
            
            @Override
            public void remove() { throw new UnsupportedOperationException(); }

            @Override
            public void close() {}

            @Override
            public int totalRecords() { return recordSet.getNumItems(); }
        };
    }
    
    private static Matchable item2Matchable(RecordSetItemIFace item)
    {
//      CollectionObject obj = session.get(CollectionObject.class, item.getRecordId());
      
//      String id = GenericGUIDGeneratorFactory.getInstance().createGUID(
//              GenericGUIDGeneratorFactory.CATEGORY_TYPE.Specimen,
//              obj.getCatalogNumber());
      
        return new MatchableIndexedId(item.getRecordId().toString() + "-KANU");
    }
}
