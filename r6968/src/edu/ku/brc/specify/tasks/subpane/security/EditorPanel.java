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
package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
        saveBtn = UIHelper.createButton(getResourceString("SAVE"));
        saveBtn.setEnabled(false);
        
        Action saveAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e)
            {
                sap.doSave(true);
            }
        };
        saveBtn.addActionListener(saveAction);
        UIHelper.addSaveKeyBinding(saveBtn, saveAction);
    }

    /**
     * @param formValidator the formValidator to set
     */
    public void setFormValidator(final FormValidator formValidator)
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
    public void stateChanged(final ChangeEvent e)
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
