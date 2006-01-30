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
     * Constructor with ID
     * @param id the id of the picklist
     */
    public PickListDBAdapter(int id)
    {
        pickList = getPickListItem(id);
        
        if (pickList != null)
        {
             for (Object obj : pickList.getItems())
            {
                items.add((PickListItem)obj); 
            }
             
            // Always keep the list sorted
            Collections.sort(items);
            
         } else
         {
             pickList = new PickList();
             pickList.setCreated(new Date());
             pickList.setPicklist_id(id);
             pickList.setItems(new HashSet());
         }
     }
    
    /**
     * Gets the PickList Item from the Database
     * @param id the id of the picklist to get
     * @return the picklist item
     */
    protected PickList getPickListItem(int id)
    {
        try
        {
	        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(PickList.class).add(Expression.eq("picklist_id", new Integer(id)));
            
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
        searchablePLI.setTitle(title);
        int index = Collections.binarySearch(items, searchablePLI);
        if (index < 0)
        {
            PickListItem item = new PickListItem(title, value);
            items.add(item);
            Collections.sort(items);
            
            if (pickList != null)
            {
                pickList.getItems().add(item);
            }
            
            try 
            {
                save();
                
            } catch (Exception e)
            {
                e.printStackTrace();
            }
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
    public void save() throws Exception
    {
        Session session = HibernateUtil.getCurrentSession();
        
        try {
            HibernateUtil.beginTransaction();
            
            session.saveOrUpdate(pickList);
            
            HibernateUtil.commitTransaction();

        } catch (Exception e) 
        {
            HibernateUtil.rollbackTransaction();
            throw e;
            
        } finally 
        {
            HibernateUtil.closeSession();
        } 
    }
    
}
