/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.datamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import edu.ku.brc.ui.forms.FormDataObjIFace;

public abstract class DataModelObjBase implements FormDataObjIFace
{
    protected Hashtable<String, Vector<PropertyChangeListener>> propListenersHash = null;
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#initialize()
     */
    public abstract void initialize();

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getId()
     */
    public Long getId()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getIdentityTitle()
     */
    public String getIdentityTitle()
    {
        Long id = getId();
        
        return id != null ? id.toString() : "";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTimestampCreated()
     */
    public Date getTimestampCreated()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setTimestampCreated(java.util.Date)
     */
    public void setTimestampCreated(Date timestampCreated)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTimestampModified()
     */
    public Date getTimestampModified()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setTimestampModified(java.util.Date)
     */
    public void setTimestampModified(Date timestampModified)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getLastEditedBy()
     */
    public String getLastEditedBy()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setLastEditedBy(java.lang.String)
     */
    public void setLastEditedBy(String lastEditedBy)
    {

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    public Integer getTableId()
    {
        throw new RuntimeException("You must implement this for all the classes! (Or certainly this class)");
    }
    
    //---------------------------------------------------------------------------
    // Property Change Support
    //---------------------------------------------------------------------------
    
    /**
     * Notifies all listeners that a property has changed.
     * @param propertyName  the property name
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void notifyPropListeners(final Vector<PropertyChangeListener> list, final PropertyChangeEvent evt)
    {
        if (list != null)
        {
            for (PropertyChangeListener l : list)
            {
                l.propertyChange(evt);
            }
        }
    }
    
    /**
     * Notifies all listeners that a property has changed.
     * @param propertyName  the property name
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void notifyPropListeners(final String propertyName,
                                       final Object oldValue, 
                                       final Object newValue)
    {
        if (propListenersHash != null)
        {
            PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
            notifyPropListeners(propListenersHash.get(""), evt);
            notifyPropListeners(propListenersHash.get(propertyName), evt);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        addPropertyChangeListener("", l);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l)
    {
        if (propListenersHash == null)
        {
            propListenersHash = new Hashtable<String, Vector<PropertyChangeListener>>();
        }
        Vector<PropertyChangeListener> list = propListenersHash.get(propertyName);
        if (list == null)
        {
            list = new Vector<PropertyChangeListener>();
            propListenersHash.put(propertyName, list);
        }
        list.add(l);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        removePropertyChangeListener("", l);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l)
    {
        if (propListenersHash != null)
        {
            Vector<PropertyChangeListener> list = propListenersHash.get(propertyName);
            if (list != null)
            {
                list.remove(l);
            }
        }
    }
}
