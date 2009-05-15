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
import javax.swing.JList;
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

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.MySQLDMBSUserMgr;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.ToggleButtonChooserPanel.Type;
import edu.ku.brc.util.Pair;

public class ConvertVerifier
{
    protected static final Logger log = Logger.getLogger(ConvertVerifier.class);
    
    protected Pair<String, String> itUsrPwd          = new Pair<String, String>(null, null);
    protected String               hostName          = "localhost";
    protected Pair<String, String> namePairToConvert = null;
    
    // These are the configuration Options for a View
    public static final long NO_OPTIONS             =   0; // Indicates there are no options
    public static final long DO_CO_PREPARATION      =   1; 
    public static final long DO_CO_CE               =   2; 
    public static final long DO_CO_LOCALITY         =   4; 
    public static final long DO_CO_PREPARER         =   8; 
    public static final long DO_CO_CATLOGER         =  16; 
    public static final long DO_CO_DETERMINER       =  32; 
    public static final long DO_CO_TAXON            =  64; 
    public static final long DO_CO_GEO              = 128; 
    public static final long DO_CO_ALL              = 1023; 
    
    protected String[] labels = {"None", "Preparations", "Collecting Events", "Localities", "Preparers", "Catalogers", "Determiners", "Taxon", "Geographies", "All"};
    protected ToggleButtonChooserPanel<String> chkPanel;
    
    //public static final long DONT_ADD_ALL_ALTVIEWS  = 256; 
    //public static final long USE_ONLY_CREATION_MODE = 512;
    
    public static final long DO_ACCESSIONS          =   1; 
    public static final long DO_AC_AUTHS            =   2; 
    public static final long DO_AC_AGENTS           =   4; 
    public static final long DO_AC_ALL              =   7; 

