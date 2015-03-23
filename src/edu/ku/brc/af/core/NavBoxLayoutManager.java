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
package edu.ku.brc.af.core;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.Vector;

import edu.ku.brc.ui.dnd.Trash;

/**
 * The layout manager for laying out NavBoxes in a vertical fashion (only). 
 * THis REALLY needs to be reworked to take into account the insets just like BoxLayout.
 * In fact, maybe this should extend BoxLayout and then override the layout method.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class NavBoxLayoutManager implements LayoutManager2
{

    private Vector<Component> comps         = new Vector<Component>();
    private Dimension         preferredSize = new Dimension();
    private Dimension         minimumSize   = new Dimension();
    private int               borderPadding;
    private int               ySeparation;

    /**
     * Constructs a layout manager for laying out NavBoxes. It lays out all the NavBoxes vertically
     * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
     * around all the boxes.
     * @param borderPadding the margin around the boxes
     * @param ySeparation the vertical separation in between the boxes.
     */
    public NavBoxLayoutManager(final int borderPadding, final int ySeparation)
    {
        this.borderPadding = borderPadding;
        this.ySeparation   = ySeparation;
    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
     */
    public void addLayoutComponent(final String arg0, final Component arg1)
    {
        if (arg1 == null)
        {
            throw new NullPointerException("Null component in addLayoutComponent"); //$NON-NLS-1$
        }
        comps.addElement(arg1);

    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
     */
    public void removeLayoutComponent(final Component target)
    {
        synchronized (target.getTreeLock()) 
        {
            if (target == null)
            {
                throw new NullPointerException("Null component in removeLayoutComponent"); //$NON-NLS-1$
            }
            comps.removeElement(target);
        }

    }

    /**
     * @param arg0
     * @param arg1
     * 
     * Moves arg0 to arg1's position and shifts other components appropriately.
     * 
     * Need to ensure that layoutContainer gets executed in order for move to be visible.
     */
    public void moveLayoutComponent(final Component arg0, final Component arg1)
    {
        int fromIdx = comps.indexOf(arg0);
        int toIdx = comps.indexOf(arg1);
        if (fromIdx == toIdx)
        {
            return;
        }
        //else
        comps.remove(fromIdx);
        comps.insertElementAt(arg0, toIdx);
    }
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
     */
    public synchronized Dimension preferredLayoutSize(final Container target)
    {
        //synchronized (target.getTreeLock()) 
        {
            calcPreferredSize();
            return new Dimension(preferredSize);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
     */
    public Dimension minimumLayoutSize(final Container arg0)
    {
         return new Dimension(minimumSize);
    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
     */
    public void layoutContainer(final Container target)
    {
        synchronized (target.getTreeLock()) 
        {
            Dimension parentSize =  target.getSize();
            parentSize.width  -= 2 * borderPadding;
            parentSize.height -= 2 * borderPadding;
    
            int x = borderPadding;
            int y = borderPadding;
    
            Trash trash = null;
            
            for (Component comp: comps)
            {
                Dimension size = comp.getPreferredSize();
                if (comp instanceof Trash)
                {
                    trash = (Trash)comp;
                    
                } else
                {
                    comp.setBounds(x, y, parentSize.width, size.height);
                    y += size.height + ySeparation;
                }
            }
            
            if (trash != null)
            {
                Dimension size = trash.getPreferredSize();
                int trashY = parentSize.height - size.height-1;
                trash.setBounds((parentSize.width - size.width)/2, trashY > y ? trashY : y, size.width, size.height);
            }
        }
    }

    /**
     * Calculates the preferred size of the contain. It lays out all the NavBoxes vertically
     * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
     * around all the boxes.
     *
     */
    protected void calcPreferredSize()
    {
        preferredSize.setSize(borderPadding*2, borderPadding);

        for (Component comp : comps)
        {
            Dimension size = comp.getPreferredSize();
            preferredSize.width = Math.max(preferredSize.width, size.width + (2 * borderPadding));
            preferredSize.height += size.height + ySeparation;
            
            minimumSize.width = Math.max(minimumSize.width, comp.getMinimumSize().width + (2 * borderPadding));
        }
        preferredSize.height -= ySeparation;
    }

    /**
     * Remove all the componets that have been added to the layout manager.
     */
    public synchronized void removeAll()
    {
        comps.clear();
    }

    //----------------------
    // LayoutManager2
    //----------------------
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
     */
    public synchronized void addLayoutComponent(final Component comp, final Object constraints)
    {
        if (comp == null)
        {
            throw new NullPointerException("Null component in addLayoutComponent"); //$NON-NLS-1$
        }
        comps.addElement(comp);
    }
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
     */
    public float getLayoutAlignmentX(final Container target)
    {
        return (float)0.0;
    }
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
     */
    public float getLayoutAlignmentY(final Container target)
    {
        return (float)0.0;
    }
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
     */
    public void invalidateLayout(final Container target)
    {
        synchronized (target.getTreeLock()) 
        {
            preferredSize.setSize(0, 0);
            calcPreferredSize();
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
     */
    public Dimension maximumLayoutSize(final Container target)
    {
        synchronized (target.getTreeLock()) 
        {
            calcPreferredSize();
            return new Dimension(minimumSize);
        }
    }

}
