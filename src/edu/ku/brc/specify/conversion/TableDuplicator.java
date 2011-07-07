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
import java.util.List;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 29, 2011
 *
 */
public class TableDuplicator
{
    private Connection conn;
    private String     tableName;
    private String     primaryKey;
    
    private PreparedStatement insertStmt;
    
    private IdMapperIFace mapper;
    
    private List<FieldMetaData> fields   = null;
    private int                 keyIndex = -1;
    private String              selectStr;
    private TableAutoNumbering  autoNum;
    private String              whereSQL;
    private String              orderBy;
    
    private String mapperName;
    
    /**
     * @param conn
     * @param tableName
     * @param primaryKey
     * @param whereSQL
     */
    public TableDuplicator(final Connection conn, 
                           final String tableName, 
                           final String primaryKey,
                           final String whereSQL,
                           final String orderBy,
                           final String postFix)
    {
        super();
        this.conn       = conn;
        this.tableName  = tableName;
        this.primaryKey = primaryKey;
        this.whereSQL   = whereSQL;
        this.orderBy    = orderBy;
        
        mapperName = tableName + "_duplicator_" + postFix;
        mapper     = IdMapperMgr.getInstance().addHashMapper(mapperName, true);
        autoNum    = new TableAutoNumbering(conn, tableName, primaryKey);
        
    }
    
    /**
     * 
     */
    public void initialize() throws SQLException
    {
        autoNum.initialize();
    }
    
    /**
     * 
     */
    protected void buildSQL() throws SQLException
    {
        fields = BasicSQLUtils.getFieldMetaDataFromSchema(conn, tableName);
        String        fieldSelStr = BasicSQLUtils.buildSelectFieldMetaDataList(fields, (String)null);
        StringBuilder sb          = new StringBuilder();
        for (int i=0;i<fields.size();i++)
        {
            if (sb.length() > 0) sb.append(',');
            sb.append('?');
            
            if (fields.get(i).getName().equals(primaryKey))
            {
                keyIndex = i+1;
            }
        }
        
        String wStr = (whereSQL != null ? ("WHERE " + whereSQL) : "");
        String oStr = (orderBy != null ? ("ORDER BY " + orderBy) : "");
        selectStr = String.format("SELECT %s FROM %s %s %s",  fieldSelStr, tableName, wStr, oStr);
        System.out.println(selectStr);
        String insertStr = String.format("INSERT INTO %s (%s) VALUES(%s)", tableName, fieldSelStr, sb.toString());
        System.out.println(insertStr);
        insertStmt = conn.prepareStatement(insertStr);
    }
    
    /**
     * @throws SQLException
     */
    public void duplicate() throws SQLException
    {
        buildSQL();
        
        int cnt = 0;
        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(selectStr);
        while (rs.next())
        {
            for (int i=1;i<fields.size()+1;i++)
            {
                if (keyIndex == i)
                {
                    int newId = autoNum.getNext();
                    int oldId = rs.getInt(i);
                    insertStmt.setObject(i, newId);
                    mapper.put(oldId, newId);
                    
                } else
                {
                    insertStmt.setObject(i, rs.getObject(i));
                }
            }
            insertStmt.execute();
            cnt++;
            if (cnt % 1000 == 0)
            {
                System.out.println(cnt);
                break;
            }
        }
        rs.close();
        stmt.close();
    }
    
    /**
     * 
     */
    public void cleanup() throws SQLException
    {
        if (insertStmt != null) insertStmt.close();
        autoNum.cleanup();
    }

    /**
     * @return the mapper
     */
    public IdMapperIFace getMapper()
    {
        return mapper;
    }

    /**
     * @return the mapperName
     */
    public String getMapperName()
    {
        return mapperName;
    }
}
