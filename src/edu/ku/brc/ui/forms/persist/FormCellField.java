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

import java.util.Date;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.ui.DateWrapper;

/**
 * This represents all the information about a cell in the form. This implements "clone".
 * 
 * @code_status Beta
 *.
 * @author rods
 *
 */
public final class FormCellField extends FormCell implements FormCellFieldIFace
{
    protected static DateWrapper scrDateFormat = null;
    
    protected FieldType uiType;
    protected FieldType dspUIType;
    protected String    format;
    protected String    formatName;
    protected String    uiFieldFormatter;
    protected boolean   isRequired     = false;
    protected boolean   isReadOnly     = false;
    protected boolean   isEncrypted    = false;
    protected boolean   useThisData    = false; // this means the field uses the entire data object to do something special with
    protected String    label          = null;
    protected String    defaultValue   = null;
    
    protected Boolean defaultDateToday = null;
    
    protected String   pickListName       = null; // Comboboxes and TextFields

    // Needed for Text Components
    protected int      cols    = 10; // TextField and TextArea
    protected int      rows    = 1;  // Text Area Only

    protected String   validationType = "";
    protected String   validationRule = "";

    protected boolean  isTextField    = false;
    protected boolean  isDSPTextField = false;

    /**
     * Constructor
     * @param type type of cell
     * @param id the id
     * @param name the name
     * @param colspan the number of columns to span
     * @param rowspan the number of rows to span
     */
    protected FormCellField(final FormCell.CellType type,
                            final String            id,
                            final String            name,
                            final int               colspan,
                            final int               rowspan)
    {
        super(type, id, name, colspan, rowspan);
        
        useThisData = name.equals("this");
    }

