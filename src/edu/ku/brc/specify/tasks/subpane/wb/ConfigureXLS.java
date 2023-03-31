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
import java.util.*;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import edu.ku.brc.specify.rstools.ExportFileConfigurationFactory;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

    public static final String POIFS_COL_KEY_PREFIX = "wbmiViewOrder@@";
    
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
    public void checkHeadsAndCols(final Sheet sheet, Vector<Integer> badHeads, Vector<Integer> emptyCols)
    {
        boolean firstRow = true;
        Vector<Boolean> firstRowCells = new Vector<Boolean>();
        Vector<Boolean> restCells = new Vector<Boolean>();
        
        // Iterate over each row in the sheet
        Iterator<?> rows = sheet.rowIterator();
        while (rows.hasNext()) {
            Row row = (Row)rows.next();
            int maxSize = Math.max(row.getPhysicalNumberOfCells(), row.getLastCellNum());
            for (int col = 0; col < maxSize; col++) {
                if (firstRow) {
                    if (row.getCell(col) == null) {
                        firstRowCells.add(false);
                    }
                    else if (row.getCell(col).getCellType() == CellType.STRING) {
                        firstRowCells.add(true);
                    }
                    else {
                        firstRowCells.add(null);
                    }
                } else {
                    if (col == restCells.size()) {
                        restCells.add(false);
                    }
                    if (!restCells.get(col)) {
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
		DataImportDialog dlg = new DataImportDialog(this, firstRowHasHeaders);
		if (!dlg.init() || dlg.hasTooManyRows)
		{
			status = Status.Cancel;
			return;
		}
		
		UIHelper.centerAndShow(dlg, 800, null);

		if (!dlg.isCancelled())
		{
			firstRowHasHeaders = dlg.getDoesFirstRowHaveHeaders();
			nonInteractiveConfig();
		} else
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
        JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow() != null ? UIRegistry.getMostRecentWindow() : UIRegistry.getTopWindow(),
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
        catch (Exception ex)
        {
            // There is no document summary information. 
            result = null;
        }
        /*
         * just returning null if anything weird happens. If there is a problem with the xls file,
         * something else will probably blow up later. 
        */
        /*catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            log.debug(ex);
            result = null;
        }
        catch (NoPropertySetStreamException ex)
        {
            //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            log.debug(ex);
            result = null;
        }
        catch (MarkUnsupportedException ex)
        {
            //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            log.debug(ex);
            result = null;
        }
        catch (UnexpectedPropertySetTypeException ex)
        {
            //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            log.debug(ex);
            result = null;
        }
        catch (IllegalPropertySetDataException ex)
        {
            //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
            log.debug(ex);
            result = null;
        }*/
        return result;
    }
    
    
    /**
     * @param col
     * @param byTitle
     * @return
     */
    protected String getKeyForCol(ImportColumnInfo col, boolean byViewOrder)
    {
    	if (!byViewOrder)
    	{
    		return col.getColTitle();
    	} else
    	{
    		return POIFS_COL_KEY_PREFIX + col.getColInx();
    	}
    }
    
    /**
     * @param props
     * @return
     */
    protected boolean usesViewOrderKey(CustomProperties props)
    {
    	if (props != null && props.keySet().size() > 0)
    	{
    		String key = props.keySet().iterator().next().toString();
    		return key.startsWith(POIFS_COL_KEY_PREFIX);
    	} else
    	{
    		return false;
    	}
    }
    
    /**
     * @param cols
     * @return
     */
    protected Set<String> getDuplicatedColTitles(List<ImportColumnInfo> cols)
    {
    	Set<String> titles = new HashSet<String>();
    	Set<String> result = new HashSet<String>();
    	for (ImportColumnInfo col : cols)
    	{
    		if (titles.contains(col.getColTitle()))
    		{
    			result.add(col.getColTitle());
    		} else
    		{
    			titles.add(col.getColTitle());
    		}
    	}
    	return result;
    }

    protected Integer getIdxForPropKey(String propKey, boolean usesViewOrder, int i) {
        if (propKey != null) {
            if (usesViewOrder) {
                String sint = propKey.replace(POIFS_COL_KEY_PREFIX, "").trim();
                try {
                    return Integer.valueOf(sint);
                } catch (NumberFormatException x) {
                    //oh well
                }
            } else {
                if (propKey.toLowerCase().trim().startsWith("period") || propKey.toLowerCase().trim().startsWith("epoch") || propKey.toLowerCase().trim().startsWith("age") || propKey.toLowerCase().trim().startsWith("max uncer")) {
                    System.out.println(propKey);
                }
                for (int c = 0; c < colInfo.size(); c++) {
                    ImportColumnInfo ci = colInfo.get(c);
                    if (ci.colName.equalsIgnoreCase(propKey.trim())) {
                        return c;
                    }
                }
                System.out.println("No match for:" + propKey);
                return i;
            }
        }
        return null;
    }

    /**
     * @param props
     * @param cols
     * @return
     */
    protected boolean doReadMappings(CustomProperties props, List<ImportColumnInfo> cols) {
    	boolean usesViewOrder = usesViewOrderKey(props);
        if (props != null && ((usesViewOrder && props.size() == cols.size()) || !usesViewOrder)) {
        	if (usesViewOrder) {
            	for (Map.Entry p : props.entrySet()) {
            	    Integer colIdx = getIdxForPropKey(p.getKey().toString(), usesViewOrder, -1);
            	    if (colIdx != null && colIdx >= 0 && colIdx < cols.size()) {
                        String[] mapping = ((String)p.getValue()).split("\t");
                        if (!mapping[0].equals(cols.get(colIdx).getColTitle())) {
                            return false;
                        }
                    }
                }
        	}
        	return true;
        } else {
        	return false;
        }
    }
    
    /**
     * @param poifs
     * 
     * Reads workbench mappings from the XLS file.
    */
    protected void readMappingsHSSF(final POIFSFileSystem poifs) {
        DocumentSummaryInformation dsi = getDocSummary(poifs);
        if (dsi != null) {
            CustomProperties props = dsi.getCustomProperties();
            if (doReadMappings(props, colInfo)) {
                boolean usesViewOrder = usesViewOrderKey(props);
                int mapIdxOffset = usesViewOrder ? 1 : 0;
                Set<String> dupedCols = usesViewOrder ? new HashSet<>() : getDuplicatedColTitles(colInfo);
                int i = 0;
                for (Map.Entry p : props.entrySet()) {
                    Integer colIdx = getIdxForPropKey(p.getKey().toString(), usesViewOrder, i++);
                    if (!usesViewOrder || (colIdx != null && colIdx >= 0 && colIdx < colInfo.size())) {
                        ImportColumnInfo col = colInfo.get(colIdx);
                        if (!dupedCols.contains(col.getColTitle())) {
                            String[] mapping = ((String) p.getValue()).split("\t");
                            col.setMapToTbl(mapping[mapIdxOffset + 0]);
                            col.setMapToFld(mapping[mapIdxOffset + 1]);
                            if (mapping.length == 7 + mapIdxOffset) {
                                col.setFormXCoord(Integer.valueOf(mapping[mapIdxOffset + 2]));
                                col.setFormYCoord(Integer.valueOf(mapping[mapIdxOffset + 3]));
                                if (StringUtils.isNotBlank(mapping[mapIdxOffset + 4])) {
                                    col.setCaption(mapping[mapIdxOffset + 4]);
                                }
                                col.setFrmFieldType(Integer.valueOf(mapping[mapIdxOffset + 5]));
                                col.setFrmMetaData(mapping[mapIdxOffset + 6]);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void readMappingsXSSF(XSSFWorkbook workbook) {
        try {
            Sheet mappingsSheet = workbook.getSheetAt(1);
            Iterator<?> rows = mappingsSheet.rowIterator();
            int mapIdxOffset = 1;
            while (rows.hasNext()) {
                Row row = (Row) rows.next();
                String mapDef = row.getCell(0).getStringCellValue();
                String[] firstCut = mapDef.split(",");
                Integer wbColIdx = Integer.valueOf(firstCut[0]);
                ImportColumnInfo col = colInfo.get(wbColIdx);
                String[] mapping = firstCut[1].split("\t");
                col.setMapToTbl(mapping[mapIdxOffset + 0]);
                col.setMapToFld(mapping[mapIdxOffset + 1]);
                if (mapping.length == 7 + mapIdxOffset) {
                    col.setFormXCoord(Integer.valueOf(mapping[mapIdxOffset + 2]));
                    col.setFormYCoord(Integer.valueOf(mapping[mapIdxOffset + 3]));
                    if (StringUtils.isNotBlank(mapping[mapIdxOffset + 4])) {
                        col.setCaption(mapping[mapIdxOffset + 4]);
                    }
                    col.setFrmFieldType(Integer.valueOf(mapping[mapIdxOffset + 5]));
                    col.setFrmMetaData(mapping[mapIdxOffset + 6]);
                }
            }
        } catch (Exception x) {
            //no mappings or bad mappings
            if (!(x instanceof IllegalArgumentException)) {
                log.error(x);
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
            Workbook workBook = WorkbookFactory.create(input);
            Sheet       sheet    = workBook.getSheetAt(0);

            // Calculate the number of rows and columns
            colInfo = new Vector<ImportColumnInfo>(16);

            Hashtable<Integer, Boolean> colTracker = new Hashtable<Integer, Boolean>();

            boolean firstRow = true;
            int   col      = 0;
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
            @SuppressWarnings("unchecked")
            Iterator<Row> rows =  sheet.rowIterator();
            while (rows.hasNext()) {
                Row row = rows.next();
                if (firstRow || numRows == 1) {
                    // Iterate over each cell in the row and print out the cell's content
                    int colNum = 0;
                    int maxSize = Math.max(row.getPhysicalNumberOfCells(), row.getLastCellNum());
                    while (colNum < maxSize) {
                        if (emptyCols.indexOf(new Integer(colNum)) == -1) {
                            ImportColumnInfo.ColumnType disciplinee = ImportColumnInfo.ColumnType.Integer;
                            String value = null;
                            boolean skip = false;
                            Cell cell = row.getCell(colNum);
                            if (cell == null) {
                                //assuming numRows == 1 or not firstRowHasHeaders.
                                //the call to checkHeadsAndCols would have already blank headers.
                                value = "";
                                disciplinee = ImportColumnInfo.ColumnType.String;
                            }
                            else {
                                CellType cellType = cell.getCellType();
                                if (cellType == CellType.NUMERIC) {
                                    double numeric = cell.getNumericCellValue();
                                    value = Double.toString(numeric);
                                    disciplinee = ImportColumnInfo.ColumnType.Double;
                                } else if (cellType == CellType.STRING) {
                                    RichTextString richVal = cell.getRichStringCellValue();
                                    value = richVal.getString().trim();
                                    disciplinee = ImportColumnInfo.ColumnType.String;
                                } else if (cellType == CellType.BLANK) {
                                    value = "";
                                    disciplinee = ImportColumnInfo.ColumnType.String;
                                } else if (cellType == CellType.BOOLEAN) {
                                    boolean bool = cell.getBooleanCellValue();
                                    value = Boolean.toString(bool);
                                    disciplinee = ImportColumnInfo.ColumnType.Boolean;
                                } else {
                                    skip = true;
                                }
                            }
                            if (numRows == 1 && !skip) {
                                colInfo.get(col).setData(value);
                                col++;
                            } else if (!skip) {
                                if (firstRowHasHeaders) {
                                    colInfo.add(new ImportColumnInfo(colNum, disciplinee, value, value, null, null,
                                            null));
                                    colTracker.put(col, true);
                                } else {
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
            if (workBook instanceof HSSFWorkbook) {
                try {
                    POIFSFileSystem fs = new POIFSFileSystem(externalFile);
                    readMappingsHSSF(fs);
                } catch (IOException x) {
                     log.warn("Unable to check for embedded WorkBench mappings in " + externalFile.getName(), x);
                     UIRegistry.displayInfoMsgDlgLocalized("ConfigureXLS_ERROR_CHECKING_FOR_MAPPINGS", externalFile.getName());
                }
            } else {
                readMappingsXSSF((XSSFWorkbook)workBook);
            }
            status = Status.Valid;
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureXLS.class, ex);
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
