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
package edu.ku.brc.specify.plugins.sgr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Stack;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.specify.conversion.TimeLogger;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 27, 2010
 *
 */
public class GroupHashDAO
{
    //private static final String sqlBase = "SELECT ID, mon, cnt, RawID FROM group_hash INNER JOIN group_hash_ids ON ID = GrpID WHERE ";
    
    /*private static final DataIndexType[] indexes = {DataIndexType.eCollector_num, DataIndexType.eInstitution_code, DataIndexType.eCollection_code, 
        DataIndexType.eCatalogue_number, DataIndexType.eAuthor, DataIndexType.eFamily, DataIndexType.eGenus, DataIndexType.eSpecies, 
        DataIndexType.eSubspecies, DataIndexType.eLatitude, DataIndexType.eLongitude, DataIndexType.eMax_altitude, DataIndexType.eMin_altitude, 
        DataIndexType.eCountry, DataIndexType.eState_province, DataIndexType.eCounty, DataIndexType.eCollector_name, DataIndexType.eLocality, 
        DataIndexType.eYear, DataIndexType.eMonth, DataIndexType.eDay};
    
    private static final String rawSQL = "SELECT collector_num, institution_code, collection_code, catalogue_number, author, " +
                                         "family, genus, species, subspecies, latitude, longitude, max_altitude, min_altitude, " +
                                         "country, state_province, county, collector_name, locality, year, month, day " +
                                         "FROM raw2 WHERE ID IN (%s)";
    */
    private static final String sqlBase = "SELECT g.ID, g.mon, g.cnt, gi.RawID FROM group_hash g INNER JOIN group_hash_ids gi ON g.ID = gi.GrpID WHERE ";
    
    private static GroupHashDAO   instance = new GroupHashDAO();
    private static Stack<RawData> recycler = new Stack<RawData>();
    
    private DBConnection      dbConn;
    private Connection        connection   = null;
    private PreparedStatement pStmtGrp     = null;
    private PreparedStatement pStmtGrpMon  = null;
    
    private PreparedStatement pStmtSNIBGrp     = null;
    private PreparedStatement pStmtSNIBGrpMon  = null;

