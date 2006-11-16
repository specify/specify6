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
        String prepTypeName = p.getPrepType().getName();
        if(prepTypeName.equalsIgnoreCase("skeleton"))
        {
            return IconManager.getIcon("skeleton", IconSize.Std24);
        }

        if(prepTypeName.equalsIgnoreCase("C&S"))
        {
            return IconManager.getIcon("cs", IconSize.Std24);
        }

        if(prepTypeName.equalsIgnoreCase("EtOH"))
        {
            return IconManager.getIcon("etoh", IconSize.Std24);
        }
        
        if(prepTypeName.equalsIgnoreCase("x-ray") || prepTypeName.equalsIgnoreCase("xray"))
        {
            return IconManager.getIcon("xray", IconSize.Std24);
        }

        return IconManager.getIcon("unknown", IconSize.Std24);
    }
}
