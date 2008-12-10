/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.utilapps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.config.init.RegisterSpecify;
import edu.ku.brc.specify.config.init.RegisterSpecify.ConnectionException;
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


    protected RegProcEntry root = new RegProcEntry("Root");
    
    /**
     * 
     */
    public RegProcessor()
    {
        super();
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
        
        for (RegProcEntry entry : kids)
        {
            if (!inclAnonymous && entry.getName().equals("Anonymous"))
            {
                root.getKids().remove(entry);
            }
        }
        
        //kids = new Vector<RegProcEntry>(root.getKids());
        Collections.sort(root.getKids(), new Comparator<RegProcEntry>() {
            @Override
            public int compare(RegProcEntry o1, RegProcEntry o2)
            {
                String date1 = o1.getProps().getProperty("date");
                String date2 = o2.getProps().getProperty("date");
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
                for (Object keyObj : entry.getProps().keySet())
                {
                    String pName = keyObj.toString();
                    String value = entry.getProps().getProperty(pName);
                    
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
                    for (Object keyObj : entry.getProps().keySet())
                    {
                        String pName = keyObj.toString();
                        String value = entry.getProps().getProperty(pName);
                        
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
                e.printStackTrace();
                throw new ConnectionException(e);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * @return
     * @throws IOException
     */
    protected boolean processTrackEntry(final long verTime, final long prevVerTime) throws IOException
    {
        //SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String     line  = br.readLine();
        Properties props = new Properties();

        do 
        {
            String[] tokens = StringUtils.split(line, "=");
            if (tokens.length == 2)
            {
                String pName = tokens[0].trim();
                if (pName.indexOf("TREE_") > -1)
                {
                    pName = StringUtils.replace(pName, "TREE_", "TR_");
                    pName = StringUtils.remove(pName, "Def");
                    
                } else if (pName.indexOf("TREEDEF") > -1)
                {
                    pName = StringUtils.replace(pName, "TREEDEF_", "TD_");
                    
                } else if (pName.indexOf("TR_") > -1 && pName.endsWith("Def"))
                {
                    pName = StringUtils.remove(pName, "Def");
                }
                props.put(pName, tokens[1].trim());
                
            } else if (tokens.length > 2)
            {
                System.err.println("Length: "+tokens.length+"  ["+line+"]");
            }
            
            line = br.readLine();
            if (line != null && line.startsWith("----------"))
            {
                String collNumber = props.getProperty("Collection_number");
                String id         = props.getProperty("id");
                if (id != null)
                {
                    String   dateStr;
                    String   date     = props.getProperty("date");
                    if (date != null)
                    {
                        String[] dateTime = StringUtils.split(date, " ");
                        dateStr  = dateTime[0] != null ? dateTime[0] : "";
                        
                        String timeStr = dateTime.length == 2 && dateTime[1] != null ? dateTime[1] : "";
                        props.put("date", dateStr);
                        props.put("time", timeStr);
                        
                        if (StringUtils.isNotEmpty(dateStr) || StringUtils.isNotEmpty(dateStr))
                        {
                            dateTimeVector.add(new Pair<String, String>(dateStr, timeStr));
                        }
                        
                    } else
                    {
                        dateStr = "";
                    }
                    
                    boolean isNew = false;
                    RegProcEntry entry = trackIdHash.get(id);
                    if (entry == null)
                    {
                        entry = new  RegProcEntry(props);
                        entry.setId(id);
                        isNew = true;
                    }
                        
                    // Version Stuff
                    String version = null;
                    if (verTime != 0 && prevVerTime != 0)
                    {
                        if (dateStr.equals("08/12/03") || 
                            dateStr.equals("08/12/02"))
                        {
                            version = "Alpha 6.1.63";
                            
                        } else if (dateStr.equals("08/12/04") || 
                                   dateStr.equals("08/12/05") || 
                                   dateStr.equals("08/12/06") || 
                                   dateStr.equals("08/12/07") || 
                                   dateStr.equals("08/12/08"))
                        {
                            version = "Alpha 6.1.64";
                        } else
                        {
                            version = props.getProperty("app_version");
                        }
                        
                        /*
                        long     time     = getDate(dateTime[0]);
                        int      inx      = -1;
                        for (int i=0;i<dateList.size();i++)
                        {
                            //System.out.println("["+df.format(new Date(time))+"]["+df.format(new Date(dateList.get(i)))+"] "+ (time > dateList.get(i)));
                            //System.out.println(String.format("[%5d][%5d] ", time, dateList.get(i))+ (time > dateList.get(i)));
                            if (time < dateList.get(i))
                            {
                                inx = i-1;
                                break;
                                
                            } else if (time == dateList.get(i))
                            {
                                inx = i;
                                break;
                            }
                        }
                        
                        if (inx > -1)
                        {
                            time    = dateList.get(inx);
                            version = dateToVer.get(time);
                            //System.out.println(time+"  "+version+" "+collNumber);
                        } else
                        {
                            continue;
                        }*/
                    }
                    
                    if (version != null && StringUtils.isNotEmpty(collNumber))
                    {
                        if (!version.equals(versionNum))
                        {
                            if (version.equals(prevVersionNum))
                            {
                                System.out.println("***  "+version+" "+collNumber+"  "+entry.get("date"));
                                Properties p = new Properties();
                                p.putAll(props);
                                RegProcEntry e = new RegProcEntry((Properties)props.clone());
                                e.setId(id);
                                prvRegNumHash.put(collNumber, e);
                                
                            } else
                            {
                                continue;
                            }
                        } else
                        {
                            System.out.println("###  "+version+" "+collNumber+"  "+entry.get("date"));
                            Properties p = new Properties();
                            p.putAll(props);
                            RegProcEntry e = new RegProcEntry((Properties)props.clone());
                            e.setId(id);
                            trackRegNumHash.put(collNumber, e);
                        }
                    }
                    // Version Stuff
                    
                    if (isNew)
                    {
                        trackIdHash.put(id, entry);
                    } else
                    {
                        entry.getProps().clear();
                        entry.getProps().putAll(props);
                    }
                }
                
                if (StringUtils.isNotEmpty(collNumber))
                {
                    RegProcEntry colEntry = regNumHash.get(collNumber);
                    if (colEntry != null)
                    {
                        for (Object keyObj : props.keySet())
                        {
                            String key = keyObj.toString();
                            if (key.startsWith("num_"))
                            {
                                colEntry.getProps().put(key, props.get(key));
                            }
                        }
                    }
                }
                
                return true;
            }
        } while (line != null);
        
        return false;
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
                String key = tokens[0];
                if (key.equals("SA_Number"))
                {
                    key = "ISA_Number";
                }
                props.put(key, tokens[1]);
                
            } else if (tokens.length > 2)
            {
                System.err.println("Length: "+tokens.length+"  ["+line+"]");
            }
            
            line = br.readLine();
            if (line == null || line.startsWith("----------"))
            {
                String regType    = props.getProperty("reg_type");
                String regNumber  = props.getProperty("reg_number");
                
                if (StringUtils.isNotEmpty(regType) && 
                    StringUtils.isNotEmpty(regNumber))
                {
                    String   date     = props.getProperty("date");
                    if (date != null)
                    {
                        String[] dateTime = StringUtils.split(date, " ");
                        props.put("date", dateTime.length > 0 && dateTime[0] != null ? dateTime[0] : "");
                        props.put("time", dateTime.length == 2 && dateTime[1] != null ? dateTime[1] : "");
                    }
                    
                    RegProcEntry currEntry = regNumHash.get(regNumber);
                    if (currEntry == null)
                    {
                        currEntry = new RegProcEntry(props);
                        regNumHash.put(regNumber, currEntry);    
                    } else
                    {
                        currEntry.getProps().putAll(props);
                    }
                    
                    Hashtable<String, RegProcEntry> entryHash = typeHash.get(regType);
                    if (entryHash == null)
                    {
                        entryHash = new  Hashtable<String, RegProcEntry>();
                        typeHash.put(regType, entryHash);
                    }
                    entryHash.put(regNumber, currEntry);
                    
                } else
                {
                    System.err.println("Skipping: "+regNumber);
                }
                
                return line != null;
            }
        } while (line != null);
        
        return false;
    }
    
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
     * @param inFile
     * @throws IOException
     */
    protected void processTracks(final File inFile) throws IOException
    {
        
        long versonTime      = 0;
        long prevVersionTime = 0;
        if (versionNum != null && prevVersionNum != null)
        {
            versonTime      = getDate(verToDateHash.get(versionNum));
            prevVersionTime = getDate(verToDateHash.get(prevVersionNum));
        }
        
        fr = new FileReader(inFile);
        br = new BufferedReader(fr);

        // first line is header
        br.readLine();
        boolean rv = true;
        while (rv)
        {
            rv = processTrackEntry(versonTime, prevVersionTime);
        }
        
        for (RegProcEntry entry : trackIdHash.values())
        {
            //System.out.println(entry);
            for (Object keyObj : entry.getProps().keySet())
            {
                String pName = keyObj.toString();
                String value = entry.getProps().getProperty(pName);
                
                if (pName.startsWith("Usage_"))
                {
                    String name = pName.substring(6);
                    //System.err.println("Usage: ["+pName+"] ["+pName.substring(6)+"]");
                    addToTracks(name, value, trackUsageHash, trackCatsHash); 
                    
                } else if (pName.startsWith("DE_") || 
                            pName.startsWith("WB_") || 
                            pName.startsWith("SS_") || 
                            pName.startsWith("RS_") || 
                            pName.startsWith("QB_") || 
                            pName.startsWith("TREE_OPEN_") || 
                            pName.startsWith("TREEDEF_OPEN_") || 
                            pName.startsWith("RunCount") || 
                            pName.startsWith("Tools_"))
                {
                    addToTracks(pName, value, trackUsageHash, trackCatsHash); 
                    //System.err.println("Adding: ["+pName+"] ");
                } else
                {
                    //System.err.println("Couldn't find: ["+pName+"]");
                }
            }
            
            String collectionNum = entry.get("Collection_number");
            if (collectionNum != null)
            {
                collHash.put(collectionNum, entry);
            }
        }
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
                        for (Object keyObj : colEntry.getProps().keySet())
                        {
                            String key = keyObj.toString();
                            if (key.startsWith("num_"))
                            {
                                entry.getProps().put(key, colEntry.getProps().get(key));
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
    
    protected RegProcEntry getParentFromHash(final RegProcEntry entry, final String className, final String propName)
    {
        return typeHash.get(className).get(entry.getProps().getProperty(propName));
    }
    
    /**
     * @param inFile
     * @throws IOException
     */
    @SuppressWarnings({ "unchecked", "cast" })
    public void process(final File inFile) throws IOException
    {
        fr = new FileReader(inFile);
        br = new BufferedReader(fr);

        // first line is header
        br.readLine();
        boolean rv = true;
        while (rv)
        {
            rv = processEntry();
        }
        
        String[] regList = {"Institution", "Division", "Discipline", "Collection"};
        
        /*Hashtable<String, RegProcEntry> hh = typeHash.get(regList[0]);
        for (RegProcEntry entry : hh.values())
        {
            System.out.println(entry.getId()+" "+entry.getProps().getProperty("Institution_number"));
        }*/
        
        for (String key : regList)
        {
            //System.out.println("\n"+key);
            Hashtable<String, RegProcEntry> h = typeHash.get(key);
            
            Vector<RegProcEntry> items = new Vector<RegProcEntry>(h.values());
            Collections.sort(items);
            
            for (RegProcEntry entry : items)
            {
                String reg_type   = entry.getProps().getProperty("reg_type");
                
                if (reg_type.equals("Collection"))
                {
                    String       dspNum = entry.getProps().getProperty("Discipline_number");
                    RegProcEntry parent = typeHash.get("Discipline").get(dspNum);
                    if (entry != null && parent != null)
                    {
                        parent.getKids().add(entry);
                        entry.setParent(parent);
                    } else
                    {
                        System.err.println("Couldn't find Discipline Num["+dspNum+"]");
                    }
                    
                } else if (reg_type.equals("Discipline"))
                {
                    String       divNum = entry.getProps().getProperty("Division_number");
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
                    String       instNum = entry.getProps().getProperty("Institution_number");
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
                    //String nm = entry.getProps().getProperty("Institution_number");
                    root.getKids().add(entry);
                    entry.setParent(root);
                    
                } else
                {
                    System.err.println("Unknown");
                }
            }
        }

        br.close();
        fr.close();
        
        boolean doDemo = true;
        if (doDemo)
        {
            Hashtable<String, Boolean> siteNamesHash = new Hashtable<String, Boolean>();
            List<String> lines = (List<String>)FileUtils.readLines(new File("sites.csv"));
            for (int i=1;i<lines.size();i++)
            {
                String[] toks = lines.get(i).split("\t");
                if (toks.length > 4)
                {
                    String   nm   = toks[4];
                    if (StringUtils.isNotEmpty(nm) && !nm.startsWith("TOTAL"))
                    {
                        siteNamesHash.put(toks[4], true);
                    }
                }
            }
            Vector<String> names = new Vector<String>(siteNamesHash.keySet());
            int cnt = 0;
            for (RegProcEntry entry : typeHash.get("Institution").values())
            {
                entry.setName(names.get(cnt % names.size()));
                cnt++;
            }
        }
        
        root.sortKids();
        
        //System.out.println("--------");
        //printEntries(root, 0);
    }
    
    /**
     * @param parent
     * @param level
     */
    protected void printEntries(final RegProcEntry parent, final int level)
    {
        for (int i=0;i<level;i++) System.out.print("  ");
        
        System.out.println(parent.getName()+" "+parent.getProps().getProperty("reg_number"));
        for (RegProcEntry kid : parent.getKids())
        {
            printEntries(kid, level+1);
        }
    }
    
    //--------------------------------------------------------

    /**
     * @param args
     */
    /*public static void main(String[] args)
    {
        RegProcessor p = new RegProcessor();
        try
        {
            p.process(new File("reg.dat"));
            p.processTracks(new File("track.dat"));
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }*/
}
