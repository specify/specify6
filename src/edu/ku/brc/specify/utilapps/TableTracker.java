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

import java.awt.Font;
import java.io.File;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JLabel;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerXMLHelper;

/**
 * Reads in and Tracks each tables usage.
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 27, 2007
 *
 */
public class TableTracker
{
    protected Vector<ERDTable> list;
    protected Font font;
    
    protected Hashtable<String, Boolean>  usedHash     = new Hashtable<String, Boolean>();
    protected Hashtable<String, NodeInfo> nodeInfoHash = new Hashtable<String, NodeInfo>();
    protected Hashtable<String, ERDTable> hash         = new Hashtable<String, ERDTable>();

    protected NodeInfo defaultNodeInfo     = new NodeInfo();
    protected NodeInfo defaultSkipNodeInfo = new NodeInfo(true, false, false, true, null);
    
    protected DBTableIdMgr tableMgr;

    
    /**
     * 
     */
    public TableTracker()
    {
        boolean doWorkBench = false;
        if (doWorkBench)
        {
            tableMgr = new DBTableIdMgr(false);
            tableMgr.initialize(new File(XMLHelper.getConfigDirPath("specify_workbench_datamodel.xml")));
            
            SchemaLocalizerXMLHelper schemaLocalizer = new SchemaLocalizerXMLHelper(SpLocaleContainer.WORKBENCH_SCHEMA, tableMgr);
            schemaLocalizer.load();
            schemaLocalizer.setTitlesIntoSchema();            
        } else
        {
            tableMgr = DBTableIdMgr.getInstance();  
        }
        
        font = new JLabel().getFont().deriveFont((float)11.0);
        list = new Vector<ERDTable>();
        
        defaultNodeInfo.setClassName("defaultNodeInfo");
        defaultNodeInfo.setClassName("defaultSkipNodeInfo");
        
        for (DBTableInfo tbl : tableMgr.getTables())
        {
            ERDTable table = new ERDTable(tbl);
            table.build(font);
            list.add(table);
            hash.put(table.getTable().getClassName(), table);
        }
        
        Collections.sort(list);
    }
    
    public Hashtable<String, ERDTable> getHash()
    {
        return hash;
    }
    
    public Hashtable<String, Boolean> getUsedHash()
    {
        return usedHash;
    }
    
    public Vector<ERDTable> getList()
    {
        return list;
    }
    
    public ERDTable getTable(final String name)
    {
        return hash.get("edu.ku.brc.specify.datamodel."+name);
    }
    
    public ERDTable getTableByClassName(final String fullClassName)
    {
        return hash.get(fullClassName);
    }
    
    /**
     * @return the font
     */
    public Font getFont()
    {
        return font;
    }
    
    /**
     * @param font the font to set
     */
    public void setFont(Font font)
    {
        this.font = font;
    }

    public NodeInfo getNodeInfo(final ERDTable table)
    {
        if (table == null)
        {
            return defaultSkipNodeInfo;
        }
        
        //System.out.println(table.getTable().getClassName());
        NodeInfo ni = nodeInfoHash.get(table.getClassName());
        if (ni == null)
        {
            return defaultNodeInfo;
        }
        return ni;
    }
    
    protected void fillListFromTree(final ERDTable table, Vector<ERDTable> nList)
    {
       nList.add(table);
       for (ERDTable kid : table.getKids())
       {
           fillListFromTree(kid, nList);
       }
    }
    
    public Vector<ERDTable> getTreeAsList(final ERDTable table)
    {
        Vector<ERDTable> nList = new Vector<ERDTable>();
        fillListFromTree(table, nList);
        return nList;
    }
    
    /**
     * @param name
     * @return
     */
    public NodeInfo getNodeInfo(final String name)
    {
        ERDTable table = getTable(name);
        if (table != null)
        {
            NodeInfo ni = nodeInfoHash.get(table.getClassName());
            if (ni == null)
            {
                return defaultNodeInfo;
            }
            return ni;
        }
        return null;
    }
    
    /**
     * @param name
     * @param skip
     * @param processKids
     * @param alwaysAKid
     * @param processAnyRel
     * @param okWhenParent
     * @return
     */
    public NodeInfo addNodeInfo(final String name, final boolean skip, final boolean processKids, final boolean alwaysAKid, final boolean processAnyRel, final ERDTable okWhenParent)
    {
        ERDTable table = getTable(name);
        if (table != null)
        {
            NodeInfo ni = new NodeInfo(skip, processKids, alwaysAKid, processAnyRel, okWhenParent);
            ni.setClassName(name);
            nodeInfoHash.put(table.getClassName(), ni);
            return ni;
        } else
        {
            System.out.println("Couldn't find ["+name+"]");
        }
        return null;
    }
}
