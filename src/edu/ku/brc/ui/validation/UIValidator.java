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

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Component;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTextField;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.log4j.Logger;

/**
 *  Validates a single UI Component the component is to be referred to in the validation script as "obj".
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class UIValidator
{
    public enum Type {None, Focus, Changed, OK}

    private static final Logger log = Logger.getLogger(UIValidator.class);

    protected JexlContext    jc  = null;
    protected Expression     exp = null;

    protected Component      comp = null;
    protected UIValidatable  uiv  = null;

    protected Type type = Type.OK;

    protected Vector<ValidationListener> listeners = new Vector<ValidationListener>();

    /**
     * Constructor
     * @param comp component
     * @param type the type of validation
     */
    protected UIValidator(Component comp, Type type)
    {
        this.comp = comp;
        this.type = type;

        if (comp instanceof UIValidatable)
        {
            uiv  = (UIValidatable)comp;
        }
    }

    /**
     * Constructor
     * @param comp UI Cinponent that MUST implement UIValidatable
     * @param type the type of validation
     * @param expression the validation expression to evaluate
     */
    public UIValidator(Component comp, Type type, String expression)
    {
        this(comp, type);

        // Only create Jexl objects if we have a "real" rule,
        if (isNotEmpty(expression))
        {
            //jc  = JexlHelper.createContext();
            //jc.getVars().put("obj", comp);

            try
            {
                exp = ExpressionFactory.createExpression( expression );

            } catch (Exception e)
            {
                log.debug("Exp["+expression+"]");
                // XXX FIXME
                e.printStackTrace();
            }
        } else if (uiv == null || !uiv.isRequired())
        {
            throw new RuntimeException("Component doesn't have a validation rule AND it isn't required, so why are creating a UIValidator!");
        }

    }

    /**
     * Tells the UI Control that the form is "new" and to not show validation errors until it has focus.
     * @param isNew true if it is a new form, false if not
     */
    public void setAsNew(boolean isNew)
    {
        //log.debug(">>>>" + (type != Type.OK ? "NOT OK" : "OK")+"  "+this.comp);
        if (uiv != null && type != Type.OK)
        {
            uiv.setAsNew(isNew);
        }
    }

    /**
     * Tells the UI Control that the form is "new" and to not show validation errors until it has focus.
     * @param isNew true if it is a new form, false if not
     */
    public void setChanged(boolean isChanged)
    {
        if (uiv != null)
        {
            uiv.setChanged(isChanged);
        }
    }

    /**
     * Parse string for Type and return None if there is a parse error
     * @param type the string with the Type
     * @return the Type
     */
    public static Type parseValidationType(final String type)
    {
        if (isNotEmpty(type))
        {
            try
            {
                return Type.valueOf(type);

            } catch (Exception ex)
            {
                return Type.OK;
            }
        }
        // else
        return Type.OK;
    }


    public boolean isRequired()
    {
        return uiv != null && uiv.isRequired();
    }

    /**
     * Validate the control
     * @return true if it passes, and false if it didn't
     */
    public boolean validate()
    {
        // If it isn't enabled than don't validate it
        if (!comp.isEnabled())
        {
            return true;
        }

        UIValidatable.ErrorType errorState = UIValidatable.ErrorType.Valid;

        boolean isTextField = (uiv != null && uiv.getValidatableUIComp() instanceof JTextField) || comp instanceof JTextField;

         // If it is required then it MUST have a value or it is in error
        if (uiv != null)
        {
            errorState = uiv.validateState();
        }

        // Skip processing the field if it is already in error as a required field
        if (errorState ==  UIValidatable.ErrorType.Valid &&
            isTextField &&
            jc != null &&
            exp != null)
        {
            try
            {
                Object result = exp.evaluate(jc);

                Map<?,?> map = jc.getVars();
                Object[] keys = map.keySet().toArray();
                for (Object key : keys)
                {
                    log.debug("## ["+key+"]["+map.get(key).getClass().toString()+"]");
                }
                log.debug("** "+exp.getExpression()+"  "+result+"  "+(result != null ? result.getClass().toString() : ""));
                if (result instanceof Boolean)
                {
                    errorState = ((Boolean)result).booleanValue() ? UIValidatable.ErrorType.Valid : UIValidatable.ErrorType.Error;
                }

            } catch (Exception e)
            {
                // XXX FIXME
                e.printStackTrace();
            }

            if (uiv != null)
            {
                uiv.setState(errorState);
            }
        }

        // XXX This isn't right, this needs some rethinking

        // Don't notify any of the listeners if we are validating it for an OK button
        if (isTextField && uiv.isRequired())
        {
            uiv.setState(errorState);
            notifyValidationListeners();

        } else if (type != Type.OK)
        {
            notifyValidationListeners();
        }

        return errorState == UIValidatable.ErrorType.Valid;
    }

    /**
     * Returns whether it is in error, meaning whether it passed the validation
     * @return Returns whether it is in error, meaning whether it passed the validation
     */
    public boolean isInError()
    {

        return uiv != null ? uiv.isInError() : false;
    }

    /**
     * Add a validation listener
     * @param l the listener
     */
    public void addValidationListener(ValidationListener l)
    {
        listeners.addElement(l);
    }

    /**
     * remove the validation listener
     * @param l the listener
     */
    public void removeValidationListener(ValidationListener l)
    {
        listeners.remove(l);
    }

    /**
     * Notify all the listeners that it was
     */
    protected void notifyValidationListeners()
    {
        for (ValidationListener vl : listeners)
        {
            vl.wasValidated(this);
        }
    }

    /**
     * Sets the JEXL Context into the validator
     * @param jc JEXL Context
     */
    public void setJc(JexlContext jc)
    {
        this.jc = jc;
    }

    /**
     * Returns the comp.
     * @return Returns the comp.
     */
    public Component getComp()
    {
        return comp;
    }

    /**
     * Returns the type.
     * @return Returns the type.
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Returns the UIValidatable
     * @return the UIValidatable
     */
    public UIValidatable getUIV()
    {
        return uiv;
    }

    /**
     * Tells it to clean up.
     */
    public void cleanUp()
    {
        listeners.clear();
    }

}
