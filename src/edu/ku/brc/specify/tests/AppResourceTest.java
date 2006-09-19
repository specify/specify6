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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Expression;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;

/**
 * Tests the AppResources. But first make sure you run CreateTestDatabases.
 * 
 * @author rods
 *
 */
public class AppResourceTest extends TestCase
{

    private static final Logger log = Logger.getLogger(AppResourceTest.class);
    protected String databaseName = "fish";
    protected String userName = "rods";
    protected String password = "rods";
    
    protected void setUp()
    {
        //-----------------------------------------------------
        // This is needed for loading views
        //-----------------------------------------------------
        UICacheManager.getInstance(); // initializes it first thing
        if (UICacheManager.getAppName() == null) // this is needed because the setUp gets run separately for each test
        {
            System.setProperty("edu.ku.brc.af.core.AppContextMgrFactory", "edu.ku.brc.specify.config.SpecifyAppContextMgr");
            System.setProperty("AppPrefsIOClassName", "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");
            
            UICacheManager.getInstance(); // initializes it first thing
            UICacheManager.setAppName("Specify");

            // Load Local Prefs
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.setDirPath(UICacheManager.getDefaultWorkingPath());
            localPrefs.load();

            if (!UIHelper.tryLogin("com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", databaseName, "jdbc:mysql://localhost/"+databaseName, userName, password))
            {
                throw new RuntimeException("Couldn't login into ["+databaseName+"] "+DBConnection.getInstance().getErrorMsg());
            }

        }
        
    }

    
    /**
     * Make sure you first run CreateTestDatabases
     */
    public void testAppResources()
    {
        SpecifyAppContextMgr contextMgr = SpecifyAppContextMgr.getInstance();

        // First get the Specify Object

        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(SpecifyUser.class);
        criteria.add(Expression.eq("name", userName));
        List list = criteria.list();
        SpecifyUser user = (SpecifyUser)list.get(0); // assumes user is already there

        // Now get the List of CatalogSeries owned by this user
        String queryStr = "select cs From CollectionObjDef as cod Inner Join cod.specifyUser as user Inner Join cod.catalogSeries as cs where user.specifyUserId = "+user.getSpecifyUserId();
        Query query = HibernateUtil.getCurrentSession().createQuery(queryStr);
        list = query.list();
        log.info("Found "+list.size()+" CatalogSeries for User");

        // Add them into a "real" list
        List<CatalogSeries> catSeries = new ArrayList<CatalogSeries>();
        for (Object obj : list)
        {
            catSeries.add((CatalogSeries)obj);
        }

        // Set up the CatalogSeries "context" manually
        CatalogSeries.setCurrentCatalogSeries(catSeries);


        contextMgr.setContext("fish", userName, false);

        assertNotNull(contextMgr.getView("Birds Views", "CollectionObject"));

        assertNotNull(contextMgr.getView("Ento Views", "CollectionObject"));

        // Now find the CollectionObjDef for Bees (which should be owned by the user in question)
        Criteria criteria2 = HibernateUtil.getCurrentSession().createCriteria(CollectionObjDef.class);
        criteria2.add(Expression.eq("name", "Bees"));
        List list2 = criteria2.list();

        // Search by CollectionObjDef
        assertNotNull(contextMgr.getView("CollectionObject", (CollectionObjDef)list2.get(0)));

        // Now Test For the other user "Josh"
        // this should to backstops


        log.info("-------------------------------");
        userName = "josh";

        // First get the Specify Object
        criteria = HibernateUtil.getCurrentSession().createCriteria(SpecifyUser.class);
        criteria.add(Expression.eq("name", userName));
        list = criteria.list();
        user = (SpecifyUser)list.get(0); // assumes user is already there

        SpecifyUser.setCurrentUser(user);

        // Now get the List of CatalogSeries owned by this user
        queryStr = "select cs From CollectionObjDef as cod Inner Join cod.specifyUser as user Inner Join cod.catalogSeries as cs where user.specifyUserId = "+user.getSpecifyUserId();
        query = HibernateUtil.getCurrentSession().createQuery(queryStr);
        list = query.list();
        log.info("Found "+list.size()+" CatalogSeries for User");

        // Add them into a "real" list
        catSeries = new ArrayList<CatalogSeries>();
        for (Object obj : list)
        {
            catSeries.add((CatalogSeries)obj);
        }

        // Set up the CatalogSeries "context" manually
        CatalogSeries.setCurrentCatalogSeries(catSeries);


        contextMgr.setContext("fish", userName, false);

        assertNotNull(contextMgr.getView("Fish Views", "CollectionObject"));

        assertNull(contextMgr.getView("Ento Views", "CollectionObject")); // Should come back null

        // Now find the CollectionObjDef for Bees (which should be owned by the user in question)
        criteria2 = HibernateUtil.getCurrentSession().createCriteria(CollectionObjDef.class);
        criteria2.add(Expression.eq("name", "fish"));
        list2 = criteria2.list();
        assertTrue(list2.size() > 0);
        
        // Search by CollectionObjDef
        assertNotNull(contextMgr.getView("CollectionObject", (CollectionObjDef)list2.get(0)));
        
        log.info("Looking up StartUpPanel for user");
        assertNotNull(contextMgr.getResource("StartUpPanel"));

        log.info("************* System Views *********************");
        assertNotNull(contextMgr.getView("Preferences", "Formatting"));
        
        log.info("Looking up DialogDefs");
        assertNotNull(contextMgr.getResource("DialogDefs"));

    
    }
}
