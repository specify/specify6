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
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * 
 * @author Romain Guy
 * 
 * (Heavily altered by rods)
 */
public class ProgressGlassPane extends JComponent
{
    private static final int     BAR_WIDTH          = 400;
    private static final int     BAR_HEIGHT         = 20;

    private static final Color   GRADIENT_COLOR2    = Color.WHITE;
    private static final Color   GRADIENT_COLOR1    = Color.GRAY;

    private int                  progress           = -1;
    private int                  textOffset         = 50;
    private Color                color              = new Color(30, 144, 255);

    /**
     * 
     */
    public ProgressGlassPane()
    {
        setBackground(Color.WHITE);
        setFont(new Font("Default", Font.BOLD, 16));
    }

    /**
     * @return the progress (0-100)
     */
    public int getProgress()
    {
        return progress;
    }

    /**
     * Sets the progress from 0 - 100
     * @param progress (0-100)
     */
    public void setProgress(final int progress)
    {
        int oldProgress = this.progress;
        this.progress   = progress;
        
        if (progress > -1 && progress < 101)
        {
            this.progress = Math.min(100, this.progress);
            
            // computes the damaged area
            FontMetrics metrics = getGraphics().getFontMetrics(getFont());
            int w = (int) (BAR_WIDTH * ((float) oldProgress / 100.0f));
            int x = w + (getWidth() - BAR_WIDTH) / 2;
            int y = (getHeight() - BAR_HEIGHT) / 2;
            y += metrics.getDescent() / 2;
            y += textOffset;
            w = (int) (BAR_WIDTH * ((float) this.progress / 100.0f)) - w;
            int h = BAR_HEIGHT;
    
            repaint(x, y, x+BAR_WIDTH, h+1);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        if (progress > -1)
        {
            // enables anti-aliasing
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
            // gets the current clipping area
            //Rectangle clip = g.getClipBounds();
    
            // sets a 65% translucent composite
            AlphaComposite alpha = AlphaComposite.SrcOver.derive(0.65f);
            Composite composite = g2.getComposite();
            g2.setComposite(alpha);
    
            // fills the background
            // g2.setColor(getBackground());
            // g2.fillRect(clip.x, clip.y, clip.width, clip.height);
    
            // centers the progress bar on screen
            FontMetrics metrics = g.getFontMetrics();
            int x = (getWidth() - BAR_WIDTH) / 2;
            int y = (getHeight() - BAR_HEIGHT - metrics.getDescent()) / 2;
    
            y += textOffset;
    
            // goes to the position of the progress bar
            y += metrics.getDescent();
    
            // computes the size of the progress indicator
            int w = (int) (BAR_WIDTH * ((float) progress / 100.0f));
            int h = BAR_HEIGHT;
    
            // draws the content of the progress bar
            Paint paint = g2.getPaint();
    
            // bar's background
            Paint gradient = new GradientPaint(x, y, GRADIENT_COLOR1, x, y + h, GRADIENT_COLOR2);
            g2.setPaint(gradient);
            g2.fillRect(x, y, BAR_WIDTH, BAR_HEIGHT);
    
            // actual progress
            g2.setComposite(AlphaComposite.SrcOver.derive(0.90f));
            
            Color grad_top = color.brighter();
            Color grad_bot = color.darker();        
            GradientPaint bg = new GradientPaint(new Point(x,y), grad_top,
                                                 new Point(x,y+h), grad_bot);
            g2.setPaint(bg);
            g2.fillRect(x, y, w, h);
    
            g2.setPaint(paint);
    
            // draws the progress bar border
            g2.drawRect(x, y, BAR_WIDTH, BAR_HEIGHT);
    
            g2.setComposite(composite);
        }
    }
}
