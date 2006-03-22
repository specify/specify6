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

    public void testGeography()
    {
        log.info("Create GeographyTreeDef, GeographyTreeDefItem and Geography objects");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            
            // Create a geography tree definition
            GeographyTreeDef geoTreeDef = new GeographyTreeDef();
            session.save(geoTreeDef);
            geoTreeDef.setName("GeographyTreeDef for DBSchemaTest");
            geoTreeDef.setRemarks("A tree def for use in the DB testing");
            
            GeographyTreeDefItem planet = new GeographyTreeDefItem();
            session.save(planet);
            planet.setName("Planet");
            planet.setRankId(0);
            
            GeographyTreeDefItem cont = new GeographyTreeDefItem();
            session.save(cont);
            cont.setName("Continent");
            cont.setRankId(100);
            
            GeographyTreeDefItem country = new GeographyTreeDefItem();
            session.save(country);
            country.setName("Country");
            country.setRankId(200);
            
            GeographyTreeDefItem state = new GeographyTreeDefItem();
            session.save(state);
            state.setName("State");
            state.setRankId(300);
            
            GeographyTreeDefItem county = new GeographyTreeDefItem();
            session.save(county);
            county.setName("County");
            county.setRankId(400);
            
            // setup parents
            county.setParent(state);
            state.setParent(country);
            country.setParent(cont);
            cont.setParent(planet);
            
            // set the tree def for each tree def item
            planet.setTreeDef(geoTreeDef);
            cont.setTreeDef(geoTreeDef);
            country.setTreeDef(geoTreeDef);
            state.setTreeDef(geoTreeDef);
            county.setTreeDef(geoTreeDef);
            
            // Create the planet Earth.
            // That seems like a big task for 5 lines of code.
            Geography earth = new Geography();
            earth.setName("Earth");
            earth.setRankId(planet.getRankId());
            earth.setTreeDef(geoTreeDef);
            session.save(earth);
            
            Geography northAmerica = new Geography();
            northAmerica.setRankId(cont.getRankId());
            northAmerica.setName("North America");
            northAmerica.setTreeDef(geoTreeDef);
            session.save(northAmerica);
            northAmerica.setParent(earth);
            
            Geography us = new Geography();
            us.setRankId(country.getRankId());
            us.setName("United States");
            us.setTreeDef(geoTreeDef);
            session.save(us);
            us.setParent(northAmerica);
            
            // Create Kansas and a few counties
            Geography ks = new Geography();
            ks.setRankId(state.getRankId());
            ks.setName("Kansas");
            ks.setTreeDef(geoTreeDef);
            session.save(ks);
            ks.setParent(us);
            
            Geography douglas = new Geography();
            douglas.setRankId(state.getRankId());
            douglas.setName("Douglas");
            douglas.setTreeDef(geoTreeDef);
            session.save(douglas);
            douglas.setParent(ks);
            
            Geography johnson = new Geography();
            johnson.setRankId(state.getRankId());
            johnson.setName("Johnson");
            johnson.setTreeDef(geoTreeDef);
            session.save(johnson);
            johnson.setParent(ks);
            
            Geography sedgwick = new Geography();
            sedgwick.setRankId(state.getRankId());
            sedgwick.setName("Sedgwick");
            sedgwick.setTreeDef(geoTreeDef);
            session.save(sedgwick);
            sedgwick.setParent(ks);
            
            // Create Iowa 
            Geography iowa = new Geography();
            iowa.setRankId(state.getRankId());
            iowa.setName("Iowa");
            iowa.setTreeDef(geoTreeDef);
            session.save(iowa);
            iowa.setParent(us);
            
            Geography blackhawk = new Geography();
            blackhawk.setRankId(state.getRankId());
            blackhawk.setName("Blackhawk");
            blackhawk.setTreeDef(geoTreeDef);
            session.save(blackhawk);
            blackhawk.setParent(iowa);
            
            Geography fayette = new Geography();
            fayette.setRankId(state.getRankId());
            fayette.setName("Fayette");
            fayette.setTreeDef(geoTreeDef);
            session.save(fayette);
            fayette.setParent(iowa);
            
            Geography polk = new Geography();
            polk.setRankId(state.getRankId());
            polk.setName("Polk");
            polk.setTreeDef(geoTreeDef);
            session.save(polk);
            polk.setParent(iowa);
            
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
            HibernateUtil.beginTransaction();
            
            // Create a geography tree definition
            LocationTreeDef locTreeDef = new LocationTreeDef();
            session.save(locTreeDef);
            locTreeDef.setName("LocationTreeDef for DBSchemaTest");
            locTreeDef.setRemarks("A location tree def for use in the DB testing");
            
            LocationTreeDefItem building = new LocationTreeDefItem();
            session.save(building);
            building.setName("building");
            building.setRankId(0);
            
            LocationTreeDefItem room = new LocationTreeDefItem();
            session.save(room);
            room.setName("room");
            room.setRankId(100);
            
            LocationTreeDefItem freezer = new LocationTreeDefItem();
            session.save(freezer);
            freezer.setName("freezer");
            freezer.setRankId(200);
            
            LocationTreeDefItem shelf = new LocationTreeDefItem();
            session.save(shelf);
            shelf.setName("shelf");
            shelf.setRankId(300);
            
            shelf.setParent(freezer);
            freezer.setParent(room);
            room.setParent(building);
            
            building.setTreeDef(locTreeDef);
            room.setTreeDef(locTreeDef);
            freezer.setTreeDef(locTreeDef);
            shelf.setTreeDef(locTreeDef);
            
            // Create the building
            Location dyche = new Location();
            dyche.setName("Dyche Hall");
            dyche.setRankId(building.getRankId());
            dyche.setTreeDef(locTreeDef);
            session.save(dyche);
            
            Location rm606 = new Location();
            rm606.setRankId(room.getRankId());
            rm606.setName("Room 606");
            rm606.setTreeDef(locTreeDef);
            session.save(rm606);
            rm606.setParent(dyche);
            
            Location freezerA = new Location();
            freezerA.setRankId(freezer.getRankId());
            freezerA.setName("Freezer A");
            freezerA.setTreeDef(locTreeDef);
            session.save(freezerA);
            freezerA.setParent(rm606);
            
            Location shelf5 = new Location();
            shelf5.setRankId(shelf.getRankId());
            shelf5.setName("Shelf 5");
            shelf5.setTreeDef(locTreeDef);
            session.save(shelf5);
            shelf5.setParent(freezerA);
            
            Location shelf4 = new Location();
            shelf4.setRankId(shelf.getRankId());
            shelf4.setName("Shelf 5");
            shelf4.setTreeDef(locTreeDef);
            session.save(shelf4);
            shelf4.setParent(freezerA);
            
            Location shelf3 = new Location();
            shelf3.setRankId(shelf.getRankId());
            shelf3.setName("Shelf 5");
            shelf3.setTreeDef(locTreeDef);
            session.save(shelf3);
            shelf3.setParent(freezerA);

            Location shelf2 = new Location();
            shelf2.setRankId(shelf.getRankId());
            shelf2.setName("Shelf 5");
            shelf2.setTreeDef(locTreeDef);
            session.save(shelf2);
            shelf2.setParent(freezerA);

            Location shelf1 = new Location();
            shelf1.setRankId(shelf.getRankId());
            shelf1.setName("Shelf 5");
            shelf1.setTreeDef(locTreeDef);
            session.save(shelf1);
            shelf1.setParent(freezerA);

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
            HibernateUtil.beginTransaction();

            // Create a geography tree definition
            TaxonTreeDef taxonTreeDef = new TaxonTreeDef();
            session.save(taxonTreeDef);
            taxonTreeDef.setName("TaxonTreeDef for DBSchemaTest");
            taxonTreeDef.setRemarks("A Taxon tree def for use in the DB testing");
            
            TaxonTreeDefItem defItemLevel0 = new TaxonTreeDefItem();
            session.save(defItemLevel0);
            defItemLevel0.setName("order");
            defItemLevel0.setRankId(0);
            
            TaxonTreeDefItem defItemLevel1 = new TaxonTreeDefItem();
            session.save(defItemLevel1);
            defItemLevel1.setName("family");
            defItemLevel1.setRankId(100);
            
            TaxonTreeDefItem defItemLevel2 = new TaxonTreeDefItem();
            session.save(defItemLevel2);
            defItemLevel2.setName("genus");
            defItemLevel2.setRankId(200);
            
            TaxonTreeDefItem defItemLevel3 = new TaxonTreeDefItem();
            session.save(defItemLevel3);
            defItemLevel3.setName("species");
            defItemLevel3.setRankId(300);
            
            defItemLevel3.setParent(defItemLevel2);
            defItemLevel2.setParent(defItemLevel1);
            defItemLevel1.setParent(defItemLevel0);
            
            defItemLevel0.setTreeDef(taxonTreeDef);
            defItemLevel1.setTreeDef(taxonTreeDef);
            defItemLevel2.setTreeDef(taxonTreeDef);
            defItemLevel3.setTreeDef(taxonTreeDef);
            
            // Create the defItemLevel0
            Taxon level0 = new Taxon();
            level0.setName("Primata");
            level0.setRankId(defItemLevel0.getRankId());
            level0.setTreeDef(taxonTreeDef);
            level0.setGuid("GUID string");
            session.save(level0);
            
            Taxon level1 = new Taxon();
            level1.setRankId(defItemLevel1.getRankId());
            level1.setName("Hominidae");
            level1.setTreeDef(taxonTreeDef);
            level1.setGuid("GUID string");
            session.save(level1);
            level1.setParent(level0);
            
            Taxon level2 = new Taxon();
            level2.setRankId(defItemLevel2.getRankId());
            level2.setName("Homo");
            level2.setTreeDef(taxonTreeDef);
            level2.setGuid("GUID string");
            session.save(level2);
            level2.setParent(level1);
            
            Taxon level3 = new Taxon();
            level3.setRankId(defItemLevel3.getRankId());
            level3.setName("sapiens");
            level3.setTreeDef(taxonTreeDef);
            level3.setGuid("GUID string");
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

    public void testGeologicTimePeriod()
    {
        log.info("Create GeologicTimePeriodTreeDef, GTPTreeDefItem and GTP objects");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
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
            HibernateUtil.beginTransaction();

            // Get a Geography to attach to
            // We assume one was successfully created in the test above
            Criteria criteria = session.createCriteria(Geography.class);
            Geography geo = (Geography)criteria.list().get(0);
            
            // Create Collection Object Definition
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
            colObj.setCatalogNumber(1901010.1f);
            colObj.setCatalogSeries(catalogSeries);
            colObj.setCollectionObjectCitations(new HashSet<Object>());
            //colObj.setCollectionObjectId(0);
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
        //criteria.add(Expression.idEq(1));
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
            //criteria.add(Expression.idEq(0));
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

