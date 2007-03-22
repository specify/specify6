/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.DateWrapper;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Imports xls data to workbenches.
 */
public class XLSImport implements DataImport
{
    private static final Logger log = Logger.getLogger(XLSImport.class);
    
    protected ConfigureXLS config;
    
    /**
     * Constrcutor.
     * @param config the cvonfiguration
     */
    public XLSImport(final ConfigureExternalData config)
    {
        setConfig(config);
    }

    /* (non-Javadoc)
     * Loads data from the file configured by the config member into a workbench.
     * @param workbench - the workbench to be loaded
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataImport#getData(edu.ku.brc.specify.datamodel.Workbench)
     */
    public void getData(final Workbench workbench)
    {
        DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
        try
        {
            InputStream     input    = new FileInputStream(config.getFile());
            POIFSFileSystem fs       = new POIFSFileSystem(input);
            HSSFWorkbook    workBook = new HSSFWorkbook(fs);
            HSSFSheet       sheet    = workBook.getSheetAt(0);
            int             numRows  = 0;

            // Calculate the number of rows and columns

            Set<WorkbenchTemplateMappingItem>    wbtmiSet  = workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems();
            Vector<WorkbenchTemplateMappingItem> wbtmiList = new Vector<WorkbenchTemplateMappingItem>();
            wbtmiList.addAll(wbtmiSet);
            Collections.sort(wbtmiList);

            // Iterate over each row in the sheet
            Iterator rows = sheet.rowIterator();
            while (rows.hasNext())
            {
                HSSFRow row = (HSSFRow) rows.next();

                //System.out.println("Row #" + row.getRowNum());

                if (numRows == 0 && config.getFirstRowHasHeaders())
                {
                    numRows++;
                    continue;
                }

                WorkbenchRow wbRow = workbench.addRow();

                for (WorkbenchTemplateMappingItem wbtmi : wbtmiList)
                {
                    short cellNum = wbtmi.getOrigImportColumnIndex().shortValue();
                    if (cellNum == -1)
                    {
                        continue;
                    }
                    HSSFCell cell    = row.getCell(cellNum);
                    if (cell == null)
                    {
                        continue;
                    }
                    int      type    = cell.getCellType();
                    String   value   = "";
                    boolean  skip    = false;

                    switch (type)
                    {
                        case HSSFCell.CELL_TYPE_NUMERIC:
                            if (wbtmi.getDataType().indexOf("date") > -1)
                            {
                                value = scrDateFormat.getSimpleDateFormat().format(
                                        cell.getDateCellValue());

                            } else if (wbtmi.getDataType().indexOf("int") > -1)
                            {
                                double numeric = cell.getNumericCellValue();
                                value = Integer.toString((int) numeric);

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
                        wbRow.setData(value, wbtmi.getViewOrder());
                    }
                }
                numRows++;
            }

        } catch (IOException ex)
        {
            log.error(ex);
        }
    }

    public void setConfig(final ConfigureExternalData config)
    {
        this.config = (ConfigureXLS) config;
    }

    public ConfigureExternalData getConfig()
    {
        return this.config;
    }

}
