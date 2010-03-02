/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.utilapps;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.specify.config.init.DataBuilder.buildDarwinCoreSchema;
import static edu.ku.brc.specify.config.init.DataBuilder.createAccession;
import static edu.ku.brc.specify.config.init.DataBuilder.createAccessionAgent;
import static edu.ku.brc.specify.config.init.DataBuilder.createAddress;
import static edu.ku.brc.specify.config.init.DataBuilder.createAgent;
import static edu.ku.brc.specify.config.init.DataBuilder.createAgentVariant;
import static edu.ku.brc.specify.config.init.DataBuilder.createAndAddTesterToCollection;
import static edu.ku.brc.specify.config.init.DataBuilder.createAttachment;
import static edu.ku.brc.specify.config.init.DataBuilder.createAttributeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createAutoNumberingScheme;
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

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.dom4j.Element;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.BackupServiceFactory;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.init.BldrPickList;
import edu.ku.brc.specify.config.init.BldrPickListItem;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.config.init.HiddenTableMgr;
import edu.ku.brc.specify.config.init.TreeDefRow;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentAttachment;
import edu.ku.brc.specify.datamodel.AgentVariant;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentMetadata;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
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
import edu.ku.brc.specify.datamodel.DNASequence;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Determination;
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
import edu.ku.brc.specify.datamodel.TreeDefItemStandardEntry;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchRowImage;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.dbsupport.BuildFromGeonames;
import edu.ku.brc.specify.dbsupport.HibernateDataProviderSession;
import edu.ku.brc.specify.dbsupport.SpecifyDeleteHelper;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.specify.tools.schemalocale.DisciplineBasedContainer;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerXMLHelper;
import edu.ku.brc.specify.treeutils.NodeNumberer;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.treeutils.TreeHelper;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileStoreAttachmentManager;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * 
 * @code_status Beta
 * 
 * @author rods
 */
public class BuildSampleDatabase
{
    private static final Logger  log      = Logger.getLogger(BuildSampleDatabase.class);
    
    //                                                  0                   1                  2                 3                   4                     5                   6                   7                     8
    private static String[] TaxonIndexNames = {"family common name", "species author", "species source", "species lsid", "species common name", "subspecies author", "subspecies source", "subspecies lsid", "subspecies common name"};
    private static String[] TaxonFieldNames = {"CommonName",         "Author",         "Source",         "GUID",         "CommonName",          "Author",            "Source",            "GUID",            "CommonName"};
    
    private static int FAMILY_COMMON_NAME     = 0;
    private static int SPECIES_AUTHOR         = 1;
    private static int SPECIES_SOURCE         = 2;
    private static int SPECIES_LSID           = 3;
    private static int SPECIES_COMMON_NAME    = 4;
    private static int SUBSPECIES_AUTHOR      = 5;
    private static int SUBSPECIES_SOURCE      = 6;
    private static int SUBSPECIES_LSID        = 7;
    private static int SUBSPECIES_COMMON_NAME = 8;
    
    protected Hashtable<String, Integer> taxonExtraColsIndexes = new Hashtable<String, Integer>();
    protected Hashtable<String, Integer> taxonIndexes          = new Hashtable<String, Integer>();

    protected static boolean     debugOn        = false;
    protected static final int   TIME_THRESHOLD = 3000;
    protected static Hashtable<String, Boolean> fieldsToHideHash = new Hashtable<String, Boolean>();
    protected static Hashtable<String, Boolean> fieldsToSkipHash = new Hashtable<String, Boolean>();
    protected static List<Object>               locs             = null;

    protected Calendar           calendar = Calendar.getInstance();
    protected Session            session;
    
    protected int                steps = 0;   
    protected ProgressFrame      frame;
    protected Properties         initPrefs     = null;
    protected Properties         backstopPrefs = null;
    
    protected SetUpBuildDlg      setupDlg  = null;
    protected boolean            hideFrame = false;
    
    protected boolean            doAddQueries        = false;
    protected boolean            copyToUserDir       = true;
    protected boolean            doShallowTaxonTree  = false;
    protected List<CollectionChoice> selectedChoices = null;
    
    protected Hashtable<Class<?>, Vector<AutoNumberingScheme>> numberingSchemesHash = new Hashtable<Class<?>, Vector<AutoNumberingScheme>>();
    
    protected Random             rand = new Random(12345678L);
    
    protected Vector<Locality>    globalLocalities = new Vector<Locality>();
    protected Vector<Agent>       globalAgents = new Vector<Agent>();
    
    protected int                 stationFieldNumberCounter = 100;
    protected String              STATION_FIELD_FORMAT = "RS%03d";
    
    protected int                 NUM_LOCALTIES = 50000;
    protected int                 NUM_COLOBJS   = 50000;
    
    protected boolean             doHugeBotany = false;
    
    protected DataType            dataType;
    protected StorageTreeDef      stgTreeDef = null;
    protected int                 createStep = 0;
    protected Transaction         trans      = null;
    
    protected static Timestamp                              now                    = new Timestamp(System .currentTimeMillis());
    protected static SimpleDateFormat                       dateTimeFormatter;
    protected static SimpleDateFormat                       dateFormatter;
    protected static String                                 nowStr;
    
    protected LinkedList<Pair<String, Integer>> recycler = new LinkedList<Pair<String, Integer>>();
    protected StringBuilder gSQLStr = new StringBuilder();
    
    protected NumberFormat     numFmt = NumberFormat.getInstance();
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    
    /**
     * 
     */
    public BuildSampleDatabase()
    {
        dateTimeFormatter  = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        dateFormatter      = new SimpleDateFormat("yyyy-MM-dd");
        nowStr              = "'" + dateTimeFormatter.format(now) + "'";
        
        numFmt.setMinimumFractionDigits(0);
        numFmt.setMaximumFractionDigits(20);
        numFmt.setGroupingUsed(false); //gets rid of commas
    }
    
    /**
     * @return
     */
    public Session getSession()
    {
        return session;
    }
    
    /**
     * @return
     */
    public DataType getDataType()
    {
        return dataType;
    }
    
    /**
     * @param s
     */
    public void setSession(final Session s)
    {
        session = s;
    }
    
    /**
     * @return
     */
    public ProgressFrame getFrame()
    {
        return frame;
    }
    
    /**
     * @param dataObjects
     * @param userAgent
     */
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
     * @param name the name of the scheme
     * @return a numbering scheme by name.
     */
    private AutoNumberingScheme getAutoNumberingScheme(final Class<?> clazz, final String name)
    {
        Vector<AutoNumberingScheme> numberingSchemes = numberingSchemesHash.get(clazz);
        if (numberingSchemes != null)
        {
            for (AutoNumberingScheme ans : numberingSchemes)
            {
                if (ans.getSchemeName().equals(name))
                {
                    return ans;
                }
            }
        }
        return null;
    }
    
    /**
     * @param clazz
     * @param ans
     */
    private void addAutoNumberingScheme(final Class<?> clazz, final AutoNumberingScheme ans)
    {
        Vector<AutoNumberingScheme> numberingSchemes = numberingSchemesHash.get(clazz);
        if (numberingSchemes == null)
        {
            numberingSchemes = new Vector<AutoNumberingScheme>();
            numberingSchemesHash.put(clazz, numberingSchemes);
        }
        numberingSchemes.add(ans);
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
    public boolean createEmptyInstitution(final Properties props, final boolean doCreateDiv, final boolean doCreateDisp)
    {
        AppContextMgr.getInstance().setHasContext(true);
        
        createStep = 0;    
        
        if (frame != null) frame.setProcess(0, 10);
        
        if (frame != null) frame.setProcess(++createStep);
        
        Institution institution = null;
        
        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String username = props.getProperty("usrUsername");
        String password = props.getProperty("usrPassword");
        
        startTx();
        
        System.out.println("----- User Agent -----");
        System.out.println("Userame:   "+username);
        
        institution = createInstitution(props.getProperty("instName"));
        institution.setCode(props.getProperty("instAbbrev"));
        institution.setIsAccessionsGlobal((Boolean)props.get("accglobal"));
        
        Address instAddress = new Address();
        instAddress.initialize();
        instAddress.setAddress(props.getProperty("addr1"));
        instAddress.setAddress2(props.getProperty("addr2"));
        instAddress.setCity(props.getProperty("city"));
        instAddress.setCountry(props.getProperty("country"));
        instAddress.setState(props.getProperty("state"));
        instAddress.setPostalCode(props.getProperty("zip"));
        instAddress.setPhone1(props.getProperty("phone"));
        
        institution.setAddress(instAddress);
        instAddress.getInsitutions().add(institution);
        
        institution.setIsSecurityOn((Boolean)props.get("security_on"));
        
        if (stgTreeDef == null)
        {
            stgTreeDef = createStorageTreeDef("Storage");
            institution.setStorageTreeDef(stgTreeDef);
            stgTreeDef.getInstitutions().add(institution);
            persist(stgTreeDef);
        }
        
        String       storXML  = props.getProperty("StorageTreeDef.treedefs");
        List<Object> storages = new Vector<Object>();
        createStorageDefFromXML(storages, stgTreeDef, storXML );
        persist(storages);
        
        String email    = props.getProperty("email");
        String userType = props.getProperty("userType");

        String encrypted = Encryption.encrypt(password, password);
        SpecifyUser specifyAdminUser = DataBuilder.createAdminGroupAndUser(session, institution, username, email, encrypted, userType);
        
        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        
        dataType = createDataType("Biota");

        persist(institution);        
        persist(specifyAdminUser); 
        persist(dataType);
        
        commitTx();
        
        AppContextMgr.getInstance().setClassObject(SpecifyUser.class, specifyAdminUser);
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);
        
        if (frame != null) frame.setProcess(++createStep);
        
        if (doCreateDiv)
        {
            DisciplineType disciplineType = (DisciplineType)props.get("disciplineType");   
            return createEmptyDivision(institution, disciplineType, specifyAdminUser, props, doCreateDisp, false) != null;
        }
        return true;
    }
    
    /**
     * @param dataType the dataType to set
     */
    public void setDataType(DataType dataType)
    {
        this.dataType = dataType;
    }

