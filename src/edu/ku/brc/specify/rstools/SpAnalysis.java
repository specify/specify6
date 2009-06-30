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
package edu.ku.brc.specify.rstools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.ConversionLogger.TableWriter;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.Triple;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 29, 2009
 *
 */
public class SpAnalysis
{

    public SpAnalysis()
    {
        super();
    }

    
    public void checkAgents(final TableWriter tblWriter)
    {
        tblWriter.append("<H3>Agents</H3>");
        tblWriter.startTable();
        tblWriter.append("<TR><TH>AgentID</TH><TH>LastName</TH><TH>FirstName</TH><TH>MiddleInitial</TH><TH>Ids</TH></TR>");
        
        Statement stmt = null;
        Statement stmt2 = null;
        try
        {
            Integer cnt = BasicSQLUtils.getCount("SELECT (COUNT(LOWER(nm)) - COUNT(DISTINCT LOWER(nm))) AS DIF  FROM (SELECT CONCAT(LN,FN,MI) NM FROM(select IFNULL(LastName, '') LN, IFNULL(FirstName, '') FN, IFNULL(MiddleInitial, '') MI from agent) T1) T2");
            if (cnt != null && cnt > 0)
            {
                String sql = "SELECT AgentID, LOWER(nm) C1 FROM (SELECT AgentID, CONCAT(LN,FN,MI) NM FROM (select AgentID, IFNULL(LastName, '') LN, IFNULL(FirstName, '') FN, IFNULL(MiddleInitial, '') MI from agent) T1) T2 ";
                
                Vector<Integer> extraIds = new Vector<Integer>();
                
                Connection conn = DBConnection.getInstance().getConnection();
                stmt  = conn.createStatement();
                stmt2 = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql + "  ORDER BY AgentID");
                while (rs.next())
                {
                    int    id  = rs.getInt(1);
                    String str = rs.getString(2);
                    
                    extraIds.clear();
                    
                    str = StringUtils.replace(str, "'", "''");
                    String sql2 = sql + " WHERE LOWER(nm) = '"+str+"' AND AgentID > " + id + "  ORDER BY AgentID";
                    
                    //System.err.println(sql2);
                    
                    int dupCnt = 0;
                    ResultSet rs2 = stmt2.executeQuery(sql2);
                    while (rs2.next())
                    {
                        extraIds.add(rs2.getInt(1));
                        dupCnt++;
                    }
                    rs2.close();
                    
                    if (dupCnt > 0)
                    {
                        String s = "SELECT AgentID, LastName, FirstName, MiddleInitial FROM agent WHERE AgentID = "+id;
                        Vector<Object[]> rows = BasicSQLUtils.query(s);
                        Object[] row = rows.get(0);
                        tblWriter.append("<TR><TD>"+row[0]+"</TD><TD>"+row[1]+"</TD><TD>"+row[2]+"</TD><TD>"+row[3]+"</TD><TD>");
                        for (int i=0;i<extraIds.size();i++)
                        {
                            if (i > 0) tblWriter.log(", ");
                            tblWriter.append(extraIds.get(i).toString());
                        }
                        tblWriter.append("</TD></TR>");
                    }
                }
                rs.close();
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (stmt2 != null)
                {
                    stmt2.close();
                }
            } catch (Exception ex) {}
        }
        
