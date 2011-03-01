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
package edu.ku.brc.specify.conversion;

import org.apache.log4j.Logger;

/**
 * Simple class that makes it easy to show elapsed time.
 * @author rods
 *
 * @code_status Complete
 *
 * Oct 21, 2009
 *
 */
public class TimeLogger
{
    private static final Logger log = Logger.getLogger(TimeLogger.class);
    
    private String desc        = null;
    private long   startTime;
    private long   endTime;
    
    /**
     * Constructor - Captures the start time.
     */
    public TimeLogger()
    {
        super();
        start();
    }
    
    /**
     * Constructor - Captures the start time.
     */
    public TimeLogger(final String desc)
    {
        this();
        this.desc = desc;
    }
    
    /**
     * Sets the start time (automatically called by the constructor).
     */
    public void start()
    {
        startTime = System.currentTimeMillis();
        endTime   = 0;
    }
    
    /**
     * Sets the start time (automatically called by the constructor).
     * @param description set the output description
     */
    public void start(final String description)
    {
        this.desc = description;
        start();
    }
    
    /**
     * Call end and then start Sets the start time (automatically called by the constructor).
     * @param description set the output description
     */
    public void restart(final String description)
    {
        end();
        this.desc = description;
        start();
    }
    
    /**
     * @return a string with hrs:min:secs (total seconds) (milliseconds) and prints to System.out.
     */
    public String end()
    {
        endTime = System.currentTimeMillis();
        
        double totalSeconds = ((double)(endTime - startTime)) / 1000.0;
        
        int hours = (int)(totalSeconds / 3600.0);
        int mins  = (int)((totalSeconds - (hours * 3600)) / 60);
        int secs  = (int)(totalSeconds - (hours * 3600) - (mins * 60));
               
        String str = String.format("%sElapsed Time: %02d:%02d:%02d (%8.4f) (%d)", (desc != null ? (desc + " - ") : ""), hours, mins, secs, totalSeconds, endTime);
        log.debug(str);
        return str;
    }
}
