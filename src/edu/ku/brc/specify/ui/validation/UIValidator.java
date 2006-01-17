/* Filename:    $RCSfile: UIValidator.java,v $
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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

public class UIValidator implements FocusListener, KeyListener, PropertyChangeListener
{
    //private static Log log = LogFactory.getLog(UIValidator.class);
    
    public enum ValidationType {None, Focus, Changed, OK};
    
    protected JexlContext    jc  = null;
    protected Expression     exp = null;
    
    protected JComponent     comp = null;
    protected UIValidatable  uiv  = null;
    
    protected ValidationType type = ValidationType.OK;
    
    protected Vector<ValidationListener>     listeners = new Vector<ValidationListener>();
    
    /**
     * Constructor 
     * @param comp component
     * @param type the type of validation
     */
    public UIValidator(JComponent comp, ValidationType type)
    {
        this.comp = comp;
        this.type = type;
    }
    
    /**
     * Constructor
     * @param comp UI Cinponent that MUST implement UIValidatable
     * @param type the type of validation
     * @param val the initial value
     */
    public UIValidator(JComponent comp, ValidationType type, String expression)
    {
        this(comp, type);
        
        if (!(comp instanceof UIValidatable))
        {
            throw new RuntimeException("Component doesn't implement UIValidatable "+comp);
        }
        if (expression == null || expression.length() == 0)
        {
            throw new RuntimeException("Expression must not be null or or empty ");
        }
        
        uiv  = (UIValidatable)comp;
        
        jc  = JexlHelper.createContext();
        jc.getVars().put("obj", comp );
        
        try 
        {
            exp = ExpressionFactory.createExpression( expression );
        } catch (Exception e)
        {
            // XXX FIXME
            e.printStackTrace();
        }

    }
    
    /**
     * Validate the control
     * @return true if it passes, and false if it didn't
     */
    public boolean validate()
    {
        boolean isInError = false;
               
        if (jc != null && exp != null)
        {
            if (comp instanceof JTextField)
            {
                try 
                {
                    Object result = exp.evaluate(jc);
                    if (result instanceof Boolean)
                    {
                        isInError = ((Boolean)result).booleanValue();
                    }
                  
                } catch (Exception e)
                {
                    // XXX FIXME
                    e.printStackTrace();
                }

                if (uiv.isRequired() && ((JTextField)comp).getText().length() == 0)
                {
                    isInError = true;
                }
            }
            
            uiv.setInError(isInError);
        }
        
        if (type != ValidationType.OK)
        {
            notifyValidationListeners();
        }
                
        return !isInError;    
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
    
    //----------------------------------------
    // FocusListener
    //----------------------------------------
    public void focusGained(FocusEvent e)
    {
        validate();
    }
    
    public void focusLost(FocusEvent e)
    {
        validate();
    }
    
    //----------------------------------------
    // KeyListener
    //----------------------------------------
    public void keyPressed(KeyEvent e)    
    {
        
    }
    
    public void keyReleased(KeyEvent e)
    {
        validate();  
    }
    
    public void keyTyped(KeyEvent e)
    {
        //validate();
    }
    
    //----------------------------------------
    // PropertyChangeListener
    //----------------------------------------
    public void propertyChange(PropertyChangeEvent evt)
    {
        
        System.out.println(evt);
    }

    /**
     * Returns the comp.
     * @return Returns the comp.
     */
    public JComponent getComp()
    {
        return comp;
    }

    /**
     * Returns the type.
     * @return Returns the type.
     */
    public ValidationType getType()
    {
        return type;
    }

}
