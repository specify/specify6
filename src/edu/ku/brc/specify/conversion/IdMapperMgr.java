/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIRegistry;


/**
 * Manages all IdMappers and provides a way to provide foreign key mappinggs for columns in other tables. 
 * The Class requires a connection to the "Old" database which is where the "from" IDs will come from and a 
 * connection to the "new" database which is where all the tables will be created.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class IdMapperMgr
{
    protected static final Logger log = Logger.getLogger(IdMapperMgr.class);
    protected static IdMapperMgr              idMapperMgr = new IdMapperMgr();
    
    protected Connection                      oldConn = null;
    protected Connection                      newConn = null;
    protected Hashtable<String, IdHashMapper> idMappers = new Hashtable<String, IdHashMapper>();
    protected ProgressFrame              frame     = null;
    /**
     * Constructor.
     */
    public IdMapperMgr()
    {
    }
    
    /**
     * Sets the progress frame.
     * @param frame the frame
     */
    public void setFrame(final ProgressFrame frame)
    {
        this.frame = frame;
    }
    
    /**
     * Sets up the DBConnection for all the mappers (This is a Required Step before using it).
     * @param oldConn the old connection
     * @param newConn the new connection
     */
    public void setDBs(final Connection oldConn, final Connection newConn)
    {
        this.oldConn = oldConn;
        this.newConn = newConn;
    }

    /**
     * Create a Table Mapper with name, id and SQL.
     * @param tableName the table name
     * @param idName the id (primary key)
     * @param sql the SQL used to do the mapping
     * @return the IdHashMapper object
     */
    public IdTableMapper addTableMapper(final String tableName, final String idName, final String sql, final boolean doDelete)
    {
        if (StringUtils.isEmpty(idName))
        {
            String msg = String.format("The idName field is empty for table %s", tableName);
            log.error(msg);
            UIRegistry.showError(msg);
            throw new RuntimeException(msg);
        }
        
        log.debug("addTableMapper called for table: " + tableName);
        log.debug("addTableMapper called for sql: " + sql);
        
        if (oldConn == null || newConn == null)
        {
            throw new RuntimeException("setDBs MUST be called on IdMapperMgr before using it! oldConn["+oldConn+"]  newConn["+newConn+"]");
            
        }
        String name = tableName.toLowerCase();
        
        List<String> fieldNames = BasicSQLUtils.getFieldNamesFromSchema(oldConn, name);
        if (fieldNames == null || fieldNames.size() == 0)
        {
            String msg = String.format("There are no fields for table %s", name);
            log.error(msg);
            UIRegistry.showError(msg);
            throw new RuntimeException(msg);
        }
        
        if (!fieldNames.get(0).equals(idName))
        {
            log.error("Table["+name+"] doesn't have first column id["+idName+"]");
        }

        IdTableMapper idMapper = new IdTableMapper(name.toLowerCase(), idName, sql, doDelete);
        idMappers.put(idMapper.getName(), idMapper);
        idMapper.setFrame(frame);
        return idMapper;
    }
    
    /**
     * @param idMapper
     */
    public void addMapper(final IdTableMapper idMapper)
    {
        idMappers.put(idMapper.getName(), idMapper);
        idMapper.setFrame(frame);
    }
    
    /**
     * Creates a mapper with name and id.
     * @param tableName the name of the table
     * @param idName the id (primary key)
     * @return the same Mapper that was passed in
     * @throws SQLException
     */
    public IdTableMapper addTableMapper(final String tableName, final String idName)
    {
        return addTableMapper(tableName, idName, null, true);
    }
    
    /**
     * Creates a mapper with name and id.
     * @param tableName the name of the table
     * @param idName the id (primary key)
     * @param doDelete
     * @return the same Mapper that was passed in
     * @throws SQLException
     */
    public IdTableMapper addTableMapper(final String tableName, final String idName, final boolean doDelete)
    {
        return addTableMapper(tableName, idName, null, doDelete);
    }
    
    /**
     * Creates a Hash mapper with pre-installed SQL.
     * @param tableName the name of the mapper
     * @param sql the sql used to create the map
     * @param doDelete
     * @return the new mapper
     * @throws SQLException
     */
    public IdHashMapper addHashMapper(final String tableName, final String sql, final boolean doDelete)
    {
        if (oldConn == null || newConn == null)
        {
            throw new RuntimeException("setDBs MUST be called on IdMapperMgr before using it!");
            
        }
        
        IdHashMapper idMapper = new IdHashMapper(tableName.toLowerCase(), sql, doDelete);
        idMappers.put(idMapper.getName(), idMapper);
        idMapper.setFrame(frame);
        return idMapper;
    }
    
    /**
     * Creates a Hash mapper.
     * @param tableName the tableName of the mapper
     * @param doDelete
     * @return the IdHashMapper object
     */
    public IdHashMapper addHashMapper(final String tableName, final boolean doDelete)
    {
        return addHashMapper(tableName, null, doDelete);
    }
    
    /**
     * Return  the old connection.
     * @return the old connection
     */
    public Connection getOldConnection()
    {
        return oldConn;
    }

    /**
     * Returns the new connection.
     * @return the new connection
     */
    public Connection getNewConnection()
    {
        return newConn;
    }

    /**
     * Get a mapper by name and id.
     * @param tableName the table name
     * @param idName the id
     * @return the IdHashMapper object
     */
    public IdMapperIFace get(final String tableName, final String idName)
    {
        /*for (String nm : idMappers.keySet())
        {
            System.out.println(nm);
        }*/
        return idMappers.get(tableName.toLowerCase()+"_"+idName);
    }
    
    /**
     * Get a mapper by name.
     * @param name
     * @return the IdHashMapper object
     */
    public IdMapperIFace get(final String name)
    {
        return idMappers.get(name);
    }
    
    /**
     * Map a foreigh key.
     * @param fkTableName foreign key table name to be mapped from
     * @param fkIdName foreign key id to be mapped from
     * @param tableName table name to be mapped to
     * @param idName id to be mapped to
     */
    public void mapForeignKey(final String fkTableName, 
                              final String fkIdName, 
                              final String tableName, 
                              final String idName)
    {
        IdHashMapper idMapper = idMappers.get(tableName.toLowerCase()+"_"+idName);
        if (idMapper != null)
        {
            idMappers.put(idMapper.getName(), idMapper);
            idMappers.put(fkTableName.toLowerCase()+"_"+fkIdName, idMapper);
            
        } else
        {
            //System.err.println("Couldn't find ["+(tableName.toLowerCase()+"_"+idName)+"] to map to foreign key.");
            // ZZZ throw new RuntimeException("Couldn't find ["+(tableName.toLowerCase()+"_"+idName)+"] to map to foreign key.");
        }
    }
    
    /**
     * Cleanup map.
     */
    public void cleanup()
    {
        /*for (String key : idMappers.keySet())
        {
            System.out.println("["+key+"]");
        }*/

        for (IdHashMapper mapper : idMappers.values())
        {
            mapper.cleanup();
        }
        idMappers.clear();
    }
    
    /**
     * Return the singleton of the Mapper Manager.
     * @return IdMapperMgr instance
     */
    public static IdMapperMgr getInstance()
    {
        return idMapperMgr;
    }
    
    public void dumpKeys()
    {
        for (String key : idMappers.keySet())
        {
            System.out.println(key);
        }
    }

}
