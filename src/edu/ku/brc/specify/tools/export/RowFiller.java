/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.tasks.subpane.qb.QBDataSource;

/**
 * @author timo
 *
 *This class creates threads for the getFieldValue() method for each cell in a QBDatasource.
 *Lists of futures for the cells are fed to a BlockingRowQueue.
 */
class RowFiller implements Runnable 
{
	private static final Logger log = Logger.getLogger(RowFiller.class);

	private final BlockingRowQueue queue;
	private final QBDataSource rows;
	private final ExecutorService executor;
	
	/**
	 * @param q
	 * @param rows
	 */
	RowFiller(BlockingRowQueue q, QBDataSource rows) 
	{ 
		queue = q; 
		this.rows = rows;
		executor = Executors.newFixedThreadPool(16);
	}
	   
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() 
	{
		try 
		{
	       //int filled = 0;
			while (rows.getNext()) 
			{ 
				queue.put(fillRow());
				//filled++;
				//System.out.println("RowFiller: filled " + filled);
			}
			queue.finish();
	   } catch (InterruptedException ex) 
	   { 
           edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
           edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RowFiller.class, ex);
           log.error(ex);
		   //ex.printStackTrace();
	   }
	}
	
	/**
	 * @return
	 */
	protected List<Future<?>> fillRow() 
	{
		List<Future<?>> result = new ArrayList<Future<?>>(rows.getFieldCount());
		final Object row = rows.getCurrentRow();
		for (int r=0; r < rows.getFieldCount(); r++)
		{
			final int col = r;
			result.add(executor.submit(new Callable<Object>() {
				/* (non-Javadoc)
				 * @see java.util.concurrent.Callable#call()
				 */
				@Override
				public Object call() throws Exception {
					return rows.getFieldValueForRow(col, row);
				}
				
			}));
		}
		return result;
	}
}
