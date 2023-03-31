/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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


import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.GrayFilter;
import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.util.Pair;

/**
 * Caches icon in three sizes (32, 24, 16).
 * NOTE: The AppName in UIRegistry MUST be set first before calling
 * setApplicationClass.
 * @code_status Beta
 *
 * @author rods
 *
 */
public class IconManager extends Component
{
    private static final Logger log = Logger.getLogger(IconManager.class);

    public enum IconSize {
        Std32(32, false, false),
        Std24(24, false, false),
        Std20(20, false, false),
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
    
    public static final    IconSize    STD_ICON_SIZE = IconSize.Std20;

    protected static final String      relativePath = "images/";
    protected static final IconManager instance     = new IconManager();
    
    protected static String            subdirPath   = null;

    protected Class<?>                 appClass = null;
    
    protected Hashtable<String, Vector<String>> iconSets = new Hashtable<String, Vector<String>>();
    
    protected Hashtable<String, IconEntry> defaultEntries  = new Hashtable<String, IconEntry>();
    
    // This is used during the registration process to track
    // extra icons loaded with a 'type' in the XML
    protected Vector<String> iconListForType = null;

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

        if (instance.defaultEntries.size() == 0)
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
   public static IconEntry register(final String iconName, 
                                    final String fileName,
                                    final IconSize id)
    {
	   
        URL url = getImagePath(fileName);

        if (url == null)
        {
            log.error("Couldn't find URL for resource path: ["+(relativePath+(subdirPath != null ? subdirPath : "")+fileName)+"]");
        }

        ImageIcon icon = null;
        try
        {	
            icon = new ImageIcon(url);  
            
        } catch (NullPointerException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IconManager.class, ex);
             log.error("Image at URL ["+url+"] ["+iconName+"] ["+fileName+"] couldn't be loaded.");
        }
       
        return icon != null ? register(iconName, icon, url, id) : null;
       
    }
   
   

    /**
     * Registers an icon (group or category), it creates an icon of "id" size and stores it
     * @param iconName the group name of icons of various sizes
     * @param icon the icon to be stored
     * @param path the URL of the icon
     * @param id the size of the icon
     * @return the icon that was created at the "id" size
     */
   public static IconEntry register(final String    iconName, 
                                    final ImageIcon icon, 
                                    final URL       path, 
                                    final IconSize  id)
    {
        if (instance.iconListForType != null && instance.defaultEntries.get(iconName) != null)
        {
            log.error("Icon name is already registered["+iconName+"]");
            return null;
        }
        
        if (icon != null)
        {
            //log.debug(iconName+"  "+id+"  "+path);
            IconEntry entry = new IconEntry(iconName, id, path);
            if (path == null)
            {
                entry.setIcon(icon);
            }
            
            instance.defaultEntries.put(iconName, entry);
            
            if (instance.iconListForType != null)
            {
                instance.iconListForType.add(iconName);
            }
        	
            return entry;
        }
        // else
        log.error("Can't register null icon name["+iconName+"] Size:"+id.toString());
        return null;
    }
    
    /**
     * @param size
     * @param bw
     * @param faded
     * @return
     */
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
                case 20 : return IconSize.Std20;
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
    public static ImageIcon getScaledIcon(final ImageIcon icon, 
                                          final IconSize  iconSize, 
                                          final IconSize  scaledIconSize)
    {
        IconEntry entry = null;
        // Find current Entry
        for (IconEntry et : instance.defaultEntries.values())
        {
            if (et.getIcon() == icon)
            {
                entry = et;
                break;
            }
        }
        
        if (entry != null)
        {
            return entry.getIcon(scaledIconSize);
        }
        
        if (icon != null)
        {
        	return createNewScaledIcon(icon, iconSize, scaledIconSize);
        } 
        
        // else
        log.error("Couldn't find icon [" + iconSize + "] to scale to [" + scaledIconSize + "]");
        return null;
    }
    
    /**
     * @param icon
     * @param iconSize
     * @param scaledIconSize
     * @return
     */
    public static ImageIcon createNewScaledIcon(final ImageIcon icon, 
                                                final IconSize  iconSize, 
                                                final IconSize  scaledIconSize) 
    {
        return new ImageIcon(instance.getFastScale(icon, iconSize, scaledIconSize));
    }

    /**
     * Returns an icon of a specified size
     * @param iconName the name to find (really a category)
     * @param id the size ID
     * @return the icon
     */
    public static ImageIcon getIcon(final String iconName, final IconSize id)
    {
        IconEntry entry = instance.defaultEntries.get(iconName);
        if (entry != null)
        {
            return entry.getIcon(id);
        }
        return null;
    }
    
