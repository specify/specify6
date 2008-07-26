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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.util.Pair;

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
    private static final Logger log = Logger.getLogger(QBJRDataSourceBase.class);
    
    protected static DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");    
    protected final List<ERTICaptionInfoQB> columnInfo;
    protected final boolean recordIdsIncluded;
    protected final ArrayList<Pair<String, Integer>> colNames = new ArrayList<Pair<String, Integer>>();
    /**
     * Sends repeats of rows to consumer of this source.
     */
    protected final RowRepeater repeater;
    /**
     * Number of repeats of the currently row.
     */
    protected int currentRowRepeats = 0;

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
        if (currentRowRepeats > 0)
        {
            currentRowRepeats--;
            return true;
        }
        boolean result = getNext();
        if (result)
        {
        if (repeater != null)
        {
            currentRowRepeats = repeater.repeats(getRepeaterRowVals()) - 1;
        }
        else
        {
            currentRowRepeats = 0;
        }
        }
        return result;
    }

    /**
     * @return true if next row exists.
     */
    protected boolean getNext()
    {
        return false;
    }
    
    /**
     * @return array of row vals for processing by repeater.
     */
    protected Object[] getRepeaterRowVals()
    {
        return null;
    }
    
    /**
     * @param columnInfo
     */
    public QBJRDataSourceBase(final List<ERTICaptionInfoQB> columnInfo, final boolean recordIdsIncluded, final Object repeats)
    {
        this.columnInfo = columnInfo;
        this.recordIdsIncluded = recordIdsIncluded;
        int c = 0;
        for (ERTICaptionInfoQB col : this.columnInfo)
        {
            String lbl = col.getColLabel();
            if (col instanceof ERTICaptionInfoRel) //lame, but avoid having to modify ERTICaptionInfo this way.
            {
                lbl = RelQRI.stripDescriptiveStuff(lbl);
            }
            colNames.add(new Pair<String, Integer>(QueryBldrPane.fixFldNameForJR(lbl), new Integer(c++)));
        }
        Collections.sort(colNames, colPairComparator);
        if (repeats == null)
        {
            repeater = null;
        }
        else if (repeats instanceof String)
        {
            //assuming repeatColumnName does not refer to a formatted or aggregated column-
            //also assuming valid columnName-
            this.repeater = new RowRepeaterColumn(getFldIdx((String )repeats));
        }
        else if (repeats instanceof Integer)
        {
            repeater = new RowRepeaterConst((Integer )repeats);
        }
        else 
        {
            repeater = null;
            log.error("invalid repeats parameter: " + repeats);
        }
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
            return formatter.formatToUI(obj);
        }
        
        return obj;
    }
    
    public Object getRecordId()
    {
        return null;
    }
}
