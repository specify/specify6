/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPreferences;


/**
 * Handles the operations required for disabling/undisabling a dialog, including
 * storing a users dialog button selection.
 * 
 *@code_status Beta
 * 
 * @author megkumin
 *
 */
public class PopupDlgPrefsMgr
{
    private static final Logger log = Logger.getLogger(PopupDlgPrefsMgr.class);
	AppPreferences appPrefs = AppPreferences.getRemote();
	private String dialogNamePrefix = "ui.popupdialog.";
	

	/**
	 * Constructor
	 */
	public PopupDlgPrefsMgr()
	{
		// do nothing
	}
	
	/**
	 * Generates a unique Id for a dialog.
	 * 
	 * @param title - the title of the dialog
	 * @param message - the message being displayed
	 * @param identifierStr - a uniquely identifying string (classname and dialog number)
	 * @return
	 * int - the unique dialog id
	 */
	public int generatePopupDialogId(String title, String message, String identifierStr )
	{
		log.debug("Generating DialogId");
		 PopupDlgId popupId = new PopupDlgId(title, message, identifierStr);	
		 return popupId.getId();
	}

	/**
	 * Disabled the dialog and stores the users button selection when the dialog was 
	 * disabled.  Dialog and selection choice are stored in the AppPreferences.
	 * 
	 * @param dialogId - the unique id of the dialog
	 * @param actionChoice - the users button selection when the dialog was disabled
	 * void
	 */
	public void disableDialog(int dialogId, int actionChoice)
	{
		String dialogPrefName = dialogNamePrefix + dialogId;
		log.debug("Adding disabling dialog to preferences: [" + dialogId + "] with selected value of: "+actionChoice);
		appPrefs.putInt(dialogPrefName, Integer.valueOf(actionChoice));
	}	
	
	/**
	 * "Undisables" the dialog from the AppPreferences.  When dialog is removed from 
	 * the AppPreferences, the next the dialog should be shown, it will.
	 * 
	 * @param dialogId - the unique id of the dialog
	 * void
	 */
	public void removeDialogFromPrefs(int dialogId)
	{
		String dialogPrefName = dialogNamePrefix + dialogId;
		log.debug("Removing dialog from preferences: [" + dialogId + "]");	
		appPrefs.remove(dialogPrefName);
	}

	/**
	 * If a dialog has been disabled, this retrieves the users original button 
	 * selection fromn the AppPreferences.
	 * 
	 * @param dialogId - the unique id of the dialog
	 * @return
	 * int - the int button selection
	 */
	public int getDisabledDialogOptionSelection(int dialogId)
	{
		String dialogPrefName = dialogNamePrefix + dialogId;
		log.debug("Reading preference to get previous selection choice for dialog: [" + dialogId + "]");
		int actionChoice = appPrefs.getInt(dialogPrefName, -1).intValue();
		log.debug(" Selection was: "+actionChoice);
		return actionChoice;
	}

	/**
	 * Checks the AppPreferences to see if the dialog has been disabled.
	 * 
	 * @param dialogId  - the unique id of the dialog
	 * @return
	 * boolean
	 */
	public boolean isDialogDisabled(int dialogId)
	{
		String dialogPrefName = dialogNamePrefix + dialogId;
		log.debug("Reading preferences to check whether dialog has been disabled: [" +dialogId+"]" );
		int actionChoice = appPrefs.getInt(dialogPrefName, -1).intValue();		
		if (actionChoice == -1)
		{
			log.debug("Dialog has not been disabled");
			return false;
		}
		log.debug("Dialog previously has been disabled");
		return true;
	}
}
