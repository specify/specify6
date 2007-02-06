/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.validation;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 27, 2007
 *
 */
public class ValSpinner extends JSpinner implements UIValidatable, GetSetValueIFace
{
    protected static ColorWrapper     requiredfieldcolor = null;

    protected UIValidatable.ErrorType valState     = UIValidatable.ErrorType.Valid;
    protected boolean                 isRequired   = false;
    protected boolean                 isReadOnly   = false;
    protected boolean                 isChanged    = false;
    protected boolean                 isNew        = false;
    protected boolean                 currentValue = false;
    
    /**
     * 
     */
    public ValSpinner()
    {
        init();
    }

    /**
     * @param arg0
     */
    public ValSpinner(SpinnerModel arg0)
    {
        super(arg0);
        init();
    }

    /**
     * Constructor.
     * @param minVal the min value
     * @param maxVal the max value
     * @param isRequired is required
     * @param isReadOnly is read only
     */
    public ValSpinner(final int minVal, final int maxVal, final boolean isRequired, final boolean isReadOnly)
    {
        super(new SpinnerNumberModel(0,      //initial value
                                     minVal, //min
                                     maxVal, //max
                                     1));    //step
        this.isRequired = isRequired;
        this.isReadOnly = isReadOnly;
        
        init();
        
        if (this.isRequired)
        {
            fixBGOfJSpinner(this);
        }
    }
    
    /**
     * Inits the control.
     */
    protected void init()
    {
        //bgColor = getBackground();
        if (requiredfieldcolor == null)
        {
            requiredfieldcolor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
    }
    
    /**
     * Sets the spinner to the proper color.
     */
    protected void fixBGOfJSpinner(Container container)
    {
        for (int i=0;i<container.getComponentCount();i++)
        {
            Component c = container.getComponent(i);
            if (c instanceof JTextField)
            {
                c.setBackground(requiredfieldcolor.getColor());
                
            } else if (c instanceof Container)
            {
                fixBGOfJSpinner((Container)c);
            }
        }
    }

    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#setState(edu.ku.brc.ui.validation.UIValidatable.ErrorType)
     */
    public void setState(ErrorType state)
    {
        this.valState = state;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#setAsNew(boolean)
     */
    public void setAsNew(boolean isNew)
    {
        this.isNew = isRequired ? isNew : false;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#validate()
     */
    public UIValidatable.ErrorType validateState()
    {
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#reset()
     */
    public void reset()
    {
        valState = UIValidatable.ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {
        // no op
    }

    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        if (value == null && StringUtils.isNotEmpty(defaultValue))
        {
            this.setValue(Integer.parseInt((String)defaultValue));
            
        } else if (value instanceof Integer || value instanceof Long)
        {
            this.setValue(value);
            
        } else if (value instanceof String)
        {
            this.setValue(Integer.parseInt((String)value));
            
        } else
        {
            this.setValue(0);
        }
    }

    // JSpinner implments GetValue
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    //public Object getValue()
    //{
    //    return this.getValue();
    //}
}
