/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.dbsupport;

import java.io.File;
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
        String ext    = ".xls"; //$NON-NLS-1$
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
        style.setBorderBottom(borderStyle);
        style.setBottomBorderColor(colorIndex);
        style.setBorderLeft(borderStyle);
        style.setLeftBorderColor(colorIndex);
        style.setBorderRight(borderStyle);
        style.setRightBorderColor(colorIndex);
        style.setBorderTop(borderStyle);
        style.setTopBorderColor(colorIndex);
    }

    /**
     * Converts a tableModel to an Excel Spreadsheet.
     * @param toFile the file object to write it to.
     * @param title the title of the spreadsheet.
     * @param tableModel the table model
     * @return a file to a spreadsheet
     */
    public static File convertToExcel(final File toFile, final String title, final TableModel tableModel)
    {
        if (tableModel != null && tableModel.getRowCount() > 0)
        {
            try
            {
                // create a new file
                FileOutputStream out = new FileOutputStream(toFile);
                
                // create a new workbook
                HSSFWorkbook wb = new HSSFWorkbook();
                
                // create a new sheet
                HSSFSheet sheet = wb.createSheet();
                // declare a row object reference
                

                // Header Captions
                HSSFFont headerFont = wb.createFont();
                headerFont.setFontHeightInPoints((short) 12);
                headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                
                // create a style for the header cell
                HSSFCellStyle headerStyle = wb.createCellStyle();
                headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                headerStyle.setFont(headerFont);
                setBordersOnStyle(headerStyle, HSSFColor.GREY_25_PERCENT.index, HSSFCellStyle.BORDER_THIN);

                short numColumns = (short)tableModel.getColumnCount();
                
                HSSFRow  headerRow  = sheet.createRow(0);
                for (int i=0;i<numColumns;i++)
                {
                    HSSFCell headerCell = headerRow.createCell((short) i);
                    headerCell.setCellStyle(headerStyle);
                    
                    //add the date to the header cell
                    headerCell.setCellValue(tableModel.getColumnName(i));
                    sheet.setColumnWidth((short)i, (short)(30 * 256));
                }
                
                //--------------------------
                // done header
                //--------------------------
                
                // create 3 cell styles
                HSSFCellStyle oddCellStyle  = wb.createCellStyle();
                HSSFCellStyle evenCellStyle = wb.createCellStyle();
                
                setBordersOnStyle(oddCellStyle, HSSFColor.GREY_25_PERCENT.index, HSSFCellStyle.BORDER_THIN);
                setBordersOnStyle(evenCellStyle, HSSFColor.GREY_25_PERCENT.index, HSSFCellStyle.BORDER_THIN);
                
                // create 2 fonts objects
                HSSFFont cellFont  = wb.createFont();
                //set font 1 to 12 point type
                cellFont.setFontHeightInPoints((short) 11);
                oddCellStyle.setFont(cellFont);
                evenCellStyle.setFont(cellFont);
                
                evenCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                oddCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                
                oddCellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
                evenCellStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
                
                oddCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                evenCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // set the sheet name to HSSF Test
                wb.setSheetName(0, title);
                
                for (short rownum = 0; rownum < (short)tableModel.getRowCount(); rownum++)
                {
                    // create a row
                    HSSFRow row = sheet.createRow(rownum+1);

                    for (short cellnum = (short) 0; cellnum < numColumns; cellnum++)
                    {
                        // create a numeric cell
                        HSSFCell cell = row.createCell(cellnum);
                        
                        Object dataVal = tableModel.getValueAt(rownum, cellnum);
                        cell.setCellValue(dataVal != null ? dataVal.toString() : "");

                        // on every other row
                        cell.setCellStyle((rownum % 2) == 0 ? evenCellStyle : oddCellStyle);
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
