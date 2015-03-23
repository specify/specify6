/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.toycode.mexconabio;

import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

public class AnalysisWithSNIB extends AnalysisBase
{
    //private static final Logger  log                = Logger.getLogger(AnalysisWithSNIB.class);
    
    /**
     * 
     */
    public AnalysisWithSNIB()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.toycode.mexconabio.AnalysisBase#process(int, int)
     */
    @Override
    public void process(final int type, final int options)
    {
        calcMaxScore();
        
        String fromClause1 = "FROM angiospermas WHERE CollectorNumber = ? AND `Year` = ? AND Genus = ?";
        String fromClause2 = "FROM angiospermas WHERE CollectorNumber IS NULL AND `Year` = ? AND `Month` = ? AND Genus = ?";
        
        String michSQLFromClause = "FROM conabio ORDER BY CollNr";
        //String michSQLFromClause = "FROM conabio WHERE CollNr IS NOT NULL ORDER BY CollNr";
        
        String gbifsnibInsert = "INSERT INTO snibmex (reltype, score, GBIFID, SNIBID) VALUES (?,?,?,?)";
        
        Statement         stmt   = null;
        PreparedStatement gStmt1 = null;
        PreparedStatement gStmt2 = null;
        PreparedStatement gsStmt = null;
        
        Object[] refRow = new Object[NUM_FIELDS];
        Object[] cmpRow = new Object[NUM_FIELDS];
        
        int SKIP_FIRST_NUM = -1;//140;

        
        long totalRecs     = BasicSQLUtils.getCount(dbSrcConn, "SELECT COUNT(*) FROM conabio");
        long procRecs      = 0;
        long startTime     = System.currentTimeMillis();
        int  secsThreshold = 0;
        
        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter("scoring_snib.log");
            
            gStmt1 = dbGBIFConn.prepareStatement(snibSQL + fromClause1);
            gStmt2 = dbGBIFConn.prepareStatement(snibSQL + fromClause2);
            gsStmt = dbDstConn.prepareStatement(gbifsnibInsert);
            
            stmt  = dbSrcConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            thresholdScore = 0;
            
            System.out.println("Starting Query... "+totalRecs);
            pw.println("Starting Query... "+totalRecs);
            
            HashSet<Integer> allMichIdsHash = new HashSet<Integer>();
            ResultSet        rsTmp          = stmt.executeQuery("SELECT ID FROM conabio");
            while (rsTmp.next())
            {
                if (procRecs < SKIP_FIRST_NUM)
                {
                    procRecs++;
                    continue;
                }
                allMichIdsHash.add(rsTmp.getInt(1));
                procRecs++;
            }
            rsTmp.close();
            
            String msg = "Total Count Ids " + procRecs+" / " + allMichIdsHash.size();
            System.out.println(msg);
            pw.println(msg);
            
            HashSet<Integer> idHash = new HashSet<Integer>();
            int writeCnt = 0;
            ResultSet rs = stmt.executeQuery(michSQL + michSQLFromClause);
            
            System.out.println(String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore));
            pw.println(String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore));
            
            procRecs = 0;
            while (rs.next())
            {
                if (procRecs < SKIP_FIRST_NUM)
                {
                    procRecs++;
                    continue;
                }
                
                fillMichRow(refRow, rs);
                int michId = rs.getInt(11);
                
                boolean isRemoved = false;
                
                // Search Records with Collector Number match
                gStmt1.setString(1, (String)refRow[COLNUM_INX]);
                gStmt1.setString(2, (String)refRow[YEAR_INX]);
                gStmt1.setString(3, (String)refRow[GENUS_INX]);
                
                idHash.clear();
                
                if (refRow[COLNUM_INX] != null)
                {
                    ResultSet gRS = gStmt1.executeQuery();
                    while (gRS.next())
                    {
                       fillSNIBRow(cmpRow, gRS);
                       
                       int score = score(refRow, cmpRow);
                       
                       if (score > thresholdScore)
                       {
                           writeCnt++;
                           
                           int gbifID = gRS.getInt(1);
                           gsStmt.setInt(1, 1);        // reltype
                           gsStmt.setInt(2, score);    // score
                           gsStmt.setInt(3, gbifID);   // SNIB Database 
                           gsStmt.setInt(4, michId);   // Inigo's Database
                           gsStmt.executeUpdate();
                           
                           idHash.add(gbifID);
                           
                           if (!isRemoved)
                           {
                               allMichIdsHash.remove(michId);
                               isRemoved = true;
                           }
                       }
                    }
                    gRS.close();
                }
                
                // Search Records with NULL Collector Number
                gStmt2.setString(1, (String)refRow[YEAR_INX]);
                gStmt2.setString(2, (String)refRow[MON_INX]);
                gStmt2.setString(3, (String)refRow[GENUS_INX]);
                
                ResultSet gRS  = gStmt2.executeQuery();
                while (gRS.next())
                {
                    int gbifID = gRS.getInt(1);
                    
                    if (idHash.contains(gbifID)) continue;
                    
                    fillSNIBRow(cmpRow, gRS);
                    
                    int score = score(refRow, cmpRow);
                   
                    if (score > thresholdScore)
                    {
                        writeCnt++;
                       
                        gsStmt.setInt(1, 2);      // reltype
                        gsStmt.setInt(2, score);  // score
                        gsStmt.setInt(3, gbifID); // SNIB Database
                        gsStmt.setInt(4, michId); // Inigo's Database
                        gsStmt.executeUpdate();
                        
                        if (!isRemoved)
                        {
                            allMichIdsHash.remove(michId);
                            isRemoved = true;
                        }
                    }
                }
                gRS.close();
                
                procRecs++;
                if (procRecs % 500 == 0)
                {
                    long endTime     = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    
                    double timePerRecord      = (elapsedTime / procRecs); 
                    
                    double hrsLeft = ((totalRecs - procRecs) * timePerRecord) / HRS;
                    
                    int seconds = (int)(elapsedTime / 60000.0);
                    if (secsThreshold != seconds)
                    {
                        secsThreshold = seconds;
                        
                        msg = String.format("Elapsed %8.2f hr.mn   Percent: %6.3f  Hours Left: %8.2f ", 
                                ((double)(elapsedTime)) / HRS, 
                                100.0 * ((double)procRecs / (double)totalRecs),
                                hrsLeft);
                        System.out.println(msg);
                        pw.println(msg);
                        pw.flush();
                    }
                }
            }
            rs.close();
            
            int    reportRecsSize = 1000;
            String dataDirName    = "michsnib";
            String title          = String.format("%s %d - %d",    dataDirName, 0, reportRecsSize);
            String fileName       = String.format("%s_%d_%d.html", dataDirName, 0, reportRecsSize);
            startLogging("analysis", dataDirName, fileName, title, true, 100);

            int cnt = 0;
            PreparedStatement ps = dbSrcConn.prepareStatement(michSQL + " FROM conabio WHERE ID = ?");
            for (Integer id : allMichIdsHash)
            {
                cnt++;
                
                ps.setInt(1, id);
                rs = ps.executeQuery();
                while (rs.next())
                {
                    fillMichRow(refRow, rs);
                    writeRow(cnt % 2 == 0 ? "ev" : "od", refRow, null);
                }
                rs.close();
                
                if (cnt % reportRecsSize == 0)
                {
                    title    = String.format("%s %d - %d",    dataDirName, cnt, (cnt+reportRecsSize));
                    fileName = String.format("%s_%d_%d.html", dataDirName, cnt, (cnt+reportRecsSize));
                    
                    startNewDocument(fileName, title, true, 100);
                }
            }
            tblWriter.endTable();
            tblWriter.close();
            
            
            msg = "Final Count Ids " + procRecs+" / " + allMichIdsHash.size();
            System.out.println(msg);
            pw.println(msg);
            
            System.out.println("Done.");
            pw.println("Done.");
            
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
                if (gStmt1 != null)
                {
                    gStmt1.close();
                }
                if (gStmt2 != null)
                {
                    gStmt2.close();
                }
            } catch (Exception ex)
            {
                
            }
        }
        System.out.println("Done.");
        pw.println("Done.");
        pw.flush();
        pw.close();
    }
    
    //------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        AnalysisWithSNIB awg = new AnalysisWithSNIB();
        awg.createDBConnection("localhost",     "3306", "plants",  "root", "root");
        awg.createSrcDBConnection("localhost",  "3306", "plants",  "root", "root");
        awg.createDestDBConnection("localhost", "3306", "plants",  "root", "root");
        awg.process(1, DO_ALL);
        awg.endLogging();
        
        awg.cleanup();
    }
}
