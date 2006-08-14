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

package edu.ku.brc.ui.validation;

import java.awt.Component;

/**
 * Interface for validatable components.
 *
 * @code_status Unknown (auto-generated)
 *
 * @author rods
 *
 */
public interface UIValidatable
{
    public enum ErrorType {Valid, Incomplete, Error}

    /**
     * Returns the isInError.
     * @return the isInError.
     */
    public boolean isInError();


    /**
     * Returns the validation state.
     * @return the validation state
     */
    public ErrorType getState();

    /**
     * Sets the validation state
     * @param state The isInError to set.
     */
    public void setState(ErrorType state);

    /**
     * Rests the state of the control to "empty" or what ever that means for the control
     */
    public void reset();

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
     * Returns whether it has changed.
     * @return whether it has changed
     */
    public boolean isChanged();

    /**
     * Sets whether it has changed.
     * @param isChanged whether it has changed.
     */
    public void setChanged(boolean isChanged);

    /**
     * Tells a control that it is new and not to validate until it has received focus.
     * @param isNew true it's new, false it is not
     */
    public void setAsNew(boolean isNew);


    /**
     * Asks it to vaidate itself.
     * @return the result of the validation
     */
    public ErrorType validateState();


    /**
     * Returns the actual Component that is validatable, some UI components are composites or may be wrapped in a JPanel
     * so this is the actual control the user interact with
     * @return the actual Component being validated
     */
    public Component getValidatableUIComp();

    /**
     * Tells it clean up, meaning unregistering listeners etc.
     */
    public void cleanUp();
}
