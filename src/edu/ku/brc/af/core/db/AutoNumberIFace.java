/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.core.db;

import java.util.Properties;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jun 20, 2007
 *
 */
public interface AutoNumberIFace
{

    /**
     * Used for initialization.
     * @param properties the initialization properties
     */
    public void setProperties(final Properties properties);
    
    /**
     * Get the next number.
     * @param formatter the formatter to use
     * @param value the current value
     * @return the fully expressed number.
     */
    public abstract String getNextNumber(final UIFieldFormatterIFace formatter, String value);
    
    /**
     * @return true if using the defined 'generic' class for numbering, false if using an external class.
     */
    public abstract boolean isGeneric();
    
    /**
     * Appends a presentation of itself in XML to the StringBuilder
     * @param sb the StringBuilder
     */
    public abstract void toXML(StringBuilder sb);
    
    /**
     * 
     * @return true if there was a problem when trying to auto number and the code should check 
     * the ErrorKey for the localized error message.
     */
    public abstract boolean isInError();
    
    /**
     * @return the localized error message.
     */
    public abstract String getErrorMsg();
    
}
