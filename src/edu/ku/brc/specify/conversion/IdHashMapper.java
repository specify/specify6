/*
 * Filename:    $RCSfile: IdHashMapper.java,v $
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


/**
 * This is a Database Table Hashtable. It doesn't support the the entire Map interface just
 * "get" and "put"
 *
 * @author rods
 *
 */
public class IdHashMapper implements IdMapper
{
    protected static Log log = LogFactory.getLog(IdHashMapper.class);

    protected String          sql           = null;
    protected String          tableName;
    protected Connection      newConn;
    protected Connection      oldConn;

    protected Vector<Integer> ids           = null;
    protected String          mapTableName  = null;
    protected boolean         showLogErrors = true;  
    
    /**
     * Default Constructor for those creating derived classes
     * @throws SQLException
     */
    protected IdHashMapper()
    {
    }

    /**
     * @param oldConn
     * @param tableName
     * @param idName
     */
    public IdHashMapper(final String tableName) throws SQLException
    {
        this.tableName    = tableName.toLowerCase();
        this.mapTableName = tableName;
        
        init(false);
    }

    /**
     * @param name
     * @param sql
     * @throws SQLException
     */
    public IdHashMapper(final String name, final String sql) throws SQLException
    {
        this(name);
        
        this.sql = sql;
    }

    /**
     * Initializes the Hash Database Table 
     */
    protected void init(final boolean checkOldDB) throws SQLException
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
                stmtNew.close();
            }

        } catch (SQLException ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapper#getName()
     */
    public String getName()
    {
        return mapTableName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapper#put(int, int)
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
            log.error(ex);
        }
    }
    
    /**
     * Maps the first index to the second index
     * The SQL to do the mappings
     */
    public void mapAllIds()
    {
        if (sql == null)
        {
            throw new RuntimeException("Calling mapAllIds and the SQL statement is NULL!");
            
        }

        BasicSQLUtils.deleteAllRecordsFromTable(mapTableName);
        
        try
        {
            Statement stmtOld = oldConn.createStatement();
            ResultSet rs = stmtOld.executeQuery(sql);
            if (rs.first())
            {
                int count = 0;
                do
                {
                    int oldIndex = rs.getInt(1);
                    int newIndex = rs.getInt(2);
                    
                    put(oldIndex, newIndex);
                    
                    if (count % 2000 == 0)
                    {
                        log.info("Mapped "+count+" records from "+tableName);
                    }
                    count++;
                    
                } while (rs.next());
                
                log.info("Mapped "+count+" records from "+tableName);

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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapper#get(java.lang.Integer)
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
            Statement stmtNew = newConn.createStatement();
            ResultSet rs      = stmtNew.executeQuery("select NewID from "+mapTableName+" where OldID = " + oldId);
            if (rs.first())
            {
                newId = rs.getInt(1);

            } else
            {
                if (showLogErrors) log.error("********** Couldn't find old index ["+oldId+"] for "+mapTableName);
                rs.close();
                return null;
            }
            rs.close();
            stmtNew.close();

            return newId;

        } catch (SQLException ex)
        {
            ex.printStackTrace();
            log.error(ex);
            throw new RuntimeException("Couldn't find old index ["+oldId+"] for "+mapTableName);
        }

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapper#size()
     */
    public int size()
    {
        return BasicSQLUtils.getNumRecords(newConn, mapTableName);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapper#getSql()
     */
    public String getSql()
    {
        return sql;
    }
    

    public boolean isShowLogErrors()
    {
        return showLogErrors;
    }

    public void setShowLogErrors(boolean showLogErrors)
    {
        this.showLogErrors = showLogErrors;
    }

    /**
     * Cleans up temporary data
     */
    public void cleanup() throws SQLException
    {
    	if (mapTableName != null)
    	{
	        try
	        {
	            Statement stmtNew = newConn.createStatement();
	            stmtNew.executeUpdate("DROP TABLE `"+mapTableName+"`");
	            stmtNew.close();
	            
	        } catch (SQLException ex)
	        {
	            ex.printStackTrace();
	            log.error(ex);
	        }
	
	        mapTableName = null;
    	}

    }

}
