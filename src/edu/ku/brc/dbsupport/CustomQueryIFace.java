/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.dbsupport;

import java.util.List;

/**
 * Custom Queries must know how to vend a collection of QueryResultsContainers that need to be processed
 *
 * @code_status Complete
 * 
 * @author rods
 * 
 */
public interface CustomQueryIFace
{

    /**
     * Synchronous execution of the query
     * @return true if executed correctly
     */
    public abstract boolean execute();
    
    /**
     * Asynchronous execution of a query callback notification is on  (It uses SwingWorker)
     * @param cql the listener for the results of the execute
     * @return
     */
    public abstract void execute(CustomQueryListener cql);
    
    /**
     * Returns a list of results.
     * @return the list of results
     */
    public abstract List<?> getDataObjects();
    
    /**
     * @return
     */
    public abstract List<QueryResultsDataObj> getResults();
    
    /**
     * Return a collection QueryResultsContainers that need to be processed
     * @return Return a collection QueryResultsContainers that need to be processed
     */
    public abstract List<QueryResultsContainerIFace> getQueryDefinition();
    
    /**
     * @return the name
     */
    public abstract String getName();
    
    //public abstract void clear();
    
    /**
     * @return true in case of error.
     */
    public abstract boolean isInError();
    
    
    /**
     * @return true if cancelled.
     */
    public abstract boolean isCancelled();
    
    /**
     * cancel the query
     */
    public abstract void cancel();
    
    /**
     * @return the list of table ids the query uses.
     */
    public abstract List<Integer> getTableIds();
    
    /**
     * @return the maximum number of rows the query will retrieve
     */
    public abstract int getMaxResults();
    
    /**
     * @param maxResulst
     * 
     * set the maximum number of rows the query will retrieve
     */
    public abstract void setMaxResults(int maxResults);
}
