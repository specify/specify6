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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.StringUtils;


/**
 * Manages the mapping of old primary key ID to new sequenial ones for table name and it key (primary or foreign).
 *
 * @code_status Complete
 **
 * @author rods
 *
 */
public class IdTableMapper extends IdHashMapper
{
    //protected static final Logger log = Logger.getLogger(IdTableMapper.class);

    protected String idName;
    protected IdMapperIndexIncrementerIFace indexIncremeter = null;

    /**
     * Creates a Mapper for a table and the name of the primary or foreign key.
     * @param tableName name of the table
     * @param idName name of the key field
     * @throws SQLException any
     */
    public IdTableMapper(final String tableName, final String idName)
    {
        this(tableName, idName, true, true);
    }
    
    /**
     * Creates a Mapper for a table and the name of the primary or foreign key.
     * @param tableName name of the table
     * @param idName name of the key field
     * @param doDelete should delete the mapping table
     * @param doCheckOldDB checks the count of the table to be mapped
     */
    public IdTableMapper(final String tableName, final String idName, final boolean doDelete, final boolean doCheckOldDB)
    {
        super();
        
        this.tableName    = tableName;
        this.idName       = idName;

        this.mapTableName = tableName + "_" + idName;
        this.doDelete     = doDelete;
        
        init(doCheckOldDB);
    }
    
    /**
     * Creates a Mapper for a table and the name of the primary or foreign key.
     * @param tableName name of the table
     * @param idName name of the key field
     * @param sql the SQL statement to build the index
     * @throws SQLException any
     */
    public IdTableMapper(final String tableName, final String idName, final String sql)
    {                                                                                                 
        this(tableName, idName, sql, true);
    }

    /**
     * Creates a Mapper for a table and the name of the primary or foreign key.
     * @param tableName name of the table
     * @param idName name of the key field
     * @param sql the SQL statement to build the index
     * @param doDelete should delete the mapping table
     * @throws SQLException any
     */
    public IdTableMapper(final String tableName, final String idName, final String sql, final boolean doDelete)
    {
        this(tableName, idName, sql, doDelete, true);
    }

    /**
     * Creates a Mapper for a table and the name of the primary or foreign key.
     * @param tableName name of the table
     * @param idName name of the key field
     * @param sql the SQL statement to build the index
     * @param doDelete should delete the mapping table
     * @param doCheckOldDB checks the count of the table to be mapped
     * @throws SQLException any
     */
    public IdTableMapper(final String tableName, final String idName, final String sql, final boolean doDelete, final boolean doCheckOldDB)
    {                                                                                                 
        this(tableName, idName, doDelete, doCheckOldDB);
        
        this.sql = sql;
        
        log.debug("IdTableMapper created for table[" + tableName +"] idName[" + idName + "] ");
        log.debug("IdTableMapper using sql: " + sql );
    }

    /**
     * @param indexIncremeter
     */
    public void setIndexIncremeter(final IdMapperIndexIncrementerIFace indexIncremeter)
    {
        this.indexIncremeter = indexIncremeter;
    }
    
    /**
     * Removes all the records from the mapper.
     */
    public void clearRecords()
    {
        BasicSQLUtils.deleteAllRecordsFromTable(oldConn, mapTableName, BasicSQLUtils.myDestinationServerType);
    }
    
    /**
     * Copies Mapping table to a destination mapping table.
     * @param dest the insert into table.
     */
    public void copy(final IdTableMapper dest)
    {
        PreparedStatement pStmt = null;
        Statement         stmt  = null;
        try
        {
            pStmt = oldConn.prepareStatement(String.format("INSERT INTO %s SET OldID=?, NewID=?", dest.getName()));
            stmt  = oldConn.createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT OldID, NewID FROM " + getName());
            while (rs.next())
            {
                pStmt.setInt(1, rs.getInt(1));
                pStmt.setInt(2, rs.getInt(2));
                pStmt.execute();
            }
            rs.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (pStmt != null) pStmt.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {}
        }
    }
    
