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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

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
public class GeographyTreeBuilder
{
    private ProgressDialog progressDelegate;
    
    private Connection     dbS3Conn;
    private Connection     conn;
    private iPadDBExporter ipadExporter;
    
    private HashMap<Integer, HashMap<Integer, GeoTreeNode>> geoHash = new HashMap<Integer, HashMap<Integer, GeoTreeNode>>();
    private ArrayList<GeoTreeNode> nodeList = new ArrayList<GeoTreeNode>();
    private Vector<Integer>        treeRanks = null;
    private PreparedStatement      lookUpParentStmt = null;
    private boolean                skipNodeList = false;
    
    /**
     * @param ipadExporter
     * @param dbS3Conn
     * @param conn
     * @param colObjToCnt
     */
    public GeographyTreeBuilder(final iPadDBExporter ipadExporter, 
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
     * @param geoId
     * @param fullName
     * @param geoName
     * @param rankId
     * @param parentId
     * @param continentId
     * @param countryId
     * @param totalCOCnt
     * @param numObjs
     * @param nodeNum
     * @param highNodeNum
     * @return
     */
    private boolean writeGeo(final PreparedStatement s3Stmt, 
                             final int     geoId, 
                             final String  fullName, 
                             final String  geoName, 
                             final int     rankId, 
                             final Integer parentId, 
                             final Integer continentId, 
                             final Integer countryId, 
                             final Integer totalCOCnt, 
                             final Integer numObjs, 
                             final int     nodeNum,
                             final int     highNodeNum,
                             final String isoCode,
                             final Double lat,
                             final Double lon)
    {
        boolean status = false;
        try
        {
            s3Stmt.setInt(1,    geoId); // GeographyID
            s3Stmt.setString(2,  fullName);
            s3Stmt.setString(3,  geoName);
            s3Stmt.setInt(4,     rankId);
            s3Stmt.setInt(5,     parentId);
            s3Stmt.setObject(6,  continentId);
            s3Stmt.setObject(7,  countryId);
            s3Stmt.setInt(8,     totalCOCnt != null ? totalCOCnt : 0);
            s3Stmt.setInt(9,     numObjs != null ? numObjs : 0);
            s3Stmt.setInt(10,    nodeNum);
            s3Stmt.setInt(11,    highNodeNum);
            s3Stmt.setString(12, isoCode);
            
            if (lat != null)
            {
                s3Stmt.setDouble(13, lat);
            } else
            {
                s3Stmt.setObject(13, null);
            }
            if (lon != null)
            {
                s3Stmt.setDouble(14, lon);
            } else
            {
                s3Stmt.setObject(14, null);
            }
            
            int rv = s3Stmt.executeUpdate();
            status = rv == 1;
            if (!status)
            {
                System.out.println("Error updating geo: "+geoId);
            }
        } catch (SQLException ex)
        {
            System.err.println("For ID: "+geoId+" - "+ex.getMessage());
        }
        return status;
    }
     
    /**
     * @param geoId
     * @param rankId
     * @param parentId
     * @param parentRankId
     * @return
     */
    private GeoTreeNode getNode(final int geoId,
                                final int rankId,
                                final Integer parentId,
                                final int parentRankId,
                                final String isoCode,
                                final Double latitude,
                                final Double longitude)
    {
        HashMap<Integer, GeoTreeNode> nodeHash = geoHash.get(rankId);
        if (nodeHash == null)
        {
            nodeHash = new HashMap<Integer, GeoTreeNode>();
            geoHash.put(rankId, nodeHash);
        }
        
        GeoTreeNode pNode = nodeHash.get(geoId);
        if (pNode == null)
        {
            pNode = new GeoTreeNode(geoId, rankId, parentId, parentRankId, isoCode, latitude, longitude);
            nodeHash.put(geoId, pNode);
            if (!skipNodeList)
            {
                nodeList.add(pNode);
            }
        }
        return pNode;
    }

    
    /**
     * @param node
     * @param contCountry
     * @throws SQLException
     */
    private void buildTree(final GeoTreeNode node, Pair<Integer, Integer> contCountry) throws SQLException
    {
        //System.out.println(String.format("%d\t%d\t%d\t%d", node.geoId, node.rankId, node.parentId, node.parentRankId));
        GeoTreeNode parent = getNode(node.parentId, node.parentRankId, null, 0, node.isoCode, node.latitude, node.longitude);
        
        if (node.rankId == 100)
        {
            contCountry.first = node.geoId;
            node.continentId = node.geoId;
            return;
        }
        
        if (node.rankId == 200)
        {
            contCountry.second = node.geoId;
        }
        
        if (node.visited)
        {
            contCountry.first = node.continentId;
            contCountry.second = node.countryId;
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
        
        buildTree(parent, contCountry);

        node.continentId = contCountry.first;
        node.countryId  = contCountry.second;
    }
    
    private void dumpTree()
    {
        System.out.println("GeographyID\tName\tTotal\tCount\tRankID\tCont\tCountry\tVisited");
        for (Integer rankID : treeRanks)
        {
            HashMap<Integer, GeoTreeNode> pNodeHash = geoHash.get(rankID);
            if (pNodeHash != null)
            {
                for (GeoTreeNode pNode : pNodeHash.values())
                {
                    String name = BasicSQLUtils.querySingleObj("SELECT FullName FROM geography WHERE GeographyID = "+pNode.geoId);
                    System.out.println(String.format("%d\t%s\t%d\t%d\t%d\t%d\t%d\t%s",  
                            pNode.geoId, name, pNode.totalCount, pNode.nodeCount, pNode.rankId, pNode.continentId, pNode.countryId, pNode.visited?"Y":"N"));
                }
            }
        }
        System.out.println("----------------------------------------------");
    }

    /**
     * 
     */
    private void exportTreeData()
    {
        PreparedStatement s3Stmt       = null;        
        PreparedStatement s3Stmt2      = null; 
        PreparedStatement s3StmtUpGeo  = null; 
        PreparedStatement s3StmtUpLoc  = null; 
        PreparedStatement geoLookUpStmt = null;
        PreparedStatement llLookUpStmt = null; // Lat, Lon Look Up
        Statement         stmt         = null;
        
        int percentAmt = 0;
        int percentInc = (int)((100.0 / 8.0) + 0.5);

        
        progressDelegate.setProcessPercent(true);
        progressDelegate.setProcess(0, 100);
        progressDelegate.setDesc("Exporting Geographical Information");
        
        int transCnt = 0;
        try
        {
            String treeRanksSQL = ipadExporter.adjustSQL("SELECT RankID FROM geographytreedefitem WHERE RankID > 0 AND GeographyTreeDefID = GEOTREEDEFID ORDER BY RankID DESC");
            treeRanks = BasicSQLUtils.queryForInts(treeRanksSQL);

            // SQLite
            dbS3Conn.setAutoCommit(false);
            s3Stmt  = dbS3Conn.prepareStatement("INSERT INTO geo (_id, FullName, Name, RankID, ParentID, ContinentID, CountryID, TotalCOCnt, NumObjs, NodeNum, HighNodeNum, ISOCode, latitude, longitude) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");        
            s3Stmt2 = dbS3Conn.prepareStatement("UPDATE geo SET ContinentID=?, CountryID=?, TotalCOCnt=?, NumObjs=?, ISOCode=? WHERE _id = ?");  
            s3StmtUpGeo = dbS3Conn.prepareStatement("UPDATE geo SET Latitude=?, Longitude=? WHERE _id = ?");  
            s3StmtUpLoc = dbS3Conn.prepareStatement("UPDATE locality SET Latitude=?, Longitude=? WHERE _id = ?");  
            
            // MySQL
            stmt    = conn.createStatement();
            lookUpParentStmt = conn.prepareStatement("SELECT gp.GeographyID, gp.RankID FROM geography g INNER JOIN geography gp ON g.ParentID = gp.GeographyID WHERE g.GeographyID = ?");
            llLookUpStmt     = conn.prepareStatement("SELECT latitude1, Longitude1 FROM locality WHERE LocalityID = ? AND Latitude1 IS NOT NULL AND Longitude1 IS NOT NULL");

            
            String coLookUp = ipadExporter.adjustSQL("SELECT co.CollectionObjectID, g.GeographyID, g.FullName, g.Name, g.RankID, g.ParentID, g.NodeNumber, g.HighestChildNodeNumber, ic.NewID, gp.RankID, g.GeographyCode, l.latitude1, l.longitude1  " +
                                                     "FROM collectionobject co " +
                                                     "INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                                                     "INNER JOIN ios_colobjcnts ic ON co.CollectionObjectID = ic.OldID " +
                                                     "INNER JOIN locality l ON ce.LocalityID = l.LocalityID " +
                                                     "INNER JOIN geography g ON l.GeographyID = g.GeographyID " +
                                                     "INNER JOIN geography gp ON g.ParentID = gp.GeographyID " +
                                                     "WHERE co.CollectionID = COLMEMID ORDER BY g.RankID DESC, g.GeographyID ASC");
            System.out.println(coLookUp);
            
            
            //---------------------------------------
            // Loop thru Collection Objects
            //---------------------------------------
            int countAmtTotal = 0;
            boolean firstTime = true;
            int prevTaxonID   = -1;
            
            int geoID;
            String fullName     = null;
            String geoName      = null;
            int rankId          = 0;
            int parentId        = 0;
            int nodeNum         = 0;
            int highNodeNum     = 0;
            int colObjCntAmt    = 0;
            int geoParentRankID = 0;
            Integer continentId = null;
            String  isoCode     = null;
            Double latitude     = null;
            Double longitude    = null;
            
            ResultSet rs = stmt.executeQuery(coLookUp); 
            while (rs.next())
            {
                geoID = rs.getInt(2); 
                
                if (firstTime)
                {
                     firstTime = false;
                } else if (geoID != prevTaxonID)
                {
                    writeGeo(s3Stmt, prevTaxonID, fullName, geoName, rankId, parentId, continentId, null, 0, countAmtTotal, nodeNum, highNodeNum, isoCode, latitude, longitude);
                    GeoTreeNode pNode = getNode(prevTaxonID, rankId, parentId, geoParentRankID, isoCode, latitude, longitude); 
                    pNode.written = true;
                    pNode.nodeCount += countAmtTotal;
                    countAmtTotal = 0;
                }
                prevTaxonID       = geoID;
                
                fullName          = rs.getString(3);
                geoName           = rs.getString(4);
                rankId            = rs.getInt(5);
                parentId          = rs.getInt(6);
                nodeNum           = rs.getInt(7);
                highNodeNum       = rs.getInt(8);
                colObjCntAmt      = rs.getInt(9);
                geoParentRankID   = rs.getInt(10);
                isoCode           = rs.getString(11);
                
                latitude          = rs.getDouble(12);
                if (rs.wasNull()) latitude = null;
                
                longitude         = rs.getDouble(13);
                if (rs.wasNull()) longitude = null;
                
                continentId = geoParentRankID == 100 ? parentId : null;
                
                countAmtTotal += colObjCntAmt;
            }
            
            writeGeo(s3Stmt, prevTaxonID, fullName, geoName, rankId, parentId, continentId, null, 0, colObjCntAmt, nodeNum, highNodeNum, isoCode, latitude, longitude);
            GeoTreeNode ppNode = getNode(prevTaxonID, rankId, parentId, geoParentRankID, isoCode, latitude, longitude);  // Parent
            ppNode.nodeCount += countAmtTotal;
            ppNode.written = true;
            
            percentAmt += percentInc;
            progressDelegate.setProcess(percentAmt);

            rs.close();
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
            
            if (true)
            {
                System.out.println("_id\tFullName\tRankID\tParentID\tTotalCOCnt\tNumObjs");
                Statement stmt1 = dbS3Conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery("SELECT _id, FullName, RankID, ParentID, TotalCOCnt, NumObjs FROM geo ORDER BY RankID");
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
                System.out.println();
            
                dumpTree();
                System.out.println();
            }
            
            skipNodeList = true;
            
            for (GeoTreeNode node: nodeList)
            {
                Pair<Integer, Integer> contCountry = new Pair<Integer, Integer>(0, 0);
                buildTree(node, contCountry);
                node.continentId = contCountry.first;
                node.countryId  = contCountry.second;
            }

            percentAmt += percentInc;
            progressDelegate.setProcess(percentAmt);

            //dumpTree();
            //System.out.println();
            
            System.out.println("ID\tRK\tTC\tNC\tPID\tPRK\tPTID\tPTRNK\tTC\tTC2");
            for (Integer rankID : treeRanks)
            {
                HashMap<Integer, GeoTreeNode> pNodeHash = geoHash.get(rankID);
                if (pNodeHash != null)
                {
                    for (GeoTreeNode pNode : pNodeHash.values())
                    {
                        if (pNode.rankId < 200) pNode.countryId = 0;
                        
                        GeoTreeNode parent = getNode(pNode.parentId, pNode.parentRankId, 0, 0, null, null, null);
                        System.out.println(String.format("%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d", pNode.geoId, pNode.rankId, pNode.totalCount, pNode.nodeCount,  pNode.parentId, pNode.parentRankId,
                                                                                           parent.geoId, parent.rankId,  parent.totalCount, (parent.totalCount + pNode.nodeCount + pNode.totalCount)));
                        parent.totalCount += pNode.nodeCount + pNode.totalCount;
                    }
                }
            }
            
            //dumpTree();
            //System.out.println();
              
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
            
            percentAmt += percentInc;
            progressDelegate.setProcess(percentAmt);
            
            int totCnt = 0;
            for (Integer rankID : treeRanks)
            {
                HashMap<Integer, GeoTreeNode> pNodeHash = geoHash.get(rankID);
                if (pNodeHash != null)
                {
                    totCnt += pNodeHash.size();
                }
            }
            
            percentAmt += percentInc;
            progressDelegate.setProcess(percentAmt);

            String geoLookUp = ipadExporter.adjustSQL("SELECT FullName, Name, NodeNumber, HighestChildNodeNumber, GeographyCode FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID AND GeographyID = ?");
            geoLookUpStmt = conn.prepareStatement(geoLookUp);
            
            int cnt = 0;
            
            for (Integer rankID : treeRanks)
            {
                HashMap<Integer, GeoTreeNode> pNodeHash = geoHash.get(rankID);
                if (pNodeHash != null)
                {
                    for (GeoTreeNode pNode : pNodeHash.values())
                    {
                        if (pNode.written)
                        {
                            s3Stmt2.setInt(1, pNode.continentId);
                            s3Stmt2.setInt(2, pNode.countryId);
                            s3Stmt2.setInt(3, pNode.totalCount);
                            s3Stmt2.setInt(4, pNode.nodeCount);
                            s3Stmt2.setString(5, pNode.isoCode);
                            s3Stmt2.setInt(6, pNode.geoId);
                            
                            int rv = s3Stmt2.executeUpdate();
                            if (rv != 1)
                            {
                                System.err.println("Error updating geo: "+pNode.geoId);
                            }
                        } else
                        {
                            geoLookUpStmt.setInt(1, pNode.geoId);
                            rs = geoLookUpStmt.executeQuery();
                            if (rs.next())
                            {
                                fullName          = rs.getString(1);
                                geoName           = rs.getString(2);
                                nodeNum           = rs.getInt(3);
                                highNodeNum       = rs.getInt(4);
                                isoCode           = rs.getString(5);
                                writeGeo(s3Stmt, pNode.geoId, fullName, geoName, pNode.rankId, pNode.parentId, pNode.continentId, pNode.countryId, pNode.totalCount, pNode.nodeCount, nodeNum, highNodeNum, isoCode, null, null);
                            } else
                            {
                                System.err.println("Error looking up geo: "+pNode.geoId);
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
            geoLookUpStmt.close();

            //-----------------------------------------------
            // Fill in missing Lat, Lon for Locality
            //-----------------------------------------------
            percentAmt += percentInc;
            progressDelegate.setProcess(percentAmt);
            
            String sql = "SELECT l._id  FROM locality l WHERE l.Latitude IS NULL OR l.Longitude IS NULL";
            Statement s3StmtTrav = dbS3Conn.createStatement();
            rs = s3StmtTrav.executeQuery(sql); 
            while (rs.next())
            {
                int locID  = rs.getInt(1);
                llLookUpStmt.setInt(1, locID);
                
                Double locLat = null;
                Double locLon = null;
                ResultSet rs2 = llLookUpStmt.executeQuery(); 
                if (rs2.next())
                {
                    locLat = rs2.getDouble(1);
                    if (rs2.wasNull()) locLat = null;
                    
                    locLon = rs2.getDouble(2);
                    if (rs2.wasNull()) locLat = null;
                }
                rs2.close();
                
                if (locLat != null && locLon != null)
                {
                    s3StmtUpLoc.setDouble(1, locLat);
                    s3StmtUpLoc.setDouble(2, locLon);
                    s3StmtUpLoc.setInt(3, locID);
                    int rv = s3StmtUpLoc.executeUpdate();
                    if (rv != 1)
                    {
                        System.err.println("Error updating loc id: "+locID);
                    }
                }
            }
            rs.close();
            s3StmtTrav.close();
            llLookUpStmt.close();
            
            //-----------------------------------------------
            // Fill in missing Lat, Lon for Geography using
            // Locality from isite database
            //-----------------------------------------------
            percentAmt += percentInc;
            progressDelegate.setProcess(percentAmt);
            
            sql = "SELECT g._id, l.Latitude, l.Longitude FROM locality l " +
                  "INNER JOIN colobj co ON l._id = co.LocID " +
                  "INNER JOIN geo g ON co.GeoID = g._id " +
                  "WHERE (l.Latitude IS NOT NULL AND l.Longitude IS NOT NULL) AND (g.Latitude IS NULL OR g.Longitude IS NULL)";
            s3StmtTrav = dbS3Conn.createStatement();
            rs = s3StmtTrav.executeQuery(sql); 
            while (rs.next())
            {
                geoID      = rs.getInt(1);
                double lat = rs.getDouble(2);
                double lon = rs.getDouble(3);
                
                s3StmtUpGeo.setDouble(1, lat);
                s3StmtUpGeo.setDouble(2, lon);
                s3StmtUpGeo.setInt(3, geoID);
                int rv = s3StmtUpGeo.executeUpdate();
                if (rv != 1)
                {
                    System.err.println("Error updating geo id: "+geoID);
                }
            }
            rs.close();
            s3StmtTrav.close();

            //-----------------------------------------------
            // Fill in missing Lat, Lon for Locality
            //-----------------------------------------------
            percentAmt += percentInc;
            progressDelegate.setProcess(percentAmt);
            
            llLookUpStmt = conn.prepareStatement("SELECT l.Longitude1, l.Longitude2 FROM geography g INNER JOIN locality l ON g.GeographyID = l.GeographyID WHERE g.GeographyID = ? LIMIT 0,1");
            
            sql        = "SELECT g._id, g.FullName FROM geo g WHERE g.Latitude IS NULL OR g.Longitude IS NULL";
            s3StmtTrav = dbS3Conn.createStatement();
            rs = s3StmtTrav.executeQuery(sql); 
            while (rs.next())
            {
                geoID = rs.getInt(1);
                System.out.println(String.format("%d - %s", geoID, rs.getString(2)));
                
                llLookUpStmt.setInt(1, geoID);
                
                Double lat = null;
                Double lon = null;
                ResultSet rs2 = llLookUpStmt.executeQuery(); 
                if (rs2.next())
                {
                    lat = rs2.getDouble(1);
                    if (rs2.wasNull()) lat = null;
                    
                    lon = rs2.getDouble(2);
                    if (rs2.wasNull()) lon = null;
                }
                rs2.close();
                
                if (lat != null && lon != null)
                {
                    s3StmtUpGeo.setDouble(1, lat);
                    s3StmtUpGeo.setDouble(2, lon);
                    s3StmtUpGeo.setInt(3,    geoID);
                    int rv = s3StmtUpLoc.executeUpdate();
                    if (rv != 1)
                    {
                        System.err.println("Error updating loc id: "+geoID);
                    }
                }
            }
            rs.close();

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
                if (s3Stmt != null) s3Stmt.close();
                if (s3Stmt2 != null) s3Stmt2.close();
                if (s3StmtUpGeo != null) s3StmtUpGeo.close();
                if (s3StmtUpLoc != null) s3StmtUpLoc.close();
                if (geoLookUpStmt != null) geoLookUpStmt.close();
                if (llLookUpStmt != null) llLookUpStmt.close();
                
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}

        }
    }
    
    
    class GeoTreeNode
    {
        int     geoId;
        int     rankId;
        int     totalCount;
        int     nodeCount;
        Integer parentId;
        int     parentRankId;
        String  isoCode;
        boolean visited;
        boolean written;
        
        int     continentId;
        int     countryId;
        
        Double  latitude;
        Double  longitude;
        
        /**
         * @param geoId
         * @param rankId
         * @param parentId
         * @param parentRankId
         * @param latitude
         * @param longitude
         */
        public GeoTreeNode(final int geoId, 
                           final int rankId,
                           final Integer parentId,
                           final int parentRankId,
                           final String isoCode,
                           final Double latitude,
                           final Double longitude)
        {
            super();
            this.geoId        = geoId;
            this.rankId       = rankId;
            this.totalCount   = 0;
            this.nodeCount    = 0;
            this.parentId     = parentId;
            this.parentRankId = parentRankId;
            this.isoCode      = isoCode;
            this.latitude     = latitude;
            this.longitude    = longitude;
            this.visited      = false;
            this.written      = false;
        }
        
    }
}
