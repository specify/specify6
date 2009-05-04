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

import java.sql.Connection;
import java.sql.Statement;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.conversion.BasicSQLUtils;


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
        
        String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, databaseHost,  dbName);
        if (connStr == null)
        {
            connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHost,  dbName);
        }
        
        dbConnection   = new DBConnection(itUsernameArg, 
                                          itPasswordArg, 
                                          connStr, 
                                          driverInfo.getDriverClassName(), 
                                          driverInfo.getDialectClassName(), dbName);
        if (connection != null)
        {
            connection = dbConnection.createConnection();
        }
        return connection != null;
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
                    log.debug(sql);
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
                int rv = BasicSQLUtils.update(connection, "DROP DATABASE "+dbName);
                
                return rv == 0;
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
        try
        {
            if (connection != null)
            {
                int rv = BasicSQLUtils.update(connection, "DROP USER "+username);
                
                return rv == 0;
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
        try
        {
            for (Object[] row : BasicSQLUtils.query(connection, "show databases"))
            {
                if (dbName.equals(row[0].toString()))
                {
                    return true;
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#getPermissions(java.lang.String, java.lang.String)
     */
    @Override
    public int getPermissions(final String username, final String dbName)
    {
        Statement stmt = null;
        try
        {
            if (connection != null)
            {
                stmt = connection.createStatement();
                String sql = String.format("SHOW GRANTS FOR '%s'@'%s'", username, hostName);
                log.debug(sql);
                Vector<Object[]> list = BasicSQLUtils.query(connection, sql);
                if (list != null)
                {
                    int perms = PERM_NONE;
                    for (Object[] row : list)
                    {
                        if (StringUtils.contains(row[0].toString(), dbName))
                        {
                            String[] tokens = StringUtils.split(row[0].toString(), ", ");
                            int inx = 1;
                            while (!tokens[inx].equals("ON"))
                            {
                                if (tokens[inx].equals("SELECT"))
                                {
                                    perms |= PERM_SELECT;
                                } else if (tokens[inx].equals("UPDATE"))
                                {
                                    perms |= PERM_UPDATE;
                                } else if (tokens[inx].equals("DELETE"))
                                {
                                    perms |= PERM_DELETE;
                                } else if (tokens[inx].equals("INSERT"))
                                {
                                    perms |= PERM_INSERT;
                                }
                                inx++;
                            }
                        }
                    }
                    log.debug("PERMS: "+perms);
                    return perms;
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            close(stmt);
        }
        return PERM_NONE;
    }

    

	/* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#doesDBHaveTables(java.lang.String)
     */
    @Override
    public boolean doesDBHaveTables()
    {
        try
        {
            for (@SuppressWarnings("unused")
            Object[] row : BasicSQLUtils.query(connection, "show tables"))
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
     * @see edu.ku.brc.dbsupport.DBMSUserMgr#setPermissions(java.lang.String, java.lang.String, int)
     */
    @Override
    public boolean setPermissions(String username, String dbName, int permissions)
    {
        Statement stmt = null;
        try
        {
            if (connection != null)
            {
                StringBuilder sb = new StringBuilder("GRANT ");
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
                    sb.setLength(sb.length()-1); // chomp comma
                }
                sb.append(String.format(" ON %s.* TO '%s'@'%s'",dbName, username, hostName));
                
                stmt = connection.createStatement();
                log.debug(sb.toString());
                
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
	 * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#close()
	 */
	@Override
	public boolean close() 
	{
		try
		{
			dbConnection.close();
			dbConnection = null;
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
                    sb.setLength(sb.length()-1); // chomp comma
                }
                sb.append(String.format(" ON %s.* TO '%s'@'%s' IDENTIFIED BY '%s'",dbName, username, hostName, password));
				
                stmt = connection.createStatement();
                log.debug(sb.toString());
                
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
}
