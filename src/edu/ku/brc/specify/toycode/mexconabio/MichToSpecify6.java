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
package edu.ku.brc.specify.toycode.mexconabio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MichToSpecify6
{

    private Connection oldDBConn = null;
    private Connection newDBConn = null;
    
    private String     oldDBName;
    private String     newDBName;
    
    
    /**
     * @param oldDBName
     * @param newDBName
     */
    public MichToSpecify6(String oldDBName, String newDBName)
    {
        super();
        
        this.oldDBName = oldDBName;
        this.newDBName = newDBName;
        
        String connStr = "jdbc:mysql://localhost/%s?characterEncoding=UTF-8&autoReconnect=true";
        try
        {
            oldDBConn = DriverManager.getConnection(String.format(connStr, oldDBName), "root", "root");
            newDBConn = DriverManager.getConnection(String.format(connStr, newDBName), "root", "root");
       } catch (SQLException e)
        {
            e.printStackTrace();
        }
     }
    
    private void createAgents()
    {
        
    }
    
    /**
     * 
     */
    public void process()
    {
        String sql = "SELECT * FROM conabio ORDER BY BarCD";
    }
    
}
