/* This library is free software; you can redistribute it and/or
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

    /**
     * Creates a Mapper for a table and the name of the primary or foreign key.
     * @param tableName name of the table
     * @param idName name of the key field
     * @throws SQLException any
     */
    public IdTableMapper(final String tableName, final String idName) throws SQLException
    {
        super();
        
        this.tableName = tableName;
        this.idName    = idName;

        this.mapTableName = tableName + "_" + idName;
        
        init(true);
    }

    /**
     * Creates a Mapper for a table and the name of the primary or foreign key.
     * @param tableName name of the table
     * @param idName name of the key field
     * @param sql the SQL statement to build the index
     * @throws SQLException any
     */
    public IdTableMapper(final String tableName, final String idName, final String sql) throws SQLException
    {
        this(tableName, idName);
        this.sql = sql;
    }

    /**
     * Map all the old IDs to new IDs
     * @param sqlArg the string to use to fill the map
     */
    public void mapAllIds(final String sqlArg)
    {
        this.sql = sqlArg;

        BasicSQLUtils.deleteAllRecordsFromTable(mapTableName);
        if (frame != null)
        {
            frame.setDesc("Mapping "+mapTableName);
        }
        
        try
        {
            Statement stmtOld = oldConn.createStatement();
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
                int newIndex = 1;
                do
                {
                    int oldIndex = rs.getInt(1);
                    put(oldIndex, newIndex++);
                    
                    if (frame != null)
                    {
                        if (newIndex % 500 == 0)
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

        } catch (SQLException ex)
        {
            ex.printStackTrace();
            log.error(ex);
            throw new RuntimeException(ex);
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
    
    
    //--------------------------------------------------
    // IdMapperIFace
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdHashMapper#mapAllIds()
     */
    public void mapAllIds()
    {
        sql = "select "+idName+" from "+tableName+" order by "+idName;
        mapAllIds(sql);
    }



}
