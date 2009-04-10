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
package edu.ku.brc.specify.tools.schemalocale;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 2, 2007
 *
 */
public class DisciplineBasedContainer extends SpLocaleContainer implements Cloneable
{
    private static final Logger log = Logger.getLogger(DisciplineBasedContainer.class);
    
    protected Hashtable<String, Set<SpLocaleContainerItem>> disciplineHashItems = null;
    
    /**
     * 
     */
    public DisciplineBasedContainer()
    {
        super();
    }
    
    /**
     * @return
     */
    public boolean hasDisciplineItems()
    {
        return disciplineHashItems != null;
    }
    
    /**
     * @param dispName
     * @return
     */
    public boolean hasDiscipline(final String dispName)
    {
        return disciplineHashItems != null && disciplineHashItems.get(dispName) != null;
    }
    
    /**
     * @param disciplineType
     * @return
     */
    public Set<SpLocaleContainerItem> getDisciplineItems(final String disciplineArg)
    {
        if (disciplineHashItems == null)
        {
            disciplineHashItems = new Hashtable<String, Set<SpLocaleContainerItem>>();
        }
        
        if (disciplineHashItems.get(disciplineArg) == null)
        {
            HashSet<SpLocaleContainerItem> dispHash = new HashSet<SpLocaleContainerItem>();
            disciplineHashItems.put(disciplineArg, dispHash);
            return dispHash;
        }
        
        return disciplineHashItems.get(disciplineArg);
    }
    
    /**
     * @param disciplineType
     * @param item
     */
    public void add(final String disciplineArg, final SpLocaleContainerItem item)
    {
        Set<SpLocaleContainerItem>  dispSet;
        if (disciplineHashItems == null)
        {
            disciplineHashItems = new Hashtable<String, Set<SpLocaleContainerItem>>();
            dispSet             = new HashSet<SpLocaleContainerItem>();
            disciplineHashItems.put(disciplineArg, dispSet);

        } else
        {
            dispSet = disciplineHashItems.get(disciplineArg);
            if (dispSet == null)
            {
                dispSet = new HashSet<SpLocaleContainerItem>();
                disciplineHashItems.put(disciplineArg, dispSet);
            }
        }
        
        dispSet.add(item);
    }
    
    /**
     * @param disciplineType
     */
    public void remove(final String disciplineArg)
    {
        if (disciplineHashItems != null)
        {  
            Set<SpLocaleContainerItem> dispSet = disciplineHashItems.get(disciplineArg);
            if (dispSet != null)
            {
                dispSet.remove(disciplineArg);
                
            } else
            {
                log.error("dispSet can't be null for disciplineType["+disciplineArg+"]");
            }
            
        } else
        {
            log.error("disciplineHashItems can't be null!");
        }
    }
    
    /**
     * @param disciplineType
     */
    public void merge(final String disciplineArg)
    {
        Set<SpLocaleContainerItem> dispSet = disciplineHashItems.get(disciplineArg);
        for (SpLocaleContainerItem item : new Vector<SpLocaleContainerItem>(items))
        {
            for (SpLocaleContainerItem dispItem : dispSet)
            {
                if (item.getName().equalsIgnoreCase(dispItem.getName()))
                {
                    items.remove(item);
                    items.add(dispItem);
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    public Object clone()
    {
        DisciplineBasedContainer dbc = new DisciplineBasedContainer();
        dbc.initialize();
        dbc.schemaType     = schemaType;
        dbc.discipline = discipline;
        dbc.name           = name;
        dbc.type           = type;
        
        return dbc;
    }
}
