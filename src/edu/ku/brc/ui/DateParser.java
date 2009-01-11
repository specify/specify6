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
/**
 * 
 */
package edu.ku.brc.ui;

import java.util.Calendar;
import java.util.Date;


/**
 * THis class is used to parse date strings. It can parse and numerical date format string
 * and works with the common set of separators. It uses the default format to determin whether
 * the Month comes before the Day.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Dec 5, 2007
 *
 */
public class DateParser
{
    public enum DateErrorType  {None, BadYear, BadMonth, BadDay, UnknownFormat, UnknownError}
    
    protected static char[] standardSeps = {'/', '-', '.'};
    protected static char   defaultSep   = '/';
    
    protected int[]       daysInMonth   = {0,31,28,31,30,31,30,31,31,30,31,30,31};
    
    public enum DateFormatType {Unknown, YYYY_AA_BB, YYYY_A_BB, YYYY_AA_B, YYYY_A_B, 
                                         AA_BB_YYYY, A_BB_YYYY, AA_B_YYYY, A_B_YYYY}
    
    protected String        monthFormat          = "MM";
    protected String        dayFormat            = "dd";
    
    protected String        defaultDateFormatStr = null;
    protected boolean       isMonthFirst         = false;
    protected boolean       isYearFirst          = false;
    protected DateErrorType dateError            = DateErrorType.None;
    
    /**
     * @param defaultDateFormat
     */
    public DateParser(final String defaultDateFormat)
    {
        setDefaultDateFormatStr(defaultDateFormat);
    }
    
    
    /**
     * @param defaultDateFormatStr the defaultDateFormatStr to set
     */
    public void setDefaultDateFormatStr(String defaultDateFormatStr)
    {
        this.defaultDateFormatStr = defaultDateFormatStr;
        initialize();
    }


    /**
     * 
     */
    private void initialize()
    {
        isYearFirst = isYearFirstInDateStr(defaultDateFormatStr);
        int inx = isYearFirst ? 5 : 0;
        isMonthFirst = defaultDateFormatStr.charAt(inx) == 'M';
        inx = 1;
        while (Character.isLetter(defaultDateFormatStr.charAt(inx)))
        {
            inx++;
        }
        defaultSep = defaultDateFormatStr.charAt(inx);
    }
    
    /**
     * @param dateStr
     * @return
     */
    protected static boolean isYearFirstInDateStr(final String dateStr)
    {
        int x = 0;
        for (char ch : dateStr.substring(0, 4).toCharArray())
        {
            if (!Character.isLetter(ch) && !Character.isDigit(ch))
            {
                return x > 3;
            }
            x++;
        }
        return true;
    }
    
    /**
     * Discovers format not knowing for either a format or a date string.
     * @param dateStr the string to test
     * @return the type of format
     */
    public static DateFormatType getDateFormatType(final String dateStr)
    {
        DateFormatType type = DateFormatType.Unknown;
        int            len  = dateStr.length();
        if (len > 7 && len < 11)
        {
            boolean        isYearFirst = isYearFirstInDateStr(dateStr);
            char           sep         = getSep(dateStr);
            
            if (len == 10)
            {
                type = isYearFirst ? DateFormatType.YYYY_AA_BB : DateFormatType.AA_BB_YYYY;
                
            } else if (len == 9)
            {
                int inx = isYearFirst ? 6 : 1;
                    
                char sepChar = dateStr.charAt(inx);
                if (sepChar == sep)
                {
                    type = isYearFirst ? DateFormatType.YYYY_A_BB : DateFormatType.A_BB_YYYY;
                    
                } else
                {
                    type = isYearFirst ? DateFormatType.YYYY_AA_B : DateFormatType.AA_B_YYYY;
                }
                
            } else if (len == 8)
            {
                return isYearFirst ? DateFormatType.YYYY_A_B : DateFormatType.A_B_YYYY;
            }
        }
        return type;
    }
    
    /**
     * Determines the separator from a set of common separators
     * @param dateStr the date string
     * @return the sep char
     */
    protected static char getSep(final String dateStr)
    {
        for (char ch : dateStr.toCharArray())
        {
            if (!Character.isLetter(ch) && !Character.isDigit(ch))
            {
                for (char st : standardSeps)
                {
                    if (ch == st)
                    {
                        return ch;
                    }
                }
            }
        }
        return defaultSep;
    }
    
