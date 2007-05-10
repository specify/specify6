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
 * @author rods, jstewart
 *
 */
public class JStatusBar extends JPanel
{
    private static final Color ERROR_COLOR   = Color.RED;
    private static final Color NORMAL_COLOR  = Color.BLACK;
    private static final Color WARNING_COLOR = Color.ORANGE;

    protected JLabel       statusLabel = null;
    protected JLabel[]     labels      = null;
    protected JProgressBar progressBar = null;
    
    protected Icon         errorIcon   = null;
    protected Icon         warningIcon = null;
    
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
                if (e.getClickCount() == 2 && lastException != null)
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
    
    public void setWarningIcon(Icon icon)
    {
        this.warningIcon = icon;
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
        statusLabel.setToolTipText(text);
        statusLabel.setIcon(null);
        statusLabel.repaint();
    }

    /**
     * Sets text into the statusbar's section.
     * 
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
     * Sets the given message into the statusbar.  The message is displayed
     * using the current error color and icon.
     * 
     * @param message the text of the error message
     * @param e the exception
     */
    public void setErrorMessage(String message, Exception e)
    {
        setText(message);
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setIcon(errorIcon);
        this.lastException = e;
    }

    /**
     * Sets the given message into the statusbar.  The message is displayed
     * using the current error color and icon.
     * 
     * @param message the the text of the error message
     */
    public void setErrorMessage(String message)
    {
        setText(message);
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setIcon(errorIcon);
        this.lastException = null;
    }

    /**
     * Sets the given message into the statusbar.  The message is displayed
     * using the current warning color and icon.
     * 
     * @param message the text of the warning message
     * @param e the exception
     */
    public void setWarningMessage(String message, Exception e)
    {
        setText(message);
        statusLabel.setForeground(WARNING_COLOR);
        statusLabel.setIcon(warningIcon);
        this.lastException = e;
    }

    /**
     * Sets the given message into the statusbar.  The message is displayed
     * using the current warning color and icon.
     * 
     * @param message the the text of the warning message
     */
    public void setWarningMessage(String message)
    {
        setText(message);
        statusLabel.setForeground(WARNING_COLOR);
        statusLabel.setIcon(warningIcon);
        this.lastException = null;
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

            g.setColor(getHighlightOuterColor(c));
            g.drawLine(1, h - 1, w - 1, h - 1);
            g.drawLine(w - 1, 1, w - 1, h - 2);

            g.translate(-x, -y);
            g.setColor(oldColor);

        }
    }

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
