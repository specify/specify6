/* Filename:    $RCSfile: FormViewable.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.forms;

import java.awt.Component;
import java.util.Map;

import edu.ku.brc.specify.ui.forms.persist.FormView;
import edu.ku.brc.specify.ui.validation.FormValidator;

/**
 * Interface to encapsulate and object that can implment a UI component as a form
 * 
 * @author rods
 *
 */
public interface FormViewable
{

    /**
     * Returns the Id for the form
     * @return Returns the Id for the form
     */
    public int getId();
    
    /**
     * Returns the form's type (field, form, table)
     * @return
     */
    public FormView.ViewType getType();
    
    /**
     * Returns the Form's UI Component
     * @return Returns the Form's UI Component
     */
    public Component getUIComponent();
    
    /**
     * Returns whether it is a sub form or not
     * @return Returns whether it is a sub form or not
     */
    public boolean isSubform();
    
    /**
     * Returns a component by name
     * @param name the name of the component
     * @return the component
     */
    public Component getComp(final String name);

    /**
     * Returns the mapping of name to control
     * @return Returns the mapping of name to control
     */
    public Map<String, Component> getControlMapping();
    
    
    /**
     * Returns the validator for the form
     * @return Returns the validator for the form
     */
    public FormValidator getValidator();
    
    /**
     * Sets the Data Object into the form
     * @param dataObj the data
     */
    public void setDataObj(final Object dataObj);

    /**
     * Returns the data object for the form
     * @return Returns the data object for the form
     */
    public Object getDataObj();
    
    /**
     * Fill the form from the data obj
     */
    public void setDataIntoUI();
    
    /**
     * Get the data from the form and put into the data object
     */
    public void getDataFromUI();
    
    /**
     * Return the data from the UI control
     * @param name the name of the control
     * @return return the value or null if the control was not found.
     */
    public Object getDataFromUIComp(final String name);
    
    /**
     * Sets data into a single field
     * @param name the name of the control
     * @param data the data for the control
     */
    public void setDataIntoUIComp(final String name, Object data);


    /**
     * Returns a subform by name
     * @param name the name of the sub form to be returned
     * @return a subform (FormViewable)
     */
    public FormViewable getSubView(final String name);
}
