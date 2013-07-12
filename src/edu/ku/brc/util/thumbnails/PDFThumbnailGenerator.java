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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Feb 19, 2013
 *
 */
public class PDFThumbnailGenerator extends BaseThumbnailGenerator implements ImageObserver
{
    private static final String[] mimetypes = {"application/pdf", "application/x-pdf"};
    
    private int    baseLineHeight  = 256;
    private Insets thumbnailInsets = new Insets(3, 3, 20, 3);
    
    /** 
     * Height of each line.  Thumbnails will be scaled to this height
     * (minus the thumbnailInsets).
     */
    private int lineheight = baseLineHeight + thumbnailInsets.top + thumbnailInsets.bottom;
    
    /**
     * Guesstimate of the width of a thumbnail that hasn't been processed yet.
     */
    //private int defaultWidth = (int)(baseLineHeight/1.41) + thumbnailInsets.left + thumbnailInsets.right;
    
    /* (non-Javadoc)
     * @see edu.ku.brc.util.thumbnails.ThumbnailGeneratorIFace#getSupportedMimeTypes()
     */
    @Override
    public String[] getSupportedMimeTypes()
    {
        return mimetypes;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.thumbnails.ThumbnailGeneratorIFace#generateThumbnail(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public boolean generateThumbnail(final String originalFile,
                                     final String thumbnailFile,
                                     final boolean doHighQuality) throws IOException
    {
        try
        {
            File            inFile = new File(originalFile);
            FileInputStream fis    = new FileInputStream(inFile);
            FileChannel     fc     = fis.getChannel();

            // Get the file's size and then map it into memory
            int sz = (int)fc.size();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

            PDFFile file = new PDFFile(bb);

            int pagetoread = 1;
            PDFPage p = file.getPage(pagetoread, true);

            int w, h;
            if (p.getRotation() == 0 || p.getRotation() == 180)
            {
                w = (int) Math.ceil((lineheight - thumbnailInsets.top - thumbnailInsets.bottom) * p.getAspectRatio());
                h = lineheight - thumbnailInsets.top - thumbnailInsets.bottom;
            } else
            {
                w = lineheight - thumbnailInsets.top - thumbnailInsets.bottom;
                h = (int) Math.ceil((lineheight - thumbnailInsets.top - thumbnailInsets.bottom) / p.getAspectRatio());
            }

            Image img = p.getImage(w, h, null, this, true, true);
            
            BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            if (bimage != null)
            {
                Graphics g = bimage.createGraphics();
                if (g != null)
                {
                    g.drawImage(img, 0, 0, null);
                    g.dispose();
                    ImageIO.write(bimage, "PNG", new File(thumbnailFile));
                    return true;
                }
            }
        } catch (Exception e)
        {
            //e.printStackTrace();
            System.err.println(e.getMessage());

            //int size = lineheight - thumbnailInsets.top - thumbnailInsets.bottom;
            //images[workingon] = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_BINARY);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
    {
        return ((infoflags & (ALLBITS | ERROR | ABORT)) == 0);
    }
}
