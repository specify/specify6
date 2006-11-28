/**
 * Copyright (C) 2006 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tests;

import static edu.ku.brc.specify.tests.DataBuilder.createAccession;
import static edu.ku.brc.specify.tests.DataBuilder.createAccessionAgent;
import static edu.ku.brc.specify.tests.DataBuilder.createAddress;
import static edu.ku.brc.specify.tests.DataBuilder.createAgent;
import static edu.ku.brc.specify.tests.DataBuilder.createAttachment;
import static edu.ku.brc.specify.tests.DataBuilder.createAttributeDef;
import static edu.ku.brc.specify.tests.DataBuilder.createCatalogSeries;
import static edu.ku.brc.specify.tests.DataBuilder.createCollectingEvent;
import static edu.ku.brc.specify.tests.DataBuilder.createCollectingEventAttr;
import static edu.ku.brc.specify.tests.DataBuilder.createCollectingTrip;
import static edu.ku.brc.specify.tests.DataBuilder.createCollectionObjDef;
import static edu.ku.brc.specify.tests.DataBuilder.createCollectionObject;
import static edu.ku.brc.specify.tests.DataBuilder.createCollectionObjectAttr;
import static edu.ku.brc.specify.tests.DataBuilder.createCollector;
import static edu.ku.brc.specify.tests.DataBuilder.createDataType;
import static edu.ku.brc.specify.tests.DataBuilder.createDetermination;
import static edu.ku.brc.specify.tests.DataBuilder.createDeterminationStatus;
import static edu.ku.brc.specify.tests.DataBuilder.createGeography;
import static edu.ku.brc.specify.tests.DataBuilder.createGeographyChildren;
import static edu.ku.brc.specify.tests.DataBuilder.createGeographyTreeDef;
import static edu.ku.brc.specify.tests.DataBuilder.createGeographyTreeDefItem;
import static edu.ku.brc.specify.tests.DataBuilder.createGeologicTimePeriod;
import static edu.ku.brc.specify.tests.DataBuilder.createGeologicTimePeriodTreeDef;
import static edu.ku.brc.specify.tests.DataBuilder.createGeologicTimePeriodTreeDefItem;
import static edu.ku.brc.specify.tests.DataBuilder.createLoan;
import static edu.ku.brc.specify.tests.DataBuilder.createLoanPhysicalObject;
import static edu.ku.brc.specify.tests.DataBuilder.createLoanReturnPhysicalObject;
import static edu.ku.brc.specify.tests.DataBuilder.createLoanAgent;
import static edu.ku.brc.specify.tests.DataBuilder.createLocality;
import static edu.ku.brc.specify.tests.DataBuilder.createLocation;
import static edu.ku.brc.specify.tests.DataBuilder.createLocationTreeDef;
import static edu.ku.brc.specify.tests.DataBuilder.createLocationTreeDefItem;
import static edu.ku.brc.specify.tests.DataBuilder.createPermit;
import static edu.ku.brc.specify.tests.DataBuilder.createPickList;
import static edu.ku.brc.specify.tests.DataBuilder.createPrepType;
import static edu.ku.brc.specify.tests.DataBuilder.createPreparation;
import static edu.ku.brc.specify.tests.DataBuilder.createShipment;
import static edu.ku.brc.specify.tests.DataBuilder.createSpecifyUser;
import static edu.ku.brc.specify.tests.DataBuilder.createTaxon;
import static edu.ku.brc.specify.tests.DataBuilder.createTaxonChildren;
import static edu.ku.brc.specify.tests.DataBuilder.createTaxonTreeDef;
import static edu.ku.brc.specify.tests.DataBuilder.createTaxonTreeDefItem;
import static edu.ku.brc.specify.tests.DataBuilder.createUserGroup;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgents;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectingTrip;
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
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanAgents;
import edu.ku.brc.specify.datamodel.LoanPhysicalObject;
import edu.ku.brc.specify.datamodel.LoanReturnPhysicalObject;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileStoreAttachmentManager;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * 
 * @code_status Alpha
 * @author jstewart
 */
public class BuildSampleDatabase
{
    private static final Logger log      = Logger.getLogger(BuildSampleDatabase.class);
    protected static Calendar   calendar = Calendar.getInstance();
    protected static Session    session;
    protected static FakeRandom     rand = new FakeRandom();
    
    public static Session getSession()
    {
        return session;
    }
    
    public static void setSession(Session s)
    {
        session = s;
    }

