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
package edu.ku.brc.ui;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Class that offers pre-package JOptionPanes that include a disable checkbox.
 * 
 *@code_status Beta
 * 
 * @author megkumin
 *
 */
public class PopupDlg 
{
	public static final Logger log = Logger.getLogger(PopupDlg.class);
	public static int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
	public static int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
	public static int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;
	public static int QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;
	public static int PLAIN_MESSAGE = JOptionPane.PLAIN_MESSAGE;
	public static int DEFAULT_OPTION = JOptionPane.DEFAULT_OPTION;
	public static int YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
	public static int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
	public static int YES_OPTION = JOptionPane.YES_OPTION;
	public static int NO_OPTION = JOptionPane.NO_OPTION;
	public static int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
	public static int OK_OPTION = JOptionPane.OK_OPTION;
	public static int CLOSED_OPTION = JOptionPane.CLOSED_OPTION;
	

	/**
	 * Constructor
	 */
	public PopupDlg()
	{
	    // do nothing
	}
	
	/**
	 * Brings up a JOptionPane dialog with the options Yes, No and Cancel; with the title, Select an Option.  Includes a checkbox that allows the user
	 * to request that this popup dialog is not shown again (uses default "do not again" message)
	 * 
	 * @param parent determines the Frame in which the dialog is displayed; if null, or if the parentComponent has no Frame, a default Frame is used
	 * @param message the String message to display
	 * @param callingClassname the name of the class that called this method
	 * @param dialogNumber a uniquely identifying id for this dialog
	 * @return
	 * int - an integer indicating the option selected by the user
	 */
	public static int showConfirmDialog(
			Component parent, 
			String message,
			String callingClassname, 
			int dialogNumber)
	{
		 PopupDlgPrefsMgr popupMgr = new PopupDlgPrefsMgr();
		 int dialogId = popupMgr.generatePopupDialogId("", message, callingClassname + dialogNumber);
		 boolean dontShow = popupMgr.isDialogDisabled(dialogId);		 
		 if (!dontShow)
		 {
			PopupDlgContent components = new PopupDlgContent(message);
			int response = JOptionPane.showConfirmDialog(parent, components.getComponents());	
			boolean disableMe = components.isCheckboxSelected();
			if(disableMe)
			{
				log.info("User has selected to disable the popup");
				popupMgr.disableDialog(dialogId, response);
			}
			else
			{
				log.info("User has selected not to disable the popup");
			}
			return response;
		}	
		return popupMgr.getDisabledDialogOptionSelection(dialogId);
	}

	/**
	 * Brings up a JOptionPane where the number of choices is determined by the optionType parameter. Includes a checkbox that allows the user
	 * to request that this popup dialog is not shown again, the message that indicates this is provided as a parameter
	 * 
	 * @param parent - determines the Frame in which the dialog is displayed; if null, or if the parentComponent has no Frame, a default Frame is used
	 * @param message - the String message to display
	 * @param title - the title string for the dialog
	 * @param doNotAgainMessage - the message to be displayed that tells the user "do not ask again" 
	 * @param optionType - an integer designating the options available on the dialog: YES_NO_OPTION, or YES_NO_CANCEL_OPTION
	 * @param callingClassname - the name of the class that called this method
	 * @param dialogNumber -  uniquely identifying id for this dialog
	 * @return
	 * int  - an integer indicating the option selected by the user
	 */
	public static int showConfirmDialog(Component parent, 
			String message, 
			String title, 
			String doNotAgainMessage,
			int optionType, 
			String callingClassname, 
			int dialogNumber)
	{
		 PopupDlgPrefsMgr popupMgr = new PopupDlgPrefsMgr();
		 int dialogId = popupMgr.generatePopupDialogId(title, message, callingClassname + dialogNumber);
		 boolean dontShow = popupMgr.isDialogDisabled(dialogId);		 
		 if (!dontShow)
		 {
			PopupDlgContent components = null;
			if(StringUtils.isBlank(doNotAgainMessage))components = new PopupDlgContent(message);
			else	 components = new PopupDlgContent(message, doNotAgainMessage);
			int response = JOptionPane.showConfirmDialog(parent, components.getComponents(),title, optionType);	
			boolean disableMe = components.isCheckboxSelected();
			if(disableMe)
			{
				log.info("User has selected to disable the popup");
				popupMgr.disableDialog(dialogId, response);
			}
			else
			{
				log.info("User has selected not to disable the popup");
			}
			return response;
		}	
		return popupMgr.getDisabledDialogOptionSelection(dialogId);
	}
	
	/**
	 * Brings up a JOptionPane with a specified icon, where the number of choices is determined by the optionType parameter. Includes a checkbox that allows the user
	 * to request that this popup dialog is not shown again, the message that indicates this is provided as a parameter.
	 * 
	 * @param parent - determines the Frame in which the dialog is displayed; if null, or if the parentComponent has no Frame, a default Frame is used
	 * @param message - the String message to display
	 * @param title - the title string for the dialog
	 * @param doNotAgainMessage - the message to be displayed that tells the user "do not ask again" 
	 * @param optionType - an integer designating the options available on the dialog: YES_NO_OPTION, or YES_NO_CANCEL_OPTION
	 * @param messageType - an integer designating the kind of message this is; primarily used to determine the icon from the pluggable Look and Feel: ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE, QUESTION_MESSAGE, or PLAIN_MESSAGE
	 * @param icon - icon - the icon to display in the dialog
	 * @param callingClassname - the name of the class that called this method
	 * @param dialogNumber -  uniquely identifying id for this dialog
	 * @return
	 * int - an int indicating the option selected by the user
	 */
	public static int showConfimDialog(Component parent, 
			String message, 
			String title, 
			String doNotAgainMessage, 
			int optionType, 
			int messageType, 
			Icon icon, 
			String callingClassname, 
			int dialogNumber)
	{
		 PopupDlgPrefsMgr popupMgr = new PopupDlgPrefsMgr();
		 int dialogId = popupMgr.generatePopupDialogId(title, message, callingClassname + dialogNumber);
		 boolean dontShow = popupMgr.isDialogDisabled(dialogId);		 
		 if (!dontShow)
		 {
			PopupDlgContent components = new PopupDlgContent(message, doNotAgainMessage);
			int response = JOptionPane.showConfirmDialog(parent, components.getComponents(),title, optionType, messageType, icon);	
			boolean disableMe = components.isCheckboxSelected();
			if(disableMe)
			{
				log.info("User has selected to disable the popup");
				popupMgr.disableDialog(dialogId, response);
			}
			else
			{
				log.info("User has selected not to disable the popup");
			}
			return response;
		}	
		return popupMgr.getDisabledDialogOptionSelection(dialogId);
	}

	/**
	 * @param args
	 * void
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub

	}
}
