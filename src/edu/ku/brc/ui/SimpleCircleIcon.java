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
package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * A simple icon composed solely of a circle.
 *
 * @code_status Complete
 * @author jstewart
 */
public class SimpleCircleIcon implements Icon
{
	/** The circle icon's diameter. */
	protected int size;
	/** The icon color. */
	protected Color color;
	
	/**
	 * Constructs a new circle icon of the given diameter and color.
	 *
	 * @param size the diameter
	 * @param color the color
	 */
	public SimpleCircleIcon(int size, Color color )
	{
		this.size = size;
		this.color = color;
	}

	/**
	 * Returns the color.
	 *
	 * @see #setColor(Color)
	 * @return the color
	 */
	public Color getColor()
	{
		return color;
	}

	/**
	 * Sets the color.
	 *
	 * @see #getColor()
	 * @param color the color
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}

	/**
	 * Returns the size.
	 *
	 * @see #setSize(int)
	 * @return the size
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * Sets the size.
	 *
	 * @see #getSize()
	 * @param size the size
	 */
	public void setSize(int size)
	{
		this.size = size;
	}

	/**
	 * Paints the icon at the given location using the provided graphics
	 * context.  Implements the <code>Icon.paintIcon(Component,Graphics,int,int)</code>
	 * interface method.  
	 *
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 * @param c the parent component
	 * @param g the graphics context
	 * @param x the x-coord
	 * @param y the y-coord
	 */
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Color origColor = g.getColor();
		g.setColor(color);
		GraphicsUtils.fillCircle(g, x, y, size);
		g.setColor(origColor);
	}

	/**
	 * Returns the width (size) of the icon.
	 *
	 * @see javax.swing.Icon#getIconWidth()
	 * @see #getSize()
	 * @return the width
	 */
	public int getIconWidth()
	{
		return size;
	}

	/**
	 * Returns the height (size) of the icon.
	 *
	 * @see javax.swing.Icon#getIconHeight()
	 * @see #getSize()
	 * @return the height
	 */
	public int getIconHeight()
	{
		return size;
	}
}
