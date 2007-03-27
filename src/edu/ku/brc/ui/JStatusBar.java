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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * This is a statusbar component. It currently enables a progressbar to be shown and
 * hidden when "indeterminate" is turned on and off. The status always comes with a "main"
 * status label area. The numer of sections passed in to the constructor can indicate how many
 * additional sections are added to the right of the main status label.
 *
 * @code_status Complete
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
    protected JLabel[]     labels      = null;
    protected JProgressBar progressBar = null;
    
    protected Icon         errorIcon   = null;
    
    protected Exception lastException = null;

    /**
     * Default Constructor.
      */
    public JStatusBar()
    {
        this(null);
    }
    
    /**
     * Constructor.
     * @param sectionSize can be null, otherwise each element of the array indicates a static size in chars 
     * for the additional section in the statusbar
     */
    public JStatusBar(final int[] sectionSize)
    {
        setLayout(new BorderLayout());

        setBorder(new MyBevelBorder());

        statusLabel = new JLabel(" ");
        progressBar = new JProgressBar(0, 100);

        statusLabel.setPreferredSize(new Dimension(100, statusLabel.getPreferredSize().height));
        progressBar.setPreferredSize(new Dimension(100, statusLabel.getPreferredSize().height));
        
        PanelBuilder builder = new PanelBuilder(new FormLayout("f:p:g,2px,right:p" + (UIHelper.getOSType() == UIHelper.OSTYPE.MacOSX ? ",15px" : ""), "p"), this);
        CellConstraints cc = new CellConstraints();
        if (sectionSize == null)
        {
            builder.add(statusLabel, cc.xy(1,1));
            
        } else
        {
            StringBuilder str = new StringBuilder("f:p:g");
            for (int size : sectionSize)
            {
                str.append("," + size + "dlu");
                str.append(",p");
            }
            PanelBuilder sbBldr = new PanelBuilder(new FormLayout(str.toString(), "p"));
            sbBldr.add(statusLabel, cc.xy(1, 1));
            
            labels = new JLabel[sectionSize.length];
            for (int i=0;i<sectionSize.length;i++)
            {
                labels[i] = new JLabel(" ", SwingConstants.CENTER);
                labels[i].setBorder(new EndsBorder(i == sectionSize.length-1));
                sbBldr.add(labels[i], cc.xy(i+2, 1));
            }
            builder.add(sbBldr.getPanel(), cc.xy(1,1));
        }
        builder.add(progressBar, cc.xy(3,1));
        progressBar.setVisible(false);
        progressBar.setValue(0);

        statusLabel.setForeground(NORMAL_COLOR);

        statusLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount()==2 && lastException!=null)
                {
                    String message = lastException.getLocalizedMessage();
                    JOptionPane.showMessageDialog(getParent(), message, statusLabel.getText(), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    public void setErrorIcon(Icon icon)
    {
        this.errorIcon = icon;
    }

    /**
     * Sets text into the statusbar and clear the foreground color (sets it to "normal").
     * 
     * @param text the text of the status bar
     */
    public void setText(final String text)
    {
        statusLabel.setForeground(NORMAL_COLOR);
        this.lastException = null;
        statusLabel.setText(text);
        statusLabel.setIcon(null);
        statusLabel.repaint();
    }

    /**
     * Sets text into the statusbar's section.
     * @param sectionInxwhich section text to sets
     * @param text the text of the status bar
     */
    public void setSectionText(final int sectionInx, final String text)
    {
        if (labels != null && sectionInx > -1 && sectionInx < labels.length)
        {
            labels[sectionInx].setText(text);
        }
    }

    /**
     * This is just a helper method that combines the work of the {@link #setText(String)}
     * and {@link #setAsError()} methods.
     * 
     * @param message the the text of the error message
     */
    public void setErrorMessage(String message, Exception e)
    {
        setText(message);
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setIcon(errorIcon);
        this.lastException = e;
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

    /**
     * Returns the ProgressBar.
     * @return the ProgressBar
     */
    public JProgressBar getProgressBar()
    {
        return progressBar;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
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

        @Override
        protected void paintLoweredBevel(Component c,
                                         Graphics g,
                                         int x,
                                         int y,
                                         int width,
                                         int height)
        {
            Color oldColor = g.getColor();
            int h = height;
            int w = width;

            g.translate(x, y);

            g.setColor(getShadowInnerColor(c));
            g.drawLine(0, 0, 0, h - 1);
            g.drawLine(1, 0, w - 1, 0);

            // g.setColor(getShadowOuterColor(c));
            // g.drawLine(1, 1, 1, h-2);
            // g.drawLine(2, 1, w-2, 1);

            g.setColor(getHighlightOuterColor(c));
            g.drawLine(1, h - 1, w - 1, h - 1);
            g.drawLine(w - 1, 1, w - 1, h - 2);

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
        private static final int BGM_WIDTH  = 13;
        private static final int BGM_HEIGHT = 13;

        public int getIconHeight()
        {
            return BGM_WIDTH;
        }

        public int getIconWidth()
        {
            return BGM_HEIGHT;
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
    
    public class EndsBorder extends BevelBorder
    {
        protected Insets insets;
        
        public EndsBorder(boolean bothEnds)
        {
            super(BevelBorder.LOWERED);
            insets = new Insets(2, 0, 0, bothEnds ? 2 : 0);
        }

        @Override
        protected void paintLoweredBevel(Component c,
                                         Graphics g,
                                         int x,
                                         int y,
                                         int width,
                                         int height)
        {
            Color oldColor = g.getColor();
            int h = height;
            int w = width;

            g.translate(x, y);

            g.setColor(getShadowInnerColor(c));
            g.drawLine(0, 0, 0, h - 1);
            
            g.setColor(getHighlightOuterColor(c));
            g.drawLine(1, 0, 1, h - 1);
            
            if (insets.right > 0)
            {
                g.setColor(getShadowInnerColor(c));
                g.drawLine(w - 2, 1, w - 2, h - 2);
                
                g.setColor(getHighlightOuterColor(c));
                g.drawLine(w - 1, 1, w - 1, h - 2);
 
            }

            g.translate(-x, -y);
            g.setColor(oldColor);

        }
        
        /* (non-Javadoc)
         * @see javax.swing.border.Border#getBorderInsets(java.awt.Component)
         */
        @Override
        public Insets getBorderInsets(Component c)
        {
            return insets;
        }
    }

}
