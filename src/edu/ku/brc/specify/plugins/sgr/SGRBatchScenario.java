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

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.sgr.BatchMatcher;
import edu.ku.brc.sgr.Matchable;
import edu.ku.brc.sgr.SGRMatcher;
import edu.ku.brc.sgr.datamodel.AccumulateResults;
import edu.ku.brc.sgr.datamodel.BatchMatchResultSet;
import edu.ku.brc.sgr.datamodel.DataModel;
import edu.ku.brc.sgr.datamodel.MatchConfiguration;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: Apr 19, 2011
 *
 */
public class SGRBatchScenario
{
    private SwingWorker<Integer, Integer> worker     = null;
    private final List<Runnable>          onFinished = new LinkedList<Runnable>();
    private final SGRMatcher              matcher;
    private BatchMatchResultSet           resultSet  = null;
    private final RecordGenerator         recordGenerator;
    private boolean                       started    = false;
    private AccumulateResults             accumulator;
    private BatchMatcher                  batchMatcher;

    public static interface RecordGenerator extends Iterator<Matchable>
    {
        public int totalRecords();
        public void close();
    }
    
    public SGRBatchScenario(BatchMatchResultSet resultSet, RecordGenerator recordGenerator)
    {
        this.resultSet = resultSet;
        this.recordGenerator = recordGenerator;
       
        matcher = setupMatcher(resultSet.getMatchConfiguration());
    }
    
    public SGRBatchScenario(RecordSetIFace recordSet, MatchConfiguration matchConfig, 
                            RecordGenerator recordGenerator, 
                            String sourceName, Long sourceId)
    {
        this.recordGenerator = recordGenerator;
        matcher = setupMatcher(matchConfig);
        String name = sourceName + " " + matchConfig.name(); 
        
        resultSet = 
            DataModel.createBatchMatchResultSet(name, matcher,
                sourceId, recordSet.getDbTableId(), matchConfig.id());
    }
    
    public SGRBatchScenario(MatchConfiguration matchConfig, 
                            RecordGenerator recordGenerator, 
                            String sourceName, Long sourceId, Integer dbTableId)
    {
        this.recordGenerator = recordGenerator;
        matcher = setupMatcher(matchConfig);
        String name = sourceName + " " + matchConfig.name(); 
        
        resultSet = 
            DataModel.createBatchMatchResultSet(name, matcher,
                sourceId, dbTableId, matchConfig.id());
    }
    
    private SGRMatcher setupMatcher(MatchConfiguration matchConfig)
    {
        SGRMatcher.Factory matcherFactory = matchConfig.createMatcherFactory();
        matcherFactory.docSupplied = true;
        matcherFactory.debugQuery = false;
        try { return matcherFactory.build(); } 
        catch (MalformedURLException e) { throw new RuntimeException(e); }
    }

    public BatchMatchResultSet getResultSet()
    {
        return resultSet;
    }
    
    public void start()
    {
        if (started)
            throw new IllegalStateException("already started");
        
        started = true;
        accumulator = new AccumulateResults(matcher, resultSet);
        batchMatcher = new BatchMatcher(recordGenerator, accumulator, 4);

        worker = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                try
                {
                    batchMatcher.run();
                } 
                finally
                {
                    worker = null;
                    recordGenerator.close();
                    for (Runnable r : onFinished)
                    {
                        SwingUtilities.invokeLater(r);
                    }
                }
                return null;
            }
            
            @Override
            protected void done() 
            { 
                // Get SwingWorker to vomit up any exceptions it swallowed.
                // ... except for interrupted.
                try { get(); }
                catch (CancellationException e) {}
                catch (InterruptedException e) {}
                catch (ExecutionException e) 
                { 
                    Logger.getLogger(SGRBatchScenario.class)
                        .error("Failed to fetch SGR results from SGR server.", e);
                    UIRegistry.loadAndPushResourceBundle("specify_plugins");
                    UIRegistry.showLocalizedError("SGR_ERROR_SERVER_FAIL");
                    UIRegistry.popResourceBundle();
                }
            }
        };
    
        worker.execute();
    }
    
    public boolean isRunning()
    {
        return worker != null;
    }
    
    public void abort()
    {
        if (isRunning())
            worker.cancel(true);
        else
            recordGenerator.close();
    }
    
    public void addOnFinished(Runnable func)
    {
        onFinished.add(func);
    }
    
    public int totalRecords()
    {
        if (!started)
            throw new IllegalStateException("not started");
        
        return recordGenerator.totalRecords();
    }
    
    public int finishedRecords()
    {
        if (!started) 
            throw new IllegalStateException("not started");
        return accumulator.nCompleted();
    }
}
