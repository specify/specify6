/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui;

/**
 * An interface defining the basic capabilities of classes that map various objects
 * to {@link String}s.
 *
 * @author jstewart
 */
public interface ObjectTextMapper
{
    /**
     * Returns a String representing the given Object.
     *
     * @param o any object
     * @return a String representing the Object argument
     */
    public String getString(Object o);
    
    /**
     * Returns an array of the classes mapped by this ObjectTextMapper.
     *
     * @return an array containing the mapped classes
     */
    public Class[] getMappedClasses();
}
