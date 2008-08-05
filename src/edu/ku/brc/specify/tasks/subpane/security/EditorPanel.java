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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 5, 2008
 *
 */
public class EditorPanel extends JPanel
{
    private JButton           saveBtn;
    private boolean           hasChanged            = false;
    
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
                sap.doSave();
            }
        });
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
}
