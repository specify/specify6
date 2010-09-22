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
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

public class AnalysisWithGBIF extends AnalysisBase
{

    /**
     * 
     */
    public AnalysisWithGBIF()
    {
        super();
     }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.toycode.mexconabio.AnalysisBase#process(int, int)
     */
    @Override
    public void process(final int type, final int options)
    {
        final double HRS = 1000.0 * 60.0 * 60.0; 
        Calendar cal = Calendar.getInstance();
        
        calcMaxScore();
        
                                     //      1         2           3        4        5           6         7          8          9               10            11      12    13    14        15
        String gbifSQL   = "SELECT DISTINCT id, catalogue_number, genus, species, subspecies, latitude, longitude, country, state_province, collector_name, locality, year, month, day, collector_num ";
        
        String fromClause1 = "FROM raw WHERE collector_num = ? AND year = ? AND genus = ?";
        String fromClause2 = "FROM raw WHERE collector_num IS NULL AND year = ? AND month = ? AND genus = ?";
        
        //                        1       2           3              4           5             6              7               8           9        10   11
        String sql121K = "SELECT BarCD, CollNr, Collectoragent1, GenusName, SpeciesName, SubSpeciesName, LocalityName, Datecollstandrd, COUNTRY, STATE, ID FROM conabio " +
        		         "WHERE CollNr IS NOT NULL ORDER BY CollNr";
        
        String gbifsnibInsert = "INSERT INTO gbifsnib (reltype, score, GBIFID, SNIBID) VALUES (?,?,?,?)";
        
        Statement         stmt   = null;
        PreparedStatement gStmt1 = null;
        PreparedStatement gStmt2 = null;
        PreparedStatement gsStmt = null;
        
        Object[] refRow = new Object[14];
        Object[] cmpRow = new Object[14];

        
        long totalRecs     = BasicSQLUtils.getCount(dbSrcConn, "SELECT COUNT(*) FROM conabio");
        long procRecs      = 0;
        long startTime     = System.currentTimeMillis();
        int  secsThreshold = 0;
        
        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter("scoring.log");
            
            gStmt1 = dbGBIFConn.prepareStatement(gbifSQL + fromClause1);
            gStmt2 = dbGBIFConn.prepareStatement(gbifSQL + fromClause2);
            gsStmt = dbDstConn.prepareStatement(gbifsnibInsert);
            
            stmt  = dbSrcConn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            System.out.println("Starting Query... "+totalRecs);
            pw.println("Starting Query... "+totalRecs);
            
            HashSet<Integer> idHash = new HashSet<Integer>();
            int writeCnt = 0;
            ResultSet rs = stmt.executeQuery(sql121K);
            
            System.out.println(String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore));
            pw.println(String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore));
            while (rs.next())
            {
                if (procRecs < 140)
                {
                    procRecs++;
                    continue;
                }
                
                String  catNum       = rs.getString(1).trim();
                String  collectorNum = rs.getString(2);
                String  collector    = rs.getString(3);
                String  genus        = rs.getString(4);
                String  species      = rs.getString(5);
                String  subspecies   = rs.getString(6);
                String  locality     = rs.getString(7);
                Date    collDate     = rs.getDate(8);
                String  country      = rs.getString(9);
                String  state        = rs.getString(10);
                
                int snibID           = rs.getInt(11);
                
                int     year;
                int     mon;
                int     day;
                
                if (collDate != null)
                {
                    cal.setTime(collDate);
                    year         = cal.get(Calendar.YEAR);
                    mon          = cal.get(Calendar.MONTH) + 1;
                    day          = cal.get(Calendar.DAY_OF_MONTH);
                } else
                {
                    year = 0;
                    mon  = 0;
                    day  = 0;
                }
                
                // Search Records with Collector Number match
                gStmt1.setString(1, collectorNum);
                gStmt1.setString(2, Integer.toString(year));
                gStmt1.setString(3, genus);
                
                refRow[CATNUM_INX]     = catNum;
                refRow[COLNUM_INX]     = collectorNum;
                refRow[GENUS_INX]      = genus;
                refRow[SPECIES_INX]    = species;
                refRow[SUBSPECIES_INX] = subspecies;
                refRow[COLLECTOR_INX]  = collector;
                refRow[LOCALITY_INX]   = locality;
                refRow[LATITUDE_INX]   = null;
                refRow[LONGITUDE_INX]  = null;
                refRow[YEAR_INX]       = year > 0 ? Integer.toString(year) : null;
                refRow[MON_INX]        = mon > 0  ? Integer.toString(mon) : null;
                refRow[DAY_INX]        = day > 0  ? Integer.toString(day) : null;
                refRow[COUNTRY_INX]    = country;
                refRow[STATE_INX]      = state;
                
                idHash.clear();
                
                ResultSet gRS = gStmt1.executeQuery();
                while (gRS.next())
                {
                   cmpRow[CATNUM_INX]     = gRS.getString(2);
                   cmpRow[COLNUM_INX]     = gRS.getString(15);
                   cmpRow[GENUS_INX]      = gRS.getString(3);
                   cmpRow[SPECIES_INX]    = gRS.getString(4);
                   cmpRow[SUBSPECIES_INX] = gRS.getString(5);
                   cmpRow[COLLECTOR_INX]  = gRS.getString(10);
                   cmpRow[LOCALITY_INX]   = gRS.getString(11);
                   cmpRow[LATITUDE_INX]   = gRS.getString(6);
                   cmpRow[LONGITUDE_INX]  = gRS.getString(7);
                   cmpRow[YEAR_INX]       = gRS.getString(12);
                   cmpRow[MON_INX]        = gRS.getString(13);
                   cmpRow[DAY_INX]        = gRS.getString(14);
                   cmpRow[COUNTRY_INX]    = gRS.getString(8);
                   cmpRow[STATE_INX]      = gRS.getString(9);
                   
                   int score = score(refRow, cmpRow);
                   
                   if (score > thresholdScore)
                   {
                       writeCnt++;
                       
                       int gbifID = gRS.getInt(1);
                       gsStmt.setInt(1, 1);     // reltype
                       gsStmt.setInt(2, score); // score
                       gsStmt.setInt(3, gbifID);    // GBIF Database
                       gsStmt.setInt(4, snibID);    // Inigo's Database
                       gsStmt.executeUpdate();
                       
                       idHash.add(gbifID);
                   }
                }
                gRS.close();
                
                // Search Records with NULL Collector Number
                gStmt2.setString(1, Integer.toString(year));
                gStmt2.setString(2, Integer.toString(mon));
                gStmt2.setString(3, genus);
                
                gRS  = gStmt2.executeQuery();
                while (gRS.next())
                {
                    int gbifID = gRS.getInt(1);
                    
                    if (idHash.contains(gbifID)) continue;
                    
                    //                 1         2           3        4        5           6         7          8          9               10            11      12    13    14        15
                    //SELECT DISTINCT id, catalogue_number, genus, species, subspecies, latitude, longitude, country, state_province, collector_name, locality, year, month, day, collector_num ";
                    
                    cmpRow[CATNUM_INX]     = gRS.getString(2);
                    cmpRow[COLNUM_INX]     = gRS.getString(15);
                    cmpRow[GENUS_INX]      = gRS.getString(3);
                    cmpRow[SPECIES_INX]    = gRS.getString(4);
                    cmpRow[SUBSPECIES_INX] = gRS.getString(5);
                    cmpRow[COLLECTOR_INX]  = gRS.getString(10);
                    cmpRow[LOCALITY_INX]   = gRS.getString(11);
                    cmpRow[LATITUDE_INX]   = gRS.getString(6);
                    cmpRow[LONGITUDE_INX]  = gRS.getString(7);
                    cmpRow[YEAR_INX]       = gRS.getString(12);
                    cmpRow[MON_INX]        = gRS.getString(13);
                    cmpRow[DAY_INX]        = gRS.getString(14);
                    cmpRow[COUNTRY_INX]    = gRS.getString(8);
                    cmpRow[STATE_INX]      = gRS.getString(9);
                    
                    int score = score(refRow, cmpRow);
                    
                    if (score > thresholdScore)
                    {
                        writeCnt++;
                        gsStmt.setInt(1, 2);     // reltype
                        gsStmt.setInt(2, score); // score
                        gsStmt.setInt(3, gbifID);
                        gsStmt.setInt(4, snibID);
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
        AnalysisWithGBIF awg = new AnalysisWithGBIF();
        awg.createDBConnection("localhost",     "3306", "plants", "root", "root");
        awg.createSrcDBConnection("localhost",  "3306", "mex",    "root", "root");
        awg.createDestDBConnection("localhost", "3306", "plants", "root", "root");
        awg.process(0,0);
        awg.cleanup();
    }
}
