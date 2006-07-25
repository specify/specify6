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

package edu.ku.brc.dbsupport;

/**
 * Interface that supports it processing through a QueryResultsHandlerIFace
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public interface QueryResultsProcessable
{
    /**
     * Sets the handler into the object
     * @param handler the new handler
     */
    public void setHandler(final QueryResultsHandlerIFace handler);
    
    /**
     * Returns the current handler used to get the results and process them
     * @return Returns the current handler used to get the results and process them
     */
    public QueryResultsHandlerIFace getHandler();
    
}
