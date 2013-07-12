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
package edu.ku.brc.af.ui.forms;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;

/**
 * This interface enables UI components to receive a session object while a form is being loaded. some UI controls
 * needs to perform an 'attach' in order to update the data object correctly. When the form is built UI components 
 * are checked to see if they implement this interface.
 *  
 * @author rods
 *
 * @code_status Complete
 *
 * Dec 11, 2010
 *
 */
public interface SessionListenerIFace
{
    /**
     * @param session the current session
     */
    public abstract void setSession(DataProviderSessionIFace session);
    
}
