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
package edu.ku.brc.specify.utilapps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 27, 2008
 *
 */
public class RegProcEntry extends DefaultMutableTreeNode implements Comparable<RegProcEntry>
{
    protected RegProcEntry         parent;
    protected String               type;
    protected String               name  = null;
    protected String               id;
    protected Properties           props = null;
    protected Vector<RegProcEntry> kids  = new Vector<RegProcEntry>();
    
    protected String               isaNumber = null;
    protected boolean              isSorted = false;
    protected Timestamp            timestampCreated;
    
    protected String               hostName = null; 
    
    // static
    private static final String HASH_FILENAME = "hash.xml";
    private static HashMap<String, String> ipHash = null;
    
    static
    {
        loadIPHash();
    }
    
    /**
     * 
     */
    public RegProcEntry()
    {
        super();
        props = new Properties();
    }

    /**
     * 
     */
    public RegProcEntry(final String name)
    {
        this(name, new Properties());
    }
    
    public RegProcEntry(final Properties props)
    {
        this.props = props;
        this.type  = props.getProperty("reg_type");
        
        discoverHostName();
    }
    
    public RegProcEntry(final String name, final Properties props)
    {
        super();
        this.name  = name;
        this.props = props;
        
        discoverHostName();
    }
    
    private static void loadIPHash()
    {
        if (ipHash == null)
        {
            try
            {
                File hashFile = new File(HASH_FILENAME);
                if (hashFile.exists())
                {
                    XStream xstream = new XStream();
                    xstream.alias("hashmap", HashMap.class);
                    ipHash = (HashMap<String, String>)xstream.fromXML(new FileInputStream(hashFile));
                    
                } else
                {
                    ipHash = new HashMap<String, String>();
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * 
     */
    public static void cleanUp()
    {
        try
        {
            File hashFile = new File(HASH_FILENAME);
            XStream xstream = new XStream();
            xstream.alias("hashmap", HashMap.class);
            xstream.toXML(ipHash, new FileOutputStream(hashFile));
                
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void sortKids()
    {
        Collections.sort(kids);
    }
    
    private void discoverHostName()
    {
        if (hostName == null)
        {
            String ip = props.getProperty("ip");
            if (ip == null)
            {
                ip = "";
                hostName = "";
                props.put("hostname", hostName);
            } else
            {
                hostName = ipHash.get(ip);
            }
            if (hostName == null)
            {
                try
                {
                    InetAddress addr = InetAddress.getByName(ip);
                    hostName = addr.getHostName();
                    ipHash.put(ip, hostName);
                    
                } catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
            }
            props.put("hostname", hostName);
        }
    }
    
    /**
     * @param propName
     * @return
     */
    public String get(final String propName)
    {
        return props.getProperty(propName);
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
                name = props.getProperty(type+"_type");
                if (name == null)
                {
                    name = "Anonymous";
                }
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
    
    public Set<Object> keySet()
    {
        return props.keySet();
    }
    
    public void put(final String key, final String value)
    {
        props.put(key, value);
        if (hostName == null && key.equals("ip"))
        {
            discoverHostName();
        }
    }
    
    /**
     * @return the kids
     */
    public Vector<RegProcEntry> getKids()
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
    
    /**
     * @return
     */
    public String getVersion()
    {
        String verStr = props.getProperty("app_version");
        if (verStr == null)
        {
            verStr = "Unknown";
        }
        return verStr;
    }
    
    /**
     * @return whether this node (as a collection) has been registered.
     */
    public boolean isRegistered()
    {
        return StringUtils.isNotEmpty(getISANumber());
    }
    
    /**
     * @return the isaNumber
     */
    public String getISANumber()
    {
        if (type != null && type.equals("Collection") && isaNumber == null)
        {
            isaNumber = props.getProperty("ISA_Number", "");
        }
        return isaNumber == null ? "" : isaNumber;
    }

    /**
     * @return the timestampCreated
     */
    public Timestamp getTimestampCreated()
    {
        return timestampCreated;
    }

    /**
     * @param timestampCreated the timestampCreated to set
     */
    public void setTimestampCreated(Timestamp timestampCreated)
    {
        this.timestampCreated = timestampCreated;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if (parent != null)
        {
            return getName() + (isRegistered() ? ("  (Registered)") : "");
        }
        
        getISANumber();
        
        StringBuilder sb = new StringBuilder();
        sb.append((name != null ? ("Name: "+ name) : "") + (type != null ? " Type: "+type : "")+ (id != null ? " id: "+id : ""));
        sb.append("\n");
        
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

    /**
     * @param parent the parent to set
     */
    public void setParent(RegProcEntry parent)
    {
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#children()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Enumeration children()
    {
        return kids.elements();
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    @Override
    public boolean getAllowsChildren()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    @Override
    public TreeNode getChildAt(int childIndex)
    {
        if (!isSorted)
        {
            Collections.sort(kids);
            isSorted = true;
        }
        return kids.get(childIndex);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    @Override
    public int getChildCount()
    {
        return kids.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
     */
    @Override
    public int getIndex(TreeNode node)
    {
        return kids.indexOf(node);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getParent()
     */
    @Override
    public TreeNode getParent()
    {
        return parent;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    @Override
    public boolean isLeaf()
    {
        return kids.size() == 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(RegProcEntry obj)
    {
        return getName().compareTo(obj.getName());
    }
    
    
}
