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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.db.BackupServiceFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.ui.ProgressFrame;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 16, 2009
 *
 */
public class BuildFromGeonames
{
    private static final Logger  log      = Logger.getLogger(BuildFromGeonames.class);
            
    protected GeographyTreeDef geoDef;
    protected String           nowStr;
    protected String           insertSQL = null;
    protected StringBuilder    sql       = new StringBuilder();
    protected Agent            createdByAgent;
    protected ProgressFrame    frame;
    
    protected String           itUsername;
    protected String           itPassword;
    protected Connection       connection;
    protected Connection       currConn;
    
    protected Hashtable<String, String>  countryToContHash     = new Hashtable<String, String>();
    protected Hashtable<String, String>  countryToAbbrevHash   = new Hashtable<String, String>();
    protected Hashtable<String, Integer> contToIdHash          = new Hashtable<String, Integer>();
    protected Hashtable<String, String>  continentNameFromCode = new Hashtable<String, String>();
    
    
    
    protected Hashtable<String, String>  stateToCountryHash = new Hashtable<String, String>();
    protected Hashtable<String, Integer> countryToIdHash    = new Hashtable<String, Integer>();
    
    protected Hashtable<String, String>  countyToStateHash = new Hashtable<String, String>();
    protected Hashtable<String, Integer> stateToIdHash    = new Hashtable<String, Integer>();
    
