/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.ui;

import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.AutoNumberIFace;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatter.PartialDateEnum;
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
public class CatalogNumberUIFieldFormatter implements UIFieldFormatterIFace
{
    protected String                name;
    protected String                title;
    protected boolean               isNumericCatalogNumber = true;
    protected boolean               isIncrementer          = true;
    protected int                   numericLength          = 9;        
    protected AutoNumberIFace       autoNumber             = null;
    protected UIFieldFormatterField field                  = null;
    protected List<UIFieldFormatterField> fields           = null;
    protected Pair<Integer, Integer> incPos;
    protected String                 pattern               = "#########";
    protected boolean                isDefault             = false;
    
    /**
     * 
     */
    public CatalogNumberUIFieldFormatter()
    {
        title = "Numeric Catalog Formatter";
        
        if (isNumericCatalogNumber)
        {
            field      = new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, numericLength, pattern, true); 
            fields     = new Vector<UIFieldFormatterField>();
            fields.add(field);
            incPos     = new Pair<Integer, Integer>(0, 9);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getTitle()
     */
    public String getTitle()
    {
        return this.title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setTitle(java.lang.String)
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#setName(java.lang.String)
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getDataClass()
     */
    public Class getDataClass()
    {
        return String.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getDateWrapper()
     */
    public DateWrapper getDateWrapper()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getFields()
     */
    public List<UIFieldFormatterField> getFields()
    {
        return fields;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getIncPosition()
     */
    public Pair<Integer, Integer> getIncPosition()
    {
        return incPos;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#getLength()
     */
    public int getLength()
    {
        return numericLength;
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
    public PartialDateEnum getPartialDateType()
    {
        return null;
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
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isOutBoundFormatter()
     */
    public boolean isOutBoundFormatter()
    {
        return isNumericCatalogNumber;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#formatOutBound(java.lang.Object)
     */
    public Object formatOutBound(final Object data)
    {
        if (isNumericCatalogNumber)
        {
            if (data != null && data instanceof String)
            {
                String dataStr = (String)data;
                if (StringUtils.isNotEmpty(dataStr))
                {
                    if (isNumericCatalogNumber && dataStr.equals(pattern))
                    {
                        return pattern;
                    }
                    String fmtStr = "%0" + numericLength + "d";
                    return String.format(fmtStr, Integer.parseInt((String)data));
                }
            }
        }
        return data;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#formatInBound(java.lang.Object)
     */
    public Object formatInBound(Object data)
    {
        if (isNumericCatalogNumber)
        {        
            if (data != null)
            {
                if (isNumericCatalogNumber && data instanceof String && StringUtils.isEmpty(data.toString()))
                {
                    return pattern;
                }
                return StringUtils.stripStart(data.toString(), "0");
            }
        }
        return data;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isInBoundFormatter()
     */
    public boolean isInBoundFormatter()
    {
        return isNumericCatalogNumber;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#toPattern()
     */
    public String toPattern()
    {
        return pattern;
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
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isUserInputNeeded()
     */
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
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#isNumericOnly()
     */
    public boolean isNumericOnly()
    {
        return isNumericCatalogNumber;
    }
}
