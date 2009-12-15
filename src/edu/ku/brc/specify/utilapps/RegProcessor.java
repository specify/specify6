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
package edu.ku.brc.specify.utilapps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.config.init.RegisterSpecify;
import edu.ku.brc.specify.config.init.RegisterSpecify.ConnectionException;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 19, 2008
 *
 */
public class RegProcessor
{
    protected String           versionNum     = null;
    protected String           prevVersionNum = null;
    protected SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd");
    
    protected FileReader     fr = null;
    protected BufferedReader br = null;
    
    protected Hashtable<String, Hashtable<String, RegProcEntry>> typeHash        = new Hashtable<String, Hashtable<String, RegProcEntry>>();
    protected Hashtable<String, RegProcEntry>                    regNumHash      = new Hashtable<String, RegProcEntry>();
    
    protected Hashtable<String, RegProcEntry>                    trackRegNumHash = new Hashtable<String, RegProcEntry>();
    protected Hashtable<String, RegProcEntry>                    trackIdHash     = new Hashtable<String, RegProcEntry>();
    protected Hashtable<String, Boolean>                         trackUsageHash  = new Hashtable<String, Boolean>();
    protected Hashtable<String, Hashtable<String, Integer>>      trackCatsHash   = new Hashtable<String, Hashtable<String, Integer>>();
    
    protected Hashtable<String, RegProcEntry>                    collHash        = new Hashtable<String, RegProcEntry>();
    protected Hashtable<String, String>                          verToDateHash   = null;
    
    protected Vector<Long>                                       dateList        = new Vector<Long>();
    protected Hashtable<Long, String>                            dateToVer       = new Hashtable<Long, String>();
    
    protected Hashtable<String, RegProcEntry>                    prvRegNumHash     = new Hashtable<String, RegProcEntry>();
    protected Hashtable<String, Boolean>                         prvTrackUsageHash = new Hashtable<String, Boolean>();
    protected Hashtable<String, Hashtable<String, Integer>>      prvTrackCatsHash  = new Hashtable<String, Hashtable<String, Integer>>();
    
    protected Vector<Pair<String, String>>                       dateTimeVector    = new Vector<Pair<String, String>>();
    
    protected Hashtable<String, Boolean>                         regNumOKHash      = new Hashtable<String, Boolean>();
    protected Hashtable<String, String>                          idToIPHash        = null;
    protected int                                                lineNo            = 0;

    protected String[] TYPES = {"Institution", "Division", "Discipline", "Collection"};
    
    protected RegProcEntry root = new RegProcEntry("Root");
    
    /**
     * 
     */
    public RegProcessor()
    {
        super();

    }
    
