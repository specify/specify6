/* Filename:    $RCSfile: FormCellField.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.forms.persist;

/**
 * This represents all the information about a cell in the form.
 * @author rods
 *
 */
public class FormCellField extends FormCell
{
    protected String   uiType;
    protected String   format;   
    protected boolean  isRequired     = false;
    protected boolean  isEncrypted    = false;
    protected String   label          = null;
    
    protected String   pickListName       = null; // Comboboxes and TextFields
    
    // Needed for Text Components
    protected int      cols    = 10; // TextField and TextArea
    protected int      rows    = 1; // Text Area Only
    
    protected String   validationType = "";
    protected String   validationRule = "";      
    protected String   initialize     = "";      
   
    /**
     * Constructor
     * @param type type of cell
     * @param name the name
     * @param colspan the number of columns to span
     * @param rowspan the number of rows to span
     */
    public FormCellField(final FormCell.CellType type, 
                         final String            name,
                         final int               colspan, 
                         final int               rowspan)
    {
        super(type, name, colspan, rowspan);
    }

    /**
     * @param type type of cell
     * @param name the name
     * @param uiType the type of ui component to be created (i.e. "checkbox", "textfield")
     * @param format the format for a text field
     * @param isRequired whether the control MUST have value
     * @param cols the number of default columns to make the text field/area
     * @param rows the number of default rows to make TextArea
     * @param colspan the number of columns to span
     * @param rowspan the number of rows to span
     * @param validationType the type of validation (when to validate)
     * @param validationRule the rule on how to validate
     * @param isEncrypted whether the control should have its value encrypted
     */
    public FormCellField(final FormCell.CellType type, 
                         final String  name, 
                         final String  uiType, 
                         final String  format,
                         final boolean isRequired, 
                         final int     cols, 
                         final int     rows, 
                         final int     colspan, 
                         final int     rowspan,
                         final String  validationType,
                         final String  validationRule,
                         final boolean isEncrypted)
    {
        this(type, name, colspan, rowspan);
        
        this.uiType         = uiType;
        this.format         = format;
        this.cols           = cols;
        this.rows           = rows;
        this.validationRule = validationRule;
        this.validationType = validationType;
        this.isRequired     = isRequired;
        this.isEncrypted    = isEncrypted;
    }

    public int getCols()
    {
        return cols;
    }

    public void setCols(int cols)
    {
        this.cols = cols;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setType(CellType type)
    {
        this.type = type;
    }

    public String getUiType()
    {
        return uiType;
    }

    public void setUiType(String uiType)
    {
        this.uiType = uiType;
    }

    public int getRows()
    {
        return rows;
    }

    public String getPickListName()
    {
        return pickListName;
    }

    public void setPickListName(String pickListName)
    {
        this.pickListName = pickListName;
    }

    public void setRows(int rows)
    {
        this.rows = rows;
    }

    public String getValidationRule()
    {
        return validationRule;
    }

    public void setValidationRule(String validationRule)
    {
        this.validationRule = validationRule;
    }

    public String getInitialize()
    {
        return initialize;
    }

    public void setInitialize(String initialize)
    {
        this.initialize = initialize;
    }

    public boolean isRequired()
    {
        return isRequired;
    }

    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    public String getValidationType()
    {
        return validationType;
    }

    public void setValidationType(String validationType)
    {
        this.validationType = validationType;
    }

    public boolean isEncrypted()
    {
        return isEncrypted;
    }

    public void setEncrypted(boolean isEncrypted)
    {
        this.isEncrypted = isEncrypted;
    }



    public String getLabel()
    {
        return label;
    }



    public void setLabel(String label)
    {
        this.label = label;
    }
    
}
