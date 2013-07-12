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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import java.text.DecimalFormatSymbols;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * This class is used for formatting numeric CatalogNumbers.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jun 29, 2007
 *
 */
public class BaseUIFieldFormatter implements UIFieldFormatterIFace, Cloneable
{
    protected static final String  deftitle = UIRegistry.getResourceString("FFE_DEFAULT");
    
    protected String                name;
    protected String                title;
    protected boolean               isNumericCatalogNumber;
    protected boolean               isIncrementer;
    protected int                   length;        
    protected int                   uiLength;        
    protected AutoNumberIFace       autoNumber;
    protected UIFieldFormatterField field;
    protected Vector<UIFieldFormatterField> fields         = null;
    protected Pair<Integer, Integer> incPos;
    protected String                 pattern;
    protected boolean                isDefault             = false;
    protected PartialDateEnum        partialDateType       = PartialDateEnum.None;
    protected Boolean                hasDash               = null;

    /**
     * @param type the name of the formatter, must be unique and not localized
     * @param altName the localized title
     * @param isIncrementer
     * @param length
     * @param isNumericCatalogNumber
     * @param autoNumber
     */
    public BaseUIFieldFormatter()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getTitle()
     */
    @Override
    public String getTitle()
    {
        return this.title;
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
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setName(java.lang.String)
     */
    @Override
    public void setName(final String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getFieldName()
     */
    @Override
    public String getFieldName()
    {
        return "catalogNumber"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setFieldName(java.lang.String)
     */
    @Override
    public void setFieldName(String fieldName)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getDataClass()
     */
    @Override
    public Class<?> getDataClass()
    {
        return String.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setDataClass(java.lang.Class)
     */
    @Override
    public void setDataClass(Class<?> dataClass)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setType(edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.FormatterType)
     */
    @Override
    public void setType(FormatterType type)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getSample()
     */
    @Override
    public String getSample()
    {
    	return "123"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#byYearApplies()
     */
    @Override
    public boolean byYearApplies()
    {
    	// no auto-year field in here
    	return false;
    }
    
    public boolean getByYear()
    {
    	return false;
    }
    
    public void setByYear(boolean byYear)
    {
    	// not applicable
    	return;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getDateWrapper()
     */
    @Override
    public DateWrapper getDateWrapper()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getFields()
     */
    @Override
    public Vector<UIFieldFormatterField> getFields()
    {
        return fields;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getYear()
     */
    public UIFieldFormatterField getYear()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getIncPosition()
     */
    @Override
    public Pair<Integer, Integer> getIncPosition()
    {
        return incPos;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getYearPosition()
     */
    @Override
    public Pair<Integer, Integer> getYearPosition()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getLength()
     */
    public int getLength()
    {
        return length;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#resetLength()
     */
    @Override
    public void resetLength()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getUILength()
     */
    @Override
    public int getUILength()
    {
        return uiLength;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isLengthOK(int)
     */
    @Override
    public boolean isLengthOK(int lengthOfData)
    {
        return lengthOfData == getLength();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getPartialDateType()
     */
    @Override
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
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isDate()
     */
    public boolean isDate()
    {
        return false;
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
    @Override
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
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setIncrementer(boolean)
     */
    @Override
    public void setIncrementer(boolean isIncrementer)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isOutBoundFormatter()
     */
    @Override
    public boolean isFromUIFormatter()
    {
        return isNumericCatalogNumber;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#formatOutBound(java.lang.Object)
     */
    public Object formatFromUI(final Object data)
    {
        if (isNumericCatalogNumber)
        {
            if (data != null && data instanceof String && UIHelper.isANumber((String)data))
            {
                String dataStr = (String)data;
                if (StringUtils.isNotEmpty(dataStr))
                {
                    if (isNumericCatalogNumber && dataStr.equals(pattern))
                    {
                        return pattern;
                    }
                    
                    //check for floating point cat num
                	char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
                	int sepIdx = dataStr.indexOf(separator);
                    if (sepIdx >= 0)
                    {
                        int precision = dataStr.length() - sepIdx - 1;
                    	String fmtStr = "%0" + length + "." + precision + "f"; //$NON-NLS-1$ //$NON-NLS-2$
                    	return String.format(fmtStr, Float.parseFloat((String)data));
                    }
                    
                    String fmtStr = "%0" + length + "d"; //$NON-NLS-1$ //$NON-NLS-2$
                    return String.format(fmtStr, Integer.parseInt((String)data));
                    
                }
            }
        }
        return data;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#formatInBound(java.lang.Object)
     */
    @Override
    public Object formatToUI(Object...datas)
    {
        Object data = datas[0];
        if (isNumericCatalogNumber)
        {        
            if (data != null)
            {
                if (isNumericCatalogNumber && data instanceof String && StringUtils.isEmpty(data.toString()))
                {
                    return pattern;
                }
                return StringUtils.stripStart(data.toString(), "0"); //$NON-NLS-1$
            }
        }
        return data;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isInBoundFormatter()
     */
    @Override
    public boolean isInBoundFormatter()
    {
        return isNumericCatalogNumber;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#toPattern()
     */
    @Override
    public String toPattern()
    {
        return pattern;
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
    public String getNextNumber(final String value)
    {
    	return getNextNumber(value, false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getNextNumber(java.lang.String, boolean)
     */
    public String getNextNumber(final String value, final boolean incrementValue)
    {
        if (autoNumber != null)
        {
            String val = value;
            if (StringUtils.isEmpty(val))
            {
                for (int i=0;i<getLength();i++)
                {
                    val += ' ';
                }
            }
            
            String number = autoNumber.getNextNumber(this, val, incrementValue);
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
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isUserInputNeeded()
     */
    @Override
    public boolean isUserInputNeeded()
    {
        if (!isNumericCatalogNumber)
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
        }
        return false;
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
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isNumeric()
     */
    @Override
    public boolean isNumeric()
    {
        return isNumericCatalogNumber;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getMaxValue()
     */
    @Override
    public Number getMaxValue()
    {
        return isNumericCatalogNumber ? Integer.MAX_VALUE : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getMinValue()
     */
    @Override
    public Number getMinValue()
    {
        return isNumericCatalogNumber ? 0 : null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder(getTitle());
        str.append(" [");
        for (UIFieldFormatterField fld : fields)
        {
            str.append(fld.getValue());
        }
        str.append("]");

        str.append(isDefault ? (' ' + deftitle) : "");
        
        return str.toString();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isSystem()
     */
    @Override
    public boolean isSystem()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isValid(java.lang.String)
     */
    @Override
    public boolean isValid(String value)
    {
        if (isNumericCatalogNumber)
        {
            return StringUtils.isNotEmpty(value) && 
                   value.length() == length && 
                   StringUtils.isNumeric(value);
        }
        return UIFieldFormatter.isValid(this, value, false);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#toXML(java.lang.StringBuilder)
     */
    @Override
    public void toXML(final StringBuilder sb)
    {
        sb.append("  <format"); //$NON-NLS-1$
        xmlAttr(sb, "system", true); //$NON-NLS-1$
        xmlAttr(sb, "name", name); //$NON-NLS-1$
        xmlAttr(sb, "class", CollectionObject.class.getName()); //$NON-NLS-1$
        xmlAttr(sb, "fieldname", getFieldName()); //$NON-NLS-1$
        xmlAttr(sb, "default", isDefault); //$NON-NLS-1$
        sb.append(">\n"); //$NON-NLS-1$
        if (autoNumber != null)
        {
            autoNumber.toXML(sb);
        }
        sb.append("    <external>" + getClass().getName() + "</external>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append("  </format>\n\n"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        BaseUIFieldFormatter uif = (BaseUIFieldFormatter)super.clone();
        uif.fields = new Vector<UIFieldFormatterField>();
        for (UIFieldFormatterField fld : fields)
        {
            uif.fields.add((UIFieldFormatterField)fld.clone());
        }
        return uif;
    }

}
