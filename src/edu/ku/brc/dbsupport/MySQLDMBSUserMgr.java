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
package edu.ku.brc.dbsupport;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 27, 2009
 *
 */
public class MySQLDMBSUserMgr extends DBMSUserMgr 
{
    private static final int[] PERM_LIST  = {PERM_SELECT, PERM_INSERT, PERM_UPDATE, PERM_DELETE, PERM_LOCK_TABLES, PERM_ALTER_TABLE, PERM_CREATE_TABLE, PERM_DROP_TABLE, };

    private static final Logger log = Logger.getLogger(MySQLDMBSUserMgr.class);
    
	private DBConnection dbConnection = null;
	private Connection   connection   = null;
    private String       itUsername   = null;
    private String       itPassword   = null;
    
    private DatabaseDriverInfo driverInfo;
	
	/**
	 * 
	 */
	public MySQLDMBSUserMgr() 
	{
		super();
		
		driverInfo = DatabaseDriverInfo.getDriver("MySQL");
		if (driverInfo == null || DBConnection.getInstance().isEmbedded())
		{
		    driverInfo = DatabaseDriverInfo.getDriver("MySQLEmbedded");
		}
	}
	
	/**
	 * @return
	 */
	public Connection getConnection()
	{
	    return connection;
	}
	
