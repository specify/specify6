/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tests;

import static edu.ku.brc.util.LatLonConverter.convertDDDDStrToDDDDBD;
import static edu.ku.brc.util.LatLonConverter.convertDDMMMMStrToDDDDBD;
import static edu.ku.brc.util.LatLonConverter.convertDDMMSSStrToDDDDBD;
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
    
    protected int        decimalLen = 10;
    
    /**
     * Test method for {@link edu.ku.brc.util.LatLonConverter#convertToDDMMSS(java.math.BigDecimal)}.
     */
    public void testConvertToDDMMSS()
    {
        BigDecimal before = new BigDecimal("38.95402");
        String     str    = convertToDDMMSS(before, decimalLen);
        BigDecimal after  =  convertDDMMSSStrToDDDDBD(str, "N");
        
        assertEquals(str, "38 57 14.472");
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDMMSS, DEGREES_FORMAT.String, decimalLen), "38 57 14.472 N");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDMMSS, DEGREES_FORMAT.String, decimalLen), "38 57 14.472 E");
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDMMSS(before, decimalLen);
        after  =  convertDDMMSSStrToDDDDBD(str, "S");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDMMSS, DEGREES_FORMAT.String, decimalLen), "38 57 14.472 S");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDMMSS, DEGREES_FORMAT.String, decimalLen), "38 57 14.472 W");
       
        before = new BigDecimal("38.95402");
        str    = convertToDDMMSS(before, decimalLen);
        after  =  convertDDMMSSStrToDDDDBD(str, "E");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDMMSS(before, decimalLen);
        after  =  convertDDMMSSStrToDDDDBD(str, "W");
        
        assertEquals(before.doubleValue(), after.doubleValue());

    }

    /**
     * Test method for {@link edu.ku.brc.util.LatLonConverter#convertToDDMMMM(java.math.BigDecimal)}.
     */
    public void testConvertToDDMMMM()
    {
        BigDecimal before = new BigDecimal("38.95402");
        String     str    = convertToDDMMMM(before, decimalLen);
        BigDecimal after  =  convertDDMMMMStrToDDDDBD(str, "N");
        
        assertEquals(str, "38 57.2412");
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDMMMM, DEGREES_FORMAT.String, decimalLen), "38 57.2412 N");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDMMMM, DEGREES_FORMAT.String, decimalLen), "38 57.2412 E");
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDMMMM(before, decimalLen);
        after  =  convertDDMMMMStrToDDDDBD(str, "S");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDMMMM, DEGREES_FORMAT.String, decimalLen), "38 57.2412 S");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDMMMM, DEGREES_FORMAT.String, decimalLen), "38 57.2412 W");

        before = new BigDecimal("38.95402");
        str    = convertToDDMMMM(before, decimalLen);
        after  =  convertDDMMMMStrToDDDDBD(str, "E");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDMMMM(before, decimalLen);
        after  =  convertDDMMMMStrToDDDDBD(str, "W");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        
    }

    /**
     * Test method for {@link edu.ku.brc.util.LatLonConverter#convertDDDDStrToDDDDBD(java.lang.String)}.
     */
    public void testOneMinute()
    {
        BigDecimal latitude = LatLonConverter.convertDDMMMMStrToDDDDBD("1 1", "N");
        String     ddmmVal  = LatLonConverter.convertToDDMMMM(latitude, decimalLen);
        
        String s1 = String.format("%10.7f", latitude);
        String s2 = String.format("%10.7f", 1.01666666667);
        log.info(s1+"  "+s2+"  "+ddmmVal);
        
        assertTrue(s1.equals(s2));
        //LatLonConverter
    }

    /**
     * Test method for {@link edu.ku.brc.util.LatLonConverter#convertDDDDStrToDDDDBD(java.lang.String)}.
     */
    public void testConvertDDDDToDDDDString()
    {
        BigDecimal before = new BigDecimal("38.95402");
        String     str    = convertToDDDDDD(before, decimalLen);
        BigDecimal after  =  convertDDDDStrToDDDDBD(str, "N");
        
        assertEquals(str, "38.95402");
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDDDDD, DEGREES_FORMAT.String, decimalLen), "38.95402 N");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDDDDD, DEGREES_FORMAT.String, decimalLen), "38.95402 E");
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDDDDD(before, decimalLen);
        after  =  convertDDDDStrToDDDDBD(str, "S");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        assertEquals(LatLonConverter.format(before, LATLON.Latitude, FORMAT.DDDDDD, DEGREES_FORMAT.String, decimalLen), "38.95402 S");
        assertEquals(LatLonConverter.format(before, LATLON.Longitude, FORMAT.DDDDDD, DEGREES_FORMAT.String, decimalLen), "38.95402 W");
        
        before = new BigDecimal("38.95402");
        str    = convertToDDDDDD(before, decimalLen);
        after  =  convertDDDDStrToDDDDBD(str, "E");
        
        assertEquals(before.doubleValue(), after.doubleValue());
        
        before = new BigDecimal("-38.95402");
        str    = convertToDDDDDD(before, decimalLen);
        after  =  convertDDDDStrToDDDDBD(str, "W");
        
        before = new BigDecimal("0.0");
        str    = convertToDDDDDD(before, decimalLen);
        after  =  convertDDDDStrToDDDDBD(str, "W");
        
        assertEquals(str, "0.0");
        assertEquals(before.doubleValue(), after.doubleValue());

        before = new BigDecimal("-0.0");
        str    = convertToDDDDDD(before, decimalLen);
        after  =  convertDDDDStrToDDDDBD(str, "W");
        
        assertEquals(str, "0.0");
        assertEquals(before.doubleValue(), after.doubleValue());

    }
    
    /**
     * Test method for {@link edu.ku.brc.util.LatLonConverter#convertDDDDStrToDDDDBD(java.lang.String)}.
     */
    public void testDegreesSymbol()
    {
        // this test just outputs some conversions
        BigDecimal before = new BigDecimal("38.95402");
        String     str    = convertToDDDDDD(before, DEGREES_FORMAT.Symbol, DIRECTION.NorthSouth, decimalLen);
        log.info(str);
        
        str = convertToDDDDDD(before, DEGREES_FORMAT.String, DIRECTION.NorthSouth, decimalLen);
        log.info(str);
        
        str = convertToDDDDDD(before, DEGREES_FORMAT.Symbol, DIRECTION.EastWest, decimalLen);
        log.info(str);
        
        str = convertToDDDDDD(before, DEGREES_FORMAT.String, DIRECTION.EastWest, decimalLen);
        log.info(str);
        
        assertTrue(true);
    }
}