        tblWriter.endTable();
    }
    
    
    public void checkCollectors(final TableWriter tblWriter)
    {
        Statement stmt = null;
        try
        {
            tblWriter.append("<H3>Collectors</H3>");
            tblWriter.startTable();
            tblWriter.append("<TR><TH>AddressID</TH><TH>Address</TH><TH>Address2</TH><TH>City</TH><TH>State</TH><TH>PostalCode</TH><TH>Ids</TH></TR>");
            
            String sql = "SELECT c.CollectingEventID, a.AgentID FROM collector c INNER JOIN agent a ON c.AgentID = a.AgentID ORDER BY c.CollectingEventID ASC, c.OrderNumber ASC";
            
            Hashtable<String, Triple<Integer, Integer, ArrayList<Integer>>> hash = new Hashtable<String, Triple<Integer, Integer, ArrayList<Integer>>>();
            StringBuilder sb = new StringBuilder();
            
            Integer ceId = null;
            Connection conn = DBConnection.getInstance().getConnection();
            stmt  = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql +"  ORDER BY AddressID");
            while (rs.next())
            {
                int    id      = rs.getInt(1);
                int    agentId = rs.getInt(2);
                
                if (ceId == null || !ceId.equals(id))
                {
                    if (ceId != null)
                    {
                        Pair<Integer, Integer> count = hash.get(sb.toString());
                        if (count == null)
                        {
                            //hash.put(sb.toString(), new Pair<Integer, Integer>(ceId, 1));
                        } else
                        {
                            count.second++;
                        }
                    }
                    sb.setLength(0);
                    sb.append(agentId);
                    sb.append(',');
                    ceId = id;
                } 
            }
            rs.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
            } catch (Exception ex) {}
        }
        
        tblWriter.endTable();
    }
    
    
    public void checkAddress(final TableWriter tblWriter)
    {
        Statement stmt = null;
        Statement stmt2 = null;
        try
        {
            Integer cnt = BasicSQLUtils.getCount("SELECT COUNT(LOWER(ADDR)) - COUNT(DISTINCT LOWER(ADDR)) AS DIF FROM (SELECT CONCAT(ID,A1,A2,C,S,Z) ADDR FROM (SELECT AgentID ID, IFNULL(Address, '') A1, IFNULL(Address2, '') A2, IFNULL(City, '') C, IFNULL(State, '') S, IFNULL(State, ''), IFNULL(PostalCode, '') Z from address) T1) T2 ");
            if (cnt != null && cnt > 0)
            {
                tblWriter.append("<H3>Address</H3>");
                tblWriter.startTable();
                tblWriter.append("<TR><TH>AddressID</TH><TH>Address</TH><TH>Address2</TH><TH>City</TH><TH>State</TH><TH>PostalCode</TH><TH>Ids</TH></TR>");
                
                String sql = "SELECT AddressID, LOWER(ADDR) C1 FROM (SELECT AddressID, CONCAT(ID, A1,A2,C,S,Z) ADDR FROM (SELECT AddressID, AgentID ID, IFNULL(Address, '') A1, IFNULL(Address2, '') A2, IFNULL(City, '') C, IFNULL(State, '') S, IFNULL(State, ''), IFNULL(PostalCode, '') Z from address) T1) T2 ";
                
                Vector<Integer> extraIds = new Vector<Integer>();
                
                Connection conn = DBConnection.getInstance().getConnection();
                stmt  = conn.createStatement();
                stmt2 = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql +"  ORDER BY AddressID");
                while (rs.next())
                {
                    int    id  = rs.getInt(1);
                    String str = rs.getString(2);
                    
                    extraIds.clear();
                    
                    str = StringUtils.replace(str, "'", "''");
                    String sql2 = sql + " WHERE LOWER(ADDR) = '"+str+"' AND AddressID > " + id;// +"  ORDER BY AddressID";
                    
                    int dupCnt = 0;
                    ResultSet rs2 = stmt2.executeQuery(sql2);
                    while (rs2.next())
                    {
                        extraIds.add(rs2.getInt(1));
                        dupCnt++;
                    }
                    rs2.close();
                    
                    if (dupCnt > 0)
                    {
                        String s = "SELECT AddressID, Address, Address2, City, state, PostalCode FROM address WHERE AddressID = "+id;
                        Vector<Object[]> rows = BasicSQLUtils.query(s);
                        Object[] row = rows.get(0);
                        tblWriter.append("<TR>");
                        for (Object data : row)
                        {
                            tblWriter.append("<TD>"+data+"</TD>");
                        }
                        tblWriter.append("<TD>");
                        for (int i=0;i<extraIds.size();i++)
                        {
                            if (i > 0) tblWriter.log(", ");
                            tblWriter.append(extraIds.get(i).toString());
                        }
                        tblWriter.append("</TD></TR>");
                    }
                }
                rs.close();
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (stmt2 != null)
                {
                    stmt2.close();
                }
            } catch (Exception ex) {}
        }
        
        tblWriter.endTable();
    }

}