    public static List<Object> createSingleDiscipline(final String colObjDefName, final String disciplineName)
    {
        log.info("Creating single discipline database: " + disciplineName);

        Vector<Object> dataObjects = new Vector<Object>();

        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        UserGroup userGroup = createUserGroup(disciplineName);
        SpecifyUser user = createSpecifyUser("rods", "rods@ku.edu", (short) 0, userGroup, "CollectionManager");
        DataType dataType = createDataType(disciplineName);
        TaxonTreeDef taxonTreeDef = createTaxonTreeDef("Sample Taxon Tree Def");
        CollectionObjDef collectionObjDef = createCollectionObjDef(colObjDefName, disciplineName, dataType, user,
                taxonTreeDef);

        dataObjects.add(collectionObjDef);
        dataObjects.add(userGroup);
        dataObjects.add(user);
        dataObjects.add(dataType);
        dataObjects.add(taxonTreeDef);
        
        ////////////////////////////////
        // build the trees
        ////////////////////////////////
        List<Object> taxa = createSimpleTaxon(collectionObjDef.getTaxonTreeDef());
        List<Object> geos = createSimpleGeography(collectionObjDef, "Geography");
        List<Object> locs = createSimpleLocation(collectionObjDef, "Location");
        List<Object> gtps = createSimpleGeologicTimePeriod(collectionObjDef, "Geologic Time Period");

        dataObjects.addAll(taxa);
        dataObjects.addAll(geos);
        dataObjects.addAll(locs);
        dataObjects.addAll(gtps);
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////
        log.info("Creating picklists");

        //                                 Name                     Table Name         Field       Formatter         R/O  Size
        dataObjects.add(createPickList("DeterminationStatus", 1, "determinationstatus", null, "DeterminationStatus", true, -1));
        dataObjects.add(createPickList("Department",          2, "accession",         "text1" ,       null,          true, -1));
        dataObjects.add(createPickList("AgentTitle",          2, "agent",             "title" ,       null,          true, -1));
        
        String[] types = {"state", "federal", "international", "<no data>"};
        dataObjects.add(createPickList("PermitType", true, types));

        //String[] titles = {"Dr.", "Mr.", "Ms.", "Mrs.", "Sir"};
        //dataObjects.add(createPickList("AgentTitle", true, titles));
        
        String[] roles = {"borrower", "receiver"};
        dataObjects.add(createPickList("LoanAgentsRole", true, roles));
        
        String[] sexes = {"both", "female", "male", "unknown"};
        dataObjects.add(createPickList("BiologicalSex", true, sexes));
        
        String[] status = {"complete", "in process", "<no data>"};
        dataObjects.add(createPickList("AccessionStatus", true, status));
        
        String[] methods = {"by hand", "USPS", "UPS", "FedEx", "DHL"};
        dataObjects.add(createPickList("ShipmentMethod", true, methods));
        
        String[] accTypes = {"collection"};
        dataObjects.add(createPickList("AccessionType", true, accTypes));
        
        String[] accRoles = {"collector", "donor", "reviewer", "staff", "receiver"};
        dataObjects.add(createPickList("AccessionRole", true, accRoles));
        
        String[] stages = {"adult", "egg", "embryo", "hatchling", "immature", "juvenile", "larva", "nymph", "pupa", "seed"};
        dataObjects.add(createPickList("BiologicalStage", true, stages));
        
        String[] collMethods = {"boat electro-shocker", "hook & line", "seine", "trap", "<no data>"};
        dataObjects.add(createPickList("CollectingMethod", true, collMethods));
        
        String[] prepMeth = {"C&S", "skeleton", "x-ray", "image", "EtOH"};
        dataObjects.add(createPickList("CollObjPrepMeth", true, prepMeth));
        
        ////////////////////////////////
        // localities
        ////////////////////////////////
        log.info("Creating localities");
        Locality forestStream = createLocality("Unnamed forest stream", (Geography)geos.get(13));
        forestStream.setLat1text("38.925467 deg N");
        forestStream.setLatitude1(38.925467);
        forestStream.setLong1text("94.984867 deg W");
        forestStream.setLongitude1(-94.984867);

        Locality lake   = createLocality("Deep, dark lake", (Geography)geos.get(18));
        lake.setLat1text("41.548842 deg N");
        lake.setLatitude1(41.548842);
        lake.setLong1text("93.732129 deg W");
        lake.setLongitude1(-93.732129);
        
        Locality farmpond = createLocality("Farm pond", (Geography)geos.get(21));
        farmpond.setLat1text("41.642187 deg N");
        farmpond.setLatitude1(41.642187);
        farmpond.setLong1text("100.403163 deg W");
        farmpond.setLongitude1(-100.403163);

        dataObjects.add(forestStream);
        dataObjects.add(lake);
        dataObjects.add(farmpond);
        
        ////////////////////////////////
        // agents and addresses
        ////////////////////////////////
        log.info("Creating agents and addresses");
        List<Agent> agents = new Vector<Agent>();
        agents.add(createAgent("Mr.", "Joshua", "D", "Stewart", "js"));
        agents.add(createAgent("Mr.", "James", "H", "Beach", "jb"));
        agents.add(createAgent("Mrs.", "Mary Margaret", "H", "Kumin", "mk"));
        agents.add(createAgent("Mr.", "Rod", "C", "Spears", "rs"));
        agents.add(createAgent("Mr.", "Wayne", "J", "Oppenheimer", "wjo"));
        agents.add(createAgent("Sir", "Dudley", "X", "Simmons", "dxs"));
        agents.add(createAgent("Mr.", "Rod", "A", "Carew", "rc"));
        Agent ku = new Agent();
        ku.initialize();
        ku.setAbbreviation("KU");
        ku.setAgentType(Agent.ORG);
        ku.setName("University of Kansas");
        ku.setEmail("webadmin@ku.edu");
        ku.setTimestampCreated(new Date());
        ku.setTimestampModified(ku.getTimestampCreated());
        agents.add(ku);
        agents.get(0).setOrganization(ku);
        agents.get(1).setOrganization(ku);
        agents.get(2).setOrganization(ku);
        agents.get(3).setOrganization(ku);

        List<Address> addrs = new Vector<Address>();
        addrs.add(createAddress(agents.get(0), "11911 S Redbud Ln", null, "Olathe", "KS", "USA", "66061"));
        agents.get(0).setEmail("jds@ku.edu");
        addrs.add(createAddress(agents.get(1), "??? Mississippi", null, "Lawrence", "KS", "USA", "66045"));
        agents.get(0).setEmail("beach@ku.edu");
        addrs.add(createAddress(agents.get(2), "1 Main St", "", "Lenexa", "KS", "USA", "66071"));
        addrs.add(createAddress(agents.get(3), "1335511 Inverness", null, "Lawrence", "KS", "USA", "66047"));
        addrs.add(createAddress(agents.get(6), "1212 Apple Street", null, "Chicago", "IL", "USA", "01010"));
        addrs.add(createAddress(ku, null, null, "Lawrence", "KS", "USA", "66045"));
        addrs.add(createAddress(agents.get(4), "Natural History Museum", "Cromwell Rd", "London", null, "UK", "SW7 5BD"));

        dataObjects.addAll(agents);
        dataObjects.addAll(addrs);
        
        ////////////////////////////////
        // collecting events (collectors, collecting trip)
        ////////////////////////////////
        log.info("Creating collecting events, collectors and a collecting trip");
        Collectors collectorJosh = createCollector(agents.get(0), 2);
        Collectors collectorJim = createCollector(agents.get(1), 1);
        CollectingEvent ce1 = createCollectingEvent(forestStream, new Collectors[]{collectorJosh,collectorJim});
        calendar.set(1993, 3, 19, 11, 56, 00);
        ce1.setStartDate(calendar);
        ce1.setStartDateVerbatim("19 Mar 1993, 11:56 AM");
        calendar.set(1993, 3, 19, 13, 03, 00);
        ce1.setEndDate(calendar);
        ce1.setEndDateVerbatim("19 Mar 1993, 1:03 PM");
        
        AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", null);
        CollectingEventAttr cevAttr = createCollectingEventAttr(ce1, cevAttrDef, "Sleepy Hollow", null);

        Collectors collectorMeg = createCollector(agents.get(2), 1);
        Collectors collectorRod = createCollector(agents.get(3), 2);
        CollectingEvent ce2 = createCollectingEvent(farmpond, new Collectors[]{collectorMeg,collectorRod});
        calendar.set(1993, 3, 20, 06, 12, 00);
        ce2.setStartDate(calendar);
        ce2.setStartDateVerbatim("20 Mar 1993, 6:12 AM");
        calendar.set(1993, 3, 20, 07, 31, 00);
        ce2.setEndDate(calendar);
        ce2.setEndDateVerbatim("20 Mar 1993, 7:31 AM");

        CollectingTrip trip = createCollectingTrip("Sample collecting trip", new CollectingEvent[]{ce1,ce2});
        
        dataObjects.add(collectorJosh);
        dataObjects.add(collectorJim);
        dataObjects.add(collectorMeg);
        dataObjects.add(collectorRod);
        dataObjects.add(ce1);
        dataObjects.add(ce2);
        dataObjects.add(trip);
        dataObjects.add(cevAttrDef);
        dataObjects.add(cevAttr);
        
        ////////////////////////////////
        // permit
        ////////////////////////////////
        log.info("Creating a permit");
        Calendar issuedDate = Calendar.getInstance();
        issuedDate.set(1993, 1, 12);
        Calendar startDate = Calendar.getInstance();
        startDate.set(1993, 2, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(1993, 5, 30);
        Permit permit = createPermit("1993-FISH-0001", "US Dept Fish and Wildlife", issuedDate, startDate, endDate, null);
        permit.setAgentByIssuee(ku);
        permit.setAgentByIssuer(agents.get(4));
        dataObjects.add(permit);
        
        ////////////////////////////////
        // catalog series
        ////////////////////////////////
        log.info("Creating a catalog series");
        CatalogSeries catalogSeries = createCatalogSeries("KUFSH", "Fish", collectionObjDef);
        dataObjects.add(catalogSeries);

        ////////////////////////////////
        // collection objects
        ////////////////////////////////
        log.info("Creating collection objects");

        List<CollectionObject> collObjs = new Vector<CollectionObject>();
        CatalogSeries cs = catalogSeries;
        CollectionObjDef cod = collectionObjDef;
        Calendar catDate = Calendar.getInstance();
        catDate.set(2006, 01, 29);
        collObjs.add(createCollectionObject(100.0f, "RCS100", agents.get(0), cs, cod,  3, ce1, catDate, "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(101.0f, "RCS101", agents.get(0), cs, cod,  2, ce1, catDate, "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(102.0f, "RCS102", agents.get(1), cs, cod,  7, ce1, catDate, "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(103.0f, "RCS103", agents.get(1), cs, cod, 12, ce1, catDate, "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(104.0f, "RCS104", agents.get(2), cs, cod,  8, ce2, catDate, "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(105.0f, "RCS105", agents.get(2), cs, cod,  1, ce2, catDate, "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(106.0f, "RCS106", agents.get(2), cs, cod,  1, ce2, catDate, "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(107.0f, "RCS107", agents.get(3), cs, cod,  1, ce2, catDate, "BuildSampleDatabase"));
        
        AttributeDef colObjAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "MoonPhase", null);
        CollectionObjectAttr colObjAttr = createCollectionObjectAttr(collObjs.get(0), colObjAttrDef, "Full", null);
        dataObjects.addAll(collObjs);
        dataObjects.add(colObjAttrDef);
        dataObjects.add(colObjAttr);
        
        ////////////////////////////////
        // determinations (determination status)
        ////////////////////////////////
        log.info("Creating determinations");
        DeterminationStatus current = createDeterminationStatus("Current","Test Status");
        DeterminationStatus notCurrent = createDeterminationStatus("Not current","Test Status");
        DeterminationStatus incorrect = createDeterminationStatus("Incorrect","Test Status");

        List<Determination> determs = new Vector<Determination>();
        Calendar recent = Calendar.getInstance();
        recent.set(2006, 10, 27, 13, 44, 00);
        Calendar longAgo = Calendar.getInstance();
        longAgo.set(1976, 01, 29, 8, 12, 00);
        Calendar whileBack = Calendar.getInstance();
        whileBack.set(2002, 7, 4, 9, 33, 12);
        determs.add(createDetermination(collObjs.get(0), agents.get(0), (Taxon)taxa.get( 8), current, recent));
        determs.add(createDetermination(collObjs.get(1), agents.get(0), (Taxon)taxa.get( 9), current, recent));
        determs.add(createDetermination(collObjs.get(2), agents.get(0), (Taxon)taxa.get(10), current, recent));
        determs.add(createDetermination(collObjs.get(3), agents.get(0), (Taxon)taxa.get(11), current, recent));
        determs.add(createDetermination(collObjs.get(4), agents.get(0), (Taxon)taxa.get(12), current, recent));
        determs.add(createDetermination(collObjs.get(5), agents.get(0), (Taxon)taxa.get(13), current, recent));
        determs.add(createDetermination(collObjs.get(6), agents.get(3), (Taxon)taxa.get(14), current, recent));
        determs.add(createDetermination(collObjs.get(7), agents.get(4), (Taxon)taxa.get(15), current, recent));
        
        determs.add(createDetermination(collObjs.get(0), agents.get(0), (Taxon)taxa.get( 8), notCurrent, longAgo));
        determs.add(createDetermination(collObjs.get(1), agents.get(1), (Taxon)taxa.get(15), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(2), agents.get(1), (Taxon)taxa.get(16), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(3), agents.get(2), (Taxon)taxa.get(17), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(4), agents.get(2), (Taxon)taxa.get(17), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(4), agents.get(3), (Taxon)taxa.get(20), incorrect, longAgo));
        determs.add(createDetermination(collObjs.get(4), agents.get(4), (Taxon)taxa.get(19), incorrect, longAgo));
        determs.get(13).setRemarks("This determination is totally wrong.  What a foolish determination.");
        
        dataObjects.add(current);
        dataObjects.add(notCurrent);
        dataObjects.add(incorrect);
        dataObjects.addAll(determs);
        
        ////////////////////////////////
        // preparations (prep types)
        ////////////////////////////////
        log.info("Creating preparations");
        PrepType skel = createPrepType("skeleton");
        PrepType cas = createPrepType("C&S");
        PrepType etoh = createPrepType("EtOH");
        PrepType xray = createPrepType("x-ray");

        List<Preparation> preps = new Vector<Preparation>();
        preps.add(createPreparation(etoh, agents.get(0), collObjs.get(0), (Location)locs.get(8), rand.nextInt(20)));
        preps.add(createPreparation(etoh, agents.get(0), collObjs.get(1), (Location)locs.get(8), rand.nextInt(20)));
        preps.add(createPreparation(etoh, agents.get(1), collObjs.get(2), (Location)locs.get(8), rand.nextInt(20)));
        preps.add(createPreparation(etoh, agents.get(1), collObjs.get(3), (Location)locs.get(8), rand.nextInt(20)));
        preps.add(createPreparation(etoh, agents.get(2), collObjs.get(4), (Location)locs.get(9), rand.nextInt(20)));
        preps.add(createPreparation(etoh, agents.get(2), collObjs.get(5), (Location)locs.get(9), rand.nextInt(20)));
        preps.add(createPreparation(etoh, agents.get(3), collObjs.get(6), (Location)locs.get(9), rand.nextInt(20)));
        preps.add(createPreparation(etoh, agents.get(3), collObjs.get(7), (Location)locs.get(9), rand.nextInt(20)));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(0), (Location)locs.get(12), rand.nextInt(20)));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(1), (Location)locs.get(12), rand.nextInt(20)));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(2), (Location)locs.get(11), rand.nextInt(20)));
        preps.add(createPreparation(skel, agents.get(2), collObjs.get(3), (Location)locs.get(10), rand.nextInt(20)));
        preps.add(createPreparation(skel, agents.get(3), collObjs.get(4), (Location)locs.get(10), rand.nextInt(20)));
        preps.add(createPreparation(skel, agents.get(0), collObjs.get(5), (Location)locs.get(10), rand.nextInt(20)));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(6), (Location)locs.get(10), rand.nextInt(20)));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(7), (Location)locs.get(10), rand.nextInt(20)));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(2), (Location)locs.get(9), rand.nextInt(20)));

        preps.add(createPreparation(xray, agents.get(1), collObjs.get(0), (Location)locs.get(8), rand.nextInt(20)));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(1), (Location)locs.get(8), rand.nextInt(20)));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(2), (Location)locs.get(9), rand.nextInt(20)));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(3), (Location)locs.get(9), rand.nextInt(20)));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(4), (Location)locs.get(10), rand.nextInt(20)));

        dataObjects.add(skel);
        dataObjects.add(cas);
        dataObjects.add(etoh);
        dataObjects.add(xray);
        dataObjects.addAll(preps);
        
        ////////////////////////////////
        // accessions (accession agents)
        ////////////////////////////////
        log.info("Creating accessions and accession agents");
        calendar.set(2006, 10, 27, 23, 59, 59);
        Accession acc1 = createAccession("gift", "complete", "2006-IC-001", DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        
        Agent donor =    agents.get(4);
        Agent receiver = agents.get(1);
        Agent reviewer = agents.get(2);
        
        List<AccessionAgents> accAgents = new Vector<AccessionAgents>();
        
        accAgents.add(createAccessionAgent("donor", donor, acc1, null));
        accAgents.add(createAccessionAgent("receiver", receiver, acc1, null));
        accAgents.add(createAccessionAgent("reviewer", reviewer, acc1, null));

        Accession acc2 = createAccession("field work", "in process", "2006-IC-002", DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        
        Agent donor2 =    agents.get(5);
        Agent receiver2 = agents.get(3);
        Agent reviewer2 = agents.get(1);
        
        accAgents.add(createAccessionAgent("donor", donor2, acc2, null));
        accAgents.add(createAccessionAgent("receiver", receiver2, acc2, null));
        accAgents.add(createAccessionAgent("reviewer", reviewer2, acc2, null));

        dataObjects.add(acc1);
        dataObjects.add(acc2);
        dataObjects.addAll(accAgents);

        ////////////////////////////////
        // loans (loan agents, shipments)
        ////////////////////////////////
        log.info("Creating loans, loan agents, and shipments");
        Calendar loanDate1 = Calendar.getInstance();
        loanDate1.set(2004, 03, 19);
        Calendar currentDueDate1 = Calendar.getInstance();
        currentDueDate1.set(2004, 9, 19);
        Calendar originalDueDate1 = currentDueDate1;
        Calendar dateClosed1 = Calendar.getInstance();
        dateClosed1.set(2004, 7, 4);
      
        List<LoanPhysicalObject> loanPhysObjs = new Vector<LoanPhysicalObject>();
        
        Loan closedLoan = createLoan("2006-001", loanDate1, currentDueDate1, originalDueDate1, 
                                     dateClosed1, Loan.LOAN, Loan.CLOSED, null);
        for (int i = 0; i < 7; ++i)
        {
            Preparation p = getObjectByClass(preps, Preparation.class, rand.nextInt(preps.size()));
            int available = p.getAvailable();
            if (available<1)
            {
                // retry
                i--;
                continue;
            }
            int quantity = Math.max(1,rand.nextInt(available));
            LoanPhysicalObject lpo = DataBuilder.createLoanPhysicalObject((short)quantity, null, null, null, (short)0, (short)0, p, closedLoan);
            loanPhysObjs.add(lpo);
            p.getLoanPhysicalObjects().add(lpo);
        }
        
        Calendar loanDate2 = Calendar.getInstance();
        loanDate2.set(2005, 11, 24);
        Calendar currentDueDate2 = Calendar.getInstance();
        currentDueDate2.set(2006, 5, 24);
        Calendar originalDueDate2 = currentDueDate2;
        Loan overdueLoan = createLoan("2006-002", loanDate2, currentDueDate2, originalDueDate2,  
                                      null, Loan.LOAN, Loan.OPEN, null);
        for (int i = 0; i < 5; ++i)
        {
            Preparation p = getObjectByClass(preps, Preparation.class, rand.nextInt(preps.size()));
            int available = p.getAvailable();
            if (available<1)
            {
                // retry
                i--;
                continue;
            }
            int quantity = Math.max(1,rand.nextInt(available));
            LoanPhysicalObject lpo = createLoanPhysicalObject((short)quantity, null, null, null, (short)0, (short)0, p, overdueLoan);
            loanPhysObjs.add(lpo);
            p.getLoanPhysicalObjects().add(lpo);
        }

        Calendar loanDate3 = Calendar.getInstance();
        loanDate3.set(2006, 3, 21);
        Calendar currentDueDate3 = Calendar.getInstance();
        currentDueDate3.set(2007, 3, 21);
        Calendar originalDueDate3 = Calendar.getInstance();
        originalDueDate3.set(2006, 9, 21);
        Loan loan3 = createLoan("2006-003", loanDate3, currentDueDate3, originalDueDate3,  
                                      null, Loan.LOAN, Loan.OPEN, null);
        Vector<LoanPhysicalObject> newLoanLPOs = new Vector<LoanPhysicalObject>();
        int lpoCountInNewLoan = 0;
        // put some LPOs in this loan that are from CollObjs that have other preps loaned out already
        // this algorithm (because of the randomness) can result in this loan having 0 LPOs.
        for( LoanPhysicalObject lpo: loanPhysObjs)
        {
            int available = lpo.getPreparation().getAvailable();
            if (available > 0)
            {
                int quantity = Math.max(1,rand.nextInt(available));
                LoanPhysicalObject newLPO = createLoanPhysicalObject((short)quantity, null, null, null, (short)0, (short)0, lpo.getPreparation(), loan3);
                newLoanLPOs.add(newLPO);
                lpo.getPreparation().getLoanPhysicalObjects().add(newLPO);
                
                // stop after we put 6 LPOs in the new loan
                lpoCountInNewLoan++;
                if(lpoCountInNewLoan==6)
                {
                    break;
                }
            }
        }
        
        // create some LoanReturnPhysicalObjects
        Vector<LoanReturnPhysicalObject> returns = new Vector<LoanReturnPhysicalObject>();
        for (int i = 0; i < 5; ++i)
        {
            LoanPhysicalObject lpo = getObjectByClass(loanPhysObjs, LoanPhysicalObject.class, rand.nextInt(loanPhysObjs.size()));
            int quantityReturned = Math.max(1, lpo.getQuantity());
            Calendar returnedDate = Calendar.getInstance();
            returnedDate.setTime(lpo.getLoan().getLoanDate().getTime());
            // make the returned date be a little while after the original loan
            returnedDate.add(Calendar.DAY_OF_YEAR, 72);
            LoanReturnPhysicalObject lrpo = createLoanReturnPhysicalObject(returnedDate, (short)quantityReturned, lpo, null, agents.get(0));
            lpo.addLoanReturnPhysicalObjects(lrpo);
            lpo.setQuantityReturned(lrpo.getQuantity());
            returns.add(lrpo);
        }

        dataObjects.add(closedLoan);
        dataObjects.add(overdueLoan);
        dataObjects.add(loan3);
        dataObjects.addAll(loanPhysObjs);
        dataObjects.addAll(newLoanLPOs);
        dataObjects.addAll(returns);
        
        LoanAgents loanAgent1 = createLoanAgent("loaner", closedLoan, agents.get(1));
        LoanAgents loanAgent2 = createLoanAgent("loaner", overdueLoan, agents.get(3));
        LoanAgents loanAgent3 = createLoanAgent("borrower", closedLoan, agents.get(4));
        LoanAgents loanAgent4 = createLoanAgent("borrower", overdueLoan, agents.get(4));
        dataObjects.add(loanAgent1);
        dataObjects.add(loanAgent2);
        dataObjects.add(loanAgent3);
        dataObjects.add(loanAgent4);
        
        Calendar ship1Date = Calendar.getInstance();
        ship1Date.set(2004, 03, 19);
        Shipment loan1Ship = createShipment(ship1Date, "2006-001", "USPS", (short) 1, "1.25 kg", null, agents.get(0), agents.get(4), agents.get(0));
        
        Calendar ship2Date = Calendar.getInstance();
        ship2Date.set(2005, 11, 24);
        Shipment loan2Ship = createShipment(ship2Date, "2006-002", "FedEx", (short) 2, "6.0 kg", null, agents.get(3), agents.get(4), agents.get(3));
        
        closedLoan.setShipment(loan1Ship);
        overdueLoan.setShipment(loan2Ship);
        
        dataObjects.add(loan1Ship);
        dataObjects.add(loan2Ship);

        ////////////////////////////////
        // attachments (attachment metadata)
        ////////////////////////////////
        if (false)
        {
            log.info("Creating attachments and attachment metadata");
            try
            {
                String attachmentFilesLoc = "demo_files" + File.separator;
                String bigEyeFilePath = attachmentFilesLoc + "bigeye.jpg";
                Attachment bigEye = createAttachment(bigEyeFilePath, "image/jpeg", 0);
                bigEye.setLoan(closedLoan);
                
                String joshPhotoPath = attachmentFilesLoc + "josh.jpg";
                Attachment joshPhoto = createAttachment(joshPhotoPath, "image/jpeg", 0);
                joshPhoto.setAgent(agents.get(0));
    
                String beachPhotoPath = attachmentFilesLoc + "beach.jpg";
                Attachment beachPhoto = createAttachment(beachPhotoPath, "image/jpeg", 2);
                beachPhoto.setAgent(agents.get(1));
    
                String megPhotoPath = attachmentFilesLoc + "meg.jpg";
                Attachment megPhoto = createAttachment(megPhotoPath, "image/jpeg", 0);
                megPhoto.setAgent(agents.get(2));
    
                String rodPhotoPath = attachmentFilesLoc + "rod.jpg";
                Attachment rodPhoto = createAttachment(rodPhotoPath, "image/jpeg", 0);
                rodPhoto.setAgent(agents.get(3));
    
                String giftPdfPath = attachmentFilesLoc + "2004-18.pdf";
                Attachment giftPDF = createAttachment(giftPdfPath, "application/pdf", 0);
                giftPDF.setLoan(closedLoan);
                
                String accessionPdfPath = attachmentFilesLoc + "Seychelles.pdf";
                Attachment accPDF = createAttachment(accessionPdfPath, "application/pdf", 0);
                // TODO: change this to setAccession()
                accPDF.setPermit(permit);
                
                String sharkVideoPath = attachmentFilesLoc + "shark5.mpg";
                Attachment sharkVideo = createAttachment(sharkVideoPath, "video/mpeg4", 0);
                sharkVideo.setLoan(closedLoan);
    
                String beakerPath = attachmentFilesLoc + "beaker.jpg";
                Attachment beakerAsBeach = createAttachment(beakerPath, "image/jpg", 1);
                beakerAsBeach.setAgent(agents.get(1));
                
                dataObjects.add(bigEye);
                dataObjects.add(joshPhoto);
                dataObjects.add(beachPhoto);
                dataObjects.add(megPhoto);
                dataObjects.add(rodPhoto);
                dataObjects.add(giftPDF);
                dataObjects.add(accPDF);
                dataObjects.add(sharkVideo);
                dataObjects.add(beakerAsBeach);
            }
            catch (Exception e)
            {
                log.error("Could not create attachments", e);
            }
        }
        // done
        log.info("Done creating single discipline database: " + disciplineName);
        return dataObjects;
    }


    public static List<Object> createSimpleGeography(final CollectionObjDef colObjDef, final String treeDefName)
    {
        log.info("createSimpleGeography " + treeDefName);

        List<Object> newObjs = new Vector<Object>();

        // Create a geography tree definition (and tie it to the CollectionObjDef)
        GeographyTreeDef geoTreeDef = createGeographyTreeDef(treeDefName);
        geoTreeDef.getCollObjDefs().add(colObjDef);
        colObjDef.setGeographyTreeDef(geoTreeDef);
        // 0
        newObjs.add(geoTreeDef);
        
        // create the geo tree def items
        GeographyTreeDefItem root = createGeographyTreeDefItem(null, geoTreeDef, "GeoRoot", 0);
        GeographyTreeDefItem cont = createGeographyTreeDefItem(root, geoTreeDef, "Continent", 100);
        GeographyTreeDefItem country = createGeographyTreeDefItem(cont, geoTreeDef, "Country", 200);
        GeographyTreeDefItem state = createGeographyTreeDefItem(country, geoTreeDef, "State", 300);
        state.setIsInFullName(true);
        GeographyTreeDefItem county = createGeographyTreeDefItem(state, geoTreeDef, "County", 400);
        county.setIsInFullName(true);
        // 1
        newObjs.add(root);
        // 2
        newObjs.add(cont);
        // 3
        newObjs.add(country);
        // 4
        newObjs.add(state);
        // 5
        newObjs.add(county);

        // Create the planet Earth.
        // That seems like a big task for 5 lines of code.
        Geography earth = createGeography(geoTreeDef, null, "Earth", root.getRankId());
        Geography northAmerica = createGeography(geoTreeDef, earth, "North America", cont.getRankId());
        Geography us = createGeography(geoTreeDef, northAmerica, "United States", country.getRankId());
        List<Geography> states = createGeographyChildren(geoTreeDef, northAmerica,
                new String[] { "Kansas", "Iowa", "Nebraska" }, state.getRankId());
        // 6
        newObjs.add(earth);
        // 7
        newObjs.add(northAmerica);
        // 8
        newObjs.add(us);
        // 9, 10, 11
        newObjs.addAll(states);

        
        
        // Create Kansas and a few counties
        List<Geography> counties = createGeographyChildren(geoTreeDef, states.get(0),
                new String[] { "Douglas", "Johnson", "Osage", "Sedgwick" }, county.getRankId());
        // 12, 13, 14, 15
        newObjs.addAll(counties);
        counties = createGeographyChildren(geoTreeDef, states.get(1),
                new String[] { "Blackhawk", "Fayette", "Polk", "Woodbury" }, county.getRankId());
        // 16, 17, 18, 19
        newObjs.addAll(counties);
        counties = createGeographyChildren(geoTreeDef, states.get(2),
                new String[] { "Dakota", "Logan", "Valley", "Wheeler" }, county.getRankId());
        // 20, 21, 22, 23
        newObjs.addAll(counties);
        
        earth.fixFullNameForAllDescendants();

        return newObjs;
    }


    public static List<Object> createSimpleGeologicTimePeriod(final CollectionObjDef colObjDef,
                                                              final String treeDefName)
    {
        log.info("createSimpleGeologicTimePeriod " + treeDefName);

        List<Object> newObjs = new Vector<Object>();

        // Create a geography tree definition
        GeologicTimePeriodTreeDef treeDef = createGeologicTimePeriodTreeDef(treeDefName);
        treeDef.getCollObjDefs().add(colObjDef);
        colObjDef.setGeologicTimePeriodTreeDef(treeDef);
        newObjs.add(treeDef);

        GeologicTimePeriodTreeDefItem defItemLevel0 = createGeologicTimePeriodTreeDefItem(
                null, treeDef, "Level 0", 0);
        GeologicTimePeriodTreeDefItem defItemLevel1 = createGeologicTimePeriodTreeDefItem(
                defItemLevel0, treeDef, "Level 1", 100);
        GeologicTimePeriodTreeDefItem defItemLevel2 = createGeologicTimePeriodTreeDefItem(
                defItemLevel1, treeDef, "Level 2", 200);
        GeologicTimePeriodTreeDefItem defItemLevel3 = createGeologicTimePeriodTreeDefItem(
                defItemLevel2, treeDef, "Level 3", 300);
        newObjs.add(defItemLevel0);
        newObjs.add(defItemLevel1);
        newObjs.add(defItemLevel2);
        newObjs.add(defItemLevel3);

        // Create the defItemLevel0
        GeologicTimePeriod level0 = createGeologicTimePeriod(treeDef, null,
                "Time As We Know It", 10.0f, 0.0f, defItemLevel0.getRankId());
        GeologicTimePeriod level1 = createGeologicTimePeriod(treeDef, level0,
                "Some Really Big Time Period", 5.0f, 0.0f, defItemLevel0.getRankId());
        GeologicTimePeriod level2 = createGeologicTimePeriod(treeDef, level1,
                "A Slightly Smaller Time Period", 1.74f, 0.0f, defItemLevel0.getRankId());
        GeologicTimePeriod level3_1 = createGeologicTimePeriod(treeDef, level2,
                "Yesterday", 0.1f, 0.0f, defItemLevel0.getRankId());
        GeologicTimePeriod level3_2 = createGeologicTimePeriod(treeDef, level2,
                "A couple of days ago", 0.2f, 0.1f, defItemLevel0.getRankId());
        GeologicTimePeriod level3_3 = createGeologicTimePeriod(treeDef, level2,
                "Last week", 0.7f, 1.4f, defItemLevel0.getRankId());
        newObjs.add(level0);
        newObjs.add(level1);
        newObjs.add(level2);
        newObjs.add(level3_1);
        newObjs.add(level3_2);
        newObjs.add(level3_3);

        level0.fixFullNameForAllDescendants();
        
        return newObjs;
    }


    public static List<Object> createSimpleLocation(final CollectionObjDef colObjDef, final String treeDefName)
    {
        log.info("createSimpleLocation " + treeDefName);

        List<Object> newObjs = new Vector<Object>();

        // Create a geography tree definition
        LocationTreeDef locTreeDef = createLocationTreeDef(treeDefName);
        locTreeDef.getCollObjDefs().add(colObjDef);
        colObjDef.setLocationTreeDef(locTreeDef);

        LocationTreeDefItem building = createLocationTreeDefItem(null, locTreeDef, "building", 0);
        building.setIsEnforced(true);
        LocationTreeDefItem room = createLocationTreeDefItem(building, locTreeDef, "room", 100);
        room.setIsInFullName(true);
        LocationTreeDefItem freezer = createLocationTreeDefItem(room, locTreeDef, "freezer", 200);
        freezer.setIsInFullName(true);
        LocationTreeDefItem shelf = createLocationTreeDefItem(freezer, locTreeDef, "shelf", 300);
        shelf.setIsInFullName(true);

        // Create the building
        Location dyche = createLocation(locTreeDef, null, "Dyche Hall", building.getRankId());
        Location rm606 = createLocation(locTreeDef, dyche, "Room 606", room.getRankId());
        Location freezerA = createLocation(locTreeDef, rm606, "Freezer A", freezer.getRankId());
        Location shelf5 = createLocation(locTreeDef, freezerA, "Shelf 5", shelf.getRankId());
        Location shelf4 = createLocation(locTreeDef, freezerA, "Shelf 4", shelf.getRankId());
        Location shelf3 = createLocation(locTreeDef, freezerA, "Shelf 3", shelf.getRankId());
        Location shelf2 = createLocation(locTreeDef, freezerA, "Shelf 2", shelf.getRankId());
        Location shelf1 = createLocation(locTreeDef, freezerA, "Shelf 1", shelf.getRankId());

        // 0
        newObjs.add(locTreeDef);
        // 1
        newObjs.add(building);
        // 2
        newObjs.add(room);
        // 3
        newObjs.add(freezer);
        // 4
        newObjs.add(shelf);
        // 5
        newObjs.add(dyche);
        // 6
        newObjs.add(rm606);
        // 7
        newObjs.add(freezerA);
        // 8
        newObjs.add(shelf5);
        // 9
        newObjs.add(shelf4);
        // 10
        newObjs.add(shelf3);
        // 11
        newObjs.add(shelf2);
        // 12
        newObjs.add(shelf1);
        
        dyche.fixFullNameForAllDescendants();
        
        return newObjs;
    }


    public static List<Object> createSimpleTaxon(final TaxonTreeDef taxonTreeDef)
    {
        log.info("createSimpleTaxon " + taxonTreeDef.getName());

        Vector<Object> newObjs = new Vector<Object>();
        // Create a Taxon tree definition
        TaxonTreeDefItem defItemLevel0 = createTaxonTreeDefItem(null, taxonTreeDef, "order", 0);
        defItemLevel0.setIsEnforced(true);
        TaxonTreeDefItem defItemLevel1 = createTaxonTreeDefItem(defItemLevel0, taxonTreeDef, "family", 100);
        TaxonTreeDefItem defItemLevel2 = createTaxonTreeDefItem(defItemLevel1, taxonTreeDef, "genus", 200);
        defItemLevel2.setIsEnforced(true);
        defItemLevel2.setIsInFullName(true);
        TaxonTreeDefItem defItemLevel3 = createTaxonTreeDefItem(defItemLevel2, taxonTreeDef, "species", 300);
        defItemLevel3.setIsEnforced(true);
        defItemLevel3.setIsInFullName(true);

        // 0
        newObjs.add(defItemLevel0);
        // 1
        newObjs.add(defItemLevel1);
        // 2
        newObjs.add(defItemLevel2);
        // 3
        newObjs.add(defItemLevel3);

        // Create the defItemLevel0
        Taxon order = createTaxon(taxonTreeDef, null, "Percidae", defItemLevel0.getRankId());
        Taxon family = createTaxon(taxonTreeDef, order, "Perciformes", defItemLevel1.getRankId());
        Taxon genus = createTaxon(taxonTreeDef, family, "Ammocrypta", defItemLevel2.getRankId());
        // 4
        newObjs.add(order);
        // 5
        newObjs.add(family);
        // 6
        newObjs.add(genus);

        String[] speciesNames = { "asprella", "beanii", "bifascia", "clara", "meridiana", "pellucida", "platostomus" };
        List<Object> kids = createTaxonChildren(taxonTreeDef, genus, speciesNames, defItemLevel3.getRankId());
        // 7, 8, 9, 10, 11, 12, 13
        newObjs.addAll(kids);

        genus = createTaxon(taxonTreeDef, order, "Caranx", defItemLevel2.getRankId());
        // 14
        newObjs.add(genus);
        String[] speciesNames2 = { "bartholomaei", "caballus", "caninus", "crysos", "dentex", "hippos", "latus" };
        kids = createTaxonChildren(taxonTreeDef, genus, speciesNames2, defItemLevel3.getRankId());
        // 15, 16, 17, 18, 19, 20, 21
        newObjs.addAll(kids);
        
        order.fixFullNameForAllDescendants();
        
        return newObjs;
    }


    public static void persist(Object o)
    {
        if (session != null)
        {
            session.saveOrUpdate(o);
        }
    }


    public static void persist(Object[] oArray)
    {
        for (Object o: oArray)
        {
            persist(o);
        }
    }


    public static void persist(List<?> oList)
    {
        for (Object o: oList)
        {
            persist(o);
        }
    }


    public static void startTx()
    {
        HibernateUtil.beginTransaction();
    }


    public static void commitTx()
    {
        HibernateUtil.commitTransaction();
    }
    

    public static void rollbackTx()
    {
        HibernateUtil.rollbackTransaction();
    }
    

    public static Object getFirstObjectByClass( List<Object> objects, Class<?> clazz)
    {
        Object ret = null;
        for (Object o: objects)
        {
            if (o.getClass() == clazz)
            {
                ret = o;
                break;
            }
        }
        return ret;
    }
    

    @SuppressWarnings("unchecked")
    public static <T> T getObjectByClass( List<?> objects, Class<T> clazz, int index)
    {
        T ret = null;
        int i = -1;
        for (Object o: objects)
        {
            if (o.getClass() == clazz)
            {
                ++i;
            }
            if (i==index)
            {
                ret = (T)o;
                break;
            }
        }
        return ret;
    }

    public static List<?> getObjectsByClass( List<Object> objects, Class<?> clazz)
    {
        Vector<Object> rightClass = new Vector<Object>();
        for (Object o: objects)
        {
            if (o.getClass() == clazz)
            {
                rightClass.add(o);
            }
        }
        return rightClass;
        
    }
    
    public static void main(String[] args) throws Exception
    {
        String databaseName = "testfish";
        String databaseHost = "localhost";
        String userName = "rods";
        String password = "rods";

        SpecifySchemaGenerator schemaGen = new SpecifySchemaGenerator();
        schemaGen.generateSchema(databaseHost, databaseName);

        //HibernateUtil.setListener("post-commit-update", new edu.ku.brc.specify.dbsupport.PostUpdateEventListener());
        HibernateUtil.setListener("post-commit-insert", new edu.ku.brc.specify.dbsupport.PostInsertEventListener());
        //HibernateUtil.setListener("post-commit-delete", new edu.ku.brc.specify.dbsupport.PostDeleteEventListener());
        //HibernateUtil.setListener("delete", new edu.ku.brc.specify.dbsupport.DeleteEventListener());

        if (UIHelper.tryLogin("com.mysql.jdbc.Driver",
                                "org.hibernate.dialect.MySQLDialect",
                                databaseName,
                                "jdbc:mysql://" + databaseHost + "/" + databaseName,
                                userName,
                                password))
        {
            boolean single = true;
            if (single)
            {
                try
                {
                    Thumbnailer thumb = new Thumbnailer();
                    thumb.registerThumbnailers("config/thumbnail_generators.xml");
                    thumb.setQuality(.5f);
                    thumb.setMaxHeight(128);
                    thumb.setMaxWidth(128);

                    AttachmentManagerIface attachMgr;
                    attachMgr = new FileStoreAttachmentManager("demo_files/AttachmentStorage/");
                    
                    AttachmentUtils.setAttachmentManager(attachMgr);
                    AttachmentUtils.setThumbnailer(thumb);
                    
                    List<Object> dataObjects = createSingleDiscipline("Fish", "fish");

                    log.info("Persisting in-memory objects to DB");
                    
                    // save it all to the DB
                    setSession(HibernateUtil.getCurrentSession());

                    startTx();
                    //persist(dataObjects.get(0)); // just persist the CollectionObjDef object
                    persist(dataObjects);
                    commitTx();
                    
                    attachMgr.cleanup();
                    
                    log.info("Done");
                }
                catch(Exception e)
                {
                    try
                    {
                        rollbackTx();
                        log.error("Failed to persist DB objects", e);
                    }
                    catch(Exception e2)
                    {
                        log.error("Failed to persist DB objects.  Rollback failed.  DB may be in inconsistent state.", e2);
                    }
                }
            }
        }
    }
}
