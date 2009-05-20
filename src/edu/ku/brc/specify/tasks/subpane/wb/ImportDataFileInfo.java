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
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.apache.commons.io.FilenameUtils;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.rstools.ExportFileConfigurationFactory;
import edu.ku.brc.ui.BiColorTableCellRenderer;
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
        if (mimeType == ExportFileConfigurationFactory.XLS_MIME_TYPE)
        {
             config = new ConfigureXLS(file);
            if (config.getStatus() == ConfigureExternalDataIFace.Status.Valid)
            {
                importer = new XLSImport(config);
                isValid = true;
            }
            
        } else if (mimeType == ExportFileConfigurationFactory.CSV_MIME_TYPE)
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
        if (importer.getTruncations().size() > 0)
        {
            JPanel mainPane = new JPanel(new BorderLayout());
            JLabel msg      = createLabel(getResourceString("WB_TRUNCATIONS"));
            msg.setFont(msg.getFont().deriveFont(Font.BOLD));
            mainPane.add(msg, BorderLayout.NORTH);
            
            String[]   heads = new String[3];
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
                    vals[row][1] = String.valueOf(trunc.getCol() + 1);
                }
                vals[row++][2] = trunc.getExcluded();
            }

            JTable mods = new JTable(vals, heads);
            mods.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));

            mainPane.add(UIHelper.createScrollPane(mods), BorderLayout.CENTER);

            CustomFrame cwin = new CustomFrame(getResourceString(MODIFIED_IMPORT_DATA),
                                               CustomFrame.OKHELP, mainPane);
            cwin.setHelpContext("WorkbenchImportData"); //help context could be more specific
            cwin.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            UIHelper.centerAndShow(cwin);
        }
        if (importer.getMessages().size() > 0)
        {
            JPanel mainPane = new JPanel(new BorderLayout());
            JTextArea msgs = new JTextArea();
            msgs.setRows(importer.getMessages().size());
            for (String msg : importer.getMessages())
            {
                msgs.append(msg);
                msgs.append("\n");
            }
            mainPane.add(msgs, BorderLayout.CENTER);
            CustomFrame cwin = new CustomFrame(getResourceString(MODIFIED_IMPORT_DATA),
                                               CustomFrame.OKHELP, mainPane);
            cwin.setHelpContext("WorkbenchImportData"); //help context could be more specific
            cwin.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            UIHelper.centerAndShow(cwin);
        }
    }
    
    /**
     * @param workbench the workbench to be loaded
     * @param workbench
     * @return
     */
    public DataImportIFace.Status loadData(final Workbench workbench)
    {
        DataImportIFace.Status result = importer.getData(workbench);
        if (result == DataImportIFace.Status.Modified) 
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
            return ExportFileConfigurationFactory.XLS_MIME_TYPE;
            
        } else if (extension.equalsIgnoreCase("csv") || extension.equalsIgnoreCase("txt"))
        {
            return ExportFileConfigurationFactory.CSV_MIME_TYPE;
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
