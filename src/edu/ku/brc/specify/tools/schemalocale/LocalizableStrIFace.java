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
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 28, 2007
 *
 */
public interface LocalizableStrIFace
{
    /**
     * @return
     */
    public abstract String getText();

    /**
     * @param text the text to set
     */
    public abstract void setText(String text);

    /**
     * @return the country
     */
    public abstract String getCountry();

    /**
     * @param country the country to set
     */
    public abstract void setCountry(String country);

    /**
     * @return the language
     */
    public abstract String getLanguage();

    /**
     * @param language the language to set
     */
    public abstract void setLanguage(String language);

    /**
     * @return the variant
     */
    public abstract String getVariant();

    /**
     * @param variant the variant to set
     */
    public abstract void setVariant(String variant);
    
    /**
     * @param locale
     * @return
     */
    public abstract boolean isLocale(Locale locale);
    
}
