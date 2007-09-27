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

import java.util.List;
import java.util.Locale;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 22, 2007
 *
 */
public interface LocalizableNameDescIFace
{
    
    /**
     * @return
     */
    public String getName();
    
    public String getType();
    
    /**
     * @return the descs
     */
    public List<Desc> getDescs();
    
    /**
     * @param descs the descs to set
     */
    public void setDescs(List<Desc> descs);

    /**
     * @return the names
     */
    public List<Name> getNames();

    /**
     * @param names the names to set
     */
    public void setNames(List<Name> names);
    
    /**
     * @param locale
     */
    public void setLocale(final Locale locale);
    
    /**
     * @param srcLocale
     * @param dstLocale
     */
    public void copyLocale(final Locale srcLocale, final Locale dstLocale);
    
}
