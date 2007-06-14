/**
 * Copyright (C) 2006 The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
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
 * @code_status Alpha
 * @author jstewart
 */
public class ImageThumbnailGenerator implements ThumbnailGenerator
{
    /** The max width of the thumbnail output. */
	protected int maxWidth;
	
    /** The max height of the thumbnail output. */
	protected int maxHeight;
	
    /** The quality factor of the thumbnail output. */
	protected float quality;
	
	/**
	 * Create an instance with a default max width and max height of 32.
	 */
	public ImageThumbnailGenerator()
	{
		maxWidth = 32;
		maxHeight = 32;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setMaxWidth(int)
	 */
	public void setMaxWidth(int maxWidth)
	{
		this.maxWidth = maxWidth;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setMaxHeight(int)
	 */
	public void setMaxHeight(int maxHeight)
	{
		this.maxHeight = maxHeight;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setQuality(float)
	 */
	public void setQuality(float percent)
	{
		this.quality = percent;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#getSupportedMimeTypes()
	 */
	public String[] getSupportedMimeTypes()
	{
		return ImageIO.getReaderMIMETypes();
	}
    
    /**
     * Creates a thumbnail of the given image bytes.
     * 
     * @param originalImageData the bytes of the input file
     * @return the bytes of the output file
     * @throws IOException if any IO errors occur during generation or storing the output
     */
    public byte[] generateThumbnail(byte[] originalImageData) throws IOException
    {
        ByteArrayInputStream inputStr = new ByteArrayInputStream(originalImageData);
        BufferedImage orig = ImageIO.read(inputStr);
        
        if (orig.getHeight() < maxHeight && orig.getWidth() < maxWidth)
        {
            // there's no need to do anything since the original is already under the max size
            return originalImageData;
        }
        
        byte[] scaledImgData = GraphicsUtils.scaleImage(orig, maxHeight, maxWidth, true);
        return scaledImgData;
    }
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#generateThumbnail(java.lang.String, java.lang.String)
	 */
	public void generateThumbnail(String originalFile, String thumbnailFile) throws IOException
	{
        byte[] origData = FileUtils.readFileToByteArray(new File(originalFile));
        byte[] thumb = generateThumbnail(origData);
        FileUtils.writeByteArrayToFile(new File(thumbnailFile), thumb);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setMaxDuration(int)
	 */
	public void setMaxDuration(int seconds)
	{
		// ignored
	}
}
