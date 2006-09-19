/**
 * 
 */
package edu.ku.brc.specify.toycode;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.SpecifyAppPrefs;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.DatabaseLoginListener;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class BasicHibernateMain implements DatabaseLoginListener
{
    public void loggedIn(final String databaseName, final String userName)
    {
    	SpecifyAppPrefs.initialPrefs();

    	// do stuff here
    	
    	System.exit(0);
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
            PlasticLookAndFeel.setMyCurrentTheme(new DesertBlue());
        }

        // Name factories
        System.setProperty("edu.ku.brc.af.core.AppContextMgrFactory", "edu.ku.brc.specify.config.SpecifyAppContextMgr");
        System.setProperty("AppPrefsIOClassName", "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");

        IconManager.setApplicationClass(Specify.class);
        UICacheManager.getInstance(); // initializes it first thing
        UICacheManager.setAppName("Specify");

        // Load Local Prefs
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UICacheManager.getDefaultWorkingPath());
        localPrefs.load();

        BasicHibernateMain tester = new BasicHibernateMain();
		UIHelper.doLogin(false, true, false, tester);
	}
}
