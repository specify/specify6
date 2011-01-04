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
package edu.ku.brc.specify.toycode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.helpers.HTTPGetter;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 24, 2010
 *
 */
public class RegPivot
{
    enum ProcessType {eBuildReg, eBuildTrack, eBuildRegCC, eBuildTrkCC, eCrossMapRegToTrk}
    
    protected DBConnection colDBConn  = null;
    protected Connection   connection = null;
    
    
    protected String dbName           = "stats"; 
    protected String itUsername       = "root";
    protected String itPassword       = "root";

    /**
     * 
     */
    public RegPivot()
    {
        super();
        
        try
        {
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
            String             connStr    = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, "localhost", dbName, itUsername, itPassword, driverInfo.getName());
            
            System.err.println(connStr);
            
            colDBConn  = DBConnection.createInstance(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), dbName, connStr, itUsername, itPassword);
            connection = colDBConn.createConnection();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param newTblName
     * @param tblName
     * @param keyName
     */
    private void process(final String newTblName, 
                         final String tblName, 
                         final String keyName,
                         final String defSQL,
                         final String fillSQL)
    {
        
        String sql  = String.format("SELECT DISTINCT Name FROM %s", tblName);
        String sql2 = "SELECT MAX(LENGTH(Value)) FROM " + tblName + " WHERE Name = '%s'";

        int instCnt = 0;
        
        Statement stmt = null;
        try
        {
            stmt = connection.createStatement();
            
            BasicSQLUtils.setDBConnection(connection);
            
            boolean doBuild = true;
            
            if (doBuild)
            {
                StringBuilder tblSQL = new StringBuilder(String.format("CREATE TABLE %s (`%s` INT(11) NOT NULL AUTO_INCREMENT, \n", newTblName, keyName));
                
                Vector<String>  dbFieldNames = new Vector<String>();
                Vector<Integer> dbFieldTypes = new Vector<Integer>();
                
                if (defSQL != null)
                {
                    ResultSet         rs   = stmt.executeQuery(defSQL);
                    ResultSetMetaData rsmd = rs.getMetaData();
                    for (int i=1;i<=rsmd.getColumnCount();i++)
                    {
                        if (i > 1) tblSQL.append(",\n ");
                        
                        String name = rsmd.getColumnName(i);
                        dbFieldNames.add(rsmd.getColumnName(i));
                        dbFieldTypes.add(rsmd.getColumnType(i));
                        switch (rsmd.getColumnType(i))
                        {
                            case java.sql.Types.INTEGER:
                                tblSQL.append(String.format("`%s` INT(11) DEFAULT NULL", name));
                                break;
                                
                            case java.sql.Types.VARCHAR:
                                tblSQL.append(String.format("`%s` VARCHAR(%s) DEFAULT NULL", name, 64));
                                break;
                                
                            case java.sql.Types.TIMESTAMP:
                                tblSQL.append(String.format("`%s` DATETIME DEFAULT NULL", name));
                                break;
                                
                            default:
                                System.err.println(String.format("No case for %s %d", name, rsmd.getColumnType(i)));
                                break;
                        }
                    }
                    rs.close();
                }
                
                int secInx = dbFieldNames.size() + 1;
                
                System.out.println("secInx: "+secInx+"  "+tblSQL.toString());
                
                HashSet<String> nameSet = new HashSet<String>();
                
                int cnt = 0;
                for (Object nmObj : BasicSQLUtils.querySingleCol(connection, sql))
                {
                    String name = nmObj.toString();
                    
                    if (name.endsWith("ID"))
                    {
                        continue;
                    }
                    
                    name = StringUtils.replace(name, "(", "_");
                    name = StringUtils.replace(name, ")", "_");
                    
                    if (nameSet.contains(name)) continue;
                    
                    nameSet.add(name);
                    
                    tblSQL.append(",\n ");
                    
                    if (name.startsWith("num_") || name.startsWith("Usage_"))
                    {
                        tblSQL.append(String.format("`%s` INT(11) DEFAULT NULL", name));
                        dbFieldNames.add(name);
                        dbFieldTypes.add(java.sql.Types.INTEGER);
                        
                    } else
                    {
                        int maxLen = BasicSQLUtils.getCountAsInt(connection, String.format(sql2, name));
                        tblSQL.append(String.format("`%s` VARCHAR(%s) DEFAULT NULL", name, maxLen+1));
                        dbFieldNames.add(name);
                        dbFieldTypes.add(java.sql.Types.VARCHAR);
                    }
                    cnt++;
                }
                
                tblSQL.append(String.format(",\n PRIMARY KEY (`%s`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8", keyName));
                
                System.out.println(tblSQL.toString());

                DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
                dbMgr.setConnection(connection);
                if (dbMgr.doesDBHaveTable(newTblName))
                {
                    BasicSQLUtils.update(connection, "DROP TABLE "+newTblName);
                }
                BasicSQLUtils.update(connection, tblSQL.toString());
            
                HashMap<Integer, String> inxToName = new HashMap<Integer, String>();
    
                StringBuilder fields = new StringBuilder();
                StringBuilder vals   = new StringBuilder();
                int           inx    = 0;
                for (String nm : dbFieldNames)
                {
                    if (fields.length() > 0) fields.append(","); 
                    fields.append(nm);
                    
                    if (vals.length() > 0) vals.append(","); 
                    vals.append('?');
                    
                    inxToName.put(inx, nm);
                    inx++;
                }
                
                String insertSQL = String.format("INSERT INTO %s (%s) VALUES(%s)", newTblName, fields.toString(), vals.toString());
                System.out.println(insertSQL);
                
                PreparedStatement pStmt = connection.prepareStatement(insertSQL);
                
                HashMap<String, Object> nameToVals = new HashMap<String, Object>();
                
                System.out.println(fillSQL);
                
                String prevId = null;
                ResultSet rs = stmt.executeQuery(fillSQL);
                while (rs.next())
                {
                    String id = rs.getString(1);
                    if (prevId == null) prevId = id;
                    
                    if (!prevId.equals(id))
                    {
                       for (int i=1;i<secInx;i++)
                       {
                           //System.out.println("Put: "+dbFieldNames.get(i-1)+"  "+dbFieldTypes.get(i-1));//+"  = "+rs.getObject(i));
                           if (dbFieldTypes.get(i-1) == java.sql.Types.TIMESTAMP)
                           {
                               try
                               {
                                   String ts = rs.getString(i);
                                   if (StringUtils.isNotEmpty(ts) && ts.equals("0000-00-00 00:00:00"))
                                   {
                                       //nameToVals.put(dbFieldNames.get(i-1), null);
                                       continue;
                                   }
                               } catch (Exception ex)
                               {
                                   nameToVals.put(dbFieldNames.get(i-1), null);//"2000-01-01 00:00:00");
                                   continue;
                               }
                           }
                           nameToVals.put(dbFieldNames.get(i-1), rs.getObject(i));
                       }
                       
                       for (int i=0;i<dbFieldNames.size();i++)
                       {
                           int    fInx  = i + 1;
                           String name  = inxToName.get(i);
                           Object value = nameToVals.get(name);
                           
                           pStmt.setObject(fInx, null);
                           
                           /*if (name.equals("num_co"))
                           {
                               System.out.println(/"+value+"  "+(value != null ? value.getClass().getSimpleName() : ""));
                           }*/
                           
                           //System.out.println(name+" - "+dbFieldTypes.get(i)+"["+value+"]"+(value != null ? value.getClass().getSimpleName():""));
                           
                           int typ = dbFieldTypes.get(i);
                           
                           if (value != null)
                           {
                               /*if (value instanceof String)
                               {
                                   String valStr = (String)value;
                                   if (StringUtils.isNotEmpty(valStr))
                                   {
                                       typ = java.sql.Types.VARCHAR;
                                   }
                                   
                               } else if (value instanceof Integer)
                               {
                                   typ = java.sql.Types.INTEGER;
                               }*/
                                if (name.equals("Institution_name"))
                                {
                                    String valStr = (String)value;
                                    if (StringUtils.isNotEmpty(valStr))
                                    {
                                        instCnt++;
                                    }
                                }
                               switch (typ)
                               {
                                   case java.sql.Types.INTEGER : 
                                       if (value instanceof Integer)
                                       {
                                           pStmt.setInt(fInx, (Integer)value);
                                       }
                                       break;
                                   
                                   case java.sql.Types.VARCHAR : 
                                       if (value instanceof String)
                                       {
                                           pStmt.setString(fInx, (String)value);
                                       }
                                       break;
                                   
                                   case java.sql.Types.TIMESTAMP : 
                                   {
                                       if (value instanceof Timestamp)
                                       {
                                           pStmt.setTimestamp(fInx, (Timestamp)value);
                                       }
                                       break;
                                   }
                               }
                           } else
                           {
                               pStmt.setObject(fInx, null);
                           }
                       }
                       pStmt.executeUpdate();
                       
                       prevId = id;
                       nameToVals.clear();
                    }
                    
                    String  name   = rs.getString(secInx);
                    name = StringUtils.replace(name, "(", "_");
                    name = StringUtils.replace(name, ")", "_");

                    Integer intVal = (Integer)rs.getObject(secInx+1);
                    String  strVal = (String)rs.getObject(secInx+2);
                    nameToVals.put(name, strVal != null ? strVal : intVal);
                }
                
                String alterSQL = "ALTER TABLE "+newTblName+" ADD Lookup VARCHAR(64) AFTER IP";
                BasicSQLUtils.update(connection, alterSQL);
                
                alterSQL = "ALTER TABLE "+newTblName+" ADD Country VARCHAR(64) AFTER Lookup";
                BasicSQLUtils.update(connection, alterSQL);
                
                alterSQL = "ALTER TABLE "+newTblName+" ADD City VARCHAR(64) AFTER Country";
                BasicSQLUtils.update(connection, alterSQL);
                
                System.out.println("InstCnt: "+instCnt);
                pStmt.close();
            }
            
            boolean doIP = false;
            if (doIP)
            {
                HTTPGetter httpGetter = new HTTPGetter();
                
                sql = "SELECT RegID, IP from reg";
                PreparedStatement pStmt = connection.prepareStatement(String.format("UPDATE %s SET lookup=?, Country=?, City=? WHERE %s = ?", newTblName, keyName));
                
                HashMap<String, String>               ipHash = new HashMap<String, String>();
                HashMap<String, Pair<String, String>> ccHash = new HashMap<String, Pair<String, String>>();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next())
                {
                    int    regId = rs.getInt(1);
                    String ip    = rs.getString(2);
                    
                    String hostName = ipHash.get(ip);
                    String country  = null;
                    String city     = null;
                    if (hostName == null)
                    {
                        String rvStr   = new String(httpGetter.doHTTPRequest("http://api.hostip.info/get_html.php?ip="+ip));
                        country = parse(rvStr, "Country:");
                        city    = parse(rvStr, "City:");
                        System.out.println(rvStr+"["+country+"]["+city+"]");
                        
                        try
                        {
                            InetAddress addr = InetAddress.getByName(ip);
                            hostName = addr.getHostName();
                            ipHash.put(ip, hostName);
                            ccHash.put(ip, new Pair<String, String>(country, city));
                            
                        } catch (UnknownHostException e)
                        {
                            e.printStackTrace();
                        }
                    } else
                    {
                        Pair<String, String> p = ccHash.get(ip);
                        if (p != null)
                        {
                            country = p.first;
                            city    = p.second;
                        }
                    }
                    
                    pStmt.setString(1, hostName);
                    pStmt.setString(2, country);
                    pStmt.setString(3, city);
                    pStmt.setInt(4, regId);
                    pStmt.executeUpdate();
                }
                pStmt.close();
            }
            
            stmt.close();
            colDBConn.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        System.out.println("Done.");
    }
    
