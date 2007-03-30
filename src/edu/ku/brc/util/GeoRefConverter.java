package edu.ku.brc.util;

import java.math.BigDecimal;
import java.util.Vector;

public class GeoRefConverter implements StringConverter
{
    public enum GeoRefFormat
    {
        DMS_PLUS_MINUS ("[\\+\\-]?\\d{1,3}\\s\\d{1,2}\\s\\d{2}\\.\\d{0,}\\s*")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDDMMSSToDDDD(orig);
            }
        },
        DM_PLUS_MINUS  ("[\\+\\-]?\\d{1,3}\\s\\d{1,2}\\.\\d{0,}\\s*")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDDMMMMToDDDD(orig);
            }
        },
        D_PLUS_MINUS   ("[\\+\\-]?\\d{1,3}\\.\\d{0,}\\s*")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDDDDToDDDD(orig);
            }
        },
        DMS_NSEW       ("\\d{1,3}\\s\\d{2}\\s\\d{1,2}\\.\\d{0,}\\s[NSEW]{1}.*")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDirectionalDDMMSSToDDDD(orig);
            }
        },
        DM_NSEW        ("\\d{1,3}\\s\\d{1,2}\\.\\d{0,}\\s[NSEW]{1}.*")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDirectionalDDMMMMToDDDD(orig);
            }
        },
        D_NSEW         ("\\d{1,3}\\.\\d{0,}\\s[NSEW]{1}.*")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDirectionalDDDDToDDDD(orig);
            }
        };
        
        public final String regex;
        
        GeoRefFormat(String regex)
        {
            this.regex = regex;
        }
        
        public boolean matches(String input)
        {
            return input.matches(regex);
        }
        
        public abstract BigDecimal convertToDecimalDegrees(String original);
    }
    
    public GeoRefConverter()
    {
        // nothing to do here
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.util.StringConverter#convert(java.lang.String, java.lang.String)
     */
    public String convert(String original, String destFormat)
    {
        if (original == null)
        {
            return null;
        }
        
        // first we have to 'discover' the original format
        // and convert to decimal degrees
        // then we convert to the requested format

        BigDecimal degreesPlusMinus = null;
        for (GeoRefFormat format: GeoRefFormat.values())
        {
            if (original.matches(format.regex))
            {
                degreesPlusMinus = format.convertToDecimalDegrees(original);
                break;
            }
        }
        
        // if we weren't able to find a matching format, return the original
        if (degreesPlusMinus == null)
        {
            return original;
        }
        
        if (destFormat == GeoRefFormat.DMS_PLUS_MINUS.name())
        {
            return LatLonConverter.convertToSignedDDMMSS(degreesPlusMinus);
        }
        else if (destFormat == GeoRefFormat.DM_PLUS_MINUS.name())
        {
            return LatLonConverter.convertToSignedDDMMMM(degreesPlusMinus);
        }
        else if (destFormat == GeoRefFormat.D_PLUS_MINUS.name())
        {
            return LatLonConverter.convertToSignedDDDDDD(degreesPlusMinus);
        }
        
        return null;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        Vector<String> inputStrings = new Vector<String>();
        inputStrings.add("-32 45 16.82");
        inputStrings.add("-132 45 16.8234");
        inputStrings.add("32 45 16.82 S");
        inputStrings.add("132 45 16.82235");
        inputStrings.add("-32 45.15166");
        inputStrings.add("32 45.16236");
        inputStrings.add("32 45.1616 S");
        inputStrings.add("52 22.6 W");
        inputStrings.add("108.13461");
        inputStrings.add("-20.26");
        inputStrings.add("100.1351 N");
        inputStrings.add("9.15161 W");

        GeoRefConverter converter = new GeoRefConverter();
        
        for (String input: inputStrings)
        {
            System.out.println("Converted output: " + converter.convert(input, GeoRefFormat.D_PLUS_MINUS.name()));
        }
    }
}
