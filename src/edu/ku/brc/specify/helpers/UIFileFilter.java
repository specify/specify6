/**
 * '$RCSfile: UIFileFilter,v $'
 * 
 * '$Author: rods $' '$Date: 2004/07/09 08:35:25 $' '$Revision: 1.0 $'
 * 
 * For Details: http://kepler.ecoinformatics.org
 * 
 * Copyright (c) 2003 The Regents of the University of California. All rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license or royalty fees, to
 * use, copy, modify, and distribute this software and its documentation for any purpose, provided
 * that the above copyright notice and the following two paragraphs appear in all copies of this
 * software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 * DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */

package edu.ku.brc.specify.helpers;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

/**
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

    /**
     * Return true if this file should be shown in the directory pane, false if it shouldn't.
     * 
     * Files that begin with "." are ignored.
     * 
     * @see #getExtension
     * @see FileFilter#accepts
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

    /**
     * Returns the human readable description of this filter. For example: "JPEG and GIF Image
     * Files (*.jpg, *.gif)"
     * 
     * @see setDescription
     * @see setExtensionListInDescription
     * @see isExtensionListInDescription
     * @see FileFilter#getDescription
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
     * 
     * @see setDescription
     * @see setExtensionListInDescription
     * @see isExtensionListInDescription
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
     * 
     * @see getDescription
     * @see setDescription
     * @see isExtensionListInDescription
     */
    public void setExtensionListInDescription(final boolean b)
    {
        useExtensionsInDescription = b;
        fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     * @see getDescription
     * @see setDescription
     * @see setExtensionListInDescription
     */
    public boolean isExtensionListInDescription()
    {
        return useExtensionsInDescription;
    }
}