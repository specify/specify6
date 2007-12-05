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
package edu.ku.brc.specify.tests;

import edu.ku.brc.ui.DateParser;
import edu.ku.brc.ui.DateParser.DateFormatType;
import junit.framework.TestCase;


public class DateParserTests extends TestCase
{

    public void testDateType()
    {
        DateFormatType[] types  = {DateFormatType.YYYY_AA_BB, DateFormatType.YYYY_A_BB, DateFormatType.YYYY_AA_B, DateFormatType.YYYY_A_B, 
                                   DateFormatType.AA_BB_YYYY, DateFormatType.A_BB_YYYY, DateFormatType.AA_B_YYYY, DateFormatType.A_B_YYYY};
        
        String[] format = {"yyyy/MM/dd",  "yyyy/M/dd",  "yyyy/MM/d",  "yyyy/M/d", 
                           "MM/dd/yyyy",  "M/dd/yyyy",  "MM/d/yyyy",  "M/d/yyyy"};
        for (int i=0;i<8;i++)
        {
            System.out.println(types[i] + " " + format[i] + " " + (DateParser.getDateFormatType(format[i]) == types[i]));
            if (DateParser.getDateFormatType(format[i]) != types[i])
            {
                DateParser.getDateFormatType(format[i]);
                int x = 0;
                x++;
            }
            assertTrue(DateParser.getDateFormatType(format[i]) == types[i]);
        }
        int x = 0;
        x++;
    }
    
    public void testDateStrs()
    {
        String[] defaultFormatters = {"yyyy/MM/dd",  "yyyy/M/dd",  "yyyy/MM/d",  "yyyy/M/d", 
                                      "MM/dd/yyyy",  "M/dd/yyyy",  "MM/d/yyyy",  "M/d/yyyy",
                                      "yyyy-MM-dd",  "yyyy-M-dd",  "yyyy-MM-d",  "yyyy-M-d", 
                                      "MM-dd-yyyy",  "M-dd-yyyy",  "MM-d-yyyy",  "M-d-yyyy",
                                      "yyyy.MM.dd",  "yyyy.M.dd",  "yyyy.MM.d",  "yyyy.M.d", 
                                      "MM.dd.yyyy",  "M.dd.yyyy",  "MM.d.yyyy",  "M.d.yyyy"};
                                
        for (String defaultFormat : defaultFormatters)
        {
            
            String[] dateStrs = {"2001/01/11",  "2001/1/11",  "2001/01/1",  "2001/1/1", 
                               "01/11/2001",  "1/11/2001",  "01/1/2001",  "1/1/2001",
                               "2001.01.11",  "2001.1.11",  "2001.01.1",  "2001.1.1", 
                               "01.11.2001",  "1.11.2001",  "01.1.2001",  "1.1.2001",
                               "2001-01-11",  "2001-1-11",  "2001-01-1",  "2001-1-1", 
                               "01-11-2001",  "1-11-2001",  "01-1-2001",  "1-1-2001"};
            
            DateParser dd = new DateParser(defaultFormat);
            for (String dStr : dateStrs)
            {
                System.out.println(dStr + " " + (dd.parseDate(dStr) != null));
            }
        }
    }
    
    
    public void testValid()
    {
        
        String defaultFormat = "yyyy/MM/dd";
        
        DateParser dd = new DateParser(defaultFormat);
        
        String[] dateStrs = {"2001/0D/11",  "d001/1/11",  "201/01/1",  "2001//1",
                             "2001/13/11",  "2001/12/32",  "2001/2/29", "2001/00/00"};
        for (String dateStr : dateStrs)
        {
            System.out.println(dateStr + " " + dd.parseDate(dateStr));
            if (dateStr.equals("2001/12/32"))
            {
                int x = 0;
                x++;
            }
            assertTrue(dd.parseDate(dateStr) == null);
        }
        
        assertTrue(dd.parseDate("02/29/2000") != null);
        assertTrue(dd.parseDate("02/29/2004") != null);
        assertTrue(dd.parseDate("00/29/2000", true, false) != null);
        assertTrue(dd.parseDate("00/00/2004", true, true) != null);
        
        System.out.println(dd.parseDate("00/29/2000", true, true));
        System.out.println(dd.parseDate("00/00/2004", true, true));
    }

}
