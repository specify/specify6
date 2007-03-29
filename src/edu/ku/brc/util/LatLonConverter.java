/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.util;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;


/**
 * Helper for methods for convert to and from various different formats to Decimal Degrees.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 10, 2007
 *
 */
public class LatLonConverter
{
    public enum LATLON          {Latitude, Longitude}
    public enum FORMAT          {DDDDDD, DDMMMM, DDMMSS}
    public enum DEGREES_FORMAT  {None, Symbol, String}
    public enum DIRECTION       {None, NorthSouth, EastWest}
    
    private static boolean useDB = false;
    
    public static DecimalFormat decFormatter = new DecimalFormat("#0.0000000000#");
    
    protected static BigDecimal     minusOne      = new BigDecimal("-1.0");
    protected static DecimalFormat decFormatter2  = new DecimalFormat("#0");
    protected static BigDecimal    one            = new BigDecimal("1.0");
    protected static BigDecimal    sixty          = new BigDecimal("60.0");
    
    public static String[] NORTH_SOUTH;
    public static String[] EAST_WEST;
    
    public static String[] northSouth  = null;
    public static String[] eastWest    = null;

    
    static 
    {
        NORTH_SOUTH = new  String[] {"N", "S"};
        EAST_WEST   = new  String[] {"E", "W"};
        northSouth = new String[] {getResourceString(NORTH_SOUTH[0]), getResourceString(NORTH_SOUTH[1])};
        eastWest   = new String[] {getResourceString(EAST_WEST[0]), getResourceString(EAST_WEST[1])};      
    }

    /**
     * Converts BigDecimal to Degrees, Minutes and Decimal Seconds.
     * @param bc the DigDecimal to be converted.
     * @return a 3 piece string
     */
    public static String convertToDDMMSS(final BigDecimal bc)
    {
        return convertToDDMMSS(bc, DEGREES_FORMAT.None, DIRECTION.None);
    }
    
    /**
     * Converts BigDecimal to Degrees, Minutes and Decimal Seconds.
     * @param bc the DigDecimal to be converted.
     * @return a 3 piece string
     */
    public static String convertToDDMMSS(final BigDecimal     bc, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction)
    {
        if (bc.doubleValue() == 0.0)
        {
            return "0.0";
        }
        
        if (useDB)
        {
            BigDecimal remainder = bc.remainder(one);
          
            BigDecimal num = bc.subtract(remainder);
            
            BigDecimal minutes         = new BigDecimal(remainder.multiply(sixty).abs().intValue());
            BigDecimal secondsFraction = remainder.abs().multiply(sixty).subtract(minutes);                  
            BigDecimal seconds         = secondsFraction.multiply(sixty);
            
            //System.out.println("["+decFormatter2.format(num)+"]["+minutes+"]["+seconds+"]");
            
            return decFormatter2.format(num) + " " + minutes + " " + seconds;
            
        }
        //else
        
        double num       = Math.abs(bc.doubleValue());
        int    whole     = (int)Math.floor(num);
        double remainder = num - whole;
        
        double minutes      = remainder * 60.0;
        int    minutesWhole = (int)Math.floor(minutes);
        double secondsFraction = minutes - minutesWhole;
        double seconds = secondsFraction * 60.0;
        
        StringBuilder sb = new StringBuilder();
        if (degreesFMT == DEGREES_FORMAT.Symbol)
        {
            sb.append("\u00B0");
        }
        sb.append(whole);
        sb.append(' ');
        sb.append(minutesWhole);
        sb.append(' ');
        sb.append(StringUtils.strip(String.format("%12.10f", new Object[] {seconds}), "0"));
        
        if (degreesFMT == DEGREES_FORMAT.String)
        {
            int inx = bc.doubleValue() < 0.0 ? 1 : 0;
            sb.append(' ');
            sb.append(direction == DIRECTION.NorthSouth ? northSouth[inx] : eastWest[inx]);
        }
        //return whole + (DEGREES_FORMAT.None ? "\u00B0" : "") + " " + minutesWhole + " " + StringUtils.strip(String.format("%12.10f", new Object[] {seconds}), "0");
        return sb.toString();
    }
    
    /**
     * Converts BigDecimal to Degrees and Decimal Minutes.
     * @param bc the DigDecimal to be converted.
     * @return a 2 piece string
     */
    public static String convertToDDMMMM(final BigDecimal bc)
    {
        return convertToDDMMMM(bc, DEGREES_FORMAT.None, DIRECTION.None);
    }
    
