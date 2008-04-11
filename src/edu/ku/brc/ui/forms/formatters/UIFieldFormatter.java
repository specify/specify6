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

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import java.util.ArrayList;
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
    public enum FormatterType   {generic, date, numeric} // all lower case to follow convention in uiformatters.xml

    //private static final Logger log = Logger.getLogger(UIFieldFormatter.class);

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

    /**
     * Default constructor
     */
    public UIFieldFormatter() {}
    
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
    
    /*
     * 
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

    /*
     * 
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
    
    /*
     * 
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
    	return toPattern();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isUserInputNeeded()
     */
    public boolean isUserInputNeeded()
    {
        for (UIFieldFormatterField f : fields)
        {
            UIFieldFormatterField.FieldType typ = f.getType();
            if (typ != UIFieldFormatterField.FieldType.alphanumeric ||
                typ != UIFieldFormatterField.FieldType.alpha)
            {
                return true;
                
            } else if (typ != UIFieldFormatterField.FieldType.numeric && !f.isIncrementer())
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
        if (StringUtils.isNotEmpty(fieldName) && !fieldName.equals("*"))
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
}


