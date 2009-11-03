/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.ui;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.tmanfe.SearchReplacePanel;
import edu.ku.brc.ui.tmanfe.SpreadSheet;

/**
 * @author megkumin
 *
 */
public class TableSearcher
{
    private static final boolean  debugging            = false;
    
    protected static final Logger   log                = Logger.getLogger(TableSearcher.class);
    
    protected int                   initialRow;
    protected int                   initialCol;
    protected boolean               isFirstPassOnTable;
    protected int                   replacementCount;
    protected SpreadSheet           table;
    protected SearchReplacePanel    findPanel;
    
    /**
     * Constructor. 
     */
    public TableSearcher(SpreadSheet table, SearchReplacePanel findPanel)
    {
        this.table = table;
        this.findPanel = findPanel;
        this.initialRow         = -1;
        this.initialCol         = -1;
        reset();
    }
    
    /**
     * sets first pass to true and clears replacement count
     */
    protected void reset()
    {
    	isFirstPassOnTable = true;
    	replacementCount = 0;
    }
    
    /**
     * @param prevRow
     * @param prevColumn
     * @param isSearchDown
     * @param isWrapOn
     * @return
     */
    public int getNextRow(final int prevRow, 
                         final int prevColumn,
                         final boolean isSearchDown,
                         final boolean isWrapOn)
    {
        return getNewCellValue(prevRow, prevColumn,isSearchDown ,isWrapOn, true);
    }
    
    /**
     * @param prevRow
     * @param prevColumn
     * @param isSearchDown
     * @param isWrapOn
     * @return
     */
    public int getNextColumn(final int prevRow, 
                            final int prevColumn,
                            final boolean isSearchDown,
                            final boolean isWrapOn)
    {
        return getNewCellValue(prevRow, prevColumn,isSearchDown ,isWrapOn, false);
    }
    
    /**
     * @param prevRow
     * @param prevColumn
     * @param isSearchDown
     * @param isWrapOn
     * @param returnRow
     * @return
     */
    private int getNewCellValue(final int prevRow, 
                                final int prevColumn,
                                final boolean isSearchDown,
                                final boolean isWrapOn,
                                final boolean returnRow)
    {
        int curRow = prevRow;
        int curCol = prevColumn;
        
        if (isSearchDown)
        {
            if (curRow == -1)  curRow = 0;
            //if current column is at end of table, start searching at the next row.
            if (curCol >= (table.getColumnModel().getColumnCount() - 1))
            {
                curRow++;
                curCol = -1;
            }
            // if if current row is the last row in teh table, wrap search to first row
            if (isWrapOn && curRow >= table.getRowCount())
            {
                curRow = 0;
            }
            curCol++;
        }
        
        //is previous clicked, reverse direction
        else
        {
            // if current row is not selected, start at the last row of the table
            if (curRow == -1) curRow = table.getRowCount()-1;            
            if (curCol <= 0 ) 
            {
                curRow--;
                curCol = table.getColumnModel().getColumnCount();
            }
            curCol--;     
        }
        if(returnRow)
            return curRow;
        return curCol;
    }
    
    /**
     * @param searchValue
     * @param prevRow
     * @param prevColumn
     * @param isSearchDown
     * @param isWrapOn
     * @param isMatchCaseOn
     * @return
     */
    public TableSearcherCell findNext(final String searchValue,
                                      final int prevRow, 
                                      final int prevColumn, 
                                      final boolean isSearchDown, 
                                      final boolean isWrapOn, 
                                      final boolean isMatchCaseOn,
                                      final boolean isSearchSelection)
    {
        if (debugging)
        {
            log.debug("findNext() called");
        }
        
        if (!isTableValid()) return null;     
        stopTableEditing(); 
        
        String findValue = searchValue;       
        int curRow = prevRow;
        int curCol = prevColumn;
        
        if (debugging)
        {
            log.debug("findNext() - FindValue[" + findValue + "] ");
            log.debug("               prevRow[" + curRow + "] ");
            log.debug("            prevColumn[" + curCol+ "] ");
            log.debug("         isSearchDown[" + isSearchDown + "]");
        }
        
        curRow = getNextRow(curRow, curCol, isSearchDown, isWrapOn);
        curCol = getNextColumn(curRow, curCol, isSearchDown, isWrapOn);
        
        if (debugging)
        {
            log.debug("                newRow[" + curRow + "] ");
            log.debug("             newColumn[" + curCol+ "] ");
        }
        
        TableSearcherCell cell = searchTableForValue(findValue, curRow, curCol, isMatchCaseOn, isSearchDown, isWrapOn, isSearchSelection); 
        return    cell;
    }       
   
