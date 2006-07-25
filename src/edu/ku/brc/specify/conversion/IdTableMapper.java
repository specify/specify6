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
import org.apache.log4j.Logger;


/**
 * Manages the mapping of old primary key ID to new sequenial ones
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class IdTableMapper extends IdHashMapper
{
    protected static final Logger log = Logger.getLogger(IdTableMapper.class);

    protected String idName;

    /**
     * @param tableName
     * @param idName
     * @throws SQLException
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
     * @param tableName
     * @param idName
     * @param sql
     * @throws SQLException
     */
    public IdTableMapper(final String tableName, final String idName, final String sql) throws SQLException
    {
        this(tableName, idName);
        this.sql = sql;
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
            Statement stmtOld = oldConn.createStatement();
            ResultSet rs      = stmtOld.executeQuery(sql);
            if (rs.first())
            {
                int newIndex = 1;
                do
                {
                    int oldIndex = rs.getInt(1);
                    put(oldIndex, newIndex++);
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

}
