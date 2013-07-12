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
package edu.ku.brc.af.ui.forms.formatters;

import java.util.Vector;

import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.util.Pair;

/**
 * This interface describes how a formatter should be used by the form system. The InBound and OutBound methods
 * are mainly used for numeric values that need to have the leading zeroes stripped or appended so they are transparent 
 * for users, but are required for sorting.
 *  
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jul 10, 2007
 *
 */
public interface UIFieldFormatterIFace
{
    public enum PartialDateEnum {None, Full, Month, Year, Search}
    public enum FormatterType   {generic, date, numeric} // all lower case to follow convention in uiformatters.xml

    /**
     * @return whether it is a system formatter that cannot be deleted.
     */
    public abstract boolean isSystem();
    
    /**
     * @return the list of fields for the format.
     */
    public abstract Vector<UIFieldFormatterField> getFields();
    
    /**
     * Returns the format field for year. There should only ever be one, but if there is more than one
     * then it returns the last one or the one with isByYear set to true.
     * @return the year field.
     */
    public abstract UIFieldFormatterField getYear();

    /**
     * The unique name of the format as referenced by the form system.
     * @param name the name
     */
    public abstract void setName(final String name);

    /**
     * @return The unique name of the format as referenced by the form system.
     */
    public abstract String getTitle();

    /**
     * @return The unique name of the field it can be applied to (unless it is a date).
     */
    public abstract String getFieldName();
    
    /**
     * @param fieldName unique name of the field it is attached to
     */
    public abstract void setFieldName(String fieldName);

     /**
     * @return A sample of the formatted field
     */
    public abstract String getSample();

    /**
    /**
     * The human readable and possibly localized name of the format, used in the editor.
     * @param title the title
     */
    public abstract void setTitle(final String title);

    /**
     * @return The human readable and possibly localized name of the format, used in the editor.
     */
    public abstract String getName();

    /**
     * @return the class of data object that the format is intended for
     */
    public abstract Class<?> getDataClass();
    
    /**
     * @param dataClass sets the data class (of the table)
     */
    public abstract void setDataClass(Class<?> dataClass);

    /**
     * @return true if it is the default formatter
     */
    public abstract boolean isDefault();

    /**
     * @param isDefault set this formatter as the default for a class of data objects.
     */
    public abstract void setDefault(boolean isDefault);

    /**
     * @return true if this formatter has an incrementer field.
     */
    public abstract boolean isIncrementer();
    
    /**
     * Sets whether the formatter is an incrementer.
     * @param isIncrementer true is incrementer
     */
    public abstract void setIncrementer(boolean isIncrementer);

    /**
     * NOTE: If you are getting the length just to compare if the length is OK, then use @see #isLengthOK(int)
     * @return the character length of the entire format.
     */
    public abstract int getLength();
    
    /**
     * Return true if the length is ok, for some formatters the length may not matter.
     * @param lengthOfData the length of the data string
     * @return true if it is ok (even though it may not be equal), false if it isn't ok.
     */
    public abstract boolean isLengthOK(int lengthOfData);

    /**
     * This is used by the editor, it tells the formatter to recalculate
     * the length (for formats where calculating the length is expensive).
     */
    public abstract void resetLength();
    
    /**
     * a hint at how many characters to the UI should use for the field, most of the time this will return the 
     * same number as length.
     * @return a hint at how many characters to the UI should use for the field.
     */
    public abstract int getUILength();

    /**
     * @return the pair of values where the first number is the index into the Fields list and the second
     * is the index of where the field's format ends.
     */
    public abstract Pair<Integer, Integer> getIncPosition();

    /**
     * @return the pair of values where the first number is the index into the Fields list and the second
     * is the index of where the field's format ends.
     */
    public abstract Pair<Integer, Integer> getYearPosition();

    /**
     * @return is by year formatter?
     */
    public boolean getByYear();
    
    /**
     * Sets by year flag on first year field found.
     */
    public void setByYear(boolean byYear);
    
    /**
     * @return whether this formatter can be set by year or not.
     */
    public boolean byYearApplies();
    
    /**
     * @return the string pattern that is used in the UI to tell the user how the value should be entered, the length should 
     * match that of the formtat, this string is autogenerated.
     */
    public abstract String toPattern();

    /**
     * Indicates whether the value should be formatted on the way 'out' of the UI before it is set into the data object.
     * @return whether the value should be formatted on the way 'out' of the UI before it is set into the data object
     */
    public abstract boolean isFromUIFormatter();

    /**
     * Formats a value after retrieval from the UI before it goes to the data object.
     * @param data the value to be formatted
     * @return the new formatted value
     */
    public abstract Object formatFromUI(final Object data);

    /**
     * @return true if this formatter can for/should format values before they get to the UI
     */
    public abstract boolean isInBoundFormatter();

    /**
     * Formats a value before it goes to the UI.
     * @param data the value to be formatted
     * @return the new formatted value
     */
    public abstract Object formatToUI(final Object...data);

    /**
     * @return the class that is used for generating the next number in the sequence.
     */
    public abstract AutoNumberIFace getAutoNumber();

    /**
     * Sets the class that is used for generating the next number in the sequence.
     * @param autoNumber the autonumber generator class
     */
    public abstract void setAutoNumber(AutoNumberIFace autoNumber);

    /**
     * Given a formatted string it returns the next formatted string in the progression.
     * @param value the current largest formatted string in the sequence
     * @return the next incremented value in the sequence
     */
    public abstract String getNextNumber(String value);

    /**
     * @param value
     * @param incrementValue
     * @return next number.
     * 
     * If incrementValue is true, value is incremented to produce next number,
     * otherwise, the highest existing number is incremented.
     */
    public abstract String getNextNumber(final String value, final boolean incrementValue);

    /**
     * @return true if part of the format needs user input, false it is auto-generated.
     */
    public abstract boolean isUserInputNeeded();
    
    /**
     * Checks to see if the value string matches the formatter
     * @param value the value
     * @return true it is valid
     */
    public abstract boolean isValid(String value);
    
    /**
     * Appends a presentation of itself in XML to the StringBuilder
     * @param sb the stringbuilder
     */
    public abstract void toXML(StringBuilder sb);
    
    //-----------------------------------------------------------------------
    // The Data Specific Methods
    //-----------------------------------------------------------------------
    
    /**
     * @return whether the format contains a dash character '-'
     */
    public abstract boolean hasDash();
    
    /**
     * @param type
     */
    public abstract void setType(FormatterType type);
    
    /**
     * Quick way to find out if it is a date formatter.
     * @return true if date formatter
     */
    public abstract boolean isDate();

    /**
     * Quick way to find out if it is a numeric formatter.
     * @return true if numeric formatter
     */
    public abstract boolean isNumeric();
    
    /**
     * @return returns the maximum value (when it is numeric)
     */
    public abstract Number getMaxValue();

    /**
     * @return returns the minimum value (when it is numeric)
     */
    public abstract Number getMinValue();

    /**
     * @return the dateWrapper the DateWrapper object for this formatter.
     */
    public abstract DateWrapper getDateWrapper();
    
    /**
     * Returns the type of Part date formatter it is.
     * @return the type of Part date formatter it is.
     */
    public abstract PartialDateEnum getPartialDateType();
    
    /**
     * @param partialDateType the date enum
     */
    public abstract void setPartialDateType(PartialDateEnum partialDateType);
    
    /**
     * @return cloned object
     */
    public abstract Object clone() throws CloneNotSupportedException;

}
