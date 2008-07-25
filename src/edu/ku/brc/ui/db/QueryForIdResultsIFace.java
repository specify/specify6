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
/**
 * 
 */
package edu.ku.brc.ui.db;

import java.awt.Color;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import edu.ku.brc.util.Pair;



/**
 * This interface is used to describe to the results "renderer" what it should display. It is used to both display
 * results and it can be used to run a new query from a list of Ids to create a set of results. 
 * Any class implementing this interface may also consider implementing ServiceProviderIFace if it 
 * needs to provide additional services.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Sep 6, 2007
 *
 */
public interface QueryForIdResultsIFace
{
    /**
     * @return the Icon Name for the return results
     */
    public abstract String getIconName();
    
    /**
     * @return the title to be displayed on the results bar
     */
    public abstract String getTitle();
    
    /**
     * @return the list of record Ids used to create the SQL Statement
     */
    public abstract Vector<Integer> getRecIds();
    
    /**
     * Perform any clean up
     */
    public abstract void cleanUp();
    
    /**
     * @return the Table Id of the primary key results 
     */
    public abstract int getTableId();
    
    /**
     * @return returns the list of caption (column headers)
     */
    public abstract List<ERTICaptionInfo> getVisibleCaptionInfo();
    
    /**
     * @return returns the Top Bar (or banner) color
     */
    public abstract Color getBannerColor();
    
    /**
     * Sets a predefined SQL statement that will be returned by the 
     * getSQL method. Eccentially setting this overrides dynamically creating the SQL.
     * 
     * @param sql the SQL that override the generated SQL
     */
    public abstract void setSQL(String sql);
    
    /**
     * Dynamically generates an SQL statement from the Search Config and the list of Ids.
     * NOTE: The returned SQL statement may be what was set by setSQL method.
     * @param searchTerm the search term
     * @param ids the list of Ids to be searched
     * @return the the SQL statement
     */
    public abstract String getSQL(final String searchTerm, Vector<Integer> ids);
    
    /**
     * @return the search term
     */
    public abstract String getSearchTerm();
    
    /**
     * @return the display position in the list of resultsets
     */
    public abstract Integer getDisplayOrder();
    
    /**
     * @return whether this should be processed as HQL or SQL
     */
    public abstract boolean isHQL();
    
    /**
     * @return indicates whether the results panel should be expanded at start up.
     */
    public abstract boolean isExpanded();
    
    /**
     * Returns a description (already localized) as to what the results means.
     * Note: This is required for search dialog box results, but may be helpful.
     * @return a description (already localized) as to what the results means.
     */
    public abstract String getDescription();
    
    /**
     * @return returns whether the services should be installed.
     */
    public abstract boolean shouldInstallServices();
    
    /**
     * @return indicates whether the table of items can be edited.
     */
    public abstract boolean isEditingEnabled();
    
    /**
     * Requests an id be removed.
     * @param id the id to be removed.
     */
    public abstract void removeIds(List<Integer> ids);
    
    /**
     * Tells the UI for the display of the results to allow Multiple selection if it is available.
     * @param isMultiple true/false
     */
    public abstract void setMultipleSelection(boolean isMultiple); 
    
    /**
     * @return whether the results UI should allow Multiple selection if poissible.
     */
    public abstract boolean isMultipleSelection();
    
    /**
     * @returns a list of <name, value> pairs describing parameters.
     */
    public abstract List<Pair<String, Object>> getParams();
    
    /**
     * Take any necessary steps when search has completed and results have been processed. 
     */
    public abstract void complete();
    
    /**
     * @param queryTask: the queryTask process/thread/task.
     */
    public abstract void setQueryTask(final Future<?> queryTask);
    
    /**
     * @return the queryTask.
     */
    public abstract Future<?> getQueryTask();
    
    /**
     * Notification that queryTask is done.
     * 
     * @param results the results of the query task. (Probably a CustomQueryIFace)
     */
    public abstract void queryTaskDone(final Object results);
    
    /**
     * Sets the record ids for the results.
     * 
     * @param ids
     */
    public abstract void setRecIds(final Vector<Integer> ids);
}
