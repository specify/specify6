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

import static edu.ku.brc.specify.conversion.BasicSQLUtils.queryForRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.SwingWorker;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.ProgressDialog;

public class TreeBuilder
{
    private ProgressDialog progressDelegate;
    private SwingWorker<Integer, Integer> worker;
    
    private Connection     dbS3Conn;
    private Connection     conn;
    private iPadDBExporter ipadExporter;
    private HashMap<Integer, HashSet<Integer>> rankSets = new HashMap<Integer, HashSet<Integer>>();
    
    private int            totalRecords;
    private int            minRank = 139;
    
    private Vector<Integer> ranks;
    
    public TreeBuilder(final iPadDBExporter ipadExporter, 
                       final Connection dbS3Conn, 
                       final Connection conn)
    {
        this.dbS3Conn         = dbS3Conn;
        this.conn             = conn;
        this.ipadExporter     = ipadExporter;
        this.progressDelegate = ipadExporter.getProgressDelegate();
        this.worker           = ipadExporter.getWorker();
    }
    
    public void process()
    {
        System.out.println("  ");
        getRanks();
        buildRankSets();
        collectParentIds();
        exportTreeData();
    }
    
    private void getRanks()
    {
        String sql = "SELECT gtd.RankID FROM geographytreedefitem gtd WHERE gtd.GeographyTreeDefID = GEOTREEDEFID ORDER BY gtd.RankID DESC ";
       ranks = BasicSQLUtils.queryForInts(conn, ipadExporter.adjustSQL(sql));
    }

    private void buildRankSets()
    {
        String sql = "SELECT g.GeographyID FROM collectionobject co " +
        		     "INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                     "INNER JOIN locality l ON ce.LocalityID = l.LocalityID " +
                     "INNER JOIN geography g ON l.GeographyID = g.GeographyID " +
                     "WHERE GeographyTreeDefID = GEOTREEDEFID AND RankID = ?";
        sql = ipadExporter.adjustSQL(sql);
        PreparedStatement pStmt = null;
        try
        {
            pStmt = conn.prepareStatement(sql);

             for (Integer rankId : ranks)
             {
                 HashSet<Integer> set = rankSets.get(rankId);
                 if (set == null)
                 {
                     set = new HashSet<Integer>();
                     rankSets.put(rankId, set);
                 }
                 pStmt.setInt(1, rankId);
                 
                 ResultSet rs = pStmt.executeQuery();
                 while (rs != null && rs.next())
                 {
                     int rId = rs.getInt(1);
                     set.add(rId);
                 }
                 rs.close();
             }
         } catch (Exception e) 
         {
             e.printStackTrace();
         } finally
         {
             try
             {
                 if (pStmt != null) pStmt.close();
             } catch (Exception ex2) { ex2.printStackTrace();}
         }
    }

