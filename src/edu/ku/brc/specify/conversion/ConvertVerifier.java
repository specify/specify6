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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
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
import java.util.Hashtable;
import java.util.List;
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

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.tasks.StatsTrackerTask;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.MySQLDMBSUserMgr;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.ToggleButtonChooserPanel.Type;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

public class ConvertVerifier
{
    protected static final Logger log = Logger.getLogger(ConvertVerifier.class);
    
    protected enum StatusType {NO_OLD_REC, NO_NEW_REC, OLD_VAL_NULL, NEW_VAL_NULL, BAD_COMPARE, BAD_DATE, COMPARE_OK, NO_COMPARE, }
    
    protected Pair<String, String> itUsrPwd          = new Pair<String, String>(null, null);
    protected String               hostName          = "localhost";
    protected Pair<String, String> namePairToConvert = null;
    
    // These are the configuration Options for a View
    public static final long NO_OPTIONS             =    0; // Indicates there are no options
    public static final long DO_CO_PREPARATION      =    1; 
    public static final long DO_CO_CE               =    2; 
    public static final long DO_CO_LOCALITY         =    4; 
    public static final long DO_CO_PREPARER         =    8; 
    public static final long DO_CO_CATLOGER         =   16; 
    public static final long DO_CO_DETERMINER       =   32; 
    public static final long DO_CO_TAXON            =   64; 
    public static final long DO_CO_GEO              =  128; 
    public static final long DO_COLLECTORS          =  256; 
    public static final long DO_COLLEVENTS          =  512; 
    public static final long DO_CO_ALL              = 1023; 
    
    protected String[] labels = {"None", "Preparations", "Collecting Events", "Localities", "Preparers", 
                                 "Catalogers", "Determiners", "Taxon", "Geographies", "Collectors", 
                                 "Collecting Events", "All"};
    protected ToggleButtonChooserPanel<String> chkPanel;
    
    //public static final long DONT_ADD_ALL_ALTVIEWS  = 256; 
    //public static final long USE_ONLY_CREATION_MODE = 512;
    
    public static final long DO_ACCESSIONS          =   1; 
    public static final long DO_AC_AUTHS            =   2; 
    public static final long DO_AC_AGENTS           =   4; 
    public static final long DO_AC_ALL              =   7; 

    protected static long                                   coOptions         = NO_OPTIONS;
    protected static long                                   acOptions         = NO_OPTIONS;
    protected static List<String>                           dbNamesToConvert  = null;
    protected static int                                    currentIndex      = 0;
    protected static Hashtable<String, String>              old2NewDBNames    = null;
    
    protected Hashtable<String, Integer>                    catNumsInErrHash  = new Hashtable<String, Integer>();
    
    
    protected String                                        oldDriver         = "";
    protected String                                        oldDBName         = "";
    protected String                                        oldUserName       = "rods";
    protected String                                        oldPassword       = "rods";

    protected IdMapperMgr                                   idMapperMgr;

    protected Connection                                    oldDBConn;
    protected Connection                                    newDBConn;

    protected Statement                                     oldDBStmt;
    protected Statement                                     newDBStmt;

    protected ResultSet                                     oldDBRS;
    protected ResultSet                                     newDBRS;
    
    protected String                                        newSQL;
    protected String                                        oldSQL;
    
    protected PrintWriter                                   out;
    protected int                                           numErrors = 0;
    protected static SimpleDateFormat                       dateFormatter          = new SimpleDateFormat("yyyy-MM-dd");
    protected boolean                                       debug = false;
    protected static ProgressFrame                          progressFrame;
    
    
    /**
     * 
     */
    public ConvertVerifier()
    {
        super();
        
        UIRegistry.setAppName("Specify");
    }

