
/**
 * Copyright (C) 2006 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.utilapps;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.specify.config.init.DataBuilder.buildDarwinCoreSchema;
import static edu.ku.brc.specify.config.init.DataBuilder.createAccession;
import static edu.ku.brc.specify.config.init.DataBuilder.createAccessionAgent;
import static edu.ku.brc.specify.config.init.DataBuilder.createAddress;
import static edu.ku.brc.specify.config.init.DataBuilder.createAdminPrincipal;
import static edu.ku.brc.specify.config.init.DataBuilder.createAgent;
import static edu.ku.brc.specify.config.init.DataBuilder.createAgentVariant;
import static edu.ku.brc.specify.config.init.DataBuilder.createAttachment;
import static edu.ku.brc.specify.config.init.DataBuilder.createAttributeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createCatalogNumberingScheme;
import static edu.ku.brc.specify.config.init.DataBuilder.createCollectingEvent;
import static edu.ku.brc.specify.config.init.DataBuilder.createCollectingEventAttr;
import static edu.ku.brc.specify.config.init.DataBuilder.createCollectingTrip;
import static edu.ku.brc.specify.config.init.DataBuilder.createCollection;
import static edu.ku.brc.specify.config.init.DataBuilder.createCollectionObject;
import static edu.ku.brc.specify.config.init.DataBuilder.createCollectionObjectAttr;
import static edu.ku.brc.specify.config.init.DataBuilder.createCollectionRelType;
import static edu.ku.brc.specify.config.init.DataBuilder.createCollector;
import static edu.ku.brc.specify.config.init.DataBuilder.createDataType;
import static edu.ku.brc.specify.config.init.DataBuilder.createDetermination;
import static edu.ku.brc.specify.config.init.DataBuilder.createDeterminationStatus;
import static edu.ku.brc.specify.config.init.DataBuilder.createDiscipline;
import static edu.ku.brc.specify.config.init.DataBuilder.createDivision;
import static edu.ku.brc.specify.config.init.DataBuilder.createGeography;
import static edu.ku.brc.specify.config.init.DataBuilder.createGeographyChildren;
import static edu.ku.brc.specify.config.init.DataBuilder.createGeographyTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createGeographyTreeDefItem;
import static edu.ku.brc.specify.config.init.DataBuilder.createGeologicTimePeriod;
import static edu.ku.brc.specify.config.init.DataBuilder.createGeologicTimePeriodTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createGeologicTimePeriodTreeDefItem;
import static edu.ku.brc.specify.config.init.DataBuilder.createGroupPerson;
import static edu.ku.brc.specify.config.init.DataBuilder.createInstitution;
import static edu.ku.brc.specify.config.init.DataBuilder.createJournal;
import static edu.ku.brc.specify.config.init.DataBuilder.createLithoStratTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createLithoStratTreeDefItem;
import static edu.ku.brc.specify.config.init.DataBuilder.createLoan;
import static edu.ku.brc.specify.config.init.DataBuilder.createLoanAgent;
import static edu.ku.brc.specify.config.init.DataBuilder.createLoanPreparation;
import static edu.ku.brc.specify.config.init.DataBuilder.createLoanReturnPreparation;
import static edu.ku.brc.specify.config.init.DataBuilder.createLocality;
import static edu.ku.brc.specify.config.init.DataBuilder.createPermit;
import static edu.ku.brc.specify.config.init.DataBuilder.createPickList;
import static edu.ku.brc.specify.config.init.DataBuilder.createPrepType;
import static edu.ku.brc.specify.config.init.DataBuilder.createPreparation;
import static edu.ku.brc.specify.config.init.DataBuilder.createQuery;
import static edu.ku.brc.specify.config.init.DataBuilder.createQueryField;
import static edu.ku.brc.specify.config.init.DataBuilder.createReferenceWork;
import static edu.ku.brc.specify.config.init.DataBuilder.createShipment;
import static edu.ku.brc.specify.config.init.DataBuilder.createSpecifyUser;
import static edu.ku.brc.specify.config.init.DataBuilder.createStorage;
import static edu.ku.brc.specify.config.init.DataBuilder.createStorageTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createStorageTreeDefItem;
import static edu.ku.brc.specify.config.init.DataBuilder.createTaxon;
import static edu.ku.brc.specify.config.init.DataBuilder.createTaxonChildren;
import static edu.ku.brc.specify.config.init.DataBuilder.createTaxonTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createWorkbench;
import static edu.ku.brc.specify.config.init.DataBuilder.createWorkbenchDataItem;
import static edu.ku.brc.specify.config.init.DataBuilder.createWorkbenchMappingItem;
import static edu.ku.brc.specify.config.init.DataBuilder.createWorkbenchTemplate;
import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createPasswordField;
import static edu.ku.brc.ui.UIHelper.createTextField;

import java.awt.Color;
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
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
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
import org.dom4j.Element;
import org.hibernate.Session;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.init.BldrPickList;
import edu.ku.brc.specify.config.init.BldrPickListItem;
import edu.ku.brc.specify.config.init.DBConfigInfo;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.config.init.HiddenTableMgr;
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
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.ConservDescription;
import edu.ku.brc.specify.datamodel.ConservEvent;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
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
import edu.ku.brc.specify.datamodel.SpPrincipal;
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
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileStoreAttachmentManager;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * 
 * @code_status Beta
 * 
 * @author rods
 */
public class BuildSampleDatabase
{
    // XXX TODO SECURITY- make secure Specify Admin user and pwd
    public String embeddedSpecifyAppRootUser;
    public String embeddedSpecifyAppRootPwd;

    private static final Logger  log      = Logger.getLogger(BuildSampleDatabase.class);
    
    protected static boolean     debugOn  = false;
    protected static final int   TIME_THRESHOLD = 30;
    protected static Hashtable<String, Boolean> fieldsToHideHash     = new Hashtable<String, Boolean>();
    protected static String                     catalogNumberFmtName = null;

    protected Calendar           calendar = Calendar.getInstance();
    protected Session            session;
    
    protected int                steps = 0;   
    protected ProgressFrame      frame;
    protected Properties         initPrefs     = null;
    protected Properties         backstopPrefs = null;
    
    protected SetupDialog        setupDlg  = null;
    protected boolean            hideFrame = false;
    
    protected boolean            doAddQueries        = false;
    protected boolean            copyToUserDir       = true;
    protected boolean            doShallowTaxonTree  = false;
    protected List<CollectionChoice> selectedChoices = null;
    
    protected Random             rand = new Random(12345678L);
    
    protected Vector<Locality>    globalLocalities = new Vector<Locality>();
    protected Vector<Agent>       globalAgents = new Vector<Agent>();
    protected DeterminationStatus current      = null;
    protected DeterminationStatus notCurrent   = null;
    protected DeterminationStatus incorrect    = null;
    protected DeterminationStatus oldDet       = null;
    
    protected int                 stationFieldNumberCounter = 100;
    protected String              STATION_FIELD_FORMAT = "RS%03d";
    
    protected int                 NUM_LOCALTIES = 50000;
    protected int                 NUM_COLOBJS   = 50000;
    
    protected boolean             doHugeBotany = false;
    
    
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
    
    protected void standardQueries(final Vector<Object> dataObjects, 
                                   final Agent userAgent)
    {
        if (doAddQueries)
        {
            //Byte greaterThan = SpQueryField.OperatorType.GREATERTHAN.getOrdinal();
            //Byte lessThan    = SpQueryField.OperatorType.LESSTHAN.getOrdinal();
            //Byte equals      = SpQueryField.OperatorType.EQUALS.getOrdinal();
            Byte greq        = SpQueryField.OperatorType.GREATERTHANEQUALS.getOrdinal();
            Byte lteq        = SpQueryField.OperatorType.LESSTHANEQUALS.getOrdinal();
            
            //Byte none        = SpQueryField.SortType.NONE.getOrdinal();
            Byte asc         = SpQueryField.SortType.ASC.getOrdinal();
            //Byte desc        = SpQueryField.SortType.DESC.getOrdinal();
            
            SpQuery query = createQuery("CO "+AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionName(), "CollectionObject", 1, AppContextMgr.getInstance().getClassObject(SpecifyUser.class), userAgent);
            createQueryField(query, (short)0, "catalogNumber", "Catalog Number", false, greq, lteq, "102", "103", asc, true, "1", 1);
            query.setIsFavorite(true);
            dataObjects.add(query);
        }
    }
    
    /**
     * 
     */
    protected void adjustLocaleFromPrefs()
    {
        String language = AppPreferences.getLocalPrefs().get("locale.lang", null); //$NON-NLS-1$
        if (language != null)
        {
            String country  = AppPreferences.getLocalPrefs().get("locale.country", null); //$NON-NLS-1$
            String variant  = AppPreferences.getLocalPrefs().get("locale.var",     null); //$NON-NLS-1$
            
            Locale prefLocale = new Locale(language, country, variant);
            
            Locale.setDefault(prefLocale);
            UIRegistry.setResourceLocale(prefLocale);
        }
        
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
        
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
        
        List<SpPrincipal> groups = new ArrayList<SpPrincipal>();
        
        Institution    institution    = createInstitution(config.getInstName());
        Division       division       = createDivision(institution, config.getDiscipline().getName(), config.getDivName(), config.getDivAbbrev(), config.getDivTitle());

        DataBuilder.createStandardGroups(groups, institution);
        
        Agent            userAgent        = createAgent(config.getLastName(), config.getFirstName(), "", config.getLastName(), "", config.getEmail());

        //SpPrincipal admin = createAdminPrincipal("Administrator", institution);
        //groups.add(admin);
        
        
        institution.setTitle(config.getInstTitle());
        
        frame.setDesc("Creating Core information...");
        frame.setProcess(++createStep);

        startTx();
        persist(institution);
        persist(division);
        
        SpecifyUser      user             = createSpecifyUser(config.getUsername(), config.getEmail(),  config.getPassword(), config.getUserType());
        persist(user);
        
        SpPrincipal     userPrincipal = DataBuilder.createUserPrincipal(user);
        groups.add(userPrincipal);
        user.addUserToSpPrincipalGroup(userPrincipal);
        
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

        Discipline discipline = createDiscipline(division, 
                                                 config.getDiscipline().getName(), 
                                                 config.getDiscipline().getTitle(), 
                                                 dataType, 
                                                 taxonTreeDef, 
                                                 geoTreeDef, 
                                                 gtpTreeDef, 
                                                 locTreeDef, 
                                                 lithoStratTreeDef);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        
        DataBuilder.createStandardGroups(groups, discipline);
        
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

        DataBuilder.createStandardGroups(groups, collection);
        
        persist(collection);

        AppContextMgr.getInstance().setClassObject(Collection.class, collection);

        persist(groups);
        
        frame.setDesc("Commiting...");
        frame.setProcess(++createStep);
        commitTx();

        makeFieldVisible(null, discipline);
        makeFieldVisible(config.getDiscipline().getName(), discipline);

        startTx();
        
        //DBTableIdMgr schema = new DBTableIdMgr(false);
        //schema.initialize(new File(XMLHelper.getConfigDirPath("specify_datamodel.xml")));
        //loadSchemaLocalization(discipline, SpLocaleContainer, schema);
        //buildDarwinCoreSchema(discipline);

        AppContextMgr.getInstance().setClassObject(SpecifyUser.class, user);
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
        // Queries
        ////////////////////////////////

        frame.setDesc("Creating Std Queries...");
        frame.setProcess(++createStep);
        standardQueries(dataObjects, userAgent);
        
        persist(dataObjects);
        dataObjects.clear();
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////

        log.info("Creating picklists");
        frame.setDesc("Creating PickLists...");
        frame.setProcess(++createStep);
        
        createPickLists(session, null);
        createPickLists(session, discipline);
        
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
        oldDet     = createDeterminationStatus(discipline, "Old Determination","", DeterminationStatus.OLDDETERMINATION);
        
        frame.setDesc("Commiting...");
        frame.setProcess(++createStep);
        //startTx();
        persist(current);
        persist(notCurrent);
        persist(incorrect);
        persist(oldDet);
        commitTx();
        
        frame.setProcess(++createStep);
        
        buildDarwinCoreSchema(discipline);

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
        
        ConservDescription desc = new ConservDescription();
        desc.initialize();
        desc.setShortDesc("Short Description");
        //desc.addReference(divs.get(0), "division");
        
        desc.addReference(colObjs.get(0), "collectionObject");
        
        //desc.setCollectionObject(colObjs.get(0));
        //colObjs.get(0).getConservDescriptions().add(desc);
        
        ConservEvent conservEvent = new ConservEvent();
        conservEvent.initialize();
        conservEvent.setExamDate(Calendar.getInstance());
        
        conservEvent.addReference(agents.get(1), "examinedByAgent");
        conservEvent.addReference(agents.get(2), "treatedByAgent");
        
        desc.addReference(conservEvent, "events");
        
        startTx();
        persist(desc);
        commitTx();
        
    }
    
    /**
     * @param discipline
     * @return
     */
    public static BldrPickList createPickLists(final Session localSession,
                                               final Discipline discipline)
    {
        return createPickLists(localSession, discipline, false, AppContextMgr.getInstance().getClassObject(Collection.class));
    }
    
