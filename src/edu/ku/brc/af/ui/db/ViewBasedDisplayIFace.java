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
package edu.ku.brc.af.ui.db;

import javax.swing.JButton;

import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;

/**
 * This interface enables both dialogs and frames to be created that displays information about a data object (usually a form).
 * The display dialog/frame is created with a single name from a factory that implementats the ViewBasedDialogFactoryIFace interface.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public interface ViewBasedDisplayIFace
{
    // Note: These values match both CustomDialog and CustomFrame
    public static final int OK_BTN             = 1;
    public static final int CANCEL_BTN         = 2;
    public static final int HELP_BTN           = 4;
    public static final int APPLY_BTN          = 8;

    /**
     * Shows the Frame or Dialog
     * @param show true - show, false hide
     */
    public abstract void showDisplay(boolean show);

    /**
     * Returns the MultiView
     * @return the multiview
     */
    public abstract MultiView getMultiView();

    /**
     * Set a listener to know when the Frame is closed
     * @param propertyChangeListener the listener
     */
    public abstract void setCloseListener(ViewBasedDisplayActionAdapter vbdaa);

    /**
     * Sets data into the dialog
     * @param dataObj the data object
     */
    public abstract void setData(final Object dataObj);
    
    /**
     * Sets parent data obect for the data object into the dialog, this is not required
     * and is used sometimes by dialog that want to pass the parent data object into the business rules.
     * @param parentDataObj the data object
     */
    public abstract void setParentData(final Object parentDataObj);
    
    /**
     * Returns whether the form is in edit mode or not
     * @return true in edit mode, false it is not
     */
    public abstract boolean isEditMode();

    /**
     * Tells the Display that it is being shutdown.
     */
    public abstract void shutdown();
    
    /**
     * Creates the UI before the call to showDisplay.
     */
    public void createUI();
    
    /**
     * @return the ok btn
     */
    public abstract JButton getOkBtn();
    
    /**
     * @return the cancel btn
     */
    public abstract JButton getCancelBtn();
    
    /**
     * @return help btn
     */
    public abstract JButton getHelpBtn();
    
    /**
     * @return apply btn
     */
    public abstract JButton getApplyBtn();
    
    /**
     * Disposes of native resources.
     */
    public abstract void dispose();
    
    /**
     * @return the integer code for which btn of the CustomDialog was pressed
     */
    public abstract int getBtnPressed();
    
    /**
     * @param helpContext
     */
    public abstract void setHelpContext(String helpContext);
    
    /**
     * @param session
     */
    public abstract void setSession(DataProviderSessionIFace session);
}
