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
package edu.ku.brc.af.ui;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.Container;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import edu.ku.brc.ui.UIRegistry;

/**
 * Creates content for a PopupDialog as used with the PopupDlgMgr.  
 * Creates a label with text, and a checkbox that allows a user to 
 * disable the popup in future viewing. 
 * as
 * 
 * @code_status Beta
 * 
 * @author megkumin
 *
 */
public class PopupDlgContent
{      
    protected JLabel    message;
    protected JCheckBox checkbox;
    
    protected Object[]  values;


	/**
	 * Constructor
	 * @param message
	 */
	public PopupDlgContent(final String messageStr)
	{
		this(messageStr, UIRegistry.getResourceString("DONT_ASK_AGAIN"));
	}	


	/**
	 * Constructor
	 * @param message
	 * @param doNotAgainMessage
	 */
	public PopupDlgContent(final String messageStr, String doNotAgainMessage)
	{
		super();

        message  = createLabel(messageStr);
        checkbox = createCheckBox(doNotAgainMessage);
		values   = new Object[] {message, checkbox};
        
	}

	/**
	 * Constructor
	 * @param displayableComponent
	 */
	public PopupDlgContent(final Container displayableComponent)
	{
		super();
		checkbox = createCheckBox(UIRegistry.getResourceString("DONT_ASK_AGAIN"));
		values   = new Object[] {displayableComponent, checkbox};
	}	

	/**
	 * Constructor
	 * @param displayableComponent
	 * @param doNotCommand
	 */
	public PopupDlgContent(final JComponent displayableComponent, final String doNotCommand)
	{
		this(displayableComponent);
		checkbox.setText(doNotCommand);
	}

	/**
	 * @return - 
	 * Object[] - 
	 */
	public Object[] getComponents() 
    {
		return values;
	}

	/**
	 * @return - 
	 * boolean - 
	 */
	public boolean isCheckboxSelected()
	{
		return checkbox.isSelected();
	}
}

