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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.XMLHelper;

/**
 * Caches icon in three sizes (32, 24, 16).
 * NOTE: The AppName in UIRegistry MUST be set first before calling
 * setApplicationClass.
 * @code_status Beta
 *
 * @author rods
 *
 */
public class IconManager
{
    private static final Logger log = Logger.getLogger(IconManager.class);

    // Icon Size Enumerations
    public enum IconSize {
        Std32(32, false, false),
        Std24(24, false, false),
        Std16(16, false, false),
        Std8(8, false, false),
        Std32Fade(32, true, false),
        Std24Fade(24, true, false),
        Std16Fade(16, true, false),
        Std8Fade(8, true, false),
        Std32BW(32, false, true),
        Std24BW(24, false, true),
        Std16BW(16, false, true),
        Std8BW(8, false, true),
        Std32FadeBW(32, true, true),
        Std24FadeBW(24, true, true),
        Std16FadeBW(16, true, true),
        Std8FadeBW(8, true, true),
        NonStd(-1, false, false);

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
        @Override
        public String  toString()     { return "Std" + Integer.toString(size) + (faded ? "f":"") + (blackWhite ? "BW":""); }
        public void setSize(int size) { this.size = size; }
        public void setFaded(boolean faded) { this.faded = faded; }
        public void setBlackWhite(boolean bw) { blackWhite = bw; }

    }

    protected static final String      relativePath = "images/";
    protected static final IconManager instance     = new IconManager();

    protected Class<?>                        appClass = null;
    protected Hashtable<String, IconEntry> entries = new Hashtable<String, IconEntry>();

    /**
     *
     *
     */
    protected IconManager()
    {
        // do nothing
    }

    /**
     * This sets the application class so the IconManager knows where the icon images are stored
     * which is ALWAYS in the "images" directory relative to the application class, this is REQUIRED before
     * using any methods in the IconManager.
     * @param appClass the application's Class object
     */
    public static void setApplicationClass(Class<?> appClass)
    {
        instance.appClass = appClass;

        if (instance.entries.size() == 0)
        {
            IconManager.loadIcons();
        }
    }

    /**
     * Registers an icon (group or category), it creates an icon of "id" size and stores it
     * @param iconName the group name of icons of various sizes
     * @param fileName the file name of the icon
     * @param id the size of the icon
     * @return the icon that was created at the "id" size
     */
    public static IconEntry register(final String iconName, final String fileName, final IconSize id)
    {
        URL url = getImagePath(fileName);

        if (url == null)
        {
            log.error("Couldn't find URL for resource path: ["+(relativePath+fileName)+"]");
        }

        ImageIcon icon = null;
        try
        {
            icon = new ImageIcon(url);

        } catch (NullPointerException ex)
        {
            log.error("Image at URL ["+url+"] couldn't be loaded.");
        }
        
        return icon != null ? register(iconName, icon, id) : null;
    }

    /**
     * Registers an icon (group or category), it creates an icon of "id" size and stores it
     * @param iconName the group name of icons of various sizes
     * @param id the size of the icon
     * @return the icon that was created at the "id" size
     */
    public static IconEntry register(final String iconName, final ImageIcon icon, final IconSize id)
    {
        if (icon != null)
        {
            IconEntry entry = new IconEntry(iconName);
            entry.add(id, icon);
            instance.entries.put(iconName, entry);
            return entry;

        }
        // else
        throw new RuntimeException("Can't register null icon name["+iconName+"] Size:"+id.toString());
    }

    public static IconSize getIconSize(int size, boolean bw, boolean faded)
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
     * Gets a scaled icon and if it doesn't exist it creates one and scales it
     * @param icon image to be scaled
     * @param iconSize the icon size (Std)
     * @param scaledIconSize the new scaled size in pixels
     * @return the scaled icon
     */
    public static ImageIcon getScaledIcon(final ImageIcon icon, final IconSize iconSize, final IconSize scaledIconSize)
    {
        if (icon != null)
        {
            ImageIcon scaledIcon = new ImageIcon(icon.getImage().getScaledInstance(scaledIconSize.size(),
                    scaledIconSize.size(), Image.SCALE_SMOOTH));
            return scaledIcon;
        }
        // else
        log.error("Couldn't find icon [" + iconSize + "] to scale to [" + scaledIconSize + "]");
        return null;
    }