    /**
     * @param year
     * @return
     */
    protected boolean isLeapYear(final int year)
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
     * @param type
     * @param dateStr
     * @param isZeroMnOK
     * @param isZeroDyOK
     * @return
     */
    protected Date parseForDate(final DateFormatType type, 
                                final String         dateStr, 
                                final boolean        isZeroMnOK, 
                                final boolean        isZeroDyOK)
    {
        String yy = "";
        String aa = "";
        String bb = "";
        
        switch (type)
        {
            case YYYY_AA_BB:
                yy = dateStr.substring(0, 4);
                aa = dateStr.substring(5, 7);
                bb = dateStr.substring(8, 10);
                break;
            
            case YYYY_A_BB:
                yy = dateStr.substring(0, 4);
                aa = dateStr.substring(5, 6);
                bb = dateStr.substring(7, 9);
                break;
            
            case YYYY_AA_B:
                yy = dateStr.substring(0, 4);
                aa = dateStr.substring(5, 7);
                bb = dateStr.substring(8, 9);
                break;
            
            case YYYY_A_B:
                yy = dateStr.substring(0, 4);
                aa = dateStr.substring(5, 6);
                bb = dateStr.substring(7, 8);
                break;
            
            case AA_BB_YYYY:
                aa = dateStr.substring(0, 2);
                bb = dateStr.substring(3, 5);
                yy = dateStr.substring(6, 10);
                break;
            
            case A_BB_YYYY:
                aa = dateStr.substring(0, 1);
                bb = dateStr.substring(2, 4);
                yy = dateStr.substring(5, 9);
                break;
            
            case AA_B_YYYY:
                aa = dateStr.substring(0, 2);
                bb = dateStr.substring(3, 4);
                yy = dateStr.substring(5, 9);
                break;
            
            case A_B_YYYY:
                aa = dateStr.substring(0, 1);
                bb = dateStr.substring(2, 3);
                yy = dateStr.substring(4, 8);
                break;
                
            case Unknown :
                dateError = DateErrorType.UnknownFormat;
                return null;
        }
        
        try
        {
            int yr = Integer.parseInt(yy);
            int mn = isMonthFirst ? Integer.parseInt(aa) : Integer.parseInt(bb);
            int dy = isMonthFirst ? Integer.parseInt(bb) : Integer.parseInt(aa);
            
            if (yr < 0 || yr > 2999)
            {
                dateError = DateErrorType.BadYear;
                return null;
            }
            
            if (mn == 0 && isZeroMnOK)
            {
                mn = 1;
                
            } else if (mn < 1 || mn > 12)
            {
                dateError = DateErrorType.BadMonth;
                return null;
            }
            
            if (dy == 0 && isZeroDyOK)
            {
                dy = 1;
            }
            
            int dysInMn = daysInMonth[mn];
            if (mn == 2 && isLeapYear(yr))
            {
                dysInMn++;
            }
            
            if (dy < 1 || dy > dysInMn)
            {
                dateError = DateErrorType.BadDay;
                return null;
            }
            
            Calendar cal = Calendar. getInstance();
            cal.set(yr, mn-1, dy);
            
            return cal.getTime();
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DateParser.class, ex);
            
        }
        dateError = DateErrorType.UnknownError;
        return null;
    }
    
    
    /**
     * Parses any date string and convert it to a date.
     * @param dateStr the string
     * @return the date or null
     */
    public Date parseDate(final String dateStr)
    {
        return parseDate(dateStr, false, false);
    }
    
    /**
     * Parses any date string and convert it to a date. The zero month and/or day is converted to a 1.
     * @param dateStr the string
     * @return the date or null
     */
    public Date parseDate(final String dateStr, 
                          final boolean isZeroMnOK, 
                          final boolean isZeroDyOK)
    {
        dateError = DateErrorType.None;
        
        DateFormatType type = getDateFormatType(dateStr);
        if (type != DateFormatType.Unknown)
        {
            return parseForDate(type, dateStr, isZeroMnOK, isZeroDyOK);
            
        }
        dateError = DateErrorType.UnknownFormat;
        return null;
    }

    /**
     * @return the dateError
     */
    public DateErrorType getDateError()
    {
        return dateError;
    }
    
    
}
