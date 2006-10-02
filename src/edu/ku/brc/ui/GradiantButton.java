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

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.AlphaComposite;
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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * A gradiant filled button button.
 * A renderer need to be created so GradiantLabel and GradiantButton can share all the code.
 *
 * When the icon is set into the JButton constructor it changes the font size
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class GradiantButton extends JButton implements MouseListener 
{
    /**
     * 
     */
    protected Color          textColor        = null;
    protected Color          textColorShadow  = null;
    protected float          iconAlpha        = 0.8f;
    protected ImageIcon      icon             = null;
    protected GradiantButton itself;
    
    /**
     * Defaults to a gradiant square button
     * @param text the label on the Button
     */
    public GradiantButton(String text) 
    {
        super(text);
        init();
    }
    
    /**
     * Defaults to a gradiant square button
     * @param icon the icon to be displayed without text
     */
    public GradiantButton(final ImageIcon icon) 
    {
        super("");
        this.icon = icon;
        init();
    }
    
    /**
     * Helper method for constructors
     */
    protected void init()
    {
        itself = this;
        setTextColor(Color.BLACK);
        setBorder(new EmptyBorder(0,0,0,0));
        super.setBorderPainted(false);
        this.addMouseListener(this);
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() 
    {
        String text = getText();
        FontMetrics fm = this.getFontMetrics(getFont());
        float scale = (50f/40f)*this.getFont().getSize2D();
        int w = fm.stringWidth(text);
        w += (int)(scale*1.4f);
        int h = fm.getHeight();
        h += (int)(scale*.3f);
        return new Dimension(w, h);
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) 
    {        
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = this.getWidth();
        int h = this.getHeight();
  
        drawButtonBody(g2, w,h, getForeground());
        
        if (pressed) 
        {
            g2.translate(1, 1);
        }
        
       String text = getText();
        if (isNotEmpty(text))
        {
            drawText(g2, w,h, getText());
        }
        
        if (icon != null)
        {
            //Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, iconAlpha));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
           icon.paintIcon(this, g2, (w - icon.getIconWidth()) / 2, (h - icon.getIconHeight()) / 2);
        }
    }
    
    /**
     * Draws the button body
     * @param g2 the graphics to be painted into
     * @param w the width of the control
     * @param h the height of the control
     * @param color the of the background
     */
    protected void drawButtonBody(Graphics2D g2, int w, int h, Color color) 
    {
        // draw the button body
        Color grad_top = color.brighter();
        Color grad_bot = color.darker();        
        GradientPaint bg = new GradientPaint(new Point(0,0), grad_top,
                                             new Point(0,h), grad_bot);
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);
    }
    
    /**
     * Paints the text of the control
     * @param g2 the graphics to be painted into
     * @param w the width of the control
     * @param h the height of the control
     * @param text the string
     */
    protected void drawText(Graphics2D g2, int w, int h, String text) 
    {
        // calculate the width and height
        int fw = g2.getFontMetrics().stringWidth(text);
        int fh = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();
        
        int textx = this.getHorizontalAlignment() == SwingConstants.LEFT ? Math.max(getInsets().left, 2) : (w-fw)/2;
        int texty = h/2 + fh/2;

        // draw the text
        g2.setColor(textColorShadow);
        g2.drawString(text,textx, texty);
        g2.setColor(textColor);
        g2.drawString(text, textx, texty);

    }
    
     /**
     * Generate the alpha version of this color
     * @param color the color in question
     * @param alpha the alpha of the new color
     * @return Generate the alpha version of this color
     */ 
    protected static Color alphaColor(Color color, int alpha) 
    {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    
    /**
     * @return returns the text color
     */
    public Color getTextColor()
    {
        return textColor;
    }

    /**
     * Sests the text Color
     * @param textColor text color
     */
    public void setTextColor(Color textColor)
    {
        this.textColor = textColor;
        textColorShadow = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 70);
    }

    public void setIconAlpha(float iconAlpha)
    {
        this.iconAlpha = iconAlpha;
    }

   
    //----------------------------------------------------------
    //-- MouseListener Implementation
    //----------------------------------------------------------
    protected boolean pressed = false;
    public void mouseExited(MouseEvent evt) 
    { 
        UICacheManager.displayStatusBarText("");
    }
    public void mouseEntered(MouseEvent evt) 
    { 
        UICacheManager.displayStatusBarText(itself.getToolTipText());
    }
    public void mouseClicked(MouseEvent evt)
    {
        // do nothing
    }
    
    public void mouseReleased(MouseEvent evt) 
    { 
        pressed = false;
    }
    
    public void mousePressed(MouseEvent evt) 
    {
        pressed = true;
    }
    
    
}
