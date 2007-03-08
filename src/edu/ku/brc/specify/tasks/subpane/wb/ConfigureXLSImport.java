/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Configures xls file for import to a workbench. Currently only property that is configured is the header list.
 *If first row does not contain headers, they are simply assigned "Column1", "Column2" etc.
 */
public class ConfigureXLSImport extends ConfigureImportBase implements ConfigureDataImport
{
    protected int numRows = 0;
    protected int numCols = 0;

    public ConfigureXLSImport(File file)
    {
        super();
        getConfig(file);
    }

    protected void interactiveConfig()
    {
        firstRowHasHeaders = determineFirstRowHasHeaders();
        nonInteractiveConfig();
    }

    /* (non-Javadoc)
     * Sets up colInfo for inputFile.
     * @see edu.ku.brc.specify.tasks.subpane.wb.ConfigureDataImport#getConfig(java.lang.String)
     */
    protected void nonInteractiveConfig()
    {
        try
        {
            InputStream input = new FileInputStream(inputFile);
            POIFSFileSystem fs = new POIFSFileSystem(input);
            HSSFWorkbook workBook = new HSSFWorkbook(fs);
            HSSFSheet sheet = workBook.getSheetAt(0);

            // Calculate the number of rows and columns
            colInfo = new Vector<ImportColumnInfo>(16);

            Hashtable<Integer, Boolean> colTracker = new Hashtable<Integer, Boolean>();

            boolean firstRow = true;
            int col = 0;
            colTracker.clear();

            // Iterate over each row in the sheet
            Iterator rows = sheet.rowIterator();
            while (rows.hasNext())
            {
                HSSFRow row = (HSSFRow) rows.next();

                //System.out.println("Row #" + row.getRowNum());

                if (firstRow || numRows == 1)
                {
                    // Iterate over each cell in the row and print out the cell's content
                    Iterator cells = row.cellIterator();
                    while (cells.hasNext())
                    {
                        HSSFCell cell = (HSSFCell) cells.next();
                        //System.out.println("Cell #" + cell.getCellNum());

                        String value = null;
                        boolean skip = false;
                        int type = cell.getCellType();
                        int cellNum = cell.getCellNum();

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
                            if (firstRowHasHeaders)
                            {
                                colInfo.add(new ImportColumnInfo(cellNum, type, value, null));
                                colTracker.put(cellNum, true);

                            } else
                            {
                                //colInfo.add(new ImportColumnInfo(cellNum, type, null, value));
                                colInfo.add(new ImportColumnInfo(cellNum, type, "column"
                                        + String.valueOf(cellNum + 1), null));
                                colTracker.put(cellNum, true);

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
}