    /**
     * @return boolean false if the table is null
     */
    private boolean isTableValid()
    {
        if (table == null)
        {
            findPanel.setStatusLabelWithFailedFind();
            return false;
        }
        return true;
    }
    
    /**
   * replaces the contents of cell where part of the cell contains the string found with the string
   * that is provided for replacement.
   * 
   * returns true if a replacement was actually made.
   */
    public boolean replace(final TableSearcherCell cell, 
                        final String findValue, 
                        final String replaceValue, 
                        final boolean isMtchCaseOn,
                        final boolean isSearchSelection)
    {
        if (debugging)
        {
            log.debug("replace() called");
        }
        
        if (!isTableValid()) { return false; }
        if (cell == null) { return false; }
        stopTableEditing();  
        
        
        int row = cell.getRow();
        int col = cell.getColumn();  
         
        Object o = table.getValueAt(row, col);
        if (!(o instanceof String))
        {
            // TODO implement replace for booleans and integers.
            if (debugging) //excessive logging can really slow down find/replace
            {
                log.info("replace () The value  value=[ " + o.toString() + "] ");
                log.info("                        row=[" + row + "] ");
                log.info("                        col=[" + col + "] ");
                log.info("                        is not a String and cannot be replaced");
            }
            else
            {
                log.info("replace () The value  value=[ " + o.toString() + "] is not a String and cannot be replaced");
            }
            return false;
        }
        
        if (row == -1 || col == -1)
        {
            findPanel.setStatusLabelWithFailedFind();
            return false;
        }
        
        String newValue = "";
        String oldValue = o.toString();
        
        if (cell.isFound())
        {
            if (isMtchCaseOn)
            {
                newValue = Pattern.compile(findValue).matcher(oldValue).replaceAll(replaceValue);
            } else
            {
                //XXX TODO implement case insensitivity");
                newValue = Pattern.compile(Pattern.quote(findValue), Pattern.CASE_INSENSITIVE)
                        .matcher(oldValue).replaceAll(replaceValue);
            }
            
            if (debugging)
            {
                log.info("replace() Old value=[" + oldValue + "] ");
                log.info("          New value=[" + newValue + "] ");
                log.info("                row=[" + row + "] ");
                log.info("                col=[" + col + "] ");
            }
            table.setValueAt(newValue, row, col);
            return true;
        } 
        return false;
    }



    /**
     * stops editing of the table.
     */
    private void stopTableEditing()
    {
        if ((table == null) && (table.getCellEditor() != null))
        {
            table.getCellEditor().stopCellEditing();
            return;
        }
    }
    
    /**
     * @param searchStringArg
     * @param myTable
     * @param row
     * @param col
     * @param isMatchCaseOn
     * @return true if cell at row and column contains searchStringArg
     * 
     * NOTE: Will return true when searchStringArg = ""
     */  
    public TableSearcherCell checkCellForMatch (final String searchStringArg,
                                                final int row, 
                                                final int column, 
                                                final boolean isMatchCaseOn)
    {
        String searchString = searchStringArg;
        if (debugging)
        {
            log.debug("checkCellForMatch() - Search value[" + searchString + "] ");
            log.debug("                       Current row[" + row + "] ");
            log.debug("                       Current col[" + column + "] ");
            log.debug("                         matchcase[" + isMatchCaseOn + "] ");
        }
        Object cellValue = table.getValueAt(row, column);
        if (cellValue != null)
        {
            String valueInTable = cellValue.toString();
            if (debugging)
            {
                log.debug("                value in table col[" + valueInTable + "] ");
            }
            if (!isMatchCaseOn)
            {
                valueInTable = valueInTable.toLowerCase();
                searchString = searchString.toLowerCase();
            }
            if (valueInTable.contains(searchString))
            {
                //XXX is the next line below necessary?? Be sure to thoroughly test wrap and next->replace and previous...
            	isFirstPassOnTable = true;
                return new TableSearcherCell(row, column, true, cellValue.toString());
            }
        }
        return new TableSearcherCell(-1, -1, false, null);
    }
    
