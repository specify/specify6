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

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.AutoNumberIFace;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.util.Pair;


/**
 * This class describes a format for a string. The format is divided up into fields and each field has a type
 * describing what kind of values it can accept.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 17, 2007
 *
 */
public class UIFieldFormatter implements UIFieldFormatterIFace
{
    public enum PartialDateEnum {None, Full, Month, Year}
    
    protected String               name;
    protected String               title;
    protected Class<?>             dataClass;
    protected boolean              isDate;
    protected PartialDateEnum      partialDateType;
    protected boolean              isDefault;
    protected List<UIFieldFormatterField> fields;
    protected boolean              isIncrementer;
    protected DateWrapper          dateWrapper = null;
    protected AutoNumberIFace      autoNumber  = null;

    /**
     * Constructor.
     * @param name the unique name of the formatter
     * @param isDate whether it is a date formatter
     * @param partialDateType the type of date formatter (if it is one)
     * @param dataClass the class of data that it operates on
     * @param isDefault whether it is the default formatter
     * @param isIncrementer whether it can/should increment the value
     * @param fields the list of fields that make up the formatter
     */
    public UIFieldFormatter(final String          name, 
                            final boolean         isDate, 
                            final PartialDateEnum partialDateType,
                            final Class<?>        dataClass,
                            final boolean         isDefault,
                            final boolean         isIncrementer,
                            final List<UIFieldFormatterField> fields)
    {
        this.name            = name;
        this.dataClass       = dataClass;
        this.partialDateType = partialDateType;
        this.isDate          = isDate;
        this.isDefault       = isDefault;
        this.fields          = fields;
        this.isIncrementer   = isIncrementer;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getTitle()
     */
    public String getTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setTitle(java.lang.String)
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getFields()
     */
    public List<UIFieldFormatterField> getFields()
    {
        return fields;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getYear()
     */
    public UIFieldFormatterField getYear()
    {
        UIFieldFormatterField year = null;
        for (UIFieldFormatterField field : fields)
        {
            if (field.getType() == UIFieldFormatterField.FieldType.year)
            {
                if (year != null)
                {
                    if (field.isByYear())
                    {
                        return field;
                    }
                } else if (field.isByYear())
                {
                    return field;
                } else
                {
                    year = field;
                }
            }
        }
        return year;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setName(java.lang.String)
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isDate()
     */
    public boolean isDate()
    {
        return isDate;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return dataClass;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isDefault()
     */
    public boolean isDefault()
    {
        return isDefault;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setDefault(boolean)
     */
    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isIncrementer()
     */
    public boolean isIncrementer()
    {
        return isIncrementer;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setIncrementer(boolean)
     */
    public void setIncrementer(boolean isIncrementer)
    {
        this.isIncrementer = isIncrementer;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getPartialDateType()
     */
    public PartialDateEnum getPartialDateType()
    {
        return partialDateType;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getLength()
     */
    public int getLength()
    {
        int len = 0;
        for (UIFieldFormatterField field : fields)
        {
            len += field.getSize();
        }
        return len;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getIncPosition()
     */
    public Pair<Integer, Integer> getIncPosition()
    {
        int len = 0;
        for (UIFieldFormatterField field : fields)
        {
            if (field.isIncrementer())
            {
                return new Pair<Integer, Integer>(len, len+field.getSize());
            }
            len += field.getSize();
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getYearPosition()
     */
    public Pair<Integer, Integer> getYearPosition()
    {
        int len = 0;
        for (UIFieldFormatterField field : fields)
        {
            if (field.getType() == UIFieldFormatterField.FieldType.year)
            {
                return new Pair<Integer, Integer>(len, len+field.getSize());
            }
            len += field.getSize();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#toPattern()
     */
    public String toPattern()
    {
        StringBuilder str = new StringBuilder();
        for (UIFieldFormatterField field : fields)
        {
            str.append(field.getValue());
        }
        return str.toString();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getDateWrapper()
     */
    public DateWrapper getDateWrapper()
    {
        return dateWrapper;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setDateWrapper(edu.ku.brc.ui.DateWrapper)
     */
    public void setDateWrapper(final DateWrapper dateWrapper)
    {
        this.dateWrapper = dateWrapper;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isOutBoundFormatter()
     */
    public boolean isOutBoundFormatter()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#formatOutBound(java.lang.Object)
     */
    public Object formatOutBound(final Object data)
    {
        throw new RuntimeException("Can't call this when isOutBoundFormatter returns false.");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getAutoNumber()
     */
    public AutoNumberIFace getAutoNumber()
    {
        return autoNumber;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getAutoNumber(edu.ku.brc.dbsupport.AutoNumberIFace)
     */
    public void setAutoNumber(AutoNumberIFace autoNumber)
    {
        this.autoNumber = autoNumber;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getNextNumber(java.lang.String)
     */
    public String getNextNumber(String value)
    {
        if (autoNumber != null)
        {
            return autoNumber.getNextNumber(this, value);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#formatInBound(java.lang.Object)
     */
    public Object formatInBound(Object data)
    {
        boolean isStr = data instanceof String;
        
        if (autoNumber != null && isStr && StringUtils.isEmpty((String)data))
        {
           String pattern = toPattern();
           UIFieldFormatterField yearField = getYear();
           if (yearField != null)
           {
               Pair<Integer, Integer> pos = getYearPosition();
               if (pos != null)
               {
                   StringBuilder sb = new StringBuilder(pattern);
                   Calendar cal = Calendar.getInstance();
                   sb.replace(pos.first, pos.second, Integer.toString(cal.get(Calendar.YEAR)));
                   return sb.toString();
               }
           }
           return pattern;
        }
        return data;
        //throw new RuntimeException("Can't call this when isInBoundFormatter returns false.");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isInBoundFormatter()
     */
    public boolean isInBoundFormatter()
    {
        return autoNumber != null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Name["+name+"] isDate["+isDate+"]  dataClass["+dataClass.getSimpleName()+"] isDefault["+isDefault+"] isIncrementor["+isIncrementer+"]");
        for (UIFieldFormatterField f : fields)
        {
            s.append("\n  "+f.toString());
        }
        return s.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isUserInputNeeded()
     */
    public boolean isUserInputNeeded()
    {
        for (UIFieldFormatterField f : fields)
        {
            UIFieldFormatterField.FieldType type = f.getType();
            if (type != UIFieldFormatterField.FieldType.alphanumeric ||
                type != UIFieldFormatterField.FieldType.alpha)
            {
                return true;
                
            } else if (type != UIFieldFormatterField.FieldType.numeric && !f.isIncrementer())
            {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isNumericOnly()
     */
    public boolean isNumericOnly()
    {
        return false;
    }
    
    
}


