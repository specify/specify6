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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import edu.ku.brc.af.core.expresssearch.ERTICaptionInfo;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Initial shot at running a report from a query.
 *
 */
public class QBJRDataSource implements JRDataSource, CustomQueryListener
{
    protected final String hql;
    protected final List<ERTICaptionInfo> columnInfo;
    protected final List<Pair<String, Integer>> colNames = new ArrayList<Pair<String, Integer>>();
    protected List<?> results;
    protected final Comparator<Pair<String, Integer>> colPairComparator = 
        new Comparator<Pair<String, Integer>>()
        {

            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
            {
                return o1.getFirst().compareTo(o2.getFirst());
            }
            
        };
    protected Object[] rowVals = null;
    protected final AtomicReference<Iterator<?>> rows = new AtomicReference<Iterator<?>>(null);
    
    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    @Override
    public Object getFieldValue(JRField arg0) throws JRException
    {
        //XXX Bad Code Alert!
        while (rows.get() == null) { 
            /*wait till done executing the query. (forever and ever??)
             * Do we know that exectionDone and executionError will be called on different threads?
             * */
        }
        int fldIdx = Collections.binarySearch(colNames, 
                    new Pair<String, Integer>(arg0.getName(), null),
                    colPairComparator);
        if (fldIdx < 0) 
            return null;
        if (rowVals == null || rowVals[fldIdx] == null)
            return null;
        System.out.println(rowVals[fldIdx].toString());
        return rowVals[fldIdx].toString();
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
    @Override
    public void exectionDone(CustomQueryIFace customQuery)
    {
        rows.set(((JPAQuery)customQuery).getDataObjects().iterator());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQueryIFace)
     */
    @Override
    public void executionError(CustomQueryIFace customQuery)
    {
        rows.set(new ArrayList<Object>().iterator());
    }

    public QBJRDataSource(final String hql, final List<ERTICaptionInfo> columnInfo)
    {
        this.hql = hql;
        this.columnInfo = columnInfo;
        int c = 0;
        for (ERTICaptionInfo col : this.columnInfo)
        {
            colNames.add(new Pair<String, Integer>(col.getColLabel(), new Integer(c++)));
        }
        Collections.sort(colNames, colPairComparator);
        startDataAcquisition();
    }
    
    /**
     * 
     */
    protected void startDataAcquisition()
    {
        new JPAQuery(hql, this).start();
    }

}
