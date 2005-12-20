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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.exceptions.ConfigurationException;
import edu.ku.brc.specify.helpers.XMLHelper;
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
        Std32Fade(32, true, false),
        Std24Fade(24, true, false),
        Std16Fade(16, true, false),     
        Std32BW(32, false, true),
        Std24BW(24, false, true),
        Std16BW(16, false, true),
        Std32FadeBW(32, true, true),
        Std24FadeBW(24, true, true),
        Std16FadeBW(16, true, true);
        
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
        public String  toString()     { return "Std" + Integer.toString(size) + (faded ? "f":"") + (blackWhite ? "BW":""); }
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
        loadIcons();
    }
    
    /**
     * Registers an icon (group or category), it creates an icon of "id" size and stores it
     * @param iconName the group name of icons of various sizes
     * @param fileName the file name of the icon
     * @param id the size of the icon
     * @return the icon that was created at the "id" size
     */
    public IconEntry register(final String iconName, final String fileName, final IconSize id)
    {
        URL url = getImagePath(fileName);
        
        assert url != null : "Couldn't find URL for resource path: ["+(relativePath+" "+fileName)+"]";

        ImageIcon icon = new ImageIcon(url);
        
        if (icon != null)
        {
            return register(iconName, icon, id);
        }
        
        return null;
    }

    /**
     * Registers an icon (group or category), it creates an icon of "id" size and stores it
     * @param iconName the group name of icons of various sizes
     * @param fileName the file name of the icon
     * @param id the size of the icon
     * @return the icon that was created at the "id" size
     */
    public IconEntry register(final String iconName, final ImageIcon icon, final IconSize id)
    {
        if (icon != null)
        {
            IconEntry entry = new IconEntry(iconName);
            entry.add(id, icon);
            entries.put(iconName, entry);
            return entry;
            
        } else
        {
            throw new RuntimeException("Can't register null icon name["+iconName+"] Size:"+id.toString());
        }
    }

    protected IconSize getIconSize(int size, boolean bw, boolean faded)
    {
        if (size != 32 && size != 24 && size != 16)
        {
            throw new RuntimeException("Wrong icon size! "+ size);
        }

        if (bw)
        {
            switch (size)
            {
                case 32 : return IconSize.Std32BW;
                case 24 : return IconSize.Std24BW;
                case 16 : return IconSize.Std16BW;
            }
        } else if (faded) 
        {
            switch (size)
            {
                case 32 : return IconSize.Std32Fade;
                case 24 : return IconSize.Std24Fade;
                case 16 : return IconSize.Std16Fade;
            }
        } else
        {
            switch (size)
            {
                case 32 : return IconSize.Std32;
                case 24 : return IconSize.Std24;
                case 16 : return IconSize.Std16;
            }
        }
        return null;
    }


    /**
     * Returns an icon of a specified size
     * @param iconName the name to find (really a category)
     * @param id the size ID
     * @return the icon
     */
    public ImageIcon getIcon(final String iconName, final IconSize id)
    {
        if (iconName == null)
        {
            throw new NullPointerException("icon name should not be null!");
        }
        
        IconEntry entry = entries.get(iconName);
        if (entry != null)
        {
            ImageIcon icon = entry.getIcon(id);
            if (icon == null)
            {
                if (id.size() != 32)
                {
                    return entry.getScaledIcon(getIconSize(32, id.blackWhite(), id.faded()), id);
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
    
    /**
     * Returns the IconSize enum for an integer
     * @param size the integer size
     * @return Returns the IconSize enum for an integer
     */
    protected IconSize getSizeFromInt(int size)
    {

        switch (size)
        {
            case 16: return IconSize.Std16;
            case 24:return IconSize.Std24;
            case 32:return IconSize.Std32;
        }
        throw new ConfigurationException("Desired Icon size doesn't exist! ["+size+"]");
    }
    
    /**
     * Loads icons from config file
     *
     */
    public void loadIcons()
    {
        
        try
        {
            Element root  = XMLHelper.readDOMFromConfigDir("icons.xml");
            
            List boxes = root.selectNodes("/icons/icon");
            for ( Iterator iter = boxes.iterator(); iter.hasNext(); ) 
            {
                org.dom4j.Element iconElement = (org.dom4j.Element) iter.next();
                
                String name  = iconElement.attributeValue("name");
                String sizes = iconElement.attributeValue("sizes");
                String file  = iconElement.attributeValue("file");
                if (sizes == null || sizes.length() == 0 || sizes.toLowerCase().equals("all"))
                {
                    
                    IconEntry entry = register(name, file, IconManager.IconSize.Std32);
                    
                    entry.addScaled(IconSize.Std32, IconSize.Std24);
                    entry.addScaled(IconSize.Std32, IconSize.Std16);
                    
                    //ImageIcon icon = entry.getIcon(IconSize.Std32);
                    //entry.add(IconSize.Std32BW, createBWImage(icon));
                    
                    //icon = entry.getIcon(IconSize.Std24);
                    //entry.add(IconSize.Std24BW, createBWImage(icon));
                    
                    //icon = entry.getIcon(IconSize.Std16);
                    //entry.add(IconSize.Std16, createBWImage(icon));
                    
                } else
                {
                   StringTokenizer st = new StringTokenizer(sizes, ",");
                   while (st.hasMoreTokens())
                   {
                       String sz = st.nextToken();
                       register(name, file, getSizeFromInt(Integer.parseInt(sz)));
                   }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
    }
    
    //------------------------------------------------------------
    // Static Methods
    //------------------------------------------------------------
    
    public static ImageIcon createBWImage(final ImageIcon img) 
    {
        BufferedImage bi = new BufferedImage(img.getIconWidth(), img.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        g.drawImage(img.getImage(), 0, 0, null);
        ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        colorConvert.filter(bi, bi);
        ImageIcon icon = new ImageIcon(bi);
        return icon;
    }

    /**
     * Returns an URL for the path to the image
     * @param imageName the image name
     * @return Returns an URL for the path to the image
     */
    public static URL getImagePath(final String imageName)
    {
        return Specify.class.getResource(relativePath+imageName);
    }

    /**
     * Returns a Standard Size icon
     * @param imageName the name of the icon/image
     * @return Returns a Standard Size icon
     */
    public static ImageIcon getImage(final String imageName)
    {
        return iconMgr.getIcon(imageName, IconSize.Std32);
    }

    /**
     * Returns a Standard Size icon
     * @param imageName the name of the icon/image
     * @param id tthe size to be returned
     * @return Returns a Standard Size icon
     */
    public static ImageIcon getImage(final String imageName, final IconSize id)
    {
        return iconMgr.getIcon(imageName, id);
    }

    
}
