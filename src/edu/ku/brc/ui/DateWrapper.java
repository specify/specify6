/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Wraps a SimpleDateFormat so it can be automatically changed by the pref system via the PrefsCachMgr.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 */
public class DateWrapper
{
    protected SimpleDateFormat simpleDateFormat;
    
    /**
     * Constructor.
     * @param simpleDateFormat the format
     */
    public DateWrapper(final SimpleDateFormat simpleDateFormat)
    {
        this.simpleDateFormat = simpleDateFormat;
    }

    /**
     * Returns the format.
     * @return the format.
     */
    public SimpleDateFormat getSimpleDateFormat()
    {
        return simpleDateFormat;
    }

    /**
     * Sets the format.
     * @param simpleDateFormat the new format
     */
    public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat)
    {
        this.simpleDateFormat = simpleDateFormat;
    }
    
    /**
     * Formats a date.
     * @param date the date to be formatted
     * @return a formatted string
     */
    public String format(final Date date)
    {
        return simpleDateFormat.format(date);
    }
    
    /**
     * Formats a date.
     * @param date the date to be formatted
     * @return a formatted string
     */
    public String format(final Calendar date)
    {
        return simpleDateFormat.format(date);
    }
    
}
