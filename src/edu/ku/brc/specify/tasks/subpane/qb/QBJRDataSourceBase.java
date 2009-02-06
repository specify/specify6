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
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.DateWrapper;

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
    protected final ArrayList<SourceColumnInfo> colNames = new ArrayList<SourceColumnInfo>();
    /**
     * Sends repeats of rows to consumer of this source.
     */
    protected final RowRepeater repeater;
    /**
     * Number of repeats of the currently row.
     */
    protected int currentRowRepeats = 0;

    protected final Comparator<SourceColumnInfo> colPairComparator = 
        new Comparator<SourceColumnInfo>()
        {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            //@Override
            public int compare(SourceColumnInfo o1, SourceColumnInfo o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
            
        };

        
    class SourceColumnInfo
    {
    	protected final String name;
    	protected final Integer rowDataIdx;
    	protected final Integer colInfoIdx;
    	
    	public SourceColumnInfo(final String name, final Integer rowDataIdx, final Integer colInfoIdx)
    	{
    		this.name = name;
    		this.rowDataIdx = rowDataIdx;
    		this.colInfoIdx = colInfoIdx;
    	}

		/**
		 * @return the name
		 */
		public String getName() 
		{
			return name;
		}

		/**
		 * @return the rowDataIdx
		 */
		public Integer getRowDataIdx() 
		{
			return rowDataIdx;
		}

		/**
		 * @return the colInfoIdx
		 */
		public Integer getColInfoIdx() 
		{
			return colInfoIdx;
		}
    }
    
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
        setUpCollNames();
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
     * creates mapping for fldnames to row and caption column indices.
     */
    protected void setUpCollNames()
    {
        int c = 0;
        int e = 0;
        for (ERTICaptionInfoQB col : this.columnInfo) 
        {
        	colNames.add(new SourceColumnInfo(col.getColStringId(),
					new Integer(c++), new Integer(e++)));
			if (col.getColInfoList() != null) 
			{
				c += col.getColInfoList().size() - 1;
			}
		}    	
    }
    /**
     * @param fldName
     * @return index for column named fldName.
     */
    protected int getFldIdx(final String fldName)
    {
        int fldIdx = Collections.binarySearch(colNames, new SourceColumnInfo(fldName, null, null),
                colPairComparator);
        if (fldIdx < 0)
            return -1;
        return colNames.get(fldIdx).getRowDataIdx();
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
    protected Object processValue(final int fldIdx, final int colIdx, final Object obj)
    {    
//        if (columnInfo.get(colIdx).getColInfoList() == null) 
//        {
        	if (obj instanceof Calendar)
        	{
        		return scrDateFormat.format((Calendar)obj);        
        	} 
        	else if (obj instanceof Timestamp )
        	{
        			return scrDateFormat.format((Date)obj);
        	} 
        	else if (obj instanceof java.sql.Date || obj instanceof Date )
        	{
        		return scrDateFormat.format((Date)obj);
        	}
    
        	UIFieldFormatterIFace formatter = columnInfo.get(colIdx).getUiFieldFormatter();
        	if (formatter != null && formatter.isInBoundFormatter())
        	{
        		return formatter.formatToUI(obj);
        	}
//        }
        //else everything has already been taken care of
        return obj;
    }
    
    /**
     * @return the record Id
     */
    public Object getRecordId()
    {
        return null;
    }
    
    /**
     * @return true if the source knows it size.
     */
    public boolean hasResultSize()
    {
        return false;
    }
    
    /**
     * @return
     */
    public int size()
    {
        return -1;
    }
}
