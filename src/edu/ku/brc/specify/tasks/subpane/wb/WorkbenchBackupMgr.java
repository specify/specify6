/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.rstools.ExportFileConfigurationFactory;
import edu.ku.brc.specify.rstools.ExportToFile;
import edu.ku.brc.specify.tasks.PluginsTask;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIHelper.OSTYPE;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 */
public class WorkbenchBackupMgr
{
    private static final Logger log            = Logger.getLogger(WorkbenchPaneSS.class);
    private static int          maxBackupCount = 5;
    private static String       backupSubDir   = "Backups";

    protected static String getPrefix(final Workbench workbench)
    {
        return "WB" + workbench.getId().toString() + "_";
    }

    protected static Vector<File> getExistingBackups(final Workbench workbench)
    {
        File[] backups = UIRegistry.getAppDataSubDir(backupSubDir, true).listFiles();
        Vector<File> result = new Vector<File>(maxBackupCount);
        for (File f : backups)
        {
            if (f.getName().startsWith(getPrefix(workbench)))
            {
                result.add(f);
            }
        }
        return result;
    }

    protected static File getEarliestBackup(final Vector<File> backups)
    {
        Date earliest = null;
        File earliestFile = null;
        for (File f : backups)
        {
            Date current = new Date(f.lastModified());
            if (earliest == null || earliest.after(current))
            {
                earliest = current;
                earliestFile = f;
            }
        }
        return earliestFile;
    }

    /**
     * if more than maxBackupCount backups exist then the earliest one will be removed.
     * 
     * @param workbench - backups for this workbench will be cleaned up
     */
    protected static void cleanupBackups(final Workbench workbench)
    {
        Vector<File> backups = getExistingBackups(workbench);
        if (backups.size() > maxBackupCount)
        {
            File earliest = getEarliestBackup(backups);
            if (earliest != null)
            {
                if (!earliest.delete())
                {
                    log.error("Unable to delete backup: " + earliest.getName());
                }
            }
            else
            {
                log.error("Unable to delete backup");
            }
        }
    }
    
    protected static String getNameForBackupFileName(Workbench workbench) {
    
    	String invalidChars;
    	UIHelper.OSTYPE os = UIHelper.getOSType();
    	if (os.equals(OSTYPE.Windows)) {
    	    invalidChars = "\\/:*?\"<>|";
    	} else if (os.equals(OSTYPE.MacOSX)) {
    	    invalidChars = "/:";
    	} else { // assume Unix/Linux
    	    invalidChars = "/";
    	}
    	char[] chars = workbench.getName().toCharArray();
    	for (int i = 0; i < chars.length; i++) {
    	    if ((invalidChars.indexOf(chars[i]) >= 0) // OS-invalid
    	        || (chars[i] < '\u0020') // ctrls
    	        || (chars[i] > '\u007e' && chars[i] < '\u00a0') // ctrls
    	    ) {
    	        chars[i] = '_';
    	    }
    	}
    	return new String(chars);

    }
    
    protected static String getFileName(final Workbench workbench)
    {
        String result;
        File file;
        int tries = 0;
        do
        {
            result = getPrefix(workbench) + Math.round(Math.random() * 1000) + "_" + getNameForBackupFileName(workbench)
                    + ".xls";
            file = new File(result);
        } while (tries++ < 100 && file.exists());
        return result;
    }

    /**
     * loads workbench from the database and backs it up (exports to an xls file) in a subdir in the
     * default working Path, and deletes old backups if necessary.
     */
    public static String backupWorkbench(final Object toBackup)
    {
        String backupName = null;
        try
        {
            Workbench workbench = null;
            if (toBackup instanceof Workbench)
            {
                workbench = (Workbench )toBackup;
            }
            else
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
                    workbench = session.get(Workbench.class, (Integer )toBackup);
                    workbench.forceLoad();
                }
                finally
                {
                    session.close();
                    session = null;
                }
            }

            String fileName = getFileName(workbench);

            backupName = UIRegistry.getAppDataSubDir(backupSubDir, true) + File.separator + fileName;
            
            Properties props = new Properties();
            props.setProperty("mimetype", ExportFileConfigurationFactory.XLS_MIME_TYPE);
            props.setProperty("fileName", backupName);
 
            CommandAction command = new CommandAction(PluginsTask.PLUGINS, PluginsTask.EXPORT_LIST);
            command.setProperty("tool", ExportToFile.class);
            command.setProperty("statusmsgkey", "WB_BACKUP_TO");
            command.setProperty("statusdonemsgkey", "WB_BACKUP_TO_DONE");
            List<?> rowData = workbench.getWorkbenchRowsAsList();
            Vector<Object> exportData = new Vector<Object>(rowData.size() + 1);
            exportData.add(workbench.getWorkbenchTemplate());
            exportData.addAll(rowData);
            command.setData(exportData);

            // XXX the command has to be sent synchronously so the backup happens before the save,
            // so when dispatchCommand goes asynchronous
            // more work will have to done here...
            WorkbenchTask.sendExportCommand(props, workbench.getWorkbenchTemplate()
                    .getWorkbenchTemplateMappingItems(), command);

            // XXX again assuming command was dispatched synchronously...
            // Clear the status bar message about successful 'export'? - but what if error during
            // backup?,
            // and remove old backups if necessary.
            UIRegistry.getStatusBar().setText("");
            cleanupBackups(workbench);
        }
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchBackupMgr.class, ex);
            log.error(ex);
        }
        return backupName;
    }
}
