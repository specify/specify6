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
package edu.ku.brc.specify.tests;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
        UIRegistry.getInstance(); // initializes it first thing
        if (UIRegistry.getAppName() == null) // this is needed because the setUp gets run separately for each test
        {
            //System.setProperty("edu.ku.brc.af.core.AppContextMgrFactory",   "edu.ku.brc.specify.config.SpecifyAppContextMgr"); // Needed by AppContextMgr
            //System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
            //System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");       // Needed By UIRegistry
            System.setProperty("edu.ku.brc.af.core.AppContextMgrFactory",   "edu.ku.brc.specify.config.SpecifyAppContextMgr"); // Needed by AppContextMgr
            System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
            System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");       // Needed By UIRegistry
            System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System
            System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");  // Needed By the Form System for updating Lucene and loggin transactions
            System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
          
            UIRegistry.getInstance(); // initializes it first thing
            UIRegistry.setAppName("Specify");

            // Load Local Prefs
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.setDirPath(UIRegistry.getAppDataDir());
            localPrefs.load();
            
            String hostName = "localhost";
            
            Pair<String, String> usernamePassword = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
            // This will log us in and return true/false
            if (!UIHelper.tryLogin("com.mysql.jdbc.Driver", 
                                   "org.hibernate.dialect.MySQLDialect", 
                                   databaseName, 
                                   "jdbc:mysql://"+hostName+"/"+databaseName, 
                                   usernamePassword.first, 
                                   usernamePassword.second))
            {
                throw new RuntimeException("Couldn't login into ["+databaseName+"] "+DBConnection.getInstance().getErrorMsg());
            }
            HibernateUtil.getCurrentSession();
            AppPreferences.getRemote().load(); // Loads prefs from the database
            log.info("Loaded preferences");
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
