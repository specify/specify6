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
package edu.ku.brc.af.ui.forms.formatters;

import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 * This interface represents a formatter for a data object. There are two types of formatters: Direct and indirect.<br>
 * A "direct" formatter implicitly knows how to format the data object being passed in. This enables us to support formatting
 * that is far more complex than anything we can describe in the XML. It also enables the internal formatter to 'key' off 
 * other data members contained in the data object.<br>
 * <br>
 * Indirect formatters use the XML description to perform a recursive formatting.<br>
 * <br>
 * Note: Formatters are 'switchable' meaning they can identify a field in the data object to determine which format they should
 * use. There can be any number of formats for switching, but usually it is just a couple.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Jan 18, 2007
 *
 */
public interface DataObjDataFieldFormatIFace
{
    /**
     * Initialization parameters.
     * @param name the name of the formatter
     * @param properties the properties
     */
    public void init(String name, Properties properties);
    
    /**
     * The class of the data object being passed in to be formatted.
     * @return The class of the data object being passed in to be formatted.
     */
    public abstract Class<?> getDataClass();

    /**
     * The name of the formatter.
     * @return The name of the formatter.
     */
    public abstract String getName();

    /**
     * Indicates whether it is the default formatter for a class of data objects.
     * @return Indicates whether it is the default formatter for a class of data objects.
     */
    public abstract boolean isDefault();

    /**
     * Indicates whether the implementing object knows implicitly how to format the data object
     * by calling 'format'.
     * @return Indicates whether the implementing object knows implicitly how to format the data object
     * by calling 'format'.
     */
    public abstract boolean isDirectFormatter();
    
    /**
     * @return returns the name of the field if there is only one and not dot notation.
     */
    public abstract String getSingleField();
    
    /**
     * Format the data object (as a Direct formatter).
     * @param dataValue the data value to be formatted
     * @return the formatted object as a string
     */
    public abstract String format(Object dataValue);
    
    /**
     * When the formatter is part of a 'switchable' formatter this is the value that the switch uses to determine
     * which formatter to use.
     * @return The value used by the switch
     */
    public abstract String getValue();
    
    /**
     * When the formatter is part of a 'switchable' formatter this sets the value that the switch uses to determine
     * which formatter to use.
     * @param value The value used by the switch
     */
    public abstract void setValue(String value);
    
    /**
     * The list of data field that are used to create the format.
     * @return the list of sub-fields.
     */
    public abstract DataObjDataField[] getFields();
    
    /**
     * Returns a XML representation of the object 
     * @return String with XML representation of the object
     */
    public abstract void toXML(StringBuilder sb);
    
    /**
     * Tells it to set up the Table and Field Info.
     */
    public abstract void setTableAndFieldInfo();
    
    /**
     * After cloning make sure setDataObjSwitchFormatter is called.
     * @return a deep cloned object
     */
    public abstract Object clone() throws CloneNotSupportedException;
    
    /**
     * This needs to be called after clone. For most DataObjDataFieldFormat implementations this is a "no op"
     * and nothing needs to be done. For some, they may need the parent DataObjSwitchFormatter.
     * @param objFormatter the parent DataObjSwitchFormatter
     */
    public abstract void setDataObjSwitchFormatter(DataObjSwitchFormatter objFormatter);
    
    
    ///////////////////////////////////////
    //  This part is for custom editors  //
    ///////////////////////////////////////
    
    /**
     * @return whether it is a custom formatter.
     */
    public abstract boolean isCustom();
    
    /**
     * Asks for the Custom Editor Panel and provide it the OK btn to turn on or off when valid.
     * @param okBtn the Dialog OK Btn
     */
    public abstract JPanel getCustomEditor(ChangeListener l);
    
    /**
     * @return true if editor is valid.
     */
    public abstract boolean isValid();
    
    /**
     * @return true if the Custom formatter has an editor
     */
    public abstract boolean hasEditor();
    
    /**
     * @return a localized label for the panel.
     */
    public abstract String getLabel();
    
    /**
     * Tells the formatter that the editing is done and it should clean up.
     */
    public abstract void doneEditting(final boolean wasCancelled);

}