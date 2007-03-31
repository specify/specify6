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

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import edu.ku.brc.specify.tasks.subpane.wb.ConfigureCSV;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataIFace;
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
    
    // This should be a WeakReference somday
    protected static Vector<ExportableType> exportTypes = null;
    
    protected ExportFileConfigurationFactory()
    {
        
    }

    public static ConfigureExternalDataIFace getConfiguration(Properties props)
    {
        if (props.getProperty("mimetype", XLS_MIME_TYPE) == XLS_MIME_TYPE)
        {
            return new ConfigureXLS(props);
        }
        return new ConfigureCSV(props);
    }
    
    /**
     * Returns a list of Localized types of exports.
     * @return the list
     */
    public static List<ExportableType> getExportList()
    {
        if (exportTypes == null)
        { 
            ExportFileConfigurationFactory factory = new ExportFileConfigurationFactory(); // cheesey I know
            exportTypes = new Vector<ExportableType>();
            exportTypes.add(factory.new ExportableType("Excel", XLS_MIME_TYPE, "xls"));
            exportTypes.add(factory.new ExportableType("CSV", CSV_MIME_TYPE, "csv"));
        }

        return exportTypes;
    }
    
    //--------------------------------------------------------------
    // Inner Class
    //--------------------------------------------------------------
    public class ExportableType 
    {
        protected String caption;
        protected String mimeType;
        protected String extension;
        
        public ExportableType(String caption, String mimeType, String extension)
        {
            super();
            this.caption = caption;
            this.mimeType = mimeType;
            this.extension = extension;
        }
        public String getCaption()
        {
            return caption;
        }
        public String getMimeType()
        {
            return mimeType;
        }
        public String getExtension()
        {
            return extension;
        }
        @Override
        public String toString()
        {
            return caption;
        }
    }
}
