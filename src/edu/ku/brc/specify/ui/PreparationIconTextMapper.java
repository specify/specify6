/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;

import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;

/**
 * This class is an implementation of both {@link ObjectTextMapper} and {@link ObjectIconMapper}
 * that handles {@link Preparation} objects.
 *
 * @code_status Beta
 * @author jstewart
 */
public class PreparationIconTextMapper implements ObjectTextMapper, ObjectIconMapper
{
    /**
     * Create an instance.
     */
    public PreparationIconTextMapper()
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getMappedClasses()
     */
    public Class<?>[] getMappedClasses()
    {
        Class<?>[] mappedClasses = new Class[1];
        mappedClasses[0] = Preparation.class;
        return mappedClasses;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getString(java.lang.Object)
     */
    public String getString(Object o)
    {
        Preparation prep = (Preparation)o;
        if (prep != null)
        {
            return DataObjFieldFormatMgr.getInstance().format(prep, "Preparation");
        }
        return null;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object)
     */
    public ImageIcon getIcon(Object o)
    {
        Preparation p = (Preparation)o;
        String prepTypeName = p.getPrepType().getName();
        //IconSize size = IconSize.Std32;
        String name = null;
        if(prepTypeName.equalsIgnoreCase("skeleton"))
        {
            name = "skeleton";
        }
        else if(prepTypeName.equalsIgnoreCase("C&S"))
        {
            name = "cs";
        }
        else if(prepTypeName.equalsIgnoreCase("EtOH"))
        {
            name = "etoh";
        }
        else if(prepTypeName.equalsIgnoreCase("x-ray") || prepTypeName.equalsIgnoreCase("xray"))
        {
            name = "xray";
        }
        else
        {
            name = "unknown";
        }
        
        ImageIcon icon = IconManager.getIcon(name);
        if (icon==null)
        {
            return null;
        }
        return IconManager.getScaledIcon(icon, null, IconSize.Std32);
    }
}
