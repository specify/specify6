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

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 11, 2014
 *
 */
public class TaxonTreeBuilding
{
    private static final Logger  log = Logger.getLogger(TaxonTreeBuilding.class);
    
    final int secsInHours = 3600;
    final int secInMin    = 60;

    private ProgressDialog progressDelegate;
    
    private Connection     dbS3Conn;
    private Connection     conn;
    private iPadDBExporter ipadExporter;
    
    private HashMap<Integer, HashMap<Integer, TreeNode>> taxonHash = new HashMap<Integer, HashMap<Integer, TreeNode>>();
    private ArrayList<TreeNode> nodeList = new ArrayList<TreeNode>();
    private Vector<Integer>     treeRanks = null;
    private PreparedStatement   lookUpParentStmt = null;
    private boolean             skipNodeList = false;
    
    /**
     * @param ipadExporter
     * @param dbS3Conn
     * @param conn
     */
    public TaxonTreeBuilding(final iPadDBExporter ipadExporter, 
                             final Connection dbS3Conn, 
                             final Connection conn)
    {
        this.dbS3Conn         = dbS3Conn;
        this.conn             = conn;
        this.ipadExporter     = ipadExporter;
        this.progressDelegate = ipadExporter.getProgressDelegate();
    }
    
    /**
     * 
     */
    public void process()
    {
        exportTreeData();
    }
    
    /**
     * @param s3Stmt
     * @param taxonId
     * @param fullName
     * @param rankId
     * @param parentId
     * @param familyId
     * @param totalCOCnt
     * @param numObjs
     * @param nodeNum
     * @param highNodeNum
     * @return
     * @throws SQLException
     */
    private boolean writeTaxon(final PreparedStatement s3Stmt, 
                               final int     taxonId, 
                               final String  fullName, 
                               final int     rankId, 
                               final Integer parentId, 
                               final Integer familyId, 
                               final Integer genusId, 
                               final Integer totalCOCnt, 
                               final Integer numObjs, 
                               final int     nodeNum,
                               final int     highNodeNum)
    {
        boolean status = false;
        try
        {
            s3Stmt.setInt(1,    taxonId); // TaxonID
            s3Stmt.setString(2, fullName);
            s3Stmt.setInt(3,    rankId);
            s3Stmt.setInt(4,    parentId);
            s3Stmt.setObject(5, familyId);
            s3Stmt.setObject(6, genusId);
            s3Stmt.setInt(7,    totalCOCnt != null ? totalCOCnt : 0);
            s3Stmt.setInt(8,    numObjs != null ? numObjs : 0);
            s3Stmt.setInt(9,    nodeNum);
            s3Stmt.setInt(10,    highNodeNum);
            
            int rv = s3Stmt.executeUpdate();
            status = rv == 1;
            if (!status)
            {
                log.error("Error updating taxon: "+taxonId);
            }
        } catch (SQLException ex)
        {
            log.error("For ID: "+taxonId+" - "+ex.getMessage());
        }
        return status;
    }
     
