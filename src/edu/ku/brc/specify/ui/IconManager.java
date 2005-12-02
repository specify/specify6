/* Filename:    $RCSfile: IconManager.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.2 $
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

import java.net.URL;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.Specify;
/**
 * @author Rod Spears
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class IconManager
{
    private static Log log = LogFactory.getLog(IconManager.class);
    
    // Icon Size Enumerations
    public enum IconSize {
        Std32(32, false, false),
        Std24(24, false, false),
        Std16(16, false, false),
        Custom(0, false, false),
        Std32Fade(32, true, false),
        Std24Fade(24, true, false),
        Std16Fade(16, true, false),     
        CustomFade(0, false, false),
        Std32BW(32, false, true),
        Std24BW(24, false, true),
        Std16BW(16, false, true),
        CustomBW(0, false, false),
        Std32FadeBW(32, true, true),
        Std24FadeBW(24, true, true),
        Std16FadeBW(16, true, true),
        CustomFadeBW(0, false, false);
        
        IconSize(final int size, final boolean faded, final boolean blackWhite)
        { 
            this.size = size;
            this.faded = faded;
            this.blackWhite = blackWhite;
        }
        
        private int     size;
        private boolean faded;
        private boolean blackWhite;
        
        public Integer size()         { return size; }
        public boolean faded()        { return faded; }
        public boolean blackWhite()   { return blackWhite; }
        public String  toString()     { return Integer.toString(size) + (faded ? "f":"") + (blackWhite ? "BW":""); }
        public void setSize(int size) { this.size = size; }
        public void setFaded(boolean faded) { this.faded = faded; }
        public void setBlackWhite(boolean bw) { blackWhite = bw; }
        
    };
    
    protected static String      relativePath = "images/";
    private   static IconManager iconMgr      = new IconManager();
    
    protected Hashtable<String, IconEntry> entries       = new Hashtable<String, IconEntry>();
    
  
    /**
     * 
     * @return the singleton instance
     */
    public static IconManager getInstance()
    {
         return iconMgr;
    }
    
    /**
     * 
     *
     */
    protected IconManager()
    {
    }


    /**
     * Registers an icon (group or category), it creates an icon of "id" size and stores it
     * @param iconName the group name of icons of various sizes
     * @param fileName the file name of the icon
     * @param id the size of the icon
     * @return the icon that was created at the "id" size
     */
    public ImageIcon register(final String iconName, final String fileName, final IconSize id)
    {
        ImageIcon icon = getIcon(iconName, id);
        if (icon == null)
        {
            IconEntry entry = new IconEntry(iconName);
            URL url = Specify.class.getResource(relativePath+fileName);
            
            assert url != null : "Couldn't find URL for resource path: ["+(relativePath+fileName)+"]";
            
            icon = new ImageIcon(url);
            if (icon != null)
            {
                
                entry.add(id.size(), icon);
                entries.put(iconName, entry);
                return icon;
                
            } else
            {
                log.error("Can't load icon ["+iconName+"]["+fileName+"]");
            }
            return null;
        } else
        {
            return icon;
        }
    }


    /**
     * Returns an icon of a specified size
     * @param iconName the name to find (really a category)
     * @param id the size ID
     * @return the icon
     */
    public ImageIcon getIcon(final String iconName, final IconSize id)
    {
        
        IconEntry entry = entries.get(iconName);
        if (entry != null)
        {
            ImageIcon icon = entry.getIcon(id.size());
            if (icon == null)
            {
                if (id.size() != 32)
                {
                    IconManager.IconSize.Custom.setSize(32);
                    IconManager.IconSize.Custom.setFaded(id.faded());
                    IconManager.IconSize.Custom.setBlackWhite(id.blackWhite());
                    return entry.getScaledIcon(IconManager.IconSize.Custom, id);
                } else
                {
                    log.error("Couldn't find Std size for icon ["+ iconName+"] is not registered.");
                }
            } else
            {
                return icon;
            }
        } else
        {
            // It is ok that it isn't registered
        }
        return null;
    }
    
}
