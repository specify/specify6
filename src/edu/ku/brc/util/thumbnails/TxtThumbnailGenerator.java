/**
 * Copyright (C) ${year}  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util.thumbnails;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class TxtThumbnailGenerator implements ThumbnailGenerator
{
	public TxtThumbnailGenerator()
	{
		// do nothing
	}
	
	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#generateThumbnail(java.lang.String, java.lang.String)
	 * @param originalFile
	 * @param thumbnailFile
	 */
	public void generateThumbnail(String originalFile, String thumbnailFile)
	{
		System.out.println("Not yet implemented");
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#getSupportedMimeTypes()
	 * @return
	 */
	public String[] getSupportedMimeTypes()
	{
		return new String[] {"text/plain"};
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setMaxHeight(int)
	 * @param maxHeight
	 */
	public void setMaxHeight(int maxHeight)
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setMaxWidth(int)
	 * @param maxWidth
	 */
	public void setMaxWidth(int maxWidth)
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGenerator#setQuality(float)
	 * @param percent
	 */
	public void setQuality(float percent)
	{
		// TODO Auto-generated method stub

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
