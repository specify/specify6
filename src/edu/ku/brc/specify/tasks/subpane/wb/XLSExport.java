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
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 */
public class XLSExport implements DataExport
{
    ConfigureXLS config;
    
    public XLSExport(ConfigureExternalData config)
    {
        setConfig(config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataExport#getConfig()
     */
    public ConfigureExternalData getConfig()
    {
        return config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataExport#setConfig(edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalData)
     */
    public void setConfig(ConfigureExternalData config)
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
    protected int[] bldColTypes(final WorkbenchRow row)
    {
        int[] result = new int[row.getWorkbenchDataItems().size()];
        for (WorkbenchTemplateMappingItem mapItem : row.getWorkbench().getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
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
        Class<?> dataType = mapItem.getDataType();
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
     * @param colTypes
     */
    protected void setCellValue(final HSSFCell cell, final String value)
    {
        switch (cell.getCellType())
        {
            case HSSFCell.CELL_TYPE_NUMERIC:
                cell.setCellValue(Double.parseDouble(value));
                return;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                cell.setCellValue(Boolean.parseBoolean(value));
                return;
        }
        cell.setCellValue(value);
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
                session.attach(((WorkbenchRow) data.get(0)).getWorkbench());
                int[] colTypes = bldColTypes((WorkbenchRow) data.get(0));
                for (Object row : data)
                {
                    HSSFRow hssfRow = workSheet.createRow(rowNum++);
                    for (short colNum = 0; colNum < ((WorkbenchRow) row).getWorkbenchDataItems().size(); colNum++)
                    {
                        HSSFCell cell = hssfRow.createCell(colNum);
                        cell.setCellType(colTypes[colNum]);
                        setCellValue(cell, ((WorkbenchRow) row).getData(colNum));
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
