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
package edu.ku.brc.ui.db;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;

import edu.ku.brc.dbsupport.HibernateUtil;

/**
 * This is an adaptor class that supports all the necessary functions for supporting a PickList.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class PickListDBAdapter
{
    // Static Data Memebers
    protected static final Logger log                = Logger.getLogger(PickListDBAdapter.class);
    protected static PickListItem searchablePLI = new PickListItem(); // used for binary searches
    
    // Data Memebers        
    protected Vector<PickListItem> items    = new Vector<PickListItem>(); // Make this Vector because the combobox can use it directly
    protected PickList             pickList = null;
    
    
    
    /**
     * Protected Default constructor derving subclasses.
     */
    protected PickListDBAdapter()
    {
        pickList = new PickList();
        pickList.initialize();
    }
    
    /**
     * Constructor with a unique name.
     * @param name the name of the picklist
     * @param createWhenNotFound indicates whether to automatically create the picklist when the name is not found,
     */
    public PickListDBAdapter(final String name, final boolean createWhenNotFound)
    {
        pickList = getPickList(name);
        
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
             Session session = HibernateUtil.getSessionFactory().openSession();
             try
             {
                 pickList = new PickList();
                 pickList.setCreated(new Date());
                 pickList.setName(name);
                 pickList.setReadOnly(false);
                 pickList.setItems(new HashSet<PickListItem>());
                 
                 session.beginTransaction();
                 session.save(pickList);
                 session.getTransaction().commit();
                 
             } catch (Exception ex)
             {
                 log.error(ex);
                 session.getTransaction().rollback();
                 
             } finally 
             {
                 if (session != null)
                 {
                     session.close();
                 }
             }

             
         } else 
         {
             throw new RuntimeException("PickList ["+name+"] was not found and shouldn't have been created!");
         }
     }
    
    /**
     * Gets the PickList Item from the Database.
     * @param name the name of the picklist to get
     * @return the picklist
     */
    protected PickList getPickList(final String name)
    {
        PickList pkList  = null;
        Session  session = null;
        try
        {
            session = HibernateUtil.getSessionFactory().openSession();
	        Criteria criteria = session.createCriteria(PickList.class).add(Expression.eq("name", name));
            
	        List items = criteria.list();
	        if (items != null && items.size() > 0)
	        {
                pkList = (PickList)items.get(0);
	        }
	        
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
            
        } finally 
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        return pkList;
        
    }
    
    /**
     * Returns the pciklist object.
     * @return Returns the pciklist object
     */
    public PickList getPickList()
    {
        return pickList;
    }

    /**
     * Returns the list of PickList items.
     * @return Returns the list of PickList items
     */
    public Vector<PickListItem> getList()
    {
        return items;
    }
    
    /**
     * Gets a pick list item by index.
     * @param index the index in question
     * @return pick list item by index
     */
    public PickListItem getItem(final int index)
    {
        return (PickListItem)items.get(index);
    }
    
    /**
     * Adds a new item to a picklist.
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
                PickListItem oldest = null;
                for (PickListItem pli : items)
                {
                    if (oldest == null || pli.getCreatedDate().getTime() < oldest.getCreatedDate().getTime())
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
            
        } else
        {
            return items.elementAt(index);
        }
    }
    
    /**
     * Persists the picklist and it's items.
     */
    public void save()
    {
        Session     session = null;
        Transaction trans   = null;
        
        try 
        {
            session = HibernateUtil.getSessionFactory().openSession();
            trans = session.beginTransaction();
            
            session.saveOrUpdate(pickList);
            
            trans.commit();
            

        } catch (Exception e) 
        {
            trans.rollback();
            
            e.printStackTrace();
            
        } finally 
        {
            session.close();
        } 
    }
    
}