    private void collectParentIds()
    {
        String sql = "SELECT g1.ParentID, g2.RankID FROM geography g1 INNER JOIN geography g2 ON g1.ParentID = g2.GeographyID WHERE g1.GeographyID=?";

        sql = ipadExporter.adjustSQL(sql);
        PreparedStatement pStmt = null;
        try
        {
            pStmt = conn.prepareStatement(sql);

            for (Integer rankId : ranks)
            {
                HashSet<Integer> tempSet = new HashSet<Integer>(rankSets.get(rankId));
                for (Integer recId : tempSet)
                {
                    pStmt.setInt(1, recId);
                    ResultSet rs = pStmt.executeQuery();
                    while (rs != null && rs.next())
                    {
                        Integer parentId = rs.getInt(1);
                        Integer rnkId    = rs.getInt(2);
                        rankSets.get(rnkId).add(parentId);
                    }
                    rs.close();
                }
            }
            
            totalRecords = 0;
            for (Integer rankId : ranks)
            {
                int cnt = rankSets.get(rankId).size();
                System.out.println(String.format("Rank: %d Cnt: %d", rankId, cnt));
            }
            System.out.println(String.format("Total: %d", totalRecords));
            
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (pStmt != null)  pStmt.close();
            } catch (Exception ex2)
            {
                ex2.printStackTrace();
            }
        }

    }

    private void exportTreeData()
    {
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totalRecords);
        }
        
        HashSet<Integer> continentSet = rankSets.get(100);
        HashSet<Integer> countrySet   = rankSets.get(200);
        HashSet<Integer> stateSet     = rankSets.get(300);
        
        PreparedStatement s3Stmt = null;        
        Statement         stmt   = null;
        int transCnt = 0;
        int cnt      = 0;
        try
        {   
            dbS3Conn.setAutoCommit(false);
            String upSQL = "INSERT INTO geo (_id, FullName, ISOCode, RankID, ParentID, TotalCOCnt, NumObjs, HighNodeNum, NodeNum, ContinentId, CountryId, Latitude, Longitude) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
            s3Stmt = dbS3Conn.prepareStatement(upSQL);        
            stmt   = conn.createStatement();
            
            for (Integer rankId : ranks)
            {
                for (Integer recId : rankSets.get(rankId))
                {
                    String sql = String.format("SELECT GeographyID, FullName, GeographyCode, RankID, ParentID, HighestChildNodeNumber, NodeNumber FROM geography WHERE GeographyID = %d", recId);
                    ResultSet rs = stmt.executeQuery(sql); // Get the GeoID and LocID
                    if (rs.next())
                    {
                        int id          = rs.getInt(1);
                        int highNodeNum = rs.getInt(6);
                        int nodeNum     = rs.getInt(7);
                        
                        Integer coTotal = null;
                        if (rankId >= 100 && rankId <= 300)
                        {
                            HashSet<Integer> tempSet = rankSets.get(rankId);
                            if (tempSet.contains(id))
                            {
                                sql = String.format("SELECT SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) CNT FROM collectionobject co " +
                                                    "INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                                                    "INNER JOIN locality l ON ce.LocalityID = l.LocalityID " +
                                                    "INNER JOIN geography g ON l.GeographyID = g.GeographyID " +
                                                    "WHERE co.CollectionID = COLMEMID AND NodeNumber > %d AND HighestChildNodeNumber <= %d", nodeNum, highNodeNum);
                                //System.out.println(sql);
                                coTotal = BasicSQLUtils.getCount(ipadExporter.adjustSQL(sql));
                                //System.out.println(String.format("%s: %s Total: %d", (rankId == 100 ? "Cont" : "Country"), rs.getString(2), coTotal));
                            }
                        }
                        //System.out.println(rankId+" "+id);
                        s3Stmt.setInt(1,    id); // GeographyID
                        s3Stmt.setString(2, rs.getString(2));
                        s3Stmt.setString(3, rs.getString(3));
                        s3Stmt.setInt(4,    rs.getInt(4));
                        s3Stmt.setInt(5,    rs.getInt(5));
                        
                        s3Stmt.setInt(6,    coTotal != null ? coTotal : 0);
                        s3Stmt.setObject(7, null);

                        s3Stmt.setInt(8, highNodeNum);
                        s3Stmt.setInt(9, nodeNum);
                        
                        s3Stmt.setObject(10, null); // ContinentID
                        s3Stmt.setObject(11, null); // CountryId
                        s3Stmt.setObject(12, null); // Lat
                        s3Stmt.setObject(13, null); // Lon
                        
                        if (s3Stmt.executeUpdate() != 1)
                        {
                            System.out.println("Error updating geography: "+id);
                        }
                        transCnt++;
                        cnt++;
                        if (cnt % 10 == 0) 
                        {
                            worker.firePropertyChange(iPadDBExporter.PROGRESS, 0, cnt);
                        }
                    }
                    rs.close();
                }
            }
            
            // Update FamilyID in all Geography Records
            //String            sql    = "SELECT HighestChildNodeNumber, NodeNumber FROM geography WHERE GeographyID = ?";
            PreparedStatement pStmt  = conn.prepareStatement("SELECT NodeNumber, HighestChildNodeNumber FROM geography WHERE GeographyID = ?");
            
            ArrayList<HashSet<Integer>> setsArray = new ArrayList<HashSet<Integer>>();
            setsArray.add(countrySet);
            setsArray.add(continentSet);
            String[] colName = {"CountryId", "ContinentId"};
            int i = 0;
            for (HashSet<Integer> tempSet : setsArray)
            {
                String sql = String.format("UPDATE geo SET %s=? WHERE NodeNum > ? AND HighNodeNum <= ?", colName[i]);
                s3Stmt = dbS3Conn.prepareStatement(sql);
                for (Integer id : tempSet)
                {
                    Object[] row = queryForRow(dbS3Conn, "SELECT _id, FullName FROM geo where _id = "+id);
                    if (row != null)
                    {
                        System.out.println(row[0]+"  "+row[1]);
                    } else
                    {
                        System.out.println("XXX is missing: "+id);
                    }
                    
                    pStmt.setInt(1, id);
                    ResultSet rs = pStmt.executeQuery();
                    if (rs.next())
                    {
                        s3Stmt.setInt(1, id);
                        s3Stmt.setInt(2, rs.getInt(1));
                        s3Stmt.setInt(3, rs.getInt(2));
                        if (s3Stmt.executeUpdate() == 0)
                        {
                            System.out.println(String.format("SELECT _id, RankID,ParentID FROM geo WHERE NodeNum > %d AND HighNodeNum <= %d", rs.getInt(1), rs.getInt(2)));
                            System.out.println(String.format("Error updating geography: %d (%d, %d)", id, rs.getInt(1), rs.getInt(2)));
                        } else 
                        {
                            transCnt++;
                        }
                    }
                }
                i++;
            }
            
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                if (stmt != null) stmt.close();
                if (s3Stmt != null) s3Stmt.close();
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}

        }
    }
}
