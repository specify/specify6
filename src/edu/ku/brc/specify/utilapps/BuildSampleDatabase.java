
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
import static edu.ku.brc.specify.utilapps.DataBuilder.createAgentVariant;
import static edu.ku.brc.specify.utilapps.DataBuilder.createAttachment;
import static edu.ku.brc.specify.utilapps.DataBuilder.createAttributeDef;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCatalogNumberingScheme;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectingEvent;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectingEventAttr;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectingTrip;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollection;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectionObject;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectionObjectAttr;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollector;
import static edu.ku.brc.specify.utilapps.DataBuilder.createDataType;
import static edu.ku.brc.specify.utilapps.DataBuilder.createGroupPerson;
import static edu.ku.brc.specify.utilapps.DataBuilder.createDetermination;
import static edu.ku.brc.specify.utilapps.DataBuilder.createDeterminationStatus;
import static edu.ku.brc.specify.utilapps.DataBuilder.createDiscipline;
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
import static edu.ku.brc.specify.utilapps.DataBuilder.createPermit;
import static edu.ku.brc.specify.utilapps.DataBuilder.createPickList;
import static edu.ku.brc.specify.utilapps.DataBuilder.createPrepType;
import static edu.ku.brc.specify.utilapps.DataBuilder.createPreparation;
import static edu.ku.brc.specify.utilapps.DataBuilder.createQuery;
import static edu.ku.brc.specify.utilapps.DataBuilder.createQueryField;
import static edu.ku.brc.specify.utilapps.DataBuilder.createReferenceWork;
import static edu.ku.brc.specify.utilapps.DataBuilder.createShipment;
import static edu.ku.brc.specify.utilapps.DataBuilder.createSpecifyUser;
import static edu.ku.brc.specify.utilapps.DataBuilder.createStorage;
import static edu.ku.brc.specify.utilapps.DataBuilder.createStorageTreeDef;
import static edu.ku.brc.specify.utilapps.DataBuilder.createStorageTreeDefItem;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.init.DBConfigInfo;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentAttachment;
import edu.ku.brc.specify.datamodel.AgentVariant;
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
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.ConservDescription;
import edu.ku.brc.specify.datamodel.ConservEvent;
import edu.ku.brc.specify.datamodel.ConservRecmdType;
import edu.ku.brc.specify.datamodel.ConservRecommendation;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.GroupPerson;
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
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PickList;
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
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDefItem;
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
    
    protected static boolean     debugOn  = false;

    protected Calendar           calendar = Calendar.getInstance();
    protected Session            session;
    
    protected int                steps = 0;   
    protected ProgressFrame      frame;
    protected Properties         initPrefs     = null;
    protected Properties         backstopPrefs = null;
    
    protected SetupDialog        setupDlg  = null;
    protected boolean            hideFrame = false;
    
    protected boolean            copyToUserDir      = true;
    protected boolean            doShallowTaxonTree = false;
    protected boolean            doExtraCollections = true;
    
    protected Random             rand = new Random(12345678L);
    
    protected Vector<Agent>       globalAgents = new Vector<Agent>();
    protected DeterminationStatus current      = null;
    protected DeterminationStatus notCurrent   = null;
    protected DeterminationStatus incorrect    = null;

    
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
    
    protected void standardQueries(final Vector<Object> dataObjects, final Agent userAgent)
    {
        //Byte greaterThan = SpQueryField.OperatorType.GREATERTHAN.getOrdinal();
        //Byte lessThan    = SpQueryField.OperatorType.LESSTHAN.getOrdinal();
        //Byte equals      = SpQueryField.OperatorType.EQUALS.getOrdinal();
        Byte greq        = SpQueryField.OperatorType.GREATERTHANEQUALS.getOrdinal();
        Byte lteq        = SpQueryField.OperatorType.LESSTHANEQUALS.getOrdinal();
        
        //Byte none        = SpQueryField.SortType.NONE.getOrdinal();
        Byte asc         = SpQueryField.SortType.ASC.getOrdinal();
        //Byte desc        = SpQueryField.SortType.DESC.getOrdinal();
        
        SpQuery query = createQuery("Collection Objects", "CollectionObject", 1, SpecifyUser.getCurrentUser(), userAgent);
        createQueryField(query, (short)0, "catalogNumber", false, greq, lteq, "102", "103", asc, true, "1");
        dataObjects.add(query);
    }

    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public List<Object> createEmptyDiscipline(final DBConfigInfo config)
    {
        Vector<Object> dataObjects = new Vector<Object>();

        
        int createStep = 0;
        
        frame.setProcess(0, 14);
        
        frame.setProcess(++createStep);

        Institution    institution    = createInstitution(config.getInstName());
        Division       division       = createDivision(institution, config.getDiscipline().getName(), config.getDivName(), config.getDivAbbrev(), config.getDivTitle());
        
        Agent            userAgent        = createAgent(config.getLastName(), config.getFirstName(), "", config.getLastName(), "", config.getEmail());
        UserGroup        userGroup        = createUserGroup(config.getDiscipline().getTitle());
        
        institution.setTitle(config.getInstTitle());
        
        frame.setDesc("Creating Core information...");
        frame.setProcess(++createStep);

        startTx();
        persist(userGroup);
        persist(institution);
        persist(division);
        
        SpecifyUser      user             = createSpecifyUser(config.getUsername(), config.getEmail(), (short) 0, userGroup, config.getUserType());
        persist(user);
        
        DataType         dataType         = createDataType(config.getDiscipline().getTitle());
        persist(dataType);
        
        frame.setDesc("Creating Trees Definitions...");
        frame.setProcess(++createStep);

        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        StorageTreeDef            locTreeDef        = createStorageTreeDef("Storage");
        
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        
        frame.setDesc("Creating Discipline...");
        frame.setProcess(++createStep);

        Discipline discipline = createDiscipline(division, config.getDiscipline().getName(), config.getDiscipline().getTitle(), 
                                                             dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, locTreeDef, lithoStratTreeDef);
        Discipline.setCurrentDiscipline(discipline);
        
        persist(discipline);
        persist(userAgent);
        
        frame.setDesc("Localizing Schema...");
        frame.setProcess(++createStep);

        loadSchemaLocalization(discipline, SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());
        
        frame.setDesc("Creating CatalogNumberingScheme...");
        frame.setProcess(++createStep);

        CatalogNumberingScheme cns = createCatalogNumberingScheme("CatalogNumber For "+config.getCollectionName(), "", true);
        
        persist(cns);
        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        frame.setDesc("Creating Collection...");
        frame.setProcess(++createStep);

        Collection collection = createCollection(config.getCollectionPrefix(), config.getCollectionName(), cns, discipline);
        persist(collection);
        
        Collection.setCurrentCollection(collection);

        frame.setDesc("Commiting...");
        frame.setProcess(++createStep);
        commitTx();
        
        startTx();
        
        //DBTableIdMgr schema = new DBTableIdMgr(false);
        //schema.initialize(new File(XMLHelper.getConfigDirPath("specify_datamodel.xml")));
        //loadSchemaLocalization(discipline, SpLocaleContainer, schema);
        
        SpecifyUser.setCurrentUser(user);
        user.addReference(userAgent, "agents");
        
        persist(user);

        ////////////////////////////////
        // build the tree def items and nodes
        ////////////////////////////////
        frame.setDesc("Creating Trees...");
        frame.setProcess(++createStep);
        //List<Object> taxa        = createSimpleTaxon(taxonTreeDef, doShallowTaxonTree);
        List<Object> geos        = createSimpleGeography(geoTreeDef);
        //List<Object> locs        = createSimpleStorage(locTreeDef);
        //List<Object> gtps        = createSimpleGeologicTimePeriod(gtpTreeDef);
        //List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef);
        
        //persist(taxa);
        persist(geos);
        //persist(locs);
        //persist(gtps);
        //persist(lithoStrats);
        
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////

        frame.setDesc("Creating Std Queries...");
        frame.setProcess(++createStep);
        standardQueries(dataObjects, userAgent);
        
        persist(dataObjects);
        dataObjects.clear();
        
        log.info("Creating picklists");
        frame.setDesc("Creating PickLists...");
        frame.setProcess(++createStep);
        
        createPickLists();
        
        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        
        ////////////////////////////////
        // Determination Status (Must be done here)
        ////////////////////////////////
        log.info("Creating determinations status");
        current    = createDeterminationStatus(discipline, "Current",    "", DeterminationStatus.CURRENT);
        notCurrent = createDeterminationStatus(discipline, "Not current","", DeterminationStatus.NOTCURRENT);
        incorrect  = createDeterminationStatus(discipline, "Incorrect",  "", DeterminationStatus.USERDEFINED);
        
        frame.setDesc("Commiting...");
        frame.setProcess(++createStep);
        //startTx();
        persist(current);
        persist(notCurrent);
        persist(incorrect);
        commitTx();
        
        frame.setProcess(++createStep);

        return dataObjects;
    }
    
    /**
     * Returns a list of object of a specified class.
     * @param cls the class
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
        
        startTx();
        persist(type);
        commitTx();
        
        ConservRecommendation recommend = new ConservRecommendation();
        recommend.initialize();
        recommend.setCompletedDate(Calendar.getInstance());
        recommend.addReference(userAgent, "curator");
        recommend.addReference(type, "conservRecmdType");

        ConservDescription desc = new ConservDescription();
        desc.initialize();
        desc.setShortDesc("Short Description");
        //desc.addReference(divs.get(0), "division");
        
        desc.addReference(recommend, "lightRecommendations");
        desc.addReference(colObjs.get(0), "collectionObject");
        
        //desc.setCollectionObject(colObjs.get(0));
        //colObjs.get(0).getConservDescriptions().add(desc);
        
        ConservEvent conservEvent = new ConservEvent();
        conservEvent.initialize();
        conservEvent.setExamDate(Calendar.getInstance());
        
        conservEvent.addReference(agents.get(1), "examinedByAgent");
        conservEvent.addReference(agents.get(2), "treatedByAgent");
        
        desc.addReference(conservEvent, "events");
        
        persist(desc);
        
        //commitTx();
        
    }
    
    protected BldrPickList createPickLists()
    {
        BldrPickList colMethods = null;
        
        List<BldrPickList> pickLists = DataBuilder.getBldrPickLists();
        DataBuilder.buildPickListFromXML(pickLists);
        for (BldrPickList pl : pickLists)
        {
            PickList pickList = createPickList(pl.getName(), pl.getType(), pl.getTableName(),
                                               pl.getFieldName(), pl.getFormatter(), pl.getReadOnly(), 
                                               pl.getSizeLimit(), pl.getIsSystem());
            for (BldrPickListItem item : pl.getItems())
            {
                pickList.addItem(item.getTitle(), item.getValue());
            }
            persist(pickList);
            
            if (pl.getName().equals("CollectingMethod"))
            {
                colMethods = pl;
            }
        }
        return colMethods;
    }
    
    protected CollectingEvent createFakeCollectingEvent(final List<Agent> agents,
                                                        final Locality farmpond)
    {
        int year = (int)(rand.nextDouble() * 20.0) +1990;
        int mon = (int)(rand.nextDouble() * 11.0) +1;
        Collector collectorMeg = createCollector(agents.get(2), 1);
        Collector collectorRod = createCollector(agents.get(3), 2);
        
        CollectingEvent ce2 = createCollectingEvent(farmpond, new Collector[]{collectorMeg, collectorRod});
        calendar.set(year, mon, 22, 06, 12, 00);
        ce2.setStartDate(calendar);
        ce2.setStartDateVerbatim("22 Apr "+year+", 6:12 AM");
        calendar.set(year, mon, 22, 07, 31, 00);
        ce2.setEndDate(calendar);
        ce2.setEndDateVerbatim("22 Apr "+year+", 7:31 AM");
        ce2.setMethod("Picked");
        return ce2;
    }
    
    protected Taxon getRandomTaxon(final int rank, final List<Object> taxa) 
    {
        
        Vector<Taxon> species = new Vector<Taxon>();
        for (Object tObj : taxa)
        {
            if (tObj instanceof Taxon)
            {
                Taxon t = (Taxon)tObj;
                if (t.getRankId().intValue() == rank)
                {
                    species.add(t);
                }
            }
        }
        int inx = (int)(rand.nextDouble() * species.size());
        return species.get(inx);
    }
    
    protected Agent getRandomAgent(final List<Agent> agents) 
    {
        int inx = (int)(rand.nextDouble() * agents.size());
        return agents.get(inx);
    }
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public List<Object> createSingleBotanyCollection(final DisciplineType disciplineType,
                                                     final Institution    institution,
                                                     final SpecifyUser    user)
    {
        frame.setProcess(0, 16);
        frame.setDesc("Creating Botany...");
        
        int createStep = 0;
        
        startTx();

        DataType         dataType = createDataType(disciplineType.getTitle());
        persist(dataType);
        
        Division       division   = createDivision(institution, disciplineType.getName(), "Botany", "BT", "Botany");
        
        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        StorageTreeDef           locTreeDef        = createStorageTreeDef("Storage");
        
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        
        Discipline discipline = createDiscipline(division, "Plant", disciplineType.getName(), 
                                                             dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, 
                                                             locTreeDef, lithoStratTreeDef);
        Discipline.setCurrentDiscipline(discipline);
        
        persist(division);
        persist(discipline);

        loadSchemaLocalization(discipline, SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());

        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           title            = initPrefs.getProperty("useragent.title",    "Mr.");
        String           firstName        = initPrefs.getProperty("useragent.firstname", "Rod");
        String           lastName         = initPrefs.getProperty("useragent.lastname", "Spears");
        String           midInit          = initPrefs.getProperty("useragent.midinit", "C");
        String           abbrev           = initPrefs.getProperty("useragent.abbrev", "rs");
        String           email            = initPrefs.getProperty("useragent.email", "rods@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", "CollectionManager");
        
        System.out.println("----- User Agent -----");
        System.out.println("Title:     "+title);
        System.out.println("FirstName: "+firstName);
        System.out.println("LastName:  "+lastName);
        System.out.println("MidInit:   "+midInit);
        System.out.println("Abbrev:    "+abbrev);
        System.out.println("Email:     "+email);
        System.out.println("UserType:  "+userType);
        
        Agent userAgent = createAgent(title, firstName, midInit, lastName, abbrev, email);
        
        discipline.addReference(userAgent, "agents");
        user.addReference(userAgent, "agents");
        
        persist(discipline);
        persist(userAgent);
        persist(user);
        
        frame.setProcess(++createStep);
        
        CatalogNumberingScheme cns = createCatalogNumberingScheme("CatalogNumber For Plants", "", true);
        
        persist(cns);
        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        Collection collection = createCollection("KUBOT", "Plant", cns, discipline);
        persist(collection);
        
        Collection.setCurrentCollection(collection);

        commitTx();
        
        frame.setProcess(++createStep);
        
        startTx();
        
        //DBTableIdMgr schema = new DBTableIdMgr(false);
        //schema.initialize(new File(XMLHelper.getConfigDirPath("specify_datamodel.xml")));
        //loadSchemaLocalization(discipline, SpLocaleContainer, schema);
        
        SpecifyUser.setCurrentUser(user);
        user.addReference(userAgent, "agents");
        
        persist(user);

        Journal journal = createJournalsAndReferenceWork();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // build the tree def items and nodes
        ////////////////////////////////
        List<Object> taxa        = createSimpleBotanyTaxonTree(taxonTreeDef);
        List<Object> geos        = createSimpleGeography(geoTreeDef);
        List<Object> locs        = createSimpleStorage(locTreeDef);
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

        Vector<Object> dataObjects = new Vector<Object>();
        
        standardQueries(dataObjects, userAgent);
        persist(dataObjects);
        dataObjects.clear();
        
        //BldrPickList colMethods = createPickLists();
        
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
        
        Locality farmpond = createLocality("Shoal Creek at Schermerhorn Park, S of Galena at Rt. 26", (Geography)geos.get(11));
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
        
        List<Agent>    agents      = new Vector<Agent>();
        
        lastName = userAgent.getLastName();
        Agent steveBoyd = createAgent("Mr.", "Steve", "D", "Boyd", "jb", "jb@net.edu");
        if (!lastName.equals("Cooper")) agents.add(createAgent("Mr.", "Peter", "D", "Cooper", "ds", "ds@whitehouse.gov"));
        if (!lastName.equals("Peck")) agents.add(createAgent("Mr.", "David", "H", "Peck", "rb", "beach@net.edu"));
        if (!lastName.equals("Appleton")) agents.add(createAgent("Mrs.", "Sally", "H", "Appleton", "jm", "jm@net.edu"));
        if (!lastName.equals("Brown")) agents.add(createAgent("Mr.", "Taylor", "C", "Brown", "kcs", "taylor.brown@ku.edu"));
        if (!lastName.equals("Boyd")) agents.add(steveBoyd);
        if (!lastName.equals("Thomas")) agents.add(createAgent("Mr", "James", "X", "Thomas", "dxt", ""));
        if (!lastName.equals("Peterson")) agents.add(createAgent("Mr.", "Pete", "A", "Peterson", "jb", ""));
        if (!lastName.equals("Guttenburg")) agents.add(createAgent("Mr.", "Mitch", "A", "Guttenburg", "jb", ""));
        if (!lastName.equals("Ford")) agents.add(createAgent("Mr.", "Daniel", "A", "Ford", "mas", "mas@ku.edu"));
        agents.add(userAgent);
        
        Agent ku = new Agent();
        ku.initialize();
        ku.setAbbreviation("KU");
        ku.setAgentType(Agent.ORG);
        ku.setLastName("University of Kansas");
        ku.setEmail("webadmin@ku.edu");
        ku.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        ku.setDiscipline(Discipline.getCurrentDiscipline());
        
        agents.add(ku);
        agents.get(0).setOrganization(ku);
        agents.get(1).setOrganization(ku);
        agents.get(2).setOrganization(ku);
        agents.get(3).setOrganization(ku);
        agents.get(8).setOrganization(ku);
        
        Agent otherAgent = new Agent();
        otherAgent.initialize();
        otherAgent.setAbbreviation("O");
        otherAgent.setAgentType(Agent.OTHER);
        otherAgent.setLastName("The Other Guys");
        otherAgent.setEmail("other@other.com");
        otherAgent.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        otherAgent.setDiscipline(Discipline.getCurrentDiscipline());
        agents.add(otherAgent);

        List<GroupPerson> gpList = new ArrayList<GroupPerson>();
        if (true)
        {
            startTx();
            Agent gm1 = createAgent("Mr.", "John", "A", "Lyon", "jal", "jal@group.edu");
            Agent gm2 = createAgent("Mr.", "Dave", "D", "Jones", "ddj", "ddj@group.edu");
            persist(gm1);
            persist(gm2);
            commitTx();
            
            Agent groupAgent = new Agent();
            groupAgent.initialize();
            groupAgent.setAbbreviation("GRP");
            groupAgent.setAgentType(Agent.GROUP);
            groupAgent.setLastName("The Group");
            groupAgent.setEmail("group@group.com");
            groupAgent.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
            groupAgent.setDiscipline(Discipline.getCurrentDiscipline());
            
            agents.add(groupAgent);
            
            gpList.add(createGroupPerson(groupAgent, gm1, 0));
            gpList.add(createGroupPerson(groupAgent, gm2, 1));
        }

        
        List<AgentVariant> agentVariants = new Vector<AgentVariant>();
        agentVariants.add(createAgentVariant(AgentVariant.VARIANT, "James Variant #1", steveBoyd));
        agentVariants.add(createAgentVariant(AgentVariant.VERNACULAR, "James VERNACULAR #1", steveBoyd));
     
        List<Address> addrs = new Vector<Address>();
        addrs.add(createAddress(agents.get(1), "1600 Pennsylvania Avenue NW", null, "Washington", "DC", "USA", "20500"));
        addrs.add(createAddress(agents.get(1), "??? Mississippi", null, "Lawrence", "KS", "USA", "66045"));
        addrs.add(createAddress(agents.get(2), "1 Main St", "", "Lenexa", "KS", "USA", "66071"));
        addrs.add(createAddress(agents.get(3), "13355 Inverness", "Bldg #3", "Lawrence", "KS", "USA", "66047"));
        addrs.add(createAddress(agents.get(4), "Natural History Museum", "Cromwell Rd", "London", null, "UK", "SW7 5BD"));
        addrs.add(createAddress(agents.get(6), "1212 Apple Street", null, "Chicago", "IL", "USA", "01010"));
        addrs.add(createAddress(agents.get(8), "11911 Oak Ln", null, "Orion", "KS", "USA", "66061"));
        addrs.add(createAddress(ku, null, null, "Lawrence", "KS", "USA", "66045"));
        
        // User Agent Address
        addrs.add(createAddress(userAgent, "1214 East Street", null, "Grinnell", "IA", "USA", "56060"));
        userAgent.setDivision(division);
                
        //startTx();
        persist(agents);
        persist(agentVariants);
        persist(gpList);
        //commitTx();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // collecting events (collectors, collecting trip)
        ////////////////////////////////
        log.info("Creating collecting events, collectors and a collecting trip");
        Collector collectorMitch = createCollector(agents.get(7), 2);
        Collector collectorJim = createCollector(agents.get(2), 1);
        
        CollectingEvent ce1 = createCollectingEvent(forestStream, new Collector[]{collectorMitch, collectorJim});
        calendar.set(1994, 4, 21, 11, 56, 00);
        ce1.setStartDate(calendar);
        ce1.setStartDateVerbatim("21 Apr 1994, 11:56 AM");
        calendar.set(1994, 4, 21, 13, 03, 00);
        ce1.setEndDate(calendar);
        ce1.setEndDateVerbatim("21 Apr 1994, 1:03 PM");   
        ce1.setMethod("Picked");
        
        AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", discipline, null);//meg added cod
        
        //startTx();
        persist(cevAttrDef);
        //commitTx();
        
        CollectingEventAttr cevAttr    = createCollectingEventAttr(ce1, cevAttrDef, "Sleepy Hollow", null);

        Collector collectorMeg = createCollector(agents.get(2), 1);
        Collector collectorRod = createCollector(agents.get(3), 2);
        CollectingEvent ce2 = createCollectingEvent(farmpond, new Collector[]{collectorMeg, collectorRod});
        calendar.set(1994, 4, 22, 06, 12, 00);
        ce2.setStartDate(calendar);
        ce2.setStartDateVerbatim("22 Apr 1994, 6:12 AM");
        calendar.set(1994, 4, 22, 07, 31, 00);
        ce2.setEndDate(calendar);
        ce2.setEndDateVerbatim("22 Apr 1994, 7:31 AM");
        ce2.setMethod("Picked");

        //CollectingTrip trip = createCollectingTrip("Sample collecting trip", new CollectingEvent[]{ce1,ce2});
        
        //dataObjects.add(trip);
        dataObjects.add(ce1);
        dataObjects.add(cevAttr);
        dataObjects.add(ce2);
        dataObjects.add(collectorMitch);
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
        Permit permit = createPermit("1991-PLAN-0001", "US Dept Botany", issuedDate, startDate, endDate, null);
        permit.setIssuedTo(ku);
        permit.setIssuedBy(agents.get(4));
        dataObjects.add(permit);
        
        log.info("Creating a repository agreement");
        RepositoryAgreement repoAg = new RepositoryAgreement();
        repoAg.initialize();
        repoAg.setDivision(division);
        repoAg.setNumber("KU-1990-01");
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
        current    = createDeterminationStatus(discipline, "Current",    "", DeterminationStatus.CURRENT);
        notCurrent = createDeterminationStatus(discipline, "Not current","", DeterminationStatus.NOTCURRENT);
        incorrect  = createDeterminationStatus(discipline, "Incorrect",  "", DeterminationStatus.USERDEFINED);

        
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
            int year = 1980 + (int)(rand.nextDouble() * 20.0);
            catDates[i].set(year, 01, 12 + i);
        }
        
        String prefix = "000000";
        int catNo = 100;
        CollectingEvent[] colEves = new CollectingEvent[8];
        for (int i=0;i<colEves.length;i++)
        {
            colEves[i] = createFakeCollectingEvent(agents, farmpond);
            collObjs.add(createCollectionObject(prefix + Integer.toString(catNo), "RSC"+Integer.toString(catNo), agents.get(i), col,  1, colEves[i], catDates[i], "BuildSampleDatabase"));
            catNo++;
        }
        dataObjects.addAll(collObjs);
        
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
        recent.set(2005, 10, 27, 13, 44, 00);
        Calendar longAgo = Calendar.getInstance();
        longAgo.set(1976, 01, 29, 8, 12, 00);
        Calendar whileBack = Calendar.getInstance(); 
        whileBack.set(2000, 7, 4, 9, 33, 12);
        
        determs.add(createDetermination(collObjs.get(0), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), current, recent));
        determs.add(createDetermination(collObjs.get(1), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), current, recent));
        determs.add(createDetermination(collObjs.get(2), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), current, recent));
        determs.add(createDetermination(collObjs.get(3), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), current, recent));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), current, recent));
        determs.add(createDetermination(collObjs.get(5), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), current, recent));
        determs.add(createDetermination(collObjs.get(6), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), current, recent));
        determs.add(createDetermination(collObjs.get(7), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), current, recent));
        
        determs.add(createDetermination(collObjs.get(0), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), notCurrent, longAgo));
        determs.add(createDetermination(collObjs.get(1), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(2), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(3), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), incorrect, longAgo));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), incorrect, longAgo));
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
        PrepType pressed = createPrepType(collection, "Pressed");

        List<Preparation> preps = new Vector<Preparation>();
        Calendar prepDate = Calendar.getInstance();
        preps.add(createPreparation(pressed, agents.get(0), collObjs.get(0), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(0), collObjs.get(1), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(2), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(3), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(2), collObjs.get(4), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(2), collObjs.get(5), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(3), collObjs.get(6), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(3), collObjs.get(7), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(0), (Storage)locs.get(11), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(1), (Storage)locs.get(11), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(2), (Storage)locs.get(10), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(2), collObjs.get(3), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(3), collObjs.get(4), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(0), collObjs.get(5), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(6), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(7), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(2), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));

        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(0), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(1), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(2), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(3), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pressed, agents.get(1), collObjs.get(4), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));

        dataObjects.add(pressed);
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
        Accession acc1 = createAccession(division,
                                         "Gift", "Complete", "2000-PL-001", 
                                         DateFormat.getInstance().format(calendar.getTime()), 
                                         calendar, calendar);
        acc1.setText1("Ichthyology");
        acc1.setRepositoryAgreement(repoAg);
        
        Agent donor =    agents.get(4);
        Agent receiver = agents.get(1);
        Agent reviewer = agents.get(2);
        
        List<AccessionAgent> accAgents = new Vector<AccessionAgent>();
        
        accAgents.add(createAccessionAgent("Donor", donor, acc1, null));
        accAgents.add(createAccessionAgent("Receiver", receiver, acc1, null));
        accAgents.add(createAccessionAgent("Reviewer", reviewer, acc1, null));

        Accession acc2 = createAccession(division,
                "Field Work", "In Process", "2004-PL-002", 
                DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        
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
        loanDate1.set(2005, 03, 19);
        
        Calendar currentDueDate1 = Calendar.getInstance();
        currentDueDate1.set(2005, 9, 19);
        
        Calendar originalDueDate1 = currentDueDate1;
        Calendar dateClosed1 = Calendar.getInstance();
        dateClosed1.set(2005, 7, 4);
      
        List<LoanPreparation>         loanPhysObjs = new Vector<LoanPreparation>();
        Vector<LoanReturnPreparation> returns      = new Vector<LoanReturnPreparation>();
        
        Loan closedLoan = createLoan("2007-001", loanDate1, currentDueDate1, originalDueDate1, 
                                     dateClosed1, Loan.CLOSED, null);
        for (int i = 0; i < 7; ++i)
        {
            Preparation p = getObjectByClass(preps, Preparation.class, rand.nextInt(preps.size()));
            int available = p.getLoanAvailable();
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
                                      null, Loan.OPEN, null);
        for (int i = 0; i < 5; ++i)
        {
            Preparation p = getObjectByClass(preps, Preparation.class, rand.nextInt(preps.size()));
            int available = p.getLoanAvailable();
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
                                      null, Loan.OPEN, null);
        Vector<LoanPreparation> newLoanLPOs = new Vector<LoanPreparation>();
        int lpoCountInNewLoan = 0;
        // put some LPOs in this loan that are from CollObjs that have other preps loaned out already
        // this algorithm (because of the randomness) can result in this loan having 0 LPOs.
        for( LoanPreparation lpo: loanPhysObjs)
        {
            int available = lpo.getPreparation().getLoanAvailable();
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
        
        LoanAgent loanAgent1 = createLoanAgent("loaner", closedLoan,    getRandomAgent(agents));
        LoanAgent loanAgent2 = createLoanAgent("loaner", overdueLoan,   getRandomAgent(agents));
        LoanAgent loanAgent3 = createLoanAgent("Borrower", closedLoan,  getRandomAgent(agents));
        LoanAgent loanAgent4 = createLoanAgent("Borrower", overdueLoan, getRandomAgent(agents));
        
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

        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        
        frame.setProcess(++createStep);
               
        commitTx();
        
        frame.setProcess(++createStep);
        

        // done
        log.info("Done creating Botany disciplineType database: " + disciplineType.getTitle());
        return dataObjects;
    }
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public void createFishCollection(final DisciplineType disciplineType,
                                     final Institution    institution,
                                     final SpecifyUser    user,
                                     final UserGroup      userGroup)
    {
        frame.setDesc("Creating Fish Collection Overhead...");
        
        startTx();
        DataType dataType = createDataType(disciplineType.getTitle());
        persist(dataType);

        Division division = createDivision(institution, disciplineType.getName(), "Icthyology", "IT", "Icthyology");
        persist(division);
        
        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        StorageTreeDef            locTreeDef        = createStorageTreeDef("Storage");
        
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        
        Discipline discipline = createDiscipline(division, disciplineType.getTitle(), disciplineType.getName(), dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, locTreeDef, lithoStratTreeDef);
        Discipline.setCurrentDiscipline(discipline);
        
        persist(discipline);
        commitTx();
        
        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           title            = initPrefs.getProperty("useragent.title",    "Mr.");
        String           firstName        = initPrefs.getProperty("useragent.firstname", "Rod");
        String           lastName         = initPrefs.getProperty("useragent.lastname", "Spears");
        String           midInit          = initPrefs.getProperty("useragent.midinit", "C");
        String           abbrev           = initPrefs.getProperty("useragent.abbrev", "rs");
        String           email            = initPrefs.getProperty("useragent.email", "rods@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", "CollectionManager");
        
        System.out.println("----- User Agent -----");
        System.out.println("Title:     "+title);
        System.out.println("FirstName: "+firstName);
        System.out.println("LastName:  "+lastName);
        System.out.println("MidInit:   "+midInit);
        System.out.println("Abbrev:    "+abbrev);
        System.out.println("Email:     "+email);
        System.out.println("UserType:  "+userType);
        Agent userAgent = createAgent(title, firstName, midInit, lastName, abbrev, email);
        userAgent.setDivision(division);
        
        SpecifyUser user2 = createSpecifyUser("tester", "tester@brc.ku.edu", (short) 0, userGroup, user.getUserType());
        startTx();
        persist(user2);
        commitTx();
        
        startTx();
        Agent userAgent2 = createAgent("Tester", "Test", "", "Tester", "", "tester@brc.ku.edu");
        userAgent2.setDivision(division);
        

        discipline.addReference(userAgent, "agents");
        
        user.addReference(userAgent, "agents");
        user2.addReference(userAgent2, "agents");
        
        persist(discipline);
        persist(userAgent);
        persist(user);
        persist(user2);

        persist(userAgent);
        persist(userAgent2);
        
        loadSchemaLocalization(discipline, SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());
        
        commitTx();
        
        frame.setDesc("Creating Fish Trees...");
        
        ////////////////////////////////
        // build the tree def items and nodes
        ////////////////////////////////
        Journal      journal     = createJournalsAndReferenceWork();
        List<Object> taxa        = createSimpleFishTaxonTree(taxonTreeDef, doShallowTaxonTree);
        List<Object> geos        = createSimpleGeography(geoTreeDef);
        List<Object> locs        = createSimpleStorage(locTreeDef);
        List<Object> gtps        = createSimpleGeologicTimePeriod(gtpTreeDef);
        List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef);
        
        startTx();
        
        BldrPickList colMethods = createPickLists();
        
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(locs);
        persist(gtps);
        persist(lithoStrats);
        commitTx(); 
        
        //frame.setProcess(++createStep);
        frame.setOverall(steps++);
        
        createFishCollection(discipline, user, userAgent, division,
                taxonTreeDef, geoTreeDef, gtpTreeDef,
                lithoStratTreeDef, locTreeDef,
                journal, taxa, geos, locs, gtps, lithoStrats,
                colMethods,
                "KUFSH", "Fish", true, false);

        frame.setOverall(steps++);
        
        if (doExtraCollections)
        {
            createFishCollection(discipline, user, userAgent, division,
                    taxonTreeDef, geoTreeDef, gtpTreeDef,
                    lithoStratTreeDef, locTreeDef,
                    journal, taxa, geos, locs, gtps, lithoStrats,
                    colMethods,
                    "KUTIS", "Fish Tissue", false, true);
        }

    }
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    @SuppressWarnings("unchecked")
    public List<Object> createFishCollection(final Discipline            discipline,
                                             final SpecifyUser               user,
                                             final Agent                     userAgent,
                                             final Division                  division,                  
                                             final TaxonTreeDef              taxonTreeDef,
                                             final GeographyTreeDef          geoTreeDef,
                                             final GeologicTimePeriodTreeDef gtpTreeDef,
                                             final LithoStratTreeDef         lithoStratTreeDef,
                                             final StorageTreeDef           locTreeDef,
                                             final Journal                   journal,
                                             final List<Object>              taxa,
                                             final List<Object>              geos,
                                             final List<Object>              locs,
                                             final List<Object>              gtps,
                                             final List<Object>              lithoStrats,
                                             final BldrPickList              colMethods,
                                             final String                    colPrefix,
                                             final String                    colName,
                                             final boolean                   createAgents,
                                             final boolean                   doTissues)
    {
        int createStep = 0;
        frame.setProcess(0, 15);
        
        frame.setDesc("Creating Collection "+  colName);
        
        startTx();
        CatalogNumberingScheme cns = createCatalogNumberingScheme("CatalogNumber For " + colName, "", true);
        
        persist(cns);
        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        Collection collection = createCollection(colPrefix, colName, cns, discipline);
        persist(collection);
        
        Collection.setCurrentCollection(collection);

        commitTx();
        
        frame.setProcess(++createStep);
        
        startTx();
        
        //DBTableIdMgr schema = new DBTableIdMgr(false);
        //schema.initialize(new File(XMLHelper.getConfigDirPath("specify_datamodel.xml")));
        //loadSchemaLocalization(discipline, SpLocaleContainer, schema);
        
        SpecifyUser.setCurrentUser(user);
        
        user.addReference(userAgent, "agents");
        
        persist(user);

        
        frame.setProcess(++createStep);
        
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////
        log.info("Creating picklists");

        Vector<Object> dataObjects = new Vector<Object>();
        
        standardQueries(dataObjects, userAgent);
        persist(dataObjects);
        dataObjects.clear();
        
        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // localities
        ////////////////////////////////
        List<Locality> localities = new Vector<Locality>();
        
        
        String POINT = "Point";
        String LINE  = "Line";
        String RECT  = "Rectangle";
        
        log.info("Creating localities");
        Locality forestStream = createLocality("Unnamed forest stream pond", (Geography)geos.get(12));
        localities.add(forestStream);
        forestStream.setLatLongType(POINT);
        forestStream.setOriginalLatLongUnit(0);
        forestStream.setLat1text("38.925467 deg N");
        forestStream.setLatitude1(new BigDecimal(38.925467));
        forestStream.setLong1text("94.984867 deg W");
        forestStream.setLongitude1(new BigDecimal(-94.984867));
        
        Locality lake   = createLocality("Deep, dark lake pond", (Geography)geos.get(17));
        localities.add(lake);
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
        
        Locality farmpond = createLocality("Shoal Creek at Schermerhorn Park, S of Galena at Rt. 26", (Geography)geos.get(11));
        localities.add(farmpond);
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
        List<Agent>    agents      = new Vector<Agent>();
        Agent johnByrn = null;
        Agent ku = new Agent();
        
        if (createAgents)
        {
            johnByrn = createAgent("Mr.", "John", "D", "Byrn", "jb", "jb@net.edu");
            agents.add(createAgent("Mr.", "David", "D", "Smith", "ds", "ds@whitehouse.gov"));
            agents.add(createAgent("Mr.", "Robert", "H", "Burk", "rb", "beach@net.edu"));
            agents.add(createAgent("Mrs.", "Margaret", "H", "Johnson", "jm", "jm@net.edu"));
            agents.add(createAgent("Mr.", "Kip", "C", "Spencer", "kcs", "rods@ku.edu"));
            agents.add(johnByrn);
            agents.add(createAgent("Sir", "Dudley", "X", "Thompson", "dxt", ""));
            agents.add(createAgent("Mr.", "Joe", "A", "Campbell", "jb", ""));
            agents.add(createAgent("Mr.", "Joe", "A", "Tester", "jb", ""));
            agents.add(createAgent("Mr.", "Mitch", "A", "Smyth", "mas", "mas@ku.edu"));
            agents.add(userAgent);
            
            ku.initialize();
            ku.setAbbreviation("KU");
            ku.setAgentType(Agent.ORG);
            ku.setLastName("University of Kansas");
            ku.setEmail("webadmin@ku.edu");
            ku.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
            ku.setDiscipline(Discipline.getCurrentDiscipline());
            
            agents.add(ku);
            agents.get(0).setOrganization(ku);
            agents.get(1).setOrganization(ku);
            agents.get(2).setOrganization(ku);
            agents.get(3).setOrganization(ku);
            agents.get(8).setOrganization(ku);
            
            Agent otherAgent = new Agent();
            otherAgent.initialize();
            otherAgent.setAbbreviation("O");
            otherAgent.setAgentType(Agent.OTHER);
            otherAgent.setLastName("The Other Guys");
            otherAgent.setEmail("other@other.com");
            otherAgent.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
            otherAgent.setDiscipline(Discipline.getCurrentDiscipline());
            agents.add(otherAgent);

            List<GroupPerson> gpList = new ArrayList<GroupPerson>();
            if (true)
            {
                startTx();
                Agent gm1 = createAgent("Mr.", "John", "A", "Lyon", "jal", "jal@group.edu");
                Agent gm2 = createAgent("Mr.", "Dave", "D", "Jones", "ddj", "ddj@group.edu");
                persist(gm1);
                persist(gm2);
                commitTx();
                
                Agent groupAgent = new Agent();
                groupAgent.initialize();
                groupAgent.setAbbreviation("GRP");
                groupAgent.setAgentType(Agent.GROUP);
                groupAgent.setLastName("The Group");
                groupAgent.setEmail("group@group.com");
                groupAgent.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
                groupAgent.setDiscipline(Discipline.getCurrentDiscipline());
                
                agents.add(groupAgent);
                
                gpList.add(createGroupPerson(groupAgent, gm1, 0));
                gpList.add(createGroupPerson(groupAgent, gm2, 1));
            }
            
            globalAgents.addAll(agents);
            
            List<AgentVariant> agentVariants = new Vector<AgentVariant>();
            agentVariants.add(createAgentVariant(AgentVariant.VARIANT, "John Variant #1", johnByrn));
            agentVariants.add(createAgentVariant(AgentVariant.VERNACULAR, "John VERNACULAR #1", johnByrn));
         
            List<Address> addrs = new Vector<Address>();
            addrs.add(createAddress(agents.get(1), "1600 Pennsylvania Avenue NW", null, "Washington", "DC", "USA", "20500"));
            addrs.add(createAddress(agents.get(1), "??? Mississippi", null, "Lawrence", "KS", "USA", "66045"));
            addrs.add(createAddress(agents.get(2), "1 Main St", "", "Lenexa", "KS", "USA", "66071"));
            addrs.add(createAddress(agents.get(3), "13355 Inverness", "Bldg #3", "Lawrence", "KS", "USA", "66047"));
            addrs.add(createAddress(agents.get(4), "Natural History Museum", "Cromwell Rd", "London", null, "UK", "SW7 5BD"));
            addrs.add(createAddress(agents.get(6), "1212 Apple Street", null, "Chicago", "IL", "USA", "01010"));
            addrs.add(createAddress(agents.get(8), "11911 Oak Ln", null, "Orion", "KS", "USA", "66061"));
            addrs.add(createAddress(ku, null, null, "Lawrence", "KS", "USA", "66045"));
            addrs.add(createAddress(userAgent, "1214 East Street", null, "Grinnell", "IA", "USA", "56060"));
            
            persist(agents);
            persist(agentVariants);
            persist(gpList);
            
        } else
        {
            agents.addAll(globalAgents);
            johnByrn = agents.get(4);
            ku       = agents.get(10);
        }
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // collecting events (collectors, collecting trip)
        ////////////////////////////////
        log.info("Creating collecting events, collectors and a collecting trip");
        Collector collectorMitch = createCollector(agents.get(8), 2);
        Collector collectorJim = createCollector(agents.get(1), 1);
        CollectingEvent ce1 = createCollectingEvent(forestStream, new Collector[]{collectorMitch,collectorJim});
        calendar.set(1993, 3, 19, 11, 56, 00);
        ce1.setStartDate(calendar);
        ce1.setStartDateVerbatim("19 Mar 1993, 11:56 AM");
        calendar.set(1993, 3, 19, 13, 03, 00);
        ce1.setEndDate(calendar);
        ce1.setEndDateVerbatim("19 Mar 1993, 1:03 PM");   
        ce1.setMethod(colMethods.getItem(1).getValue());
        
        AttributeDef        cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", discipline, null);//meg added cod
        
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
        ce2.setMethod(colMethods.getItem(2).getValue());

        CollectingTrip trip = createCollectingTrip("Sample collecting trip", new CollectingEvent[]{ce1,ce2});

        
        dataObjects.add(trip);
        dataObjects.add(ce1);
        dataObjects.add(cevAttr);
        dataObjects.add(ce2);
        dataObjects.add(collectorMitch);
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
        repoAg.setDivision(division);
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
        
        if (createAgents)
        {
            current    = createDeterminationStatus(discipline, "Current",    "", DeterminationStatus.CURRENT);
            notCurrent = createDeterminationStatus(discipline, "Not current","", DeterminationStatus.NOTCURRENT);
            incorrect  = createDeterminationStatus(discipline, "Incorrect",  "", DeterminationStatus.USERDEFINED);
            persist(current);
            persist(notCurrent);
            persist(incorrect);
        }

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
        
        AttributeDef colObjAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "MoonPhase", discipline, null);//meg added cod
        colObjAttrDef.setDiscipline(discipline);
        discipline.getAttributeDefs().add(colObjAttrDef);
        
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
        
        int baseInx = 41 - (doShallowTaxonTree ? 30 : 0);
        determs.add(createDetermination(collObjs.get(0), agents.get(0), (Taxon)taxa.get(baseInx+1), current, recent));
        determs.add(createDetermination(collObjs.get(1), agents.get(0), (Taxon)taxa.get(baseInx+2), current, recent));
        determs.add(createDetermination(collObjs.get(2), agents.get(0), (Taxon)taxa.get(baseInx+3), current, recent));
        determs.add(createDetermination(collObjs.get(3), agents.get(0), (Taxon)taxa.get(baseInx+4), current, recent));
        determs.add(createDetermination(collObjs.get(4), agents.get(0), (Taxon)taxa.get(baseInx+5), current, recent));
        determs.add(createDetermination(collObjs.get(5), agents.get(0), (Taxon)taxa.get(baseInx+6), current, recent));
        determs.add(createDetermination(collObjs.get(6), agents.get(3), (Taxon)taxa.get(baseInx+7), current, recent));
        determs.add(createDetermination(collObjs.get(7), agents.get(4), (Taxon)taxa.get(baseInx+8), current, recent));
        
        determs.add(createDetermination(collObjs.get(0), agents.get(0), (Taxon)taxa.get(baseInx), notCurrent, longAgo));
        determs.add(createDetermination(collObjs.get(1), agents.get(1), (Taxon)taxa.get(baseInx+7), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(2), agents.get(1), (Taxon)taxa.get(baseInx+9), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(3), agents.get(2), (Taxon)taxa.get(baseInx+10), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(4), agents.get(2), (Taxon)taxa.get(baseInx+10), notCurrent, whileBack));
        determs.add(createDetermination(collObjs.get(4), agents.get(3), (Taxon)taxa.get(baseInx+13), incorrect, longAgo));
        determs.add(createDetermination(collObjs.get(4), agents.get(4), (Taxon)taxa.get(baseInx+12), incorrect, longAgo));
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
        
        PrepType skel = null;
        PrepType cas  = null;
        PrepType etoh = null;
        PrepType xray = null;
        
        if (doTissues)
        {
            skel = createPrepType(collection, "Tissue");
            cas  = skel;
            etoh = skel;
            xray = skel;
            
        } else
        {
            skel = createPrepType(collection, "skeleton");
            cas  = createPrepType(collection, "C&S");
            etoh = createPrepType(collection, "EtOH");
            xray = createPrepType(collection, "x-ray");            
        }

        List<Preparation> preps = new Vector<Preparation>();
        Calendar prepDate = Calendar.getInstance();
        preps.add(createPreparation(etoh, agents.get(0), collObjs.get(0), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(0), collObjs.get(1), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(1), collObjs.get(2), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(1), collObjs.get(3), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(2), collObjs.get(4), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(2), collObjs.get(5), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(3), collObjs.get(6), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(etoh, agents.get(3), collObjs.get(7), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(0), (Storage)locs.get(11), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(1), (Storage)locs.get(11), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(1), collObjs.get(2), (Storage)locs.get(10), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(2), collObjs.get(3), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(3), collObjs.get(4), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(skel, agents.get(0), collObjs.get(5), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(6), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(7), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(cas, agents.get(1), collObjs.get(2), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));

        preps.add(createPreparation(xray, agents.get(1), collObjs.get(0), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(1), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(2), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(3), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(xray, agents.get(1), collObjs.get(4), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));

        dataObjects.add(collection);
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
        int yr = 2000 + (int)(rand.nextDouble() * 7);
        Accession acc1 = createAccession(division,
                                         "Gift", "Complete", yr + "-IC-001", 
                                         DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        acc1.setText1("Ichthyology");
        acc1.setRepositoryAgreement(repoAg);
        
        Agent donor =    agents.get(4);
        Agent receiver = agents.get(1);
        Agent reviewer = agents.get(2);
        
        List<AccessionAgent> accAgents = new Vector<AccessionAgent>();
        
        accAgents.add(createAccessionAgent("Donor", donor, acc1, null));
        accAgents.add(createAccessionAgent("Receiver", receiver, acc1, null));
        accAgents.add(createAccessionAgent("Reviewer", reviewer, acc1, null));

        Accession acc2 = createAccession(division,
                "Field Work", "In Process", yr + "-IC-002", DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        
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
        
        //yr = 2000 + (int)(rand.nextDouble() * 7);
        Loan closedLoan = createLoan(yr + "-001", loanDate1, currentDueDate1, originalDueDate1, 
                                     dateClosed1, Loan.CLOSED, null);
        for (int i = 0; i < 7; ++i)
        {
            Preparation p = getObjectByClass(preps, Preparation.class, rand.nextInt(preps.size()));
            int available = p.getLoanAvailable();
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
        Loan overdueLoan = createLoan(yr + "-001", loanDate2, currentDueDate2, originalDueDate2,  
                                      null, Loan.OPEN, null);
        for (int i = 0; i < 5; ++i)
        {
            Preparation p = getObjectByClass(preps, Preparation.class, rand.nextInt(preps.size()));
            int available = p.getLoanAvailable();
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
        
        Loan loan3 = createLoan(yr + "-003", loanDate3, currentDueDate3, originalDueDate3,  
                                      null, Loan.OPEN, null);
        Vector<LoanPreparation> newLoanLPOs = new Vector<LoanPreparation>();
        int lpoCountInNewLoan = 0;
        // put some LPOs in this loan that are from CollObjs that have other preps loaned out already
        // this algorithm (because of the randomness) can result in this loan having 0 LPOs.
        for( LoanPreparation lpo: loanPhysObjs)
        {
            int available = lpo.getPreparation().getLoanAvailable();
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
        Shipment loan1Ship = createShipment(ship1Date, yr + "-001", "USPS", (short) 1, "1.25 kg", null, agents.get(0), agents.get(4), agents.get(0));
        
        Calendar ship2Date = Calendar.getInstance();
        ship2Date.set(2005, 11, 24);
        Shipment loan2Ship = createShipment(ship2Date, yr + "-002", "FedEx", (short) 2, "6.0 kg", null, agents.get(3), agents.get(4), agents.get(3));
        
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
            
            
            File f = new File(UIRegistry.getDefaultWorkingPath() +File.separator + "demo_files" + File.separator + "card" + i + (i == 2 ? ".png" : ".jpg"));
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

        startTx();
        persist(dataObjects);
        commitTx();
        
        dataObjects.clear();
        
        frame.setProcess(++createStep);
               
        ////////////////////////////////
        // attachments (attachment metadata)
        ////////////////////////////////
        
            log.info("Creating attachments and attachment metadata");
            try
            {
                String attachmentFilesLoc = UIRegistry.getDefaultWorkingPath() + File.separator + "demo_files" + File.separator;

//                String bigEyeFilePath = attachmentFilesLoc + "bigeye.jpg";
//                Attachment bigEye = createAttachment(bigEyeFilePath, "image/jpeg", 0);
//                bigEye.setLoan(closedLoan);
                
                String[] names  = {"Beach",     "Smyth",  "Spears",  "Kumin",   "Bentley"};
                String[] photos = {"beach.jpg", "rod.jpg", "rod.jpg", "meg.jpg", "andy.jpg"};
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
        
        if (true)
        {
            List<Taxon> taxa2 = HibernateUtil.getCurrentSession().createQuery("SELECT t FROM Taxon t WHERE t.name = 'Ammocrypta'").list();
            List<ReferenceWork> rwList = new Vector<ReferenceWork>();

            startTx();
            rwList.addAll(journal.getReferenceWorks());
            
            TaxonCitation taxonCitation = new TaxonCitation();
            taxonCitation.initialize();
            Taxon ammocrypta = (Taxon)taxa2.get(0);
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
        frame.setProcess(++createStep);

        return null;
    }
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public void createSingleDiscipline(final DisciplineType disciplineType)
    {
        System.out.println("Creating single disciplineType database: " + disciplineType.getTitle());
        
        int createStep = 0;
        
        frame.setProcess(0, 2);
        
        frame.setProcess(++createStep);

        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           username         = initPrefs.getProperty("initializer.username", "rods");
        String           email            = initPrefs.getProperty("useragent.email", "rods@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", "CollectionManager");
        
        System.out.println("----- User Agent -----");
        System.out.println("Userame:   "+username);
        
        Institution    institution    = createInstitution("Natural History Museum");
        
        UserGroup        userGroup        = createUserGroup(disciplineType.getTitle());
        
        
        startTx();
        persist(userGroup);
        persist(institution);
        
        SpecifyUser user = createSpecifyUser(username, email, (short) 0, userGroup, userType);
        persist(user);
        
        commitTx();
        
        frame.setProcess(++createStep);
        
        createFishCollection(DisciplineType.getByTitle("Fish"), institution, user, userGroup);
        
        frame.setOverall(steps++);
        
        if (doExtraCollections)
        {
            createSingleBotanyCollection(DisciplineType.getByTitle("Fish"), institution, user);
        }
        
        // done
        log.info("Done creating single disciplineType database: " + disciplineType.getTitle());
    }

    /**
     * @param def
     * @return
     */
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
                new String[] { "Cherokee", "Douglas", "Johnson", "Osage", "Sedgwick" }, county.getRankId());
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
        
        int i = 0;
        for (Object geo : newObjs)
        {
            if (geo instanceof Geography)
            {
                log.debug(i+" "+((Geography)geo).getName());
            }
            i++;
        }
        
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


    public static List<Object> createSimpleStorage(final StorageTreeDef locTreeDef)
    {
        log.info("createSimpleStorage " + locTreeDef.getName());

        List<Object> newObjs = new Vector<Object>();

        StorageTreeDefItem building = createStorageTreeDefItem(null, locTreeDef, "building", 0);
        building.setIsEnforced(true);
        StorageTreeDefItem room = createStorageTreeDefItem(building, locTreeDef, "room", 100);
        room.setIsInFullName(true);
        StorageTreeDefItem freezer = createStorageTreeDefItem(room, locTreeDef, "freezer", 200);
        freezer.setIsInFullName(true);
        StorageTreeDefItem shelf = createStorageTreeDefItem(freezer, locTreeDef, "shelf", 300);
        shelf.setIsInFullName(true);

        // Create the building
        Storage dyche        = createStorage(locTreeDef, null,         "Dyche Hall", building.getRankId());
        Storage rm606        = createStorage(locTreeDef, dyche,        "Room 606",   room.getRankId());
        Storage freezerA     = createStorage(locTreeDef, rm606,        "Freezer A",  freezer.getRankId());
        Storage shelf5       = createStorage(locTreeDef, freezerA,     "Shelf 5",    shelf.getRankId());
        Storage shelf4       = createStorage(locTreeDef, freezerA,     "Shelf 4",    shelf.getRankId());
        Storage shelf3       = createStorage(locTreeDef, freezerA,     "Shelf 3",    shelf.getRankId());
        Storage shelf2       = createStorage(locTreeDef, freezerA,     "Shelf 2",    shelf.getRankId());
        Storage shelf1       = createStorage(locTreeDef, freezerA,     "Shelf 1",    shelf.getRankId());

        Storage rm701        = createStorage(locTreeDef, dyche,        "Room 701",   room.getRankId());
        Storage freezerA_701 = createStorage(locTreeDef, rm701,        "Freezer A",  freezer.getRankId());
        Storage shelf1_701   = createStorage(locTreeDef, freezerA_701, "Shelf 1",    shelf.getRankId());
        
        Storage rm703        = createStorage(locTreeDef, dyche,        "Room 703",   room.getRankId());
        Storage freezerA_703 = createStorage(locTreeDef, rm703,        "Freezer A",  freezer.getRankId());
        Storage shelf1_703   = createStorage(locTreeDef, freezerA_703, "Shelf 1",    shelf.getRankId());
        Storage shelf2_703   = createStorage(locTreeDef, freezerA_703, "Shelf 2",    shelf.getRankId());
        Storage shelf3_703   = createStorage(locTreeDef, freezerA_703, "Shelf 3",    shelf.getRankId());
        
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


    /**
     * @param taxonTreeDef
     * @param doShallow
     * @return
     */
    public static List<Object> createSimpleFishTaxonTree(final TaxonTreeDef taxonTreeDef, 
                                                         final boolean      doShallow)
    {
        log.info("createSimpleFishTaxonTree " + taxonTreeDef.getName());

        Vector<Object> newObjs = new Vector<Object>();
        Set<TaxonTreeDefItem> newItems;
        if (doShallow)
        {
            Object[][] taxonItems = {
                { TaxonTreeDef.TAXONOMY_ROOT,   "Taxonomy Root", true,  false },
                { TaxonTreeDef.ORDER,           "Order",         true,  false },
                { TaxonTreeDef.SUBORDER,        "Suborder",      false, false },
                { TaxonTreeDef.FAMILY,          "Family",        false, false },
                { TaxonTreeDef.GENUS,           "Genus",         true,  true },
                { TaxonTreeDef.SPECIES,         "Species",       false, true },
                };
            newItems = TreeFactory.addStandardTaxonDefItems(taxonTreeDef, taxonItems);
            
        } else
        {
            newItems = TreeFactory.addStandardTaxonDefItems(taxonTreeDef);
        }
        
        newObjs.addAll(newItems);
        // 0 - 27
        
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


        Taxon life            = createTaxon(taxonTreeDef, null, "Life", TaxonTreeDef.TAXONOMY_ROOT);
        
        Taxon animalia        = null;
        Taxon chordata        = null;
        Taxon vertebrata      = null;
        Taxon osteichthyes    = null;
        Taxon actinopterygii  = null;
        Taxon neopterygii     = null;
        Taxon teleostei       = null;
        Taxon acanthopterygii = null;
        if (!doShallow)
        {
            animalia        = createTaxon(taxonTreeDef, life,            "Animalia",        TaxonTreeDef.KINGDOM);
            chordata        = createTaxon(taxonTreeDef, animalia,        "Chordata",        TaxonTreeDef.PHYLUM);
            vertebrata      = createTaxon(taxonTreeDef, chordata,        "Vertebrata",      TaxonTreeDef.SUBPHYLUM);
            osteichthyes    = createTaxon(taxonTreeDef, vertebrata,      "Osteichthyes",    TaxonTreeDef.SUPERCLASS);
            actinopterygii  = createTaxon(taxonTreeDef, osteichthyes,    "Actinopterygii",  TaxonTreeDef.CLASS);
            neopterygii     = createTaxon(taxonTreeDef, actinopterygii,  "Neopterygii",     TaxonTreeDef.SUBCLASS);
            teleostei       = createTaxon(taxonTreeDef, neopterygii,     "Teleostei",       TaxonTreeDef.INFRACLASS);
            acanthopterygii = createTaxon(taxonTreeDef, teleostei,       "Acanthopterygii", TaxonTreeDef.SUPERORDER);
        }
        
        Taxon perciformes     = createTaxon(taxonTreeDef, doShallow ? life : acanthopterygii, "Perciformes",     TaxonTreeDef.ORDER);
        Taxon percoidei       = createTaxon(taxonTreeDef, perciformes,     "Percoidei",       TaxonTreeDef.SUBORDER);
        Taxon percidae        = createTaxon(taxonTreeDef, percoidei,       "Percidae",        TaxonTreeDef.FAMILY);
        Taxon ammocrypta      = createTaxon(taxonTreeDef, percidae,        "Ammocrypta",      TaxonTreeDef.GENUS);
        ammocrypta.setCommonName("sand darters");
        
        newObjs.add(life);
        
        if (!doShallow)
        {
            newObjs.add(animalia);
            newObjs.add(chordata);
            newObjs.add(vertebrata);
            newObjs.add(osteichthyes);
            newObjs.add(actinopterygii);
            newObjs.add(neopterygii);
            newObjs.add(teleostei);
            newObjs.add(acanthopterygii);
        }
        newObjs.add(perciformes);
        newObjs.add(percoidei);
        newObjs.add(percidae);
        newObjs.add(ammocrypta);
        // 28 - 40

        String[] speciesNames = { "asprella", "beanii", "bifascia", "clara", "meridiana", "pellucida", "vivax" };
        String[] commonNames  = {"crystal darter", "naked sand darter", "Florida sand darter", "western sand darter", "southern sand darter", "eastern sand darter", "scaly sand darter"};
        List<Object> kids = createTaxonChildren(taxonTreeDef, ammocrypta, speciesNames, commonNames, TaxonTreeDef.SPECIES);
        // 41 - 47
        newObjs.addAll(kids);

        Taxon carangidae = createTaxon(taxonTreeDef, percoidei, "Carangidae", TaxonTreeDef.FAMILY);
        Taxon caranx     = createTaxon(taxonTreeDef, carangidae, "Caranx", TaxonTreeDef.GENUS);
        
        // 48
        newObjs.add(carangidae);
        // 49
        newObjs.add(caranx);

        String[] speciesNames2 = { "bartholomaei", "caballus", "caninus", "crysos", "dentex", "hippos", "latus"};
        String[] commonNames2  = {"yellow jack", "green jack", "Pacific crevalle jack", "blue runner", "white trevally", "crevalle jack", "horse-eye jack"};
        kids = createTaxonChildren(taxonTreeDef, caranx, speciesNames2, commonNames2, TaxonTreeDef.SPECIES);
        // 50 - 56
        newObjs.addAll(kids);
        
        int baseInx = 53 - (doShallow ? 30 : 0);

        // setup a couple of synonyms (and supporting nodes)
        Taxon fusus = createTaxon(taxonTreeDef, caranx, "fusus", TaxonTreeDef.SPECIES);
        fusus.setCommonName("Blue runner");
        Taxon crysos = (Taxon)newObjs.get(baseInx);
        fusus.setAcceptedTaxon(crysos);
        fusus.setIsAccepted(false);
        
        Taxon carangus = createTaxon(taxonTreeDef, caranx, "carangus", TaxonTreeDef.SPECIES);
        carangus.setCommonName("Blacktailed trevally");
        Taxon hippos = (Taxon)newObjs.get(baseInx+2);
        carangus.setAcceptedTaxon(hippos);
        carangus.setIsAccepted(false);
        
        Taxon etheostoma = createTaxon(taxonTreeDef, percidae, "Etheostoma", TaxonTreeDef.GENUS);
        etheostoma.setCommonName("smoothbelly darters");
        
        Taxon meridianum = createTaxon(taxonTreeDef, etheostoma, "meridianum", TaxonTreeDef.SPECIES);
        meridianum.setCommonName("southern sand darter");
        Taxon merdiana = (Taxon)newObjs.get(baseInx-8);
        merdiana.setAcceptedTaxon(meridianum);
        merdiana.setIsAccepted(false);
        
        Taxon pellucidum = createTaxon(taxonTreeDef, etheostoma, "pellucidum", TaxonTreeDef.SPECIES);
        pellucidum.setCommonName("eastern sand darter");
        Taxon pellucida = (Taxon)newObjs.get(baseInx-7);
        pellucidum.setAcceptedTaxon(pellucida);
        pellucidum.setIsAccepted(false);
        
        Taxon ethVivax = createTaxon(taxonTreeDef, etheostoma, "vivax", TaxonTreeDef.SPECIES);
        ethVivax.setCommonName("scaly sand darter");
        Taxon vivax = (Taxon)newObjs.get(baseInx-6);
        ethVivax.setAcceptedTaxon(vivax);
        ethVivax.setIsAccepted(false);
        
        Taxon beani = createTaxon(taxonTreeDef, ammocrypta, "beani", TaxonTreeDef.SPECIES);
        beani.setCommonName("naked sand darter");
        Taxon beanii = (Taxon)newObjs.get(baseInx-11);
        beani.setAcceptedTaxon(beanii);
        beani.setIsAccepted(false);
        
        Taxon crystallaria = createTaxon(taxonTreeDef, percidae, "Crystallaria", TaxonTreeDef.GENUS);
        crystallaria.setCommonName("crystal darters");
        
        Taxon crysAsprella = createTaxon(taxonTreeDef, crystallaria, "asprella", TaxonTreeDef.SPECIES);
        crysAsprella.setCommonName("crystal darter");
        Taxon asprella = (Taxon)newObjs.get(baseInx-12);
        asprella.setAcceptedTaxon(crysAsprella);
        asprella.setIsAccepted(false);

        newObjs.add(fusus);
        newObjs.add(carangus);
        newObjs.add(etheostoma);
        newObjs.add(meridianum);
        newObjs.add(pellucidum);
        newObjs.add(ethVivax);
        newObjs.add(beani);
        newObjs.add(crystallaria);
        newObjs.add(crysAsprella);
        // 57 - 65

        TreeHelper.fixFullnameForNodeAndDescendants(life);
        life.setNodeNumber(1);
        fixNodeNumbersFromRoot(life);
        
        return newObjs;
    }
    
    /**
     * @param taxonTreeDef
     * @return
     */
    public static List<Object> createSimpleBotanyTaxonTree(final TaxonTreeDef taxonTreeDef)
    {
        log.info("createSimpleBotanyTaxonTree " + taxonTreeDef.getName());

        Vector<Object> newObjs = new Vector<Object>();
        Set<TaxonTreeDefItem> newItems;
        Object[][] taxonItems = { { TaxonTreeDef.TAXONOMY_ROOT, "Taxonomy Root", true, false },
                { TaxonTreeDef.KINGDOM, "Kingdom",   true, false },
                { TaxonTreeDef.PHYLUM, "Division",   false, false },
                { TaxonTreeDef.CLASS, "Class",       false, false },
                { TaxonTreeDef.ORDER, "Order",       false, false },
                { TaxonTreeDef.FAMILY, "Family",     false, false },
                { TaxonTreeDef.GENUS, "Genus",       true,  true },
                { TaxonTreeDef.SPECIES, "Species",   false, true }, 
                { TaxonTreeDef.VARIETY, "Variety",   false, true }, 
                };
        newItems = TreeFactory.addStandardTaxonDefItems(taxonTreeDef, taxonItems);


        newObjs.addAll(newItems);
        // 0 - 27

        for (TaxonTreeDefItem item : newItems)
        {
            if (item.getRankId().equals(TaxonTreeDef.GENUS))
            {
                item.setFormatToken("%G");
            } else if (item.getRankId().equals(TaxonTreeDef.SPECIES))
            {
                item.setFormatToken("%S");
            } else if (item.getRankId().equals(TaxonTreeDef.SUBSPECIES))
            {
                item.setFormatToken("%SS");
            }
        }

        Taxon life = createTaxon(taxonTreeDef, null, "Life", TaxonTreeDef.TAXONOMY_ROOT);
        Taxon Plantae = createTaxon(taxonTreeDef, life, "Plantae", TaxonTreeDef.KINGDOM);
        Taxon Magnoliophyta = createTaxon(taxonTreeDef, Plantae, "Magnoliophyta", TaxonTreeDef.PHYLUM);
        Taxon Magnoliopsida = createTaxon(taxonTreeDef, Magnoliophyta, "Magnoliopsida", TaxonTreeDef.CLASS);
        Taxon Sapindales = createTaxon(taxonTreeDef, Magnoliopsida, "Sapindales", TaxonTreeDef.ORDER);
        Taxon Sapindaceae = createTaxon(taxonTreeDef, Sapindales, "Sapindaceae", TaxonTreeDef.FAMILY);
        
        Taxon Acer = createTaxon(taxonTreeDef, Sapindaceae, "Acer", TaxonTreeDef.GENUS);
        
        newObjs.add(life);
        newObjs.add(Plantae);
        newObjs.add(Magnoliophyta);
        newObjs.add(Magnoliopsida);
        newObjs.add(Sapindales);
        newObjs.add(Sapindaceae);
        newObjs.add(Acer);
        
        String[] speciesNames = {"saccharum", "platanoides", "circinatum", "palmatum"};
        String[] commonNames = {"Sugar Maple", "Norway Maple", "Vine Maple", "apanese Maple"};
        
        List<Object> kids = createTaxonChildren(taxonTreeDef, Acer, speciesNames, commonNames, TaxonTreeDef.SPECIES);
        newObjs.addAll(kids);

        Taxon Aesculus = createTaxon(taxonTreeDef, Sapindaceae, "Aesculus", TaxonTreeDef.GENUS);
        newObjs.add(Aesculus);

        String[] speciesNames2 = { "arguta",        "californica",        "chinensis",              "glabra"};
        String[] commonNames2  = { "Texas Buckeye", "California Buckeye", "Chinese Horse-chestnut", "Ohio Buckeye"};
        kids = createTaxonChildren(taxonTreeDef, Aesculus, speciesNames2, commonNames2, TaxonTreeDef.SPECIES);

        newObjs.addAll(kids);

        TreeHelper.fixFullnameForNodeAndDescendants(life);
        life.setNodeNumber(1);
        fixNodeNumbersFromRoot(life);

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
        int max = frame.getOrigMax();
        
        frame.setProcess(0, oList.size());
        int cnt = 0;
        for (Object o: oList)
        {
            frame.setProcess(++cnt);
            //System.out.println("* " + cnt + " " + o.getClass().getSimpleName());
            persist(o);
        }
        frame.setProcess(oList.size());
        frame.setOrigMax(max);
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
     * Makes sure the Derby Directory gets created to hold the database. 
     */
    protected void ensureDerbyDirectory(final String driver)
    {
        if (StringUtils.isNotEmpty(driver) && 
            StringUtils.contains(driver, "Derby") && 
            StringUtils.isNotEmpty(UIRegistry.getJavaDBPath()))
        {
            File derbyDir = new File(UIRegistry.getJavaDBPath());
            if (!derbyDir.exists())
            {
                if (!derbyDir.mkdirs())
                {
                    try
                    {
                        log.error("Couldn't create Derby Path["+derbyDir.getCanonicalPath()+"]");
                        
                    } catch (IOException ex)
                    {
                        log.error(ex);
                    }
                }
            }
        }
    }
    
    /**
     * Creates the dialog to find out what database and what database driver to use. 
     */
    protected void buildSetup(final String[] args)
    {
        boolean doEmptyBuild = false;
        String  derbyPath    = null;
        
        boolean wasJavaDBSet = false;
        if (args != null && args.length > 0)
        {
            for (String arg : args)
            {
                String[] pair = StringUtils.split(arg, "=");
                if (pair.length == 2)
                {
                    String option = pair[0];
                    String value  = pair[1];
                    
                    if (option.equals("-Dappdir"))
                    {
                        UIRegistry.setDefaultWorkingPath(value);
                        
                    } else if (option.equals("-Dappdatadir"))
                    {
                        UIRegistry.setBaseAppDataDir(value);
                        
                    } else if (option.equals("-Djavadbdir"))
                    {
                        UIRegistry.setJavaDBDir(value);
                        derbyPath = UIRegistry.getJavaDBPath();
                        wasJavaDBSet = true;
                    }
                }
            }
            
            if (args.length == 2 && !args[0].startsWith("-D") && !args[1].startsWith("-D"))
            {
                doEmptyBuild = args[0].equals("build_empty");
                derbyPath    = StringUtils.isNotEmpty(args[1]) ? args[1] : derbyPath;
                hideFrame    = true;
                log.debug("doEmptyBuild [ "+doEmptyBuild+" ]");
            }
        }
        
        UIRegistry.setAppName("Specify");
        
        if (!wasJavaDBSet)
        {
            UIRegistry.setJavaDBDir(derbyPath != null ? derbyPath : UIRegistry.getDefaultWorkingPath() + File.separator + "DerbyDatabases");
        }
        
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
            ensureDerbyDirectory(driverName);
            
            DisciplineType         disciplineType = DisciplineType.getDiscipline("fish");
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver(driverName);
            DBConfigInfo config = new DBConfigInfo(driverInfo, "localhost", "WorkBench", "guest", "guest", 
                                                   "guest", "guest", "guest@ku.edu", disciplineType, 
                                                   "Institution", "Division");
            buildEmptyDatabase(config);

        } else
        {
            setupDlg = new SetupDialog(databaseName, driverName);
             
            final SwingWorker worker = new SwingWorker()
            {
                @Override
                public Object construct()
                {
                    setupDlg.pack();
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
    protected void startBuild(final String     dbName, 
                              final String     driverName, 
                              final DisciplineType disciplineType,
                              final String     username, 
                              final String     password,
                              final boolean    doExtraCollectionsArg)
    {
        this.doExtraCollections = doExtraCollectionsArg;
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
                        DBConfigInfo       config     = new DBConfigInfo(driverInfo, "localhost", "mydata", username, password, "Rod", "Spears", "rods@ku.edu", disciplineType, "Institition", "Division");
                        buildEmptyDatabase(config);

                    } else
                    {
                        build(dbName, driverName, disciplineType, username, password);
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
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
                
                frame.setVisible(false);
                frame.dispose();
                System.exit(0); // I didn't used to have to do this.

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
    public boolean buildEmptyDatabase(final DBConfigInfo config)
    {
        System.setProperty(AppPreferences.factoryName,          "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
        System.setProperty("edu.ku.brc.dbsupport.DataProvider", "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
        
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(500,125));
        frame.setTitle("Building Specify Database");
        if (!hideFrame)
        {
            UIHelper.centerAndShow(frame);
        } else
        {
            System.out.println("Building Specify Database Username["+config.getUsername()+"]");
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
                frame.setDesc("Creating Database Schema for "+config.getDbName());
                frame.setOverall(steps++);
            }
        });
        
        DatabaseDriverInfo driverInfo = config.getDriverInfo();
        
        try
        {
            if (hideFrame) System.out.println("Creating schema");
            
            SpecifySchemaGenerator.generateSchema(driverInfo, config.getHostName(), config.getDbName(), config.getUsername(), config.getPassword());
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.getProcessProgress().setIndeterminate(true);
                    frame.getProcessProgress().setString("");
                    frame.setDesc("Logging into "+config.getDbName()+"....");
                    frame.setOverall(steps++);
                }
            });
            
            String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, config.getHostName(), config.getDbName());
            if (connStr == null)
            {
                connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, config.getHostName(), config.getDbName());
            }
            
            if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                    driverInfo.getDialectClassName(), 
                    config.getDbName(), 
                    connStr, 
                    config.getUsername(), 
                    config.getPassword()))
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
                    frame.setDesc("Creating database "+config.getDbName()+"....");
                    frame.setOverall(steps++);
                }
            });
            
            Thumbnailer thumb = new Thumbnailer();
            File thumbFile = XMLHelper.getConfigDir("thumbnail_generators.xml");
            thumb.registerThumbnailers(thumbFile);
            thumb.setQuality(.5f);
            thumb.setMaxHeight(128);
            thumb.setMaxWidth(128);

            File attLoc = UIRegistry.getAppDataSubDir("AttachmentStorage", true);
            FileUtils.cleanDirectory(attLoc);
            AttachmentManagerIface attachMgr = new FileStoreAttachmentManager(attLoc);
            AttachmentUtils.setAttachmentManager(attachMgr);
            AttachmentUtils.setThumbnailer(thumb);
            
            if (hideFrame) System.out.println("Creating Empty Database");
            
            List<Object> dataObjects = createEmptyDiscipline(config);
            

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.getProcessProgress().setIndeterminate(true);
                    frame.getProcessProgress().setString("");
                    frame.setDesc("Saving data into "+config.getDbName()+"....");
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
    
    protected void persistDataObjects(final List<?> dataObjects)
    {
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
        persist(dataObjects);
        commitTx();
    }
    
    /** 
     * Drops, Creates and Builds the Database.
     * 
     * @throws SQLException
     */
    protected void build(final String     dbName, 
                         final String     driverName, 
                         final DisciplineType disciplineType,
                         final String     username, 
                         final String     passwordStr) throws SQLException
    {
        boolean doingDerby = StringUtils.contains(driverName, "Derby");
        
        ensureDerbyDirectory(driverName);

        frame = new ProgressFrame("Building Sample DB");
        frame.setSize(new Dimension(500,125));
        UIHelper.centerAndShow(frame);
        frame.setProcessPercent(true);
        frame.setOverall(0, 9+(doingDerby ? 1 : 0));
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
        
        String userName     = initPrefs.getProperty("initializer.username", username);
        String password     = initPrefs.getProperty("initializer.password", passwordStr);
        String databaseHost = initPrefs.getProperty("initializer.host",     "localhost");
        
        frame.setTitle("Building -> Database: "+ dbName + " Driver: "+ driverName + " User: "+userName);

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
                    File thumbFile = XMLHelper.getConfigDir("thumbnail_generators.xml");
                    thumb.registerThumbnailers(thumbFile);
                    thumb.setQuality(.5f);
                    thumb.setMaxHeight(128);
                    thumb.setMaxWidth(128);

                    frame.setDesc("Cleaning Attachment Cache...");
                    frame.setOverall(steps++);
                    
                    File attLoc = UIRegistry.getAppDataSubDir("AttachmentStorage", true);
                    FileUtils.cleanDirectory(attLoc);
                    AttachmentManagerIface attachMgr = new FileStoreAttachmentManager(attLoc);
                    
                    AttachmentUtils.setAttachmentManager(attachMgr);
                    AttachmentUtils.setThumbnailer(thumb);
                    
                    // save it all to the DB
                    setSession(HibernateUtil.getCurrentSession());

                    createSingleDiscipline(disciplineType);

                    attachMgr.cleanup();
                    

                    frame.setDesc("Done Saving data...");
                    frame.setOverall(steps++);


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
            System.err.println("Couldn't find Init Prefs ["+initFile.getAbsolutePath()+"]");
            
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
        protected JCheckBox          extraCollectionsChk;
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
            
            //Vector<DisciplineType> disciplinesList = DisciplineType.getDisciplineList();
            disciplines     = new JComboBox(DisciplineType.getDisciplineList());
            disciplines.setSelectedItem(DisciplineType.getDiscipline("fish"));
            
            databaseNameTxt = new JTextField(databaseName);
            
            usernameTxtFld = new JTextField("rods");
            passwdTxtFld   = new JPasswordField("rods");
            
            extraCollectionsChk = new JCheckBox("Create Extra Collections");
            extraCollectionsChk.setSelected(true);
            
            PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,2px,p:g", "p,4px,p,4px,p,4px,p,4px,p,4px,p,10px,p"));
            CellConstraints cc         = new CellConstraints();
            builder.add(new JLabel("Username:", SwingConstants.RIGHT),      cc.xy(1,1));
            builder.add(usernameTxtFld,                                     cc.xy(3,1));
            builder.add(new JLabel("Password:", SwingConstants.RIGHT),      cc.xy(1,3));
            builder.add(passwdTxtFld,                                       cc.xy(3,3));
            builder.add(new JLabel("Database Name:", SwingConstants.RIGHT), cc.xy(1,5));
            builder.add(databaseNameTxt,                                    cc.xy(3,5));
            builder.add(new JLabel("DisciplineType Name:", SwingConstants.RIGHT), cc.xy(1,7));
            builder.add(disciplines,                                        cc.xy(3,7));
            builder.add(new JLabel("Driver:", SwingConstants.RIGHT),        cc.xy(1,9));
            builder.add(drivers,                                            cc.xy(3,9));
            //builder.add(new JLabel("Driver:", SwingConstants.RIGHT),        cc.xy(1,11));
            builder.add(extraCollectionsChk,                                cc.xy(3,11));
            
            final JButton okBtn     = new JButton("OK");
            final JButton cancelBtn = new JButton("Cancel");
            builder.add(ButtonBarFactory.buildOKCancelBar(okBtn, cancelBtn), cc.xywh(1,13,3,1));
            
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
                        startBuild(databaseName, 
                                   dbDriver.getName(), 
                                   (DisciplineType)disciplines.getSelectedItem(), 
                                   username, 
                                   password,
                                   extraCollectionsChk.isSelected());
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                } else
                {
                    System.exit(0);
                }
                wasClosed = true;
                setupDlg.dispose();
                setupDlg = null;
            }
        }
    }
    
    public static void loadLocalization(final SpLocaleContainerItem memoryItem, final SpLocaleContainerItem newItem)
    {
        newItem.setName(memoryItem.getName());
        newItem.setType(memoryItem.getType());
        newItem.setFormat(memoryItem.getFormat());
        newItem.setIsUIFormatter(memoryItem.getIsUIFormatter());
        
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
    
    public static void loadLocalization(final SpLocaleContainer memoryContainer, final SpLocaleContainer newContainer)
    {
        newContainer.setName(memoryContainer.getName());
        newContainer.setType(memoryContainer.getType());
        newContainer.setFormat(newContainer.getFormat());
        newContainer.setIsUIFormatter(newContainer.getIsUIFormatter());
        
        debugOn = false;//memoryContainer.getName().equals("collectionobject");
       
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
    public static void loadSchemaLocalization(final Discipline collTyp, final Byte schemaType, final DBTableIdMgr tableMgr)
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
            container.setDiscipline(collTyp);
        }
    }
    
    public static void main(final String[] args)
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
        
        if (args != null && args.length > 0)
        {
            System.out.println("BuildSampleDatabase ");
           
            BuildSampleDatabase builder = new BuildSampleDatabase();
            builder.buildSetup(args);
   
        } else
        {
            // Now check the System Properties
            String appDir = System.getProperty("appdir");
            if (StringUtils.isNotEmpty(appDir))
            {
                UIRegistry.setDefaultWorkingPath(appDir);
            }
            
            String appdatadir = System.getProperty("appdatadir");
            if (StringUtils.isNotEmpty(appdatadir))
            {
                UIRegistry.setBaseAppDataDir(appdatadir);
            }
            
            String javadbdir = System.getProperty("javadbdir");
            if (StringUtils.isNotEmpty(javadbdir))
            {
                UIRegistry.setJavaDBDir(javadbdir);
            }
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    
                    BuildSampleDatabase builder = new BuildSampleDatabase();
                    builder.buildSetup(null);
                }
            });
        }
    }

}