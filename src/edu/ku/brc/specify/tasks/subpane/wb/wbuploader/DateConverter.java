/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Mostly copied from Josh's GeoRefConverter class.
 * 
 * Needs more formats.
 * Also problems with conversion, for example 5-42-1999 gets converted to june 11, 1999, which is probably not desirable?
 * Also, error handling is not thought out at all.
 * Oh, and, how to tell if 7/6/2007 means july 6 or june 7?? 
 *
 */
public class DateConverter
{

    public static enum DateFormats {
        MON_DAY_LYEAR(
                "[0123456789]?[0123456789]([ /\\.-])[0123456789]?[0123456789]\\1[0-9][0-9][0-9][0-9]")
        {
            @Override
            public Calendar convertToCalendar(String dateStr) throws ParseException
            {
                DateFormat df;
                if (dateStr.contains(" "))
                {
                    df = new SimpleDateFormat("MM dd yyyy");
                }
                else if (dateStr.contains("/"))
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
                else
                {
                    return null;
                }
                Calendar result = new GregorianCalendar();
                result.setTime(df.parse(dateStr));
                return result;
            }
        },
        MON_DAY_SYEAR("[0123456789]?[0123456789]([ /\\.-])[0123456789]?[0123456789]\\1[0-9][0-9]")
        {
            @Override
            public Calendar convertToCalendar(String dateStr) throws ParseException
            {
                DateFormat df;
                if (dateStr.contains(" "))
                {
                    df = new SimpleDateFormat("MM dd yy");
                }
                else if (dateStr.contains("/"))
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
                else
                {
                    return null;
                }
                Calendar result = new GregorianCalendar();
                result.setTime(df.parse(dateStr));
                return result;
            }
        },
        DAY_MON_LYEAR(
                "[0123456789]?[0123456789]([ /\\.-])[0123456789]?[0123456789]\\1[0-9][0-9][0-9][0-9]")
        {
            @Override
            public Calendar convertToCalendar(String dateStr) throws ParseException
            {
                DateFormat df;
                if (dateStr.contains(" "))
                {
                    df = new SimpleDateFormat("dd MM yyyy");
                }
                else if (dateStr.contains("/"))
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
                else
                {
                    return null;
                }
                Calendar result = new GregorianCalendar();
                result.setTime(df.parse(dateStr));
                return result;
            }
        },
        DAY_MON_SYEAR("[0123456789]?[0123456789]([ /\\.-])[0123456789]?[0123456789]\\1[0-9][0-9]")
        {
            @Override
            public Calendar convertToCalendar(String dateStr) throws ParseException
            {
                DateFormat df;
                if (dateStr.contains(" "))
                {
                    df = new SimpleDateFormat("dd MM yy");
                }
                else if (dateStr.contains("/"))
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
                else
                {
                    return null;
                }
                Calendar result = new GregorianCalendar();
                result.setTime(df.parse(dateStr));
                return result;
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
    }

    public DateConverter()
    {
        //nothing do do
    }

    public Calendar convert(String dateStr) throws ParseException
    {
        if (dateStr == null) { return null; }
        for (DateFormats format : DateFormats.values())
        {
            if (dateStr.matches(format.regex)) { return format.convertToCalendar(dateStr); }
        }

        throw new ParseException("unrecognized date format", 0);
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
