/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;

/**
 *
 * @code_status Beta
 * @author jstewart
 */
public interface ObjectIconMapper
{
    public ImageIcon getIcon(Object o);
    public Class[] getMappedClasses();
}
