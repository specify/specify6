/* Filename:    $RCSfile: PickListDBAdapter.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/27 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import edu.ku.brc.specify.dbsupport.HibernateUtil;

/**
 * This is an adaptor class that supports all the necessary functions for supporting a PickList
 * 
 * @author rods
 *
 */
public class PickListDBAdapter
{
    protected Vector<PickListItem> items    = new Vector<PickListItem>(); // Make this Vector because the combobox can use it directly
    protected PickList             pickList = null;
    
    protected static PickListItem searchablePLI = new PickListItem(); // used for binary searches
    
    
    /**
     * Constructor with a unique name
     * @param name the name of the picklist
     * @param createWhenNotFound indicates whether to automatically create the picklist when the name is not found,
     */
    public PickListDBAdapter(final String name, final boolean createWhenNotFound)
    {
        pickList = getPickListItem(name);
        
        if (pickList != null)
        {
             for (Object obj : pickList.getItems())
            {
                items.add((PickListItem)obj); 
            }
             
            // Always keep the list sorted
            Collections.sort(items);
            
         } else if (createWhenNotFound) 
         {
             pickList = new PickList();
             pickList.setCreated(new Date());
             pickList.setName(name);
             pickList.setItems(new HashSet());
             
         } else 
         {
             throw new RuntimeException("PickList ["+name+"] was not found and shouldn't have been created!");
         }
     }
    
    /**
     * Gets the PickList Item from the Database
     * @param name the name of the picklist to get
     * @return the picklist item
     */
    protected PickList getPickListItem(final String name)
    {
        try
        {
	        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(PickList.class).add(Expression.eq("name", name));
            
	        List items = criteria.list();
	        if (items != null && items.size() > 0)
	        {
	            return (PickList)items.get(0);
	        }
	        
        } catch (Exception e)
        {
            e.printStackTrace();
            
        } finally 
        {
            HibernateUtil.closeSession();
        }
        return null;
        
    }
    
    /**
     * Returns the pciklist object
     * @return Returns the pciklist object
     */
    public PickList getPickList()
    {
        return pickList;
    }

    /**
     * Returns the list of PickList items
     * @return Returns the list of PickList items
     */
    public Vector<PickListItem> getList()
    {
        return items;
    }
    
    /**
     * Gets a pick list item by index
     * @param index the index in question
     * @return pick list item by index
     */
    public PickListItem getItem(final int index)
    {
        return (PickListItem)items.get(index);
    }
    
    /**
     * Adds a new item to a picklist
     * @param title the title (or text) of the picklist
     * @param value although currently no supported we may want to display one text string but save a different one
     * @return returns the new PickListItem
     */
    public PickListItem addItem(final String title, final String value)
    {
        // this should never happen!
        if (pickList.getReadOnly())
        {
            throw new RuntimeException("Trying to add an item to a readonly picklist ["+pickList.getName()+"]");
        }
        
        int sizeLimit = 50; // arbitrary size could be a pref (XXX PREF)
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
                PickListItem oldest = null;
                for (PickListItem pli : items)
                {
                    if (oldest == null || pli.getCreatedDate().getTime() < oldest.getCreatedDate().getTime())
                    {
                        oldest = pli;
                    }
                }
                System.out.println("Removing old item["+oldest.getTitle()+"]");
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
            
        } else
        {
            return items.elementAt(index);
        }
    }
    
    /**
     * Persists the picklist and it's items
     * @throws Exception some strange DB exception
     */
    public void save()
    {
        Session session = HibernateUtil.getCurrentSession();
        
        try {
            HibernateUtil.beginTransaction();
            
            session.saveOrUpdate(pickList);
            
            HibernateUtil.commitTransaction();

        } catch (Exception e) 
        {
            HibernateUtil.rollbackTransaction();
            e.printStackTrace();
            
        } finally 
        {
            HibernateUtil.closeSession();
        } 
    }
    
}
