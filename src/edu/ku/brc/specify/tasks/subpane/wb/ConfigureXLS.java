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
import java.util.Properties;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.csvreader.CsvReader;

import edu.ku.brc.specify.exporters.ExportFileConfigurationFactory;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Configures xls file for import to a workbench. Currently only property that is configured is the header list.
 *If first row does not contain headers, they are simply assigned "Column1", "Column2" etc.
 */
public class ConfigureXLS extends ConfigureExternalDataBase implements ConfigureExternalData
{
    protected int numRows = 0;
    protected int numCols = 0;

    public ConfigureXLS(File file)
    {
        super();
        getConfig(file);
    }

    public ConfigureXLS(Properties props)
    {
        super(props);
    }
    
    @Override
    protected void interactiveConfig()
    {
        firstRowHasHeaders = determineFirstRowHasHeaders();
        nonInteractiveConfig();
    }

    /* (non-Javadoc)
     * Sets up colInfo for externalFile.
     * @see edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalData#getConfig(java.lang.String)
     */
    @Override
    protected void nonInteractiveConfig()
    {
        try
        {
            InputStream     input    = new FileInputStream(externalFile);
            POIFSFileSystem fs       = new POIFSFileSystem(input);
            HSSFWorkbook    workBook = new HSSFWorkbook(fs);
            HSSFSheet       sheet    = workBook.getSheetAt(0);

            // Calculate the number of rows and columns
            colInfo = new Vector<ImportColumnInfo>(16);

            Hashtable<Integer, Boolean> colTracker = new Hashtable<Integer, Boolean>();

            boolean firstRow = true;
            int     col      = 0;
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
                        ImportColumnInfo.ColumnType colType = ImportColumnInfo.ColumnType.Integer;
                        String  value   = null;
                        boolean skip    = false;
                        int     cellNum = cell.getCellNum();

                        switch (cell.getCellType())
                        {
                            case HSSFCell.CELL_TYPE_NUMERIC:
                                double numeric = cell.getNumericCellValue();
                                value   = Double.toString(numeric);
                                colType = ImportColumnInfo.ColumnType.Double;
                                break;

                            case HSSFCell.CELL_TYPE_STRING:
                                value   = cell.getStringCellValue();
                                colType = ImportColumnInfo.ColumnType.String;
                                break;

                            case HSSFCell.CELL_TYPE_BLANK:
                                value = "";
                                colType = ImportColumnInfo.ColumnType.String;
                                break;

                            case HSSFCell.CELL_TYPE_BOOLEAN:
                                boolean bool = cell.getBooleanCellValue();
                                value   = Boolean.toString(bool);
                                colType = ImportColumnInfo.ColumnType.Boolean;
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
                                colInfo.add(new ImportColumnInfo(cellNum, colType, value, null));
                                colTracker.put(cellNum, true);

                            } else
                            {
                                //colInfo.add(new ImportColumnInfo(cellNum, type, null, value));
                                colInfo.add(new ImportColumnInfo(cellNum, colType, "column"
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
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataBase#getProperties()
     */
    @Override
    public Properties getProperties()
    {
        Properties result = super.getProperties();
        result.setProperty("mimetype", ExportFileConfigurationFactory.XLS_MIME_TYPE);
       
        return result;
    }
}