    /**
     * @param searchStringArg
     * @param row
     * @param column
     * @param isMatchCaseOn
     * @param isWrapOn
     * @return
     */
    private TableSearcherCell searchTableForValueBkwds(final String searchStringArg,
                                                       final int row,
                                                       final int column,
                                                       final boolean isMatchCaseOn,
                                                       final boolean isWrapOn,
                                                       final boolean isSearchSelection)
    {
        String searchString = searchStringArg;
        int currentRow = row;
        int currentColumn = column; 
        int numOfCols = table.getColumnCount();
        int numOfRows = table.getRowCount();        
        boolean isStartOfRow = true;
        
        if (debugging)
        {
            log.debug("searchTableForValueBkwds() - Search value[" + searchString + "] ");
            log.debug("                              Current row[" + currentRow + "] ");
            log.debug("                              Current col[" + currentColumn + "] ");
            log.debug("                              isFirstPass[" + isFirstPassOnTable + "]");
            log.debug("                             Starting Row[" + initialRow + "]");
            log.debug("                             Starting Col[" + initialCol + "]");
            log.debug("                           isMatchCaseOn [" + isMatchCaseOn + "]");
        }
        
        for (int i = currentRow; i > -1; i--)
        {
            if (!isStartOfRow)
            {
                currentColumn = numOfCols - 1;
            }
            for (int j = currentColumn; j > -1; j--)
            {
//                Object cellValue = table.getValueAt(i,j); 
//                if (cellValue != null)
//                {
//                    if ((!isFirstPassOnTable) && (initialRow == i) && (initialCol == j))
//                    {
//                        return new TableSearcherCell(-1, -1, false, null);
//                    }
//                    String valueInTable = cellValue.toString();
//
//                    if (!isMatchCaseOn)
//                    {
//                        valueInTable = valueInTable.toLowerCase();
//                        searchString = searchString.toLowerCase();
//                    }
//                    if (valueInTable.contains(searchString))
//                    {
//                        if (debugging)
//                        {
//                            log.debug("searchTableForValueBkwds - Found!");
//                        }
//                        isFirstPassOnTable = true;
//                        return new TableSearcherCell(i, j, true, cellValue.toString());
//                    }
//                }
            	TableSearcherCell cell = checkCell(searchString, i, j, isMatchCaseOn, isSearchSelection);
            	if (cell.isFound())
            	{
            		return cell;
            	}
                isStartOfRow = false;
            }
        }  
       
        if(isWrapOn && isFirstPassOnTable)
        {
            isFirstPassOnTable = false;
            numOfRows--;
            numOfCols--;
            return searchTableForValueBkwds(searchString, numOfRows, numOfCols, isMatchCaseOn, isWrapOn, isSearchSelection);
        }
        reset();
        return new TableSearcherCell(-1, -1, false, null);
    }


    /**
     * @param searchString
     * @param i
     * @param j
     * @param isMatchCaseOn
     * @param isSearchSelection
     * @return Match status for cell.
     */
    public TableSearcherCell checkCell(final String searchString,
    									final int i,
    									final int j,
    									final boolean isMatchCaseOn,
    									final boolean isSearchSelection)
    {
        if (!isFirstPassOnTable && initialRow == i && initialCol == j)
        {
            if (debugging)
            {
                log.debug("isFirstPassOnTable" + isFirstPassOnTable);
                log.debug("initialRow" + initialRow);
                log.debug("initialCol" + initialCol);
            }
            return new TableSearcherCell(-1, -1, false, null);
        }
        if (debugging)
        {                
            log.debug("calling checkCellForMatch");
        }
        if (!isSearchSelection || table.isCellSelected(i, j))
        {
        	TableSearcherCell cell = checkCellForMatch(searchString, i, j, isMatchCaseOn);
        	if(cell.isFound())
        	{
        		if (debugging)
        		{
        			log.debug("returning match");
        		}
        		return cell;
        	}
        }
        return new TableSearcherCell(-1, -1, false, null);
    }
    
