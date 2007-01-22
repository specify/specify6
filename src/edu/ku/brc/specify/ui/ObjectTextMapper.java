/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public interface ObjectTextMapper
{
    public String getString(Object o);
    public Class[] getMappedClasses();
}
