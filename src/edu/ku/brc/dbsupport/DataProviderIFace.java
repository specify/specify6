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

/**
 * A wrapper interface for a provider class. 
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 2, 2006
 *
 */
public interface DataProviderIFace
{
    /**
     * Eveicts a class of objects from the provider.
     * @param clsObject the class
     */
    public void evict(Class<?> clsObject);
    
    /**
     * Request the provider to be shutdown.
     */
    public void shutdown();
    
    /**
     * Gets the current session, if the current session doesn't exist, it creates a new one.
     * @return the current session
     */
    public DataProviderSessionIFace getCurrentSession();
    
    /**
     * Creates a new session.
     * @return the seesion
     */
    public DataProviderSessionIFace createSession();
    
    /**
     * Adds a listener for changes to any database object.
     * @param listener the listener that will be notified
     */
    //public void addChangeListener(DataProviderChangeListener listener);
    
}