    /**
     * @param discipline
     * @return
     */
    public static BldrPickList createPickLists(final Session    localSession,
                                               final Discipline discipline, 
                                               final boolean    doCheck,
                                               final Collection collection)
    {
        
        Hashtable<String, Boolean> nameHash = doCheck ? new Hashtable<String, Boolean>() : null;
        
        if (doCheck)
        {
            for (PickList pl : collection.getPickLists())
            {
                nameHash.put(pl.getName(), true);
            }
        }
        
        BldrPickList colMethods = null;
        
        List<BldrPickList> pickLists = DataBuilder.getBldrPickLists(discipline != null ? discipline.getName() : "common");
        if (pickLists != null)
        {
            for (BldrPickList pl : pickLists)
            {
                if (doCheck && nameHash.get(pl.getName()) != null)
                {
                    continue;
                }
                
                PickList pickList = createPickList(pl.getName(), pl.getType(), pl.getTableName(),
                                                   pl.getFieldName(), pl.getFormatter(), pl.getReadOnly(), 
                                                   pl.getSizeLimit(), pl.getIsSystem());
                pickList.setCollection(collection);
                collection.getPickLists().add(pickList);
                
                for (BldrPickListItem item : pl.getItems())
                {
                    pickList.addItem(item.getTitle(), item.getValue());
                }
                
                if (localSession != null)
                {
                    localSession.saveOrUpdate(pickList);
                }
                
                if (pl.getName().equals("CollectingMethod"))
                {
                    colMethods = pl;
                }
            }
            if (localSession != null)
            {
                localSession.saveOrUpdate(collection);
            }
        } else
        {
            log.error("No PickList XML");
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
        
        String stationFieldNumber = String.format(STATION_FIELD_FORMAT, stationFieldNumberCounter++);
        calendar.set(year, mon, 22, 06, 12, 00);
        CollectingEvent ce2 = createCollectingEvent(farmpond, calendar, stationFieldNumber, new Collector[]{collectorMeg, collectorRod});

        ce2.setStartDateVerbatim("22 Apr "+year+", 6:12 AM");
        
        calendar.set(year, mon, 22, 07, 31, 00);
        ce2.setEndDate(calendar);
        ce2.setEndDateVerbatim("22 Apr "+year+", 7:31 AM");
        ce2.setMethod("Picked");
        return ce2;
    }
    
    /**
     * @param rank
     * @param taxa
     * @return
     */
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
    
    /**
     * @param agents
     * @return
     */
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
        StorageTreeDef            locTreeDef        = createStorageTreeDef("Storage");
        
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        
        Discipline discipline = createDiscipline(division, disciplineType.getName(), disciplineType.getTitle(), 
                                                             dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, 
                                                             locTreeDef, lithoStratTreeDef);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        
        List<SpPrincipal> groups = new ArrayList<SpPrincipal>();
        
        
        DataBuilder.createStandardGroups(groups, discipline);
        
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
        //String           password         = initPrefs.getProperty("useragent.password", "rods");
        
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
        SpPrincipal disciplineGroup = DataBuilder.findGroup(groups, discipline, "Guest");
        user.addUserToSpPrincipalGroup(disciplineGroup);
        
        persist(userAgent);
        persist(user);

        // Tester
        Agent testerAgent = createAgent("Mr.", "Bob", "", "Botony", "", "botanyuser@ku.edu");
        testerAgent.setDivision(division);
        SpecifyUser testerUser          = createSpecifyUser("botanyuser", "botanyuser@ku.edu", /*(short) 0,*/ "botanyuser", disciplineGroup, "Guest");
        SpPrincipal testerUserPrincipal = DataBuilder.createUserPrincipal(testerUser);
        groups.add(testerUserPrincipal);
        testerUser.addUserToSpPrincipalGroup(testerUserPrincipal);
        discipline.addReference(testerAgent, "agents");
        testerUser.addReference(testerAgent, "agents");
        
        persist(discipline);
        persist(testerAgent);
        persist(testerUser);
        
        frame.setProcess(++createStep);
        
        CatalogNumberingScheme cns = createCatalogNumberingScheme("CatalogNumber For Plants", "", true);
        
        persist(cns);
        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        Collection collection = createCollection("KUBOT", "Botany", cns, discipline);
        persist(collection);
        
        DataBuilder.createStandardGroups(groups, collection);

        AppContextMgr.getInstance().setClassObject(Collection.class, collection);

        persist(groups);
        
        commitTx();
        
        makeFieldVisible(null, discipline);
        makeFieldVisible(disciplineType.getName(), discipline);

        frame.setProcess(++createStep);
        
        startTx();
        
        //DBTableIdMgr schema = new DBTableIdMgr(false);
        //schema.initialize(new File(XMLHelper.getConfigDirPath("specify_datamodel.xml")));
        //loadSchemaLocalization(discipline, SpLocaleContainer, schema);
        //buildDarwinCoreSchema(discipline);

        AppContextMgr.getInstance().setClassObject(SpecifyUser.class, user);
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
        
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(locs);
        persist(gtps);
        persist(lithoStrats);
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////

        log.info("Creating picklists");
        frame.setDesc("Creating PickLists...");
        //frame.setProcess(++createStep);
        
        createPickLists(session, null);
        createPickLists(session, discipline);
        
        Vector<Object> dataObjects = new Vector<Object>();
        
        standardQueries(dataObjects, userAgent);
        persist(dataObjects);
        dataObjects.clear();
        
        //BldrPickList colMethods = createPickLists();
        
        persist(dataObjects);
        dataObjects.clear();
        
        commitTx();
        
        startTx();
        
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

        persist(forestStream);
        persist(lake);
        persist(farmpond);
        
        commitTx();
        
        startTx();
        
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
        ku.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
        AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(ku);
        
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
        otherAgent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
        AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(otherAgent);
        agents.add(otherAgent);
        
        commitTx();

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
            groupAgent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
            AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(groupAgent);
            
            agents.add(groupAgent);
            
            gpList.add(createGroupPerson(groupAgent, gm1, 0));
            gpList.add(createGroupPerson(groupAgent, gm2, 1));
        }

        startTx();
        
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
                
        persist(agents);
        persist(agentVariants);
        persist(gpList);
        commitTx();
        
        startTx();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // collecting events (collectors, collecting trip)
        ////////////////////////////////
        log.info("Creating collecting events, collectors and a collecting trip");
        Collector collectorMitch = createCollector(agents.get(7), 2);
        Collector collectorJim = createCollector(agents.get(2), 1);
        
        calendar.set(1994, 4, 21, 11, 56, 00);
        String stationFieldNumber = String.format(STATION_FIELD_FORMAT, stationFieldNumberCounter++);
        CollectingEvent ce1 = createCollectingEvent(forestStream, calendar, stationFieldNumber, new Collector[]{collectorMitch, collectorJim});
        ce1.setStartDateVerbatim("21 Apr 1994, 11:56 AM");
        calendar.set(1994, 4, 21, 13, 03, 00);
        ce1.setEndDate(calendar);
        ce1.setEndDateVerbatim("21 Apr 1994, 1:03 PM");   
        ce1.setMethod("Picked");
        
        AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", discipline, null);//meg added cod
        
        persist(cevAttrDef);
        commitTx();
        
        startTx();
        
        CollectingEventAttr cevAttr    = createCollectingEventAttr(ce1, cevAttrDef, "Sleepy Hollow", null);

        Collector collectorMeg = createCollector(agents.get(2), 1);
        Collector collectorRod = createCollector(agents.get(3), 2);
        stationFieldNumber = String.format(STATION_FIELD_FORMAT, stationFieldNumberCounter++);
        calendar.set(1994, 4, 22, 06, 12, 00);
        CollectingEvent ce2 = createCollectingEvent(farmpond, calendar, stationFieldNumber, new Collector[]{collectorMeg, collectorRod});
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
        
        persist(dataObjects);
        dataObjects.clear();
        
        commitTx();
        
        startTx();
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
        Permit permit = createPermit("1991-PLAN-0001", "US Dept Wildlife", issuedDate, startDate, endDate, null);
        permit.setIssuedTo(ku);
        permit.setIssuedBy(agents.get(4));
        dataObjects.add(permit);
        
        log.info("Creating a repository agreement");
        RepositoryAgreement repoAg = new RepositoryAgreement();
        repoAg.initialize();
        repoAg.setDivision(division);
        repoAg.setRepositoryAgreementNumber("KU-1990-01");
        repoAg.setOriginator(ku);
        Calendar received = Calendar.getInstance();
        received.set(1992, 2, 10);
        repoAg.setDateReceived(received);
        Calendar repoEndDate = Calendar.getInstance();
        received.set(2010, 2, 9);
        repoAg.setEndDate(repoEndDate);
        dataObjects.add(repoAg);
        
        persist(dataObjects);
        dataObjects.clear();
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // Determination Status (Must be done here)
        ////////////////////////////////
        log.info("Creating determinations status");
        current    = createDeterminationStatus(discipline, "Current",    "", DeterminationStatus.CURRENT);
        notCurrent = createDeterminationStatus(discipline, "Not current","", DeterminationStatus.NOTCURRENT);
        incorrect  = createDeterminationStatus(discipline, "Incorrect",  "", DeterminationStatus.USERDEFINED);
        oldDet     = createDeterminationStatus(discipline, "Old Determination","", DeterminationStatus.OLDDETERMINATION);

        
        persist(current);
        persist(notCurrent);
        persist(incorrect);
        persist(oldDet);
        
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
        
        for (CollectingEvent ce : colEves)
        {
            persist(ce);
        }
        
        persist(dataObjects);
        dataObjects.clear();

        commitTx();
        
        startTx();
        
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
        
        persist(determs);
        dataObjects.clear();
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
                
        ////////////////////////////////
        // preparations (prep types)
        ////////////////////////////////
        log.info("Creating preparations");
        
        Vector<PrepType> prepTypesForSaving = loadPrepTypes();
        PrepType pressed = prepTypesForSaving.get(0);
        
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

        dataObjects.addAll(prepTypesForSaving);
        dataObjects.addAll(preps);
        
        persist(dataObjects);
        dataObjects.clear();
        
        commitTx();
        
        startTx();
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
        
        persist(dataObjects);
        dataObjects.clear();
        commitTx();
        
        startTx();
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
        
        LoanAgent loanAgent1 = createLoanAgent("loaner",   closedLoan,    getRandomAgent(agents));
        LoanAgent loanAgent2 = createLoanAgent("loaner",   overdueLoan,   getRandomAgent(agents));
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
        
        persist(dataObjects);
        dataObjects.clear();

        persist(dataObjects);
        dataObjects.clear();
        
        frame.setProcess(++createStep);
               
        commitTx();
        
        frame.setProcess(++createStep);
        
        buildDarwinCoreSchema(discipline);

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
    public List<Object> createHugeBotanyCollection(final DisciplineType disciplineType,
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
        StorageTreeDef            locTreeDef        = createStorageTreeDef("Storage");
        
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        
        Discipline discipline = createDiscipline(division, disciplineType.getName(), disciplineType.getTitle(), 
                                                             dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, 
                                                             locTreeDef, lithoStratTreeDef);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        
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
        Collection collection = createCollection("KUBOT", "Botany", cns, discipline);
        persist(collection);
        
        AppContextMgr.getInstance().setClassObject(Collection.class, collection);

        commitTx();
        
        makeFieldVisible(null, discipline);
        makeFieldVisible(disciplineType.getName(), discipline);

        frame.setProcess(++createStep);
        
        startTx();
        
        //DBTableIdMgr schema = new DBTableIdMgr(false);
        //schema.initialize(new File(XMLHelper.getConfigDirPath("specify_datamodel.xml")));
        //loadSchemaLocalization(discipline, SpLocaleContainer, schema);
        //buildDarwinCoreSchema(discipline);

        AppContextMgr.getInstance().setClassObject(SpecifyUser.class, user);
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
        
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(locs);
        persist(gtps);
        persist(lithoStrats);
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////

        log.info("Creating picklists");
        frame.setDesc("Creating PickLists...");
        //frame.setProcess(++createStep);
        
        createPickLists(session, null);
        createPickLists(session, discipline);
        
        Vector<Object> dataObjects = new Vector<Object>();
        
        standardQueries(dataObjects, userAgent);
        persist(dataObjects);
        dataObjects.clear();
        
        //BldrPickList colMethods = createPickLists();
        
        persist(dataObjects);
        dataObjects.clear();
        
        commitTx();
        
        startTx();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // localities
        ////////////////////////////////
        String POINT = "Point";
        
        frame.setDesc("Creating localities");
        log.info("Creating localities");
        frame.setProcess(0, NUM_LOCALTIES);
        Vector<Locality> localities = new Vector<Locality>();
        Vector<Object> evictList = new Vector<Object>();
        for (int i=0;i<NUM_LOCALTIES;i++)
        {
            Locality locality = createLocality("Unnamed forest stream pond", (Geography)geos.get(12));
            locality.setLatLongType(POINT);
            locality.setOriginalLatLongUnit(0);
            locality.setLat1text("38.925467 deg N");
            locality.setLatitude1(new BigDecimal(38.925467));
            locality.setLong1text("94.984867 deg W");
            locality.setLongitude1(new BigDecimal(-94.984867));
            persist(locality);
            
            evictList.add(locality);
            
            localities.add(locality);
            
            if ((i+1) % 10 == 0)
            {
                commitTx();
                for (Object obj : evictList)
                {
                    session.evict(obj);
                }
                evictList.clear();
                startTx(); 
                frame.setProcess(i);
            }
        }
        
        commitTx();
        
        startTx();
        
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
        ku.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
        AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(ku);
        
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
        otherAgent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
        AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(otherAgent);
        agents.add(otherAgent);
        
        commitTx();

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
            groupAgent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
            AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(groupAgent);
            
            agents.add(groupAgent);
            
            gpList.add(createGroupPerson(groupAgent, gm1, 0));
            gpList.add(createGroupPerson(groupAgent, gm2, 1));
        }

        startTx();
        
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
                
        persist(agents);
        persist(agentVariants);
        persist(gpList);
        commitTx();
        
        startTx();
        ////////////////////////////////
        // Determination Status (Must be done here)
        ////////////////////////////////
        log.info("Creating determinations status");
        current    = createDeterminationStatus(discipline, "Current",    "", DeterminationStatus.CURRENT);
        notCurrent = createDeterminationStatus(discipline, "Not current","", DeterminationStatus.NOTCURRENT);
        incorrect  = createDeterminationStatus(discipline, "Incorrect",  "", DeterminationStatus.USERDEFINED);
        oldDet     = createDeterminationStatus(discipline, "Old Determination","", DeterminationStatus.OLDDETERMINATION);
        
        persist(current);
        persist(notCurrent);
        persist(incorrect);
        persist(oldDet);
        
