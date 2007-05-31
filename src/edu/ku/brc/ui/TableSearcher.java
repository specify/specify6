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
     * 
     */
    public TableSearcher()
    {
        // TODO Auto-generated constructor stub
    }
    
    public TableSearcherCell cellContains(String search, JTable table, int row, int column, boolean matchCase)
    {
        log.debug("cellContains - searchString: " + search + " Current row: " + row + " Current col: " + column);
        boolean found = false;
        if (table.getValueAt(row,column) != null)
          {
            String valueInTable = table.getValueAt(row,column).toString();
            log.debug("checking to replace valueInTable: " + valueInTable.getClass());
            //printMatching( searchString,  valueInTable,  curRowPos,  curColPos,  matchCase);
            if (!matchCase)
            {
                valueInTable = valueInTable.toLowerCase();
                search = search.toLowerCase();
            }
                if (valueInTable.contains(search))
                {
                    log.debug("This cell constains the search value!");
                    found = true;
                    isFirstSearch = true;
                    return new TableSearcherCell(row, column, found);
                }
            
          }
        log.debug("cellContains: ran into a null value");
        isFirstSearch = true;
        return new TableSearcherCell(-1, -1, found);
    }
    
    private TableSearcherCell findCellInTableBackwards(String search, JTable table,  int row, int column, boolean matchCase, boolean isWrapOn)
    {
        log.debug("tableContainsBackwards: + searchString: " + search + " Current row: " + row + " Current col: " + column);
        boolean found = false;
        int colCnt = table.getColumnCount();
        int rowCnt = table.getRowCount();

        boolean isStartOfRow = true;

        for (int i = row; i > -1; i--) 
        {
            if (!isStartOfRow)
            {
                column = colCnt -1 ;
            }
          for(int j = column; j > -1; j--) 
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
                        search = search.toLowerCase();
                    }
                    if (valueInTable.contains(search))
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
            return findCellInTableBackwards( search,  table,  (rowCnt-1), (colCnt-1),  matchCase,  isWrapOn);
        }
        found = false;
        isFirstSearch = true;
        return new TableSearcherCell(-1, -1, found);
    }


    public TableSearcherCell findCellInTable(String search, JTable table, int row, int column, boolean matchCase, boolean forwards, boolean isWrapOn)
    {
        log.debug("findCellInTable() - searchString[" + search + "] Current row[" + row + "] Current col[" + column+"] isFirstPass["+ isFirstSearch +"]");
        log.debug("findCellInTable() - initialRow[" + initialRow + "] initialCol[" + initialCol +"]");
        

        if (isFirstSearch)
        {
             initialRow = row;
             initialCol = column;     
        }
        //if it's done a full wrap search
        if ((!isFirstSearch) && (initialRow == row) &&  (initialCol == column))
        {
            return new TableSearcherCell(-1, -1, false);
        }
        if (!forwards)
        {
            return findCellInTableBackwards(search, table, row,  column,matchCase, isWrapOn); 
        } 
       
        int colCnt = table.getColumnCount();
        int rowCnt = table.getRowCount();
        boolean isStartOfRow = true;

        for (int i = row; i < rowCnt; i++)
        {
            if (!isStartOfRow)
            {
                column = 0;
            }
            for (int j = column; j < colCnt; j++)
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
                        search = search.toLowerCase();
                    }
                    if (valueInTable.contains(search))
                    {
                        log.debug("tableContains() - Found! value at Row["+i+"] Col["+j+"]");
                        isFirstSearch = true;
                        return new TableSearcherCell(i, j, true);
                    }
                }
                isStartOfRow = false;
            }
        }  
        
        if(isWrapOn)
        {
            log.debug("tableContains() - wrap is on, moving to start of table");
            isFirstSearch = false;
            return findCellInTable( search,  table,  0, 0,  matchCase,  forwards,  isWrapOn);
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
