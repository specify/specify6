/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.IllegalPropertySetDataException;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import edu.ku.brc.specify.rstools.ExportFileConfigurationFactory;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Configures xls file for import to a workbench. Currently only property that is configured is the header list.
 *If first row does not contain headers, they are simply assigned "Column1", "Column2" etc.
 */
public class ConfigureXLS extends ConfigureExternalDataBase
{
    private static final Logger log = Logger.getLogger(ConfigureXLS.class);

    protected int numRows = 0;
    protected int numCols = 0;

    public ConfigureXLS(final File file)
    {
        super();
        readConfig(file);
    }

    public ConfigureXLS(final Properties props)
    {
        super(props);
    }
    
    
    /**
     * Fills badHeads with indexes for columns that contain data but don't have a header or have an non-string header (because it makes things difficult with HSSF).
     * Fills emptyCols with indexes for columns that are totally empty.
     * Assumes that badHeads and emptyCols are not null and empty.
     * 
     */
    public void checkHeadsAndCols(final HSSFSheet sheet, Vector<Integer> badHeads, Vector<Integer> emptyCols) 
    {
        boolean firstRow = true;
        Vector<Boolean> firstRowCells = new Vector<Boolean>();
        Vector<Boolean> restCells = new Vector<Boolean>();
        
        // Iterate over each row in the sheet
        Iterator<?> rows = sheet.rowIterator();
        while (rows.hasNext())
        {
            HSSFRow row = (HSSFRow) rows.next();
            int maxSize = Math.max(row.getPhysicalNumberOfCells(), row.getLastCellNum());
            for (short col = 0; col < maxSize; col++)
            {
                if (firstRow)
                {
                    if (row.getCell(col) == null)
                    {
                        firstRowCells.add(false);
                    }
                    else if (row.getCell(col).getCellType() == HSSFCell.CELL_TYPE_STRING)
                    {
                        firstRowCells.add(true);
                    }
                    else
                    {
                        firstRowCells.add(null);
                    }
                }
                else
                {
                    if (col == restCells.size())
                    {
                        restCells.add(false);
                    }
                    if (!restCells.get(col))
                    {
                        restCells.set(col, row.getCell(col) != null);
                    }
                }
            }
            firstRow = false;
        }
        
        //pad smaller vector with false if necessary.
        while (restCells.size() < firstRowCells.size())
        {
            restCells.add(false);
        }
        while (firstRowCells.size() < restCells.size())
        {
            firstRowCells.add(false);
        }
                
        for (int c = 0; c < firstRowCells.size(); c++)
        {
            if (firstRowCells.get(c) == null || (!firstRowCells.get(c) && restCells.get(c)))
            {
                badHeads.add(c);
            }
            if (firstRowCells.get(c) != null && !firstRowCells.get(c) && !restCells.get(c))
            {
                emptyCols.add(c);
            }
        }
    }

    @Override
    protected void interactiveConfig()
    {
        //firstRowHasHeaders = determineFirstRowHasHeaders();
        DataImportDialog dlg = new DataImportDialog(this,  firstRowHasHeaders);
        
        if (!dlg.isCancelled())
        {
            firstRowHasHeaders = dlg.getDoesFirstRowHaveHeaders();
            nonInteractiveConfig();
        }
        else
        {
            status = Status.Cancel;
        }
        //nonInteractiveConfig();
    }

