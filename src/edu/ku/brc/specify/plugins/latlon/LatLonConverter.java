/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;

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
    private static boolean useDB = false;
    public static DecimalFormat decFormatter = new DecimalFormat("#0.0000000000#");
    
    protected static DecimalFormat decFormatter2 = new DecimalFormat("#0");
    protected static BigDecimal    one = new BigDecimal("1.0");
    protected static BigDecimal    sixty = new BigDecimal("60.0");

    
    /**
     * Converts BigDecimal to Degrees, Minutes and Deimal Seconds.
     * @param bc the DigDeimal to be converted.
     * @return a 3 piece string
     */
    public static String convertToDDMMSS(final BigDecimal bc)
    {
        if (useDB)
        {
            BigDecimal remainder = bc.remainder(one);
          
            BigDecimal num = bc.subtract(remainder);
            
            BigDecimal minutes         = new BigDecimal(remainder.multiply(sixty).abs().intValue());
            BigDecimal secondsFraction = remainder.abs().multiply(sixty).subtract(minutes);                  
            BigDecimal seconds         = secondsFraction.multiply(sixty);
            
            //System.out.println("["+decFormatter2.format(num)+"]["+minutes+"]["+seconds+"]");
            
            return decFormatter2.format(num) + " " + minutes + " " + seconds;
            
        } else
        {
            double num       = Math.abs(bc.doubleValue());
            int    whole     = (int)Math.floor(num);
            double remainder = num - whole;
            
            double minutes      = remainder * 60.0;
            int    minutesWhole = (int)Math.floor(minutes);
            double secondsFraction = minutes - minutesWhole;
            double seconds = secondsFraction * 60.0;
            
            return whole + " " + minutesWhole + " " + StringUtils.strip(String.format("%12.10f", new Object[] {seconds}), "0");
        }
    }
    
    /**
     * Converts BigDecimal to Degrees and Deimal Minutes.
     * @param bc the DigDeimal to be converted.
     * @return a 2 piece string
     */
    public static String convertToDDMMMM(final BigDecimal bc)
    {
        if (useDB)
        {
            BigDecimal remainder = bc.remainder(one);
          
            BigDecimal num = bc.subtract(remainder);
            
            BigDecimal minutes = remainder.multiply(sixty).abs();
            
            //System.out.println("["+decFormatter2.format(num)+"]["+minutes+"]");
            return decFormatter2.format(num) + " " + minutes;
            
        } else
        {
            double num       = Math.abs(bc.doubleValue());
            int    whole     = (int)Math.floor(num);
            double remainder = num - whole;
            
            double minutes = remainder * 60.0;
            //System.out.println("["+whole+"]["+String.format("%10.10f", new Object[] {minutes})+"]");
            return whole + " " + StringUtils.strip(String.format("%10.10f", new Object[] {minutes}), "0");
        }
        
    }
    
    /**
     * Converts Degrees, Minutes and Deimal Seconds to BigDecimal.
     * @param bc the DigDeimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMSSToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str);
        double p0 =  Double.parseDouble(parts[0]);
        double p1 =  Double.parseDouble(parts[1]);
        double p2 =  Double.parseDouble(parts[2]);

        BigDecimal val = new BigDecimal(p0 + ((p1 + (p2 / 60.0)) / 60.0));
        return val;
    }
    
    /**
     * Converts Degrees decimal Minutes to BigDecimal.
     * @param bc the DigDeimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMMMToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str);
        
        
        double p0 =  Double.parseDouble(parts[0]);
        double p1 =  Double.parseDouble(parts[1]);

        BigDecimal val = new BigDecimal(p0 + (p1 / 60.0));

        return val;
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

    
    public static void main(String[] args)
    {
        
        // -95.30248, 38.954080
        //convertToDDMMSS(new BigDecimal("38.954020"));
        //convertToDDMMSS(new BigDecimal("-95.30248"));
        
        //convertToDDMMMM(new BigDecimal("38.954020"));
        
        //DecimalFormat decFormatter = new DecimalFormat("#0.0000000000#");
        
        //System.out.println(decFormatter.format(convertDDMMSSToDDDD("38 57 14.472")));
        //System.out.println(decFormatter.format(convertDDMMMMToDDDD("38 57.2412")));
        
        BigDecimal start = new BigDecimal("38.95402");
        String str = convertToDDMMMM(start);
        BigDecimal end =  convertDDMMMMToDDDD(str);
        
        System.out.println("["+str+"]["+start.doubleValue()+"]["+end.doubleValue()+"] ["+(start.doubleValue() == end.doubleValue() ? "EQUALS" : "NOT EQUAL")+"]");
        
        start = new BigDecimal("38.95402");
        str   = convertToDDMMSS(start);
        end   =  convertDDMMSSToDDDD(str);
        
        System.out.println("["+str+"]["+start.doubleValue()+"]["+end.doubleValue()+"] ["+(start.doubleValue() == end.doubleValue() ? "EQUALS" : "NOT EQUAL")+"]");
    }
}
