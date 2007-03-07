package edu.ku.brc.specify;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.deleteAllRecordsFromTable;
import static edu.ku.brc.specify.tests.DataBuilder.*;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.ResultsPager;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.GenericDBConversion;
import edu.ku.brc.specify.conversion.IdMapperMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.tests.BuildSampleDatabase;
import edu.ku.brc.specify.tests.DataBuilder;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;

/**
 * Create more sample data, letting Hibernate persist it for us.
 *
 * @code_status Beta
 * @author rods
 */
public class SpecifyDBConverter
{
    protected static final Logger log = Logger.getLogger(SpecifyDBConverter.class);

    protected static Hashtable<String, Integer> prepTypeMapper    = new Hashtable<String, Integer>();
    protected static int                        attrsId           = 0;
    protected static SimpleDateFormat           dateFormatter     = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static StringBuffer               strBuf            = new StringBuffer("");
    protected static Calendar                   calendar          = Calendar.getInstance();
    
    protected static List<String>               dbNamesToConvert  = null;
    protected static int                        currentIndex      = 0;
    protected static Hashtable<String, String>  old2NewDBNames    = null;
    
    protected static ProgressFrame              frame             = null;

    /**
     * Constructor.
     */
    public SpecifyDBConverter()
    {
        //
    }

    /**
     * Utility method to associate an artist with a catObj
     */
    public static void main(String args[]) throws Exception
    {
        /*
        for (Enumeration e=LogManager.getCurrentLoggers(); e.hasMoreElements();)
        {
            Logger logger = (Logger)e.nextElement();
            logger.setLevel(Level.ALL);
            System.out.println("Setting "+ logger.getName() + " to " + logger.getLevel());
        }*/
        
        Logger logger = LogManager.getLogger("edu.ku.brc");
        if (logger != null)
        {
            logger.setLevel(Level.ALL);
            System.out.println("Setting "+ logger.getName() + " to " + logger.getLevel());
        }
        
        logger = LogManager.getLogger(edu.ku.brc.dbsupport.HibernateUtil.class);
        if (logger != null)
        {
            logger.setLevel(Level.INFO);
            System.out.println("Setting "+ logger.getName() + " to " + logger.getLevel());
        }
        
        // Create Specify Application
        SwingUtilities.invokeLater(new Runnable() {
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
                    log.error("Can't change L&F: ", e);
                }
                
                frame = new ProgressFrame("Converting");
                old2NewDBNames = new Hashtable<String, String>();
                String[] names = {"Fish", "sp4_fish", "Accessions", "sp4_accessions", "Cranbrook", "sp4_cranbrook", "Ento", "sp4_ento"};
                for (int i=0;i<names.length;i++)
                {
                    old2NewDBNames.put(names[i], names[++i]);
                }
                UICacheManager.setAppName("Specify");
                
                dbNamesToConvert = selectedDBsToConvert(names);
                currentIndex = 0;
                processDB();

            }
        });
    }
    
    protected static void processDB()
    {
        
        if (dbNamesToConvert.size() > 0 && currentIndex < dbNamesToConvert.size())
        {
            
            final SwingWorker worker = new SwingWorker()
            {
                @Override
                public Object construct()
                {
                    try
                    {
                        String currentDBName = dbNamesToConvert.get(currentIndex++);
                        frame.setTitle("Converting "+currentDBName+"...");
                        convertDB(old2NewDBNames.get(currentDBName), currentDBName.toLowerCase());
                        
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
                    processDB();
                }
            };
            worker.start();
        }
    }

    /**
     * Convert old Database to New 
     * @param oldDatabaseName name of an old database
     * @param databaseName name of new DB
     * @throws Exception xx
     */
    protected static void convertDB(final String oldDatabaseName, final String databaseName) throws Exception
    {
        boolean doAll = true; // when converting

        System.out.println("************************************************************");
        System.out.println("From "+oldDatabaseName+" to "+databaseName);
        System.out.println("************************************************************");

        HibernateUtil.shutdown();
        
        Properties initPrefs = BuildSampleDatabase.getInitializePrefs(databaseName);
        
        String userName     = initPrefs.getProperty("initializer.username", "rods");
        String password     = initPrefs.getProperty("initializer.password", "rods");
        String driverName   = initPrefs.getProperty("initializer.driver",   "MySQL");
        String databaseHost = initPrefs.getProperty("initializer.host",     "localhost");

        DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver(driverName);
        if (driverInfo == null)
        {
            throw new RuntimeException("COuldn't find driver by name ["+driverInfo+"] in driver list.");
        }
        
        // This will log us in and return true/false
        // This will connect without specifying a DB, which allows us to create the DB
        if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                driverInfo.getDialectClassName(), 
                databaseName, 
                driverInfo.getConnectionStr(databaseHost, databaseName), 
                userName, 
                password))
        {
            throw new RuntimeException("Couldn't login into ["+databaseName+"] "+DBConnection.getInstance().getErrorMsg());
        }
        
        DataBuilder.setSession(HibernateUtil.getNewSession());
        
        System.out.println("Preparing new database");
        
        SpecifySchemaGenerator schemaGen = new SpecifySchemaGenerator();
        schemaGen.generateSchema(driverInfo, databaseHost, databaseName, userName, password);
        
        System.out.println("Preparing new database: completed");
        
        // This will log us in and return true/false
        if (!UIHelper.tryLogin("com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", databaseName, "jdbc:mysql://localhost/"+databaseName, "rods", "rods"))
        {
            throw new RuntimeException("Couldn't login into ["+databaseName+"] "+DBConnection.getInstance().getErrorMsg());
        }

        HibernateUtil.getCurrentSession();
        IdMapperMgr idMapperMgr = null;
        try
        {
        	GenericDBConversion.setShouldCreateMapTables(true);

            GenericDBConversion.setShouldDeleteMapTables(true);
            
            frame.setOverall(0, 15);
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    UIHelper.centerAndShow(frame);
                }
            });

            boolean doConvert = true;
            if (doConvert)
            {
                GenericDBConversion conversion = new GenericDBConversion("com.mysql.jdbc.Driver",
                                                                         oldDatabaseName,
                                                                         "jdbc:mysql://localhost/"+oldDatabaseName,
                                                                         "rods",
                                                                         "rods");

                conversion.setFrame(frame);
                

                idMapperMgr = IdMapperMgr.getInstance();
                Connection oldConn = conversion.getOldDBConnection();
                Connection newConn = conversion.getNewDBConnection();
                if (oldConn==null || newConn==null)
                {
                	log.error("One of the DB connections is null.  Cannot proceed.  Check your DB install to make sure both DBs exist.");
                	System.exit(-1);
                }
                idMapperMgr.setDBs(oldConn, newConn);

                // NOTE: Within BasicSQLUtils the connection is for removing tables and records
                BasicSQLUtils.setDBConnection(conversion.getNewDBConnection());


                frame.setDesc("Converting Agents.");
                log.info("Converting Agents.");

                // This MUST be done before any of the table copies because it
                // creates the IdMappers for Agent, Address and mor eimportantly AgentAddress
                // NOTE: AgentAddress is actually mapping from the old AgentAddress table to the new Agent table
                boolean copyAgentAddressTables = false;
                if (copyAgentAddressTables || doAll)
                {
                    conversion.convertAgents();

                } else
                {
                    idMapperMgr.addTableMapper("agent", "AgentID");
                    idMapperMgr.addTableMapper("address", "AddressID");
                    idMapperMgr.addTableMapper("agentaddress", "AgentAddressID");
                }
                frame.incOverall();

                frame.setDesc("Converting Geologic Time Period.");
                log.info("Converting Geologic Time Period.");
                // GTP needs to be converted here so the stratigraphy conversion can use
                // the IDs
                boolean doGTP = false;
                if( doGTP || doAll )
                {
                	GeologicTimePeriodTreeDef treeDef = conversion.convertGTPDefAndItems();
                	conversion.convertGTP(treeDef);
                }

                frame.incOverall();

                frame.setDesc("Mapping Tables.");
                log.info("Mapping Tables.");
                boolean mapTables = true;
                if (mapTables || doAll)
                {
                    // Ignore these field names from new table schema when mapping OR
                    // when mapping IDs
                    BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(new String[] {"MethodID",  "RoleID",  "CollectionID",  "ConfidenceID",
                                                                                "TypeStatusNameID",  "ObservationMethodID",  "StatusID",
                                                                                "TypeID",  "ShipmentMethodID", "RankID", "DirectParentRankID",
                                                                                "RequiredParentRankID"});
                    conversion.mapIds();
                    BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(null);
                }

                frame.incOverall();


                frame.setDesc("Converting CollectionObjectDefs.");
                log.info("Converting CollectionObjectDefs.");
                boolean convertCatalogSeriesDef = false;
                if (convertCatalogSeriesDef || doAll)
                {
                    DataBuilder.getSession().beginTransaction();
                    
                    String           username         = initPrefs.getProperty("initializer.username", "rods");
                    String           title            = initPrefs.getProperty("useragent.title",    "Mr.");
                    String           firstName        = initPrefs.getProperty("useragent.firstname", "Rod");
                    String           lastName         = initPrefs.getProperty("useragent.lastname", "Spears");
                    String           midInit          = initPrefs.getProperty("useragent.midinit", "C");
                    String           abbrev           = initPrefs.getProperty("useragent.abbrev", "rs");
                    String           email            = initPrefs.getProperty("useragent.email", "rods@ku.edu");
                    String           userType         = initPrefs.getProperty("useragent.usertype", "CollectionManager");   
                    
                    UserGroup userGroup = DataBuilder.createUserGroup("admin2");
                    
                    Criteria criteria = DataBuilder.getSession().createCriteria(Agent.class);
                    criteria.add(Restrictions.eq("lastName", lastName));
                    criteria.add(Restrictions.eq("firstName", firstName));
                    
                    Agent userAgent = null;
                    List<?> list = criteria.list();
                    if (list != null && list.size() == 1)
                    {
                        userAgent = (Agent)list.get(0);
                    } else
                    {
                        userAgent = DataBuilder.createAgent(title, firstName, lastName, midInit, abbrev, email);
                    }
                    
                    SpecifyUser specifyUser = DataBuilder.createSpecifyUser(username, email, (short)0, userGroup, userType);
                    specifyUser.setAgent(userAgent);
                    
                    DataBuilder.getSession().getTransaction().commit();

                    conversion.convertCollectionObjectDefs(specifyUser.getSpecifyUserId());
                    SpecifyUser.setCurrentUser(specifyUser);
                    

                } else
                {
                    idMapperMgr.addTableMapper("CatalogSeriesDefinition", "CatalogSeriesDefinitionID");
                    idMapperMgr.addTableMapper("CollectionObjectType", "CollectionObjectTypeID");
                }
                frame.incOverall();
                frame.setDesc("Converting USYS Tables.");
                log.info("Converting USYS Tables.");
                boolean copyUSYSTables = false;
                if (copyUSYSTables || doAll)
                {
                    conversion.convertUSYSTables();
                }
                frame.incOverall();

                frame.setDesc("Converting Determinations Records");
                log.info("Converting Determinations Records");
                boolean doDeterminations = true;
                if (doDeterminations || doAll)
                {
                    conversion.createDefaultDeterminationStatusRecords();
                    frame.incOverall();

                    conversion.convertDeterminationRecords();
                } else
                {
                    frame.incOverall();
                }
                frame.incOverall();
                
                frame.setDesc("Converting doLoanPhysicalObjects Records");
                log.info("Converting doLoanPhysicalObjects Records");
                boolean doLoanPhysicalObjects = true;
                if (doLoanPhysicalObjects || doAll)
                {
                    conversion.convertLoanPhysicalObjects();
                    frame.incOverall();
                    
                } else
                {
                    frame.incOverall();
                }

                frame.setDesc("Copying Tables");
                log.info("Copying Tables");
                boolean copyTables = false;
                if (copyTables || doAll)
                {
                    conversion.copyTables();
                }

                frame.incOverall();

                
                frame.setDesc("Converting DeaccessionCollectionObject");
                log.info("Converting DeaccessionCollectionObject");
                boolean doDeaccessionCollectionObject = false;
                if (doDeaccessionCollectionObject || doAll)
                {
                    conversion.convertDeaccessionCollectionObject();
                }
                frame.incOverall();                

                frame.setDesc("Converting CollectionObjects");
                log.info("Converting CollectionObjects");
                boolean doCollectionObjects = false;
                if (doCollectionObjects || doAll)
                {
                    if (true)
                    {
                        Map<String, PrepType> prepTypeMap = conversion.createPreparationTypesFromUSys();
                        prepTypeMap.put("n/a", prepTypeMap.get("misc"));
                        conversion.createPreparationRecords(prepTypeMap);
                    }
                    
                    conversion.createCollectionRecords();
                    frame.incOverall();

                    
                }  else
                {
                    frame.incOverall();
                    frame.incOverall();
                }
                
                frame.setDesc("Converting Geography");
                log.info("Converting Geography");
                boolean doGeography = false;
                if (!databaseName.startsWith("accessions") && (doGeography || doAll))
                {
                    GeographyTreeDef treeDef = conversion.createStandardGeographyDefinitionAndItems();
                    conversion.convertGeography(treeDef);
                    frame.incOverall();
                    conversion.convertLocality();
                    frame.incOverall();
                } else
                {
                    frame.incOverall();
                    frame.incOverall();
                }

                frame.setDesc("Creating Location");
                log.info("Creating Location");
                boolean doLocation = false;
                if( doLocation || doAll )
                {
                    conversion.buildSampleLocationTreeDef();
                }
                frame.incOverall();
                
                frame.setDesc("Converting Taxonomy");
                log.info("Converting Taxonomy");
                boolean doTaxonomy = false;
                if( doTaxonomy || doAll )
                {
                	conversion.copyTaxonTreeDefs();
                	conversion.convertTaxonTreeDefItems();
                    
                    // fix the fullNameDirection field in each of the converted tree defs
                    Session session = HibernateUtil.getCurrentSession();
                    Query q = session.createQuery("FROM TaxonTreeDef");
                    List<?> allTTDs = q.list();
                    HibernateUtil.beginTransaction();
                    for(Object o: allTTDs)
                    {
                        TaxonTreeDef ttd = (TaxonTreeDef)o;
                        ttd.setFullNameDirection(TreeDefIface.FORWARD);
                        session.saveOrUpdate(ttd);
                    }
                    try
                    {
                        HibernateUtil.commitTransaction();
                    }
                    catch(Exception e)
                    {
                        log.error("Error while setting the fullname direction of taxonomy tree definitions.");
                    }
                    
                    // fix the fullNameSeparator field in each of the converted tree def items
                    session = HibernateUtil.getCurrentSession();
                    q = session.createQuery("FROM TaxonTreeDefItem");
                    List<?> allTTDIs = q.list();
                    HibernateUtil.beginTransaction();
                    for(Object o: allTTDIs)
                    {
                        TaxonTreeDefItem ttdi = (TaxonTreeDefItem)o;
                        ttdi.setFullNameSeparator(" ");
                        session.saveOrUpdate(ttdi);
                    }
                    try
                    {
                        HibernateUtil.commitTransaction();
                    }
                    catch(Exception e)
                    {
                        log.error("Error while setting the fullname separator of taxonomy tree definition items.");
                    }
                    
                	conversion.copyTaxonRecords();
                }
                frame.incOverall();

                System.setProperty(AppPreferences.factoryName, "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
                System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
                
                // Initialize the Prefs
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
                
                boolean doFurtherTesting = false;
                if (doFurtherTesting)
                {

                    BasicSQLUtils.deleteAllRecordsFromTable("datatype");
                    BasicSQLUtils.deleteAllRecordsFromTable("specifyuser");
                    BasicSQLUtils.deleteAllRecordsFromTable("usergroup");
                    BasicSQLUtils.deleteAllRecordsFromTable("collectionobjdef");

                    DataType          dataType  = createDataType("Animal");
                    UserGroup         userGroup = createUserGroup("Fish");
                    SpecifyUser       user      = createSpecifyUser("rods", "rods@ku.edu", (short)0, new UserGroup[] {userGroup}, "CollectionManager");



                    Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CatalogSeries.class);
                    criteria.add(Restrictions.eq("catalogSeriesId", new Integer(0)));
                    List<?> catalogSeriesList = criteria.list();

                    boolean doAddTissues = false;
                    if (doAddTissues)
                    {
                        deleteAllRecordsFromTable("catalogseries");
                        try
                        {
                            Session session = HibernateUtil.getCurrentSession();
                            HibernateUtil.beginTransaction();

                            CatalogSeries voucherSeries = null;
                            if (catalogSeriesList.size() == 0)
                            {
                                voucherSeries = new CatalogSeries();
                               // voucherSeries.setIsTissueSeries(false);
                                voucherSeries.setTimestampCreated(new Date());
                                voucherSeries.setTimestampModified(new Date());
                                voucherSeries.setCatalogSeriesId(100L);
                                voucherSeries.setCatalogSeriesPrefix("KUFISH");
                                voucherSeries.setSeriesName("Fish Collection");
                                session.saveOrUpdate(voucherSeries);

                            } else
                            {
                                voucherSeries = (CatalogSeries)catalogSeriesList.get(0);
                            }

                            if (voucherSeries != null)
                            {
                                CatalogSeries tissueSeries = new CatalogSeries();
                               // tissueSeries.setIsTissueSeries(true);
                                tissueSeries.setTimestampCreated(new Date());
                                tissueSeries.setTimestampModified(new Date());
                                tissueSeries.setCatalogSeriesId(101L);
                                tissueSeries.setCatalogSeriesPrefix("KUTIS");
                                tissueSeries.setSeriesName("Fish Tissue");
                                session.saveOrUpdate(tissueSeries);

                                //voucherSeries.setTissue(tissueSeries);
                                session.saveOrUpdate(voucherSeries);

                                HibernateUtil.commitTransaction();
                            }

                        } catch (Exception e)
                        {
                            log.error("******* " + e);
                            e.printStackTrace();
                            HibernateUtil.rollbackTransaction();
                        }
                        return;
                    }

                        Set<CollectionObjDef>  colObjDefSet = conversion.createCollectionObjDef("Fish", dataType, user, null, null);//(CatalogSeries)catalogSeriesList.get(0));


                        Object obj = colObjDefSet.iterator().next();
                        CollectionObjDef colObjDef = (CollectionObjDef)obj;

                        conversion.convertBiologicalAttrs(colObjDef, null, null);
                }
                //conversion.showStats();
            }

            if (idMapperMgr != null && GenericDBConversion.shouldDeleteMapTables())
            {
                idMapperMgr.cleanup();
            }
            log.info("Done - " + databaseName);
            frame.setDesc("Done - " + databaseName);
            frame.setTitle("Done - " + databaseName);
            frame.incOverall();
            frame.processDone();
            
            DataBuilder.getSession().close();

        } catch (Exception ex)
        {
            ex.printStackTrace();

            if (idMapperMgr != null && GenericDBConversion .shouldDeleteMapTables())
            {
                idMapperMgr.cleanup();
            }

        }
    }

    protected void testPaging()
    {
        boolean testPaging = false;
        if (testPaging)
        {
            /*
            long start;
            List list;
            ResultSet rs;
            java.sql.Statement stmt;

            start = System.currentTimeMillis();
            stmt = DBConnection.getConnection().createStatement();
            rs  = stmt.executeQuery("SELECT * FROM collectionobject c LIMIT 31000,32000");
            log.info("JDBC ******************** "+(System.currentTimeMillis() - start));

            Session session = HibernateUtil.getCurrentSession();
            //start = System.currentTimeMillis();
            //list = session.createQuery("from catalogseries in class CatalogSeries").setFirstResult(1).setMaxResults(1000).list();
            //log.info("HIBR ******************** "+(System.currentTimeMillis() - start));


            start = System.currentTimeMillis();
            stmt = DBConnection.getConnection().createStatement();
            rs  = stmt.executeQuery("SELECT * FROM collectionobject c LIMIT 31000,32000");
            log.info("JDBC ******************** "+(System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            list = session.createQuery("from collectionobject in class CollectionObject").setFirstResult(30000).setMaxResults(1000).list();
            log.info("HIBR ******************** "+(System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            list = session.createQuery("from collectionobject in class CollectionObject").setFirstResult(10000).setMaxResults(1000).list();
            log.info("HIBR ******************** "+(System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            list = session.createQuery("from collectionobject in class CollectionObject").setFirstResult(1000).setMaxResults(1000).list();
            log.info("HIBR ******************** "+(System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            stmt = DBConnection.getConnection().createStatement();
            rs  = stmt.executeQuery("SELECT * FROM collectionobject c LIMIT 1000,2000");
            ResultSetMetaData rsmd = rs.getMetaData();
            rs.first();
            while (rs.next())
            {
                for (int i=1;i<=rsmd.getColumnCount();i++)
                {
                    Object o = rs.getObject(i);
                }
            }
            log.info("JDBC ******************** "+(System.currentTimeMillis() - start));

           */

            /*
            HibernatePage.setDriverName("com.mysql.jdbc.Driver");

            int pageNo = 1;
            Pagable page = HibernatePage.getHibernatePageInstance(HibernateUtil.getCurrentSession().createQuery("from collectionobject in class CollectionObject"), 0, 100);
            log.info("Number Pages: "+page.getLastPageNumber());
            int cnt = 0;
            for (Object list : page.getThisPageElements())
            {
                //cnt += list.size();

                log.info("******************** Page "+pageNo++);
            }
            */

            ResultsPager pager = new ResultsPager(HibernateUtil.getCurrentSession().createQuery("from collectionobject in class CollectionObject"), 0, 10);
            //ResultsPager pager = new ResultsPager(HibernateUtil.getCurrentSession().createCriteria(CollectionObject.class), 0, 100);
            int pageNo = 1;
            do
            {
                long start = System.currentTimeMillis();
                List<?> list = pager.getList();
                if (pageNo % 100 == 0)
                {
                    log.info("******************** Page "+pageNo+" "+(System.currentTimeMillis() - start) / 1000.0);
                }
                pageNo++;

                for (Object co : list)
                {
                    if (pageNo % 1000 == 0)
                    {
                        log.info(((CollectionObject)co).getCatalogNumber());
                    }
                }
                list.clear();
                System.gc();
            } while (pager.isNextPage());

        }

    }
    
    /**
     * Loads the dialog
     * @param hashNames every other one is the new name
     * @return the list of selected DBs
     */
    protected static List<String> selectedDBsToConvert(final String[] hashNames)
    {
        String initStr = "";
        String selKey  = "Database_Selected";
        
        for (int i=0;i<hashNames.length;i++)
        {
            initStr += hashNames[i++];
        }
        
        boolean isNew = false;
        Properties props = new Properties();
        File propsFile = new File(UICacheManager.getDefaultWorkingPath() + File.separator + "convert.properties");
        if (!propsFile.exists())
        {
             isNew = true;
            
        } else
        {
            try
            {
                props.loadFromXML(new FileInputStream(propsFile));

            } catch (Exception ex)
            {
                log.error(ex);
            }
        } 
        
        List<String> list = new ArrayList<String>();
        for (int i=0;i<hashNames.length;i++)
        {
            list.add(hashNames[i++]);
        }
        
        int[] indices = null;
        if (isNew)
        {
            indices = new int[] {0};
            
        } else
        {
            String[] indicesStr = props.getProperty(selKey).split(",");
            indices = new int[indicesStr.length];
            for (int i=0;i<indicesStr.length;i++)
            {
                indices[i] = Integer.parseInt(indicesStr[i]);
            }
        }
        
        
         
        class ChooseDBs<T> extends ChooseFromListDlg<T>
        {
            public ChooseDBs(final String title, final List<T> items) throws HeadlessException
            {
                super(null, title, items);
                
                final SwingWorker worker = new SwingWorker()
                {
                    @Override
                    public Object construct()
                    {
                        try
                        {
                            Thread.sleep(10000); // 10 seconds
                            
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
                        if (list.getSelectedIndex() != -1)
                        {
                            okBtn.doClick();
                        }
                    }
                };
                worker.start();
            }

        }
        
        ChooseDBs<String> dlg = new ChooseDBs<String>("Choose Database(s) to Convert", list);
        dlg.setMultiSelect(true);
        dlg.setIndices(indices);
        UIHelper.centerAndShow(dlg);
        
        dlg.dispose();
        if (dlg.isCancelled())
        {
            return new ArrayList<String>();
        }
        
        StringBuilder sb = new StringBuilder();
        for (int inx : dlg.getSelectedIndices())
        {
            if (sb.length() > 0) sb.append(",");
            sb.append(Integer.toString(inx));
        }
        props.put(selKey, sb.toString());
        
        try
        {
            props.storeToXML(new FileOutputStream(propsFile), "Databases to Convert");
            
        } catch (Exception ex)
        {
            log.error(ex);
        }
        
        return dlg.getSelectedObjects();
    }
}
