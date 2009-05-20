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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    protected String          sql           = null;
    protected boolean         isUsingSQL    = false;
    protected String          tableName;
    protected Connection      newConn;
    protected Connection      oldConn;

    protected String          mapTableName  = null;
    protected boolean         showLogErrors = true;
    
    protected ProgressFrame   frame         = null;
    protected int             initialIndex  = 1;
    
    protected static TableWriter tblWriter = null;
    
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
        this.tableName    = tableName.toLowerCase();
        this.mapTableName = tableName;
        
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
        this(tableName);
        
        this.sql   = sql;
        isUsingSQL = StringUtils.isNotEmpty(sql);
    }

    /**
     * Initializes the Hash Database Table.
     */
    protected void init(final boolean checkOldDB)
    {
        oldConn = IdMapperMgr.getInstance().getOldConnection();
        newConn = IdMapperMgr.getInstance().getNewConnection();
        
        int numRecs = checkOldDB ? BasicSQLUtils.getNumRecords(oldConn, tableName) : 0;
        
        log.info(numRecs+" Records in "+tableName);
        
        try
        {

            if (GenericDBConversion.shouldCreateMapTables())
            {
                Statement stmtNew = newConn.createStatement();
                //String str  = "DROP TABLE `"+mapTableName+"`";
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
                
                
                String str2 = "alter table "+mapTableName+" add index INX_"+mapTableName+" (NewID)";
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

        if (!isUsingSQL)
        {
            BasicSQLUtils.deleteAllRecordsFromTable(mapTableName, BasicSQLUtils.myDestinationServerType);
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
            Statement stmtOld = oldConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs      = stmtOld.executeQuery(sql);
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
                    int oldIndex = rs.getInt(1);
                    int newIndex = rs.getInt(2);
                    
                    put(oldIndex, newIndex);
                    
                    if (frame != null)
                    {
                        if (count % 500 == 0)
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

        } catch (SQLException ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IdHashMapper.class, ex);
            log.error("trying to execute:" + sql);
            log.error(ex);
            
            throw new RuntimeException(ex);
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
        if (mapTableName != null)
        {
            try
            {
                Statement stmtNew = newConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
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
        try
        {
            String str = "INSERT INTO "+mapTableName+" VALUES (" + oldIndex + "," + newIndex + ")";
            Statement stmtNew = newConn.createStatement();
            stmtNew.executeUpdate(str);
            stmtNew.clearBatch();
            stmtNew.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IdHashMapper.class, ex);

            log.error(ex);
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
            Statement stmtNew = newConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs      = stmtNew.executeQuery("select NewID from "+mapTableName+" where OldID = " + oldId);
            if (rs.next())
            {
                newId = rs.getInt(1);

            } else
            {
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapperIFace#size()
     */
    public int size()
    {
        return BasicSQLUtils.getNumRecords(newConn, mapTableName);
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
