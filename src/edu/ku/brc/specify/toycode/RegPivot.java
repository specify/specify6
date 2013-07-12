/* Copyright (C) 2013, University of Kansas Center for Research
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
import java.sql.SQLException;
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
                         final String fillSQL,
                         final boolean isRegBuild)
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
                        
                    } else if (name.endsWith("_number"))
                    {
                        tblSQL.append(String.format("`%s` VARCHAR(16) DEFAULT NULL", name));
                        dbFieldNames.add(name);
                        dbFieldTypes.add(java.sql.Types.VARCHAR);
                        
                    } else
                    {
                        int maxLen = BasicSQLUtils.getCountAsInt(connection, String.format(sql2, name));
                        tblSQL.append(String.format("`%s` VARCHAR(%s) DEFAULT NULL", name, maxLen+1));
                        dbFieldNames.add(name);
                        dbFieldTypes.add(java.sql.Types.VARCHAR);
                    }
                    cnt++;
                }
                
                if (isRegBuild) 
                {
                    tblSQL.append(String.format(",\n`RecordType`INT(11) DEFAULT NULL"));
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
                
                if (isRegBuild) 
                {
                    if (fields.length() > 0) fields.append(","); 
                    fields.append("RecordType");
                    
                    if (vals.length() > 0) vals.append(","); 
                    vals.append('?');
                }
                
                String insertSQL = String.format("INSERT INTO %s (%s) VALUES(%s)", newTblName, fields.toString(), vals.toString());
                System.out.println(insertSQL);
                
                PreparedStatement pStmt = connection.prepareStatement(insertSQL);
                
                if (isRegBuild)
                {
                    fillRegisterTable(newTblName, stmt, pStmt, fillSQL, secInx, dbFieldTypes, dbFieldNames, inxToName);
                } else
                {
                    fillTrackTable(newTblName, stmt, pStmt, fillSQL, secInx, dbFieldTypes, dbFieldNames, inxToName);
                }
                
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
     * @param newTblName
     * @param stmt
     * @param pStmt
     * @param fillSQL
     * @param secInx
     * @param dbFieldTypes
     * @param dbFieldNames
     * @param inxToName
     * @return
     * @throws SQLException
     */
    private int fillRegisterTable(final String newTblName,
                                  final Statement         stmt,
                                  final PreparedStatement pStmt,
                                  final String fillSQL, 
                                  final int secInx, 
                                  final Vector<Integer> dbFieldTypes,
                                  final Vector<String>  dbFieldNames,
                                  final HashMap<Integer, String> inxToName) throws SQLException
    {
        System.out.println("Filling Register Table.");

        int instCnt = 0;
        
        System.out.println(fillSQL);
        
        ResultSet         rs   = stmt.executeQuery(fillSQL);
        ResultSetMetaData rsmd = rs.getMetaData();
        
        HashMap<String, Integer> nameToIndex = new HashMap<String, Integer>();
        for (int c=1;c<=rsmd.getColumnCount();c++)
        {
            nameToIndex.put(rsmd.getColumnName(c), c);
            System.out.println(c+" - "+rsmd.getColumnName(c));
        }
        
        //int nameInx = nameToIndex.get("i.Name");
        boolean debug = false;
        
        String prevRegId = null;
        
        HashMap<String, HashMap<String, Object>> instHash = new HashMap<String, HashMap<String, Object>>();
        HashMap<String, HashMap<String, Object>> divHash  = new HashMap<String, HashMap<String, Object>>();
        HashMap<String, HashMap<String, Object>> dspHash  = new HashMap<String, HashMap<String, Object>>();
        HashMap<String, HashMap<String, Object>> colHash  = new HashMap<String, HashMap<String, Object>>();
        
        HashMap<String, Object> nameToVals = new HashMap<String, Object>();

        
        while (rs.next())
        {
            String regId = rs.getString(1);
            if (prevRegId == null) prevRegId = regId;
            
            for (int i = 1; i < secInx; i++)
            {
                if (debug) System.out.println("Put: "+dbFieldNames.get(i-1)+"  "+dbFieldTypes.get(i-1)+"  = "+rs.getObject(i));
                nameToVals.put(dbFieldNames.get(i - 1), rs.getObject(i));
            }
            String name = rs.getString(secInx);
            name = StringUtils.replace(name, "(", "_");
            name = StringUtils.replace(name, ")", "_");
            
            if (name.equals("reg_type"))
            {
                String strVal = (String) rs.getObject(secInx + 2);
                name = strVal + "_number";
                
                nameToVals.put(name, regId);
                if (debug) System.out.println("Put: "+name+" = "+regId);
            } else
            {
                Integer intVal = (Integer) rs.getObject(secInx + 1);
                String  strVal = (String) rs.getObject(secInx + 2);
                nameToVals.put(name, strVal != null ? strVal : intVal);
                if (debug) System.out.println("Put: "+name+" = "+intVal+" / "+strVal);
            }
            
            if (debug) System.out.println("-------------------------------------------");
            
            if (!prevRegId.equals(regId))
            {
                String instNum = (String)nameToVals.get("Institution_number");
                String divNum  = (String)nameToVals.get("Division_number");
                String dspNum  = (String)nameToVals.get("Discipline_number");
                String colNum  = (String)nameToVals.get("Collection_number");
                
                if (StringUtils.isNotEmpty(instNum))
                {
                    copyHash(instNum, instHash, nameToVals);
                }
                
                if (StringUtils.isNotEmpty(divNum))
                {
                    copyHash(divNum, divHash, nameToVals);
                }
                
                if (StringUtils.isNotEmpty(dspNum))
                {
                    copyHash(dspNum, dspHash, nameToVals);
                }
                
                if (StringUtils.isNotEmpty(colNum))
                {
                    // 1288612353.83
                    String cn = (String)nameToVals.get("Collection_number");
                    copyHash(colNum, colHash, nameToVals);
                }
                
                /*{
                    System.err.println("ID is empty:");
                    for (String key : nameToVals.keySet())
                    {
                        System.out.println("--: "+key+" = "+nameToVals.get(key));
                    }
                    System.err.println("===============");
                }*/
                prevRegId = regId;
                nameToVals.clear();
            }
        }
        
        writeHash(instHash, 0, pStmt, dbFieldTypes, dbFieldNames, inxToName);
        writeHash(divHash,  1, pStmt, dbFieldTypes, dbFieldNames, inxToName);
        writeHash(dspHash,  2, pStmt, dbFieldTypes, dbFieldNames, inxToName);
        writeHash(colHash,  3, pStmt, dbFieldTypes, dbFieldNames, inxToName);
        
        String alterSQL = "ALTER TABLE "+newTblName+" ADD Lookup VARCHAR(64) AFTER IP";
        BasicSQLUtils.update(connection, alterSQL);
        
        alterSQL = "ALTER TABLE "+newTblName+" ADD Country VARCHAR(64) AFTER Lookup";
        BasicSQLUtils.update(connection, alterSQL);
        
        alterSQL = "ALTER TABLE "+newTblName+" ADD City VARCHAR(64) AFTER Country";
        BasicSQLUtils.update(connection, alterSQL);

        return instCnt;
    }
    
    /**
     * @param numId
     * @param hash
     * @param data
     */
    private void copyHash(final String numId, final HashMap<String, HashMap<String, Object>> hash, final HashMap<String, Object> data)
    {
        HashMap<String, Object> dataHash = hash.get(numId);
        if (dataHash == null)
        {
            dataHash = new HashMap<String, Object>(data);
            hash.put(numId, dataHash);
        } else
        {
            for (String key : data.keySet())
            {
                dataHash.put(key, data.get(key));
            }
        }
    }
    
    /**
     * @param hash
     * @param recordType
     * @param pStmt
     * @param dbFieldTypes
     * @param dbFieldNames
     * @param inxToName
     * @throws SQLException
     */
    private void writeHash(final HashMap<String, HashMap<String, Object>> hash, 
                           final Integer                  recordType,
                           final PreparedStatement        pStmt,
                           final Vector<Integer>          dbFieldTypes,
                           final Vector<String>           dbFieldNames,
                           final HashMap<Integer, String> inxToName) throws SQLException
    {
        int totalCnt = hash.size();
        int cnt = 0;
        
        for (String idKey : hash.keySet())
        {
            cnt++;
            if (cnt % 500 == 0) System.out.println(cnt +" / "+totalCnt);
            
            HashMap<String, Object> nameToVals = hash.get(idKey);
            
            if (recordType != null)
            {
                pStmt.setInt(dbFieldNames.size()+1, (Integer) recordType);
            }
            
            for (int i = 0; i < dbFieldNames.size(); i++)
            {
                int fInx = i + 1;
                String name  = inxToName.get(i);
                Object value = nameToVals.get(name);

                pStmt.setObject(fInx, null);

                int typ = dbFieldTypes.get(i);

                if (value != null)
                {
                    if (value instanceof Integer)
                    {
                        pStmt.setInt(fInx, (Integer) value);
                        
                    } else if (value instanceof String)
                    {
                        pStmt.setString(fInx, (String) value);
                    } else if (value instanceof Timestamp)
                    {
                        pStmt.setTimestamp(fInx, (Timestamp) value);
                    } else
                    {
                        System.err.println("Unhandled class: "+ value.getClass().getName());
                    }
                } else
                {
                    pStmt.setObject(fInx, null);
                }
            }
            pStmt.executeUpdate();
        }

    }
    /**
     * @param newTblName
     * @param stmt
     * @param pStmt
     * @param fillSQL
     * @param secInx
     * @param dbFieldTypes
     * @param dbFieldNames
     * @param inxToName
     * @return
     * @throws SQLException
     */
    private int fillTrackTable(final String newTblName,
                                  final Statement         stmt,
                                  final PreparedStatement pStmt,
                                  final String fillSQL, 
                                  final int secInx, 
                                  final Vector<Integer> dbFieldTypes,
                                  final Vector<String>  dbFieldNames,
                                  final HashMap<Integer, String> inxToName) throws SQLException
    {
        System.out.println("Filling Track Table.");

        int instCnt = 0;
        
        System.out.println(fillSQL);
        
        ResultSet         rs   = stmt.executeQuery(fillSQL);
        ResultSetMetaData rsmd = rs.getMetaData();
        
        HashMap<String, Integer> nameToIndex = new HashMap<String, Integer>();
        for (int c=1;c<=rsmd.getColumnCount();c++)
        {
            nameToIndex.put(rsmd.getColumnName(c), c);
            System.out.println(c+" - "+rsmd.getColumnName(c));
        }
        
        boolean debug = false;
        
        String prevRegId = null;
        
        HashMap<String, HashMap<String, Object>> colHash  = new HashMap<String, HashMap<String, Object>>();
        
        HashMap<String, Object> nameToVals = new HashMap<String, Object>();

        
        while (rs.next())
        {
            String regId = rs.getString(1);
            if (prevRegId == null) prevRegId = regId;
            
            for (int i = 1; i < secInx; i++)
            {
                if (debug) System.out.println("Put: "+dbFieldNames.get(i-1)+"  "+dbFieldTypes.get(i-1)+"  = "+rs.getObject(i));
                
                if (dbFieldTypes.get(i-1) == java.sql.Types.TIMESTAMP)
                {
                    try
                    {
                        String ts = rs.getString(i);
                        if (StringUtils.isNotEmpty(ts) && ts.equals("0000-00-00 00:00:00"))
                        {
                            continue;
                        }
                    } catch (Exception ex)
                    {
                        continue;
                    }
                }
                nameToVals.put(dbFieldNames.get(i - 1), rs.getObject(i));
            }
            String name = rs.getString(secInx);
            name = StringUtils.replace(name, "(", "_");
            name = StringUtils.replace(name, ")", "_");
            
            if (name.equals("reg_type"))
            {
                String strVal = (String) rs.getObject(secInx + 2);
                name = strVal + "_number";
                
                nameToVals.put(name, regId);
                if (debug) System.out.println("Put: "+name+" = "+regId);
            } else
            {
                Integer intVal = (Integer) rs.getObject(secInx + 1);
                String  strVal = (String) rs.getObject(secInx + 2);
                nameToVals.put(name, strVal != null ? strVal : intVal);
                if (debug) System.out.println("Put: "+name+" = "+intVal+" / "+strVal);
            }
            
            if (debug) System.out.println("-------------------------------------------");
            
            if (!prevRegId.equals(regId))
            {
                String colNum  = (String)nameToVals.get("Collection_number");
                
                if (StringUtils.isNotEmpty(colNum))
                {
                    copyHash(colNum, colHash, nameToVals);
                }
                prevRegId = regId;
                nameToVals.clear();
            }
        }
        
        writeHash(colHash,  null, pStmt, dbFieldTypes, dbFieldNames, inxToName);
        
        String alterSQL = "ALTER TABLE "+newTblName+" ADD Lookup VARCHAR(64) AFTER IP";
        BasicSQLUtils.update(connection, alterSQL);
        
        alterSQL = "ALTER TABLE "+newTblName+" ADD Country VARCHAR(64) AFTER Lookup";
        BasicSQLUtils.update(connection, alterSQL);
        
        alterSQL = "ALTER TABLE "+newTblName+" ADD City VARCHAR(64) AFTER Country";
        BasicSQLUtils.update(connection, alterSQL);

        return instCnt;
    }

    /**
     * @param newTblName
     * @param stmt
     * @param pStmt
     * @param fillSQL
     * @param secInx
     * @param dbFieldTypes
     * @param dbFieldNames
     * @param inxToName
     * @return
     * @throws SQLException
     */
    private int fillTrackTableX(final String            newTblName,
                               final Statement         stmt,
                               final PreparedStatement pStmt,
                               final String            fillSQL, 
                               final int               secInx, 
                               final Vector<Integer>   dbFieldTypes,
                               final Vector<String>    dbFieldNames,
                               final HashMap<Integer, String> inxToName) throws SQLException
    {
        System.out.println("Filling Track Table.");
        int instCnt = 0;
        
        HashMap<String, Object> nameToVals = new HashMap<String, Object>();
        
        System.out.println(fillSQL);
        
        String prevId = null;
        ResultSet         rs   = stmt.executeQuery(fillSQL);
        ResultSetMetaData rsmd = rs.getMetaData();
        
        HashMap<String, Integer> nameToIndex = new HashMap<String, Integer>();
        for (int c=1;c<=rsmd.getColumnCount();c++)
        {
            nameToIndex.put(rsmd.getColumnName(c), c);
            System.out.println(c+" - "+rsmd.getColumnName(c));
        }
        
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
                   
                   int typ = dbFieldTypes.get(i);
                   
                   if (value != null)
                   {
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

        return instCnt;
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
            
            String sql = String.format("SELECT %s, IP, Lookup, Country, City FROM %s WHERE Country IS NULL", keyName, tblName);
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
                    process("reg", "registeritem", "RegID", defSQL, fillSQL, true);
                }
                break;
                
            case eBuildTrack:
                {
                    String defSQL  = "SELECT t.CountAmt, t.IP, t.TimestampCreated, t.TimestampModified FROM track t WHERE t.Id IS NULL";
                    String fillSQL = "SELECT t.CountAmt, t.IP, t.TimestampCreated, t.TimestampModified, i.Name, i.CountAmt, i.Value FROM track t INNER JOIN trackitem i ON t.TrackID = i.TrackID ORDER BY TimestampCreated";
                    process("trk", "trackitem", "TrkID", defSQL, fillSQL, false);
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
        //rp.doProcess(ProcessType.eBuildTrack);
        //rp.doProcess(ProcessType.eBuildRegCC);
        //rp.doProcess(ProcessType.eBuildTrkCC);
        rp.shutdown();
        
        System.out.println("App Done.");
    }

}
