/*
 * Filename:    $RCSfile: IdMapper.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.0 $
 * Date:        $Date: 2005/10/20 12:53:02 $
 *
 * This library is free software; you can redistribute it and/or
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.dbsupport.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.DBConnection;

/**
 * Manages the mapping of old primary key ID to new sequenial ones
 * 
 * @author rods
 *
 */
public class IdMapper
{
    protected static Log log = LogFactory.getLog(IdMapper.class);

    protected String          tableName;
    protected String          idName;
    protected Connection      oldConn;
    protected Connection      newConn;
    
    protected Vector<Integer> ids           = null;
    protected String          mapTableName = null;
    protected boolean         usingMemory   = false;  
    protected int             lastIdAdded   = -1;
    

    /**
     * @param oldConn
     * @param tableName
     * @param idName
     */
    public IdMapper(final Connection oldConn, final String tableName, final String idName)
    {
        this.oldConn   = oldConn;
        this.tableName = tableName.toLowerCase();
        this.idName    = idName;
        
        newConn = DBConnection.getConnection();
        
        mapTableName = tableName + "_" + idName;
        int numRecs = BasicSQLUtils.getNumRecords(oldConn, tableName);
        log.info(numRecs+" Records in "+tableName);
        
        // XXX DEBUG and Testing
        boolean alreadyCreated = !GenericDBConversion.shouldCreateMapTables();
        numRecs = 5001; // for all tables
        
        if (numRecs < 5000)
        {
            usingMemory = true;
            ids         = new Vector<Integer>();
            
            ids.add(0);
            lastIdAdded = 0;
             
        } else if (!alreadyCreated)
        {
            try
            {
                
                if (GenericDBConversion.shouldCreateMapTables())
                {
                    Statement stmt = newConn.createStatement();
                    String    str  = "DROP TABLE `"+mapTableName+"`";
                    try
                    {
                        stmt.executeUpdate(str);
                    } catch (SQLException ex){};
                
                    str = "CREATE TABLE `"+mapTableName+"` ("+
                                        "`NewID` int(11) NOT NULL default '0', "+
                                        "`OldID` int(11) NOT NULL default '0', "+
                                        " PRIMARY KEY (`NewID`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1";
                    //log.info(str);
                    stmt.executeUpdate(str); 
                    
                    stmt.executeUpdate("alter table "+mapTableName+" add index INX_"+mapTableName+" (OldID)"); 
                    
                    stmt.clearBatch();
                    stmt.close();
                }
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                log.error(ex);
            }
        }
    }
    
    /**
     * Returns unique name
     * @return Returns unique name
     */
    public String getName()
    {
        return mapTableName;
    }

    /**
     * @param newIndex
     * @param oldIndex
     */
    public void addIndex(final int newIndex, final int oldIndex)
    {
        if (usingMemory)
        {
            if (newIndex != lastIdAdded+1)
            {
                log.error("Indexes out of sequence! ["+mapTableName+"] was["+newIndex+"] should have been["+(lastIdAdded+1)+"]");
            } else
            {
                ids.add(oldIndex);
                //log.error("["+ids.size()+"] n["+newIndex+"] o["+oldIndex+"]");
            }
            lastIdAdded = newIndex;
        } else
        {
            try
            {
                Statement stmt = newConn.createStatement();
                String str = "INSERT INTO "+mapTableName+" VALUES (" + newIndex + ","+oldIndex+")";
                stmt.executeUpdate(str); 
                stmt.clearBatch();
                stmt.close();
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                log.error(ex);
            }            
        }
    }
    
    /**
     * Map all the old iDs to new IDs
     */
    public void mapAllIds()
    {
        mapAllIds("select "+idName+" from "+tableName+" order by "+idName);
    }
    
    /**
     * Map all the old iDs to new IDs
     */
    public void mapAllIds(final String sql)
    {
        BasicSQLUtils.deleteAllRecordsFromTable(mapTableName);
        try
        {
            Statement stmt = oldConn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.first())
            {
                int newIndex = 1;
                do
                {
                    int oldIndex = rs.getInt(1);
                    addIndex(newIndex++, oldIndex);
                    if (newIndex % 1000 == 0)
                    {
                        log.info("Mapped "+newIndex+" records from "+tableName);
                    }
                    
                } while (rs.next());
                log.info("Mapped "+newIndex+" records from "+tableName);
                
            } else
            {
                log.info("No records to map in "+tableName);
            }
            rs.close();
            stmt.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            log.error(ex);
            throw new RuntimeException(ex);
        }             
    }
    
    /**
     * @param oldIndex old index
     * @return the new index
     */
    public Integer getNewIndexFromOld(final Integer oldIndex)
    {
        if (oldIndex == null || oldIndex.intValue() == 0)
        {
            return null;
        }
        
        if (usingMemory)
        {
            for (int i=0;i<ids.size();i++)
            {
                if (oldIndex.intValue() == ids.get(i).intValue())
                {
                    return i;
                }
            }
            throw new RuntimeException("Couldn't find old index ["+oldIndex+"] for "+mapTableName);
        } else
        {
            try
            {
                Integer   newIndex = null;
                Statement stmt     = newConn.createStatement();
                ResultSet rs       = stmt.executeQuery("select NewID from "+mapTableName+" where OldID = " + oldIndex);
                if (rs.first())
                {
                    newIndex = rs.getInt(1);

                } else
                {
                    log.error("********** Couldn't find old index ["+oldIndex+"] for "+mapTableName+" "+idName);
                    rs.close();
                    stmt.close();
                    return null;
                }
                rs.close();
                stmt.close();
                
                return newIndex;
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                log.error(ex);
                throw new RuntimeException("Couldn't find old index ["+oldIndex+"] for "+mapTableName);
            }            
        } 
    }
    
    /**
     * Cleans up temporary data
     */
    public void cleanup()
    {
        if (mapTableName != null)
        {
            if (usingMemory)
            {
                ids.clear();
                ids = null;
                
            } else
            {
                try
                {
                    Statement stmt = newConn.createStatement();
                    stmt.executeUpdate("DROP TABLE `"+mapTableName+"`"); 
                    stmt.close();
                    
                } catch (SQLException ex)
                {
                    ex.printStackTrace();
                    log.error(ex);
                }           
            }
            mapTableName = null;
        }

    }

}
