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


import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

/**
 * A JPanel that has built in Validation support, which hold Validatable objects
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValidatedJPanel extends JPanel
{
    protected FormValidator formValidator = new FormValidator(null);

    /**
     * Constructor of the Validated setting panel
     */
    public ValidatedJPanel()
    {
        super(new BorderLayout()); 
        setOpaque(false);
    }
    
    /**
     * Add the main panel to form
     * @param panel the panel to be added
     */
    public void addPanel(final JPanel panel)
    {
        add(panel, BorderLayout.CENTER);
    }
    
    /**
     * Adds a component to be know by the validator
     * @param id the id of the component
     * @param comp the component
     */
    public void addValidationComp(final String id, final Component comp)
    {
        formValidator.addUIComp(id, comp);
        formValidator.addRuleObjectMapping(id, comp);
    }

    /**
     * @return Returns the validator
     */
    public FormValidator getFormValidator()
    {
        return formValidator;
    }
    
    /**
     * Cleanups internal data
     */
    public void cleanUp()
    {
        formValidator.cleanUp();
        removeAll();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    public void finalize()
    {
        cleanUp();
    }   
}
