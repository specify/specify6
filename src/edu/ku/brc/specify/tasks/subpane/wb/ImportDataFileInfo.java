/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.io.FilenameUtils;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.ui.CustomFrame;
import edu.ku.brc.ui.UIHelper;

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
    protected static String MODIFIED_IMPORT_DATA = "WB_MODIFIED_IMPORT_DATA";
    
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
     *  shows modified (truncated) data after import
     */
    protected void showModifiedData()
    {
        JPanel mainPane = new JPanel(new BorderLayout());
        JLabel msg = new JLabel(getResourceString("WB_TRUNCATIONS"));
        msg.setFont(msg.getFont().deriveFont(Font.BOLD));
        mainPane.add(msg, BorderLayout.NORTH);
        String[] heads = new String[3];
        String[][] vals = new String[importer.getTruncations().size()][3];
        heads[0] = getResourceString("WB_ROW");
        heads[1] = getResourceString("WB_COLUMN");
        heads[2] = getResourceString("WB_TRUNCATED");
        int row = 0;
        for (DataImportTruncation trunc : importer.getTruncations())
        {
            vals[row][0] = String.valueOf(trunc.getRow());
            vals[row][1] = trunc.getColHeader();
            if (vals[row][1].equals(""))
            {
                vals[row][1] = String.valueOf(trunc.getCol()+1);
            }
            vals[row++][2] = trunc.getExcluded();
        }
        
        JTable mods = new JTable(vals, heads);
        
        mainPane.add(new JScrollPane(mods), BorderLayout.CENTER);
        
        CustomFrame cwin = new CustomFrame(getResourceString(MODIFIED_IMPORT_DATA), CustomFrame.OKHELP, mainPane);
        cwin.setHelpContext("WorkbenchImportData"); //help context could be more specific
        UIHelper.centerAndShow(cwin);
    }
    
    /**
     * @param workbench the workbench to be loaded
     * @param workbench
     * @return
     */
    public DataImportIFace.Status loadData(final Workbench workbench)
    {
        DataImportIFace.Status result = importer.getData(workbench);
        if (result == DataImportIFace.Status.Modified && importer.getTruncations().size() > 0) 
        {
            showModifiedData();
        }
        return result;
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
