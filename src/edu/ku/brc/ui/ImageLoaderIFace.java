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

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 4, 2012
 *
 */
public interface ImageLoaderIFace
{
    // Error Code
    public static final int kImageOK          = 0;
    public static final int kError            = 1;
    public static final int kHttpError        = 2;
    public static final int kInterruptedError = 3;
    public static final int kIOError          = 4;
    public static final int kURLError         = 5;

    /**
     * Called on background thread
     */
    public abstract void load();
    
    /**
     * Not on UI Thread
     */
    public abstract void done();
    
    /**
     * @return
     */
    public abstract int getStatus();
    
    /**
     * Tells Image Loader to stop
     */
    public abstract void stopLoading();
    
    /**
     * Removes listeners and other internal memory items.
     */
    public abstract void cleanup();
}
