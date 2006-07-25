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

/**
 * Simple class for filtering by an extension. If you want to filter by *.xml" then pass in "xml".
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class DiskFileFilter implements java.io.FileFilter 
{
    protected String ext;
    
    /**
     * Constructs with an extension
     * @param ext the extension with no period like "xml"
     */
    public DiskFileFilter(final String ext)
    {
        this.ext = ext;
    }
    
    /* (non-Javadoc)
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) 
    {
        return f.getName().toLowerCase().endsWith(ext);
    }
}