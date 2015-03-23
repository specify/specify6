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
package edu.ku.brc.af.prefs;

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

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
@SuppressWarnings("serial") //$NON-NLS-1$
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
        title = createLabel(titleStr);
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
