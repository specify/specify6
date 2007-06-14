/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.ui.DateWrapper;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Imports xls data to workbenches.
 */
public class XLSImport extends DataImport implements DataImportIFace
{
    private static final Logger log = Logger.getLogger(XLSImport.class);
    private short cardImageCol = -1;
    private short geoCol = -1;
    protected ConfigureExternalDataIFace config;
    
    private void getSystemCols(final HSSFRow headerRow)
    {
        for (short c = headerRow.getFirstCellNum(); c <= headerRow.getLastCellNum(); c++)
        {
            HSSFCell cell = headerRow.getCell(c);
            if (cell != null)
            {
                String header = cell.getStringCellValue();
                if (header != null)
                {
                    if (header.equals(IMAGE_PATH_HEADING))
                    {
                        cardImageCol = c;
                    }
                    if (header.equals(GEO_DATA_HEADING))
                    {
                        geoCol = c;
                    }
                }
                
            }
        }
    }
     
     /**
     * Constrcutor.
     * @param config the configuration
     */
    public XLSImport(final ConfigureExternalDataIFace config)
    {
        this.config = config;
    }

    /* (non-Javadoc)
     * Loads data from the file configured by the config member into a workbench.
     * @param workbench - the workbench to be loaded
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataImportIFace#getData(edu.ku.brc.specify.datamodel.Workbench)
     */
    public DataImportIFace.Status getData(final Workbench workbench)
    {
        if (config.getStatus() == ConfigureExternalDataIFace.Status.Valid)
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
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(0);
                nf.setGroupingUsed(false); //gets rid of commas
                
                wbtmiList.addAll(wbtmiSet);
                
                Collections.sort(wbtmiList);
                
                this.truncations.clear();
    
                // Iterate over each row in the sheet
                Iterator<?> rows = sheet.rowIterator();
                while (rows.hasNext())
                {
                    HSSFRow row = (HSSFRow) rows.next();
    
                    if (numRows == 0 && config.getFirstRowHasHeaders())
                    {
                        numRows++;
                        getSystemCols(row);
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
                            {
                                if (HSSFDateUtil.isCellDateFormatted(cell))
                                {
                                    value = scrDateFormat.getSimpleDateFormat().format(cell.getDateCellValue());
                                    
                                } else
                                {
                                    Class classObj = WorkbenchTask.getDataType(wbtmi);
                                    if (classObj.equals(Calendar.class) || classObj.equals(Date.class))
                                    {
                                        value = scrDateFormat.getSimpleDateFormat().format(cell.getDateCellValue());
        
                                    } else if (classObj.equals(Integer.class))
                                    {
                                        double numeric = cell.getNumericCellValue();
                                        value = Integer.toString((int) numeric);
        
                                    } else
                                    {
                                        double numeric = cell.getNumericCellValue();
                                        value = nf.format(numeric);
                                     }
                                }
                                break;
                            }
    
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
                                break;
    
                            default:
                                skip = true;
                                break;
                        }
    
                        if (!skip)
                        {
                            wbRow.setData(truncateIfNecessary(value, numRows, wbtmi.getViewOrder(), wbtmi.getCaption()), wbtmi.getViewOrder());
                        }
                    }
                    addImageInfo(row, wbRow);
                    addGeoInfo(row, wbRow);
                    numRows++;
                }
                return status = this.truncations.size() == 0 ? DataImportIFace.Status.Valid : DataImportIFace.Status.Modified;
            } catch (IOException ex)
            {
                log.error(ex);
            }
        }
        return status = DataImportIFace.Status.Error;
    }

    private void addImageInfo(final HSSFRow row, final WorkbenchRow wbRow)
    {
        if (cardImageCol != -1)
        {
           HSSFCell c = row.getCell(cardImageCol);
           if (c != null)
           {
               String imagePath = c.getStringCellValue();
               if (imagePath != null)
               {
                    String[] paths = imagePath.split("; ");
                    for (String path : paths)
                    {
                        try
                        {
                            wbRow.addImage(new File(path));
                        }
                        catch (IOException e)
                        {
                            System.out.println("OH NO!!!!!");
                        }
                    }
                }
           }
        }
    }
    
    public void addGeoInfo(final HSSFRow row, final WorkbenchRow wbRow)
    {
        if (geoCol != -1)
        {
            HSSFCell c = row.getCell(geoCol);
            if (c != null)
            {
                String geoData = c.getStringCellValue();
                if (geoData != null)
                {
                    wbRow.setBioGeomancerResults(geoData);
                }
            }
        }
    }
    
    public void setConfig(final ConfigureExternalDataIFace config)
    {
        this.config = config;
    }

    public ConfigureExternalDataIFace getConfig()
    {
        return this.config;
    }

}
