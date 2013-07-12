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
 * This interface is used to enable a database search (or lengthy search) to see if a
 * data object can be deleted. The method is called no matter what, the arg doDelete
 * tells the destination whether it can delete the object or not.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jan 11, 2008
 *
 */
public interface BusinessRulesOkDeleteIFace
{

    /**
     * Tell it is ok to be deleted.
     * @param dataObj the data object to be deleted
     * @param session the current session.
     * @param doDelete whether it can be deleted or not.
     */
    public void doDeleteDataObj(Object dataObj, DataProviderSessionIFace session, boolean doDelete);
    
    
}
