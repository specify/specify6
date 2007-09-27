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

import java.util.List;

public interface AltViewIFace
{
    public enum CreationMode {None, Edit, View, Search}

    public abstract CreationMode getMode();

    public abstract String getViewDefName();

    public abstract String getLabel();

    public abstract void setLabel(String label);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract boolean isValidated();

    public abstract ViewDefIFace getViewDef();

    public abstract void setViewDef(ViewDefIFace viewDef);

    public abstract boolean isDefault();

    public abstract void setDefault(boolean isDefault);

    public abstract ViewIFace getView();

    public abstract void setMode(CreationMode mode);

    public abstract String getSelectorName();

    public abstract void setSelectorName(String selectorName);

    public abstract String getSelectorValue();

    public abstract void setSelectorValue(String selectorValue);

    public abstract List<AltViewIFace> getSubViews();

    public abstract void setSubViews(List<AltViewIFace> subViews);

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public abstract Object clone() throws CloneNotSupportedException;

    //-------------------------------------
    // Comparable
    //-------------------------------------
    public abstract int compareTo(AltViewIFace obj);

}