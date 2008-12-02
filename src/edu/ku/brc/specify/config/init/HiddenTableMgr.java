/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config.init;

import java.util.Hashtable;
import java.util.List;

import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 16, 2008
 *
 */
public class HiddenTableMgr
{
    private Hashtable<String, Hashtable<String, Boolean>> disciplines = new Hashtable<String, Hashtable<String,Boolean>>();
    
    /**
     * Constructs and loads.
     */
    public HiddenTableMgr()
    {
        load();
    }
    
    /**
     * Loads the hiddent table XML
     */
    @SuppressWarnings("unchecked")
    private void load()
    {
        Element dom = XMLHelper.readDOMFromConfigDir("hiddentables.xml");
        if (dom != null)
        {
            for (Element element : (List<Element>)dom.selectNodes("/disciplines/discipline"))
            {
                String dName = XMLHelper.getAttr(element, "name", null);
                Hashtable<String, Boolean> tableHash = disciplines.get(dName);
                if (tableHash == null)
                {
                    tableHash = new Hashtable<String, Boolean>();
                    disciplines.put(dName, tableHash);
                }
                for (Element tbl : (List<Element>)element.selectNodes("table"))
                {
                    String tName = XMLHelper.getAttr(tbl, "name", null);
                    tableHash.put(tName, true);
                }  
            }
        }
    }
    
    /**
     * Returns if the table is hidden for this discipline.
     * @param disciplineType the discipline name (not localized)
     * @param tableName the table name (not the class name).
     * @return true if hidden
     */
    public boolean isHidden(final String disciplineType, final String tableName)
    {
        Hashtable<String, Boolean> tableHash = disciplines.get(disciplineType);
        if (tableHash != null)
        {
            Boolean isHidden = tableHash.get(tableName);
            return isHidden != null && isHidden ? true : false;
        }
        throw new RuntimeException("The discipline["+disciplineType+"] was not found.");
    }
    
}
