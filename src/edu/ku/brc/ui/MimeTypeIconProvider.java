/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
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