    /**
     * @param tblName
     * @param keyName
     */
    public void fillCountryCity(final String tblName, final String keyName)
    {
        Statement stmt = null;
        try
        {
            stmt = connection.createStatement();
            
            BasicSQLUtils.setDBConnection(connection);
        
            HTTPGetter httpGetter = new HTTPGetter();
            
            String sql = String.format("SELECT %s, IP, Lookup, Country, City from %s", keyName, tblName);
            PreparedStatement pStmt = connection.prepareStatement(String.format("UPDATE %s SET lookup=?, Country=?, City=? WHERE %s = ?", tblName, keyName));
            
            HashMap<String, String>               ipHash = new HashMap<String, String>();
            HashMap<String, Pair<String, String>> ccHash = new HashMap<String, Pair<String, String>>();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                int    regId   = rs.getInt(1);
                String ip      = rs.getString(2);
                String lookup  = rs.getString(3);
                String country = rs.getString(4);
                String city    = rs.getString(5);
                
                boolean allEmpty = StringUtils.isEmpty(lookup) && StringUtils.isEmpty(country) && StringUtils.isEmpty(city);
                
                String hostName = ipHash.get(ip);
                
                if (allEmpty && hostName == null)
                {
                    String rvStr   = new String(httpGetter.doHTTPRequest("http://api.hostip.info/get_html.php?ip="+ip));
                    country = parse(rvStr, "Country:");
                    city    = parse(rvStr, "City:");
                    System.out.println(rvStr+"["+country+"]["+city+"]");
                    
                    try
                    {
                        InetAddress addr = InetAddress.getByName(ip);
                        hostName = addr.getHostName();
                        ipHash.put(ip, hostName);
                        ccHash.put(ip, new Pair<String, String>(country, city));
                        
                    } catch (UnknownHostException e)
                    {
                        e.printStackTrace();
                    }
                } else
                {
                    Pair<String, String> p = ccHash.get(ip);
                    if (p != null)
                    {
                        country = p.first;
                        city    = p.second;
                    }
                }
                
                pStmt.setString(1, hostName);
                pStmt.setString(2, country);
                pStmt.setString(3, city);
                pStmt.setInt(4,    regId);
                pStmt.executeUpdate();
            }
            pStmt.close();
            
            stmt.close();
            colDBConn.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        System.out.println("Done.");
    }
    
