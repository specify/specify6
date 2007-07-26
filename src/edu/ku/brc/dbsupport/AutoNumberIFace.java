/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.dbsupport;

import java.util.Properties;

import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;

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
    
}
