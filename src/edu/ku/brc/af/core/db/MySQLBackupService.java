/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.af.core.db;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.FileDialog;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.BackupCompareDlg;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
    
    /**
     * Does the backup on a SwingWorker Thread.
     * @param isMonthly whether it is a monthly backup
     * @param doSendAppExit requests sending an application exit command when done
     * @return true if the prefs are set up and there were no errors before the SwingWorker thread was started
     */
    private boolean doBackUp(final boolean isMonthly, final boolean doSendAppExit)
    {
        AppPreferences remotePrefs = AppPreferences.getLocalPrefs();
        
        final String mysqldumpLoc = remotePrefs.get(MYSQLDUMP_LOC, getDefaultMySQLDumpLoc());
        final String backupLoc    = remotePrefs.get(MYSQLBCK_LOC,  getDefaultBackupLoc());
        
        if (!(new File(mysqldumpLoc)).exists())
        {
            UIRegistry.showLocalizedError("MySQLBackupService.MYSQL_NO_DUMP", mysqldumpLoc);
            return false;
        }
        
        File backupDir = new File(backupLoc);
        if (!backupDir.exists())
        {
            if (!backupDir.mkdir())
            {
                UIRegistry.showLocalizedError("MySQLBackupService.MYSQL_NO_BK_DIR", backupDir.getAbsoluteFile());
                return false;  
            }
        }
        
        errorMsg = null;
        
        final String databaseName = AppPreferences.getLocalPrefs().get("CURRENT_DB", null);
        
        getNumberofTables();
        
        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
        {
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                int count = 0;
                BufferedWriter backupOut = null;
                try
                {
                    Thread.sleep(100);
                    
                    // Create output file
                    SimpleDateFormat sdf      = new SimpleDateFormat("yyyy_MM_dd_kk_mm_ss");
                    String           fileName = sdf.format(Calendar.getInstance().getTime()) + (isMonthly ? "_monthly" : "") + ".sql";
                    String           fullPath = backupLoc + File.separator + fileName;
                    backupOut = new BufferedWriter(new FileWriter(fullPath));
                    
                    writeStats(getCollectionStats(getTableNames()), getStatsName(fullPath));
                    
                    String cmdLine = mysqldumpLoc + " -u Specify --password=Specify " + databaseName;// XXX RELEASE
                    String[] args = StringUtils.split(cmdLine, ' ');
                    Process process = Runtime.getRuntime().exec(args);
                    
                    String        line = null;
                    StringBuilder sb   = new StringBuilder();

                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    line = null;
                    while ((line = in.readLine()) != null)
                    {
                    	//System.err.println(line);
                        if (line != null)
                        {
                            if (line.length() > 0 && line.charAt(0) == 'C' && line.startsWith("CREATE TABLE"))
                            {
                                count++;
                                setProgress((int)(100.0 * count / numTables));
                            }
                            backupOut.write(line);
                            backupOut.write('\n');
                        }
                    }
                    setProgress(100);
                    
                    line = null;
                    sb   = new StringBuilder();
                    
                    BufferedReader errIn = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((line = errIn.readLine()) != null)
                    {
                    	System.err.println(line);
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
            }
        };
        
        final JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setProgressRange(STATUSBAR_NAME, 0, 100);
        
        UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("MySQLBackupService.BACKINGUP", databaseName), 24);
        
        backupWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if ("progress".equals(evt.getPropertyName())) 
                        {
                            statusBar.setValue(STATUSBAR_NAME, (Integer)evt.getNewValue());
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
    public void doCompareBeforeRestore(final String restoreFilePath)
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
                        doActualRestore(restoreFilePath);
                    } else
                    {
                        UIRegistry.clearSimpleGlassPaneMsg();
                    }
                } else
                {
                  doActualRestore(restoreFilePath);
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
        
        final String   databaseName = AppPreferences.getLocalPrefs().get("CURRENT_DB", null);
        UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("MySQLBackupService.RESTORING", databaseName), 24);

        doCompareBeforeRestore(path);
    }
    
    /**
     * @param restoreFilePath
     */
    protected void doActualRestore(final String restoreFilePath)
    {
        AppPreferences remotePrefs  = AppPreferences.getLocalPrefs();
        final String   mysqlLoc     = remotePrefs.get(MYSQL_LOC,     getDefaultMySQLLoc());
        final String   databaseName = AppPreferences.getLocalPrefs().get("CURRENT_DB", null);

        getNumberofTables();
        
        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
        {
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                BufferedReader input = null;
                int count = 0;
                try
                {
                	String   cmdLine = mysqlLoc+" -u Specify --password=Specify " + databaseName; // XXX RELEASE
                	String[] args    = StringUtils.split(cmdLine, ' ');
                    Process  process = Runtime.getRuntime().exec(args); 
                    
                    Thread.sleep(100);
                    
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    
                    // wait as long it takes till the other process has prompted.
                    try 
                    {
                        input = new BufferedReader(new FileReader(restoreFilePath));
                        try 
                        {
                            String line = null; //not declared within while loop
                            while (( line = input.readLine()) != null)
                            {
                            	if (line != null)
                            	{
                            		out.write(line);
                            		out.newLine();
                            	}
                                
                                if (line.length() > 0 && line.charAt(0) == 'C' && line.startsWith("CREATE TABLE"))
                                {
                                    count++;
                                    setProgress((int)(100.0 * count / numTables));
                                }
                            }
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
                    }

                    out.flush();
                    out.close();
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null)
                    {
                        //System.err.println(line);
                    }
                    setProgress(100);
                    
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
                    
                    //System.out.println(process.exitValue());
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    errorMsg = ex.toString();
                    
                } finally
                {
                    if (input != null)
                    {
                        input.close();
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
                
                UIRegistry.showLocalizedMsg("MySQLBackupService.ABT_EXIT_TITLE", "MySQLBackupService.ABT_EXIT");
                
                CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit"));
            }
        };
        
        final JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setProgressRange(STATUSBAR_NAME, 0, 100);
        
        //UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("MySQLBackupService.RESTORING", databaseName), 24);
        
        backupWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if ("progress".equals(evt.getPropertyName())) 
                        {
                            statusBar.setValue(STATUSBAR_NAME, (Integer)evt.getNewValue());
                        }
                    }
                });
        backupWorker.execute();
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
            return doBackUp(isMonthly, doSendExit);
        }
        
        return false;
    }
    
    /**
     * @param command
     * @return
     */
    @SuppressWarnings("unused")
    private boolean runCmd(final String command)
    {
        BufferedReader input = null;
        try
        {
            Thread.sleep(100);
            
            Process process = Runtime.getRuntime().exec(command);
            
            input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = input.readLine()) != null)
            {
                if (line.startsWith("ERR"))
                {
                    sb.append(line);
                    sb.append("\n");
                }
            }
            errorMsg = sb.toString();
            if (StringUtils.isNotEmpty(errorMsg))
            {
                return false;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            errorMsg = ex.toString();
            return false;
            
        } finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                } catch (Exception ex) {}
            }
        }
        return true;
    }

    /**
     * @return
     */
    public static String getDefaultMySQLDumpLoc()
    {
        String mysqldumpLoc = "";
        switch (UIHelper.getOSType())
        {
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
     * @return
     */
    public static String getDefaultBackupLoc()
    {
        return UIRegistry.getAppDataDir() + File.separator + "backups";
    }
    
}
