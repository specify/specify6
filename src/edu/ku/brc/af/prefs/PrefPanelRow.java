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

package edu.ku.brc.af.prefs;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.ku.brc.ui.UICacheManager;

/**
 * 
 * One row (or section) of the prefs when it is laaid out in a grid  
 * (Currently not in use)
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class PrefPanelRow extends JPanel
{

    protected PrefPanelRowLayoutManager layout;
    protected JLabel                    title;
    
    /**
     * Constructor.
     * @param titleStr the title string of the panel row
     */
    public PrefPanelRow(final String titleStr)
    {
        title = new JLabel(titleStr);
        title.setFont(UICacheManager.getFont(JLabel.class));
        setLayout(layout = new PrefPanelRowLayoutManager(title));     
        add(title);
        
    }
    
    /**
     * Return the preferred Cel size which is the largest cell width and height dims.
     * @return Return the preferred Cel size which is the largest cell width and height dims
     */
    public Dimension getPreferredCellSize()
    {
        return layout.getPreferredCellSize();
    }
    
    /**
     * Return the width calculated as the number of cells (minus the title) mulitplied by the number of cells.
     * @param actualCellSize the actual size of a cell
     * @return the max width calulated from the actual cell size
     */
    public int getActualWidth(final Dimension actualCellSize)
    {
        return layout.getActualWidth(actualCellSize);
        //getComponentCount() - 1) * actualCellSize.width;
    }
    
    /**
     * Sets the actual cell size for layout.
     * @param actualCellSize the dim of the cell, which is the same size as all other cells in the panel
     */
    public void setActualCellSize(Dimension actualCellSize)
    {
        layout.setActualCellSize(actualCellSize);
    }
    
    /**
     * Sets the actual size of row.
     * @param actualRowSize the actual size of row
     */
    public void setActualRowSize(final Dimension actualRowSize)
    {
        layout.setActualRowSize(actualRowSize);
    }
    
    /**
     * Sets the most number of items on a row.
     * @param maxNumItems the most number of items on a row
     */
    public void setMaxNumItems(final int maxNumItems)
    {
        layout.setMaxNumItems(maxNumItems);
    }

    /**
     * Returns the JLabel for the panel.
     * @return Returns the JLabel for the panel
     */
    public JLabel getTitle()
    {
        return title;
    }
    
}
