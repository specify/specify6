package edu.ku.brc.specify.tests;

import java.awt.Color;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.conversion.FishConversion;
import edu.ku.brc.specify.conversion.GenericDBConversion;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.User;
import edu.ku.brc.specify.dbsupport.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * Tests the Preferences and Preferences cache
 * 
 * @author rods
 *
 */
public class DBSchemaTest extends TestCase
{
    private static Log log = LogFactory.getLog(DBSchemaTest.class);
    
    static {
        DBConnection.setUsernamePassword("rods", "rods");
        DBConnection.setDriver("com.mysql.jdbc.Driver");
        DBConnection.setDBName("jdbc:mysql://localhost/demo_fish3");
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp()
    {

    }

    /**
     * Clean All the tables (Remove all their records)
     */
    public void testRemoveAll()
    {
        log.info("Removed All Records from All the Tables.");
        
        BasicSQLUtils.cleanAllTables();
    }

    /**
     * 
     */
    public void testCreateDataType()
    {
        log.info("Create Data Type");
        GenericDBConversion conversion = new GenericDBConversion();
        DataType            dataType  = conversion.createDataTypes("Animal"); 
        assertNotNull(dataType);
    }

    /**
     * 
     */
    public void testCreateUser()
    {
        log.info("Create User");
        GenericDBConversion conversion = new GenericDBConversion();
        User                user      = conversion.createNewUser("rods", "rods", 0);
        assertNotNull(user);
    }

    /**
     * 
     */
    public void testCreateAgent()
    {
        log.info("Create Agent");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            // Create Collection Object Definition
            Agent agent = new Agent();
            agent.setAbbreviation("JD");
            agent.setAgentId(0);
            agent.setAgentType((byte)0);
            agent.setMiddleInitial("A");
            agent.setName("John");
            agent.setLastName("Doe");
            agent.setTitle("Mr.");
            agent.setTimestampCreated(new Date());
            agent.setTimestampModified(new Date());
            
            session.save(agent);
            
            HibernateUtil.commitTransaction();

            assertTrue(true);
            
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue(false);
        }

    }

