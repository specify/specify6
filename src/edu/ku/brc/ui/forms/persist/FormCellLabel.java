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

import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.IconManager;

/*
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class FormCellLabel extends FormCellSeparator
{
    protected String    labelFor;
    protected String    iconName;
    protected ImageIcon icon;
    protected boolean   recordObj;
    
    public FormCellLabel(final String id, 
                         final String name, 
                         final String label, 
                         final String labelFor,
                         final String iconName,
                         final boolean recordObj, 
                         final int colspan)
    {
        super(name, id, label, colspan);
        
        this.type      = FormCell.CellType.label;        
        this.labelFor  = labelFor;
        this.recordObj = recordObj;
        this.iconName  = iconName;
        
        icon = StringUtils.isNotEmpty(iconName) ? IconManager.getIcon(iconName, IconManager.IconSize.Std16) : null;
    }
    
    public String getLabelFor()
    {
        return labelFor;
    }

    public void setLabelFor(String labelFor)
    {
        this.labelFor = labelFor;
    }

    public ImageIcon getIcon()
    {
        return icon;
    }

    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
    }

    public boolean isRecordObj()
    {
        return recordObj;
    }

    public void setRecordObj(boolean recordObj)
    {
        this.recordObj = recordObj;
    }

    public String getIconName()
    {
        return iconName;
    }

    public void setIconName(String iconName)
    {
        this.iconName = iconName;
    }

}
