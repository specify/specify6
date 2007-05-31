/**
 * 
 */
package edu.ku.brc.ui;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

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
    public TableSearcherCell cellContains(final String search, final JTable table, 
                                          final int row, final int column, final boolean matchCase)
    {
        log.debug("cellContains - searchString: " + search + " Current row: " + row + " Current col: " + column);
        boolean found = false;
        String searchString = search;
        if (table.getValueAt(row,column) != null)
          {
            String valueInTable = table.getValueAt(row,column).toString();
            log.debug("cellContains -checking to replace valueInTable: " + valueInTable.getClass());
            if (!matchCase)
            {
                valueInTable = valueInTable.toLowerCase();
                searchString = searchString.toLowerCase();
            }
                if (valueInTable.contains(searchString))
                {
                    log.debug("cellContains - This cell constains the search value!");
                    found = true;
                    isFirstSearch = true;
                    return new TableSearcherCell(row, column, found);
                }            
          }
        log.debug("cellContains: ran into a null value");
        isFirstSearch = true;
        return new TableSearcherCell(-1, -1, found);
    }
    
    private TableSearcherCell findCellInTableBackwards(final String search, final JTable table,  
                                                       final int row, final int column, 
                                                       final boolean matchCase, final boolean isWrapOn)
    {
        log.debug("tableContainsBackwards: + searchString: " + search + " Current row: " + row + " Current col: " + column);
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
                        log.debug("Found!");
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
            return findCellInTableBackwards( searchString,  table,  (rowCnt-1), (colCnt-1),  matchCase,  isWrapOn);
        }
        found = false;
        isFirstSearch = true;
        return new TableSearcherCell(-1, -1, found);
    }


    public TableSearcherCell findCellInTable(final String search, final JTable table, 
                                             final int row, final int column, 
                                             final boolean matchCase, final boolean forwards, final boolean isWrapOn)
    {
        log.debug("findCellInTable() - searchString[" + search + "] Current row[" + row + "] Current col[" + column+"] isFirstPass["+ isFirstSearch +"]");
        log.debug("findCellInTable() - initialRow[" + initialRow + "] initialCol[" + initialCol +"]");
        String searchString = search;
        int currentRow = row;
        int currentColumn = column; 
        
        if (isFirstSearch)
        {
             initialRow = currentRow;
             initialCol = currentColumn;     
        }
        //if it's done a full wrap search
        if ((!isFirstSearch) && (initialRow == currentRow) &&  (initialCol == currentColumn))
        {
            return new TableSearcherCell(-1, -1, false);
        }
        if (!forwards)
        {
            return findCellInTableBackwards(searchString, table, currentRow,  currentColumn,matchCase, isWrapOn); 
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
                    //log.debug("tableContains() - looking at val i["+i+"] j["+j+"]");
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
                        log.debug("findCellInTable() - Found! value at Row["+i+"] Col["+j+"]");
                        isFirstSearch = true;
                        return new TableSearcherCell(i, j, true);
                    }
                }
                isStartOfRow = false;
            }
        }  
        
        if(isWrapOn)
        {
            log.debug("findCellInTable() - wrap is on, moving to start of table");
            isFirstSearch = false;
            return findCellInTable( searchString,  table,  0, 0,  matchCase,  forwards,  isWrapOn);
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
