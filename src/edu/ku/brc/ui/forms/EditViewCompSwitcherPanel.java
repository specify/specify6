/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.forms;

import java.awt.CardLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.forms.persist.FormCellIFace;
import edu.ku.brc.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.ui.forms.validation.DataChangeListener;
import edu.ku.brc.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.ui.forms.validation.FormValidator;
import edu.ku.brc.ui.forms.validation.UIValidatable;
import edu.ku.brc.ui.forms.validation.UIValidator;
import edu.ku.brc.ui.forms.validation.ValidationListener;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 4, 2007
 *
 */
public class EditViewCompSwitcherPanel extends JPanel implements GetSetValueIFace, UIValidatable, DataChangeListener, ValidationListener
{
    protected Component          editComp;
    protected Component          viewComp;
    protected Component          currentComp;
    protected FormCellIFace      formCell;
    protected String             currentCard;
    protected Object             dataObj;
    protected UIValidatable      uiVal;
    protected DataChangeNotifier dataChangeNotifier;
    
    protected CardLayout         cardLayout      = new CardLayout();
    protected FormValidator      validator       = null;
    protected FormValidator      parentValidator = null;
    
    /**
     * @param formCell
     */
    public EditViewCompSwitcherPanel(final FormCellIFace formCell)
    {
        super();
        
        this.formCell = formCell;
        
        setLayout(cardLayout);
        
        validator = new FormValidator(null);
        validator.addValidationListener(this);
    }
    
    /**
     * @param parentValidator the parentValidator to set
     */
    public void setParentValidator(FormValidator parentValidator)
    {
        this.parentValidator = parentValidator;
    }

    /**
     * @param dataChangeNotifier the dataChangeNotifier to set
     */
    public void setDataChangeNotifier(DataChangeNotifier dataChangeNotifier)
    {
        this.dataChangeNotifier = dataChangeNotifier;
    }

    /**
     * @return the validator
     */
    public FormValidator getValidator()
    {
        return validator;
    }

    public void putIntoEditMode()
    {
        currentCard = "Edit";
        cardLayout.show(this, currentCard);
        
        currentComp = editComp;
        repaint();
    }
    
    public void putIntoViewMode()
    {
        currentCard = "View";
        cardLayout.show(this, currentCard);
        
        currentComp = viewComp;
        repaint();
    }
    
    /**
     * @param editCmp
     * @param viewCmp
     */
    public void set(final Component editCmpReg, 
                    final Component editCmpAdd,
                    final Component viewCmpReg, 
                    final Component viewCmpAdd)
    {
        /*editComp = (GetSetValueIFace)(editCmpReg != null ? editCmpReg : editCmpAdd);
        viewComp = (GetSetValueIFace)(viewCmpReg != null ? viewCmpReg : viewCmpAdd);
        
        add("Edit", editCmpAdd != null ? editCmpAdd : editCmpReg);
        add("View", viewCmpAdd != null ? viewCmpAdd : viewCmpReg);
        */
        
        editComp = register("Edit", editCmpAdd, editCmpReg);
        viewComp = register("View", viewCmpAdd, viewCmpReg);
        
        uiVal = editComp instanceof UIValidatable ? (UIValidatable)editComp : null;
        
        currentComp = editComp;
    }
    
    /**
     * @param name
     * @param compToAdd
     * @param compToReg
     * @return
     */
    protected Component register(final String    name, 
                                 final Component compToAdd, 
                                 final Component compToReg)
    {
        add(name, compToAdd);
        
        Component control =  compToReg == null ? compToAdd : compToReg;
        
        JScrollPane scrPane;
        Component comp;
        if (control instanceof JScrollPane)
        {
            scrPane = (JScrollPane)control;
            comp = scrPane.getViewport().getView();
        } else
        {
            scrPane = null;
            comp = control;
        }
        return comp;
    }
    
    /**
     * @return the current component
     */
    public Component getCurrentComp()
    {
        return currentComp;
    }
    
