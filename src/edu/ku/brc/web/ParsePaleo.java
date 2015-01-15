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

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.replace;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

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
    private DBConnection dbConn        = null;
    private Connection   dbS3Conn      = null;
    private boolean      isInitialized = false;
    private TreeNode     rootNode      = null;
    
    private HashMap<String, Integer> strats = new HashMap<String, Integer>();
    private HashMap<String, Integer> ages   = new HashMap<String, Integer>();
    private String                   stratKeys[];
    
    private HashMap<String, Integer> taxonNameToId = new HashMap<String, Integer>();
    private PreparedStatement taxonInsertStmt = null;
    
    private PrintWriter pw = null;

    /**
     * 
     */
    private void startUp()
    {
        try
        {
            dbConn = DBConnection.getInstance();
            dbConn.setUsernamePassword("root", "root");
            dbConn.setDriver("com.mysql.jdbc.Driver");
            dbConn.setConnectionStr("jdbc:mysql://localhost/");
            dbConn.setDatabaseName("test1");
            
            dbConn.getConnection().setCatalog("digitalatlas");
        
            Connection conn = dbConn.getConnection();
            BasicSQLUtils.setDBConnection(conn);
            
            File srcFile = new File("/Users/rods/databases/digitalatlas.sqlite");
            File dstFile = new File("/Users/rods/databases/daal.sqlite");
            FileUtils.copyFile(srcFile, dstFile, true);
            
            Class.forName("org.sqlite.JDBC");
            dbS3Conn = DriverManager.getConnection("jdbc:sqlite:" + dstFile.getAbsolutePath());
            dbS3Conn.setAutoCommit(false);
            
            String pStr = "INSERT INTO taxon (name, commonname, parentId, rankId) VALUES(?,?,?,?)";
            taxonInsertStmt = dbS3Conn.prepareStatement(pStr);

            isInitialized = true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    private void shutdown()
    {
        try
        {
            dbS3Conn.close();
            taxonInsertStmt.close();
            
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DBConnection.shutdown();
    }
    
    /**
     * @param parentNode
     * @param names
     * @param ranks
     * @param level
     * @throws SQLException
     */
    private void buildTree(final TreeNode parentNode, String[] names, int[] ranks, final int level, final String common, final int len) throws SQLException
    {
        if (isNotEmpty(names[level]))
        {
            String commonname = level+1 == len ? common : null;
            TreeNode childNode = parentNode.addKid(names[level], ranks[level], commonname);
            if (childNode != null && level+1 < ranks.length)
            {
                buildTree(childNode, names, ranks, level+1, common, len);
            }
        }
    }
    
    /**
     * @param parentNode
     * @param names
     * @param level
     * @param len
     * @return
     */
    private TreeNode getTreeNode(final TreeNode parentNode, final String[] names, final int level, final int len)
    {
        if (isNotEmpty(names[level]))
        {
            TreeNode childNode = parentNode.nodeForName(names[level]);
            if (childNode != null && level+1 < len)
            {
                return getTreeNode(childNode, names, level+1, len);
            }
            return childNode;
        }
        return null;
    }
    
    /**
     * 
     */
    private void buildTaxonTree()
    {
        try
        {
            Connection conn = dbConn.getConnection();

            int rootRecId = -1;
            taxonInsertStmt.setString(1, "Root");
            taxonInsertStmt.setString(2, null);
            taxonInsertStmt.setInt(3,    0); // ParentID
            taxonInsertStmt.setInt(4,    0); // RankID
            int rv = taxonInsertStmt.executeUpdate();
            if (rv == 1)
            {
                Integer recId = BasicSQLUtils.getInsertedId(taxonInsertStmt);
                if (recId != null)
                {
                    rootRecId = recId;
                }
            }
            if (rootRecId == -1)
            {
                throw new RuntimeException("Bad Root Taxon Node.");
            }
            
            rootNode = new TreeNode(rootRecId, "Root", 0, 0, null);
            
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
                
                String phylum  = values.get("phylum");
                String clazz   = values.get("class");
                String family  = values.get("family");
                String genus   = values.get("genus");
                String species = values.get("species");
                String common  = values.get("common_name");
                
                String[] names = {phylum, clazz, family, genus, species};
                int[]    ranks = {30,        60,    140,   180,     220};
                
                int len = 0;
                while (len < names.length && names[len] != null)
                {
                    len++;
                }
                buildTree(rootNode, names, ranks, 0, common, len);
            }
            stmt.close();
            
            dbS3Conn.commit();
            
            System.out.println("Done with taxon tree.");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
    *
    */
   private void createStrats()
   {
       try
       {
           String pStr = "INSERT INTO strat (_id, name, age) VALUES(?,?,?)";
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
                   "Early Pleistocene",   // 3
                   "Middle Pleistocene",  // 2
                   "Late Pleistocene",    // 1
                   "Early Pliocene ",     // 5
                   "Late Pliocene",       // 4
                   "Early Miocene",       // 8
                   "Middle Miocene",      // 7
                   "Late Miocene",        // 6
                   };
           
           int ages[] = {
                   0,
                   3,
                   2,
                   1,
                   5,
                   4,
                   8,
                   7,
                   6,
                   };
           
           for (int i=0;i<stratKeys.length;i++)
           {
               strats.put(stratKeys[i], i);
               pStmt.setInt(1, i);
               pStmt.setString(2, stratTitles[i]);
               pStmt.setInt(3, ages[i]);
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
   
   /**
    * 
    */
   private void createAges()
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
//           String stratTitles[] = { 
//                   "Unknown",
//                   "Early Pleistocene",
//                   "Middle Pleistocene",
//                   "Late Pleistocene",
//                   "Early Pliocene",
//                   "Late Pliocene",
//                   "Early Miocene",
//                   "Middle Miocene",
//                   "Late Miocene",
//                   };
           
           for (int i=0;i<stratVals.length;i++)
           {
               ages.put(stratVals[i], i);
//               pStmt.setInt(1, i);
//               pStmt.setString(2, stratTitles[i]);
//               if (pStmt.executeUpdate() == 0)
//               {
//                   System.err.println("Error inserting record.");
//               }
           }
           
//           pStmt.close();
//           dbS3Conn.commit();

       } catch (Exception ex)
       {
           ex.printStackTrace();
       }
   }
   
    
    /**
     * 
     */
    private void processAll()
    {
        startUp();
        if (isInitialized)
        {
            buildTaxonTree();
            process();
        }
        shutdown();
    }
    
    
    /**
     * @throws SQLException 
     * 
     */
    private int recurseForAttachments(final PreparedStatement pStmtTaxonAttach, final TreeNode node) throws SQLException
    {
        if (node.rankId == 220)
        {
            Vector<Integer> ids = BasicSQLUtils.queryForInts(dbS3Conn, String.format("SELECT attachId FROM taxon_attach WHERE taxonId = %d", node.recId));
            if (ids.size() > 0)
            {
                return ids.get(0);
            }
            throw new RuntimeException("No image for taxonId: "+node.recId);
        }

        int firstId = -1;
        for (TreeNode kidNode : node.kids)
        {
            int attachId = recurseForAttachments(pStmtTaxonAttach, kidNode);
            if (firstId == -1)
            {
                firstId = attachId;
            }
            if (kidNode.rankId > 0)
            {
                pStmtTaxonAttach.setInt(1, kidNode.recId);
                pStmtTaxonAttach.setInt(2, attachId);
                if (pStmtTaxonAttach.executeUpdate() == 0)
                {
                    System.err.println("Error inserting record.");
                }
            }
        }
        return firstId;
    }
    
    
    /**
     * 
     */
    private void process()
    {

        Pattern p = Pattern.compile("\"([^\"]*)\"");

        try
        {
            pw = new PrintWriter("filedownload.sh");
            
            Connection conn = dbConn.getConnection();
            
            createStrats();
            createAges();
            
            String pStr = "INSERT INTO data (citation, georange, paleodist, remarks, taxonId) VALUES(?,?,?,?,?)";
            PreparedStatement pStmt = dbS3Conn.prepareStatement(pStr);
            
            String pStrStrat = "INSERT INTO data_strat (dataId, stratId, formation) VALUES(?,?,?)";
            PreparedStatement pStmtStrat = dbS3Conn.prepareStatement(pStrStrat);
            
            String pStrAges = "INSERT INTO data_ages (dataId, stratId) VALUES(?,?)";
            PreparedStatement pStmtAges = dbS3Conn.prepareStatement(pStrAges);
            
            String pStrAttach = "INSERT INTO attach (imgname, url, caption, type) VALUES(?,?,?,?)";
            PreparedStatement pStmtAttach = dbS3Conn.prepareStatement(pStrAttach);
            
            String pStrTaxonAttach = "INSERT INTO taxon_attach (taxonId, attachId) VALUES(?,?)";
            PreparedStatement pStmtTaxonAttach = dbS3Conn.prepareStatement(pStrTaxonAttach);
            
            Statement stmt  = conn.createStatement();
            Statement stmt2 = conn.createStatement();
            
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
                
                String phylum  = values.get("phylum");
                String clazz   = values.get("class");
                String family  = values.get("family");
                String genus   = values.get("genus");
                String species = values.get("species");
                String[] names = {phylum, clazz, family, genus, species};
                int len = 0;
                while (len < names.length && names[len] != null)
                {
                    len++;
                }
                TreeNode node = getTreeNode(rootNode, names, 0, len);
                if (node == null)
                {
                    node = getTreeNode(rootNode, names, 0, len);
                    throw new RuntimeException("Could find tree node" + names);
                }
                
                int i = 1;
                pStmt.setString(i++, getStrValue(values, "cite", recId));
                pStmt.setString(i++, getStrValue(values, "geo_range", recId));
                pStmt.setString(i++, getStrValue(values, "paleo_dist", recId));
                pStmt.setString(i++, getStrValue(values, "remarks", recId));
                pStmt.setInt(i++,    node.recId);
                
                if (pStmt.executeUpdate() == 0)
                {
                    System.err.println("Error inserting record.");
                }
                
                Integer dataId = BasicSQLUtils.getInsertedId(pStmt);
                
                String agesStr = values.get("ages");
                if (isNotEmpty(agesStr))
                {
                    Matcher m = p.matcher(agesStr);
                    
                    while (m.find()) 
                    {
                        String  age     = replace(m.group(), "\"", "");
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
                    if (isNotEmpty(stratStr) && index > 0)
                    {
                        Matcher m = p.matcher(stratStr);
                        while (m.find()) 
                        {
                            String  strat   = replace(m.group(), "\"", "");
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

                sql = String.format("SELECT p.ID, post_title, p.post_name, p.post_type, pm.meta_value FROM wp_xuub_posts p " +
                		            "INNER JOIN wp_xuub_postmeta pm ON p.ID = pm.post_id " +
                		            "WHERE post_parent = %d AND post_type = 'attachment' AND NOT (pm.meta_value LIKE 'a:%c')", recId, '%', '%');
                System.out.println(sql);
                ResultSet rsId = stmt2.executeQuery(sql);
                while (rsId.next())
                {
                    int pId      = rsId.getInt(1);
                    String title = rsId.getString(2);
                    if (title.contains("2000px")) continue;
                    
                    sql = "SELECT p.ID, p.post_title, pm.meta_value, p.guid FROM wp_xuub_posts p INNER JOIN wp_xuub_postmeta pm ON p.ID = pm.post_id WHERE pm.meta_key = '_wp_attached_file' AND p.ID = " + pId;
                    ResultSet rs   = stmt.executeQuery(sql);
                    while (rs.next())
                    {
                        String fileName = rs.getString(3);
                        String url      = rs.getString(4);
                        
                        if (fileName.contains("Early") || fileName.contains("Middle") || fileName.contains("Late"))
                        {
                            continue;
                        }
                        
                        String path = "/Users/rods/Documents/XCodeProjects/DigitalAtlasAcientLife/DigitalAtlasAcientLife/Resources/images/" + fileName;
                        File file = new File(path);
                        if (!file.exists())
                        {
                            pw.println(String.format("curl %s > %s", url, fileName));
                        }
                        
                        //String captKey = String.format("%s_%dc", nm, j);
                        //String captVal = values.get(captKey);
                        
                        pStmtAttach.setString(1, fileName);
                        pStmtAttach.setString(2, url);
                        pStmtAttach.setString(3, "");
                        pStmtAttach.setInt(4,    0); // Type
                        
                        if (pStmtAttach.executeUpdate() == 0)
                        {
                            System.err.println("Error inserting record.");
                        }
                        
                        Integer attachId = BasicSQLUtils.getInsertedId(pStmtAttach);
                        if (attachId == null)
                        {
                            throw new RuntimeException("Error saving attachment record.");
                        }
                        
                        pStmtTaxonAttach.setInt(1, node.recId);
                        pStmtTaxonAttach.setInt(2, attachId);
                        if (pStmtTaxonAttach.executeUpdate() == 0)
                        {
                            System.err.println("Error inserting record.");
                        }
                    }
                    rs.close();
                }
                rsId.close();
                
                /*
                for (int k=0;k<1;k++)
                {
                    String nm = k == 0 ? "photo" : "map";
                    for (int j=1;j<9;j++)
                    {
                        String key = String.format("%s_%dd", nm, j);
                        String val = values.get(key);
                        if (isNotEmpty(val))
                        {
                            System.out.println(String.format("%d [%s][%s]", recId, val, key));
                            
                            sql = "SELECT p.ID, p.post_title, pm.meta_value, p.guid FROM wp_xuub_posts p INNER JOIN wp_xuub_postmeta pm ON p.ID = pm.post_id WHERE pm.meta_key = '_wp_attached_file' AND p.ID = " + val;
                            ResultSet rs   = stmt.executeQuery(sql);
                            while (rs.next())
                            {
                                //int    pId      = rs.getInt(1);
                                //String title    = rs.getString(2);
                                String fileName = rs.getString(3);
                                String url      = rs.getString(4);
                                
                                pw.println(String.format("curl %s > %s", url, fileName));
                                
                                String captKey = String.format("%s_%dc", nm, j);
                                String captVal = values.get(captKey);
                                
                                pStmtAttach.setString(1, fileName);
                                pStmtAttach.setString(2, url);
                                pStmtAttach.setString(3, k == 0 ? captVal : "");
                                pStmtAttach.setInt(4, k); // Type
                                
                                if (pStmtAttach.executeUpdate() == 0)
                                {
                                    System.err.println("Error inserting record.");
                                }
                                
                                Integer attachId = BasicSQLUtils.getInsertedId(pStmtAttach);
                                if (attachId == null)
                                {
                                    throw new RuntimeException("Error saving attachment record.");
                                }
                                
                                pStmtTaxonAttach.setInt(1, node.recId);
                                pStmtTaxonAttach.setInt(2, attachId);
                                if (pStmtTaxonAttach.executeUpdate() == 0)
                                {
                                    System.err.println("Error inserting record.");
                                }
                            }
                        }
                    }
                }*/
                
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
            dbS3Conn.commit();
            
            //recurseForAttachments(pStmtTaxonAttach, rootNode);
           
            stmt.close();
            stmt2.close();
            pStmt.close();
            pStmtStrat.close();
            pStmtAges.close();
            pStmtAttach.close();
            pStmtTaxonAttach.close();
            stmt.close();
            
            dbS3Conn.commit();

            pw.close();
            
            System.out.println("Done");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * @param values
     * @param key
     * @param recId
     * @return
     */
    private String getStrValue(final HashMap<String, String> values, final String key, final int recId)
    {
        String value = values.get(key);
//        if (isEmpty(value))
//        {
//            if (!key.equals("common_name") && !key.equals("remarks"))
//            {
//                System.err.println(String.format("Value is null for key [%s] for id: %d", key, recId));
//            }
//            return null;
//        }
        return value;
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (true)
        {
            ParsePaleo pp = new ParsePaleo();
            pp.processAll();
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
                    set.add(replace(m.group(), "\"", ""));
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

    
    class TreeNode
    {
        int    recId;
        String name;
        int    rankId;
        int    parentId;
        String common;
        
        Vector<TreeNode> kids = new Vector<TreeNode>();
        HashMap<String, TreeNode> kidsMap = new HashMap<String, TreeNode>();

        /**
         * @param recId
         * @param name
         * @param rankId
         * @param parentId
         * @param common
         */
        public TreeNode(final int recId, final String name, final int rankId, final int parentId, final String common)
        {
            super();
            this.recId    = recId;
            this.name     = name;
            this.common   = common;
            this.rankId   = rankId;
            this.parentId = parentId;
        }
        
        public TreeNode addKid(final String kidName, final int kidRankId, final String kidCommon) throws SQLException
        {
            TreeNode node = kidsMap.get(kidName);
            if (node == null)
            {
                taxonInsertStmt.setString(1, kidName);
                taxonInsertStmt.setString(2, kidCommon);
                taxonInsertStmt.setInt(3,    this.recId); // ParentId
                taxonInsertStmt.setInt(4,    kidRankId);
                
                int rv = taxonInsertStmt.executeUpdate();
                if (rv == 1)
                {
                    Integer kidRecId = BasicSQLUtils.getInsertedId(taxonInsertStmt);
                    if (kidRecId != null)
                    {
                        node = new TreeNode(kidRecId, kidName, kidRankId, this.recId, kidCommon);
                        kids.add(node); 
                        kidsMap.put(kidName, node);
                    } else
                    {
                        throw new RuntimeException("Can't insert taxon.");
                    }
                } else
                {
                    throw new RuntimeException("Can't insert taxon.");
                }
            }
            return node;
        }
        
        public TreeNode nodeForName(final String kidName)
        {
            return kidsMap.get(kidName);
        }
    }
}
