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

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
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
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpVersion;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
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
    
    private final int OVERALL_TOTAL = 16;
    
    private static final String APP          = "App";
    private static final String APP_REQ_EXIT = "AppReqExit";

    private Pair<String, String> itUserNamePassword = null;
    private ProgressFrame        frame;
    
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
            root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("schema_version.xml")));//$NON-NLS-1$
            if (root != null)
            {
                Element dbElement = (Element)root;
                if (dbElement != null)
                {
                    dbVersion = dbElement.getTextTrim();
                }
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
                    Vector<Object[]> rows = BasicSQLUtils.query(dbConn.getConnection(), "SELECT AppVersion, SchemaVersion, SpVersionID, Version FROM spversion ORDER BY TimestampCreated DESC");
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
                            if (checkVersion(appVerFromDB, appVerNum, "SpecifySchemaUpdateService.APP_VER_ERR", 
                                                                      "SpecifySchemaUpdateService.APP_VER_NEQ_OLD", 
                                                                      "SpecifySchemaUpdateService.APP_VER_NEQ_NEW",
                                                                      false))
                            {
                                doUpdateAppVer = true;
                            } else
                            {
                                CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                return SchemaUpdateType.Error;
                            }
                        }
                        
                        if (dbVersion != null && !schemaVerFromDB.equals(dbVersion))
                        {
                            String errKey = "SpecifySchemaUpdateService.DB_VER_NEQ";
                            if (checkVersion(schemaVerFromDB, dbVersion, 
                                             "SpecifySchemaUpdateService.DB_VER_ERR", errKey, errKey, false))
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
                            if (!askToUpdateSchema())
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
                                
                                fixLocaleSchema();
                                
                            } else
                            {
                                CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                return SchemaUpdateType.Error;
                            }
                        }
                        
                        if (doInsert || (appVerFromDB == null && schemaVerFromDB == null))
                        {
                            SpVersion.createInitialRecord(dbConn.getConnection(), appVerNum, dbVersion);
                            
                        } else if (doSchemaUpdate || doUpdateAppVer)
                        {
                            recVerNum++;
                            SpVersion.updateRecord(dbConn.getConnection(), appVerNum, dbVersion, recVerNum, spverId);
                        }
                        return useSilentSuccess ? SchemaUpdateType.SuccessSilent : SchemaUpdateType.Success;
                        
                    } else
                    {
                        return SchemaUpdateType.NotNeeded;
                    }
                    
                } catch (SQLException e)
                {
                    e.printStackTrace();
                    
                } finally
                {
                    dbMgr.close();
                }
            }
        }
        return SchemaUpdateType.Error;
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
        Vector<Object[]> rows = BasicSQLUtils.query(conn, "SELECT CHARACTER_MAXIMUM_LENGTH FROM `information_schema`.`COLUMNS` where TABLE_SCHEMA = '" +
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
                    Integer count = null;
                    stmt = conn.createStatement();
                    int rv = 0;
                    Integer len = getFieldLength(conn, databaseName, "localitydetail", "UtmDatum");
                    if (len == null)
                    {
                        count = BasicSQLUtils.getCount("SELECT COUNT(*) FROM localitydetail");
                        rv = BasicSQLUtils.update(conn, "ALTER TABLE localitydetail CHANGE getUtmDatum UtmDatum varchar(255)");
                        if (rv != count)
                        {
                            errMsgList.add("Unable to alter table: localitydetail");
                            return false;
                        }
                    } else 
                    {
                        if (len.intValue() != 255) 
                        {
                            count = BasicSQLUtils.getCount("SELECT COUNT(*) FROM localitydetail");
                            rv = BasicSQLUtils.update(conn, "ALTER TABLE localitydetail MODIFY UtmDatum varchar(255)");
                            if (rv != count)
                            {
                                errMsgList.add("Unable to alter table: localitydetail");
                                return false;
                            }
                        }
                    }
                    frame.incOverall();
                    
                    len = getFieldLength(conn, databaseName, "specifyuser", "Password");
                    if (len == null)
                    {
                        errMsgList.add("Unable to update table: specifyuser");
                        return false;
                    }
                    if (len.intValue() != 255)
                    {
                        count = BasicSQLUtils.getCount("SELECT COUNT(*) FROM specifyuser");
                        rv = BasicSQLUtils.update(conn, "ALTER TABLE specifyuser MODIFY Password varchar(255)");
                        if (rv != count)
                        {
                            errMsgList.add("Update count didn't match for update to table: specifyuser");
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    len = getFieldLength(conn, databaseName, "spexportschemaitem", "FieldName");
                    if (len == null)
                    {
                        errMsgList.add("Update count didn't match for update to table: spexportschemaitem");
                        return false;
                    }
                    if (len.intValue() != 64)
                    {
                        count = BasicSQLUtils.getCount("SELECT COUNT(*) FROM spexportschemaitem");
                        rv = BasicSQLUtils.update(conn, "ALTER TABLE spexportschemaitem MODIFY FieldName varchar(64)");
                        if (rv != count)
                        {
                            errMsgList.add("Update count didn't match for update to table: spexportschemaitem");
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    len = getFieldLength(conn, databaseName, "agent", "LastName");
                    if (len == null)
                    {
                        errMsgList.add("Update count didn't match for update to table: agent");
                        return false;
                    }
                    if (len.intValue() != 128)
                    {
                        count = BasicSQLUtils.getCount("SELECT COUNT(*) FROM agent");
                        rv = BasicSQLUtils.update(conn, "ALTER TABLE agent MODIFY LastName varchar(128)");
                        if (rv != count)
                        {
                            errMsgList.add("Update count didn't match for update to table: agent");
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    len = getFieldLength(conn, databaseName, "spexportschema", "SchemaName");
                    if (len == null)
                    {
                        errMsgList.add("Update count didn't match for update to table: spexportschema");
                        return false;
                    }
                    if (len.intValue() != 80)
                    {
                        count = BasicSQLUtils.getCount("SELECT COUNT(*) FROM spexportschema");
                        rv = BasicSQLUtils.update(conn, "ALTER TABLE spexportschema MODIFY SchemaName varchar(80)");
                        if (rv != count)
                        {
                            errMsgList.add("Update count didn't match for update to table: spexportschema");
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    len = getFieldLength(conn, databaseName, "spexportschema", "SchemaVersion");
                    if (len == null)
                    {
                        errMsgList.add("Update count didn't match for update to table: spexportschema");
                        return false;
                    }
                    if (len.intValue() != 80)
                    {
                        count = BasicSQLUtils.getCount("SELECT COUNT(*) FROM spexportschema");
                        rv = BasicSQLUtils.update(conn, "ALTER TABLE spexportschema MODIFY SchemaVersion varchar(80)");
                        if (rv != count)
                        {
                            errMsgList.add("Update count didn't match for update to table: spexportschema");
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    SpecifySchemaUpdateScopeFixer collectionMemberFixer = new SpecifySchemaUpdateScopeFixer(databaseName);
                    if (!collectionMemberFixer.fix(conn))
                    {
                        errMsgList.add("Error fixing CollectionMember tables");
                        return false;
                    }
                    
                    // Do updates for Schema 1.2
                    doFixesForDBSchema1_2(conn, databaseName);
                    
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
                        for (Object[] row : BasicSQLUtils.query(conn, sql))
                        {
                            collIdToFormatHash.put((Integer)row[0], row[1].toString());  // Key -> CollId, Value -> Format
                        }
                        
                        String ansSQL = "SELECT ac.CollectionID, ac.AutoNumberingSchemeID " + postfix;
                        log.debug(ansSQL);
                        int totCnt = 0;
                        for (Object[] row : BasicSQLUtils.query(conn, ansSQL))
                        {
                            sql = "DELETE FROM autonumsch_coll WHERE CollectionID = " + ((Integer)row[0]) + " AND AutoNumberingSchemeID = " + ((Integer)row[1]);
                            log.debug(sql);
                            rv = BasicSQLUtils.update(conn, sql);
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
                    
                    String sql = "SELECT COUNT(d.UserGroupScopeId) CNT, d.UserGroupScopeId FROM division d INNER JOIN autonumsch_div ad ON d.UserGroupScopeId = ad.DivisionID " +
                                 "INNER JOIN autonumberingscheme ans ON ad.AutoNumberingSchemeID = ans.AutoNumberingSchemeID GROUP BY d.UserGroupScopeId";
                    log.debug(sql);
                    for (Object[] row : BasicSQLUtils.query(conn, sql))
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
                            for (Object[] innerRow : BasicSQLUtils.query(conn, sql))
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
                            for (Object idAnsObj : BasicSQLUtils.querySingleCol(conn, sql))
                            {
                                Integer ansId = (Integer)idAnsObj;
                                if (cnt > 0)
                                {
                                    sql = "DELETE FROM autonumsch_div WHERE DivisionID = " + divId + " AND AutoNumberingSchemeID = " + ansId;
                                    if (BasicSQLUtils.update(conn, sql) != 1)
                                    {
                                        errMsgList.add("There was an error fixing the table: autonumsch_div for DivisionID = " + divId + " AND AutoNumberingSchemeID = " + ansId);
                                        return false;
                                    }
                                    
                                    sql = "DELETE FROM autonumberingscheme WHERE AutoNumberingSchemeID = " + ansId;
                                    if (BasicSQLUtils.update(conn, sql) != 1)
                                    {
                                        errMsgList.add("There was an error fixing the table: autonumberingscheme; removing AutoNumberingSchemeID = " + ansId);
                                        return false;
                                    }
                                    
                                    sql = "SELECT SpLocaleContainerItemID, ds.Name FROM splocalecontaineritem ci INNER JOIN splocalecontainer c ON ci.SpLocaleContainerID = c.SpLocaleContainerID " +
                                          "INNER JOIN discipline ds ON c.DisciplineID = ds.UserGroupScopeId " +
                                          "INNER JOIN division dv ON ds.DivisionID = dv.UserGroupScopeId " +
                                          "WHERE ci.Name =  'accessionNumber' AND dv.UserGroupScopeId = " + divId + " AND NOT (ci.`Format` = '" + newFormatName + "')";
                                    
                                    log.debug(sql);
                                    
                                    for (Object[] idRow : BasicSQLUtils.query(conn, sql))
                                    {
                                        Integer spItemId = (Integer)idRow[0];
                                        String  dispName = idRow[1].toString();
                                        
                                        sql = "UPDATE splocalecontaineritem SET `Format`='"+newFormatName+"' WHERE SpLocaleContainerItemID  = " + spItemId;
                                        log.debug(sql);
                                        if (BasicSQLUtils.update(conn, sql) == 1)
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
                    fixAgentsDivsDisps(conn);
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- This will add any new fields to the schema
                    //-----------------------------------------------------------------------------
                    //System.setProperty("AddSchemaTablesFields", "TRUE");
                    
                    //fixLocaleSchema();
                    
                    return true;
                    
                } catch (SQLException ex)
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
            dbConn.close();
        }
        return false;
    }
    
    /**
     * @param conn
     */
    public void fixAgentsDivsDisps(final Connection conn)
    {
        boolean doUpdate = true;
        PreparedStatement pStmt    = null;
        PreparedStatement pStmtDel = null;
        PreparedStatement pStmtAdd = null;
        try
        {
            pStmt    = conn.prepareStatement("UPDATE agent SET DivisionID=? WHERE AgentID = ?");
            pStmtDel = conn.prepareStatement("DELETE FROM agent_discipline WHERE AgentID = ?");
            pStmtAdd = conn.prepareStatement("INSERT INTO agent_discipline (AgentID, DisciplineID) VALUES(?, ?)");
            
            String sql = "SELECT T1.SpecifyUserID FROM (SELECT COUNT(su.SpecifyUserID) AS cnt, su.SpecifyUserID FROM specifyuser su INNER JOIN agent a ON su.SpecifyUserID = a.SpecifyUserID GROUP BY su.SpecifyUserID) T1 WHERE cnt > 1";
            Vector<Object> rows = BasicSQLUtils.querySingleCol(conn, sql);
            for (Object obj : rows)
            {
                Integer spId = (Integer)obj;
                
                //--------------------------------------------------------------
                // Get all the available Divisions
                //--------------------------------------------------------------
                /*Vector<Integer> divIds = new Vector<Integer>();
                for (Object divObj : BasicSQLUtils.querySingleCol(conn, "SELECT DivisionID FROM division ORDER BY DivisionID"))
                {
                    divIds.add((Integer)divObj);
                }*/
                
                //--------------------------------------------------------------
                // Get the Disciplines for SpecifyUser from the Permissions
                //--------------------------------------------------------------
                Vector<Pair<Integer, Integer>> divDspList = new Vector<Pair<Integer,Integer>>();
                HashSet<Integer> disciplinesHash = new HashSet<Integer>();
                HashSet<Integer> divisionHash    = new HashSet<Integer>();
                sql = "SELECT dv.UserGroupScopeId, ds.UserGroupScopeId FROM collection cln " +
                      "INNER JOIN spprincipal p ON cln.UserGroupScopeId = p.userGroupScopeID " +
                      "INNER JOIN discipline ds ON cln.DisciplineID = ds.UserGroupScopeId " +
                      "INNER JOIN division dv ON ds.DivisionID = dv.UserGroupScopeId " +
                      "INNER JOIN specifyuser_spprincipal su_pr ON p.SpPrincipalID = su_pr.SpPrincipalID " +
                      "INNER JOIN specifyuser su ON su_pr.SpecifyUserID = su.SpecifyUserID  WHERE su.SpecifyUserID = " + spId;
                
                for (Object[] row : BasicSQLUtils.query(conn, sql))
                {
                    divisionHash.add((Integer)row[0]);
                    disciplinesHash.add((Integer)row[1]);
                    divDspList.add(new Pair<Integer, Integer>((Integer)row[0], (Integer)row[1]));
                }
                
                //--------------------------------------------------------------
                // Fix up the Agent's DivisionID because they are all the same.
                //
                // Easier Just to delete all the agent_disicpline records
                // and re-add them.
                //--------------------------------------------------------------
                HashMap<Integer, Integer> divToAgentHash = new HashMap<Integer, Integer>();
                HashSet<Integer> agentHash = new HashSet<Integer>();
                Vector<Integer>  agents    = new Vector<Integer>();
                int inx = 0;
                sql = "SELECT a.AgentID FROM specifyuser su INNER JOIN agent a ON su.SpecifyUserID = a.SpecifyUserID WHERE su.SpecifyUserID = " + spId;
                for (Object agtObj : BasicSQLUtils.querySingleCol(conn, sql))
                {
                    Integer agentId = (Integer)agtObj;
                    agentHash.add(agentId);
                    
                    Pair<Integer, Integer> divDsp = divDspList.get(inx);
                    
                    pStmt.setInt(1, divDsp.first);
                    pStmt.setInt(2, agentId);
                    if (doUpdate) pStmt.execute();
                    
                    divToAgentHash.put(divDsp.first, agentId);
                    
                    pStmtDel.setInt(1, agentId);
                    if (doUpdate) pStmtDel.execute();
                    
                    agents.add(agentId);
                    
                    System.err.println(spId +" -> "+ agentId +" -> "+ divDsp.first);
                    inx++;
                }
                
                System.err.println("Number Agents: "+ agents.size() +" Number of Discipline: "+ divDspList.size());
                
                //--------------------------------------------------------------
                // Now re-add the agent_discipline records.
                //--------------------------------------------------------------
                inx = 0;
                for (Pair<Integer, Integer> divDsp : divDspList)
                {
                    pStmtAdd.setInt(1, agents.get(inx));
                    pStmtAdd.setInt(2, divDsp.second);
                    if (doUpdate) pStmtAdd.execute();
                    inx++;
                }
                
                //--------------------------------------------------------------------------------
                // Check all records with Agents to make sure the wrong agent wasn't used
                //--------------------------------------------------------------------------------
                /*HashSet<String> tiNames = new HashSet<String>();
                int cntHard = 0;
                int cntEasy = 0;
                for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
                {
                    for (DBRelationshipInfo ri : ti.getRelationships())
                    {
                        if (ri.getType() == DBRelationshipInfo.RelationshipType.ManyToOne &&
                            ri.getDataClass() == Agent.class && 
                            !ri.getColName().equals("CreatedByAgentID") &&
                            !ri.getColName().equals("ModifiedByAgentID"))
                        {
                            System.out.print(String.format("%s.%s", ti.getName(), ri.getName()));
                            //System.out.println(ti.getFieldByName("division") != null || ti.getFieldByName("discipline") != null ? " HAS" : " NOT");
                            
                            boolean easy = ti.getFieldByColumnName("DivisionID", true) != null ||
                                           ti.getFieldByColumnName("DisciplineID", true) != null ||
                                           ti.getFieldByColumnName("CollectionMemberId", true) != null;
                            
                            System.out.println(easy ? " EASY" : " HARD");
                            cntHard += easy ? 0 : 1;
                            cntEasy += easy ? 1 : 0;
                            tiNames.add(ti.getName());
                        }
                    }
                }
                System.out.print(String.format("Easy %d  Hard %d  TI %d", cntEasy, cntHard, tiNames.size()));
                
                System.out.println("//----------------------------------------------------------------");
                Vector<String> names = new Vector<String>(tiNames);
                Collections.sort(names);
                for (String nm : names)
                {
                    System.out.println("\n  //---------- "+nm);
                }*/
                
                //----------------------------------------------------------------
                
                //---------- accessionagent
                sql = "SELECT accessionagent.AccessionAgentID, accessionagent.AgentID, accession.DivisionID " +
                "FROM accessionagent INNER JOIN accession ON accessionagent.AccessionID = accession.AccessionID ";
                fixAgents(conn, sql, "accessionagent", "AgentID", divToAgentHash);
                
                //---------- addressofrecord (Accession, Borrow, ExchangeIn, EchangeOut, Gift, Loan, RepositoryAgreement)
                
                sql = "SELECT addressofrecord.AddressOfRecordID, addressofrecord.AgentID, accession.DivisionID " +
                "FROM addressofrecord INNER JOIN accession ON addressofrecord.AddressOfRecordID = accession.AddressOfRecordID ";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                sql = "SELECT addressofrecord.AddressOfRecordID, addressofrecord.AgentID, exchangein.DivisionID " +
                "FROM addressofrecord INNER JOIN exchangein ON addressofrecord.AddressOfRecordID = exchangein.AddressOfRecordID ";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                sql = "SELECT addressofrecord.AddressOfRecordID, addressofrecord.AgentID, exchangeout.DivisionID " +
                "FROM addressofrecord INNER JOIN exchangeout ON addressofrecord.AddressOfRecordID = exchangeout.AddressOfRecordID ";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                sql = "SELECT addressofrecord.AddressOfRecordID, addressofrecord.AgentID, gift.DivisionID " +
                "FROM addressofrecord INNER JOIN gift ON addressofrecord.AddressOfRecordID = gift.AddressOfRecordID ";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                sql = "SELECT addressofrecord.AddressOfRecordID, addressofrecord.AgentID, loan.DivisionID " +
                "FROM addressofrecord INNER JOIN loan ON addressofrecord.AddressOfRecordID = loan.AddressOfRecordID ";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                sql = "SELECT addressofrecord.AddressOfRecordID, addressofrecord.AgentID, repositoryagreement.DivisionID " +
                "FROM addressofrecord INNER JOIN repositoryagreement ON addressofrecord.AddressOfRecordID = repositoryagreement.AddressOfRecordID ";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                //---------- agentgeography
                sql = "SELECT agentgeography.AgentGeographyID, agentgeography.AgentID, discipline.DivisionID " +
                        "FROM agentgeography INNER JOIN geography ON agentgeography.GeographyID = geography.GeographyID " +
                        "INNER JOIN discipline ON geography.GeographyTreeDefID = discipline.GeographyTreeDefID ";
                fixAgents(conn, sql, "agentgeography", "AgentID", divToAgentHash);

                //---------- agentspecialty
                // (Don't Need To)
                
                //---------- agentvariant
                // (Don't Need To)
                
                //---------- appraisal
                sql = "SELECT appraisal.AppraisalID, appraisal.AgentID, accession.DivisionID " +
                "FROM appraisal INNER JOIN accession ON appraisal.AccessionID = accession.AccessionID ";
                fixAgents(conn, sql, "appraisal", "AgentID", divToAgentHash);
                
                sql = "SELECT appraisal.AppraisalID, appraisal.AgentID, discipline.DivisionID " +
                        "FROM appraisal INNER JOIN collectionobject ON appraisal.AppraisalID = collectionobject.AppraisalID " +
                        "INNER JOIN collection ON collectionobject.CollectionID = collection.UserGroupScopeId " +
                        "INNER JOIN discipline ON collection.DisciplineID = discipline.UserGroupScopeId ";
                fixAgents(conn, sql, "appraisal", "AgentID", divToAgentHash);
                
                //---------- author
                // (Skipping for now - no way to know)
                
                //---------- borrowagent
                sql = "SELECT borrowagent.BorrowAgentID, borrowagent.AgentID, discipline.DivisionID " +
                      "FROM borrowagent  " +
                      "INNER JOIN collection ON borrowagent.CollectionMemberID = collection.UserGroupScopeId " +
                      "INNER JOIN discipline ON collection.DisciplineID = discipline.UserGroupScopeId ";
                fixAgents(conn, sql, "borrowagent", "AgentID", divToAgentHash);

                //---------- borrowreturnmaterial
                sql = "SELECT BorrowReturnMaterialID, borrowreturnmaterial.ReturnedByID, discipline.DivisionID " +
                "FROM borrowreturnmaterial INNER JOIN borrowmaterial ON borrowreturnmaterial.BorrowMaterialID = borrowmaterial.BorrowMaterialID " +
                "INNER JOIN collection ON borrowmaterial.CollectionMemberID = collection.UserGroupScopeId " +
                "INNER JOIN discipline ON collection.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "borrowreturnmaterial", "ReturnedByID", divToAgentHash);

                //---------- collectionobject
                sql = "SELECT CollectionObjectID, collectionobject.CatalogerID, discipline.DivisionID " +
                "FROM collectionobject INNER JOIN collection ON collectionobject.CollectionID = collection.UserGroupScopeId " +
                "INNER JOIN discipline ON collection.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "collectionobject", "CatalogerID", divToAgentHash);

                //---------- collector
                sql = "SELECT CollectorID, collector.AgentID, discipline.DivisionID " +
                "FROM collector INNER JOIN collection ON collector.CollectionMemberID = collection.UserGroupScopeId " +
                "INNER JOIN discipline ON collection.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "collector", "AgentID", divToAgentHash);

                //---------- conservevent
                // (Skipping for now - no way to know)
                
                //---------- deaccessionagent
                // (Skipping for now - no way to know)
                
                //---------- determination
                sql = "SELECT DeterminationID, determination.DeterminerID, discipline.DivisionID " +
                "FROM determination INNER JOIN collection ON determination.CollectionMemberID = collection.UserGroupScopeId " +
                "INNER JOIN discipline ON collection.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "determination", "DeterminerID", divToAgentHash);
                
                //---------- dnasequence
                sql = "SELECT DnaSequenceID, dnasequence.AgentID, discipline.DivisionID " +
                "FROM collection INNER JOIN discipline ON collection.DisciplineID = discipline.UserGroupScopeId " +
                "INNER JOIN dnasequence ON dnasequence.CollectionMemberID = collection.UserGroupScopeId";
                fixAgents(conn, sql, "dnasequence", "AgentID", divToAgentHash);
                
                //---------- exchangein
                sql = "SELECT ExchangeInID, CatalogedByID, DivisionID FROM exchangein";
                fixAgents(conn, sql, "exchangein", "CatalogedByID", divToAgentHash);
                
                //---------- exchangeout
                sql = "SELECT ExchangeOutID, CatalogedByID, DivisionID FROM exchangeout";
                fixAgents(conn, sql, "exchangeout", "CatalogedByID", divToAgentHash);
                
                //---------- fieldnotebook
                sql = "SELECT FieldNotebookID, fieldnotebook.AgentID, discipline.DivisionID " +
                "FROM fieldnotebook INNER JOIN discipline ON fieldnotebook.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "fieldnotebook", "AgentID", divToAgentHash);
                
                //---------- fieldnotebookpageset
                sql = "SELECT FieldNotebookPageSetID, fieldnotebookpageset.AgentID, discipline.DivisionID " +
                "FROM fieldnotebookpageset INNER JOIN discipline ON fieldnotebookpageset.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "fieldnotebookpageset", "AgentID", divToAgentHash);
                
                //---------- geocoorddetail
                sql = "SELECT GeoCoordDetailID, geocoorddetail.AgentID, discipline.DivisionID " +
                "FROM geocoorddetail INNER JOIN locality ON geocoorddetail.LocalityID = locality.LocalityID " +
                "INNER JOIN discipline ON locality.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "geocoorddetail", "AgentID", divToAgentHash);
               
                //---------- giftagent
                sql = " SELECT giftagent.GiftAgentID, giftagent.AgentID, gift.DivisionID, discipline.DivisionID " +
                "FROM giftagent INNER JOIN gift ON giftagent.GiftID = gift.GiftID " +
                "INNER JOIN discipline ON gift.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "giftagent", "AgentID", divToAgentHash);
                
                //---------- groupperson (Don't need to)

                //---------- inforequest
                sql = "SELECT InfoRequestID, inforequest.AgentID, discipline.DivisionID " +
                "FROM inforequest INNER JOIN collection ON inforequest.CollectionMemberID = collection.UserGroupScopeId " +
                "INNER JOIN discipline ON collection.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "inforequest", "AgentID", divToAgentHash);
                
                //---------- loanagent
                sql = " SELECT loanagent.LoanAgentID, loanagent.AgentID, loan.DivisionID, discipline.DivisionID " +
                "FROM loanagent INNER JOIN loan ON loanagent.LoanID = loan.LoanID " +
                "INNER JOIN discipline ON loan.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "loanagent", "AgentID", divToAgentHash);
               
                //---------- loanreturnpreparation
                sql = "SELECT LoanReturnPreparationID, loanreturnpreparation.ReceivedByID, discipline.DivisionID " +
                "FROM loanreturnpreparation INNER JOIN discipline ON loanreturnpreparation.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "loanreturnpreparation", "ReceivedByID", divToAgentHash);
                
                //---------- permit (No way to know)

                //---------- preparation
                sql = "SELECT PreparationID, preparation.PreparedByID, discipline.DivisionID " +
                "FROM preparation INNER JOIN collectionobject ON preparation.CollectionObjectID = collectionobject.CollectionObjectID " +
                "INNER JOIN collection ON collectionobject.CollectionID = collection.UserGroupScopeId " +
                "INNER JOIN discipline ON collection.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "preparation", "PreparedByID", divToAgentHash);
                
                //---------- project (No way to know)

                //---------- repositoryagreement
                sql = "SELECT repositoryagreement.RepositoryAgreementID, repositoryagreement.AgentID, repositoryagreement.DivisionID FROM repositoryagreement";
                fixAgents(conn, sql, "repositoryagreement", "AgentID", divToAgentHash);

                //---------- shipment
                
                sql = "SELECT ShipmentID, shipment.ShipperID, discipline.DivisionID FROM shipment INNER JOIN discipline ON shipment.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "shipment", "ShipperID", divToAgentHash);
                
                sql = "SELECT ShipmentID, shipment.ShippedToID, discipline.DivisionID FROM shipment INNER JOIN discipline ON shipment.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "shipment", "ShippedToID", divToAgentHash);
                
                sql = "SELECT ShipmentID, shipment.ShippedByID, discipline.DivisionID  FROM shipment INNER JOIN discipline ON shipment.DisciplineID = discipline.UserGroupScopeId";
                fixAgents(conn, sql, "shipment", "ShippedByID", divToAgentHash);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                if (pStmt != null)
                {
                    pStmt.close();
                }
                if (pStmtDel != null)
                {
                    pStmtDel.close();
                }
                if (pStmtAdd != null)
                {
                    pStmtAdd.close();
                }
            } catch (Exception ex)
            {
                
            }
        }
    }
    
    private void fixAgents(final Connection conn,
                           final String sql, 
                           final String tableName, 
                           final String fldName, 
                           final HashMap<Integer, Integer> divToAgentHash)
    {
        PreparedStatement pStmt    = null;
        Statement         stmt     = null;
        try
        {
            
            stmt  = conn.createStatement();
            ResultSet         rs   = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            
            String recIdColName = rs.getMetaData().getColumnName(1);
            pStmt = conn.prepareStatement(String.format("UPDATE %s SET %s=? WHERE %s = ?", tableName, fldName, recIdColName));
            
            int itemsFixed = 0;
            while (rs.next())
            {
                Integer recID   = rs.getInt(1);
                Integer agentId = rs.getInt(2);
                Integer divId   = rs.getObject(3) != null ? rs.getInt(3) : null;
                Integer divId2  = rsmd.getColumnCount() == 4 ? rs.getInt(4) : null;
                
                if (divId2 != null && divId == null)
                {
                    divId = divId2;
                }
                
                Integer mappedAgentId = divToAgentHash.get(divId);
                if (mappedAgentId != null)
                {
                    pStmt.setInt(1, mappedAgentId);
                    pStmt.setInt(2, recID);
                    pStmt.execute();
                    log.debug(String.format("CNG %s.%s (%d)  Old: %d -> New: %d", tableName, fldName, recID, agentId, mappedAgentId));
                    itemsFixed++;
                }
            }
            
            log.debug(String.format("%d Fixed ->%s.%s  (%s)", itemsFixed, tableName, fldName, sql));
            
            rs.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
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
            int rv = BasicSQLUtils.update(conn, String.format("ALTER TABLE %s MODIFY %s varchar(%d)", tblName, fldName, newLen));
            System.err.println("rv= "+rv);
            /*if (rv != count)
            {
                errMsgList.add("Update count didn't match for update to table: spexportschema");
                return false;
            }*/
        }
        return true;
    }
    
    /**
     * Fixes the Schema for Database Version 1.2
     * @param conn
     * @throws Exception
     */
    private boolean doFixesForDBSchema1_2(final Connection conn, final String databaseName) throws Exception
    {
        /////////////////////////////
        // PaleoContext
        /////////////////////////////
        Integer len = getFieldLength(conn, databaseName, "paleocontext", "Text1");
        alterFieldLength(conn, databaseName, "paleocontext", "Text1", 32, 64);
        alterFieldLength(conn, databaseName, "paleocontext", "Text2", 32, 64);
        
        len = getFieldLength(conn, databaseName, "paleocontext", "Remarks");
        if (len == null)
        {
            int count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM paleocontext");
            int rv    = BasicSQLUtils.update(conn, "ALTER TABLE paleocontext ADD Remarks VARCHAR(60)");
            if (rv != count)
            {
                errMsgList.add("Error updating PaleoContext.Remarks");
                return false;
            }
        }
        frame.incOverall();

        /////////////////////////////
        // FieldNotebookPage
        /////////////////////////////
        len = getFieldLength(conn, databaseName, "fieldnotebookpage", "PageNumber");
        if (len != null && len == 16)
        {
            alterFieldLength(conn, databaseName, "fieldnotebookpage", "PageNumber", 16, 32);
            BasicSQLUtils.update(conn, "ALTER TABLE fieldnotebookpage ALTER COLUMN ScanDate DROP DEFAULT");
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
        
        boolean statusOK = true;
        int     count    = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM localitydetail WHERE UtmScale IS NOT NULL");
        if (count > 0)
        {
            Vector<Object[]> values = BasicSQLUtils.query("SELECT ld.LocalityDetailID, ld.UtmScale, l.LocalityName FROM localitydetail ld INNER JOIN locality l ON ld.LocalityID = l.LocalityID");
            
            BasicSQLUtils.update(conn, "ALTER TABLE localitydetail DROP COLUMN UtmScale");
            BasicSQLUtils.update(conn, "ALTER TABLE localitydetail ADD COLUMN UtmScale FLOAT AFTER utmOrigLongitude");
            BasicSQLUtils.update(conn, "ALTER TABLE localitydetail ADD COLUMN MgrsZone VARCHAR(4) AFTER UtmScale");
            
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
        DBConnection dbc     = DBConnection.getInstance();
        DBMSUserMgr  dbmsMgr = DBMSUserMgr.getInstance();
        if (dbmsMgr.connectToDBMS(itUserNamePassword.first, itUserNamePassword.second, dbc.getServerName()))
        {       
            Connection connection = dbmsMgr.getConnection();
            try
            {
                // Add New Fields to Determination
                String sql = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'determination' AND COLUMN_NAME = 'VarQualifer'", dbc.getDatabaseName());
                count = BasicSQLUtils.getCountAsInt(sql);
                if (count == 0)
                {
                    frame.setDesc("Updating SubSpQualifier...");
                    BasicSQLUtils.update(conn, "ALTER TABLE determination ADD COLUMN SubSpQualifier VARCHAR(16) AFTER Qualifier");
                    frame.setDesc("Updating VarQualifier...");
                    BasicSQLUtils.update(conn, "ALTER TABLE determination ADD COLUMN VarQualifier VARCHAR(16) AFTER SubSpQualifier");
                }
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
                    for (Object[] cols : BasicSQLUtils.query(sql))
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
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        BasicSQLUtils.update(conn, "DROP INDEX COLEVATSColMemIDX on collectingeventattribute");
                        BasicSQLUtils.update(conn, "ALTER TABLE collectingeventattribute DROP COLUMN CollectionMemberID");
                        BasicSQLUtils.update(conn, "ALTER TABLE collectingeventattribute ADD COLUMN DisciplineID int(11)");
                        BasicSQLUtils.update(conn, "CREATE INDEX COLEVATSDispIDX ON collectingeventattribute(DisciplineID)");
                        
                        double inc     = count > 0 ? (100.0 / (double)count) : 0;
                        double cnt     = 0;
                        int    percent = 0;
                        frame.setProcess(0, 100);
                        frame.setProcessPercent(true);
                        frame.setDesc("Updating Collecting Event Attributes...");
                        
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
                    for (Object[] cols : BasicSQLUtils.query(sql))
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
                    
                    frame.setDesc("Updating Collectors...");
                    Statement stmt = null;
                    try
                    {
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        BasicSQLUtils.update(conn, "DROP INDEX COLTRColMemIDX on collector");
                        BasicSQLUtils.update(conn, "ALTER TABLE collector DROP COLUMN CollectionMemberID");
                        BasicSQLUtils.update(conn, "ALTER TABLE collector ADD COLUMN DivisionID int(11)");
                        BasicSQLUtils.update(conn, "CREATE INDEX COLTRDivIDX ON collector(DivisionID)");
                        
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

                    frame.getProcessProgress().setIndeterminate(true);
                    frame.setDesc("Loading updated schema...");
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                
            } finally
            {
                dbmsMgr.close();
            }
        }
        
        return statusOK;
    }
    
    /**
     * @param sessionArg
     */
    protected void fixLocaleSchema()
    {
        PostInsertEventListener.setAuditOn(false);
        
        ProgressFrame            frame     = null;
        DataProviderSessionIFace session   = null;
        Session                  hbSession = null;
        Transaction              trans     = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
                
            hbSession = ((HibernateDataProviderSession)session).getSession(); // this is a no no
            trans     = hbSession.beginTransaction();
            List<Discipline> disciplines = session.getDataList(Discipline.class);
            
            frame = new ProgressFrame(getResourceString("UPDATE_SCHEMA_TITLE"));
            frame.adjustProgressFrame();
            frame.turnOffOverAll();
            frame.getCloseBtn().setVisible(false);
            
            frame.setDesc("Merging New Schema Fields...");
            if (disciplines.size() > 1)
            {
                frame.getProcessProgress().setIndeterminate(false);
                frame.setProcess(0, disciplines.size());
            } else
            {
                frame.getProcessProgress().setIndeterminate(true);
            }
            frame.setVisible(true);
            
            UIHelper.centerAndShow(frame);
            
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
                                           true,
                                           session);
                frame.setProcess(cnt++);
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
            frame.setVisible(false);
            
            PostInsertEventListener.setAuditOn(true);

        }
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
                                                             getLocalizedMessage("SpecifySchemaUpdateService.DB_SCH_UP"),  //$NON-NLS-1$
                                                             getResourceString("SpecifySchemaUpdateService.DB_SCH_UP_TITLE"),  //$NON-NLS-1$
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
        
        if (prefProgFrame!= null)
        {
            prefProgFrame.setProcess(0, 100);
        }
        
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
                        
                    int    cnt = 0;
                    String sql = "SELECT ge.asciiname, g.GeographyID FROM geoname ge INNER JOIN geography g ON ge.name = g.Name WHERE ge.Name <> ge.asciiname";
                    ResultSet rs  = stmt.executeQuery(sql);
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
        
        if (prefProgFrame != null)
        {
            worker.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(final PropertyChangeEvent evt) {
                            if ("progress".equals(evt.getPropertyName())) 
                            {
                                prefProgFrame.setProcess((Integer)evt.getNewValue());
                            }
                        }
                    });
        }
        
        worker.execute();
        
        /*try
        {
            //worker.get();// Blocks GUI Thread
            
            frame.setVisible(false);
            frame.dispose();
            frame = null;
            
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        } */
    }
}
