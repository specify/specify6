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
    protected DataImport importer;

    public ImportDataFileInfo(final File file)
    {
        String mimeType = getMimeType(file);
        if (mimeType == "XLS")
        {
            importer = new XLSImport(new ConfigureXLSImport(file));
        } else if (mimeType == "CSV")
        {
            importer = new CSVImport(new ConfigureCSVImport(file));
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

    private String getMimeType(final File file)
    {
        String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        if (extension.equals("xls"))
        {
            return "XLS";
            
        } else if (extension.equals("csv"))
        {
            return "CSV";
        }
        return "";
    }
}