    /**
     * Converts BigDecimal to Degrees and Decimal Minutes.
     * @param bc the DigDecimal to be converted.
     * @return a 2 piece string
     */
    public static String convertToDDMMMM(final BigDecimal     bc, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction)
    {
        if (bc.doubleValue() == 0.0)
        {
            return "0.0";
        }
        
        if (useDB)
        {
            BigDecimal remainder = bc.remainder(one);
          
            BigDecimal num = bc.subtract(remainder);
            
            BigDecimal minutes = remainder.multiply(sixty).abs();
            
            //System.out.println("["+decFormatter2.format(num)+"]["+minutes+"]");
            return decFormatter2.format(num) + " " + minutes;
            
        }
        //else
        
        double num       = Math.abs(bc.doubleValue());
        int    whole     = (int)Math.floor(num);
        double remainder = num - whole;
        
        double minutes = remainder * 60.0;
        //System.out.println("["+whole+"]["+String.format("%10.10f", new Object[] {minutes})+"]");
        
        StringBuilder sb = new StringBuilder();
        if (degreesFMT == DEGREES_FORMAT.Symbol)
        {
            sb.append("\u00B0");
        }
        sb.append(whole);
        sb.append(' ');
        sb.append(StringUtils.strip(String.format("%10.10f", new Object[] {minutes}), "0"));
        
        if (degreesFMT == DEGREES_FORMAT.String)
        {
            int inx = bc.doubleValue() < 0.0 ? 1 : 0;
            sb.append(' ');
            sb.append(direction == DIRECTION.NorthSouth ? northSouth[inx] : eastWest[inx]);
        }
        //return whole + (degreesFMT == DEGREES_FORMAT.Symbol ? "\u00B0" : "") + " " + StringUtils.strip(String.format("%10.10f", new Object[] {minutes}), "0");
        return sb.toString();
        
    }
        
    /**
     * Converts BigDecimal to Decimal Degrees.
     * @param bc the DigDecimal to be converted.
     * @return a 1 piece string
     */
    public static String convertToDDDDDD(final BigDecimal bc)
    {
        return convertToDDDDDD(bc, DEGREES_FORMAT.None, DIRECTION.None);
    }
    
