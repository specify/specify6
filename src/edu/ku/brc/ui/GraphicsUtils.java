package edu.ku.brc.ui;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

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
    public static byte[] scaleImage(byte[] imgData, int maxHeight, int maxWidth, boolean preserveAspectRatio) throws IOException
    {
        ByteArrayInputStream inputStr = new ByteArrayInputStream(imgData);
        BufferedImage orig = ImageIO.read(inputStr);
        
        return scaleImage(orig, maxHeight, maxWidth, preserveAspectRatio);
    }
    
    /**
     * @param orig the original image
     * @param maxHeight the max height of the scaled image
     * @param maxWidth the max width of the scaled image
     * @param preserveAspectRatio if true, the scaling preserves the aspect ratio of the original image
     * @return the byte array of the scaled image
     * @throws IOException an error occurred while encoding the result as a JPEG image
     */
    public static byte[] scaleImage(BufferedImage orig, int maxHeight, int maxWidth, boolean preserveAspectRatio) throws IOException
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

        BufferedImage scaled = getScaledInstance(orig, targetW, targetH, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);

        ByteArrayOutputStream output = new ByteArrayOutputStream(8192);
        
        ImageIO.write(scaled, "jpeg", output);

        byte[] outputBytes = output.toByteArray();
        output.close();

        return outputBytes;
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
    public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint,
            boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w, h;
        if (higherQuality)
        {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        }
        else
        {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do
        {
            if (higherQuality && w > targetWidth)
            {
                w /= 2;
                if (w < targetWidth)
                {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight)
            {
                h /= 2;
                if (h < targetHeight)
                {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
}
