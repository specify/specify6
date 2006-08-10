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

import java.util.Hashtable;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;

import edu.ku.brc.af.prefs.AppPrefsMgr;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.UIHelper;
import edu.ku.brc.specify.SpecifyAppPrefs;
import edu.ku.brc.specify.config.AppContextMgr;
import edu.ku.brc.specify.config.Discipline;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.ui.DBObjDialogFactory;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.util.FileCache;

public class AppContextTests extends TestCase
{
    private static final Logger log = Logger.getLogger(AppContextTests.class);
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp()
    {
        //-----------------------------------------------------
        // This is needed for loading views
        //-----------------------------------------------------
        UICacheManager.getInstance(); // initializes it first thing
        if (UICacheManager.getAppName() == null) // this is needed because the setUp gets run separately for each test
        {
            UICacheManager.setAppName("Specify");
    
            UICacheManager.setAppPrefs(AppPrefsMgr.getInstance().load(UICacheManager.getDefaultWorkingPath()));
            SpecifyAppPrefs.initialPrefs();
    
            FileCache.setDefaultPath(UICacheManager.getDefaultWorkingPath());
    
            UICacheManager.setViewbasedFactory(DBObjDialogFactory.getInstance());
        }
    }
    
    public void testAppContext()
    {
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
        
        CatalogSeries cs = AppContextMgr.getInstance().setupCurrentCatalogSeries(user, false); // false means don't ask if you already have one
        assertNotNull(cs);
        
        log.info("Selected CatalogSeries: ["+cs.getSeriesName()+"]");
        
        cs = AppContextMgr.getInstance().setupCurrentCatalogSeries(user, true); // false means don't ask if you already have one
        assertNotNull(cs);
        
        log.info("Selected CatalogSeries: ["+cs.getSeriesName()+"]");
        
        CollectionObjDef cod = AppContextMgr.getInstance().setupCurrentColObjDef(cs, false); // false means don't ask if you already have one
        assertNotNull(cod);
        
        log.info("Selected CollectionObjDef: ["+cod.getName()+"]");
        
        cod = AppContextMgr.getInstance().setupCurrentColObjDef(cs, true); // false means don't ask if you already have one
        assertNotNull(cod);
        
        log.info("Selected CollectionObjDef: ["+cod.getName()+"]");
        
    }
}
