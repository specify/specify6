/**
 * 
 */
package edu.ku.brc.ui;

import javax.swing.JTable;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.tmanfe.SearchReplacePanel;

/**
 * @author megkumin
 *
 */
public class TableSearcher
{

    int ROWDIM = 50;
    int initialRow = -1;
    int initialCol = -1;
    boolean isFirstSearch = true;
    protected static final Logger    log                     = Logger.getLogger(SearchReplacePanel.class);
    
    /**
     * Constructor. 
     */
    public TableSearcher()
    {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * @param search
     * @param table
     * @param row
     * @param col
     * @param matchCase
     * @return
     */  
    public TableSearcherCell checkCellForMatch(final String search, final JTable table, 
                                          final int row, final int column, final boolean matchCase)
    {
        log.debug("checkCellForMatch - searchString[" + search + "] CurrentRow[" + row + "] CurrentCol[" + column + "]");
        boolean found = false;
        String searchString = search;
        if (table.getValueAt(row,column) != null)
          {
            String valueInTable = table.getValueAt(row,column).toString();
            log.debug("checkCellForMatch -checking to replace valueInTable: " + valueInTable.getClass());
            if (!matchCase)
            {
                valueInTable = valueInTable.toLowerCase();
                searchString = searchString.toLowerCase();
            }
                if (valueInTable.contains(searchString))
                {
                    log.debug("checkCellForMatch - This cell constains the search value!");
                    found = true;
                    isFirstSearch = true;
                    return new TableSearcherCell(row, column, found);
                }            
          }
        log.debug("checkCellForMatch: ran into a null value");
        isFirstSearch = true;
        return new TableSearcherCell(-1, -1, found);
    }
    
    private TableSearcherCell searchTableForMatchingCellBackwards(final String search, final JTable table,  
                                                       final int row, final int column, 
                                                       final boolean matchCase, final boolean isWrapOn)
    {
        log.debug("searchTableForMatchingCellBackwards: + searchString: " + search + " Current row: " + row + " Current col: " + column);
        String searchString = search;
        boolean found = false;
        int colCnt = table.getColumnCount();
        int rowCnt = table.getRowCount();
        int currentRow = row;
        int currentColumn = column; 

        boolean isStartOfRow = true;

        for (int i = currentRow; i > -1; i--) 
        {
            if (!isStartOfRow)
            {
                currentColumn = colCnt -1 ;
            }
          for(int j = currentColumn; j > -1; j--) 
          {
              if (table.getValueAt(i,j) != null)
              {
                  if ((!isFirstSearch) && (initialRow == i) &&  (initialCol == j))
                  {
                      found = false;
                      return new TableSearcherCell(-1, -1, found);
                  }
                    String valueInTable = table.getValueAt(i,j).toString();
                              
                    if (!matchCase)
                    {
                        valueInTable = valueInTable.toLowerCase();
                        searchString = searchString.toLowerCase();
                    }
                    if (valueInTable.contains(searchString))
                    {
                        log.debug("searchTableForMatchingCellBackwards - Found!");
                        found = true;
                        isFirstSearch = true;
                        return new TableSearcherCell(i, j, found);
                    }
              }
              isStartOfRow = false;
          }
        }  
       
        if(isWrapOn)
        {
            isFirstSearch = false;
            return searchTableForMatchingCellBackwards( searchString,  table,  (rowCnt-1), (colCnt-1),  matchCase,  isWrapOn);
        }
        found = false;
        isFirstSearch = true;
        return new TableSearcherCell(-1, -1, found);
    }


    public TableSearcherCell searchTableForMatchingCell(final String search, final JTable table, 
                                             final int row, final int column, 
                                             final boolean matchCase, final boolean forwards, final boolean isWrapOn)
    {
         
        String searchString = search;
        int currentRow = row;
        int currentColumn = column; 
        log.debug("searchTableForMatchingCell() - Search value[" + search + "] Current row[" + currentRow + "] Current col[" + currentColumn+"] isFirstPass["+ isFirstSearch +"]");
        
        if (isFirstSearch)
        {
             initialRow = currentRow;
             initialCol = currentColumn;     
        }
        
        //log.debug("searchTableForMatchingCell() - initialRow[" + initialRow + "] initialCol[" + initialCol +"]");
        //if it's done a full wrap search
        if ((!isFirstSearch) && (initialRow == currentRow) &&  (initialCol == currentColumn))
        {
            log.debug("searchTableForMatchingCell() - search has wrapped the table and value was not found");
            return new TableSearcherCell(-1, -1, false);
        }
        if (!forwards)
        {
            log.debug("searchTableForMatchingCell() - need to search backwards");
            return searchTableForMatchingCellBackwards(searchString, table, currentRow,  currentColumn,matchCase, isWrapOn); 
        } 
       
        int colCnt = table.getColumnCount();
        int rowCnt = table.getRowCount();
        boolean isStartOfRow = true;

        for (int i = currentRow; i < rowCnt; i++)
        {
            if (!isStartOfRow)
            {
                currentColumn = 0;
            }
            for (int j = currentColumn; j < colCnt; j++)
            {
                if (table.getValueAt(i, j) != null)
                {
                    if ((!isFirstSearch) && (initialRow == i) && (initialCol == j))
                    {
                        return new TableSearcherCell(-1, -1, false);
                    }
                    String valueInTable = table.getValueAt(i, j).toString();

                    if (!matchCase)
                    {
                        valueInTable = valueInTable.toLowerCase();
                        searchString = searchString.toLowerCase();
                    }
                    if (valueInTable.contains(searchString))
                    {
                        log.debug("searchTableForMatchingCell() - Found! value at Row["+i+"] Col["+j+"]");
                        isFirstSearch = true;
                        return new TableSearcherCell(i, j, true);
                    }
                }
                isStartOfRow = false;
            }
        }  
        
        if(isWrapOn)
        {
            log.debug("searchTableForMatchingCell() - wrap is on, moving to start of table");
            isFirstSearch = false;
            return searchTableForMatchingCell( searchString,  table,  0, 0,  matchCase,  forwards,  isWrapOn);
        }
        isFirstSearch = true;
        return new TableSearcherCell(-1, -1, false);
    }
    

    /**
     * @param args
     * void
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

}