    /**
     * Returns an icon of a specified size
     * @param iconName the name to find (really a category)
     * @param id the size ID
     * @return the icon
     */
    public static ImageIcon getIcon(final String iconName, final IconSize id)
    {
        ImageIcon icon = getIcon(iconName);
        if (icon==null)
        {
            return null;
        }
        
        icon = getScaledIcon(icon, null, id);
        return icon;
    }
    
    /**
     * Gets an icon as it's "base" size or meaning its opriginal size.
     * @param iconName the name of the icon
     * @return the un-sized icon
     */
    public static ImageIcon getIcon(final String iconName)
    {
        if (iconName == null)
        {
            throw new NullPointerException("icon name should not be null!");
        }

        IconEntry entry = instance.entries.get(iconName);
        if (entry != null)
        {
            return entry.getIcon();
        }
        return null;
    }

    /**
     * Returns the IconSize enum for an integer
     * @param size the integer size
     * @return Returns the IconSize enum for an integer
     */
    protected static IconSize getSizeFromInt(int size)
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
    public static void loadIcons()
    {

        try
        {
            Element root  = XMLHelper.readDOMFromConfigDir("icons.xml");
            if (root != null)
            {
                Hashtable<String, String> aliases = new Hashtable<String, String>();
                List<?> boxes = root.selectNodes("/icons/icon");
                for ( Iterator<?> iter = boxes.iterator(); iter.hasNext(); )
                {
                    org.dom4j.Element iconElement = (org.dom4j.Element) iter.next();

                    String name  = iconElement.attributeValue("name");
                    String sizes = iconElement.attributeValue("sizes");
                    String file  = iconElement.attributeValue("file");
                    String alias  = iconElement.attributeValue("alias");
                    
                    if (StringUtils.isNotEmpty(alias))
                    {
                        aliases.put(name, alias);
                        
                    } else if (sizes == null || sizes.length() == 0 || sizes.toLowerCase().equals("all"))
                    {
                        //log.info("["+name+"]["+sizes+"]["+file+"]");
                        IconEntry entry = register(name, file, IconManager.IconSize.Std32);

                        entry.addScaled(IconSize.Std32, IconSize.Std24);
                        entry.addScaled(IconSize.Std32, IconSize.Std16);

                    } else if (sizes.toLowerCase().equals("nonstd"))
                    {
                        register(name, file, IconSize.NonStd);

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
                
                for (String name : aliases.keySet())
                {
                    IconEntry entry = instance.entries.get(aliases.get(name));
                    if (entry != null)
                    {
                        instance.entries.put(name, entry);
                    }
                }
            } else
            {
                log.debug("Couldn't open icons.xml");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
    }

    /**
     * Creates a Black and White image from the color
     * @param img the image to be converted
     * @return new B&W image
     */
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
     * Returns an URL for the path to the image in the "images" directory that is relative to the application class.
     * <br> For example &lt;app class&gt;/images/&lt;file name&gt;
     * @param imageName the image name
     * @return Returns an URL for the path to the image
     */
    public static URL getImagePath(final String imageName)
    {
        return instance.appClass.getResource(relativePath+imageName);
    }

    /**
     * Returns a Standard Size icon (32x32 pixels).
     * @param imageName the name of the icon/image
     * @return Returns a Standard Size icon
     */
    public static ImageIcon getImage(final String imageName)
    {
        return getIcon(imageName, IconSize.Std32);
    }

    /**
     * Returns a Standard Size icon
     * @param imageName the name of the icon/image
     * @param id tthe size to be returned
     * @return Returns a Standard Size icon
     */
    public static ImageIcon getImage(final String imageName, final IconSize id)
    {
        return getIcon(imageName, id);
    }


}
