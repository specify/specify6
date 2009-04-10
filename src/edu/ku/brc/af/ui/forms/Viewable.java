/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.awt.Component;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewDef;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;

/**
 * A Viewable is typically a Form or a Table (not a JTable) of a data object or obects. Some of the methods are specifically for forms
 * and do not apply to tables.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public interface Viewable
{
    /**
     * Returns a unique name.
     * @return a unique name
     */
    public abstract String getName();
    
    /**
     * Returns the form's type (field, form, table).
     * @return the form's type (field, form, table)
     */
    public abstract ViewDef.ViewType getType();

    /**
     * Returns the Form's UI Component.
     * @return Returns the Form's UI Component
     */
    public abstract Component getUIComponent();

    /**
     * Returns whether it is a sub form or not.
     * @return Returns whether it is a sub form or not
     */
    public abstract boolean isSubform();

    /**
     * Returns a component by id for "labelfor" attr.
     * @param id the id of the component
     * @return the component
     */
    public abstract <T> T getCompById(final String id);

    /**
     * Returns a label by id.
     * @param id the id of the label
     * @return the label
     */
    public abstract JLabel getLabelFor(final String id);

    /**
     * Returns the mapping of name to control.
     * @return Returns the mapping of name to control
     */
    public abstract Map<String, Component> getControlMapping();

    /**
     * Returns the validator for the form.
     * @return Returns the validator for the form
     */
    public abstract FormValidator getValidator();

    /**
     * Sets the Data Object into the form.
     * @param dataObj the data
     */
    public abstract void setDataObj(final Object dataObj);

    /**
     * Returns the data object for the form.
     * @return Returns the data object for the form
     */
    public abstract Object getDataObj();
    
    /**
     * Sets in the RecordSet the form needs to traverse over.
     * @param recordSet the recordset of items
     */
    public abstract void setRecordSet(RecordSetIFace recordSet);

    /**
     * Sets the Parent Data Object into the Viewable. This is usually when the form will manage a list (Set)
     * of items that are "owned" in the Hibernate sense by a parent object. This is typically
     * a One-to-Many where the parent data object is the "One" and the List (Set) of objects is the "Many".
     * @param parentDataObj the parent data object
     */
    public abstract void setParentDataObj(Object parentDataObj);

    /**
     * Returns the parent data object for the form.
     * @return Returns the parent data object for the form
     */
    public abstract Object getParentDataObj();

    /**
     * Fill the form from the data obj.
     */
    public abstract void setDataIntoUI();

    /**
     * Get the data from the form and put into the data object.
     */
    public abstract void getDataFromUI();

    /**
     * Return the data from the UI control.
     * @param id the id of the control
     * @return return the value or null if the control was not found.
     */
    public abstract Object getDataFromUIComp(final String id);

    /**
     * Sets data into a single field.
     * @param name the name of the control
     * @param data the data for the control
     */
    public abstract void setDataIntoUIComp(final String name, Object data);

    /**
     * Returns a subform by name
     * @param name the name of the sub form to be returned
     * @return a subform (Viewable)
     */
    public abstract MultiView getSubView(final String name);

    /**
     * List the List with all the ids of the cells of type "field".
     * @param fieldIds the list to be filled
     */
    public abstract void getFieldIds(final List<String> fieldIds);

    /**
     * List the List with all the names of the cells of type "field".
     * @param fieldIds the list to be filled
     */
    public abstract void getFieldNames(final List<String> fieldNames);

    /**
     * Tells the object it is abut to be shown.
     * @param show whether it is being shown or hidden
     */
    public abstract void aboutToShow(boolean show);

    /**
     * Returns the View definition for this viewable.
     * @return the View definition for this viewable
     */
    public abstract ViewIFace getView();

    /**
     * Returns the ViewDef definition for this viewable.
     * @return the ViewDef definition for this viewable
     */
    public abstract ViewDefIFace getViewDef();

    /**
     * Returns the AltView definition for this viewable.
     * @return the AltView definition for this viewable
     */
    public abstract AltViewIFace getAltView();

    /**
     * Indicates it should hide the UI that enables switching between different AltViews;
     * this is used for children MultiView when there are only two of the same ViewDef differing only by edit mode or view mode.
     * @param hide true - hide, false show
     */
    public abstract void hideMultiViewSwitch(boolean hide);

    /**
     * Tell the viewable whether the validation was OK so it knows to update the UI appropriately; this usually means it should update an OK or save button.
     * @param wasOK whether validation was OK
     */
    public abstract void validationWasOK(boolean wasOK);
    
    /**
     * Sets the "cell" name of this subview which is the name of this control in the form, this doesn't
     * need to be called ("set") when it is a top-level form.
     * @param cellName the cell name
     */
    public abstract void setCellName(String cellName);
    
    /**
     * The class name that will be used to create new objects.
     * @param classToCreate the classToCreate to set
     */
    public abstract void setClassToCreate(Class<?> classToCreate);
    
    /**
     * Sets the current serssion into the Viewable.
     * @param session the current session
     */
    public abstract void setSession(DataProviderSessionIFace session);
    
    
    /**
     * Set whether the viewable has new data and should any UI accordingly.
     * @param hasNewData true- has new data, false - doesn't
     */
    public abstract void setHasNewData(boolean hasNewData);
    
    /**
     * Sets the Save Btn into the Viewable so it can enable and disABLE IT.
     * @param saveBtn THE SAVE BTN
     */
    public abstract void registerSaveBtn(JButton saveBtn);
    
    /**
     * Updates the enable/disable state of the save btn if it has one.
     */
    public abstract void updateSaveBtn();
    
    /**
     * @return the save btn if it has one.
     */
    public abstract JComponent getSaveComponent();
    
    /**
     * Checks to see if the current item has changed and asks if it should be saved.
     * @param throwAwayOnDiscard indicates whether to throw away or reload the data object on discard
     * @return true to continue false to stop
     */
    public abstract boolean isDataCompleteAndValid(boolean throwAwayOnDiscard);
    
    /**
     * After a new data object is created.
     * @param newDataObj the newly created object.
     */
    public abstract void setNewObject(FormDataObjIFace newDataObj);
    
    /**
     * @return
     */
    public abstract JComponent getControllerPanel();
    
    /**
     * Focus the appropriate UI element.
     */
    public abstract void focus();
    
    /**
     * Tells the Viewable to skip the DB attach when loading data.
     * @param isSkippingAttach true/false
     */
    public void setSkippingAttach(final boolean isSkippingAttach);

    /**
     * Call right before formShutdown is called, but the entire UI is still intact.
     */
    public abstract void aboutToShutdown();

    /**
     * Tells the Viewable that it is being shutdown and it should cleanup.
     * Sometimes this is called asynchronously by being dispatched onto the 
     * UI Event thread and sometimes not.
     */
    public abstract void shutdown();

}