    /**
     * Map all the old IDs to new IDs
     * @param sqlArg the string to use to fill the map
     */
    public void mapAllIds(final String sqlArg)
    {
        log.debug("mapAllIds with sql: " + sqlArg) ;
        this.sql = sqlArg;

        int mappingCount = getMapCount(mapTableName);
        wasEmpty = mappingCount == 0;
        
        if (doDelete || mappingCount == 0)
        {
            BasicSQLUtils.deleteAllRecordsFromTable(oldConn, mapTableName, BasicSQLUtils.myDestinationServerType);
            if (frame != null)
            {
                String dMsg = "Mapping "+mapTableName;
                frame.setDesc(dMsg);
                log.debug(dMsg);
            }
            
            try
            {
                log.debug("Executing: "+sql);
                PreparedStatement pStmt   = oldConn.prepareStatement("INSERT INTO "+mapTableName+" VALUES (?,?)");
                Statement         stmtOld = oldConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet         rs      = stmtOld.executeQuery(sql);
                
                if (rs.last())
                {
                    if (frame != null)
                    {
                       frame.setProcess(0, rs.getRow()); 
                    }
                }            
                
                if (rs.first())
                {
                    int newIndex = initialIndex;
                    do
                    {
                        int oldIndex = rs.getInt(1);
                        //log.debug("map "+mapTableName+" old[" + oldIndex + "] new [" + newIndex +"]");
                        
                        if (indexIncremeter != null)
                        {
                            newIndex = indexIncremeter.getNextIndex();
                        }
                        
                        pStmt.setInt(1, oldIndex); // Old Index
                        pStmt.setInt(2, newIndex); // New Index
                        if (pStmt.executeUpdate() != 1)
                        {
                            String msg = String.format("Error writing to Map table[%s] old: %d  new: %d", mapTableName, oldIndex, newIndex);
                            log.error(msg);
                            throw new RuntimeException(msg);
                        }
                        
                        newIndex++; // incrementing doesn't matter when there is an indexIncremeter
                        
                        if (frame != null)
                        {
                            if (newIndex % 1000 == 0)
                            {
                                frame.setProcess(newIndex);
                            }
                            
                        } else
                        {
                            if (newIndex % 2000 == 0)
                            {
                                log.debug("Mapped "+newIndex+" records from "+tableName);
                            }                        
                        }
    
                    } while (rs.next());
                    log.info("Mapped "+newIndex+" records from "+tableName);
                    
                } else
                {
                    log.info("No records to map in "+tableName);
                }
                rs.close();
                stmtOld.close();
                pStmt.close();
    
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IdTableMapper.class, ex);
                log.error(ex);
                throw new RuntimeException(ex);
            }
        } else
        {
            log.debug("Skipping the build of mapper: "+mapTableName);
        }
        
        if (frame != null)
        {
           frame.setProcess(0, 0); 
        }
    }

    /**
     * Map all the old IDs to new IDs
     */
    public void mapAllIdsWithSQL()
    {
        if (StringUtils.isNotEmpty(sql))
        {
            mapAllIds(sql);

        } else
        {
            throw new RuntimeException("The SQL strng is empty in idmapper. "+tableName);
        }
    }
    
    /**
     * Map all the old IDs to new IDs
     */
    public void mapAllIdsNoIncrement()
    {
        mapAllIdsNoIncrement(sql);
    }
    
    /**
     * Map all the old IDs to new IDs
     * @param sqlArg the string to use to fill the map
     */
    public void mapAllIdsNoIncrement(final String sqlArg)
    {
        log.debug("mapAllIdsNoIncrement with sql: " + sqlArg) ;
        this.sql = sqlArg;

        int mappingCount = getMapCount(mapTableName);
        wasEmpty = mappingCount == 0;
        
        if (doDelete || mappingCount == 0)
        {
            BasicSQLUtils.deleteAllRecordsFromTable(oldConn, mapTableName, BasicSQLUtils.myDestinationServerType);
            if (frame != null)
            {
                String dMsg = "Mapping "+mapTableName;
                frame.setDesc(dMsg);
                log.debug(dMsg);
            }
            
            try
            {
                log.debug("Executing: "+sql);
                PreparedStatement pStmt   = oldConn.prepareStatement("INSERT INTO "+mapTableName+" VALUES (?,?)");
                Statement         stmtOld = oldConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet         rs      = stmtOld.executeQuery(sql);
                
                if (rs.last())
                {
                    if (frame != null)
                    {
                       frame.setProcess(0, rs.getRow()); 
                    }
                }            
                
                int cnt = 0;
                if (rs.first())
                {
                    do
                    {
                        int oldIndex = rs.getInt(1);
                        int toIndex = rs.getInt(2);
                        pStmt.setInt(1, oldIndex); // Old Index
                        pStmt.setInt(2, toIndex); // New Index
                        if (pStmt.executeUpdate() != 1)
                        {
                            String msg = String.format("Error writing to Map table[%s] old: %d  new: %d", mapTableName, oldIndex, toIndex);
                            log.error(msg);
                            throw new RuntimeException(msg);
                        }
                        
                        if (frame != null)
                        {
                            if (cnt % 1000 == 0)
                            {
                                frame.setProcess(cnt);
                            }
                            
                        } else
                        {
                            if (cnt % 2000 == 0)
                            {
                                log.debug("Mapped "+cnt+" records from "+tableName);
                            }                        
                        }
                        
                        cnt++;
                        
                    } while (rs.next());
                    
                    log.info("Mapped "+cnt+" records from "+tableName);
                    
                } else
                {
                    log.info("No records to map in "+tableName);
                }
                rs.close();
                stmtOld.close();
                pStmt.close();
    
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IdTableMapper.class, ex);
                log.error(ex);
                throw new RuntimeException(ex);
            }
        } else
        {
            log.debug("Skipping the build of mapper: "+mapTableName);
        }
        
        if (frame != null)
        {
           frame.setProcess(0, 0); 
        }
    }

    //--------------------------------------------------
    // IdMapperIFace
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdHashMapper#mapAllIds()
     */
    public void mapAllIds()
    {
        sql = "SELECT "+idName+" FROM "+tableName+" ORDER BY "+idName;
        mapAllIds(sql);
    }



}
