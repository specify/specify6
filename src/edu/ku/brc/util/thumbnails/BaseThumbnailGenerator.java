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
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class generates thumbnails for any image readable by {@link ImageIO#read(File)}.
 * The exact list can be retrieved by calling {@link ImageIO#getReaderMIMETypes()}.
 * All thumbnail images are JPEG encoded.
 *
 * @code_status Alpha
 * @author jstewart
 * @author rods
 */
public abstract class BaseThumbnailGenerator implements ThumbnailGeneratorIFace
{
    /** The max size of the thumbnail output. */
	protected Dimension maxSize;
	
    /** The quality factor of the thumbnail output. */
	protected float quality;
	
	/**
	 * Create an instance with a default max width and max height of 32.
	 */
	public BaseThumbnailGenerator()
	{
	    maxSize = new Dimension(100, 100);
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setMaxHeight(int)
	 */
    @Override
	public void setMaxSize(final Dimension maxSize)
	{
		this.maxSize = maxSize;
	}
	
	/* (non-Javadoc)
     * @see edu.ku.brc.util.thumbnails.ThumbnailGeneratorIFace#setMaxSize(int, int)
     */
    @Override
    public void setMaxSize(int width, int height)
    {
        this.maxSize = new Dimension(width, height);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.thumbnails.ThumbnailGeneratorIFace#getMaxSize()
     */
    @Override
    public Dimension getMaxSize()
    {
        return this.maxSize;
    }

    /* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setQuality(float)
	 */
    @Override
	public void setQuality(final float percent)
	{
		this.quality = percent;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#getSupportedMimeTypes()
	 */
    @Override
	public String[] getSupportedMimeTypes()
	{
		return ImageIO.getReaderMIMETypes();
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#generateThumbnail(java.lang.String, java.lang.String, boolean)
	 */
    @Override
	public abstract boolean generateThumbnail(final String originalFile, 
	                                 final String thumbnailFile,
	                                 final boolean doHighQuality) throws IOException;

}
