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
public class TxtThumbnailGenerator implements ThumbnailGeneratorIFace
{
	public TxtThumbnailGenerator()
	{
		// do nothing
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGeneratorIFace#generateThumbnail(java.lang.String, java.lang.String, boolean)
	 */
	public boolean generateThumbnail(final String originalFile,
	                                 final String thumbnailFile, 
	                                 final boolean doHighQuality)
	{
		System.out.println("Not yet implemented");
		return false;
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGeneratorIFace#getSupportedMimeTypes()
	 * @return
	 */
	public String[] getSupportedMimeTypes()
	{
		return new String[] {"text/plain"};
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGeneratorIFace#setMaxHeight(int)
	 * @param maxHeight
	 */
	public void setMaxHeight(final int maxHeight)
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGeneratorIFace#setMaxWidth(int)
	 * @param maxWidth
	 */
	public void setMaxWidth(final int maxWidth)
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGeneratorIFace#setQuality(float)
	 * @param percent
	 */
	public void setQuality(final float percent)
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 *
	 * @see edu.ku.brc.util.thumbnails.ThumbnailGeneratorIFace#setMaxDuration(int)
	 * @param seconds
	 */
	public void setMaxDuration(final int seconds)
	{
		// ignored
	}

}
