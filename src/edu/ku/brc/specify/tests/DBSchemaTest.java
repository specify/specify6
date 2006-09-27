package edu.ku.brc.specify.tests;

import static edu.ku.brc.specify.tests.CreateTestDatabases.createMultipleAgents;
import static edu.ku.brc.specify.tests.CreateTestDatabases.createMultipleLocalities;
import static edu.ku.brc.specify.tests.CreateTestDatabases.getDBObject;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createAttributeDef;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCatalogSeries;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectingEvent;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectingEventAttr;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectionObject;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectionObjectAttr;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollector;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createDetermination;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createDeterminationStatus;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createGeography;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createGeographyTreeDef;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createGeographyTreeDefItem;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPrepType;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPreparation;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPreparationAttr;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.setSession;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.IdTableMapper;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.Collectors;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttr;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.UserGroup;

/**
 * Tests for the Schema.
 * REMEMBER: Call made to "ObjCreatorHelper" do not commit, but calls made to "CreateTestDatabases" do
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class DBSchemaTest extends TestCase
{
    private static final Logger log = Logger.getLogger(DBSchemaTest.class);

    static {
        DBConnection dbConn = DBConnection.getInstance();
        dbConn.setUsernamePassword("rods", "rods");
        dbConn.setDriver("com.mysql.jdbc.Driver");
        dbConn.setConnectionStr("jdbc:mysql://localhost/");
        dbConn.setDatabaseName("demo_fish3");
    }

    protected Calendar startCal = Calendar.getInstance();


    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp()
    {

    }


    /**
     * Clean All the tables (Remove all their records)
     */
    public void XtestIdMapper()
    {
        log.info("Testing IdTableMapper");

        DBConnection oldDB = DBConnection.createInstance("com.mysql.jdbc.Driver", null, "demo_fish2", "jdbc:mysql://localhost/demo_fish2", "rods", "rods");

        try
        {
            IdTableMapper  idMapper = new IdTableMapper("collectionobject", "CollectionObjectID");
            Statement stmt     = oldDB.createConnection().createStatement();
            ResultSet rs       = stmt.executeQuery("select CollectionObjectID from collectionobject limit 0,100");
            long      newInx   = 1L;
            while (rs.next())
            {
                idMapper.put(rs.getInt(1), newInx++);
            }
            rs.close();
            stmt.close();

            long oldInx = -2135666521L;
            newInx = idMapper.get(oldInx);
            log.info("New Index ["+newInx+"] for old ["+oldInx+"]");
            assertTrue(newInx == 100);

            idMapper.cleanup();

            // Now Test Memory Approach
            idMapper = new IdTableMapper("accession", "AccessionID");
            stmt     = oldDB.createConnection().createStatement();
            rs       = stmt.executeQuery("select AccessionID from accession limit 0,100");
            newInx   = 1;
            while (rs.next())
            {
                idMapper.put(rs.getInt(1), newInx++);
            }
            rs.close();
            stmt.close();

            oldInx = 74;
            newInx = idMapper.get(oldInx);
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
        //GenericDBConversion conversion = new GenericDBConversion();
        //DataType            dataType  = conversion.createDataTypes("Animal");
        //assertNotNull(dataType);
    }

    /**
     *
     */
    public void testCreateUser()
    {
        log.info("Create SpecifyUser");
        
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            
            UserGroup userGroup = ObjCreatorHelper.createUserGroup("Fish");
            assertNotNull(userGroup);
    
            SpecifyUser user = ObjCreatorHelper.createSpecifyUser("rods", "rods@ku.edu", (short)0, userGroup, "CollectionManager");
            assertNotNull(user);
            
            HibernateUtil.commitTransaction();
            
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
    public void testCreateAgent()
    {
        log.info("Create Agents");
        assertTrue(createMultipleAgents());
    }

    /**
     * 
     */
    public void testCreateCollectionObjDef()
    {
        log.info("Create CollectionObjDef");

        // Find SpecifyUser
        SpecifyUser user = (SpecifyUser)getDBObject(SpecifyUser.class);
        assertNotNull(user);

        // Find Data Type
        DataType dataType = (DataType)getDBObject(DataType.class);
        assertNotNull(dataType);

        // This call auto creates a TaxonTreeDef and attaches it to the ColObjDef
        assertNotNull(CreateTestDatabases.createCollectionObjDef(dataType, user, "Fish", "fish"));
    }



    /**
     * 
     */
    @SuppressWarnings("unused")
    public void testGeography()
    {
        log.info("Create GeographyTreeDef, GeographyTreeDefItem and Geography objects");

        CollectionObjDef collectionObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);
        assertTrue(CreateTestDatabases.createSimpleGeography(collectionObjDef, "GeographyTreeDef for DBSchemaTest"));

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
            GeographyTreeDefItem state   = createGeographyTreeDefItem(country, geoTreeDef, "State", 300);
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

    /**
     * 
     */
    public void testLocation()
    {
        log.info("Create LocationTreeDef, LocationTreeDefItem and Location objects"); 
        CollectionObjDef collectionObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);
        assertTrue(CreateTestDatabases.createSimpleLocation(collectionObjDef, "LocationTreeDef for DBSchemaTest"));
    }

    /**
     * 
     */
    public void testTaxon()
    {
        CollectionObjDef collectionObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);
        log.info("Create TaxonTreeDef, TaxonTreeDefItem and Taxon objects"); 
        assertTrue(CreateTestDatabases.createSimpleTaxon(collectionObjDef.getTaxonTreeDef()));
    }

    /**
     * 
     */
    public void testGeologicTimePeriod()
    {
        log.info("Create GeologicTimePeriodTreeDef, GTPTreeDefItem and GTP objects");
        CollectionObjDef collectionObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);
        assertTrue(CreateTestDatabases.createGeologicTimePeriod(collectionObjDef, "GeologicTimePeriodTreeDef for DBSchemaTest"));
        
    }

    /**
     *
     */
    public void testLocality()
    {
        log.info("Create Locality");
        List<Locality> localities = createMultipleLocalities();
        assertTrue(localities.size() > 0);
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
            HibernateUtil.beginTransaction();
            
            CollectionObjDef collectionObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);
            assertNotNull(collectionObjDef);

            CatalogSeries catalogSeries = createCatalogSeries("KUFSH", "Fish", collectionObjDef);
            

            catalogSeries.getCollectionObjDefItems().add(collectionObjDef);
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
    @SuppressWarnings("unused")
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
                    new Collectors[] {createCollector(Darwin, 0), createCollector(Agassiz, 1)});

            // Create AttributeDef for Collecting Event
            AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", null);

            // Create CollectingEventAttr
            CollectingEventAttr cevAttr = createCollectingEventAttr(colEv, cevAttrDef, "Clinton Park", null);

            // Create Collection Object
            CollectionObject colObj1 = createCollectionObject(1601010.1f, "RCS101", null, Darwin,  catalogSeries, colObjDef, 5, colEv);
            CollectionObject colObj2 = createCollectionObject(1701011.1f, "RCS102", null, Agassiz, catalogSeries, colObjDef, 20, colEv);
            CollectionObject colObj3 = createCollectionObject(1801012.1f, "RCS103", null, Darwin, catalogSeries, colObjDef, 35, colEv);

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

            // Create DeterminationStatus
            DeterminationStatus oldDetermination = createDeterminationStatus("Not current","Test Determination Status");
            DeterminationStatus currentStatus = createDeterminationStatus("Current","Test Determination Status");
            
            // Create Determination
            Determination determination = createDetermination(colObj1, Darwin, t3, currentStatus, null);
            determination = createDetermination(colObj1, Darwin, t4, oldDetermination, null);

            determination = createDetermination(colObj2, Darwin, t3, currentStatus, null);
            determination = createDetermination(colObj2, Darwin, t7, oldDetermination, null);

            determination = createDetermination(colObj3, Agassiz, t7, currentStatus, null);
            determination = createDetermination(colObj3, Agassiz, t8, oldDetermination, null);

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

