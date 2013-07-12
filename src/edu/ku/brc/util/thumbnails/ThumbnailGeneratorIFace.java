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
package edu.ku.brc.util.thumbnails;

import java.awt.Dimension;
import java.io.IOException;

/**
 * This interface defines the minimum capabilities required of a class capable
 * of generating thumbnails of various types of files.
 *
 * @code_status Alpha
 * @author jstewart
 */
public interface ThumbnailGeneratorIFace
{
    /**
     * Sets the maximum width / height of any visual thumbnails created.
     *
     * @param maxSize the maximum thumbnail size
     */
    public abstract void setMaxSize(Dimension maxSize);
    
    /**
     * Sets the maximum width / height of any visual thumbnails created.
     *
     * @param maxSize the maximum thumbnail size
     */
    public abstract void setMaxSize(int width, int height);
    
    /**
     * @return the maximum width and height
     */
    public abstract Dimension getMaxSize();
	
	/**
	 * Sets the maximum duration of any audio or video 'thumbnails' created.
	 *
	 * @param seconds the time length of the audio or video thumbnails created
	 */
	//public abstract void setMaxDuration(int seconds);

	/**
	 * Sets the quality factor for any thumbnailers that implement a configurable
	 * level of quality.
	 *
	 * @param percent the quality factor
	 */
	public abstract void setQuality(float percent);

	/**
	 * Returns an array of MIME types that are supported by this thumbnail generator.
	 *
	 * @return the array of supported MIME types
	 */
	public abstract String[] getSupportedMimeTypes();

	/**
	 * Create a thumbnail for the given original, placing the output in the given output file.
	 *
	 * @param originalFile the path to the original
     * @param thumbnailFile the path to the output thumbnail
     * @param doHighQuality true creates a high quality thumbnail (slow), false a low resolution thumbnail (fast)
     * @return true if thumbnail was create and false it is wasn't
	 * @throws IOException if any IO errors occur during generation or storing the output
	 */
	public abstract boolean generateThumbnail(String originalFile, 
	                                          String thumbnailFile,
	                                          boolean doHighQuality) throws IOException;
}
