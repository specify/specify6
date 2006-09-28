/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.datamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import edu.ku.brc.ui.forms.FormDataObjIFace;

public abstract class DataModelObjBase implements FormDataObjIFace
{
    protected Hashtable<String, Vector<PropertyChangeListener>> propListenersHash = null;
    protected PropertyChangeSupport changes = new PropertyChangeSupport(this);
    
    protected Date timestampCreated;
    protected Date timestampModified;
    protected String lastEditedBy;
    
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
        return this.timestampCreated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setTimestampCreated(java.util.Date)
     */
    public void setTimestampCreated(Date timestampCreated)
    {
        this.timestampCreated = timestampCreated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTimestampModified()
     */
    public Date getTimestampModified()
    {
        return this.timestampModified;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setTimestampModified(java.util.Date)
     */
    public void setTimestampModified(Date timestampModified)
    {
        this.timestampModified = timestampModified;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getLastEditedBy()
     */
    public String getLastEditedBy()
    {
        return this.lastEditedBy;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#setLastEditedBy(java.lang.String)
     */
    public void setLastEditedBy(String lastEditedBy)
    {
        this.lastEditedBy = lastEditedBy;
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

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        changes.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        changes.addPropertyChangeListener(propertyName, listener);
    }

    public void firePropertyChange(PropertyChangeEvent evt)
    {
        changes.firePropertyChange(evt);
    }

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue)
    {
        changes.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange(String propertyName, int oldValue, int newValue)
    {
        changes.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        changes.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        changes.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        changes.removePropertyChangeListener(propertyName, listener);
    }
}