    /**
     * 
     */
    public void processFile()
    {
        try
        {
            //idToIPHash = getIdToIPHash();
            
            process(new File("/Users/rods/reg.dat"), true);
            process(new File("/Users/rods/track.dat"), false);
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @return the dateFmt
     */
    public SimpleDateFormat getDateFmt()
    {
        return dateFmt;
    }

    /**
     * @return the root
     */
    public RegProcEntry getRoot(final boolean inclAnonymous)
    {
        Vector<RegProcEntry> kids = new Vector<RegProcEntry>(root.getKids());
        
        if (!inclAnonymous)
        {
            for (RegProcEntry entry : kids)
            {
                if (entry.getName().equals("Anonymous"))
                {
                    root.getKids().remove(entry);
                }
            }
        }
        
        //kids = new Vector<RegProcEntry>(root.getKids());
        Collections.sort(root.getKids(), new Comparator<RegProcEntry>() {
            @Override
            public int compare(RegProcEntry o1, RegProcEntry o2)
            {
                String date1 = o1.get("date");
                String date2 = o2.get("date");
                return date1 != null && date2 != null ? date1.compareTo(date2) : 0;
            }
        });
        return root;
    }
    
    /**
     * @param versionNum
     */
    public void setVersionDates(final String versionStr, 
                                final String preVersionStr,
                                final Hashtable<String, String> verToDateHashArg)
    {
        typeHash.clear();
        regNumHash.clear();
        
        trackRegNumHash.clear();
        trackIdHash.clear();
        trackUsageHash.clear();
        trackCatsHash.clear();
        
        collHash.clear();
        dateList.clear();
        dateToVer.clear();
        
        prvRegNumHash.clear();
        prvTrackUsageHash.clear();
        prvTrackCatsHash.clear();
        
        if (verToDateHashArg != null)
        {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            verToDateHash   = verToDateHashArg;
            for (String ver : verToDateHash.keySet())
            {
                String dateStr = verToDateHash.get(ver);
                try
                {
                    long time = df.parse(dateStr).getTime();
                    dateToVer.put(time, ver);
                    dateList.add(time);
                    
                } catch (ParseException ex)
                {
                    ex.printStackTrace();
                }
            }
            Collections.sort(dateList);
            
        }
        versionNum     = versionStr;
        prevVersionNum = preVersionStr;
    }
    
    /**
     * @param key
     * @param valueStr
     */
    protected void addToTracks(final String key, 
                               final String valueStr,
                               final Hashtable<String, Boolean>                    trackUsageHashArg,
                               final Hashtable<String, Hashtable<String, Integer>> trackCatsHashArg)
    {
        trackUsageHashArg.put(key, true);
        
        String category = getCategoryKey(key);
        if (category != null)
        {
            Hashtable<String, Integer> countHash = trackCatsHashArg.get(category);
            if (countHash == null)
            {
                countHash = new Hashtable<String, Integer>();
                trackCatsHashArg.put(category, countHash);
            }
            
            Integer count  = countHash.get(key);
            int     valNum = Integer.parseInt(valueStr);
            if (count == null)
            {
                count = valNum;
            } else
            {
                count += valNum;
            }
            countHash.put(key, count);
        }
    }
    
    private String getCategoryKey(final String key)
    {
        int inx = -1;
        if (key.equals("RunCount"))
        {
            inx = key.length();
        } else
        {
            inx = key.indexOf('_');
        }
        
        if (inx > -1)
        {
            return key.substring(0, inx);
        }
        return null;
    }
    
    /**
     * @param key
     * @param valueStr
     */
    protected void subtractPrevious()
    {
        if (prvRegNumHash.size() > 0)
        {
            Hashtable<String, Boolean>                    trkUsageHash  = new Hashtable<String, Boolean>();
            Hashtable<String, Hashtable<String, Integer>> trkCatsHash   = new Hashtable<String, Hashtable<String, Integer>>();
            
            // First find all the Collection Register Numbers
            // that were the same from 
            Vector<String> collectionNumbers = new Vector<String>(trackRegNumHash.keySet());
            System.out.println("========================");
            for (String colKey : collectionNumbers)
            {
                RegProcEntry entry = trackRegNumHash.get(colKey);
                System.out.println("CUR "+entry.get("date")+"  "+colKey);
                for (Object keyObj : entry.keySet())
                {
                    String pName = keyObj.toString();
                    String value = entry.get(pName);
                    
                    if (pName.startsWith("Usage_"))
                    {
                        addToTracks(pName.substring(6), value, trkUsageHash, trkCatsHash);
                        
                    } else if (pName.startsWith("DE_") || 
                            pName.startsWith("WB_") || 
                            pName.startsWith("SS_") || 
                            pName.startsWith("RS_") || 
                            pName.startsWith("QB_") || 
                            pName.startsWith("TREE_OPEN_") || 
                            pName.startsWith("RunCount") || 
                            pName.startsWith("Tools_"))
                    {
                        addToTracks(pName, value, trkUsageHash, trkCatsHash);
                    }
                }
            }
            System.out.println("========================!");
            prvTrackUsageHash.clear();
            prvTrackCatsHash.clear();
            
            for (String colKey : collectionNumbers)
            {
                RegProcEntry entry = prvRegNumHash.get(colKey);
                if (entry != null)
                {
                    System.out.println("PRV "+entry.get("date")+"  "+colKey);
                    for (Object keyObj : entry.keySet())
                    {
                        String pName = keyObj.toString();
                        String value = entry.get(pName);
                        
                        if (pName.startsWith("Usage_"))
                        {
                            addToTracks(pName.substring(6), value, prvTrackUsageHash, prvTrackCatsHash);
                            
                        } else if (pName.startsWith("DE_") || 
                                pName.startsWith("WB_") || 
                                pName.startsWith("SS_") || 
                                pName.startsWith("RS_") || 
                                pName.startsWith("QB_") || 
                                pName.startsWith("TREE_OPEN_") || 
                                pName.startsWith("RunCount") || 
                                pName.startsWith("Tools_"))
                        {
                            addToTracks(pName, value, prvTrackUsageHash, prvTrackCatsHash);
                        }
                    }
                }
            }
            
            for (String category : trkCatsHash.keySet())
            {
                Hashtable<String, Integer> prvCountHash = prvTrackCatsHash.get(category);
                Hashtable<String, Integer> countHash    = trkCatsHash.get(category);
                if (prvCountHash != null && countHash != null)
                {
                    for (String cntKey : countHash.keySet())
                    {
                        Integer cnt    = countHash.get(cntKey);
                        Integer prvCnt = prvCountHash.get(cntKey);
                        if (cnt != null && prvCnt != null)
                        {
                            if (prvCnt.intValue() > cnt.intValue())
                            {
                                System.out.println("   "+String.format("%5d %5d %s %s ", cnt, prvCnt, category ,cntKey));
                            } else
                            {
                                cnt -= prvCnt;
                                countHash.put(cntKey, cnt);
                                System.out.println("OK "+String.format("%5d %5d %s %s ", cnt, prvCnt, category ,cntKey));
                            }
                        }
                    }
                }
            }
            trackUsageHash = trkUsageHash;
            trackCatsHash  = trkCatsHash;
        }
    }
    

    /**
     * @param urlKey
     * @param inclDmp
     * @return
     */
    public File getDataFromWeb(final String urlKey, final boolean inclDmp)
    {
        try
        {
            HttpClient httpClient = new HttpClient();
            httpClient.getParams().setParameter("http.useragent", RegisterSpecify.class.getName()); //$NON-NLS-1$
            
            String urlStr = UIRegistry.getResourceString(urlKey);
            
            PostMethod postMethod = new PostMethod(urlStr + (inclDmp ? "?dmp=1&" : ""));
            
            // connect to the server
            try
            {
                httpClient.executeMethod(postMethod);
                
                InputStream iStream = postMethod.getResponseBodyAsStream();
                
                File   tempFile = File.createTempFile("web", "data");
                byte[] bytes    = new byte[8196];
                
                PrintWriter pw = new PrintWriter(tempFile);
                int numBytes = 0;
                do 
                {
                    numBytes = iStream.read(bytes);
                    if (numBytes > 0)
                    {
                        pw.write(new String(bytes, 0, numBytes));
                    }
                    
                } while (numBytes > 0);
                
                pw.close();
                
                return tempFile;
            }
            catch (Exception e)
            {
                //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RegProcessor.class, e);
                e.printStackTrace();
                throw new ConnectionException(e);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public static Connection connection     = null;
    public static Statement  stmt           = null;
    public static String     tableName      = null;
    public static String     tableItemName  = null;
    
    /**
     * @return
     */
    public String[] getTrackKeys()
    {
        return new String[] {
                "DE", 
                "WB", 
                "SS", 
                "RS", 
                "QB", 
                "TR", 
                "TD",
                "ST", 
                "IR",  
                "DB", 
                "IN", 
                "RP", 
                "AP", 
                "RunCount", 
                "Tools", 
                 };
    }
    /**
     * @return
     */
    public Hashtable<String, String> getAllDescPairsHash()
    {
        Hashtable<String, String> hash = new Hashtable<String, String>();
        for (Pair<String, String> p : getAllKeyDescPairs())
        {
           hash.put(p.first, p.second); 
        }
        return hash;
    }
    
    /**
     * @return the dateTimeVector
     */
    public Vector<Pair<String, String>> getDateTimeVector()
    {
        for (Pair<String, String> p : dateTimeVector)
        {
            System.out.println(p.first+"  "+p.second);
        }
        return dateTimeVector;
    }

    /**
     * @return
     */
    public List<Pair<String, String>> getRegKeyDescPairs()
    {
        Vector<Pair<String, String>> list = new Vector<Pair<String, String>>();
        
        list.add(new Pair<String, String>("java_version", "Java Version")); 
        list.add(new Pair<String, String>("java_vendor",  "Java Vendor")); 
        list.add(new Pair<String, String>("os_version",   "OS Version")); 
        list.add(new Pair<String, String>("os_name",      "OS Name")); 
        list.add(new Pair<String, String>("date",         "Date")); 
        list.add(new Pair<String, String>("reg_type",     "Registration Type")); 
        list.add(new Pair<String, String>("platform",     "Platform")); 
        list.add(new Pair<String, String>("reg_isa",      "Registration ISA")); 
        list.add(new Pair<String, String>("ip",           "IP Number")); 
        list.add(new Pair<String, String>("ISA_Number",   "ISA Number")); 
        list.add(new Pair<String, String>("User_email",   "User EMail")); 
        list.add(new Pair<String, String>("app_version",  "App Version")); 
        list.add(new Pair<String, String>("by_date",      "By Date")); 
        list.add(new Pair<String, String>("by_month",     "By Month")); 
        list.add(new Pair<String, String>("by_year",      "By Year")); 
        list.add(new Pair<String, String>("time",         "Time")); 

        return list;
    }
    
    
    /**
     * @return
     */
    public List<Pair<String, String>> getTrackKeyDescPairs()
    {
        Vector<Pair<String, String>> list = new Vector<Pair<String, String>>();
        
        list.add(new Pair<String, String>("DE", "Data Entry")); 
        list.add(new Pair<String, String>("WB", "WorkBench")); 
        list.add(new Pair<String, String>("SS", "System Configuration")); 
        list.add(new Pair<String, String>("RS", "RecordSets")); 
        list.add(new Pair<String, String>("QB", "Query Builder")); 
        list.add(new Pair<String, String>("TR", "Tree")); 
        list.add(new Pair<String, String>("TD", "Tree Def")); 
        list.add(new Pair<String, String>("ST", "Statistics")); 
        list.add(new Pair<String, String>("IR", "Information Request")); 
        list.add(new Pair<String, String>("DB", "Database Save/Update/Remove")); 
        list.add(new Pair<String, String>("IN", "Interactions")); 
        list.add(new Pair<String, String>("RP", "Reports")); 
        list.add(new Pair<String, String>("AP", "Application")); 
        list.add(new Pair<String, String>("RunCount", "Run Count")); 
        list.add(new Pair<String, String>("Tools", "Plugins"));
        
        return list;
    }
    
    
    /**
     * @return
     */
    public List<Pair<String, String>> getAllKeyDescPairs()
    {
        Vector<Pair<String, String>> list = new Vector<Pair<String, String>>();
        
        list.add(new Pair<String, String>("num_co",     "Collections"));
        list.add(new Pair<String, String>("num_tx",     "Taxon Records"));
        list.add(new Pair<String, String>("num_txu",    "Taxon Records Used"));
        list.add(new Pair<String, String>("num_geo",    "Geography Records"));
        list.add(new Pair<String, String>("num_geou",   "Geography Records Used"));
        list.add(new Pair<String, String>("num_loc",    "Locality Records"));
        list.add(new Pair<String, String>("num_locgr",  "Locality Records Used"));
        list.add(new Pair<String, String>("num_preps",  "Preparations Records"));
        list.add(new Pair<String, String>("num_prpcnt", "Count of Preparations"));
        list.add(new Pair<String, String>("num_litho",  "Lithostratigraphy Records"));
        list.add(new Pair<String, String>("num_lithou", "Lithostratigraphy Records Used"));
        list.add(new Pair<String, String>("num_gtp",    "Chronostratigraphy Records"));
        list.add(new Pair<String, String>("num_gtpu",   "Chronostratigraphy Records Used"));
        
        list.add(new Pair<String, String>("Phone",      "Phone"));
        list.add(new Pair<String, String>("Address",    "Address"));
        
        list.add(new Pair<String, String>("Institution_number",   "Institution Number"));
        list.add(new Pair<String, String>("Institution_name",     "Institution"));
        list.add(new Pair<String, String>("Division_number",      "Division Number"));
        list.add(new Pair<String, String>("Division_name",        "Division"));
        list.add(new Pair<String, String>("Discipline_number",    "Discipline Number"));
        list.add(new Pair<String, String>("Discipline_type",      "Discipline"));
        list.add(new Pair<String, String>("Collection_name",      "Collection Name"));
        list.add(new Pair<String, String>("Collection_number",    "Collection Number"));
        list.add(new Pair<String, String>("Collection_estsize",   "Collection Estimated Size"));
        list.add(new Pair<String, String>("reg_number",           "Registration Number"));
        list.add(new Pair<String, String>("id",                   "Id"));
        list.add(new Pair<String, String>("last_used_date",       "Last Opened Date"));
        list.add(new Pair<String, String>("hostname",             "Host Name"));
        
        
        list.addAll(getRegKeyDescPairs());
        list.addAll(getTrackKeyDescPairs());
        
        return list;
    }
    
    /**
     * @return
     */
    public Hashtable<String, RegProcEntry> getCollectionsHash()
    {
        return typeHash.get("Collection");
    }
    
    /**
     * @return the regNumHash
     */
    public Hashtable<String, RegProcEntry> getRegNumHash()
    {
        return regNumHash;
    }

    /**
     * @return the collHash
     */
    public Hashtable<String, RegProcEntry> getCollectionHash()
    {
        return collHash;
    }

    /**
     * @return the trackIdHash
     */
    public Hashtable<String, RegProcEntry> getTrackIdHash()
    {
        return trackIdHash;
    }

    /**
     * @return the trackCatsHash
     */
    public Hashtable<String, Hashtable<String, Integer>> getTrackCatsHash()
    {
        return trackCatsHash;
    }
    
    /**
     * @param dateStr
     * @return
     */
    public long getDate(final String dateStrArg)
    {
        String dateStr = dateStrArg;
        try
        {
            if (dateStr.indexOf(' ') == 8)
            {
                dateStr = dateStr.substring(0, 8);
            }
            return dateFmt.parse((dateStr.length() == 8 ? "20" : "") + dateStr).getTime();
            
        } catch (ParseException ex)
        {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 
     */
    public void mergeStats()
    {
        subtractPrevious();
        
        for (RegProcEntry entry : regNumHash.values())
        {
            String colType = entry.get("reg_type");
            if (StringUtils.isNotEmpty(colType) && colType.equals("Collection"))
            {
                String colNum = entry.get("reg_number");
                if (StringUtils.isNotEmpty(colNum))
                {
                    //System.out.println(colNum);
                    RegProcEntry colEntry = collHash.get(colNum);
                    if (colEntry != null)
                    {
                        for (Object keyObj : colEntry.keySet())
                        {
                            String key = keyObj.toString();
                            if (key.startsWith("num_"))
                            {
                                entry.put(key, colEntry.get(key));
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * @return sorted list of usage names.
     */
    public Vector<String> getTrackUsageNames()
    {
        Vector<String> names = new Vector<String>(trackUsageHash.keySet());
        Collections.sort(names);
        return names;
    }
    
    /**
     * @param entry
     * @param className
     * @param propName
     * @return
     */
    protected RegProcEntry getParentFromHash(final RegProcEntry entry, final String className, final String propName)
    {
        return typeHash.get(className).get(entry.get(propName));
    }
    
    /**
     * @param inFile
     */
    public void processSQL()
    {
        String sql = "SELECT r.RegisterID, r.RegNumber, ri.Name, ri.Value, ri.CountAmt, r.TimestampCreated, r.IP" +
                     " FROM register r INNER JOIN registeritem ri ON r.RegisterID = ri.RegisterID WHERE r.TimestampCreated > '2009-04-12' ORDER BY r.RegNumber";
        System.err.println(sql);
        //Connection connection = DBConnection.getInstance().getConnection();
        try
        {
            stmt = DBConnection.getInstance().getConnection().createStatement();
            ResultSet         rs     = stmt.executeQuery(sql);
            int               prevId = Integer.MAX_VALUE;
            RegProcEntry      currEntry = null;
            while (rs.next())
            {
                int id = rs.getInt(1);
                if (id != prevId)
                {
                    if (currEntry != null)
                    {
                        String regType = currEntry.get("reg_type");
                        if (regType != null)
                        {
                            Hashtable<String, RegProcEntry> entryHash = typeHash.get(regType);
                            if (entryHash == null)
                            {
                                entryHash = new  Hashtable<String, RegProcEntry>();
                                typeHash.put(regType, entryHash);
                            }
                            currEntry.put("reg_number", currEntry.getId());
                            currEntry.setType(currEntry.get("reg_type"));

                            
                            if (entryHash.get(currEntry.getId()) == null)
                            {
                                entryHash.put(currEntry.getId(), currEntry);    
                            } else
                            {
                                System.err.println("Already there: "+currEntry.getId());
                            }
                        } else
                        {
                            System.err.println("1Skipping: "+rs.getString(2));
                        }
                    }
                        
                    String regNumber = rs.getString(2);
                    String ip        = rs.getString(7);
                    currEntry = regNumHash.get(regNumber);
                    if (currEntry == null)
                    {
                        if (ip != null)
                        {
                            currEntry = new RegProcEntry();
                            regNumHash.put(regNumber, currEntry);
                            currEntry.setId(regNumber);
                            currEntry.setTimestampCreated(rs.getTimestamp(6));
                            currEntry.put("ip", ip);
                            
                        } else
                        {
                            System.err.println("IP is null for "+regNumber);
                            ip = "N/A";
                        }
                        
                    } else
                    {
                        System.err.println("Already "+regNumber);
                    }
                        
                    prevId = id;
                    
                } else if (prevId == Integer.MAX_VALUE)
                {
                    prevId = id;
                }
            
                String value = rs.getString(4);
                if (value == null)
                {
                    value = rs.getString(5);
                }
                
                String propName = rs.getString(3);
                if (currEntry != null && value != null && propName != null)
                {
                    currEntry.put(propName, value);
                }
            }
            rs.close();
            
            Hashtable<String, RegProcEntry> checkHash = new Hashtable<String, RegProcEntry>();
            Hashtable<String, RegProcEntry> instHash  = typeHash.get("Institution");
            
            for (RegProcEntry entry : new Vector<RegProcEntry>(regNumHash.values()))
            {
                entry.setName(null);
                String ip = entry.get("ip");
                if (ip == null || ip.startsWith("129.") || ip.startsWith("24."))
                {
                    System.out.println("Removing ip: "+ip);
                    instHash.remove(entry.getId());
                    regNumHash.remove(entry.getId());
                    
                } else
                {
                    RegProcEntry e = checkHash.get(ip);
                    if (e == null)
                    {
                        checkHash.put(ip, entry);
                    } else
                    {
                        instHash.remove(e.getId());
                        regNumHash.remove(e.getId());
                        checkHash.put(ip, entry);
                        System.out.println("Compressing ip: "+ip);
                    }
                }
            }
            
            buildTree();

        } catch (SQLException ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                } catch (Exception ex) {}
            }
        }
    }
    
    /**
     * 
     */
    protected void buildTree()
    {
        String[] regList = {"Institution", "Division", "Discipline", "Collection"};
        
        /*Hashtable<String, RegProcEntry> hh = typeHash.get(regList[0]);
        for (RegProcEntry entry : hh.values())
        {
            System.out.println(entry.getId()+" "+entry.get("Institution_number"));
        }*/
        
        for (String key : regList)
        {
            System.out.println("\n"+key);
            Hashtable<String, RegProcEntry> h = typeHash.get(key);
            if (h == null || h.values() == null)
            {
                System.out.println("Skipping: "+key);
                continue;
            }
            System.out.println("Adding: "+key);
            Vector<RegProcEntry> items = new Vector<RegProcEntry>(h.values());
            Collections.sort(items);
            
            for (RegProcEntry entry : items)
            {
                String reg_type   = entry.get("reg_type");
                
                if (reg_type.equals("Collection"))
                {
                    String       dspNum = entry.get("Discipline_number");
                    Hashtable<String, RegProcEntry> hash = typeHash.get("Discipline");
                    if (hash != null)
                    {
                        RegProcEntry parent = typeHash.get("Discipline").get(dspNum);
                        if (entry != null && parent != null)
                        {
                            parent.getKids().add(entry);
                            entry.setParent(parent);
                            System.out.println(parent.getName()+" "+entry.getName());
                        } else
                        {
                            System.err.println("Couldn't find Discipline Num["+dspNum+"]");
                        }
                    }
                    
                } else if (reg_type.equals("Discipline"))
                {
                    String       divNum = entry.get("Division_number");
                    RegProcEntry parent = typeHash.get("Division").get(divNum);
                    if (entry != null && parent != null)
                    {
                        parent.getKids().add(entry);
                        entry.setParent(parent);
                    } else
                    {
                        System.err.println("Couldn't find Division Num["+divNum+"]");
                    }    
                    
                } else if (reg_type.equals("Division"))
                {
                    String       instNum = entry.get("Institution_number");
                    RegProcEntry parent  = typeHash.get("Institution").get(instNum);
                    if (entry != null && parent != null)
                    {
                        parent.getKids().add(entry);
                        entry.setParent(parent);
                    } else
                    {
                        System.err.println("Couldn't find Inst Num["+instNum+"]");
                    }
                    
                } else if (reg_type.equals("Institution"))
                {
                    root.getKids().add(entry);
                    entry.setParent(root);
                    
                } else
                {
                    System.err.println("Unknown");
                }
            }
        }

    }
    
    /**
     * @param parent
     * @param level
     */
    protected void printEntries(final RegProcEntry parent, final int level)
    {
        for (int i=0;i<level;i++) System.out.print("  ");
        
        System.out.println(parent.getName()+" "+parent.get("reg_number"));
        for (RegProcEntry kid : parent.getKids())
        {
            printEntries(kid, level+1);
        }
    }

    /**
     * @return
     */
    protected Hashtable<String, String> getIdToIPHash()
    {
        Hashtable<String, String> hash = new Hashtable<String, String>();
        String sql = "SELECT DISTINCT(registeritem.Value), register.IP FROM register INNER JOIN registeritem ON register.RegisterID = registeritem.RegisterID WHERE Name = 'id'";
        Vector<Object[]> rows = BasicSQLUtils.query(DBConnection.getInstance().getConnection(), sql);
        for (Object[] row : rows)
        {
            hash.put(row[0].toString(), row[1].toString());
        }
        return hash;
    }
    
    /**
     * @return
     * @throws IOException
     */
    protected boolean processEntry() throws IOException
    {
        String     line  = br.readLine();
        Properties props = new Properties();
        do 
        {
            String[] tokens = StringUtils.split(line, "=");
            if (tokens.length == 2)
            {
                props.put(tokens[0].trim(), tokens[1]);
                
            } else if (tokens.length > 2)
            {
                System.err.println("Length: "+tokens.length+"  ["+line+"]");
            }
            
            line = br.readLine();
            if (line == null || line.startsWith("----------"))
            {
                String ip         = props.getProperty("ip");
                String regNumber  = props.getProperty("reg_number");
                
                if (StringUtils.isNotEmpty(ip) && 
                    StringUtils.isNotEmpty(regNumber))
                {
                    
                    if (!ip.startsWith("129.") && !ip.startsWith("24."))
                    {
                        regNumOKHash.put(regNumber, Boolean.TRUE);
                        System.out.println("OK: "+regNumber+"  "+ip);
                    } else
                    {
                        System.out.println("SK: "+regNumber+"  "+ip);
                    }
                    
                } else
                {
                    System.err.println("Skipping: "+regNumber+"  "+ip);
                }
                
                return line != null;
            }
        } while (line != null);
        
        return false;
    }
    
    /**
     * @return
     * @throws IOException
     */
    protected boolean fixProcessEntry() throws IOException
    {
        String     line  = br.readLine();
        Properties props = new Properties();
        do 
        {
            String[] tokens = StringUtils.split(line, "=");
            if (tokens.length == 2)
            {
                props.put(tokens[0], tokens[1]);
                
            } else if (tokens.length > 2)
            {
                System.err.println("Length: "+tokens.length+"  ["+line+"]");
            }
            
            line = br.readLine();
            if (line == null || line.startsWith("----------"))
            {
                String ip         = props.getProperty("ip");
                String regNumber  = props.getProperty("reg_number");
                
                if (StringUtils.isNotEmpty(ip) && 
                    StringUtils.isNotEmpty(regNumber))
                {
                    
                   String sql = "UPDATE register SET IP='"+ip+"' WHERE RegNumber = '"+regNumber+"'";
                   BasicSQLUtils.update(DBConnection.getInstance().getConnection(), sql);
                    
                } else
                {
                    System.err.println("Skipping: "+regNumber+"  "+ip);
                }
                
                return line != null;
            }
        } while (line != null);
        
        return false;
    }
    
    /**
     * @return
     * @throws IOException
     */
    protected boolean fixTrackEntry() throws IOException
    {
        String line  = br.readLine();
        lineNo++;

        Properties props = new Properties();
        do 
        {
            String[] tokens = StringUtils.split(line, "=");
            
            if (tokens.length == 2)
            {
                props.put(tokens[0].trim(), tokens[1]);
                
            } else if (tokens.length > 2)
            {
                System.err.println("Length: "+tokens.length+"  ["+line+"]");
            }
            
            line = br.readLine();
            lineNo++;
            if (line == null || line.startsWith("----------"))
            {
                String id  = props.getProperty("id");
                String ip  = props.getProperty("ip");
                
                if (StringUtils.isNotEmpty(id))
                {
                    //String ip = idToIPHash.get(id);
                    if (ip != null)
                    {
                        String sql = "UPDATE track SET IP='"+ip+"' WHERE Id = '"+id+"'";
                        BasicSQLUtils.update(DBConnection.getInstance().getConnection(), sql);
                        
                    } else
                    {
                        System.err.println("No Id->IP: "+id);
                    }
                    
                } else
                {
                    System.err.println("Track Skipping: "+id+"  line: "+lineNo);
                }
                
                return line != null;
            }
        } while (line != null);
        
        return false;
    }

    /**
     * @param inFile
     * @throws IOException
     */
    @SuppressWarnings({ "unchecked", "cast" })
    public void process(final File inFile, final boolean doReg) throws IOException
    {
        lineNo = 0;
        fr = new FileReader(inFile);
        br = new BufferedReader(fr);
        
        // first line is header
        br.readLine();
        boolean rv = true;
        while (rv)
        {
            if (doReg)
            {
                rv = processEntry();
                //rv = fixProcessEntry();
                //rv = false;
            } else
            {
                //rv = fixTrackEntry();
                rv = false;
            }
        }

        br.close();
        fr.close();
    }


}
