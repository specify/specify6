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

import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.specify.tasks.subpane.ESResultsSubPane;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class QBResultSetTableModel extends ResultSetTableModel
{
    private static final Logger log = Logger.getLogger(ESResultsSubPane.class);
    
    public QBResultSetTableModel(final ESResultsTablePanelIFace parentERTP,
                                 final QueryForIdResultsIFace results)
    {
        super(parentERTP, results);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQuery)
     */
    @Override
    public void exectionDone(final CustomQueryIFace customQuery)
    {        
        results.queryTaskDone(customQuery);
        List<?> list      = customQuery.getDataObjects();
        boolean hasIds = ((QBQueryForIdResultsHQL ) results).isHasIds();
        List<ERTICaptionInfo> captions = results.getVisibleCaptionInfo();
             
        if (!customQuery.isInError() && !customQuery.isCancelled() && list != null && list.size() > 0)
        {
                int maxTableRows = results.getMaxTableRows();
                int rowNum = 0;
                for (Object rowObj : list)
                {
                    if (rowNum == maxTableRows)
                    {
                        break;
                    }
                    if (customQuery.isCancelled())
                    {
                        break;
                    }
                    Vector<Object> row = new Vector<Object>(rowObj.getClass().isArray() ? ((Object[])rowObj).length : 1);
                    Integer id = null;
                    if (rowObj != null && rowObj.getClass().isArray())
                    {
                        int col = 0;
                        Iterator<ERTICaptionInfo> cols = captions.iterator();                        
                        for (Object colObj : (Object[])rowObj)
                        {
                            if (col == 0)
                            {
                                if (hasIds) //Does this mean 
                                {
                                    id = (Integer )colObj;
                                    if (doDebug) log.debug("*** 1 Adding id["+colObj+"]");
                                } else
                                {
                                    //log.error("First Column must be Integer id! ["+colObj+"]");
                                    row.add(cols.next().processValue(colObj));
                                }
                            } else
                            {
                                Object obj = cols.next().processValue(colObj);
                                row.add(obj);
                                if (doDebug) log.debug("*** 2 Adding id["+obj+"]");
                            }
                            col++;
                        }
                    } else
                    {
                        row.add(rowObj);
                    }
                    if (hasIds)
                    {
                        row.add(id);
                    }
                    cache.add(row);
                    UIRegistry.getStatusBar().incrementValue(((QBQueryForIdResultsHQL )results).getQueryName());
                    rowNum++;
                }                
            
            results.cacheFilled(cache);
            
            fireTableDataChanged();
        }
        
        if (propertyListener != null)
        {
            propertyListener.propertyChange(new PropertyChangeEvent(this, "rowCount", null, new Integer(cache.size())));
        }
        
        if (parentERTP != null)
        {
            CommandAction cmdAction = new CommandAction(ExpressSearchTask.EXPRESSSEARCH, "SearchComplete", customQuery);
            cmdAction.setProperty("QueryForIdResultsIFace", results);
            cmdAction.setProperty("ESResultsTablePanelIFace", parentERTP);
            CommandDispatcher.dispatch(cmdAction);
        }
    }

    /**
     * Returns a RecordSet object from the table
     * @param rows the selected rows
     * @param returnAll indicates whether all the records should be returned if nothing was selected
     * @return Returns a RecordSet object from the table
     */
    @Override
    public RecordSetIFace getRecordSet(final int[] rows, final boolean returnAll)
    {
        RecordSet rs = new RecordSet();
        rs.setType(RecordSet.GLOBAL);
        rs.initialize();

        if (cache == null ||!((QBQueryForIdResultsHQL)results).isHasIds() || (!returnAll && (rows == null || rows.length == 0)))
        {
            return rs;
        }

        int idCol = cache.size() > 0 ? cache.get(0).size()-1 : -1;
        if (rows == null)
        {
            for (Vector<Object> row : cache)
            {
                rs.addItem((Integer )row.get(idCol));
            }
        }
        else
        {
            for (int inx : rows)
            {
                rs.addItem((Integer )cache.get(inx).get(idCol));
            }
        }
        
        return rs;
    }

    /**
     * @param index
     * @return
     */
    @Override
    public Integer getRowId(final int index)
    {
        //!hasIds should imply that tableid = -1, which prevents methods dependent on ids from
        //being called, but checking anyway...
        if (!((QBQueryForIdResultsHQL )results).isHasIds())
        {
            return null;
        }
        Vector<Object> row = cache.get(index);
        return (Integer )row.get(row.size()-1);
    }
    
    /**
     * Removes a row from the model.
     * @param index the index to be removed.
     */
    @Override
    public void removeRow(final int index)
    {
        cache.remove(index);
        fireTableRowsDeleted(index, index);
    }

}
