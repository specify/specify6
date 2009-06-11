/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.List;
import java.util.Vector;

import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 *This class is used when exporting query results to a database.
 *There is no need for much of the stuff in QBJRDataSource,
 *But overriding seemed to be the best way to run a query and process the results with minimal ui overhead.
 * 
 * Might be better to somehow lift the CustomQueryListener stuff out of QBJRDataSource and then subclass that.
 * Or to modify QBResultSetTableModel somehow.
 *
 */
public class QBExportDataSource extends QBJRDataSource
{
    public QBExportDataSource(final String hql, final List<Pair<String, Object>> params, final List<SortElement> sort, 
            final List<ERTICaptionInfoQB> columnInfo,
            final boolean recordIdsIncluded)
    {
    	super(hql, params, sort, columnInfo, recordIdsIncluded);
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSource#needToPreProcess()
	 */
	@Override
	protected boolean needToPreProcess()
	{
		return true;
	}

	/**
	 * @return the pre-processed rows.
	 */
	public Vector<Vector<Object>> getCache()
	{
        //XXX Bad Code Alert!
        while (rows.get() == null || processing.get()) { 
            /*wait till done executing the query. (forever and ever??)
             * Do we know that exectionDone and executionError will be called on different threads?
             * */
        }
		return cache;
	}
    
}
