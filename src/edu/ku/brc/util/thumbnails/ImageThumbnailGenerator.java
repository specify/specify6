/**
 * Copyright (C) ${year}  The University of Kansas
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
 *
 * @code_status Alpha
 * @author jstewart
 */
public class ImageThumbnailGenerator implements ThumbnailGenerator
{
	protected int maxWidth;
	protected int maxHeight;
	protected float quality;
	
	public ImageThumbnailGenerator()
	{
		maxWidth = 32;
		maxHeight = 32;
	}
	
	/**
	 *
	 *
	 * @see edu.ku.brc.jds.ThumbnailGenerato#setMaxWidth(int)
	 * @param maxWidth
	 */
	public void setMaxWidth(int maxWidth)
	{
		this.maxWidth = maxWidth;
	}
	
	/**
	 *
	 *
	 * @see edu.ku.brc.jds.ThumbnailGenerato#setMaxHeight(int)
	 * @param maxHeight
	 */
	public void setMaxHeight(int maxHeight)
	{
		this.maxHeight = maxHeight;
	}
	
	/**
	 *
	 *
	 * @see edu.ku.brc.jds.ThumbnailGenerato#setQuality(float)
	 * @param percent
	 */
	public void setQuality(float percent)
	{
		this.quality = percent;
	}
	
	/**
	 *
	 *
	 * @see edu.ku.brc.jds.ThumbnailGenerato#getSupportedMimeTypes()
	 * @return
	 */
	public String[] getSupportedMimeTypes()
	{
		return ImageIO.getReaderMIMETypes();
	}
	
	/**
	 *
	 *
	 * @see edu.ku.brc.jds.ThumbnailGenerato#generateThumbnail(java.lang.String, java.lang.String)
	 * @param originalFile
	 * @param thumbnailFile
	 * @throws IOException
	 */
	public void generateThumbnail(String originalFile, String thumbnailFile) throws IOException
	{
		BufferedImage img = ImageIO.read(new File(originalFile));
		
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
		
	    BufferedImage thumbImage = new BufferedImage(thumbWidth,thumbHeight,BufferedImage.TYPE_INT_RGB);
	    Graphics2D graphics2D = thumbImage.createGraphics();
	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    graphics2D.drawImage(img, 0, 0, thumbWidth, thumbHeight, null);

	    // save thumbnail image to OUTFILE
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

	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setMaxDuration(int)
	 * @param seconds
	 */
	public void setMaxDuration(int seconds)
	{
		// ignored
	}
}
