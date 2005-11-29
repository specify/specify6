/* Filename:    $RCSfile: IconEntry.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui;

import java.awt.Image;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.ui.IconManager.IconSize;

public class IconEntry
{
    private static Log log = LogFactory.getLog(IconManager.class);
    
    private String name;
    private Hashtable<Integer, ImageIcon> icons = new Hashtable<Integer, ImageIcon>();
    
    /**
     * 
     * @param name
     */
    public IconEntry(final String name)
    {
        this.name = name;
    }


    /**
     * 
     * @param size
     * @return
     */
    public ImageIcon getIcon(final Integer size)
    {
        ImageIcon icon = icons.get(size);
        //if (icon == null)
        //{
        //    icon = icons.get(size);
        //}
        return icon;
    }
    
    /**
     * 
     * @param size
     * @param icon
     */
    public void add(final Integer size, final ImageIcon icon)
    {
        icons.put(size, icon);
    }


    /**
     * 
     * @param iconSize
     * @param scaledIconSize
     * @return
     */
    public ImageIcon getScaledIcon(final IconSize iconSize, final IconSize scaledIconSize)
    {
        ImageIcon icon = icons.get(iconSize.size());
        if (icon != null)
        {
            ImageIcon scaledIcon = new ImageIcon(icon.getImage().getScaledInstance(scaledIconSize.size(), scaledIconSize.size(), Image.SCALE_SMOOTH));
            if (scaledIcon != null)
            {
                icons.put(scaledIconSize.size(), scaledIcon);
                return scaledIcon;
            } else
            {
                
                log.error("Can't scale icon ["+iconSize+"] to ["+scaledIconSize+"]");
            }
            
        } else
        {
            log.error("Couldn't find icon ["+iconSize+"] to scale to ["+scaledIconSize+"]");
        }
        return null;
    }
}
