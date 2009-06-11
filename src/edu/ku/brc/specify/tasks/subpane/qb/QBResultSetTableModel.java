/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBRelationshipInfo.RelationshipType;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.dbsupport.QueryExecutor;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
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
@SuppressWarnings("serial")
public class QBResultSetTableModel extends ResultSetTableModel
{
    private static final Logger log = Logger.getLogger(QBResultSetTableModel.class);
    
    protected boolean debugging = false;
    
    protected final QueryExecutor queryExecutor = new QueryExecutor();
    protected final AtomicBoolean loadingCache = new AtomicBoolean(false);
    protected final AtomicBoolean isPostProcessed = new AtomicBoolean(false);
    protected final AtomicBoolean backgroundLoadsCancelled = new AtomicBoolean(false);
    
    protected boolean loadingCells = false;
    protected int bgTaskCount = 0;
    
    /**
     * @param parentERTP
     * @param results
     */
    public QBResultSetTableModel(final ESResultsTablePanelIFace parentERTP,
                                 final QueryForIdResultsIFace results)
    {
    	super(parentERTP, results, false, false);
    }
            
    /**
     * @return true while background cell-loading tasks are still running.
     */
    @Override
    public synchronized boolean isLoadingCells()
    {
        return bgTaskCount != 0;
    }
    
