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
package edu.ku.brc.af.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;

/**
 * This organized NavBoxItemIFace object in a vertical layout (via a layout manager)<br>
 * (Note: Overrides paint on prupose)
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class NavBox extends JPanel implements NavBoxIFace
{
    private static final Logger      log      = Logger.getLogger(NavBox.class);
    
    protected String             name;
    protected NavBoxIFace.Scope  scope;
    protected NavBoxMgr          mgr;
    protected Vector<NavBoxItemIFace> items = new Vector<NavBoxItemIFace>();
    
    protected JPanel itemsPanel;
    
    protected boolean            collapsed             = false;
    protected ImageIcon          icon                  = null;
    protected ImageIcon          collapsableIconOpen   = null;
    protected ImageIcon          collapsableIconClosed = null;
    protected Rectangle          iconRect              = null;
    protected int                minHeight             = -1;
     
    
    /**
     * Constructor (with name).
     * @param name the name of the NavBox.
     */
    public NavBox(final String name)
    {
        this(name, false);
    }
    
    /**
     * Constructor (with name).
     * @param name the name of the NavBox.
     * @param collapsable indicates whether the NavBox can be collapsable
     */
    public NavBox(final String name, final boolean collapsable)
    {
        super();
        this.name = name;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.PAGE_AXIS));
        itemsPanel.setBorder(null);
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setOpaque(true);
        
        setBorder(BorderFactory.createEmptyBorder(22, 4, 4, 4));
        //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
        setBackground(Color.WHITE);
        setOpaque(true);
        
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(null);
        add(scrollPane);
        
        if (collapsable)
        {/*
            collapsableIconOpen   = IconManager.getIcon("Minimize");
            collapsableIconClosed = IconManager.getIcon("Maximize");
            
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e)
                {
                    if (iconRect != null && iconRect.contains(e.getPoint()))
                    {
                        collapsed = !collapsed;
                        if (collapsed)
                        {
                            icon = collapsableIconClosed;
                            setSize(getSize().width, minHeight);
                        } else
                        {
                            icon = collapsableIconOpen;
                            setSize(getPreferredSize());
                        }
                        validate();
                        invalidate();
                        doLayout();
                    }
                }
            });
        */ }       
    }
    
    /**
     * Returns the scope of the tab.
     * @return returns the scope of the tab
     */
    public Scope getScope()
    {
        return scope;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.NavBoxIFace#setScope(edu.ku.brc.af.core.NavBoxIFace.Scope)
     */
    public void setScope(final NavBoxIFace.Scope scope)
    {
        this.scope = scope;
    }


    /* (non-Javadoc)
     * @see java.awt.Component#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setName(java.lang.String)
     */
    @Override
    public void setName(final String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.NavBoxIFace#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.NavBoxIFace#getItems()
     */
    public List<NavBoxItemIFace> getItems()
    {
        return items;
    }

    /**
     * Adds a NavBoxItemIFace item to the box insorted order.
     * @param item the NavBoxItemIFace item to be added
     * @param notify whether to have it relayout or not (true -> does layout)
     * @return the UI component for this item
     */
    public Component insertSorted(final NavBoxItemIFace item)
    {
        int insertionInx = Math.abs(Collections.binarySearch(items, item)) - 1;
        return insert(item, true, insertionInx);
    }

    /**
     * Adds a NavBoxItemIFace item to the box and returns the UI component for that item.
     * @param item the NavBoxItemIFace item to be added
     * @param notify whether to have it relayout or not (true -> does layout)
     * @param position the position in the list
     * @return the UI component for this item
     */
    public Component insert(final NavBoxItemIFace item, final boolean notify, final int position)
    {
        if (position == -1 || position == items.size())
        {
            itemsPanel.add(item.getUIComponent());
            items.addElement(item);
            
        } else
        {
            items.insertElementAt(item, position);
            removeAll();
            for (NavBoxItemIFace nb : items)
            {
                itemsPanel.add(nb.getUIComponent());
            }
        }
       
        if (notify && mgr != null)
        {
            refresh(this);
            //mgr.invalidate();
            //mgr.doLayout();
        }
        item.getUIComponent().setBackground(getBackground());
        item.getUIComponent().setOpaque(true);
        return item.getUIComponent();
    }
       
    /**
     * Adds a NavBoxItemIFace item to the box and returns the UI component for that item.
     * @param item the NavBoxItemIFace item to be added
     * @param notify whether to have it relayout or not (true -> does layout)
     * @return the UI component for this item
     */
    public Component add(final NavBoxItemIFace item, boolean notify)
    {
        return insert(item, notify, items.size());
    }
       
    /**
     * Adds a NavBoxItemIFace item to the box and returns the UI component for that item and does not perform a doLayout of the box.
     * @param item NavBoxItemIFace to be added
     * @return the ui component for the item
     */
    public Component add(final NavBoxItemIFace item)
    {
        return add(item, false);
    }
    
    /**
     * Removes an item from the navbox.
     * @param item the item to be removed
     */
    public void remove(final NavBoxItemIFace item)
    {
        remove(item.getUIComponent());
        items.remove(item);
        
        refresh(this);
    }

    /**
     * Removes all the items from the navbox.
     */
    public void clear()
    {
        removeAll();
        items.clear();
        refresh(this);
    }
    
    /**
     * Returns the number of NavBoxes.
     * @return the number of NavBoxes.
     */
    public int getCount()
    {
        return items.size();
    }

    
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        Dimension   size   = super.getPreferredSize();
        FontMetrics fm     = this.getFontMetrics(getFont());
        int         width  = fm.stringWidth(name);
        Insets      insets = getBorder().getBorderInsets(this);
        width += insets.left + insets.right;
        size.width = Math.max(size.width, width);
        
        if (collapsed)
        {
            size.height = minHeight;
        }
        
        log.debug("NavBox preferred size: " + size);
        
        return size;
    }
        

    /* (non-Javadoc)
     * @see java.awt.Component#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        Dimension dim = getSize();
        
        FontMetrics fm   = g.getFontMetrics();
        int         strW = fm.stringWidth(name);
        
        int x = (dim.width - strW) / 2;
        Insets ins = getBorder().getBorderInsets(this);
        int y = 2 + fm.getAscent();
        
        int lineW = dim.width - ins.left - ins.right;
        x = ins.left;
        int txtY = y;
        y += 3;

        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(x, y,   x+lineW, y);
        y++;
        minHeight = y;
        
        x++;
        g.setColor(Color.LIGHT_GRAY.darker());
        g.drawLine(x, y,   x+lineW, y);
        
        g.setColor(Color.BLUE.darker());
        g.drawString(name, x, txtY);
        
        if (collapsableIconOpen != null)
        {
            if (iconRect == null)
            {
                iconRect   = getBounds();
                iconRect.x = iconRect.width - collapsableIconOpen.getIconWidth();
                iconRect.y = 0;
            }
            g.drawImage(icon.getImage(), iconRect.x, iconRect.y, null);
        }
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.NavBoxIFace#setMgr(edu.ku.brc.af.core.NavBoxMgr)
     */
    public void setMgr(NavBoxMgr mgr)
    {
        this.mgr = mgr;
    }

    /**
     * Returns a NavBoxItemIFace item built from a NavBoxButton.
     * @param label the text label
     * @param iconName the icon name, not the image filename, but the IconManager name for the icon
     * @param iconSize the size  to use
     * @return a NavBoxItemIFace item built from a NavBoxButton
     */
    public static NavBoxItemIFace createBtn(final String label, 
                                            final String iconName, 
                                            final IconManager.IconSize iconSize, 
                                            final ActionListener al)
    {
        ImageIcon icon = null;
        
        if (iconName != null)
        {
            icon = IconManager.getImage(iconName, iconSize);
        }
        
        NavBoxButton btn = new NavBoxButton(label, icon);
        if (al != null)
        {
            btn.addActionListener(al);
        }
        
        return btn; 
    }
    
    /**
     * Returns a NavBoxItemIFace item built from a NavBoxButton.
     * @param label the text label
     * @param iconName the icon name, not the image filename, but the IconManager name for the icon
     * @param iconSize the size  to use
     * @return a NavBoxItemIFace item built from a NavBoxButton
     */
    public static NavBoxItemIFace createBtn(final String label, 
                                            final String iconName, 
                                            final IconManager.IconSize iconSize)
    {
        return createBtn(label, iconName, iconSize, null);
    }
    
    /**
     * Returns a NavBoxItemIFace item built from a NavBoxButton.
     * @param label the text label
     * @param iconName the icon name, not the image filename, but the IconManager name for the icon
     * @param iconSize the size  to use
     * @param iconSize the size  to use
     * @param al the action listener that will be added the item 
     * @return a NavBoxItemIFace item built from a NavBoxButton
     */
    public static NavBoxItemIFace createBtnWithTT(final String label,
                                                  final String iconName, 
                                                  final String toolTip,
                                                  final IconManager.IconSize iconSize,
                                                  final ActionListener al)
    {
        NavBoxItemIFace nbi = createBtn(label, iconName, iconSize, al);
        if (toolTip != null)
        {
            nbi.setToolTip(toolTip);
        }
        return  nbi;
    }
    
    public static void refresh(final NavBoxIFace nb)
    {
        NavBox box = (NavBox)nb;
        //log.debug("0box "+box.getPreferredSize()+" "+box.getSize());
        
        box.invalidate();
        box.validate();
        box.doLayout();
        //box.setSize(box.getPreferredSize());
        box.repaint();
        
        NavBoxMgr.getInstance().invalidate();
        NavBoxMgr.getInstance().doLayout();
        NavBoxMgr.getInstance().repaint();
        
        /*log.debug("1box "+box.getPreferredSize()+" "+box.getSize());
        for (NavBoxItemIFace nbi : box.items)
        {
            Component c = (Component)nbi;
            log.debug("nbi "+c.getPreferredSize()+" "+c.getSize());
        }*/
        UICacheManager.forceTopFrameRepaint();
    }
    
    public static void refresh(final NavBoxItemIFace nbi)
    {
        if (nbi != null)
        {
            Component comp = nbi.getUIComponent();
            comp.invalidate();
            comp.doLayout();
            comp.setSize(comp.getPreferredSize());
            comp.repaint();
            log.debug("comp "+comp.getPreferredSize()+" "+comp.getSize());
            refresh((NavBox)nbi.getUIComponent().getParent());
        }
    }

}
