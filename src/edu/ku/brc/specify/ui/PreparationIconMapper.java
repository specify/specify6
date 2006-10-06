/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;

import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class PreparationIconMapper implements ObjectIconMapper
{
    public PreparationIconMapper()
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getMappedClasses()
     */
    public Class[] getMappedClasses()
    {
        Class[] mappedClasses = new Class[1];
        mappedClasses[0] = Preparation.class;
        return mappedClasses;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object)
     */
    public ImageIcon getIcon(Object o)
    {
        Preparation p = (Preparation)o;
        if(p.getPrepType().getName().equalsIgnoreCase("C&S"))
        {
            return IconManager.getIcon("image", IconSize.Std24);
        }

        if(p.getPrepType().getName().equalsIgnoreCase("EtOH"))
        {
            return IconManager.getIcon("audio", IconSize.Std24);
        }

        if(p.getPrepType().getName().equalsIgnoreCase("X-Ray"))
        {
            return IconManager.getIcon("video", IconSize.Std24);
        }

        return IconManager.getIcon("unknown", IconSize.Std24);
    }
}
