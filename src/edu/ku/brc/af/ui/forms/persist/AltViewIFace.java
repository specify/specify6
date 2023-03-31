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
package edu.ku.brc.af.ui.forms.persist;

import java.util.List;

/**
 * Definition of an Alternate View which can be an "Edit", "View", "None".
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 29, 2007
 *
 */
public interface AltViewIFace extends Comparable<AltViewIFace>
{
    public enum CreationMode {NONE, EDIT, VIEW, SEARCH}

    /**
     * @return the mode used during creation
     */
    public abstract CreationMode getMode();

    /**
     * @return the name of the ViewDefIFace it owns
     */
    public abstract String getViewDefName();

    /**
     * @return title used by the selector combobox
     */
    public abstract String getTitle();

    /**
     * @return the unique name of the AltView
     */
    public abstract String getName();

    /**
     * @param name the unique name of the AltView
     */
    public abstract void setName(String name);

    /**
     * @return whether the ViewDef will have a validator
     */
    public abstract boolean isValidated();

    /**
     * @return ViewDefIFace it refers to
     */
    public abstract ViewDefIFace getViewDef();

    /**
     * Sets the ViewDef.
     * @param viewDef the ViewDef
     */
    public abstract void setViewDef(ViewDefIFace viewDef);

    /**
     * @return whether it is the default (initial) altview
     */
    public abstract boolean isDefault();

    /**
     * @param isDefault whether it is the default (initial) altview
     */
    public abstract void setDefault(boolean isDefault);

    /**
     * @return it's owning view
     */
    public abstract ViewIFace getView();

    /**
     * @param mode the mode to use when creating the ViewDef the altview refers to
     */
    public abstract void setMode(CreationMode mode);

    /**
     * @return the name of the select (field name)
     */
    public abstract String getSelectorName();

    /**
     * @param selectorName the name of the select (field name)
     */
    public abstract void setSelectorName(String selectorName);

    /**
     * @return the actual value of the selector to switch between viewdefs
     */
    public abstract String getSelectorValue();

    /**
     * @param selectorValue the actual value of the selector to switch between viewdefs
     */
    public abstract void setSelectorValue(String selectorValue);

    /**
     * @return the list of subview for the altview
     */
    public abstract List<AltViewIFace> getSubViews();

    /**
     * @param subViews the list of subview for the altview
     */
    public abstract void setSubViews(List<AltViewIFace> subViews);
    
    /**
     * Appends its XML.
     * @param sb the buffer
     */
    public abstract void toXML(final StringBuilder sb);

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public abstract Object clone() throws CloneNotSupportedException;

    //-------------------------------------
    // Comparable
    //-------------------------------------
    public abstract int compareTo(AltViewIFace obj);

}
