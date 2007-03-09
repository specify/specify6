/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui.tmanfe;

import javax.swing.table.AbstractTableModel;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 8, 2007
 *
 */
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

}

