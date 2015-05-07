/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.dbsupport;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.db.BackupServiceFactory;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIRegistry;

/**
 * Can't use PrepareStatment because of MySQL boolean bit issue.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Aug 16, 2009
 *
 */
public class BuildFromGeonames
{
    private static final Logger  log      = Logger.getLogger(BuildFromGeonames.class);
    
    private final static String                 CNT_SQL            = "SELECT COUNT(*) FROM geoname";
    public  final static String                 GEONAMES_DATE_PREF = "GEONAMES_LAST_MOD";
    
    protected GeographyTreeDef           geoDef;
    protected Timestamp                  now;
    protected String                     insertSQL = null;
    protected Agent                      createdByAgent;
    protected ProgressFrame              frame;
    
    protected String                     itUsername;
    protected String                     itPassword;
    protected Connection                 readConn = null;
    protected Connection                 updateConn;
    protected PreparedStatement          pStmt    = null;
    
    protected ArrayList<Object>          rowData = new ArrayList<Object>();
    
    protected Hashtable<String, String>  countryToContHash     = new Hashtable<String, String>();
    protected Hashtable<String, Integer> contToIdHash          = new Hashtable<String, Integer>();
    protected Hashtable<String, String>  continentNameFromCode = new Hashtable<String, String>();
    
    protected Hashtable<String, Hashtable<String, Integer>>  countryStateCodeToIdHash = new Hashtable<String, Hashtable<String, Integer>>();
    
    protected Hashtable<String, Integer> countryCodeToIdHash   = new Hashtable<String, Integer>();
    
