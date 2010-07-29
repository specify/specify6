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

import javax.swing.JOptionPane;
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
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEventAttribute;
import edu.ku.brc.specify.datamodel.CollectionObjectAttribute;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.FieldNotebookPage;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityDetail;
import edu.ku.brc.specify.datamodel.PaleoContext;
import edu.ku.brc.specify.datamodel.PreparationAttribute;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpVersion;
import edu.ku.brc.specify.tasks.subpane.security.NavigationTreeMgr;
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
                    doFixesForDBSchemaVersions(conn, databaseName);
                    
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
                    frame.setDesc("Fixing User's Agents...");
                    fixAgentsDivsDisps(conn);
                    frame.incOverall();
                    
                    //fixSpUserAndAgents();

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
        boolean           doUpdate = true;
        PreparedStatement pStmt    = null;
        PreparedStatement pStmtDel = null;
        PreparedStatement pStmtAdd = null;
        try
        {
            pStmt    = conn.prepareStatement("UPDATE agent SET DivisionID=? WHERE AgentID = ?");
            pStmtDel = conn.prepareStatement("DELETE FROM agent_discipline WHERE AgentID = ?");
            pStmtAdd = conn.prepareStatement("INSERT INTO agent_discipline (AgentID, DisciplineID) VALUES(?, ?)");
            
            // Remove all the agent_discipline records for agents that have users
            String sql = " SELECT a.AgentID FROM specifyuser s INNER JOIN agent a ON s.SpecifyUserID = a.SpecifyUserID";
            for (Object[] row : BasicSQLUtils.query(sql))
            {
                Integer agtId = (Integer)row[0];
                pStmtDel.setInt(1, agtId);
                if (doUpdate) pStmtDel.execute();
                log.debug("Removing all agent_disp for agentId: " + agtId);
            }
            
            sql = "SELECT T1.SpecifyUserID FROM (SELECT COUNT(su.SpecifyUserID) AS cnt, su.SpecifyUserID FROM specifyuser su INNER JOIN agent a ON su.SpecifyUserID = a.SpecifyUserID GROUP BY su.SpecifyUserID) T1 WHERE cnt > 1";
            //String sql = "SELECT SpecifyUserID FROM specifyuser";
            log.debug(sql);
            Vector<Integer> rows = BasicSQLUtils.queryForInts(conn, sql);
            for (Integer spId : rows)
            {
                log.debug("-------- For SpUser: " + spId + " --------------");
                
                //--------------------------------------------------------------
                // Get the Disciplines for SpecifyUser from the Permissions
                //--------------------------------------------------------------
                //HashMap<String, Pair<Integer, Integer>> divDspHashMap      = new HashMap<String, Pair<Integer,Integer>>();
                //HashSet<Integer>                        disciplinesHashSet = new HashSet<Integer>();
                //HashSet<Integer>                        divisionHashSet    = new HashSet<Integer>();
                
                ArrayList<Integer>        divsList        = new ArrayList<Integer>();

                sql = "SELECT DISTINCT dv.UserGroupScopeId FROM collection cln " +
                      "INNER JOIN spprincipal p ON cln.UserGroupScopeId = p.userGroupScopeID " +
                      "INNER JOIN discipline ds ON cln.DisciplineID = ds.UserGroupScopeId " +
                      "INNER JOIN division dv ON ds.DivisionID = dv.UserGroupScopeId " +
                      "INNER JOIN specifyuser_spprincipal su_pr ON p.SpPrincipalID = su_pr.SpPrincipalID " +
                      "INNER JOIN specifyuser su ON su_pr.SpecifyUserID = su.SpecifyUserID  WHERE su.SpecifyUserID = " + spId;
                log.debug(sql);
                
                // Gets all the Divisions that this SpecifyUser is assigned to
                for (Integer divId : BasicSQLUtils.queryForInts(conn, sql))
                {
                    divsList.add(divId);
                    log.debug(String.format("spId: %d  div: %d", divId, divId));
                }
                
                //--------------------------------------------------------------
                // Fix up the Agent's DivisionID because they are all the same.
                //
                // Easier to just delete all the agent_discipline records
                // and re-add them.
                //--------------------------------------------------------------
                HashMap<Integer, Integer>      divToAgentHash  = new HashMap<Integer, Integer>();
                HashMap<Integer, Integer>      agentToDivHash  = new HashMap<Integer, Integer>();
                
                sql = String.format("SELECT a.AgentID FROM specifyuser su " +
                            		"INNER JOIN agent a ON su.SpecifyUserID = a.SpecifyUserID " +
                            		"WHERE su.SpecifyUserID = %d ORDER BY a.TimestampModified ASC", spId);
                log.debug(sql);
                Vector<Integer>  agentsForSpUserList = BasicSQLUtils.queryForInts(conn, sql);
                ArrayList<Agent> agentsToBeDuped     = new ArrayList<Agent>();
                
                DataProviderSessionIFace session = null;
                try
                {
                    
                    session = DataProviderFactory.getInstance().createSession();
                    Agent firstAgent = session.get(Agent.class, agentsForSpUserList.get(0));
                    
                    session.beginTransaction();
                    int agtInx = 0;
                    for (Integer divId : divsList)
                    {
                        Division div = session.get(Division.class, divId);
                        
                        log.debug("agtInx:" + agtInx+" -  agentsForSpUserList.size(): " +agentsForSpUserList.size() +"  DivId: "+ divId);
                        if (agtInx < agentsForSpUserList.size())
                        {
                            Integer  agentId = agentsForSpUserList.get(agtInx);
                            Agent    agent   = session.get(Agent.class, agentId);
                            
                            agent.setDivision(div);
                            session.saveOrUpdate(agent);
                            log.debug("Setting - agentId: " + agentId+" ->  Div: " +divId);
                            
                            agentToDivHash.put(agentId, divId);
                            divToAgentHash.put(divId, agentId);
                            
                            if (agtInx > 0)
                            {
                                Agent clonedAgent = (Agent)firstAgent.clone();
                                clonedAgent.setAgentId(agent.getId());
                                clonedAgent.setVersion(agent.getVersion());
                                clonedAgent.setDivision(agent.getDivision());
                                agentsToBeDuped.add(clonedAgent);
                            }
                            
                            log.debug("agtInx:" + agtInx+" -  spId: " +spId +" ->  AgtId: "+ agentId +" ->  DivId: "+ divId);
                            agtInx++;
                            
                        } else
                        {
                            Agent dupAgent = (Agent)firstAgent.clone();
                            dupAgent.setAgentId(null);
                            dupAgent.setDivision(div);
                            dupAgent.setVersion(0);
                            
                            session.saveOrUpdate(dupAgent);
                            
                            Integer newAgentId = dupAgent.getAgentId();
                            agentToDivHash.put(newAgentId, divId);
                            divToAgentHash.put(divId, newAgentId);
                            
                            log.debug(String.format("Saved New Agent %s (%d) for Division %s (%d)", 
                                      dupAgent.getLastName(), newAgentId, div.getName(), div.getId()));
                        }
                    }
                    
                    session.commit();
                    
                } catch (final Exception e1)
                {
                    e1.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
                    
                    session.rollback();
                    
                    log.error("Exception caught: " + e1.toString());
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
                
                session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    session.beginTransaction();
                    for (Agent agent : agentsToBeDuped)
                    {
                        agent = session.merge(agent);
                        session.save(agent);
                    }
                    session.commit();
                    
                } catch (final Exception e1)
                {
                    e1.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
                    
                    session.rollback();
                    
                    log.error("Exception caught: " + e1.toString());
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
                
                
                sql = "SELECT dsp.UserGroupScopeId, dv.UserGroupScopeId FROM division dv INNER JOIN discipline dsp ON dv.UserGroupScopeId = dsp.DivisionID";
                for (Object[] r : BasicSQLUtils.query(conn, sql))
                {
                    Integer dspId = (Integer)r[0];
                    Integer divId = (Integer)r[1];
                    Integer agtId = divToAgentHash.get(divId);
                    
                    pStmtAdd.setInt(1, agtId);
                    pStmtAdd.setInt(2, dspId);
                    pStmtAdd.execute();
                    
                    log.debug(String.format("Inserted - AgentId: %d DispId: %d", agtId, dspId));
                }
                
                //----------------------------------------------------------------
                // Now fix the all then agent
                //---------- accessionagent
                sql = "SELECT aa.AccessionAgentID, aa.AgentID, a.DivisionID " +
                      "FROM accessionagent aa INNER JOIN accession a ON aa.AccessionID = a.AccessionID ORDER BY aa.AgentID";
                fixAgents(conn, sql, "accessionagent", "AgentID", divToAgentHash);
                
                //---------- addressofrecord (Accession, Borrow, ExchangeIn, EchangeOut, Gift, Loan, RepositoryAgreement)
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, a.DivisionID " +
                "FROM addressofrecord aa INNER JOIN accession a ON aa.AddressOfRecordID = a.AddressOfRecordID ORDER BY aa.AgentID";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, ei.DivisionID " +
                "FROM addressofrecord aa INNER JOIN exchangein ei ON aa.AddressOfRecordID = ei.AddressOfRecordID ORDER BY aa.AgentID";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, eo.DivisionID " +
                "FROM addressofrecord aa INNER JOIN exchangeout eo ON aa.AddressOfRecordID = eo.AddressOfRecordID ORDER BY aa.AgentID";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, g.DivisionID " +
                "FROM addressofrecord aa INNER JOIN gift g ON aa.AddressOfRecordID = g.AddressOfRecordID ORDER BY aa.AgentID";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, l.DivisionID " +
                "FROM addressofrecord aa INNER JOIN loan l ON aa.AddressOfRecordID = l.AddressOfRecordID ";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                sql = "SELECT aa.AddressOfRecordID, aa.AgentID, ra.DivisionID " +
                "FROM addressofrecord aa INNER JOIN repositoryagreement ra ON aa.AddressOfRecordID = ra.AddressOfRecordID ";
                fixAgents(conn, sql, "addressofrecord", "AgentID", divToAgentHash);
                
                //---------- agentgeography
                sql = "SELECT ag.AgentGeographyID, ag.AgentID, dp.DivisionID " +
                        "FROM agentgeography ag INNER JOIN geography g ON ag.GeographyID = g.GeographyID " +
                        "INNER JOIN discipline dp ON g.GeographyTreeDefID = dp.GeographyTreeDefID ";
                fixAgents(conn, sql, "agentgeography", "AgentID", divToAgentHash);

                //---------- agentspecialty
                // (Don't Need To)
                
                //---------- agentvariant
                // (Don't Need To)
                
                //---------- appraisal
                sql = "SELECT ap.AppraisalID, ap.AgentID, a.DivisionID " +
                "FROM appraisal ap INNER JOIN accession a ON ap.AccessionID = a.AccessionID ";
                fixAgents(conn, sql, "appraisal", "AgentID", divToAgentHash);
                
                sql = "SELECT ap.AppraisalID, ap.AgentID, dp.DivisionID " +
                        "FROM appraisal ap INNER JOIN collectionobject co ON ap.AppraisalID = co.AppraisalID " +
                        "INNER JOIN collection c ON co.CollectionID = c.UserGroupScopeId " +
                        "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId ";
                fixAgents(conn, sql, "appraisal", "AgentID", divToAgentHash);
                
                //---------- author
                // (Skipping for now - no way to know)
                
                //---------- borrowagent
                sql = "SELECT ba.BorrowAgentID, ba.AgentID, dp.DivisionID FROM borrowagent ba " +
                      "INNER JOIN collection c ON ba.CollectionMemberID = c.UserGroupScopeId " +
                      "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId ";
                fixAgents(conn, sql, "borrowagent", "AgentID", divToAgentHash);

                //---------- borrowreturnmaterial
                sql = "SELECT brm.BorrowReturnMaterialID, brm.ReturnedByID, dp.DivisionID " +
                "FROM borrowreturnmaterial brm INNER JOIN borrowmaterial bm ON brm.BorrowMaterialID = bm.BorrowMaterialID " +
                "INNER JOIN collection c ON bm.CollectionMemberID = c.UserGroupScopeId " +
                "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "borrowreturnmaterial", "ReturnedByID", divToAgentHash);

                //---------- collectionobject
                sql = "SELECT co.CollectionObjectID, co.CatalogerID, dp.DivisionID " +
                "FROM collectionobject co INNER JOIN collection c ON co.CollectionID = c.UserGroupScopeId " +
                "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "collectionobject", "CatalogerID", divToAgentHash);

                //---------- collector
                sql = "SELECT CollectorID, AgentID, DivisionID FROM collector";
                fixAgents(conn, sql, "collector", "AgentID", divToAgentHash);

                //---------- conservevent
                // (Skipping for now - no way to know)
                
                //---------- deaccessionagent
                // (Skipping for now - no way to know)
                
                //---------- determination
                sql = "SELECT d.DeterminationID, d.DeterminerID, dp.DivisionID " +
                "FROM determination d INNER JOIN collection c ON d.CollectionMemberID = c.UserGroupScopeId " +
                "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "determination", "DeterminerID", divToAgentHash);
                
                //---------- dnasequence
                sql = "SELECT DnaSequenceID, dna.AgentID, dp.DivisionID " +
                "FROM collection c INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId " +
                "INNER JOIN dnasequence dna ON dna.CollectionMemberID = c.UserGroupScopeId";
                fixAgents(conn, sql, "dnasequence", "AgentID", divToAgentHash);
                
                //---------- exchangein
                sql = "SELECT ExchangeInID, CatalogedByID, DivisionID FROM exchangein";
                fixAgents(conn, sql, "exchangein", "CatalogedByID", divToAgentHash);
                
                //---------- exchangeout
                sql = "SELECT ExchangeOutID, CatalogedByID, DivisionID FROM exchangeout";
                fixAgents(conn, sql, "exchangeout", "CatalogedByID", divToAgentHash);
                
                //---------- fieldnotebook
                sql = "SELECT fn.FieldNotebookID, fn.AgentID, dp.DivisionID " +
                "FROM fieldnotebook fn INNER JOIN discipline dp ON fn.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "fieldnotebook", "AgentID", divToAgentHash);
                
                //---------- fieldnotebookpageset
                sql = "SELECT FieldNotebookPageSetID, fnps.AgentID, dp.DivisionID " +
                "FROM fieldnotebookpageset fnps INNER JOIN discipline dp ON fnps.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "fieldnotebookpageset", "AgentID", divToAgentHash);
                
                //---------- geocoorddetail
                sql = "SELECT gd.GeoCoordDetailID, gd.AgentID, dp.DivisionID " +
                "FROM geocoorddetail gd INNER JOIN locality l ON gd.LocalityID = l.LocalityID " +
                "INNER JOIN discipline dp ON l.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "geocoorddetail", "AgentID", divToAgentHash);
               
                //---------- giftagent
                sql = "SELECT ga.GiftAgentID, ga.AgentID, g.DivisionID, dp.DivisionID " +
                "FROM giftagent ga INNER JOIN gift g ON ga.GiftID = g.GiftID " +
                "INNER JOIN discipline dp ON g.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "giftagent", "AgentID", divToAgentHash);
                
                //---------- groupperson (Don't need to)

                //---------- inforequest
                sql = "SELECT ir.InfoRequestID, ir.AgentID, dp.DivisionID " +
                "FROM inforequest ir INNER JOIN collection c ON ir.CollectionMemberID = c.UserGroupScopeId " +
                "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "inforequest", "AgentID", divToAgentHash);
                
                //---------- loanagent
                sql = "SELECT la.LoanAgentID, la.AgentID, l.DivisionID, dp.DivisionID " +
                "FROM loanagent la INNER JOIN loan l ON la.LoanID = l.LoanID " +
                "INNER JOIN discipline dp ON l.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "loanagent", "AgentID", divToAgentHash);
               
                //---------- loanreturnpreparation
                sql = "SELECT lrp.LoanReturnPreparationID, lrp.ReceivedByID, dp.DivisionID " +
                "FROM loanreturnpreparation lrp INNER JOIN discipline dp ON lrp.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "loanreturnpreparation", "ReceivedByID", divToAgentHash);
                
                //---------- permit (No way to know)

                //---------- preparation
                sql = "SELECT PreparationID, p.PreparedByID, dp.DivisionID " +
                "FROM preparation p INNER JOIN collectionobject co ON p.CollectionObjectID = co.CollectionObjectID " +
                "INNER JOIN collection c ON co.CollectionID = c.UserGroupScopeId " +
                "INNER JOIN discipline dp ON c.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "preparation", "PreparedByID", divToAgentHash);
                
                //---------- project (No way to know)

                //---------- repositoryagreement
                sql = "SELECT RepositoryAgreementID, AgentID, DivisionID FROM repositoryagreement";
                fixAgents(conn, sql, "repositoryagreement", "AgentID", divToAgentHash);

                //---------- shipment
                
                sql = "SELECT s.ShipmentID, s.ShipperID, dp.DivisionID FROM shipment s INNER JOIN discipline dp ON s.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "shipment", "ShipperID", divToAgentHash);
                
                sql = "SELECT s.ShipmentID, s.ShippedToID, dp.DivisionID FROM shipment s INNER JOIN discipline dp ON s.DisciplineID = dp.UserGroupScopeId";
                fixAgents(conn, sql, "shipment", "ShippedToID", divToAgentHash);
                
                sql = "SELECT s.ShipmentID, s.ShippedByID, dp.DivisionID FROM shipment s INNER JOIN discipline dp ON s.DisciplineID = dp.UserGroupScopeId";
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
            String sqlStr       = String.format("UPDATE %s SET %s=? WHERE %s = ?", tableName, fldName, recIdColName);
            System.out.println(sqlStr);
            pStmt = conn.prepareStatement(sqlStr);
            
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
            log.debug(String.format("Updating %s %s.%s - %d -> %d rv= %d", databaseName, tblName, fldName, origLen, newLen, rv));
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
    private boolean doFixesForDBSchemaVersions(final Connection conn, final String databaseName) throws Exception
    {
        /////////////////////////////
        // PaleoContext
        /////////////////////////////
        setTableTitleForFrame(PaleoContext.getClassTableId());
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
        
        DBConnection dbc = DBConnection.getInstance();

        /////////////////////////////
        // FieldNotebookPage
        /////////////////////////////
        setTableTitleForFrame(FieldNotebookPage.getClassTableId());
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
        
        setTableTitleForFrame(LocalityDetail.getClassTableId());
        
        boolean statusOK = true;
        String sql = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'localitydetail' AND COLUMN_NAME = 'UtmScale' AND DATA_TYPE = 'varchar'", dbc.getDatabaseName());
        int count = BasicSQLUtils.getCountAsInt(sql);
        if (count > 0)
        {
            Vector<Object[]> values = BasicSQLUtils.query("SELECT ld.LocalityDetailID, ld.UtmScale, l.LocalityName " +
            	                                          "FROM localitydetail ld INNER JOIN locality l ON ld.LocalityID = l.LocalityID");
            
            BasicSQLUtils.update(conn, "ALTER TABLE localitydetail DROP COLUMN UtmScale");
            String tblName = "localitydetail";
            addColumn(conn, databaseName, tblName, "UtmScale", "ALTER TABLE %s ADD COLUMN %s FLOAT AFTER UtmOrigLongitude");
            addColumn(conn, databaseName, tblName, "MgrsZone", "ALTER TABLE %s ADD COLUMN %s VARCHAR(4) AFTER UtmScale");

            
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
            Connection connection = dbmsMgr.getConnection();
            try
            {
                // Add New Fields to Determination
                
                setTableTitleForFrame(Determination.getClassTableId());
                String tblName = "determination";
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
                        setTableTitleForFrame(CollectingEventAttribute.getClassTableId());
                        
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
                    
                    setTableTitleForFrame(Collector.getClassTableId());
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
                }
                
                //////////////////////////////////////////////
                // Schema Changes 1.4
                //////////////////////////////////////////////
                
                // Add New Fields to Address
                tblName = setTableTitleForFrame(Agent.getClassTableId());
                addColumn(conn, databaseName, tblName, "DateType",             "TINYINT(4)", "Title");
                addColumn(conn, databaseName, tblName, "DateOfBirthPrecision", "TINYINT(4)", "DateOfBirth");
                addColumn(conn, databaseName, tblName, "DateOfDeathPrecision", "TINYINT(4)", "DateOfDeath");
                
                if (!doesColumnExist(databaseName, "address", "Address3"))
                {
                    frame.setDesc("Updating Address Fields...");
                    String fmtStr = "ALTER TABLE address ADD COLUMN Address%d VARCHAR(64) AFTER Address%d";
                    for (int i=3;i<6;i++)
                    {
                        BasicSQLUtils.update(conn, String.format(fmtStr, i, i-1));
                    }
                }
                
                tblName = setTableTitleForFrame(LocalityDetail.getClassTableId());
                addColumn(conn, databaseName, tblName, "StartDepth",         "Double",      "Drainage");
                addColumn(conn, databaseName, tblName, "StartDepthUnit",     "TINYINT(4)",  "StartDepth");
                addColumn(conn, databaseName, tblName, "StartDepthVerbatim", "VARCHAR(32)", "StartDepthUnit");

                addColumn(conn, databaseName, tblName, "EndDepth",         "Double",      "StartDepthVerbatim");
                addColumn(conn, databaseName, tblName, "EndDepthUnit",     "TINYINT(4)",  "EndDepth");
                addColumn(conn, databaseName, tblName, "EndDepthVerbatim", "VARCHAR(32)", "EndDepthUnit");
                
                tblName = setTableTitleForFrame(Locality.getClassTableId());
                addColumn(conn, databaseName, tblName, "Text1", "VARCHAR(255)", "SrcLatLongUnit");
                addColumn(conn, databaseName, tblName, "Text2", "VARCHAR(255)", "Text1");
                
                tblName = setTableTitleForFrame(CollectionObjectAttribute.getClassTableId());
                addColumn(conn, databaseName, tblName, "Text15",  "VARCHAR(64)", "Text14");
                
                tblName = setTableTitleForFrame(PaleoContext.getClassTableId());
                addColumn(conn, databaseName, tblName, "ChronosStratEndID",  "INT", "ChronosStratID");
                
                tblName = setTableTitleForFrame(Institution.getClassTableId());
                addColumn(conn, databaseName, tblName, "IsSingleGeographyTree",  "BIT(1)", "IsServerBased");
                addColumn(conn, databaseName, tblName, "IsSharingLocalities",    "BIT(1)", "IsSingleGeographyTree");
                BasicSQLUtils.update(conn, "UPDATE institution SET IsSingleGeographyTree=0, IsSharingLocalities=0");
                
                tblName = setTableTitleForFrame(GeologicTimePeriod.getClassTableId());
                addColumn(conn, databaseName, tblName, "Text1", "VARCHAR(64)", "EndUncertainty");
                addColumn(conn, databaseName, tblName, "Text2", "VARCHAR(64)", "Text1");
                
                tblName = setTableTitleForFrame(PreparationAttribute.getClassTableId());
                // Fix Field Length
                String prepAttrFld = "Text22";
                len = getFieldLength(conn, databaseName, tblName, prepAttrFld);
                if (len != null && len == 10)
                {
                    alterFieldLength(conn, databaseName, tblName, prepAttrFld, 10, 50);
                }
                
                frame.getProcessProgress().setIndeterminate(true);
                frame.setDesc("Loading updated schema...");
                
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
     * Adds Agents to Divisions.
     */
    @SuppressWarnings("unused")
    private void fixSpUserAndAgents()
    {
        HashMap<Integer, HashSet<Integer>> spUserToDivHash = new HashMap<Integer, HashSet<Integer>>();
        HashMap<Integer, Integer>          spUserToAgentHash = new HashMap<Integer,Integer>();
        
        String sql = "SELECT su.SpecifyUserID, a.AgentID, a.DivisionID FROM specifyuser AS su Inner Join agent AS a ON su.SpecifyUserID = a.SpecifyUserID ";
        log.debug(sql);
        
        for (Object[] row : BasicSQLUtils.query(sql))
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
        
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            int colDiv   = (Integer)row[0];
            int spUserID = (Integer)row[1];
            int dispID   = (Integer)row[2];
            
            HashSet<Integer> divs = spUserToDivHash.get(spUserID);
            if (divs == null || !divs.contains(colDiv))
            {
                String divName  = BasicSQLUtils.querySingleObj("SELECT Name FROM division WHERE DivisionID = "+colDiv);
                String userName = BasicSQLUtils.querySingleObj("SELECT Name FROM specifyuser WHERE SpecifyUserID = "+spUserID);
                
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
                    dsp.getAgents().add(dupAgent);
                    
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
                    
                    session.rollback();
                    
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
    private String setTableTitleForFrame(final int tableId)
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);
        if (ti != null)
        {
            frame.setDesc(String.format("Updating %s Fields...", ti != null ? ti.getTitle() : "DB"));
            return ti.getName();
        }
        throw new RuntimeException("Couldn't find table in Mgr for Table Id " + tableId);
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
            String fmtSQL = String.format(updateSQL, tableName, colName);
            return BasicSQLUtils.update(conn, fmtSQL) == 1;
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
        String sql = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s' AND COLUMN_NAME = '%s'", dbName, tableName, colName);
        return BasicSQLUtils.getCountAsInt(sql) == 1;
    }
    
    /**
     * @param sessionArg
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
            localFrame.setVisible(false);
            
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
