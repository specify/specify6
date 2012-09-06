/*
 * Copyright (c) 2007, Romain Guy All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer. * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. * Neither the name of the TimingFramework project nor
 * the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ku.brc.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JComponent;

import edu.ku.brc.ui.dnd.ShadowFactory;
import edu.ku.brc.util.Pair;

/**
 * 
 * @author Romain Guy
 * 
 * (Heavily altered by rods)
 */
public class BubbleGlassPane extends JComponent
{
    private static final int     DEF_BAR_WIDTH      = 400;
    private static final int     DEF_BAR_HEIGHT     = 400;
    private static final int     SHADOW_SIZE        = 20;
    private static final int     CORNERSIZE         = 20;
    private static final int     BTN_MARGIN         = 4;
    private static final int     BTN_HEIGHT         = 26;
    private static final int     LINE_SEP           = 4;
    

    private static final Color   GRADIENT_COLOR1    = Color.DARK_GRAY;
    private static final Color   GRADIENT_COLOR2    = Color.WHITE;

    private Font                 font               = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private Color                color              = new Color(30, 144, 255);
    
    private int                  bubWidth           = DEF_BAR_WIDTH;
    private int                  bubHeight          = DEF_BAR_HEIGHT;
    private BufferedImage        shadowBuffer       = null;
    
    protected Vector<Pair<String, String>> textItems        = new Vector<Pair<String, String>>();
    protected Vector<BubbleBtnInfo>        btnItems         = new Vector<BubbleBtnInfo>();
    protected boolean                      needsBtnLayout   = true;

    /**
     * 
     */
    public BubbleGlassPane()
    {
        setBackground(Color.WHITE);
        setFont(new Font("Default", Font.BOLD, 16));
    }
    
    /**
     * 
     */
    public void clearUI()
    {
        textItems.clear();
        btnItems.clear();
        needsBtnLayout = true;
    }
    
    /**
     * @param label
     * @param value
     */
    public void addLine(final String label, final String value)
    {
        textItems.add(new Pair<String, String>(label, value));
    }
    
