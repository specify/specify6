/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tests;


import static edu.ku.brc.specify.utilapps.DataBuilder.createAccession;
import static edu.ku.brc.specify.utilapps.DataBuilder.createAgent;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCatalogNumberingScheme;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollection;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectionObject;
import static edu.ku.brc.specify.utilapps.DataBuilder.createCollectionType;
import static edu.ku.brc.specify.utilapps.DataBuilder.createDataType;
import static edu.ku.brc.specify.utilapps.DataBuilder.createSpecifyUser;
import static edu.ku.brc.specify.utilapps.DataBuilder.createTaxonTreeDef;
import static edu.ku.brc.specify.utilapps.DataBuilder.createUserGroup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.Discipline;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CatalogNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionType;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.dbsupport.CollectionAutoNumber;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.specify.ui.CatalogNumberUIFieldFormatter;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.ui.forms.validation.ValFormattedTextField;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileStoreAttachmentManager;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 1, 2007
 *
 */
public class TestAutoNumbering extends TestCase
{
    private final Logger         log      = Logger.getLogger(TestAutoNumbering.class);
            
    protected static boolean  doInit  = true;
    protected static boolean  fillDB  = true;
    protected static Session  session = null;
    protected static MyFmtMgr fmtMgr  = null;

    /**
     * Setup all the System properties. This names all the needed factories. 
     */
    protected void setUpSystemProperties()
    {
        // Name factories
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences
        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");            // Needed By UIRegistry
        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System
        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and logging transactions
        System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Complete UI
        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory");
        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");    // Needed for CatalogNumberign
    }
    
    public static void persist(Object o)
    {
        if (session != null)
        {
            session.saveOrUpdate(o);
        }
    }

    public static void persist(List<?> oList)
    {
        for (Object o: oList)
        {
            persist(o);
        }
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
        TaxonTreeDef      taxonTreeDef      = createTaxonTreeDef("Sample Taxon Tree Def");
        LithoStratTreeDef lithoStratTreeDef = BuildSampleDatabase.createStandardLithoStratDefinitionAndItems();
        
        CollectionType collectionType = createCollectionType(collTypeName, disciplineName, dataType, user, taxonTreeDef, null, null, null, lithoStratTreeDef);

        SpecifyUser.setCurrentUser(user);
        user.setAgent(userAgent);

        dataObjects.add(collectionType);
        dataObjects.add(userGroup);
        dataObjects.add(user);
        dataObjects.add(dataType);
        dataObjects.add(taxonTreeDef);
        dataObjects.add(userAgent);
        
        List<Object> taxa = BuildSampleDatabase.createSimpleTaxon(collectionType.getTaxonTreeDef());
        List<Object> geos = BuildSampleDatabase.createSimpleGeography(collectionType, "Geography");
        List<Object> locs = BuildSampleDatabase.createSimpleLocation(collectionType, "Location");
        List<Object> gtps = BuildSampleDatabase.createSimpleGeologicTimePeriod(collectionType, "Geologic Time Period");

        dataObjects.addAll(taxa);
        dataObjects.addAll(geos);
        dataObjects.addAll(locs);
        dataObjects.addAll(gtps);
        
        CatalogNumberingScheme cns = createCatalogNumberingScheme("CatalogNumber", "", true);
        dataObjects.add(cns);

        Collection collection = createCollection("Fish", "Fish", cns, collectionType);
        dataObjects.add(collection);

        return dataObjects;
    }

    
    /** 
     * Drops, Creates and Builds the Database.
     * 
     * @throws SQLException
     * @throws IOException
     */
    public boolean setupDatabase(final DatabaseDriverInfo driverInfo,
                                 final String hostName, 
                                 final String dbName, 
                                 final String username, 
                                 final String password, 
                                 final String firstName, 
                                 final String lastName, 
                                 final String email,
                                 final Discipline  discipline)
    {
        String actionStr = fillDB ? "Creating" : "Initializing";
        log.info(actionStr + " Specify Database Username["+username+"]");
        log.info(actionStr + " Database Schema for "+dbName);
        log.info(actionStr + " schema");
          
        if (fillDB)
        {
            try
            {
                SpecifySchemaGenerator.generateSchema(driverInfo, hostName, dbName, username, password);
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                return false;
            }
        }
        
        log.info("Logging into "+dbName+"....");
        
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
            log.info("Login Failed!");
            return false;
        }         
        
        session = HibernateUtil.getCurrentSession();

        log.info("Creating database "+dbName+"....");
        
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
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
        
