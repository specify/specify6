/* Filename:    $RCSfile: VectorButton.java,v $
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * A "liquid" button that default to a gradeient filled square button.
 * A renderer need to be created so VectorLabel and VectorButton can share all the code.
*
 * (Adapted from the book SwingHacks)
 *
 * @author rods
 * @author Chris Adamson
 * @author Joshua Marinacci
 *
 */
@SuppressWarnings("serial")
public class VectorButton extends JButton implements MouseListener
{
    protected boolean isDrawHighlight  = false;
    protected boolean isRoundedRect    = false;
    protected boolean isDrawInnerBlock = false;
    protected boolean isBorderPainted  = false;

    protected Color   textColor        = null;
    protected Color   textColorShadow  = null;

    /**
     * Defaults to a gradiant square button
     * @param text the label on the Button
     */
    public VectorButton(String text)
    {
        super(text);
        setTextColor(Color.BLACK);
        super.setBorderPainted(false);
        this.addMouseListener(this);
    }

    /**
     * Constructor for indicating it should be a complete liquid button
     * @param text the lable text
     * @param doLiquid true - liquid, false - gradient
     */
    public VectorButton(String text, boolean doLiquid)
    {
        super(text);
        isDrawHighlight  = doLiquid;
        isRoundedRect    = doLiquid;
        isDrawInnerBlock = doLiquid;
        setTextColor(Color.BLACK);
        super.setBorderPainted(false);
        this.addMouseListener(this);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        String text = getText();
        FontMetrics fm = this.getFontMetrics(getFont());
        float scale = (50f/30f)*this.getFont().getSize2D();
        int w = fm.stringWidth(text);
        w += (int)(scale*1.4f);
        int h = fm.getHeight();
        h += (int)(scale*.3f);
        return new Dimension(w,h);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.getBackground());
        g2.fillRect(0,0,this.getWidth(),this.getHeight());

        float scale = (50f/30f)*this.getFont().getSize2D();