    /**
     * @param title
     * @param al
     */
    public void addBtn(final String btnTitle, final ActionListener al)
    {
        btnItems.add(new BubbleBtnInfo(btnTitle, al));
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        // enables anti-aliasing
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(UIHelper.createTextRenderingHints());

        // gets the current clipping area
        //Rectangle clip = g.getClipBounds();

        // Cache old Value to put it back later
        Composite composite = g2.getComposite();

        // sets a 65% translucent composite
        AlphaComposite alpha = AlphaComposite.SrcOver.derive(1.0f);
        g2.setComposite(alpha);

        // fills the background
        // g2.setColor(getBackground());
        // g2.fillRect(clip.x, clip.y, clip.width, clip.height);

        // centers the progress bar on screen
        
        g2.setFont(font);
        FontMetrics fm   = g2.getFontMetrics();
        int newBubHeight = (textItems.size()+1) * (fm.getHeight() + LINE_SEP);
        int txtY         =  (int)((double)fm.getHeight() * 1.25);
        int maxWidth     = 0;
        int maxLbl       = 0;
        for (Pair<String, String> p : textItems)
        {
            int firstLen = fm.stringWidth(p.first);
            int len      = fm.stringWidth(p.second);
            maxWidth     = Math.max(len, maxWidth);
            maxLbl       = Math.max(firstLen, maxLbl);
        }
        int newBubWidth = maxLbl + maxWidth + 25;
        
        if (btnItems.size() > 0)
        {
            newBubHeight += (BTN_MARGIN * 2) + BTN_HEIGHT;
        }
        
        if (newBubWidth != bubWidth || newBubHeight != bubHeight)
        {
            shadowBuffer = null;
            bubWidth     = newBubWidth;
            bubHeight    = newBubHeight;
        }
        
        BufferedImage bgBufImg = getBackgroundImageBuffer(bubWidth, bubHeight);

        int shadowWidth  = bgBufImg.getWidth() - bubWidth;
        int shadowHeight = bgBufImg.getHeight() - bubHeight;

        int left   = (int)((shadowWidth) * 0.5);
        int top    = (int)((shadowHeight)* 0.4);

        int x = (getWidth() - bubWidth) / 2;
        int y = (getHeight() - bubHeight) / 2;

        // computes the size of the progress indicator
        int w = bubWidth;
        int h = bubHeight;
        
        // draws the content of the progress bar
        Paint paint = g2.getPaint();
        
        // bar's background
        Paint gradient = new GradientPaint(x, y, GRADIENT_COLOR1, x, y + h, GRADIENT_COLOR2);
        g2.setPaint(gradient);
        //g2.fillRect(x, y, barWidth, barHeight);
        //g2.fillRoundRect(x, y, barWidth, barHeight, arcSize, arcSize);
        
        // actual progress
        g2.setComposite(AlphaComposite.SrcOver.derive(1.0f));
        
        g2.drawImage(shadowBuffer, x-left, y-top, null);
        
        Color grad_top = color.brighter();
        Color grad_bot = color.darker();        
        GradientPaint bg = new GradientPaint(new Point(x,y), grad_top,
                                             new Point(x,y+h), grad_bot);
        g2.setPaint(bg);
        //g2.fillRect(x, y, w, h);
        g2.fillRoundRect(x, y, w, h, CORNERSIZE, CORNERSIZE);
        //System.out.println(String.format("%d,%d,%d,%d", x,y,x+w,y+h));
        
        g2.setStroke(UIHelper.getStdLineStroke());
        g2.setColor(Color.WHITE);
        for (Pair<String, String> p : textItems)
        {
            int firstLen = fm.stringWidth(p.first);
            g2.drawString(p.first,  x+10+maxLbl-firstLen, y+txtY);
            g2.drawString(p.second, x+10+5+maxLbl, y+txtY);
            txtY += fm.getHeight() + LINE_SEP;
        }
        
        if (needsBtnLayout && btnItems.size() > 0)
        {
            txtY += -(fm.getHeight() / 2) + BTN_MARGIN;
            
            int btnTotal = 0;
            int maxBtnW  = 0;
            for (BubbleBtnInfo bbi : btnItems)
            {
                Rectangle r = new Rectangle();
                r.y      = y + txtY;
                r.width  = fm.stringWidth(bbi.getTitle()) + BTN_HEIGHT;
                r.height = BTN_HEIGHT;
                maxBtnW  = Math.max(maxBtnW, r.width);
                btnTotal += r.width;
                bbi.setRect(r);
            }

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(grad_top);
            
            int lx = x+bubWidth-6   ;
            int ly = y-13;
            g.fillArc(lx, ly, 20, 20, 0, 360);
            
            g2.setColor(Color.WHITE);
            g.drawLine(lx+5, ly+5, lx+13, ly+13);
            g.drawLine(lx+13, ly+5, lx+5, ly+13);

            int gap = (bubWidth - btnTotal) / (btnItems.size() +1);
            int yGap = (BTN_HEIGHT - fm.getHeight()) / 2;
            int bx = x + gap;
            for (BubbleBtnInfo bbi : btnItems)
            {
                Rectangle r = bbi.getRect();
                r.x = bx;
                
                //g2.setColor(Color.BLACK);
                g2.setColor(grad_bot.darker());
                g.fillRoundRect(bx, r.y, r.width, r.height, BTN_HEIGHT, BTN_HEIGHT);
                //System.out.println(r);
                
                g2.setColor(Color.WHITE);
                g.drawString(bbi.getTitle(), bx + (BTN_HEIGHT/2), r.y + yGap + fm.getAscent());
                bx += r.width + gap;
            }
        }

        g2.setPaint(paint);

        // draws the progress bar border
        //g2.drawRect(x, y, barWidth, barHeight);

        g2.setComposite(composite);
    }
    
    private BufferedImage getBackgroundImageBuffer(final int width, final int height)
    {
        if (shadowBuffer == null)
        {
            ShadowFactory factory = new ShadowFactory(SHADOW_SIZE, 0.4f, Color.BLACK);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, image.getWidth(), image.getHeight(), CORNERSIZE, CORNERSIZE);
            g2.dispose();

            shadowBuffer = factory.createShadow(image);
        }
        return shadowBuffer;
    }

    
    //-----------------------------------------------------------------
    class BubbleBtnInfo 
    {
        protected String         title;
        protected Rectangle      rect = null;
        protected ActionListener actionListener;
        
        /**
         * @param title
         * @param actionListener
         */
        public BubbleBtnInfo(String title, ActionListener actionListener)
        {
            super();
            this.title = title;
            this.actionListener = actionListener;
        }
        /**
         * @return the rect
         */
        public Rectangle getRect()
        {
            return rect;
        }
        /**
         * @param rect the rect to set
         */
        public void setRect(Rectangle rect)
        {
            this.rect = rect;
        }
        /**
         * @return the title
         */
        public String getTitle()
        {
            return title;
        }
    }
}
