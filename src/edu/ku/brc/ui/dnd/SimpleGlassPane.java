/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui.dnd;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;

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
    
    /**
     * @param text
     * @param pointSize
     */
    public SimpleGlassPane(final String text, 
                           final int pointSize)
    {
        this.text      = text;
        this.pointSize = pointSize;
        
        setBackground(new Color(0, 0, 0, 220));
        setOpaque(false);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paintComponent(g);
        
        Graphics2D    g2     = (Graphics2D)g;
        
        Dimension size = getSize();
        
        JStatusBar statusBar = UIRegistry.getStatusBar();
        if (statusBar != null)
        {
            size.height -= statusBar.getSize().height;
        }
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 128));
        g2.fillRect(0, 0, size.width, size.height);
        
        
        g2.setFont(new Font((new JLabel()).getFont().getName(), Font.BOLD, pointSize));
        FontMetrics fm = g2.getFontMetrics();
        
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();
        int tx = (size.width - tw) / 2;
        int ty = (size.height - th) / 2;
        
        int expand = 20;
        int arc    = expand * 2;
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(tx-(expand / 2), ty-fm.getAscent()-(expand / 2), tw+expand, th+expand, arc, arc);
        
        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(tx-(expand / 2), ty-fm.getAscent()-(expand / 2), tw+expand, th+expand, arc, arc);
        
        g2.setColor(Color.BLACK);
        g2.drawString(text, tx, ty);
        g2.dispose();
    }
    
}