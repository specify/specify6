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
package edu.ku.brc.specify.tools.schemalocale;

import java.util.List;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Sep 28, 2007
 *
 */
public interface LocalizableItemIFace
{
    /**
     * @return
     */
    public abstract String getName();
    
    /**
     * @param name
     */
    public abstract void setName(String name);
    
    /**
     * @return
     */
    public abstract String getType();
    
    /**
     * @param type
     */
    public abstract void setType(String type);
    
    /**
     * @return
     */
    public abstract Boolean getIsHidden();
    
    /**
     * @param isHidden the isHidden to set
     */
    public abstract void setIsHidden(Boolean isHidden);

    /**
     * @param str
     */
    public abstract void addDesc(LocalizableStrIFace str);
    
    /**
     * @param str
     */
    public abstract void removeDesc(LocalizableStrIFace str);
    
    /**
     * @param descs
     */
    public abstract void fillDescs(List<LocalizableStrIFace> descs);

    /**
     * @param str
     */
    public abstract void addName(LocalizableStrIFace str);
    
    /**
     * @param str
     */
    public abstract void removeName(LocalizableStrIFace str);
    
    /**
     * @param names
     */
    public abstract void fillNames(List<LocalizableStrIFace> names);
    
    /**
     * 
     */
    public abstract String getFormat();
    
    /**
     * 
     */
    public abstract void setFormat(String format);
    
    /**
     * @param isUIFormatter
     */
    public abstract void setIsUIFormatter(Boolean isUIFormatter);
    
    /**
     * @return
     */
    public abstract Boolean getIsUIFormatter();
    
    /**
     * @return
     */
    public abstract Boolean getIsRequired();
    
    /**
     * @param required
     */
    public abstract void setIsRequired(Boolean required);
    
    /**
     * @return the pickListName
     */
    public abstract String getPickListName();

    /**
     * @param pickListName the pickListName to set
     */
    public abstract void setPickListName(String pickListName);

    /**
     * @return
     */
    public abstract String getWebLinkName();

    /**
     * @param webLinkName the webLinkName to set
     */
    public abstract void setWebLinkName(String webLinkName);
}