    /**
     * @param databaseNameSource
     * @param databaseNameDest
     * @throws Exception
     */
    public void verifyDB(final String databaseNameSource, 
                         final String databaseNameDest) throws Exception
    {
        
        File verifyFile = new File("conversions" + File.separator + databaseNameDest + File.separator + "verify.html");
        out = new PrintWriter(verifyFile);
        
        String title = "From "+databaseNameSource+" to "+databaseNameDest;
        System.out.println("************************************************************");
        System.out.println("From "+databaseNameSource+" to "+databaseNameDest);
        System.out.println("************************************************************");
        
        out.println("<html><head>");
        writeStyle(out);
        out.println("<title>"+databaseNameDest+"</title></head><body>");
        out.println("<h2>"+title+"</h2>");

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
        oldDBConn = oldDB.createConnection();
        if (oldDBConn == null)
        {
            throw new RuntimeException(oldDB.getErrorMsg());
        }
        newDBConn = DBConnection.getInstance().createConnection();
        newDBStmt = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        oldDBStmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        
        String[] tableNames = {"CollectingEvent", "CollectingEvent", "Locality", "Locality"};
        for (int i=0;i<tableNames.length;i+=2)
        {
            verifyTableCounts(tableNames[i].toLowerCase(), tableNames[i+1].toLowerCase());
        }
        
        progressFrame = new ProgressFrame("Checking Catalog Objects....");
        progressFrame.adjustProgressFrame();
        
        Integer total = BasicSQLUtils.getCount(oldDBConn, "SELECT COUNT(CatalogNumber) FROM collectionobjectcatalog WHERE CollectionObjectTypeID < 20 ORDER BY CatalogNumber ASC");
        progressFrame.setProcess(0, total);
        //progressFrame.setDesc("Checking Catalog Objects....");
        
        progressFrame.setOverall(0, total*4);
        progressFrame.setOverall(0);
        progressFrame.setDesc("");

        UIHelper.centerAndShow(progressFrame);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                UIHelper.centerAndShow(progressFrame);
            }
        });
        
        out.println("<H3>Collection Objects</H3>");
        out.println("<table class=\"o\" cellspacing=\"0\" border=\"0\">");
        
        coOptions = DO_CO_ALL;
        acOptions = DO_AC_ALL;

        if (coOptions > NO_OPTIONS)
        {
            int i = 0;
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT CatalogNumber FROM collectionobjectcatalog WHERE CollectionObjectTypeID < 20 ORDER BY CatalogNumber ASC");
            while (rs.next())
            {
                int    oldCatNum = rs.getInt(1);
                String newCatNum = convertCatNum(oldCatNum);
                
                if (isCOOn(DO_CO_DETERMINER) && !verifyDeterminer(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Determiner");
                    catNumsInErrHash.put(newCatNum, oldCatNum);
                }            
                if (isCOOn(DO_CO_CATLOGER) && !verifyCataloger(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Cataloger");
                    catNumsInErrHash.put(newCatNum, oldCatNum);
                }
                if (isCOOn(DO_CO_GEO) && !verifyGeography(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Geography");
                    catNumsInErrHash.put(newCatNum, oldCatNum);
                }
                if (isCOOn(DO_CO_CE) && !verifyCollectingEvent(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Collecting Event");
                    catNumsInErrHash.put(newCatNum, oldCatNum);
                }
                if (isCOOn(DO_CO_TAXON) && !verifyTaxon(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Taxon");
                    catNumsInErrHash.put(newCatNum, oldCatNum);
                }
                if (isCOOn(DO_CO_LOCALITY) && !verifyCOToLocality(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Locality");
                    catNumsInErrHash.put(newCatNum, oldCatNum);
                }
                if (isCOOn(DO_CO_PREPARATION) && !verifyPreparation(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Preparations");
                    catNumsInErrHash.put(newCatNum, oldCatNum);
                }
                if (isCOOn(DO_CO_PREPARER) && !verifyPreparer(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Preparer");
                    catNumsInErrHash.put(newCatNum, oldCatNum);
                }
                
                if ((i % 100) == 0)
                {
                    System.out.println(i+"  "+oldCatNum);
                    progressFrame.setProcess(i);
                    progressFrame.setOverall(i);
                }
                i++;
            }
            
            rs.close();
            stmt.close();
        }
        
        progressFrame.setProcess(total);
        
        if (isCOOn(DO_COLLECTORS))
        {
            verifyCollectors();
        }
        progressFrame.setOverall(total*2);
        if (isCOOn(DO_COLLEVENTS))
        {
            verifyCEs();
        }
        progressFrame.setOverall(total*3);
        
        printTotal();
        out.println("</table>");
        
        out.println("<BR>");
        out.println("Catalog Summary:<BR>");
        Vector<String> catNumList = new Vector<String>(catNumsInErrHash.keySet());
        Collections.sort(catNumList);
        for (String catNum : catNumList)
        {
            out.println(catNum+"<BR>");
        }
        out.println("<BR>");
        
        out.println("<H3>Accessions</H3>");
        out.println("<table class=\"o\" cellspacing=\"0\" border=\"0\">");
        
        if (acOptions > NO_OPTIONS)
        {
            int i = 0;
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT Number FROM accession ORDER BY Number ASC");
            while (rs.next())
            {
                String oldAccNum = rs.getString(1);
                String newAccNum = oldAccNum;
                
                if (isACOn(DO_ACCESSIONS) && !verifyAccessions(oldAccNum, newAccNum))
                {
                    printVerifyHeader("Accessions");
                    log.error("Accession Num: "+oldAccNum);
                    //log.error("New SQL: "+newSQL);
                    //log.error("Old SQL: "+oldSQL);
                    //break;
                }            
                
                if (isACOn(DO_AC_AGENTS) && !verifyAccessionAgents(oldAccNum, newAccNum))
                {
                    printVerifyHeader("Accession Agents");
                    log.error("Accession Num: "+oldAccNum);
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
        progressFrame.setOverall(total*4);
        newDBConn.close();
        oldDBConn.close();
        
        printTotal();
        out.println("</table>");
        out.println("</body>");
        
        log.info("Done.");
        
        out.flush();
        out.close();
        
        progressFrame.setVisible(false);
        
        AttachmentUtils.openURI(verifyFile.toURI());
        
        System.exit(0);
    }
    
    /**
     * @param out
     */
    protected void writeStyle(final PrintWriter out)
    {
        out.println("<style>");
        out.println(" table.o { border-top: solid 1px rgb(128, 128, 128); border-left: solid 1px rgb(128, 128, 128); }");
        out.println(" table.o td { border-bottom: solid 1px rgb(128, 128, 128); border-right: solid 1px rgb(128, 128, 128); }");
        out.println(" table.i { border-top: solid 1px rgb(192, 192, 192); border-left: solid 1px rgb(192, 192, 192); }");
        out.println(" table.i td { border-bottom: solid 1px rgb(192, 192, 192); border-right: solid 1px rgb(192, 192, 192); }");
        out.println("</style>");
    }
    
    /**
     * @param name
     */
    protected void printVerifyHeader(final String name)
    {
        out.println("<tr><td colspan=\"2\">"+name+"</td></tr>");
    }
    
    /**
     * @param oldCatNum
     * @param newCatNum
     * @throws SQLException
     */
    protected boolean verifyTaxon(final int oldCatNum, final String newCatNum) throws SQLException
    {
        newSQL = "SELECT collectionobject.CatalogedDate, collectionobject.CatalogedDatePrecision, determination.DeterminedDate, determination.DeterminedDatePrecision, taxon.FullName " + 
                        "FROM determination LEFT JOIN collectionobject ON determination.CollectionObjectID = collectionobject.CollectionObjectID "+
                        "LEFT JOIN taxon ON determination.TaxonID = taxon.TaxonID WHERE CatalogNumber = '"+ newCatNum + "'";

        oldSQL = "SELECT collectionobjectcatalog.CatalogedDate, determination.Date,taxonname.FullTaxonName " + 
                        "FROM determination LEFT JOIN taxonname ON determination.TaxonNameID = taxonname.TaxonNameID " + 
                        "LEFT JOIN collectionobjectcatalog ON collectionobjectcatalog.CollectionObjectCatalogID = determination.BiologicalObjectID " + 
                        "WHERE CatalogNumber = " + oldCatNum;
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
     * @param status
     */
    protected void dumpStatus(final StatusType status)
    {
        switch (status)
        {
            case OLD_VAL_NULL:
                out.print("<tr><td colspan=\"2\">");
                out.println(dumpSQL(newDBConn, newSQL));
                out.println(dumpSQL(oldDBConn, oldSQL));
                out.println("<BR>");
                out.println("</td></tr><!-- 4 -->");
                out.flush();
                break;
                
            case NO_OLD_REC:
                out.print("<tr><td colspan=\"2\">");
                out.println(dumpSQL(newDBConn, newSQL));
                out.println("<BR>");
                out.println("</td></tr><!-- 4 -->");
                out.flush();
                break;
                
            case NEW_VAL_NULL:
                out.print("<tr><td colspan=\"2\">");
                out.println(dumpSQL(newDBConn, newSQL));
                out.println(dumpSQL(oldDBConn, oldSQL));
                out.println("<BR>");
                out.println("</td></tr><!-- 4 -->");
                out.flush();
                break;
                
            case NO_NEW_REC:
                out.print("<tr><td colspan=\"2\">");
                out.println(dumpSQL(oldDBConn, oldSQL));
                out.println("<BR>");
                out.println("</td></tr><!-- 4 -->");
                out.flush();
                break;
        }

    }
                         
    /**
     * @param oldCatNum
     * @param newCatNum
     * @return
     * @throws SQLException
     */
    protected boolean verifyGeography(final int oldCatNum, final String newCatNum) throws SQLException
    {
        newSQL = "SELECT geography.Name " +
            "FROM collectionobject INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
            "INNER JOIN locality ON collectingevent.LocalityID = locality.LocalityID " +
            "INNER JOIN geography ON locality.GeographyID = geography.GeographyID " +
            "WHERE CatalogNumber = '"+ newCatNum + "'";

        oldSQL = "SELECT geography.GeographyID, geography.ContinentOrOcean, geography.Country, geography.State, geography.County " +
            "FROM collectionobjectcatalog INNER JOIN collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID " +
            "INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
            "INNER JOIN locality ON collectingevent.LocalityID = locality.LocalityID " +
            "INNER JOIN geography ON locality.GeographyID = geography.GeographyID " +
            "WHERE CatalogNumber = " + oldCatNum;
        
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
            
            String newGeoName = newDBRS.getString(1);
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
            log.error("Couldn't find new Geo Name["+newGeoName+"]");
            for (int i=names.length-1;i>=0;i--)
            {
                log.error("  ["+names[i]+"]");
            }
            return false;
            
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
    protected boolean verifyTableCounts(final String newTableName, final String oldTableName)
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

    protected boolean verifyCOToLocality(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT locality.LocalityName " +
                        "FROM collectionobject INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
                        "INNER JOIN locality ON collectingevent.LocalityID = locality.LocalityID " +
                        "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT locality.LocalityName  " +
                        "FROM collectionobjectcatalog INNER JOIN collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID " +
                        "INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
                        "INNER JOIN locality ON collectingevent.LocalityID = locality.LocalityID " +
                        "WHERE CatalogNumber = " + oldCatNum;
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
    protected boolean verifyCataloger(final int oldCatNum, final String newCatNum) throws SQLException
    {
        //log.debug("New SQL: "+newSQL);
        //log.debug("Old SQL: "+oldSQL);
        
        // address.Address, 
         newSQL = "SELECT agent.FirstName, agent.MiddleInitial, agent.LastName " +
                  "FROM collectionobject INNER JOIN agent ON collectionobject.CatalogerID = agent.AgentID " +
                  "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT agent.FirstName, agent.MiddleInitial, agent.LastName, agent.Name  " +
                  "FROM collectionobjectcatalog INNER JOIN agent ON collectionobjectcatalog.CatalogerID = agent.AgentID WHERE CatalogNumber = " + oldCatNum;
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
    protected boolean verifyDeterminer(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT agent.FirstName, agent.MiddleInitial, agent.LastName " +
                  "FROM collectionobject INNER JOIN determination ON collectionobject.CollectionObjectID = determination.CollectionObjectID " +
                  "INNER JOIN agent ON determination.DeterminerID = agent.AgentID " +
                  "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT agent.FirstName, agent.MiddleInitial, agent.LastName, agent.Name  " +
                  "FROM collectionobjectcatalog INNER JOIN collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID " +
                  "INNER JOIN determination ON determination.BiologicalObjectID = collectionobject.CollectionObjectID " + 
                  "INNER JOIN agent ON determination.DeterminerID = agent.AgentID WHERE CatalogNumber = " + oldCatNum;
        
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
    protected boolean verifyPreparer(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT agent.FirstName, agent.MiddleInitial, agent.LastName " +
                  "FROM collectionobject INNER JOIN preparation ON collectionobject.CollectionObjectID = preparation.CollectionObjectID INNER JOIN agent ON preparation.PreparedByID = agent.AgentID " +
                  "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT agent.FirstName, agent.MiddleInitial, agent.LastName, agent.Name  " +
                  "FROM collectionobjectcatalog INNER JOIN collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.DerivedFromID " +
                  "INNER JOIN preparation ON collectionobject.CollectionObjectID = preparation.PhysicalObjectTypeID " +
                  "INNER JOIN agent ON preparation.PreparedByID = agent.AgentID " +
                  "WHERE CatalogNumber = " + oldCatNum;
        
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
    protected boolean verifyAllLocalityToGeo() throws SQLException
    {
        newSQL = "SELECT locality.LocalityID, geography.GeographyID, geography.Name " +
                 "FROM locality " +
                 "INNER JOIN geography ON locality.GeographyID = geography.GeographyID ";

        oldSQL = "SELECT locality.LocalityID, geography.GeographyID, geography.GeographyID, geography.ContinentOrOcean, geography.Country, geography.State, geography.County " +
                 "FROM locality " +
                 "INNER JOIN geography ON locality.GeographyID = geography.GeographyID ";
    
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
    protected boolean verifyCollectingEvent(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT collectingevent.StartDate, collectingevent.StartDatePrecision, collectingevent.StationFieldNumber " +
                        "FROM collectionobject INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
                        "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT collectingevent.StartDate, collectingevent.StationFieldNumber  " +
                        "FROM collectionobjectcatalog INNER JOIN collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID " +
                        "INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
                        "WHERE CatalogNumber = " + oldCatNum;
        
         StatusType status = compareRecords("CE To Locality", oldCatNum, newCatNum, oldSQL, newSQL);
         dumpStatus(status);
         return status == StatusType.COMPARE_OK;
    }
                         
    /**
     * @param oldCatNum
     * @param newCatNum
     * @return
     * @throws SQLException
     */
    protected boolean verifyPreparation(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT preparation.CountAmt, " +
                    "preptype.Name, " +
                    "preparation.Text1, " +
                    "preparation.Text2 " +
                    "FROM collectionobject INNER JOIN preparation ON collectionobject.CollectionObjectID = preparation.CollectionObjectID " +
                    "INNER JOIN preptype ON preparation.PrepTypeID = preptype.PrepTypeID " +
                    "WHERE CatalogNumber = '"+ newCatNum + "' ORDER BY PreparationID";

         oldSQL = "SELECT co.Count, co.PreparationMethod, co.Text1, co.Text2 FROM collectionobject co " +
                  "INNER JOIN collectionobjectcatalog cc ON co.DerivedFromID = cc.CollectionObjectCatalogID " + 
                  "WHERE co.CollectionObjectTypeID > 20 AND CatalogNumber = " + oldCatNum + " ORDER BY co.CollectionObjectID";
        
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
    protected boolean verifyAccessions(final String oldAccNum, final String newAccNum) throws SQLException
    {
         newSQL = "SELECT AccessionNumber, Status, Type, VerbatimDate, DateAccessioned, DateReceived, Number1, Number2, YesNo1, YesNo2 FROM accession  " +
                  "WHERE AccessionNumber = '"+ newAccNum + "'";

         oldSQL = "SELECT Number, Status, Type, VerbatimDate, DateAccessioned, DateReceived, Number1, Number2, YesNo1, YesNo2 FROM accession " +
                  "WHERE Number = '" + oldAccNum + "'";
        
         StatusType status = compareRecords("Accession", oldAccNum, newAccNum, oldSQL, newSQL);
         dumpStatus(status);
         return status == StatusType.COMPARE_OK;
    }
    
    /**
     * @param oldAccNum
     * @param newAccNum
     * @return
     * @throws SQLException
     */
    protected boolean verifyAccessionAgents(final String oldAccNum, final String newAccNum) throws SQLException
    {
        newSQL = "SELECT accessionagent.Role, agent.FirstName, agent.MiddleInitial, agent.LastName " +
                 "FROM accession INNER JOIN accessionagent ON accession.AccessionID = accessionagent.AccessionID "+
                 "INNER JOIN agent ON accessionagent.AgentID = agent.AgentID  " +
                 "WHERE AccessionNumber = '" + newAccNum + "' ORDER BY agent.LastName";

        oldSQL = "SELECT accessionagents.Role, agent.FirstName, agent.MiddleInitial, agent.LastName, agent.Name " +
                 "FROM accession INNER JOIN accessionagents ON accession.AccessionID = accessionagents.AccessionID " +
                 "INNER JOIN agentaddress ON accessionagents.AgentAddressID = agentaddress.AgentAddressID " +
                 "INNER JOIN agent ON agentaddress.AgentID = agent.AgentID " +
                 "WHERE Number = '" + oldAccNum + "' ORDER BY agent.LastName";

        StatusType status = compareRecords("Accession", oldAccNum, newAccNum, oldSQL, newSQL);
        dumpStatus(status);
        return status == StatusType.COMPARE_OK;
    }


                         
    /**
     * @param oldSQL
     * @param newSQL
     * @throws SQLException
     */
    protected void getResultSets(final String oldSQLArg, final String newSQLArg)  throws SQLException
    {
        newDBRS   = newDBStmt.executeQuery(newSQLArg);  
        oldDBRS   = oldDBStmt.executeQuery(oldSQLArg);  
    }
    
    /**
     * @throws SQLException
     */
    protected void doneWithRS() throws SQLException
    {
        newDBRS.close();
        oldDBRS.close();
    }
    
    /**
     * @param oldCatNum
     * @return
     */
    protected String convertCatNum(final int oldCatNum)
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
    protected StatusType compareRecords(final String desc, 
                                        final int    oldCatNum, 
                                        final String newCatNum, 
                                        final String oldSQLArg, 
                                        final String newSQLArg) throws SQLException
    {
        return compareRecords(desc, Integer.toString(oldCatNum), newCatNum, oldSQLArg, newSQLArg);
    }
    
    /**
     * @param msg
     */
    protected void printRowError(final String msg)
    {
        out.println("<!-- 1s --> <tr><td>&nbsp;</td><td>"+msg+"</td></tr> <!-- 1e -->");
        numErrors++;
    }
    
    /**
     * @param msg
     */
    protected void printRowError(final String tbl, final String msg)
    {
        out.println("<!-- 2s --><tr><td>" + (tbl != null ? tbl : "&nbsp;") + "</td><td>"+msg+"</td></tr> <!-- 2e -->");
        numErrors++;
    }
    
    /**
     * 
     */
    protected void printTotal()
    {
        out.println("<tr><td>Total Errors</td><td>"+numErrors+"</td></tr> <!-- 3 -->");
        numErrors = 0;
    }
    
    /**
     * @param oldSQL
     * @param newSQL
     * @return
     * @throws SQLException
     */
    protected StatusType compareRecords(final String desc, 
                                        final String oldCatNum, 
                                        final String newCatNum, 
                                        final String oldSQLArg, 
                                        final String newSQLArg) throws SQLException
    {
        boolean dbg = false;
        
        getResultSets(oldSQLArg, newSQLArg);
        if (dbg)
        {
            System.out.println(oldSQLArg);
            System.out.println(newSQLArg);
        }
        
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
                log.error(desc+ " - No Old Record for ["+oldCatNum+"]");
                printRowError(desc, "No Old Record for ["+oldCatNum+"]");
                return StatusType.NO_OLD_REC;
            }
            if (!hasNewRec)
            {
                log.error(desc+ " - No New Record for ["+newCatNum+"]");
                printRowError(desc, "No New Record for ["+newCatNum+"]");
                return StatusType.NO_NEW_REC;
            }
            
            boolean checkForAgent = newSQL.indexOf("agent.LastName") > -1;
            
            ResultSetMetaData oldRsmd = oldDBRS.getMetaData();
            ResultSetMetaData newRsmd = newDBRS.getMetaData();
            
            Pair<String, String> datePair = new Pair<String, String>();
            Calendar             cal      = Calendar.getInstance();
            StringBuilder        errSB    = new StringBuilder();
            
            while (hasNewRec && hasOldRec)
            {
                errSB.setLength(0);
                
                int oldColInx = 0;
                int newColInx = 0;
                
                int numCols = newRsmd.getColumnCount();
                
                for (int col=0;col<numCols;col++)
                {
                    newColInx++;
                    oldColInx++;

                    if (dbg)
                    {
                        System.out.println("col       "+col+" / "+oldRsmd.getColumnCount());
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
                        }
                        continue;
                    }
                    
                    String oldColName = oldRsmd.getColumnName(oldColInx);
                    if (oldColName.equals("PreparationMethod"))
                    {
                        if ((oldObj == null && newObj != null && !newObj.equals("Misc")) || 
                            (oldObj != null && newObj != null && !newObj.equals(oldObj)))
                        {
                            String msg = "Old Value was null and shouldn't have been for Old CatNum ["+oldCatNum+"] Field ["+oldColName+"] oldObj["+oldObj+"] newObj ["+newObj+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(desc, msg);
                            return StatusType.OLD_VAL_NULL;
                        }
                        continue;
                    }
                    
                    if (oldObj == null && !StringUtils.contains(oldColName, "LastName"))
                    {
                        if (!oldColName.equals("PreparationMethod") || !newObj.equals("Misc"))
                        {
                            String msg = "Old Value was null and shouldn't have been for Old CatNum ["+oldCatNum+"] Field ["+oldColName+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(desc, msg);
                            return StatusType.OLD_VAL_NULL;
                        }
                    }
                    
                    if (newObj == null)
                    {
                        String clsName = newRsmd.getColumnClassName(newColInx);
                        String colName = newRsmd.getColumnName(newColInx);

                        if (!clsName.equals("java.sql.Date") || (!(oldObj instanceof String) && ((Number)oldObj).intValue() != 0))
                        {
                            String msg = "New Value was null and shouldn't have been for Key Value CatNo["+newCatNum+"] Field ["+colName+"] ["+oldObj+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(desc, msg);
                            return StatusType.NEW_VAL_NULL;
                        }
                        
                        if (StringUtils.contains(colName, "Date") && StringUtils.contains(newRsmd.getColumnName(newColInx+1), "DatePrecision"))
                        {
                            newColInx++;
                            numCols--;
                        }
                        continue;
                    }
                    
                    //System.out.println(newObj.getClass().getName()+"  "+oldObj.getClass().getName());
                    
                    if (newObj instanceof java.sql.Date)
                    {
                        boolean isPartialDate = false;
                        Byte partialDateType = null;
                        if (StringUtils.contains(newRsmd.getColumnName(newColInx+1), "DatePrecision"))
                        {
                            newColInx++;
                            numCols--;
                            partialDateType =  newDBRS.getByte(newColInx);
                            isPartialDate   = true;
                        }
                        
                        int oldIntDate = oldDBRS.getInt(oldColInx);
                        if (oldIntDate == 0)
                        {
                            continue;
                        }
                        
                        BasicSQLUtils.getPartialDate(oldIntDate, datePair, false);
                        
                        if (partialDateType != null)
                        {
                            if (Byte.parseByte(datePair.second) != partialDateType.byteValue())
                            {
                                errSB.append("Partial Dates Type do not match. Old["+datePair.second+"]  New ["+partialDateType.byteValue()+"]");
                                // error partial dates don't match
                            }
                        } 
                        
                        cal.setTime((Date)newObj);
                        
                        int year = Integer.parseInt(datePair.first.substring(0, 4));
                        int mon  = Integer.parseInt(datePair.first.substring(5, 7));
                        int day  = Integer.parseInt(datePair.first.substring(8, 10));
                        
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
                            printRowError(oldCatNum + " / "+newCatNum, errSB.toString());
                            return StatusType.BAD_DATE;
                        }
                        
                    } else if (newObj instanceof Float || newObj instanceof Double)
                    {
                        String s1 = String.format("%10.5f", newObj instanceof Float ? (Float)newObj : (Double)newObj);
                        String s2 = String.format("%10.5f", oldObj instanceof Float ? (Float)oldObj : (Double)oldObj);
                        if (!s1.equals(s2))
                        {
                            String msg = "Columns don't compare["+s1+"]["+s2+"]  ["+newRsmd.getColumnName(col)+"]["+oldRsmd.getColumnName(oldColInx)+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(desc, msg);
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
                            if (!newLastName.equals(lastName) &&!newLastName.equals(agentName))
                            {
                                String msg = "Columns don't compare["+newObj+"]["+oldObj+"]  ["+newColName+"]["+oldColName+"]";
                                log.error(desc+ " - "+msg);
                                printRowError(desc, msg);
                                return StatusType.NO_COMPARE;
                            }
                            
                        } else if (!newObj.equals(oldObj))
                        {
                            String msg = "Columns don't Cat Num["+oldCatNum+"] compare["+newObj+"]["+oldObj+"]  ["+newColName+"]["+oldColName+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(desc, msg);
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
                    log.error(desc+ " - No Old Record for ["+oldCatNum+"]");
                    printRowError("No Old Record for ["+oldCatNum+"]");
                    return StatusType.NO_OLD_REC;
                }
                if (!hasNewRec)
                {
                    log.error(desc+ "No New Record for ["+newCatNum+"]");
                    printRowError("No New Record for ["+newCatNum+"]");
                    return StatusType.NO_NEW_REC;
                }
            }
        } finally
        {
            doneWithRS();
        }
            
        return StatusType.COMPARE_OK;
    }
    
    protected void verifyCollectors()
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	boolean dbg = false;
    	
         oldSQL = "SELECT collectingevent.CollectingEventID, collectingevent.StartDate, agent.FirstName,  agent.LastName, agent.Name, collectors.Order  " + 
         "FROM collectingevent INNER JOIN collectors ON collectingevent.CollectingEventID = collectors.CollectingEventID " + 
         "INNER JOIN agent ON collectors.AgentID = agent.AgentID ORDER BY collectingevent.CollectingEventID, collectors.Order";
    	
    	 newSQL = "SELECT collectingevent.CollectingEventID, collectingevent.StartDate, agent.FirstName, agent.LastName, collector.OrderNumber   " + 
    	 "FROM collectingevent INNER JOIN collector ON collectingevent.CollectingEventID = collector.CollectingEventID  " + 
    	 "INNER JOIN agent ON collector.AgentID = agent.AgentID ORDER BY collectingevent.CollectingEventID, collector.OrderNumber";
    	
    	int prevOldId = Integer.MAX_VALUE;
    	int prevNewId = Integer.MAX_VALUE;
    	
    	try
        {
	        getResultSets(oldSQL, newSQL);
	        
	        if (dbg)
	        {
	            System.out.println(oldSQL);
	            System.out.println(newSQL);
	        }
	        
	        while (true)
	        {
	        
	            boolean hasOldRec = oldDBRS.next();
	            boolean hasNewRec = newDBRS.next();
	            
	            if (!hasOldRec && !hasNewRec)
	            {
	                break;
	            }
	            
	            int    newId        = newDBRS.getInt(1);
	            String newStartDate = newDBRS.getString(2);
	            String newFirstName = newDBRS.getString(3);
	            String newLastName  = newDBRS.getString(4);
	            int    newOrder     = newDBRS.getInt(5);
	            
	            int    oldId        = oldDBRS.getInt(1);
	            String oldStartDate = oldDBRS.getString(2);
	            String oldFirstName = oldDBRS.getString(3);
	            String oldLastName  = oldDBRS.getString(4);
	            String oldName      = oldDBRS.getString(5);
	            int    oldOrder     = oldDBRS.getInt(6);

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
                    printRowError(msg);
                    break;
	            }
	            
	            if (oldFirstName == null && oldLastName == null && oldName != null)
	            {
	            	oldLastName = oldName;
	            }
	            
            	Object dateObj = oldDBRS.getObject(2);
            	if (dateObj != null && ((Integer)dateObj) == 0)
            	{
            		oldStartDate = null;
            	}
            	
	            // Date
	            if (oldStartDate == null && newStartDate != null)
	            {
	            	String msg = "CE["+oldId + " / "  + newId+"]  Old Date["+oldStartDate+"] is NULL   New Date["+newStartDate+"] is not";
	            	log.error(msg);
                    printRowError(msg);
	            }
	            if (oldStartDate != null && newStartDate == null)
	            {
	            	String msg = "Old Date["+oldStartDate+"] is not null   New Date["+newStartDate+"] is NULL";
	            	log.error(msg);
                    printRowError(msg);
	            }
	            if (oldStartDate != null && newStartDate != null)
	            {
            		Date date = UIHelper.convertIntToDate(oldDBRS.getInt(2));
            		
            		oldStartDate = sdf.format(date);
	            	if (!oldStartDate.equals(newStartDate))
	            	{
		            	String msg = "CE["+oldId + " / "  + newId+"]  Old Date["+oldStartDate+"] is NOT equals   New Date["+newStartDate+"]";
		            	log.error(msg);
	                    printRowError(msg);
	            	}
	            }
	            
	            // First Name
	            if (oldFirstName == null && newFirstName != null)
	            {
	            	String msg = "CE["+oldId + " / "  + newId+"]  Old FirstName["+oldFirstName+"] is NULL   New FirstName["+newFirstName+"] is not";
	            	log.error(msg);
                    printRowError(msg);
	            }
	            if (oldFirstName != null && newFirstName == null)
	            {
	            	String msg = "CE["+oldId + " / "  + newId+"]  Old FirstName["+oldFirstName+"] is not null   New FirstName["+newFirstName+"] is NULL";
	            	log.error(msg);
                    printRowError(msg);
	            }
	            if (oldFirstName != null && newFirstName != null && !oldFirstName.equals(newFirstName))
	            {
	            	String msg = "CE["+oldId + " / "  + newId+"]  Old FirstName["+oldFirstName+"] is NOT equals   New FirstName["+newFirstName+"]";
	            	log.error(msg);
                    printRowError(msg);
	            }
	            
	            // Last Name and old Name
	            if (oldLastName == null && newLastName != null)
	            {
	            	String msg = "CE["+oldId + " / "  + newId+"]  Old LastName["+oldLastName+"] is NULL   New LastName["+newLastName+"] is not";
	            	log.error(msg);
                    printRowError(msg);

	            }
	            if (oldLastName != null && newLastName == null)
	            {
	            	String msg = "CE["+oldId + " / "  + newId+"]  Old LastName["+oldLastName+"] is not null   New LastName["+newLastName+"] is NULL";
	            	log.error(msg);
                    printRowError(msg);

	            }
	            if (oldLastName != null && newLastName != null && !oldLastName.equals(newLastName))
	            {
	            	String msg = "CE["+oldId + " / "  + newId+"]  Old LastName["+oldLastName+"] is NOT equals   New LastName["+newLastName+"]";
	            	log.error(msg);
                    printRowError(msg);
	            }
	            
	            // Order
	            if (oldOrder != newOrder)
	            {
	            	String msg = "CE["+oldId + " / "  + newId+"]  Old Order["+oldOrder+"] is not equal ["+newOrder+"]";
	            	log.error(msg);
                    printRowError(msg);
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
    protected void verifyCEs()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        newSQL = "SELECT c.CollectingEventID, c.StartDate, c.StartTime, l.LocalityName, l.Latitude1, l.Longitude1, g.Name " + 
                        "FROM collectingevent c LEFT JOIN locality l ON c.LocalityID = l.LocalityID " + 
                        "LEFT JOIN geography g ON l.GeographyID = g.GeographyID ORDER BY c.CollectingEventID";


        oldSQL = "SELECT c.CollectingEventID, c.StartDate, c.StartTime, l.LocalityName, l.Latitude1, l.Longitude1, g.ContinentOrOcean, g.Country, g.State,  g.County, g.IslandGroup, g.Island, g.WaterBody, g.Drainage "+
                        "FROM collectingevent c LEFT JOIN locality l ON c.LocalityID = l.LocalityID "+
                        "LEFT JOIN geography g ON l.GeographyID = g.GeographyID ORDER BY c.CollectingEventID";
        
        String newCntSQL = "SELECT count(*) " + 
                        "FROM collectingevent c LEFT JOIN locality l ON c.LocalityID = l.LocalityID " + 
                        "LEFT JOIN geography g ON l.GeographyID = g.GeographyID ORDER BY c.CollectingEventID";


        String oldCntSQL = "SELECT count(*) "+
                        "FROM collectingevent c LEFT JOIN locality l ON c.LocalityID = l.LocalityID "+
                        "LEFT JOIN geography g ON l.GeographyID = g.GeographyID ORDER BY c.CollectingEventID";


        Integer oldCnt = BasicSQLUtils.getCount(oldCntSQL);
        Integer newCnt = BasicSQLUtils.getCount(newCntSQL);
        String msg2 = "Record Counts ["+oldCnt + " / "  + newCnt+"]";
        log.info(msg2);
        printRowError(msg2);

        try
        {
            getResultSets(oldSQL, newSQL);
            while (true)
            {
            
                boolean hasOldRec = oldDBRS.next();
                boolean hasNewRec = newDBRS.next();
                
                if (!hasOldRec && !hasNewRec)
                {
                    break;
                }
                
                int col = 1;
                int     newId           = newDBRS.getInt(col++);
                String  newStartDate    = newDBRS.getString(col++);
                Integer newStartTime    = newDBRS.getInt(col++);
                String  newLocalityName = newDBRS.getString(col++);
                Double  newLatitude     = newDBRS.getDouble(col++);
                Double  newLongitude    = newDBRS.getDouble(col++);
                String  newGeoName      = newDBRS.getString(col++);
                
                col = 1;
                int          oldId           = oldDBRS.getInt(col++);
                String       oldStartDate    = oldDBRS.getString(col++);
                Integer      oldStartTime    = oldDBRS.getInt(col++);
                String       oldLocalityName = oldDBRS.getString(col++);
                Double       oldLatitude     = oldDBRS.getDouble(col++);
                Double       oldLongitude    = oldDBRS.getDouble(col++);
                
                if (newGeoName != null)
                {
                    boolean fnd       = false;
                    for (int i=7;i<15;i++)
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
                        String msg = "CE["+oldId + " / "  + newId+"]  no match found for new Geo ["+newGeoName+"]";
                        log.error(msg);
                        printRowError(msg);
                    }
                }
                
                Object dateObj = oldDBRS.getObject(2);
                if (dateObj != null && ((Integer)dateObj) == 0)
                {
                    oldStartDate = null;
                }
                
                // Date
                if (oldStartDate == null && newStartDate != null)
                {
                    String msg = "CE["+oldId + " / "  + newId+"]  Old Date["+oldStartDate+"] is NULL   New Date["+newStartDate+"] is not";
                    log.error(msg);
                    printRowError(msg);
                }
                
                if (oldStartDate != null && newStartDate == null)
                {
                    String msg = "Old Date["+oldStartDate+"] is not null   New Date["+newStartDate+"] is NULL";
                    log.error(msg);
                    printRowError(msg);
                }
                
                if (oldStartDate != null && newStartDate != null)
                {
                    Date date = UIHelper.convertIntToDate(oldDBRS.getInt(2));
                    
                    oldStartDate = sdf.format(date);
                    if (!oldStartDate.equals(newStartDate))
                    {
                        String msg = "CE["+oldId + " / "  + newId+"]  Old Date["+oldStartDate+"] is NOT equals   New Date["+newStartDate+"]";
                        log.error(msg);
                        printRowError(msg);
                    }
                }

                // LocalityName
                if (oldLocalityName == null && newLocalityName != null)
                {
                    String msg = "LocName["+oldId + " / "  + newId+"]  Old LocalityName["+oldLocalityName+"] is NULL   New LocalityName["+newLocalityName+"] is not";
                    log.error(msg);
                    printRowError(msg);
                }
                if (oldLocalityName != null && newLocalityName == null)
                {
                    String msg = "LocName["+oldId + " / "  + newId+"]  Old LocalityName["+oldLocalityName+"] is not null   New LocalityName["+newLocalityName+"] is NULL";
                    log.error(msg);
                    printRowError(msg);
                }
                if (oldLocalityName != null && newLocalityName != null && !oldLocalityName.equals(newLocalityName))
                {
                    String msg = "LocName["+oldId + " / "  + newId+"]  Old LocalityName["+oldLocalityName+"] is NOT equals   New LocalityName["+newLocalityName+"]";
                    log.error(msg);
                    printRowError(msg);
                }
                
                // Latitude
                if (oldLatitude == null && newLatitude != null)
                {
                    String msg = "Latitude["+oldId + " / "  + newId+"]  Old Latitude["+oldLatitude+"] is NULL   New Latitude["+newLatitude+"] is not";
                    log.error(msg);
                    printRowError(msg);
                }
                if (oldLatitude != null && newLatitude == null)
                {
                    String msg = "Latitude["+oldId + " / "  + newId+"]  Old Latitude["+oldLatitude+"] is not null   New Latitude["+newLatitude+"] is NULL";
                    log.error(msg);
                    printRowError(msg);
                }
                if (oldLatitude != null && newLatitude != null && !oldLatitude.equals(newLatitude))
                {
                    String msg = "Latitude["+oldId + " / "  + newId+"]  Old Latitude["+oldLatitude+"] is NOT equals   New Latitude["+newLatitude+"]";
                    log.error(msg);
                    printRowError(msg);
                }
                
                // Latitude
                if (oldLongitude == null && newLongitude != null)
                {
                    String msg = "Longitude["+oldId + " / "  + newId+"]  Old Longitude["+oldLongitude+"] is NULL   New Longitude["+newLongitude+"] is not";
                    log.error(msg);
                    printRowError(msg);
                }
                if (oldLongitude != null && newLongitude == null)
                {
                    String msg = "Longitude["+oldId + " / "  + newId+"]  Old Longitude["+oldLongitude+"] is not null   New Longitude["+newLongitude+"] is NULL";
                    log.error(msg);
                    printRowError(msg);
                }
                if (oldLongitude != null && newLongitude != null && !oldLongitude.equals(newLongitude))
                {
                    String msg = "Longitude["+oldId + " / "  + newId+"]  Old Longitude["+oldLongitude+"] is NOT equals   New Longitude["+newLongitude+"]";
                    log.error(msg);
                    printRowError(msg);
                }
            }
            
            oldDBRS.close();
            newDBRS.close();

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
	                                    cv.verifyDB( pair.first, pair.second);
	                                    
	                                } catch (Exception ex)
	                                {
	                                    ex.printStackTrace();
	                                }
	                                return null;
	                            }
	                            
	                            @SuppressWarnings("unchecked") //$NON-NLS-1$
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
    protected boolean selectedDBsToConvert()
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
        
        return true;
    }
    
    /**
     * @return
     * @throws SQLException
     */
    protected Pair<String, String> chooseTable() throws SQLException
    {
        MySQLDMBSUserMgr mgr = new MySQLDMBSUserMgr();
        
        Vector<DBNamePair> availOldPairs = new Vector<DBNamePair>();
        Vector<DBNamePair> availNewPairs = new Vector<DBNamePair>();
        
        try
        {
            if (mgr.connectToDBMS(itUsrPwd.first, itUsrPwd.second, hostName))
            {
                BasicSQLUtils.setSkipTrackExceptions(true);
                
                Connection conn = mgr.getConnection();
                Vector<Object[]> dbNames = BasicSQLUtils.query(conn, "show databases");
                for (Object[] row : dbNames)
                {
                    System.err.println("Setting ["+row[0].toString()+"]");
                    conn.setCatalog(row[0].toString());
                    
                    boolean isSp5 = false;
                    boolean isSp6 = false;
                    
                    Vector<Object[]> tables = BasicSQLUtils.query(conn, "show tables");
                    for (Object[] tblRow : tables)
                    {
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
    	                Vector<Object[]> tableDesc = BasicSQLUtils.query(conn, "select CollectionName FROM collection");
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
                }
                
                Comparator<Pair<String, String>> comparator =  new Comparator<Pair<String, String>>() {
                    @Override
                    public int compare(Pair<String, String> o1, Pair<String, String> o2)
                    {
                        return o1.first.compareTo(o2.first);
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
                pb.add(chkPanel, cc.xyw(1, 5, 3));
                
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
                
                final CustomDialog dlg = new CustomDialog(null, "Select a DB to Convert", true, pb.getPanel());
                
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
            return first + "   ("+ second + ")";
        }
        
    }


}
