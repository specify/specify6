/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.exporters;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.subpane.wb.CSVExport;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureCSV;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureXLS;
import edu.ku.brc.specify.tasks.subpane.wb.DataExport;
import edu.ku.brc.specify.tasks.subpane.wb.XLSExport;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ExportToFile implements RecordSetExporter
{    
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
        else
        {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#exportList(java.util.List)
     */
    public void exportList(List<?> data, Properties reqParams) throws Exception
    {
        DataExport exporter = buildExporter(reqParams);
        if (exporter != null)
        {
          try
          {
            exporter.writeData(data, (DataProviderSessionIFace) reqParams.get("session"), true);
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
    public void exportRecordSet(RecordSet data, Properties reqParams) throws Exception
    {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getDescription()
     */
    public String getDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getHandledClasses()
     */
    public Class<?>[] getHandledClasses()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getIconName()
     */
    public String getIconName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getName()
     */
    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
