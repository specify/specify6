/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.DateWrapper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 20, 2007
 *
 */
public class DataFileInfo
{

    protected File               inputFile;
    protected int                numRows     = 0;
    protected int                numCols     = 0;
    protected Vector<ColumnInfo> colInfo     = null;
    
    /**
     * @param inputFile
     */
    public DataFileInfo(final File inputFile)
    {
        super();
        this.inputFile = inputFile;
        
        readFile();
    }
    
    protected void readFile()
    {
        String path = inputFile.getAbsolutePath().toLowerCase();
        if (path.endsWith("xls"))
        {
            readXLS(true);
            
        } else  // if not XLS then assume it is a CSV file 
        {
            
        } 
    }
    
    /**
     * Imports an Excel File.
     */
    protected void readXLS(final boolean firstRowHasNames)
    {
        try
        {
            InputStream     input    = new FileInputStream(inputFile);
            POIFSFileSystem fs       = new POIFSFileSystem(input);
            HSSFWorkbook    workBook = new HSSFWorkbook(fs);
            HSSFSheet       sheet    = workBook.getSheetAt(0);

            // Calculate the number of rows and columns
            colInfo = new Vector<ColumnInfo>(16);
            
            Hashtable<Integer, Boolean> colTracker = new Hashtable<Integer, Boolean>();

            
            boolean firstRow = true;
            int     col      = 0; 
            colTracker.clear();
            
            // Iterate over each row in the sheet
            Iterator rows = sheet.rowIterator();
            while (rows.hasNext())
            {
                HSSFRow row = (HSSFRow)rows.next();
                
                //System.out.println("Row #" + row.getRowNum());

                if (firstRow || numRows == 1)
                {
                    // Iterate over each cell in the row and print out the cell's content
                    Iterator cells = row.cellIterator();
                    while (cells.hasNext())
                    {
                        HSSFCell cell = (HSSFCell)cells.next();
                        //System.out.println("Cell #" + cell.getCellNum());
                        
                        String  value   = null;
                        boolean skip    = false;
                        int     type    = cell.getCellType();
                        int     cellNum = cell.getCellNum();
                        
                        switch (cell.getCellType())
                        {
                            case HSSFCell.CELL_TYPE_NUMERIC:
                                double numeric = cell.getNumericCellValue();
                                value = Double.toString(numeric);
                                break;
                                
                            case HSSFCell.CELL_TYPE_STRING:
                                value = cell.getStringCellValue();
                                break;
                                
                            case HSSFCell.CELL_TYPE_BLANK:
                                value = "";
                                type = HSSFCell.CELL_TYPE_STRING;
                                break;
                                
                            case HSSFCell.CELL_TYPE_BOOLEAN:
                                boolean bool = cell.getBooleanCellValue();
                                value = Boolean.toString(bool);
                                //type = HSSFCell.CELL_TYPE_STRING;
                                break;
                                
                            default:
                                //System.out.println("unsuported cell type");
                                skip = true;
                                break;
                        }
                        
                        if (numRows == 1)
                        {
                            colInfo.get(cellNum).setData(value);
                            col++;
                            
                        } else if (!skip)
                        {
                            //System.out.println("Cell #" + cellNum + " " + type+"  "+value);
                            if (firstRowHasNames)
                            {
                                colInfo.add(new ColumnInfo(cellNum, type, value, null));
                                colTracker.put(cellNum, true);
                                
                            } else
                            {
                                colInfo.add(new ColumnInfo(cellNum, type, null, value));    
                            }
                            numCols++;
                        }
                    }
                    firstRow = false;
                }
                numRows++;
            }
            
            //System.out.println("Rows["+numRows+"]  Cols["+numCols+"]");

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        Collections.sort(colInfo);
    }
    
    /**
     * Load the contents into a workbench.
     * @param workbench the workbench to have adata added to it.
     */
    public void loadData(final Workbench workbench)
    {
        String path = inputFile.getAbsolutePath().toLowerCase();
        if (path.endsWith("xls"))
        {
            loadDataFromXLS(workbench, true);
            
        } else  // if not XLS then assume it is a CSV file 
        {
            
        } 
    }
    
    /**
     * Load the data from an Excel spreadsheet.
     * @param workbench the workbench to be added to
     * @param firstRowHasNames whether the first row should be skipped
     */
    protected void loadDataFromXLS(Workbench workbench, final boolean firstRowHasNames)
    {
        DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
        try
        {
            InputStream     input    = new FileInputStream(inputFile);
            POIFSFileSystem fs       = new POIFSFileSystem(input);
            HSSFWorkbook    workBook = new HSSFWorkbook(fs);
            HSSFSheet       sheet    = workBook.getSheetAt(0);

            // Calculate the number of rows and columns
            colInfo = new Vector<ColumnInfo>(16);
            
            Set<WorkbenchTemplateMappingItem>    wbtmiSet  = workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems();
            Vector<WorkbenchTemplateMappingItem> wbtmiList = new Vector<WorkbenchTemplateMappingItem>();
            wbtmiList.addAll(wbtmiSet);
            Collections.sort(wbtmiList);
            
            Hashtable<Integer, Boolean> colTracker = new Hashtable<Integer, Boolean>();
            
            boolean firstRow = true;
            // Iterate over each row in the sheet
            Iterator rows = sheet.rowIterator();
            while (rows.hasNext())
            {
                HSSFRow row = (HSSFRow)rows.next();
                
                //System.out.println("Row #" + row.getRowNum());

                if (firstRow)
                {
                    firstRow = false;
                    continue;
                }
                
                colTracker.clear();
                int col = 0; 
                // Iterate over each cell in the row and print out the cell's content
                Iterator cells = row.cellIterator();
                while (cells.hasNext())
                {
                    HSSFCell cell = (HSSFCell)cells.next();
                    int      type = cell.getCellType();
                    String   value = "";
                    boolean skip = false;
                    
                    int cellNum = cell.getCellNum();
                    
                    WorkbenchTemplateMappingItem wbtmi   = wbtmiList.get(cellNum);
                    //String                       typeStr = wbtmi.getDataType();
                    
                    //System.out.println(wbtmiList.get(cellNum).getDataType());
                    
                    switch (type)
                    {
                        case HSSFCell.CELL_TYPE_NUMERIC:
                            if (wbtmi.getDataType().indexOf("date") > -1)
                            {
                                value = scrDateFormat.getSimpleDateFormat().format(cell.getDateCellValue());
                                
                            } else if (wbtmi.getDataType().indexOf("int") > -1)
                            {
                                double numeric = cell.getNumericCellValue();
                                value = Integer.toString((int)numeric);
                                
                            } else
                            {
                                double numeric = cell.getNumericCellValue();
                                value = Double.toString(numeric);
                            }
                            break;
                            
                        case HSSFCell.CELL_TYPE_STRING:
                            value = cell.getStringCellValue();
                            break;
                            
                        case HSSFCell.CELL_TYPE_BLANK:
                            value = "";
                            type = HSSFCell.CELL_TYPE_STRING;
                            break;
                            
                        case HSSFCell.CELL_TYPE_BOOLEAN:
                            boolean bool = cell.getBooleanCellValue();
                            value = Boolean.toString(bool);
                            //type = HSSFCell.CELL_TYPE_STRING;
                            break;
                            
                        default:
                            //System.out.println("unsuported cell type");
                            skip = true;
                            break;
                    }
                    
                    if (!skip)
                    {
                        //System.out.println("DATA Cell #" + cellNum + " " + type+"  "+value);
                        WorkbenchDataItem wbdi = new WorkbenchDataItem();
                        wbdi.initialize();
                        wbdi.setCellData(value);
                        wbdi.setColumnNumber(cellNum);
                        wbdi.setRowNumber(numRows);
                        workbench.addWorkbenchDataItem(wbdi);
                        colTracker.put(cellNum, true);
                        col++;
                    }
                }
                
                for (int i=0;i<wbtmiList.size();i++)
                {
                    if (colTracker.get(i) == null)
                    {
                        WorkbenchDataItem wbdi = new WorkbenchDataItem();
                        wbdi.initialize();
                        wbdi.setCellData("");
                        wbdi.setColumnNumber(i);
                        wbdi.setRowNumber(numRows);
                        workbench.addWorkbenchDataItem(wbdi);
                    }
                }
                numRows++;
            }

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }


    public Vector<ColumnInfo> getColInfo()
    {
        return colInfo;
    }

    public int getNumCols()
    {
        return numCols;
    }

    public int getNumRows()
    {
        return numRows;
    }
    
    //---------------------------------------------------------------------------
    //--
    //---------------------------------------------------------------------------
    public class ColumnInfo  implements Comparable<ColumnInfo>
    {
        protected Integer colInx;
        protected int     colType;
        protected String  colName;
        protected String  data;
        
        public ColumnInfo(int colInx, int colType, String colName, String data)
        {
            super();
            this.colInx  = colInx;
            this.colType = colType;
            this.colName = colName;
            this.data    = data;
        }

        public String getColName()
        {
            return colName;
        }

        public int getColType()
        {
            return colType;
        }

        public String getData()
        {
            return data;
        }

        public void setData(String data)
        {
            this.data = data;
        }

        public void setColName(String colName)
        {
            this.colName = colName;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(ColumnInfo obj)
        {
            return colInx.compareTo(obj.colInx);
        }
        
    }

    
}
