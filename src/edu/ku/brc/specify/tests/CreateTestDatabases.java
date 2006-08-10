package edu.ku.brc.specify.tests;

import static edu.ku.brc.specify.tests.ObjCreatorHelper.createAccession;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createAccessionAgent;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createAccessionAuthorizations;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createAddress;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createAgent;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createAttributeDef;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCatalogSeries;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectingEvent;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectingEventAttr;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectionObject;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectionObjectAttr;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollector;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createContainer;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createContainerItem;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createDataType;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createDetermination;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createGeography;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createGeographyTreeDef;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createGeographyTreeDefItem;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createGeologicTimePeriodTreeDef;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createGeologicTimePeriodTreeDefItem;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createLocality;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createLocation;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createLocationTreeDef;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createLocationTreeDefItem;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPermit;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPrepType;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPreparation;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPreparationAttr;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createSpecifyUser;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createTaxon;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createTaxonTreeDefItem;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createUserGroup;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.setSession;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgents;
import edu.ku.brc.specify.datamodel.AccessionAuthorizations;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.Collectors;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.datamodel.ContainerItem;
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
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.UserGroup;

public class CreateTestDatabases
{
    private static final Logger log = Logger.getLogger(CreateTestDatabases.class);
    protected static Calendar calendar = Calendar.getInstance();

    protected CreateTestDatabases()
    {
        calendar.clear();
    }

    /**
     * Returns the first item from a table
     * @param classObj the class of the item to get
     * @return null if no items in table
     */
    public static Object getDBObject(Class classObj)
    {
        return getDBObject(classObj, 0);
    }

    /**
     * Returns the item at the given index from a table
     * @param classObj the class of the item to get
     * @return null if no items in table
     */
    public static Object getDBObject(Class classObj, final int index)
    {
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(classObj);
        java.util.List list = criteria.list();
        if (list.size() == 0) return null;

        return list.get(index);
    }

    public static Geography[] addGeographyKids(final GeographyTreeDef geoTreeDef,
                                               final Geography        parent,
                                               final String[]         children,
                                               final int              rankId)
    {
        Geography[] geos = new Geography[children.length];
        for (int i=0;i<children.length;i++)
        {
            geos[i] = createGeography(geoTreeDef, parent, children[i], rankId);
        }
        return geos;
    }

