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
package edu.ku.brc.ui;

import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.db.ViewBasedSearchDialogIFace;

/**
 * Interface to the factory for creating View (Form) Based Dialogs/Frame or Search dialogs.<br>
 * These  dialogs are special in that a single name references a detailed discription of what the dialog is to look like,
 * what form should be used, and whether it can be editable.<BR>BR>
 *
 * For the search dialogs, the definition contains information about how tro search both in JDBC (straigh SQL) or
 * by using Hibernate. Note: The applicaiton is currently set upto have just one factory and it must be "set" into
 * the UICacheManager before it can be used by everyone.<br><BR>
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
     * @param name the name of the DBObjSearchDialog to return
     * @return a DBObjSearchDialog by name
     */
    public ViewBasedSearchDialogIFace createSearchDialog(String name);

   /**
     * Creates a Frame/Dialog from the Factory by name.
     * @param name the Name of the display to create (the factory uses this name)
     * @param frameTitle the title on the frame or dialog
     * @param closeBtnTitle the title of close btn
     * @param isEdit whether it is a view or edit form
     * @param options the options needed for creating the form
     * @param type the type of frame (Frame or Dialog) model or non-model
     * @return the object (Frame) displaying the form
     */
    public ViewBasedDisplayIFace createDisplay(String      name,
                                               String      frameTitle,
                                               String      closeBtnTitle,
                                               boolean     isEdit,
                                               int         options,
                                               FRAME_TYPE  type);

}
