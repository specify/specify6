/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui.dnd;

import static edu.ku.brc.ui.UIHelper.isMacOS;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

/**
 * Simple glass pane that writes and centers text while fading the background.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 2, 2008
 *
 */
public class SimpleGlassPane extends JPanel
{
    private String text;
    private int    pointSize;
    private Color  textColor    = null;
    private Color  fillColor    = new Color(0, 0, 0, 50);
    private Insets margin       = new Insets(0,0,0,0);
    
    private  boolean          useBGImage       = false;
    private  boolean          hideOnClick      = false;
    private  BufferedImage    img              = null;
    private  DelegateRenderer delegateRenderer = null;

    
    /**
     * @param text
     * @param pointSize
     */
    public SimpleGlassPane(final String text, 
                           final int pointSize)
    {
        this(text, pointSize, true);
    }
    
    /**
     * @param text
     * @param pointSize
     */
    public SimpleGlassPane(final String text, 
                           final int pointSize,
                           final boolean doBlockMouseEvents)
    {
        this.text      = text;
        this.pointSize = pointSize;
        
        setBackground(fillColor);
        setOpaque(false);
        
        /*if (doBlockMouseEvents)
        {
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e)   { checkMouseEvent(e);   super.mouseClicked(e);}
                @Override public void mouseDragged(MouseEvent e)   { checkMouseEvent(e);   super.mouseDragged(e);}
                @Override public void mouseEntered(MouseEvent e)   { checkMouseEvent(e);   super.mouseEntered(e);}
                @Override public void mouseExited(MouseEvent e)    { checkMouseEvent(e);   super.mouseExited(e);}
                @Override public void mouseMoved(MouseEvent e)     { checkMouseEvent(e);   super.mouseMoved(e);}
                @Override public void mousePressed(MouseEvent e)   { checkMouseEvent(e);   super.mousePressed(e);}
                @Override public void mouseReleased(MouseEvent e)  { checkMouseEvent(e);   super.mouseReleased(e);}
                @Override public void mouseWheelMoved(MouseWheelEvent e) { checkMouseEvent(e); }
            });
        }*/
    }
    
    /**
     * @return
     */
    private Rectangle getInternalBounds()
    {
        Rectangle r = getBounds();
        r.x      += margin.left;
        r.y      += margin.top;
        r.width  -= margin.left + margin.right;
        r.height -= margin.top + margin.bottom;
        return r;
    }

    protected void checkMouseEvent(MouseEvent e)
    {
        Rectangle r = getInternalBounds();
        Point p = e.getPoint();
        
        if (r.contains(p))
        {
            System.out.println("consumed");
            e.consume();
        }
    }
    /**
     * @return the margin
     */
    public Insets getMargin()
    {
        return margin;
    }

    /**
     * @param margin the margin to set
     */
    public void setMargin(Insets margin)
    {
        this.margin = margin;
    }

    /**
     * @param fillColor the fillColor to set
     */
    public void setFillColor(Color fillColor)
    {
        this.fillColor = fillColor;
    }

    /**
     * @param textColor the textColor to set
     */
    public void setTextColor(Color textColor)
    {
        this.textColor = textColor;
    }

    /**
     * @param useBGImage the useBGImage to set
     */
    public void setUseBGImage(boolean useBGImage)
    {
        this.useBGImage = useBGImage;
    }
    
    /**
     * @param delegateRenderer the delegateRenderer to set
     */
    public void setDelegateRenderer(DelegateRenderer delegateRenderer)
    {
        this.delegateRenderer = delegateRenderer;
    }

    /**
     * @param hideOnClick the hideOnClick to set
     */
    public void setHideOnClick(boolean hideOnClick)
    {
        this.hideOnClick = hideOnClick;
    }

    @Override
    public boolean contains(final int x, final int y)
    {
        Rectangle r = getInternalBounds();
        return !r.contains(x, y);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics graphics)
    {
        Graphics2D g = (Graphics2D)graphics;
        
        Rectangle rect = getInternalBounds();
        int width  = rect.width;
        int height = rect.height;
        
        if (useBGImage)
        {
            // Create a translucent intermediate image in which we can perform
            // the soft clipping
            GraphicsConfiguration gc  = g.getDeviceConfiguration();
            if (img == null || img.getWidth() != width || img.getHeight() != height)
            {
                img = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            }
            Graphics2D g2 = img.createGraphics();
            
            // Clear the image so all pixels have zero alpha
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, width, height);
            
            g2.setComposite(AlphaComposite.Src);
            g2.setColor(new Color(0, 0, 0, 85));
            g2.fillRect(0, 0, width, height);
    
            if (delegateRenderer != null)
            {
                delegateRenderer.render(g, g2, img);
            }
    
            g2.dispose();
    
            // Copy our intermediate image to the screen
            g.drawImage(img, rect.x, rect.y, null);
        }
        
        if (StringUtils.isNotEmpty(text))
        {
            Graphics2D g2 = (Graphics2D)graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRect(margin.left, margin.top, rect.width, rect.height);
            
            g2.setFont(new Font((new JLabel()).getFont().getName(), Font.BOLD, pointSize));
            FontMetrics fm = g2.getFontMetrics();
            
            int tw = fm.stringWidth(text);
            int th = fm.getHeight();
            int tx = (rect.width - tw) / 2;
            int ty = (rect.height - th) / 2;
            
            int expand = 20;
            int arc    = expand * 2;
            g2.setColor(Color.LIGHT_GRAY);
            
            int x = margin.left + tx - (expand / 2);
            int y = margin.top  + ty-fm.getAscent()-(expand / 2);
            
            g2.fillRoundRect(x+4, y+6, tw+expand, th+expand, arc, arc);
            
            g2.setColor(isMacOS() ? Color.WHITE : new Color(200, 220, 255));
            g2.fillRoundRect(x, y, tw+expand, th+expand, arc, arc);
            
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(x, y, tw+expand, th+expand, arc, arc);
            
            g2.setColor(textColor == null ? Color.BLACK : textColor);
            g2.drawString(text, tx, ty);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#processMouseEvent(java.awt.event.MouseEvent)
     */
    @Override
    protected void processMouseEvent(MouseEvent e)
    {
        if (hideOnClick)
        {
            hideOnClick = false;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    SimpleGlassPane.this.setVisible(false);
                }
            });
        }
        super.processMouseEvent(e);
    }

    /**
     * @author rod
     *
     * @code_status Alpha
     *
     * Dec 21, 2008
     *
     */
    public interface DelegateRenderer
    {
        public abstract void render(Graphics2D compG, Graphics2D imgG, BufferedImage bufImg);
    }
    
}