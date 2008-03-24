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
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.db.ERTICaptionInfo;
import edu.ku.brc.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.ui.forms.DataObjectSettable;
import edu.ku.brc.ui.forms.DataObjectSettableFactory;
import edu.ku.brc.ui.forms.formatters.DataObjAggregator;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;

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
    protected ESResultsTablePanelIFace    parentERTP;
    protected Vector<Class<?>>            classNames  = new Vector<Class<?>>();
    protected Vector<String>              colNames    = new Vector<String>();
    protected int                         currentRow  = 0;
    protected QueryForIdResultsIFace      results;
    
    protected int                         numColumns  = 0;
    protected Vector<Integer>             ids         = null;  // Must be initialized to null!
    protected Vector<Vector<Object>>      cache       = new Vector<Vector<Object>>();
    protected List<ERTICaptionInfo>       captionInfo = null;
    protected int[]                       columnIndexMapper = null;
    
    protected PropertyChangeListener      propertyListener = null;
    
    // Frame buffer
    protected int startInx = 0;
    protected int endInx   = 0;
    
    protected boolean useColOffset = false;
    
    /**
     * Construct with a QueryForIdResultsIFace
     * @param results
     */
    public ResultSetTableModel(final ESResultsTablePanelIFace parentERTP,
                               final QueryForIdResultsIFace results)
    {
        this.parentERTP = parentERTP;
        this.results = results;
        
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
                jpaQuery = new JPAQuery(sqlStr, this);
                jpaQuery.start();
            }
            
        } else
        {
            SQLExecutionProcessor sqlProc = new SQLExecutionProcessor(this, results.getSQL(results.getSearchTerm(), ids));
            sqlProc.start();
        }
    }
    
    /**
     * Cleans up internal data members.
     */
    public void cleanUp()
    {
        parentERTP = null;
        results    = null;
        propertyListener = null;
        
        for (Vector<Object> list : cache)
        {
            list.clear(); 
        }
        cache.clear();
        
        if (ids != null)
        {
            ids.clear();
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
        /*
         * Modified this to fix a bug that (so far) had only shown itself for Timestamp columns.
         * 
         * ResultSetTableModel.getValueAt(row, column) returns a formatted object for the column.
         * 
         * Then in the JTable display code, JTable.getCellRenderer calls getDefaultRenderer(getColumnClass())
         * Which returns a formatter for columnClass and trie to format the already-formatted value
         * from getValueAt(). Which actually works out (except the "centered" property is sometimes overridden
         * for Numbers) for all classes (usually a default formatter Object is eventually returned) except
         * java.sql.Timestamp. 
         * 
         * Everything seems OK now.
         * 
         */
        return Object.class; //whatever getValueAt(?, column) returns.
    }
    
    protected Class<?> getColumnClass2(int column)
    {
        if (captionInfo != null)
        {
            if (captionInfo.get(column).getColClass() == null)
            {
                return String.class;
            }
            //log.debug(captionInfo.get(column).getColClass().getName());
            return captionInfo.get(column).getColClass();
        }
        
        if (classNames.size() > 0)
        {
            Class<?> classObj = classNames.elementAt(column);
            
            if (classObj == Calendar.class || classObj == java.sql.Date.class || classObj == Date.class || classObj == Timestamp.class)
            {
                return String.class;
            }
            return classObj;
        }
        return String.class;
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
                Object obj = rowArray.get(column);
                
                Class<?> dataClassObj = getColumnClass2(column); //see comment for getColumnClass().
                if (obj == null && (dataClassObj == null || dataClassObj == String.class))
                {
                    return "";
                }
                
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
                
                //System.out.println(row+" "+column+" ["+obj+"] "+getColumnClass(column).getSimpleName() + " " + useColOffset);
                
                UIFieldFormatterIFace formatter = captionInfo != null ? captionInfo.get(column).getUiFieldFormatter() : null;
                if (formatter != null && formatter.isInBoundFormatter())
                {
                    return formatter.formatInBound(obj);
                }
                if (obj == null)
                {
                    int x = 0;
                    x++;
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
     * @param index
     * @return
     */
    public Integer getRowId(final int index)
    {
        return ids.get(index);
    }
    
    /**
     * Removes a row from the model.
     * @param index the index to be removed.
     */
    public void removeRow(final int index)
    {
        cache.remove(index);
        ids.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    /**
     * Returns a RecordSet object from the table
     * @param rows the selected rows
     * @param returnAll indicates whether all the records should be returned if nothing was selected
     * @return Returns a RecordSet object from the table
     */
    public RecordSetIFace getRecordSet(final int[] rows, final boolean returnAll)
    {
        RecordSet rs = new RecordSet();
        rs.setType(RecordSet.GLOBAL);
        rs.initialize();

        // return if now rows are selected
        if (!returnAll && (rows == null || rows.length == 0))
        {
            return rs;
        }

        for (int inx : rows)
        {
            rs.addItem(ids.get(inx));
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
    
    /**
     * @param setter
     * @param parent
     * @param fieldName
     * @param fieldClass
     * @param resultSet
     * @param colIndex
     * @throws SQLException
     */
    protected void setField(final DataObjectSettable setter, 
                            final Object             parent, 
                            final String             fieldName, 
                            final Class<?>           fieldClass,
                            final ResultSet          resultSet, 
                            final int                colIndex) throws SQLException
    {
        Object fieldDataObj = resultSet.getObject(colIndex + 1);
        //log.debug("fieldName ["+fieldName+"] fieldClass ["+fieldClass.getSimpleName()+"] colIndex [" +  colIndex + "] fieldDataObj [" + fieldDataObj+"]");
        if (fieldDataObj != null)
        {
            if (fieldClass == String.class)
            {
                setter.setFieldValue(parent, fieldName, fieldDataObj);    
                
            } else if (fieldClass == Byte.class)
            {
                setter.setFieldValue(parent, fieldName, resultSet.getByte(colIndex + 1));
                
            } else if (fieldClass == Short.class)
            {
                setter.setFieldValue(parent, fieldName, resultSet.getShort(colIndex + 1));
                
            } else
            {
                setter.setFieldValue(parent, fieldName, fieldDataObj);
            }
        } 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    //@Override
    //@SuppressWarnings("null")
    public synchronized void exectionDone(final SQLExecutionProcessor process, final ResultSet resultSet)
    {
        List<ERTICaptionInfo> captions = results.getVisibleCaptionInfo();
        
        // This can do one of two things:
        // 1) Take multiple columns and create an object and use a DataObjectFormatter to format the object.
        // 2) Table multiple objects that were derived from the columns and roll those up into a single column's value.
        //    This happens when you get back rows of info where part of the columns are duplicated because you really
        //    want those value to be put into a single column.
        //
        // Step One - Is to figure out what type of object needs to be created and what the Columns are 
        //            that need to be set into the object so the dataObjFormatter can do its job.
        //
        // Step Two - If the objects are being aggregated then the object created from the columns are added to a List
        //            and then last formatted as an "aggregation"
        
        try
        {
            if (resultSet.next())
            {
                ResultSetMetaData metaData = resultSet.getMetaData();
                
                // Composite
                boolean                hasCompositeObj = false;
                DataObjSwitchFormatter formatterObj    = null;
                Object                 compObj         = null;
                
                // Aggregates
                ERTICaptionInfo        aggCaption = null;
                Vector<Object>         aggList    = null;
                DataObjectSettable     aggSetter  = null;
                Stack<Object>          aggListRecycler = null;
                
                DataObjectSettable     dataSetter     = null;  // data getter for Aggregate or the Subclass
                            
                // Loop through the caption to figure out what columns will be displayed.
                // Watch for Captions with an Aggregator or Composite 
                numColumns = captions.size();
                for (ERTICaptionInfo caption : captions)
                {
                     colNames.addElement(caption.getColLabel());
                     
                     int      inx = caption.getPosIndex() + 1;
                     Class<?> cls = Class.forName(metaData.getColumnClassName(inx));
                     if (cls == Calendar.class ||  cls == java.sql.Date.class || cls == Date.class)
                     {
                         cls = String.class;
                     }
                     classNames.addElement(cls);
                     caption.setColClass(cls);
                     
                     if (caption.getAggregatorName() != null)
                     {
                         //log.debug("The Agg is ["+caption.getAggregatorName()+"] "+caption.getColName());
                         
                         // Alright we have an aggregator
                         aggList         = new Vector<Object>();
                         aggListRecycler = new Stack<Object>();
                         aggCaption      = caption;
                         aggSetter       = DataObjectSettableFactory.get(aggCaption.getAggClass().getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
                         
                         // Now check to see if we are aggregating the this type of object or a child object of this object
                         // For example Collectors use an Agent as part of the aggregation
                         if (aggCaption.getSubClass() != null)
                         {
                             dataSetter = DataObjectSettableFactory.get(aggCaption.getSubClass().getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
                         } else
                         {
                             dataSetter = aggSetter;
                         }
                         
                     } else if (caption.getColInfoList() != null)
                     {
                         // OK, now aggregation but we will be rolling up multiple columns into a single object for formatting
                         // We need to get the formatter to see what the Class is of the object
                         hasCompositeObj = true;
                         aggCaption      = caption;
                         formatterObj    = caption.getDataObjFormatter();
                         if (formatterObj != null)
                         {
                             if (formatterObj.getDataClass() != null)
                             {
                                 aggSetter = DataObjectSettableFactory.get(formatterObj.getDataClass().getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
                             } else
                             {
                                 log.error("formatterObj.getDataClass() was null for "+caption.getColName());
                             }
                         } else
                         {
                             log.error("DataObjFormatter was null for "+caption.getColName());
                         }
                         
                     }
                     //colNames.addElement(metaData.getColumnName(i));
                     //System.out.println("**************** " + caption.getColLabel()+ " "+inx+ " " + caption.getColClass().getSimpleName());
                }
                
                // aggCaption will be non-null for both a Aggregate AND a Composite
                if (aggCaption != null)
                {
                    // Here we need to dynamically discover what the column indexes are that we to grab
                    // in order to set them into the created data object
                    for (ERTICaptionInfo.ColInfo colInfo : aggCaption.getColInfoList())
                    {
                        for (int i=0;i<metaData.getColumnCount();i++)
                        {
                            String colName = StringUtils.substringAfterLast(colInfo.getColumnName(), ".");
                            if (colName.equalsIgnoreCase(metaData.getColumnName(i+1)))
                            {
                                colInfo.setPosition(i);
                                break;
                            }
                        }
                    }
                    
                    // Now check to see if there is an Order Column because the Aggregator might need it for sorting the Aggregation
                    String ordColName = aggCaption.getOrderCol();
                    if (StringUtils.isNotEmpty(ordColName))
                    {
                        String colName = StringUtils.substringAfterLast(ordColName, ".");
                        //log.debug("colName ["+colName+"]");
                        for (int i=0;i<metaData.getColumnCount();i++)
                        {
                            //log.debug("["+colName+"]["+metaData.getColumnName(i+1)+"]");
                            if (colName.equalsIgnoreCase(metaData.getColumnName(i+1)))
                            {
                                aggCaption.setOrderColIndex(i);
                                break;
                            }
                        }
                        if (aggCaption.getOrderColIndex() == -1)
                        {
                            log.error("Agg Order Column Index wasn't found ["+ordColName+"]");
                        }
                    }
                }
                
                if (ids == null)
                {
                    ids = new Vector<Integer>();
                } else
                {
                    ids.clear();
                }
                
                // Here is the tricky part.
                // When we are doing a Composite we are just taking multiple columns and 
                // essentially replace them with a single value from the DataObjFormatter
                //
                // But when doing an Aggregation we taking several rows and rolling them up into a single value.
                // so this code knows when it is doing an aggregation, so it knows to only add a new row to the display-able
                // results when primary id changes.
                
                Vector<Object> row       = null;
                boolean        firstTime = true;
                int            prevId    = Integer.MAX_VALUE;  // really can't assume any value but will choose Max
                do 
                {
                    int id = resultSet.getInt(1);
                    //log.debug("id: "+id+"  prevId: "+prevId);
                    
                    // Remember aggCaption is used by both a Aggregation and a Composite
                    if (aggCaption != null && !hasCompositeObj)
                    {
                        if (firstTime)
                        {
                            prevId    = id;
                            row       = new Vector<Object>();
                            firstTime = false;
                            cache.add(row);
                            
                        } else if (id != prevId)
                        {
                            //log.debug("Agg List len: "+aggList.size());
                            
                            if (row != null && aggList != null)
                            {
                                int aggInx = captions.indexOf(aggCaption);
                                row.remove(aggInx);
                                row.insertElementAt(DataObjFieldFormatMgr.aggregate(aggList, aggCaption.getAggClass()), aggInx);

                                if (aggListRecycler != null)
                                {
                                    aggListRecycler.addAll(aggList);
                                }
                                aggList.clear();
                                
                                row = new Vector<Object>();
                                cache.add(row);
                            }
                            prevId = id;
                            
                        } else if (row == null)
                        {
                            row = new Vector<Object>();
                            cache.add(row);
                        }
                    } else
                    {
                        row = new Vector<Object>();
                        cache.add(row);
                    }
                    
                    ids.add(id);
                    
                    // Now for each Caption column get a value
                    for (ERTICaptionInfo caption :  captions)
                    {
                        int posIndex = caption.getPosIndex();
                        if (caption == aggCaption) // Checks to see if we need to take multiplier columns and make one column
                        {
                            if (hasCompositeObj) // just doing a Composite
                            {
                                if (aggSetter != null && row != null && formatterObj != null)
                                {
                                    if (compObj == null)
                                    {
                                        compObj = aggCaption.getAggClass().newInstance();
                                    }
                                    
                                    for (ERTICaptionInfo.ColInfo colInfo : aggCaption.getColInfoList())
                                    {
                                        setField(aggSetter, compObj, colInfo.getFieldName(), colInfo.getFieldClass(), resultSet, colInfo.getPosition());
                                    }
                                    row.add(DataObjFieldFormatMgr.format(compObj, compObj.getClass()));
                                    
                                } else
                                {
                                    log.error("Aggregator is null! ["+aggCaption.getAggregatorName()+"] or row or aggList");
                                }
                            } else if (aggSetter != null && row != null && aggList != null) // Doing an Aggregation
                            {
                                Object aggObj;
                                if (aggListRecycler.size() == 0)
                                {
                                    aggObj = aggCaption.getAggClass().newInstance();
                                } else
                                {
                                    aggObj = aggListRecycler.pop();
                                }
                                Object aggSubObj = aggCaption.getSubClass() != null ? aggCaption.getSubClass().newInstance() : null;
                                aggList.add(aggObj);

                                @SuppressWarnings("unused")
                                DataObjAggregator aggregator = DataObjFieldFormatMgr.getAggregator(aggCaption.getAggregatorName());
                                //log.debug(" aggCaption.getOrderColIndex() "+ aggCaption.getOrderColIndex());
                                
                                //aggSetter.setFieldValue(aggObj, aggregator.getOrderFieldName(), resultSet.getObject(aggCaption.getOrderColIndex() + 1));
                                
                                Object dataObj;
                                if (aggSubObj != null)
                                {
                                    aggSetter.setFieldValue(aggObj, aggCaption.getSubClassFieldName(), aggSubObj);
                                    dataObj = aggSubObj;
                                } else
                                {
                                    dataObj = aggObj;
                                }
                                
                                for (ERTICaptionInfo.ColInfo colInfo : aggCaption.getColInfoList())
                                {
                                    setField(dataSetter, dataObj, colInfo.getFieldName(), colInfo.getFieldClass(), resultSet, colInfo.getPosition());
                                }
                                row.add("PlaceHolder");
                                
                            } else if (aggSetter == null || aggList == null)
                            {
                                log.error("Aggregator is null! ["+aggCaption.getAggregatorName()+"] or aggList["+aggList+"]");
                            }
                            
                        } else if (row != null)
                        {
                            Object obj = caption.processValue(resultSet.getObject(posIndex + 1));
                            row.add(obj);
                        }
                    }
                    
                } while (resultSet.next());
                
                // We were always setting the rolled up data when the ID changed
                // but on the last row we need to do it here manually (so to speak)
                if (aggCaption != null && aggList != null && aggList.size() > 0 && row != null)
                {
                    int aggInx = captions.indexOf(aggCaption);
                    row.remove(aggInx);
                    String colStr = DataObjFieldFormatMgr.aggregate(aggList, aggCaption.getAggClass());
                    row.insertElementAt(colStr, aggInx);
                    aggList.clear();
                    aggListRecycler.clear();
                }
                
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
    public synchronized void executionError(SQLExecutionProcessor process, Exception ex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQuery)
     */
    //@Override
    public void exectionDone(final CustomQueryIFace customQuery)
    {
        JPAQuery jpaQuery = (JPAQuery)customQuery;
        List<?> list      = jpaQuery.getDataObjects();
        List<ERTICaptionInfo> captions = results.getVisibleCaptionInfo();
        
        log.debug("Results size: "+list.size());
        
        if (ids == null)
        {
            ids = new Vector<Integer>();
        } else
        {
            ids.clear();
        }
        
        if (!jpaQuery.isInError() && list != null && list.size() > 0)
        {
            /*if (numColumns == 1)
            {
                for (Object rowObj : list)
                {
                    Vector<Object> row = new Vector<Object>(list.size());
                    row.add(rowObj);
                    cache.add(row);
                }
                
            } else*/
            {
                
                for (Object rowObj : list)
                {
                    Vector<Object> row = new Vector<Object>(list.size());
                    if (rowObj.getClass().isArray())
                    {
                        int col = 0;
                        Iterator<ERTICaptionInfo> cols = captions.iterator();                        
                        for (Object colObj : (Object[])rowObj)
                        {
                            if (col == 0)
                            {
                                if (colObj instanceof Integer)
                                {
                                    ids.add((Integer)colObj);
                                } else
                                {
                                    log.error("First Column must be Integer id! ["+colObj+"]");
                                    row.add(cols.next().processValue(colObj));
                                }
                            } else
                            {
                                row.add(cols.next().processValue(colObj));
                            }
                            col++;
                        }
                    } else
                    {
                        row.add(rowObj);
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
        CommandAction cmdAction = new CommandAction(ExpressSearchTask.EXPRESSSEARCH, "SearchComplete", customQuery);
        cmdAction.setProperty("QueryForIdResultsIFace", results);
        cmdAction.setProperty("ESResultsTablePanelIFace", parentERTP);
        CommandDispatcher.dispatch(cmdAction);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQuery)
     */
    //@Override
    public void executionError(CustomQueryIFace customQuery)
    {
        // NO OP
    }
}