    /**
     * 
     */
    public void testGeographyLocality()
    {
        log.info("Create Geography and Locality");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            // Create Collection Object Definition
            Geography geo = new Geography();
            geo.setGeographyId(0);
            //geo.setCountry("USA");
            //geo.setContinentOrOcean("North America");
            //geo.setState("KS");
            geo.setTimestampCreated(new Date());
            geo.setTimestampModified(new Date());
            session.save(geo);
            
            Locality locality = new Locality();
            locality.setLocalityId(0);
            locality.setLocalityName("This is the place.");
            locality.setGeography(geo);
            locality.setTimestampCreated(new Date());
            locality.setTimestampModified(new Date());
            session.save(locality);
            
            HibernateUtil.commitTransaction();

            assertTrue(true);
            
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue(false);
        }

    }

    /**
     * 
     */
    public void testCollectionObjDef()
    {
        log.info("Create CollectionObjDef");
        try
        {
            
            // Find User
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(User.class);
            java.util.List list = criteria.list();
            User user = (User)list.get(0);
            
            // Find Data Type
            criteria = HibernateUtil.getCurrentSession().createCriteria(DataType.class);
            list = criteria.list();
            DataType dataType = (DataType)list.get(0);
            
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            // Create Collection Object Definition
            CollectionObjDef colObjDef = new CollectionObjDef();
            colObjDef.setName("Fish");
            colObjDef.setDataType(dataType);
            colObjDef.setUser(user);
            colObjDef.setTaxonomyTreeDef(null);
            colObjDef.setCatalogSeries(new HashSet<Object>());
            colObjDef.setAttrsDefs(new HashSet<Object>());
            
            session.save(colObjDef);
            
            // Update the User to own the ColObjDef 
            Set<Object> set = new HashSet<Object>();
            set.add(colObjDef);
            user.setCollectionObjDef(set);
            session.saveOrUpdate(user);
            
            HibernateUtil.commitTransaction();

            assertTrue(true);
            
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue(false);
        }
    }

    /**
     * 
     */
    public void testCatalogSeries()
    {
        log.info("Create CatalogSeries");
        try
        {
            Criteria         criteria         = HibernateUtil.getCurrentSession().createCriteria(CollectionObjDef.class);
            java.util.List   list             = criteria.list();
            CollectionObjDef collectionObjDef = (CollectionObjDef)list.get(0);
            
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            Set<Object> colObjDefSet = new HashSet<Object>();
            colObjDefSet.add(collectionObjDef);
            
            CatalogSeries catalogSeries = new CatalogSeries();
            catalogSeries.setCatalogSeriesId(0);
            catalogSeries.setCatalogSeriesPrefix("XXX");
            catalogSeries.setCollectionObjDefItems(colObjDefSet);
            catalogSeries.setLastEditedBy(null);
            catalogSeries.setRemarks("These are the remarks");
            catalogSeries.setSeriesName("Fish Series");
            catalogSeries.setTimestampCreated(new Date());
            catalogSeries.setTimestampModified(new Date());

            // I don't think we need to do this
            //collectionObjDef.getCatalogSeries().add(catalogSeries);
            //session.save(collectionObjDef);
       
            session.save(catalogSeries);
                        
            HibernateUtil.commitTransaction();

            assertTrue(true);
            
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue(false);
        }        
    }

    /**
     * 
     */
    public void testCollectionObject()
    {
        log.info("Create CollectionObject");
        try
        {
            Criteria       criteria      = HibernateUtil.getCurrentSession().createCriteria(CatalogSeries.class);
            java.util.List list          = criteria.list();
            CatalogSeries  catalogSeries = (CatalogSeries)list.get(0);
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(Agent.class);
            list     = criteria.list();
            Agent agent = (Agent)list.get(0);
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(Locality.class);
            list     = criteria.list();
            Locality locality = (Locality)list.get(0);
            
            
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            Set<Object> collectors = new HashSet<Object>();
            collectors.add(agent);
            
            CollectingEvent colEv = new CollectingEvent();

            colEv.setCollectingEventId(0);
            colEv.setStartDate(20060101);
            colEv.setEndDate(20060102);
            colEv.setCollectors(collectors);
            colEv.setLocality(locality);
            colEv.setTimestampCreated(new Date());
            colEv.setTimestampModified(new Date());

            session.save(colEv);
            
            CollectionObject colObj = new CollectionObject();

            colObj.setCollectionObjectId(0);
            colObj.setAgent(agent);
            colObj.setCatalogedDate(20060101);
            colObj.setCatalogNumber("KSNHM121");
            colObj.setFieldNumber("Field #1");
            colObj.setDeterminations(new HashSet());
            colObj.setTimestampCreated(new Date());
            colObj.setTimestampModified(new Date());

            session.save(colObj);
            
            /*criteria = HibernateUtil.getCurrentSession().createCriteria(CollectionObject.class);
            criteria.add(Expression.idEq(0));
            list = criteria.list();
            colObj = (CollectionObject)list.get(0);
            Set determinations = colObj.getDeterminations();
            determinations.size();
            determinations = null;
            */
            
            Taxon taxon = new Taxon();
            taxon.setCommonName("darter");
            taxon.setTaxonId(0);
            taxon.setName("darterius");
            taxon.setTimestampCreated(new Date());
            taxon.setTimestampModified(new Date());
            session.save(taxon);
            
            Determination determination = new Determination();
            determination.setDeterminationId(0);
            determination.setIsCurrent(true);
            determination.setCollectionObject(colObj);
            determination.setDateField(20060101);
            determination.setDeterminer(agent);
            determination.setTaxon(taxon);
            determination.setTimestampCreated(new Date());
            determination.setTimestampModified(new Date());   
            session.save(determination);
            
            colObj.getDeterminations().add(determination);
            session.saveOrUpdate(colObj);
            
            HibernateUtil.commitTransaction();

            assertTrue(true);
            
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue(false);
        }        
    }
    
    /**
     * 
     */
    public void testCheckCollectionObject()
    {
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CollectionObject.class);
        criteria.add(Expression.idEq(0));
        java.util.List list     = criteria.list();
        
        log.info("list.size() == 1: list.size() == "+list.size());
        assertTrue(list.size() == 1);
        
        CollectionObject colObj = (CollectionObject)list.get(0);
        Set determinations = colObj.getDeterminations();
        
        log.info("determinations.size() == 1: determinations.size() == "+determinations.size());
        assertTrue(determinations.size() == 1);
        
        Determination determination = (Determination)determinations.iterator().next();
        log.info("determination.getCollectionObject().equals(colObj) -> true = "+determination.getCollectionObject().equals(colObj));
        assertTrue(determination.getCollectionObject().equals(colObj));
        
        
        
    }

    /**
     * 
     */
    public void testRemoveCollectionObject()
    {
        log.info("Remove CollectionObject");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CollectionObject.class);
            criteria.add(Expression.idEq(0));
            java.util.List list     = criteria.list();
            
            log.info("list.size() == 1: list.size() == "+list.size());
            assertTrue(list.size() == 1);
            
            CollectionObject colObj = (CollectionObject)list.get(0);
            session.delete(colObj);
            
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(Determination.class);
            list     = criteria.list();
            log.info("determination should be zero = "+list.size());
            assertTrue(list.size() == 0);
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(Locality.class);
            list     = criteria.list();
            log.info("Locality should be one = "+list.size());
            assertTrue(list.size() == 1);
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(Taxon.class);
            list     = criteria.list();
            log.info("Taxon should be one = "+list.size());
            assertTrue(list.size() == 1);
            
            HibernateUtil.commitTransaction();

            assertTrue(true);
            
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue(false);
        }
    }



}
