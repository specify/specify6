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
    
    // Transient
    protected boolean   isDerived = false;
    
    /**
     * 
     */
    public FormCellLabel()
    {
        
    }
    
    /**
     * @param id
     * @param name
     * @param label
     * @param labelFor
     * @param iconName
     * @param recordObj
     * @param colspan
     */
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
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#isDerived()
     */
    @Override
    public boolean isDerived()
    {
        return isDerived;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#setDerived(boolean)
     */
    @Override
    public void setDerived(boolean isDerived)
    {
        this.isDerived = isDerived;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#getLabelFor()
     */
    @Override
    public String getLabelFor()
    {
        return labelFor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#setLabelFor(java.lang.String)
     */
    @Override
    public void setLabelFor(String labelFor)
    {
        this.labelFor = labelFor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#getIcon()
     */
    @Override
    public ImageIcon getIcon()
    {
        return icon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#setIcon(javax.swing.ImageIcon)
     */
    @Override
    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#isRecordObj()
     */
    @Override
    public boolean isRecordObj()
    {
        return isRecordObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#setRecordObj(boolean)
     */
    @Override
    public void setRecordObj(boolean isRecordObj)
    {
        this.isRecordObj = isRecordObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#getIconName()
     */
    @Override
    public String getIconName()
    {
        return iconName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#setIconName(java.lang.String)
     */
    @Override
    public void setIconName(String iconName)
    {
        this.iconName = iconName;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#clone()
     */
    @Override
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
    @Override
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
