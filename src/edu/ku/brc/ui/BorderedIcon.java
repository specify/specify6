package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class BorderedIcon implements Icon
{
	protected Icon  image;
	protected int   leftWidth;
	protected int   topWidth;
	protected int   rightWidth;
	protected int   bottomWidth;
	protected Color borderColor;
	
	/**
	 * @param image
	 * @param borderWidth
	 * @param borderColor
	 */
	public BorderedIcon( Icon image, int borderWidth, Color borderColor )
	{
		this(image,borderWidth,borderWidth,borderWidth,borderWidth,borderColor);
	}

	/**
	 * @param image
	 * @param leftWidth
	 * @param topWidth
	 * @param rightWidth
	 * @param bottomWidth
	 * @param borderColor
	 */
	public BorderedIcon(Icon image,
						int leftWidth,
						int topWidth,
						int rightWidth,
						int bottomWidth,
						Color borderColor)
	{
		super();
		// TODO Auto-generated constructor stub
		this.image = image;
		this.leftWidth = leftWidth;
		this.topWidth = topWidth;
		this.rightWidth = rightWidth;
		this.bottomWidth = bottomWidth;
		this.borderColor = borderColor;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	public void paintIcon(Component c, Graphics g, int x, int y)
	{	
		g.setColor(borderColor);
		g.drawRect(x, y, getIconWidth(), getIconHeight()-1);
		image.paintIcon(c, g, x+this.rightWidth, y+this.topWidth);
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#getIconWidth()
	 */
	public int getIconWidth()
	{
		return image.getIconWidth() + this.leftWidth + this.rightWidth;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#getIconHeight()
	 */
	public int getIconHeight()
	{
		return image.getIconHeight() + this.topWidth + this.bottomWidth;
	}
}
