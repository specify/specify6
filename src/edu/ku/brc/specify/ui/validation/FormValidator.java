/* Filename:    $RCSfile: FormValidator.java,v $
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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
/**
 * This class manages all the validators for a single form. One or all the UI components 
 * can be validated and the form can have its own set of validation rukes for enabling or 
 * disabling any of the controls depending on the state of other "named" controls.
 * Both UI Controls and thier labels are registered by name. When a component is disabledits 
 * label can also be disabled.
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class FormValidator implements ValidationListener, DataChangeListener
{
    private static Log log = LogFactory.getLog(FormValidator.class);
    
    // Form validation
    protected JexlContext jc  = null;
    protected Expression  exp = null;

    protected Hashtable<String, DataChangeNotifier> dcNotifiers = new Hashtable<String, DataChangeNotifier>();
    protected Hashtable<String, Component>  fields      = new Hashtable<String, Component>();
    protected Hashtable<String, JLabel>     labels      = new Hashtable<String, JLabel>();
    protected Vector<RuleExpression>        enableRules = new Vector<RuleExpression>();
    protected boolean                       hasChanged  = false;
    protected JButton                       okBtn       = null;    
    protected boolean                       okToEnable  = false;
    
    protected boolean                       ignoreValidationNotifications = false;
    
    // This is a list of listeners for when any data changes in the form
    protected List<DataChangeListener>      dcListeners = new ArrayList<DataChangeListener>();
    
    /**
     * 
     */
    public FormValidator()
    {
        jc  = JexlHelper.createContext();
        addRuleObjectMapping("form", this );
        
    }
    
    /**
     * Register which UI Component is the OK button, meaning which button will
     * be enabled when all the controls are validated
     * @param btn the control 
     */
    public void registerOKButton(final JButton btn)
    {
        okBtn = btn;
    }
    
    /**
     * Returns whether it is alright to enable the OK btn
     * @return Returns whether it is alright to enable the OK btn
     */
    public boolean isOKToEnable()
    {
        return okToEnable;
    }

    /**
     * Add a validation for enabling or disabling control by name
     * @param name the name of the rule
     * @param rule the rule that will be validated
     */
    public void addEnableRule(final String name, final String rule)
    {
        enableRules.addElement(new RuleExpression(name, rule)); 
    }
    
    /**
     * Evaludate all the enable/disable rules and set the control and label
     */
    public void processEnableRules()
    {
        for (RuleExpression expRule : enableRules)
        {
            try 
            {
                // Now evaluate the expression, getting the result
                Object result = expRule.evaluate(jc);
                log.info("Result "+result+" for "+expRule.getName()+"  ");
                if (result instanceof Boolean)
                {
                    Component comp = getComp(expRule.getName());
                    if (comp != null)
                    {
                        log.info("comp.setEnabled("+result+") "+comp);
                        comp.setEnabled((Boolean)result);
                    }
                    
                    JLabel lbl = labels.get(expRule.getName());
                    if (lbl != null)
                    {
                        lbl.setEnabled((Boolean)result);
                    }
                    
                } else
                {
                    log.info("the return from the evaluation is of class "+result);
                }
              
            } catch (Exception ex)
            {
                log.error(ex);
                ex.printStackTrace();
            }

        }
    }
    
    /**
     * Create a new JTextField
     * @param name The name of the control
     * @param size the number of columns for the text control
     * @param isRequired whether the field must be filled in
     * @param valType the type of validation to do
     * @param valStr the validation rule where the subject is "obj"
     * @return returns a new JTextField thata is registered with the logical name
     */
    public JTextField createTextField(final String                     name, 
                                      final int                        size, 
                                      final boolean                    isRequired, 
                                      final UIValidator.ValidationType valType, 
                                      final String                     valStr)
    {
        ValTextField tf = new ValTextField(size);
        tf.setRequired(isRequired);
        fields.put(name, tf);
        
       
        UIValidator        uiv = createValidator(name, tf, isRequired, valType, valStr);
        DataChangeNotifier dcn = new DataChangeNotifier(name, tf, uiv);
        
        dcn.addDataChangeListener(this);
        dcNotifiers.put(name, dcn);
        
        //if (valType == UIValidator.ValidationType.Changed)
        //{
            tf.addKeyListener(dcn);
            
       // } else if (valType == UIValidator.ValidationType.Focus)
        //{
            tf.addFocusListener(dcn);
            
        //} else 
       // {
           // Do nothing for UIValidator.ValidationType.OK
        //}

        addRuleObjectMapping(name, tf);
        return tf;
            
    }
    
    /**
     * Create a new JTextField
     * @param name The name of the control
     * @param size the number of columns for the text control
     * @param isRequired whether the field must be filled in
     * @param valType the type of validation to do
     * @param valStr the validation rule where the subject is "obj"
     * @return returns a new JTextField thata is registered with the logical name
     */
    public JTextField createPasswordField(final String                     name, 
                                          final int                        size, 
                                          final boolean                    isRequired, 
                                          final boolean                    isEncrypted, 
                                          final UIValidator.ValidationType valType, 
                                          final String                     valStr)
    {
        ValPasswordField tf = new ValPasswordField(size);
        tf.setRequired(isRequired);
        tf.setEncrypted(isEncrypted);
        
        fields.put(name, tf);
        
        UIValidator        uiv = createValidator(name, tf, isRequired, valType, valStr);
        DataChangeNotifier dcn = new DataChangeNotifier(name, tf, uiv);
        
        dcn.addDataChangeListener(this);
        dcNotifiers.put(name, dcn);
        
        //if (valType == UIValidator.ValidationType.Changed)
        //{
            tf.addKeyListener(dcn);
            
       // } else if (valType == UIValidator.ValidationType.Focus)
        //{
            tf.addFocusListener(dcn);
            
        //} else 
       // {
           // Do nothing for UIValidator.ValidationType.OK
        //}
    
        addRuleObjectMapping(name, tf);
        return tf;
            
    }
    
    /**
     * Return a new combobox 
     * @param name the name of the control
     * @param items the items to initialize the combobox
     * @return Return a new combobox
     */
    public JComboBox createComboBox(final String name, final String[] items)
    {
        JComboBox          cbx = new JComboBox(items);
        UIValidator        uiv = createValidator(name, cbx, UIValidator.ValidationType.OK);
        DataChangeNotifier dcn = new DataChangeNotifier(name, cbx, uiv);
        
        dcn.addDataChangeListener(this);
        dcNotifiers.put(name, dcn);
        cbx.getModel().addListDataListener(dcn);
        
        fields.put(name, cbx);
        addRuleObjectMapping(name, cbx);
        
        return cbx;
    }
  
    /**
     * Create a label and register it
     * @param name the logical name
     * @param labelStr the string to be shown in the label
     * @return returns the new JLabel
     */
    public JLabel createLabel(final String name, final String labelStr)
    {
        JLabel lbl = new JLabel(labelStr);
        labels.put(name, lbl);
        return lbl;
    }
  
    /**
     * Register the label by name
     * @param name the logical name of the label
     * @param lbl the label component
     * @return return the label that is passed in
     */
    public JLabel addUILabel(final String name, final JLabel lbl)
    {
        labels.put(name, lbl);
        return lbl;
    }
    
    /**
     * Returns a new JTextField 
     * @param builder the JGoodies builder
     * @param cc the CellConstraint
     * @param row the row
     * @param col the col
     * @param name the logical name
     * @param labelName the label's name for the control
     * @param size the number of columns in the text control
     * @param isRequired whether the TextField MUSt have a value
     * @param valType the type of validation
     * @param valStr the default string value
     * @param isPassword maike it a password field
     * @return the new TextControl
     */
    public JTextField createTextField(PanelBuilder    builder, 
                                      CellConstraints cc,
                                      int             row,
                                      int             col,
                                      String          name, 
                                      String          labelName, 
                                      int             size, 
                                      boolean         isRequired, 
                                      UIValidator.ValidationType valType, 
                                      String          valStr,
                                      boolean         isPassword,
                                      boolean         isEncrypted)
    {
        JTextField tf = isPassword ? createPasswordField(name, size, isRequired, isEncrypted, valType, valStr) :
                                     createTextField(name, size, isRequired, valType, valStr);
        
        addUILabel(name, builder.addTitle(labelName+":", cc.xy (col, row)));
        builder.add(tf, cc.xy(col+2, row));
        addRuleObjectMapping(name, tf);
        
        return tf;
       
    }
  
    /**
     * Gets a component by name and returns it as a ValTextField
     * @param name the name of the component
     * @return returns the component by name
     */
    public ValTextField getTextField(final String name)
    {
        Component comp = fields.get(name);
        if (comp instanceof ValTextField)
        {
            return (ValTextField)comp;
        }
        throw new RuntimeException("desired JComponent is not of type ValTextField"+comp);
    }
    
    /**
     * Returns a component by name
     * @param name the name of the component 
     * @return Returns a component by name
     */
    public Component getComp(final String name)
    {
        return fields.get(name);
    }
    
    /**
     * Adds a generic Component by name and return it
     * @param name the logical name of the component
     * @param aComp
     * @return returns the passed in component after it is registered
     */
    public Component addUIComp(final String name, final Component comp)
    {
        fields.put(name, comp);
        addRuleObjectMapping(name, comp);
        
        return comp;
    }
    
    /**
     * Adds a component (or object) that can be referred to oby name in a validatoin rule
     * @param name the name of the component
     * @param comp the component
     */
    public void addRuleObjectMapping(final String name, final Object comp)
    {
        jc.getVars().put(name, comp);
    }
    
    /**
     * The validator for the control (usually a JTextField) that can have a string as a default value
     * 
     * @param componentName the name of the component
     * @param comp the component to be validated (MUST implement UIValidatable)
     * @param isRequired whether the component must have a value
     * @param valType the type of validation to occur
     * @param valStr the default value
     * @return the validator for the control
     */
    public UIValidator createValidator(String                     componentName, 
                                       JComponent                 comp, 
                                       boolean                    isRequired, 
                                       UIValidator.ValidationType valType, 
                                       String                     valStr)
    {
        if (comp instanceof UIValidatable)
        {
            UIValidator validator = new UIValidator(comp, valType, valStr);
            validator.addValidationListener(this);
            return validator;
            
        } else
        {
            throw new RuntimeException("Component is NOT an UIValidatable "+comp);
        }
    }
    
    /**
     * Create a validator for any generic UI control (defaults to be validated as ValidationType.OK)
     * @param componentName the name of the component
     * @param comp the component to be validated (MUST implement UIValidatable)
     * @param valType the type of validation to occur
     * @return the validator for the control
     */
    public UIValidator createValidator(String componentName, JComponent comp, UIValidator.ValidationType valType)
    {
        UIValidator validator = new UIValidator(comp, valType);
        validator.addValidationListener(this);
        return validator;
    }
    
    /**
     * Validate all the fields, this is usually called manually when the OK or Apply button is pressed.
     * Note that we turn off all the Validation notifications because we don't want them firing when 
     * we manually call it.
     */
    public void validateFields()
    {
        ignoreValidationNotifications = true;
        
        processEnableRules();

        okToEnable = true;
        for (Enumeration e=dcNotifiers.elements();e.hasMoreElements();)
        {
            DataChangeNotifier dcn = (DataChangeNotifier)e.nextElement();
            dcn.manualCheckForDataChanged();
            
            UIValidator uiv = dcn.getUIV();
            
            // Make sure we validate the fields that only get validated when the OK button is pressed
            if (uiv != null && uiv.getType() == UIValidator.ValidationType.OK)
            {
                // XXX FIXME not sure what to do here
                if (!uiv.validate())
                {
                    // I really don't think we need to do anything here
                    okToEnable = false;
                }
            }
        }
        ignoreValidationNotifications = false;
    }
    
    /**
     * Reset all dataChangedNotifiers
     */
    public void resetFields()
    {
        for (Enumeration e=dcNotifiers.elements();e.hasMoreElements();)
        {
            DataChangeNotifier dcn = (DataChangeNotifier)e.nextElement();
            UIValidator        uiv = dcn.getUIV();
            if (uiv != null && uiv.getType() == UIValidator.ValidationType.OK)
            {
                dcn.reset();
            }
        }
    }
    
    /**
     * Creates and register a DataChangeNotifier
     * @param name the name
     * @param comp the component
     * @param uiv the UI validator
     * @return the dcn
     */
    public DataChangeNotifier createDataChangeNotifer(String name, Component comp, UIValidator uiv)
    {
        DataChangeNotifier dcn = new DataChangeNotifier(name, comp, uiv);
        dcn.addDataChangeListener(this);
        dcNotifiers.put(name, dcn);
        return dcn;
    }
    
    /**
     * Return an Enumeration of the DataChangeNotifier(s)
     * @return Return an Enumeration of the DataChangeNotifier(s)
     */
    public Enumeration<DataChangeNotifier> getDcNotifiers()
    {
        return dcNotifiers.elements();
    }
    
    /**
     * Clean up internal data 
     */
    public void cleanUp()
    {
        jc  = null;
        exp = null;

        for (Enumeration e=dcNotifiers.elements();e.hasMoreElements();) ((DataChangeNotifier)e.nextElement()).cleanUp();
        dcNotifiers.clear();
        
        fields.clear();
        labels.clear();
        
        for (Enumeration e=enableRules.elements();e.hasMoreElements();) ((RuleExpression)e.nextElement()).cleanUp();
        enableRules.clear();
        
        okBtn = null;            

    }
    

    /**
     * Adds validation listener
     * @param l the listener
     */
    public void addDataChangeListener(final DataChangeListener l)
    {
        dcListeners.add(l);
    }

    /**
     * Removes validation listener
     * @param l the listener
     */
    public void removeDataChangeListener(final DataChangeListener l)
    {
        dcListeners.remove(l);
    }

    //-----------------------------------------------------
    // ValidationListener
    //-----------------------------------------------------
    
    /**
     * Helper methods for turning on the "default" OK button after a validation was completed.
     * @param itsOKToEnable indicates whether it is OK to enable the "OK" button 
     */
    protected void turnOnOKButton(final boolean itsOKToEnable)
    {
        okToEnable = hasChanged && itsOKToEnable;
        if (okBtn != null)
        {
            okBtn.setEnabled(hasChanged && itsOKToEnable);
        }
    }
    
   /* (non-Javadoc)
     * @see ValidationListener#wasValidated(UIValidator)
     */
    public void wasValidated(final UIValidator validator)
    {
        // When the form has been asked manually to be validated then ignore the notifications 
        if (!ignoreValidationNotifications)
        {
            //log.debug("wasValidated "+validator.getComp());
            processEnableRules();
            
            boolean isEnabled = okBtn != null && okBtn.isEnabled();
            boolean isOK      = !validator.isInError();
            if (isEnabled && !isOK)
            {
                //okToEnable = false;
                turnOnOKButton(false);
                
            } else if (!isEnabled && isOK)
            {
                //okToEnable = true;
                turnOnOKButton(true);
            }
        }
    }
    
    //-----------------------------------------------------
    // DataChangeListener
    //-----------------------------------------------------
   
    /* (non-Javadoc)
     * @see DataChangeListener#dataChanged(java.lang.String, java.awt.Component)
     */
    public void dataChanged(final String name, final Component comp)
    {
        //log.debug("DataChangeListener "+name + " was changed");
        hasChanged = true;
        processEnableRules();
        turnOnOKButton(true);
        
        for (DataChangeListener dcl : dcListeners)
        {
            dcl.dataChanged(name, comp);
        }
    }
}
