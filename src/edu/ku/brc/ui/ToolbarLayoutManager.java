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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.util.List;
import java.util.Vector;

/**
 * The layout manager for laying out the toolbar in a horizontal fashion ONLY. 
 * This is a prototype layout manager, more work needs to be done.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class ToolbarLayoutManager implements LayoutManager, LayoutManager2
{
    
    private Vector<Component> comps         = new Vector<Component>();
    private Dimension         preferredSize = new Dimension();
    private int               borderPadding = 2;
    private int               separation    = 5;
    private int               maxCompHeight = 0;

    /**
     * Contructs a layout manager for layting out NavBoxes. It lays out all the NavBoxes vertically 
     * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
     * aroound all the boxes
     * @param borderPadding the margin around the boxes
     * @param separation the vertical separation inbetween the boxes.
     */
    public ToolbarLayoutManager(int borderPadding, int separation)
    {
        this.borderPadding = borderPadding;
        this.separation   = separation;
    }
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
     */
    public void addLayoutComponent(String arg0, Component arg1)
    {
        if (arg1 == null)
        {
            throw new NullPointerException("Null component in addLayoutComponent");
        }
        comps.addElement(arg1);

    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
     */
    public void removeLayoutComponent(Component arg0)
    {
        if (arg0 == null)
        {
            throw new NullPointerException("Null component in removeLayoutComponent");
        }
        comps.removeElement(arg0);

    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
     */
    public Dimension preferredLayoutSize(Container arg0)
    {
        return new Dimension(preferredSize);
    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
     */
    public Dimension minimumLayoutSize(Container arg0)
    {
         return new Dimension(preferredSize);
    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
     */
    public void layoutContainer(Container arg0)
    {        
        calcPreferredSize();

        int x = borderPadding;
        int y = borderPadding;
        
        Component lastComp = comps.size() > 0 ? comps.lastElement() : null;
        for (Component comp : comps)
        {
            Dimension size = comp.getPreferredSize();
            if (comp == lastComp)
            {
                x = arg0.getSize().width - (borderPadding + size.width);
            }
            
            int yc = y;
            if (size.height < preferredSize.height)
            {
                yc = (preferredSize.height - size.height) / 2;
            }
            comp.setBounds(x, yc, size.width, size.height);
            x += size.width + separation;
        }

    }
    
    /**
     * Calculates the preferred size of the contain. It lays out all the NavBoxes vertically 
     * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
     * aroound all the boxes
     *
     */
    protected void calcPreferredSize()
    {
        if (maxCompHeight == 0)
        {
            preferredSize.setSize(borderPadding*2, borderPadding);
            
            // Assumes Horizontal layout at the moment
            for (Component comp : comps)
            {
                Dimension size = comp.getPreferredSize();
                maxCompHeight        = Math.max(maxCompHeight, size.height);
                preferredSize.height = Math.max(preferredSize.height, size.height + (2 * borderPadding));
                preferredSize.width += size.width + separation;
            }
            preferredSize.width -= separation;
        }
    }
    
    /**
     * Return the list of all the components that have been added to the alyout manager
     * @return the list of all the components that have been added to the alyout manager
     */
    public List<Component> getComponentList()
    {
        return comps;
    }
    
    /*
     * Remove all the componets that have been added to the layout manager
     */
    public void removeAll()
    {
        comps.clear();
    }
    
    // LayoutManager2
    public void  addLayoutComponent(Component comp, Object constraints)
    {
        if (comp == null)
        {
            throw new NullPointerException("Null component in addLayoutComponent");
        }
        comps.addElement(comp);
    }
    public float   getLayoutAlignmentX(Container target)
    {
        return (float)0.0;
    }
    public float   getLayoutAlignmentY(Container target)
    {
        return (float)0.0;
    }
    public void invalidateLayout(Container target)
    {
        maxCompHeight = 0;
        preferredSize.setSize(0, 0);
        calcPreferredSize();
    }
    public Dimension maximumLayoutSize(Container target) 
    {
        calcPreferredSize();        
        return new Dimension(preferredSize); 
    }

}
