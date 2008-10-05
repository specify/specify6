/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.validation.DataChangeListener;
import edu.ku.brc.af.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 5, 2008
 *
 */
@SuppressWarnings("serial")
public class EditorPanel extends JPanel implements ChangeListener, 
                                                   DataChangeListener
{
    private JButton                saveBtn;
    private boolean                hasChanged            = false;
    private FormValidator          formValidator         = null;
    
    /**
     * @param sap
     */
    public EditorPanel(final SecurityAdminPane sap)
    {
        saveBtn = UIHelper.createButton("Save"); // I18N
        saveBtn.setEnabled(false);
        
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                sap.doSave(true);
            }
        });
    }

    /**
     * @param formValidator the formValidator to set
     */
    public void setFormValidator(FormValidator formValidator)
    {
        this.formValidator = formValidator;
    }

    /**
     * @return the saveBtn
     */
    public JButton getSaveBtn()
    {
        return saveBtn;
    }

    /**
     * @return the hasChanged
     */
    public boolean hasChanged()
    {
        return hasChanged;
    }

    /**
     * @param changed
     */
    public void setHasChanged(final boolean changed)
    {
        if (!hasChanged && changed)
        {
            saveBtn.setEnabled(true);
            
        } else if (hasChanged && !changed)
        {
            saveBtn.setEnabled(false);
        }
        hasChanged = changed;
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        setHasChanged(true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.DataChangeListener#dataChanged(java.lang.String, java.awt.Component, edu.ku.brc.ui.forms.validation.DataChangeNotifier)
     */
    public void dataChanged(String name, Component comp, DataChangeNotifier dcn)
    {
        setHasChanged(formValidator.isFormValid());
    }
    
}
