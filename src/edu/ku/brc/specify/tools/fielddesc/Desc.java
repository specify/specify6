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
 * Sep 4, 2007
 *
 */
public class Desc implements Cloneable
{
    protected String text;
    protected String country;
    protected String lang;
    protected String variant;
    
    public Desc(String text, String country, String lang, String variant)
    {
        super();
        this.text    = text;
        this.country = country;
        this.lang    = lang;
        this.variant = variant;
    }
    
    public Desc(final String text, final Locale locale)
    {
        super();
        this.text    = text;
        this.country = locale.getCountry();
        this.lang    = locale.getLanguage();
        this.variant = locale.getVariant();
    }
    
    /**
     * @return the text
     */
    public String getText()
    {
        return text;
    }
    /**
     * @return the country
     */
    public String getCountry()
    {
        return country;
    }
    /**
     * @return the lang
     */
    public String getLang()
    {
        return lang;
    }
    
    /**
     * @return the variant
     */
    public String getVariant()
    {
        return variant;
    }
    /**
     * @param text the text to set
     */
    public void setText(String text)
    {
        this.text = text;
    }
    /**
     * @param country the country to set
     */
    public void setCountry(String country)
    {
        this.country = country;
    }
    /**
     * @param lang the lang to set
     */
    public void setLang(String lang)
    {
        this.lang = lang;
    }
    /**
     * @param variant the variant to set
     */
    public void setVariant(String variant)
    {
        this.variant = variant;
    }
    
    public boolean isLocale(final Locale locale)
    {
        //System.out.println(lang.equals(locale.getLanguage()) +" "+
        //        country.equals(locale.getCountry()) +" "+ 
        //        (variant == null || variant.equals(locale.getVariant())));
        return lang.equals(locale.getLanguage()) &&
               (country == null || country.equals(locale.getCountry())) && 
               (variant == null || variant.equals(locale.getVariant()));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Desc d = (Desc)super.clone();
        d.country = country;
        d.lang    = lang;
        d.text    = text;
        return d;
    }
}
