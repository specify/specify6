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
package edu.ku.brc.af.ui.forms.persist;

import javax.swing.ImageIcon;

/**
 * Represents a Label in the UI.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 29, 2007
 *
 */
public interface FormCellLabelIFace extends FormCellSeparatorIFace
{
    /**
     * @return what form control this label is for
     */
    public abstract String getLabelFor();

    /**
     * @param labelFor what form control this label is for
     */
    public abstract void setLabelFor(String labelFor);

    /**
     * @return  the icon created form the icon name
     */
    public abstract ImageIcon getIcon();

    /**
     * @param icon the icon created form the icon name
     */
    public abstract void setIcon(ImageIcon icon);

    /**
     * @return whether this should create a special control that enables the user to drag the record ID form the icon in the separator
     */
    public abstract boolean isRecordObj();

    /**
     * @param recordObj whether this should create a special control that enables the user to drag the record ID form the icon in the separator
     */
    public abstract void setRecordObj(boolean recordObj);

    /**
     * @return the name of the icon to use for the separator
     */
    public abstract String getIconName();

    /**
     * @param iconName the name of the icon to use for the separator
     */
    public abstract void setIconName(String iconName);
    
    /**
     * @return the isDerived
     */
    public abstract boolean isDerived();

    /**
     * @param isDerived the isDerived to set
     */
    public abstract void setDerived(boolean isDerived);

    //public abstract Object clone() throws CloneNotSupportedException;

}