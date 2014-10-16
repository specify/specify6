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
import java.util.HashSet;

import javax.swing.SwingWorker;

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
    private ProgressDialog progressDelegate;
    private SwingWorker<Integer, Integer> worker;
    
    private Connection     dbS3Conn;
    private Connection     conn;
    private iPadDBExporter ipadExporter;
    private int            totalRecords;
    
    /**
     * @param ipadExporter
     * @param dbS3Conn
     * @param conn
     * @param colObjToCnt
     */
    public TaxonTreeBuilding(final iPadDBExporter ipadExporter, 
                             final Connection dbS3Conn, 
                             final Connection conn)
    {
        this.dbS3Conn         = dbS3Conn;
        this.conn             = conn;
        this.ipadExporter     = ipadExporter;
        this.progressDelegate = ipadExporter.getProgressDelegate();
        this.worker           = ipadExporter.getWorker();
    }
    
    /**
     * 
     */
    public void process()
    {
        exportTreeData();
    }
    
    /**
     * @param pStmt
     * @param nodenum
     * @param highNodeNum
     * @return
     * @throws SQLException
     */
    private Pair<Integer, Integer> calcTotalForTreeSpan(final PreparedStatement pStmt, 
                                                        final int nodeNum, 
                                                        final int highNodeNum) throws SQLException
    {
        pStmt.setInt(1, nodeNum);
        pStmt.setInt(2, highNodeNum);
        
        int countBelow = 0;
        int countAt    = 0;
        ResultSet rs   = pStmt.executeQuery(); // Get the GeoID and LocID
        while (rs.next())
        {
            int     nn   = rs.getInt(2);
            Integer cnt  = rs.getInt(3);
            if (rs.wasNull()) cnt = null;
            
            if (cnt != null)
            {
                if (nn == nodeNum)
                {
                    countAt += cnt;
                } else
                {
                    countBelow += cnt;
                }
            }
        }
        rs.close();
        
        return new Pair<Integer, Integer>(countBelow, countAt);
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
            s3Stmt.setInt(6,    totalCOCnt != null ? totalCOCnt : 0);
            s3Stmt.setInt(7,    numObjs != null ? numObjs : 0);
            s3Stmt.setInt(8,    nodeNum);
            s3Stmt.setInt(9,    highNodeNum);
            
            int rv = s3Stmt.executeUpdate();
            status = rv == 1;
            if (!status)
            {
                System.out.println("Error updating taxon: "+taxonId);
            }
        } catch (SQLException ex)
        {
            System.err.println("For ID: "+taxonId+" - "+ex.getMessage());
        }
        return status;
    }

    /**
     * 
     */
    private void exportTreeData()
    {
        PreparedStatement s3Stmt      = null;        
        Statement         stmt        = null;
        Statement         stmtGenera  = null;
        PreparedStatement pStmtKids   = null;
        PreparedStatement coCountLookUpStmt = null;
        
        int transCnt = 0;
        int cnt      = 0;
        try
        {   
            dbS3Conn.setAutoCommit(false);
            s3Stmt = dbS3Conn.prepareStatement("INSERT INTO taxon (_id, FullName, RankID, ParentID, FamilyID, TotalCOCnt, NumObjs, NodeNum, HighNodeNum) VALUES (?,?,?,?,?,?,?,?,?)");        
            stmt   = conn.createStatement();
            stmtGenera = conn.createStatement();
            
            String coLookUp = ipadExporter.adjustSQL("SELECT co.CollectionObjectID, t.NodeNumber, i.NewID FROM taxon t " +
                                                     "INNER JOIN determination d ON t.TaxonID = d.TaxonID " +
                                                     "INNER JOIN collectionobject co ON co.CollectionObjectID = d.CollectionObjectID " +
                                                     "INNER JOIN ios_colobjcnts i ON co.CollectionObjectID = i.OldID " +
                                                     "WHERE co.CollectionID = COLMEMID AND d.IsCurrent = TRUE AND t.NodeNumber >= ? AND t.NodeNumber <= ?");
            System.out.println(coLookUp);
            coCountLookUpStmt = conn.prepareStatement(coLookUp);
            
            String kidsSQL   = "SELECT co.CollectionObjectID, t.TaxonID, t.FullName, t.RankID, t.ParentID, t.NodeNumber, t.HighestChildNodeNumber FROM taxon t " +
                               "INNER JOIN determination d ON t.TaxonID = d.TaxonID " +
                               "INNER JOIN collectionobject co ON co.CollectionObjectID = d.CollectionObjectID " +
                               "WHERE co.CollectionID = COLMEMID AND d.IsCurrent = TRUE AND NodeNumber >= ? AND NodeNumber <= ? ORDER BY t.TaxonID";
            
            String preSQL    = "SELECT t.TaxonID, t.FullName, t.RankID, t.ParentID, t.NodeNumber, t.HighestChildNodeNumber FROM taxon t ";
            
            String familySQL = preSQL + ipadExporter.adjustSQL("WHERE t.TaxonTreeDefID = TAXTREEDEFID AND RankID = 140");
            
            String extraJoins = "LEFT OUTER JOIN taxon p1 ON t.ParentID = p1.TaxonID " +
                                "LEFT OUTER JOIN taxon p2 ON p1.ParentID = p2.TaxonID " +
                                "LEFT OUTER JOIN taxon p3 ON p2.ParentID = p3.TaxonID " +
                                "LEFT JOIN taxon p4 ON p3.ParentID = p4.TaxonID ";
            
            //String generaSQL = preSQL + extraJoins + ipadExporter.adjustSQL(" WHERE t.TaxonTreeDefID = TAXTREEDEFID AND t.RankID = 180 AND (t.ParentID = %d OR p1.ParentID = %d OR p2.ParentID = %d OR p3.ParentID = %d OR p4.ParentID = %d)");
            String generaSQL = preSQL + extraJoins + ipadExporter.adjustSQL(" WHERE t.TaxonTreeDefID = TAXTREEDEFID AND t.RankID = 180 AND t.ParentID = %d");
            
            
            pStmtKids = conn.prepareStatement(ipadExporter.adjustSQL(kidsSQL));
            
            String cntSQL = ipadExporter.adjustSQL("SELECT COUNT(*) FROM taxon t WHERE t.TaxonTreeDefID = TAXTREEDEFID AND t.RankID = 140");
            totalRecords  = BasicSQLUtils.getCountAsInt(cntSQL);
            int percentInx = (int)Math.round((float)totalRecords *0.01f);
            if (progressDelegate != null)
            {
                progressDelegate.setDesc("Calculating Taxonomic Counts...");
                progressDelegate.setProcess(0, 100);
                progressDelegate.setProcessPercent(true);
            }
            
            //familySQL = "SELECT t.TaxonID, t.FullName, t.RankID, t.ParentID, t.NodeNumber, t.HighestChildNodeNumber FROM taxon t WHERE t.TaxonID = 369"; // Chiasmodontidae

            
            //long startTime = System.currentTimeMillis();
            int prevPercent = 0;
            //-----------------------
            // Loop thru Families
            //-----------------------
            ResultSet rsFamily = stmt.executeQuery(familySQL); // Get the GeoID and LocID
            while (rsFamily.next())
            {
                System.out.println("Family TaxonID: "+rsFamily.getInt(1));
                
                int taxonIdFamily     = rsFamily.getInt(1); 
                String fullNameFamily = rsFamily.getString(2);
                int rankIdFamily      = rsFamily.getInt(3);
                int parentIdFamily    = rsFamily.getInt(4);
                int nodeNumFamily     = rsFamily.getInt(5);
                int highNodeNumFamily = rsFamily.getInt(6);
                
                Pair<Integer, Integer> counts = calcTotalForTreeSpan(coCountLookUpStmt, nodeNumFamily, highNodeNumFamily);
                if (counts.first == 0 && counts.second == 0)
                {
                    continue;
                }
                writeTaxon(s3Stmt, taxonIdFamily, fullNameFamily, rankIdFamily, parentIdFamily, null, counts.first, counts.second, nodeNumFamily, highNodeNumFamily);
                
                HashSet<Integer> usedRankSet = new HashSet<Integer>();
                //-----------------------
                // Loop thru Genera 
                //-----------------------
                //for (Integer traversalRankId : ranks)
                {
                    //System.out.println("Genera Rank: "+traversalRankId);
                    
                    String gSQL = String.format(generaSQL, taxonIdFamily);//, taxonIdFamily, taxonIdFamily, taxonIdFamily, taxonIdFamily);
                    //System.out.println(gSQL);
                    //gSQL = "SELECT t.TaxonID, t.FullName, t.RankID, t.ParentID, t.NodeNumber, t.HighestChildNodeNumber FROM taxon t WHERE t.TaxonID = 4918"; // Kali

                    ResultSet rs = stmtGenera.executeQuery(gSQL);
                    while (rs.next())
                    {
                        int taxonIdGenera = rs.getInt(1); // Genera TaxonID
                        if (usedRankSet.contains(taxonIdGenera))
                        {
                            continue;
                        }
                        //System.out.println("Genera TaxonID: "+taxonIdGenera);
                        
                        usedRankSet.add(taxonIdGenera);
                        
                        String fullNameGenera = rs.getString(2);
                        int rankIdGenera      = rs.getInt(3);
                        int parentIdGenera    = rs.getInt(4);
                        int nodeNumGenera     = rs.getInt(5);
                        int highNodeNumGenera = rs.getInt(6);
    
                        counts = calcTotalForTreeSpan(coCountLookUpStmt, nodeNumGenera, highNodeNumGenera);
                        if (counts.first == 0 && counts.second == 0)
                        {
                            continue;
                        }
                        writeTaxon(s3Stmt, taxonIdGenera, fullNameGenera, rankIdGenera, parentIdGenera, taxonIdFamily, counts.first, counts.second, nodeNumGenera, highNodeNumGenera);
    
                        pStmtKids.setInt(1, nodeNumGenera);
                        pStmtKids.setInt(2, highNodeNumGenera);
                        
                        //---------------------------------
                        // Loop thru all children of Genera
                        //---------------------------------
                        int prevTx = -1;
                        int txId   = -1;
                        ResultSet rsLK = pStmtKids.executeQuery();
                        while (rsLK.next())
                        {
                            txId = rsLK.getInt(2);
                            
                            // Write Children of Genera
                            if (txId != prevTx)
                            {
                                String fullName = rsLK.getString(3);
                                int rankId      = rsLK.getInt(4);
                                int parentId    = rsLK.getInt(5);
                                int nodeNN      = rsLK.getInt(6);
                                int highNN      = rsLK.getInt(7);
                                
//                                counts = calcTotalForTreeSpan(coCountLookUpStmt, nodeNN, highNN);
//                                if (counts.first == 0 && counts.second == 0)
//                                {
//                                    continue;
//                                }
//                                System.out.println("Taxon: "+txId+" CO: "+rsLK.getInt(1));
                                writeTaxon(s3Stmt, txId, fullName, rankId, parentId, taxonIdFamily, 1, 1, nodeNN, highNN);
                                prevTx = txId;
                            }
                        }
                        rsLK.close();
                    }
                    rs.close();
                }
                cnt++;
                if (cnt % percentInx == 0) 
                {
                    int percent = Math.max((int)(((float)cnt / (float)totalRecords) * 100.0f), 100);
                    if (percent != prevPercent)
                    {
                        //System.out.println(String.format("Elapsed: %5.2f - %d", ((float)(System.currentTimeMillis() - startTime) / 1000.0f), percent));
                        worker.firePropertyChange(iPadDBExporter.PROGRESS, 0, percent);
                        prevPercent = percent;
                    }
                }
            }
            stmtGenera.close();
            rsFamily.close();
            
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
                if (pStmtKids != null) pStmtKids.close();
                if (coCountLookUpStmt != null) coCountLookUpStmt.close();
                
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}

        }
    }
}
