/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.Component;

import javax.swing.JButton;

import edu.ku.brc.af.ui.forms.ResultSetControllerListener;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.ui.dnd.GhostActionable;

public interface FormPaneWrapper extends GhostActionable, ResultSetControllerListener
{

    /**
     * Sets in a new Workbench and all the WBTMI have different object pointer because of the merge
     * that was done with the session. So we need to match up Record Ids and replace all the old WBTMIs
     * with the new ones.
     * @param workbench the new wb
     */
    public abstract void setWorkbench(final Workbench workbench);

    public abstract Component getPane();

    /**
     * @return the WorkbenchPaneSS
     */
    public abstract WorkbenchPaneSS getWorkbenchPane();

    /**
     * @return the controlPropsBtn
     */
    public abstract JButton getControlPropsBtn();

    /**
     * Clean up and listeners etc.
     */
    public abstract void cleanup();

    /**
     * Swaps out a TextField for a TextArea and vs.
     * @param inputPanel the InputPanel that hold the text component
     * @param fieldLen the length of the text field component (columns)
     */
    public abstract void swapTextFieldType(final InputPanel inputPanel, final short fieldLen);

    /**
     * Tells the form it is being hidden.
     * @param show true - show, false hide
     */
    public abstract void aboutToShowHide(final boolean show);

    /**
     * Tells the pane whether it is about to show or not when the parent pane is being shown or not.
     * @param show true show, false hide
     */
    public abstract void showingPane(final boolean show);

    /**
     * Copies the data from the form into the row.
     */
    public abstract void copyDataFromForm();

    /**
     * Updates validation status display for all controls.
     */
    public abstract void updateValidationUI();

}