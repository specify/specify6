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

import edu.ku.brc.ui.forms.DataObjectGettable;
import edu.ku.brc.ui.forms.DataObjectSettable;

public interface ViewDefIFace
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

    public abstract ViewType getType();

    public abstract void setType(final ViewType type);

    public abstract String getDesc();

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getClassName();

    public abstract String getDataGettableName();

    public abstract DataObjectGettable getDataGettable();

    public abstract DataObjectSettable getDataSettable();
    
    public abstract Boolean isAbsoluteLayout();

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public abstract Object clone() throws CloneNotSupportedException;

}