    /**
     * @param geoDef
     * @param nowStr
     * @param createdByAgent
     * @param connection
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
     * @param session
     * @param treeDef
     * @return
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
     * @return
     */
    public boolean build(final int earthId)
    {
        sql.setLength(0); // because of debugging
        sql.append(insertSQL);
        
        DBConnection geoDBConn  = null;
        Statement    stmt       = null;
        Statement    updateStmt = null;
        try
        {
            currConn = DBConnection.getInstance().createConnection();
            
            String dbName = "geonames";
            
            DBConnection currDBConn = DBConnection.getInstance();
            
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver(currDBConn.getDriverName());
            String             connStr    = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, currDBConn.getServerName(), dbName, itUsername, itPassword, driverInfo.getName());
            
            geoDBConn  = DBConnection.createInstance(currDBConn.getDriver(), currDBConn.getDialect(), "geonames", connStr, itUsername, itPassword);
            connection = geoDBConn.createConnection();
            
            stmt       = connection.createStatement();
            updateStmt = currConn.createStatement();
            
            Integer count = BasicSQLUtils.getCount(connection, "SELECT COUNT(*) FROM geoname");
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
            
            rs = stmt.executeQuery("SELECT g.name AS CountryFullName, ci.name AS Country, cc.name, cc.code AS Continent FROM geoname g Inner Join countryinfo ci ON g.country = ci.iso_alpha2 Inner Join continentCodes cc ON ci.continent = cc.code ORDER BY Country");
            while (rs.next())
            {
                //System.out.println("["+rs.getString(1)+"]["+rs.getString(2)+"]["+rs.getString(3)+"]["+rs.getString(4)+"]");
                countryToContHash.put(rs.getString(1), rs.getString(4));
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
            
            currConn.setAutoCommit(false);
            
            if (frame != null)
            {
                frame.setDesc("Creating Continents..."); // I18N
            }
            
            int cnt = 0;
            // Continent
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
                    contToIdHash.put(rs.getString(1), newId);
                    
                    currConn.commit();
                }
                sql.setLength(insertSQL.length());
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
                }
            }
            rs.close();
            
            // Make hash of Country Codes
            HashSet<String> countryCodes = new HashSet<String>();
            rs = stmt.executeQuery("SELECT DISTINCT country FROM geoname WHERE fcode = 'PCLI'");
            while (rs.next())
            {
                countryCodes.add(rs.getString(1));
            }
            rs.close();
            
            rs = stmt.executeQuery("SELECT iso_alpha2 FROM countryinfo ORDER BY iso_alpha2");
            while (rs.next())
            {
                String code = rs.getString(1);
                if (!countryCodes.contains(code))
                {
                    //System.out.println(code);
                    
                    Statement tmpStmt = connection.createStatement();
                    
                    String str = "SELECT name, iso_alpha2, continent FROM countryinfo  WHERE iso_alpha2 = '"+code+"'";
                    //log.debug(str);
                    ResultSet tmpRS = tmpStmt.executeQuery(str);
                    while (tmpRS.next())
                    {
                        createCountry(tmpRS.getString(1), tmpRS.getString(2), tmpRS.getString(3), 200);
                        @SuppressWarnings("unused")
                        int rv = updateStmt.executeUpdate(sql.toString());
                        
                        countryToAbbrevHash.put(tmpRS.getString(1), tmpRS.getString(2));
                        
                        Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                        countryToIdHash.put(code, newId);
                        
                        currConn.commit();
                    }
                    tmpRS.close();
                    tmpStmt.close();
                    sql.setLength(insertSQL.length());
                }
            }
            rs.close();
            
            if (frame != null)
            {
                frame.setDesc("Creating Countries...");
            }
            // Country
            sqlStr = "SELECT name, latitude, longitude, country FROM geonames.geoname WHERE fcode = 'PCLI' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                String nameStr = rs.getString(1);
                if (buildInsert(rs, 200, earthId))
                {
                    countryToAbbrevHash.put(nameStr, rs.getString(4));
                    
                    @SuppressWarnings("unused")
                    int rv = updateStmt.executeUpdate(sql.toString());
                    
                    Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                    countryToIdHash.put(rs.getString(4), newId);
                    
                    currConn.commit();
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
                frame.setDesc("Creating States...");
            }
            
            // STATE
            sqlStr = "SELECT name,  latitude, longitude, country FROM geonames.geoname WHERE fcode = 'ADM1' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (buildInsert(rs, 300, earthId))
                {
                    String nameStr = rs.getString(1);
                    stateToCountryHash.put(nameStr, rs.getString(4));
                    
                    @SuppressWarnings("unused")
                    int rv = updateStmt.executeUpdate(sql.toString());
                    Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                    stateToIdHash.put(nameStr, newId);
    
                    currConn.commit();
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
                frame.setDesc("Creating Counties...");
            }
            
            // COUNTY
            sqlStr = "SELECT name, latitude, longitude, country FROM geonames.geoname WHERE fcode = 'ADM2' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (buildInsert(rs, 400, earthId))
                {
                    @SuppressWarnings("unused")
                    int rv = updateStmt.executeUpdate(sql.toString());
                    currConn.commit();
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
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);

            try
            {
                currConn.rollback();
                
            } catch (Exception exr)
            {
                exr.printStackTrace();
            }
        } finally
        {
            try
            {
                stmt.close();
                connection.close();
                geoDBConn.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        return false;
    }
    
    /**
     * @param rs
     * @param geoDefItemId
     * @throws SQLException
     */
    private boolean buildInsert(final ResultSet rs, 
                                final int       rankId,
                                final int       earthId) throws SQLException
    {
        
        GeographyTreeDefItem item         = geoDef.getDefItemByRank(rankId);
        int                  geoDefItemId = item.getId();
        
        String nameStr = rs.getString(1).trim();
        
        Integer parentId = null;
        
        if (rankId == 100) // Country
        {
            parentId = earthId;
            
        } else if (rankId == 200) // Country
        {
            String continentCode = countryToContHash.get(nameStr);
            if (continentCode != null)
            {
                String continentName = continentNameFromCode.get(continentCode);
                if (continentName != null)
                {
                    parentId = contToIdHash.get(continentName);
                    if (parentId == null)
                    {
                        log.error("No Continent Id for Name  ["+continentName+"]   Country["+nameStr+"]");
                    }
                } else
                {
                    log.error("No Continent Name for Code ["+continentCode+"]");
                }
            } else
            {
                log.error("No Continent Code for Country Name["+nameStr+"]["+rs.getString(4)+"]");
            }
            
        } else if (rankId == 300) // State
        {
            String countryCode = rs.getString(4);
            
            parentId = countryToIdHash.get(countryCode);
            if (parentId == null)
            {
                log.error("No Country Code Id for ["+countryCode+"]["+nameStr+"]");
            }
            
        } else if (rankId == 400) // County
        {
            parentId = countryToIdHash.get(rs.getString(4));
            if (parentId == null)
            {
                log.error("No State Id for CC["+rs.getString(4)+"]  State["+nameStr+"]");
            }
        }
        
        if (nameStr.length() > 64)
        {
            log.error("Name["+nameStr+" is too long "+nameStr.length() + "truncating.");
            nameStr = nameStr.substring(0, 64);
        }
        
        if (parentId != null)
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
            sql.append(",");
            sql.append(rs.getDouble(2)); // Lat
            sql.append(",");
            sql.append(rs.getDouble(3)); // Lon
            sql.append(",");
            sql.append(StringUtils.isNotEmpty(rs.getString(4)) ? ("\"" + rs.getString(4) + "\""): "NULL"); // Abbrev
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
        
        String continentName = continentNameFromCode.get(continentCode);
        if (continentName != null)
        {
            parentId = contToIdHash.get(continentName);
            if (parentId == null)
            {
                log.error("No Continent Id for Name  ["+continentName+"]   Country["+nameStr+"]");
            }
        } else
        {
            log.error("No Continent Name for Code ["+continentCode+"]");
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
     * Unzips the geonames backup file.
     * @param zipFile the backup file
     * @return the file of the new uncompressed back up file.
     */
    private File unzipToFile(final File zipFile)
    {
        final int bufSize = 102400;
        
        File outFile = null;
        try
        {
            ZipInputStream zin   = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry       entry = zin.getNextEntry();
            if (entry == null)
            {
                return null;
            }
            if (zin.available() == 0)
            {
                return null;
            }
            outFile = File.createTempFile("zip_", "sql");
            FileOutputStream fos = new FileOutputStream(outFile);
            
            byte[] bytes = new byte[bufSize]; // 10K
            int bytesRead = zin.read(bytes, 0, bufSize);
            while (bytesRead > 0)
            {
                fos.write(bytes, 0, bytesRead);
                bytesRead = zin.read(bytes, 0, 100);
            }
            
        } catch (ZipException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
            return null; //I think this means it is not a zip file.
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
            return null;
        }
        return outFile;
    }
    
    /**
     * Unzips and loads the SQL backup of the geonames database needed for building the full geography tree.
     * @return true if build correctly.
     */
    public boolean loadGeoNamesDB()
    {
        File file = new File(XMLHelper.getConfigDirPath("geonames.sql.zip"));
        if (file.exists())
        {
            final File unzippedFile = unzipToFile(file);
            if (unzippedFile != null && unzippedFile.exists())
            {
                BackupServiceFactory bsf = BackupServiceFactory.getInstance();
                bsf.setUsernamePassword(itUsername, itPassword);
                
                boolean status = bsf.doRestoreInBackground("geonames", unzippedFile.getAbsolutePath(), null, null, null, true); // does it asynchronously
                
                // delete the unzipped file
                try
                {
                    unzippedFile.delete();
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
                // Clear IT Username and Password
                bsf.setUsernamePassword(null, null);
                
                return status;
            }
        }
        return false;
    }
}
