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
public class CSVImport implements DataImport
{
    private static final Logger log = Logger.getLogger(CSVImport.class);
    
    protected ConfigureCSV config;
    
    
    public CSVImport(final ConfigureExternalData config)
    {
        setConfig(config);
    }

    public ConfigureExternalData getConfig()
    {
        return config;
    }

    /*
     * (non-Javadoc) Loads data from the file configured by the config member into a workbench.
     * @param Workbench - the workbench to getData into
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataImport#getData(edu.ku.brc.specify.datamodel.Workbench)
     */
    public void getData(final Workbench workbench)
    {
        try
        {
            CsvReader csv = new CsvReader(new FileInputStream(config.getFile()), config.getDelimiter(), config.getCharset());
            csv.setEscapeMode(config.getEscapeMode());

            Set<WorkbenchTemplateMappingItem>    wbtmiSet  = workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems();
            Vector<WorkbenchTemplateMappingItem> wbtmiList = new Vector<WorkbenchTemplateMappingItem>();
            wbtmiList.addAll(wbtmiSet);
            Collections.sort(wbtmiList);

            if (config.getFirstRowHasHeaders())
            {
                csv.readHeaders();
            }
            
            // Create hash of the column number so later we can easily 
            // look up whether this column should be used.
            Hashtable<Integer, WorkbenchTemplateMappingItem> colHash = new Hashtable<Integer, WorkbenchTemplateMappingItem>();
            for (WorkbenchTemplateMappingItem wbtmi : wbtmiList)
            {
                if (wbtmi.getDataColumnIndex() > -1)
                {
                    colHash.put(wbtmi.getDataColumnIndex(), wbtmi);
                }
            }
            
            while (csv.readRecord())
            {
                WorkbenchRow wbRow = workbench.addRow();
                for (int col = 0; col < csv.getColumnCount(); col++)
                {
                    // Skip the column if it isn't found in the hash
                    WorkbenchTemplateMappingItem wbtmi = colHash.get(col);
                    if (wbtmi != null)
                    {
                        wbRow.setData(csv.get(col), wbtmi.getViewOrder());
                    }
                }
            }

        } catch (IOException ex)
        {
           log.error(ex);
        }
    }

    public void setConfig(final ConfigureExternalData config)
    {
        this.config = (ConfigureCSV) config;
    }

}
