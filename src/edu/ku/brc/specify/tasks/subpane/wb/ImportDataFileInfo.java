/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.io.File;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;

import edu.ku.brc.specify.datamodel.Workbench;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * Imports csv and xls data into workbenches.
 */
public class ImportDataFileInfo
{
    protected static String XLS_MIME_TYPE = "bindary/xls";
    protected static String CSV_MIME_TYPE = "text/csv";
    
    protected DataImportIFace importer;
    
    protected ConfigureExternalDataBase config;

    public ImportDataFileInfo()
    {
        // no-op
    }
    
    /**
     * Loads a file and reads aprse it for columns and data.
     * @param file the file
     * @return true if it was processed correctly
     */
    public boolean load(final File file)
    {
        importer = null;
        
        boolean isValid = false;
        String mimeType = getMimeType(file);
        if (mimeType == XLS_MIME_TYPE)
        {
             config = new ConfigureXLS(file);
            if (config.getStatus() == ConfigureExternalDataIFace.Status.Valid)
            {
                importer = new XLSImport(config);
                isValid = true;
            }
            
        } else if (mimeType == CSV_MIME_TYPE)
        {
             config = new ConfigureCSV(file);
            if (config.getStatus() == ConfigureExternalDataIFace.Status.Valid)
            {
                importer = new CSVImport(config);
                isValid = true;
            } 
                
        } else
        {
            isValid = false;
        }
        return isValid;
    }

    /**
     * @param workbench the workbench to be loaded
     * @param workbench
     * @return
     */
    public DataImportIFace.Status loadData(final Workbench workbench)
    {
        return importer.getData(workbench);
    }

    /**
     * @return
     */
    public Vector<ImportColumnInfo> getColInfo()
    {
        return importer.getConfig().getColInfo();
    }

    /**
     * Returns mime type for an extension.
     * @param file the file to check 
     * @return the mimeType;
     */
    private String getMimeType(final File file)
    {
        String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        if (extension.equalsIgnoreCase("xls"))
        {
            return XLS_MIME_TYPE;
            
        } else if (extension.equalsIgnoreCase("csv"))
        {
            return CSV_MIME_TYPE;
        }
        return "";
    }

    /**
     * @return the importer
     */
    public DataImportIFace getImporter()
    {
        return importer;
    }

    /**
     * @return the config
     */
    public ConfigureExternalDataBase getConfig()
    {
        return config;
    }
}