    /**
     * Converts BigDecimal to Decimal Degrees.
     * @param bc the DigDecimal to be converted.
     * @param degreesFMT indicates whether to include the degrees symbol
     * @return a 1 piece string
     */
    public static String convertToDDDDDD(final BigDecimal     bc, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction)
    {
        if (bc.doubleValue() == 0.0)
        {
            return "0.0";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(format(bc.abs()));
        
        if (degreesFMT == DEGREES_FORMAT.Symbol)
        {
            sb.append("\u00B0");
            
        } else if (degreesFMT == DEGREES_FORMAT.String)
        {
            int inx = bc.doubleValue() < 0.0 ? 1 : 0;
            sb.append(' ');
            sb.append(direction == DIRECTION.NorthSouth ? northSouth[inx] : eastWest[inx]);
        }
        //return format(bc.abs()) + (degreesFMT == DEGREES_FORMAT.Symbol ? "\u00B0" : "");
        return sb.toString();

        
    }
    
    /**
     * Given a single character string  should the direction be negative.
     * @param direction the string
     * @return true negative, false positive
     */
    protected static boolean isNegative(final String direction)
    {
        return direction.equals("S") || direction.equals("W");
    }
    
    
    /**
     * Converts Decmal Degrees to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDDDToDDDD(final String str)
    {
        return new BigDecimal(str);
    }
    
    /**
     * Converts Decmal Degrees to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @param direction the direction
     * @return a BigDecimal
     */
    public static BigDecimal convertDDDDToDDDD(final String str, final String direction)
    {
        BigDecimal bd = new BigDecimal(str);
        if (isNegative(direction))
        {
            return bd.multiply(minusOne);
        }
        return bd;
    }
    
    /**
     * Converts Degrees, Minutes and Decimal Seconds to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMSSToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str);
        double p0 =  Double.parseDouble(parts[0]);
        boolean neg = false;
        if (p0 < 0)
        {
            p0 = p0*-1;
            neg = true;
        }
        double p1 =  Double.parseDouble(parts[1]);
        double p2 =  Double.parseDouble(parts[2]);

        BigDecimal val = new BigDecimal(p0 + ((p1 + (p2 / 60.0)) / 60.0));
        if (neg)
        {
            val = val.multiply(minusOne);
        }
        return val;
    }
    
    /**
     * Converts Degrees, Minutes and Decimal Seconds to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @param direction the direction
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMSSToDDDD(final String str, final String direction)
    {
        BigDecimal bd = convertDDMMSSToDDDD(str);

        if (isNegative(direction))
        {
            return bd.multiply(minusOne);
        }
        return bd;
    }
    
    /**
     * Converts Degrees decimal Minutes to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMMMToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str);
        
        
        double p0 =  Double.parseDouble(parts[0]);
        boolean neg = false;
        if (p0 < 0)
        {
            p0 = p0*-1;
            neg = true;
        }
        double p1 =  Double.parseDouble(parts[1]);

        BigDecimal val = new BigDecimal(p0 + (p1 / 60.0));

        if (neg)
        {
            val = val.multiply(minusOne);
        }
        return val;
    }
    
    /**
     * Converts Degrees decimal Minutes to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @param direction the direction
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMMMToDDDD(final String str, final String direction)
    {
        BigDecimal bd = convertDDMMMMToDDDD(str);
        if (isNegative(direction))
        {
            return bd.multiply(minusOne);
        }
        return bd;
    }
    
    /**
     * Strinps any zeros at end of string, but will append a zero if string would end in a decimal.
     * @param str the string to be converted
     * @return the new string
     */
    public static String stripZeroes(final String str)
    {
        if (str.indexOf('.') == -1)
        {
            return str;
            
        }
        // else
        String newStr = StringUtils.strip(str, "0");
        if (newStr.endsWith("."))
        {
            return newStr + "0";
        }
        return newStr;
    }
    
    /**
     * Returns a formatted string using the class level formatter and then strips any extra zeroes.
     * @param bd the BigDecimal to be formatted.
     * @return the formatted string
     */
    public static String format(final BigDecimal bd)
    {
        return stripZeroes(decFormatter.format(bd));
    }

    
    /**
     * Converts a Lat/Lon BigDecimal to a String
     * @param value the value
     * @param latOrLon whether it is a latitude or longitude
     * @param format the format to use
     * @param degreesFMT indicates whether to use a symbol or append single character text representation of the direction ('N', 'S', 'E', 'W")
     * @return string of the value
     */
    public static String format(final BigDecimal value, 
                                final LATLON latOrLon, 
                                final FORMAT format,
                                final DEGREES_FORMAT degreesFMT)
    {
        DIRECTION dir = latOrLon == LATLON.Latitude ? DIRECTION.NorthSouth : DIRECTION.EastWest;
        switch (format)
        {
            case DDDDDD:
                return convertToDDDDDD(value, degreesFMT, dir);
                
            case DDMMMM:
                return convertToDDMMMM(value, degreesFMT, dir);
                
            case DDMMSS: 
                return convertToDDMMSS(value, degreesFMT, dir);
        }
        return "";
    }
    
    public static BigDecimal convertDirectionalDDMMSSToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str);
        double p0 =  Double.parseDouble(parts[0]);
        double p1 =  Double.parseDouble(parts[1]);
        double p2 =  Double.parseDouble(parts[2]);
        String dir = parts[3].substring(0, 1);

        BigDecimal val = new BigDecimal(p0 + ((p1 + (p2 / 60.0)) / 60.0));

        if ( isNegative(dir) )
        {
            val = val.multiply(minusOne);
        }

        return val;
    }

    public static BigDecimal convertDirectionalDDMMMMToDDDD(final String dm)
    {
        String[] parts = StringUtils.split(dm);
        double p0 =  Double.parseDouble(parts[0]);
        double p1 =  Double.parseDouble(parts[1]);
        String dir = parts[2].substring(0, 1);

        BigDecimal val = new BigDecimal(p0 + (p1 / 60.0));

        if ( isNegative(dir) )
        {
            val = val.multiply(minusOne);
        }

        return val;
    }

    public static BigDecimal convertDirectionalDDDDToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str);
        double p0  = Double.parseDouble(parts[0]);
        String dir = parts[1].substring(0, 1);
        
        BigDecimal val = new BigDecimal(p0);

        if ( isNegative(dir) )
        {
            val = val.multiply(minusOne);
        }

        return val;
    }
}
