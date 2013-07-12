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
package edu.ku.brc.ui;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.util.UUEncoder;

import sun.misc.UUDecoder;

/**
 * Provides simple utility functions for drawing circles, arrows, etc.  Some
 * of the code included here was taken from 
 * http://forum.java.sun.com/thread.jspa?threadID=378460&tstart=135.
 *
 * @code_status Complete
 * @author jstewart
 */
public class GraphicsUtils
{
	/** <code>RenderingHints</code> used to turn on anti-aliased drawing. */
	protected static RenderingHints hints;
	
	static
	{
		hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}
	
	/**
	 * Draws a circle using the given graphics context, centered at <code>(x,y)</code>,
	 * having the given diameter.
	 * 
	 * @see #fillCircle(Graphics, int, int, int)
	 * @param g the graphics context to draw in
	 * @param x the x coordinate for the center of the circle
	 * @param y the y coordinate for the center of the circle
	 * @param diameter the diameter of the circle
	 */
	public static void drawCircle( Graphics g, int x, int y, int diameter )
	{
		((Graphics2D)g).addRenderingHints(hints);
		g.drawOval(x-diameter/2, y-diameter/2, diameter, diameter);
	}

	/**
	 * Draws a filled circle using the given graphics context, centered at <code>(x,y)</code>,
	 * having the given diameter.
	 * 
	 * @see #drawCircle(Graphics, int, int, int)
	 * @param g the graphics context to draw in
	 * @param x the x coordinate for the center of the circle
	 * @param y the y coordinate for the center of the circle
	 * @param diameter the diameter of the circle
	 */
	public static void fillCircle( Graphics g, int x, int y, int diameter )
	{
		((Graphics2D)g).addRenderingHints(hints);
		g.fillOval(x-diameter/2, y-diameter/2, diameter, diameter);
	}

	/**
	 * Draws the given <code>String</code>, centered at <code>(x,y)</code>, using
	 * the given graphics context.
	 * 
	 * @param s the string
	 * @param g the graphics context to draw in
	 * @param x the x coordinate for center of the <code>String</code>
	 * @param y the y coordinate for center of the <code>String</code>
	 */
	public static void drawCenteredString( String s, Graphics g, int x, int y )
	{
		((Graphics2D)g).addRenderingHints(hints);

		FontMetrics fm = g.getFontMetrics();
		int ht = fm.getAscent() + fm.getDescent();
		int width = fm.stringWidth(s);
		g.drawString(s, x-width/2, y+(fm.getAscent()-ht/2));
	}

	/**
	 * Draws an arrow from <code>(xCenter,yCenter)</code> to <code>(x,y)</code>.
	 * Code stolen from http://forum.java.sun.com/thread.jspa?threadID=378460&tstart=135.
	 * 
	 * @param g the graphics context to draw in
	 * @param headSize the size of the arrow head
	 * @param xCenter the x-coord of the arrow tail
	 * @param yCenter the y-coord of the arrow tail
	 * @param x the x-coord of the arrow head's tip
	 * @param y the y-coord of the arrow head's tip
	 * @param stroke the <code>Stroke</code> to use
	 */
	public static void drawArrow(Graphics g, int xCenter, int yCenter, int x, int y, int headSize, float stroke)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.addRenderingHints(hints);
		
