/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.schemalocale;

import java.util.Locale;

/**
 * Class used for displaying a Locale in a list.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Oct 2, 2007
 *
 */
public class DisplayLocale implements Comparable<DisplayLocale>
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

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(DisplayLocale o)
    {
        return locale.getDisplayName().compareTo(o.getLocale().getDisplayName());
    }
}