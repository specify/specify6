/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchRowImage;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 */
public class XLSExport implements DataExport
{
    ConfigureXLS config;
    
    public XLSExport(ConfigureExternalDataIFace config)
    {
        setConfig(config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataExport#getConfig()
     */
    public ConfigureExternalDataIFace getConfig()
    {
        return config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataExport#setConfig(edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataIFace)
     */
    public void setConfig(final ConfigureExternalDataIFace config)
    {
        this.config = (ConfigureXLS)config;
    }

    protected void writeHeaders(final HSSFSheet workSheet)
    {
        String[] headers = config.getHeaders();
        HSSFRow hssfRow = workSheet.createRow(0);
        short col = 0;
        for (String head : headers)
        {
            hssfRow.createCell(col++).setCellValue(new HSSFRichTextString(head));
        }
    }

    /**
     * @param workSheet
     * writes headers for imagePath and geocoord (bg) data columns
     */
    protected void writeExtraHeaders(final HSSFSheet workSheet, int imgCols)
    {
        HSSFRow hssfRow = workSheet.getRow(0);
        short cellNum = hssfRow.getLastCellNum();
        hssfRow.createCell(++cellNum).setCellValue(new HSSFRichTextString(DataImport.GEO_DATA_HEADING));
        for (int c = 0; c < imgCols; c++)
        {
            hssfRow.createCell(++cellNum).setCellValue(new HSSFRichTextString(DataImport.IMAGE_PATH_HEADING));
        }
    }

    /**
     * @param row
     * @return HSSFCellTypes for each column in workbench.
     */
    protected int[] bldColTypes(final WorkbenchTemplate wbt)
    {
        int[] result = new int[wbt.getWorkbenchTemplateMappingItems().size()];
        for (WorkbenchTemplateMappingItem mapItem : wbt.getWorkbenchTemplateMappingItems())
        {
            result[mapItem.getViewOrder()] = getColType(mapItem); 
        }
        return result;
    }
    /**
     * @param colNum - index of a workbench column.
     * @return the excel cell type appropriate for the database field the workbench column maps to.
     */
    protected int getColType(final WorkbenchTemplateMappingItem mapItem)
    {
        Class<?> dataType = WorkbenchTask.getDataType(mapItem);
        // These are the classes currently returned by getDataType():
        // java.lang.Long
        // java.lang.String
        // java.util.Calendar
        // java.lang.Float
        // java.lang.Boolean
        // java.lang.Byte
        // java.lang.Integer
        // java.lang.Short
        // java.lang.Double
        // java.util.Date
        // java.lang.BigDecimal

        if (dataType == java.lang.Long.class
                || dataType == java.lang.Float.class
                || dataType == java.lang.Byte.class
                || dataType == java.lang.Integer.class
                || dataType == java.lang.Short.class
                || dataType == java.lang.Double.class
                || dataType == java.math.BigDecimal.class)
        {
            return HSSFCell.CELL_TYPE_NUMERIC;
        }
        else if (dataType == java.lang.Boolean.class)
        {
            // XXX still need to test if this type allows "don't know"
            return HSSFCell.CELL_TYPE_BOOLEAN;
        }
        else
        {
            return HSSFCell.CELL_TYPE_STRING;
        }
    }
    
    /**
     * calls HSSFCell.setCellValue with the java type appropriate for the cell type.
     * 
     * @param cell
     * @param value
     */
    protected void setCellValue(final HSSFCell cell, final String value)
    {
        int type = cell.getCellType();
        
        boolean valueSet = true;
        try
        {
            if (type == HSSFCell.CELL_TYPE_NUMERIC)
            {
                cell.setCellValue(Double.parseDouble(value));
            }
            // parseBoolean returns true if value == "true" (ignoring case) else false.
            // Seems like data could be lost so I am assigning the string unless value definitely
            // seems
            // to be boolean.
            // We probably should add a new cell editor specifically for "boolean" to control the
            // value.
            // But we probably shouldn't even with bother with CELL_TYPE_BOOLEAN since Specify6
            // booleans arent' true booleans anyway so
            // why bother?
            else if (type == HSSFCell.CELL_TYPE_BOOLEAN
                    && (value == "true" || value == "false"))
            {
                cell.setCellValue(Boolean.parseBoolean(value));
            }
            else
            {
                valueSet = false;
            }
        }
        catch (NumberFormatException ex)
        {
            valueSet = false;
        }
        
        if (!valueSet)
        {
            cell.setCellValue(new HSSFRichTextString(value));
        }
    }
    
    /**
     * @param wbt
     * @return DocumentSummaryInformation containing the mappings for wbt.
     * 
     * Each mapping is stored as a property, using the column heading as the key.
     */
    protected DocumentSummaryInformation writeMappings(final WorkbenchTemplate wbt)
    {
        DocumentSummaryInformation dsi = PropertySetFactory.newDocumentSummaryInformation();
        CustomProperties cps = new CustomProperties();
        for (WorkbenchTemplateMappingItem wbmi : wbt.getWorkbenchTemplateMappingItems())
        {
            cps.put(wbmi.getCaption(), wbmi.getTableName() + "\t" + wbmi.getFieldName());
        }
        dsi.setCustomProperties(cps);
        return dsi;
    }
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataExport#writeData(java.util.List)
     */
    public void writeData(final List<?> data) throws Exception
    {
        HSSFWorkbook workBook  = new HSSFWorkbook();
        HSSFSheet    workSheet = workBook.createSheet();
        DocumentSummaryInformation mappings = null;
        
        int rowNum = 0;

        if (config.getFirstRowHasHeaders() && !config.getAppendData())
        {
            writeHeaders(workSheet);
            rowNum++;
            
            String[] headers = config.getHeaders();
            for (short i=0;i<headers.length;i++)
            {
                workSheet.setColumnWidth(i, (short)(StringUtils.isNotEmpty(headers[i]) ? (256 * headers[i].length()) : 2560));
            }
            
            if (data.get(0).getClass().equals(WorkbenchTemplate.class))
            {
                mappings = writeMappings((WorkbenchTemplate)data.get(0));
            }
            else
            {
                mappings = writeMappings(((WorkbenchRow)data.get(0)).getWorkbench().getWorkbenchTemplate());
            }
        }
        
        if (data.size() > 0)
        {
            int[] disciplinees;
            if (data.get(0).getClass().equals(WorkbenchTemplate.class))
            {
                disciplinees = bldColTypes((WorkbenchTemplate) data.get(0));
                // now set up cell types and formats for a bunch of empty rows....
                
            }
            else
            {
                WorkbenchRow      wbRow     = (WorkbenchRow) data.get(0);
                Workbench         workBench = wbRow.getWorkbench();
                WorkbenchTemplate template  = workBench.getWorkbenchTemplate();
                short             numCols   = (short)template.getWorkbenchTemplateMappingItems().size();
                boolean geoDataPresent = false;
                int imgCols = 0;
                
                disciplinees = bldColTypes(template);
                for (Object rowObj : data)
                {
                    WorkbenchRow row     = (WorkbenchRow)rowObj;
                    HSSFRow      hssfRow = workSheet.createRow(rowNum++);
                    short colNum;
                    boolean rowHasGeoData = false;
                    
                    for (colNum = 0; colNum < numCols; colNum++)
                    {
                        HSSFCell cell = hssfRow.createCell(colNum);
                        cell.setCellType(disciplinees[colNum]);
                        setCellValue(cell, row.getData(colNum));
                    }
                    
                    if (row.getBioGeomancerResults() != null && !row.getBioGeomancerResults().equals(""))
                    {
                        geoDataPresent = true;
                        rowHasGeoData = true;
                        HSSFCell cell = hssfRow.createCell(colNum++);
                        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                        setCellValue(cell, row.getBioGeomancerResults());
                    }

                    //if (row.getCardImage() != null)
                    if (row.getRowImage(0) != null)
                    {
                        if (!rowHasGeoData)
                        {
                           colNum++; 
                        }
                        int imgIdx = 0;
                        WorkbenchRowImage img = row.getRowImage(imgIdx++);
                        while (img != null)
                        {
                            HSSFCell cell = hssfRow.createCell(colNum++);
                            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                            setCellValue(cell, img.getCardImageFullPath());
                            img = row.getRowImage(imgIdx++);
                        }
                        if (imgIdx-1 > imgCols)
                        {
                            imgCols = imgIdx-1;
                        }
                    }
                    
                }
                if (imgCols > 0 || geoDataPresent)
                {
                    writeExtraHeaders(workSheet, imgCols);
                }
            }
        }
        try
        {
            //write the workbook
            FileOutputStream fos = new FileOutputStream(getConfig().getFileName());
            workBook.write(fos);
            fos.close();
            
            //Now write the mappings.
            //NOT (hopefully) the best way to write the mappings, but (sadly) the easiest way. 
            //May need to do this another way if this slows performance for big wbs.
            if (mappings != null)
            {
                InputStream is = new FileInputStream(getConfig().getFileName());
                POIFSFileSystem poifs = new POIFSFileSystem(is);
                is.close();
                mappings.write(poifs.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);
                fos = new FileOutputStream(getConfig().getFileName());
                poifs.writeFilesystem(fos);
                fos.close();
            }
        } 
        catch (Exception e)
        {
            throw(e);
        }
    }
    
}