        persist(dataObjects);
        dataObjects.clear();
        
        commitTx();
        
        startTx();
        
        ////////////////////////////////
        // collection objects
        ////////////////////////////////
        log.info("Creating collection objects");

        Calendar[] catDates = new Calendar[300];
        for (int i=0;i<catDates.length;i++)
        {
            catDates[i] = Calendar.getInstance();
            int year = 1980 + (int)(rand.nextDouble() * 20.0);
            catDates[i].set(year, 01, 12 + i);
        }
        
        Vector<PrepType> prepTypesForSaving = loadPrepTypes();
        dataObjects.addAll(prepTypesForSaving);
        
        persist(dataObjects);
        dataObjects.clear();
        
        frame.setProcess(++createStep);
        
        commitTx();
        
        PrepType pressed = prepTypesForSaving.get(0);

        
        startTx();

        frame.setProcess(0, NUM_COLOBJS);
        
        evictList.clear();
        
        frame.setDesc("Creating Collection Objects");
        int catNo = 100;
        for (int i=0;i<NUM_COLOBJS;i++)
        {
            int years20 = (int)(rand.nextDouble() * 20.0);
            int years10 = (int)(rand.nextDouble() * 10.0);
            int years50 = (int)(rand.nextDouble() * 50.0);
            
            Calendar recent = Calendar.getInstance();
            recent.set(1950+years20+years10, 10, 27, 13, 44, 00);
            Calendar longAgo = Calendar.getInstance();
            longAgo.set(1900+years50, 01, 29, 8, 12, 00);
            Calendar whileBack = Calendar.getInstance(); 
            whileBack.set(1960+years20, 7, 4, 9, 33, 12);

            String catNumStr = String.format("%09d", catNo);
            
            int inx       = (int)(rand.nextDouble() * localities.size());
            int agentInx  = (int)(rand.nextDouble() * agents.size());
            int agentInx2 = (int)(rand.nextDouble() * agents.size());
            int calInx    = (int)(rand.nextDouble() * catDates.length);
            
            CollectingEvent  ce = createFakeCollectingEvent(agents, localities.get(inx));
            CollectionObject co = createCollectionObject(catNumStr, "RSC"+Integer.toString(catNo), agents.get(agentInx), collection,  1, ce, catDates[calInx], "BuildSampleDatabase");
            Determination    dt = createDetermination(co, getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), current, recent);
            
            Calendar prepDate = Calendar.getInstance();
            Preparation prep = createPreparation(pressed, agents.get(agentInx2), co, (Storage)locs.get(7), rand.nextInt(20)+1, prepDate);

            persist(ce);
            persist(co);
            persist(dt);
            persist(prep);
            
            evictList.add(ce);
            evictList.add(co);
            evictList.add(dt);
            evictList.add(prep);
            