    /**
     * Constructor.
     * @param geoDef
     * @param nowStr
     * @param createdByAgent
     * @param itUsername
     * @param itPassword
     * @param frame
     */
    public BuildFromGeonames(final GeographyTreeDef   geoDef, 
                             final Timestamp          now,
                             final Agent              createdByAgent,
                             final String             itUsername,
                             final String             itPassword,
                             final ProgressFrame      frame)
    {
        super();
        this.geoDef         = geoDef;
        this.now            = now;
        this.createdByAgent = createdByAgent;
        this.itUsername     = itUsername;
        this.itPassword     = itPassword;
        this.frame          = frame;
        
        insertSQL = "INSERT INTO geography (Name, RankID, ParentID, IsAccepted, IsCurrent, GeographyTreeDefID, GeographyTreeDefItemID, " +
                    "CreatedByAgentID, CentroidLat, CentroidLon, Abbrev, " +
                    "TimestampCreated, TimestampModified, Version, GeographyCode) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    /**
     * Builds the root Geography record (node).
     * @param session the current session
     * @return the root
     */
    public Geography buildEarth(final Session session)
    {
        try
        {
         // setup the root Geography record (planet Earth)
            Geography earth = new Geography();
            earth.initialize();
            earth.setName("Earth");
            earth.setFullName("Earth");
            earth.setNodeNumber(1);
            earth.setHighestChildNodeNumber(1);
            earth.setRankId(0);
            earth.setDefinition(geoDef);
            
            GeographyTreeDefItem defItem = geoDef.getDefItemByRank(0);
            
            earth.setDefinitionItem(defItem);
            defItem.getTreeEntries().add(earth);
            
            session.saveOrUpdate(earth);
            
            return earth;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            
        }
        return null;
    }
    

    
    /**
     * Builds the Geography tree from the geonames table.
     * @param earthId the id of the root.
     * @return true on success
     */
    public boolean build(final int earthId)
    {
        Statement    stmt       = null;
        try
        {
            DBConnection currDBConn = DBConnection.getInstance();
            if (updateConn == null)
            {
                updateConn = currDBConn.createConnection();
            }
            pStmt = updateConn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            
            readConn = currDBConn.createConnection();
            
            stmt = readConn.createStatement();
            
            Integer count = BasicSQLUtils.getCount(readConn, CNT_SQL);
            doProgress(0, count, "Initializing Reference Geography...");
            
            Hashtable<String, String> continentCodeFromName = new Hashtable<String, String>();
            ResultSet rs = stmt.executeQuery("SELECT code, name from continentcodes");
            while (rs.next())
            {
                continentNameFromCode.put(rs.getString(1), rs.getString(2));
                continentCodeFromName.put(rs.getString(2), rs.getString(1));
            }
            rs.close();
            
            /*if (false) // For testing
            {
                int delCnt = BasicSQLUtils.update(currConn, "DELETE FROM geography WHERE GeographyID > 1 AND RankId = 400");
                log.debug("Deleted "+delCnt+" geography records.");
                delCnt = BasicSQLUtils.update(currConn, "DELETE FROM geography WHERE GeographyID > 1 AND RankId = 300");
                log.debug("Deleted "+delCnt+" geography records.");
                delCnt = BasicSQLUtils.update(currConn, "DELETE FROM geography WHERE GeographyID > 1 AND RankId = 200");
                log.debug("Deleted "+delCnt+" geography records.");
                delCnt = BasicSQLUtils.update(currConn, "DELETE FROM geography WHERE GeographyID > 1 AND RankId = 100");
                log.debug("Deleted "+delCnt+" geography records.");
            }*/
            
            doProgress("Initializing Reference Continents..."); // I18N
            
            String earthSQL = "UPDATE geography SET GeographyCode='..' WHERE GeographyID = " + earthId;
            BasicSQLUtils.update(earthSQL);
            
            int cnt = 0;
            
            //////////////////////
            // Continent
            //////////////////////
            String sqlStr = "SELECT continentcodes.name, geoname.latitude, geoname.longitude, continentcodes.code FROM geoname Inner Join continentcodes ON geoname.name = continentcodes.name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                doProgress(cnt);
                
                if (buildInsert(rs, 100, earthId))
                {
                    pStmt.executeUpdate();
                    Integer newId = BasicSQLUtils.getInsertedId(pStmt);
                    contToIdHash.put(rs.getString(4), newId);
                }
                
                cnt++;
                if (cnt % 100 == 0)
                {
                    doProgress(cnt);
                }
            }
            rs.close();
            
            //////////////////////
            // Countries
            //////////////////////
            
            // Make hash of Country Codes
            HashSet<String> countryCodes = new HashSet<String>();
            rs = stmt.executeQuery("SELECT DISTINCT country FROM geoname WHERE fcode = 'PCLI'");
            while (rs.next())
            {
                countryCodes.add(rs.getString(1));
            }
            rs.close();
            
            doProgress("Initializing Reference Countries...");
            
            // First map all Countries to Continents
            rs = stmt.executeQuery("SELECT name, iso_alpha2 AS CountryCode, continent FROM countryinfo ORDER BY continent, iso_alpha2");
            while (rs.next())
            {
                String countryCode   = rs.getString(2);
                String continentCode = rs.getString(3);
                countryStateCodeToIdHash.put(countryCode, new Hashtable<String, Integer>());
                countryToContHash.put(countryCode, continentCode);
            }
            rs.close();
            
            // Now create all the countries in the geoname table
            sqlStr = "SELECT asciiname, latitude, longitude, country FROM geoname WHERE fcode LIKE 'PCL%' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (buildInsert(rs, 200, earthId))
                {
                    pStmt.executeUpdate();
                    Integer newId = BasicSQLUtils.getInsertedId(pStmt);
                    countryCodeToIdHash.put(rs.getString(4), newId);
                }
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    doProgress(cnt);
                }

            }
            rs.close();
            
