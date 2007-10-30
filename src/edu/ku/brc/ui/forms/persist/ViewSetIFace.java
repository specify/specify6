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
package edu.ku.brc.ui.forms.persist;

import java.util.Hashtable;

public interface ViewSetIFace extends Comparable<ViewSetIFace>
{
    public enum Type {System, User}

    /**
     * Cleans up intneral data.
     */
    public abstract void cleanUp();

    /**
     * Gets a view by name.
     * @param nameStr name of view to be retrieved
     * @return the view or null if it isn't found
     */
    public abstract ViewIFace getView(final String nameStr);

    /**
     * Get all the views. It loads them if they have not been loaded yet.
     * @return the vector of all the view in the ViewSet
     */
    public abstract Hashtable<String, ViewIFace> getViews();

    /**
     * Returns all the ViewDefs.
     * @return all the ViewDefs.
     */
    public abstract Hashtable<String, ViewDefIFace> getViewDefs();

    /**
     * Gets the name.
     * @return the name of the viewset
     */
    public abstract String getName();

    /**
     * Returns the type of ViewSet it is.
     * @return the type of ViewSet it is
     */
    public abstract Type getType();

    /**
     * Sets the name.
     * @param name the name of the viewset
     */
    public abstract void setName(final String name);

    /**
     * Returns the title.
     * @return the title
     */
    public abstract String getTitle();

    /**
     * Returns file name (no path)
     * @return file name (no path)
     */
    public abstract String getFileName();

    /**
     * Indicates that is contains the core set of forms that can be referred in other places with specifying the viewset name.
     * @return that is contains the core set of forms that can be referred in other places with specifying the viewset name
     */
    public abstract boolean isSystem();
    
    /**
     * Appends its XML.
     * @param sb the buffer
     */
    public abstract void toXML(final StringBuilder sb);


    /**
     * Comparator.
     * @param obj the obj to compare
     * @return 0,1,-1
     */
    public abstract int compareTo(ViewSetIFace obj);

}