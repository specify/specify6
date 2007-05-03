/**
 * 
 */
package edu.ku.brc.ui;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * @author megkumin
 *
 */
public class ArraySearcher
{

	int ROWDIM = 50;
	/**
	 * 
	 */
	public ArraySearcher()
	{
		// TODO Auto-generated constructor stub
	}
    
    public ASearchableCell cellContains(String searchString, JTable theTable, TableModel model, int curRowPos, int curColPos, boolean matchCase)
    {
        System.out.println("cellContains - searchString: " + searchString + " Current row: " + curRowPos + " Current col: " + curColPos);
        boolean found = false;
        if (theTable.getValueAt(curRowPos,curColPos) != null)
          {
            String valueInTable = theTable.getValueAt(curRowPos,curColPos).toString();
            System.out.println("checking to replace valueInTable: " + valueInTable.getClass());
            //printMatching( searchString,  valueInTable,  curRowPos,  curColPos,  matchCase);
            if (!matchCase)
            {
                valueInTable = valueInTable.toLowerCase();
                searchString = searchString.toLowerCase();
            }
                if (valueInTable.contains(searchString))
                {
                    System.out.println("This cell constains the search value!");
                    found = true;
                    return new ASearchableCell(curRowPos, curColPos, found);
                }
            
          }
        System.out.println("cellContains: ran into a null value");
        return new ASearchableCell(-1, -1, found);
    }
    
    public ASearchableCell tableContainsBackwards(String searchString, JTable theTable, TableModel model, int rowPos, int columPos, boolean matchCase)
    {
        System.out.println("tableContainsBackwards: + searchString: " + searchString + " Current row: " + rowPos + " Current col: " + columPos);
        boolean found = false;
        int colCnt = theTable.getColumnCount();
        int rowCnt = theTable.getRowCount();

        boolean firstRun = true;

        for (int i = rowPos; i > -1; i--) 
        {
            if (!firstRun)
            {
                columPos = colCnt -1 ;
            }
          for(int j = columPos; j > -1; j--) 
          {
              if (theTable.getValueAt(i,j) != null)
              {
                    String valueInTable = theTable.getValueAt(i,j).toString();
                              
                    if (!matchCase)
                    {
                        valueInTable = valueInTable.toLowerCase();
                        searchString = searchString.toLowerCase();
                    }
                    printMatching( searchString,  valueInTable,  i,  j,  matchCase);
                    if (valueInTable.contains(searchString))
                    {
                        System.out.println("Found!");
                        found = true;
                        return new ASearchableCell(i, j, found);
                    }
              }
              firstRun = false;
          }
        }  
        found = false;
        return new ASearchableCell(-1, -1, found);
    }


	public ASearchableCell tableContains(String search, JTable theTable, TableModel model, int rowPos, int columPos, boolean matchCase, boolean forwards)
	{
        if (!forwards)
        {
            return tableContainsBackwards(search, theTable, model,  rowPos,  columPos,matchCase); 
        }

        System.out.println("tableContains: + searchString: " + search + " Current row: " + rowPos + " Current col: " + columPos);
        boolean found = false;
		int colCnt = theTable.getColumnCount();
		int rowCnt = theTable.getRowCount();
        boolean firstRun = true;

		for (int i = rowPos; i < rowCnt; i++) 
		{
          if (!firstRun)columPos = 0;
		  for(int j = columPos; j < colCnt; j++) 
		  {
			  if (theTable.getValueAt(i,j) != null)
			  {
				  	String valueInTable = theTable.getValueAt(i,j).toString();
                              
				  	if (!matchCase)
				  	{
				  		valueInTable = valueInTable.toLowerCase();
				  		search = search.toLowerCase();
				  	}
                    printMatching( search,  valueInTable,  i,  j,  matchCase);
                    if (valueInTable.contains(search))
				  	{
				  		System.out.println("Found!");
						found = true;
						return new ASearchableCell(i, j, found);
				  	}
			  }
              firstRun = false;
		  }
		}  
		found = false;
		return new ASearchableCell(-1, -1, found);
	}
    
    private void printMatching(String searchString, String valueInTable, int row, int col, boolean matchCase)
    {
        System.out.println("---------------------");
        System.out.println("Matchcase: "+matchCase);
        System.out.println("rowPos: "+row );
        System.out.println("columPos: "+col);
        System.out.println("Search :"+searchString);
        System.out.println("String :"+valueInTable);
        System.out.println("---------------------");
           
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
