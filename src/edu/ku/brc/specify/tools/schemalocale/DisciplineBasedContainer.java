/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.tools.schemalocale;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

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
     * @param discipline
     * @return
     */
    public Set<SpLocaleContainerItem> getDisciplineItems(final String discipline)
    {
        if (disciplineHashItems == null)
        {
            disciplineHashItems = new Hashtable<String, Set<SpLocaleContainerItem>>();
        }
        
        if (disciplineHashItems.get(discipline) == null)
        {
            HashSet<SpLocaleContainerItem> dispHash = new HashSet<SpLocaleContainerItem>();
            disciplineHashItems.put(discipline, dispHash);
            return dispHash;
        }
        
        return disciplineHashItems.get(discipline);
    }
    
    /**
     * @param discipline
     * @param item
     */
    public void add(final String discipline, final SpLocaleContainerItem item)
    {
        Set<SpLocaleContainerItem>  dispSet;
        if (disciplineHashItems == null)
        {
            disciplineHashItems = new Hashtable<String, Set<SpLocaleContainerItem>>();
            dispSet             = new HashSet<SpLocaleContainerItem>();
            disciplineHashItems.put(discipline, dispSet);

        } else
        {
            dispSet = disciplineHashItems.get(discipline);
            if (dispSet == null)
            {
                dispSet = new HashSet<SpLocaleContainerItem>();
                disciplineHashItems.put(discipline, dispSet);
            }
        }
        
        dispSet.add(item);
    }
    
    /**
     * @param discipline
     */
    public void remove(final String discipline)
    {
        if (disciplineHashItems != null)
        {  
            Set<SpLocaleContainerItem> dispSet = disciplineHashItems.get(discipline);
            if (dispSet != null)
            {
                dispSet.remove(discipline);
                
            } else
            {
                log.error("dispSet can't be null for discipline["+discipline+"]");
            }
            
        } else
        {
            log.error("disciplineHashItems can't be null!");
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
        dbc.collectionType = collectionType;
        
        return dbc;
    }
}
