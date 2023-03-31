/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import edu.ku.brc.af.ui.forms.persist.FormCellIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellLabel;
import edu.ku.brc.af.ui.forms.persist.FormCellSubView;

/**
 * This interface enables class to accept the UI components to be layed out by it's internal
 * layout mechanism.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 */
public interface ViewBuilderIFace
{
    /**
     * Adds a control by name so it can be looked up later.
     * @param formCell the FormCell def that describe the cell
     * @param label the the label to be added
     */
    public abstract void addLabel(FormCellLabel formCell, JLabel label);
    
    /**
     * Adds a control by name so it can be looked up later. This registers the component in such a way that
     * data can be "mapped" into the control.
     * @param formCell the FormCell def that describe the cell
     * @param control the control to be added
     */    
    public abstract void registerControl(FormCellIFace formCell, Component control);
    
    /**
     * Adds a control to the UI (usually it is being added to a container).
     * @param control the control to be added
     * @param colInx the column placement in the row
     * @param rowInx the row it is to be placed on
     * @param colSpan the number of columns to span
     * @param rowSpan the number of rows to span
     */
    public abstract void addControlToUI(Component control, int colInx, int rowInx, int colSpan, int rowSpan);
    
    /**
     * Registers a plugin into the builder.
     * @param formCell the formCell of the UIPlugin
     * @param uip the plugin
     */
    public abstract void registerPlugin(FormCellIFace formCell, UIPluginable uip);
    
    /**
     * Creates a separator (this is usually some text with a line after it).
     * @param title the title text
     * @return the separator component (usually a container)
     */
    public abstract Component createSeparator(String title);
    
    /**
     * Creates a special RecordIndentifier UI component with a name and icon. It is intended that
     * this component is dragganle in some way and it represents a unique fiueld about the object it represents.
     * @param title the title (sometimes used as  patr of a separator)
     * @param icon the icon, which is usually on the left hand side.
     * @return the component (which is typically a JPanel)
     */
    public abstract JComponent createRecordIndentifier(String title, ImageIcon icon);
    
    /**
     * Adds a control by name so it can be looked up later.
     * @param formCell the FormCell def that describe the cell
     * @param subView the subView
     * @param colInx the column placement in the row
     * @param rowInx the row it is to be placed on
     * @param colSpan the number of columns to span
     * @param rowSpan the number of rows to span
     */
    public abstract void addSubView(FormCellSubView formCell, MultiView subView, int colInx, int rowInx, int colSpan, int rowSpan);
    
    /**
     * Tells it the FormCellSubView is being closed.
     * @param formCell the formcel being closed.
     */
    public abstract void closeSubView(FormCellSubView formCell);
    
    /**
     * Indicates whether the calling should assume it support nested layout for whether this
     * object will try to "flatten" it. For example, forms that cantain subforms are hierarchical, if 
     * the Form layout was passed into a table to be displayed then the table would request that it be flatten.
     * @return true flatten, false hierarchical
     */
    public abstract boolean shouldFlatten();

    /**
     * Returns a Component by name from the Form.
     * @param name the name of the field according to the XML definition
     * @return the component or null
     */
    public Component getControlByName(final String name);
    
    /**
     * Returns a Component by name from the Form.
     * @param name the id of the field according to the XML definition
     * @return the component or null
     */
    public abstract Component getControlById(String id);
    
    /**
     * 
     */
    public abstract void fixUpRequiredDerivedLabels();
    
    /**
     * @return returns whether there were required fields or not in the view.
     */
    public abstract boolean hasRequiredFields();
    
    /**
     * Provide an opportunity to tell the controls that the form is now complete (i.e. like UIPlugins)
     */
    public abstract void doneBuilding();
    
}
