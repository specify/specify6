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
public class CreationChecker
{

    
    /**
     * 
     */
    public CreationChecker()
    {
        super();
    }

    protected void check()
    {
        String dbName           = "testfish"; 
        String itUsername       = "root";
        String itPassword       = "nessie1601";
        
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        
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
            
            Vector<Object[]> tblRows = BasicSQLUtils.query(connection, "show databases");
            for (Object[] row : tblRows)
            {
                String name = row[0].toString();
                if (name.endsWith("_6"))
                {
                    connection.setCatalog(name);
                    for (Object[] dataRow : BasicSQLUtils.query(connection, "show table status"))
                    {
                        if (dataRow[11] != null)
                        {
                            Timestamp createTime = (Timestamp)dataRow[11];
                            System.out.println(row[0].toString()+"  "+sdf.format(createTime));                        
                        }
                        break;
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
        CreationChecker checker = new CreationChecker();
        checker.check();

    }

}
