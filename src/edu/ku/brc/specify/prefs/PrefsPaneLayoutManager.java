/* Filename:    $RCSfile: PrefsPaneLayoutManager.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.prefs;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.List;

/**
 * The layout manager for laying all the cells in the panel, but the size comes from the largest cell
 * dimensions of all the cells in the entire panel.
 * 
 * (Currently not in use)
 * 
 * @author rods
 *
 */
public class PrefsPaneLayoutManager implements LayoutManager, LayoutManager2
{
    
    private List<Component> comps             = new ArrayList<Component>();
    private Dimension       preferredSize     = new Dimension();
    private Dimension       actualCellSize    = new Dimension(32, 32);
    private Dimension       actualRowSize     = new Dimension(0, 0);
    
    private int             maxNumItems       = 0;

    /**
     * Contructs a layout manager for layting out NavBoxes. It lays out all the NavBoxes vertically 
     * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
     * around all the boxes
    */
    public PrefsPaneLayoutManager()
    {
    }
    
    /**
     * Sets the actual cell size for layout
     * @param actualCellSize the dim of the cell, which is the same size as all other cells in the panel
     */
    public void setActualCellSize(Dimension actualCellSize)
    {
        this.actualCellSize = actualCellSize;
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
        
        if (arg1 instanceof PrefPanelRow)
        {
            comps.add((PrefPanelRow)arg1);
        }

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
        comps.remove(arg0);

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
        if (preferredSize.width == 0 || preferredSize.height == 0)
        {
            calcPreferredSize(); 
        }
        
        int x = 0;
        int y = 0;
        
        actualRowSize.width = Math.max(actualRowSize.width, arg0.getSize().width);
        
        for (Component comp : comps)
        {
            PrefPanelRow ppr   = (PrefPanelRow)comp;
            ppr.setActualCellSize(actualCellSize);
            ppr.setActualRowSize(actualRowSize);
            ppr.setMaxNumItems(maxNumItems);
            comp.setBounds(x, y, actualRowSize.width, actualRowSize.height);
            y += actualRowSize.height;
        }

    }
    
    /**
     * Calculates the preferred size of the contain. It lays out all the NavBoxes vertically 
     * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
     * around all the boxes
     *
     */
    protected void calcPreferredSize()
    {
        preferredSize.width   = 0;
        
        actualCellSize.width  = 0;
        actualCellSize.height = 0;
        
        // Find the max height and the largest any given cell wants to be
        int rowHeight   = 0;
        for (Component comp : comps)
        {
            PrefPanelRow ppr               = (PrefPanelRow)comp;
            Dimension    size              = ppr.getPreferredSize();
            Dimension    preferredCellSize = ppr.getPreferredCellSize();
            
            actualCellSize.width  = Math.max(actualCellSize.width, preferredCellSize.width);
            actualCellSize.height = Math.max(actualCellSize.height, preferredCellSize.height);
            
            rowHeight   = Math.max(rowHeight, size.height);
            maxNumItems = Math.max(ppr.getComponentCount()-1, maxNumItems);
        }
        
        // Now that we now the size of each cell, than have them tells what their max width would be
        for (Component comp : comps)
        {
            PrefPanelRow ppr    = (PrefPanelRow)comp;
            preferredSize.width = Math.max(preferredSize.width, ppr.getActualWidth(actualCellSize));
        }
        
        actualRowSize.width  = preferredSize.width;
        actualRowSize.height = rowHeight;
        preferredSize.height = rowHeight * comps.size();
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
        
        if (comp instanceof PrefPanelRow)
        {
            comps.add(comp);
        }
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
        preferredSize.setSize(0, 0);
        calcPreferredSize();
    }
    public Dimension maximumLayoutSize(Container target) 
    {
        calcPreferredSize();        
        return new Dimension(preferredSize); 
    }

}
