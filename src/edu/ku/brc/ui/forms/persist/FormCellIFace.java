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

import java.util.Properties;

public interface FormCellIFace
{
    public enum CellType {separator, field, label, statictext, subview, command, panel, statusbar, iconview}
    
    public abstract int getColspan();

    public abstract String getName();

    public abstract String getIdent();

    public abstract int getRowspan();

    public abstract CellType getType();

    public abstract boolean isIgnoreSetGet();

    public abstract boolean isChangeListenerOnly();

    public abstract boolean isMultiField();

    public abstract String[] getFieldNames();

    public abstract void setChangeListenerOnly(boolean changeListenerOnly);

    public abstract void setColspan(int colspan);

    public abstract void setFieldNames(String[] fieldNames);

    public abstract void setIgnoreSetGet(boolean ignoreSetGet);

    public abstract void setMultiField(boolean isMultiField);

    public abstract void setName(String name);

    public abstract void setIdent(String id);

    public abstract void setRowspan(int rowspan);

    public abstract void setType(CellType type);

    public abstract int compareTo(FormCellIFace obj);

    public abstract void setProperties(final Properties properties);

    public abstract void addProperty(final String nameStr, final String value);

    public abstract String getProperty(final String nameStr);

    public abstract int getPropertyAsInt(final String nameStr, final int defVal);

    public abstract boolean getPropertyAsBoolean(final String nameStr, final boolean defVal);

    public abstract Properties getProperties();

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public abstract Object clone() throws CloneNotSupportedException;

}