/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui.skin;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.ui.JTiledPanel;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 19, 2008
 *
 */
public class SkinItem
{
    protected String name       = null;
    protected String fontName   = null;
    protected int    fontSize   = -1;
    protected String fontStyle  = null;
    protected Color  fgColor    = null;
    protected Color  bgColor    = null;
    protected String imagePath  = null;
    protected String desc       = null;
    
    // Transient
    protected Image  bgImage = null;
    protected Font   font    = null;
    
    protected Hashtable<String, Object> cacheHash = new Hashtable<String, Object>();

    /**
     * 
     */
    public SkinItem()
    {
        super();
    }
   
    /**
     * @param name
     * @param fontName
     * @param fontSize
     * @param fontStyle
     * @param fgColor
     * @param bgColor
     * @param imagePath
     * @param desc
     */
    public SkinItem(String name, 
                    String fontName, 
                    int    fontSize, 
                    String fontStyle, 
                    Color  fgColor, 
                    Color  bgColor, 
                    String imagePath,
                    String desc)
    {
        super();
        this.name      = name;
        this.fontName  = fontName;
        this.fontSize  = fontSize;
        this.fontStyle = fontStyle;
        this.fgColor   = fgColor;
        this.bgColor   = bgColor;
        this.imagePath = imagePath;
        this.desc      = desc;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the fontName
     */
    public String getFontName()
    {
        return fontName;
    }

    /**
     * @param fontName the fontName to set
     */
    public void setFontName(String fontName)
    {
        this.fontName = fontName;
    }

    /**
     * @return the fontSize
     */
    public int getFontSize()
    {
        return fontSize;
    }

    /**
     * @param fontSize the fontSize to set
     */
    public void setFontSize(int fontSize)
    {
        this.fontSize = fontSize;
    }

    /**
     * @return the fontStyle
     */
    public String getFontStyle()
    {
        return fontStyle;
    }

    /**
     * @param fontStyle the fontStyle to set
     */
    public void setFontStyle(String fontStyle)
    {
        this.fontStyle = fontStyle;
    }

    /**
     * @return the fgColor
     */
    public Color getFgColor()
    {
        return fgColor;
    }

    /**
     * @param fgColor the fgColor to set
     */
    public void setFgColor(Color fgColor)
    {
        this.fgColor = fgColor;
    }

    /**
     * @return the bgColor
     */
    public Color getBgColor()
    {
        return bgColor;
    }

    /**
     * @param bgColor the bgColor to set
     */
    public void setBgColor(Color bgColor)
    {
        this.bgColor = bgColor;
    }

    /**
     * @return the imagePath
     */
    public String getImagePath()
    {
        return imagePath;
    }

    /**
     * @param imagePath the imagePath to set
     */
    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    /**
     * @return the desc
     */
    public String getDesc()
    {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc)
    {
        this.desc = desc;
    }
    
    /**
     * @return whether the item should be opaque
     */
    public boolean isOpaque()
    {
        return !SkinsMgr.doesCurrentHaveBG() && (bgColor != null || StringUtils.isNotEmpty(imagePath));
    }
    
    /**
     * @param comp
     */
    public void setupPanel(final JComponent comp)
    {
        boolean isOpaque = bgColor != null || StringUtils.isNotEmpty(imagePath);
        if (isOpaque)
        {
            comp.setOpaque(isOpaque);
        }
        
        if (bgColor != null)
        {
            comp.setBackground(bgColor);
        }
        
        if (comp instanceof JTiledPanel && (bgImage != null || StringUtils.isNotEmpty(imagePath)))
        {
            ((JTiledPanel)comp).setTileImage(getBGImage());
        }
    }
    
    /**
     * Constructs the font or return null.
     * @return return the font or null
     */
    public Font getFont()
    {
        if (font == null && StringUtils.isNotEmpty(fontName) && fontSize > 0)
        {
            int style = Font.PLAIN;
            if (StringUtils.isNotEmpty(fontStyle))
            {
                if (fontStyle.equalsIgnoreCase("plain"))
                {
                    style = Font.PLAIN;
                    
                } else if (fontStyle.equalsIgnoreCase("bold"))
                {
                    style = Font.BOLD;
                    
                } else if (fontStyle.equalsIgnoreCase("italic"))
                {
                    style = Font.ITALIC;
                    
                } else if (fontStyle.equalsIgnoreCase("bolditalic"))
                {
                    style = Font.BOLD | Font.ITALIC;
                }
            }
            font = new Font(fontName, style, fontSize);
        }
        return font;
    }
    
    /**
     * @return
     */
    public Image getBGImage()
    {
        if (bgImage == null)
        {
            if ((new File(imagePath)).exists())
            {
                ImageIcon imgIcon = new ImageIcon(imagePath);
                bgImage = imgIcon.getImage();
            }
        }
        return bgImage;
    }
    
    /**
     * @param nm
     */
    public void pushFG(final String nm)
    {
        if (fgColor != null)
        {
            if (cacheHash == null)
            {
                cacheHash = new Hashtable<String, Object>();
            }
            cacheHash.put(nm, UIManager.get(nm));
            UIManager.put(nm, new ColorUIResource(fgColor));
        }
    }
    
    /**
     * @param nm
     */
    public void popFG(final String nm)
    {
        if (fgColor != null)
        {
            Object obj = cacheHash.get(nm);
            UIManager.put(nm, obj);
        }
    }
    
    /**
     * 
     */
    public void register()
    {
        if (getFont() != null)
        {
            UIManager.put(name+'.'+"font", new FontUIResource(font));
        }
        if (bgColor != null)
        {
            UIManager.put(name+'.'+"bgcolor", new ColorUIResource(bgColor));
        }
        if (fgColor != null)
        {
            UIManager.put(name+'.'+"fgcolor", new ColorUIResource(fgColor));
        }
        if (getBGImage() != null)
        {
            UIManager.put(name+'.'+"bgimage", bgImage);
        }
    }

    /**
     * 
     */
    public void unregister()
    {
        String[] propNames = {"font", "bgcolor", "fgcolor", "bgimage"};
        for (String pn : propNames)
        {
            UIManager.put(name+'.'+pn, null);
        }
    }
    
    /**
     * @param xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("skinitem", SkinItem.class); //$NON-NLS-1$
        
        xstream.useAttributeFor(SkinItem.class, "name"); //$NON-NLS-1$
        xstream.useAttributeFor(SkinItem.class, "fontName"); //$NON-NLS-1$
        xstream.useAttributeFor(SkinItem.class, "fontSize"); //$NON-NLS-1$
        xstream.useAttributeFor(SkinItem.class, "fontStyle"); //$NON-NLS-1$
        xstream.useAttributeFor(SkinItem.class, "fgColor"); //$NON-NLS-1$
        xstream.useAttributeFor(SkinItem.class, "bgColor"); //$NON-NLS-1$
        //xstream.useAttributeFor(SkinItem.class, "imagePath"); //$NON-NLS-1$
        
        xstream.omitField(SkinItem.class, "bgImage"); //$NON-NLS-1$
        xstream.omitField(SkinItem.class, "cacheHash"); //$NON-NLS-1$
        
        xstream.aliasAttribute(SkinItem.class, "name",      "name"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(SkinItem.class, "fontName",  "fontname"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(SkinItem.class, "fontSize",  "fontsize"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(SkinItem.class, "fontStyle",  "fontstyle"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(SkinItem.class, "fgColor",   "fg"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(SkinItem.class, "bgColor",   "bg"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasField("image", SkinItem.class, "imagePath");
    }
 
}
