/* Filename:    $RCSfile: SQLExecutionListener.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.dbsupport;

/**
 * Interface to notify the object that a query has completed. When it finishes successfully the resulset gets passed back.
 * when it finishes in error the exception gets passed as a parameter.
 * 
 * @author rods
 *
 */
public interface SQLExecutionListener
{
    /**
     * Notification that the process is done and has finished without exception
     * @param process the calling processor
     * @param resultSet the resultset of the query
     */
    public void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet resultSet);
    
    /**
     * Notification that the process was done and completed with an exception
     * @param process the calling processor
     * @param ex the exception of what went wrong
     */
    public void executionError(final SQLExecutionProcessor process, final Exception ex);
    
}
