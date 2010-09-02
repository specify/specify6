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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Calendar;

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
        
        String insertSQL = "INSERT INTO raw_cache (id, old_id,data_provider_id,data_resource_id,resource_access_point_id, institution_code, collection_code, " +
                           "catalogue_number, scientific_name, author, rank, kingdom, phylum, class, order_rank, family, genus, species, subspecies, latitude, longitude,  " +
                           "lat_long_precision, max_altitude, min_altitude, altitude_precision, min_depth, max_depth, depth_precision, continent_ocean, country, state_province, county, collector_name, " + 
                           "locality,year, month, day, basis_of_record, identifier_name, identification_date,unit_qualifier, created, modified, deleted, collector_num, other_collnum) " +
                           "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        String gbifSQL = "SELECT DISTINCT id, old_id,data_provider_id,data_resource_id,resource_access_point_id, institution_code, collection_code, " +
                         "catalogue_number, scientific_name, author, rank, kingdom, phylum, class, order_rank, family, genus, species, subspecies, latitude, longitude,  " +
                         "lat_long_precision, max_altitude, min_altitude, altitude_precision, min_depth, max_depth, depth_precision, continent_ocean, country, state_province, county, collector_name, " + 
                         "locality,year, month, day, basis_of_record, identifier_name, identification_date,unit_qualifier, created, modified, deleted, collector_num " +
                         "FROM raw WHERE collector_num = '%s' AND year = '%s' AND genus = '%s'";
        
        String sql = "SELECT BarCD, CollNr, Collectoragent1, GenusName, SpeciesName, LocalityName, Datecollstandrd FROM conabio " +
        		     "WHERE CollNr IS NOT NULL ORDER BY CollNr LIMIT 140,1000";
        
        Statement         stmt  = null;
        Statement         gStmt = null;
        PreparedStatement pStmt = null;
        
        long totalRecs     = BasicSQLUtils.getCount(dbSrcConn, "SELECT COUNT(*) FROM conabio");
        long procRecs      = 0;
        long startTime     = System.currentTimeMillis();
        int  secsThreshold = 0;
        
        try
        {
            stmt  = dbSrcConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            gStmt = dbGBIFConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            pStmt = dbDstConn.prepareStatement(insertSQL);
            
            BasicSQLUtils.update(dbDstConn, "DELETE FROM raw_cache WHERE id > 0");
            
            System.out.println("Starting... "+totalRecs);
            
            int writeCnt = 0;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                //String  catNum       = rs.getString(1).trim();
                String  collectorNum = rs.getString(2).trim();
                //String  collector    = rs.getString(3);
                String  genus        = rs.getString(4);
                //String  species      = rs.getString(5);
                //String  locality     = rs.getString(6);
                Date    collDate     = rs.getDate(7);
                
                if (collDate == null) continue;
                
                cal.setTime(collDate);
                int     year         = cal.get(Calendar.YEAR);
                //int     mon          = cal.get(Calendar.MONTH) + 1;
                //int     day          = cal.get(Calendar.DAY_OF_MONTH);
                
                //long start = System.currentTimeMillis();
                sql = String.format(gbifSQL, collectorNum, year, genus);
                //System.out.println(sql);
                
                ResultSet         gRS  = gStmt.executeQuery(sql);
                ResultSetMetaData rsmd = gRS.getMetaData();
                //System.out.println(String.format("Time: %8.2f", (System.currentTimeMillis()-start) / 1000.0));
                while (gRS.next())
                {
                   //System.out.println(gRS.getObject(1));
                   for (int i=1;i<=rsmd.getColumnCount();i++)
                   {
                       //System.out.println(i+" "+rsmd.getColumnName(i));
                       pStmt.setObject(i, gRS.getObject(i));
                   }
                   pStmt.setString(46, collectorNum);
                   pStmt.executeUpdate();
                   writeCnt++;
                   //System.out.println("Out: "+writeCnt);
                }
                gRS.close();
                
                procRecs++;
                if (procRecs % 1000 == 0)
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
                    }
                }
            }
            rs.close();
            
            System.out.println("Done.");
            
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
                if (gStmt != null)
                {
                    gStmt.close();
                }
                if (pStmt != null)
                {
                    pStmt.close();
                }
            } catch (Exception ex)
            {
                
            }
        }
        System.out.println("Done.");
    }
    
    //------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        AnalysisWithGBIF awg = new AnalysisWithGBIF();
        awg.createDBConnection("localhost",     "3306", "gbif",           "root", "root");
        awg.createSrcDBConnection("localhost",  "3306", "mex",            "root", "root");
        awg.createDestDBConnection("localhost", "3306", "analysis_cache", "root", "root");
        awg.process(0,0);
        awg.cleanup();
    }
}
