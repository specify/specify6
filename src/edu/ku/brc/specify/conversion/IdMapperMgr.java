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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.ku.brc.ui.ProgressFrame;


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
    public IdTableMapper addTableMapper(final String tableName, final String idName, final String sql) throws SQLException
    {
        if (oldConn == null || newConn == null)
        {
            throw new RuntimeException("setDBs MUST be called on IdMapperMgr before using it! oldConn["+oldConn+"]  newConn["+newConn+"]");
            
        }
        String name = tableName.toLowerCase();
        
        List<String> fieldNames = new ArrayList<String>();
        BasicSQLUtils.getFieldNamesFromSchema(oldConn, name, fieldNames);
        if (!fieldNames.get(0).equals(idName))
        {
            throw new RuntimeException("Table["+name+"] doesn't have first column id["+idName+"]");
        }

        IdTableMapper idMapper = new IdTableMapper(name.toLowerCase(), idName, sql);
        idMappers.put(idMapper.getName(), idMapper);
        idMapper.setFrame(frame);
        return idMapper;
    }
    
    /**
     * Creates a mapper with name and id.
     * @param tableName the name of the table
     * @param idName the id (primary key)
     * @return the same Mapper that was passed in
     * @throws SQLException
     */
    public IdTableMapper addTableMapper(final String tableName, final String idName)  throws SQLException
    {
        return addTableMapper(tableName, idName, null);
    }
    
    /**
     * Creates a Hash mapper with pre-installed SQL.
     * @param tableName the name of the mapper
     * @param sql the sql used to create the map
     * @return the new mapper
     * @throws SQLException
     */
    public IdHashMapper addHashMapper(final String tableName, final String sql) throws SQLException
    {
        if (oldConn == null || newConn == null)
        {
            throw new RuntimeException("setDBs MUST be called on IdMapperMgr before using it!");
            
        }
        
        IdHashMapper idMapper = new IdHashMapper(tableName.toLowerCase(), sql);
        idMappers.put(idMapper.getName(), idMapper);
        idMapper.setFrame(frame);
        return idMapper;
    }
    
    /**
     * Creates a Hash mapper.
     * @param tableName the tableName of the mapper
     * @return the IdHashMapper object
     */
    public IdHashMapper addHashMapper(final String tableName) throws SQLException
    {
        return addHashMapper(tableName, null);
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
        return idMappers.get(tableName.toLowerCase()+"_"+idName);
    }
    
    /**
     * Get a mapper by name.
     * @param name
     * @return the IdHashMapper object
     */
    public IdMapperIFace get(final String name)
    {
        return idMappers.get(name.toLowerCase());
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
            System.err.println("Couldn't find ["+(tableName.toLowerCase()+"_"+idName)+"] to map to foreign key.");
        }
    }
    
    /**
     * Cleanup map.
     */
    public void cleanup() throws SQLException
    {
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
