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
package edu.ku.brc.af.prefs;

import java.awt.Color;
import java.util.Properties;

import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.ui.forms.validation.FormValidator;

/**
 * Simple interface for Pref Panels
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public interface PrefsPanelIFace
{
    /**
     * @return a unique name
     */
    public abstract String getName();

    /**
     * @param name the name to set
     */
    public abstract void setName(String name);
    
    /**
     * @return a unique title
     */
    public abstract String getTitle();

    /**
     * @param title the title to set
     */
    public abstract void setTitle(String title);
    
    /**
     * Return the validator for the panel
     * @return Return the validator for the panel
     */
    public abstract FormValidator getValidator();
    
    /**
     * @return whether the panel is valid
     */
    public abstract boolean isFormValid();
    
    /**
     * Fills in a Properties object with the values that have changed.
     * @param changeHash  the properties
     */
    public abstract void getChangedFields(Properties changeHash);
    
    /**
     * @return true if it is OK to add the pref, false not load it.
     */
    public abstract boolean isOKToLoad();
    
    /**
     * @return returns a permissions object
     */
    public abstract PermissionIFace getPermissions();
    
    /**
     * Sets a permission object
     * @param permissions the obj
     */
    public abstract void setPermissions(PermissionIFace permissions);
    
    /**
     * Gives acces to the overall prefs manager.
     * @param mgr the manager
     */
    public abstract void setPrefsPanelMgr(PrefsPanelMgrIFace mgr);
    
    /**
     * @return the help context for the panel.
     */
    public abstract String getHelpContext();
    
    /**
     * @param hContext the context
     */
    public abstract void setHelpContext(String hContext);
    
    /**
     * @param color
     */
    public abstract void setShadeColor(Color color);
    
}