    /**
     * @param type type of cell
     * @param id the id
     * @param name the name
     * @param uiType the type of ui component to be created (i.e. "checkbox", "textfield")
     * @param format the format for a text field
     * @param formatName name of formatter to use
     * @param uiFieldFormatter name of UIFieldFormatter to use
     * @param isRequired whether the control MUST have value
     * @param cols the number of default columns to make the text field/area
     * @param rows the number of default rows to make TextArea
     * @param colspan the number of columns to span
     * @param rowspan the number of rows to span
     * @param validationType the type of validation (when to validate)
     * @param validationRule the rule on how to validate
     * @param isEncrypted whether the control should have its value encrypted
     */
    public FormCellField(final FormCellIFace.CellType type,
                         final String            id,
                         final String    name,
                         final FieldType uiType,
                         final FieldType dspUIType,
                         final String  format,
                         final String  formatName,
                         final String  uiFieldFormatter,
                         final boolean isRequired,
                         final int     cols,
                         final int     rows,
                         final int     colspan,
                         final int     rowspan,
                         final String  validationType,
                         final String  validationRule,
                         final boolean isEncrypted)
    {
        this(type, id, name, colspan, rowspan);

        this.format         = format;
        this.formatName     = formatName;
        this.uiFieldFormatter = uiFieldFormatter;
        this.cols           = cols;
        this.rows           = rows;
        this.validationRule = validationRule;
        this.validationType = validationType;
        this.isRequired     = isRequired;
        this.isEncrypted    = isEncrypted;

        // must use setters  because they set booleans as
        // to whether they are text controls
        setUiType(uiType);
        setDspUIType(dspUIType);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getCols()
     */
    public int getTxtCols()
    {
        return cols;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setCols(int)
     */
    public void setTxtCols(int cols)
    {
        this.cols = cols;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getDspUIType()
     */
    public FieldType getDspUIType()
    {
        return dspUIType;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setDspUIType(edu.ku.brc.ui.forms.persist.FormCellField.FieldType)
     */
    public void setDspUIType(FieldType dspUIType)
    {
        this.dspUIType = dspUIType;

        this.isDSPTextField = dspUIType == FieldType.dsptextfield || dspUIType == FieldType.dsptextarea;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getFormat()
     */
    public String getFormat()
    {
        return format;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setFormat(java.lang.String)
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getFormatName()
     */
    public String getFormatName()
    {
        return formatName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setFormatName(java.lang.String)
     */
    public void setFormatName(String formatName)
    {
        this.formatName = formatName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#isEncrypted()
     */
    public boolean isEncrypted()
    {
        return isEncrypted;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setEncrypted(boolean)
     */
    public void setEncrypted(boolean isEncrypted)
    {
        this.isEncrypted = isEncrypted;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#useThisData()
     */
    public boolean useThisData()
    {
        return useThisData;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getDefaultValue()
     */
    public String getDefaultValue()
    {
        if (defaultDateToday == null)
        {
            defaultDateToday = uiFieldFormatter.equals("Date") && defaultValue.equals("today");
        }
        
        if (defaultDateToday)
        {
            Date date = new Date();
            if (scrDateFormat == null)
            {
                scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
            }
            return scrDateFormat.format(date);
        }
        return defaultValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#isReadOnly()
     */
    public boolean isReadOnly()
    {
        return isReadOnly;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setReadOnly(boolean)
     */
    public void setReadOnly(boolean isReadOnly)
    {
        this.isReadOnly = isReadOnly;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#isTextField(edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode)
     */
    public boolean isTextFieldForMode(AltView.CreationMode mode)
    {
        // A mode of "None" default to "Edit"
        return mode == AltViewIFace.CreationMode.VIEW ? isDSPTextField : isTextField;
    }
    
    /**
     * @return the isTextField
     */
    public boolean isTextField()
    {
        return isTextField;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setTextField(boolean)
     */
    public void setTextField(boolean isTextField)
    {
        this.isTextField = isTextField;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getLabel()
     */
    public String getLabel()
    {
        return label;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setLabel(java.lang.String)
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getPickListName()
     */
    public String getPickListName()
    {
        return pickListName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setPickListName(java.lang.String)
     */
    public void setPickListName(String pickListName)
    {
        this.pickListName = pickListName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getRows()
     */
    public int getTxtRows()
    {
        return rows;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setRows(int)
     */
    public void setTxtRows(int rows)
    {
        this.rows = rows;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getUIFieldFormatter()
     */
    public String getUIFieldFormatter()
    {
        return uiFieldFormatter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setUIFieldFormatter(java.lang.String)
     */
    public void setUIFieldFormatter(String uiFieldFormatter)
    {
        this.uiFieldFormatter = uiFieldFormatter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getUiType()
     */
    public FieldType getUiType()
    {
        return uiType;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setUiType(edu.ku.brc.ui.forms.persist.FormCellField.FieldType)
     */
    public void setUiType(FieldType uiType)
    {
        this.uiType = uiType;

        this.isTextField = uiType == FieldType.text ||
                           uiType == FieldType.formattedtext ||
                           uiType == FieldType.textarea;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getValidationRule()
     */
    public String getValidationRule()
    {
        return validationRule;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setValidationRule(java.lang.String)
     */
    public void setValidationRule(String validationRule)
    {
        this.validationRule = validationRule;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getValidationType()
     */
    public String getValidationType()
    {
        return validationType;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setValidationType(java.lang.String)
     */
    public void setValidationType(String validationType)
    {
        this.validationType = validationType;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#clone()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#clone()
     */
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException
    {
        FormCellField fcf = (FormCellField)super.clone();
        fcf.uiType = uiType;
        fcf.dspUIType = dspUIType;
        fcf.format = format;
        fcf.formatName = formatName;
        fcf.uiFieldFormatter = uiFieldFormatter;
        fcf.isRequired = isRequired;
        fcf.isReadOnly = isReadOnly;
        fcf.isEncrypted = isEncrypted;
        fcf.label = label;
        fcf.defaultValue = defaultValue;
        fcf.defaultDateToday = defaultDateToday;
        fcf.pickListName = pickListName;
        fcf.cols = cols;
        fcf.rows = rows;
        fcf.validationType = validationType;
        fcf.validationRule = validationRule;
        fcf.isTextField = isTextField;
        fcf.isDSPTextField = isDSPTextField;
        return fcf;      
    }

}
