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

import java.beans.PropertyChangeListener;

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
    /**
     * Shows the Frame or Dialog
     * @param show true - show, false hide
     */
    public void showDisplay(boolean show);

    /**
     * Returns the MultiView
     * @return the multiview
     */
    public MultiView getMultiView();

    /**
     * Set a listener to know when the Frame is closed
     * @param propertyChangeListener the listener
     */
    public void setCloseListener(final PropertyChangeListener propertyChangeListener);

    /**
     * Sets data into the dialog
     * @param dataObj the data object
     */
    public void setData(final Object dataObj);
    
    /**
     * Returns whether the form is in edit mode or not
     * @return true in edit mode, false it is not
     */
    public boolean isEditMode();

    /**
     * Tells the Display that it is being shutdown
     */
    public void shutdown();

}
