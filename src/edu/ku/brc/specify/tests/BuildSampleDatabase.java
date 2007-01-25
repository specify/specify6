
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
import static edu.ku.brc.specify.tests.DataBuilder.createJournal;
import static edu.ku.brc.specify.tests.DataBuilder.createLoan;
import static edu.ku.brc.specify.tests.DataBuilder.createLoanAgent;
import static edu.ku.brc.specify.tests.DataBuilder.createLoanPhysicalObject;
import static edu.ku.brc.specify.tests.DataBuilder.createLoanReturnPhysicalObject;
import static edu.ku.brc.specify.tests.DataBuilder.createLocality;
import static edu.ku.brc.specify.tests.DataBuilder.createLocation;
import static edu.ku.brc.specify.tests.DataBuilder.createLocationTreeDef;
import static edu.ku.brc.specify.tests.DataBuilder.createLocationTreeDefItem;
import static edu.ku.brc.specify.tests.DataBuilder.createPermit;
import static edu.ku.brc.specify.tests.DataBuilder.createPickList;
import static edu.ku.brc.specify.tests.DataBuilder.createPrepType;
import static edu.ku.brc.specify.tests.DataBuilder.createPreparation;
import static edu.ku.brc.specify.tests.DataBuilder.createReferenceWork;
import static edu.ku.brc.specify.tests.DataBuilder.createShipment;
import static edu.ku.brc.specify.tests.DataBuilder.createSpecifyUser;
import static edu.ku.brc.specify.tests.DataBuilder.createTaxon;
import static edu.ku.brc.specify.tests.DataBuilder.createTaxonChildren;
import static edu.ku.brc.specify.tests.DataBuilder.createTaxonTreeDef;
import static edu.ku.brc.specify.tests.DataBuilder.createTaxonTreeDefItem;
import static edu.ku.brc.specify.tests.DataBuilder.createUserGroup;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
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
import edu.ku.brc.specify.datamodel.Journal;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanAgents;
import edu.ku.brc.specify.datamodel.LoanPhysicalObject;
import edu.ku.brc.specify.datamodel.LoanReturnPhysicalObject;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityCitation;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonCitation;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
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
    protected static Session session;
    protected static Random     rand = new Random(12345678L);
    
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
        UserGroup    userGroup    = createUserGroup(disciplineName);
        SpecifyUser  user         = createSpecifyUser("rods", "rods@ku.edu", (short) 0, userGroup, "CollectionManager");
        DataType     dataType     = createDataType(disciplineName);
        TaxonTreeDef taxonTreeDef = createTaxonTreeDef("Sample Taxon Tree Def");
        CollectionObjDef collectionObjDef = createCollectionObjDef(colObjDefName, disciplineName, dataType, user, taxonTreeDef, null, null, null);
        //dataType.addCollectionObjDef(collectionObjDef);
        dataObjects.add(collectionObjDef);
        dataObjects.add(userGroup);
        dataObjects.add(user);
        dataObjects.add(dataType);
        dataObjects.add(taxonTreeDef);
        
        Journal journal = createJournalsAndReferenceWork();
        List<ReferenceWork> rwList = new Vector<ReferenceWork>();
        rwList.addAll(journal.getReferenceWorks());
        dataObjects.add(journal);
        for (ReferenceWork rw : rwList)
        {
            dataObjects.add(rw);
        }
        
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

        int tableType      = PickListDBAdapterIFace.Type.Table.ordinal();
        int tableFieldType = PickListDBAdapterIFace.Type.TableField.ordinal();
        
        //                                 Name                Type           Table Name           Field       Formatter          R/O  Size
        dataObjects.add(createPickList("DeterminationStatus", tableType,      "determinationstatus", null, "DeterminationStatus", true, -1));
        dataObjects.add(createPickList("DataType",            tableType,      "datatype",            null, "DataType",            true, -1));
        dataObjects.add(createPickList("Department",          tableFieldType, "accession",         "text1" ,       null,          true, -1));
        dataObjects.add(createPickList("AgentTitle",          tableFieldType, "agent",             "title" ,       null,          true, -1));
        
        String[] types = {"State", "Federal", "International", "US Dept Fish and Wildlife", "<no data>"};
        dataObjects.add(createPickList("PermitType", true, types));

        //String[] titles = {"Dr.", "Mr.", "Ms.", "Mrs.", "Sir"};
        //dataObjects.add(createPickList("AgentTitle", true, titles));
        
        String[] roles = {"Borrower", "Receiver"};
        dataObjects.add(createPickList("LoanAgentsRole", true, roles));
        
        String[] sexes = {"Both", "Female", "Male", "Unknown"};
        dataObjects.add(createPickList("BiologicalSex", true, sexes));
        
        String[] status = {"Complete", "In Process", "<no data>"};
        dataObjects.add(createPickList("AccessionStatus", true, status));
        
        String[] methods = {"By Hand", "USPS", "UPS", "FedEx", "DHL"};
        dataObjects.add(createPickList("ShipmentMethod", true, methods));
        
        String[] accTypes = {"Collection","Gift"};
        dataObjects.add(createPickList("AccessionType", true, accTypes));
        
        String[] accRoles = {"Collector", "Donor", "Reviewer", "Staff", "Receiver"};
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
        String POINT = "Point";
        String LINE  = "Line";
        String RECT  = "Rectangle";
        
        log.info("Creating localities");
        Locality forestStream = createLocality("Unnamed forest stream pond", (Geography)geos.get(13));
        forestStream.setLatLongType(POINT);
        forestStream.setOriginalLatLongUnit(0);
        forestStream.setLat1text("38.925467 deg N");
        forestStream.setLatitude1(new BigDecimal(38.925467));
        forestStream.setLong1text("94.984867 deg W");
        forestStream.setLongitude1(new BigDecimal(-94.984867));

        Locality lake   = createLocality("Deep, dark lake pond", (Geography)geos.get(18));
        lake.setLatLongType(RECT);
        lake.setOriginalLatLongUnit(1);
        lake.setLat1text("41.548842 deg N");
        lake.setLatitude1(new BigDecimal(41.548842));
        lake.setLong1text("93.732129 deg W");
        lake.setLongitude1(new BigDecimal(-93.732129));
        
        lake.setLat2text("41.642195 deg N");
        lake.setLatitude2(new BigDecimal(41.642195));
        lake.setLong2text("100.403180 deg W");
        lake.setLongitude2(new BigDecimal(-100.403180));
        
        Locality farmpond = createLocality("Farm pond", (Geography)geos.get(22));
        farmpond.setLatLongType(LINE);
        farmpond.setOriginalLatLongUnit(2);
        farmpond.setLat1text("41.642187 deg N");
        farmpond.setLatitude1(new BigDecimal(41.642187));
        farmpond.setLong1text("100.403163 deg W");
        farmpond.setLongitude1(new BigDecimal(-100.403163));

        farmpond.setLat2text("49.647435 deg N");
        farmpond.setLatitude2(new BigDecimal(49.647435));
        farmpond.setLong2text("-55.112163 deg W");
        farmpond.setLongitude2(new BigDecimal(-55.112163));

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
        agents.add(createAgent("Mr.", "Andy", "D", "Bentley", "AB"));
        agents.add(createAgent("Sir", "Dudley", "X", "Simmons", "dxs"));
        agents.add(createAgent("Mr.", "Rod", "A", "Carew", "rc"));
        
        // e-mail addresses
        agents.get(0).setEmail("jds@ku.edu");
        agents.get(1).setEmail("beach@ku.edu");
        agents.get(2).setEmail("megkumin@ku.edu");
        agents.get(3).setEmail("rods@ku.edu");
        
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
        // Josh
        addrs.add(createAddress(agents.get(0), "11911 S Redbud Ln", null, "Olathe", "KS", "USA", "66061"));
        // Jim
        addrs.add(createAddress(agents.get(1), "??? Mississippi", null, "Lawrence", "KS", "USA", "66045"));
        // Meg
        addrs.add(createAddress(agents.get(2), "1 Main St", "", "Lenexa", "KS", "USA", "66071"));
        // Rod
        addrs.add(createAddress(agents.get(3), "13355 Inverness", "Bldg #3", "Lawrence", "KS", "USA", "66047"));
        // Wayne Oppenheimer
        addrs.add(createAddress(agents.get(4), "Natural History Museum", "Cromwell Rd", "London", null, "UK", "SW7 5BD"));
        // no address for agent 5 (Dudley Simmons)
        // Rod Carew
        addrs.add(createAddress(agents.get(6), "1212 Apple Street", null, "Chicago", "IL", "USA", "01010"));
        // KU
        addrs.add(createAddress(ku, null, null, "Lawrence", "KS", "USA", "66045"));
        
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
        ce1.setMethod(collMethods[1]);
        
        AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", collectionObjDef, null);//meg added cod
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
        ce2.setMethod(collMethods[2]);

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
        permit.setIssuedTo(ku);
        permit.setIssuedBy(agents.get(4));
        dataObjects.add(permit);
        
        ////////////////////////////////
        // catalog series
        ////////////////////////////////
        log.info("Creating a catalog series");
        CatalogSeries catalogSeries = createCatalogSeries("KUFSH", "Fish", collectionObjDef);
        dataObjects.add(catalogSeries);
        
        ////////////////////////////////
        // Determination Status (Must be done here)
        ////////////////////////////////
        log.info("Creating determinations status");
        DeterminationStatus current    = createDeterminationStatus("Current","Test Status");
        DeterminationStatus notCurrent = createDeterminationStatus("Not current","Test Status");
        DeterminationStatus incorrect  = createDeterminationStatus("Incorrect","Test Status");
        
        dataObjects.add(current);
        dataObjects.add(notCurrent);
        dataObjects.add(incorrect);

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
        
        AttributeDef colObjAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "MoonPhase", cod, null);//meg added cod
        CollectionObjectAttr colObjAttr = createCollectionObjectAttr(collObjs.get(0), colObjAttrDef, "Full", null);
        dataObjects.addAll(collObjs);
        dataObjects.add(colObjAttrDef);
        dataObjects.add(colObjAttr);
        
        ////////////////////////////////
        // determinations (determination status)
        ////////////////////////////////
        log.info("Creating determinations");

        List<Determination> determs = new Vector<Determination>();
        Calendar recent = Calendar.getInstance();
        recent.set(2006, 10, 27, 13, 44, 00);
        Calendar longAgo = Calendar.getInstance();
        longAgo.set(1976, 01, 29, 8, 12, 00);
        Calendar whileBack = Calendar.getInstance(); 
        whileBack.set(2002, 7, 4, 9, 33, 12);
        determs.add(createDetermination(collObjs.get(0), agents.get(0), (Taxon)taxa.get(10), current, recent));
        determs.add(createDetermination(collObjs.get(1), agents.get(0), (Taxon)taxa.get(11), current, recent));
        determs.add(createDetermination(collObjs.get(2), agents.get(0), (Taxon)taxa.get(12), current, recent));
        determs.add(createDetermination(collObjs.get(3), agents.get(0), (Taxon)taxa.get(13), current, recent));
        determs.add(createDetermination(collObjs.get(4), agents.get(0), (Taxon)taxa.get(14), current, recent));
        determs.add(createDetermination(collObjs.get(5), agents.get(0), (Taxon)taxa.get(15), current, recent));
        determs.add(createDetermination(collObjs.get(6), agents.get(3), (Taxon)taxa.get(16), current, recent));
        determs.add(createDetermination(collObjs.get(7), agents.get(4), (Taxon)taxa.get(17), current, recent));
        
        determs.add(createDetermination(collObjs.get(0), agents.get(0), (Taxon)taxa.get(10), notCurrent, longAgo));
        determs.add(createDetermination(collObjs.get(1), agents.get(1), (Taxon)taxa.get(17), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(2), agents.get(1), (Taxon)taxa.get(18), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(3), agents.get(2), (Taxon)taxa.get(19), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(4), agents.get(2), (Taxon)taxa.get(19), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(4), agents.get(3), (Taxon)taxa.get(22), incorrect, longAgo));
        determs.add(createDetermination(collObjs.get(4), agents.get(4), (Taxon)taxa.get(21), incorrect, longAgo));
        determs.get(13).setRemarks("This determination is totally wrong.  What a foolish determination.");
        
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
        Calendar prepDate = Calendar.getInstance();
        preps.add(createPreparation(etoh, agents.get(0), collObjs.get(0), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(0), collObjs.get(1), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(1), collObjs.get(2), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(1), collObjs.get(3), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(2), collObjs.get(4), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(2), collObjs.get(5), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(3), collObjs.get(6), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(3), collObjs.get(7), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(0), (Location)locs.get(12), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(1), (Location)locs.get(12), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(2), (Location)locs.get(11), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(2), collObjs.get(3), (Location)locs.get(10), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(3), collObjs.get(4), (Location)locs.get(10), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(0), collObjs.get(5), (Location)locs.get(10), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(6), (Location)locs.get(10), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(7), (Location)locs.get(10), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(2), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));

        preps.add(createPreparation(xray, agents.get(1), collObjs.get(0), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(1), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(2), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(3), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(4), (Location)locs.get(10), rand.nextInt(20)+1, prepDate));

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
        Accession acc1 = createAccession("Gift", "Complete", "2006-IC-001", DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        acc1.setText1("Ichthyology");
        
        Agent donor =    agents.get(4);
        Agent receiver = agents.get(1);
        Agent reviewer = agents.get(2);
        
        List<AccessionAgent> accAgents = new Vector<AccessionAgent>();
        
        accAgents.add(createAccessionAgent("Donor", donor, acc1, null));
        accAgents.add(createAccessionAgent("Receiver", receiver, acc1, null));
        accAgents.add(createAccessionAgent("Reviewer", reviewer, acc1, null));

        Accession acc2 = createAccession("Field Work", "In Process", "2006-IC-002", DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        
        Agent donor2 =    agents.get(5);
        Agent receiver2 = agents.get(3);
        Agent reviewer2 = agents.get(1);
        
        accAgents.add(createAccessionAgent("Donor", donor2, acc2, null));
        accAgents.add(createAccessionAgent("Receiver", receiver2, acc2, null));
        accAgents.add(createAccessionAgent("Reviewer", reviewer2, acc2, null));

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
      
        List<LoanPhysicalObject>         loanPhysObjs = new Vector<LoanPhysicalObject>();
        Vector<LoanReturnPhysicalObject> returns      = new Vector<LoanReturnPhysicalObject>();
        
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
            LoanPhysicalObject lpo = DataBuilder.createLoanPhysicalObject(quantity, null, null, null, 0, 0, p, closedLoan);
            
            lpo.setIsResolved(true);
            loanPhysObjs.add(lpo);
            
            Calendar returnedDate     = Calendar.getInstance();       
            returnedDate.setTime(lpo.getLoan().getLoanDate().getTime());
            returnedDate.add(Calendar.DAY_OF_YEAR, 72); // make the returned date be a little while after the original loan
            
            LoanReturnPhysicalObject lrpo = createLoanReturnPhysicalObject(returnedDate, quantity, lpo, null, agents.get(0));
            lpo.addLoanReturnPhysicalObjects(lrpo);
            returns.add(lrpo);

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
            if (available < 1 )
            {
                // retry
                i--;
                continue;
            }
            int quantity = Math.max(1, rand.nextInt(available));
            LoanPhysicalObject lpo = createLoanPhysicalObject(quantity, null, null, null, 0, 0, p, overdueLoan);
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
                int quantity = Math.max(1, rand.nextInt(available));
                LoanPhysicalObject newLPO = createLoanPhysicalObject(quantity, null, null, null, 0, 0, lpo.getPreparation(), loan3);
                newLoanLPOs.add(newLPO);
                lpo.getPreparation().getLoanPhysicalObjects().add(newLPO);
                
                // stop after we put 6 LPOs in the new loan
                lpoCountInNewLoan++;
                if (lpoCountInNewLoan == 6)
                {
                    break;
                }
            }
        }
        
        // create some LoanReturnPhysicalObjects
        int startIndex = returns.size();
        for (int i=startIndex;i<loanPhysObjs.size();i++)
        {
            LoanPhysicalObject lpo = loanPhysObjs.get(i);
        
            int    quantityLoaned   = lpo.getQuantity();
            int    quantityReturned = (i == (loanPhysObjs.size() - 1)) ? quantityLoaned : (short)rand.nextInt(quantityLoaned);
            Calendar returnedDate     = Calendar.getInstance();
            
            returnedDate.setTime(lpo.getLoan().getLoanDate().getTime());
            // make the returned date be a little while after the original loan
            returnedDate.add(Calendar.DAY_OF_YEAR, 72);
            LoanReturnPhysicalObject lrpo = createLoanReturnPhysicalObject(returnedDate, quantityReturned, lpo, null, agents.get(0));
            lpo.addLoanReturnPhysicalObjects(lrpo);
            
            lpo.setQuantityReturned(quantityReturned);
            lpo.setQuantityResolved((quantityLoaned - quantityReturned));
            lpo.setIsResolved(quantityLoaned == quantityReturned);
            returns.add(lrpo);
            i++;
        }

        dataObjects.add(closedLoan);
        dataObjects.add(overdueLoan);
        dataObjects.add(loan3);
        dataObjects.addAll(loanPhysObjs);
        dataObjects.addAll(newLoanLPOs);
        dataObjects.addAll(returns);
        
        LoanAgents loanAgent1 = createLoanAgent("loaner", closedLoan, agents.get(1));
        LoanAgents loanAgent2 = createLoanAgent("loaner", overdueLoan, agents.get(3));
        LoanAgents loanAgent3 = createLoanAgent("Borrower", closedLoan, agents.get(4));
        LoanAgents loanAgent4 = createLoanAgent("Borrower", overdueLoan, agents.get(4));
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
        
        //closedLoan.setShipment(loan1Ship);
        //overdueLoan.setShipment(loan2Ship);
        closedLoan.getShipments().add(loan1Ship);
        overdueLoan.getShipments().add(loan2Ship);
        dataObjects.add(loan1Ship);
        dataObjects.add(loan2Ship);   

        if (true)
        {
            TaxonCitation taxonCitation = new TaxonCitation();
            taxonCitation.initialize();
            Taxon taxon10 = (Taxon)taxa.get(10);
            taxonCitation.setTaxon(taxon10);
            taxonCitation.setReferenceWork(rwList.get(0));
            rwList.get(0).addTaxonCitations(taxonCitation);
            taxon10.getTaxonCitations().add(taxonCitation);
            dataObjects.add(taxonCitation);
            
            
            LocalityCitation localityCitation = new LocalityCitation();
            localityCitation.initialize();
            localityCitation.setLocality(ce1.getLocality());
            ce1.getLocality().getLocalityCitations().add(localityCitation);
            localityCitation.setReferenceWork(rwList.get(1));
            rwList.get(1).addLocalityCitations(localityCitation);
            dataObjects.add(localityCitation);
        }
        
        ////////////////////////////////
        // attachments (attachment metadata)
        ////////////////////////////////
        if (true)
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
    
                Attachment sharkVideo2 = createAttachment(sharkVideoPath, "video/mpeg4", 0);
                sharkVideo2.setCollectingEvent(ce1);
    
