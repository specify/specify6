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

import javax.swing.JDialog;

/**
 * This interface represents a class that is used for searching for a data object. Objects implementing this interface 
 * are created by a factory imlpementing the ViewBasedDialogFactoryIFace interface. 
 * 
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public interface ViewBasedSearchDialogIFace
{
    /**
     * Returns the dialog object for this interface (usually returns 'this')
     * @return the dialog object for this interface (usually returns 'this')
     */
    public JDialog getDialog();
    
    /**
     * Returns whether the dialog was cancelled
     * @return whether the dialog was cancelled
     */
    public boolean isCancelled();
    
    /**
     * Return the selected object 
     * @return the selected object 
     */
    public Object getSelectedObject();
    

}
