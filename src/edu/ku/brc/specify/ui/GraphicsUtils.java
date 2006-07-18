package edu.ku.brc.specify.ui;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 * Provides simple utility functions for drawing circles, arrows, etc.  Some
 * of the code included here was taken from 
 * http://forum.java.sun.com/thread.jspa?threadID=378460&tstart=135.
 * 
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
		int x = start.x + (int)(percent * (float)(end.x-start.x));
		int y = start.y + (int)(percent * (float)(end.y-start.y));
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

	public static void turnOnAntialiasedDrawing(Graphics g)
	{
		if(g instanceof Graphics2D)
		{
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
	}
}
