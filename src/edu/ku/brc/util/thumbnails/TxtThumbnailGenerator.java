/* Copyright (C) 2009, University of Kansas Center for Research
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
