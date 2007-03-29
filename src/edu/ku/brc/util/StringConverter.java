package edu.ku.brc.util;

public interface StringConverter
{
    /**
     * Converts the provided original String to the format specified.
     * 
     * @param original the original string
     * @param destFormat the name of the destination format, not a format string as in {@link String#format(String, Object[])}
     * @return the converted string
     */
    public String convert(String original, String destFormat);
}
