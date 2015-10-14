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
/**
 * 
 */
package edu.ku.brc.specify.plugins.sgr;

import java.util.Iterator;
import java.util.Set;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.sgr.Matchable;
import edu.ku.brc.sgr.datamodel.BatchMatchResultSet;
import edu.ku.brc.sgr.datamodel.MatchConfiguration;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.tasks.WorkbenchTask;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: May 12, 2011
 *
 */
public class WorkBenchBatchMatch
{
    public static SGRBatchScenario resumeScenario(BatchMatchResultSet resultSet)
    {
        final DataProviderSessionIFace mySession = DataProviderFactory.getInstance().createSession();
        Workbench workbench = WorkbenchTask.loadWorkbench((int)(long)resultSet.getRecordSetId(), mySession, false);
        Workbench2SGR workbench2SGR = new Workbench2SGR(workbench);

        return new SGRBatchScenario(resultSet, recordGenerator(workbench, workbench2SGR, mySession));
    }
    
    public static SGRBatchScenario newScenario(RecordSetIFace recordSet, MatchConfiguration matchConfig)
    {
        final DataProviderSessionIFace mySession = DataProviderFactory.getInstance().createSession();
        Workbench workbench = WorkbenchTask.loadWorkbench(recordSet.getOnlyItem().getRecordId(), mySession, false);
        Workbench2SGR workbench2SGR = new Workbench2SGR(workbench);
        
        return new SGRBatchScenario(recordSet, matchConfig, 
                recordGenerator(workbench, workbench2SGR, mySession),
                workbench.getName(), 
                Long.valueOf(workbench.getId()));
    }

    public static SGRBatchScenario newScenario(int workbenchId, MatchConfiguration matchConfig)
    {
        final DataProviderSessionIFace mySession = DataProviderFactory.getInstance().createSession();
        Workbench workbench = WorkbenchTask.loadWorkbench(workbenchId, mySession, false);
        Workbench2SGR workbench2SGR = new Workbench2SGR(workbench);
        
        return new SGRBatchScenario(matchConfig, 
                    recordGenerator(workbench, workbench2SGR, mySession),
                    workbench.getName(), 
                    Long.valueOf(workbench.getId()),
                    workbench.getDbTableId());
    }
    
    private static SGRBatchScenario.RecordGenerator recordGenerator(final Workbench workbench, 
            final Workbench2SGR workbench2sgr, final DataProviderSessionIFace session)
    {
        return new SGRBatchScenario.RecordGenerator()
        {
            Set<WorkbenchRow> rows = workbench.getWorkbenchRows();
            Iterator<WorkbenchRow> iter = rows.iterator();
            final int total = rows.size();
            
            @Override
            public Matchable next()
            {
                return workbench2sgr.row2SgrRecord(iter.next()).asMatchable();
            }

            @Override
            public boolean hasNext() { return iter.hasNext(); }

            @Override
            public void remove() { throw new UnsupportedOperationException(); }

            @Override
            public int totalRecords() { return total; }
            
            @Override
            public void close() { session.close(); }
        };
    }
}
