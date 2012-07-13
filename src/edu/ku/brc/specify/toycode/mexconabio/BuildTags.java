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
package edu.ku.brc.specify.toycode.mexconabio;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.IdHashMapper;
import edu.ku.brc.specify.conversion.IdMapperMgr;
import edu.ku.brc.specify.conversion.IdTableMapper;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Division;
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
public class BuildTags
{
    private final Logger log = Logger.getLogger(BuildTags.class);
    protected Session                      session = null;
    protected HibernateDataProviderSession hibSession = null;

    private Connection dbConn     = null;
    private Connection dbConn2    = null;
    private Connection srcDBConn  = null;
    private Connection srcDBConn2 = null;
    
    private PreparedStatement geoStmt1  = null;
    private PreparedStatement geoStmt2  = null;
    private PreparedStatement agentStmt = null;
    private PreparedStatement locStmt   = null;
    
    private StringBuilder sb = new StringBuilder();
    
    /**
     * 
     */
    public BuildTags()
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
    protected String buildLocalityName(final String...args)
    {
        sb.setLength(0);
        for (String str : args)
        {
            if (StringUtils.isNotEmpty(str))
            {
                if (sb.length() > 0) sb.append(", ");
                sb.append(str);
            }
        }
        return sb.length() == 0 ? "N/A" : sb.toString();
    }
    
    /**
     * @param localityName
     * @return
     * @throws SQLException
     */
    private Integer getLocalityId(final String localityName) throws SQLException
    {
        if (StringUtils.isNotEmpty(localityName))
        {
            locStmt.setString(1, localityName);
            return getId(locStmt);
        }
        return null;
    }
    
