/* Copyright (C) 2022, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.GenericGUIDGeneratorFactory;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.dbsupport.*;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyGUIDGeneratorFactory;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.IdMapperMgr;
import edu.ku.brc.specify.conversion.IdTableMapper;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.prefs.S2nPrefsPanel;
import edu.ku.brc.specify.tasks.subpane.security.NavigationTreeMgr;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.specify.tools.export.ExportToMySQLDB;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.*;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.List;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.*;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

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
    
    private final int OVERALL_TOTAL = 86; //the number of incOverall() calls (+1 or +2)

    private static final String TINYINT4 = "TINYINT(4)";
    private static final String INT11    = "INT(11)";
    
    
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
    public SchemaUpdateType updateSchema(final String appVerNumber, final String username)
    {
        String  dbVersion      = getDBSchemaVersionFromXML();
        String  appVerNum      = appVerNumber;
        boolean internalVerNum = isInternalVerNum(appVerNum);
        
        boolean useSilentSuccess = false;
        
        DBConnection dbConn = DBConnection.getInstance();
        if (dbConn != null)
        {
            SpVersion.fixSchemaNumber(dbConn);
            
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
                
                //log.debug("appVerNumArg:  ["+appVerNumber+"] dbVersion from XML["+dbVersion+"] "+ dbVersion.compareTo("1.6"));

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
                        
                        log.debug("appVerNumArg: ["+appVerNumber+"] dbVersion from XML["+dbVersion+"] appVersion["+appVerFromDB+"] schemaVersion["+schemaVerFromDB+"]  spverId["+spverId+"]  recVerNum["+recVerNum+"] ");
                        
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
                                return SchemaUpdateType.Error;
                            }
                        }
                        
                        if (StringUtils.isNotEmpty(schemaVerFromDB))
                        {
                            boolean doOverrideUpdateSchema = AppPreferences.getLocalPrefs().getBoolean("UPDATE_SCHEMA", false);
                            if (doOverrideUpdateSchema)
                            {
                                AppPreferences.getLocalPrefs().putBoolean("UPDATE_SCHEMA", false);
                                double currVersion = Double.parseDouble(schemaVerFromDB.trim());
                                if (currVersion > 1.6)
                                {
                                    dbVersion = String.format("%2.1f", currVersion);
                                    currVersion -= 0.1;
                                    schemaVerFromDB = String.format("%2.1f", currVersion);
                                }
                            }
                        }
                        
                        if (dbVersion != null && schemaVerFromDB != null)
                        {
                            Boolean isDBClosed      = false;
                            String  dbClosedBy      = null;

                            //log.debug("schemaVerFromDB["+schemaVerFromDB+"]  compareTo["+schemaVerFromDB.compareTo("1.6")+"] ");
                            if (schemaVerFromDB.compareTo("1.6") > -1)
                            {
                                rows = query(dbConn.getConnection(), "SELECT IsDBClosed, DbClosedBy FROM spversion ORDER BY TimestampCreated DESC");
                                if (rows.size() > 0)
                                {
                                    row = (Object[])rows.get(rows.size()-1);
                                    isDBClosed = (Boolean)row[0];
                                    dbClosedBy = (String)row[1];
                                    //log.debug("isDBClosed["+isDBClosed+"]  dbClosedBy["+dbClosedBy+"] ");
                                }
                            }
                            
                            if (isDBClosed != null && isDBClosed)
                            {
                                if (dbClosedBy != null && !dbClosedBy.equals(username))
                                {
                                    UIRegistry.showLocalizedError("SYSSTP_CLSD_MSG", dbClosedBy);
                                    return SchemaUpdateType.Error;
                                }
                            }

                            if (!schemaVerFromDB.equals(dbVersion))
                            {
                                String errKey = mkKey("DB_VER_NEQ");
                                if (checkVersion(schemaVerFromDB, dbVersion, 
                                                 mkKey("DB_VER_ERR"), errKey, errKey, false))
                                {
                                    doSchemaUpdate = true;
                                } else
                                {
                                    //CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                    return SchemaUpdateType.Error;
                                }
                            }
                        }
                    } else
                    {
                        //If somebody somehow got a hold of an 'internal' version (via a conversion, or possibly by manually checking for updates.
                        doUpdateAppVer = true;
                        if (appVerNumber != null && appVerNumber.length() > 2)
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
                        if (!appCanUpdateSchema) {
                        	UIRegistry.showLocalizedError("SpecifySchemaUpdateService.AppLacksUpdatePermission");
                        	return SchemaUpdateType.Error;
                        }
                        
                    	fixDuplicatedPaleoContexts(dbConn.getConnection());
                        
                        fixLatLonMethodGEOLocate(dbConn.getConnection());

                        if (doSchemaUpdate || doInsert)
                        {
                            //SpecifySchemaUpdateService.attachUnhandledException();
                            //BasicSQLUtils.setSkipTrackExceptions(true);
                            
                            if (!askToUpdateSchema())
                            {
                                //CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                return SchemaUpdateType.Error;
                            }
                            
                            /* Warning Dlg about GUID overwrite. See Bug #9356
                            if (Double.parseDouble(schemaVerFromDB) < 1.8)
                            {
                            	String msg = UIRegistry.getResourceString("SpecifySchemaUpdateService.UPDATE_SCH_GUID_OVERWRITE");
                                boolean opt = UIRegistry.displayConfirm(UIRegistry.getResourceString("SpecifySchemaUpdateService.UPDATE_SCH_GUID_TITLE"),
                                		msg,
                                		UIRegistry.getResourceString("CONTINUE"),
                                		UIRegistry.getResourceString("CANCEL"),
                                		JOptionPane.WARNING_MESSAGE);
                                if (!opt)
                                {
                                    CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                    return SchemaUpdateType.Error;  
                                }
                            }*/
                            
                            String msg = UIRegistry.getResourceString("UPDATE_SCH_BACKUP");
                            int opt = UIRegistry.askYesNoLocalized("EXIT", "CONTINUE", msg, "MySQLBackupService.BACKUP_NOW");
                            if (opt == JOptionPane.YES_OPTION)
                            {
                                //CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
                                return SchemaUpdateType.Error;  
                            }
                            
                            itUserNamePassword = DatabaseLoginPanel.getITUsernamePwd();
                            if (itUserNamePassword != null)
                            {
                                DBConnection dbc = DBConnection.getInstance();
                                
                                DBMSUserMgr dbmsMgr = DBMSUserMgr.getInstance();
                                if (dbmsMgr.connectToDBMS(itUserNamePassword.first, itUserNamePassword.second, dbc.getServerName()))
                                {
                                	if (!dbmsMgr.checkPermissionsForUpdate(itUserNamePassword.first, dbConn.getDatabaseName()))
                                	{
                                        dbmsMgr.close();
                                        
                                        //errMsgList.add("You must have permissions to alter database tables.");
                                        errMsgList.add(dbmsMgr.getErrorMsg());
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
                                
                                //frame.setOverall(0, OVERALL_TOTAL);
                                
                                UIHelper.centerAndShow(frame);
                                
                                //BasicSQLUtils.setSkipTrackExceptions(true); // Needs to be reset
                                
                                boolean ok = manuallyFixDB(DatabaseDriverInfo.getDriver(dbc.getDriver()), dbc.getServerName(), dbc.getDatabaseName(), itUserNamePassword.first,itUserNamePassword.second);
                                if (!ok)
                                {
                                    frame.setVisible(false);
                                    return SchemaUpdateType.Error;
                                }
                                
                                frame.setProcess(0, 100);
                                frame.setDesc("Updating Schema...");
                                ok = SpecifySchemaGenerator.updateSchema(DatabaseDriverInfo.getDriver(dbc.getDriver()), dbc.getServerName(), dbc.getDatabaseName(), itUserNamePassword.first, itUserNamePassword.second);
                                if (ok) {
                                	ok &= finishSchemaUpdate(DatabaseDriverInfo.getDriver(dbc.getDriver()), dbc.getServerName(), dbc.getDatabaseName(), itUserNamePassword.first,itUserNamePassword.second);
                                }
                                if (!ok)
                                {
                                    errMsgList.add("There was an error updating the schema.");
                                    frame.setVisible(false);
                                    return SchemaUpdateType.Error;
                                }
                                frame.setVisible(false);
                                
                                fixSchemaMappingScope(dbConn.getConnection(), dbConn.getDatabaseName());
                                fixLocaleSchema();
                                
                                // Unhide All GUID fields for Schema 1.8
                                String updateSQL = "UPDATE splocalecontaineritem SET IsHidden=FALSE WHERE LOWER(Name) = 'guid'";
                                BasicSQLUtils.update(dbConn.getConnection(), updateSQL);
                                //set picklists for disposals/deaccs.
                                updateSQL = "update splocalecontaineritem i inner join splocalecontainer c on c.splocalecontainerid = i.splocalecontainerid " +
                                        " set i.picklistname='DisposalAgentRole' where i.name = 'role' and c.name='disposalagent'"; //but what about multiple disciplines? picklist name competition??
                                BasicSQLUtils.update(dbConn.getConnection(), updateSQL);
                                updateSQL = "update splocalecontaineritem i inner join splocalecontainer c on c.splocalecontainerid = i.splocalecontainerid " +
                                        " set i.picklistname='DeaccessionStatus' where i.name = 'status' and c.name='deaccession'"; //but what about multiple disciplines? picklist name competition??
                                BasicSQLUtils.update(dbConn.getConnection(), updateSQL);
                                updateSQL = "update splocalecontaineritem i inner join splocalecontainer c on c.splocalecontainerid = i.splocalecontainerid " +
                                        " set i.picklistname='DeaccessionType' where i.name = 'type' and c.name='deaccession'"; //but what about multiple disciplines? picklist name competition??
                                BasicSQLUtils.update(dbConn.getConnection(), updateSQL);
                                updateSQL = "update splocalecontaineritem i inner join splocalecontainer c on c.splocalecontainerid = i.splocalecontainerid " +
                                        " set i.picklistname='DisposalType' where i.name = 'type' and c.name='disposal'"; //but what about multiple disciplines? picklist name competition??
                                BasicSQLUtils.update(dbConn.getConnection(), updateSQL);
                                updateSQL = "update splocalecontaineritem i inner join splocalecontainer c on c.splocalecontainerid = i.splocalecontainerid " +
                                        " set i.picklistname='DeaccessionAgentRole' where i.name = 'role' and c.name='deaccessionagent'"; //but what about multiple disciplines? picklist name competition??
                                BasicSQLUtils.update(dbConn.getConnection(), updateSQL);
                            } else
                            {
                                //CommandDispatcher.dispatch(new CommandAction(APP, APP_REQ_EXIT, null));
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
                            fixDuplicatedPaleoContexts(dbConn.getConnection());
                            
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
    
    /* paleo model change finalization ... */
    
    private boolean moveDataFromHereToHere(final String move, final String databaseName, final Connection conn) 
    {
    	String from = move.split("\\|")[0];
    	String to = move.split("\\|")[1];
    	//System.out.println("moving from " + from + " to " + to);
    	log.info("moving from " + from + " to " + to);
    	String fromTable = from.split("\\.")[0];
    	String fromField = from.split("\\.")[1];
    	String toTable = to.split("\\.")[0];
    	String toField = to.split("\\.")[1];
   	    //assuming fromTable is paleoContext
    	//also assuming toTable is collectionObjectAttribute
    	if (doesColumnExist(databaseName, fromTable, fromField, conn)) {
    		int recsToMove = BasicSQLUtils.getCountAsInt("SELECT count(*) FROM `" + fromTable + "` WHERE `" + fromField + "` IS NOT NULL");
    		if (recsToMove > 0) {
    			String sql = "SELECT count(*) FROM paleocontext pc INNER JOIN collectionobject co ON co.PaleoContextID=pc.PaleoContextID INNER JOIN collectionobjectattribute coa ON "
    					+ "coa.CollectionObjectAttributeID=co.CollectionObjectAttributeID WHERE pc.`" + fromField + "` IS NOT NULL";
    			int recsToMoveTo = BasicSQLUtils.getCountAsInt(sql);
    			if (recsToMoveTo < recsToMove) {
    				sql = "INSERT INTO collectionobjectattribute(TimestampCreated, TimestampModified, Version, CollectionMemberID,`" + toField + "`) "
    						+ "SELECT pc.TimestampCreated, pc.TimestampModified, 0, co.CollectionMemberID, co.CollectionObjectID FROM paleocontext pc INNER JOIN collectionobject co ON co.PaleoContextID=pc.PaleoContextID "
    						+ "WHERE pc.`" + fromField + "` IS NOT NULL AND co.CollectionObjectAttributeID IS NULL";
    				BasicSQLUtils.update(sql);
    				sql = "UPDATE collectionobject co INNER JOIN collectionobjectattribute coa ON coa.`" + toField + "`=co.CollectionObjectID SET co.CollectionObjectAttributeID=coa.CollectionObjectAttributeID";
    				BasicSQLUtils.update(sql);
    			}
    	
    			sql = "UPDATE paleocontext pc INNER JOIN collectionobject co ON co.PaleoContextID=pc.PaleoContextID INNER JOIN collectionobjectattribute coa ON "
    					+ "coa.CollectionObjectAttributeID=co.CollectionObjectAttributeID SET coa.`" + toField + "`=pc.`" + fromField + "` WHERE pc.`" + fromField + "` IS NOT NULL";
    			int recsUpdated = BasicSQLUtils.update(sql);
    			if (recsUpdated != recsToMove) {
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    /**
     * @param toDrop
     * @param databaseName
     * @param conn
     * @return
     */
    private boolean removeField(String toDrop, String databaseName, Connection conn) {
    	//System.out.println("removing " + toDrop);
    	log.info("removing " + toDrop);
    	String tbl = toDrop.split("\\.")[0];
    	String fld = toDrop.split("\\.")[1];
    	//String sql = "SELECT COUNT(*) FROM `" + tbl+ "`";
    	if (doesColumnExist(databaseName, tbl, fld, conn)) {
        	//int cnt = BasicSQLUtils.getCountAsInt(sql);
        	String sql = "ALTER TABLE `" + databaseName + "`.`" + tbl + "` DROP `" + fld + "`";
        	BasicSQLUtils.update(conn, sql);        		
        	if (doesColumnExist(databaseName, tbl, fld, conn)) {
        		return false;
        	}
    	}
    	return true;
    }
    
    /**
     * @param itConn
     * @param databaseName
     * @return
     */
    public boolean fixPaleoModelAftermath(Connection itConn, String databaseName) {
    	//update collection.PaleoContextChildTable and IsPaleoContextEmbedded.
    	//It is safe to do this for all collections, though it is only applicable to paleo collections.
    	BasicSQLUtils.update("UPDATE discipline SET PaleoContextChildTable='collectionobject', IsPaleoContextEmbedded=true");    		
    	
    	//move data to new fields
    	String[] moves = {
    			"paleocontext.positionState|collectionobjectattribute.positionState",
    			"paleocontext.topDistance|collectionobjectattribute.topDistance",
    			"paleocontext.bottomDistance|collectionobjectattribute.bottomDistance",
    			"paleocontext.distanceUnits|collectionobjectattribute.distanceUnits",
    			"paleocontext.direction|collectionobjectattribute.direction"
    	};
    	for (int i = 0; i < moves.length; i++) {
    		if (!moveDataFromHereToHere(moves[i], databaseName, itConn)) {
    			return false;
    		}
    	}
    	//delete old fields
    	for (int i = 0; i < moves.length; i++) {
    		if (!removeField(moves[i].split("\\|")[0], databaseName, itConn)) {
    			return false;
    		}
    	}

    	if (!fixSchemaAfterPaleoModelUpdate()) {
    		return false;
    	}
    	
    	if (!fixTypeSearchDefResourcesAfterPaleoModelUpdate()) {
    		return false;
    	}
    	
    	if (!rescopePaleoContext(databaseName, itConn)) {
    		return false;
    	}
    	
    	return true;
    }

    /**
     * @return
     */
    public boolean fixSchemaAfterPaleoModelUpdate() {
    	//remove old fields from schema
    	String sql = "SELECT COUNT(*) FROM splocaleitemstr WHERE splocalecontaineritemdescid IN(SELECT splocalecontaineritemid FROM splocalecontaineritem WHERE splocalecontainerid IN"
    			+ "(SELECT splocalecontainerid FROM splocalecontainer WHERE name='paleocontext') AND name IN('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance'))";
    	int cnt = BasicSQLUtils.getCountAsInt(sql);
    	sql = "DELETE FROM splocaleitemstr WHERE splocalecontaineritemdescid IN(SELECT splocalecontaineritemid FROM splocalecontaineritem WHERE splocalecontainerid IN"
    			+ "(SELECT splocalecontainerid FROM splocalecontainer WHERE name='paleocontext') AND name IN('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance'))";
    	if (BasicSQLUtils.update(sql) != cnt) {
    		return false;
    	}
    	
    	sql = "SELECT COUNT(*) from splocaleitemstr where splocalecontaineritemnameid in (select splocalecontaineritemid from splocalecontaineritem where splocalecontainerid in"
    			+ "(select splocalecontainerid from splocalecontainer where name='paleocontext') and name in('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance'))";
    	cnt = BasicSQLUtils.getCountAsInt(sql);
    	sql = "delete from splocaleitemstr where splocalecontaineritemnameid in (select splocalecontaineritemid from splocalecontaineritem where splocalecontainerid in"
    			+ "(select splocalecontainerid from splocalecontainer where name='paleocontext') and name in('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance'))";
    	if (BasicSQLUtils.update(sql) != cnt) {
    		return false;
    	}
    	
    	sql = "SELECT COUNT(*) from splocalecontaineritem where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='paleocontext') and name in('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance')";
    	cnt = BasicSQLUtils.getCountAsInt(sql);
    	sql = "delete from splocalecontaineritem where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='paleocontext') and name in('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance')";
    	if (BasicSQLUtils.update(sql) != cnt) {
    		return false;
    	}

    	//hide PaleoContext.Collectingevents
		BasicSQLUtils.update("update splocalecontaineritem set ishidden=true where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='paleocontext') and name='collectingevents'");
		
		//show PaleoContext.CollectionObjects
		BasicSQLUtils.update("update splocalecontaineritem set ishidden=false where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='paleocontext') and name='collectionobjects'");	

		//hide collectingevent.PaleoContextID
		BasicSQLUtils.update("update splocalecontaineritem set ishidden=true where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='collectingevent') and name='paleocontext'");

		//show collectionobject.PaleoContextID ...just in case
		BasicSQLUtils.update("update splocalecontaineritem set ishidden=false where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='collectionobject') and name='paleocontext'");

		return true;
    }

    /**
     * @param databaseName
     * @return
     */
    protected boolean rescopePaleoContext(String databaseName, Connection itConn) {
    	boolean result = true;
    	if (doesColumnExist(databaseName, "paleocontext", "CollectionMemberID", itConn)) {
    		int cnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM paleocontext");
    		if (cnt > 0) {
        		String sql = "UPDATE paleocontext pc INNER JOIN collection cn ON cn.CollectionID = pc.CollectionMemberID "
        				+ "SET pc.DisciplineID=cn.DisciplineID";
    			result = cnt == BasicSQLUtils.update(itConn, sql);
    		}
    		if (result) {
    			result = removeField("paleocontext.CollectionMemberID", databaseName, itConn);
    		}
    	}
    	return result;
    }
    
    /**
     * @return
     */
    private boolean fixTypeSearchDefResourcesAfterPaleoModelUpdate() {
    	String sql = "SELECT SpAppResourceID FROM spappresource where `Name`='TypeSearches'";
    	List<Object> resources = BasicSQLUtils.querySingleCol(sql);
    	if (resources != null && resources.size() > 0) {
    		String pcSearch = "    <typesearch tableid=\"32\"  name=\"PaleoContext\"           searchfield=\"paleoContextName,cs.fullName,ls.fullName\" displaycols=\"paleoContextName,cs.fullName,cse.fullName,ls.fullName\" format=\"%s, %s, %s, %s\"\n"     
    		    	+ "        dataobjformatter=\"PaleoContext\">\n"
    		    	+ "        SELECT %s1 FROM PaleoContext pc LEFT JOIN pc.chronosStrat cs JOIN cs.definition csd LEFT JOIN pc.chronosStratEnd cse LEFT JOIN cse.definition csed LEFT JOIN pc.lithoStrat ls LEFT JOIN ls.definition lsd WHERE pc.discipline.disciplineId = DSPLNID AND (pc.chronosStrat IS NULL OR csd.geologicTimePeriodTreeDefId=GTPTREEDEFID) AND (pc.chronosStratEnd IS NULL OR csed.geologicTimePeriodTreeDefId=GTPTREEDEFID) AND (pc.lithoStrat IS NULL OR lsd.lithoStratTreeDefId=LITHOTREEDEFID) AND %s2 ORDER BY pc.paleoContextName,cs.fullName,cse.fullName,ls.fullName\n"
    		    	+ "    </typesearch>\n";
    		for (Object resource : resources) {
    			sql = "UPDATE spappresourcedata SET `data`=replace(`data`, '</typesearches>','" + pcSearch + "</typesearches>') WHERE SpAppResourceID=" + resource;
    			if (1 != BasicSQLUtils.update(sql)) {
    				return false;
    			}
    		}
    	}
    	return true;
    }

    /**
     * @return
     */
    private boolean fixTypeSearchDefResourcesAfterDNAModelUpdate() {
    	String sql = "SELECT a.SpAppResourceID FROM spappresource a inner join spappresourcedata ad on ad.spappresourceid = a.spappresourceid where a.`Name`='TypeSearches' and ad.data not like '%<typesearch tableid=\"150\"%'";
    	List<Object> resources = BasicSQLUtils.querySingleCol(sql);
    	if (resources != null && resources.size() > 0) {
    		String dnapSearch = "<typesearch tableid=\"150\" name=\"DNAPrimer\" searchfield=\"primerDesignator\" displaycols=\"primerDesignator\" format=\"%s\" dataobjformatter=\"\"/>";
    		for (Object resource : resources) {
    			sql = "UPDATE spappresourcedata SET `data`=replace(`data`, '</typesearches>','" + dnapSearch + "</typesearches>') WHERE SpAppResourceID=" + resource;
    			if (1 != BasicSQLUtils.update(sql)) {
    				return false;
    			}
    		}
    	}
    	return true;
    }

    /**
     * @return
     */
    private boolean fixTypeSearchDefResourcesAfterDeaccUpdate() {
        String sql = "SELECT a.SpAppResourceID FROM spappresource a inner join spappresourcedata ad on ad.spappresourceid = a.spappresourceid where a.`Name`='TypeSearches' and ad.data not like '%<typesearch tableid=\"163\"%'";
        List<Object> resources = BasicSQLUtils.querySingleCol(sql);
        if (resources != null && resources.size() > 0) {
            String deaccSearch = "<typesearch tableid=\"163\" name=\"Deaccession\" searchfield=\"deaccessionNumber\" displaycols=\"deaccessionNumber\" format=\"%s\" dataobjformatter=\"Deaccession\"/>";
            String disposalSearch = "<typesearch tableid=\"34\" name=\"Disposal\" searchfield=\"disposalNumber\" displaycols=\"disposalNumber\" format=\"%s\" dataobjformatter=\"Disposal\"/>";
            for (Object resource : resources) {
                sql = "UPDATE spappresourcedata SET `data`=replace(`data`, '</typesearches>','" + deaccSearch + "'\n'" + disposalSearch + "</typesearches>') WHERE SpAppResourceID=" + resource;
                if (1 != BasicSQLUtils.update(sql)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean doAddPrepFlavor(Element e) {
        String table = e.attributeValue("table");
        String action = e.attributeValue("action");
        return ("loan".equals(table) && "NEW_LOAN".equals(action)) ||
                ("loan".equals(table) && "Edit".equals(action)) ||
                ("gift".equals(table) && "NEW_GIFT".equals(action)) ||
                ("gift".equals(table) && "Edit".equals(action));
    }
    /**
     * @return
     */
    private boolean fixInteractionsTaskInitResourcesAfterDeaccUpdate() {
        String sql = "select spappresourceid from spappresource where name like 'InteractionsTaskInit'";
        List<Object> resources = BasicSQLUtils.querySingleCol(sql);
        if (resources != null && resources.size() > 0) {
            for (Object resource : resources) {
                //the nukeyuhler option
                sql = "delete from spappresourcedata where SpAppResourceID=" + resource;
                if (1 != BasicSQLUtils.update(sql)) {
                    return false;
                }
                sql = "delete from spappresource where SpAppResourceID=" + resource;
                if (1 != BasicSQLUtils.update(sql)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean fixDnaPrimerFormatterAfterDNAModelUpdate() {
    	String sql = "SELECT a.SpAppResourceID FROM spappresource a inner join spappresourcedata ad on ad.spappresourceid = a.spappresourceid where a.`Name`='DataObjFormatters' and ad.data not like '%name=\"DNAPrimer\"%'";
    	List<Object> resources = BasicSQLUtils.querySingleCol(sql);
    	if (resources != null && resources.size() > 0) {
    		String dnaFmt =     "<format \n"
    		        + "name=\"DNAPrimer\"\n"
    		        + "title=\"DNAPrimer\"\n"
    		        + "class=\"edu.ku.brc.specify.datamodel.DNAPrimer\"\n"
    		        + "default=\"true\"\n"
    		        + ">\n"
    		        + "<switch single=\"true\">\n"
    		        + "<fields>\n"
    		        + "     <field>primerDesignator</field>\n"
    		        + "   </fields>\n"
    		        + "</switch>\n"
    		        + "</format>\n";

    		
    		for (Object resource : resources) {
    			sql = "UPDATE spappresourcedata SET `data`=replace(`data`, '<aggregators>','" + dnaFmt + "<aggregators>') WHERE SpAppResourceID=" + resource;
    			if (1 != BasicSQLUtils.update(sql)) {
    				return false;
    			}
    		}
    	}
    	return true;
    }

    /* end paleo model finalization */
    
    private boolean finishSchemaUpdate(final DatabaseDriverInfo dbdriverInfo, 
            final String             hostname,
            final String             databaseName,
            final String             userName,
            final String             password) {
        String connectionStr = dbdriverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostname, databaseName, true, true,
                userName, password, dbdriverInfo.getName());
        log.debug("generateSchema connectionStr: " + connectionStr);

        log.debug("Creating database connection to: " + connectionStr);
        DBConnection dbConn = null;
        try {
        	dbConn = DBConnection.createInstance(dbdriverInfo.getDriverClassName(), dbdriverInfo.getDialectClassName(), 
        			databaseName, connectionStr, userName, password);
        	boolean result = false;
        	if (dbConn != null && dbConn.getConnection() != null) {
        		Connection conn = dbConn.getConnection();
        		result = true;
        		if (!AppPreferences.getGlobalPrefs().getBoolean("PaleoAftermathCleanup", false) 
        			&& doesColumnExist(databaseName, "paleocontext", "positionState", conn)) {
        			if (fixPaleoModelAftermath(conn, databaseName)) {
        				AppPreferences.getGlobalPrefs().putBoolean("PaleoAftermathCleanup", true);
        			} else {
        				result = false;
        			}
        		}
        		if (!AppPreferences.getGlobalPrefs().getBoolean("InvalidPermissionCleanup", false)) {
        			if (cleanupInvalidPermissions()) {
        				AppPreferences.getGlobalPrefs().putBoolean("InvalidPermissionCleanup", true);
        			} else {
        				result = false;
        			}
        		}
        		//add indexes for new tables
        		if (!AppPreferences.getGlobalPrefs().getBoolean("GGBNAftermathCleanup", false)) {
        			if (cleanupGGBNAftermath(conn)) {
        				AppPreferences.getGlobalPrefs().putBoolean("GGBNAftermathCleanup", true);
        			} else {
        				result = false;
        			}
        		}
        		addGeoCleanupTables();
        		//add collectingeventauthorization if necessary
                if (!doesTableExist(databaseName, "collectingeventauthorization")) {
                    addCollectingEventAuth(conn);
                    result = doesTableExist(databaseName, "collectingeventauthorization");
                }
                //fix collations
                Vector<Object[]> tbls = BasicSQLUtils.query("select table_name, table_collation from information_schema.tables where table_schema = '"
                        + databaseName + "' and table_collation != 'utf8_general_ci'");
                for (Object[] tbl : tbls) {
                    String tblName = (String)tbl[0];
                    if (!tblName.startsWith("ios_")) {
                        String sql = "ALTER TABLE `" + tblName + "` CONVERT TO CHARACTER SET utf8";
                        if (0 > BasicSQLUtils.update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            log.error("update error: " + sql);
                            result = false;
                        }
                    }
                }
                /**
                 * @return
                 */
                if (!fixTypeSearchDefResourcesAfterDeaccUpdate()) {
                    result = false;
                }

                if (!AppPreferences.getGlobalPrefs().getBoolean("InteractionsTaskAfterDeaccFix", false)) {
                    if (fixInteractionsTaskInitResourcesAfterDeaccUpdate()) {
                        AppPreferences.getGlobalPrefs().putBoolean("InteractionsTaskAfterDeaccFix", true);
                    } else {
                        result = false;
                    }
                }
                if (!AppPreferences.getGlobalPrefs().getBoolean("UniquenessConstraintsFix", false)) {
                    if (fixUniquenessConstraints(conn)) {
                        AppPreferences.getGlobalPrefs().putBoolean("UniquenessConstraintsFix", true);
                    } else {
                        result = false;
                    }
                }
            }
        	return result;
        } finally {
            if (dbConn != null) dbConn.close();
        }
    }

    private boolean fixUniquenessConstraints(Connection conn) {
        String sql;
        if (!doesIndexExist("preparation", "PrepBarCodeIdx")) {
            sql = "alter table preparation add constraint unique collPrepUniqueId(CollectionMemberID, barcode)";
            if (-1 == BasicSQLUtils.update(conn, sql)) {
                return false;
            }
            sql = "alter table preparation add index PrepBarCodeIdx(barcode)";
            if (-1 == BasicSQLUtils.update(conn, sql)) {
                return false;
            }
        }
        if (!doesIndexExist("collectionobject", "COUniqueIdentifierIDX")) {
            sql = "alter table collectionobject add constraint unique collCoUniqueId(CollectionID, UniqueIdentifier)";
            if (-1 == BasicSQLUtils.update(conn, sql)) {
                return false;
            }
            sql = "alter table collectionobject add index COUniqueIdentifierIDX(UniqueIdentifier)";
            if (-1 == BasicSQLUtils.update(conn, sql)) {
                return false;
            }
        }
        if (!doesIndexExist("collectingevent", "CEUniqueIdentifierIDX")) {
            sql = "alter table collectingevent add constraint unique dispCEUniqueId(DisciplineID, UniqueIdentifier)";
            if (-1 == BasicSQLUtils.update(conn, sql)) {
                return false;
            }
            sql = "alter table collectingevent add index CEUniqueIdentifierIDX(UniqueIdentifier)";
            if (-1 == BasicSQLUtils.update(conn, sql)) {
                return false;
            }
        }
        if (!doesIndexExist("locality", "LocalityUniqueIdentifierIDX")) {
            sql = "alter table locality add constraint unique dispLocUniqueId(DisciplineID, UniqueIdentifier)";
            if (-1 == BasicSQLUtils.update(conn, sql)) {
                return false;
            }
            sql = "alter table locality add index LocalityUniqueIdentifierIDX(UniqueIdentifier)";
            if (-1 == BasicSQLUtils.update(conn, sql)) {
                return false;
            }
        }
        return true;
    }

    private void addCollectingEventAuth(Connection conn) {
        String sql = "CREATE TABLE `collectingeventauthorization` ("
                + "  `CollectingEventAuthorizationID` int(11) NOT NULL AUTO_INCREMENT,"
                + "  `TimestampCreated` datetime NOT NULL,"
                + "  `TimestampModified` datetime DEFAULT NULL,"
                + "  `Version` int(11) DEFAULT NULL,"
                + "  `Remarks` text,"
                + "  `PermitID` int(11) NOT NULL,"
                + "  `CreatedByAgentID` int(11) DEFAULT NULL,"
                + "`CollectingEventID` int(11) DEFAULT NULL,"
                + "`ModifiedByAgentID` int(11) DEFAULT NULL,"
                + "PRIMARY KEY (`CollectingEventAuthorizationID`),"
                + "KEY `FK67DBF8977699B003` (`CreatedByAgentID`),"
                + "KEY `FK67DBF897AD1F31F4` (`PermitID`),"
                + "KEY `FK67DBF897B237E2BC` (`CollectingEventID`),"
                + "KEY `FK67DBF8975327F942` (`ModifiedByAgentID`),"
                + "CONSTRAINT `FK67DBF8975327F942` FOREIGN KEY (`ModifiedByAgentID`) REFERENCES `agent` (`agentid`),"
                + "CONSTRAINT `FK67DBF8977699B003` FOREIGN KEY (`CreatedByAgentID`) REFERENCES `agent` (`agentid`),"
                + "CONSTRAINT `FK67DBF897AD1F31F4` FOREIGN KEY (`PermitID`) REFERENCES `permit` (`permitid`),"
                + "CONSTRAINT `FK67DBF897B237E2BC` FOREIGN KEY (`CollectingEventID`) REFERENCES `collectingevent` (`collectingeventid`) "
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        BasicSQLUtils.update(conn, sql);
    }
    /**
     * @return
     */
    private boolean cleanupGGBNAftermath(Connection conn) {
//  This gets done in fix db after login 
//    	List<Object[]> collsWithDNA = BasicSQLUtils.query("select distinct dna.collectionmemberid, c.isanumber, dsc.disciplineid, dv.divisionid, dv.institutionid from dnasequence dna "
//    			+ "inner join collection c on c.collectionid = dna.collectionmemberid inner join discipline dsc on dsc.disciplineid = c.disciplineid inner join `division` dv on dv.divisionid = dsc.divisionid");
//    	for (Object[] coll : collsWithDNA) {
//    		int codna = BasicSQLUtils.getCountAsInt("SELECT count(*) FROM dnasequence d inner join collectionobject co on co.CollectionObjectID = d.CollectionObjectID WHERE d.CollectionMemberID = " + coll[0]);
//    		int msdna = BasicSQLUtils.getCountAsInt("SELECT count(*) FROM dnasequence d inner join materialsample ms on ms.MaterialSampleID = d.MaterialSampleID WHERE d.CollectionMemberID = " + coll[0]);
//    		try {
//    			Vector<NameValuePair> postparams = StatsTrackerTask.createBasicPostParameters();
//    			for (NameValuePair postparam : postparams) {
//    				if (postparam.getName().equals("ISA_number")) {
//    					postparam.setValue(coll[1].toString());
//    					break;
//    				}
//    			}
//    			edu.ku.brc.specify.tasks.StatsTrackerTask.appendBasicCollStatsStat((Integer)coll[0], null, (Integer)coll[2], (Integer)coll[3], (Integer)coll[4], postparams);
//    			postparams.add(new BasicNameValuePair("num_co_dna", Integer.toString(codna)));
//    			postparams.add(new BasicNameValuePair("num_ms_dna", Integer.toString(msdna)));
//    			StatsTrackerTask.sendStats(StatsTrackerTask.getVersionCheckURL(), postparams, "StatsTrackerTask");
//    		} catch (Exception ex) {
//    			ex.printStackTrace();
//    	        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//    	        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatsTrackerTask.class, ex);
//    	        return false;
//    		}
//    			
//    	}
		if (!doesIndexExist("materialsample", "DesignationIDX")) {
			String sql = "create index DesignationIDX on materialsample(GGBNSampleDesignation)";
			if (-1 == BasicSQLUtils.update(conn, sql)) {
				errMsgList.add("update error: " + sql);
				return false;
			}
		}

		if (!doesIndexExist("dnaprimer", "DesignatorIDX")) {
			String sql = "create index DesignatorIDX on dnaprimer(PrimerDesignator)";
			if (-1 == BasicSQLUtils.update(conn, sql)) {
				errMsgList.add("update error: " + sql);
				return false;
			}
		}

		if (!fixTypeSearchDefResourcesAfterDNAModelUpdate()) {
			return false;
		}
		if (!fixDnaPrimerFormatterAfterDNAModelUpdate()) {
			return false;
		}
    	return true;
    }

    /**
     * @return
     */
    private boolean cleanupInvalidPermissions() {
    	String sql = "SELECT COUNT(*) FROM sppermission WHERE PermissionClass='edu.ku.brc.specify.datamodel.SpPermission'";
    	int cnt = BasicSQLUtils.getCountAsInt(sql);
    	sql = "UPDATE sppermission SET PermissionClass='edu.ku.brc.af.auth.specify.permission.BasicSpPermission' WHERE PermissionClass='edu.ku.brc.specify.datamodel.SpPermission'";
    	if (cnt != BasicSQLUtils.update(sql)) {
    		return false;
    	} 
    	return true;
    }
    
    /**
     * @param e
     */
    private static void processUnhandledException(final Throwable throwable)
    {
        //streamlined expedition
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
    	Object prop = getFieldProp(conn, databaseName, tableName, fieldName, "CHARACTER_MAXIMUM_LENGTH");
        if (prop == null)
        {
            return null; //the field doesn't even exits
        } else 
        {
            return((Number )prop).intValue();
        }
    }
    
    /**
     * @param conn
     * @param databaseName
     * @param tableName
     * @param fieldName
     * @param propToGet
     * @return
     */
    private Object getFieldProp(final Connection conn, final String databaseName, final String tableName, final String fieldName,
    		final String propToGet)
    {
        // XXX portability. This is MySQL -specific.
        List<Object[]> rows = query(conn, "SELECT " + propToGet + " FROM `information_schema`.`COLUMNS` where TABLE_SCHEMA = '" +
                databaseName + "' and TABLE_NAME = '" + tableName + "' and COLUMN_NAME = '" + fieldName + "'");                    
        if (rows.size() == 0)
        {
            return null; //the field doesn't even exits
        } else 
        {
            return rows.get(0)[0];
        }
    }

    /**
     * @param conn
     * @param databaseName
     * @param tableName
     * @param fieldName
     * @return length of field or null if field does not exist.
     */
    private Boolean getFieldNullability(final Connection conn, final String databaseName, final String tableName, final String fieldName)
    {
        // XXX portability. This is MySQL -specific.
        Object prop = getFieldProp(conn, databaseName, tableName, fieldName, "IS_NULLABLE");                   
        if (prop == null)
        {
            return null; //the field doesn't even exits
        } else 
        {
            return "YES".equalsIgnoreCase(prop.toString()) ? true : false;
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
        // XXX portability. This is MySQL -specific.
        Object prop = getFieldProp(conn, databaseName, tableName, fieldName, "COLUMN_TYPE");                   
        if (prop == null)
        {
            return null; //the field doesn't even exits
        } else 
        {
            return prop.toString();
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
        frame.setOverall(0, OVERALL_TOTAL); // 23 + 7 + 
        
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
                    String  tblName = getTableNameAndTitleForFrame(LocalityDetail.getClassTableId());
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
                    tblName = getTableNameAndTitleForFrame(SpecifyUser.getClassTableId());
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
                    tblName = getTableNameAndTitleForFrame(SpExportSchemaItem.getClassTableId());
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
                    //-- SpExportSchema
                    //---------------------------------------------------------------------------
                    tblName = getTableNameAndTitleForFrame(SpExportSchema.getClassTableId());
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
                    tblName = getTableNameAndTitleForFrame(Agent.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, "DateType"))
                    {
                        String[] addrCols = {"DateType", TINYINT4, "Title", 
                                             "DateOfBirthPrecision", TINYINT4, "DateOfBirth", 
                                             "DateOfDeathPrecision", TINYINT4, "DateOfDeath"};
                        if (!checkAndAddColumns(conn, databaseName, tblName, addrCols))
                        {
                            return false;
                        }
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- Address
                    //-----------------------------------------------------------------------------
                    tblName = getTableNameAndTitleForFrame(Address.getClassTableId());
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
                    tblName = getTableNameAndTitleForFrame(LocalityDetail.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, "StartDepth"))
                    {
                        String[] locDetCols = {"StartDepth",         "Double",      "Drainage", 
                                               "StartDepthUnit",     TINYINT4,  "StartDepth",
                                               "StartDepthVerbatim", "VARCHAR(32)", "StartDepthUnit",
                                               "EndDepth",           "Double",      "StartDepthVerbatim", 
                                               "EndDepthUnit",       TINYINT4,  "EndDepth",
                                               "EndDepthVerbatim",   "VARCHAR(32)", "EndDepthUnit"};
                        if (!checkAndAddColumns(conn, databaseName, tblName, locDetCols))
                        {
                            return false;
                        }
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- Locality
                    //-----------------------------------------------------------------------------
                    tblName = getTableNameAndTitleForFrame(Locality.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, "Text1"))
                    {
                        String[] locCols = {"Text1", "VARCHAR(255)", "SrcLatLongUnit", 
                                            "Text2", "VARCHAR(255)", "Text1"};
                        if (!checkAndAddColumns(conn, databaseName, tblName, locCols))
                        {
                            return false;
                        }
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- CollectionObjectAttribute
                    //-----------------------------------------------------------------------------
                    tblName = getTableNameAndTitleForFrame(CollectionObjectAttribute.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, "Text15"))
                    {
                        if (!addColumn(conn, databaseName, tblName, "Text15",  "VARCHAR(64)", "Text14"))
                        {
                            return false;
                        }
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- PaleoContext
                    //-----------------------------------------------------------------------------
                    tblName = getTableNameAndTitleForFrame(PaleoContext.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, "ChronosStratEndID"))
                    {
                        if (!addColumn(conn, databaseName, tblName, "ChronosStratEndID",  "INT", "ChronosStratID"))
                        {
                            return false;
                        }
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- Institution
                    //-----------------------------------------------------------------------------
                    tblName = getTableNameAndTitleForFrame(Institution.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, "IsSingleGeographyTree"))
                    {
                        String[] instCols = {"IsSingleGeographyTree",  "BIT(1)", "IsServerBased", 
                                             "IsSharingLocalities",    "BIT(1)", "IsSingleGeographyTree"};
                        if (checkAndAddColumns(conn, databaseName, tblName, instCols))
                        {
                            update(conn, "UPDATE institution SET IsSingleGeographyTree=0, IsSharingLocalities=0");
                        } else
                        {
                            return false;
                        }
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- GeologicTimePeriod
                    //-----------------------------------------------------------------------------
                    tblName = getTableNameAndTitleForFrame(GeologicTimePeriod.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, "Text1"))
                    {
                        String[] gtpCols = {"Text1", "VARCHAR(64)", "EndUncertainty", 
                                            "Text2", "VARCHAR(64)", "Text1"};
                        if (!checkAndAddColumns(conn, databaseName, tblName, gtpCols))
                        {
                            return false;
                        }
                    }
                    frame.incOverall();

                    //-----------------------------------------------------------------------------
                    //-- PreparationAttribute
                    //-----------------------------------------------------------------------------
                    // Fix Field Length
                    tblName = getTableNameAndTitleForFrame(PreparationAttribute.getClassTableId());
                    String prepAttrFld = "Text22";
                    len = getFieldLength(conn, databaseName, tblName, prepAttrFld);
                    if (len != null && len == 10)
                    {
                        alterFieldLength(conn, databaseName, tblName, prepAttrFld, 10, 50);
                    }
                    frame.incOverall(); 

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //                                                                                                              //
                    // Schema Changes 1.5                                                                                           //
                    //                                                                                                              //
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    //BasicSQLUtils.update(conn, "UPDATE agent SET Title='mr' WHERE AgentType = 1 AND Title is NULL OR Title = ''");
                    

                    //-----------------------------------------------------------------------------
                    //-- GeoCoordDetail
                    //-----------------------------------------------------------------------------
                    // Change column types for MaxUncertaintityEst and NamedPlaceExtent
                    tblName    = getTableNameAndTitleForFrame(GeoCoordDetail.getClassTableId());
                    String columnType = getFieldColumnType(conn, databaseName, tblName, "MaxUncertaintyEst");
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
                    tblName = getTableNameAndTitleForFrame(LoanPreparation.getClassTableId());
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
                    tblName = getTableNameAndTitleForFrame(ConservEvent.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, "Text1"))
                    {
                        String[] consrvEvCols = {"Text1",  "VARCHAR(64)", "Remarks",
                                                 "Text2",  "VARCHAR(64)", "Text1",
                                                 "Number1",  INT11,       "Text2",
                                                 "Number2",  INT11,       "Number1",
                                                 "YesNo1", "BIT(1)",      "Number2",
                                                 "YesNo2", "BIT(1)",      "YesNo1",};
                        if (!checkAndAddColumns(conn, databaseName, tblName, consrvEvCols))
                        {
                            return false;
                        }
                    }
                    frame.incOverall(); 
                    
                    //-----------------------------------------------------------------------------
                    //-- DNASequencingRun
                    //-----------------------------------------------------------------------------
                    String runByAgentID = "RunByAgentID";
                    tblName = getTableNameAndTitleForFrame(DNASequencingRun.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, runByAgentID))
                    {
                        if (addColumn(conn, databaseName, tblName, runByAgentID,  INT11, "DNASequenceID"))
                        {
                            update(conn, "ALTER TABLE dnasequencingrun ADD KEY `FKDNASEQRUNRUNBYAGT` (`RunByAgentID`)");
                            update(conn, "ALTER TABLE dnasequencingrun ADD CONSTRAINT `FKDNASEQRUNRUNBYAGT` FOREIGN KEY (`RunByAgentID`) REFERENCES `agent` (`AgentID`)");
                            
                            if (addColumn(conn, databaseName, tblName, "PreparedByAgentID",  INT11, "RunByAgentID"))
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
                    
                    String pwdMinLen = "MinimumPwdLength";
                    tblName = getTableNameAndTitleForFrame(Institution.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, pwdMinLen))
                    {
                        if (!addColumn(conn, databaseName, tblName, pwdMinLen,  TINYINT4, "LsidAuthority"))
                        {
                            return false;
                        }
                    }
                    
                    frame.incOverall(); 
                    

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //                                                                                                              //
                    // Schema Changes 1.6                                                                                        //
                    //                                                                                                              //
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    
                    createSGRTables(conn, databaseName);
                    frame.incOverall();
                    
                    if (!miscSchema16Updates(conn, databaseName)) // Steps 26 - 28
                    {
                        return false;
                    }

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //                                                                                                              //
                    // Schema Changes 1.7                                                                                           //
                    //                                                                                                              //
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    tblName = getTableNameAndTitleForFrame(SpQuery.getClassTableId());
                    len = getFieldLength(conn, databaseName, tblName, "SqlStr");
                    if (len == 64)
                    {
                        if (!fixSpQuerySQLLength(conn, databaseName))
                        {
                            return false;
                        }
                    }                       
                    
                    frame.incOverall();
                    frame.setProcess(0, 100);
                    //-----------------------------------------------------------------------------
                    //-- Determination fix
                    //-----------------------------------------------------------------------------
                    String varQualNameBad  = "VarQualifer";
                    String varQualNameGood = "VarQualifier";
                    
                    tblName = getTableNameAndTitleForFrame(Determination.getClassTableId());
                    if (doesColumnExist(databaseName, tblName, varQualNameBad) &&
                        !doesColumnExist(databaseName, tblName, varQualNameGood))
                    {
                        if (addColumn(conn, databaseName, tblName, varQualNameGood,  "VARCHAR(16)", "TypeStatusName"))
                        {
                            update(conn, String.format("UPDATE determination SET %s=%s WHERE %s IS NOT NULL", varQualNameGood, varQualNameBad, varQualNameBad));
                            update(conn, String.format("ALTER TABLE determination DROP COLUMN %s", varQualNameBad));
                            
                            update(conn, String.format("UPDATE splocalecontaineritem SET Name='%s' WHERE Name = '%s'", varQualNameGood, varQualNameBad));
                            
                        } else
                        {
                            return false;
                        }
                    }
                    
                    //-----------------------------------------------------------------------------
                    // Fix Bug 8747 - PickList item has wrong value
                    //-----------------------------------------------------------------------------
                    String updateStr = "UPDATE picklistitem SET Value = 'exisotype' WHERE Title = 'Ex Isotype' AND Value = 'isotype'";
                    BasicSQLUtils.update(updateStr);
                    
                    frame.setProcess(0, 100);
                    frame.incOverall(); 
                    
                    //-----------------------------------------------------------------------------
                    // Adds new OCR field to CollectionObject
                    //-----------------------------------------------------------------------------
                    frame.setDesc("Adding OCR field to Collection Object"); // I18N
                    String ocrField = "OCR";
                    tblName = getTableNameAndTitleForFrame(CollectionObject.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, ocrField))
                    {
                        if (!addColumn(conn, databaseName, tblName, ocrField,  "TEXT", "TotalValue"))
                        {
                            return false;
                        }
                    }
                    frame.setProcess(0, 100);
                    frame.incOverall(); 
                    
                    updateDNAAttachments(conn);
                    
                    frame.setProcess(0, 100);
                    frame.incOverall(); 
                    
                    // Fix indexes
                    
                    if (doesIndexExist("borrowagent", "BorColMemIDX"))
                    {
                        update(conn, "DROP INDEX BorColMemIDX on borrowagent");
                        update(conn, "CREATE INDEX BorColMemIDX2 ON borrowagent(CollectionMemberID)");
                    }

                    if (doesIndexExist("exchangeout", "DescriptionOfMaterialIDX"))
                    {
                        update(conn, "DROP INDEX DescriptionOfMaterialIDX on exchangeout");
                        update(conn, "CREATE INDEX DescriptionOfMaterialIDX2 ON exchangeout(DescriptionOfMaterial)");
                    }
                    frame.incOverall(); 

                    frame.setProcess(0, 100);
                    fixCollectorOrder(conn); // fixes the Ordinal number of Collectors
                    
                    frame.incOverall();

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //                                                                                                              //
                    // Schema Changes 1.8                                                                                           //
                    //                                                                                                              //
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    int instID = BasicSQLUtils.getCountAsInt("SELECT InstitutionID FROM institution");
                    
                    frame.setDesc("Updating Reference Work..."); // I18N
                    String instName = "InstitutionID";
                    tblName = getTableNameAndTitleForFrame(ReferenceWork.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, instName))
                    {
                        String updateSQL = "ALTER TABLE %s ADD COLUMN %s INT(11) NOT NULL AFTER JournalID";
                        if (addColumn(conn, databaseName, tblName, instName,  updateSQL))
                        {
                            update(conn, "ALTER TABLE referencework ADD KEY `FK5F7C68DC81223908` (`InstitutionID`)");
                            update(conn, String.format("UPDATE referencework SET InstitutionID=%d", instID));
                            update(conn, "ALTER TABLE referencework ADD CONSTRAINT `FK5F7C68DC81223908` FOREIGN KEY (`InstitutionID`) REFERENCES `institution` (`UserGroupScopeId`)");
                        } else {
                            return false;
                        }
                    }
                    frame.setProcess(0, 100);
                    
                    fixConservDescriptions(conn);

                    frame.setDesc("Updating Journals..."); // I18N
                    tblName = getTableNameAndTitleForFrame(Journal.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, instName))
                    {
                        String updateSQL = "ALTER TABLE %s ADD COLUMN %s INT(11) NOT NULL AFTER Text1";
                        if (addColumn(conn, databaseName, tblName, instName,  updateSQL))
                        {
                            update(conn, "ALTER TABLE journal ADD KEY `FKAB64AF3781223908` (`InstitutionID`)");
                            update(conn, String.format("UPDATE journal SET InstitutionID=%d", instID));
                            update(conn, "ALTER TABLE journal ADD CONSTRAINT `FKAB64AF3781223908` FOREIGN KEY (`InstitutionID`) REFERENCES `institution` (`UserGroupScopeId`)");
                        } else {
                            return false;
                        }
                    }
                    frame.setProcess(0, 100);
                    frame.incOverall(); 

                    frame.setDesc("Updating Permits..."); // I18N
                    tblName = getTableNameAndTitleForFrame(Permit.getClassTableId());
                    if (!doesColumnExist(databaseName, tblName, instName))
                    {
                        String updateSQL = "ALTER TABLE %s ADD COLUMN %s INT(11) NOT NULL AFTER IssuedToID";
                        if (addColumn(conn, databaseName, tblName, instName,  updateSQL))
                        {
                            update(conn, "ALTER TABLE permit ADD KEY `FKC4E3841B81223908` (`InstitutionID`)");
                            update(conn, String.format("UPDATE permit SET InstitutionID=%d", instID));
                            update(conn, "ALTER TABLE permit ADD CONSTRAINT `FKC4E3841B81223908` FOREIGN KEY (`InstitutionID`) REFERENCES `institution` (`UserGroupScopeId`)");
                        } else {
                            return false;
                        }
                    }
                    frame.setProcess(0, 100);
                    frame.incOverall(); 

                    //-----------------------------------------------------------------------------
                    //-- LocalityDetail
                    //-----------------------------------------------------------------------------
                    frame.setDesc("Updating Locality Detail..."); // I18N
                    
                    // Change column types for UTMEasting, UTMNorthing and UTMScale
                    tblName = getTableNameAndTitleForFrame(LocalityDetail.getClassTableId());
                    String eastingColumnType = getFieldColumnType(conn, databaseName, tblName, "UTMEasting");
                    if (eastingColumnType == null)
                    {
                        errMsgList.add(String.format(COL_TYP_NO_DET, tblName));
                        return false;
                    }
                    if (!eastingColumnType.trim().equalsIgnoreCase("DECIMAL(19,2)"))
                    {
                        count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM " + tblName + " where UtmEasting is not null");
                        if (count > 0)
                        {
                            File outFile = new File(UIRegistry.getAppDataDir() + File.separator + "localityDetailUtmEasting.txt");
                            try {
                            	PrintWriter pw = new PrintWriter(outFile);
                            	String q = "select localitydetailid, UtmEasting from localitydetail "
										+ " where UtmEasting is not null";
                            	ResultSet rs = stmt.executeQuery(q);
                            	while (rs.next()) {
                            		pw.write(String.format("%d\t%f\n",
										rs.getInt(1), rs.getFloat(2)));
                            	}
                            	rs.close();
                            	pw.flush();
                            	pw.close();
                            } catch (IOException ex) {
                            	ex.printStackTrace();
                            }
                        }
                        count = getCount(tblName);
                        rv = update(conn, "ALTER TABLE localitydetail CHANGE COLUMN `UtmEasting` `UtmEasting` DECIMAL(19,2) NULL DEFAULT NULL");
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    String northingColumnType = getFieldColumnType(conn, databaseName, tblName, "UTMNorthing");
                    if (northingColumnType == null)
                    {
                        errMsgList.add(String.format(COL_TYP_NO_DET, tblName));
                        return false;
                    }
                    if (!northingColumnType.trim().equalsIgnoreCase("DECIMAL(19,2)"))
                    {
                        count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM " + tblName + " where utmNorthing is not null");
                        if (count > 0)
                        {
                            File outFile = new File(UIRegistry.getAppDataDir() + File.separator + "localityDetailUtm.txt");
                            try {
                            	PrintWriter pw = new PrintWriter(outFile);
                            	String q = "select localitydetailid, UtmNorthing from localitydetail "
										+ " where UtmNorthing is not null";
                            	ResultSet rs = stmt.executeQuery(q);
                            	while (rs.next()) {
                            		pw.write(String.format("%d\t%f\n",
										rs.getInt(1), rs.getFloat(2)));
                            	}
                            	rs.close();
                            	pw.flush();
                            	pw.close();
                            } catch (IOException ex) {
                            	ex.printStackTrace();
                            }
                        }
                        count = getCount(tblName);
                        rv = update(conn, "ALTER TABLE localitydetail CHANGE COLUMN `UtmNorthing` `UtmNorthing` DECIMAL(19,2) NULL DEFAULT NULL");
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    String scaleColumnType = getFieldColumnType(conn, databaseName, tblName, "UTMScale");
                    if (scaleColumnType == null)
                    {
                        errMsgList.add(String.format(COL_TYP_NO_DET, tblName));
                        return false;
                    }
                    if (!scaleColumnType.trim().equalsIgnoreCase("DECIMAL(20,10)"))
                    {
                        count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM " + tblName + " where UTMScale is not null");
                        if (count > 0)
                        {
                            File outFile = new File(UIRegistry.getAppDataDir() + File.separator + "localityDetailUtm.txt");
                            try {
                            	PrintWriter pw = new PrintWriter(outFile);
                            	String q = "select localitydetailid, UtmScale from localitydetail "
										+ " where UtmScale is not null";
                            	ResultSet rs = stmt.executeQuery(q);
                            	while (rs.next()) {
                            		pw.write(String.format("%d\t%f\n",
										rs.getInt(1), rs.getFloat(2)));
                            	}
                            	rs.close();
                            	pw.flush();
                            	pw.close();
                            } catch (IOException ex) {
                            	ex.printStackTrace();
                            }
                        }
                        count = getCount(tblName);
                        rv = update(conn, "ALTER TABLE localitydetail CHANGE COLUMN `UtmScale` `UtmScale` DECIMAL(20,10) NULL DEFAULT NULL");
                        if (rv != count)
                        {
                            errMsgList.add(String.format(UPD_CNT_NO_MATCH, tblName));
                            return false;
                        }
                    }
                    frame.incOverall();
                    
                    //End LocalityDetail changes
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    frame.setProcess(0, 100);
                    frame.incOverall(); 
                    
                    Integer[] sgrTblIds = new Integer[] {CollectionObject.getClassTableId(), CollectingEvent.getClassTableId(), 
                                                         Locality.getClassTableId(), WorkbenchRow.getClassTableId()};
                    String[]  sgrAfter  = new String[] {"TotalValue", "Remarks", "Text2", "UploadStatus"};
                    String sgrStatusField = "SGRStatus";
                    int i = 0;
                    for (Integer tblId : sgrTblIds)
                    {
                        tblName = getTableNameAndTitleForFrame(tblId);
                        if (!doesColumnExist(databaseName, tblName, ocrField))
                        {
                            if (!addColumn(conn, databaseName, tblName, sgrStatusField,  TINYINT4, sgrAfter[i]))
                            {
                                return false;
                            }
                        }
                        i++;
                    }

                    addNewAttachmentTables(conn);

                    frame.setDesc("Adding ISO Code field to Geography"); // I18N
                    String geoCode = "GeographyCode";
                    tblName = getTableNameAndTitleForFrame(Geography.getClassTableId());
                    len     = getFieldLength(conn, databaseName, tblName, geoCode);
                    if (len != null && len == 8)
                    {
                        alterFieldLength(conn, databaseName, tblName, geoCode, 8, 24);
                    }
                    
                    // Adding fields to the 'attachment' table and fill them in
                    frame.setDesc("Adding TableID to Attachment Table"); // I18N
                    if (!addTableIDToAttachmentTable(conn, databaseName))
                    {
                        return false;
                    }
                    
                    frame.setDesc("Adding and fixing Attachment scoping");
                    if (!addScopingToAttachmentTable(conn, databaseName))
                    {
                        return false;
                    }
                    
                    frame.setDesc("Fixing PDF mimetype in Attachment Table");
                    update(conn, 
                            "UPDATE attachment SET MimeType='application/pdf' " +
                    		"WHERE MimeType = 'application/octet-stream' " +
                    		"AND LOWER(SubStr(AttachmentLocation, LENGTH(AttachmentLocation) - 2, LENGTH(AttachmentLocation))) = 'pdf'");
                    
                    tblName = getTableNameAndTitleForFrame(Attachment.getClassTableId());
                    len     = getFieldLength(conn, databaseName, tblName, "origFilename");
                    if (len != null && len == 128)
                    {
                        alterFieldLength(conn, databaseName, tblName, "origFilename", 128, 20000);
                    }
                    
                    len     = getFieldLength(conn, databaseName, tblName, "title");
                    if (len != null && len == 64)
                    {
                        alterFieldLength(conn, databaseName, tblName, "title", 64, 255);
                    }
                    
                    frame.setDesc("Updating GUIDs"); // I18N
                    if (!addGUIDCols(conn, databaseName))
                    {
                        return false;
                    }
                    
                    generateMissingGUIDs(frame);
                    frame.incOverall();
                    
                    // Setting new Field Length for QueryFields
                    String startValue = "StartValue";
                    tblName = getTableNameAndTitleForFrame(SpQueryField.getClassTableId());
                    len     = getFieldLength(conn, databaseName, tblName, startValue);
                    if (len != null && len == 64)
                    {
                        alterFieldLength(conn, databaseName, tblName, startValue, 64, 255);
                    }
                    String endValue = "EndValue";
                    len     = getFieldLength(conn, databaseName, tblName, endValue);
                    if (len != null && len == 64)
                    {
                        alterFieldLength(conn, databaseName, tblName, endValue, 64, 255);
                    }

                    frame.incOverall();

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //                                                                                                              //
                    // Schema Changes 1.9                                                                                           //
                    //                                                                                                              //
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    //-------------------------------------------------------------------
                    // spreport -- fix for bug #9414. 
                    //-------------------------------------------------------------------
                    if (!getFieldNullability(conn, databaseName, "spreport", "SpQueryID")) {
                    
                    	frame.setDesc("Fixing workbench-based reports...");
                    	String usql = "ALTER TABLE " + databaseName + 
                    		".spreport CHANGE COLUMN SpQueryID SpQueryID INT(11) NULL";
                    	if (update(conn, usql) == -1) {
                    		errMsgList.add("update error: " + usql);
                    		return false;
                    	}
                    }
                    frame.incOverall(); 
                    
                    //-------------------------------------------------------------------
                    // loan -- bug #9492. 
                    //-------------------------------------------------------------------
                    
                    frame.setDesc("Stretching SrcGeography and SrcGeography fields for loans and gifts...");
                    alterFieldLength(conn, databaseName, "loan", "SrcGeography",  32, 500);
                    alterFieldLength(conn, databaseName, "loan", "SrcTaxonomy",  32, 500);

                    //-------------------------------------------------------------------
                    // gift -- bug #9492. 
                    //-------------------------------------------------------------------
                    alterFieldLength(conn, databaseName, "gift", "SrcGeography",  32, 500);
                    alterFieldLength(conn, databaseName, "gift", "SrcTaxonomy",  32, 500);

                    frame.incOverall();

                    //-------------------------------------------------------------------
                    //-- Change long strings that were created as text in earlier dbs and
                    //-- as varchar in newer versions.
                    //
                    //-- bug #9457. 
                    //-------------------------------------------------------------------
                    String[][] toFix = {
                    		{"accession", "Text1,Text2,Text3"},
                    		{"borrow", "Text1,Text2"},
                    		{"collectionobject", "Text1,Text2"},
                    		{"collectionobjectattribute", "Text1,Text2,Text3"},                    		
                    		{"collectingeventattribute", "Text1,Text2,Text3"},
                    		{"commonnametxcitation", "Text1,Text2"},
                    		{"deaccession", "Text1,Text2"},
                    		{"determination", "Text1,Text2"},
                    		{"dnasequencingruncitation", "Text1,Text2"},
                    		{"exchangein", "Text1,Text2"},
                    		{"exchangeout", "Text1,Text2"},
                    		{"gift", "Text1,Text2"},
                    		{"lithostrat", "Text1,Text2"},
                    		{"loan", "Text1,Text2"},
                    		{"locality", "Text1,Text2"},
                    		{"localitydetail", "Text1,Text2"},
                    		{"permit", "Text1,Text2"},
                    		{"preparation", "Text1,Text2"},
                    		{"preparationattribute", "Text1,Text2,Text10"},
                    		{"project", "Text1,Text2"},
                    		{"referencework", "Text1,Text2"},
                    		{"shipment", "Text1,Text2"},
                    		{"taxoncitation", "Text1,Text2"},
                    		{"workbenchdataitem", "CellData"}
                    };
                    
                    for (String[] fldToFix : toFix) {
                    	String tbl = fldToFix[0];
                        frame.setDesc("Fixing " + tbl + " user-defined Text field types...");
                    	String[] flds = fldToFix[1].split(",");
                    	List<String> fldsToFix = new ArrayList<String>(flds.length);
                    	for (String fld : flds) {
                    		if (!"text".equals(getFieldColumnType(conn, databaseName, tbl, fld))) {
                    			fldsToFix.add(fld);
                    		}
                    	}
                    	String fldFixSql = "alter table " + databaseName + "." + tbl;
                    	boolean comma = false;
                    	for (String fld : fldsToFix) {
                    		if (comma) {
                    			fldFixSql += ",";
                    		} else {
                    			comma = true;
                    		}
                    		fldFixSql += " change column " + fld + " " + fld + " text";
                    	}
                        if (update(conn, fldFixSql) == -1) {
                            errMsgList.add("update error: " + fldFixSql);
                            return false;
                        }
                    }
                    frame.incOverall(); 
                    
                    //------------------------------------------------------------------
                    // Stretch ReferenceWork.Title and ReferenceWork.Publisher. #9718
                    //alterFieldLength(conn, databaseName, "referencework", "Title", 255, 255);
                    alterFieldLength(conn, databaseName, "referencework", "Publisher", 50, 250);
                    
                    //-------------------------------------------------------------------
                    // Create tables need for Geography Cleanup tool
                    // geonames tables
                    //-------------------------------------------------------------------
                    //addGeoCleanupTables();
                    //frame.incOverall();
                    
                    //-------------------------------------------------------------------
                    //-- Create tables needed for iPad Export
                    //-------------------------------------------------------------------
                    addIPadExporterTables(conn);
                    frame.incOverall(); 
                    
                    //-------------------------------------------------------------------
                    // Fixing unique constraint on author
                    //-- bug #9454. 
                    //-------------------------------------------------------------------
                    
                    if (doesIndexExist("author", "OrderNumber")) {
                    	sql = "ALTER TABLE " + databaseName + ".author " +
                    		"DROP INDEX OrderNumber " + 
                    		", ADD UNIQUE INDEX AgentIDX (ReferenceWorkID ASC, AgentID ASC)";
                    	frame.setDesc("Fixing ReferenceWork Author index...");
                    	if (update(conn, sql) == -1) {
                    		errMsgList.add("update error: " + sql);
                    		return false;
                    	}
                    }
                    frame.incOverall();
                    
                    //------------------------------------------------------------
                    // Matching CollectionMemberIDs between preparation and collectionobject
                    // --bug #9760
                    fixPreparationCollectionMemberID();
                    frame.incOverall();
                    
                    
                    //-------------------------------------------------------------------
                    // Ordinal in AttachmentObject tables made non-nullable
                    //-- bug #9423. 
                    //-------------------------------------------------------------------

                    String[] attObjTbls = {"accessionattachment",
                    		"agentattachment",
                    		"borrowattachment",
                    		"collectingeventattachment",
                    		"collectionobjectattachment",
                    		"conservdescriptionattachment",
                    		"conserveventattachment",
                    		"dnasequenceattachment",
                    		"dnasequencerunattachment",
                    		"fieldnotebookattachment",
                    		"fieldnotebookpageattachment",
                    		"fieldnotebookpagesetattachment",
                    		"giftattachment",
                    		"loanattachment",
                    		"localityattachment",
                    		"permitattachment",
                    		"preparationattachment",
                    		"referenceworkattachment",
                    		"repositoryagreementattachment",
                    		"taxonattachment"
                    };
                    for (String tbl : attObjTbls) {
                    	if (getFieldNullability(conn, databaseName, tbl, "Ordinal")) {
                    		frame.setDesc("Fixing " + tbl + " Ordinal field requirement setting...");
                    		String objTbl = tbl.replace("attachment", "");
                    		String objTblKey = "dnasequencerun".equals(objTbl) ? "dnasequencingrunid" : objTbl + "ID";
                    		String attObjTblKey = "dnasequencerun".equals(objTbl) ? "dnasequencingrunattachmentid" : tbl + "ID";
                    		String q = "select " + objTblKey + ", " + attObjTblKey + " from " + 
                    			databaseName + "." + tbl + " where Ordinal is null";
                    		List<Object[]> nulls = BasicSQLUtils.query(conn, q);
                    		if (nulls != null && nulls.size() > 0) {
                    			for (Object[] row : nulls) {
                    				q = "select max(Ordinal) from " + databaseName + "." + tbl 
                    					+ " where " + objTblKey + "=" + row[0].toString();
                    				Integer max = BasicSQLUtils.getCount(conn, q);
                    				if (max == null) {
                    					max = -1;
                    				}
                    				max += 1;
                    				q = "update " + databaseName + "." + tbl + " set Ordinal=" + max +
                    					" where " + attObjTblKey + "=" + row[1].toString();
                    				if (-1 == update(conn, q)) {
                    					errMsgList.add("update error: " + q);
                    					return false;
                    				}
                    			}
                    		}
                    		q = "ALTER TABLE " + databaseName + "." + tbl + 
                        		" CHANGE COLUMN `Ordinal` `Ordinal` INT(11) NOT NULL";
                    		if (-1 == update(conn, q)) {
                    			errMsgList.add("update error: " + q);
                    			return false;
                    		}
                    	}
                    }
                    frame.incOverall();
                    
                    //-----------------------------------------------------------------------
                    //
                    // Schema changes for 2.1
                    //
                    //--------------------------------------------------------------------------
                    
                    //change geocoord field types and values in export cache tables
                    sql = "select SpExportSchemaMappingID, MappingName from spexportschemamapping";
                    List<Object[]> mappings = BasicSQLUtils.query(conn, sql);
                    for (Object[] mapping : mappings) {
                    	frame.setDesc("Fixing geocoordinate precision for '" + mapping[1] + "' mapping...");
                    	boolean updateOK = false;
                    	try {
                    		updateOK = ExportToMySQLDB.updateLatLngInCache(conn, (Integer)mapping[0]);
                    	} catch (Exception ex) {
                    		log.error(ex);
                    	}
                    	if (!updateOK) {
                    		log.error("Unable to fix geocoords for " + mapping[1]+ ". Setting LastExportTime to null to force cache rebuild.");
                    		BasicSQLUtils.update(conn, "update spexportschemamapping set TimestampExported=null where spexportschemamappingid="
                    				+ mapping[0]);
                    	}
                    }
                    frame.incOverall();
                    
                    //-----------------------------------------------------------------------
                    //
                    // Schema changes for 2.2
                    //
                    //--------------------------------------------------------------------------
                    
                    //change preparationattribute.attrdate from timestamp to date
                	frame.setDesc("Fixing data type for preparationattribute.AttrDate");
                    sql = "alter table preparationattribute modify column attrdate date null";
            		if (-1 == update(conn, sql)) {
            			errMsgList.add("update error: " + sql);
            			return false;
            		}
            		frame.incOverall();
            		
            		//add attachment.IsPublic, with default value = true
                	frame.setDesc("Adding attachment.IsPublic field");
            		if (!doesColumnExist(databaseName, "attachment", "isPublic", conn)) {
            			sql = "alter table attachment add column IsPublic bit(1) NOT NULL DEFAULT TRUE";
            			if (-1 == update(conn, sql)) {
            				errMsgList.add("update error: " + sql);
            				return false;
            			}
            		}
            		frame.incOverall();
            		
            		frame.setDesc("Fixing data type for localitydetail.Start/EndDepthUnit");
            		sql = "alter table localitydetail modify column enddepthunit varchar(23), modify column startdepthunit varchar(23)";
            		if (-1 == update(conn, sql)) {
        				errMsgList.add("update error: " + sql);
        				return false;
        			}
                    frame.incOverall(); 

            		frame.setDesc("Adding index for CollectionObject.AltCatalogNumber");
            		if (!doesIndexExist("collectionobject", "AltCatalogNumberIDX")) {
            			sql = "create index AltCatalogNumberIDX on collectionobject(AltCatalogNumber)";
            			if (-1 == update(conn, sql)) {
            				errMsgList.add("update error: " + sql);
            				return false;
            			}
            		}
                    frame.incOverall(); 

                    //-------------------------------------------------------------------------------
                    //
                    // Schema changes for 2.4
                    //
                    //-------------------------------------------------------------------------------
                    
                    frame.setDesc("Picklist value enlengthenization");
                    sql = "alter table picklistitem modify column `Title` varchar(128), modify column `Value` varchar(128)";
            		if (-1 == update(conn, sql)) {
        				errMsgList.add("update error: " + sql);
        				return false;
        			}
                    frame.incOverall(); 

                    frame.setDesc("Stretching AltCatalogNumber");
                    sql = "alter table collectionobject modify column AltCatalogNumber varchar(64)";
            		if (-1 == update(conn, sql)) {
        				errMsgList.add("update error: " + sql);
        				return false;
        			}
                    frame.incOverall(); 

            		//-------------------------------------------------------------------------------
                    //
                    // Schema changes for 2.5
                    //
                    //-------------------------------------------------------------------------------

                    frame.setDesc("Fixing index for Preparation GUID");
                    if (!doesIndexExist("preparation", "PrepGuidIDX") && doesColumnExist(databaseName, "preparation", "GUID")) {
                        if (!fixDupPrepGuids(conn)) {
                            errMsgList.add("update error: Fix Prep GUIDs");
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Changing CollectionObjectAttribute.Number8 to an integer.");
                    sql = "alter table collectionobjectattribute modify column number8 int";
                    if (-1 == update(conn, sql)) {
                        errMsgList.add("update error: " + sql);
                        return false;
                    }
                    frame.incOverall();

                    frame.setDesc("Changing CollectionObjectAttribute.Number30 to an integer.");
                    sql = "alter table collectionobjectattribute modify column number30 int";
                    if (-1 == update(conn, sql)) {
                        errMsgList.add("update error: " + sql);
                        return false;
                    }
                    frame.incOverall();

                    //-------------------------------------------------------------------------------
                    //
                    // Schema changes for 2.6
                    //
                    //-------------------------------------------------------------------------------
                    frame.setDesc("Increasing storage size for Query Builder search values.");
                    if (getFieldLength(conn, databaseName, "spqueryfield", "StartValue") != 1000) {
                        sql = "alter table spqueryfield modify column StartValue varchar(1000)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    if (getFieldLength(conn, databaseName, "spqueryfield", "EndValue") != 1000) {
                        sql = "alter table spqueryfield modify column EndValue varchar(1000)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Modifying SpAuditLogField table.");
                    sql = "alter table spauditlogfield modify column NewValue text null";
                    if (-1 == update(conn, sql)) {
                        errMsgList.add("update error: " + sql);
                        return false;
                    }
                    sql = "alter table spauditlogfield modify column OldValue text null";
                    if (-1 == update(conn, sql)) {
                        errMsgList.add("update error: " + sql);
                        return false;
                    }
                    sql = "alter table spauditlogfield modify column FieldName varchar(128)";
                    if (-1 == update(conn, sql)) {
                        errMsgList.add("update error: " + sql);
                        return false;
                    }
                    frame.incOverall();

                    //-------------------------------------------------------------------------------
                    //
                    // Schema changes for 2.7
                    //
                    //-------------------------------------------------------------------------------
                    frame.setDesc("Adding index for Preparation.SampleNumber");
                    if (!doesIndexExist("preparation", "PrepSampleNumIDX")) {
                        sql = "create index PrepSampleNumIDX on preparation(SampleNumber)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of TreatmentEvent.type");
                    sql = "alter table treatmentevent modify column `Type` varchar(128)";
                    if (-1 == update(conn, sql)) {
                        errMsgList.add("update error: " + sql);
                        return false;
                    }
                    frame.incOverall();

                    if (!doesTableExist(databaseName, "spstynthy")) {
                        frame.setDesc("Modifying specify system tables.");
                        sql = S2nPrefsPanel.getSynthyTblCreateSQL();
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of Geography.Name");
                    sql = "alter table geography modify column `Name` varchar(128)";
                    if (-1 == update(conn, sql)) {
                        errMsgList.add("update error: " + sql);
                        return false;
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of Geography.FullName");
                    sql = "alter table geography modify column `FullName` varchar(500)";
                    if (-1 == update(conn, sql)) {
                        errMsgList.add("update error: " + sql);
                        return false;
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of Agent.LastName");
                    sql = "alter table agent modify column `LastName` varchar(256)";
                    if (-1 == update(conn, sql)) {
                        errMsgList.add("update error: " + sql);
                        return false;
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of CollectingTrip.CollectingTripName");
                    sql = "alter table collectingtrip modify column `CollectingTripName` varchar(250)";
                    if (-1 == update(conn, sql)) {
                        errMsgList.add("update error: " + sql);
                        return false;
                    }
                    frame.incOverall();


                    //-------------------------------------------------------------------------------
                    //
                    // Schema changes for 2.8 & 2.9
                    //
                    //-------------------------------------------------------------------------------
//                    frame.setDesc("Converting deaccessionpreparation.quantity to int");
//                    String coType = getFieldColumnType(conn, databaseName, "deaccessionpreparation", "quantity");
//                    if (coType != null && coType.endsWith("int(6)")) {
//                        sql = "alter table deaccessionpreparation modify quantity int(11)";
//                        if (-1 == update(conn, sql)) {
//                            errMsgList.add("update error: " + sql);
//                            return false;
//                        }
//                    }
//                    frame.setDesc("Removing deaccession.accessionid");
//                    if (doesColumnExist(databaseName, "deaccession", "accessionid", conn)) {
//                        sql = "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_SCHEMA = '"
//                            + databaseName + "' AND REFERENCED_TABLE_NAME = 'accession' AND TABLE_NAME = 'deaccession' AND COLUMN_NAME = 'AccessionID'";
//                        String constraint = BasicSQLUtils.querySingleObj(sql);
//                        if (constraint != null) {
//                            sql = "alter table deaccession drop foreign key " + constraint;
//                            if (-1 == update(conn, sql)) {
//                                errMsgList.add("update error: " + sql);
//                                return false;
//                            }
//                        }
//                        sql = "alter table deaccession drop column accessionid";
//                        if (-1 == update(conn, sql)) {
//                            errMsgList.add("update error: " + sql);
//                            return false;
//                        }
//                    }
//                    frame.incOverall();
                    frame.setDesc("Checking for duplicate export mapping names.");
                    Vector<Object[]> dups = BasicSQLUtils.query("select mappingname from spexportschemamapping group by 1 having count(spexportschemamappingid) > 1");
                    if (dups != null && dups.size() > 0) {
                        errMsgList.add("The database schema cannot be updated because it contains duplicate Export Schema Mapping names. Please contact Specify customer support.");
                        return false;
                    }
                    frame.incOverall();
                    frame.setDesc("Removing old Deaccession tables");
                    if (!doesTableExist(databaseName, "disposal")) {
                        if (BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM deaccession") > 0) {
                            errMsgList.add("The database schema cannot be updated because it contains deaccession data. Please contact Specify customer support.");
                            return false;
                        }
                        sql = "SELECT TABLE_NAME,CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE "
                                + "REFERENCED_TABLE_SCHEMA = '" + databaseName + "' AND REFERENCED_TABLE_NAME = 'deaccessionpreparation'";
                        Vector<Object[]> constraints = query(conn, sql);
                        for (Object[] c : constraints) {
                            //sql = "alter table " + c[0] + " drop constraint " + c[1];
                            sql = "alter table " + c[0] + " drop foreign key " + c[1];
                            if (-1 == update(conn, sql)) {
                                errMsgList.add("update error: " + sql);
                            }
                        }
                        sql = "drop table deaccessionpreparation";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                        sql = "drop table deaccessionagent";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                        sql = "drop table deaccession";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                        sql = "select splocalecontainerid from splocalecontainer where name in('deaccessionpreparation',"
                                + "'deaccessionagent', 'deaccession')";
                        Vector<Object> ids = BasicSQLUtils.querySingleCol(sql);
                        String containerIdList = commaSeparate(ids);
                        sql = "select splocalecontaineritemid from splocalecontaineritem where splocalecontainerid in("
                            + containerIdList + ")";
                        ids = BasicSQLUtils.querySingleCol(sql);
                        String containerItemIdList = commaSeparate(ids);
                        String inStr = " in(" + containerItemIdList + ") ";
                        sql = "delete from splocaleitemstr where SpLocaleContainerItemNameID" + inStr
                                + "or SpLocaleContainerItemDescID" + inStr;
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                        inStr = " in(" + containerIdList + ") ";
                        sql = "delete from splocaleitemstr where SpLocaleContainerDescID" + inStr + "or "
                                + "SpLocaleContainerNameID" + inStr;
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                        sql = "delete from splocalecontaineritem where splocalecontainerid in(" + containerIdList + ")";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                        sql = "delete from splocalecontainer where splocalecontainerid in(" + containerIdList + ")";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    if (!doesIndexExist("exchangeout", "ExchangeOutNumberIDX")) {
                        frame.setDesc("Adding index for ExchangeOut.ExchangeOutNumber");
                        sql = "create index ExchangeOutNumberIDX on exchangeout(ExchangeOutNumber)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    if (getFieldNullability(conn, databaseName, "exchangeout", "ExchangeOutNumber")) {
                        frame.setDesc("Requiring ExchangeOut.ExchangeOutNumber");
                        String usql = "update exchangeout set exchangeoutnumber = '' where exchangeoutnumber is null";
                        if (update(conn, usql) == -1) {
                            errMsgList.add("update error: " + usql);
                            return false;
                        }
                        usql = "ALTER TABLE " + databaseName +
                                ".exchangeout CHANGE COLUMN ExchangeOutNumber ExchangeOutNumber varchar(50) NOT NULL";
                        if (update(conn, usql) == -1) {
                            errMsgList.add("update error: " + usql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing size of Determination.FeatureOrBasis.");
                    if (getFieldLength(conn, databaseName, "determination", "FeatureOrBasis") != 250) {
                        sql = "alter table determination modify column FeatureOrBasis varchar(250)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing size of CollectionObject.Description.");
                    if (!getFieldColumnType(conn, databaseName, "collectionobject", "Description").equalsIgnoreCase("text")) {
                        sql = "alter table collectionobject modify column Description text(65535)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing size of Referencework.Title.");
                    if (getFieldLength(conn, databaseName, "referencework", "Title") != 400) {
                        sql = "alter table referencework modify column Title varchar(400)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing size of Collectingtrip.Collectingtripname.");
                    if (getFieldLength(conn, databaseName, "collectingtrip", "Collectingtripname") != 400) {
                        sql = "alter table collectingtrip modify column Collectingtripname varchar(400)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing size of Address.Address3.");
                    if (getFieldLength(conn, databaseName, "address", "Address3") != 400) {
                        sql = "alter table address modify column Address3 varchar(400)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing size of Address.Address4.");
                    if (getFieldLength(conn, databaseName, "address", "Address4") != 400) {
                        sql = "alter table address modify column Address4 varchar(400)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing size of Address.Address5.");
                    if (getFieldLength(conn, databaseName, "address", "Address5") != 400) {
                        sql = "alter table address modify column Address5 varchar(400)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of BorrowMaterial.Description");
                    if (getFieldLength(conn, databaseName, "borrowmaterial", "Description") != 250) {
                        sql = "alter table borrowmaterial modify column `Description` varchar(250)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Creating picklists.");
                    sql = "select collectionid from collection";
                    Vector<Object> colls = BasicSQLUtils.querySingleCol(conn, sql);
                    String[][] pls = {
                            {"DeaccessionType", "Gift", "Destructive Sampling", "Exchange", "Lost"},
                            {"DeaccessionStatus", "No Data", "In Process", "Complete"},
                            {"DeaccessionAgentRole", "Approver", "Other", "Preparer", "Receiver", "Sponsor", "Staff", "Student"},
                            {"DisposalAgentRole", "Approver", "Other", "Preparer", "Receiver", "Sponsor", "Staff", "Student"},
                            {"DisposalType", "Destroyed", "Destructive Sampling", "Lost"}
                    };
                    for (Object coll : colls) {
                        if (!doPicklists(conn, coll, pls)) {
                            errMsgList.add("error building picklists for collection " + coll);
                            return false;
                        }
                    }
                    frame.incOverall();

                    //-------------------------------------------------------------------------------
                    //
                    // Schema changes for 2.10
                    //
                    //-------------------------------------------------------------------------------

                    frame.setDesc("Converting float fields to decimal.");
                    SchemaUpdateService.createDBTablesFromSQLFile(conn, "floats_to_decimals.sql");
                    frame.incOverall();

                    frame.setDesc("Increasing length of Locality.LocalityName");
                    if (getFieldLength(conn, databaseName, "locality", "LocalityName") != 1024) {
                        sql = "alter table locality modify column `LocalityName` varchar(1024) not null";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of SpLocaleItemStr.Text");
                    if (getFieldLength(conn, databaseName, "splocaleitemstr", "Text") != 2048) {
                        sql = "alter table splocaleitemstr modify column `Text` varchar(2048) not null";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of PicklistItem.Title");
                    if (getFieldLength(conn, databaseName, "picklistitem", "Title") != 1024) {
                        sql = "alter table picklistitem modify column `Title` varchar(1024) not null";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of PicklistItem.Value");
                    if (getFieldLength(conn, databaseName, "picklistitem", "Value") != 1024) {
                        sql = "alter table picklistitem modify column `Value` varchar(1024)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of Workbench.Name");
                    if (getFieldLength(conn, databaseName, "workbench", "Name") != 256) {
                        sql = "alter table workbench modify column `Name` varchar(256)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Changing Attachment.origFilename to text.");
                    if (!getFieldColumnType(conn, databaseName, "attachment", "origFilename").equalsIgnoreCase("text")) {
                        sql = "alter table attachment modify column origFilename text(65535) not null";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Changing SpQueryField.StartValue to text.");
                    if (!getFieldColumnType(conn, databaseName, "spqueryfield", "StartValue").equalsIgnoreCase("text")) {
                        sql = "alter table spqueryfield modify column StartValue text(65535)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Changing SpQueryField.EndValue to text.");
                    if (!getFieldColumnType(conn, databaseName, "spqueryfield", "EndValue").equalsIgnoreCase("text")) {
                        sql = "alter table spqueryfield modify column EndValue text(65535)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of Attachment.Mimetype");
                    if (getFieldLength(conn, databaseName, "attachment", "MimeType") != 1024) {
                        sql = "alter table attachment modify column `MimeType` varchar(1024)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Changing Container.Description to text.");
                    if (!getFieldColumnType(conn, databaseName, "container", "Description").equalsIgnoreCase("text")) {
                        sql = "alter table container modify column Description text(65535)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing length of Container.Name");
                    if (getFieldLength(conn, databaseName, "container", "Name") != 1024) {
                        sql = "alter table container modify column `Name` varchar(1024)";
                        if (-1 == update(conn, sql)) {
                            errMsgList.add("update error: " + sql);
                            return false;
                        }
                    }
                    frame.incOverall();

                    frame.setDesc("Increasing size of CollectingEventAttribute.text* fields.");
                    String[] ceatextfields = {
                        "Text10",
                        "Text11",
                        "Text12",
                        "Text13",
                        "Text14",
                        "Text15",
                        "Text16",
                        "Text17",
                        "Text2",
                        "Text3",
                        "Text4",
                        "Text5",
                        "Text6",
                        "Text7",
                        "Text8",
                        "Text9"
                    };
                    for (String field : ceatextfields) {
                        if (!getFieldColumnType(conn, databaseName, "collectingeventattribute", field).equalsIgnoreCase("text")) {
                            sql = "alter table collectingeventattribute modify column " + field + " text(65535)";
                            if (-1 == update(conn, sql)) {
                                errMsgList.add("update error: " + sql);
                                return false;
                            }
                        }
                    }
                    frame.incOverall();
                    
                    frame.setProcess(0, 100);
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

    private boolean doPicklists(Connection conn, Object collId, String[][] pls) {
        for (String[] pl : pls) {
            String sql = "select count(*) from picklist where collectionid = " + collId + " and name ='" + pl[0] + "'";
            if (getCountAsInt(conn, sql) == 0) {
                sql = "insert into picklist(version,timestampcreated,collectionid,name,issystem,type,readonly,sizelimit)"
                        + " values(0, now()," + collId + ", '" + pl[0] + "',true,  0, true, " + (pl.length - 1) + ")";
                if (-1 == update(conn, sql)) {
                    return false;
                }
                for (int i = 1; i < pl.length; i++) {
                    sql = "insert into picklistitem(TimestampCreated, Version, Title, Value, Picklistid) "
                            + " values(now(), 0, '" + pl[i] + "', '" + pl[i] + "', (select picklistid from picklist where name = '"
                            + pl[0] + "' and collectionid = " + collId + "))";
                    if (-1 == update(conn, sql)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private String commaSeparate(Vector<Object> v) {
        String result = null;
        if (v != null && v.size() > 0) {
            result = "";
            for (Object o : v) {
                if (result.length() > 0) {
                    result += ",";
                }
                result += o;
            }
        }
        return result;
    }

    private boolean fixDupPrepGuids(final Connection conn) {
        String sql = "select GUID, count(preparationid), group_concat(preparationid order by preparationid) from preparation "
                + "where GUID is not null group by 1 having count(preparationid) > 1 order by 1";
        List<Object[]> dups = BasicSQLUtils.query(conn, sql);
        boolean dupsPresent = dups.size() > 0;
        boolean result = true;
        int inc = 1;
        frame.setProcessPercent(false);
        int max = dupsPresent ? dups.size() + 4 : 2;
        frame.setProcess(0, max);
        if (dupsPresent) {
            //System.out.println("alter table preparation add index tempprepguididx(GUID)");
            update(conn, "alter table preparation add index tempprepguididx(GUID)");
            frame.setProcess(inc++);
            for (Object[] dup : dups) {
                String ids[] = ((String)dup[2]).split(",");
                sql = "update preparation set guid = null where guid = '" + dup[0] + "' and preparationid != " + ids[0];
                int r = BasicSQLUtils.update(conn, sql);
                frame.setProcess(inc++);
                if (((Number)dup[1]).intValue() != r+1) {
                    result =  false;
                    break;
                }
            }
            BasicSQLUtils.update(conn, "alter table preparation drop index tempprepguididx");
            frame.setProcess(inc++);
        }
        if (result) {
            BasicSQLUtils.update(conn, "alter table preparation add unique index PrepGuidIDX(guid)");
            frame.setProcess(inc++);
            BasicSQLUtils.update(conn, "update preparation set guid = uuid(), timestampmodified=now(), " +
                    "modifiedbyagentid = createdbyagentid where guid is null");
            frame.setProcess(inc++);
        }
        //System.out.println("CHECK!!!!!!!!!!!!!!!!" + result);
        frame.processDone();
        frame.getProcessProgress().setIndeterminate(true);
        return result;
    }

    /**
     * Matches preparations' CollectionMemberIDs with their collectionobjects' CollectionMemberIDs.  
     */
    public void fixPreparationCollectionMemberID() {
    	try {
    		int cnt = getCountAsInt("SELECT COUNT(PreparationID) FROM preparation p INNER JOIN collectionobject co "
    			+ " ON co.CollectionObjectID = p.CollectionObjectID WHERE co.CollectionMemberID != p.CollectionMemberID");
    		if (cnt > 0) {
    			String sql = "UPDATE preparation p INNER JOIN collectionobject co "
        			+ " ON co.CollectionObjectID = p.CollectionObjectID SET p.CollectionMemberID=co.CollectionMemberID"
    				+ " WHERE co.CollectionMemberID != p.CollectionMemberID";
    			BasicSQLUtils.update(sql);
    		}
    		cnt = getCountAsInt("select count(*) from preparation p inner join preptype pt on pt.PrepTypeID = p.PrepTypeID where pt.CollectionID != p.CollectionMemberID");
        	if (cnt > 0) {
        		//This might not work in rare cases where error has been present for a long time
        		//and preptypes in the correct collection have been deleted 
        		String sql = "update preparation p inner join preptype pt on pt.PrepTypeID = p.PrepTypeID "
        			+ "inner join preptype ptc on ptc.Name = pt.Name and ptc.CollectionID = p.CollectionMemberID "
        			+ "set p.PrepTypeID = ptc.PrepTypeID";
        		BasicSQLUtils.update(sql);
        	}
    	} catch (Exception ex) {
    		log.error(ex.getMessage());
    	}
    }

    /**
     * @param conn
     */
    protected void fixConservDescriptions(final Connection conn)
    {
        String sql = "SELECT cd.ConservDescriptionID, d.DivisionID, a.DivisionID FROM conservdescription cd " +
            		 "LEFT JOIN collectionobject co ON cd.CollectionObjectID = co.CollectionObjectID " +
            		 "LEFT JOIN collection c ON co.CollectionID = c.UserGroupScopeId " +
            		 "LEFT JOIN discipline d ON c.DisciplineID = d.UserGroupScopeId " +
            		 "LEFT JOIN agent a ON cd.CreatedByAgentID = a.AgentID WHERE cd.DivisionID IS NULL";
        
        for (Object[] row : BasicSQLUtils.query(conn, sql))
        {
            Integer csId = (Integer)row[0];
            Integer dv1Id = (Integer)row[1]; // ColObj Div
            Integer dv2Id = (Integer)row[2]; // Agent Div
            
            Integer divId = dv2Id != null ? dv2Id : dv1Id;
            if (divId != null)
            {
                sql = "UPDATE conservdescription SET DivisionID = "+divId + " WHERE ConservDescriptionID="+csId;
                /*int rv = */BasicSQLUtils.update(sql);
                //System.out.println("rv= "+rv+"  "+sql);
            }
        }
    }
    
    /**
     * 
     */
    public static void addNewAttachmentTables(final Connection conn)
    {
        File sqlFile = XMLHelper.getConfigDir("new_attch_tables.sql");
        try
        {
            String str = FileUtils.readFileToString(sqlFile);
            String[] stmts = StringUtils.splitPreserveAllTokens(str, ';');
            for (String sql : stmts)
            {
                String[] toks = StringUtils.splitPreserveAllTokens(sql, '`');
                if (toks.length == 73)
                {
                    if (!BasicSQLUtils.doesTableExist(conn, toks[1]))
                    {
                        int rv = BasicSQLUtils.update(conn, sql);
                        System.out.println("rv = "+rv);
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @param conn
     * @param stmt
     * @return
     * @throws SQLException
     */
    private void fixTablesRefWorksAttachmentsScoping(final Connection conn) throws SQLException
     {
         int      instID = BasicSQLUtils.getCountAsInt("SELECT InstitutionID FROM institution");
         String  sql   = String.format("UPDATE attachment SET ScopeID = %d, ScopeType = %d " +
         		                        "WHERE AttachmentID IN " +
         		                        "(SELECT AttachmentID FROM referenceworkattachment) " +
                                        "AND (ScopeID IS NULL OR ScopeType IS NULL)",
         		                        instID, Attachment.INSTITUTION_SCOPE);
         int cnt = update(conn, sql);
         log.debug(String.format("Updated %d RefWorks Attachments", cnt));
     }
                                 
    /**
     * @param conn
     * @param stmt
     * @return
     * @throws SQLException
     */
    private void fixTablesAccessionAttachmentsScoping(final Connection conn) throws SQLException
     {
         int     instID        = BasicSQLUtils.getCountAsInt("SELECT InstitutionID FROM institution");
         boolean isMgrGlobally = BasicSQLUtils.getCountAsInt("SELECT IsAccessionsGlobal FROM institution") != 0;
         
         String sql;
         if (isMgrGlobally)
         {
             sql = String.format(
                     "UPDATE attachment, accessionattachment " +
                     "SET attachment.ScopeID = %d, attachment.ScopeType = %d " +
                     "WHERE attachment.AttachmentID = accessionattachment.AttachmentID " +
                     "AND (attachment.ScopeID IS NULL OR attachment.ScopeType IS NULL)", 
                     instID, Attachment.INSTITUTION_SCOPE);
         } else {
             sql = String.format(
                     "UPDATE attachment, accessionattachment, accession " +
                     "SET attachment.ScopeID = accession.DivisionID, attachment.ScopeType = %d " +
                     "WHERE attachment.AttachmentID = accessionattachment.AttachmentID " +
                     "AND accession.AccessionID = accessionattachment.AccessionID " +
                     "AND (attachment.ScopeID IS NULL OR attachment.ScopeType IS NULL)",
                     Attachment.DIVISION_SCOPE);
         }

         int cnt = update(conn, sql);
         log.debug(String.format("Updated %d Accession Attachments", cnt));
     }
                                 
    
    private String makeSimpleAttachmentFixerSql(final Class<?> cls, final String scopeIdCol, 
                                                final byte scopeType)
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(cls.getName());
        
        String mainTable = ti.getName();
        String joinTable = (ti.getTableId() == 88 ? "dnasequencerun" : ti.getName()) + "attachment";
        String idCol = cls.getSimpleName() + "ID";
        
        String scopeIDExpr = (scopeType == Attachment.GLOBAL_SCOPE) ? "NULL" : "maintable." + scopeIdCol;
        
        return String.format(
                "UPDATE attachment, %s jointable, %s maintable " +
                "SET attachment.ScopeID = %s, attachment.ScopeType = %d " +
                "WHERE attachment.AttachmentID = jointable.AttachmentID " +
                "AND jointable.%s = maintable.%s " +
                "AND (attachment.ScopeID IS NULL OR attachment.ScopeType IS NULL)",
                joinTable, mainTable,
                scopeIDExpr, scopeType,
                idCol, idCol);
    }
    
    
 
  
    /**
     * @param conn
     * @param stmt
     * @param classes
     * @param sqls
     * @param scopeIdCol
     * @param scopeType
     * @return
     * @throws SQLException
     */
    private void fixSimpleAttachmentScoping(final Connection conn, 
                                                 final Class<?>[] classes, 
                                                 final String scopeIdCol,
                                                 final byte scopeType) throws SQLException
    {
        for (Class<?> cls : classes)
        {
            String sql = makeSimpleAttachmentFixerSql(cls, scopeIdCol, scopeType);
            int cnt = update(conn, sql);
            log.debug(String.format("Updated %d for %s", cnt, cls.getSimpleName()));
        }
    }
    
    /**
     * @param frame
     */
    private void generateMissingGUIDs(final ProgressFrame frame)
    {        
        if (GenericGUIDGeneratorFactory.getInstance() instanceof SpecifyGUIDGeneratorFactory)
        {
            SpecifyGUIDGeneratorFactory guidGen = (SpecifyGUIDGeneratorFactory)GenericGUIDGeneratorFactory.getInstance();
            guidGen.setFrame(frame);
            guidGen.buildGUIDs(null);
        }
    }
    
    /**
     * @param conn
     * @param databaseName
     * @return
     */
    private boolean addGUIDCols(final Connection conn, final String databaseName)
    {
        String   guidField = "GUID";
        int[]    tblIds    = {CollectingEvent.getClassTableId(), Attachment.getClassTableId(), Collection.getClassTableId(), Institution.getClassTableId(), Determination.getClassTableId()};
        String[] afterName = {"SGRStatus", "ScopeType", "EstimatedSize", "MinimumPwdLength", "YesNo2", };
        String[] indexName = {"CEGuidIDX", "AttchmentGuidIDX", "CollectionGuidIDX", "InstGuidIDX", "DeterminationGuidIDX"};
        
        for (int i=0;i<tblIds.length;i++)
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tblIds[i]);
            //frame.setDesc(String.format("Adding GUID field to %s", ti.getTitle())); // I18N
            String tblName = getTableNameAndTitleForFrame(tblIds[i]);
            if (!doesColumnExist(databaseName, tblName, guidField))
            {
                if (!addColumn(conn, databaseName, tblName, guidField,  "VARCHAR(128)", afterName[i]))
                {
                    UIRegistry.showError(String.format("There was an error updating the schema for table `%s`\n please contact the Specify Help Desk.", ti.getTitle()));
                    return false;
                }
                update(conn, String.format("CREATE INDEX %s ON %s(GUID)", indexName[i], ti.getName()));
            }
        }
       return true;
    }
    
    public boolean addScopingToAttachmentTable(final Connection conn, final String databaseName)
    {
        String scopeId = "ScopeID";
        String tblName = getTableNameAndTitleForFrame(Attachment.getClassTableId());

        if (!doesColumnExist(databaseName, tblName, scopeId))
        {
            if (!addColumn(conn, databaseName, tblName, scopeId,  INT11, "TableID"))
            {
                return false;
            }
        } 
        String scopeType = "ScopeType";
        if (!doesColumnExist(databaseName, tblName, scopeType))
        {
            if (!addColumn(conn, databaseName, tblName, scopeType,  "TINYINT", "ScopeID"))
            {
                return false;
            }
        } 

        try
        {
            int step = 1;
            frame.setProcess(0, 4);
            
            fixTablesAccessionAttachmentsScoping(conn);
            
            fixTablesRefWorksAttachmentsScoping(conn);
            
            
            Class<?>[] attachOwnerClasses = {
                    CollectionObject.class,  
                    Borrow.class,  // Borrow -> BorrowMaterial.ColMemID
                    DNASequence.class, 
                    DNASequencingRun.class, 
                    Preparation.class, 
                };
            fixSimpleAttachmentScoping(conn, attachOwnerClasses, "CollectionMemberID", Attachment.COLLECTION_SCOPE);
            frame.setProcess(step++);
            
            Class<?>[] divOwnerClasses = {
                    Agent.class,
                    ConservDescription.class,
                    RepositoryAgreement.class,
            };
            fixSimpleAttachmentScoping(conn, divOwnerClasses, "DivisionID", Attachment.DIVISION_SCOPE);
            frame.setProcess(step++);
            
            Class<?>[] dispOwnerClasses = {
                    CollectingEvent.class,
                    Gift.class,
                    Loan.class,
                    Locality.class,
                    FieldNotebook.class,  
            };
            fixSimpleAttachmentScoping(conn, dispOwnerClasses, "DisciplineID", Attachment.DISCIPLINE_SCOPE);
            frame.setProcess(step++);
            
            
            String[] sqls = {
                    String.format(
                    "UPDATE attachment, conserveventattachment, conservevent, conservdescription " +
                    "SET attachment.ScopeID = conservdescription.DivisionID, attachment.ScopeType = %d " +
                    "WHERE attachment.AttachmentID = conserveventattachment.AttachmentID " +
                    "AND conserveventattachment.ConserveventID = conservevent.ConserveventID " +
                    "AND conservevent.ConservDescriptionID = conservdescription.ConservDescriptionID",
                    Attachment.DIVISION_SCOPE),
                    
                    String.format(
                    "UPDATE attachment, fieldnotebookpagesetattachment, fieldnotebookpageset, fieldnotebook " +
                    "SET attachment.ScopeID = fieldnotebook.DisciplineID, attachment.ScopeType = %d " +
                    "WHERE attachment.AttachmentID = fieldnotebookpagesetattachment.AttachmentID " +
                    "AND fieldnotebookpagesetattachment.FieldNotebookPageSetID = fieldnotebookpageset.FieldNotebookPageSetID " +
                    "AND fieldnotebookpageset.FieldNotebookID = fieldnotebook.FieldNotebookID",
                    Attachment.DISCIPLINE_SCOPE),
                    
                    String.format(
                    "UPDATE attachment, fieldnotebookpageattachment, fieldnotebookpage, fieldnotebookpageset, fieldnotebook " +
                    "SET attachment.ScopeID = fieldnotebook.DisciplineID, attachment.ScopeType = %d " +
                    "WHERE attachment.AttachmentID = fieldnotebookpageattachment.AttachmentID " +
                    "AND fieldnotebookpageattachment.FieldNotebookPageID = fieldnotebookpage.FieldNotebookPageID " +
                    "AND fieldnotebookpage.FieldNotebookPageSetID = fieldnotebookpageset.FieldNotebookPageSetID " +
                    "AND fieldnotebookpageset.FieldNotebookID = fieldnotebook.FieldNotebookID",
                    Attachment.DISCIPLINE_SCOPE),
                    
                    String.format(
                    "UPDATE attachment, permitattachment, permit " +
                    "SET attachment.ScopeID = permit.InstitutionID, attachment.ScopeType = %d " +
                    "WHERE attachment.AttachmentID = permitattachment.AttachmentID " +
                    "AND permitattachment.PermitID = permit.PermitID",
                    Attachment.INSTITUTION_SCOPE),
                    
                    String.format(
                    "UPDATE attachment, taxonattachment, taxon, discipline " +
                    "SET attachment.ScopeID = discipline.UserGroupScopeID, attachment.ScopeType = %d " +
                    "WHERE attachment.AttachmentID = taxonattachment.AttachmentID " +
                    "AND taxonattachment.TaxonID = taxon.TaxonID " +
                    "AND taxon.TaxonTreeDefID = discipline.TaxonTreeDefID",
                    Attachment.DISCIPLINE_SCOPE)
            };
            
            for (String sql: sqls)
            {
                int cnt = update(conn, sql + " AND (attachment.ScopeID IS NULL OR attachment.ScopeType IS NULL)");
                log.debug(String.format("Updated %d attachments.", cnt));
            }

            frame.setProcess(step++);
            
            if (!doesIndexExist("attachment", "AttchScopeIDIDX"))
            {
                update(conn, "CREATE INDEX AttchScopeIDIDX ON attachment(ScopeID)");
            }            
            if (!doesIndexExist("attachment", "AttchScopeTypeIDX"))
            {
                update(conn, "CREATE INDEX AttchScopeTypeIDX ON attachment(ScopeType)");
            }
                        
            return true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
                                  
    /**
     * @param conn
     */
    public boolean addTableIDToAttachmentTable(final Connection conn, final String databaseName)
    {
        String tableID = "TableID";
        String tblName = getTableNameAndTitleForFrame(Attachment.getClassTableId());
        if (!doesColumnExist(databaseName, tblName, tableID))
        {
            if (!addColumn(conn, databaseName, tblName, tableID,  "SMALLINT", "Title"))
            {
                return false;
            }
        } else
        {
            String colType = getFieldColumnType(conn, databaseName, tblName, tableID);
            if (!colType.toLowerCase().startsWith("small"))
            {
                int rv = update(conn, "ALTER TABLE attachment MODIFY TableID SMALLINT");
                log.debug("rv = "+rv);
            }
        }
        
        try
        {
            Class<?>[] attachmentClasses = {
                    AccessionAttachment.class,
                    AgentAttachment.class,
                    BorrowAttachment.class,
                    CollectingEventAttachment.class,
                    CollectionObjectAttachment.class,
                    ConservDescriptionAttachment.class,
                    ConservEventAttachment.class,
                    DNASequenceAttachment.class,
                    DNASequencingRunAttachment.class,
                    FieldNotebookAttachment.class,
                    FieldNotebookPageAttachment.class,
                    FieldNotebookPageSetAttachment.class,
                    GiftAttachment.class,
                    LoanAttachment.class,
                    LocalityAttachment.class,
                    PermitAttachment.class,
                    PreparationAttachment.class,
                    ReferenceWorkAttachment.class,
                    RepositoryAgreementAttachment.class,
                    TaxonAttachment.class,
                };
            
            Class<?>[] ownerClasses = {
                    Accession.class,
                    Agent.class,
                    Borrow.class,
                    CollectingEvent.class,
                    CollectionObject.class,
                    ConservDescription.class,
                    ConservEvent.class,
                    DNASequence.class,
                    DNASequencingRun.class,
                    FieldNotebook.class,
                    FieldNotebookPage.class,
                    FieldNotebookPageSet.class,
                    Gift.class,
                    Loan.class,
                    Locality.class,
                    Permit.class,
                    Preparation.class,
                    ReferenceWork.class,
                    RepositoryAgreement.class,
                    Taxon.class,
                };

            frame.setProcess(0, attachmentClasses.length);
            
            int i = 1;
            for (Class<?> cls : attachmentClasses)
            {
                frame.setProcess(i);
                DBTableInfo ownerTI = DBTableIdMgr.getInstance().getByClassName(ownerClasses[i-1].getName());
                DBTableInfo ti      = DBTableIdMgr.getInstance().getByClassName(cls.getName());
                String sql = String.format(
                        "UPDATE attachment SET TableID = %d " +
                        "WHERE AttachmentID IN (SELECT AttachmentID FROM %s) " +
                        "AND TableID IS NULL",
                        ownerTI.getTableId(), ti.getName());
                
                int cnt = update(conn, sql);
                log.debug(String.format("Set TableID for %d attachments.", cnt));
                i++;
            }
            return true;
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * @param conn
     */
    public void fixCollectorOrder(final Connection conn)
    {
        frame.setDesc("Fixing Collector Ordering...");

        try
        {
            String post = " FROM (SELECT ce.CollectingEventID ID, COUNT(c.OrderNumber) CNT, MAX(c.OrderNumber) MX, MIN(c.OrderNumber) MN " +
                            "FROM collectingevent ce " +
                            "INNER JOIN collector c ON ce.CollectingEventID = c.CollectingEventID " +
                            "INNER JOIN agent a ON c.AgentID = a.AgentID GROUP BY ce.CollectingEventID) T1 WHERE MN != 0 OR MX+1 != CNT ";
       
            String sql = "SELECT COUNT(*)"+post;
            log.debug(sql);
            
            int totalCnt = BasicSQLUtils.getCountAsInt(conn, sql);
            if (totalCnt == 0) return;
            
            int percent = totalCnt / 50;
            percent = Math.max(percent,  1);
            frame.setProcess(0, 100);
            
            int cnt = 0;
            PreparedStatement pStmt  = conn.prepareStatement("SELECT CollectorID FROM collector WHERE CollectingEventID = ? ORDER BY OrderNumber");
            PreparedStatement pStmt2 = conn.prepareStatement("UPDATE collector SET OrderNumber = ? WHERE CollectorID = ?");
            Statement         stmt   = conn.createStatement();
            ResultSet         rs     = stmt.executeQuery("SELECT ID"+post);
            while (rs.next())
            {
                int order = 0;
                pStmt.setInt(1, rs.getInt(1));
                ResultSet rs2 = pStmt.executeQuery();
                while (rs2.next())
                {
                    pStmt2.setInt(1, order++);
                    pStmt2.setInt(2, rs2.getInt(1));
                    if (pStmt2.executeUpdate() != 1)
                    {
                        log.error("Error updating CollectorID "+rs2.getInt(1));
                    }
                }
                rs2.close();
                cnt++;
                if (cnt % percent == 0)
                {
                    frame.setProcess(cnt * 100 / totalCnt);
                }
            }
            rs.close();
            stmt.close();
            pStmt.close();
            pStmt2.close();
            
            frame.processDone();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Schema Update for 1.6
     */
    public boolean miscSchema16Updates(final Connection conn, final String databaseName) throws Exception
    {
        
        //-----------------------------------------------------------------------------
        //-- SpVersion
        //-----------------------------------------------------------------------------
        String tblName = getTableNameAndTitleForFrame(SpVersion.getClassTableId());
        if (!doesColumnExist(databaseName, tblName, "IsDBClosed"))
        {
            String[] instCols = {"IsDBClosed", "BIT(1)", "SchemaVersion", 
                                 "DbClosedBy", "VARCHAR(32)", "IsDBClosed"};
            if (!checkAndAddColumns(conn, databaseName, tblName, instCols))
            {
                return false;
            }
        }
        frame.incOverall();

        //-----------------------------------------------------------------------------
        //-- GeoCoordDetail
        //-----------------------------------------------------------------------------
        tblName = getTableNameAndTitleForFrame(GeoCoordDetail.getClassTableId());
        if (!doesColumnExist(databaseName, tblName, "UncertaintyPolygon"))
        {
            String[] instCols = {"UncertaintyPolygon", "TEXT", "MaxUncertaintyEstUnit", 
                                 "ErrorPolygon", "TEXT", "UncertaintyPolygon"};
            if (!checkAndAddColumns(conn, databaseName, tblName, instCols))
            {
                return false;
            }
        }
        frame.incOverall(); 

        frame.setDesc("Fixing SrcLatLonUnit in Locality");
        fixSrcLatLongUnit(conn);
        frame.incOverall(); 
        frame.setDesc("Processing...");

        return true;
    }
    
    /**
     * @param conn
     * @param databaseName
     * @throws SQLException
     */
    public static void createSGRTables(final Connection conn, final String databaseName) throws SQLException
    {

        if (!doesTableExist(databaseName, "sgrmatchconfiguration"))
        {
            String sql = "CREATE TABLE `sgrmatchconfiguration` (" +
                            "`id`                       bigint(20)      NOT NULL AUTO_INCREMENT, " +
                            "`name`                     varchar(128)    NOT NULL, " +
                            "`similarityFields`         text            NOT NULL, " +
                            "`serverUrl`                text            NOT NULL, " +
                            "`filterQuery`              varchar(128)    NOT NULL, " +
                            "`queryFields`              text            NOT NULL, " +
                            "`remarks`                  text            NOT NULL, " +
                            "`boostInterestingTerms`    tinyint(1)      NOT NULL, " +
                            "`nRows`                    int(11)         NOT NULL, " +
                            "PRIMARY KEY (`id`) " +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
            update(conn, sql);
        }
        
        if (!doesTableExist(databaseName, "sgrbatchmatchresultset"))
        {
            String sql = "CREATE TABLE `sgrbatchmatchresultset` (" +
                        "`id`                       bigint(20)      NOT NULL AUTO_INCREMENT, " +
                        "`insertTime`               timestamp       NOT NULL, " +
                        "`name`                     varchar(128)    NOT NULL, " +
                        "`recordSetID`              bigint(20)      DEFAULT NULL, " +
                        "`matchConfigurationId`     bigint(20)      NOT NULL, " +
                        "`query`                    text            NOT NULL, " +
                        "`remarks`                  text            NOT NULL, " +
                        "`dbTableId`                int(11)         DEFAULT NULL, " +
                        "PRIMARY KEY (`id`), " +
                        "KEY `sgrbatchmatchresultsetfk2` (`matchConfigurationId`), " +
                        "CONSTRAINT `sgrbatchmatchresultsetfk2` FOREIGN KEY (`matchConfigurationId`) REFERENCES `sgrmatchconfiguration` (`id`) " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
            update(conn, sql);
        }
        
        if (!doesTableExist(databaseName, "sgrbatchmatchresultitem"))
        {
            String sql = "CREATE TABLE `sgrbatchmatchresultitem` ( " +
                        "`id`                       bigint(20)      NOT NULL AUTO_INCREMENT, " +
                        "`matchedId`                varchar(128)    NOT NULL, " +
                        "`maxScore`                 float           NOT NULL, " +
                        "`batchMatchResultSetId`    bigint(20)      NOT NULL, " +
                        "`qTime`                    int(11)         NOT NULL, " +
                        "PRIMARY KEY (`id`), " +
                        "KEY `sgrbatchmatchresultitemfk1` (`batchMatchResultSetId`), " +
                        "CONSTRAINT `sgrbatchmatchresultitemfk1` FOREIGN KEY (`batchMatchResultSetId`) REFERENCES `sgrbatchmatchresultset` (`id`) ON DELETE CASCADE " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
            update(conn, sql);
        }
    }
    
    /**
     * @param oldDBConn
     * @param newDBConn
     */
    private LatLonConverter.FORMAT discoverUnitType(final String latLonStr)
    {
        LatLonConverter.FORMAT fmt = LatLonConverter.FORMAT.None;
        if (StringUtils.isNotEmpty(latLonStr))
        {
            int colonCnt = StringUtils.countMatches(latLonStr, ":");
            
            if (colonCnt == 0)
            {
                fmt = LatLonConverter.FORMAT.DDDDDD;
                
            } else if (colonCnt == 1)
            {
                fmt = LatLonConverter.FORMAT.DDMMMM;
                
            } else if (colonCnt == 2)
            {
                fmt = LatLonConverter.FORMAT.DDMMSS;
            }
        }
        return fmt;
    }
    

    /**
     * @param conn
     * @return
     */
    public static boolean updateDNAAttachments(final Connection conn)
    {
    	boolean isInnoDB = true;
        Object[] createRow = BasicSQLUtils.queryForRow("SHOW CREATE TABLE collectionobject");
        if (createRow != null && createRow.length > 1)
        {
        	isInnoDB = StringUtils.containsIgnoreCase(createRow[1].toString(), "InnoDB");
        }

        String dbType = isInnoDB ? "InnoDB" : "MyISAM";
        String dnaSeqRunAttSQL = String.format("CREATE TABLE `dnasequencerunattachment` ( `DnaSequencingRunAttachmentId` int(11) NOT NULL AUTO_INCREMENT, `TimestampCreated` datetime NOT NULL, `TimestampModified` datetime DEFAULT NULL, `Version` int(11) DEFAULT NULL, " +
                "`Ordinal` int(11) DEFAULT NULL, `Remarks` text, `ModifiedByAgentID` int(11) DEFAULT NULL, `AttachmentID` int(11) NOT NULL, `CreatedByAgentID` int(11) DEFAULT NULL, `DnaSequencingRunID` int(11) NOT NULL, " +
                "PRIMARY KEY (`DnaSequencingRunAttachmentId`), KEY `FKD0DAEB167699B003` (`CreatedByAgentID`), KEY `FKD0DAEB1678F036AA` (`DnaSequencingRunID`), KEY `FKD0DAEB16C7E55084` (`AttachmentID`), KEY `FKD0DAEB165327F942` (`ModifiedByAgentID`), " +
                "CONSTRAINT `FKD0DAEB165327F942` FOREIGN KEY (`ModifiedByAgentID`) REFERENCES `agent` (`AgentID`), CONSTRAINT `FKD0DAEB167699B003` FOREIGN KEY (`CreatedByAgentID`) REFERENCES `agent` (`AgentID`), " +
                "CONSTRAINT `FKD0DAEB1678F036AA` FOREIGN KEY (`DnaSequencingRunID`) REFERENCES `dnasequencingrun` (`DNASequencingRunID`), CONSTRAINT `FKD0DAEB16C7E55084` FOREIGN KEY (`AttachmentID`) REFERENCES `attachment` (`AttachmentID`) ) " +
                "ENGINE=%s DEFAULT CHARSET=utf8;", dbType);
        
        String dnaSeqAttSQL = String.format("CREATE TABLE `dnasequenceattachment` ( `DnaSequenceAttachmentId` int(11) NOT NULL AUTO_INCREMENT, `TimestampCreated` datetime NOT NULL, `TimestampModified` datetime DEFAULT NULL, `Version` int(11) DEFAULT NULL, " +
                            "`Ordinal` int(11) DEFAULT NULL, `Remarks` text, `AttachmentID` int(11) NOT NULL, `CreatedByAgentID` int(11) DEFAULT NULL, `DnaSequenceID` int(11) NOT NULL, `ModifiedByAgentID` int(11) DEFAULT NULL, PRIMARY KEY (`DnaSequenceAttachmentId`), " +
                            "KEY `FKFFC2E0FB265FB168` (`DnaSequenceID`), KEY `FKFFC2E0FB7699B003` (`CreatedByAgentID`), KEY `FKFFC2E0FBC7E55084` (`AttachmentID`), KEY `FKFFC2E0FB5327F942` (`ModifiedByAgentID`), " +
                            "CONSTRAINT `FKFFC2E0FB5327F942` FOREIGN KEY (`ModifiedByAgentID`) REFERENCES `agent` (`AgentID`), CONSTRAINT `FKFFC2E0FB265FB168` FOREIGN KEY (`DnaSequenceID`) REFERENCES `dnasequence` (`DnaSequenceID`), " +
                            "CONSTRAINT `FKFFC2E0FB7699B003` FOREIGN KEY (`CreatedByAgentID`) REFERENCES `agent` (`AgentID`), CONSTRAINT `FKFFC2E0FBC7E55084` FOREIGN KEY (`AttachmentID`) REFERENCES `attachment` (`AttachmentID`) ) " +
                            "ENGINE=%s DEFAULT CHARSET=utf8;", dbType);

        String insert = "INSERT INTO dnasequencerunattachment ( DnaSequencingRunAttachmentId, TimestampCreated, TimestampModified, Version, " +
                        "Ordinal, Remarks, AttachmentID, CreatedByAgentID, DnaSequencingRunID, ModifiedByAgentID) " +
                        "SELECT DnaSequencingRunAttachmentId, TimestampCreated, TimestampModified, Version, " +
                        "Ordinal, Remarks, AttachmentID, CreatedByAgentID, DnaSequencingRunID, ModifiedByAgentID FROM dnasequenceattachment ORDER BY DnaSequencingRunAttachmentId ASC";

        DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
        dbMgr.setConnection(conn);
        
        String dnasequenceattachment = "dnasequenceattachment";
        
        boolean isDnaSeqInError = dbMgr.doesDBHaveTable(dnasequenceattachment) &&
                                  dbMgr.doesFieldExistInTable(dnasequenceattachment, "DnaSequencingRunAttachmentId");
        
        int recCnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM dnasequenceattachment");
        log.debug("Number of dnasequenceattachment records: "+recCnt);
        
        boolean isDNASeqTableFixed = false;
        if (isDnaSeqInError)
        {
            if (recCnt ==  0)
            {
                int rv = BasicSQLUtils.update(conn, "DROP TABLE dnasequenceattachment");
                log.debug("Dropped Old table dnasequenceattachment: "+rv);
                if (rv != 0)
                {
                    log.info("Failed dropping dnasequenceattachment: "+rv);
                    return false;
                }
                
                rv = BasicSQLUtils.update(conn, dnaSeqAttSQL);
                log.debug("Created New table dnasequenceattachment: "+rv);
                if (rv != 0)
                {
                    log.info("Failed creating dnasequenceattachment: "+rv);
                    return false;
                }
                isDNASeqTableFixed = true;
                
            } else
            {
                UIRegistry.showError("The is a problem with the DNASequenceAttachment table that can not be fixed automatically.\nPlease contact the Specify Help Desk.");
                return false;
            }
        }
        
        if (dbMgr.doesDBHaveTable("dnasequencerunattachment"))
        {
            log.error("dnasequencerunattachment already exists");
            return false;
        }
        
        int rv;
        
        rv = BasicSQLUtils.update(conn, dnaSeqRunAttSQL);
        log.debug("Created dnasequencerunattachment: "+rv);
        if (rv != 0)
        {
            log.info("Failed creating dnasequencerunattachment: "+rv);
            return false;
        }
        
        if (recCnt > 0)
        {
            rv = BasicSQLUtils.update(conn, insert);
            log.debug("Moved Records dnasequencerunattachment: "+rv);
            if (rv != 0)
            {
                log.info("Failed moving records dnasequencerunattachment: "+rv);
                return false;
            }
        }
        
        if (!isDNASeqTableFixed)
        {
            rv = BasicSQLUtils.update(conn, "DROP TABLE dnasequenceattachment");
            log.debug("Dropped Old table dnasequenceattachment: "+rv);
            if (rv != 0)
            {
                log.info("Failed dropping dnasequenceattachment: "+rv);
                return false;
            }
            
            rv = BasicSQLUtils.update(conn, dnaSeqAttSQL);
            log.debug("Created New table dnasequenceattachment: "+rv);
            if (rv != 0)
            {
                log.info("Failed creating dnasequenceattachment: "+rv);
                return false;
            }
        }
        
        return true;
    }


    /**
     * @param oldDBConn
     * @param conn
     */
    private void fixSrcLatLongUnit(final Connection conn)
    {
        String post   = " FROM locality WHERE Lat1Text IS NOT NULL AND Long1Text IS NOT NULL AND OriginalLatLongUnit <> SrcLatLongUnit";
        String cntSQL = "SELECT COUNT(*)" + post;
        String sql    = "SELECT LocalityID, OriginalLatLongUnit, SrcLatLongUnit, Lat1Text, Long1Text" + post;
        
        int total = BasicSQLUtils.getCountAsInt(conn, cntSQL);
        int cnt = 0;
        //int updated = 0;
        
        frame.setProcess(0, total);
        PreparedStatement pStmt1 = null;
        try
        {
            pStmt1 = conn.prepareStatement("UPDATE locality SET SrcLatLongUnit=? WHERE LocalityID = ?");
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            while (rs.next())
            {
                int locID       = rs.getInt(1);
                //Integer orgUnit = rs.getInt(2);
                Integer srcUnit = rs.getInt(3);
                String  latStr  = rs.getString(4);
                String  lonStr  = rs.getString(5);
                
                LatLonConverter.FORMAT latFmt = discoverUnitType(latStr);
                LatLonConverter.FORMAT lonFmt = discoverUnitType(lonStr);
                
                LatLonConverter.FORMAT fmt;
                if (latFmt == LatLonConverter.FORMAT.DDMMSS || 
                    lonFmt == LatLonConverter.FORMAT.DDMMSS)
                {
                    fmt = LatLonConverter.FORMAT.DDMMSS;
                } else
                {
                    fmt = latFmt.ordinal() > lonFmt.ordinal() ? latFmt : lonFmt;
                }
                int fmtUnit = fmt.ordinal();
                if (fmtUnit != srcUnit)
                {
                    pStmt1.setInt(1, fmtUnit);
                    pStmt1.setInt(2, locID);
                    pStmt1.executeUpdate();
                    //updated++;
                }
                
                cnt++;
                if (cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
                    //System.out.println(String.format("%d / %d", cnt, total));
                }
            }
            frame.setProcess(total);
            rs.close();
            stmt.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (pStmt1 != null) pStmt1.close();
            } catch (Exception ex) {}
        }
        //System.out.println("Fixed "+cnt+" Locality records and updated "+updated);
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
    public void fixLatLonMethodGEOLocate(final Connection conn)
    {
        //-----------------------------------------------------------------------------
        // Fixes LatLonMethod to use a standard value 'GEOLocate'
        //-----------------------------------------------------------------------------
        
        // check first before issuing update
        if (BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM locality WHERE LOWER(LatLongMethod) = 'geolocate'") > 0)
        {
            update(conn, "UPDATE locality SET LatLongMethod='GEOLocate' WHERE LOWER(LatLongMethod) = 'geolocate'");
        }
        
        // This update should be very fast
        update(conn, "UPDATE picklistitem SET Title='GEOLocate', Value ='GEOLocate' WHERE LOWER(Title) = 'geolocate' OR LOWER(Value) = 'geolocate'");
    }
    /**
     * @param connection
     */
    public void fixDuplicatedPaleoContexts(final Connection conn)
    {
        String sql = "SELECT PaleoContextID FROM (SELECT pc.PaleoContextID, COUNT(pc.PaleoContextID) cnt " +
        	         "FROM paleocontext pc INNER JOIN collectionobject co ON pc.PaleoContextID = co.PaleoContextID " +
                     "GROUP BY pc.PaleoContextID) T1 WHERE cnt > 1 ";
        
        String coSQL = "SELECT CollectionObjectID FROM collectionobject WHERE PaleoContextID = ";
        
        
        List<String> pcFieldNames = getFieldNamesFromSchema(conn, "paleocontext");
        String       fieldStr     = buildSelectFieldList(pcFieldNames, null);
        fieldStr = StringUtils.remove(fieldStr, "PaleoContextID, ");
        
        StringBuilder sb = new StringBuilder("INSERT INTO paleocontext (");
        sb.append(fieldStr);
        sb.append(") SELECT ");
        sb.append(fieldStr);
        sb.append(" FROM paleocontext WHERE PaleoContextID = ?");
        
        String updateSQL = sb.toString();
        //System.out.println(updateSQL);
        
        boolean           isErr  = false;
        PreparedStatement pStmt  = null;
        PreparedStatement pStmt2 = null;
        try
        {
            pStmt  = conn.prepareStatement(updateSQL, Statement.RETURN_GENERATED_KEYS);
            pStmt2 = conn.prepareStatement("UPDATE collectionobject SET PaleoContextID=? WHERE CollectionObjectID = ?");
            
            for (Integer pcId : BasicSQLUtils.queryForInts(conn, sql))
            {
                Vector<Integer> colObjIds = BasicSQLUtils.queryForInts(conn, coSQL + pcId);
                for (int i=1;i<colObjIds.size();i++)
                {
                    pStmt.setInt(1, pcId);
                    int rv = pStmt.executeUpdate();
                    if (rv == 1)
                    {
                        Integer newPCId = BasicSQLUtils.getInsertedId(pStmt);
                        pStmt2.setInt(1, newPCId);
                        pStmt2.setInt(2, colObjIds.get(i));
                        rv = pStmt2.executeUpdate();
                        if (rv != 1)
                        {
                            log.error("Error updating co "+colObjIds.get(i));
                            isErr = true;
                        }
                    } else
                    {
                        log.error("Error updating pc "+pcId);
                        isErr = true;
                    }
                }
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, ex);
        } finally
        {
            try
            {
                if (pStmt != null) pStmt.close();
                if (pStmt2 != null) pStmt2.close();
            } catch (SQLException ex) {}
        }
        
        if (isErr)
        {
            UIRegistry.showError("There was an error updating the duplicated PaleoContexts\nPlease contact support.");
        }
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
                
                if (availAgents.size() > 0)
                {
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
            int rv = update(conn, sql);
            if (rv != 1)
            {
                errMsgList.add(String.format("Error deleting Agent %d Table %s rv: %d\n", id, tableName, rv));
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
            if (BasicSQLUtils.doesTableExist(DBConnection.getInstance().getConnection(), ti.getName()) 
            		&& ti.getRelationshipByName(relName) != null)
            {
                String sql = String.format("SELECT COUNT(*) FROM %s %s", ti.getName(), fullInClause);
                log.debug(sql);
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
        getTableNameAndTitleForFrame(PaleoContext.getClassTableId());
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
        getTableNameAndTitleForFrame(FieldNotebookPage.getClassTableId());
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
        // AttachmentImageAttribute Table
        /////////////////////////////
        if (doesTableExist(databaseName, "attachmentimageattribute"))
        {
        	alterFieldLength(conn, databaseName, "attachmentimageattribute", "CreativeCommons", 128, 500);
        	frame.incOverall();
        }
        
        /////////////////////////////
        // LocalityDetail
        /////////////////////////////
        
        String tblName = getTableNameAndTitleForFrame(LocalityDetail.getClassTableId());
        
        boolean statusOK = true;
        String sql = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'localitydetail' AND COLUMN_NAME = 'UtmScale' AND DATA_TYPE = 'varchar'", dbc.getDatabaseName());
        int count = BasicSQLUtils.getCountAsInt(sql);
        if (count > 0)
        {
            Vector<Object[]> values = query("SELECT ld.LocalityDetailID, ld.UtmScale, l.LocalityName " +
            	                                          "FROM localitydetail ld INNER JOIN locality l ON ld.LocalityID = l.LocalityID WHERE ld.UtmScale IS NOT NULL");
            
            update(conn, "ALTER TABLE localitydetail DROP COLUMN UtmScale");
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
        } else
        {
            addColumn(conn, databaseName, tblName, "UtmScale", "FLOAT",      "UtmOrigLongitude");
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
                tblName = getTableNameAndTitleForFrame(Determination.getClassTableId());
                addColumn(conn, databaseName, tblName, "VarQualifier",    "ALTER TABLE %s ADD COLUMN %s VARCHAR(16) AFTER Qualifier");
                addColumn(conn, databaseName, tblName, "SubSpQualifier", "ALTER TABLE %s ADD COLUMN %s VARCHAR(16) AFTER VarQualifier");
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
                        getTableNameAndTitleForFrame(CollectingEventAttribute.getClassTableId());
                        
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
                    
                    getTableNameAndTitleForFrame(Collector.getClassTableId());
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
                    //UIRegistry.showLocalizedError("SCHEMA_UPDATE_ERROR", errMsgStr);
                    JTextArea ta = UIHelper.createTextArea();
                    ta.setText(errMsgStr);
                    CellConstraints cc  = new CellConstraints();
                    PanelBuilder    pb  = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
                    pb.add(new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), cc.xy(1, 1));
                    pb.setDefaultDialogBorder();
                    
                    CustomDialog    dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), getResourceString("SCHEMA_UPDATE_ERROR"), true, pb.getPanel());
                    dlg.setVisible(true);
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
    private String getTableNameAndTitleForFrame(final int tableId)
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
            String  fmtSQL     = String.format(updateSQL, tableName, colName);
            int     rv         = update(conn, fmtSQL);
            boolean isAlterTbl = updateSQL.trim().toLowerCase().startsWith("alter table");
            if (rv == 0 && isAlterTbl)
            {
                return true;
            }
            
            if (rv != 1 && !isAlterTbl)
            {
                log.error(fmtSQL);
                errMsgList.add(String.format("Error adding column '%s' to table '%s'", colName, tableName));
                return false;
            }
        }
        return true; // true means it was OK if it wasn't added
    }
    
    protected boolean doesColumnExist(final String dbName, final String tableName, final String colName) {
    	return doesColumnExist(dbName, tableName, colName, null);
    }
    /**
     * @param dbName
     * @param tableName
     * @param colName
     * @return
     */
    protected boolean doesColumnExist(final String dbName, final String tableName, final String colName, final Connection con)
    {
        String  sql  = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s' AND COLUMN_NAME = '%s'", dbName, tableName, colName);
        //log.debug(sql);
        if (con == null) {
        	return BasicSQLUtils.getCountAsInt(sql) == 1;
        } else {
        	return BasicSQLUtils.getCountAsInt(con, sql) == 1;
        }
    }
    
    /**
     * @param tableName
     * @param indexName
     * @return
     */
    protected boolean doesIndexExist(final String tableName, final String indexName)
    {
        String  sql  = String.format("SHOW INDEX IN %s WHERE Key_name = '%s'", tableName, indexName);
        Vector<Object[]> rows =  BasicSQLUtils.query(sql);
        return rows != null && rows.size() > 0;
    }
    
    /**
     * @param dbName
     * @param tableName
     * @return
     */
    protected static boolean doesTableExist(final String dbName, final String tableName)
    {
        String  sql  = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`TABLES` WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'", dbName, tableName);
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
	protected void fixSchemaMappingScope(final Connection conn, final String databaseName) throws Exception 
	{
	    String tblName = getTableNameAndTitleForFrame(SpExportSchemaMapping.getClassTableId());
        if (!doesColumnExist(databaseName, tblName, "CollectionMemberID"))
        {
            String[] instCols = {"CollectionMemberID", INT11, "TimestampExported"};
            if (!checkAndAddColumns(conn, databaseName, tblName, instCols))
            {
                return;
            }
        }
        
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

//	protected void fixSchemaMappingTblNames(final Connection conn, final String databaseName) throws Exception {
//        String sql = "select distinct SpExportSchemaMappingID, MappingName, CollectionMemberID, SpecifyUserID from spexportschemamapping "
//                + "em inner join spexportschemaitemmapping im on im.spexportschemamappingid = em.spexportschemamappingid "
//                + " inner join spqueryfield qf on qf.spqueryfieldid = im.spqueryfieldid inner join spquery q on q.spqueryid = qf.spqueryid";
//        Vector<Object[]> mappingsToFix = BasicSQLUtils.query(sql);
//        if (mappingsToFix != null && mappingsToFix.size() > 0) {
//            for (Object[] row : mappingsToFix) {
//                String baseTbl = ExportToMySQLDB.fixTblNameForMySQL(row[1].toString());
//                String fixedTbl = ExportToMySQLDB.getExportMappingTblName(row[1].toString(), row[2].toString(), row[3].toString());
//                if (doesTableExist(databaseName, baseTbl)) {
//                    log.info("export mapping " + row[1] + ": renaming cache table from " + baseTbl + " to " + fixedTbl);
//                    if (!doesTableExist(databaseName, fixedTbl)) {
//                        sql = "rename table `" + baseTbl + "` to `" + fixedTbl + "`";
//                    } else {
//                        throw new Exception("table " + fixedTbl + " already exists.");
//                    }
//                }
//            }
//        }
//    }

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
        if (BasicSQLUtils.getCount(sql).equals(0))
        {
            AppPreferences.getGlobalPrefs().putBoolean(FIXED_GEO, true);
            return;
        }
        
        final int numRecs = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM geoname ge INNER JOIN geography g ON ge.name = g.Name WHERE ge.Name <> ge.asciiname");
        if (BasicSQLUtils.getCount(sql).equals(0))
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
    
    /**
     * @param conn
     * @param databaseName
     * @return
     */
    private boolean fixSpQuerySQLLength(Connection conn, String databaseName)
    {
		BasicSQLUtils.setSkipTrackExceptions(false);
		try {
			update(conn, "ALTER TABLE spquery CHANGE COLUMN `SqlStr` `SqlStr` TEXT NULL DEFAULT NULL");
		} catch (Exception ex) {
			errMsgList.add(String.format(
					"Error - Updating %s SpQuery.SqlStr - varchar(64) -> text  Excpt: %s",
					databaseName, "SpQuery", ex.getMessage()));
			return false;
		}
		BasicSQLUtils.setSkipTrackExceptions(false);
		return true;
    }

    //-----------------------------------------------------------------------------------------------
    //-- Methods for adding the new tables that are needed for the SpecifyInsight iPad app
    //-- And for Geography Cleanup tools
    //-----------------------------------------------------------------------------------------------
    private Discipline getCurrentDiscipline()
    {
        Discipline               disp       = AppContextMgr.getInstance().getClassObject(Discipline.class);
        DataProviderSessionIFace session    = DataProviderFactory.getInstance().createSession();
        Discipline               discipline = session.get(Discipline.class, disp.getId());
        if (session != null) 
        {
            session.close();
        }
        return discipline;
    }
    
    /**
     * @return
     */
    private boolean addGeoCleanupTables()
    {
        try
        {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            BuildFromGeonames bldGeoNames = new BuildFromGeonames(null, now, null, 
                                                                  itUserNamePassword.first, itUserNamePassword.second, frame);    
            boolean isOK =  bldGeoNames.loadGeoNamesDB(DBConnection.getInstance().getConnection().getCatalog());
            if (!isOK)
            {
                //return SchemaUpdateService.createDBTablesFromSQLFile(conn, "create_geonames_tables.sql");
            }
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * @param conn
     * @return
     */
    public static boolean addIPadExporterTables(final Connection conn)
    {
        try
        {
            return SchemaUpdateService.createDBTablesFromSQLFile(conn, "build_ipad_xreftables.sql");
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
