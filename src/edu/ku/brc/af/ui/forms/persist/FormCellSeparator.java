/* Copyright (C) 2009, University of Kansas Center for Research
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


/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class FormCellSeparator extends FormCell implements Cloneable, FormCellSeparatorIFace
{
    protected String label;
    protected String collapseCompName;
    
    /**
     * 
     */
    public FormCellSeparator()
    {
        
    }

    /**
     * @param id
     * @param name
     * @param label
     * @param colspan
     */
    public FormCellSeparator(final String id, 
                             final String name, 
                             final String label, 
                             final int    colspan)
    {
        this(id, name, label, null, colspan);
    }    
    
    /**
     * @param id
     * @param name
     * @param label
     * @param collapseCompName
     * @param colspan
     */
    public FormCellSeparator(final String id, 
                             final String name, 
                             final String label, 
                             final String collapseCompName,
                             final int    colspan)
     {
         super(FormCellIFace.CellType.separator, id, name);
         
         this.label   = label;
         this.collapseCompName   = collapseCompName;
         this.colspan = colspan;
         this.ignoreSetGet = true;
     }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSeparatorIFace#getLabel()
     */
    @Override
    public String getLabel()
    {
        return label;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSeparatorIFace#setLabel(java.lang.String)
     */
    @Override
    public void setLabel(String label)
    {
        this.label = label;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSeparatorIFace#getCollapseCompName()
     */
    @Override
    public String getCollapseCompName()
    {
        return collapseCompName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSeparatorIFace#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        FormCellSeparator cellSep = (FormCellSeparator)super.clone();
        cellSep.label             = label;
        cellSep.collapseCompName  = collapseCompName;
        return cellSep;      
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return label + " (separator)";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#toXMLAttrs(java.lang.StringBuilder)
     */
    @Override
    public void toXMLAttrs(StringBuilder sb)
    {
        xmlAttr(sb, "label",     label);
        xmlAttr(sb, "collapse",  collapseCompName);
    }
}
