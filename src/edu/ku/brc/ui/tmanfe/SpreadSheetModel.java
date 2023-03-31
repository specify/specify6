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
package edu.ku.brc.ui.tmanfe;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 8, 2007
 *
 */
@SuppressWarnings("serial")
public abstract class SpreadSheetModel extends AbstractTableModel
{
    protected SpreadSheet spreadSheet = null;
    
    public SpreadSheetModel()
    {
        super();
    }
    
    public void setSpreadSheet(final SpreadSheet spreadSheet)
    {
        this.spreadSheet = spreadSheet;
    }
   
    public abstract void clearCells(int[] rows, int[] cols);
    
    public abstract void deleteRows(int[] rows);
    
    public abstract void fill(int colInx, int valueRowInx, int[] rowInxs);
    
    public abstract void insertRow(int rowInx);
    
    public abstract void appendRow();
    
    public abstract int getColDataLen(final int column);
        
    /**
     * @param value
     * 
     * Allows painting, sorting, and other operations to be postponed while
     * large amounts of data are being updated. 
     */
    public abstract void setBatchMode(final boolean value);
    
    /**
     * @return true if model is in batch mode.
     */
    public abstract boolean isBatchMode();
    
    /**
     * Cleans up references.
     */
    public void cleanUp()
    {
        for (TableModelListener l : getTableModelListeners())
        {
            removeTableModelListener(l);
        }
        spreadSheet = null;
    }

}

