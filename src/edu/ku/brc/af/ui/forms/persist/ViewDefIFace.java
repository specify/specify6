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
package edu.ku.brc.af.ui.forms.persist;

import edu.ku.brc.af.ui.forms.DataObjectGettable;
import edu.ku.brc.af.ui.forms.DataObjectSettable;

public interface ViewDefIFace extends Comparable<ViewDefIFace>
{
    public enum ViewType {form, table, field, formtable, iconview, rstable}
    
    /**
     * Returns the Class of the 'Sub-classed' Interface, for example ViewDefIFace, TanleDefIFace, or FormDefIFace
     * @return the class of the interface
     */
    public abstract Class<?> getDerivedInterface();
    
    /**
     * Clean up internal data 
     */
    public abstract void cleanUp();

    /**
     * @return the type of view def
     */
    public abstract ViewType getType();

    /**
     * @param type the type og View Def
     */
    public abstract void setType(final ViewType type);

    /**
     * @return a human readable description of the form
     */
    public abstract String getDesc();

    /**
     * @return the name of the form
     */
    public abstract String getName();

    /**
     * @param name the name of the form
     */
    public abstract void setName(String name);

    /**
     * @return the Java class name of the data that goes in the form
     */
    public abstract String getClassName();

    /**
     * @return The gettable for getting data out of the form
     */
    public abstract DataObjectGettable getDataGettable();

    /**
     * @return the settable for setting data into the form
     */
    public abstract DataObjectSettable getDataSettable();
    
    /**
     * @return the Settable's class name for getting the data out of the form
     */
    public abstract String getDataSettableName();

    /**
     * @return the gettable's class name for getting the data out of the form
     */
    public abstract String getDataGettableName();
    
    /**
     * @param dataSettableName the dataSettableName to set
     */
    public abstract void setDataSettableName(String dataSettableName);
    
    /**
     * @param dataGettableName the dataGettableName to set
     */
    public abstract void setDataGettableName(String dataGettableName);
    
    /**
     * @return whether it should localize the labels
     */
    public abstract boolean isUseResourceLabels();

    /**
     * @return whether it should localize the labels
     */
    public abstract String getResourceLabels();

    
    /**
     * @return whether this form support absolute layout or JGoodies
     */
    public abstract Boolean isAbsoluteLayout();
    
    /**
     * @return the xCoord
     */
    public abstract int getXCoord();

    /**
     * @param coord the xCoord to set
     */
    public abstract void setXCoord(int coord);

    /**
     * @return the yCoord
     */
    public abstract int getYCoord();

    /**
     * @param coord the yCoord to set
     */
    public abstract void setYCoord(int coord);

    /**
     * @return the width
     */
    public abstract int getWidth();

    /**
     * @param width the width to set
     */
    public abstract void setWidth(int width);

    /**
     * @return the height
     */
    public abstract int getHeight();

    /**
     * @param height the height to set
     */
    public abstract void setHeight(int height);
    
    /**
     * Appends its XML.
     * @param sb the buffer
     */
    public abstract void toXML(final StringBuilder sb);
    
    //-------------------------------------
    // Comparable
    //-------------------------------------
    public abstract int compareTo(ViewDefIFace obj);
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public abstract Object clone() throws CloneNotSupportedException;

}
