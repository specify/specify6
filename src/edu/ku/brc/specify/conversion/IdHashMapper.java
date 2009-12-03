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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.conversion.ConversionLogger.TableWriter;
import edu.ku.brc.ui.ProgressFrame;


/**
 * This is a Database Table Hashtable. It doesn't support the the entire Map interface just
 * "get" and "put"
 
 * @code_status Complete
 **
 * @author rods
 *
 */
public class IdHashMapper implements IdMapperIFace
{
    protected static final Logger log = Logger.getLogger(IdHashMapper.class);
    
    protected static TableWriter tblWriter = null;

    protected String            sql           = null;
    protected boolean           isUsingSQL    = false;
    protected String            tableName;
    //protected Connection        newConn;
    protected Connection        oldConn;
    protected PreparedStatement prepStmt      = null;

    protected String            mapTableName  = null;
    protected boolean           showLogErrors = true;
    
    protected ProgressFrame     frame         = null;
    protected int               initialIndex  = 1;
    protected Vector<Integer>   oldIdNullList = new Vector<Integer>();
    
    protected boolean           doDelete      = true;
    protected boolean           wasEmpty      = true;

    
    /**
     * Default Constructor for those creating derived classes.
     * @throws SQLException
     */
    protected IdHashMapper()
    {
    }

    /**
     * Create a IdHashMapper with a Table Name.
     * @param tableName the table name
     */
    public IdHashMapper(final String tableName)
    {
        this(tableName, false);
    }

    /**
     * Create a IdHashMapper with a Table Name.
     * @param tableName the table name
     */
    public IdHashMapper(final String tableName, final boolean doDelete)
    {
        this.tableName    = tableName.toLowerCase();
        this.mapTableName = tableName;
        this.doDelete     = doDelete;
        
        init(false);
    }

    /**
     * Create a IdHashMapper with a table name an SQL that is used to do the mapping.
     * @param tableName the table name
     * @param sql the sql
     * @throws SQLException
     */
    public IdHashMapper(final String tableName, final String sql)
    {
        this(tableName, sql, false);
    }

    /**
     * Create a IdHashMapper with a table name an SQL that is used to do the mapping.
     * @param tableName the table name
     * @param sql the sql
     * @throws SQLException
     */
    public IdHashMapper(final String tableName, final String sql, final boolean doDelete)
    {
        this(tableName, doDelete);
        
        this.sql        = sql;
        this.isUsingSQL = StringUtils.isNotEmpty(sql);
    }
    
    /**
     * @param tblName
     * @return
     */
    public int getMapCount(final String tblName)
    {
        Statement cntStmt = null;
        ResultSet rs      = null;

        try
        {
            Integer   count = 0;
            cntStmt = oldConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            rs      = cntStmt.executeQuery("select count(*) from "+tblName);
            if (rs.first())
            {
                count = rs.getInt(1);
                if (count == null)
                {
                    return 0;
                }
            }

            return count;

        } catch (SQLException ex) {
        } finally
        {
            try
            {
                if (rs != null) rs.close();
                if (cntStmt != null) cntStmt.close();
            } catch (Exception ex) {}
        }
        return 0;
    }

