/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.sql.Timestamp;

import edu.ku.brc.specify.datamodel.Agent;

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
    public abstract Integer getId();
    
    /**
     * Return a String that uniquely identifies this object, usually this is a String field in the object (This should never return null).
     * @return String that uniquely identifies this object, usually this is a String field in the object.
     */
    public abstract String getIdentityTitle();
    
    /**
     * Return the Timestamp Created.
     * @return the Timestamp Created
     */
    public abstract Timestamp getTimestampCreated();

    /**
     * Sets the Timestamp Created.
     * @param timestampCreated
     */
    public abstract void setTimestampCreated(Timestamp timestampCreated);

    /**
     * Returns the Timestamp Modified.
     * @return the Timestamp Modified.
     */
    public abstract Timestamp getTimestampModified();

    /**
     * Sets the Timestamp Modified.
     * @param timestampModified the new timestamp 
     */
    public abstract void setTimestampModified(Timestamp timestampModified);
    
    /**
     * Returns modifiedByAgent.
     * @return modifiedByAgent
     */
    public abstract Agent getModifiedByAgent();

    /**
     * Sets modifiedByAgent.
     * @param modifiedByAgent the agent who changed the data object
     */
    public abstract void setModifiedByAgent(Agent modifiedByAgent);
    
    /**
     * Sets modifiedByAgent.
     * @param modifiedByAgent the agent who changed the data object
     */
    public abstract void setCreatedByAgent(Agent createdByAgent);
    
    /**
     * Add a new foreign key reference to this object.  This method provides
     * a generic way to call other methods such as addAgent(Agent a) or
     * addLocality(Locality l).
     * 
     * @param ref the new foreign key record
     * @param fieldName a String indicating the relationship to which the reference should be added
     */
    public abstract void addReference(FormDataObjIFace ref, String fieldName);
    
    /**
     * @param ref the new foreign key record
     * @param fieldName a String indicating the relationship to which the reference should be added
     * @param doOtherSide should add the reverse side of the linkage
     */
    public abstract void addReference(FormDataObjIFace ref, String fieldName, boolean doOtherSide);
    
    /**
     * Removes a foreign key reference to this object.  This method provides
     * a generic way to call other methods such as removeAgent(Agent a) or
     * removeLocality(Locality l).
     * 
     * @param ref the foreign key record to detach from this object
     * @param fieldName a String indicating which relationship from which to remove the reference
     */
    public abstract void removeReference(FormDataObjIFace ref, String fieldName);
    
    /**
     * @param ref the foreign key record to detach from this object
     * @param fieldName a String indicating which relationship from which to remove the reference
     * @param doOtherSide should remove the reverse side of the linkage
     */
    public abstract void removeReference(FormDataObjIFace ref, String fieldName, boolean doOtherSide);
    
    /**
     * Gets the value of a foreign key reference for this object.  This method provides
     * a generic way to call other methods such as getAgent() or getLocalities().
     * 
     * @param ref the foreign key record to grab the value of
     * @return the value of that foreign key record
     */
    public abstract Object getReferenceValue(String ref);
    
    public abstract void onSave();
    public abstract void onDelete();
    public abstract void onUpdate();
    
    /**
     * Returns the internal Table Id.
     * @return the internal Table Id
     */
    public abstract int getTableId();
    
    /**
     * @return the version number of the record or null if it does apply.
     */
    public abstract Integer getVersion();
    
    /**
     * Returns whether the viewing of this class is restrictable
     * @return - boolean whether the viewing of this class is restrictable
     */
    public abstract boolean isRestrictable();
    
    /**
     * Returns the actual class object befoire being wrapped by ORM tools.
     * @return the actual class object befoire being wrapped by ORM tools.
     */
    public abstract Class<?> getDataClass();
    
    /**
     * Indicates whether this object should be indexed by the Express Search Indexer.
     * 
     * @return true - index, false - don't
     */
    public abstract boolean isChangeNotifier();
    
    /**
     * Asks the object to force load any children object while in the context of a session.
     */
    public abstract void forceLoad();
    
    /**
     * 
     * @param getter
     * @return 0 if set members should not be forceLoaded
     * 	       -1 if all set members should be forceLoaded
     * 			n if at most n set members should be forceLoaded  
     */
    public abstract int shouldForceLoadChildSet(Method getter);
    
    /**
     * @return
     */
    public abstract Object clone() throws CloneNotSupportedException;
    
    //---------------------------------------------------------------------------
    // Audit Support Support
    //---------------------------------------------------------------------------

    /**
     * @return The parent data object's table id at the time the object is inserted, updated, or deleted.
     */
    public abstract Integer getParentTableId();
    
    /**
     * NOTE: This MUST be called before calling 'getParentTableId' this method sets the table table number.
     * @return The parent data object's primary record id at the time the object is inserted, updated, or deleted.
     */
    public abstract Integer getParentId();
    
    //---------------------------------------------------------------------------
    // Property Change Support
    //---------------------------------------------------------------------------
    
    /**
     * Adds a property change listener for any property (all properties).
     * @param l the listener
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Adds a property change listener.
     * @param propertyName
     * @param l the listener
     */
    public abstract void addPropertyChangeListener(String propertyName, PropertyChangeListener l);
    
    /**
     * Removes a property change listener for any property (all properties).
     * @param l the listener
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Removes a property change listener.
     * @param propertyName the property name
     * @param l the listener
     */
    public abstract void removePropertyChangeListener(String propertyName, PropertyChangeListener l);
    
}
