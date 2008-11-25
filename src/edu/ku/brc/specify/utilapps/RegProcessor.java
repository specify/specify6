/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.utilapps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    
    protected Hashtable<String, Hashtable<String, Entry>> typeHash   = new Hashtable<String, Hashtable<String,Entry>>();
    protected Hashtable<String, Entry>                    regNumHash = new Hashtable<String, Entry>();
    protected Hashtable<String, Entry>                    trackHash  = new Hashtable<String, Entry>();
    
    protected Entry root = new Entry("Root");
    
    /**
     * 
     */
    public RegProcessor()
    {
        super();
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
                props.put(tokens[0].trim(), tokens[1].trim());
                
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
                    Entry  entry = trackHash.get(id);
                    if (entry == null)
                    {
                        entry = new  Entry(props);
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
        String line = br.readLine();
        /*while (!line.startsWith("----------"))
        {
            line = br.readLine();
        }*/

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
            if (line != null && line.startsWith("----------"))
            {
                String regType   = props.getProperty("reg_type");
                String regNumber = props.getProperty("reg_number");
                
                if (StringUtils.isNotEmpty(regType) && StringUtils.isNotEmpty(regNumber))
                {
                    Entry currEntry = regNumHash.get(regNumber);
                    if (currEntry == null)
                    {
                        currEntry = new Entry(props);
                        regNumHash.put(regNumber, currEntry);    
                    }
                    
                    Hashtable<String, Entry> entryHash = typeHash.get(regType);
                    if (entryHash == null)
                    {
                        entryHash = new  Hashtable<String, Entry>();
                        typeHash.put(regType, entryHash);
                    }
                    entryHash.put(regNumber, currEntry);
                } else
                {
                    System.err.println("Skipping: "+regNumber);
                }
                
                return true;
            }
        } while (line != null);
        
        return false;
    }
    
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
        
        for (Entry entry : trackHash.values())
        {
            System.out.println(entry);
        }
    }
    
    protected void process(final File inFile) throws IOException
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
        
        for (String key : typeHash.keySet())
        {
            //System.out.println("\n"+key);
            Hashtable<String, Entry> h = typeHash.get(key);
            
            for (Entry entry : h.values())
            {
                //String reg_number = entry.getProps().getProperty("reg_number");
                String reg_type   = entry.getProps().getProperty("reg_type");
                
                if (reg_type.equals("Collection"))
                {
                    Entry parent = typeHash.get("Discipline").get(entry.getProps().getProperty("Discipline_number"));
                    parent.getKids().add(entry);
                    
                } else if (reg_type.equals("Discipline"))
                {
                    Entry parent = typeHash.get("Division").get(entry.getProps().getProperty("Division_number"));
                    parent.getKids().add(entry);
                    
                } else if (reg_type.equals("Division"))
                {
                    Entry parent = typeHash.get("Institution").get(entry.getProps().getProperty("Institution_number"));
                    parent.getKids().add(entry);
                    
                } else if (reg_type.equals("Institution"))
                {
                    root.getKids().add(entry);
                    
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
        
        /*for (Entry entry : regNumHash.values())
        {
            System.out.println(entry.getProps().get(key))
        }*/
    }
    
    protected void printEntries(final Entry parent, final int level)
    {
        for (int i=0;i<level;i++) System.out.print("  ");
        
        System.out.println(parent.getName()+" "+parent.getProps().getProperty("reg_number"));
        for (Entry kid : parent.getKids())
        {
            printEntries(kid, level+1);
        }
    }
    
    //--------------------------------------------------------
    class Entry
    {
        protected String        type;
        protected String        name = null;
        protected String        id;
        protected Properties    props = null;
        protected Vector<Entry> kids  = new Vector<Entry>();
        
        /**
         * 
         */
        public Entry(String name)
        {
            this(name, new Properties());
        }
        
        public Entry(final Properties props)
        {
            this(null, props);
            this.type  = props.getProperty("reg_type");
        }
        
        public Entry(final String name, final Properties props)
        {
            super();
            this.name  = name;
            this.props = props;
        }
        
        /**
         * @return the name
         */
        public String getName()
        {
            if (name == null)
            {
                name = props.getProperty(type+"_name");
                if (name == null)
                {
                    name = "N/A";
                }
            }
            return name;
        }
        /**
         * @param name the name to set
         */
        public void setName(String name)
        {
            this.name = name;
        }
        
        /**
         * @return the id
         */
        public String getId()
        {
            return id;
        }
        /**
         * @param id the id to set
         */
        public void setId(String id)
        {
            this.id = id;
        }
        /**
         * @return the props
         */
        public Properties getProps()
        {
            return props;
        }
        /**
         * @return the kids
         */
        public Vector<Entry> getKids()
        {
            return kids;
        }
        /**
         * @return the type
         */
        public String getType()
        {
            return type;
        }
        /**
         * @param type the type to set
         */
        public void setType(String type)
        {
            this.type = type;
        }
        
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append((name != null ? ("Name: "+ name) : "") + (type != null ? " Type: "+type : "")+ (id != null ? " id: "+id : "") +"\n");
            for (Object key : props.keySet())
            {
                if (key.equals("id")) continue;
                
                sb.append("  ");
                sb.append(key);
                sb.append("=");
                sb.append(props.get(key));
                sb.append("\n");
            }
            return sb.toString();
        }
    }

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
