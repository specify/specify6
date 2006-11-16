/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.ui.forms;

import java.beans.PropertyChangeListener;
import java.util.Date;

/**
 * Interface that all Data Model class MUST implement to play nice in the form system.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 */
public interface FormDataObjIFace
{
    
    /**
     * Initialize the object (this is not called by Hibernate).
     */
    public abstract void initialize();
    
    /**
     * The Record Id.
     * @return Record Id.
     */
    public Long getId();
    
    /**
     * Return a String that uniquely identifies this object, usually this is a String field in the object (This should never return null).
     * @return String that uniquely identifies this object, usually this is a String field in the object.
     */
    public String getIdentityTitle();
    
    /**
     * Return the Timestamp Created.
     * @return the Timestamp Created
     */
    public Date getTimestampCreated();

    /**
     * Sets the Timestamp Created.
     * @param timestampCreated
     */
    public void setTimestampCreated(Date timestampCreated);

    /**
     * Returns the Timestamp Modified.
     * @return the Timestamp Modified.
     */
    public Date getTimestampModified();

    /**
     * Sets the Timestamp Modified.
     * @param timestampModified the new timestamp 
     */
    public void setTimestampModified(Date timestampModified);
    
    /**
     * Returns lastEditedBy.
     * @return lastEditedBy
     */
    public String getLastEditedBy();

    /**
     * Sets lastEditedBy.
     * @param lastEditedBy the text string usually the user name
     */
    public void setLastEditedBy(String lastEditedBy);
    
    /**
     * Add a new foreign key reference to this object.  This method provides
     * a generic way to call other methods such as addAgent(Agent a) or
     * addLocality(Locality l).
     * 
     * @param ref the new foreign key record
     * @param refName a String indicating the relationship to which the reference should be added
     */
    public void addReference(FormDataObjIFace ref, String refName);
    
    /**
     * Removes a foreign key reference to this object.  This method provides
     * a generic way to call other methods such as removeAgent(Agent a) or
     * removeLocality(Locality l).
     * 
     * @param ref the foreign key record to detach from this object
     * @param refName a String indicating which relationship from which to remove the reference
     */
    public void removeReference(FormDataObjIFace ref, String refName);
    
    /**
     * Gets the value of a foreign key reference for this object.  This method provides
     * a generic way to call other methods such as getAgent() or getLocalities().
     * 
     * @param ref the foreign key record to grab the value of
     * @return the value of that foreign key record
     */
    public Object getReferenceValue(String ref);
    
    public void onSave();
    public void onDelete();
    public void onUpdate();
    
    /**
     * Returns the internal Table Id.
     * @return the internal Table Id
     */
    public Integer getTableId();
    
    //---------------------------------------------------------------------------
    // Property Change Support
    //---------------------------------------------------------------------------
    
    /**
     * Adds a property change listener for any property (all properties).
     * @param l the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Adds a property change listener.
     * @param propertyName
     * @param l the listener
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l);
    
    /**
     * Removes a property change listener for any property (all properties).
     * @param l the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Removes a property change listener.
     * @param propertyName the property name
     * @param l the listener
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l);
    
}
