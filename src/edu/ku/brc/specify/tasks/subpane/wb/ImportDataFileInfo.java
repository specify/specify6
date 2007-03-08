/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.io.File;
import java.util.Vector;

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
    DataImport importer;

    public ImportDataFileInfo(File file)
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
     * @param workbench -
     *            the workbench to be loaded
     */
    public void loadData(Workbench workbench)
    {
        importer.getData(workbench);
    }

    public Vector<ImportColumnInfo> getColInfo()
    {
        return importer.getConfig().getColInfo();
    }

    private String getMimeType(File file)
    {
        String path = file.getAbsolutePath().toLowerCase();
        if (path.endsWith("xls"))
        {
            return "XLS";
        } else if (path.endsWith("csv"))
        {
            return "CSV";
        } else
        {
            return "";
        }
        // return AttachmentUtils.getMimeType(file.getName());
    }
}
