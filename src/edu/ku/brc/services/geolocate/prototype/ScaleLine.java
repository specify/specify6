package edu.ku.brc.services.geolocate.prototype;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import org.jdesktop.swingx.JXPanel;

@SuppressWarnings("serial")
public class ScaleLine extends JXPanel {
	
	public final int barPxLength = 100;
	private static final int tickPxRadius = 12;
	private static final int tickPxDiameter = tickPxRadius * 2;
	private static final int tickFrameOffset = 1;
	
	private String metricCaption = "";
	private Point metricTickLocation = new Point(barPxLength, tickPxRadius);
	private String imperialCaption = "";
	private Point imperialTickLocation = new Point(barPxLength, tickPxRadius);
	
	
	public ScaleLine()
	{
		setLayout(new FlowLayout(FlowLayout.CENTER, 0,0));
		setPreferredSize(new Dimension(barPxLength, tickPxDiameter));
		//setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		setOpaque(false);
	}
	
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2.0f));
		g2.setColor(new Color(74, 75, 77));
		
		//Draw starting tick.
		g2.drawLine(tickFrameOffset, 0, tickFrameOffset, tickPxDiameter);
		
		//Draw scale line from longest distance between metric and imperial.
		g2.drawLine( 0, tickPxRadius, Math.max(metricTickLocation.x, imperialTickLocation.x), tickPxRadius);
		
		Font font = new Font("Arial", Font.BOLD, 12);
		g2.setFont(font);
		//Draw metric caption centered.
		Rectangle2D labelBounds = g.getFontMetrics().getStringBounds(metricCaption, g);
        int strOffsetX = (metricTickLocation.x - (int)labelBounds.getWidth())/2;
		g2.drawString(metricCaption, strOffsetX, tickPxRadius - 3);
		//Draw metric tick.
		g2.drawLine(metricTickLocation.x + tickFrameOffset, 0, metricTickLocation.x + tickFrameOffset, metricTickLocation.y);
		
		//Draw imperial caption centered.
		labelBounds = g.getFontMetrics().getStringBounds(imperialCaption, g);
        strOffsetX = (imperialTickLocation.x - (int)labelBounds.getWidth())/2;
		g2.drawString(imperialCaption, strOffsetX, tickPxDiameter);
		
		//Draw imperial tick.
		g2.drawLine(imperialTickLocation.x + tickFrameOffset, tickPxRadius, 
				imperialTickLocation.x + tickFrameOffset, imperialTickLocation.y + tickPxRadius);
	}
	
	public void adjust(String metricCaption, Point metricTickLocation, String imperialCaption, Point imperialTickLocation, int scaleLineXOffset) {
		this.metricCaption = metricCaption;
		this.imperialCaption = imperialCaption;
		this.imperialTickLocation = new Point(imperialTickLocation.x - scaleLineXOffset, tickPxRadius);
		this.metricTickLocation = new Point(metricTickLocation.x - scaleLineXOffset, tickPxRadius);
		this.repaint();
	}
	
	@Override
	public Point getLocation() {
		Point location = super.getLocation();
		location.setLocation(location.x, location.y + tickPxRadius);
		return location;
	}
}

