/* Filename:    $RCSfile: CurvedBorder.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui;

/**
 * This Border class draws a curved border
 */
import java.awt.*;
import javax.swing.border.*;

public class CurvedBorder extends AbstractBorder
{
    private Color borderColor = Color.gray;
    private int   borderWidth = 6;

    /**
     * 
     *
     */
    public CurvedBorder()
    {
    }

    /**
     * 
     * @param borderWidth
     */
    public CurvedBorder(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

    /**
     * 
     * @param borderColor
     */
    public CurvedBorder(Color borderColor)
    {
        this.borderColor = borderColor;
    }

    /**
     * 
     * @param borderWidth
     * @param borderColor
     */
    public CurvedBorder(int borderWidth, Color borderColor)
    {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
    }

    /**
     * 
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
    {
        g.setColor(getBorderColor());
        g.drawRoundRect(x, y, w - 1, h - 1, borderWidth, borderWidth);

    }

    /**
     * 
     */
    public Insets getBorderInsets(Component c)
    {
        return new Insets(borderWidth, borderWidth, borderWidth, borderWidth);
    }

    /**
     * 
     */
    public Insets getBorderInsets(Component c, Insets i)
    {
        i.left = i.right = i.bottom = i.top = borderWidth;
        return i;
    }

    /**
     * 
     */
    public boolean isBorderOpaque()
    {
        return true;
    }

    /**
     * 
     * @return
     */
    public Color getBorderColor()
    {
        return borderColor;
    }

    /**
     * 
     * @param borderColor
     */
    public void setBorderColor(Color borderColor)
    {
        this.borderColor = borderColor;
    }

    /**
     * 
     * @return
     */
    public int getBorderWidth()
    {
        return borderWidth;
    }

    /**
     * 
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

}