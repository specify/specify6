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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.SwingWorker;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.IdMapperIFace;
import edu.ku.brc.ui.ProgressDialog;

public class TaxonTreeBuilding
{
    private ProgressDialog progressDelegate;
    private SwingWorker<Integer, Integer> worker;
    
    private Connection     dbS3Conn;
    private Connection     conn;
    private iPadDBExporter ipadExporter;
    private HashMap<Integer, HashSet<Integer>> rankSets = new HashMap<Integer, HashSet<Integer>>();
    
    private int            totalRecords;
    private int            minRank = 139;
    private IdMapperIFace  colObjToCnt;
    
    private Vector<Integer> ranks;
    
    public TaxonTreeBuilding(final iPadDBExporter ipadExporter, 
                             final Connection dbS3Conn, 
                             final Connection conn,
                             final IdMapperIFace colObjToCnt)
    {
        this.dbS3Conn         = dbS3Conn;
        this.conn             = conn;
        this.ipadExporter     = ipadExporter;
        this.progressDelegate = ipadExporter.getProgressDelegate();
        this.worker           = ipadExporter.getWorker();
        this.colObjToCnt      = colObjToCnt;
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
        String sql = "SELECT ttd.RankID FROM taxontreedefitem ttd WHERE ttd.TaxonTreeDefID = TAXTREEDEFID ORDER BY ttd.RankID DESC ";
       ranks = BasicSQLUtils.queryForInts(conn, ipadExporter.adjustSQL(sql));
    }

    private void buildRankSets()
    {
        String sql = "SELECT t.TaxonID FROM collectionobject co INNER JOIN determination d ON co.CollectionObjectID = d.CollectionObjectID " +
                     "INNER JOIN taxon t ON d.TaxonID = t.TaxonID WHERE d.IsCurrent = TRUE AND TaxonTreeDefID = TAXTREEDEFID AND RankID = ? ";
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
        String sql = "SELECT t1.ParentID, t2.RankID FROM taxon t1 INNER JOIN taxon t2 ON t1.ParentID = t2.TaxonID WHERE t1.TaxonID=?";

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
                if (rankId > minRank)
                {
                    totalRecords += cnt;
                }
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
        
        HashSet<Integer> familySet = rankSets.get(140);
        
        PreparedStatement s3Stmt = null;        
        Statement         stmt   = null;
        PreparedStatement pStmtLK = null;
        
        int transCnt = 0;
        int cnt      = 0;
        try
        {   
            dbS3Conn.setAutoCommit(false);
            s3Stmt = dbS3Conn.prepareStatement("INSERT INTO taxon (_id, FullName, RankID, ParentID, FamilyID, TotalCOCnt, NumObjs, HighNodeNum, NodeNum) VALUES (?,?,?,?,?,?,?,?,?)");        
            stmt   = conn.createStatement();
            String selSQL = "SELECT co.CollectionObjectID, t.NodeNumber FROM taxon t " +
                            "INNER JOIN determination d ON t.TaxonID = d.TaxonID " +
                            "INNER JOIN collectionobject co ON co.CollectionObjectID = d.CollectionObjectID " +
                            "WHERE co.CollectionID = COLMEMID AND d.IsCurrent = TRUE AND NodeNumber >= ? AND NodeNumber <= ?";
            pStmtLK  = conn.prepareStatement(ipadExporter.adjustSQL(selSQL));
            
            for (Integer rankId : ranks)
            {
                for (Integer recId : rankSets.get(rankId))
                {
                    String sql = String.format("SELECT TaxonID, FullName, RankID, ParentID, HighestChildNodeNumber, NodeNumber FROM taxon WHERE RankID > %d AND TaxonID = %d", minRank, recId);
                    ResultSet rs = stmt.executeQuery(sql); // Get the GeoID and LocID
                    if (rs.next())
                    {
                        int id          = rs.getInt(1);
                        int highNodeNum = rs.getInt(5);
                        int nodeNum     = rs.getInt(6);
                        
                        Integer coTotal = null; // Total count of this level and all below
                        Integer coCount = null; // Count of current level
                        
                        if (rankId == 140 && familySet.contains(id))
                        {
                            int totCnt = 0;
                            pStmtLK.setInt(1, nodeNum);
                            pStmtLK.setInt(2, highNodeNum);
                            
                            ResultSet rsLK = pStmtLK.executeQuery();
                            while (rsLK.next())
                            {
                                Integer coCnt = colObjToCnt.get(rsLK.getInt(1));
                                if (coCnt != null)
                                {
                                    totCnt += coCnt;
                                    int nn = rsLK.getInt(2);
                                    if (nn == nodeNum)
                                    {
                                        coCount = coCnt;
                                    }
                                }
                                
                            }
                            if (totCnt > 0)
                            {
                                coTotal = totCnt;
                            }
                        }
                        //System.out.println(rankId+" "+id);
                        s3Stmt.setInt(1,    id); // TaxonID
                        s3Stmt.setString(2, rs.getString(2));
                        s3Stmt.setInt(3,    rs.getInt(3));
                        s3Stmt.setInt(4,    rs.getInt(4));
                        s3Stmt.setObject(5, null);
                        s3Stmt.setInt(6,    coTotal != null ? coTotal : 0);
                        s3Stmt.setInt(6,    coCount != null ? coCount : 0);
                        
                        s3Stmt.setInt(8, highNodeNum);
                        s3Stmt.setInt(9, nodeNum);
                        
                        if (s3Stmt.executeUpdate() != 1)
                        {
                            System.out.println("Error updating taxon: "+id);
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
            s3Stmt.close();
            
            // Update FamilyID in all Taxon Records
            //String            sql    = "SELECT HighestChildNodeNumber, NodeNumber FROM taxon WHERE TaxonID = ?";
            PreparedStatement pStmt  = conn.prepareStatement("SELECT NodeNumber, HighestChildNodeNumber FROM taxon WHERE TaxonID = ?");
            s3Stmt = dbS3Conn.prepareStatement("UPDATE taxon SET FamilyID=? WHERE NodeNum > ? AND HighNodeNum <= ?");
            
            for (Integer id : familySet)
            {
                Object[] row = queryForRow(dbS3Conn, "SELECT _id, FullName FROM taxon where _id = "+id);
                if (row != null)
                {
                    System.out.println(row[0]+"  "+row[1]);
                } else
                {
                    System.out.println("Family is missing: "+id);
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
                        System.out.println(String.format("SELECT _id, RankID,ParentID FROM taxon WHERE NodeNum > %d AND HighNodeNum <= %d", rs.getInt(1), rs.getInt(2)));
                        System.out.println(String.format("Error updating taxon: %d (%d, %d)", id, rs.getInt(1), rs.getInt(2)));
                    } else 
                    {
                        transCnt++;
                    }
                }
            }
            pStmt.close();
            s3Stmt.close();
            
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
                if (pStmtLK != null) pStmtLK.close();
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}

        }
    }
}
