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
    
    public TableSearcherCell cellContains(String searchString, JTable theTable, TableModel model, int curRowPos, int curColPos, boolean matchCase)
    {
        log.debug("cellContains - searchString: " + searchString + " Current row: " + curRowPos + " Current col: " + curColPos);
        boolean found = false;
        if (theTable.getValueAt(curRowPos,curColPos) != null)
          {
            String valueInTable = theTable.getValueAt(curRowPos,curColPos).toString();
            log.debug("checking to replace valueInTable: " + valueInTable.getClass());
            //printMatching( searchString,  valueInTable,  curRowPos,  curColPos,  matchCase);
            if (!matchCase)
            {
                valueInTable = valueInTable.toLowerCase();
                searchString = searchString.toLowerCase();
            }
                if (valueInTable.contains(searchString))
                {
                    log.debug("This cell constains the search value!");
                    found = true;
                    isFirstSearch = true;
                    return new TableSearcherCell(curRowPos, curColPos, found);
                }
            
          }
        log.debug("cellContains: ran into a null value");
        isFirstSearch = true;
        return new TableSearcherCell(-1, -1, found);
    }
    
    private TableSearcherCell tableContainsBackwards(String searchString, JTable theTable, TableModel model, int rowPos, int columPos, boolean matchCase, boolean isWrapOn)
    {
        log.debug("tableContainsBackwards: + searchString: " + searchString + " Current row: " + rowPos + " Current col: " + columPos);
        boolean found = false;
        int colCnt = theTable.getColumnCount();
        int rowCnt = theTable.getRowCount();

        boolean isStartOfRow = true;

        for (int i = rowPos; i > -1; i--) 
        {
            if (!isStartOfRow)
            {
                columPos = colCnt -1 ;
            }
          for(int j = columPos; j > -1; j--) 
          {
              if (theTable.getValueAt(i,j) != null)
              {
                  if ((!isFirstSearch) && (initialRow == i) &&  (initialCol == j))
                  {
                      found = false;
                      return new TableSearcherCell(-1, -1, found);
                  }
                    String valueInTable = theTable.getValueAt(i,j).toString();
                              
                    if (!matchCase)
                    {
                        valueInTable = valueInTable.toLowerCase();
                        searchString = searchString.toLowerCase();
                    }
                    printMatching( searchString,  valueInTable,  i,  j,  matchCase);
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
            return tableContainsBackwards( searchString,  theTable,  model, (rowCnt-1), (colCnt-1),  matchCase,  isWrapOn);
        }
        found = false;
        isFirstSearch = true;
        return new TableSearcherCell(-1, -1, found);
    }


    public TableSearcherCell tableContains(String search, JTable theTable, TableModel model, int rowPos, int columPos, boolean matchCase, boolean forwards, boolean isWrapOn)
    {
        log.debug("tableContains() - searchString[" + search + "] Current row[" + rowPos + "] Current col[" + columPos+"] isFirstPass["+ isFirstSearch +"]");
        log.debug("tableContains() - initialRow[" + initialRow + "] initialCol[" + initialCol +"]");
        

        if (isFirstSearch)
        {
             initialRow = rowPos;
             initialCol = columPos;     
        }
        //if it's done a full wrap search
        if ((!isFirstSearch) && (initialRow == rowPos) &&  (initialCol == columPos))
        {
            return new TableSearcherCell(-1, -1, false);
        }
        if (!forwards)
        {
            return tableContainsBackwards(search, theTable, model,  rowPos,  columPos,matchCase, isWrapOn); 
        } 
       
        int colCnt = theTable.getColumnCount();
        int rowCnt = theTable.getRowCount();
        boolean isStartOfRow = true;

        for (int i = rowPos; i < rowCnt; i++)
        {
            if (!isStartOfRow)
            {
                columPos = 0;
            }
            for (int j = columPos; j < colCnt; j++)
            {
                if (theTable.getValueAt(i, j) != null)
                {
                    //log.debug("tableContains() - looking at val i["+i+"] j["+j+"]");
                    if ((!isFirstSearch) && (initialRow == i) && (initialCol == j))
                    {
                        return new TableSearcherCell(-1, -1, false);
                    }
                    String valueInTable = theTable.getValueAt(i, j).toString();

                    if (!matchCase)
                    {
                        valueInTable = valueInTable.toLowerCase();
                        search = search.toLowerCase();
                    }
                    printMatching(search, valueInTable, i, j, matchCase);
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
            return tableContains( search,  theTable,  model, 0, 0,  matchCase,  forwards,  isWrapOn);
        }
        isFirstSearch = true;
        return new TableSearcherCell(-1, -1, false);
    }
    
    private void printMatching(String searchString, String valueInTable, int row, int col, boolean matchCase)
    {
//        log.debug("---------------------");
//        log.debug("Matchcase: "+matchCase);
//        log.debug("rowPos: "+row );
//        log.debug("columPos: "+col);
//        log.debug("Search :"+searchString);
//        log.debug("String :"+valueInTable);
//        log.debug("---------------------");
           
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
