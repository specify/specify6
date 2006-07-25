/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.helpers;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

/*
 * @code_status Unknown (auto-generated)
 **
 * @author Rod Spears
 * 
 * Adapted from the Swing JFileChooser demo and the ExampleFileFilter file
 */
public class UIFileFilter extends FileFilter
{
    private Hashtable<String, UIFileFilter> filters                    = null;
    private String                          description                = null;
    private String                          fullDescription            = null;
    private boolean                         useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all files are accepted.
     * 
     * @see #addExtension
     */
    public UIFileFilter()
    {
        this.filters = new Hashtable<String, UIFileFilter>();
    }

    /**
     * Creates a file filter that accepts files with the given extension. Example: new
     * ExampleFileFilter("jpg");
     * 
     * @see #addExtension
     */
    public UIFileFilter(final String extension)
    {
        this(extension, null);
    }

    /**
     * Creates a file filter that accepts the given file type. Example: new ExampleFileFilter("jpg",
     * "JPEG Image Images");
     * 
     * Note that the "." before the extension is not needed. If provided, it will be ignored.
     * 
     * @see #addExtension
     */
    public UIFileFilter(final String extension, final String description)
    {
        this();
        if (extension != null)
        {
            addExtension(extension);
        }

        if (description != null)
        {
            setDescription(description);
        }
    }

    /**
     * Creates a file filter from the given string array. Example: new ExampleFileFilter(String
     * {"gif", "jpg"});
     * 
     * Note that the "." before the extension is not needed adn will be ignored.
     * 
     * @see #addExtension
     */
    public UIFileFilter(final String[] filters)
    {
        this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description. Example: new
     * UIFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     * 
     * Note that the "." before the extension is not needed and will be ignored.
     * 
     * @see #addExtension
     */
    public UIFileFilter(final String[] filterStr, final String description)
    {
        this();
        for (int i = 0; i < filterStr.length; i++)
        {
            // add filters one by one
            addExtension(filterStr[i]);
        }
        if (description != null)
        {
            setDescription(description);
        }
    }


    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f)
    {
        if (f != null)
        {
            if (f.isDirectory()) { return true; }
            String extension = getExtension(f);
            if (extension != null && filters.get(getExtension(f)) != null) { return true; }
            ;
        }
        return false;
    }

    /**
     * Return the extension portion of the file's name .
     * 
     * @param f the file to get the extension of
     * @see #getExtension
     * @see FileFilter#accept
     */
    public String getExtension(File f)
    {
        if (f != null)
        {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1) { return filename.substring(i + 1)
                    .toLowerCase(); }
            ;
        }
        return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against.
     * 
     * For example: the following code will create a filter that filters out all files except those
     * that end in ".jpg" and ".tif":
     * 
     * ExampleFileFilter filter = new ExampleFileFilter(); filter.addExtension("jpg");
     * filter.addExtension("tif");
     * 
     * Note that the "." before the extension is not needed and will be ignored.
     */
    public void addExtension(final String extension)
    {
        if (filters == null)
        {
            filters = new Hashtable<String, UIFileFilter>(5);
        }
        filters.put(extension.toLowerCase(), this);
        fullDescription = null;
    }

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription()
    {
        if (fullDescription == null)
        {
            if (description == null || isExtensionListInDescription())
            {
                fullDescription = description == null ? "(" : description + " (";
                // build the description from the extension list
                Enumeration extensions = filters.keys();
                if (extensions != null)
                {
                    fullDescription += "." + (String) extensions.nextElement();
                    while (extensions.hasMoreElements())
                    {
                        fullDescription += ", ." + (String) extensions.nextElement();
                    }
                }
                fullDescription += ")";
            } else
            {
                fullDescription = description;
            }
        }
        return fullDescription;
    }

   /**
     * Sets the human readable description of this filter. For example: filter.setDescription("Gif
     * and JPG Images");
     * @param description the description
     */
    public void setDescription(final String description)
    {
        this.description = description;
        fullDescription = null;
    }

    /**
     * Determines whether the extension list (.jpg, .gif, etc) should show up in the human readable
     * description.
     * 
     * Only relevent if a description was provided in the constructor or using setDescription();
     * @param b indicates whether to use the description
     * 
     */
    public void setExtensionListInDescription(final boolean b)
    {
        useExtensionsInDescription = b;
        fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description. Only relevent if a description was provided in the constructor
     * or using setDescription();
     * @return hether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     */
    public boolean isExtensionListInDescription()
    {
        return useExtensionsInDescription;
    }
}
