/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.LatLonConverter;
import org.apache.poi.ss.usermodel.*;

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
    
    private Vector<Integer>              cardImageCols = new Vector<Integer>();
    private int                          geoCol        = -1;
    protected ConfigureExternalDataIFace config;
    
    /**
     * @param headerRow
     */
    private void getSystemCols(final Row headerRow)
    {
        for (int c = headerRow.getFirstCellNum(); c <= headerRow.getLastCellNum(); c++)
        {
            Cell cell = headerRow.getCell(c);
            int nulls = 0;
            if (cell != null)
            {
                String header = cell.getRichStringCellValue().getString();
                if (header != null)
                {
                    if (header.equals(IMAGE_PATH_HEADING))
                    {
                        cardImageCols.add(c - nulls);
                    }
                    if (header.equals(GEO_DATA_HEADING))
                    {
                        geoCol = c - nulls;
                    }
                }
            }
            else
            {
            	nulls++;
            }
        }
    }
     
     /**
     * Constructor.
     * @param config the configuration
     */
    public XLSImport(final ConfigureExternalDataIFace config)
    {
        this.config = config;
    }

    /**
     * @param wbtmi
     * @return true if wbtmi maps a geo-coordinate
     */
    protected boolean isGeoCoordinate(final WorkbenchTemplateMappingItem wbtmi)
    {
    	String fld = wbtmi.getFieldName().toLowerCase();
    	return fld.equals("latitude1") || fld.equals("latitude2") || fld.equals("longitude1") || fld.equals("longitude2");
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
                Workbook workBook = WorkbookFactory.create(input);
                Sheet sheet    = workBook.getSheetAt(0);
                int             numRows  = 0;
                
                // Calculate the number of rows and columns
    
                Set<WorkbenchTemplateMappingItem>    wbtmiSet  = workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems();
                Vector<WorkbenchTemplateMappingItem> wbtmiList = new Vector<WorkbenchTemplateMappingItem>();
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(0);
                nf.setMaximumFractionDigits(20);
                nf.setGroupingUsed(false); //gets rid of commas
                NumberFormat nfGeoCoord = NumberFormat.getInstance();
                nfGeoCoord.setMinimumFractionDigits(0);
                nfGeoCoord.setMaximumFractionDigits(LatLonConverter.DECIMAL_SIZES[LatLonConverter.FORMAT.DDDDDD.ordinal()]);
                nfGeoCoord.setGroupingUsed(false); //gets rid of commas
                char decSep = new DecimalFormatSymbols().getDecimalSeparator();
                wbtmiList.addAll(wbtmiSet);
                
                Collections.sort(wbtmiList);
                
                this.truncations.clear();
                Vector<Hyperlink> activeHyperlinks = new Vector<>();
                
                // Iterate over each row in the sheet
                Iterator<?> rows = sheet.rowIterator();
                while (rows.hasNext())
                {
                    Row row = (Row) rows.next();
                    if (numRows == 0 && config.getFirstRowHasHeaders())
                    {
                        numRows++;
                        getSystemCols(row);
                        continue;
                    }
    
                    WorkbenchRow wbRow = workbench.addRow();
                    
                    for (WorkbenchTemplateMappingItem wbtmi : wbtmiList)
                    {
                        int cellNum = wbtmi.getOrigImportColumnIndex().intValue();
                        if (cellNum == -1)
                        {
                            if (wbtmi.getViewOrder() != null)
                            {
                                cellNum = wbtmi.getViewOrder().intValue();
                                if (cellNum == -1)
                                {
                                    continue;
                                }
                            }
                        }
                        Cell cell    = row.getCell(cellNum);
                        if (cell == null)
                        {
                            continue;
                        }
                        CellType      type    = cell.getCellType();
                        if (type == CellType.FORMULA)
                        {
                        	type = cell.getCachedFormulaResultType();
                        }
                        String   value   = "";
                        boolean  skip    = false;
    
                            if (type == CellType.NUMERIC) {
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    //even if WorkbenchTask.getDataType(wbtmi) is not Calendar or Date. Hmmmm.
                                    value = scrDateFormat.getSimpleDateFormat().format(cell.getDateCellValue());
                                } else {
                                    Class<?> classObj = WorkbenchTask.getDataType(wbtmi, false);
                                    if (classObj.equals(Integer.class)) {
                                        double numeric = cell.getNumericCellValue();
                                        value = Integer.toString((int) numeric);
                                    } else if (classObj.equals(Calendar.class) || classObj.equals(Date.class)) {
                                        Date d = cell.getDateCellValue();
                                        if (d != null) {
                                        	value = scrDateFormat.getSimpleDateFormat().format(cell.getDateCellValue());
                                        } else {
                                        	value = null;
                                        }
                                    } else {
                                        double numeric = cell.getNumericCellValue();
                                        value = nf.format(numeric);
                                        if (isGeoCoordinate(wbtmi)) {
                                            int sepInx = value.indexOf(decSep);
                                        	if (sepInx > -1 && value.substring(sepInx).length() > nfGeoCoord.getMaximumFractionDigits()) {
                                        		String value2 = nfGeoCoord.format(numeric);
                                        		int maxlen = wbtmi.getFieldName().startsWith("latitude") 
                                        			? nfGeoCoord.getMaximumFractionDigits() + 3 
                                        			: nfGeoCoord.getMaximumFractionDigits() + 4;
                                        		if (numeric < 0) {
                                        			maxlen++;
                                        		}
                                        		value = value2;
                                        	}
                                        }    
                                    }
                                }
                            } else if (type == CellType.STRING) {
                                Hyperlink hl = checkHyperlinks(cell, activeHyperlinks);
                                if (hl == null /*|| (hl != null && hl.getType() == HSSFHyperlink.LINK_EMAIL)*/)
                                {
                                    value = cell.getRichStringCellValue().getString();
                                }
                                else
                                {
                                    //value = hl.getAddress();
                                	value = hl.getLabel();
                                }
                            } else if (type == CellType.BLANK) {
                                value = "";
                                type = CellType.STRING;
                            } else if (type == CellType.BOOLEAN) {
                                boolean bool = cell.getBooleanCellValue();
                                value = Boolean.toString(bool);
                            } else {
                                skip = true;
                        }
    
                        if (!skip && value != null && !value.trim().equals(""))
                        {
                            wbRow.setData(truncateIfNecessary(value, numRows, wbtmi), wbtmi.getViewOrder(), true);
                        }
                    }
                    addImageInfo(row, wbRow);
                    addGeoInfo(row, wbRow);
                    numRows++;
                }
                if (activeHyperlinks.size() > 0)
                {
                    log.warn("Hyperlinks vector not empty after import. Overlapping hyperlink ranges?");
                }
                return status = this.truncations.size() == 0 && this.messages.size() == 0 ? DataImportIFace.Status.Valid : DataImportIFace.Status.Modified;
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(XLSImport.class, ex);
                log.error(ex);
            }
        }
        return status = DataImportIFace.Status.Error;
    }

    /**
     * @param cell
     * @param activeHyperlinks
     * @return the Hyperlink applicable for the cell.
     * 
     * NOTE: This code assumes that hyperlinks' row and column ranges do not overlap.   
     */
    protected Hyperlink checkHyperlinks(final Cell cell, final Vector<Hyperlink> activeHyperlinks)
    {
        if (cell.getHyperlink() != null)
        {
            Hyperlink l = cell.getHyperlink();
        	if (l.getLastRow() > cell.getRowIndex() || l.getLastColumn() > cell.getColumnIndex())
            {
                activeHyperlinks.add(l);
            }
            return l;
        }
        
        for (Hyperlink hl : activeHyperlinks)
        {
            if (cell.getRowIndex() >= hl.getFirstRow() && cell.getRowIndex() <= hl.getLastRow() 
                    && cell.getColumnIndex() >= hl.getFirstColumn() && cell.getColumnIndex() <= hl.getLastColumn())
            {
                if (cell.getRowIndex() == hl.getLastRow())
                {
                    activeHyperlinks.remove(hl);
                }
                return hl;
            }
        }
        
        return null;
    }
    
    private void addImageInfo(final Row row, final WorkbenchRow wbRow)
    {
        for (Integer c : cardImageCols)
        {
            Cell imgCell = row.getCell(c);
            if (imgCell != null)
            {
                String imageSpec[] = imgCell.getRichStringCellValue().getString().split("\\t");
                String imagePath = imageSpec[0];
                String attachToTblName = imageSpec.length > 1 ? imageSpec[1] : null;
                if (imagePath != null)
                {
                    try
                    {
                        wbRow.addImage(new File(imagePath), attachToTblName);
                    }
                    catch (IOException e)
                    {
                        //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(XLSImport.class, e);
                    	wbRow.addImagePath(imagePath, attachToTblName);
                        UIRegistry.getStatusBar().setErrorMessage(e.getMessage());
                        StringBuilder errMsg = new StringBuilder(getResourceString("WB_IMG_IMPORT_ERROR"));
                        errMsg.append(": ");
                        errMsg.append(getResourceString("WB_ROW"));
                        errMsg.append(" ");
                        errMsg.append(row.getRowNum());
                        errMsg.append(", ");
                        errMsg.append(imagePath);
                        messages.add(errMsg.toString());
                    }
                }
            }
        }
    }
    
    public void addGeoInfo(final Row row, final WorkbenchRow wbRow)
    {
        if (geoCol != -1)
        {
            Cell c = row.getCell(geoCol);
            if (c != null)
            {
                String geoData = c.getRichStringCellValue().getString();
                if (geoData != null)
                {
                    // TEMP FIX FOR BUG 4562 RELEASE
                    // Only allow 255 chars - Note this really shouldn't happen because 
                    // the WB should have never been able to save something larger
                    wbRow.setBioGeomancerResults(geoData.length() <= 255 ? geoData : geoData.substring(0, 254));
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