    protected static long                                   coOptions         = NO_OPTIONS;
    protected static long                                   acOptions         = NO_OPTIONS;
    protected static ProgressFrame                          frame             = null;
    protected static List<String>                           dbNamesToConvert  = null;
    protected static int                                    currentIndex      = 0;
    protected static Hashtable<String, String>              old2NewDBNames    = null;
    
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
    
    
    /**
     * @param databaseNameSource
     * @param databaseNameDest
     * @throws Exception
     */
    public void verifyDB(final String databaseNameSource, 
                         final String databaseNameDest) throws Exception
    {
        out = new PrintWriter(new File("verify.html"));
        
        String title = "From "+databaseNameSource+" to "+databaseNameDest;
        System.out.println("************************************************************");
        System.out.println("From "+databaseNameSource+" to "+databaseNameDest);
        System.out.println("************************************************************");
        
        out.println("<html><head><title>"+databaseNameDest+"</title></head><body>");
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
        else if (driverNameDest.equals("Derby"))BasicSQLUtils.myDestinationServerType = BasicSQLUtils.SERVERTYPE.Derby;
        else if (driverNameDest.equals("SQLServer"))BasicSQLUtils.myDestinationServerType = BasicSQLUtils.SERVERTYPE.MS_SQLServer;
        
        if (driverNameSource.equals("MySQL"))BasicSQLUtils.mySourceServerType = BasicSQLUtils.SERVERTYPE.MySQL;
        else if (driverNameSource.equals("Derby"))BasicSQLUtils.mySourceServerType = BasicSQLUtils.SERVERTYPE.Derby;
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
        
        out.println("<H3>Collection Objects</H3>");
        out.println("<table border=\"1\">");
        
        coOptions = DO_CO_ALL;
        acOptions = DO_AC_ALL;

        if (coOptions > NO_OPTIONS)
        {
            
            int i = 0;
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT CatalogNumber FROM collectionobjectcatalog WHERE CollectionObjectTypeID = 10 ORDER BY CatalogNumber ASC");
            while (rs.next())
            {
                int    oldCatNum = rs.getInt(1);
                String newCatNum = convertCatNum(oldCatNum);
                
                if (isCOOn(DO_CO_DETERMINER) && !verifyDeterminer(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Determiner");
                    log.error("Cat Num: "+oldCatNum);
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    //break;
                }            
                if (isCOOn(DO_CO_CATLOGER) && !verifyCataloger(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Cataloger");
                    log.error("Cat Num: "+oldCatNum);
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    //break;
                }
                if (isCOOn(DO_CO_GEO) && !verifyGeography(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Geography");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    //break;
                }
                if (isCOOn(DO_CO_CE) && !verifyCollectingEvent(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Collecting Event");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    //break;
                }
                if (isCOOn(DO_CO_TAXON) && !verifyTaxon(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Taxon");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    //break;
                }
                if (isCOOn(DO_CO_LOCALITY) && !verifyCOToLocality(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Locality");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    //break;
                }
                if (isCOOn(DO_CO_PREPARATION) && !verifyPreparation(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Preparations");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    //break;
                }
                if (isCOOn(DO_CO_PREPARER) && !verifyPreparer(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Preparer");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    //break;
                }
                
                if ((i % 100) == 0)
                {
                    System.out.println(i+"  "+oldCatNum);
                }
                i++;
            }
            
            rs.close();
            stmt.close();
        }
        printTotal();
        out.println("</table>");
        out.println("<H3>Accessions</H3>");
        out.println("<table border=\"1\">");
        
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
        
        newDBConn.close();
        oldDBConn.close();
        
        printTotal();
        out.println("</table>");
        out.println("</body>");
        
        log.info("Done.");
        
        out.flush();
        out.close();
    }
    
    public void compareDate(final Integer sp5Date, final Calendar sp6Date, final Byte partialType)
    {
        
    }
    
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
                        "FROM determination INNER JOIN collectionobject ON determination.CollectionObjectID = collectionobject.CollectionObjectID "+
                        "INNER JOIN taxon ON determination.TaxonID = taxon.TaxonID WHERE CatalogNumber = '"+ newCatNum + "'";

        oldSQL = "SELECT collectionobjectcatalog.CatalogedDate, determination.Date,taxonname.FullTaxonName " + 
                        "FROM determination INNER JOIN taxonname ON determination.TaxonNameID = taxonname.TaxonNameID " + 
                        "INNER JOIN collectionobjectcatalog ON collectionobjectcatalog.CollectionObjectCatalogID = determination.BiologicalObjectID " + 
                        "WHERE CatalogNumber = " + oldCatNum;
        if (debug)
        {
	         log.debug("New SQL: "+newSQL);
	         log.debug("Old SQL: "+oldSQL);
        }
        return compareRecords("Taxon", oldCatNum, newCatNum, oldSQL, newSQL);
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
                //return false;
                return true;
            }
            if (!hasNewRec)
            {
                log.error("Geography - No New Record for ["+newCatNum+"]");
                //return false;
                return true;
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
         
        return compareRecords("Locality", oldCatNum, newCatNum, oldSQL, newSQL);
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
        return compareRecords("Cataloger", oldCatNum, newCatNum, oldSQL, newSQL);
    }
    
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
         
        return compareRecords("Determiner", oldCatNum, newCatNum, oldSQL, newSQL);
    }

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
        return compareRecords("Preparer", oldCatNum, newCatNum, oldSQL, newSQL);
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
                    //return false;
                    return true;
                }
                if (!hasNewRec)
                {
                    log.error("Geography - No New Record for ["+oldDBRS.getObject(1)+"]");
                    //return false;
                    return true;
                }

            }
            return false;

        } finally
        {
            doneWithRS();
        }
    }
                         
    protected boolean verifyCollectingEvent(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT collectingevent.StartDate, collectingevent.StartDatePrecision, collectingevent.StationFieldNumber " +
                        "FROM collectionobject INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
                        "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT collectingevent.StartDate, collectingevent.StationFieldNumber  " +
                        "FROM collectionobjectcatalog INNER JOIN collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID " +
                        "INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
                        "WHERE CatalogNumber = " + oldCatNum;
        
        return compareRecords("Locality", oldCatNum, newCatNum, oldSQL, newSQL);
    }
                         
    protected boolean verifyPreparation(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT preparation.CountAmt, " +
                    "collectionobject.FieldNumber, " +
                    "preptype.Name, " +
                    "preparation.Text2 " +
                    "FROM collectionobject INNER JOIN preparation ON collectionobject.CollectionObjectID = preparation.CollectionObjectID " +
                    "INNER JOIN preptype ON preparation.PrepTypeID = preptype.PrepTypeID " +
                    "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT collectionobject.Count, " +
                    "collectionobject.FieldNumber, " +
                    "collectionobject.PreparationMethod, " +
                    "collectionobject.Text2 " +
                    "FROM collectionobject INNER JOIN collectionobjectcatalog ON collectionobject.DerivedFromID = collectionobjectcatalog.CollectionObjectCatalogID " +
                    "WHERE CatalogNumber = " + oldCatNum;
        
        return compareRecords("Locality", oldCatNum, newCatNum, oldSQL, newSQL);
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
        
        return compareRecords("Accession", oldAccNum, newAccNum, oldSQL, newSQL);
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

        return compareRecords("Accession", oldAccNum, newAccNum, oldSQL, newSQL);
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
    protected boolean compareRecords(final String desc, 
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
        out.println("<tr><td>&nbsp;</td><td>"+msg+"</td></tr>");
        numErrors++;
    }
    
    /**
     * @param msg
     */
    protected void printRowError(final String catNo, final String msg)
    {
        out.println("<tr><td>"+catNo+"</td><td>"+msg+"</td></tr>");
        numErrors++;
    }
    
    /**
     * 
     */
    protected void printTotal()
    {
        out.println("<tr><td>Total Errors</td><td>"+numErrors+"</td></tr>");
        numErrors = 0;
    }
    
    /**
     * @param oldSQL
     * @param newSQL
     * @return
     * @throws SQLException
     */
    protected boolean compareRecords(final String desc, 
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
                return true;
            }
            
            if (!hasOldRec)
            {
                log.error(desc+ " - No Old Record for ["+oldCatNum+"]");
                printRowError("No Old Record for ["+oldCatNum+"]");
                //return false;
                return true;
            }
            if (!hasNewRec)
            {
                log.error(desc+ " - No New Record for ["+newCatNum+"]");
                printRowError("No New Record for ["+newCatNum+"]");
                //return false;
                return true;
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
                    if (oldObj == null && !StringUtils.contains(oldColName, "LastName"))
                    {
                        String msg = "Old Value was null and shouldn't have been for Key Value ["+oldCatNum+"] Field ["+oldColName+"]";
                        log.error(desc+ " - "+msg);
                        printRowError(msg);
                        //return false;
                        return true;
                    }
                    
                    if (newObj == null)
                    {
                        String clsName = newRsmd.getColumnClassName(newColInx);
                        String colName = newRsmd.getColumnName(newColInx);

                        if (!clsName.equals("java.sql.Date") || (!(oldObj instanceof String) && ((Number)oldObj).intValue() != 0))
                        {
                            String msg = "New Value was null and shouldn't have been for Key Value CatNo["+newCatNum+"] Field ["+colName+"] ["+oldObj+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(msg);
                            return false;
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
                            return false;
                        }
                        
                    } else if (newObj instanceof Float || newObj instanceof Double)
                    {
                        String s1 = String.format("%10.5f", newObj instanceof Float ? (Float)newObj : (Double)newObj);
                        String s2 = String.format("%10.5f", oldObj instanceof Float ? (Float)oldObj : (Double)oldObj);
                        if (!s1.equals(s2))
                        {
                            String msg = "Columns don't compare["+s1+"]["+s2+"]  ["+newRsmd.getColumnName(col)+"]["+oldRsmd.getColumnName(oldColInx)+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(msg);
                            return true;
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
                                printRowError(msg);
                                return true;
                            }
                            
                        } else if (!newObj.equals(oldObj))
                        {
                            String msg = "Columns don't Num["+oldCatNum+"] compare["+newObj+"]["+oldObj+"]  ["+newColName+"]["+oldColName+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(msg);
                            return true;

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
                    return true;
                }
                
                if (!hasOldRec)
                {
                    log.error(desc+ " - No Old Record for ["+oldCatNum+"]");
                    printRowError("No Old Record for ["+oldCatNum+"]");
                    return true;
                }
                if (!hasNewRec)
                {
                    log.error(desc+ "No New Record for ["+newCatNum+"]");
                    printRowError("No New Record for ["+newCatNum+"]");
                    return true;
                }
            }
        } finally
        {
            doneWithRS();
        }
            
        return true;
    }
    
    protected void verifyCollectors()
    {
    	String oldSQL = "SELECT collectingevent.CollectingEventID, collectingevent.StartDate, agent.FirstName,  agent.LastName, collectors.Order  " + 
         "FROM collectingevent INNER JOIN collectors ON collectingevent.CollectingEventID = collectors.CollectingEventID " + 
         "INNER JOIN agent ON collectors.AgentID = agent.AgentID ORDER BY collectingevent.CollectingEventID, collectors.Order";
    	
    	String newSQL = "SELECT collectingevent.CollectingEventID, collectingevent.StartDate, agent.FirstName, agent.LastName, collector.OrderNumber   " + 
    	 "FROM collectingevent INNER JOIN collector ON collectingevent.CollectingEventID = collector.CollectingEventID  " + 
    	 "INNER JOIN agent ON collector.AgentID = agent.AgentID ORDER BY collectingevent.CollectingEventID, collectors.OrderNumber";
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
                
                frame = new ProgressFrame("Converting");
                
                ConvertVerifier cv = new ConvertVerifier();
                
                if (cv.selectedDBsToConvert())
                {
                	try
                	{
	                	Pair<String, String> pair = cv.chooseTable();
	                	if (pair != null)
	                	{
	                		cv.verifyDB( pair.first, pair.second);
	                	}
                	} catch (Exception ex)
                	{
                		ex.printStackTrace();
                	}
                }
                System.exit(0);
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
            
            ActionListener al = new ActionListener() {
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
            
            //chkPanel.getButtons().get(0).addActionListener(al);
            //chkPanel.getButtons().get(chkPanel.getButtons().size()-1).addActionListener(al);

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
        
        return null;
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
