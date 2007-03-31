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

import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
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
    public void setConfig(ConfigureExternalDataIFace config)
    {
        this.config = (ConfigureXLS)config;
    }

    protected void writeHeaders(HSSFSheet workSheet)
    {
        String[] headers = config.getHeaders();
        HSSFRow hssfRow = workSheet.createRow(0);
        short col = 0;
        for (String head : headers)
        {
            hssfRow.createCell(col++).setCellValue(head);
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
            //XXX still need to test if this type allows "don't know"
            return HSSFCell.CELL_TYPE_BOOLEAN;
        }
        else
        {
            return HSSFCell.CELL_TYPE_STRING;
        }
    }
    
    /**
     * calls HSSFCell.setCellValue with the java type appropriate for the cell type. 
     * @param cell 
     * @param value
     */
    protected void setCellValue(final HSSFCell cell, String value)
    {
        boolean valueSet = true;
        try
        {
            if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
            {
                cell.setCellValue(Double.parseDouble(value));
            }
            // parseBoolean returns true if value == "true" (ignoring case) else false.
            // Seems like data could be lost so I am assigning the string unless value definitely seems
            // to be boolean.
            // We probably should add a new cell editor specifically for "boolean" to control the value.
            // But we probably shouldn't even with bother with CELL_TYPE_BOOLEAN since Specify6
            // booleans arent' true booleans anyway so
            // why bother?
            else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN
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
            cell.setCellValue(value);
        }
    }
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataExport#writeData(java.util.List)
     */
    public void writeData(List<?> data) throws Exception
    {
        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet workSheet = workBook.createSheet();
        int rowNum = 0;

        if (config.getFirstRowHasHeaders() && !config.getAppendData())
        {
            writeHeaders(workSheet);
            rowNum++;
        }
        if (data.size() > 0)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                int[] colTypes;
                if (data.get(0).getClass() == WorkbenchTemplate.class)
                {
                    session.attach(data.get(0));
                    colTypes = bldColTypes((WorkbenchTemplate) data.get(0));
                    // now set up cell types and formats for a bunch of empty rows....
                    
                }
                else
                {
                    session.attach(((WorkbenchRow) data.get(0)).getWorkbench());
                    colTypes = bldColTypes(((WorkbenchRow) data.get(0)).getWorkbench()
                            .getWorkbenchTemplate());
                    for (Object row : data)
                    {
                        HSSFRow hssfRow = workSheet.createRow(rowNum++);
                        for (short colNum = 0; colNum < ((WorkbenchRow) row).getWorkbenchDataItems()
                                .size(); colNum++)
                        {
                            HSSFCell cell = hssfRow.createCell(colNum);
                            cell.setCellType(colTypes[colNum]);
                            setCellValue(cell, ((WorkbenchRow) row).getData(colNum));
                        }
                    }
               }
            }
            finally
            {
                session.close();
            }
        }
        try
        {
            FileOutputStream fos = new FileOutputStream(getConfig().getFileName());
            workBook.write(fos);
        } catch (Exception e)
        {
            throw(e);
        }
    }
    
}
