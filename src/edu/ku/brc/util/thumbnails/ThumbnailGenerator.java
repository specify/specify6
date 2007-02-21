/**
 * Copyright (C) ${year}  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util.thumbnails;

import java.io.IOException;

/**
 * This interface defines the minimum capabilities required of a class capable
 * of generating thumbnails of various types of files.
 *
 * @code_status Alpha
 * @author jstewart
 */
public interface ThumbnailGenerator
{
	/**
	 * Sets the maximum width of any visual thumbnails created.
	 *
	 * @param maxWidth the maximum thumbnail width
	 */
	public void setMaxWidth(int maxWidth);

	/**
	 * Sets the maximum height of any visual thumbnails created.
	 *
	 * @param maxHeight the maximum thumbnail height
	 */
	public void setMaxHeight(int maxHeight);
	
	/**
	 * Sets the maximum duration of any audio or video 'thumbnails' created.
	 *
	 * @param seconds the time length of the audio or video thumbnails created
	 */
	public void setMaxDuration(int seconds);

	/**
	 * Sets the quality factor for any thumbnailers that implement a configurable
	 * level of quality.
	 *
	 * @param percent the quality factor
	 */
	public void setQuality(float percent);

	/**
	 * Returns an array of MIME types that are supported by this thumbnail generator.
	 *
	 * @return the array of supported MIME types
	 */
	public String[] getSupportedMimeTypes();

	/**
	 * Create a thumbnail for the given original, placing the output in the given output file.
	 *
	 * @param originalFile the path to the original
	 * @param thumbnailFile the path to the output thumbnail
	 * @throws IOException if any IO errors occur during generation or storing the output
	 */
	public void generateThumbnail(String originalFile, String thumbnailFile) throws IOException;
}