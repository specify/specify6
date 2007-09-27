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



public interface FormCellFieldIFace extends FormCellIFace
{
    // NOTE: textpl is not intended to be defined in the form, it is for internal use only
    public enum FieldType {text, formattedtext, label, dsptextfield, textfieldinfo, image, url, combobox, checkbox, spinner,
                           password, dsptextarea, textarea, browse, querycbx, list, colorchooser, button, progress, plugin,
                           textpl}

    public abstract int getTxtCols();

    public abstract void setTxtCols(int cols);

    public abstract FieldType getDspUIType();

    public abstract void setDspUIType(FieldType dspUIType);

    public abstract String getFormat();

    public abstract void setFormat(String format);

    public abstract String getFormatName();

    public abstract void setFormatName(String formatName);

    public abstract boolean isEncrypted();

    public abstract void setEncrypted(boolean isEncrypted);

    public abstract boolean isRequired();

    public abstract boolean useThisData();

    public abstract String getDefaultValue();

    public abstract void setDefaultValue(String defaultValue);

    public abstract boolean isReadOnly();

    public abstract void setReadOnly(boolean isReadOnly);

    public abstract void setRequired(boolean isRequired);

    public abstract boolean isTextField(AltView.CreationMode mode);

    public abstract void setTextField(boolean isTextField);

    public abstract String getLabel();

    public abstract void setLabel(String label);

    public abstract String getPickListName();

    public abstract void setPickListName(String pickListName);

    public abstract int getTxtRows();

    public abstract void setTxtRows(int rows);

    public abstract String getUIFieldFormatter();

    public abstract void setUIFieldFormatter(String uiFieldFormatter);

    public abstract FieldType getUiType();

    public abstract void setUiType(FieldType uiType);

    public abstract String getValidationRule();

    public abstract void setValidationRule(String validationRule);

    public abstract String getValidationType();

    public abstract void setValidationType(String validationType);

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#clone()
     */
    //@SuppressWarnings("unchecked")
    //public abstract Object clone() throws CloneNotSupportedException;
    
    
}