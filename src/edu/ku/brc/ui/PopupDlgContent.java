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
 *//**
 * 
 */
package edu.ku.brc.ui;
import java.awt.Container;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

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
	//String message = "";	
	JLabel message = new JLabel("");
	JCheckBox checkbox = new JCheckBox("Do not me ask again");
	Object[] values = new Object[] {message, checkbox};


	/**
	 * Constructor
	 * @param message
	 */
	public PopupDlgContent(String message)
	{
		super();
		JLabel dispalyableMessage = new JLabel(message);
		values = new Object[] {dispalyableMessage, checkbox};
	}	


	/**
	 * Constructor
	 * @param message
	 * @param doNotAgainMessage
	 */
	public PopupDlgContent(String message, String doNotAgainMessage)
	{
		super();
		checkbox = new JCheckBox(doNotAgainMessage);
		JLabel dispalyableMessage = new JLabel(message);
		values = new Object[] {dispalyableMessage, checkbox};
	}

	/**
	 * Constructor
	 * @param displayableComponent
	 */
	public PopupDlgContent(Container displayableComponent)
	{
		super();
		values = new Object[] {displayableComponent, checkbox};
	}	

	/**
	 * Constructor
	 * @param displayableComponent
	 * @param doNotCommand
	 */
	public PopupDlgContent(JComponent displayableComponent, String doNotCommand)
	{
		super();
		checkbox = new JCheckBox(doNotCommand);
		values = new Object[] {displayableComponent, checkbox};
	}

	/**
	 * @return - 
	 * Object[] - 
	 */
	public Object[] getComponents() {
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

	/**
	 * @param args
	 */
	public static void main(String[] args)

	{
		// TODO Auto-generated method stub
	}
}

