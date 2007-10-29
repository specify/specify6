
/**
 * Copyright (C) 2006 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.utilapps;

import static edu.ku.brc.specify.utilapps.DataBuilder.createAccession;
import static edu.ku.brc.specify.utilapps.DataBuilder.createAccessionAgent;
import static edu.ku.brc.specify.utilapps.DataBuilder.createAddress;
import static edu.ku.brc.specify.utilapps.DataBuilder.createAgent;
import static edu.ku.brc.specify.utilapps.DataBuilder.createAttachment;
import static edu.ku.brc.specify.utilapps.DataBuilder.createAttributeDef;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCatalogNumberingScheme;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectingEvent;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectingEventAttr;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectingTrip;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollection;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectionObject;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectionObjectAttr;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectionType;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollector;
import static edu.ku.brc.specify.utilapps.DataBuilder.createDataType;
import static edu.ku.brc.specify.utilapps.DataBuilder.createDetermination;
import static edu.ku.brc.specify.utilapps.DataBuilder.createDeterminationStatus;
import static edu.ku.brc.specify.utilapps.DataBuilder.createDivision;
import static edu.ku.brc.specify.utilapps.DataBuilder.createGeography;
import static edu.ku.brc.specify.utilapps.DataBuilder.createGeographyChildren;
import static edu.ku.brc.specify.utilapps.DataBuilder.createGeographyTreeDef;
import static edu.ku.brc.specify.utilapps.DataBuilder.createGeographyTreeDefItem;
import static edu.ku.brc.specify.utilapps.DataBuilder.createGeologicTimePeriod;
import static edu.ku.brc.specify.utilapps.DataBuilder.createGeologicTimePeriodTreeDef;
import static edu.ku.brc.specify.utilapps.DataBuilder.createGeologicTimePeriodTreeDefItem;
import static edu.ku.brc.specify.utilapps.DataBuilder.createInstitution;
import static edu.ku.brc.specify.utilapps.DataBuilder.createJournal;
import static edu.ku.brc.specify.utilapps.DataBuilder.createLithoStratTreeDef;
import static edu.ku.brc.specify.utilapps.DataBuilder.createLoan;
import static edu.ku.brc.specify.utilapps.DataBuilder.createLoanAgent;
import static edu.ku.brc.specify.utilapps.DataBuilder.createLoanPreparation;
import static edu.ku.brc.specify.utilapps.DataBuilder.createLoanReturnPreparation;
import static edu.ku.brc.specify.utilapps.DataBuilder.createLocality;
import static edu.ku.brc.specify.utilapps.DataBuilder.createLocation;
import static edu.ku.brc.specify.utilapps.DataBuilder.createLocationTreeDef;
import static edu.ku.brc.specify.utilapps.DataBuilder.createLocationTreeDefItem;
import static edu.ku.brc.specify.utilapps.DataBuilder.createPermit;
import static edu.ku.brc.specify.utilapps.DataBuilder.createPickList;
import static edu.ku.brc.specify.utilapps.DataBuilder.createPrepType;
import static edu.ku.brc.specify.utilapps.DataBuilder.createPreparation;
import static edu.ku.brc.specify.utilapps.DataBuilder.createQuery;
import static edu.ku.brc.specify.utilapps.DataBuilder.createQueryField;
import static edu.ku.brc.specify.utilapps.DataBuilder.createReferenceWork;
import static edu.ku.brc.specify.utilapps.DataBuilder.createShipment;
import static edu.ku.brc.specify.utilapps.DataBuilder.createSpecifyUser;
import static edu.ku.brc.specify.utilapps.DataBuilder.createTaxon;
import static edu.ku.brc.specify.utilapps.DataBuilder.createTaxonChildren;
import static edu.ku.brc.specify.utilapps.DataBuilder.createTaxonTreeDef;
import static edu.ku.brc.specify.utilapps.DataBuilder.createUserGroup;
import static edu.ku.brc.specify.utilapps.DataBuilder.createWorkbench;
import static edu.ku.brc.specify.utilapps.DataBuilder.createWorkbenchDataItem;
import static edu.ku.brc.specify.utilapps.DataBuilder.createWorkbenchMappingItem;
import static edu.ku.brc.specify.utilapps.DataBuilder.createWorkbenchTemplate;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.Discipline;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentAttachment;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentMetadata;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.CatalogNumberingScheme;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttr;
import edu.ku.brc.specify.datamodel.CollectingTrip;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.CollectionType;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.ConservDescription;
import edu.ku.brc.specify.datamodel.ConservEvent;
import edu.ku.brc.specify.datamodel.ConservRecmdType;
import edu.ku.brc.specify.datamodel.ConservRecommendation;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.Journal;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanAgent;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityCitation;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonCitation;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchRowImage;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerXMLHelper;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.treeutils.TreeHelper;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileStoreAttachmentManager;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * 
 * @code_status Beta
 * @author jstewart
 */
public class BuildSampleDatabase
{
    private static final Logger  log      = Logger.getLogger(BuildSampleDatabase.class);
    protected Calendar           calendar = Calendar.getInstance();
    protected Session            session;
    protected Random             rand = new Random(12345678L);
    
    protected int                steps = 0;   
    protected ProgressFrame      frame;
    protected Properties         initPrefs     = null;
    protected Properties         backstopPrefs = null;
    
    protected SetupDialog        setupDlg  = null;
    protected boolean            hideFrame = false;
    
    protected boolean            copyToUserDir = true;
    
    /**
     * 
     */
    public BuildSampleDatabase()
    {
        frame = new ProgressFrame("Building Specify Workbench Database");
    }
    
    public Session getSession()
    {
        return session;
    }
    
    public void setSession(Session s)
    {
        session = s;
    }
    
    public ProgressFrame getFrame()
    {
        return frame;
    }
    
    protected void standardQueries(final Vector<Object> dataObjects)
    {
        //Byte greaterThan = SpQueryField.OperatorType.GREATERTHAN.getOrdinal();
        //Byte lessThan    = SpQueryField.OperatorType.LESSTHAN.getOrdinal();
        //Byte equals      = SpQueryField.OperatorType.EQUALS.getOrdinal();
        Byte greq        = SpQueryField.OperatorType.GREATERTHANEQUALS.getOrdinal();
        Byte lteq        = SpQueryField.OperatorType.LESSTHANEQUALS.getOrdinal();
        
        //Byte none        = SpQueryField.SortType.NONE.getOrdinal();
        Byte asc         = SpQueryField.SortType.ASC.getOrdinal();
        //Byte desc        = SpQueryField.SortType.DESC.getOrdinal();
        
        SpQuery query = createQuery("Collection Objects", "CollectionObject", 1, SpecifyUser.getCurrentUser());
        createQueryField(query, (short)0, "catalogNumber", false, greq, lteq, "102", "103", asc, true, "1");
        dataObjects.add(query);
    }

    /**
     * Creates a single discipline collection.
     * @param collTypeName the name of the Collection Type to use
     * @param disciplineName the discipline name
     * @return the entire list of DB object to be persisted
     */
    public List<Object> createEmptyDiscipline(final String collTypeName, 
                                              final String disciplineName,
                                              final String username, 
                                              final String userType, 
                                              final String firstName, 
                                              final String lastName, 
                                              final String email)
    {
        Vector<Object> dataObjects = new Vector<Object>();

        Agent             userAgent         = createAgent("", firstName, "", lastName, "", email);
        UserGroup         userGroup         = createUserGroup(disciplineName);
        SpecifyUser       user              = createSpecifyUser(username, email, (short) 0, userGroup, userType);
        DataType          dataType          = createDataType(disciplineName);

        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Sample Taxon Tree");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Sample Geography Tree");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Sample Geologic Time Period Tree");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("Sample LithoStrat Tree");
        LocationTreeDef           locTreeDef        = createLocationTreeDef("Sample Location Tree");

        Institution    institution    = createInstitution("Natural History Museum");
        Division       division       = createDivision(institution, "Icthyology");
        CollectionType collectionType = createCollectionType(division, collTypeName, disciplineName, dataType, user, taxonTreeDef, null, null, null, lithoStratTreeDef);

        startTx();
        persist(institution);
        persist(division);
        persist(collectionType);
        commitTx();
        
        loadSchemaLocalization(collectionType, SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());
        
        DBTableIdMgr schema = new DBTableIdMgr(false);
        schema.initialize(new File(XMLHelper.getConfigDirPath("specify_workbench_datamodel.xml")));
        loadSchemaLocalization(collectionType, SpLocaleContainer.WORKBENCH_SCHEMA, schema);
        
        SpecifyUser.setCurrentUser(user);
        user.setAgent(userAgent);

        dataObjects.add(collectionType);
        dataObjects.add(userGroup);
        dataObjects.add(user);
        dataObjects.add(dataType);
        dataObjects.add(taxonTreeDef);
        dataObjects.add(userAgent);
        
