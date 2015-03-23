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
package edu.ku.brc.specify.tasks.subpane.images;

import java.io.File;

import javax.swing.ImageIcon;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 4, 2012
 *
 */
public interface ImageLoaderListener
{
    /**
     * @param imageName
     * @param mimeType
     * @param doLoadFullImage
     * @param scale
     * @param isError
     * @param imgIcon
     * @param localFile
     */
    public abstract void imageLoaded(String    imageName,
                                      String    mimeType,
                                      boolean   doLoadFullImage,
                                      int       scale,
                                      boolean   isError,
                                      ImageIcon imageIcon, 
                                      File      localFile);
    
    /**
     * Called if image was asked to stop loading.
     * @param imageName
     * @param doLoadFullImage
     */
    public abstract void imageStopped(String imageName, boolean doLoadFullImage);
}
