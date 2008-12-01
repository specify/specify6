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
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

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
        
        int inx = key.indexOf('_');
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
                /*if (pName.startsWith("Usage."))
                {
                    trackUsageHash.put(pName.substring(6), true); 
                    
                } else if (pName.startsWith("DE_") || 
                            pName.startsWith("WB_") || 
                            pName.startsWith("SS_") || 
                            pName.startsWith("RS_"))
                {
                    trackUsageHash.put(pName, true); 
                }*/
                
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
                props.put(tokens[0], tokens[1]);
                
                if (tokens[0].equals("SA_Number") && StringUtils.isNotEmpty(tokens[1]))
                {
                    int x= 0;
                    x++;
                }
                
                if (tokens[1].equals("1228100630.39"))
                {
                    int x= 0;
                    x++;
                }
                
            } else if (tokens.length > 2)
            {
                System.err.println("Length: "+tokens.length+"  ["+line+"]");
            }
            
            line = br.readLine();
            if (line == null || line.startsWith("----------"))
            {
                String regType   = props.getProperty("reg_type");
                String regNumber = props.getProperty("reg_number");
                
                if (StringUtils.isNotEmpty(regType) && StringUtils.isNotEmpty(regNumber))
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
                
                if (pName.startsWith("Usage."))
                {
                    addToTracks(pName.substring(6), value); 
                    
                } else if (pName.startsWith("DE_") || 
                            pName.startsWith("WB_") || 
                            pName.startsWith("SS_") || 
                            pName.startsWith("RS_"))
                {
                    addToTracks(pName, value);  
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
                String date = entry.getProps().getProperty("date");
                if (date != null && date.startsWith("08/11/30 07:22:32"))
                {
                    int x= 0;
                    x++;
                }
                
                String isa = entry.getProps().getProperty("SA_Number");
                if (StringUtils.isNotEmpty(isa))
                {
                    int x= 0;
                    x++;
                }
                
                String rn = entry.getProps().getProperty("reg_number");
                if (StringUtils.isNotEmpty(rn) && rn.equals("1228100630.39"))
                {
                    int x= 0;
                    x++;
                }
                
                //String reg_number = entry.getProps().getProperty("reg_number");
                String reg_type   = entry.getProps().getProperty("reg_type");
                //System.out.println("=> "+entry.)
                
                if (reg_type.equals("Collection"))
                {
                    String dspNum = entry.getProps().getProperty("Discipline_number");
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
                    String divNum = entry.getProps().getProperty("Division_number");
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
                    String nm = entry.getProps().getProperty("Institution_number");
                    if (nm != null && nm.equals("1227547423.86"))
                    {
                       int x = 0;
                       x++;
                    }
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
    public static void main(String[] args)
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
    }
}
