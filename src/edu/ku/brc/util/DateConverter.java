/* Copyright (C) 2023, Specify Collections Consortium
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
package edu.ku.brc.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Mostly copied from Josh's GeoRefConverter class.
 * 
 * 
 */
public class DateConverter
{
    protected static final Logger log            = Logger.getLogger(DateConverter.class);
    protected boolean             preferMonthDay;
    
    public static enum DateFormats {
        MON_DAY_LYEAR(
                "[0123456789]?[0123456789]([ /\\.-])[0123456789]?[0123456789]\\1[0-9][0-9][0-9][0-9]")
        {
            @Override
            public Calendar convertToCalendar(String dateStr) throws ParseException
            {
                DateFormat df;
                if (dateStr.contains("/"))
                {
                    df = new SimpleDateFormat("MM/dd/yyyy");
                }
                else if (dateStr.contains("."))
                {
                    df = new SimpleDateFormat("MM.dd.yyyy");
                }
                else if (dateStr.contains("-"))
                {
                    df = new SimpleDateFormat("MM-dd-yyyy");
                }
                else if (dateStr.contains(" "))
                {
                    df = new SimpleDateFormat("MM dd yyyy");
                }
                else
                {
                    return null;
                }
                Calendar result = new GregorianCalendar();
                result.setLenient(false);
                df.setLenient(false);
                result.setTime(df.parse(dateStr));
                return result;
            }
            
            @Override
            public UIFieldFormatterIFace.PartialDateEnum getPrecision(String dateStr) throws ParseException
            {
                String month = dateStr.substring(0, 2);
                String day = dateStr.substring(3, 5);
                boolean noMonth = month.equals("00") || month.equals("  ");
                boolean noDay = day.equals("00") || day.equals("  ");
                if (noMonth && !noDay)
                {
                    throw new ParseException("invalid date", 0);
                }
                if (noMonth && noDay)
                {
                    return UIFieldFormatterIFace.PartialDateEnum.Year;
                }
                if (noDay)
                {
                    return UIFieldFormatterIFace.PartialDateEnum.Month;
                }
                return UIFieldFormatterIFace.PartialDateEnum.Full;
            }
            
            @Override
            public String adjustForPrecision(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
            {
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                {
                    return "01" + dateStr.substring(2,3) + "01" + dateStr.substring(5);
                }
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                {
                    return dateStr.substring(0,3) + "01" + dateStr.substring(5);
                }
                return dateStr;
            }

            @Override
            public String adjustForPrecisionOut(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
            {
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                {
                    return "00" + dateStr.substring(2,3) + "00" + dateStr.substring(5);
                }
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                {
                    return dateStr.substring(0,3) + "00" + dateStr.substring(5);
                }
                return dateStr;
            }
            
            @Override
            public String getFormatString(String separator)
            {
                return "MM" + separator + "dd" + separator + "yyyy";
            }

        },
        MON_DAY_SYEAR("[0123456789]?[0123456789]([ /\\.-])[0123456789]?[0123456789]\\1[0-9][0-9]")
        {
            @Override
            public Calendar convertToCalendar(String dateStr) throws ParseException
            {
                DateFormat df;
                if (dateStr.contains("/"))
                {
                    df = new SimpleDateFormat("MM/dd/yy");
                }
                else if (dateStr.contains("."))
                {
                    df = new SimpleDateFormat("MM.dd.yy");
                }
                else if (dateStr.contains("-"))
                {
                    df = new SimpleDateFormat("MM-dd-yy");
                }
                else if (dateStr.contains(" "))
                {
                    df = new SimpleDateFormat("MM dd yy");
                }
                else
                {
                    return null;
                }
                Calendar result = new GregorianCalendar();
                result.setLenient(false);
                df.setLenient(false);
                result.setTime(df.parse(dateStr));
                return result;
            }
            
            @Override
            public UIFieldFormatterIFace.PartialDateEnum getPrecision(String dateStr) throws ParseException
            {
                String month = dateStr.substring(0, 2);
                String day = dateStr.substring(3, 5);
                boolean noMonth = month.equals("00") || month.equals("  ");
                boolean noDay = day.equals("00") || day.equals("  ");
                if (noMonth && !noDay)
                {
                    throw new ParseException("invalid date", 0);
                }
                if (noMonth && noDay)
                {
                    return UIFieldFormatterIFace.PartialDateEnum.Year;
                }
                if (noDay)
                {
                    return UIFieldFormatterIFace.PartialDateEnum.Month;
                }
                return UIFieldFormatterIFace.PartialDateEnum.Full;
            }

            @Override
            public String adjustForPrecision(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
            {
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                {
                    return "01" + dateStr.substring(2,3) + "01" + dateStr.substring(5);
                }
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                {
                    return dateStr.substring(0,3) + "01" + dateStr.substring(5);
                }
                return dateStr;
            }
            
            @Override
            public String adjustForPrecisionOut(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
            {
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                {
                    return "00" + dateStr.substring(2,3) + "00" + dateStr.substring(5);
                }
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                {
                    return dateStr.substring(0,3) + "00" + dateStr.substring(5);
                }
                return dateStr;
            }
            
            @Override
            public String getFormatString(String separator)
            {
                return "MM" + separator + "dd" + separator + "yy";
            }

        },
        DAY_MON_LYEAR(
                "[0123456789]?[0123456789]([ /\\.-])[0123456789]?[0123456789]\\1[0-9][0-9][0-9][0-9]")
        {
            @Override
            public Calendar convertToCalendar(String dateStr) throws ParseException
            {
                DateFormat df;
                if (dateStr.contains("/"))
                {
                    df = new SimpleDateFormat("dd/MM/yyyy");
                }
                else if (dateStr.contains("."))
                {
                    df = new SimpleDateFormat("dd.MM.yyyy");
                }
                else if (dateStr.contains("-"))
                {
                    df = new SimpleDateFormat("dd-MM-yyyy");
                }
                else if (dateStr.contains(" "))
                {
                    df = new SimpleDateFormat("dd MM yyyy");
                }
                else
                {
                    return null;
                }
                Calendar result = new GregorianCalendar();
                result.setLenient(false);
                df.setLenient(false);
                result.setTime(df.parse(dateStr));
                return result;
            }
            
            @Override
            public UIFieldFormatterIFace.PartialDateEnum getPrecision(String dateStr) throws ParseException
            {
                String month = dateStr.substring(3, 5);
                String day = dateStr.substring(0, 2);
                boolean noMonth = month.equals("00") || month.equals("  ");
                boolean noDay = day.equals("00") || day.equals("  ");
                if (noMonth && !noDay)
                {
                    throw new ParseException("invalid date", 0);
                }
                if (noMonth && noDay)
                {
                    return UIFieldFormatterIFace.PartialDateEnum.Year;
                }
                if (noDay)
                {
                    return UIFieldFormatterIFace.PartialDateEnum.Month;
                }
                return UIFieldFormatterIFace.PartialDateEnum.Full;
            }
            
            @Override
            public String adjustForPrecision(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
            {
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                {
                    return "01" + dateStr.substring(2,3) + "01" + dateStr.substring(5);
                }
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                {
                    return "01" + dateStr.substring(2);
                }
                return dateStr;
            }
            
            @Override
            public String adjustForPrecisionOut(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
            {
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                {
                    return "00" + dateStr.substring(2,3) + "00" + dateStr.substring(5);
                }
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                {
                    return "00" + dateStr.substring(2);
                }
                return dateStr;
            }
            
            @Override
            public String getFormatString(String separator)
            {
                return "dd" + separator + "MM" + separator + "yyyy";
            }

        },
        DAY_MON_SYEAR("[0-9]?[0-9]([ /\\.-])[0-9]?[0-9]\\1[0-9][0-9]")
        {
            @Override
            public Calendar convertToCalendar(String dateStr) throws ParseException
            {
                DateFormat df;
                if (dateStr.contains("/"))
                {
                    df = new SimpleDateFormat("dd/MM/yy");
                }
                else if (dateStr.contains("."))
                {
                    df = new SimpleDateFormat("dd.MM.yy");
                }
                else if (dateStr.contains("-"))
                {
                    df = new SimpleDateFormat("dd-MM-yy");
                }
                else if (dateStr.contains(" "))
                {
                    df = new SimpleDateFormat("dd MM yy");
                }
                else
                {
                    return null;
                }
                Calendar result = new GregorianCalendar();
                result.setLenient(false);
                df.setLenient(false);
                result.setTime(df.parse(dateStr));
                return result;
            }
            
            @Override
            public UIFieldFormatterIFace.PartialDateEnum getPrecision(String dateStr) throws ParseException
            {
                String month = dateStr.substring(3, 5);
                String day = dateStr.substring(0, 2);
                boolean noMonth = month.equals("00") || month.equals("  ");
                boolean noDay = day.equals("00") || day.equals("  ");
                if (noMonth && !noDay)
                {
                    throw new ParseException("invalid date", 0);
                }
                if (noMonth && noDay)
                {
                    return UIFieldFormatterIFace.PartialDateEnum.Year;
                }
                if (noDay)
                {
                    return UIFieldFormatterIFace.PartialDateEnum.Month;
                }
                return UIFieldFormatterIFace.PartialDateEnum.Full;
            }
            
            @Override
            public String adjustForPrecision(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
            {
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                {
                    return "01" + dateStr.substring(2,3) + "01" + dateStr.substring(5);
                }
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                {
                    return "01" + dateStr.substring(2);
                }
                return dateStr;
            }
            
            @Override
            public String adjustForPrecisionOut(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
            {
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                {
                    return "00" + dateStr.substring(2,3) + "00" + dateStr.substring(5);
                }
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                {
                    return "00" + dateStr.substring(2);
                }
                return dateStr;
            }

            @Override
            public String getFormatString(String separator)
            {
                return "MM" + separator + "dd" + separator + "yy";
            }
            
        },
        LYEAR_MON_DAY(
        // "[0123456789]?[0123456789]([ /\\.-])[0123456789]?[0123456789]\\1[0-9][0-9][0-9][0-9]")
                "[0-9][0-9][0-9][0-9]([ /\\.-])[0-9]?[0-9]\\1[0-9]?[0-9]")
        {
            @Override
            public Calendar convertToCalendar(String dateStr) throws ParseException
            {
                DateFormat df;
                if (dateStr.contains("/"))
                {
                    df = new SimpleDateFormat("yyyy/MM/dd");
                }
                else if (dateStr.contains("."))
                {
                    df = new SimpleDateFormat("yyyy.MM.dd");
                }
                else if (dateStr.contains("-"))
                {
                    df = new SimpleDateFormat("yyyy-MM-dd");
                }
                else if (dateStr.contains(" "))
                {
                    df = new SimpleDateFormat("yyyy MM dd");
                }
                else
                {
                    return null;
                }
                Calendar result = new GregorianCalendar();
                result.setLenient(false);
                df.setLenient(false);
                result.setTime(df.parse(dateStr));
                return result;
            }

            @Override
            public UIFieldFormatterIFace.PartialDateEnum getPrecision(String dateStr) throws ParseException
            {
                String month = dateStr.substring(5, 7);
                String day = dateStr.substring(8, 10);
                boolean noMonth = month.equals("00") || month.equals("  ");
                boolean noDay = day.equals("00") || day.equals("  ");
                if (noMonth && !noDay)
                {
                    throw new ParseException("invalid date", 0);
                }
                if (noMonth && noDay)
                {
                    return UIFieldFormatterIFace.PartialDateEnum.Year;
                }
                if (noDay)
                {
                    return UIFieldFormatterIFace.PartialDateEnum.Month;
                }
                return UIFieldFormatterIFace.PartialDateEnum.Full;
            }
            
            @Override
            public String adjustForPrecision(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
            {
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                {
                    return dateStr.substring(0,5) + "01" + dateStr.substring(7,8) + "01";
                }
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                {
                    return dateStr.substring(0,8) + "01";
                }
                return dateStr;
            }
            
            @Override
            public String adjustForPrecisionOut(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
            {
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                {
                    return dateStr.substring(0,5) + "00" + dateStr.substring(7,8) + "00";
                }
                if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                {
                    return dateStr.substring(0,8) + "00";
                }
                return dateStr;
            }

            @Override
            public String getFormatString(String separator)
            {
                return "yyyy" + separator + "MM" + separator + "dd";
            }
            
        },
        DAY_CMON_LYEAR(
                // "[0123456789]?[0123456789]([ /\\.-])[0123456789]?[0123456789]\\1[0-9][0-9][0-9][0-9]")
                        "[0-9][0-9]([ /\\.-])[A-z|-][A-z|-][A-z|-]\\1[0-9][0-9][0-9][0-9]")
                {
                    @Override
                    public Calendar convertToCalendar(String dateStr) throws ParseException
                    {
                        DateFormat df;
                        if (dateStr.contains("/"))
                        {
                            df = new SimpleDateFormat("dd/MMM/yyy");
                        }
                        else if (dateStr.contains("."))
                        {
                            df = new SimpleDateFormat("dd.MMM.yyyy");
                        }
                        else if (dateStr.contains("-"))
                        {
                            df = new SimpleDateFormat("dd-MMM-yyyy");
                        }
                        else if (dateStr.contains(" "))
                        {
                            df = new SimpleDateFormat("dd MMM yyyy");
                        }
                        else
                        {
                            return null;
                        }
                        Calendar result = new GregorianCalendar();
                        result.setLenient(false);
                        result.setTime(df.parse(dateStr));
                        return result;
                    }

                    @Override
                    public UIFieldFormatterIFace.PartialDateEnum getPrecision(String dateStr) throws ParseException
                    {
                        String month = dateStr.substring(3, 6);
                        String day = dateStr.substring(0, 2);
                        boolean noMonth = month.equals("00") || month.equals("  ") || month.equals("---");
                        boolean noDay = day.equals("00") || day.equals("  ");
                        if (noMonth && !noDay)
                        {
                            throw new ParseException("invalid date", 0);
                        }
                        if (noMonth && noDay)
                        {
                            return UIFieldFormatterIFace.PartialDateEnum.Year;
                        }
                        if (noDay)
                        {
                            return UIFieldFormatterIFace.PartialDateEnum.Month;
                        }
                        return UIFieldFormatterIFace.PartialDateEnum.Full;
                    }
                    
                    @Override
                    public String adjustForPrecision(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
                    {
                        if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                        {
                            //NOTE: this switches the format to DAY_MON_LYEAR 
                            return "01" + dateStr.substring(2,3) + "01" + dateStr.substring(6);
                        }
                        if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                        {
                            return "01" + dateStr.substring(2);
                        }
                        return dateStr;
                    }
                    
                    @Override
                    public String adjustForPrecisionOut(String dateStr, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException
                    {
                        if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                        {
                            //NOTE: this switches the format to DAY_MON_LYEAR 
                            return "00" + dateStr.substring(2,3) + "00" + dateStr.substring(6);
                        }
                        if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month))
                        {
                            return "00" + dateStr.substring(2);
                        }
                        return dateStr;
                    }

                    @Override
                    public String getFormatString(String separator)
                    {
                        return "dd" + separator + "MMM" + separator + "yyyy";
                    }
                };
        public final String regex;

        DateFormats(String regex)
        {
            this.regex = regex;
        }

        public boolean matches(String input)
        {
            return input.matches(regex);
        }

        public abstract Calendar convertToCalendar(String original) throws ParseException;
        public abstract UIFieldFormatterIFace.PartialDateEnum getPrecision(String original) throws ParseException;
        public abstract String adjustForPrecision(String original, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException;
        public abstract String adjustForPrecisionOut(String original, UIFieldFormatterIFace.PartialDateEnum prec) throws ParseException;
        public abstract String getFormatString(String separator);
    }

    /**
     * 
     */
    public DateConverter()
    {
        String currentFormat = AppPreferences.getRemote().get("ui.formatting.scrdateformat", null);  
        if (currentFormat.startsWith("MM") || currentFormat.startsWith("mm"))
        {
        	preferMonthDay = true;
        } else if (currentFormat.startsWith("DD") || currentFormat.startsWith("dd"))
        {
        	preferMonthDay = false;
        } else
        {
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT); //for default locale; SHORT -> completely numeric
            Calendar result = new GregorianCalendar();
            result.set(2000, 3, 1);
            String dtstr = df.format(result.getTime());
            int moIdx = dtstr.indexOf("4");
            int dayIdx = dtstr.indexOf("1");
            preferMonthDay = moIdx < dayIdx;
        }
        
        //System.out.println(df.)
        //preferMonthDay = /*loc == Locale.CANADA || */loc == Locale.US;            
    }

    /**
     * @param dateStr
     * @return DateFormats matching dateStr
     * 
     * If multiple matches are found, uses preferMonthDay value to choose the match. 
     */
    /**
     * @param dateStr
     * @return
     */
    protected DateFormats match(String dateStr)
    {
        LinkedList<DateFormats> matches = new LinkedList<DateFormats>();
        for (DateFormats format : DateFormats.values())
        {
            if (format.matches(dateStr))
            {
                matches.add(format);
            }
        }
        
        if (matches.size() == 0)
        {
            return null;
        }
        
        if (matches.size() == 1)
        {
            return matches.get(0);
        }
        
        for (DateFormats format : matches)
        {
            if (preferMonthDay && format.equals(DateFormats.MON_DAY_LYEAR) || format.equals(DateFormats.MON_DAY_SYEAR))
            {
                return format;
            }
            else if (!preferMonthDay && format.equals(DateFormats.DAY_MON_LYEAR) || format.equals(DateFormats.DAY_MON_SYEAR))
            {
                return format;
            }
        }
        /*
         * It shouldn't be possible for multiple matches to exist involving formats other than DAY_MON_LYEAR/SYEAR and MON_DAY_LYEAR/SYEAR.
         * But if it occurs, complain and return null.
         */
        if (matches.size() > 0)
        {
            log.error("Unable to resolve multiple date-format matches for '" + dateStr + "'");
        }
        
        return null;
    }
    
    /**
     * @param dateStr
     * @return Calendar defined by date
     * @throws ParseException
     */
    public Calendar convert(String dateStr) throws ParseException
    {
        if (StringUtils.isBlank(dateStr)) 
        { 
            return null; 
        }
        
        DateFormats format = match(dateStr);
        if (format != null) 
        { 
            return format.convertToCalendar(dateStr); 
        }

        throw new ParseException("unrecognized date format", 0);
    }

    /**
     * @param dateStr
     * @return the precision of dateStr
     * @throws ParseException
     */
    public UIFieldFormatterIFace.PartialDateEnum getDatePrecision(String dateStr) throws ParseException
    {
        if (StringUtils.isBlank(dateStr)) 
        { 
            return UIFieldFormatterIFace.PartialDateEnum.None; 
        }
        
        DateFormats format = match(dateStr);
        if (format != null)
        {
            return format.getPrecision(dateStr); 
        }

        throw new ParseException("unrecognized date format", 0);
    }
    
    /**
     * @param dateStr
     * @return a String defining a valid Calendar object according to precision of dateStr
     * @throws ParseException
     */
    public String adjustForPrecision(String dateStr) throws ParseException
    {
        if (StringUtils.isBlank(dateStr)) 
        { 
            return dateStr; 
        }
        
        DateFormats format = match(dateStr);
        if (format != null)
        {
            UIFieldFormatterIFace.PartialDateEnum prec = format.getPrecision(dateStr);
            return format.adjustForPrecision(dateStr, prec);
        }
        
        throw new ParseException("unrecognized date format", 0);
    }
    
    /**
     * @return the preferMonthDay
     */
    public boolean isPreferMonthDay()
    {
        return preferMonthDay;
    }

    /**
     * @param preferMonthDay the preferMonthDay to set
     */
    public void setPreferMonthDay(boolean preferMonthDay)
    {
        this.preferMonthDay = preferMonthDay;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub
        DateConverter dc = new DateConverter();
        try
        {
            System.out.println(dc.convert("12/07/2007").getTime().toString());
            System.out.println(dc.convert("3/21/2006").getTime().toString());
            System.out.println(dc.convert("12 07 2007").getTime().toString());
            System.out.println(dc.convert("3/22/2004").getTime().toString());
            System.out.println(dc.convert("12-07-2007").getTime().toString());
            System.out.println(dc.convert("5.21.1999").getTime().toString());
            System.out.println(dc.convert("9/15/04").getTime().toString());
            System.out.println(dc.convert("11-1-07").getTime().toString());
            System.out.println(dc.convert("5.21.00").getTime().toString());
            System.out.println(dc.convert("3/12/2004").getTime().toString());
            System.out.println(dc.convert("12-07-2007").getTime().toString());
            System.out.println(dc.convert("5.42.1999").getTime().toString());
        }
        catch (ParseException pe)
        {
            System.out.println(pe.getMessage());
        }
    }


}