    /**
     * 
     */
    private GroupHashDAO()
    {
        super();
        
        String server   = "localhost";
        String database = "plants";
        String username = "root";
        String password = "root";
        
        DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
        String             connStr    = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, server, database, 
                                                                    username, password, driverInfo.getName());
        boolean isFirst = true;
        do 
        {
            if (!isFirst)
            {
                password = JOptionPane.showInputDialog("Enter MySQL's root password:");
            }
            if (StringUtils.isNotEmpty(password))
            {
                try
                {
                    dbConn     = new DBConnection("root", password, connStr, driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), database);
                    connection = dbConn.getConnection();
                } catch (Exception ex) {}
            }
            isFirst = false;
        } while (dbConn == null || connection == null);
        
        
    }
    
    /**
     * @return the instance
     */
    public static GroupHashDAO getInstance()
    {
        if (instance == null)
        {
            instance = new GroupHashDAO();
        }
        return instance;
    }

    /**
     * @return the connection
     */
    public Connection getConnection()
    {
        return connection;
    }

    /**
     * @param list
     */
    public void recycle(final List<RawData> list)
    {
        for (RawData rd : list)
        {
            rd.clear();
        }
        recycler.addAll(list);
    }
    
    /**
     * @throws SQLException
     */
    private void verifyPrepStmts() throws SQLException
    {
        if (pStmtGrp == null)
        {
            String fromClause = "collnum = ? AND genus = ? AND year = ?";
            pStmtGrp = connection.prepareStatement(sqlBase + fromClause);
        }
        if (pStmtGrpMon == null)
        {
            String fromClause = "collnum IS NULL AND genus = ? AND year = ? AND mon = ?";
            pStmtGrpMon = connection.prepareStatement(sqlBase + fromClause);
        }
        
        String sql = "SELECT IdSNIB, `Month` FROM angiospermas WHERE ";
        if (pStmtSNIBGrp == null)
        {
            String fromClause = "CollectorNumber = ? AND Genus = ? AND `Year` = ?";
            pStmtSNIBGrp = connection.prepareStatement(sql + fromClause);
        }
        
        if (pStmtSNIBGrpMon == null)
        {
            String fromClause = "CollectorNumber IS NULL AND Genus = ? AND `Year` = ? AND `Month` = ?";
            pStmtSNIBGrpMon = connection.prepareStatement(sql + fromClause);
        }
    }
    
    /**
     * @param collNum
     * @param genus
     * @param year
     * @param month
     * @return
     */
    public GroupingColObjData getGroupingData(final String collNum,
                                              final String genus,
                                              final String year,
                                              final String month)
    {
        GroupingColObjData grpData = null;
        try
        {
            verifyPrepStmts();
            
            pStmtGrp.setString(1, collNum);
            pStmtGrp.setString(2, genus);
            pStmtGrp.setString(3, year);
            
            System.err.println(">"+pStmtGrp.toString());
            int cnt = 0;
            
            TimeLogger tmLogger = new TimeLogger("Fetching Groups");
            ResultSet  rs       = pStmtGrp.executeQuery();
            while (rs.next())
            {
                cnt++;
                if (grpData == null)
                {
                    grpData = new GroupingColObjData(rs.getInt(1), collNum, genus, year, rs.getString(2), rs.getInt(3));
                    //System.err.println("1*: "+rs.getInt(1)+", "+collNum+", "+genus+", "+year+", "+rs.getString(2)+", "+rs.getInt(3));
                    //System.err.println("2*:     "+rs.getInt(4));
                }
                grpData.addRawId(rs.getInt(4));
            }
            rs.close();
            tmLogger.end();
            
            if (cnt > 0)
            {
                //System.err.println("Count: "+cnt+" "+collNum+" "+genus+" "+year);
            }
            
            if (StringUtils.isNotEmpty(month))
            {
                pStmtGrpMon.setString(1, genus);
                pStmtGrpMon.setString(2, year);
                pStmtGrpMon.setString(3, month);
                
                cnt= 0;
                rs = pStmtGrpMon.executeQuery();
                while (rs.next())
                {
                    cnt++;
                    if (grpData == null)
                    {
                        grpData = new GroupingColObjData(rs.getInt(1), collNum, genus, year, rs.getString(2), rs.getInt(3));
                        System.err.println("2*: "+rs.getInt(1)+", "+collNum+", "+genus+", "+year+", "+rs.getString(2)+", "+rs.getInt(3));
                    }
                    grpData.addRawId(rs.getInt(4));
                    System.err.println("2*:     "+rs.getInt(4));
                }
                rs.close();
                
                if (cnt > 0)
                {
                    System.err.println("Count: "+cnt+" "+genus+" "+year+" "+month);
                }
            }

            return grpData;
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * @param collNum
     * @param genus
     * @param year
     * @param month
     * @return
     */
    public GroupingColObjData getSNIBData(final String collNum,
                                          final String genus,
                                          final String year,
                                          final String month)
    {
        GroupingColObjData grpData = null;
        try
        {
            verifyPrepStmts();
            
            pStmtSNIBGrp.setString(1, collNum);
            pStmtSNIBGrp.setString(2, genus);
            pStmtSNIBGrp.setString(3, year);
            
            System.err.println(">"+pStmtSNIBGrp.toString());
            
            int        cnt      = 0;
            TimeLogger tmLogger = new TimeLogger("Fetching Groups");
            ResultSet  rs       = pStmtSNIBGrp.executeQuery();
            while (rs.next())
            {
                cnt++;
                if (grpData == null)
                {
                    grpData = new GroupingColObjData(null, collNum, genus, year, rs.getString(2), 0);
                }
                grpData.addRawId(rs.getInt(1));
            }

            rs.close();
            tmLogger.end();
            
            if (StringUtils.isNotEmpty(month))
            {
                pStmtSNIBGrpMon.setString(1, genus);
                pStmtSNIBGrpMon.setString(2, year);
                pStmtSNIBGrpMon.setString(3, month);
                
                rs = pStmtSNIBGrpMon.executeQuery();
                while (rs.next())
                {
                    cnt++;
                    if (grpData == null)
                    {
                        grpData = new GroupingColObjData(null, collNum, genus, year, rs.getString(2), 0);
                    }
                    grpData.addRawId(rs.getInt(1));
                }
                rs.close();
                
                if (cnt > 0)
                {
                    grpData.setCnt(cnt);
                }
            }

            return grpData;
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;
    }
    

    
    /**
     * @param collNum
     * @param genus
     * @param year
     * @param month
     * @return
     */
    /*public List<RawData> getItems(final List<Integer> rawIds)
    {
        Vector<RawData> items = new Vector<RawData>();
        
        if (rawIds.size() > 0)
        {
            sb.setLength(0);
            for (Integer id : rawIds)
            {
                if (sb.length() > 0) sb.append(',');
                sb.append(id);
            }
            
            Statement stmt = null;
            try
            {
                stmt = connection.createStatement();
                
                String sql = String.format(rawSQL, sb.toString());
                
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next())
                {
                    RawData rdo   = getRawDataObj();
                    int     index = 1;
                    for (DataIndexType d : indexes)
                    {
                        rdo.setData(d, rs.getString(index));
                        index++;
                    }
                    items.add(rdo);
                }
                rs.close();
                
            } catch (SQLException e)
            {
                e.printStackTrace();
            } finally
            {
                try
                {
                    if (stmt != null) stmt.close();
                } catch (Exception ex) {}
            }
        }
        return items;
    }*/
    
    /**
     * @return a new or recycled data object
     */
    public RawData getRawDataObj()
    {
        if (recycler.size() == 0)
        {
            return new RawData();
        }
        RawData rd = recycler.pop();
        rd.clear();
        return rd;
    }
    
    /**
     * 
     */
    public void cleanUp()
    {
        try
        {
            if (pStmtGrp != null) pStmtGrp.close();
            if (pStmtGrpMon != null) pStmtGrpMon.close();
            pStmtGrp     = null;
            pStmtGrpMon  = null;
            
            if (pStmtSNIBGrp != null) pStmtSNIBGrp.close();
            if (pStmtSNIBGrpMon != null) pStmtSNIBGrpMon.close();
            pStmtSNIBGrp     = null;
            pStmtSNIBGrpMon  = null;
            
            if (dbConn != null) dbConn.close();
            
            dbConn       = null;
            instance     = null;
            
        } catch (Exception ex) {}
    }
}