		double aDir = Math.atan2(xCenter - x, yCenter - y);
		Stroke origStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(stroke)); // make the arrow head solid even if dash pattern has been specified
		g2d.drawLine(x, y, xCenter, yCenter);
		Polygon tmpPoly = new Polygon();
		int i1 = 2*headSize + (int) stroke; //(stroke * 2);
		int i2 = headSize + (int) stroke; // make the arrow head the same size regardless of the length length
		tmpPoly.addPoint(x, y); // arrow tip
		tmpPoly.addPoint(x + xCor(i1, aDir + .5), y + yCor(i1, aDir + .5));
		tmpPoly.addPoint(x + xCor(i2, aDir), y + yCor(i2, aDir));
		tmpPoly.addPoint(x + xCor(i1, aDir - .5), y + yCor(i1, aDir - .5));
		tmpPoly.addPoint(x, y); // arrow tip
		g2d.drawPolygon(tmpPoly);
		g2d.fillPolygon(tmpPoly); // remove this line to leave arrow head unpainted
		g2d.setStroke(origStroke);
	}

	/**
	 * Calculates the location of a point that is a given percentage of the distance
	 * from <code>start</code> to <code>end</code>.
	 * 
	 * @param start the start of the line
	 * @param end the end of the line
	 * @param percent the percentage distance
	 * @return the point
	 */
	public static Point getPointAlongLine( Point start, Point end, float percent )
	{
		int x = start.x + (int)(percent * (end.x-start.x));
		int y = start.y + (int)(percent * (end.y-start.y));
		return new Point(x,y);
	}

	/**
	 * Calculates the distance from <code>a</code> to <code>b</code>.
	 *
	 * @param a a point
	 * @param b a point
	 * @return the distance (in pixel units)
	 */
	public static double distance( Point a, Point b)
	{
		return Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y, 2));
	}
	
	/**
	 * Returns the value of <code>len * Math.cos(dir)</code>.
 	 * Code stolen from http://forum.java.sun.com/thread.jspa?threadID=378460&tstart=135.
	 *
	 * @param len the length
	 * @param dir the angle in radians
	 * @return the result
	 */
	private static int yCor(int len, double dir)
	{
		return (int) (len * Math.cos(dir));
	}

	/**
	 * Returns the value of <code>len * Math.sin(dir)</code>.
	 * Code stolen from http://forum.java.sun.com/thread.jspa?threadID=378460&tstart=135.
	 *
	 * @param len the length
	 * @param dir the angle in radians
	 * @return the result
	 */
	private static int xCor(int len, double dir)
	{
		return (int) (len * Math.sin(dir));
	}

	/**
	 * Modifes the given {@link Graphics} object to enable anti-aliased
	 * drawing.
	 *
	 * @param g a {@link Graphics} object
	 */
	public static void turnOnAntialiasedDrawing(Graphics g)
	{
		if(g instanceof Graphics2D)
		{
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
	}
    
    /**
     * Scales an image using a relatively fast algorithm.
     * 
     * @param imgData the byte array of the original image data (must be readable by {@link ImageIO#read(java.io.InputStream)})
     * @param maxHeight the max height of the scaled image
     * @param maxWidth the max width of the scaled image
     * @param preserveAspectRatio if true, the scaling preserves the aspect ratio of the original image
     * @return the byte array of the scaled image
     * @throws IOException an error occurred while loading the input bytes as a BufferedImage or while encoding the output as a JPEG
     */
    public static ImageIcon scaleImageToIconImage(final BufferedImage img, 
                                                  final int maxHeight, 
                                                  final int maxWidth, 
                                                  final boolean preserveAspectRatio,
                                                  final boolean doHighQuality) throws IOException
    {
        
        
        byte[] bytes = scaleImage(img, maxHeight, maxWidth, preserveAspectRatio, doHighQuality);
        if (bytes != null && bytes.length > 0)
        {
            return new ImageIcon(bytes);
        }
        return null;
    }
    
    /**
     * Scales an image using a relatively fast algorithm.
     * 
     * @param imgData the byte array of the original image data (must be readable by {@link ImageIO#read(java.io.InputStream)})
     * @param maxHeight the max height of the scaled image
     * @param maxWidth the max width of the scaled image
     * @param preserveAspectRatio if true, the scaling preserves the aspect ratio of the original image
     * @return the byte array of the scaled image
     * @throws IOException an error occurred while loading the input bytes as a BufferedImage or while encoding the output as a JPEG
     */
    public static byte[] scaleImage(final byte[] imgData, 
                                    final int maxHeight, 
                                    final int maxWidth, 
                                    final boolean preserveAspectRatio,
                                    final boolean doHighQuality) throws IOException
    {
        ByteArrayInputStream inputStr = new ByteArrayInputStream(imgData);
        BufferedImage orig = ImageIO.read(inputStr);
        
        return scaleImage(orig, maxHeight, maxWidth, preserveAspectRatio, doHighQuality);
    }
    
    /**
     * @param orig the original image
     * @param maxHeight the max height of the scaled image
     * @param maxWidth the max width of the scaled image
     * @param preserveAspectRatio if true, the scaling preserves the aspect ratio of the original image
     * @param doHighQuality do higher quality thumbnail (slow)
     * @return the byte array of the scaled image
     * @throws IOException an error occurred while encoding the result as a JPEG image
     */
    public static byte[] scaleImage(final BufferedImage orig, 
                                    final int maxHeight, 
                                    final int maxWidth, 
                                    final boolean preserveAspectRatio,
                                    boolean doHighQuality) throws IOException
    {
        BufferedImage scaled;
        if (true)
        {
            int targetW = maxWidth;
            int targetH = maxHeight;
    
            if (preserveAspectRatio)
            {
                int origWidth = orig.getWidth();
                int origHeight = orig.getHeight();
                
                double origRatio   = (double)origWidth/(double)origHeight;
                double scaledRatio = (double)maxWidth/(double)maxHeight;
                
                if ( origRatio > scaledRatio )
                {
                    targetH = (int)(targetW / origRatio);
                }
                else
                {
                    targetW = (int)(targetH * origRatio);
                }
            }
    
            scaled = getScaledInstance(orig, targetW, targetH, doHighQuality);
        } else
        {
            scaled = generateScaledImage(orig, RenderingHints.VALUE_INTERPOLATION_BILINEAR, Math.max(maxHeight, maxWidth));
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream(8192);
        
        ImageIO.write(scaled, "jpeg", output);

        byte[] outputBytes = output.toByteArray();
        output.close();

        return outputBytes;
    }
    
    /**
     * @param bufImg
     * @param size
     * @return
     */
    public static BufferedImage generateScaledImage(final BufferedImage bufImg, 
                                                    @SuppressWarnings("unused")
                                                    final Object hintsArg,
                                                    final int size)
    {
        BufferedImage sourceImage = bufImg;
        int srcWidth  = sourceImage.getWidth();
        int srcHeight = sourceImage.getHeight();
        
        double longSideForSource = Math.max(srcWidth, srcHeight);
        double longSideForDest   = size;
        
        double multiplier = longSideForDest / longSideForSource;
        int destWidth = (int) (srcWidth * multiplier);
        int destHeight = (int) (srcHeight * multiplier);

        BufferedImage destImage = null;
        
        destImage = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = destImage.createGraphics();
        graphics2D.drawImage(sourceImage, 0, 0, destWidth, destHeight, null);
        graphics2D.dispose();
            
            
        return destImage;
    }

    
    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     * 
     * Code stolen from http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in down-scaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(final BufferedImage img, 
                                                  final int targetWidth, 
                                                  final int targetHeight,
                                                  final boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage temp = img;

        BufferedImage result = new BufferedImage(targetWidth, targetHeight, type);
        Graphics2D g2 = result.createGraphics();
        if (higherQuality)
        {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        g2.drawImage(temp, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();

        return result;
    }
    
    /**
     * @param fm
     * @param text
     * @param availableWidth
     * @return
     */
    public static String clipString(FontMetrics fm, String text, int availableWidth)
    {
        // first see if the string needs clipping at all
        
        if (text == null)
        {
            return "";
        }
        
        if (fm.stringWidth(text) < availableWidth)
        {
            return text;
        }
        
        String dots = "...";
        
        StringBuilder sb = new StringBuilder(text + dots);
        while (sb.length()-4 > 0 && fm.stringWidth(sb.toString()) > availableWidth)
        {
            sb.deleteCharAt(sb.length()-4);
        }
        return sb.toString();
    }
    
    /**
     * Gets a scaled icon and if it doesn't exist it creates one and scales it
     * @param icon image to be scaled
     * @param iconSize the icon size (Std)
     * @param scaledIconSize the new scaled size in pixels
     * @return the scaled icon
     */
    public static BufferedImage getBufferedImage(final ImageIcon icon)
    {
        Image imgMemory = icon.getImage();
        
        //make sure all pixels in the image were loaded
        imgMemory = new ImageIcon(imgMemory).getImage();
        
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D    graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(imgMemory, 0, 0, w, h, 0, 0, w, h, null);
        graphics2D.dispose();
        
        return bufferedImage;
    }
    
    /**
     * Gets a scaled icon and if it doesn't exist it creates one and scales it
     * @param icon image to be scaled
     * @param iconSize the icon size (Std)
     * @param scaledIconSize the new scaled size in pixels
     * @return the scaled icon
     */
    public static Image getScaledImage(final ImageIcon icon, 
                                       final int     newMaxWidth, 
                                       final int     newMaxHeight, 
                                       final boolean maintainRatio)
    {
        if (icon != null)
        {
            int dstWidth  = newMaxWidth;
            int dstHeight = newMaxHeight;
            
            int srcWidth  = icon.getIconWidth();
            int srcHeight = icon.getIconHeight();
                    
            if ((dstWidth < 0) || (dstHeight < 0))
            {   //image is nonstd, revert to original size
                dstWidth  = icon.getIconWidth();
                dstHeight = icon.getIconHeight();
            }
            
            if (maintainRatio)
            {
                double longSideForSource = Math.max(srcWidth, srcHeight);
                double longSideForDest   = Math.max(dstWidth, dstHeight);
                
                double multiplier = longSideForDest / longSideForSource;
                dstWidth  = (int) (srcWidth * multiplier);
                dstHeight = (int) (srcHeight * multiplier);
            }
            
            Image imgMemory = icon.getImage();
            
            //make sure all pixels in the image were loaded
            imgMemory = new ImageIcon(imgMemory).getImage();
            
            BufferedImage thumbImage = new BufferedImage(dstWidth, dstHeight, BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D    graphics2D = thumbImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(imgMemory, 0, 0, dstWidth, dstHeight, 0, 0, srcWidth, srcHeight, null);
            graphics2D.dispose();
            return thumbImage;
            
        }
        return null;
    }
    
    /**
     * Reads an image to a byte array (Tiff, Png, JPeg). 
     * @param srcFile the file
     * @return byte array
     */
    public static byte[] readImage(final String srcFile)
    {
        return readImage(new File(srcFile));
    }
    
    /**
     * Reads an image to a byte array (Tiff, Png, JPeg). 
     * @param srcFile the file
     * @return byte array
     */
    public static byte[] readImage(final File srcFile)
    {
        if (srcFile != null && srcFile.exists())
        {
            try
            {
                String ext = FilenameUtils.getExtension(srcFile.getName());
                if (ext.equals("tif") || ext.equals("tiff"))
                {
                    return GraphicsUtils.readTiffImage(srcFile);
                } 
                return FileUtils.readFileToByteArray(srcFile);
                
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * @param fileName
     * @return
     */
    public static byte[] readTiffImage(final String fileName)
    {
        return readTiffImage(new File(fileName));
    }
    
    /**
     * Taken from:
     * http://java.sun.com/products/java-media/jai/forDevelopers/jai1_0_1guide-unc/Examples.doc.html
     * 
     * @param fileName
     * @return
     */
    public static byte[] readTiffImage(final File fileName)
    {
    	/*
        try
        {
        	
            FileSeekableStream stream = new FileSeekableStream(fileName);

            // Store the input stream in a ParameterBlock to be sent to
            // the operation registry, and eventually to the TIFF
            // decoder.
            ParameterBlock params = new ParameterBlock();
            params.add(stream);

            // Specify to TIFF decoder to decode images as they are and
            // not to convert unsigned short images to byte images.
            TIFFDecodeParam decodeParam = new TIFFDecodeParam();
            decodeParam.setDecodePaletteAsShorts(true);

            // Create an operator to decode the TIFF file.
            RenderedOp image1 = JAI.create("tiff", params);

            // Find out the first image's data type.
            int        dataType = image1.getSampleModel().getDataType();
            RenderedOp image2   = null;
            if (dataType == DataBuffer.TYPE_BYTE)
            {
                // Display the byte image as it is.
                //System.out.println("TIFF image is type byte.");
                image2 = image1;
                
            } else if (dataType == DataBuffer.TYPE_USHORT)
            {

                // Convert the unsigned short image to byte image.
                //System.out.println("TIFF image is type ushort.");

                // Setup a standard window-level lookup table. 
                byte[] tableData = new byte[0x10000];
                for (int i = 0; i < 0x10000; i++)
                {
                    tableData[i] = (byte) (i >> 8);
                }

                // Create a LookupTableJAI object to be used with the
                // "lookup" operator.
                LookupTableJAI table = new LookupTableJAI(tableData);

                // Create an operator to lookup image1.
                image2 = JAI.create("lookup", image1, table);

            } else
            {
                //System.out.println("TIFF image is type " + dataType + ", and will not be displayed.");
                return null;
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream(8192);
            
            ImageIO.write(image2, "png", output);

            byte[] outputBytes = output.toByteArray();
            output.close();

            return outputBytes;
            
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }*/
        return null;
    }
    
    

    
    /**
     * @param name
     * @param imgIcon
     * @return
     */
    public static String uuencodeImage(final String name, final ImageIcon imgIcon)
    {
        try
        {
            BufferedImage tmp = new BufferedImage(imgIcon.getIconWidth(), imgIcon.getIconWidth(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(imgIcon.getImage(), 0, 0, imgIcon.getIconWidth(), imgIcon.getIconWidth(), null);
            g2.dispose();
            
            ByteArrayOutputStream output = new ByteArrayOutputStream(8192);
            ImageIO.write(tmp, "PNG", output);
            byte[] outputBytes = output.toByteArray();
            output.close();
            
            ByteArrayOutputStream  bos = new ByteArrayOutputStream();
            UUEncoder uuencode = new UUEncoder(name);
            uuencode.encode(new ByteArrayInputStream(outputBytes), bos);
            return bos.toString();
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GraphicsUtils.class, ex);
            ex.printStackTrace();
        }
        return "";
    }
    
    /**
     * @param name
     * @param str
     * @return
     */
    public static ImageIcon uudecodeImage(final String name, final String str)
    {
        try
        {
            UUDecoder decoder = new UUDecoder();
            ByteBuffer bb = decoder.decodeBufferToByteBuffer(str);
            ImageIcon img = new ImageIcon(bb.array());
            return img;
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GraphicsUtils.class, ex);
            ex.printStackTrace();
        }
        return null;
    }
}
