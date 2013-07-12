/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.ui.skin;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.helpers.XMLHelper;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Dec 19, 2008
 *
 */
public class SkinsMgr
{
    protected static SkinsMgr instance = new SkinsMgr();
    protected static boolean  hasSkins;
    
    // Transient
    protected Skin            currentSkin = null;
    
    /**
     * 
     */
    protected SkinsMgr()
    {
        load();
        
        File skinsFile = new File(XMLHelper.getConfigDirPath("skins.xml"));
        hasSkins = skinsFile.exists();
    }
    
    /**
     * @return the singleton
     */
    public static SkinsMgr getInstance()
    {
        return instance;
    }
    
    /**
     * @return the hasSkins
     */
    public static boolean hasSkins()
    {
        return hasSkins;
    }

    /**
     * @return
     */
    public static Skin getCurrentSkin()
    {
        return instance.currentSkin;
    }
    
    /**
     * @param name
     * @return
     */
    public static SkinItem getSkinItem(final String itemName)
    {
        if (instance.currentSkin != null)
        {
            return instance.currentSkin.getItem(itemName);
        }
        return null;
    }
    
    /**
     * @return whether there is a background image for the current skin or whether the current
     */
    public static boolean shouldBeOpaque(final SkinItem item)
    {
        return !doesCurrentHaveBG() || (item != null && item.isOpaque());
    }
    
    /**
     * @return whether there is a background image for this skin
     */
    public static boolean doesCurrentHaveBG()
    {
        if (instance.currentSkin != null)
        {
            return instance.currentSkin.hasBG();
        }
        return false;
    }
    
    /**
     * Gets a skin.
     * @param skinName the name of the skin
     * @return the skin object
     */
    public Skin setSkin(final String skinName)
    {
        if (hasSkins)
        {
            if (currentSkin == null || !currentSkin.getName().equals(skinName))
            {
                if (currentSkin != null)
                {
                    currentSkin.unregister();
                }
                currentSkin = null;
                Hashtable<String, Skin> skinsHash = load();
                if (skinsHash != null)
                {
                    currentSkin = skinsHash.get(skinName);
                    if (currentSkin != null)
                    {
                        currentSkin.register();
                    }
                }
            }
        }
        return currentSkin;
    }
    
    /**
     * @return
     */
    public List<Skin> getSkinList()
    {
        List<Skin> skinsList = new Vector<Skin>();
        
        if (hasSkins)
        {
            Hashtable<String, Skin> skinHash = load();
            skinsList.addAll(skinHash.values());
            Collections.sort(skinsList);
        }
        
        return skinsList;
    }
    
    /**
     * Loads the skins from XML.
     */
    @SuppressWarnings("unchecked")
    protected static Hashtable<String, Skin> load()
    {
        XStream xstream = new XStream();
        config(xstream);
        
        if (false)
        {
            Hashtable<String, Skin> skinsHash = new Hashtable<String, Skin>();
            
            /*
             giraffe_tile.png
            giraffe_tile1.png
            metal.jpg
            reptile_tiled.jpg
            reptile_tiled.png
             */
            String p = "/Users/rods/workspace/Specify6/";
            Skin skin1 = new Skin("metal", "metal desc");
            skin1.getItems().put("item1", new SkinItem("n1", "fn", 10, "plain", Color.BLACK, Color.MAGENTA, p+"giraffe_tile.png", "desc"));
            skin1.getItems().put("item2", new SkinItem("n2", "fn", 10, "bold", Color.BLACK, Color.ORANGE, p+"giraffe_tile.png", "desc"));
            
            Skin skin2 = new Skin("giraffe", "metal desc");
            skin2.getItems().put("item1", new SkinItem("n1", "fn", 10, "bolditalic", Color.GREEN, Color.YELLOW, p+"metal.jpg", "desc"));
            skin2.getItems().put("item2", new SkinItem("n2", "fn", 10, "italic", Color.BLUE, Color.RED, p+"metal.jpg", "desc"));
            
            skinsHash.put(skin1.getName(), skin1);
            skinsHash.put(skin2.getName(), skin2);
            
            File skinsFile = new File(XMLHelper.getConfigDirPath("skins.xml"));
            try
            {
                FileUtils.writeStringToFile(skinsFile, xstream.toXML(skinsHash));
            } catch (IOException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SkinsMgr.class, ex);
                ex.printStackTrace();
            }
        }
        
        File skinsFile = new File(XMLHelper.getConfigDirPath("skins.xml"));
        if (skinsFile.exists())
        {
            return (Hashtable<String, Skin>)xstream.fromXML(XMLHelper.getContents(skinsFile));
        }
        return null;
    }
    
    /**
     * @param xstream
     */
    protected static void config(final XStream xstream)
    {
        xstream.alias("skins", SkinsMgr.class); //$NON-NLS-1$
        
        Skin.config(xstream);
    }
 
}
