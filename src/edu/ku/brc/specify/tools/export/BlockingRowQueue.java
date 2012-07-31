/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;

/**
 * @author timo
 *
 *Stores Futures for the getFieldValue() methods for fields in QBDataSource objects.
 *Each item in the queue is a list of futures - one future per QBDataSource column.
 */
@SuppressWarnings("serial")
class BlockingRowQueue extends ArrayBlockingQueue<List<Future<?>>> 
{
	boolean done = false;
	
	/**
	 * @param capacity
	 */
	public BlockingRowQueue(int capacity)
	{
		super(capacity);
	}
	
	/**
	 * Called by RowFiller when all rows have been processed.
	 */
	public void finish()
	{
		synchronized(this)
		{
			done = true;
		}
	}
	
	/**
	 * @return
	 */
	public boolean isFinished()
	{
		return done;
	}
}
