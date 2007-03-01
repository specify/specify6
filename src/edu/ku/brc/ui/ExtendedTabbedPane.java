package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.MouseInputAdapter;

/**
 * Adds a close "X" in the bottom right of the TabbedPane for closing tabs and adds a Close btn to each tab.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ExtendedTabbedPane extends JTabbedPane
{
    protected static final int CLOSER_SIZE = 5;
    
    protected Rectangle closerRect = new Rectangle();
    protected boolean   isOver     = false;
    protected ExtendedTabbedPane itself;
    
    /**
     * Constructor.
     */
    public ExtendedTabbedPane()
    {
        super();
        init();
    }

    /**
     * Constructor.
     * @param tabPlacement tabLayoutPolicy
     */
    public ExtendedTabbedPane(int tabPlacement)
    {
        super(tabPlacement);
        init();
    }

    /**
     * Constructor.
     * @param tabPlacement tabPlacement
     * @param tabLayoutPolicy tabLayoutPolicy
     */
    public ExtendedTabbedPane(int tabPlacement, int tabLayoutPolicy)
    {
        super(tabPlacement, tabLayoutPolicy);
        init();
    }
    
    /**
     * Hooks up listeners for painting the hover state of the close "X".
     */
    protected void init()
    {
        itself = this;
        
        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
            @Override
            public void mouseExited(MouseEvent e) 
            {
                if (!closerRect.contains(e.getPoint()))
                {
                    isOver = false;
                    repaint();
                    //UICacheManager.displayStatusBarText("");
                }
            }
            @Override
            public void mouseMoved(MouseEvent e)
            {
                if (closerRect.contains(e.getPoint()))
                {
                    isOver = true;
                    repaint();
                    //UICacheManager.displayStatusBarText(itself.getToolTipText());
                    
                } else if (isOver)
                {
                    isOver = false;
                    repaint();
                }
                
            }
            @Override
            public void mouseClicked(MouseEvent e) 
            {
                if (closerRect.contains(e.getPoint()))
                {
                    closeCurrent();
                }
                
            }
          };
        addMouseListener(mouseInputAdapter);
        addMouseMotionListener(mouseInputAdapter);
        
    }
    
    /**
     * Adds a Close Btn to the Tab.
     * @param index the index of the tab
     * @param title the title of the tab
     * @param icon the icon for the tab (can be null)
     */
    protected void adjustTab(final String title, final Icon icon, final Component comp)
    {
        final JLabel closeBtn = new JLabel(IconManager.getIcon("Close"));
        closeBtn.setBorder(null);

        closeBtn.setOpaque(false);
        closeBtn.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                remove(comp);
            }
            @Override
            public void mouseEntered(MouseEvent e)
            {
                closeBtn.setIcon(IconManager.getIcon("CloseHover"));
                closeBtn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                closeBtn.setIcon(IconManager.getIcon("Close"));
                closeBtn.repaint();
            }
        });

        // XXX Java 6.0
        /*
        JPanel tabPanel = new JPanel(new BorderLayout());
        if (icon != null)
        {
            tabPanel.add(new JLabel(title, icon, SwingConstants.RIGHT), BorderLayout.WEST);
            tabPanel.add(new JLabel(" "), BorderLayout.CENTER);
        }
        tabPanel.add(closeBtn, BorderLayout.EAST);
        
        setTabComponentAt(getTabCount()-1, tabPanel);
        */
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JTabbedPane#addTab(java.lang.String, java.awt.Component)
     */
    @Override
    public void addTab(String title, Component component)
    {
        super.addTab(title, component);
        
        adjustTab(title, null, component);
    }

    /* (non-Javadoc)
     * @see javax.swing.JTabbedPane#addTab(java.lang.String, javax.swing.Icon, java.awt.Component, java.lang.String)
     */
    @Override
    public void addTab(String title, Icon icon, Component component, String tip)
    {
        super.addTab(title, icon, component, tip);
        
        adjustTab(title, icon, component);
    }

    /* (non-Javadoc)
     * @see javax.swing.JTabbedPane#addTab(java.lang.String, javax.swing.Icon, java.awt.Component)
     */
    @Override
    public void addTab(String title, Icon icon, Component component)
    {
        super.addTab(title, icon, component);
        
        adjustTab(title, icon, component);
    }

    /* (non-Javadoc)
     * @see javax.swing.JTabbedPane#insertTab(java.lang.String, javax.swing.Icon, java.awt.Component, java.lang.String, int)
     */
    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index)
    {
        super.insertTab(title, icon, component, tip, index);
        
        adjustTab(title, icon, component);
    }

    /**
     * Clsoes the current tab.
     */
    protected void closeCurrent()
    {
        this.remove(this.getSelectedComponent());
    }
    
    /**
     *  Draws the close "X" 
     * @param g f
     * @param x x
     * @param y y
     * @param w w
     * @param h h
     */
    protected void drawCloser(final Graphics g, final int x, final int y, final int w, final int h)
    {
        closerRect.setBounds(x, y, w, h);
        
        g.drawLine(x, y, x+w, y+h);
        g.drawLine(x+w, y, x, y+h);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        if (this.getTabCount() > 0)
        {
            Dimension s = getSize();
            s.width  -= CLOSER_SIZE + 1;
            s.height -= CLOSER_SIZE + 1;
            
            Color color = getBackground();
            
            int x =  s.width -5;
            int y =  s.height-5;
            if (isOver)
            {
                g.setColor(Color.RED);
                drawCloser(g, x, y, CLOSER_SIZE, CLOSER_SIZE);
                
            } else
            {
                g.setColor(color.darker());
                drawCloser(g, x, y, CLOSER_SIZE, CLOSER_SIZE);                
            }
        } else
        {
            closerRect.setBounds(0,0,0,0);
        }
    }
}
