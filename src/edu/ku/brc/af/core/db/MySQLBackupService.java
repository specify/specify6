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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.BackupCompareDlg;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
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
    private final String WEEKLY_PREF    = "LAST.BACKUP.WEEKLY";
    private final String MONTHLY_PREF   = "LAST.BACKUP.MONS";

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
        Connection dbConnection = DBConnection.getInstance().createConnection();
        Statement dbStatement = null;
        try
        {
            dbConnection = DBConnection.getInstance().createConnection();
            if (dbConnection != null)
            {
                dbStatement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = dbStatement.executeQuery("show tables");
                
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
        
        Connection dbConnection = DBConnection.getInstance().createConnection();
        Statement dbStatement = null;
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
                propChgListener.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, "Error", 0, 1));
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
                    propChgListener.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, "Error", 0, 1));
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
                    
                    String cmdLine  = String.format("%s -u %s --password=%s %s", mysqldumpLoc, userName, password, databaseName);
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
                    	System.err.println(line);
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
                    propChgListener.propertyChange(new PropertyChangeEvent(MySQLBackupService.this, "Done", null, fullPath));
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
        
        FileDialog dlg = new FileDialog(((Frame)UIRegistry.getTopWindow()), "Open", FileDialog.LOAD);
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
        
        final String   databaseName = DBConnection.getInstance().getDatabaseName();
        SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("MySQLBackupService.RESTORING", databaseName), 24);

        doCompareBeforeRestore(path, glassPane);
    }
    
    /**
     * @param restoreFilePath
     * @param glassPane
     */
    protected void doActualRestore(final String restoreFilePath, final SimpleGlassPane glassPane)
    {
        AppPreferences remotePrefs  = AppPreferences.getLocalPrefs();
        final String   mysqlLoc     = remotePrefs.get(MYSQL_LOC,     getDefaultMySQLLoc());
        final String   databaseName = DBConnection.getInstance().getDatabaseName();

        getNumberofTables();
        
        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
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
                    String userName = DBConnection.getInstance().getUserName();
                    String password = DBConnection.getInstance().getPassword();
                	String   cmdLine = mysqlLoc+" -u "+userName+" --password="+password+" " + databaseName; // XXX RELEASE
                	String[] args    = StringUtils.split(cmdLine, ' ');
                    Process  process = Runtime.getRuntime().exec(args); 
                    
                    Thread.sleep(100);
                    
                    OutputStream out = process.getOutputStream();
                    
                    // wait as long it takes till the other process has prompted.
                    try 
                    {
                        File inFile    = new File(restoreFilePath);
                        fileSize  = inFile.length();
                        System.out.println(fileSize);
                        
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
                    line = null;
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
                
                UIRegistry.getStatusBar().setText(UIRegistry.getLocalizedMessage("MySQLBackupService.RESTORE_COMPLETE", dspMegs));
                
                // We don't have to shutdown the App when it is stand alone
                // really need to send the notificaiton no matter what and have the App display the Exit dialog if it wants to.
                //UIRegistry.showLocalizedMsg("MySQLBackupService.ABT_EXIT_TITLE", "MySQLBackupService.ABT_EXIT");
                //CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit"));
            }
        };
        
        //final JStatusBar statusBar = UIRegistry.getStatusBar();
        //statusBar.setProgressRange(STATUSBAR_NAME, 0, 100);
        
        glassPane.setProgress(0);
        
        backupWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (MEGS.equals(evt.getPropertyName())) 
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
        backupWorker.execute();
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
                    long totalBytes = 0;
                    byte[] bytes = new byte[8192*4]; // 32K
                    do
                    {
                        int numBytes = input.read(bytes, 0, bytes.length);
                        totalBytes += numBytes;
                        System.out.println(numBytes+" / "+totalBytes);
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
            line = null;
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
            
            System.out.println("errorMsg: ["+errorMsg+"]");
            
            return errorMsg == null || errorMsg.isEmpty();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            errorMsg = ex.toString();
        }
        return false;            
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
        
        // Commented lines are for testing
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        Calendar calNow = Calendar.getInstance();
        
        //Calendar testNow = Calendar.getInstance();
        //testNow.add(Calendar.DAY_OF_MONTH, 40);
        
        Date dateNow = calNow.getTime();
        //System.out.println(sdf.format(dateNow));
        
        //System.out.println(sdf.format(testNow.getTime().getTime()));
        //calNow  = testNow;
        //dateNow = calNow.getTime();
        
        Long timeDays = AppPreferences.getLocalPrefs().getLong(WEEKLY_PREF, null);//$NON-NLS-1$
        if (timeDays == null)
        {
            timeDays = dateNow.getTime();
            AppPreferences.getLocalPrefs().putLong(WEEKLY_PREF, dateNow.getTime());
        }
        
        Long timeMons = AppPreferences.getLocalPrefs().getLong(MONTHLY_PREF, null);//$NON-NLS-1$
        if (timeMons == null)
        {
            timeMons = dateNow.getTime();
            AppPreferences.getLocalPrefs().putLong(MONTHLY_PREF, dateNow.getTime());
        }
        
        Date lastBackUpDays = new Date(timeDays);
        Date lastBackUpMons = new Date(timeMons);
        
        int diffMons = (int)((dateNow.getTime() - lastBackUpMons.getTime()) / oneDayMilliSecs);
        int diffDays = (int)((dateNow.getTime() - lastBackUpDays.getTime()) / oneDayMilliSecs);
        //System.out.println("diffMons "+diffMons+"  diffDays "+diffDays);
                
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
        
        if (key != null && !doSkipAsk)
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
        
        if (isMonthly)
        {
            AppPreferences.getLocalPrefs().putLong(MONTHLY_PREF, dateNow.getTime());
            if (diffDays > 7)
            {
                AppPreferences.getLocalPrefs().putLong(WEEKLY_PREF, dateNow.getTime());
            }
        } else
        {
            AppPreferences.getLocalPrefs().putLong(WEEKLY_PREF, dateNow.getTime());
        }
        
        if (userChoice == JOptionPane.YES_OPTION || doSkipAsk)
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
            	System.out.println(dir.getAbsolutePath());
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
    
}
