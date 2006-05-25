package edu.ku.brc.specify.ui;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;

public class GraphicsUtils
{
	protected static RenderingHints hints;
	
	static
	{
		hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}
	
	/**
	 * @param g the Graphics context to draw in
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
	 * @param g the Graphics context to draw in
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
	 * @param g the Graphics context to draw in
	 * @param x the x coordinate for center of the String
	 * @param y the y coordinate for center of the String
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
	 * Code stolen from http://forum.java.sun.com/thread.jspa?threadID=378460&tstart=135
	 * 
	 * 
	 * 
	 * @param g2d
	 * @param xCenter
	 * @param yCenter
	 * @param x
	 * @param y
	 * @param stroke
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

	public static Point getPointAlongLine( Point start, Point end, float percent )
	{
		int x = start.x + (int)(percent * (float)(end.x-start.x));
		int y = start.y + (int)(percent * (float)(end.y-start.y));
		return new Point(x,y);
	}

	public static double distance( Point a, Point b)
	{
		return Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y, 2));
	}
	
	private static int yCor(int len, double dir)
	{
		return (int) (len * Math.cos(dir));
	}

	private static int xCor(int len, double dir)
	{
		return (int) (len * Math.sin(dir));
	}
}
