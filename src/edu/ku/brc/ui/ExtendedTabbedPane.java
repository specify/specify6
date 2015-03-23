/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.event.MouseInputAdapter;

import org.apache.log4j.Logger;

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
    private static final Logger log = Logger.getLogger(ExtendedTabbedPane.class);
    
    protected static final int CLOSER_SIZE = 5;
    
    protected Rectangle closerRect = new Rectangle();
    protected boolean   isOver     = false;
    
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
        
        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
            @Override
            public void mouseExited(MouseEvent e) 
            {
                if (!closerRect.contains(e.getPoint()))
                {
                    isOver = false;
                    repaint();
                    //UIRegistry.displayStatusBarText("");
                }
            }
            @Override
            public void mouseMoved(MouseEvent e)
            {
                if (closerRect.contains(e.getPoint()))
                {
                    isOver = true;
                    repaint();
                    //UIRegistry.displayStatusBarText(itself.getToolTipText());
                    
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
     * @param title the title of the tab
     * @param icon the icon for the tab (can be null)
     * @param comp the component tab
     * @param index the index of the tab to be fixed
     */
    protected ExtendedTabPanel adjustTab(final String    title, 
                                         final Icon      icon, 
                                         final Component comp, 
                                         final int       index)
    {
        ExtendedTabPanel tabUI = new ExtendedTabPanel(comp, title, icon);
        setTabComponentAt(index, tabUI);
        return tabUI;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JTabbedPane#insertTab(java.lang.String, javax.swing.Icon, java.awt.Component, java.lang.String, int)
     */
    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index)
    {
        super.insertTab(null, null, component, tip, index);
        
        adjustTab(title, icon, component, index);
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
        try
        {
            super.paintComponent(g);
            
        } catch (java.lang.ArrayIndexOutOfBoundsException ex)
        {
            log.error(ex);
        }
        
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