    /**
     * Stops background cell loading and clears the queue of cells to be loaded. 
     */
    public void cancelBackgroundLoads()
    {
        backgroundLoadsCancelled.set(true);
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQuery)
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized void exectionDone(final CustomQueryIFace customQuery)
    {
    	if (debugging) log.debug("execution done");
    	if (customQuery instanceof JPAQuery)
        {
            JPAQuery jpa = (JPAQuery) customQuery;
            Object data = jpa.getData();
            if (data != null && data instanceof Vector<?>)
            {
            	if (debugging) log.debug("aggregating list");
                if (backgroundLoadsCancelled.get())
                {
                    queryExecutor.shutdown();
                    return;
                }
                Vector<Object> info = (Vector<Object>) data;
                int row = (Integer) info.get(0);
                int col = (Integer) info.get(1);
                Class<?> cls = (Class<?>) info.get(2);
                Vector<Object> cols = (Vector<Object>) info.get(3);

                synchronized (this)
                {
                    cols.set(col, DataObjFieldFormatMgr.getInstance().aggregate(
                            jpa.getDataObjects(), cls));
                    bgTaskCount--;
                }
                if (!isPostProcessed.get())
                {
                    fireTableCellUpdated(row, col);
                }
                else if (!loadingCache.get())
                {
                    //If postSorted then we don't actually know the row number.
                    
                    //it seems like overkill to fire this for every single background cell
                    //but so far performance seems OK.
                    fireTableDataChanged();
                }
                return;
            }
        }
    	
        try 
        {
			results.queryTaskDone(customQuery);
			List<?> list = customQuery.getDataObjects();
			boolean hasIds = ((QBQueryForIdResultsHQL) results).isHasIds();
			List<ERTICaptionInfo> captions = results.getVisibleCaptionInfo();
			if (!customQuery.isInError() && !customQuery.isCancelled()
					&& list != null && list.size() > 0) 
			{
				if (debugging)
					log.debug("loading cache");
				loadingCache.set(true);
				isPostProcessed.set(((QBQueryForIdResultsHQL) results)
						.isPostSorted() || ((QBQueryForIdResultsHQL) results)
						.isFilterDups());

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
					Vector<Object> row = null;
					if (rowObj != null)
					{
						row = new Vector<Object>(rowObj.getClass()
							.isArray() ? ((Object[]) rowObj).length : 1);
					}
					else if (!hasIds)
					{
						row = new Vector<Object>(1);
					}
					
					Integer id = null;
					if (rowObj != null) 
					{
						int skipCols = 0;
						Iterator<ERTICaptionInfo> cols = captions.iterator();
						Object[] values;
						if (rowObj.getClass().isArray())
						{
							values = (Object[] )rowObj;
						}
						else
						{
							values = new Object[1];
							values[0] = rowObj;
						}
						int col = 0;
						while (col < values.length) 
						{
							Object colObj = values[col];
							if (skipCols != 0)
							{
								skipCols--;
								continue;
							}
							
							if (col == 0 && hasIds) {
								if (hasIds) // Does this mean
								{
									id = (Integer) colObj;
									if (debugging)
										log.debug("*** 1 Adding id[" + colObj
												+ "]");
								} 
							} 
							else 
							{
								ERTICaptionInfo erti = cols.next();
								if (colObj != null
										&& erti instanceof ERTICaptionInfoRel
										&& ((ERTICaptionInfoRel) erti)
												.getRelationship().getType() == RelationshipType.OneToMany) 
								{
									ERTICaptionInfoRel ertiRel = (ERTICaptionInfoRel) erti;
									JPAQuery jpa = new JPAQuery(queryExecutor,
											ertiRel.getListHql(colObj), this);
									Vector<Object> info = new Vector<Object>();
									info.add(rowNum);
									info.add(row.size());
									info.add(ertiRel.getRelationship()
											.getDataClass());
									info.add(row);
									jpa.setData(info);
									if (debugging)
										log.debug("*** 3 launching aggregator["
												+ erti.getColLabel() + "]");
									jpa.start();

									synchronized (this) 
									{
										bgTaskCount++;
									}
									row
											.add(UIRegistry
													.getResourceString("QBResultSetTableModel.LOADING_CELL"));
								}
								else
								{
									if (erti.getColInfoList() != null)
									{
										Object[] modifiedColObj = new Object[erti.getColInfoList().size()];
										modifiedColObj[0] = colObj;
										for (int subCol = 1; subCol < modifiedColObj.length; subCol++)
										{
											modifiedColObj[subCol] = values[++col];
										}
										colObj = modifiedColObj;
									}
									
									Object obj = erti.processValue(colObj);
									row.add(obj);
									if (debugging)
										log.debug("*** 2 Adding field[" + obj
												+ "]");
								}
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
					UIRegistry.getStatusBar().incrementValue(
							((QBQueryForIdResultsHQL) results).getQueryName());
					rowNum++;
				}

				if (customQuery.isCancelled()) 
				{
					queryExecutor.shutdown();
				} else 
				{
					results.cacheFilled(cache);
				}
				loadingCache.set(false);

				fireTableDataChanged();
			} else 
			{
				if (debugging)
					log.debug("cache load cancelled");

			}
			if (propertyListener != null) 
			{
				propertyListener.propertyChange(new PropertyChangeEvent(this,
						"rowCount", null, new Integer(cache.size())));
			}

			if (parentERTP != null) 
			{
				if (debugging)
					log.debug("search complete ");
				CommandAction cmdAction = new CommandAction(
						ExpressSearchTask.EXPRESSSEARCH, "SearchComplete",
						customQuery);
				cmdAction.setProperty("QueryForIdResultsIFace", results);
				cmdAction.setProperty("ESResultsTablePanelIFace", parentERTP);
				CommandDispatcher.dispatch(cmdAction);
			}
		} catch (Exception e) 
		{
            UsageTracker.incrHandledUsageCount();
			edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(
					QBResultSetTableModel.class, e);
		}
   }

    /**
	 * Returns a RecordSet object from the table
	 * 
	 * @param rows
	 *            the selected rows
	 * @param returnAll
	 *            indicates whether all the records should be returned if
	 *            nothing was selected
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
        // !hasIds should imply that tableid = -1, which prevents methods
		// dependent on ids from
        // being called, but checking anyway...
        if (!((QBQueryForIdResultsHQL )results).isHasIds())
        {
            return null;
        }
        Vector<Object> row = cache.get(index);
        return (Integer )row.get(row.size()-1);
    }
    
    /**
	 * Removes a row from the model.
	 * 
	 * @param index
	 *            the index to be removed.
	 */
    @Override
    public void removeRow(final int index)
    {
        cache.remove(index);
        fireTableRowsDeleted(index, index);
    }

    @Override
    public void cleanUp()
    {
        queryExecutor.shutdown();
        bgTaskCount = 0;
        super.cleanUp();
    }
}
