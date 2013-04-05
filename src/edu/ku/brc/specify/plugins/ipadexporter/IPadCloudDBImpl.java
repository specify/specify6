/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.plugins.ipadexporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 9, 2012
 *
 */
public class IPadCloudDBImpl implements IPadCloudIFace
{
    private String  loginKey;
    private boolean isLoggedIn = false;
    
    private Integer userID     = null;
    
    // For local Testing
    private Connection connection;
    
    /**
     * 
     */
    public IPadCloudDBImpl()
    {
        super();
        
        connection = DBConnection.getInstance().createConnection();
        try
        {
            connection.setCatalog("spinsight");
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#isLoggedIn()
     */
    @Override
    public boolean isLoggedIn()
    {
        return isLoggedIn && userID != null && StringUtils.isNotEmpty(loginKey);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#isUserNameOK(java.lang.String)
     */
    @Override
    public boolean isUserNameOK(final String usrName)
    {
        PreparedStatement pStmt = null;
        boolean isOK = false;
        try
        {
            String sql = "SELECT COUNT(UserID) FROM users WHERE Username = ?";
            pStmt = connection.prepareStatement(sql);
            pStmt.setString(1, usrName);

            ResultSet rs = pStmt.executeQuery();
            if (rs.next())
            {
                isOK = rs.getInt(1) == 1;
            }
            rs.close();
            pStmt.close();
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return isOK;

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#login(java.lang.String, java.lang.String)
     */
    @Override
    public boolean login(final String usrName, final String pwd)
    {
        userID     = null;
        isLoggedIn = false;
        
        //System.out.println("["+pwd+"] "+org.apache.commons.codec.digest.DigestUtils.shaHex(pwd));
        String md5Pwd = org.apache.commons.codec.digest.DigestUtils.shaHex(pwd);
        PreparedStatement pStmt = null;
        try
        {
            String sql = "SELECT UserID FROM users WHERE Username=? AND Password=?";
            pStmt = connection.prepareStatement(sql);
            pStmt.setString(1, usrName);
            pStmt.setString(2, md5Pwd);

            ResultSet rs = pStmt.executeQuery();
            if (rs.next())
            {
                userID     = rs.getInt(1);
                isLoggedIn = true;
                pStmt.close();

                sql = "UPDATE users SET LastLoginTimestamp = ? WHERE UserID = ?";
                pStmt = connection.prepareStatement(sql);
                pStmt.setDate(1, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
                pStmt.setInt(2, userID);
                boolean isOK = pStmt.executeUpdate() == 1;
            }
            rs.close();
            pStmt.close();
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
            
        return userID != null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.IPadCloudIFace#doesDataSetExist(java.lang.String, java.lang.String)
     */
    @Override
    public boolean doesDataSetExist(final String dsName, final String website)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.IPadCloudIFace#removeDataSet(java.lang.String, java.lang.String)
     */
    @Override
    public boolean removeDataSet(String dsName, String website)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#logout()
     */
    @Override
    public boolean logout()
    {
        userID     = null;
        isLoggedIn = false;

        return true;
    }
    
    /**
     * @param tblName
     * @param idName
     * @param fieldName
     * @param value
     * @return
     */
    private Integer getId(final String tblName, final String idName, final String fieldName, final String value)
    {
        PreparedStatement pStmt = null;
        Integer id = null;
        try
        {
            String sql = String.format("SELECT %s FROM %s WHERE %s = ?", idName, tblName, fieldName);
            pStmt = connection.prepareStatement(sql);
            pStmt.setString(1, value);

            ResultSet rs = pStmt.executeQuery();
            if (rs.next())
            {
                id = rs.getInt(1);
            }
            rs.close();
            pStmt.close();
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return id;
    }
    
    /**
     * @param usrName
     * @param dataSetName
     * @return
     */
    private Pair<Integer, Integer> getUserIdDataSetId(final String usrName, final String dataSetName)
    {
        Pair<Integer, Integer> p = null;
            
        Integer usrId = getId("users", "UserID", "Username", usrName);
        if (usrId != null)
        {
            Integer dsId = getId("datasets", "DataSetID", "Name", dataSetName);
            if (dsId != null)
            {
                p = new Pair<Integer, Integer>(usrId, dsId);
            }
        }
        return p;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#addUserAccessToDataSet(java.lang.String, java.lang.String)
     */
    @Override
    public boolean addUserAccessToDataSet(final String usrName, final String dataSetName)
    {
        Pair<Integer, Integer> idPair = getUserIdDataSetId(usrName, dataSetName);
        boolean isOK = false;
        if (idPair != null)
        {
            isOK = assignAccess(connection, idPair.first, idPair.second);
        }
        return isOK;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#addDataSetToUser(java.lang.String, java.lang.String)
     */
    @Override
    public boolean addDataSetToUser(final String usrName, final String dataSetName)
    {
        Pair<Integer, Integer> idPair = getUserIdDataSetId(usrName, dataSetName);
        boolean isOK = false;
        if (idPair != null)
        {
            isOK = assignOwner(connection, idPair.first, idPair.second);
        }
        return isOK;
    }
    
    /**
     * @param usrName
     * @param dataSetName
     * @return
     */
    private boolean removeUserFromRelationship(final String tblName, final String usrName, final String dataSetName)
    {
        Pair<Integer, Integer> idPair = getUserIdDataSetId(usrName, dataSetName);
        boolean isOK = false;
        if (idPair != null)
        {
            String sql = String.format("DELETE FROM %s WHERE UserID = %d and DataSetID = %d", tblName, idPair.first, idPair.second);
            isOK = BasicSQLUtils.update(connection, sql) == 1;
        }
        return isOK;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#removeUserAccessFromDataSet(java.lang.String, java.lang.String)
     */
    @Override
    public boolean removeUserAccessFromDataSet(final String usrName, final String dataSetName)
    {
        return removeUserFromRelationship("access", usrName, dataSetName);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#removeDataSetFromUser(java.lang.String, java.lang.String)
     */
    @Override
    public boolean removeDataSetFromUser(final String usrName, final String dataSetName)
    {
        return removeUserFromRelationship("owner", usrName, dataSetName);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.IPadCloudIFace#addNewDataSet(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, java.lang.String)
     */
    @Override
    public boolean addNewDataSet(final String dsName, 
                                 final String dirName,
                                 final String inst,
                                 final String div,
                                 final String disp,
                                 final String coll,
                                 final Boolean isGlobal,
                                 final String iconName,
                                 final String curator)
    {
        return createDataSet(connection, dsName, dirName, inst, div, disp, coll, false) != -1;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#addNewUser(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean addNewUser(final String usrName, 
                              final String pwd, 
                              final String inst)
    {
        return createUser(connection, usrName, pwd, inst) != -1;
    }
    
    
    /**
     * @param tblName
     * @param idName
     * @param fieldName
     * @param val
     * @return
     */
    private boolean updateValue(final String tblName, final String idName, final String fieldName, final int recId, final Object value)
    {
        boolean isOK = false;
        PreparedStatement pStmt = null;
        try
        {
            String sql = String.format("UPDATE %s SET %s=? WHERE %s = %d", tblName, fieldName, idName, recId);
            pStmt = connection.prepareStatement(sql);
            
            if (value instanceof Integer)
            {
                pStmt.setInt(1, (Integer)value);
                
            } else if (value instanceof java.sql.Date)
            {
                pStmt.setDate(1, (java.sql.Date)value);
                
            } if (value instanceof Boolean)
            {
                pStmt.setBoolean(1, (Boolean)value);
                
            } else if (value == null)
            {
                pStmt.setObject(1, null);
            } else
            {
                System.err.println("Unrecognized type: "+value.getClass());
            }

            isOK = pStmt.executeUpdate(sql) == 1;
            pStmt.close();
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return isOK;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#makeDataSetGlobal(java.lang.String, boolean)
     */
    @Override
    public boolean makeDataSetGlobal(final String dataSetName, final boolean isGlobal)
    {
        Integer id = getId("datasets", "DataSetID", "Name", dataSetName);
        if (id != null)
        {
            return updateValue("datasets", "DataSetID", "IsGlobal", id, isGlobal);
        }

        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#removeAccount()
     */
    @Override
    public boolean removeAccount()
    {
        String sql = String.format("DELETE FROM access WHERE UserID = %d", userID);
        Boolean isOK = BasicSQLUtils.update(connection, sql) == 1;
        if (!isOK) return false;
        
        sql  = String.format("DELETE FROM owner WHERE UserID = %d", userID);
        isOK = BasicSQLUtils.update(connection, sql) == 1;
        if (!isOK) return false;
        
        Vector<DataSetInfo> items = getOwnerList(userID);
        if (items != null && items.size() > 0)
        {
            for (DataSetInfo dsi : items)
            {
                sql = String.format("DELETE FROM dataset WHERE DataSetID = %d", dsi.getId());
                isOK = BasicSQLUtils.update(connection, sql) == 1;
                if (!isOK) return false;
            }
        }
        sql = String.format("DELETE FROM users WHERE UserID = %d", userID);
        isOK = BasicSQLUtils.update(connection, sql) == 1;
        if (!isOK) return false;
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#setPassword(java.lang.String)
     */
    @Override
    public boolean setPassword(final String newPwd)
    {
        String md5Pwd = org.apache.commons.codec.digest.DigestUtils.shaHex(newPwd);

        return updateValue("users", "UserID", "Password", userID, md5Pwd);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#sendPwdReminder()
     */
    @Override
    public boolean sendPwdReminder()
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.iPadCloudIFace#getOwnerList()
     */
    @Override
    public Vector<DataSetInfo> getOwnerList()
    {
        return getOwnerList(userID);
    }
    
    /**
     * @param userId
     * @return
     */
    private Vector<DataSetInfo> getOwnerList(final int userId)
    {
        Vector<DataSetInfo> dsList = new Vector<DataSetInfo>();
        String sql = "SELECT ds.DataSetID, ds.Name, ds.Institution, ds.Division, ds.Discipline, ds.Collection, ds.IsGlobal FROM users u INNER JOIN owner o ON u.UserID = o.UserID " +
                     "INNER JOIN datasets ds ON o.DataSetID = ds.DataSetID WHERE u.UserID = " + userId;
        Statement stmt;
        try
        {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                DataSetInfo dsi = new DataSetInfo(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getBoolean(7));
                dsList.add(dsi);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return dsList;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.IPadCloudIFace#getAccessList(java.lang.String)
     */
    @Override
    public List<String> getAccessList(final String dataSetName)
    {
        Vector<String> usrList = new Vector<String>();
        String sql = "SELECT Username FROM users u INNER JOIN access a ON u.UserID = a.UserID " +
                     "INNER JOIN datasets ds ON a.DataSetID = ds.DataSetID WHERE ds.Name = ?";
        PreparedStatement pStmt;
        try
        {
            pStmt = connection.prepareStatement(sql);
            pStmt.setString(1, dataSetName);
            
            ResultSet rs = pStmt.executeQuery();
            while (rs.next())
            {
                usrList.add(rs.getString(1));
            }
            rs.close();
            pStmt.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
            
        return usrList;
    }

    /**
     * @param conn
     * @param usrName
     * @param pwd
     * @param inst
     * @return
     */
    private int createUser(final Connection conn, 
                          final String usrName, 
                          final String pwd, 
                          final String inst)
    {
        PreparedStatement pStmt = null;
        try
        {
            String md5Pwd = org.apache.commons.codec.digest.DigestUtils.shaHex(pwd);
            
            String sql = "INSERT INTO users (Username, Password, Institution, CreatedTimestamp, ModifiedTimestamp, LastLoginTimestamp) " +
                         " VALUES(?,?,?,?,?,?)";
            pStmt = conn.prepareStatement(sql);
            pStmt.setString(1, usrName);
            pStmt.setString(2, md5Pwd);
            pStmt.setString(3, inst);
            
            java.sql.Date d = new java.sql.Date(Calendar.getInstance().getTime().getTime());
            pStmt.setDate(4, d);
            pStmt.setDate(5, d);
            pStmt.setDate(6, null);
            
            if (pStmt.executeUpdate() != 1)
            {
                return -1;
            }
            return BasicSQLUtils.getInsertedId(pStmt);
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return -1;
    }
    
    /**
     * @param conn
     * @param dsName
     * @param inst
     * @param div
     * @param disp
     * @param coll
     * @param isGlobal
     * @return
     */
    private int createDataSet(final Connection conn, 
                              final String dsName, 
                              final String dirName, 
                              final String inst,
                              final String div,
                              final String disp,
                              final String coll,
                              final boolean isGlobal)
    {
        PreparedStatement pStmt = null;
        try
        {
            String sql = "INSERT INTO datasets (Name, DirName, Institution, Division, Discipline, Collection, IsGlobal, CreatedTimestamp, ModifiedTimestamp) " +
                         " VALUES(?,?,?,?,?,?,?,?,?)";
            pStmt = conn.prepareStatement(sql);
            pStmt.setString(1, dsName);
            pStmt.setString(2, dirName);
            pStmt.setString(3, inst);
            pStmt.setString(4, div);
            pStmt.setString(5, disp);
            pStmt.setString(6, coll);
            pStmt.setBoolean(7, isGlobal);
            
            java.sql.Date d = new java.sql.Date(Calendar.getInstance().getTime().getTime());
            pStmt.setDate(8, d);
            pStmt.setDate(9, d);
            
            if (pStmt.executeUpdate() != 1)
            {
                return -1;
            }
            return BasicSQLUtils.getInsertedId(pStmt);
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return -1;
    }        
    
    /**
     * @param userId
     * @param dsId
     * @param tblName
     * @return
     */
    private boolean assign(final Connection conn, final int userId, final int dsId, final String tblName)
    {
        PreparedStatement pStmt = null;
        try
        {
            String sql = "INSERT INTO " + tblName + " (UserId, DataSetID) VALUES(?,?)";
            pStmt = conn.prepareStatement(sql);
            pStmt.setInt(1, userId);
            pStmt.setInt(2, dsId);
            
            if (pStmt.executeUpdate() == 1)
            {
                return true;
            }
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * @param conn
     * @param userId
     * @param dsId
     * @return
     */
    private boolean assignOwner(final Connection conn, final int userId, final int dsId)
    {
        return assign(conn, userId, dsId, "owner");
    }
    

        
    /**
     * @param conn
     * @param userId
     * @param dsId
     * @return
     */
    private boolean assignAccess(final Connection conn, final int userId, final int dsId)
    {
        return assign(conn, userId, dsId, "access");
    }
    

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.IPadCloudIFace#getInstId(java.lang.String)
     */
    @Override
    public Integer getInstId(String name)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.IPadCloudIFace#getInstitutionInfo(int)
     */
    @Override
    public HashMap<String, Object> getInstitutionInfo(int instId)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.IPadCloudIFace#saveInstitutionInfo(java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Integer saveInstitutionInfo(Integer instId, String name, String uri, String code, String guid)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.IPadCloudIFace#getInstList()
     */
    @Override
    public List<String> getInstList()
    {
        return null;
    }

    /**
     * 
     */
    public void createTestData()
    {
        Connection conn = DBConnection.getInstance().getConnection();
        try
        {
            String catName = conn.getCatalog();
            conn.setCatalog("spinsight");
            
            String[] userData = {"rods", "tim", "ben", "jim"};
            String[] instData = {"KU", "KU", "ISU", "MSU"};
            int[]    userIds  = new int[userData.length];
            for (int i=0;i<userData.length;i++)
            {
                userIds[i] = createUser(conn, userData[i], "XXX", instData[i]);
            }
            
            String[] dsNames    = {"AAA", "BBB", "CCC", "DDD"};
            String[] dsDirs     = {"XAAA", "XBBB", "XCCC", "XDDD"};
            String[] instData2  = {"KU", "KU", "ISU", "MSU"};
            String[] divData   = {"Fish", "Bugs", "Birds", "Fossils"};
            String[] dispData  = {"Icthy", "Ento", "Orno", "Paleo"};
            String[] collData  = {"KU Icthy", "KU Ento", "ISU Orno", "MSU Paleo"};
            boolean[] isGlob   = {true, false, false, true};
            int[]    dsIds     = new int[dsNames.length];
            for (int i=0;i<dsNames.length;i++)
            {
                dsIds[i] = createDataSet(conn, dsNames[i], dsDirs[i], instData2[i], divData[i], dispData[i], collData[i], isGlob[i]);
            }
            
            assignOwner(conn, userIds[0], dsIds[0]);
            assignOwner(conn, userIds[1], dsIds[1]);
            assignOwner(conn, userIds[2], dsIds[2]);
            assignOwner(conn, userIds[3], dsIds[3]);
            
            assignAccess(conn, userIds[0], dsIds[3]);
            assignAccess(conn, userIds[1], dsIds[0]);
            assignAccess(conn, userIds[3], dsIds[0]);
            assignAccess(conn, userIds[3], dsIds[1]);
            assignAccess(conn, userIds[3], dsIds[2]);
        
            conn.setCatalog(catName);
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

}
