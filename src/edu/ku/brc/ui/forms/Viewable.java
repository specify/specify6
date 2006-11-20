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

import java.awt.Component;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.validation.FormValidator;

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
    public String getName();
    
    /**
     * Returns the form's type (field, form, table).
     * @return the form's type (field, form, table)
     */
    public ViewDef.ViewType getType();

    /**
     * Returns the Form's UI Component.
     * @return Returns the Form's UI Component
     */
    public Component getUIComponent();

    /**
     * Returns whether it is a sub form or not.
     * @return Returns whether it is a sub form or not
     */
    public boolean isSubform();

    /**
     * Returns a component by id for "labelfor" attr.
     * @param id the id of the component
     * @return the component
     */
    public Component getCompById(final String id);

    /**
     * Returns a label by id.
     * @param id the id of the label
     * @return the label
     */
    public JLabel getLabelFor(final String id);

    /**
     * Returns the mapping of name to control.
     * @return Returns the mapping of name to control
     */
    public Map<String, Component> getControlMapping();

    /**
     * Returns the validator for the form.
     * @return Returns the validator for the form
     */
    public FormValidator getValidator();

    /**
     * Sets the Data Object into the form.
     * @param dataObj the data
     */
    public void setDataObj(final Object dataObj);

    /**
     * Returns the data object for the form.
     * @return Returns the data object for the form
     */
    public Object getDataObj();

    /**
     * Sets the Parent Data Object into the Viewable. This is usually when the form will manage a list (Set)
     * of items that are "owned" in the Hibernate sense by a parent object. This is typically
     * a One-to-Many where the parent data object is the "One" and the List (Set) of objects is the "Many".
     * @param parentDataObj the parent data object
     */
    public void setParentDataObj(Object parentDataObj);

    /**
     * Returns the parent data object for the form.
     * @return Returns the parent data object for the form
     */
    public Object getParentDataObj();

    /**
     * Fill the form from the data obj.
     */
    public void setDataIntoUI();

    /**
     * Get the data from the form and put into the data object.
     */
    public void getDataFromUI();

    /**
     * Return the data from the UI control.
     * @param id the id of the control
     * @return return the value or null if the control was not found.
     */
    public Object getDataFromUIComp(final String id);

    /**
     * Sets data into a single field.
     * @param name the name of the control
     * @param data the data for the control
     */
    public void setDataIntoUIComp(final String name, Object data);

    /**
     * Returns a subform by name
     * @param name the name of the sub form to be returned
     * @return a subform (Viewable)
     */
    public MultiView getSubView(final String name);

    /**
     * List the List with all the names of the cells of type "field".
     * @param fieldIds the list to be filled
     */
    public void getFieldIds(final List<String> fieldIds);

    /**
     * Tells the object it is abut to be shown.
     * @param show whether it is being shown or hidden
     */
    public void aboutToShow(boolean show);

    /**
     * Returns the View definition for this viewable.
     * @return the View definition for this viewable
     */
    public View getView();

    /**
     * Returns the ViewDef definition for this viewable.
     * @return the ViewDef definition for this viewable
     */
    public ViewDef getViewDef();

    /**
     * Returns the AltView definition for this viewable.
     * @return the AltView definition for this viewable
     */
    public AltView getAltView();

    /**
     * Indicates it should hide the UI that enables switching between different AltViews;
     * this is used for children MultiView when there are only two of the same ViewDef differing only by edit mode or view mode.
     * @param hide true - hide, false show
     */
    public void hideMultiViewSwitch(boolean hide);

    /**
     * Tell the viewable whether the validation was OK so it knows to update the UI appropriately; this usually means it should update an OK or save button.
     * @param wasOK whether validation was OK
     */
    public void validationWasOK(boolean wasOK);
    

    /**
     * Sets the "cell" name of this subview which is the name of this control in the form, this doesn't
     * need to be called ("set") when it is a top-level form.
     * @param cellName the cell name
     */
    public void setCellName(String cellName);
    
    /**
     * Sets the current serssion into the Viewable.
     * @param session the current session
     */
    public void setSession(DataProviderSessionIFace session);
    
    
    /**
     * Set whether the viewable has new data and should any UI accordingly.
     * @param hasNewData true- has new data, false - doesn't
     */
    public void setHasNewData(boolean hasNewData);
    
    public void registerSaveBtn(JButton saveBtn);

    /**
     * Tells the Viewable that it is being shutdown and it should cleanup.
     */
    public void shutdown();

}
