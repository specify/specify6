/**
 * Copyright (C) ${year}  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util.thumbnails;

import java.io.IOException;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public interface ThumbnailGenerator
{
	public void setMaxWidth(int maxWidth);

	public void setMaxHeight(int maxHeight);
	
	public void setMaxDuration(int seconds);

	public void setQuality(float percent);

	public String[] getSupportedMimeTypes();

	public void generateThumbnail(String originalFile, String thumbnailFile) throws IOException;
}