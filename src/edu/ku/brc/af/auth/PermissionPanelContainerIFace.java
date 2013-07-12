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
package edu.ku.brc.af.auth;

import java.awt.Component;
import java.util.Hashtable;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 21, 2008
 *
 */
public interface PermissionPanelContainerIFace
{
    /**
     * @return
     */
    public abstract String getPanelName();
    
    /**
     * @return
     */
    public abstract Component getUIComponent();
    
    /**
     * @param principalArg
     * @param overrulingPrincipal
     * @param userType
     */
    public abstract void updateData(SpPrincipal principalArg, 
                                    SpPrincipal overrulingPrincipal, 
                                    Hashtable<String, SpPermission> existingPerms,
                                    Hashtable<String, SpPermission> overrulingPerms,
                                    String     userType);
    
    /**
     * 
     */
    public abstract void savePermissions(DataProviderSessionIFace session) throws Exception;
    
    /**
     * @return the enumerator used to display the permissions
     */
    public abstract PermissionEnumerator  getPermissionEnumerator();
    
    /**
     * @return whether the editor supports Select All / Delselect All
     */
    public abstract boolean doesSupportSelectAll();
    
    /**
     * 
     */
    public abstract void selectAll();
    
    /**
     * 
     */
    public abstract void deselectAll();
    
}
