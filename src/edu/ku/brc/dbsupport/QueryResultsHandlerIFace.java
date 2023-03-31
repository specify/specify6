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
package edu.ku.brc.dbsupport;

/**
 * Interface for starting the query (starting the one or more Queries associated with the QueryResultsContainers) 
 * and then processing the results.
 * 
 * The caller of this interface constructs a single or multiple QueryResultsContainers, then asks this 
 * to start the query process and then collect the results and return them as a list of Objects.
 * 
 * (NOTE: I removed a method to pass back a list of QueryResultsContainers, we may need ot add this back)
 *
 * @code_status Complete
 * 
 * @author rods
 *     
 */
public interface QueryResultsHandlerIFace
{
    //----------------------------------
    // Initialization Methods
    //----------------------------------
    
    /**
     * Initializes the handler with a listener and a list of containers. The listener is notified when all the results
     * are ready. (Note: Each QueryResultsContainer contains the SQL statement need to fill the attached QueryResuoltsDataObjects).
     * @param listener the listener to be notified
     * @param list the list of QueryResultsContainers to be queried for
     */
    public void init(final QueryResultsListener listener, final java.util.List<QueryResultsContainerIFace> list);
    
    /**
      * Initializes the handler with a listener and a list of containers. The listener is notified when all the results
     * are ready. (Note: Each QueryResultsContainer contains the SQL statement need to fill the attached QueryResuoltsDataObjects).
     * @param listener the listener to be notified
     * @param qrc the single container
     */
    public void init(final QueryResultsListener listener, final QueryResultsContainerIFace qrc);
     
    /**
     * This tells the handler to issue the Queries and wait for the results.
     *
     */
    public void startUp();
     
    //----------------------------------
    // Post Processing Methods
    //----------------------------------
    

     /**
      * This tells the handler to clean up any leftover data structure that are not longer needed.
      *
      */
     public void cleanUp();
    
     /**
      * Returns the list of data Objects (String, Long, Float, Double, Ineger, etc.) from the one or more queries.
      * The order of the list is the same as the order of the QueryResultsContainer and it childeren (A flatten tree 
      * processed left to right).
      * @return Returns the list of data Objects (String, Long, Float, Double, Ineger, etc.) from the one or more queries.
      * The order of the list is the same as the order of the QueryResultsContainer and it childeren (A flatten tree 
      * processed left to right).
      */
     public java.util.List<Object> getDataObjects();
     
     /**
      * Returns true if the handler considers all the results to be in 'pairs' for example: name/value, 
      * where the name is a string and the value can be any Numeric object.
      * @return Return true if the handler considers all the results to be in 'pairs' for example: name/value, 
      * where the name is a string and the value can be any Numeric object.
      */
     public boolean isPairs();
    
    
}
