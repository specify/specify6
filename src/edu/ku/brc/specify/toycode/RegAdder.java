/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.toycode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Dec 14, 2009
 *HashMap<String, String> mv
 */
public class RegAdder
{
    protected static final Logger                           log                    = Logger.getLogger(RegAdder.class);
            
    private static final int STR_SIZE = 128;
    
    private Timestamp        ts     = new Timestamp(Calendar.getInstance().getTime().getTime());
    
    private int              cnt = 0;
    private int              lineNo = 0;
    
    //private Connection       connection;
    private Statement        stmt = null;
    
    private PreparedStatement trkStmt1;
    private PreparedStatement trkStmt2;
    private PreparedStatement trkStmt3;
    private PreparedStatement trkStmt4;

    private PreparedStatement regStmt1;
    private PreparedStatement regStmt2;

    
    /**
     * 
     */
    public RegAdder(final Connection connection)
    {
        super();

        try
        {
            stmt = connection.createStatement();
            
            trkStmt1  = connection.prepareStatement("INSERT INTO track (TimestampCreated, Id, CountAmt) VALUES(?, ?, ?)");
            trkStmt2  = connection.prepareStatement("INSERT INTO trackitem (Name, CountAmt, Value, TrackID) VALUES(?, ?, ?, ?)");
            trkStmt3  = connection.prepareStatement("UPDATE trackitem SET CountAmt=?, Value=? WHERE TrackItemID=?");
            trkStmt4  = connection.prepareStatement("UPDATE track SET CountAmt=? WHERE TrackID=?");
            
            regStmt1  = connection.prepareStatement("INSERT INTO register (TimestampCreated, RegNumber, RegType) VALUES(?, ?, ?)");
            regStmt2  = connection.prepareStatement("INSERT INTO registeritem (Name, CountAmt, Value, RegisterID) VALUES(?, ?, ?, ?)");
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }

    }
    
    /**
     * @param trackId
     * @param mv
     * @param pStmt
     * @throws SQLException
     */
    private void doTrackInserts(final int trackId, final HashMap<String, String> mv, final PreparedStatement pStmt) throws SQLException
    {
        for (String key : mv.keySet())
        {
            String value = mv.get(key);
            pStmt.setString(1, key);
            if (!StringUtils.contains(value, ".") && StringUtils.isNumeric(value) && value.length() < 10)
            {
                pStmt.setInt(2, Integer.parseInt(value));
                pStmt.setNull(3, java.sql.Types.VARCHAR);
                
            } else if (value.length() < STR_SIZE+1)
            {
                pStmt.setNull(2, java.sql.Types.INTEGER);
                pStmt.setString(3, value);
                
            } else
            {
                String v = value.substring(0, STR_SIZE);
                System.err.println("Error - On line "+lineNo+" Value["+value+"] too big trunccating to["+v+"]");
                
                pStmt.setNull(2, java.sql.Types.INTEGER);
                pStmt.setString(3, v);
            }
            pStmt.setInt(4, trackId);
            
            //System.out.println(pStmt2.toString());
            
            int rv = pStmt.executeUpdate();
            if (rv != 1)
            {
                for (String k : mv.keySet())
                {
                    System.out.println("["+k+"]["+mv.get(k)+"]");
                }
                System.err.println("------------------------ Line No: " + lineNo);
                throw new RuntimeException("Error insert trackitem for Id: "+trackId);
            }
        }
    }
    
