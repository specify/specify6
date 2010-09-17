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
package edu.ku.brc.specify.toycode.mexconabio;

import java.io.PrintWriter;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

public class AnalysisWithGBIFToGBIF extends AnalysisBase
{
    //private static final Logger  log                = Logger.getLogger(AnalysisWithSNIB.class);
    
    private StringBuilder sb = new StringBuilder();
    
    /**
     * 
     */
    public AnalysisWithGBIFToGBIF()
    {
        super();
    }

    /**
     * @param val
     * @return
     */
    private String getIntToStr(final Object val)
    {
        if (val != null && val instanceof Integer)
        {
            return Integer.toString((Integer)val);
        }
        return null;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.toycode.mexconabio.AnalysisBase#process(int, int)
     */
    @Override
    public void process(final int type, final int options)
    {
        final double HRS = 1000.0 * 60.0 * 60.0; 
        
        calcMaxScore();
        
        String gbifSQL   = "SELECT DISTINCT id, catalogue_number, genus, species, subspecies, latitude, longitude, country, state_province, collector_name, locality, year, month, day, collector_num ";
        
        String fromClause1 = "FROM raw WHERE collector_num = ? AND year = ? AND genus = ? AND id <> ?";
        String fromClause2 = "FROM raw WHERE collector_num IS NULL AND year = ? AND month = ? AND genus = ? AND id <> ?";
        
        //                        1       2           3        4           5         6          7         8           9               10          11       12    13    14      15
        String postSQL = "FROM raw WHERE collector_num IS NOT NULL GROUP BY collector_num, year, genus ORDER BY collector_num";
        String srcSQL  = "SELECT id, catalogue_number, genus, species, subspecies, latitude, longitude, country, state_province, collector_name, locality, year, month, day, collector_num " + postSQL;
        
        String gbifgbifInsert = "INSERT INTO gbifgbif (reltype, score, GBIFID, SNIBID) VALUES (?,?,?,?)";
        
        Statement         stmt   = null;
        PreparedStatement gStmt1 = null;
        PreparedStatement gStmt2 = null;
        PreparedStatement gsStmt = null;
        
        Object[] refRow = new Object[14];
        Object[] cmpRow = new Object[14];

        
        long totalRecs     = BasicSQLUtils.getCount(dbSrcConn, "SELECT COUNT(*) " + postSQL);
        long procRecs      = 0;
        long startTime     = System.currentTimeMillis();
        int  secsThreshold = 0;
        
        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter("scoring_gbifgbif.log");
            
            gStmt1 = dbGBIFConn.prepareStatement(gbifSQL + fromClause1);
            gStmt2 = dbGBIFConn.prepareStatement(gbifSQL + fromClause2);
            gsStmt = dbDstConn.prepareStatement(gbifgbifInsert);
            
            stmt  = dbSrcConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            System.out.println("Starting Query... "+totalRecs);
            pw.println("Starting Query... "+totalRecs);
            System.out.flush();
            pw.flush();
            
            HashSet<Integer> idHash = new HashSet<Integer>();
            int writeCnt = 0;
            ResultSet rs = stmt.executeQuery(srcSQL);
            
            System.out.println(String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore));
            pw.println(String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore));
            System.out.flush();
            pw.flush();
            
            while (rs.next())
            {
                if (procRecs < 140)
                {
                    procRecs++;
                    continue;
                }
                
                fillGBIF(refRow, rs);

                Integer srcId = rs.getInt(1);
                
                // Search Records with Collector Number match
                gStmt1.setString(1, (String)refRow[COLNUM_INX]);
                gStmt1.setString(2, (String)refRow[YEAR_INX]);
                gStmt1.setString(3, (String)refRow[GENUS_INX]);
                gStmt1.setInt(4,    srcId);
                
                idHash.clear();
                
                ResultSet gRS = gStmt1.executeQuery();
                while (gRS.next())
                {
                   fillGBIF(cmpRow, gRS);
                   
                   int score = score(refRow, cmpRow);
                   
                   if (score > thresholdScore)
                   {
                       writeCnt++;
                       
                       int gbifID = gRS.getInt(1);
                       gsStmt.setInt(1, 1);      // reltype
                       gsStmt.setInt(2, score);  // score
                       gsStmt.setInt(3, gbifID);
                       gsStmt.setInt(4, srcId);
                       gsStmt.executeUpdate();
                       
                       idHash.add(gbifID);
                   }
                }
                gRS.close();
                
                // Search Records with NULL Collector Number
                gStmt2.setString(1, (String)refRow[YEAR_INX]);
                gStmt2.setString(2, (String)refRow[MON_INX]);
                gStmt2.setString(3, (String)refRow[GENUS_INX]);
                gStmt2.setInt(4,    srcId);
                
                gRS  = gStmt2.executeQuery();
                while (gRS.next())
                {
                    int gbifID = gRS.getInt(1);
                    
                    if (idHash.contains(gbifID)) continue;
                    
                    fillGBIF(cmpRow, gRS);
                    
                    int score = score(refRow, cmpRow);
                   
                    if (score > thresholdScore)
                    {
                        writeCnt++;
                       
                        gsStmt.setInt(1, 2);     // reltype
                        gsStmt.setInt(2, score); // score
                        gsStmt.setInt(3, gbifID);
                        gsStmt.setInt(4, srcId);
                        gsStmt.executeUpdate();
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
                        
                        String msg = String.format("Elapsed %8.2f hr.mn   Percent: %6.3f  Hours Left: %8.2f ", 
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
        AnalysisWithGBIFToGBIF awg = new AnalysisWithGBIFToGBIF();
        awg.createDBConnection("localhost",     "3306", "plants", "root", "root");
        awg.createSrcDBConnection("localhost",  "3306", "plants", "root", "root");
        awg.createDestDBConnection("localhost", "3306", "plants", "root", "root");
        awg.process(1, DO_ALL);
        awg.endLogging();
        
        awg.cleanup();
    }
}
