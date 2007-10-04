/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.utilapps;

import java.awt.Font;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JLabel;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;

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

    
    /**
     * 
     */
    public TableTracker()
    {
        font = new JLabel().getFont().deriveFont((float)11.0);
        list = new Vector<ERDTable>();
        
        defaultNodeInfo.setClassName("defaultNodeInfo");
        defaultNodeInfo.setClassName("defaultSkipNodeInfo");
        
        for (DBTableInfo tbl : DBTableIdMgr.getInstance().getList())
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
