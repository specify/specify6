/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

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
    //protected static DateWrapper scrDateFormat     = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");    
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
                                final Vector<UIFieldFormatterField> fields)
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
            
        } else if (datas[0] instanceof GregorianCalendar)
        {
            date = ((GregorianCalendar)datas[0]).getTime();
            
        } else if (datas[0] instanceof Calendar)
        {
            date = ((Calendar)datas[0]).getTime();
        }
        
        Integer dType = datas[1] instanceof Number ? ((Number)datas[1]).intValue() : 1;
        
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
