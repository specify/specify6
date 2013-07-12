/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Toolkit;

import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import edu.ku.brc.ui.UIRegistry;

/**
 * Used for enforcing the length of data in a text field.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jun 12, 2007
 *
 */
public class LengthInputVerifier extends InputVerifier
{
    protected String caption;
    protected int    maxLength;
    
    /**
     * @param caption
     * @param maxLength
     */
    public LengthInputVerifier(final String caption, final int maxLength)
    {
        this.caption   = caption;
        this.maxLength = maxLength;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
     */
    @Override
    public boolean verify(JComponent comp)
    {
        String text = null;
        if (comp instanceof JTextComponent)
        {
        	text = ((JTextComponent )comp).getText();
        } else if (comp instanceof JComboBox)
        {
        	text = ((JComboBox )comp).getSelectedItem().toString();
        }
    	boolean isOK = text == null || text.length() <= maxLength;
        if (!isOK)
        {
            String msg = String.format(getResourceString("UI_NEWDATA_TOO_LONG"), new Object[] { caption, maxLength } );
            UIRegistry.getStatusBar().setErrorMessage(msg);
            Toolkit.getDefaultToolkit().beep();
            
        } else
        {
            UIRegistry.getStatusBar().setText("");
        }
        return isOK;
    }
    // This is for testing it is 256 characters
    // 1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456
    // 12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234561234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345612345678901234567890
}
