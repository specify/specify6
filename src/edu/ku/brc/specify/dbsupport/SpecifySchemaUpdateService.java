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
package edu.ku.brc.specify.dbsupport;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.queryForInts;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.querySingleCol;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.querySingleObj;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.update;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.IdMapperMgr;
import edu.ku.brc.specify.conversion.IdTableMapper;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEventAttribute;
import edu.ku.brc.specify.datamodel.CollectionObjectAttribute;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.ConservEvent;
import edu.ku.brc.specify.datamodel.DNASequencingRun;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.FieldNotebookPage;
import edu.ku.brc.specify.datamodel.GeoCoordDetail;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityDetail;
import edu.ku.brc.specify.datamodel.PaleoContext;
import edu.ku.brc.specify.datamodel.PreparationAttribute;
import edu.ku.brc.specify.datamodel.SpExportSchema;
import edu.ku.brc.specify.datamodel.SpExportSchemaItem;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpTaskSemaphore;
import edu.ku.brc.specify.datamodel.SpVersion;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.subpane.security.NavigationTreeMgr;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.specify.tools.export.ExportToMySQLDB;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 18, 2009
 *
 */
public class SpecifySchemaUpdateService extends SchemaUpdateService
{
    protected static final Logger  log = Logger.getLogger(SpecifySchemaUpdateService.class);
    
    private final int OVERALL_TOTAL = 18;
    
    private static final String APP                     = "App";
    private static final String APP_REQ_EXIT            = "AppReqExit";
    private static final String SCHEMA_VERSION_FILENAME = "schema_version.xml";
    
    private static final String UPD_CNT_NO_MATCH  = "Update count didn't match for update to table: %s";
    private static final String COL_TYP_NO_DET    = "Column type couldn't be determined for update to table %s";
    private static final String ERR_ADDING_FIELDS = "For table %s error adding fields %s";

    private Pair<String, String> itUserNamePassword     = null;
    private ProgressFrame        frame;
    private String               errMsgStr              = null;
    
    /**
     * 
     */
    public SpecifySchemaUpdateService()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SchemaUpdateService#getDBSchemaVersionFromXML()
     */
    public String getDBSchemaVersionFromXML()
    {
        String dbVersion = null;
        Element root;
        try
        {
            root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath(SCHEMA_VERSION_FILENAME)));//$NON-NLS-1$
            if (root != null)
            {
                dbVersion = ((Element)root).getTextTrim();
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return dbVersion;
    }

