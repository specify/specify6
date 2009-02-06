/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 28, 2007
 *
 */
public class VerticalSeparator extends JComponent
{
    protected Color bgColor;
    protected Color fgColor;
    protected int   preferredWidth = 10;
    
    /**
     * @param bgColor
     * @param fgColor
     */
    public VerticalSeparator(final Color fgColor, final Color bgColor)
    {
        this.bgColor = bgColor;
        this.fgColor = fgColor;
    }

    /**
     * @param bgColor
     * @param fgColor
     */
    public VerticalSeparator(final Color fgColor, final Color bgColor, final int preferredWidth)
    {
        this.bgColor = bgColor;
        this.fgColor = fgColor;
        this.preferredWidth = preferredWidth;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        ((Graphics2D)g).setRenderingHints(UIHelper.createTextRenderingHints());
        
        Dimension size = getSize();
        
        int x = size.width / 2;
        
        g.setColor(bgColor);
        g.drawLine(x, 2, x, size.height-1);
        //g.drawLine(x+1, 2, x+1, size.height-1);
        
        g.setColor(fgColor);
        g.drawLine(x-2, 0, x-2, size.height-3);
        //g.drawLine(x-1, 0, x-1, size.height-3);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        Dimension size = super.getPreferredSize();
        size.width = preferredWidth;
        return size;
    }

}
