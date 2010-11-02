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
/**
 * 
 */
package edu.ku.brc.specify.toycode;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Vector;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 1, 2009
 *
 */
public class AgentRemover
{

    
    /**
     * 
     */
    public AgentRemover()
    {
        super();
    }

    protected void check()
    {
        
        String sql  = "SELECT COLUMN_NAME FROM `information_schema`.`COLUMNS` where TABLE_SCHEMA = 'sp6_schema' and TABLE_NAME = '%s' AND (COLUMN_NAME = 'CreatedByAgentID' OR COLUMN_NAME = 'ModifiedByAgentID')";
        String sql2 = "SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE where TABLE_SCHEMA = 'sp6_schema' AND TABLE_NAME = '%s' AND COLUMN_NAME = '%s'";
        
        String dbName           = "sp6_schema"; 
        String itUsername       = "root";
        String itPassword       = "root";
        
        DBConnection colDBConn  = null;
        Connection   connection = null;
        Statement    stmt       = null;
        try
        {
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
            String             connStr    = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, "localhost", dbName, itUsername, itPassword, driverInfo.getName());
            
            System.err.println(connStr);
            
            colDBConn  = DBConnection.createInstance(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), dbName, connStr, itUsername, itPassword);
            connection = colDBConn.createConnection();
            stmt       = connection.createStatement();
            
            BasicSQLUtils.setDBConnection(connection);
            
            for (Object[] dataRow : BasicSQLUtils.query(connection, "show tables"))
            {
                if (dataRow[0] != null)
                {
                    String tableName = dataRow[0].toString();
                    if (tableName.equals("agent")) continue;
                    
                    String sqlStr = String.format(sql, tableName, "%id", "%agent%");
                    for (Object fieldNameObj : BasicSQLUtils.querySingleCol(sqlStr))
                    {
                        String fieldName = fieldNameObj.toString();
                        
                        System.out.println(tableName+" "+fieldName);
                        
                        sqlStr = String.format(sql2, tableName, fieldName);
                        for (Object conName : BasicSQLUtils.querySingleCol(sqlStr))
                        {
                            System.out.println(tableName+" "+fieldName+"  DROPPING["+conName.toString()+"]");
                            String dropCol = String.format("ALTER TABLE %s DROP FOREIGN KEY %s", tableName, conName.toString());
                            BasicSQLUtils.update(dropCol);
                        }
                        
                        String dropCol = String.format("ALTER TABLE %s DROP COLUMN %s", tableName, fieldName);
                        BasicSQLUtils.update(dropCol);
                    }
                }
            }
            stmt.close();
            colDBConn.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        AgentRemover checker = new AgentRemover();
        checker.check();

    }

}
