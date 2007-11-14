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
    
    /**
     * @return the class of the subview
     */
    public abstract String getClassDesc();

    /**
     * @param classDesc
     */
    public abstract void setClassDesc(String classDesc);

    /**
     * @return
     */
    public abstract String getViewName();

    /**
     * @param viewName
     */
    public abstract void setView(String viewName);

    /**
     * @return
     */
    public abstract String getViewSetName();

    /**
     * @param viewSetName
     */
    public abstract void setViewSetName(String viewSetName);

    /**
     * @return
     */
    public abstract boolean isSingleValueFromSet();

    /**
     * @return
     */
    public abstract String getDescription();

    /**
     * @return
     */
    public abstract String getDefaultAltViewType();

    /**
     * @return
     */
    public abstract int getTableRows();

    /**
     * @param tableRows
     */
    public abstract void setTableRows(int tableRows);
    
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
     * @return a comma separated list of function modes.
     */
    public abstract String getFuncModes();
    
    /**
     * Fills the list with the available function modes.
     * @param list the list to be filled
     */
    public abstract void fillWithFuncModes(List<Modes> list);
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    //public abstract Object clone() throws CloneNotSupportedException;

}