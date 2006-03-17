package edu.ku.brc.specify.tests;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import edu.ku.brc.specify.conversion.GenericDBConversion;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AttributeIFace;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttr;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.User;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.dbsupport.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;

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
     * Retuturns the first item from a table
     * @param classObj the class of the item to get
     * @return null if no items in table
     */
    public Object getDBObject(Class classObj)
    {
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(classObj).setFirstResult(0).setMaxResults(1);
        java.util.List list = criteria.list();
        if (list.size() == 0) return null;
        
        return list.get(0);
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
        
        UserGroup userGroup = conversion.createUserGroup("Fish");
        assertNotNull(userGroup);
        
        User user = conversion.createNewUser(userGroup, "rods", "rods", (short)0);
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
    public void testCreateGeographyLocality()
    {
        log.info("Create Geography and Locality");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            // Create Collection Object Definition
            Geography geo = new Geography();
            geo.setTreeId(0);
            geo.setDefinition(null);
            geo.setHighestChildNodeNumber(0);
            geo.setCurrent(false);
            geo.setLastEditedBy("");
            geo.setLocalities(new HashSet<Object>());
            geo.setAbbrev("KS");
            geo.setName("Kansas");
            geo.setNodeNumber(0);
            geo.setRankId(0);
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
    public void testCreateCollectionObjDef()
    {
        log.info("Create CollectionObjDef");
        try
        {
            
            // Find User
            User user = (User)getDBObject(User.class);
            assertNotNull(user);
            
            // Find Data Type
            DataType dataType = (DataType)getDBObject(DataType.class);
            assertNotNull(dataType);
            
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            // Create Collection Object Definition
            CollectionObjDef colObjDef = new CollectionObjDef();
            colObjDef.setName("Fish");
            colObjDef.setDataType(dataType);
            colObjDef.setUser(user);
            colObjDef.setTaxonTreeDef(null);
            colObjDef.setCatalogSeries(new HashSet<Object>());
            colObjDef.setAttributeDefs(new HashSet<Object>());
            
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
    public void testCreateCatalogSeries()
    {
        log.info("Create CatalogSeries");
        try
        {
            Session          session          = HibernateUtil.getCurrentSession();
            CollectionObjDef collectionObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);
            assertNotNull(collectionObjDef);
            
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
            CatalogSeries  catalogSeries = (CatalogSeries)getDBObject(CatalogSeries.class);
            assertNotNull(catalogSeries);
            
            Agent agent = (Agent)getDBObject(Agent.class);
            assertNotNull(agent);
            
            Locality locality = (Locality)getDBObject(Locality.class);
            assertNotNull(locality);
            
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            Set<Object> collectors = new HashSet<Object>();
            //collectors.add(agent);
            
            // Create Collecting Event
            CollectingEvent colEv = new CollectingEvent();

            Calendar startCal = Calendar.getInstance();
            startCal.clear();
            startCal.set(2006, 0, 1);
            colEv.setCollectingEventId(0);
            colEv.setStartDate(startCal);
            
            Calendar endCal = Calendar.getInstance();
            startCal.clear();
            startCal.set(2006, 0, 2);   
            colEv.setEndDate(startCal);
            colEv.setAttrs(new HashSet<Object>());
            colEv.setCollectors(collectors);
            colEv.setLocality(locality);
            colEv.setTimestampCreated(new Date());
            colEv.setTimestampModified(new Date());

            session.save(colEv);
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();

            
            // Create AttributeDef for Collecting Event
            AttributeDef cevAttrDef = new AttributeDef();
            cevAttrDef.setDataType(AttributeIFace.FieldType.StringType.getType());
            cevAttrDef.setFieldName("ParkName");
            cevAttrDef.setPrepType(null);
            
            session.saveOrUpdate(cevAttrDef);
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();

            // Create CollectingEventAttr
            CollectingEventAttr cevAttr = new CollectingEventAttr();
            cevAttr.setDblValue(null);
            cevAttr.setDefinition(cevAttrDef);
            cevAttr.setCollectingEvent(colEv);
            cevAttr.setStrValue("Clinton Park");
            cevAttr.setTimestampCreated(new Date());
            cevAttr.setTimestampModified(new Date());
            
            colEv.getAttrs().add(cevAttr);
            
            session.saveOrUpdate(cevAttr);
            session.saveOrUpdate(colEv);
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();

            // Create Collection Object
            CollectionObject colObj = new CollectionObject();
            colObj.setAccession(null);
            colObj.setAttrs(new HashSet());
            colObj.setCataloger(agent);
            colObj.setCatalogedDate(startCal);
            colObj.setCatalogedDateVerbatim("Sometime this year");
            colObj.setCatalogNumber(1101010.1f);
            colObj.setCatalogSeries(catalogSeries);
            colObj.setCollectionObjectCitations(new HashSet<Object>());
            colObj.setCollectionObjectId(0);
            colObj.setContainer(null);
            colObj.setContainerItem(null);
            colObj.setCountAmt(20);
            colObj.setDeaccessionCollectionObjects(new HashSet<Object>());
            colObj.setDeaccessioned(false);
            colObj.setDescription("This is the description");
            colObj.setDeterminations(new HashSet());
            colObj.setExternalResources(new HashSet());
            colObj.setFieldNumber("Field #1");
            colObj.setGuid("This is the GUID");
            colObj.setLastEditedBy("rods");
            colObj.setModifier("modifier");
            colObj.setName("The Name!!!!!!");
            colObj.setPreparations(new HashSet<Object>());
            colObj.setProjectCollectionObjects(new HashSet<Object>());
            colObj.setRemarks("These are the remarks");
            colObj.setYesNo1(false);
            colObj.setYesNo2(true);
            
            colObj.setTimestampCreated(new Date());
            colObj.setTimestampModified(new Date());

            session.saveOrUpdate(colObj);
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();

            // Create AttributeDef for Collection Object
            AttributeDef colObjAttrDef = new AttributeDef();
            colObjAttrDef.setDataType(AttributeIFace.FieldType.StringType.getType());
            colObjAttrDef.setFieldName("MoonPhase");
            colObjAttrDef.setPrepType(null);
            
            session.saveOrUpdate(colObjAttrDef);
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();

            // Create CollectionObjectAttr
            CollectionObjectAttr colObjAttr = new CollectionObjectAttr();
            colObjAttr.setDblValue(null);
            colObjAttr.setDefinition(colObjAttrDef);
            colObjAttr.setCollectionObject(colObj);
            colObjAttr.setStrValue("Full");
            colObjAttr.setTimestampCreated(new Date());
            colObjAttr.setTimestampModified(new Date());
            
            session.saveOrUpdate(colObjAttr);

            colObj.getAttrs().add(colObjAttr);
            session.saveOrUpdate(colObj);
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();

            // Create Taxon Object
            Taxon taxon = new Taxon();
            taxon.setCommonName("darter");
            taxon.setTreeId(0);
            taxon.setName("darterius");
            taxon.setTimestampCreated(new Date());
            taxon.setTimestampModified(new Date());
            session.save(taxon);
            
            // Create Determination
            Determination determination = new Determination();
            determination.setDeterminationId(0);
            determination.setIsCurrent(true);
            determination.setCollectionObject(colObj);
            determination.setDeterminedDate(startCal);
            determination.setDeterminer(agent);
            determination.setTaxon(taxon);
            determination.setTimestampCreated(new Date());
            determination.setTimestampModified(new Date());   
            session.save(determination);
            
            colObj.getDeterminations().add(determination);
            session.saveOrUpdate(colObj);
            
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();
            
            // Create Preparation Type
            PrepType prepType = new PrepType();
            prepType.setName("Skeleton");
            prepType.setPreparations(new HashSet<Object>());
            prepType.setAttributeDefs(new HashSet<Object>());
            session.saveOrUpdate(prepType);
            
            Location location = null;//new Location();
            /*location.setTreeId(0);
            location.setDefinition(null);
            location.setAbbrev("XX");
            location.setIsCurrent((short)0);
            location.setParent(null);
            session.saveOrUpdate(location);*/
            
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();

            session.update(colObj);
            session.update(agent);
            session.update(prepType);
            
            // Create Preparation
            Preparation prep = new Preparation();
            prep.setAttrs(new HashSet<Object>());
            prep.setCollectionObject(colObj);
            prep.setCount(10);
            prep.setExternalResources(new HashSet<Object>());
            prep.setLastEditedBy("Rod");
            prep.setLoanPhysicalObjects(new HashSet<Object>());
            prep.setLocation(location);
            prep.setPreparedByAgent(agent);
            prep.setPreparedDate(Calendar.getInstance());
            prep.setPrepType(prepType);
            prep.setRemarks("These are the remarks");
            prep.setStorageLocation("This is the textual storage location");
            prep.setText1("Thi is text1");
            prep.setText2("This is text2");
            prep.setTimestampCreated(new Date());
            prep.setTimestampModified(new Date());
            
            session.saveOrUpdate(prep);
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();

            colObj.getPreparations().add(prep);
            session.saveOrUpdate(colObj);
            
            session.update(prepType);
            session.update(prep);
            
            // Create AttributeDef for Preparation
            AttributeDef prepAttrDef = new AttributeDef();
            prepAttrDef.setDataType(AttributeIFace.FieldType.IntegerType.getType());
            prepAttrDef.setFieldName("Length");
            prepAttrDef.setPrepType(prepType);
            
            session.saveOrUpdate(prepAttrDef);
            //HibernateUtil.commitTransaction();
            
            //HibernateUtil.beginTransaction();

            // Create PreparationAttr
            PreparationAttr prepAttr = new PreparationAttr();
            prepAttr.setDblValue(100.0);
            prepAttr.setDefinition(prepAttrDef);
            prepAttr.setPreparation(prep);
            prepAttr.setStrValue(null);
            prepAttr.setTimestampCreated(new Date());
            prepAttr.setTimestampModified(new Date());
            
            session.saveOrUpdate(prepAttr);
            
            prep.getAttrs().add(prepAttr);
            session.saveOrUpdate(prep);

            HibernateUtil.commitTransaction();
            
            //HibernateUtil.beginTransaction();
            //session.delete(colObj);
            //HibernateUtil.commitTransaction();

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
            
            log.info("CollectionObject list.size() == 1: list.size() == "+list.size());
            assertTrue(list.size() == 1);
            
            CollectionObject colObj = (CollectionObject)list.get(0);
            session.delete(colObj);
            
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(Determination.class);
            list     = criteria.list();
            log.info("Determination should be zero = "+list.size());
            assertTrue(list.size() == 0);
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(Preparation.class);
            list     = criteria.list();
            log.info("Preparation should be zero = "+list.size());
            assertTrue(list.size() == 0);
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(PreparationAttr.class);
            list     = criteria.list();
            log.info("PreparationAttr should be zero = "+list.size());
            assertTrue(list.size() == 0);
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(Locality.class);
            list     = criteria.list();
            log.info("Locality should be one = "+list.size());
            assertTrue(list.size() == 1);
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(Taxon.class);
            list     = criteria.list();
            log.info("Taxon should be one = "+list.size());
            assertTrue(list.size() == 1);
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(CollectingEvent.class);
            list     = criteria.list();
            log.info("CollectingEvent should be one = "+list.size());
            assertTrue(list.size() == 1);
            
            criteria = HibernateUtil.getCurrentSession().createCriteria(CollectingEventAttr.class);
            list     = criteria.list();
            log.info("CollectingEventAttr should be one = "+list.size());
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