        drawLiquidButton(this.getForeground(),
            this.getWidth(), this.getHeight(),
            getText(), scale,
            g2);

    }

    /**
     * Draw a liquid button
     * @param base base color
     * @param width the width
     * @param height the height
     * @param text the string for the label
     * @param scale the scaling factor
     * @param g2 the graphics to be drawn into
     */
    protected void drawLiquidButton(Color base,
                                    int width, int height,
                                    String text, float scale,
                                    Graphics2D g2)
    {

        // calculate inset
        int inset = (int)(scale*0.04f);

        boolean doingGradient = !isDrawHighlight && !isRoundedRect && !isDrawInnerBlock;
        int w = doingGradient ? width : width - inset*2 - 1; // XXX ?? Hmmm,
        int h = doingGradient ? height : height - (int)(scale*0.1f) - 1;


        g2.translate(inset,0);
        //drawDropShadow(w,h,scale,g2);

        if (pressed) {
            g2.translate(0, 0.04f*scale);
        }

        drawButtonBody(w,h,scale,base,g2);
        drawText(w,h,scale,text,g2);
        if (isDrawHighlight)
        {
            drawHighlight(w,h,scale,base,g2);
        }

        if (isBorderPainted())
        {
            drawBorder(w,h,scale,g2);
        }

        if(pressed) {
            g2.translate(0, 0.04f*scale);
        }
        g2.translate(-inset,0);
    }

    protected void drawDropShadow(int w, int h, float scale, Graphics2D g2)
    {
            // draw the outer drop shadow
        g2.setColor(new Color(0,0,0,50));
        fillRoundRect(g2,
            (-.04f)*scale,
            (.02f)*scale,
            w+.08f*scale, h+0.08f*scale,
            scale*1.04f, scale*1.04f);
        g2.setColor(new Color(0,0,0,100));
        fillRoundRect(g2,0,0.06f*scale,w,h,scale,scale);
    }

    protected void drawButtonBody(int w, int h, float scale,
        Color base, Graphics2D g2)
    {
            // draw the button body
        Color grad_top = base.brighter();
        Color grad_bot = base.darker();
        GradientPaint bg = new GradientPaint(
            new Point(0,0), grad_top,
            new Point(0,h), grad_bot);
        g2.setPaint(bg);
        fillRoundRect(g2,
            (0)*scale,
            (0)*scale,
            w,h,1*scale,1*scale);

        // draw the inner color
        if (isDrawInnerBlock)
        {
            Color inner = base.brighter();
            inner = alphaColor(inner,75);
            g2.setColor(inner);
            fillRoundRect(g2,
                scale*(.4f),
                scale*(.4f),
                w-scale*.8f, h-scale*.5f,
                scale*.6f,scale*.4f);
        }
    }

    protected void drawText(int w, int h, float scale,
                            String text, Graphics2D g2)
    {
        // calculate the width and height
        int fw = g2.getFontMetrics().stringWidth(text);
        int fh = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();

        int textx = this.getHorizontalAlignment() == JLabel.LEFT ? Math.max(getInsets().left, 2) : (w-fw)/2;
        //int textx = (w-fw)/2;//this.getHorizontalAlignment() == JLabel.LEFT ? (int)(w-scale*.2f) : (w-fw)/2;
        int texty = h/2 + fh/2;


        // draw the text
        g2.setColor(textColorShadow);
        g2.drawString(text,(int)((float)textx+scale*(0.04f)), (int)((float)texty + scale*(0.04f)));
        g2.setColor(textColor);
        g2.drawString(text, textx, texty);

    }


    protected void drawHighlight(int w, int h, float scale, Color base, Graphics2D g2)
    {
        // create the highlight
        GradientPaint highlight = new GradientPaint(
            new Point2D.Float(scale*0.2f,scale*0.2f),
            new Color(255,255,255,175),
            new Point2D.Float(scale*0.2f,scale*0.55f),
            new Color(255,255,255,0)
            );
        g2.setPaint(highlight);
        this.fillRoundRect(g2, scale*0.2f, scale*0.1f,
            w-scale*0.4f, scale*0.4f, scale*0.8f, scale*0.4f);
        drawRoundRect(g2, scale*0.2f, scale*0.1f,
            w-scale*0.4f, scale*0.4f, scale*0.8f, scale*0.4f);
    }

    protected void drawBorder(int w, int h, float scale, Graphics2D g2)
    {
        // draw the border
        g2.setColor(new Color(0,0,0,150));
        drawRoundRect(g2,
            scale*(0f),
            scale*(0f),
            w,h,scale,scale);
    }

    // float version of fill round rect
    protected void fillRoundRect(Graphics2D g2,
                                 float x, float y,
                                 float w, float h,
                                 float ax, float ay)
    {
        if (isRoundedRect)
        {
            g2.fillRoundRect(
                (int)x, (int)y,
                (int)w, (int)h,
                (int)ax, (int)ay
                );
        } else
        {
            g2.fillRoundRect(
                    (int)x, (int)y,
                    (int)w, (int)h,
                    0,0
                    );
        }
    }

    // float version of draw round rect
    protected void drawRoundRect(Graphics2D g2,
                                 float x, float y,
                                 float w, float h,
                                 float ax, float ay)
    {
        if (isRoundedRect)
        {
           g2.drawRoundRect(
                (int)x, (int)y,
                (int)w, (int)h,
                (int)ax, (int)ay
                );
        } else
        {
            g2.drawRoundRect(
                    (int)x, (int)y,
                    (int)w, (int)h,
                    0,0
                    );
        }
    }

    // generate the alpha version of this color
    protected static Color alphaColor(Color color, int alpha)
    {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }


    /* mouse listener implementation */
    protected boolean pressed = false;
    public void mouseExited(MouseEvent evt) { }
    public void mouseEntered(MouseEvent evt) { }
    public void mouseClicked(MouseEvent evt) { }
    public void mouseReleased(MouseEvent evt) {
        pressed = false;
    }
    public void mousePressed(MouseEvent evt)
    {
        pressed = true;
    }

    public boolean isDrawHighlight()
    {
        return isDrawHighlight;
    }

    public void setDrawHighlight(boolean isDrawHighlight)
    {
        this.isDrawHighlight = isDrawHighlight;
    }

    public boolean isDrawInnerBlock()
    {
        return isDrawInnerBlock;
    }

    public void setDrawInnerBlock(boolean isdrawInnerBlock)
    {
        this.isDrawInnerBlock = isdrawInnerBlock;
    }

    public boolean isRoundedRect()
    {
        return isRoundedRect;
    }

    public void setRoundedRect(boolean isRoundedRect)
    {
        this.isRoundedRect = isRoundedRect;
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

    public boolean isBorderPainted()
    {
        return isBorderPainted;
    }

    public void setBorderPainted(boolean isBorderPainted)
    {
        this.isBorderPainted = isBorderPainted;
    }

}