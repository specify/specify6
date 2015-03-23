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

/**
 * Interface to notify the object that a query has completed. When it finishes successfully the resulset gets passed back.
 * when it finishes in error the exception gets passed as a parameter.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public interface SQLExecutionListener
{
    /**
     * Notification that the process is done and has finished without exception.
     * @param process the calling processor
     * @param resultSet the resultset of the query
     */
    public void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet resultSet);
    
    /**
     * Notification that the process was done and completed with an exception.
     * @param process the calling processor
     * @param ex the exception of what went wrong
     */
    public void executionError(final SQLExecutionProcessor process, final Exception ex);
    
}
