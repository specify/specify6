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
    
    protected DataImport importer;

    public ImportDataFileInfo(final File file)
    {
        String mimeType = getMimeType(file);
        if (mimeType == XLS_MIME_TYPE)
        {
            importer = new XLSImport(new ConfigureXLS(file));
            
        } else if (mimeType == CSV_MIME_TYPE)
        {
            importer = new CSVImport(new ConfigureCSV(file));
        }
    }

    /**
     * @param workbench the workbench to be loaded
     */
    public void loadData(final Workbench workbench)
    {
        importer.getData(workbench);
    }

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
        if (extension.equals("xls"))
        {
            return XLS_MIME_TYPE;
            
        } else if (extension.equals("csv"))
        {
            return CSV_MIME_TYPE;
        }
        return "";
    }
}
