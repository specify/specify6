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

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.WorkbenchRow;

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
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataExport#writeData(java.util.List)
     */
    public void writeData(List<?> data, final DataProviderSessionIFace session, final boolean closeSession) throws Exception
    {
        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet workSheet = workBook.createSheet();
        int rowNum = 0;
        if (config.getFirstRowHasHeaders() && !config.getAppendData())
        {
            writeHeaders(workSheet);
            rowNum++;
        }
        for (Object row : data)
        {
            HSSFRow hssfRow = workSheet.createRow(rowNum++);
            for (short colNum = 0; colNum < ((WorkbenchRow) row).getWorkbenchDataItems().size(); colNum++)
            {
                HSSFCell cell = hssfRow.createCell(colNum);
                cell.setCellValue(((WorkbenchRow) row).getData(colNum));
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
        if (session != null && closeSession)
        {
            session.close();
        }
    }

}
