/* Copyright (C) 2022, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField.FieldType;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;
import static org.apache.commons.lang.StringUtils.*;


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
    private static final Logger log = Logger.getLogger(UIFieldFormatter.class);
    protected static DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
    
    public static int[]            daysInMon = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; 
    protected static final String  deftitle = UIRegistry.getResourceString("FFE_DEFAULT");     
    protected static final String  systitle = UIRegistry.getResourceString("FFE_SYSTEM");     

    protected String               fieldName;
    protected String               name;
    protected boolean              isSystem;
    protected boolean              isExternal;
    protected String               title;
    protected Class<?>             dataClass;
    protected FormatterType        type;
    protected PartialDateEnum      partialDateType;
    protected boolean              isDefault;
    protected Vector<UIFieldFormatterField> fields;
    protected boolean              isIncrementer;
    protected DateWrapper          dateWrapper = null;
    protected AutoNumberIFace      autoNumber  = null;
    
    protected int                  precision = 0;
    protected int                  scale     = 0;
    
    protected Number               minValue = null;
    protected Number               maxValue = null;
    protected Boolean              hasDash  = null;
    

    /**
     * Default constructor
     */
    public UIFieldFormatter() 
    {
        fields = new Vector<UIFieldFormatterField>();
        this.isExternal = false;
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
                            final Vector<UIFieldFormatterField> fields)
    {
        this.name            = name;
        this.isSystem        = isSystem;
        this.isExternal      = false;
        this.fieldName       = fieldName;
        this.dataClass       = dataClass;
        this.partialDateType = partialDateType;
        this.type            = type;
        this.isDefault       = isDefault;
        this.fields          = fields;
        this.isIncrementer   = isIncrementer;
        /* testing ...
        System.out.println(getRegExp());
        String rx = getRegExp();
        //Pattern p = new Pattern(rx);
        System.out.println(getSample());
        System.out.println(java.util.regex.Pattern.matches(rx, getSample()));
        if (!java.util.regex.Pattern.matches(rx, getSample()) || "".equals(rx)) {
            System.out.println("huh?");
            getRegExp();
        }

        ...testing */
    }

    private String getRegExp() {
        if (fields.size() > 0) {
            return getRegExpFromFlds();
        } else if (type.equals(FormatterType.date)) {
            String year = getYearRx(), month = getMonthRx(), day = getDayRx();
            if (partialDateType.equals(PartialDateEnum.Full) || partialDateType.equals(PartialDateEnum.Search)) {
                return makeDateRx(year, month, day);
            }
            if (partialDateType.equals(PartialDateEnum.Month)) {
                return makeDateRx(year, month, null);
            }
            if (partialDateType.equals(PartialDateEnum.Year)) {
                return makeDateRx(year, null, null);
            }
        }
        return "";
    }

    private String makeDateRx(String year, String mon, String day) {
        String sep = getDateSep();
        List<String> pieces = orderDateRx(year, mon, day);
        String result = "";
        for (String p : pieces) {
            if (!"".equals(result)) {
                result += sep;
            }
            result += p;
        }
        return result;
    }
    private String getDateSep() {
        //XXX based on pref and locale???
        return "-";
    }
    private List<String> orderDateRx(String year, String mon, String day) {
        //XXX order according to prefs and locale???
        List<String> result = new ArrayList<>();
        int len = 0;
        if (year != null) {
            result.add(year);
        }
        if (mon != null) {
            result.add(mon);
        }
        if (day != null) {
            result.add(day);
        }
        return result;
    }
    private String getDayRx() {
        return "\\p{Digit}{2}";
    }
    private String getMonthRx() {
        return "\\p{Digit}{2}";
    }
    private String getYearRx() {
        return "\\p{Digit}{" + getYearSize() + "}";
    }
    private int getYearSize() {
        return 4;
    }


    private String getRegExpFromFlds() {
        List<Pair<String, String>> fldRegExps = new ArrayList();
        int idx = 0;
        for (UIFieldFormatterField field : fields) {
            fldRegExps.add(new Pair<>(field.getRegExpGrp(++idx), field.getRegExp()));
        }
        String result = "";
        for (Pair<String, String> fre : fldRegExps) {
            result += "(?<" + fre.getFirst() + ">" + fre.getSecond() + ")";
            //result += fre.getSecond();
        }
        return result;
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
    @Override
    public boolean isSystem()
    {
        return isSystem;
    }

    /**
     * @return the isExternal
     */
    public boolean isExternal()
    {
        return isExternal;
    }

    /**
     * @param isExternal the isExternal to set
     */
    public void setExternal(boolean isExternal)
    {
        this.isExternal = isExternal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title == null ? name : title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title)
    {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getFieldName()
     */
    @Override
    public String getFieldName()
    {
        return fieldName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getFields()
     */
    @Override
    public Vector<UIFieldFormatterField> getFields()
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
   		fields = new Vector<UIFieldFormatterField>();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getYear()
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean isDate()
    {
        return type == FormatterType.date && (partialDateType == null || partialDateType != PartialDateEnum.Search);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isNumeric()
     */
    @Override
    public boolean isNumeric()
    {
        return type == FormatterType.numeric;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getMaxValue()
     */
    @Override
    public Number getMaxValue()
    {
        if (maxValue == null && dataClass == BigDecimal.class)
        {
            // This is kind lame, but it works
            if (precision > 0 && (precision-scale) > 0)
            {
                String nines = "99999999999999999999";
                String mask = nines.substring(0, precision-scale)+"."+nines.substring(0, scale);
                //System.err.println(mask);
                maxValue = new BigDecimal(Double.parseDouble(mask));
            } else
            {
                maxValue = Double.MAX_VALUE;
            }
        }
        return maxValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getMinValue()
     */
    public Number getMinValue()
    {
        if (minValue == null && dataClass == BigDecimal.class)
        {
            minValue = -Double.MAX_VALUE;
        }
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
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setPartialDateType(edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.PartialDateEnum)
     */
    @Override
    public void setPartialDateType(PartialDateEnum partialDateType)
    {
        this.partialDateType = partialDateType;
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getLength()
     */
    @Override
    public int getLength() {
        int len = 0;
        for (UIFieldFormatterField field : fields) {
            len += field.getSize();
        }
        return len;
   }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getMinLength()
     */
    @Override
    public int getMinLength() {
        int len = 0;
        for (UIFieldFormatterField field : fields) {
            len += field.getMinSize();
        }
        return len;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#resetLength()
     */
    @Override
    public void resetLength() {
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getUILength()
     */
    @Override
    public int getUILength()
    {
        return getLength();
    }

    
    /* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setLength(int)
	 */
	@Override
	public void setLength(int length) {
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getIncPosition()
     */
    @Override
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
    @Override
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
    @Override
    public String toPattern()
    {
        StringBuilder str = new StringBuilder();
        for (UIFieldFormatterField field : fields)
        {
        	if (field.getType() == UIFieldFormatterField.FieldType.regex)
        	{
        		str.append(field.formatRegexValue());
        	} else 
        	{
        		str.append(field.getValue());
        	}
            
        }
        return str.toString();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getSample()
     */
    @Override
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
    @Override
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
    @Override
    public boolean isFromUIFormatter()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#formatOutBound(java.lang.Object)
     */
    @Override
    public Object formatFromUI(final Object data)
    {
        // Just pass it on through
        return data;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getAutoNumber()
     */
    @Override
    public AutoNumberIFace getAutoNumber()
    {
        return autoNumber;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getAutoNumber(edu.ku.brc.dbsupport.AutoNumberIFace)
     */
    @Override
    public void setAutoNumber(AutoNumberIFace autoNumber)
    {
        this.autoNumber = autoNumber;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getNextNumber(java.lang.String)
     */
    @Override
    public String getNextNumber(String value)
    {
        return getNextNumber(value, false);
    }

    
    /* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getNextNumber(java.lang.String, boolean)
	 */
	@Override
	public String getNextNumber(String value, boolean incrementValue) 
	{
        if (autoNumber != null)
        {
            String number = autoNumber.getNextNumber(this, value, incrementValue);
            if (number == null && autoNumber.isInError())
            {
                UIRegistry.showError(autoNumber.getErrorMsg());
            } else
            {
                return number;
            }
        }
        return null;
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#formatInBound(java.lang.Object)
     */
    @Override
    public Object formatToUI(Object...datas)
    {
        Object data = datas[0];
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
           
        } else if (data instanceof Number)
        {
            /*
             * This block modified to fix #10238
        	 * the entire block actually seems kind of unnecessary because
        	 * the same code for displaying values in view mode could be used instead.
        	*/
        	
        	//int    size = fields.get(0).getSize();
            //String fmt;
            if (data instanceof Float || data instanceof Double)
            {
                //fmt = "%" + (size-2) + ".2f";
            	//this results in a display that matches the
            	return String.valueOf(data);
                    
            } else if (data instanceof BigDecimal)
            {
                //fmt = "%" + (size-2) + ".2f";
            	
            	/*
            	 * This was originally being cast to a double and then converted to string.
            	 * This may cause unwanted issues when displaying certain values.
            	 * (See https://stackoverflow.com/questions/16098046/how-do-i-print-a-double-value-without-scientific-notation-using-java)
            	 * To avoid these issues, the BigDecimal is first stripped of it's trailing zeros and then directly converted to string.
            	 */
            	BigDecimal strippedDecimal = ((BigDecimal) data).stripTrailingZeros();
            	
            	// If the BigDecimal is an integer and not zero
            	if (strippedDecimal.scale() <= 0 && strippedDecimal.signum() != 0)
        		{
        			// Add a decimal place so the format is #.0
            		strippedDecimal = strippedDecimal.setScale(1);
        		}
            	return strippedDecimal.toPlainString();
            } else
            {
                //fmt = "%d";
                return String.format("%d", data).trim();
            }
            
        } else if (data instanceof Calendar)
        {
            return scrDateFormat.format((Calendar)data);
            
        } else if (data instanceof Date)
        {
            return scrDateFormat.format((Date)data);
        }
        return data;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isInBoundFormatter()
     */
    @Override
    public boolean isInBoundFormatter()
    {
        return autoNumber != null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder(getTitle());
        str.append(" [");
        for (UIFieldFormatterField field : fields)
        {
            String val = field.getValue();
            if (StringUtils.isEmpty(val))
            {
                val = field.getSample();
            }
            if (field.type == FieldType.regex)
            {
            	val = field.formatRegexValue();
            }
            str.append(val);
        }
        str.append("]");

        if (isSystem || isDefault)
        {
            str.append(" (");
        
            str.append(isDefault ? deftitle : "");
            str.append(isSystem ? ((isDefault ? ", " : "") + systitle) : "");
        
            str.append(")");
        }

    	return str.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isUserInputNeeded()
     */
    @Override
    public boolean isUserInputNeeded()
    {
        for (UIFieldFormatterField f : fields)
        {
            UIFieldFormatterField.FieldType typ = f.getType();
            if (typ == UIFieldFormatterField.FieldType.alphanumeric ||
                typ == UIFieldFormatterField.FieldType.alpha ||
                typ == UIFieldFormatterField.FieldType.anychar ||
                typ == UIFieldFormatterField.FieldType.regex)
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
    @Override
    public boolean isValid(final String text)
    {
        return isValid(this, text, false);
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
            if (StringUtils.isNumericSpace(val))
            {
                yearVal = Integer.parseInt(val);
                if (yearVal == 0 || yearVal > 2500)
                {
                    return false;
                }
            } else
            {
                return false;
            }
        }
        
        
        if (month != null)
        {
            int monVal = 0;
            String val = text.substring(monthInx, monthInx+month.getSize()).trim();
            if (StringUtils.isNumericSpace(val))
            {
                monVal = Integer.parseInt(val);
                if (monVal < 1 || monVal > 12)
                {
                    return false;
                }
            } else
            {
                return false;
            }
            
            if (day != null)
            {
                daysInMon[1] = isLeapYear(yearVal) ? 29 : 28;
                
                val    = text.substring(dayInx, dayInx+day.getSize());
                if (StringUtils.isNumericSpace(val))
                {
                    int    dayVal = Integer.parseInt(val);
                    if (dayVal < 1 || dayVal > daysInMon[monVal-1])
                    {
                        return false;
                    }                    
                } else
                {
                    return false;
                }
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
    public static boolean isValid(final UIFieldFormatterIFace formatter, 
                                  final String  text,
                                  final boolean doValidateAll)
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
                
                int pos    = 0;
                for (UIFieldFormatterField field : formatter.getFields())
                {
                    if (pos < txtLen)
                    {
                        if (!field.isIncrementer() || doValidateAll)
                        {
                            //numeric, alphanumeric, alpha, separator, year, regex
                            String val = text.substring(pos, Math.min(pos+field.getSize(), txtLen));
                            switch (field.getType())
                            {
                                case numeric: {
                                    String str1 = StringUtils.remove(val, '.');
                                    String str2 = StringUtils.remove(str1, '-');
                                    
                                    if (StringUtils.isNumeric(str2))
                                    {
                                        Class<?> cls = formatter.getDataClass();
                                        if (cls == java.lang.Integer.class || cls == java.lang.Long.class || cls == java.lang.Short.class || cls == java.lang.Byte.class)
                                        {
                                            return str1.length() == val.length();
                                        } 
                                        return true;
                                    }
                                    return false;
                                }
                                
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
                                    
                                case year:
                                    if (!StringUtils.isNumeric(val))
                                    {
                                        return false;
                                    }
                                    int year = Integer.parseInt(val);
                                    return year > 0 && year < 2100;
                                    
                                case constant:
                                case separator:
                                    if (!val.equals(field.getValue()))
                                    {
                                        return false;
                                    }
                                    break;
                                case regex:
                                    return Pattern.matches(field.getRegex(), val);
                                default:
                                    break;
                            }
                        }
                    } else
                    {
                        return false;
                    }
                    pos += field.getSize();
                }
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isLengthOK(int)
     */
    @Override
    public boolean isLengthOK(int lengthOfData)
    {
        if (type == FormatterType.numeric)
        {
            return lengthOfData < getLength();
        }

        return getMinLength() - lengthOfData <= 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#toXML(java.lang.StringBuilder)
     */
    @Override
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
        
        if (type != FormatterType.generic && type != null)
        {
            xmlAttr(sb, "type", type.toString());
        }
        
        if (partialDateType != null && partialDateType != PartialDateEnum.None)
        {
            xmlAttr(sb, "partialdate", partialDateType.toString());
        }
        
        sb.append(">\n");
        if (autoNumber != null)
        { 
            autoNumber.toXML(sb);
        }
        
        if (type != FormatterType.date)
        {
            for (UIFieldFormatterField field : fields)
            {
                field.toXML(sb);
            }
        }
        if (isExternal)
        {
            sb.append("    <external>");
            sb.append("    <external>");
            sb.append("    </extneral>\n");
        }
        sb.append("  </format>\n\n");
    }

	public void setSystem(boolean isSystem) 
	{
		this.isSystem = isSystem;
	}

	public void setFieldName(String fieldName) 
	{
		this.fieldName = fieldName;
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setDataClass(java.lang.Class)
     */
    @Override
	public void setDataClass(Class<?> dataClass) 
	{
		this.dataClass = dataClass;
	}

	/**
	 * @param type
	 */
	public void setType(FormatterType type) 
	{
		this.type = type;
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#hasDash()
     */
    @Override
    public boolean hasDash()
    {
        if (hasDash == null)
        {
            hasDash = false;
            for (UIFieldFormatterField fld : getFields())
            {
                if ((fld.isSeparator() || fld.isConstant()) && fld.getValue().equals("-"))
                {
                    hasDash = true;
                    break;
                }
            }
        }
        return hasDash;
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