            // Create an Countries that referenced in the geoname table
            rs = stmt.executeQuery("SELECT name, iso_alpha2 AS CountryCode, continent, iso_alpha2 FROM countryinfo ORDER BY continent, iso_alpha2");
            while (rs.next())
            {
                String countryCode   = rs.getString(2);
                String continentCode = rs.getString(3);
                
                if (BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM geography WHERE RankID = 200 AND Abbrev = '"+countryCode+"'") == 0)
                {
                    String countryName = rs.getString(1);
                    
                    log.error("Adding country["+countryName+"]["+countryCode+"]["+continentCode+"]");
                    
                    createCountry(countryName, countryCode, continentCode, 200);
                    
                    pStmt.executeUpdate();
                    Integer newId = BasicSQLUtils.getInsertedId(pStmt);
                    countryCodeToIdHash.put(countryCode, newId);
                }
            }
            rs.close();
            
            doProgress("Initializing Reference States...");
            
            //////////////////////
            // States
            //////////////////////
            sqlStr = "SELECT asciiname, latitude, longitude, country, admin1 as StateCode, ISOCode FROM geoname WHERE fcode = 'ADM1' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (buildInsert(rs, 300, earthId))
                {
                    String nameStr     = rs.getString(1);
                    String countryCode = rs.getString(4);
                    String stateCode   = rs.getString(5);
                    
                    pStmt.executeUpdate();
                    
                    Integer newId = BasicSQLUtils.getInsertedId(pStmt);
                    Hashtable<String, Integer> stateToIdHash = countryStateCodeToIdHash.get(countryCode);
                    if (stateToIdHash != null)
                    {
                        stateToIdHash.put(stateCode, newId);
                    } else
                    {
                        log.error("****** Error - No State for code ["+stateCode+"]  Country: "+countryCode+"   Name: "+nameStr);
                    }
                }
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    doProgress(cnt);
                }
            }
            rs.close();
            
            //-------------------------------------------------------------
            // Create States that are referenced by Counties in Countries
            //-------------------------------------------------------------
            sqlStr = "SELECT asciiname AS CountyName, latitude, longitude, country, admin1 as StateCode, ISOCode FROM geoname WHERE fcode = 'ADM2' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                String countryCode = rs.getString(4);
                String stateCode   = rs.getString(5);
                
                Hashtable<String, Integer> stateToIdHash = countryStateCodeToIdHash.get(countryCode);
                if (stateToIdHash != null && stateToIdHash.get(stateCode) == null)
                {
                    rowData.clear();
                    rowData.add(rs.getString(1));            // State Name, same as Code
                    rowData.add(new BigDecimal(-1000));
                    rowData.add(new BigDecimal(-1000));
                    rowData.add(countryCode);
                    rowData.add(stateCode);
                    rowData.add(rs.getString(6));
                                       
                    if (buildInsert(rowData, 300, earthId))
                    {
                        log.debug("Adding State ["+rs.getString(1)+"]["+stateCode+"] for Country ["+countryCode+"]");
                        
                        pStmt.executeUpdate();
                        Integer newId = BasicSQLUtils.getInsertedId(pStmt);
                        stateToIdHash.put(stateCode, newId);
                    }
                }
                
                /*cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    doProgress(cnt);
                }*/

            }
            rs.close();
            
            doProgress("Initializing Reference Counties...");
            
            //////////////////////
            // County
            //////////////////////
            sqlStr = "SELECT asciiname, latitude, longitude, country, admin1 as StateCode, ISOCode FROM geoname WHERE fcode = 'ADM2' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (buildInsert(rs, 400, earthId))
                {
                    pStmt.executeUpdate();
                }
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    doProgress(cnt);
                }
            }
            rs.close();
            
            doProgress(count);
            
            return true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, ex);

            try
            {
                updateConn.rollback();
                
            } catch (Exception exr)
            {
                exr.printStackTrace();
            }
        } finally
        {
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (readConn != null)
                {
                    readConn.close();
                }
                if (pStmt != null)
                {
                    pStmt.close();
                }
                if (updateConn != DBConnection.getInstance())
                {
                    updateConn.close();
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        if (frame != null)
        {
            frame.setVisible(false);
        }
        return false;
    }
    
    
    
    /**
     * @param rs
     * @param rankId
     * @param earthId
     * @return
     * @throws SQLException
     */
    private boolean buildInsert(final ResultSet rs, 
                                final int       rankId,
                                final int       earthId) throws SQLException
    {
        rowData.clear();
        for (int i=0;i<rs.getMetaData().getColumnCount();i++)
        {
            rowData.add(rs.getObject(i+1));
        }
        return buildInsert(rowData, rankId, earthId);
    }
    
    /**
     * @param row
     * @param rankId
     * @param earthId
     * @return
     * @throws SQLException
     */
    private boolean buildInsert(final List<Object> row, 
                                final int          rankId,
                                final int          earthId) throws SQLException
    {
        
        GeographyTreeDefItem item         = geoDef.getDefItemByRank(rankId);
        int                  geoDefItemId = item.getId();
        
        String nameStr = row.get(0).toString().trim();
        
        Integer parentId = null;
        String  abbrev   = null;
        String  isoCode  = null;
        
        if (rankId == 100) // Continent
        {
            parentId = earthId;
            abbrev   = row.get(3).toString();
            isoCode  = abbrev;
            
        } else if (rankId == 200) // Country
        {
            String countryCode   = row.get(3).toString();
            String continentCode = countryToContHash.get(countryCode);
            
            isoCode = (String)row.get(row.size()-1);
            abbrev  = countryCode;
            
            if (continentCode != null)
            {
                parentId = contToIdHash.get(continentCode);
                if (parentId == null)
                {
                    log.error("No Continent Id for Name  continentCode["+continentCode+"]   Country["+nameStr+"]");
                }
            } else
            {
                StringBuilder sb = new StringBuilder("No Continent Code ["+continentCode+"]:\n");
                for (int i=0;i<row.size();i++)
                {
                    sb.append(i+" - "+row.get(i)+"\n");
                }
                log.error(sb.toString());
            }
            
        } else if (rankId == 300) // State
        {
            String countryCode = row.get(3).toString();
            abbrev             = row.get(4).toString();
            isoCode            = (String)row.get(row.size()-1);
            
            parentId = countryCodeToIdHash.get(countryCode);
            if (parentId == null)
            {
                log.error("No Country Code Id for ["+countryCode+"]["+nameStr+"]");
            }
            
        } else if (rankId == 400) // County
        {
            String stateCode   = row.get(4).toString();
            String countryCode = row.get(3).toString();
            
            abbrev  = row.get(3).toString();
            isoCode = (String)row.get(row.size()-1);
            
            Hashtable<String, Integer> stateToIdHash = countryStateCodeToIdHash.get(countryCode);
            if (stateToIdHash != null)
            {
                parentId = stateToIdHash.get(stateCode);
                if (parentId == null)
                {
                    log.error("No State Id for CC["+countryCode+"]  stateCode["+stateCode+"] County["+nameStr+"]");
                }
            } else
            {
                log.error("No State Hash for CC["+countryCode+"]  State["+stateCode+"] Name: "+row.get(1));
            }
        }
        
        if (nameStr.length() > 64)
        {
            log.error("Name["+nameStr+" is too long "+nameStr.length() + "truncating.");
            nameStr = nameStr.substring(0, 64);
        }
        
        if (parentId != null)
        {
            Double lat = row.get(1) != null ? ((BigDecimal)row.get(1)).doubleValue() : null;
            Double lon = row.get(2) != null ? ((BigDecimal)row.get(2)).doubleValue() : null;
            
            pStmt.setString(1, nameStr);
            pStmt.setInt(2, rankId);
            pStmt.setInt(3, parentId);
            pStmt.setBoolean(4, true);
            pStmt.setBoolean(5, true);
            pStmt.setInt(6, geoDef.getId());
            pStmt.setInt(7, geoDefItemId);
            pStmt.setInt(8, createdByAgent == null ? 1 : createdByAgent.getId());
            pStmt.setBigDecimal(9, lat > -181 ? new BigDecimal(lat) : null); // Lat
            pStmt.setBigDecimal(10, lon > -181 ? new BigDecimal(lon) : null); // Lon
            
            pStmt.setString(11, StringUtils.isNotEmpty(abbrev) ? abbrev : null); // Abbrev
            pStmt.setTimestamp(12, now);
            pStmt.setTimestamp(13, now);
            pStmt.setInt(14, 0);
            pStmt.setString(15, isoCode);
            
            return true;
        }
        return false;
    }
    
    /**
     * @param nameStr
     * @param countryCode
     * @param continentCode
     * @param rankId
     * @throws SQLException 
     */
    private void createCountry(final String nameStr, final String countryCode, final String continentCode, final int rankId) throws SQLException
    {
        GeographyTreeDefItem item         = geoDef.getDefItemByRank(rankId);
        int                  geoDefItemId = item.getId();
        
        Integer parentId = null;
        
        parentId = contToIdHash.get(continentCode);
        if (parentId == null)
        {
            log.error("No Continent Id for continentCode["+continentCode+"]   Country["+nameStr+"]");
        }
    
        if (parentId != null || rankId == 100)
        {
            pStmt.setString(1, nameStr);
            pStmt.setInt(2, rankId);
            pStmt.setInt(3, parentId);
            pStmt.setBoolean(4, true);
            pStmt.setBoolean(5, true);
            pStmt.setInt(6, geoDef.getId());
            pStmt.setInt(7, geoDefItemId);
            pStmt.setInt(8, createdByAgent == null ? 1 : createdByAgent.getId());
            pStmt.setBigDecimal(9, null); // Lat
            pStmt.setBigDecimal(10, null); // Lon
            
            pStmt.setString(11, countryCode); // Abbrev
            pStmt.setTimestamp(12, now);
            pStmt.setTimestamp(13, now);
            pStmt.setInt(14, 0);
            pStmt.setString(15, countryCode);
            
        } else
        {
            log.error("parentId is NULL ["+continentCode+"]");
        }
    }
    
    /*private Object getCreatedDate(final File file)
    {
        Path file = ...;
        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

        System.out.println("creationTime: " + attr.creationTime());
        System.out.println("lastAccessTime: " + attr.lastAccessTime());
        System.out.println("lastModifiedTime: " + attr.lastModifiedTime());

        System.out.println("isDirectory: " + attr.isDirectory());
        System.out.println("isOther: " + attr.isOther());
        System.out.println("isRegularFile: " + attr.isRegularFile());
        System.out.println("isSymbolicLink: " + attr.isSymbolicLink());
        System.out.println("size: " + attr.size());
    }*/
    
    /**
     * @return the Globals prefs or null if the app isn't logged in.
     */
    public static AppPreferences getGlobalPrefs()
    {
        try
        {
            return AppPreferences.getGlobalPrefs();
        } catch (InternalError ex) 
        {
            return null;
        }
    }
    
    /**
     * @return the time in milliseconds when geonames was last upload.
     */
    public static Long getLastGeonamesBuiltTime()
    {
    	AppPreferences appPrefs = null;
    	try {
        	appPrefs = getGlobalPrefs();
        } catch(Exception ex) {
        	//in case of errors due to pending schema updates bug #9977
        	log.error(ex.getMessage());
        }
        return appPrefs != null ? appPrefs.getLong(GEONAMES_DATE_PREF, null) : null;
    }
    
    /**
     * Unzips and loads the SQL backup of the geonames database needed for building the full geography tree.
     * @param doAsynchronously whether it is done asynchronously in the background
     * @return true if build correctly.
     */
    public boolean loadGeoNamesDB(final String databaseName)
    {
        // See if date of file matches the last time the file was restored
        boolean isDatesMatch = true;
        final File file = new File(XMLHelper.getConfigDirPath("geonames.sql.zip"));
        //System.out.println(file.getAbsolutePath());
        if (file.exists())
        {
            Long lastModLong = getLastGeonamesBuiltTime();
            long prefMilli   = lastModLong != null ? lastModLong : 0;
            isDatesMatch     = file.lastModified() == prefMilli;
            
        } else
        {
            return false;
        }
        
        DBMSUserMgr  dbMgr         = null;
        boolean shouldLoadGeoNames = true;
        try
        {
            DBConnection currDBConn = DBConnection.getInstance();
            dbMgr = DBMSUserMgr.getInstance();
            if (dbMgr == null) return false;
            
            String dbName = databaseName != null ? databaseName : currDBConn.getDatabaseName();
            //DBMSUserMgr.DBSTATUS status = DBMSUserMgr.checkForDB(dbName, currDBConn.getServerName(), itUsername, itPassword); // opens and closes connection
            
            if (!dbMgr.connectToDBMS(itUsername, itPassword, currDBConn.getServerName(), dbName, currDBConn.isEmbedded()))
            {
                UIRegistry.showError("Unable to login as IT user. ");
                return false;
            }

            boolean isDBOk = true;
            //boolean isDBOk           = false;
            boolean isGeoNameTableOK = false;
            
            //XXX
            //For unknown reasons, on Windows 8, the checkForDB call above fails because the embedded-db directory structure appears to have not yet been created.
            //Currently, this method is not called from a context in which it would make sense for it to create the db the geonames are being added to if it did not already exist.
            //So it is probably safe to assume, that if we are here, the db exists, and this code is not necessary.
            //Removing fixes the Windows 8 issue.
            
            //if (status == DBMSUserMgr.DBSTATUS.missingDB)
           //{
            //    if (!dbMgr.createDatabase(dbName))
            //    {
            //        UIRegistry.showLocalizedError("ERROR_CRE_GEODB", dbName);
            //    } else
            //    {
            //        isDBOk = true;
            //    }
            //        
            //} else
            //{
            //    isDBOk = true;
            //}
            
            if (isDBOk)
            {
                if (dbMgr.doesDBHaveTable(dbName, "geoname"))
                {
                    isGeoNameTableOK = BasicSQLUtils.getCountAsInt(currDBConn.getConnection(), CNT_SQL) > 30000;
                }
                shouldLoadGeoNames = !isGeoNameTableOK || !isDatesMatch;
            } else
            {
                return false;
            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, ex);
            return false;
            
        } finally
        {
            if (dbMgr != null) dbMgr.close();
        }
        
        shouldLoadGeoNames = true;
        if (shouldLoadGeoNames)
        {
            BackupServiceFactory bsf = BackupServiceFactory.getInstance();
            bsf.setUsernamePassword(itUsername, itPassword);
            
            //String  dbName = DBConnection.getInstance().getDatabaseName();
            
            // 2nd to last 'true' - does it synchronously, last boolean 'false' means drop database
            DBConnection currDBConn = DBConnection.getInstance();
            String       dbName     = databaseName != null ? databaseName : currDBConn.getDatabaseName();
            boolean      status     = bsf.doRestoreBulkDataInBackground(dbName, null, file.getAbsolutePath(), null, null, null, true, false);
            if (status)
            {
            	//File createFile = bsf.getUnzippedFileByName("continentcodes.txt");
            	buildISOCodes();
            }
            
            AppPreferences appPrefs = getGlobalPrefs();
            if (appPrefs != null)
            {
            	appPrefs.putLong(GEONAMES_DATE_PREF, status ? file.lastModified() : 0);
            	try 
            	{
					appPrefs.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
            }
            // Clear IT Username and Password
            bsf.setUsernamePassword(null, null);
            
            return status;
        }
        return true;
    }
    
    /**
     * Needed when a new geonames file is gotten
     */
    @SuppressWarnings("unused")
    private void buildISOCodes()
    {
        try
        {
            DBConnection currDBConn = DBConnection.getInstance();
            String       dbName     = currDBConn.getDatabaseName();
            DBMSUserMgr  dbMgr      = DBMSUserMgr.getInstance();
            if (dbMgr != null)
            {
                boolean isConnected = dbMgr.connectToDBMS(itUsername, itPassword, currDBConn.getServerName(), dbName, currDBConn.isEmbedded());
                if (isConnected)
                {
                    Connection conn = dbMgr.getConnection();
                    Statement  stmt = null;
                    ResultSet  rs   = null;
                    PreparedStatement pStmt = null;
                    try
                    {
                        conn.setCatalog(dbName);
                        
                        boolean isFieldOK = true;
                        if (!dbMgr.doesFieldExistInTable("geoname", "ISOCode"))
                        {
                            if (BasicSQLUtils.update(conn, "ALTER TABLE geoname ADD COLUMN ISOCode VARCHAR(24) DEFAULT NULL") == 0)
                            {
                                isFieldOK = false;
                            }
                        } else 
                        {
                        	int numMissingISOCodes = BasicSQLUtils.getCountAsInt(currDBConn.getConnection(), "SELECT COUNT(*) FROM geoname WHERE ISOCode IS NULL");
                        	if (numMissingISOCodes == 0)
                        	{
                                dbMgr.close();
                                return;
                        	}
                        }
                        
                        if (isFieldOK)
                        {
                        	Vector<Object[]> rowData = BasicSQLUtils.query("SELECT code, name, geonameId FROM continentcodes");
                        	// Do Continents and Oceans
                            for (Object[] cols : rowData)
                            {
                            	String iso  = cols[0].toString();
                            	String name = cols[1].toString();
                            	StringBuilder str = new StringBuilder(String.format("UPDATE geoname SET ISOCode='%s' WHERE ", iso));
                            	String sql = "";
                            	if (name.contains("Ocean") && !name.contains("Oceania"))
                            	{
                            		str.append(String.format("asciiname LIKE '|%s' AND fcode = 'OCN'", name));
                            		sql = str.toString().replace("|", "%"); 
                            	} else
                            	{
                            		str.append("geonameId = " + cols[2]);
                            		sql = str.toString();
                            	}
                            	System.out.println(sql);
                                int rv = BasicSQLUtils.update(sql);
                                if (rv < 1)
                                {
                                	log.error("Can't update geoname: "+sql);
                                }
                            }
                            
                            int    cnt      = 0; 
                            String countStr = "SELECT COUNT(*) ";
                            String post     = " FROM geoname WHERE fcode = 'GULF' AND country IS NOT NULL AND LENGTH(country) > 0";
                            String pre      = "SELECT geonameId, country";
                            String sql      = pre + post;
                            
                            double totalCnt = BasicSQLUtils.getCountAsInt(conn, countStr + post);
                            doProgress(0, 100, "Initializing...");
                            
                            for (Object[] cols : BasicSQLUtils.query(sql))
                            {
                            	String id  = cols[0].toString();
                            	String iso = cols[1].toString();
                            	sql        = String.format("UPDATE geoname SET ISOCode='%s' WHERE geonameId = %s", iso, id);
                            	System.out.println(sql);
                                int rv = BasicSQLUtils.update(sql);
                                if (rv < 1)
                                {
                                	log.error("Can't update geoname: "+sql);
                                }
                                cnt++;
                                doProgress((int)((double)cnt / totalCnt * 100.0));
                            }
                            
                            post = " FROM geoname g WHERE ISOCode IS NULL ORDER BY g.country ASC, g.fcode DESC, g.admin1 ASC, g.admin2 ASC";
                            pre  = "SELECT g.geonameId, g.fcode, g.country, g.admin1, g.admin2";
                            sql  = pre + post;
                            
                            totalCnt = BasicSQLUtils.getCountAsInt(conn, "SELECT COUNT(*) "+post);
                            doProgress(0, 100, "Preparing data...");
                            
                            pStmt = conn.prepareStatement("UPDATE geoname SET ISOCode=? WHERE geonameId = ?");
                            stmt  = conn.createStatement();
                            rs    = stmt.executeQuery(sql);
                            
                            cnt        = 0;
                            boolean hasCountry = false;
                            boolean isOK       = true;
                            StringBuilder sb = new StringBuilder();
                            while (rs.next() && isOK)
                            {
                                String fcode = rs.getString(2);
                                if (!hasCountry)
                                {
                                    hasCountry = fcode.equals("PCLI");
                                }
                                
                                if (hasCountry)
                                {
                                    String country = rs.getString(3);
                                    if (StringUtils.isEmpty(country))
                                    {
                                        continue;
                                    }
                                    
                                    String state   = rs.getString(4);
                                    String county  = rs.getString(5);
                                    
                                    sb.setLength(0);
                                    if (StringUtils.isNotEmpty(country))
                                    {
                                        sb.append(country);
                                        if (!fcode.equals("PCLI") && StringUtils.isNotEmpty(state))
                                        {
                                            sb.append(state);
                                            if (StringUtils.isNotEmpty(county))
                                            {
                                                sb.append(county);
                                            }
                                        }
                                        pStmt.setString(1, sb.length() > 24 ? sb.substring(0, 24) : sb.toString());
                                        pStmt.setInt(2, rs.getInt(1));
                                        isOK = pStmt.executeUpdate() == 1;
                                    }
                                }
                                cnt++;
                                if (cnt % 2000 == 0)
                                {
                                    doProgress((int)((double)cnt / totalCnt * 100.0));
                                    //doProgress(cnt);
                                }
                            }
                        } else
                        {
                            UIRegistry.showLocalizedError("ERROR_ERR_GEODB", dbName);
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    } finally
                    {
                        try
                        {
                            if (rs != null) rs.close();
                            if (stmt != null) stmt.close();
                            if (pStmt != null) pStmt.close();

                        } catch (Exception ex) {}
                    }
                } else
                {
                    UIRegistry.showLocalizedError("ERROR_LOGIN_GEODB", dbName);
                }
                dbMgr.close();
                
            } else
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, new Exception("Couldn't create DBMSMgr"));
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, ex);
        }

    }
    
    private void doProgress(final int startProgress, final int endProgress, final String msg)
    {
        if (frame != null)
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                frame.setProcess(startProgress, endProgress);
                frame.setDesc(msg);
                
            } else
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        frame.setProcess(startProgress, endProgress);
                        frame.setDesc(msg);
                    }
                });
            }
        }
    }
    
    private void doProgress(final int progress)
    {
        if (frame != null)
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                frame.setProcess(progress);
                
            } else
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        frame.setProcess(progress);
                    }
                });
            }
        }
    }
    
    private void doProgress(final String msg)
    {
        if (frame != null)
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                frame.setDesc(msg);
                
            } else
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        frame.setDesc(msg);
                    }
                });
            }
        }
    }
}
