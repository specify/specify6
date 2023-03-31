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
package edu.ku.brc.af.prefs;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

/**
 * The layout manager for laying all the cells in the panel, but the size comes from the largest cell
 * dimensions of all the cells in the entire panel.
 * 
 * (Currently not in use)
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class PrefPanelRowLayoutManager implements LayoutManager, LayoutManager2
{
    
    private List<Component> comps             = new ArrayList<Component>();
    private Dimension       preferredSize     = new Dimension();
    private Dimension       preferredCellSize = new Dimension(32, 32);
    private Dimension       actualCellSize    = null;
    private Dimension       actualRowSize     = null;

    private int             ySeparation       = 5;
    private int             xSeparation       = 10;
    private int             titleSeparation   = 2;
    private int             maxNumItems       = 0;
    
    private JLabel          title = null;

    /**
     * Contructs a layout manager for layting out NavBoxes. It lays out all the NavBoxes vertically 
     * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
     * around all the boxes.
     * @param title the JLabel title
     */
    public PrefPanelRowLayoutManager(final JLabel title)
    {
        this.title = title;
    }
    
    /**
     * Sets the actual cell size for layout.
     * @param actualCellSize the dim of the cell, which is the same size as all other cells in the panel
     */
    public void setActualCellSize(Dimension actualCellSize)
    {
        this.actualCellSize = actualCellSize;
    }
    
    /**
     * Sets the actual size of row.
     * @param actualRowSize the actual size of row
     */
    public void setActualRowSize(final Dimension actualRowSize)
    {
        this.actualRowSize = actualRowSize;
    }

    
    /**
     * Return the preferred celll size of all the cells in this row.
     * @return Return the preferred celll size of all the cells in this row
     */ 
    public Dimension getPreferredCellSize()
    {
        return preferredCellSize;
    }

    /**
     * Sets the most number of items on a row.
     * @param maxNumItems the most number of items on a row
     */
    public void setMaxNumItems(final int maxNumItems)
    {
        this.maxNumItems = maxNumItems;
    }
    
    /**
     * Return the width calculated as the number of cells (minus the title) mulitplied by the number of cells.
     * @param actualCellSize the actual size of a cell
     * @return the max width calulated from the actual cell size
     */
    public int getActualWidth(final Dimension cellSize)
    {
        return Math.max((comps.size() * (cellSize.width + xSeparation)) + xSeparation, title.getPreferredSize().width+(2 * xSeparation));
    }


    /* (non-Javadoc)
     * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
     */
    public void addLayoutComponent(String arg0, Component arg1)
    {
        if (arg1 == null)
        {
            throw new NullPointerException("Null component in addLayoutComponent"); //$NON-NLS-1$
        }
        if (arg1 != title)
        {
            comps.add(arg1);
        }

    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
     */
    public void removeLayoutComponent(Component arg0)
    {
        if (arg0 == null)
        {
            throw new NullPointerException("Null component in removeLayoutComponent"); //$NON-NLS-1$
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
    public void layoutContainer(Container target)
    {
        synchronized (target.getTreeLock()) 
        {
            int x = xSeparation;
            int y = ySeparation;
            
            int xSep = (actualRowSize.width - (actualCellSize.width * maxNumItems)) / (maxNumItems+1);
            
            Dimension size = title.getPreferredSize();
            title.setBounds(x, y, size.width, size.height);
            
            y += size.height + titleSeparation;
            //System.out.println("Row Hgt: "+actualRowSize.height+" Y: "+y+"  Cell Hgt: "+actualCellSize.height+"  Space: "+(actualRowSize.height - y));
            
            int height = actualRowSize.height - y;
            y += (height - actualCellSize.height) / 2;
            
            for (Component comp : comps)
            {
                comp.setBounds(x, y, actualCellSize.width, actualCellSize.height);
                x += actualCellSize.width + xSep;
            }
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
        Dimension titleSize = title.getPreferredSize();
        
        int labelWidth = titleSize.width + (2 * xSeparation);
        preferredSize.setSize(xSeparation, titleSize.height + (2 * ySeparation) + titleSeparation);
        
        preferredCellSize.setSize(0,0);
        
        int maxHeight = 0;
        for (Component comp : comps)
        {
            Dimension size = comp.getPreferredSize();
            
            preferredCellSize.width  = Math.max(preferredCellSize.width, size.width);
            preferredCellSize.height = Math.max(preferredCellSize.height, size.height);
            
            preferredSize.width += size.width + xSeparation;
            maxHeight = Math.max(maxHeight, size.height);
        }
        preferredSize.width = Math.max(preferredSize.width, labelWidth);
        preferredSize.height += maxHeight;
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
            throw new NullPointerException("Null component in addLayoutComponent"); //$NON-NLS-1$
        }
        if (comp != title)
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
