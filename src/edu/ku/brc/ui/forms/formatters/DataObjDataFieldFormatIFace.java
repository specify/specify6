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
package edu.ku.brc.ui.forms.formatters;

import java.util.Properties;

/**
 * This interface represents a formatter for a data object. There are two types of formatters: Direct and indirect.<br>
 * A "direct" formatter implicitly knows how to format the data object being passed in. This enables us to support formatting
 * that is far more complex than anything we can describe in the XML. It also enabloes the internal formatter to 'key' off 
 * other data members contained in the data object.<br>
 * <br>
 * Indirect formatters use the XML description to perform a recursive formatting.<br>
 * <br>
 * Note: Formatters are 'switchable' meaning they can identify a field in the data object to determine which format they should
 * use. There can be any number of formats for switching, but usually it is just a couple.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jun 15, 2007 (this wrong)
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
     * The list of data field that are used to create the format.
     * @return the list of sub-fields.
     */
    public abstract DataObjDataField[] getFields();


}