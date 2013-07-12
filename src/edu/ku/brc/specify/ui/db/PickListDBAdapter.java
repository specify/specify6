/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.ui.db;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.ui.AutoCompComboBoxModelIFace;

/**
 * This is an adaptor class that supports all the necessary functions for supporting a PickList.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class PickListDBAdapter extends AbstractListModel implements PickListDBAdapterIFace, 
                                                                    MutableComboBoxModel, 
                                                                    AutoCompComboBoxModelIFace
{
    // Static Data Members
    protected static final Logger log                = Logger.getLogger(PickListDBAdapter.class);
    protected static PickListItemIFace searchablePLI = new PickListItem(); // used for binary searches
    
    // Data Members        
    protected Vector<PickListItemIFace> items    = new Vector<PickListItemIFace>(); // Make this Vector because the combobox can use it directly
    protected PickList                  pickList = null;
    
    protected Object                    selectedObject  = null;
    protected boolean                   doAutoSaveOnAdd = true;
    protected boolean                   needsToBeSaved  = false;
    protected Vector<ChangeListener>    changeListeners = new Vector<ChangeListener>();
     
    
    /**
     * Protected Default constructor deriving subclasses.
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
        
        loadItems(false);
        
        super.fireContentsChanged(this, 0, items.size()-1);
    }
    
    /**
     * 
     */
    protected void loadItems(final boolean doReload)
    {
        if (doReload)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                pickList = session.get(PickList.class, pickList.getId());
    
            } catch (Exception e) 
            {
                
            } finally 
            {
                if (session != null)session.close();
            } 
        }
        
        items.clear();
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
        pickList = new PickList();
        pickList.initialize();                 
        pickList.setName(name);
        
        DataModelObjBase.save(pickList);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterIFace#setAutoSaveOnAdd(boolean)
     */
    @Override
    public void setAutoSaveOnAdd(boolean doAutoSave)
    {
        doAutoSaveOnAdd = doAutoSave;
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
            needsToBeSaved = true;
            
            //System.out.println(pickList.getItems().size());
            //if (doAutoSaveOnAdd)
            {
                int version = BasicSQLUtils.getCount("SELECT Version FROM picklist WHERE PickListID = " + pickList.getId());
                if (version != pickList.getVersion())
                {
                    loadItems(true);
                }
            }
            
            //System.out.println(pickList.getItems().size());

            // find oldest item and remove it
            if (items.size() >= sizeLimit && sizeLimit > 0) 
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
                pickList.removeItem(oldest);
            }
            
            PickListItem item = new PickListItem(title, value, new Timestamp(System.currentTimeMillis()));
            items.add(item);
            
            if (pickList != null)
            {
                pickList.addItem(item);
                item.setPickList(pickList);
                pickList.reorder();
            }
            
            Collections.sort(items);

            if (doAutoSaveOnAdd)
            {
                save();
            }
            
            super.fireContentsChanged(this, 0, items.size()-1);
            
            for (ChangeListener cl : changeListeners)
            {
                cl.stateChanged(new ChangeEvent(this));
            }

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
        if (needsToBeSaved)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                session.beginTransaction();
                session.saveOrUpdate(pickList);
                session.commit();
                needsToBeSaved = false;
                
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PickListDBAdapter.class, e);
                // ignoring warning about 'null'
                if (session != null)
                {
                    session.rollback();
                }
                
                e.printStackTrace();
                
            } finally 
            {
                if (session != null) session.close();
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterIFace#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener(ChangeListener l)
    {
        if (l != null) changeListeners.add(l);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterIFace#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener(ChangeListener l)
    {
        if (l != null) changeListeners.remove(l);
    }
    
    //------------------------------------------------------------------------
    //-- Default
    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.swing.ComboBoxModel#getSelectedItem()
     */
    public Object getSelectedItem()
    {
        return selectedObject;
    }

    /* (non-Javadoc)
     * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
     */
    public void setSelectedItem(Object anObject)
    {
        if ((selectedObject != null && !selectedObject.equals(anObject)) || selectedObject == null
                && anObject != null)
        {
            selectedObject = anObject;
            fireContentsChanged(this, -1, -1);
            //System.out.println("*************** SetSelectedItem["+selectedObject+"]");
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index)
    {
        if ( index >= 0 && index < items.size() )
        {
            return items.elementAt(index);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize()
    {
        if (pickList.getId() == 28) log.debug("Size: "+items.size()+"  Id: "+pickList.getId());
        return items.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.MutableComboBoxModel#addElement(java.lang.Object)
     */
    public void addElement(Object anObject)
    {
        
        if (anObject instanceof PickListItemIFace)
        {
            PickListItemIFace item = (PickListItemIFace)anObject;
            items.add(item);
            item.setPickList(pickList);
            if (pickList != null)
            {
                pickList.addItem(item);
                pickList.reorder();
            }
            
            Collections.sort(items);
            super.fireContentsChanged(this, 0, items.size()-1);
            
        } else if (anObject instanceof String)
        {
            addItem((String)anObject, (String)anObject);
        } else
        {
            throw new RuntimeException("Inserting item that is not a String or PickListItemIFace");
        }
        
        fireIntervalAdded(this, items.size()-1, items.size()-1);
        if ( items.size() == 1 && selectedObject == null) 
        {
            setSelectedItem( anObject );
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.MutableComboBoxModel#insertElementAt(java.lang.Object, int)
     */
    public void insertElementAt(Object obj, int index)
    {
        if (obj instanceof PickListItemIFace)
        {
            PickListItemIFace item = (PickListItemIFace)obj;
            
            items.insertElementAt((PickListItemIFace)obj, index);
            item.setPickList(pickList);
            if (pickList != null)
            {
                pickList.addItem(item);
                pickList.reorder();
            }
            
        } else if (obj instanceof String)
        {
            addItem((String)obj, (String)obj);
            
        } else
        {
            throw new RuntimeException("Inserting item that is not a String or PickListItemIFace");
        }
        fireIntervalAdded(this, index, index);
    }

    /* (non-Javadoc)
     * @see javax.swing.MutableComboBoxModel#removeElement(java.lang.Object)
     */
    public void removeElement(Object anObject) 
    {
        int index = items.indexOf(anObject);
        if ( index != -1 ) 
        {
            removeElementAt(index);
        }
        
        if (pickList != null)
        {
            pickList.reorder();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.MutableComboBoxModel#removeElementAt(int)
     */
    public void removeElementAt(int index)
    {
        if ( getElementAt( index ) == selectedObject ) 
        {
            PickListItemIFace item = (PickListItemIFace)selectedObject;
            
            if ( index == 0 ) 
            {
                setSelectedItem( getSize() == 1 ? null : getElementAt( index + 1 ) );
            } else 
            {
                setSelectedItem( getElementAt( index - 1 ) );
            }
            
            if (pickList != null)
            {
                pickList.removeItem(item);
            }
        }

        items.removeElementAt(index);

        fireIntervalRemoved(this, index, index);
    }

    //-------------------------------------------------
    // Interface AutoCompComboBoxModelIFace
    //-------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.AutoCompComboBoxModelIFace#add(java.lang.Object)
     */
    public void add(Object item)
    {
        //addElement(item);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.AutoCompComboBoxModelIFace#isMutable()
     */
    public boolean isMutable()
    {
        return !isReadOnly();
    }
    
}