    /**
     * @param institution
     * @param disciplineType
     * @param props
     * @return
     */
    public Division createEmptyDivision(final Institution    institution, 
                                        final DisciplineType disciplineType,
                                        final SpecifyUser    specifyAdminUser, 
                                        final Properties     props,
                                        final boolean        doCreateDisp,
                                        final boolean        doSetProgressRange)
    {
        if (doSetProgressRange && frame != null)
        {
            frame.setProcess(0, 19);
        }
        
        startTx();

        Division division = createDivision(institution, 
                                           disciplineType.getName(), 
                                           props.getProperty("divName"), 
                                           props.getProperty("divAbbrev"), 
                                           null); //props.getProperty("divTitle");
        
        frame.incOverall();
        
        persist(division);
        
        String title     = props.getProperty("title",     "");
        String firstName = props.getProperty("firstName", "Test");
        String lastName  = props.getProperty("lastName",  "User");
        String midInit   = props.getProperty("middleInitial", "A");
        String abbrev    = props.getProperty("abbrev",     "");
        String email     = props.getProperty("email");
        //String userType  = props.getProperty("userType");

        /*System.out.println("----- User Agent -----");
        System.out.println("Title:     "+title);
        System.out.println("FirstName: "+firstName);
        System.out.println("LastName:  "+lastName);
        System.out.println("MidInit:   "+midInit);
        System.out.println("Abbrev:    "+abbrev);
        System.out.println("Email:     "+email);
        System.out.println("UserType:  "+userType);
        */
        
        frame.incOverall();
        
        Agent userAgent    = AppContextMgr.getInstance().getClassObject(Agent.class);
        Agent newUserAgent = null;
        String fromWiz = props.getProperty("fromwizard");
        if (userAgent == null || (StringUtils.isNotEmpty(fromWiz) && fromWiz.equals("true")))
        {
            userAgent  = createAgent(title, firstName, midInit, lastName, abbrev, email, division, null);
            
        } else
        {
            try
            {
                newUserAgent = (Agent)userAgent.clone();
                specifyAdminUser.getAgents().add(newUserAgent);
                newUserAgent.setSpecifyUser(specifyAdminUser);
                
                newUserAgent.setDivision(division); // Set the new Division
                
                session.saveOrUpdate(newUserAgent);
                session.saveOrUpdate(specifyAdminUser);
                
                userAgent = newUserAgent;
                
            } catch (CloneNotSupportedException ex)
            {
                ex.printStackTrace();
            }
        }
        
        try
        {
            userAgent = (Agent)session.merge(userAgent);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        specifyAdminUser.addReference(userAgent, "agents");
        persist(specifyAdminUser);

        
        commitTx();
        
        if (doCreateDisp)
        {
            Pair<Discipline, Collection> dc = createEmptyDisciplineAndCollection(division, props, disciplineType, userAgent, specifyAdminUser, true, false);
            if (dc == null)
            {
                return null;
            }
        }
        
        frame.incOverall();

        return division;
    }
    
    /**
     * @param division
     * @param props
     * @param disciplineType
     * @param userAgent
     * @param specifyAdminUser
     * @param doCollection
     * @return
     */
    public Pair<Discipline, Collection> createEmptyDisciplineAndCollection(final Division       division, 
                                                                           final Properties     props, 
                                                                           final DisciplineType disciplineType,
                                                                           final Agent          userAgent,
                                                                           final SpecifyUser    specifyAdminUser,
                                                                           final boolean        doCollection,
                                                                           final boolean        doSetProgressRange)
    {
        log.debug("In createEmptyDisciplineAndCollection - createStep: "+createStep);
        
        if (doSetProgressRange)
        {
           frame.setProcess(0, 17);
        }

        String dispName = props.getProperty("dispName");
        if (StringUtils.isEmpty(dispName))
        {
            dispName = disciplineType.getTitle();
        }
        
        Object pltObj = props.get("preloadtaxon");
        boolean preLoadTaxon = pltObj == null ? false : (Boolean)pltObj;
        
        String taxonXML      = props.getProperty("TaxonTreeDef.treedefs");
        String taxonFileName = props.getProperty("taxonfilename");
        String geoXML        = props.getProperty("GeographyTreeDef.treedefs");
        
        Boolean usingOtherTxnFile = (Boolean)props.get("othertaxonfile");
        
        frame.incOverall();
        
        Discipline discipline = createEmptyDiscipline(division, dispName, disciplineType, userAgent,
                                                      preLoadTaxon, 
                                                      taxonFileName, 
                                                      usingOtherTxnFile != null ? usingOtherTxnFile : false,
                                                      taxonXML, geoXML, props);
        if (discipline != null)
        {
            frame.setProcess(0, 17);
            frame.setProcess(++createStep);
            frame.setDesc("Loading Schema..."); // I18N
            
            boolean isAccGlobal = false;
            if (props.get("accglobal") == null)
            {
                Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
                isAccGlobal = inst != null && inst.getIsAccessionsGlobal();
                
            } else
            {
                isAccGlobal = (Boolean)props.get("accglobal");
            }
            
            // The two AutoNumberingSchemes have been committed
            Pair<AutoNumberingScheme, AutoNumberingScheme> ansPair = localizeDisciplineSchema(division, discipline, props, isAccGlobal);
            
            // These create a new session and persist records in the Schema tables (SpLocaleContainerItem)
            makeFieldVisible(null, discipline);
            makeFieldVisible(disciplineType.getName(), discipline);
            
            frame.setProcess(0, 17);
            frame.setProcess(++createStep);
    
            Collection collection = null;
            if (doCollection)
            {
                // Persists the Collection
                collection = createEmptyCollection(discipline, 
                                                   props.getProperty("collPrefix").toString(), 
                                                   props.getProperty("collName").toString(),
                                                   userAgent,
                                                   specifyAdminUser,
                                                   ansPair.first,
                                                   disciplineType.isEmbeddedCollecingEvent());
            }
            
            startTx();
            
            // Adds AutoNumberScheme to Division
            if (ansPair.second != null)
            {
                division.addReference(ansPair.second, "numberingSchemes");
                persist(ansPair.second);
                persist(division);
            }
            
            commitTx();
            
            buildDarwinCoreSchema(discipline); // creates own DataProviderSessionIFace
            
            log.debug("Out createEmptyDisciplineAndCollection - createStep: "+createStep);
            
            return new Pair<Discipline, Collection>(discipline, collection);
        }
        return null;
    }
    
    /**
     * @param division
     * @param dispTitle
     * @param disciplineType
     * @param userAgent
     * @param preLoadTaxon
     * @param taxonDefXML
     * @param geoDefXML
     * @return
     */
    public Discipline createEmptyDiscipline(final Division       division,
                                            final String         dispTitle,
                                            final DisciplineType disciplineType,
                                            final Agent          userAgent,
                                            final boolean        preLoadTaxon, 
                                            final String         taxonFileName,
                                            final boolean        usingOtherTxnFile, 
                                            final String         taxonDefXML, 
                                            final String         geoDefXML, 
                                            final Properties     props)
    {
        log.debug("In createEmptyDiscipline - createStep: "+createStep);

        startTx();
        
        // create tree defs (later we will make the definition items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        
        frame.incOverall();
        
        Discipline discipline = createDiscipline(division, 
                                                 disciplineType.getName(), 
                                                 dispTitle, 
                                                 dataType, 
                                                 taxonTreeDef, 
                                                 geoTreeDef, 
                                                 gtpTreeDef, 
                                                 lithoStratTreeDef);
        
        userAgent.getDisciplines().add(discipline);
        discipline.getAgents().add(userAgent);

        persist(division);
        persist(discipline);
        persist(userAgent);
        
        //commitTx();
        
        
        frame.incOverall();
        
        //startTx();

        frame.setProcess(++createStep);
        
        ////////////////////////////////
        // build the tree def items and nodes
        ////////////////////////////////
        frame.setDesc("Building Trees...");
        Vector<Object> taxa = new Vector<Object>();
        
        // Create Tree Definition
        taxonTreeDef.setDiscipline(discipline);
        taxa.add(taxonTreeDef);

        commitTx();
        
        frame.setProcess(++createStep);

        boolean isPaleo = disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.paleobotany ||
                          disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.vertpaleo ||
                          disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.invertpaleo;
        
        if (isPaleo)
        {
            startTx();
            
            LithoStratTreeDefItem earth     = createLithoStratTreeDefItem(lithoStratTreeDef, "Surface", 0, false);
            LithoStratTreeDefItem superGrp  = createLithoStratTreeDefItem(earth,     "Super Group", 100, false);
            LithoStratTreeDefItem lithoGrp  = createLithoStratTreeDefItem(superGrp,  "Litho Group", 200, false);
            LithoStratTreeDefItem formation = createLithoStratTreeDefItem(lithoGrp,  "Formation",   300, false);
            LithoStratTreeDefItem member    = createLithoStratTreeDefItem(formation, "Member",      400, false);
            @SuppressWarnings("unused")
            LithoStratTreeDefItem bed       = createLithoStratTreeDefItem(member,    "Bed",         500, true);
            persist(earth);
            
            // setup the root Geography record (planet Earth)
            LithoStrat earthNode = new LithoStrat();
            earthNode.initialize();
            earthNode.setName("Earth");
            earthNode.setFullName("Earth");
            earthNode.setNodeNumber(1);
            earthNode.setHighestChildNodeNumber(1);
            earthNode.setRankId(0);
            earthNode.setDefinition(lithoStratTreeDef);
            earthNode.setDefinitionItem(earth);
            earth.getTreeEntries().add(earthNode);
            persist(earthNode);
            
            commitTx();
            
            convertChronoStratFromXLS(gtpTreeDef); // does commits
        }
        
        frame.incOverall();
        
        List<Object> geos = new Vector<Object>();
        //List<Object> lithoStrats = isPaleo ? createSimpleLithoStrat(lithoStratTreeDef, false) : null;
        
        startTx();
        
        HashSet<String> colNameHash   = null;
        if (StringUtils.isNotEmpty(taxonFileName))
        {
            colNameHash = getColumnNamesFromXLS(taxonFileName, usingOtherTxnFile);
        }
        
        boolean taxonWasBuilt = createTaxonDefFromXML(taxa, colNameHash, taxonTreeDef, taxonDefXML);
        
        frame.incOverall();
        
        log.debug(" taxonWasBuilt "+taxonWasBuilt);
        if (!taxonWasBuilt)
        {
            TaxonTreeDefItem ttdi = new TaxonTreeDefItem();
            ttdi.initialize();
            ttdi.setTreeDef(taxonTreeDef);
            taxonTreeDef.getTreeDefItems().add(ttdi);
            ttdi.setName("Root");
            ttdi.setRankId(0);
            ttdi.setParent(null);
            ttdi.setFullNameSeparator(null);
            ttdi.setIsEnforced(true);
            ttdi.setIsInFullName(false);
            
            Taxon tx = new Taxon();
            tx.initialize();
            tx.setDefinition(taxonTreeDef);
            tx.setDefinitionItem(ttdi);
            ttdi.getTreeEntries().add(tx);
            tx.setName("Life"); // I18N
            tx.setFullName(tx.getName());
            tx.setNodeNumber(1);
            tx.setHighestChildNodeNumber(1);
            
            persist(ttdi);
            persist(tx);
        }
        
        frame.setProcess(++createStep);
        frame.incOverall();
        
        createGeographyDefFromXML(geos, geoTreeDef, geoDefXML);
        
        frame.setProcess(++createStep);
        
        persist(taxa);
        persist(geos);
        
        commitTx();
        
        frame.setProcess(++createStep);
        frame.incOverall();
        
        log.debug(" preLoadTaxon ["+preLoadTaxon+"]");
        log.debug(" fileName     ["+taxonFileName+"]");
        if (preLoadTaxon && taxonFileName != null)
        {
            convertTaxonFromXLS(taxonTreeDef, taxonFileName, usingOtherTxnFile); // this does a startTx() / commitTx()
        }
        
        frame.setProcess(++createStep);
        frame.incOverall();

//        startTx();
//        
//        if (lithoStrats != null)
//        {
//            persist(lithoStrats);
//            
//        } 
//        
//        commitTx();
        
        frame.setProcess(++createStep);
        
        //convertGeographyFromXLS(geoTreeDef);  // this does a startTx() / commitTx()
        
        String itUsername = props.getProperty("dbUserName");
        String itPassword = props.getProperty("dbPassword");
        if (StringUtils.isEmpty(itUsername) || StringUtils.isEmpty(itPassword))
        {
            frame.setVisible(false);
            
            Pair<String, String> usrPwd = SchemaUpdateService.getITUsernamePwd();
            frame.setVisible(true);
            if (usrPwd != null)
            {
                itUsername = usrPwd.first;
                itPassword = usrPwd.second;
                
            } else
            {
                return null;
            }
        }
        
        //convertGeographyFromXLS(discipline.getGeographyTreeDef());
        @SuppressWarnings("unused")
        Geography earth = createGeographyFromGeonames(discipline, userAgent, itUsername, itPassword);
        
        frame.setProcess(++createStep);
        frame.incOverall();
        
        log.debug("Out createEmptyDiscipline - createStep: "+createStep);
        
        return discipline;
    }
    
    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Vector<TaxonFileDesc> readTaxonLoadFiles()
    {
        try
        {
            String fileName = "taxonfiles.xml";
            
            File file = XMLHelper.getConfigDir("../demo_files/taxonomy/"+fileName);
            log.debug(" file "+file.getAbsolutePath() +"  "+file.exists());
            if (!file.exists())
            {
                log.error("Couldn't file[" + file.getAbsolutePath() + "] checking the config dir");
                file = XMLHelper.getConfigDir(fileName);
                log.debug(" file "+file.getAbsolutePath() +"  "+file.exists());
                if (!file.exists())
                {
                    file = new File("Specify/demo_files/"+fileName);
                }
            }
    
            if (file == null || !file.exists() || file.isDirectory())
            {
                log.error("Couldn't file[" + file.getAbsolutePath() + "]");
                return null;
            }
            
            XStream xstream = new XStream();
            TaxonFileDesc.configXStream(xstream);
            
            return (Vector<TaxonFileDesc>)xstream.fromXML(FileUtils.readFileToString(file));
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * @param discipline
     * @param collPrefix
     * @param collName
     * @param userAgent
     * @param specifyAdminUser
     * @param catNumScheme
     * @param isEmbeddedCE
     * @return
     */
    public Collection createEmptyCollection(final Discipline         discipline, 
                                            final String             collPrefix, 
                                            final String             collName,
                                            final Agent              userAgent,
                                            final SpecifyUser        specifyAdminUser,
                                            final AutoNumberingScheme catNumScheme,
                                            final boolean             isEmbeddedCE)
    {
        log.debug("In createEmptyCollection - createStep: "+createStep);
        
        frame.setProcess(++createStep);
        
        startTx();
        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        frame.setDesc("Creating a Collection"); // I18N
        
        Collection collection = createCollection(collPrefix, 
                                                 collName, 
                                                 catNumScheme.getFormatName(),  // Catalog Format Name
                                                 catNumScheme,                  // Catalog Number Schema
                                                 discipline, 
                                                 isEmbeddedCE);
        
        collection.setDiscipline(discipline);
        
        collection.getTechnicalContacts().add(userAgent);
        collection.getContentContacts().add(userAgent);
        
        persist(collection);
        persist(catNumScheme);
        
        frame.setProcess(++createStep);
        
        persist(loadPrepTypes(discipline.getType(), collection, userAgent));
        
        ///////////////////////////////////////
        // Default user groups and test user
        ///////////////////////////////////////

        // create the standard user groups for this collection
        Map<String, SpPrincipal> groupMap = DataBuilder.createStandardGroups(session, collection);

        // add the administrator as a Collections Manager in this group
        specifyAdminUser.addUserToSpPrincipalGroup(groupMap.get(SpecifyUserTypes.UserType.Manager.toString()));

        // add tester
        //String dspAbbrev = disciplineType.getAbbrev();
        //createAndAddTesterToCollection(dspAbbrev+"Tester", dspAbbrev+"tester@brc.ku.edu", dspAbbrev+"Tester", 
        //        "", dspAbbrev, "", "Tester", "", discipline, division, groupMap, SpecifyUserTypes.UserType.Manager.toString());
        
        commitTx();
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////

        log.info("Creating picklists");
        frame.setDesc("Creating Common PickLists...");
        
        frame.setProcess(++createStep);
        
        createPickLists(session, null, false, collection);
         
        frame.setProcess(++createStep);
         
        frame.setDesc("Creating PickLists...");
        createPickLists(session, discipline, false, collection);
         
        frame.setProcess(++createStep);

        log.debug("Out createEmptyCollection - createStep: "+createStep);

        return collection;
    }
    
    /**
     * @param props
     * @param propName
     * @param schemeName
     * @param tableId
     * @return
     */
    public AutoNumberingScheme createAutoNumScheme(final Properties props, 
                                                   final String     propName,
                                                   final String     schemeName,
                                                   final int        tableId)
    {
        Object                numFmtObj       = props.get(propName);
        UIFieldFormatterIFace numFormat       = numFmtObj instanceof UIFieldFormatterIFace ? (UIFieldFormatterIFace)numFmtObj : null;
        boolean               isNumFmtNumeric = false;
        String                numFmtName      = numFormat != null ? numFormat.getName() : (numFmtObj != null ? numFmtObj.toString() : null);
        
        if (numFormat != null)
        {
            isNumFmtNumeric = numFormat.isNumeric();
            
        } else if (StringUtils.isNotEmpty(numFmtName))
        {
            numFormat = UIFieldFormatterMgr.getInstance().getFormatter(numFmtName);
            if (numFormat != null)
            {
                isNumFmtNumeric = numFormat.isNumeric(); 
            }
        }
        
        AutoNumberingScheme autoNumScheme = null;
        if (numFormat != null)
        {
            autoNumScheme = createAutoNumberingScheme(schemeName, "", numFmtName, isNumFmtNumeric, tableId);
        }

        return autoNumScheme;
    }
    
    /**
     * @param discipline
     * @param props
     * @param isAccGlobal
     * @return
     */
    public Pair<AutoNumberingScheme, AutoNumberingScheme> localizeDisciplineSchema(final Division division, 
                                                                                   final Discipline discipline, 
                                                                                   final Properties props,
                                                                                   final boolean    isAccGlobal)
    {
        AutoNumberingScheme catNumScheme = createAutoNumScheme(props, "catnumfmt", "Catalog Numbering Scheme",   CollectionObject.getClassTableId()); // I18N
        AutoNumberingScheme accNumScheme = null;
        
        // Check to see if we are creating from scratch
        boolean isFromScratch = props.getProperty("instName") != null;
        
        
        String postFix = "FROM autonumberingscheme ans INNER JOIN autonumsch_div ad ON ans.AutoNumberingSchemeID = ad.AutoNumberingSchemeID INNER JOIN division d ON ad.DivisionID = d.UserGroupScopeId WHERE d.UserGroupScopeId = " + division.getId();
        String sql = "SELECT COUNT(*) " + postFix;
        log.debug(sql);
        int numOfDivAns = BasicSQLUtils.getCountAsInt(sql);
        if (numOfDivAns > 1)
        {
            // error
        }
        
        // NOTE: createAutoNumScheme persists the AutoNumberingScheme
        if (!isAccGlobal || isFromScratch)
        {
            if (numOfDivAns == 0)
            {
                accNumScheme = createAutoNumScheme(props, "accnumfmt", "Accession Numbering Scheme", Accession.getClassTableId()); // I18N
                
            } else
            {
                sql = "SELECT ans.AutoNumberingSchemeID " + postFix;
                log.debug(sql);
                int ansId = BasicSQLUtils.getCountAsInt(sql);
                
                DataProviderSessionIFace hSession = new HibernateDataProviderSession(session);
                List<?> list = hSession.getDataList("FROM AutoNumberingScheme WHERE id = "+ansId);
                if (list != null && list.size() == 1)
                {
                    accNumScheme = (AutoNumberingScheme)list.get(0);
                }
                // Do not close the session it is owned by someone else
                //hSession.close();
            }
            
        } else
        {
            DataProviderSessionIFace hSession = new HibernateDataProviderSession(session);
            List<?> list = hSession.getDataList("FROM AutoNumberingScheme WHERE tableNumber = "+Accession.getClassTableId());
            if (list != null && list.size() == 1)
            {
                accNumScheme = (AutoNumberingScheme)list.get(0);
            }
            // Do not close the session it is owned by someone else
            //hSession.close();
        }
        
        startTx();

        DBTableIdMgr dbMgr = new DBTableIdMgr(true);
        dbMgr.initialize();
        
        loadSchemaLocalization(discipline, 
                               SpLocaleContainer.CORE_SCHEMA, 
                               dbMgr,
                               catNumScheme.getFormatName(),
                               accNumScheme != null ? accNumScheme.getFormatName() : null);
        
        frame.setProcess(++createStep);
        
        persist(discipline);
        
        frame.setProcess(++createStep);

        commitTx();
        
        // The two AutoNumberingSchemes have been persisted.
        
        return new Pair<AutoNumberingScheme, AutoNumberingScheme>(catNumScheme, accNumScheme);
    }
    
    /**
     * @param geoList
     * @param geoTreeDef
     * @param geoXML
     */
    @SuppressWarnings("unchecked")
    public static void createGeographyDefFromXML(final List<Object>     geoList, 
                                                 final GeographyTreeDef geoTreeDef, 
                                                 final String           geoXML)
    {
        if (StringUtils.isNotEmpty(geoXML))
        {
            XStream xstream = new XStream();
            TreeDefRow.configXStream(xstream);
            
            Vector<TreeDefRow>   treeDefList = (Vector<TreeDefRow>)xstream.fromXML(geoXML);
            GeographyTreeDefItem parent      = null;
            int                  cnt         = 0;
            for (TreeDefRow row : treeDefList)
            {
                GeographyTreeDefItem gtdi = new GeographyTreeDefItem();
                gtdi.initialize();
                gtdi.setTreeDef(geoTreeDef);
                geoTreeDef.getTreeDefItems().add(gtdi);
                gtdi.setName(row.getDefName());
                gtdi.setRankId(row.getRank());
                gtdi.setParent(parent);
                gtdi.setFullNameSeparator(row.getSeparator());
                gtdi.setIsEnforced(row.isEnforced());
                gtdi.setIsInFullName(row.isInFullName());
                
                geoList.add(gtdi);
                
                if (parent != null)
                {
                    parent.getChildren().add(gtdi);
                }
                parent = gtdi;
                cnt++;
            }
        }
    }
    
    /**
     * @param taxonList
     * @param colNameHash
     * @param taxonTreeDef
     * @param taxonDefXML
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean createTaxonDefFromXML(final List<Object>    taxonList, 
                                                final HashSet<String> colNameHash,
                                                final TaxonTreeDef    taxonTreeDef, 
                                                final String          taxonDefXML)
    {
        if (StringUtils.isNotEmpty(taxonDefXML))
        {
            XStream xstream = new XStream();
            TreeDefRow.configXStream(xstream);
            
            Vector<TreeDefRow> treeDefList = (Vector<TreeDefRow>)xstream.fromXML(taxonDefXML);
            
            if (colNameHash != null)
            {
                Hashtable<String, TreeDefItemStandardEntry> stdTreeLevelsHash = new Hashtable<String, TreeDefItemStandardEntry>();
                for (TreeDefItemStandardEntry entry : TaxonTreeDef.getStandardLevelsStatic())
                {
                    stdTreeLevelsHash.put(entry.getName().toLowerCase(), entry);
                }
                
                Hashtable<String, TreeDefRow> xmlTreeLevelsHash = new Hashtable<String, TreeDefRow>();
                for (TreeDefRow row : treeDefList)
                {
                    xmlTreeLevelsHash.put(row.getDefName().toLowerCase(), row);
                }
                
                for (String columnName : colNameHash)
                {
                    String colName = columnName.toLowerCase();
                    
                    TreeDefRow treeDefRow = xmlTreeLevelsHash.get(colName);
                    if (treeDefRow == null)
                    {
                        log.debug(colName+" NOT found in XML, checking std.");
                        TreeDefItemStandardEntry entry = stdTreeLevelsHash.get(colName);
                        if (entry != null)
                        {
                            for (int i=0;i<treeDefList.size();i++)
                            {
                                if (i < treeDefList.size() && entry.getRank() < treeDefList.get(i).getRank())
                                {
                                    log.debug(String.format("Adding '%s' as rank %d.", colName, entry.getRank()));
                                    TreeDefRow newRow = new TreeDefRow(entry.getTitle(), 
                                                                        entry.getRank(),
                                                                        true,   // included
                                                                        false,  // enforced
                                                                        false,  // is in Full Name 
                                                                        false,  // is required 
                                                                        ",");
                                    treeDefList.insertElementAt(newRow, i);
                                    break;
                                }
                            }
                        } else 
                        {
                            //UIRegistry.showLocalizedError("The wizard was unable to find '%s' as a standard Taxonomy level.", colName);
                            log.debug(String.format("The wizard was unable to find '%s' as a standard Taxonomy level.", colName));
                        }
                    } else
                    {
                        log.debug(colName+" found in XML");
                    }
                }
                
                for (TreeDefRow row : treeDefList)
                {
                    log.debug(row.getDefName()+"  "+ row.getRank());
                }
            }
            
            TaxonTreeDefItem ttdiRoot = null;
            Taxon            txRoot   = null;
            
            TaxonTreeDefItem   parent      = null;
            int                cnt         = 0;
            for (TreeDefRow row : treeDefList)
            {
                if (row.isIncluded() || 
                    (row.getDefName() != null && (colNameHash == null || colNameHash.contains(row.getDefName().toLowerCase()))))
                {
                    TaxonTreeDefItem ttdi = new TaxonTreeDefItem();
                    ttdi.initialize();
                    ttdi.setTreeDef(taxonTreeDef);
                    taxonTreeDef.getTreeDefItems().add(ttdi);
                    ttdi.setName(row.getDefName());
                    ttdi.setRankId(row.getRank());
                    ttdi.setParent(parent);
                    ttdi.setFullNameSeparator(row.getSeparator());
                    ttdi.setIsEnforced(row.isEnforced());
                    ttdi.setIsInFullName(row.isInFullName());
                    
                    taxonList.add(ttdi);
                    
                    if (cnt == 0)
                    {
                        ttdiRoot = ttdi;
                        
                        Taxon tx = new Taxon();
                        tx.initialize();
                        tx.setDefinition(taxonTreeDef);
                        tx.setDefinitionItem(ttdi);
                        ttdi.getTreeEntries().add(tx);
                        tx.setName("Life"); // I18N
                        tx.setFullName("Life"); //I18N
                        tx.setNodeNumber(1);
                        tx.setHighestChildNodeNumber(1);
                        
                        txRoot = tx;
                    }
                   
                    if (parent != null)
                    {
                        parent.getChildren().add(ttdi);
                    }
                    parent = ttdi;
                    cnt++;
                }
            }
            
            if (ttdiRoot != null) persist(ttdiRoot);
            if (txRoot != null) persist(txRoot);
            
        }
        return true;
    }
    
    /**
     * @param storageList
     * @param storageTreeDef
     * @param storageXML
     */
    @SuppressWarnings("unchecked")
    public static  void createStorageDefFromXML(final List<Object> storageList, 
                                              final StorageTreeDef storageTreeDef, 
                                              final String       storageXML)
    {
        if (StringUtils.isNotEmpty(storageXML))
        {
            XStream xstream = new XStream();
            TreeDefRow.configXStream(xstream);
            
            Vector<TreeDefRow> treeDefList = (Vector<TreeDefRow>)xstream.fromXML(storageXML);
            StorageTreeDefItem parent      = null;
            int                cnt         = 0;
            for (TreeDefRow row : treeDefList)
            {
                if (row.isIncluded())
                {
                    StorageTreeDefItem stdi = new StorageTreeDefItem();
                    stdi.initialize();
                    stdi.setTreeDef(storageTreeDef);
                    storageTreeDef.getTreeDefItems().add(stdi);
                    stdi.setName(row.getDefName());
                    stdi.setRankId(row.getRank());
                    stdi.setParent(parent);
                    stdi.setFullNameSeparator(row.getSeparator());
                    stdi.setIsEnforced(row.isEnforced());
                    stdi.setIsInFullName(row.isInFullName());
                    
                    storageList.add(stdi);
                    
                    if (cnt == 0)
                    {
                        Storage stg = new Storage();
                        stg.initialize();
                        stg.setDefinition(storageTreeDef);
                        stg.setDefinitionItem(stdi);
                        stdi.getTreeEntries().add(stg);
                        stg.setName("Site");
                        stg.setFullName("Site");
                        stg.setNodeNumber(1);
                        stg.setHighestChildNodeNumber(1);
                    }
                   
                    if (parent != null)
                    {
                        parent.getChildren().add(stdi);
                    }
                    parent = stdi;
                    cnt++;
                }
            }
        }
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
    protected void addConservatorData(final List<Agent> agents, 
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
        
        persist(desc);
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
        BldrPickList colMethods = null;
        try
        {
            Transaction trans = localSession.beginTransaction();
            
            Hashtable<String, PickList> nameHash = doCheck ? new Hashtable<String, PickList>() : null;
            
            if (doCheck)
            {
                for (PickList pl : new Vector<PickList>(collection.getPickLists()))
                {
                    if (pl.getNumItems() > 0)
                    {
                        nameHash.put(pl.getName(), pl);
                        
                    } else if (discipline == null)
                    {
                        log.debug("Deleting PickList: "+pl.getName());
                        collection.getPickLists().remove(pl);
                        localSession.delete(pl);
                    }
                }
            }
            
            List<BldrPickList> pickLists = DataBuilder.getBldrPickLists(discipline != null ? discipline.getType() : "common");
            if (pickLists != null)
            {
                for (BldrPickList pl : pickLists)
                {
                    PickList pickList;
                    
                    pickList = doCheck ? nameHash.get(pl.getName()) : null;
                    if (pickList != null)
                    {
                        pickList.setIsSystem(true);
                        if (pickList.getNumItems() > 0)
                        {
                            log.info("Skipping PickList["+pl.getName()+"]");
                            continue;
                        }
                        
                    } else
                    {
                        log.info("Creating PickList["+pl.getName()+"]");
                        pickList = createPickList(pl.getName(), pl.getType(), pl.getTableName(),
                                                       pl.getFieldName(), pl.getFormatter(), pl.getReadOnly(), 
                                                       pl.getSizeLimit(), pl.getIsSystem(), pl.getSortType(), collection);
                    }
                    pickList.setIsSystem(true);
                    pickList.setCollection(collection);
                    collection.getPickLists().add(pickList);
                    
                    for (BldrPickListItem item : pl.getItems())
                    {
                        pickList.addItem(item.getTitle(), item.getValue(), item.getOrdinal());
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
            trans.commit();
            localSession.flush();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return colMethods;
    }
    
    protected CollectingEvent createFakeCollectingEvent(final List<Agent> agents,
                                                        final Locality farmpond,
                                                        final String method)
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
        ce2.setMethod(method);
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
     * @param preps
     * @param agents
     * @param dataObjects
     */
    protected void createLoanExamples(List<Preparation> preps,
                                      List<Agent>       agents,
                                      Vector<Object>    dataObjects)
    {
        if (true) return;
        
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
      
        List<LoanPreparation>         loanPreps = new Vector<LoanPreparation>();
        Vector<LoanReturnPreparation> returns      = new Vector<LoanReturnPreparation>();
        
        Loan closedLoan = createLoan("2007-001", loanDate1, currentDueDate1, originalDueDate1, 
                                     dateClosed1, Loan.CLOSED, null);
        int loanPrepCnt = 0;
        for (int i = 0; i < 7; ++i)
        {
            Preparation prep = preps.get(rand.nextInt(preps.size()));
            int available = prep.getLoanAvailable();
            if (available < 1 || !prep.getPrepType().getIsLoanable())
            {
                // retry
                i--;
                continue;
            }
            
            int quantity = Math.min(Math.max(1, rand.nextInt(available)), available);
            LoanPreparation lpo = DataBuilder.createLoanPreparation(quantity, null, null, null, 0, 0, prep, closedLoan);
            
            lpo.setIsResolved(true);
            loanPreps.add(lpo);
            
            Calendar returnedDate     = Calendar.getInstance();       
            returnedDate.setTime(lpo.getLoan().getLoanDate().getTime());
            returnedDate.add(Calendar.DAY_OF_YEAR, 72); // make the returned date be a little while after the original loan
            
            LoanReturnPreparation lrpo = createLoanReturnPreparation(returnedDate, quantity, quantity, lpo, null, agents.get(0));
            lpo.addReference(lrpo, "loanReturnPreparations");
            returns.add(lrpo);

            prep.getLoanPreparations().add(lpo);
            
            loanPrepCnt++;
            if (loanPrepCnt > 3)
            {
                break;
            }
        }
        
        Calendar loanDate2 = Calendar.getInstance();
        loanDate2.set(2005, 11, 24);
        
        Calendar currentDueDate2 = Calendar.getInstance();
        currentDueDate2.set(2006, 5, 24);
        
        Calendar originalDueDate2 = currentDueDate2;
        Loan overdueLoan = createLoan("2006-002", loanDate2, currentDueDate2, originalDueDate2,  
                                      null, Loan.OPEN, null);
        loanPrepCnt = 0;
        for (int i = 0; i < 5; ++i)
        {
            Preparation p = getObjectByClass(preps, Preparation.class, rand.nextInt(preps.size()));
            int available = p.getLoanAvailable();
            if (available < 1 || !p.getPrepType().getIsLoanable())
            {
                // retry
                i--;
                continue;
            }
            int quantity = Math.max(1, rand.nextInt(available));
            LoanPreparation lpo = createLoanPreparation(quantity, null, null, null, 0, 0, p, overdueLoan);
            loanPreps.add(lpo);
            p.getLoanPreparations().add(lpo);
            
            loanPrepCnt++;
            if (loanPrepCnt > 3)
            {
                break;
            }
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
        for( LoanPreparation lpo: loanPreps)
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
        for (int i=startIndex;i<loanPreps.size();i++)
        {
            LoanPreparation lpo = loanPreps.get(i);
        
            int    quantityLoaned   = lpo.getQuantity();
            int    quantityReturned = (i == (loanPreps.size() - 1)) ? quantityLoaned : (short)rand.nextInt(quantityLoaned);
            
            Calendar returnedDate     = Calendar.getInstance();
            
            returnedDate.setTime(lpo.getLoan().getLoanDate().getTime());
            // make the returned date be a little while after the original loan
            returnedDate.add(Calendar.DAY_OF_YEAR, 72);
            LoanReturnPreparation lrpo = createLoanReturnPreparation(returnedDate, quantityReturned, quantityReturned, lpo, null, agents.get(0));
            lpo.addReference(lrpo, "loanReturnPreparations");
            
            lpo.setQuantityReturned(quantityReturned);
            lpo.setQuantityResolved(quantityReturned);
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
        dataObjects.addAll(loanPreps);
        dataObjects.addAll(newLoanLPOs);
        dataObjects.addAll(returns);
        
        dataObjects.add(loanAgent1);
        dataObjects.add(loanAgent2);
        dataObjects.add(loanAgent3);
        dataObjects.add(loanAgent4);
        
        Calendar ship1Date = Calendar.getInstance();
        ship1Date.set(2004, 03, 19);
        Shipment loan1Ship = createShipment(ship1Date, "2006-001", "usps", (short) 1, "1.25 kg", null, agents.get(0), agents.get(4), agents.get(0));
        
        Calendar ship2Date = Calendar.getInstance();
        ship2Date.set(2005, 11, 24);
        Shipment loan2Ship = createShipment(ship2Date, "2006-002", "fedex", (short) 2, "6.0 kg", null, agents.get(3), agents.get(4), agents.get(3));
        
        loan1Ship.setLoan(closedLoan);
        loan2Ship.setLoan(overdueLoan);

        //closedLoan.setShipment(loan1Ship);
        //overdueLoan.setShipment(loan2Ship);
        closedLoan.getShipments().add(loan1Ship);
        overdueLoan.getShipments().add(loan2Ship);
        dataObjects.add(loan1Ship);
        dataObjects.add(loan2Ship);   

    }
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public List<Object> createSingleBotanyCollection(final DisciplineType disciplineType,
                                                     final Institution    institution,
                                                     final SpecifyUser    user,
                                                     final CollectionChoice choice)
    {
        frame.setProcess(0, 16);
        frame.setDesc("Creating Botany...");
        
        createStep = 0;
        
        startTx();

        Division       division   = createDivision(institution, disciplineType.getName(), "Botany", "BT", "Botany");
        
        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        
        boolean buildStorageTree = false;
        if (stgTreeDef == null)
        {
            stgTreeDef        = createStorageTreeDef("Storage");
            institution.setStorageTreeDef(stgTreeDef);
            buildStorageTree = true;
        }
        
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        
        Discipline discipline = createDiscipline(division, disciplineType.getName(), disciplineType.getTitle(), 
                                                             dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, 
                                                             lithoStratTreeDef);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        
        List<SpPrincipal> groups = new ArrayList<SpPrincipal>();
        
        
        persist(institution);
        persist(division);
        persist(discipline);
        
        AppContextMgr.getInstance().setClassObject(Division.class, division);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);

        loadSchemaLocalization(discipline, 
                                SpLocaleContainer.CORE_SCHEMA, 
                                DBTableIdMgr.getInstance(),
                                choice.getCatalogNumberingFmtName(),
                                choice.getAccessionNumberingFmtName());

        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           title            = initPrefs.getProperty("useragent.title",    "mr");
        String           firstName        = initPrefs.getProperty("useragent.firstname", "Test");
        String           lastName         = initPrefs.getProperty("useragent.lastname", "User");
        String           midInit          = initPrefs.getProperty("useragent.midinit", "A");
        String           abbrev           = initPrefs.getProperty("useragent.abbrev", "tu");
        String           email            = initPrefs.getProperty("useragent.email", "testuser@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", SpecifyUserTypes.UserType.Manager.toString());
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
        
        persist(userAgent);
        persist(user);
        
        frame.setProcess(++createStep);
        
        Pair<AutoNumberingScheme, AutoNumberingScheme> pairANS = createAutoNumberingSchemes(choice);
        AutoNumberingScheme cns         = pairANS.first;
        AutoNumberingScheme accessionNS = pairANS.second;

        persist(cns);
        persist(accessionNS);
        
        commitTx();
        
        startTx();

        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        Collection collection = createCollection("KUBOT", "Botany", choice.getCatalogNumberingFmtName(), cns, discipline);
        persist(collection);
        
        // create the standard user groups for this collection
        Map<String, SpPrincipal> groupMap = DataBuilder.createStandardGroups(session, collection);

        // add the administrator as a Collections Manager in this group
        user.addUserToSpPrincipalGroup(groupMap.get(SpecifyUserTypes.UserType.Manager.toString()));

        // Tester
        createAndAddTesterToCollection(session, "botanyuser", "botanyuser@ku.edu", "botanyuser", "mr", "Bob", "", "Botony", "",  
                                        discipline, division, groupMap, "Guest");

        persist(discipline);

        AppContextMgr.getInstance().setClassObject(Collection.class, collection);

        persist(groups);
        
        division.addReference(accessionNS, "numberingSchemes");
        persist(division);

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
        List<Object> geos        = createSimpleGeography(geoTreeDef, true);
        List<Object> gtps        = createSimpleGeologicTimePeriod(gtpTreeDef, true);
        List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef, true);
        
        institution.setStorageTreeDef(stgTreeDef);

        persist(institution);
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(buildStorageTree ? createSimpleStorage(stgTreeDef) : null);
        persist(gtps);
        persist(lithoStrats);
        commitTx();
        
        AppContextMgr.getInstance().setClassObject(Division.class, division);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);

        
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
        
        startTx();
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
        Agent steveBoyd = createAgent("mr", "Steve", "D", "Boyd", "jb", "jb@net.edu");
        if (!lastName.equals("Cooper")) agents.add(createAgent("mr", "Peter", "D", "Cooper", "ds", "ds@whitehouse.gov"));
        if (!lastName.equals("Peck")) agents.add(createAgent("mr", "David", "H", "Peck", "rb", "beach@net.edu"));
        if (!lastName.equals("Appleton")) agents.add(createAgent("mrs", "Sally", "H", "Appleton", "jm", "jm@net.edu"));
        if (!lastName.equals("Brown")) agents.add(createAgent("mr", "Taylor", "C", "Brown", "kcs", "taylor.brown@ku.edu"));
        if (!lastName.equals("Boyd")) agents.add(steveBoyd);
        if (!lastName.equals("Thomas")) agents.add(createAgent("Mr", "James", "X", "Thomas", "dxt", ""));
        if (!lastName.equals("Peterson")) agents.add(createAgent("mr", "Pete", "A", "Peterson", "jb", ""));
        if (!lastName.equals("Guttenburg")) agents.add(createAgent("mr", "Mitch", "A", "Guttenburg", "jb", ""));
        if (!lastName.equals("Ford")) agents.add(createAgent("mr", "Daniel", "A", "Ford", "mas", "mas@ku.edu"));
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
        ku.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
        
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
        otherAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

        agents.add(otherAgent);
        
        commitTx();

        List<GroupPerson> gpList = new ArrayList<GroupPerson>();
        if (true)
        {
            startTx();
            Agent gm1 = createAgent("mr", "John", "A", "Lyon", "jal", "jal@group.edu");
            Agent gm2 = createAgent("mr", "Dave", "D", "Jones", "ddj", "ddj@group.edu");
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
            groupAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

            agents.add(groupAgent);
            
            gpList.add(createGroupPerson(groupAgent, gm1, 0, division));
            gpList.add(createGroupPerson(groupAgent, gm2, 1, division));
        }

        startTx();
        
        List<AgentVariant> agentVariants = new Vector<AgentVariant>();
        agentVariants.add(createAgentVariant(AgentVariant.VARIANT, "James Variant #1", steveBoyd));
        agentVariants.add(createAgentVariant(AgentVariant.VERNACULAR, "James VERNACULAR #1", steveBoyd));
     
        List<Address> addrs = new Vector<Address>();
        addrs.add(createAddress(agents.get(1), "1600 Pennsylvania Avenue NW", null, "Washington", "DC", "USA", "20500", 0));
        addrs.add(createAddress(agents.get(1), "??? Mississippi", null, "Lawrence", "KS", "USA", "66045", 1));
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
            colEves[i] = createFakeCollectingEvent(agents, farmpond, "cut");
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
        
        determs.add(createDetermination(collObjs.get(0), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(1), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(2), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(3), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(5), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(6), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(7), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        
        determs.add(createDetermination(collObjs.get(0), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, longAgo));
        determs.add(createDetermination(collObjs.get(1), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(2), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(3), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, longAgo));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, longAgo));
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
        
        Vector<PrepType> prepTypesForSaving = loadPrepTypes(discipline.getType());
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
                                         "gift", "complete", "2000-PL-001", 
                                         DateFormat.getInstance().format(calendar.getTime()), 
                                         calendar, calendar);
        acc1.setText1("Ichthyology");
        acc1.setRepositoryAgreement(repoAg);
        
        Agent donor =    agents.get(4);
        Agent receiver = agents.get(1);
        Agent reviewer = agents.get(2);
        
        List<AccessionAgent> accAgents = new Vector<AccessionAgent>();
        
        accAgents.add(createAccessionAgent("donor", donor, acc1, null));
        accAgents.add(createAccessionAgent("receiver", receiver, acc1, null));
        accAgents.add(createAccessionAgent("reviewer", reviewer, acc1, null));

        Accession acc2 = createAccession(division,
                "field_work", "inprocess", "2004-PL-002", 
                DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        
        Agent donor2 =    agents.get(5);
        Agent receiver2 = agents.get(3);
        Agent reviewer2 = agents.get(1);
        
        accAgents.add(createAccessionAgent("donor", donor2, acc2, null));
        accAgents.add(createAccessionAgent("receiver", receiver2, acc2, null));
        accAgents.add(createAccessionAgent("reviewer", reviewer2, acc2, null));

        dataObjects.add(acc1);
        dataObjects.add(acc2);
        dataObjects.addAll(accAgents);
        
        persist(dataObjects);
        dataObjects.clear();
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);

        createLoanExamples(preps, agents, dataObjects);
        frame.setProcess(++createStep);

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
                                                     final SpecifyUser    user,
                                                     final CollectionChoice choice)
    {
        frame.setProcess(0, 16);
        frame.setDesc("Creating Botany...");
        
        createStep = 0;
        
        startTx();

        Division       division   = createDivision(institution, disciplineType.getName(), "Botany", "BT", "Botany");
        
        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        
        boolean buildStorageTree = false;
        if (stgTreeDef == null)
        {
            stgTreeDef        = createStorageTreeDef("Storage");
            institution.setStorageTreeDef(stgTreeDef);
            buildStorageTree = true;
        }
        
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        
        Discipline discipline = createDiscipline(division, disciplineType.getName(), disciplineType.getTitle(), 
                                                             dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, 
                                                             lithoStratTreeDef);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);
        
        persist(institution);
        persist(division);
        persist(discipline);
        
        AppContextMgr.getInstance().setClassObject(Division.class, division);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);


        loadSchemaLocalization(discipline, 
                SpLocaleContainer.CORE_SCHEMA, 
                DBTableIdMgr.getInstance(),
                choice.getCatalogNumberingFmtName(),
                choice.getAccessionNumberingFmtName());

        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           title            = initPrefs.getProperty("useragent.title",    "mr");
        String           firstName        = initPrefs.getProperty("useragent.firstname", "Test");
        String           lastName         = initPrefs.getProperty("useragent.lastname", "User");
        String           midInit          = initPrefs.getProperty("useragent.midinit", "A");
        String           abbrev           = initPrefs.getProperty("useragent.abbrev", "tu");
        String           email            = initPrefs.getProperty("useragent.email", "testuser@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", SpecifyUserTypes.UserType.Manager.toString());
        
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
        
        Pair<AutoNumberingScheme, AutoNumberingScheme> pairANS = createAutoNumberingSchemes(choice);
        AutoNumberingScheme cns         = pairANS.first;
        AutoNumberingScheme accessionNS = pairANS.second;
        
        persist(cns);
        persist(accessionNS);
        
        commitTx();
        
        startTx();

        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        Collection collection = createCollection("KUBOT", "Botany", choice.getCatalogNumberingFmtName(), cns, discipline);
        persist(collection);
        
        AppContextMgr.getInstance().setClassObject(Collection.class, collection);
        
        division.addReference(accessionNS, "numberingSchemes");
        persist(division);

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
        List<Object> geos        = createSimpleGeography(geoTreeDef, true);
        List<Object> gtps        = createSimpleGeologicTimePeriod(gtpTreeDef, true);
        List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef, true);
        
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(buildStorageTree ? createSimpleStorage(stgTreeDef) : null);
        persist(gtps);
        persist(lithoStrats);
        commitTx();
        
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
        
        startTx();
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
        Agent steveBoyd = createAgent("mr", "Steve", "D", "Boyd", "jb", "jb@net.edu");
        if (!lastName.equals("Cooper")) agents.add(createAgent("mr", "Peter", "D", "Cooper", "ds", "ds@whitehouse.gov"));
        if (!lastName.equals("Peck")) agents.add(createAgent("mr", "David", "H", "Peck", "rb", "beach@net.edu"));
        if (!lastName.equals("Appleton")) agents.add(createAgent("mrs", "Sally", "H", "Appleton", "jm", "jm@net.edu"));
        if (!lastName.equals("Brown")) agents.add(createAgent("mr", "Taylor", "C", "Brown", "kcs", "taylor.brown@ku.edu"));
        if (!lastName.equals("Boyd")) agents.add(steveBoyd);
        if (!lastName.equals("Thomas")) agents.add(createAgent("Mr", "James", "X", "Thomas", "dxt", ""));
        if (!lastName.equals("Peterson")) agents.add(createAgent("mr", "Pete", "A", "Peterson", "jb", ""));
        if (!lastName.equals("Guttenburg")) agents.add(createAgent("mr", "Mitch", "A", "Guttenburg", "jb", ""));
        if (!lastName.equals("Ford")) agents.add(createAgent("mr", "Daniel", "A", "Ford", "mas", "mas@ku.edu"));
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
        ku.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

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
        otherAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

        agents.add(otherAgent);
        
        commitTx();

        List<GroupPerson> gpList = new ArrayList<GroupPerson>();
        if (true)
        {
            startTx();
            Agent gm1 = createAgent("mr", "John", "A", "Lyon", "jal", "jal@group.edu");
            Agent gm2 = createAgent("mr", "Dave", "D", "Jones", "ddj", "ddj@group.edu");
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
            groupAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

            agents.add(groupAgent);
            
            gpList.add(createGroupPerson(groupAgent, gm1, 0, division));
            gpList.add(createGroupPerson(groupAgent, gm2, 1, division));
        }

        startTx();
        
        List<AgentVariant> agentVariants = new Vector<AgentVariant>();
        agentVariants.add(createAgentVariant(AgentVariant.VARIANT, "James Variant #1", steveBoyd));
        agentVariants.add(createAgentVariant(AgentVariant.VERNACULAR, "James VERNACULAR #1", steveBoyd));
     
        List<Address> addrs = new Vector<Address>();
        addrs.add(createAddress(agents.get(1), "1600 Pennsylvania Avenue NW", null, "Washington", "DC", "USA", "20500", 0));
        addrs.add(createAddress(agents.get(1), "??? Mississippi", null, "Lawrence", "KS", "USA", "66045", 1));
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
//        log.info("Creating determinations status");
//        current    = createDeterminationStatus(discipline, "Current",    "", DeterminationStatus.CURRENT);
//        currentAccepted    = createDeterminationStatus(discipline, "Current Accepted",    "", DeterminationStatus.CURRENTTOACCEPTED);
//        notCurrent = createDeterminationStatus(discipline, "Not current","", DeterminationStatus.NOTCURRENT);
//        incorrect  = createDeterminationStatus(discipline, "Incorrect",  "", DeterminationStatus.USERDEFINED);
//        oldDet     = createDeterminationStatus(discipline, "Old Determination","", DeterminationStatus.OLDDETERMINATION);
        
//        persist(current);
//        persist(currentAccepted);
//        persist(notCurrent);
//        persist(incorrect);
//        persist(oldDet);
        
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
        
        Vector<PrepType> prepTypesForSaving = loadPrepTypes(discipline.getType());
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
            
            CollectingEvent  ce = createFakeCollectingEvent(agents, localities.get(inx), "cut");
            CollectionObject co = createCollectionObject(catNumStr, "RSC"+Integer.toString(catNo), agents.get(agentInx), collection,  1, ce, catDates[calInx], "BuildSampleDatabase");
            Determination    dt = createDetermination(co, getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent);
            
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
                                                          final SpecifyUser    user,
                                                          final CollectionChoice choice)
    {
        frame.setProcess(0, 16);
        frame.setDesc("Creating "+disciplineType.getTitle()+"...");
        
        createStep = 0;
        
        startTx();

        Division division   = createDivision(institution, disciplineType.getName(), disciplineType.getTitle(), "INVP", disciplineType.getTitle());
        
        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        
        boolean buildStorageTree = false;
        if (stgTreeDef == null)
        {
            stgTreeDef        = createStorageTreeDef("Storage");
            institution.setStorageTreeDef(stgTreeDef);
            buildStorageTree = true;
        }
        
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        
        Discipline discipline = createDiscipline(division, disciplineType.getName(), disciplineType.getTitle(), 
                                                             dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, 
                                                             lithoStratTreeDef);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);
        
        persist(institution);
        persist(division);
        persist(discipline);
        
        AppContextMgr.getInstance().setClassObject(Division.class, division);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);


        loadSchemaLocalization(discipline, 
                SpLocaleContainer.CORE_SCHEMA, 
                DBTableIdMgr.getInstance(),
                choice.getCatalogNumberingFmtName(),
                choice.getAccessionNumberingFmtName());

        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           title            = initPrefs.getProperty("useragent.title",    "mr");
        String           firstName        = initPrefs.getProperty("useragent.firstname", "Test");
        String           lastName         = initPrefs.getProperty("useragent.lastname", "User");
        String           midInit          = initPrefs.getProperty("useragent.midinit", "A");
        String           abbrev           = initPrefs.getProperty("useragent.abbrev", "tu");
        String           email            = initPrefs.getProperty("useragent.email", "testuser@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", SpecifyUserTypes.UserType.Manager.toString());
        
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
        
//        LithoStratTreeDefItem earth     = createLithoStratTreeDefItem(lithoStratTreeDef, "Earth", 0, false);
//        LithoStratTreeDefItem superGrp  = createLithoStratTreeDefItem(earth,     "Super Group", 100, false);
//        LithoStratTreeDefItem lithoGrp  = createLithoStratTreeDefItem(superGrp,  "Litho Group", 200, false);
//        LithoStratTreeDefItem formation = createLithoStratTreeDefItem(lithoGrp,  "Formation",   300, false);
//        LithoStratTreeDefItem member    = createLithoStratTreeDefItem(formation, "Member",      400, false);
//        @SuppressWarnings("unused")
//        LithoStratTreeDefItem bed       = createLithoStratTreeDefItem(member,    "Bed",         500, true);
        
        frame.setProcess(++createStep);
        
        Pair<AutoNumberingScheme, AutoNumberingScheme> pairANS = createAutoNumberingSchemes(choice);
        AutoNumberingScheme cns         = pairANS.first;
        AutoNumberingScheme accessionNS = pairANS.second;
        
        persist(cns);
        persist(accessionNS);
        //persist(earth);
        
        commitTx();
        
        startTx();

        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        Collection collection = createCollection("KUIVP", disciplineType.getTitle(), choice.getCatalogNumberingFmtName(), cns, discipline);
        persist(collection);
        
        // create the standard user groups for this collection
        Map<String, SpPrincipal> groupMap = DataBuilder.createStandardGroups(session, collection);

        // add the administrator as a Collections Manager in this group
        user.addUserToSpPrincipalGroup(groupMap.get(SpecifyUserTypes.UserType.Manager.toString()));

        // Tester
        createAndAddTesterToCollection(session, "ivpuser", "InvertPaleo@ku.edu", "ivpuser", "mr", "Joe", "", "InvertPaleo", "",
                                       discipline, division, groupMap, "Guest");

        AppContextMgr.getInstance().setClassObject(Collection.class, collection);

        division.addReference(accessionNS, "numberingSchemes");
        persist(division);

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
        List<Object> geos        = createSimpleGeography(geoTreeDef, true);
        List<Object> gtps        = createSimpleGeologicTimePeriod(gtpTreeDef, true);
        //List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef, true);
        
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(buildStorageTree ? createSimpleStorage(stgTreeDef) : null);
        persist(gtps);
        //persist(lithoStrats);
        commitTx();
        
        
        LithoStrat earthLithoStrat = convertLithoStratFromCSV(lithoStratTreeDef);
        if (earthLithoStrat == null)
        {
            //throw new RuntimeException("No Tree");
            startTx();
            List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef, true);
            persist(lithoStrats);
            commitTx();
        }

        
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
        
        startTx();

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
        Agent steveBoyd = createAgent("mr", "Steve", "D", "Boyd", "jb", "jb@net.edu");
        if (!lastName.equals("Cooper")) agents.add(createAgent("mr", "Peter", "D", "Cooper", "ds", "ds@whitehouse.gov"));
        if (!lastName.equals("Peck")) agents.add(createAgent("mr", "David", "H", "Peck", "rb", "beach@net.edu"));
        if (!lastName.equals("Appleton")) agents.add(createAgent("mrs", "Sally", "H", "Appleton", "jm", "jm@net.edu"));
        if (!lastName.equals("Brown")) agents.add(createAgent("mr", "Taylor", "C", "Brown", "kcs", "taylor.brown@ku.edu"));
        if (!lastName.equals("Boyd")) agents.add(steveBoyd);
        if (!lastName.equals("Thomas")) agents.add(createAgent("Mr", "James", "X", "Thomas", "dxt", ""));
        if (!lastName.equals("Peterson")) agents.add(createAgent("mr", "Pete", "A", "Peterson", "jb", ""));
        if (!lastName.equals("Guttenburg")) agents.add(createAgent("mr", "Mitch", "A", "Guttenburg", "jb", ""));
        if (!lastName.equals("Ford")) agents.add(createAgent("mr", "Daniel", "A", "Ford", "mas", "mas@ku.edu"));
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
        ku.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

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
        otherAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

        agents.add(otherAgent);
        
        commitTx();

        List<GroupPerson> gpList = new ArrayList<GroupPerson>();
        if (true)
        {
            startTx();
            Agent gm1 = createAgent("mr", "John", "A", "Lyon", "jal", "jal@group.edu");
            Agent gm2 = createAgent("mr", "Dave", "D", "Jones", "ddj", "ddj@group.edu");
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
            groupAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

            
            agents.add(groupAgent);
            
            gpList.add(createGroupPerson(groupAgent, gm1, 0, division));
            gpList.add(createGroupPerson(groupAgent, gm2, 1, division));
        }

        startTx();
        
        List<Address> addrs = new Vector<Address>();
        addrs.add(createAddress(agents.get(1), "1600 Pennsylvania Avenue NW", null, "Washington", "DC", "USA", "20500", 0));
        addrs.add(createAddress(agents.get(1), "??? Mississippi", null, "Lawrence", "KS", "USA", "66045", 1));
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
            colEves[i] = createFakeCollectingEvent(agents, farmpond, "Dug");
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
        
        determs.add(createDetermination(collObjs.get(0), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(1), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(2), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(3), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(5), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(6), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(7), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        
        determs.add(createDetermination(collObjs.get(0), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, longAgo));
        determs.add(createDetermination(collObjs.get(1), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(2), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(3), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, longAgo));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, longAgo));
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
        
        Vector<PrepType> prepTypesForSaving = loadPrepTypes(discipline.getType());
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
                                         "gift", "complete", "2000-IP-001", 
                                         DateFormat.getInstance().format(calendar.getTime()), 
                                         calendar, calendar);
        acc1.setText1(disciplineType.getTitle());
        acc1.setRepositoryAgreement(repoAg);
        
