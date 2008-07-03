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

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createProgressBar;
import static edu.ku.brc.ui.UIHelper.setControlSize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

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
    
    protected Vector<ProgressItem>            prgItemsRecycler = new Vector<ProgressItem>();
    protected Vector<ProgressItem>            prgItems         = new Vector<ProgressItem>();
    protected Hashtable<String, ProgressItem> prgHash          = new Hashtable<String, ProgressItem>();
    

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

        statusLabel = createLabel(" ");
        progressBar = createProgressBar(0, 100);

        statusLabel.setPreferredSize(new Dimension(100, statusLabel.getPreferredSize().height));
        progressBar.setPreferredSize(new Dimension(150, statusLabel.getPreferredSize().height));
        
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
                str.append(",max(p;" + size + "dlu)");
                str.append(",p");
            }
            PanelBuilder sbBldr = new PanelBuilder(new FormLayout(str.toString(), "p"));
            sbBldr.add(statusLabel, cc.xy(1, 1));
            
            labels = new JLabel[sectionSize.length];
            for (int i=0;i<sectionSize.length;i++)
            {
                labels[i] = new JLabel(" ", SwingConstants.CENTER);
                setControlSize(labels[i]);
                labels[i].setBorder(new CompoundBorder(new EndsBorder(i == sectionSize.length-1), new EmptyBorder(0,5,0,5)));
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
     * @param name
     * @return
     */
    protected synchronized ProgressItem getProgressItem(final String name)
    {
        ProgressItem item = prgHash.get(name);
        if (item == null)
        {
            if (prgItemsRecycler.size() == 0)
            {
                item = new ProgressItem(name);
            } else
            {
                item = prgItemsRecycler.remove(0);
                item.setName(name);
            }
            item.clear();
            prgHash.put(name, item);
            prgItems.add(item);
        }
        return item;
    }
    
    /**
     * @param name
     */
    protected synchronized void progressDone(final String name)
    {
        ProgressItem item = prgHash.get(name);
        if (item != null)
        {
            prgHash.remove(name);
            prgItems.remove(item);
            prgItemsRecycler.add(item);
            
            if (prgItems.size() > 0)
            {
                updateProgress(prgItems.get(0));
            }
        }
    }
    
    /**
     * @param item
     * @return
     */
    protected boolean isCurrent(final ProgressItem item)
    {
        //if (prgItems.size() > 0)
        //{
        //    System.err.println("Current: "+prgItems.get(0).getName());
        //}
        return prgItems.size() > 0 && prgItems.get(0) == item;
    }

    /**
     * Sets the progressbar as being indeterminate and sets the visibility to true
     * @param isIndeterminate whether it should be shown and set to be Indeterminate
     */
    public synchronized void setIndeterminate(final String name, final boolean isIndeterminate)
    {
        setIndeterminate(name, isIndeterminate, false);
    }
    
    /**
     * Sets the progressbar as being indeterminate and sets the visibility to true
     * @param isIndeterminate whether it should be shown and set to be Indeterminate
     * @param usePlatformLnF some platforms have special UI for Indeterminate progress bars (Mac)
     */
    public synchronized void setIndeterminate(final String name, 
                                              final boolean isIndeterminate, 
                                              final boolean usePlatformLnF)
    {
        if (progressBar != null)
        {
            ProgressItem item = getProgressItem(name);
            if (item != null)
            {
                item.setIndeterminate(isIndeterminate);
                item.setUsePlatformUI(usePlatformLnF);
                updateProgress(item);
            }
        }
    }
    
    /**
     * @param name
     */
    public synchronized void incrementRange(final String name)
    {
        if (progressBar != null)
        {
            if (prgHash.get(name) == null)
            {
                setProgressRange(name, 0, 1, 0);
                
            } else
            {
                ProgressItem item = getProgressItem(name);
                item.setMax(item.getMax()+1);
                updateProgress(item);    
            }
        }
    }
    
    /**
     * @param name
     */
    public synchronized void incrementValue(final String name)
    {
        if (progressBar != null)
        {
            if (prgHash.get(name) == null)
            {
                setProgressRange(name, 0, 1);
                
            } 
            
            ProgressItem item = getProgressItem(name);
            item.setValue(item.getValue()+1);
            updateProgress(item); 
        }
    }
    
    /**
     * Sets the progressbar as being indeterminate and sets the visibility to true
     * @param isIndeterminate whether it should be shown and set to be Indeterminate
     * @param usePlatformLnF some platforms have special UI for Indeterminate progress bars (Mac)
     */
    public synchronized void setProgressDone(final String name)
    {
        if (progressBar != null)
        {
            ProgressItem item = getProgressItem(name);
            if (item != null)
            {
                item.clear();
                item.setIndStatusChanged(true);
                
                updateProgress(item);
                
                prgHash.remove(name);
                prgItems.remove(item);
                prgItemsRecycler.add(item);
            }
        }
    }
    
    /**
     * @param item
     */
    protected void updateProgress(final ProgressItem item)
    {
        if (isCurrent(item))
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    if (item.isIndStatusChanged())
                    {
                        if (UIHelper.isMacOS())
                        {
                            progressBar.putClientProperty("JProgressBar.style", item.isUsePlatformUI() ? "circular" : null);
                        }
                        progressBar.setIndeterminate(item.isIndeterminate());
                    }
                    
                    if (!item.isIndeterminate() && item.hasRange())
                    {
                        progressBar.setMinimum(item.getMin());
                        progressBar.setMaximum(item.getMax());
                        progressBar.setValue(item.getValue());
                        
                        if (item.getMax() == item.getValue())
                        {
                            setProgressDone(item.getName());
                            return;
                        }
                    }
                    
                    if (progressBar.isVisible())
                    {
                        if (!item.isIndeterminate() && !item.hasRange())
                        {
                            progressBar.setVisible(false);
                            JStatusBar.this.validate();
                        }
                        
                    } else
                    {
                        if (item.isIndeterminate() || item.hasRange())
                        {
                            progressBar.setVisible(true);
                            JStatusBar.this.validate();
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Set the min, max on the GUI thread.
     * @param min min value
     * @param max max value
     */
    public synchronized void setProgressRange(final String name, final int min, final int max)
    {
        setProgressRange(name, min, max, min);
    }
    
    /**
     * Set the min, max on the GUI thread.
     * @param min min value
     * @param max max value
     * @param value initial value
     */
    public synchronized void setProgressRange(final String name, final int min, final int max, final int value)
    {
        if (progressBar != null)
        {
            ProgressItem item = getProgressItem(name);
            item.setProgressRange(min, max, value);
            updateProgress(item);
        }
    }

    /**
     * Set the min, max on the GUI thread.
     * @param min min value
     * @param max max value
     * @param value initial value
     */
    public synchronized void setValue(final String name, final int value)
    {
        if (progressBar != null)
        {
            ProgressItem item = getProgressItem(name);
            item.setValue(value);
            updateProgress(item);
        }
    }

    /**
     * Returns the ProgressBar.
     * @return the ProgressBar
     */
    public JProgressBar getProgressBar()
    {
        return progressBar;
    }

    
    //-------------------------------------------------------
    //
    //-------------------------------------------------------
    public class ProgressItem
    {
        private String  name;
        private int     min;
        private int     max;
        private int     value;
        private boolean isIndeterminate  = false;
        private boolean usePlatformUI    = false;
        private boolean indStatusChanged = false;
        
        /**
         * 
         */
        public ProgressItem(final String name)
        {
            this.name = name;
        }

        /**
         * @param indStatusChanged the indStatusChanged to set
         */
        public void setIndStatusChanged(boolean indStatusChanged)
        {
            this.indStatusChanged = indStatusChanged;
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name)
        {
            this.name = name;
        }

        /**
         * 
         */
        public void clear()
        {
            min = max = value = 0;
            indStatusChanged = false;
            isIndeterminate  = false;
        }
        
        public boolean hasRange()
        {
            return min != 0 || max != 0;
        }
        
        /**
         * @return the isIndeterminate
         */
        public boolean isIndeterminate()
        {
            return isIndeterminate;
        }

        /**
         * @param isIndeterminate the isIndeterminate to set
         */
        public void setIndeterminate(boolean isIndeterminate)
        {
            indStatusChanged = this.isIndeterminate != isIndeterminate;
            this.isIndeterminate = isIndeterminate;
        }
        
        /**
         * @return the indStatusChanged
         */
        public boolean isIndStatusChanged()
        {
            return indStatusChanged;
        }

        /**
         * @return the usePlatformUI
         */
        public boolean isUsePlatformUI()
        {
            return usePlatformUI;
        }

        /**
         * @param usePlatformUI the usePlatformUI to set
         */
        public void setUsePlatformUI(boolean usePlatformUI)
        {
            this.usePlatformUI = usePlatformUI;
        }

        /**
         * Set the min, max on the GUI thread.
         * @param min min value
         * @param max max value
         * @param value initial value
         */
        public void setProgressRange(final int min, final int max)
        {
            setProgressRange(min, max, min);
        }
        
        /**
         * Set the min, max on the GUI thread.
         * @param minArg min value
         * @param maxArg max value
         * @param valueArg initial value
         */
        public void setProgressRange(final int minArg, final int maxArg, final int valueArg)
        {
            this.min   = minArg;
            this.max   = maxArg;
            this.value = valueArg;
            
            setIndeterminate(false);
        }
        
        /**
         * @return the value
         */
        public int getValue()
        {
            return value;
        }

        /**
         * @param value the value to set
         */
        public void setValue(int value)
        {
            this.value = value;
        }

        /**
         * @return the min
         */
        public int getMin()
        {
            return min;
        }

        /**
         * @return the max
         */
        public int getMax()
        {
            return max;
        }

        /**
         * @param max the max to set
         */
        public void setMax(int max)
        {
            this.max = max;
        }
    }
    
    //-------------------------------------------------------
    //
    //-------------------------------------------------------
    private class MyBevelBorder extends BevelBorder
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
