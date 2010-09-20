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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

public class MakeGBIFProcessHash extends AnalysisBase
{
    PrintWriter pw = null;
    Statement         stmt       = null;
    PreparedStatement insertStmt = null;
    long totalRecs;
    HashSet<String> groupHash = new HashSet<String>();
    int writeCnt = 0;
    
    /**
     * 
     */
    public MakeGBIFProcessHash()
    {
        super();
    }
    
    private void writeHash() throws SQLException
    {
        System.out.println("Writing Hash...");
        pw.println("Writing Hash...");
        
        long sTm = System.currentTimeMillis();
        int err = 0;
        int cnt = 0;
        for (String nm : groupHash)
        {
            insertStmt.setString(1, nm);
            try
            {
                insertStmt.executeUpdate();
                writeCnt++;
                cnt++;
            } catch (Exception ex) { err++;}
        }
        groupHash.clear();
        
        long elapsed = (System.currentTimeMillis() - sTm) / 1000;
        String msg = String.format("Done Writing Dups: %d  Cnt: %d Elapsed: %d", err, cnt, elapsed);
        System.out.println(msg);
        pw.println(msg);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.toycode.mexconabio.AnalysisBase#process(int, int)
     */
    @Override
    public void process(final int type, final int options)
    {
        final double HRS = 1000.0 * 60.0 * 60.0; 
        
        String gbifSQL        = "SELECT  collector_num, year, genus FROM raw LIMIT 900000,10000000";
        String gbifsnibInsert = "INSERT INTO group_hash (name) VALUES (?)";
        
        totalRecs     = 51253307;//BasicSQLUtils.getCount(dbGBIFConn, "SELECT COUNT(*) FROM raw");
        long procRecs      = 0;
        long startTime     = System.currentTimeMillis();
        int  secsThreshold = 0;
        
        try
        {
            pw = new PrintWriter("GroupHash.log");
            
            insertStmt = dbDstConn.prepareStatement(gbifsnibInsert);
            
            stmt       = dbGBIFConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.FETCH_FORWARD);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            System.out.println("Starting Query... "+totalRecs);
            pw.println("Starting Query... "+totalRecs);
            
            ResultSet rs = stmt.executeQuery(gbifSQL);
            
            String msg = String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore);
            System.out.println(msg);
            pw.println(msg);
            
            while (rs.next())
            {
                String  collectorNum = rs.getString(1);
                String  genus        = rs.getString(2);
                String  year         = rs.getString(3);
                
                collectorNum = StringUtils.isNotEmpty(collectorNum) ? collectorNum : "X?";
                genus        = StringUtils.isNotEmpty(genus) ? genus : "X?";
                year         = StringUtils.isNotEmpty(year) ? year : "X?";
                
                String name = String.format("%s_%s_%s", collectorNum, genus, year);
                groupHash.add(name);
                
                if (groupHash.size() > 100000)
                {
                    writeHash();
                }
                
                procRecs++;
                if (procRecs % 100 == 0)
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
            
            if (groupHash.size() > 0)
            {
                writeHash();
            }
            
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
                if (insertStmt != null)
                {
                    insertStmt.close();
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
        MakeGBIFProcessHash awg = new MakeGBIFProcessHash();
        awg.createDBConnection("localhost",     "3306", "plants", "root", "root");
        awg.createDestDBConnection("localhost", "3306", "plants", "root", "root");
        awg.process(0,0);
        awg.cleanup();
    }
}
