/* Copyright (C) 2013, University of Kansas Center for Research
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

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 29, 2011
 *
 */
public class TableAutoNumbering
{
    private Connection conn;
    private String     tableName;
    private String     primaryKey;
    private int        currentId = 0;
    
    private PreparedStatement pStmt = null;
    
    /**
     * @param conn
     * @param tableName
     */
    public TableAutoNumbering(final Connection conn, 
                              final String tableName,
                              final String primaryKey)
    {
        super();
        this.conn       = conn;
        this.tableName  = tableName;
        this.primaryKey = primaryKey;
    }
    
    public void initialize() throws SQLException
    {
        pStmt = conn.prepareStatement(String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", tableName, primaryKey));
    }
    
    /**
     * @return
     * @throws SQLException
     */
    public int getNext() throws SQLException
    {
        do
        {
            currentId++;
            if (currentId == Integer.MAX_VALUE)
            {
                currentId = Integer.MIN_VALUE;
            }
            pStmt.setInt(1, currentId);
            ResultSet rs = pStmt.executeQuery();
            if (rs.next())
            {
                if (rs.getInt(1) == 0)
                {
                    return currentId;
                }
            }
        } while (true);
    }
    
    /**
     * @throws SQLException
     */
    public void cleanup() throws SQLException
    {
        if (pStmt != null) pStmt.close();
    }
}
