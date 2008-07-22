/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.core.expresssearch;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jun 18, 2008
 *
 */
public class SearchTermField
{
    public static final int NO_OPTIONS             =   0; // Indicates there are no options
    public static final int IS_STRING              =   1; // String Only
    public static final int IS_DATE                =   2; // Is fully
    public static final int IS_YEAR_OF_DATE        =   4; // It could be just the year 
    public static final int IS_NUMERIC             =   8; // Is a numeric number
    public static final int HAS_DEC_POINT          =  16; // has a decimal point in the number
    public static final int STARTS_WILDCARD        =  32; // starts with wild card
    public static final int ENDS_WILDCARD          =  64; // ends with wild card

    protected String term;
    protected int    options = NO_OPTIONS;
    
    /**
     * @param term
     * @param options
     */
    public SearchTermField(final String term)
    {
        super();
        this.term = term;
        this.options = NO_OPTIONS;
    }
    
    public boolean isSingleChar()
    {
        return term.length() == 1 && term.equals("*");
    }

    /**
     * @param term the term to set
     */
    public void setTerm(String term)
    {
        this.term = term;
    }

    /**
     * @return the term
     */
    public String getTerm()
    {
        return term;
    }

    /**
     * @return the options
     */
    public int getOptions()
    {
        return options;
    }
    
    /**
     * @param option
     */
    public void setOption(final int option)
    {
        options |= option;
    }

    public boolean isOptionOn(final int opt)
    {

        return (options & opt) == opt;
    }
    
    /**
     * @param option
     * @return
     */
    public boolean isOn(final int option)
    {
        return isOptionOn(options, option);
    }
    
    /**
     * Helper method to see if an option is turned on.
     * @param options the range of options that can be turned on
     * @param opt the actual option that may be turned on
     * @return true if the opt bit is on
     */
    public static boolean isOptionOn(final int options, final int opt)
    {

        return (options & opt) == opt;
    }
}