    /**
     * @param stmt
     * @return
     */
    private Integer getId(final PreparedStatement stmt)
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
    }
    
    /**
     * @param country
     * @param state
     * @param county
     * @return
     * @throws SQLException 
     */
    protected Integer getGeography(final String countryArg, 
                                   final String stateArg, 
                                   final String countyArg) throws SQLException
    {
        String sql;
        String country = countryArg == null ? "" : countryArg;
        String state   = stateArg   == null ? "" : stateArg;
        String county  = countyArg  == null ? "" : countyArg;

        
        Integer countryId = null;
        Integer stateId   = null;
        Integer countyId  = null;
        
        if (StringUtils.isNotEmpty(country))
        {
            geoStmt2.setInt(1, 200);
            geoStmt2.setString(2, country.toLowerCase());
            countryId = getId(geoStmt2);
        }
        
        if (StringUtils.isNotEmpty(state))
        {
            PreparedStatement stmt;
            if (countryId != null)
            {
                geoStmt1.setInt(1, 300);
                geoStmt1.setInt(2, countryId);
                geoStmt1.setString(3, state.toLowerCase());
                stmt = geoStmt1;
            } else
            {
                geoStmt2.setInt(1, 300);
                geoStmt2.setString(2, state.toLowerCase());
                stmt = geoStmt2;
            }
            stateId = getId(stmt);
        }
        
        if (StringUtils.isNotEmpty(county))
        {
            PreparedStatement stmt;
            if (stateId != null)
            {
                geoStmt1.setInt(1, 400);
                geoStmt1.setInt(2, stateId);
                geoStmt1.setString(3, county.toLowerCase());
                stmt = geoStmt1;
           } else
            {
                geoStmt2.setInt(1, 400);
                geoStmt2.setString(2, county.toLowerCase());
                stmt = geoStmt2;
            }
            countyId = getId(stmt);
        }
        
        Integer id = null;
        if (countyId != null)
        {
            id = countyId;
            
        } else if (stateId != null)
        {
            id = stateId;
            
        } else if (countryId != null)
        {
            id = countryId;
        }
        return id;
    }
    
    /**
     * @param first
     * @param last
     * @param state
     * @return
     * @throws SQLException 
     */
    private Agent getAgent(final String first, final String last, final String city, final String state) throws SQLException
    {
        agentStmt.setObject(1, first != null ? first.toLowerCase() : null);
        agentStmt.setObject(2, last != null  ? last.toLowerCase()  : null);
        agentStmt.setObject(3, city != null  ? city.toLowerCase()  : null);
        agentStmt.setObject(4, state != null ? state.toLowerCase() : null);
        
        Integer id = getId(agentStmt);
        if (id != null)
        {
            return hibSession.get(Agent.class, id);
        }
        return null;
    }
    
    /**
     * @throws SQLException 
     * 
     */
    public void initialPrepareStatements() throws SQLException
    {
        if (dbConn != null)
        {
            geoStmt1  = dbConn.prepareStatement("SELECT GeographyID FROM geography WHERE RankID = ? AND ParentID = ? AND LOWER(Abbrev) = ?");
            geoStmt2  = dbConn.prepareStatement("SELECT GeographyID FROM geography WHERE RankID = ? AND LOWER(Abbrev) = ?");
            agentStmt = dbConn.prepareStatement("SELECT a.AgentID FROM agent a INNER JOIN address ad ON a.AgentID = ad.AgentID WHERE LOWER(a.FirstName) = ? AND LOWER(LastName) = ? AND LOWER(ad.City) = ? AND LOWER(ad.State) = ?");
            locStmt   = dbConn.prepareStatement("SELECT LocalityID FROM locality WHERE LocalityName = ?");
        } else
        {
            log.error("dbConn is null!");
        }
    }
    
    /**
     * 
     */
    public void process() throws SQLException
    {
        int dupAgents   = 0;
        int dupLocality = 0;
        int unknown     = 0;
        
        boolean doAll = true;

        BasicSQLUtils.setDBConnection(dbConn);
        
        boolean doTrim = false;
        if (doTrim || doAll)
        {
            String trimNamesSQL = "UPDATE tagger SET first=TRIM(first),last=TRIM(last),company=TRIM(company),address1=TRIM(address1),address2=TRIM(address2),city=TRIM(city),state=TRIM(state)";
            BasicSQLUtils.update(srcDBConn2, trimNamesSQL);
            
            String removeQuote = "UPDATE tagger SET first=SUBSTRING_INDEX(first, '\"', -1),last=SUBSTRING_INDEX(last, '\"', -1),company=SUBSTRING_INDEX(company, '\"', -1),address1=SUBSTRING_INDEX(address1, '\"', -1)," +
            		             "address2=SUBSTRING_INDEX(address2, '\"', -1),city=SUBSTRING_INDEX(city, '\"', -1), state=SUBSTRING_INDEX(state, '\"', -1)";
            BasicSQLUtils.update(srcDBConn2, removeQuote);
            
            String trimNamesSQL2 = "UPDATE tag SET city=TRIM(city),county=TRIM(county),state=TRIM(state)";
            BasicSQLUtils.update(srcDBConn2, trimNamesSQL2);
            
            String removeQuote2 = "UPDATE tag SET city=SUBSTRING_INDEX(city, '\"', -1), county=SUBSTRING_INDEX(county, '\"', -1), state=SUBSTRING_INDEX(state, '\"', -1)";
            BasicSQLUtils.update(srcDBConn2, removeQuote2);
        }
        
        IdMapperMgr idMapperMgr = IdMapperMgr.getInstance();
        idMapperMgr.setDBs(srcDBConn2, dbConn);
        
        IdHashMapper agentMapper;
        
        Division   division   = (Division)session.get(Division.class, 2);
        
        initialPrepareStatements();
        
        BasicSQLUtils.update(srcDBConn, "UPDATE tag SET `Date` = null WHERE Date = '0000-00-00'");

        boolean doAgents = false;
        if (doAgents || doAll)
        {
            agentMapper = idMapperMgr.addHashMapper("agent_AgentID", null, false);
            
            String    sql  = "SELECT first, last, company, address1, address2, city, state, country, zip, phone, fax, enail, tnum FROM tagger";
            Statement stmt = srcDBConn.createStatement();
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            log.debug("Querying for Agents...");
            ResultSet rs = stmt.executeQuery(sql);
            int cnt = 0;
            while (rs.next())
            {
                String first   = rs.getString(1);
                String last    = rs.getString(2);
                String company = rs.getString(3);
                String addr1   = rs.getString(4);
                String addr2   = rs.getString(5);
                String city    = rs.getString(6);
                String state   = rs.getString(7);
                String country = rs.getString(8);
                String zip     = rs.getString(9);
                String phone   = rs.getString(10);
                String fax     = rs.getString(11);
                String email   = rs.getString(12);
                Integer oldId  = rs.getInt(13);
                
                if (oldId == null)
                {
                    log.error("Null primary Id: "+last+" "+first);
                    continue;
                }
                
                Agent   agent   = getAgent(first, last, city, state);
                Integer agentId = null;
                if (agent == null)
                {
                    agent = new Agent();
                    agent.initialize();
                    agent.setFirstName(first);
                    agent.setLastName(last);
                    agent.setEmail(email);
                    agent.setRemarks(company);
                    agent.setDivision(division);
                    
                    Address addr = new Address();
                    addr.initialize();
                    addr.setAddress(addr1);
                    addr.setAddress2(addr2);
                    addr.setCity(city);
                    addr.setState(state);
                    addr.setCountry(country);
                    addr.setPostalCode(zip);
                    addr.setPhone1(phone);
                    addr.setFax(fax);
                    
                    agent.getAddresses().add(addr);
                    addr.setAgent(agent);
                    
                    Transaction trans = null;
                    try
                    {
                        trans = session.beginTransaction();
                        session.saveOrUpdate(agent);
                        session.saveOrUpdate(addr);
                        trans.commit();
                        
                        agentId = agent.getId();
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        try
                        {
                            if (trans != null) trans.rollback();
                        } catch (Exception ex2) 
                        {
                            ex2.printStackTrace();
                        }
                    }
                } else
                {
                    agentId = agent.getId();
                    dupAgents++;
                    //System.out.println("Found Agent: "+first+", "+last);
                }
                agentMapper.put(oldId, agentId);

                cnt++;
                if (cnt % 500 == 0)
                {
                    System.out.println("Agents: "+cnt);
                }
                
                if (cnt % 400 == 0)
                {
                    HibernateUtil.closeSession();
                    session    = HibernateUtil.getCurrentSession();
                    hibSession = new HibernateDataProviderSession(session);
                }
            }
            rs.close();
            stmt.close();
            
            division = (Division)session.get(Division.class, 2);
     
        } else
        {
            //agentMapper = idMapperMgr.addTableMapper("agent", "AgentID", false);
            agentMapper = new IdTableMapper("agent", "agentid", null, false, false);
        }
        
        System.out.println("Duplicated Agent: "+dupAgents);
        
        boolean doTags = true;
        if (doTags || doAll)
        {
            HashMap<String, Integer> localityHash    = new HashMap<String, Integer>();
            HashMap<Integer, String> geoFullNameHash = new HashMap<Integer, String>();
            
            int divId = 2;
            int dspId = 3;
            int colId = 4;
            
            String sql = "SELECT t.tagid, t.`date`, t.wild, t.gender, t.city, t.county, t.state, t.country, t.zip, t.observations, t.lat, t.lon, t.sunangle, p.tnum " +
                         "FROM tag AS t  Inner Join page AS p ON t.page = p.page ";
            Statement stmt = srcDBConn.createStatement();
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            log.debug("Querying for Tags...");
            ResultSet rs = stmt.executeQuery(sql);
            int cnt = 0;
            log.debug("Done querying for Tags...");
            
            Calendar  cal = Calendar.getInstance();
            Timestamp ts  = new Timestamp(cal.getTime().getTime());
            
            String common = "TimestampCreated, Version, CreatedByAgentID";
            String coStr = String.format("INSERT INTO collectionobject (CatalogNumber, FieldNumber, Text1, Remarks, CollectionID, CollectionMemberId, CollectingEventID, %s) VALUES(?,?,?,?,?,?,?,?,?,?)", common);
            String ceStr = String.format("INSERT INTO collectingevent (StartDate, Method, DisciplineID, LocalityID, %s) VALUES(?,?,?,?,?,?,?)", common);
            String lcStr = String.format("INSERT INTO locality (Latitude1, Longitude1, SrcLatLongUnit, Lat1text, Long1text, LatLongType, DisciplineID, MaxElevation, LocalityName, GeographyID, %s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)", common);
            String clStr = String.format("INSERT INTO collector (OrderNumber, IsPrimary, CollectingEventID, DivisionID, AgentID, %s) VALUES(?,?,?,?,?,?,?,?)", common);
            
            PreparedStatement coStmt = dbConn.prepareStatement(coStr);
            PreparedStatement ceStmt = dbConn.prepareStatement(ceStr);
            PreparedStatement lcStmt = dbConn.prepareStatement(lcStr);
            PreparedStatement clStmt = dbConn.prepareStatement(clStr);

            int recNum = 1;
            while (rs.next())
            {
                String tag     = rs.getString(1);
                if (tag != null && tag.startsWith("ERR")) continue;
                
                Date   date    = rs.getDate(2);
                String wild    = rs.getString(3);
                String gender  = rs.getString(4);
                String city    = rs.getString(5);
                String county  = rs.getString(6);
                String state   = rs.getString(7);
                String country = rs.getString(8);
                //String zip     = rs.getString(9);
                String obs     = rs.getString(10);
                double lat     = rs.getDouble(11);
                double lon     = rs.getDouble(12);
                double angle   = rs.getDouble(13);
                Integer taggerId = rs.getInt(14);
                
                String locName = null;
                String fullName = null;
                
                Integer locId   = null;
                Integer geoId   = getGeography(country, state, county);
                if (geoId != null)
                {
                    //locName   = localityHash.get(geoId);
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
                    
                } else
                {
                    unknown++;
                    fullName = "Unknown";
                    locName = buildLocalityName(city, fullName);
                    geoId = 27507; // Unknown
                    locId = localityHash.get(locName);
                    //log.error("Couldn't find matching geography["+country+", "+state+", "+county+"]");
                }
                
                if (locId == null)
                {
                    lcStmt.setDouble(1, lat);
                    lcStmt.setDouble(2, lon);
                    lcStmt.setByte(3, (byte)0);
                    lcStmt.setString(4, Double.toString(lat));
                    lcStmt.setString(5, Double.toString(lon));
                    lcStmt.setString(6, "Point");
                    lcStmt.setInt(7, dspId);
                    lcStmt.setDouble(8, angle);
                    lcStmt.setString(9, locName);
                    lcStmt.setObject(10, geoId);
                    lcStmt.setTimestamp(11, ts);
                    lcStmt.setInt(12, 1);
                    lcStmt.setInt(13, 1);
                    lcStmt.executeUpdate();
                    locId = BasicSQLUtils.getInsertedId(lcStmt);
                    
                    localityHash.put(locName, locId);
                } else
                {
                    dupLocality++;
                }
                
                // (StartDate, Method, DisciplineID, LocalityID
                ceStmt.setDate(1, date);
                ceStmt.setString(2, wild);
                ceStmt.setInt(3, dspId);
                ceStmt.setInt(4, locId);
                ceStmt.setTimestamp(5, ts);
                ceStmt.setInt(6, 1);
                ceStmt.setInt(7, 1);
                ceStmt.executeUpdate();
                Integer ceId = BasicSQLUtils.getInsertedId(ceStmt);
                
                //(CatalogNumber, FieldNumber, Text1, Remarks, CollectionID, CollectionMemberId
                coStmt.setString(1, String.format("%09d", recNum++));
                coStmt.setString(2, tag);
                coStmt.setString(3, gender);
                coStmt.setString(4, obs);
                coStmt.setInt(5, colId);
                coStmt.setInt(6, colId);
                coStmt.setInt(7, ceId);
                coStmt.setTimestamp(8, ts);
                coStmt.setInt(9, 1);
                coStmt.setInt(10, 1);
                coStmt.executeUpdate();
                //Integer coId = BasicSQLUtils.getInsertedId(coStmt);
                
                //Integer coltrId = null;
                if (taggerId != null)
                {
                    Integer agentId = agentMapper.get(taggerId);
                    //System.out.println(agentId);
                    if (agentId != null)
                    {
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
                        //coltrId = BasicSQLUtils.getInsertedId(clStmt);
                        //BasicSQLUtils.getInsertedId(clStmt);
                        
                    } else
                    {
                        log.debug("Couldn't find Agent in DB for tagger id (tnum): "+taggerId+"  AgentID:: "+agentId);
                    }
                } else
                {
                    log.debug("Couldn't find Mapped Id for tagger id (tnum): "+taggerId);
                }
                
                cnt++;
                if (cnt % 1000 == 0)
                {
                    System.out.println("Col Obj: "+cnt);
                }
            }
            
            coStmt.close();
            ceStmt.close();
            lcStmt.close();
            clStmt.close();
            
            System.out.println("Duplicated Agent:      "+dupAgents);
            System.out.println("Duplicated Localities: "+dupLocality);
            System.out.println("Unknown Localities:    "+unknown);
            System.out.println("Localities:            "+BasicSQLUtils.getCountAsInt(dbConn, "SELECT COUNT(*) FROM locality"));
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
     * @param dbConn the dbConn to set
     */
    public void setDbConn(Connection dbConn)
    {
        this.dbConn = dbConn;
    }

    /**
     * @param dbConn2 the dbConn2 to set
     */
    public void setDbConn2(Connection dbConn2)
    {
        this.dbConn2 = dbConn2;
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
            
            if (geoStmt1 != null) geoStmt1.close();
            if (geoStmt2 != null) geoStmt2.close();
            if (agentStmt != null) agentStmt.close();
            if (locStmt != null) locStmt.close();
            
            if (dbConn != null) dbConn.close();
            if (dbConn2 != null) dbConn2.close();
            if (srcDBConn != null) srcDBConn.close();
            if (srcDBConn2 != null) srcDBConn2.close();
            
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
            BuildTags awg = new BuildTags();
            awg.setUp();
            awg.createDBConnection("localhost", "3306", "monarchs", "root", "root");
            awg.createSrcDBConnection("localhost", "3306", "tags", "root", "root");
            awg.process();
            awg.cleanup();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
}
