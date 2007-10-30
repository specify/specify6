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

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.IconManager;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class FormCellLabel extends FormCellSeparator implements FormCellLabelIFace
{
    protected String    labelFor;
    protected String    iconName;
    protected ImageIcon icon;
    protected boolean   isRecordObj;
    
    public FormCellLabel()
    {
        
    }
    
    public FormCellLabel(final String id, 
                         final String name, 
                         final String label, 
                         final String labelFor,
                         final String iconName,
                         final boolean recordObj, 
                         final int colspan)
    {
        super(name, id, label, colspan);
        
        this.type        = FormCellIFace.CellType.label;        
        this.labelFor    = labelFor;
        this.isRecordObj = recordObj;
        this.iconName    = iconName;
        
        icon = StringUtils.isNotEmpty(iconName) ? IconManager.getIcon(iconName, IconManager.IconSize.Std16) : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#getLabelFor()
     */
    public String getLabelFor()
    {
        return labelFor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#setLabelFor(java.lang.String)
     */
    public void setLabelFor(String labelFor)
    {
        this.labelFor = labelFor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#getIcon()
     */
    public ImageIcon getIcon()
    {
        return icon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#setIcon(javax.swing.ImageIcon)
     */
    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#isRecordObj()
     */
    public boolean isRecordObj()
    {
        return isRecordObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#setRecordObj(boolean)
     */
    public void setRecordObj(boolean isRecordObj)
    {
        this.isRecordObj = isRecordObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#getIconName()
     */
    public String getIconName()
    {
        return iconName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#setIconName(java.lang.String)
     */
    public void setIconName(String iconName)
    {
        this.iconName = iconName;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        FormCellLabel fcl = (FormCellLabel)super.clone();

        fcl.labelFor  = labelFor;
        fcl.isRecordObj = isRecordObj;
        fcl.iconName  = iconName;
        fcl.icon      = icon;
        
        return fcl;      
    }
    

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return label + " (label)";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#toXMLAttrs(java.lang.StringBuilder)
     */
    public void toXMLAttrs(StringBuilder sb)
    {
        xmlAttr(sb, "label",     label);
        xmlAttr(sb, "icon",      iconName);
        if (isRecordObj)
        {
            xmlAttr(sb, "recordobj", isRecordObj);
        }
        xmlAttr(sb, "labelfor",  labelFor);
    }
}
