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
package edu.ku.brc.af.ui;

import java.awt.Window;

import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;

/**
 * Interface to the factory for creating View (Form) Based Dialogs/Frame or Search dialogs.<br>
 * These  dialogs are special in that a single name references a detailed discription of what the dialog is to look like,
 * what form should be used, and whether it can be editable.<BR>BR>
 *
 * For the search dialogs, the definition contains information about how tro search both in JDBC (straigh SQL) or
 * by using Hibernate. Note: The applicaiton is currently set upto have just one factory and it must be "set" into
 * the UIRegistry before it can be used by everyone.<br><BR>
 *
 * There are not rules for who creates the factory or when the factory should created. Currently, the edu.ku.brc.ui.db
 * package contains refernce implementations of Dialogs/Frame created by the "createDisplay" methods and these can be
 * used as is. Anyone creating their own factory must create there own "createSearchDialog" implementation.
 *
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public interface ViewBasedDialogFactoryIFace
{
    public enum FRAME_TYPE {FRAME, DIALOG} // TODO may want to change this to model/non-model

    /**
     * Creates a new DBObjSearchDialog by name
     * @param window the parent frame
     * @param name the name of the DBObjSearchDialog to return
     * @return a DBObjSearchDialog by name
     */
    public abstract ViewBasedSearchDialogIFace createSearchDialog(Window window, String name);

   /**
     * Creates a Frame/Dialog from the Factory by name.
     * @param window the parent frame, can be null and is ignored type is FRAME.
     * @param name the Name of the display to create (the factory uses this name)
     * @param frameTitle the title on the frame or dialog
     * @param closeBtnTitle the title of close btn
     * @param isEdit whether it is a view or edit form
     * @param options the options needed for creating the form
     * @param type the type of frame (Frame or Dialog) model or non-model
     * @return the object (Frame) displaying the form
     */
    public abstract ViewBasedDisplayIFace createDisplay(Window      window, 
                                                        String      name,
                                                        String      frameTitle,
                                                        String      closeBtnTitle,
                                                        boolean     isEdit,
                                                        int         options,
                                                        String      helpContext,
                                                        FRAME_TYPE  type);

    /**
     * Returns the name of the search that the search dialog will use.
     * @param searchDlgName returns the name of the search that this named search dialog will use.
     * @return the name of the search (that defines the XML for the Query)
     */
    public abstract String getSearchName(String searchDlgName);
}
