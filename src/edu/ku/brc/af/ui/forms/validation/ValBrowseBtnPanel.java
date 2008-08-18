/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.af.ui.forms.validation;

import java.awt.Component;

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
        return textField.getState();
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
        return textField.isInError();
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
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    public void reset()
    {
        textField.reset();
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
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#validateState()
     */
    public ErrorType validateState()
    {
        ErrorType err       = textField.validateState();
        ErrorType fileError = isValidatingFile && isValidFile ? UIValidatable.ErrorType.Error : UIValidatable.ErrorType.Valid;
        
        return err.ordinal() > fileError.ordinal() ? err : fileError;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    public String getReason()
    {
        return null;
    }

}
