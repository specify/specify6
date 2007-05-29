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
	private int ROWDIM = 50;
    private int initialRow = -1;
    private int initialCol = -1;
    private boolean isFirstPassOfSearch = true;
    private static final Logger    log                     = Logger.getLogger(SearchReplacePanel.class);
	
    /**
     * Constructor. 
     */
	public TableSearcher()
	{
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
                                          final int row, final int col, final boolean matchCase)
    {        
        log.debug("cellContains - searchString: " + search + " Current row: " + row+ " Current col: " + col);        
        if (table.getValueAt(row, col) != null)
        {
            String searchString = search;
            String valueInTable = table.getValueAt(row, col).toString();
            if (!matchCase)
            {
                valueInTable = valueInTable.toLowerCase();
                searchString = searchString.toLowerCase();
            }
            if (valueInTable.contains(searchString))
            {
                log.debug("cellContains - Found value: at Row["+row+"] Col["+col+"]");
                return new TableSearcherCell(row, col, true);
            }
        }
        return new TableSearcherCell(-1, -1, false);
    }
    
    /**
     * @param search the string to be searched for
     * @param table the table in which to perform the search
     * @param row the current row selected in the table
     * @param column the current column selected in teh table
     * @param matchCase whether or not the user's selected match case on the search
     * @param isWrapOn whether or not the user's selected wrap search for the search
     * @return TableSearcherCell contains information as whether a match was found and at what row and column
     */
    private TableSearcherCell findCellInTableBackwards(final String search, final JTable table, 
                                                       final int row, final int column, 
                                                       final boolean matchCase, final boolean isWrapOn)
    {
        log.debug("tableContainsBackwards - searchString: " + search + " Current row: " + row + " Current col: " + column);
        String searchString = search;
        int currentRow = row;
        int currentColumn = column;
        
        int colCnt = table.getColumnCount();
        int rowCnt = table.getRowCount();

        boolean isStartOfRow = true;

        for (int i = currentRow; i > -1; i--)
        {
            if (!isStartOfRow)
            {
                currentColumn = colCnt - 1;
            }
            for (int j = currentColumn; j > -1; j--)
            {
                if (table.getValueAt(i, j) != null)
                {
                    if ((!isFirstPassOfSearch) && (initialRow == i) && (initialCol == j)) 
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
                        log.debug("tableContainsBackwards - Found value: at Row[" + i + "] Col["+ j + "]");
                        isFirstPassOfSearch = true;
                        return new TableSearcherCell(i, j, true);
                    }
                }
                isStartOfRow = false;
            }
        }  
       
        if (isWrapOn)
        {
            isFirstPassOfSearch = false;
            return findCellInTableBackwards( searchString,  table, (rowCnt-1), (colCnt-1),  matchCase,  isWrapOn);
        }
        isFirstPassOfSearch = true;
        return new TableSearcherCell(-1, -1, false);
    }

    /**
     * @param search the string to be searched for
     * @param table the table in which to perform the search
     * @param row the current row selected in the table
     * @param column the current column selected in teh table
     * @param matchCase whether or not the user's selected match case on the search
     * @param forwards true if the search is a fowards search, false if backwards
     * @param isWrapOn whether or not the user's selected wrap search for the search
     * @return TableSearcherCell contains information as whether a match was found and at what row and column
     */
	public TableSearcherCell findCellInTable(final String search, final JTable table, final int row, final int column, 
                                             final boolean matchCase, final boolean forwards, final boolean isWrapOn)
	{
        log.debug("tableContains - searchString[" + search + "] Current row[" + row + "] Current col[" + column+"] isFirstPass["+ isFirstPassOfSearch +"]");
        log.debug("tableContains - initialRow[" + initialRow + "] initialCol[" + initialCol +"]");
        String searchString = search;
        int curSearchRow = row;
        int curSearchCol = column;
        
        if (isFirstPassOfSearch)
        {
             initialRow = row;
             initialCol = column;     
        }
        
        //if it's done a full wrap search
        if ((!isFirstPassOfSearch) && (initialRow == curSearchRow) &&  (initialCol == curSearchCol))
        {
            return new TableSearcherCell(-1, -1, false);
        }
        
        if (!forwards)
        {
            return findCellInTableBackwards(search, table,curSearchRow,  curSearchCol,matchCase, isWrapOn); 
        } 
        
        boolean isStartOfRow = true;

		for (int i = curSearchRow; i < table.getRowCount(); i++)
		{
			if (!isStartOfRow)
			{
                curSearchCol = 0;
			}
            
			for (int j = curSearchCol; j < table.getColumnCount(); j++)
			{
				if (table.getValueAt(i, j) != null)
				{
					if ((!isFirstPassOfSearch) && (initialRow == i) && (initialCol == j))
					{
						return new TableSearcherCell(-1, -1, false);
					}
					
                    String valueInTable = table.getValueAt(i, j).toString();
					if (!matchCase)
					{
						valueInTable = valueInTable.toLowerCase();
                        searchString = search.toLowerCase();
					}					
                    if (valueInTable.contains(search))
					{
						log.debug("tableContains - Found value: at Row["+i+"] Col["+j+"]");
						isFirstPassOfSearch = true;
						return new TableSearcherCell(i, j, true);
					}
				}
				isStartOfRow = false;
			}
		}  
		
        if (isWrapOn)
        {
        	log.debug("tableContains - wrap is on, moving to start of table");
            isFirstPassOfSearch = false;
            return findCellInTable( search,  table, 0, 0,  matchCase,  forwards,  isWrapOn);
        }
        isFirstPassOfSearch = true;
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
