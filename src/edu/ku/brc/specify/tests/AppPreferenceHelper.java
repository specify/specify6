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

import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UICacheManager;

/**
 * 
 * @code_status Unknown
 * 
 * @author megkumin
 *
 */
public class AppPreferenceHelper
{
    private static final String databaseName = "fish";
    private static final Logger log = Logger.getLogger(AppPreferenceHelper.class);
    /**
     * Constructor 
     */
    public AppPreferenceHelper()
    {
        // TODO Auto-generated constructor stub
    }
    
    public static void setupPreferences() 
    {
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
     * @param args - 
     * void
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

}