    /**
     * @return the either the edit component 'true' or the view component 'false'
     * @param isEditComp true -> edit, false -> view
     * @return the component
     */
    public Component getComp(final boolean isEditComp)
    {
        return isEditComp ? editComp : viewComp;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        for (int i=0;i<getComponentCount();i++)
        {
            getComponent(i).setEnabled(enabled);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        if (currentComp == viewComp)
        {
            return dataObj;
        }
        
        // Note this always passes back the value from the "edit" component and never from the read only component
        // this SHOULD be ok, because we also always set the edit component. so the validation system validates it ok.
        boolean isSingleValueFromSet = false;
        if (formCell instanceof FormCellSubViewIFace)
        {
            isSingleValueFromSet = ((FormCellSubViewIFace)formCell).isSingleValueFromSet();
        }
        
        return FormViewObj.getValueFromComponent(editComp, isSingleValueFromSet,
                                                 formCell.getType() == FormCellIFace.CellType.command, 
                                                 currentCard);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        dataObj = value;
        FormViewObj.setDataIntoUIComp(editComp, value, defaultValue);
        FormViewObj.setDataIntoUIComp(viewComp, value, defaultValue);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        dataObj = null;
        if (dataChangeNotifier != null)
        {
            dataChangeNotifier.cleanUp();
            dataChangeNotifier = null;
        }
        
        if (validator != null)
        {
            validator.removeValidationListener(this);
            validator       = null;
        }
        parentValidator = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getState()
     */
    @Override
    public ErrorType getState()
    {
        if (currentComp == viewComp)
        {
            return UIValidatable.ErrorType.Valid;
        }
        return uiVal != null ? uiVal.getState() : UIValidatable.ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    @Override
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isChanged()
     */
    @Override
    public boolean isChanged()
    {
        if (currentComp == viewComp)
        {
            return false;
        }
        return uiVal != null ? uiVal.isChanged() : false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isInError()
     */
    @Override
    public boolean isInError()
    {
        if (currentComp == viewComp)
        {
            return false;
        }
        return uiVal != null ? uiVal.isInError() : false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isRequired()
     */
    @Override
    public boolean isRequired()
    {
        if (currentComp == viewComp)
        {
            return false;
        }
        return uiVal != null ? uiVal.isRequired() : false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    @Override
    public void reset()
    {
        if (uiVal != null)
        {
            uiVal.reset();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    @Override
    public void setAsNew(boolean isNew)
    {
        if (currentComp == editComp && uiVal != null)
        {
            uiVal.setAsNew(isNew);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    @Override
    public void setChanged(boolean isChanged)
    {
        if (currentComp == editComp && uiVal != null)
        {
            uiVal.setChanged(isChanged);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    @Override
    public void setRequired(boolean isRequired)
    {
        if (currentComp == editComp && uiVal != null)
        {
            uiVal.setRequired(isRequired);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setState(edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType)
     */
    @Override
    public void setState(ErrorType state)
    {
        if (currentComp == editComp && uiVal != null)
        {
            uiVal.setState(state);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#validateState()
     */
    @Override
    public ErrorType validateState()
    {
        if (currentComp == viewComp)
        {
            return UIValidatable.ErrorType.Valid;
        }
        return uiVal != null ? uiVal.validateState() : UIValidatable.ErrorType.Valid;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.DataChangeListener#dataChanged(java.lang.String, java.awt.Component, edu.ku.brc.ui.forms.validation.DataChangeNotifier)
     */
    @Override
    public void dataChanged(String name, Component comp, DataChangeNotifier dcn)
    {
        if (dcn != null)
        {
            dcn.actionPerformed(null);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.ValidationListener#wasValidated(edu.ku.brc.ui.forms.validation.UIValidator)
     */
    @Override
    public void wasValidated(UIValidator validatorArg)
    {
        if (parentValidator != null)
        {
            //parentValidator.wasValidated(validatorArg);
            dataChangeNotifier.actionPerformed(null);
        }
    }
}
