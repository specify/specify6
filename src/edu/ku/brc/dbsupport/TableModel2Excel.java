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
 * Helper that converts a Swing TableModel to an MS-Excel Spreadsheet
 *
 * @author Rod Spears <rods@ku.edu>
 */

public class TableModel2Excel
{
    private static final Logger log = Logger.getLogger(TableModel2Excel.class);

    public TableModel2Excel()
    {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public static File convertToExcel(String aTitle, TableModel aTableModel)
    {
        File tmpFile = null;
        if (aTableModel != null && aTableModel.getRowCount() > 0)
        {
            try
            {
                tmpFile = new File(File.createTempFile("collection_items_", null).getName() + ".xls");
                //tmpFile = new File("temp.xls");
                
                // create a new file
                FileOutputStream out = new FileOutputStream(tmpFile);
                
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
                
                short numColumns = (short)aTableModel.getColumnCount();
                
                HSSFRow  headerRow  = sheet.createRow(0);
                for (int i=0;i<numColumns;i++)
                {
                    HSSFCell headerCell = headerRow.createCell((short) i);
                    headerCell.setCellStyle(headerStyle);
                    
                    //add the date to the header cell
                    headerCell.setCellValue(aTableModel.getColumnName(i));
                    sheet.setColumnWidth((short)i, (short)(30 * 256));
                }
                
                //--------------------------
                // done header
                //--------------------------
                
                // create 3 cell styles
                HSSFCellStyle oddCellStyle  = wb.createCellStyle();
                HSSFCellStyle evenCellStyle = wb.createCellStyle();
                
                // create 2 fonts objects
                HSSFFont cellFont  = wb.createFont();
                //set font 1 to 12 point type
                cellFont.setFontHeightInPoints((short) 11);
                oddCellStyle.setFont(cellFont);
                evenCellStyle.setFont(cellFont);
                
                evenCellStyle.setFillPattern((short) HSSFCellStyle.SOLID_FOREGROUND);
                oddCellStyle.setFillPattern((short) HSSFCellStyle.SOLID_FOREGROUND);
                
                oddCellStyle.setFillForegroundColor((short) HSSFColor.WHITE.index);
                evenCellStyle.setFillForegroundColor((short) HSSFColor.GREY_25_PERCENT.index);
                
                oddCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                evenCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // set the sheet name to HSSF Test
                wb.setSheetName(0, aTitle);
                
                for (short rownum = 1; rownum < (short)aTableModel.getRowCount(); rownum++)
                {
                    // create a row
                    HSSFRow row = sheet.createRow(rownum);

                    for (short cellnum = (short) 0; cellnum < numColumns; cellnum++)
                    {
                        // create a numeric cell
                        HSSFCell cell = row.createCell(cellnum);
                        
                        cell.setCellValue(aTableModel.getValueAt(rownum, cellnum).toString());

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
                log.error("convertToExcel", ex);
            }
        }
        
        return tmpFile;
        
    }

}
