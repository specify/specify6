package edu.ku.brc.util;

import java.math.BigDecimal;

public class GeoRefConverter implements StringConverter
{
    public static enum GeoRefFormat
    {
        DMS_PLUS_MINUS ("[\\+\\-]?\\d{1,3}[\\sd°]\\s?\\d{1,2}[\\s']\\s?\\d{1,2}(\\.\\d{0,}\\s*)?\"?")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDDMMSSToDDDD(orig);
            }
        },
        DM_PLUS_MINUS  ("[\\+\\-]?\\d{1,3}[\\sd°]\\s?\\d{1,2}(\\.\\d{0,}\\s*)?'?")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDDMMMMToDDDD(orig);
            }
        },
        D_PLUS_MINUS   ("[\\+\\-]?\\d{1,3}(\\.\\d{0,}\\s*)?[d°]?")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDDDDToDDDD(orig);
            }
        },
        DMS_NSEW       ("\\d{1,3}[\\sd°]\\s?\\d{1,2}[\\s']\\s?\\d{1,2}(\\.\\d{0,})?\"?\\s?[NSEW]{1}.*")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDirectionalDDMMSSToDDDD(orig);
            }
        },
        DM_NSEW        ("\\d{1,3}[\\sd°]\\s?\\d{1,2}(\\.\\d{0,})?'?\\s?[NSEW]{1}.*")
        {
            @Override
            public BigDecimal convertToDecimalDegrees(String orig)
            {
                return LatLonConverter.convertDirectionalDDMMMMToDDDD(orig);
            }
        },
        D_NSEW         ("\\d{1,3}(\\.\\d{0,})?[d°]?\\s?[NSEW]{1}.*")
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
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        String destFormat = GeoRefFormat.DMS_PLUS_MINUS.name();
        
        String[] inputStrings = new String[] {
                
                // +/- Deg Min Sec
                "//+/- Deg Min Sec",
                "0 0 0",
                "0 0 0.",
                "-32 45 16.8232",
                "-32d 45' 16.8232\"",
                "-32d45'16.8232\"",
                "-32°45'16.8232\"",
                "-32° 45' 16.82\"",
                "-32 45 16.82",
                "-32 45 6.8232",
                "-32 45 6.82",
                "-32 45 0.82",
                "-132 45 16.82151",
                "-132 45 6.82",
                "32 45 16.8232",
                "32 45 16.82",
                "32 45 6.8232",
                "32 45 6.82",
                "32 45 0.82",
                "132 45 16.82151",
                "132 45 6.82",
                
                // Deg Min Sec N/S/E/W
                "//Deg Min Sec N/S/E/W",
                "32 45 16.8232 N",
                "32 45 16.82 N",
                "32d45'16.82\" N",
                "32d45'16.82\"N",
                "32d 45' 16.82\" N",
                "32° 45' 16.82\" N",
                "32 45 16.82 N",
                "32 45 6.8232 N",
                "32 45 6.82 N",
                "32 45 0.82 N",
                "132 45 16.82151 N",
                "132 45 6.82 N",
                
                "32 45 16.8232 S",
                "32 45 16.82 S",
                "32 45 6.8232 S",
                "32 45 6.82 S",
                "32 45 0.82 S",
                "132 45 16.82151 S",
                "132 45 6.82 S",
                
                "32 45 16.8232 E",
                "32 45 16.82 E",
                "32 45 6.8232 E",
                "32 45 6.82 E",
                "32 45 0.82 E",
                "132 45 16.82151 E",
                "132 45 6.82 E",
                
                "32 45 16.8232 W",
                "32 45 16.82 W",
                "32 45 6.8232 W",
                "32 45 6.82 W",
                "32 45 0.82 W",
                "132 45 16.82151 W",
                "132 45 6.82 W",
                
                // +/- Deg Min
                "//+/- Deg Min",
                "0 0",
                "0 0.",
                "-32 16.8232",
                "-32 16.82",
                "-32° 16.82'",
                "-32°16.82",
                "-32d 16",
                "-32 16.82",
                "-32 6.8232",
                "-32 6.82",
                "-32 0.82",
                "-132 16.82151",
                "-132 6.82",
                "32 16.8232",
                "32 16.82",
                "32 6.8232",
                "32 6.82",
                "32 0.82",
                "132 16.82151",
                "132 6.82",
                
                // Deg Min N/S/E/W
                "//Deg Min N/S/E/W",
                "32 16.8232 N",
                "32 16.82 N",
                "32 6.8232 N",
                "32 6.82 N",
                "32 0.82 N",
                "132 16.82151 N",
                "132 6.82 N",
                
                "32 16.8232 S",
                "32 16.82 S",
                "32 6.8232 S",
                "32 6.82 S",
                "32 0.82 S",
                "132 16.82151 S",
                "132 6.82 S",
                
                "32 16.8232 E",
                "32 16.82 E",
                "32 6.8232 E",
                "32 6.82 E",
                "32 0.82 E",
                "132 16.82151 E",
                "132 6.82 E",
                
                "32 16.8232 W",
                "32 16.82 W",
                "32 6.8232 W",
                "32 6.82 W",
                "32 0.82 W",
                "132 16.82151 W",
                "132 6.82 W",
                
                // +/- Decimal Degrees
                "//+/- Decimal Degrees",
                "0",
                "0.",
                "-16.8232",
                "-16.8232°",
                "-16.82",
                "-6.8232",
                "-6.82",
                "-0.82",
                "-116.82151",
                "-116.82",
                "-1.82",
                "16.8232",
                "16.82",
                "6.8232",
                "6.82",
                "0.82",
                "116.82151",
                "116.82",
                "1.82",
                
                // Decimal Degrees N/S/E/W
                "//Decimal Degrees N/S/E/W",
                "16.8232 N",
                "16.82 N",
                "16.8232° N",
                "16.82° N",
                "16.8232°N",
                "16.82°N",
                "6.8232 N",
                "6.82 N",
                "0.82 N",
                "116.82151 N",
                "116.82 N",
                "1.82 N",
                
                "16.8232 S",
                "16.82 S",
                "6.8232 S",
                "6.82 S",
                "0.82 S",
                "116.82151 S",
                "116.82 S",
                "1.82 S",
                
                "16.8232 E",
                "16.82 E",
                "6.8232 E",
                "6.82 E",
                "0.82 E",
                "116.82151 E",
                "116.82 E",
                "1.82 E",
                
                "16.8232 W",
                "16.82 W",
                "6.8232 W",
                "6.82 W",
                "0.82 W",
                "116.82151 W",
                "116.82 W",
                "1.82 W",
                "41 43."
        };

        for (String input: inputStrings)
        {
            if (input.length()==0)
            {
                continue;
            }
            
            if (input.startsWith("//"))
            {
                System.out.println();
                System.out.println("----------------------------------");
                System.out.println("----------------------------------");
                System.out.println(input.substring(2));
                System.out.println("----------------------------------");
                System.out.println("----------------------------------");
                continue;
            }
            
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

        GeoRefConverter converter = new GeoRefConverter();
        for (String input: inputStrings)
        {
            if (input.length()==0)
            {
                continue;
            }
            
            if (input.startsWith("//"))
            {
                System.out.println();
                System.out.println("----------------------------------");
                System.out.println("----------------------------------");
                System.out.println(input.substring(2));
                System.out.println("----------------------------------");
                System.out.println("----------------------------------");
                continue;
            }
            
            System.out.println("Input:             " + input);
            String decimalDegrees = converter.convert(input, GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name());
            System.out.println("Decimal degrees:   " + decimalDegrees);
        }
        System.out.println("----------------------------------");
        System.out.println("----------------------------------");
        System.out.println("----------------------------------");
        System.out.println("----------------------------------");
        System.out.println("----------------------------------");
        System.out.println("----------------------------------");
        System.out.println("----------------------------------");
        System.out.println("----------------------------------");

        String problemString = "41 43 18.";
        System.out.println("input: " + problemString);
        String d   = converter.convert(problemString, GeoRefFormat.D_PLUS_MINUS.name());
        String dm  = converter.convert(problemString, GeoRefFormat.DM_PLUS_MINUS.name());
        String dms = converter.convert(problemString, GeoRefFormat.DMS_PLUS_MINUS.name());
        System.out.println(d + "   :   " + dm + "   :   " + dms);

        problemString = d;
        System.out.println("input: " + problemString);
        String d2   = converter.convert(problemString, GeoRefFormat.D_PLUS_MINUS.name());
        String dm2  = converter.convert(problemString, GeoRefFormat.DM_PLUS_MINUS.name());
        String dms2 = converter.convert(problemString, GeoRefFormat.DMS_PLUS_MINUS.name());
        System.out.println(d2 + "   :   " + dm2 + "   :   " + dms2);
        
        problemString = dm;
        System.out.println("input: " + problemString);
        String d3   = converter.convert(problemString, GeoRefFormat.D_PLUS_MINUS.name());
        String dm3  = converter.convert(problemString, GeoRefFormat.DM_PLUS_MINUS.name());
        String dms3 = converter.convert(problemString, GeoRefFormat.DMS_PLUS_MINUS.name());
        System.out.println(d3 + "   :   " + dm3 + "   :   " + dms3);
    }
}
