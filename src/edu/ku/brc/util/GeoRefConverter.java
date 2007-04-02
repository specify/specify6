package edu.ku.brc.util;

import java.math.BigDecimal;

public class GeoRefConverter implements StringConverter
{
    public enum GeoRefFormat
    {
        DMS_PLUS_MINUS ("[\\+\\-]?\\d{1,3}\\s\\d{1,2}\\s\\d{1,2}\\.\\d{0,}\\s*")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDDMMSSToDDDD(orig);
            }
        },
        DM_PLUS_MINUS  ("[\\+\\-]?\\d{0,3}\\s\\d{1,2}\\.\\d{0,}\\s*")
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
    public String convert(String original, String destFormat) throws Exception
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
        
        // if we weren't able to find a matching format, throw an exception
        if (degreesPlusMinus == null)
        {
            throw new Exception("Cannot find matching input format");
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
     * @throws Exception 
     */
    public static void main(String[] args)
    {
        String destFormat = GeoRefFormat.DMS_PLUS_MINUS.name();
        
        String[] inputStrings = new String[] {
                "0 0 0",
                "0 0 0.",
                "-32 45 16.8232",
                "-32 45 16.82",
                "-32 45 6.82",
                "-32 45 0.82",
                "-32 45 .82",
                "-132 45 16.82151",
                "-132 45 6.82",
                "-132 45 .82",
                "32 45 16.82",
                "32 45 16.82",
                "32 45 6.82",
                "32 45 0.82",
                "32 45 .82",
                "132 45 16.82",
                "132 45 6.82",
                "132 45 .82",
                "32 45 16.8232 S",
                "32 45 16.82 S",
                "32 45 6.82 S",
                "32 45 .82 S",
                "132 45 16.82151 W",
                "132 45 6.82 W",
                "132 45 .82 W",
                "32 45 16.82",
                "32 45 16.82",
                "32 45 6.82",
                "32 45 .82",
                "132 45 16.82",
                "132 45 6.82",
                "132 45 .82",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
        };

        for (String input: inputStrings)
        {
            System.out.println("Input:             " + input);
            BigDecimal degreesPlusMinus = null;
            for (GeoRefFormat format: GeoRefFormat.values())
            {
                if (input.matches(format.regex))
                {
                    System.out.println("Format match:      " + format.name());
                    degreesPlusMinus = format.convertToDecimalDegrees(input);
                    break;
                }
            }
            
            // if we weren't able to find a matching format, throw an exception
            if (degreesPlusMinus == null)
            {
                System.out.println("No matching format found");
                System.out.println("----------------------------------");
                continue;
            }
            
            String convertedVal = null;
            if (destFormat == GeoRefFormat.DMS_PLUS_MINUS.name())
            {
                convertedVal = LatLonConverter.convertToSignedDDMMSS(degreesPlusMinus);
            }
            else if (destFormat == GeoRefFormat.DM_PLUS_MINUS.name())
            {
                convertedVal = LatLonConverter.convertToSignedDDMMMM(degreesPlusMinus);
            }
            else if (destFormat == GeoRefFormat.D_PLUS_MINUS.name())
            {
                convertedVal = LatLonConverter.convertToSignedDDDDDD(degreesPlusMinus);
            }
            
            System.out.println("Converted value:   " + convertedVal);
            System.out.println("----------------------------------");
        }
    }
}
