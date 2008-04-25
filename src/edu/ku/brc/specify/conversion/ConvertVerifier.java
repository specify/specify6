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

import java.sql.Connection;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.DatabaseConnectionProperties;

public class ConvertVerifier
{
    protected static final Logger log = Logger.getLogger(ConvertVerifier.class);
    
    protected static ProgressFrame              frame             = null;
    protected static List<String>               dbNamesToConvert  = null;
    protected static int                        currentIndex      = 0;
    protected static Hashtable<String, String>  old2NewDBNames    = null;
    
    protected String                                        oldDriver              = "";
    protected String                                        oldDBName              = "";
    protected String                                        oldUserName            = "";
    protected String                                        oldPassword            = "";

    protected IdMapperMgr                                   idMapperMgr;

    protected Connection                                    oldDBConn;
    protected Connection                                    newDBConn;

    public void verifyDB(final String databaseNameSource, 
                         final String databaseNameDest) throws Exception
    {
        System.out.println("************************************************************");
        System.out.println("From "+databaseNameSource+" to "+databaseNameDest);
        System.out.println("************************************************************");

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
        DataBuilder.setSession(HibernateUtil.getNewSession());
        HibernateUtil.getCurrentSession(); 
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
                
                log.debug("size of name to conver: " + dbNamesToConvert.size());
                
                {
                    log.debug("Running custom converter");
                    log.debug("Source db: " + dbNamesToConvert.get(0));
                    log.debug("Dest db:   " + dbNamesToConvert.get(1));
                    
                    try
                    {
                        ConvertVerifier cv = new ConvertVerifier();
                        cv.verifyDB( dbNamesToConvert.get(0), dbNamesToConvert.get(1));
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        });

    }

}