    /**
     * Gets an icon as it's "base" size or meaning its original size.
     * @param iconName the name of the icon
     * @return the un-sized icon
     */
    public static ImageIcon getIcon(final String iconName)
    {
        if (iconName == null)
        {
            throw new NullPointerException("icon name should not be null!");
        }

        //create icon 
        
        IconEntry entry = instance.defaultEntries.get(iconName);
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
            case 20:return IconSize.Std20;
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
        loadIcons(XMLHelper.getConfigDir("icons.xml"));
    }

    /**
     * Loads icons from config file
     *
     */
    public static void loadIcons(final File iconFile)
    {
        try
        {
            Element root = XMLHelper.readFileToDOM4J(iconFile);
            if (root != null)
            {
                Hashtable<String, String> aliases = new Hashtable<String, String>();
                Element iconsNode = (Element)root.selectSingleNode("/icons");
                String  type      = XMLHelper.getAttr(iconsNode, "type", null);
                String  subdir    = XMLHelper.getAttr(iconsNode, "subdir", null);
                if (StringUtils.isNotEmpty(type))
                {
                    if (instance.iconSets.get(type) == null)
                    {
                        instance.iconListForType = new Vector<String>();
                        instance.iconSets.put(type, instance.iconListForType);
                    } else
                    {
                        log.debug("Type ["+type+"] has already been loaded.");
                    }
                }
                
                if (StringUtils.isNotEmpty(subdir))
                {
                    subdirPath = subdir + "/";
                } else
                {
                    subdirPath = null; 
                }
                
                List<?> boxes = root.selectNodes("/icons/icon");
                for ( Iterator<?> iter = boxes.iterator(); iter.hasNext(); )
                {
                    org.dom4j.Element iconElement = (org.dom4j.Element) iter.next();

                    String name  = iconElement.attributeValue("name");
                    String sizes = iconElement.attributeValue("sizes");
                    String file  = iconElement.attributeValue("file");
                    String alias = iconElement.attributeValue("alias");
                    
                    if (StringUtils.isNotEmpty(alias))
                    {
                        aliases.put(name, alias);

                    } else if (sizes == null || sizes.length() == 0 || sizes.toLowerCase().equals("all"))
                    {
                        
                        //log.info("["+name+"]["+sizes+"]["+file+"]");
                    	//this is the cache of the icons, i want to just cache filename
                        /*IconEntry entry = register(name, file, IconManager.IconSize.Std32);
                        if (entry != null)
                        {
                           entry.addScaled( IconSize.Std32, IconSize.Std24);
                           entry.addScaled( IconSize.Std32, IconSize.Std16); 
                        }*/
                    	//---------do not need to addScaled, the image will scale when it is needed
                    	
                    	register(name, file, IconManager.IconSize.Std32);
                    	
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
                    IconEntry entry = instance.defaultEntries.get(aliases.get(name));
                    if (entry != null)
                    {
                        instance.defaultEntries.put(name, entry);
                    }
                    //makeAlias(aliases.get(name), name);
                }
            } else
            {
                log.debug("Couldn't open icons.xml");
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IconManager.class, ex);
            ex.printStackTrace();
            log.error(ex);
        }
        
        subdirPath               = null; 
        instance.iconListForType = null;
    }
    
    /**
     * Makes a new IconEntry in the manager 
     * @param iconName
     * @param aliasName
     */
    public static void makeAlias(final String iconName, 
                                 final String aliasName)
    {
        IconEntry entry = instance.defaultEntries.get(iconName);
        if (entry != null)
        {
            IconEntry aliasEntry = new IconEntry(entry.getName(), entry.getSize(), entry.getUrl(), true, entry.getIcons());
            instance.defaultEntries.put(aliasName, aliasEntry);
        }
    }
    
    /**
     * @param iconName
     * @param aliasName
     */
    public static void aliasImages(final String iconName, 
                                   final String aliasName)
    {
        IconEntry entry = instance.defaultEntries.get(iconName);
        if (entry != null)
        {
            IconEntry aliasEntry = instance.defaultEntries.get(aliasName);
            if (entry != null)
            {
                aliasEntry.setIcon(entry.getIcon());
                aliasEntry.setIcons(entry.getIcons());
                aliasEntry.setUrl(entry.getUrl());
                aliasEntry.setSize(entry.getSize());
                
            } else
            {
                log.error("Couldn't find icon entry["+aliasName+"] (destination of images)");
            }
        } else
        {
            log.error("Couldn't find icon entry["+iconName+"] (source of images)");
        }
    }
    
    /**
     * Create a new list of icons for a given type and size
     * @param type the type of icon (external set of icons)
     * @param size the size to return
     * @return the list of icons for a given type.
     */
    public static List<Pair<String, ImageIcon>> getListByType(final String type, final IconSize size)
    {
        Vector<String> nameList = instance.iconSets.get(type);
        if (nameList != null)
        {
            List<Pair<String, ImageIcon>> icons = new Vector<Pair<String, ImageIcon>>(); 
            for (String key : nameList)
            {
                ImageIcon ii = getIcon(key, size);
                if (ii != null)
                {
                    icons.add(new Pair<String, ImageIcon>(key, ii));
                }
            }
            return icons;
        }
        return null;
    }
    
    /**
     * @param icon
     * @param size
     * @return
     */
    public static URL getURLForIcon(final ImageIcon icon, final IconManager.IconSize size)
    {
        for (IconEntry entry : instance.defaultEntries.values())
        {
            for (IconEntry.IconSizeEntry sizeEntry : entry.getIcons().values())
            {
                if (sizeEntry.isIconAvailable() && icon == sizeEntry.getImageIcon())
                {
                    return entry.getUrl();
                }
            }
        }
        return null;
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
        g.dispose();
        return icon;
    }

    /**
     * Creates a Black and White image from the color
     * @param img the image to be converted
     * @return new B&W image
     */
    public static ImageIcon createFadedImage(final ImageIcon icon)
    {
        Image image = GrayFilter.createDisabledImage(icon.getImage());
        return new ImageIcon(image);
        
        /*BufferedImage bi = new BufferedImage(img.getIconWidth(), img.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        g.drawImage(img.getImage(), 0, 0, null);
        g.setColor(new Color(255, 255, 255, 128));
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        ImageIcon icon = new ImageIcon(bi);
        g.dispose();
        return icon;*/
    }

    /**
     * Returns an URL for the path to the image in the "images" directory that is relative to the application class.
     * <br> For example &lt;app class&gt;/images/&lt;file name&gt;
     * @param imageName the image name
     * @return Returns an URL for the path to the image
     */
    public static URL getImagePath(final String imageName)
    {
        return instance.appClass.getResource(relativePath + (subdirPath != null ? subdirPath : "") + imageName);
    }
    
    /**
     * @param iconName
     * @return
     */
    public static IconEntry getIconEntryByName(final String iconName)
    {
        return instance.defaultEntries.get(iconName);
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
     * @param id the size to be returned
     * @return Returns a Standard Size icon
     */
    public static ImageIcon getImage(final String imageName, final IconSize id)
    {
        return getIcon(imageName, id);
    }

   
  
    /**
     * Gets a scaled icon and if it doesn't exist it creates one and scales it
     * @param icon image to be scaled
     * @param iconSize the icon size (Std)
     * @param scaledIconSize the new scaled size in pixels
     * @return the scaled icon
     */
    public Image getFastScale(final ImageIcon icon, final IconSize iconSize, final IconSize scaledIconSize)
    {
    	if (icon != null)
    	{
    		int width = scaledIconSize.size();
    		int height = scaledIconSize.size();
    				
    		if ((width < 0) || (height < 0))
    		{	//image is nonstd, revert to original size
    			width = icon.getIconWidth();
    			height = icon.getIconHeight();
    		}
    		
    		Image imgMemory = createImage(icon.getImage().getSource());
    		//make sure all pixels in the image were loaded
    		imgMemory = new ImageIcon(imgMemory).getImage();
    		
    		BufferedImage thumbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    		
			Graphics2D    graphics2D = thumbImage.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
										RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2D.drawImage(imgMemory, 0, 0, 
								width, 
								height, null);
			graphics2D.dispose();
			
			imgMemory = thumbImage;
			return imgMemory;
			
    	}
    	//else
    	log.error("Couldn't find icon [" + iconSize + "] to scale to [" + scaledIconSize + "]");
    	return null;
    }
    
    
    /**
     * @return an icon name with 'E' or 'M' for embedded or mobile
     */
    public static String makeIconName(final String baseName)
    {
        String postFix = "";
        if (UIRegistry.isEmbedded())
        {
            postFix = "E";
        } else if (UIRegistry.isMobile())
        {
            postFix = "M";
        }
        return baseName + postFix;
    }
 
}


