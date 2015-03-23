/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms.validation;

import java.awt.Component;
import java.io.File;

import edu.ku.brc.af.ui.BrowseBtnPanel;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 8, 2007
 *
 */
public class ValBrowseBtnPanel extends BrowseBtnPanel implements UIValidatable
{
    protected ValTextField textField;
    protected ErrorType    errorStatus = ErrorType.Valid;
    
    
    /**
     * @param textField
     * @param doDirsOnly
     * @param isForInput
     */
    public ValBrowseBtnPanel(final ValTextField textField, final boolean doDirsOnly, final boolean isForInput)
    {
        super(textField, doDirsOnly, isForInput);
        
        this.textField = textField;
        
        setValidatingFile(true);
    }
    
    /**
     * @return
     */
    public ValTextField getValTextField()
    {
        return textField;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    @Override
    public void setEnabled(final boolean enabled)
    {
        // Don't call super
        browseBtn.setEnabled(enabled);
        textField.setEnabled(enabled);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#requestFocus()
     */
    @Override
    public void requestFocus()
    {
        //browseBtn.requestFocus();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {
        textField.cleanUp();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return textField.getState().ordinal() > errorStatus.ordinal() ? textField.getState() : errorStatus;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return textField.isChanged();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        //System.out.println("isInError: "+textField.isInError());
        return getState() != ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        //System.out.println("textField.isRequired(): "+textField.isRequired());
        return textField.isRequired();
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        return !textField.getText().isEmpty();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    public void reset()
    {
        textField.reset();
        errorStatus = ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    public void setAsNew(final boolean isNew)
    {
        textField.setAsNew(isNew);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(final boolean isChanged)
    {
        textField.setChanged(isChanged);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(final boolean isRequired)
    {
        textField.setRequired(isRequired);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setState(edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType)
     */
    public void setState(final ErrorType state)
    {
        textField.setState(state); 
        errorStatus = state;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#validateState()
     */
    public ErrorType validateState()
    {
        ErrorType err = textField.validateState();
        if (isValidatingFile && err == ErrorType.Valid)
        {
            File file = new File(textField.getText());
            isValidFile = file.isFile() && file.exists();
        }
        
        ErrorType fileError = isValidatingFile && !isValidFile ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid;
        
        return errorStatus = err.ordinal() > fileError.ordinal() ? err : fileError;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    public String getReason()
    {
        return null;
    }

}
