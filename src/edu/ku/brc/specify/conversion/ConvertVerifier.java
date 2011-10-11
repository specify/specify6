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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.MySQLDMBSUserMgr;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.dbsupport.PostInsertEventListener;
import edu.ku.brc.specify.ui.AppBase;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.ToggleButtonChooserPanel.Type;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

public class ConvertVerifier extends AppBase
{
    private static final Logger log = Logger.getLogger(ConvertVerifier.class);
    
    private enum StatusType {NO_OLD_REC, NO_NEW_REC, OLD_VAL_NULL, NEW_VAL_NULL, BAD_COMPARE, BAD_DATE, COMPARE_OK, NO_COMPARE, }
    
    private Pair<String, String> itUsrPwd          = new Pair<String, String>(null, null);
    private String               hostName          = "localhost";
    private Pair<String, String> namePairToConvert = null;
    
    private boolean              dbgStatus         = false;
    private boolean              compareTo6DBs     = false;
    
    
    // These are the configuration Options for a View
    public static final long NO_OPTIONS             =     0; // Indicates there are no options
    public static final long DO_CO_PREPARATION      =     1; 
    public static final long DO_CO_CE               =     2; 
    public static final long DO_CO_LOCALITY         =     4; 
    public static final long DO_CO_PREPARER         =     8; 
    public static final long DO_CO_CATLOGER         =    16; 
    public static final long DO_CO_DETERMINER       =    32; 
    public static final long DO_CO_TAXON            =    64; 
    public static final long DO_CO_GEO              =   128; 
    public static final long DO_COLLECTORS          =   256; 
    public static final long DO_COLLEVENTS          =   512; 
    public static final long DO_TAXON_CIT           =  1024; 
    public static final long DO_SHIPMENTS           =  2048; 
    public static final long DO_OTHER_IDENT         =  4096; 
    public static final long DO_CO_COLLECTORS       =  8192; 
    public static final long DO_AGENTS              = 16384; 
    public static final long DO_LOANS               = 32768; 
    public static final long DO_CO_ALL              = 65535;
    
    private String[] labels = {"None", "Preparations", "CO Collecting Events", "Localities", "Preparers", 
                               "Catalogers", "Determiners", "Taxon", "Geographies", "Collectors", 
                               "Collecting Events", "Taxon Citations", "Shipments", "Other Ident", "ColObj Collectors", 
                               "Agents", "Loans", "All"};
    
    private ToggleButtonChooserPanel<String> chkPanel;
    
    //public static final long DONT_ADD_ALL_ALTVIEWS  = 256; 
    //public static final long USE_ONLY_CREATION_MODE = 512;
    
    public static final long DO_ACCESSIONS          =   1; 
    public static final long DO_AC_AUTHS            =   2; 
    public static final long DO_AC_AGENTS           =   4; 
    public static final long DO_AC_ALL              =   7; 
    
    private String[] accLabels = {"None", "Accessions", "Authorizations", "Agents", "All"};


    private static long                                   coOptions         = NO_OPTIONS;
    private static long                                   acOptions         = NO_OPTIONS;
    //private static List<String>                           dbNamesToConvert  = null;
    //private static int                                    currentIndex      = 0;
    //private static Hashtable<String, String>              old2NewDBNames    = null;
    
    private Hashtable<String, Integer>                    catNumsInErrHash  = new Hashtable<String, Integer>();
    private Hashtable<String, String>                     accNumsInErrHash  = new Hashtable<String, String>();
    
    
    //private String                                        oldDriver         = "";
    //private String                                        oldDBName         = "";
    //private String                                        oldUserName       = "rods";
    //private String                                        oldPassword       = "rods";

    private IdMapperMgr                                   idMapperMgr;

    private Connection                                    oldDBConn;
    private Connection                                    newDBConn;

    private Statement                                     oldDBStmt;
    private Statement                                     newDBStmt;

    private ResultSet                                     oldDBRS;
    private ResultSet                                     newDBRS;
    
    private String                                        newSQL;
    private String                                        oldSQL;
    
    private int                                           numErrors = 0;
    private static SimpleDateFormat                       dateFormatter          = new SimpleDateFormat("yyyy-MM-dd");
    private boolean                                       debug = false;
    private static ProgressFrame                          progressFrame;
    
    private ConversionLogger                              convLogger = new ConversionLogger();
    private TableWriter                                   tblWriter  = null;
    
    
    /**
     * 
     */
    public ConvertVerifier()
    {
        super();
        
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
        
        this.idMapperMgr = IdMapperMgr.getInstance();
    }

