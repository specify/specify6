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
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.SwingUtilities;
import javax.transaction.Transaction;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import edu.ku.brc.helpers.XMLHelper;
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
    protected Connection       newDBConn;
    protected ProgressFrame    frame;
    
    protected Hashtable<String, String>  countryToContHash = new Hashtable<String, String>();
    protected Hashtable<String, String>  countryToAbbrevHash = new Hashtable<String, String>();
    protected Hashtable<String, Integer> contToIdHash      = new Hashtable<String, Integer>();
    
    protected Hashtable<String, String>  stateToCountryHash = new Hashtable<String, String>();
    protected Hashtable<String, Integer> countryToIdHash    = new Hashtable<String, Integer>();
    
    protected Hashtable<String, String>  countyToStateHash = new Hashtable<String, String>();
    protected Hashtable<String, Integer> stateToIdHash    = new Hashtable<String, Integer>();
    
    /**
     * @param geoDef
     * @param nowStr
     * @param createdByAgent
     * @param newDBConn
     * @param frame
     */
    public BuildFromGeonames(final GeographyTreeDef geoDef, 
                             final String           nowStr,
                             final Agent            createdByAgent,
                             final Connection       newDBConn,
                             final ProgressFrame    frame)
    {
        super();
        this.geoDef = geoDef;
        this.nowStr = nowStr;
        this.createdByAgent = createdByAgent;
        this.newDBConn = newDBConn;
        this.frame = frame;
        
        insertSQL = "INSERT INTO geography (Name, RankID, ParentID, IsAccepted, IsCurrent, GeographyTreeDefID, GeographyTreeDefItemID, " +
                    "CreatedByAgentID, CentroidLat, CentroidLon, Abbrev, " +
                    "TimestampCreated, TimestampModified, Version) VALUES (";
    }
    
    /**
     * @return
     */
    public boolean build()
    {
        loadXLS();
        
        sql.setLength(0); // because of debugging
        sql.append(insertSQL);
        
        Connection conn = null;
        Statement  stmt = null;
        Statement  updateStmt = null;
        try
        {
            conn       = DriverManager.getConnection("jdbc:mysql://localhost/geonames?characterEncoding=UTF-8&autoReconnect=true", "root", "root");
            stmt       = conn.createStatement();
            updateStmt = newDBConn.createStatement();
            
            int delCnt = BasicSQLUtils.update(newDBConn, "DELETE FROM geography WHERE GeographyID > 1 AND RankId = 400");
            log.debug("Deleted "+delCnt+" geography records.");
            delCnt = BasicSQLUtils.update(newDBConn, "DELETE FROM geography WHERE GeographyID > 1 AND RankId = 300");
            log.debug("Deleted "+delCnt+" geography records.");
            delCnt = BasicSQLUtils.update(newDBConn, "DELETE FROM geography WHERE GeographyID > 1 AND RankId = 200");
            log.debug("Deleted "+delCnt+" geography records.");
            delCnt = BasicSQLUtils.update(newDBConn, "DELETE FROM geography WHERE GeographyID > 1 AND RankId = 100");
            log.debug("Deleted "+delCnt+" geography records.");
            
            newDBConn.setAutoCommit(false);
            
            // Continent
            String sqlStr = "SELECT name, latitude, longitude, country FROM geonames.geoname WHERE fcode = 'CONT' ORDER BY name";
            ResultSet rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                buildInsert(rs, 100);
                
                log.debug(sql.toString());
                int rv = updateStmt.executeUpdate(sql.toString());
                
                Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                contToIdHash.put(rs.getString(1), newId);
                
                newDBConn.commit();
                
                sql.setLength(insertSQL.length());
            }
            rs.close();
            
            // Country
            sqlStr = "SELECT name, latitude, longitude, country FROM geonames.geoname WHERE fcode = 'PCLI' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                String nameStr = rs.getString(1);
                buildInsert(rs, 200);
                countryToAbbrevHash.put(nameStr, rs.getString(4));
                
                int rv = updateStmt.executeUpdate(sql.toString());
                
                Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                countryToIdHash.put(nameStr, newId);
                
                newDBConn.commit();
                
                sql.setLength(insertSQL.length());
            }
            rs.close();
            
            // STATE
            sqlStr = "SELECT name,  latitude, longitude, country FROM geonames.geoname WHERE fcode = 'ADM1' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                buildInsert(rs, 300);
                
                String nameStr = rs.getString(1);
                stateToCountryHash.put(nameStr, rs.getString(4));
                
                int rv = updateStmt.executeUpdate(sql.toString());
                Integer newId = BasicSQLUtils.getInsertedId(updateStmt);
                stateToIdHash.put(nameStr, newId);

                newDBConn.commit();
                
                sql.setLength(insertSQL.length());
            }
            rs.close();
            
            // COUNTY
            sqlStr = "SELECT name, latitude, longitude, country FROM geonames.geoname WHERE fcode = 'ADM2' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                buildInsert(rs, 400);
                
                int rv = updateStmt.executeUpdate(sql.toString());
                
                newDBConn.commit();
                
                sql.setLength(insertSQL.length());
            }
            rs.close();
            
            return true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            try
            {
                newDBConn.rollback();
                
            } catch (Exception exr)
            {
                exr.printStackTrace();
            }
        } finally
        {
            try
            {
                stmt.close();
                conn.close();
                
                newDBConn.setAutoCommit(true);
                
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
    private void buildInsert(final ResultSet rs, 
                             final int       rankId) throws SQLException
    {
        
        GeographyTreeDefItem item         = geoDef.getDefItemByRank(rankId);
        int                  geoDefItemId = item.getId();
        
        String nameStr = rs.getString(1);
        
        Integer parentId = null;
        if (rankId == 200) // Country
        {
            String nm = nameStr;
            if (nameStr.startsWith("Republic of "))
            {
                nm = nameStr.substring(12);
            }
            String continent = countryToContHash.get(nm);
            if (continent != null)
            {
                parentId = contToIdHash.get(continent);
                if (parentId == null)
                {
                    log.error("No Continent Id for ["+continent+"]["+nm+"]");
                }
            } else
            {
                
                log.error("No Continent for ["+nm+"]");
            }
            
        } else if (rankId == 300) // State
        {
            String country = stateToCountryHash.get(nameStr);
            if (country != null)
            {
                parentId = countryToIdHash.get(country);
                if (parentId == null)
                {
                    log.error("No Country Id for ["+country+"]["+nameStr+"]");
                }
            } else
            {
                log.error("No Country for ["+nameStr+"]");
            }
        } else if (rankId == 400) // County
        {
            String state = countyToStateHash.get(nameStr);
            if (state != null)
            {
                parentId = stateToIdHash.get(state);
                if (parentId == null)
                {
                    log.error("No State Id for ["+state+"]["+nameStr+"]");
                }
            } else
            {
                log.error("No State for ["+nameStr+"]");
            }
        }
        
        if (nameStr.length() > 64)
        {
            log.error("Name["+nameStr+" is too long "+nameStr.length() + "truncating.");
            nameStr = nameStr.substring(0, 64);
        }
        
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
    }
    
    /**
     * 
     */
    private void loadXLS()
    {
        int counter = 0;
        
        String fileName = "Geography.xls";
        File file = XMLHelper.getConfigDir("../demo_files/"+fileName);
        if (!file.exists())
        {
            log.error("Couldn't file[" + file.getAbsolutePath() + "] checking the config dir");
            file = XMLHelper.getConfigDir(fileName);
            if (!file.exists())
            {
                file = new File("Specify/demo_files/"+fileName);
            }
        }
        
        try
        {
            String[]        cells    = new String[4];
            InputStream     input    = new FileInputStream(file);
            POIFSFileSystem fs       = new POIFSFileSystem(input);
            HSSFWorkbook    workBook = new HSSFWorkbook(fs);
            HSSFSheet       sheet    = workBook.getSheetAt(0);
            Iterator<?>     rows     = sheet.rowIterator();
            
            int lastRowNum  = sheet.getLastRowNum();
            if (frame != null)
            {
                final int mx = lastRowNum;
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        frame.setProcess(0, mx);
                    }
                });
            }
            
            while (rows.hasNext())
            {
                if (counter == 0)
                {
                    counter = 1;
                    rows.next();
                    continue;
                }
                if (counter % 100 == 0)
                {
                    if (frame != null) frame.setProcess(counter);
                    log.info("Converted " + counter + " Geography records");
                }
                
                cells[2] = null;
                
                HSSFRow row = (HSSFRow) rows.next();
                Iterator<?> cellsIter = row.cellIterator();
                int i = 0;
                while (cellsIter.hasNext() && i < 4)
                {
                    HSSFCell cell = (HSSFCell)cellsIter.next();
                    if (cell != null)
                    {
                        cells[i] = StringUtils.trim(cell.getRichStringCellValue().getString());
                        if (cells[i].length() == 0)
                        {
                            break;
                        }
                        i++;
                    }
                }
                System.out.println(i+"  "+cells[1]+"  "+cells[0]+"  "+cells[2]);
                if (i == 2)
                {
                    countryToContHash.put(cells[1], cells[0]);
                }
    
                counter++;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
}
