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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.csvreader.CsvReader;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * Imports csv data into a workbench
 */
public class CSVImport extends DataImport implements DataImportIFace
{
    private static final Logger log = Logger.getLogger(CSVImport.class);
    
    protected ConfigureExternalDataIFace config;

    
    public CSVImport(final ConfigureExternalDataIFace config)
    {
        this.config = config;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataImportIFace#getConfig()
     */
    public ConfigureExternalDataIFace getConfig()
    {
        return config;
    }

    /*
     * (non-Javadoc) Loads data from the file configured by the config member into a workbench.
     * @param Workbench - the workbench to getData into
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataImportIFace#getData(edu.ku.brc.specify.datamodel.Workbench)
     */
    public DataImportIFace.Status getData(final Workbench workbench)
    {
        try
        {
            ConfigureCSV configCSV = null;
            if (config instanceof ConfigureCSV)
            {
                configCSV = (ConfigureCSV)config;
                
                if (configCSV.getStatus() == ConfigureExternalDataIFace.Status.Valid)
                {
                    CsvReader csv = new CsvReader(new FileInputStream(config.getFile()), configCSV.getDelimiter(), configCSV.getCharset());

                    csv.setEscapeMode(configCSV.getEscapeMode());
                    csv.setTextQualifier(configCSV.getTextQualifier());
        
                    Set<WorkbenchTemplateMappingItem>    wbtmiSet  = workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems();
                    Vector<WorkbenchTemplateMappingItem> wbtmiList = new Vector<WorkbenchTemplateMappingItem>();
                    wbtmiList.addAll(wbtmiSet);
                    Collections.sort(wbtmiList);
        
                    this.truncations.clear();
                    
                    if (config.getFirstRowHasHeaders())
                    {
                        csv.readHeaders();
                    }else
                    {
                        csv.setHeaders(configCSV.setupHeaders());
                    }
                    
                    //add additional dummy column headers
                    String[] newHeaders = null;
                    if(configCSV.getNumOfColsToAppend() > csv.getColumnCount())
                    {
                            newHeaders = configCSV.padColumnHeaders(configCSV.getNumOfColsToAppend(), csv.getHeaders());
                            csv.setHeaders(newHeaders);   
                    }
                    
                    // Create hash of the column number so later we can easily 
                    // look up whether this column should be used.
                    Hashtable<Short, WorkbenchTemplateMappingItem> colHash = new Hashtable<Short, WorkbenchTemplateMappingItem>();
                    for (WorkbenchTemplateMappingItem wbtmi : wbtmiList)
                    {
                        if (wbtmi.getOrigImportColumnIndex() > -1)
                        {
                            colHash.put(wbtmi.getOrigImportColumnIndex(), wbtmi);
                        }
                        else if (wbtmi.getViewOrder() != null)
                        {
                            if (wbtmi.getViewOrder() != -1)
                            {
                                colHash.put(wbtmi.getViewOrder(), wbtmi);
                            }
                        }
                    }
                    
                    int row = config.getFirstRowHasHeaders() ? 1 : 0;
                    while (csv.readRecord())
                    {
                        WorkbenchRow wbRow = workbench.addRow();
                        row++;
                        for (int col = 0; col < csv.getColumnCount(); col++)
                        {
                            // Skip the column if it isn't found in the hash
                            WorkbenchTemplateMappingItem wbtmi = colHash.get((short) col);
                            if (wbtmi != null)
                            {
                                wbRow.setData(truncateIfNecessary(csv.get(col), config
                                        .getFirstRowHasHeaders() ? row - 1 : row, wbtmi), wbtmi.getViewOrder(), true);
                            }
                        }
                    }
                    return status = this.truncations.size() == 0 ? DataImportIFace.Status.Valid : DataImportIFace.Status.Modified;
                }
            }
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CSVImport.class, ex);
           log.error(ex);
        }
        
        return status = DataImportIFace.Status.Error;
    }

    public void setConfig(final ConfigureExternalDataIFace config)
    {
        this.config = config;
    }

}