    private TableSearcherCell searchTableForValue(final String searchString,
                                                 final int row,
                                                 final int column,
                                                 final boolean isMatchCaseOn,
                                                 final boolean isForwardSearch,
                                                 final boolean isWrapOn,
                                                 final boolean isSearchSelection)
    {
         
        int currentRow = row;
        int currentColumn = column; 
        int numOfCols = table.getColumnCount();
        int numOfRows = table.getRowCount();
        
        if (isFirstPassOnTable)
        {
             initialRow = currentRow;
             initialCol = currentColumn;     
        }
        if (debugging)
        {
            log.debug("searchTableForValue() - Search value[" + searchString + "] ");
            log.debug("                         Current row[" + currentRow + "] ");
            log.debug("                         Current col[" + currentColumn + "] ");
            log.debug("                         isFirstPass[" + isFirstPassOnTable + "]");
            log.debug("                        Starting Row[" + initialRow + "]");
            log.debug("                        Starting Col[" + initialCol + "]");
            log.debug("                      isMatchCaseOn [" + isMatchCaseOn + "]");
        }
        
        if (!isForwardSearch)
        {
            if (debugging)
            {
                log.debug("searchTableForValue() - need to search backwards");
            }
            TableSearcherCell matchCell =  searchTableForValueBkwds(searchString, currentRow, currentColumn,  isMatchCaseOn, isWrapOn, isSearchSelection);
            return matchCell;
        } 
        boolean isStartOfRow = true;
        for (int i = currentRow; i < numOfRows; i++)
        {
            if (!isStartOfRow)  currentColumn = 0;
            
            
            for (int j = currentColumn; j < numOfCols; j++)
            {
                if (debugging)
                {
                     log.debug("searchTableForValue() Current initialRow[" + initialRow + "] ");
                     log.debug("searchTableForValue() Current initialCol[" + initialCol+"] ");
                     log.debug("searchTableForValue() Current row[" + i + "] ");
                     log.debug("searchTableForValue() Current col[" + j+"] ");
                }
                TableSearcherCell cell = checkCell(searchString, i, j, isMatchCaseOn, isSearchSelection);
                if (cell.isFound())
                {
                	return cell;
                }
                isStartOfRow = false;
            }
        }  
        
        if(isWrapOn && isFirstPassOnTable)
        {
            if (debugging)
            {
                log.debug("searchTableForValue() - wrap is on, moving to start of table");
            }
            isFirstPassOnTable = false;
            TableSearcherCell matchCell = searchTableForValue( searchString,  0, 0,  isMatchCaseOn,  isForwardSearch,  isWrapOn, isSearchSelection);
            return matchCell;
        }
        isFirstPassOnTable = true;
        return new TableSearcherCell(-1, -1, false, null);
    }
    
    /**
     * called to finalize replacements.
     */
    public void replacementCleanup()
    {
    	if (replacementCount > 0)
    	{
    		table.getModel().fireTableDataChanged();
    	}
    }

	/**
	 * @return the replacementCount
	 */
	public int getReplacementCount()
	{
		return replacementCount;
	}
    
    
//  /**
//  * replaces all of the values where a cell contains the string
//  * @return
//  */
// public TableSearcherCell replacxeAll(final String findValue, 
//                                     final String replaceValue,
//                                     final boolean isMatchCaseOn,
//                                     final boolean isForwardSearch,
//                                     final boolean isWrapOn)
// {
//     if (!isTableValid())
//         return new TableSearcherCell(-1, -1, false, null);
//     stopTableEditing();
//     
//     
//     int curRow = 0;
//     int curCol = 0;
//
//     findPanel.setSearchDown(true);
//
//     log.debug("replaceAll() creating TableSearcher()");
//     //TableSearcher tableSearch = new TableSearcher();
//     //reset();!!!!!
//     TableSearcherCell cell = searchTableForValue(findValue, curRow, curCol,isMatchCaseOn, isForwardSearch, isWrapOn);
//     boolean found = cell.isFound();  
//     if (!found)
//     {
//       //log.debug("repalceall() found nothing");
//       //searching down
//       //wrap is not on & end of table reached
//       //next button disable, previous button enabled
//       if (isForwardSearch)
//       {
//           findPanel.setFinishedSearchingDown(true);
//           if (!isWrapOn)
//           {
//               findPanel.disableNextButton();
//              // nextButton.setEnabled(false);
//               findPanel.enablePreviousButton();
//               //previousButton.setEnabled(true);                 
//           }
//           findPanel.setStatusLabelWithFailedFind();
//       } 
//       //searching up
//       //wrap is not on & top of table reached
//       //next button enabled, previous button disabled
//       else
//       {
//           findPanel.setFinishedSearchingUp(true);
//           if (!isWrapOn)
//           {
//               findPanel.enableNextButton();
//               findPanel.disablePreviousButton();
//           }
//           findPanel.setStatusLabelWithFailedFind();
//       }      
//     }
//     while (found)
//     {           
//         curRow = cell.getRow();
//         curCol = cell.getColumn();
//         log.debug("replaceall() found value at row[" + curRow +"] curCol[" + curCol +"]");
//         replace(cell, findValue, replaceValue, isMatchCaseOn);
//         
//         if(curCol >= (table.getColumnModel().getColumnCount()-1)) 
//         {
//             curRow++;
//             curCol = -1;
//         }
//         curCol++;
//         log.debug("replaceall() calling tableSearcher again, incremented row and column");
//         cell = searchTableForValue(findValue, curRow, curCol, isMatchCaseOn, isForwardSearch, false);
//         
//         found = cell.isFound();
//     }  
//     
//     return cell;
////     //nextButton.setEnabled(false);
////     findPanel.disableNextButton();
////     //previousButton.setEnabled(false);
////     findPanel.disablePreviousButton();
////     findPanel.getReplaceButton().setEnabled(false);
////     findPanel.getReplaceAllButton().setEnabled(false);
////     findPanel.setStatusLabelEndReached();
// }  
}
