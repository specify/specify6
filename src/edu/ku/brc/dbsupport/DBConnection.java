/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.dbsupport;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mysql.management.driverlaunched.ServerLauncherSocketFactory;

import edu.ku.brc.specify.config.init.SpecifyDBSetupWizard;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * A singleton that remembers all the information needed for creating a JDBC Database connection. 
 * It uses the DBConnection
 * After setting the necessary parameters you can ask it for a connection at any time.<br><br>
 * Also, has a factory method for creating instances so users can connect to more than one database at a time.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class DBConnection
{
    private static final Logger log = Logger.getLogger(DBConnection.class);
    
    private static int          dbCnt          = 0;
    private static boolean      debugCnt       = false;
    private static SQLException loginException = null;
    
    protected String dbUsername;
    protected String dbPassword;
    protected String dbConnectionStr;             // For Create or Open
    protected String dbCloseConnectionStr = null; // for closing
    protected String dbDriver;
    protected String dbDialect;                   // needed for Hibernate
    protected String dbName;
    protected String serverName;                  // Hostname
    protected String dbDriverName;                // Hostname
    
    protected boolean argHaveBeenChecked = false;
    protected boolean skipDBNameCheck    = false;
    
    protected Connection connection = null;
     
    protected String     errMsg = ""; //$NON-NLS-1$
    
    // Static Data Members
    protected static final DBConnection  instance;
    protected static Boolean             isEmbeddedDB;
    protected static File                embeddedDataDir;
    protected static Stack<DBConnection> connections;
    protected static boolean             isShuttingDown;
    protected static File                mobileMachineDir = null;
    protected static boolean             isCopiedToMachineDisk = false;
    protected static boolean             hasCopiedToMobileDisk  = false;
    protected static AtomicBoolean       finalShutdownComplete  = new AtomicBoolean(false);
    
    protected static ShutdownUIIFace     shutdownUI             = null;
    protected static boolean             firstTime              = true;
    protected static boolean             connectionCreated      = false;
    
    
    static
    {
        isShuttingDown  = false;
        isEmbeddedDB    = null;
        embeddedDataDir = null;
        connections     = new Stack<DBConnection>();
        instance        = new DBConnection();
        
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() 
            {
                Runtime.getRuntime().addShutdownHook(new Thread() 
                {
                    @Override
                    public void run() 
                    {
                        if (connectionCreated)
                        {
                            shutdownFinalConnection(false, true);
                        }
                    }
                });
                return null;
            }
        });
    }
    
    /**
     * Protected Default constructor
     *
     */
    protected DBConnection()
    {
        connections.push(this);
        if (debugCnt) System.err.println("Connection Cnt: "+(++dbCnt));
    }
    
    /**
     * @param connectionStr
     * @return
     */
    public static boolean isEmbedded(final String connectionStr)
    {
        return StringUtils.isNotEmpty(connectionStr) && StringUtils.contains(connectionStr, "mxj");
    }
    
    /**
     * @param isEmbeddedDB the isEmbeddedDB to set
     */
    public static void setIsEmbeddedDB(Boolean isEmbeddedDB)
    {
        DBConnection.isEmbeddedDB = isEmbeddedDB;
    }

    /**
     * 
     */
    public static void resetEmbeddedDir()
    {
        DBConnection.embeddedDataDir = null;
    }
    
    /**
     * For Embedded MySQL.
     * @param connectionStr JDBC connection string
     */
    public static void checkForEmbeddedDir(final String connectionStr)
    {
        if (connectionStr != null && DBConnection.embeddedDataDir == null)
        {
            DBConnection.isEmbeddedDB = isEmbedded(connectionStr);
            if (DBConnection.isEmbeddedDB)
            {
                String attr = "server.basedir=";
                int inx = connectionStr.indexOf(attr);
                if (inx > -1)
                {
                    inx += attr.length();
                    int eInx = connectionStr.indexOf("&", inx);
                    if (eInx > -1)
                    {
                        DBConnection.embeddedDataDir = new File(connectionStr.substring(inx, eInx));
                    }
                }
            }
        }
    }
    
    /**
     * @param shutdownUI the shutdownUI to set
     */
    public static void setShutdownUI(final ShutdownUIIFace shutdownUI)
    {
        DBConnection.shutdownUI = shutdownUI;
    }
    
    /**
     * Enable a another login to be called.
     */
    public static void startOver()
    {
        finalShutdownComplete.set(false);
    }

    /**
     * Shuts down the Embedded process.
     */
    public static void shutdownFinalConnection(final boolean doExit, final boolean doImmediately)
    {
        if (!finalShutdownComplete.get())
        {
            if (shutdownUI != null)
            {
                shutdownUI.displayShutdownMsgDlg();
            }
            
            if (doImmediately)
            {
                doingShutdownFinalConnection(doExit);
                
            } else
            {
                javax.swing.SwingWorker<Object, Object> worker = new javax.swing.SwingWorker<Object, Object>()
                {
                    @Override
                    protected Object doInBackground() throws Exception
                    {
                        try
                        {
                            Thread.sleep(1000);
                            
                        } catch (Exception ex) {}
                        
                        return null;
                    }
    
                    @Override
                    protected void done()
                    {
                        super.done();
                        
                        doingShutdownFinalConnection(doExit);
                    }
                };
                worker.execute();
            }
        }
    }
    
    /**
     * 
     */
    private static void doingShutdownFinalConnection(final boolean doExit)
    {
        if (isEmbeddedDB != null && isEmbeddedDB)
        {
            ServerLauncherSocketFactory.shutdown(embeddedDataDir, null);
        }
        
        // Give it a little time to shutdown
        try
        {
            Thread.sleep(1000);
            
        } catch (Exception ex) {}
        
        if (UIRegistry.isMobile())
        {
            copyToMobileDisk();
        }
        
        finalShutdownComplete.set(true);
        
        if (shutdownUI != null)
        {
            shutdownUI.displayFinalShutdownDlg();
        }
        
        if (doExit)
        {
            System.exit(0);
        }
    }
    
    /**
     * Removes the current embedded bin directory so the right executable will be.
     */
    public static void clearEmbeddedBinDir()
    {
        try
        {
            boolean rmDir = true;
            try
            {
                File mobileEmbeddedDir = new File(UIRegistry.getMobileEmbeddedDBPath());
                if (mobileEmbeddedDir.exists())
                {
                    File osFile = new File(UIRegistry.getMobileEmbeddedDBPath()+File.separator+ "os.txt");
                    if (osFile.exists())
                    {
                        String os = FileUtils.readFileToString(osFile);
                        if (UIHelper.getOSType().toString().equals(os))
                        {
                            rmDir = false;
                        }
                    }
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
            
            if (rmDir)
            {
                File binDir = new File(UIRegistry.getMobileEmbeddedDBPath() + File.separator + "bin");
                if (binDir.exists())
                {
                    FileUtils.deleteDirectory(binDir);
                }
            }
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @return whether the database is being run in "embedded" mode
     */
    public boolean isEmbedded()
    {
        return isEmbeddedDB == null ? false : isEmbeddedDB;
    }
    
    /**
     * @return the the full file path to the embedded directory. 
     */
    public static File getEmbeddedDataDir()
    {
        return embeddedDataDir;
    }

    /**
     * @param dbUsername
     * @param dbPassword
     * @param dbConnectionStr
     * @param dbDriver
     * @param dbDialect
     * @param dbName
     */
    public DBConnection(String dbUsername, 
                        String dbPassword, 
                        String dbConnectionStr,
                        String dbDriver, 
                        String dbDialect, 
                        String dbName)
    {
        super();
        this.dbUsername      = dbUsername;
        this.dbPassword      = dbPassword;
        this.dbConnectionStr = dbConnectionStr;
        this.dbDriver        = dbDriver;
        this.dbDialect       = dbDialect;
        this.dbName          = dbName;
        this.skipDBNameCheck = dbName == null;
        
        connections.push(this);
        
        checkForEmbeddedDir(dbConnectionStr);
        
        if (debugCnt) System.err.println("DB Connection Cnt: "+(++dbCnt));
    }

    /**
     * The error message if it was caused by an exception.
     * @return the error message if it was caused by an exception
     */
    public String getErrorMsg()
    {
        return this.errMsg;
    }
    
    /**
     * @param skipDBNameCheck the skipDBNameCheck to set
     */
    public void setSkipDBNameCheck(boolean skipDBNameCheck)
    {
        this.skipDBNameCheck = skipDBNameCheck;
    }

    /**
     * @return the loginException
     */
    public static SQLException getLoginException()
    {
        return loginException;
    }

    /**
     * Returns a new connection to the database from an instance of DBConnection.
     * It uses the database name, driver, username and password to connect.
     * @return the JDBC connection to the database
     */
    public Connection createConnection()
    {
        //ensureEmbddedDirExists();
        
        connectionCreated = true;
        if (shutdownUI != null && firstTime)
        {
            shutdownUI.displayInitialDlg();
            firstTime = false;
        }

        if (UIRegistry.isMobile() && this == getInstance())
        {
            if (!isCopiedToMachineDisk)
            {
                clearEmbeddedBinDir();
            }
            
            if (copyToMachineDisk())
            {
                try
                {
                    if (mobileMachineDir.exists())
                    {
                        File osFile = new File(mobileMachineDir+File.separator+ "os.txt");
                        FileUtils.writeStringToFile(osFile, UIHelper.getOSType().toString());
                    }
                    
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        
        Connection con = null;
        try
        {
            if (!argHaveBeenChecked)
            {
                if (!skipDBNameCheck && StringUtils.isEmpty(dbName))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_NAME"); //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbConnectionStr))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_CONN_STR"); //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbUsername))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_USERNAME");//"The Username is empty."; //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbPassword))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_PASSWORD");//"The Password is empty."; //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbDriver))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_DRIVER"); //$NON-NLS-1$
                    return null;
                }
                argHaveBeenChecked = true;
            }
            Class.forName(dbDriver); // load driver
            
            //if (System.getProperty("user.name").equals("rods"))
            //{
            //    log.debug("******** ["+dbConnectionStr+"]["+dbUsername+"]["+dbPassword+"] ");
            //}
            //System.err.println("["+dbConnectionStr+"]["+dbUsername+"]["+dbPassword+"] ");
            con = DriverManager.getConnection(dbConnectionStr, dbUsername, dbPassword);
            
        } catch (SQLException sqlEx)
        {
            loginException = sqlEx;
            
            sqlEx.printStackTrace();
            
            log.error("Error in getConnection", sqlEx);
            if (sqlEx.getNextException() != null)
            {
                errMsg = sqlEx.getNextException().getMessage();
            } else
            {
                errMsg = sqlEx.getMessage();
            }
            
            errMsg += " For ["+dbConnectionStr+"]["+dbUsername+"]";//["+dbPassword+"]";
                
        } catch (Exception ex)
        {
//            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DBConnection.class, ex);
            log.error("Error in getConnection", ex);
            errMsg = ex.getMessage();
            errMsg += " For ["+dbConnectionStr+"]["+dbUsername+"]";//["+dbPassword+"]";
        }
        return con;
    }
    
    /**
     * Closes the connection to the database and disposes it.
     */
    public void close()
    {
        if (debugCnt) System.err.println("DB Connection Cnt: "+(--dbCnt) +"  is Instance: "+(this == getInstance()));
        try
        {
            if (connections.size() == 1 && this == instance)
            {
                connections.remove(this);
                
            } else if (connections.indexOf(this) > -1)
            {
                connections.remove(this);
                
            } else
            {
                String msg = "The DBConnection ["+this+"] has already been removed!";
                log.error(msg);
                //UIRegistry.showError(msg);
            }
            
            if (!isShuttingDown)
            {
                if (this == instance)
                {
                    String msg = "The DBConnection.getInstance().close() should not be called. (Call DBConnection.shutdown()).";
                    log.error(msg);
                    UIRegistry.showError(msg);
                }
            }
            
            //if (connections.isEmpty())
            //{
            //    shutdownFinalConnection();    
            //}
            
            // This is primarily for Derby non-networked database. 
            if (dbCloseConnectionStr != null)
            {
                Connection con = DriverManager.getConnection(dbCloseConnectionStr, dbUsername, dbPassword);
                if (con != null)
                {
                    con.close();
                }
            } else if (connection != null)
            {
                connection.close();
                connection = null;
            }
        } catch (Exception ex)
        {
            log.error(ex);
        }
    }
    
    /**
     * Sets the user name and password.
     * @param dbUsername the username
     * @param dbPassword the password
     */
    public void setUsernamePassword(final String dbUsername, final String dbPassword)
    {
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        argHaveBeenChecked = false;
    }
    
    /**
     * Sets the database name.
     * @param dbName the database name
     */
    public void setDatabaseName(final String dbName)
    {
        this.dbName = dbName;
        argHaveBeenChecked = false;
    }
    
    /**
     * Sets the driver name.
     * @param dbDriver the driver name
     */
    public void setDriver(final String dbDriver)
    {
        this.dbDriver = dbDriver;
        argHaveBeenChecked = false;
    }
    
    /**
     * Sets the Hibernate Dialect class name.
     * @param dbDialect the driver name
     */
    public void setDialect(final String dbDialect)
    {
        this.dbDialect = dbDialect;
        argHaveBeenChecked = false;
    }
    
    /**
     * @return the serverName
     */
    public String getServerName()
    {
        return serverName;
    }

    /**
     * @param server the server to set
     */
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    /**
     * @return the dbDriverName
     */
    public String getDriverName()
    {
        return dbDriverName;
    }

    /**
     * @param dbDriverName the dbDriverName to set
     */
    public void setDriverName(String dbDriverName)
    {
        this.dbDriverName = dbDriverName;
    }

    /**
     * Sets the fully specified path to connect to the database.
     * i.e. jdbc:mysql://localhost/fish<br>Some databases may need to construct their fully specified path.
     * @param dbConnectionStr the full connection string
     */
    public void setConnectionStr(final String dbConnectionStr)
    {
        this.dbConnectionStr = dbConnectionStr;
        argHaveBeenChecked = false;
        
        checkForEmbeddedDir(dbConnectionStr);
    }
    
    /**
     * Returns the driver
     * @return the driver
     */
    public String getDriver()
    {
        return dbDriver;
    }

    /**
     * Gets the fully specified path to connect to the database.
     * i.e. jdbc:mysql://localhost/fish<br>Some databases may need to construct their fully specified path.
     * @return the full connection string
     */
    public String getConnectionStr()
    {
        return dbConnectionStr;
    }

    /**
     * Returns the Close Connection String.
     * @return the Close Connection String.
     */
    public String getDbCloseConnectionStr()
    {
        return dbCloseConnectionStr;
    }

    /**
     * Sets the Close Connection String.
     * @param dbCloseConnectionStr the string (can be null to clear it)
     */
    public void setDbCloseConnectionStr(final String dbCloseConnectionStr)
    {
        this.dbCloseConnectionStr = dbCloseConnectionStr;
    }

    /**
     * Returns the Database Name.
     * @return the Database Name.
     */
    public String getDatabaseName()
    {
        return dbName;
    }

    /**
     * Returns the Password.
     * @return the Password.
     */
    public String getPassword()
    {
        return dbPassword;
    }

    /**
     * Returns the USe Name.
     * @return the USe Name.
     */
    public String getUserName()
    {
        return dbUsername;
    }
    
    /**
     * Returns the Dialect.
     * @return the Dialect.
     */
    public String getDialect()
    {
        return dbDialect;
    }

    @SuppressWarnings("unused")
    private void ensureEmbddedDirExists()
    {
        if (isEmbedded())
        {
            File embeddedDir = getEmbeddedDataDir();
            if (!embeddedDir.exists())
            {
                log.debug("Created data dir["+embeddedDir.getAbsolutePath()+"]");
                if (embeddedDir.mkdirs())
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, new Exception("The Embedded Data Dir could not be created at["+embeddedDir.getAbsolutePath()+"]"));

                }
            }
        }
    }
    
    /**
     * Returns a new connection to the database. 
     * @return the JDBC connection to the database
     */
    public Connection getConnection()
    {
        if (connection == null)
        {
            connection = createConnection();
        }
        
        return connection;
    }
    
    /**
     * Create a new instance.
     * @param dbDriver the driver name
     * @param dbDialect the dialect class name for Hibernate
     * @param dbName the database name (just the name)
     * @param dbConnectionStr the full connection string
     * @param dbUsername the username
     * @param dbPassword the password
     * @return a new instance of a DBConnection
     */
    public static DBConnection createInstance(final String dbDriver, 
                                              final String dbDialect, 
                                              final String dbName, 
                                              final String dbConnectionStr, 
                                              final String dbUsername, 
                                              final String dbPassword)
    {
        DBConnection dbConnection = new DBConnection();
        
        dbConnection.setDriver(dbDriver);
        dbConnection.setDialect(dbDialect);
        dbConnection.setDatabaseName(dbName);
        dbConnection.setConnectionStr(dbConnectionStr);
        dbConnection.setUsernamePassword(dbUsername, dbPassword);
        
        checkForEmbeddedDir(dbConnectionStr);
        
        return dbConnection;
    }
    
    /**
     * Returns the instance to the singleton.
     * @return the instance to the singleton
     */
    public static DBConnection getInstance()
    {
        return instance;
    }
    
    /**
     * Shuts down all the connections (including the main getInstance()).
     */
    public synchronized static void shutdown()
    {
        isShuttingDown = true;
        try
        {
            while (!connections.isEmpty())
            {
                DBConnection dbConn = connections.peek();
                dbConn.close();
            }
            connections.clear();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        isShuttingDown = false;
        
        /*if (UIRegistry.isMobile())
        {
            copyToMobileDisk();
        }*/
    }
    
    public static void clearMobileMachineDir()
    {
        mobileMachineDir = null;
    }
    
    /**
     * @return
     * @throws IOException
     */
    public static File getMobileMachineDir(final String dbName)
    {
        if (mobileMachineDir == null && StringUtils.isNotEmpty(dbName))
        {
            String path = UIRegistry.getDefaultUserHomeDir();
            
            mobileMachineDir = new File(path + File.separator + dbName + "_data_" + Long.toString(System.currentTimeMillis()));
            
        } else if (StringUtils.isEmpty(dbName) && mobileMachineDir == null)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, new RuntimeException("dbName was null"));
        }
        return mobileMachineDir;
    }
    
    /**
     * @return
     * @throws IOException
     */
    public static File getMobileMachineDir()
    {
        return getMobileMachineDir(null);
    }

    /**
     * @param isCopiedToMachineDisk the isCopiedToMachineDisk to set
     */
    public static void setCopiedToMachineDisk(final boolean isCopiedToMachineDisk)
    {
        DBConnection.isCopiedToMachineDisk = isCopiedToMachineDisk;
    }

    /**
     * Copies from the MobileEmbeddedDir to MobileMachineDir 
     * @return true on success
     */
    private static boolean copyToMachineDisk()
    {
        if (!isCopiedToMachineDisk)
        {
            try
            {
                mobileMachineDir = getMobileMachineDir();
                if (!mobileMachineDir.exists())
                {
                    mobileMachineDir.mkdirs();
                }
                
                System.out.println("****** copyToMachineDisk - getMobileEmbeddedDBPath["+(new File(UIRegistry.getMobileEmbeddedDBPath()).getCanonicalPath())+"] to mobileTmpDir["+mobileMachineDir.getCanonicalPath()+"]");
                
                File mobileEmbeddedDir = new File(UIRegistry.getMobileEmbeddedDBPath());
                if (mobileEmbeddedDir.exists())
                {
                    FileUtils.copyDirectory(mobileEmbeddedDir, mobileMachineDir, true);
                    UIRegistry.setEmbeddedDBPath(mobileMachineDir.getCanonicalPath());
                    
                    isCopiedToMachineDisk = true;
                    
                    for (Object fObj : FileUtils.listFiles(mobileMachineDir, null, true))
                    {
                        File f = (File)fObj;
                        if (f.getName().endsWith("DS_Store"))
                        {
                            f.delete();
                        }
                    }
                    return true;
                } 
                
                log.error("Mobile path doesn't exist at["+mobileEmbeddedDir.getCanonicalPath()+"]");
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * @param isCopiedToMobileDisk the isCopiedToMobileDisk to set
     */
    public static void setCopiedToMobileDisk(boolean isCopiedToMobileDisk)
    {
        DBConnection.hasCopiedToMobileDisk = isCopiedToMobileDisk;
    }

    /**
     * @return
     */
    private static boolean copyToMobileDisk()
    {
        //System.out.println("######  copyToMobileDisk  -  mobileTmpDir["+mobileMachineDir+"] isCopiedToMachineDisk ["+isCopiedToMachineDisk+"]");
        if (!hasCopiedToMobileDisk && mobileMachineDir != null && isCopiedToMachineDisk)
        {
            hasCopiedToMobileDisk = true;
            try
            {
                File mobileDir = new File(UIRegistry.getMobileEmbeddedDBPath());
                FileUtils.deleteDirectory(mobileDir);
                
                //System.out.println("###### copyToMobileDisk  -  mobileTmpDir["+mobileMachineDir+"] to mobileDir["+mobileDir.getCanonicalPath()+"]");
                
                FileUtils.copyDirectory(mobileMachineDir, mobileDir, true);
                for (Object fObj : FileUtils.listFiles(mobileDir, null, true))
                {
                    File f = (File)fObj;
                    if (f.getName().endsWith("DS_Store"))
                    {
                        f.delete();
                    }
                }
                
                for (Object fObj : FileUtils.listFiles(mobileMachineDir, null, true))
                {
                    File f = (File)fObj;
                    if (f.exists() && !f.getName().equals("mysql.sock"))
                    {
                        f.delete();
                    }
                }
                
                log.debug("Removing on exit["+mobileMachineDir.getCanonicalPath()+"]");
                
                mobileMachineDir.deleteOnExit();
                
                return true;
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return false;
    }
    
    
    //-------------------------------------------------------------------------------------
    //-- 
    //-------------------------------------------------------------------------------------
    public interface ShutdownUIIFace 
    {
        /**
         * This should display a modal dialog telling the user that they will need to be notified that the app shutdown.
         */
        public abstract void displayInitialDlg();
        
        /**
         * This should display a non-modal dialog with a shutdown message (i.e. an in progress like message)
         */
        public abstract void displayShutdownMsgDlg();
        
        /**
         * This is a final modal dialog that tells them that they can remove the USB key.
         */
        public abstract void displayFinalShutdownDlg();
        
        
    }

}