    /**
     * Initializes the Hash Database Table.
     */
    protected void init(final boolean checkOldDB)
    {
        oldConn = IdMapperMgr.getInstance().getOldConnection();
        //newConn = IdMapperMgr.getInstance().getNewConnection();
        
        int numRecs      = checkOldDB ? BasicSQLUtils.getNumRecords(oldConn, tableName) : 0;
        int mappingCount = getMapCount(mapTableName);
        
        wasEmpty = mappingCount == 0;
        log.info(numRecs+" Records in "+tableName);
        
        try
        {

            if (doDelete || mappingCount == 0)
            {
                Statement stmtNew = oldConn.createStatement();
                String str  = "DROP TABLE "+mapTableName;
                
                try
                {
                    log.info(str);
                    stmtNew.executeUpdate(str);
                    
                } catch (Exception ex)
                {
                    // Exception may occur if table doesn't exist
                }

                str = "CREATE TABLE `"+mapTableName+"` ("+
                                    "`OldID` int(11) NOT NULL default '0', "+
                                    "`NewID` int(11) NOT NULL default '0', "+
                                    " PRIMARY KEY (`OldID`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1";
                log.info("orig sql: " + str);
                str = BasicSQLUtils.getServerTypeSpecificSQL(str, BasicSQLUtils.myDestinationServerType);
                log.info("sql standard query: " + str);
                stmtNew.executeUpdate(str);
                
                
                String str2 = "ALTER TABLE "+mapTableName+" ADD INDEX INX_"+mapTableName+" (NewID)";
                log.info("orig sql: " + str2);
                str2 =  BasicSQLUtils.createIndexFieldStatment(mapTableName, BasicSQLUtils.myDestinationServerType) ;
                log.info("sql standard query: " + str2);
                stmtNew.executeUpdate(str2);
                
                stmtNew.clearBatch();
                stmtNew.close();
            }

        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IdHashMapper.class, ex);
            //
            log.error(ex);
            ex.printStackTrace();
        }

    }

    /**
     * Maps the first index to the second index.
     * The SQL to do the mappings.
     */
    public void mapAllIds()
    {
        if (sql == null)
        {
            throw new RuntimeException("Calling mapAllIds and the SQL statement is NULL!");
        }

        int mappingCount = getMapCount(mapTableName);
        wasEmpty = mappingCount == 0;
        
        if (doDelete || mappingCount == 0)
        {
            if (!isUsingSQL)
            {
                BasicSQLUtils.deleteAllRecordsFromTable(oldConn, mapTableName, BasicSQLUtils.myDestinationServerType);
            }
            
            if (frame != null)
            {
                frame.setDesc("Mapping "+mapTableName);
            }

            try
            {
                if (frame != null)
                {
                   frame.setProcess(0, 0); 
                }
                
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
                    int count = 0;
                    do
                    {
                        pStmt.setInt(1, rs.getInt(1)); // Old Index
                        pStmt.setInt(2, rs.getInt(2)); // New Index
                        if (pStmt.executeUpdate() != 1)
                        {
                            String msg = String.format("Error writing to Map table[%s] old: %d  new: %d", mapTableName, rs.getInt(1), rs.getInt(2));
                            log.error(msg);
                            throw new RuntimeException(msg);
                        }
                        
                        if (frame != null)
                        {
                            if (count % 1000 == 0)
                            {
                                frame.setProcess(count);
                            }
                            
                        } else
                        {
                            if (count % 2000 == 0)
                            {
                                log.debug("Mapped "+count+" records from "+tableName);
                            }                        
                        }
                        count++;
                        
                    } while (rs.next());
                    
                    log.info("Mapped "+count+" records from "+tableName);
                    if (frame != null)
                    {
                       frame.setProcess(0, 0); 
                    }
    
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
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IdHashMapper.class, ex);
                log.error("trying to execute:" + sql);
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
     * Returns whether it is showing log errors.
     * @return whether it is showing log errors
     */
    public boolean isShowLogErrors()
    {
        return showLogErrors;
    }

    /**
     * Tells it to show log errors.
     * @param showLogErrors true/false
     */
    public void setShowLogErrors(boolean showLogErrors)
    {
        this.showLogErrors = showLogErrors;
    }

    /**
     * Cleans up temporary data.
     */
    public void cleanup()
    {
        closePrepareStmt();
        
        if (mapTableName != null && doDelete)
        {
            try
            {
                Statement stmtNew = oldConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                stmtNew.executeUpdate("DROP TABLE `"+mapTableName+"`");
                stmtNew.close();
                
            } catch (com.mysql.jdbc.exceptions.MySQLSyntaxErrorException ex)
            {
                log.error(ex);
                
            } catch (Exception ex)
            {
                //ex.printStackTrace();
                log.error(ex);
            }
            mapTableName = null;
        }
    }
    
    /**
     * Closes internal Prepare Statement.
     */
    public void closePrepareStmt()
    {
        if (prepStmt != null)
        {
            try
            {
                prepStmt.close();
            } catch (Exception ex)
            {
                log.error(ex);
            }
        }
        prepStmt = null;
    }
    
    //--------------------------------------------------
    // IdMapperIFace
    //--------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapperIFace#getName()
     */
    public String getName()
    {
        return mapTableName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapperIFace#put(int, int)
     */
    public void put(final int oldIndex, final int newIndex)
    {
        if (wasEmpty)
        {
            try
            {
                if (prepStmt == null)
                {
                    prepStmt = oldConn.prepareStatement("INSERT INTO "+mapTableName+" VALUES (?,?)");
    
                }
                prepStmt.setInt(1, oldIndex); // Old Index
                prepStmt.setInt(2, newIndex); // New Index
    
                if (prepStmt.executeUpdate() != 1)
                {
                    String msg = String.format("Error writing to Map table[%s] old: %d  new: %d", mapTableName, oldIndex, newIndex);
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IdHashMapper.class, ex);
    
                log.error(ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapperIFace#get(java.lang.Integer)
     */
    public Integer get(final Integer oldId)
    {
        if (oldId == null )
        {
            return null;
        }

        try
        {
            Integer   newId = null;
            Statement stmtNew = oldConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs      = stmtNew.executeQuery("select NewID from "+mapTableName+" where OldID = " + oldId);
            if (rs.next())
            {
                newId = rs.getInt(1);

            } else
            {
                oldIdNullList.add(oldId);
                
                if (showLogErrors) 
                {
                	String msg = "********** Couldn't find old index ["+oldId+"] for "+mapTableName;
                    log.error(msg);
                    if (tblWriter != null) tblWriter.logError(msg);
                }
                rs.close();
                return null;
            }
            rs.close();
            stmtNew.close();

            return newId;

        } catch (SQLException ex)
        {
            String msg = "Couldn't find old index ["+oldId+"] for "+mapTableName;
            if (tblWriter != null) tblWriter.logError(msg);
            
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IdHashMapper.class, ex);
            ex.printStackTrace();
            log.error(ex);
            throw new RuntimeException(msg);
        }
    }
    
    /**
     * @return the oldIdNullList
     */
    public Vector<Integer> getOldIdNullList()
    {
        return oldIdNullList;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapperIFace#size()
     */
    public int size()
    {
        return BasicSQLUtils.getNumRecords(oldConn, mapTableName);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapperIFace#getSql()
     */
    public String getSql()
    {
        return sql;
    }
    
    public void setFrame(ProgressFrame frame)
    {
        this.frame = frame;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapperIFace#setInitialIndex(int)
     */
    public void setInitialIndex(int initialIndex)
    {
        this.initialIndex = initialIndex;
    }

    /**
     * @return the tblWriter
     */
    public static TableWriter getTblWriter() {
        return tblWriter;
    }

    /**
     * @param tblWriter the tblWriter to set
     */
    public static void setTblWriter(TableWriter tblWriter) {
        IdHashMapper.tblWriter = tblWriter;
    }
    
}
