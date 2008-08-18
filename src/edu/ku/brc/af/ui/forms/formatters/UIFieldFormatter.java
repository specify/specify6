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

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;
import static org.apache.commons.lang.StringUtils.isAlpha;
import static org.apache.commons.lang.StringUtils.isAlphanumeric;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.UIRegistry;
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
public class UIFieldFormatter implements UIFieldFormatterIFace, Cloneable
{
    public enum PartialDateEnum {None, Full, Month, Year}
    public enum FormatterType   {generic, date, numeric} // all lower case to follow convention in uiformatters.xml
    //private static final Logger log = Logger.getLogger(UIFieldFormatter.class);

    public static int[]            daysInMon = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; 
    protected static final String  deftitle = UIRegistry.getResourceString("FFE_DEFAULT");     

    protected String               fieldName;
    protected String               name;
    protected boolean              isSystem;
    protected String               title;
    protected Class<?>             dataClass;
    protected FormatterType        type;
    protected PartialDateEnum      partialDateType;
    protected boolean              isDefault;
    protected List<UIFieldFormatterField> fields;
    protected boolean              isIncrementer;
    protected DateWrapper          dateWrapper = null;
    protected AutoNumberIFace      autoNumber  = null;
    
    protected int                  precision = 0;
    protected int                  scale     = 0;
    
    protected Number               minValue = null;
    protected Number               maxValue = null;
    
    // Transient

