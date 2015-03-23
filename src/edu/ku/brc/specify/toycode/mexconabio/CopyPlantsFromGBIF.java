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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 23, 2010
 *
 */
public class CopyPlantsFromGBIF
{
    private static final double HRS = 1000.0 * 60.0 * 60.0; 
    private static final String TAXSEARCH_GNSP_SQL = "SELECT * FROM raw WHERE genus = ? AND species = ? LIMIT 0,1";
    private static final String TAXSEARCH_GN_SQL   = "SELECT * FROM raw WHERE genus = ? LIMIT 0,1";
    
    private static final String pSQL = "INSERT INTO raw (old_id,data_provider_id,data_resource_id,resource_access_point_id, institution_code, collection_code, " +
    "catalogue_number, scientific_name, author, rank, kingdom, phylum, class, order_rank, family, genus, species, subspecies, latitude, longitude,  " +
    "lat_long_precision, max_altitude, min_altitude, altitude_precision, min_depth, max_depth, depth_precision, continent_ocean, country, state_province, county, collector_name, " + 
    "locality,year, month, day, basis_of_record, identifier_name, identification_date,unit_qualifier, created, modified, deleted, collector_num) " +
    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String gbifSQLBase = "SELECT old_id, data_provider_id,data_resource_id,resource_access_point_id, institution_code, collection_code, " +
           "catalogue_number, scientific_name, author, rank, kingdom, phylum, class, order_rank, family, genus, species, subspecies, latitude, longitude,  " +
           "lat_long_precision, max_altitude, min_altitude, altitude_precision, min_depth, max_depth, depth_precision, continent_ocean, country, state_province, county, collector_name, " + 
           "locality,year, month, day, basis_of_record, identifier_name, identification_date,unit_qualifier, created, modified, deleted, collector_num ";

    private Connection srcConn = null;
    private Connection dstConn = null;
    private Connection colConn = null;
    
