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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.db.ERTICaptionInfo;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.util.Pair;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Base class for implementations of JRDataSource for QueryBuilder queries.
 *
 */
public class QBJRDataSourceBase implements JRDataSource
{
    protected static DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");    
    protected final List<ERTICaptionInfo> columnInfo;
    protected final ArrayList<Pair<String, Integer>> colNames = new ArrayList<Pair<String, Integer>>();
    protected final Comparator<Pair<String, Integer>> colPairComparator = 
        new Comparator<Pair<String, Integer>>()
        {

            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            //@Override
            public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
            {
                return o1.getFirst().compareTo(o2.getFirst());
            }
            
        };

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    //@Override
    public Object getFieldValue(JRField arg0) throws JRException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#next()
     */
    //@Override
    public boolean next() throws JRException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param columnInfo
     */
    public QBJRDataSourceBase(final List<ERTICaptionInfo> columnInfo)
    {
        this.columnInfo = columnInfo;
        int c = 0;
        for (ERTICaptionInfo col : this.columnInfo)
        {
            colNames.add(new Pair<String, Integer>(QueryBldrPane.fixFldNameForJR(col.getColLabel()), new Integer(c++)));
        }
        Collections.sort(colNames, colPairComparator);
    }
    
    /**
     * @param fldName
     * @return index for column named fldName.
     */
    protected int getFldIdx(final String fldName)
    {
        int fldIdx = Collections.binarySearch(colNames, new Pair<String, Integer>(fldName, null),
                colPairComparator);
        if (fldIdx < 0)
            return -1;
        return colNames.get(fldIdx).getSecond();
    }
    
    /**
     * @param fldIdx
     * @param obj
     * @return Possibly formatted version of obj.
     * 
     * Adds formatting, if necessary, for columns that aren't represented by ERTIRelCaptionInfo Objects.
     * (Might be better to create a ERTICaptionInfo descendant with a processValue to do this for QueryBuilder query columns 
     * that don't represent relationships.)
     *
     * This code may be affected by changes to QBJRDataSourceConnection.getColClass, and vice-versa.
     */
    protected Object processValue(final int fldIdx, final Object obj)
    {    
        if (obj instanceof Calendar)
        {
            return scrDateFormat.format((Calendar)obj);
        
        } else if (obj instanceof Timestamp )
        {
            return scrDateFormat.format((Date)obj);
        } else if (obj instanceof java.sql.Date || obj instanceof Date )
        {
            return scrDateFormat.format((Date)obj);
        }
    
        UIFieldFormatterIFace formatter = columnInfo.get(fldIdx).getUiFieldFormatter();
        if (formatter != null && formatter.isInBoundFormatter())
        {
            return formatter.formatInBound(obj);
        }
        
        return obj;
    }
}