    /**
     * Default constructor
     */
    public UIFieldFormatter() 
    {
        fields = new Vector<UIFieldFormatterField>();
    }
    
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
                            final boolean         isSystem,
                            final String          fieldName, 
                            final FormatterType   type, 
                            final PartialDateEnum partialDateType,
                            final Class<?>        dataClass,
                            final boolean         isDefault,
                            final boolean         isIncrementer,
                            final List<UIFieldFormatterField> fields)
    {
        this.name            = name;
        this.isSystem        = isSystem;
        this.fieldName       = fieldName;
        this.dataClass       = dataClass;
        this.partialDateType = partialDateType;
        this.type            = type;
        this.isDefault       = isDefault;
        this.fields          = fields;
        this.isIncrementer   = isIncrementer;
    }

    /**
     * @return the BG text
     */
    public String getText()
    {
    	return toPattern();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isSystem()
     */
    public boolean isSystem()
    {
        return isSystem;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getTitle()
     */
    public String getTitle()
    {
        return title == null ? name : title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setTitle(java.lang.String)
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getFieldName()
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getFields()
     */
    public List<UIFieldFormatterField> getFields()
    {
        return fields;
    }

    /*
     * Adds a field to the formatter
     * @param field Field being added
     */
    public void addField(UIFieldFormatterField field)
    {
    	if (fields == null) 
    	{
    		resetFields();
    	}
    	
    	fields.add(field);
    }

    /*
     * Resets formatter fields
     */
    public void resetFields()
    {
   		fields = new ArrayList<UIFieldFormatterField>();
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
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#byYearApplies()
     */
    public boolean byYearApplies()
    {
    	boolean hasYearField = false;
    	boolean hasAutoNumber = false;

    	for (UIFieldFormatterField field : fields)
        {
    		hasYearField |= field.isCurrentYear();
    		hasAutoNumber |= field.isIncrementer();
        }
    	
    	return (hasYearField && hasAutoNumber);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getByYear()
     */
    public boolean getByYear()
    {
    	for (UIFieldFormatterField field : fields)
        {
    		if (field.isByYear())
    			return true;
        }
    	
    	return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setByYear(boolean)
     */
    public void setByYear(boolean byYear)
    {
    	if (!byYearApplies())
    		return;
    	
    	for (UIFieldFormatterField field : fields)
        {
    		if (field.isCurrentYear())
    		{
    			field.setByYear(byYear);
    		}
        }
    }
    
    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(Number minValue)
    {
        this.minValue = minValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(Number maxValue)
    {
        this.maxValue = maxValue;
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
        return type == FormatterType.date;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isNumeric()
     */
    public boolean isNumeric()
    {
        return type == FormatterType.numeric;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getMaxValue()
     */
    public Number getMaxValue()
    {
        return maxValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getMinValue()
     */
    public Number getMinValue()
    {
        return minValue;
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
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getUILength()
     */
    public int getUILength()
    {
        return getLength();
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
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getSample()
     */
    public String getSample()
    {
        StringBuilder str = new StringBuilder();
        for (UIFieldFormatterField field : fields)
        {
            str.append(field.getSample());
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
    public boolean isFromUIFormatter()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#formatOutBound(java.lang.Object)
     */
    public Object formatFromUI(final Object data)
    {
        // Just pass it on through
        return data;
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
    public Object formatToUI(Object data)
    {
        boolean isStr = data instanceof String;
        
        if (autoNumber != null && isStr && isEmpty((String)data))
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
    	return toPattern() + (isDefault ? (' ' + deftitle) : "");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isUserInputNeeded()
     */
    public boolean isUserInputNeeded()
    {
        for (UIFieldFormatterField f : fields)
        {
            UIFieldFormatterField.FieldType typ = f.getType();
            if (typ == UIFieldFormatterField.FieldType.alphanumeric ||
                typ == UIFieldFormatterField.FieldType.alpha)
            {
                return true;
                
            } else if (typ == UIFieldFormatterField.FieldType.numeric && !f.isIncrementer())
            {
                return true;
                
            } else if (typ == UIFieldFormatterField.FieldType.year)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the type
     */
    public FormatterType getType()
    {
        return type;
    }

    /**
     * @return the scale
     */
    public int getScale()
    {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(int scale)
    {
        this.scale = scale;
    }

    /**
     * @return the precision
     */
    public int getPrecision()
    {
        return precision;
    }

    /**
     * @param precision the precision to set
     */
    public void setPrecision(int precision)
    {
        this.precision = precision;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isValid(java.lang.String)
     */
    public boolean isValid(final String text)
    {
        return isValid(this, text);
    }
    
    /**
     * @param year
     * @return
     */
    protected static boolean isLeapYear(final int year)
    {
        if (year % 4 == 0)
        {
            if (year % 100 == 0)
            {
                if (year % 400 == 0)
                {
                    return true;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * @param formatter
     * @param text
     * @return
     */
    private static boolean isDateValid(final UIFieldFormatterIFace formatter, final String text)
    {
        UIFieldFormatterField month = null;
        UIFieldFormatterField day   = null;
        UIFieldFormatterField year  = null;
        
        int monthInx = 0;
        int dayInx   = 0;
        int yearInx  = 0;
        int inx      = 0;
        
        for (UIFieldFormatterField field : formatter.getFields())
        {
            if (month == null && field.getValue().equals("MM"))
            {
                month    = field;
                monthInx = inx;
                
            } else if (day == null && field.getValue().equals("DD"))
            {
                day    = field;
                dayInx = inx;
                
            } else if (year == null && field.getValue().equals("YYYY"))
            {
                year    = field;
                yearInx = inx;
            }
            inx += field.getSize();
        }
        
        int yearVal = -1;
        if (year != null)
        {
            String val     = text.substring(yearInx, yearInx+year.getSize());
            yearVal = Integer.parseInt(val);
            if (yearVal == 0 || yearVal > 2500)
            {
                return false;
            }
        }
        
        if (month != null)
        {
            String val    = text.substring(monthInx, monthInx+month.getSize());
            int    monVal = Integer.parseInt(val);
            if (monVal < 1 || monVal > 31)
            {
                return false;
            }
            
            daysInMon[1] = isLeapYear(yearVal) ? 29 : 28;
            
            val    = text.substring(dayInx, dayInx+day.getSize());
            int    dayVal = Integer.parseInt(val);
            
            if (dayVal < 1 || dayVal > daysInMon[monVal-1])
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Validates with any formatter that has fields defined and an inputed string
     * @param formatter the formatter 
     * @param text the text to be validated.
     * @return true if it is valid
     */
    public static boolean isValid(final UIFieldFormatterIFace formatter, final String text)
    {
        if (isNotEmpty(text))
        {
            int txtLen = text.length();
            if (formatter.isLengthOK(txtLen))
            {
                if (formatter.isDate())
                {
                    return isDateValid(formatter, text);
                }
                
                int inx    = 0;
                int pos    = 0;
                for (UIFieldFormatterField field : formatter.getFields())
                {
                    if (pos < txtLen)
                    {
                        if (!field.isIncrementer())
                        {
                            //numeric, alphanumeric, alpha, separator, year
                            String val = text.substring(pos, Math.min(pos+field.getSize(), txtLen));
                            switch (field.getType())
                            {
                                case numeric:
                                    if (!StringUtils.isNumeric(val))
                                    {
                                        return false;
                                    }
                                    break;
                                    
                                case alphanumeric:
                                    if (!isAlphanumeric(val))
                                    {
                                        return false;
                                    }
                                    break;
                                    
                                case alpha:
                                    if (!isAlpha(val))
                                    {
                                        return false;
                                    }
                                    break;
                                    
                                case separator:
                                    if (!val.equals(field.getValue()))
                                    {
                                        return false;
                                    }
                                    break;
                                    
                                default:
                                    break;
                            }
                        }
                    } else
                    {
                        return false;
                    }
                    pos += field.getSize();
                    inx++;
                }
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isLengthOK(int)
     */
    public boolean isLengthOK(int lengthOfData)
    {
        return lengthOfData == getLength();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#toXML(java.lang.StringBuilder)
     */
    public void toXML(final StringBuilder sb)
    {
        sb.append("  <format");
        xmlAttr(sb, "system", isSystem);
        xmlAttr(sb, "name", name);
        
        if (dataClass != null)
        {
            xmlAttr(sb, "class", dataClass.getName());
        }
        if (isNotEmpty(fieldName) && !fieldName.equals("*"))
        {
            xmlAttr(sb, "fieldname", fieldName);
        }
        
        if (isDefault)
        {
            xmlAttr(sb, "default", isDefault);
        }
        
        if (type != FormatterType.generic)
        {
            xmlAttr(sb, "type", type.toString());
        }
        
        if (partialDateType != PartialDateEnum.None)
        {
            xmlAttr(sb, "partialdate", partialDateType.toString());
        }
        
        sb.append(">\n");
        if (autoNumber != null)
        { 
            autoNumber.toXML(sb);
        }
        
        if (type != FormatterType.numeric && type != FormatterType.date)
        {
            for (UIFieldFormatterField field : fields)
            {
                field.toXML(sb);
            }
        }
        sb.append("  </format>\n\n");
    }

	public void setSystem(boolean isSystem) {
		this.isSystem = isSystem;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public void setDataClass(Class<?> dataClass) {
		this.dataClass = dataClass;
	}

	public void setType(FormatterType type) {
		this.type = type;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        UIFieldFormatter uif = (UIFieldFormatter)super.clone();
        uif.fields = new Vector<UIFieldFormatterField>();
        for (UIFieldFormatterField fld : fields)
        {
            uif.fields.add((UIFieldFormatterField)fld.clone());
        }
        return uif;
    }
	
	
}


