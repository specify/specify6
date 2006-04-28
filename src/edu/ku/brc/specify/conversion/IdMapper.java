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

import org.apache.commons.lang.StringUtils;
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

    protected String          sql           = null;
    protected String          tableName;
    protected String          idName;
    protected Connection      newConn;
    protected Connection      oldConn;
    protected Statement       stmtOld;
    protected Statement       stmtNew;

    protected Vector<Integer> ids           = null;
    protected String          mapTableName  = null;
    protected boolean         usingMemory   = false;
    protected int             lastIdAdded   = -1;

    /**
     * @param oldConn
     * @param tableName
     * @param idName
     */
    public IdMapper(final String tableName, final String idName) throws SQLException
    {
        oldConn = IdMapperMgr.getInstance().getOldConnection();
        
        newConn = IdMapperMgr.getInstance().getNewConnection();
        
        stmtOld = oldConn.createStatement();
        stmtNew = newConn.createStatement();
        
        this.tableName = tableName.toLowerCase();
        this.idName    = idName;

        mapTableName = tableName + "_" + idName;
        int numRecs = BasicSQLUtils.getNumRecords(oldConn, tableName);
        log.info(numRecs+" Records in "+tableName);

        // XXX DEBUG and Testing
        boolean alreadyCreated = !GenericDBConversion.shouldCreateMapTables();
        numRecs = 2001; // for all tables

        if (numRecs < 2000)
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
                    String str  = "DROP TABLE `"+mapTableName+"`";
                    try
                    {
                        stmtNew.executeUpdate(str);
                    } catch (SQLException ex){};

                    str = "CREATE TABLE `"+mapTableName+"` ("+
                                        "`OldID` int(11) NOT NULL default '0', "+
                                        "`NewID` int(11) NOT NULL default '0', "+
                                        " PRIMARY KEY (`OldID`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1";
                    //log.info(str);
                    stmtNew.executeUpdate(str);

                    stmtNew.executeUpdate("alter table "+mapTableName+" add index INX_"+mapTableName+" (NewID)");

                    stmtNew.clearBatch();
                }

            } catch (SQLException ex)
            {
                ex.printStackTrace();
                log.error(ex);
            }
        }
    }
    
    /**
     * @param oldConn
     * @param tableName
     * @param idName
     */
    public IdMapper(final String tableName, final String idName, final String sql) throws SQLException
    {
        this(tableName, idName);
        this.sql = sql;
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
                String str = "INSERT INTO "+mapTableName+" VALUES (" + oldIndex + "," + newIndex + ")";
                stmtNew.executeUpdate(str);
                stmtNew.clearBatch();

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
        sql = "select "+idName+" from "+tableName+" order by "+idName;
        mapAllIds(sql);
    }

    /**
     * Map all the old iDs to new IDs
     */
    public void mapAllIds(final String sql)
    {
        this.sql = sql;
        
        BasicSQLUtils.deleteAllRecordsFromTable(mapTableName);
        try
        {
            ResultSet rs = stmtOld.executeQuery(sql);
            if (rs.first())
            {
                int newIndex = 1;
                do
                {
                    int oldIndex = rs.getInt(1);
                    addIndex(newIndex++, oldIndex);
                    if (newIndex % 2000 == 0)
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

        } catch (SQLException ex)
        {
            ex.printStackTrace();
            log.error(ex);
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Map all the old iDs to new IDs
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
     * Returns the New Record Id given the Old Record Id  (Usually primary key)
     * @param oldId old Id
     * @return the new record Id (Usually primary key)
     */
    public Integer getNewIdFromOldId(final Integer oldId)
    {
        if (oldId == null )
        {
            return null;
        }

        if (usingMemory)
        {
            for (int i=0;i<ids.size();i++)
            {
                if (oldId.intValue() == ids.get(i).intValue())
                {
                    return i;
                }
            }
            throw new RuntimeException("Couldn't find old index ["+oldId+"] for "+mapTableName);
        } else
        {
            try
            {
                Integer   newId = null;
                ResultSet rs       = stmtNew.executeQuery("select NewID from "+mapTableName+" where OldID = " + oldId);
                if (rs.first())
                {
                    newId = rs.getInt(1);

                } else
                {
                    log.error("********** Couldn't find old index ["+oldId+"] for "+mapTableName+" "+idName);
                    rs.close();
                    return null;
                }
                rs.close();

                return newId;

            } catch (SQLException ex)
            {
                ex.printStackTrace();
                log.error(ex);
                throw new RuntimeException("Couldn't find old index ["+oldId+"] for "+mapTableName);
            }
        }
    }
    
    public String getSql()
    {
        return sql;
    }

    /**
     * Cleans up temporary data
     */
    public void cleanup() throws SQLException
    {
        oldConn = null;
        newConn = null;
        
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
                    stmtNew.executeUpdate("DROP TABLE `"+mapTableName+"`");

                } catch (SQLException ex)
                {
                    ex.printStackTrace();
                    log.error(ex);
                }
            }
            mapTableName = null;
        }
        
        stmtNew.close();
        stmtOld.close();

    }

}