    @SuppressWarnings("unchecked")
    public static boolean createSimpleGeography(final CollectionObjDef colObjDef, final String treeDefName)
    {
        log.info("createSimpleGeography " + treeDefName);
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            // Create a geography tree definition
            GeographyTreeDef geoTreeDef = createGeographyTreeDef(treeDefName);
            geoTreeDef.getCollObjDefs().add(colObjDef);
            session.saveOrUpdate(geoTreeDef);

            GeographyTreeDefItem planet = createGeographyTreeDefItem(null, geoTreeDef, "Planet", 0);
            GeographyTreeDefItem cont   = createGeographyTreeDefItem(planet, geoTreeDef, "Continent", 100);

            GeographyTreeDefItem country = createGeographyTreeDefItem(cont, geoTreeDef, "Country", 200);
            GeographyTreeDefItem state   = createGeographyTreeDefItem(country, geoTreeDef, "State", 300);
            GeographyTreeDefItem county  = createGeographyTreeDefItem(state, geoTreeDef, "County", 400);

            // Create the planet Earth.
            // That seems like a big task for 5 lines of code.
            Geography earth        = createGeography(geoTreeDef, null, "Earth", planet.getRankId());
            Geography northAmerica = createGeography(geoTreeDef, earth, "North America", cont.getRankId());
            @SuppressWarnings("unused") 
            Geography us           = createGeography(geoTreeDef, northAmerica, "United States", country.getRankId());

            Geography[] states = addGeographyKids(geoTreeDef, northAmerica, new String[] {"Kansas", "Iowa", "Nebraska"}, state.getRankId());

            // Create Kansas and a few counties
            addGeographyKids(geoTreeDef, states[0], new String[] {"Douglas", "Johnson", "Osage", "Sedgwick"}, county.getRankId());
            addGeographyKids(geoTreeDef, states[1], new String[] {"Blackhawk", "Fayette", "Polk", "Woodbury"}, county.getRankId());
            addGeographyKids(geoTreeDef, states[2], new String[] {"Dakota", "Logan", "Valley", "Wheeler"}, county.getRankId());


            colObjDef.setGeographyTreeDef(geoTreeDef);
            session.saveOrUpdate(colObjDef);

            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();

            return true;
        }
        catch( Exception ex )
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
        return false;
    }

    /**
     * Returns an array of State Geographies
     * @param colObjDef colObjDef
     * @param treeDefName treeDefName
     * @return array
     */
    @SuppressWarnings("unchecked")
    public static Geography[] createGeographies(final CollectionObjDef colObjDef, final String treeDefName)
    {
        log.info("createGeographies " + treeDefName);

        setSession(null);

        // Create a geography tree definition
        GeographyTreeDef geoTreeDef = createGeographyTreeDef(treeDefName);
        geoTreeDef.getCollObjDefs().add(colObjDef);

        GeographyTreeDefItem planet = createGeographyTreeDefItem(null, geoTreeDef, "Planet", 0);
        GeographyTreeDefItem cont   = createGeographyTreeDefItem(planet, geoTreeDef, "Continent", 100);

        GeographyTreeDefItem country = createGeographyTreeDefItem(cont, geoTreeDef, "Country", 200);
        GeographyTreeDefItem state   = createGeographyTreeDefItem(country, geoTreeDef, "State", 300);
        GeographyTreeDefItem county  = createGeographyTreeDefItem(state, geoTreeDef, "County", 400);

        // Create the planet Earth.
        // That seems like a big task for 5 lines of code.
        Geography earth        = createGeography(geoTreeDef, null, "Earth", planet.getRankId());
        Geography northAmerica = createGeography(geoTreeDef, earth, "North America", cont.getRankId());
        @SuppressWarnings("unused") Geography us           = createGeography(geoTreeDef, northAmerica, "United States", country.getRankId());

        Geography[] states = addGeographyKids(geoTreeDef, northAmerica, new String[] {"Kansas", "Iowa", "Nebraska"}, state.getRankId());

        // Create Kansas and a few counties
        addGeographyKids(geoTreeDef, states[0], new String[] {"Douglas", "Johnson", "Osage", "Sedgwick"}, county.getRankId());
        addGeographyKids(geoTreeDef, states[1], new String[] {"Blackhawk", "Fayette", "Polk", "Woodbury"}, county.getRankId());
        addGeographyKids(geoTreeDef, states[2], new String[] {"Dakota", "Logan", "Valley", "Wheeler"}, county.getRankId());

        colObjDef.setGeographyTreeDef(geoTreeDef);

        return states;

    }


    /**
     * @param treeDefName treeDefName
     * @return true on success
     */
    @SuppressWarnings("unchecked")
    public static boolean createGeologicTimePeriod(final CollectionObjDef colObjDef, final String treeDefName)
    {
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            // Create a geography tree definition
            GeologicTimePeriodTreeDef treeDef = createGeologicTimePeriodTreeDef(treeDefName);
            treeDef.getCollObjDefs().add(colObjDef);
            session.saveOrUpdate(treeDef);

            GeologicTimePeriodTreeDefItem defItemLevel0 = createGeologicTimePeriodTreeDefItem(null, treeDef, "Level 0", 0);
            GeologicTimePeriodTreeDefItem defItemLevel1 = createGeologicTimePeriodTreeDefItem(defItemLevel0, treeDef, "Level 1", 100);
            GeologicTimePeriodTreeDefItem defItemLevel2 = createGeologicTimePeriodTreeDefItem(defItemLevel1, treeDef, "Level 2", 200);
            GeologicTimePeriodTreeDefItem defItemLevel3 = createGeologicTimePeriodTreeDefItem(defItemLevel2, treeDef, "Level 3", 300);

            // Create the defItemLevel0
            GeologicTimePeriod level0 = ObjCreatorHelper.createGeologicTimePeriod(treeDef, null, "Time As We Know It", defItemLevel0.getRankId());
            GeologicTimePeriod level1 = ObjCreatorHelper.createGeologicTimePeriod(treeDef, level0, "Some Really Big Time Period", defItemLevel0.getRankId());
            GeologicTimePeriod level2 = ObjCreatorHelper.createGeologicTimePeriod(treeDef, level1, "A Slightly Smaller Time Period", defItemLevel0.getRankId());
            GeologicTimePeriod level3 = ObjCreatorHelper.createGeologicTimePeriod(treeDef, level2, "Yesterday", defItemLevel0.getRankId());
            
            colObjDef.setGeologicTimePeriodTreeDef(treeDef);
            session.saveOrUpdate(colObjDef);

            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();

            return true;
        }
        catch( Exception ex )
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
        return false;
    }


    /**
     * Create a set of Locations
     * @param colObjDef  colObjDef
     * @param treeDefName treeDefName
     * @return true on success
     */
    public static boolean createSimpleLocation(final CollectionObjDef colObjDef, final String treeDefName)
    {
        log.info("createSimpleLocation "+treeDefName);
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            // Create a geography tree definition
            LocationTreeDef locTreeDef = createLocationTreeDef(treeDefName);
            locTreeDef.getCollObjDefs().add(colObjDef);
            session.saveOrUpdate(locTreeDef);

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


            colObjDef.setLocationTreeDef(locTreeDef);
            session.saveOrUpdate(colObjDef);

            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();

            return true;
        }
        catch( Exception ex )
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return false;
    }

    public static Location[] createLocations(final CollectionObjDef colObjDef, final String treeDefName)
    {
        log.info("createSimpleLocation "+treeDefName);

        setSession(null);

        // Create a geography tree definition
        LocationTreeDef locTreeDef = createLocationTreeDef(treeDefName);
        locTreeDef.getCollObjDefs().add(colObjDef);

        LocationTreeDefItem building = createLocationTreeDefItem(null, locTreeDef, "building", 0);
        LocationTreeDefItem room = createLocationTreeDefItem(building, locTreeDef, "building", 100);
        LocationTreeDefItem freezer = createLocationTreeDefItem(room, locTreeDef, "freezer", 200);
        LocationTreeDefItem shelf = createLocationTreeDefItem(freezer, locTreeDef, "shelf", 300);

        // Create the building
        Location dyche = createLocation(locTreeDef, null, "Dyche Hall", building.getRankId());
        Location rm606 = createLocation(locTreeDef, dyche, "Room 606", room.getRankId());
        Location freezerA = createLocation(locTreeDef, rm606, "Freezer A", freezer.getRankId());

        Location[] locations = new Location[5];
        for (int i=0;i<locations.length;i++)
        {
            locations[i] = createLocation(locTreeDef, freezerA, "Shelf "+i, shelf.getRankId());
        }
        colObjDef.setLocationTreeDef(locTreeDef);
        return locations;
    }



    public static Taxon[] addTaxonKids(final TaxonTreeDef treeDef,
                                       final Taxon parent,
                                       final String[] children,
                                       final int rankId)
    {
        Taxon[] geos = new Taxon[children.length];
        for (int i = 0; i < children.length; i++)
        {
            geos[i] = createTaxon(treeDef, parent, children[i], rankId);
        }
        return geos;
    }


    /**
     * @param taxonTreeDef tree def
     * @return true on success
     */
    public static boolean createSimpleTaxon(final TaxonTreeDef taxonTreeDef)
    {
        log.info("createSimpleTaxon "+taxonTreeDef.getName());
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            // Create a Taxon tree definition
            TaxonTreeDefItem defItemLevel0 = createTaxonTreeDefItem(null, taxonTreeDef, "order", 0);
            TaxonTreeDefItem defItemLevel1 = createTaxonTreeDefItem(defItemLevel0, taxonTreeDef, "family", 100);
            TaxonTreeDefItem defItemLevel2 = createTaxonTreeDefItem(defItemLevel1, taxonTreeDef, "genus", 200);
            TaxonTreeDefItem defItemLevel3 = createTaxonTreeDefItem(defItemLevel2, taxonTreeDef, "species", 300);

            // Create the defItemLevel0
            Taxon order  = createTaxon(taxonTreeDef, null, "Percidae", defItemLevel0.getRankId());
            Taxon family = createTaxon(taxonTreeDef, order, "Perciformes", defItemLevel1.getRankId());
            Taxon genus  = createTaxon(taxonTreeDef, family, "Ammocrypta", defItemLevel2.getRankId());

            String[] speciesNames = {"asprella", "beanii", "bifascia", "clara", "meridiana", "pellucida", "vivax"};
            Taxon[] species = addTaxonKids(taxonTreeDef, genus, speciesNames, defItemLevel3.getRankId());

            genus  = createTaxon(taxonTreeDef, order, "Caranx", defItemLevel2.getRankId());
            String[] speciesNames2 = {"bartholomaei", "caballus", "caninus", "crysos", "dentex", "hippos", "latus"};
            Taxon[] species2 = addTaxonKids(taxonTreeDef, genus, speciesNames2, defItemLevel3.getRankId());


            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();

            return true;
        }
        catch( Exception ex )
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
        return false;
    }

    public static Taxon[] createTaxonomy(final TaxonTreeDef taxonTreeDef)
    {
        log.info("createSimpleTaxon "+taxonTreeDef.getName());

        setSession(null);

        // Create a Taxon tree definition
        TaxonTreeDefItem defItemLevel0 = createTaxonTreeDefItem(null, taxonTreeDef, "order", 0);
        TaxonTreeDefItem defItemLevel1 = createTaxonTreeDefItem(defItemLevel0, taxonTreeDef, "family", 100);
        TaxonTreeDefItem defItemLevel2 = createTaxonTreeDefItem(defItemLevel1, taxonTreeDef, "genus", 200);
        TaxonTreeDefItem defItemLevel3 = createTaxonTreeDefItem(defItemLevel2, taxonTreeDef, "species", 300);

        // Create the defItemLevel0
        Taxon order  = createTaxon(taxonTreeDef, null, "Percidae", defItemLevel0.getRankId());
        Taxon family = createTaxon(taxonTreeDef, order, "Perciformes", defItemLevel1.getRankId());
        Taxon genus  = createTaxon(taxonTreeDef, family, "Ammocrypta", defItemLevel2.getRankId());

        ArrayList<Taxon> list = new ArrayList<Taxon>();
        String[] speciesNames = {"asprella", "beanii", "bifascia", "clara", "meridiana", "pellucida", "vivax"};
        Taxon[] species = addTaxonKids(taxonTreeDef, genus, speciesNames, defItemLevel3.getRankId());
        Collections.addAll(list, species);

        genus  = createTaxon(taxonTreeDef, order, "Caranx", defItemLevel2.getRankId());
        String[] speciesNames2 = {"bartholomaei", "caballus", "caninus", "crysos", "dentex", "hippos", "latus"};
        Taxon[] species2 = addTaxonKids(taxonTreeDef, genus, speciesNames2, defItemLevel3.getRankId());
        Collections.addAll(list, species2);

        Taxon[] t = new Taxon[list.size()];
        for (int i=0;i<list.size();i++)
        {
            t[i] = list.get(i);
        }
        return t;
    }


    /**
     * @return true on success
     */
    public static boolean createMultipleLocalities()
    {
        log.info("Creating Multiple Localities");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            session.save(createLocality("This is the place", (Geography)getDBObject(Geography.class, 6)));
            session.save(createLocality("My Private Forest", (Geography)getDBObject(Geography.class, 10)));

            HibernateUtil.commitTransaction();

            return true;

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
        return false;
    }

    /**
     * Creates an array of address and hooks them up to agents
     * @param agents the agents to get addresses
     * @return address array
     */
    public static Address[] createAddresses(Agent[] agents)
    {
        String[] addresses = {"101 High Street.",  "St. Charles",  "Kent", "Great Britain", "AE00939",
                "Harvard Square",     "Cambridge",   "MA",   "USA",           "009391",
                "99 East Street.",    "Lawrence",    "KS",   "USA",         "66045",
                "123 Johnson Street", "Olathe",      "KS",   "USA",         "66045",
                "11911 S Redbud Ln",  "Olathe",      "KS",   "USA",         "66061",
                "RR1",                "Olathe",      "KS",   "USA",         "66045",
                "12 Mississippi",     "Lawrence",    "KS",   "USA",         "66045",
                "156 Inverness",      "Lawrence",    "KS",   "USA",         "66045",
                "100 Main Street",    "Topeka",      "KS",   "USA",         "66099",
        };
        
        

        Address[] addrs = new Address[addresses.length / 5];
        for (int i = 0; i < agents.length; i += 5)
        {
            Address addr = createAddress(agents[i], addresses[i], "", addresses[i+1], addresses[i+2], addresses[i+3], addresses[i+4]);
            addrs[i/5] = addr;
        }
        return addrs;
    }


    /**
     * @return true on success
     */
    public static boolean createMultipleAgents()
    {
        log.info("Create Agents");
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            // Create Collection Object Definition
            String[] values = {"Mr.",  "Charles","A", "Darwin","CD",
                               "Mr.",  "Louis","", "Agassiz","AL",
                               "Mr.",  "Andrew", "", "Bentley", "AB",
                               "Mr.",  "Josh", "", "Stewart", "JS",
                               "Mrs.", "Meg", "", "Kumin", "MK",
                               "Mr.",  "Jim", "", "Beach", "JB",
                               "Mr.",  "Rod", "", "Spears", "RS",
                               "Mr.",  "Stewart", "", "Johnson", "SJ",
            };
            Agent[] agents = new Agent[values.length/5];
            for (int i=0;i<values.length;i+=5)
            {
                agents[i/5] = createAgent(values[i], values[i+1], values[i+2], values[i+3], values[i+4]);
            }

            String[] addresses = {"101 High Street.",  "St. Charles",  "Kent", "Great Britain", "AE00939",
                                  "Harvard Square",     "Cambridge",   "MA",   "USA",           "009391",
                                  "99 East Street.",    "Lawrence",    "KS",   "USA",         "66045",
                                  "123 Johnson Street", "Olathe",      "KS",   "USA",         "66045",
                                  "RR1",                "Olathe",      "KS",   "USA",         "66045",
                                  "12 Mississippi",     "Lawrence",    "KS",   "USA",         "66045",
                                  "156 Inverness",      "Lawrence",    "KS",   "USA",         "66045",
                                  "100 Main Street",    "Topeka",      "KS",   "USA",         "66099",
            };

            Address[] addrs = createAddresses(agents);

            // Add an extra address for one of them
            //Address addr = createAddress(agents[0], "34 Vintage Drive", "", "San Diego", "CA",   "USA", "92129");

            HibernateUtil.commitTransaction();

            return true;

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return false;
    }

    /**
     * Return an array of Agents that are in memory (not in database)
     * @return an array of Agents that are in memory (not in database)
     */
    public static Agent[] createAgentsInMemory()
    {
        log.info("Create Agents");

        setSession(null);

        // Create Collection Object Definition
        String[] values = {"Mr.",  "Charles","A", "Darwin","CD",
                           "Mr.",  "Louis","", "Agassiz","AL",
                           "Mr.",  "Andrew", "", "Bentley", "AB",
                           "Mr.",  "Josh", "", "Stewart", "JS",
                           "Mrs.", "Meg", "", "Kumin", "MK",
                           "Mr.",  "Jim", "", "Beach", "JB",
                           "Mr.",  "Rod", "", "Spears", "RS",
                           "Mr.",  "Stewart", "", "Johnson", "SJ",
        };
        Agent[] agents = new Agent[values.length/5];
        for (int i=0;i<values.length;i+=5)
        {
            agents[i/5] = createAgent(values[i], values[i+1], values[i+2], values[i+3], values[i+4]);
        }
        return agents;

    }

    /**
     * createAccessionsInMemory.
     * @param agents agents
     * @return accession array
     */
    public static Accession[] createAccessionsInMemory()
    {
        setSession(null);

        Agent[]   agents    = createAgentsInMemory();
        Address[] addresses = createAddresses(agents); // created AgentAddress also

        String[] roles    = {"Reviewer", "Submitter", "Accepter"};
        String[] division = {"Botany", "Entomology", "Herpetology", "Ichthyology", "Invertebrate Paleo", "Invertebrate Zoology", "Mammalogy", "Ornithology", "Paleobotany", "Vertebrate Paleo"};
        String[] status   = {"In Process", "Complete", "Closed"};
        String[] types    = {"Field Work", "Bequest", "Gift", "Collection", "Purchase", "Exchange", "Abandonement", "Salvage", "Other"};

        String[] permitType = {"International", "Federal", "State"};

        String[] accessionNumbers = {"2005-IT-0121", "2005-PB-0122", "2005-PB-0123"};
        String[] permitNumbers    = {"P101", "P102", "P103"};

        int agentsInx = 0;
        Accession[] accessions = new Accession[accessionNumbers.length];
        for (int i=0;i<accessions.length;i++)
        {
            accessions[i] = createAccession(types[i],
                                            status[i],
                                            accessionNumbers[i],
                                            "",
                                            Calendar.getInstance(),
                                            Calendar.getInstance());
            accessions[i].setText1(division[i]);

            for (int j=0;j<roles.length;j++)
            {
                AccessionAgents accessionAgent = createAccessionAgent(roles[j],  agents[agentsInx % agents.length], accessions[i], null);
                agentsInx++;
                accessions[i].getAccessionAgents().add(accessionAgent);
            }

            // Make as many permits as the position of the accession in the array
            agentsInx = 0;
            for (int j=0;j<i+1;j++)
            {
                Permit permit = createPermit(permitNumbers[j], permitType[1],
                        Calendar.getInstance(),  // issuedDate
                        Calendar.getInstance(), // startDate
                        Calendar.getInstance(), // endDate
                        Calendar.getInstance());// renewalDate

                permit.setAgentByIssuee(i == 1 && j == 0 ? null : agents[agentsInx % agents.length]);
                agentsInx++;
                permit.setAgentByIssuer(agents[agentsInx % agents.length]);
                agentsInx++;

                AccessionAuthorizations accessionAuthorizations = createAccessionAuthorizations(permit, accessions[i], null);
                accessions[i].getAccessionAuthorizations().add(accessionAuthorizations);
            }

        }
        return accessions;
    }


    public static boolean createCollectionObjDef(final DataType    dataType,
                                                 final SpecifyUser user,
                                                 final String      name)
    {
        try
        {
            Session session = HibernateUtil.getCurrentSession();

            TaxonTreeDef taxonTreeDef = new TaxonTreeDef();
            taxonTreeDef.setName("Taxon for "+name);
            taxonTreeDef.setTreeDefItems(new HashSet<Object>());
            taxonTreeDef.setTreeEntries(new HashSet<Object>());

            setSession(session);

            HibernateUtil.beginTransaction();

            // Create Collection Object Definition
            CollectionObjDef colObjDef = ObjCreatorHelper.createCollectionObjDef(name, dataType, user, taxonTreeDef);
            session.save(colObjDef);

            // Update the SpecifyUser to own the ColObjDef
            user.getCollectionObjDef().add(colObjDef);
            session.saveOrUpdate(user);

            HibernateUtil.commitTransaction();

            return true;

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
        return false;
    }


    /**
     * @param disciplineName fish, birds, bees etc
     * @return true on success
     */
    public static boolean createSingleDiscipline(final String disciplineName)
    {
        BasicSQLUtils.cleanAllTables();

        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            UserGroup        userGroup        = createUserGroup(disciplineName);
            SpecifyUser      user             = createSpecifyUser("John Doe", "jd@email.com", (short)0, userGroup);
            DataType         dataType         = createDataType(disciplineName);

            createMultipleLocalities();

            HibernateUtil.commitTransaction();

            createCollectionObjDef(dataType, user, disciplineName); // creates TaxonTreeDef
            CollectionObjDef collectionObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);

            createSimpleGeography(collectionObjDef, "Geography");
            createSimpleTaxon(collectionObjDef.getTaxonTreeDef());
            createSimpleLocation(collectionObjDef, "Location");

            createMultipleAgents();

            HibernateUtil.beginTransaction();
            CatalogSeries catalogSeries = createCatalogSeries("KUFSH", "Fish");

            CollectionObjDef colObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);
            Locality locality = (Locality)getDBObject(Locality.class);

            String[] agentNames = { "Darwin", "Agassiz", "Bentley", "Stewart", "Kumin", "Beach" };
            Agent[] agents = new Agent[agentNames.length];
            for (int i = 0; i < agents.length; i++)
            {
                agents[i] = getAgentByLastName(agentNames[i]);
            }

            // Create Collecting Event
            CollectingEvent colEv = createCollectingEvent(locality,
                    new Collectors[] {createCollector(agents[0], 0), createCollector(agents[1], 1)});

            // Create AttributeDef for Collecting Event
            AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", null);

            // Create CollectingEventAttr
            CollectingEventAttr cevAttr = createCollectingEventAttr(colEv, cevAttrDef, "Clinton Park", null);

            // Create Collection Object
            Object[]  values = {1001010.1f, "RCS101", agents[0], 5,
                                1101011.1f, "RCS102", agents[1], 20,
                                1201012.1f, "RCS103", agents[2], 15,
                                1301013.1f, "RCS104", agents[3], 25,
                                1401014.1f, "RCS105", agents[4], 35,
                                1501015.1f, "RCS106", agents[5], 45,
                                1601016.1f, "RCS107", agents[0], 55,
                                1701017.1f, "RCS108", agents[1], 65};
            CollectionObject[] colObjs = new CollectionObject[values.length/4];
            for (int i=0;i<values.length;i+=4)
            {
                colObjs[i/4] = createCollectionObject((Float)values[i],
                                                      (String)values[i+1],
                                                      null,
                                                      (Agent)values[i+2],
                                                      catalogSeries,
                                                      colObjDef,
                                                      (Integer)values[+3],
                                                      colEv);
            }

            // Create AttributeDef for Collection Object
            AttributeDef colObjAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "MoonPhase", null);

            // Create CollectionObjectAttr
            CollectionObjectAttr colObjAttr = createCollectionObjectAttr(colObjs[0], colObjAttrDef, "Full", null);

            String[] speciesNames = {"asprella", "beanii", "bifascia", "clara", "meridiana", "pellucida", "vivax",
                                     "bartholomaei", "caballus", "caninus", "crysos", "dentex", "hippos", "latus"};
            Taxon[] t = new Taxon[speciesNames.length];
            for (int i = 0; i < t.length; i++)
            {
                t[i] = getTaxonByName(speciesNames[i]);
            }

            int agentInx = 0;
            int taxonInx = 0;
            // Create Determination
            for (int i=0;i<colObjs.length;i++)
            {
                for (int j=0;j<i+2;j++)
                {
                    Calendar cal = Calendar.getInstance();
                    cal.clear();
                    cal.set(1990-i, 11-i, 28-(i+j));
                    createDetermination(colObjs[i], agents[agentInx % agents.length], t[taxonInx % t.length], j == 0, cal);
                    agentInx++;
                    taxonInx++;
                }
            }

            // Create Preparation Type
            PrepType prepType = createPrepType("Skeleton");
            PrepType prepType2 = createPrepType("C&S");

            Location location = (Location)getDBObject(Location.class, 6); // Shelf 2

            // Create Preparation for each CollectionObject
            agentInx = 3; // arbitrary
            Preparation[] preps = new Preparation[colObjs.length];
            for (int i=0;i<preps.length;i++)
            {
                preps[i] = createPreparation(prepType,  agents[agentInx % agents.length], colObjs[i], location, 10+i);
                agentInx++;
            }

            // Create AttributeDef for Preparation
            AttributeDef prepAttrDefSize = createAttributeDef(AttributeIFace.FieldType.IntegerType, "size", prepType);
            AttributeDef prepAttrDefSex  = createAttributeDef(AttributeIFace.FieldType.StringType, "sex", prepType);

            // Create PreparationAttr
            for (int i=0;i<preps.length;i++)
            {
                createPreparationAttr(prepAttrDefSize, preps[i], null, 100.0);
                createPreparationAttr(prepAttrDefSex,  preps[i], i % 2 == 0 ? "Male" : "Female", null);
            }

            HibernateUtil.commitTransaction();

            log.info("Done createSingleDiscipline " + disciplineName);

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return true;
    }

    /**
     * Return a taxon by name
     * @param name the name
     * @return the taxon object
     */
    public static Taxon getTaxonByName(final String name)
    {
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Taxon.class);
        criteria.add(Expression.eq("name", name));
        java.util.List list = criteria.list();
        if (list.size() == 0)
        {
            log.error("Couldn't find taxon name ["+name+"]");
            return null;
        }

        return (Taxon)list.get(0);
    }

    /**
     * Return agent by lastname
     * @param lastName  lastName
     * @return Agent on success
     */
    public static Agent getAgentByLastName(final String lastName)
    {
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Agent.class);
        criteria.add(Expression.eq("lastName", lastName));
        java.util.List list = criteria.list();
        if (list.size() == 0)
        {
            log.error("Couldn't find Agent name ["+lastName+"]");
            return null;
        }

        return (Agent)list.get(0);
    }

    public static boolean createTwoColObjDefOneCatSeries()
    {
        BasicSQLUtils.cleanAllTables();

        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            UserGroup        userGroup        = createUserGroup("MyGroup");
            SpecifyUser      user             = createSpecifyUser("John Doe", "jd@email.com", (short)0, userGroup);
            DataType         dataType         = createDataType("Animal");

            createMultipleLocalities();

            HibernateUtil.commitTransaction();

            createCollectionObjDef(dataType, user, "Birds"); // creates TaxonTreeDef
            createCollectionObjDef(dataType, user, "Bees");  // creates TaxonTreeDef
            CollectionObjDef colObjDefBirds = (CollectionObjDef)getDBObject(CollectionObjDef.class, 0);
            CollectionObjDef colObjDefBees  = (CollectionObjDef)getDBObject(CollectionObjDef.class, 1);

            createSimpleGeography(colObjDefBirds, "Geography");
            createSimpleTaxon(colObjDefBirds.getTaxonTreeDef());
            createSimpleLocation(colObjDefBirds, "Location");

            createSimpleGeography(colObjDefBees, "GeographyBees");
            createSimpleTaxon(colObjDefBees.getTaxonTreeDef());
            createSimpleLocation(colObjDefBees, "LocationBees");

            createMultipleAgents();

            HibernateUtil.beginTransaction();
            CatalogSeries catalogSeries = createCatalogSeries("KUBB", "BirdsBees");

            Locality locality = (Locality)getDBObject(Locality.class);

            String[] agentNames = {"Darwin","Agassiz","Bentley","Stewart","Kumin", "Beach"};
            Agent[] agents = new Agent[agentNames.length];
            for (int i=0;i<agents.length;i++)
            {
                agents[i] = getAgentByLastName(agentNames[i]);
            }

            // Create Collecting Event
            CollectingEvent colEv = createCollectingEvent(locality,
                    new Collectors[] {createCollector(agents[0], 0), createCollector(agents[1], 1)});

            // Create AttributeDef for Collecting Event
            AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", null);

            // Create CollectingEventAttr
            CollectingEventAttr cevAttr = createCollectingEventAttr(colEv, cevAttrDef, "Clinton Park", null);

            // Create Collection Object
            Object[]  values = {1601010.1f, "RCS101", agents[0], 5,
                                1701011.1f, "RCS102", agents[1], 20,
                                1801012.1f, "RCS103", agents[2], 35};
            CollectionObject[] colObjs = new CollectionObject[values.length/4];
            for (int i=0;i<values.length;i+=4)
            {
                colObjs[i/4] = createCollectionObject((Float)values[i], (String)values[i+1], null, (Agent)values[i+2],  catalogSeries, colObjDefBirds, (Integer)values[+3], null);
            }

            // Create AttributeDef for Collection Object
            AttributeDef colObjAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "MoonPhase", null);

            // Create CollectionObjectAttr
            CollectionObjectAttr colObjAttr = createCollectionObjectAttr(colObjs[0], colObjAttrDef, "Full", null);

            String[] speciesNames = {"asprella", "beanii", "bifascia", "clara", "meridiana", "pellucida", "vivax",
                                     "bartholomaei", "caballus", "caninus", "crysos", "dentex", "hippos", "latus"};
            Taxon[] t = new Taxon[speciesNames.length];
            for (int i=0;i<t.length;i++)
            {
                t[i] = getTaxonByName(speciesNames[i]);
            }


            // Create Determination
            Determination determination = createDetermination(colObjs[0], agents[3], t[0], true, null);
            determination = createDetermination(colObjs[0], agents[0], t[1], false, null);

            determination = createDetermination(colObjs[1], agents[4], t[4], true, null);
            determination = createDetermination(colObjs[1], agents[0], t[5], false, null);

            determination = createDetermination(colObjs[2], agents[2], t[7], true, null);
            determination = createDetermination(colObjs[2], agents[1], t[8], false, null);

            // Create Preparation Type
            PrepType prepType = createPrepType("Skeleton");

            Location location = (Location)getDBObject(Location.class, 6); // Shelf 2

            // Create Preparation
            Preparation[] preps = new Preparation[3];
            preps[0] = createPreparation(prepType, agents[0], colObjs[0], location, 10);
            preps[1] = createPreparation(prepType,  agents[1], colObjs[1], location, 33);
            preps[2] = createPreparation(prepType,  agents[3], colObjs[2], location, 23);

            // Create AttributeDef for Preparation
            AttributeDef prepAttrDefSize = createAttributeDef(AttributeIFace.FieldType.IntegerType, "size", prepType);
            AttributeDef prepAttrDefSex  = createAttributeDef(AttributeIFace.FieldType.StringType, "sex", prepType);

            // Create PreparationAttr
            createPreparationAttr(prepAttrDefSize, preps[0], null, 100.0);
            createPreparationAttr(prepAttrDefSex,  preps[0], "M", null);

            createPreparationAttr(prepAttrDefSize, preps[1], null, 10.0);
            createPreparationAttr(prepAttrDefSex,  preps[1], "F", null);

            createPreparationAttr(prepAttrDefSize, preps[2], null, 20.0);
            createPreparationAttr(prepAttrDefSex,  preps[2], "Uknown", null);

            HibernateUtil.commitTransaction();

            log.info("Done createTwoColObjDefOneCatSeries");

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return true;
    }

    /**
     * @param name
     * @return true on success
     */
    public static boolean createAccessionsDatabase(final String name)
    {
        BasicSQLUtils.cleanAllTables();

        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            UserGroup        userGroup        = createUserGroup(name);
            SpecifyUser      user             = createSpecifyUser("John Doe", "jd@email.com", (short)0, userGroup);

            createMultipleAgents();

            String[] agentNames = { "Darwin", "Agassiz", "Bentley", "Stewart", "Kumin", "Beach" };
            Agent[] agents = new Agent[agentNames.length];
            for (int i = 0; i < agents.length; i++)
            {
                agents[i] = getAgentByLastName(agentNames[i]);
            }

            Object[] permitInfo = {"101", "Field Work"};
            /*
            final String permitNumber,
            final String type,
            final Calendar issuedDate,
            final Calendar startDate,
            final Calendar endDate,
            final Calendar renewalDate
            */
            //Permit[] permits = new

            log.info("Done Created Accession Database " + name);

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
       return true;
    }

    /**
     * @param disciplineName fish, birds, bees etc
     * @return true on success
     */
    public static boolean createPlantDatabaseWithContainers(final String disciplineName)
    {
        BasicSQLUtils.cleanAllTables();

        try
        {
            Session session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();

            UserGroup        userGroup        = createUserGroup(disciplineName);
            SpecifyUser      user             = createSpecifyUser("John Doe", "jd@email.com", (short)0, userGroup);
            DataType         dataType         = createDataType(disciplineName);

            createMultipleLocalities();

            HibernateUtil.commitTransaction();

            // These do there own Transaction
            createCollectionObjDef(dataType, user, disciplineName); // creates TaxonTreeDef
            CollectionObjDef collectionObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);

            createSimpleGeography(collectionObjDef, "Geography");
            createSimpleTaxon(collectionObjDef.getTaxonTreeDef());
            createSimpleLocation(collectionObjDef, "Location");

            createMultipleAgents();
            // DONE

            session = HibernateUtil.getCurrentSession();
            setSession(session);
            HibernateUtil.beginTransaction();
            CatalogSeries catalogSeries = createCatalogSeries("KUFSH", "Fish");

            CollectionObjDef colObjDef = (CollectionObjDef)getDBObject(CollectionObjDef.class);
            Locality locality = (Locality)getDBObject(Locality.class);

            String[] agentNames = { "Darwin", "Agassiz", "Bentley", "Stewart", "Kumin", "Beach" };
            Agent[] agents = new Agent[agentNames.length];
            for (int i = 0; i < agents.length; i++)
            {
                agents[i] = getAgentByLastName(agentNames[i]);
            }

            // Create Collecting Event
            CollectingEvent colEv = createCollectingEvent(locality,
                    new Collectors[] {createCollector(agents[0], 0), createCollector(agents[1], 1)});

            // Create AttributeDef for Collecting Event
            AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", null);

            // Create CollectingEventAttr
            CollectingEventAttr cevAttr = createCollectingEventAttr(colEv, cevAttrDef, "Clinton Park", null);

            // Create Collection Object
            Object[]  values = {1001010.1f, "RCS101", agents[0], 5,
                                1101011.1f, "RCS102", agents[1], 20,
                                1201012.1f, "RCS103", agents[2], 15,
                                1301013.1f, "RCS104", agents[3], 25,
                                1401014.1f, "RCS105", agents[4], 35,
                                1501015.1f, "RCS106", agents[5], 45,
                                1601016.1f, "RCS107", agents[0], 55,
                                1701017.1f, "RCS108", agents[1], 65};
            CollectionObject[] colObjs = new CollectionObject[values.length/4];
            for (int i=0;i<values.length;i+=4)
            {
                colObjs[i/4] = createCollectionObject((Float)values[i],
                                                      (String)values[i+1],
                                                      null,
                                                      (Agent)values[i+2],
                                                      catalogSeries,
                                                      colObjDef,
                                                      (Integer)values[+3],
                                                      colEv);
            }

            Location location = (Location)getDBObject(Location.class, 6); // Shelf 2

            // Create a Container that is CollectionObject[0]
            // than add two children
            Container container = createContainer((short)0, "Folder", "Folder Desc", null, colObjs[0], location);

            ContainerItem containerItem = createContainerItem(container);
            container.getItems().add(containerItem);
            containerItem.getCollectionObjects().add(colObjs[1]);
            colObjs[1].setContainerItem(containerItem);
            session.saveOrUpdate(containerItem);
            session.saveOrUpdate(container);
            session.saveOrUpdate(colObjs[1]);

            containerItem = createContainerItem(container);
            container.getItems().add(containerItem);
            containerItem.getCollectionObjects().add(colObjs[2]);
            colObjs[2].setContainerItem(containerItem);
            session.saveOrUpdate(containerItem);
            session.saveOrUpdate(container);
            session.saveOrUpdate(colObjs[2]);

            // Create AttributeDef for Collection Object
            AttributeDef colObjAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "MoonPhase", null);

            // Create CollectionObjectAttr
            CollectionObjectAttr colObjAttr = createCollectionObjectAttr(colObjs[0], colObjAttrDef, "Full", null);

            String[] speciesNames = {"asprella", "beanii", "bifascia", "clara", "meridiana", "pellucida", "vivax",
                                     "bartholomaei", "caballus", "caninus", "crysos", "dentex", "hippos", "latus"};
            Taxon[] t = new Taxon[speciesNames.length];
            for (int i = 0; i < t.length; i++)
            {
                t[i] = getTaxonByName(speciesNames[i]);
            }

            int agentInx = 0;
            int taxonInx = 0;
            // Create Determination
            for (int i=0;i<colObjs.length;i++)
            {
                for (int j=0;j<i+2;j++)
                {
                    Calendar cal = Calendar.getInstance();
                    cal.clear();
                    cal.set(1990-i, 11-i, 28-(i+j));
                    createDetermination(colObjs[i], agents[agentInx % agents.length], t[taxonInx % t.length], j == 0, cal);
                    agentInx++;
                    taxonInx++;
                }
            }

            // Create Preparation Type
            PrepType prepType = createPrepType("Skeleton");
            PrepType prepType2 = createPrepType("C&S");



            // Create Preparation for each CollectionObject
            agentInx = 3; // arbitrary
            Preparation[] preps = new Preparation[colObjs.length];
            for (int i=0;i<preps.length;i++)
            {
                preps[i] = createPreparation(prepType,  agents[agentInx % agents.length], colObjs[i], location, 10+i);
                agentInx++;
            }

            // Create AttributeDef for Preparation
            AttributeDef prepAttrDefSize = createAttributeDef(AttributeIFace.FieldType.IntegerType, "size", prepType);
            AttributeDef prepAttrDefSex  = createAttributeDef(AttributeIFace.FieldType.StringType, "sex", prepType);

            // Create PreparationAttr
            for (int i=0;i<preps.length;i++)
            {
                createPreparationAttr(prepAttrDefSize, preps[i], null, 100.0);
                createPreparationAttr(prepAttrDefSex,  preps[i], i % 2 == 0 ? "Male" : "Female", null);
            }

            HibernateUtil.commitTransaction();

            log.info("Done createSingleDiscipline " + disciplineName);

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }

        return true;
    }



    public static void main(String args[]) throws Exception
    {
        DBConnection dbConn = DBConnection.getInstance();
        dbConn.setUsernamePassword("rods", "rods");
        dbConn.setDriver("com.mysql.jdbc.Driver");
        dbConn.setConnectionStr("jdbc:mysql://localhost/");
        dbConn.setDatabaseName("fish");

        createSingleDiscipline("Fish");
        
        //createTwoColObjDefOneCatSeries();
        //createPlantDatabaseWithContainers("Plant");

    }

}
