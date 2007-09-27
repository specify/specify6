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

public interface FormCellSubViewIFace extends FormCellIFace
{
    public enum Modes {viewOnly, editAll, newOnly, search}
    
    public abstract String getClassDesc();

    public abstract void setClassDesc(String classDesc);

    public abstract String getViewName();

    public abstract void setView(String viewName);

    public abstract String getViewSetName();

    public abstract void setViewSetName(String viewSetName);

    public abstract boolean isSingleValueFromSet();

    public abstract String getDescription();

    public abstract String getDefaultAltViewType();

    public abstract int getTableRows();

    public abstract void setTableRows(int tableRows);
    
    public abstract void getModes(List<Modes> list);
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    //public abstract Object clone() throws CloneNotSupportedException;

}