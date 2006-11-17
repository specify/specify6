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
package edu.ku.brc.specify.ui.db;

import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.ui.db.PickListIFace;
import edu.ku.brc.ui.db.PickListItemIFace;

/**
 * This is an adaptor class that supports all the necessary functions for supporting a PickList.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class PickListDBAdapter implements PickListDBAdapterIFace
{
    // Static Data Memebers
    protected static final Logger log                = Logger.getLogger(PickListDBAdapter.class);
    protected static PickListItemIFace searchablePLI = new PickListItem(); // used for binary searches
    
    // Data Memebers        
    protected Vector<PickListItemIFace> items    = new Vector<PickListItemIFace>(); // Make this Vector because the combobox can use it directly
    protected PickListIFace             pickList = null;
     
    
    /**
     * Protected Default constructor derving subclasses.
     */
    protected PickListDBAdapter()
    {
        PickList pl = new PickList();
        pl.initialize();
        pickList = pl;
    }
    
    /**
     * Creates an adapter from a PickList.
     * @param pickList the picklist
     */
    public PickListDBAdapter(final PickList pickList)
    {
        this.pickList = pickList;
        
        for (PickListItemIFace pli : pickList.getItems())
        {
            items.add(pli); 
        }
         
        // Always keep the list sorted
        Collections.sort(items);
    }
    
    /**
     * Constructor with a unique name.
     * @param name the name of the picklist
     */
    public PickListDBAdapter(final String name)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            PickList pl = new PickList();
            pl.initialize();                 
            pl.setName(name);
            pickList = pl;
            
            session.beginTransaction();
            session.save(pickList);
            session.commit();
            
        } catch (Exception ex)
        {
            log.error(ex);
            session.rollback();
            
        } finally 
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getPickList()
     */
    public PickListIFace getPickList()
    {
        return pickList;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getList()
     */
    public Vector<PickListItemIFace> getList()
    {
        return items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getItem(int)
     */
    public PickListItemIFace getItem(final int index)
    {
        return items.get(index);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#addItem(java.lang.String, java.lang.String)
     */
    public PickListItemIFace addItem(final String title, final String value)
    {
        // this should never happen!
        if (pickList.getReadOnly())
        {
            throw new RuntimeException("Trying to add an item to a readonly picklist ["+pickList.getName()+"]");
        }
        
        int     sizeLimit = 50; // arbitrary size could be a pref (XXX PREF)
        Integer sizeLimitInt = pickList.getSizeLimit();
        if (sizeLimitInt != null)
        {
            sizeLimit = sizeLimitInt.intValue();
        }
        
        searchablePLI.setTitle(title);
        int index = Collections.binarySearch(items, searchablePLI);
        if (index < 0)
        {
            // find oldest item and remove it
            if (items.size() >= sizeLimit) 
            {
                PickListItemIFace oldest = null;
                for (PickListItemIFace pli : items)
                {
                    if (oldest == null || pli.getTimestampCreated().getTime() < oldest.getTimestampCreated().getTime())
                    {
                        oldest = pli;
                    }
                }
                items.remove(oldest);
                pickList.getItems().remove(oldest);
            }
            
            PickListItem item = new PickListItem(title, value, new Date());
            items.add(item);
            Collections.sort(items);
            
            if (pickList != null)
            {
                pickList.getItems().add(item);
            }

            save();

            return item;
            
        }
        // else
        return items.elementAt(index);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#save()
     */
    public void save()
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            
            session.saveOrUpdate(pickList);
            
            session.commit();
            

        } catch (Exception e) 
        {
            // ignoring warning about 'null'
            if (session != null)
            {
                session.rollback();
            }
            
            e.printStackTrace();
            
        } finally 
        {
            if (session != null)
            {
                session.close();
            }
        } 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#isReadOnly()
     */
    public boolean isReadOnly()
    {
        return pickList.getReadOnly();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#isTabledBased()
     */
    public boolean isTabledBased()
    {
        return pickList == null ? false : pickList.getType() > 0;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getType()
     */
    public PickListDBAdapterIFace.Type getType()
    {
        switch (pickList.getType())
        {
            case 0 : return PickListDBAdapterIFace.Type.Item;
            case 1 : return PickListDBAdapterIFace.Type.Table;
            case 2 : return PickListDBAdapterIFace.Type.TableField;
        }
        throw new RuntimeException("Unknown picklist type["+pickList.getType()+"]");
    }
    
}
