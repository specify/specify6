/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.util.Hashtable;

import com.thoughtworks.xstream.XStream;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 19, 2008
 *
 */
public class Skin implements Comparable<Skin>
{

    protected String                      name       = null;
    protected String                      desc       = null;
    protected Hashtable<String, SkinItem> items      = null;
    
    // Transient 
    protected Boolean                     hasBG      = false;
    
    /**
     * 
     */
    public Skin()
    {
        super();
    }
    
    /**
     * @param name
     * @param desc
     */
    public Skin(String name, String desc)
    {
        super();
        this.name = name;
        this.desc = desc;
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
     * @return the items
     */
    public Hashtable<String, SkinItem> getItems()
    {
        if (items == null)
        {
            items = new Hashtable<String, SkinItem>();
        }
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(Hashtable<String, SkinItem> items)
    {
        this.items = items;
    }
    
    /**
     * @param itemName the name of the item
     * @return the skin item
     */
    public SkinItem getItem(final String itemName)
    {
        SkinItem item = items.get(itemName);
        if (item == null && !itemName.equals("bg"))
        {
            return items.get("bg");
        }
        return item;
    }
    
    /**
     * @return
     */
    public boolean hasBG()
    {
        if (hasBG == null)
        {
            hasBG = getItem("bg") != null;
        }
        return hasBG;
    }
    
    /**
     * 
     */
    public void register()
    {
        for (SkinItem item : items.values())
        {
            item.register();
        }
    }

    public void unregister()
    {
        for (SkinItem item : items.values())
        {
            item.unregister();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Skin obj)
    {
        return name.compareTo(obj.name);
    }

    /**
     * @param xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("skin", Skin.class); //$NON-NLS-1$
        
        xstream.useAttributeFor(Skin.class, "name"); //$NON-NLS-1$
        
        xstream.omitField(Skin.class, "hasBG"); //$NON-NLS-1$

        SkinItem.config(xstream);
    }

}
