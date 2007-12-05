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
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Dec 5, 2007
 *
 */
public class DateParser
{
    protected static char[] standardSeps = {'/', '-', '.'};
    protected static char   defaultSep   = '/';
    
    protected int[]       daysInMonth   = {0,31,28,31,30,31,30,31,31,30,31,30,31};
    
    public enum DateFormatType {Unknown, YYYY_AA_BB, YYYY_A_BB, YYYY_AA_B, YYYY_A_B, 
                                         AA_BB_YYYY, A_BB_YYYY, AA_B_YYYY, A_B_YYYY}
    
    protected String monthFormat  = "MM";
    protected String dayFormat    = "dd";
    
    //protected Hashtable<Character, Hashtable<DateFormatType, SimpleDateFormat>> dateFormatters = new Hashtable<Character, Hashtable<DateFormatType, SimpleDateFormat>>();
    protected String  defaultDateFormatStr = null;
    protected boolean isMonthFirst         = false;
    protected boolean isYearFirst          = false;
    
    /**
     * @param defaultDateFormat
     */
    public DateParser(final String defaultDateFormat)
    {
        this.defaultDateFormatStr = defaultDateFormat;
        
        buildFormatterHash();
    }
    
    /**
     * 
     */
    public void buildFormatterHash()
    {
        System.out.println(defaultDateFormatStr);
        
        isYearFirst = isYearFirstInDateStr(defaultDateFormatStr);
        int inx = isYearFirst ? 5 : 0;
        isMonthFirst = defaultDateFormatStr.charAt(inx) == 'M';
        inx = 1;
        while (Character.isLetter(defaultDateFormatStr.charAt(inx)))
        {
            inx++;
        }
        defaultSep = defaultDateFormatStr.charAt(inx);
        
        /*
        char[] seps = {defaultSep, ' ', ' '};
        inx = 1;
        for (int i=0;i<standardSeps.length;i++)
        {
            if (standardSeps[i] != defaultSep)
            {
                seps[inx++] = standardSeps[i];
            }
        }
        
        dateFormatters.clear();
        DateFormatType[] yearFirstTypes  = {DateFormatType.YYYY_AA_BB, DateFormatType.YYYY_A_BB, DateFormatType.YYYY_AA_B, DateFormatType.YYYY_A_B};
        DateFormatType[] notYrFirstTypes = {DateFormatType.AA_BB_YYYY, DateFormatType.A_BB_YYYY, DateFormatType.AA_B_YYYY, DateFormatType.A_B_YYYY};
        for (char sep : seps)
        {
            DateFormatType[] types = isYearFirst ? yearFirstTypes : notYrFirstTypes;
            
            Hashtable<DateFormatType, SimpleDateFormat> hash = new Hashtable<DateFormatType, SimpleDateFormat>();
            
            hash.put(types[0], new SimpleDateFormat(buildFormatString(isYearFirst, 2, isMonthFirst, 2, sep)));
            hash.put(types[1], new SimpleDateFormat(buildFormatString(isYearFirst, 1, isMonthFirst, 2, sep)));
            hash.put(types[2], new SimpleDateFormat(buildFormatString(isYearFirst, 2, isMonthFirst, 1, sep)));
            hash.put(types[3], new SimpleDateFormat(buildFormatString(isYearFirst, 1, isMonthFirst, 1, sep)));
            
            types = !isYearFirst ? yearFirstTypes : notYrFirstTypes;
            hash.put(types[0], new SimpleDateFormat(buildFormatString(!isYearFirst, 2, isMonthFirst, 2, sep)));
            hash.put(types[1], new SimpleDateFormat(buildFormatString(!isYearFirst, 1, isMonthFirst, 2, sep)));
            hash.put(types[2], new SimpleDateFormat(buildFormatString(!isYearFirst, 2, isMonthFirst, 1, sep)));
            hash.put(types[3], new SimpleDateFormat(buildFormatString(!isYearFirst, 1, isMonthFirst, 1, sep)));
            
            dateFormatters.put(sep, hash);
        }
        */
        
        // debug 
        /*for (DateFormatType typ : dateFormatters.keySet())
        {
            System.out.println(typ+" "+dateFormatters.get(typ).toPattern());
        }*/
    }
    
    /**
     * @param isYrFirst
     * @param isMnFirst
     * @return
     */
    protected String buildFormatString(final boolean isYrFirst, 
                                       final int     mnSize,
                                       final boolean isMnFirst,
                                       final int     dySize,
                                       final char    sep)
    {
        if (isYrFirst)
        {
            return "yyyy" + sep + (isMnFirst ? monthFormat.substring(0, mnSize) : dayFormat.substring(0, dySize)) + sep + (isMnFirst ? dayFormat.substring(0, dySize) : monthFormat.substring(0, mnSize));
        }
        return (isMnFirst ? monthFormat.substring(0, mnSize) : dayFormat.substring(0, dySize)) + sep + (isMnFirst ? dayFormat.substring(0, dySize) : monthFormat.substring(0, mnSize)) + sep + "yyyy";
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
     * Discovers format not knowning DD or MM.
     * @param dateStr the string to test
     * @return the type of format
     */
    public static DateFormatType getDateFormatType(final String dateStr)
    {
        DateFormatType type        = DateFormatType.Unknown;
        int            len         = dateStr.length();
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
        return type;
    }
    
    /**
     * @param dateStr
     * @return
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
    protected Date isDateValid(final DateFormatType type, 
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
                return null;
        }
        
        try
        {
            int yr = Integer.parseInt(yy);
            int mn = isMonthFirst ? Integer.parseInt(aa) : Integer.parseInt(bb);
            int dy = isMonthFirst ? Integer.parseInt(bb) : Integer.parseInt(aa);
            
            if (yr < 0 || yr > 2999)
            {
                return null;
            }
            
            if (mn == 0 && isZeroMnOK)
            {
                mn = 1;
                
            } else if (mn < 1 || mn > 12)
            {
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
                return null;
            }
            
            Calendar cal = Calendar. getInstance();
            cal.set(yr, mn-1, dy);
            
            return cal.getTime();
            
        } catch (Exception ex)
        {
            
        }
        return null;
    }
    
    
    /**
     * @param defaultDateFormat
     * @param dateStr
     * @return
     */
    public Date parseDate(final String defaultDateFormat, 
                          final String dateStr)
    {
        return parseDate(defaultDateFormat, dateStr, false, false);
    }
    
    /**
     * @param defaultDateFormat
     * @param dateStr
     * @return
     */
    public Date parseDate(final String defaultDateFormat, 
                          final String dateStr, 
                          final boolean isZeroMnOK, 
                          final boolean isZeroDyOK)
    {
        if (defaultDateFormatStr == null || !defaultDateFormatStr.equals(defaultDateFormat))
        {
            buildFormatterHash();
        }
        
        DateFormatType type = getDateFormatType(dateStr);
        if (type != DateFormatType.Unknown)
        {
            return isDateValid(type, dateStr, isZeroMnOK, isZeroDyOK);
            
            /*try
            {
                Hashtable<DateFormatType, SimpleDateFormat> hash = dateFormatters.get(getSep(dateStr));
                SimpleDateFormat formatter = hash.get(type);
                if (formatter != null)
                {
                    return formatter.parse(dateStr);
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace(); // for debugging only
            }*/
        }
        
        return null;
    }
    
    
}