        ////////////////////////////////
        // build the tree def items and nodes
        ////////////////////////////////
        List<Object> taxa        = createSimpleTaxon(taxonTreeDef);
        List<Object> geos        = createSimpleGeography(geoTreeDef);
        List<Object> locs        = createSimpleLocation(locTreeDef);
        List<Object> gtps        = createSimpleGeologicTimePeriod(gtpTreeDef);
        List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef);
        
        //startTx();
        persist(taxa);
        persist(geos);
        persist(locs);
        persist(gtps);
        persist(lithoStrats);
         
        CatalogNumberingScheme cns = createCatalogNumberingScheme("CatalogNumber", "", true);
        dataObjects.add(cns);

        Collection collection = createCollection("Fish", "Fish", cns, collectionType);
        dataObjects.add(collection);

        return dataObjects;
    }
    
    /**
     * Returns a list of object of a specified class.
     * @param cls the clas 
     * @param dataObjects the list of possible object
     * @return the list
     */
    protected List<?> getObjectsOfClass(final Class<?> cls, final List<Object> dataObjects)
    {
        Vector<Object> list = new Vector<Object>();
        for (Object obj : dataObjects)
        {
            if (obj.getClass() == cls)
            {
                list.add(obj);
            }
        }
        return list;
    }
    
    /**
     * @param dataObjects
     */
    @SuppressWarnings("unchecked")
    protected void addConservatorData(final Agent userAgent, 
                                      final List<Agent> agents, 
                                      final List<CollectionObject> colObjs)
    {
        //startTx();
        
        ConservRecmdType type = new ConservRecmdType();
        type.initialize();
        type.setTitle("Avoid Light");
        persist(type);
        
        ConservDescription desc = new ConservDescription();
        desc.initialize();
        desc.setShortDesc("Short Description");
        //desc.addReference(divs.get(0), "division");
        
        desc.addReference(userAgent, "curator");
        desc.addReference(colObjs.get(0), "collectionObject");
        
        //desc.setCollectionObject(colObjs.get(0));
        //colObjs.get(0).getConservDescriptions().add(desc);
        
        ConservEvent ce = new ConservEvent();
        ce.initialize();
        ce.setExamDate(Calendar.getInstance());
        
        ce.addReference(agents.get(1), "examinedByAgent");
        ce.addReference(agents.get(2), "treatedByAgent");
        
        /*int inx = 5;
        ce.setExaminedByAgent(agents.get(inx));
        agents.get(inx).getExaminedByAgentConservEvents().add(ce);
        System.out.println(agents.get(inx).getLastName());
        
        //ce.setTreatedByAgent(agents.get(7));
        //agents.get(7).getTreatedByAgentConservEvents().add(ce);
         */
        
        //desc.getEvents().add(ce);
        //ce.setConservDescription(desc);
        desc.addReference(ce, "events");
        
        
        ConservRecommendation recommend = new ConservRecommendation();
        recommend.initialize();
        recommend.setCompletedDate(Calendar.getInstance());
        recommend.addReference(type, "conservRecmdType");
        ce.addReference(recommend, "lightRecommendations");
 
        persist(desc);
        
        //commitTx();
        
    }
    
    /**
     * Creates a single discipline collection.
     * @param collTypeName the name of the Collection Type to use
     * @param disciplineName the discipline name
     * @return the entire list of DB object to be persisted
     */
    public List<Object> createSingleDiscipline(final Discipline discipline)
    {
        System.out.println("Creating single discipline database: " + discipline.getTitle());
        
        int createStep = 0;
        
        frame.setProcess(0, 15);
        
        frame.setProcess(++createStep);

        List<Agent>    agents      = new Vector<Agent>();
        
        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           username         = initPrefs.getProperty("initializer.username", "rods");
        String           title            = initPrefs.getProperty("useragent.title",    "Mr.");
        String           firstName        = initPrefs.getProperty("useragent.firstname", "Rod");
        String           lastName         = initPrefs.getProperty("useragent.lastname", "Spears");
        String           midInit          = initPrefs.getProperty("useragent.midinit", "C");
        String           abbrev           = initPrefs.getProperty("useragent.abbrev", "rs");
        String           email            = initPrefs.getProperty("useragent.email", "rods@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", "CollectionManager");
        
        System.out.println("----- User Agent -----");
        System.out.println("Userame:   "+username);
        System.out.println("Title:     "+title);
        System.out.println("FirstName: "+firstName);
        System.out.println("LastName:  "+lastName);
        System.out.println("MidInit:   "+midInit);
        System.out.println("Abbrev:    "+abbrev);
        System.out.println("Email:     "+email);
        System.out.println("UserType:  "+userType);
        
        
        Institution      institution      = createInstitution("Natural History Museum");
        Division         division         = createDivision(institution, "Icthyology");
        
        Agent            userAgent        = createAgent(title, firstName, midInit, lastName, abbrev, email);
        userAgent.setCollectionMemberId(0);
        UserGroup        userGroup        = createUserGroup(discipline.getTitle());
        
        
        startTx();
        persist(userGroup);
        persist(institution);
        persist(division);
        
        SpecifyUser      user             = createSpecifyUser(username, email, (short) 0, userGroup, userType);
        persist(user);
        
        DataType         dataType         = createDataType(discipline.getTitle());
        persist(dataType);
        
        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Sample Taxon Tree");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Sample Geography Tree");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Sample Geologic Time Period Tree");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("Sample LithoStrat Tree");
        LocationTreeDef           locTreeDef        = createLocationTreeDef("Sample Location Tree");
        
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        
        CollectionType collectionType = createCollectionType(division, discipline.getName(), discipline.getTitle(), dataType, user, taxonTreeDef, geoTreeDef, gtpTreeDef, locTreeDef, lithoStratTreeDef);
        
        persist(collectionType);
        persist(userAgent);
        
        loadSchemaLocalization(collectionType, SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());
        
        CatalogNumberingScheme cns = createCatalogNumberingScheme("CatalogNumber", "", true);
        
        persist(cns);
        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a catalog series");
        Collection collection = createCollection("KUFSH", "Fish", cns, collectionType);
        persist(collection);
        
        Collection.setCurrentCollection(collection);

        commitTx();
        
        startTx();
        
        //DBTableIdMgr schema = new DBTableIdMgr(false);
        //schema.initialize(new File(XMLHelper.getConfigDirPath("specify_datamodel.xml")));
        //loadSchemaLocalization(collectionType, SpLocaleContainer, schema);
        
        SpecifyUser.setCurrentUser(user);
        user.setAgent(userAgent);
        
        persist(user);

        Journal journal = createJournalsAndReferenceWork();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // build the tree def items and nodes
        ////////////////////////////////
        List<Object> taxa        = createSimpleTaxon(taxonTreeDef);
        List<Object> geos        = createSimpleGeography(geoTreeDef);
        List<Object> locs        = createSimpleLocation(locTreeDef);
        List<Object> gtps        = createSimpleGeologicTimePeriod(gtpTreeDef);
        List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef);
        
        //startTx();
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(locs);
        persist(gtps);
        persist(lithoStrats);
        //commitTx();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////
        log.info("Creating picklists");

        int tableType      = PickListDBAdapterIFace.Type.Table.ordinal();
        int tableFieldType = PickListDBAdapterIFace.Type.TableField.ordinal();
        
        Vector<Object> dataObjects = new Vector<Object>();
        
        
        standardQueries(dataObjects);
        persist(dataObjects);
        dataObjects.clear();

        //                                 Name                Type           Table Name           Field       Formatter          R/O  Size
        dataObjects.add(createPickList("DeterminationStatus", tableType,      "determinationstatus", null, "DeterminationStatus", true, -1));
        dataObjects.add(createPickList("DataType",            tableType,      "datatype",            null, "DataType",            true, -1));
        dataObjects.add(createPickList("Department",          tableFieldType, "accession",         "text1" ,       null,          true, -1));
        dataObjects.add(createPickList("AgentTitle",          tableFieldType, "agent",             "title" ,       null,          true, -1));
        dataObjects.add(createPickList("PrepType",            tableType,      "preptype",           null, "PrepType",             true, -1));
        dataObjects.add(createPickList("CollectionType",      tableType,      "collectiontype",     null, "CollectionType",     true, -1));
        dataObjects.add(createPickList("GeologicTimePeriodTreeDef", tableType, "geologictimeperiodtreedef", null, "GeologicTimePeriodTreeDef", true, -1));
        dataObjects.add(createPickList("CatalogNumberingScheme", tableType, "catalognumberingscheme", null, "CatalogNumberingScheme", true, -1));
        
        String[] types = {"State", "Federal", "International", "US Dept Fish and Wildlife", "<no data>"};
        dataObjects.add(createPickList("PermitType", true, types));

        //String[] titles = {"Dr.", "Mr.", "Ms.", "Mrs.", "Sir"};
        //dataObjects.add(createPickList("AgentTitle", true, titles));
        
        String[] roles = {"Borrower", "Receiver"};
        dataObjects.add(createPickList("LoanAgentRole", true, roles));
        
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
        
        String[] taxonLabelFormatter = {"%G %S", "%G %S (%A1 ex. %A2)"};
        dataObjects.add(createPickList("TaxonLabelFormatter", false, taxonLabelFormatter, 500));
        
        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // localities
        ////////////////////////////////
        String POINT = "Point";
        String LINE  = "Line";
        String RECT  = "Rectangle";
        
        log.info("Creating localities");
        Locality forestStream = createLocality("Unnamed forest stream pond", (Geography)geos.get(12));
        forestStream.setLatLongType(POINT);
        forestStream.setOriginalLatLongUnit(0);
        forestStream.setLat1text("38.925467 deg N");
        forestStream.setLatitude1(new BigDecimal(38.925467));
        forestStream.setLong1text("94.984867 deg W");
        forestStream.setLongitude1(new BigDecimal(-94.984867));

        Locality lake   = createLocality("Deep, dark lake pond", (Geography)geos.get(17));
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
        
        Locality farmpond = createLocality("Farm pond", (Geography)geos.get(21));
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

        //startTx();
        persist(forestStream);
        persist(lake);
        persist(farmpond);
        //commitTx();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // agents and addresses
        ////////////////////////////////
        log.info("Creating agents and addresses");

        if (!lastName.equals("Smith")) agents.add(createAgent("Mr.", "David", "D", "Smith", "ds", "ds@whitehouse.gov"));
        if (!lastName.equals("Burk")) agents.add(createAgent("Mr.", "Robert", "H", "Burk", "rb", "beach@net.edu"));
        if (!lastName.equals("Johnson")) agents.add(createAgent("Mrs.", "Margaret", "H", "Johnson", "jm", "jm@net.edu"));
        if (!lastName.equals("Spencer")) agents.add(createAgent("Mr.", "Kip", "C", "Spencer", "kcs", "rods@ku.edu"));
        if (!lastName.equals("Byrn")) agents.add(createAgent("Mr.", "John", "D", "Byrn", "jb", "jb@net.edu"));
        if (!lastName.equals("Thompson")) agents.add(createAgent("Sir", "Dudley", "X", "Thompson", "dxt", ""));
        if (!lastName.equals("Campbell")) agents.add(createAgent("Mr.", "Joe", "A", "Campbell", "jb", ""));
        if (!lastName.equals("Tester")) agents.add(createAgent("Mr.", "Joe", "A", "Tester", "jb", ""));
        if (!lastName.equals("Stewart")) agents.add(createAgent("Mr.", "Joshua", "D", "Stewart", "jds", "jds@ku.edu"));
        agents.add(userAgent);
        
        Agent ku = new Agent();
        ku.initialize();
        ku.setAbbreviation("KU");
        ku.setAgentType(Agent.ORG);
        ku.setName("University of Kansas");
        ku.setEmail("webadmin@ku.edu");
        ku.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        agents.add(ku);
        agents.get(0).setOrganization(ku);
        agents.get(1).setOrganization(ku);
        agents.get(2).setOrganization(ku);
        agents.get(3).setOrganization(ku);
        agents.get(8).setOrganization(ku);
        
        List<Address> addrs = new Vector<Address>();
        // David Smith
        addrs.add(createAddress(agents.get(1), "1600 Pennsylvania Avenue NW", null, "Washington", "DC", "USA", "20500"));
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
        // no address for agent 7 (Joe Tester)
        // Josh
        addrs.add(createAddress(agents.get(8), "11911 S Redbud Ln", null, "Olathe", "KS", "USA", "66061"));
        // KU
        addrs.add(createAddress(ku, null, null, "Lawrence", "KS", "USA", "66045"));
        
        // User Agent Address
        addrs.add(createAddress(userAgent, "1214 East Street", null, "Grinnell", "IA", "USA", "56060"));
                
        //startTx();
        persist(agents);
        //commitTx();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // collecting events (collectors, collecting trip)
        ////////////////////////////////
        log.info("Creating collecting events, collectors and a collecting trip");
        Collector collectorJosh = createCollector(agents.get(8), 2);
        Collector collectorJim = createCollector(agents.get(1), 1);
        CollectingEvent ce1 = createCollectingEvent(forestStream, new Collector[]{collectorJosh,collectorJim});
        calendar.set(1993, 3, 19, 11, 56, 00);
        ce1.setStartDate(calendar);
        ce1.setStartDateVerbatim("19 Mar 1993, 11:56 AM");
        calendar.set(1993, 3, 19, 13, 03, 00);
        ce1.setEndDate(calendar);
        ce1.setEndDateVerbatim("19 Mar 1993, 1:03 PM");   
        ce1.setMethod(collMethods[1]);
        
        AttributeDef        cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", collectionType, null);//meg added cod
        
        //startTx();
        persist(cevAttrDef);
        //commitTx();
        
        CollectingEventAttr cevAttr    = createCollectingEventAttr(ce1, cevAttrDef, "Sleepy Hollow", null);

        Collector collectorMeg = createCollector(agents.get(2), 1);
        Collector collectorRod = createCollector(agents.get(3), 2);
        CollectingEvent ce2 = createCollectingEvent(farmpond, new Collector[]{collectorMeg,collectorRod});
        calendar.set(1993, 3, 20, 06, 12, 00);
        ce2.setStartDate(calendar);
        ce2.setStartDateVerbatim("20 Mar 1993, 6:12 AM");
        calendar.set(1993, 3, 20, 07, 31, 00);
        ce2.setEndDate(calendar);
        ce2.setEndDateVerbatim("20 Mar 1993, 7:31 AM");
        ce2.setMethod(collMethods[2]);

        CollectingTrip trip = createCollectingTrip("Sample collecting trip", new CollectingEvent[]{ce1,ce2});

        
        dataObjects.add(trip);
        dataObjects.add(ce1);
        dataObjects.add(cevAttr);
        dataObjects.add(ce2);
        dataObjects.add(collectorJosh);
        dataObjects.add(collectorJim);
        dataObjects.add(collectorMeg);
        dataObjects.add(collectorRod);
        
        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        
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
        
        log.info("Creating a repository agreement");
        RepositoryAgreement repoAg = new RepositoryAgreement();
        repoAg.initialize();
        repoAg.setNumber("KU-1992-01");
        repoAg.setOriginator(ku);
        Calendar received = Calendar.getInstance();
        received.set(1992, 2, 10);
        repoAg.setDateReceived(received);
        Calendar repoEndDate = Calendar.getInstance();
        received.set(2010, 2, 9);
        repoAg.setEndDate(repoEndDate);
        dataObjects.add(repoAg);
        
        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        
        frame.setProcess(++createStep);

        
        ////////////////////////////////
        // Determination Status (Must be done here)
        ////////////////////////////////
        log.info("Creating determinations status");
        DeterminationStatus current    = createDeterminationStatus("Current","Test Status", true);
        DeterminationStatus notCurrent = createDeterminationStatus("Not current","Test Status", false);
        DeterminationStatus incorrect  = createDeterminationStatus("Incorrect","Test Status", false);
        
        //startTx();
        persist(current);
        persist(notCurrent);
        persist(incorrect);
        //commitTx();
        
        ////////////////////////////////
        // collection objects
        ////////////////////////////////
        log.info("Creating collection objects");

        List<CollectionObject> collObjs = new Vector<CollectionObject>();
        Collection      col     = collection;
        
        Calendar[] catDates = new Calendar[8];
        for (int i=0;i<catDates.length;i++)
        {
            catDates[i] = Calendar.getInstance();
            catDates[i].set(catDates[i].get(Calendar.YEAR), 01, 12 + i);
        }
        
        String prefix = "000000";
        collObjs.add(createCollectionObject(prefix + "100", "RSC100", agents.get(0), col,  3, ce1, catDates[0], "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(prefix + "101", "RSC101", agents.get(0), col,  2, ce1, catDates[1], "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(prefix + "102", "RSC102", agents.get(1), col,  7, ce1, catDates[2], "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(prefix + "103", "RSC103", agents.get(1), col, 12, ce1, catDates[3], "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(prefix + "104", "RSC104", agents.get(2), col,  8, ce2, catDates[4], "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(prefix + "105", "RSC105", agents.get(2), col,  1, ce2, catDates[5], "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(prefix + "106", "RSC106", agents.get(2), col,  1, ce2, catDates[6], "BuildSampleDatabase"));
        collObjs.add(createCollectionObject(prefix + "107", "RSC107", agents.get(3), col,  1, ce2, catDates[7], "BuildSampleDatabase"));
        
        AttributeDef colObjAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "MoonPhase", collectionType, null);//meg added cod
        colObjAttrDef.setCollectionType(collectionType);
        collectionType.getAttributeDefs().add(colObjAttrDef);
        
        CollectionObjectAttr colObjAttr = createCollectionObjectAttr(collObjs.get(0), colObjAttrDef, "Full", null);
        dataObjects.add(colObjAttrDef);
        dataObjects.addAll(collObjs);
        dataObjects.add(colObjAttr);
        
        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();

        
        frame.setProcess(++createStep);
        
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
        determs.add(createDetermination(collObjs.get(0), agents.get(0), (Taxon)taxa.get(32), current, recent));
        determs.add(createDetermination(collObjs.get(1), agents.get(0), (Taxon)taxa.get(33), current, recent));
        determs.add(createDetermination(collObjs.get(2), agents.get(0), (Taxon)taxa.get(34), current, recent));
        determs.add(createDetermination(collObjs.get(3), agents.get(0), (Taxon)taxa.get(35), current, recent));
        determs.add(createDetermination(collObjs.get(4), agents.get(0), (Taxon)taxa.get(36), current, recent));
        determs.add(createDetermination(collObjs.get(5), agents.get(0), (Taxon)taxa.get(37), current, recent));
        determs.add(createDetermination(collObjs.get(6), agents.get(3), (Taxon)taxa.get(38), current, recent));
        determs.add(createDetermination(collObjs.get(7), agents.get(4), (Taxon)taxa.get(39), current, recent));
        
        determs.add(createDetermination(collObjs.get(0), agents.get(0), (Taxon)taxa.get(32), notCurrent, longAgo));
        determs.add(createDetermination(collObjs.get(1), agents.get(1), (Taxon)taxa.get(39), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(2), agents.get(1), (Taxon)taxa.get(41), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(3), agents.get(2), (Taxon)taxa.get(42), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(4), agents.get(2), (Taxon)taxa.get(42), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(4), agents.get(3), (Taxon)taxa.get(45), incorrect, longAgo));
        determs.add(createDetermination(collObjs.get(4), agents.get(4), (Taxon)taxa.get(44), incorrect, longAgo));
        determs.get(13).setRemarks("This determination is totally wrong.  What a foolish determination.");
        
        //startTx();
        persist(determs);
        //commitTx();
        dataObjects.clear();
        
        frame.setProcess(++createStep);
                
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
        preps.add(createPreparation(etoh, agents.get(0), collObjs.get(0), (Location)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(0), collObjs.get(1), (Location)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(1), collObjs.get(2), (Location)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(1), collObjs.get(3), (Location)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(2), collObjs.get(4), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(2), collObjs.get(5), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(3), collObjs.get(6), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(3), collObjs.get(7), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(0), (Location)locs.get(11), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(1), (Location)locs.get(11), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(2), (Location)locs.get(10), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(2), collObjs.get(3), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(3), collObjs.get(4), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(0), collObjs.get(5), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(6), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(7), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(2), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));

        preps.add(createPreparation(xray, agents.get(1), collObjs.get(0), (Location)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(1), (Location)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(2), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(3), (Location)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(4), (Location)locs.get(9), rand.nextInt(20)+1, prepDate));

        dataObjects.add(skel);
        dataObjects.add(cas);
        dataObjects.add(etoh);
        dataObjects.add(xray);
        dataObjects.addAll(preps);
        
        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // accessions (accession agents)
        ////////////////////////////////
        log.info("Creating accessions and accession agents");
        calendar.set(2006, 10, 27, 23, 59, 59);
        Accession acc1 = createAccession("Gift", "Complete", "2006-IC-001", DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        acc1.setText1("Ichthyology");
        acc1.setRepositoryAgreement(repoAg);
        
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
        
        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        
        frame.setProcess(++createStep);
        

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
      
        List<LoanPreparation>         loanPhysObjs = new Vector<LoanPreparation>();
        Vector<LoanReturnPreparation> returns      = new Vector<LoanReturnPreparation>();
        
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
            LoanPreparation lpo = DataBuilder.createLoanPreparation(quantity, null, null, null, 0, 0, p, closedLoan);
            
            lpo.setIsResolved(true);
            loanPhysObjs.add(lpo);
            
            Calendar returnedDate     = Calendar.getInstance();       
            returnedDate.setTime(lpo.getLoan().getLoanDate().getTime());
            returnedDate.add(Calendar.DAY_OF_YEAR, 72); // make the returned date be a little while after the original loan
            
            LoanReturnPreparation lrpo = createLoanReturnPreparation(returnedDate, quantity, lpo, null, agents.get(0));
            lpo.addReference(lrpo, "loanReturnPreparations");
            returns.add(lrpo);

            p.getLoanPreparations().add(lpo);
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
            LoanPreparation lpo = createLoanPreparation(quantity, null, null, null, 0, 0, p, overdueLoan);
            loanPhysObjs.add(lpo);
            p.getLoanPreparations().add(lpo);
        }

        Calendar loanDate3 = Calendar.getInstance();
        loanDate3.set(2006, 3, 21);
        
        Calendar currentDueDate3 = Calendar.getInstance();
        currentDueDate3.set(2007, 3, 21);
        
        Calendar originalDueDate3 = Calendar.getInstance();
        originalDueDate3.set(2006, 9, 21);
        
        Loan loan3 = createLoan("2006-003", loanDate3, currentDueDate3, originalDueDate3,  
                                      null, Loan.LOAN, Loan.OPEN, null);
        Vector<LoanPreparation> newLoanLPOs = new Vector<LoanPreparation>();
        int lpoCountInNewLoan = 0;
        // put some LPOs in this loan that are from CollObjs that have other preps loaned out already
        // this algorithm (because of the randomness) can result in this loan having 0 LPOs.
        for( LoanPreparation lpo: loanPhysObjs)
        {
            int available = lpo.getPreparation().getAvailable();
            if (available > 0)
            {
                int quantity = Math.max(1, rand.nextInt(available));
                LoanPreparation newLPO = createLoanPreparation(quantity, null, null, null, 0, 0, lpo.getPreparation(), loan3);
                newLoanLPOs.add(newLPO);
                lpo.getPreparation().getLoanPreparations().add(newLPO);
                
                // stop after we put 6 LPOs in the new loan
                lpoCountInNewLoan++;
                if (lpoCountInNewLoan == 6)
                {
                    break;
                }
            }
        }
        
        // create some LoanReturnPreparations
        int startIndex = returns.size();
        for (int i=startIndex;i<loanPhysObjs.size();i++)
        {
            LoanPreparation lpo = loanPhysObjs.get(i);
        
            int    quantityLoaned   = lpo.getQuantity();
            int    quantityReturned = (i == (loanPhysObjs.size() - 1)) ? quantityLoaned : (short)rand.nextInt(quantityLoaned);
            Calendar returnedDate     = Calendar.getInstance();
            
            returnedDate.setTime(lpo.getLoan().getLoanDate().getTime());
            // make the returned date be a little while after the original loan
            returnedDate.add(Calendar.DAY_OF_YEAR, 72);
            LoanReturnPreparation lrpo = createLoanReturnPreparation(returnedDate, quantityReturned, lpo, null, agents.get(0));
            lpo.addReference(lrpo, "loanReturnPreparations");
            
            lpo.setQuantityReturned(quantityReturned);
            lpo.setQuantityResolved((quantityLoaned - quantityReturned));
            lpo.setIsResolved(quantityLoaned == quantityReturned);
            returns.add(lrpo);
            i++;
        }
        
        LoanAgent loanAgent1 = createLoanAgent("loaner", closedLoan, agents.get(1));
        LoanAgent loanAgent2 = createLoanAgent("loaner", overdueLoan, agents.get(3));
        LoanAgent loanAgent3 = createLoanAgent("Borrower", closedLoan, agents.get(4));
        LoanAgent loanAgent4 = createLoanAgent("Borrower", overdueLoan, agents.get(4));
        
        dataObjects.add(closedLoan);
        dataObjects.add(overdueLoan);
        dataObjects.add(loan3);
        dataObjects.addAll(loanPhysObjs);
        dataObjects.addAll(newLoanLPOs);
        dataObjects.addAll(returns);
        
        dataObjects.add(loanAgent1);
        dataObjects.add(loanAgent2);
        dataObjects.add(loanAgent3);
        dataObjects.add(loanAgent4);
        
        frame.setProcess(++createStep);
        
        
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
        
        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        


        /*if (false)
        {
            TaxonCitation taxonCitation = new TaxonCitation();
            taxonCitation.initialize();
            Taxon taxon11 = (Taxon)taxa.get(11);
            taxonCitation.setTaxon(taxon11);
            taxonCitation.setReferenceWork(rwList.get(0));
            rwList.get(0).addTaxonCitations(taxonCitation);
            taxon11.getTaxonCitations().add(taxonCitation);
            dataObjects.add(taxonCitation);
            
            
            LocalityCitation localityCitation = new LocalityCitation();
            localityCitation.initialize();
            localityCitation.setLocality(ce1.getLocality());
            ce1.getLocality().getLocalityCitations().add(localityCitation);
            localityCitation.setReferenceWork(rwList.get(1));
            rwList.get(1).addLocalityCitations(localityCitation);
            dataObjects.add(localityCitation);
        }*/
        
        ////////////////////////////////
        // Workbench
        ////////////////////////////////
        
        // setup a template and its mapping items
        String name = "Simple DataSet";
        WorkbenchTemplate wbTemplate = createWorkbenchTemplate(user, name, "These are the remarks");
        WorkbenchTemplateMappingItem wbtmi0 = createWorkbenchMappingItem("CollectionObject", 
                                                                        1, "fieldNumber", "Field Number", 25, 0, 0, wbTemplate);
        WorkbenchTemplateMappingItem wbtmi1 = createWorkbenchMappingItem("CollectionObject",    
                                                                        1, "catalogedDate", "Cataloged Date", 25, 1, 1, wbTemplate);
        WorkbenchTemplateMappingItem wbtmi2 = createWorkbenchMappingItem("CollectionObject", 
                                                                        1, "catalogNumber", "Catalog Number", 25, 2, 2, wbTemplate);
        WorkbenchTemplateMappingItem wbtmi3 = createWorkbenchMappingItem("CollectionObject", 
                                                                        1, "collectionObjectYesNo1", "Yes/No", 8, 3, 3, wbTemplate);

        dataObjects.add(wbTemplate);
        dataObjects.add(wbtmi0);
        dataObjects.add(wbtmi1);
        dataObjects.add(wbtmi2);
        dataObjects.add(wbtmi3);

        // setup a workbench based on that template
        Workbench         workBench  = createWorkbench(user, name, "These are the remarks", "field_notebook.cvs", wbTemplate);
        dataObjects.add(workBench);

        // create a bunch of rows for the workbench
        for (int i = 1; i <= 14; ++i)
        {
            WorkbenchRow wbRow = workBench.addRow();
            WorkbenchDataItem wbdi0 = createWorkbenchDataItem(wbRow, "RS-10" + i, 0);
            
            // just to make the dates look a little random
            int date = (i*547) % 31 + 1;
            String dateStr = "0" + Integer.toString(date);
            dateStr = dateStr.substring(dateStr.length()-2);
            WorkbenchDataItem wbdi1 = createWorkbenchDataItem(wbRow, "03/" + dateStr + "/2007", 1);
            WorkbenchDataItem wbdi2 = createWorkbenchDataItem(wbRow, "CN-10" + i, 2);
            
            String boolValAsStr = null;
            switch (i % 3)
            {
                case 0:
                {
                    boolValAsStr = "true";
                    break;
                }
                case 1:
                {
                    boolValAsStr = "false";
                    break;
                }
                case 2:
                {
                    boolValAsStr = "";
                    break;
                }
            }
            boolValAsStr = "";
            WorkbenchDataItem wbdi3 = createWorkbenchDataItem(wbRow, boolValAsStr, 3);
            
            WorkbenchRowImage wbRowImage = null;
            
            File f = new File("demo_files" + File.separator + "card" + i + (i == 2 ? ".png" : ".jpg"));
            if (f.exists())
            {
                try
                {
                    int imageIndex = wbRow.addImage(f);
                    wbRowImage= wbRow.getRowImage(imageIndex);
                }
                catch (IOException e)
                {
                    log.error("Unable to add card image to workbench row", e);
                }
            }

            dataObjects.add(wbRow);
            dataObjects.add(wbdi0);
            dataObjects.add(wbdi1);
            dataObjects.add(wbdi2);
            if (wbRowImage != null)
            {
                dataObjects.add(wbRowImage);
            }
            
            // since some of these values will be "", the data item might be null
            if (wbdi3 != null)
            {
                dataObjects.add(wbdi3);
            }
        }
        
//        // create a workbench that uses the old, single-image capabilities
//        Workbench         workBench2  = createWorkbench(user, name + " (pre-conversion)", "These are the remarks", "field_notebook.cvs", wbTemplate);
//        dataObjects.add(workBench2);
//
//        // create a bunch of rows for the workbench
//        for (int i = 1; i <= 14; ++i)
//        {
//            WorkbenchRow wbRow = workBench2.addRow();
//            WorkbenchDataItem wbdi0 = createWorkbenchDataItem(wbRow, "RS-10" + i, 0);
//            
//            // just to make the dates look a little random
//            int date = (i*547) % 31 + 1;
//            String dateStr = "0" + Integer.toString(date);
//            dateStr = dateStr.substring(dateStr.length()-2);
//            WorkbenchDataItem wbdi1 = createWorkbenchDataItem(wbRow, "03/" + dateStr + "/2007", 1);
//            WorkbenchDataItem wbdi2 = createWorkbenchDataItem(wbRow, "CN-10" + i, 2);
//            
//            String boolValAsStr = null;
//            switch (i % 3)
//            {
//                case 0:
//                {
//                    boolValAsStr = "true";
//                    break;
//                }
//                case 1:
//                {
//                    boolValAsStr = "false";
//                    break;
//                }
//                case 2:
//                {
//                    boolValAsStr = "";
//                    break;
//                }
//            }
//            boolValAsStr = "";
//            WorkbenchDataItem wbdi3 = createWorkbenchDataItem(wbRow, boolValAsStr, 3);
//            
//            WorkbenchRowImage wbRowImage = null;
//            
//            File f = new File("demo_files" + File.separator + "card" + i + (i == 2 ? ".png" : ".jpg"));
//            if (f.exists())
//            {
//                try
//                {
//                    // NOTE: this is not scaling the images to the proper sizes.  Since this is just sample DB/test code, this isn't a problem.
//                    byte[] imageData = FileUtils.readFileToByteArray(f);
//                    wbRow.setCardImageData(imageData);
//                    wbRow.setCardImageFullPath(f.getAbsolutePath());
//                }
//                catch (IOException e)
//                {
//                    log.error("Unable to add card image to workbench row", e);
//                }
//            }
//
//            dataObjects.add(wbRow);
//            dataObjects.add(wbdi0);
//            dataObjects.add(wbdi1);
//            dataObjects.add(wbdi2);
//            if (wbRowImage != null)
//            {
//                dataObjects.add(wbRowImage);
//            }
//            
//            // since some of these values will be "", the data item might be null
//            if (wbdi3 != null)
//            {
//                dataObjects.add(wbdi3);
//            }
//        }

        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        
        frame.setProcess(++createStep);
               
        ////////////////////////////////
        // attachments (attachment metadata)
        ////////////////////////////////
        
            log.info("Creating attachments and attachment metadata");
            try
            {
                String attachmentFilesLoc = "demo_files" + File.separator;

//                String bigEyeFilePath = attachmentFilesLoc + "bigeye.jpg";
//                Attachment bigEye = createAttachment(bigEyeFilePath, "image/jpeg", 0);
//                bigEye.setLoan(closedLoan);
                
                String[] names  = {"Beach",     "Stewart",  "Spears",  "Kumin",   "Bentley"};
                String[] photos = {"beach.jpg", "josh.jpg", "rod.jpg", "meg.jpg", "andy.jpg"};
                for (Agent agent : agents)
                {
                    for (int i=0;i<names.length;i++)
                    {
                        if (agent.getLastName() != null && agent.getLastName().startsWith(names[i]))
                        {
                            String photoPath = attachmentFilesLoc + photos[i];
                            
                            // create the attachment record
                            Attachment photoAttachment = createAttachment(photoPath, "image/jpeg");
                            dataObjects.add(photoAttachment);

                            // link the attachment to the agent
                            AgentAttachment agentAttach = new AgentAttachment();
                            agentAttach.initialize();
                            agentAttach.setAgent(agent);
                            agentAttach.setAttachment(photoAttachment);
                            agentAttach.setOrderIndex(0);
                            dataObjects.add(agentAttach);
                            
                            // add some metadata to the attachment record
                            AttachmentMetadata copyrightData = new AttachmentMetadata();
                            copyrightData.initialize();
                            copyrightData.setName("Copyright");
                            copyrightData.setValue("2006");
                            photoAttachment.getMetadata().add(copyrightData);
                            copyrightData.setAttachment(photoAttachment);
                            dataObjects.add(copyrightData);
                            
                            AttachmentMetadata defPhotoIndicator = new AttachmentMetadata();
                            defPhotoIndicator.initialize();
                            defPhotoIndicator.setName("Default Photo");
                            defPhotoIndicator.setValue("yes");
                            photoAttachment.getMetadata().add(defPhotoIndicator);
                            defPhotoIndicator.setAttachment(photoAttachment);
                            dataObjects.add(defPhotoIndicator);
                            
                            // store the actual file into the attachment storage system
                            AttachmentUtils.getAttachmentManager().setStorageLocationIntoAttachment(photoAttachment);
                            photoAttachment.storeFile();
                        }
                    }
                }
                    
//                String giftPdfPath = attachmentFilesLoc + "2004-18.pdf";
//                Attachment giftPDF = createAttachment(giftPdfPath, "application/pdf", 0);
//                giftPDF.setLoan(closedLoan);
//                
//                String accessionPdfPath = attachmentFilesLoc + "Seychelles.pdf";
//                Attachment accPDF = createAttachment(accessionPdfPath, "application/pdf", 0);
//                // TODO: change this to setAccession()
//                accPDF.setPermit(permit);
//                
//                String sharkVideoPath = attachmentFilesLoc + "shark5.mpg";
//                Attachment sharkVideo = createAttachment(sharkVideoPath, "video/mpeg4", 0);
//                sharkVideo.setLoan(closedLoan);
//    
//                Attachment sharkVideo2 = createAttachment(sharkVideoPath, "video/mpeg4", 0);
//                sharkVideo2.setCollectingEvent(ce1);
//    
//                String beakerPath = attachmentFilesLoc + "beaker.jpg";
//                Attachment beakerAsBeach = createAttachment(beakerPath, "image/jpg", 1);
//                beakerAsBeach.setAgent(agents.get(1));
//                
//                dataObjects.add(bigEye);
//                dataObjects.add(giftPDF);
//                dataObjects.add(accPDF);
//                dataObjects.add(sharkVideo);
//                dataObjects.add(sharkVideo2);
//                dataObjects.add(beakerAsBeach);
            }
            catch (Exception e)
            {
                log.error("Could not create attachments", e);
            }
        
        addConservatorData(userAgent, agents, collObjs);
        
        commitTx();
        
        frame.setProcess(++createStep);
        

        // done
        log.info("Done creating single discipline database: " + discipline.getTitle());
        return dataObjects;
    }

    public static List<Object> createSimpleLithoStrat(LithoStratTreeDef def)
    {
        log.info("createSimpleLithoStrat " + def.getName());

        List<Object> newObjs = new Vector<Object>();

        LithoStratTreeDefItem planet = new LithoStratTreeDefItem();
        planet.initialize();
        planet.setName("Surface");
        planet.setRankId(0);
        planet.setIsEnforced(true);
        planet.setFullNameSeparator(", ");

        LithoStratTreeDefItem superLitho = new LithoStratTreeDefItem();
        superLitho.initialize();
        superLitho.setName("SuperLitho");
        superLitho.setRankId(100);
        superLitho.setFullNameSeparator(", ");

        LithoStratTreeDefItem group = new LithoStratTreeDefItem();
        group.initialize();
        group.setName("Group");
        group.setRankId(200);
        group.setIsInFullName(true);
        group.setFullNameSeparator(", ");

        LithoStratTreeDefItem formation = new LithoStratTreeDefItem();
        formation.initialize();
        formation.setName("Formation");
        formation.setRankId(300);
        formation.setIsInFullName(true);
        formation.setFullNameSeparator(", ");

        LithoStratTreeDefItem member = new LithoStratTreeDefItem();
        member.initialize();
        member.setName("Member");
        member.setRankId(400);
        member.setIsInFullName(true);
        member.setFullNameSeparator(", ");

        LithoStratTreeDefItem bed = new LithoStratTreeDefItem();
        bed.initialize();
        bed.setName("Bed");
        bed.setRankId(500);
        bed.setIsInFullName(true);
        bed.setFullNameSeparator(", ");

        // setup parents
        bed.setParent(member);
        member.setParent(formation);
        formation.setParent(group);
        group.setParent(superLitho);
        superLitho.setParent(planet);

        // set the tree def for each tree def item
        planet.setTreeDef(def);
        superLitho.setTreeDef(def);
        group.setTreeDef(def);
        formation.setTreeDef(def);
        member.setTreeDef(def);
        bed.setTreeDef(def);
        
        Set<LithoStratTreeDefItem> defItems = def.getTreeDefItems();
        defItems.add(planet);
        defItems.add(superLitho);
        defItems.add(group);
        defItems.add(formation);
        defItems.add(member);
        defItems.add(bed);
        
        newObjs.add(planet);
        newObjs.add(superLitho);
        newObjs.add(group);
        newObjs.add(formation);
        newObjs.add(member);
        newObjs.add(bed);
        
        LithoStrat earth = new LithoStrat();
        earth.initialize();
        earth.setName("Earth");
        earth.setFullName("Earth");
        earth.setDefinition(def);
        earth.setDefinitionItem(planet);
        earth.setNodeNumber(1);
        earth.setHighestChildNodeNumber(1);
        
        newObjs.add(earth);
        
        return newObjs;
    }

    public static List<Object> createSimpleGeography(final GeographyTreeDef geoTreeDef)
    {
        log.info("createSimpleGeography " + geoTreeDef.getName());

        List<Object> newObjs = new Vector<Object>();

        // create the geo tree def items
        GeographyTreeDefItem root = createGeographyTreeDefItem(null, geoTreeDef, "GeoRoot", 0);
        root.setIsEnforced(true);
        GeographyTreeDefItem cont = createGeographyTreeDefItem(root, geoTreeDef, "Continent", 100);
        GeographyTreeDefItem country = createGeographyTreeDefItem(cont, geoTreeDef, "Country", 200);
        GeographyTreeDefItem state = createGeographyTreeDefItem(country, geoTreeDef, "State", 300);
        state.setIsInFullName(true);
        GeographyTreeDefItem county = createGeographyTreeDefItem(state, geoTreeDef, "County", 400);
        county.setIsInFullName(true);
        county.setTextAfter(" Co.");

        // 0
        newObjs.add(root);
        // 1
        newObjs.add(cont);
        // 2
        newObjs.add(country);
        // 3
        newObjs.add(state);
        // 4
        newObjs.add(county);

        // Create the planet Earth.
        // That seems like a big task for 5 lines of code.
        Geography earth = createGeography(geoTreeDef, null, "Earth", root.getRankId());
        Geography northAmerica = createGeography(geoTreeDef, earth, "North America", cont.getRankId());
        Geography us = createGeography(geoTreeDef, northAmerica, "United States", country.getRankId());
        List<Geography> states = createGeographyChildren(geoTreeDef, us,
                new String[] { "Kansas", "Iowa", "Nebraska" }, state.getRankId());
        // 5
        newObjs.add(earth);
        // 6
        newObjs.add(northAmerica);
        // 7
        newObjs.add(us);
        // 8, 9, 10
        newObjs.addAll(states);

        
        
        // Create Kansas and a few counties
        List<Geography> counties = createGeographyChildren(geoTreeDef, states.get(0),
                new String[] { "Douglas", "Johnson", "Osage", "Sedgwick" }, county.getRankId());
        // 11, 12, 13, 14
        newObjs.addAll(counties);
        counties = createGeographyChildren(geoTreeDef, states.get(1),
                new String[] { "Blackhawk", "Fayette", "Polk", "Woodbury", "Johnson" }, county.getRankId());
        // 15, 16, 17, 18, 19
        newObjs.addAll(counties);
        counties = createGeographyChildren(geoTreeDef, states.get(2),
                new String[] { "Dakota", "Logan", "Valley", "Wheeler", "Johnson" }, county.getRankId());
        // 20, 21, 22, 23, 24
        newObjs.addAll(counties);
        
        TreeHelper.fixFullnameForNodeAndDescendants(earth);
        earth.setNodeNumber(1);
        fixNodeNumbersFromRoot(earth);

        return newObjs;
    }


    public static List<Object> createSimpleGeologicTimePeriod(final GeologicTimePeriodTreeDef treeDef)
    {
        log.info("createSimpleGeologicTimePeriod " + treeDef.getName());

        List<Object> newObjs = new Vector<Object>();

        // Create a geologic time period tree definition
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

        TreeHelper.fixFullnameForNodeAndDescendants(level0);
        level0.setNodeNumber(1);
        fixNodeNumbersFromRoot(level0);
        
        return newObjs;
    }


    public static List<Object> createSimpleLocation(final LocationTreeDef locTreeDef)
    {
        log.info("createSimpleLocation " + locTreeDef.getName());

        List<Object> newObjs = new Vector<Object>();

        LocationTreeDefItem building = createLocationTreeDefItem(null, locTreeDef, "building", 0);
        building.setIsEnforced(true);
        LocationTreeDefItem room = createLocationTreeDefItem(building, locTreeDef, "room", 100);
        room.setIsInFullName(true);
        LocationTreeDefItem freezer = createLocationTreeDefItem(room, locTreeDef, "freezer", 200);
        freezer.setIsInFullName(true);
        LocationTreeDefItem shelf = createLocationTreeDefItem(freezer, locTreeDef, "shelf", 300);
        shelf.setIsInFullName(true);

        // Create the building
        Location dyche        = createLocation(locTreeDef, null,         "Dyche Hall", building.getRankId());
        Location rm606        = createLocation(locTreeDef, dyche,        "Room 606",   room.getRankId());
        Location freezerA     = createLocation(locTreeDef, rm606,        "Freezer A",  freezer.getRankId());
        Location shelf5       = createLocation(locTreeDef, freezerA,     "Shelf 5",    shelf.getRankId());
        Location shelf4       = createLocation(locTreeDef, freezerA,     "Shelf 4",    shelf.getRankId());
        Location shelf3       = createLocation(locTreeDef, freezerA,     "Shelf 3",    shelf.getRankId());
        Location shelf2       = createLocation(locTreeDef, freezerA,     "Shelf 2",    shelf.getRankId());
        Location shelf1       = createLocation(locTreeDef, freezerA,     "Shelf 1",    shelf.getRankId());

        Location rm701        = createLocation(locTreeDef, dyche,        "Room 701",   room.getRankId());
        Location freezerA_701 = createLocation(locTreeDef, rm701,        "Freezer A",  freezer.getRankId());
        Location shelf1_701   = createLocation(locTreeDef, freezerA_701, "Shelf 1",    shelf.getRankId());
        
        Location rm703        = createLocation(locTreeDef, dyche,        "Room 703",   room.getRankId());
        Location freezerA_703 = createLocation(locTreeDef, rm703,        "Freezer A",  freezer.getRankId());
        Location shelf1_703   = createLocation(locTreeDef, freezerA_703, "Shelf 1",    shelf.getRankId());
        Location shelf2_703   = createLocation(locTreeDef, freezerA_703, "Shelf 2",    shelf.getRankId());
        Location shelf3_703   = createLocation(locTreeDef, freezerA_703, "Shelf 3",    shelf.getRankId());
        
        // 0
        newObjs.add(building);
        // 1
        newObjs.add(room);
        // 2
        newObjs.add(freezer);
        // 3
        newObjs.add(shelf);
        // 4
        newObjs.add(dyche);
        // 5
        newObjs.add(rm606);
        // 6
        newObjs.add(freezerA);
        // 7
        newObjs.add(shelf5);
        // 8
        newObjs.add(shelf4);
        // 9
        newObjs.add(shelf3);
        // 10
        newObjs.add(shelf2);
        // 11
        newObjs.add(shelf1);
        // 12
        newObjs.add(rm701);
        // 13
        newObjs.add(freezerA_701);
        // 14
        newObjs.add(shelf1_701);
        // 15
        newObjs.add(rm703);
        // 16
        newObjs.add(freezerA_703);
        // 17
        newObjs.add(shelf1_703);
        // 18
        newObjs.add(shelf2_703);
        // 19
        newObjs.add(shelf3_703);
        
        TreeHelper.fixFullnameForNodeAndDescendants(dyche);
        dyche.setNodeNumber(1);
        fixNodeNumbersFromRoot(dyche);
        
        return newObjs;
    }


    public static List<Object> createSimpleTaxon(final TaxonTreeDef taxonTreeDef)
    {
        log.info("createSimpleTaxon " + taxonTreeDef.getName());

        Vector<Object> newObjs = new Vector<Object>();
        
        Set<TaxonTreeDefItem> newItems = TreeFactory.addStandardTaxonDefItems(taxonTreeDef);
        newObjs.addAll(newItems);
        
//        // Create a Taxon tree definition
//        TaxonTreeDefItem taxonRoot = createTaxonTreeDefItem(null, taxonTreeDef, "life", TaxonTreeDef.TAXONOMY_ROOT);
//        taxonRoot.setIsEnforced(true);
//        TaxonTreeDefItem defItemLevel0 = createTaxonTreeDefItem(taxonRoot, taxonTreeDef, "order", TaxonTreeDef.ORDER);
//        defItemLevel0.setIsEnforced(true);
//        TaxonTreeDefItem defItemLevel1 = createTaxonTreeDefItem(defItemLevel0, taxonTreeDef, "family", TaxonTreeDef.FAMILY);
//        TaxonTreeDefItem defItemLevel2 = createTaxonTreeDefItem(defItemLevel1, taxonTreeDef, "genus", TaxonTreeDef.GENUS);
//        defItemLevel2.setFormatToken("%G");
//        defItemLevel2.setIsEnforced(true);
//        defItemLevel2.setIsInFullName(true);
//        TaxonTreeDefItem defItemLevel3 = createTaxonTreeDefItem(defItemLevel2, taxonTreeDef, "species", TaxonTreeDef.SPECIES);
//        defItemLevel3.setFormatToken("%S");
//        defItemLevel3.setIsEnforced(true);
//        defItemLevel3.setIsInFullName(true);
//        TaxonTreeDefItem defItemLevel4 = createTaxonTreeDefItem(defItemLevel3, taxonTreeDef, "subspecies", TaxonTreeDef.SUBSPECIES);
//        defItemLevel4.setFormatToken("%SS");
//        defItemLevel4.setIsEnforced(false);
//        defItemLevel4.setIsInFullName(true);
        
        for (TaxonTreeDefItem item: newItems)
        {
            if (item.getRankId().equals(TaxonTreeDef.GENUS))
            {
                item.setFormatToken("%G");
            }
            else if (item.getRankId().equals(TaxonTreeDef.SPECIES))
            {
                item.setFormatToken("%S");
            }
            else if (item.getRankId().equals(TaxonTreeDef.SUBSPECIES))
            {
                item.setFormatToken("%SS");
            }
        }

//        // 0
//        newObjs.add(taxonRoot);
//        // 1
//        newObjs.add(defItemLevel0);
//        // 2
//        newObjs.add(defItemLevel1);
//        // 3
//        newObjs.add(defItemLevel2);
//        // 4
//        newObjs.add(defItemLevel3);
//        // 5
//        newObjs.add(defItemLevel4);
        
        // new items are 0 through 27
        
//        TaxonTreeDefItem root = taxonTreeDef.getDefItemByRank(TaxonTreeDef.TAXONOMY_ROOT);
//        TaxonTreeDefItem order = taxonTreeDef.getDefItemByRank(TaxonTreeDef.ORDER);
//        TaxonTreeDefItem family = taxonTreeDef.getDefItemByRank(TaxonTreeDef.FAMILY);
//        TaxonTreeDefItem genus = taxonTreeDef.getDefItemByRank(TaxonTreeDef.GENUS);

        Taxon life        = createTaxon(taxonTreeDef, null,        "Life",        TaxonTreeDef.TAXONOMY_ROOT);
        Taxon perciformes = createTaxon(taxonTreeDef, life,        "Perciformes", TaxonTreeDef.ORDER);
        Taxon percidae    = createTaxon(taxonTreeDef, perciformes, "Percidae",    TaxonTreeDef.FAMILY);
        Taxon ammocrypta  = createTaxon(taxonTreeDef, percidae,    "Ammocrypta",  TaxonTreeDef.GENUS);
        // 28
        newObjs.add(life);
        // 29
        newObjs.add(perciformes);
        // 30
        newObjs.add(percidae);
        // 31
        newObjs.add(ammocrypta);

        String[] speciesNames = { "asprella", "beanii", "bifascia", "clara", "meridiana", "pellucida", "vivax" };
        String[] commonNames  = {"Crystal darter", "Naked sand darter", "Florida sand darter", "Western sand darter", "Southern sand darter", "Eastern sand darter", "Scaly sand darter"};
        List<Object> kids = createTaxonChildren(taxonTreeDef, ammocrypta, speciesNames, commonNames, TaxonTreeDef.SPECIES);
        // 32, 33, 34, 35, 36, 37, 38
        newObjs.addAll(kids);

        Taxon carangidae = createTaxon(taxonTreeDef, perciformes, "Carangidae", TaxonTreeDef.FAMILY);
        Taxon caranx    = createTaxon(taxonTreeDef, carangidae, "Caranx", TaxonTreeDef.GENUS);
        
        // 39
        newObjs.add(carangidae);
        // 40
        newObjs.add(caranx);

        String[] speciesNames2 = { "bartholomaei", "caballus", "caninus", "crysos", "dentex", "hippos", "latus" };
        String[] commonNames2  = {"Yellow jack", "Green jack", "Pacific crevalle jack", "Blue runner", "White trevally", "Crevalle jack", "Horse-eye jack"};
        kids = createTaxonChildren(taxonTreeDef, caranx, speciesNames2, commonNames2, TaxonTreeDef.SPECIES);
        // 41, 42, 43, 44, 45, 46, 47
        newObjs.addAll(kids);

        TreeHelper.fixFullnameForNodeAndDescendants(life);
        life.setNodeNumber(1);
        fixNodeNumbersFromRoot(life);
        
        for (Object o: newObjs)
        {
            if (o instanceof Taxon)
            {
                Taxon t = (Taxon)o;
                t.setIsAccepted(true);
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
    public static int fixNodeNumbersFromRoot( Treeable root )
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

    public void persist(Object o)
    {
        if (session != null)
        {
            session.saveOrUpdate(o);
        }
    }

    public void persist(Object...objects)
    {
        for (Object o: objects)
        {
            persist(o);
        }
    }


    public void persist(List<?> oList)
    {
        frame.setProcess(0, oList.size());
        int cnt = 0;
        for (Object o: oList)
        {
            frame.setProcess(++cnt);
            //System.out.println("* " + cnt + " " + o.getClass().getSimpleName());
            persist(o);
        }
        frame.setProcess(oList.size());
    }


    public void startTx()
    {
        HibernateUtil.beginTransaction();
    }


    public void commitTx()
    {
        HibernateUtil.commitTransaction();
    }
    

    public void rollbackTx()
    {
        HibernateUtil.rollbackTransaction();
    }
    

    public Object getFirstObjectByClass( List<Object> objects, Class<?> clazz)
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
    public <T> T getObjectByClass( List<?> objects, Class<T> clazz, int index)
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

    /**
     * Returns a list of database object by class from the arg list.
     * @param objects the list of object (source)
     * @param clazz the class to use to filter
     * @return the new list of objects
     */
    protected List<?> getObjectsByClass(final List<Object> objects, Class<?> clazz)
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
    
    /**
     * Copies the DerbyDatabases dir to the User app dir
     */
    protected void copyToUserWorkingDir()
    {
        File src = new File(UIRegistry.getDefaultWorkingPath()+File.separator+"DerbyDatabases");
        File dst = new File(UIRegistry.getUserHomeAppDir()+File.separator+"DerbyDatabases");
        log.info("Copying DerbyDatabases from \n"+ src.getAbsolutePath() + "\n to \n" + dst.getAbsolutePath());
        try
        {
            FileUtils.copyDirectory(src, dst);
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Creates the dialog to find out what database and what database driver to use. 
     */
    protected void buildSetup(final String[] args)
    {
        boolean doEmptyBuild = false;
        String  derbyPath    = null;
        
        if (args != null && args.length > 0)
        {
            if (args.length == 2)
            {
                doEmptyBuild = args[0].equals("build_empty");
                derbyPath    = StringUtils.isNotEmpty(args[1]) ? args[1] : null;
                hideFrame    = true;
                //System.out.println("doEmptyBuild [ "+doEmptyBuild+" ]");
                
            } else
            {
                throw new RuntimeException("The args MUST be \"empty <derby path>\"");
            }
            
            File derbyDir = new File(derbyPath);
            if (!derbyDir.exists())
            {
                if (derbyDir.mkdirs())
                {
                    System.err.println("Couldn't create Derby Path["+derbyDir.getAbsolutePath()+"]");
                }
            }
        }
        
        UIRegistry.setAppName("Specify");
        
        UIRegistry.setJavaDBDir(derbyPath != null ? derbyPath : UIRegistry.getDefaultWorkingPath() + File.separator + "DerbyDatabases");
        
        if (hideFrame)
        {
            System.out.println("Derby Path [ "+UIRegistry.getJavaDBPath()+" ]");
        }
        
        System.setProperty(AppPreferences.factoryName,          "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
        System.setProperty("edu.ku.brc.dbsupport.DataProvider", "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set

        backstopPrefs = getInitializePrefs(null);
        
        String driverName   = backstopPrefs.getProperty("initializer.drivername",   "MySQL");
        String databaseName = backstopPrefs.getProperty("initializer.databasename", "testfish");        
            
        if (doEmptyBuild)
        {
            Discipline         discipline = Discipline.getDiscipline("fish");
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("Derby");
            buildEmptyDatabase(driverInfo, "localhost", "WorkBench", "guest", "guest", "guest", "guest", "guest@ku.edu", discipline);

        } else
        {
            setupDlg = new SetupDialog(databaseName, driverName);
             
            final SwingWorker worker = new SwingWorker()
            {
                @Override
                public Object construct()
                {
                    UIHelper.centerAndShow(setupDlg);
                    try
                    {
                        int seconds = 10;
                        while (seconds > 0)
                        {
                            setupDlg.setTitle(Integer.toString(seconds));
                            Thread.sleep(1000); 
                            seconds--;
                        }
                        
                        if (setupDlg != null)
                        {
                            setupDlg.closeDlg(false);
                        }
                    }
                    catch (Exception ex)
                    {
                        // ignore
                    }
                    return null;
                }
    
                //Runs on the event-dispatching thread.
                @SuppressWarnings("synthetic-access")
                @Override
                public void finished()
                {
                    // no-op
                }
            };
            worker.start();
        }

    }
    
    /** 
     * Starts the Build on a swinf worker thread.
     * 
     * @throws SQLException
     * @throws IOException
     */
    protected void startBuild(final String dbName, 
                              final String driverName, 
                              final Discipline discipline,
                              final String username, 
                              final String password)
    {
        final SwingWorker worker = new SwingWorker()
        {
            @Override
            public Object construct()
            {
                try
                {
                    if (false) // XXX Debug
                    {
                        DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("Derby");
                        buildEmptyDatabase(driverInfo, "localhost", "mydata", username, password, "Rod", "Spears", "rods@ku.edu", discipline);

                    } else
                    {
                        build(dbName, driverName, discipline);
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
                frame.setVisible(false);
                frame.dispose();


                return null;
            }

            //Runs on the event-dispatching thread.
            @Override
            public void finished()
            {
                // do nothing
                // for future reference:
                // for that matter, there was no real reason to use SwingWorker for this since
                // we're not doing anything on the Swing thread in this method
                // we could have just used a regular Thread object
                
                // In fact, we're doing stuff in the construct method that should happen on the Swing
                // thread.  The construct() method IS NOT run on the Swing thread.
            }
        };
        worker.start();
    }
    
    /** 
     * Drops, Creates and Builds the Database.
     * 
     * @throws SQLException
     * @throws IOException
     */
    public boolean buildEmptyDatabase(final DatabaseDriverInfo driverInfo,
                                      final String hostName, 
                                      final String dbName, 
                                      final String username, 
                                      final String password, 
                                      final String firstName, 
                                      final String lastName, 
                                      final String email,
                                      final Discipline  discipline)
    {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(500,125));
        frame.setTitle("Building Specify Database");
        if (!hideFrame)
        {
            UIHelper.centerAndShow(frame);
        } else
        {
            System.out.println("Building Specify Database Username["+username+"]");
        }
        
        frame.setProcessPercent(true);
        frame.setOverall(0, 4);
        frame.getCloseBtn().setVisible(false);

        steps = 0;
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                frame.getProcessProgress().setIndeterminate(true);
                frame.getProcessProgress().setString("");
                frame.setDesc("Creating Database Schema for "+dbName);
                frame.setOverall(steps++);
            }
        });
        
        try
        {
            if (hideFrame) System.out.println("Creating schema");
            
            SpecifySchemaGenerator.generateSchema(driverInfo, hostName, dbName, username, password);
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.getProcessProgress().setIndeterminate(true);
                    frame.getProcessProgress().setString("");
                    frame.setDesc("Logging into "+dbName+"....");
                    frame.setOverall(steps++);
                }
            });
            
            String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, hostName, dbName);
            if (connStr == null)
            {
                connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, dbName);
            }
            
            if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                    driverInfo.getDialectClassName(), 
                    dbName, 
                    connStr, 
                    username, 
                    password))
            {
                if (hideFrame) System.out.println("Login Failed!");
                return false;
            }         
            
            setSession(HibernateUtil.getCurrentSession());

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.getProcessProgress().setIndeterminate(true);
                    frame.getProcessProgress().setString("");
                    frame.setDesc("Creating database "+dbName+"....");
                    frame.setOverall(steps++);
                }
            });
            
            Thumbnailer thumb = new Thumbnailer();
            thumb.registerThumbnailers("config/thumbnail_generators.xml");
            thumb.setQuality(.5f);
            thumb.setMaxHeight(128);
            thumb.setMaxWidth(128);

            AttachmentManagerIface attachMgr = new FileStoreAttachmentManager(UIRegistry.getAppDataSubDir("AttachmentStorage", true));
            AttachmentUtils.setAttachmentManager(attachMgr);
            AttachmentUtils.setThumbnailer(thumb);
            
            if (hideFrame) System.out.println("Creating Empty Database");
            
            List<Object> dataObjects = createEmptyDiscipline(discipline.getTitle(), 
                                                             discipline.getName(),
                                                             username,
                                                             "CollectionManager",
                                                             firstName,
                                                             lastName,
                                                             email);
            

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.getProcessProgress().setIndeterminate(true);
                    frame.getProcessProgress().setString("");
                    frame.setDesc("Saving data into "+dbName+"....");
                    frame.setOverall(steps++);
                }
            });
            
            if (hideFrame) System.out.println("Persisting Data...");
            startTx();
            persist(dataObjects);
            commitTx();
            HibernateUtil.getCurrentSession().close();
            
            if (hideFrame) System.out.println("Done.");
            
            frame.setVisible(false);
            frame.dispose();
            
            return true;
            
        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    /** 
     * Drops, Creates and Builds the Database.
     * 
     * @throws SQLException
     */
    protected void build(final String dbName, 
                         final String driverName, 
                         final Discipline discipline) throws SQLException
    {
        boolean doingDerby = StringUtils.contains(driverName, "Derby");

        frame = new ProgressFrame("Building sample DB");
        frame.setSize(new Dimension(500,125));
        frame.setTitle("Building Test Database");
        UIHelper.centerAndShow(frame);
        frame.setProcessPercent(true);
        frame.setOverall(0, 5+(doingDerby ? 1 : 0));
        frame.getCloseBtn().setVisible(false);

        System.setProperty(AppPreferences.factoryName,          "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
        System.setProperty("edu.ku.brc.dbsupport.DataProvider", "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set

        Properties props = getInitializePrefs(dbName);
        if (props.size() > 0)
        {
            initPrefs = props;
        } else
        {
            initPrefs = backstopPrefs;
        }
        
        String userName     = initPrefs.getProperty("initializer.username", "rods");
        String password     = initPrefs.getProperty("initializer.password", "rods");
        String databaseHost = initPrefs.getProperty("initializer.host",     "localhost");

        steps = 0;
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                frame.getProcessProgress().setIndeterminate(true);
                frame.getProcessProgress().setString("");
                frame.setDesc("Creating Database Schema for "+dbName);
                frame.setOverall(steps++);
            }
        });
        
        DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver(driverName);
        if (driverInfo == null)
        {
            throw new RuntimeException("Couldn't find driver by name ["+driverInfo+"] in driver list.");
        }
        SpecifySchemaGenerator.generateSchema(driverInfo, databaseHost, dbName, userName, password);

        /*class PostInsertEL implements org.hibernate.event.PostInsertEventListener
        {
            protected int counter = 0;
            public void onPostInsert(PostInsertEvent arg0)
            {
                if (arg0.getEntity() instanceof FormDataObjIFace)
                {
                    FormDataObjIFace dataObj = (FormDataObjIFace)arg0.getEntity();
                    System.out.println(counter + " PostInsert["+dataObj.getId()+"] "+dataObj.getDataClass().getSimpleName() + " Id: " +dataObj.getId());
                    counter++;
                }
            }

        }
        
        class PreInsertEL implements org.hibernate.event.PreInsertEventListener
        {
            protected int counter = 0;
            @Override
            public boolean onPreInsert(PreInsertEvent arg0)
            {
                if (arg0.getEntity() instanceof FormDataObjIFace)
                {
                    FormDataObjIFace dataObj = (FormDataObjIFace)arg0.getEntity();
                    //if (dataObj.getDataClass().getSimpleName().indexOf("Conserv") > -1)
                    System.out.println(counter + " PreInsert["+dataObj.getId()+"] "+dataObj.getDataClass().getSimpleName());// + " Id: " +dataObj.getId());
                    counter++;
                }
                return false;
            }
            
        }*/
        
        //HibernateUtil.setListener("post-commit-update", new edu.ku.brc.specify.dbsupport.PostUpdateEventListener());
        //HibernateUtil.setListener("post-commit-insert", new PostInsertEL());
        //HibernateUtil.setListener("pre-insert", new PreInsertEL());
        //HibernateUtil.setListener("post-commit-delete", new edu.ku.brc.specify.dbsupport.PostDeleteEventListener());
        //HibernateUtil.setListener("delete", new edu.ku.brc.specify.dbsupport.DeleteEventListener());
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                frame.setDesc("Logging in...");
                frame.setOverall(steps++);
            }
        });

        if (UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                              driverInfo.getDialectClassName(), 
                              dbName, 
                              driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHost, dbName), 
                              userName, 
                              password))
        {
            
            boolean single = true;
            if (single)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        frame.setDesc("Creating data...");
                        frame.setOverall(steps++);
                    }
                });

                try
                {
                    Thumbnailer thumb = new Thumbnailer();
                    thumb.registerThumbnailers("config/thumbnail_generators.xml");
                    thumb.setQuality(.5f);
                    thumb.setMaxHeight(128);
                    thumb.setMaxWidth(128);

                    AttachmentManagerIface attachMgr = new FileStoreAttachmentManager(UIRegistry.getAppDataSubDir("AttachmentStorage", true));
                    
                    AttachmentUtils.setAttachmentManager(attachMgr);
                    AttachmentUtils.setThumbnailer(thumb);
                    
                    // save it all to the DB
                    setSession(HibernateUtil.getCurrentSession());

                    List<Object> dataObjects = createSingleDiscipline(discipline);

                    log.info("Persisting in-memory objects to DB");
                    

                    frame.setProcess(0);
                    frame.getProcessProgress().setIndeterminate(true);
                    frame.getProcessProgress().setString("");
                    frame.setDesc("Getting Session...");
                    frame.setOverall(steps++);
                    
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            frame.setDesc("Saving data...");
                            frame.setOverall(steps++);
                        }
                    });
                    
                    startTx();
                    //persist(dataObjects.get(0)); // just persist the CollectionType object
                    persist(dataObjects);
                    commitTx();
                    

                    frame.setDesc("Done Saving data...");
                    frame.setOverall(steps++);

                    
                    if (true)
                    {
                        List<?> journal    = HibernateUtil.getCurrentSession().createCriteria(Journal.class).list();
                        List<?> taxa       = HibernateUtil.getCurrentSession().createQuery("SELECT t FROM Taxon t WHERE t.name = 'Ammocrypta'").list();
                        List<?> localities = HibernateUtil.getCurrentSession().createCriteria(Locality.class).list();
                        List<ReferenceWork> rwList = new Vector<ReferenceWork>();

                        startTx();
                        rwList.addAll(((Journal)journal.get(0)).getReferenceWorks());
                        
                        TaxonCitation taxonCitation = new TaxonCitation();
                        taxonCitation.initialize();
                        Taxon ammocrypta = (Taxon)taxa.get(0);
                        taxonCitation.setTaxon(ammocrypta);
                        taxonCitation.setReferenceWork(rwList.get(0));
                        rwList.get(0).addTaxonCitations(taxonCitation);
                        ammocrypta.getTaxonCitations().add(taxonCitation);
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
                    

                    frame.setDesc("Copying Preferences...");
                    frame.setOverall(steps++);

                    AppPreferences remoteProps = AppPreferences.getRemote();
                    
                    for (Object key : initPrefs.keySet())
                    {
                        String keyStr = (String)key;
                        if (!keyStr.startsWith("initializer.") && !keyStr.startsWith("useragent."))
                        {
                            remoteProps.put(keyStr, (String)initPrefs.get(key)); 
                        }
                    }
                    AppPreferences.getRemote().flush();
                    
                    
                    if (doingDerby)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                frame.getProcessProgress().setIndeterminate(true);
                                frame.getProcessProgress().setString("");
                                frame.setDesc("Copying DerbyDatases to User App Dir");
                                frame.setOverall(steps++);
                            }
                        });
                        copyToUserWorkingDir();
                    }
                    

                    frame.setDesc("Build Completed.");
                    frame.setOverall(steps++);

                    
                    log.info("Done");
                }
                catch(Exception e)
                {
                    try
                    {
                        rollbackTx();
                        log.error("Failed to persist DB objects", e);
                        return;
                    }
                    catch(Exception e2)
                    {
                        log.error("Failed to persist DB objects.  Rollback failed.  DB may be in inconsistent state.", e2);
                        return;
                    }
                }
            }
        }
        else
        {
            log.error("Login failed");
            return;
        }
        
        System.out.println("All done");
    }
    
    public static void turnOnHibernateLogging(Level level)
    {
        for (Enumeration<?> e=LogManager.getCurrentLoggers(); e.hasMoreElements();)
        {
            Logger    logger = (Logger)e.nextElement();
            if (StringUtils.contains(logger.getName(), "hibernate"))
            {
                logger.setLevel(level);
            }
        }

    }
    
    /**
     * Returns the Properties by database name.
     * @param databaseName the database
     * @return the properties
     */
    public static Properties getInitializePrefs(final String databaseName)
    {
        Properties properties = new Properties();
        try
        {
            String base = UIRegistry.getDefaultWorkingPath();
            File initFile = new File(base + File.separator + (databaseName != null ? (databaseName + "_") : "") + "init.prefs");
            if (initFile.exists())
            {
                properties.load(new FileInputStream(initFile));
                return properties;
            } 
            System.out.println("Couldn't find ["+initFile.getAbsolutePath()+"]");
            
        } catch (Exception ex)
        {
            System.err.println(ex); // XXX Error Dialog
        }
        return new Properties();
    }
    
    
    // XXX may want to use the Logon Dialog in the future
    class SetupDialog extends JDialog
    {
        protected String             databaseName;
        protected DatabaseDriverInfo dbDriver;
        protected boolean            isCancelled = false;
        
        protected JTextField         usernameTxtFld;
        protected JPasswordField     passwdTxtFld;
        protected JTextField         databaseNameTxt;
        protected JComboBox          drivers;
        protected JComboBox          disciplines;
        protected Vector<DatabaseDriverInfo> driverList;
        protected boolean            wasClosed = false;
        
        /**
         * Creates a dialog for entering database name and selecting the appropriate driver.
         * @param databaseName 
         * @param dbDriverName
         */
        public SetupDialog(final String databaseName, final String dbDriverName)
        {
            super();
            
            driverList = DatabaseDriverInfo.getDriversList();
            int inx = Collections.binarySearch(driverList, new DatabaseDriverInfo(dbDriverName, null, null));
            
            drivers     = new JComboBox(driverList);
            drivers.setSelectedIndex(inx);
            
            //Vector<Discipline> disciplinesList = Discipline.getDisciplineList();
            disciplines     = new JComboBox(Discipline.getDisciplineList());
            disciplines.setSelectedItem(Discipline.getDiscipline("fish"));
            
            databaseNameTxt = new JTextField(databaseName);
            
            usernameTxtFld = new JTextField("rods");
            passwdTxtFld   = new JPasswordField("rods");
            
            PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,2px,p:g", "p,4px,p,4px,p,4px,p,4px,p,10px,p"));
            CellConstraints cc         = new CellConstraints();
            builder.add(new JLabel("Username:", SwingConstants.RIGHT),      cc.xy(1,1));
            builder.add(usernameTxtFld,                                     cc.xy(3,1));
            builder.add(new JLabel("Password:", SwingConstants.RIGHT),      cc.xy(1,3));
            builder.add(passwdTxtFld,                                       cc.xy(3,3));
            builder.add(new JLabel("Database Name:", SwingConstants.RIGHT), cc.xy(1,5));
            builder.add(databaseNameTxt,                                    cc.xy(3,5));
            builder.add(new JLabel("Discipline Name:", SwingConstants.RIGHT), cc.xy(1,7));
            builder.add(disciplines,                                    cc.xy(3,7));
            builder.add(new JLabel("Driver:", SwingConstants.RIGHT),        cc.xy(1,9));
            builder.add(drivers,                                            cc.xy(3,9));
            
            final JButton okBtn     = new JButton("OK");
            final JButton cancelBtn = new JButton("Cancel");
            builder.add(ButtonBarFactory.buildOKCancelBar(okBtn, cancelBtn), cc.xywh(1,11,3,1));
            
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    closeDlg(true);
                }
             });
             
            okBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    closeDlg(false);
                }
             });
            
            // make sure closing the window does the same thing as clicking cancel
            this.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosed(WindowEvent e)
                {
                    cancelBtn.doClick();
                }
            });
            
            builder.setDefaultDialogBorder();
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setContentPane(builder.getPanel());
            pack();
            Dimension dim = getPreferredSize();
            dim.width = 300;
            setSize(dim);
        }
        
        public void closeDlg(final boolean wasCancelled)
        {
            if (!wasClosed)
            {
                isCancelled = wasCancelled;
                if (!isCancelled)
                {
                    databaseName = databaseNameTxt.getText();
                    if (StringUtils.isEmpty(databaseName))
                    {
                        isCancelled = true;
                        
                    } else if (drivers.getSelectedIndex() > -1)
                    {
                        dbDriver = (DatabaseDriverInfo)drivers.getSelectedItem();
                    } else
                    {
                        isCancelled = true;
                    }
                }
                setVisible(false);
                
                if (!isCancelled)
                {
                    try
                    {
                        String username = usernameTxtFld.getText();
                        String password = new String(passwdTxtFld.getPassword());
                        startBuild(databaseName, dbDriver.getName(), (Discipline)disciplines.getSelectedItem(), username, password);
                        
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                wasClosed = true;
                setupDlg.dispose();
                setupDlg = null;
            }
        }
    }
    
    boolean debugOn = false;
    protected void loadLocalization(final SpLocaleContainerItem memoryItem, final SpLocaleContainerItem newItem)
    {
        newItem.setName(memoryItem.getName());
        newItem.setType(memoryItem.getType());
       
        for (SpLocaleItemStr nm : memoryItem.getNames())
        {
            SpLocaleItemStr str = new SpLocaleItemStr();
            str.initialize();
            
            str.setText(nm.getText());
            if (debugOn) System.out.println(nm.getText());
            str.setLanguage(nm.getLanguage());
            str.setCountry(nm.getCountry());
            str.setVariant(nm.getVariant());
            
            newItem.getNames().add(str);
            str.setItemName(newItem);
        }
        
        for (SpLocaleItemStr desc : memoryItem.getDescs())
        {
            SpLocaleItemStr str = new SpLocaleItemStr();
            str.initialize();
            
            str.setText(desc.getText());
            if (debugOn) System.out.println(desc.getText());
            str.setLanguage(desc.getLanguage());
            str.setCountry(desc.getCountry());
            str.setVariant(desc.getVariant());
            
            newItem.getDescs().add(str);
            str.setItemDesc(newItem);
        }

    }
    
    protected void loadLocalization(final SpLocaleContainer memoryContainer, final SpLocaleContainer newContainer)
    {
        newContainer.setName(memoryContainer.getName());
        newContainer.setType(memoryContainer.getType());
        
        debugOn = memoryContainer.getName().equals("accession");
       
        for (SpLocaleItemStr nm : memoryContainer.getNames())
        {
            SpLocaleItemStr str = new SpLocaleItemStr();
            str.initialize();
            
            str.setText(nm.getText());
            str.setLanguage(nm.getLanguage());
            str.setCountry(nm.getCountry());
            str.setVariant(nm.getVariant());
            
            newContainer.getNames().add(str);
            str.setContainerName(newContainer);
        }
        
        for (SpLocaleItemStr desc : memoryContainer.getDescs())
        {
            SpLocaleItemStr str = new SpLocaleItemStr();
            str.initialize();
            
            str.setText(desc.getText());
            str.setLanguage(desc.getLanguage());
            str.setCountry(desc.getCountry());
            str.setVariant(desc.getVariant());
            
            newContainer.getDescs().add(str);
            str.setContainerDesc(newContainer);
        }
        
        for (SpLocaleContainerItem item : memoryContainer.getItems())
        {
            SpLocaleContainerItem newItem = new SpLocaleContainerItem();
            newItem.initialize();
            
            newContainer.getItems().add(newItem);
            newItem.setContainer(newContainer);
            
            loadLocalization(item, newItem);
        }
    }
    
    /**
     * @param collTyp
     * @param schemaType
     * @param tableMgr
     */
    protected void loadSchemaLocalization(final CollectionType collTyp, final Byte schemaType, final DBTableIdMgr tableMgr)
    {
        SchemaLocalizerXMLHelper schemaLocalizer = new SchemaLocalizerXMLHelper(schemaType, tableMgr);
        schemaLocalizer.load();
        
        for (SpLocaleContainer table : schemaLocalizer.getSpLocaleContainers())
        {
            SpLocaleContainer container = new SpLocaleContainer();
            container.initialize();
            container.setName(table.getName());
            container.setType(table.getType());
            container.setSchemaType(schemaType);
            
            loadLocalization(table, container);
            
            collTyp.getSpLocaleContainers().add(container);
            container.setCollectionType(collTyp);
        }
    }
    
    public static void main(final String[] args)
    {
        if (args != null && args.length > 0)
        {
            System.out.println("BuildSampleDatabase ");
           
            BuildSampleDatabase builder = new BuildSampleDatabase();
            builder.buildSetup(args);
   
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    
                    try
                    {
                        if (!System.getProperty("os.name").equals("Mac OS X"))
                        {
                            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                            PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    
                    BuildSampleDatabase builder = new BuildSampleDatabase();
                    builder.buildSetup(null);
                }
            });
        }
    }
}