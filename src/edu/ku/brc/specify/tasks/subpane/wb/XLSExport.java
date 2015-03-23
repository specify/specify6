/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks.subpane.wb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

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
import edu.ku.brc.ui.UIRegistry;

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
        int col = 0;
        for (String head : headers)
        {
            hssfRow.createCell(col++).setCellValue(new HSSFRichTextString(head));
        }
    }

    /**
     * @param workSheet
     * writes headers for imagePath and geocoord (bg) data columns
     */
    protected void writeExtraHeaders(final HSSFSheet workSheet, Vector<Integer> imgCols, int geoDataCol)
    {
        HSSFRow hssfRow = workSheet.getRow(0);
        if (geoDataCol != -1)
        {
        	hssfRow.createCell(geoDataCol).setCellValue(new HSSFRichTextString(DataImport.GEO_DATA_HEADING));
        }
        for (Integer c : imgCols)
        {
            hssfRow.createCell(c).setCellValue(new HSSFRichTextString(DataImport.IMAGE_PATH_HEADING));
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
     * calls HSSFCell.setCellValue 
     * 
     * Since all data is treated as string data by the WB and is not validated until an upload is attempted,
     * Validation and type-checking is no longer performed here since it could lead to loss of data 
     * in the exported file.
     * 
     * @param cell
     * @param value
     */
    protected void setCellValue(final HSSFCell cell, final String value)
    {
    	cell.setCellValue(new HSSFRichTextString(value));
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
        List<WorkbenchTemplateMappingItem> wbmis = new ArrayList<WorkbenchTemplateMappingItem>(wbt.getWorkbenchTemplateMappingItems());
        Collections.sort(wbmis, new Comparator<WorkbenchTemplateMappingItem>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(WorkbenchTemplateMappingItem arg0,
					WorkbenchTemplateMappingItem arg1) {
				// TODO Auto-generated method stub
				return arg0.getViewOrder().compareTo(arg1.getViewOrder());
			}
        	
        });
        for (WorkbenchTemplateMappingItem wbmi : wbmis)
        {
            //cps.put(wbmi.getCaption(), 
        	//cps.put(wbmi.getTableName() + "." + wbmi.getFieldName(), 
        	cps.put(ConfigureXLS.POIFS_COL_KEY_PREFIX + wbmi.getViewOrder(),
        			wbmi.getCaption()
        			+ "\t" + wbmi.getTableName() 
            		+ "\t" + wbmi.getFieldName()
            		+ "\t" + wbmi.getXCoord() 
            		+ "\t" + wbmi.getYCoord()
            		+ "\t" + wbmi.getCaption()
            		+ "\t" + wbmi.getFieldType()
            		+ "\t" + wbmi.getMetaData()
            );
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
            for (int i=0;i<headers.length;i++)
            {
                workSheet.setColumnWidth(i, StringUtils.isNotEmpty(headers[i]) ? (256 * headers[i].length()) : 2560);
            }
            
            WorkbenchTemplate wbTemplate = null;
            if (data.get(0) instanceof WorkbenchTemplate)
            {
            	wbTemplate = (WorkbenchTemplate )data.get(0);
            }
            else
            {
            	wbTemplate = ((WorkbenchRow )data.get(0)).getWorkbench().getWorkbenchTemplate();
            }
            mappings = writeMappings(wbTemplate);
        }
        //assuming data is never empty.
        boolean hasTemplate = data.get(0) instanceof WorkbenchTemplate;
        boolean hasRows = hasTemplate ? data.size() > 1 : data.size() > 0;
        if (hasRows)
		{
			int[] disciplinees;
			
			WorkbenchRow wbRow = (WorkbenchRow) data.get(hasTemplate ? 1 : 0);
			Workbench workBench = wbRow.getWorkbench();
			WorkbenchTemplate template = workBench.getWorkbenchTemplate();
			int numCols = template.getWorkbenchTemplateMappingItems()
					.size();
			int geoDataCol = -1;
			Vector<Integer> imgCols = new Vector<Integer>();

			disciplinees = bldColTypes(template);
			for (Object rowObj : data)
			{
				if (rowObj instanceof WorkbenchTemplate)
				{
					continue;
				}

				WorkbenchRow row = (WorkbenchRow) rowObj;
				HSSFRow hssfRow = workSheet.createRow(rowNum++);
				int colNum;
				boolean rowHasGeoData = false;

				for (colNum = 0; colNum < numCols; colNum++)
				{
					HSSFCell cell = hssfRow.createCell(colNum);
					cell.setCellType(disciplinees[colNum]);
					setCellValue(cell, row.getData(colNum));
				}

				if (row.getBioGeomancerResults() != null
						&& !row.getBioGeomancerResults().equals(""))
				{
					geoDataCol = colNum;
					rowHasGeoData = true;
					HSSFCell cell = hssfRow.createCell(colNum++);
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					setCellValue(cell, row.getBioGeomancerResults());
				}

				// if (row.getCardImage() != null)
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
						if (imgCols.indexOf(colNum) < 0)
						{
							imgCols.add(colNum);
						}
						HSSFCell cell = hssfRow.createCell(colNum++);
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						String cellValue = img.getCardImageFullPath();
						String attachToTbl = img.getAttachToTableName();
						if (attachToTbl != null)
						{
							cellValue += "\t" + attachToTbl;
						}
						setCellValue(cell, cellValue);
						img = row.getRowImage(imgIdx++);
					}
				}

			}
			if (imgCols.size() > 0 || geoDataCol != -1)
			{
				writeExtraHeaders(workSheet, imgCols, geoDataCol);
			}

		}
        try
        {
            // Write the workbook
            File file = new File(getConfig().getFileName());
            if (file.canWrite() || (!file.exists() && file.createNewFile()))
            {
                FileOutputStream fos = new FileOutputStream(file);
                workBook.write(fos);
                fos.close();
                
                //Now write the mappings.
                //NOT (hopefully) the best way to write the mappings, but (sadly) the easiest way. 
                //May need to do this another way if this slows performance for big wbs.
                if (mappings != null)
                {
                    InputStream is = new FileInputStream(file);
                    POIFSFileSystem poifs = new POIFSFileSystem(is);
                    is.close();
                    mappings.write(poifs.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);
                    fos = new FileOutputStream(file);
                    poifs.writeFilesystem(fos);
                    fos.close();
                }
            } else
            {
                UIRegistry.displayErrorDlgLocalized("WB_EXPORT_PERM_ERR");
            }
        } 
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(XLSExport.class, e);
            throw(e);
        }
    }
    
}
