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
    public final static String tiff = "tiff"; //$NON-NLS-1$
    public final static String tif  = "tif"; //$NON-NLS-1$
    
    protected static String[] types = {jpeg, jpg, gif, png, tif, tiff};
    
    // Unsupported at this time
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
    @Override
    public boolean accept(File f)
    {
        if (f.isDirectory()) { return true; }

        return isImageFile(f.getAbsolutePath());
    }
    
    /* (non-Javadoc)
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    @Override
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
    @Override
    public String getDescription()
    {
        return getResourceString("ImageFilter.IMAGES"); //$NON-NLS-1$
    }
}