        if (fillDB)
        {
            log.info("Creating Empty Database");
            
            List<Object> dataObjects = createEmptyDiscipline(discipline.getTitle(), 
                                                             discipline.getName(),
                                                             username,
                                                             "CollectionManager",
                                                             firstName,
                                                             lastName,
                                                             email);
            
    
            log.info("Saving data into "+dbName+"....");
            log.info("Persisting Data...");
            HibernateUtil.beginTransaction();
            persist(dataObjects);
            HibernateUtil.commitTransaction();
        }
        
        
        SpecifyAppPrefs.initialPrefs();
        
        fmtMgr = new MyFmtMgr();

        return true;
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        if (doInit)
        {
            UIRegistry.setAppName("Specify");
            
            //UIRegistry.setJavaDBDir(derbyPath != null ? derbyPath : UIRegistry.getDefaultWorkingPath() + File.separator + "DerbyDatabases");
            
            setUpSystemProperties();
            
            Discipline         discipline = Discipline.getDiscipline("fish");
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
            
            // This may just initialize the DB and not build it, check data member 'fillDB'
            setupDatabase(driverInfo, "localhost", "Test", "rods", "rods", "guest", "guest", "guest@ku.edu", discipline);
            doInit = false;
        }
    }
    
    /**
     * Tests a numeric catalog number with an empty database and increments it.
     */
    public void testNumericNumWithEmptyDB()
    {
        log.info("testEmptyDBNumericNum");
        
        // Test Empty Database
        CatalogNumberUIFieldFormatter catalogNumber = new CatalogNumberUIFieldFormatter();
        catalogNumber.setAutoNumber(new CollectionAutoNumber());

        ValFormattedTextField valText = new ValFormattedTextField(catalogNumber, false);
        valText.setValue(null, null);
        
        valText.updateAutoNumbers();
        
        assertEquals(valText.getValue(), "000000001");

    }

    
    /**
     * Tests a numeric catalog number and increments it.
     */
    public void testNumericDBNum()
    {
        log.info("testNumericDBNum");
        
        Agent      agent      = (Agent)session.createCriteria(Agent.class).list().get(0);
        Collection collection = (Collection)session.createCriteria(Collection.class).list().get(0);
        
        CollectionObject colObj = createCollectionObject("000000100", "RSC100", agent, collection,  3, null, Calendar.getInstance(), "BuildSampleDatabase");
        HibernateUtil.beginTransaction();
        persist(colObj);
        HibernateUtil.commitTransaction();
        
        CatalogNumberUIFieldFormatter catalogNumber = new CatalogNumberUIFieldFormatter();
        catalogNumber.setAutoNumber(new CollectionAutoNumber());
        
        ValFormattedTextField valText = new ValFormattedTextField(catalogNumber, false);
        valText.setValue(null, null);
        
        valText.updateAutoNumbers();
        
        log.info(valText.getValue());
        
        assertEquals(valText.getValue(), "000000101");
        
        HibernateUtil.beginTransaction();
        session.delete(colObj);
        HibernateUtil.commitTransaction();
    }
    
    /**
     * Tests a numeric catalog number and increments it.
     */
    public void testAlphaNumCatNumEmptyDB()
    {
        log.info("testNumericDBNum");
        
        CollectionType colType = (CollectionType)session.createCriteria(CollectionType.class).list().get(0);
        
        CatalogNumberingScheme catNumSchemeAlphaNumeric = createCatalogNumberingScheme("CatalogNumberAN", "", false);
        Collection collection = createCollection("Fish", "Fish Tissue", catNumSchemeAlphaNumeric, colType);

        Collection.setCurrentCollection(collection);
        
        String currentYearCatNum = Calendar.getInstance().get(Calendar.YEAR) + "-000001";
        
        UIFieldFormatterIFace fmt = fmtMgr.getFmt("CatalogNumber");
        ValFormattedTextField valText = new ValFormattedTextField(fmt, false);
        valText.setValue(null, null);
        
        valText.updateAutoNumbers();
        
        log.info("["+currentYearCatNum+"]["+valText.getValue()+"]");
        
        Collection.setCurrentCollection(null);
        
        assertEquals(valText.getValue(), currentYearCatNum);
    }
    
    /**
     * Tests a numeric catalog number and increments it.
     */
    public void testAlphaNumCatNum()
    {
        log.info("testNumericDBNum");
        
        Agent          agent   = (Agent)session.createCriteria(Agent.class).list().get(0);
        CollectionType colType = (CollectionType)session.createCriteria(CollectionType.class).list().get(0);
        
        CatalogNumberingScheme catNumSchemeAlphaNumeric = createCatalogNumberingScheme("CatalogNumberAN", "", false);
        Collection collection = createCollection("Fish", "Fish Tissue", catNumSchemeAlphaNumeric, colType);

        int    currentYear       = Calendar.getInstance().get(Calendar.YEAR);
        String currentYearCatNum = currentYear + "-000001";
        String currentYearNext   = currentYear + "-000002";

        CollectionObject colObj = createCollectionObject(currentYearCatNum, "RSC100", agent, collection,  3, null, Calendar.getInstance(), "me");
        HibernateUtil.beginTransaction();
        persist(catNumSchemeAlphaNumeric);
        persist(collection);
        persist(colObj);
        HibernateUtil.commitTransaction();
        
        Collection.setCurrentCollection(collection);
        
        UIFieldFormatterIFace fmt = fmtMgr.getFmt("CatalogNumber");
        ValFormattedTextField valText = new ValFormattedTextField(fmt, false);
        valText.setValue(null, null);
        
        valText.updateAutoNumbers();
        
        log.info("["+currentYearNext+"]["+valText.getValue()+"]");
        
        Collection.setCurrentCollection(null);
        
        HibernateUtil.beginTransaction();
        session.delete(colObj);
        session.delete(collection);
        session.delete(catNumSchemeAlphaNumeric);
        HibernateUtil.commitTransaction();
        
        assertEquals(valText.getValue(), currentYearNext);

        
    }
    
    /**
     * Tests the highest Year in the database is less than the current year.
     * and this tests when the user doesn't change the year in the text field.
     */
    public void testAccessionAlphaNumericDBNumOldYear()
    {
        log.info("testAlphaNumericDBNumOldYear");
        
        Accession accession = createAccession("XXX", "XXXX", "2005-IC-001", "XXXX", Calendar.getInstance(), Calendar.getInstance());
        HibernateUtil.beginTransaction();
        persist(accession);
        HibernateUtil.commitTransaction();
        
        UIFieldFormatterIFace fmt = fmtMgr.getFmt("AccessionNumber");
        ValFormattedTextField valText = new ValFormattedTextField(fmt, false);
        valText.setValue(null, null);
        
        try
        {
            valText.updateAutoNumbers();
            
            String currentYearCatNum = Calendar.getInstance().get(Calendar.YEAR) + "-AA-001";
            log.info(currentYearCatNum);
            log.info(valText.getValue());
            assertEquals(valText.getValue(), currentYearCatNum);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        HibernateUtil.beginTransaction();
        session.delete(accession);
        HibernateUtil.commitTransaction();
    }
    
    /**
     * Tests the highest Year in the database is less than the current year.
     * and this tests when the user doesn't change the year in the text field.
     */
    public void testAccessionAlphaNumericDBNumOldYearWithEdit()
    {
        log.info("testAlphaNumericDBNumOldYear");
        
        Accession accession = createAccession("XXX", "XXXX", "2005-IC-001", "XXXX", Calendar.getInstance(), Calendar.getInstance());
        HibernateUtil.beginTransaction();
        persist(accession);
        HibernateUtil.commitTransaction();
        
        UIFieldFormatterIFace fmt = fmtMgr.getFmt("AccessionNumber");
        ValFormattedTextField valText = new ValFormattedTextField(fmt, false);
        valText.setValue(null, null);
        
        valText.setText("2005-IC-###");
        valText.updateAutoNumbers();
        
        log.info(valText.getValue());
        
        // NOTE: Must delete before the assert
        HibernateUtil.beginTransaction();
        session.delete(accession);
        HibernateUtil.commitTransaction();  
        
        assertEquals(valText.getValue(), "2005-IC-002");
    }

    /**
     * Tests the highest Year in the database is less than the current year.
     * and this tests when the user doesn't change the year in the text field.
     */
    public void testAlphaNumWithTwoCollections()
    {
        log.info("testAlphaNumericDBNumOldYear");
        
        Accession accession = createAccession("XXX", "XXXX", "2005-IC-001", "XXXX", Calendar.getInstance(), Calendar.getInstance());
        HibernateUtil.beginTransaction();
        persist(accession);
        HibernateUtil.commitTransaction();
        
        UIFieldFormatterIFace fmt = fmtMgr.getFmt("AccessionNumber");
        ValFormattedTextField valText = new ValFormattedTextField(fmt, false);
        valText.setValue(null, null);
        
        valText.setText("2005-IC-###");
        valText.updateAutoNumbers();
        
        log.info(valText.getValue());
        
        // NOTE: Must delete before the assert
        HibernateUtil.beginTransaction();
        session.delete(accession);
        HibernateUtil.commitTransaction();  
        
        assertEquals(valText.getValue(), "2005-IC-002");
    }


    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        log.info("tearDown");
    }

    class MyFmtMgr extends SpecifyUIFieldFormatterMgr
    {
        /**
         * Returns the DOM it is suppose to load the formatters from.
         * @return Returns the DOM it is suppose to load the formatters from.
         */
        protected Element getDOM() throws Exception
        {
            return XMLHelper.readDOMFromConfigDir("backstop/uiformatters.xml");
        }
        
        public UIFieldFormatterIFace getFmt(final String name)
        {
            return getFormatterInternal(name);

        }
    }
}
