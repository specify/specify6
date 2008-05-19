/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.ui.db.ERTICaptionInfo;
import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Provides data by executing an hql statement for a QueryBuilder query.
 *
 */
public class QBJRDataSource extends QBJRDataSourceBase implements CustomQueryListener
{
    protected static final Logger log = Logger.getLogger(QBJRDataSource.class);

    /**
     * hql that produces the data.
     */
    protected final String hql;
    /**
     * name-value list of parameters for the hql query.
     */
    protected final List<Pair<String, Object>> params;
    /**
     * stores size of the data set.
     */
    protected final AtomicInteger resultSetSize = new AtomicInteger();
    /**
     * column values for the current record.
     */
    protected Object[] rowVals = null;
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
        //XXX - what if user-defined 'resultsetsize' field exists???
    	if (arg0.getName().equalsIgnoreCase("resultsetsize"))
        {
        	return String.valueOf(resultSetSize);  //currently returned as a string for convenience.
        }
    	
    	//XXX Bad Code Alert!
        while (rows.get() == null) { 
            /*wait till done executing the query. (forever and ever??)
             * Do we know that exectionDone and executionError will be called on different threads?
             * */
        }
        int fldIdx = getFldIdx(arg0.getName());
        if (fldIdx < 0)
            return null;
        
        return processValue(fldIdx, columnInfo.get(fldIdx).processValue(rowVals[fldIdx]));
    }
    
    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#next()
     */
    @Override
    public boolean next() throws JRException
    {
        //XXX Bad Code Alert!
        while (rows.get() == null) { 
            /*wait till done executing the query. (forever and ever??)
             * Do we know that exectionDone and executionError will be called on different threads?
             * */
        }
        if (rows.get().hasNext())
        {
            rowVals = (Object[])rows.get().next();
            return true;
        }
        rowVals = null;
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    //@Override
    public void exectionDone(CustomQueryIFace customQuery)
    {
        resultSetSize.set(((JPAQuery)customQuery).getDataObjects().size()); 
    	rows.set(((JPAQuery)customQuery).getDataObjects().iterator());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    //@Override
    public void executionError(CustomQueryIFace customQuery)
    {
        rows.set(new ArrayList<Object>().iterator());
    }

    /**
     * @param hql
     * @param columnInfo
     */
    public QBJRDataSource(final String hql, final List<Pair<String, Object>> params, final List<ERTICaptionInfo> columnInfo)
    {
        super(columnInfo);
        this.hql = hql;
        this.params = params;
        startDataAcquisition();
    }
    
    /**
     * 
     */
    protected void startDataAcquisition()
    {
        JPAQuery q = new JPAQuery(hql, this);
        q.setParams(params);
        q.start();
    }

}
