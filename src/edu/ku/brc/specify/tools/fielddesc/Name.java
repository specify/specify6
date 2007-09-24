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
 * Sep 21, 2007
 *
 */
public class Name extends Desc implements Cloneable
{
    /**
     * @param text
     * @param country
     * @param lang
     * @param variant
     */
    public Name(String text, String country, String lang, String variant)
    {
        super(text, country, lang, variant);
    }
    
    public Name(final String text, final Locale locale)
    {
        super(text, locale);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Name nm = (Name)super.clone();
        nm.country = country;
        nm.lang    = lang;
        nm.text    = text;
        return nm;
    }
}
