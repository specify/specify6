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
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 10, 2007
 *
 */
public class LatLonConverter
{
    public static DecimalFormat decFormatter = new DecimalFormat("#0.0000000000#");
    
    protected static DecimalFormat decFormatter2 = new DecimalFormat("#0");
    protected static BigDecimal    one = new BigDecimal("1.0");
    protected static BigDecimal    sixty = new BigDecimal("60.0");

    
    public static String convertToDDMMSS(final BigDecimal bc)
    {
        boolean useDB = false;
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
            
            //System.out.println("["+whole+"]["+String.format("%10.10f", new Object[] {minutes})+"]");
            return whole + " " + minutesWhole + " " + StringUtils.strip(String.format("%12.10f", new Object[] {seconds}), "0");
        }
    }
    
    public static String convertToDDMMMM(final BigDecimal bc)
    {
        
        boolean useDB = false;
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
    
    public static BigDecimal convertDDMMSSToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str);
        double p0 =  Double.parseDouble(parts[0]);
        double p1 =  Double.parseDouble(parts[1]);
        double p2 =  Double.parseDouble(parts[2]);

        BigDecimal val = new BigDecimal(p0 + ((p1 + (p2 / 60.0)) / 60.0));
        return val;
    }
    
    public static BigDecimal convertDDMMMMToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str);
        
        
        double p0 =  Double.parseDouble(parts[0]);
        double p1 =  Double.parseDouble(parts[1]);

        BigDecimal val = new BigDecimal(p0 + (p1 / 60.0));

        return val;
        
        /*
        String[] parts = StringUtils.split(str);
        BigDecimal p0 = new BigDecimal(parts[0]);
        BigDecimal p1 = new BigDecimal(parts[1]);
        BigDecimal p2 = new BigDecimal(parts[2], );
        
        BigDecimal x = p1.divide(sixty);
        
        p0 = p0.add(p1.divide(sixty));
        p0 = p0.add(p2.divide(sixty));
        return p0;*/
    }
    
    public static String format(final BigDecimal bd)
    {
        return StringUtils.strip(decFormatter.format(bd), "0");
    }
    
    protected static String strip(final String valStr)
    {
        String str = StringUtils.strip(valStr, "0");
        return StringUtils.strip(str, ".");
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
