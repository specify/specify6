/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (Pat your option) any later version.
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
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
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;
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

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.db.MySQLBackupService;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.MySQLDMBSUserMgr;
import edu.ku.brc.dbsupport.ResultsPager;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
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
import edu.ku.brc.specify.dbsupport.PostInsertEventListener;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.specify.ui.AppBase;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.Triple;

/**
 * Create more sample data, letting Hibernate persist it for us.
 *
 * @code_status Beta
 * @author rods
 */
public class SpecifyDBConverter extends AppBase
{
    protected static final Logger log = Logger.getLogger(SpecifyDBConverter.class);

    protected static final int                  OVERALL_STEPS     = 22;
    
    protected static Hashtable<String, Integer> prepTypeMapper    = new Hashtable<String, Integer>();
    protected static int                        attrsId           = 0;
    protected static SimpleDateFormat           dateFormatter     = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static StringBuffer               strBuf            = new StringBuffer("");
    protected static Calendar                   calendar          = Calendar.getInstance();
    
    protected long                              startTime;
    protected long                              endTime;
    protected long                              waitTime;
    
    protected static boolean                    doFixCollectors   = false;
    
    protected Pair<String, String>              namePairToConvert = null;
    
    protected static ProgressFrame              frame             = null;
    
    protected Pair<String, String>              itUsrPwd     = new Pair<String, String>(null, null);
    protected Pair<String, String>              masterUsrPwd = new Pair<String, String>("Master", "Master");
    protected String                            hostName     = "localhost";
    
    protected GenericDBConversion               conversion;
    protected ConversionLogger                  convLogger   = new ConversionLogger();
    
    /**
     * Constructor.
     */
    public SpecifyDBConverter()
    {
        PostInsertEventListener.setAuditOn(false);
        
        setUpSystemProperties();
        
        AppContextMgr.getInstance().setHasContext(true);
        
        // Load Local Prefs
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());

