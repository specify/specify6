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
package edu.ku.brc.util.thumbnails;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import edu.ku.brc.ui.GraphicsUtils;

/**
 * This class generates thumbnails for any image readable by {@link ImageIO#read(File)}.
 * The exact list can be retrieved by calling {@link ImageIO#getReaderMIMETypes()}.
 * All thumbnail images are JPEG encoded.
 *
 * @code_status Beta
 * @author jstewart
 * @author rods
 */
public class ImageThumbnailGenerator extends BaseThumbnailGenerator
{
	/**
	 * Create an instance with a default max width and max height of 100.
	 */
	public ImageThumbnailGenerator()
	{
	    super();
	}
	
    /**
     * Creates a thumbnail of the given image bytes.
     * 
     * @param originalImageData the bytes of the input file
     * @param doHighQuality higher quality thumbnail (slower)
     * @return the bytes of the output file
     * @throws IOException if any IO errors occur during generation or storing the output
     */
    public byte[] generateThumbnail(final byte[] originalImageData,
                                    final boolean doHighQuality) throws IOException
    {
        ByteArrayInputStream inputStr = new ByteArrayInputStream(originalImageData);
        if (inputStr != null)
        {
            BufferedImage orig = ImageIO.read(inputStr);
            if (orig != null)
            {
                if (orig.getHeight() < maxSize.getHeight() && orig.getWidth() < maxSize.getWidth())
                {
                    // there's no need to do anything since the original is already under the max size
                    return originalImageData;
                }
                
                byte[] scaledImgData = GraphicsUtils.scaleImage(orig, maxSize.height, maxSize.width, true, doHighQuality);
                return scaledImgData;
            }
        }
        return null;
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#generateThumbnail(java.lang.String, java.lang.String, boolean)
	 */
    @Override
	public boolean generateThumbnail(final String originalFile, 
	                                 final String thumbnailFile,
	                                 final boolean doHighQuality) throws IOException
	{
	    byte[] origData = GraphicsUtils.readImage(originalFile);
        if (origData != null)
        {
            byte[] thumb = generateThumbnail(origData, doHighQuality);
            if (thumb != null)
            {
                FileUtils.writeByteArrayToFile(new File(thumbnailFile), thumb);
                return true;
            }
        }
	    
        return false;
	}
}
