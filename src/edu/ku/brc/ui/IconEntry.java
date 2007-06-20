/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.ui;

import java.net.URL;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import edu.ku.brc.ui.IconManager.IconSize;

/**
 * An entry in the IconManager
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class IconEntry
{ 
    private String name;
    private Hashtable<IconSize, URL> icons = new Hashtable<IconSize, URL>();
    /**
     * 
     * @param name the name of the icon entry
     */
    public IconEntry(final String name)
    {
        this.name = name;
    }

    /**
     * Returns an icon for a given size in pixels
     * @param id the ID of the size of the icon
     * @return the icon for that size in pixels
     */
    public ImageIcon getIcon(final IconSize id)
    {
        //log.debug("Getting["+name+"]["+id.toString()+"]");
        return makeIcon(icons.get(id));
    }
    
    /**
     * Adds an icon URL of a particular size
     * @param id the IconSize
     * @param path the URL of the icon to be added
     */
    public void add(final IconSize id, final URL path)
    {
        //log.debug("Putting["+name+"]["+id.toString()+"]");
    	icons.put(id,path);
    }

    /*
    do not need addScaled, the image will scale when it is called. 
    public void addScaled(final IconSize id, final IconSize newId)	
    {
        //log.debug("Putting["+name+"]["+id.toString()+"]");
        icons.put(newId, getScaledIcon(id, newId));
    }*/
    
    

    /**
     * Gets a scaled icon and ,if it doesn't exist it creates one and scales it
     * @param iconSize the icon size (Std)
     * @param scaledIconSize the new scaled size in pixels
     * @return the scaled icon
     */     
   public ImageIcon getScaledIcon(final IconSize iconSize, final IconSize scaledIconSize)
    {
	   ImageIcon scaledIcon = IconManager.getScaledIcon(makeIcon(icons.get(iconSize)), iconSize, scaledIconSize);
      
    	return scaledIcon;
    }
    
    public ImageIcon getIcon()
    {
        switch (icons.size())
        {
            case 0:
            {
                return null;
            }
            case 1:
            {
            	return makeIcon(icons.values().iterator().next());
            }
            default:
            {
                ImageIcon icon = makeIcon(icons.get(IconSize.NonStd));
                if (icon!=null)
                {
                    return icon;
                }
                for (IconSize size: IconSize.values())
                {
                	icon = makeIcon(icons.get(size));
                    if (icon!=null)
                    {
                        return icon;
                    }
                }
                return null;
            }
        }
    }


    /**
     * Return name
     * @return Return name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns an icon from a given URL
     * @param path the URL of the icon
     * @return the icon from that path
     */
    public ImageIcon makeIcon(URL path)
    {
    	ImageIcon icon = new ImageIcon(path);
    	return icon;
    	
    }
    
    /**
     * Return URL
     * @return Return URL
     */
    public URL getURL(final IconSize id)
    {
    	return icons.get(id);
    }
   
    
}
