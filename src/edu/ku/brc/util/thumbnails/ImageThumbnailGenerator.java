/**
 * Copyright (C) 2006 The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util.thumbnails;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

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
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#generateThumbnail(java.lang.String, java.lang.String)
	 */
	public void generateThumbnail(String originalFile, String thumbnailFile) throws IOException
	{
        // read the original
		BufferedImage img = ImageIO.read(new File(originalFile));
		
        // calculate the new height and width while maintaining the aspect ratio
		int origWidth = img.getWidth();
		int origHeight = img.getHeight();
		int thumbWidth;
		int thumbHeight;
		if( origWidth >= origHeight )
		{
			thumbWidth = maxWidth;
			thumbHeight = (int)(origHeight * ((float)thumbWidth/(float)origWidth));
		}
		else
		{
			thumbHeight = maxHeight;
			thumbWidth = (int)(origWidth * ((float)thumbHeight/(float)origHeight));
		}
		
        // scale the image
	    BufferedImage thumbImage = new BufferedImage(thumbWidth,thumbHeight,BufferedImage.TYPE_INT_RGB);
	    Graphics2D graphics2D = thumbImage.createGraphics();
	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    graphics2D.drawImage(img, 0, 0, thumbWidth, thumbHeight, null);

	    // save thumbnail image to thumbnailFile as a JPEG
	    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(thumbnailFile));
	    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
	    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
	    if(quality < 0)
	    {
	    	quality = 0;
	    }
	    if(quality > 1)
	    {
	    	quality = 1;
	    }
	    param.setQuality(quality, false);
	    encoder.setJPEGEncodeParam(param);
	    encoder.encode(thumbImage);
	    out.close(); 
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setMaxDuration(int)
	 */
	public void setMaxDuration(int seconds)
	{
		// ignored
	}
}
