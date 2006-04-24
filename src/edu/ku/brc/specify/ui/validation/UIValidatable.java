/* Filename:    $RCSfile: UIValidatable.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/16 19:59:54 $
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

package edu.ku.brc.specify.ui.validation;

/**
 * Interface for validatable components.
 * 
 * @author rods
 *
 */
public interface UIValidatable
{
    /**
     * Returns the isInError.
     * @return Returns the isInError.
     */
    public boolean isInError();

    /**
     * Sets whether it is in error.
     * @param isInError The isInError to set.
     */
    public void setInError(boolean isInError);

    /**
     * Returns the isRequired.
     * @return Returns the isRequired.
     */
    public boolean isRequired();

    /**
     * Sets whether it is required
     * @param isRequired The isRequired to set.
     */
    public void setRequired(boolean isRequired);

    /**
     * Returns whether it has changed
     * @return whether it has changed
     */
    public boolean isChanged();

    /**
     * Sets whether it has changed
     * @param isChanged whether it has changed.
     */
    public void setChanged(boolean isChanged);
    
    /**
     * Tells a control that it is new and not to validate until
     * it has received focus
     * @param isNew true it's new, false it is not
     */
    public void setAsNew(boolean isNew);
}
