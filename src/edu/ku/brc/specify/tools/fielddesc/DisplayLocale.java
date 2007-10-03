/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.fielddesc;

import java.util.Locale;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 2, 2007
 *
 */
public class DisplayLocale
{
    protected Locale locale;

    /**
     * @param locale
     */
    public DisplayLocale(final Locale locale)
    {
        super();
        this.locale = locale;
    }

    /**
     * @return the locale
     */
    public Locale getLocale()
    {
        return locale;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return locale.getDisplayName();
    }
}