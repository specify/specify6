/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.rstools;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.subpane.wb.CSVExport;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureCSV;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureXLS;
import edu.ku.brc.specify.tasks.subpane.wb.DataExport;
import edu.ku.brc.specify.tasks.subpane.wb.XLSExport;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;

/**
 * 
 * @author timbo
 * 
 * @code_status Alpha
 */
public class ExportToFile implements RecordSetToolsIFace
{    
    public ExportToFile()
    {
        
    }
    
    protected DataExport buildExporter(Properties reqParams)
    {
        String mimeType = reqParams.getProperty("mimetype");
        if (mimeType == ExportFileConfigurationFactory.XLS_MIME_TYPE)
        {
            return new XLSExport(new ConfigureXLS(reqParams));
            
        } else if (mimeType == ExportFileConfigurationFactory.CSV_MIME_TYPE)
        {
             return new CSVExport(new ConfigureCSV(reqParams));
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#exportList(java.util.List)
     */
    public void processDataList(List<?> data, Properties reqParams) throws Exception
    {
        DataExport exporter = buildExporter(reqParams);
        if (exporter != null)
        {
            String     name      = FilenameUtils.getName(exporter.getConfig().getFileName());
            JStatusBar statusBar = UIRegistry.getStatusBar();
            if (statusBar != null)
            {
                statusBar.setText(String.format(UIRegistry.getResourceString("EXPORTING_TO"), new Object[] {name}));
            }
            try
            {
                exporter.writeData(data);
                
                if (statusBar != null)
                {
                    statusBar.setText(String.format(UIRegistry.getResourceString("EXPORTING_DONE"), new Object[] {name}));
                }
            }
            catch (IOException e)
            {
                throw(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#exportRecordSet(edu.ku.brc.specify.datamodel.RecordSet)
     */
    public void processRecordSet(RecordSet data, Properties reqParams) throws Exception
    {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getDescription()
     */
    public String getDescription()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getHandledClasses()
     */
    public Class<?>[] getHandledClasses()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getIconName()
     */
    public String getIconName()
    {
        return "AppIcon";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getName()
     */
    public String getName()
    {
        return UIRegistry.getResourceString("FILE");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#isVisible()
     */
    public boolean isVisible()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetToolsIFace#getTableIds()
     */
    public Integer[] getTableIds()
    {
        return new Integer[] {79};
    }
}
