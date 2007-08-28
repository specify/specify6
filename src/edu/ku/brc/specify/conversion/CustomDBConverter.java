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
/**
 * 
 */
package edu.ku.brc.specify.conversion;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ExpressSearchSQLAdjuster;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 */
public class CustomDBConverter implements CustomDBConverterListener
{
    private static final Logger          log            = Logger.getLogger(CustomDBConverter.class);
    /**
     * 
     */
    public CustomDBConverter()
    {
        // TODO Auto-generated constructor stub
    }
    public void loggedIn(final String databaseNameArg, final String userNameArg)
    {
        log.debug("loggedIn");
    }
    /**
     * Setup all the System properties. This names all the needed factories. 
     */
    public void setUpSystemProperties()
    {
        // Name factories
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences
        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");            // Needed By UIRegistry
        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System
        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and logging transactions
        System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI
        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory");
        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");    // Needed for CatalogNumberign
        System.setProperty(ExpressSearchSQLAdjuster.factoryName,        "edu.ku.brc.specify.dbsupport.SpecifyExpressSearchSQLAdjuster");    // Needed for ExpressSearch        
    } 
    
    public void setUpPreferrences()
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir()); 
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#cancelled()
     */
    public void cancelled()
    {
        System.exit(0);
    }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

}