    /**
     * @param taxonId
     * @param rankId
     * @param parentId
     * @param parentRankId
     * @return
     */
    private TreeNode getNode(final int taxonId,
                             final int rankId,
                             final Integer parentId,
                             final int parentRankId)
    {
        HashMap<Integer, TreeNode> nodeHash = taxonHash.get(rankId);
        if (nodeHash == null)
        {
            nodeHash = new HashMap<Integer, TreeNode>();
            taxonHash.put(rankId, nodeHash);
        }
        
        TreeNode pNode = nodeHash.get(taxonId);
        if (pNode == null)
        {
            pNode = new TreeNode(taxonId, rankId, parentId, parentRankId);
            nodeHash.put(taxonId, pNode);
            if (!skipNodeList)
            {
                nodeList.add(pNode);
            }
        }
        return pNode;
    }

    
    /**
     * @param node
     * @param famGen
     * @throws SQLException
     */
    private void buildTree(final TreeNode node, Pair<Integer, Integer> famGen) throws SQLException
    {
        //System.out.println(String.format("%d\t%d\t%d\t%d", node.taxonId, node.rankId, node.parentId, node.parentRankId));
        TreeNode parent = getNode(node.parentId, node.parentRankId, null, 0);
        
        if (node.rankId == 140)
        {
            famGen.first = node.taxonId;
            node.familyId = node.taxonId;
            return;
        }
        
        if (node.rankId == 180)
        {
            famGen.second = node.taxonId;
        }
        
        if (node.visited)
        {
            famGen.first = node.familyId;
            famGen.second = node.genusId;
            return;
        }
        
        node.visited = true;
        if (parent.parentId == null)
        {
            lookUpParentStmt.setInt(1, node.parentId);
            ResultSet rs = lookUpParentStmt.executeQuery();
            if (rs.next())
            {
                parent.parentId     = rs.getInt(1);
                parent.parentRankId = rs.getInt(2);

            } else
            {
                return;
            }
            rs.close();
        }
        
        buildTree(parent, famGen);

        node.familyId = famGen.first;
        node.genusId  = famGen.second;
    }
    
    /**
     * 
     */
    static int fileCnt = 0;
    private void dumpTree()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("TaxonID\tName\tTotal\tCount\tRankID\tFamily\tGenus\tVisited\n");
        for (Integer rankID : treeRanks)
        {
            HashMap<Integer, TreeNode> pNodeHash = taxonHash.get(rankID);
            if (pNodeHash != null)
            {
                for (TreeNode pNode : pNodeHash.values())
                {
                    String name = BasicSQLUtils.querySingleObj("SELECT FullName from taxon WHERE TaxonID = "+pNode.taxonId);
                    sb.append(String.format("%d\t%s\t%d\t%d\t%d\t%d\t%d\t%s\n",  
                            pNode.taxonId, name, pNode.totalCount, pNode.nodeCount, pNode.rankId, pNode.familyId, pNode.genusId, pNode.visited?"Y":"N"));
                }
            }
        }
        System.out.println(sb.toString()+"----------------------------------------------");
        
