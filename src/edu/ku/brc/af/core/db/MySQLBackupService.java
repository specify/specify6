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
package edu.ku.brc.af.core.db;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.FileDialog;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.BackupCompareDlg;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.helpers.ZipFileHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

/**
 * Backups a MySQL database use command line tools mysqldump and restores using mysql.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Sep 2, 2008
 *
 */
public class MySQLBackupService extends BackupServiceFactory
{
    private static final Logger log = Logger.getLogger(MySQLBackupService.class);
    
    private final String WEEKLY_PREF      = "LAST.BACKUP.WEEKLY";
    private final String MONTHLY_PREF     = "LAST.BACKUP.MONS";
    private final String RESTORE_COMPLETE = "MySQLBackupService.RESTORE_COMPLETE";

    private final String STATUSBAR_NAME = "BackUp";
    private final String MYSQLDUMP_LOC  = "mysqldump.location";
    private final String MYSQL_LOC      = "mysql.location";
    private final String MYSQLBCK_LOC   = "backup.location";
    private final String MEGS           = "MEGS";             // Used in Backup PropretyNotifications
    
    private int    numTables;
    private String errorMsg      = null;
    
    /**
     * 
     */
    public MySQLBackupService()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#getNumberofTables()
     */
    public int getNumberofTables()
    {
        Connection dbConnection = null;
        Statement  dbStatement = null;
        try
        {
            dbConnection = DBConnection.getInstance().createConnection();
            if (dbConnection != null)
            {
                dbStatement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = dbStatement.executeQuery("SHOW TABLES");
                
                ResultSetMetaData metaData = resultSet.getMetaData();
                numTables = 0;
                while (resultSet.next())
                {
                    for (int i=0;i<metaData.getColumnCount();i++)
                    {
                        numTables++;
                    }
                }
                resultSet.close();
                return numTables;
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                if (dbStatement != null)
                {
                    dbStatement.close();
                }
                if (dbConnection != null)
                {
                    dbConnection.close();
                }
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        
        return -1;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#getTableNames()
     */
    @Override
    public Vector<String> getTableNames()
    {
        Vector<String> tablesNames = new Vector<String>();
        
        Connection dbConnection = null;
        Statement  dbStatement = null;
        try
        {
            dbConnection = DBConnection.getInstance().createConnection();
            if (dbConnection != null)
            {
                dbStatement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = dbStatement.executeQuery("show tables");
                
                ResultSetMetaData metaData = resultSet.getMetaData();
                while (resultSet.next())
                {
                    for (int i=0;i<metaData.getColumnCount();i++)
                    {
                        String name = resultSet.getString(i+1);
                        tablesNames.add(name);
                    }
                }
                resultSet.close();
                return tablesNames;
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                if (dbStatement != null)
                {
                    dbStatement.close();
                }
                if (dbConnection != null)
                {
                    dbConnection.close();
                }
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#doBackUp()
     */
    @Override
    public void doBackUp()
    {
        checkForBackUp(false, true);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#doBackUp(java.beans.PropertyChangeListener)
     */
    @Override
    public void doBackUp(final PropertyChangeListener pcl)
    {
        doBackUp(false, false, pcl);
    }

    /**
     * Does the backup on a SwingWorker Thread.
     * @param isMonthly whether it is a monthly backup
     * @param doSendAppExit requests sending an application exit command when done
     * @return true if the prefs are set up and there were no errors before the SwingWorker thread was started
     */
    private boolean doBackUp(final boolean isMonthly, 
                             final boolean doSendAppExit,
                             final PropertyChangeListener propChgListener)
    {
        AppPreferences remotePrefs = AppPreferences.getLocalPrefs();
        
        final String mysqldumpLoc = remotePrefs.get(MYSQLDUMP_LOC, getDefaultMySQLDumpLoc());
        final String backupLoc    = remotePrefs.get(MYSQLBCK_LOC,  getDefaultBackupLoc());
        
        if (!(new File(mysqldumpLoc)).exists())
        {
            UIRegistry.showLocalizedError("MySQLBackupService.MYSQL_NO_DUMP", mysqldumpLoc);
            if (propChgListener != null)
            {
                propChgListener.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, ERROR, 0, 1));
            }
            return false;
        }
        
        File backupDir = new File(backupLoc);
        if (!backupDir.exists())
        {
            if (!backupDir.mkdir())
            {
                UIRegistry.showLocalizedError("MySQLBackupService.MYSQL_NO_BK_DIR", backupDir.getAbsoluteFile());
                if (propChgListener != null)
                {
                    propChgListener.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, ERROR, 0, 1));
                }
                return false;  
            }
        }
        
        errorMsg = null;
        
        final String databaseName = DBConnection.getInstance().getDatabaseName();
        
        getNumberofTables();
        
        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
        {
            protected String fullPath = null;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                FileOutputStream backupOut = null;
                try
                {
                    Thread.sleep(100);
                    
                    // Create output file
                    SimpleDateFormat sdf      = new SimpleDateFormat("yyyy_MM_dd_kk_mm_ss");
                    String           fileName = sdf.format(Calendar.getInstance().getTime()) + (isMonthly ? "_monthly" : "") + ".sql";
                    
                    fullPath = backupLoc + File.separator + fileName;
                    
                    File file     = new File(fullPath);
                    backupOut     = new FileOutputStream(file);
                    
                    writeStats(getCollectionStats(getTableNames()), getStatsName(fullPath));
                    
                    String userName = DBConnection.getInstance().getUserName();
                    String password = DBConnection.getInstance().getPassword();
                    
                    if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password))
                    {
                        Pair<String, String> up = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
                        if (up != null &&  up.first != null && up.second != null)
                        {
                            userName = up.first;
                            password = up.second;
                        }
                    }
                    
                    String port   = DatabaseDriverInfo.getDriver(DBConnection.getInstance().getDriverName()).getPort();
                    String server = DBConnection.getInstance().getServerName();
                    
                    String cmdLine  = String.format("%s -u %s --password=%s --host=%s %s %s", mysqldumpLoc, userName, password, server, (port != null ? ("--port="+port) : ""), databaseName);
                    String[] args   = StringUtils.split(cmdLine, ' ');
                    Process process = Runtime.getRuntime().exec(args);
                    
                    InputStream input = process.getInputStream();
                    byte[] bytes = new byte[8192*2];
                    
                    double oneMeg     = (1024.0 * 1024.0);
                    long   dspMegs    = 0;
                    long   totalBytes = 0;
                     
                    do
                    {
                        int numBytes = input.read(bytes, 0, bytes.length);
                        totalBytes += numBytes;
                        if (numBytes > 0)
                        {
                            long megs = (long)(totalBytes / oneMeg);
                            if (megs != dspMegs)
                            {
                                dspMegs = megs;
                                long megsWithTenths = (long)((totalBytes * 10.0) / oneMeg);
                                firePropertyChange(MEGS, 0, megsWithTenths);
                            }
                            
                            backupOut.write(bytes, 0, numBytes);
                            
                        } else
                        {
                            break;
                        }
                        
                    } while(true);
                    
                    StringBuilder sb = new StringBuilder();
                    
                    String line;
                    BufferedReader errIn = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((line = errIn.readLine()) != null)
                    {
                        //System.err.println(line);
                        if (line.startsWith("ERR") || StringUtils.contains(line, "Got error"))
                        {
                            sb.append(line);
                            sb.append("\n");
                            
                            if (StringUtils.contains(line, "1044") && 
                                StringUtils.contains(line, "LOCK TABLES"))
                            {
                                sb.append("\n");
                                sb.append(UIRegistry.getResourceString("MySQLBackupService.LCK_TBL_ERR"));
                                sb.append("\n");
                            }
                        }
                    }
                    errorMsg = sb.toString();
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    errorMsg = ex.toString();
                    UIRegistry.showLocalizedError("MySQLBackupService.EXCP_BK");
                    
                } finally
                {
                    if (backupOut != null)
                    {
                        try
                        {
                            backupOut.flush();
                            backupOut.close();
                            
                        } catch (IOException ex)
                        {
                            ex.printStackTrace();
                            errorMsg = ex.toString();
                        }
                    }
                }
                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                UIRegistry.getStatusBar().setProgressDone(STATUSBAR_NAME);
                
                UIRegistry.clearSimpleGlassPaneMsg();
                
                if (StringUtils.isNotEmpty(errorMsg))
                {
                    UIRegistry.showError(errorMsg);
                }
                
                if (doSendAppExit)
                {
                    CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit"));  
                }
                
                if (propChgListener != null)
                {
                    propChgListener.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, DONE, null, fullPath));
                }
            }
        };
        
        final JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setIndeterminate(STATUSBAR_NAME, true);
        
        UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("MySQLBackupService.BACKINGUP", databaseName), 24);
        
        backupWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (MEGS.equals(evt.getPropertyName())) 
                        {
                            long value = (Long)evt.getNewValue();
                            double val = value / 10.0;
                            statusBar.setText(UIRegistry.getLocalizedMessage("MySQLBackupService.BACKUP_MEGS", val));
                        }
                    }
                });
        backupWorker.execute();
        
        return true;
    }
    
    /**
     * @param newFilePath
     * @param isMonthly
     */
    public void doCompareBeforeRestore(final String restoreFilePath, final SimpleGlassPane glassPane)
    {
        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
        {
            protected Vector<Object[]> rowData = null;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                rowData = doCompare(MySQLBackupService.this.getTableNames(), restoreFilePath);
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                if (rowData != null && rowData.size() > 0)
                {
                    UIRegistry.getStatusBar().setProgressDone(STATUSBAR_NAME);
                    
                    BackupCompareDlg dlg = new BackupCompareDlg(rowData);
                    dlg.setVisible(true);
                    if (!dlg.isCancelled())
                    {
                        doActualRestore(restoreFilePath, glassPane);
                    } else
                    {
                        UIRegistry.clearSimpleGlassPaneMsg();
                    }
                } else
                {
                  doActualRestore(restoreFilePath, glassPane);
                }
            }
        };
        backupWorker.execute();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#doRestore()
     */
    @Override
    public void doRestore()
    {
        AppPreferences remotePrefs  = AppPreferences.getLocalPrefs();
        final String   mysqlLoc     = remotePrefs.get(MYSQL_LOC,     getDefaultMySQLLoc());
        final String   backupLoc    = remotePrefs.get(MYSQLBCK_LOC,  getDefaultBackupLoc());
        
        if (!(new File(mysqlLoc)).exists())
        {
            UIRegistry.showLocalizedError("MySQLBackupService.MYSQL_NO_RESTORE", mysqlLoc);
            return;
        }
        
        File backupDir = new File(backupLoc);
        if (!backupDir.exists())
        {
            if (!backupDir.mkdir())
            {
                UIRegistry.showLocalizedError("MySQLBackupService.MYSQL_NO_BK_DIR", backupDir.getAbsoluteFile());
                return;  
            }
        }
        
        FileDialog dlg = new FileDialog(((Frame)UIRegistry.getTopWindow()), getResourceString("Open"), FileDialog.LOAD);
        dlg.setDirectory(backupLoc);
        dlg.setVisible(true);
        
        String dirStr   = dlg.getDirectory();
        String fileName = dlg.getFile();
        if (StringUtils.isEmpty(dirStr) || StringUtils.isEmpty(fileName))
        {
            return;
        }
        
        errorMsg = null;
        
        final String path = dirStr + fileName;
        
        final JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setIndeterminate(STATUSBAR_NAME, true);
        
        String          databaseName = DBConnection.getInstance().getDatabaseName();
        SimpleGlassPane glassPane    = UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("MySQLBackupService.RESTORING", databaseName), 24);

        doCompareBeforeRestore(path, glassPane);
    }
    
    /**
     * Does backup restore
     * @param restoreFilePath the path of the backup file
     * @param glassPane the glass pane to write the message on
     * @param useGlassPane whether it invokes the glass pane or ignores it
     */
    protected void doActualRestore(final String restoreFilePath, 
                                   final SimpleGlassPane glassPane)
    {
        String databaseName = DBConnection.getInstance().getDatabaseName();
        doRestoreInBackground(databaseName, restoreFilePath, glassPane, RESTORE_COMPLETE, null, false);
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#doRestoreInBackground(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.beans.PropertyChangeListener, boolean, boolean)
     */
    @Override
    public boolean doRestoreInBackground(final String                 databaseName,
                                         final String                 restoreFilePath,
                                         final String                 restoreMsgKey,
                                         final String                 completionMsgKey,
                                         final PropertyChangeListener pcl,
                                         final boolean                doSynchronously,
                                         final boolean                useGlassPane)
    {
        SimpleGlassPane glassPane = useGlassPane ? UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage(restoreMsgKey, databaseName), 24) : null;
        return doRestoreInBackground(databaseName, restoreFilePath, glassPane,completionMsgKey, pcl, doSynchronously);
    }
    
    /**
     * @param databaseName
     * @param restoreFilePath
     * @param glassPane
     * @param completionMsgKey
     */
    protected boolean doRestoreInBackground(final String                 databaseName,
                                            final String                 restoreFilePath, 
                                            final SimpleGlassPane        glassPane,
                                            final String                 completionMsgKey,
                                            final PropertyChangeListener pcl,
                                            final boolean                doSynchronously)
    {
        AppPreferences remotePrefs  = AppPreferences.getLocalPrefs();
        final String   mysqlLoc     = remotePrefs.get(MYSQL_LOC, getDefaultMySQLLoc());

        getNumberofTables();
        
        SynchronousWorker backupWorker = new SynchronousWorker()
        {
            long dspMegs    = 0;
            long fileSize   = 0;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                FileInputStream input = null;
                try
                {
                    String userName  = itUsername != null ? itUsername : DBConnection.getInstance().getUserName();
                    String password  = itPassword != null ? itPassword : DBConnection.getInstance().getPassword();
                    String port      = DatabaseDriverInfo.getDriver(DBConnection.getInstance().getDriverName()).getPort();
                    String server    = DBConnection.getInstance().getServerName();
                    
                    String cmdLine  = String.format("%s -u %s --password=%s --host=%s %s %s", mysqlLoc, userName, password, server, (port != null ? ("--port="+port) : ""), databaseName);
                    String[] args    = StringUtils.split(cmdLine, ' ');
                    Process  process = Runtime.getRuntime().exec(args); 
                    
                    Thread.sleep(100);
                    
                    OutputStream out = process.getOutputStream();
                    
                    // wait as long it takes till the other process has prompted.
                    try 
                    {
                        File inFile    = new File(restoreFilePath);
                        fileSize  = inFile.length();
                        //System.out.println(fileSize);
                        
                        double oneMB      = (1024.0 * 1024.0);
                        double threshold  = fileSize < (oneMB * 4) ? 8192*8 : oneMB;
                        long   totalBytes = 0;
                        
                        dspMegs = 0;

                        input = new FileInputStream(inFile);
                        try 
                        {
                            byte[] bytes = new byte[8192*4];
                            do
                            {
                                int numBytes = input.read(bytes, 0, bytes.length);
                                
                                totalBytes += numBytes;
                                if (numBytes > 0)
                                {
                                    out.write(bytes, 0, numBytes);
                                 
                                    long megs = (long)(totalBytes / threshold);
                                    if (megs != dspMegs)
                                    {
                                        dspMegs = megs;
                                        firePropertyChange(MEGS, dspMegs, (int)( (100.0 * totalBytes) / fileSize));
                                    }
                                    
                                } else
                                {
                                    break;
                                }
                            } while (true);
                        }
                        finally 
                        {
                            input.close();
                        }
                    }
                    catch (IOException ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MySQLBackupService.class, ex);
                        ex.printStackTrace();
                        errorMsg = ex.toString();
                        UIRegistry.showLocalizedError("MySQLBackupService.EXCP_RS");
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        if (pcl != null)
                        {
                            pcl.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, ERROR, 0, 1));
                        }
                    }
                    
                    setProgress(100);
                    
                    out.flush();
                    out.close();
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null)
                    {
                        //System.err.println(line);
                    }
                    
                    in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    StringBuilder sb = new StringBuilder();
                    while ((line = in.readLine()) != null)
                    {
                        if (line.startsWith("ERR"))
                        {
                            sb.append(line);
                            sb.append("\n");
                        }
                    }
                    errorMsg = sb.toString();
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    errorMsg = ex.toString();
                    if (pcl != null)
                    {
                        pcl.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, ERROR, 0, 1));
                    }
                }
                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                JStatusBar statusBar = UIRegistry.getStatusBar();
                if (statusBar != null)
                {
                    statusBar.setProgressDone(STATUSBAR_NAME);
                }
                
                if (glassPane != null)
                {
                    UIRegistry.clearSimpleGlassPaneMsg();
                }
                
                if (StringUtils.isNotEmpty(errorMsg))
                {
                    UIRegistry.showError(errorMsg);
                }
                
                if (statusBar != null)
                {
                    statusBar.setText(UIRegistry.getLocalizedMessage(completionMsgKey, dspMegs));
                }
                
                if (pcl != null)
                {
                    pcl.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, DONE, 0, 1));
                }
            }
        };
        
        if (glassPane != null)
        {
            glassPane.setProgress(0);
        }
        
        backupWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (MEGS.equals(evt.getPropertyName()) && glassPane != null) 
                        {
                            int value = (Integer)evt.getNewValue();
                            
                            if (value < 100)
                            {
                                glassPane.setProgress((Integer)evt.getNewValue());
                            } else
                            {
                                glassPane.setProgress(100);
                            }
                        }
                    }
                });
        
        if (doSynchronously)
        {
            return backupWorker.doWork();
        }
        
        backupWorker.execute();
        return true;
    }
    
    /**
     * @param restoreFilePath
     * @param mysqlLoc
     * @param databaseName
     * @return
     */
    public boolean doRestore(final String restoreFilePath,
                             final String mysqlLoc,
                             final String databaseName,
                             final String userName,
                             final String password)
    {
        FileInputStream input = null;
        try
        {
            String   cmdLine = mysqlLoc+" -u "+userName+" --password="+password+" " + databaseName; 
            String[] args    = StringUtils.split(cmdLine, ' ');
            Process  process = Runtime.getRuntime().exec(args); 
            
            //Thread.sleep(100);
            
            OutputStream out = process.getOutputStream();
            
            // wait as long it takes till the other process has prompted.
            try 
            {
                File inFile    = new File(restoreFilePath);
                input = new FileInputStream(inFile);
                try 
                {
                    //long totalBytes = 0;
                    byte[] bytes = new byte[8192*4]; // 32K
                    do
                    {
                        int numBytes = input.read(bytes, 0, bytes.length);
                        //totalBytes += numBytes;
                        //System.out.println(numBytes+" / "+totalBytes);
                        if (numBytes > 0)
                        {
                            out.write(bytes, 0, numBytes);
                        } else
                        {
                            break;
                        }
                    } while (true);
                }
                finally 
                {
                    input.close();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                errorMsg = ex.toString();
                return false;
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                return false;
                
            } finally
            {
                out.flush();
                out.close();     
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null)
            {
                //System.err.println(line);
            }
            
            in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null)
            {
                if (line.startsWith("ERR"))
                {
                    sb.append(line);
                    sb.append("\n");
                }
            }
            errorMsg = sb.toString();
            
            //System.out.println("errorMsg: ["+errorMsg+"]");
            
            return errorMsg == null || errorMsg.isEmpty();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            errorMsg = ex.toString();
        }
        return false;            
    }
    
    
    /**
     * @param ch
     * @param bytes
     * @param startInx
     * @param len
     * @return
     */
    protected int indexOf(final char ch, final byte[] bytes, final int startInx, final int len)
    {
        int i = startInx;
        while (i < len)
        {
            if (bytes[i] == (byte)ch)
            {
                return i;
            }
            i++;
        }
        return -1;
    }
    
    /**
     * @param connection
     * @param inFile
     * @return
     * @throws Exception
     */
    protected long restoreFile(final Connection connection, final File inFile) throws Exception
    {
        long dspMegs    = 0;
        long fileSize   = 0;
        
        FileInputStream input = null;
        
        // wait as long it takes till the other process has prompted.
        try 
        {
            fileSize = inFile.length();
            //System.out.println("fileSize: "+fileSize);
            
            double oneMB      = (1024.0 * 1024.0);
            double threshold  = fileSize < (oneMB * 4) ? 8192*8 : oneMB;
            long   totalBytes = 0;
            
            dspMegs = 0;
            
            StringBuilder sb = new StringBuilder();

            int len = 0;
            input   = new FileInputStream(inFile);
            try 
            {
                byte[] bytes    = new byte[65536];  // 64
                byte[] strBytes = new byte[65536];  // 64
                byte[] readBuf  = new byte[16384];  // 16
                do
                {
                    int numBytes = input.read(readBuf, 0, readBuf.length);
                    totalBytes += numBytes;
                    if (numBytes > 0)
                    {
                        //System.out.println("Copy FROM 0 to  len["+len+"]  numBytes["+numBytes+"]");
                        System.arraycopy(readBuf, 0, bytes, len, numBytes);
                        len += numBytes;
                        
                        int inx = indexOf(';', bytes, 0, len);
                        while (inx > -1)
                        {
                            System.arraycopy(bytes, 0, strBytes, 0, inx+1);
                            strBytes[inx] = 0;
                            
                            String fullStr = new String(strBytes, 0, inx).trim();
                            //System.out.println("["+fullStr+"]");
                            
                            String[] toks = StringUtils.split(fullStr, '\n');
                            for (String str : toks)
                            {
                                //System.out.println("*****["+str+"]");
                                
                                if (str.length() > 0 && !str.startsWith("--") && !str.startsWith("/*"))
                                {
                                    sb.append(str);
                                }
                            }
                            
                            if (sb.length() > 0)
                            {
                                //System.out.println("###### ["+sb.toString()+"]");
                                int rv = BasicSQLUtils.update(connection, sb.toString());
                                log.debug("rv: "+rv);
                                sb.setLength(0);
                            }
                            
                            len -= (inx + 1);
                            System.arraycopy(bytes, inx+1, bytes, 0, len);
                            
                            //System.out.println("inx: "+inx+"  len: "+len);
                            
                            inx = indexOf(';', bytes, 0, len);
                        }
                        
                        long megs = (long)(totalBytes / threshold);
                        if (megs != dspMegs)
                        {
                            dspMegs = megs;
                            //firePropertyChange(MEGS, dspMegs, (int)( (100.0 * totalBytes) / fileSize));
                        }
                        
                    } else
                    {
                        break;
                    }
                } while (true);
            }
            finally 
            {
                input.close();
            }
        }
        catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MySQLBackupService.class, ex);
            ex.printStackTrace();
            errorMsg = ex.toString();
            UIRegistry.showLocalizedError("MySQLBackupService.EXCP_RS");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            /*if (pcl != null)
            {
                pcl.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, ERROR, 0, 1));
            }*/
        } finally 
        {
            //stmt.close();
        }
        
        return fileSize;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#doRestoreBulkDataInBackground(java.lang.String, java.lang.String, java.lang.String, edu.ku.brc.ui.dnd.SimpleGlassPane, java.lang.String, java.beans.PropertyChangeListener, boolean)
     */
    public boolean doRestoreBulkDataInBackground(final String                 databaseName,
                                                 final String                 options,
                                                 final String                 restoreZipFilePath, 
                                                 final SimpleGlassPane        glassPane,
                                                 final String                 completionMsgKey,
                                                 final PropertyChangeListener pcl,
                                                 final boolean                doSynchronously,
                                                 final boolean                doDropDatabase)
    {
        getNumberofTables();
        
        SynchronousWorker backupWorker = new SynchronousWorker()
        {
            long dspMegs    = 0;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                boolean skipTrackExceptions = BasicSQLUtils.isSkipTrackExceptions();
                BasicSQLUtils.setSkipTrackExceptions(false);
                try
                {
                    String userName  = itUsername != null ? itUsername : DBConnection.getInstance().getUserName();
                    String password  = itPassword != null ? itPassword : DBConnection.getInstance().getPassword();
                    
                    DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
                    if (dbMgr.connectToDBMS(userName, password, DBConnection.getInstance().getServerName()))
                    {
                        if (doDropDatabase)
                        {
                            if (dbMgr.doesDBExists(databaseName) && !dbMgr.dropDatabase(databaseName))
                            {
                                log.error("Database["+databaseName+"] could not be dropped before load.");
                                UIRegistry.showLocalizedError("MySQLBackupService.ERR_DRP_DB", databaseName);
                                return null;
                            }
                            
                            if (!dbMgr.createDatabase(databaseName))
                            {
                                log.error("Database["+databaseName+"] could not be created before load.");
                                UIRegistry.showLocalizedError("MySQLBackupService.CRE_DRP_DB", databaseName);
                                return null;
                            }
                        }

                        DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver(DBConnection.getInstance().getDriverName());
                        String             connStr    = DBConnection.getInstance().getConnectionStr();
                        System.err.println(connStr);
                        
                        DBConnection itDBConn   = DBConnection.createInstance(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), databaseName, connStr, userName, password);
                        Connection   connection = itDBConn.createConnection();
                        connection.setCatalog(databaseName);
                        
                        List<File> unzippedFiles = ZipFileHelper.getInstance().unzipToFiles(new File(restoreZipFilePath));
                        
                        boolean dbCreated = false;
                        for (File file : unzippedFiles)
                        {
                            //System.out.println(file.getName());
                            if (file.getName().equals("createdb.sql"))
                            {
                                long size = restoreFile(connection, file);
                                log.debug("size: "+size);
                                dbCreated = true;
                            }
                        }
                        
                        if (dbCreated)
                        {
                            for (File file : unzippedFiles)
                            {
                                if (file.getName().endsWith("infile"))
                                {
                                    String fPath = file.getCanonicalPath();
                                    if (UIHelper.isWindows())
                                    {
                                        fPath = StringUtils.replace(fPath, "\\", "\\\\");
                                    }
                                    String sql = "LOAD DATA LOCAL INFILE '" + fPath + "' INTO TABLE " + FilenameUtils.getBaseName(file.getName());
                                    log.debug(sql);
                                    //System.err.println(sql);
                                    int rv = BasicSQLUtils.update(connection, sql);
                                    log.debug("done fPath["+fPath+"] rv= "+rv);
                                    //System.err.println("done fPath["+fPath+"] rv= "+rv);
                                }
                            }
                        }
                        
                        ZipFileHelper.getInstance().cleanUp();
                        
                        /*if (!dbMgr.dropDatabase(databaseName))
                        {
                            log.error("Database["+databaseName+"] could not be dropped after load.");
                            UIRegistry.showLocalizedError("MySQLBackupService.ERR_DRP_DBAF", databaseName);
                        }*/
                        
                        setProgress(100);
                        
                        //errorMsg = sb.toString();
                        
                        itDBConn.close();
                    } else
                    {
                        // error can't connect
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    errorMsg = ex.toString();
                    if (pcl != null)
                    {
                        pcl.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, ERROR, 0, 1));
                    }
                    
                } finally
                {
                    BasicSQLUtils.setSkipTrackExceptions(skipTrackExceptions);
                }
                return null;
            }


            @Override
            protected void done()
            {
                super.done();
                
                JStatusBar statusBar = UIRegistry.getStatusBar();
                if (statusBar != null)
                {
                    statusBar.setProgressDone(STATUSBAR_NAME);
                }
                
                if (glassPane != null)
                {
                    UIRegistry.clearSimpleGlassPaneMsg();
                }
                
                if (StringUtils.isNotEmpty(errorMsg))
                {
                    UIRegistry.showError(errorMsg);
                }
                
                if (statusBar != null)
                {
                    statusBar.setText(UIRegistry.getLocalizedMessage(completionMsgKey, dspMegs));
                }
                
                if (pcl != null)
                {
                    pcl.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, DONE, 0, 1));
                }
            }
        };
        
        if (glassPane != null)
        {
            glassPane.setProgress(0);
        }
        
        backupWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (MEGS.equals(evt.getPropertyName()) && glassPane != null) 
                        {
                            int value = (Integer)evt.getNewValue();
                            
                            if (value < 100)
                            {
                                glassPane.setProgress((Integer)evt.getNewValue());
                            } else
                            {
                                glassPane.setProgress(100);
                            }
                        }
                    }
                });
        
        if (doSynchronously)
        {
            return backupWorker.doWork();
        }
        
        backupWorker.execute();
        return true;
    }

                  
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#checkForBackUp()
     */
    @Override
    public boolean checkForBackUp(final boolean doSendExit)
    {
        return checkForBackUp(doSendExit, false);
    }
    
    /**
     * @param key
     * @param diff
     */
    private void showEZDBBackupMessage(final String key, final int diff)
    {
        File   ezdbFile   = DBConnection.getEmbeddedDataDir();
        String emdDirPath = ezdbFile != null ? ezdbFile.getAbsolutePath() : "N/A";
        String line1      = key != null ? getLocalizedMessage(key, diff) : "";
        String msg        = String.format(getResourceString("MySQLBackupService.EZDB_BACKUP"), line1, emdDirPath);
        
        int rv = UIRegistry.askYesNoLocalized("MySQLBackupService.DIRBTN", "CLOSE", msg, 
                                                getResourceString("MySQLBackupService.BK_NOW_TITLE"));
        if (rv == JOptionPane.OK_OPTION)
        {
            try
            {
                String urlString = UIRegistry.getResourceString("MySQLBackupService.EZDB_BACKUP_LINK");
                AttachmentUtils.openURI(new URL(urlString).toURI());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                
            } 
        }
    }
    
    /**
     * @param prefName
     * @return
     */
    private Long getLastBackupTime(final String prefName)
    {
        String dbName = DBConnection.getInstance().getDatabaseName();
        String key    = dbName + "." + prefName;
        
        // Check New Location (database specific)
        Long timeDaysOrMons = AppPreferences.getLocalPrefs().getLong(key, null);
        if (timeDaysOrMons != null)
        {
            return timeDaysOrMons;
        }
        
        // Check Old Location
        return AppPreferences.getLocalPrefs().getLong(prefName, null);
    }
    
    /**
     * @param prefName
     * @param bkTime
     */
    private void saveLastBackupTime(final String prefName, final long bkTime)
    {
        String dbName = DBConnection.getInstance().getDatabaseName();
        String key    = dbName + "." + prefName;
        
        AppPreferences.getLocalPrefs().putLong(key, bkTime);
    }
    
    /**
     * Checks to see if it is time to do a weekly or monthly backup. Weeks are rolling 7 days and months
     * are rolling 30 days.
     * @param doSendExit requests to send an application exit command
     * @param doSkipAsk indicates whether to skip asking the user thus forcing the backup if it is time
     * @return true if the backup was started, false if it wasn't started.
     */
    private boolean checkForBackUp(final boolean doSendExit, 
                                   final boolean doSkipAsk)
    {
        final long oneDayMilliSecs = 86400000;
        
        Calendar calNow  = Calendar.getInstance();
        Date     dateNow = calNow.getTime();
        
        Long timeDays = getLastBackupTime(WEEKLY_PREF);
        if (timeDays == null)
        {
            timeDays = dateNow.getTime();
            saveLastBackupTime(WEEKLY_PREF, dateNow.getTime());
        }
        
        Long timeMons = getLastBackupTime(MONTHLY_PREF);
        if (timeMons == null)
        {
            timeMons = dateNow.getTime();
            saveLastBackupTime(MONTHLY_PREF, dateNow.getTime());
        }
        
        Date lastBackUpDays = new Date(timeDays);
        Date lastBackUpMons = new Date(timeMons);
        
        int diffMons = (int)((dateNow.getTime() - lastBackUpMons.getTime()) / oneDayMilliSecs);
        int diffDays = (int)((dateNow.getTime() - lastBackUpDays.getTime()) / oneDayMilliSecs);
                
        int      diff     = 0;
        String   key      = null;
        boolean isMonthly = false;
        
        if (diffMons > 30)
        {
            key       =  "MySQLBackupService.MONTHLY";
            diff      = diffMons;
            isMonthly = true;
            
        } else if (diffDays > 7)
        {
            key  = "MySQLBackupService.WEEKLY";
            diff = diffDays;
        }
        
        int userChoice = JOptionPane.CANCEL_OPTION;
        
        if (key != null && (UIRegistry.isEmbedded() ||!doSkipAsk))
        {
            if (UIRegistry.isEmbedded())
            {
                showEZDBBackupMessage(key, diff);
                
            } else
            {
                Object[] options = { getResourceString("MySQLBackupService.BACKUP_NOW"),  //$NON-NLS-1$
                                     getResourceString("MySQLBackupService.BK_SKIP")  //$NON-NLS-1$
                                   };
                userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                getLocalizedMessage(key, diff),  //$NON-NLS-1$
                                                getResourceString("MySQLBackupService.BK_NOW_TITLE"),  //$NON-NLS-1$
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            }
        }
        
        if (isMonthly)
        {
            saveLastBackupTime(MONTHLY_PREF, dateNow.getTime());
            if (diffDays > 7)
            {
                saveLastBackupTime(WEEKLY_PREF, dateNow.getTime());
            }
        } else
        {
            saveLastBackupTime(WEEKLY_PREF, dateNow.getTime());
        }
        
        if (!UIRegistry.isEmbedded() && (userChoice == JOptionPane.YES_OPTION || doSkipAsk))
        {
            return doBackUp(isMonthly, doSendExit, null);
        }
        
        return false;
    }
    
    /**
     * @return
     */
    public static String getDefaultMySQLDumpLoc()
    {
        String mysqldumpLoc = "";
        switch (UIHelper.getOSType())
        {
            case Windows : 
                mysqldumpLoc = searchForWindowsPath(true); // true means search for mysqldump.exe
                break;
                
            case MacOSX : 
                mysqldumpLoc = "/usr/local/mysql/bin/mysqldump";
                break;
                
            case Linux :
                mysqldumpLoc = "/usr/bin/mysqldump";
                break;
                
            default:
                break;
        }
        return mysqldumpLoc;
    }
    
    /**
     * @return
     */
    public static String getDefaultMySQLLoc()
    {
        String mysqlLoc = "";
        switch (UIHelper.getOSType())
        {
            case Windows : 
                mysqlLoc = searchForWindowsPath(false);// false means search for mysql.exe
                break;
                
            case MacOSX : 
                mysqlLoc = "/usr/local/mysql/bin/mysql";
                break;
                
            case Linux : 
                mysqlLoc = "/usr/bin/mysql";
                break;
                
            default:
                break;                
                
        }
        return mysqlLoc;
    }
    
    /**
     * Search for the mysql exes starting at the location of Specify, assuming it was installed
     * into the 'Program Files' location. If it can't find it it just returns empty string.
     * @param doDump true searches for 'mysqldump.exe'; false searches for 'mysql.exe'
     * @return the full path or empty string.
     */
    @SuppressWarnings("unchecked")
    private static String searchForWindowsPath(final boolean doDump)
    {
        String exeName = doDump ? "mysqldump.exe" : "mysql.exe";
        
        try
        {
            String programFilesPath = UIRegistry.getDefaultWorkingPath() + File.separator + ".." + File.separator + "..";
            File dir = new File(programFilesPath);
            if (dir.exists() && dir.isDirectory())
            {
                //System.out.println(dir.getAbsolutePath());
                // First search for the mysql directory
                File mysqlDir = null;
                for (File file : dir.listFiles())
                {
                    if (StringUtils.contains(file.getName().toLowerCase(), "mysql"))
                    {
                        mysqlDir = file;
                        break;
                    }
                }
                
                // Now search for exes
                if (mysqlDir != null)
                {
                    for (File file : (Collection<File>)FileUtils.listFiles(mysqlDir, new String[] {"exe"}, true))
                    {
                        if (file.getName().equalsIgnoreCase(exeName))
                        {
                            return file.getAbsolutePath();
                        }
                    }
                }
            }
            
        } catch (Exception ex)
        {
            // might get an exception on Vista
        }
        return "";
    }
    
    /**
     * @return
     */
    public static String getDefaultBackupLoc()
    {
        return UIRegistry.getAppDataDir() + File.separator + "backups";
    }
    
    abstract class SynchronousWorker extends SwingWorker<Integer, Integer>
    {
        /**
         * Do the Work Asynchronously.
         */
        public boolean doWork() 
        {
            try
            {
                doInBackground();
                done();
                return true;
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            return false;
        }
    }
    
    
    /**
     * @param args
     */
    /*public static void main(String[] args)
    {
        System.setProperty(DBMSUserMgr.factoryName, "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");

        String usr = "root";
        String pwd = "";
        
        DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
        String             connStr    = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, "localhost", null, usr, pwd, driverInfo.getName());
        
        System.err.println(connStr);
        
        DBConnection.getInstance().setDialect(driverInfo.getDialectClassName());
        DBConnection.getInstance().setDriverName("MySQL");
        DBConnection.getInstance().setDriver(driverInfo.getDriverClassName());
        DBConnection.getInstance().setServerName("localhost");
        
        //DBConnection dbConn = DBConnection.getInstance().(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), null, connStr, usr, pwd);
        MySQLBackupService bks = new MySQLBackupService();
        bks.setUsernamePassword(usr, pwd);
        bks.doRestoreBulkDataInBackground(null, "geonames2", null, "/home/rods/geonames.zip", null, DONE, null, true);
        
        //dbConn.close();
    }*/
    
}