    public void showBadHeadingsMsg(final Vector<Integer> badHeadingIdxs, final Vector<Integer> emptyCols, final String title)
    {
        String colStr = "";
        for (int c=0; c<badHeadingIdxs.size(); c++)
        {
            if (c > 0)
            {
                colStr += c == badHeadingIdxs.size()-1 ? " and " : ", ";
            }
            int adjust = 1;
            if (emptyCols != null)
            {
                for (Integer ec : emptyCols)
                {
                    if (ec <= badHeadingIdxs.get(c))
                    {
                        adjust--;
                    }
                }
                colStr += badHeadingIdxs.get(c)+adjust;
            }
        }
        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                String.format(getResourceString((badHeadingIdxs.size() == 1 ? "WB_IMPORT_INVALID_COL_HEADER" : "WB_IMPORT_INVALID_COL_HEADERS")), colStr), 
                title, JOptionPane.ERROR_MESSAGE);

    }
    
    /**
     * @param poifs
     * @returns the DocumentSummaryInformation for poifs, or null if no DocumentSummaryInformation is found.
     */
    protected DocumentSummaryInformation getDocSummary(final POIFSFileSystem poifs)
    {
        DirectoryEntry dir = poifs.getRoot();
        DocumentSummaryInformation result = null;
        try
        {
            DocumentEntry dsiEntry = (DocumentEntry)
                dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
            DocumentInputStream dis = new DocumentInputStream(dsiEntry);
            PropertySet ps = new PropertySet(dis);
            dis.close();
            result = new DocumentSummaryInformation(ps);
        }
        catch (FileNotFoundException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            // There is no document summary information. 
            result = null;
        }
        /*
         * just returning null if anything weird happens. If there is a problem with the xls file,
         * something else will probably blow up later. 
        */
        catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            log.debug(ex);
            result = null;
        }
        catch (NoPropertySetStreamException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            log.debug(ex);
            result = null;
        }
        catch (MarkUnsupportedException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            log.debug(ex);
            result = null;
        }
        catch (UnexpectedPropertySetTypeException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            log.debug(ex);
            result = null;
        }
        catch (IllegalPropertySetDataException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            log.debug(ex);
            result = null;
        }
        return result;
    }
    
    /**
     * @param poifs
     * 
     * Reads workbench mappings from the XLS file.
    */
    protected void readMappings(final POIFSFileSystem poifs)
    {
        DocumentSummaryInformation dsi = getDocSummary(poifs);
        if (dsi != null)
        {
            CustomProperties props = dsi.getCustomProperties();
            if (props != null)
            {
                for (ImportColumnInfo col : colInfo)
                {
                    if (props.get(col.getColTitle()) != null)
                    {
                        String[] mapping = ((String) props.get(col.getColTitle())).split("\t");
                        col.setMapToTbl(mapping[0]);
                        col.setMapToFld(mapping[1]);
                    }
                }
            }
        }
    }
    /* (non-Javadoc)
     * Sets up colInfo for externalFile.
     * @see edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataIFace#getConfig(java.lang.String)
     */
    @Override
    protected void nonInteractiveConfig()
    {
        try
        {
            InputStream     input    = new FileInputStream(externalFile);
            POIFSFileSystem fs       = new POIFSFileSystem(input);
            HSSFWorkbook    workBook = new HSSFWorkbook(fs);
            HSSFSheet       sheet    = workBook.getSheetAt(0);

            // Calculate the number of rows and columns
            colInfo = new Vector<ImportColumnInfo>(16);

            Hashtable<Short, Boolean> colTracker = new Hashtable<Short, Boolean>();

            boolean firstRow = true;
            short   col      = 0;
            colTracker.clear();

            Vector<Integer> badHeads = new Vector<Integer>();
            Vector<Integer> emptyCols = new Vector<Integer>();
            checkHeadsAndCols(sheet, badHeads, emptyCols);
            
            if (firstRowHasHeaders && badHeads.size() > 0)
            {
                status = ConfigureExternalDataIFace.Status.Error;
                showBadHeadingsMsg(badHeads, null, getResourceString("Error"));
                return;
            }
            
            // Iterate over each row in the sheet
            @SuppressWarnings("unchecked") Iterator<HSSFRow> rows =  sheet.rowIterator();
            while (rows.hasNext())
            {
                HSSFRow row = rows.next();
                if (firstRow || numRows == 1)
                {
                    // Iterate over each cell in the row and print out the cell's content
                    short colNum = 0;
                    int maxSize = Math.max(row.getPhysicalNumberOfCells(), row.getLastCellNum());
                    while (colNum < maxSize)
                    {
                        if (emptyCols.indexOf(new Integer(colNum)) == -1)
                        {
                            ImportColumnInfo.ColumnType disciplinee = ImportColumnInfo.ColumnType.Integer;
                            String value = null;
                            boolean skip = false;
                            HSSFCell cell = row.getCell(colNum);
                            if (cell == null)
                            {
                                //assuming numRows == 1 or not firstRowHasHeaders.
                                //the call to checkHeadsAndCols would have already blank headers.
                                value = "";
                                disciplinee = ImportColumnInfo.ColumnType.String;
                            }
                            else switch (cell.getCellType())
                            {
                                case HSSFCell.CELL_TYPE_NUMERIC:
                                    double numeric = cell.getNumericCellValue();
                                    value = Double.toString(numeric);
                                    disciplinee = ImportColumnInfo.ColumnType.Double;
                                    break;
                                case HSSFCell.CELL_TYPE_STRING:
                                    HSSFRichTextString richVal = cell.getRichStringCellValue();
                                    value = richVal.getString().trim();
                                    disciplinee = ImportColumnInfo.ColumnType.String;
                                    break;
                                case HSSFCell.CELL_TYPE_BLANK:
                                    value = "";
                                    disciplinee = ImportColumnInfo.ColumnType.String;
                                    break;
                                case HSSFCell.CELL_TYPE_BOOLEAN:
                                    boolean bool = cell.getBooleanCellValue();
                                    value = Boolean.toString(bool);
                                    disciplinee = ImportColumnInfo.ColumnType.Boolean;
                                    break;
                                default:
                                    skip = true;
                                    break;
                            }

                            if (numRows == 1 && !skip)
                            {
                                colInfo.get(col).setData(value);
                                col++;
                            }
                            else if (!skip)
                            {
                                if (firstRowHasHeaders)
                                {
                                    colInfo.add(new ImportColumnInfo(colNum, disciplinee, value, value, null, null,
                                            null));
                                    colTracker.put(col, true);
                                }
                                else
                                {
                                    String colName = getResourceString("DEFAULT_COLUMN_NAME") + " "
                                            + (colNum + 1);
                                    colInfo.add(new ImportColumnInfo(colNum, disciplinee, colName,
                                            colName, null, null, null));
                                    colTracker.put(colNum, true);
                                }
                                numCols++;
                            }
                        }
                        colNum++;
                    }
                    firstRow = false;
                }
                numRows++;
            }
            Collections.sort(colInfo);
            readMappings(fs);
            status = Status.Valid;
        } catch (IOException ex)
        {
            status = Status.Error;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataBase#getProperties()
     */
    @Override
    public Properties getProperties()
    {
        Properties result = super.getProperties();
        result.setProperty("mimetype", ExportFileConfigurationFactory.XLS_MIME_TYPE);
       
        return result;
    }
}
