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
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

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
public final class FormCellField extends FormCell
{
    // NOTE: textpl is not intended to be defined in the form, it is for internal use only
    public enum FieldType {text, formattedtext, label, dsptextfield, textfieldinfo, image, url, combobox, checkbox, 
                           password, dsptextarea, textarea, browse, querycbx, treequerycbx, list, colorchooser, button, progress, plugin,
                           textpl}

    protected static DateWrapper scrDateFormat = null;
    
    protected FieldType uiType;
    protected FieldType dspUIType;
    protected String    format;
    protected String    formatName;
    protected String    uiFieldFormatter;
    protected boolean   isRequired     = false;
    protected boolean   isReadOnly     = false;
    protected boolean   isEncrypted    = false;
    protected String    label          = null;
    protected String    defaultValue   = null;
    
    protected Boolean defaultDateToday = null;
    
    protected String   pickListName       = null; // Comboboxes and TextFields

    // Needed for Text Components
    protected int      cols    = 10; // TextField and TextArea
    protected int      rows    = 1; // Text Area Only

    protected String   validationType = "";
    protected String   validationRule = "";

    protected boolean  isTextField    = false;
    protected boolean  isDSPTextField = false;

    protected Hashtable<String, String> properties = null;

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
    public FormCellField(final FormCell.CellType type,
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

    public void setProperties(final Hashtable<String, String> properties)
    {
        this.properties = properties;
    }

    public void addProperty(final String nameStr, final String value)
    {
        if (properties == null)
        {
            properties = new Hashtable<String, String>();
        }
        properties.put(nameStr, value);
    }

    public String getProperty(final String nameStr)
    {
        if (properties != null)
        {
            return properties.get(nameStr);
        }
        return null;
    }

    public int getPropertyAsInt(final String nameStr, final int defVal)
    {
        if (properties != null)
        {
            String str = properties.get(nameStr);
            if (StringUtils.isNotEmpty(str))
            {
                return Integer.parseInt(str);
            }
        } else
        {
            return defVal;
        }
        return -1;
    }

    public boolean getPropertyAsBoolean(final String nameStr, final boolean defVal)
    {
        if (properties != null)
        {
            String str = properties.get(nameStr);
            if (StringUtils.isNotEmpty(str))
            {
                return str.equalsIgnoreCase("true");
            }
        } else
        {
            return defVal;
        }
        return false;
    }

    public int getCols()
    {
        return cols;
    }

    public void setCols(int cols)
    {
        this.cols = cols;
    }

    public FieldType getDspUIType()
    {
        return dspUIType;
    }

    public void setDspUIType(FieldType dspUIType)
    {
        this.dspUIType = dspUIType;

        this.isDSPTextField = dspUIType == FieldType.dsptextfield || dspUIType == FieldType.dsptextarea;

    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public String getFormatName()
    {
        return formatName;
    }

    public void setFormatName(String formatName)
    {
        this.formatName = formatName;
    }

    public boolean isEncrypted()
    {
        return isEncrypted;
    }

    public void setEncrypted(boolean isEncrypted)
    {
        this.isEncrypted = isEncrypted;
    }

    public boolean isRequired()
    {
        return isRequired;
    }

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

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public boolean isReadOnly()
    {
        return isReadOnly;
    }

    public void setReadOnly(boolean isReadOnly)
    {
        this.isReadOnly = isReadOnly;
    }

    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    public boolean isTextField(AltView.CreationMode mode)
    {
        // A mode of "None" default to "Edit"
        return mode == AltView.CreationMode.View ? isDSPTextField : isTextField;
    }

    public void setTextField(boolean isTextField)
    {
        this.isTextField = isTextField;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getPickListName()
    {
        return pickListName;
    }

    public void setPickListName(String pickListName)
    {
        this.pickListName = pickListName;
    }

    public int getRows()
    {
        return rows;
    }

    public void setRows(int rows)
    {
        this.rows = rows;
    }

    public String getUIFieldFormatter()
    {
        return uiFieldFormatter;
    }

    public void setUIFieldFormatter(String uiFieldFormatter)
    {
        this.uiFieldFormatter = uiFieldFormatter;
    }

    public FieldType getUiType()
    {
        return uiType;
    }

    public void setUiType(FieldType uiType)
    {
        this.uiType = uiType;

        this.isTextField = uiType == FieldType.text ||
                           uiType == FieldType.formattedtext ||
                           uiType == FieldType.textarea;
    }

    public String getValidationRule()
    {
        return validationRule;
    }

    public void setValidationRule(String validationRule)
    {
        this.validationRule = validationRule;
    }

    public String getValidationType()
    {
        return validationType;
    }

    public void setValidationType(String validationType)
    {
        this.validationType = validationType;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#clone()
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
        fcf.properties = (Hashtable<String, String>)properties.clone();
        return fcf;      
    }

}
