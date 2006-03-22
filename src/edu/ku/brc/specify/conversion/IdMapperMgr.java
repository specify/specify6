/*
 * Filename:    $RCSfile: IdMapperMgr.java,v $
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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.ku.brc.specify.dbsupport.BasicSQLUtils;

/**
 * Manages all IdMappers and provides a way to provide foreign key mappnigs for columns in other tables
 * 
 * @author rods
 *
 */
public class IdMapperMgr
{
    protected static IdMapperMgr          idMapperMgr = null;
    
    protected Connection                  oldConn;
    protected Hashtable<String, IdMapper> idMappers = new Hashtable<String, IdMapper>();
    
    /**
     * Needs to be created manually before calling getInstance
     * @param oldConn the connection to the old database
     */
    public IdMapperMgr(final Connection oldConn)
    {
        this.oldConn = oldConn;
        idMapperMgr = this;
    }

    /**
     * @param tableName
     * @param idName
     * @return
     */
    public IdMapper addMapper(final String tableName, final String idName)
    {
        String name = tableName.toLowerCase();
        
        List<String> fieldNames = new ArrayList<String>();
        BasicSQLUtils.getFieldNamesFromSchema(oldConn, name, fieldNames);
        if (!fieldNames.get(0).equals(idName))
        {
            throw new RuntimeException("Table["+name+"] doesn't have first column id["+idName+"]");
        }

        IdMapper idMapper = new IdMapper(oldConn, name, idName);
        idMappers.put(idMapper.getName(), idMapper);
        return idMapper;
    }
    
    /**
     * @return
     */
    public Connection getOldConnection()
    {
        return oldConn;
    }

    /**
     * @param tableName
     * @param idName
     * @return
     */
    public IdMapper get(final String tableName, final String idName)
    {
        return idMappers.get(tableName.toLowerCase()+"_"+idName);
    }
    
    /**
     * @param fkTableName
     * @param fkIdName
     * @param tableName
     * @param idName
     */
    public void mapForeignKey(final String fkTableName, 
                              final String fkIdName, 
                              final String tableName, 
                              final String idName)
    {
        IdMapper idMapper = idMappers.get(tableName.toLowerCase()+"_"+idName);
        if (idMapper != null)
        {
            idMappers.put(idMapper.getName(), idMapper);
            idMappers.put(fkTableName.toLowerCase()+"_"+fkIdName, idMapper);
            
        } else
        {
            throw new RuntimeException("Couldn't find ["+(tableName.toLowerCase()+"_"+idName)+"] to map to foreign key.");
        }
    }
    
    /**
     * 
     */
    public void cleanup()
    {
        for (IdMapper mapper : idMappers.values())
        {
            mapper.cleanup();
        }
        idMappers.clear();
    }
    
    /**
     * @return
     */
    public static IdMapperMgr getInstance()
    {
        return idMapperMgr;
    }

}
