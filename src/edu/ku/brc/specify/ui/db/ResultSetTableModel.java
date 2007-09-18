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
package edu.ku.brc.specify.ui.db;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ERTICaptionInfo;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.dbsupport.CustomQuery;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;

/**
 * @code_status Alpha
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ResultSetTableModel extends AbstractTableModel implements SQLExecutionListener, CustomQueryListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ResultSetTableModel.class);
    
    protected static DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
    
    protected static int VISIBLE_ROWS = 10; // XXX Got get this from elsewhere

    // Data Members
    protected Vector<Class<?>>            classNames  = new Vector<Class<?>>();
    protected Vector<String>              colNames    = new Vector<String>();
    protected int                         currentRow  = 0;
    protected int                         primaryKeyIndex = 0;
    protected QueryForIdResultsIFace      results;
    
    protected int                         numColumns  = 0;
    protected Vector<Integer>             ids         = null;
    protected Vector<Vector<Object>>      cache       = new Vector<Vector<Object>>();
    protected  List<ERTICaptionInfo>      captionInfo = null;
    
    protected PropertyChangeListener      propertyListener = null;
    
    // Frame buffer
    protected int startInx = 0;
    protected int endInx   = 0;
    
    protected boolean useColOffset = false;
    
    /**
     * Construct with a ResultSet.
     * @param resultSet the recordset
     */
    public ResultSetTableModel(final QueryForIdResultsIFace results, 
                               final ResultSet resultSet)
    {
        this.results = results;
        
        int count = 0;
        try
        {
            if (resultSet != null)
            {
                ResultSetMetaData metaData   = resultSet.getMetaData();
                numColumns = metaData.getColumnCount();
                for (int i=1;i<=numColumns;i++)
                {
                     classNames.addElement(Class.forName(metaData.getColumnClassName(i)));
                     colNames.addElement(metaData.getColumnName(i));
                }

                if (!resultSet.first())
                {
                    return;
                }

                currentRow = 0;
                
                do 
                {
                    /*for (int i=1;i<=metaData.getColumnCount();i++)
                    {
                         System.out.println(cache.size() + " " + i+ " "+resultSet.getObject(i));
                    }*/
                    
                    ids.add(resultSet.getInt(primaryKeyIndex+1));
                    
                    if (count < VISIBLE_ROWS)
                    {
                        Vector<Object> row = new Vector<Object>();
                        for (int i=1;i<=metaData.getColumnCount();i++)
                        {
                             row.add(resultSet.getObject(i));
                        }
                        cache.add(row);
                    }
                    count++;
                } while (resultSet.next());
                
                //System.out.println("Total Records Returned: "+ count);
                
                captionInfo = results.getVisibleCaptionInfo();
            }
            
        } catch (SQLException ex)
        {
            log.error("In constructor of ResultSetTableModel", ex);
        }
        catch (Exception ex)
        {
            log.error("In constructor of ResultSetTableModel", ex);
            
        } finally 
        {
            try
            {
                if (resultSet != null)
                {
                    resultSet.close();
                }
            }
            catch (Exception ex)
            {
                log.error("Error closing resultset", ex);
                
            }
        }
    }
     
    /**
     * Construct with a array of ids.
     * @param searchId
     * @param ids
     */
    public ResultSetTableModel(final QueryForIdResultsIFace results)
    {
        this.results = results;
        
        //this.ids = results.getRecIds();
        
        captionInfo = results.getVisibleCaptionInfo();
        
        startDataAquisition();
    }
    
    /**
     * 
     */
    protected void startDataAquisition()
    {
        //System.out.println("\n"+results.getTitle()+" " +results.isHQL());
        
        if (results.isHQL())
        {
            useColOffset = false;
            
            List<ERTICaptionInfo> captions = results.getVisibleCaptionInfo();
            numColumns = captions.size();
            for (ERTICaptionInfo caption : captions)
            {
                 classNames.addElement(caption.getColClass());
                 colNames.addElement(caption.getColName());
            }
            
            JPAQuery jpaQuery = null;
            String   sqlStr   = results.getSQL(results.getSearchTerm(), ids);
            if (sqlStr != null)
            {
                jpaQuery = new JPAQuery(this, sqlStr);
                jpaQuery.start();
            }
            
        } else
        {
            useColOffset = true;
            SQLExecutionProcessor sqlProc = new SQLExecutionProcessor(this, results.getSQL(results.getSearchTerm(), ids));
            sqlProc.start();
        }
    }

    /**
     * Returns the number of columns
     * @return Number of columns
     */
    public int getColumnCount()
    {
        if (captionInfo != null)
        {
            return captionInfo.size();
        }
        return numColumns;
    }

    /**
     * Returns the Class object for a column
     * @param column the column in question
     * @return the Class of the column
     */
    public Class<?> getColumnClass(int column)
    {
        return classNames.size() == 0 ? (Class<?>)String.class : (Class<?>)classNames.elementAt(column);
    }

    /**
     * Get the column name
     * @param column the column of the cell to be gotten
     */
    public String getColumnName(int column)
    {
        if (captionInfo != null)
        {
            return captionInfo.get(column).getColLabel();
        }

        if (column > -1 && column < colNames.size())
        {
            return colNames.get(column);
        }

        return "N/A";
    }

    /**
     * Gets the value of the row col.
     * @param rowArg the row of the cell to be gotten
     * @param colArg the column of the cell to be gotten
     */
    public Object getValueAt(final int row, final int column)
    {
        if (row > -1 && row < cache.size())
        {
            Vector<Object> rowArray = cache.get(row);
            if (column > -1 && column < rowArray.size())
            {
                Object obj = rowArray.get(useColOffset ? column+1 : column );
                
                if (obj instanceof Calendar)
                {
                    return scrDateFormat.format((Calendar)obj);
                    
                } else if (obj instanceof java.sql.Date || obj instanceof Date)
                {
                    return scrDateFormat.format((Date)obj);
                }
                
                //System.out.println(row+" "+column+" ["+obj+"] "+getColumnClass(column).getSimpleName() + " " + useColOffset);
                
                String fmtName = captionInfo != null ? captionInfo.get(column).getFormatter() : null;
                if (fmtName != null)
                {
                    UIFieldFormatterIFace formatter = UIFieldFormatterMgr.getFormatter(fmtName);
                    if (formatter != null && formatter.isInBoundFormatter())
                    {
                        return formatter.formatInBound(obj);
                    }
                    //log.error("Couldn't find UIFieldFormatterIFace ["+fmtName+"] or doesn't support In Bound formatting. InBnd["+
                    //        formatter != null ? (formatter != null ? formatter.isInBoundFormatter() : "formatter is null") : "???"+"]");
                }
                return obj;
            }
        }

        return "No Data";
    }

    /**
     * Sets a new value into the Model
     * @param aValue the value to be set
     * @param row the row of the cell to be set
     * @param column the column of the cell to be set
     */
    public void setValueAt(Object aValue, int row, int column)
    {

    }

    /**
     * Returns the number of rows
     * @return Number of rows
     */
    public int getRowCount()
    {
        return cache.size();
    }
    
    /**
     * Returns a RecordSet object from the table
     * @param rows the selected rows
     * @param column the col that contains the ID
     * @param returnAll indicates whether all the records should be returned if nothing was selected
     * @return Returns a RecordSet object from the table
     */
    public RecordSetIFace getRecordSet(final int[] rows, final int column, final boolean returnAll)
    {
        RecordSet rs = new RecordSet();
        rs.initialize();

        // return if now rows are selected
        if (!returnAll && (rows == null || rows.length == 0))
        {
            return rs;
        }

        for (Integer id : ids)
        {
            rs.addItem(id);
        }
        return rs;
    }

    /**
     * Clears all the data from the model
     *
     */
    public void clear()
    {
        ids.clear();
        
        if (cache != null)
        {
            for (Vector<Object> row : cache)
            {
                row.clear();
            }
            cache.clear();
        }
        classNames.clear();
        colNames.clear();

        currentRow = 0;
    }

    /**
     * @param propertyListener the propertyListener to set
     */
    public void setPropertyListener(PropertyChangeListener propertyListener)
    {
        this.propertyListener = propertyListener;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    //@Override
    public void exectionDone(SQLExecutionProcessor process, ResultSet resultSet)
    {
        List<ERTICaptionInfo> captions = results.getVisibleCaptionInfo();
        
        try
        {
            if (resultSet.first())
            {
                ResultSetMetaData metaData = resultSet.getMetaData();
                
                numColumns = captions.size();
                for (ERTICaptionInfo caption : captions)
                {
                     //classNames.addElement(caption.getColClass());
                     colNames.addElement(caption.getColLabel());
                     
                     int      inx = caption.getPosIndex() + 1;
                     Class<?> cls = Class.forName(metaData.getColumnClassName(inx));
                     if (cls == Calendar.class ||  cls == java.sql.Date.class || cls == Date.class)
                     {
                         cls = String.class;
                     }
                     classNames.addElement(cls);
                     caption.setColClass(cls);
                     //colNames.addElement(metaData.getColumnName(i));
                     //System.out.println("**************** " + caption.getColLabel()+ " "+inx+ " " + caption.getColClass().getSimpleName());
                }
                
                ids = new Vector<Integer>();
                
                do 
                {
                    Vector<Object> row = new Vector<Object>();
                    
                    row.add(resultSet.getInt(1));
                    
                    ids.add(resultSet.getInt(primaryKeyIndex+1));
                    
                    for (ERTICaptionInfo caption :  captions)
                    {
                        int    inx = caption.getPosIndex() + 1;
                        Object obj = resultSet.getObject(inx);
                        row.add(obj);
                    }
                    cache.add(row);
                } while (resultSet.next());
                
                fireTableStructureChanged();
                fireTableDataChanged();
            }
            
        } catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        if (propertyListener != null)
        {
            propertyListener.propertyChange(new PropertyChangeEvent(this, "rowCount", null, new Integer(cache.size())));
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    //@Override
    public void executionError(SQLExecutionProcessor process, Exception ex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQuery)
     */
    //@Override
    public void exectionDone(CustomQuery customQuery)
    {
        JPAQuery jpaQuery = (JPAQuery)customQuery;
        List<?> list      = jpaQuery.getDataObjects();
        
        if (!jpaQuery.isInError() && list != null && list.size() > 0)
        {
            if (numColumns == 1)
            {
                for (Object rowObj : list)
                {
                    Vector<Object> row = new Vector<Object>(list.size());
                    row.add(rowObj);
                    cache.add(row);
                }
                
            } else
            {
                for (Object rowObj : list)
                {
                    Vector<Object> row = new Vector<Object>(list.size());
                    for (Object colObj : (Object[])rowObj)
                    {
                        row.add(colObj);
                    } 
                    cache.add(row);
                }                
            }

            fireTableDataChanged();
        }
        
        if (propertyListener != null)
        {
            propertyListener.propertyChange(new PropertyChangeEvent(this, "rowCount", null, new Integer(cache.size())));
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQuery)
     */
    //@Override
    public void executionError(CustomQuery customQuery)
    {
        // TODO Auto-generated method stub
        
    }
    
    
    
}