        try
        {
            String fileName = String.format("/Users/rods/dumps/dump_%d.tab", fileCnt++);
            PrintWriter pw = new PrintWriter(fileName);
            pw.println(sb.toString());
            pw.flush();
            pw.close();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * 
     */
    private void exportTreeData()
    {
        PreparedStatement s3Update     = null;        
        PreparedStatement s3Update2    = null; 
        PreparedStatement s3Update3    = null; 
        PreparedStatement txLookUpStmt = null;
        Statement         stmt         = null;
        
        progressDelegate.setProcessPercent(true);
        progressDelegate.setProcess(0, 100);
        progressDelegate.setDesc("Exporting Taxonomic Information");

        int transCnt = 0;
        try
        {
            String treeRanksSQL = ipadExporter.adjustSQL("SELECT RankID FROM taxontreedefitem WHERE RankID > 139 AND TaxonTreeDefID = TAXTREEDEFID ORDER BY RankID DESC");
            treeRanks = BasicSQLUtils.queryForInts(treeRanksSQL);

            // SQLite
            dbS3Conn.setAutoCommit(false);
            s3Update  = dbS3Conn.prepareStatement("INSERT INTO taxon (_id, FullName, RankID, ParentID, FamilyID, GenusID, TotalCOCnt, NumObjs, NodeNum, HighNodeNum) VALUES (?,?,?,?,?,?,?,?,?,?)");        
            s3Update2 = dbS3Conn.prepareStatement("UPDATE taxon SET FamilyID=?, GenusID=?, TotalCOCnt=?, NumObjs=? WHERE _id = ?");  
            s3Update3 = dbS3Conn.prepareStatement("UPDATE taxon SET ParentID=? WHERE _id = ?");  
            
            // MySQL
            stmt    = conn.createStatement();
            lookUpParentStmt = conn.prepareStatement("SELECT tp.TaxonId, tp.RankID FROM taxon t INNER JOIN taxon tp ON t.ParentID = tp.TaxonID WHERE t.TaxonID = ?");
            
            String coLookUp = ipadExporter.adjustSQL("SELECT co.CollectionObjectID, t.TaxonID, t.FullName, t.RankID, t.ParentID, t.NodeNumber, t.HighestChildNodeNumber, ios_colobjcnts.NewID, tp.RankID " +
            		                                 "FROM collectionobject co " +
                                                     "INNER JOIN determination d ON co.CollectionObjectID = d.CollectionObjectID " +
                                                     "INNER JOIN taxon t ON d.TaxonID = t.TaxonID " +
                                                     "INNER JOIN taxon tp ON t.ParentID = tp.TaxonID " +
                                                     "INNER JOIN ios_colobjcnts ON co.CollectionObjectID = ios_colobjcnts.OldID " +
                                                     "WHERE co.CollectionID = COLMEMID AND d.IsCurrent = TRUE ORDER BY t.RankID DESC, t.TaxonID ASC");
            //log.error(coLookUp);
            
            
            //---------------------------------------
            // Loop thru Collection Objects
            //---------------------------------------
            int countAmtTotal = 0;
            boolean firstTime = true;
            int prevTaxonID   = -1;
            
            int taxonID;
            String fullName   = null;
            int rankId        = 0;
            int parentId      = 0;
            int nodeNum       = 0;
            int highNodeNum   = 0;
            int colObjCntAmt  = 0;
            int taxonParentRankID = 0;
            Integer familyId  = null;
            
            ResultSet rs = stmt.executeQuery(coLookUp); 
            while (rs.next())
            {
                // co.CollectionObjectID, t.TaxonID, t.FullName, t.RankID, t.ParentID, t.NodeNumber, t.HighestChildNodeNumber, ios_colobjcnts.NewID, tp.RankID
                //int colObjId          = rsFamily.getInt(1); 
                taxonID = rs.getInt(2); 
                
                if (firstTime)
                {
                     firstTime = false;
                } else if (taxonID != prevTaxonID)
                {
                    writeTaxon(s3Update, prevTaxonID, fullName, rankId, parentId, familyId, null, 0, countAmtTotal, nodeNum, highNodeNum);
                    TreeNode pNode = getNode(prevTaxonID, rankId, parentId, taxonParentRankID); 
                    pNode.written = true;
                    pNode.nodeCount += countAmtTotal;
                    countAmtTotal = 0;
                }
                prevTaxonID       = taxonID;
                
                fullName          = rs.getString(3);
                rankId            = rs.getInt(4);
                parentId          = rs.getInt(5);
                nodeNum           = rs.getInt(6);
                highNodeNum       = rs.getInt(7);
                colObjCntAmt      = rs.getInt(8);
                taxonParentRankID = rs.getInt(9);
                
                //Integer gpParentId    = rs.getInt(10);
                //if (rs.wasNull()) gpParentId = null;
                //int gpRankId          = rs.getInt(11);
                
                familyId = taxonParentRankID == 140 ? parentId : null;
                
                countAmtTotal += colObjCntAmt;
            }
            
            writeTaxon(s3Update, prevTaxonID, fullName, rankId, parentId, familyId, null, 0, colObjCntAmt, nodeNum, highNodeNum);
            TreeNode ppNode = getNode(prevTaxonID, rankId, parentId, taxonParentRankID);  // Parent
            ppNode.nodeCount += countAmtTotal;
            ppNode.written = true;
            
            progressDelegate.setProcess(20);

            rs.close();
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
            
            if (false)
            {
                System.out.println("_id\tFullName\tRankID\tParentID\tTotalCOCnt\tNumObjs");
                Statement stmt1 = dbS3Conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery("SELECT _id, FullName, RankID, ParentID, TotalCOCnt, NumObjs FROM taxon ORDER BY RankID");
                while (rs1.next())
                {
                   for (int i=1;i<7;i++)
                   {
                       System.out.print(rs1.getObject(i)+"\t");
                   }
                   System.out.println();
                }
                rs1.close();
                stmt1.close();
            
                dumpTree();
            }
            
            skipNodeList = true;
            
            for (TreeNode node: nodeList)
            {
                Pair<Integer, Integer> famGen = new Pair<Integer, Integer>(0, 0);
                buildTree(node, famGen);
                node.familyId = famGen.first;
                node.genusId  = famGen.second;
            }

            progressDelegate.setProcess(40);

            //dumpTree();
            
            //System.out.println("ID\tRK\tTC\tNC\tPID\tPRK\tPTID\tPTRNK\tTC\tTC2");
            for (Integer rankID : treeRanks)
            {
                HashMap<Integer, TreeNode> pNodeHash = taxonHash.get(rankID);
                if (pNodeHash != null)
                {
                    for (TreeNode pNode : pNodeHash.values())
                    {
                        if (pNode.rankId < 180) pNode.genusId = 0;
                        
                        if (pNode.parentId != null)
                        {
                            TreeNode parent = getNode(pNode.parentId, pNode.parentRankId, 0, 0);
                            //System.out.println(String.format("%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d", pNode.taxonId, pNode.rankId, pNode.totalCount, pNode.nodeCount,  pNode.parentId, pNode.parentRankId,
                            //                                                                   parent.taxonId, parent.rankId,  parent.totalCount, (parent.totalCount + pNode.nodeCount + pNode.totalCount)));
                            parent.totalCount += pNode.nodeCount + pNode.totalCount;
                        }
                    }
                }
            }
            
            //dumpTree();
              
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
            
            progressDelegate.setProcess(60);
            
/*
 * 
`_id` INTEGER PRIMARY KEY, 
`FullName` TEXT,
`RankID` INTEGER,
`ParentID` INTEGER,
`FamilyID` INTEGER,
`GenusID` INTEGER,
`TotalCOCnt` INTEGER,
`NumObjs` INTEGER,
`NodeNum` INTEGER,
`HighNodeNum` INTEGER
 */
            String txLookUp = ipadExporter.adjustSQL("SELECT FullName, NodeNumber, HighestChildNodeNumber FROM taxon WHERE TaxonTreeDefID = TAXTREEDEFID AND TaxonID = ?");
            txLookUpStmt = conn.prepareStatement(txLookUp);
            
            //s3Stmt = dbS3Conn.prepareStatement("UPDATE taxon SET TotalCOCnt=? WHERE _id = ?");
            int totCnt = 0;
            for (Integer rankID : treeRanks)
            {
                HashMap<Integer, TreeNode> pNodeHash = taxonHash.get(rankID);
                if (pNodeHash != null)
                {
                    totCnt += pNodeHash.size();
                }
            }
            
            ////////////////////////////////
            // Remove extra Levels
            ////////////////////////////////
            
            for (Integer rankID : treeRanks)
            {
                HashMap<Integer, TreeNode> pNodeHash = taxonHash.get(rankID);
                if (pNodeHash != null)
                {
                    for (TreeNode pNode : pNodeHash.values())
                    {
                        if (pNode.parentRankId > 140 && pNode.parentRankId < 180)
                        {
                            TreeNode familyNode = getFamilyNode(pNode);
                            if (familyNode != null)
                            {
                                pNode.parentId     = familyNode.taxonId;
                                pNode.parentRankId = familyNode.rankId;
                                if (pNode.written)
                                {
                                    s3Update3.setInt(1, familyNode.taxonId);
                                    s3Update3.setInt(2, pNode.taxonId);
                                    int rv = s3Update3.executeUpdate();
                                    if (rv != 1)
                                    {
                                        log.error("Error updating taxon parentID  TaxonID: "+pNode.taxonId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            
            
            // Write the rest of the taxon nodes
            progressDelegate.setProcess(80);

            int cnt = 0;
            for (Integer rankID : treeRanks)
            {
                HashMap<Integer, TreeNode> pNodeHash = taxonHash.get(rankID);
                if (pNodeHash != null)
                {
                    for (TreeNode pNode : pNodeHash.values())
                    {
                        if (pNode.rankId > 140 && pNode.rankId < 180)
                        {
                            if (pNode.nodeCount == 0)
                            {
                                if (pNode.written)
                                {
                                    int rv = BasicSQLUtils.update("DELETE FROM taxon WHERE _id = " + pNode.taxonId);
                                    if (rv != 1)
                                    {
                                        log.error("Error removing unneed parent -  TaxonID: "+pNode.taxonId);
                                    }
                                }
                                continue;
                            }
                        }
                        
                        if (pNode.written)
                        {
                            s3Update2.setInt(1, pNode.familyId);
                            s3Update2.setInt(2, pNode.genusId);
                            s3Update2.setInt(3, pNode.totalCount);
                            s3Update2.setInt(4, pNode.nodeCount);
                            s3Update2.setInt(5, pNode.taxonId);
                            int rv = s3Update2.executeUpdate();
                            if (rv != 1)
                            {
                                log.error("Error updating taxon: "+pNode.taxonId);
                            }
                        } else
                        {
                            txLookUpStmt.setInt(1, pNode.taxonId);
                            rs = txLookUpStmt.executeQuery();
                            if (rs.next())
                            {
                                fullName          = rs.getString(1);
                                nodeNum           = rs.getInt(2);
                                highNodeNum       = rs.getInt(3);
                                if (pNode.parentId != null)
                                {
                                    writeTaxon(s3Update, pNode.taxonId, fullName, pNode.rankId, pNode.parentId, pNode.familyId, pNode.genusId, pNode.totalCount, pNode.nodeCount, nodeNum, highNodeNum);
                                }
                            } else
                            {
                                log.error("Error looking up taxon: "+pNode.taxonId);
                            }
                            rs.close();
                        }
                        cnt++;
                        if (cnt % 100 == 0)
                        {
                            System.out.println(String.format("%d / %d", cnt, totCnt));
                        }
                    }
                }
            }
            
            progressDelegate.setProcess(100);
            
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
                if (s3Update != null) s3Update.close();
                if (s3Update2 != null) s3Update2.close();
                if (s3Update3 != null) s3Update3.close();
                if (txLookUpStmt != null) txLookUpStmt.close();
                
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}

        }
    }
    
    private TreeNode getFamilyNode(final TreeNode node)
    {
        HashMap<Integer, TreeNode> treeLevel = taxonHash.get(node.parentRankId);
        if (treeLevel != null)
        {
            TreeNode parentNode = treeLevel.get(node.parentId);
            if (parentNode != null)
            {
                if (parentNode.rankId == 140)
                {
                    return parentNode;
                }
                return getFamilyNode(parentNode);
            }
        }
        log.debug("Error searching for Family Node - Can't find parent for node Id: " + node.taxonId);
        return null;
    }
    
    
    class TreeNode
    {
        int     taxonId;
        int     rankId;
        int     totalCount;
        int     nodeCount;
        Integer parentId;
        int     parentRankId;
        boolean visited;
        boolean written;
        
        int     familyId;
        int     genusId;
        
        /**
         * @param taxonId
         * @param totalCount
         */
        public TreeNode(int taxonId, 
                        int rankId,
                        Integer parentId,
                        int parentRankId)
        {
            super();
            this.taxonId      = taxonId;
            this.rankId       = rankId;
            this.totalCount   = 0;
            this.nodeCount    = 0;
            this.parentId     = parentId;
            this.parentRankId = parentRankId;
            this.visited      = false;
            this.written      = false;
        }
        
    }
}
