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
import java.util.List;

import javax.swing.event.ChangeListener;

import edu.ku.brc.af.core.PermissionIFace;

/**
 * A custom editor for a set of permissions. It is used when View/Modify/Delete/Add are used differently
 * to describe permissions for an object. For example, the Backup tool may use View for enabling 
 * Backup and Modify for enabling Restore.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 21, 2008
 *
 */
public interface PermissionEditorIFace
{
    /**
     * @param permissions
     */
    public abstract void setPermissions(List<PermissionIFace> permissions);
    
    /**
     * @return
     */
    public abstract List<PermissionIFace> getPermissions();
    
    /**
     * @return
     */
    public abstract Component getUIComponent();
    
    /**
     * @return
     */
    public abstract boolean hasChanged();
    
    /**
     * 
     */
    public abstract void setChanged(boolean changed);

    /**
     * @param title
     */
    public abstract void setTitle(String title);
    
    /**
     * @return an array of table ids that is associated with the object that the editor is representing.
     */
    public abstract int[] getAssociatedTableIds();
    
    /**
     * @param option the option (view, add, modify, delete)
     * @param text the order text for it or null
     * @param readOnly whether the panel is readonly
     */
    public abstract void setOverrideText(int option, String text, boolean readOnly);

    /**
     * @param l
     */
    public abstract void addChangeListener(ChangeListener l);
    
    /**
     * @param l
     */
    public abstract void removeChangeListener(ChangeListener l);
    
}
