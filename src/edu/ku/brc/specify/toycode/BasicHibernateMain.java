/**
 * 
 */
package edu.ku.brc.specify.toycode;

import java.awt.Window;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.af.auth.MasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginListener;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class BasicHibernateMain implements DatabaseLoginListener
{
    public void aboutToLoginIn()
    {
        // do nothing
        // fill-in with your code
    }
    
    public void loggedIn(final Window window, final String databaseName, final String userName)
    {
    	SpecifyAppPrefs.initialPrefs();

    	// do stuff here
                
    	//System.exit(0);
    }
    
    public void cancelled()
    {
        System.exit(0);
    }

	/**
	 *
	 *
	 * @param args
	 */
	public static void main(String[] args) throws UnsupportedLookAndFeelException
	{
        if (!System.getProperty("os.name").equals("Mac OS X"))
        {
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
        }

        // Name factories
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr"); // Needed by AppContextMgr
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");       // Needed By UIRegistry
        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System
        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and loggin transactions
        System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI

        IconManager.setApplicationClass(Specify.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml"));
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml"));

        UIRegistry.getInstance(); // initializes it first thing
        UIRegistry.setAppName("Specify");

        // Load Local Prefs
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        localPrefs.load();

        Pair<String, String> usernamePassword = MasterPasswordMgr.getInstance().getUserNamePassword();
        BasicHibernateMain tester = new BasicHibernateMain();
		UIHelper.doLogin(usernamePassword.first, usernamePassword.second, true, false, tester, null);
	}
}
