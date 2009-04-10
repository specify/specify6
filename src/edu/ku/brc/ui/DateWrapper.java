/* Copyright (C) 2009, University of Kansas Center for Research
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
        return simpleDateFormat.format(date.getTime());
    }
    
}
