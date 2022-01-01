/* Copyright (C) 2022, Specify Collections Consortium
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
package edu.ku.brc.dbsupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import edu.ku.brc.ui.UIRegistry;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Helper that converts a Swing TableModel to an MS-Excel Spreadsheet, HTML Table.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Dec 2, 2006
 *
 */
public class TableModel2Excel
{
    private static final Logger log = Logger.getLogger(TableModel2Excel.class);
    
    protected static TableModel2Excel instance = new TableModel2Excel();
    
    public TableModel2Excel()
    {
        // no op
    }
    
    /**
     * Returns just the name part with the path or the extension.
     * @param path the fill path with file name and extension
     * @return Returns just the name part with the path or the extension
     */
    public static String getFileNameWithoutExt(final String path)
    {
        int inx = path.indexOf(File.separator);
        return path.substring(inx+1, path.lastIndexOf('.'));
    }
    
    /**
     * Returns a temporary file name (no path).
     * @return a temporary file name (no path).
     */
    public static File getTempExcelName()
    {
        String prefix = "collection_items_"; //$NON-NLS-1$
        String ext    = ".xlsx"; //$NON-NLS-1$
        try
        {
            String fileName = getFileNameWithoutExt(File.createTempFile(prefix, null).getName());
            return new File(fileName  + ext);
            
        } catch (IOException ioex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TableModel2Excel.class, ioex);
            return new File(prefix + Long.toString(new Date().getTime()) + ext);
        }
    }
    
    /**
     * Converts a tableModel to an Excel Spreadsheet.
     * @param title the title of the spreadsheet.
     * @param tableModel the table model
     * @return a file to a spreadsheet
     */
    public static File convertToExcel(String title, TableModel tableModel)
    {
        return convertToExcel(getTempExcelName(), title, tableModel);
    }
    
    protected static void setBordersOnStyle(final HSSFCellStyle style, 
                                            final short colorIndex, 
                                            final short borderStyle)
    {
//        style.setBorderBottom(borderStyle);
//        style.setBottomBorderColor(colorIndex);
//        style.setBorderLeft(borderStyle);
//        style.setLeftBorderColor(colorIndex);
//        style.setBorderRight(borderStyle);
//        style.setRightBorderColor(colorIndex);
//        style.setBorderTop(borderStyle);
//        style.setTopBorderColor(colorIndex);
    }

    /**
     * Converts a tableModel to an Excel Spreadsheet.
     * @param toFile the file object to write it to.
     * @param title the title of the spreadsheet.
     * @param tableModel the table model
     * @return a file to a spreadsheet
     */
    public static File convertToExcel(final File toFile, 
                                      final String title, 
                                      final TableModel tableModel)
    {
        if (toFile == null)
        {
            UIRegistry.showLocalizedMsg("WARNING", "FILE_NO_EXISTS", toFile != null ? toFile.getAbsolutePath() : "");
            return null;
        }
        
        if (tableModel != null && tableModel.getRowCount() > 0)
        {
            try
            {
                // create a new file
                FileOutputStream out;
                try
                {
                    out = new FileOutputStream(toFile);
                    
                } catch (FileNotFoundException ex)
                {
                    UIRegistry.showLocalizedMsg("WARNING", "FILE_NO_WRITE", toFile != null ? toFile.getAbsolutePath() : "");
                    return null;
                }
                
                // create a new workbook
                XSSFWorkbook wb = new XSSFWorkbook();
                
                // create a new sheet
                Sheet sheet = wb.createSheet();
                // declare a row object reference
                

                // Header Captions
                Font headerFont = wb.createFont();
                headerFont.setFontHeightInPoints((short) 12);
                //headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                
                short numColumns = (short)tableModel.getColumnCount();
                
                Row headerRow  = sheet.createRow(0);
                for (int i=0;i<numColumns;i++)
                {
                    Cell headerCell = headerRow.createCell((short) i);
                    //headerCell.setCellStyle(headerStyle);
                    
                    //add the date to the header cell
                    headerCell.setCellValue(tableModel.getColumnName(i));
                    sheet.setColumnWidth((short)i, (short)(30 * 256));
                }
                

                // set the sheet name to HSSF Test
                wb.setSheetName(0, title);
                
                for (int rownum = 0; rownum < tableModel.getRowCount(); rownum++)
                {
                    // create a row
                    Row row = sheet.createRow(rownum+1);

                    for (short cellnum = (short) 0; cellnum < numColumns; cellnum++)
                    {
                        // create a numeric cell
                        Cell cell = row.createCell(cellnum);
                        
                        Object dataVal = tableModel.getValueAt(rownum, cellnum);
                        cell.setCellValue(dataVal != null ? dataVal.toString() : "");

                    }
                }

                // write the workbook to the output stream
                // close our file (don't blow out our file handles
                wb.write(out);
                out.close();
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TableModel2Excel.class, ex);
                log.error("convertToExcel", ex); //$NON-NLS-1$
            }
        }
        return toFile;
    }

    /**
     * Converts a tableModel to an HTML Table.
     * @param title the title of the spreadsheet.
     * @param tableModel the table model
     * @return a file to a spreadsheet
     */
    public static StringBuilder convertToHTML(String title, TableModel tableModel)
    {
        StringBuilder strBuilder = new StringBuilder(512);
        strBuilder.append("<table border=1>"); //$NON-NLS-1$
        if (tableModel != null && tableModel.getRowCount() > 0)
        {
            for (int i=0;i<tableModel.getColumnCount();i++)
            {    
                //add the date to the header cell
                strBuilder.append("<td align=center>"); //$NON-NLS-1$
                strBuilder.append(tableModel.getColumnName(i));
                strBuilder.append("</td>"); //$NON-NLS-1$
            }
            strBuilder.append("</tr>\n"); //$NON-NLS-1$
            
            //--------------------------
            // done header
            //--------------------------
            
            for (int rownum = 0; rownum < tableModel.getRowCount(); rownum++)
            {
                strBuilder.append("<tr>\n"); //$NON-NLS-1$
                for (short cellnum = (short) 0; cellnum < tableModel.getColumnCount(); cellnum++)
                {
                    //add the date to the header cell
                    strBuilder.append("<td align=center>"); //$NON-NLS-1$
                    Object data = tableModel.getValueAt(rownum, cellnum);
                    strBuilder.append(data != null ? data.toString() : "&nbsp;");
                    strBuilder.append("</td>"); //$NON-NLS-1$
                }
                strBuilder.append("</tr>\n"); //$NON-NLS-1$
            }
            strBuilder.append("</table>\n"); //$NON-NLS-1$
        }
        return strBuilder;
        
    }
    
    /*
    public class TableModel2ExcelSetup
    {
        protected boolean isOddEven;
        protected boolean oodColor;
        protected boolean evenColor;
        protected boolean headerFontSize;
        
        public TableModel2ExcelSetup()
        {
            
        }
    }*/

}
