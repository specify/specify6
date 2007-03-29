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

import static edu.ku.brc.util.LatLonConverter.convertDDDDToDDDD;
import static edu.ku.brc.util.LatLonConverter.convertDDMMMMToDDDD;
import static edu.ku.brc.util.LatLonConverter.convertDDMMSSToDDDD;
import static edu.ku.brc.util.LatLonConverter.convertToDDDDDD;
import static edu.ku.brc.util.LatLonConverter.convertToDDMMMM;
import static edu.ku.brc.util.LatLonConverter.convertToDDMMSS;

import java.math.BigDecimal;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.LatLonConverter.DEGREES_FORMAT;
import edu.ku.brc.util.LatLonConverter.DIRECTION;
import edu.ku.brc.util.LatLonConverter.FORMAT;
import edu.ku.brc.util.LatLonConverter.LATLON;

/**
 * Tests conversion of BigDecimal to a String and back to BigDecimal.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Jan 17, 2007
 *
 */
public class LatLonConversionTest extends TestCase
{
    protected static final Logger log = Logger.getLogger(LatLonConversionTest.class);
    
    /**
     * Test method for {@link edu.ku.brc.util.LatLonConverter#convertToDDMMSS(java.math.BigDecimal)}.
     */
    public void testConvertToDDMMSS()
    {
        BigDecimal before = new BigDecimal("38.95402");
        String     str    = convertToDDMMSS(before);
        BigDecimal after  =  convertDDMMSSToDDDD(str, "N");
        
        assertEquals(str, "38 57 14.472");
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDMMSS, DEGREES_FORMAT.String), "38 57 14.472 N");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDMMSS, DEGREES_FORMAT.String), "38 57 14.472 E");
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDMMSS(before);
        after  =  convertDDMMSSToDDDD(str, "S");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDMMSS, DEGREES_FORMAT.String), "38 57 14.472 S");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDMMSS, DEGREES_FORMAT.String), "38 57 14.472 W");
       
        before = new BigDecimal("38.95402");
        str    = convertToDDMMSS(before);
        after  =  convertDDMMSSToDDDD(str, "E");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDMMSS(before);
        after  =  convertDDMMSSToDDDD(str, "W");
        
        assertEquals(before.doubleValue(), after.doubleValue());

    }

    /**
     * Test method for {@link edu.ku.brc.util.LatLonConverter#convertToDDMMMM(java.math.BigDecimal)}.
     */
    public void testConvertToDDMMMM()
    {
        BigDecimal before = new BigDecimal("38.95402");
        String     str    = convertToDDMMMM(before);
        BigDecimal after  =  convertDDMMMMToDDDD(str, "N");
        
        assertEquals(str, "38 57.2412");
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDMMMM, DEGREES_FORMAT.String), "38 57.2412 N");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDMMMM, DEGREES_FORMAT.String), "38 57.2412 E");
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDMMMM(before);
        after  =  convertDDMMMMToDDDD(str, "S");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDMMMM, DEGREES_FORMAT.String), "38 57.2412 S");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDMMMM, DEGREES_FORMAT.String), "38 57.2412 W");

        before = new BigDecimal("38.95402");
        str    = convertToDDMMMM(before);
        after  =  convertDDMMMMToDDDD(str, "E");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDMMMM(before);
        after  =  convertDDMMMMToDDDD(str, "W");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        
    }

    /**
     * Test method for {@link edu.ku.brc.util.LatLonConverter#convertDDDDToDDDD(java.lang.String)}.
     */
    public void testConvertDDDDToDDDDString()
    {
        BigDecimal before = new BigDecimal("38.95402");
        String     str    = convertToDDDDDD(before);
        BigDecimal after  =  convertDDDDToDDDD(str, "N");
        
        assertEquals(str, "38.95402");
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDDDDD, DEGREES_FORMAT.String), "38.95402 N");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDDDDD, DEGREES_FORMAT.String), "38.95402 E");
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDDDDD(before);
        after  =  convertDDDDToDDDD(str, "S");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDDDDD, DEGREES_FORMAT.String), "38.95402 S");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDDDDD, DEGREES_FORMAT.String), "38.95402 W");
        
        before = new BigDecimal("38.95402");
        str    = convertToDDDDDD(before);
        after  =  convertDDDDToDDDD(str, "E");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDDDDD(before);
        after  =  convertDDDDToDDDD(str, "W");
        
        before = new BigDecimal("0.0");
        str    = convertToDDDDDD(before);
        after  =  convertDDDDToDDDD(str, "W");
        
        assertEquals(str, "0.0");
        assertEquals(before.doubleValue(), after.doubleValue());

        before = new BigDecimal("-0.0");
        str    = convertToDDDDDD(before);
        after  =  convertDDDDToDDDD(str, "W");
        
        assertEquals(str, "0.0");
        assertEquals(before.doubleValue(), after.doubleValue());

    }
    
    /**
     * Test method for {@link edu.ku.brc.util.LatLonConverter#convertDDDDToDDDD(java.lang.String)}.
     */
    public void testDegreesSymbol()
    {
        // this test just outputs some conversions
        BigDecimal before = new BigDecimal("38.95402");
        String     str    = convertToDDDDDD(before, DEGREES_FORMAT.Symbol, DIRECTION.NorthSouth);
        log.info(str);
        
        str = convertToDDDDDD(before, DEGREES_FORMAT.String, DIRECTION.NorthSouth);
        log.info(str);
        
        str = convertToDDDDDD(before, DEGREES_FORMAT.Symbol, DIRECTION.EastWest);
        log.info(str);
        
        str = convertToDDDDDD(before, DEGREES_FORMAT.String, DIRECTION.EastWest);
        log.info(str);
        
        assertTrue(true);
 
    }
}