            if ((i+1) % 10 == 0)
            {
                commitTx();
                for (Object obj : evictList)
                {
                    session.evict(obj);
                }
                evictList.clear();
                startTx();
                
                frame.setProcess(i);
            }
            catNo++;
        }

        commitTx();
        
        frame.setProcess(++createStep);
        
        buildDarwinCoreSchema(discipline);


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
    public List<Object> createSingleInvertPaleoCollection(final DisciplineType disciplineType,
                                                          final Institution    institution,
                                                          final SpecifyUser    user)
    {
        frame.setProcess(0, 16);
        frame.setDesc("Creating "+disciplineType.getTitle()+"...");
        
        int createStep = 0;
        
        startTx();

        DataType dataType = createDataType(disciplineType.getTitle());
        persist(dataType);
        
        Division division   = createDivision(institution, disciplineType.getName(), disciplineType.getTitle(), "INVP", disciplineType.getTitle());
        
        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        StorageTreeDef            locTreeDef        = createStorageTreeDef("Storage");
        
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        
        Discipline discipline = createDiscipline(division, disciplineType.getName(), disciplineType.getTitle(), 
                                                             dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, 
                                                             locTreeDef, lithoStratTreeDef);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        
        List<SpPrincipal> groups = new ArrayList<SpPrincipal>();

        //List<SpPrincipal> groups = new ArrayList<SpPrincipal>();
        DataBuilder.createStandardGroups(groups, discipline);
        SpPrincipal disciplineGroup = DataBuilder.findGroup(groups, discipline, "CollectionManager");
        //groups.add(disciplineGroup);   
        
        persist(division);
        persist(discipline);
        //persist(disciplineGroup);

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
        user.addUserToSpPrincipalGroup(disciplineGroup);
        
        // Tester
        Agent testerAgent = createAgent("Mr.", "Joe", "", "InvertPaleo", "", "InvertPaleo@ku.edu");
        testerAgent.setDivision(division);
        SpecifyUser testerUser          = createSpecifyUser("ivpuser", "InvertPaleo@ku.edu", /*(short) 0,*/ "ivpuser", disciplineGroup, "Guest");
        SpPrincipal testerUserPrincipal = DataBuilder.createUserPrincipal(testerUser);
        groups.add(testerUserPrincipal);
        testerUser.addUserToSpPrincipalGroup(testerUserPrincipal);
        discipline.addReference(testerAgent, "agents");
        testerUser.addReference(testerAgent, "agents");
        
        persist(testerAgent);
        persist(testerUser);
        persist(discipline);
        persist(userAgent);
        persist(user);
        
        LithoStratTreeDefItem earth     = createLithoStratTreeDefItem(lithoStratTreeDef, "Earth", 0, false);
        LithoStratTreeDefItem superGrp  = createLithoStratTreeDefItem(earth,     "Super Group", 100, false);
        LithoStratTreeDefItem lithoGrp  = createLithoStratTreeDefItem(superGrp,  "Litho Group", 200, false);
        LithoStratTreeDefItem formation = createLithoStratTreeDefItem(lithoGrp,  "Formation",   300, false);
        LithoStratTreeDefItem member    = createLithoStratTreeDefItem(formation, "Member",      400, false);
        @SuppressWarnings("unused")
        LithoStratTreeDefItem bed       = createLithoStratTreeDefItem(member,    "Bed",         500, true);
        
        frame.setProcess(++createStep);
        
        CatalogNumberingScheme cns = createCatalogNumberingScheme("CatNo "+disciplineType.getTitle(), "", true);
        
        persist(cns);
        persist(earth);
        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        Collection collection = createCollection("KUIVP", disciplineType.getTitle(), cns, discipline);
        persist(collection);
        
        DataBuilder.createStandardGroups(groups, collection);

        AppContextMgr.getInstance().setClassObject(Collection.class, collection);

        persist(groups);
        
        commitTx();
        
        makeFieldVisible(null, discipline);
        makeFieldVisible(disciplineType.getName(), discipline);

        frame.setProcess(++createStep);
        
        startTx();
        
        //DBTableIdMgr schema = new DBTableIdMgr(false);
        //schema.initialize(new File(XMLHelper.getConfigDirPath("specify_datamodel.xml")));
        //loadSchemaLocalization(discipline, SpLocaleContainer, schema);
        
        AppContextMgr.getInstance().setClassObject(SpecifyUser.class, user);
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
        //List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef);
        
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(locs);
        persist(gtps);
        //persist(lithoStrats);
        commitTx();
        
        
        LithoStrat earthLithoStrat = convertLithoStratFromCSV(lithoStratTreeDef);
        if (earthLithoStrat == null)
        {
            //throw new RuntimeException("No Tree");
            startTx();
            List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef);
            persist(lithoStrats);
            commitTx();
        }

        
        startTx();
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////

        log.info("Creating picklists");
        frame.setDesc("Creating Common PickLists...");
        //frame.setProcess(++createStep);
        
        createPickLists(session, null);
        
        frame.setDesc("Creating PickLists...");
        createPickLists(session, discipline);
        
        Vector<Object> dataObjects = new Vector<Object>();
        
        frame.setDesc("Creating Queries...");
        standardQueries(dataObjects, userAgent);
        persist(dataObjects);
        dataObjects.clear();
        
        //BldrPickList colMethods = createPickLists();
        
        persist(dataObjects);
        dataObjects.clear();
        
        frame.setDesc("Intermediate save....");
        commitTx();
        
        frame.setDesc("Creating Localities....");
        startTx();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // localities
        ////////////////////////////////
        String POINT = "Point";
        String LINE  = "Line";
        String RECT  = "Rectangle";
        
        log.info("Creating localities");
        Locality forestStream = createLocality("Gravel Pit", (Geography)geos.get(12));
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

        persist(forestStream);
        persist(lake);
        persist(farmpond);
        
        commitTx();
        
        startTx();
        
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
        ku.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
        AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(ku);
        
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
        otherAgent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
        AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(otherAgent);
        agents.add(otherAgent);
        
        commitTx();

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
            groupAgent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
            AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(groupAgent);
            
            agents.add(groupAgent);
            
            gpList.add(createGroupPerson(groupAgent, gm1, 0));
            gpList.add(createGroupPerson(groupAgent, gm2, 1));
        }

        startTx();
        
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
                
        persist(agents);
        persist(gpList);
        commitTx();
        
        startTx();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // collecting events (collectors, collecting trip)
        ////////////////////////////////
        log.info("Creating collecting events, collectors and a collecting trip");
        Collector collectorMitch = createCollector(agents.get(7), 2);
        Collector collectorJim = createCollector(agents.get(2), 1);
        
        String stationFieldNumber = String.format(STATION_FIELD_FORMAT, stationFieldNumberCounter++);
        calendar.set(1994, 4, 21, 11, 56, 00);
        CollectingEvent ce1 = createCollectingEvent(forestStream, calendar, stationFieldNumber, new Collector[]{collectorMitch, collectorJim});
        ce1.setStartDateVerbatim("21 Apr 1994, 11:56 AM");
        calendar.set(1994, 4, 21, 13, 03, 00);
        ce1.setEndDate(calendar);
        ce1.setEndDateVerbatim("21 Apr 1994, 1:03 PM");   
        ce1.setMethod("Picked");
        
        AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", discipline, null);//meg added cod
        
        persist(cevAttrDef);
        commitTx();
        
        startTx();
        
        CollectingEventAttr cevAttr    = createCollectingEventAttr(ce1, cevAttrDef, "Sleepy Hollow", null);

        Collector collectorMeg = createCollector(agents.get(2), 1);
        Collector collectorRod = createCollector(agents.get(3), 2);
        stationFieldNumber = String.format(STATION_FIELD_FORMAT, stationFieldNumberCounter++);
        calendar.set(1994, 4, 22, 06, 12, 00);
        CollectingEvent ce2 = createCollectingEvent(farmpond, calendar, stationFieldNumber, new Collector[]{collectorMeg, collectorRod});
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
        
        persist(dataObjects);
        dataObjects.clear();
        
        commitTx();
        
        startTx();
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
        Permit permit = createPermit("1980-INVRTP-0001", "US Dept Wildlife", issuedDate, startDate, endDate, null);
        permit.setIssuedTo(ku);
        permit.setIssuedBy(agents.get(4));
        dataObjects.add(permit);
        
        log.info("Creating a repository agreement");
        RepositoryAgreement repoAg = new RepositoryAgreement();
        repoAg.initialize();
        repoAg.setDivision(division);
        repoAg.setRepositoryAgreementNumber("KU-1979-01");
        repoAg.setOriginator(ku);
        Calendar received = Calendar.getInstance();
        received.set(1992, 2, 10);
        repoAg.setDateReceived(received);
        Calendar repoEndDate = Calendar.getInstance();
        received.set(2010, 2, 9);
        repoAg.setEndDate(repoEndDate);
        dataObjects.add(repoAg);
        
        persist(dataObjects);
        dataObjects.clear();
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // Determination Status (Must be done here)
        ////////////////////////////////
        log.info("Creating determinations status");
        current    = createDeterminationStatus(discipline, "Current",    "", DeterminationStatus.CURRENT);
        notCurrent = createDeterminationStatus(discipline, "Not current","", DeterminationStatus.NOTCURRENT);
        incorrect  = createDeterminationStatus(discipline, "Incorrect",  "", DeterminationStatus.USERDEFINED);
        oldDet     = createDeterminationStatus(discipline, "Old Determination","", DeterminationStatus.OLDDETERMINATION);

        
        persist(current);
        persist(notCurrent);
        persist(incorrect);
        persist(oldDet);
        
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
        for (CollectingEvent ce : colEves)
        {
            persist(ce);
        }
        
        persist(dataObjects);
        dataObjects.clear();

        commitTx();
        
        startTx();
        
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
        
        persist(determs);
        dataObjects.clear();
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
                
        ////////////////////////////////
        // preparations (prep types)
        ////////////////////////////////
        log.info("Creating preparations");
        
        Vector<PrepType> prepTypesForSaving = loadPrepTypes();
        PrepType pressed = prepTypesForSaving.get(0);
        
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

        dataObjects.addAll(prepTypesForSaving);
        dataObjects.addAll(preps);
        
        persist(dataObjects);
        dataObjects.clear();
        
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // accessions (accession agents)
        ////////////////////////////////
        log.info("Creating accessions and accession agents");
        calendar.set(2006, 10, 27, 23, 59, 59);
        Accession acc1 = createAccession(division,
                                         "Gift", "Complete", "2000-IP-001", 
                                         DateFormat.getInstance().format(calendar.getTime()), 
                                         calendar, calendar);
        acc1.setText1(disciplineType.getTitle());
        acc1.setRepositoryAgreement(repoAg);
        
        Agent donor =    agents.get(4);
        Agent receiver = agents.get(1);
        Agent reviewer = agents.get(2);
        
        List<AccessionAgent> accAgents = new Vector<AccessionAgent>();
        
        accAgents.add(createAccessionAgent("Donor", donor, acc1, null));
        accAgents.add(createAccessionAgent("Receiver", receiver, acc1, null));
        accAgents.add(createAccessionAgent("Reviewer", reviewer, acc1, null));

        Accession acc2 = createAccession(division,
                "Field Work", "In Process", "2004-IP-002", 
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
        
        persist(dataObjects);
        dataObjects.clear();
        commitTx();
        
        startTx();
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
        Loan overdueLoan = createLoan("2005-002", loanDate2, currentDueDate2, originalDueDate2,  
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
        
        Loan loan3 = createLoan("2005-003", loanDate3, currentDueDate3, originalDueDate3,  
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
        
        LoanAgent loanAgent1 = createLoanAgent("loaner",   closedLoan,    getRandomAgent(agents));
        LoanAgent loanAgent2 = createLoanAgent("loaner",   overdueLoan,   getRandomAgent(agents));
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
        Shipment loan1Ship = createShipment(ship1Date, "2005-001", "USPS", (short) 1, "10.25 kg", null, agents.get(0), agents.get(4), agents.get(0));
        
        Calendar ship2Date = Calendar.getInstance();
        ship2Date.set(2005, 11, 24);
        Shipment loan2Ship = createShipment(ship2Date, "2005-002", "FedEx", (short) 2, "60.0 kg", null, agents.get(3), agents.get(4), agents.get(3));
        
        //closedLoan.setShipment(loan1Ship);
        //overdueLoan.setShipment(loan2Ship);
        closedLoan.getShipments().add(loan1Ship);
        overdueLoan.getShipments().add(loan2Ship);
        dataObjects.add(loan1Ship);
        dataObjects.add(loan2Ship);   
        
        persist(dataObjects);
        dataObjects.clear();

        persist(dataObjects);
        dataObjects.clear();
        
        frame.setProcess(++createStep);
               
        commitTx();
        
        frame.setProcess(++createStep);
        
        buildDarwinCoreSchema(discipline);

        // done
        log.info("Done creating "+disciplineType.getTitle()+" disciplineType database: " + disciplineType.getTitle());
        return dataObjects;
    }
    
    /**
     * @param treeDef
     * @return
     */
    @SuppressWarnings("unchecked")
    public LithoStrat convertLithoStratFromCSV(final LithoStratTreeDef treeDef)
    {
        Hashtable<String, LithoStrat> lithoStratHash = new Hashtable<String, LithoStrat>();
        
        lithoStratHash.clear();

        File file = new File("demo_files/Stratigraphy.csv");
        if (!file.exists())
        {
            log.error("Couldn't file[" + file.getAbsolutePath() + "] checking the config dir");
            file = XMLHelper.getConfigDir("Stratigraphy.csv");
            if (!file.exists())
            {
                file = new File("Specify/demo_files/Stratigraphy.csv");
            }
        }
        

        if (file == null || !file.exists())
        {
            log.error("Couldn't file[" + file.getAbsolutePath() + "]");
            return null;
        }

        List<String> lines = null;
        try
        {
            lines = FileUtils.readLines(file);

        } catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
        
        startTx();

        // setup the root Geography record (planet Earth)
        LithoStrat earth = new LithoStrat();
        earth.initialize();
        earth.setName("Earth");
        earth.setRankId(0);
        earth.setDefinition(treeDef);
        LithoStratTreeDefItem defItem = treeDef.getDefItemByRank(0);
        earth.setDefinitionItem(defItem);
        
        persist(earth);

        frame.setDesc("Adding Stratigraphy Objects");
        frame.setProcess(0, lines.size());
        
        
        int counter = 0;
        // for each old record, convert the record
        for (String line : lines)
        {
            if (counter == 0)
            {
                counter = 1;
                continue; // skip header line
            }

            if (counter % 100 == 0)
            {
                frame.setProcess(counter);
                log.info("Converted " + counter + " Stratigraphy records");
            }

            String[] columns = StringUtils.splitPreserveAllTokens(line, ',');
            if (columns.length < 7)
            {
                log.error("Skipping[" + line + "]");
                continue;
            }

            // grab the important data fields from the old record
            String superGroup = columns[2];
            String lithoGroup = columns[3];
            String formation  = columns[4];
            String member     = columns[5];
            String bed        = columns[6];

            // create a new Litho Stratigraphy object from the old data
            @SuppressWarnings("unused")
            LithoStrat newStrat = convertOldStratRecord(superGroup, lithoGroup, formation, member, bed, earth);

            counter++;
        }

        frame.setProcess(counter);
        
        log.info("Converted " + counter + " Stratigraphy records");

        TreeHelper.fixFullnameForNodeAndDescendants(earth);
        earth.setNodeNumber(1);
        fixNodeNumbersFromRoot(earth);
        
        commitTx();
        
        /*startTx();
        TreeHelper.fixFullnameForNodeAndDescendants(earth);
        earth.setNodeNumber(1);
        fixNodeNumbersFromRoot(earth);
        
        printTree(earth, 0);
        saveTree(earth);

        commitTx();*/
        
        log.info("Converted " + counter + " Stratigraphy records");

        // set up Geography foreign key mapping for locality
        lithoStratHash.clear();

        return earth;
    }
    
    /**
     * @param root
     */
    @SuppressWarnings("unchecked")
    public void saveTree( Treeable root )
    {
        persist(root);
        
        for( Treeable child: (Set<Treeable>)root.getChildren() )
        {
            saveTree(child);
        }
    }
    
    /**
     * @param root
     * @param level
     */
    @SuppressWarnings("unchecked")
    public void printTree( Treeable root, int level)
    {
        for (int i=0;i<level;i++) System.out.print(" ");
        System.out.println(root.getName()+"  "+root.getNodeNumber()+"  "+root.getHighestChildNodeNumber());
        for( Treeable child: (Set<Treeable>)root.getChildren() )
        {
            printTree(child, level+2);
        }
    }
    
    /**
     * @param superGroup
     * @param lithoGroup
     * @param formation
     * @param member
     * @param bed
     * @param stratRoot
     * @param localSession
     * @return
     */
    protected LithoStrat convertOldStratRecord(String superGroup,
                                               String lithoGroup,
                                               String formation,
                                               String member,
                                               String bed,
                                               LithoStrat stratRoot)
    {
        String levelNames[] = { superGroup, lithoGroup, formation, member, bed };
        int levelsToBuild = 0;
        for (int i = levelNames.length; i > 0; --i)
        {
            if (StringUtils.isNotEmpty(levelNames[i - 1]))
            {
                levelsToBuild = i;
                break;
            }
        }

        for (int i = 0; i < levelsToBuild; i++)
        {
            if (StringUtils.isEmpty(levelNames[i]))
            {
                levelNames[i] = "(Empty)";
            }
        }

        LithoStrat prevLevelStrat = stratRoot;
        for (int i = 0; i < levelsToBuild; ++i)
        {
            LithoStrat newLevelStrat = buildLithoStratLevel(levelNames[i], prevLevelStrat);
            prevLevelStrat = newLevelStrat;
        }

        return prevLevelStrat;
    }

    /**
     * @param nameArg
     * @param parentArg
     * @return
     */
    protected LithoStrat buildLithoStratLevel(final String nameArg,
                                              final LithoStrat parentArg)
    {
        String name = nameArg;
        if (name == null)
        {
            name = "N/A";
        }

        // search through all of parent's children to see if one already exists with the same name
        Set<LithoStrat> children = parentArg.getChildren();
        for (LithoStrat child : children)
        {
            if (name.equalsIgnoreCase(child.getName()))
            {
                // this parent already has a child by the given name
                // don't create a new one, just return this one
                return child;
            }
        }

        // we didn't find a child by the given name
        // we need to create a new Geography record
        LithoStrat newStrat = new LithoStrat();
        newStrat.initialize();
        newStrat.setName(name);
        newStrat.setParent(parentArg);
        parentArg.addChild(newStrat);
        newStrat.setDefinition(parentArg.getDefinition());
        int newGeoRank = parentArg.getRankId() + 100;
        
        LithoStratTreeDefItem defItem = parentArg.getDefinition().getDefItemByRank(newGeoRank);
        newStrat.setDefinitionItem(defItem);
        newStrat.setRankId(newGeoRank);

        persist(newStrat);
        
        return newStrat;
    }

    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public List<Object> createGenericCollection(final DisciplineType disciplineType,
                                                final Institution    institution,
                                                final SpecifyUser    user)
    {
        frame.setProcess(0, 16);
        frame.setDesc("Creating "+disciplineType.getTitle()+"...");
        
        int createStep = 0;
        
        startTx();

        DataType dataType = createDataType(disciplineType.getTitle());
        persist(dataType);
        
        Division division   = createDivision(institution, disciplineType.getName(), disciplineType.getTitle(), disciplineType.getAbbrev(), disciplineType.getTitle());
        
        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        StorageTreeDef            locTreeDef        = createStorageTreeDef("Storage");
        
        Discipline discipline = createDiscipline(division, disciplineType.getName(), disciplineType.getTitle(), 
                                                             dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, 
                                                             locTreeDef, lithoStratTreeDef);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);

        List<SpPrincipal> groups = new ArrayList<SpPrincipal>();
        DataBuilder.createStandardGroups(groups, discipline);
        SpPrincipal disciplineGroup = DataBuilder.findGroup(groups, discipline, "CollectionManager");

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
        user.addUserToSpPrincipalGroup(disciplineGroup);
        
        // Tester
        String dspAbbrev = disciplineType.getAbbrev();
        Agent testerAgent = createAgent("", dspAbbrev, "", "Tester", "", dspAbbrev+"tester@brc.ku.edu");
        testerAgent.setDivision(division);
        SpecifyUser testerUser          = createSpecifyUser(dspAbbrev+"Tester", dspAbbrev+"tester@brc.ku.edu", dspAbbrev+"Tester", disciplineGroup, user.getUserType());
        SpPrincipal testerUserPrincipal = DataBuilder.createUserPrincipal(testerUser);
        groups.add(testerUserPrincipal);
        testerUser.addUserToSpPrincipalGroup(testerUserPrincipal);
        discipline.addReference(testerAgent, "agents");
        testerUser.addReference(testerAgent, "agents");

        
        persist(discipline);
        persist(userAgent);
        persist(user);
        
        persist(testerAgent);
        persist(testerUser);
        
        frame.setProcess(++createStep);
        
        CatalogNumberingScheme cns = createCatalogNumberingScheme("CatNo "+disciplineType.getTitle(), "", true);
        
        persist(cns);
        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        frame.setDesc("Creating a Collection");
        Collection collection = createCollection("KU", disciplineType.getTitle(), cns, discipline, disciplineType.isEmbeddedCollecingEvent());
        persist(collection);
        
        DataBuilder.createStandardGroups(groups, collection);

        AppContextMgr.getInstance().setClassObject(Collection.class, collection);

        persist(groups);

        commitTx();
        
        makeFieldVisible(null, discipline);
        makeFieldVisible(disciplineType.getName(), discipline);

        frame.setProcess(++createStep);
        
        startTx();
        
        createTaxonTreeDefFromXML(taxonTreeDef, disciplineType);
        persist(taxonTreeDef);
        
        //DBTableIdMgr schema = new DBTableIdMgr(false);
        //schema.initialize(new File(XMLHelper.getConfigDirPath("specify_datamodel.xml")));
        //loadSchemaLocalization(discipline, SpLocaleContainer, schema);
        //buildDarwinCoreSchema(discipline);

        AppContextMgr.getInstance().setClassObject(SpecifyUser.class, user);
        user.addReference(userAgent, "agents");
        
        persist(user);

        Journal journal = createJournalsAndReferenceWork();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // build the tree def items and nodes
        ////////////////////////////////
        Vector<Object> taxa = new Vector<Object>();
        createTaxonTreeFromXML(taxa, taxonTreeDef, disciplineType);
        
        boolean isPaleo = disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.paleobotany ||
                          disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.vertpaleo ||
                          disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.invertpaleo;
        
        if (isPaleo)
        {
            LithoStratTreeDefItem earth     = createLithoStratTreeDefItem(lithoStratTreeDef, "Earth", 0, false);
            LithoStratTreeDefItem superGrp  = createLithoStratTreeDefItem(earth,     "Super Group", 100, false);
            LithoStratTreeDefItem lithoGrp  = createLithoStratTreeDefItem(superGrp,  "Litho Group", 200, false);
            LithoStratTreeDefItem formation = createLithoStratTreeDefItem(lithoGrp,  "Formation",   300, false);
            LithoStratTreeDefItem member    = createLithoStratTreeDefItem(formation, "Member",      400, false);
            @SuppressWarnings("unused")
            LithoStratTreeDefItem bed       = createLithoStratTreeDefItem(member,    "Bed",         500, true);
            persist(earth);
        }
        
        List<Object> geos        = createSimpleGeography(geoTreeDef);
        List<Object> locs        = createSimpleStorage(locTreeDef);
        List<Object> gtps        = createSimpleGeologicTimePeriod(gtpTreeDef);
        List<Object> lithoStrats = isPaleo ? null : createSimpleLithoStrat(lithoStratTreeDef);
        
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(locs);
        persist(gtps);
        
        if (lithoStrats != null)
        {
            persist(lithoStrats);
            
        } else if (isPaleo)
        {
            LithoStrat earthLithoStrat = convertLithoStratFromCSV(lithoStratTreeDef);
            if (earthLithoStrat == null)
            {
                lithoStrats = createSimpleLithoStrat(lithoStratTreeDef);
                persist(lithoStrats);
            }  
        }
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////

        log.info("Creating picklists");
        frame.setDesc("Creating Common PickLists...");
        //frame.setProcess(++createStep);
        
        createPickLists(session, null);
        
        frame.setDesc("Creating PickLists...");
        createPickLists(session, discipline);
        
        Vector<Object> dataObjects = new Vector<Object>();
        
        frame.setDesc("Creating Queries...");
        standardQueries(dataObjects, userAgent);
        persist(dataObjects);
        dataObjects.clear();
        
        persist(dataObjects);
        dataObjects.clear();
        
        frame.setDesc("Intermediate save....");
        commitTx();
        
        frame.setDesc("Creating Localities....");
        startTx();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // localities
        ////////////////////////////////
        String POINT = "Point";
        String LINE  = "Line";
        String RECT  = "Rectangle";
        
        log.info("Creating localities");
        Locality forestStream = createLocality("Gravel Pit", (Geography)geos.get(12));
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

        persist(forestStream);
        persist(lake);
        persist(farmpond);
        
        commitTx();
        
        startTx();
        
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // agents and addresses
        ////////////////////////////////
        log.info("Creating agents and addresses");
        frame.setDesc("Creating agents and addresses");
        
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
        ku.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
        AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(ku);
        
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
        otherAgent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
        AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(otherAgent);
        agents.add(otherAgent);
        
        commitTx();

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
            groupAgent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
            AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(groupAgent);
            
            agents.add(groupAgent);
            
            gpList.add(createGroupPerson(groupAgent, gm1, 0));
            gpList.add(createGroupPerson(groupAgent, gm2, 1));
        }

        startTx();
        
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
                
        persist(agents);
        persist(gpList);
        commitTx();
        
        startTx();
        
        frame.setProcess(++createStep);
        
        //////////////////////////////////////////////////
        // collecting events (collectors, collecting trip)
        ///////////////////////////////////////////////////
        log.info("Creating collecting events, collectors and a collecting trip");
        Collector collectorMitch = createCollector(agents.get(7), 2);
        Collector collectorJim = createCollector(agents.get(2), 1);
        
        calendar.set(1994, 4, 21, 11, 56, 00);
        String stationFieldNumber = String.format(STATION_FIELD_FORMAT, stationFieldNumberCounter++);
        CollectingEvent ce1 = createCollectingEvent(forestStream, calendar, stationFieldNumber, new Collector[]{collectorMitch, collectorJim});
        ce1.setStartDateVerbatim("21 Apr 1994, 11:56 AM");
        calendar.set(1994, 4, 21, 13, 03, 00);
        ce1.setEndDate(calendar);
        ce1.setEndDateVerbatim("21 Apr 1994, 1:03 PM");   
        ce1.setMethod("Picked");
        
        AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", discipline, null);//meg added cod
        
        persist(cevAttrDef);
        commitTx();
        
        startTx();
        
        CollectingEventAttr cevAttr    = createCollectingEventAttr(ce1, cevAttrDef, "Sleepy Hollow", null);

        Collector collectorMeg = createCollector(agents.get(2), 1);
        Collector collectorRod = createCollector(agents.get(3), 2);
        calendar.set(1994, 4, 22, 06, 12, 00);
        stationFieldNumber = String.format(STATION_FIELD_FORMAT, stationFieldNumberCounter++);
        CollectingEvent ce2 = createCollectingEvent(farmpond, calendar, stationFieldNumber, new Collector[]{collectorMeg, collectorRod});
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
        
        persist(dataObjects);
        dataObjects.clear();
        
        commitTx();
        
        startTx();
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
        
        Permit permit = createPermit("1980-"+disciplineType.getAbbrev()+"-0001", "US Dept Wildlife", issuedDate, startDate, endDate, null);
        permit.setIssuedTo(ku);
        permit.setIssuedBy(agents.get(4));
        dataObjects.add(permit);
        
        log.info("Creating a repository agreement");
        RepositoryAgreement repoAg = new RepositoryAgreement();
        repoAg.initialize();
        repoAg.setDivision(division);
        repoAg.setRepositoryAgreementNumber("KU-"+disciplineType.getAbbrev()+"-01");
        repoAg.setOriginator(ku);
        Calendar received = Calendar.getInstance();
        received.set(1992, 2, 10);
        repoAg.setDateReceived(received);
        Calendar repoEndDate = Calendar.getInstance();
        received.set(2010, 2, 9);
        repoAg.setEndDate(repoEndDate);
        dataObjects.add(repoAg);
        
        persist(dataObjects);
        dataObjects.clear();
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // Determination Status (Must be done here)
        ////////////////////////////////
        log.info("Creating determinations status");
        current    = createDeterminationStatus(discipline, "Current",    "", DeterminationStatus.CURRENT);
        notCurrent = createDeterminationStatus(discipline, "Not current","", DeterminationStatus.NOTCURRENT);
        incorrect  = createDeterminationStatus(discipline, "Incorrect",  "", DeterminationStatus.USERDEFINED);
        oldDet     = createDeterminationStatus(discipline, "Old Determination","", DeterminationStatus.OLDDETERMINATION);

        
        persist(current);
        persist(notCurrent);
        persist(incorrect);
        persist(oldDet);
        
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
        for (CollectingEvent ce : colEves)
        {
            persist(ce);
        }
        
        persist(dataObjects);
        dataObjects.clear();

        commitTx();
        
        startTx();
        
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
        
        persist(determs);
        dataObjects.clear();
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
                
        ////////////////////////////////
        // preparations (prep types)
        ////////////////////////////////
        log.info("Creating preparations");
        
        Vector<PrepType> prepTypesForSaving = loadPrepTypes();
        PrepType pressed = prepTypesForSaving.get(0);
        
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

        dataObjects.addAll(prepTypesForSaving);
        dataObjects.addAll(preps);
        
        persist(dataObjects);
        dataObjects.clear();
        
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // accessions (accession agents)
        ////////////////////////////////
        log.info("Creating accessions and accession agents");
        calendar.set(2006, 10, 27, 23, 59, 59);
        Accession acc1 = createAccession(division,
                                         "Gift", "Complete", "2000-"+disciplineType.getAbbrev()+"-001", 
                                         DateFormat.getInstance().format(calendar.getTime()), 
                                         calendar, calendar);
        acc1.setText1(disciplineType.getTitle());
        acc1.setRepositoryAgreement(repoAg);
        
        Agent donor =    agents.get(4);
        Agent receiver = agents.get(1);
        Agent reviewer = agents.get(2);
        
        List<AccessionAgent> accAgents = new Vector<AccessionAgent>();
        
        accAgents.add(createAccessionAgent("Donor", donor, acc1, null));
        accAgents.add(createAccessionAgent("Receiver", receiver, acc1, null));
        accAgents.add(createAccessionAgent("Reviewer", reviewer, acc1, null));

        Accession acc2 = createAccession(division,
                "Field Work", "In Process", "2004-"+disciplineType.getAbbrev()+"-002", 
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
        
        persist(dataObjects);
        dataObjects.clear();
        commitTx();
        
        startTx();
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
        Loan overdueLoan = createLoan("2005-002", loanDate2, currentDueDate2, originalDueDate2,  
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
        
        Loan loan3 = createLoan("2005-003", loanDate3, currentDueDate3, originalDueDate3,  
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
        
        LoanAgent loanAgent1 = createLoanAgent("loaner",   closedLoan,    getRandomAgent(agents));
        LoanAgent loanAgent2 = createLoanAgent("loaner",   overdueLoan,   getRandomAgent(agents));
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
        Shipment loan1Ship = createShipment(ship1Date, "2005-001", "USPS", (short) 1, "10.25 kg", null, agents.get(0), agents.get(4), agents.get(0));
        
        Calendar ship2Date = Calendar.getInstance();
        ship2Date.set(2005, 11, 24);
        Shipment loan2Ship = createShipment(ship2Date, "2005-002", "FedEx", (short) 2, "60.0 kg", null, agents.get(3), agents.get(4), agents.get(3));
        
        //closedLoan.setShipment(loan1Ship);
        //overdueLoan.setShipment(loan2Ship);
        closedLoan.getShipments().add(loan1Ship);
        overdueLoan.getShipments().add(loan2Ship);
        dataObjects.add(loan1Ship);
        dataObjects.add(loan2Ship);   
        
        persist(dataObjects);
        dataObjects.clear();

        persist(dataObjects);
        dataObjects.clear();
        
        frame.setProcess(++createStep);
               
        commitTx();
        
        frame.setProcess(++createStep);
        
        buildDarwinCoreSchema(discipline);

        // done
        log.info("Done creating "+disciplineType.getTitle()+" disciplineType database: " + disciplineType.getTitle());
        return dataObjects;
    }
    /**
     * @param treeDef
     * @param disciplineType
     */
    @SuppressWarnings("unchecked")
    public void createStorageTreeDefFromXML(final Vector<Object> storageList, 
                                            final File domFile,
                                            final StorageTreeDef treeDef) throws Exception
    {
        StorageTreeDefItem parent = null;
        
        Element root = XMLHelper.readFileToDOM4J(domFile);
        for (Element node : (List<Element>)root.selectNodes("/tree/treedef/level"))
        {
            String  name       = getAttr(node, "name", null);
            int     rankId     = getAttr(node, "rank", 0);
            boolean infullname = getAttr(node, "infullname", false);
            boolean isEnforced = getAttr(node,  "enforced", false);
            
            StorageTreeDefItem tdi = new StorageTreeDefItem();
            tdi.initialize();
            tdi.setName(name);
            tdi.setRankId(rankId);
            tdi.setIsEnforced(isEnforced);
            tdi.setIsInFullName(infullname);
            treeDef.getTreeDefItems().add(tdi);
            tdi.setParent(parent);
            if (parent != null)
            {
                parent.getChildren().add(tdi);
            }
            tdi.setTreeDef(treeDef);
            persist(tdi);
            parent = tdi;
        }
        
        createStorageTreeFromXML(storageList, root, treeDef);

    }
    
    @SuppressWarnings("unchecked")
    protected void createStorageTreeFromXML(final Vector<Object> storageList, 
                                            final Element        root,
                                            final StorageTreeDef treeDef)
    {
        StorageTreeDefItem rootTTD = null;
        Hashtable<Integer, StorageTreeDefItem> treeDefItemHash = new Hashtable<Integer, StorageTreeDefItem>();
        for (StorageTreeDefItem ttdi : treeDef.getTreeDefItems())
        {
            treeDefItemHash.put(ttdi.getRankId(), ttdi);
            if (ttdi.getRankId() == 0)
            {
                rootTTD = ttdi;
            }
        }
        
        Storage storage = new Storage();
        storage.initialize();
        
        storage.setRankId(0);
        storage.setName("Storage Root");
        storage.setDefinition(treeDef);
        storage.setDefinitionItem(rootTTD);
        rootTTD.getTreeEntries().add(storage);
        storage.setParent(null);
        persist(storage);
        storageList.add(storage);
        
        for (Element node : (List<Element>)root.selectNodes("/tree/nodes/node"))
        {
            traverseTree(storageList, treeDefItemHash, treeDef, node, storage);
        }
        
        TreeHelper.fixFullnameForNodeAndDescendants(storage);
        storage.setNodeNumber(1);
        fixNodeNumbersFromRoot(storage);
    }
    
    @SuppressWarnings("unchecked")
    protected void traverseTree(final Vector<Object> storageList, 
                                final Hashtable<Integer, StorageTreeDefItem> treeDefItemHash,
                                final StorageTreeDef treeDef,
                                final Element root,
                                final Storage parent)
    {
        String name   = getAttr(root, "name", null);
        int    rankId = getAttr(root, "rank", 0);
        
        Storage storage = new Storage();
        storage.initialize();
        
        storage.setRankId(rankId);
        storage.setName(name);
        storage.setDefinition(treeDef);
        storage.setDefinitionItem(treeDefItemHash.get(rankId));
        storage.setParent(parent);
        parent.getChildren().add(storage);
        treeDefItemHash.get(rankId).getTreeEntries().add(storage);
        persist(storage);
        storageList.add(storage);
        
        for (Element node : (List<Element>)root.selectNodes("node"))
        {
            traverseTree(storageList, treeDefItemHash, treeDef, node, storage);
        } 
    }
    
    /**
     * @param treeDef
     * @param disciplineType
     */
    @SuppressWarnings("unchecked")
    protected void createTaxonTreeDefFromXML(final TaxonTreeDef treeDef, 
                                             final DisciplineType disciplineType)
    {
        TaxonTreeDefItem parent = null;
        Element root = getDOMForDiscpline(disciplineType, "taxon_init.xml");
        for (Element node : (List<Element>)root.selectNodes("/tree/treedef/level"))
        {
            String  name       = getAttr(node, "name", null);
            int     rankId     = getAttr(node, "rank", 0);
            boolean infullname = getAttr(node, "infullname", false);
            boolean isEnforced = getAttr(node,  "enforced", false);
            
            TaxonTreeDefItem tdi = new TaxonTreeDefItem();
            tdi.initialize();
            tdi.setName(name);
            tdi.setRankId(rankId);
            tdi.setIsEnforced(isEnforced);
            tdi.setIsInFullName(infullname);
            treeDef.getTreeDefItems().add(tdi);
            tdi.setParent(parent);
            if (parent != null)
            {
                parent.getChildren().add(tdi);
            }
            tdi.setTreeDef(treeDef);
            persist(tdi);
            parent = tdi;
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void createTaxonTreeFromXML(final Vector<Object> taxonList, 
                                          final TaxonTreeDef treeDef, 
                                          final DisciplineType disciplineType)
    {
        TaxonTreeDefItem rootTTD = null;
        Hashtable<Integer, TaxonTreeDefItem> treeDefItemHash = new Hashtable<Integer, TaxonTreeDefItem>();
        for (TaxonTreeDefItem ttdi : treeDef.getTreeDefItems())
        {
            treeDefItemHash.put(ttdi.getRankId(), ttdi);
            if (ttdi.getRankId() == 0)
            {
                rootTTD = ttdi;
            }
        }
        Taxon taxon = new Taxon();
        taxon.initialize();
        
        taxon.setRankId(0);
        taxon.setName("Taxonomy Root");
        taxon.setDefinition(treeDef);
        taxon.setDefinitionItem(rootTTD);
        rootTTD.getTreeEntries().add(taxon);
        taxon.setParent(null);
        persist(taxon);
        taxonList.add(taxon);
        
        Element root = getDOMForDiscpline(disciplineType, "taxon_init.xml");
        for (Element node : (List<Element>)root.selectNodes("/tree/nodes/node"))
        {
            traverseTree(taxonList, treeDefItemHash, treeDef, node, taxon);
        }
        
        TreeHelper.fixFullnameForNodeAndDescendants(taxon);
        taxon.setNodeNumber(1);
        fixNodeNumbersFromRoot(taxon);
    }
    
    @SuppressWarnings("unchecked")
    protected void traverseTree(final Vector<Object>   taxonList, 
                                final Hashtable<Integer, TaxonTreeDefItem> treeDefItemHash,
                                final TaxonTreeDef     treeDef,
                                final Element          root,
                                final Taxon            parent)
    {
        String name   = getAttr(root, "name",   null);
        String common = getAttr(root, "common", null);
        int    rankId = getAttr(root, "rank",   0);
        
        Taxon taxon = new Taxon();
        taxon.initialize();
        
        taxon.setRankId(rankId);
        taxon.setName(name);
        taxon.setCommonName(common);
        taxon.setDefinition(treeDef);
        taxon.setDefinitionItem(treeDefItemHash.get(rankId));
        taxon.setParent(parent);
        parent.getChildren().add(taxon);
        treeDefItemHash.get(rankId).getTreeEntries().add(taxon);
        persist(taxon);
        taxonList.add(taxon);
        
        for (Element node : (List<Element>)root.selectNodes("node"))
        {
            traverseTree(taxonList, treeDefItemHash, treeDef, node, taxon);
        } 
    }
    
    protected Element getDOMForDiscpline(final DisciplineType dType, final String fileName)
    {
        return XMLHelper.readDOMFromConfigDir(dType.getName()+ File.separator + fileName);
    }
    
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public void createFishCollection(final DisciplineType disciplineType,
                                     final Institution    institution,
                                     final SpecifyUser    user)
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
        
        Discipline discipline = createDiscipline(division, 
                                                 disciplineType.getName(), 
                                                 disciplineType.getTitle(), 
                                                 dataType, 
                                                 taxonTreeDef, 
                                                 geoTreeDef, 
                                                 gtpTreeDef, 
                                                 locTreeDef, 
                                                 lithoStratTreeDef);
        
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);        
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
        
        List<SpPrincipal> groups = new ArrayList<SpPrincipal>();

        DataBuilder.createStandardGroups(groups, discipline);
        SpPrincipal disciplineGroup = DataBuilder.findGroup(groups, discipline, "CollectionManager");
        
        Agent userAgent = createAgent(title, firstName, midInit, lastName, abbrev, email);
        userAgent.setDivision(division);
        discipline.addReference(userAgent, "agents");
        user.addReference(userAgent, "agents");
        user.addUserToSpPrincipalGroup(disciplineGroup);
        
        SpPrincipal userPrincipal = DataBuilder.createUserPrincipal(user);
        groups.add(userPrincipal);
        user.addUserToSpPrincipalGroup(userPrincipal);
        
        // Tester
        SpPrincipal guestGroup  = DataBuilder.findGroup(groups, discipline, "Guest");
        Agent       testerAgent = createAgent("", "Fish", "", "Tester", "", "fishtester@brc.ku.edu");
        testerAgent.setDivision(division);
        SpecifyUser testerUser          = createSpecifyUser("FishTester", "fishtester@brc.ku.edu", "FishTester", disciplineGroup, guestGroup.getGroupType());
        SpPrincipal testerUserPrincipal = DataBuilder.createUserPrincipal(testerUser);
        groups.add(testerUserPrincipal);
        testerUser.addUserToSpPrincipalGroup(guestGroup);
        testerUser.addUserToSpPrincipalGroup(testerUserPrincipal);
        discipline.addReference(testerAgent, "agents");
        testerUser.addReference(testerAgent, "agents");
        
        startTx();
        persist(discipline);               
        persist(user);
        persist(userAgent);
        persist(testerUser);
        persist(testerAgent);
        persist(groups);
             
        loadSchemaLocalization(discipline, SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());
        commitTx();
        
        makeFieldVisible(null, discipline);
        makeFieldVisible(disciplineType.getName(), discipline);

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
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(locs);
        persist(gtps);
        persist(lithoStrats);
        commitTx(); 
        
        //frame.setProcess(++createStep);
        frame.setOverall(steps++);
        
        Collection voucher = null;
        if (isChoosen(DisciplineType.STD_DISCIPLINES.fish, false))
        {
            voucher = createFishCollection(discipline, user, userAgent, division,
                                            taxonTreeDef, geoTreeDef, gtpTreeDef,
                                            lithoStratTreeDef, locTreeDef,
                                            journal, taxa, geos, locs, gtps, lithoStrats,
                                            "KUFSH", "Fish", true, false);
        }
        

        frame.setOverall(steps++);
        
        Collection tissue = null;
        if (isChoosen(DisciplineType.STD_DISCIPLINES.fish, true))
        {
            tissue = createFishCollection(discipline, user, userAgent, division,
                                            taxonTreeDef, geoTreeDef, gtpTreeDef,
                                            lithoStratTreeDef, locTreeDef,
                                            journal, taxa, geos, locs, gtps, lithoStrats,
                                            "KUTIS", "Fish Tissue", false, true);
        }
        
        if (voucher != null && tissue != null)
        {
            startTx();
            CollectionRelType colRelType = createCollectionRelType("Voucher Tissue", voucher, tissue);
            persist(colRelType);
            commitTx(); 
        }

        buildDarwinCoreSchema(discipline);

        globalLocalities.clear();
    }
    
    /**
     * @param type
     * @param isTissue
     * @return
     */
    protected boolean isChoosen(final DisciplineType.STD_DISCIPLINES type, 
                                final boolean isTissue)
    {
        if (selectedChoices != null)
        {
            for (CollectionChoice cc : selectedChoices)
            {
                if (cc.getType() == type && cc.isTissue() == isTissue)
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @return returns a list of preptypes read in from preptype.xml
     */
    @SuppressWarnings("unchecked")
    protected Vector<PrepType> loadPrepTypes()
    {
        Vector<PrepType> prepTypes = new Vector<PrepType>();
        
        XStream xstream = new XStream();
        xstream.alias("preptype",     PrepType.class);
        
        xstream.omitField(PrepType.class, "prepTypeId");
        xstream.omitField(PrepType.class, "collection");
        xstream.omitField(PrepType.class, "preparations");
        xstream.omitField(PrepType.class, "attributeDefs");
        
        xstream.useAttributeFor(PrepType.class, "name");
        xstream.useAttributeFor(PrepType.class, "isLoanable");
        
        xstream.aliasAttribute("isloanable",  "isLoanable");
        
        xstream.omitField(DataModelObjBase.class,  "timestampCreated");
        xstream.omitField(DataModelObjBase.class,  "timestampModified");
        xstream.omitField(DataModelObjBase.class,  "lastEditedBy");
        
        String discipline = AppContextMgr.getInstance().getClassObject(Discipline.class).getName();
        File   file       = XMLHelper.getConfigDir(discipline + File.separator + "preptypes.xml");
        if (file.exists())
        {
            try
            {
                prepTypes = (Vector<PrepType>)xstream.fromXML(FileUtils.readFileToString(file));
                
            } catch (Exception ex)
            {
                log.error(ex);
            }
    
            Timestamp now = new Timestamp(System.currentTimeMillis());
            for (PrepType pt : prepTypes)
            {
                pt.setCreatedByAgent(Agent.getUserAgent());
                pt.setTimestampCreated(now);
                pt.setCollection(AppContextMgr.getInstance().getClassObject(Collection.class));
                pt.setPreparations(new HashSet<Preparation>());
                pt.setAttributeDefs(new HashSet<AttributeDef>());
            }
            return prepTypes;
        }
        throw new RuntimeException("preptypes.xml is missing for discipline["+discipline+"]");
    }
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    @SuppressWarnings("unchecked")
    public Collection createFishCollection(final Discipline                discipline,
                                           final SpecifyUser               user,
                                           final Agent                     userAgent,
                                           final Division                  division,                  
                                           final TaxonTreeDef              taxonTreeDef,
                                           final GeographyTreeDef          geoTreeDef,
                                           final GeologicTimePeriodTreeDef gtpTreeDef,
                                           final LithoStratTreeDef         lithoStratTreeDef,
                                           final StorageTreeDef            locTreeDef,
                                           final Journal                   journal,
                                           final List<Object>              taxa,
                                           final List<Object>              geos,
                                           final List<Object>              locs,
                                           final List<Object>              gtps,
                                           final List<Object>              lithoStrats,
                                           final String                    colPrefix,
                                           final String                    colName,
                                           final boolean                   isVoucherCol,
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
        Collection collection = createCollection(colPrefix, colName, cns, discipline, false);
        persist(collection);
        
        List<SpPrincipal> groups = new ArrayList<SpPrincipal>();
        DataBuilder.createStandardGroups(groups, collection);

        AppContextMgr.getInstance().setClassObject(Collection.class, collection);
        
        persist(groups);
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////
        log.info("Creating picklists");
        
        createPickLists(session, null);
        BldrPickList colMethods = createPickLists(session, discipline);
        
        commitTx();
        
        frame.setProcess(++createStep);
        
        startTx();
        
        //DBTableIdMgr schema = new DBTableIdMgr(false);
        //schema.initialize(new File(XMLHelper.getConfigDirPath("specify_datamodel.xml")));
        //loadSchemaLocalization(discipline, SpLocaleContainer, schema);
        //buildDarwinCoreSchema(discipline);

        AppContextMgr.getInstance().setClassObject(SpecifyUser.class, user);
        
        user.addReference(userAgent, "agents");
        
        persist(user);

        
        frame.setProcess(++createStep);
        
        
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
        
        Locality forestStream;
        Locality lake;
        Locality farmpond;
        
        if (isVoucherCol)
        {
            log.info("Creating localities");
            forestStream = createLocality("Unnamed forest stream pond", (Geography)geos.get(12));
            localities.add(forestStream);
            globalLocalities.add(forestStream);
            forestStream.setLatLongType(POINT);
            forestStream.setOriginalLatLongUnit(0);
            forestStream.setLat1text("38.925467 deg N");
            forestStream.setLatitude1(new BigDecimal(38.925467));
            forestStream.setLong1text("94.984867 deg W");
            forestStream.setLongitude1(new BigDecimal(-94.984867));
            
            lake   = createLocality("Deep, dark lake pond", (Geography)geos.get(17));
            localities.add(lake);
            globalLocalities.add(lake);
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
            
            farmpond = createLocality("Shoal Creek at Schermerhorn Park, S of Galena at Rt. 26", (Geography)geos.get(11));
            localities.add(farmpond);
            globalLocalities.add(farmpond);
            
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
            
            persist(forestStream);
            persist(lake);
            persist(farmpond);
        } else
        {
            forestStream = globalLocalities.get(0);
            lake         = globalLocalities.get(1);
            farmpond     = globalLocalities.get(2);
            localities.addAll(globalLocalities);
        }

        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // agents and addresses
        ////////////////////////////////
        log.info("Creating agents and addresses");
        List<Agent>    agents      = new Vector<Agent>();
        Agent johnByrn = null;
        Agent ku = new Agent();
        
        if (isVoucherCol)
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
            ku.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
            AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(ku);
            
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
            otherAgent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
            AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(otherAgent);
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
                groupAgent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
                AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(groupAgent);
                
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
        calendar.set(1993, 3, 19, 11, 56, 00);
        String stationFieldNumber = String.format(STATION_FIELD_FORMAT, stationFieldNumberCounter++);
        CollectingEvent ce1 = createCollectingEvent(forestStream, calendar, stationFieldNumber, new Collector[]{collectorMitch,collectorJim});
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
        calendar.set(1993, 3, 20, 06, 12, 00);
        stationFieldNumber = String.format(STATION_FIELD_FORMAT, stationFieldNumberCounter++);
        CollectingEvent ce2 = createCollectingEvent(farmpond, calendar, stationFieldNumber, new Collector[]{collectorMeg,collectorRod});
        ce2.setStartDateVerbatim("20 Mar 1993, 6:12 AM");
        calendar.set(1993, 3, 20, 07, 31, 00);
        ce2.setEndDate(calendar);
        ce2.setEndDateVerbatim("20 Mar 1993, 7:31 AM");
        ce2.setMethod(colMethods.getItem(2).getValue());

        CollectingTrip trip = createCollectingTrip("My Collecint Trip", "Sample collecting trip", new CollectingEvent[]{ce1,ce2});

        
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
        Permit permit = createPermit("1993-FISH-0001", "US Dept Wildlife", issuedDate, startDate, endDate, null);
        permit.setIssuedTo(ku);
        permit.setIssuedBy(agents.get(4));
        dataObjects.add(permit);
        
        log.info("Creating a repository agreement");
        RepositoryAgreement repoAg = new RepositoryAgreement();
        repoAg.initialize();
        repoAg.setDivision(division);
        repoAg.setRepositoryAgreementNumber("KU-1992-01");
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
        
        if (isVoucherCol)
        {
            current    = createDeterminationStatus(discipline, "Current",    "", DeterminationStatus.CURRENT);
            notCurrent = createDeterminationStatus(discipline, "Not current","", DeterminationStatus.NOTCURRENT);
            incorrect  = createDeterminationStatus(discipline, "Incorrect",  "", DeterminationStatus.USERDEFINED);
            oldDet     = createDeterminationStatus(discipline, "Old Determination","", DeterminationStatus.OLDDETERMINATION);

            persist(current);
            persist(notCurrent);
            persist(incorrect);
            persist(oldDet);
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
        
        Vector<PrepType> prepTypesForSaving = loadPrepTypes();
        Vector<PrepType> pt                 = new Vector<PrepType>();
        
        if (doTissues)
        {
            pt.clear();
            PrepType tissuePT = createPrepType(collection, "Tissue");
            for (int i=0;i<prepTypesForSaving.size();i++)
            {
                pt.add(tissuePT);
            }
            prepTypesForSaving.clear();
            prepTypesForSaving.add(tissuePT);
            
        } else
        {
            pt.addAll(prepTypesForSaving);
        }

        List<Preparation> preps = new Vector<Preparation>();
        Calendar prepDate = Calendar.getInstance();
        preps.add(createPreparation(pt.get(0), agents.get(0), collObjs.get(0), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(0), agents.get(0), collObjs.get(1), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(0), agents.get(1), collObjs.get(2), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(0), agents.get(1), collObjs.get(3), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(0), agents.get(2), collObjs.get(4), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(0), agents.get(2), collObjs.get(5), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(0), agents.get(3), collObjs.get(6), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(0), agents.get(3), collObjs.get(7), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(1), agents.get(1), collObjs.get(0), (Storage)locs.get(11), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(1), agents.get(1), collObjs.get(1), (Storage)locs.get(11), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(1), agents.get(1), collObjs.get(2), (Storage)locs.get(10), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(1), agents.get(2), collObjs.get(3), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(1), agents.get(3), collObjs.get(4), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(1), agents.get(0), collObjs.get(5), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(2), agents.get(1), collObjs.get(6), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(2), agents.get(1), collObjs.get(7), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(2), agents.get(1), collObjs.get(2), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));

        preps.add(createPreparation(pt.get(3), agents.get(1), collObjs.get(0), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(3), agents.get(1), collObjs.get(1), (Storage)locs.get(7), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(3), agents.get(1), collObjs.get(2), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(3), agents.get(1), collObjs.get(3), (Storage)locs.get(8), rand.nextInt(20)+1, prepDate));
        preps.add(createPreparation(pt.get(3), agents.get(1), collObjs.get(4), (Storage)locs.get(9), rand.nextInt(20)+1, prepDate));

        dataObjects.add(collection);
        dataObjects.addAll(prepTypesForSaving);
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
        String name = discipline.getTitle() + " DataSet";
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
                            
                            File file = new File(attachmentFilesLoc + photos[i]);
                            if (!file.exists())
                            {
                                continue;
                            }
                            
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
                            copyrightData.setValue("2008");
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
            List<Taxon> taxa2 = session.createQuery("SELECT t FROM Taxon t WHERE t.name = 'Ammocrypta'").list();
            List<ReferenceWork> rwList = new Vector<ReferenceWork>();

            startTx();
            rwList.addAll(journal.getReferenceWorks());
            
            TaxonCitation taxonCitation = new TaxonCitation();
            taxonCitation.initialize();
            Taxon ammocrypta = taxa2.get(0);
            taxonCitation.setTaxon(ammocrypta);
            taxonCitation.setReferenceWork(rwList.get(0));
            rwList.get(0).addTaxonCitations(taxonCitation);
            ammocrypta.getTaxonCitations().add(taxonCitation);
            dataObjects.add(taxonCitation);
            persist(taxonCitation);
            
            if (isVoucherCol)
            {
                Locality locality = localities.get(0);
                LocalityCitation localityCitation = new LocalityCitation();
                localityCitation.initialize();
                localityCitation.setLocality(locality);
                locality.getLocalityCitations().add(localityCitation);
                localityCitation.setReferenceWork(rwList.get(1));
                rwList.get(1).addLocalityCitations(localityCitation);
                dataObjects.add(localityCitation);
                persist(localityCitation);
            }
            commitTx();
        }
        frame.setProcess(++createStep);

        return collection;
    }
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public void createSingleDiscipline(final DisciplineType disciplineType,
                                       final String usernameArg,
                                       final String passwordArg)
    {
        System.out.println("Creating single disciplineType database: " + disciplineType.getTitle());
        
        int createStep = 0;
        
        frame.setProcess(0, 4);
        
        frame.setProcess(++createStep);

        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           username         = initPrefs.getProperty("initializer.username", usernameArg);
        String           email            = initPrefs.getProperty("useragent.email", "rods@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", "CollectionManager");
        String           password         = initPrefs.getProperty("useragent.password", passwordArg);
        
        System.out.println("----- User Agent -----");
        System.out.println("Userame:   "+username);
        
        List<SpPrincipal> groups = new ArrayList<SpPrincipal>();
        
        Institution    institution    = createInstitution("Natural History Museum");        
        
        SpecifyUser user = createSpecifyUser(username, email, password, userType);
//        SpPrincipal     userPrincipal = DataBuilder.createUserPrincipal(user);
//        groups.add(userPrincipal);
        
        SpPrincipal admin = createAdminPrincipal("Administrator", institution);
        groups.add(admin);
        user.addUserToSpPrincipalGroup(admin);
        
        startTx();
        persist(institution);        
        persist(user); 
        persist(groups);
        //persist(admin);
        commitTx();
        
        frame.setProcess(++createStep);
        
        //boolean done = false;
        if (isChoosen(DisciplineType.STD_DISCIPLINES.fish, false) ||
            isChoosen(DisciplineType.STD_DISCIPLINES.fish, true))
        {
            createFishCollection(DisciplineType.getDiscipline("fish"), institution, user);/*, groupPrincipal);*/
            //done = true;
        }
        
        frame.setOverall(steps++);
        
        if (isChoosen(DisciplineType.STD_DISCIPLINES.invertpaleo, false))
        {
            createSingleInvertPaleoCollection(DisciplineType.getDiscipline("invertpaleo"), institution, user);
            //done = true;
        }
        frame.setOverall(steps++);
        
        if (isChoosen(DisciplineType.STD_DISCIPLINES.botany, false))
        {
            if (!doHugeBotany)
            {
                createSingleBotanyCollection(DisciplineType.getDiscipline("botany"), institution, user);
            } else
            {
                createHugeBotanyCollection(DisciplineType.getDiscipline("botany"), institution, user);
            }
            //done = true;
        }
        
        for (DisciplineType.STD_DISCIPLINES disp : DisciplineType.STD_DISCIPLINES.values())
        {
            if (disp != DisciplineType.STD_DISCIPLINES.botany &&
                disp != DisciplineType.STD_DISCIPLINES.invertpaleo &&
                disp != DisciplineType.STD_DISCIPLINES.fish &&
                isChoosen(disp, false))
            {
                DisciplineType dType = DisciplineType.getDiscipline(disp);
                if (XMLHelper.getConfigDir(dType.getName()+ File.separator + "taxon_init.xml").exists())
                {
                    createGenericCollection(dType, institution, user);
                }
            }
        }
        frame.setOverall(steps++);
        
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
            try
            {
                
                session.saveOrUpdate(o);
                
            } catch (Exception ex)
            {
                UIRegistry.showError(ex.toString());
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
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
        try
        {
            
            HibernateUtil.commitTransaction();
            
        } catch (Exception ex)
        {
            UIRegistry.showError(ex.toString());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
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
    public void buildSetup(final String[] args)
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
        
        if (StringUtils.isEmpty(UIRegistry.getAppName()))
        {
            UIRegistry.setAppName("Specify");
        }
        
        if (!wasJavaDBSet)
        {
            UIRegistry.setJavaDBDir(derbyPath != null ? derbyPath : UIRegistry.getDefaultWorkingPath() + File.separator + "DerbyDatabases");
        }
        
        if (hideFrame)
        {
            System.out.println("Derby Path [ "+UIRegistry.getJavaDBPath()+" ]");
        }
        
        System.setProperty(AppPreferences.factoryName,          "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences
        System.setProperty("edu.ku.brc.dbsupport.DataProvider", "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
        System.setProperty(SecurityMgr.factoryName,             "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");       // Needed for Tree Field Names //$NON-NLS-1$

        embeddedSpecifyAppRootUser = SecurityMgr.getInstance().getEmbeddedUserName();
        embeddedSpecifyAppRootPwd  = SecurityMgr.getInstance().getEmbeddedPwd();
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());

        backstopPrefs = getInitializePrefs(null);
        
        String driverName   = backstopPrefs.getProperty("initializer.drivername",   "MySQL");
        String databaseName = backstopPrefs.getProperty("initializer.databasename", "testfish");    
            
        if (doEmptyBuild)
        {
            ensureDerbyDirectory(driverName);
            
            DisciplineType     disciplineType = DisciplineType.getDiscipline("fish");
            DatabaseDriverInfo driverInfo     = DatabaseDriverInfo.getDriver(driverName);
            DBConfigInfo       config         = new DBConfigInfo(driverInfo, "localhost", "WorkBench", "guest", "guest", 
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
                        int seconds = TIME_THRESHOLD;
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
     * Starts the Build on a swing worker thread.
     * 
     * @throws SQLException
     * @throws IOException
     */
    protected void startBuild(final String     dbName, 
                              final String     driverName, 
                              final DisciplineType disciplineType,
                              final String     username, 
                              final String     password,
                              final List<CollectionChoice> selectedChoicesArg)
    {
        AppContextMgr.getInstance().setHasContext(true); // Fake that there is a Context
        
        this.selectedChoices = selectedChoicesArg;
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
                    embeddedSpecifyAppRootUser, 
                    embeddedSpecifyAppRootPwd))
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
            String msg = "Couldn't find driver by name ["+driverInfo+"] in driver list.";
            UIRegistry.showError(msg);
            throw new RuntimeException(msg);
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
                              embeddedSpecifyAppRootUser, 
                              embeddedSpecifyAppRootPwd))
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
                    try {
                        FileUtils.cleanDirectory(attLoc);
                    }
                    catch(IOException e)
                    {
                        String msg = "failed to connect to directory location to delete directory: " + attLoc;
                        log.warn(msg);
                        UIRegistry.showError(msg);
                    }
                    AttachmentManagerIface attachMgr = new FileStoreAttachmentManager(attLoc);
                    
                    AttachmentUtils.setAttachmentManager(attachMgr);
                    AttachmentUtils.setThumbnailer(thumb);
                    
                    // save it all to the DB
                    setSession(HibernateUtil.getCurrentSession());

                    createSingleDiscipline(disciplineType, username, password);

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

                    assignPermssions();
                    
                    log.info("Done");
                }
                catch(Exception e)
                {
                    try
                    {
                        rollbackTx();
                        log.error("Failed to persist DB objects", e);
                        UIRegistry.showError("Failed to persist DB objects");
                        return;
                    }
                    catch(Exception e2)
                    {
                        log.error("Failed to persist DB objects.  Rollback failed.  DB may be in inconsistent state.", e2);
                        UIRegistry.showError("Failed to persist DB objects. Rollback failed.");
                        return;
                    }
                }
            }
        }
        else
        {
            log.error("Login failed");
            UIRegistry.showError("Login failed");
            return;
        }
        
        System.out.println("All done");
        
        if (frame != null)
        {
            frame.processDone();
        }
        
        // Set the Schema Size into Locale Prefs
        String schemaKey = "schemaSize";
        int    schemaFileSize = 0;
        File schemaFile = XMLHelper.getConfigDir("specify_datamodel.xml");
        if (schemaFile != null)
        {
            schemaFileSize = (int)schemaFile.length();
            AppPreferences.getLocalPrefs().putInt(schemaKey, schemaFileSize);
        }
        
        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                "The build completed successfully.", 
                "Complete", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void assignPermssions()
    {
/*        setSession(HibernateUtil.getCurrentSession());
        SpecifyUser u = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        List<?> users       = HibernateUtil.getCurrentSession().createQuery("SELECT u FROM SpecifyUser u WHERE t.name = 'Ammocrypta'").list();
        SpSecurtyPermissionMgr.setupTestTreePermissions(u);*/
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
        protected JComboBox          catNumFmts;
        protected JCheckBox          extraCollectionsChk;
        protected JComboBox          disciplines;
        protected ToggleButtonChooserPanel<CollectionChoice> collChoicePanel;
        protected Vector<DatabaseDriverInfo>  driverList;
        protected Vector<UIFieldFormatterIFace> catNumFmtList;
        protected boolean            wasClosed = false;
        
        /**
         * Creates a dialog for entering database name and selecting the appropriate driver.
         * @param databaseName 
         * @param dbDriverName
         */
        public SetupDialog(final String databaseName, 
                           final String dbDriverName)
        {
            super();
            
            Specify.setUpSystemProperties();
            UIFieldFormatterMgr.setDoingLocal(true);
            
            driverList = DatabaseDriverInfo.getDriversList();
            int inx = Collections.binarySearch(driverList, new DatabaseDriverInfo(dbDriverName, null, null));
            
            drivers = createComboBox(driverList);
            drivers.setSelectedIndex(inx);
            
            catNumFmtList = (Vector<UIFieldFormatterIFace>)UIFieldFormatterMgr.getInstance().getFormatterList(CollectionObject.class, "catalogNumber");
            Collections.sort(catNumFmtList, new Comparator<UIFieldFormatterIFace>() {
                @Override
                public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
                {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });
            catNumFmts = createComboBox(catNumFmtList);
            
            int i = 0;
            for (UIFieldFormatterIFace fmt : catNumFmtList)
            {
                System.out.println(fmt.getName());
                if (fmt.getName().equals("CatalogNumberNumeric"))
                {
                    catNumFmts.setSelectedIndex(i);
                }
                i++;
            }
            
            //Vector<DisciplineType> disciplinesList = DisciplineType.getDisciplineList();
            disciplines     = createComboBox(DisciplineType.getDisciplineList());
            disciplines.setSelectedItem(DisciplineType.getDiscipline("fish"));
            
            databaseNameTxt = createTextField(databaseName);
            
            usernameTxtFld = createTextField("rods");
            passwdTxtFld   = createPasswordField("rods");
            
            extraCollectionsChk = createCheckBox("Create Extra Collections");
            extraCollectionsChk.setSelected(true);
            
            PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,2px,p:g", "p,4px,p,4px,p,4px,p,4px,p,4px,p,4px,p:g,10px,p"));
            CellConstraints cc         = new CellConstraints();
            builder.add(createLabel("Username:", SwingConstants.RIGHT),      cc.xy(1,1));
            builder.add(usernameTxtFld,                                     cc.xy(3,1));
            builder.add(createLabel("Password:", SwingConstants.RIGHT),      cc.xy(1,3));
            builder.add(passwdTxtFld,                                       cc.xy(3,3));
            builder.add(createLabel("Database Name:", SwingConstants.RIGHT), cc.xy(1,5));
            builder.add(databaseNameTxt,                                    cc.xy(3,5));
            builder.add(createLabel("DisciplineType Name:", SwingConstants.RIGHT), cc.xy(1,7));
            builder.add(disciplines,                                        cc.xy(3,7));
            builder.add(createLabel("Driver:", SwingConstants.RIGHT),        cc.xy(1,9));
            builder.add(drivers,                                            cc.xy(3,9));
            builder.add(createLabel("Cat. Num Fmt:", SwingConstants.RIGHT), cc.xy(1,11));
            builder.add(catNumFmts,                                         cc.xy(3,11));
            
            //int y = 13;
            CollectionChoice[] choicesArray = {
                    new CollectionChoice(DisciplineType.STD_DISCIPLINES.fish, false, true),
                    new CollectionChoice(DisciplineType.STD_DISCIPLINES.fish, true, true),
                    new CollectionChoice(DisciplineType.STD_DISCIPLINES.botany, false, true),
                    new CollectionChoice(DisciplineType.STD_DISCIPLINES.invertpaleo, false, true),
            };
            
            // For some reason peristsing the CollectionChoice vector causes the JVM on the Mac to die.
            // Get persisted choices
            /*List<CollectionChoice> selectdObjects = new Vector<CollectionChoice>();
            Hashtable<String, CollectionChoice> choiceHash = new Hashtable<String, CollectionChoice>();
            for (CollectionChoice choice : loadPersistedChoices())
            {
                choiceHash.put(choice.toString(), choice);
            }
            for (CollectionChoice choice : choicesArray)
            {
                CollectionChoice permChoice = choiceHash.get(choice.toString());
                if (permChoice != null && permChoice.isSelected())
                {
                    selectdObjects.add(choice);
                }
            }*/
            Vector<CollectionChoice> choices = new Vector<CollectionChoice>();
            Collections.addAll(choices, choicesArray);
            
            for (DisciplineType.STD_DISCIPLINES disp : DisciplineType.STD_DISCIPLINES.values())
            {
                if (disp != DisciplineType.STD_DISCIPLINES.botany &&
                    disp != DisciplineType.STD_DISCIPLINES.invertpaleo &&
                    disp != DisciplineType.STD_DISCIPLINES.fish)
                {
                    DisciplineType dType = DisciplineType.getDiscipline(disp);
                    File file = XMLHelper.getConfigDir(dType.getName()+ File.separator + "taxon_init.xml");
                    if (file != null && file.exists())
                    {
                        choices.add(new CollectionChoice(disp, false, true));
                    }
                }
            }
            collChoicePanel = new ToggleButtonChooserPanel<CollectionChoice>(choices, ToggleButtonChooserPanel.Type.Checkbox);
            collChoicePanel.setAddSelectAll(true);
            collChoicePanel.setUseScrollPane(true);
            collChoicePanel.createUI();
            collChoicePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            builder.add(collChoicePanel, cc.xywh(1,13,3,1));
            
            collChoicePanel.setSelectedObjects(choices);
            
            final JButton okBtn     = createButton("OK");
            final JButton cancelBtn = createButton("Cancel");
            builder.add(ButtonBarFactory.buildOKCancelBar(okBtn, cancelBtn), cc.xywh(1,15,3,1));
            
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    closeDlg(true);
                }
             });
             
            okBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    //saveChoices((Vector<CollectionChoice>)collChoicePanel.getSelectedObjects());

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
        
        /**
         * @return
         */
        public List<CollectionChoice> getSelectedColleectionChoices()
        {
            return collChoicePanel.getSelectedObjects();
        }
        
        /**
         * @return
         */
        @SuppressWarnings("unchecked")
        protected Vector<CollectionChoice> loadPersistedChoices()
        {
            XStream xstream = new XStream();
            config(xstream);
            
            File file = new File(UIRegistry.getDefaultWorkingPath()+File.separator+"bld_coll_choices.xml");
            if (file.exists())
            {
                try
                {
                    Vector<CollectionChoice> list = (Vector<CollectionChoice>)xstream.fromXML(FileUtils.readFileToString(file));
                    for (CollectionChoice cc : list)
                    {
                        cc.initialize();
                    }
                    return list;
                    
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
            return new Vector<CollectionChoice>();
        }
        
        /**
         * @param choices
         */
        public void saveChoices(final Vector<CollectionChoice> choices)
        {
            XStream xstream = new XStream();
            config(xstream);
            
            System.out.println("Start");
            File file = new File(UIRegistry.getDefaultWorkingPath()+File.separator+"bld_coll_choices.xml");
            try
            {
                FileUtils.writeStringToFile(file, xstream.toXML(choices));
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
            System.out.println("Stop");
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
                        UIFieldFormatterIFace cnf = (UIFieldFormatterIFace)catNumFmts.getSelectedItem();
                        if (cnf != null)
                        {
                            catalogNumberFmtName = cnf.getName();
                        }
                        String username = usernameTxtFld.getText();
                        String password = new String(passwdTxtFld.getPassword());
                        
                        startBuild(databaseName, 
                                   dbDriver.getName(), 
                                   (DisciplineType)disciplines.getSelectedItem(), 
                                   username, 
                                   password,
                                   getSelectedColleectionChoices());
                        
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
    
    /**
     * 
     */
    protected static void loadFieldsToHideHash()
    {
        if (fieldsToHideHash.size() == 0)
        {
            String[] fields = { "version", 
                                "timestampCreated", 
                                "timestampModified", 
                                "createdByAgent", 
                                "modifiedByAgent", 
                                "collectionMemberId", 
                                "visibility", 
                                "visibilitySetBy"};
            
            for (String fieldName : fields)
            {
                fieldsToHideHash.put(fieldName, true);
            }
        }
    }
    
    /**
     * @param memoryItem
     * @param newItem
     * @param hideGenericFields
     */
    public static void loadLocalization(final SpLocaleContainerItem memoryItem, 
                                        final SpLocaleContainerItem newItem,
                                        final boolean hideGenericFields)
    {
        String itemName = memoryItem.getName();
        newItem.setName(itemName);
        
        newItem.setType(memoryItem.getType());
        newItem.setFormat(memoryItem.getFormat());
        newItem.setIsUIFormatter(memoryItem.getIsUIFormatter());
        newItem.setPickListName(memoryItem.getPickListName());
        newItem.setWebLinkName(memoryItem.getWebLinkName());
        newItem.setIsHidden(memoryItem.getIsHidden());
        
        if (fieldsToHideHash.get(itemName) != null || 
            itemName.startsWith("text") ||
            itemName.startsWith("number") ||
            itemName.startsWith("yesNo"))
        {
            newItem.setIsHidden(true);
        }

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
    
    /**
     * @param memoryContainer
     * @param newContainer
     */
    public static void loadLocalization(final SpLocaleContainer memoryContainer, 
                                        final SpLocaleContainer newContainer,
                                        final boolean hideGenericFields)
    {
        newContainer.setName(memoryContainer.getName());
        newContainer.setType(memoryContainer.getType());
        newContainer.setFormat(newContainer.getFormat());
        newContainer.setIsUIFormatter(newContainer.getIsUIFormatter());
        newContainer.setPickListName(newContainer.getPickListName());
        newContainer.setWebLinkName(newContainer.getWebLinkName());
        newContainer.setIsHidden(newContainer.getIsHidden());
        
        boolean isColObj = memoryContainer.getName().equals("collectionobject");
        
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
            
            loadLocalization(item, newItem, hideGenericFields);
            
            if (isColObj && item.getName().equals("catalogNumber") && catalogNumberFmtName != null)
            {
                newItem.setFormat(catalogNumberFmtName);
            }
            
        }
    }
    
    /**
     * @param discipline
     * @param schemaType
     * @param tableMgr
     */
    public static void loadSchemaLocalization(final Discipline discipline, final Byte schemaType, final DBTableIdMgr tableMgr)
    {
        
        HiddenTableMgr hiddenTableMgr = new HiddenTableMgr();

        SchemaLocalizerXMLHelper schemaLocalizer = new SchemaLocalizerXMLHelper(schemaType, tableMgr);
        schemaLocalizer.load();
        
        boolean hideGenericFields = true;
        
        loadFieldsToHideHash();
        
        for (SpLocaleContainer table : schemaLocalizer.getSpLocaleContainers())
        {
            SpLocaleContainer container = new SpLocaleContainer();
            container.initialize();
            container.setName(table.getName());
            container.setType(table.getType());
            container.setSchemaType(schemaType);
            
            container.setIsHidden(hiddenTableMgr.isHidden(discipline.getName(), table.getName()));
            
            loadLocalization(table, container, hideGenericFields);
            
            discipline.getSpLocaleContainers().add(container);
            container.setDiscipline(discipline);
        }
    }
    
    /**
     * Make specific fields visible.
     */
    public static void makeFieldVisible(final String disciplineDirName,
                                        final Discipline discipline)
    {
        //setFieldVisible("collectionobject", "timestampModified");
        //setFieldVisible("determination",    "yesNo1");
        
        try
        {
            String dirName  = disciplineDirName != null ? disciplineDirName + File.separator : "";
            String filePath = XMLHelper.getConfigDirPath(dirName + "show_fields.xml");
            File showFieldsFile = new File(filePath);
            if (showFieldsFile.exists())
            {
                System.out.println(FileUtils.readFileToString(showFieldsFile));
                
                Element root = XMLHelper.readDOMFromConfigDir(dirName + "show_fields.xml");
                if (root != null)
                {
                    List<?> tables = root.selectNodes("/tables/table");
                    for (Iterator<?> iter = tables.iterator(); iter.hasNext(); )
                    {
                        Element table = (Element)iter.next();
                        String  tName = XMLHelper.getAttr(table, "name", null);
                        if (StringUtils.isNotEmpty(tName))
                        {
                            DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoByTableName(tName.toLowerCase());
                            if (tbl != null)
                            {
                                List<?> fields = table.selectNodes("field");
                                for (Iterator<?> fIter = fields.iterator(); fIter.hasNext(); )
                                {
                                    Element fieldEl = (Element)fIter.next();
                                    String  fName   = XMLHelper.getAttr(fieldEl, "name", null);
                                    if (StringUtils.isNotEmpty(fName))
                                    {
                                        DBFieldInfo fld = tbl.getFieldByName(fName);
                                        if (fld != null)
                                        {
                                            setFieldVisible(tbl.getName(), fld.getName(), discipline);
                                        } else
                                        {
                                            UIRegistry.showError("show_list.xml in ["+disciplineDirName+"] for table name ["+tName+"] has bad field name["+fName+"]");
                                        }
                                    }
                                }
                            } else
                            {
                                UIRegistry.showError("show_list.xml in ["+disciplineDirName+"] has bad table name ["+tName+"]");
                            }
                        }
                    }   
                }
            }
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    /**
     * Looks up a table/field and sets it to be visible.
     * @param tableName the table name
     * @param fieldName the field name
     */
    protected static void setFieldVisible(final String tableName, 
                                          final String fieldName,
                                          final Discipline discipline)
    {
        DataProviderSessionIFace localSession = null;
        try
        {
            localSession = DataProviderFactory.getInstance().createSession();
            Object[] cols = (Object[])localSession.getData("FROM SpLocaleContainer as sp INNER JOIN sp.discipline as d WHERE sp.name = '" + tableName + "' AND d.disciplineId = "+discipline.getId());
            SpLocaleContainer container = (SpLocaleContainer)cols[0];
            if (container != null)
            {
                for (SpLocaleContainerItem item : container.getItems())
                {
                    System.out.println(fieldName+" "+ item.getName());
                    if (item.getName().equals(fieldName))
                    {
                        item.setIsHidden(false);
                        localSession.beginTransaction();
                        localSession.save(item);
                        localSession.commit();
                        localSession.flush();
                        return;
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally 
        {
            if (localSession != null)
            {
                localSession.close();
            }
        }
    }
    
    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
        
        new HiddenTableMgr();
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
    
    private void config(final XStream xstream)
    {
        xstream.alias("choice",       CollectionChoice.class);
        xstream.useAttributeFor(CollectionChoice.class, "name");
        xstream.useAttributeFor(CollectionChoice.class, "isTissue");
        xstream.useAttributeFor(CollectionChoice.class, "isSelected");
        
        xstream.omitField(CollectionChoice.class, "type");
    }
    

    class CollectionChoice
    {
        protected DisciplineType.STD_DISCIPLINES  type;
        protected String  name;
        protected boolean isTissue;
        protected boolean isSelected;
        /**
         * 
         */
        public CollectionChoice()
        {
            super();
            // TODO Auto-generated constructor stub
        }
        /**
         * @param name
         * @param isTissue
         * @param isSelected
         */
        public CollectionChoice(DisciplineType.STD_DISCIPLINES type, boolean isTissue, boolean isSelected)
        {
            super();
            this.type = type;
            this.isTissue = isTissue;
            this.isSelected = isSelected;
            this.name = type.toString();
            
        }
        
        public void initialize()
        {
            type = DisciplineType.STD_DISCIPLINES.valueOf(name);
        }
        
        /**
         * @return the type
         */
        public DisciplineType.STD_DISCIPLINES getType()
        {
            return type;
        }
        /**
         * @param type the type to set
         */
        public void setType(DisciplineType.STD_DISCIPLINES type)
        {
            this.type = type;
            this.name = type.toString();
        }
        
        /**
         * @return the isTissue
         */
        public boolean isTissue()
        {
            return isTissue;
        }
        /**
         * @param isTissue the isTissue to set
         */
        public void setTissue(boolean isTissue)
        {
            this.isTissue = isTissue;
        }
        /**
         * @return the isSelected
         */
        public boolean isSelected()
        {
            return isSelected;
        }
        /**
         * @param isSelected the isSelected to set
         */
        public void setSelected(boolean isSelected)
        {
            this.isSelected = isSelected;
        }
        
        public String toString()
        {
            DisciplineType dType = DisciplineType.getDiscipline(type);
            return dType.getTitle() + (isTissue ? " Tissue" : "");
        }
    }

}