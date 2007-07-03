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

import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.ui.UIRegistry;

public class AppContextTests extends TestCase
{
    //private static final Logger log = Logger.getLogger(AppContextTests.class);
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp()
    {
        //-----------------------------------------------------
        // This is needed for loading views
        //-----------------------------------------------------
        UIRegistry.getInstance(); // initializes it first thing
        if (UIRegistry.getAppName() == null) // this is needed because the setUp gets run separately for each test
        {
            System.setProperty("edu.ku.brc.af.core.AppContextMgrFactory", "edu.ku.brc.specify.config.SpecifyAppContextMgr");
            System.setProperty("AppPrefsIOClassName", "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");
            
            UIRegistry.getInstance(); // initializes it first thing
            UIRegistry.setAppName("Specify");

            // Load Local Prefs
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.setDirPath(UIRegistry.getAppDataDir());
            localPrefs.load();
        }
        
    }

    
    public void testAppContextSnigleCollection()
    {
        /*
        String databaseName  = "fish";
        String userName      = "rods";
        String catSeriesName = "Bees";
        
        List<Collection> collectionList = new ArrayList<Collection>();
        
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Collection.class);
        criteria.add(Expression.eq("collectionName", catSeriesName));
        List list = criteria.list();
        if (list.size() == 1)
        {
            collectionList.add((Collection)list.get(0));
            
        } else
        {
            throw new RuntimeException("Problems with Collection["+catSeriesName+"] for user["+userName+"]");
        }
        
        Collection.setCurrentCollection(collectionList);
        
        assertTrue(SpecifyAppContextMgr.getInstance().setContext(databaseName, userName, false) == AppContextMgr.CONTEXT_STATUS.OK);
        

        
        // These are taken from the disciplines.xml file
        Hashtable<Integer, String> hash = new Hashtable<Integer, String>();
        hash.put(0, "Collection");
        hash.put(1, "Tissues");
        hash.put(2, "Accessions");

        log.info("List Disciplines:");
        for (Discipline d : AppContextMgr.getInstance().getDisciplines())
        {
            log.info("[" + hash.get(d.getType()) + "][" + d.getName() + "][" + d.getTitle() + "]");
        }
        
        assertNotNull(AppContextMgr.getInstance().getByTitle("Mammal"));
        assertNotNull(AppContextMgr.getInstance().get("accessions"));
        

        assertTrue(UIHelper.tryLogin("com.mysql.jdbc.Driver",
                                     "org.hibernate.dialect.MySQLDialect", 
                                     "fish", 
                                     "jdbc:mysql://localhost/fish", "rods", "rods"));
        
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(SpecifyUser.class);
        List     list     = criteria.list();
        assertTrue(list.size() > 0);

        criteria = HibernateUtil.getCurrentSession().createCriteria(SpecifyUser.class);
        criteria.add(Expression.eq("name", "rods"));
        list     = criteria.list();
        assertTrue(list.size() == 1);
        
        SpecifyUser user = (SpecifyUser)list.get(0);
        
        Collection cs = AppContextMgr.getInstance().setupCurrentCollection(user, false); // false means don't ask if you already have one
        assertNotNull(cs);
        
        log.info("Selected Collection: ["+cs.getSeriesName()+"]");
        
        cs = AppContextMgr.getInstance().setupCurrentCollection(user, true); // false means don't ask if you already have one
        assertNotNull(cs);
        
        log.info("Selected Collection: ["+cs.getSeriesName()+"]");
        
        CollectionObjDef cod = AppContextMgr.getInstance().setupCurrentColObjDef(cs, false); // false means don't ask if you already have one
        assertNotNull(cod);
        
        log.info("Selected CollectionObjDef: ["+cod.getName()+"]");
        
        cod = AppContextMgr.getInstance().setupCurrentColObjDef(cs, true); // false means don't ask if you already have one
        assertNotNull(cod);
        
        log.info("Selected CollectionObjDef: ["+cod.getName()+"]");
        */
    }
}
