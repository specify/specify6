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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
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
    
    private static final String APP          = "App";
    private static final String APP_REQ_EXIT = "AppReqExit";

    
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
                    Vector<Object[]> rows = BasicSQLUtils.query(dbConn.getConnection(), "SELECT AppVersion, SchemaVersion, SpVersionID, Version FROM spversion");
                    if (rows.size() == 1)
                    {
                        Object[] row  = (Object[])rows.get(0);
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
                    	    doSchemaUpdate = Integer.parseInt(appVerNumArg.substring(2, 3)) == 0;
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
                            
                            Pair<String, String> usrPwd = getITUsernamePwd();
                            if (usrPwd != null)
                            {
                                DBConnection dbc = DBConnection.getInstance();
                                
                                DBMSUserMgr dbmsMgr = DBMSUserMgr.getInstance();
                                if (dbmsMgr.connectToDBMS(usrPwd.first, usrPwd.second, dbc.getServerName()))
                                {
                                    int permissions = dbmsMgr.getPermissions(usrPwd.first, usrPwd.second);
                                    if (!((permissions & DBMSUserMgr.PERM_ALTER_TABLE) == DBMSUserMgr.PERM_ALTER_TABLE))
                                    {
                                        errMsgList.add("You must have permissions to alter database tables.");
                                        //CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                        return SchemaUpdateType.Error;
                                    }
                                }
        
                                ProgressFrame frame = new ProgressFrame(getResourceString("UPDATE_SCHEMA_TITLE"));
                                frame.adjustProgressFrame();
                                frame.turnOffOverAll();
                                frame.getCloseBtn().setVisible(false);
                                frame.getProcessProgress().setIndeterminate(true);
                                frame.setDesc(UIRegistry.getLocalizedMessage("UPDATE_SCHEMA", dbVersion));
                                
                                UIHelper.centerAndShow(frame);
                                
                                boolean ok = manuallyFixDB(DatabaseDriverInfo.getDriver(dbc.getDriver()), dbc.getServerName(), dbc.getDatabaseName(), usrPwd.first,usrPwd.second);
                                if (!ok)
                                {
                                    frame.setVisible(false);
                                    return SchemaUpdateType.Error;
                                }
                                
                                ok = SpecifySchemaGenerator.updateSchema(DatabaseDriverInfo.getDriver(dbc.getDriver()), dbc.getServerName(), dbc.getDatabaseName(), usrPwd.first, usrPwd.second);
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
        
        DBMSUserMgr dbmsMgr = DBMSUserMgr.getInstance();
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
                    dbmsMgr.setConnection(conn);
                    
                    Integer count = null;
                	stmt = conn.createStatement();
                    int rv = 0;
                    Integer len = dbmsMgr.getFieldLength("localitydetail", "UtmDatum");
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
                    
                    len = dbmsMgr.getFieldLength("specifyuser", "Password");
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
                    
                    len = dbmsMgr.getFieldLength("spexportschemaitem", "FieldName");
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
                    
                    len = dbmsMgr.getFieldLength("agent", "LastName");
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
                    
                    len = dbmsMgr.getFieldLength("spexportschema", "SchemaName");
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
                    
                    len = dbmsMgr.getFieldLength("spexportschema", "SchemaVersion");
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
                    
                    if (!dbmsMgr.doesFieldExistInTable("collectingeventattachment", "HostTaxonID"))
                    {
                        
                    }
                    
                    SpecifySchemaUpdateScopeFixer collectionMemberFixer = new SpecifySchemaUpdateScopeFixer(databaseName);
                    if (!collectionMemberFixer.fix(conn))
                    {
                        errMsgList.add("Error fixing CollectionMember tables");
                        return false;
                    }
                    
                    // Do updates for Schema 1.2
                    doFixesForDBSchema1_2(conn);
                    
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
                    if (dbmsMgr != null)
                    {
                        dbmsMgr.setConnection(null);
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
    
    private boolean alterFieldLength(final Connection conn,
                                     final String tblName, 
                                     final String fldName, 
                                     final int origLen, 
                                     final int newLen)
    {
        Integer len = DBMSUserMgr.getInstance().getFieldLength(tblName, fldName);
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
    private boolean doFixesForDBSchema1_2(final Connection conn) throws Exception
    {
        /////////////////////////////
        // PaleoContext
        /////////////////////////////
        Integer len = DBMSUserMgr.getInstance().getFieldLength("paleocontext", "Text1");
        alterFieldLength(conn, "paleocontext", "Text1", 32, 64);
        alterFieldLength(conn, "paleocontext", "Text2", 32, 64);
        
        len = DBMSUserMgr.getInstance().getFieldLength("paleocontext", "Remarks");
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
        
        /////////////////////////////
        // FieldNotebookPage
        /////////////////////////////
        len = DBMSUserMgr.getInstance().getFieldLength("fieldnotebookpage", "PageNumber");
        if (len != null && len == 16)
        {
            alterFieldLength(conn, "fieldnotebookpage", "PageNumber", 16, 32);
            BasicSQLUtils.update(conn, "ALTER TABLE fieldnotebookpage ALTER COLUMN ScanDate DROP DEFAULT");
        }

        /////////////////////////////
        // Project Table
        /////////////////////////////
        alterFieldLength(conn, "project", "projectname", 50, 128);
        
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
            frame.getProcessProgress().setIndeterminate(false);
            
            frame.setProcess(0, disciplines.size());
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

}
