/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.exporters.ExportFileConfigurationFactory;
import edu.ku.brc.specify.exporters.ExportToFile;
import edu.ku.brc.specify.tasks.ExportTask;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.ui.CommandAction;
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
        File[] backups = UIRegistry.getDefaultWorkingPathSubDir(backupSubDir, true).listFiles();
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

    protected static String getFileName(final Workbench workbench)
    {
        String result;
        File file;
        int tries = 0;
        do
        {
            result = getPrefix(workbench) + Math.round(Math.random() * 1000) + "_" + workbench.getName()
                    + ".xls";
            file = new File(result);
        } while (tries++ < 100 && file.exists());
        return result;
    }

    /**
     * loads workbench from the database and backs it up (exports to an xls file) in a subdir in the
     * default working Path, and deletes old backups if necessary.
     */
    public static void backupWorkbench(final long workbenchId, final WorkbenchTask task)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            Workbench workbench = session.get(Workbench.class, workbenchId);
            session.attach(workbench);
            workbench.forceLoad();
            session.close();
            session = null;

            String fileName = getFileName(workbench);

            Properties props = new Properties();
            props.setProperty("mimetype", ExportFileConfigurationFactory.XLS_MIME_TYPE);
            props.setProperty("fileName", UIRegistry.getDefaultWorkingPathSubDir(backupSubDir,
                    true)
                    + File.separator + fileName);

            CommandAction command = new CommandAction(ExportTask.EXPORT, ExportTask.EXPORT_LIST);
            command.setProperty("exporter", ExportToFile.class);
            command.setData(workbench.getWorkbenchRowsAsList());

            // XXX the command has to be sent synchronously so the backup happens before the save,
            // so when dispatchCommand goes asynchronous
            // more work will have to done here...
            task.sendExportCommand(props, workbench.getWorkbenchTemplate()
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
            log.error(ex);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
}
