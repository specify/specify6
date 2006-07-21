/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 * A "liquid" Label that default to a gradeient filled square Label. 
 * A renderer need to be created so VectorLabel and GradiantLabel can share all the code.
* 
 * (Adapted from the book SwingHacks)
 * 
 * @author rods
 * @author Chris Adamson
 * @author Joshua Marinacci
 *
 */
@SuppressWarnings("serial")
public class GradiantLabel extends JLabel
{
    protected Color   textColor        = null;
    protected Color   textColorShadow  = null;
    
    /**
     * Defaults to a gradiant square Label
     * @param text the label on the Label
     */
    public GradiantLabel(String text) 
    {
        super(text);
        setTextColor(Color.BLACK);
        setBorder(new EmptyBorder(0,0,0,0));
    }
    
    /**
     * Defaults to a gradiant square button with JLabel alignments (XXX right align is not implemented)
     * @param text the lable text
     * @param align alignment from JLabel
     */
    public GradiantLabel(String text, int align) 
    {
        super(text, align);
        setTextColor(Color.BLACK);
    }
    

    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() 
    {
        String text = getText();
        FontMetrics fm = this.getFontMetrics(getFont());
        float scale = (50f/40f)*this.getFont().getSize2D();
        int w = fm.stringWidth(text);
        w += (int)(scale*1.4f);
        int h = fm.getHeight();
        h += (int)(scale*.3f);
        
        Icon icon = getIcon();
        if (icon != null)
        {
            h = Math.max(icon.getIconHeight(), h);
            w += icon.getIconWidth() + this.getIconTextGap();
        }
        return new Dimension(w, h);
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) 
    {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.getBackground());
        
        String text      = getText();
        int    textWidth = g2.getFontMetrics().stringWidth(text);
        
        int w = this.getWidth();
        int h = this.getHeight();
        g2.fillRect(0,0, w,h);
 
        drawLabelBody(w,h, getForeground(), g2);

        Icon icon = getIcon();
        if (icon == null)
        {
            drawText(w, h, text, textWidth, g2);
            
        } else if (this.getHorizontalTextPosition() == JLabel.LEFT)
        {
            int offsetX = drawText(w, h, text, textWidth, g2);
            icon.paintIcon(this, g2, offsetX+textWidth+this.getIconTextGap(), h > icon.getIconHeight() ? (h - icon.getIconHeight())/2 : 0);
            
        } else
        {
            icon.paintIcon(this, g2, 0, h > icon.getIconHeight() ? (h - icon.getIconHeight())/2 : 0);
            drawText(this.getIconTextGap()+icon.getIconWidth(), w, h, text, textWidth, g2);
            
        }
    }
    
    protected void drawLabelBody(int w, int h, Color base, Graphics2D g2) 
    {
        // draw the Label body
        Color grad_top = base.brighter();
        Color grad_bot = base.darker();        
        GradientPaint bg = new GradientPaint(new Point(0,0), grad_top,
                                             new Point(0,h), grad_bot);
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);
        
    }
    
    protected int drawText(int w, int h, String text, int textWidth, Graphics2D g2) 
    {
        // calculate the width and height
        int textx = this.getHorizontalAlignment() == JLabel.LEFT ? Math.max(getInsets().left, 2) : (w-textWidth)/2;
        drawText(textx, w, h, text, textWidth, g2);
        return textx;
    }
    
    protected void drawText(int x, int w, int h, String text, int textWidth, Graphics2D g2) 
    {
        // calculate the width and height
        int fh = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();
        int texty = h/2 + fh/2;
        
        // draw the text
        g2.setColor(textColorShadow);
        g2.drawString(text, x, texty);
        g2.setColor(textColor);
        g2.drawString(text, x, texty);

    }
    
    
     // generate the alpha version of this color
    protected static Color alphaColor(Color color, int alpha) 
    {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    
    
    public Color getTextColor()
    {
        return textColor;
    }

    public void setTextColor(Color textColor)
    {
        this.textColor = textColor;
        textColorShadow = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 70);
    }

    
}
