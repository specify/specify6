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
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.config.init.RegisterSpecify;
import edu.ku.brc.specify.config.init.RegisterSpecify.ConnectionException;
import edu.ku.brc.ui.UIRegistry;

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
    protected FileReader     fr = null;
    protected BufferedReader br = null;
    
    protected Hashtable<String, Hashtable<String, RegProcEntry>> typeHash        = new Hashtable<String, Hashtable<String, RegProcEntry>>();
    protected Hashtable<String, RegProcEntry>                    regNumHash      = new Hashtable<String, RegProcEntry>();
    protected Hashtable<String, RegProcEntry>                    trackHash       = new Hashtable<String, RegProcEntry>();
    protected Hashtable<String, Boolean>                         trackUsageHash  = new Hashtable<String, Boolean>();
    protected Hashtable<String, Hashtable<String, Integer>>      trackCatsHash   = new Hashtable<String, Hashtable<String, Integer>>();
    
    protected Hashtable<String, RegProcEntry>                    collHash        = new Hashtable<String, RegProcEntry>();

    protected RegProcEntry root = new RegProcEntry("Root");
    
    /**
     * 
     */
    public RegProcessor()
    {
        super();
    }
    
    /**
     * @return the root
     */
    public RegProcEntry getRoot(final boolean inclAnonymous)
    {
        Vector<RegProcEntry> kids = new Vector<RegProcEntry>(root.getKids());
        
        for (RegProcEntry entry : kids)
        {
            if (entry.getName().equals("Anonymous"))
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
     * @param key
     * @param valueStr
     */
    protected void addToTracks(final String key, final String valueStr)
    {
        trackUsageHash.put(key, true);
        
        int inx = -1;
        if (key.equals("RunCount"))
        {
            inx = key.length();
        } else if (key.startsWith("TREE_OPEN_"))
        {
            inx = 9;
        } else
        {
            inx = key.indexOf('_');
        }
        
        if (inx > -1)
        {
            String category = key.substring(0, inx);
            Hashtable<String, Integer> countHash = trackCatsHash.get(category);
            if (countHash == null)
            {
                countHash = new Hashtable<String, Integer>();
                trackCatsHash.put(category, countHash);
            }
            
            Integer count = countHash.get(key);
            if (count == null)
            {
                count = 0;
            } else
            {
                count += Integer.parseInt(valueStr);
            }
            countHash.put(key, count);
        }
    }
    
    public File getDataFromWeb(final String urlKey)
    {
        try
        {
            HttpClient httpClient = new HttpClient();
            httpClient.getParams().setParameter("http.useragent", RegisterSpecify.class.getName()); //$NON-NLS-1$
            
            String urlStr = UIRegistry.getResourceString(urlKey);
            
            PostMethod postMethod = new PostMethod(urlStr + "?dmp=1&");
            
            // get the POST parameters
            /*NameValuePair[] postParams = new NameValuePair[2];
            postParams[0] = new NameValuePair("reg_number", "XXX");
            postParams[1] = new NameValuePair("test", "ZZZZ");
            postMethod.setRequestBody(postParams);*/
            
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
    protected boolean processTrackEntry() throws IOException
    {
        String     line  = br.readLine();
        Properties props = new Properties();

        do 
        {
            String[] tokens = StringUtils.split(line, "=");
            if (tokens.length == 2)
            {
                String pName = tokens[0].trim();
                props.put(pName, tokens[1].trim());
                
            } else if (tokens.length > 2)
            {
                System.err.println("Length: "+tokens.length+"  ["+line+"]");
            }
            
            line = br.readLine();
            if (line != null && line.startsWith("----------"))
            {
                String id = props.getProperty("id");
                if (id != null)
                {
                    RegProcEntry entry = trackHash.get(id);
                    if (entry == null)
                    {
                        entry = new  RegProcEntry(props);
                        entry.setId(id);
                        trackHash.put(id, entry);
                    }
                    entry.getProps().clear();
                    entry.getProps().putAll(props);
                }
                
                String collNumber = props.getProperty("Collection_number");
                if (StringUtils.isNotEmpty(collNumber))
                {
                    /*RegProcEntry colEntry = collHash.get(collNumber);
                    if (colEntry == null)
                    {
                        colEntry = new RegProcEntry(collNumber);
                        collHash.put(collNumber, colEntry);
                    }
                    colEntry.getProps().putAll(props);*/
                    
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
    
    public String[] getTrackDescPairs()
    {
        return new String[] {
                "DE", "Data Entry", 
                "WB", "WorkBench", 
                "SS", "System Configuration", 
                "RS", "RecordSets", 
                "QB", "Query Builder", 
                "TREE_OPEN", "Tree Open", 
                "RunCount", "Run Count", 
                "Tools", "PLugins", 
                            };
        
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
    /*public Hashtable<String, RegProcEntry> getCollectionHash()
    {
        return collHash;
    }*/

    /**
     * @return the trackCatsHash
     */
    public Hashtable<String, Hashtable<String, Integer>> getTrackCatsHash()
    {
        return trackCatsHash;
    }

    /**
     * @param inFile
     * @throws IOException
     */
    protected void processTracks(final File inFile) throws IOException
    {
        fr = new FileReader(inFile);
        br = new BufferedReader(fr);

        // first line is header
        br.readLine();
        boolean rv = true;
        while (rv)
        {
            rv = processTrackEntry();
        }
        
        for (RegProcEntry entry : trackHash.values())
        {
            //System.out.println(entry);
            for (Object keyObj : entry.getProps().keySet())
            {
                String pName = keyObj.toString();
                String value = (String)entry.getProps().getProperty(pName);
                
                if (pName.startsWith("Usage_"))
                {
                    System.err.println("Usage: ["+pName+"] ["+pName.substring(6)+"]");
                    addToTracks(pName.substring(6), value); 
                    
                } else if (pName.startsWith("DE_") || 
                            pName.startsWith("WB_") || 
                            pName.startsWith("SS_") || 
                            pName.startsWith("RS_") || 
                            pName.startsWith("QB_") || 
                            pName.startsWith("TREE_OPEN_") || 
                            pName.startsWith("RunCount") || 
                            pName.startsWith("Tools_"))
                {
                    addToTracks(pName, value); 
                    System.err.println("Adding: ["+pName+"] ");
                } else
                {
                    System.err.println("Couldn't find: ["+pName+"]");
                }
            }
        }
    }
    
    /**
     * 
     */
    public void mergeStats()
    {
        for (RegProcEntry entry : regNumHash.values())
        {
            String colType = entry.get("reg_type");
            if (StringUtils.isNotEmpty(colType) && colType.equals("Collection"))
            {
                String colNum = entry.get("reg_number");
                if (StringUtils.isNotEmpty(colNum))
                {
                    System.out.println(colNum);
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
        
        Hashtable<String, RegProcEntry> hh = typeHash.get(regList[0]);
        
        for (RegProcEntry entry : hh.values())
        {
            System.out.println(entry.getId()+" "+entry.getProps().getProperty("Institution_number"));
        }
        
        for (String key : regList)
        {
            System.out.println("\n"+key);
            Hashtable<String, RegProcEntry> h = typeHash.get(key);
            
            for (RegProcEntry entry : h.values())
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
        
        System.out.println("--------");
        printEntries(root, 0);
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
