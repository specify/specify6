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
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class manages all the validators for a single form. One or all the UI components
 * can be validated and the form can have its own set of validation rukes for enabling or
 * disabling any of the controls depending on the state of other "named" controls.
 * Both UI Controls and thier labels are registered by name. When a component is disabled its
 * label can also be disabled.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class FormValidator implements ValidationListener, DataChangeListener
{
    private static final Logger log = Logger.getLogger(FormValidator.class);
    
    public enum EnableType { ValidItems, ValidAndChangedItems, ValidNotNew}

    private String name = ""; // Optional for debugging

    // Form validation
    protected JexlContext jc  = null;
    protected Expression  exp = null;
    
    protected FormValidator                         parent;
    protected Vector<FormValidator>                 kids        = new Vector<FormValidator>();

    protected List<FormValidationRuleIFace>         formRules   = new Vector<FormValidationRuleIFace>();
    protected Hashtable<String, DataChangeNotifier> dcNotifiers = new Hashtable<String, DataChangeNotifier>();
    protected List<UIValidator>                     validators  = new Vector<UIValidator>();

    protected Hashtable<String, Component>          fields      = new Hashtable<String, Component>();
    protected Hashtable<String, JLabel>             labels      = new Hashtable<String, JLabel>();
    
    protected Hashtable<EnableType, Vector<Component>> enableHash = new Hashtable<EnableType, Vector<Component>>();
    protected JComponent                            saveComp           = null;
    protected EnableType                            saveEnableType     = EnableType.ValidAndChangedItems;

    protected boolean                               isTopLevel  = false;
    protected boolean                               enabled     = false;
    protected boolean                               hasChanged  = false;
    protected boolean                               isNewObj    = false;
    protected boolean                               isRequired  = true;
    
    protected UIValidatable.ErrorType               formValidationState = UIValidatable.ErrorType.Valid;
    protected UIValidatable.ErrorType               kidsValState        = UIValidatable.ErrorType.Valid;
    
    protected boolean                               processRulesAreOK   = true;
    
    protected boolean                               ignoreValidationNotifications = false;
    protected boolean                               okToDataChangeNotification    = true;
    
    protected JButton                               validationInfoBtn             = null;

    // This is a list of listeners for when any data changes in the form
    protected List<DataChangeListener>              dcListeners  = new ArrayList<DataChangeListener>();
    protected List<ValidationListener>              valListeners = new ArrayList<ValidationListener>();

    /**
     * Constructor.
     * @param parent the parent validator or null if top level
     */
    public FormValidator(final FormValidator parent)
    {
        this.parent = parent;
        
        jc  = JexlHelper.createContext();
        addRuleObjectMapping("form", this );
        
        for (EnableType type : EnableType.values())
        {
            enableHash.put(type, new Vector<Component>());
        }
    }

    /**
     * @return the parent
     */
    public FormValidator getParent()
    {
        return parent;
    }

    /**
     * @param saveComp
     * @param saveEnableType
     */
    public void setSaveComp(final JComponent saveComp, 
                            final EnableType saveEnableType)
    {
        this.saveComp       = saveComp;
        this.saveEnableType = saveEnableType;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(FormValidator parent)
    {
        this.parent = parent;
    }

    /**
     * @return the validators
     */
    protected List<UIValidator> getValidators()
    {
        return validators;
    }

    /**
     * @return the isRequired
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /**
     * @param isRequired the isRequired to set
     */
    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /**
     * @param isNewObj the isNewObj to set
     */
    public void setNewObj(boolean isNewObj)
    {
        this.isNewObj = isNewObj;
        enableUIItems(getState() == UIValidatable.ErrorType.Valid && !isNewObj, EnableType.ValidItems);
        enableUIItems(getState() == UIValidatable.ErrorType.Valid && !isNewObj, EnableType.ValidNotNew);
        updateValidationBtnUIState();
    }

    /**
     * Adds a child validator.
     * @param val the validator
     */
    public void add(final FormValidator val)
    {
        //log.debug("Add Kid["+val.getName()+"] to "+name);
        kids.add(val);
    }
    
    /**
     * Removes a child validator.
     * @param val the validator.
     */
    public void remove(final FormValidator val)
    {
        //log.debug("Remove Kid["+val.getName()+"] from "+name);
        kids.remove(val);
    }
    
    /**
     * emoves all the children validators.
     */
    public void clearKids()
    {
        //log.debug("Parent "+name+" clearing kids:   "+kids.size());
        kids.clear();
    }
    
    /**
     * (Protected so other objects in the class can access it)
     * @return the kids
     */
    protected Vector<FormValidator> getKids()
    {
        return kids;
    }

    /**
     * @param comp
     * @param type
     */
    public void addEnableItem(final Component comp, 
                              final EnableType type)
    {
        
        Vector<Component> list = enableHash.get(type);
        list.add(comp);
        comp.setEnabled(false);
    }
    
    /**
     * @param comp
     * @param type
     */
    public void removeEnabledItem(final Component comp, final EnableType type)
    {
        Vector<Component> list = enableHash.get(type);
        list.remove(comp);
    }
    
    /**
     * Returns whether the form is valid
     * @return Returns whether it is alright to enable the OK btn
     */
    public boolean isFormValid()
    {
        return isFormValid(true);
    }

    /**
     * Returns whether the form is valid
     * @param checkKids whether to check the kids
     * @return Returns whether it is alright to enable the OK btn
     */
    protected boolean isFormValid(final boolean checkKids)
    {
        //log.debugprintln("isFormValid - Name: "+name+"  enabled "+enabled+"  hasChanged "+hasChanged+"  isNewObj "+isNewObj);
        
        if (checkKids)
        {
            for (FormValidator kid : kids)
            {
                if (!kid.isFormValid())
                {
                    return false;
                }
            }
        }
        //log.debug("isFormValid - "+formValidationState);
        return formValidationState == UIValidatable.ErrorType.Valid && processRulesAreOK;
    }

    /**
     * Manually sets the state of the validator.
     * @param formValidationState the new state
     */
    public void setFormValidationState(UIValidatable.ErrorType formValidationState)
    {
        if (enabled)
        {
            if (this.formValidationState != formValidationState)
            {
                updateValidationBtnUIState();
            }
            this.formValidationState = formValidationState;
        }
    }

    /**
     * Returns the state of the form, which really means... return the worst
     * "state" that was found from all the validators.
     * @return the state of the form
     */
    public UIValidatable.ErrorType getState()
    {
        //UIValidatable.ErrorType val = !hasChanged && isNewObj && isFirstTime ? UIValidatable.ErrorType.Valid : processRulesAreOK ? formValidationState : UIValidatable.ErrorType.Error;
        //log.info(name+" ["+val+"]["+hasChanged+"]["+isNewObj+"]["+isFirstTime+"] "+formValidationState);
        //return !hasChanged && isNewObj && isFirstTime ? UIValidatable.ErrorType.Valid : processRulesAreOK ? formValidationState : UIValidatable.ErrorType.Error;
        
        return !hasChanged && isNewObj ? UIValidatable.ErrorType.Valid : processRulesAreOK ? formValidationState : UIValidatable.ErrorType.Error;
    }

    /**
     * @param state
     */
    public void setState(final UIValidatable.ErrorType state)
    {
        formValidationState = state;
        updateValidationBtnUIState();
    }
    
    /**
     * Returns whether the form has been changed
     * @return Returns whether the form has been changed
     */
    public boolean hasChanged()
    {
        for (FormValidator kid : kids)
        {
            if (kid.hasChanged())
            {
                return true;
            }
        }
        return hasChanged;
    }

    /**
     * Sets whether the form will notify the listeners of data change notifications
     * @param okToDataChangeNotification whether to notify
     */
    public void setDataChangeNotification(final boolean okToDataChangeNotification)
    {
        this.okToDataChangeNotification = okToDataChangeNotification;
        for (FormValidator kid : kids)
        {
            kid.setDataChangeNotification(okToDataChangeNotification);
        }
    }

    /**
     * Helper to that adds a validation for enabling or disabling control by name
     * @param id the id of the rule
     * @param rule the rule that will be validated
     */
    public void addEnableRule(final String id, final String rule)
    {
        formRules.add(new RuleExpression(id, rule));
    }

    /**
     * Helper to that adds a validation for enabling or disabling control by name
     * @param rule the rule that will be validated
     */
    public void addRule(final FormValidationRuleIFace rule)
    {
        formRules.add(rule);
    }

    /**
     * Tells all the validators that are required not to validate
     */
    public void setUIValidatorsToNew(boolean isNew)
    {
        if (enabled)
        {
            for (UIValidator uiv : validators)
            {
                uiv.setAsNew(isNew);
            }
            updateValidationBtnUIState();
            
            for (FormValidator kid : kids)
            {
                kid.setUIValidatorsToNew(isNew);
            }
        }
    }
    
    /**
     * 
     */
    public void setUIValidatorsToNotChanged()
    {
        this.hasChanged = false;
        for (UIValidator uiv : validators)
        {
            uiv.setChanged(false);
        }
        
        if (enabled)
        {
            updateValidationBtnUIState();
        }
        
        for (FormValidator kid : kids)
        {
            kid.setUIValidatorsToNotChanged();
        }

    }

    /**
     * Return true if validaot ris registering changes.
     * @return true if validaot ris registering changes.
     */
    public boolean isEnabled()
    {
        return enabled;
    }
    
    /**
     * @param parentFV
     * @param enable
     * @param type
     */
    protected void enabledTreeForUI(final FormValidator parentFV, 
                                    final boolean enable,
                                    final EnableType type)
    {
        enableUIItems(enable, type);
        
        for (FormValidator kid : parentFV.kids)
        {
            enabledTreeForUI(kid, enable, type);
        }
    }

    /**
     * Sets whether validator should except changes, this also sets the the Validated state to Valid
     * and set the hasChanged to false when disabling.
     * @param enabled true to enable it false to disable it.
     */
    public void setEnabled(final boolean enabled)
    {
        //log.debug("SetEnabled: "+name+" "+enabled);
        
        if (this.enabled != enabled)
        {
            if (!enabled)
            {
                if (getName().equals("PreparationProperty")) {
                        log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!DISABLING PP Validator !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        new Exception().printStackTrace();
                        log.debug("------------------------- END DISABLING PP Validator Stack ---------------------------------------------------------");
                    }
                hasChanged          = false;
                formValidationState = UIValidatable.ErrorType.Valid;
                kidsValState        = UIValidatable.ErrorType.Valid;
                resetFields();
            }
            
            this.enabled = enabled;
            
            // Enable / Diable the Validator (NOT the UI associated with the validator)
            for (FormValidator kid : kids)
            {
                kid.setEnabled(enabled);
            } 
            
            //enableUIItems(enabled, saveEnableType);
        }
    }

    /**
     * @return the isTopLevel
     */
    public boolean isTopLevel()
    {
        return isTopLevel;
    }

    /**
     * @param isTopLevel the isTopLevel to set
     */
    public void setTopLevel(boolean isTopLevel)
    {
        this.isTopLevel = isTopLevel;
    }

    /**
     * @return the isNewObj
     */
    public boolean isNewObj()
    {
        return isNewObj;
    }

    /**
     * Resets the form, typically after new data has arrived. For new objects it sets the validation state to "Valid"
     * for non-new objects it validates the form.
     * 
     * @param isNewObj true if it is a new data object, false if not.
     */
    public void reset(final boolean isNewObjArg)
    {
        if (enabled)
        {
            // setHasChanged(false); // Don't call this it cascades.
            
            hasChanged = false;
            
            resetFields();
    
            setDataChangeNotification(true); // this doesn't effect validation notifications
    
            if (isNewObjArg)
            {
                isNewObj = isNewObjArg;
                //setFormValidationState(UIValidatable.ErrorType.Valid); 
                
            } else 
            {
                validateForm();    
            }
            
            updateValidationBtnUIState();
            
            for (FormValidator kid : kids)
            {
                kid.reset(isNewObjArg);
            }
        }
    }

    /**
     * Evaluate all the enable/disable rules and set the control and label
     */
    public boolean processFormRules()
    {
        boolean formIsOK = true;

        boolean debug = false;
        if (debug)
        {
            log.debug(name+" ****** processFormRules  ");
            Map<?,?> map = jc.getVars();
            Object[] keys = map.keySet().toArray();
            for (Object key : keys)
            {
                log.debug(name+" ### ["+key+"]["+map.get(key).getClass().toString()+"]");
            }
        }

        //log.debug("processFormRules ["+name+"]------------------------------------------------- Number of Rules: "+formRules.size());
        for (FormValidationRuleIFace rule : formRules)
        {
            try
            {
                // Now evaluate the expression, getting the result
                boolean result = rule.evaluate(jc);
                //log.debug("Result ["+result+"] for ID["+rule.getId()+"]  Rule["+((RuleExpression)rule).expression.getExpression()+"]");
                if (rule.getScope() == FormValidationRuleIFace.Scope.Field)
                {
                    Component comp = getComp(rule.getId());
                    if (comp != null)
                    {
                        //log.debug("    comp.setEnabled("+result+") "+comp.getClass().toString());
                        comp.setEnabled(result);
                    }

                    JLabel lbl = labels.get(rule.getId());
                    if (lbl != null)
                    {
                        lbl.setEnabled(result);
                    }

                } else if (rule.getScope() == FormValidationRuleIFace.Scope.Form)
                {
                    formIsOK &= result;
                }


            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormValidator.class, ex);
                log.error(name+" "+ex.toString());
                formIsOK = false;
                //ex.printStackTrace();
            }
        }
        return formIsOK;
    }

    /**
     * @param textField textField to be hooked up
     * @param id id of control
     * @param isRequiredArg whether the field must be filled in
     * @param valType the type of validation to do
     * @param valStr the validation rule where the subject is its name
     * @param changeListenerOnly indicates whether to create a validator
     */
    public void hookupTextField(final JTextComponent   textField,
                                final String           id,
                                final boolean          isRequiredArg,
                                final UIValidator.Type valType,
                                final String           valStr,
                                final boolean          changeListenerOnly)
    {

        fields.put(id, textField);

        UIValidator.Type type = isRequiredArg ? UIValidator.Type.Changed : valType;

        UIValidator uiv = null;
        if (StringUtils.isEmpty(valStr))
        {
            if (valType != UIValidator.Type.None)
            {
                uiv = createValidator(textField, valType);
            }
        } else
        {
            uiv = changeListenerOnly ? null : createValidator(textField, type, valStr);
        }
        
        if (uiv != null)
        {
            DataChangeNotifier dcn = new DataChangeNotifier(id, textField, uiv);
            dcn.addDataChangeListener(this);
    
            dcNotifiers.put(id, dcn);
    
            if (type == UIValidator.Type.Changed || isRequiredArg || changeListenerOnly)
            {
                textField.getDocument().addDocumentListener(dcn);
    
            } else if (type == UIValidator.Type.Focus)
            {
                textField.addFocusListener(dcn);
    
            } else
            {
               // Do nothing for UIValidator.Type.OK
            }
        }
        
        addRuleObjectMapping(id, textField);
    }

    /**
     * Hooks up generic component to be validated
     * @param comp component to be hooked up
     * @param id id of control
     * @param valType the type of validation to do
     * @param valStr the validation rule where the subject is its name
     * @param changeListenerOnly indicates whether to create a validator
     * @return the component passed in
     */
    public DataChangeNotifier hookupComponent(final JComponent       comp,
                                              final String           id,
                                              final UIValidator.Type valType,
                                              final String           valStr,
                                              final boolean          changeListenerOnly)
    {
        fields.put(id, comp);

        UIValidator uiv = null;
        if (StringUtils.isEmpty(valStr))
        {
            uiv = createValidator(comp, valType);
        } else
        {
            uiv = changeListenerOnly ? null : createValidator(comp, valType, valStr);
        }
        DataChangeNotifier dcn = new DataChangeNotifier(id, comp, uiv);
        dcn.addDataChangeListener(this);

        dcNotifiers.put(id, dcn);

        addRuleObjectMapping(id, comp);

        return dcn;
    }

    /**
     * Create a label and register it
     * @param labelName the logical name
     * @param labelStr the string to be shown in the label
     * @return returns the new JLabel
     */
    public JLabel createLabel(final String labelName, final String labelStr)
    {
        JLabel lbl = UIHelper.createLabel(labelStr);
        setControlSize(lbl);
        labels.put(labelName, lbl);
        return lbl;
    }

    /**
     * Register the label by name
     * @param labelName the logical name of the label
     * @param lbl the label component
     * @return return the label that is passed in
     */
    public JLabel addUILabel(final String labelName, final JLabel lbl)
    {
        labels.put(labelName, lbl);
        return lbl;
    }

    /**
     * Gets a component by id and returns it as a ValTextField
     * @param id the id of the component
     * @return returns the component by id
     */
    public ValTextField getTextField(final String id)
    {
        Component comp = fields.get(id);
        if (comp instanceof ValTextField)
        {
            return (ValTextField)comp;
        }
        throw new RuntimeException("Desired JComponent ["+id+"]is not of type ValTextField"+comp);
    }

    /**
     * Returns a component by name
     * @param id the name of the component
     * @return Returns a component by name
     */
    public Component getComp(final String id)
    {
        return fields.get(id);
    }

    /**
     * Adds a generic Component by name and return it
     * @param id the id of the component
     * @param comp the component to add
     * @return returns the passed in component after it is registered
     */
    public Component addUIComp(final String id, final Component comp)
    {
        fields.put(id, comp);
        addRuleObjectMapping(id, comp);

        //log.debug(" Adding ["+id+"]["+comp.getClass().toString()+"] to validator.");

        return comp;
    }

    /**
     * Returns the Text of the label for a control's Id (strips ':' from end of string)
     * @param id the unique identifier
     * @return the text of the label
     */
    public String getLabelTextForId(final String id)
    {
        JLabel label = labels.get(id);
        if (label != null)
        {
            String labelStr = label.getText();
            if (labelStr.endsWith(":"))
            {
                return labelStr.substring(0, labelStr.length()-1);
            }
            return labelStr;
        }
        return "";
    }

    /**
     * Adds a component (or object) that can be referred to oby name in a validatoin rule.
     * @param id the name of the component
     * @param comp the component
     */
    @SuppressWarnings("unchecked")
    public void addRuleObjectMapping(final String id, final Object comp)
    {
        if (isNotEmpty(id) && comp != null)
        {
            if (jc != null && jc.getVars() != null)
            {
                jc.getVars().put(id, comp);
            }
            
        } else if (!UIRegistry.isRelease())
        {
            throw new RuntimeException("id["+id+"] or Comp["+comp+"] is null.");
        }
    }

    /**
     * Creates a validator for the control (usually a JTextField) that can have a string as a default value.
     * @param comp the component to be validated (MUST implement UIValidatable)
     * @param valType the type of validation to occur
     * @param valStr the default value
     * @return the validator for the control
     */
    protected UIValidator createValidator(final JComponent       comp,
                                          final UIValidator.Type valType,
                                          final String           valStr)
    {
        if (comp instanceof UIValidatable)
        {
            UIValidator validator = new UIValidator(comp, valType, valStr);
            validator.setJc(jc);
            validator.addValidationListener(this);
            validators.add(validator);

            return validator;

        }
        // else
        throw new RuntimeException("Component is NOT an UIValidatable "+comp);
    }

    /**
     * Create a validator for any generic UI control (defaults to be validated as Type.OK)
     * @param comp the component to be validated (MUST implement UIValidatable)
     * @param valType the type of validation to occur
     * @return the validator for the control
     */
    public UIValidator createValidator(JComponent comp, UIValidator.Type valType)
    {
        UIValidator validator = new UIValidator(comp, valType);
        validator.setJc(jc);
        validator.addValidationListener(this);
        validators.add(validator);
        return validator;
    }

    /**
     * This will evaluate the forms "rules" but none of the fields, instead
     * it asks each validatable field if it is ok (because they will have already
     * been validated.
     */
    protected void checkForValidForm()
    {
        processRulesAreOK = processFormRules();

        //log.debug("checkForValidForm ["+name+"]");
        //log.debug(name+" checkForValidForm -> formValidationState - processFormRules ["+formValidationState+"]");
       
        if (processRulesAreOK)
        {
            formValidationState = UIValidatable.ErrorType.Valid;
            for (UIValidator uiv : validators)
            {
                if (uiv.getComp().isEnabled())
                {
                    if (uiv.isInError())
                    {
                       switch (uiv.getUIV().getState())
                       {
                           case Valid :
                                break;
                        
                           case Incomplete:
                                if (formValidationState == UIValidatable.ErrorType.Valid)
                                {
                                    formValidationState = UIValidatable.ErrorType.Incomplete;
                                }
                                break;
                        
                           case Error :
                                formValidationState = UIValidatable.ErrorType.Error;
                                break;
                        }
                       
                        // Assumes Error is th worst, so if it is "less than" an Error i.e. Incompete
                        // then we don't want to override an error with a lesser state
                        if (formValidationState == UIValidatable.ErrorType.Error)
                        {
                            break;
                            
                        }
                    }
                }
            }
        }

        boolean isValid = getState() == UIValidatable.ErrorType.Valid;
        enableUIItems(isValid && hasChanged, EnableType.ValidAndChangedItems);
        enableUIItems(isValid, EnableType.ValidItems);
        enableUIItems(isValid && !isNewObj, EnableType.ValidNotNew);
        
        //log.debug(">>>>>> isValid: "+isValid+" hasCHanged: "+hasChanged+" Val & New: "+(isValid && !isNewObj));
        
        //log.debug(name);
        if (parent != null)
        {
            FormValidator parentFV = parent;
            parentFV.validateForm();
            while (parentFV.parent != null)
            {
                parentFV = parentFV.parent;
                parentFV.validateForm();
            }
        }
    }

    /**
     * @return the top most validator
     */
    protected FormValidator getRoot()
    {
        if (parent == null)
        {
            return this;
        }
        
        FormValidator parentFV = parent;
        while (parentFV.parent != null)
        {
            parentFV = parent;
        }
        return parentFV;
    }
    
    /**
     * @param ignoreValidationNotifications the ignoreValidationNotifications to set
     */
    public void setIgnoreValidationNotifications(boolean ignoreValidationNotifications)
    {
        this.ignoreValidationNotifications = ignoreValidationNotifications;
    }

    /**
     * Validates the fields.
     */
    public void validateForm()
    {
        if (enabled)
        {
            //log.debug("validateForm ["+name+"] ");
    
            boolean curIgnoreVal = ignoreValidationNotifications; // cache the value in case it has already been set
            ignoreValidationNotifications = true;
            
            kidsValState = UIValidatable.ErrorType.Valid;
            for (FormValidator kid : kids)
            {
                kid.validateForm();
                
                if (kid.getState().ordinal() > kidsValState.ordinal())
                {
                    kidsValState = kid.getState();
                }
                /*if (kid.getState() != UIValidatable.ErrorType.Valid)
                {
                    if (kidsValState != UIValidatable.ErrorType.Error)
                    {
                        kidsValState = kid.getState();
                    }
                }*/
            }
    
            processRulesAreOK = processFormRules();
            if (processRulesAreOK)
            {
                formValidationState = UIValidatable.ErrorType.Valid;
        
                // We need to go ahead and validate everything even if processFormRules fails
                // because the user will need the visual feed back on the form for which fields are in error
                for (DataChangeNotifier dcn : dcNotifiers.values())
                {
                    if (dcn.isEnabled())
                    {
                        dcn.manualCheckForDataChanged();
        
                        UIValidator uiv = dcn.getUIV();
                        if (uiv != null && !uiv.validate())
                        {
                            switch (uiv.getUIV().getState())
                            {
                                case Valid :
                                     break;
                             
                                case Incomplete:
                                     if (formValidationState == UIValidatable.ErrorType.Valid)
                                     {
                                         formValidationState = UIValidatable.ErrorType.Incomplete;
                                     }
                                     break;
                             
                                case Error :
                                     formValidationState = UIValidatable.ErrorType.Error;
                                     break;
                             }
                        }
                    }
                }
            } else
            {
                formValidationState = UIValidatable.ErrorType.Error;
            }
            
            //log.debug(name + " - kidsValState "+kidsValState+"  formValidationState "+formValidationState);
            
            if (kidsValState.ordinal() > formValidationState.ordinal())
            {
                formValidationState = kidsValState;
            }
    
            //log.debug("validateForm ["+name+"] State: "+formValidationState);
            
            boolean isValid = isFormValid();
            enableUIItems(hasChanged && isValid, EnableType.ValidAndChangedItems);
            enableUIItems(isValid, EnableType.ValidItems);
            enableUIItems(isValid && !isNewObj, EnableType.ValidNotNew);
            
            updateValidationBtnUIState();
            
            ignoreValidationNotifications = curIgnoreVal;
        }
    }
    
    /**
     * IMPORTANT CHANGE: rods 05/04/11 will now only cascade upwards
     * when the parent is false and the child is true. It use to 
     * cascade false up through the parents.
     * 
     * @param changed true/false
     */
    protected void cascadeHasChanged(final boolean changed)
    {
        hasChanged = changed;
        
        if (parent != null)
        {
            FormValidator parentFV = parent;
            while (parentFV != null)
            {
                if (!parentFV.hasChanged && changed)
                {
                    parentFV.hasChanged = changed;
                }
                //log.debug("Parent Val "+parentFV.getName()+" hasChanged "+changed);
                parentFV = parentFV.parent;
            }
        }
    }
    
    /**
     * Sets the DataChangeNotifier to change = true if it has one.
     * @param comp the component to check
     */
    public void setDataChangeInNotifier(final Component comp)
    {
        if (comp != null)
        {
            cascadeHasChanged(true);
            
            for (DataChangeNotifier dcn : dcNotifiers.values())
            {
                UIValidator uiv = dcn.getUIV();
                if (uiv != null && uiv.getComp() == comp)
                {
                    dcn.setDataChanged(true);
                }
            }
        }
    }

    /**
     * Reset all dataChangedNotifiers
     */
    public void resetFields()
    {
        if (enabled)
        {
            for (Enumeration<DataChangeNotifier> e=dcNotifiers.elements();e.hasMoreElements();)
            {
                DataChangeNotifier dcn = e.nextElement();
                dcn.reset();
            }
        }
    }

    /**
     * Creates and register a DataChangeNotifier
     * @param id the id
     * @param comp the component
     * @param uiv the UI validator
     * @return the dcn
     */
    public DataChangeNotifier createDataChangeNotifer(final String      id, 
                                                      final Component   comp, 
                                                      final UIValidator uiv)
    {
        DataChangeNotifier dcn = new DataChangeNotifier(id, comp, uiv);
        dcn.addDataChangeListener(this);
        dcNotifiers.put(id, dcn);

        if (uiv != null && !validators.contains(uiv))
        {
            validators.add(uiv);
        }
        return dcn;
    }

    /**
     * Clean up internal data
     */
    public void cleanUp()
    {
        jc  = null;
        exp = null;
        
        for (FormValidator kid : kids)
        {
            kid.cleanUp();
        }
        parent = null;

        for (DataChangeNotifier dcn : dcNotifiers.values())
        {
            dcn.cleanUp();
        }
        dcNotifiers.clear();

        for (UIValidator uv : validators)
        {
            uv.cleanUp();
        }
        validators.clear();

        fields.clear();
        labels.clear();

        for (FormValidationRuleIFace rule : formRules)
        {
            if (rule instanceof RuleExpression)
            {
                ((RuleExpression)rule).cleanUp();
            }
        }
        formRules.clear();

        for (Vector<Component> list : enableHash.values())
        {
            list.clear();
        }
        dcListeners.clear();
        valListeners.clear();
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

    /**
     * Adds validation listener
     * @param l the listener
     */
    public void addValidationListener(final ValidationListener l)
    {
        valListeners.add(l);
    }

    /**
     * Removes validation listener
     * @param l the listener
     */
    public void removeValidationListener(final ValidationListener l)
    {
        valListeners.remove(l);
    }

    /**
     * Returns the name of the validator (optional)
     * @return the name of the validator (optional)
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the validator (optional for debugging)
     * @param name the name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the Map of Notifiers
     * @return the Map of Notifiers
     */
    public Map<String, DataChangeNotifier> getDCNs()
    {
        return dcNotifiers;
    }

    /**
     * Dumps the State of the Validation State
     * @param doBrief just the state of validator, when false it dumps everthing about all the DCNs and validators
     */
    public void dumpState(final boolean doBrief, final int level)
    {
        String displayName = (name != null ? name : "No Name");

        if (doBrief)
        {
            log.debug(level + " *** "+displayName+"  isValid: "+isFormValid()+" "+getState());
            
        } else
        {
            StringBuilder strBuf = new StringBuilder(64);
            log.debug("\n" + level + " ------------"+displayName+"-------------");
            log.debug("Valid: "+isFormValid());
            log.debug("\n");

            int maxLen = 0;
            for (DataChangeNotifier dcn : dcNotifiers.values())
            {
                int len = dcn.getId().length();
                maxLen = Math.max(maxLen, len);
            }

            for (DataChangeNotifier dcn : dcNotifiers.values())
            {
                String nm = dcn.getId();
                strBuf.setLength(0);
                strBuf.append(nm);
                for (int i=0;i<=(maxLen-nm.length());i++) strBuf.append(" ");
                UIValidator uiv = dcn.getUIV();
                if (uiv != null)
                {
                    strBuf.append("UIV: ");
                    strBuf.append(uiv.getType());
                    strBuf.append(" ");
                    strBuf.append(uiv.getUIV().getState());
                }

                log.debug(strBuf.toString());
            }
            log.debug("-------------------------");
        }
        
        for (FormValidator kid : kids)
        {
            kid.dumpState(doBrief, level+1);
        }
    }
    
    /**
     * @param validationInfoBtn
     */
    public void setValidationBtn(final JButton valInfoBtn)
    {
        this.validationInfoBtn = valInfoBtn;
        
        // This for debugging validation, makes it easier to see the validation feedback
        // validationInfoBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    
    /**
     * Sets the visual state on the validation btn. 
     */
    public void updateValidationBtnUIState()
    {
        //log.debug("updateValidationBtnUIState ["+name+"] "+getState());
        if (validationInfoBtn != null)
        {
            boolean                 enable = true;
            ImageIcon               icon   = IconManager.getIcon("ValidationValid", IconManager.IconSize.Std16);
            UIValidatable.ErrorType state  = getState();
            
            if ( kidsValState.ordinal() > state.ordinal())
            {
                state = kidsValState;
            }

            if (state == UIValidatable.ErrorType.Incomplete)
            {
                icon = IconManager.getIcon("ValidationWarning", IconManager.IconSize.Std16);

            } else if (state == UIValidatable.ErrorType.Error)
            {
                icon = IconManager.getIcon("ValidationError", IconManager.IconSize.Std16);
            } else
            {
                enable = false;
            }
            validationInfoBtn.setEnabled(enable);
            validationInfoBtn.setIcon(icon);
        }
    }

    //-----------------------------------------------------
    // ValidationListener
    //-----------------------------------------------------


    /**
     * Helper methods for turning on the "default" OK button after a validation was completed.
     * @param itsOKToEnable indicates whether it is OK to enable the "OK" button
     */
    protected void enableUIItems(final boolean itsOKToEnable, final EnableType type)
    {
        //log.debug(name+" hasChanged ["+hasChanged+"]     itsOKToEnable ["+itsOKToEnable+ "]    enableItems: [" + type+"]");

        for (Component comp : enableHash.get(type))
        {
            comp.setEnabled(itsOKToEnable);
        }
        updateValidationBtnUIState();
        
        //log.debug("UIValidator.isIgnoreAllValidation() "+UIValidator.isIgnoreAllValidation());
        //log.debug(name+" type "+type+"  itsOKToEnable "+itsOKToEnable);
        if ((type == saveEnableType || type == EnableType.ValidAndChangedItems) && saveComp != null && !UIValidator.isIgnoreAllValidation())
        {
            if (itsOKToEnable)
            {
                saveComp.setEnabled(isFormValid(true));
            } else
            {
                saveComp.setEnabled(false);
            }
        }
    }
    
    /**
     * Asks the Save button to update the enabled state based on whether the form is valid.
     */
    public void updateSaveUIEnabledState()
    {
        if (saveComp != null)
        {
            saveComp.setEnabled(isFormValid(true));
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
            if (validator != null && validators.contains(validator))
            {
                cascadeHasChanged(true);
            }
            checkForValidForm();

            for (ValidationListener vcl : valListeners)
            {
                vcl.wasValidated(validator);
            }
            updateValidationBtnUIState();
        }
        
        if (parent != null)
        {
            //parent.wasValidated(validator);
        }
    }
    
    /**
     * Walk up to the Root validator and validate it.
     */
    public void validateRoot()
    {
        FormValidator fvParent = this;
        while (fvParent.getParent() != null)
        {
            fvParent = fvParent.getParent();
        }
        fvParent.validateForm();
    }

    //-----------------------------------------------------
    // DataChangeListener
    //-----------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.DataChangeListener#dataChanged(java.lang.String, java.awt.Component, edu.ku.brc.ui.forms.validation.DataChangeNotifier)
     */
    public void dataChanged(final String dcName, final Component comp, DataChangeNotifier dcn)
    {
        if (!okToDataChangeNotification || UIValidator.isIgnoreAllValidation())
        {
            return;
        }
        //log.debug("DataChangeListener "+name + " was changed");

        // Here is the big assumption:
        // The validation system is built on the premise that when a control changes
        // it is validated before it calls all of it's listeners to tell them it has been changed.
        //
        // This means that this form has already received a callback "wasValidated" and validated
        // the entire form. we don't have to do anything here but flip the boolean that data has changed
        // and then see if the button can enabled
        // But if the DataChangeNotifier didn't have a UIValidator then we need to re-validate the
        // form because "wasValidated" wouldn't have been called

        // rods - 08/01/2008 - Moved here before valifing the root 
        // instead of below and afterward
        cascadeHasChanged(true);
        
        UIValidator uiv = dcn != null ? dcn.getUIV() : null;
        if (uiv == null)
        {
            
            validateRoot();

        } else
        {
            //log.debug("About validateForm: ["+name+"] "+enabled);
            //validateForm();
        }
        
        //cascadeHasChanged(true);
        
        // Notify anyone else who is listening to the form for changes
        for (DataChangeListener dcl : dcListeners)
        {
            dcl.dataChanged(dcName, comp, dcn);
        }

        boolean isValid = formValidationState == UIValidatable.ErrorType.Valid;
        //log.debug(hasChanged+"  "+isValid);
        
        enableUIItems(hasChanged && isValid, EnableType.ValidAndChangedItems);
        //enableUIItems(isValid && !isNewObj, EnableType.ValidItems);
        enableUIItems(isValid, EnableType.ValidItems);
        enableUIItems(isValid && !isNewObj, EnableType.ValidNotNew);

        
        updateValidationBtnUIState();
    }

    /**
     * Sets whether the data in the form has changed and cascades
     * it up through the parents to the top. NOTE: It only cascades
     * changes from false to true.
     * 
     * @param hasChanged whether the data in the form has changed
     */
    public void setHasChanged(boolean hasChanged)
    {
        //log.debug("setHasChanged ["+name+"] "+hasChanged);
        cascadeHasChanged(hasChanged);
    }


}
