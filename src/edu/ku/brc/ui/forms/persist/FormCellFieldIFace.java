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

/**
 * Represents a Form Cell 'Field' in the UI. See the enum to as a list of those components it can create. The plugin enum
 * enables it to handle additional type of controls.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 29, 2007
 *
 */
public interface FormCellFieldIFace extends FormCellIFace
{
    // NOTE: textpl is not intended to be defined in the form, it is for internal use only
    public enum FieldType {text, formattedtext, label, dsptextfield, textfieldinfo, image, url, combobox, checkbox, spinner,
                           password, dsptextarea, textarea, browse, querycbx, list, colorchooser, button, progress, plugin,
                           textpl}

    /**
     * @return the number of text columns to use for text controls
     */
    public abstract int getTxtCols();

    /**
     * @param cols the number of text columns to use for text controls
     */
    public abstract void setTxtCols(int cols);

    /**
     * @return The type of control to use when in read-only of view mode
     */
    public abstract FieldType getDspUIType();

    /**
     * @param dspUIType
     */
    public abstract void setDspUIType(FieldType dspUIType);

    /**
     * @return  this represents and actual String.format format like '%s'
     */
    public abstract String getFormat();

    /**
     * @param format this represents and actual String.format format like '%s'
     */
    public abstract void setFormat(String format);

    /**
     * @return the name of the formatter to use
     */
    public abstract String getFormatName();

    /**
     * @param formatName the name of the formatter to use
     */
    public abstract void setFormatName(String formatName);

    /**
     * @return whether the field's data should be encrypted (password field)
     */
    public abstract boolean isEncrypted();

    /**
     * @param isEncrypted whether the field's data should be encrypted (password field)
     */
    public abstract void setEncrypted(boolean isEncrypted);

    /**
     * @return whether the field is required to have data before being saved
     */
    public abstract boolean isRequired();

    /**
     * @return wwhether it supports using the 'this' pointer
     */
    public abstract boolean useThisData();

    /**
     * @return the default value to use when it is created
     */
    public abstract String getDefaultValue();

    /**
     * @param defaultValue the default value to use when it is created
     */
    public abstract void setDefaultValue(String defaultValue);

    /**
     * @return whether the field is read-only (not editable)
     */
    public abstract boolean isReadOnly();

    /**
     * @param isReadOnly whether the field is read-only (not editable)
     */
    public abstract void setReadOnly(boolean isReadOnly);

    /**
     * @param isRequired whether the field is required to have data before being saved
     */
    public abstract void setRequired(boolean isRequired);

    /**
     * @param mode given the mode it returns whether it is an editable text field.
     * Some text field are converted to read0only label like controls.
     * @return
     */
    public abstract boolean isTextFieldForMode(AltView.CreationMode mode);
    
    /**
     * @return whether this field support text editing
     */
    public abstract boolean isTextField();

    /**
     * @return the human readable label name
     */
    public abstract String getLabel();

    /**
     * @param label the human readable label name
     */
    public abstract void setLabel(String label);

    /**
     * @return  the name of the pre-defined picklist it should use
     */
    public abstract String getPickListName();

    /**
     * @param pickListName the name of the pre-defined picklist it should use
     */
    public abstract void setPickListName(String pickListName);

    /**
     * @return he number of rows it should use for a JTextArea
     */
    public abstract int getTxtRows();

    /**
     * @param rows the number of rows it should use for a JTextArea
     */
    public abstract void setTxtRows(int rows);

    /**
     * @return
     */
    public abstract String getUIFieldFormatter();

    /**
     * @param uiFieldFormatter the name of the predefined UIFieldFormatter
     */
    public abstract void setUIFieldFormatter(String uiFieldFormatter);

    /**
     * @return the type of control the field is
     */
    public abstract FieldType getUiType();

    /**
     * @param uiType the type of control the field is
     */
    public abstract void setUiType(FieldType uiType);

    /**
     * @return the rule used for validation (Java code scripting)
     */
    public abstract String getValidationRule();

    /**
     * @param validationRule the rule used for validation (Java code scripting)
     */
    public abstract void setValidationRule(String validationRule);

    /**
     * @return when the control is validated (Focus, Change) default is Change.
     */
    public abstract String getValidationType();

    /**
     * @param validationType when the control is validated (Focus, Change) default is Change.
     */
    public abstract void setValidationType(String validationType);
    
    /**
     * @return whether it is a password
     */
    public abstract boolean isPassword();

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#clone()
     */
    //@SuppressWarnings("unchecked")
    //public abstract Object clone() throws CloneNotSupportedException;
    
    
}