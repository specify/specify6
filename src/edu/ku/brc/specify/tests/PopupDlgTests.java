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
package edu.ku.brc.specify.tests;
import java.util.Random;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.UIHelper;
import edu.ku.brc.ui.PopupDlg;
import edu.ku.brc.ui.PopupDlgPrefsMgr;
import edu.ku.brc.ui.UICacheManager;


/**
 * Tests the PopupDialog system, such that dialogs can be disabled and not shown again
 * but also retrieve the users selection from the dialog.
 * @code_status Alpha
 * 
 * @author megkumin
 *
 */
public class PopupDlgTests extends TestCase 
{
	private static final Logger log = Logger.getLogger(PopupDlgTests.class);
    private static final String databaseName = "fish";
    private Random generator = new Random();
    
    protected AppPreferences appPrefs = null;

	/**
	 * Constructor
	 * @param arg0
	 */
	public PopupDlgTests(String arg0) {
		super(arg0);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		log.info("Setup - Loading preferences...");
        //-----------------------------------------------------
        // This is needed for loading views
        //-----------------------------------------------------
        UICacheManager.getInstance(); // initializes it first thing
        if (UICacheManager.getAppName() == null) // this is needed because the setUp gets run separately for each test
        {
            System.setProperty("edu.ku.brc.af.core.AppContextMgrFactory",   "edu.ku.brc.specify.config.SpecifyAppContextMgr"); // Needed by AppContextMgr
            System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
            System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");       // Needed By UICacheManager
            
            UICacheManager.getInstance(); // initializes it first thing
            UICacheManager.setAppName("Specify");

            // Load Local Prefs
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.setDirPath(UICacheManager.getDefaultWorkingPath());
            localPrefs.load();
            
            // This will log us in and return true/false
            if (!UIHelper.tryLogin("com.mysql.jdbc.Driver", 
                                   "org.hibernate.dialect.MySQLDialect", 
                                   databaseName, 
                                   "jdbc:mysql://localhost/"+databaseName, 
                                   "rods", 
                                   "rods"))
            {
                throw new RuntimeException("Couldn't login into ["+databaseName+"] "+DBConnection.getInstance().getErrorMsg());
            } else
            {
                HibernateUtil.getCurrentSession();
                AppPreferences.getRemote().load(); // Loads prefs from the database
                log.info("Loaded preferences");
            }
        }
	}
	
	/**
	 * Test to make sure that a user can disable a dialog.
	 * void
	 */
	public void testDisablingOfDialog() {
		log.info("JUNIT TEST - test Disabling Of Dialog");
		String message = "ABCDEFGHIJKLMNOPQRSTUVWXYZ?";
		String title = "Title";
		int dialogNumber = generator.nextInt();
		String callingClassname = this.getClass().toString();
		int userSelection = PopupDlg.YES_OPTION;
		
		PopupDlgPrefsMgr popupMgr = new PopupDlgPrefsMgr();
		int dialogId = popupMgr.generatePopupDialogId(title, message, callingClassname + dialogNumber);
		
		assertFalse("FAILURE - Dialog should not be disabled yet [" 
				+ dialogId + "] User Selection: [" + userSelection  + "]", 
				popupMgr.isDialogDisabled(dialogId));
		
		popupMgr.disableDialog(dialogId, userSelection);
		assertTrue("FAILURE - did not disable dialog. DialogId: [" 
				+ dialogId + "] User Selection: [" + userSelection  + "]", 
				popupMgr.isDialogDisabled(dialogId));
		
		popupMgr.removeDialogFromPrefs(dialogId);
		assertFalse("FAILURE -Dialog failed to remove Dialog from prefs . DialogId: [" + dialogId + "]", 
				popupMgr.isDialogDisabled(dialogId));
	}
	
	/**
	 * Tests to make sure that a dialog disabling and user selection are stored and
	 * retrieved properly.
	 * 
	 * void
	 */
	public void testRetrievalOfUserSelectionFromDisabledDialog() {
		log.info("JUNIT TEST - test Retrieval Of User Selection From Disabled Dialog");
		String message = "ABCDEFGHIJKLMNOPQRSTUVWXYZ?";
		String title = "Title";
		int dialogNumber = generator.nextInt();
		String callingClassname = this.getClass().toString();
		int userSelection = PopupDlg.YES_OPTION;
		
		PopupDlgPrefsMgr popupMgr = new PopupDlgPrefsMgr();
		int dialogId = popupMgr.generatePopupDialogId(title, message, callingClassname + dialogNumber);
		
		assertFalse("FAILURE - Dialog should not be disabled yet [" 
				+ dialogId + "] User Selection: [" + userSelection  + "]", 
				popupMgr.isDialogDisabled(dialogId));
		
		popupMgr.disableDialog(dialogId, userSelection);
		assertTrue("FAILURE - did not disable dialog. DialogId: [" 
				+ dialogId + "] User Selection: [" + userSelection  + "]", 
				popupMgr.isDialogDisabled(dialogId));
		
		int foundVal = popupMgr.getDisabledDialogOptionSelection(dialogId);
		assertEquals("FAILURE - user selection value was not stored correctly. DialogId:" + dialogId + " Expected Value: [" + userSelection + "] Found value: [" + foundVal + "]", 
				userSelection, foundVal);		

		popupMgr.removeDialogFromPrefs(dialogId);
		assertFalse("FAILURE - Dialog failed to remove Dialog from prefs. DialogId: [" + dialogId + "]", 
				popupMgr.isDialogDisabled(dialogId));
	}
	
	/**
	 * Tests to make sure that a dummy dialog cannot be found disabled as a preference.
	 * 
	 * void
	 */
	public void testNonExistentDialog() {
		log.info("JUNIT TEST - test to make sure non existent dialog is not disabled.");
		PopupDlgPrefsMgr popupMgr = new PopupDlgPrefsMgr();
		
		int dialogNumber = generator.nextInt();
		assertFalse("FAILURE - Dialog found to be disabled when it was not disabled. DialogId: [" + dialogNumber + "]", 
				popupMgr.isDialogDisabled(dialogNumber));		
	}

	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		log.info("Tearing down");
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
