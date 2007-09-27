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

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;


/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class FormViewDef extends ViewDef implements Cloneable, FormViewDefIFace
{
    protected String             columnDef      = "";
    protected String             rowDef         = "";
    protected List<FormRowIFace> rows           = new Vector<FormRowIFace>(); 
    protected String             definitionName = null;
    
    protected Hashtable<String, String>  enableRules = null;

    /**
     * @param type the type (could be form or field)
     * @param name the name
     * @param className the class name of the data object
     * @param gettableClassName the class name of the gettable
     * @param settableClassName the class name of the settable
     * @param desc description
      */
    public FormViewDef(final ViewDef.ViewType type, 
                        final String  name, 
                        final String  className, 
                        final String  gettableClassName, 
                        final String  settableClassName, 
                        final String  desc)
    {
        super(type, name, className, gettableClassName, settableClassName, desc);
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDerivedInterface()
     */
    public Class<?> getDerivedInterface()
    {
        return FormViewDefIFace.class;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#addRow(edu.ku.brc.ui.forms.persist.FormRowIFace)
     */
    public FormRowIFace addRow(FormRowIFace row)
    {
        rows.add(row);
        return row;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getRows()
     */
    public List<FormRowIFace> getRows()
    {
        return rows;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getFormCellById(java.lang.String)
     */
    public FormCellIFace getFormCellById(String idStr)
    {
        for (FormRowIFace row : rows)
        {
            for (FormCellIFace c : row.getCells())
            {
                if (c.getIdent().equals(idStr))
                {
                    return c;
                }
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getFormCellByName(java.lang.String)
     */
    public FormCellIFace getFormCellByName(final String nameStr)
    {
        for (FormRowIFace row : rows)
        {
            for (FormCellIFace c : row.getCells())
            {
                if (c.getName().equals(nameStr))
                {
                    return c;
                }
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormView#cleanUp()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        super.cleanUp();
        for (FormRowIFace row : rows)
        {
            row.cleanUp();
        }
        rows.clear();
        enableRules.clear();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getColumnDef()
     */
    public String getColumnDef()
    {
        return columnDef;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setColumnDef(java.lang.String)
     */
    public void setColumnDef(String columnDef)
    {
        this.columnDef = columnDef;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getRowDef()
     */
    public String getRowDef()
    {
        return rowDef;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setRowDef(java.lang.String)
     */
    public void setRowDef(String rowDef)
    {
        this.rowDef = rowDef;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getEnableRules()
     */
    public Hashtable<String, String> getEnableRules()
    {
        return enableRules;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setEnableRules(java.util.Map)
     */
    public void setEnableRules(Hashtable<String, String> enableRules)
    {
        this.enableRules = enableRules;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getDefinitionName()
     */
    public String getDefinitionName()
    {
        return definitionName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setDefinitionName(java.lang.String)
     */
    public void setDefinitionName(String definitionName)
    {
        this.definitionName = definitionName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        FormViewDef fvd = (FormViewDef)super.clone();
        fvd.rows      = new Vector<FormRowIFace>(); 
        fvd.columnDef = columnDef;
        fvd.rowDef    = rowDef;
        for (FormRowIFace formRow : rows)
        {
            fvd.rows.add((FormRow)formRow.clone()); 
        }
        return fvd;      
    }
}
