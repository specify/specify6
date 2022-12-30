/* Copyright (C) 2022, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Provides data by executing an hql statement for a QueryBuilder query.
 *
 */
public class QBDataSource extends QBDataSourceBase implements CustomQueryListener
{
    protected static final Logger log = Logger.getLogger(QBDataSource.class);
    
    protected final AtomicBoolean loadCancelled = new AtomicBoolean(false);
    
    protected JPAQuery theQuery = null;
    protected boolean firstRow = true;
    protected int currentRow = 0;
    protected Long firstResult = 0L;
    protected Long maxResults = 0L;
    
    protected boolean removeReadRows = true; //if true remove rows after they are iterated. A memory-conservation thing.
    
    /**
     * hql that produces the data.
     */
    protected final String hql;
    /**
     * name-value list of parameters for the hql query.
     */
    protected final List<Pair<String, Object>> params;
    
    /**
     * sort to be applied AFTER data is retrieved.
     */
    protected final List<SortElement> sort;
    
    protected final int smusherColIdx;
    protected final int smusherRecIdIdx;
    
    /**
     * true if data has been pre-processed (i.e. sorted).
     */
    protected boolean processed = false;
    
    protected AtomicBoolean processing = new AtomicBoolean(true);
    
    /**
     * stores processed data;
     */
    protected Vector<Vector<Object>> cache = null;
    
    /**
     * stores size of the data set.
     */
    protected final AtomicInteger resultSetSize = new AtomicInteger(-1);
    /**
     * column values for the current record.
     */
    protected Object rowVals = null;
    /**
     * iterator of records in the data source.
     */
    protected final AtomicReference<Iterator<?>> rows = new AtomicReference<Iterator<?>>(null);
    
    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    @Override
    public Object getFieldValue(JRField arg0) throws JRException
    {
        //XXX - what if user-defined 'resultsetsize' or 'id' fields exist???
    	if (arg0.getName().equalsIgnoreCase("resultsetsize"))
        {
        	return String.valueOf(resultSetSize.get());  //currently returned as a string for convenience.
        }
        if (arg0.getName().equalsIgnoreCase("ID"))
        {
        	return getRecordId();
        }
    	
    	boolean logIt = rows.get() == null || processing.get();
        if (logIt)
        {
            log.debug(this + " waiting for rows...");
        }
        //XXX Bad Code Alert!
        while (processing.get() || rows.get() == null) { 
            /*wait till done executing the query. (forever and ever??)
             * Do we know that exectionDone and executionError will be called on different threads?
             * */
        }
        if (logIt)
        {
            log.debug("... " + this + " got rows");
        }
        int fldIdx = getFldIdx(arg0.getName());
        return getFieldValue(fldIdx, arg0.getName(), arg0.getClass(), null);
    }
    
    public Object getFieldValueForRow(int colIdx, Object forRowVals)
    {
    	return getFieldValue(colIdx, null, null, forRowVals);
    }
    
    public Object getFieldValue(int colIdx)
    {
    	return getFieldValue(colIdx, null, null, null);
    }
    
    public int getFieldCount()
    {
    	return this.columnInfo.size() + (this.recordIdsIncluded ? 1 : 0);
    }
    