        Agent donor =    agents.get(4);
        Agent receiver = agents.get(1);
        Agent reviewer = agents.get(2);
        
        List<AccessionAgent> accAgents = new Vector<AccessionAgent>();
        
        accAgents.add(createAccessionAgent("donor", donor, acc1, null));
        accAgents.add(createAccessionAgent("receiver", receiver, acc1, null));
        accAgents.add(createAccessionAgent("reviewer", reviewer, acc1, null));

        Accession acc2 = createAccession(division,
                "field_work", "inprocess", "2004-IP-002", 
                DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        
        Agent donor2 =    agents.get(5);
        Agent receiver2 = agents.get(3);
        Agent reviewer2 = agents.get(1);
        
        accAgents.add(createAccessionAgent("donor", donor2, acc2, null));
        accAgents.add(createAccessionAgent("receiver", receiver2, acc2, null));
        accAgents.add(createAccessionAgent("reviewer", reviewer2, acc2, null));

        dataObjects.add(acc1);
        dataObjects.add(acc2);
        dataObjects.addAll(accAgents);
        
        persist(dataObjects);
        dataObjects.clear();
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);

        createLoanExamples(preps, agents, dataObjects);
        frame.setProcess(++createStep);

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
        earth.setFullName("Earth");
        earth.setNodeNumber(1);
        earth.setHighestChildNodeNumber(1);
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
     * @param discipline
     * @param treeDef
     * @return
     */
    public Geography createGeographyFromGeonames(final Discipline discipline, 
                                                 final Agent      agent,
                                                 final String     itUsername,
                                                 final String     itPassword)
    {
        Geography earth = null;
        try
        {
            BuildFromGeonames bldGeoNames = new BuildFromGeonames(discipline.getGeographyTreeDef(), dateFormatter.format(now), agent, itUsername, itPassword, frame);
            
            try
            {
                startTx();
                earth = bldGeoNames.buildEarth(session);
                commitTx();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                rollbackTx();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BackupServiceFactory.class, ex);

                return null;
            }
            
            //System.err.println(DBConnection.getInstance().getDatabaseName());
            for (String nm : BasicSQLUtils.getTableNames(DBConnection.getInstance().getConnection()))
            {
                System.err.println(nm);
            }
            
            if (bldGeoNames.loadGeoNamesDB())
            {
                bldGeoNames.build(earth.getId());
            }
            
            session.refresh(earth);
            
            GeographyTreeDef geoTreeDef = discipline.getGeographyTreeDef();
            session.refresh(earth);
            
            frame.setDesc("Configuring Geography Tree...");
            
            Discipline disp = AppContextMgr.getInstance().getClassObject(Discipline.class);
            if (disp == null)
            {
                AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
            }
            geoTreeDef.updateAllNodes(earth, true, true);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return earth;
    }
    /**
     * @param treeDef
     * @return
     */
    public Geography convertGeographyFromXLS(final GeographyTreeDef treeDef)
    {
        frame.setDesc("Building Geography Tree...");

        Hashtable<String, Geography> geoHash = new Hashtable<String, Geography>();
        
        geoHash.clear();

        String fileName = "Geography.xls";
        File file = XMLHelper.getConfigDir("../demo_files/"+fileName);
        if (!file.exists())
        {
            log.error("Couldn't file[" + file.getAbsolutePath() + "] checking the config dir");
            file = XMLHelper.getConfigDir(fileName);
            if (!file.exists())
            {
                file = new File("Specify/demo_files/"+fileName);
            }
        }

        if (file == null || !file.exists())
        {
            log.error("Couldn't file[" + file.getAbsolutePath() + "]");
            return null;
        }
        
        // setup the root Geography record (planet Earth)
        Geography earth = new Geography();
        earth.initialize();
        earth.setName("Earth");
        earth.setFullName("Earth");
        earth.setNodeNumber(1);
        earth.setHighestChildNodeNumber(1);
        earth.setRankId(0);
        earth.setDefinition(treeDef);
        GeographyTreeDefItem defItem = treeDef.getDefItemByRank(0);
        earth.setDefinitionItem(defItem);

        int counter = 0;
        
        try
        {
            startTx();
            
            persist(earth);
            
            String[]        cells    = new String[4];
            InputStream     input    = new FileInputStream(file);
            POIFSFileSystem fs       = new POIFSFileSystem(input);
            HSSFWorkbook    workBook = new HSSFWorkbook(fs);
            HSSFSheet       sheet    = workBook.getSheetAt(0);
            Iterator<?>     rows     = sheet.rowIterator();
            
            int lastRowNum  = sheet.getLastRowNum();
            if (frame != null)
            {
                final int mx = lastRowNum;
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        frame.setProcess(0, mx);
                    }
                });
            }
            
            while (rows.hasNext())
            {
                if (counter == 0)
                {
                    counter = 1;
                    rows.next();
                    continue;
                }
                if (counter % 100 == 0)
                {
                    if (frame != null) frame.setProcess(counter);
                    log.info("Converted " + counter + " Geography records");
                }
                
                HSSFRow row = (HSSFRow) rows.next();
                Iterator<?> cellsIter = row.cellIterator();
                int i = 0;
                while (cellsIter.hasNext() && i < 4)
                {
                    HSSFCell cell = (HSSFCell)cellsIter.next();
                    if (cell != null)
                    {
                        cells[i] = StringUtils.trim(cell.getRichStringCellValue().getString());
                        i++;
                    }
                }
                // Sets nulls to unused cells
                for (int j=i;j<4;j++)
                {
                    cells[j] = null;
                }
                //System.out.println();
                @SuppressWarnings("unused")
                Geography newGeo = convertGeographyRecord(cells[0], cells[1], cells[2], cells[3], earth);
    
                counter++;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        if (frame != null) frame.setProcess(counter);
        
        log.info("Converted " + counter + " Geography records");
        
        frame.setDesc("Saving Geography Tree...");
        frame.getProcessProgress().setIndeterminate(true);

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
        geoHash.clear();

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
     * @param continent
     * @param country
     * @param state
     * @param county
     * @param geoRoot
     * @return
     */
    protected Geography convertGeographyRecord(final String    continent,
                                               final String    country,
                                               final String    state,
                                               final String    county,
                                               final Geography geoRoot)
    {
        String levelNames[] = { continent, country, state, county };
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

        Geography prevLevelGeo = geoRoot;
        for (int i = 0; i < levelsToBuild; ++i)
        {
            Geography newLevelGeo = buildGeographyLevel(levelNames[i], prevLevelGeo);
            prevLevelGeo = newLevelGeo;
        }

        return prevLevelGeo;
    }

    /**
     * @param nameArg
     * @param parentArg
     * @return
     */
    protected Geography buildGeographyLevel(final String    nameArg,
                                            final Geography parentArg)
    {
        String name = nameArg;
        if (name == null)
        {
            name = "N/A";
        }

        // search through all of parent's children to see if one already exists with the same name
        Set<Geography> children = parentArg.getChildren();
        for (Geography child : children)
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
        Geography newGeo = new Geography();
        newGeo.initialize();
        newGeo.setName(name);
        newGeo.setParent(parentArg);
        parentArg.addChild(newGeo);
        newGeo.setDefinition(parentArg.getDefinition());
        int newGeoRank = parentArg.getRankId() + 100;
        
        GeographyTreeDefItem defItem = parentArg.getDefinition().getDefItemByRank(newGeoRank);
        newGeo.setDefinitionItem(defItem);
        newGeo.setRankId(newGeoRank);

        persist(newGeo);
        
        return newGeo;
    }

    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public List<Object> createGenericCollection(final DisciplineType disciplineType,
                                                final Institution    institution,
                                                final SpecifyUser    user,
                                                final CollectionChoice choice,
                                                final String         method)
    {
        frame.setProcess(0, 16);
        frame.setDesc("Creating "+disciplineType.getTitle()+"...");
        
        createStep = 0;
        
        startTx();

        Division division   = createDivision(institution, disciplineType.getName(), disciplineType.getTitle(), disciplineType.getAbbrev(), disciplineType.getTitle());
        
        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        
        boolean buildStorageTree = false;
        if (stgTreeDef == null)
        {
            stgTreeDef        = createStorageTreeDef("Storage");
            institution.setStorageTreeDef(stgTreeDef);
            buildStorageTree = true;
        }
        
        Discipline discipline = createDiscipline(division, disciplineType.getName(), disciplineType.getTitle(), 
                                                             dataType, taxonTreeDef, geoTreeDef, gtpTreeDef, 
                                                             lithoStratTreeDef);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);
        
        persist(institution);
        persist(division);
        persist(discipline);
        
        AppContextMgr.getInstance().setClassObject(Division.class, division);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);


        frame.setDesc("Loading Schema...");
        
        loadSchemaLocalization(discipline, 
                SpLocaleContainer.CORE_SCHEMA, 
                DBTableIdMgr.getInstance(),
                choice.getCatalogNumberingFmtName(),
                choice.getAccessionNumberingFmtName());

        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           title            = initPrefs.getProperty("useragent.title",    "mr");
        String           firstName        = initPrefs.getProperty("useragent.firstname", "Test");
        String           lastName         = initPrefs.getProperty("useragent.lastname", "User");
        String           midInit          = initPrefs.getProperty("useragent.midinit", "A");
        String           abbrev           = initPrefs.getProperty("useragent.abbrev", "tu");
        String           email            = initPrefs.getProperty("useragent.email", "testuser@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", SpecifyUserTypes.UserType.Manager.toString());
        
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
        
        Pair<AutoNumberingScheme, AutoNumberingScheme> pairANS = createAutoNumberingSchemes(choice);
        AutoNumberingScheme cns         = pairANS.first;
        AutoNumberingScheme accessionNS = pairANS.second;        
        
        persist(cns);
        persist(accessionNS);
        
        commitTx();
        
        startTx();

        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        frame.setDesc("Creating a Collection");
        
        Collection collection = createCollection("KU", disciplineType.getTitle(), 
                                                 choice.getCatalogNumberingFmtName(), 
                                                 cns, discipline, disciplineType.isEmbeddedCollecingEvent());
        persist(collection);
        
        // create the standard user groups for this collection
        Map<String, SpPrincipal> groupMap = DataBuilder.createStandardGroups(session, collection);

        // add the administrator as a Collections Manager in this group
        user.addUserToSpPrincipalGroup(groupMap.get(SpecifyUserTypes.UserType.Manager.toString()));

        // Tester
        String dspAbbrev = disciplineType.getAbbrev();
        createAndAddTesterToCollection(session, dspAbbrev+"Tester", dspAbbrev+"tester@brc.ku.edu", dspAbbrev+"Tester", 
                "", dspAbbrev, "", "Tester", "", discipline, division, groupMap, "Guest");

        AppContextMgr.getInstance().setClassObject(Collection.class, collection);
        
        division.addReference(accessionNS, "numberingSchemes");
        persist(division);

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
        frame.setDesc("Building Trees...");
        Vector<Object> taxa = new Vector<Object>();
        createTaxonTreeFromXML(taxa, taxonTreeDef, disciplineType);
        
        boolean isPaleo = disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.paleobotany ||
                          disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.vertpaleo ||
                          disciplineType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.invertpaleo;
        
        if (isPaleo)
        {
//            LithoStratTreeDefItem earth     = createLithoStratTreeDefItem(lithoStratTreeDef, "Earth", 0, false);
//            LithoStratTreeDefItem superGrp  = createLithoStratTreeDefItem(earth,     "Super Group", 100, false);
//            LithoStratTreeDefItem lithoGrp  = createLithoStratTreeDefItem(superGrp,  "Litho Group", 200, false);
//            LithoStratTreeDefItem formation = createLithoStratTreeDefItem(lithoGrp,  "Formation",   300, false);
//            LithoStratTreeDefItem member    = createLithoStratTreeDefItem(formation, "Member",      400, false);
//            @SuppressWarnings("unused")
//            LithoStratTreeDefItem bed       = createLithoStratTreeDefItem(member,    "Bed",         500, true);
//            persist(earth);
        }
        
        List<Object> geos        = createSimpleGeography(geoTreeDef, true);
        List<Object> gtps        = createSimpleGeologicTimePeriod(gtpTreeDef, true);
        List<Object> lithoStrats = isPaleo ? null : createSimpleLithoStrat(lithoStratTreeDef, true);
        
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(buildStorageTree ? createSimpleStorage(stgTreeDef) : null);
        persist(gtps);
        
        if (lithoStrats != null)
        {
            persist(lithoStrats);
            
        }
        commitTx();

        if (isPaleo)
        {
            LithoStrat earthLithoStrat = convertLithoStratFromCSV(lithoStratTreeDef);// does startTx() / commitTx
            if (earthLithoStrat == null)
            {
                startTx();
                lithoStrats = createSimpleLithoStrat(lithoStratTreeDef, true); 
                persist(lithoStrats);
                commitTx();
            }  
        }
        
        
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
        
        startTx();
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
        frame.setDesc("Creating localities...");
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
        Agent steveBoyd = createAgent("mr", "Steve", "D", "Boyd", "jb", "jb@net.edu");
        if (!lastName.equals("Cooper")) agents.add(createAgent("mr", "Peter", "D", "Cooper", "ds", "ds@whitehouse.gov"));
        if (!lastName.equals("Peck")) agents.add(createAgent("mr", "David", "H", "Peck", "rb", "beach@net.edu"));
        if (!lastName.equals("Appleton")) agents.add(createAgent("mrs", "Sally", "H", "Appleton", "jm", "jm@net.edu"));
        if (!lastName.equals("Brown")) agents.add(createAgent("mr", "Taylor", "C", "Brown", "kcs", "taylor.brown@ku.edu"));
        if (!lastName.equals("Boyd")) agents.add(steveBoyd);
        if (!lastName.equals("Thomas")) agents.add(createAgent("Mr", "James", "X", "Thomas", "dxt", ""));
        if (!lastName.equals("Peterson")) agents.add(createAgent("mr", "Pete", "A", "Peterson", "jb", ""));
        if (!lastName.equals("Guttenburg")) agents.add(createAgent("mr", "Mitch", "A", "Guttenburg", "jb", ""));
        if (!lastName.equals("Ford")) agents.add(createAgent("mr", "Daniel", "A", "Ford", "mas", "mas@ku.edu"));
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
        ku.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

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
        otherAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

        agents.add(otherAgent);
        
        commitTx();

        frame.setDesc("Group Persons...");
        List<GroupPerson> gpList = new ArrayList<GroupPerson>();
        if (true)
        {
            startTx();
            Agent gm1 = createAgent("mr", "John", "A", "Lyon", "jal", "jal@group.edu");
            Agent gm2 = createAgent("mr", "Dave", "D", "Jones", "ddj", "ddj@group.edu");
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
            groupAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

            agents.add(groupAgent);
            
            gpList.add(createGroupPerson(groupAgent, gm1, 0, division));
            gpList.add(createGroupPerson(groupAgent, gm2, 1, division));
        }

        startTx();
        
        List<Address> addrs = new Vector<Address>();
        addrs.add(createAddress(agents.get(1), "1600 Pennsylvania Avenue NW", null, "Washington", "DC", "USA", "20500", 0));
        addrs.add(createAddress(agents.get(1), "??? Mississippi", null, "Lawrence", "KS", "USA", "66045", 1));
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
        frame.setDesc("Creating collecting events, collectors and a collecting trip...");
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
        frame.setDesc("Creating a permit...");

        Calendar issuedDate = Calendar.getInstance();
        issuedDate.set(1993, 1, 12);
        Calendar startDate = Calendar.getInstance();
        startDate.set(1993, 2, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(1993, 5, 30);
        
        Permit permit = createPermit("1980-"+disciplineType.getAbbrev().substring(0, 2)+"-0001", "US Dept Wildlife", issuedDate, startDate, endDate, null);
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
        // collection objects
        ////////////////////////////////
        log.info("Creating collection objects");
        frame.setDesc("Creating collection objects...");

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
            colEves[i] = createFakeCollectingEvent(agents, farmpond, method);
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
        frame.setDesc("Creating determinations...");

        List<Determination> determs = new Vector<Determination>();
        Calendar recent = Calendar.getInstance();
        recent.set(2005, 10, 27, 13, 44, 00);
        Calendar longAgo = Calendar.getInstance();
        longAgo.set(1976, 01, 29, 8, 12, 00);
        Calendar whileBack = Calendar.getInstance(); 
        whileBack.set(2000, 7, 4, 9, 33, 12);
        
        determs.add(createDetermination(collObjs.get(0), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(1), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(2), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(3), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(5), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(6), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        determs.add(createDetermination(collObjs.get(7), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), true, recent));
        
        determs.add(createDetermination(collObjs.get(0), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, longAgo));
        determs.add(createDetermination(collObjs.get(1), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(2), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(3), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, whileBack));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, longAgo));
        determs.add(createDetermination(collObjs.get(4), getRandomAgent(agents), getRandomTaxon(TaxonTreeDef.SPECIES, taxa), false, longAgo));
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
        frame.setDesc("Creating preparations...");

        Vector<PrepType> prepTypesForSaving = loadPrepTypes(discipline.getType());
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
        frame.setDesc("Creating accessions...");

        calendar.set(2006, 10, 27, 23, 59, 59);
        Accession acc1 = createAccession(division,
                                         "gift", "complete", "2000-"+disciplineType.getAbbrev().substring(0, 2)+"-001", 
                                         DateFormat.getInstance().format(calendar.getTime()), 
                                         calendar, calendar);
        acc1.setText1(disciplineType.getTitle());
        acc1.setRepositoryAgreement(repoAg);
        
        Agent donor =    agents.get(4);
        Agent receiver = agents.get(1);
        Agent reviewer = agents.get(2);
        
        List<AccessionAgent> accAgents = new Vector<AccessionAgent>();
        
        accAgents.add(createAccessionAgent("donor", donor, acc1, null));
        accAgents.add(createAccessionAgent("receiver", receiver, acc1, null));
        accAgents.add(createAccessionAgent("reviewer", reviewer, acc1, null));

        Accession acc2 = createAccession(division,
                "field_work", "inprocess", "2004-"+disciplineType.getAbbrev().substring(0, 2)+"-002", 
                DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        
        Agent donor2 =    agents.get(5);
        Agent receiver2 = agents.get(3);
        Agent reviewer2 = agents.get(1);
        
        accAgents.add(createAccessionAgent("donor", donor2, acc2, null));
        accAgents.add(createAccessionAgent("receiver", receiver2, acc2, null));
        accAgents.add(createAccessionAgent("reviewer", reviewer2, acc2, null));

        dataObjects.add(acc1);
        dataObjects.add(acc2);
        dataObjects.addAll(accAgents);
        
        persist(dataObjects);
        dataObjects.clear();
        commitTx();
        
        startTx();
        frame.setProcess(++createStep);

        createLoanExamples(preps, agents, dataObjects);
        frame.setProcess(++createStep);

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
                                            final File           domFile,
                                            final StorageTreeDef treeDef,
                                            final boolean        doAddTreeNodes) throws Exception
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
        
        createStorageTreeFromXML(storageList, root, treeDef, doAddTreeNodes);

    }
    
    @SuppressWarnings("unchecked")
    protected void createStorageTreeFromXML(final Vector<Object> storageList, 
                                            final Element        root,
                                            final StorageTreeDef treeDef,
                                            final boolean doAddTreeNodes)
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
        storage.setFullName("Storage Root");
        storage.setNodeNumber(1);
        storage.setHighestChildNodeNumber(1);
        storage.setDefinition(treeDef);
        storage.setDefinitionItem(rootTTD);
        rootTTD.getTreeEntries().add(storage);
        storage.setParent(null);
        persist(storage);
        storageList.add(storage);
        
        if (doAddTreeNodes)
        {
            for (Element node : (List<Element>)root.selectNodes("/tree/nodes/node"))
            {
                traverseTree(storageList, treeDefItemHash, treeDef, node, storage);
            }
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
        storage.setFullName(name); 
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
        if (root != null)
        {
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
        taxon.setFullName("Taxonomy Root");
        taxon.setNodeNumber(1);
        taxon.setHighestChildNodeNumber(1);
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
        taxon.setFullName(name);
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
        return XMLHelper.readDOMFromConfigDir(dType.getFolder()+ File.separator + fileName);
    }
    
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public void createFishCollection(final DisciplineType   disciplineType,
                                     final Institution      institution,
                                     final SpecifyUser      user,
                                     final CollectionChoice collChoice)
    {
        frame.setDesc("Creating Fish Collection Overhead...");
        
        startTx();
        
        Division division = createDivision(institution, disciplineType.getName(), "Ichthyology", "IT", "Ichthyology");
        persist(division);
        
        // create tree defs (later we will make the def items and nodes)
        TaxonTreeDef              taxonTreeDef      = createTaxonTreeDef("Taxon");
        GeographyTreeDef          geoTreeDef        = createGeographyTreeDef("Geography");
        GeologicTimePeriodTreeDef gtpTreeDef        = createGeologicTimePeriodTreeDef("Chronos Stratigraphy");
        LithoStratTreeDef         lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
        lithoStratTreeDef.setRemarks("A simple super, group, formation, member, bed Litho Stratigraphy tree");
        boolean buildStorageTree = false;
        if (stgTreeDef == null)
        {
            stgTreeDef = createStorageTreeDef("Storage");
            institution.setStorageTreeDef(stgTreeDef);
            buildStorageTree = true;
        }
        
        Discipline discipline = createDiscipline(division, 
                                                 disciplineType.getName(), 
                                                 disciplineType.getTitle(), 
                                                 dataType, 
                                                 taxonTreeDef, 
                                                 geoTreeDef, 
                                                 gtpTreeDef, 
                                                 lithoStratTreeDef);
        
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);        
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);
        
        persist(institution);
        persist(discipline);
        commitTx();
        
        AppContextMgr.getInstance().setClassObject(Division.class, division);
        AppContextMgr.getInstance().setClassObject(Discipline.class, discipline);
        AppContextMgr.getInstance().setClassObject(Institution.class, institution);
        
        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           title            = initPrefs.getProperty("useragent.title",    "mr");
        String           firstName        = initPrefs.getProperty("useragent.firstname", "Test");
        String           lastName         = initPrefs.getProperty("useragent.lastname", "User");
        String           midInit          = initPrefs.getProperty("useragent.midinit", "A");
        String           abbrev           = initPrefs.getProperty("useragent.abbrev", "tu");
        String           email            = initPrefs.getProperty("useragent.email", "testuser@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", SpecifyUserTypes.UserType.Manager.toString());
        
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
        discipline.addReference(userAgent, "agents");
        user.addReference(userAgent, "agents");
        

        startTx();
        persist(discipline);               
        persist(user);
        persist(userAgent);
             
        loadSchemaLocalization(discipline, 
                               SpLocaleContainer.CORE_SCHEMA, 
                               DBTableIdMgr.getInstance(),
                               collChoice.getCatalogNumberingFmtName(),
                               collChoice.getAccessionNumberingFmtName());
        commitTx();
        
        makeFieldVisible(null, discipline);
        makeFieldVisible(disciplineType.getName(), discipline);

        frame.setDesc("Creating Fish Trees...");
        
        ////////////////////////////////
        // build the tree def items and nodes
        ////////////////////////////////
        Journal      journal     = createJournalsAndReferenceWork();
        List<Object> taxa        = createSimpleFishTaxonTree(taxonTreeDef, doShallowTaxonTree);
        List<Object> geos        = createSimpleGeography(geoTreeDef, true);
        List<Object> gtps        = createSimpleGeologicTimePeriod(gtpTreeDef, true);
        List<Object> lithoStrats = createSimpleLithoStrat(lithoStratTreeDef, true);
        
        startTx();        
        persist(journal);
        persist(taxa);
        persist(geos);
        persist(buildStorageTree ? createSimpleStorage(stgTreeDef) : null);
        persist(gtps);
        persist(lithoStrats);
        commitTx(); 
        
        //frame.setProcess(++createStep);
        frame.setOverall(steps++);
        
        Collection voucher = null;
        if (isChoosen(DisciplineType.STD_DISCIPLINES.fish, false))
        {
            voucher = createFishCollection(disciplineType, discipline, user, userAgent, division,
                                            taxonTreeDef, geoTreeDef, gtpTreeDef,
                                            lithoStratTreeDef,
                                            journal, taxa, geos, gtps, lithoStrats,
                                            "KUFSH", "Fish", true, false, collChoice);
        }
        

        frame.setOverall(steps++);
        
        Collection tissue = null;
        if (isChoosen(DisciplineType.STD_DISCIPLINES.fish, true))
        {
            tissue = createFishCollection(disciplineType, discipline, user, userAgent, division,
                                            taxonTreeDef, geoTreeDef, gtpTreeDef,
                                            lithoStratTreeDef,
                                            journal, taxa, geos, gtps, lithoStrats,
                                            "KUTIS", "Fish Tissue", false, true, collChoice);
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
    
    protected CollectionChoice getChoice(final DisciplineType.STD_DISCIPLINES type, 
                                         final boolean isTissue)
    {
        if (selectedChoices != null)
        {
            for (CollectionChoice cc : selectedChoices)
            {
                if (cc.getType() == type && cc.isTissue() == isTissue)
                {
                    return cc;
                }
            }
        }
        return null;
    }
    
    /**
     * @return returns a list of preptypes read in from preptype.xml
     */
    protected Vector<PrepType> loadPrepTypes(final String discipline)
    {
        return loadPrepTypes(discipline, null, null);
    }
    
    /**
     * @return returns a list of preptypes read in from preptype.xml
     */
    @SuppressWarnings("unchecked")
    protected Vector<PrepType> loadPrepTypes(final String     discipline, 
                                             final Collection collection, 
                                             final Agent      createdByAgent)
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
        
        Collection colltn = collection     != null ? collection     : AppContextMgr.getInstance().getClassObject(Collection.class);
        Agent      agent  = createdByAgent != null ? createdByAgent : Agent.getUserAgent();
        
        DisciplineType dType = DisciplineType.getByName(discipline);
        
        File file = XMLHelper.getConfigDir(dType.getFolder() + File.separator + "preptypes.xml");
        if (file.exists())
        {
            try
            {
                prepTypes = (Vector<PrepType>)xstream.fromXML(FileUtils.readFileToString(file));
                
            } catch (Exception ex)
            {
                log.error(ex);
            }
    
            Timestamp nowTm = new Timestamp(System.currentTimeMillis());
            for (PrepType pt : prepTypes)
            {
                pt.setCreatedByAgent(agent);
                pt.setTimestampCreated(nowTm);
                pt.setCollection(colltn);
                pt.setAttributeDefs(new HashSet<AttributeDef>());
            }
            return prepTypes;
        }
        throw new RuntimeException("preptypes.xml is missing for discipline["+discipline+"]");
    }
    
    /**
     * 
     */
    protected void loadQueries()
    {
        loadQueries(XMLHelper.getConfigDirPath("common" + File.separator + "queries.xml"));
        
        String discipline = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
        loadQueries(XMLHelper.getConfigDirPath(discipline + File.separator + "queries.xml"));
    }
    
    /**
     * @param path
     */
    protected void loadQueries(final String path)
    {
        File file = new File(path);
        if (!file.exists())
        {
            return;
        }
        
        Vector<SpQuery> queries = new Vector<SpQuery>();
        try
        {
            Element root = XMLHelper.readFileToDOM4J(file);
            for (Object obj : root.selectNodes("/queries/query"))
            {
                Element el = (Element)obj;
                SpQuery query = new SpQuery();
                query.initialize();
                query.fromXML(el);
                query.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
                queries.add(query);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return;
        }
        
        // Persist out to database
        DataProviderSessionIFace localSession = null;
        try
        {
            localSession = DataProviderFactory.getInstance().createSession();
            localSession.beginTransaction();
            
            for (SpQuery query : queries)
            {
                query.setName(query.getName());
                localSession.saveOrUpdate(query);
            }
            
            localSession.commit();
            
        } catch (Exception ex)
        {
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildSampleDatabase.class, ex);
            // XXX Error dialog
            ex.printStackTrace();
            localSession.rollback();
            
        }
        finally
        {
            if (localSession != null)
            {
                localSession.close();
            }
        }
    }
    
    /**
     * @param cc
     */
    private Pair<AutoNumberingScheme, AutoNumberingScheme> createAutoNumberingSchemes(final CollectionChoice cc)
    {
        Pair<AutoNumberingScheme, AutoNumberingScheme> pair = new Pair<AutoNumberingScheme, AutoNumberingScheme>();
        String catGroup = cc.getCatNumGroup();
        if (StringUtils.isNotEmpty(catGroup))
        {
            AutoNumberingScheme catANS = getAutoNumberingScheme(CollectionObject.class, catGroup);
            if (catANS == null)
            {
                catANS = createAutoNumberingScheme(catGroup, "", cc.getCatalogNumberingFmtName(), false, CollectionObject.getClassTableId());
                addAutoNumberingScheme(CollectionObject.class, catANS);
            }
            pair.first = catANS;
        }
        
        String accGroup = cc.getAccNumGroup();
        if (StringUtils.isNotEmpty(accGroup))
        {
            AutoNumberingScheme accANS = getAutoNumberingScheme(Accession.class, accGroup);
            if (accANS == null)
            {
                accANS = createAutoNumberingScheme(accGroup, "", cc.getAccessionNumberingFmtName(), false, Accession.getClassTableId());
                addAutoNumberingScheme(Accession.class, accANS);
            }
            pair.second = accANS;
        }
        return pair;
    }
    
    /**
     * Replace DataBuilder session with current BuildSampleDatabase session and 
     * return old DataBuilder session so it can be later restored.
     * @return
     */
    /*private Session switchDataBuilderSession() 
    {
        Session oldSession = DataBuilder.getSession();
        DataBuilder.setSession(session);
        return oldSession;
    }*/
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    @SuppressWarnings("unchecked")
    public Collection createFishCollection(final DisciplineType            disciplineType,
                                           final Discipline                discipline,
                                           final SpecifyUser               user,
                                           final Agent                     userAgent,
                                           final Division                  division,                  
                                           final TaxonTreeDef              taxonTreeDef,
                                           final GeographyTreeDef          geoTreeDef,
                                           final GeologicTimePeriodTreeDef gtpTreeDef,
                                           final LithoStratTreeDef         lithoStratTreeDef,
                                           final Journal                   journal,
                                           final List<Object>              taxa,
                                           final List<Object>              geos,
                                           final List<Object>              gtps,
                                           final List<Object>              lithoStrats,
                                           final String                    colPrefix,
                                           final String                    colName,
                                           final boolean                   isVoucherCol,
                                           final boolean                   doTissues,
                                           final CollectionChoice          choice)
    {
        createStep = 0;
        frame.setProcess(0, 15);
        
        frame.setDesc("Creating Collection "+  colName);
        
        startTx();
        
        Pair<AutoNumberingScheme, AutoNumberingScheme> pairANS = createAutoNumberingSchemes(choice);
        AutoNumberingScheme cns         = pairANS.first;
        AutoNumberingScheme accessionNS = pairANS.second;
        
        persist(cns);
        persist(accessionNS);
        
        commitTx();
        
        startTx();
        
        ////////////////////////////////
        // Create Collection
        ////////////////////////////////
        log.info("Creating a Collection");
        Collection collection = createCollection(colPrefix, colName, choice.getCatalogNumberingFmtName(), cns, discipline, false);
        persist(collection);

        AppContextMgr.getInstance().setClassObject(Collection.class, collection);
        
        division.addReference(accessionNS, "numberingSchemes");
        persist(division);

        ////////////////////////////////
        // Default user groups and test user
        ////////////////////////////////
        Map<String, SpPrincipal> groupMap = DataBuilder.createStandardGroups(session, collection);
        
        // add the administrator as a Collections Manager in this group
        user.addUserToSpPrincipalGroup(groupMap.get(SpecifyUserTypes.UserType.Manager.toString()));
        persist(user);
        
        // Tester
        String userPrefix = (isVoucherCol)? "" : "Tis";
        createAndAddTesterToCollection(session, userPrefix + "FishTester", "fishtester@brc.ku.edu", userPrefix + "FishTester", 
                "", "Fish", "", "Tester", "", discipline, division, groupMap, "Guest");
        
        commitTx();
        
        ////////////////////////////////
        // picklists
        ////////////////////////////////
        log.info("Creating picklists");
        
        createPickLists(session, null);
        BldrPickList colMethods = createPickLists(session, discipline);
        
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
        Locality clintonLake;
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
            
            Geography douglasKS = null;
            for (Object o : geos)
            {
                if (o instanceof Geography)
                {
                    Geography g = (Geography)o;
                    if (g.getFullName().indexOf("Douglas") == 0)
                    {
                        douglasKS = g;
                    }
                }
            }
            clintonLake   = createLocality("Clinton Lake", douglasKS);
            localities.add(clintonLake);
            globalLocalities.add(clintonLake);
            
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
            persist(clintonLake);
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
            johnByrn = createAgent("mr", "John", "D", "Byrn", "jb", "jb@net.edu");
            agents.add(createAgent("mr", "David", "D", "Smith", "ds", "ds@whitehouse.gov"));
            agents.add(createAgent("mr", "Robert", "H", "Burk", "rb", "beach@net.edu"));
            agents.add(createAgent("mrs", "Margaret", "H", "Johnson", "jm", "jm@net.edu"));
            agents.add(createAgent("mr", "Kip", "C", "Spencer", "kcs", "kip@ku.edu"));
            agents.add(johnByrn);
            agents.add(createAgent("sir", "Dudley", "X", "Thompson", "dxt", ""));
            agents.add(createAgent("mr", "Joe", "A", "Campbell", "jb", ""));
            agents.add(createAgent("mr", "Joe", "A", "Tester", "jb", ""));
            agents.add(createAgent("mr", "Mitch", "A", "Smyth", "mas", "mas@ku.edu"));
            agents.add(userAgent);
            
            ku.initialize();
            ku.setAbbreviation("KU");
            ku.setAgentType(Agent.ORG);
            ku.setLastName("University of Kansas");
            ku.setEmail("webadmin@ku.edu");
            ku.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
            ku.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
            AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(ku);
            ku.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));

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
            otherAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
            agents.add(otherAgent);

            List<GroupPerson> gpList = new ArrayList<GroupPerson>();
            if (true)
            {
                startTx();
                Agent gm1 = createAgent("mr", "John", "A", "Lyon", "jal", "jal@group.edu");
                Agent gm2 = createAgent("mr", "Dave", "D", "Jones", "ddj", "ddj@group.edu");
                persist(gm1);
                persist(gm2);
                commitTx();
                
                Discipline dsp = AppContextMgr.getInstance().getClassObject(Discipline.class);
                
                Agent groupAgent = new Agent();
                groupAgent.initialize();
                groupAgent.setAbbreviation("GRP");
                groupAgent.setAgentType(Agent.GROUP);
                groupAgent.setLastName("The Group");
                groupAgent.setEmail("group@group.com");
                groupAgent.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
                groupAgent.getDisciplines().add(dsp);
                groupAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
                dsp.getAgents().add(groupAgent);
                
                agents.add(groupAgent);
                
                gpList.add(createGroupPerson(groupAgent, gm1, 0, division));
                gpList.add(createGroupPerson(groupAgent, gm2, 1, division));
            }
            
            globalAgents.addAll(agents);
            
            List<AgentVariant> agentVariants = new Vector<AgentVariant>();
            agentVariants.add(createAgentVariant(AgentVariant.VARIANT, "John Variant #1", johnByrn));
            agentVariants.add(createAgentVariant(AgentVariant.VERNACULAR, "John VERNACULAR #1", johnByrn));
         
            List<Address> addrs = new Vector<Address>();
            addrs.add(createAddress(agents.get(1), "1600 Pennsylvania Avenue NW", null, "Washington", "DC", "USA", "20500", 0));
            addrs.add(createAddress(agents.get(1), "??? Mississippi", null, "Lawrence", "KS", "USA", "66045", 1));
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

        int[] mn = {31,28,31,30,31,30,31,31,30,31,30,31};
        
        Vector<CollectingEvent> ceList = new Vector<CollectingEvent>();
        boolean oldWay = false;
        if (!oldWay)
        {
            int monInx = rand.nextInt(12);
            int dayInx = rand.nextInt(mn[monInx]);
            calendar.set(1990 + rand.nextInt(15), monInx+1, dayInx+1, rand.nextInt(24), rand.nextInt(60), rand.nextInt(60));
            stationFieldNumber = String.format(STATION_FIELD_FORMAT, stationFieldNumberCounter++);
            
            Collector collector = null;
            int coltrInx = rand.nextInt(4);
            switch (coltrInx)
            {
                case 0 : collector = collectorMitch;break;
                case 1 : collector = collectorJim;break;
                case 2 : collector = collectorMeg;break;
                case 3 : collector = collectorRod;break;
                default:
                    collector = collectorRod;break;
            }
            Locality        loc = globalLocalities.get(rand.nextInt(globalLocalities.size()));
            CollectingEvent ce  = createCollectingEvent(loc, calendar, stationFieldNumber, new Collector[]{collector});
            //ce1.setStartDateVerbatim("19 Mar 1993, 11:56 AM");
            ceList.add(ce);
            dataObjects.add(ce);
        }
        
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
        // collection objects
        ////////////////////////////////
        log.info("Creating collection objects");

        List<DNASequence>      dnaObjs  = new Vector<DNASequence>();
        List<CollectionObject> collObjs = new Vector<CollectionObject>();
        Collection      col     = collection;
        
        Calendar[] catDates = new Calendar[oldWay ? 8 : 50];
        for (int i=0;i<catDates.length;i++)
        {
            catDates[i] = Calendar.getInstance();
            catDates[i].set(catDates[i].get(Calendar.YEAR), 01, 12 + i);
        }
        
        String prefix = "000000";
        if (oldWay)
        {
            collObjs.add(createCollectionObject(prefix + "100", "RSC100", agents.get(0), col,  3, ce1, catDates[0], "BuildSampleDatabase"));
            collObjs.add(createCollectionObject(prefix + "101", "RSC101", agents.get(0), col,  2, ce1, catDates[1], "BuildSampleDatabase"));
            collObjs.add(createCollectionObject(prefix + "102", "RSC102", agents.get(1), col,  7, ce1, catDates[2], "BuildSampleDatabase"));
            collObjs.add(createCollectionObject(prefix + "103", "RSC103", agents.get(1), col, 12, ce1, catDates[3], "BuildSampleDatabase"));
            collObjs.add(createCollectionObject(prefix + "104", "RSC104", agents.get(2), col,  8, ce2, catDates[4], "BuildSampleDatabase"));
            collObjs.add(createCollectionObject(prefix + "105", "RSC105", agents.get(2), col,  1, ce2, catDates[5], "BuildSampleDatabase"));
            collObjs.add(createCollectionObject(prefix + "106", "RSC106", agents.get(2), col,  1, ce2, catDates[6], "BuildSampleDatabase"));
            collObjs.add(createCollectionObject(prefix + "107", "RSC107", agents.get(3), col,  1, ce2, catDates[7], "BuildSampleDatabase"));
        } else
        {
            for (int i=0;i<catDates.length;i++)
            {
                Integer catNum = i + 100;
                int agentInx = rand.nextInt(agents.size());
                CollectingEvent ce = ceList.get(rand.nextInt(ceList.size()));
                collObjs.add(createCollectionObject(prefix + catNum, "RSC"+catNum, agents.get(agentInx), col,  rand.nextInt(12)+1, ce, catDates[i], "BuildSampleDatabase"));
            }
            
/*
            Comp. A :   148      
            Comp. G :   131      
            Comp. C :   199      
            Comp. T :   174      
            Ambiguous :     0    
        123456789012345678901234567890123456789012345678901234567890123456789012345
        CCTGTATTTAGTATTTGGTGCCTGAGCAGGCATAGTCGGCACAGCCCTCAGCCTTCTGATCCGTGCCGAACTGAG
        CCAACCCGGTGCCCTGCTTGGCGATGATCAGATCTACAATGTTATCGTCACAGCCCACGCCTTTGTCATGATTTT
        CTTTATAGTAATACCCATCATAATTGGCGGATTCGGAAACTGACTGGTCCCCCTAATAATTGGGGCCCCAGACAT
        GGCATTTCCTCGCATGAACAATATGAGCTTCTGACTCCTACCCCCATCCTTCCTACTCCTTTTAGCCTCCTCTGG
        GGTAGAGGCCGGAGCCGGCACAGGGTGAACTGTTTACCCCCCACTGGCGGGAAACCTGGCCCATGCAGGAGCCTC
        TGTAGACCTAACCATTTTCTCCCTTCACCTGGCTGGGGTTTCGTCCATTTTGGGGGCTATTAATTTTATTACCAC
        CATTATTAACATGAAACCCCCCGCAGTATCCCAATATCAGACACCTCTATTTGTGTGATCTGTATTAATCACGGC
        CGTACTTCTCCTACTATCACTGCCAGTGCTAGCTGCAGGGATCACAATGCTCCTAACAGACCGAAATTTAAACAC
        CACCTTCTTTGACCCAGCCGGAGGAGGAGACCCCATCCTCTACCAACACCTA
        */
            char[] syms = {'A', 'C', 'T', 'G', };
            
            for (int i=0;i<catDates.length;i++)
            {
                int monInx = rand.nextInt(12);
                int dayInx = rand.nextInt(mn[monInx]);
                Calendar cal = Calendar.getInstance();
                cal.set(2006 + rand.nextInt(3), monInx+1, dayInx+1, rand.nextInt(24), rand.nextInt(60), rand.nextInt(60));

                DNASequence dna = new DNASequence();
                dna.initialize();
                
                //dna.setSeqDate(cal); //moved to DNASequencingRun
                dna.setCollectionMemberId(collObjs.get(i).getCollectionMemberId());
                dna.setCollectionObject(collObjs.get(i));
                //dna.setGeneName("COI5'");
                int agentInx = rand.nextInt(agents.size());
                dna.setCreatedByAgent(agents.get(agentInx));
                dna.setSequencer(agents.get(agentInx));
                StringBuilder sb = new StringBuilder();
                for (int j=0;j<((8*75)+52);j++)
                {
                    sb.append(syms[rand.nextInt(syms.length)]);
                }
                dna.setGeneSequence(sb.toString());
                //dna.setPcrPrimerFwd("C_VF1LFt1"); //moved to DNASequencingRun (sort of)
                //dna.setPcrPrimerRev("C_VR1LRt1"); //moved to DNASequencingRun (sort of)
                //dna.setProcessIdentifier("M13R"); //moved to DNASequencingRun (sort of)
                if (rand.nextInt(3) < 2)
                {
                    //dna.setBarCodeIdent(String.format("NOSMF%03d-%d02", rand.nextInt(1000), i)); //moved to DNASequencingRun (sort of)
                    Calendar submDate = (Calendar)cal.clone();
                    submDate.add(Calendar.DAY_OF_MONTH, 12);
                    //dna.setSubmissionDate(submDate);
                }
                dnaObjs.add(dna);
            }
        }
        AttributeDef colObjAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "MoonPhase", discipline, null);//meg added cod
        colObjAttrDef.setDiscipline(discipline);
        discipline.getAttributeDefs().add(colObjAttrDef);
        
        CollectionObjectAttr colObjAttr = createCollectionObjectAttr(collObjs.get(0), colObjAttrDef, "Full", null);
        dataObjects.add(colObjAttrDef);
        dataObjects.addAll(collObjs);
        dataObjects.addAll(dnaObjs);
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
        if (oldWay)
        {
            determs.add(createDetermination(collObjs.get(0), agents.get(0), (Taxon)taxa.get(baseInx+1), true, recent));
            determs.add(createDetermination(collObjs.get(1), agents.get(0), (Taxon)taxa.get(baseInx+2), true, recent));
            determs.add(createDetermination(collObjs.get(2), agents.get(0), (Taxon)taxa.get(baseInx+3), true, recent));
            determs.add(createDetermination(collObjs.get(3), agents.get(0), (Taxon)taxa.get(baseInx+4), true, recent));
            determs.add(createDetermination(collObjs.get(4), agents.get(0), (Taxon)taxa.get(baseInx+5), true, recent));
            determs.add(createDetermination(collObjs.get(5), agents.get(0), (Taxon)taxa.get(baseInx+6), true, recent));
            determs.add(createDetermination(collObjs.get(6), agents.get(3), (Taxon)taxa.get(baseInx+7), true, recent));
            determs.add(createDetermination(collObjs.get(7), agents.get(4), (Taxon)taxa.get(baseInx+8), true, recent));
            
            determs.add(createDetermination(collObjs.get(0), agents.get(0), (Taxon)taxa.get(baseInx), false, longAgo));
            determs.add(createDetermination(collObjs.get(1), agents.get(1), (Taxon)taxa.get(baseInx+7), false, whileBack));
            determs.add(createDetermination(collObjs.get(2), agents.get(1), (Taxon)taxa.get(baseInx+9), false, whileBack));
            determs.add(createDetermination(collObjs.get(3), agents.get(2), (Taxon)taxa.get(baseInx+10), false, whileBack));
            determs.add(createDetermination(collObjs.get(4), agents.get(2), (Taxon)taxa.get(baseInx+10), false, whileBack));
            determs.add(createDetermination(collObjs.get(4), agents.get(3), (Taxon)taxa.get(baseInx+13), false, longAgo));
            determs.add(createDetermination(collObjs.get(4), agents.get(4), (Taxon)taxa.get(baseInx+12), false, longAgo));
            determs.get(13).setRemarks("This determination is totally wrong.  What a foolish determination.");
        } else
        {
            for (CollectionObject co : collObjs)
            {
                determs.add(createDetermination(co, agents.get(0), (Taxon)taxa.get(baseInx+rand.nextInt(13)), true, recent));
            }
        }
        
        //startTx();
        persist(determs);
        //commitTx();
        dataObjects.clear();
        
        frame.setProcess(++createStep);
                
        ////////////////////////////////
        // preparations (prep types)
        ////////////////////////////////
        log.info("Creating preparations");
        
        Vector<PrepType> prepTypesForSaving = loadPrepTypes(discipline.getType());
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
        if (oldWay)
        {
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
        } else
        {
            for (CollectionObject co : collObjs)
            {
                preps.add(createPreparation(pt.get(0), agents.get(rand.nextInt(4)), co, (Storage)locs.get(rand.nextInt(6)+7), rand.nextInt(20)+1, prepDate));
            }
        }

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
                                         "gift", "complete", yr + "-IC-001", 
                                         DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        acc1.setText1("Ichthyology");
        acc1.setRepositoryAgreement(repoAg);
        
        Agent donor =    agents.get(4);
        Agent receiver = agents.get(1);
        Agent reviewer = agents.get(2);
        
        List<AccessionAgent> accAgents = new Vector<AccessionAgent>();
        
        accAgents.add(createAccessionAgent("donor", donor, acc1, null));
        accAgents.add(createAccessionAgent("receiver", receiver, acc1, null));
        accAgents.add(createAccessionAgent("reviewer", reviewer, acc1, null));

        Accession acc2 = createAccession(division,
                "field_work", "inprocess", yr + "-IC-002", DateFormat.getInstance().format(calendar.getTime()), calendar, calendar);
        
        Agent donor2 =    agents.get(5);
        Agent receiver2 = agents.get(3);
        Agent reviewer2 = agents.get(1);
        
        accAgents.add(createAccessionAgent("donor", donor2, acc2, null));
        accAgents.add(createAccessionAgent("receiver", receiver2, acc2, null));
        accAgents.add(createAccessionAgent("reviewer", reviewer2, acc2, null));

        dataObjects.add(acc1);
        dataObjects.add(acc2);
        dataObjects.addAll(accAgents);
        
        //startTx();
        persist(dataObjects);
        //commitTx();
        dataObjects.clear();
        
        frame.setProcess(++createStep);
        
        createLoanExamples(preps, agents, dataObjects);
        frame.setProcess(++createStep);

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
        String name = discipline.getName() + " DataSet";
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
                    if (imageIndex > -1)
                    {
                        wbRowImage= wbRow.getRowImage(imageIndex);    
                    }
                }
                catch (IOException e)
                {
                    String msg = UIRegistry.getResourceString("WB_IMG_ERR_LOAD");
                    UIRegistry.getStatusBar().setErrorMessage(msg, e);
                    log.error(msg, e);
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
        startTx();
        
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
        
        addConservatorData(agents, collObjs);
        
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
        
        //SpecifyDeleteHelper.showTableCounts("EmptyDB.txt", true);

        return collection;
    }
    
    /**
     * Creates a single disciplineType collection.
     * @param disciplineName the name of the Discipline to use
     * @param disciplineName the disciplineType name
     * @return the entire list of DB object to be persisted
     */
    public void createDisciplines(final String usernameArg,
                                  final String passwordArg)
    {
        createStep = 0;
        
        frame.setProcess(0, 4);
        
        frame.setProcess(++createStep);

        ////////////////////////////////
        // Create the really high-level stuff
        ////////////////////////////////
        String           username         = initPrefs.getProperty("useragent.username", usernameArg);
        String           email            = initPrefs.getProperty("useragent.email", "ku@ku.edu");
        String           userType         = initPrefs.getProperty("useragent.usertype", SpecifyUserTypes.UserType.Manager.toString());
        String           password         = initPrefs.getProperty("useragent.password", passwordArg);
        
        System.out.println("----- User Agent -----");
        System.out.println("Userame:   "+username);
        
        Institution institution = createInstitution("Natural History Museum");
        
        startTx();
        
        SpecifyUser specifyAdminUser = DataBuilder.createAdminGroupAndUser(session, institution,  username, email, password, userType);
        
        dataType = createDataType("Biota");
        
        persist(institution);        
        persist(dataType);
        persist(specifyAdminUser);
        commitTx();
        
        frame.setProcess(++createStep);
        
        //boolean done = false;
        if (isChoosen(DisciplineType.STD_DISCIPLINES.fish, false) ||
            isChoosen(DisciplineType.STD_DISCIPLINES.fish, true))
        {
            createFishCollection(DisciplineType.getDiscipline("fish"), institution, specifyAdminUser,
                                 getChoice(DisciplineType.STD_DISCIPLINES.fish, false));
            //done = true;
        }
        
        frame.setOverall(steps++);
        
        if (isChoosen(DisciplineType.STD_DISCIPLINES.invertpaleo, false))
        {
            createSingleInvertPaleoCollection(DisciplineType.getDiscipline("invertpaleo"), institution, specifyAdminUser, 
                                              getChoice(DisciplineType.STD_DISCIPLINES.invertpaleo, false));
            //done = true;
        }
        frame.setOverall(steps++);
        
        if (isChoosen(DisciplineType.STD_DISCIPLINES.botany, false))
        {
            if (!doHugeBotany)
            {
                createSingleBotanyCollection(DisciplineType.getDiscipline("botany"), institution, specifyAdminUser, 
                                             getChoice(DisciplineType.STD_DISCIPLINES.botany, false));
            } else
            {
                createHugeBotanyCollection(DisciplineType.getDiscipline("botany"), institution, specifyAdminUser, 
                                           getChoice(DisciplineType.STD_DISCIPLINES.botany, false));
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
                frame.setOverall(steps++);
                
                String method = null;
                switch (disp)
                {
                    case herpetology : method = "trap";
                    break;
                    case paleobotany : method = "dug";
                    break; 
                    case vertpaleo : method = "dug";
                    break;
                    case bird : method = "shot";
                    break; 
                    case mammal : method = "trap";
                    break; 
                    case insect : method = "trap";
                    break; 
                    case invertebrate : method = "trap";
                    break; 
                    default: method = "XXX";
                    break;
                }
                DisciplineType dType = DisciplineType.getDiscipline(disp);
                log.debug("Building "+dType.getName());
                if (XMLHelper.getConfigDir(dType.getFolder()+ File.separator + "taxon_init.xml").exists())
                {
                    createGenericCollection(dType, institution, specifyAdminUser,  getChoice(disp, false), method);
                }
            }
        }
        frame.setOverall(steps++);
        
    }

    /**
     * @param def
     * @return
     */
    public static List<Object> createSimpleLithoStrat(final LithoStratTreeDef def, final boolean doAddTreeNodes)
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

        planet.setTreeDef(def);
        Set<LithoStratTreeDefItem> defItems = def.getTreeDefItems();
        defItems.add(planet);
        newObjs.add(planet);

        if (doAddTreeNodes)
        {
            // setup parents
            bed.setParent(member);
            member.setParent(formation);
            formation.setParent(group);
            group.setParent(superLitho);
            superLitho.setParent(planet);
    
            // set the tree def for each tree def item
            superLitho.setTreeDef(def);
            group.setTreeDef(def);
            formation.setTreeDef(def);
            member.setTreeDef(def);
            bed.setTreeDef(def);
            
            defItems.add(superLitho);
            defItems.add(group);
            defItems.add(formation);
            defItems.add(member);
            defItems.add(bed);
            
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
            earth.setIsAccepted(true);
            newObjs.add(earth);
        }
        
        return newObjs;
    }

    /**
     * @param geoTreeDef
     * @param doAddTreeNodes
     * @return
     */
    public static List<Object> createSimpleGeography(final GeographyTreeDef geoTreeDef, final boolean doAddTreeNodes)
    {
        log.info("createSimpleGeography " + geoTreeDef.getName());

        List<Object> newObjs = new Vector<Object>();

        // create the geo tree def items
        GeographyTreeDefItem root = createGeographyTreeDefItem(null,       geoTreeDef, "   ", 0);
        root.setIsEnforced(true);
        GeographyTreeDefItem cont    = createGeographyTreeDefItem(root,    geoTreeDef, "Continent", 100);
        GeographyTreeDefItem country = createGeographyTreeDefItem(cont,    geoTreeDef, "Country", 200);
        GeographyTreeDefItem state   = createGeographyTreeDefItem(country, geoTreeDef, "State", 300);
        state.setIsInFullName(true);
        GeographyTreeDefItem county  = createGeographyTreeDefItem(state,   geoTreeDef, "County", 400);
        county.setIsInFullName(true);
        //county.setTextAfter(" Co.");

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

        Geography earth = null;
        
        if (doAddTreeNodes)
        {
            // Create the planet Earth.
            // That seems like a big task for 5 lines of code.
            earth = createGeography(geoTreeDef, null, "Earth", root.getRankId());
            // 5
            newObjs.add(earth);
            
            Geography northAmerica = createGeography(geoTreeDef, earth, "North America", cont.getRankId());
            Geography us = createGeography(geoTreeDef, northAmerica, "United States", country.getRankId());
            List<Geography> states = createGeographyChildren(geoTreeDef, us,
                    new String[] { "Kansas", "Iowa", "Nebraska" }, state.getRankId());
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
        }

        int i = 0;
        for (Object geo : newObjs)
        {
            if (geo instanceof Geography)
            {
                log.debug(i+" "+((Geography)geo).getName());
            }
            i++;
        }
        
        if (doAddTreeNodes)
        {
            TreeHelper.fixFullnameForNodeAndDescendants(earth);
            earth.setNodeNumber(1);
            fixNodeNumbersFromRoot(earth);
        }

        return newObjs;
    }


    /**
     * @param treeDef
     * @param doAddTreeNode
     * @return
     */
    public static List<Object> createSimpleGeologicTimePeriod(final GeologicTimePeriodTreeDef treeDef, 
                                                              final boolean doAddTreeNode)
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
                "Place Holder", 10.0f, 0.0f, defItemLevel0.getRankId());
        newObjs.add(level0);

        if (doAddTreeNode)
        {
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
            newObjs.add(level1);
            newObjs.add(level2);
            newObjs.add(level3_1);
            newObjs.add(level3_2);
            newObjs.add(level3_3);
        }

        TreeHelper.fixFullnameForNodeAndDescendants(level0);
        level0.setNodeNumber(1);
        fixNodeNumbersFromRoot(level0);
        
        return newObjs;
    }


    public static List<Object> createSimpleStorage(final StorageTreeDef stgTreeDef)
    {
        log.info("createSimpleStorage " + stgTreeDef.getName());

        locs = new Vector<Object>();

        StorageTreeDefItem building = createStorageTreeDefItem(null, stgTreeDef, "building", 0);
        building.setIsEnforced(true);
        StorageTreeDefItem room = createStorageTreeDefItem(building, stgTreeDef, "room", 100);
        room.setIsInFullName(true);
        StorageTreeDefItem freezer = createStorageTreeDefItem(room, stgTreeDef, "freezer", 200);
        freezer.setIsInFullName(true);
        StorageTreeDefItem shelf = createStorageTreeDefItem(freezer, stgTreeDef, "shelf", 300);
        shelf.setIsInFullName(true);

        // Create the building
        Storage dyche        = createStorage(stgTreeDef, null,         "Dyche Hall", building.getRankId());
        Storage rm606        = createStorage(stgTreeDef, dyche,        "Room 606",   room.getRankId());
        Storage freezerA     = createStorage(stgTreeDef, rm606,        "Freezer A",  freezer.getRankId());
        Storage shelf5       = createStorage(stgTreeDef, freezerA,     "Shelf 5",    shelf.getRankId());
        Storage shelf4       = createStorage(stgTreeDef, freezerA,     "Shelf 4",    shelf.getRankId());
        Storage shelf3       = createStorage(stgTreeDef, freezerA,     "Shelf 3",    shelf.getRankId());
        Storage shelf2       = createStorage(stgTreeDef, freezerA,     "Shelf 2",    shelf.getRankId());
        Storage shelf1       = createStorage(stgTreeDef, freezerA,     "Shelf 1",    shelf.getRankId());

        Storage rm701        = createStorage(stgTreeDef, dyche,        "Room 701",   room.getRankId());
        Storage freezerA_701 = createStorage(stgTreeDef, rm701,        "Freezer A",  freezer.getRankId());
        Storage shelf1_701   = createStorage(stgTreeDef, freezerA_701, "Shelf 1",    shelf.getRankId());
        
        Storage rm703        = createStorage(stgTreeDef, dyche,        "Room 703",   room.getRankId());
        Storage freezerA_703 = createStorage(stgTreeDef, rm703,        "Freezer A",  freezer.getRankId());
        Storage shelf1_703   = createStorage(stgTreeDef, freezerA_703, "Shelf 1",    shelf.getRankId());
        Storage shelf2_703   = createStorage(stgTreeDef, freezerA_703, "Shelf 2",    shelf.getRankId());
        Storage shelf3_703   = createStorage(stgTreeDef, freezerA_703, "Shelf 3",    shelf.getRankId());
        
        // 0
        locs.add(building);
        // 1
        locs.add(room);
        // 2
        locs.add(freezer);
        // 3
        locs.add(shelf);
        // 4
        locs.add(dyche);
        // 5
        locs.add(rm606);
        // 6
        locs.add(freezerA);
        // 7
        locs.add(shelf5);
        // 8
        locs.add(shelf4);
        // 9
        locs.add(shelf3);
        // 10
        locs.add(shelf2);
        // 11
        locs.add(shelf1);
        // 12
        locs.add(rm701);
        // 13
        locs.add(freezerA_701);
        // 14
        locs.add(shelf1_701);
        // 15
        locs.add(rm703);
        // 16
        locs.add(freezerA_703);
        // 17
        locs.add(shelf1_703);
        // 18
        locs.add(shelf2_703);
        // 19
        locs.add(shelf3_703);
        
        TreeHelper.fixFullnameForNodeAndDescendants(dyche);
        dyche.setNodeNumber(1);
        fixNodeNumbersFromRoot(dyche);
        
        return locs;
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

        Taxon life = createTaxon(taxonTreeDef, null, "Life", TaxonTreeDef.TAXONOMY_ROOT);
        
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
        if (oList != null)
        {
            int max = frame != null ? frame.getOrigMax() : 0;
            
            if (frame != null) frame.setProcess(0, oList.size());
            int cnt = 0;
            for (Object o: oList)
            {
                if (frame != null) frame.setProcess(++cnt);
                //System.out.println("* " + cnt + " " + o.getClass().getSimpleName());
                persist(o);
            }
            
            if (frame != null) 
            {
                frame.setProcess(oList.size());
                frame.setOrigMax(max);
            }
        }
    }


    public void startTx()
    {
        trans = session.beginTransaction();
    }


    public void commitTx()
    {
        try
        {
            
            if (trans != null)
            {
                trans.commit();
            } else
            {
                throw new RuntimeException("Transaction is null");
            }
            trans = null;
            
        } catch (Exception ex)
        {
            rollbackTx();
            
            trans = null;
            UIRegistry.showError(ex.toString());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    

    public void rollbackTx()
    {
        if (trans != null)
        {
            trans.rollback();
        } else
        {
            throw new RuntimeException("Transaction is null");
        }
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
    
    public ProgressFrame createProgressFrame(final String title)
    {
        if (frame == null)
        {
            frame = new ProgressFrame(title, "SpecifyLargeIcon");
            frame.pack();
        } 
        return frame;
    }
    
    /**
     * Creates the dialog to find out what database and what database driver to use. 
     */
    public void buildSetup(final String[] args)
    {
        boolean doEmptyBuild   = false;
        
        UIRegistry.setEmbeddedDBPath(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
        
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
                        
                    } else if (option.equals("-Dembeddeddbdir"))
                    {
                        UIRegistry.setEmbeddedDBPath(value);
                        
                    } else if (option.equals("-Dmobile"))
                    {
                        UIRegistry.setEmbeddedDBPath(UIRegistry.getDefaultMobileEmbeddedDBPath());
                    }
                }
            }
        }
        
        if (StringUtils.isEmpty(UIRegistry.getAppName()))
        {
            UIRegistry.setAppName("Specify");
        }
        
        if (hideFrame)
        {
            System.out.println("Embedded DB Path [ "+UIRegistry.getEmbeddedDBPath()+" ]");
        }
        
        // Then set this
        IconManager.setApplicationClass(Specify.class);
        
        IconManager.aliasImages("SpBuilder", // Source //$NON-NLS-1$
                                "AppIcon");  // Dest //$NON-NLS-1$
        
        createProgressFrame("Building Specify Database");
        
        System.setProperty(AppPreferences.factoryName,          "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences
        System.setProperty("edu.ku.brc.dbsupport.DataProvider", "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
        System.setProperty(SecurityMgr.factoryName,             "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");       // Needed for Tree Field Names //$NON-NLS-1$

        AppPrefsCache.setUseLocalOnly(true);
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        localPrefs.load();
        
        backstopPrefs = getInitializePrefs(null);
        
        String driverName   = backstopPrefs.getProperty("initializer.drivername",   "MySQL");
        String databaseName = backstopPrefs.getProperty("initializer.databasename", "testfish"); 
        
        Properties props = getInitializePrefs(databaseName);
        if (props.size() > 0)
        {
            initPrefs = props;
        } else
        {
            initPrefs = backstopPrefs;
        }
        
        Pair<String, String> dbUser = new Pair<String, String>(initPrefs.getProperty("initializer.dbUserName", "Specify"),
                                                               initPrefs.getProperty("initializer.dbPassword", "Specify"));
        
        Pair<String, String> saUser = new Pair<String, String>(initPrefs.getProperty("initializer.saUserName", "Master"),
                                                               initPrefs.getProperty("initializer.saPassword", "Master"));
        
        Pair<String, String> cmUser = new Pair<String, String>(initPrefs.getProperty("useragent.username", "testuser"),
                                                               initPrefs.getProperty("useragent.password", "testuser"));
        
        if (doEmptyBuild)
        {
            /*ensureDerbyDirectory(driverName);
            
            DisciplineType     disciplineType = DisciplineType.getDiscipline("fish");
            DatabaseDriverInfo driverInfo     = DatabaseDriverInfo.getDriver(driverName);
            DBConfigInfo       config         = new DBConfigInfo(driverInfo, "localhost", "WorkBench", "guest", "guest", 
                                                     "guest", "guest", "guest@ku.edu", disciplineType, 
                                                     "Institution", "Division");
            buildEmptyDatabase(config);
            */

        } else
        {
            setupDlg = new SetUpBuildDlg(databaseName, driverName, dbUser, saUser, cmUser, this);
            UIHelper.centerAndShow(setupDlg);
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
                              final Pair<String, String> dbUser, 
                              final Pair<String, String> saUser, 
                              final Pair<String, String> cmUser,
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
                    build(dbName, driverName, dbUser, saUser, cmUser);
                    
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
                DBConnection.shutdown();
                System.exit(0); // I didn't used to have to do this.

            }
        };
        worker.start();
    }
    
    /**
     * @param driverInfo
     * @param saUserName
     * @param saPassword
     * @return
     */
    public static boolean createSpecifySAUser(final String hostName,
									          final String itUsername,
									          final String itPassword,
									          final String saUsername,
									          final String saPassword,
                                              final String databaseName)
    {
		DBMSUserMgr mgr = DBMSUserMgr.getInstance();
        
        mgr.setHostName(hostName);
        
        boolean isOK = false;
        if (mgr.connectToDBMS(itUsername, itPassword, hostName))
        {
            if (!mgr.doesUserExists(saUsername))
            {
                isOK = mgr.createUser(saUsername, saPassword, databaseName, DBMSUserMgr.PERM_ALL_BASIC);
            } else
            {
                isOK = true;
            }
        } else
        {
            // No Connect Error
            isOK = false;
        }
        mgr.close();
        return isOK;
    }
    
    /** 
     * Drops, Creates and Builds the Database.
     * 
     * @throws SQLException
     * @throws IOException
     */
    public boolean buildEmptyDatabase(final Properties props)
    {
        createProgressFrame("Building Specify Database");

        final String dbName = props.getProperty("dbName");
        
        frame.adjustProgressFrame();
        
        frame.setTitle("Building Specify Database");
        if (!hideFrame)
        {
            UIHelper.centerWindow(frame);
            frame.setVisible(true);
            ImageIcon imgIcon = IconManager.getIcon("AppIcon", IconManager.IconSize.Std16);
            if (imgIcon != null)
            {
                frame.setIconImage(imgIcon.getImage());
            }
            
        } else
        {
            System.out.println("Building Specify Database Username["+props.getProperty("dbUserName")+"]");
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
        
        DatabaseDriverInfo driverInfo = (DatabaseDriverInfo)props.get("driver");
        
        try
        {
            if (hideFrame) System.out.println("Creating schema");
            
            String itUsername = props.getProperty("dbUserName");
            String itPassword = props.getProperty("dbPassword");
            
            boolean doBuild = true;
            if (doBuild)
            {
                SpecifySchemaGenerator.generateSchema(driverInfo, 
                                                      props.getProperty("hostName"),
                                                      dbName, 
                                                      itUsername, 
                                                      itPassword);
            }
            
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
            
            String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, 
                                                         props.getProperty("hostName"), 
                                                         dbName);
            if (connStr == null)
            {
                connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, props.getProperty("hostName"),  dbName);
            }
            
            if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                                    driverInfo.getDialectClassName(), 
                                    dbName, 
                                    connStr, 
                                    itUsername, 
                                    itPassword))
            {
                if (hideFrame) System.out.println("Login Failed!");
                return false;
            }
            
            String saUserName = props.getProperty("saUserName"); // Master Username
            String saPassword = props.getProperty("saPassword"); // Master Password
            
            createSpecifySAUser(props.getProperty("hostName"), itUsername, itPassword, saUserName, saPassword, dbName);

            if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                                   driverInfo.getDialectClassName(), 
                                   dbName, 
                                   connStr, 
                                   saUserName, 
                                   saPassword))
            {
                if (hideFrame) System.out.println("Login Failed!");
                return false;
            }   
            
            
            setSession(HibernateUtil.getCurrentSession());
            //DataBuilder.setSession(session);
            
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
            
            createEmptyInstitution(props, true, true);

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
            
            HibernateUtil.getCurrentSession().close();
            
            if (hideFrame) System.out.println("Done.");
            
            frame.setVisible(false);
            frame.dispose();
            
            SpecifyDeleteHelper.showTableCounts("EmptyDB.txt", true);
            
            return true;
            
        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * @param dataObjects
     */
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
                         final Pair<String, String> dbUser, 
                         final Pair<String, String> saUser, 
                         final Pair<String, String> cmUser) throws SQLException
    {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        Dimension size = frame.getSize();
        size.width = Math.max(size.width, 500);
        frame.setSize(size);
        frame.setTitle("Building Specify Database");
        if (!hideFrame)
        {
            UIHelper.centerWindow(frame);
            frame.setVisible(true);
            
            ImageIcon imgIcon = IconManager.getIcon("AppIcon", IconManager.IconSize.Std16);
            if (imgIcon != null)
            {
                frame.setIconImage(imgIcon.getImage());
            }
        }
        
        frame.setProcessPercent(true);
        frame.setOverall(0, 7 + this.selectedChoices.size());
        frame.getCloseBtn().setVisible(false);

        
        String databaseHost = initPrefs.getProperty("initializer.host", "localhost");
        
        frame.setTitle("Building -> Database: "+ dbName + " Driver: "+ driverName + " User: "+cmUser.first);

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
        
        String newConnStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHost, dbName, saUser.first, saUser.second, driverInfo.getName());
        DBConnection.checkForEmbeddedDir(newConnStr);
        
        if (DBConnection.isEmbedded(newConnStr))
        {
            try
            {
                Class.forName(driverInfo.getDriverClassName());
                
                DBConnection testDB = DBConnection.createInstance(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), dbName, newConnStr, saUser.first, saUser.second);
                
                testDB.getConnection();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            DBConnection.getInstance().setDatabaseName(null);
        }
                
        SpecifySchemaGenerator.generateSchema(driverInfo, databaseHost, dbName, dbUser.first, dbUser.second);

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
                              saUser.first, 
                              saUser.second))
        {
            createSpecifySAUser(databaseHost, dbUser.first, dbUser.second, saUser.first, saUser.second, dbName);
            
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
                    //DataBuilder.setSession(session);
                    
                    createDisciplines(cmUser.first, cmUser.second);

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
                    
                    frame.setDesc("Build Completed.");
                    frame.setOverall(steps++);

                    assignPermssions();
                    
                    log.info("Done");
                }
                catch(Exception e)
                {
                    e.printStackTrace();
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
            log.debug("Couldn't find Init Prefs ["+initFile.getAbsolutePath()+"]");
            
        } catch (Exception ex)
        {
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildSampleDatabase.class, ex);
            System.err.println(ex); // XXX Error Dialog
        }
        return new Properties();
    }
    
   
    /**
     * @param tableName
     * @param memoryItem
     * @param newItem
     * @param hideGenericFields
     */
    public static void loadLocalization(final String                tableName, 
                                        final SpLocaleContainerItem memoryItemArg, 
                                        final SpLocaleContainerItem newItem,
                                        final SpLocaleContainerItem dispItem,
                                        final boolean               hideGenericFields,
                                        final boolean               isFish)
    {
        SpLocaleContainerItem memoryItem = dispItem != null ? dispItem : memoryItemArg;
        
        String itemName = memoryItem.getName();
        newItem.setName(itemName);
        
        newItem.setType(memoryItem.getType());
        newItem.setFormat(memoryItem.getFormat());
        newItem.setIsUIFormatter(memoryItem.getIsUIFormatter());
        newItem.setPickListName(memoryItem.getPickListName());
        newItem.setWebLinkName(memoryItem.getWebLinkName());
        newItem.setIsHidden(memoryItem.getIsHidden());
        newItem.setIsRequired(memoryItem.getIsRequired());

        for (SpLocaleItemStr nm : memoryItem.getNames())
        {
            SpLocaleItemStr str = new SpLocaleItemStr();
            str.initialize();
            
            String title = nm.getText();
            if (!isFish && title.equals("Collecting Event"))
            {
                title = "Collecting Information";
            }
            str.setText(title);
            
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
    public static void loadLocalization(final Integer           disciplineId,
                                        final String            disciplineName,
                                        final SpLocaleContainer memoryContainer, 
                                        final SpLocaleContainer newContainer,
                                        final boolean           hideGenericFields,
                                        final String            catFmtName,
                                        final String            accFmtName,
                                        final boolean           isDoingUpdate,
                                        final DataProviderSessionIFace session)
    {
        
        boolean isColObj          = memoryContainer.getName().equals("collectionobject");
        boolean isAccession       = memoryContainer.getName().equals("accession");
        boolean isCollectingEvent = memoryContainer.getName().equals("collectingevent");
        boolean isFish            = disciplineName.equals("fish");

        if (newContainer.getId() == null)
        {
            newContainer.setName(memoryContainer.getName());
            newContainer.setType(memoryContainer.getType());
            newContainer.setFormat(newContainer.getFormat());
            newContainer.setIsUIFormatter(newContainer.getIsUIFormatter());
            newContainer.setPickListName(newContainer.getPickListName());
            newContainer.setWebLinkName(newContainer.getWebLinkName());
            newContainer.setIsHidden(newContainer.getIsHidden());
            //debugOn = false;
            
            if (session != null)
            {
                try
                {
                    session.saveOrUpdate(newContainer);
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
           
            for (SpLocaleItemStr nm : memoryContainer.getNames())
            {
                SpLocaleItemStr str = new SpLocaleItemStr();
                str.initialize();
                
                String title = nm.getText();
                if (isCollectingEvent && !isFish)
                {
                    title = "Collecting Information"; // I18N
                }
                str.setText(title);
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
        }
        
        Hashtable<String, SpLocaleContainerItem> dispItemHash = new Hashtable<String, SpLocaleContainerItem>();
        if (memoryContainer instanceof DisciplineBasedContainer)
        {
            DisciplineBasedContainer dbc = (DisciplineBasedContainer)memoryContainer;
            
            Set<SpLocaleContainerItem> itemsSet = dbc.getDisciplineItems(disciplineName);
            for (SpLocaleContainerItem item : itemsSet)
            {
                dispItemHash.put(item.getName(), item);
            }
        }
        
        for (SpLocaleContainerItem item : memoryContainer.getItems())
        {
            boolean okToCreate = true;
            if (isDoingUpdate)
            {
                String sql = String.format(" FROM splocalecontainer c INNER JOIN splocalecontaineritem ci ON c.SpLocaleContainerID = ci.SpLocaleContainerID WHERE ci.Name = '%s' AND c.DisciplineID = %d", item.getName(), disciplineId);
                String fullSQL = "SELECT COUNT(*)" + sql;
                //log.debug(fullSQL);
                int cnt = BasicSQLUtils.getCountAsInt(fullSQL);
                if (cnt > 0)
                {
                    okToCreate = false;
                    fullSQL = "SELECT ci.SpLocaleContainerItemID" + sql;
                    //log.debug(fullSQL);
                }
            }
            
            if (okToCreate)
            {
                //log.debug("Adding Item: "+item.getName());
                SpLocaleContainerItem newItem = new SpLocaleContainerItem();
                newItem.initialize();
                
                newContainer.getItems().add(newItem);
                newItem.setContainer(newContainer);
                
                SpLocaleContainerItem dispItem = dispItemHash.get(item.getName());
            
                loadLocalization(memoryContainer.getName(), item, newItem, dispItem, hideGenericFields, isFish);
            
                if (isColObj && catFmtName != null && item.getName().equals("catalogNumber"))
                {
                    newItem.setFormat(catFmtName);
                    newItem.setIsUIFormatter(true);
                }
                
                if (isAccession && accFmtName != null && item.getName().equals("accessionNumber"))
                {
                    newItem.setFormat(accFmtName);
                    newItem.setIsUIFormatter(true);
                }
                if (session != null)
                {
                    try
                    {
                        session.saveOrUpdate(newItem);
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * @param discipline
     * @param schemaType
     * @param tableMgr
     * @param catFmtName
     * @param accFmtName
     */
    public void loadSchemaLocalization(final Discipline   discipline, 
                                       final Byte         schemaType, 
                                       final DBTableIdMgr tableMgr,
                                       final String       catFmtName,
                                       final String       accFmtName)
    {
        loadSchemaLocalization(discipline, schemaType, tableMgr, catFmtName, accFmtName, false, null);
    }
    
    /**
     * @param discipline
     * @param schemaType
     * @param tableMgr
     * @param catFmtName
     * @param accFmtName
     */
    public void loadSchemaLocalization(final Discipline   discipline, 
                                       final Byte         schemaType, 
                                       final DBTableIdMgr tableMgr,
                                       final String       catFmtName,
                                       final String       accFmtName,
                                       final boolean      isDoingUpdate,
                                       final DataProviderSessionIFace sessionArg)
    {
        HiddenTableMgr hiddenTableMgr = new HiddenTableMgr();

        SchemaLocalizerXMLHelper schemaLocalizer = new SchemaLocalizerXMLHelper(schemaType, tableMgr);
        schemaLocalizer.load();
        
        boolean hideGenericFields = true;
        
        //loadFieldsToHideHash();
        
        String dispName = discipline.getType().toString();
        
        for (SpLocaleContainer table : schemaLocalizer.getSpLocaleContainers())
        {
            Integer spcId      = null;
            boolean okToCreate = true;
            if (isDoingUpdate)
            {
                String sql     = String.format(" FROM splocalecontainer WHERE Name = '%s' AND DisciplineID = %d", table.getName(), discipline.getId());
                String fullSQL = "SELECT COUNT(*)"+sql;
                //log.debug(fullSQL);
                int cnt = BasicSQLUtils.getCountAsInt(fullSQL);
                if (cnt > 0)
                {
                    okToCreate = false;
                    fullSQL = "SELECT SpLocaleContainerID"+sql;
                    //log.debug(fullSQL);
                    spcId = BasicSQLUtils.getCount(fullSQL);
                }
            }
            
            SpLocaleContainer container = null;
            if (okToCreate)
            {
                container = new SpLocaleContainer();
                container.initialize();
                container.setName(table.getName());
                container.setType(table.getType());
                container.setSchemaType(schemaType);
                container.setDiscipline(discipline);
                
                container.setIsHidden(hiddenTableMgr.isHidden(discipline.getType(), table.getName()));
                
                if (sessionArg != null)
                {
                    try
                    {
                        sessionArg.saveOrUpdate(container);
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                
            } else
            {
                container = (SpLocaleContainer)sessionArg.getData("FROM SpLocaleContainer WHERE id = "+spcId);
            }
            
            loadLocalization(discipline.getId(), dispName, table, container, hideGenericFields, catFmtName, accFmtName, isDoingUpdate, sessionArg);
            
            if (okToCreate)
            {
                discipline.getSpLocaleContainers().add(container);
                container.setDiscipline(discipline);
            }
        }
    }
    
    /**
     * @param discipline
     * @param collection
     * @param userAgent
     * @param schemaType
     * @param tableMgr
     */
    public void createPickLists(final Discipline   discipline, 
                                final Collection   collection,
                                final Agent        userAgent,
                                final Byte         schemaType, 
                                final DBTableIdMgr tableMgr)
    {
        SchemaLocalizerXMLHelper schemaLocalizer = new SchemaLocalizerXMLHelper(schemaType, tableMgr);
        
        // Loads and Merges the PickLists from Common and from the Discipline
        for (PickList pl : schemaLocalizer.getPickLists(discipline.getType()))
        {
            pl.setCreatedByAgent(userAgent);
            pl.setCollection(collection);
            persist(pl);
        }
    }
    
    /**
     * Make specific fields visible.
     * @param disciplineDirName the name of the directory for the Discipline
     * @param discipline the Discipline itself
     */
    public static void makeFieldVisible(final String disciplineDirName,
                                        final Discipline discipline)
    {
        final String showFieldsFileName = "show_fields.xml";
        
        String dirName        = disciplineDirName != null ? disciplineDirName + File.separator : "";
        String filePath       = XMLHelper.getConfigDirPath(dirName + showFieldsFileName);
        File   showFieldsFile = new File(filePath);
        
        if (showFieldsFile.exists())
        {
            /*try
            {
                System.out.println(FileUtils.readFileToString(showFieldsFile));
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }*/
            
            Element root = XMLHelper.readDOMFromConfigDir(dirName + showFieldsFileName);
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
    }
    
    /**
     * Looks up a table/field and sets it to be visible.
     * @param tableName the table name
     * @param fieldName the field name
     */
    protected static void setFieldVisible(final String     tableName, 
                                          final String     fieldName,
                                          final Discipline discipline)
    {
        DataProviderSessionIFace localSession = null;
        try
        {
            localSession = DataProviderFactory.getInstance().createSession();
            String sql = "FROM SpLocaleContainer as sp INNER JOIN sp.discipline as d WHERE sp.name = '" + tableName + "' AND d.id = "+discipline.getId();
            //System.err.println(sql);
            Object[] cols = (Object[])localSession.getData(sql);
            if (cols != null && cols.length > 0)
            {
                SpLocaleContainer container = (SpLocaleContainer)cols[0];
                if (container != null)
                {
                    for (SpLocaleContainerItem item : container.getItems())
                    {
                        //System.out.println(fieldName+" "+ item.getName());
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
            } else
            {
                System.err.println("Couldn't find Table ["+tableName+"] for discipline["+discipline.getId()+"]");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildSampleDatabase.class, ex);
            
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
    	System.setProperty(DBMSUserMgr.factoryName, "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");
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
        
        if (StringUtils.isEmpty(UIRegistry.getAppName()))
        {
            UIRegistry.setAppName("Specify");
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                BuildSampleDatabase builder = new BuildSampleDatabase();
                builder.buildSetup(args);
            }
        });
    }
    
    private File getFileForTaxon(final String fileName, final boolean usingOtherTxnFile)
    {
        if (!usingOtherTxnFile)
        {
            File file = XMLHelper.getConfigDir("../demo_files/taxonomy/"+fileName);
            log.debug(" file "+file.getAbsolutePath() +"  "+file.exists());
            if (!file.exists())
            {
                log.error("Couldn't file[" + file.getAbsolutePath() + "] checking the config dir");
                file = XMLHelper.getConfigDir(fileName);
                log.debug(" file "+file.getAbsolutePath() +"  "+file.exists());
                if (!file.exists())
                {
                    return new File("Specify/demo_files/"+fileName);
                }
            }
    
            if (file == null || !file.exists() || file.isDirectory())
            {
                log.error("Couldn't file[" + file.getAbsolutePath() + "]");
                return null;
            }
            return file;
        } 
        
        File file = new File(fileName);
        return file.exists() ? file : null;
    }
    
    /**
     * @param fileName
     * @return
     */
    public HashSet<String> getColumnNamesFromXLS(final String fileName, final boolean usingOtherTxnFile)
    {
        File file = getFileForTaxon(fileName, usingOtherTxnFile);
        if (file == null)
        {
            return null;
        }
        
        HashSet<String> nameHash = new HashSet<String>();
        try
        {
            String[]        cells    = new String[35];
            InputStream     input    = new FileInputStream(file);
            POIFSFileSystem fs       = new POIFSFileSystem(input);
            HSSFWorkbook    workBook = new HSSFWorkbook(fs);
            HSSFSheet       sheet    = workBook.getSheetAt(0);
            Iterator<?>     rows     = sheet.rowIterator();
            
            rows = sheet.rowIterator();
            if (rows.hasNext())
            {
                for (int i=0;i<cells.length;i++)
                {
                    cells[i] = null;
                }
                
                HSSFRow row = (HSSFRow) rows.next();
                Iterator<?> cellsIter = row.cellIterator();
                while (cellsIter.hasNext())
                {
                    HSSFCell cell = (HSSFCell)cellsIter.next();
                    if (cell != null)
                    {
                        nameHash.add(StringUtils.trim(cell.getRichStringCellValue().getString()));
                    }
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return nameHash;
    }
    
    /**
     * @param cell
     * @return
     */
    public String getXLSCellValueAsStr(final HSSFCell cell)
    {
        String value = null;
        // if cell is blank, set value to ""
        if (cell == null)
        {
            value = "";
        }
        else
        {
            switch (cell.getCellType())
            {
                case HSSFCell.CELL_TYPE_NUMERIC:
                    // The best I can do at this point in the app is to guess if a
                    // cell is a date.
                    // Handle dates carefully while using HSSF. Excel stores all
                    // dates as numbers, internally.
                    // The only way to distinguish a date is by the formatting of
                    // the cell. (If you
                    // have ever formatted a cell containing a date in Excel, you
                    // will know what I mean.)
                    // Therefore, for a cell containing a date, cell.getCellType()
                    // will return
                    // HSSFCell.CELL_TYPE_NUMERIC. However, you can use a utility
                    // function,
                    // HSSFDateUtil.isCellDateFormatted(cell), to check if the cell
                    // can be a date.
                    // This function checks the format against a few internal
                    // formats to decide the issue,
                    // but by its very nature it is prone to false negatives.
                    if (HSSFDateUtil.isCellDateFormatted(cell))
                    {
                        DateWrapper      scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
                        SimpleDateFormat simpDateFmt   = scrDateFormat != null && scrDateFormat.getSimpleDateFormat() != null ? scrDateFormat.getSimpleDateFormat() : sdf;
                        value = simpDateFmt.format(cell.getDateCellValue());
                    }
                    else
                    {
                        double numeric = cell.getNumericCellValue();
                        value = numFmt.format(numeric);
                    }
                    break;

                case HSSFCell.CELL_TYPE_STRING:
                    value = cell.getRichStringCellValue().getString();
                    break;

                case HSSFCell.CELL_TYPE_BLANK:
                    value = "";
                    break;

                case HSSFCell.CELL_TYPE_BOOLEAN:
                    value = Boolean.toString(cell.getBooleanCellValue());
                    break;

                default:
                    value = "";
                    log.error("unsuported cell type");
                    break;
            }
        }
        return value;
    }
    

    int recCnt = 0;
    
    /**
     * @param treeDef
     * @param fileName
     * @return
     */
    public Taxon convertTaxonFromXLS(final TaxonTreeDef treeDef, final String fileName, final boolean usingOtherTxnFile)
    {
        Hashtable<String, Taxon> taxonHash = new Hashtable<String, Taxon>();
        
        taxonHash.clear();

        File file = getFileForTaxon(fileName, usingOtherTxnFile);
        if (file == null)
        {
            return null;
        }
        
        Vector<TaxonTreeDefItem>   rankedItems = new Vector<TaxonTreeDefItem>();
        Hashtable<String, Boolean> colNames    = new Hashtable<String, Boolean>();
        for (TaxonTreeDefItem item : treeDef.getTreeDefItems())
        {
            colNames.put(item.getName().toLowerCase(), true);
            rankedItems.add(item);
        }
        
        Collections.sort(rankedItems, new Comparator<TaxonTreeDefItem>() {
            @Override
            public int compare(TaxonTreeDefItem o1, TaxonTreeDefItem o2)
            {
                return o1.getRankId().compareTo(o2.getRankId());
            }
        });
        
        Connection conn = null;
        Statement  stmt = null;
        
        TaxonTreeDefItem rootTreeDefItem = rankedItems.get(0);
        Set<Taxon>       rootKids        = rootTreeDefItem.getTreeEntries();
        Taxon            root            = rootKids.iterator().next();
        
        Vector<Pair<String, Integer>> nodeList = new Vector<Pair<String,Integer>>();
        Pair<String, Integer>         rootNode = new Pair<String, Integer>(root.getName(), root.getId());
        nodeList.add(rootNode);

        int counter     = 0;
        int numDataCols = 0; 
        try
        {
            startTx();
            
            for (TaxonTreeDefItem item : treeDef.getTreeDefItems())
            {
                persist(item);
            }
            
            persist(root);
            
            commitTx();
            
            String[]        cells    = new String[35];
            String[]        header   = new String[35];
            InputStream     input    = new FileInputStream(file);
            POIFSFileSystem fs       = new POIFSFileSystem(input);
            HSSFWorkbook    workBook = new HSSFWorkbook(fs);
            HSSFSheet       sheet    = workBook.getSheetAt(0);
            Iterator<?>     rows     = sheet.rowIterator();
            
            int lastRowNum  = sheet.getLastRowNum();
            if (frame != null)
            {
                final int mx = lastRowNum;
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        frame.setProcess(0, mx);
                    }
                });
            }
            
            conn = DBConnection.getInstance().createConnection();
            //conn.setAutoCommit(false);
            stmt = conn.createStatement();
            
            int rowCnt = 0;
            rows = sheet.rowIterator();
            while (rows.hasNext())
            {
                System.out.println(rowCnt);
                rowCnt++;
                
                for (int i=0;i<cells.length;i++)
                {
                    cells[i] = null;
                }
                
                if (counter == 0)
                {
                    HSSFRow row = (HSSFRow) rows.next();
                    Iterator<?> cellsIter = row.cellIterator();
                    int i = 0;
                    while (cellsIter.hasNext())
                    {
                        HSSFCell cell = (HSSFCell)cellsIter.next();
                        if (cell != null)
                        {
                            cells[i] = getXLSCellValueAsStr(cell);
                            header[i] = cells[i];
                            i++;
                        }
                    }
                    for (i=0;i<cells.length;i++)
                    {
                        if (cells[i] == null) break;
                        
                        if (colNames.get(cells[i].toLowerCase()) != null)
                        {
                            numDataCols = i+1;
                        } else
                        {
                            for (String key : colNames.keySet())
                            {
                                System.err.println("key["+key+"]");
                            }
                            System.err.println("Not Found: ["+cells[i].toLowerCase()+"]");
                            break;
                        }
                    }
                    loadIndexes(cells);
                    counter = 1;
                    
                    for (String hdr : header)
                    {
                        if (hdr == null) break;
                        
                        int inx = 0;
                        for (TaxonTreeDefItem item : rankedItems)
                        {
                            if (hdr.equalsIgnoreCase(item.getName()))
                            {
                                log.debug("Header: "+hdr+" -> "+inx);
                                taxonIndexes.put(hdr, inx);
                            } else
                            {
                                log.debug("Header: "+hdr+" -> skipped.");
                            }
                            inx++;
                        }
                    }
                    continue;
                }
                
                if (counter % 100 == 0)
                {
                    if (frame != null) frame.setProcess(counter);
                    //log.info("Converted " + counter + " of "+lastRowNum+" Taxon records");
                }
                
                HSSFRow row = (HSSFRow) rows.next();
                Iterator<?> cellsIter = row.cellIterator();
                int i = 0;
                while (cellsIter.hasNext() && i < cells.length)
                {
                    HSSFCell cell = (HSSFCell)cellsIter.next();
                    if (cell != null)
                    {
                        cells[i] = getXLSCellValueAsStr(cell);
                    }
                }

                convertTaxonNodes(conn, stmt, header, cells, numDataCols, rootNode, nodeList, rankedItems, root.getDefinition().getId());
    
                counter++;
            }
            
            stmt.executeUpdate("UPDATE taxon SET IsAccepted = true WHERE IsAccepted IS NULL and AcceptedID IS NULL");
            
            conn.close();
            
            input.close();
            
            if (frame != null) frame.setProcess(lastRowNum);
            
            root = (Taxon)session.createQuery("FROM Taxon WHERE id = "+root.getId()).list().get(0);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BackupServiceFactory.class, ex);
            
        } finally
        {
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (conn != null)
                {
                    conn.close();
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (frame != null)
        {
            frame.setDesc("Saving Taxon Tree...");
            frame.getProcessProgress().setIndeterminate(true);
        }
        
        NodeNumberer<Taxon,TaxonTreeDef,TaxonTreeDefItem> nodeNumberer = new NodeNumberer<Taxon,TaxonTreeDef,TaxonTreeDefItem>(root.getDefinition());
        nodeNumberer.doInBackground();
        
        //startTx();
        //TreeHelper.fixFullnameForNodeAndDescendants(root);
        //root.setNodeNumber(1);
        //fixNodeNumbersFromRoot(root);
        //commitTx();
        
        log.info("Converted " + counter + " Taxon records");

        // set up Taxon foreign key mapping for locality
        taxonHash.clear();

        return root;
    }
    
    /**
     * @param cells
     */
    private void loadIndexes(final String[] cells)
    {
        for (int i=FAMILY_COMMON_NAME;i<=SUBSPECIES_COMMON_NAME;i++)
        {
            for (int inx=0;inx<cells.length;inx++)
            {
                if (cells[inx] == null) break;

                System.out.println(cells[inx]+"  "+TaxonIndexNames[i]);
                if (cells[inx].equals(TaxonIndexNames[i]))
                {
                    System.out.println("** "+TaxonIndexNames[i]+" -> "+inx);
                    taxonExtraColsIndexes.put(TaxonIndexNames[i].toLowerCase(), inx);
                    break;
                }
            }
        }
    }
    
    /**
     * @param conn
     * @param stmt
     * @param levelNames
     * @param startIndex
     * @param numColumns
     * @param parent
     * @param nodeList
     * @param rankedItems
     * @param txTreeDefId
     * @throws SQLException
     */
    public void convertTaxonNodes(final Connection            conn,
                                  final Statement             stmt,
                                  final String[]              header,
                                  final String[]              levelNames,
                                  final int                   numColumns,
                                  final Pair<String, Integer> parent,
                                  final Vector<Pair<String, Integer>> nodeList,
                                  final Vector<TaxonTreeDefItem>      rankedItems,
                                  final int                   txTreeDefId) throws SQLException
    {
        /*
         * kingdom     phylum      class   order       superfamily family      genus       species   subspecies  species author  species source  species lsid    species common name family common name  subspecies author   subspecies source   subspecies lsid subspecies common name
           Animalia    Arthropoda  Insecta Orthoptera  Acridoidea  Acrididae   Abisares    depressus             Uvarov 1938 orthoptera.speciesfile.org  urn:lsid:catalogueoflife.org:taxon:e32007de-29c1-102b-9a4a-00304854f820:ac2008                      
         */
        String fullName = "";
        
        for (int i = 0; i < numColumns; i++)
        {
            int inx = i + 1;
            if (StringUtils.isEmpty(levelNames[i]))
            {
                break;
            }
            
            Integer depthInx = taxonIndexes.get(header[i]);
            if (depthInx == null && taxonExtraColsIndexes.get(header[i]) != null)
            {
                break;
            }
            TaxonTreeDefItem ttdi = rankedItems.get(depthInx);
            
            if (ttdi.getIsInFullName())
            {
                if (StringUtils.isNotEmpty(ttdi.getTextBefore()))
                {
                    fullName += ttdi.getTextBefore();
                }
                fullName += levelNames[i];
                if (StringUtils.isNotEmpty(ttdi.getTextAfter()))
                {
                    fullName += ttdi.getTextAfter();
                }
                if (StringUtils.isNotEmpty(ttdi.getFullNameSeparator()))
                {
                    fullName += ttdi.getFullNameSeparator();
                }
            }
            
            if (inx == nodeList.size() || !levelNames[i].equals(nodeList.get(inx).first))
            {
                for (int j=inx;j<nodeList.size();j++)
                {
                    recycler.push(nodeList.get(j));
                }
                nodeList.setSize(inx);
                Pair<String, Integer> node = createTaxonNode(conn, stmt, levelNames[i], txTreeDefId, ttdi, 
                                                             nodeList.get(i).second, fullName.trim(), levelNames);
                nodeList.add(node);
            }
        }
    }
    
    /**
     * @param conn
     * @param stmt
     * @param name
     * @param txTreeDefId
     * @param tdi
     * @param parentId
     * @param fullName
     * @param levelNames
     * @return
     * @throws SQLException
     */
    protected Pair<String, Integer> createTaxonNode(final Connection conn,
                                                    final Statement  stmt,
                                                    final String     name,
                                                    final int        txTreeDefId,
                                                    final TaxonTreeDefItem tdi,
                                                    final Integer    parentId,
                                                    final String     fullName,
                                                    final String[]   levelNames) throws SQLException
    {
        gSQLStr.setLength(0);
        gSQLStr.append("INSERT INTO taxon (Name, TaxonTreeDefID, FullName, TaxonTreeDefItemID, RankID, ParentID, TimestampCreated, Version");
        addExtraColumns(gSQLStr, tdi.getRankId(), levelNames, true);
        gSQLStr.append(") VALUES (");
        gSQLStr.append("'");
        gSQLStr.append(name);
        gSQLStr.append("',");
        gSQLStr.append(txTreeDefId);
        gSQLStr.append(",");
        gSQLStr.append('\'');
        if (StringUtils.isNotEmpty(fullName))
        {
            gSQLStr.append(fullName);
            
        } else
        {
            gSQLStr.append(name);
        }
        gSQLStr.append('\'');        
        gSQLStr.append(",");
        gSQLStr.append(tdi.getId());
        gSQLStr.append(',');
        gSQLStr.append(tdi.getRankId());
        gSQLStr.append(',');
        gSQLStr.append(parentId);
        gSQLStr.append(',');
        gSQLStr.append(nowStr);
        gSQLStr.append(",1");
        addExtraColumns(gSQLStr, tdi.getRankId(), levelNames, false);
        gSQLStr.append(")");

        
        //System.out.println(sb.toString());
        stmt.executeUpdate(gSQLStr.toString());
        
        Integer newId = BasicSQLUtils.getInsertedId(stmt);
        if (newId == null)
        {
            throw new RuntimeException("Couldn't get the Taxon's inserted ID");
        }
        
        recCnt++;
        System.out.println("rec: "+recCnt);
        
        if (recycler.size() > 0)
        {
            Pair<String, Integer> p = recycler.pop();
            p.first = name;
            p.second = newId;
            return p;
        }
        
        return new Pair<String, Integer>(name, newId);
    }
    
    /**
     * @param sb
     * @param rankId
     * @param cells
     * @param doFieldNames
     */
    protected void addExtraColumns(final StringBuilder sb, 
                                   final int           rankId,
                                   final String[]      cells,
                                   final boolean       doFieldNames)
    {
        switch (rankId)
        {
            case 140: // family
                loadTaxonFields(sb, new int[] {FAMILY_COMMON_NAME}, cells, doFieldNames);
                break;
                
            case 220: // Species
                loadTaxonFields(sb, new int[] {SPECIES_AUTHOR, SPECIES_SOURCE, SPECIES_LSID, SPECIES_COMMON_NAME}, cells, doFieldNames);
                break;
                
            case 230: // SubSpecies
                loadTaxonFields(sb, new int[] {SUBSPECIES_AUTHOR, SUBSPECIES_SOURCE, SUBSPECIES_LSID, SUBSPECIES_COMMON_NAME}, cells, doFieldNames);
                break;
                
            default:
                break;
        }

    }
    /**
     * @param taxon
     * @param indexes
     * @param cells
     */
    private void loadTaxonFields(final StringBuilder sb, final int[] indexes, final String[] cells, final boolean doFieldNames)
    {
        for (int inx : indexes)
        {
            if (TaxonIndexNames[inx] != null)
            {
                //XXX - temporary work-around for initialization problems (bug #7204) when botany sample
            	//taxon is loaded. Skipping  block if gotten is null results in common names
            	//(for botany) not being loaded.
            	Integer gotten = taxonExtraColsIndexes.get(TaxonIndexNames[inx].toLowerCase());
            	if (gotten != null)
            	{
            	
            		int index = gotten;
            		String data = cells[index];
            		if (StringUtils.isNotEmpty(data))
            		{
            			sb.append(",");
            			if (doFieldNames)
            			{
            				sb.append(TaxonFieldNames[inx]);
            			} else
            			{
            				data = StringUtils.replace(data, "'", "\\\'");
            				sb.append("'" + data + "'");    
            			}
            		}
            	}
            }
        }
    }

    
    /**
     * @param treeDef
     * @return
     */
    public GeologicTimePeriod convertChronoStratFromXLS(final GeologicTimePeriodTreeDef treeDef)
    {
        startTx();

        GeologicTimePeriodTreeDefItem root   = createGeologicTimePeriodTreeDefItem(null, treeDef,   "Root", 0);
        GeologicTimePeriodTreeDefItem era    = createGeologicTimePeriodTreeDefItem(root, treeDef,   "Erathem/Era", 100);
        GeologicTimePeriodTreeDefItem period = createGeologicTimePeriodTreeDefItem(era, treeDef,    "System/Period", 200);
        GeologicTimePeriodTreeDefItem series = createGeologicTimePeriodTreeDefItem(period, treeDef, "Series/Epoch",   300);
        @SuppressWarnings("unused")
        GeologicTimePeriodTreeDefItem member = createGeologicTimePeriodTreeDefItem(series, treeDef, "Stage/Age",      400);
        persist(root);
        commitTx();

        frame.setDesc("Building ChronoStratigraphy Tree...");
        
        Hashtable<String, GeologicTimePeriod> chronoHash = new Hashtable<String, GeologicTimePeriod>();
        
        chronoHash.clear();

        String fileName = "chronostrat_tree.xls";
        File file = XMLHelper.getConfigDir("../demo_files/"+fileName);
        if (!file.exists())
        {
            log.error("Couldn't file[" + file.getAbsolutePath() + "] checking the config dir");
            file = XMLHelper.getConfigDir(fileName);
            if (!file.exists())
            {
                file = new File("Specify/demo_files/"+fileName);
            }
        }

        if (file == null || !file.exists())
        {
            log.error("Couldn't file[" + file.getAbsolutePath() + "]");
            return null;
        }

        
        // setup the root ChronoStrat record (planet Earth)
        GeologicTimePeriod rootNode = new GeologicTimePeriod();
        rootNode.initialize();
        rootNode.setName("Root");
        rootNode.setFullName("Root");
        rootNode.setRankId(0);
        rootNode.setDefinition(treeDef);
        rootNode.setDefinitionItem(root);

        int counter = 0;
        
        try
        {
            startTx();
            
            persist(rootNode);
            
            String[]        cells    = new String[4];
            InputStream     input    = new FileInputStream(file);
            POIFSFileSystem fs       = new POIFSFileSystem(input);
            HSSFWorkbook    workBook = new HSSFWorkbook(fs);
            HSSFSheet       sheet    = workBook.getSheetAt(0);
            Iterator<?>     rows     = sheet.rowIterator();
            
            int lastRowNum  = sheet.getLastRowNum();
            if (frame != null)
            {
                final int mx = lastRowNum;
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        frame.setProcess(0, mx);
                    }
                });
            }
            
            while (rows.hasNext())
            {
                if (counter == 0)
                {
                    counter = 1;
                    continue;
                }
                if (counter % 100 == 0)
                {
                    if (frame != null) frame.setProcess(counter);
                    log.info("Converted " + counter + " ChronoStrat records");
                }
                
                HSSFRow row = (HSSFRow) rows.next();
                Iterator<?> cellsIter = row.cellIterator();
                int i = 0;
                while (cellsIter.hasNext() && i < 4)
                {
                    HSSFCell cell = (HSSFCell)cellsIter.next();
                    if (cell != null)
                    {
                        cells[i] = StringUtils.trim(cell.getRichStringCellValue().getString());
                        i++;
                    }
                }
                for (int j=i;j<4;j++)
                {
                    cells[j] = null;
                }
                //System.out.println();
                @SuppressWarnings("unused")
                GeologicTimePeriod newGeo = convertChronoStratRecord(cells[0], cells[1], cells[2], cells[3], rootNode);
    
                counter++;
            }
            
            input.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        if (frame != null) frame.setProcess(counter);
        
        log.info("Converted " + counter + " ChronoStrat records");

        TreeHelper.fixFullnameForNodeAndDescendants(rootNode);
        rootNode.setNodeNumber(1);
        fixNodeNumbersFromRoot(rootNode);
        
        commitTx();
        
        log.info("Converted " + counter + " Stratigraphy records");

        // set up ChronoStrat foreign key mapping for locality
        chronoHash.clear();

        return rootNode;
    }
    
    
    /**
     * @param continent
     * @param country
     * @param state
     * @param county
     * @param geoRoot
     * @return
     */
    protected GeologicTimePeriod convertChronoStratRecord(final String    continent,
                                                          final String    country,
                                                          final String    state,
                                                          final String    county,
                                                          final GeologicTimePeriod geoRoot)
    {
        String levelNames[] = { continent, country, state, county };
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

        GeologicTimePeriod prevLevelGeo = geoRoot;
        for (int i = 0; i < levelsToBuild; ++i)
        {
            GeologicTimePeriod newLevelGeo = buildChronoStratLevel(levelNames[i], prevLevelGeo);
            prevLevelGeo = newLevelGeo;
        }

        return prevLevelGeo;
    }

    /**
     * @param nameArg
     * @param parentArg
     * @return
     */
    protected GeologicTimePeriod buildChronoStratLevel(final String    nameArg,
                                                       final GeologicTimePeriod parentArg)
    {
        String name = nameArg;
        if (name == null)
        {
            name = "N/A";
        }

        // search through all of parent's children to see if one already exists with the same name
        Set<GeologicTimePeriod> children = parentArg.getChildren();
        for (GeologicTimePeriod child : children)
        {
            if (name.equalsIgnoreCase(child.getName()))
            {
                // this parent already has a child by the given name
                // don't create a new one, just return this one
                return child;
            }
        }

        // we didn't find a child by the given name
        // we need to create a new GeologicTimePeriod record
        GeologicTimePeriod newGeo = new GeologicTimePeriod();
        newGeo.initialize();
        newGeo.setName(name);
        newGeo.setParent(parentArg);
        parentArg.addChild(newGeo);
        newGeo.setDefinition(parentArg.getDefinition());
        int newGeoRank = parentArg.getRankId() + 100;
        
        GeologicTimePeriodTreeDefItem defItem = parentArg.getDefinition().getDefItemByRank(newGeoRank);
        newGeo.setDefinitionItem(defItem);
        newGeo.setRankId(newGeoRank);

        persist(newGeo);
        
        return newGeo;
    }
    
    /**
     * @param fmt
     */
    public static void fixNumericCatalogNumbers(final UIFieldFormatterIFace fmt)
    {
        Connection conn     = DBConnection.getInstance().createConnection();
        Statement  stmt     = null;
        Statement  updtStmt = null;
        try
        {
             stmt = conn.createStatement();
             updtStmt = conn.createStatement();
            
            ResultSet rs = stmt.executeQuery("select CollectionObjectID, CatalogNumber FROM collectionobject");
            while (rs.next())
            {
                int id        = rs.getInt(1);
                String catNum = (String)fmt.formatFromUI(rs.getString(2));
                
                updtStmt.executeUpdate("UPDATE collectionobject SET CatalogNumber='"+catNum+"' WHERE CollectionObjectID = "+id);
            }
            rs.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                stmt.close();
                updtStmt.close();
                conn.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

}
