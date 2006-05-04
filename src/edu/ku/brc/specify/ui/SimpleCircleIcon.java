package edu.ku.brc.specify.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class SimpleCircleIcon implements Icon
{
	protected int size;
	protected Color color;
	
	public SimpleCircleIcon(int size, Color color )
	{
		this.size = size;
		this.color = color;
	}

	/**
	 * @return Returns the color.
	 */
	public Color getColor()
	{
		return color;
	}

	/**
	 * @param color The color to set.
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}

	/**
	 * @return Returns the size.
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * @param size The size to set.
	 */
	public void setSize(int size)
	{
		this.size = size;
	}

	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Color origColor = g.getColor();
		g.setColor(color);
		GraphicsUtils.fillCircle(g, x, y, size);
		g.setColor(origColor);
	}

	public int getIconWidth()
	{
		return size;
	}

	public int getIconHeight()
	{
		return size;
	}
}
