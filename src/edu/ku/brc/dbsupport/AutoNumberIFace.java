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

import edu.ku.brc.ui.forms.formatters.UIFieldFormatter;

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

    public abstract String getNextNumber(final UIFieldFormatter formatter, String value);
    
}