    private PreparedStatement colStmtGNSP    = null;
    private PreparedStatement colStmtGN      = null;
    private PreparedStatement colDstStmtGNSP = null;
    private PreparedStatement colDstStmtGN   = null;
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public CopyPlantsFromGBIF()
    {
        super();
     }
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     * @throws SQLException 
     */
    public void connectToDst(final String server, 
                             final String port, 
                             final String dbName, 
                             final String username, 
                             final String pwd) throws SQLException
    {
        dstConn    = connect(server, port, dbName, username, pwd);
        
        colDstStmtGNSP = dstConn.prepareStatement(TAXSEARCH_GNSP_SQL);
        colDstStmtGNSP.setFetchSize(Integer.MIN_VALUE);
        
        colDstStmtGN = dstConn.prepareStatement(TAXSEARCH_GN_SQL);
        colDstStmtGN.setFetchSize(Integer.MIN_VALUE);
    }
        
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public void connectToSrc(final String server, 
                             final String port, 
                             final String dbName, 
                             final String username, 
                             final String pwd)
    {
        srcConn = connect(server, port, dbName, username, pwd);
    }
        
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     * @throws SQLException 
     */
    public void connectToCOLTaxa(final String server, 
                                 final String port, 
                                 final String dbName, 
                                 final String username, 
                                 final String pwd) throws SQLException 
    {
        colConn = connect(server, port, dbName, username, pwd);
        
        colStmtGNSP = dstConn.prepareStatement(TAXSEARCH_GNSP_SQL);
        colStmtGNSP.setFetchSize(Integer.MIN_VALUE);
        
        colStmtGN = dstConn.prepareStatement(TAXSEARCH_GN_SQL);
        colStmtGN.setFetchSize(Integer.MIN_VALUE);
    }
        
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    private Connection connect(final String server, 
                               final String port, 
                               final String dbName, 
                               final String username, 
                               final String pwd)
    {

        String connStr = "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&autoReconnect=true";
        try
        {
            return DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    
    /**
     * @param genus
     * @param species
     * @return
     */
    private boolean isPlant(final PreparedStatement pStmtGN,
                            final PreparedStatement pStmtGNSP,
                            final String genus, 
                            final String species)
    {
        ResultSet rs = null;
        try
        {
            PreparedStatement pStmt;
            if (species == null)
            {
                pStmt = pStmtGN;
            } else
            {
                pStmt = pStmtGNSP;
                pStmt.setString(2, species); 
            }
            pStmt.setString(1, genus);
            

            rs = pStmt.executeQuery();
            if (rs.next())
            {
                return true;
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            System.err.println(pStmtGN+"  "+pStmtGNSP+" "+genus+"  "+species);
            
        } finally
        {
            try
            {
                if (rs != null) rs.close();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }
        
    /**
     * 
     */
    public void processNullKingdom()
    {
        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter("gbif_plants_from_null.log");
            
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        
        System.out.println("----------------------- Searching NULL ----------------------- ");
        
        String gbifWhereStr = "FROM raw WHERE kingdom IS NULL";
        
        long startTime   = System.currentTimeMillis();
        
        String cntGBIFSQL = "SELECT COUNT(*) " + gbifWhereStr;// + " LIMIT 0,1000";
        String gbifSQL    = gbifSQLBase + gbifWhereStr;

        System.out.println(cntGBIFSQL);
        
        long totalRecs   = BasicSQLUtils.getCount(srcConn, cntGBIFSQL);
        long procRecs    = 0;
        int  secsThreshold = 0;
        
        String msg = String.format("Query: %8.2f secs",  (double)(System.currentTimeMillis() - startTime) / 1000.0);
        System.out.println(msg);
        pw.println(msg);
        pw.flush();

        startTime = System.currentTimeMillis();
        
        Statement         gStmt = null;
        PreparedStatement pStmt = null;
        
        try
        {
            pw = new PrintWriter("gbif_plants_from_null.log");
            
            pStmt = dstConn.prepareStatement(pSQL);
            
            System.out.println("Total Records: "+totalRecs);
            pw.println("Total Records: "+totalRecs);
            
            gStmt = srcConn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            gStmt.setFetchSize(Integer.MIN_VALUE);
            
            ResultSet         rs   = gStmt.executeQuery(gbifSQL);
            ResultSetMetaData rsmd = rs.getMetaData();
            
            while (rs.next())
            {
                String genus = rs.getString(16);
                if (genus == null) continue;
                
                String species = rs.getString(17);
                
                if (isPlant(colStmtGN, colStmtGNSP, genus, species) || isPlant(colDstStmtGN, colDstStmtGNSP, genus, species))
                {
                
                    for (int i=1;i<=rsmd.getColumnCount();i++)
                    {
                        Object obj = rs.getObject(i);
                        pStmt.setObject(i, obj);
                    }
                    
                    try
                    {
                        pStmt.executeUpdate();
                        
                    } catch (Exception ex)
                    {
                        System.err.println("For Old ID["+rs.getObject(1)+"]");
                        ex.printStackTrace();
                        pw.print("For Old ID["+rs.getObject(1)+"] "+ex.getMessage());
                        pw.flush();
                    }
                    
                    procRecs++;
                    if (procRecs % 10000 == 0)
                    {
                        long endTime     = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;
                        
                        double avergeTime = (double)elapsedTime / (double)procRecs;
                        
                        double hrsLeft = (((double)elapsedTime / (double)procRecs) * (double)totalRecs - procRecs)  / HRS;
                        
                        int seconds = (int)(elapsedTime / 60000.0);
                        if (secsThreshold != seconds)
                        {
                            secsThreshold = seconds;
                            
                            msg = String.format("Elapsed %8.2f hr.mn   Ave Time: %5.2f    Percent: %6.3f  Hours Left: %8.2f ", 
                                    ((double)(elapsedTime)) / HRS, 
                                    avergeTime,
                                    100.0 * ((double)procRecs / (double)totalRecs),
                                    hrsLeft);
                            System.out.println(msg);
                            pw.println(msg);
                            pw.flush();
                        }
                    }
                }
            }

            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally 
        {
            try
            {
                if (gStmt != null)
                {
                    gStmt.close();
                }
                if (pStmt != null)
                {
                    pStmt.close();
                }
                pw.close();
                
            } catch (Exception ex)
            {
                
            }
        }
        System.out.println("Done transferring.");
        pw.println("Done transferring.");
    }
    
    /**
     * 
     */
    public void processNonNullNonPlantKingdom()
    {
        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter("gbif_plants_from_nonnull.log");
            
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        
        System.out.println("----------------------- Search non-Plantae ----------------------- ");
        
        String gbifWhereStr = "FROM raw WHERE kingdom = '%s'";
        
        Vector<String> nonPlantKingdoms = new Vector<String>();
        String sqlStr = "SELECT * FROM (select kingdom, count(kingdom) as cnt from plants.raw WHERE kingdom is not null AND NOT (lower(kingdom) like '%plant%') group by kingdom) T1 ORDER BY cnt desc;";
        for (Object[] obj : BasicSQLUtils.query(sqlStr))
        {
            String  kingdom = (String)obj[0];
            Integer count   = (Integer)obj[1];
            
            System.out.println(kingdom + " " + count);
            pw.println(kingdom + " " + count);
            if (!StringUtils.contains(kingdom.toLowerCase(), "plant"))
            {
                nonPlantKingdoms.add(kingdom);
            }
        }

        long startTime   = System.currentTimeMillis();
        
        for (String kingdom : nonPlantKingdoms)
        {
            String where = String.format(gbifWhereStr, kingdom);
            
            String cntGBIFSQL = "SELECT COUNT(*) " + where;
            String gbifSQL    = gbifSQLBase + where;
    
            System.out.println(cntGBIFSQL);
            
            long totalRecs     = BasicSQLUtils.getCount(srcConn, cntGBIFSQL);
            long procRecs      = 0;
            int  secsThreshold = 0;
            
            String msg = String.format("Query: %8.2f secs",  (double)(System.currentTimeMillis() - startTime) / 1000.0);
            System.out.println(msg);
            pw.println(msg);
            pw.flush();
    
            startTime = System.currentTimeMillis();
            
            Statement         gStmt = null;
            PreparedStatement pStmt = null;
            
            try
            {
                pStmt = dstConn.prepareStatement(pSQL);
                
                System.out.println("Total Records: "+totalRecs);
                pw.println("Total Records: "+totalRecs);
                pw.flush();
                
                gStmt = srcConn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                gStmt.setFetchSize(Integer.MIN_VALUE);
                
                ResultSet         rs   = gStmt.executeQuery(gbifSQL);
                ResultSetMetaData rsmd = rs.getMetaData();
                
                while (rs.next())
                {
                    String genus = rs.getString(16);
                    if (genus == null) continue;
                    
                    String species = rs.getString(17);
                    
                    if (isPlant(colStmtGN, colStmtGNSP, genus, species) || isPlant(colDstStmtGN, colDstStmtGNSP, genus, species))
                    {
                    
                        for (int i=1;i<=rsmd.getColumnCount();i++)
                        {
                            Object obj = rs.getObject(i);
                            pStmt.setObject(i, obj);
                        }
                        
                        try
                        {
                            pStmt.executeUpdate();
                            
                        } catch (Exception ex)
                        {
                            System.err.println("For Old ID["+rs.getObject(1)+"]");
                            ex.printStackTrace();
                            pw.print("For Old ID["+rs.getObject(1)+"] "+ex.getMessage());
                            pw.flush();
                        }
                        
                        procRecs++;
                        if (procRecs % 10000 == 0)
                        {
                            long endTime     = System.currentTimeMillis();
                            long elapsedTime = endTime - startTime;
                            
                            double avergeTime = (double)elapsedTime / (double)procRecs;
                            
                            double hrsLeft = (((double)elapsedTime / (double)procRecs) * (double)totalRecs - procRecs)  / HRS;
                            
                            int seconds = (int)(elapsedTime / 60000.0);
                            if (secsThreshold != seconds)
                            {
                                secsThreshold = seconds;
                                
                                msg = String.format("Elapsed %8.2f hr.mn   Ave Time: %5.2f    Percent: %6.3f  Hours Left: %8.2f ", 
                                        ((double)(elapsedTime)) / HRS, 
                                        avergeTime,
                                        100.0 * ((double)procRecs / (double)totalRecs),
                                        hrsLeft);
                                System.out.println(msg);
                                pw.println(msg);
                                pw.flush();
                            }
                        }
                    }
                }
    
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                
            } finally 
            {
                try
                {
                    if (gStmt != null)
                    {
                        gStmt.close();
                    }
                    if (pStmt != null)
                    {
                        pStmt.close();
                    }
                    pw.close();
                    
                } catch (Exception ex)
                {
                    
                }
            }
        }
        System.out.println("Done transferring.");
        pw.println("Done transferring.");
        
    }
    

    /**
     * 
     */
    public void cleanup()
    {
        try
        {
            if (colStmtGNSP != null)
            {
                colStmtGNSP.close();
            }
            if (colStmtGN != null)
            {
                colStmtGN.close();
            }
            if (colDstStmtGNSP != null)
            {
                colDstStmtGNSP.close();
            }
            if (colDstStmtGN != null)
            {
                colDstStmtGN.close();
            }
            
            if (colConn != null)
            {
                colConn.close();
            }
            if (dstConn != null)
            {
                dstConn.close();
            }
            if (srcConn != null)
            {
                srcConn.close();
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    //------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        CopyPlantsFromGBIF app = new CopyPlantsFromGBIF();
        
        try
        {
            app.connectToSrc("localhost",     "3306", "gbif",     "root", "root");
            app.connectToDst("localhost",     "3306", "plants",   "root", "root");
            app.connectToCOLTaxa("localhost", "3306", "col_taxa", "root", "root");
            app.processNullKingdom();
            //app.processNonNullNonPlantKingdom();
            app.cleanup();
            
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /*
     Query for transferring plant data:
     
     INSERT INTO plants.raw (id, old_id,data_provider_id,data_resource_id,resource_access_point_id, institution_code, collection_code, 
     catalogue_number, scientific_name, author, rank, kingdom, phylum, class, order_rank, family, genus, species, subspecies, 
     latitude, longitude, lat_long_precision, max_altitude, min_altitude, altitude_precision, min_depth, max_depth, depth_precision, 
     continent_ocean, country, state_province, county, collector_name, locality,year, month, day, basis_of_record, identifier_name, 
     identification_date,unit_qualifier, created, modified, deleted, collector_num) SELECT id, old_id,data_provider_id,data_resource_id,
     resource_access_point_id, institution_code, collection_code,    catalogue_number, scientific_name, author, rank, kingdom, phylum, 
     class, order_rank, family, genus, species, subspecies, latitude, longitude,
     lat_long_precision, max_altitude, min_altitude, altitude_precision, min_depth, max_depth, depth_precision, continent_ocean, country, 
     state_province, county, collector_name, locality,year, month, day, basis_of_record, identifier_name, identification_date,unit_qualifier, 
     created, modified, deleted, collector_num FROM raw WHERE lower(kingdom) LIKE "%plant%";
     */
}
