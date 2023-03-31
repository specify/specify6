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
package edu.ku.brc.af.ui.forms.validation;

import static edu.ku.brc.ui.UIHelper.setControlSize;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIHelper;

/**
 * Wrapped JCheckBox that can accept Boolean, String, Short, Integer, Long values. String can be mixed case and
 * it checks against "true". For numeric values zero is false and anything else is true. This control cannot be in error.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Dec 21, 2006
 *
 */
public class ValCheckBox extends JCheckBox implements UIValidatable, GetSetValueIFace
{
    private static final Logger log = Logger.getLogger(ValCheckBox.class);
            
    protected UIValidatable.ErrorType valState     = UIValidatable.ErrorType.Valid;
    protected boolean                 isRequired   = false;
    protected boolean                 isReadOnly   = false;
    protected boolean                 isChanged    = false;
    protected boolean                 isNew        = false;
    protected Boolean                 currentValue = null;
    
    /**
     * Constructs a validated checkbox.
     * @param label the label string
     * @param isRequired dones't mean much for this control
     * @param isReadOnly if it is read only (user click always get reset back to oringal value
     */
    public ValCheckBox(final String label, final boolean isRequired, final boolean isReadOnly)
    {
        super(label);
        this.isRequired = isRequired;
        this.isReadOnly = isReadOnly;
        
        setOpaque(false);
        setControlSize(this);
        
        if (isReadOnly)
        {
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    setSelected(currentValue != null ? currentValue : false);
                }
            });
        } else
        {
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    isChanged = true;
                }
            });
            
        }
    }

    /**
     * @param currentValue
     */
    public void setCurrentValue(Boolean currentValue)
    {
        this.currentValue = currentValue;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------


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
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        return true;
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
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(final Object value, String defaultValue)
    {
        currentValue = null;
        
        if (value != null)
        {
            if (value instanceof Boolean)
            {
                currentValue = ((Boolean)value).booleanValue();
                 
            } else if (value instanceof String)
            {
                if (StringUtils.isNotEmpty(value.toString()))
                {
                    currentValue = ((String)value).toLowerCase().equals("true");
                }
                 
            } else if (value instanceof Integer)
            {
                currentValue = ((Integer)value).intValue() != 0;
                 
            } else if (value instanceof Short)
            {
                currentValue = ((Short)value).shortValue() != 0;
                 
            } else if (value instanceof Long)
            {
                currentValue = ((Long)value).longValue() != 0;
                 
            } else
            {
                log.error("Can't value from class ["+value.getClass().getName()+"]");
            }
            
         } else if (StringUtils.isNotEmpty(defaultValue))
         {
             currentValue = defaultValue.toLowerCase().equals("true") ? true : false;
         }

        if (currentValue != null)
        {
            setSelected(currentValue);
        } else
        {
            setSelected(false);
        }
        isChanged = false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        if (isChanged)
        {
            return this.isSelected();
        }
        return currentValue;
    }


}