    /**
     * @param appVerNumArg
     * @return true if appVerNumArg seems to be an internal version
     */
    protected boolean isInternalVerNum(final String appVerNumArg)
    {
        if (appVerNumArg != null)
        {
            String[] pieces = StringUtils.split(appVerNumArg, ".");
            for (int p = 0; p < pieces.length; p++)
            {
                try
                {
                    new Integer(pieces[p]);
                } catch (NumberFormatException ex)
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Makes L10N Key
     * @param shortKey
     * @return L10N Key
     */
    private static String mkKey(final String shortKey)
    {
        return "SpecifySchemaUpdateService." + shortKey;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SchemaUpdateService#updateSchema(java.lang.String)
     */
    @Override
    public SchemaUpdateType updateSchema(final String appVerNumArg)
    {
        String  dbVersion      = getDBSchemaVersionFromXML();
        String  appVerNum      = appVerNumArg;
        boolean internalVerNum = isInternalVerNum(appVerNum);
        
        boolean useSilentSuccess = false;
        
        DBConnection dbConn = DBConnection.getInstance();
        if (dbConn != null)
        {
            DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
            if (dbMgr.connect(dbConn.getUserName(), dbConn.getPassword(), dbConn.getServerName(), dbConn.getDatabaseName()))
            {
                // Here checks to see if this is the first ever
                boolean doUpdateAppVer  = false;
                boolean doSchemaUpdate  = false;
                boolean doInsert        = false;
                String  appVerFromDB    = null;
                String  schemaVerFromDB = null;
                Integer spverId         = null;
                Integer recVerNum       = 1;
                
                
                log.debug("appVerNumArg:  ["+appVerNumArg+"] dbVersion from XML["+dbVersion+"] ");

                if (dbMgr.doesDBHaveTable("spversion"))
                {
                    Vector<Object[]> rows = query(dbConn.getConnection(), "SELECT AppVersion, SchemaVersion, SpVersionID, Version FROM spversion ORDER BY TimestampCreated DESC");
                    if (rows.size() > 0)
                    {
                        Object[] row  = (Object[])rows.get(rows.size()-1);
                        appVerFromDB    = row[0].toString();
                        schemaVerFromDB = row[1].toString();
                        spverId       = (Integer)row[2];
                        recVerNum     = (Integer)row[3];
                        
                        log.debug("appVerNumArg: ["+appVerNumArg+"] dbVersion from XML["+dbVersion+"] appVersion["+appVerFromDB+"] schemaVersion["+schemaVerFromDB+"]  spverId["+spverId+"]  recVerNum["+recVerNum+"] ");
                        
                        if (appVerNum == null /*happens for developers*/ || internalVerNum) 
                        {
                            appVerNum = appVerFromDB;
                        }
                        
                        if (appVerFromDB == null || schemaVerFromDB == null)
                        {
                            doUpdateAppVer = true;
                            
                        } else if (!appVerFromDB.equals(appVerNum))
                        {
                            if (checkVersion(appVerFromDB, appVerNum, mkKey("APP_VER_ERR"), 
                                                                      mkKey("APP_VER_NEQ_OLD"), 
                                                                      mkKey("APP_VER_NEQ_NEW"),
                                                                      false))
                            {
                                doUpdateAppVer = true;
                            } else
                            {
                                CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                return SchemaUpdateType.Error;
                            }
                        }
                        
                        if (dbVersion != null && schemaVerFromDB != null && !schemaVerFromDB.equals(dbVersion))
                        {
                            String errKey = mkKey("DB_VER_NEQ");
                            if (checkVersion(schemaVerFromDB, dbVersion, 
                                             mkKey("DB_VER_ERR"), errKey, errKey, false))
                            {
                                doSchemaUpdate = true;
                            } else
                            {
                                CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                return SchemaUpdateType.Error;
                            }
                        }
                    } else
                    {
                        //If somebody somehow got a hold of an 'internal' version (via a conversion, or possibly by manually checking for updates.
                        doUpdateAppVer = true;
                        if (appVerNumArg != null && appVerNumArg.length() > 2)
                        {
                            doSchemaUpdate   = true; //Integer.parseInt(appVerNumArg.substring(2, 3)) == 0;
                            useSilentSuccess = true;
                        }
                    }
                } else
                {
                    doInsert = true;
                }
                
                try
                {
                    if (doSchemaUpdate || doInsert || doUpdateAppVer)
                    {
                        if (doSchemaUpdate || doInsert)
                        {
                            //SpecifySchemaUpdateService.attachUnhandledException();
                            //BasicSQLUtils.setSkipTrackExceptions(true);
                            
                            if (!askToUpdateSchema())
                            {
                                CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                return SchemaUpdateType.Error;
                            }
                            
                            String msg = UIRegistry.getResourceString("UPDATE_SCH_BACKUP");
                            int opt = UIRegistry.askYesNoLocalized("EXIT", "CONTINUE", msg, "MySQLBackupService.BACKUP_NOW");
                            if (opt == JOptionPane.YES_OPTION)
                            {
                                CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                return SchemaUpdateType.Error;  
                            }
                            
                            itUserNamePassword = DatabaseLoginPanel.getITUsernamePwd();
                            if (itUserNamePassword != null)
                            {
                                DBConnection dbc = DBConnection.getInstance();
                                
                                DBMSUserMgr dbmsMgr = DBMSUserMgr.getInstance();
                                if (dbmsMgr.connectToDBMS(itUserNamePassword.first, itUserNamePassword.second, dbc.getServerName()))
                                {
                                    int permissions = dbmsMgr.getPermissions(itUserNamePassword.first, dbConn.getDatabaseName());
                                    if (!((permissions & DBMSUserMgr.PERM_ALTER_TABLE) == DBMSUserMgr.PERM_ALTER_TABLE))
                                    {
                                        dbmsMgr.close();
                                        
                                        errMsgList.add("You must have permissions to alter database tables.");
                                        //CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                        return SchemaUpdateType.Error;
                                    }
                                }
                                dbmsMgr.close();
                                
                                frame = new ProgressFrame(getResourceString("UPDATE_SCHEMA_TITLE"));
                                frame.adjustProgressFrame();
                                frame.getCloseBtn().setVisible(false);
                                frame.getProcessProgress().setIndeterminate(true);
                                frame.setDesc(UIRegistry.getLocalizedMessage("UPDATE_SCHEMA", dbVersion));
                                
                                frame.setOverall(0, OVERALL_TOTAL);
                                
                                UIHelper.centerAndShow(frame);
                                
                                //BasicSQLUtils.setSkipTrackExceptions(true); // Needs to be reset
                                
                                boolean ok = manuallyFixDB(DatabaseDriverInfo.getDriver(dbc.getDriver()), dbc.getServerName(), dbc.getDatabaseName(), itUserNamePassword.first,itUserNamePassword.second);
                                if (!ok)
                                {
                                    frame.setVisible(false);
                                    return SchemaUpdateType.Error;
                                }
                                
                                ok = SpecifySchemaGenerator.updateSchema(DatabaseDriverInfo.getDriver(dbc.getDriver()), dbc.getServerName(), dbc.getDatabaseName(), itUserNamePassword.first, itUserNamePassword.second);
                                if (!ok)
                                {
                                    errMsgList.add("There was an error updating the schema.");
                                    frame.setVisible(false);
                                    return SchemaUpdateType.Error;
                                }
                                frame.setVisible(false);
                                
                                fixSchemaMappingScope();
                                
                                fixLocaleSchema();
                                
                            } else
                            {
                                CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                return SchemaUpdateType.Error;
                            }
                            
                            //UIHelper.attachUnhandledException();
                            //BasicSQLUtils.setSkipTrackExceptions(false);
                        }
                        
                        if (doInsert || (appVerFromDB == null && schemaVerFromDB == null))
                        {
                            SpVersion.createInitialRecord(dbConn.getConnection(), appVerNum, dbVersion);
                            
                        } else if (doSchemaUpdate || doUpdateAppVer)
                        {
                            recVerNum++;
                            SpVersion.updateRecord(dbConn.getConnection(), appVerNum, dbVersion, recVerNum, spverId);
                        }
                        boolean onlyAppVersion = !doSchemaUpdate && doUpdateAppVer;
                        return useSilentSuccess ? SchemaUpdateType.SuccessSilent : onlyAppVersion ? SchemaUpdateType.SuccessAppVer : SchemaUpdateType.Success;
                        
                    } else
                    {
                        return SchemaUpdateType.NotNeeded;
                    }
                    
                } catch (Exception e)
                {
                    e.printStackTrace();
                    //processUnhandledException(e);
                    
                } finally
                {
                    dbMgr.close();
                }
            }
        }
        return SchemaUpdateType.Error;
    }
    
    /**
     * @param e
     */
    private static void processUnhandledException(@SuppressWarnings("unused") final Throwable throwable)
    {
        /*if (UIHelper.isExceptionOKToThrow(throwable))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    String msg = "There was an error while updating the schema.";
                    UnhandledExceptionDialog dlg = new UnhandledExceptionDialog(msg, throwable);
                    UIHelper.centerAndShow(dlg);
                }
            });
        }*/
    }
    
    /**
     * Creates and attaches the UnhandledException handler for piping them to the dialog
     */
    @SuppressWarnings("unused")
    private static void attachUnhandledException()
    {
        log.debug("attachUnhandledException "+Thread.currentThread().getName()+ " "+Thread.currentThread().hashCode());
        
        Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                processUnhandledException(e);
            }
        });
        
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    log.debug("attachUnhandledException "+Thread.currentThread().getName()+ " "+Thread.currentThread().hashCode());
                    Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler()
                    {
                        public void uncaughtException(Thread t, Throwable e)
                        {
                            processUnhandledException(e);
                        }
                    });
                }
            });
        } catch (InterruptedException e1)
        {
            e1.printStackTrace();
        } catch (InvocationTargetException e1)
        {
            e1.printStackTrace();
        }
        
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                processUnhandledException(e);
            }
        });
        
    }


    /**
     * @param conn
     * @param databaseName
     * @param tableName
     * @param fieldName
     * @return length of field or null if field does not exist.
     */
    private Integer getFieldLength(final Connection conn, final String databaseName, final String tableName, final String fieldName)
    {
        //XXX portability. This is MySQL -specific.
        Vector<Object[]> rows = query(conn, "SELECT CHARACTER_MAXIMUM_LENGTH FROM `information_schema`.`COLUMNS` where TABLE_SCHEMA = '" +
                databaseName + "' and TABLE_NAME = '" + tableName + "' and COLUMN_NAME = '" + fieldName + "'");                    
        if (rows.size() == 0)
        {
            return null; //the field doesn't even exits
        } else 
        {
            return((Number )rows.get(0)[0]).intValue();
        }
    }
    
    /**
     * @param conn
     * @param databaseName
     * @param tableName
     * @param fieldName
     * @return length of field or null if field does not exist.
     */
    private String getFieldColumnType(final Connection conn, final String databaseName, final String tableName, final String fieldName)
    {
        //XXX portability. This is MySQL -specific.
        Vector<Object[]> rows = query(conn, "SELECT COLUMN_TYPE FROM `information_schema`.`COLUMNS` where TABLE_SCHEMA = '" +
                databaseName + "' and TABLE_NAME = '" + tableName + "' and COLUMN_NAME = '" + fieldName + "'");                    
        if (rows.size() == 0)
        {
            return null; //the field doesn't even exits
        } else 
        {
            return rows.get(0)[0].toString();
        }
    }
    /**
     * @param dbdriverInfo
     * @param hostname
     * @param databaseName
     * @param userName
     * @param password
     * @return
     * @throws SQLException
     */
    private boolean manuallyFixDB(final DatabaseDriverInfo dbdriverInfo, 
                                  final String             hostname,
                                  final String             databaseName,
                                  final String             userName,
                                  final String             password) throws SQLException
    {
        frame.setOverall(0, 30); // 23 + 7 
        
        String connectionStr = dbdriverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostname, databaseName, true, true,
                                                             userName, password, dbdriverInfo.getName());
        log.debug("generateSchema connectionStr: " + connectionStr);

        log.debug("Creating database connection to: " + connectionStr);
        // Now connect to other databases and "create" the Derby database
        DBConnection dbConn = null;
        try
        {
            dbConn = DBConnection.createInstance(dbdriverInfo.getDriverClassName(), dbdriverInfo.getDialectClassName(), 
                                                 databaseName, connectionStr, userName, password);
            if (dbConn != null && dbConn.getConnection() != null)
            {
                Connection conn = dbConn.getConnection();
                Statement  stmt = null;
                try
                {
                    stmt = conn.createStatement();
                    Integer count   = null;
                    int     rv      = 0;
                    
                    //---------------------------------------------------------------------------
                    //-- LocalityDetail
                    //---------------------------------------------------------------------------
                    String  tblName = getTableTitleForFrame(LocalityDetail.getClassTableId());
                    Integer len     = getFieldLength(conn, databaseName, tblName, "UtmDatum");
                    if (len == null)
                    {
                        count = getCount(tblName);
                        rv = update(conn, "ALTER TABLE localitydetail CHANGE getUtmDatum UtmDatum varchar(255)");
                        if (rv != count)
                        {
                            errMsgList.add("Unable to alter table: localitydetail");
                            return false;
                        }
                    } else 
                    {
                        if (len.intValue() != 255) 
                        {
                            count = getCount(tblName);
                            rv = update(conn, "ALTER TABLE localitydetail MODIFY UtmDatum varchar(255)");
                            if (rv != count)
                            {
                                errMsgList.add("Unable to alter table: localitydetail");
                                return false;
                            }
                        }
                    }
                    frame.incOverall();
                    
                    //---------------------------------------------------------------------------
                    //-- SpecifyUser
                    //---------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(SpecifyUser.getClassTableId());
                    len     = getFieldLength(conn, databaseName, tblName, "Password");
                    if (len == null)
                    {
                        errMsgList.add(String.format("Unable to update table: %", tblName));
                        return false;
                    }
                    if (len.intValue() != 255)
                    {
                        count = getCount(tblName);
                        rv = update(conn, "ALTER TABLE specifyuser MODIFY Password varchar(255)");
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    //---------------------------------------------------------------------------
                    //-- SpExportSchemaItem
                    //---------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(SpExportSchemaItem.getClassTableId());
                    len     = getFieldLength(conn, databaseName, tblName, "FieldName");
                    if (len == null)
                    {
                        errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                        return false;
                    }
                    if (len.intValue() != 64)
                    {
                        count = BasicSQLUtils.getCount("SELECT COUNT(*) FROM spexportschemaitem");
                        rv = update(conn, "ALTER TABLE spexportschemaitem MODIFY FieldName varchar(64)");
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    //---------------------------------------------------------------------------
                    //-- Agent
                    //---------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(Agent.getClassTableId());
                    len     = getFieldLength(conn, databaseName, tblName, "LastName");
                    if (len == null)
                    {
                        errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                        return false;
                    }
                    if (len.intValue() != 128)
                    {
                        count = getCount(tblName);
                        rv = update(conn, "ALTER TABLE agent MODIFY LastName varchar(128)");
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    //---------------------------------------------------------------------------
                    //-- SpExportSchema
                    //---------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(SpExportSchema.getClassTableId());
                    len     = getFieldLength(conn, databaseName, tblName, "SchemaName");
                    if (len == null)
                    {
                        errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                        return false;
                    }
                    if (len.intValue() != 80)
                    {
                        count = getCount(tblName);
                        rv = update(conn, "ALTER TABLE spexportschema MODIFY SchemaName varchar(80)");
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    len = getFieldLength(conn, databaseName, tblName, "SchemaVersion");
                    if (len == null)
                    {
                        errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                        return false;
                    }
                    if (len.intValue() != 80)
                    {
                        count = getCount(tblName);
                        rv = update(conn, "ALTER TABLE spexportschema MODIFY SchemaVersion varchar(80)");
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    frame.incOverall();

                    
//            		String checkSQL = "select SpExportSchemaMappingID, MappingName from spexportschemamapping "
//            			+ "where CollectionMemberID is null";
//            		Vector<Object[]> mappingsToFix = BasicSQLUtils.query(checkSQL);
//                    if (mappingsToFix != null && mappingsToFix.size() > 0)
//                    {
//            			Vector<Object> collectionIDs = BasicSQLUtils.querySingleCol("select UserGroupScopeID from collection");
//            			if (collectionIDs.size() == 1)
//            			{
//            				//easy
//            				BasicSQLUtils.update("update spexportschemamapping set CollectionMemberID = " + collectionIDs.get(0));
//            			}
//            			else 
//            			{
//            				for (Object[] row : mappingsToFix)
//            				{
//            					log.info("fixing mappings in multiple collection database");
//            					String cacheName = ExportToMySQLDB.fixTblNameForMySQL(row[1].toString());
//            					if (BasicSQLUtils.doesTableExist(DBConnection.getInstance().getConnection(), cacheName))
//            					{
//            						String cacheID = cacheName + "ID";
//            						String sql = "select distinct CollectionMemberID from collectionobject co inner join "
//            							+ cacheName + " cn on cn." + cacheID + " = co.CollectionObjectID";
//            						Vector<Object> collsInCache = BasicSQLUtils.querySingleCol(sql);
//            						if (collsInCache != null && collsInCache.size() == 1)
//            						{
//            							//easy
//            							String updateSQL = "update spexportschemamapping set CollectionMemberID = " + collsInCache.get(0)
//            								+ " where SpExportSchemaMappingID = " + row[0];
//            							log.info("Updating exportmapping with cache containing single collection: " + updateSQL);
//            							BasicSQLUtils.update(updateSQL);
//            					
//            						} else if (collsInCache != null && collsInCache.size() > 1) 
//            						{
//            							//This should never happen, but if it does, should ask user to choose.
//            							//Also need to update TimestampModified to force rebuild of cache...
//            							//but...
//            							String updateSQL = "update spexportschemamapping set CollectionMemberID = " + collsInCache.get(0)
//            								+ " where SpExportSchemaMappingID = " + row[0];
//            							log.info("Updating exportmapping with cache containing multiple collections: " + updateSQL);
//            							BasicSQLUtils.update(updateSQL);
//            						}
//            				
//            					} else
//            					{
//            						log.info("updating export mapping that has no cache: " + row[1] + " - " + row[0]);
//            						String discSQL = "select distinct DisciplineID from spexportschema es inner join spexportschemaitem esi "
//            							+ "on esi.SpExportSchemaID = es.SpExportSchemaID inner join spexportschemaitemmapping esim "
//            							+ "on esim.ExportSchemaItemID = esi.SpExportSchemaItemID where esim.SpExportSchemaMappingID "
//            							+ "= " + row[0];    	    			
//            						Object disciplineID = BasicSQLUtils.querySingleObj(discSQL);
//            						if (disciplineID != null)
//            						{
//            							String discCollSql = "select UserGroupScopeID from collection where DisciplineID = " + disciplineID;
//            							Vector<Object> collIDsInDisc = BasicSQLUtils.querySingleCol(discCollSql);
//            							if (collIDsInDisc != null && collIDsInDisc.size() == 1)
//            							{
//            								//easy
//            								String updateSQL = "update spexportschemamapping set CollectionMemberID = " + collIDsInDisc.get(0)
//            									+ " where SpExportSchemaMappingID = " + row[0];
//            								log.info("Updating exportmapping that has no cache and one collection in its discipline: " + updateSQL);
//            								BasicSQLUtils.update(updateSQL);
//                					
//            							} else if (collIDsInDisc != null && collIDsInDisc.size() > 1) 
//            							{
//            								//Picking the first collection. How likely is it to matter? Not very.
//            								String updateSQL = "update spexportschemamapping set CollectionMemberID = " + collIDsInDisc.get(0)
//            									+ " where SpExportSchemaMappingID = " + row[0];
//            								log.info("Updating exportmapping that has no cache and a discipline with multiple collections: " + updateSQL);
//            								BasicSQLUtils.update(updateSQL);
//            							}
//            						} else
//            						{
//            							throw new Exception("unable to find discipline for exportschemamapping " + row[0]);
//            						}
//            	    			
//            					}
//            				}
//            			}
//            	        //AppPreferences.getGlobalPrefs().putBoolean("FixExportSchemaCollectionMemberIDs", true);
//                    }
//                    frame.incOverall();

                    //---------------------------------------------------------------------------
                    //-- SpecifySchemaUpdateScopeFixer
                    //---------------------------------------------------------------------------
                    SpecifySchemaUpdateScopeFixer collectionMemberFixer = new SpecifySchemaUpdateScopeFixer(databaseName);
                    if (!collectionMemberFixer.fix(conn))
                    {
                        errMsgList.add("Error fixing CollectionMember tables");
                        return false;
                    }
                    
                    // Do updates for Schema 1.2
                    doFixesForDBSchemaVersions(conn, databaseName);  // increments 7 times
                    
                    // Find Accession NumberingSchemes that 'attached' to Collections
                    String postfix = " FROM autonumsch_coll ac Inner Join autonumberingscheme ans ON ac.AutoNumberingSchemeID = ans.AutoNumberingSchemeID WHERE ans.TableNumber = '7'";
                    log.debug("SELECT COUNT(*)" + postfix);
                    count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*)" + postfix);
                    if (count > 0)
                    {
                        // Get the Format name being used for each Collection's Access Number
                        Hashtable<Integer, String> collIdToFormatHash = new Hashtable<Integer, String>();
                        String sql = "SELECT c.UserGroupScopeId, ci.`Format` FROM collection c Inner Join discipline d ON c.DisciplineID = d.UserGroupScopeId " +
                                     "Inner Join splocalecontainer cn ON d.UserGroupScopeId = cn.DisciplineID " +
                                     "Inner Join splocalecontaineritem ci ON cn.SpLocaleContainerID = ci.SpLocaleContainerID " + 
                                     "WHERE ci.Name =  'accessionNumber'";
                        for (Object[] row : query(conn, sql))
                        {
                            collIdToFormatHash.put((Integer)row[0], row[1].toString());  // Key -> CollId, Value -> Format
                        }
                        
                        String ansSQL = "SELECT ac.CollectionID, ac.AutoNumberingSchemeID " + postfix;
                        log.debug(ansSQL);
                        int totCnt = 0;
                        for (Object[] row : query(conn, ansSQL))
                        {
                            sql = "DELETE FROM autonumsch_coll WHERE CollectionID = " + ((Integer)row[0]) + " AND AutoNumberingSchemeID = " + ((Integer)row[1]);
                            log.debug(sql);
                            rv = update(conn, sql);
                            if (rv != 1)
                            {
                                errMsgList.add("There was an error fixing the table: autonumsch_coll for CollectionID = " + ((Integer)row[0]) + " AND AutoNumberingSchemeID = " + ((Integer)row[1]));
                            }
                            totCnt++;
                        }
                        if (totCnt != count)
                        {
                            errMsgList.add("There was an error fixing the table: autonumsch_coll");
                        } else
                        {
                            rv = count;
                        }
                        frame.incOverall();

                    } else
                    {
                        rv = count;
                    }
                    
                    if (rv != count)
                    {
                        return false;
                    }
                    
                    //---------------------------------------------------------------------------
                    //-- Fixing
                    //---------------------------------------------------------------------------
                    String sql = "SELECT COUNT(d.UserGroupScopeId) CNT, d.UserGroupScopeId FROM division d INNER JOIN autonumsch_div ad ON d.UserGroupScopeId = ad.DivisionID " +
                                 "INNER JOIN autonumberingscheme ans ON ad.AutoNumberingSchemeID = ans.AutoNumberingSchemeID GROUP BY d.UserGroupScopeId";
                    log.debug(sql);
                    for (Object[] row : query(conn, sql))
                    {
                        Integer divId = ((Integer)row[1]);
                        if (((Long)row[0]) > 1)
                        {
                            sql = "SELECT  dv.UserGroupScopeId AS divId, ds.UserGroupScopeId AS dispId, ci.Name, ci.`Format` FROM division dv " +
                                  "Inner Join discipline ds ON dv.UserGroupScopeId = ds.DivisionID " +
                                  "Inner Join splocalecontainer c ON ds.UserGroupScopeId = c.DisciplineID " +
                                  "Inner Join splocalecontaineritem ci ON c.SpLocaleContainerID = ci.SpLocaleContainerID " +
                                  "WHERE ci.Name = 'accessionNumber' AND dv.UserGroupScopeId = " + divId;
                            Vector<String>             namesList = new Vector<String>();
                            Hashtable<String, Integer> formatNames = new Hashtable<String, Integer>();
                            for (Object[] innerRow : query(conn, sql))
                            {
                                String  formatName = innerRow[3].toString();
                                Integer dsid       = (Integer)innerRow[1];
                                if (formatNames.get(formatName) == null)
                                {
                                    formatNames.put(formatName, dsid);
                                    namesList.add(formatName);
                                }
                            }
                            
                            String desc = "<html>Accessions belong to the same Division. They must all share the same formatter.<BR>" +
                                          "Please choose a format below that will be the for your Division.<BR>";
                            ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>((Frame)UIRegistry.getTopWindow(), "Choose a Format", desc, ChooseFromListDlg.OK_BTN, namesList);
                            dlg.setVisible(true);
                            
                            String newFormatName = dlg.getSelectedObject();
                            
                            List<String> disciplineNameList = new ArrayList<String>();
                                
                            sql = "SELECT ans.AutoNumberingSchemeID FROM division d INNER JOIN autonumsch_div ad ON d.UserGroupScopeId = ad.DivisionID " +
                                  "INNER JOIN autonumberingscheme ans ON ad.AutoNumberingSchemeID = ans.AutoNumberingSchemeID WHERE d.UserGroupScopeId = " + divId;
                            log.debug(sql);
                            int cnt = 0;
                            for (Object idAnsObj : querySingleCol(conn, sql))
                            {
                                Integer ansId = (Integer)idAnsObj;
                                if (cnt > 0)
                                {
                                    sql = "DELETE FROM autonumsch_div WHERE DivisionID = " + divId + " AND AutoNumberingSchemeID = " + ansId;
                                    if (update(conn, sql) != 1)
                                    {
                                        errMsgList.add("There was an error fixing the table: autonumsch_div for DivisionID = " + divId + " AND AutoNumberingSchemeID = " + ansId);
                                        return false;
                                    }
                                    
                                    sql = "DELETE FROM autonumberingscheme WHERE AutoNumberingSchemeID = " + ansId;
                                    if (update(conn, sql) != 1)
                                    {
                                        errMsgList.add("There was an error fixing the table: autonumberingscheme; removing AutoNumberingSchemeID = " + ansId);
                                        return false;
                                    }
                                    
                                    sql = "SELECT SpLocaleContainerItemID, ds.Name FROM splocalecontaineritem ci INNER JOIN splocalecontainer c ON ci.SpLocaleContainerID = c.SpLocaleContainerID " +
                                          "INNER JOIN discipline ds ON c.DisciplineID = ds.UserGroupScopeId " +
                                          "INNER JOIN division dv ON ds.DivisionID = dv.UserGroupScopeId " +
                                          "WHERE ci.Name =  'accessionNumber' AND dv.UserGroupScopeId = " + divId + " AND NOT (ci.`Format` = '" + newFormatName + "')";
                                    
                                    log.debug(sql);
                                    
                                    for (Object[] idRow : query(conn, sql))
                                    {
                                        Integer spItemId = (Integer)idRow[0];
                                        String  dispName = idRow[1].toString();
                                        
                                        sql = "UPDATE splocalecontaineritem SET `Format`='"+newFormatName+"' WHERE SpLocaleContainerItemID  = " + spItemId;
                                        log.debug(sql);
                                        if (update(conn, sql) == 1)
                                        {
                                            disciplineNameList.add(dispName);
                                        } else
                                        {
                                            log.error("Error changing formatter name.");
                                        }
                                    }
                                }
                                cnt++;
                            }
                            
                            desc = "<html>The following Disciplines have had their Accession Number formatter changed.<BR>" +
                                   "This change may require some Accession Numbers to be changed.<BR>" + 
                                   "Please contact Specify Customer Support for additional help.<BR>";
                            dlg = new ChooseFromListDlg<String>((Frame)UIRegistry.getTopWindow(), "Accession Number Changes", desc, ChooseFromListDlg.OK_BTN, disciplineNameList);
                            dlg.createUI();
                            dlg.getOkBtn().setEnabled(true);
                            dlg.setVisible(true);

                        }
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- This will fix any Agents messed up by creating new Divisions
                    //-----------------------------------------------------------------------------
                    frame.setDesc("Fixing User's Agents..."); // I18N
                    fixAgentsDivsDisps(conn);
                    frame.incOverall();
                    
                    //fixSpUserAndAgents();

                    //-----------------------------------------------------------------------------
                    //-- This will add any new fields to the schema
                    //-----------------------------------------------------------------------------
                    //System.setProperty("AddSchemaTablesFields", "TRUE");
                    
                    //fixLocaleSchema();
                    
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //                                                                                                              //
                    // Schema Changes 1.4                                                                                          //
                    //                                                                                                              //
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    if (BasicSQLUtils.doesTableExist(conn, "agent_discipline"))
                    {
                        PreparedStatement pStmt    = null;
                        try
                        {
                            pStmt    = conn.prepareStatement("UPDATE agent SET DivisionID=? WHERE AgentID = ?");
                        
                            sql = "SELECT a.AgentID, d.DivisionID FROM agent AS a " +
                                  "Inner Join agent_discipline AS ad ON a.AgentID = ad.AgentID " +
                                  "Inner Join discipline AS d ON ad.DisciplineID = d.UserGroupScopeId " +    
                                  "WHERE a.DivisionID IS NULL";
        
                            for (Object[] row : query(conn, sql))
                            {
                                int agtId = (Integer)row[0];
                                int divId = (Integer)row[1];
                                pStmt.setInt(1, divId);
                                pStmt.setInt(2, agtId);
                                pStmt.executeUpdate();
                            }
                        } catch (Exception e1)
                        {
                            e1.printStackTrace();
                        } finally
                        {
                            try
                            {
                                if (pStmt != null)
                                {
                                    pStmt.close();
                                }
                            } catch (Exception ex) {}
                        }
                    

                        update(conn, "DROP TABLE agent_discipline");
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- Agent
                    //-----------------------------------------------------------------------------
                    // Add New Fields to Address
                    tblName = getTableTitleForFrame(Agent.getClassTableId());
                    String[] addrCols = {"DateType", "TINYINT(4)", "Title", 
                                         "DateOfBirthPrecision", "TINYINT(4)", "DateOfBirth", 
                                         "DateOfDeathPrecision", "TINYINT(4)", "DateOfDeath"};
                    if (!checkAndAddColumns(conn, databaseName, tblName, addrCols))
                    {
                        return false;
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- Address
                    //-----------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(Address.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, "Address3"))
                    {
                        frame.setDesc("Updating Address Fields...");
                        String fmtStr = "ALTER TABLE address ADD COLUMN Address%d VARCHAR(64) AFTER Address%d";
                        for (int i=3;i<6;i++)
                        {
                            update(conn, String.format(fmtStr, i, i-1));
                        }
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- LocalityDetail
                    //-----------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(LocalityDetail.getClassTableId());
                    String[] locDetCols = {"StartDepth",         "Double",      "Drainage", 
                                           "StartDepthUnit",     "TINYINT(4)",  "StartDepth",
                                           "StartDepthVerbatim", "VARCHAR(32)", "StartDepthUnit",
                                           "EndDepth",           "Double",      "StartDepthVerbatim", 
                                           "EndDepthUnit",       "TINYINT(4)",  "EndDepth",
                                           "EndDepthVerbatim",   "VARCHAR(32)", "EndDepthUnit"};
                    if (!checkAndAddColumns(conn, databaseName, tblName, locDetCols))
                    {
                        return false;
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- Locality
                    //-----------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(Locality.getClassTableId());
                    String[] locCols = {"Text1", "VARCHAR(255)", "SrcLatLongUnit", 
                                        "Text2", "VARCHAR(255)", "Text1"};
                    if (!checkAndAddColumns(conn, databaseName, tblName, locCols))
                    {
                        return false;
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- CollectionObjectAttribute
                    //-----------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(CollectionObjectAttribute.getClassTableId());
                    if (!addColumn(conn, databaseName, tblName, "Text15",  "VARCHAR(64)", "Text14"))
                    {
                        return false;
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- PaleoContext
                    //-----------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(PaleoContext.getClassTableId());
                    if (!addColumn(conn, databaseName, tblName, "ChronosStratEndID",  "INT", "ChronosStratID"))
                    {
                        return false;
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- Institution
                    //-----------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(Institution.getClassTableId());
                    String[] instCols = {"IsSingleGeographyTree",  "BIT(1)", "IsServerBased", 
                                         "IsSharingLocalities",    "BIT(1)", "IsSingleGeographyTree"};
                    if (checkAndAddColumns(conn, databaseName, tblName, instCols))
                    {
                        update(conn, "UPDATE institution SET IsSingleGeographyTree=0, IsSharingLocalities=0");
                    } else
                    {
                        return false;
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- GeologicTimePeriod
                    //-----------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(GeologicTimePeriod.getClassTableId());
                    String[] gtpCols = {"Text1", "VARCHAR(64)", "EndUncertainty", 
                                        "Text2", "VARCHAR(64)", "Text1"};
                    if (!checkAndAddColumns(conn, databaseName, tblName, gtpCols))
                    {
                        return false;
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- PreparationAttribute
                    //-----------------------------------------------------------------------------
                    // Fix Field Length
                    tblName = getTableTitleForFrame(PreparationAttribute.getClassTableId());
                    String prepAttrFld = "Text22";
                    len = getFieldLength(conn, databaseName, tblName, prepAttrFld);
                    if (len != null && len == 10)
                    {
                        alterFieldLength(conn, databaseName, tblName, prepAttrFld, 10, 50);
                    }
                    frame.incOverall(); // #19

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //                                                                                                              //
                    // Schema Changes 1.5                                                                                           //
                    //                                                                                                              //
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    //-----------------------------------------------------------------------------
                    //-- LocalityDetail
                    //-----------------------------------------------------------------------------
                    // Change column types for UTMEasting, UTMNorthing and UTMScale
                    tblName = getTableTitleForFrame(LocalityDetail.getClassTableId());
                    String columnType = getFieldColumnType(conn, databaseName, tblName, "UTMEasting");
                    if (columnType == null)
                    {
                        errMsgList.add(String.format(COL_TYP_NO_DET, tblName));
                        return false;
                    }
                    if (!columnType.trim().equalsIgnoreCase("DECIMAL(20,10)"))
                    {
                        count = getCount(tblName);
                        rv = update(conn, "ALTER TABLE localitydetail CHANGE COLUMN `UtmEasting` `UtmEasting` DECIMAL(20,10) NULL DEFAULT NULL, " +
                        		                        "CHANGE COLUMN `UtmNorthing` `UtmNorthing` DECIMAL(20,10) NULL DEFAULT NULL, " +
                        		                        "CHANGE COLUMN `UtmScale` `UtmScale` DECIMAL(20,10) NULL DEFAULT NULL");
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- GeoCoordDetail
                    //-----------------------------------------------------------------------------
                    // Change column types for MaxUncertaintityEst and NamedPlaceExtent
                    tblName    = getTableTitleForFrame(GeoCoordDetail.getClassTableId());
                    columnType = getFieldColumnType(conn, databaseName, tblName, "MaxUncertaintyEst");
                    if (columnType == null)
                    {
                        errMsgList.add(String.format(COL_TYP_NO_DET, tblName));
                        return false;
                    }
                    if (!columnType.trim().equalsIgnoreCase("DECIMAL(20,10)"))
                    {
                        count = getCount(tblName);
                        rv = update(conn, "ALTER TABLE geocoorddetail CHANGE COLUMN `MaxUncertaintyEst` `MaxUncertaintyEst` DECIMAL(20,10) NULL DEFAULT NULL, " +
                                                        "CHANGE COLUMN `NamedPlaceExtent` `NamedPlaceExtent` DECIMAL(20,10) NULL DEFAULT NULL");
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    
                    sql = "SELECT COUNT(*) FROM geocoorddetail WHERE AgentID IS NULL";
                    int total = BasicSQLUtils.getCountAsInt(conn, sql);
                    if (total > 0)
                    {
                        sql = "UPDATE geocoorddetail SET AgentID=CreatedByAgentID WHERE AgentID IS NULL";
                        rv  = update(conn, sql);
                    }
                    
                    frame.incOverall();
                    
                    //-----------------------------------------------------------------------------
                    //-- LoanPreparation
                    //-----------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(LoanPreparation.getClassTableId());
                    columnType = getFieldColumnType(conn, databaseName, tblName, "DescriptionOfMaterial");
                    if (columnType == null)
                    {
                        errMsgList.add(String.format(COL_TYP_NO_DET, tblName));
                        return false;
                    }
                    if (!columnType.trim().equalsIgnoreCase("text"))
                    {
                        count   = getCount(tblName);
                        rv      = update(conn, String.format("ALTER TABLE %s MODIFY DescriptionOfMaterial TEXT", tblName));
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    //-----------------------------------------------------------------------------
                    //-- ConservEvent
                    //-----------------------------------------------------------------------------
                    tblName = getTableTitleForFrame(ConservEvent.getClassTableId());
                    String[] consrvEvCols = {"Text1",  "VARCHAR(64)", "Remarks",
                                             "Text2",  "VARCHAR(64)", "Text1",
                                             "Number1",  "INT(11)",   "Text2",
                                             "Number2",  "INT(11)",   "Number1",
                                             "YesNo1", "BIT(1)",      "Number2",
                                             "YesNo2", "BIT(1)",      "YesNo1",};
                    if (!checkAndAddColumns(conn, databaseName, tblName, consrvEvCols))
                    {
                        return false;
                    }
                    frame.incOverall(); // #23
                    
                    //-----------------------------------------------------------------------------
                    //-- DNASequencingRun
                    //-----------------------------------------------------------------------------
                    String runByAgentID = "RunByAgentID";
                    tblName = getTableTitleForFrame(DNASequencingRun.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, runByAgentID))
                    {
                        if (addColumn(conn, databaseName, tblName, runByAgentID,  "INT(11)", "DNASequenceID"))
                        {
                            update(conn, "ALTER TABLE dnasequencingrun ADD KEY `FKDNASEQRUNRUNBYAGT` (`RunByAgentID`)");
                            update(conn, "ALTER TABLE dnasequencingrun ADD CONSTRAINT `FKDNASEQRUNRUNBYAGT` FOREIGN KEY (`RunByAgentID`) REFERENCES `agent` (`AgentID`)");
                            
                            if (addColumn(conn, databaseName, tblName, "PreparedByAgentID",  "INT(11)", "RunByAgentID"))
                            {
                                update(conn, "ALTER TABLE dnasequencingrun ADD KEY `FKDNASEQRUNPREPBYAGT` (`PreparedByAgentID`)");
                                update(conn, "ALTER TABLE dnasequencingrun ADD CONSTRAINT `FKDNASEQRUNPREPBYAGT` FOREIGN KEY (`PreparedByAgentID`) REFERENCES `agent` (`AgentID`)");
                            } else
                            {
                                return false;
                            }
                        } else
                        {
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    return true;
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    
                } finally
                {
                    if (stmt != null)
                    {
                        stmt.close();
                    }
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (dbConn != null) dbConn.close();
        }
        return false;
    }
    
    /**
     * Creates error message with all the field names and adds it to the error list.
     * @param tableName table name
     * @param fieldNames list of fields
     * @return error msg
     */
    private String generateErrFieldsMsg(final String tableName, final String[] fieldNames)
    {
        StringBuilder sb = new StringBuilder();
        for (String nm : fieldNames)
        {
            sb.append(nm);
            sb.append(",\n");
        }
        sb.setLength(sb.length()-2);
        String msg = String.format(ERR_ADDING_FIELDS, tableName, sb.toString());
        errMsgList.add(msg);
        return msg;
    }
    
    /**
     * Returns number of records in table.
     * @param tableName unique table name
     * @return count
     */
    private int getCount(final String tableName)
    {
        return BasicSQLUtils.getCountAsInt(String.format("SELECT COUNT(*) FROM %s", tableName));
    }
    
    /**
     * @param conn
     */
    public void fixAgentsDivsDisps(final Connection conn)
    {
        try
        {
            String sql = "SELECT T1.SpecifyUserID FROM (SELECT COUNT(su.SpecifyUserID) AS cnt, su.SpecifyUserID FROM specifyuser su INNER JOIN agent a ON su.SpecifyUserID = a.SpecifyUserID GROUP BY su.SpecifyUserID) T1 WHERE cnt > 1";
            //String sql = "SELECT SpecifyUserID FROM specifyuser";
            log.debug(sql);
            
            Vector<Integer> rows = queryForInts(conn, sql);
            if (rows.size() == 0)
            {
                return;
            }
            
            for (Integer spId : rows)
            {
                log.debug("-------- For SpUser: " + spId + " --------------");
                
                HashSet<Integer> currAgentsDivHash = new HashSet<Integer>();
                
                String fromClause = String.format(" FROM specifyuser su " +
                                                  "INNER JOIN agent a ON su.SpecifyUserID = a.SpecifyUserID " +
                                                  "WHERE su.SpecifyUserID = %d AND a.DivisionID IS NOT NULL ORDER BY a.TimestampModified ASC", spId);

                sql = "SELECT a.AgentID, a.DivisionID" + fromClause;
                
                Integer firstAgentId = null;
                Integer firstDivId     = null;
                
                HashMap<Integer, Integer> agentIdsWithDupDivs = new HashMap<Integer, Integer>();
                HashMap<Integer, Integer> divToAgentHash      = new HashMap<Integer, Integer>();
                HashMap<Integer, Integer> divToAgentToFixHash = new HashMap<Integer, Integer>();
                
                log.debug(sql);
                for (Object[] row : query(conn, sql))
                {
                    Integer agtId = (Integer)row[0];
                    Integer divId = (Integer)row[1];
                    
                    if (firstAgentId == null)
                    {
                        firstAgentId = agtId;
                        firstDivId     = divId;
                    }
                    
                    divToAgentHash.put(divId, agtId);
                    log.debug(String.format("Div: %d -> Agent: %d", divId, agtId));
                    
                    if (currAgentsDivHash.contains(divId))
                    {
                        agentIdsWithDupDivs.put(agtId, divId);
                        log.debug(String.format("Agent %d was a duplicate Agent for Div %d", agtId, divId));
                    } else
                    {
                        currAgentsDivHash.add(divId);
                    }
                }
                
                //--------------------------------------------------------------
                // Get the Disciplines for SpecifyUser from the Permissions
                //--------------------------------------------------------------
                ArrayList<Integer> divsForSpecifyUserList = new ArrayList<Integer>();
                HashSet<Integer>   divsForSpecifyUserHash = new HashSet<Integer>();
                
                sql = "SELECT DISTINCT dv.UserGroupScopeId FROM collection cln " +
                      "INNER JOIN spprincipal p ON cln.UserGroupScopeId = p.userGroupScopeID " +
                      "INNER JOIN discipline ds ON cln.DisciplineID = ds.UserGroupScopeId " +
                      "INNER JOIN division dv ON ds.DivisionID = dv.UserGroupScopeId " +
                      "INNER JOIN specifyuser_spprincipal su_pr ON p.SpPrincipalID = su_pr.SpPrincipalID " +
                      "INNER JOIN specifyuser su ON su_pr.SpecifyUserID = su.SpecifyUserID  WHERE su.SpecifyUserID = " + spId +
                      " ORDER BY dv.UserGroupScopeId";
                log.debug(sql);
                
                ArrayList<Integer> divsForSpUserWithNoAgent = new ArrayList<Integer>();

                // Gets all the Divisions that this SpecifyUser is assigned to
                // and make sure there is an Agent for that Division
                for (Integer divId : queryForInts(conn, sql))
                {
                    divsForSpecifyUserList.add(divId);
                    log.debug(String.format("spId: %d  div: %d", spId, divId));
                    divsForSpecifyUserHash.add(divId);
                    
                    if (divToAgentHash.get(divId) == null)
                    {
                        divsForSpUserWithNoAgent.add(divId);
                        log.debug(String.format("Div %d doesn't have an UserAgent", divId));
                    }
                }
                
                
                if (agentIdsWithDupDivs.size() == 0 && divsForSpUserWithNoAgent.size() == 0)
                {
                   continue; 
                }
                
                ArrayList<Integer> availAgents = new ArrayList<Integer>(agentIdsWithDupDivs.keySet());
                ArrayList<Integer> availDivs   = new ArrayList<Integer>(divsForSpUserWithNoAgent);

                // Loop thru all the Divisions that are missing an Agent
                // and use an already existing Agent.
                PreparedStatement pStmt = null;
                try
                {
                    pStmt = conn.prepareStatement("UPDATE agent SET DivisionID=? WHERE AgentID = ?");
                    
                    for (Integer divId : divsForSpUserWithNoAgent)
                    {
                        if (availAgents.size() > 0)
                        {
                            Integer agtId = availAgents.get(0);
                            availAgents.remove(0); // remove first element
                            
                            pStmt.setInt(1, divId);
                            pStmt.setInt(2, agtId);
                            pStmt.executeUpdate();
                            
                            log.debug(String.format("Set Agent %d to Div %d", agtId, divId));
                            
                            divToAgentToFixHash.put(divId, agtId);
                            
                            availDivs.remove(divId); // remove the DiviId object
                        } else
                        {
                            break; // out of extra agents
                        }
                    }
                } catch (Exception e1)
                {
                    e1.printStackTrace();
                    
                } finally
                {
                    try
                    {
                        if (pStmt != null)
                        {
                            pStmt.close();
                        }
                    } catch (Exception ex) {}
                }
                
                if (availDivs.size() > 0)
                {
                    // Finish looping thru all the Divisions that are missing an Agent
                    // and clone an Agent for them because we have already used up any extra Agents.
                    DataProviderSessionIFace session = null;
                    try
                    {
                        session = DataProviderFactory.getInstance().createSession();
                        
                        ArrayList<Agent> agentToBeAdded = new ArrayList<Agent>();
                        
                        // At this point the list 'availDivs' has divisions that do not have agents
                        for (Integer divId : availDivs)
                        {
                            Agent    clonableAgent = session.get(Agent.class, firstAgentId);  // get the agent to be duplicated
                            Division division      = session.get(Division.class, divId);
                            
                            Agent clonedAgent = (Agent)clonableAgent.clone();
                            clonedAgent.setAgentId(null);
                            clonedAgent.setVersion(0);
                            clonedAgent.setDivision(division);
                            
                            log.debug(String.format("Cloning Agent %d for Div %d", firstAgentId, divId));
                            
                            agentToBeAdded.add(clonedAgent);
                        }
                        
                        session.close();
                        
                        // Create a new session or the cloned agents don't save correctly
                        session = DataProviderFactory.getInstance().createSession();
                        session.beginTransaction();
                        for (Agent agent : agentToBeAdded)
                        {
                            Agent newAgent = (Agent)agent.clone();
                            session.save(newAgent);
                            
                            divToAgentToFixHash.put(newAgent.getDivision().getId(), newAgent.getId());
                        }
                        session.commit();
                        
                    } catch (Exception e1)
                    {
                        e1.printStackTrace();
                    } finally
                    {
                        try
                        {
                            if (session != null)
                            {
                                session.close();
                            }
                        } catch (Exception ex) {}
                    }
                }
                
                //----------------------------------------------------------------
                // Now fix all for the agent
                //----------------------------------------------------------------
                StringBuilder sb = new StringBuilder();
                
                // At this point all the divisions for a SpecyUser now have an Agent,
                // either an old one that was reused or a newly cloned one.
                // We shouldn't have any left over Agents, but the list 'availAgents'
                // will have any extra previous used Agents that need to be deleted.
                for (Integer agtId : availAgents)
                {
                    if (sb.length() > 0) sb.append(',');
                    sb.append(agtId);
                }
                
                String inClause = " WHERE %s in (" + sb.toString() + ") ";
                
                // Add this Original Mapping in
                divToAgentToFixHash.put(firstDivId, firstAgentId);
                
                
                //---------- accessionagent
                sql = "SELECT aa.AccessionAgentID, aa.AgentID, a.DivisionID " +
                      "FROM accessionagent aa INNER JOIN accession a ON aa.AccessionID = a.AccessionID ORDER BY aa.AgentID";
                fixAgents(conn, sql, "accessionagent", "aa.AgentID", divToAgentToFixHash, inClause);
                
                //---------- addressofrecord (Accession, Borrow, ExchangeIn, EchangeOut, Gift, Loan, RepositoryAgreement)
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, a.DivisionID " +
                "FROM addressofrecord aa INNER JOIN accession a ON aa.AddressOfRecordID = a.AddressOfRecordID ORDER BY aa.AgentID";
                fixAgents(conn, sql, "addressofrecord", "aa.AgentID", divToAgentToFixHash, inClause);
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, ei.DivisionID " +
                "FROM addressofrecord aa INNER JOIN exchangein ei ON aa.AddressOfRecordID = ei.AddressOfRecordID ORDER BY aa.AgentID";
                fixAgents(conn, sql, "addressofrecord", "aa.AgentID", divToAgentToFixHash, inClause);
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, eo.DivisionID " +
                "FROM addressofrecord aa INNER JOIN exchangeout eo ON aa.AddressOfRecordID = eo.AddressOfRecordID ORDER BY aa.AgentID";
                fixAgents(conn, sql, "addressofrecord", "aa.AgentID", divToAgentToFixHash, inClause);
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, g.DivisionID " +
                "FROM addressofrecord aa INNER JOIN gift g ON aa.AddressOfRecordID = g.AddressOfRecordID ORDER BY aa.AgentID";
                fixAgents(conn, sql, "addressofrecord", "aa.AgentID", divToAgentToFixHash, inClause);
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, l.DivisionID " +
                "FROM addressofrecord aa INNER JOIN loan l ON aa.AddressOfRecordID = l.AddressOfRecordID ";
                fixAgents(conn, sql, "addressofrecord", "aa.AgentID", divToAgentToFixHash, inClause);
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, ra.DivisionID " +
                "FROM addressofrecord aa INNER JOIN repositoryagreement ra ON aa.AddressOfRecordID = ra.AddressOfRecordID ";
                fixAgents(conn, sql, "addressofrecord", "aa.AgentID", divToAgentToFixHash, inClause);
                
                //---------- agentgeography
                sql = "SELECT ag.AgentGeographyID, ag.AgentID, dp.DivisionID " +
                        "FROM agentgeography ag INNER JOIN geography g ON ag.GeographyID = g.GeographyID " +
                        "INNER JOIN discipline dp ON g.GeographyTreeDefID = dp.GeographyTreeDefID ";
                fixAgents(conn, sql, "agentgeography", "ag.AgentID", divToAgentToFixHash, inClause);

                //---------- agentspecialty
                // (Don't Need To)
                
                //---------- agentvariant
                // (Don't Need To)
                
                //---------- appraisal
                sql = "SELECT ap.AppraisalID, ap.AgentID, a.DivisionID " +
                "FROM appraisal ap INNER JOIN accession a ON ap.AccessionID = a.AccessionID ";
                fixAgents(conn, sql, "appraisal", "ap.AgentID", divToAgentToFixHash, inClause);
                
                sql = "SELECT ap.AppraisalID, ap.AgentID, dp.DivisionID " +
                        "FROM appraisal ap INNER JOIN collectionobject co ON ap.AppraisalID = co.AppraisalID " +
                        "INNER JOIN collection c ON co.CollectionID = c.UserGroupScopeId " +
                        "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId ";
                fixAgents(conn, sql, "appraisal", "ap.AgentID", divToAgentToFixHash, inClause);
                
                //---------- author
                // (Skipping for now - no way to know)
                
                //---------- borrowagent
                sql = "SELECT ba.BorrowAgentID, ba.AgentID, dp.DivisionID FROM borrowagent ba " +
                      "INNER JOIN collection c ON ba.CollectionMemberID = c.UserGroupScopeId " +
                      "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId ";
                fixAgents(conn, sql, "borrowagent", "ba.AgentID", divToAgentToFixHash, inClause);

                //---------- borrowreturnmaterial
                sql = "SELECT brm.BorrowReturnMaterialID, brm.ReturnedByID, dp.DivisionID " +
                "FROM borrowreturnmaterial brm INNER JOIN borrowmaterial bm ON brm.BorrowMaterialID = bm.BorrowMaterialID " +
                "INNER JOIN collection c ON bm.CollectionMemberID = c.UserGroupScopeId " +
                "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "borrowreturnmaterial", "brm.ReturnedByID", divToAgentToFixHash, inClause);

                //---------- collectionobject
                sql = "SELECT co.CollectionObjectID, co.CatalogerID, dp.DivisionID " +
                "FROM collectionobject co INNER JOIN collection c ON co.CollectionID = c.UserGroupScopeId " +
                "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "collectionobject", "co.CatalogerID", divToAgentToFixHash, inClause);

                //---------- collector
                sql = "SELECT CollectorID, AgentID, DivisionID FROM collector";
                fixAgents(conn, sql, "collector", "AgentID", divToAgentToFixHash, inClause);

                //---------- conservevent
                // (Skipping for now - no way to know)
                
                //---------- deaccessionagent
                // (Skipping for now - no way to know)
                
                //---------- determination
                sql = "SELECT d.DeterminationID, d.DeterminerID, dp.DivisionID " +
                "FROM determination d INNER JOIN collection c ON d.CollectionMemberID = c.UserGroupScopeId " +
                "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "determination", "d.DeterminerID", divToAgentToFixHash, inClause);
                
                //---------- dnasequence
                sql = "SELECT DnaSequenceID, dna.AgentID, dp.DivisionID " +
                "FROM collection c INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId " +
                "INNER JOIN dnasequence dna ON dna.CollectionMemberID = c.UserGroupScopeId";
                fixAgents(conn, sql, "dnasequence", "dna.AgentID", divToAgentToFixHash, inClause);
                
                //---------- exchangein
                sql = "SELECT ExchangeInID, CatalogedByID, DivisionID FROM exchangein";
                fixAgents(conn, sql, "exchangein", "CatalogedByID", divToAgentToFixHash, inClause);
                
                //---------- exchangeout
                sql = "SELECT ExchangeOutID, CatalogedByID, DivisionID FROM exchangeout";
                fixAgents(conn, sql, "exchangeout", "CatalogedByID", divToAgentToFixHash, inClause);
                
                //---------- fieldnotebook
                sql = "SELECT fn.FieldNotebookID, fn.AgentID, dp.DivisionID " +
                "FROM fieldnotebook fn INNER JOIN discipline dp ON fn.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "fieldnotebook", "fn.AgentID", divToAgentToFixHash, inClause);
                
                //---------- fieldnotebookpageset
                sql = "SELECT FieldNotebookPageSetID, fnps.AgentID, dp.DivisionID " +
                "FROM fieldnotebookpageset fnps INNER JOIN discipline dp ON fnps.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "fieldnotebookpageset", "fnps.AgentID", divToAgentToFixHash, inClause);
                
                //---------- geocoorddetail
                sql = "SELECT gd.GeoCoordDetailID, gd.AgentID, dp.DivisionID " +
                "FROM geocoorddetail gd INNER JOIN locality l ON gd.LocalityID = l.LocalityID " +
                "INNER JOIN discipline dp ON l.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "geocoorddetail", "gd.AgentID", divToAgentToFixHash, inClause);
               
                //---------- giftagent
                sql = "SELECT ga.GiftAgentID, ga.AgentID, g.DivisionID, dp.DivisionID " +
                "FROM giftagent ga INNER JOIN gift g ON ga.GiftID = g.GiftID " +
                "INNER JOIN discipline dp ON g.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "giftagent", "ga.AgentID", divToAgentToFixHash, inClause);
                
                //---------- groupperson (Don't need to)

                //---------- inforequest
                sql = "SELECT ir.InfoRequestID, ir.AgentID, dp.DivisionID " +
                "FROM inforequest ir INNER JOIN collection c ON ir.CollectionMemberID = c.UserGroupScopeId " +
                "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "inforequest", "ir.AgentID", divToAgentToFixHash, inClause);
                
                //---------- loanagent
                sql = "SELECT la.LoanAgentID, la.AgentID, l.DivisionID, dp.DivisionID " +
                        "FROM loanagent la INNER JOIN loan l ON la.LoanID = l.LoanID " +
                        "INNER JOIN discipline dp ON l.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "loanagent", "la.AgentID", divToAgentToFixHash, inClause);
               
                //---------- loanreturnpreparation
                sql = "SELECT lrp.LoanReturnPreparationID, lrp.ReceivedByID, dp.DivisionID " +
                "FROM loanreturnpreparation lrp INNER JOIN discipline dp ON lrp.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "loanreturnpreparation", "lrp.ReceivedByID", divToAgentToFixHash, inClause);
                
                //---------- permit (No way to know)

                //---------- preparation
                sql = "SELECT PreparationID, p.PreparedByID, dp.DivisionID " +
                "FROM preparation p INNER JOIN collectionobject co ON p.CollectionObjectID = co.CollectionObjectID " +
                "INNER JOIN collection c ON co.CollectionID = c.UserGroupScopeId " +
                "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "preparation", "p.PreparedByID", divToAgentToFixHash, inClause);
                
                //---------- project (No way to know)

                //---------- repositoryagreement
                sql = "SELECT RepositoryAgreementID, AgentID, DivisionID FROM repositoryagreement";
                fixAgents(conn, sql, "repositoryagreement", "AgentID", divToAgentToFixHash, inClause);

                //---------- shipment
                
                sql = "SELECT s.ShipmentID, s.ShipperID, dp.DivisionID FROM shipment s INNER JOIN discipline dp ON s.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "shipment", "s.ShipperID", divToAgentToFixHash, inClause);
                
                sql = "SELECT s.ShipmentID, s.ShippedToID, dp.DivisionID FROM shipment s INNER JOIN discipline dp ON s.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "shipment", "s.ShippedToID", divToAgentToFixHash, inClause);
                
                sql = "SELECT s.ShipmentID, s.ShippedByID, dp.DivisionID FROM shipment s INNER JOIN discipline dp ON s.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "shipment", "s.ShippedByID", divToAgentToFixHash, inClause);
                
                //---------- AppResources
                
                sql = "SELECT ap.SpAppResourceDirID, ap.CreatedByAgentID, d.DivisionID FROM spappresourcedir AS ap " +
                      "Inner Join discipline AS d ON ap.DisciplineID = d.UserGroupScopeId ";
                fixAgents(conn, sql, "spappresourcedir", "ap.CreatedByAgentID", divToAgentToFixHash, inClause);
                
                sql = "SELECT ap.SpAppResourceDirID, ap.ModifiedByAgentID, d.DivisionID FROM spappresourcedir AS ap " +
                      "Inner Join discipline AS d ON ap.DisciplineID = d.UserGroupScopeId ";
                fixAgents(conn, sql, "spappresourcedir", "ap.ModifiedByAgentID", divToAgentToFixHash, inClause);
                
                sql = "SELECT r.SpAppResourceID, r.CreatedByAgentID, d.DivisionID FROM spappresource AS r " +
                        "Inner Join spappresourcedir AS rd ON r.SpAppResourceDirID = rd.SpAppResourceDirID " +
                        "Inner Join discipline AS d ON rd.DisciplineID = d.UserGroupScopeId ";
                fixAgents(conn, sql, "spappresource", "r.CreatedByAgentID", divToAgentToFixHash, inClause);

                sql = "SELECT r.SpAppResourceID, r.ModifiedByAgentID, d.DivisionID FROM spappresource AS r " +
                        "Inner Join spappresourcedir AS rd ON r.SpAppResourceDirID = rd.SpAppResourceDirID " +
                        "Inner Join discipline AS d ON rd.DisciplineID = d.UserGroupScopeId ";
                fixAgents(conn, sql, "spappresource", "r.ModifiedByAgentID", divToAgentToFixHash, inClause);
                
                sql = "SELECT ada.SpAppResourceDataID, ada.CreatedByAgentID, d.DivisionID FROM spappresourcedata AS ada " +
                        "Inner Join spappresource AS ar ON ada.SpAppResourceID = ar.SpAppResourceID " +
                        "Inner Join spappresourcedir AS ad ON ar.SpAppResourceDirID = ad.SpAppResourceDirID " +
                        "Inner Join discipline AS d ON ad.DisciplineID = d.UserGroupScopeId ";
                fixAgents(conn, sql, "spappresourcedata", "ada.CreatedByAgentID", divToAgentToFixHash, inClause);
                
                sql = "SELECT ada.SpAppResourceDataID, ada.ModifiedByAgentID, d.DivisionID FROM spappresourcedata AS ada " +
                        "Inner Join spappresource AS ar ON ada.SpAppResourceID = ar.SpAppResourceID " +
                        "Inner Join spappresourcedir AS ad ON ar.SpAppResourceDirID = ad.SpAppResourceDirID " +
                        "Inner Join discipline AS d ON ad.DisciplineID = d.UserGroupScopeId ";
                fixAgents(conn, sql, "spappresourcedata", "ada.ModifiedByAgentID", divToAgentToFixHash, inClause);
                
                //---------- Now Check All CreatedBy and Modified By
                fixAllTables(conn, firstAgentId, inClause, "CreatedByAgentID");
                fixAllTables(conn, firstAgentId, inClause, "ModifiedByAgentID");
                
                deleteAgentRelationships(conn, "address", availAgents);
                deleteAgentRelationships(conn, "agentvariant", availAgents);
                deleteAgentRelationships(conn, "agentgeography", availAgents);
                deleteAgentRelationships(conn, "agentspecialty", availAgents);
                deleteAgentAttachments(conn, availAgents);
                   
                // At this point all the divisions for a SpecyUser now have an Agent,
                // either an old one that was reused or a newly cloned one.
                // We shouldn't have any left over Agents, but the list 'availAgents'
                // will have any extra previous used Agents that need to be deleted.
                BasicSQLUtils.setSkipTrackExceptions(true);
                for (Integer agtId : availAgents)
                {
                    try
                    {
                        System.out.println("Delete Agent: "+ agtId);
                        update(conn, "DELETE FROM agent WHERE AgentID = "+agtId);
                        
                    } catch (Exception ex)
                    {
                        UIRegistry.showError("There was error deleting Agent ID %d.\nPlease write this number down and report this error immediately to the Specify team.");
                    }
                }
                BasicSQLUtils.setSkipTrackExceptions(false);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        }
    }
    
    /**
     * @param conn
     * @param tableName
     * @param ids
     */
    private void deleteAgentRelationships(final Connection conn, final String tableName, final List<Integer> ids)
    {
        for (Integer id : ids)
        {
            String sql = String.format("DELETE FROM %s WHERE AgentID = %d", tableName, id);
            if (update(conn, sql) != 1)
            {
                errMsgList.add(String.format("Error deleting Agent %d Table %s\n", id, tableName));
            }
        }
    }
    
    /**
     * @param conn
     * @param ids
     */
    private void deleteAgentAttachments(final Connection conn, final List<Integer> ids)
    {
        for (Integer id : ids)
        {
            String sql = " SELECT aa.AgentAttachmentID, a.AttachmentID FROM agentattachment aa INNER JOIN attachment a ON aa.AttachmentID = a.AttachmentID WHERE aa.AgentID = " + id;
            for (Object[] row  : query(sql))
            {
                Integer agtAtchId = (Integer)row[1];
                Integer atchId    = (Integer)row[2];
                
                sql = String.format("DELETE FROM attachment WHERE AttachmentID = %d", atchId);
                if (update(conn, sql) != 1)
                {
                    errMsgList.add(String.format("Error deleting Agent %d Table attachment\n", id));
                }
                
                sql = String.format("DELETE FROM agentattachment WHERE AgentAttachmentID = %d", agtAtchId);
                if (update(conn, sql) != 1)
                {
                    errMsgList.add(String.format("Error deleting Agent %d Table agentattachment\n", id));
                }
            }
        }
    }
    
    /**
     * @param conn
     * @param firstAgentId
     * @param fullInClause
     * @param fldName
     */
    private void fixAllTables(final Connection conn, final Integer firstAgentId, final String inClause, final String fldName)
    {
        String fullInClause = String.format(inClause, fldName);
        
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            String primaryKey = ti.getPrimaryKeyName();
            if (ti.getTableId() == SpTaskSemaphore.getClassTableId())
            {
                primaryKey = "TaskSemaphoreID";
                
            } else if (ti.getPrimaryKeyName().equals("userGroupId"))
            {
                primaryKey = ti.getClassObj().getSimpleName() + "ID";
            }
            
            String relName = fldName.toLowerCase().substring(0, fldName.length()-2);
            if (ti.getRelationshipByName(relName) != null)
            {
                String sql = String.format("SELECT COUNT(*) FROM %s %s", ti.getName(), fullInClause);
                //log.debug(sql);
                int total = BasicSQLUtils.getCountAsInt(sql);
                if (total > 0)
                {
                    //log.debug(total);
                    
                    int    percentStep = (int)((double)total * 0.02);
                    frame.setProcess(0, total);
                    frame.setDesc("Fixing "+ ti.getName());
                    
                    PreparedStatement ps = null;
                    try
                    {
                        sql = String.format("UPDATE %s SET %s = ? WHERE %s = ?", ti.getName(), fldName, primaryKey);
                        //log.debug(sql);
                        ps = conn.prepareStatement(sql);
                        
                        sql = String.format("SELECT %s FROM %s %s", primaryKey, ti.getName(), fullInClause);
                        //log.debug(sql);
                        int cnt = 0;
                        for (Integer id : queryForInts(sql))
                        {
                            ps.setInt(1, firstAgentId);
                            ps.setInt(2, id);
                            if (ps.executeUpdate() != 1)
                            {
                                errMsgList.add(String.format("Error updating Agent %d Table [%s] Field [%s] Primary [%s]\n", firstAgentId, ti.getName(), fldName, primaryKey));
                            }
                            
                            cnt++;
                            if (percentStep > 0 && cnt % percentStep == 0)
                            {
                                frame.setProcess(cnt);
                            }
                        }
                        frame.setProcess(total);
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        errMsgList.add(ex.getMessage());
                        
                    } finally
                    {
                        if (ps != null)
                        {
                            try
                            {
                                ps.close();
                            } catch (SQLException e) {}
                        } 
                    }
                }
            }
        }
    }
    
    /**
     * @param sql
     * @param inClause
     * @return
     */
    private String addInClause(final String sql, final String inClause)
    {
        final String ORDER_BY = "order by";
        int inx = sql.toLowerCase().indexOf(ORDER_BY);
        if (inx == -1)
        {
            return sql + inClause;
        }
        
        String str = sql.substring(0, inx-1) + inClause + sql.substring(inx);
        //System.out.println(str);
        return str;
    }
    
    /**
     * @param conn
     * @param sql
     * @param tableName
     * @param fldName
     * @param divToAgentToFixHash
     */
    private void fixAgents(final Connection conn,
                           final String     sqlArg, 
                           final String     tableName, 
                           final String     fieldName, 
                           final HashMap<Integer, Integer> divToAgentToFixHash,
                           final String     inClause)
    {
        PreparedStatement pStmt    = null;
        Statement         stmt     = null;
        try
        {
            String fullInClause = String.format(inClause, fieldName);
            String sql          = addInClause(sqlArg, fullInClause);
            
            int    inx         = sql.indexOf("FROM");
            String tmpSQL      = "SELECT COUNT(*) " + sql.substring(inx);
            //log.debug(tmpSQL);
            
            int    total       = BasicSQLUtils.getCountAsInt(tmpSQL);
            int    percentStep = (int)((double)total * 0.02);
            frame.setProcess(0, total);
            frame.setDesc("Fixing "+ tableName);
            
            String fldName = fieldName;
            inx = fieldName.indexOf('.');
            if (inx > -1)
            {
                fldName = fieldName.substring(inx+1);
            }
            
            stmt  = conn.createStatement();
            ResultSet         rs   = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            
            String recIdColName = rs.getMetaData().getColumnName(1);
            String sqlStr       = String.format("UPDATE %s SET %s=? WHERE %s = ?", tableName, fldName, recIdColName);
            //System.out.println(sql);
            //System.out.println(sqlStr);
            pStmt = conn.prepareStatement(sqlStr);
            
            int cnt = 0;
            int itemsFixed = 0;
            while (rs.next())
            {
                Integer recID   = rs.getInt(1);
                //Integer agentId = rs.getInt(2);
                Integer divId   = rs.getObject(3) != null ? rs.getInt(3) : null;
                Integer divId2  = rsmd.getColumnCount() == 4 ? rs.getInt(4) : null;
                
                if (divId2 != null && divId == null)
                {
                    divId = divId2;
                }
                
                Integer mappedAgentId = divToAgentToFixHash.get(divId);
                if (mappedAgentId != null)
                {
                    pStmt.setInt(1, mappedAgentId);
                    pStmt.setInt(2, recID);
                    if (pStmt.executeUpdate() != 1)
                    {
                        errMsgList.add(String.format("Error deleting Agent %d Table %s Field %s RecIdCol %s RecID %d", mappedAgentId, tableName, fldName, recIdColName, recID));
                    }
                    //log.debug(String.format("CNG %s.%s (%d)  Old: %d -> New: %d", tableName, fldName, recID, agentId, mappedAgentId));
                    itemsFixed++;
                }
                cnt++;
                if (percentStep > 0 && cnt % percentStep == 0)
                {
                    frame.setProcess(cnt);
                }
            }
            frame.setProcess(total);
            log.debug(String.format("%d Fixed ->%s.%s  (%s)", itemsFixed, tableName, fldName, sql));
            
            rs.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            errMsgList.add(ex.getMessage());
            
        } finally
        {
            try
            {
                if (pStmt != null)
                {
                    pStmt.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
            } catch (Exception ex)
            {
                
            }
        }
    }
    
    /**
     * @param conn
     * @param databaseName
     * @param tblName
     * @param fldName
     * @param origLen
     * @param newLen
     * @return
     */
    private boolean alterFieldLength(final Connection conn,
                                     final String databaseName,
                                     final String tblName, 
                                     final String fldName, 
                                     final int origLen, 
                                     final int newLen)
    {
        Integer len = getFieldLength(conn, databaseName, tblName, fldName);
        if (len == origLen)
        {
            BasicSQLUtils.setSkipTrackExceptions(false);
            try
            {
                update(conn, String.format("ALTER TABLE %s MODIFY %s varchar(%d)", tblName, fldName, newLen));
                //log.debug(String.format("Updating %s %s.%s - %d -> %d rv= %d", databaseName, tblName, fldName, origLen, newLen, rv));
            } catch (Exception ex)
            {
                errMsgList.add(String.format("Error - Updating %s %s.%s - %d -> %d  Excpt: %s", databaseName, tblName, fldName, origLen, newLen, ex.getMessage()));
            }
            BasicSQLUtils.setSkipTrackExceptions(false);
        }
        return true;
    }
    
    /**
     * Fixes the Schema for Database Version 1.2
     * @param conn
     * @throws Exception
     */
    private boolean doFixesForDBSchemaVersions(final Connection conn, final String databaseName) throws Exception
    {
        /////////////////////////////
        // PaleoContext
        /////////////////////////////
        getTableTitleForFrame(PaleoContext.getClassTableId());
        Integer len = getFieldLength(conn, databaseName, "paleocontext", "Text1");
        alterFieldLength(conn, databaseName, "paleocontext", "Text1", 32, 64);
        alterFieldLength(conn, databaseName, "paleocontext", "Text2", 32, 64);
        
        len = getFieldLength(conn, databaseName, "paleocontext", "Remarks");
        if (len == null)
        {
            int count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM paleocontext");
            int rv    = update(conn, "ALTER TABLE paleocontext ADD Remarks VARCHAR(60)");
            if (rv != count)
            {
                errMsgList.add("Error updating PaleoContext.Remarks");
                return false;
            }
        }
        frame.incOverall();
        
        DBConnection dbc = DBConnection.getInstance();

        /////////////////////////////
        // FieldNotebookPage
        /////////////////////////////
        getTableTitleForFrame(FieldNotebookPage.getClassTableId());
        len = getFieldLength(conn, databaseName, "fieldnotebookpage", "PageNumber");
        if (len != null && len == 16)
        {
            alterFieldLength(conn, databaseName, "fieldnotebookpage", "PageNumber", 16, 32);
            update(conn, "ALTER TABLE fieldnotebookpage ALTER COLUMN ScanDate DROP DEFAULT");
        }
        frame.incOverall();

        /////////////////////////////
        // Project Table
        /////////////////////////////
        alterFieldLength(conn, databaseName, "project", "projectname", 50, 128);
        frame.incOverall();

        /////////////////////////////
        // LocalityDetail
        /////////////////////////////
        
        getTableTitleForFrame(LocalityDetail.getClassTableId());
        
        boolean statusOK = true;
        String sql = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'localitydetail' AND COLUMN_NAME = 'UtmScale' AND DATA_TYPE = 'varchar'", dbc.getDatabaseName());
        int count = BasicSQLUtils.getCountAsInt(sql);
        if (count > 0)
        {
            Vector<Object[]> values = query("SELECT ld.LocalityDetailID, ld.UtmScale, l.LocalityName " +
            	                                          "FROM localitydetail ld INNER JOIN locality l ON ld.LocalityID = l.LocalityID WHERE ld.UtmScale IS NOT NULL");
            
            update(conn, "ALTER TABLE localitydetail DROP COLUMN UtmScale");
            String tblName = "localitydetail";
            addColumn(conn, databaseName, tblName, "UtmScale", "FLOAT",      "UtmOrigLongitude");
            addColumn(conn, databaseName, tblName, "MgrsZone", "VARCHAR(4)", "UtmScale");

            
            HashMap<String, String> badLocalitiesHash = new HashMap<String, String>();
            
            try
            {
                PreparedStatement pStmt = conn.prepareStatement("UPDATE localitydetail SET UtmScale=? WHERE LocalityDetailID=?");
                
                for (Object[] row : values)
                {
                    Integer locDetailId = (Integer)row[0];
                    String  scale       = (String)row[1];
                    String  locName     = (String)row[2];
                    
                    scale = StringUtils.contains(scale, ',') ? StringUtils.replace(scale, ",", "") : scale;
                    if (!StringUtils.isNumeric(scale))
                    {
                        badLocalitiesHash.put(locName, scale);
                        continue;
                    }
                    
                    float scaleFloat = 0.0f;
                    try
                    {
                        scaleFloat = Float.parseFloat(scale);
                        
                    } catch (NumberFormatException ex)
                    {
                        badLocalitiesHash.put(locName, scale);
                        continue;
                    }
                    
                    pStmt.setFloat(1, scaleFloat);
                    pStmt.setInt(2, locDetailId);
                    pStmt.execute();
                }
                pStmt.close();
                
            } catch (SQLException ex)
            {
                statusOK = false;
            }
            
            if (badLocalitiesHash.size() > 0)
            {
                try
                {
                    File file = new File(UIRegistry.getUserHomeDir() + File.separator + "localitydetailerrors.html");
                    TableWriter tblWriter = new TableWriter(file.getAbsolutePath(), "Locality Detail Errors");
                    tblWriter.startTable();
                    tblWriter.logHdr(new String[] {"Locality Name", "Scale"});
                    
                    for (String key : badLocalitiesHash.keySet())
                    {
                        tblWriter.log(key, badLocalitiesHash.get(key));
                    }
                    tblWriter.endTable();
                    tblWriter.flush();
                    tblWriter.close();
                    
                   
                    UIRegistry.showLocalizedError("LOC_DETAIL_ERRORS", badLocalitiesHash.size(), file.getAbsoluteFile());
                    
                    badLocalitiesHash.clear();
                    
                    if (file.exists())
                    {
                        try
                        {
                            AttachmentUtils.openURI(file.toURI());
                            
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        frame.incOverall();

        //////////////////////////////////////////////
        // collectingeventattribute Schema 1.3
        //////////////////////////////////////////////
        DBMSUserMgr  dbmsMgr = DBMSUserMgr.getInstance();
        if (dbmsMgr.connectToDBMS(itUserNamePassword.first, itUserNamePassword.second, dbc.getServerName()))
        {       
            boolean status = true;
            
            Connection connection = dbmsMgr.getConnection();
            try
            {
                // Add New Fields to Determination
                String tblName = getTableTitleForFrame(Determination.getClassTableId());
                addColumn(conn, databaseName, tblName, "VarQualifer",    "ALTER TABLE %s ADD COLUMN %s VARCHAR(16) AFTER Qualifier");
                addColumn(conn, databaseName, tblName, "SubSpQualifier", "ALTER TABLE %s ADD COLUMN %s VARCHAR(16) AFTER VarQualifer");
                frame.incOverall();

                // CollectingEventAttributes
                sql = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'collectingeventattribute' AND COLUMN_NAME = 'CollectionMemberID'", dbc.getDatabaseName());
                count = BasicSQLUtils.getCountAsInt(sql);
                
                connection.setCatalog(dbc.getDatabaseName());
                
                //int numCEAttrs = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM collectingeventattribute");
                if (count > 0)
                {
                    HashMap<Integer, Integer> collIdToDispIdHash = new HashMap<Integer, Integer>();
                    sql = "SELECT UserGroupScopeId, DisciplineID FROM collection";
                    for (Object[] cols : query(sql))
                    {
                        Integer colId = (Integer)cols[0];
                        Integer dspId = (Integer)cols[1];
                        collIdToDispIdHash.put(colId, dspId);
                    }
                    
                    count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM collectingeventattribute");
                    
                    IdMapperMgr.getInstance().setDBs(connection, connection);
                    IdTableMapper mapper = new IdTableMapper("ceattrmapper","id", "SELECT CollectingEventAttributeID, CollectionMemberID FROM collectingeventattribute", true, false);
                    mapper.setFrame(frame);
                    mapper.mapAllIdsNoIncrement(count > 0 ? count : null);
                    
                    Statement stmt = null;
                    try
                    {
                        getTableTitleForFrame(CollectingEventAttribute.getClassTableId());
                        
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        update(conn, "DROP INDEX COLEVATSColMemIDX on collectingeventattribute");
                        update(conn, "ALTER TABLE collectingeventattribute DROP COLUMN CollectionMemberID");
                        update(conn, "ALTER TABLE collectingeventattribute ADD COLUMN DisciplineID int(11)");
                        update(conn, "CREATE INDEX COLEVATSDispIDX ON collectingeventattribute(DisciplineID)");
                        
                        double inc     = count > 0 ? (100.0 / (double)count) : 0;
                        double cnt     = 0;
                        int    percent = 0;
                        frame.setProcess(0, 100);
                        frame.setProcessPercent(true);
                        
                        PreparedStatement pStmt = conn.prepareStatement("UPDATE collectingeventattribute SET DisciplineID=? WHERE CollectingEventAttributeID=?");
                        ResultSet rs = stmt.executeQuery("SELECT CollectingEventAttributeID FROM collectingeventattribute");
                        while (rs.next())
                        {
                            Integer ceAttrId = rs.getInt(1);
                            Integer oldColId = mapper.get(ceAttrId);
                            if (oldColId != null)
                            {
                                Integer dispId = collIdToDispIdHash.get(oldColId);
                                if (dispId != null)
                                {
                                    pStmt.setInt(1, dispId);
                                    pStmt.setInt(2, ceAttrId);
                                    pStmt.execute();
                                    
                                } else
                                {
                                    log.debug("Error getting hashed DisciplineID from Old Collection ID["+oldColId+"]  ceAttrId["+ceAttrId+"]");
                                }
                            } else
                            {
                                log.debug("Error getting mapped  Collection ID["+oldColId+"]  ceAttrId["+ceAttrId+"]");
                            }
                            
                            cnt += inc;
                            if (((int)cnt) > percent)
                            {
                                percent = (int)cnt;
                                frame.setProcess(percent);
                            }
                        }
                        rs.close();
                        pStmt.close();
                        
                        frame.setProcess(100);
                        
                    } catch (SQLException ex)
                    {
                        ex.printStackTrace();
                        
                    } finally
                    {
                        if (stmt != null) stmt.close();
                    }
                    mapper.cleanup();
                }
                frame.incOverall();

                //-----------------------------
                // Collectors
                //-----------------------------
                sql = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'collector' AND COLUMN_NAME = 'CollectionMemberID'", dbc.getDatabaseName());
                count = BasicSQLUtils.getCountAsInt(sql);
                if (count > 0)
                {
                    HashMap<Integer, Integer> collIdToDivIdHash = new HashMap<Integer, Integer>();
                    sql = "SELECT c.UserGroupScopeId, d.DivisionID FROM collection c INNER JOIN discipline d ON c.DisciplineID = d.UserGroupScopeId";
                    for (Object[] cols : query(sql))
                    {
                        Integer colId = (Integer)cols[0];
                        Integer divId = (Integer)cols[1];
                        collIdToDivIdHash.put(colId, divId);
                    }
                    
                    count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM collector");
                    
                    IdMapperMgr.getInstance().setDBs(connection, connection);
                    IdTableMapper mapper = new IdTableMapper("collectormap","id", "SELECT CollectorID, CollectionMemberID FROM collector", true, false);
                    mapper.setFrame(frame);
                    mapper.mapAllIdsNoIncrement(count > 0 ? count : null);
                    
                    getTableTitleForFrame(Collector.getClassTableId());
                    Statement stmt = null;
                    try
                    {
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        update(conn, "DROP INDEX COLTRColMemIDX on collector");
                        update(conn, "ALTER TABLE collector DROP COLUMN CollectionMemberID");
                        update(conn, "ALTER TABLE collector ADD COLUMN DivisionID INT(11)");
                        update(conn, "CREATE INDEX COLTRDivIDX ON collector(DivisionID)");
                        
                        double inc     = count > 0 ? (100.0 / (double)count) : 0;
                        double cnt     = 0;
                        int    percent = 0;
                        frame.setProcess(0, 100);
                        frame.setProcessPercent(true);
        
                        PreparedStatement pStmt = conn.prepareStatement("UPDATE collector SET DivisionID=? WHERE CollectorID=?");
                        ResultSet rs = stmt.executeQuery("SELECT CollectorID FROM collector");
                        while (rs.next())
                        {
                            Integer coltrId = rs.getInt(1);
                            Integer oldColId = mapper.get(coltrId);
                            if (oldColId != null)
                            {
                                Integer divId = collIdToDivIdHash.get(oldColId);
                                if (divId != null)
                                {
                                    pStmt.setInt(1, divId);
                                    pStmt.setInt(2, coltrId);
                                    pStmt.execute();
                                    
                                } else
                                {
                                    log.debug("Error getting hashed DisciplineID from Old Collection ID["+oldColId+"]");
                                }
                            } else
                            {
                                log.debug("Error getting mapped Collector ID["+oldColId+"]");
                            }
                            
                            cnt += inc;
                            if (((int)cnt) > percent)
                            {
                                percent = (int)cnt;
                                frame.setProcess(percent);
                            }
                        }
                        rs.close();
                        pStmt.close();
                        
                        frame.setProcess(100);
                        
                    } catch (SQLException ex)
                    {
                        ex.printStackTrace();
                        
                    } finally
                    {
                        if (stmt != null) stmt.close();
                    }
                    mapper.cleanup();
                    
                    frame.incOverall();
                }
                
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                
            } finally
            {
                frame.getProcessProgress().setIndeterminate(true);
                frame.setDesc("Loading updated schema...");
                
                if (!status)
                {
                    UIRegistry.showLocalizedError("SCHEMA_UPDATE_ERROR", errMsgStr);
                }
                
                dbmsMgr.close();
            }
        }
        
        return statusOK;
    }
    
    /**
     * Adds Agents to Divisions.
     */
    @SuppressWarnings("unused")
    private void fixSpUserAndAgents()
    {
        HashMap<Integer, HashSet<Integer>> spUserToDivHash = new HashMap<Integer, HashSet<Integer>>();
        HashMap<Integer, Integer>          spUserToAgentHash = new HashMap<Integer,Integer>();
        
        String sql = "SELECT su.SpecifyUserID, a.AgentID, a.DivisionID FROM specifyuser AS su Inner Join agent AS a ON su.SpecifyUserID = a.SpecifyUserID ";
        log.debug(sql);
        
        for (Object[] row : query(sql))
        {
            int spUserID = (Integer)row[0];
            int agtId    = (Integer)row[1];
            int divId    = (Integer)row[2];
            
            spUserToAgentHash.put(spUserID, agtId);
            
            HashSet<Integer> usersHash = spUserToDivHash.get(spUserID);
            if (usersHash == null)
            {
                usersHash = new HashSet<Integer>();
                spUserToDivHash.put(spUserID, usersHash);
            }
            usersHash.add(divId);
            log.debug(String.format("Collecing User %d in Division %d", spUserID, divId));
        }
        
        sql = "SELECT dsp.DivisionID, su.SpecifyUserID, dsp.DisciplineID FROM collection AS cln " +
                "Inner Join spprincipal AS p ON cln.UserGroupScopeId = p.userGroupScopeID " +
                "Inner Join specifyuser_spprincipal AS su_pr ON p.SpPrincipalID = su_pr.SpPrincipalID " +
                "Inner Join specifyuser AS su ON su_pr.SpecifyUserID = su.SpecifyUserID " +
                "Inner Join discipline AS dsp ON cln.DisciplineID = dsp.UserGroupScopeId WHERE su_pr.SpecifyUserID IS NOT NULL";
        log.debug(sql);
        
        for (Object[] row : query(sql))
        {
            int colDiv   = (Integer)row[0];
            int spUserID = (Integer)row[1];
            int dispID   = (Integer)row[2];
            
            HashSet<Integer> divs = spUserToDivHash.get(spUserID);
            if (divs == null || !divs.contains(colDiv))
            {
                String divName  = querySingleObj("SELECT Name FROM division WHERE DivisionID = "+colDiv);
                String userName = querySingleObj("SELECT Name FROM specifyuser WHERE SpecifyUserID = "+spUserID);
                
                log.debug(String.format("*********** No Agent for User %d (%s) in Division %d (%s) - (Going to Duplicate)", spUserID, userName, colDiv, divName));
                Integer agtId = spUserToAgentHash.get(spUserID);
                
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    session.beginTransaction();
                    
                    Agent      agent = session.get(Agent.class, agtId);
                    Discipline dsp   = session.get(Discipline.class, dispID);
                    Division   div   = session.get(Division.class, colDiv);
                    
                    Agent dupAgent = (Agent)agent.clone();
                    dupAgent.setDivision(div);
                    
                    session.save(dupAgent);
                    session.save(dsp);
                    
                    session.commit();
                    
                    log.debug(String.format("Saved New Agent %s (%d) for Discipline %s (%d), Division %s (%d)", 
                            dupAgent.getLastName(), dupAgent.getId(), dsp.getName(), dsp.getId(), div.getName(), div.getId()));
                    
                } catch (final Exception e1)
                {
                    e1.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
                    
                    if (session != null) session.rollback();
                    
                    log.error("Exception caught: " + e1.toString());
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            }
        }
    }
    
    
    /**
     * @param tableId the id of the table
     * @return the db table name
     */
    private String getTableTitleForFrame(final int tableId)
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);
        if (ti != null)
        {
            frame.setDesc(String.format("Updating %s Fields...", ti.getTitle()));
            return ti.getName();
        }
        throw new RuntimeException("Couldn't find table in Mgr for Table Id " + tableId);
    }
    
    /**
     * @param conn
     * @param dbName
     * @param tableName
     * @param columnInfo
     * @return
     */
    protected boolean checkAndAddColumns(final Connection conn, 
                                         final String dbName, 
                                         final String tableName, 
                                         final String[] columnInfo)
    {
        int inx = 0;
        while (inx < columnInfo.length)
        {
            if (!doesColumnExist(dbName, tableName, columnInfo[inx]))
            {
                if (!addColumn(conn, dbName, tableName, columnInfo[inx],  columnInfo[inx+1], columnInfo[inx+2]))
                {
                    String msg = String.format("Error adding DB: %s  TBL: %s  Col:%s  Typ:%s  After: %s", dbName, tableName, columnInfo[inx],  columnInfo[inx+1], columnInfo[inx+2]);
                    log.error(msg);
                    errMsgList.add(msg);
                    msg = generateErrFieldsMsg(tableName, columnInfo);
                    log.error(msg);
                    return false;
                }
            }
            inx += 3;
        }
        return true;
    }
    
    /**
     * @param conn
     * @param dbName
     * @param tableName
     * @param colName
     * @param updateSQL
     * @return
     */
    protected boolean addColumn(final Connection conn, 
                                final String dbName, 
                                final String tableName, 
                                final String colName, 
                                final String type,
                                final String afterField)
    {
        String updateSQL = "ALTER TABLE %s ADD COLUMN %s " + type + " AFTER " + afterField;
        return addColumn(conn, dbName, tableName, colName,  updateSQL);
    }
    
    /**
     * @param conn
     * @param dbName
     * @param tableName
     * @param colName
     * @param updateSQL
     * @return
     */
    protected boolean addColumn(final Connection conn, 
                                final String dbName, 
                                final String tableName, 
                                final String colName, 
                                final String updateSQL)
    {
        if (!doesColumnExist(dbName, tableName, colName))
        {
            String  fmtSQL = String.format(updateSQL, tableName, colName);
            int     rv     = update(conn, fmtSQL);
            if (rv == 0 && updateSQL.trim().toLowerCase().startsWith("alter table"))
            {
                return true;
            }
            
            if (rv != 1)
            {
                log.error(fmtSQL);
                errMsgList.add(String.format("Error adding column '%s' to table '%s'", colName, tableName));
                return false;
            }
        }
        return true; // true means it was OK if it wasn't added
    }
    
    /**
     * @param dbName
     * @param tableName
     * @param colName
     * @return
     */
    protected boolean doesColumnExist(final String dbName, final String tableName, final String colName)
    {
        String  sql  = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s' AND COLUMN_NAME = '%s'", dbName, tableName, colName);
        //log.debug(sql);
        return BasicSQLUtils.getCountAsInt(sql) == 1;
    }
    
    /**
     * 
     */
    protected void fixLocaleSchema()
    {
        PostInsertEventListener.setAuditOn(false);
        
        ProgressFrame            localFrame = null;
        DataProviderSessionIFace session    = null;
        Session                  hbSession  = null;
        Transaction              trans      = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
                
            hbSession = ((HibernateDataProviderSession)session).getSession(); // this is a no no
            trans     = hbSession.beginTransaction();
            List<Discipline> disciplines = session.getDataList(Discipline.class);
            
            localFrame = new ProgressFrame(getResourceString("UPDATE_SCHEMA_TITLE"));
            localFrame.adjustProgressFrame();
            localFrame.turnOffOverAll();
            localFrame.getCloseBtn().setVisible(false);
            
            localFrame.setDesc("Merging New Schema Fields...");
            if (disciplines.size() > 1)
            {
                localFrame.getProcessProgress().setIndeterminate(false);
                localFrame.setProcess(0, disciplines.size());
            } else
            {
                localFrame.getProcessProgress().setIndeterminate(true);
            }
            localFrame.setVisible(true);
            
            UIHelper.centerAndShow(localFrame);
            
            int cnt = 1;
            for (Discipline discipline : disciplines)
            {
                BuildSampleDatabase bsd = new BuildSampleDatabase();
                bsd.setSession(hbSession); 
                
                bsd.loadSchemaLocalization(discipline, 
                                           SpLocaleContainer.CORE_SCHEMA, 
                                           DBTableIdMgr.getInstance(),
                                           null,
                                           null,
                                           BuildSampleDatabase.UpdateType.eMerge,
                                           session);
                localFrame.setProcess(cnt++);
            }
            
            trans.commit();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
            try
            {
               if (trans != null) trans.rollback();
            } catch (Exception ex1)
            {
                ex1.printStackTrace();
            }
        } finally
        {
            if (localFrame != null) localFrame.setVisible(false);
            
            PostInsertEventListener.setAuditOn(true);

        }
    }
    
    /**
     * Assigns a CollectionMemberID to ExportSchema records 
     */
	protected void fixSchemaMappingScope() throws Exception {
		String checkSQL = "select SpExportSchemaMappingID, MappingName from spexportschemamapping "
				+ "where CollectionMemberID is null";
		Vector<Object[]> mappingsToFix = BasicSQLUtils.query(checkSQL);
		if (mappingsToFix != null && mappingsToFix.size() > 0)
		{
			Vector<Object> collectionIDs = BasicSQLUtils
					.querySingleCol("select UserGroupScopeID from collection");
			if (collectionIDs.size() == 1)
			{
				// easy
				BasicSQLUtils
						.update("update spexportschemamapping set CollectionMemberID = "
								+ collectionIDs.get(0));
			} else
			{
				for (Object[] row : mappingsToFix)
				{
					log.info("fixing mappings in multiple collection database");
					String cacheName = ExportToMySQLDB
							.fixTblNameForMySQL(row[1].toString());
					if (BasicSQLUtils.doesTableExist(DBConnection.getInstance()
							.getConnection(), cacheName))
					{
						String cacheID = cacheName + "ID";
						String sql = "select distinct CollectionMemberID from collectionobject co inner join "
								+ cacheName
								+ " cn on cn."
								+ cacheID
								+ " = co.CollectionObjectID";
						Vector<Object> collsInCache = BasicSQLUtils
								.querySingleCol(sql);
						if (collsInCache != null && collsInCache.size() == 1)
						{
							// easy
							String updateSQL = "update spexportschemamapping set CollectionMemberID = "
									+ collsInCache.get(0)
									+ " where SpExportSchemaMappingID = "
									+ row[0];
							log
									.info("Updating exportmapping with cache containing single collection: "
											+ updateSQL);
							BasicSQLUtils.update(updateSQL);

						} else if (collsInCache != null
								&& collsInCache.size() > 1)
						{
							// This should never happen, but if it does, should
							// ask user to choose.
							// Also need to update TimestampModified to force
							// rebuild of cache...
							// but...
							String updateSQL = "update spexportschemamapping set CollectionMemberID = "
									+ collsInCache.get(0)
									+ " where SpExportSchemaMappingID = "
									+ row[0];
							log
									.info("Updating exportmapping with cache containing multiple collections: "
											+ updateSQL);
							BasicSQLUtils.update(updateSQL);
						}

					} else
					{
						log.info("updating export mapping that has no cache: "
								+ row[1] + " - " + row[0]);
						String discSQL = "select distinct DisciplineID from spexportschema es inner join spexportschemaitem esi "
								+ "on esi.SpExportSchemaID = es.SpExportSchemaID inner join spexportschemaitemmapping esim "
								+ "on esim.ExportSchemaItemID = esi.SpExportSchemaItemID where esim.SpExportSchemaMappingID "
								+ "= " + row[0];
						Object disciplineID = BasicSQLUtils
								.querySingleObj(discSQL);
						if (disciplineID != null)
						{
							String discCollSql = "select UserGroupScopeID from collection where DisciplineID = "
									+ disciplineID;
							Vector<Object> collIDsInDisc = BasicSQLUtils
									.querySingleCol(discCollSql);
							if (collIDsInDisc != null
									&& collIDsInDisc.size() == 1)
							{
								// easy
								String updateSQL = "update spexportschemamapping set CollectionMemberID = "
										+ collIDsInDisc.get(0)
										+ " where SpExportSchemaMappingID = "
										+ row[0];
								log
										.info("Updating exportmapping that has no cache and one collection in its discipline: "
												+ updateSQL);
								BasicSQLUtils.update(updateSQL);

							} else if (collIDsInDisc != null
									&& collIDsInDisc.size() > 1)
							{
								// Picking the first collection. How likely is
								// it to matter? Not very.
								String updateSQL = "update spexportschemamapping set CollectionMemberID = "
										+ collIDsInDisc.get(0)
										+ " where SpExportSchemaMappingID = "
										+ row[0];
								log
										.info("Updating exportmapping that has no cache and a discipline with multiple collections: "
												+ updateSQL);
								BasicSQLUtils.update(updateSQL);
							}
						} else
						{
							throw new Exception(
									"unable to find discipline for exportschemamapping "
											+ row[0]);
						}

					}
				}
			}
		}
		// AppPreferences.getGlobalPrefs().putBoolean("FixExportSchemaCollectionMemberIDs",
		// true);
	}

    /**
     * Launches dialog for Importing and Exporting Forms and Resources.
     */
    public static boolean askToUpdateSchema()
    {
        if (SubPaneMgr.getInstance().aboutToShutdown())
        {
            Object[] options = { getResourceString("CONTINUE"),  //$NON-NLS-1$
                                 getResourceString("CANCEL")  //$NON-NLS-1$
                  };
            return JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                             getLocalizedMessage(mkKey("DB_SCH_UP")),  //$NON-NLS-1$
                                                             getResourceString(mkKey("DB_SCH_UP_TITLE")),  //$NON-NLS-1$
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        }
        return false;

    }
    
    /**
     * Check to make sure two version number are double and the new one is greater or equals to the existing one.
     * @param versionFromDB the existing version number
     * @param localVersionNum the new version number
     * @param notNumericErrKey I18N localization key
     * @param localVerTooOldKey I18N localization key
     * @param localVerTooNewKey I18N localization key
     * @return true if OK
     */
    protected boolean checkVersion(final String versionFromDB, 
                                   final String localVersionNum,
                                   final String notNumericErrKey,
                                   final String localVerTooOldKey,
                                   final String localVerTooNewKey,
                                   final boolean checkForTooNew)
    {
        try
        {
            log.debug("App - Prev["+versionFromDB+"] New["+localVersionNum+"]");
            
            Integer verNumFromDB = Integer.parseInt(StringUtils.replace(StringUtils.deleteWhitespace(versionFromDB), ".", ""));
            Integer localVerNum  = Integer.parseInt(StringUtils.replace(StringUtils.deleteWhitespace(localVersionNum), ".", ""));
            
            log.debug("App - Prev["+verNumFromDB+"] New["+localVerNum+"]");
            
            if (verNumFromDB > localVerNum)
            {
                UIRegistry.showLocalizedError(localVerTooOldKey, localVersionNum, versionFromDB);
                
                return false;
                
            } else if (checkForTooNew && verNumFromDB < localVerNum)
            {
                UIRegistry.showLocalizedError(localVerTooNewKey, localVersionNum, versionFromDB);
                return false;
            } 
            return true;
            
        } catch (NumberFormatException ex)
        {
            UIRegistry.showLocalizedError(notNumericErrKey, localVersionNum, versionFromDB);
        }
        
        return false;
    }

    /**
     * Changes all the contents of the Geography 'Name' field from the geonames 'name' to 'acsiiname' to 
     * get rid of the unprintable ascii characters.
     */
    public void updateGeographyNames()
    {
        final String FIXED_GEO = "FIXED.GEOGRAPHY";
        
        if (AppPreferences.getGlobalPrefs().getBoolean(FIXED_GEO, false))
        {
            //return;
        }
        
        String sql = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'geoname'", 
                                    DBConnection.getInstance().getDatabaseName());
        if (BasicSQLUtils.getCount(sql) == 0)
        {
            AppPreferences.getGlobalPrefs().putBoolean(FIXED_GEO, true);
            return;
        }
        
        final int numRecs = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM geoname ge INNER JOIN geography g ON ge.name = g.Name WHERE ge.Name <> ge.asciiname");
        if (BasicSQLUtils.getCount(sql) == 0)
        {
            AppPreferences.getGlobalPrefs().putBoolean(FIXED_GEO, true);
            return;
        }
        
        final ProgressFrame prefProgFrame = new ProgressFrame(getResourceString("UPDATE_SCHEMA_TITLE"));
        prefProgFrame.adjustProgressFrame();
        prefProgFrame.getCloseBtn().setVisible(false);
        prefProgFrame.getProcessProgress().setIndeterminate(true);
        prefProgFrame.setDesc(UIRegistry.getLocalizedMessage("UPDATE_GEO"));
        UIHelper.centerAndShow(prefProgFrame);
        
        prefProgFrame.setProcess(0, 100);
        
        SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>()
        {
            @Override
            protected Boolean doInBackground() throws Exception
            {
                Statement         stmt  = null;
                PreparedStatement pStmt = null;
                try
                {
                    Connection currDBConn = DBConnection.getInstance().getConnection();
                    
                    pStmt = currDBConn.prepareStatement("UPDATE geography SET Name=? WHERE GeographyID=?");
                    stmt  = currDBConn.createStatement();
                        
                    int    cnt    = 0;
                    String sqlStr = "SELECT ge.asciiname, g.GeographyID FROM geoname ge INNER JOIN geography g ON ge.name = g.Name WHERE ge.Name <> ge.asciiname";
                    ResultSet rs  = stmt.executeQuery(sqlStr);
                    while (rs.next())
                    {
                        pStmt.setString(1, rs.getString(1));
                        pStmt.setInt(2, rs.getInt(2));
                        if (pStmt.executeUpdate() != 1)
                        {
                            
                        }
                        
                        cnt++;
                        if (prefProgFrame != null && cnt % 100 == 0)
                        {
                            setProgress((int)(cnt / numRecs * 100.0));
                        }
                    }
                    rs.close();
                    
                    if (prefProgFrame != null)
                    {
                        prefProgFrame.setProcess(numRecs);
                    }
                    
                    AppPreferences.getGlobalPrefs().putBoolean(FIXED_GEO, true);
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, ex);

                } finally
                {
                    try
                    {
                        if (stmt != null)
                        {
                            stmt.close();
                        }
                        if (pStmt != null)
                        {
                            pStmt.close();
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                return true;
            }

            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#done()
             */
            @Override
            protected void done()
            {
                super.done();
                
                prefProgFrame.setVisible(false);
                prefProgFrame.dispose();
            }
            
        };
        
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(final PropertyChangeEvent evt) {
                        if ("progress".equals(evt.getPropertyName())) 
                        {
                            prefProgFrame.setProcess((Integer)evt.getNewValue());
                        }
                    }
                });
        worker.execute();
    }
}
