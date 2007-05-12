/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.helpers;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * May 12, 2007
 *
 */
public class ImageFilter extends FileFilter implements java.io.FilenameFilter
{
    public final static String jpeg = "jpeg";
    public final static String jpg  = "jpg";
    public final static String gif  = "gif";
    public final static String tiff = "tiff";
    public final static String tif  = "tif";
    public final static String png  = "png";
    public final static String bmp  = "bmp";


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
     * returns true if the file type is ok
     * @param filename the name of the file
     * @return true is ok, false is not
     */
    protected boolean isImageFile(final String filename)
    {
        String extension = FilenameUtils.getExtension(filename);
        if (extension != null)
        {
            if (extension.equals(gif) ||
                extension.equals(jpeg) || 
                extension.equals(jpg) || 
                extension.equals(png)
                //extension.equals(tiff) || 
                //extension.equals(tif) ||
                //extension.equals(BMP)
                )
            {
                return true;
            }
        }
        return false;
    }

    //The description of this filter
    public String getDescription()
    {
        return UIRegistry.getResourceString("Images");
    }
}