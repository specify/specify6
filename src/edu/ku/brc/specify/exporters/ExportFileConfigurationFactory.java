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

import java.util.Properties;

import edu.ku.brc.specify.tasks.subpane.wb.ConfigureCSV;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalData;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureXLS;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ExportFileConfigurationFactory
{
    public static String XLS_MIME_TYPE = "bindary/xls";
    public static String CSV_MIME_TYPE = "text/csv";

    public static ConfigureExternalData getConfiguration(Properties props)
    {
        if (props.getProperty("mimetype", XLS_MIME_TYPE) == XLS_MIME_TYPE)
        {
            return new ConfigureXLS(props);
        }
        return new ConfigureCSV(props);
        
    }
}