//                String beakerPath = attachmentFilesLoc + "beaker.jpg";
//                Attachment beakerAsBeach = createAttachment(beakerPath, "image/jpg", 1);
//                beakerAsBeach.setAgent(agents.get(1));
                
                dataObjects.add(bigEye);
                dataObjects.add(joshPhoto);
                dataObjects.add(beachPhoto);
                dataObjects.add(megPhoto);
                dataObjects.add(rodPhoto);
                dataObjects.add(giftPDF);
                dataObjects.add(accPDF);
                dataObjects.add(sharkVideo);
                dataObjects.add(sharkVideo2);
                //dataObjects.add(beakerAsBeach);
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
        state.setTextAfter(" ");
        GeographyTreeDefItem county = createGeographyTreeDefItem(state, geoTreeDef, "County", 400);
        county.setIsInFullName(true);
        county.setTextAfter(" Co.");

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
        List<Geography> states = createGeographyChildren(geoTreeDef, us,
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
                new String[] { "Blackhawk", "Fayette", "Polk", "Woodbury", "Johnson" }, county.getRankId());
        // 16, 17, 18, 19, 20
        newObjs.addAll(counties);
        counties = createGeographyChildren(geoTreeDef, states.get(2),
                new String[] { "Dakota", "Logan", "Valley", "Wheeler", "Johnson" }, county.getRankId());
        // 21, 22, 23, 24, 25
        newObjs.addAll(counties);
        
        earth.fixFullNameForAllDescendants();
        earth.setNodeNumber(1);
        fixNodeNumbersFromRoot(earth);

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
                "Some Really Big Time Period", 5.0f, 0.0f, defItemLevel1.getRankId());
        GeologicTimePeriod level2 = createGeologicTimePeriod(treeDef, level1,
                "A Slightly Smaller Time Period", 1.74f, 0.0f, defItemLevel2.getRankId());
        GeologicTimePeriod level3_1 = createGeologicTimePeriod(treeDef, level2,
                "Yesterday", 0.1f, 0.0f, defItemLevel3.getRankId());
        GeologicTimePeriod level3_2 = createGeologicTimePeriod(treeDef, level2,
                "A couple of days ago", 0.2f, 0.1f, defItemLevel3.getRankId());
        GeologicTimePeriod level3_3 = createGeologicTimePeriod(treeDef, level2,
                "Last week", 0.7f, 1.4f, defItemLevel3.getRankId());
        newObjs.add(level0);
        newObjs.add(level1);
        newObjs.add(level2);
        newObjs.add(level3_1);
        newObjs.add(level3_2);
        newObjs.add(level3_3);

        level0.fixFullNameForAllDescendants();
        level0.setNodeNumber(1);
        fixNodeNumbersFromRoot(level0);
        
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
        room.setTextAfter(" ");
        LocationTreeDefItem freezer = createLocationTreeDefItem(room, locTreeDef, "freezer", 200);
        freezer.setIsInFullName(true);
        freezer.setTextAfter(" ");
        LocationTreeDefItem shelf = createLocationTreeDefItem(freezer, locTreeDef, "shelf", 300);
        shelf.setIsInFullName(true);
        shelf.setTextAfter(" ");

        // Create the building
        Location dyche = createLocation(locTreeDef, null, "Dyche Hall", building.getRankId());
        Location rm606 = createLocation(locTreeDef, dyche, "Room 606", room.getRankId());
        Location freezerA = createLocation(locTreeDef, rm606, "Freezer A", freezer.getRankId());
        Location shelf5 = createLocation(locTreeDef, freezerA, "Shelf 5", shelf.getRankId());
        Location shelf4 = createLocation(locTreeDef, freezerA, "Shelf 4", shelf.getRankId());
        Location shelf3 = createLocation(locTreeDef, freezerA, "Shelf 3", shelf.getRankId());
        Location shelf2 = createLocation(locTreeDef, freezerA, "Shelf 2", shelf.getRankId());
        Location shelf1 = createLocation(locTreeDef, freezerA, "Shelf 1", shelf.getRankId());

        Location rm701 = createLocation(locTreeDef, dyche, "Room 701", room.getRankId());
        Location freezerA_701 = createLocation(locTreeDef, rm701, "Freezer A", freezer.getRankId());
        Location shelf1_701 = createLocation(locTreeDef, freezerA_701, "Shelf 1", shelf.getRankId());
        
        Location rm703 = createLocation(locTreeDef, dyche, "Room 703", room.getRankId());
        Location freezerA_703 = createLocation(locTreeDef, rm703, "Freezer A", freezer.getRankId());
        Location shelf1_703 = createLocation(locTreeDef, freezerA_703, "Shelf 1", shelf.getRankId());
        Location shelf2_703 = createLocation(locTreeDef, freezerA_703, "Shelf 2", shelf.getRankId());
        Location shelf3_703 = createLocation(locTreeDef, freezerA_703, "Shelf 3", shelf.getRankId());
        
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
        // 13
        newObjs.add(rm701);
        // 14
        newObjs.add(freezerA_701);
        // 15
        newObjs.add(shelf1_701);
        // 16
        newObjs.add(rm703);
        // 17
        newObjs.add(freezerA_703);
        // 18
        newObjs.add(shelf1_703);
        // 19
        newObjs.add(shelf2_703);
        // 20
        newObjs.add(shelf3_703);
        
        dyche.fixFullNameForAllDescendants();
        dyche.setNodeNumber(1);
        fixNodeNumbersFromRoot(dyche);
        
        return newObjs;
    }


    public static List<Object> createSimpleTaxon(final TaxonTreeDef taxonTreeDef)
    {
        log.info("createSimpleTaxon " + taxonTreeDef.getName());

        Vector<Object> newObjs = new Vector<Object>();
        // Create a Taxon tree definition
        TaxonTreeDefItem taxonRoot = createTaxonTreeDefItem(null, taxonTreeDef, "life", 0);
        taxonRoot.setIsEnforced(true);
        TaxonTreeDefItem defItemLevel0 = createTaxonTreeDefItem(taxonRoot, taxonTreeDef, "order", 100);
        defItemLevel0.setIsEnforced(true);
        TaxonTreeDefItem defItemLevel1 = createTaxonTreeDefItem(defItemLevel0, taxonTreeDef, "family", 140);
        TaxonTreeDefItem defItemLevel2 = createTaxonTreeDefItem(defItemLevel1, taxonTreeDef, "genus", 180);
        defItemLevel2.setIsEnforced(true);
        defItemLevel2.setIsInFullName(true);
        defItemLevel2.setTextAfter(" ");
        TaxonTreeDefItem defItemLevel3 = createTaxonTreeDefItem(defItemLevel2, taxonTreeDef, "species", 220);
        defItemLevel3.setIsEnforced(true);
        defItemLevel3.setIsInFullName(true);
        defItemLevel3.setTextAfter(" ");

        // 0
        newObjs.add(taxonRoot);
        // 1
        newObjs.add(defItemLevel0);
        // 2
        newObjs.add(defItemLevel1);
        // 3
        newObjs.add(defItemLevel2);
        // 4
        newObjs.add(defItemLevel3);

        Taxon life = createTaxon(taxonTreeDef, null, "Life", taxonRoot.getRankId());
        Taxon order = createTaxon(taxonTreeDef, life, "Percidae", defItemLevel0.getRankId());
        Taxon family = createTaxon(taxonTreeDef, order, "Perciformes", defItemLevel1.getRankId());
        Taxon genus1 = createTaxon(taxonTreeDef, family, "Ammocrypta", defItemLevel2.getRankId());
        // 5
        newObjs.add(life);
        // 6
        newObjs.add(order);
        // 7
        newObjs.add(family);
        // 8
        newObjs.add(genus1);

        String[] speciesNames = { "asprella", "beanii", "bifascia", "clara", "meridiana", "pellucida", "vivax" };
        String[] commonNames  = {"Crystal darter", "Naked sand darter", "Florida sand darter", "Western sand darter", "Southern sand darter", "Eastern sand darter", "Scaly sand darter"};
        List<Object> kids = createTaxonChildren(taxonTreeDef, genus1, speciesNames, commonNames, defItemLevel3.getRankId());
        // 9, 10, 11, 12, 13, 14, 15
        newObjs.addAll(kids);

        Taxon genus2 = createTaxon(taxonTreeDef, order, "Caranx", defItemLevel2.getRankId());
        // 16
        newObjs.add(genus2);
        String[] speciesNames2 = { "bartholomaei", "caballus", "caninus", "crysos", "dentex", "hippos", "latus" };
        String[] commonNames2  = {"Yellow jack", "Green jack", "Pacific crevalle jack", "Blue runner", "White trevally", "Crevalle jack", "Horse-eye jack"};
        kids = createTaxonChildren(taxonTreeDef, genus2, speciesNames2, commonNames2, defItemLevel3.getRankId());
        // 17, 18, 19, 20, 21, 22, 23
        newObjs.addAll(kids);

        life.fixFullNameForAllDescendants();
        life.setNodeNumber(1);
        fixNodeNumbersFromRoot(life);
        
        for (Object o: newObjs)
        {
            if (o instanceof Taxon)
            {
                Taxon t = (Taxon)o;
                t.setIsAccepted(true);
                //t.setAccepted((short)1);
            }
        }
        
        return newObjs;
    }
    
    public static Journal createJournalsAndReferenceWork()
    {
        Journal journal = createJournal("Fish times", "FT");
        
        @SuppressWarnings("unused")
        ReferenceWork rw = createReferenceWork((byte)1, "Why Do Fish Have Scales?", "Fish Publishing", "NYC", "12/12/1900", "Vol 1.", "Pages 234-236", null, "112974-4532", true, journal);
        rw = createReferenceWork((byte)1, "Can Fish think?", "Fish Publishing", "Chicago", "12/12/1901", "Vol 2", "Pages 1-10", null, "64543-4532", true, journal);
        rw = createReferenceWork((byte)1, "The Taxon Def of Blubber Fish?", "Icthy Publishing", "SFO", "12/12/1960", "Vol 200", "Pages 10-100", null, "856433-4532", false, journal);
        
        return journal;
    }

    @SuppressWarnings("unchecked")
    protected static int fixNodeNumbersFromRoot( Treeable root )
    {
        int nextNodeNumber = root.getNodeNumber();
        for( Treeable child: (Set<Treeable>)root.getChildren() )
        {
            child.setNodeNumber(++nextNodeNumber);
            nextNodeNumber = fixNodeNumbersFromRoot(child);
        }
        root.setHighestChildNodeNumber(nextNodeNumber);
        return nextNodeNumber;
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
                    
                    File origDir = new File("demo_files/AttachmentStorage/originals");
                    File thumbDir = new File("demo_files/AttachmentStorage/thumbnails");
                    
                    // clean out the originals and thumbnails directories without deleting the .svn subdir
                    Collection<?> files = FileUtils.listFiles(origDir, null, false);
                    for (Object o: files)
                    {
                        File f = (File)o;
                        FileUtils.forceDelete(f);
                    }
                    
                    files = FileUtils.listFiles(thumbDir, null, false);
                    for (Object o: files)
                    {
                        File f = (File)o;
                        FileUtils.forceDelete(f);
                    }
                    
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
                    
                    if (true)
                    {
                        startTx();
                        List<?> journal    = HibernateUtil.getCurrentSession().createCriteria(Journal.class).list();
                        List<?> taxa       = HibernateUtil.getCurrentSession().createCriteria(Taxon.class).list();
                        List<?> localities = HibernateUtil.getCurrentSession().createCriteria(Locality.class).list();
                        List<ReferenceWork> rwList = new Vector<ReferenceWork>();
                        rwList.addAll(((Journal)journal.get(0)).getReferenceWorks());
                        
                        TaxonCitation taxonCitation = new TaxonCitation();
                        taxonCitation.initialize();
                        Taxon taxon10 = (Taxon)taxa.get(10);
                        taxonCitation.setTaxon(taxon10);
                        taxonCitation.setReferenceWork(rwList.get(0));
                        rwList.get(0).addTaxonCitations(taxonCitation);
                        taxon10.getTaxonCitations().add(taxonCitation);
                        dataObjects.add(taxonCitation);
                        persist(taxonCitation);
                        
                        Locality locality = (Locality)localities.get(0);
                        LocalityCitation localityCitation = new LocalityCitation();
                        localityCitation.initialize();
                        localityCitation.setLocality(locality);
                        locality.getLocalityCitations().add(localityCitation);
                        localityCitation.setReferenceWork(rwList.get(1));
                        rwList.get(1).addLocalityCitations(localityCitation);
                        dataObjects.add(localityCitation);
                        persist(localityCitation);
                        commitTx();
                    }
                    
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
        else
        {
            log.error("Login failed");
        }
    }
}