        // Then set this
        IconManager.setApplicationClass(Specify.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$
        
        appIcon = new JLabel("  "); //$NON-NLS-1$
        setAppIcon(null); //$NON-NLS-1$

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
        
        UIRegistry.setEmbeddedDBPath(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
        
        AppBase.processArgs(args);
        
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
                    if (converter.selectedDBsToConvert(false))
                    {
                        namePair = converter.chooseTable("Select a DB to Convert", "Specify 5 Databases", true);
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
    public Pair<String, String> chooseTable(final String title, 
                                            final String subTitle,
                                            final boolean doSp5DBs) throws SQLException
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
                String dbName = row[0].toString();
                
                //System.out.print("Database Found ["+dbName+"]  ");
                conn.setCatalog(dbName);
                
                boolean isSp5DB = false;
                Vector<Object[]> tables = BasicSQLUtils.query(conn, "show tables");
                for (Object[] tblRow : tables)
                {
                    String tableName = tblRow[0].toString();
                    if (tableName.equalsIgnoreCase("usysversion"))
                    {
                        isSp5DB = true;
                        break;
                    }
                }
                
                if ((!isSp5DB && doSp5DBs) || (isSp5DB && !doSp5DBs))
                {
                    continue;
                }
                
                // make all table names lowercase
                try
                {
                    Integer count = BasicSQLUtils.getCount(conn, "select COUNT(*) FROM collection");
                    if (count == null)
                    {
                        for (Object[] tblRow : tables)
                        {
                            String tableName = tblRow[0].toString();
                            BasicSQLUtils.update(conn, "RENAME TABLE " + tableName + " TO "+ tableName.toLowerCase());
                        }
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
                Vector<Object> tableDesc = BasicSQLUtils.querySingleCol(conn, "select CollectionName FROM collection");
                if (tableDesc.size() > 0)
                {
                    String collName =  tableDesc.get(0).toString();
                    availPairs.add(new DBNamePair(collName, row[0].toString()));
                }
            }
            
            Collections.sort(availPairs, new Comparator<Pair<String, String>>() {
                @Override
                public int compare(Pair<String, String> o1, Pair<String, String> o2)
                {
                    return o1.second.compareTo(o2.second);
                }
            });
            
            mgr.close();
            BasicSQLUtils.setSkipTrackExceptions(false);
            
            final JList     list = new JList(availPairs);
            CellConstraints cc   = new CellConstraints();
            PanelBuilder    pb   = new PanelBuilder(new FormLayout("f:p:g", "p,4px,f:p:g"));
            pb.add(UIHelper.createLabel(subTitle), cc.xy(1, 1));
            pb.add(UIHelper.createScrollPane(list, true), cc.xy(1,3));
            pb.setDefaultDialogBorder();
            
            final CustomDialog dlg = new CustomDialog(null, title, true, pb.getPanel());
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
    /*protected void setUpSystemProperties()
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
    }*/
    
    /**
     * @param oldDBConn
     */
    private void fixOldTablesTimestamps(final Connection oldDBConn)
    {
        // Makes sure old data has all the TimestampCreated filled in
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Timestamp        now               = new Timestamp(System .currentTimeMillis());
        String           nowStr            = dateTimeFormatter.format(now);
        List<String>     tableNames        = BasicSQLUtils.getTableNames(oldDBConn);
        
        frame.setProcess(0, tableNames.size());
        
        int cnt = 0;
        for (String tableName : tableNames)
        {
            frame.setProcess(cnt++);
            
            if (!tableName.toLowerCase().startsWith("usys") && 
                !tableName.toLowerCase().startsWith("web") && 
                !tableName.toLowerCase().equals("taxonomytype") && 
                !tableName.toLowerCase().equals("taxonomicunittype") && 
                !tableName.toLowerCase().equals("reports"))
            {
                try
                {
                    System.out.println("Table: "+tableName);
                    
                    List<String> fieldNames = BasicSQLUtils.getFieldNamesFromSchema(oldDBConn, tableName);
                    for (String fieldName : fieldNames)
                    {
                        if (fieldName.equals("TimestampCreated"))
                        {
                            if (BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM " + tableName + " WHERE TimestampCreated IS NULL") > 0)
                            {
                                BasicSQLUtils.update(oldDBConn, "UPDATE "+tableName+ " SET TimestampCreated='"+nowStr+"' WHERE TimestampCreated IS NULL");
                            }
                            break;
                        }
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        frame.setProcess(tableNames.size());
        log.debug("Done setting Timestamps");
    }
    

    /**
     * Convert old Database to New 
     * @param databaseNameSource name of an old database
     * @param databaseNameDest name of new DB
     * @throws Exception xx
     */
    @SuppressWarnings("unchecked")
    protected  void convertDB(final String dbNameSource, 
                              final String dbNameDest) throws Exception
    {
        //System.setProperty(DBMSUserMgr.factoryName, "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");

        AppContextMgr.getInstance().clear();
        
        boolean startfromScratch    = true; 
        boolean deleteMappingTables = false;
        
        
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
        frame.setDesc("Gather statistics from " + dbNameDest);
        frame.turnOffOverAll();
        frame.getProcessProgress().setIndeterminate(true);
        
        UIHelper.centerAndShow(frame);
        
        DBConnection oldDB = DBConnection.createInstance(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), dbNameDest, oldConnStr, itUsrPwd.first, itUsrPwd.second);
        
        Connection oldDBConn = oldDB.createConnection();
        Connection newDBConn = DBConnection.getInstance().createConnection();
        
        if (!isOldDBOK(oldDBConn))
        {
            return;
        }
        
        boolean doFix = false;
        if (doFix)
        {
            IdMapperMgr.getInstance().setDBs(oldDBConn, newDBConn);
            DuplicateCollectingEvents dce = new DuplicateCollectingEvents(oldDBConn, newDBConn);
            //dce.performMaint(true);
            dce.fixCollectorsForCollectingEvents2();
            //dce.removeUnneededCEs();
            return;
        }
        
        if (!System.getProperty("user.name").equals("rods"))
        {
            OldDBStatsDlg dlg = new OldDBStatsDlg(oldDBConn);
            frame.setVisible(false);
            
            dlg.setVisible(true);
            if (dlg.isCancelled())
            {
                oldDBConn.close();
                newDBConn.close();
                System.exit(0);
            }
            doFixCollectors = dlg.doFixAgents();
        }
        
        startTime = System.currentTimeMillis();
        
        convLogger.initialize(dbNameDest);
        convLogger.setIndexTitle(dbNameDest + " Conversion "+(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(Calendar.getInstance().getTime()));

        
        /*if (true)
        {
            TableWriter tDSTblWriter = convLogger.getWriter("TableDataSummary.html", "Table Data Summary", true);
            TableDataChecker tblDataChecker = new TableDataChecker(oldDBConn);
            tblDataChecker.createHTMLReport(tDSTblWriter);
            tDSTblWriter.close();
            return;
        }*/
        
        
        //ConvertMiscData.moveHabitatToStratSp5(oldDBConn);

        boolean doFix2 = false;
        if (doFix2)
        {
            ConvertMiscData.convertMethodFromStratGTP(oldDBConn, newDBConn);
            return;
        }
        
        frame.setSize(500, frame.getPreferredSize().height);
        
        frame.setDesc("Fixing NULL Timestamps for conversion.");
        UIHelper.centerAndShow(frame);
        
        //fixOldTablesTimestamps(oldDBConn);
        
        frame.turnOnOverAll();
        
        conversion = new GenericDBConversion(oldDBConn, newDBConn, dbNameSource, convLogger);
        GenericDBConversion.CollectionResultType collInitStatus = conversion.initialize();
        if (collInitStatus == GenericDBConversion.CollectionResultType.eError)
        {
            oldDBConn.close();
            newDBConn.close();
            throw new RuntimeException("There are no collections!");
            
        } else if (collInitStatus == GenericDBConversion.CollectionResultType.eCancel)
        {
            oldDBConn.close();
            newDBConn.close();
            System.exit(0);
        }
        
        boolean doFix3 = false;
        if (doFix3)        
        {
            IdMapperMgr.getInstance().setDBs(oldDBConn, newDBConn);
            AgentConverter agentConverter = new AgentConverter(conversion, IdMapperMgr.getInstance(), false);
            agentConverter.fixMissingAddrsFromConv();
            oldDBConn.close();
            newDBConn.close();
            return;
        }
        
        boolean doFix4 = false;
        if (doFix4)        
        {
            IdMapperMgr.getInstance().setDBs(oldDBConn, newDBConn);
            ConvertMiscData.moveStratFieldsToCEA(oldDBConn, newDBConn);
            oldDBConn.close();
            newDBConn.close();
            return;
        }
        
        String sql = "SELECT count(*) FROM (SELECT ce.CollectingEventID, Count(ce.CollectingEventID) as cnt FROM collectingevent AS ce " +
        "Inner Join collectionobject AS co ON ce.CollectingEventID = co.CollectingEventID " +
        "Inner Join collectionobjectcatalog AS cc ON co.CollectionObjectID = cc.CollectionObjectCatalogID " +    
        "WHERE ce.BiologicalObjectTypeCollectedID <  21 " +
        "GROUP BY ce.CollectingEventID) T1 WHERE cnt > 1";

        /*if (true)
        {
            IdMapperMgr.getInstance().setDBs(oldDBConn, newDBConn);
            
            //convLogger.initialize(dbNameDest);
            //convLogger.setIndexTitle(dbNameDest + " Conversion "+(new SimpleDateFormat("yyy-MM-dd hh:mm:ss")).format(Calendar.getInstance().getTime()));
            
            TableWriter tblWriter = convLogger.getWriter("ScopeUpdater.html", "Updating Scope Summary");
            ConvScopeFixer convScopeFixer = new ConvScopeFixer(oldDBConn, newDBConn, dbNameDest, tblWriter);
            convScopeFixer.doFixTables();
            convScopeFixer.checkTables();
            return;
        }*/
        
        int numCESharing = BasicSQLUtils.getCountAsInt(oldDBConn, sql);

        String msg = String.format("Will this Collection share Collecting Events?\nThere are %d Collecting Events that are sharing now.\n(Sp5 was %ssharing them.)", numCESharing, isUsingEmbeddedCEsInSp5() ? "NOT " : "");
        boolean doingOneToOneForColObjToCE = !UIHelper.promptForAction("Share", "Adjust CEs", "Duplicate Collecting Events", msg);

        /*if (false) 
        {
            createTableSummaryPage();
            return;
        }*/
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                conversion.setFrame(frame);
                frame.setDesc("Building Database Schema...");
                frame.adjustProgressFrame();
                frame.getProcessProgress().setIndeterminate(true);
                frame.getProcessProgress().setString("");
                UIHelper.centerAndShow(frame);

            }
        });
        
        
        if (startfromScratch)
        {
            boolean doBuild = true;
            File    file    = new File("blank.sql");
            System.err.println(file.getAbsolutePath());
            if (file.exists())
            {
                DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
                if (dbMgr.connect(itUsrPwd.first, itUsrPwd.second, "localhost", dbNameDest))
                {
                    if (dbMgr.doesDBExists(dbNameDest))
                    {
                        dbMgr.dropDatabase(dbNameDest);
                    }
                    
                    if (dbMgr.createDatabase(dbNameDest))
                    {
                        doBuild = false;
                    }
                    
                    dbMgr.close();
                }

                
                MySQLBackupService bkService = new MySQLBackupService();
                
                doBuild = !bkService.doRestore(file.getAbsolutePath(),"/usr/local/mysql/bin/mysql", dbNameDest, itUsrPwd.first, itUsrPwd.second);
            } 
            
            if (doBuild)
            {
                DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
                if (dbMgr.connect(itUsrPwd.first, itUsrPwd.second, "localhost", dbNameDest))
                {
                    if (dbMgr.doesDBExists(dbNameDest))
                    {
                        dbMgr.dropDatabase(dbNameDest);
                    }
                    
                    if (dbMgr.createDatabase(dbNameDest))
                    {
                        doBuild = false;
                    }
                    
                    dbMgr.close();
                }

                log.debug("Starting from scratch and generating the schema");
                SpecifySchemaGenerator.generateSchema(driverInfo, hostName, dbNameDest, itUsrPwd.first, itUsrPwd.second);
            }
        }
        
        log.debug("Preparing new database: completed");

        setSession(HibernateUtil.getNewSession());
        IdMapperMgr idMapperMgr = null;
        SpecifyUser specifyUser = null;

        try
        {
        	GenericDBConversion.setShouldCreateMapTables(startfromScratch);
            GenericDBConversion.setShouldDeleteMapTables(deleteMappingTables);
            
            frame.setOverall(0, OVERALL_STEPS);
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
                
                /*if (false)
                {
                    addStorageTreeFomrXML(true);
                    return;
                }*/
                
                //---------------------------------------------------------------------------------------
                //-- Create basic set of information.
                //---------------------------------------------------------------------------------------
                conversion.doInitialize();
                
                boolean doAll = true;
                
                if (startfromScratch)
                {
                    BasicSQLUtils.deleteAllRecordsFromTable(conversion.getNewDBConnection(), "agent", BasicSQLUtils.myDestinationServerType);
                    BasicSQLUtils.deleteAllRecordsFromTable(conversion.getNewDBConnection(), "address", BasicSQLUtils.myDestinationServerType);
                }
                conversion.initializeAgentInfo(startfromScratch);
                
                frame.setDesc("Mapping Tables.");
                log.info("Mapping Tables.");
                boolean mapTables = true;
                if (mapTables)
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
                
                conversion.convertDivision(institutionId);
                frame.incOverall();
                
                Agent userAgent = null;
                if (startfromScratch)
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
                    specifyUser = (SpecifyUser)getSession().createCriteria(SpecifyUser.class).list().get(0);
                    userAgent   = specifyUser.getAgents().iterator().next();
                    
                    AppContextMgr.getInstance().setClassObject(SpecifyUser.class, specifyUser);
                    // XXX Works for a Single Convert
                    Collection collection = (Collection)getSession().createCriteria(Collection.class).list().get(0);
                    AppContextMgr.getInstance().setClassObject(Collection.class, collection);
                }
                
                /////////////////////////////////////////////////////////////
                // Really need to create or get a proper Discipline Record
                /////////////////////////////////////////////////////////////
                TableWriter taxonTblWriter = convLogger.getWriter("FullTaxon.html", "Taxon Conversion");
                ConvertTaxonHelper           taxonHelper    = new ConvertTaxonHelper(oldDBConn, newDBConn, dbNameDest, frame, taxonTblWriter, conversion, conversion);
                taxonHelper.createTaxonIdMappings();
                taxonHelper.doForeignKeyMappings();
                
                frame.setDesc("Converting CollectionObjectDefs.");
                log.info("Converting CollectionObjectDefs.");
                boolean convertDiscipline = true;
                if (convertDiscipline)
                {
                    conversion.convertCollectionObjectTypes(specifyUser.getSpecifyUserId(), userAgent);

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
                boolean copyAgentAddressTables = doAll;
                if (copyAgentAddressTables)
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
                if (mapTables)
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
                
                
                TableWriter gtpTblWriter = convLogger.getWriter("GTP.html", "Geologic Time Period");
                StratToGTP  stratToGTP   = null;//new StratToGTP(oldDBConn, newDBConn, dbNameDest, gtpTblWriter);

                
                frame.setDesc("Converting Geography");
                log.info("Converting Geography");
                boolean doGeography = doAll;
                if (!dbNameDest.startsWith("accessions") && doGeography)
                {
                    GeographyTreeDef treeDef = conversion.createStandardGeographyDefinitionAndItems(true);
                    conversion.convertGeography(treeDef, null, true);
                }
                frame.incOverall();

                frame.setDesc("Converting Geologic Time Period.");
                log.info("Converting Geologic Time Period.");
                // GTP needs to be converted here so the stratigraphy conversion can use
                // the IDs
                boolean doGTP = doAll;
                if (doGTP)
                {
                    if (stratToGTP != null)
                    {
                        stratToGTP.createTreeDef();
                        
                    } else
                    {
                        GeologicTimePeriodTreeDef treeDef = conversion.convertGTPDefAndItems(conversion.isPaleo());
                        conversion.convertGTP(gtpTblWriter, treeDef, conversion.isPaleo());
                    }
                } else
                {
                    idMapperMgr.addTableMapper("geologictimeperiod", "GeologicTimePeriodID");
                    idMapperMgr.mapForeignKey("Stratigraphy", "GeologicTimePeriodID", "GeologicTimePeriod", "GeologicTimePeriodID");
                }
                frame.incOverall();

                frame.setDesc("Converting Taxonomy");
                log.info("Converting Taxonomy");
                boolean doTaxonomy = doAll;
                if (doTaxonomy)
                {
                    BasicSQLUtils.setTblWriter(taxonTblWriter);
                    taxonHelper.doConvert();
                    BasicSQLUtils.setTblWriter(null);
                }
                frame.incOverall();

                //checkDisciplines();

                //-------------------------------------------------------------------------------
                // Get the Discipline Objects and put them into the CollectionInfo Objects
                //-------------------------------------------------------------------------------
                //conversion.loadDisciplineObjects();
                
                conversion.convertHabitat();
                
                frame.setDesc("Converting Determinations Records");
                log.info("Converting Determinations Records");
                boolean doDeterminations = doAll;
                if (doDeterminations)
                {
                    frame.incOverall();
                    conversion.convertDeterminationRecords();
                } else
                {
                    frame.incOverall();
                }
                frame.incOverall();

                //checkDisciplines();

                
                frame.setDesc("Copying Tables");
                log.info("Copying Tables");
                boolean copyTables = doAll;
                if (copyTables)
                {
                    boolean doBrief  = false;
                    conversion.copyTables(doBrief);
                }

                frame.incOverall();

                //checkDisciplines();
                
                conversion.updateHabitatIds();

                frame.setDesc("Converting Locality");
                log.info("Converting Locality");
                
                boolean doLocality = doAll;
                if (!dbNameDest.startsWith("accessions") && (doGeography || doLocality))
                {
                    conversion.convertLocality();
                    frame.incOverall();
                    
                } else
                {
                    frame.incOverall();
                    frame.incOverall();
                }

                //checkDisciplines();
                
                frame.setDesc("Converting DeaccessionCollectionObject");
                log.info("Converting DeaccessionCollectionObject");
                boolean doDeaccessionCollectionObject = doAll;
                if (doDeaccessionCollectionObject)
                {
                    conversion.convertDeaccessionCollectionObject();
                }
                frame.incOverall();          

                //checkDisciplines();

                frame.setDesc("Converting Preparations");
                log.info("Converting Preparations");
                boolean doCollectionObjects = doAll;
                if (doCollectionObjects)
                {
                    if (true)
                    {
                        Session session = HibernateUtil.getCurrentSession();
                        try
                        {
                            // Get a HashMap of all the PrepTypes for each Collection
                            Hashtable<Integer, Map<String, PrepType>> collToPrepTypeHash = new Hashtable<Integer, Map<String,PrepType>>();
                            Query   q = session.createQuery("FROM Collection");
                            for (Object dataObj :  q.list())
                            {
                                Collection            collection  = (Collection)dataObj;
                                Map<String, PrepType> prepTypeMap = conversion.createPreparationTypesFromUSys(collection); // Hashed by PrepType's Name
                                
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
                                // So Cache a Map of PrepTYpes for each Collection
                                collToPrepTypeHash.put(collection.getCollectionId(), prepTypeMap);
                            }
                            conversion.convertPreparationRecords(collToPrepTypeHash);

                            //checkDisciplines();

                        } catch (Exception ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    }
                    
                    frame.setDesc("Converting Loan Records");
                    log.info("Converting Loan Records");
                    boolean doLoanPreparations = doAll;
                    if (doLoanPreparations)
                    {
                    	conversion.convertLoanRecords(false);     // Loans
                    	conversion.convertLoanAgentRecords(false);// Loans
                        conversion.convertLoanPreparations();
                        
                        frame.setDesc("Converting Gift Records");
                        log.info("Converting Gift Records");
                    	conversion.convertLoanAgentRecords(true); // Gifts
                    	conversion.convertLoanRecords(true);      // Gifts
                        conversion.convertGiftPreparations();
                        frame.incOverall();
                        
                    } else
                    {
                        frame.incOverall();
                    }

                    //checkDisciplines();

                    // Arg1 - Use Numeric Catalog Number
                    // Arg2 - Use the Prefix from Catalog Series
                    frame.setDesc("Converting CollectionObjects Records");
                    log.info("Converting CollectionObjects Records");
                    conversion.convertCollectionObjects(true, false);
                    frame.incOverall();

                    
                }  else
                {
                    frame.incOverall();
                    frame.incOverall();
                }
                
                conversion.updateBioLogicalObjAttrIds();;
                conversion.updatePrepAttrIds();

                //checkDisciplines();

                //conversion.convertHostTaxonId();
                
                if (getSession() != null)
                {
                    getSession().close();
                    setSession(null);
                }
                
                setSession(HibernateUtil.getNewSession());
                
                frame.setDesc("Converting Straigraphy");
                log.info("Converting Straigraphy");
                boolean doStrat = true;
                if (doStrat)
                {
                     TableWriter tblWriter = convLogger.getWriter("FullStrat.html", "Straigraphy Conversion");
                     
                     conversion.convertStrat(tblWriter, conversion.isPaleo());
                }

                //checkDisciplines();

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

                    //checkDisciplines();

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

                    //checkDisciplines();
                    
                    if (stratToGTP != null)
                    {
                        stratToGTP.convertStratToGTP();
                    }

                    division    = dscp.getDivision();
                    localSession.lock(division, LockMode.NONE);
                    institution = division.getInstitution();
                    localSession.lock(institution, LockMode.NONE);
                    institution.getDivisions().size();
                    
                    AppContextMgr.getInstance().setClassObject(Collection.class, collection);
                    AppContextMgr.getInstance().setClassObject(Division.class, division);
                    AppContextMgr.getInstance().setClassObject(Institution.class, institution);

                    //checkDisciplines();

                    
                    if (doFixCollectors)
                    {
                        agentConverter.fixupForCollectors(division, dscp);
                    }

                    //checkDisciplines();

                    setSession(localSession);
                    
                    try
                    {
                        for (CollectionInfo collInfo : CollectionInfo.getCollectionInfoList(oldDBConn))
                        {
                            if (collInfo.getCollectionId() == null)
                            {
                                log.error("CollectionID: was null for "+collInfo.getCatSeriesName());
                                continue;
                            }
                            List<?> list = localSession.createQuery("FROM Collection WHERE id = " + collInfo.getCollectionId()).list();
                            
                            List<Collection> tmpCollList   = (List<Collection>)list;
                            Collection       tmpCollection = tmpCollList.get(0);
                            
                            // create the standard user groups for this collection
                            Map<String, SpPrincipal> groupMap = createStandardGroups(localSession, tmpCollection);

                            // add the administrator as a Collections Manager in this group
                            specifyUser.addUserToSpPrincipalGroup(groupMap.get(SpecifyUserTypes.UserType.Manager.toString()));
                            
                            Transaction trans = localSession.beginTransaction();
                            
                            for (SpPrincipal prin : groupMap.values())
                            {
                                localSession.saveOrUpdate(prin);
                            }
                            
                            localSession.saveOrUpdate(specifyUser);
                            
                            trans.commit();

                        }

                        //checkDisciplines();

                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    
                    status = true;
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                

                //checkDisciplines();

                localSession.close();
                
                localSession = HibernateUtil.getNewSession();
                setSession(localSession);
                
                specifyUser = (SpecifyUser)localSession.merge(specifyUser);
                
                division       = (Division)localSession.createQuery("FROM Division WHERE id = " + division.getId()).list().iterator().next();
                institution    = (Institution)localSession.createQuery("FROM Institution WHERE id = " + institution.getId()).list().iterator().next();
                collection     = (Collection)localSession.createQuery("FROM Collection WHERE id = " + collection.getId()).list().iterator().next();
                
                AppContextMgr.getInstance().setClassObject(Collection.class, collection);
                AppContextMgr.getInstance().setClassObject(Division.class,   division);
                AppContextMgr.getInstance().setClassObject(Institution.class, institution);
                
                dscp = (Discipline)localSession.createQuery("FROM Discipline WHERE id = " + dscp.getId()).list().iterator().next();
                AppContextMgr.getInstance().setClassObject(Discipline.class, dscp);

                //checkDisciplines();

                localSession.flush();
                
                setSession(cachedCurrentSession);
                
                frame.setDesc("Converting USYS Tables.");
                log.info("Converting USYS Tables.");
                boolean copyUSYSTables = doAll;
                if (copyUSYSTables)
                {
                    if (status)
                    {
                        BasicSQLUtils.deleteAllRecordsFromTable("picklist", BasicSQLUtils.myDestinationServerType);
                        BasicSQLUtils.deleteAllRecordsFromTable("picklistitem", BasicSQLUtils.myDestinationServerType);
    
                        for (Collection collectionObj : (List<Collection>)localSession.createQuery("FROM Collection").list())
                        {
                            log.debug("Loading PickLists for Collection ["+collectionObj.getCollectionName()+"] id["+collectionObj.getId()+"]");
                            
                            conversion.convertUSYSTables(localSession, collectionObj);
                            
                            frame.setDesc("Creating PickLists from XML.");
                            
                            BuildSampleDatabase.createPickLists(localSession, null, true, collectionObj);
                            
                            BuildSampleDatabase.createPickLists(localSession, collection.getDiscipline(), true, collectionObj);
                        }
                        
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

                //checkDisciplines();

                frame.incOverall();
                
                frame.setDesc("Fixing Preferred Taxon");
                
                // MySQL Only ???
                sql = "UPDATE determination SET PreferredTaxonID = CASE WHEN " +
                             "(SELECT AcceptedID FROM taxon WHERE taxon.TaxonID = determination.TaxonID) IS NULL " +
                             "THEN determination.TaxonID ELSE (SELECT AcceptedID FROM taxon WHERE taxon.TaxonID = determination.TaxonID) END";
                System.out.println(sql);
                BasicSQLUtils.setSkipTrackExceptions(true);
                BasicSQLUtils.update(sql);
                
                frame.incOverall();
                
                ShipmentConverter shipmentConv = new ShipmentConverter(oldDBConn, newDBConn);
                shipmentConv.fixup();
                
                //------------------------------------------------
                // Localize Schema and make form fields visible
                //------------------------------------------------
                frame.setDesc("Localizing the Schema");
                conversion.doLocalizeSchema();

                frame.incOverall();
                
                //HabitatTaxonIdConverter habitatConverter = new HabitatTaxonIdConverter(oldDB.getConnection(), newDBConn);
                //habitatConverter.convert(conversion.getCollectionMemberId());
                
                frame.incOverall();
                
                agentConverter.fixAddressOfRecord();
                
                frame.incOverall();
                
                if (dbNameSource.startsWith("gcf"))
                {
                    GulfInvertsFixer giFixer = new GulfInvertsFixer(oldDBConn, newDBConn, dbNameSource, null);
                    giFixer.convert(conversion.getCollectionMemberId());
                }
                //checkDisciplines();

                TableWriter tblWriter = convLogger.getWriter("ScopeUpdater.html", "Updating Scope Summary");
                ConvScopeFixer convScopeFixer = new ConvScopeFixer(oldDBConn, newDBConn, dbNameDest, tblWriter);
                convScopeFixer.doFixTables();
                convScopeFixer.checkTables();
                
                waitTime = 0;
                
                /*
                long stTime = System.currentTimeMillis();
                
                sql = "SELECT count(*) FROM (SELECT ce.CollectingEventID, Count(ce.CollectingEventID) as cnt FROM collectingevent AS ce " +
                        "Inner Join collectionobject AS co ON ce.CollectingEventID = co.CollectingEventID " +
                        "Inner Join collectionobjectcatalog AS cc ON co.CollectionObjectID = cc.CollectionObjectCatalogID " +    
                        "WHERE ce.BiologicalObjectTypeCollectedID <  21 " +
                        "GROUP BY ce.CollectingEventID) T1 WHERE cnt > 1";
                
                int numCESharing = BasicSQLUtils.getCountAsInt(oldDBConn, sql);

                String msg = String.format("Will this Collection share Collecting Events?\nThere are %d Collecting Events that are sharing now.\n(Sp5 was %ssharing them.)", numCESharing, isUsingEmbeddedCEsInSp5() ? "NOT " : "");
                boolean doingOneToOneForColObjToCE = !UIHelper.promptForAction("Share", "Adjust CEs", "Duplicate Collecting Events", msg);
                
                waitTime = System.currentTimeMillis() - stTime;
                */
                
                File ceFile = new File(dbNameDest+".ce_all");
                if (doingOneToOneForColObjToCE)
                {
                    DuplicateCollectingEvents dce = new DuplicateCollectingEvents(oldDBConn, newDBConn, frame, conversion.getCurAgentCreatorID(), dscp.getId());
                    dce.performMaint(true);
                    
                    FileUtils.writeStringToFile(ceFile, dbNameDest);
                    
                } else if (ceFile.exists())
                {
                    ceFile.delete();
                }
                
                //endTime = System.currentTimeMillis();
                
                int convertTimeInSeconds = (int)((endTime - startTime - waitTime) / 1000.0);
                
                int               colObjCnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM collectionobject");
                ConvertStatSender sender    = new ConvertStatSender();
                sender.senConvertInfo(dbNameDest, colObjCnt, convertTimeInSeconds);
                
                frame.incOverall();
                
                fixHibernateHiLo(newDBConn);
                
                DisciplineDuplicator d = new DisciplineDuplicator(conversion.getOldDBConn(), conversion.getNewDBConn(), tblWriter, frame, conversion);
                d.doShowFieldsForDiscipline();
                d.duplicateCollectingEvents();
                d.duplicateLocalities();
                d.duplicateGeography();
                
                frame.setDesc("Running Table Checker to report on fields with data.");
                TableWriter      tDSTblWriter   = convLogger.getWriter("TableDataSummary.html", "Table Data Summary", true);
                TableDataChecker tblDataChecker = new TableDataChecker(oldDBConn);
                tblDataChecker.createHTMLReport(tDSTblWriter);
                //tDSTblWriter.close();
                
                updateVersionInfo(newConn);
                
                if (dbNameDest.startsWith("kui_fish_"))
                {
                    ConvertMiscData.convertKUFishCruiseData(oldDBConn, newDBConn, conversion.getCurDisciplineID());
                }
                
                // Check for mismatched Disciplines for CE and CE Attrs
                sql = "SELECT Count(ce.CollectingEventID) FROM collectingevent AS ce " +
                        "Inner Join collectingeventattribute AS cea ON ce.CollectingEventAttributeID = cea.CollectingEventAttributeID " +    
                        "WHERE ce.DisciplineID <> cea.DisciplineID";
                
                int ceCnt = BasicSQLUtils.getCountAsInt(sql);
                if (ceCnt > 0)
                {
                    UIRegistry.showErrorNonModal(String.format("There are %d CollectingEvents and CE Attributes where their DisciplineID do not match.", ceCnt));
                }
                
                // Check for ColObjs that have bad DisciplineIDs compared to the Collection's Discipline
                sql = "SELECT Count(ce.CollectingEventID) FROM collectingevent AS ce " +
                        "Inner Join collectionobject AS co ON ce.CollectingEventID = co.CollectingEventID " +
                        "Inner Join collection ON co.CollectionID = collection.UserGroupScopeId " +    
                        "WHERE ce.DisciplineID <>  collection.DisciplineID";
                int dspCnt = BasicSQLUtils.getCountAsInt(sql);
                if (dspCnt > 0)
                {
                    UIRegistry.showErrorNonModal(String.format("There are %d mismatches between the Collection Object Discipline and the Discipline of the Colleciton it is in", dspCnt));
                }
                
                // Check for One-To-One for ColObj -> CE
                if (doingOneToOneForColObjToCE)
                {
                    sql = "SELECT COUNT(*) FROM (SELECT ce.CollectingEventID, Count(ce.CollectingEventID) AS cnt FROM collectingevent AS ce " +
                            "Inner Join collectionobject AS co ON ce.CollectingEventID = co.CollectingEventID " +
                            "GROUP BY ce.CollectingEventID) T1 WHERE cnt > 1";
                    ceCnt = BasicSQLUtils.getCountAsInt(sql);
                    if (ceCnt > 0)
                    {
                        sql = "SELECT id,cnt FROM (SELECT ce.CollectingEventID as id, Count(ce.CollectingEventID) AS cnt FROM collectingevent AS ce " +
                                "Inner Join collectionobject AS co ON ce.CollectingEventID = co.CollectingEventID " +
                                "GROUP BY ce.CollectingEventID) T1 WHERE cnt > 1";
                        for (Object[] row : BasicSQLUtils.query(sql))
                        {
                            log.debug(String.format("CE[%s] has %s Collection Objects.", row[0].toString(), row[1].toString()));
                        }
                        UIRegistry.showErrorNonModal(String.format("There are %d CollectingEvents that have more than one Collection Object and they are suppose to be a One-To-One", ceCnt));
                    }
                }
                
                log.info("Done - " + dbNameDest + " " + convertTimeInSeconds);
                frame.setDesc("Done - " + dbNameDest + " " + convertTimeInSeconds);


                //System.setProperty(AppPreferences.factoryName, "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");    // Needed by AppReferences
                System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
                
                createTableSummaryPage();
                conversion.cleanUp();
            }

            if (idMapperMgr != null && GenericDBConversion.shouldDeleteMapTables())
            {
                idMapperMgr.cleanup();
            }

            //This will convert Specify5 queries exported from the 5 database to a file that can be imported
            //into the six database. Need to have standard location for files. Can probably also import the queries here.
            //QueryConverter.convert("/home/timo/Desktop/KUI_FishQueries.xml", "/home/timo/convertedSpQ.xml", "/home/timo/UnconvertedFields.xml");

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
    
    protected void checkDisciplines()
    {
        System.out.println("Checking kDisciplines....");
        int count = 0;
        for (Object obj : BasicSQLUtils.querySingleCol("SELECT TaxonTreeDefID FROM discipline"))
        {
            if (((Integer)obj) == 1)
            {
                count++;
            }
        }
        
        System.out.println("Disciplines Count: "+count);
        if (count == 3)
        {
            throw new RuntimeException("set back");
        }
    }
    
    
    /**
     * 
     */
    private void fixHibernateHiLo(final Connection connection)
    {
        Vector<Object> values = BasicSQLUtils.querySingleCol(connection, "SELECT next_hi FROM hibernate_unique_key");
        if (values.size() == 1)
        {
            int nextHi = (Integer)values.get(0);
            BasicSQLUtils.update("UPDATE hibernate_unique_key SET next_hi="+(nextHi+1));
            
        } else
        {
            throw new RuntimeException("The hibernate_unique_key must be created.");
        }
    }
    
    /**
     * @param newDBConn
     * @throws SQLException
     */
    private void updateVersionInfo(final Connection newDBConn) throws SQLException
    {
        String  appVerStr      = null;
        String  schemaVersion  = null;
        Integer spverId        = null;
        Integer recVerNum      = 1;
        
        try
        {
            System.setProperty(SchemaUpdateService.factoryName, "edu.ku.brc.specify.dbsupport.SpecifySchemaUpdateService");   // needed for updating the schema
            schemaVersion = SchemaUpdateService.getInstance().getDBSchemaVersionFromXML();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        Vector<Object[]> rows = BasicSQLUtils.query(newDBConn, "SELECT AppVersion, SchemaVersion, SpVersionID, Version FROM spversion");
        if (rows.size() == 1)
        {
            Object[] row  = (Object[])rows.get(0);
            appVerStr     = row[0].toString();
            schemaVersion = row[1].toString();
            spverId       = (Integer)row[2];
            recVerNum     = (Integer)row[3];
        }
        
        if (appVerStr != null)
        {
            appVerStr = UIHelper.getInstall4JInstallString();
            if (appVerStr == null)
            {
                do
                {
                    appVerStr = JOptionPane.showInputDialog("Enter Specify App version:"); 
                } while (StringUtils.isEmpty(appVerStr));
            }
            
            PreparedStatement pStmt = newDBConn.prepareStatement("UPDATE spversion SET AppVersion=?, SchemaVersion=?, Version=? WHERE SpVersionID = ?");
            pStmt.setString(1, appVerStr);
            pStmt.setString(2, SchemaUpdateService.getInstance().getDBSchemaVersionFromXML());
            pStmt.setInt(3, ++recVerNum);
            pStmt.setInt(4, spverId);
            if (pStmt.executeUpdate() != 1)
            {
                throw new RuntimeException("Problem updating SpVersion");
            }
            
        } else
        {
            appVerStr = UIHelper.getInstall4JInstallString();
            if (appVerStr == null)
            {
                do
                {
                    appVerStr = JOptionPane.showInputDialog("Enter Specify App version:"); 
                } while (StringUtils.isEmpty(appVerStr));
            }
            
            PreparedStatement pStmt = newDBConn.prepareStatement("INSERT INTO spversion (AppVersion, SchemaVersion, Version, TimestampCreated) VALUES(?,?,?,?)");
            pStmt.setString(1, appVerStr);
            pStmt.setString(2, schemaVersion);
            pStmt.setInt(3, 0);
            pStmt.setTimestamp(4, new Timestamp(Calendar.getInstance().getTime().getTime()));
            if (pStmt.executeUpdate() != 1)
            {
                throw new RuntimeException("Problem inserting SpVersion");
            }
        }
    }

    
    private boolean isUsingEmbeddedCEsInSp5()
    {
        String sql = String.format("SELECT ControlType FROM usysmetacontrollayout mcl INNER JOIN usysmetacontrol mc ON mc.ControlID = mcl.ControlID " +
        "WHERE mc.FieldSetSubTypeID = (SELECT FieldSetSubTypeID FROM usysmetafieldsetsubtype sst where sst.FieldSetID = 19 " +
        "AND sst.FieldValue = %d) AND mc.ObjectID = 10152 AND mcl.FullForm <> 0 ", conversion.getColObjTypeID());
        Integer controlType = BasicSQLUtils.getCount(conversion.getOldDBConn(), sql);
        
        return controlType != null && controlType != 5;
    }
    
    /**
     * 
     */
    protected void createTableSummaryPage()
    {
        TableWriter tblWriter = convLogger.getWriter("TableSummary.html", "Table Summary");
        tblWriter.startTable();
        tblWriter.println("<tr><th>Table</th><th>Count</th></tr>");
        int total = 0;
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            Integer count = BasicSQLUtils.getCount("select count(*) from "+ti.getName());
            if (count != null && count > 0)
            {
                tblWriter.log(ti.getName(), count.toString());
            }
            total += count;
        }
        tblWriter.println("<tr><td>Total Records</td><td>"+total+"</td></tr>");
        tblWriter.endTable();
       
        String sql;
        
        //----------------------------------------------------------------------------------
        tblWriter.println("<H3>Col Obj Counts By Discipline/Collection</H3>");
        sql = " SELECT cct.CollectionObjectTypeName, cs.SeriesName, Count(cc.CollectionObjectCatalogID) FROM catalogseries AS cs " +
        "Inner Join collectionobjectcatalog AS cc ON cs.CatalogSeriesID = cc.CatalogSeriesID " +
        "Inner Join collectionobjecttype AS cct ON cc.CollectionObjectTypeID = cct.CollectionObjectTypeID " +
        "Inner Join collectionobject AS co ON cc.CollectionObjectCatalogID = co.CollectionObjectID " +    " WHERE co.DerivedFromID IS NULL " +
        "GROUP BY cct.CollectionObjectTypeName, cs.SeriesName";
        
        showTable(tblWriter, "Specify 5", false, sql, "Discipline", "Collection", "Count");
        
        sql = "SELECT d.Name, c.CollectionName, Count(co.CollectionObjectID) AS `Count` FROM discipline AS d " +
        "Inner Join collection AS c ON d.UserGroupScopeId = c.DisciplineID " +
        "Inner Join collectionobject AS co ON co.CollectionID = c.UserGroupScopeId " +
        "GROUP BY d.Name, c.CollectionName";
        tblWriter.println("<BR>");
        showTable(tblWriter, "Specify 6", true, sql, "Discipline", "Collection", "Count");
        
        //----------------------------------------------------------------------------------
        /*tblWriter.println("<H3>Collecting Events Counts By Discipline</H3>");
        sql = " SELECT ct.CollectionObjectTypeName, Count(ce.CollectingEventID) FROM collectionobjecttype AS ct " +
        "Inner Join collectingevent AS ce ON ct.CollectionObjectTypeID = ce.BiologicalObjectTypeCollectedID " +
        "GROUP BY ct.CollectionObjectTypeName";
        showTable(tblWriter, "Specify 5", false, sql, "Discipline", "Count");
        
        sql = " SELECT d.Name, Count(ce.CollectingEventID) FROM discipline AS d " +
        "Inner Join collectingevent AS ce ON d.UserGroupScopeId = ce.DisciplineID " +
        "GROUP BY d.Name";
        tblWriter.println("<BR>");
        showTable(tblWriter, "Specify 6", true, sql, "Discipline", "Count");
        */
        //----------------------------------------------------------------------------------
        tblWriter.println("<H3>Col Obj Counts By Collection</H3>");
        sql = " SELECT cs.SeriesName, Count(cc.CollectionObjectCatalogID) FROM catalogseries AS cs " +
        "Inner Join collectionobjectcatalog AS cc ON cs.CatalogSeriesID = cc.CatalogSeriesID " +
        "Inner Join collectionobject AS co ON cc.CollectionObjectCatalogID = co.CollectionObjectID " +    " WHERE co.DerivedFromID IS NULL " +
        "GROUP BY cs.SeriesName";
        
        showTable(tblWriter, "Specify 5", false, sql,  "Collection", "Count");
        
        sql = " SELECT c.CollectionName, Count(co.CollectionObjectID) FROM collection AS c " +
        "Inner Join collectionobject AS co ON c.UserGroupScopeId = co.CollectionID " +
        "GROUP BY c.CollectionName";
        tblWriter.println("<BR>");
        showTable(tblWriter, "Specify 6", true, sql, "Collection", "Count");
        
        //----------------------------------------------------------------------------------
        tblWriter.println("<H3>Locality Counts</H3>");
        sql = " SELECT Count(LocalityID) FROM locality";
        showTable(tblWriter, "Specify 5", false, sql, "Count");
        
        sql = " SELECT Count(LocalityID) FROM locality";
        tblWriter.println("<BR>");
        showTable(tblWriter, "Specify 6", true, sql, "Count");

        //----------------------------------------------------------------------------------
        tblWriter.println("<H3>Locality Counts By Discipline</H3>");
        sql = " SELECT ct.CollectionObjectTypeName, Count(locid) FROM collectionobjecttype AS ct " +
        "Inner Join (SELECT ce.CollectingEventID as ceid, ce.BiologicalObjectTypeCollectedID as botid, locality.LocalityID as locid FROM collectingevent ce Inner Join locality ON ce.LocalityID = locality.LocalityID GROUP BY locality.LocalityID) T1 ON ct.CollectionObjectTypeID = T1.botid " +
        "GROUP BY ct.CollectionObjectTypeName";
        
        showTable(tblWriter, "Specify 5", false, sql, "Discipline", "Count");
        
        sql = " SELECT d.Name, Count(l.LocalityID) FROM discipline AS d " +
        "Inner Join locality AS l ON d.UserGroupScopeId = l.DisciplineID " +
        "GROUP BY d.Name";
        tblWriter.println("<BR>");
        showTable(tblWriter, "Specify 6", true, sql, "Discipline", "Count");
        
        //----------------------------------------------------------------------------------
        tblWriter.startTable();
        tblWriter.logHdr(CollectionInfoModel.getHeaders());
        
        DefaultTableModel model = CollectionInfo.getCollectionInfoTableModel(true);
        Object[] row = new Object[model.getColumnCount()];
        for (int r=0;r<model.getRowCount();r++)
        {
            for (int i=0;i<model.getColumnCount();i++)
            {
                row[i] = model.getValueAt(r, i);
            }
            tblWriter.logObjRow(row);
        }
        tblWriter.endTable();
        tblWriter.close();

        /*
        tblWriter.startTable();
        tblWriter.logHdr("&nbsp;", "Specify 5", "Specify 6");
        for (Triple<String, String, String> qry : getSummaryQueries())
        {
            
        }
        tblWriter.endTable();
        */
    }
    
    private void showTable(final TableWriter tblWriter, 
                           final String title,
                           final boolean isNewDB,
                           final String sql, 
                           String...cols)
    {
        tblWriter.println(title + "<BR>");
        tblWriter.startTable();
        tblWriter.logHdr(cols);
        for (Object[] row : BasicSQLUtils.query(isNewDB ? conversion.getNewDBConn() : conversion.getOldDBConn(), sql))
        {
            tblWriter.logObjRow(row);
        }
        tblWriter.endTable();
    }
    
    /**
     * @return
     */
    @SuppressWarnings("unused")
    private List<Triple<String, String, String>> getSummaryQueries()
    {
        String[] desc = {};
        String[] oldQ = {};
        String[] newQ = {};
        
        ArrayList<Triple<String, String, String>> list = new ArrayList<Triple<String,String, String>>(oldQ.length);
        for (int i=0;i<oldQ.length;i++)
        {
            list.add(new Triple<String, String, String>(desc[i], oldQ[i], newQ[i]));
        }
        return list;
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
    public boolean selectedDBsToConvert(final boolean useITOnly)
    {
        final JTextField     itUserNameTF = UIHelper.createTextField("root", 15);
        final JPasswordField itPasswordTF = UIHelper.createPasswordField("", 15);
        
        final JTextField     masterUserNameTF = UIHelper.createTextField("Master", 15);
        final JPasswordField masterPasswordTF = UIHelper.createPasswordField("Master", 15);
        
        final JTextField     hostNameTF = UIHelper.createTextField("localhost", 15);

        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,2px,p,2px,p,4px," + (useITOnly ? "" : "p,2px,p,2px,") + "p,8px,p,4px"));
        
        int y = 1;
        pb.addSeparator("IT User", cc.xyw(1, y, 4)); y += 2;
        pb.add(UIHelper.createLabel("Username:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(itUserNameTF, cc.xy(3, y)); y += 2;

        pb.add(UIHelper.createLabel("Password:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(itPasswordTF, cc.xy(3, y)); y += 2;

        if (!useITOnly)
        {
            pb.addSeparator("Master User", cc.xyw(1, y, 4)); y += 2;
            pb.add(UIHelper.createLabel("Username:", SwingConstants.RIGHT), cc.xy(1, y));
            pb.add(masterUserNameTF, cc.xy(3, y)); y += 2;
    
            pb.add(UIHelper.createLabel("Password:", SwingConstants.RIGHT), cc.xy(1, y));
            pb.add(masterPasswordTF, cc.xy(3, y)); y += 2;
        }

        pb.add(UIHelper.createLabel("Host Name:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(hostNameTF, cc.xy(3, y)); y += 2;
        
        if (System.getProperty("user.name").equals("rods"))
        {
            itPasswordTF.setText("root"); // password for converter database
        } else
        {
            itPasswordTF.requestFocus();
        }
        
        PanelBuilder panel = new PanelBuilder(new FormLayout("f:p:g,10px,f:p:g", "f:p:g"));
        panel.add(new JLabel(IconManager.getIcon("SpecifyConv")), cc.xy(1, 1));
        panel.add(pb.getPanel(), cc.xy(3, 1));

        CustomDialog dlg = new CustomDialog(null, "Specify Converter", true, panel.getPanel());
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
    
    
    
    /**
     * @return the itUsrPwd
     */
    public Pair<String, String> getItUsrPwd()
    {
        return itUsrPwd;
    }

    /**
     * @return the hostName
     */
    public String getHostName()
    {
        return hostName;
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
            return second + "   ("+ first + ")";
        }
        
    }
}
