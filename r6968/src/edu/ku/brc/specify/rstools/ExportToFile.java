/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import edu.ku.brc.dbsupport.RecordSetIFace;
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
    /**
     * 
     */
    public ExportToFile()
    {
        
    }
    
    /**
     * @param reqParams
     * @return
     */
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
            final String     name      = FilenameUtils.getName(exporter.getConfig().getFileName());
            final String msgKey = reqParams.getProperty("statusmsgkey") == null ? "EXPORTING_TO" : reqParams.getProperty("statusmsgkey");
            final String doneMsgKey = reqParams.getProperty("statusdonemsgkey") == null ? "EXPORTING_DONE" : reqParams.getProperty("statusdonemsgkey");
            final JStatusBar statusBar = UIRegistry.getStatusBar();
            if (statusBar != null)
            {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run()
                    {
                        statusBar.setText(String.format(UIRegistry.getResourceString(msgKey), new Object[] {name}));
                    }
                });
            }
            try
            {
                exporter.writeData(data);
                
                if (statusBar != null)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            statusBar.setText(String.format(UIRegistry
                                    .getResourceString(doneMsgKey), new Object[] { name }));
                        }
                    });
                }
            }
            catch (IOException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExportToFile.class, e);
                throw(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.rstools.RecordSetToolsIFace#processRecordSet(edu.ku.brc.dbsupport.RecordSetIFace, java.util.Properties)
     */
    public void processRecordSet(RecordSetIFace data, Properties reqParams) throws Exception
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
    public int[] getTableIds()
    {
        return new int[] {79};
    }
}
