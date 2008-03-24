/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
/**
 * 
 */
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.UIManager;

/**
 * @author rod
 * @author balajihe
 * 
 * @code_status Alpha
 * 
 * Dec 14, 2007
 * 
 */
public class ScrollablePopupMenu extends JPopupMenu implements ActionListener
{
    public static final Icon EMPTY_IMAGE_ICON = new ImageIcon("menu_spacer.gif");

    private JPanel           panelMenus       = new JPanel();
    private JScrollPane      scroll           = null;

    public ScrollablePopupMenu()
    {
        super();
        //this.frame = frame;
        this.setLayout(new BorderLayout());
        panelMenus.setLayout(new GridLayout(0, 1));
        panelMenus.setBackground(UIManager.getColor("MenuItem.background"));
        // panelMenus.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        //init();

    }

    private static Frame getFrame(Component c) {
        Component w = c;

        while(!(w instanceof Frame) && (w!=null)) {
            w = w.getParent();
        }
        return (Frame)w;
    } 
    
    private void init(Component invoker)
    {
        super.removeAll();
        
        Frame frame = getFrame(invoker);
        
        scroll = new JScrollPane();
        scroll.setViewportView(panelMenus);
        scroll.setBorder(null);
        scroll.setMinimumSize(new Dimension(240, 40));

        Dimension maxSize = new Dimension(scroll.getMaximumSize().width,
                this.getToolkit().getScreenSize().height
                - this.getToolkit().getScreenInsets(frame.getGraphicsConfiguration()).top
                - this.getToolkit().getScreenInsets(frame.getGraphicsConfiguration()).bottom - 4);
        //System.err.println(maxSize);
        scroll.setMaximumSize(maxSize);
        scroll.setFocusable(false);
        panelMenus.setFocusable(false);
        
        super.add(scroll, BorderLayout.CENTER);
    }
    
    

    /* (non-Javadoc)
     * @see javax.swing.JPopupMenu#menuSelectionChanged(boolean)
     */
    @Override
    public void menuSelectionChanged(boolean isIncluded)
    {
        // TODO Auto-generated method stub
        super.menuSelectionChanged(isIncluded);
    }

    /* (non-Javadoc)
     * @see javax.swing.JPopupMenu#setVisible(boolean)
     */
    public void setVisible(final boolean vis)
    {
        super.setVisible(vis);
        if (!vis)
        {
            int x = 0;
            x++;
        }
    }
    
    public void show(Component invoker, int x, int y)
    {
        init(invoker);
        
        panelMenus.validate();
        int maxsize = scroll.getMaximumSize().height;
        int realsize = panelMenus.getPreferredSize().height;

        int sizescroll = 0;

        if (maxsize < realsize)
        {
            sizescroll = scroll.getVerticalScrollBar().getPreferredSize().width;
        }
        scroll.setPreferredSize(new Dimension(scroll.getPreferredSize().width + sizescroll + 20,
                                              scroll.getPreferredSize().height));
        
        //System.err.println(panelMenus.getPreferredSize());
        this.pack();
        this.setInvoker(invoker);
        if (sizescroll != 0)
        {
            // Set popup size only if scrollbar is visible
            this.setPopupSize(new Dimension(scroll.getPreferredSize().width + 20, scroll.getMaximumSize().height - 20));
        }
        
        // this.setMaximumSize(scroll.getMaximumSize());
        Point invokerOrigin = invoker.getLocationOnScreen();
        this.setLocation((int) invokerOrigin.getX() + x, (int) invokerOrigin.getY() + y);
        //this.setVisible(true);
        panelMenus.getComponent(0).requestFocus();
        
        super.show(invoker, x, y);
    }

    public void hidemenu()
    {
        if (this.isVisible())
        {
            this.setVisible(false);
        }
    }

    @Override
    public JMenuItem add(final JMenuItem menuItem)
    {
        // menuItem.setMargin(new Insets(0, 20, 0 , 0));
        if (menuItem != null) 
        { 
            panelMenus.add(menuItem);
            //menuItem.removeActionListener(this);
            //menuItem.addActionListener(this);
        }
        return menuItem;
    }
    
    public void addSeparator()
    {
        panelMenus.add(new JSeparator());
    }

    public void actionPerformed(ActionEvent e)
    {
        this.hidemenu();
    }

    public Component[] getComponents()
    {
        return panelMenus.getComponents();
    }
}
