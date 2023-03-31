/* Copyright (C) 2023, Specify Collections Consortium
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

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class QBQueryForIdResultsHQL extends QueryForIdResultsHQL 
{
    protected final QueryBldrPane                             queryBuilder;
    protected final AtomicReference<Future<CustomQueryIFace>> queryTask      = new AtomicReference<Future<CustomQueryIFace>>();
    protected final AtomicReference<CustomQueryIFace>         query          = new AtomicReference<CustomQueryIFace>();
    protected final AtomicBoolean                             cancelled      = new AtomicBoolean(
                                                                                     false);
    protected List<Pair<String, Object>>                      params = null;
    protected String                                          title;
    protected String                                          iconName;
    protected List<SortElement>                               sortElements = null;
    protected boolean 										  filterDups = false;							
    protected Vector<Vector<Object>>                          cache = null;
    protected boolean                                         recIdsLoaded = false;
    protected boolean                                         hasIds = true;
    protected int											  maxTableRows = ExpressSearchTask.RESULTS_THRESHOLD;
        
    /**
     * @param bannerColor
     * @param title
     * @param iconName
     * @param tableId
     * @param queryBuilder
     */
    public QBQueryForIdResultsHQL(final Color     bannerColor,
                                  final String    title,
                                  final String    iconName,
                                  final int       tableId,
                                  final QueryBldrPane queryBuilder)
    {
        super(null, bannerColor, tableId);
        this.title    = title;
        this.iconName = iconName;
        this.queryBuilder = queryBuilder;
    }
    
    /**
     * @param list
     */
    public void setCaptions(List<ERTICaptionInfo> list)
    {
        this.captions = list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getDisplayOrder()
     */
    @Override
    public Integer getDisplayOrder()
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getIconName()
     */
    @Override
    public String getIconName()
    {
        return iconName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#buildCaptions()
     */
    @Override
    protected void buildCaptions()
    {
        //no.
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getTableId()
     */
    @Override
    public int getTableId()
    {
        return tableId;
    }

    
    /**
     * @param repResource
     * @returns true if repResource is currently available from the AppContextMgr.
     */
    protected boolean repContextIsActive(final AppResourceIFace repResource)
    {
        AppResourceIFace match = AppContextMgr.getInstance().getResource(repResource.getName());
        //XXX - Is it really safe to assume there there won't be more than one resource with the same name and mimeType?
        return match != null && match.getMimeType().equalsIgnoreCase(repResource.getMimeType());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getParams()
     */
    @Override
    public List<Pair<String, Object>> getParams()
    {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(List<Pair<String, Object>> params)
    {
        this.params = params;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#cleanUp()
     */
    @Override
    public void complete()
    {
        if (queryBuilder != null)
        {
            queryBuilder.resultsComplete();
        }
        super.complete();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getQueryTask()
     */
    @Override
    public Future<?> getQueryTask()
    {
        return queryTask.get();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#setQueryTask(java.util.concurrent.Future)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setQueryTask(Future<?> queryTask)
    {
        this.queryTask.set((Future<CustomQueryIFace>)queryTask);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#queryTaskDone()
     */
    @Override
    public void queryTaskDone(final Object results)
    {
        try
        {
            query.set((CustomQueryIFace)results);
            if (cancelled.get())
            {
                query.get().cancel();
            }
            queryTask.set(null);
            if (queryBuilder != null && !queryBuilder.queryTaskDone())
            {
                query.get().cancel();
            }
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QBQueryForIdResultsHQL.class, e);
            throw new RuntimeException(e);
        }
    }
    
    public CustomQueryIFace getQuery()
    {
        return query.get();
    }
    
    protected boolean isRetrieving()
    {
        return queryTask.get() != null && query.get() == null;
    }
    
    protected boolean isDisplaying()
    {
        return !isRetrieving() && query.get() != null;
    }
    
    public void cancel()
    {
        if (isRetrieving())
        {
            cancelled.set(true);
            queryTask.get().cancel(true);
            //and what else needs to be done???...
        }
        else if (isDisplaying())
        {
            cancelled.set(true);
            query.get().cancel(); 
        }
        //else do nuthin.
    }

    /**
     * @return the cancelled
     */
    public boolean getCancelled()
    {
        return cancelled.get();
    }
 
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getRecIds()
     */
    @Override
    public Vector<Integer> getRecIds()
    {
        if (!recIdsLoaded)
        {
            //if somebody wants 'em copy em from the cache
            recIds = new Vector<Integer>(cache != null ? cache.size() : 0);
            if (cache != null)
            {
                for (Vector<Object> row : cache)
                {
                    recIds.add((Integer )row.get(row.size()-1));                
                }
            }
            recIdsLoaded = true;
        }
        return recIds;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#size()
     */
    @Override
    public int size()
    {
        return cache != null ? cache.size() : 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#showProgress()
     */
    @Override
    public boolean showProgress()
    {
        //QueryBuilder sets up progress bar and status messages for itself.
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#getMaxTableRows()
     */
    @Override
    public int getMaxTableRows()
    {
    	return maxTableRows;
    }    

    /**
     * @param maxTableRows the maxTableRows to set
     */
    public void setMaxTableRows(int maxTableRows)
    {
    	this.maxTableRows = maxTableRows;
    }
    
    /**
     * @param sortElements the sortElements to set.
     */
    public void setSort(final List<SortElement> sortElements)
    {
        this.sortElements = sortElements;
    }

    /**
     * @param filterDups the filterDups to set
     */
    public void setFilterDups(boolean filterDups)
    {
    	this.filterDups = filterDups;
    }
    
    public String getHQL()
    {
    	return overrideSQL;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#cacheFilled(java.util.Vector)
     */
    @Override
    public void cacheFilled(final Vector<Vector<Object>> cacheData)
    {
    	if (filterDups && cacheData.size() > 1)
        {
        	List<SortElement> dupSorts = new LinkedList<SortElement>();
        	for (int c = 0; c < cacheData.get(0).size(); c++)
        	{
        		dupSorts.add(new SortElement(c, SortElement.ASCENDING));
        	}
        	//Sort the rows by all columns, then iterate list and remove identical rows.
			ResultRowComparator comparator = new ResultRowComparator(dupSorts);
        	Collections.sort(cacheData, comparator);
			int r = 0;
			LinkedList<Vector<Object>> rowsToDelete = new LinkedList<Vector<Object>>();
			while (r < cacheData.size())
			{
				Vector<Object> row = cacheData.get(r);
				r++;
				boolean go = true;
				while (r < cacheData.size() && go)
				{
					Vector<Object> nextRow = cacheData.get(r);
					if (comparator.compare(row, nextRow) == 0)
					{
						rowsToDelete.add(nextRow);
						r++;
					}
					else
					{
						go = false;
					}
				}
			}
			//It is probably possible (and probably more efficient) 
			//to eliminate this step by using an iterator  and deleting 
			//in the loop above (if the iterator supports deletion).
			for (Vector<Object> toDelete : rowsToDelete)
			{
				cacheData.remove(toDelete);
			}
        }
        
    	if (sortElements != null)
        {
            Collections.sort(cacheData, new ResultRowComparator(sortElements));
        }
    	
    	if (queryBuilder != null && queryBuilder.isSmushed()) {
    		Smusher s = new Smusher(cacheData, queryBuilder.getSmushedCol(), queryBuilder.getRecordIdCol());
    		this.cache = s.smush();
    	} else {
    		this.cache = cacheData;
    	}
    }

    public Vector<Vector<Object>> getCache() {
    	return this.cache;
    }
    /**
     * @return the hasIds
     */
    public boolean isHasIds()
    {
        return hasIds;
    }

    /**
     * @param hasIds the hasIds to set
     */
    public void setHasIds(boolean hasIds)
    {
        this.hasIds = hasIds;
    }
    
    /**
     * @return true if a sort will be applied to elements after retrieval from db.
     */
    public boolean isPostSorted()
    {
        return sortElements != null && sortElements.size() > 0; 
    }
    
    /**
     * @return the query name.
     */
    public String getQueryName()
    {
        return queryBuilder != null ? queryBuilder.getQuery().getName() : "";
    }

	/**
	 * @return the filterDups
	 */
	public boolean isFilterDups() 
	{
		return filterDups;
	}

	/**
	 * @return the queryBuilder
	 */
	public QueryBldrPane getQueryBuilder() 
	{
		return queryBuilder;
	}
    	
	
	
}
