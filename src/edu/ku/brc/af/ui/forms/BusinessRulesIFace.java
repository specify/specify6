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
package edu.ku.brc.af.ui.forms;

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
     */
    public abstract void beforeFormFill();
    
    /**
     * Notification a form was just filled with data.
     * 
     * @param dataObj the data object that went into the form
     */
    public abstract void afterFillForm(Object dataObj);
    
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
     * @param dataObj the data object for the rules to be processed on.
     * @return the result status after processing the busniess rules.
     */
    public abstract STATUS processBusinessRules(Object dataObj);
    
    /**
     * Processes the business rules for the data object.
     * 
     * @param parentDataObj the parent data object for the rules to be processed on.
     * @param dataObj the data object for the rules to be processed on.
     * @param isExistingObject true means it was being edited, false means it was a new object
     * @return the result status after processing the business rules.
     */
    public abstract STATUS processBusinessRules(Object parentDataObj, Object dataObj, boolean isExistingObject);
    
    /**
     * Returns a list of warnings and errors after processing the business rules.
     * 
     * @return a list of warnings and errors after processing the business rules.
     */
    public abstract List<String> getWarningsAndErrors();
    
    /**
     * @return all the warning and error messages as a string
     */
    public abstract String getMessagesAsString();
    
    /**
     * Asks if the object can be deleted.
     * 
     * @param dataObj the data object in question
     * @return true if it can be deleted, false if not
     */
    public abstract boolean okToEnableDelete(Object dataObj);
    
    /**
     * This is a last second check where the business rules can determine whether it can be saved
     * or not. This is because some state within the database may have changed. This is called synchronously
     * so it must be fast. If it returns false then getWarningsAndErrors or getMessagesAsString can be called
     * to get the reason.
     * 
     * @param dataObj the object to be saved
     * @param session the data provider session
     * @return true if it can be deleted, false if not
     */
    public abstract boolean isOkToSave(Object dataObj, DataProviderSessionIFace session);

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
     * @param dataObj the data object that will be or has been deleted but still contains its values
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
     * @param session the data provider session (it might be null)
     * 
     */
    public abstract boolean afterSaveCommit(Object dataObj, DataProviderSessionIFace session);
    
    /**
     * Called AFTER a failed attempt to save the data object.  This can be called on newly created objects or existing data
     * objects that have been edited. 
     * 
     * @param dataObj the object that was not saved
     * @param session the data provider session (it might be null)
     * 
     */
    public abstract void afterSaveFailure(Object dataObj, DataProviderSessionIFace session);
    
    /**
     * Called after a successful save has been committed and all other form actions related to the save 
     * have been performed.
     * @param dataObj
     */
    public abstract void saveFinalization(Object dataObj);
    
    /**
     * Called BEFORE deleting an object from the DB.  This is called before the object is even
     * slated for deletion within the DB access code (e.g. Hibernate, etc).
     * 
     * @param dataObj the object to be deleted
     * @param session the data provider session
     * @return TODO
     */
    public abstract Object beforeDelete(Object dataObj, DataProviderSessionIFace session);
    
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
     * Check to see if it is OK to add the search object to the parent. Some rule need to check for duplicates.
     * @param newParentDataObj the parent data object
     * @param dataObjectFromSearch the object being added.
     * @return true if it can be added. (the default is true);
     */
    public abstract boolean isOkToAssociateSearchObject(Object newParentDataObj, Object dataObjectFromSearch);
    
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
     * @return true if the business rules is responsible for creating the new object for the viewable
     */
    public abstract boolean canCreateNewDataObject();
   
    /**
     * Creates a new data object for the Viewable and call {@link Viewable#setNewObject(FormDataObjIFace, boolean, Object)}
     * @param doSetIntoAndValidateArg indicates whether the data should be set into the forma and validated.
     * @param oldDataObj the previous data object in the form.
     * @return the new data object or null if there is an error
     */
    public abstract void createNewObj(boolean doSetIntoAndValidateArg, Object oldDataObj);
    
    /**
     * Called after the object is created, the parent is set and the CarryForward Values have been set
     * Provides an opportunity for the BR to set values before the form is filled in. Handy for 
     * setting initial values for creation instead of when the form is filled with an already existing object.
     * 
     * @param newDataObj the newly created object
     */
    public abstract void afterCreateNewObj(Object newDataObj);
    
    /**
     * @param parentObj
     * @return
     */
    public abstract boolean isOkToAddSibling(Object parentObj);
    
    /**
     * Called before the objects in the cached HashSet of objects are processed in a loop.
     */
    public abstract void startProcessingBeforeAfterRules();
    
    /**
     * Called after the objects in the cached HashSet of objects are processed in a loop.
     */
    public abstract void endProcessingBeforeAfterRules();
    
    
    /**
     * Call right before formShutdown is called, but the entire UI is still intact.
     */
    public abstract void aboutToShutdown();
    
    /**
     * The form is being tossed.
     */
    public abstract void formShutdown();
    
}
