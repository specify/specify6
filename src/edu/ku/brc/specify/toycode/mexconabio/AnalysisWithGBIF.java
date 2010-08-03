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

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

public class AnalysisWithGBIF
{

    private Connection dbConn    = null;
    private Connection srcDBConn = null;
    
    @SuppressWarnings("unused")
    private String     dbName;
    @SuppressWarnings("unused")
    private String     srcDBName = null;
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public AnalysisWithGBIF()
    {
        super();
     }
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public void createDBConnection(final String server, 
                                   final String port, 
                                   final String dbName, 
                                   final String username, 
                                   final String pwd)
    {
        this.dbName = dbName;

        String connStr = "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&autoReconnect=true";
        try
        {
            dbConn = DriverManager.getConnection(String.format(connStr, server, port, dbName),
                    username, pwd);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
        
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public void createSrcDBConnection(final String server, 
                                      final String port, 
                                      final String dbName, 
                                      final String username, 
                                      final String pwd)
    {

        this.srcDBName = dbName;
        
        String connStr = "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&autoReconnect=true";
        try
        {
            srcDBConn = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
        
    
    /**
     * 
     */
    public void process()
    {
        Calendar cal = Calendar.getInstance();
        
        String pSQL = "INSERT INTO raw (id,data_provider_id,data_resource_id,resource_access_point_id, institution_code, collection_code, " +
        "catalogue_number, scientific_name, author, rank, kingdom, phylum, class, order_rank, family, genus, species, subspecies, latitude, longitude,  " +
        "lat_long_precision, max_altitude, min_altitude, altitude_precision, min_depth, max_depth, depth_precision, continent_ocean, country, state_province, county, collector_name, " + 
        "locality,year, month, day, basis_of_record, identifier_name, identification_date,unit_qualifier, created, modified, deleted,origcatnumber) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        String gbifSQL = "SELECT r.* AS CollNum FROM raw_occurrence_record r, identifier_record i " +
                         "WHERE r.id = i.occurrence_id AND i.identifier_type = 3 AND i.identifier = '%s' AND r.year = '%s' AND genus = '%s'";
        
        String sql = "SELECT BarCD, CollNr, Collectoragent1, GenusName, SpeciesName, LocalityName, Datecollstandrd FROM conabio " +
        		     "WHERE CollNr IS NOT NULL ORDER BY CollNr LIMIT 140,1000";
        
        Statement stmt  = null;
        Statement gStmt = null;
        PreparedStatement pStmt = null;
        
        try
        {
            stmt  = srcDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            gStmt = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            pStmt = srcDBConn.prepareStatement(pSQL);
            
            BasicSQLUtils.update(srcDBConn, "DELETE FROM raw WHERE id > 0");
            
            int cnt      = 0;
            int writeCnt = 0;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                String  catNum       = rs.getString(1).trim();
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
                
                long start = System.currentTimeMillis();
                sql = String.format(gbifSQL, collectorNum, year, genus);
                System.out.println(sql);
                
                ResultSet         gRS  = gStmt.executeQuery(sql);
                ResultSetMetaData rsmd = gRS.getMetaData();
                System.out.println(String.format("Time: %8.2f", (System.currentTimeMillis()-start)/1000.0));
                while (gRS.next())
                {
                   for (int i=1;i<=rsmd.getColumnCount();i++)
                   {
                       //System.out.println(i+" "+rsmd.getColumnName(i));
                       pStmt.setObject(i, gRS.getObject(i));
                   }
                   pStmt.setString(44, catNum);
                   pStmt.executeUpdate();
                   writeCnt++;
                   System.out.println("Out: "+writeCnt);
                }
                gRS.close();
                
                cnt++;
                //if (cnt % 100 == 0)
                //{
                    System.out.println(cnt);
                //}
            }
            rs.close();
            
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
    
    /**
     * 
     */
    public void cleanup()
    {
        try
        {
            if (dbConn != null)
            {
                dbConn.close();
            }
            if (srcDBConn != null)
            {
                srcDBConn.close();
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * @return the dbConn
     */
    public Connection getDBConn()
    {
        return dbConn;
    }

    /**
     * @return the srcDBConn
     */
    public Connection getSrcDBConn()
    {
        return srcDBConn;
    }


    
    //------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        AnalysisWithGBIF awg = new AnalysisWithGBIF();
        awg.createDBConnection("lm2gbdb.nhm.ku.edu", "3399", "gbc20091216", "rods", "specify4us");
        awg.createSrcDBConnection("localhost", "3306", "mex", "root", "root");
        awg.process();
        awg.cleanup();
    }
}
