/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.helpers;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.io.File;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

/**
 * Class for filtering image types by extension.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * May 12, 2007
 *
 */
public class ImageFilter extends FileFilter implements java.io.FilenameFilter
{
    public final static String jpeg = "jpeg"; //$NON-NLS-1$
    public final static String jpg  = "jpg"; //$NON-NLS-1$
    public final static String gif  = "gif"; //$NON-NLS-1$
    public final static String png  = "png"; //$NON-NLS-1$
    
    protected static String[] types = {jpeg, jpg, gif, png};
    
    // Unsupported at this time
    public final static String tiff = "tiff"; //$NON-NLS-1$
    public final static String tif  = "tif"; //$NON-NLS-1$
    public final static String bmp  = "bmp"; //$NON-NLS-1$
    
    protected Hashtable<String, Boolean> hash = new Hashtable<String, Boolean>();
    
    /**
     * Constructor.
     */
    public ImageFilter()
    {
        for (String type : types)
        {
            hash.put(type, true);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f)
    {
        if (f.isDirectory()) { return true; }

        return isImageFile(f.getAbsolutePath());
    }
    
    /* (non-Javadoc)
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    public boolean accept(File dir, String filename)
    {
        return isImageFile(filename);
    }
    
    /**
     * Returns true if the file type is ok.
     * @param filename the name of the file
     * @return true is ok, false is not
     */
    public boolean isImageFile(final String filename)
    {
        String extension = FilenameUtils.getExtension(filename);
        if (extension != null)
        {
            return hash.get(extension.toLowerCase()) != null;
        }
        return false;
    }

    //The description of this filter
    public String getDescription()
    {
        return getResourceString("ImageFilter.IMAGES"); //$NON-NLS-1$
    }
}