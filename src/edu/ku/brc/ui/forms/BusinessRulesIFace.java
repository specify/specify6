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
package edu.ku.brc.ui.forms;

import java.util.List;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;


/**
 * This interface represents the actions that can be performed on a data object by a business rules object.<br>
 * NOTE: This cannot be turned into a generic because of how it is called.
 *
 * @code_status Beta
 * 
 * @author rods, jstewart
 *
 */
public interface BusinessRulesIFace
{

    /**
     * The status of the processing of the business rules.
     */
    public enum STATUS {None, OK, Warning, Error}
    
    /**
     * Enables the Business Rules to initialize itself with a Viewable.
     * The Viewable will be the same for the life of the BR.
     * @param viewable the Viewable the BR is attached to.
     */
    public abstract void initialize(Viewable viewable);
    
    /**
     * Notification a form is about to be filled in with data.
     * 
     * @param viewable the viewable that recieved the dataObject (using getDataOject would return the same object)
     */
    public abstract void beforeFormFill(Viewable viewable);
    
    /**
     * Notification a form was just filled with data.
     * 
     * @param dataObj the data object that went into the form
     * @param viewable the viewable that recieved the dataObject (using getDataOject would return the same object)
     */
    public abstract void afterFillForm(Object dataObj, Viewable viewable);
    
    /**
     * This enables a new data object to be populated with any children objects before being
     * set into a form. Some data object may require children to be added.
     * 
     * @param dataObj the new dataObject
     */
    public abstract void addChildrenToNewDataObjects(Object newDataObj);
    
    /**
     * Indicates whether a SubView should have data created for it.
     * @param fieldName the field name
     * @return true - create data
     */
    public abstract boolean shouldCreateSubViewData(String fieldName);
    
    /**
     * Processes the business rules for the data object.
     * 
     * @param dataObj the data object for rthe rules to be processed on.
     * @return the result status after processing the busniess rules.
     */
    public abstract STATUS processBusinessRules(Object dataObj);
    
    /**
     * Processes the business rules for the data object.
     * 
     * @param parentDataObj the parent data object for rthe rules to be processed on.
     * @param dataObj the data object for rthe rules to be processed on.
     * @return the result status after processing the busniess rules.
     */
    public abstract STATUS processBusinessRules(Object parentDataObj, Object dataObj);
    
    /**
     * Returns a list of warnings and errors after processing the business rules.
     * 
     * @return a list of warnings and errors after processing the business rules.
     */
    public abstract List<String> getWarningsAndErrors();
    
    /**
     * Asks if the object can be deleted.
     * 
     * @param dataObj the data object in question
     * @return true if it can be deleted, false if not
     */
    public abstract boolean okToEnableDelete(Object dataObj);
    
    /**
     * For some objects the check for deletion may need to hit the database.
     * This is called by a form after the user has said yes, but before anything else happens.
     * 
     * @param dataObj the object to be deleted
     * @param session the data provider session
     * @param deletable usually a FormViewObj, but it is really something that can perform the delete.
     * @return true if it can be deleted, false if not
     */
    public abstract void okToDelete(Object dataObj, DataProviderSessionIFace session, BusinessRulesOkDeleteIFace deletable);
    
    /**
     * Returns a message for the user describing what was deleted (intended to be a single line of text).
     * 
     * @param dataObj the data object that will be or has been deleted but still continas its values
     * @return the single line text string
     */
    public abstract String getDeleteMsg(Object dataObj);
    
    /**
     * Called BEFORE the merge with the database because the merge actually saves the obj.
     * created objects or existing data objects that have been editted.
     * 
     * @param dataObj the object to be saved
     * @param session the data provider session
     */
    public abstract void beforeMerge(Object dataObj, DataProviderSessionIFace session);
    
    /**
     * Called BEFORE saving an object to the DB.  This can be called on newly
     * created objects or existing data objects that have been editted.
     * 
     * @param dataObj the object to be saved
     * @param session the data provider session
     */
    public abstract void beforeSave(Object dataObj, DataProviderSessionIFace session);
    
    /**
     * Called BEFORE committing a transaction in which the passed in data object will
     * be saved to the DB.  When this is called, the transaction has already been started.
     * Any new DB actions will be added to the open transaction.  This can be called on
     * newly created objects or existing data objects that have been editted.
     * 
     * @param dataObj the object that is being saved
     * @param session the data provider session
     * @throws Exception when any unhandled exception occurs, in which case, the transaction should be aborted
     * @return true if the transaction should continue, false otherwise
     */
    public abstract boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session) throws Exception;
    
    /**
     * Called AFTER committing a transaction in which the passed in data object was
     * saved to the DB.  This can be called on newly created objects or existing data
     * objects that have been editted.
     * 
     * @param dataObj the object that was saved
     */
    public abstract boolean afterSaveCommit(Object dataObj);
    
    /**
     * Called BEFORE deleting an object from the DB.  This is called before the object is even
     * slated for deletion within the DB access code (e.g. Hibernate, etc).
     * 
     * @param dataObj the object to be deleted
     * @param session the data provider session
     */
    public abstract void beforeDelete(Object dataObj, DataProviderSessionIFace session);
    
    /**
     * Called BEFORE committing a transaction in which the passed in data object will
     * be deleted from the DB.
     * 
     * @param dataObj the object being deleted
     * @param session the data provider session
     * @throws Exception when any unhandled exception occurs, in which case, the transaction should be aborted
     * @return true if the transaction should continue, false otherwise
     */
    public abstract boolean beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session) throws Exception;
    
    /**
     * Called AFTER committing a transaction in which the passed in data object was
     * deleted from the DB.
     * 
     * @param dataObj the object that was deleted
     */
    public abstract void afterDeleteCommit(Object dataObj);
    
    /**
     * @param dataObj
     * @param draggableIcon
     */
    public abstract void setObjectIdentity(Object dataObj, DraggableRecordIdentifier draggableIcon);
    
    /**
     * Returns whether a the form should create a new object and pass it in. This new object
     * is usually a required parent for the the search object.
     * @return whether to create a new object
     */
    public abstract boolean doesSearchObjectRequireNewParent();
    
    /**
     * Asks the business rules to associate the optional new parent data object and the search object.
     * @param newParentDataObj the new parent object (is null if doesSearchObjectRequireNewParent return false)
     * @param dataObjectFromSearch the new object found from the search.
     * @return the dataObjectFromSearch
     */
    public abstract Object processSearchObject(Object newParentDataObj, Object dataObjectFromSearch);
    
    /**
     * During CarryForward some fields should be copied and some cloned depending on there context 
     * within the parent data object. For example, most many-to-one relationships will have the data 
     * object's reference copied so the new object reuses and points at the same object as the previous 
     * (Carry Forward source object). But in some circumstances the entire data object gets cloned.
     * 
     * @return true if field is to be cloned false if the value (or the value reference) is to be copied. 
     * (most of the time this returns false).
     */
    public abstract boolean shouldCloneField(final String fieldName);
    
    /**
     * The form is being tossed.
     */
    public abstract void formShutdown();
    
}
