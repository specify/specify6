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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;

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
    
    public SimpleGlassPane(final String text, final int pointSize)
    {
        this.text      = text;
        this.pointSize = pointSize;
        
        setBackground(new Color(0, 0, 0, 220));
        setOpaque(false);
    }

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