/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
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
                                wbRow.setData(truncateIfNecessary(csv.get(col), row, (short) col, ""), wbtmi.getViewOrder());
                            }
                        }
                    }
                    return status = this.truncations.size() == 0 ? DataImportIFace.Status.Valid : DataImportIFace.Status.Modified;
                }
            }
            
        } catch (IOException ex)
        {
           log.error(ex);
        }
        
        return status = DataImportIFace.Status.Error;
    }

    public void setConfig(final ConfigureExternalDataIFace config)
    {
        this.config = (ConfigureCSV) config;
    }

}
