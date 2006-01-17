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

public interface UIValidatable
{
    /**
     * @return Returns the isInError.
     */
    public boolean isInError();

    /**
     * @param isInError The isInError to set.
     */
    public void setInError(boolean isInError);

    /**
     * @return Returns the isRequired.
     */
    public boolean isRequired();

    /**
     * @param isRequired The isRequired to set.
     */
    public void setRequired(boolean isRequired);

}
