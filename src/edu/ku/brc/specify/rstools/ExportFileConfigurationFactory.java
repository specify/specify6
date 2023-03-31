/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.rstools;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import edu.ku.brc.specify.tasks.subpane.wb.ConfigureCSV;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataIFace;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureXLS;

/**
 * 
 * @author timbo
 * @code_status Alpha
 */
public class ExportFileConfigurationFactory
{
    public static String XLS_MIME_TYPE = "binary/xls";
    public static String XLSX_MIME_TYPE = "binary/xlsx";
    public static String CSV_MIME_TYPE = "text/csv";
    
    // This should be a WeakReference somday
    protected static Vector<ExportableType> exportTypes = null;
    
    protected ExportFileConfigurationFactory()
    {
        //blank
    }

    public static ConfigureExternalDataIFace getConfiguration(final Properties props) {
        if (props.getProperty("mimetype", XLSX_MIME_TYPE) == XLSX_MIME_TYPE) {
            return new ConfigureXLS(props);
        } else if (props.getProperty("mimetype") == XLS_MIME_TYPE) {
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
            //exportTypes.add(factory.new ExportableType("Excel 97-2003", XLS_MIME_TYPE, "xls"));
            exportTypes.add(factory.new ExportableType("Excel", XLSX_MIME_TYPE, "xlsx"));
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
