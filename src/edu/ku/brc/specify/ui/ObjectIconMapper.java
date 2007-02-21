/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;

/**
 * An interface defining the basic capabilities of classes that map various objects
 * to {@link ImageIcon}s.
 *
 * @author jstewart
 */
public interface ObjectIconMapper
{
    /**
     * Returns an ImageIcon representing the given Object.
     *
     * @param o any object
     * @return an ImageIcon representing the Object argument
     */
    public ImageIcon getIcon(Object o);
    
    /**
     * Returns an array of the classes mapped by this ObjectIconMapper.
     *
     * @return an array containing the mapped classes
     */
    public Class[] getMappedClasses();
}
