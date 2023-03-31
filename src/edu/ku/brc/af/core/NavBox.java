/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.skin.SkinItem;
import edu.ku.brc.ui.skin.SkinsMgr;

/**
 * This organized NavBoxItemIFace object in a vertical layout (via a layout manager)<br>
 * (Note: Overrides paint on prupose)
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class NavBox extends JPanel implements NavBoxIFace
{
    private static final Logger      log      = Logger.getLogger(NavBox.class);
    
    private static final int MAX_HEIGHT = 180;
    
    protected String             name;
    protected NavBoxIFace.Scope  scope;
    protected NavBoxMgr          mgr;
    protected Vector<NavBoxItemIFace> items = new Vector<NavBoxItemIFace>();
    
    protected boolean            scrollable;
    protected JPanel             itemsPanel;
    
    protected boolean            collapsed             = false;
    protected ImageIcon          icon                  = null;
    protected ImageIcon          collapsableIconOpen   = null;
    protected ImageIcon          collapsableIconClosed = null;
    protected Rectangle          iconRect              = null;
    protected int                minHeight             = -1;
    protected boolean            isManaged             = false;
     
    /**
     * Constructor (with name).
     * @param name the name of the NavBox.
     */
    public NavBox(final String name)
    {
        this(name, false, false);
    }
    
    /**
     * Constructor (with name).
     * @param name the name of the NavBox.
     * @param collapsable indicates whether the NavBox can be collapsable
     */
    public NavBox(final String name, final boolean collapsable, final boolean scrollable)
    {
        super();
        this.name = name;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        this.scrollable = scrollable;
        
        SkinItem skinItem = SkinsMgr.getSkinItem("NavBox");
        
        if (scrollable)
        {
            itemsPanel = new JPanel();//new BoxLayout(this, BoxLayout.Y_AXIS));

            itemsPanel.setBorder(null);
            
            if (skinItem != null)
            {
                itemsPanel.setOpaque(skinItem.isOpaque());
                skinItem.setupPanel(itemsPanel);
                
            } else
            {
                itemsPanel.setBackground(NavBoxMgr.getBGColor());
            }
        }
        
        setBorder(BorderFactory.createEmptyBorder(22, 4, 4, 4));
        setBackground(NavBoxMgr.getBGColor());
        setOpaque(!SkinsMgr.hasSkins());
        
        if (scrollable)
        {
            JScrollPane scrollPane = new JScrollPane(itemsPanel);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);
            add(scrollPane);
        }
        
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
        return insert(item, true, true, insertionInx);
    }

    /**
     * 
     */
    private void reAddItems()
    {
        JPanel panelToLayout = scrollable ? itemsPanel : this;
        panelToLayout.removeAll();
        
        String rowDef = "";
        if (items.size() > 0)
        {
            rowDef = UIHelper.createDuplicateJGoodiesDef("p", "1px", items.size()) + ",";
        }
        rowDef += "f:p:g";
        
        int row = 1;
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g",  rowDef), panelToLayout);
        for (int i=0;i<items.size();i++)
        {
            pb.add(items.get(i).getUIComponent(), cc.xy(1, row));
            row += 2;
        }
    }
    
    /**
     * Adds a NavBoxItemIFace item to the box and returns the UI component for that item.
     * @param item the NavBoxItemIFace item to be added
     * @param doLayout whether to have it relayout or not (true -> does layout)
     * @param position the position in the list
     * @param position the position in the list
     * @return the UI component for this item
     */
    public Component insert(final NavBoxItemIFace item, 
                            final boolean doLayout, 
                            final boolean doSort,
                            final int position)
    {
        boolean newWay = true;
        if (newWay)
        {
            if (position == -1 || position == items.size())
            {
                items.addElement(item);
            } else
            {
                items.insertElementAt(item, position);    
            }
            
            if (doSort)
            {
                Collections.sort(items);
            }
            
            reAddItems();
        } else if (position == -1 || position == items.size())
        {
            if (scrollable)
            {
                itemsPanel.add(item.getUIComponent());
            }
            else
            {
                super.add(item.getUIComponent());
            }
            items.addElement(item);
            
        } else
        {
            items.insertElementAt(item, position);
            if (doSort)
            {
                Collections.sort(items);
            }
            
            if (scrollable)
            {
                itemsPanel.removeAll();
                for (NavBoxItemIFace nb : items)
                {
                    itemsPanel.add(nb.getUIComponent());
                }
            }
            else
            {
                removeAll();
                for (NavBoxItemIFace nb : items)
                {
                    super.add(nb.getUIComponent());
                }
            }
        } 
        
        if (isManaged && item instanceof GhostActionable)
        {
            ((GhostGlassPane)UIRegistry.get(UIRegistry.GLASSPANE)).add((GhostActionable)item);
        }
       
        item.getUIComponent().setBackground(getBackground());
        item.getUIComponent().setOpaque(!SkinsMgr.hasSkins());
        
        if (doLayout)
        {
            refresh(this);
        }
        return item.getUIComponent();
    }
    
    /**
     * Sorts all the items and reads them sorted.
     */
    public void sort()
    {
        Collections.sort(items);
        itemsPanel.removeAll();
        for (NavBoxItemIFace nb : items)
        {
            itemsPanel.add(nb.getUIComponent());
        }
        refresh(this);
    }
       
    /**
     * Adds a NavBoxItemIFace item to the box and returns the UI component for that item.
     * @param item the NavBoxItemIFace item to be added
     * @param notify whether to have it relayout or not (true -> does layout)
     * @return the UI component for this item
     */
    public Component add(final NavBoxItemIFace item, boolean notify)
    {
        return insert(item, notify, false, items.size());
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.NavBoxIFace#remove(edu.ku.brc.af.core.NavBoxItemIFace)
     */
    public void remove(final NavBoxItemIFace item)
    {
        if (scrollable)
        {
            itemsPanel.remove(item.getUIComponent());
        }
        else
        {
            remove(item.getUIComponent());
        }
        items.remove(item);
        
        if (isManaged && item instanceof GhostActionable)
        {
            ((GhostGlassPane)UIRegistry.get(UIRegistry.GLASSPANE)).remove((GhostActionable)item);
        }
        
        refresh(this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.NavBoxIFace#setIsManaged(boolean)
     */
    public void setIsManaged(final boolean isManaged)
    {
        this.isManaged = isManaged;
    }

    /**
     * Removes all the items from the navbox.
     */
    public void clear()
    {
        if (scrollable)
        {
            itemsPanel.removeAll();
        }
        else
        {
            removeAll();
        }
        
        if (isManaged)
        {
            for (NavBoxItemIFace item : items)
            {
                if (item instanceof GhostActionable)
                {
                    ((GhostGlassPane)UIRegistry.get(UIRegistry.GLASSPANE)).add((GhostActionable)item);
                }
            }
        }
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

        // if we're putting the items in a scrollpane, return 180 as the maximum preferred height
        if (scrollable)
        {
            if (size.height > MAX_HEIGHT)
            {
                size.height = MAX_HEIGHT;
            }
        }
        
        if (collapsed)
        {
            size.height = minHeight;
        }
        
        //log.debug("NavBox preferred size: " + size);
        
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
        
        ((Graphics2D)g).setRenderingHints(UIHelper.createTextRenderingHints());
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
        if (StringUtils.isNotEmpty(toolTip))
        {
            nbi.setToolTip(toolTip);
        }
        return  nbi;
    }
    
    /**
     * Refreshes - meaning it makes sure it is resized (layed out) and drawn.
     * @param nbi the box to refresh
     */
    public static void refresh(final NavBoxIFace nb)
    {
        final NavBox box = (NavBox)nb;
        //log.debug("0box "+box.getPreferredSize()+" "+box.getSize());
        
        SwingUtilities.invokeLater(new Runnable() {

            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run()
            {
                for (NavBoxItemIFace nbii : box.getItems())
                {
                    if (nbii instanceof RolloverCommand)
                    {
                        RolloverCommand rc = (RolloverCommand)nbii;
                        //rc.invalidate();
                        //rc.validate();
                        //rc.doLayout();
                        //rc.paintImmediately(rc.getBounds());
                        rc.revalidate();
                    }
                }
                //box.invalidate();
                //box.validate();
                //box.doLayout();
                //box.paintImmediately(box.getBounds());
                //box.setSize(box.getPreferredSize());
                //box.repaint();
                box.revalidate();
                
                if (box.itemsPanel != null)
                {
                    box.itemsPanel.validate();
                    box.itemsPanel.invalidate();
                }
                
                NavBoxMgr.getInstance().invalidate();
                NavBoxMgr.getInstance().doLayout();
                NavBoxMgr.getInstance().repaint();
                
                /*log.debug("1box "+box.getPreferredSize()+" "+box.getSize());
                for (NavBoxItemIFace nbi : box.items)
                {
                    Component c = (Component)nbi;
                    log.debug("nbi "+c.getPreferredSize()+" "+c.getSize());
                }*/
                UIRegistry.forceTopFrameRepaint();
            }
        });
    }
    
    /**
     * Refreshes - meaning it makes sure it is resized (layed out) and drawn.
     * @param nbi the box to refresh
     */
    public static void refresh(final NavBoxItemIFace nbi)
    {
        if (nbi != null)
        {
            Component comp = nbi.getUIComponent();
            comp.invalidate();
            comp.doLayout();
            comp.setSize(comp.getPreferredSize());
            comp.repaint();
            log.debug("comp "+comp.getPreferredSize()+" "+comp.getSize()); //$NON-NLS-1$ //$NON-NLS-2$
            
            Container parentComp = nbi.getUIComponent().getParent();
            if (parentComp instanceof NavBox)
            {
                refresh( (NavBox)parentComp );
            }
            else if (parentComp instanceof JScrollPane)
            {
                // this must be a scrollable NavBox;
                // let's get the actual NavBox
                // container heirarchy is NavBox -> JScrollPane -> NavBoxItem
                parentComp = parentComp.getParent().getParent();
                refresh( (NavBox)parentComp );
            }
        }
    }

}
