/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.util.List;
import java.util.Vector;

/**
 * This represents a layout panel (JPanel) that supports JGoodies layout
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class FormCellPanel extends FormCell implements Cloneable, FormCellPanelIFace
{
    protected String colDef;
    protected String rowDef;
    protected String panelType;
    protected List<FormRowIFace> rows = new Vector<FormRowIFace>(); 

    /**
     * Constructor
     * @param name the name
     * @param id the id
     * @param colDef JGoodies column definition
     * @param rowDef JGoodies row definition
     * @param colspan the number of columns to span
     * @param rowspan the number of rows to span
     */
    public FormCellPanel(final String            id, 
                         final String            name,
                         final String            panelType, 
                         final String            colDef, 
                         final String            rowDef,
                         final int               colspan, 
                         final int               rowspan)
    {
        super(FormCellIFace.CellType.panel, id, name, colspan, rowspan);
        this.panelType    = panelType;
        this.ignoreSetGet = true;
        this.colDef       = colDef;
        this.rowDef       = rowDef;
    }
    
    /**
     * Add a row to the form
     * @param row the row to add
     * @return the row that was added
     */
    public FormRowIFace addRow(FormRow row)
    {
        rows.add(row);
        return row;
    }    
    
    public String getColDef()
    {
        return colDef;
    }

    public String getRowDef()
    {
        return rowDef;
    }

    public String getPanelType()
    {
        return panelType;
    }

    public List<FormRowIFace> getRows()
    {
        return rows;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        FormCellPanel fcp = (FormCellPanel)super.clone();
        fcp.rows         = new Vector<FormRowIFace>(); 
        fcp.panelType    = panelType;
        fcp.colDef       = colDef;
        fcp.rowDef       = rowDef;
        for (FormRowIFace formRow : rows)
        {
            fcp.rows.add((FormRow)formRow.clone());
        }
        return fcp;      
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#toXMLAttrs(java.lang.StringBuilder)
     */
    public void toXMLAttrs(StringBuilder sb)
    {
        xmlAttr(sb, "coldef", colDef);
        xmlAttr(sb, "rowdef", rowDef);
        xmlAttr(sb, "paneltype", panelType);
    }
}
