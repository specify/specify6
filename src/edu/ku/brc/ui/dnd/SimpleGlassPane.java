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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
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
        
        addMouseListener(new MouseAdapter() {

            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseClicked(MouseEvent e)
            {
                e.consume();
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseDragged(MouseEvent e)
            {
                e.consume();
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseEntered(MouseEvent e)
            {
                e.consume();
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseExited(MouseEvent e)
            {
                e.consume();
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseMoved(MouseEvent e)
            {
                e.consume();
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
             */
            @Override
            public void mousePressed(MouseEvent e)
            {
                e.consume();
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseReleased(MouseEvent e)
            {
                e.consume();
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
             */
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                e.consume();
            }
        });
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
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect(tx-(expand / 2)+4, ty-fm.getAscent()-(expand / 2)+6, tw+expand, th+expand, arc, arc);
        
        g2.setColor(UIHelper.isMacOS() ? Color.WHITE : new Color(200, 220, 255));
        g2.fillRoundRect(tx-(expand / 2), ty-fm.getAscent()-(expand / 2), tw+expand, th+expand, arc, arc);
        
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(tx-(expand / 2), ty-fm.getAscent()-(expand / 2), tw+expand, th+expand, arc, arc);
        
        g2.setColor(Color.BLACK);
        g2.drawString(text, tx, ty);
        g2.dispose();
    }
    
}