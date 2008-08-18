/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * A list of valid values for predefined system coded fields like Agent.agentType, DeterminationStatus.type
 *
 */
public class RecordTypeCode implements PickListDBAdapterIFace, ComboBoxModel
{
    protected final Vector<PickListItemIFace> items;
    protected final String                   fldName;
    
    protected Object selectedItem = null;
    
    /**
     * Default constructor for a dead RecordTypeCode.
     */
    public RecordTypeCode()
    {
        items = null;
        fldName = null;
    }
    
    /**
     * @param items
     * @param fldInfo
     * @param tblInfo
     */
    public RecordTypeCode(final Vector<PickListItemIFace> items, final String fldName)
    {
        this.items = items;
        this.fldName = fldName;
    }

    
    /**
     * @param title
     * @return item with matching title.
     */
    public PickListItemIFace getItemByTitle(final String title)
    {
        for (PickListItemIFace item : items)
        {
            if (item.getTitle().equals(title))
            {
                return item;
            }
        }
        return null;
    }
    
    public PickListItemIFace getItemByValue(final Object value)
    {
        for (PickListItemIFace item : items)
        {
            if (item.getValueObject().equals(value))
            {
                return item;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#addItem(java.lang.String, java.lang.String)
     */
    @Override
    public PickListItemIFace addItem(String title, String value)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getItem(int)
     */
    @Override
    public PickListItemIFace getItem(int index)
    {
        return items.get(index);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getList()
     */
    @Override
    public Vector<PickListItemIFace> getList()
    {
        return items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getPickList()
     */
    @Override
    public PickListIFace getPickList()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getType()
     */
    @Override
    public Type getType()
    {
        return PickListDBAdapterIFace.Type.Item;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#isReadOnly()
     */
    @Override
    public boolean isReadOnly()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#isTabledBased()
     */
    @Override
    public boolean isTabledBased()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#save()
     */
    @Override
    public void save()
    {
        //nothing
    }

    /**
     * @return the fldName
     */
    public String getFldName()
    {
        return fldName;
    }

    /* (non-Javadoc)
     * @see javax.swing.ComboBoxModel#getSelectedItem()
     */
    @Override
    public Object getSelectedItem()
    {
        return selectedItem;
    }

    /* (non-Javadoc)
     * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
     */
    @Override
    public void setSelectedItem(Object anItem)
    {
        selectedItem = anItem;
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
     */
    @Override
    public void addListDataListener(ListDataListener l)
    {
        //rather not
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getElementAt(int)
     */
    @Override
    public Object getElementAt(int index)
    {
        return getItem(index);
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getSize()
     */
    @Override
    public int getSize()
    {
        return items.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
     */
    @Override
    public void removeListDataListener(ListDataListener l)
    {
        // certainly not
    }
    
    
}
