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
