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

import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCountAsInt;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.IdHashMapper;
import edu.ku.brc.specify.conversion.IdMapperIFace;
import edu.ku.brc.specify.conversion.IdMapperMgr;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.ui.ProgressDialog;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 10, 2014
 *
 */
public class ExportPaleo
{
    private static final Logger  log = Logger.getLogger(ExportPaleo.class);

    private static final String  COLOBJLITHONAME  = "ios_colobjlitho";
    private static final String  COLOBJBIONAME    = "ios_colobjbio";
    private static final String  COLOBJCHRONNAME  = "ios_colobjchron";


    
    enum RelationshipType { eTypeError, eColObj, eColCE, eLocality}

    private IdMapperIFace                            colObjToLitho           = null;
    private IdMapperIFace                            colObjToBio             = null;
    private IdMapperIFace                            colObjToChron           = null;
    
    private ProgressDialog progressDelegate;
    private Connection     dbS3Conn;
    private iPadDBExporter ipadExporter;
    private TableWriter    tblWriter;  
    private SwingWorker<Integer, Integer> worker;
    private Connection     dbConn;
    
    private int treeTotal   = 0;
    
    private RelationshipType paleRelType = RelationshipType.eColObj;
    
    /**
     * @param ipadExporter
     * @param dbS3Conn
     * @param conn
     * @param colObjToCnt
     */
    public ExportPaleo(final iPadDBExporter ipadExporter, 
                       final Connection     dbS3Conn, 
                       final Connection     conn,
                       final TableWriter    tblWriter,
                       final SwingWorker<Integer, Integer> worker)
    {
        this.dbS3Conn         = dbS3Conn;
        this.ipadExporter     = ipadExporter;
        this.progressDelegate = ipadExporter.getProgressDelegate();
        this.tblWriter        = tblWriter;
        this.worker           = worker;
    }
    
    /**
     * @return
     */
    public boolean initialize()
    {
        this.paleRelType = discoverPaleRelationshipType();
        if (this.paleRelType != RelationshipType.eTypeError)
        {
            dbConn = DBConnection.getInstance().getConnection();
            return true;
        }
        return false;
    }
    