	/**
	 * @param userName
	 * @param password
	 * @param databaseHost
	 * @return
	 */
	@Override
	public boolean connectToDBMS(final String itUsernameArg, 
	                             final String itPasswordArg,
	                             final String databaseHost)
	{
	    try
        {
            itUsername = itUsernameArg;
            itPassword = itPasswordArg;
            hostName   = databaseHost;
            
     	    String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Opensys, databaseHost, null);
     	    
    	    dbConnection   = new DBConnection(itUsernameArg, 
    	                                      itPasswordArg, 
    	                                      connStr, 
    	                                      driverInfo.getDriverClassName(), 
    	                                      driverInfo.getDialectClassName(), null);
            if (dbConnection != null)
            {
                connection = dbConnection.createConnection();
            }
            return connection != null;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
	    return false;
	}
	

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#connect(java.lang.String, java.lang.String)
     */
    @Override
    public boolean connect(final String itUsernameArg, 
                           final String itPasswordArg,
                           final String databaseHost,
                           final String dbName) 
    {
        itUsername = itUsernameArg;
        itPassword = itPasswordArg;
        hostName   = databaseHost;
        
        String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, databaseHost, dbName);
        if (connStr == null)
        {
            connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHost, dbName);
        }
        
        dbConnection   = new DBConnection(itUsernameArg, 
                                          itPasswordArg, 
                                          connStr, 
                                          driverInfo.getDriverClassName(), 
                                          driverInfo.getDialectClassName(), 
                                          dbName);
        if (connection == null)
        {
            connection = dbConnection.createConnection();
        }
        return connection != null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#getDatabaseList()
     */
    @Override
    public List<String> getDatabaseList()
    {
        ArrayList<String> names   = new ArrayList<String>();
        if (connection != null)
        {
            Vector<Object> dbNames = BasicSQLUtils.querySingleCol(connection, "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME <> 'information_schema' AND SCHEMA_NAME <> 'mysql' ORDER BY SCHEMA_NAME");
            for (Object nm : dbNames)
            {
                names.add(nm.toString());
            }
        }
        return names;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#getDatabaseListForUser(java.lang.String)
     */
    @Override
    public List<String> getDatabaseListForUser(final String username)
    {
        String[] permsArray = new String[] {"SELECT", "DELETE", "UPDATE", "INSERT", "LOCK TABLES", };
        HashSet<String> permsHash = new HashSet<String>();
        Collections.addAll(permsHash, permsArray);
        
        ArrayList<String> dbNames = new ArrayList<String>();
        try
        {
            if (connection != null)
            {
                String userStr = String.format("'%s'@'%s'", username, hostName);
                String sql = "SHOW GRANTS";
                for (Object obj : BasicSQLUtils.querySingleCol(connection, sql))
                {
                    boolean isAllDBs = false;
                    String  data     = (String)obj;
                    String  dbName   = null;
                    System.out.println("->["+data+"]");
                    if (StringUtils.contains(data, userStr))
                    {
                        // get database name
                        String[] toks = StringUtils.split(data, '`');
                        if (toks.length > 2)
                        {
                            dbName = toks[1];
                        }
                    } else if (StringUtils.contains(data, "ON *.* TO"))
                    {
                        //dbNames.add(obj.toString());   
                        isAllDBs = true;
                    }
                    
                    // get permissions
                    
                    
                    String permsStr = StringUtils.substringBetween(data, "GRANT ", " ON");
                    String[] pToks = StringUtils.split(permsStr, ',');
                    
                    if (pToks != null)
                    {
                        if (pToks.length == 1 && pToks[0].equalsIgnoreCase("ALL PRIVILEGES") && isAllDBs)
                        {
                            dbNames.addAll(getDatabaseList());
                            
                        } else if (pToks.length >= permsHash.size())
                        {
                            int cnt = 0;
                            for (String p : pToks)
                            {
                                if (permsHash.contains(p.trim()))
                                {
                                    cnt++;
                                }
                            }
                            
                            if (cnt == permsHash.size())
                            {
                                if (isAllDBs)
                                {
                                    dbNames.addAll(getDatabaseList());
                                    break;
                                    
                                } else if (dbName != null)
                                {
                                    dbNames.add(dbName);   
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return dbNames;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#canGrantPemissions(java.lang.String, java.lang.String)
     */
    @Override
    public boolean canGrantPemissions(String hostMachineName, String username)
    {
        PreparedStatement pStmt = null;
        try
        {
            if (connection != null)
            {
                try
                {
                    pStmt = connection.prepareStatement("SELECT Grant_priv FROM mysql.user WHERE Host = ? AND User = ?");
                    pStmt.setString(1, hostMachineName);
                    pStmt.setString(2, username);
                    
                    boolean hasPerm = false;
                    ResultSet rs = pStmt.executeQuery();
                    if (rs.next())
                    {
                        hasPerm = rs.getString(1).equals("Y");
                    }
                    rs.close();
                    return hasPerm;
                    
                } catch (SQLException ex)
                {
                    ex.printStackTrace();
                } finally
                {
                    try
                    {
                        if (pStmt != null) pStmt.close();
                    } catch (SQLException ex) {}
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#doesFieldExistInTable(java.lang.String, java.lang.String)
     */
    @Override
    public boolean doesFieldExistInTable(final String tableName, final String fieldName)
    {
        try
        {
            DatabaseMetaData mdm = connection.getMetaData();
            ResultSet        rs  = mdm.getColumns(connection.getCatalog(), connection.getCatalog(), tableName, null);
            while (rs.next())
            {
                String dbFieldName = rs.getString("COLUMN_NAME");
                if (dbFieldName.equals(fieldName))
                {
                    rs.close();
                    return true;
                }
            }
            rs.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#setConnection(java.sql.Connection)
     */
    @Override
    public void setConnection(Connection connection)
    {
        this.connection = connection;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#createDatabase(java.lang.String)
     */
    @Override
    public boolean createDatabase(final String dbName)
    {
        try
        {
            if (connection != null)
            {
                int rv = BasicSQLUtils.update(connection, "CREATE DATABASE "+dbName);
                if (rv == 1)
                {
                    String sql = String.format("GRANT ALL ON %s.* TO '%s'@'%s' IDENTIFIED BY '%s'", dbName, itUsername, hostName, itPassword);
                    //log.debug(sql);
                    rv = BasicSQLUtils.update(connection, sql);
                    return rv == 0;
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#dropDatabase(java.lang.String)
     */
    @Override
    public boolean dropDatabase(String dbName)
    {
        try
        {
            if (connection != null)
            {
                int rv = BasicSQLUtils.update(connection, "DROP DATABASE "+dbName); // Returns number of tables
                
                return rv > -1;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }   
        
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#dropTable(java.lang.String)
     */
    @Override
    public boolean dropTable(String tableName)
    {
        try
        {
            if (connection != null)
            {
                int rv = BasicSQLUtils.update(connection, "DROP TABLE "+tableName); // Returns number of tables
                
                return rv > -1;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } 
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#dropUser(java.lang.String)
     */
    @Override
    public boolean dropUser(String username)
    {
        PreparedStatement pStmt       = null;
        PreparedStatement delStmtUser = null;
        PreparedStatement delStmtDB   = null;
        try
        {
            if (connection != null)
            {
                boolean isOK = true;
                try
                {
                    pStmt = connection.prepareStatement("SELECT host FROM mysql.user WHERE user = ?");
                    pStmt.setString(1, username);
                    
                    delStmtUser = connection.prepareStatement("DELETE FROM mysql.user WHERE user = ? AND host = ?");
                    delStmtDB   = connection.prepareStatement("DELETE FROM mysql.db WHERE user = ? AND host = ?");
                    
                    ResultSet rs = pStmt.executeQuery();
                    while (rs.next())
                    {
                        String hostNm = rs.getString(1);
                        
                        delStmtUser.setString(1, username);
                        delStmtUser.setString(2, hostNm);
                        if (delStmtUser.executeUpdate() == 0)
                        {
                            isOK = false;
                            break;
                        }
                        
                        delStmtDB.setString(1, username);
                        delStmtDB.setString(2, hostNm);
                        if (delStmtDB.executeUpdate() == 0)
                        {
                            isOK = false;
                            break;
                        }
                    }
                    rs.close();
                    
                    /*String[] tblNames = new String[] {"USER", "SCHEMA", "TABLE", "COLUMN"};
                    for (String tblNm : tblNames)
                    {
                        String sql = String.format("DELETE FROM information_schema.%s_PRIVILEGES WHERE GRANTEE = \"'%s'@'%s'\"",  tblNm, username, hostName);
                        BasicSQLUtils.update(connection, sql);
                    }*/
                    
                    BasicSQLUtils.update(connection, "FLUSH PRIVILEGES");

                    
                } catch (SQLException ex)
                {
                    ex.printStackTrace();
                    isOK = false;
                } finally
                {
                    try
                    {
                        if (pStmt != null) pStmt.close();
                        if (delStmtUser != null) delStmtUser.close();
                        if (delStmtDB != null) delStmtDB.close();
                    } catch (SQLException ex) {}
                }
                
                return isOK;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#exists(java.lang.String)
     */
    @Override
    public boolean doesDBExists(final String dbName)
    {
        if (dbName != null)
        {
            try
            {
                for (Object[] row : BasicSQLUtils.query(connection, "show databases"))
                {
                    if (row[0] != null && dbName.equalsIgnoreCase(row[0].toString()))
                    {
                        return true;
                    }
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#doesUserExists(java.lang.String)
     */
    @Override
    public boolean doesUserExists(String userName)
    {
        Integer count = BasicSQLUtils.getCount(connection, String.format("SELECT count(*) FROM mysql.user WHERE User = '%s' AND Host = '%s'", userName, hostName));
        return count == null ? false : count == 1;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#changePassword(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
	public boolean changePassword(final String username, final String oldPwd, final String newPwd) 
    {
        Statement stmt = null;
        try
        {
            if (connection != null)
            {
                stmt = connection.createStatement();
                String sql = String.format("SELECT host,user,password FROM mysql.user WHERE host = '%s' AND user = '%s' and password = password('%s')", hostName, username, oldPwd);
                Vector<Object[]> list = BasicSQLUtils.query(connection, sql);
                if (list != null && list.size() == 1)
                {
                    sql = String.format("UPDATE mysql.user SET Password=PASSWORD('%s') WHERE User='%s' AND Host='%s'", newPwd, username, hostName);
                    if (BasicSQLUtils.update(connection, sql) == 1)
                    {
                        return true;
                    }
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            close(stmt);
        }
		return false;
	}
    
    /**
     * @param permStr
     * @param permsArg
     * @return
     */
    private int addPerm(final String permStr, 
                        final int permsArg)
    {
        int perms = permsArg;
        if (permStr.equals("SELECT"))
        {
            perms |= PERM_SELECT;
            
        } else if (permStr.equals("UPDATE"))
        {
            perms |= PERM_UPDATE;
            
        } else if (permStr.equals("DELETE"))
        {
            perms |= PERM_DELETE;
            
        } else if (permStr.equals("ALL"))
        {
            perms |= PERM_ALL;
            
        } else if (permStr.equals("LOCK TABLES"))
        {
            perms |= PERM_LOCK_TABLES;
            
        } else if (permStr.equals("INSERT"))
        {
            perms |= PERM_INSERT;
            
        } else if (permStr.equals("CREATE"))
        {
            perms |= PERM_CREATE_TABLE;
            
        } else if (permStr.equals("DROP"))
        {
            perms |= PERM_DROP_TABLE;
            
        } else if (permStr.equals("ALTER"))
        {
            perms |= PERM_ALTER_TABLE;
            
        } else
        {
            log.error("Unhandled Permission ["+permStr+"]");
        }
        return perms;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#getPermissionsForUpdate(java.lang.String, java.lang.String)
     */
    private int getPermissionsFromMySQL(final String username, final String dbName, final boolean doAccess)
    {
        if (connection != null)
        {
            if (username.equalsIgnoreCase("root"))
            {
                return PERM_ALL;
            }
            
            StringBuilder debugLines   = new StringBuilder();
            boolean       doDebugPerms = AppPreferences.getLocalPrefs().getBoolean("DEBUG_IT_PERMS", false);
            

            String[] permNames = new String[] {"Select", "Insert", "Update", "Delete", "Lock_tables", "Alter", "Create", "Drop"};
            int      permLen   = doAccess ? 5 : permNames.length;
            
            StringBuilder sb = new StringBuilder("SELECT host `Host`");
            if (doAccess)
            {
                sb.append(", user `USER`, db `Database`");
            }
            sb.append(", REPLACE(RTRIM(CONCAT(");
            for (int i=0;i<permLen;i++)
            {
                String perm = permNames[i];
                if (i > 0) sb.append(',');
                if (doDebugPerms) sb.append("\n");
                sb.append(String.format("IF(%s_priv = 'Y', '%s ', '') ", perm, perm));
            }
            sb.append(")), ' ', ', ') AS `Privileges` FROM ");
            if (doAccess)
            {
                sb.append(String.format("mysql.db WHERE User = '%s' ORDER BY Host, User, Db", username));
            } else
            {
                sb.append(String.format("mysql.user WHERE User = '%s' ORDER BY Host, User", username));
            }
            
            log.debug(sb.toString());
            
            if (doDebugPerms)
            {
                debugLines.append(sb.toString() +"\n\n");
            }
            
            HashMap<String, Integer> nameToPerm = new HashMap<String, Integer>();
            for (int i=0;i<permLen;i++)
            {
                nameToPerm.put(permNames[i], PERM_LIST[i]);
            }
            
            HashMap<String, Integer> hostHash = new HashMap<String, Integer>();
            BasicSQLUtils.setSkipTrackExceptions(true);
            Vector<Object[]> rows = BasicSQLUtils.query(connection, sb.toString());
            BasicSQLUtils.setSkipTrackExceptions(false);
            
            for (Object[] row : rows)
            {
                String host = (String)row[0];
                if (StringUtils.isNotEmpty(host))
                {
                    String permStr = (String)row[1];
                    if (StringUtils.isNotEmpty(permStr))
                    {
                        int perms = PERM_NONE;
                        String[] privs = StringUtils.split(permStr, ',');
                        if (privs != null && privs.length > 0)
                        {
                            for (String p : privs)
                            {
                                Integer prm = nameToPerm.get(p.trim());
                                if (prm != null)
                                {
                                    debugLines.append("Adding Perm: " + p.trim()+"\n");
                                    perms |= prm;
                                }
                            }
                        }
                        if (doDebugPerms)
                        {
                            debugLines.append("Host: [" + host+"]\n\n");
                        }
                        hostHash.put(host, perms);
                    }
                }
            }
            
            if (doDebugPerms)
            {
                debugLines.append("hostHash.size(): " + hostHash.size()+"\n");
                
                int maxPerms = PERM_NONE;
                for (String key : hostHash.keySet())
                {
                    Integer p = hostHash.get(key);
                    debugLines.append("Key/Val: [" + key+ "]["+p+"]\n");
                    if (p != null && p > maxPerms)
                    {
                        maxPerms = p;
                    }
                }
                debugLines.append("maxPerms: " + maxPerms+"\n");
            
                JTextArea ta = UIHelper.createTextArea();
                ta.setText(debugLines.toString());
                
                JPanel p = new JPanel(new BorderLayout());
                p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
                p.add(UIHelper.createScrollPane(ta, true), BorderLayout.CENTER);
                
                CustomDialog dlg = new CustomDialog((Frame)null, "Debug", true, CustomDialog.OK_BTN, p);
                dlg.setOkLabel("Close");
                UIHelper.centerAndShow(dlg);
            }
            

            // if only one entry use that.
            if (hostHash.size() == 1)
            {
                return hostHash.values().iterator().next();
            }
            
            // Otherwise, use the best perms for any host 
            int maxPerms = PERM_NONE;
            for (Integer p : hostHash.values())
            {
                if (p > maxPerms)
                {
                    maxPerms = p;
                }
            }
            
            // Old Way this needs to go away
            if (maxPerms == PERM_NONE)
            {
                maxPerms = getPermissionsFromUserTable(username, dbName);
                if (maxPerms != PERM_NONE)
                {
                    UsageTracker.incrUsageCount("OLD_IT_PERMS_WORKED");
                }
            }
            
            return maxPerms;
        }
            
        return PERM_NONE;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#getPermissionsForAccess(java.lang.String, java.lang.String)
     */
    @Override
    public int getPermissionsForUpdate(final String username, final String dbName)
    {
        return getPermissionsFromMySQL(username, dbName, false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#getPermissionsForAccess(java.lang.String, java.lang.String)
     */
    @Override
    public int getPermissionsForAccess(final String username, final String dbName)
    {
        return getPermissionsFromMySQL(username, dbName, true);
    }
    
    /**
     * @param username
     * @param serverName
     * @param dbName
     * @return
     */
    public int getPermissionsUsingGrants(final String username, final String serverName, final String dbName)
    {
        try
        {
            if (connection != null)
            {
                //String sql = String.format("SHOW GRANTS FOR '%s'@'%s'", username, serverName);
                String uNameStr = String.format("'%s'@'", username);
                //log.debug(sql);
                Vector<Object[]> list = BasicSQLUtils.query(connection, "SHOW GRANTS");
                if (list != null)
                {
                    int perms = PERM_NONE;
                    for (Object[] row : list)
                    {
                        String line = row[0].toString();
                        if (StringUtils.contains(line, uNameStr) &&
                            StringUtils.contains(line, dbName))
                        {
                            if (StringUtils.containsIgnoreCase(line, "GRANT ALL"))
                            {
                                return PERM_ALL;
                            }
                            
                            String pStr = line;
                            int eInx = pStr.indexOf(" ON ");
                            if (eInx > -1)
                            {
                                pStr = pStr.substring(5, eInx).trim();
                                String[] tokens = StringUtils.split(pStr, ",");
                                for (String tok : tokens)
                                {
                                    perms = addPerm(tok.trim(), perms);
                                }
                            }
                        }
                    }
                    //log.debug("PERMS: "+perms);
                    
                    if (perms == 0 && username.equalsIgnoreCase("root"))
                    {
                        perms = PERM_ALL;
                    }
                    return perms;
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return PERM_NONE;
    }
    
    /**
     * @param username
     * @param dbName
     * @return
     */
    private int getPermissionsFromInfoSchema(final String username, final String dbName)
    {
        BasicSQLUtils.setSkipTrackExceptions(true);
        Statement stmt = null;
        try
        {
            if (connection != null)
            {
                stmt  = connection.createStatement();
                
                Vector<Object[]> list = BasicSQLUtils.query(connection, "SELECT * FROM INFORMATION_SCHEMA.USER_PRIVILEGES");
                if (list != null)
                {
                    int perms = PERM_NONE;
                    for (Object[] row : list)
                    {
                        String[] toks = StringUtils.split(row[0].toString(), "'");
                        if (toks[0].equals(username))
                        {
                            String yesStr = row[3].toString();
                            if (yesStr.equalsIgnoreCase("YES"))
                            {
                                perms = addPerm(row[2].toString(), perms);
                                
                            } else if (list.size() == 1)
                            {
                                perms = getPermissionsUsingGrants(toks[0].toString(), toks[2].toString(), dbName);
                            }
                        }
                    }
                    //log.debug("PERMS: "+perms);
                    
                    if (perms == 0 && username.equalsIgnoreCase("root"))
                    {
                        perms = PERM_ALL;
                    }
                    
                    return perms;
                }
            }
            
        } catch (SQLException ex)
        {
            if (ex.getErrorCode() == 1142)
            {
                return PERM_NO_ACCESS;
            }
            ex.printStackTrace();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            close(stmt);
            BasicSQLUtils.setSkipTrackExceptions(false);
        }
        return PERM_NONE;
    }
    
    /**
     * @param username
     * @param dbName
     * @return
     */
    private int getPermissionsFromUserTable(final String username, final String dbName)
    {
        int perms = PERM_NONE;
        if (connection != null)
        {
            if (username.equalsIgnoreCase("root"))
            {
                return PERM_ALL;
            }
            
            String columns  = "Select_priv, Insert_priv, Update_priv, Delete_priv, Lock_tables_priv, Alter_priv, Create_priv, Drop_priv ";
            
            String pre = "SELECT " + columns + " ";
            // Check permissions for the user against the database
            
            if (StringUtils.isNotEmpty(dbName))
            {
                String sql = pre + " FROM mysql.db WHERE User = ? AND Db = ?";
                perms = getPerms(PERM_LIST, sql, username, dbName);
                
                if (perms == PERM_NO_ACCESS)
                {
                    perms = getPermissionsFromInfoSchema(username, dbName);
                }
            }
            
            if (perms == PERM_NONE)
            {
                // the the global permissions of the user (like 'root')
                String sql = pre + "FROM mysql.user WHERE User = ? AND Host = ?";
                perms = getPerms(PERM_LIST, sql, username, hostName);
            }
                
            return perms;
        }
            
        return PERM_NONE;
    }

    /**
     * @param permList
     * @param sql
     * @param args
     * @return
     */
    private int getPerms(int[] permList, final String sql, final String...args)
    {
        String            yes   = "Y";
        int               perms = PERM_NONE;
        PreparedStatement pStmt = null;
        ResultSet         rs    = null;
        try
        {
            pStmt = connection.prepareStatement(sql);
            for (int i=0;i<args.length;i++)
            {
                pStmt.setString(i+1, args[i]);
            }
            rs = pStmt.executeQuery();
            if (rs.next())
            {
                for (int i=0;i<permList.length;i++)
                {
                    if (rs.getString(i+1).equals(yes))
                    {
                        perms |= permList[i];
                    }
                }
            }
        } catch (SQLException ex)
        {
            if (ex.getErrorCode() == 1142)
            {
                return PERM_NO_ACCESS;
            }
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (pStmt != null) pStmt.close();
                if (rs != null) rs.close();
            } catch (SQLException ex) {}
        }
        return perms;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#getPermissionsForUser(java.lang.String, java.lang.String)
     */
    @Override
    public int getPermissionsForUser(String userName)
    {
        return getPermissionsFromMySQL(userName, null, true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#doesDBHaveTables(java.lang.String)
     */
    @Override
    public boolean doesDBHaveTables()
    {
        try
        {
            for (@SuppressWarnings("unused")Object[] row : BasicSQLUtils.query(connection, "show tables"))
            {
                return true;
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#doesDBHaveTable(java.lang.String)
     */
    @Override
    public boolean doesDBHaveTable(final String tableName)
    {
        try
        {
            return doesDBHaveTable(connection.getCatalog(), tableName);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#doesDBHaveTable(java.lang.String)
     */
    @Override
    public boolean doesDBHaveTable(final String databaseName, final String tableName)
    {
        if (tableName != null)
        {
            PreparedStatement stmt = null;
            ResultSet         rs   = null;
            try
            {
                String sql = "SELECT COUNT(*) FROM information_schema.`TABLES` T WHERE T.TABLE_SCHEMA = ? AND T.TABLE_NAME = ?";
                stmt = connection.prepareStatement(sql);
                if (stmt != null)
                {
                    stmt.setString(1, databaseName);
                    stmt.setString(2, tableName);
                    rs = stmt.executeQuery();
                    if (rs != null && rs.next())
                    {
                        return (rs.getInt(1) > 0);
                    }
                }
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            } finally
            {
                try
                {
                    if (stmt != null) stmt.close();
                    if (rs != null) rs.close();
                } catch (SQLException ex) {}
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#setPermissions(java.lang.String, java.lang.String, int)
     */
    @Override
    public boolean setPermissions(final String username, final String dbName, final int permissions)
    {
       
        if (connection != null)
        {
            if (permissions == PERM_NONE)
            {
                PreparedStatement pStmt = null;
                try
                {
                    pStmt = connection.prepareStatement("DELETE FROM mysql.db WHERE Host=? AND Db=? AND User=?");
                    pStmt.setString(1, hostName);
                    pStmt.setString(2, dbName);
                    pStmt.setString(3, username);
                    int rv = pStmt.executeUpdate();
                    BasicSQLUtils.update(connection, "FLUSH PRIVILEGES");
                    
                    return rv == 1;
                    
                } catch (SQLException ex)
                {
                    UIRegistry.showError("Removing permissions failed.");
                    ex.printStackTrace();
                } finally
                {
                    close(pStmt);
                }
                
            } else
            {
                StringBuilder sb = new StringBuilder("GRANT ");
                appendPerms(sb, permissions);
                sb.append(String.format(" ON %s.* TO '%s'@'%s'", dbName, username, hostName));
                //log.debug(sb.toString());
                
                Statement stmt = null;
                try
                {
                    stmt = connection.createStatement();
                    
                    int rv = stmt.executeUpdate(sb.toString());
                    BasicSQLUtils.update(connection, "FLUSH PRIVILEGES");
                    
                    return rv == 0;
                    
                } catch (SQLException ex)
                {
                    ex.printStackTrace();
                    UIRegistry.showError("Setting permissions failed.");
                    
                } finally
                {
                    close(stmt);
                }
            }
        }
            
        return false;
    }

    /* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#close()
	 */
	@Override
	public boolean close() 
	{
		try
		{
		    if (dbConnection != null)
		    {
		        dbConnection.close();
		        dbConnection = null;
		    }
		    
            if (connection != null)
            {
                connection.close();
                connection = null;
            }
			return true;
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * @param stmt
	 */
	private void close(final Statement stmt)
	{
	    if (stmt != null)
        {
            try
            {
                stmt.close();
            } catch (Exception ex) {}
        }
	}
	
	/**
	 * Appends the MySQL permissions to the StringBuilder
	 * @param sb the StringBuilder
	 * @param permissions the permissions mask
	 */
	protected void appendPerms(final StringBuilder sb, final int permissions)
	{
	    if ((permissions & PERM_ALL) == PERM_ALL)
        {
            sb.append("ALL ");
            
        } else
        {
            if ((permissions & PERM_SELECT) == PERM_SELECT)
            {
                sb.append("SELECT,");
            }
            if ((permissions & PERM_UPDATE) == PERM_UPDATE)
            {
                sb.append("UPDATE,");
            }
            if ((permissions & PERM_DELETE) == PERM_DELETE)
            {
                sb.append("DELETE,");
            }
            if ((permissions & PERM_INSERT) == PERM_INSERT)
            {
                sb.append("INSERT,");
            }
            if ((permissions & PERM_LOCK_TABLES) == PERM_LOCK_TABLES)
            {
                sb.append("LOCK TABLES,");
            }
            
            if ((permissions & PERM_ALTER_TABLE) == PERM_ALTER_TABLE)
            {
                sb.append("ALTER,");
            }
            if ((permissions & PERM_CREATE_TABLE) == PERM_CREATE_TABLE)
            {
                sb.append("CREATE,");
            }
            if ((permissions & PERM_DROP_TABLE) == PERM_DROP_TABLE)
            {
                sb.append("DROP,");
            }
            sb.setLength(sb.length()-1); // chomp comma
        }
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#createUser(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	@Override
	public boolean createUser(final String username, final String password, final String dbName, final int permissions) 
	{
	    Statement stmt = null;
		try
		{
			if (connection != null)
			{
				StringBuilder sb = new StringBuilder("GRANT ");
				appendPerms(sb, permissions);
                sb.append(String.format(" ON %s.* TO '%s'@'%s' IDENTIFIED BY '%s'",dbName, username, hostName, password));
				
                stmt = connection.createStatement();
                //log.debug(sb.toString());
                
                int rv = stmt.executeUpdate(sb.toString());

				return rv == 0;
			}
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
			
		} finally
		{
		    close(stmt);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#removeUser(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean removeUser(final String username, final String password) 
	{
	    Statement stmt = null;
		try
		{
			if (connection != null)
			{
			    stmt = connection.createStatement();
                int rv = stmt.executeUpdate(String.format("DROP USER '%s'@'%s'", username, hostName));
                return rv == 0;
			}
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		} finally
        {
            close(stmt);
        }
		return false;
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#verifyEngineAndCharSet()
     */
    @Override
    public boolean verifyEngineAndCharSet(final String dbName)
    {
        errMsg = null;
        Vector<Object[]> rows = BasicSQLUtils.query(connection, "select ENGINE,TABLE_COLLATION FROM information_schema.tables WHERE table_schema = '"+dbName+"'");
        if (rows != null && rows.size() > 0)
        {
            Object[] row = rows.get(0);
            if (row[0] != null && !row[0].toString().equalsIgnoreCase("InnoDB"))
            {
                errMsg = "The engine is not InnoDB.";
            }
            if (row[1] != null && !StringUtils.contains(row[1].toString(), "utf8"))
            {
                errMsg = (errMsg == null ? "" : errMsg + "\n") + "The character set is not UTF-8."; 
            }
        } else
        {
            errMsg = "Error checking the database engine and character set.";
        }
        return errMsg == null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#getFieldLength(java.lang.String, java.lang.String)
     */
    @Override
    public Integer getFieldLength(String tableName, String fieldName)
    {
        try
        {
            String sql = "SELECT CHARACTER_MAXIMUM_LENGTH FROM `information_schema`.`COLUMNS` where TABLE_SCHEMA = '" +
                          connection.getCatalog() + "' and TABLE_NAME = '" + tableName + "' and COLUMN_NAME = '" + fieldName + "'";
            //log.debug(sql);
            
            Vector<Object> rows = BasicSQLUtils.querySingleCol(connection, sql);                    
            if (rows.size() == 0)
            {
                return null; //the field doesn't even exits
            }
            
            return((Number )rows.get(0)).intValue();
            
        } catch (Exception ex)
        {
            errMsg = "Error getting field length";
        }
        return null;
    }
 
    
    
}
