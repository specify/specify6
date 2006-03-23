package edu.ku.brc.specify.tests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;

import edu.ku.brc.specify.conversion.GenericDBConversion;
import edu.ku.brc.specify.conversion.IdMapper;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AttributeIFace;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttr;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.User;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.dbsupport.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;

import static edu.ku.brc.specify.tests.ObjCreatorHelper.*;

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
    
    protected Calendar startCal = Calendar.getInstance();


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
        return getDBObject(classObj, 0);
    }

    /**
     * Retuturns the first item from a table
     * @param classObj the class of the item to get
     * @return null if no items in table
     */
    public Object getDBObject(Class classObj, final int index)
    {
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(classObj);
        java.util.List list = criteria.list();
        if (list.size() == 0) return null;

        return list.get(index);
    }

    /**
     * Clean All the tables (Remove all their records)
     */
    public void XtestIdMapper()
    {
        log.info("Testing IdMapper");

        DBConnection oldDB     = DBConnection.createInstance("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/demo_fish2", "rods", "rods");
        
        try
        {
            IdMapper  idMapper = new IdMapper(oldDB.getConnectionToDB(), "collectionobject", "CollectionObjectID");
            Statement stmt     = oldDB.getConnectionToDB().createStatement();
            ResultSet rs       = stmt.executeQuery("select CollectionObjectID from collectionobject limit 0,100");
            int       newInx   = 1;
            while (rs.next())
            {
                idMapper.addIndex(newInx++, rs.getInt(1));
            }
            rs.close();
            stmt.close();
            
            int oldInx = -2135666521;
            newInx = idMapper.getNewIndexFromOld(oldInx);
            log.info("New Index ["+newInx+"] for old ["+oldInx+"]");
            assertTrue(newInx == 100);
            
            idMapper.cleanup();
            
            // Now Test Memory Approach
            idMapper = new IdMapper(oldDB.getConnectionToDB(), "accession", "AccessionID");
            stmt     = oldDB.getConnectionToDB().createStatement();
            rs       = stmt.executeQuery("select AccessionID from accession limit 0,100");
            newInx   = 1;
            while (rs.next())
            {
                idMapper.addIndex(newInx++, rs.getInt(1));
            }
            rs.close();
            stmt.close();
            
            oldInx = 74;
            newInx = idMapper.getNewIndexFromOld(oldInx);
            log.info("New Index ["+newInx+"] for old ["+oldInx+"]");
            assertTrue(newInx == 8);
            
            idMapper.cleanup();
            
        } catch (SQLException ex)
        {
            log.error(ex);
            assertTrue(false);
        }
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
        log.info("Create Agents");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            // Create Collection Object Definition
            createAgent("Mr.","Charles","A","Darwin","CD");
            createAgent("Mr.","Louis","","Agassiz","AL");

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

    public void testGeography()
    {
        log.info("Create GeographyTreeDef, GeographyTreeDefItem and Geography objects");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();
            
            // Create a geography tree definition
            GeographyTreeDef geoTreeDef = createGeographyTreeDef("GeographyTreeDef for DBSchemaTest");
            GeographyTreeDefItem planet = createGeographyTreeDefItem(null, geoTreeDef, "Planet", 0);
            GeographyTreeDefItem cont   = createGeographyTreeDefItem(planet, geoTreeDef, "Continent", 100);
            
            GeographyTreeDefItem country = createGeographyTreeDefItem(cont, geoTreeDef, "Country", 200);
            GeographyTreeDefItem state   = createGeographyTreeDefItem(country, geoTreeDef, "Country", 300);
            GeographyTreeDefItem county  = createGeographyTreeDefItem(state, geoTreeDef, "County", 400);
            
            // Create the planet Earth.
            // That seems like a big task for 5 lines of code.
            Geography earth        = createGeography(geoTreeDef, null, "Earth", planet.getRankId());
            Geography northAmerica = createGeography(geoTreeDef, earth, "North America", cont.getRankId());
            Geography us           = createGeography(geoTreeDef, northAmerica, "United States", country.getRankId());
           
            // Create Kansas and a few counties
            Geography ks = createGeography(geoTreeDef, us, "Kansas", state.getRankId());
            Geography douglas = createGeography(geoTreeDef, ks, "Douglas", county.getRankId());
            Geography johnson = createGeography(geoTreeDef, ks, "Johnson", county.getRankId());
            Geography sedgwick = createGeography(geoTreeDef, ks, "Sedgwick", county.getRankId());
            
            // Create Iowa 
            Geography iowa      = createGeography(geoTreeDef, us, "Iowa", state.getRankId());
            Geography blackhawk = createGeography(geoTreeDef, iowa, "Blackhawk", county.getRankId());
            Geography fayette   = createGeography(geoTreeDef, iowa, "Fayette", county.getRankId());
            Geography polk      = createGeography(geoTreeDef, iowa, "Polk", county.getRankId());
            
            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();
            
            assertTrue(true);
        }
        catch( Exception ex )
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue(false);
        }
    }
   
    public void testLocation()
    {
        log.info("Create LocationTreeDef, LocationTreeDefItem and Location objects");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();
            
            // Create a geography tree definition
            LocationTreeDef locTreeDef = createLocationTreeDef("LocationTreeDef for DBSchemaTest");
            LocationTreeDefItem building = createLocationTreeDefItem(null, locTreeDef, "building", 0);
            LocationTreeDefItem room = createLocationTreeDefItem(building, locTreeDef, "building", 100);
            LocationTreeDefItem freezer = createLocationTreeDefItem(room, locTreeDef, "freezer", 200);
            LocationTreeDefItem shelf = createLocationTreeDefItem(freezer, locTreeDef, "shelf", 300);
            
            // Create the building
            Location dyche = createLocation(locTreeDef, null, "Dyche Hall", building.getRankId());
            Location rm606 = createLocation(locTreeDef, dyche, "Room 606", room.getRankId());
            Location freezerA = createLocation(locTreeDef, rm606, "Freezer A", freezer.getRankId());
            Location shelf5 = createLocation(locTreeDef, freezerA, "Shelf 5", shelf.getRankId());
            Location shelf4 = createLocation(locTreeDef, freezerA, "Shelf 4", shelf.getRankId());
            Location shelf3 = createLocation(locTreeDef, freezerA, "Shelf 3", shelf.getRankId());
            Location shelf2 = createLocation(locTreeDef, freezerA, "Shelf 2", shelf.getRankId());
            Location shelf1 = createLocation(locTreeDef, freezerA, "Shelf 1", shelf.getRankId());

            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();
            
            assertTrue(true);
        }
        catch( Exception ex )
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue(false);
        }       
    }

    public void testTaxon()
    {
        log.info("Create TaxonTreeDef, TaxonTreeDefItem and Taxon objects");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            // Create a geography tree definition
            TaxonTreeDef taxonTreeDef = createTaxonTreeDef("TaxonTreeDef for DBSchemaTest");
            TaxonTreeDefItem defItemLevel0 = createTaxonTreeDefItem(null, taxonTreeDef, "order", 0);
            TaxonTreeDefItem defItemLevel1 = createTaxonTreeDefItem(defItemLevel0, taxonTreeDef, "family", 100);
            TaxonTreeDefItem defItemLevel2 = createTaxonTreeDefItem(defItemLevel1, taxonTreeDef, "genus", 200);
            TaxonTreeDefItem defItemLevel3 = createTaxonTreeDefItem(defItemLevel2, taxonTreeDef, "species", 300);
            
            // Create the defItemLevel0
            Taxon level0 = createTaxon(taxonTreeDef, null, "Percidae", defItemLevel0.getRankId());
            Taxon level1 = createTaxon(taxonTreeDef, level0, "Perciformes", defItemLevel1.getRankId());
            Taxon level2 = createTaxon(taxonTreeDef, level1, "Ammocrypta", defItemLevel2.getRankId());
            
            Taxon level3 = createTaxon(taxonTreeDef, level2, "beanii", defItemLevel3.getRankId());
            level3 = createTaxon(taxonTreeDef, level2, "beanii2", defItemLevel3.getRankId());
            level3 = createTaxon(taxonTreeDef, level2, "beanii3", defItemLevel3.getRankId());
            level3 = createTaxon(taxonTreeDef, level2, "beanii4", defItemLevel3.getRankId());
            level3 = createTaxon(taxonTreeDef, level2, "beaniis5", defItemLevel3.getRankId());
            level3 = createTaxon(taxonTreeDef, level2, "beanii6", defItemLevel3.getRankId());
            
            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();
            
            assertTrue(true);
        }
        catch( Exception ex )
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
            assertTrue(false);
        }       
    }

    public void testGeologicTimePeriod()
    {
        log.info("Create GeologicTimePeriodTreeDef, GTPTreeDefItem and GTP objects");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            // Create a geography tree definition
            GeologicTimePeriodTreeDef treeDef = new GeologicTimePeriodTreeDef();
            session.save(treeDef);
            treeDef.setName("GeologicTimePeriodTreeDef for DBSchemaTest");
            treeDef.setRemarks("A GeologicTimePeriod tree def for use in the DB testing");
            
            GeologicTimePeriodTreeDefItem defItemLevel0 = new GeologicTimePeriodTreeDefItem();
            session.save(defItemLevel0);
            defItemLevel0.setName("Level 0");
            defItemLevel0.setRankId(0);
            
            GeologicTimePeriodTreeDefItem defItemLevel1 = new GeologicTimePeriodTreeDefItem();
            session.save(defItemLevel1);
            defItemLevel1.setName("Level 1");
            defItemLevel1.setRankId(100);
            
            GeologicTimePeriodTreeDefItem defItemLevel2 = new GeologicTimePeriodTreeDefItem();
            session.save(defItemLevel2);
            defItemLevel2.setName("Level 2");
            defItemLevel2.setRankId(200);
            
            GeologicTimePeriodTreeDefItem defItemLevel3 = new GeologicTimePeriodTreeDefItem();
            session.save(defItemLevel3);
            defItemLevel3.setName("Level 3");
            defItemLevel3.setRankId(300);
            
            defItemLevel3.setParent(defItemLevel2);
            defItemLevel2.setParent(defItemLevel1);
            defItemLevel1.setParent(defItemLevel0);
            
            defItemLevel0.setTreeDef(treeDef);
            defItemLevel1.setTreeDef(treeDef);
            defItemLevel2.setTreeDef(treeDef);
            defItemLevel3.setTreeDef(treeDef);
            
            // Create the defItemLevel0
            GeologicTimePeriod level0 = new GeologicTimePeriod();
            level0.setName("Time As We Know It");
            level0.setRankId(defItemLevel0.getRankId());
            level0.setTreeDef(treeDef);
            session.save(level0);
            
            GeologicTimePeriod level1 = new GeologicTimePeriod();
            level1.setRankId(defItemLevel1.getRankId());
            level1.setName("Some Really Big Time Period");
            level1.setTreeDef(treeDef);
            session.save(level1);
            level1.setParent(level0);
            
            GeologicTimePeriod level2 = new GeologicTimePeriod();
            level2.setRankId(defItemLevel2.getRankId());
            level2.setName("A Slightly Smaller Time Period");
            level2.setTreeDef(treeDef);
            session.save(level2);
            level2.setParent(level1);
            
            GeologicTimePeriod level3 = new GeologicTimePeriod();
            level3.setRankId(defItemLevel3.getRankId());
            level3.setName("Yesterday");
            level3.setTreeDef(treeDef);
            session.save(level3);
            level3.setParent(level2);
            
            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();
            
            assertTrue(true);
        }
        catch( Exception ex )
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
    public void testLocality()
    {
        log.info("Create Locality");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            session.save(createLocality("This is the place", (Geography)getDBObject(Geography.class, 6)));
            session.save(createLocality("My Private Forest", (Geography)getDBObject(Geography.class, 10)));
            
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
            setSession(session);
            HibernateUtil.beginTransaction();

            // Create Collection Object Definition
            CollectionObjDef colObjDef = createCollectionObjDef("Fish", dataType, user);
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
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            
            CollectionObjDef collectionObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);
            assertNotNull(collectionObjDef);

            HibernateUtil.beginTransaction();

            Set<Object> colObjDefSet = new HashSet<Object>();
            colObjDefSet.add(collectionObjDef);

            CatalogSeries catalogSeries = createCatalogSeries("KUFSH", "Fish");
            catalogSeries.setCollectionObjDefItems(colObjDefSet);

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
            CatalogSeries catalogSeries = (CatalogSeries)getDBObject(CatalogSeries.class);
            assertNotNull(catalogSeries);

            CollectionObjDef colObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);
            assertNotNull(colObjDef);

            Agent Darwin = (Agent)getDBObject(Agent.class);
            assertNotNull(Darwin);
            
            Agent Agassiz = (Agent)getDBObject(Agent.class, 1);
            assertNotNull(Agassiz);

            Locality locality = (Locality)getDBObject(Locality.class);
            assertNotNull(locality);

            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            // Create Collecting Event
            CollectingEvent colEv = createCollectingEvent(locality, 
                    new Collector[] {createCollector(Darwin, 0), createCollector(Agassiz, 1)});

            // Create AttributeDef for Collecting Event
            AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", null);

            // Create CollectingEventAttr
            CollectingEventAttr cevAttr = createCollectingEventAttr(colEv, cevAttrDef, "Clinton Park", null);

            // Create Collection Object
            CollectionObject colObj1  = createCollectionObject(1601010.1f, "RCS101", null, Darwin,  catalogSeries, colObjDef, 5);
            CollectionObject colObj2 = createCollectionObject(1701011.1f, "RCS102", null, Agassiz, catalogSeries, colObjDef, 20);
            CollectionObject colObj3 = createCollectionObject(1801012.1f, "RCS103", null, Darwin, catalogSeries, colObjDef, 35);

            // Create AttributeDef for Collection Object
            AttributeDef colObjAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "MoonPhase", null);

            // Create CollectionObjectAttr
            CollectionObjectAttr colObjAttr = createCollectionObjectAttr(colObj1, colObjAttrDef, "Full", null);

            // Create Taxon Object
            Taxon t3 = (Taxon)getDBObject(Taxon.class, 3);
            Taxon t4 = (Taxon)getDBObject(Taxon.class, 4);
            Taxon t5 = (Taxon)getDBObject(Taxon.class, 5);
            Taxon t6 = (Taxon)getDBObject(Taxon.class, 6);
            Taxon t7 = (Taxon)getDBObject(Taxon.class, 7);
            Taxon t8 = (Taxon)getDBObject(Taxon.class, 8);
 
            // Create Determination
            Determination determination = createDetermination(colObj1, Darwin, t3, true);
            determination = createDetermination(colObj1, Darwin, t4, false);

            determination = createDetermination(colObj2, Darwin, t3, true);
            determination = createDetermination(colObj2, Darwin, t7, false);

            determination = createDetermination(colObj3, Agassiz, t7, true);
            determination = createDetermination(colObj3, Agassiz, t8, false);

            // Create Preparation Type
            PrepType prepType = createPrepType("Skeleton");

          
            Location location = (Location)getDBObject(Location.class, 6); // Shelf 2
            log.info("Location: "+location.getName());

            // Create Preparation
            Preparation prep = createPreparation(prepType, Darwin, colObj1, location, 10);
            prep = createPreparation(prepType, Agassiz, colObj1, location, 33);
            
            prep = createPreparation(prepType, Darwin, colObj2, location, 23);

            
            // Create AttributeDef for Preparation
            AttributeDef prepAttrDef = createAttributeDef(AttributeIFace.FieldType.IntegerType, "Length", prepType);

            // Create PreparationAttr
            PreparationAttr prepAttr = createPreparationAttr(prepAttrDef, prep, null, 100.0);

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
    public void XtestCheckCollectionObject()
    {
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CollectionObject.class);
        //criteria.add(Expression.idEq(1));
        java.util.List list     = criteria.list();

        log.info("list.size() == 1: list.size() == "+list.size());
        assertTrue(list.size() == 1);

        CollectionObject colObj = (CollectionObject)list.get(0);
        Set determinations = colObj.getDeterminations();

        log.info("determinations.size() == 2: determinations.size() == "+determinations.size());
        assertTrue(determinations.size() == 1);

        Determination determination = (Determination)determinations.iterator().next();
        log.info("determination.getCollectionObject().equals(colObj) -> true = "+determination.getCollectionObject().equals(colObj));
        assertTrue(determination.getCollectionObject().equals(colObj));



    }

    /**
     *
     */
    public void XtestRemoveCollectionObject()
    {
        log.info("Remove CollectionObject");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CollectionObject.class);
            //criteria.add(Expression.idEq(0));
            java.util.List list     = criteria.list();

            log.info("CollectionObject list.size() == 2: list.size() == "+list.size());
            assertTrue(list.size() == 2);

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
            log.info("Taxon should be 5 = "+list.size());
            assertTrue(list.size() == 5);

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

