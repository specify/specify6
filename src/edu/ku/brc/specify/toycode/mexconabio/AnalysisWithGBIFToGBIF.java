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
package edu.ku.brc.specify.toycode.mexconabio;

import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

public class AnalysisWithGBIFToGBIF extends AnalysisBase
{
    //private static final Logger  log                = Logger.getLogger(AnalysisWithSNIB.class);
    
    protected Stack<Object[]> rowRecycler = new Stack<Object[]>();
    protected Object[] nullRow = null;
    
    /**
     * 
     */
    public AnalysisWithGBIFToGBIF()
    {
        super();
        
        nullRow = new Object[NUM_FIELDS];
        for (int i=0;i<nullRow.length;i++)
        {
            nullRow[i] = null;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.toycode.mexconabio.AnalysisBase#process(int, int)
     */
    @Override
    public void process(final int type, final int options)
    {
        calcMaxScore();
        
        String gbifSQL   = "SELECT DISTINCT id, catalogue_number, genus, species, subspecies, latitude, longitude, country, state_province, collector_name, locality, year, month, day, collector_num ";
        
        String fromClause1a = "FROM raw WHERE collector_num LIKE ? AND year = ? AND genus = ?";
        String fromClause1b = "FROM raw WHERE collector_num IS NULL AND year = ? AND genus = ?";
        //String fromClause2  = "FROM raw WHERE collector_num IS NULL AND year = ? AND month = ? AND genus = ? AND id <> ?";
        
        //                        1       2           3        4           5         6          7         8           9               10          11       12    13    14      15
        String postSQL = "FROM raw WHERE collector_num IS NOT NULL GROUP BY collector_num, year, genus";
        String srcSQL  = "SELECT id, catalogue_number, genus, species, subspecies, latitude, longitude, country, state_province, collector_name, locality, year, month, day, collector_num " + postSQL +  " ORDER BY collector_num";
        
        String grphashSQL = "SELECT name FROM group_hash";
        
        String gbifgbifInsert = "INSERT INTO gbifgbif (reltype, score, GBIFID, SNIBID) VALUES (?,?,?,?)";
        
        Statement         stmt    = null;
        PreparedStatement gStmt1a = null;
        PreparedStatement gStmt1b = null;
        //PreparedStatement gStmt2  = null;
        PreparedStatement gsStmt  = null;
        
        Object[] refRow = new Object[NUM_FIELDS];
        Object[] cmpRow = new Object[NUM_FIELDS];

        
        long totalRecs     = BasicSQLUtils.getCount(dbSrcConn, "SELECT COUNT(*) FROM group_hash");
        long procRecs      = 0;
        long startTime     = System.currentTimeMillis();
        int  secsThreshold = 0;
        
        String blank = "X?";
        
        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter("scoring_gbifgbif.log");
            
            gStmt1a = dbGBIFConn.prepareStatement(gbifSQL + fromClause1a);
            gStmt1b = dbGBIFConn.prepareStatement(gbifSQL + fromClause1b);
            
            //gStmt2 = dbGBIFConn.prepareStatement(gbifSQL + fromClause2);
            gsStmt = dbDstConn.prepareStatement(gbifgbifInsert);
            
            stmt  = dbSrcConn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            System.out.println("Starting Query... "+totalRecs);
            pw.println("Starting Query... "+totalRecs);
            System.out.flush();
            pw.flush();
            
            HashSet<Integer> idHash = new HashSet<Integer>();
            int writeCnt = 0;
            ResultSet rs = stmt.executeQuery(grphashSQL);
            
            System.out.println(String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore));
            pw.println(String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore));
            System.out.flush();
            pw.flush();
            
            Vector<Object[]>   group = new Vector<Object[]>();
            ArrayList<Integer> ids   = new ArrayList<Integer>();
            while (rs.next())
            {
                String[] tokens = StringUtils.split(rs.getString(1), '_');
                
                String colNum = tokens[0].trim();
                String year   = tokens[1].trim();
                String genus  = tokens[2].trim();
                
                if (StringUtils.isEmpty(colNum) || colNum.equals(blank)) colNum = null;
                if (StringUtils.isEmpty(year)   || year.equals(blank)) year = null;
                if (StringUtils.isEmpty(genus)  || genus.equals(blank)) genus = null;
                
                PreparedStatement gStmt1;
                if (colNum != null)
                {
                    gStmt1 = gStmt1a;
                    gStmt1.setString(1, "%"+colNum+"%");
                } else
                {
                    gStmt1 = gStmt1b;
                    gStmt1.setString(1, null);
                }
                gStmt1.setString(2, year);
                gStmt1.setString(3, genus);
                ResultSet gRS = gStmt1.executeQuery();
                
                ids.clear();
                int maxNonNullTot = -1;
                int maxNonNullInx = -1;
                int inx           = 0;
                while (gRS.next())
                {
                    
                    Object[] row = getRow();
                    int cnt = fillRowWithScore(row, gRS);
                    if (cnt > maxNonNullTot)
                    {
                        maxNonNullInx = inx;
                        maxNonNullTot = cnt;
                    }
                    group.add(row);
                    ids.add(gRS.getInt(1));
                    inx++;
                }
                gRS.close();
                
                if (inx < 2)
                {
                    for (Object[] r : group)
                    {
                        recycleRow(r);
                    }
                    group.clear();
                    continue;
                }
                
                System.arraycopy(group.get(maxNonNullInx), 0, refRow, 0, refRow.length);
                
                Integer srcId = ids.get(maxNonNullInx);
                
                for (int i=0;i<group.size();i++)
                {
                    if (i != maxNonNullInx)
                    {
                        int score = score(refRow, group.get(i));
                        
                        if (score > thresholdScore)
                        {
                            writeCnt++;
                            
                            int gbifID = ids.get(i);
                            gsStmt.setInt(1, 1);      // reltype
                            gsStmt.setInt(2, score);  // score
                            gsStmt.setInt(3, gbifID);
                            gsStmt.setInt(4, srcId);
                            gsStmt.executeUpdate();
                            
                            idHash.add(gbifID);
                        }
                    }
                }

                idHash.clear();
                
                for (Object[] r : group)
                {
                    recycleRow(r);
                }
                group.clear();
                
                if (gStmt1 == gStmt1b)
                {
                    continue;
                }
                
                gStmt1 = gStmt1b;
                gStmt1.setString(1, year);
                gStmt1.setString(2, genus);
                
                gRS = gStmt1.executeQuery();
                while (gRS.next())
                {
                   fillRowWithScore(cmpRow, gRS);
                   
                   int gbifID = gRS.getInt(1);
                   if (gbifID == srcId) continue;
                   
                   int score = score(refRow, cmpRow);
                   
                   if (score > thresholdScore)
                   {
                       writeCnt++;
                       gsStmt.setInt(1, 1);      // reltype
                       gsStmt.setInt(2, score);  // score
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
                if (gStmt1a != null)
                {
                    gStmt1a.close();
                }
                if (gStmt1b != null)
                {
                    gStmt1b.close();
                }
                /*if (gStmt2 != null)
                {
                    gStmt2.close();
                }*/
            } catch (Exception ex)
            {
                
            }
        }
        System.out.println("Done.");
        pw.println("Done.");
        pw.flush();
        pw.close();
    }
    
    /**
     * @return
     */
    private Object[] getRow()
    {
        Object[] row = rowRecycler.size() == 0 ? new Object[NUM_FIELDS] : rowRecycler.pop();
        System.arraycopy(nullRow, 0, row, 0, row.length);
        return row;
    }
    
    /**
     * @param row
     */
    private void recycleRow(final Object[] row)
    {
        rowRecycler.push(row);
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
