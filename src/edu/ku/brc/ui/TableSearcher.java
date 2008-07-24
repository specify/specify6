/**
 * 
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
    protected static final Logger   log                = Logger.getLogger(TableSearcher.class);
    
    protected int                   initialRow;//         = -1;
    protected int                   initialCol;//         = -1;
    protected boolean               isFirstPassOnTable;// = true;
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
        this.isFirstPassOnTable = true;
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
                                      final boolean isMatchCaseOn)
    {
        log.debug("findNext() called");
        if (!isTableValid()) return null;     
        stopTableEditing(); 
        
        String findValue = searchValue;       
        int curRow = prevRow;
        int curCol = prevColumn;
        
        log.debug("findNext() - FindValue[" + findValue + "] ");
        log.debug("               prevRow[" + curRow + "] ");
        log.debug("            prevColumn[" + curCol+ "] ");
        log.debug("         isSearchDown[" + isSearchDown + "]");

        curRow = getNextRow(curRow, curCol, isSearchDown, isWrapOn);
        curCol = getNextColumn(curRow, curCol, isSearchDown, isWrapOn);
        log.debug("                newRow[" + curRow + "] ");
        log.debug("             newColumn[" + curCol+ "] ");
        TableSearcherCell cell = searchTableForValue(findValue, curRow, curCol, isMatchCaseOn, isSearchDown, isWrapOn); 
        return    cell;
    }       
//      
//      if (isSearchDown)
//      {
//          if (curRow == -1)  curRow = 0;
//          //if current column is at end of table, start searching at the next row.
//          if (curCol >= (table.getColumnModel().getColumnCount() - 1))
//          {
//              curRow++;
//              curCol = -1;
//          }
//          // if if current row is the last row in teh table, wrap search to first row
//          if (isWrapOn && curRow >= table.getRowCount())
//          {
//              curRow = 0;
//          }
//          curCol++;
//      }
//      
//      //is previous clicked, reverse direction
//      else
//      {
//          // if current row is not selected, start at the last row of the table
//          if (curRow == -1) curRow = table.getRowCount()-1;            
//          if (curCol <= 0 ) 
//          {
//              curRow--;
//              curCol = table.getColumnModel().getColumnCount();
//          }
//          curCol--;     
//      }
//      
//      //reset();
//      log.debug("               nextRow[" + curRow + "] ");
//      log.debug("            nextColumn[" + curCol+ "] ");
   
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
   */
    public void replace(final TableSearcherCell cell, 
                        final String findValue, 
                        final String replaceValue, 
                        final boolean isMtchCaseOn)
    {
        log.debug("replace() called");
        if (!isTableValid()) { return; }
        if (cell == null) { return; }
        stopTableEditing();  
        
        
        int row = cell.getRow();
        int col = cell.getColumn();  
         
        Object o = table.getValueAt(row, col);
        if (!(o instanceof String))
        {
            // TODO implement replace for booleans and integers.
            log.info("replace () The value  value=[ " + o.toString() + "] ");
            log.info("                        row=[" + row + "] ");
            log.info("                        col=[" + col + "] ");
            log.info("                        is not a String and cannot be replaced");
            return;
        }
        
        if (row == -1 || col == -1)
        {
            findPanel.setStatusLabelWithFailedFind();
            return;
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
            
            log.info("replace() Old value=[" + oldValue + "] ");
            log.info("          New value=[" + newValue + "] ");
            log.info("                row=[" + row + "] ");
            log.info("                col=[" + col + "] ");
            table.setValueAt(newValue, row, col);
        } 
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
     * @return
     */  
    public TableSearcherCell checkCellForMatch (final String searchStringArg,
                                                final int row, 
                                                final int column, 
                                                final boolean isMatchCaseOn)
    {
        String searchString = searchStringArg;
        log.debug("checkCellForMatch() - Search value[" + searchString + "] ");
        log.debug("                       Current row[" + row + "] ");
        log.debug("                       Current col[" + column + "] ");
        log.debug("                         matchcase[" + isMatchCaseOn + "] ");
        Object cellValue = table.getValueAt(row, column);
        if (cellValue != null)
        {
            String valueInTable = cellValue.toString();
            log.debug("                Value in table col[" + valueInTable + "] ");
            if (!isMatchCaseOn)
            {
                valueInTable = valueInTable.toLowerCase();
                searchString = searchString.toLowerCase();
            }
            if (valueInTable.contains(searchString))
            {
                isFirstPassOnTable = true;
                return new TableSearcherCell(row, column, true, cellValue.toString());
            }
        }
        isFirstPassOnTable = true;
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
                                                       final boolean isWrapOn)
    {
        String searchString = searchStringArg;
        int currentRow = row;
        int currentColumn = column; 
        int numOfCols = table.getColumnCount();
        int numOfRows = table.getRowCount();        
        boolean isStartOfRow = true;
        
        log.debug("searchTableForValueBkwds() - Search value[" + searchString + "] ");
        log.debug("                              Current row[" + currentRow + "] ");
        log.debug("                              Current col[" + currentColumn+"] ");
        log.debug("                              isFirstPass["+ isFirstPassOnTable +"]");        
        log.debug("                             Starting Row[" + initialRow + "]"); 
        log.debug("                             Starting Col[" + initialCol +"]");
        log.debug("                           isMatchCaseOn [" + isMatchCaseOn +"]");
        
        for (int i = currentRow; i > -1; i--)
        {
            if (!isStartOfRow)
            {
                currentColumn = numOfCols - 1;
            }
            for (int j = currentColumn; j > -1; j--)
            {
                Object cellValue = table.getValueAt(i,j); 
                if (cellValue != null)
                {
                    if ((!isFirstPassOnTable) && (initialRow == i) && (initialCol == j))
                    {
                        return new TableSearcherCell(-1, -1, false, null);
                    }
                    String valueInTable = cellValue.toString();

                    if (!isMatchCaseOn)
                    {
                        valueInTable = valueInTable.toLowerCase();
                        searchString = searchString.toLowerCase();
                    }
                    if (valueInTable.contains(searchString))
                    {
                        log.debug("searchTableForValueBkwds - Found!");
                        isFirstPassOnTable = true;
                        return new TableSearcherCell(i, j, true, cellValue.toString());
                    }
                }
                isStartOfRow = false;
            }
        }  
       
        if(isWrapOn)
        {
            isFirstPassOnTable = false;
            numOfRows--;
            numOfCols--;
            return searchTableForValueBkwds(searchString, numOfRows, numOfCols, isMatchCaseOn, isWrapOn);
        }
        isFirstPassOnTable = true;
        return new TableSearcherCell(-1, -1, false, null);
    }


    private TableSearcherCell searchTableForValue(final String searchString,
                                                 final int row,
                                                 final int column,
                                                 final boolean isMatchCaseOn,
                                                 final boolean isForwardSearch,
                                                 final boolean isWrapOn)
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
        log.debug("searchTableForValue() - Search value[" + searchString + "] ");
        log.debug("                         Current row[" + currentRow + "] ");
        log.debug("                         Current col[" + currentColumn+"] ");
        log.debug("                         isFirstPass["+ isFirstPassOnTable +"]");        
        log.debug("                        Starting Row[" + initialRow + "]");  
        log.debug("                        Starting Col[" + initialCol +"]");
        log.debug("                      isMatchCaseOn [" + isMatchCaseOn +"]");

        if ((!isFirstPassOnTable) && (initialRow == currentRow) && (initialCol == currentColumn))
        {
            log.debug("searchTableForValue() - search has wrapped the table and value was not found");
            return new TableSearcherCell(-1, -1, false, null);
        }        
        if (!isForwardSearch)
        {
            log.debug("searchTableForValue() - need to search backwards");
            TableSearcherCell matchCell =  searchTableForValueBkwds(searchString, currentRow, currentColumn,  isMatchCaseOn, isWrapOn);
            return matchCell;
        } 
        boolean isStartOfRow = true;
        for (int i = currentRow; i < numOfRows; i++)
        {
            if (!isStartOfRow)  currentColumn = 0;
            
            
            for (int j = currentColumn; j < numOfCols; j++)
            {
                log.debug("searchTableForValue()                         Current initialRow[" + initialRow + "] ");
                log.debug("searchTableForValue()                         Current initialCol[" + initialCol+"] ");
                log.debug("searchTableForValue()                         Current row[" + i + "] ");
                log.debug("searchTableForValue()                         Current col[" + j+"] ");
                if ((!isFirstPassOnTable) && (initialRow == i) && (initialCol == j))
                {
                    log.debug("isFirstPassOnTable" + isFirstPassOnTable);
                    log.debug("initialRow" + initialRow);
                    log.debug("initialCol" + initialCol);
                    return new TableSearcherCell(-1, -1, false, null);
                }
                log.debug("calling checkCellForMatch");
                TableSearcherCell cell = checkCellForMatch(searchString, i, j, isMatchCaseOn);
                if(cell.isFound())
                {
                    log.debug("reutrning match");
                    return cell;
                }
                isStartOfRow = false;
            }
        }  
        
        if(isWrapOn)
        {
            log.debug("searchTableForValue() - wrap is on, moving to start of table");
            isFirstPassOnTable = false;
            TableSearcherCell matchCell = searchTableForValue( searchString,  0, 0,  isMatchCaseOn,  isForwardSearch,  isWrapOn);
            return matchCell;
        }
        isFirstPassOnTable = true;
        return new TableSearcherCell(-1, -1, false, null);
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
