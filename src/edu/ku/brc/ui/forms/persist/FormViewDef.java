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
import java.util.Map;
import java.util.Vector;


/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class FormViewDef extends ViewDef implements Cloneable
{
    protected String        columnDef      = "";
    protected String        rowDef         = "";
    protected List<FormRow> rows           = new Vector<FormRow>(); 
    protected String        definitionName = null;
    
    protected Map<String, String>  enableRules = null;

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
    
    /**
     * Add a row to the form
     * @param row the row to add
     * @return the row that was added
     */
    public FormRow addRow(FormRow row)
    {
        rows.add(row);
        return row;
    }

    /**
     * @return all the rows
     */
    public List<FormRow> getRows()
    {
        return rows;
    }
    
    /**
     * Returns a FormCell by ID (searches the rows and then the columns)
     * @param idStr the ID of the field 
     * @return a FormCell by ID (searches the rows and then the columns)
     */
    public FormCell getFormCellById(String idStr)
    {
        for (FormRow row : rows)
        {
            for (FormCell c : row.getCells())
            {
                if (c.getId().equals(idStr))
                {
                    return c;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns a FormCell by name (searches the rows and then the columns)
     * @param nameStr the name of the field 
     * @return a FormCell by name (searches the rows and then the columns)
     */
    public FormCell getFormCellByName(final String nameStr)
    {
        for (FormRow row : rows)
        {
            for (FormCell c : row.getCells())
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
    @Override
    public void cleanUp()
    {
        super.cleanUp();
        for (FormRow row : rows)
        {
            row.cleanUp();
        }
        rows.clear();
        enableRules.clear();
    }

    public String getColumnDef()
    {
        return columnDef;
    }

    public void setColumnDef(String columnDef)
    {
        this.columnDef = columnDef;
    }

    public String getRowDef()
    {
        return rowDef;
    }

    public void setRowDef(String rowDef)
    {
        this.rowDef = rowDef;
    }

    public Map<String, String> getEnableRules()
    {
        return enableRules;
    }

    public void setEnableRules(Map<String, String> enableRules)
    {
        this.enableRules = enableRules;
    }
    
    public String getDefinitionName()
    {
        return definitionName;
    }

    public void setDefinitionName(String definitionName)
    {
        this.definitionName = definitionName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        FormViewDef fvd = (FormViewDef)super.clone();
        fvd.rows      = new Vector<FormRow>(); 
        fvd.columnDef = columnDef;
        fvd.rowDef    = rowDef;
        for (FormRow formRow : rows)
        {
            fvd.rows.add((FormRow)formRow.clone()); 
        }
        return fvd;      
    }
}
