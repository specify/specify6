/*
     * Copyright (C) 2009  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.ui.DateWrapper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 29, 2009
 *
 */
public class PartialDateFormatter extends UIFieldFormatter
{
    protected static DateWrapper scrDateFormat     = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");    
    protected static DateWrapper scrDateFormatMon  = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformatmon");    
    protected static DateWrapper scrDateFormatYear = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformatyear");    

    /**
     * 
     */
    public PartialDateFormatter()
    {
        super();
    }

    /**
     * @param name
     * @param isSystem
     * @param fieldName
     * @param type
     * @param partialDateType
     * @param dataClass
     * @param isDefault
     * @param isIncrementer
     * @param fields
     */
    public PartialDateFormatter(final String name, 
                                final boolean isSystem, 
                                final String fieldName,
                                final FormatterType type, 
                                final PartialDateEnum partialDateType, 
                                final Class<?> dataClass,
                                final boolean isDefault, 
                                final boolean isIncrementer, 
                                final List<UIFieldFormatterField> fields)
    {
        super(name, isSystem, fieldName, type, partialDateType, dataClass, isDefault, isIncrementer, fields);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatter#formatToUI(java.lang.Object[])
     */
    @Override
    public Object formatToUI(Object... datas)
    {
        Date date = null;
        if (datas[0] instanceof Date)
        {
            date = (Date)datas[0];
            
        } else if (datas[0] instanceof Calendar)
        {
            date = ((Calendar)datas[0]).getTime();
        }
        
        Integer dType = datas[1] instanceof Number ? ((Number)datas[1]).intValue() : 0;
        
        if (datas.length == 2 && date != null)
        {
            String dateStr = "";
            if (dType == 1)
            {
                dateStr = scrDateFormat.format(date);
                
            } else if (dType == 2)
            {
                dateStr = scrDateFormatMon.format(date);
                
            } else if (dType == 3)
            {
                scrDateFormatYear = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformatyear");   
                dateStr = scrDateFormatYear.format(date);
            }
            return dateStr;
        }
        
        return super.formatToUI(datas);
    }

}
