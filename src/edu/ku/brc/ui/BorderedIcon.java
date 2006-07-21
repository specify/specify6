package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class BorderedIcon implements Icon
{
	protected Icon image;
	protected int leftWidth;
	protected int topWidth;
	protected int rightWidth;
	protected int bottomWidth;
	protected Color borderColor;
	
	public BorderedIcon( Icon image, int borderWidth, Color borderColor )
	{
		this(image,borderWidth,borderWidth,borderWidth,borderWidth,borderColor);
	}

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

	public void paintIcon(Component c, Graphics graphics, int x, int y)
	{	
		graphics.setColor(borderColor);
		graphics.drawRect(x, y, getIconWidth(), getIconHeight()-1);
		image.paintIcon(c, graphics, x+this.rightWidth, y+this.topWidth);
	}

	public int getIconWidth()
	{
		return image.getIconWidth() + this.leftWidth + this.rightWidth;
	}

	public int getIconHeight()
	{
		return image.getIconHeight() + this.topWidth + this.bottomWidth;
	}
}
