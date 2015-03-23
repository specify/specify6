/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.sql.Connection;
import java.util.List;


/**
 * Interface for a container of results that were gotten (most likely) asynchronously.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Created: Mar 2, 2007
 *
 */
public interface QueryResultsContainerIFace
{

    /**
     * Starts the execution asynchronously.
     * @param listener the calback listener who is notified when it is done
     * @param connect optional JDBC connect for some case where it can be reused
     */
    public abstract void start(QRCProcessorListener listener, Connection connect);
    
    /**
     * Returns whether the execution failed.
     * @return whether the execution failed.
     */
    public abstract boolean hasFailed();
    
    /**
     * Adds a QueryResultsDataObj to the container.
     * @param qrdo the item to be added
     */
    public abstract void add(QueryResultsDataObj qrdo);
    
    /**
     * Returns the list of QueryResultsDataObj objects.
     * @return the list of QueryResultsDataObj objects.
     */
    public abstract List<QueryResultsDataObj> getQueryResultsDataObjs();
    
    /**
     * Clears and extra data objects.
     */
    public abstract void clear();
    
}
