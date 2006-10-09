package edu.ku.brc.specify;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.deleteAllRecordsFromTable;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.ResultsPager;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.GenericDBConversion;
import edu.ku.brc.specify.conversion.IdMapperMgr;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.tests.ObjCreatorHelper;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;

/**
 * Create more sample data, letting Hibernate persist it for us.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class SpecifyDBConverter
{
    protected static final Logger log = Logger.getLogger(SpecifyDBConverter.class);

    protected static Hashtable<String, Integer> prepTypeMapper    = new Hashtable<String, Integer>();
    protected static int                        attrsId           = 0;
    protected static SimpleDateFormat           dateFormatter     = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static StringBuffer               strBuf            = new StringBuffer("");
    protected static Calendar                   calendar          = Calendar.getInstance();

    /**
     * Constructor.
     */
    public SpecifyDBConverter()
    {

    }

    /**
     * Utility method to associate an artist with a catObj
     */
    public static void main(String args[]) throws Exception
    {
        // Create Specify Application
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
        
                try
                {
                    if (!System.getProperty("os.name").equals("Mac OS X"))
                    {
                        UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                        PlasticLookAndFeel.setMyCurrentTheme(new DesertBlue());
                    }
                }
                catch (Exception e)
                {
                    log.error("Can't change L&F: ", e);
                }
                
                Hashtable<String, String> old2NewDBNames = new Hashtable<String, String>();
                String[] names = {"Fish", "sp4_fish", "Accessions", "sp4_accessions", "Cranbrook", "sp4_cranbrook", "Ento", "sp4_ento"};
                for (int i=0;i<names.length;i++)
                {
                    old2NewDBNames.put(names[i], names[++i]);
                }
                UICacheManager.setAppName("SpecifyDBConverter");
                
                for (String name : selectedDBsToConvert(names))
                {
                    try
                    {
                        convertDB(old2NewDBNames.get(name), name.toLowerCase());
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        return;
                    }
                }
            }
        });
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
        
        // This will log us in and return true/false
        if (!UIHelper.tryLogin("com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", databaseName, "jdbc:mysql://localhost/", "rods", "rods"))
        {
            throw new RuntimeException("Couldn't login into ["+databaseName+"] "+DBConnection.getInstance().getErrorMsg());
        }

        // NOTE: You must have already created the database to use this
        // but the database can be empty
        boolean restartFromScratch = true;
        if (restartFromScratch)
        {
            Connection connection = DBConnection.getConnection();
            Statement stmt = connection.createStatement();
            try
            {
                log.info("Dropping database "+databaseName);
                boolean rv = stmt.execute("drop database "+ databaseName);
                if (!rv)
                {
                    //throw new RuntimeException("Couldn't drop database "+databaseName);
                }
                log.info("Dropped database "+databaseName);
                
            } catch (SQLException ex)
            {
                log.info("Database ["+databaseName+"] didn't exist.");
            }

            stmt = connection.createStatement();
            boolean rv = stmt.execute("create database "+ databaseName);
            if (!rv)
            {
                //throw new RuntimeException("Couldn't create database "+databaseName);
            }
            log.info("Created database "+databaseName);
            
            stmt.close();
            connection.close();
            
            writeHibPropFile(databaseName);
            doGenSchema();
        }
        
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

            boolean doConvert = true;
            if (doConvert)
            {
                GenericDBConversion conversion = new GenericDBConversion("com.mysql.jdbc.Driver",
                                                                         oldDatabaseName,
                                                                         "jdbc:mysql://localhost/"+oldDatabaseName,
                                                                         "rods",
                                                                         "rods");

                idMapperMgr = IdMapperMgr.getInstance();
                idMapperMgr.setDBs(conversion.getOldDBConnection(), conversion.getNewDBConnection());

                // NOTE: Within BasicSQLUtils the connection is for removing tables and records
                BasicSQLUtils.setDBConnection(conversion.getNewDBConnection());

                // This MUST be done before any of the table copies because it
                // creates the IdMappers for Agent, Address and mor eimportantly AgentAddress
                // NOTE: AgentAddress is actually mapping from the old AgentAddress table to the new Agent table
                boolean copyAgentAddressTables = false;
                if (copyAgentAddressTables || doAll)
                //if (copyAgentAddressTables)
                {
                    conversion.convertAgents();

                } else
                {
                    idMapperMgr.addTableMapper("agent", "AgentID");
                    idMapperMgr.addTableMapper("address", "AddressID");
                    idMapperMgr.addTableMapper("agentaddress", "AgentAddressID");
                }

                // GTP needs to be converted here so the stratigraphy conversion can use
                // the IDs
                boolean doGTP = false;
                if( doGTP || doAll )
                {
                	GeologicTimePeriodTreeDef treeDef = conversion.convertGTPDefAndItems();
                	conversion.convertGTP(treeDef);
                }

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

                boolean convertCatalogSeriesDef = false;
                if (convertCatalogSeriesDef || doAll)
                {
                    String userType =  databaseName.toLowerCase().indexOf("accessions") > -1 ? "Accessions" : "Collection Manager";
                    long specifyUserId = conversion.createDefaultUser("rods", userType);
                    conversion.convertCollectionObjectDefs(specifyUserId);

                } else
                {
                    idMapperMgr.addTableMapper("CatalogSeriesDefinition", "CatalogSeriesDefinitionID");
                    idMapperMgr.addTableMapper("CollectionObjectType", "CollectionObjectTypeID");
                }


                boolean copyUSYSTables = false;
                if (copyUSYSTables || doAll)
                {
                    conversion.convertUSYSTables();
                }

                boolean copyTables = false;
                if (copyTables || doAll)
                {
                    conversion.copyTables();
                }

                boolean doCollectionObjects = true;
                if (doCollectionObjects || doAll)
                {
                    if (true)
                    {
                        Map<String, PrepType> prepTypeMap = conversion.createPreparationTypesFromUSys();
                        prepTypeMap.put("n/a", prepTypeMap.get("misc"));
                        conversion.createPreparationRecords(prepTypeMap);
                    }
                    conversion.createCollectionRecords();

                    conversion.createDefaultDeterminationStatusRecords();
                    conversion.fixDeterminationStatus();
                }

                boolean doTaxonomy = false;
                if( doTaxonomy || doAll )
                {
                	conversion.copyTaxonTreeDefs();
                	conversion.convertTaxonTreeDefItems();
                	conversion.copyTaxonRecords();
                }

                boolean doGeography = false;
                if ((doGeography || doAll) && !databaseName.startsWith("accessions"))
                {
                	GeographyTreeDef treeDef = conversion.createStandardGeographyDefinitionAndItems();
                	conversion.convertGeography(treeDef);
                	conversion.convertLocality();
                }

                boolean doLocation = false;
                if( doLocation || doAll )
                {
                	conversion.buildSampleLocationTreeDef();
                }

                boolean doFurtherTesting = false;
                if (doFurtherTesting)
                {

                    BasicSQLUtils.deleteAllRecordsFromTable("datatype");
                    BasicSQLUtils.deleteAllRecordsFromTable("user");
                    BasicSQLUtils.deleteAllRecordsFromTable("usergroup");
                    BasicSQLUtils.deleteAllRecordsFromTable("collectionobjdef");

                    DataType          dataType  = ObjCreatorHelper.createDataType("Animal");
                    UserGroup         userGroup = ObjCreatorHelper.createUserGroup("Fish");
                    SpecifyUser       user      = ObjCreatorHelper.createSpecifyUser("rods", "rods@ku.edu", (short)0, userGroup, "CollectionManager");



                    Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CatalogSeries.class);
                    criteria.add(Expression.eq("catalogSeriesId", new Integer(0)));
                    java.util.List catalogSeriesList = criteria.list();

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
                                voucherSeries.setIsTissueSeries(false);
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
                                tissueSeries.setIsTissueSeries(true);
                                tissueSeries.setTimestampCreated(new Date());
                                tissueSeries.setTimestampModified(new Date());
                                tissueSeries.setCatalogSeriesId(101L);
                                tissueSeries.setCatalogSeriesPrefix("KUTIS");
                                tissueSeries.setSeriesName("Fish Tissue");
                                session.saveOrUpdate(tissueSeries);

                                voucherSeries.setTissue(tissueSeries);
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
            }

            if (GenericDBConversion.shouldDeleteMapTables())
            {
                idMapperMgr.cleanup();
            }


            log.info("Done.");

        } catch (Exception ex)
        {
            ex.printStackTrace();

            if (idMapperMgr != null && GenericDBConversion.shouldDeleteMapTables())
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
                List list = pager.getList();
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
                super(title, items);
                
                final SwingWorker worker = new SwingWorker()
                {
                    public Object construct()
                    {
                        try
                        {
                            Thread.sleep(10000); // 10 seconds
                            
                        } catch (Exception ex) {}
                        return null;
                    }

                    //Runs on the event-dispatching thread.
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
        //log.info("["+selKey+"]["+sb.toString()+"]");
        
        try
        {
            props.storeToXML(new FileOutputStream(propsFile), "Databases to Convert");
            
        } catch (Exception ex)
        {
            log.error(ex);
        }
        
        return dlg.getSelectedObjects();
    }

    /**
     * @throws Exception
     */
    protected static void doGenSchema() throws Exception
    {
        log.info("Starting up ANT for genschema task.");

        // Create a new project, and perform some default initialization
        Project project = new Project();
        try
        {
            project.init();
            project.setBasedir(".");

            ProjectHelper.getProjectHelper().parse(project, new File("build.xml"));

            project.executeTarget("genschema");

        } catch (BuildException e)
        {
            throw new Exception(e);
        }
    }


    /**
     * @param databaseName
     */
    protected static void writeHibPropFile(final String databaseName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("hibernate.dialect=org.hibernate.dialect.MySQLDialect\n");
        sb.append("hibernate.connection.driver_class=com.mysql.jdbc.Driver\n");
        sb.append("hibernate.connection.url=jdbc:mysql://localhost/"+databaseName+"\n");
        sb.append("hibernate.connection.username=rods\n");
        sb.append("hibernate.connection.password=rods\n");
        sb.append("hibernate.max_fetch_depth=3\n");
        sb.append("hibernate.connection.pool_size=5\n");
        sb.append("hibernate.cglib.use_reflection_optimizer=true\n");

        try
        {
            XMLHelper.setContents(new File("src" + File.separator + "hibernate.properties"), sb.toString());

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    

}
