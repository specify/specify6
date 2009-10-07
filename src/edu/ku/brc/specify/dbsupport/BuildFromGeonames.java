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
package edu.ku.brc.specify.dbsupport;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.db.BackupServiceFactory;
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
    
    protected GeographyTreeDef           geoDef;
    protected String                     nowStr;
    protected String                     insertSQL = null;
    protected StringBuilder              sql       = new StringBuilder();
    protected Agent                      createdByAgent;
    protected ProgressFrame              frame;
    
    protected String                     itUsername;
    protected String                     itPassword;
    protected Connection                 readConn = null;
    protected Connection                 updateConn;
    
    protected ArrayList<Object>          rowData = new ArrayList<Object>();
    
    protected Hashtable<String, String>  countryToContHash     = new Hashtable<String, String>();
    protected Hashtable<String, Integer> contToIdHash          = new Hashtable<String, Integer>();
    protected Hashtable<String, String>  continentNameFromCode = new Hashtable<String, String>();
    
    protected Hashtable<String, Hashtable<String, Integer>>  countryStateCodeToIdHash = new Hashtable<String, Hashtable<String, Integer>>();
    
    protected Hashtable<String, String>  stateToCountryHash = new Hashtable<String, String>();
    protected Hashtable<String, Integer> countryCodeToIdHash    = new Hashtable<String, Integer>();
    
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
                             final String             nowStr,
                             final Agent              createdByAgent,
                             final String             itUsername,
                             final String             itPassword,
                             final ProgressFrame      frame)
    {
        super();
        this.geoDef         = geoDef;
        this.nowStr         = nowStr;
        this.createdByAgent = createdByAgent;
        this.itUsername     = itUsername;
        this.itPassword     = itPassword;
        this.frame          = frame;
        
        insertSQL = "INSERT INTO geography (Name, RankID, ParentID, IsAccepted, IsCurrent, GeographyTreeDefID, GeographyTreeDefItemID, " +
                    "CreatedByAgentID, CentroidLat, CentroidLon, Abbrev, " +
                    "TimestampCreated, TimestampModified, Version) VALUES (";
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
        sql.setLength(0); // because of debugging
        sql.append(insertSQL);
        
        Statement    stmt       = null;
        Statement    updateStmt = null;
        try
        {
            DBConnection currDBConn = DBConnection.getInstance();
            if (updateConn == null)
            {
                updateConn = currDBConn.createConnection();
            }
            readConn = currDBConn.createConnection();
            
            stmt       = readConn.createStatement();
            updateStmt = updateConn.createStatement();
            
            Integer count = BasicSQLUtils.getCount(readConn, "SELECT COUNT(*) FROM geoname");
            if (frame != null)
            {
                frame.setProcess(0, count);
                frame.setDesc("Creating Geography...");
            }
            
            Hashtable<String, String> continentCodeFromName = new Hashtable<String, String>();
            ResultSet rs = stmt.executeQuery("SELECT code, name from continentCodes");
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
            
            updateConn.setAutoCommit(false);
            
            if (frame != null)
            {
                frame.setDesc("Creating Continents..."); // I18N
            }
            
            int cnt = 0;
            
            //////////////////////
            // Continent
            //////////////////////
            String sqlStr = "SELECT continentCodes.name, geoname.latitude, geoname.longitude, continentCodes.code FROM geoname Inner Join continentCodes ON geoname.name = continentCodes.name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (frame != null)
                {
                    frame.setProcess(cnt);
                }
                
                if (buildInsert(rs, 100, earthId))
                {
                    log.debug(sql.toString());
                    @SuppressWarnings("unused")
                    int rv = updateStmt.executeUpdate(sql.toString());
                    
                    Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                    contToIdHash.put(rs.getString(4), newId);
                    
                    updateConn.commit();
                }
                sql.setLength(insertSQL.length());
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
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
            
            if (frame != null)
            {
                frame.setDesc("Creating Countries...");
            }
            
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
            
            // Now create ll the countries in the geoname table
            sqlStr = "SELECT name, latitude, longitude, country FROM geoname WHERE fcode = 'PCLI' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (buildInsert(rs, 200, earthId))
                {
                    @SuppressWarnings("unused")
                    int rv = updateStmt.executeUpdate(sql.toString());
                    
                    Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                    countryCodeToIdHash.put(rs.getString(4), newId);
                    updateConn.commit();
                }
                
                sql.setLength(insertSQL.length());
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
                }

            }
            rs.close();
            
            // Create an Countries that referenced in the geoname table
            rs = stmt.executeQuery("SELECT name, iso_alpha2 AS CountryCode, continent FROM countryinfo ORDER BY continent, iso_alpha2");
            while (rs.next())
            {
                String countryCode   = rs.getString(2);
                String continentCode = rs.getString(3);
                
                if (BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM geography WHERE RankID = 200 AND Abbrev = '"+countryCode+"'") == 0)
                {
                    String countryName = rs.getString(1);
                    
                    log.error("Adding country["+countryName+"]");
                    
                    createCountry(countryName, countryCode, continentCode, 200);
                    @SuppressWarnings("unused")
                    int rv = updateStmt.executeUpdate(sql.toString());
                    
                    Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                    countryCodeToIdHash.put(countryCode, newId);
                    
                    updateConn.commit();
                    sql.setLength(insertSQL.length());
                }
            }
            rs.close();
            
            if (frame != null)
            {
                frame.setDesc("Creating States...");
            }
            
            //////////////////////
            // States
            //////////////////////
            sqlStr = "SELECT name, latitude, longitude, country, admin1 as StateCode FROM geoname WHERE fcode = 'ADM1' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (buildInsert(rs, 300, earthId))
                {
                    String nameStr     = rs.getString(1);
                    String countryCode = rs.getString(4);
                    String stateCode   = rs.getString(5);
                    
                    stateToCountryHash.put(nameStr, countryCode);
                    
                    @SuppressWarnings("unused")
                    int     rv    = updateStmt.executeUpdate(sql.toString());
                    Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                    
                    Hashtable<String, Integer> stateToIdHash = countryStateCodeToIdHash.get(countryCode);
                    if (stateToIdHash != null)
                    {
                        stateToIdHash.put(stateCode, newId);
                    } else
                    {
                        log.error("****** Error - No State for code ["+stateCode+"]  Country: "+countryCode+"   Name: "+nameStr);
                    }
    
                    updateConn.commit();
                }
                
                sql.setLength(insertSQL.length());
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
                }

            }
            rs.close();
            
            // Create States that are referenced by Counties in Countries
            sqlStr = "SELECT name AS CountyName, latitude, longitude, country, admin1 as StateCode FROM geoname WHERE fcode = 'ADM2' ORDER BY name";
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
                                       
                    if (buildInsert(rowData, 300, earthId))
                    {
                        stateToCountryHash.put(stateCode, countryCode);
                        
                        log.debug("Adding State ["+rs.getString(1)+"]["+stateCode+"] for Country ["+countryCode+"]");
                        
                        @SuppressWarnings("unused")
                        int     rv    = updateStmt.executeUpdate(sql.toString());
                        Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                        
                        stateToIdHash.put(stateCode, newId);
                        
                        updateConn.commit();
                    }
                    sql.setLength(insertSQL.length());
                }
                
                /*cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
                }*/

            }
            rs.close();
            if (frame != null)
            {
                frame.setDesc("Creating Counties...");
            }
            
            //////////////////////
            // County
            //////////////////////
            sqlStr = "SELECT name, latitude, longitude, country, admin1 as StateCode FROM geoname WHERE fcode = 'ADM2' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (buildInsert(rs, 400, earthId))
                {
                    @SuppressWarnings("unused")
                    int rv = updateStmt.executeUpdate(sql.toString());
                    updateConn.commit();
                }
                
                sql.setLength(insertSQL.length());
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
                }

            }
            rs.close();
            
            if (frame != null)
            {
                frame.setProcess(count);
            }
            
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
                if (updateStmt != null)
                {
                    updateStmt.close();
                }
                if (readConn != null)
                {
                    readConn.close();
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
        
        if (rankId == 100) // Country
        {
            parentId = earthId;
            abbrev   = row.get(3).toString();
            
        } else if (rankId == 200) // Country
        {
            String countryCode   = row.get(3).toString();
            String continentCode = countryToContHash.get(countryCode);
            
            abbrev = countryCode;
            
            if (continentCode != null)
            {
                parentId = contToIdHash.get(continentCode);
                if (parentId == null)
                {
                    log.error("No Continent Id for Name  continentCode["+continentCode+"]   Country["+nameStr+"]");
                }
            } else
            {
                StringBuilder sb = new StringBuilder("No Continent Code ["+continentCode+":\n");
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
            
            parentId = countryCodeToIdHash.get(countryCode);
            if (parentId == null)
            {
                log.error("No Country Code Id for ["+countryCode+"]["+nameStr+"]");
            }
            
        } else if (rankId == 400) // County
        {
            String stateCode   = row.get(4).toString();
            String countryCode = row.get(3).toString();
            
            abbrev = row.get(3).toString();
            
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
            Double lat = ((BigDecimal)row.get(1)).doubleValue();
            Double lon = ((BigDecimal)row.get(2)).doubleValue();
            
            sql.append("\"");
            sql.append(nameStr);
            sql.append("\",");
            sql.append(rankId);
            sql.append(",");
            sql.append(parentId != null ? parentId.toString() : "NULL");
            sql.append(", TRUE, TRUE, ");
            sql.append(geoDef.getId());
            sql.append(",");
            sql.append(geoDefItemId);
            sql.append(",");
            sql.append(createdByAgent == null ? 1 : createdByAgent.getId());
            sql.append(",");
            sql.append(lat > -181 ? lat.toString() : "NULL"); // Lat
            sql.append(",");
            sql.append(lon > -181 ? lon.toString() : "NULL"); // Lon
            sql.append(",");
            
            sql.append(StringUtils.isNotEmpty(abbrev) ? ("\"" + abbrev + "\""): "NULL"); // Abbrev
            sql.append(",\"");
            sql.append(nowStr);
            sql.append("\",\"");
            sql.append(nowStr);
            sql.append("\", 0)");
            return true;
        }
        return false;
    }
    
    /**
     * @param nameStr
     * @param countryCode
     * @param continentCode
     * @param rankId
     */
    private void createCountry(final String nameStr, final String countryCode, final String continentCode, final int rankId)
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
            sql.append("\"");
            sql.append(nameStr);
            sql.append("\",");
            sql.append(rankId);
            sql.append(",");
            sql.append(parentId != null ? parentId.toString() : "NULL");
            sql.append(", TRUE, TRUE, ");
            sql.append(geoDef.getId());
            sql.append(",");
            sql.append(geoDefItemId);
            sql.append(",");
            sql.append(createdByAgent == null ? 1 : createdByAgent.getId());
            sql.append(",NULL, NULL,'");
            sql.append(countryCode); // Abbrev
            sql.append("',\"");
            sql.append(nowStr);
            sql.append("\",\"");
            sql.append(nowStr);
            sql.append("\", 0)");
            
        } else
        {
            log.error("parentId is NULL ["+continentCode+"]");
        }
    }
    
    /**
     * Unzips and loads the SQL backup of the geonames database needed for building the full geography tree.
     * @return true if build correctly.
     */
    public boolean loadGeoNamesDB()
    {
        try
        {
            DBConnection currDBConn = DBConnection.getInstance();
            String       dbName     = currDBConn.getDatabaseName();
            DBMSUserMgr.DBSTATUS status = DBMSUserMgr.checkForDB(dbName, currDBConn.getServerName(), itUsername, itPassword);
            
            if (status == DBMSUserMgr.DBSTATUS.missingOrEmpty)
            {
                DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
                
                if (dbMgr != null)
                {
                    if (dbMgr.connectToDBMS(itUsername, itPassword, currDBConn.getServerName()))
                    {
                        if (!dbMgr.createDatabase(dbName))
                        {
                            UIRegistry.showLocalizedError("ERROR_CRE_GEODB", dbName);
                        }
                    } else
                    {
                        UIRegistry.showLocalizedError("ERROR_LOGIN_GEODB", dbName);
                    }
                    
                } else
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, new Exception("Couldn't create DBMSMgr"));
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, ex);
        }
        
        File file = new File(XMLHelper.getConfigDirPath("geonames.sql.zip"));
        if (file.exists())
        {
            BackupServiceFactory bsf = BackupServiceFactory.getInstance();
            bsf.setUsernamePassword(itUsername, itPassword);
            
            String dbName = DBConnection.getInstance().getDatabaseName();
            boolean status = bsf.doRestoreBulkDataInBackground(dbName, null, file.getAbsolutePath(), null, null, null, true, false); // true - does it asynchronously, 
            
            // Clear IT Username and Password
            bsf.setUsernamePassword(null, null);
            
            return status;
        }
        return false;
    }
}
