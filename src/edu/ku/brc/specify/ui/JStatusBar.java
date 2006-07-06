/* Filename:    $RCSfile: JStatusBar.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/05/05 19:59:54 $
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

import javax.swing.*;
import javax.swing.border.BevelBorder;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.*;

/**
 * This is a statusbar component. It currently enables a progressbar to be show and 
 * hidden when "indeterminate" is turned on and off
 * 
 * @author rods
 *
 */
public class JStatusBar extends JPanel
{
    // RGB values discovered using ZoomIn
    //private static final Color WHITE_LINE_COLOR = new Color(255, 255, 255);
    //private static final Color GRAY_LINE_COLOR  = new Color(172, 168, 153);
    
    private static final Color ERROR_COLOR  = Color.RED;
    private static final Color NORMAL_COLOR = Color.BLACK;
    
    protected JLabel       statusLabel = null;
    protected JProgressBar progressBar = null;

    /**
     * Constructor
     */
    public JStatusBar()
    {
        setLayout(new BorderLayout());

        setBorder(new MyBevelBorder());
        
        statusLabel = new JLabel(" ");
        progressBar = new JProgressBar(0, 100);
        
        statusLabel.setPreferredSize(new Dimension(100, statusLabel.getPreferredSize().height));
        progressBar.setPreferredSize(new Dimension(100, statusLabel.getPreferredSize().height));
        
        PanelBuilder builder = new PanelBuilder(new FormLayout("f:p:g,2px,right:p", "p"), this);
        CellConstraints cc = new CellConstraints();
        builder.add(statusLabel, cc.xy(1,1));
        builder.add(progressBar, cc.xy(3,1));
        progressBar.setVisible(false);
        progressBar.setValue(0);
        
        statusLabel.setForeground(NORMAL_COLOR);

    }
    
    /**
     * Sets text into the statusbar and clear the foreground color (sets it to "normal").
     * @param text the text of the status bar
     */
    public void setText(final String text)
    {
        statusLabel.setForeground(NORMAL_COLOR);
        statusLabel.setText(text);
    }
    
    /**
     * Sets the text's forground color to be in the "error" color
     */
    public void setAsError()
    {
        statusLabel.setForeground(ERROR_COLOR);

    }
    
    /**
     * Sets the progressbar as being indeterminate and sets the visibility to true
     * @param isIndeterminate whether it should be shown and set to be Indeterminate
     */
    public void setIndeterminate(final boolean isIndeterminate)
    {
        progressBar.setIndeterminate(isIndeterminate);
        progressBar.setVisible(isIndeterminate);
        validate();
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        /*int y = 0;
        g.setColor(new Color(156, 154, 140));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(196, 194, 183));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(218, 215, 201));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(233, 231, 217));
        g.drawLine(0, y, getWidth(), y);

        y = getHeight() - 3;
        g.setColor(new Color(233, 232, 218));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(233, 231, 216));
        g.drawLine(0, y, getWidth(), y);
        y = getHeight() - 1;
        g.setColor(new Color(221, 221, 220));
        g.drawLine(0, y, getWidth(), y);
        */

    }
    
    public class MyBevelBorder extends BevelBorder
    {
        public MyBevelBorder()
        {
            super(BevelBorder.LOWERED);
        }
        protected void paintLoweredBevel(Component c, Graphics g, int x, int y,
                                         int width, int height)  {
         Color oldColor = g.getColor();
         int h = height;
         int w = width;

         g.translate(x, y);

         g.setColor(getShadowInnerColor(c));
         g.drawLine(0, 0, 0, h-1);
         g.drawLine(1, 0, w-1, 0);

         //g.setColor(getShadowOuterColor(c));
         //g.drawLine(1, 1, 1, h-2);
         //g.drawLine(2, 1, w-2, 1);

         g.setColor(getHighlightOuterColor(c));
         g.drawLine(1, h-1, w-1, h-1);
         g.drawLine(w-1, 1, w-1, h-2);

         //g.setColor(getHighlightInnerColor(c));
        // g.drawLine(2, h-2, w-2, h-2);
        // g.drawLine(w-2, 2, w-2, h-3);

         g.translate(-x, -y);
         g.setColor(oldColor);

     }
    }

    /*
    public class AngledLinesWindowsCornerIcon implements Icon
    {

        // Dimensions
        private static final int WIDTH  = 13;
        private static final int HEIGHT = 13;

        public int getIconHeight()
        {
            return WIDTH;
        }

        public int getIconWidth()
        {
            return HEIGHT;
        }

        public void paintIcon(Component c, Graphics g, int x, int y)
        {

            g.setColor(WHITE_LINE_COLOR);
            g.drawLine(0, 12, 12, 0);
            g.drawLine(5, 12, 12, 5);
            g.drawLine(10, 12, 12, 10);

            g.setColor(GRAY_LINE_COLOR);
            g.drawLine(1, 12, 12, 1);
            g.drawLine(2, 12, 12, 2);
            g.drawLine(3, 12, 12, 3);

            g.drawLine(6, 12, 12, 6);
            g.drawLine(7, 12, 12, 7);
            g.drawLine(8, 12, 12, 8);

            g.drawLine(11, 12, 12, 11);
            g.drawLine(12, 12, 12, 12);

        }
    }
    */
}
