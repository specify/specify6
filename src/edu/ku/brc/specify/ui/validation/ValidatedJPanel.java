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
package edu.ku.brc.specify.ui.validation;


import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

/**
 * A JPanel that has built in Validation support, which hold Validatable objects
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValidatedJPanel extends JPanel
{
    
    protected FormValidator formValidator = new FormValidator();

    /**
     * Constructor of the EMail setting panel
     */
    public ValidatedJPanel()
    {
        super(new BorderLayout()); 
    }
    
    /**
     * Constructor of the EMail setting panel
     */
    public ValidatedJPanel(JPanel panel)
    {
        super(new BorderLayout()); 
        
        add(panel, BorderLayout.CENTER);
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
    public void finalize()
    {
        cleanUp();
    }   
}
