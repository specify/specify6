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
package edu.ku.brc.ui;

import java.net.URL;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import edu.ku.brc.ui.IconManager.IconSize;

/**
 * An entry in the IconManager. Note: is isAlias is set it is not the "true" own of the Icon hash.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class IconEntry
{ 
    private String                   name;
    private boolean                  isAlias;
    private Hashtable<IconSize, IconSizeEntry> icons = null;
    
    // Base Size and location as 
    private IconSize                 size;
    private URL                      url;
    private ImageIcon                imageIcon = null;
    
    /**
     * 
     * @param name the name of the icon entry
     */
    public IconEntry(final String   name, 
                     final IconSize size,
                     final URL      url,
                     final boolean  isAlias,
                     final Hashtable<IconSize, IconSizeEntry> iconHash)
    {
        this.name    = name;
        this.size    = size;
        this.url     = url;
        this.isAlias = isAlias;
        this.icons   = iconHash;
    }
    
    /**
     * 
     * @param name the name of the icon entry
     */
    public IconEntry(final String   name, 
                     final IconSize size,
                     final URL      url)
    {
        this(name, size, url, false, new Hashtable<IconSize, IconSizeEntry>());
    }

    /**
     * @return the isAlias
     */
    public boolean isAlias()
    {
        return isAlias;
    }

    /**
     * @param isAlias the isAlias to set
     */
    public void setAlias(boolean isAlias)
    {
        this.isAlias = isAlias;
    }

    /**
     * @return the icons
     */
    public Hashtable<IconSize, IconSizeEntry> getIcons()
    {
        return icons;
    }

    /**
     * @param icons the icons to set
     */
    public void setIcons(Hashtable<IconSize, IconSizeEntry> icons)
    {
        if (this.icons == null)
        {
            this.icons = new Hashtable<IconSize, IconSizeEntry>();
        }
        this.icons.clear();
        for (IconSize sz : icons.keySet())
        {
            this.icons.put(sz, icons.get(sz));
        }
    }

    /**
     * Returns an icon for a given size in pixels
     * @param id the ID of the size of the icon
     * @return the icon for that size in pixels
     */
    public ImageIcon getIcon(final IconSize id)
    {
        imageIcon = getIcon();
        
        IconSizeEntry sizeEntry = icons.get(id);
        if (sizeEntry == null)
        {

            ImageIcon imgIcon = IconManager.createNewScaledIcon(imageIcon, size, id);
            if (id.blackWhite())
            {
                imgIcon = IconManager.createBWImage(imgIcon);
            }
            if (id.faded())
            {
                imgIcon = IconManager.createFadedImage(imgIcon);
            }
            IconSizeEntry newSizeEntry = new IconSizeEntry(id, imgIcon);
            icons.put(id, newSizeEntry);
            return imgIcon;
        }
        return sizeEntry.getImageIcon();
    }
    
   /**
     * @return the size
     */
    public IconSize getSize()
    {
        return size;
    }

    /**
     * @return the url
     */
    public URL getUrl()
    {
        return url;
    }

   /**
     * @param size the size to set
     */
    public void setSize(IconSize size)
    {
        this.size = size;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(URL url)
    {
        this.url = url;
    }

   /**
     * @param imageIcon the imageIcon to set
     */
    public void setIcon(ImageIcon imageIcon)
    {
        this.imageIcon = imageIcon;
    }

/**
    * @return
    */
   public ImageIcon getIcon()
   {
       if (imageIcon == null)
       {
           imageIcon = new ImageIcon(url);
       }
       return imageIcon;
       //IconSizeEntry entry = getDefaultIconSizeEntry();
       //return entry != null ? entry.getImageIcon() : null;
   }
   
   /**
    * @return
    */
   /*private IconSizeEntry getDefaultIconSizeEntry()
   {
        switch (icons.size())
        {
            case 0:
            {
                return null;
            }
            
            case 1:
            {
            	return icons.values().iterator().next();
            }
            
            default:
            {
                // First try 32x32
                IconSizeEntry entry = icons.get(IconSize.Std32);
                if (entry != null)
                {
                    return entry;
                }
                
                // then try non-std
                entry = icons.get(IconSize.NonStd);
                if (entry != null)
                {
                    return entry;
                }
                
                // ok, got looking for anything
                for (IconSize size: IconSize.values())
                {
                    entry = icons.get(size);
                    if (entry != null)
                    {
                        return entry;
                    }
                }
                return null;
            }
        }
    }*/

    /**
     * Return name
     * @return Return name
     */
    public String getName()
    {
        return name;
    }
    
    //---------------------------------------------------
    //-- 
    //---------------------------------------------------
    public class IconSizeEntry 
    {
        protected IconSize  iseSize;
        protected ImageIcon iseImageIcon;
        
        /**
         * @param urlStr
         */
        public IconSizeEntry(final IconSize size)
        {
            super();
            this.iseSize      = size;
            this.iseImageIcon = null;
        }
        
        public IconSizeEntry(final IconSize size, 
                             final ImageIcon imageIcon)
        {
            super();
            this.iseSize      = size;
            this.iseImageIcon = imageIcon;
        }
        
        public boolean isIconAvailable()
        {
            return iseImageIcon != null;
        }
        
        /**
         * @return the imageIcon
         */
        public ImageIcon getImageIcon()
        {
            if (iseImageIcon == null)
            {
                iseImageIcon = new ImageIcon(url);
            }
            return iseImageIcon;
        }

        /**
         * @param iseImageIcon the iseImageIcon to set
         */
        public void setImageIcon(ImageIcon iseImageIcon)
        {
            this.iseImageIcon = iseImageIcon;
        }

        /**
         * @return the size
         */
        public IconSize getSize()
        {
            return iseSize;
        }
    }
}