    /**
     * 
     */
    public void crossMapCC()
    {
        Statement    stmt       = null;
        try
        {
            stmt       = connection.createStatement();
            
            BasicSQLUtils.setDBConnection(connection);
            
            String sql   = "SELECT TrkID, id from trk";
            String lkSQL = "SELECT lookup, Country, City FROM reg WHERE id = '%s'";
            
            PreparedStatement pStmt = connection.prepareStatement(String.format("UPDATE %s SET lookup=?, Country=?, City=? WHERE %s = ?", "trk", "TrkID"));

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                int    trkId = rs.getInt(1);
                String idStr = rs.getString(2);
                
                Vector<Object[]> rows = BasicSQLUtils.query(String.format(lkSQL, idStr));
                
                if (rows != null && rows.size() > 0)
                {
                    Object[] row = rows.get(0);
                    pStmt.setString(1, (String)row[0]);
                    pStmt.setString(2, (String)row[1]);
                    pStmt.setString(3, (String)row[2]);
                    pStmt.setInt(4, trkId);
                    pStmt.executeUpdate();
                }
            }
            pStmt.close();
            
            stmt.close();
            
            System.out.println("Done.");
        
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param str
     * @param sym
     * @return
     */
    private String parse(final String str, final String sym)
    {
        if (StringUtils.isNotEmpty(str))
        {
            int sInx = str.indexOf(sym);
            if (sInx > -1)
            {
                int eInx = str.indexOf('\n', sInx);
                if (eInx > -1)
                {
                    return str.substring(sInx+sym.length(), eInx);
                }
            }
        }
        return null;
    }

    /**
     * 
     */
    public void shutdown()
    {
        try
        {
            colDBConn.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void doProcess(final ProcessType processType)
    {
        switch (processType)
        {
            case eBuildReg:
                {
                    String defSQL  = "SELECT r.RegNumber, r.RegType, r.IP, r.TimestampCreated FROM register r WHERE r.RegNumber IS NULL";
                    String fillSQL = "SELECT r.RegNumber, r.RegType, r.IP, r.TimestampCreated, i.Name, i.CountAmt, i.Value FROM register r INNER JOIN registeritem i ON r.RegisterID = i.RegisterID";
                    process("reg", "registeritem", "RegID", defSQL, fillSQL);
                }
                break;
                
            case eBuildTrack:
                {
                    String defSQL  = "SELECT t.CountAmt, t.IP, t.TimestampCreated, t.TimestampModified FROM track t WHERE t.Id IS NULL";
                    String fillSQL = "SELECT t.CountAmt, t.IP, t.TimestampCreated, t.TimestampModified, i.Name, i.CountAmt, i.Value FROM track t INNER JOIN trackitem i ON t.TrackID = i.TrackID ORDER BY TimestampCreated";
                    process("trk", "trackitem", "TrkID", defSQL, fillSQL);
                }
                break;
                
            case eBuildRegCC:
                fillCountryCity("reg", "RegID");
                break;
                
            case eBuildTrkCC:
                fillCountryCity("trk", "TrkID");
                break;
                
            case eCrossMapRegToTrk:
                crossMapCC();
                break;
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.setProperty(DBMSUserMgr.factoryName, "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");

        RegPivot rp = new RegPivot();
        
        rp.doProcess(ProcessType.eBuildReg);
        rp.shutdown();
    }

}
