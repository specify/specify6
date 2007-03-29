package edu.ku.brc.util;

import java.math.BigDecimal;
import java.util.Vector;

public class GeoRefConverter implements StringConverter
{
    public enum Pattern
    {
        DMS_PLUS_MINUS ("[\\+\\-]?\\d{1,3}\\s\\d{1,2}\\s\\d{2}\\.\\d{0,}\\s*")
        {
            @Override
            public String convertToDecimalDegrees(String orig)
            {
                BigDecimal dd = LatLonConverter.convertDDMMSSToDDDD(orig);
                return dd.toPlainString();
            }
        },
        DM_PLUS_MINUS  ("[\\+\\-]?\\d{1,3}\\s\\d{1,2}\\.\\d{0,}\\s*")
        {
            @Override
            public String convertToDecimalDegrees(String orig)
            {
                BigDecimal dd = LatLonConverter.convertDDMMMMToDDDD(orig);
                return dd.toPlainString();
            }
        },
        D_PLUS_MINUS   ("[\\+\\-]?\\d{1,3}\\.\\d{0,}\\s*")
        {
            @Override
            public String convertToDecimalDegrees(String orig)
            {
                BigDecimal dd = LatLonConverter.convertDDDDToDDDD(orig);
                return dd.toPlainString();
            }
        },
        DMS_NSEW       ("\\d{1,3}\\s\\d{2}\\s\\d{1,2}\\.\\d{0,}\\s[NSEW]{1}.*")
        {
            @Override
            public String convertToDecimalDegrees(String orig)
            {
                BigDecimal dd = LatLonConverter.convertDirectionalDDMMSSToDDDD(orig);
                return dd.toPlainString();
            }
        },
        DM_NSEW        ("\\d{1,3}\\s\\d{1,2}\\.\\d{0,}\\s[NSEW]{1}.*")
        {
            @Override
            public String convertToDecimalDegrees(String orig)
            {
                BigDecimal dd = LatLonConverter.convertDirectionalDDMMMMToDDDD(orig);
                return dd.toPlainString();
            }
        },
        D_NSEW         ("\\d{1,3}\\.\\d{0,}\\s[NSEW]{1}.*")
        {
            @Override
            public String convertToDecimalDegrees(String orig)
            {
                BigDecimal dd = LatLonConverter.convertDirectionalDDDDToDDDD(orig);
                return dd.toPlainString();
            }
        };
        
        private final String regex;
        
        Pattern(String regex)
        {
            this.regex = regex;
        }
        
        public boolean matches(String input)
        {
            return input.matches(regex);
        }
        
        public abstract String convertToDecimalDegrees(String original);
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
        // first we have to 'discover' the original format

        for (Pattern p: Pattern.values())
        {
            if (p.matches(original))
            {
                System.out.println(p + " matched the input string");
                String converted = p.convertToDecimalDegrees(original);
                System.out.println("Converted output: " + converted);
                return converted;
            }
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
        inputStrings.add("132 45 16.82235 W");
        inputStrings.add("-32 45.15166");
        inputStrings.add("-32 45.16236");
        inputStrings.add("32 45.1616 S");
        inputStrings.add("52 22.6 W");
        inputStrings.add("-108.13461");
        inputStrings.add("-20.26");
        inputStrings.add("100.1351 S");
        inputStrings.add("9.15161 W");

        for (String input: inputStrings)
        {
            for (Pattern p: Pattern.values())
            {
                if (p.matches(input))
                {
                    System.out.println(p + " matched the input string");
                    System.out.println("Converted output: " + p.convertToDecimalDegrees(input));
                }
            }
        }
    }
}
