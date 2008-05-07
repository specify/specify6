/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.conversion;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

public class ConvertVerifier
{
    protected static final Logger log = Logger.getLogger(ConvertVerifier.class);
    
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


    public void verifyDB(final String databaseNameSource, 
                         final String databaseNameDest) throws Exception
    {
        out = new PrintWriter(new File("verify.htm"));
        
        String title = "From "+databaseNameSource+" to "+databaseNameDest;
        System.out.println("************************************************************");
        System.out.println("From "+databaseNameSource+" to "+databaseNameDest);
        System.out.println("************************************************************");
        
        out.println("<html><head><title>"+databaseNameDest+"</title></head><body>");
        out.println("<h2>"+title+"</h2>");

        HibernateUtil.shutdown();    
        
        Properties initPrefs = BuildSampleDatabase.getInitializePrefs(databaseNameDest);
        
        String userNameSource     = "";
        String passwordSource     = "";
        String driverNameSource   = "";
        String databaseHostSource = "";
        DatabaseDriverInfo driverInfoSource = null;
        
        String userNameDest     = "";
        String passwordDest     = "";
        String driverNameDest   = "";
        String databaseHostDest = "";
        DatabaseDriverInfo driverInfoDest = null;
        
        log.debug("Running an non-custom MySQL convert, using old default login creds");
        userNameSource      = initPrefs.getProperty("initializer.username", "rods");
        passwordSource      = initPrefs.getProperty("initializer.password", "rods");
        driverNameSource    = initPrefs.getProperty("initializer.driver",   "MySQL");
        databaseHostSource  = initPrefs.getProperty("initializer.host",     "localhost"); 
        
        userNameDest        = initPrefs.getProperty("initializer.username", "rods");
        passwordDest        = initPrefs.getProperty("initializer.password", "rods");
        driverNameDest      = initPrefs.getProperty("initializer.driver",   "MySQL");
        databaseHostDest    = initPrefs.getProperty("initializer.host",     "localhost");  
    
        log.debug("Custom Convert Source Properties ----------------------");
        log.debug("databaseNameSource: " + databaseNameSource);        
        log.debug("userNameSource: " + userNameSource);
        log.debug("passwordSource: " + passwordSource);
        log.debug("driverNameSource: " + driverNameSource);
        log.debug("databaseHostSource: " + databaseHostSource);
        
        log.debug("Custom Convert Destination Properties ----------------------");
        log.debug("databaseNameDest: " + databaseNameDest);
        log.debug("userNameDest: " + userNameDest);
        log.debug("passwordDest: " + passwordDest);
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
        String destConnectionString = driverInfoDest.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, "", userNameDest, passwordDest, driverNameDest);
        log.debug("attempting login to destination: " + destConnectionString);
        // This will log us in and return true/false
        // This will connect without specifying a DB, which allows us to create the DB
        if (!UIHelper.tryLogin(driverInfoDest.getDriverClassName(), 
                driverInfoDest.getDialectClassName(), 
                databaseNameDest, 
                destConnectionString,
                userNameDest, 
                passwordDest))
        {
            log.error("Failed connection string: "  +driverInfoSource.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, databaseNameDest, userNameDest, passwordDest, driverNameDest) );
            throw new RuntimeException("Couldn't login into ["+databaseNameDest+"] "+DBConnection.getInstance().getErrorMsg());
        }
        
        //MEG WHY IS THIS COMMENTED OUT???
        //DataBuilder.setSession(HibernateUtil.getNewSession());
        
        log.debug("DESTINATION driver class: " + driverInfoDest.getDriverClassName());
        log.debug("DESTINATION dialect class: " + driverInfoDest.getDialectClassName());               
        log.debug("DESTINATION Connection String: " + driverInfoDest.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, databaseNameDest, userNameDest, passwordDest, driverNameDest)); 
        
        // This will log us in and return true/false
        if (!UIHelper.tryLogin(driverInfoDest.getDriverClassName(), 
                driverInfoDest.getDialectClassName(), 
                databaseNameDest, 
                driverInfoDest.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, databaseNameDest, userNameDest, passwordDest, driverNameDest),                 
                userNameDest, 
                passwordDest))
        {
            throw new RuntimeException("Couldn't login into ["+databaseNameDest+"] "+DBConnection.getInstance().getErrorMsg());
        }
        
        String srcConStr = driverInfoSource.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostSource, databaseNameSource, userNameSource, passwordSource, driverNameSource);
        DBConnection oldDB = DBConnection.createInstance(driverInfoSource.getDriverClassName(), null, databaseNameSource, srcConStr, userNameSource, passwordSource);
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
                    break;
                }            
                if (isCOOn(DO_CO_CATLOGER) && !verifyCataloger(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Cataloger");
                    log.error("Cat Num: "+oldCatNum);
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    break;
                }
                if (isCOOn(DO_CO_GEO) && !verifyGeography(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Geography");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    break;
                }
                if (isCOOn(DO_CO_CE) && !verifyCollectingEvent(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Collecting Event");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    break;
                }
                if (isCOOn(DO_CO_TAXON) && !verifyTaxon(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Taxon");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    break;
                }
                if (isCOOn(DO_CO_LOCALITY) && !verifyCOToLocality(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Locality");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    break;
                }
                if (isCOOn(DO_CO_PREPARATION) && !verifyPreparation(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Preparations");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    break;
                }
                if (isCOOn(DO_CO_PREPARER) && !verifyPreparer(oldCatNum, newCatNum))
                {
                    printVerifyHeader("Preparer");
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    break;
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
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    break;
                }            
                
                if (isACOn(DO_AC_AGENTS) && !verifyAccessionAgents(oldAccNum, newAccNum))
                {
                    printVerifyHeader("Accession Agents");
                    log.error("Accession Num: "+oldAccNum);
                    log.error("New SQL: "+newSQL);
                    log.error("Old SQL: "+oldSQL);
                    break;
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
        newSQL = "SELECT collectionobject.CatalogedDate, determination.DeterminedDate, taxon.FullName " + 
                        "FROM determination INNER JOIN collectionobject ON determination.CollectionObjectID = collectionobject.CollectionObjectID "+
                        "INNER JOIN taxon ON determination.TaxonID = taxon.TaxonID WHERE CatalogNumber = '"+ newCatNum + "'";

        oldSQL = "SELECT collectionobjectcatalog.CatalogedDate,determination.Date1,taxonname.FullTaxonName " + 
                        "FROM determination INNER JOIN taxonname ON determination.TaxonNameID = taxonname.TaxonNameID " + 
                        "INNER JOIN collectionobjectcatalog ON collectionobjectcatalog.CollectionObjectCatalogID = determination.BiologicalObjectID " + 
                        "WHERE CatalogNumber = " + oldCatNum;
        
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
    
    protected boolean verifyTableCounts(final String newTableName, final String oldTableName) throws SQLException
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
         newSQL = "SELECT collectingevent.StartDate, locality.LocalityName " +
                        "FROM collectionobject INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
                        "INNER JOIN locality ON collectingevent.LocalityID = locality.LocalityID " +
                        "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT collectingevent.StartDate, locality.LocalityName  " +
                        "FROM collectionobjectcatalog INNER JOIN collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID " +
                        "INNER JOIN collectingevent ON collectionobject.CollectingEventID = collectingevent.CollectingEventID " +
                        "INNER JOIN locality ON collectingevent.LocalityID = locality.LocalityID " +
                        "WHERE CatalogNumber = " + oldCatNum;
        
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
        
         log.debug("New SQL: "+newSQL);
         log.debug("Old SQL: "+oldSQL);
         
        return compareRecords("Determiner", oldCatNum, newCatNum, oldSQL, newSQL);
    }

    protected boolean verifyPreparer(final int oldCatNum, final String newCatNum) throws SQLException
    {
         newSQL = "SELECT agent.FirstName, agent.MiddleInitial, agent.LastName " +
                  "FROM collectionobject INNER JOIN preparation ON collectionobject.CollectionObjectID = preparation.CollectionObjectID INNER JOIN agent ON preparation.PreparedByID = agent.AgentID " +
                  "WHERE CatalogNumber = '"+ newCatNum + "'";

         oldSQL = "SELECT agent.FirstName, agent.MiddleInitial, agent.LastName, agent.Name  " +
                  "FROM collectionobjectcatalog INNER JOIN collectionobject ON collectionobjectcatalog.CollectionObjectCatalogID = collectionobject.CollectionObjectID " +
                  "INNER JOIN determination ON determination.BiologicalObjectID = collectionobject.CollectionObjectID " + 
                  "INNER JOIN agent ON determination.DeterminerID = agent.AgentID WHERE CatalogNumber = " + oldCatNum;
        
         log.error("New SQL: "+newSQL);
         log.error("Old SQL: "+oldSQL);
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
    
        System.out.println(newSQL);
        System.out.println(oldSQL);

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
         newSQL = "SELECT collectingevent.StartDate, collectingevent.StationFieldNumber " +
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
         newSQL = "SELECT preparation.Count, " +
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
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
    protected boolean compareRecords(final String desc, 
                                     final String oldCatNum, 
                                     final String newCatNum, 
                                     final String oldSQLArg, 
                                     final String newSQLArg) throws SQLException
    {
        getResultSets(oldSQLArg, newSQLArg);
        
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
            while (hasNewRec && hasOldRec)
            {
                for (int col=1;col<=newRsmd.getColumnCount();col++)
                {
                    //System.out.println(newRsmd.getColumnName(col));
                    
                    Object newObj = newDBRS.getObject(col);
                    Object oldObj = oldDBRS.getObject(col);
                    
                    if (oldObj == null && newObj == null)
                    {
                        continue;
                    }
                    
                    String oldColName = oldRsmd.getColumnName(col);
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
                        String clsName = newRsmd.getColumnClassName(col);
                        if (!clsName.equals("java.sql.Date") || ((Integer)oldObj) != 0)
                        {
                            String msg = "New Value was null and shouldn't have been for Key Value ["+newCatNum+"] Field ["+newRsmd.getColumnName(col)+"] ["+oldObj+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(msg);
                            return false;
                        } else
                        {
                            continue;
                        }
                    }
                    
                    //System.out.println(newObj.getClass().getName()+"  "+oldObj.getClass().getName());
                    
                    if (newObj instanceof java.sql.Date)
                    {
                        int  oldIntDate = oldDBRS.getInt(col);
                        if (oldIntDate == 0)
                        {
                            continue;
                        }
                        Date oldDate = UIHelper.convertIntToDate(oldIntDate);
                        Date newDate = newDBRS.getDate(col);
                        
                        int newYear = newDate.getYear();
                        int oldYear = oldDate.getYear();
                        
                        if (newYear != oldYear)
                        {
                            String msg = "Dates don't compare["+oldDate+"]["+newDate+"] Years["+oldYear+"]["+newYear+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(msg);
                            return false;
                        }
                        
                    } else if (newObj instanceof Float || newObj instanceof Double)
                    {
                        String s1 = String.format("%10.5f", newObj instanceof Float ? (Float)newObj : (Double)newObj);
                        String s2 = String.format("%10.5f", oldObj instanceof Float ? (Float)oldObj : (Double)oldObj);
                        if (!s1.equals(s2))
                        {
                            String msg = "Columns don't compare["+s1+"]["+s2+"]  ["+newRsmd.getColumnName(col)+"]["+oldRsmd.getColumnName(col)+"]";
                            log.error(desc+ " - "+msg);
                            printRowError(msg);
                            return true;
                        }
                        
                    } else
                    {
                        String newColName = newRsmd.getColumnName(col);
                        if (checkForAgent && StringUtils.contains(newColName, "LastName"))
                        {
                            String lastName    = oldDBRS.getString(col);
                            String agentName   = oldDBRS.getString(col+1); // The 'Name' Column
                            String newLastName = newDBRS.getString(col);
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
                                log.error(desc+ " - Columns don't compare["+newObj+"]["+oldObj+"]  ["+newRsmd.getColumnName(col)+"]["+oldRsmd.getColumnName(col)+"]");
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
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        final SpecifyDBConverter converter = new  SpecifyDBConverter();
        
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
                String[] names = {
                        "Custom", "",
                        //"Fish", "sp4_fish", 
                        //"Accessions", "sp4_accessions", 
                        //"Cranbrook", "sp4_cranbrook", 
                        //"Ento", "sp4_ento", 
                        "Bird_Deleware", "sp5_dmnhbird",
                        "Ento_CAS", "sp5_cas_ento",
                        "Fish_Kansas", "sp5_kufish",                        
                        "Herbarium_NewHampshire","sp5_unhnhaherbarium",
                        "Herps_Guatemala","sp5_uvgherps",
                        "Invert_Alabama", "sp5_uamc_invert",
                        "Mammals_SAfrica", "sp5_mmpemammals",
                        "Multi_ChicagoAS", "sp5_chias_multi",
                        "Paleo_Colorado", "sp5_cupaleo",
                        };
                for (int i=0;i<names.length;i++)
                {
                    old2NewDBNames.put(names[i], names[++i]);
                }
                UIRegistry.setAppName("Specify");
                
                dbNamesToConvert = converter.selectedDBsToConvert(names);
                if (dbNamesToConvert.size() == 1)
                {
                    String oldName = old2NewDBNames.get(dbNamesToConvert.get(0));
                    log.debug("size of name to conver: " + dbNamesToConvert.size());
                    
                    {
                        log.debug("Running custom converter");
                        log.debug("Source db: " + dbNamesToConvert.get(0));
                        log.debug("Dest db:   " + oldName);
                        
                        try
                        {
                            ConvertVerifier cv = new ConvertVerifier();
                            cv.verifyDB( oldName, dbNamesToConvert.get(0).toLowerCase());
                            
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
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

}