    /**
     * @param trackItemId
     * @param value
     * @param pStmt
     */
    private void doTrackUpdate(final int trackItemId, final String value, final PreparedStatement pStmt) throws SQLException
    {
        if (!StringUtils.contains(value, ".") && StringUtils.isNumeric(value) && value.length() < 10)
        {
            pStmt.setInt(1, Integer.parseInt(value));
            pStmt.setNull(2, java.sql.Types.VARCHAR);
            
        } else if (value.length() < STR_SIZE+1)
        {
            pStmt.setNull(1, java.sql.Types.INTEGER);
            pStmt.setString(2, value);
            
        } else
        {
            String v = value.substring(0, STR_SIZE);
            System.err.println("Error - On line "+lineNo+" Value["+value+"] too big trunccating to["+v+"]");
            
            pStmt.setNull(1, java.sql.Types.INTEGER);
            pStmt.setString(2, v);
        }
        pStmt.setInt(3, trackItemId);
        
        int rv = pStmt.executeUpdate();
        if (rv != 1)
        {
            throw new RuntimeException("Error insert trackitem for Id: "+trackItemId);
        }
    }
    
    /**
     * @param mv
     */
    private void insertTrack(final HashMap<String, String> mv)
    {
        if (mv.size() > 0)
        {
            /*
            +------------------+-------------+------+-----+---------+----------------+
            | Field            | Type        | Null | Key | Default | Extra          |
            +------------------+-------------+------+-----+---------+----------------+
            | TrackID          | int(11)     | NO   | PRI | NULL    | auto_increment | 
            | TimestampCreated | datetime    | NO   |     | NULL    |                | 
            | Id               | varchar(64) | YES  |     | NULL    |                | 
            | CountAmt         | int(11)     | YES  |     | NULL    |                | 
            +------------------+-------------+------+-----+---------+----------------+
            4 rows in set (0.00 sec)

            mysql> describe trackitem;
            +-------------+-------------+------+-----+---------+----------------+
            | Field       | Type        | Null | Key | Default | Extra          |
            +-------------+-------------+------+-----+---------+----------------+
            | TrackItemID | int(11)     | NO   | PRI | NULL    | auto_increment | 
            | Name        | varchar(64) | NO   |     | NULL    |                | 
            | CountAmt    | int(11)     | YES  |     | NULL    |                | 
            | Value       | varchar(64) | YES  |     | NULL    |                | 
            | TrackID     | int(11)     | NO   | MUL | NULL    |                | 
            +-------------+-------------+------+-----+---------+----------------+
            */
            try
            {
                
                String id = mv.get("id");
                String ip = mv.get("IP");
                
                if (StringUtils.isNotEmpty(id) && (ip == null || !ip.startsWith("129.237.201")))
                {
                    cnt++;
                    if (cnt % 100 == 0)
                    {
                        System.out.println(cnt);
                    }
                    
                    int recCnt = BasicSQLUtils.getCountAsInt(String.format("SELECT COUNT(*) FROM track WHERE Id = '%s'", id));
                    if (recCnt == 0) // Insert
                    {
                        trkStmt1.setTimestamp(1, ts);
                        trkStmt1.setString(2,    id);
                        trkStmt1.setInt(3,       1);
                        
                        //pStmt.toString();
                        
                        if (trkStmt1.executeUpdate() == 1)
                        {
                            recCnt++;
                            if (recCnt % 100 == 0)
                            {
                                System.out.println(recCnt);
                            }
                            
                            Integer trkId = BasicSQLUtils.getInsertedId(trkStmt1);
                            doTrackInserts(trkId, mv, trkStmt2);
                            
                        } else
                        {
                            throw new RuntimeException("Error insert track for ID: "+id);
                        }
                        
                    } else // Update
                    {
                        recCnt = BasicSQLUtils.getCountAsInt(String.format("SELECT CountAmt FROM track WHERE Id = '%s'", id)) + 1;
                        Integer trackId = BasicSQLUtils.getCount(String.format("SELECT TrackID FROM track WHERE Id = '%s'", id));
                        if (trackId != null)
                        {
                            trkStmt4.setInt(1, recCnt);
                            trkStmt4.setInt(2, trackId);
                            
                            if (trkStmt4.executeUpdate() == 1)
                            {
                                for (String key : mv.keySet())
                                {
                                    String sql = String.format("SELECT TrackItemID FROM trackitem WHERE TrackID = %d AND Name ='%s'", trackId, key);
                                    Integer trackItemId =  BasicSQLUtils.getCount(sql);
                                    if (trackItemId == null) // Insert
                                    {
                                        doTrackInserts(trackId, mv, trkStmt2);
                                        
                                    } else // Update
                                    {
                                        doTrackUpdate(trackItemId, mv.get(key), trkStmt3);
                                    }
                                }
                            } else
                            {
                                log.error(trkStmt4.toString());
                                log.error("Error updating "+id);
                            }
                        }
                    }
                }
                
            } catch (SQLException ex)
            {
                for (String k : mv.keySet())
                {
                    System.out.println("["+k+"]["+mv.get(k)+"]");
                }
                System.err.println("------------------------ Line No: " + lineNo);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * @param mv
     */
    private void insertReg(final HashMap<String, String> mv)
    {
        
        
        if (mv.size() > 0)
        {
            /*
            +------------------+-------------+------+-----+---------+----------------+
            | Field            | Type        | Null | Key | Default | Extra          |
            +------------------+-------------+------+-----+---------+----------------+
            | RegisterID       | int(11)     | NO   | PRI | NULL    | auto_increment | 
            | TimestampCreated | datetime    | NO   |     | NULL    |                | 
            | RegNumber        | varchar(32) | YES  | UNI | NULL    |                | 
            | RegType          | varchar(32) | YES  |     | NULL    |                | 
            +------------------+-------------+------+-----+---------+----------------+
            4 rows in set (0.00 sec)

            mysql> describe registeritem;
            +----------------+-------------+------+-----+---------+----------------+
            | Field          | Type        | Null | Key | Default | Extra          |
            +----------------+-------------+------+-----+---------+----------------+
            | RegisterItemID | int(11)     | NO   | PRI | NULL    | auto_increment | 
            | Name           | varchar(32) | NO   |     | NULL    |                | 
            | CountAmt       | int(11)     | YES  |     | NULL    |                | 
            | Value          | varchar(64) | YES  |     | NULL    |                | 
            | RegisterID     | int(11)     | NO   | MUL | NULL    |                | 
            +----------------+-------------+------+-----+---------+----------------+
             */
            try
            {
                String type = mv.get("reg_type");
                String num  = mv.get("reg_number");
                String ip   = mv.get("IP");
                
                if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(num) && (ip == null || !ip.startsWith("129.237.201")))
                {
                    
                    int numRegNum = BasicSQLUtils.getCountAsInt(String.format("SELECT COUNT(*) FROM register WHERE RegNumber = '%s'", num));
                    if (numRegNum > 0)
                    {
                        return;
                    }
                    
                    regStmt1.setTimestamp(1, ts);
                    regStmt1.setString(2,    num);
                    regStmt1.setString(3,    type);
                    
                    //pStmt.toString();
                    
                    if (regStmt1.executeUpdate() == 1)
                    {
                        cnt++;
                        if (cnt % 100 == 0)
                        {
                            System.out.println(cnt);
                        }
                        
                        Integer regId = BasicSQLUtils.getInsertedId(regStmt1);
                        for (String key : mv.keySet())
                        {
                            String value = mv.get(key);
                            regStmt2.setString(1, key);
                            if (!StringUtils.contains(value, ".") && StringUtils.isNumeric(value) && value.length() < 10)
                            {
                                regStmt2.setInt(2, Integer.parseInt(value));
                                regStmt2.setNull(3, java.sql.Types.VARCHAR);
                                
                            } else if (value.length() < STR_SIZE+1)
                            {
                                regStmt2.setNull(2, java.sql.Types.INTEGER);
                                regStmt2.setString(3, value);
                                
                            } else
                            {
                                String v = value.substring(0, STR_SIZE);
                                System.err.println("Error - On line "+lineNo+" Value["+value+"] too big trunccating to["+v+"]");
                                
                                regStmt2.setNull(2, java.sql.Types.INTEGER);
                                regStmt2.setString(3, v);
                            }
                            regStmt2.setInt(4, regId);
                            
                            //System.out.println(pStmt2.toString());
                            
                            int rv = regStmt2.executeUpdate();
                            if (rv != 1)
                            {
                                for (String k : mv.keySet())
                                {
                                    System.out.println("["+k+"]["+mv.get(k)+"]");
                                }
                                System.err.println("------------------------ Line No: " + lineNo);
                                throw new RuntimeException("Error insert registeritem for Reg Id: "+regId);
                            }
                        }
                    } else
                    {
                        throw new RuntimeException("Error insert register for Reg Type: "+type+"  Num: "+num);
                    }
                } else
                {
                    System.err.println("------------------------ Line No: " + lineNo);
                    System.err.println("Error for Reg Type: ["+type+"]  or Num: ["+ num + "] is null.");
                }
                
            } catch (SQLException ex)
            {
                for (String k : mv.keySet())
                {
                    System.out.println("["+k+"]["+mv.get(k)+"]");
                }
                System.err.println("------------------------ Line No: " + lineNo);
                ex.printStackTrace();
            }
        }
    }
    

    /**
     * @param dataFileName
     * @param dbName
     * @param doClear
     */
    public void process(final String dataFileName, final String dbName, final boolean doClear)
    {
        File           file   = new File(dataFileName);
        BufferedReader reader = null;
        
        boolean isReg = dbName.startsWith("reg");
        
        if (doClear)
        {
            if (isReg)
            {
                BasicSQLUtils.deleteAllRecordsFromTable("register", BasicSQLUtils.SERVERTYPE.MySQL);
                BasicSQLUtils.deleteAllRecordsFromTable("registeritem", BasicSQLUtils.SERVERTYPE.MySQL);
            } else
            {
                BasicSQLUtils.deleteAllRecordsFromTable("track", BasicSQLUtils.SERVERTYPE.MySQL);
                BasicSQLUtils.deleteAllRecordsFromTable("trackitem", BasicSQLUtils.SERVERTYPE.MySQL);
            }
        }

        try
        {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            HashMap<String, String> mappedValues = new HashMap<String, String>();
            
            // repeat until all lines is read
            while ((text = reader.readLine()) != null)
            {
                if (text.startsWith("-----------"))
                {
                    if (isReg)
                    {
                        insertReg(mappedValues);
                    } else
                    {
                        insertTrack(mappedValues);
                    }
                    mappedValues.clear();
                    continue;
                }
                
                if (StringUtils.isNotEmpty(text))
                {
                    String[] pair = StringUtils.split(text, "=");
                    if (pair.length == 2)
                    {
                        mappedValues.put(pair[0], pair[1]);
                        
                    } else if (pair.length == 1)
                    {
                        //mappedValues.put(pair[0], "");
                    } else
                    {
                        System.err.println("Error pairs "+pair.length+" ["+text+"]");
                    }
                }
                lineNo++;
            }
            
            System.out.println("Cnt: "+cnt);
            
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (stmt != null) stmt.close();
                if (trkStmt1 != null) trkStmt1.close();
                if (trkStmt2 != null) trkStmt2.close();
                if (trkStmt3 != null) trkStmt3.close();
                if (trkStmt4 != null) trkStmt4.close();
                if (regStmt1 != null) regStmt1.close();
                if (regStmt2 != null) regStmt2.close();
                
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
            
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
        String dbName           = "stats"; 
        String itUsername       = "root";
        String itPassword       = "root";
        
        DBConnection colDBConn  = null;
        Connection   connection = null;
        try
        {
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
            String             connStr    = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, "localhost", dbName, itUsername, itPassword, driverInfo.getName());
            
            System.err.println(connStr);
            
            colDBConn  = DBConnection.createInstance(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), dbName, connStr, itUsername, itPassword);
            connection = colDBConn.createConnection();
            
            BasicSQLUtils.setDBConnection(connection);
            
            RegAdder ra = new RegAdder(connection);
            //ra.process("/home/rods/reg.dat", "register", true);
            ra.process("/home/rods/track.dat", "track", true);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                    
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        

    }

}
