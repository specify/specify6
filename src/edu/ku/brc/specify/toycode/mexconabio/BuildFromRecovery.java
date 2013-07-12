/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.toycode.mexconabio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.IdMapperMgr;
import edu.ku.brc.specify.dbsupport.HibernateDataProviderSession;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 23, 2010
 *
 */
public class BuildFromRecovery
{
    private final Logger log = Logger.getLogger(BuildFromRecovery.class);
    protected Session                  session = null;
    protected DataProviderSessionIFace hibSession;

    private Connection dbConn     = null;
    private Connection dbConn2    = null;
    private Connection srcDBConn  = null;
    private Connection srcDBConn2 = null;
    
    private PreparedStatement geoStmt1  = null;
    private PreparedStatement geoStmt2  = null;
    private PreparedStatement agentStmt = null;
    private PreparedStatement tagStmt   = null;

    private HashMap<String, Integer> localityHash    = new HashMap<String, Integer>();
    private HashMap<Integer, String> geoFullNameHash = new HashMap<Integer, String>();
    private BuildTags                buildTags;
    
    private StringBuilder sb = new StringBuilder();
    
    /**
     * 
     */
    public BuildFromRecovery()
    {
        super();
     }
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public void createDBConnection(final String server, 
                                   final String port, 
                                   final String dbName, 
                                   final String username, 
                                   final String pwd)
    {
        String connStr = "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&autoReconnect=true";
        try
        {
            dbConn = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            dbConn2 = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
        
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public void createSrcDBConnection(final String server, 
                                      final String port, 
                                      final String dbName, 
                                      final String username, 
                                      final String pwd)
    {

        String connStr = "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&autoReconnect=true";
        try
        {
            srcDBConn  = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            srcDBConn2 = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param args
     * @return
     */
    private String getLocalityName(final String...args)//final String country, final String state, final String county, final String city)
    {
        sb.setLength(0);
        for (String str : args)
        {
            if (sb.length() > 0) sb.append(", ");
            if (StringUtils.isNotEmpty(str))
            {
                sb.append(str);
            }
        }
        return sb.length() == 0 ? "N/A" : sb.toString();
    }
    
    /**
     * @param stmt
     * @param first
     * @param last
     * @return
     */
    private Integer getAgentId(final PreparedStatement stmt, final String first, final String last)
    {
        Integer   id = null;
        ResultSet rs = null;
        try
        {
            stmt.setString(1, first == null ? null : first.toLowerCase());
            stmt.setString(2, last == null ? null : last.toLowerCase());
            
            rs = stmt.executeQuery();
            if (rs.next())
            {
                id = rs.getInt(1);
                if (rs.wasNull()) id = null;
            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null) rs.close();
            } catch (Exception ex) {}
        }

        return id;
    }
    
    /**
     * @param stmt
     * @return
     */
    /*private Integer getId(final PreparedStatement stmt)
    {
        Integer   count = null;
        ResultSet rs    = null;
        try
        {
            rs   = stmt.executeQuery();
            if (rs.next())
            {
                count = rs.getInt(1);
                if (rs.wasNull()) count = null;
            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null) rs.close();
            } catch (Exception ex) {}
        }

        return count;
    }*/
    
    /**
     * @param str1
     * @param str2
     * @param str3
     * @return
     */
    private String condense(final String...fields)
    {
        String str = "";
        for (String s : fields)
        {
            if (StringUtils.isNotEmpty(s))
            {
                str = s;
            }
        }
        return str;
    }
    
    /**
     * 
     */
    public void process() throws SQLException
    {
        buildTags = new BuildTags();
        buildTags.setDbConn(dbConn);
        buildTags.setDbConn2(dbConn);
        buildTags.initialPrepareStatements();
        
        BasicSQLUtils.setDBConnection(dbConn);
        
        IdMapperMgr idMapperMgr = IdMapperMgr.getInstance();
        idMapperMgr.setDBs(srcDBConn2, dbConn);
        
        geoStmt1  = dbConn.prepareStatement("SELECT GeographyID FROM geography WHERE RankID = ? AND ParentID = ? AND LOWER(Abbrev) = ?");
        geoStmt2  = dbConn.prepareStatement("SELECT GeographyID FROM geography WHERE RankID = ? AND LOWER(Abbrev) = ?");
        agentStmt = dbConn.prepareStatement("SELECT AgentID FROM agent WHERE LOWER(FirstName) = ? AND LOWER(LastName) = ?");
        tagStmt   = dbConn.prepareStatement("SELECT CollectionObjectID FROM collectionobject WHERE CollectionID = 4 AND LOWER(FieldNumber) = ?");
        
        BasicSQLUtils.update(srcDBConn, "UPDATE recovery SET r_date = null WHERE r_date = '0000-00-00'");

        boolean doTags = true;
        if (doTags)
        {
            int divId = 2;
            int dspId = 3;
            int colId = 32768;
            
            String sql = "SELECT tagid, " +
            		     "r_city, r_state, r_zip, r_country, r_date, r_lat, r_long, " +
            		     "reporter_first, reporter_last, reporter_city, reporter_state, reporter_country, reporter_zip, " +
            		     "dir, dist, gender, " +
            		     "t_first, t_middle, t_last, t_city, t_state, t_country, t_postalcode, t_org, t_lat, t_long, t_date FROM recovery ORDER BY recovid ASC";
            
            Statement stmt = srcDBConn.createStatement();
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            log.debug("Querying for Tags...");
            ResultSet rs = stmt.executeQuery(sql);
            int cnt = 0;
            log.debug("Done querying for Tags...");
            
            Calendar  cal = Calendar.getInstance();
            Timestamp ts  = new Timestamp(cal.getTime().getTime());
            
            String common = "TimestampCreated, Version, CreatedByAgentID";
            String coStr = String.format("INSERT INTO collectionobject (CatalogNumber, FieldNumber, Text1, Text2, Remarks, CollectionID, CollectionMemberId, CollectingEventID, %s) VALUES(?,?,?,?,?,?,?,?,?,?,?)", common);
            String ceStr = String.format("INSERT INTO collectingevent (StartDate, DisciplineID, LocalityID, %s) VALUES(?,?,?,?,?,?)", common);
            String lcStr = String.format("INSERT INTO locality (Latitude1, Longitude1, SrcLatLongUnit, Lat1text, Long1text, LatLongType, DisciplineID, LocalityName, GeographyID, %s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)", common);
            String clStr = String.format("INSERT INTO collector (OrderNumber, IsPrimary, CollectingEventID, DivisionID, AgentID, %s) VALUES(?,?,?,?,?,?,?,?)", common);
            String rlStr = String.format("INSERT INTO collectionrelationship (collectionRelTypeID, LeftSideCollectionID, RightSideCollectionID, %s) VALUES(?,?,?,?,?,?)", common);
            String agStr = String.format("INSERT INTO agent (AgentType, FirstName, LastName, %s) VALUES(?,?,?,?,?,?)", common);
            String adStr = String.format("INSERT INTO address (City, State, PostalCode, Country, AgentID, %s) VALUES(?,?,?,?, ?,?,?,?)", common);
            
            String lcUpdateStr = "UPDATE locality SET Latitude1=?, Longitude1=?, SrcLatLongUnit=?, Lat1text=?, Long1text=?, LatLongType=? WHERE LocalityID = ?";
            String lcStr2      = "SELECT LocalityID FROM locality WHERE LocalityName LIKE ? AND LocalityName LIKE ?";

            
            PreparedStatement coStmt = dbConn.prepareStatement(coStr);
            PreparedStatement ceStmt = dbConn.prepareStatement(ceStr);
            PreparedStatement lcStmt = dbConn.prepareStatement(lcStr);
            PreparedStatement clStmt = dbConn.prepareStatement(clStr);
            PreparedStatement rlStmt = dbConn.prepareStatement(rlStr);
            PreparedStatement agStmt = dbConn.prepareStatement(agStr);
            PreparedStatement adStmt = dbConn.prepareStatement(adStr);
            PreparedStatement lcUpStmt = dbConn.prepareStatement(lcUpdateStr);
            PreparedStatement lcStmt2  = dbConn.prepareStatement(lcStr2);
            
            int recNum = 1;
            while (rs.next())
            {
                String tag     = rs.getString(1);
                
                String city    = rs.getString(2);
                String state   = rs.getString(3);
                String zip     = rs.getString(4);
                String country = rs.getString(5);
                Date   date    = rs.getDate(6);
                
                double lat        = rs.getDouble(7);
                boolean isLatNull = rs.wasNull();
                
                double lon     = rs.getDouble(8);
                boolean isLonNull = rs.wasNull();
                
                String dir     = rs.getString(9);
                String dist    = rs.getString(10);
                String gender  = rs.getString(11);
                
                String rep_first   = rs.getString(12);
                String rep_last    = rs.getString(13);
                String rep_city    = rs.getString(14);
                String rep_state   = rs.getString(15);
                String rep_country = rs.getString(16);
                String rep_zip     = rs.getString(17);
                
                String t_first     = rs.getString(18);
                //String t_middle    = rs.getString(19);
                String t_last      = rs.getString(20);
                String t_city      = rs.getString(21);
                String t_state     = rs.getString(22);
                String t_country   = rs.getString(23);
                String t_zip       = rs.getString(24);
                //String t_org       = rs.getString(25);
                
                
                double t_lat       = rs.getDouble(26);
                boolean isTLatNull = rs.wasNull();

                double t_lon       = rs.getDouble(27);
                boolean isTLonNull = rs.wasNull();

                //String oldState = state;
                
                city      = condense(rep_city, t_city, city);
                state     = condense(rep_state, state, t_state);
                country   = condense(rep_country, country, t_country);
                zip       = condense(rep_zip, zip, t_zip);
                rep_first = condense(rep_first, t_first);
                rep_last  = condense(rep_last, t_last);
                
               
                /*boolean debug = ((rep_state != null && rep_state.equals("IA")) || 
                    (t_state != null && t_state.equals("IA")) || 
                    (oldState != null && oldState.equals("IA")));
                
                if (debug && (state == null || !state.equals("IA")))
                {
                    System.out.println("ouch");
                }*/
                        
                
                if (rep_first != null && rep_first.length() > 50)
                {
                    rep_first = rep_first.substring(0, 50);
                }
                
                lat = isLatNull && !isTLatNull ? t_lat : lat;
                lon = isLonNull && !isTLonNull ? t_lon : lon;
                
                try
                {
                    // (Latitude1, Longitude1, SrcLatLongUnit, Lat1text, Long1text, LatLongType, DisciplineID, MaxElevation, LocalityName, GeographyID
                    Integer geoId = buildTags.getGeography(country, state, null);
                    
                    // Latitude varies between -90 and 90, and Longitude between -180 and 180.
                    if (lat < -90.0 || lat > 90.0)
                    {
                        lcStmt.setObject(1, null);
                        lcStmt.setObject(4, null);
                    } else
                    {
                        lcStmt.setDouble(1, lat);
                        lcStmt.setString(4, Double.toString(lat));
                        
                        lcUpStmt.setDouble(1, lat);
                        lcUpStmt.setString(4, Double.toString(lat));
                    }
                    
                    if (lon < -180.0 || lon > 180.0)
                    {
                        lcStmt.setObject(2, null);
                        lcStmt.setObject(5, null);
                    } else
                    {
                        lcStmt.setDouble(2, lon);
                        lcStmt.setString(5, Double.toString(lon));
                        
                        lcUpStmt.setDouble(2, lon);
                        lcUpStmt.setString(5, Double.toString(lon));
                    }
                    
                    String locName = null;
                    String fullName = null;
                    
                    Integer locId = null;
                    geoId = buildTags.getGeography(country, state, null);
                    if (geoId != null)
                    {
                        fullName = geoFullNameHash.get(geoId);
                        if (fullName == null)
                        {
                            fullName = BasicSQLUtils.querySingleObj("SELECT FullName FROM geography WHERE GeographyID = " + geoId);
                            geoFullNameHash.put(geoId, fullName);
                        }
                        
                        if (StringUtils.isNotEmpty(city))
                        {
                            locName = city + ", " + fullName;
                        } else
                        {
                            locName = fullName;
                        }
                        locId = localityHash.get(locName);
                        if (locId == null)
                        {
                            lcStmt2.setString(1, "%"+city);
                            lcStmt2.setString(2, country+"%");
                            ResultSet lcRS = lcStmt2.executeQuery();
                            if (lcRS.next())
                            {
                                locId = lcRS.getInt(1);
                                if (!lcRS.wasNull())
                                {
                                    localityHash.put(locName, locId);
                                }
                            }
                            lcRS.close();
                        }

                    } else
                    {
                        //unknown++;
                        fullName = "Unknown";
                        locName = buildTags.buildLocalityName(city, fullName);
                        geoId = 27507; // Unknown
                        locId = localityHash.get(locName);
                        //log.error("Couldn't find matching geography["+country+", "+state+", "+county+"]");
                    }
                    
                    if (locId == null)
                    {
                        lcStmt.setByte(3, (byte)0);
                        lcStmt.setString(6, "Point");
                        lcStmt.setInt(7, dspId);
                        lcStmt.setString(8, getLocalityName(country, state, null, city));
                        lcStmt.setObject(9, geoId);
                        lcStmt.setTimestamp(10, ts);
                        lcStmt.setInt(11, 1);
                        lcStmt.setInt(12, 1);
                        lcStmt.executeUpdate();
                        locId = BasicSQLUtils.getInsertedId(lcStmt);
                        
                    } else if (!isLatNull && !isLonNull)
                    {
                        int count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM locality WHERE Latitude1 IS NULL AND Longitude1 IS NULL AND LocalityID = "+locId);
                        if (count == 1)
                        {
                            lcUpStmt.setByte(3, (byte)0);
                            lcUpStmt.setString(6, "Point");
                            lcUpStmt.setInt(7, locId);
                            lcUpStmt.executeUpdate();
                        }
                    }
                    
                    // (StartDate, Method, DisciplineID, LocalityID
                    ceStmt.setDate(1, date);
                    ceStmt.setInt(2, dspId);
                    ceStmt.setInt(3, locId);
                    ceStmt.setTimestamp(4, ts);
                    ceStmt.setInt(5, 1);
                    ceStmt.setInt(6, 1);
                    ceStmt.executeUpdate();
                    Integer ceId = BasicSQLUtils.getInsertedId(ceStmt);
                    
                    //(CatalogNumber, FieldNumber, Text1, Remarks, CollectionID, CollectionMemberId
                    coStmt.setString(1, String.format("%09d", recNum++));
                    coStmt.setString(2, tag);
                    coStmt.setString(3, gender);
                    coStmt.setString(4, dir);
                    coStmt.setString(5, dist);
                    coStmt.setInt(6, colId);
                    coStmt.setInt(7, colId);
                    coStmt.setInt(8, ceId);
                    coStmt.setTimestamp(9, ts);
                    coStmt.setInt(10, 1);
                    coStmt.setInt(11, 1);
                    coStmt.executeUpdate();
                    //Integer coId = BasicSQLUtils.getInsertedId(coStmt);
                    
                    Integer agentId = getAgentId(agentStmt, rep_first, rep_last);
                    if (agentId == null)
                    {
                        agStmt.setInt(1, 0);
                        agStmt.setString(2, rep_first);
                        agStmt.setString(3, rep_last);
                        agStmt.setTimestamp(4, ts);
                        agStmt.setInt(5, 1);
                        agStmt.setInt(6, 1);
                        agStmt.executeUpdate();
                        agentId = BasicSQLUtils.getInsertedId(agStmt);
                        
                        
                        if (agentId != null)
                        {
                            adStmt.setString(1, rep_city);
                            adStmt.setString(2, rep_state);
                            adStmt.setString(3, rep_zip);
                            adStmt.setString(4, rep_country);
                            adStmt.setInt(5, agentId);
                            adStmt.setTimestamp(6, ts);
                            adStmt.setInt(7, 1);
                            adStmt.setInt(8, 1);
                            adStmt.executeUpdate();
                        } else
                        {
                            log.error("agentId is null after being created: "+rep_first+", "+rep_last);
                        }
                    }
                    
                    // OrderIndex, IsPrimary, CollectingEventID, DivisionID, AgentID
                    clStmt.setInt(1, 0);
                    clStmt.setBoolean(2, true);
                    clStmt.setInt(3, ceId);
                    clStmt.setInt(4, divId);
                    clStmt.setInt(5, agentId);
                    clStmt.setTimestamp(6, ts);
                    clStmt.setInt(7, 1);
                    clStmt.setInt(8, 1);
                    clStmt.executeUpdate();
                    
                } catch (Exception ex)
                {
                    log.debug(recNum+" tag["+tag+"]");
                    ex.printStackTrace();
                }
                
                cnt++;
                if (cnt % 100 == 0)
                {
                    System.out.println("Col Obj: "+cnt);
                }
            }
            
            coStmt.close();
            ceStmt.close();
            lcStmt.close();
            clStmt.close();
            rlStmt.close();
            agStmt.close();
            adStmt.close();
            lcUpStmt.close();
            
            buildTags.cleanup();
        }
    }
    
    /**
     * 
     */
    public void buildColRels()
    {
        int cnt = 0;
        try
        {
            Calendar  cal = Calendar.getInstance();
            Timestamp ts  = new Timestamp(cal.getTime().getTime());
            
            String common = "TimestampCreated, Version, CreatedByAgentID";
            String rlStr = String.format("INSERT INTO collectionrelationship (collectionRelTypeID, LeftSideCollectionID, RightSideCollectionID, %s) VALUES(?,?,?,?,?,?)", common);
            PreparedStatement rlStmt = dbConn.prepareStatement(rlStr);
            
            String sql = "SELECT c1.CollectionObjectID, c2.CollectionObjectID FROM collectionobject AS c1 " +
                         "Inner Join collectionobject AS c2 ON c1.FieldNumber = c2.FieldNumber WHERE c1.CollectionID =  4 AND c2.CollectionID <>  4";
            Statement stmt = dbConn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                int id1 = rs.getInt(1);
                int id2 = rs.getInt(2);
                
                rlStmt.setInt(1, 1); // ColRelType
                rlStmt.setInt(2, id1);
                rlStmt.setInt(3, id2);
                rlStmt.setTimestamp(4, ts);
                rlStmt.setInt(5, 1);
                rlStmt.setInt(6, 1);
                rlStmt.executeUpdate();
                
                cnt++;
                if (cnt % 1000 == 0)
                {
                    System.out.println("Col Obj Rel: "+cnt);
                }

            }
            
            rs.close();
            rlStmt.close();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
   /**
     * Setup all the System properties. This names all the needed factories. 
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
        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.utilapps.LocalDiskUIFieldFormatterMgr");    // Needed for CatalogNumberign
        System.setProperty(DataObjFieldFormatMgr.factoryName,           "edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr");     // Needed for WebLnkButton //$NON-NLS-1$
    }
    
    /** 
     * Drops, Creates and Builds the Database.
     * 
     * @throws SQLException
     * @throws IOException
     */
    public boolean setupDatabase(final DatabaseDriverInfo driverInfo,
                                 final String hostName, 
                                 final String dbName, 
                                 final String username, 
                                 final String password)
    {
        
        log.info("Logging into "+dbName+"....");
        
        String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, hostName, dbName);
        if (connStr == null)
        {
            connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, dbName);
        }
        
        if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                driverInfo.getDialectClassName(), 
                dbName, 
                connStr, 
                username, 
                password))
        {
            log.info("Login Failed!");
            return false;
        }         
        
        session    = HibernateUtil.getCurrentSession();
        hibSession = new HibernateDataProviderSession(session);
        
        //SpecifyAppPrefs.initialPrefs();
        
        return true;
    }

    
    /**
     * 
     */
    public void setUp()
    {
        UIRegistry.setAppName("Specify");
        
        //UIRegistry.setJavaDBDir(derbyPath != null ? derbyPath : UIRegistry.getDefaultWorkingPath() + File.separator + "DerbyDatabases");
        
        setUpSystemProperties();
        
        DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
        setupDatabase(driverInfo, "localhost", "monarchs", "root", "root");
    }
    /**
     * 
     */
    public void cleanup()
    {
        try
        {
            HibernateUtil.closeSession();
            
            if (dbConn != null) dbConn.close();
            if (dbConn2 != null) dbConn2.close();
            if (srcDBConn != null) srcDBConn.close();
            if (srcDBConn2 != null) srcDBConn2.close();
            
            if (geoStmt1 != null) geoStmt1.close();
            if (geoStmt2 != null) geoStmt2.close();
            if (agentStmt != null) agentStmt.close();
            if (tagStmt != null) tagStmt.close();
            
            if (session != null)
            {
                session.close();
                DBConnection.getInstance().close();
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * @return the dbConn
     */
    public Connection getDBConn()
    {
        return dbConn;
    }

    /**
     * @return the srcDBConn
     */
    public Connection getSrcDBConn()
    {
        return srcDBConn;
    }

    //------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        try
        {
            BuildFromRecovery awg = new BuildFromRecovery();
            awg.setUp();
            awg.createDBConnection("localhost", "3306", "monarchs", "root", "root");
            awg.createSrcDBConnection("localhost", "3306", "monarch", "root", "root");
            awg.process();
            //awg.buildColRels();
            awg.cleanup();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