    @SuppressWarnings("unchecked")
    protected Object getFieldValue(final int fldIdx, final String fldName, final Class<?> fldClass, final Object forRowVals)
    {
        Object theRow = forRowVals == null ? rowVals : forRowVals;
    	if (fldIdx < 0)
           return null;
        
        boolean isRawCol = fldName == null; //isRawCol assumes no additional column info - partial date or other stuff.
        int colInfoIdx; 
        if (isRawCol)
        {
        	colInfoIdx = fldIdx - 1;
        }
        else 
        {
        	colInfoIdx = Collections.binarySearch(colNames, new SourceColumnInfo(fldName, null, null), srcColNameComparator);
        	if (colInfoIdx < 0)
        	{
        		if (fldClass.equals(String.class))
        		{
        			return String.format(UIRegistry.getResourceString("QBJRDS_UNKNOWN_FIELD"), fldName);
        		}
        		log.error("field not found: " + fldName + " (" + fldIdx + ")");
        		return null;
        	}
        }
        
        boolean skipProcessing = isRawCol && fldIdx == 0; //it's a request for the id field.
        
        //adjust fldIdx to account for additional 'hidden' cols such as those for PartialDatePrecision values.
        int adjustedFldIdx = fldIdx;
        if (isRawCol)
        {
        	for (int c = 0; c < colInfoIdx; c++)
        	{
        		ERTICaptionInfoQB col = columnInfo.get(c);
        		if (col.getColInfoList() != null && col.getColInfoList().size() > 0)
        		{
        			adjustedFldIdx += col.getColInfoList().size() - 1;
        		}
        	}
        }
        if (!processed && !skipProcessing)
        {
            int processIdx = isRawCol ? colInfoIdx : colNames.get(colInfoIdx).getColInfoIdx();
            ERTICaptionInfoQB col = columnInfo.get(processIdx);
            Object value = null;
            if (col.getColInfoList() != null && col.getColInfoList().size() > 1)
            {
            	//Then assume the values for the fields in the colInfo list are
            	//stored consecutively in the resultset.
            	value = new Object[col.getColInfoList().size()];
            	((Object[] )value)[0] = ((Object[] )theRow)[adjustedFldIdx];
            	for (int i = 1; i < col.getColInfoList().size(); i++)
            	{
            		((Object[] )value)[i] = ((Object[] )theRow)[adjustedFldIdx+i];
            	}
            }
            else
            {
                Object[] rowArray = (Object[])theRow;
                if (rowArray != null && adjustedFldIdx > -1 && adjustedFldIdx < rowArray.length)
                {
                    value = ((Object[])theRow)[adjustedFldIdx];
                }
            }
            return processValue(processIdx, col.processValue(value));
        }
        if (!processed)
        {
        	return ((Object[] )theRow)[adjustedFldIdx];
        }
        //else processing already done
        //int colIdx = this.recordIdsIncluded ? fldIdx - 1 : fldIdx;       
        return ((Vector<Object> )theRow).get(adjustedFldIdx);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#getNext()
     */
    @Override
    public boolean getNext() 
    {
        boolean logIt = rows.get() == null || processing.get();
        if (logIt)
        {
            log.debug(this + " waiting for rows...");
        }
        //XXX Bad Code Alert!
        while (rows.get() == null || processing.get()) { 
            /*wait till done executing the query. (forever and ever??)
             * Do we know that exectionDone and executionError will be called on different threads?
             * */
        }
        if (logIt)
        {
            log.debug("... " + this + " got rows");
        }
        

        currentRow++;
        boolean result = doGetNext(false);
        if (result)
        {
            for (QBDataSourceListenerIFace listener : listeners)
            {
            	if (listener.isListeningClosely())
            	{
            		listener.currentRow(currentRow);
            	}
            }
        }
        else
        {
            if (loadCancelled.get())
            {
            	return true; //this is to work around JasperReports behavior.
            				//returning true prevents "Document Contained No Pages" message
            }
        	for (QBDataSourceListenerIFace listener : listeners)
            {
            	if (listener.isListeningClosely())
            	{
            		listener.done(currentRow);
            	}
            }
        }
        return result;
    }

    protected synchronized boolean doGetNext(final boolean sorting)
    {
//        if (loadCancelled.get())
//        {
//        	return false;
//        }
        
    	if (rows.get().hasNext())
        {
            if (!firstRow && removeReadRows)
            {
                if (!sorting)
                {
                    try 
                    {
                    	rows.get().remove();
                    } catch (IllegalStateException isex)
                    {
                    	log.warn(isex);
                    } catch (UnsupportedOperationException onsex)
                    {
                    	log.warn(onsex);
                    }
                }
            }
            else
            {
                firstRow = false;
            }
            
            Object nextRow = rows.get().next();
            if (Object[].class.isAssignableFrom(nextRow.getClass()))
            {
                rowVals = nextRow;
            }
            else if (Vector.class.isAssignableFrom(nextRow.getClass()))
            {
                rowVals = nextRow;
            }
            else
            {
                //if only one column...
                rowVals = new Object[1];
                ((Object[] )rowVals)[0] = nextRow;
            }
            return true;
        }
        rowVals = null;
        rows.set(null);
        if (theQuery != null)
        {
        	//clear out pointers and caches to aid garbage collection
        	theQuery.getDataObjects().clear();
        	if (theQuery.getDataObjects() instanceof ArrayList)
        	{
        		((ArrayList<?> )theQuery.getDataObjects()).trimToSize();
        	}
        	theQuery.clearCql();
        	//theQuery = null;
        }
        return false;
    }
    
    public synchronized Object getCurrentRow()
    {
    	return rowVals;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#getRepeaterRowVals()
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object[] getRepeaterRowVals()
    {
        if (Vector.class.isAssignableFrom(rowVals.getClass()))
        {
            Object[] result = new Object[((Vector<Object>) rowVals).size()];
            for (int r = 0; r < ((Vector<Object> )rowVals).size(); r++)
            {
                result[r] = ((Vector<Object> )rowVals).get(r);
            }
            return result;
        }
        return (Object[] )rowVals;
    }

    /**
     * @return true if need to process all results before sending to consumers
     */
    protected boolean needToPreProcess()
    {
    	return needToSort() || needToSmush();
    }
    
    /**
     * @return
     */
    protected boolean needToSort() {
    	return sort != null && sort.size() > 0;
    }
    
    /**
     * @return
     */
    protected boolean needToSmush() {
    	return smusherColIdx != -1;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    @Override
    public void exectionDone(CustomQueryIFace customQuery)
    {
        resultSetSize.set(((JPAQuery)customQuery).getDataObjects().size()); 
        for (QBDataSourceListenerIFace listener : listeners)
        {
        	listener.rowCount(resultSetSize.get());
        }
        rows.set(((JPAQuery)customQuery).getDataObjects().iterator());
        //cache rows and sort
        if (needToPreProcess())
        {
            cache = new Vector<Vector<Object>>(resultSetSize.get());
            for (QBDataSourceListenerIFace listener : listeners)
            {
            	listener.loading();
            }
            
            while (doGetNext(true))
            {
                for (QBDataSourceListenerIFace listener : listeners)
                {
                	if (listener.isListeningClosely())
                	{
                		listener.currentRow(++currentRow);
                	}
                }
                Vector<Object> row = new Vector<Object>(((Object[] )rowVals).length);
                for (int fldIdx = 0, colIdx = 0; fldIdx < ((Object[] )rowVals).length; fldIdx++, colIdx++)
                {
                	if (recordIdsIncluded && fldIdx == 0)
                	{
                		row.add(((Object[] )rowVals)[fldIdx]);
                		colIdx--;
                	}
                	else
                	{
                		ERTICaptionInfoQB col = this.columnInfo.get(colIdx);
                		Object value;
                        if (col.getColInfoList() != null && col.getColInfoList().size() > 1)
                        {
                        	//Then assume the values for the fields in the colInfo list are
                        	//stored consecutively in the resultset.
                        	value = new Object[col.getColInfoList().size()];
                        	((Object[] )value)[0] = ((Object[] )rowVals)[fldIdx];
                        	for (int i = 1; i < col.getColInfoList().size(); i++)
                        	{
                        		((Object[] )value)[i] = ((Object[] )rowVals)[fldIdx+i];
                        	}
                        	fldIdx += col.getColInfoList().size() - 1;
                        }
                		else
                		{
                			value = ((Object[] )rowVals)[fldIdx];
                		}
                		row.add(processValue(colIdx, col.processValue(value)));
                	}
                }
                cache.add(row);
            }
            this.setUpColNamesPostProcess();
            if (needToSort()) {
            	Collections.sort(cache, new ResultRowComparator(sort, recordIdsIncluded, columnInfo));
            }
            if (needToSmush()) {
        		Smusher s = new Smusher(cache, smusherColIdx, smusherRecIdIdx);
        		this.cache = s.smush();
            }
            processed = true;
            firstRow = true;
            currentRow = 0;
            rows.set(cache.iterator());
            for (QBDataSourceListenerIFace listener : listeners)
            {
            	listener.loaded();
            }
        }
        processing.set(false);
        for (QBDataSourceListenerIFace listener : listeners)
        {
        	listener.filling();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    @Override
    public void executionError(CustomQueryIFace customQuery)
    {
        rows.set(new ArrayList<Object>().iterator());
    }

    /**
     * @param hql
     * @param params
     * @param columnInfo
     * @param recordIdsIncluded
     */
    public QBDataSource(final String hql, final List<Pair<String, Object>> params, final List<SortElement> sort, 
                          final List<ERTICaptionInfoQB> columnInfo,
                          final boolean recordIdsIncluded)
    {
        super(columnInfo, recordIdsIncluded, null);
        this.hql = hql;
        this.params = params;
        this.sort = sort;
        //XXX assuming no smushing for exports, easy enough to add with some work in tools.export.ExportPanel.java
        this.smusherColIdx = -1;
        this.smusherRecIdIdx = -1;
    }
    
    /**
     * @param hql
     * @param params
     * @param columnInfo
     * @param recordIdsIncluded
     * @param repeatCount - number of repeats for each record
     */
    public QBDataSource(final String hql, final List<Pair<String, Object>> params, final List<SortElement> sort,
                          final List<ERTICaptionInfoQB> columnInfo,
                          final boolean recordIdsIncluded, final Object repeats,
                          final int smusherColIdx, final int smusherRecIdIdx)
    {
        super(columnInfo, recordIdsIncluded, repeats);
        this.hql = hql;
        this.params = params;
        this.sort = sort;
        this.smusherColIdx = smusherColIdx;
        this.smusherRecIdIdx = smusherRecIdIdx;
    }    
    
    /**
     * 
     */
    public void startDataAcquisition()
    {
        firstRow = true;
        currentRow = 0;
        theQuery = new JPAQuery(hql, this);
        
        if (firstResult > 0) 
        {
        	theQuery.setFirstResult(firstResult.intValue());
        }
        if (maxResults > 0)
        {
        	theQuery.setMaxResults(maxResults.intValue());
        }
        theQuery.setParams(params);
        theQuery.setIsModified(false);
        theQuery.start();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#getRecordId()
     */
    @Override
    public Object getRecordId()
    {
        //XXX what if processed???? does this EVER get called??
        if (!recordIdsIncluded) {
            return super.getRecordId();
        } else {
        	Object result = null;
            if (Vector.class.isAssignableFrom(rowVals.getClass())) {
            	result = ((List<?>)rowVals).get(0);
            } else {
            	result = ((Object[])rowVals)[0];
            }
        	if (result instanceof List<?>) {
        		return ((List<?>)result).get(0);
        	} else {
        		return result;
        	}
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#getFldIdx(java.lang.String)
     */
    @Override
    protected int getFldIdx(String fldName)
    {
        int result = super.getFldIdx(fldName);
        return recordIdsIncluded ? result + 1 : result;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#hasResultSize()
     */
    @Override
    public boolean hasResultSize()
    {
        return this.resultSetSize.get() != -1;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#size()
     */
    @Override
    public int size()
    {
        //this does not take repeats into account.
        return this.resultSetSize.get();
    }

	/**
	 * cancels the "pre-processing" loop in the exectionDone method.
	 */
	public synchronized void cancelLoad()
	{
		loadCancelled.set(true);
		Iterator<?> r = rows.get();
		while (r.hasNext())
		{
			r.next();
		}
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#updateNewListener(edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceListenerIFace)
	 */
	@Override
	protected void updateNewListener(QBDataSourceListenerIFace listener)
	{
		super.updateNewListener(listener);
		if (processing.get())
		{
			listener.loading();
		}
		else if (currentRow < size())
		{
			listener.filling();
		}
	}

	/**
	 * @return the firstRecord
	 */
	public long getFirstResult() 
	{
		return firstResult;
	}

	/**
	 * @param firstRecord the firstRecord to set
	 */
	public void setFirstResult(long firstRecord) 
	{
		this.firstResult = firstRecord;
	}

	/**
	 * @return the maxRecords
	 */
	public long getMaxResults() 
	{
		return maxResults;
	}

	/**
	 * @param maxRecords the maxRecords to set
	 */
	public void setMaxResults(long maxRecords) 
	{
		this.maxResults = maxRecords;
	}

	/**
	 * @return the removeReadRows
	 */
	public boolean isRemoveReadRows() 
	{
		return removeReadRows;
	}

	/**
	 * @param removeReadRows the removeReadRows to set
	 */
	public void setRemoveReadRows(boolean removeReadRows) 
	{
		this.removeReadRows = removeReadRows;
	}
	
	
    
}
