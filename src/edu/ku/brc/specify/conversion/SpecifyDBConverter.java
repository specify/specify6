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
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.specify.config.init.DataBuilder.createAdminGroupAndUser;
import static edu.ku.brc.specify.config.init.DataBuilder.createAgent;
import static edu.ku.brc.specify.config.init.DataBuilder.createStandardGroups;
import static edu.ku.brc.specify.config.init.DataBuilder.getSession;
import static edu.ku.brc.specify.config.init.DataBuilder.setSession;

import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.MySQLDMBSUserMgr;
import edu.ku.brc.dbsupport.ResultsPager;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDefItem;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.dbsupport.PostInsertEventListener;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
    
    protected static boolean                    doFixCollectors   = false;
    
    protected Pair<String, String>              namePairToConvert = null;
    
    protected static ProgressFrame              frame             = null;
    
    protected Pair<String, String> itUsrPwd     = new Pair<String, String>(null, null);
    protected Pair<String, String> masterUsrPwd = new Pair<String, String>("Master", "Master");
    protected String               hostName     = "localhost";
    
    protected ConversionLogger     convLogger   = new ConversionLogger();
    
    /**
     * Constructor.
     */
    public SpecifyDBConverter()
    {
        PostInsertEventListener.setAuditOn(false);
        
        setUpSystemProperties();
        
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception
    {
        // Set App Name, MUST be done very first thing!
        UIRegistry.setAppName("Specify");  //$NON-NLS-1$

        log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        UIRegistry.setEmbeddedDBDir(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
        
        for (String s : args)
        {
            String[] pairs = s.split("="); //$NON-NLS-1$
            if (pairs.length == 2)
            {
                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
                {
                    //System.err.println("["+pairs[0].substring(2, pairs[0].length())+"]["+pairs[1]+"]");
                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
                } 
            } else
            {
                String symbol = pairs[0].substring(2, pairs[0].length());
                //System.err.println("["+symbol+"]");
                System.setProperty(symbol, symbol);
            }
        }
        
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
        
        final SpecifyDBConverter converter = new  SpecifyDBConverter();
        
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
                
                Pair<String, String> namePair = null;
                try
                {
                    if (converter.selectedDBsToConvert())
                    {
                        namePair = converter.chooseTable();
                    }
                       
                } catch (SQLException ex)
                {
                    ex.printStackTrace();
                	JOptionPane.showConfirmDialog(null, "The Converter was unable to login.", "Error", JOptionPane.CLOSED_OPTION);
                }
                
                if (namePair != null)
                {
                    frame = new ProgressFrame("Converting");
                    
                    converter.processDB(); 
                } else
                {
                    JOptionPane.showConfirmDialog(null, "The Converter was unable to login.", "Error", JOptionPane.CLOSED_OPTION);
                    System.exit(0);
                }
                
            }
        });
    }
    
    
    /**
     * @param newDBConn
     */
    protected boolean showStatsFromNewCollection(final Connection newDBConn)
    {
        String[] queries = {"SELECT count(*) FROM collectionobject",
                            "SELECT count(*) FROM preparation",
                            "SELECT count(*) FROM determination",
                            "SELECT count(*) FROM taxon",
                            "SELECT count(*) FROM agent",};
        
        String[] descs = {"CollectionObjects",
                          "Preparations",
                          "Determinations",
                          "Taxon",
                          "Agents"};
        
        Object[][] rows = new Object[queries.length][2];
        for (int i=0;i<queries.length;i++)
        {
            rows[i][0] = descs[i];
            rows[i][1] = BasicSQLUtils.getCount(newDBConn, queries[i]);
        }
        JTable table = new JTable(rows, new Object[] {"Description", "Count"});
        CustomDialog dlg = new CustomDialog((Frame)null, "Destination DB Statistics", true, CustomDialog.OKCANCEL, UIHelper.createScrollPane(table, true));
        dlg.setOkLabel("Continue");
        dlg.setVisible(true);
        return !dlg.isCancelled();
    }
    
    /**
     * @return
     * @throws SQLException
     */
    protected Pair<String, String> chooseTable() throws SQLException
    {
        MySQLDMBSUserMgr mgr = new MySQLDMBSUserMgr();
        
        Vector<DBNamePair> availPairs = new Vector<DBNamePair>();
        
        if (mgr.connectToDBMS(itUsrPwd.first, itUsrPwd.second, hostName))
        {
            BasicSQLUtils.setSkipTrackExceptions(true);
            
            Connection conn = mgr.getConnection();
            Vector<Object[]> dbNames = BasicSQLUtils.query(conn, "show databases");
            for (Object[] row : dbNames)
            {
                System.err.println("Setting ["+row[0].toString()+"]");
                conn.setCatalog(row[0].toString());
                
                boolean fnd = false;
                Vector<Object[]> tables = BasicSQLUtils.query(conn, "show tables");
                for (Object[] tblRow : tables)
                {
                    if (tblRow[0].toString().equals("usysversion"))
                    {
                        fnd = true;
                        break;
                    }
                }
                
                if (!fnd)
                {
                    continue;
                }
                
                Vector<Object[]> tableDesc = BasicSQLUtils.query(conn, "select CollectionName FROM collection");
                if (tableDesc.size() > 0)
                {
                    String collName =  tableDesc.get(0)[0].toString();
                    availPairs.add(new DBNamePair(collName, row[0].toString()));
                }
            }
            
            Collections.sort(availPairs, new Comparator<Pair<String, String>>() {
                @Override
                public int compare(Pair<String, String> o1, Pair<String, String> o2)
                {
                    return o1.first.compareTo(o2.first);
                }
            });
            
            mgr.close();
            BasicSQLUtils.setSkipTrackExceptions(false);
            
            final JList     list = new JList(availPairs);
            CellConstraints cc   = new CellConstraints();
            PanelBuilder    pb   = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
            pb.add(UIHelper.createScrollPane(list, true), cc.xy(1,1));
            pb.setDefaultDialogBorder();
            
            final CustomDialog dlg = new CustomDialog(null, "Select a DB to Convert", true, pb.getPanel());
            list.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        dlg.getOkBtn().setEnabled(list.getSelectedIndex() > -1);
                    }
                }
            });
            
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() == 2)
                    {
                        dlg.getOkBtn().setEnabled(list.getSelectedIndex() > -1);
                        dlg.getOkBtn().doClick();
                    }
                }
            });
            
            dlg.createUI();
            dlg.setVisible(true);
            if (dlg.isCancelled())
            {
                return null;
            }
            
            return namePairToConvert = (DBNamePair)list.getSelectedValue();
        }
        
        return null;
    }
    
    /**
     * @param isCustomConvert
     * @param sourceDbProps
     * @param destDbProps
     */
    protected  void processDB()
    {
        String inputName = null;
        if (namePairToConvert.second != null && namePairToConvert.second.startsWith("sp5_"))
        {
            inputName = namePairToConvert.second.substring(4);
        } else
        {
            //inputName = JOptionPane.showInputDialog("Enter new DB Name:");
            inputName = namePairToConvert.second +"_6";
        }
        
        if (inputName != null)
        {
            final String      destName = inputName;
            final SwingWorker worker = new SwingWorker()
            {
                @Override
                public Object construct()
                {
                    try
                    {
                        frame.setTitle("Converting "+namePairToConvert.toString()+"...");
                        
                        convertDB(namePairToConvert.second,  destName);
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        //System.exit(1);
                    }
                    return null;
                }

                //Runs on the event-dispatching thread.
                @Override
                public void finished()
                {
                    System.exit(0);
                }
            };
            worker.start();
        }
    }
    
    /**
     * 
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
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI
        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory");
        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");    // Needed for CatalogNumberign
        System.setProperty(QueryAdjusterForDomain.factoryName,          "edu.ku.brc.specify.dbsupport.SpecifyExpressSearchSQLAdjuster");    // Needed for ExpressSearch
        System.setProperty(SchemaI18NService.factoryName,               "edu.ku.brc.specify.config.SpecifySchemaI18NService");    // Needed for Localization and Schema
        System.setProperty(SecurityMgr.factoryName,                     "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");

        AppContextMgr.getInstance().setHasContext(true);
    }
    
    
    

    /**
     * Convert old Database to New 
     * @param databaseNameSource name of an old database
     * @param databaseNameDest name of new DB
     * @throws Exception xx
     */
    protected  void convertDB(final String dbNameSource, 
                              final String dbNameDest) throws Exception
    {
        System.setProperty(DBMSUserMgr.factoryName, "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");

        AppContextMgr.getInstance().clear();
        
        boolean doAll               = true; 
        boolean startfromScratch    = true; 
        boolean deleteMappingTables = true;
        
        System.out.println("************************************************************");
        System.out.println("From "+dbNameSource+" to "+dbNameDest);
        System.out.println("************************************************************");

        HibernateUtil.shutdown(); 
        
        DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
        
        String oldConnStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, dbNameSource, 
                itUsrPwd.first, itUsrPwd.second, driverInfo.getName());

        String newConnStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, dbNameDest, 
                itUsrPwd.first, itUsrPwd.second, driverInfo.getName());
        
        MySQLDMBSUserMgr mysqlMgr = new MySQLDMBSUserMgr();
        if (mysqlMgr.connectToDBMS(itUsrPwd.first, itUsrPwd.second, hostName))
        {
            if (!mysqlMgr.doesDBExists(dbNameDest))
            {
                mysqlMgr.createDatabase(dbNameDest);
            }
        }
        mysqlMgr.close();   

        // This will log us in and return true/false
        // This will connect without specifying a DB, which allows us to create the DB
        if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                driverInfo.getDialectClassName(), 
                dbNameDest, 
                newConnStr,
                itUsrPwd.first, 
                itUsrPwd.second))
        {
            log.error("Failed connection string: "  +driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, dbNameDest, itUsrPwd.first, itUsrPwd.second, driverInfo.getName()) );
            throw new RuntimeException("Couldn't login into ["+dbNameDest+"] "+DBConnection.getInstance().getErrorMsg());
        }
        
        log.debug("Preparing new database");
        
         DBConnection oldDB = DBConnection.createInstance(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), dbNameDest, oldConnStr, itUsrPwd.first, itUsrPwd.second);
        
        Connection oldDBConn = oldDB.createConnection();
        Connection newDBConn = DBConnection.getInstance().createConnection();
        
        if (!isOldDBOK(oldDBConn))
        {
            return;
        }
        
        OldDBStatsDlg dlg = new OldDBStatsDlg(oldDBConn);
        dlg.setVisible(true);
        if (dlg.isCancelled())
        {
            oldDBConn.close();
            newDBConn.close();
            System.exit(0);
        }
        
        doFixCollectors = dlg.doFixAgents();
        
        convLogger.initialize(dbNameDest);
        
        final GenericDBConversion conversion = new GenericDBConversion(oldDBConn, newDBConn, dbNameSource, dbNameDest, convLogger);
        if (!conversion.initialize())
        {
            oldDBConn.close();
            newDBConn.close();
            System.exit(0);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                conversion.setFrame(frame);
                frame.setDesc("Building Database Schema...");
                frame.adjustProgressFrame();
                frame.getProcessProgress().setIndeterminate(true);
                UIHelper.centerAndShow(frame);

            }
        });

        
        if (startfromScratch)
        {
            log.debug("Starting from scratch and generating the schema");
            SpecifySchemaGenerator.generateSchema(driverInfo, hostName, dbNameDest, itUsrPwd.first, itUsrPwd.second);
        }

        log.debug("Preparing new database: completed");

        setSession(HibernateUtil.getNewSession());
        IdMapperMgr idMapperMgr = null;
        SpecifyUser specifyUser = null;

        try
        {
        	GenericDBConversion.setShouldCreateMapTables(startfromScratch);
            GenericDBConversion.setShouldDeleteMapTables(deleteMappingTables);
            
            frame.setOverall(0, 18);
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    UIHelper.centerAndShow(frame);
                }
            });

            boolean doConvert = true;
            if (doConvert)
            {
                BuildSampleDatabase.createSpecifySAUser(hostName, itUsrPwd.first, itUsrPwd.second, masterUsrPwd.first, masterUsrPwd.second, dbNameDest);

                idMapperMgr = IdMapperMgr.getInstance();
                Connection oldConn = conversion.getOldDBConnection();
                Connection newConn = conversion.getNewDBConnection();
                if (oldConn == null || newConn == null)
                {
                	log.error("One of the DB connections is null.  Cannot proceed.  Check your DB install to make sure both DBs exist.");
                	System.exit(-1);
                }
                
                idMapperMgr.setDBs(oldConn, newConn);

                // NOTE: Within BasicSQLUtils the connection is for removing tables and records
                BasicSQLUtils.setDBConnection(conversion.getNewDBConnection());
                
                if (false)
                {
                    addStorageTreeFomrXML(true);
                    return;
                }
                
                //---------------------------------------------------------------------------------------
                //-- Create basic set of information.
                //---------------------------------------------------------------------------------------
                conversion.doInitialize();
                
                if (startfromScratch)
                {
                    BasicSQLUtils.deleteAllRecordsFromTable(conversion.getNewDBConnection(), "agent", BasicSQLUtils.myDestinationServerType);
                    BasicSQLUtils.deleteAllRecordsFromTable(conversion.getNewDBConnection(), "address", BasicSQLUtils.myDestinationServerType);
                }
                conversion.initializeAgentInfo(startfromScratch);
                
                frame.setDesc("Mapping Tables.");
                log.info("Mapping Tables.");
                boolean mapTables = true;
                
                //GenericDBConversion.setShouldCreateMapTables(false);
                if (mapTables || doAll)
                {
                    // Ignore these field names from new table schema when mapping OR
                    // when mapping IDs
                    BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(new String[] {"MethodID",  "RoleID",  "CollectionID",  "ConfidenceID",
                                                                                "TypeStatusNameID",  "ObservationMethodID",  "StatusID",
                                                                                "TypeID",  "ShipmentMethodID", "RankID", "DirectParentRankID",
                                                                                "RequiredParentRankID", "MediumID"});
                    conversion.mapIds();
                    BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(null);
                }
                //GenericDBConversion.setShouldCreateMapTables(startfromScratch);
                
                frame.incOverall();

                Integer institutionId = conversion.createInstitution("Natural History Museum");
                if (institutionId == null)
                {
                    UIRegistry.showError("Problem with creating institution, the Id came back null");
                    System.exit(0);
                }
                
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // Need to ask for Discipline here or get it from the Sp5 DB
                
                conversion.convertDivision(institutionId);
                frame.incOverall();
                
                /////////////////////////////////////////////////////////////
                // Really need to create or get a proper Discipline Record
                /////////////////////////////////////////////////////////////
                
                frame.setDesc("Converting CollectionObjectDefs.");
                log.info("Converting CollectionObjectDefs.");
                boolean convertDiscipline = true;
                if (convertDiscipline || doAll)
                {
                    String           username         = "testuser";
                    String           title            = "Mr.";
                    String           firstName        = "Test";
                    String           lastName         = "User";
                    String           midInit          = "C";
                    String           abbrev           = "tcu";
                    String           email            = "testuser@ku.edu";
                    String           userType         = SpecifyUserTypes.UserType.Manager.toString();   
                    String           password         = "testuser";
                    
                    Agent userAgent   = null;
                    
                    if (startfromScratch)
                    {
                        Transaction trans = getSession().beginTransaction();
                        
                        //BasicSQLUtils.deleteAllRecordsFromTable(newConn, "usergroup", BasicSQLUtils.myDestinationServerType);
                        BasicSQLUtils.deleteAllRecordsFromTable(newConn, "specifyuser", BasicSQLUtils.myDestinationServerType);
                        //SpPrincipal userGroup = createUserGroup("admin2");
                        
                        Criteria criteria = getSession().createCriteria(Agent.class);
                        criteria.add(Restrictions.eq("lastName", lastName));
                        criteria.add(Restrictions.eq("firstName", firstName));
                        
                        
                        List<?> list = criteria.list();
                        if (list != null && list.size() == 1)
                        { 
                            userAgent = (Agent)list.get(0);
                        } else
                        {
                            userAgent = createAgent(title, firstName, midInit, lastName, abbrev, email);
                        }
                        
                        //specifyUser = createSpecifyUser(username, email, password, userType);
                        Institution institution = (Institution)getSession().createQuery("FROM Institution").list().get(0);
                        
                        String encrypted = Encryption.encrypt(password, password);
                        specifyUser = createAdminGroupAndUser(getSession(), institution,  username, email, encrypted, userType);
                        specifyUser.addReference(userAgent, "agents");
                        
                        getSession().saveOrUpdate(institution);
                        
                        userAgent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
                        getSession().saveOrUpdate(userAgent);
                        
                        trans.commit();
                        getSession().flush();
                        
                    } else
                    {
                        // XXX Works for a Single Convert
                        specifyUser = (SpecifyUser)getSession().createCriteria(SpecifyUser.class).list().get(0);
                        userAgent   = specifyUser.getAgents().iterator().next();
                    }
                    
                    if (startfromScratch)
                    {
                        int     catSeriesId = 0;
                        conversion.convertCollectionObjectDefs(specifyUser.getSpecifyUserId(), catSeriesId, userAgent);
                        
                    } else
                    {
                        AppContextMgr.getInstance().setClassObject(SpecifyUser.class, specifyUser);
                        // XXX Works for a Single Convert
                        Collection collection = (Collection)getSession().createCriteria(Collection.class).list().get(0);
                        AppContextMgr.getInstance().setClassObject(Collection.class, collection);
                    }

                } else
                {
                    idMapperMgr.addTableMapper("CatalogSeriesDefinition", "CatalogSeriesDefinitionID");
                    idMapperMgr.addTableMapper("CollectionObjectType",    "CollectionObjectTypeID");
                }
                frame.incOverall();

                
                frame.setDesc("Converting Agents.");
                log.info("Converting Agents.");
                
                AgentConverter agentConverter = new AgentConverter(conversion, idMapperMgr, startfromScratch);

                
                // This MUST be done before any of the table copies because it
                // creates the IdMappers for Agent, Address and more importantly AgentAddress
                // NOTE: AgentAddress is actually mapping from the old AgentAddress table to the new Agent table
                boolean copyAgentAddressTables = true;
                if (copyAgentAddressTables || doAll)
                {
                    log.info("Calling - convertAgents");
                    
                    agentConverter.convertAgents(doFixCollectors);
                    
                } else
                {
                    idMapperMgr.addTableMapper("agent", "AgentID");
                    idMapperMgr.addTableMapper("address", "AddressID");
                    idMapperMgr.addTableMapper("agentaddress", "AgentAddressID");
                }
                frame.incOverall();
                
                frame.setDesc("Mapping Agent Tables.");
                log.info("MappingAgent Tables.");
                if (mapTables || doAll)
                {
                    // Ignore these field names from new table schema when mapping OR
                    // when mapping IDs
                    BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(new String[] {"MethodID",  "RoleID",  "CollectionID",  "ConfidenceID",
                                                                                "TypeStatusNameID",  "ObservationMethodID",  "StatusID",
                                                                                "TypeID",  "ShipmentMethodID", "RankID", "DirectParentRankID",
                                                                                "RequiredParentRankID", "MediumID"});
                    conversion.mapAgentRelatedIds();//MEG LOOK HERE
                    BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs(null);
                }
                frame.incOverall();


                frame.setDesc("Converting Geologic Time Period.");
                log.info("Converting Geologic Time Period.");
                // GTP needs to be converted here so the stratigraphy conversion can use
                // the IDs
                boolean doGTP = true;
                if (doGTP || doAll )
                {
                    ConversionLogger.TableWriter tblWriter = convLogger.getWriter("GTP.html", "Geologic Time Period");
                    GeologicTimePeriodTreeDef treeDef = conversion.convertGTPDefAndItems();
                    conversion.convertGTP(tblWriter, treeDef);
                } else
                {
                    idMapperMgr.addTableMapper("geologictimeperiod", "GeologicTimePeriodID");
                    idMapperMgr.mapForeignKey("Stratigraphy", "GeologicTimePeriodID", "GeologicTimePeriod", "GeologicTimePeriodID");
                }

                frame.incOverall();
                
                frame.setDesc("Converting Determinations Records");
                log.info("Converting Determinations Records");
                boolean doDeterminations = false;
                if (doDeterminations || doAll)
                {
                    //conversion.createDefaultDeterminationStatusRecords();
                    frame.incOverall();

                    conversion.convertDeterminationRecords();
                } else
                {
                    frame.incOverall();
                }
                frame.incOverall();
                
                frame.setDesc("Copying Tables");
                log.info("Copying Tables");
                boolean copyTables = true;
                if (copyTables || doAll)
                {
                    boolean doBrief  = false;
                    conversion.copyTables(doBrief);
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
                        Session session = HibernateUtil.getCurrentSession();
                        try
                        {
                            Hashtable<Integer, Map<String, PrepType>> collToPrepTypeHash = new Hashtable<Integer, Map<String,PrepType>>();
                            Query   q = session.createQuery("FROM Collection");
                            for (Object dataObj :  q.list())
                            {
                                boolean cache = GenericDBConversion.shouldCreateMapTables();
                                GenericDBConversion.setShouldCreateMapTables(true);
                                
                                Collection            collection  = (Collection)dataObj;
                                Map<String, PrepType> prepTypeMap = conversion.createPreparationTypesFromUSys(collection);
                                
                                GenericDBConversion.setShouldCreateMapTables(cache);
                                
                                PrepType miscPT = prepTypeMap.get("misc");
                                if (miscPT != null)
                                {
                                    prepTypeMap.put("n/a", miscPT);
                                } else
                                {
                                    miscPT = prepTypeMap.get("Misc"); 
                                    if (miscPT != null)
                                    {
                                        prepTypeMap.put("n/a", miscPT);
                                    } else
                                    {
                                        log.error("******************************* Couldn't find 'Misc' PrepType!");
                                    }
                                }
                                collToPrepTypeHash.put(collection.getCollectionId(), prepTypeMap);
                            }
                            conversion.convertPreparationRecords(collToPrepTypeHash);
                            
                        } catch (Exception ex)
                        {
                            throw new RuntimeException(ex);
                            
                        }
                    }
                    
                    frame.setDesc("Converting LoanPreparations Records");
                    log.info("Converting LoanPreparations Records");
                    boolean doLoanPreparations = false;
                    if (doLoanPreparations || doAll)
                    {
                    	conversion.convertLoanRecords(false);     // Loans
                    	conversion.convertLoanAgentRecords(false);// Loans
                        conversion.convertLoanPreparations();
                        
                    	conversion.convertLoanAgentRecords(true); // Gifts
                    	conversion.convertLoanRecords(true);      // Gifts
                        conversion.convertGiftPreparations();
                        frame.incOverall();
                        
                    } else
                    {
                        frame.incOverall();
                    }
                    
                    // Arg1 - Use Numeric Catalog Number
                    // Arg2 - Use the Prefix from Catalog Series
                    conversion.convertCollectionObjects(true, false);
                    frame.incOverall();

                    
                }  else
                {
                    frame.incOverall();
                    frame.incOverall();
                }
                
                frame.setDesc("Converting Geography");
                log.info("Converting Geography");
                boolean doGeography = true;
                if (!dbNameDest.startsWith("accessions") && (doGeography || doAll))
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
 
                frame.setDesc("Converting Taxonomy");
                log.info("Converting Taxonomy");
                boolean doTaxonomy = true;
                if (doTaxonomy || doAll )
                {
                	 ConversionLogger.TableWriter tblWriter = convLogger.getWriter("FullTaxon.html", "Taxon Conversion");
                	
                	conversion.copyTaxonTreeDefs(tblWriter);
                	conversion.convertTaxonTreeDefItems(tblWriter);
                	BasicSQLUtils.setTblWriter(tblWriter);
                    
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
                    for(Object o : allTTDIs)
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
                    
                	conversion.convertTaxonRecords(tblWriter);
                	
                	BasicSQLUtils.setTblWriter(null);
                }
                frame.incOverall();
                
                frame.setDesc("Converting Straigraphy");
                log.info("Converting Straigraphy");
                boolean doStrat = false;
                if (doStrat || doAll )
                {
                     ConversionLogger.TableWriter tblWriter = convLogger.getWriter("FullStrat.html", "Straigraphy Conversion");
                     
                     conversion.convertStrat(tblWriter);
                }
                
                //-------------------------------------------
                // Get Discipline and Collection
                //-------------------------------------------
                
                frame.incOverall();
                
                if (getSession() != null)
                {
                    getSession().close();
                    setSession(null);
                }

                boolean     status       = false;
                Institution institution  = null;
                Division    division     = null;
                Collection  collection   = null;
                Discipline  dscp         = null;
                Session     localSession = HibernateUtil.getNewSession();
                Session     cachedCurrentSession = getSession();
                setSession(null);
                try
                {
                    if (conversion.getCurDisciplineID() == null)
                    {
                        List<?> list = localSession.createQuery("FROM Discipline").list();
                        dscp = (Discipline)list.get(0);
                        
                    } else
                    {
                        log.debug("Loading Discipline with Id["+conversion.getCurDisciplineID()+"]");
                        List<?> list = localSession.createQuery("FROM Discipline WHERE id = "+conversion.getCurDisciplineID()).list();
                        dscp = (Discipline)list.get(0);
                    }
                    AppContextMgr.getInstance().setClassObject(Discipline.class, dscp);
                    
                    if (dscp.getCollections().size() == 1)
                    {
                        collection = dscp.getCollections().iterator().next();
                    }
                    
                    if (collection == null)
                    {
                        if (conversion.getCurCollectionID() == null || conversion.getCurCollectionID() == 0)
                        {
                            List<?> list = localSession.createQuery("FROM Collection").list();
                            collection = (Collection)list.get(0);
                            
                        } else
                        {
                            String hsql = "FROM Collection WHERE id = "+conversion.getCurCollectionID();
                            log.info(hsql);
                            List<?> list = localSession.createQuery(hsql).list();
                            if (list == null || list.size() == 0)
                            {
                                UIRegistry.showError("Couldn't find the Collection record ["+hsql+"]");
                            }
                            collection = (Collection)list.get(0);
                        }
                    }
                    
                    division    = dscp.getDivision();
                    localSession.lock(division, LockMode.NONE);
                    institution = division.getInstitution();
                    localSession.lock(institution, LockMode.NONE);
                    institution.getDivisions().size();
                    
                    AppContextMgr.getInstance().setClassObject(Collection.class, collection);
                    AppContextMgr.getInstance().setClassObject(Division.class, division);
                    AppContextMgr.getInstance().setClassObject(Institution.class, institution);
                    
                    if (doFixCollectors)
                    {
                        agentConverter.fixupForCollectors(division, dscp);
                    }
                    
                    setSession(localSession);
                    
                    if (true)
                    {
                        try
                        {
                            // create the standard user groups for this collection
                            Map<String, SpPrincipal> groupMap = createStandardGroups(localSession, collection);

                            // add the administrator as a Collections Manager in this group
                            specifyUser.addUserToSpPrincipalGroup(groupMap.get(SpecifyUserTypes.UserType.Manager.toString()));
                            
                            Transaction trans = localSession.beginTransaction();
                            
                            for (SpPrincipal prin : groupMap.values())
                            {
                                localSession.saveOrUpdate(prin);
                            }
                            
                            localSession.saveOrUpdate(specifyUser);
                            
                            trans.commit();
                            
                            localSession.close();
                            
                            localSession = HibernateUtil.getNewSession();
                            
                            specifyUser = (SpecifyUser)localSession.merge(specifyUser);
                            
                            division       = (Division)localSession.createQuery("FROM Division WHERE id = " + division.getId()).list().iterator().next();
                            institution    = (Institution)localSession.createQuery("FROM Institution WHERE id = " + institution.getId()).list().iterator().next();
                            collection     = (Collection)localSession.createQuery("FROM Collection WHERE id = " + collection.getId()).list().iterator().next();
                            
                            AppContextMgr.getInstance().setClassObject(Collection.class, collection);
                            AppContextMgr.getInstance().setClassObject(Division.class,   division);
                            AppContextMgr.getInstance().setClassObject(Institution.class, institution);
                            
                            dscp = (Discipline)localSession.createQuery("FROM Discipline WHERE id = " + dscp.getId()).list().iterator().next();
                            dscp.getAgents();
                            AppContextMgr.getInstance().setClassObject(Discipline.class, dscp);
                            
                            localSession.flush();
                            
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                    
                    status = true;
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
                setSession(cachedCurrentSession);
                
                frame.setDesc("Converting USYS Tables.");
                log.info("Converting USYS Tables.");
                boolean copyUSYSTables = false;
                if (copyUSYSTables || doAll)
                {
                    if (status)
                    {
                        BasicSQLUtils.deleteAllRecordsFromTable("picklist", BasicSQLUtils.myDestinationServerType);
                        BasicSQLUtils.deleteAllRecordsFromTable("picklistitem", BasicSQLUtils.myDestinationServerType);
    
                        conversion.convertUSYSTables(localSession, collection);
                        
                        frame.setDesc("Creating PickLists from XML.");
                        BuildSampleDatabase.createPickLists(localSession, null, true, collection);
                        BuildSampleDatabase.createPickLists(localSession, dscp, true, collection);
                    } else
                    {
                        log.error("STATUS was FALSE for PickList creation!");
                    }
                    
                    frame.incOverall();
                    
                } else
                {
                    frame.incOverall();
                }
                
                if (localSession != null)
                {
                    localSession.close();
                }
                
                
                // MySQL Only ???
                String sql = "UPDATE determination SET PreferredTaxonID = CASE WHEN " +
                "(SELECT AcceptedID FROM taxon WHERE taxon.TaxonID = determination.TaxonID) IS NULL " +
                "THEN determination.TaxonID ELSE (SELECT AcceptedID FROM taxon WHERE taxon.TaxonID = determination.TaxonID) END";
                System.out.println(sql);
                BasicSQLUtils.setSkipTrackExceptions(true);
                BasicSQLUtils.update(sql);
                
                //------------------------------------------------
                // Localize Schema and make form fields visible
                //------------------------------------------------
                frame.setDesc("Localizing the Schema");
                conversion.doLocalizeSchema();
                
                BuildSampleDatabase.makeFieldVisible(null, dscp);
                BuildSampleDatabase.makeFieldVisible(dscp.getType(), dscp);

                frame.incOverall();
                

                System.setProperty(AppPreferences.factoryName, "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
                System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
                
                createTableSummaryPage();
                
                boolean doFurtherTesting = false;
                if (doFurtherTesting)
                {
                    /*
                    BasicSQLUtils.deleteAllRecordsFromTable("datatype", BasicSQLUtils.myDestinationServerType);
                    BasicSQLUtils.deleteAllRecordsFromTable("specifyuser", BasicSQLUtils.myDestinationServerType);
                    BasicSQLUtils.deleteAllRecordsFromTable("usergroup", BasicSQLUtils.myDestinationServerType);
                    BasicSQLUtils.deleteAllRecordsFromTable("discipline", BasicSQLUtils.myDestinationServerType);

                    DataType          dataType  = createDataType("Animal");
                    UserGroup         userGroup = createUserGroup("Fish");
                    SpecifyUser       user      = createSpecifyUser("rods", "rods@ku.edu", (short)0, new UserGroup[] {userGroup}, SpecifyUserTypes.UserType.Manager.toString());



                    Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Collection.class);
                    criteria.add(Restrictions.eq("collectionId", new Integer(0)));
                    List<?> collectionList = criteria.list();

                    boolean doAddTissues = false;
                    if (doAddTissues)
                    {
                        deleteAllRecordsFromTable("collection", BasicSQLUtils.myDestinationServerType);
                        try
                        {
                            Session session = HibernateUtil.getCurrentSession();
                            HibernateUtil.beginTransaction();

                            Collection voucherSeries = null;
                            if (collectionList.size() == 0)
                            {
                                voucherSeries = new Collection();
                               // voucherSeries.setIsTissueSeries(false);
                                voucherSeries.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
                                voucherSeries.setCollectionId(100);
                                voucherSeries.setCollectionPrefix("KUFISH");
                                voucherSeries.setCollectionName("Fish Collection");
                                session.saveOrUpdate(voucherSeries);

                            } else
                            {
                                voucherSeries = (Collection)collectionList.get(0);
                            }

                            if (voucherSeries != null)
                            {
                                Collection tissueSeries = new Collection();
                               // tissueSeries.setIsTissueSeries(true);
                                tissueSeries.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
                                tissueSeries.setCollectionId(101);
                                tissueSeries.setCollectionPrefix("KUTIS");
                                tissueSeries.setCollectionName("Fish Tissue");
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

                    Set<Discipline>  disciplineSet = conversion.createDiscipline("Fish", dataType, user, null, null);


                    Object obj = disciplineSet.iterator().next();
                    Discipline discipline = (Discipline)obj;

                    conversion.convertBiologicalAttrs(discipline, null, null);*/
                }
                //conversion.showStats();
                
                conversion.cleanUp();
            }

            if (idMapperMgr != null && GenericDBConversion.shouldDeleteMapTables())
            {
                idMapperMgr.cleanup();
            }
            log.info("Done - " + dbNameDest);
            frame.setDesc("Done - " + dbNameDest);
            frame.setTitle("Done - " + dbNameDest);
            frame.incOverall();
            frame.processDone();

        } catch (Exception ex)
        {
            ex.printStackTrace();

            if (idMapperMgr != null && GenericDBConversion .shouldDeleteMapTables())
            {
                idMapperMgr.cleanup();
            }
            
        } finally
        {
            if (getSession() != null)
            {
                getSession().close();
            }
        }
    }
    
    /**
     * 
     */
    protected void createTableSummaryPage()
    {
        ConversionLogger.TableWriter tblWriter = convLogger.getWriter("TableSummary.html", "Table summary");
        tblWriter.startTable();
        tblWriter.println("<tr><th>Table</th><th>Count</th></tr>");
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            Integer count = BasicSQLUtils.getCount("select count(*) from "+ti.getName());
            if (count != null && count > 0)
            {
                tblWriter.log(ti.getName(), count.toString());
            }
        }
        tblWriter.endTable();
        tblWriter.startTable();
        
    }
    
    /**
     * @param conn
     * @return
     */
    protected boolean isOldDBOK(final Connection conn)
    {
    	StringBuilder errMsgs = new StringBuilder();
    	
        /*int coInnerCnt = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM collectionobject co INNER JOIN collectionobjectcatalog coc ON co.CollectionObjectID = coc.CollectionObjectCatalogID");
        int coLeftCnt1 = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM collectionobject co LEFT JOIN collectionobjectcatalog coc ON co.CollectionObjectID = coc.CollectionObjectCatalogID");
        int coLeftCnt2 = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM collectionobjectcatalog coc LEFT JOIN collectionobject co ON co.CollectionObjectID = coc.CollectionObjectCatalogID");
        
        int cntCO  = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM collectionobject");
        int cntCOC = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM collectionobjectcatalog");
        
        if (cntCO != cntCOC || coInnerCnt != coLeftCnt1 || coInnerCnt != coLeftCnt2)
        {
        	errMsgs.append("There is a mismatch between CollectionObjects and CollectionObjectCatalogs\n");
        }*/
        
        int cntACInnerCO = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM accession INNER JOIN collectionobjectcatalog ON accession.AccessionID = collectionobjectcatalog.AccessionID WHERE CollectionObjectTypeID < 20");
        int cntACLeftCO  = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM accession LEFT JOIN collectionobjectcatalog ON accession.AccessionID = collectionobjectcatalog.AccessionID WHERE CollectionObjectTypeID < 20");
        
        if (cntACInnerCO != cntACLeftCO)
        {
        	errMsgs.append("There is a mismatch between Accessions and its CollectionObject references.\n");
        }
        
        int cntDTInnerCO = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM determination INNER JOIN collectionobject ON determination.BiologicalObjectID = collectionobject.CollectionObjectID WHERE CollectionObjectTypeID < 20");
        int cntDTLeftCO  = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM determination LEFT JOIN collectionobject ON determination.BiologicalObjectID = collectionobject.CollectionObjectID WHERE CollectionObjectTypeID < 20");
        
        if (cntDTInnerCO != cntDTLeftCO)
        {
        	errMsgs.append("There is a mismatch between Determinations and its CollectionObject references.\n");
        }
        
        /*int cntPPInnerCO = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM collectionobjectcatalog INNER JOIN collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID");
        int cntPPLeftCO  = BasicSQLUtils.getCount(conn, "SELECT count(*) FROM collectionobjectcatalog LEFT JOIN collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID");
        
        if (cntPPInnerCO != cntPPLeftCO)
        {
        	errMsgs.append("There is a mismatch between Preparations and its CollectionObject references.\n");
        }*/
        
        if (errMsgs.length() > 0)
        {
        	UIRegistry.showError(errMsgs.toString());
        	return false;
        }
        return true;
    }
    
    /**
     * 
     */
    public static void addStorageTreeFomrXML(final boolean doAddTreeNodes)
    {
        BuildSampleDatabase bsd = new BuildSampleDatabase();
        Session tmpSession = HibernateUtil.getNewSession();
        bsd.setSession(tmpSession);
        
        Transaction    trans = null;
        try
        {
            List<?> list = tmpSession.createQuery("FROM StorageTreeDef WHERE id = 1").list();
            if (list != null)
            {
                StorageTreeDef std   = (StorageTreeDef)list.iterator().next();
                trans = tmpSession.beginTransaction();
                for (StorageTreeDefItem item : new Vector<StorageTreeDefItem>(std.getTreeDefItems()))
                {
                    for (Storage s : new Vector<Storage>(item.getTreeEntries()))
                    {
                        item.getTreeEntries().remove(s);
                        for (Preparation p : s.getPreparations())
                        {
                            p.setStorage(null);
                            tmpSession.saveOrUpdate(p);
                        }
                        s.getPreparations().clear();
                        tmpSession.delete(s);
                    }
                }
                trans.commit();
                tmpSession.flush();
                
                trans = tmpSession.beginTransaction();
                for (StorageTreeDefItem item : new Vector<StorageTreeDefItem>(std.getTreeDefItems()))
                {
                    std.getTreeDefItems().remove(item);
                    tmpSession.delete(item);
                }
                trans.commit();
                tmpSession.flush();
                
                File domFile = new File("demo_files/storage_init.xml");
                if (domFile.exists())
                {
                    trans = tmpSession.beginTransaction();
                    
                    Vector<Object> storages = new Vector<Object>();
                    bsd.createStorageTreeDefFromXML(storages, domFile, std, doAddTreeNodes);
                    trans.commit();
                    
                } else
                {
                    log.error("File["+domFile.getAbsolutePath()+"] is not found.");
                }
            }
            
        } catch (Exception ex)
        {
            if (trans != null)
            {
                trans.rollback();
            }
            ex.printStackTrace();
            
        } finally
        {
            tmpSession.close();
        }
        log.info("Done creating Storage treee.");
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
            //list = session.createQuery("from collection in class Collection").setFirstResult(1).setMaxResults(1000).list();
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
    protected boolean selectedDBsToConvert()
    {
        final JTextField     itUserNameTF = UIHelper.createTextField("root", 15);
        final JPasswordField itPasswordTF = UIHelper.createPasswordField("", 15);
        
        final JTextField     masterUserNameTF = UIHelper.createTextField("Master", 15);
        final JPasswordField masterPasswordTF = UIHelper.createPasswordField("Master", 15);
        
        final JTextField     hostNameTF = UIHelper.createTextField("localhost", 15);

        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,2px,p,2px,p,4px,p,2px,p,2px,p,8px,p,4px"));
        
        int y = 1;
        pb.addSeparator("IT User", cc.xyw(1, y, 4)); y += 2;
        pb.add(UIHelper.createLabel("Username:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(itUserNameTF, cc.xy(3, y)); y += 2;

        pb.add(UIHelper.createLabel("Password:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(itPasswordTF, cc.xy(3, y)); y += 2;

        pb.addSeparator("Master User", cc.xyw(1, y, 4)); y += 2;
        pb.add(UIHelper.createLabel("Username:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(masterUserNameTF, cc.xy(3, y)); y += 2;

        pb.add(UIHelper.createLabel("Password:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(masterPasswordTF, cc.xy(3, y)); y += 2;

        pb.add(UIHelper.createLabel("Host Name:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(hostNameTF, cc.xy(3, y)); y += 2;

        CustomDialog dlg = new CustomDialog(null, "Database Info", true, pb.getPanel());
        ((JPanel)dlg.getContentPanel()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UIHelper.centerAndShow(dlg);
        
        dlg.dispose();
        if (dlg.isCancelled())
        {           
           return false;
        }
        
        hostName        = hostNameTF.getText();
        itUsrPwd.first  = itUserNameTF.getText();
        itUsrPwd.second = ((JTextField)itPasswordTF).getText();
        
        masterUsrPwd.first  = masterUserNameTF.getText();
        masterUsrPwd.second = ((JTextField)masterPasswordTF).getText();
        
        return true;
    }
    
    public CustomDBConverterDlg runCustomConverter()
    {       
        return UIHelper.doSpecifyConvert();
    }
    
    //----------------------------------------
    class DBNamePair extends Pair<String, String>
    {
        
        /**
         * 
         */
        public DBNamePair()
        {
            super();
        }

        /**
         * @param first
         * @param second
         */
        public DBNamePair(String first, String second)
        {
            super(first, second);
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.util.Pair#toString()
         */
        @Override
        public String toString()
        {
            return first + "   ("+ second + ")";
        }
        
    }
}
