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
package edu.ku.brc.ui;

import java.util.Hashtable;

import javax.swing.ImageIcon;

/**
 * This is a class that manages a mapping between MIME types and {@link ImageIcon}s
 * that represent the type.
 *
 * @author jstewart
 * @code_status Complete
 */
public class MimeTypeIconProvider
{
    /** The data member holding the actual mapping. */
    protected Hashtable<String, ImageIcon> mimeTypeToIconMap;
    
    /** A singleton instance of the mapper. */
    protected static MimeTypeIconProvider instance = null;
    
    /**
     * Creates a new mapper, containing no mappings.
     */
    protected MimeTypeIconProvider()
    {
        mimeTypeToIconMap = new Hashtable<String, ImageIcon>();
    }
    
    /**
     * Gets the singleton instance of the mapper.
     * 
     * @return the instance
     */
    public static synchronized MimeTypeIconProvider getInstance()
    {
        if( instance == null )
        {
            instance = new MimeTypeIconProvider();
        }
        return instance;
    }
    
    /**
     * Registers a MIME type/ImageIcon mappping with the mapping system.
     * 
     * @param mimeType the MIME type
     * @param icon the representative ImageIcon
     */
    public void registerMimeType(String mimeType, ImageIcon icon)
    {
        mimeTypeToIconMap.put(mimeType, icon);
    }
    
    /**
     * Gets the representative ImageIcon for the given MIME type.
     * 
     * @param mimeType the MIME type of interest
     * @return the representative icon
     */
    public ImageIcon getIconForMimeType(String mimeType)
    {
        return mimeTypeToIconMap.get(mimeType);
    }
}