    /**
     * @param databaseNameSource
     * @param databaseNameDest
     * @throws Exception
     */
    public void verifyDB(final String databaseNameSource, 
                         final String databaseNameDest) throws Exception
    {
        String path = UIRegistry.getUserHomeDir() + File.separator + "verify";
        convLogger.initialize(path, databaseNameDest);
        
        String title = "From "+databaseNameSource+" to "+databaseNameDest;
        System.out.println("************************************************************");
        System.out.println(title);
        System.out.println("************************************************************");
        
        HibernateUtil.shutdown();    
        
        Properties initPrefs = BuildSampleDatabase.getInitializePrefs(databaseNameDest);
        
        String driverNameSource   = "";
        String databaseHostSource = "";
        DatabaseDriverInfo driverInfoSource = null;
        
        String driverNameDest   = "";
        String databaseHostDest = "";
        DatabaseDriverInfo driverInfoDest = null;
        
        log.debug("Running an non-custom MySQL convert, using old default login creds");
        driverNameSource    = initPrefs.getProperty("initializer.driver",   "MySQL");
        databaseHostSource  = initPrefs.getProperty("initializer.host",     "localhost"); 
        
        driverNameDest      = initPrefs.getProperty("initializer.driver",   "MySQL");
        databaseHostDest    = initPrefs.getProperty("initializer.host",     "localhost");  
    
        log.debug("Custom Convert Source Properties ----------------------");
        log.debug("databaseNameSource: " + databaseNameSource);        
        log.debug("driverNameSource: " + driverNameSource);
        log.debug("databaseHostSource: " + databaseHostSource);
        
        log.debug("Custom Convert Destination Properties ----------------------");
        log.debug("databaseNameDest: " + databaseNameDest);
        log.debug("driverNameDest: " + driverNameDest);
        log.debug("databaseHostDest: " + databaseHostDest);

        driverInfoSource = DatabaseDriverInfo.getDriver(driverNameSource);
        driverInfoDest = DatabaseDriverInfo.getDriver(driverNameDest);
        
        if (driverInfoSource == null)
        {
            throw new RuntimeException("Couldn't find Source DB driver by name ["+driverInfoSource+"] in driver list.");
        }
        if (driverInfoDest == null)
        {
            throw new RuntimeException("Couldn't find Destination driver by name ["+driverInfoDest+"] in driver list.");
        }
        
        if (driverNameDest.equals("MySQL"))BasicSQLUtils.myDestinationServerType = BasicSQLUtils.SERVERTYPE.MySQL;
        else if (driverNameDest.equals("SQLServer"))BasicSQLUtils.myDestinationServerType = BasicSQLUtils.SERVERTYPE.MS_SQLServer;
        
        if (driverNameSource.equals("MySQL"))BasicSQLUtils.mySourceServerType = BasicSQLUtils.SERVERTYPE.MySQL;
        else if (driverNameSource.equals("SQLServer"))BasicSQLUtils.mySourceServerType = BasicSQLUtils.SERVERTYPE.MS_SQLServer;
        
        else 
        {
            log.error("Error setting ServerType for destination database for conversion.  Could affect the"
                    + " way that SQL string are generated and executed on differetn DB egnines");
        }
        String destConnectionString = driverInfoDest.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, "", itUsrPwd.first, itUsrPwd.second, driverNameDest);
        log.debug("attempting login to destination: " + destConnectionString);
        // This will log us in and return true/false
        // This will connect without specifying a DB, which allows us to create the DB
        if (!UIHelper.tryLogin(driverInfoDest.getDriverClassName(), 
                driverInfoDest.getDialectClassName(), 
                databaseNameDest, 
                destConnectionString,
                itUsrPwd.first, 
                itUsrPwd.second))
        {
            log.error("Failed connection string: "  +driverInfoSource.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, databaseNameDest, itUsrPwd.first, itUsrPwd.second, driverNameDest) );
            throw new RuntimeException("Couldn't login into ["+databaseNameDest+"] "+DBConnection.getInstance().getErrorMsg());
        }
        
        convLogger.setIndexTitle(databaseNameDest + " Verify "+(new SimpleDateFormat("yyy-MM-dd hh:mm:ss")).format(Calendar.getInstance().getTime()));
        
        //MEG WHY IS THIS COMMENTED OUT???
        //DataBuilder.setSession(HibernateUtil.getNewSession());
        
        log.debug("DESTINATION driver class: " + driverInfoDest.getDriverClassName());
        log.debug("DESTINATION dialect class: " + driverInfoDest.getDialectClassName());               
        log.debug("DESTINATION Connection String: " + driverInfoDest.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, databaseNameDest, itUsrPwd.first, itUsrPwd.second, driverNameDest)); 
        
        // This will log us in and return true/false
        if (!UIHelper.tryLogin(driverInfoDest.getDriverClassName(), 
                driverInfoDest.getDialectClassName(), 
                databaseNameDest, 
                driverInfoDest.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, databaseNameDest, itUsrPwd.first, itUsrPwd.second, driverNameDest),                 
                itUsrPwd.first, 
                itUsrPwd.second))
        {
            throw new RuntimeException("Couldn't login into ["+databaseNameDest+"] "+DBConnection.getInstance().getErrorMsg());
        }
        
        String srcConStr = driverInfoSource.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostSource, databaseNameSource, itUsrPwd.first, itUsrPwd.second, driverNameSource);
        DBConnection oldDB = DBConnection.createInstance(driverInfoSource.getDriverClassName(), null, databaseNameSource, srcConStr, itUsrPwd.first, itUsrPwd.second);
        oldDBConn = oldDB.getConnection();
        if (oldDBConn == null)
        {
            throw new RuntimeException(oldDB.getErrorMsg());
        }
        newDBConn = DBConnection.getInstance().createConnection();
        newDBStmt = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        oldDBStmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        
        IdMapperMgr.getInstance().setDBs(oldDBConn, newDBConn);
        
        long startTime = System.currentTimeMillis();

        String[] tableNames = {"CollectingEvent", "CollectingEvent", "Locality", "Locality"};
        for (int i=0;i<tableNames.length;i+=2)
        {
            verifyTableCounts(tableNames[i].toLowerCase(), tableNames[i+1].toLowerCase());
        }
        
        progressFrame = new ProgressFrame("Checking Catalog Objects....");
        progressFrame.adjustProgressFrame();
        
        String cntSQL = compareTo6DBs ? "SELECT COUNT(*) FROM collectionobject" :
                                        "SELECT COUNT(*) FROM collectionobjectcatalog WHERE CollectionObjectTypeID > 8 && CollectionObjectTypeID < 20";
        Integer numColObjs = BasicSQLUtils.getCount(oldDBConn, cntSQL);
        
        progressFrame.setProcess(0, numColObjs);
        //progressFrame.setDesc("Checking Catalog Objects....");
        
        progressFrame.setOverall(0, numColObjs*4);
        progressFrame.setOverall(0);
        progressFrame.setDesc("");

        UIHelper.centerAndShow(progressFrame);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                UIHelper.centerAndShow(progressFrame);
            }
        });
        
        HashMap<Long, TableWriter> tblWriterHash = new HashMap<Long, TableWriter>();
        for (int i=1;i<labels.length-1;i++)
        {
            long id = (long)Math.pow(2, i-1);
            id = Math.max(id, 1);
            tblWriter = convLogger.getWriter(labels[i] + ".html", labels[i]);
            //printVerifyHeader(labels[i]);
            tblWriter.startTable();
            tblWriter.logHdr("ID", "Desc");
            tblWriterHash.put(id, tblWriter);
            System.out.println(id + " - " + labels[i]);
        }
        
        boolean nullCEOk = false;
        File ceFile = new File(databaseNameDest+".ce_all");
        if (ceFile.exists())
        {
            nullCEOk = true;
            //ceFile.delete();
        }
        
        nullCEOk = true;
        
        // For Debug
        coOptions = DO_CO_ALL;
        
        //if (coOptions > NO_OPTIONS)
        {
            int i = 0;
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String    sql  = compareTo6DBs ? "SELECT CatalogNumber FROM collectionobject ORDER BY CatalogNumber ASC" :
                                             "SELECT CatalogNumber FROM collectionobjectcatalog WHERE CollectionObjectTypeID > 8 && CollectionObjectTypeID < 20 AND SubNumber >= 0 ORDER BY CatalogNumber ASC";
            ResultSet rs   = stmt.executeQuery(sql);
            while (rs.next())
            {
                int    oldCatNum = rs.getInt(1);
                String newCatNum = convertCatNum(oldCatNum);
                
                //if (oldCatNum < 1643) continue;
                
                /*if (isCOOn(DO_CO_DETERMINER))
                {
                    tblWriter = tblWriterHash.get(DO_CO_DETERMINER);
                    if (!verifyDeterminer(oldCatNum, newCatNum))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                } 
                
                if (isCOOn(DO_CO_CATLOGER))
                {
                    tblWriter = tblWriterHash.get(DO_CO_CATLOGER);
                    if (!verifyCataloger(oldCatNum, newCatNum))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                }
                
                if (isCOOn(DO_CO_COLLECTORS))
                {
                    tblWriter = tblWriterHash.get(DO_CO_CATLOGER);
                    if (!verifyCollector(oldCatNum, newCatNum))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                }
                
                if (isCOOn(DO_CO_GEO))
                {
                    tblWriter = tblWriterHash.get(DO_CO_GEO);
                    if (!verifyGeography(oldCatNum, newCatNum))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                }
                
                if (isCOOn(DO_CO_CE))
                {
                    tblWriter = tblWriterHash.get(DO_CO_CE);
                    if (!verifyCollectingEvent(oldCatNum, newCatNum, nullCEOk))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                }
                
                if (isCOOn(DO_CO_TAXON))
                {
                    tblWriter = tblWriterHash.get(DO_CO_TAXON);
                    if (!verifyTaxon(oldCatNum, newCatNum))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                }
                
                if (isCOOn(DO_CO_LOCALITY))
                {
                    tblWriter = tblWriterHash.get(DO_CO_LOCALITY);
                    if (!verifyCOToLocality(oldCatNum, newCatNum))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                }
                
                if (isCOOn(DO_CO_PREPARATION))
                {
                    tblWriter = tblWriterHash.get(DO_CO_PREPARATION);
                    if (!verifyPreparation(oldCatNum, newCatNum))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                }
                
                if (isCOOn(DO_CO_PREPARER))
                {
                    tblWriter = tblWriterHash.get(DO_CO_PREPARER);
                    if (!verifyPreparer(oldCatNum, newCatNum))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                }
                
                if (isCOOn(DO_TAXON_CIT))
                {
                    tblWriter = tblWriterHash.get(DO_TAXON_CIT);
                    if (!verifyTaxonCitations(oldCatNum, newCatNum))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                }
                
                if (isCOOn(DO_OTHER_IDENT))
                {
                    tblWriter = tblWriterHash.get(DO_OTHER_IDENT);
                    if (!verifyOtherIdentifier(oldCatNum, newCatNum))
                    {
                        catNumsInErrHash.put(newCatNum, oldCatNum);
                    }
                } */
                
                if ((i % 100) == 0)
                {
                    System.out.println(i+"  "+oldCatNum);
                    progressFrame.setProcess(i);
                    progressFrame.setOverall(i);
                }
                
                if ((i % 1000) == 0)
                {
                    for (TableWriter tw : tblWriterHash.values())
                    {
                        tw.flush();
                    }
                }
                i++;
            }
            
            rs.close();
            stmt.close();
        }
        
        progressFrame.setProcess(numColObjs);
        
        if (isCOOn(DO_COLLECTORS))
        {
            tblWriter = tblWriterHash.get(DO_COLLECTORS);
            //verifyCollectors();
        }
        
        if (isCOOn(DO_AGENTS))
        {
            tblWriter = tblWriterHash.get(DO_AGENTS);
            verifyAgents();
        }
        
        progressFrame.setOverall(numColObjs*2);
        if (isCOOn(DO_COLLEVENTS))
        {
            tblWriter = tblWriterHash.get(DO_COLLEVENTS);
            verifyCEs();
        }
        
        //progressFrame.setOverall(numColObjs*2);
        if (isCOOn(DO_COLLEVENTS))
        {
            tblWriter = tblWriterHash.get(DO_COLLEVENTS);
            verifyShipments();
        }
        
        if (isCOOn(DO_LOANS))
        {
            tblWriter = tblWriterHash.get(DO_LOANS);
            verifyLoans();
            verifyGifts();
            verifyLoanRetPreps();
        }
        
        for (TableWriter tw : tblWriterHash.values())
        {
            tw.endTable();
        }
        
        progressFrame.setOverall(numColObjs*3);
        
        
        tblWriter = convLogger.getWriter("CatalogNumberSummary.html", "Catalog Nummber Summary");
        tblWriter.startTable();
        tblWriter.logHdr("Number", "Description");
        tblWriter.logErrors(Integer.toString(numErrors), "All Errors");
        tblWriter.logErrors(Integer.toString(catNumsInErrHash.size()), "Catalog Number with Errors");
        tblWriter.endTable();
        
        tblWriter.println("<BR>");
        tblWriter.println("Catalog Summary:<BR>");
        Vector<String> catNumList = new Vector<String>(catNumsInErrHash.keySet());
        Collections.sort(catNumList);
        for (String catNum : catNumList)
        {
            tblWriter.println(catNum+"<BR>");
        }
        tblWriter.println("<BR>");
        numErrors = 0;

        //-----------------------------------------------------------------------------------------------------------
        // Accessions
        //-----------------------------------------------------------------------------------------------------------
        // For Debug
        acOptions = DO_AC_ALL;
        
        HashMap<Long, TableWriter> accTblWriterHash = new HashMap<Long, TableWriter>();
        for (int i=1;i<accLabels.length;i++)
        {
            long id = (long)Math.pow(2, i-1);
            id = Math.max(id, 1);
            tblWriter = convLogger.getWriter("accession_"+accLabels[i] + ".html", "Accession "+accLabels[i]);
            tblWriter.startTable();
            tblWriter.logHdr("ID", "Desc");
            accTblWriterHash.put(id, tblWriter);
        }
        
        if (acOptions > NO_OPTIONS)
        {
            int i = 0;
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT Number FROM accession ORDER BY Number ASC");
            while (rs.next())
            {
                String oldAccNum = rs.getString(1);
                String newAccNum = oldAccNum;
                
                if (isACOn(DO_ACCESSIONS))
                {
                    tblWriter = accTblWriterHash.get(DO_ACCESSIONS);
                    if (!verifyAccessions(oldAccNum, newAccNum))
                    {
                        log.error("Accession Num: "+oldAccNum);
                        accNumsInErrHash.put(newAccNum, oldAccNum);
                    }
                    //log.error("New SQL: "+newSQL);
                    //log.error("Old SQL: "+oldSQL);
                    //break;
                }            
                
                if (isACOn(DO_AC_AGENTS))
                {
                    tblWriter = accTblWriterHash.get(DO_AC_AGENTS);
                    if (!verifyAccessionAgents(oldAccNum, newAccNum))
                    {
                        log.error("Accession Num: "+oldAccNum);
                        accNumsInErrHash.put(newAccNum, oldAccNum);
                    }
                    //log.error("New SQL: "+newSQL);
                    //log.error("Old SQL: "+oldSQL);
                    //break;
                }            
                
                if ((i % 100) == 0)
                {
                    System.out.println(i+"  "+oldAccNum);
                }
                i++;
            }
            
            rs.close();
            stmt.close();
        }
        progressFrame.setOverall(numColObjs*4);
        newDBConn.close();
        oldDBConn.close();
        
        for (TableWriter tw : accTblWriterHash.values())
        {
            tw.endTable();
        }
        
        printAccessionTotal("Accession");
        
        File indexFile = convLogger.closeAll();
        
        long endTime = System.currentTimeMillis();
        
        int convertTimeInSeconds = (int)((endTime - startTime) / 1000.0);
        
        //ConvertStatSender sender = new ConvertStatSender("verify.php");
        //sender.senConvertInfo(databaseNameDest, numColObjs, convertTimeInSeconds);
        
        log.info("Done.");
        
        progressFrame.setVisible(false);
        
        AttachmentUtils.openURI(indexFile.toURI());
        
        System.exit(0);
    }
    
    /**
     * @param compare26DBs the compare26DBs to set
     */
    public void setCompareTo6DBs(final boolean compareTo6DBs)
    {
        this.compareTo6DBs = compareTo6DBs;
    }

    /**
     * @param prefix
     * @throws FileNotFoundException
     */
    private void printAccessionTotal(final String prefix) throws FileNotFoundException
    {
        tblWriter = convLogger.getWriter(prefix+"Summary.html", prefix + " Summary");
        tblWriter.startTable();
        tblWriter.logHdr("Number", "Description");
        tblWriter.logErrors(Integer.toString(numErrors), "All Errors");
        tblWriter.logErrors(Integer.toString(accNumsInErrHash.size()), "Accession Numbers with Errors");
        tblWriter.endTable();
    }
    
    /**
     * @param oldCatNum
     * @param newCatNum
     * @throws SQLException
     */
    private boolean verifyTaxon(final int oldCatNum, final String newCatNum) throws SQLException
    {
        newSQL = "SELECT co.CollectionObjectID, co.CatalogedDate, co.CatalogedDatePrecision, determination.DeterminedDate, determination.DeterminedDatePrecision, tx.FullName " + 
                 "FROM determination LEFT JOIN collectionobject co ON determination.CollectionObjectID = co.CollectionObjectID "+
                 "LEFT JOIN taxon tx ON determination.TaxonID = tx.TaxonID WHERE CatalogNumber = '"+ newCatNum + "'";

        oldSQL = "SELECT cc.CollectionObjectCatalogID, cc.CatalogedDate, determination.Date,taxonname.FullTaxonName " + 
                 "FROM determination LEFT JOIN taxonname ON determination.TaxonNameID = taxonname.TaxonNameID " + 
                 "LEFT JOIN collectionobjectcatalog cc ON cc.CollectionObjectCatalogID = determination.BiologicalObjectID " + 
                 "WHERE cc.SubNumber > -1 AND CatalogNumber = " + oldCatNum;
        if (debug)
        {
	         log.debug("New SQL: "+newSQL);
	         log.debug("Old SQL: "+oldSQL);
        }
        
        StatusType status = compareRecords("Taxon", oldCatNum, newCatNum, oldSQL, newSQL);
        dumpStatus(status);
        return status == StatusType.COMPARE_OK;
    }
    
    
    /**
     * @param oldCatNum
     * @param newCatNum
     * @throws SQLException
     */
    private boolean verifyOtherIdentifier(final int oldCatNum, final String newCatNum) throws SQLException
    {
        newSQL = "SELECT collectionobject.CatalogNumber, otheridentifier.Identifier FROM collectionobject " +
                 "Inner Join otheridentifier ON collectionobject.CollectionObjectID = otheridentifier.CollectionObjectID " +
                 "WHERE CatalogNumber = '"+ newCatNum + "'" +
                 "ORDER BY collectionobject.CatalogNumber ASC, otheridentifier.Identifier ASC";

        oldSQL = "SELECT cc.CatalogNumber, i.Identifier FROM collectionobject AS c " +
                    "Inner Join collectionobjectcatalog AS cc ON c.CollectionObjectID = cc.CollectionObjectCatalogID " +
                    "Inner Join otheridentifier AS i ON cc.CollectionObjectCatalogID = i.CollectionObjectID " +    
                    "WHERE c.CollectionObjectTypeID <  21 AND cc.SubNumber > -1 AND cc.CatalogNumber = " + oldCatNum + " " + 
                    "ORDER BY " +    " cc.CatalogNumber ASC, i.Identifier ASC";
        if (debug)
        {
             log.debug("New SQL: "+newSQL);
             log.debug("Old SQL: "+oldSQL);
        }
        
        StatusType status = compareRecords("OtherIdentifier", oldCatNum, newCatNum, oldSQL, newSQL);
        dumpStatus(status);
        return status == StatusType.COMPARE_OK;
    }

    /**
     * @param status
     */
    private void dumpStatus(final StatusType status)
    {
        if (dbgStatus)
        {
            //log.debug(oldSQL);
            //log.debug(newSQL);
            //System.err.println(oldSQL);
            //System.err.println(newSQL);
            
            switch (status)
            {
                case OLD_VAL_NULL:
                    tblWriter.print("<tr><td colspan=\"2\">");
                    tblWriter.println(dumpSQL(newDBConn, newSQL));
                    tblWriter.println(dumpSQL(oldDBConn, oldSQL));
                    tblWriter.println("<BR>");
                    tblWriter.println("</td></tr><!-- 4 -->");
                    tblWriter.flush();
                    break;
                    
                case NO_OLD_REC:
                    tblWriter.print("<tr><td colspan=\"2\">");
                    tblWriter.println(dumpSQL(newDBConn, newSQL));
                    tblWriter.println("<BR>");
                    tblWriter.println("</td></tr><!-- 4 -->");
                    tblWriter.flush();
                    break;
                    
                case NEW_VAL_NULL:
                    tblWriter.print("<tr><td colspan=\"2\">");
                    tblWriter.println(dumpSQL(newDBConn, newSQL));
                    tblWriter.println(dumpSQL(oldDBConn, oldSQL));
                    tblWriter.println("<BR>");
                    tblWriter.println("</td></tr><!-- 4 -->");
                    tblWriter.flush();
                    break;
                    
                case NO_NEW_REC:
                    tblWriter.print("<tr><td colspan=\"2\">");
                    tblWriter.println(dumpSQL(oldDBConn, oldSQL));
                    tblWriter.println("<BR>");
                    tblWriter.println("</td></tr><!-- 4 -->");
                    tblWriter.flush();
                    break;
            }
        }
    }
                         
    /**
     * @param oldCatNum
     * @param newCatNum
     * @return
     * @throws SQLException
     */
    private boolean verifyGeography(final int oldCatNum, final String newCatNum) throws SQLException
    {
        String[] lbls = new String[] {"ContinentOrOcean", "Country", "State", "County"};
        
        newSQL = "SELECT g.GeographyID, g.Name " +
                    "FROM collectionobject co INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                    "INNER JOIN locality l ON ce.LocalityID = l.LocalityID " +
                    "INNER JOIN geography g ON l.GeographyID = g.GeographyID " +
                    "WHERE CatalogNumber = '"+ newCatNum + "'";

        oldSQL = "SELECT g.GeographyID, g.ContinentOrOcean, g.Country, g.State, g.County " +
                    "FROM collectionobjectcatalog cc INNER JOIN collectionobject co ON cc.CollectionObjectCatalogID = co.CollectionObjectID " +
                    "INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                    "INNER JOIN locality l ON ce.LocalityID = l.LocalityID " +
                    "INNER JOIN geography g ON l.GeographyID = g.GeographyID " +
                    "WHERE cc.SubNumber > -1 AND CatalogNumber = " + oldCatNum;
        
        if (debug)
        {
	         log.debug("New SQL: "+newSQL);
	         log.debug("Old SQL: "+oldSQL);
        }
        
        try
        {
            getResultSets(oldSQL, newSQL);
            
            boolean hasOldRec = oldDBRS.next();
            boolean hasNewRec = newDBRS.next();
            
            if (!hasOldRec && !hasNewRec)
            {
                return true;
            }
            
            if (!hasOldRec)
            {
                log.error("Geography - No Old Record for ["+oldCatNum+"]");
                return false;
            }
            if (!hasNewRec)
            {
                log.error("Geography - No New Record for ["+newCatNum+"]");
                return false;
            }
            
            String newGeoName = newDBRS.getString(2);
            String[] names = new String[4];
            for (int i=0;i<names.length;i++)
            {
                names[i] = oldDBRS.getString(i+2);
            }
            
            for (int i=names.length-1;i>=0;i--)
            {
                if (newGeoName.equalsIgnoreCase(names[i]))
                {
                    return true;
                }
            }
            
            if (!newGeoName.equals("Undefined"))
            {
                StringBuilder sb = new StringBuilder("Couldn't find New Geo Name[");
                sb.append(newGeoName);
                sb.append("] Old Id[");
                sb.append(oldDBRS.getInt(1));
                sb.append("] New Id[");
                sb.append(newDBRS.getInt(1));
                sb.append("]");
                for (int i=names.length-1;i>=0;i--)
                {
                    sb.append(" ");
                    sb.append(lbls[i]);
                    sb.append("[");
                    sb.append(names[i]);
                    sb.append("]");
                }
                String oldNewIdStr = oldCatNum + " / "  + newCatNum+" ";
                log.error(oldNewIdStr + " - " + sb.toString());
                tblWriter.logErrors(oldNewIdStr, sb.toString());
                
                return false;
            }
            return true;
            
        } finally
        {
            doneWithRS();
        }
    }
    
    /**
     * @param conn
     * @param sql
     * @return
     */
    public static String dumpSQL(final Connection conn, final String sql)
    {
        StringBuilder sb = new StringBuilder("<table class=\"i\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n");
        Vector<Object[]> list = BasicSQLUtils.query(conn, sql, true);
        for (Object[] row : list)
        {
            sb.append("<tr>");
            for (Object obj : row)
            {
                sb.append("<td>");
                sb.append(obj);
                sb.append("</td>");
            }
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
        return sb.toString();
    }
    
    /**
     * @param newTableName
     * @param oldTableName
     * @return
     */
    private boolean verifyTableCounts(final String newTableName, final String oldTableName)
    {
        int newCnt = BasicSQLUtils.getNumRecords(newDBConn, newTableName);
        int oldCnt = BasicSQLUtils.getNumRecords(oldDBConn, oldTableName);
        
        if (newCnt != oldCnt)
        {
            log.error("Table Record Counts don't match New["+newTableName+"  "+newCnt+"]["+oldTableName+"  "+oldCnt+"]");
            return false;
        }
        return true;
    }

    private boolean verifyCOToLocality(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT l.LocalityID, l.LocalityName " +
                        "FROM collectionobject co INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                        "INNER JOIN locality l ON ce.LocalityID = l.LocalityID " +
                        "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT l.LocalityID, l.LocalityName  " +
                        "FROM collectionobjectcatalog cc INNER JOIN collectionobject co ON cc.CollectionObjectCatalogID = co.CollectionObjectID " +
                        "INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                        "INNER JOIN locality l ON ce.LocalityID = l.LocalityID " +
                        "WHERE cc.SubNumber > -1 AND CatalogNumber = " + oldCatNum;
         if (debug)
         {
	         log.debug("New SQL: "+newSQL);
	         log.debug("Old SQL: "+oldSQL);
         }
         
        StatusType status = compareRecords("Locality", oldCatNum, newCatNum, oldSQL, newSQL);
        dumpStatus(status);
        return status == StatusType.COMPARE_OK;

    }
      
    /**
     * @param oldCatNum
     * @param newCatNum
     * @return
     * @throws SQLException
     */
    private boolean verifyCataloger(final int oldCatNum, final String newCatNum) throws SQLException
    {
        //log.debug("New SQL: "+newSQL);
        //log.debug("Old SQL: "+oldSQL);
        
        // address.Address, 
         newSQL = "SELECT a.AgentID, a.FirstName, a.MiddleInitial, a.LastName " +
                  "FROM collectionobject co INNER JOIN agent a ON co.CatalogerID = a.AgentID " +
                  "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT a.AgentID, a.FirstName, a.MiddleInitial, a.LastName, a.Name  " +
                  "FROM collectionobjectcatalog cc INNER JOIN agent a ON cc.CatalogerID = a.AgentID WHERE cc.SubNumber = 0 AND CatalogNumber = " + oldCatNum;
         if (debug)
         {
	         log.debug("New SQL: "+newSQL);
	         log.debug("Old SQL: "+oldSQL);
         }
         StatusType status = compareRecords("Cataloger", oldCatNum, newCatNum, oldSQL, newSQL);
         dumpStatus(status);
         return status == StatusType.COMPARE_OK;
    }
    
    
    /**
     * @param oldCatNum
     * @param newCatNum
     * @return
     * @throws SQLException
     */
    private boolean verifyCollector(final int oldCatNum, final String newCatNum) throws SQLException
    {
        //log.debug("New SQL: "+newSQL);
        //log.debug("Old SQL: "+oldSQL);
        
         newSQL = "SELECT a.AgentID, a.FirstName, a.MiddleInitial, a.LastName " +
         		"FROM collectionobject AS co " +
         		"INNER Join collectingevent AS ce ON co.CollectingEventID = ce.CollectingEventID " +
         		"INNER Join collector AS c ON ce.CollectingEventID = c.CollectingEventID " +
         		"INNER Join agent AS a ON c.AgentID = a.AgentID WHERE co.CatalogNumber =  '"+ newCatNum + "' ORDER BY OrderNumber, c.TimestampCreated, a.LastName";

         oldSQL = "SELECT a.AgentID, a.FirstName, a.MiddleInitial, a.LastName, a.Name " +
         		"FROM collectionobjectcatalog AS cc " +
         		"INNER Join collectionobject AS co ON cc.CollectionObjectCatalogID = co.CollectionObjectID " +
         		"INNER Join collectingevent AS ce ON co.CollectingEventID = ce.CollectingEventID " +
         		"INNER Join collectors AS c ON ce.CollectingEventID = c.CollectingEventID " +
         		"INNER Join agent AS a ON c.AgentID = a.AgentID WHERE cc.CatalogNumber = " + oldCatNum+ " ORDER BY `Order`, c.TimestampCreated, a.LastName, a.Name";
         if (debug)
         {
             log.debug("New SQL: "+newSQL);
             log.debug("Old SQL: "+oldSQL);
         }
         StatusType status = compareRecords("Collector", oldCatNum, newCatNum, oldSQL, newSQL);
         dumpStatus(status);
         return status == StatusType.COMPARE_OK;
    }

    
    /**
     * @param oldCatNum
     * @param newCatNum
     * @return
     * @throws SQLException
     */
    private boolean verifyDeterminer(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT a.AgentID, a.FirstName, a.MiddleInitial, a.LastName " +
                  "FROM collectionobject co INNER JOIN determination ON co.CollectionObjectID = determination.CollectionObjectID " +
                  "INNER JOIN agent a ON determination.DeterminerID = a.AgentID " +
                  "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT a.AgentID, a.FirstName, a.MiddleInitial, a.LastName, a.Name  " +
                  "FROM collectionobjectcatalog cc INNER JOIN collectionobject co ON cc.CollectionObjectCatalogID = co.CollectionObjectID " +
                  "INNER JOIN determination ON determination.BiologicalObjectID = co.CollectionObjectID " + 
                  "INNER JOIN agent a ON determination.DeterminerID = a.AgentID WHERE cc.SubNumber > -1 AND CatalogNumber = " + oldCatNum;
        
         if (debug)
         {
	         log.debug("New SQL: "+newSQL);
	         log.debug("Old SQL: "+oldSQL);
         }
         
         StatusType status = compareRecords("Determiner", oldCatNum, newCatNum, oldSQL, newSQL);
         dumpStatus(status);
         return status == StatusType.COMPARE_OK;
    }

    /**
     * @param oldCatNum
     * @param newCatNum
     * @return
     * @throws SQLException
     */
    private boolean verifyPreparer(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT a.AgentID, a.FirstName, a.MiddleInitial, a.LastName " +
                  "FROM collectionobject co INNER JOIN preparation p ON co.CollectionObjectID = p.CollectionObjectID INNER JOIN agent a ON p.PreparedByID = a.AgentID " +
                  "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT a.AgentID, a.FirstName, a.MiddleInitial, a.LastName, a.Name  " +
                  "FROM collectionobjectcatalog cc INNER JOIN collectionobject co ON cc.CollectionObjectCatalogID = co.DerivedFromID " +
                  "INNER JOIN preparation p ON co.CollectionObjectID = p.PhysicalObjectTypeID " +
                  "INNER JOIN agent a ON p.PreparedByID = a.AgentID " +
                  "WHERE cc.SubNumber > -1 AND CatalogNumber = " + oldCatNum;
        
         if (debug)
         {
	         log.debug("New SQL: "+newSQL);
	         log.debug("Old SQL: "+oldSQL);
         }
         StatusType status = compareRecords("Preparer", oldCatNum, newCatNum, oldSQL, newSQL);
         dumpStatus(status);
         return status == StatusType.COMPARE_OK;
    }

    /**
     * @return
     * @throws SQLException
     */
    private boolean verifyTaxonCitations(final int oldCatNum, final String newCatNum) throws SQLException
    {
        newSQL = "SELECT t.TaxonID, t.Name, tc.Text1, tc.Text2, tc.Number1, tc.Number2, tc.YesNo1, tc.YesNo2, rw.ReferenceWorkType, rw.Title, rw.Publisher, rw.PlaceOfPublication, rw.Volume, rw.Pages, rw.LibraryNumber " +
                 "FROM collectionobject co INNER JOIN determination d ON co.CollectionObjectID = d.CollectionObjectID " +
                 "INNER JOIN taxon t ON d.TaxonID = t.TaxonID " +
                 "INNER JOIN taxoncitation tc ON t.TaxonID = tc.TaxonID " +
                 "INNER JOIN referencework rw ON tc.ReferenceWorkID = rw.ReferenceWorkID " +
                 "WHERE CatalogNumber = '"+ newCatNum + "'";
        
        oldSQL = "SELECT t.TaxonNameID, t.TaxonName, tc.Text1, tc.Text2, tc.Number1, tc.Number2, tc.YesNo1, tc.YesNo2, rw.ReferenceWorkType, rw.Title, rw.Publisher, rw.PlaceOfPublication, rw.Volume, rw.Pages, rw.LibraryNumber " +
                    "FROM collectionobjectcatalog cc INNER JOIN determination d ON cc.CollectionObjectCatalogID = d.BiologicalObjectID " +
                    "INNER JOIN taxonname t ON d.TaxonNameID = t.TaxonNameID " +
                    "INNER JOIN taxoncitation tc ON t.TaxonNameID = tc.TaxonNameID " +
                    "INNER JOIN referencework rw ON tc.ReferenceWorkID = rw.ReferenceWorkID " +
                    "WHERE cc.SubNumber > -1 AND CatalogNumber = " + oldCatNum;

        if (debug)
        {
            log.debug("New SQL: "+newSQL);
            log.debug("Old SQL: "+oldSQL);
        }
        StatusType status = compareRecords("TaxonCitation", oldCatNum, newCatNum, oldSQL, newSQL);
        dumpStatus(status);
        return status == StatusType.COMPARE_OK;
    }

    /**
     * @return
     * @throws SQLException
     */
    private boolean verifyAllLocalityToGeo() throws SQLException
    {
        newSQL = "SELECT l.LocalityID, g.GeographyID, g.Name " +
                 "FROM locality l " +
                 "INNER JOIN geography g ON l.GeographyID = g.GeographyID ";

        oldSQL = "SELECT l.LocalityID, g.GeographyID, g.GeographyID, g.ContinentOrOcean, g.Country, g.State, g.County " +
                 "FROM locality l " +
                 "INNER JOIN geography g ON l.GeographyID = g.GeographyID ";
    
        //System.out.println(newSQL);
        //System.out.println(oldSQL);

        try
        {
            getResultSets(oldSQL, newSQL);

            boolean hasOldRec = oldDBRS.next();
            boolean hasNewRec = newDBRS.next();

            String[] names    = new String[4];
            int      startInx = 3;

            while (hasOldRec && hasNewRec)
            {
                String newGeoName = newDBRS.getString(startInx);
                for (int i=0;i<names.length;i++)
                {
                    names[i] = oldDBRS.getString(i+startInx);
                }

                boolean fnd = true;
                for (int i=names.length-1;i>=0;i--)
                {
                    if (names[i] != null && !newGeoName.equalsIgnoreCase(names[i]))
                    {
                        fnd = false;
                    }
                }

                if (!fnd)
                {
                    log.error("Couldn't find new Geo Name["+newGeoName+"]  Loc NewId["+newDBRS.getObject(1)+"]  Loc Old Id["+oldDBRS.getObject(1)+"]");
                    log.error("  Geo NewId["+newDBRS.getObject(2)+"]  Geo Old Id["+oldDBRS.getObject(2)+"]");
                    for (int i=names.length-1;i>=0;i--)
                    {
                        log.error("  ["+names[i]+"]");
                    }
                }

                hasOldRec = oldDBRS.next();
                hasNewRec = newDBRS.next();

                if (!hasOldRec && !hasNewRec)
                {
                    return true;
                }

                if (!hasOldRec)
                {
                    log.error("Geography - No Old Record for ["+newDBRS.getObject(1)+"]");
                    return false;
                }
                if (!hasNewRec)
                {
                    log.error("Geography - No New Record for ["+oldDBRS.getObject(1)+"]");
                    return false;
                }
            }
            return false;

        } finally
        {
            doneWithRS();
        }
    }
                         
    /**
     * @param oldCatNum
     * @param newCatNum
     * @return
     * @throws SQLException
     */
    private boolean verifyCollectingEvent(final int oldCatNum, final String newCatNum, final boolean nullsAreOK) throws SQLException
    {
         newSQL = "SELECT ce.CollectingEventID, ce.StartDate, ce.StartDatePrecision, ce.StationFieldNumber " +
                        "FROM collectionobject co INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                        "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT ce.CollectingEventID, ce.StartDate, ce.StationFieldNumber  " +
                        "FROM collectionobjectcatalog cc INNER JOIN collectionobject co ON cc.CollectionObjectCatalogID = co.CollectionObjectID " +
                        "INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                        "WHERE cc.SubNumber > -1 AND CatalogNumber = " + oldCatNum;
        
         StatusType status = compareRecords("CE To  Locality", oldCatNum, newCatNum, oldSQL, newSQL);
         dumpStatus(status);
         return status == StatusType.COMPARE_OK;
    }
                         
    /**
     * @param oldCatNum
     * @param newCatNum
     * @return
     * @throws SQLException
     */
    private boolean verifyPreparation(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT co.CollectionObjectID, p.CountAmt, preptype.Name, p.Text1, p.Text2 " +
                  "FROM collectionobject co INNER JOIN preparation p ON co.CollectionObjectID = p.CollectionObjectID " +
                  "INNER JOIN preptype ON p.PrepTypeID = preptype.PrepTypeID " +
                  "WHERE CatalogNumber = '"+ newCatNum + "' ORDER BY preptype.Name, p.TimestampCreated";

         
         oldSQL = "SELECT cc.CollectionObjectCatalogID, co.Count, co.PreparationMethod, co.Text1, co.Text2 FROM collectionobject co " +
                  "INNER JOIN collectionobjectcatalog cc ON co.DerivedFromID = cc.CollectionObjectCatalogID " + 
                  "WHERE cc.SubNumber > -1 AND co.CollectionObjectTypeID > 20 AND CatalogNumber = " + oldCatNum + "  ORDER BY co.PreparationMethod, co.TimestampCreated";
         
         
         /*oldSQL = "SELECT co.Count, co.PreparationMethod, co.Text1, co.Text2 FROM collectionobject co WHERE CollectionObjectID IN " +
                  "(SELECT CollectionObjectCatalogID AS COCID FROM collectionobjectcatalog WHERE CollectionObjectTypeID > 20 AND CatalogNumber = " + oldCatNum + ")";
                  */
        
         StatusType status = compareRecords("Preparation", oldCatNum, newCatNum, oldSQL, newSQL);
         dumpStatus(status);
         return status == StatusType.COMPARE_OK;
    }
                         
    /**
     * @param oldAccNum
     * @param newAccNum
     * @return
     * @throws SQLException
     */
    private boolean verifyAccessions(final String oldAccNum, final String newAccNum) throws SQLException
    {
         newSQL = "SELECT AccessionID, AccessionNumber, Status, Type, VerbatimDate, DateAccessioned, DateReceived, Number1, Number2, YesNo1, YesNo2 FROM accession  " +
                  "WHERE AccessionNumber = \"" + newAccNum + "\"";

         oldSQL = "SELECT AccessionID, Number, Status, Type, VerbatimDate, DateAccessioned, DateReceived, Number1, Number2, YesNo1, YesNo2 FROM accession " +
                  "WHERE Number = \"" + oldAccNum + "\"";
        
         StatusType status = compareRecords("Accession", oldAccNum, newAccNum, oldSQL, newSQL, false);
         dumpStatus(status);
         return status == StatusType.COMPARE_OK;
    }
    
    /**
     * @param oldAccNum
     * @param newAccNum
     * @return
     * @throws SQLException
     */
    private boolean verifyAccessionAgents(final String oldAccNum, final String newAccNum) throws SQLException
    {
        newSQL = "SELECT ac.AccessionID, aa.Role, a.FirstName, a.MiddleInitial, a.LastName " +
        "FROM accession ac INNER JOIN accessionagent aa ON ac.AccessionID = aa.AccessionID "+
        "INNER JOIN agent a ON aa.AgentID = a.AgentID  " +
        "WHERE ac.AccessionNumber = '" + newAccNum + "' ORDER BY aa.Role, aa.TimestampCreated,  a.LastName";

        oldSQL = "SELECT ac.AccessionID, aa.Role, a.FirstName, a.MiddleInitial, a.LastName, a.Name " +
        "FROM accession ac INNER JOIN accessionagents aa ON ac.AccessionID = aa.AccessionID " +
        "INNER JOIN agentaddress ON aa.AgentAddressID = agentaddress.AgentAddressID " +
        "INNER JOIN agent a ON agentaddress.AgentID = a.AgentID " +
        "WHERE ac.Number = '" + oldAccNum + "' ORDER BY aa.Role, aa.TimestampCreated, a.Name, a.LastName";

        StatusType status = compareRecords("Accession", oldAccNum, newAccNum, oldSQL, newSQL, false);
        dumpStatus(status);
        return status == StatusType.COMPARE_OK;
    }


                         
    /**
     * @param oldSQL
     * @param newSQL
     * @throws SQLException
     */
    private void getResultSets(final String oldSQLArg, final String newSQLArg)  throws SQLException
    {
        try
        {
            newDBRS   = newDBStmt.executeQuery(newSQLArg);  
            oldDBRS   = oldDBStmt.executeQuery(compareTo6DBs ? newSQLArg : oldSQLArg);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @throws SQLException
     */
    private void doneWithRS() throws SQLException
    {
        newDBRS.close();
        oldDBRS.close();
    }
    
    /**
     * @param oldCatNum
     * @return
     */
    public static String convertCatNum(final int oldCatNum)
    {
        int size = 9;
        
        String fmt = size == 0 ? "%d" : ("%0" + size + "d");
        return String.format(fmt, oldCatNum);
    }
    
    /**
     * @param oldSQL
     * @param newSQL
     * @return
     * @throws SQLException
     */
    private StatusType compareRecords(final String desc, 
                                        final int    oldCatNum, 
                                        final String newCatNum, 
                                        final String oldSQLArg, 
                                        final String newSQLArg) throws SQLException
    {
        return compareRecords(desc, Integer.toString(oldCatNum), newCatNum, oldSQLArg, newSQLArg, false);
    }
    
    /**
     * @param desc
     * @param sql
     */
    private void dump(final String desc, final Connection conn, final String sql)
    {
        System.out.println("----- "+desc + "-----");
        System.out.println(sql);
        for (Object[] rows : BasicSQLUtils.query(conn, sql))
        {
            for (Object obj : rows)
            {
                System.out.print(obj);
                System.out.print(", ");
            }
            System.out.println();
        }
        System.out.println("------------------------------------------");
    }
    
    /**
     * @param oldSQL
     * @param newSQL
     * @return
     * @throws SQLException
     */
    private StatusType compareRecords(final String desc, 
                                      final String oldCatNumArg, 
                                      final String newCatNumArg, 
                                      final String oldSQLArg, 
                                      final String newSQLArg,
                                      final boolean nullsAreOK) throws SQLException
    {
        boolean dbg = false;
        if (dbg)
        {
            System.out.println(oldSQLArg);
            System.out.println(newSQLArg);
        }
        if (dbg)
        {
            System.out.println("\n"+desc);
            dump(desc, oldDBConn, compareTo6DBs ? newSQLArg : oldSQLArg);
            dump(desc, newDBConn, newSQLArg);
        } 
        
        String oldCatNum = oldCatNumArg;
        String newCatNum = newCatNumArg;
        if (compareTo6DBs)
        {
            oldCatNum = newCatNumArg;
        }

        getResultSets(oldSQLArg, newSQLArg);
        
        try
        {
            boolean hasOldRec = oldDBRS.next();
            boolean hasNewRec = newDBRS.next();
            
            if (!hasOldRec && !hasNewRec)
            {
                return StatusType.COMPARE_OK;
            }
            
            if (!hasOldRec)
            {
                if (nullsAreOK)
                {
                    log.error(desc+ " - No Old Record for ["+oldCatNum+"]");
                    tblWriter.logErrors(oldCatNum, "No Old Record");
                    return StatusType.NO_OLD_REC;
                }
                return StatusType.COMPARE_OK;
            }
            if (!hasNewRec)
            {
                log.error(desc+ " - No New Record for ["+newCatNum+"]");
                tblWriter.logErrors(newCatNum, "No New Record");
                return StatusType.NO_NEW_REC;
            }
            
            String oldNewIdStr = oldCatNum + " / "+newCatNum;
            
            boolean checkForAgent = newSQL.indexOf("a.LastName") > -1;
            
            ResultSetMetaData oldRsmd = oldDBRS.getMetaData();
            ResultSetMetaData newRsmd = newDBRS.getMetaData();
            
            PartialDateConv datePair = new PartialDateConv();
            Calendar        cal      = Calendar.getInstance();
            StringBuilder   errSB    = new StringBuilder();
            
            while (hasNewRec && hasOldRec)
            {
                errSB.setLength(0);
                
                int oldColInx = 0;
                int newColInx = 0;
                String idMsgStr = "";
                
                int numCols = newRsmd.getColumnCount();
                
                for (int col=0;col<numCols;col++)
                {
                    newColInx++;
                    oldColInx++;

                    if (dbg)
                    {
                        System.out.println("\ncol       "+col+" / "+oldRsmd.getColumnCount());
                        System.out.println("newColInx "+newColInx);
                        System.out.println("oldColInx "+oldColInx);
                        System.out.println(oldRsmd.getColumnName(oldColInx));
                        System.out.println(newRsmd.getColumnName(newColInx));
                    }
                    
                    Object newObj = newDBRS.getObject(newColInx);
                    Object oldObj = oldDBRS.getObject(oldColInx);
                    
                    if (oldObj == null && newObj == null)
                    {
                        String colName = newRsmd.getColumnName(newColInx);

                        if (StringUtils.contains(colName, "Date") && StringUtils.contains(newRsmd.getColumnName(newColInx+1), "DatePrecision"))
                        {
                            newColInx++;
                            numCols--;
                            if (compareTo6DBs) oldColInx++;
                        }
                        continue;
                    }
                    
                    if (col == 0)
                    {
                        idMsgStr = String.format(" - Rec Ids[%s / %s] ", (oldObj != null ? oldObj : -1), (newObj != null ? newObj : -1));
                        continue;
                    }
                    
                    String oldColName = oldRsmd.getColumnName(oldColInx);
                    if (oldColName.equals("PreparationMethod") && newObj != null)
                    {
                        String newObjStr = newObj.toString();
                        if ((oldObj == null && !newObjStr.equalsIgnoreCase("Misc")) || 
                            (oldObj != null && !newObjStr.equalsIgnoreCase(oldObj.toString())))
                        {
                            String msg = idMsgStr + "Old Value was null and shouldn't have been for Old CatNum ["+oldCatNum+"] Field ["+oldColName+"] oldObj["+oldObj+"] newObj ["+newObj+"]";
                            log.error(desc + " - " + msg);
                            tblWriter.logErrors(oldCatNum, msg);
                            return StatusType.OLD_VAL_NULL;
                        }
                        continue;
                    }
                    
                    if (oldObj == null && !StringUtils.contains(oldColName, "LastName"))
                    {
                        if (!oldColName.equals("PreparationMethod") || !newObj.equals("Misc"))
                        {
                            String msg = idMsgStr + "Old Value was null and shouldn't have been for Old CatNum ["+oldCatNum+"] Field ["+oldColName+"]  New Val["+newObj+"]";
                            log.error(desc+ " - "+msg);
                            tblWriter.logErrors(oldCatNum, msg);
                            return StatusType.OLD_VAL_NULL;
                        }
                    }
                    
                    if (newObj == null)
                    {
                        String clsName = newRsmd.getColumnClassName(newColInx);
                        String colName = newRsmd.getColumnName(newColInx);

                        if (compareTo6DBs)
                        {
                            if (!clsName.equals("java.sql.Date") || oldObj != null)
                            {
                                String msg = "New Value was null and shouldn't have been for Key Value New CatNo["+newCatNum+"] Field ["+colName+"] ["+oldObj+"]";
                                log.error(desc+ " - "+msg);
                                tblWriter.logErrors(newCatNum, msg);
                                return StatusType.NEW_VAL_NULL;
                            }
                            
                        } else
                        {
                            if (!clsName.equals("java.sql.Date") || (!(oldObj instanceof String) && ((Number)oldObj).intValue() != 0))
                            {
                                String msg = "New Value was null and shouldn't have been for Key Value New CatNo["+newCatNum+"] Field ["+colName+"] ["+oldObj+"]";
                                log.error(desc+ " - "+msg);
                                tblWriter.logErrors(newCatNum, msg);
                                dbg = true;
                                return StatusType.NEW_VAL_NULL;
                            }
                        }
                        
                        if (StringUtils.contains(colName, "Date") && StringUtils.contains(newRsmd.getColumnName(newColInx+1), "DatePrecision"))
                        {
                            newColInx++;
                            numCols--;
                            if (compareTo6DBs) oldColInx++;
                        }
                        continue;
                    }
                    
                    //String colName = newRsmd.getColumnName(col);
                    //System.out.println(newObj.getClass().getName()+"  "+oldObj.getClass().getName());
                    
                    if (newObj instanceof java.sql.Date)
                    {
                        boolean isPartialDate   = false;
                        Byte    partialDateType = null;
                        if (StringUtils.contains(newRsmd.getColumnName(newColInx+1), "DatePrecision"))
                        {
                            newColInx++;
                            numCols--;
                            partialDateType = newDBRS.getByte(newColInx);
                            isPartialDate   = true;
                        }
                        
                        if (compareTo6DBs)
                        {
                            Object dateObj = oldDBRS.getObject(oldColInx);
                            
                            boolean isPartialDate2   = false;
                            Byte    partialDateType2 = null;
                            if (StringUtils.contains(oldRsmd.getColumnName(oldColInx+1), "DatePrecision"))
                            {
                                oldColInx++;
                                partialDateType2 =  newDBRS.getByte(oldColInx);
                                isPartialDate2   = true;
                                
                            } else
                            {
                                log.error("Next isn't DatePrecision and can't be!");
                                tblWriter.logErrors(oldNewIdStr, errSB.toString());
                            }
                            
                            if (!newObj.equals(dateObj) || (isPartialDate2 && !partialDateType2.equals(partialDateType)))
                            {
                                errSB.insert(0, oldColName+"  ");
                                errSB.append("[");
                                errSB.append(datePair);
                                errSB.append("][");
                                errSB.append(dateFormatter.format((Date)newObj));
                                errSB.append("] oldDate[");
                                errSB.append(dateFormatter.format((Date)dateObj));
                                errSB.append("]");
                                log.error(errSB.toString());
                                tblWriter.logErrors(oldNewIdStr, errSB.toString());
                                return StatusType.BAD_DATE;
                            }
                            
                        } else
                        {
                            int oldIntDate = oldDBRS.getInt(oldColInx);
                            if (oldIntDate == 0)
                            {
                                continue;
                            }
                            
                            BasicSQLUtils.getPartialDate(oldIntDate, datePair, false);
                            
                            if (partialDateType != null)
                            {
                                boolean ok = StringUtils.isNotEmpty(datePair.getPartial()) && StringUtils.isNumeric(datePair.getPartial());
                                if (!ok || (Byte.parseByte(datePair.getPartial()) != partialDateType.byteValue()))
                                {
                                    errSB.append("Partial Dates Type do not match. Old["+datePair.getPartial()+"]  New ["+partialDateType.byteValue()+"]");
                                    // error partial dates don't match
                                }
                            } 
                            
                            cal.setTime((Date)newObj);
                            
                            if (StringUtils.isNotEmpty(datePair.getDateStr()) && !datePair.getDateStr().equalsIgnoreCase("null"))
                            {
                                int year = Integer.parseInt(datePair.getDateStr().substring(0, 4));
                                int mon  = Integer.parseInt(datePair.getDateStr().substring(5, 7));
                                int day  = Integer.parseInt(datePair.getDateStr().substring(8, 10));
                                
                                if (mon > 0) mon--;
                                
                                boolean isYearOK = true;
                                
                                int yr = cal.get(Calendar.YEAR);
                                if (year != yr)
                                {
                                    errSB.append("Year mismatch Old["+year+"]  New ["+yr+"] ");
                                    isYearOK = false;
                                }
                                
                                if (mon != cal.get(Calendar.MONTH))
                                {
                                    errSB.append("Month mismatch Old["+mon+"]  New ["+cal.get(Calendar.MONTH)+"] ");
                                }
                                
                                if (day != cal.get(Calendar.DAY_OF_MONTH))
                                {
                                    errSB.append("Day mismatch Old["+day+"]  New ["+cal.get(Calendar.DAY_OF_MONTH)+"] ");
                                }
                                
                                if (errSB.length() > 0 && (!isYearOK || !isPartialDate))
                                {
                                    errSB.insert(0, oldColName+"  ");
                                    errSB.append("[");
                                    errSB.append(datePair);
                                    errSB.append("][");
                                    errSB.append(dateFormatter.format((Date)newObj));
                                    errSB.append("]");
                                    log.error(errSB.toString());
                                    tblWriter.logErrors(oldNewIdStr, errSB.toString());
                                    return StatusType.BAD_DATE;
                                }
                            } else
                            {
                                //String msg = "Date contains the string 'NULL'";
                                //log.error(msg);
                                //tblWriter.logErrors(oldNewIdStr, msg);
                                //return StatusType.BAD_DATE;
                            }
                        }                        
                    } else if (newObj instanceof Float || newObj instanceof Double)
                    {
                        String s1 = String.format("%10.5f", newObj instanceof Float ? (Float)newObj : (Double)newObj);
                        String s2 = String.format("%10.5f", oldObj instanceof Float ? (Float)oldObj : (Double)oldObj);
                        if (!s1.equals(s2))
                        {
                            String msg = idMsgStr + "Columns don't compare["+s1+"]["+s2+"]  ["+newRsmd.getColumnName(col)+"]["+oldRsmd.getColumnName(oldColInx)+"]";
                            log.error(desc+ " - "+msg);
                            tblWriter.logErrors(oldNewIdStr, msg);
                            return StatusType.NO_COMPARE;
                        }
                        
                    } else
                    {
                        String newColName = newRsmd.getColumnName(newColInx);
                        if (checkForAgent && StringUtils.contains(newColName, "LastName"))
                        {
                            String lastName    = oldDBRS.getString(oldColInx);
                            String agentName   = oldDBRS.getString(oldColInx+1); // The 'Name' Column
                            String newLastName = newDBRS.getString(newColInx);
                            if (!newLastName.equals(lastName) && !newLastName.equals(agentName))
                            {
                                String msg = idMsgStr + "Name Columns don't compare["+newObj+"]["+oldObj+"]  ["+newColName+"]["+oldColName+"]";
                                log.error(desc+ " - "+msg);
                                tblWriter.logErrors(oldNewIdStr, msg);
                                log.error(oldSQLArg+"\n"+newSQLArg);
                                return StatusType.NO_COMPARE;
                            }
                            
                        } else if (StringUtils.contains(newColName, "YesNo"))
                        {
                            boolean yesNoNew = newDBRS.getBoolean(newColInx);
                            boolean yesNoOld = oldDBRS.getInt(oldColInx) != 0;
                            
                            if (yesNoNew != yesNoOld)
                            {
                                String msg = idMsgStr + "Columns don't Cat Num["+oldCatNum+"] compare["+yesNoNew+"]["+yesNoOld+"]  ["+newColName+"]["+oldColName+"]";
                                log.error(desc+ " - "+msg);
                                tblWriter.logErrors(oldNewIdStr, msg);
                                return StatusType.NO_COMPARE;
                            }
                            
                        } else if (!newObj.equals(oldObj))
                        {
                            String msg = idMsgStr + "Columns don't Cat Num["+oldCatNum+"] compare["+newObj+"]["+oldObj+"]  ["+newColName+"]["+oldColName+"]";
                            log.error(desc+ " - "+msg);
                            tblWriter.logErrors(oldNewIdStr, msg);
                            return StatusType.NO_COMPARE;

                            /*boolean isOK = false;
                            if (oldObj instanceof String)
                            {
                                String oldStr = (String)oldObj;
                                String newStr = (String)newObj;
                                String lof    = "\\r\\n";
                                int    inx    = newStr.indexOf(lof);
                                if (inx > -1)
                                {
                                    String tok = oldStr.substring(0, inx);
                                    if (newStr.equals(tok))
                                    {
                                        isOK = true;
                                    }
                                }
                            }
                            if (!isOK)
                            {
                                log.error(desc+ " - Columns don't compare["+newObj+"]["+oldObj+"]  ["+newRsmd.getColumnName(newColInx)+"]["+oldRsmd.getColumnName(oldColInx)+"]");
                                return false;
                            }*/
                        }
                    }
                }
                
                hasOldRec = oldDBRS.next();
                hasNewRec = newDBRS.next();
                
                if (!hasOldRec && !hasNewRec)
                {
                    return StatusType.COMPARE_OK;
                }
                
                if (!hasOldRec)
                {
                    log.error(desc+ idMsgStr + " - No Old Record for ["+oldCatNum+"]");
                    tblWriter.logErrors(oldNewIdStr, "No Old Record for ["+oldCatNum+"]");
                    return StatusType.NO_OLD_REC;
                }
                if (!hasNewRec)
                {
                    log.error(desc+ idMsgStr + " No New Record for ["+newCatNum+"]");
                    tblWriter.logErrors(oldNewIdStr, "No New Record for ["+newCatNum+"]");
                    return StatusType.NO_NEW_REC;
                }
            }
        } finally
        {
            doneWithRS();
        }
            
        return StatusType.COMPARE_OK;
    }
    
    /**
     * @param oldNewIdStr
     * @param newColInx
     * @param oldColInx
     * @return
     * @throws SQLException
     */
    private StatusType compareDates(final String oldNewIdStr, final int newColInx, final int oldColInx) throws SQLException 
    {
        PartialDateConv datePair = new PartialDateConv();
        
        Object newObj = newDBRS.getObject(newColInx);
        Object oldObj = oldDBRS.getObject(oldColInx);
        
        ResultSetMetaData newRsmd = newDBRS.getMetaData();
        ResultSetMetaData oldRsmd = oldDBRS.getMetaData();
        
        String newColName = newRsmd.getColumnName(newColInx);
        String oldColName = oldRsmd.getColumnName(oldColInx);
        
        if (newObj == null)
        {
            String clsName = newRsmd.getColumnClassName(newColInx);

            if (compareTo6DBs)
            {
                if (!clsName.equals("java.sql.Date") || oldObj != null)
                {
                    String msg = "New Value was null and shouldn't have been for Key Value New  Field ["+newColName+"] ["+oldObj+"]";
                    log.error(msg);
                    tblWriter.logErrors(newColName, msg);
                    return StatusType.NEW_VAL_NULL;
                }
                
            } else if (oldObj != null)
            {
                if (oldObj instanceof Number && ((Number)oldObj).intValue() == 0)
                {
                    return StatusType.COMPARE_OK;
                    
                } else if (!clsName.equals("java.sql.Date") || (!(oldObj instanceof String) && ((Number)oldObj).intValue() != 0))
                {
                    String msg = "New Value was null and shouldn't have been for Key Value New Field ["+newColName+"] ["+oldObj+"]";
                    log.error(msg);
                    tblWriter.logErrors(newColName, msg);
                    return StatusType.NEW_VAL_NULL;
                }
            } else
            {
                return StatusType.COMPARE_OK;
            }
        }
        
        StringBuilder errSB = new StringBuilder();
        
        //System.out.println(newObj.getClass().getName()+"  "+oldObj.getClass().getName());
        
        if (newObj instanceof java.sql.Date)
        {
            boolean isPartialDate   = false;
            Byte    partialDateType = null;
            if (StringUtils.contains(newRsmd.getColumnName(newColInx+1), "DatePrecision"))
            {
                partialDateType = newDBRS.getByte(newColInx);
                isPartialDate   = true;
            }
            
            if (compareTo6DBs)
            {
                Object dateObj = oldDBRS.getObject(oldColInx);
                
                boolean isPartialDate2   = false;
                Byte    partialDateType2 = null;
                if (StringUtils.contains(oldRsmd.getColumnName(oldColInx+1), "DatePrecision"))
                {
                    partialDateType2 =  newDBRS.getByte(oldColInx);
                    isPartialDate2   = true;
                    
                } else
                {
                    log.error("Next isn't DatePrecision and can't be!");
                    tblWriter.logErrors(oldNewIdStr, errSB.toString());
                }
                
                if (!newObj.equals(dateObj) || (isPartialDate2 && !partialDateType2.equals(partialDateType)))
                {
                    errSB.insert(0, oldColName+"  ");
                    errSB.append("[");
                    errSB.append(datePair);
                    errSB.append("][");
                    errSB.append(dateFormatter.format((Date)newObj));
                    errSB.append("] oldDate[");
                    errSB.append(dateFormatter.format((Date)dateObj));
                    errSB.append("]");
                    log.error(errSB.toString());
                    tblWriter.logErrors(oldNewIdStr, errSB.toString());
                    return StatusType.BAD_DATE;
                }
                
            } else
            {
                int oldIntDate = oldDBRS.getInt(oldColInx);
                if (oldIntDate == 0)
                {
                    return StatusType.NO_OLD_REC;
                }
                
                BasicSQLUtils.getPartialDate(oldIntDate, datePair, false);
                
                if (partialDateType != null)
                {
                    if (Byte.parseByte(datePair.getPartial()) != partialDateType.byteValue())
                    {
                        errSB.append("Partial Dates Type do not match. Old["+datePair.getPartial()+"]  New ["+partialDateType.byteValue()+"]");
                        // error partial dates don't match
                    }
                } 
                
                Calendar cal = Calendar.getInstance();
                cal.setTime((Date)newObj);
                
                int year = Integer.parseInt(datePair.getDateStr().substring(0, 4));
                int mon  = Integer.parseInt(datePair.getDateStr().substring(5, 7));
                int day  = Integer.parseInt(datePair.getDateStr().substring(8, 10));
                
                if (mon > 0) mon--;
                
                boolean isYearOK = true;
                
                int yr = cal.get(Calendar.YEAR);
                if (year != yr)
                {
                    errSB.append("Year mismatch Old["+year+"]  New ["+yr+"] ");
                    isYearOK = false;
                }
                
                if (mon != cal.get(Calendar.MONTH))
                {
                    errSB.append("Month mismatch Old["+mon+"]  New ["+cal.get(Calendar.MONTH)+"] ");
                }
                
                if (day != cal.get(Calendar.DAY_OF_MONTH))
                {
                    errSB.append("Day mismatch Old["+day+"]  New ["+cal.get(Calendar.DAY_OF_MONTH)+"] ");
                }
                
                if (errSB.length() > 0 && (!isYearOK || !isPartialDate))
                {
                    errSB.insert(0, oldColName+"  ");
                    errSB.append("[");
                    errSB.append(datePair);
                    errSB.append("][");
                    errSB.append(dateFormatter.format((Date)newObj));
                    errSB.append("]");
                    log.error(errSB.toString());
                    tblWriter.logErrors(oldNewIdStr, errSB.toString());
                    return StatusType.BAD_DATE;
                }
            } 
        }
        
        return StatusType.COMPARE_OK;
    }
    
    /**
     * @param oldNewIdStr
     * @throws SQLException
     */
    private void compareNames(final String oldNewIdStr, final int startInxNewArg, final int startInxOldArg) throws SQLException 
    {
        String newFirstName = newDBRS.getString(startInxNewArg);
        String newLastName  = newDBRS.getString(startInxNewArg+1);
        
        String oldFirstName = oldDBRS.getString(startInxOldArg);
        String oldLastName  = oldDBRS.getString(startInxOldArg+1);
        String oldName      = oldDBRS.getString(startInxOldArg+2);
        
        if (StringUtils.isNotEmpty(oldName) && StringUtils.isEmpty(oldLastName))
        {
            oldLastName = oldName;
        }
        
        if (oldFirstName == null && oldLastName == null && oldName != null)
        {
            oldLastName = oldName;
        }
        
        // First Name
        if (oldFirstName == null && newFirstName != null)
        {
            String msg = "Old FirstName["+oldFirstName+"] is NULL   New FirstName["+newFirstName+"] is not";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
            
        } else if (oldFirstName != null && newFirstName == null)
        {
            String msg = "Old FirstName["+oldFirstName+"] is not null   New FirstName["+newFirstName+"] is NULL";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
        }
        
        if (oldFirstName != null && newFirstName != null && !oldFirstName.equals(newFirstName))
        {
            String msg = "Old FirstName["+oldFirstName+"] is NOT equals   New FirstName["+newFirstName+"]";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
        }
        
        // Last Name and old Name
        if (oldLastName == null && newLastName != null)
        {
            String msg = "Old LastName["+oldLastName+"] is NULL   New LastName["+newLastName+"] is not";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);

        } else if (oldLastName != null && newLastName == null)
        {
            String msg = "Old LastName["+oldLastName+"] is not null   New LastName["+newLastName+"] is NULL";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);

        } else if (oldLastName != null && newLastName != null && !oldLastName.equals(newLastName))
        {
            String msg = "Old LastName["+oldLastName+"] is NOT equals   New LastName["+newLastName+"]";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
        }
    }
    
    /**
     * @param oldNewIdStr
     * @throws SQLException
     */
    private void compareStrings(final String oldNewIdStr, final String colName, final int startInxNewArg, final int startInxOldArg) throws SQLException 
    {
        String newStr = newDBRS.getString(startInxNewArg);
        String oldStr = oldDBRS.getString(startInxOldArg);
        
        if (oldStr == null && newStr == null) return;
        
        if (oldStr == null && newStr != null)
        {
            String msg = "Old "+colName+" ["+oldStr+"] is NULL   New "+colName+"["+newStr+"] is not";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
            
        } else if (oldStr != null && newStr == null)
        {
            String msg = "Old "+colName+"["+oldStr+"] is not null   New "+colName+"["+newStr+"] is NULL";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
            
        } else if (!oldStr.equals(newStr))
        {
            String msg = "Old "+colName+"["+oldStr+"] is not equal   New "+colName+"["+newStr+"]";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
        }
    }

    /**
     * @param oldNewIdStr
     * @throws SQLException
     */
    private void compareBoolean(final String oldNewIdStr, final String colName, final int startInxNewArg, final int startInxOldArg) throws SQLException 
    {
        boolean newBool = newDBRS.getBoolean(startInxNewArg);
        boolean oldBool = oldDBRS.getBoolean(startInxOldArg);
        
        if (newBool != oldBool)
        {
            String msg = "Old "+colName+"["+oldBool+"] is not null   New "+colName+"["+newBool+"] is NULL";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
        }
    }

    /**
     * @param oldNewIdStr
     * @throws SQLException
     */
    private void compareNumber(final String oldNewIdStr, final String colName, final int startInxNewArg, final int startInxOldArg) throws SQLException 
    {
        Integer newInt = (Integer)newDBRS.getObject(startInxNewArg);
        Integer oldInt = (Integer)oldDBRS.getObject(startInxOldArg);
        
        if (oldInt == null && newInt == null) return;
        
        if (oldInt == null && newInt != null)
        {
            String msg = "Old "+colName+" ["+oldInt+"] is NULL New "+colName+"["+newInt+"] is not";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
            
        } else if (oldInt != null && newInt == null)
        {
            String msg = "Old "+colName+"["+oldInt+"] is not null New "+colName+"["+newInt+"] is NULL";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
            
        } else if (!oldInt.equals(newInt))
        {
            String msg = "Old "+colName+"["+oldInt+"] is not equal New "+colName+"["+newInt+"]";
            log.error(oldNewIdStr + " " + msg);
            tblWriter.logErrors(oldNewIdStr, msg);
        }
    }
    
    /**
     * 
     */
    private void verifyCollectors()
    {
    	boolean dbg = true;
    	
    	IdTableMapper ceIdMapper = idMapperMgr.addTableMapper("collectingevent", "CollectingEventID", false);
    	
         oldSQL = "SELECT ce.CollectingEventID, a.FirstName,  a.LastName, a.Name, collectors.Order  " + 
         "FROM collectingevent ce INNER JOIN collectors ON ce.CollectingEventID = collectors.CollectingEventID " + 
         "INNER JOIN agent a ON collectors.AgentID = a.AgentID ORDER BY ce.CollectingEventID, collectors.Order";
    	
    	 newSQL = "SELECT ce.CollectingEventID, a.FirstName, a.LastName, collector.OrderNumber   " + 
    	 "FROM collectingevent ce INNER JOIN collector ON ce.CollectingEventID = collector.CollectingEventID  " + 
    	 "INNER JOIN agent a ON collector.AgentID = a.AgentID WHERE ce.CollectingEventID = %d ORDER BY collector.OrderNumber ";
    	
    	int prevOldId = Integer.MAX_VALUE;
    	int prevNewId = Integer.MAX_VALUE;
    	
    	try
        {
    	    oldDBRS = oldDBStmt.executeQuery(oldSQL); 
	        
	        if (dbg)
	        {
	            System.out.println(oldSQL);
	        }
	        
	        while (oldDBRS.next())
	        {
	            int     oldId = oldDBRS.getInt(1);
	            Integer newId = ceIdMapper.get(oldId);
	            
	            String oldNewIdStr = oldId + " / "+newId;
	            
	            if (newId == null)
	            {
                    String msg = "No New Id from mapping New ["+newId+"]";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
	                continue;
	            }
	            
	            String sql = String.format(newSQL, newId);
	            if (dbg) System.out.println(sql);
	            
	            newDBRS = newDBStmt.executeQuery(sql); 
	            if (!newDBRS.next())
	            {
	                String msg = "No New record  New ["+newId+"] from Old["+oldId+"]";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                    continue;
	            }
	            
	            int    newOrder = newDBRS.getInt(4);
	            int    oldOrder = oldDBRS.getInt(5);
	            
	            if (newId == Integer.MAX_VALUE)
	            {
	            	prevNewId = newId;
	            }
	            
	            if (oldId == Integer.MAX_VALUE)
	            {
	            	prevOldId = oldId;
	            }
	            
	            boolean isNewNextCE = prevNewId != newId;
	            boolean isOldNextCE = prevOldId != oldId;
	            
	            if (isNewNextCE != isOldNextCE)
	            {
	            	String msg = "The is a mismatch in the number of Collectors for Old["+oldId+"]  New ["+newId+"] Old["+isOldNextCE+"]  New ["+isNewNextCE+"] ";
	            	log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                    break;
	            }
	            
	            compareNames(oldNewIdStr, 2, 2);
	            
	            // Order
	            if (oldOrder != newOrder)
	            {
	            	String msg = "Old Order["+oldOrder+"] is not equal ["+newOrder+"]";
	            	log.error(oldNewIdStr + " " + msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
	            }
	        }
	        
            oldDBRS.close();
            newDBRS.close();
	            
        } catch (SQLException ex)
        {
        	ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    private void verifyCEs()
    {
        newSQL = "SELECT c.CollectingEventID, c.StartTime, l.LocalityName, l.Latitude1, l.Longitude1, g.Name " + 
                        "FROM collectingevent c LEFT JOIN locality l ON c.LocalityID = l.LocalityID " + 
                        "LEFT JOIN geography g ON l.GeographyID = g.GeographyID ORDER BY c.CollectingEventID";


        oldSQL = "SELECT c.CollectingEventID, c.StartTime, l.LocalityName, l.Latitude1, l.Longitude1, g.ContinentOrOcean, g.Country, g.State,  g.County, g.IslandGroup, g.Island, g.WaterBody, g.Drainage "+
                        "FROM collectingevent c LEFT JOIN locality l ON c.LocalityID = l.LocalityID "+
                        "LEFT JOIN geography g ON l.GeographyID = g.GeographyID ORDER BY c.CollectingEventID";
        
        String newCntSQL = "SELECT count(*) " + 
                           "FROM collectingevent c LEFT JOIN locality l ON c.LocalityID = l.LocalityID " + 
                           "LEFT JOIN geography g ON l.GeographyID = g.GeographyID ORDER BY c.CollectingEventID";


        String oldCntSQL = "SELECT count(*) "+
                           "FROM collectingevent c LEFT JOIN locality l ON c.LocalityID = l.LocalityID "+
                           "LEFT JOIN geography g ON l.GeographyID = g.GeographyID ORDER BY c.CollectingEventID";


        log.info(newCntSQL);
        log.info(oldCntSQL);
        log.info(newSQL);
        log.info(oldSQL);

        Integer oldCnt = BasicSQLUtils.getCount(oldCntSQL);
        Integer newCnt = BasicSQLUtils.getCount(newCntSQL);
        String msg2 = "Record Counts ["+oldCnt + " / "  + newCnt+"]";
        log.info(msg2);
        
        //tblWriter.logErrors("Record Counts", oldCnt + " / "  + newCnt);
        tblWriter.flush();
        
        try
        {
            getResultSets(oldSQL, newSQL);
            while (true)
            {
            
                boolean hasOldRec = oldDBRS.next();
                boolean hasNewRec = newDBRS.next();
                
                if (!hasOldRec || !hasNewRec)
                {
                    break;
                }
                
                int col = 1;
                int        newId           = newDBRS.getInt(col++);
                Integer    newStartTime    = newDBRS.getInt(col++);
                String     newLocalityName = newDBRS.getString(col++);
                
                Object     bigDecObj       = newDBRS.getObject(col); 
                BigDecimal newLatitude     = bigDecObj == null ? null : newDBRS.getBigDecimal(col);
                col++;
                
                bigDecObj = newDBRS.getObject(col);
                BigDecimal newLongitude    = bigDecObj == null ? null : newDBRS.getBigDecimal(col);
                col++;
                
                String     newGeoName      = newDBRS.getString(col++);
                
                col = 1;
                int          oldId           = oldDBRS.getInt(col++);
                Integer      oldStartTime    = oldDBRS.getInt(col++);
                String       oldLocalityName = oldDBRS.getString(col++);
                
                bigDecObj = newDBRS.getObject(col); 
                Double       oldLatitude     = bigDecObj == null ? null : oldDBRS.getDouble(col);
                col++;
                
                bigDecObj = newDBRS.getObject(col); 
                Double       oldLongitude    = bigDecObj == null ? null : oldDBRS.getDouble(col);
                col++;
                
                String oldNewIdStr = oldId + " / "+newId;
                
                if (newGeoName != null && !newGeoName.equals("Undefined"))
                {
                    boolean fnd       = false;
                    for (int i=6;i<14;i++)
                    {
                        //if (i == 7) System.out.println();
                        String name = oldDBRS.getString(i);
                        if (name != null)
                        {
                            //System.out.println("["+name+"]");
                            if (name.equalsIgnoreCase(newGeoName))
                            {
                                fnd = true;
                                break;
                            }
                        }
                    }
                    
                    if (!fnd)
                    {
                        String msg = "No match found for new Geo ["+newGeoName+"] ["+oldId + " / "  + newId+"]";
                        log.error(msg);
                        tblWriter.logErrors(oldNewIdStr, msg);
                    }
                }

                // StartTime
                if (oldStartTime == null && newStartTime != null)
                {
                    String msg = "LocName["+oldId + " / "  + newId+"]  Old StartTime["+oldStartTime+"] is NULL   New StartTime["+newStartTime+"] is not";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                    
                } else if (oldStartTime != null && newStartTime == null)
                {
                    String msg = "LocName["+oldId + " / "  + newId+"]  Old StartTime["+oldStartTime+"] is not null   New StartTime["+newStartTime+"] is NULL";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                    
                } else if (oldStartTime != null && newStartTime != null && !oldStartTime.equals(newStartTime))
                {
                    String msg = "LocName["+oldId + " / "  + newId+"]  Old StartTime["+oldStartTime+"] is NOT equals   New StartTime["+newStartTime+"]";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                }

                
                // LocalityName
                if (oldLocalityName == null && newLocalityName != null)
                {
                    String msg = "LocName["+oldId + " / "  + newId+"]  Old LocalityName["+oldLocalityName+"] is NULL   New LocalityName["+newLocalityName+"] is not";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                } else if (oldLocalityName != null && newLocalityName == null)
                {
                    String msg = "LocName["+oldId + " / "  + newId+"]  Old LocalityName["+oldLocalityName+"] is not null   New LocalityName["+newLocalityName+"] is NULL";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                } else if (oldLocalityName != null && newLocalityName != null && !oldLocalityName.equals(newLocalityName))
                {
                    String msg = "LocName["+oldId + " / "  + newId+"]  Old LocalityName["+oldLocalityName+"] is NOT equals   New LocalityName["+newLocalityName+"]";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                }
                
                // Latitude
                if (oldLatitude == null && newLatitude != null)
                {
                    String msg = "Latitude["+oldId + " / "  + newId+"]  Old Latitude["+oldLatitude+"] is NULL   New Latitude["+newLatitude+"] is not";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                } else if (oldLatitude != null && newLatitude == null)
                {
                    String msg = "Latitude["+oldId + " / "  + newId+"]  Old Latitude["+oldLatitude+"] is not null   New Latitude["+newLatitude+"] is NULL";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                } else if (oldLatitude != null && newLatitude != null && !oldLatitude.equals(newLatitude.doubleValue()))
                {
                    String msg = "Latitude["+oldId + " / "  + newId+"]  Old Latitude["+oldLatitude+"] is NOT equals   New Latitude["+newLatitude+"]";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                }
                
                // Longitude
                if (oldLongitude == null && newLongitude != null)
                {
                    String msg = "Longitude["+oldId + " / "  + newId+"]  Old Longitude["+oldLongitude+"] is NULL   New Longitude["+newLongitude+"] is not";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                } else if (oldLongitude != null && newLongitude == null)
                {
                    String msg = "Longitude["+oldId + " / "  + newId+"]  Old Longitude["+oldLongitude+"] is not null   New Longitude["+newLongitude+"] is NULL";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                } else if (oldLongitude != null && newLongitude != null && !oldLongitude.equals(newLongitude.doubleValue()))
                {
                    String msg = "Longitude["+oldId + " / "  + newId+"]  Old Longitude["+oldLongitude+"] is NOT equals   New Longitude["+newLongitude+"]";
                    log.error(msg);
                    tblWriter.logErrors(oldNewIdStr, msg);
                }
            }
            
            oldDBRS.close();
            newDBRS.close();

        } catch (Exception ex)
        {
            ex.printStackTrace(); 
        }
    }
    
    private boolean compareStr(final String oldStr, final String newStr)
    {
        if (oldStr == null && newStr == null)
        {
            return true;
        }
        
        if (oldStr == null || newStr == null)
        {
            return false;
        }
        
        return oldStr.equals(newStr);
    }
    
    /**
     * 
     */
    private void verifyAgents()
    {
    
        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs   = stmt.executeQuery("SELECT OldID, NewID FROM agent_AgentID");
            while (rs.next())
            {
                int oldId = rs.getInt(1);
                int newId = rs.getInt(2);
                
                newSQL = "SELECT a.AgentType, a.LastName, a.MiddleInitial, a.FirstName, " +
                         "adr.Phone1, adr.Phone2, adr.Address, adr.City, adr.State, adr.PostalCode, adr.Country " +
                         "FROM agent AS a Left Join address AS adr ON a.AgentID = adr.AgentID WHERE a.AgentID = " + newId + " ORDER BY adr.Phone1, adr.Address, adr.City, adr.State, adr.PostalCode";
   
                oldSQL = "SELECT a.AgentType, IF (a.LastName IS null OR LENGTH(a.LastName) = 0, a.Name, a.LastName), a.MiddleInitial, a.FirstName," +
                         "aa.Phone1, aa.Phone2 ,adr.Address, adr.City, adr.State ,adr.Postalcode, adr.Country FROM agent AS a " +
                         "Left Join agentaddress AS aa ON a.AgentID = aa.AgentID " +
                         "Left Join address AS adr ON aa.AddressID = adr.AddressID WHERE a.AgentID = " + oldId + " ORDER BY aa.Phone1, adr.Address, adr.City, adr.State ,adr.Postalcode";
            
                //log.info(newSQL);
                //log.info(oldSQL);
                getResultSets(oldSQL, newSQL);
                
                while (true)
                {
                    boolean hasOldRec = oldDBRS.next();
                    boolean hasNewRec = newDBRS.next();
        
                    if (!hasOldRec || !hasNewRec)
                    {
                        break;
                    }
                    
                    for (int i=1;i<=newDBRS.getMetaData().getColumnCount();i++)
                    {
                        String newStr = newDBRS.getString(i);
                        String oldStr = oldDBRS.getString(i);
                        if (!compareStr(oldStr, newStr))
                        {
                            String fldName = newDBRS.getMetaData().getColumnName(i);
                            String oldNewIdStr = oldId + " / "+newId;
                            String msg  = " Fields "+fldName+" don't match. ["+oldStr+"]["+newStr+"]";
                            tblWriter.logErrors(oldNewIdStr, msg);
                            log.error(oldNewIdStr+msg);
                        }
                    }
                }
            }
            rs.close();
            stmt.close();
            
            oldDBRS.close();
            newDBRS.close();

        } catch (Exception ex)
        {
            ex.printStackTrace(); 
        }

    }
    
    /**
     * 
     */
    private void verifyShipments()
    {
    
        newSQL = "SELECT s.ShipmentNumber,s.ShipmentDate, s.ShipmentMethod, s.NumberOfPackages, s.Weight, s.InsuredForAmount, ato.FirstName, ato.LastName, aby.FirstName, aby.LastName " +
                 "FROM shipment AS s " +
                 "Inner Join agent AS ato ON s.ShippedToID = ato.AgentID " +
                 "Inner Join agent AS aby ON s.ShippedByID = aby.AgentID " +
                 "ORDER BY s.ShipmentNumber ASC";
    
        oldSQL = "SELECT s.ShipmentNumber,s.ShipmentDate, s.ShipmentMethod, s.NumberOfPackages, s.Weight, s.InsuredForAmount, ato.FirstName, ato.LastName, ato.Name, aby.FirstName, aby.LastName, aby.Name " +
                 "FROM shipment AS s " +
                 "Inner Join agent AS ato ON s.ShippedToID = ato.AgentID " +
                 "Inner Join agent AS aby ON s.ShippedByID = aby.AgentID " +
                 "ORDER BY s.ShipmentNumber ASC";
    
        log.info(newSQL);
        log.info(oldSQL);
        
        int prevOldId = Integer.MAX_VALUE;
        int prevNewId = Integer.MAX_VALUE;
    
        try
        {
            getResultSets(oldSQL, newSQL);
            
            
            ResultSetMetaData rmd = newDBRS.getMetaData();
            while (true)
            {
            
                boolean hasOldRec = oldDBRS.next();
                boolean hasNewRec = newDBRS.next();
                
                if (!hasOldRec && !hasNewRec)
                {
                    break;
                }
                
                int    newId        = newDBRS.getInt(1);
                System.out.println(newId);
                int    oldId        = oldDBRS.getInt(1);
                
                String oldNewIdStr = oldId + " / "+newId;
                
                if (newId == Integer.MAX_VALUE)
                {
                    prevNewId = newId;
                }
                
                if (oldId == Integer.MAX_VALUE)
                {
                    prevOldId = oldId;
                }
                
                compareStrings(oldNewIdStr, rmd.getColumnName(1), 1, 1);
                compareStrings(oldNewIdStr, rmd.getColumnName(2), 2, 2);
                compareDates(oldNewIdStr, 3, 3);
                compareStrings(oldNewIdStr, rmd.getColumnName(4), 4, 4);
                compareStrings(oldNewIdStr, rmd.getColumnName(5), 5, 5);
                compareStrings(oldNewIdStr, rmd.getColumnName(6), 6, 6);
                
                compareNames(oldNewIdStr, 7,7);
                compareNames(oldNewIdStr, 9, 10);
                               
            }
            
            oldDBRS.close();
            newDBRS.close();

        } catch (Exception ex)
        {
            ex.printStackTrace(); 
        }

    }
    
    /**
     * 
     */
    private void verifyLoans()
    {
        
        newSQL = "SELECT l.LoanNumber, l.LoanDate, l.IsClosed, l.CurrentDueDate, l.OriginalDueDate, l.DateClosed, l.Text1, l.Text2, l.Number1, l.Number2, l.YesNo1, l.YesNo2, " +
                 "lp.Quantity, lp.DescriptionOfMaterial, lp.OutComments, lp.InComments, lp.QuantityResolved, lp.QuantityReturned, LoanPreparationID " +
                 "FROM loan l Inner Join loanpreparation lp ON l.LoanID = lp.LoanID WHERE LoanNumber = '%s' ORDER BY LoanPreparationID";

    
        oldSQL = "SELECT l.LoanNumber, l.LoanDate, l.Closed, l.CurrentDueDate, l.OriginalDueDate, l.DateClosed, l.Text1, l.Text2, l.Number1, l.Number2, l.YesNo1, l.YesNo2, " +
        	     "lp.Quantity, lp.DescriptionOfMaterial, lp.OutComments, lp.InComments, lp.QuantityResolved, lp.QuantityReturned, lp.LoanPhysicalObjectID " +
        	     "FROM loan AS l Inner Join loanphysicalobject AS lp ON l.LoanID = lp.LoanID Left Join loanphysicalobject_LoanPhysicalObjectID AS lr ON lp.LoanPhysicalObjectID = lr.OldID " +
                 "WHERE l.Category = 0 AND LoanNumber = '%s' ORDER BY lr.NewID ASC";
    
        String newSQLCnt = "SELECT COUNT(*) FROM loan l Inner Join loanpreparation lp ON l.LoanID = lp.LoanID WHERE LoanNumber = '%s'";
        String oldSQLCnt = "SELECT COUNT(*) FROM loan AS l Inner Join loanphysicalobject AS lp ON l.LoanID = lp.LoanID WHERE l.Category = 0 AND LoanNumber = '%s'";

        log.info(newSQL);
        log.info(oldSQL);
        
        try
        {
            Vector<Object> loanNums = BasicSQLUtils.querySingleCol(oldDBConn, "SELECT LoanNumber FROM loan WHERE Category = 0");
            
            for (Object loanNum : loanNums)
            {
                int oldCnt = BasicSQLUtils.getCountAsInt(oldDBConn, String.format(oldSQLCnt, loanNum));
                int newCnt = BasicSQLUtils.getCountAsInt(newDBConn, String.format(newSQLCnt, loanNum));
                
                if (oldCnt != newCnt)
                {
                    String msg = "For Loan Number["+loanNum+" the number of New LoanPreps["+newCnt+"] doesn't match the old["+oldCnt+"]";
                    log.error(msg);
                    tblWriter.logErrors(loanNum.toString(), msg);
                }
                
                getResultSets(String.format(oldSQL, loanNum), String.format(newSQL, loanNum));
                
                
                ResultSetMetaData rmd = newDBRS.getMetaData();
                while (true)
                {
                
                    boolean hasOldRec = oldDBRS.next();
                    boolean hasNewRec = newDBRS.next();
                    
                    if (!hasOldRec && !hasNewRec)
                    {
                        break;
                    }
                    
                    int    newId        = newDBRS.getInt(1);
                    int    oldId        = oldDBRS.getInt(1);
                    
                    String oldNewIdStr = oldId + " / "+newId + " ("+oldDBRS.getInt(19)+" / "+newDBRS.getInt(19)+")";
                    
                    compareStrings(oldNewIdStr, rmd.getColumnName(1), 1, 1);
                    
                    compareDates(oldNewIdStr, 2, 2);
                    
                    compareBoolean(oldNewIdStr, rmd.getColumnName(3), 3, 3);
                    
                    compareDates(oldNewIdStr, 4, 4);
                    compareDates(oldNewIdStr, 5, 5);
                    compareDates(oldNewIdStr, 6, 6);
                    
                    compareStrings(oldNewIdStr, rmd.getColumnName(7), 7, 7);
                    compareStrings(oldNewIdStr, rmd.getColumnName(8), 8, 8);
                    
                    compareNumber(oldNewIdStr,  rmd.getColumnName(9), 9, 9);
                    compareNumber(oldNewIdStr,  rmd.getColumnName(10), 10, 10);
                    
                    compareBoolean(oldNewIdStr, rmd.getColumnName(11), 11, 11);
                    compareBoolean(oldNewIdStr, rmd.getColumnName(12), 12, 12);
                    
                    compareNumber(oldNewIdStr,  rmd.getColumnName(13), 13, 13);
                    
                    compareStrings(oldNewIdStr, rmd.getColumnName(14), 14, 14);
                    compareStrings(oldNewIdStr, rmd.getColumnName(15), 15, 15);
                    compareStrings(oldNewIdStr, rmd.getColumnName(16), 16, 16);
                    
                    compareNumber(oldNewIdStr,  rmd.getColumnName(17), 17, 17);
                    compareNumber(oldNewIdStr,  rmd.getColumnName(18), 18, 18);
                }
                
                oldDBRS.close();
                newDBRS.close();
            }

        } catch (Exception ex)
        {
            ex.printStackTrace(); 
        }

    }
    
    
    /**
     * 
     */
    private void verifyGifts()
    {
        
        newSQL = "SELECT g.GiftNumber, g.GiftDate, g.Remarks, g.Number1, g.Number2, g.Text1, g.Text2, g.YesNo1, g.YesNo2, gp.Quantity, gp.DescriptionOfMaterial, gp.OutComments, gp.InComments " +
        		 "FROM gift AS g Inner Join giftpreparation AS gp ON g.GiftID = gp.GiftID WHERE g.GiftNumber = '%s' ORDER BY gp.GiftPreparationID";
    
        oldSQL = "SELECT g.LoanNumber, g.LoanDate, g.Remarks, g.Number1, g.Number2, g.Text1, g.Text2, g.YesNo1, g.YesNo2, " +
                 "gp.Quantity, gp.DescriptionOfMaterial, gp.OutComments, gp.InComments " +
                 "FROM loan AS g Inner Join loanphysicalobject AS gp ON g.LoanID = gp.LoanID Left Join loanphysicalobject_LoanPhysicalObjectID AS lr ON gp.LoanPhysicalObjectID = lr.OldID " +
                 "WHERE g.Category = 1 AND g.LoanNumber = '%s' ORDER BY lr.NewID ASC";
    
        String newSQLCnt = "SELECT COUNT(*) FROM gift AS g Inner Join giftpreparation AS gp ON g.GiftID = gp.GiftID WHERE GiftNumber = '%s'";
        String oldSQLCnt = "SELECT COUNT(*) FROM loan AS g Inner Join loanphysicalobject AS gp ON g.LoanID = gp.LoanID WHERE g.Category = 1 AND g.LoanNumber = '%s'";

        log.info(newSQL);
        log.info(oldSQL);
        
        try
        {
            Vector<Object> loanNums = BasicSQLUtils.querySingleCol(oldDBConn, "SELECT LoanNumber FROM loan WHERE Category = 1"); // Gifts
            
            for (Object loanNum : loanNums)
            {
                int oldCnt = BasicSQLUtils.getCountAsInt(oldDBConn, String.format(oldSQLCnt, loanNum));
                int newCnt = BasicSQLUtils.getCountAsInt(newDBConn, String.format(newSQLCnt, loanNum));
                
                if (oldCnt != newCnt)
                {
                    String msg = "For Loan Number["+loanNum+" the number of New LoanPreps["+newCnt+"] doesn't match the old["+oldCnt+"]";
                    log.error(msg);
                    tblWriter.logErrors(loanNum.toString(), msg);
                }
                
                getResultSets(String.format(oldSQL, loanNum), String.format(newSQL, loanNum));
                
                
                ResultSetMetaData rmd = newDBRS.getMetaData();
                while (true)
                {
                
                    boolean hasOldRec = oldDBRS.next();
                    boolean hasNewRec = newDBRS.next();
                    
                    if (!hasOldRec && !hasNewRec)
                    {
                        break;
                    }
                    
                    int    newId        = newDBRS.getInt(1);
                    int    oldId        = oldDBRS.getInt(1);
                    
                    String oldNewIdStr = oldId + " / "+newId;// + " ("+oldDBRS.getInt(19)+" / "+newDBRS.getInt(19)+")";
                    
                    int inx = 1;
                    
                    compareStrings(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++; // Gift Number
                    
                    compareDates(oldNewIdStr, inx, inx); inx++;                           // Gift Date
                    
                    compareStrings(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++;  // Remarks
                    
                    compareNumber(oldNewIdStr,  rmd.getColumnName(inx), inx, inx); inx++;  // Number1
                    compareNumber(oldNewIdStr,  rmd.getColumnName(inx), inx, inx); inx++;  // Number2
                    
                    compareStrings(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++;  // Test1
                    compareStrings(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++;  // Test2
                    
                    compareBoolean(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++;  // YesNo1
                    compareBoolean(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++;  // YesNo2
                 
                    compareNumber(oldNewIdStr,  rmd.getColumnName(inx), inx, inx); inx++;  // Qty
                    compareStrings(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++;  // DescOfMat
                    compareStrings(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++;  // OutComm
                    compareStrings(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++;  // InComm
                }
                
                oldDBRS.close();
                newDBRS.close();
            }

        } catch (Exception ex)
        {
            ex.printStackTrace(); 
        }

    }
    

    /**
     * 
     */
    private void verifyLoanRetPreps()
    {
        
        newSQL = "SELECT l.LoanNumber, r.ReturnedDate, r.QuantityResolved, r.Remarks " +
                "FROM loan AS l Inner Join loanpreparation AS lp ON l.LoanID = lp.LoanID " +
                "LEFT Join loanreturnpreparation AS r ON lp.LoanPreparationID = r.LoanPreparationID " +
                "WHERE l.LoanNumber = '%s' ORDER BY lp.LoanPreparationID";

        oldSQL = "SELECT l.LoanNumber, r.`Date`, r.Quantity, r.Remarks " +
        	     "FROM loan AS l Inner Join loanphysicalobject AS lp ON l.LoanID = lp.LoanID " +
        	     "LEFT Join loanreturnphysicalobject AS r ON lp.LoanPhysicalObjectID = r.LoanPhysicalObjectID Left Join loanphysicalobject_LoanPhysicalObjectID AS lr ON lp.LoanPhysicalObjectID = lr.OldID " +
                 "WHERE l.Category = 0 AND LoanNumber = '%s' ORDER BY lr.NewID ASC";
        
        String newSQLCnt = "SELECT COUNT(ID) FROM (SELECT LoanReturnPreparationID AS ID, LoanNumber FROM loan AS l Inner Join loanpreparation AS lp ON l.LoanID = lp.LoanID Left Join loanreturnpreparation AS r ON lp.LoanPreparationID = r.LoanPreparationID WHERE LoanReturnPreparationID IS NOT NULL AND LoanNumber = '%s') T1";
        String oldSQLCnt = "SELECT COUNT(ID) FROM (SELECT LoanReturnPhysicalObjectID AS ID, LoanNumber FROM loan AS l Inner Join loanphysicalobject AS lp ON l.LoanID = lp.LoanID Left Join loanreturnphysicalobject AS r ON lp.LoanPhysicalObjectID = r.LoanPhysicalObjectID WHERE LoanReturnPhysicalObjectID IS NOT NULL AND LoanNumber = '%s') T1";

        log.info(newSQL);
        log.info(oldSQL);
        
        try
        {
            Vector<Object> loanNums = BasicSQLUtils.querySingleCol(oldDBConn, "SELECT LoanNumber FROM loan WHERE Category = 0");
            
            for (Object loanNum : loanNums)
            {
                int oldCnt = BasicSQLUtils.getCountAsInt(oldDBConn, String.format(oldSQLCnt, loanNum));
                int newCnt = BasicSQLUtils.getCountAsInt(newDBConn, String.format(newSQLCnt, loanNum));
                
                if (oldCnt != newCnt)
                {
                    String msg = "For Loan Number["+loanNum+" the number of New LoanPreps["+newCnt+"] doesn't match the old["+oldCnt+"]";
                    log.error(msg);
                    tblWriter.logErrors(loanNum.toString(), msg);
                }
                
                getResultSets(String.format(oldSQL, loanNum), String.format(newSQL, loanNum));
                
                
                ResultSetMetaData rmd = newDBRS.getMetaData();
                while (true)
                {
                
                    boolean hasOldRec = oldDBRS.next();
                    boolean hasNewRec = newDBRS.next();
                    
                    if (!hasOldRec && !hasNewRec)
                    {
                        break;
                    }
                    
                    int    newId        = newDBRS.getInt(1);
                    int    oldId        = oldDBRS.getInt(1);
                    
                    String oldNewIdStr = oldId + " / "+newId;
                    
                    int inx = 1;
                    
                    compareStrings(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++;  // LoanNumber
                    compareDates(oldNewIdStr, inx, inx); inx++;                            // Gift Date
                    compareNumber(oldNewIdStr,  rmd.getColumnName(inx), inx, inx); inx++;  // Quantity
                    compareStrings(oldNewIdStr, rmd.getColumnName(inx), inx, inx); inx++;  // Remarks
                }
                
                oldDBRS.close();
                newDBRS.close();
            }

        } catch (Exception ex)
        {
            ex.printStackTrace(); 
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
        UIRegistry.setAppName("Specify");
        AppBase.processArgs(args);

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
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConvertVerifier.class, e);
                    log.error("Can't change L&F: ", e);
                }
                
                final ConvertVerifier cv = new ConvertVerifier();
                
                if (cv.selectedDBsToConvert())
                {
                	try
                	{
	                	final Pair<String, String> pair = cv.chooseTable();
	                	if (pair != null)
	                	{
	                	    SwingWorker workerThread = new SwingWorker()
	                        {
	                            @Override
	                            public Object construct()
	                            {
	                                try
	                                {
	                                    boolean compareTo6DBS = false;
	                                    if (compareTo6DBS)
	                                    {
	                                        pair.first = "ku_fish";
	                                        pair.second = "kui_fish_dbo_6";
	                                        cv.setCompareTo6DBs(compareTo6DBS);
	                                    }
	                                    
	                                    cv.verifyDB( pair.first, pair.second);
	                                    
	                                } catch (Exception ex)
	                                {
	                                    ex.printStackTrace();
	                                }
	                                return null;
	                            }
	                            
	                            @Override
	                            public void finished()
	                            {
	                            }
	                        };
	                        
	                        // start the background task
	                        workerThread.start();
	                		
	                	} else
	                	{
	                	    JOptionPane.showMessageDialog(null, "The ConvertVerifier was unable to login", "Not Logged In", JOptionPane.ERROR_MESSAGE);
	                	    System.exit(0);
	                	}
                	} catch (Exception ex)
                	{
                		ex.printStackTrace();
                		System.exit(0);
                	}
                }
               
            }
        });

    }
    
    /**
     * Helper method to see if an option is turned on.
     * @param opt the actual option that may be turned on
     * @return true if the opt bit is on
     */
    public static boolean isCOOn(final long opt)
    {
        return (coOptions & opt) == opt;
    }

    public static boolean isACOn(final long opt)
    {
        return (acOptions & opt) == opt;
    }
    
    /**
     * Loads the dialog
     * @param hashNames every other one is the new name
     * @return the list of selected DBs
     */
    private boolean selectedDBsToConvert()
    {
        final JTextField     itUserNameTF = UIHelper.createTextField("root", 15);
        final JPasswordField itPasswordTF = UIHelper.createPasswordField("", 15);
        
        final JTextField     hostNameTF = UIHelper.createTextField("localhost", 15);

        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,2px,p,2px,p,8px,p"));
        
        int y = 1;
        pb.addSeparator("IT User", cc.xyw(1, y, 4)); y += 2;
        pb.add(UIHelper.createLabel("Username:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(itUserNameTF, cc.xy(3, y)); y += 2;

        pb.add(UIHelper.createLabel("Password:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(itPasswordTF, cc.xy(3, y)); y += 2;

        pb.add(UIHelper.createLabel("Host Name:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(hostNameTF, cc.xy(3, y)); y += 2;
        
        PanelBuilder panel = new PanelBuilder(new FormLayout("f:p:g,10px,f:p:g", "f:p:g"));
        panel.add(new JLabel(IconManager.getIcon("SpecifyLargeIcon")), cc.xy(1, 1));
        panel.add(pb.getPanel(), cc.xy(3, 1));

        CustomDialog dlg = new CustomDialog(null, "Database Info", true, panel.getPanel());
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
        
        return true;
    }
    
    /**
     * @return
     * @throws SQLException
     */
    private Pair<String, String> chooseTable() throws SQLException
    {
        MySQLDMBSUserMgr mgr = new MySQLDMBSUserMgr();
        
        final Vector<DBNamePair> availOldPairs = new Vector<DBNamePair>();
        final Vector<DBNamePair> availNewPairs = new Vector<DBNamePair>();
        
        try
        {
            if (mgr.connectToDBMS(itUsrPwd.first, itUsrPwd.second, hostName))
            {
                BasicSQLUtils.setSkipTrackExceptions(true);
                
                Connection conn = mgr.getConnection();
                Vector<Object[]> dbNames = BasicSQLUtils.query(conn, "show databases");
                for (Object[] row : dbNames)
                {
                    System.err.println("Setting ["+row[0].toString()+"] ");
                    conn.setCatalog(row[0].toString());
                    
                    boolean isSp5 = false;
                    boolean isSp6 = false;
                    
                    Vector<Object[]> tables = BasicSQLUtils.query(conn, "show tables");
                    for (Object[] tblRow : tables)
                    {
                        if (row[0].toString().equals("debugdb"))
                        {
                            System.err.println(tblRow[0].toString());
                        }
                    	if (tblRow[0].toString().equals("usysversion"))
                        {
                    		isSp5 = true;
                            break;
                            
                        } else if (tblRow[0].toString().equals("gift"))
                        {
                        	isSp6 = true;
                            break;
                        }
                    }
                    
                    if (isSp5 || isSp6)
                    {
    	            	String collName = null;
    	                Vector<Object[]> tableDesc = BasicSQLUtils.query(conn, "SELECT CollectionName FROM collection");
    	                if (tableDesc.size() > 0)
    	                {
    	                    collName =  tableDesc.get(0)[0].toString();
    	                }
    	                
    	                if (collName == null)
    	                {
    	                	continue;
    	                }
    	
    	                if (isSp5)
    	                {
    	                	availOldPairs.add(new DBNamePair(collName, row[0].toString()));
    	                } else
    	                {
    	                	availNewPairs.add(new DBNamePair(collName, row[0].toString()));
    	                }
                    }
                    
                    System.err.println("isSp5 ["+isSp5+"] isSp6 ["+isSp6+"] ");
                }
                
                Comparator<Pair<String, String>> comparator =  new Comparator<Pair<String, String>>() {
                    @Override
                    public int compare(Pair<String, String> o1, Pair<String, String> o2)
                    {
                        return o1.second.compareTo(o2.second);
                    }
                };
                Collections.sort(availOldPairs, comparator);
                Collections.sort(availNewPairs, comparator);
                
                mgr.close();
                BasicSQLUtils.setSkipTrackExceptions(false);
                
                final JList     oldlist = new JList(availOldPairs);
                final JList     newList = new JList(availNewPairs);
                CellConstraints cc   = new CellConstraints();
                PanelBuilder    pb   = new PanelBuilder(new FormLayout("f:p:g,10px,f:p:g", "p,2px,f:p:g,4px,p"));
                pb.addSeparator("Specify 5 Databases",     cc.xy(1,1));
                pb.add(UIHelper.createScrollPane(oldlist), cc.xy(1,3));
                
                pb.addSeparator("Specify 6 Databases",     cc.xy(3,1));
                pb.add(UIHelper.createScrollPane(newList), cc.xy(3,3));
                
                ArrayList<String> list = new ArrayList<String>(labels.length);
                for (String s : labels)
                {
                    list.add(s);
                }
                chkPanel = new ToggleButtonChooserPanel<String>(list, Type.Checkbox);
                chkPanel.setUseScrollPane(true);
                chkPanel.createUI();
                //pb.add(chkPanel, cc.xyw(1, 5, 3));
                
                /*ActionListener al = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean isSelected = chkPanel.getButtons().get(0).isSelected();
                        int inx = chkPanel.getSelectedIndex();
                        if (inx == 0)
                        {
                            Vector<JToggleButton> btns = chkPanel.getButtons();
                            for (int i=1;i<btns.size();i++)
                            {
                                btns.get(i).setEnabled(!isSelected);
                            }
                        } 
                    }
                };
                
                chkPanel.getButtons().get(0).addActionListener(al);
                chkPanel.getButtons().get(chkPanel.getButtons().size()-1).addActionListener(al);*/
                
                ListSelectionListener oldDBListener = new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            DBNamePair pair = (DBNamePair)oldlist.getSelectedValue();
                            if (pair != null)
                            {
                                int index = 0;
                                for (DBNamePair p : availNewPairs)
                                {
                                    if (p.second.startsWith(pair.second))
                                    {
                                        final int inx = index;
                                        SwingUtilities.invokeLater(new Runnable(){
                                            @Override
                                            public void run()
                                            {
                                                newList.setSelectedIndex(inx);
                                                newList.ensureIndexIsVisible(inx);
                                            }
                                        });
                                    }
                                    index++;
                                }
                            }
                        }
                    }
                };
                
                oldlist.getSelectionModel().addListSelectionListener(oldDBListener);
    
                MouseAdapter ma = new MouseAdapter()
                {
    				@Override
    				public void mouseClicked(MouseEvent e) {
    					super.mouseClicked(e);
    					
    					Vector<JToggleButton> btns = chkPanel.getButtons();
                        if (e.getSource() == btns.get(0))
                        {
                            boolean isSelected = btns.get(0).isSelected();
                            
                            for (int i=1;i<btns.size();i++)
                            {
                                btns.get(i).setEnabled(!isSelected);
                            }
                        } else if (e.getSource() == btns.get(btns.size()-1))
                        {
                            boolean isSelected = btns.get(btns.size()-1).isSelected();
                            for (int i=0;i<btns.size()-1;i++)
                            {
                            	if (i > 0)  btns.get(i).setSelected(!isSelected);
                                btns.get(i).setEnabled(!isSelected);
                            }
                        }
    				}
                };
                chkPanel.getButtons().get(0).addMouseListener(ma);
                chkPanel.getButtons().get(chkPanel.getButtons().size()-1).addMouseListener(ma);
    
                /*ChangeListener cl = new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        Vector<JToggleButton> btns = chkPanel.getButtons();
                        if (e.getSource() == btns.get(0))
                        {
                            boolean isSelected = btns.get(0).isSelected();
                            System.out.println(isSelected);
                            
                            for (int i=1;i<btns.size();i++)
                            {
                                btns.get(i).setEnabled(!isSelected);
                            }
                        } else if (e.getSource() == btns.get(btns.size()-1))
                        {
                            boolean isSelected = btns.get(0).isSelected();
                            System.out.println(isSelected);
                            
                            for (int i=0;i<btns.size()-1;i++)
                            {
                                btns.get(i).setEnabled(!isSelected);
                            }
                        }
                    }
                };
                chkPanel.getButtons().get(0).addChangeListener(cl);
                chkPanel.getButtons().get(chkPanel.getButtons().size()-1).addChangeListener(cl);*/
                
                pb.setDefaultDialogBorder();
                
                final CustomDialog dlg = new CustomDialog(null, "Select a DB to Verify", true, pb.getPanel());
                
                ListSelectionListener lsl = new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            dlg.getOkBtn().setEnabled(oldlist.getSelectedIndex() > -1);
                        }
                    }
                };
                oldlist.addListSelectionListener(lsl);
                newList.addListSelectionListener(lsl);
                
                oldlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                newList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                
                MouseAdapter listMA = new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        if (e.getClickCount() == 2)
                        {
                            dlg.getOkBtn().setEnabled(oldlist.getSelectedIndex() > -1 && newList.getSelectedIndex() > -1);
                            dlg.getOkBtn().doClick();
                        }
                    }
                };
                oldlist.addMouseListener(listMA);
                newList.addMouseListener(listMA);
                
                dlg.createUI();
                dlg.pack();
                //dlg.setSize(300, 800);
                dlg.pack();
                dlg.setVisible(true);
                if (dlg.isCancelled())
                {
                    return null;
                }
                
                DBNamePair oldPair = (DBNamePair)oldlist.getSelectedValue();
                namePairToConvert = (DBNamePair)newList.getSelectedValue();
                namePairToConvert.first = oldPair.second;
                return namePairToConvert;
            }
        } catch (Exception ex)
        {
            
        }
        return null;
    }

    /**
     * @return
     */
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
