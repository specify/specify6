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
package edu.ku.brc.ui.db;

import edu.ku.brc.ui.forms.MultiView;

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
     * Returns whether the form is in edit mode or not
     * @return true in edit mode, false it is not
     */
    public abstract boolean isEditMode();

    /**
     * Tells the Display that it is being shutdown.
     */
    public abstract void shutdown();
    
    /**
     * Disposes of native resources.
     */
    public abstract void dispose();
    
    /**
     * @return the integer code for which btn of the CustomDialog was pressed
     */
    public abstract int getBtnPressed();
}
