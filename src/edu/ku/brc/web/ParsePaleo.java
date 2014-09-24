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
package edu.ku.brc.web;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 10, 2014
 *
 */
public class ParsePaleo
{
    private HashMap<String, Integer> strats = new HashMap<String, Integer>();
    private HashMap<String, Integer> ages   = new HashMap<String, Integer>();
    private String stratKeys[];

    private void createStrats(final Connection dbS3Conn)
    {
        try
        {
            String pStr = "INSERT INTO strat (_id, name) VALUES(?,?)";
            PreparedStatement pStmt = dbS3Conn.prepareStatement(pStr);

            stratKeys = new String[] {
                    "strat_occ",
                    "strat_occ_epleisto",
                    "strat_occ_mpleisto",
                    "strat_occ_lpleisto",
                    "strat_occ_eplio",
                    "strat_occ_lplio",
                    "strat_occ_emio",
                    "strat_occ_mmio",
                    "strat_occ_lmio",
                    };
            String stratTitles[] = {
                    "Unknown",
                    "Early Pleistocene",
                    "Middle Pleistocene",
                    "Late Pleistocene",
                    "Early Pliocene",
                    "Late Pliocene",
                    "Early Miocene",
                    "Middle Miocene",
                    "Late Miocene",
                    };
            
            for (int i=0;i<stratKeys.length;i++)
            {
                strats.put(stratKeys[i], i);
                pStmt.setInt(1, i);
                pStmt.setString(2, stratTitles[i]);
                if (pStmt.executeUpdate() == 0)
                {
                    System.err.println("Error inserting record.");
                }
            }
            
            pStmt.close();
            dbS3Conn.commit();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    private void createAges(final Connection dbS3Conn)
    {
        try
        {
            String stratVals[] = {
                    "strat_occ",
                    "early-pleistocene",
                    "mid-pleistocene",
                    "late-pleistocene",
                    "early-pliocene",
                    "late-pliocene",
                    "early-miocene",
                    "mid-miocene",
                    "late-miocene",
                    };
//            String stratTitles[] = { 
//                    "Unknown",
//                    "Early Pleistocene",
//                    "Middle Pleistocene",
//                    "Late Pleistocene",
//                    "Early Pliocene",
//                    "Late Pliocene",
//                    "Early Miocene",
//                    "Middle Miocene",
//                    "Late Miocene",
//                    };
            
            for (int i=0;i<stratVals.length;i++)
            {
                ages.put(stratVals[i], i);
//                pStmt.setInt(1, i);
//                pStmt.setString(2, stratTitles[i]);
//                if (pStmt.executeUpdate() == 0)
//                {
//                    System.err.println("Error inserting record.");
//                }
            }
            
//            pStmt.close();
//            dbS3Conn.commit();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    private void process()
    {

        Pattern p = Pattern.compile("\"([^\"]*)\"");

        try
        {
            DBConnection dbConn = DBConnection.getInstance();
            dbConn.setUsernamePassword("root", "root");
            dbConn.setDriver("com.mysql.jdbc.Driver");
            dbConn.setConnectionStr("jdbc:mysql://localhost/");
            dbConn.setDatabaseName("test1");
            
            dbConn.getConnection().setCatalog("test1");
        
            Connection conn = dbConn.getConnection();
            BasicSQLUtils.setDBConnection(conn);
            
            File srcFile = new File("/Users/rods/databases/paleo.sqlite");
            File dstFile = new File("/Users/rods/databases/neogene.sqlite");
            FileUtils.copyFile(srcFile, dstFile, true);
            
            Class.forName("org.sqlite.JDBC");
            Connection dbS3Conn = DriverManager.getConnection("jdbc:sqlite:" + dstFile.getAbsolutePath());
            dbS3Conn.setAutoCommit(false);
            
            createStrats(dbS3Conn);
            createAges(dbS3Conn);
            
            String pStr = "INSERT INTO data (phylum, class, family, genus, species, citation, commonname, georange, paleodist, remarks) VALUES(?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement pStmt = dbS3Conn.prepareStatement(pStr);
            
            String pStrStrat = "INSERT INTO data_strat (dataId, stratId, formation) VALUES(?,?,?)";
            PreparedStatement pStmtStrat = dbS3Conn.prepareStatement(pStrStrat);
            
            String pStrAges = "INSERT INTO data_ages (dataId, stratId) VALUES(?,?)";
            PreparedStatement pStmtAges = dbS3Conn.prepareStatement(pStrAges);
            
            String pStrAttach = "INSERT INTO attach (imgname, url, caption, type, dataId) VALUES(?,?,?,?,?)";
            PreparedStatement pStmtAttach = dbS3Conn.prepareStatement(pStrAttach);
            
            Statement stmt = conn.createStatement();
            
            HashMap<String, String> values = new HashMap<String, String>();
            String          sql = "SELECT p.ID FROM wp_xuub_posts p where p.post_type LIKE '%_page'";
            Vector<Integer> ids = BasicSQLUtils.queryForInts(sql);
            for (int recId : ids)
            {
                values.clear();
                sql = String.format("SELECT pm.meta_key, pm.meta_value FROM wp_xuub_posts p INNER JOIN wp_xuub_postmeta pm ON p.ID = pm.post_id WHERE ID = %d AND (NOT (pm.meta_key LIKE '\\_%c'))", recId, '%');
                Vector<Object[]> data = BasicSQLUtils.query(sql);
                for (Object[] row : data)
                {
                    if (row[1] != null)
                    {
                        values.put(row[0].toString(), row[1].toString());
                    }
                }
                
                System.out.println(values);
                if (values.size() == 0)
                {
                    System.out.println(sql);
                    continue;
                }
                
                int i = 1;
                pStmt.setString(i++, values.get("phylum"));
                pStmt.setString(i++, values.get("class"));
                pStmt.setString(i++, values.get("family"));
                pStmt.setString(i++, values.get("genus"));
                pStmt.setString(i++, values.get("species"));
                pStmt.setString(i++, values.get("cite"));
                pStmt.setString(i++, values.get("common_name"));
                pStmt.setString(i++, values.get("geo_range"));
                pStmt.setString(i++, values.get("paleo_dist"));
                pStmt.setString(i++, values.get("remarks"));
                
                if (pStmt.executeUpdate() == 0)
                {
                    System.err.println("Error inserting record.");
                }
                
                Integer dataId = BasicSQLUtils.getInsertedId(pStmt);
                
                String agesStr = values.get("ages");
                if (StringUtils.isNotEmpty(agesStr))
                {
                    Matcher m = p.matcher(agesStr);
                    
                    while (m.find()) 
                    {
                        String  age     = StringUtils.replace(m.group(), "\"", "");
                        Integer stratId = ages.get(age);
                        if (stratId == null)
                        {
                            System.out.println(String.format("[%s][%s]", m.group(), age));
                            stratId = 0;
                        }
                        
                        pStmtAges.setInt(1, dataId);
                        pStmtAges.setInt(2, stratId);
                        if (pStmtAges.executeUpdate() == 0)
                        {
                            System.err.println("Error inserting record.");
                        }
                    }
                }
                
                int index = 0;
                for (String stratKey : stratKeys)
                {
                    String stratStr = values.get(stratKey);
                    if (StringUtils.isNotEmpty(stratStr) && index > 0)
                    {
                        Matcher m = p.matcher(stratStr);
                        while (m.find()) 
                        {
                            String  strat   = StringUtils.replace(m.group(), "\"", "");
                            pStmtStrat.setInt(1, dataId);
                            pStmtStrat.setInt(2, index);
                            pStmtStrat.setString(3, strat);
                            if (pStmtStrat.executeUpdate() == 0)
                            {
                                System.err.println("Error inserting record.");
                            }
                        }
                    }
                    index++;
                }

                for (int k=0;k<2;k++)
                {
                    String nm = k == 0 ? "photo" : "map";
                    for (int j=1;j<9;j++)
                    {
                        String key = String.format("%s_%dd", nm, j);
                        String val = values.get(key);
                        if (StringUtils.isNotEmpty(val))
                        {
                            System.out.println(String.format("%d [%s][%s]", recId, val, key));
                            
                            sql = "SELECT p.ID, p.post_title, pm.meta_value, p.guid FROM wp_xuub_posts p " +
                                  "INNER JOIN wp_xuub_postmeta pm ON p.ID = pm.post_id WHERE pm.meta_key = '_wp_attached_file' AND p.ID = " + val;
                            ResultSet rs   = stmt.executeQuery(sql);
                            while (rs.next())
                            {
                                //int    pId      = rs.getInt(1);
                                //String title    = rs.getString(2);
                                String fileName = rs.getString(3);
                                String url      = rs.getString(4);
                                
                                String captKey = String.format("%s_%dc", nm, j);
                                String captVal = values.get(captKey);
                                
                                pStmtAttach.setString(1, fileName);
                                pStmtAttach.setString(2, url);
                                pStmtAttach.setString(3, k == 0 ? captVal : "");
                                pStmtAttach.setInt(4, k); // Type
                                pStmtAttach.setInt(5, dataId);
                                
                                if (pStmtAttach.executeUpdate() == 0)
                                {
                                    System.err.println("Error inserting record.");
                                }
                            }
                        }
                    }
                }
                // a:5:{s:5:"width";i:3300;s:6:"height";i:2550;s:4:"file";s:38:"Polygona_maxwelli_EarlyPleistocene.jpg";s:5:"sizes";
                //  a:2:{s:9:"thumbnail";
                //   a:4:{s:4:"file";s:46:"Polygona_maxwelli_EarlyPleistocene-250x250.jpg";s:5:"width";i:250;s:6:"height";i:250;s:9:"mime-type";s:10:"image/jpeg";}
                // s:6:"medium";a:4:{s:4:"file";s:48:"Polygona_maxwelli_EarlyPleistocene-2000x1545.jpg";s:5:"width";i:2000;s:6:"height";i:1545;s:9:"mime-type";s:10:"image/jpeg";}}
                //s:10:"image_meta";a:10:{s:8:"aperture";i:0;s:6:"credit";s:0:"";s:6:"camera";s:0:"";s:7:"caption";s:0:"";s:17:"created_timestamp";i:0;s:9:"copyright";s:0:"";s:12:"focal_length";i:0;s:3:"iso";i:0;s:13:"shutter_speed";i:0;s:5:"title";s:0:"";}}
//                sql = "SELECT p.ID, p.post_title, pm.meta_value, p.guid FROM wp_xuub_posts p " +
//                	  "INNER JOIN wp_xuub_postmeta pm ON p.ID = pm.post_id WHERE pm.meta_key = '_wp_attached_file' AND p.ID = " + recId;
//                ResultSet rs   = stmt.executeQuery(sql);
//                while (rs.next())
//                {
//                    int    pId      = rs.getInt(1);
//                    String title    = rs.getString(2);
//                    String fileName = rs.getString(3);
//                    String url      = rs.getString(4);
//                }

            }
            
            pStmt.close();
            pStmtStrat.close();
            pStmtAges.close();
            pStmtAttach.close();
            stmt.close();
            
            dbS3Conn.commit();
            dbS3Conn.close();
            
            DBConnection.shutdown();
            
            System.out.println("Done");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (true)
        {
            ParsePaleo pp = new ParsePaleo();
            pp.process();
            return;
        }
        try
        {
            HashSet<String> set = new HashSet<String>(); 
            for (String line : FileUtils.readLines(new File("/Users/rods/Downloads/ages.txt")))
            {
                //Pattern p = Pattern.compile("\"([^\"\\]*(\\.[^\"\\]*)*)\"|\'([^\'\\]*(\\.[^\'\\]*)*)\'");
                //Pattern p = Pattern.compile("\"([^\"]*)\"|(\\S+)");
                Pattern p = Pattern.compile("\"([^\"]*)\"");
                Matcher m = p.matcher(line);

                //List<String> animals = new ArrayList()<String>();
                while (m.find()) {
                    //System.out.println(m.group());
                    set.add(StringUtils.replace(m.group(), "\"", ""));
                    //animals.add(m.group());
                }
            }
            for (String str : set)
            {
                System.out.println(str);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
