/* Copyright (C) 2009, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIHelper.setControlSize;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIHelper;

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
    protected static ColorWrapper     requiredFieldColor = null;

    protected UIValidatable.ErrorType valState     = UIValidatable.ErrorType.Valid;
    protected boolean                 isRequired   = false;
    protected boolean                 isReadOnly   = false;
    protected boolean                 isChanged    = false;
    protected boolean                 isNew        = false;
    protected boolean                 currentValue = false;
    protected Color                   bgColor      = null;
    protected JTextField              textField    = null;

    protected int                     minValue = -1;
    protected int                     maxValue = -1;
    
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
        super(new SpinnerNumberModel(minVal, // initial value
                                     minVal, // min
                                     maxVal, // max
                                     1));    // step
        this.isRequired = isRequired;
        this.isReadOnly = isReadOnly;
        this.minValue   = minVal;
        this.maxValue   = maxVal;
        
        init();
        
        textField = getTextField(this);
        if (textField != null)
        {
            bgColor = textField.getBackground(); 
        }
        
        if (this.isRequired)
        {
            fixBGOfJSpinner();
        }
    }
    
    /**
     * Sets a new Model with a new Min,Max range.
     * @param min the min value 
     * @param max the max value
     * @param val the initial value
     */
    public void setRange(final int minVal, 
                         final int maxVal,
                         final int val)
    {
        setModel(new SpinnerNumberModel(val, // initial value
                minVal, // min
                maxVal, // max
                1));
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        if (textField != null)
        {
            if (requiredFieldColor != null)
            {
                textField.setBackground(this.isRequired && enabled ? requiredFieldColor.getColor() : bgColor);
            }
        }
        super.setEnabled(enabled);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        return getValue() != null;
    }

    /**
     * Initializes the control.
     */
    protected void init()
    {
        setControlSize(this);

        if (requiredFieldColor == null)
        {
            requiredFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
        
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                isChanged = true;
            }
        });
    }
    
    /**
     * Sets the spinner to the proper color.
     */
    protected void fixBGOfJSpinner()
    {
        if (textField != null)
        {
            if (requiredFieldColor != null)
            {
                textField.setBackground(requiredFieldColor.getColor());
            } 
        }
    }
    
    /**
     * Sets the spinner to the proper color.
     */
    protected JTextField getTextField(final Container container)
    {
        for (int i=0;i<container.getComponentCount();i++)
        {
            Component c = container.getComponent(i);
            if (c instanceof JTextField)
            {
                return (JTextField)c;
                
            } else if (c instanceof Container)
            {
                JTextField tf = getTextField((Container)c);
                if (tf != null)
                {
                    return tf;
                }
            }
        }
        return null;
    }
    
    /**
     * @return returns the value as an Integer (via a cast).
     */
    public Integer getIntValue()
    {
        return (Integer)getValue();
    }

    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------

    /**
     * @return the minValue
     */
    public int getMinValue()
    {
        return minValue;
    }

    /**
     * @return the maxValue
     */
    public int getMaxValue()
    {
        return maxValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setState(edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType)
     */
    public void setState(ErrorType state)
    {
        this.valState = state;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setAsNew(boolean)
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
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    public void reset()
    {
        valState = UIValidatable.ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {
        UIHelper.removeFocusListeners(this);
        UIHelper.removeKeyListeners(this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    public String getReason()
    {
        return null;
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
            this.setValue(Integer.parseInt(defaultValue));
            
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
