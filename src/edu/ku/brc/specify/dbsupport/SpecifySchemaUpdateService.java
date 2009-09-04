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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
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
    
    
    /**
     * 
     */
    public SpecifySchemaUpdateService()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SchemaUpdateService#updateSchema(java.lang.String)
     */
    @Override
    public boolean updateSchema(final String appVerNumArg)
    {
        String  dbVersion = null;
        String  appVerNum = appVerNumArg;
        
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
        
        
        DBConnection dbConn = DBConnection.getInstance();
        if (dbConn != null)
        {
            DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
            if (dbMgr.connect(dbConn.getUserName(), dbConn.getPassword(), dbConn.getServerName(), dbConn.getDatabaseName()))
            {
                // Here checks to see if this is the first ever
                boolean doUpdateAppVer = false;
                boolean doSchemaUpdate = false;
                boolean doInsert       = false;
                String  appVersion     = null;
                String  schemaVersion  = null;
                Integer spverId        = null;
                Integer recVerNum     = 1;
                
                log.debug("appVerNumArg: ["+appVerNumArg+"] dbVersion from XML["+dbVersion+"] ");

                if (dbMgr.doesDBHaveTable("spversion"))
                {
                    Vector<Object[]> rows = BasicSQLUtils.query(dbConn.getConnection(), "SELECT AppVersion, SchemaVersion, SpVersionID, Version FROM spversion");
                    if (rows.size() == 1)
                    {
                        Object[] row  = (Object[])rows.get(0);
                        appVersion    = row[0].toString();
                        schemaVersion = row[1].toString();
                        spverId       = (Integer)row[2];
                        recVerNum     = (Integer)row[3];
                        
                        log.debug("appVerNumArg: ["+appVerNumArg+"] dbVersion from XML["+dbVersion+"] appVersion["+appVersion+"] schemaVersion["+schemaVersion+"]  spverId["+spverId+"]  recVerNum["+recVerNum+"] ");

                        
                        if (appVerNum == null) // happens for developers
                        {
                            appVerNum = appVersion;
                        }
                        
                        if (appVersion == null && schemaVersion == null)
                        {
                            doUpdateAppVer = true;
                            
                        } else if (!appVersion.equals(appVerNum))
                        {
                            if (checkVersion(appVersion, appVerNum, "SpecifySchemaUpdateService.APP_VER_ERR", "SpecifySchemaUpdateService.APP_VER_NEQ"))
                            {
                                doUpdateAppVer = true;
                            } else
                            {
                                CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit", null));
                                return false;
                            }
                        } 
                        
                        if (dbVersion != null && !schemaVersion.equals(dbVersion))
                        {
                            if (checkVersion(schemaVersion, dbVersion, "SpecifySchemaUpdateService.DB_VER_ERR", "SpecifySchemaUpdateService.DB_VER_NEQ"))
                            {
                                doSchemaUpdate = true;
                            } else
                            {
                                CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit", null));
                                return false;
                            }
                        }
                        
                    }
                } else
                {
                    doInsert = true;
                }
                
                SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Timestamp        now               = new Timestamp(System.currentTimeMillis());

                try
                {
                    if (doSchemaUpdate || doInsert || doUpdateAppVer)
                    {
                        if (doSchemaUpdate || doInsert)
                        {
                            if (!askToUpdateSchema())
                            {
                                CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit", null));
                                return false;
                            }
                            
                            Pair<String, String> usrPwd = getITUsernamePwd();
                            if (usrPwd != null)
                            {
                                DBConnection dbc = DBConnection.getInstance();
        
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
                                    return false;
                                }
                                
                                ok = SpecifySchemaGenerator.updateSchema(DatabaseDriverInfo.getDriver(dbc.getDriver()), dbc.getServerName(), dbc.getDatabaseName(), usrPwd.first, usrPwd.second);
                                if (!ok)
                                {
                                    errMsgList.add("There was an error updating the schema.");
                                    frame.setVisible(false);
                                    return false;
                                }
                                frame.setVisible(false);
                                
                            } else
                            {
                                CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit", null));
                                return false;
                            }
                        }
                        
                        if (doInsert || (appVersion == null && schemaVersion == null))
                        {
                            String sql = "INSERT INTO spversion (AppName, AppVersion, SchemaVersion, TimestampCreated, TimestampModified, Version) VALUES('Specify', '"+appVerNum+"', '"+dbVersion+"', '" + 
                                                                 dateTimeFormatter.format(now) + "', '" + dateTimeFormatter.format(now) + "', "+recVerNum+")";
                            //System.err.println(sql);
                            BasicSQLUtils.update(dbConn.getConnection(), sql);
                            
                        } else if (doSchemaUpdate || doUpdateAppVer)
                        {
                            recVerNum++;
                            String sql = "UPDATE spversion SET AppVersion='"+appVerNum+"', SchemaVersion='"+dbVersion+"', Version="+recVerNum+" WHERE SpVersionID = "+ spverId;
                            //System.err.println(sql);
                            BasicSQLUtils.update(dbConn.getConnection(), sql);
                        }
                        return true;
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
        return false;
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
                    stmt = conn.createStatement();
                    int rv = BasicSQLUtils.update(conn, "ALTER TABLE localitydetail CHANGE getUtmDatum UtmDatum varchar(32)");
                    if (rv != 0)
                    {
                        errMsgList.add("Unable to alter table: localitydetail");
                        return false;
                    }
                    Integer count = BasicSQLUtils.getCount("SELECT COUNT(*) FROM specifyuser");
                    rv = BasicSQLUtils.update(conn, "ALTER TABLE specifyuser MODIFY Password varchar(255)");
                    if (rv != count)
                    {
                        errMsgList.add("Update count didn't match for update to table: specifyuser");
                        return false;
                    }
                    
                    // Find Accession NumberingSchemes that 'attached' to Collections
                    String postfix = " FROM autonumsch_coll ac Inner Join autonumberingscheme ans ON ac.AutoNumberingSchemeID = ans.AutoNumberingSchemeID WHERE ans.TableNumber =  '7'";
                    count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*)" + postfix);
                    if (count > 0)
                    {
                        String ansSQL = "SELECT ac.CollectionID, ac.AutoNumberingSchemeID " + postfix;
                        for (Object[] row : BasicSQLUtils.query(ansSQL))
                        {
                            String sql = "DELETE FROM autonumsch_coll WHERE CollectionID = " + ((Integer)row[0]) + " AND AutoNumberingSchemeID = " + ((Integer)row[1]);
                            rv = BasicSQLUtils.update(sql);
                        }
                        if (rv != count)
                        {
                            errMsgList.add("There was an error fixing the table: autonumsch_coll");
                        }
                        
                    } else
                    {
                        rv = count;
                    }
                    
                    return rv == count;
                    
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
     * @param prevVersionArg the existing version number
     * @param newVersionArg the new version number
     * @param notNumericErrKey I18N localization key
     * @param newVerBadErrKey  I18N localization key
     * @return true if OK
     */
    protected boolean checkVersion(final String prevVersionArg, 
                                   final String newVersionArg,
                                   final String notNumericErrKey,
                                   final String newVerBadErrKey)
    {
        try
        {
            log.debug("App - Prev["+prevVersionArg+"] New["+newVersionArg+"]");
            
            Integer prevVersion = Integer.parseInt(StringUtils.replace(StringUtils.deleteWhitespace(prevVersionArg), ".", ""));
            Integer newVersion  = Integer.parseInt(StringUtils.replace(StringUtils.deleteWhitespace(newVerBadErrKey), ".", ""));
            log.debug("App - Prev["+prevVersion+"] New["+newVersion+"]");
            
            if (prevVersion > newVersion)
            {
                UIRegistry.showLocalizedError(newVerBadErrKey, newVersionArg, prevVersionArg);
                return false;
            } 
            return true;
            
        } catch (NumberFormatException ex)
        {
            UIRegistry.showLocalizedError(notNumericErrKey);
        }
        
        return false;
    }

}
