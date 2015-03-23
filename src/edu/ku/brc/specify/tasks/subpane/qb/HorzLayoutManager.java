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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class HorzLayoutManager implements LayoutManager2
{

    private Vector<Component> comps         = new Vector<Component>();
    private Dimension         preferredSize = new Dimension();
    private Dimension         minimumSize   = new Dimension();
    private int               borderPadding = 2;
    private int               xSeparation   = 5;

    /**
     * Constructs a layout manager for laying out NavBoxes. It lays out all the NavBoxes vertically
     * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
     * around all the boxes.
     * @param borderPadding the margin around the boxes
     * @param ySeparation the vertical separation in between the boxes.
     */
    public HorzLayoutManager(final int borderPadding, final int xSeparation)
    {
        this.borderPadding = borderPadding;
        this.xSeparation   = xSeparation;
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
         return new Dimension(minimumSize);
    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
     */
    public void layoutContainer(Container target)
    {
        synchronized (target.getTreeLock()) 
        {
            Insets insets = target.getInsets();

            Dimension parentSize =  target.getSize();
            parentSize.width  -= (2 * borderPadding) + insets.left + insets.right;
            parentSize.height -= (2 * borderPadding) + insets.top + insets.bottom;
    
            int x = borderPadding;
            int y = borderPadding;
    
            for (Component comp: comps)
            {
                Dimension size = comp.getPreferredSize();
                if (comp instanceof JButton)
                {
                    comp.setBounds(x, y+parentSize.height-size.height, size.width, size.height);
                    
                } else
                {
                    comp.setBounds(x, y, size.width, parentSize.height);
                }
                x += size.width + xSeparation;
            }
        }
    }

    /**
     * Calculates the preferred size of the contain. It lays out all the NavBoxes vertically
     * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
     * around all the boxes.
     *
     */
    protected void calcPreferredSize(Container target)
    {
        Insets insets = target.getInsets();
        preferredSize.setSize(borderPadding+insets.left+insets.right, (borderPadding*2)+insets.top+insets.bottom);

        for (Component comp : comps)
        {
            Dimension size = comp.getPreferredSize();
            //System.out.println(size);
            preferredSize.height = Math.max(preferredSize.height, size.height + (2 * borderPadding));
            preferredSize.width += size.width + xSeparation;
            
            minimumSize.height = Math.max(minimumSize.height, comp.getMinimumSize().height + (2 * borderPadding));
        }
        preferredSize.width -= xSeparation;
    }

    /**
     * Return the list of all the components that have been added to the alyout manager.
     * @return the list of all the components that have been added to the alyout manager
     */
    public List<Component> getComponentList()
    {
        return comps;
    }

    /**
     * Remove all the componets that have been added to the layout manager.
     */
    public void removeAll()
    {
        comps.clear();
        preferredSize.setSize(0, 0);
    }

    //----------------------
    // LayoutManager2
    //----------------------
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
     */
    public void  addLayoutComponent(Component comp, Object constraints)
    {
        if (comp == null)
        {
            throw new NullPointerException("Null component in addLayoutComponent");
        }
        comps.addElement(comp);
    }
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
     */
    public float   getLayoutAlignmentX(Container target)
    {
        return (float)0.0;
    }
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
     */
    public float   getLayoutAlignmentY(Container target)
    {
        return (float)0.0;
    }
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
     */
    public void invalidateLayout(Container target)
    {
        preferredSize.setSize(0, 0);
        calcPreferredSize(target);
    }
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
     */
    public Dimension maximumLayoutSize(Container target)
    {
        calcPreferredSize(target);
        return new Dimension(minimumSize);
    }
}
