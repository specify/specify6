package edu.ku.brc.specify.tests;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.GregorianCalendar;
import java.util.prefs.Preferences;

import org.apache.commons.lang.time.FastDateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;
import edu.ku.brc.specify.helpers.*;


public class MiscUtilTest extends TestCase
{
    private static Log log = LogFactory.getLog(MiscUtilTest.class);
    /**
     * Tests the Date formating 
     */
    public void testSimpleDateFormat()
    {
        Calendar calendar = new GregorianCalendar();
        
        calendar.clear(); // clear time components
        
        calendar.set(Calendar.YEAR, 1845);
        calendar.set(Calendar.MONTH, 9);       // Oct
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        
        log.info(formatter.format(calendar.getTime()));
        assertTrue(formatter.format(calendar.getTime()).equals("10/01/1845"));

        int origDate = 18451001;
        
        Date date = UIHelper.convertIntToDate(origDate);
        
        log.info("date:     "+date.getTime());
        log.info("calendar: "+calendar.getTimeInMillis());
        
        assertTrue(date.getTime() == calendar.getTimeInMillis());
        
        int iDate = UIHelper.convertDateToInt(date);
        
        log.info("origDate: "+origDate);
        log.info("iDate:    "+iDate);
        
        assertTrue(iDate == origDate);
        
        log.info("10/01/1845");
        log.info(formatter.format(date));
        assertTrue(formatter.format(date).equals("10/01/1845"));
        
    }
}