    /**
     * @return
     */
    private String getFromClause()
    {
        String joins = "";
        switch (paleRelType)
        {
            case eColObj:
                joins = "FROM paleocontext pc " +
                		"LEFT JOIN collectionobject co ON pc.PaleoContextID = co.PaleoContextID " +
                		"LEFT JOIN lithostrat l ON pc.LithoStratID = l.LithoStratID " + 
                		"LEFT JOIN lithostrat bl ON pc.BioStratID = bl.LithoStratID " + 
                		"LEFT JOIN geologictimeperiod g ON pc.ChronosStratID = g.GeologicTimePeriodID ";
               break;
                
            case eColCE:
                joins = "FROM paleocontext pc " +
                		"LEFT JOIN collectingevent ce ON pc.PaleoContextID = ce.PaleoContextID " +
                		"LEFT JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID " +
                		"LEFT JOIN lithostrat l ON pc.LithoStratID = l.LithoStratID " +
                		"LEFT JOIN lithostrat bl ON pc.BioStratID = bl.LithoStratID " + 
                        "LEFT JOIN geologictimeperiod g ON pc.ChronosStratID = g.GeologicTimePeriodID ";
                break;
                
            case eLocality:
                joins = "FROM collectionobject co " +
                		"LEFT JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " + 
                        "LEFT JOIN locality lc ON ce.LocalityID = lc.LocalityID " + 
                        "LEFT JOIN paleocontext pc ON lc.PaleoContextID = pc.PaleoContextID " + 
                        "LEFT JOIN lithostrat l ON pc.LithoStratID = l.LithoStratID " +
                        "LEFT JOIN lithostrat bl ON pc.BioStratID = bl.LithoStratID " + 
                        "LEFT JOIN geologictimeperiod g ON pc.ChronosStratID = g.GeologicTimePeriodID ";
                break;
                
        }
        return joins;
    }
    
    
    /**
     * Count number of Specimens for each Collection Object.
     * @throws SQLException
     */
    protected void doBuildPaleo() throws SQLException
    {
        worker.firePropertyChange(iPadDBExporter.MSG, "", "Locating Specimen Paleo data...");
        
        String postSQL = getFromClause() + " WHERE CollectionID = COLMEMID";
        String sql = ipadExporter.adjustSQL("SELECT COUNT(*)" + postSQL);
        
        log.debug(sql);
        
        int totCnt = getCountAsInt(dbConn, sql);
        if (progressDelegate != null) progressDelegate.setProcess(0, 100);
                
        colObjToLitho = IdMapperMgr.getInstance().addHashMapper(COLOBJLITHONAME, null, true);
        colObjToLitho.reset();

        colObjToBio = IdMapperMgr.getInstance().addHashMapper(COLOBJBIONAME, null, true);
        colObjToBio.reset();
       
        colObjToChron = IdMapperMgr.getInstance().addHashMapper(COLOBJCHRONNAME, null, true);
        colObjToChron.reset();
        
        IdHashMapper.setEnableDelete(false);
        IdMapperMgr.getInstance().setDBs(dbConn, dbConn);
        
        dbS3Conn.setAutoCommit(false);

        //----------------------------------------------------------
        // Creating Mapping from ColObj top Geo
        //----------------------------------------------------------
        int fivePercent = Math.max(totCnt / 20, 1);
        
        int cnt = 0; 
        
        PreparedStatement s3Stmt = null;
        Statement         stmt   = null;
        try
        {
            String upSQL = "INSERT INTO paleo (_id, LithoID, ChronosID, BioStratID) VALUES (?,?,?,?)";
            s3Stmt = dbS3Conn.prepareStatement(upSQL);
    
            sql  = ipadExporter.adjustSQL("SELECT co.CollectionObjectID, l.LithoStratID, pc.ChronosStratID, bl.LithoStratID " + postSQL);
            log.debug(sql);
            stmt = dbConn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            while (rs.next())
            {
                int coId       = rs.getInt(1);
                s3Stmt.setInt(1, coId);
                
                Integer lithId = rs.getInt(2);
                if (rs.wasNull())
                {
                    lithId = null;
                    s3Stmt.setObject(2, null);
                } else
                {
                    s3Stmt.setInt(2, lithId);
                }
                
                Integer chronosId = rs.getInt(3);
                if (rs.wasNull())
                {
                    chronosId = null;
                    s3Stmt.setObject(3, null);
                } else
                {
                    s3Stmt.setInt(3, chronosId);
                }
                
                Integer bioId = rs.getInt(4);
                if (rs.wasNull())
                {
                    bioId = null;
                    s3Stmt.setObject(4, null);
                } else
                {
                    s3Stmt.setInt(4, bioId);    
                }

                if (s3Stmt.executeUpdate() != 1)
                {
                    System.out.println("Error updating litho: "+coId);
                }                
                cnt++;
                if (cnt % fivePercent == 0) 
                {
                    worker.firePropertyChange(iPadDBExporter.PROGRESS, 0, Math.max((100 * cnt) / totCnt, 1));
                }
            }
            rs.close();
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
    }
        
    /**
     * @throws SQLException
     */
    protected void doBuildLitho() throws SQLException
    {
        int     cnt = 0;
        String  sql;
        Integer count = null;

        // Get Specimen Counts for Litho
        worker.firePropertyChange(iPadDBExporter.MSG, "", "Getting Lithostratigraphy Counts...");
        HashMap<Integer, Integer> numObjsHash = new HashMap<Integer, Integer>();
        sql = ipadExporter.adjustSQL("SELECT l.LithoStratID, SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) " +
                        getFromClause() +
                        "WHERE l.LithoStratTreeDefID = LITHOTREEDEFID GROUP BY l.LithoStratID");
        
        log.debug(sql);
        
        for (Object[] row : query(sql))
        {
            count = iPadDBExporter.getCount(row[1]);
            numObjsHash.put((Integer)row[0], count);
        }
        
        // Get All Unsed Litho items
        worker.firePropertyChange(iPadDBExporter.MSG, "", "Building Lithostrat...");
        
        String prefix  = "SELECT l.LithostratID, l.FullName, l.RankID, l.ParentID, l.HighestChildNodeNumber, l.NodeNumber ";
        String fullPostFix = ipadExporter.adjustSQL(getFromClause() +
                                                    "WHERE l.LithoStratTreeDefID = LITHOTREEDEFID AND co.CollectionObjectID IS NOT NULL ");

        
        sql = "SELECT COUNT(*) " + fullPostFix;
        log.debug(sql);
        int totCnt = getCountAsInt(dbConn, sql);
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        sql = prefix + fullPostFix + " GROUP BY l.LithostratID";
        log.debug(sql);
        
        tblWriter.log("Lithostrat Issues");
        tblWriter.startTable();
        tblWriter.logHdr("Full Name", "Rank Id", "Issue");

        
        //boolean isSingleCollection = getCountAsInt(dbConn, "SELECT COUNT(*) FROM collection") == 1;

        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt = null;
        Statement         stmt   = null;
        try
        {
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
    
            dbS3Conn.setAutoCommit(false);
            int transCnt = 0;
            
            cnt = 0;
            String upSQL = "INSERT INTO litho (_id, FullName, RankID, ParentID, TotalCOCnt, NumObjs, HighNodeNum, NodeNum) VALUES (?,?,?,?,?,?,?,?)";
            s3Stmt = dbS3Conn.prepareStatement(upSQL);
            ResultSet rs = stmt.executeQuery(ipadExporter.adjustSQL(sql));
            while (rs.next())
            {
                transCnt++;
    
                Integer id = rs.getInt(1);
                
                s3Stmt.setInt(1,    id);
                s3Stmt.setString(2, rs.getString(2)); // FullName
                s3Stmt.setInt(3,    rs.getInt(3));    // RankID
                s3Stmt.setInt(4,    rs.getInt(4));    // ParentID
                
                int highestNodeNum = rs.getInt(5);    
                int nodeNumber     = rs.getInt(6);
                
                s3Stmt.setInt(5,    0);          // Total Number of Specimens below
                
                Integer numObjs = numObjsHash.get(id);
                if (numObjs == null) numObjs = 0;
                s3Stmt.setInt(6,    numObjs);          // Number of Specimens this Lithostrat has
                
                s3Stmt.setInt(7,    highestNodeNum);
                s3Stmt.setInt(8,    nodeNumber);
                
                if (s3Stmt.executeUpdate() != 1)
                {
                    System.out.println("Error updating litho: "+id);
                }
                cnt++;
                if (cnt % 1000 == 0) 
                {
                    worker.firePropertyChange(iPadDBExporter.PROGRESS, 0, cnt);
                    log.debug("Lithostrat: "+cnt);
                }
            }
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
    
            rs.close();
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        tblWriter.endTable();
        
        worker.firePropertyChange(iPadDBExporter.MSG, "", "Updating Total Lithostrategraphy Counts...");
        
        //sql = ipadExporter.adjustSQL("SELECT LithoStratID FROM lithostrat WHERE LithoStratTreeDefID = LITHOTREEDEFID AND RankID = 0");
        //int rootId = getCountAsInt(dbConn, sql);
        
        sql = ipadExporter.adjustSQL("SELECT COUNT(*) FROM lithostrat WHERE LithoStratTreeDefID = LITHOTREEDEFID");
        treeTotal = getCountAsInt(dbConn, sql);
        if (treeTotal == 0)
        {
            return;
        }
        //treePercent = treeTotal / 20;
        if (progressDelegate != null) 
        {
            progressDelegate.setProcessPercent(true);
            progressDelegate.setProcess(0, 100);
        }

        dbS3Conn.setAutoCommit(false);
        PreparedStatement pStmt = null;
        try
        {
            String upStr = "UPDATE litho SET TotalCOCnt=? WHERE _id=?";
            pStmt = dbS3Conn.prepareStatement(upStr);
            iPadDBExporter.fillTotalCountForTree(dbS3Conn, worker, "Litho", pStmt);
            pStmt.close();
            
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (pStmt != null) pStmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        if (progressDelegate != null) progressDelegate.setProcessPercent(false);

    }

    
    /**
     * @throws SQLException
     */
    protected void doBuildChronostrat() throws SQLException
    {
        int     cnt = 0;
        String  sql;
        Integer count = null;
        
        // GeologicTimePeriodID geologictimeperiod

        // Get Specimen Counts for ChronosStrat
        worker.firePropertyChange(iPadDBExporter.MSG, "", "Getting ChronosStrat Counts...");
        HashMap<Integer, Integer> numObjsHash = new HashMap<Integer, Integer>();
        sql = ipadExporter.adjustSQL("SELECT g.GeologicTimePeriodID, SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) " +
                                     getFromClause() +
                                     "WHERE g.GeologicTimePeriodTreeDefID = GTPTREEDEFID GROUP BY g.GeologicTimePeriodID");
        
        log.debug(sql);
        
        for (Object[] row : query(sql))
        {
            count = iPadDBExporter.getCount(row[1]);
            numObjsHash.put((Integer)row[0], count);
        }
        
        // Get All Unsed Litho items
        worker.firePropertyChange(iPadDBExporter.MSG, "", "Pruning ChronosStrat Tree...");
        worker.firePropertyChange(iPadDBExporter.MSG, "", "Building ChronosStrat...");
        
        String prefix  = "SELECT g.GeologicTimePeriodID, g.FullName, g.RankID, g.ParentID, g.HighestChildNodeNumber, g.NodeNumber ";
        String fullPostfix = ipadExporter.adjustSQL(getFromClause() +
                                                    "WHERE g.GeologicTimePeriodTreeDefID = GTPTREEDEFID AND co.CollectionObjectID IS NOT NULL");
        
        sql = "SELECT COUNT(*) " + fullPostfix;
        log.debug(sql);
        int totCnt = getCountAsInt(dbConn, sql);
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        sql = prefix + fullPostfix + " GROUP BY g.GeologicTimePeriodID";
        log.debug(sql);
        
        tblWriter.log("ChronosStrat Issues");
        tblWriter.startTable();
        tblWriter.logHdr("Full Name", "Rank Id", "Issue");
        
        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt = null;
        Statement         stmt   = null;
        try
        {
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
    
            dbS3Conn.setAutoCommit(false);
            int transCnt = 0;
            
            cnt = 0;
            String upSQL = "INSERT INTO gtp (_id, FullName, RankID, ParentID, TotalCOCnt, NumObjs, HighNodeNum, NodeNum) VALUES (?,?,?,?,?,?,?,?)";
            s3Stmt = dbS3Conn.prepareStatement(upSQL);
            ResultSet rs = stmt.executeQuery(ipadExporter.adjustSQL(sql));
            while (rs.next())
            {
                transCnt++;
    
                Integer id = rs.getInt(1);
                s3Stmt.setInt(1,    id);
                s3Stmt.setString(2, rs.getString(2)); // FullName
                s3Stmt.setInt(3,    rs.getInt(3));    // RankID
                s3Stmt.setInt(4,    rs.getInt(4));    // ParentID
                
                int highestNodeNum = rs.getInt(5);    
                int nodeNumber     = rs.getInt(6);
                
                s3Stmt.setInt(5,    0);          // Total Number of Specimens below
                
                Integer numObjs = numObjsHash.get(id);
                if (numObjs == null) numObjs = 0;
                s3Stmt.setInt(6,    numObjs);          // Number of Specimens this Chronos has
                
                s3Stmt.setInt(7,    highestNodeNum);
                s3Stmt.setInt(8,    nodeNumber);
                
                if (s3Stmt.executeUpdate() != 1)
                {
                    System.out.println("Error updating litho: "+id);
                }
                cnt++;
                if (cnt % 1000 == 0) 
                {
                    worker.firePropertyChange(iPadDBExporter.PROGRESS, 0, cnt);
                    log.debug("Chronos: "+cnt);
                }
            }
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
    
            rs.close();
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        tblWriter.endTable();
        
        worker.firePropertyChange(iPadDBExporter.MSG, "", "Updating Total Chronostratigraphy Counts...");
        
        sql = ipadExporter.adjustSQL("SELECT COUNT(*) FROM geologictimeperiod WHERE GeologicTimePeriodTreeDefID = GTPTREEDEFID");
        treeTotal = getCountAsInt(dbConn, sql);
        if (treeTotal == 0)
        {
            return;
        }
        if (progressDelegate != null) 
        {
            progressDelegate.setProcessPercent(true);
            progressDelegate.setProcess(0, 100);
        }

        dbS3Conn.setAutoCommit(false);
        PreparedStatement pStmt = null;
        try
        {
            String upStr = "UPDATE gtp SET TotalCOCnt=? WHERE _id=?";
            pStmt = dbS3Conn.prepareStatement(upStr);
            iPadDBExporter.fillTotalCountForTree(dbS3Conn, worker, "GTP", pStmt);
            pStmt.close();
            
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (pStmt != null) pStmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        if (progressDelegate != null) progressDelegate.setProcessPercent(false);

    }
        
    
    /**
     * @return
     */
    public static RelationshipType discoverPaleRelationshipType()
    {
        String[] values = {"error", "collectionobject", "collectingevent", "locality"};
        Discipline disp = AppContextMgr.getInstance().getClassObject(Discipline.class);
        System.out.println(disp.getPaleoContextChildTable());
        for (int i=0;i<values.length;i++)
        {
            if (values[i].equals(disp.getPaleoContextChildTable()))
            {
                return RelationshipType.values()[i];
            }
        }
        return RelationshipType.eTypeError;
    }

}
