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
package edu.ku.brc.util;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.UIHelper;


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
    protected static DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
    protected static String               decimalSep;
    
    public final  static char UNICODE_DEGREE = 0x00b0;
    
	public final static String DEGREES_SYMBOL = "\u00b0";
    public final static String SEPS           = DEGREES_SYMBOL + ":'\" ";
    
    protected final static int DDDDDD_LEN = 7;
    protected final static int DDMMMM_LEN = 5;
    protected final static int DDMMSS_LEN = 3;
    
    public static int[] DECIMAL_SIZES = {7, 5, 3, 0};
    
    public enum LATLON          {Latitude, Longitude}
    public enum FORMAT          {DDDDDD, DDMMMM, DDMMSS, None} // None must be at the end so the values match Specify 5
    public enum DEGREES_FORMAT  {None, Symbol, String}
    public enum DIRECTION       {None, NorthSouth, EastWest}
    
    private static boolean         useDB           = false;
    
    public static DecimalFormat    decFormatter    = new DecimalFormat("#0.0000000#");
    protected static StringBuffer  zeroes          = new StringBuffer(64);
    
    //patched
    
    protected static BigDecimal    minusOne        = new BigDecimal("-1.0");
    protected static DecimalFormat decFormatter2   = new DecimalFormat("#0");
    protected static BigDecimal    one             = new BigDecimal("1.0");
    protected static BigDecimal    sixty           = new BigDecimal("60.0");
    
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
        
        for (int i=0;i<8;i++)
        {
            zeroes.append("00000000");
        }
        decimalSep = Character.toString(formatSymbols.getDecimalSeparator());
    }
    
    /**
     * 
     */
    private LatLonConverter()
    {
        super();
    }

    /**
     * @param formatInt
     * @return
     */
    public static FORMAT convertIntToFORMAT(final int formatInt)
    {
        switch (formatInt)
        {
            case 0 : return FORMAT.DDDDDD;
            case 1 : return FORMAT.DDMMMM;
            case 2 : return FORMAT.DDMMSS;
            default : return FORMAT.DDDDDD;
        }
    }
    
    /**
     * @param bd
     * @param latLonStr
     * @param type
     * @return
     */
    public static String ensureFormattedString(final BigDecimal bd, 
                                               final String     latLonStr, 
                                               final FORMAT     type,
                                               final LATLON     latOrLon)
    {
        return ensureFormattedString(bd, latLonStr, type, latOrLon, DECIMAL_SIZES[type.ordinal()]);
    }
 
    
    /**
     * @param bd
     * @param latLonStr
     * @param type
     * @return
     */
    public static String ensureFormattedString(final BigDecimal bd, 
                                               final String     latLonStr, 
                                               final FORMAT     type,
                                               final LATLON     latOrLon,
                                               final int        decimalSize)
    {
        if (StringUtils.isEmpty(latLonStr))
        {
            if (bd == null)
            {
                return null;
            }
            
            String outStr = format(bd, latOrLon, type, DEGREES_FORMAT.Symbol, decimalSize);
            if (latOrLon == LATLON.Latitude)
            {
                outStr += " " + northSouth[bd.doubleValue() < 0.0 ? 1 : 0];
            } else
            {
                outStr += " " +  eastWest[bd.doubleValue() < 0.0 ? 1 : 0];
            }
            return outStr;
        }
        return latLonStr;
    }

    /**
     * @param str
     * @param actualFmt
     * @param addSymbols
     * @param inclZeroes
     * @return
     */
    public static LatLonValueInfo adjustLatLonStr(final String  strArg,
                                                  final FORMAT  actualFmt, 
                                                  final boolean addSymbols,
                                                  final boolean inclZeroes, 
                                                  final LATLON latOrLon)
    {
        if (StringUtils.isNotEmpty(strArg))
        {
            String str = strArg;
            
            String[] tokens = breakStringAPart(str);
            boolean startsWithMinus = str.startsWith("-");
            if (startsWithMinus)
            {
                str = str.substring(1);
            }
            
            if (tokens.length == 1)
            {
                if (latOrLon == LATLON.Latitude)
                {
                    str += " " + northSouth[startsWithMinus ? 1 : 0]; 
                } else
                {
                    str += " " + eastWest[startsWithMinus ? 1 : 0]; 
                }
            }
            
            tokens = breakStringAPart(str);
            String   zero   = inclZeroes ? "0" : "";
            //System.err.println("tokens[1] ["+tokens[1]+"]["+str+"]");
            boolean hasDegreesTxt = tokens[1].length() == 3 && tokens[1].equals("deg");
   
            LatLonValueInfo latLonInfo = new LatLonValueInfo(hasDegreesTxt);
            latLonInfo.addPart(tokens[0]);
            
            String dirStr = null;
            FORMAT fmt    = null;
            
            if (actualFmt != null && actualFmt != FORMAT.None)
            {
                switch (actualFmt)
                {
                    case DDDDDD:
                        dirStr = tokens[1];
                        break;
                        
                    case DDMMMM:
                        
                        if (tokens.length == 3)
                        {
                            latLonInfo.addPart(tokens[1]);
                            dirStr = tokens[2];
                        } else
                        {
                            latLonInfo.addPart(zero);
                            dirStr = tokens[1];
                        }
                        
                        break;
                        
                    case DDMMSS:
                        if (tokens.length == 4)
                        {
                            latLonInfo.addPart(tokens[1]);
                            latLonInfo.addPart(tokens[2]);
                            dirStr = tokens[3];
                            
                        } else if (tokens.length == 3)
                        {
                            latLonInfo.addPart(tokens[1]);
                            latLonInfo.addPart(zero);
                            dirStr = tokens[2];
                            
                        } else if (tokens.length == 2)
                        {
                            latLonInfo.addPart(zero);
                            latLonInfo.addPart(zero);
                            dirStr = tokens[1];
                        }
                        break;
                        
                    default:
                        break;
                } // switch 
                fmt = actualFmt;
                
            } else
            {
                switch (tokens.length)
                {
                    case 2 : 
                    {
                        dirStr = tokens[1];
                        fmt = FORMAT.DDDDDD;
                        break;
                    }
                        
                    case 3 : 
                    {
                        if (hasDegreesTxt)
                        {
                            fmt = FORMAT.DDDDDD;
                        } else
                        {
                            latLonInfo.addPart(tokens[1]);
                            fmt = FORMAT.DDMMMM;
                        }
                        dirStr = tokens[2];
                        break;
                    }
                        
                    case 4 :
                    {
                        if (hasDegreesTxt)
                        {
                            fmt = FORMAT.DDMMMM;
                        } else
                        {
                            latLonInfo.addPart(tokens[1]);
                            fmt = FORMAT.DDMMSS;
                        }
                        latLonInfo.addPart(tokens[2]);
                        dirStr = tokens[3];
    
                        break;
                    }
                        
                    case 5 :
                        latLonInfo.addPart(tokens[2]);
                        latLonInfo.addPart(tokens[3]);
                        dirStr = tokens[4];
                        fmt = FORMAT.DDMMSS;
                        break;
                        
                    default:
                        break;
                    
                } // switch
            } // if
            latLonInfo.addPart(dirStr);
            latLonInfo.setFormat(fmt);
            latLonInfo.setDirStr(dirStr);
            return latLonInfo;
                
        }
        return null;
    }
    

    /**
     * @param strArg
     * @param fromFmt
     * @param toFmt
     * @param latOrLon
     * @return
     */
    public static String convert(final String strArg, 
                                 final FORMAT fromFmt, 
                                 final FORMAT toFmt, 
                                 final LATLON latOrLon)
    {
        if (fromFmt == toFmt)
        {
            return strArg;
        }
     
        LatLonValueInfo latLonVal = adjustLatLonStr(strArg, fromFmt, false, true, latOrLon);
        
        String str = latLonVal.getStrVal(false);
        
        if (StringUtils.isNotEmpty(str))
        {
            BigDecimal bd = null;
            switch (fromFmt)
            {
                case DDDDDD:
                    bd = convertDDDDStrToDDDDBD(str);
                    break;
    
                case DDMMMM:
                    bd = convertDDMMMMStrToDDDDBD(str);
                    break;
    
                case DDMMSS:
                    bd = convertDDMMSSStrToDDDDBD(str);
                    break;
                    
                case None:
                    break;
            }
            
            String outStr = "";
            if (bd != null)
            {
                outStr = format(bd, latOrLon, toFmt, DEGREES_FORMAT.Symbol, DECIMAL_SIZES[toFmt.ordinal()]);
                if (StringUtils.isNotEmpty(latLonVal.getDirStr()))
                {
                    outStr += " " + latLonVal.getDirStr();
                }
            }
            return outStr;
        }
        
        return null;
    }
    
    /**
     * Converts BigDecimal to Degrees, Minutes and Decimal Seconds.
     * @param bd the DigDecimal to be converted.
     * @return a 3 piece string
     */
    public static String convertToDDMMSS(final BigDecimal bd,
                                         final int        decimalLen)
    {
        return convertToDDMMSS(bd, DEGREES_FORMAT.None, DIRECTION.None, decimalLen);
    }

    /**
     * @param bd
     * @param decimalLen
     * @return
     */
    public static String convertToSignedDDMMSS(final BigDecimal bd,
    										  final int 		decimalLen)
    {
    	return convertToSignedDDMMSS(bd, decimalLen, DEGREES_FORMAT.None);
    }

    /**
     * @param bd
     * @param decimalLen
     * @param degFmt
     * @return
     */
    public static String convertToSignedDDMMSS(final BigDecimal bd,
                                               final int        decimalLen,
                                               final DEGREES_FORMAT degFmt)
    {
        String sign = "";
        if (bd.compareTo(bd.abs()) < 0)
        {
            sign = "-";
        }
        
        String convertedAbs = convertToDDMMSS(bd, degFmt, DIRECTION.None, decimalLen);
        return sign + convertedAbs;
    }
    
    /**
     * Calculates how many decimal places there is in the string.
     * @param latLonStr the string to be checked
     * @return the number of decimals places
     */
    public static int getDecimalLength(final String latLonStr)
    {
        int decimalFmtLen = 0;
        if (StringUtils.isNotEmpty(latLonStr))
        {
            int decIndex = latLonStr.lastIndexOf('.');
            if (decIndex > -1 && latLonStr.length() > decIndex)
            {
                decimalFmtLen = latLonStr.length() - decIndex;
            }
        }
        return decimalFmtLen;
    }

    /**
     * Converts BigDecimal to Degrees, Minutes and Decimal Seconds.
     * @param bd the DigDecimal to be converted.
     * @return a 3 piece string
     */
    public static String convertToDDMMSS(final BigDecimal     bd, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction,
                                         final int            decimalLen)
    {
    	return convertToDDMMSS(bd, degreesFMT, direction, decimalLen, false);
    }

    /**
     * Converts BigDecimal to Degrees, Minutes and Decimal Seconds.
     * @param bd the DigDecimal to be converted.
     * @return a 3 piece string
     */
    public static String convertToDDMMSS(final BigDecimal     bd, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction,
                                         final int            decimalLen,
                                         final boolean alwaysIncludeDir)
    {
        
        if (bd.doubleValue() == 0.0)
        {
            return "0." + zeroes.substring(0, decimalLen);
        }
        
        if (useDB)
        {
            BigDecimal remainder = bd.remainder(one);
          
            BigDecimal num = bd.subtract(remainder);
            
            BigDecimal minutes         = new BigDecimal(remainder.multiply(sixty).abs().intValue());
            BigDecimal secondsFraction = remainder.abs().multiply(sixty).subtract(minutes);                  
            BigDecimal seconds         = secondsFraction.multiply(sixty);
            
            //System.out.println("["+decFormatter2.format(num)+"]["+minutes+"]["+seconds+"]");
            
            return decFormatter2.format(num) + " " + decFormatter2.format(minutes) + " " + decFormatter.format(seconds);
            
        }
        //else
        
        double num       = Math.abs(bd.doubleValue());
        int    whole     = (int)Math.floor(num);
        double remainder = num - whole;
        
        double minutes      = remainder * 60.0;
        int    minutesWhole = (int)Math.floor(minutes);
        double secondsFraction = minutes - minutesWhole;
        double seconds = secondsFraction * 60.0;
        
        boolean addMinSecsSyms = degreesFMT != DEGREES_FORMAT.None;
        
        if (minutesWhole == 60)
        {
            whole += 1;
            minutesWhole = 0;
        }
        
        // round to 2 decimal places precision
        seconds = Math.round(seconds * 1000) / 1000.0;
        
        int secondsWhole = (int)Math.floor(seconds);
        if (secondsWhole == 60)
        {
            minutesWhole += 1;
            seconds = 0.0;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(whole);
        if (degreesFMT == DEGREES_FORMAT.Symbol)
        {
            sb.append(DEGREES_SYMBOL);
        }        
        sb.append(' ');
        sb.append(minutesWhole);
        if (addMinSecsSyms) sb.append("'");
        sb.append(' ');
        
        sb.append(String.format("%2."+decimalLen+"f", seconds));
        if (addMinSecsSyms) sb.append("\"");
        
        if (degreesFMT == DEGREES_FORMAT.String || alwaysIncludeDir)
        {
            int inx = bd.doubleValue() < 0.0 ? 1 : 0;
            if (direction != DIRECTION.None)
            {
            	sb.append(' ');
            	sb.append(direction == DIRECTION.NorthSouth ? northSouth[inx] : eastWest[inx]);
            }
        }
        //System.err.println("["+sb.toString()+"]");
        //return whole + (DEGREES_FORMAT.None ? "\u00B0" : "") + " " + minutesWhole + " " + StringUtils.strip(String.format("%12.10f", new Object[] {seconds}), "0");
        return sb.toString();
    }
    
    /**
     * Converts BigDecimal to Degrees and Decimal Minutes.
     * @param bd the DigDecimal to be converted.
     * @return a 2 piece string
     */
    public static String convertToDDMMMM(final BigDecimal bd,
                                         final int        decimalLen)
    {
        return convertToDDMMMM(bd, DEGREES_FORMAT.None, DIRECTION.None, decimalLen);
    }

    /**
     * @param dd
     * @param decimalLen
     * @return
     */
    public static String convertToSignedDDMMMM(final BigDecimal dd,
            final int        decimalLen)
    {
    	return convertToSignedDDMMMM(dd, decimalLen, DEGREES_FORMAT.None);
    }

    /**
     * @param dd
     * @param decimalLen
     * @param degFmt
     * @return
     */
    public static String convertToSignedDDMMMM(final BigDecimal dd,
                                               final int        decimalLen,
                                               final DEGREES_FORMAT degFmt)
    {
        String sign = "";
        if (dd.compareTo(dd.abs()) < 0)
        {
            sign = "-";
        }
        
        String convertedAbs = convertToDDMMMM(dd, degFmt, DIRECTION.None, decimalLen);
        return sign + convertedAbs;
    }

    /**
     * Converts BigDecimal to Degrees and Decimal Minutes.
     * @param bd the DigDecimal to be converted.
     * @return a 2 piece string
     */
    public static String convertToDDMMMM(final BigDecimal     bd, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction,
                                         final int            decimalLen)
    {
    	return convertToDDMMMM(bd, degreesFMT, direction, decimalLen, false);
    }
    
    /**
     * Converts BigDecimal to Degrees and Decimal Minutes.
     * @param bd the DigDecimal to be converted.
     * @return a 2 piece string
     */
    public static String convertToDDMMMM(final BigDecimal     bd, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction,
                                         final int            decimalLen,
                                         final boolean alwaysIncludeDir)
    {
        if (bd.doubleValue() == 0.0)
        {
            return "0.0";
        }
        
        if (useDB)
        {
            BigDecimal remainder = bd.remainder(one);
          
            BigDecimal num = bd.subtract(remainder);
            
            BigDecimal minutes = remainder.multiply(sixty).abs();
            
            //System.out.println("["+decFormatter2.format(num)+"]["+minutes+"]");
            return decFormatter2.format(num) + " " + decFormatter2.format(minutes);
            
        }
        //else
        
        boolean addMinSecsSyms = degreesFMT != DEGREES_FORMAT.None;   
        
        double num       = Math.abs(bd.doubleValue());
        int    whole     = (int)Math.floor(num);
        double remainder = num - whole;
        
        double minutes = remainder * 60.0;
        //System.out.println("["+whole+"]["+String.format("%10.10f", new Object[] {minutes})+"]");
        
        StringBuilder sb = new StringBuilder();
        sb.append(whole);
        if (degreesFMT == DEGREES_FORMAT.Symbol)
        {
            sb.append("\u00B0");
        }
        sb.append(' ');
        
        // round to four decimal places of precision
        //minutes = Math.round(minutes*10000) / 10000;
        
        sb.append(String.format("%"+2+"."+decimalLen+"f", minutes));
        if (addMinSecsSyms) sb.append("'");
        
        if (degreesFMT == DEGREES_FORMAT.String || alwaysIncludeDir)
        {
            int inx = bd.doubleValue() < 0.0 ? 1 : 0;
            if (direction != DIRECTION.None)
            {
            	sb.append(' ');
            	sb.append(direction == DIRECTION.NorthSouth ? northSouth[inx] : eastWest[inx]);
            }
        }
        //return whole + (degreesFMT == DEGREES_FORMAT.Symbol ? "\u00B0" : "") + " " + StringUtils.strip(String.format("%10.10f", new Object[] {minutes}), "0");
        return sb.toString();
        
    }
        
    /**
     * Converts BigDecimal to Decimal Degrees.
     * @param bd the DigDecimal to be converted.
     * @return a 1 piece string
     */
    public static String convertToDDDDDD(final BigDecimal bd,
                                         final int        decimalLen)
    {
        return convertToDDDDDD(bd, DEGREES_FORMAT.String, DIRECTION.None, decimalLen);
    }

    /**
     * @param dd
     * @param decimalLen
     * @return
     */
    public static String convertToSignedDDDDDD(final BigDecimal dd,
            								   final int        decimalLen)
    {
    	return convertToSignedDDDDDD(dd, decimalLen, DEGREES_FORMAT.None);
    }

    /**
     * @param dd
     * @param decimalLen
     * @param detFmt
     * @return
     */
    public static String convertToSignedDDDDDD(final BigDecimal     dd,
                                               final int            decimalLen,
                                               final DEGREES_FORMAT degFmt)
    {
        String sign = "";
        if (dd.compareTo(dd.abs()) < 0)
        {
            sign = "-";
        }
        
        String convertedAbs = convertToDDDDDD(dd, degFmt, DIRECTION.None, decimalLen);
        return sign + convertedAbs;
    }
 
    
    /**
     * Converts BigDecimal to Decimal Degrees.
     * @param bd the DigDecimal to be converted.
     * @param degreesFMT indicates whether to include the degrees symbol
     * @return a 1 piece string
     */
    public static String convertToDDDDDD(final BigDecimal     bd, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction,
                                         final int            decimalLen)
    {
    	return convertToDDDDDD(bd, degreesFMT, direction, decimalLen, false);
    }
    
    /**
     * Converts BigDecimal to Decimal Degrees.
     * @param bd the DigDecimal to be converted.
     * @param degreesFMT indicates whether to include the degrees symbol
     * @return a 1 piece string
     */
    public static String convertToDDDDDD(final BigDecimal     bd, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction,
                                         final int            decimalLen,
                                         final boolean        alwaysIncludeDir)
    {
        if (bd == null || bd.doubleValue() == 0.0)
        {
            return "0.0";
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("%"+decimalLen+"."+decimalLen+"f", bd.abs()));
        
        if (degreesFMT == DEGREES_FORMAT.Symbol)
        {
            sb.append("\u00B0");
            
        } 
        if (degreesFMT == DEGREES_FORMAT.String || alwaysIncludeDir)
        {
            int inx = bd.doubleValue() < 0.0 ? 1 : 0;
            if (direction != DIRECTION.None)
            {
                sb.append(' ');
            	sb.append(direction == DIRECTION.NorthSouth ? northSouth[inx] : eastWest[inx]);
            }
        }
        //return format(bd.abs()) + (degreesFMT == DEGREES_FORMAT.Symbol ? "\u00B0" : "");
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
     * @param bd the DigDecimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDDDStrToDDDDBD(final String str)
    {
        String withoutDegSign = StringUtils.chomp(str, "°");
        //above doesn't always work for Windows/Mac so try again...
        withoutDegSign = StringUtils.chomp(withoutDegSign, "�");
        //apparently need to do this on mac
        withoutDegSign =  StringUtils.remove(withoutDegSign, UNICODE_DEGREE);
        
        String val = StringUtils.replace(StringUtils.replace(withoutDegSign, decimalSep, ""), "-", "");
        return StringUtils.isNumeric(val) ? UIHelper.parseDoubleToBigDecimal(withoutDegSign) : null;
    }
    
    /**
     * Converts Decimal Degrees to BigDecimal.
     * @param bd the DigDecimal to be converted.
     * @param direction the direction
     * @return a BigDecimal
     */
    public static BigDecimal convertDDDDStrToDDDDBD(final String str, final String direction)
    {
        String val = StringUtils.replace(StringUtils.replace(str, decimalSep, ""), "-", "");
        if (!StringUtils.isNumeric(val))
        {
            return null;
        }
        
        BigDecimal bd = UIHelper.parseDoubleToBigDecimal(str);
        if (isNegative(direction))
        {
            return bd.multiply(minusOne);
        }
        return bd;
    }
    
    /**
     * Converts Degrees, Minutes and Decimal Seconds into it's string pieces.
     * @param bd the DigDecimal to be converted.
     * @return a BigDecimal
     */
    public static String[] breakStringAPart(final String str)
    {
        return StringUtils.split(str, SEPS);
    }
    
    /**
     * Converts Degrees, Minutes and Decimal Seconds to BigDecimal.
     * @param bd the DigDecimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMSSStrToDDDDBD(final String str)
    {
        String[] parts = StringUtils.split(str," d°'\"" + DEGREES_SYMBOL);
        if (parts.length != 3)
        {
            return null;
        }
        
        double p0 =  UIHelper.parseDouble(parts[0]);
        boolean neg = false;
        if (p0 < 0)
        {
            p0 = p0*-1;
            neg = true;
        }
        double p1 =  UIHelper.parseDouble(parts[1]);
        double p2 =  UIHelper.parseDouble(parts[2]);

        BigDecimal val = new BigDecimal(p0 + ((p1 + (p2 / 60.0)) / 60.0));
        if (neg)
        {
            val = val.multiply(minusOne);
        }
        return val;
    }
    
    /**
     * Converts Degrees, Minutes and Decimal Seconds to BigDecimal.
     * @param bd the DigDecimal to be converted.
     * @param direction the direction
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMSSStrToDDDDBD(final String str, final String direction)
    {
        BigDecimal bd = convertDDMMSSStrToDDDDBD(str);

        if (isNegative(direction))
        {
            return bd.multiply(minusOne);
        }
        return bd;
    }
    
    /**
     * Converts Degrees decimal Minutes to BigDecimal.
     * @param bd the DigDecimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMMMStrToDDDDBD(final String str)
    {
        String[] parts = StringUtils.split(str, " d°'\"" + DEGREES_SYMBOL);
        
        double p0 =  UIHelper.parseDouble(parts[0]);
        boolean neg = false;
        if (p0 < 0)
        {
            p0 = p0*-1;
            neg = true;
        }
        double p1 =  UIHelper.parseDouble(parts[1]);

        BigDecimal val = new BigDecimal(p0 + (p1 / 60.0));

        if (neg)
        {
            val = val.multiply(minusOne);
        }
        return val;
    }
    
    /**
     * Converts Degrees decimal Minutes to BigDecimal.
     * @param bd the DigDecimal to be converted.
     * @param direction the direction
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMMMStrToDDDDBD(final String str, final String direction)
    {
        BigDecimal bd = convertDDMMMMStrToDDDDBD(str);
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
        String newStr = StringUtils.stripEnd(str, "0");
        if (newStr.endsWith(decimalSep))
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
        String formatted = decFormatter.format(bd);
        return stripZeroes(formatted);
    }

    
    /**
     * Converts a Lat/Lon BigDecimal to a String
     * @param value the value
     * @param latOrLon whether it is a latitude or longitude
     * @param format the format to use
     * @param degreesFMT indicates whether to use a symbol or append single character text representation of the direction ('N', 'S', 'E', 'W")
     * @return string of the value
     */
    public static String format(final BigDecimal     value, 
                                final LATLON         latOrLon, 
                                final FORMAT         format,
                                final DEGREES_FORMAT degreesFMT,
                                final int            decimalLen)
    {
        DIRECTION dir = latOrLon == LATLON.Latitude ? DIRECTION.NorthSouth : DIRECTION.EastWest;
        switch (format)
        {
            case DDDDDD:
                return convertToDDDDDD(value, degreesFMT, dir, decimalLen);
                
            case DDMMMM:
                return convertToDDMMMM(value, degreesFMT, dir, decimalLen);
                
            case DDMMSS: 
                return convertToDDMMSS(value, degreesFMT, dir, decimalLen);
                
            case None:
                break;
        }
        return "";
    }
    
    /**
     * @param str
     * @return
     */
    public static BigDecimal convertDirectionalDDMMSSToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str," d°'\"" + DEGREES_SYMBOL);
        
        String dir = null;
        if (parts.length < 4)
        {
            parts[2] = parts[2].replaceAll("[NSEW]", "");
            
            int beginIndex = str.indexOf(parts[2]) + parts[2].length();
            dir = str.substring(beginIndex, beginIndex + 1);
        }
        else
        {
            dir = parts[3].substring(0, 1);
        }
        
        double p0 =  UIHelper.parseDouble(parts[0]);
        double p1 =  UIHelper.parseDouble(parts[1]);
        double p2 =  UIHelper.parseDouble(parts[2]);

        BigDecimal val = new BigDecimal(p0 + ((p1 + (p2 / 60.0)) / 60.0));

        if ( isNegative(dir) )
        {
            val = val.multiply(minusOne);
        }

        return val;
    }

    /**
     * @param dm
     * @return
     */
    public static BigDecimal convertDirectionalDDMMMMToDDDD(final String dm)
    {
        String[] parts = StringUtils.split(dm," d°'\"" + DEGREES_SYMBOL);
        
        String dir = null;
        if (parts.length < 3)
        {
            parts[1] = parts[1].replaceAll("[NSEW]", "");
            
            int beginIndex = dm.indexOf(parts[1]) + parts[1].length();
            dir = dm.substring(beginIndex, beginIndex + 1);
        }
        else
        {
            dir = parts[2].substring(0, 1);
        }
        
        double p0 =  UIHelper.parseDouble(parts[0]);
        double p1 =  UIHelper.parseDouble(parts[1]);

        BigDecimal val = new BigDecimal(p0 + (p1 / 60.0));

        if ( isNegative(dir) )
        {
            val = val.multiply(minusOne);
        }

        return val;
    }

    /**
     * @param str
     * @return
     */
    public static BigDecimal convertDirectionalDDDDToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str," d°'\"" + DEGREES_SYMBOL);
        
        String dir = null;
        if (parts.length < 2)
        {
            parts[0] = parts[0].replaceAll("[NSEW]", "");
            
            int beginIndex = str.indexOf(parts[0]) + parts[0].length();
            dir = str.substring(beginIndex, beginIndex + 1);
        }
        else
        {
            dir = parts[1].substring(0, 1);
        }
        
        double p0  = UIHelper.parseDouble(parts[0]);
        
        BigDecimal val = new BigDecimal(p0);

        if ( isNegative(dir) )
        {
            val = val.multiply(minusOne);
        }

        return val;
    }
    
}
