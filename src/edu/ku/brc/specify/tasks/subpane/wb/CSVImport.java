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
import java.util.Set;
import java.util.Vector;

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
    ConfigureCSVImport config;
    
    public CSVImport(ConfigureDataImport config)
    {
        setConfig(config);
    }

    public ConfigureDataImport getConfig()
    {
        return config;
    }

    /*
     * (non-Javadoc) Loads data from the file configured by the config member into a workbench.
     * @param Workbench - the workbench to getData into
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataImport#getData(edu.ku.brc.specify.datamodel.Workbench)
     */
    public void getData(Workbench workbench)
    {
        try
        {
            CsvReader csv = new CsvReader(new FileInputStream(config.getFile()), config
                    .getDelimiter(), config.getCharset());
            csv.setEscapeMode(config.getEscapeMode());

            Set<WorkbenchTemplateMappingItem> wbtmiSet = workbench.getWorkbenchTemplate()
                    .getWorkbenchTemplateMappingItems();
            Vector<WorkbenchTemplateMappingItem> wbtmiList = new Vector<WorkbenchTemplateMappingItem>();
            wbtmiList.addAll(wbtmiSet);
            Collections.sort(wbtmiList);

            if (config.getFirstRowHasHeaders())
            {
                csv.readHeaders();
            }
            while (csv.readRecord())
            {
                WorkbenchRow wbRow = workbench.addRow();
                for (int col = 0; col < csv.getColumnCount(); col++)
                {
                    wbRow.setData(csv.get(col), col);
                }
            }

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public void setConfig(ConfigureDataImport config)
    {
        this.config = (ConfigureCSVImport) config;
    }

}
